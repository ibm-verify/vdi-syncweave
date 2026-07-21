/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;

/**
 * This class uses the AssemblyLineFC function to invoke operations on an
 * AssemblyLine. The behavior of this class depends on the operations defined by
 * the target AssemblyLine. If the target AL has defined connector interface
 * methods that match the connector interface names, this connector will invoke
 * the corresponding operation in the target AL. If a mode ends up in an
 * internal connector interface method with no corresponding operation defined
 * an exception is thrown. The exception is the CallReply mode which is the
 * default for all non-standard modes.
 * 
 * A connector interface based adapter AssemblyLine must define operations that
 * correspond to the names found in the connector interface. This means that in
 * order to act as an Iterator it should define "selectentries" and "getnext" as
 * valid operations. This connector will compute the valid modes an AssemblyLine
 * provides by looking at the operation names.
 * <p>
 * When the target AssemblyLine has its operations invoked through the native
 * connector interface methods (e.g. getnext, selectentries etc) the
 * AssemblyLine connector will provide a work entry with predefined attribute
 * names. These attributes are: <p/> <i>conn</i> - the entry (or entries)
 * passed between this connector and the adapter. The value can be null, a
 * single entry or an array/collection of entries in case there are multiple.
 * <p/> <i>search</i> - the search criteria as specified by the user. See
 * {@link SearchCriteria} <p/> <i>current</i> - the current target object. this
 * is used when modifying existing entries.
 * 
 * Conversely, when the target AssemblyLine returns data for these
 * methods/operations it should return an entry with these attribute names.
 */
public class AssemblyLineConnector extends Connector implements
		ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the properties file
	 */
	private static final String PROPERTIES_FILE = "assemblylineconnector";

	/**
	 * Name of the component.
	 */
	private static final String myName = "AssemblyLine Connector";

	/**
	 * Assembly line function component.
	 */
	private AssemblyLineFC fc;
	/**
	 * ResourceHash used for access of the TMS messages
	 */
	private static ResourceHash sResHash = null;

	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - get next Entry.
	 */
	private static final String OP_GET = "getNextEntry";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - putEntry.
	 */
	private static final String OP_ADD = "putEntry";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method -modEntry.
	 */
	private static final String OP_MODIFY = "modEntry";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - deleteEntry.
	 */
	private static final String OP_DELETE = "deleteEntry";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - findEntry.
	 */
	private static final String OP_FIND = "findEntry";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - selectEntries.
	 */
	private static final String OP_SELECT = "selectEntries";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - queryReply.
	 */
	private static final String OP_CALLREPLY = "queryReply";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - initialize.
	 */
	private static final String OP_INITIALIZE = "initialize";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - terminate.
	 */
	private static final String OP_TERMINATE = "terminate";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - querySchema.
	 */
	private static final String OP_QUERYSCHEMA = "querySchema";
	/**
	 * This constant names the target operation that corresponds to internal
	 * connector method - Default.
	 */
	private static final String OP_DEFAULT = "Default";

	/**
	 * This array defines which target operations must be defined to support the
	 * specific standard mode.
	 */
	private static final String[] REQUIRED_ITERATOR_MODE = { OP_GET };
	/**
	 * This array defines which target operations must be defined to support the
	 * iterator mode.
	 */
	private static final String[] ITERATOR_MODE = { OP_GET, OP_SELECT };
	/**
	 * This array defines which target operations must be defined to support the
	 * add only mode.
	 */
	private static final String[] ADDONLY_MODE = { OP_ADD };
	/**
	 * This array defines which target operations must be defined to support the
	 * lookup mode.
	 */
	private static final String[] LOOKUP_MODE = { OP_FIND };
	/**
	 * This array defines which target operations must be defined to support the
	 * update mode.
	 */
	private static final String[] UPDATE_MODE = { OP_FIND, OP_MODIFY, OP_ADD };
	/**
	 * This array defines which target operations must be defined to support the
	 * delete mode.
	 */
	private static final String[] DELETE_MODE = { OP_DELETE };
	/**
	 * This array defines which target operations must be defined to support the
	 * call reply mode.
	 */
	private static final String[] CALL_REPLY_MODE = { OP_CALLREPLY };

	/**
	 * This is attribute provided in the work entry when the target AssemblyLine
	 * operates as a connector (e.g. connector interface methods/operations
	 * defined).
	 */
	private static final String ADAPTER_CONN = "conn";
	/**
	 * This is attribute provided in the work entry when the target AssemblyLine
	 * operates as a connector (e.g. connector interface methods/operations
	 * defined).
	 */
	private static final String ADAPTER_CURRENT = "current";
	/**
	 * This is attribute provided in the work entry when the target AssemblyLine
	 * operates as a connector (e.g. connector interface methods/operations
	 * defined).
	 */
	private static final String ADAPTER_SEARCH = "search";

	/**
	 * nativeModes holds a map of booleans that are used at runtime to decide
	 * how to deal with a specific mode call.
	 */
	private HashMap<String, Boolean> nativeModes = new HashMap<String, Boolean>();

	/**
	 * These vector contains the public real operations implemented by the
	 * target AssemblyLine.
	 */
	private Vector<String> operations;
	/**
	 * These two vectors contain the computed operations implemented by the
	 * target AssemblyLine.
	 */
	private Vector<String> computedModes;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * The default constructor for this connector. This call will set the base
	 * connector modes.
	 */
	public AssemblyLineConnector() {
		Trace.entrymid(this, "AssemblyLineConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE, });
		Trace.exitmid(this, "AssemblyLineConnector");

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);

		// Create FC and pass it our own configuration
		fc = new AssemblyLineFC();
		fc.setConfiguration(getRawConnectorConfiguration());
		fc.setLog(getLog());
		fc.setRSInterface(getRSInterface());

		// configure modes and operation invocation strategy
		computedModes = getModes((ConnectorConfig) getConfiguration());

		// Initialize FC providing default operation unless we are in adapter
		// mode
		String mode = ((ConnectorConfig) getConfiguration()).getMode();
		if (mode != null && mode.trim().length() > 0) {
			if (!isNativeMode(mode))
				getRawConnectorConfiguration().setStringParameter(
						AssemblyLineFC.OPERATION, mode);
			else
				getRawConnectorConfiguration().setStringParameter(
						AssemblyLineFC.OPERATION, "*");
			fc.initialize(null);
		}

		if (isNativeMode(OP_INITIALIZE))
			performOperation(null, OP_INITIALIZE);

		Trace.entrymin(this, "initialize");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		Trace.entrymin(this, "terminate");
		if (fc != null) {
			try {
				if (isNativeMode(OP_TERMINATE))
					performOperation(null, OP_TERMINATE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				fc.terminate();
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
		fc = null;
		Trace.exitmin(this, "terminate");
	}

	/**
	 * This method prepares the connector if the configured target AL have an
	 * operation called "selectEntries" then that operation is invoked. If no
	 * such operation exists then the underlying {@link AssemblyLineFC}
	 * initialization will take place.
	 * 
	 * @throws Exception
	 *             if error occurs while calling the "selectEntries" operation
	 *             of the target AL.
	 */
	@Override
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		if (isNativeMode(ConnectorConfig.ITERATOR_MODE))
			performOperation(null, OP_SELECT);
//	This causes initialize to happen twice, which may be a problem in some cases.
//	Especially when you debug the server you get two debug sessions for this one component.
// 	If selectEntries should reset the connector it should call terminate/initialize.
//		else
//			fc.initialize(null);
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "getNextEntry"
	 * operation defined then that operation is invoked, otherwise the target AL
	 * is just executed with no IWE.
	 * 
	 * @return the result entry of the AL's execution.
	 * @throws Exception
	 *             if the connector have been terminated.
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		Trace.entrymin(this, "getNextEntry");
		if (fc == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.ASSEMBLYLINE.NOTHING.EXCEP"));
		} else {
			Trace.exitmin(this, "getNextEntry");
			if (!isNativeMode(ConnectorConfig.ITERATOR_MODE))
				return (Entry) fc.perform(null);

			return unwrapAdapterEntry(performOperation(null, OP_GET));
		}
	}

	/**
	 * Unwraps the ADAPTER_CONN attribute returned by the target AL.
	 * 
	 * @param e
	 *            The entry returned by the adapter AL
	 * @return The first entry defined by the attribute (or null if e is null or
	 *         empty)
	 * @throws Exception
	 */
	private Entry unwrapAdapterEntry(Entry e) throws Exception {
		if (e == null)
			return null;

		Attribute conn = e.getAttribute(ADAPTER_CONN);
		if (conn == null)
			return e;

		clearFindEntries();

		for (int i = 0; i < conn.size(); i++) {
			Object value = conn.getValue(i);
			if (value instanceof Entry)
				addFindEntry((Entry) value);
			else
				throw new Exception(sResHash.getString(
						"CONNECTOR.ASSEMBLYLINE.UNEXPECTED.VALUE.CLASS", value
								.getClass().getName()));
		}

		return getFirstFindEntry();
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "queryReply"
	 * operation defined then that operation is invoked, otherwise the target AL
	 * is just executed.
	 * 
	 * @param entry
	 *            the {@link Entry} object passed as the work entry to the
	 *            target AL.
	 * 
	 * @return the result entry of the AL's execution.
	 * @throws Exception
	 *             if an error occurs while executing the target AL.
	 */
	@Override
	public Entry queryReply(Entry entry) throws Exception {
		if (isNativeMode(ConnectorConfig.CALL_REPLY_MODE))
			return (Entry) unwrapAdapterEntry(performOperation(entry,
					OP_CALLREPLY));
		else
			return (Entry) fc.perform(entry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		Trace.entrymax(this, "querySchema");
		if (fc == null) {
			fc = new AssemblyLineFC();
			fc.setConfiguration(getRawConnectorConfiguration());
		}
		try {
			fc.updateSchemaConnector((ConnectorConfig) getConfiguration());
		} catch (Exception err) {
			err.printStackTrace();
			throw err;
		}
		Trace.exitmax(this, "querySchema");
		return new Vector<Object>();
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This method checks the defined operations of the target AL and returns
	 * them as a Vector of names. If the connector is in debug mode then
	 * additional messages are printed to the log.
	 * 
	 * @return a Vector containing the operations names defined by the target
	 *         AL.
	 * @param config
	 *            the configuration object containing parameters used to connect
	 *            to a target AL. If this is <code>null</code> then the
	 *            default modes are returned.
	 */
	public Vector<String> getModes(ConnectorConfig config) {
		getALModes(config);
		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.ASSEMBLYLINE.COMPUTED.MODES",
					getName()));
			for (int i = 0; i < computedModes.size(); i++)
				logmsg(" -- " + computedModes.get(i));

			logmsg(sResHash.getString("CONNECTOR.ASSEMBLYLINE.AL.OPERATIONS",
					config.getConnectionConfig().getParameter(
							AssemblyLineFC.ASSEMBLYLINE)));
			for (int i = 0; i < operations.size(); i++)
				logmsg(" -- " + operations.get(i));

			logmsg(sResHash.getString("CONNECTOR.ASSEMBLYLINE.STANDARD.MODES"));
			for (Iterator<String> i = nativeModes.keySet().iterator(); i
					.hasNext();) {
				Object key = i.next();
				logmsg(" -- " + key + " = " + nativeModes.get(key));
			}
		}
		return computedModes;
	}

	/**
	 * This method checks the defined operations of the target AL and returns
	 * them as a Vector of names.
	 * 
	 * @return a Vector containing the operations names defined by the target
	 *         AL.
	 * @param config
	 *            the configuration object containing parameters used to connect
	 *            to a target AL. If this is <code>null</code> then the
	 *            default modes are returned.
	 */
	public Vector<String> getALModes(ConnectorConfig config) {
		operations = new Vector<String>();
		computedModes = new Vector<String>();

		// This is backward compatibility mode - e.g. no target operations just
		// iterate off the AssemblyLine.
		computedModes.add(ConnectorConfig.ITERATOR_MODE);
		if (config == null)
			return computedModes;

		// If target AssemblyLine has operations defined then we expose those
		// as modes. The standard modes are computed from the operations defined
		// by the target al (e.g. selectentries+getnext = Iterator mode).
		try {
			ContainerConfig ops = getALOperations(config.getConnectionConfig());
			if (ops == null || ops.size() == 0)
				return computedModes;

			for (int i = 0; i < ops.size(); i++) {
				String op = ops.getConfig(i).getShortName();
				if (!op.equals(OP_DEFAULT))
					operations.add(op);
			}

			if (operations.size() == 0)
				return computedModes;

			computedModes = computeModes(operations);
		} catch (Exception e) {
			logError(e.toString());
		}

		return computedModes;
	}

	/**
	 * This method computes the standard modes from the list of available
	 * operations given in the input param. The connector internal method names
	 * are the basis for computing standard modes. The connector internal names
	 * are removed from the list and replaced by standard mode names and a
	 * corresponding flag is set to indicate that this connector should invoke
	 * those operations instead of the operation defined by the mode.
	 * 
	 * @param input
	 *            List of supported operations
	 * @return List of computed modes exposed as available operations for the
	 *         user
	 */
	@SuppressWarnings("unchecked")
	private Vector<String> computeModes(Vector<String> input) {
		Vector<String> newlist = (Vector<String>) input.clone();

		checkMode(REQUIRED_ITERATOR_MODE, ConnectorConfig.ITERATOR_MODE, input);
		checkMode(ADDONLY_MODE, ConnectorConfig.ADDONLY_MODE, input);
		checkMode(LOOKUP_MODE, ConnectorConfig.LOOKUP_MODE, input);
		checkMode(UPDATE_MODE, ConnectorConfig.UPDATE_MODE, input);
		checkMode(DELETE_MODE, ConnectorConfig.DELETE_MODE, input);
		checkMode(CALL_REPLY_MODE, ConnectorConfig.CALL_REPLY_MODE, input);

		// Remove "method" names from operations
		if (isNativeMode(ConnectorConfig.ITERATOR_MODE))
			newlist.removeAll(Arrays.asList(ITERATOR_MODE));

		if (isNativeMode(ConnectorConfig.ADDONLY_MODE))
			newlist.removeAll(Arrays.asList(ADDONLY_MODE));

		if (isNativeMode(ConnectorConfig.LOOKUP_MODE))
			newlist.removeAll(Arrays.asList(LOOKUP_MODE));

		if (isNativeMode(ConnectorConfig.UPDATE_MODE))
			newlist.removeAll(Arrays.asList(UPDATE_MODE));

		if (isNativeMode(ConnectorConfig.DELETE_MODE))
			newlist.removeAll(Arrays.asList(DELETE_MODE));

		if (isNativeMode(ConnectorConfig.CALL_REPLY_MODE))
			newlist.removeAll(Arrays.asList(CALL_REPLY_MODE));

		if (input.contains(OP_INITIALIZE))
			newlist.remove(OP_INITIALIZE);

		if (input.contains(OP_TERMINATE))
			newlist.remove(OP_TERMINATE);

		if (input.contains(OP_QUERYSCHEMA))
			newlist.remove(OP_QUERYSCHEMA);

		// Add the standard modes to the list of supported modes
		for (Iterator<String> i = nativeModes.keySet().iterator(); i.hasNext();) {
			String str = i.next();
			if (isNativeMode(str))
				newlist.add(str);
		}

		// For simplicity we treat initialize and terminate as "standard" modes
		checkMode(new String[] { OP_INITIALIZE }, OP_INITIALIZE, input);
		checkMode(new String[] { OP_TERMINATE }, OP_TERMINATE, input);

		return newlist;
	}

	/**
	 * Adds a check result(the available operations for a set of names) and the
	 * provided mode to the nativeModes map.
	 * 
	 * @param methods
	 *            The names to check
	 * @param mode
	 *            name of the mode
	 * @param input
	 *            The list of operations checked against
	 */
	private void checkMode(String[] methods, String mode, Vector<String> input) {
		nativeModes.put(mode, Boolean.valueOf(standardModeSupported(methods,
				input)));
	}

	/**
	 * Checks the available operations for a set of names.
	 * 
	 * @param methods
	 *            The names to check
	 * @param operations
	 *            The list of operations checked against
	 * @return true if all methods are supported
	 */
	private boolean standardModeSupported(String[] methods,
			Vector<String> operations) {
		for (int i = 0; i < methods.length; i++) {
			if (!operations.contains(methods[i]))
				return false;
		}
		return true;
	}

	/**
	 * Returns the ContainerConfig with the defined operations for the target
	 * AL.
	 * 
	 * @param cc
	 *            {@link BaseConfiguration}.
	 * 
	 * @return Operations container from the {@link AssemblyLineConfig}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private ContainerConfig getALOperations(BaseConfiguration cc)
			throws Exception {
		AssemblyLineConfig alc;
		String server = cc.getStringParameter(AssemblyLineFC.SERVER);
		String conf = cc.getStringParameter(AssemblyLineFC.CONFIG);
		String al = cc.getStringParameter(AssemblyLineFC.ASSEMBLYLINE);

		if (al == null || al.trim().length() == 0)
			return null;

		if (server != null && server.trim().length() > 0) {
			if (fc == null) {
				// Create FC and pass it our own configuration
				fc = new AssemblyLineFC();
				fc.setConfiguration(getRawConnectorConfiguration());
			}
			ConfigInstance ci = fc.getConfigInstance(conf);
			if (ci == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.ASSEMBLYLINE.CANNOT.FIND.CONFIG.INSTANCE",
						new Object[] { server, conf }));
			}

			alc = ci.getConfiguration().getAssemblyLine(al);
		} else {
			MetamergeConfig local = cc.getMetamergeConfig();
			if (conf != null && conf.length() > 0 && RS.getServer(conf) != null)
				local = RS.getServer(conf).getMetamergeConfig();
			alc = local.getAssemblyLine(al);
		}

		return alc.getOperations();
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "deleteEntry"
	 * operation defined then that operation is invoked, otherwise the target AL
	 * is just executed.
	 * 
	 * @param entry
	 *            the {@link Entry} object passed as the work entry to the
	 *            target AL.
	 * @param search
	 *            the {@link SearchCriteria} object passed to the target AL as
	 *            an attribute of the op-entry.
	 * 
	 * @throws Exception
	 *             if an error occurs while executing the target AL.
	 */
	@Override
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws Exception {
		Entry e = new Entry();
		e.setAttribute(ADAPTER_SEARCH, search);
		if (isNativeMode(ConnectorConfig.DELETE_MODE))
			performOperation(entry, OP_DELETE, e);
		else
			performOperation(entry, ConnectorConfig.DELETE_MODE, e);
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "findEntry"
	 * operation defined then that operation is invoked, otherwise unsupported
	 * operation exception is thrown.
	 * 
	 * @param search
	 *            the {@link SearchCriteria} object passed to the target AL as
	 *            an attribute of the op-entry.
	 * @return the result {@link Entry} object produced by the target AL.
	 * @throws Exception
	 *             if an error occurs while executing the target AL or the
	 *             target AL does not have an operation with that name.
	 */
	@Override
	public Entry findEntry(SearchCriteria search) throws Exception {
		Entry e = new Entry();
		e.setAttribute(ADAPTER_SEARCH, search);
		if (isNativeMode(ConnectorConfig.UPDATE_MODE)
				|| isNativeMode(ConnectorConfig.DELETE_MODE)
				|| isNativeMode(ConnectorConfig.LOOKUP_MODE)) {
			return unwrapAdapterEntry(performOperation(new Entry(), OP_FIND, e));
		}
		return super.findEntry(search);
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "modEntry"
	 * operation defined then that operation is invoked, otherwise unsupported
	 * operation exception is thrown.
	 * 
	 * @param entry
	 *            the work entry object of the target AL
	 * @param search
	 *            the {@link SearchCriteria} object passed to the target AL as
	 *            an attribute of the op-entry.
	 * @param old
	 *            the {@link Entry} object passed to the target AL as an
	 *            attribute of the op-entry.
	 * @throws Exception
	 *             if an error occurs while executing the target AL or the
	 *             target AL does not have an operation with that name.
	 */
	@Override
	public void modEntry(Entry entry, SearchCriteria search, Entry old)
			throws Exception {
		if (isNativeMode(ConnectorConfig.UPDATE_MODE)) {
			Entry e = new Entry();
			e.setAttribute(ADAPTER_SEARCH, search);
			if (old != null)
				e.setAttribute(ADAPTER_CURRENT, old);
			performOperation(entry, OP_MODIFY, e);
		} else {
			super.modEntry(entry, search, old);
		}
	}

	/**
	 * Checks if the provided mode is native.
	 * 
	 * @param mode
	 *            the mode to check.
	 * @return <code>true</code> if native , <code>false</code> otherwise.
	 */
	private boolean isNativeMode(String mode) {
		Boolean b = nativeModes.get(mode);
		return (b == null ? false : b.booleanValue());
	}

	/**
	 * 
	 * This method runs the target AL. If the target AL have the "putEntry"
	 * operation defined then that operation is invoked, otherwise unsupported
	 * operation exception is thrown.
	 * 
	 * @param entry
	 *            the work entry object of the target AL
	 * 
	 * @throws Exception
	 *             if an error occurs while executing the target AL or the
	 *             target AL does not have an operation with that name.
	 */
	public void putEntry(Entry entry) throws Exception {
		if (isNativeMode(ConnectorConfig.ADDONLY_MODE)
				|| isNativeMode(ConnectorConfig.UPDATE_MODE)) {
			performOperation(entry, OP_ADD);
		} else {
			super.putEntry(entry);
		}
	}

	/**
	 * This method invokes the named operation in the target AssemblyLine.
	 * 
	 * @param workEntry
	 *            The work entry provided to the AssemblyLine
	 * @param operation
	 *            The operation to invoke
	 * @return Result from operation
	 * @throws Exception if an error occurs.
	 */
	public Entry performOperation(Entry workEntry, String operation)
			throws Exception {
		return performOperation(workEntry, operation, null);
	}

	/**
	 * This method invokes the named operation in the target AssemblyLine.
	 * 
	 * @param workEntry
	 *            The work entry provided to the AssemblyLine
	 * @param operation
	 *            The operation to invoke
	 * @param opentry
	 *            the op-entry passed to the target AL.
	 * @return the result {@link Entry} object from the executed operation
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry performOperation(Entry workEntry, String operation,
			Entry opentry) throws Exception {
		if (debugMode()) {
			String workEntryStr = (workEntry != null) ? workEntry
					.toDeltaString() : "NULL";

			logmsg(sResHash.getString(
					"CONNECTOR.ASSEMBLYLINE.PERFORM.OPERATION", new Object[] {
							operation, workEntryStr }));
		}
		Object obj = fc.performOperation(workEntry, operation, opentry);
		if (debugMode()) {
			logmsg(sResHash.getString(
					"CONNECTOR.ASSEMBLYLINE.OPERATION.RESULT", new Object[] {
							operation, obj }));
		}

		return (Entry) obj;
	}
}
