/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLineHandler;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.OperationConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Sequence;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.server.TaskStatistics;
import com.ibm.icu.util.StringTokenizer;

/**
 * This is a function component that calls an AssemblyLine(AL) on a local or
 * remote server. The call can be made in three different modes.
 * <OL>
 * <LI>Run and wait for result. Each call to perform() starts the target AL and
 * returns the result object for the execution, typically the last work Entry.
 * initialize() does not start the target AL.
 * <LI>Run in background. The target AL runs on its own, in the background.
 * initialize() does not start the target AL.
 * <LI>Manual (cycle mode). The target AL is controlled by the FC by executing
 * a cycle for each call to perform(). initialize() starts the target AL.
 * </OL>
 * When the FC runs an AssemblyLine in background mode, the FC keeps a reference
 * to the target AL and the FC can return the status of the running/terminated
 * assemblyline. You obtain this status simply by calling the FC with a null or
 * empty Entry parameter. The returned Entry object contains the reference to
 * the target AL in its "value" attribute. If you pass a null value to the FC,
 * the return value is the actual reference to the target AL. This complies with
 * the calling conventions of Function components in general (e.g. return an
 * Entry object if called with Entry object).
 * <p>
 * When the FC runs an AssemblyLine in background mode, you can also call the FC
 * with specific string values to obtain various info about the target AL:
 * 
 * <pre>
 * fc.perform(&quot;target&quot;); // returns the object reference of the target al
 * fc.perform(&quot;active&quot;); // returns either &quot;&quot;active&quot;, &quot;aborted&quot; or &quot;terminated&quot; depending on the target al status
 * fc.perform(&quot;error&quot;); // returns the java.lang.Exception object when the status is &quot;aborted&quot;
 * fc.perform(&quot;result&quot;); // returns the current result Entry object
 * fc.perform(&quot;stop&quot;); // tries to terminate an active target al - may throw an error if call does not succeed
 * </pre>
 * 
 * When the FC is called in this mode with an Entry object, the Entry object can
 * contain one or more of the above keywords in the <i>command</i> Attribute.
 * The returned Entry object is then populated with the same values as described
 * above. So, rather than calling perform() with all keywords, you can create an
 * Entry with all keywords as attributes in the Entry object and get away with
 * one call to perform():
 * 
 * var e = system.newEntry(); e.setAttribute("command", "target, status"); var
 * result = fc.perform(e); task.logmsg("The status is: " +
 * result.getString("status"));
 * 
 * When the FC runs an AL in manual mode, each call with an Entry object causes
 * one cycle to be executed in the target AL. The returned Entry object is the
 * work entry result of the cycle. When the target AL has completed, a null
 * entry is returned. If the cycle execution causes an error, that error is
 * re-thrown by the FC (you should use a try/catch block in your script).
 * <p>
 * To provide a TaskCallBlock (TCB) you can use the fc.getTCB() and set
 * parameters in the returned TCB object. This object will be used the next time
 * an AssemblyLine is started by this FC. You should only set connector
 * parameters in the returned TCB as this FC will potentially overwrite the
 * runmode and initial work entry.
 * <p>
 * Another way to set TCB parameters are by using the output attribute map where
 * you define variables with the specific prefix "$tcb.". When these attributes
 * are found in the entry they will be moved to the TCB and removed from the
 * entry. This will only work when the FC runs an assemblyline each time the FC
 * is called (e.g. run and await completion).
 */
public class AssemblyLineFC extends Function {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "assemblylinefc";

	/**
	 * Parameter name.
	 */
	public final static String ASSEMBLYLINE = "assemblyLine";
	/**
	 * Parameter name.
	 */
	public final static String SERVER = "server";
	/**
	 * Parameter name.
	 */
	public final static String CONFIG = "config";
	/**
	 * Parameter name.
	 */
	public final static String CYCLE_MODE = "mode";
	/**
	 * Parameter name.
	 */
	public final static String SIMULATE_MODE = "simulate.mode";
	/**
	 * Parameter name.
	 */
	public final static String TCB = "tcb";
	/**
	 * Parameter name.
	 */
	public final static String CUSTOM_SEC = "customSecurity";
	/**
	 * Parameter name.
	 */
	public final static String USE_TCB_ATTRS = "useTCBAttributes";
	/**
	 * Parameter name.
	 */
	public final static String SHARE_LOG = "shareLog";
	/**
	 * Parameter name.
	 */
	public final static String OPERATION = "operation";

	/**
	 * TCB prefix.
	 */
	private final static String TCB_PREFIX = "$tcb.";

	/**
	 * TCB accumulator.
	 */
	private final static String TCB_ACCUMULATOR = "$tcb.accumulator";

	/**
	 * Prefix for operation initialization.
	 */
	public final static String OPERATION_INIT_PREFIX = "$initialize.";

	/**
	 * {@link AssemblyLine} instance
	 */
	private AssemblyLine al;

	/**
	 * {@link Sequence} instance
	 */
	private Sequence seq;

	/**
	 * Representation of the session.
	 */
	private Session session;

	/**
	 * Configuration instance corresponding to a specific configuration ID.
	 */
	private ConfigInstance configInstance;

	/**
	 * Cycle of the remote AL.
	 */
	private AssemblyLineHandler remoteALcycle;

	/**
	 * Remote AssemblyLine
	 */
	private com.ibm.di.api.remote.AssemblyLine remoteAL;

	/**
	 * Remote Sequence
	 */
	private com.ibm.di.api.remote.Sequence remoteSeq;

	/**
	 * Indicates if the AL is in cycle mode.
	 */
	private boolean cycleMode = false;
	/**
	 * Indicates if the AL is in 'Run and wait for result' - mode.
	 */
	private boolean awaitCompletion = false;

	/**
	 * When true the FC will interpret attributes with a "$tcb." prefix as
	 * parameters to the TCB and remove them from the entry
	 */
	private boolean useTCBAttributes = false;

	/**
	 * If true, the called AssemblyLine will use the same logging as this
	 * Component
	 */
	private boolean shareLog = false;

	/**
	 * {@link TaskCallBlock} instance.
	 */
	private TaskCallBlock userTCB = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
	.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor.
	 */
	public AssemblyLineFC() {
		super();
	}

	/**
	 * Executes the AL in the specified mode.
	 * 
	 * @param obj
	 *            the initial work entry for the AssemblyLine being executed (an
	 *            object of type com.ibm.di.entry.Entry)
	 * @return the result entry of the AssemblyLine
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();

		if (cycleMode) {
			return executeCycle(obj);
		} else if (awaitCompletion) {
			return runOnce(obj);
		} else {
			startInstance(obj, false);
			return checkStatus(obj);
		}
	}

	/**
	 * Checks the status of the parameter.
	 * 
	 * @param p
	 *            {@link Entry} {@link String} or <code>null</code> .
	 * @return the result of the check.
	 * @throws Exception
	 */
	private Object checkStatus(Object p) throws Exception {
		Object obj = null;
		String cmd = null;

		if (p instanceof Entry)
			cmd = ((Entry) p).getString("command");
		else if (p instanceof String)
			return getStatusParam((String) p);
		else if (p == null)
			return getStatusParam("target");
		else
			return null;

		if (cmd == null)
			return getStatusParam("target");

		StringTokenizer st = new StringTokenizer(cmd, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			obj = getStatusParam(tok);
			if (obj != null)
				((Entry) p).setAttribute(tok, obj);
			else
				((Entry) p).removeAttribute(tok);
		}
		return p;

	}

	/**
	 * Obtains the status of the AL for the provided parameter.
	 * 
	 * @param p
	 *            String
	 * @return the status parameter.
	 * @throws Exception :
	 *             {@link DIException} or {@link RemoteException}
	 */
	private Object getStatusParam(String p) throws Exception {
		if ("active".equalsIgnoreCase(p)) {
			if (al != null)
				return Boolean.valueOf(al.isAlive());
			else if (remoteALcycle != null)
				return Boolean.valueOf(remoteALcycle.getAssemblyLine().isActive());
			else if (remoteAL != null)
				return Boolean.valueOf(remoteAL.isActive());
			else if (seq != null)
				return Boolean.valueOf(seq.isAlive());
			else if (remoteSeq != null)
				return Boolean.valueOf(remoteSeq.isActive());
		} else if ("target".equalsIgnoreCase(p)) {
			if (al != null)
				return al;
			else if (remoteALcycle != null)
				return remoteALcycle;
			else if (remoteAL != null)
				return remoteAL;
			else if (seq != null)
				return seq;
			else if (remoteSeq != null)
				return remoteSeq;
		} else if ("result".equalsIgnoreCase(p)) {
			if (al != null)
				return al.getResult();
			else if (remoteALcycle != null)
				return remoteALcycle.getAssemblyLine().getResult();
			else if (remoteAL != null)
				return remoteAL.getResult();
			else if (seq != null)
				return seq.getResult();
			else if (remoteSeq != null)
				return remoteSeq.getResult();
		} else if ("error".equalsIgnoreCase(p)) {
			TaskStatistics stats = null;
			if (al != null)
				stats = al.getStats();
			else if (remoteALcycle != null)
				stats = remoteALcycle.getAssemblyLine().getStatistics();
			else if (remoteAL != null)
				stats = remoteAL.getStatistics();
			else if (seq != null)
				stats = seq.getStats();
			else if (remoteSeq != null)
				stats = remoteSeq.getStatistics();
			if (stats != null)
				return stats.getError();
		} else if ("stop".equalsIgnoreCase(p)) {
			if (al != null)
				al.shutdown();
			else if (remoteALcycle != null)
				remoteALcycle.close();
			else if (remoteAL != null)
				remoteAL.stop();
			else if (seq != null)
				seq.shutdown();
			else if (remoteSeq != null)
				remoteSeq.stop();
			else
				return null;
			return "OK";
		}
		return null;
	}

	/**
	 * Run once the AL.
	 * 
	 * @param obj
	 *            {@link Entry}
	 * @return The last "work" entry object.
	 * @throws Exception :
	 *             if error occurs
	 */
	private Object runOnce(Object obj) throws Exception {
		startInstance(obj, true);
		if (al != null) {
			Exception e = al.getStats().getError();
			if (e != null)
				throw e;
			return al.getResult();
		} else if (remoteAL != null) {
			TaskStatistics stats = remoteAL.getStatistics();
			if (stats != null && stats.getError() != null)
				throw stats.getError();
			return remoteAL.getResult();
		} else if (seq != null) {
			Exception e = seq.getStats().getError();
			if (e != null)
				throw e;
			return seq.getResult();
		} else if (remoteSeq != null) {
			TaskStatistics stats = remoteSeq.getStatistics();
			if (stats != null && stats.getError() != null)
				throw stats.getError();
			return remoteSeq.getResult();
		} else {
			return null;
		}
	}

	/**
	 * Executes an AssemblyLine cycle.
	 * 
	 * @param work
	 *            the work entry to use
	 * @return the work entry at the end of the cycle.
	 * 
	 * @throws Exception :
	 *             if the argument isn't an {@link Entry} instance
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 * 
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	private Object executeCycle(Object work) throws Exception {
		Entry workEntry = null;
		if (work instanceof Entry && ((Entry) work).size() > 0)
			workEntry = (Entry) work;

		try {
			if (al != null)
				return al.executeCycle(workEntry);
			else if (remoteALcycle != null)
				return remoteALcycle.executeCycle(workEntry);
		} catch (Throwable t) {
			throw new Exception(t);
		}

		throw new Exception(sResHash.getString("ALFC.NOAL.INITIATED"));
	}

	/**
	 * This method invokes an operation in the target AssemblyLine.
	 * 
	 * @param work
	 *            The work entry
	 * @param operation
	 *            The operation to invoke
	 * @return The work entry from the target AssemblyLine
	 * @throws Exception
	 */
	public Object performOperation(Object work, String operation)
	throws Exception {
		return performOperation(work, operation, null);
	}

	/**
	 * Performs the specified operation.
	 * 
	 * @param work
	 *            the work entry to use
	 * @param operation
	 *            The AL operation.
	 * @param opentry
	 *            {@link Entry}
	 * @return the work entry at the end of the operation.
	 * @throws Exception :
	 *             if an error occurs.
	 */
	public Object performOperation(Object work, String operation, Entry opentry)
	throws Exception {
		Entry workEntry = null;
		if (work instanceof Entry && ((Entry) work).size() > 0)
			workEntry = (Entry) work;

		try {
			getTCB().setALOperation(operation);
			getTCB().setOperationInitParams(opentry);
			if (al != null) {
				return al.executeCycle(workEntry, true);
			} else if (remoteALcycle != null) {
				return remoteALcycle.executeCycle(workEntry, true);
			}
		} catch (Throwable t) {
			throw new Exception(t);
		}

		throw new Exception(sResHash.getString("ALFC.NOAL.INITIATED"));
	}

	/**
	 * Returns the TaskCallBlock to use before starting an instance. Currently,
	 * you cannot provide a TCB when cycling a remote AL.
	 * 
	 * @return TaskCallBlock
	 * @throws Exception
	 */
	public TaskCallBlock getTCB() throws Exception {
		if (getParam(TCB) instanceof TaskCallBlock)
			userTCB = (TaskCallBlock) getParam(TCB);

		if (userTCB == null) {
			String name = (String) getParam(ASSEMBLYLINE);
			if (configInstance != null || isSequence(name))
				userTCB = new TaskCallBlock();
			else
				userTCB = new TaskCallBlock(name, SystemFunctions.loadAssemblyLine(name, getRSInterface()), null);
		}
		return userTCB;
	}

	/**
	 * Strips the TCB Attributes from an {@link Entry} and dumps it.
	 * 
	 * @param entry
	 *            {@link Entry}
	 * @param tcb
	 *            TaskCallBlock
	 * @throws Exception :
	 *             if the prefix of the entries value is not recognized.
	 */
	private void stripTCBAttributes(Entry entry, TaskCallBlock tcb)
	throws Exception {
		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (!names[i].startsWith(TCB_PREFIX))
				continue;

			Attribute a = entry.getAttribute(names[i]);
			Object value = a.getValue(0);
			entry.removeAttribute(names[i]);

			if (names[i].equals(TCB_ACCUMULATOR)) {
				tcb.setAccumulator(value);
			} else {
				String str = names[i].substring(TCB_PREFIX.length());
				int index = str.indexOf(".");
				if (index == -1) {
					throw new Exception(sResHash.getString("ALFC.UNKNOWN.TCB",
							names[i]));
				}
				String connector = str.substring(0, index);
				String parameter = str.substring(index + 1);
				tcb.setConnectorParameter(connector, parameter, value, a
						.getProtected());
			}

		}
	}

	/**
	 * Called once to initialize the function
	 * 
	 * @param obj
	 *            this parameter is ignored
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void initialize(Object obj) throws Exception {
		int val = getConfiguration().getIntegerParameter(CYCLE_MODE, 1);
		cycleMode = (val == 2);
		awaitCompletion = (val == 0);

		useTCBAttributes = getConfiguration().getBooleanParameter(
				USE_TCB_ATTRS, false);
		shareLog = getConfiguration().getBooleanParameter(SHARE_LOG, false);

		// Establish server connection
		connectServer((String) getParam(SERVER));
		if (session != null)
			getConfigInstance("" + getParam(CONFIG));

		// Start instance
		if (cycleMode)
			startInstance(null, false);

		super.initialize(null);
	}

	/**
	 * Establish a server connection.
	 * 
	 * @param server
	 *            server address
	 * @return Session
	 * @throws Exception :
	 *             never.
	 */
	public Session connectServer(String server) throws Exception {
		if (getConfiguration() == null)
			return null;

		if (server == null || server.equals(""))
			server = "local";

		if (!server.equalsIgnoreCase("local")) {
			if (server.indexOf(":") == -1)
				server += ":1099";

			boolean customSecurity = getConfiguration().getBooleanParameter(
					CUSTOM_SEC, false);
			String currentValue = System
			.getProperty("api.client.ssl.custom.properties.on");
			try {
				System.setProperty("api.client.ssl.custom.properties.on", ""
						+ customSecurity);
				SessionFactory sf = (SessionFactory) Naming.lookup("rmi://"
						+ server + "/SessionFactory");
				if (sf != null)
					session = sf.createSession();
			} finally {
				System.setProperty("api.client.ssl.custom.properties.on", ""
						+ currentValue);
			}
		}

		return session;
	}

	/**
	 * 
	 * Retrieves the ConfigInstance object using the provided ID from the
	 * current session.
	 * 
	 * @param ci
	 *            the ID of the ConfigInstance to look for.
	 * @return the ConfigInstance object or null.
	 * @throws Exception
	 *             if unable to establish a connection to the remote server or
	 *             the provided ID is null or an empty string.
	 */
	public ConfigInstance getConfigInstance(String ci) throws Exception {
		if (session == null)
			connectServer((String) getParam(SERVER));

		if (session == null)
			return null;

		if (configInstance == null) {
			if (ci == null || ci.trim().equals("")) {
				throw new Exception(sResHash.getString(
						"ALFC.NOGET.CONFIG.INSTANCE", "" + getParam(SERVER)));
			}

			configInstance = session.getConfigInstance(ci);
		}
		return configInstance;
	}

	/**
	 * Retrieves session object.
	 * 
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * Starts AL instance.
	 * 
	 * @param obj
	 *            Entry
	 * @throws Exception
	 */
	private void startInstance(Object obj, boolean sync) throws Exception {
		TaskCallBlock tcb = getTCB();

		// Set a few settings params in TCB
		tcb.setALSetting("debug", "" + getConfiguration().getDebug(false));

		// Set the operation
		String operation = (String) getParam(OPERATION);
		if (operation != null && operation.length() > 0)
			tcb.setALOperation(operation);

		// Get the init params off the raw configuration
		BaseConfiguration ip = getConfiguration();
		List<String> names = ip.getKeys(BaseConfiguration.RECURSIVE_ONELEVEL);
		for (int i = 0; i < names.size(); i++) {
			String str = names.get(i);
			if (str.startsWith(OPERATION_INIT_PREFIX))
				tcb.setOperationInitParam(str.substring(OPERATION_INIT_PREFIX
						.length()), ip.getParameter(str));
		}

		Entry iwe = (obj instanceof Entry) ? (Entry) obj : null;

		// Move TCB params
		if (iwe != null && useTCBAttributes)
			stripTCBAttributes(iwe, tcb);

		if (configInstance != null) {
			String alName = (String) getParam(ASSEMBLYLINE);
			if (cycleMode) {
				Entry initial = (iwe != null && iwe.size() > 0) ? iwe : null;
				remoteALcycle = configInstance.startAssemblyLineManual(alName, initial);

			} else {
				if (iwe != null && iwe.size() > 0)
					tcb.setInitialWorkEntry(iwe);

				if (isSequence(alName))
					remoteSeq = configInstance.startSequence(alName, tcb, sync);
				else
					remoteAL = configInstance.startAssemblyLine(alName, tcb, sync);
			}
		} else {
			RS rs;
			String configID = (String) getParam(CONFIG);
			if (configID != null && configID.length() > 0) {
				rs = RS.getServer(configID);
				if (rs == null)
					throw new Exception(ResourceHash.getHash("miserver").getString("Scheduler.cannot.find.configinstance", configID));
			} else {
				rs = (RS)getRSInterface();
				if (rs == null)
					throw new Exception(sResHash.getString("ALFC.CANNOT.RUN.AL.WARNING"));
			}

			String alName = tcb.getAssemblyLineName();
			if (alName == null)
				alName = (String) getParam(ASSEMBLYLINE);
			if (cycleMode && ! isSequence(alName) )
				tcb.setRunMode(AssemblyLine.RUNMODE_MANUAL);

			if (iwe != null && iwe.size() > 0)
				tcb.setInitialWorkEntry(iwe);

			// if null the AssemblyLine will treat it as false
			tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE,
					getParam(SIMULATE_MODE));

			// Let the server throw the Exception for no name, so we don't have
			// to add another error message
			if (alName == null)
				rs.startAL(tcb);

			Vector<Object> args = new Vector<Object>();
			args.add(tcb);
			if (shareLog && getLog() != null)
				args.add(getLog());

			if (isSequence(alName)) {
				seq = rs.startSequence(alName, args);
				if (sync)
					seq.join();
			} else {
				al = rs.startAL(alName, args);
				if (sync)
					al.join();
			}
		}
	}

	/**
	 * This method frees any resources allocated. If in manual mode, the
	 * AssemblyLine will also be terminated gracefully.
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void terminate() throws Exception {
		if (remoteALcycle != null) {
			remoteALcycle.close();
		}
		if (cycleMode && al != null) {
			al.executeTerminateAL();
		}
		al = null;
		remoteAL = null;
		remoteALcycle = null;
		seq = null;
		remoteSeq = null;
		configInstance = null;
		session = null;
		userTCB = null;

		super.terminate();
	}

	/**
	 * This method modifies the schema in the provided configuration. The intent
	 * is to allow the FC to provide a schema definition dynamically based on a
	 * given configuration.
	 * 
	 * @param config
	 *            {@link FunctionConfig}
	 * @return boolean
	 * @throws Exception :
	 *             never
	 */
	public boolean updateSchema(FunctionConfig config) throws Exception {
		setConfiguration(config.getFunctionConfig());
		return updateSchemaInternal(config);
	}

	/**
	 * This method modifies the schema in the provided configuration. The intent
	 * is to allow the FC to provide a schema definition dynamically based on a
	 * given configuration.
	 * 
	 * @param config
	 *            The connector configuration
	 * @return Returns true when the update is completed.
	 * @throws Exception
	 *             An exception is thrown if this method fails.
	 */
	public boolean updateSchemaConnector(ConnectorConfig config)
	throws Exception {
		setConfiguration(config.getConnectionConfig());
		return updateSchemaInternal(config);
	}

	/**
	 * This method obtains the AssemblyLineConfig object for the target AL and
	 * updates the current configuration's schema to reflect the input/output
	 * parameters the target AL has defined.
	 * 
	 * @param config
	 *            The connector configuration
	 * @return Returns true when the update is completed.
	 * @throws Exception
	 *             An exception is thrown if this method fails.
	 */
	private boolean updateSchemaInternal(ConnectorConfig config)
	throws Exception {

		// fill the input Schema in
		SchemaConfig inSchema = config.getSchema(true);
		List<Entry> in = querySchema(Boolean.TRUE);

		HashSet<String> existingNames = new HashSet<String>();
		List<String> list = inSchema.getItemNames();
		for (int i = 0; i < list.size(); i++)
			existingNames.add(list.get(i).toLowerCase(Locale.ENGLISH));

		for (int i = 0; in != null && i < in.size(); i++) {
			String name = in.get(i).getString("name");
			String lowerName = name.toLowerCase(Locale.ENGLISH);
			if (!existingNames.contains(lowerName)) {
				inSchema.newItem(name);
				existingNames.add(lowerName);
			}
		}

		// fill the output Schema in
		SchemaConfig outSchema = config.getSchema(false);
		List<Entry> out = querySchema(Boolean.FALSE);

		existingNames = new HashSet<String>();
		list = outSchema.getItemNames();
		for (int i = 0; i < list.size(); i++)
			existingNames.add(list.get(i).toLowerCase(Locale.ENGLISH));

		for (int i = 0; out != null && i < out.size(); i++) {
			String name = out.get(i).getString("name");
			String lowerName = name.toLowerCase(Locale.ENGLISH);
			if (!existingNames.contains(lowerName)) {
				outSchema.newItem(name);
				existingNames.add(lowerName);
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Entry> querySchema(Object source) throws Exception {
		Boolean input = null;
		ConnectorConfig cfg = null;
		List<Entry> result = null;

		// parser the input
		if (source != null) {
			Object[] arr = null;
			if (source instanceof Object[]) {
				arr = (Object[]) source;
			} else {
				arr = new Object[] { source };
			}

			for (int i = 0; i < arr.length; i++) {
				Object obj = arr[i];
				if (cfg == null && obj instanceof FunctionConfig) {
					cfg = (FunctionConfig) obj;
					setConfiguration(((FunctionConfig) obj).getFunctionConfig());
				} else if (cfg == null && obj instanceof ConnectorConfig) {
					cfg = (ConnectorConfig) obj;
					setConfiguration(cfg.getConnectionConfig());
				} else if (input == null && obj instanceof Boolean) {
					input = (Boolean) obj;
				}
			}
		}

		if (getConfiguration() == null) {
			return null;
		} else if (cfg == null) {
			cfg = (ConnectorConfig) getConfiguration().getParent();
		}

		if (input == null) {
			input = Boolean.TRUE;
		}

		String al = (String) getParam(ASSEMBLYLINE);
		if (cfg == null || al == null || al.length() == 0 || isSequence(al))
			return null;

		AssemblyLineConfig alc = null;
		// Establish server connection
		if (connectServer((String) getParam(SERVER)) != null) {
			getConfigInstance((String) getParam(CONFIG));
			alc = configInstance.getConfiguration().getAssemblyLine(al);
		} else {
			alc = cfg.getMetamergeConfig().getAssemblyLine(al);
		}

		SchemaConfig schema = null;
		AttributeMapConfig attMap = null;

		// If an operation is used then only pull the input/output schema for
		// the operation
		String mode = cfg.getMode();
		if (cfg instanceof FunctionConfig)
			mode = (String) getParam(OPERATION);

		if (mode != null && mode.trim().length() > 0) {
			OperationConfig oc = alc.getOperation(mode);
			if (oc == null)
				oc = alc.getOperation("querySchema");
			if (oc == null) {
				// Try using specific operations 

				if (ConnectorConfig.UPDATE_MODE.equals(mode)) {
					// special code for Update, try both modEntry and putEntry
					oc = alc.getOperation("modEntry");
					if (oc != null) {
						result = getListFromOperation(oc, input);
						oc = alc.getOperation("putEntry");
						if (oc != null) {
							List<Entry> add = getListFromOperation(oc, input);
							if (add != null)
								result.addAll(add);
						}
						return result;
					}
				}

				if (ConnectorConfig.LOOKUP_MODE.equals(mode))
					oc = alc.getOperation("findEntry");
				else if (ConnectorConfig.UPDATE_MODE.equals(mode) ||
						ConnectorConfig.ADDONLY_MODE.equals(mode))
					oc = alc.getOperation("putEntry");
				else if (ConnectorConfig.ITERATOR_MODE.equals(mode))
					oc = alc.getOperation("getNextEntry");
				else if (ConnectorConfig.CALL_REPLY_MODE.equals(mode))
					oc = alc.getOperation("queryReply");				
				else if (ConnectorConfig.DELETE_MODE.equals(mode))
					oc = alc.getOperation("deleteEntry");				
			}
			if (oc != null) 
				return getListFromOperation(oc, input);
		}

		schema = alc.getSchema(!input);
		// Add input parameters from AL schema if available
		result = addSchemaToList(schema);
		if (result != null)
			return result;

		// Try Attribute Map
		attMap = alc.getAttributeMap(!input);
		result = addAttributeMapToList(attMap);
		if (result != null)
			return result;

		result = new Vector<Entry>();
		ArrayList<String> lowerCaseNames = new ArrayList<String>();

		// Add attribute names from input connectors
		List<BaseConfiguration> items = alc.getEntryFeedComponents().getConfigurations(null);
		alc.getDataFlowComponents().getConfigurations(items);

		for (int i = 0; i < items.size(); i++) {
			BaseConfiguration bc = items.get(i);
			if (!(bc instanceof ConnectorConfig) || !bc.getEnabled()) {
				continue;
			}

			ConnectorConfig cc = (ConnectorConfig) bc;
			if (input) {
				if (cc.getMode().equals(ConnectorConfig.ADDONLY_MODE)
						|| cc.getMode().equals(ConnectorConfig.UPDATE_MODE))
					continue;
			} else {
				if (cc.getMode().equals(ConnectorConfig.LOOKUP_MODE)
						|| cc.getMode().equals(ConnectorConfig.DELETE_MODE))
					continue;
			}
			boolean b = input;
			if (cc.getMode().equals(ConnectorConfig.ITERATOR_MODE))
				b = true;

			List<Entry> temp = null;
			if (alc.autoMapAllAttributes(cc.getName())) {
				schema = cc.getSchema(b);
				temp = addSchemaToList(schema);
			} else {
				attMap = cc.getAttributeMap(b);
				temp = addAttributeMapToList(attMap);
			}

			for (int j = 0; temp != null && j < temp.size(); j++) {
				String attName = ((Entry) temp.get(j)).getString("name")
				.toLowerCase();
				if (!lowerCaseNames.contains(attName)) {
					result.add(temp.get(j));
				}
			}
		}

		return result;
	}

	private List<Entry> getListFromOperation(OperationConfig oc, boolean input) {
		List<Entry> result = addSchemaToList(oc.getSchema(!input));
		if (result != null)
			return result;
		else
			return addAttributeMapToList(oc.getAttributeMap(!input));
	}

	/**
	 * The method returns a {@link List} holding entries with the values from
	 * the {@link SchemaConfig} argument.
	 * 
	 * @param src
	 *            {@link SchemaConfig}
	 * @return List or <code>null</code> , if argument is <code>null</code>
	 *         or empty
	 */
	private List<Entry> addSchemaToList(SchemaConfig src) {
		if (src == null)
			return null;

		List<String> names = src.getItemNames();
		List<Entry> result = new Vector<Entry>();

		if (names == null || names.size() == 0) {
			return null;
		}

		for (int i = 0; i < names.size(); i++) {

			SchemaItemConfig sic = src.getItem(names.get(i));

			if (sic.getEnabled()) {
				Entry e = new Entry();
				e.setAttribute("name", names.get(i));
				e.setAttribute("syntax", sic.getJavaClass());
				result.add(e);
			}
		}

		if (result.size() > 0)
			return result;
		else
			return null;
	}

	/**
	 * The method returns a {@link List} holding entries with the values from
	 * the {@link AttributeMapConfig} argument.
	 * 
	 * @param src
	 *            {@link AttributeMapConfig}
	 * @return List or <code>null</code> , if argument is <code>null</code>
	 *         or empty
	 */
	private List<Entry> addAttributeMapToList(AttributeMapConfig src) {
		if (src == null)
			return null;

		List<String> names = src.getAttributeNames();
		List<Entry> result = new Vector<Entry>();

		if (names == null || names.size() == 0) {
			return null;
		}

		for (int i = 0; i < names.size(); i++) {

			AttributeMapItem ami = src.getAttributeMapItem(names.get(i));

			if (ami.getEnabled()) {
				Entry e = new Entry();
				e.setAttribute("name", names.get(i));
				e.setAttribute("syntax", ami.getType());
				result.add(e);
			}
		}

		if (result.size() > 0)
			return result;
		else
			return null;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Returns true if the AssemblyLine name is really a sequence name
	 */
	private boolean isSequence(String name) {
		return name != null && 
		name.contains("/" + MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/");
	}
}
