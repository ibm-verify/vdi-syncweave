/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.log.LogUtils;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AssemblyLine.AssemblyLineListener;
import com.ibm.icu.util.StringTokenizer;

public class Sequence extends Monitor implements TaskInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the properties file from which this component will read its
	 * localized message strings.
	 */
	public final static String PROPERTIES_FILE = "miserver";

	/** Our config instance */
	private RS parent;

	/** Accumulated statistics for all AssemblyLines */
	private TaskStatistics stats = new TaskStatistics();

	/** Work Entry */
	private Entry work = new Entry();

	/** Our config */
	private SequenceConfig config;

	/** Log object */
	private Log log;

	/** Script Engine */
	private ScriptEngine se;

	/**
	 * True if we have inherited the ScriptEngine.
	 */
	private boolean seIsInherited;

	/** TaskCallBlock */
	private TaskCallBlock tcb;

	/** Termination requested? */
	private volatile boolean terminationRequested;

	/** HashCode Implementation */
	private static int seqCounter;

	/**
	 * Unique identifier for the Sequence object.
	 */
	private final int seqHashCode;

	/** Message Resource Hash */
	private static ResourceHash res = ResourceHash.getHash(PROPERTIES_FILE);

	/** Currently running AL */
	private AssemblyLine activeAL;

	/** if we are running a Sequence instead of an AL */
	private Sequence activeSeq;

	private ThreadSafeListenableImpl<AssemblyLineListener> eventSource = new ThreadSafeListenableImpl<AssemblyLineListener>();

	public Sequence(RS rs, SequenceConfig sc, Object params) {
		parent = rs;
		config = sc;

		synchronized (Sequence.class) {
			seqHashCode = ++seqCounter;
		}

		// Parse parameter object(s)
		if (params instanceof Vector<?>) {
			for (Object o : (Vector<?>) params) {
				parseParameter(o);
			}
		} else if (params != null) {
			parseParameter(params);
		}

		if (tcb == null)
			tcb = new TaskCallBlock();

		setName(config.getShortName());
		if (log == null) {
			log = new Log("miserver", getLogCategory());
		} else {
			// Redefine log so we have our own.
			log = new Log(log);
		}
		tcb.setTask(this);

		// Debug mode
		log.setDebug(config.getDebug());
		// Add Specific logging for this instance

		try {
			LogUtils.addLoggers("Sequence", config.getShortName(), log, config.getLogConfig(), parent);
		} catch (Exception e) {
			log.error(e.toString());
		}

		log.info("Sequence.Started.by", currentThread().getName());
		
		threadStarted(this, null);

	}

	/**
	 * Interpret object passed as parameter to this Sequence
	 * 
	 * @param p
	 *            {@link TaskCallBlock} , {@link Entry} or
	 *            {@link Log} object.
	 */
	private void parseParameter(Object p) {
		if (p instanceof TaskCallBlock) {
			tcb = (TaskCallBlock) p;
		} else if (p instanceof Entry) {
			work = (Entry) p;
		} else if (p instanceof Log) {
			log = (Log) p;
		} else if (p instanceof ScriptEngine) {
			se = (ScriptEngine) p;
			seIsInherited = true;
		} else if (p instanceof AssemblyLineListener) {
			addListener((AssemblyLineListener) p);
		}
	}

	private String getLogCategory() {
		if (Boolean.getBoolean("com.ibm.di.logging.addALcounter"))
			return "Sequence." + getName() + "." + hashCode();
		else
			return "Sequence." + getName();
	}

	/**
	 * @return the unique identifier for this Sequence object. All the Sequence objects in
	 *         the JVM they run have a different number.
	 */
	public int hashCode() {
		return seqHashCode;
	}

	@Override
	public void run() {

		try {
			initScriptEngine();
			for (int i = 0; i< config.size() && ! terminationRequested; i++) {
				BaseConfiguration item = config.getConfig(i);
				if (!item.getEnabled())
					continue;
				if (item instanceof ScriptConfig) {
					runScript(item);
					continue;
				}				
				try {
					runAL(item, tcb.clone());
				} catch (Exception e) {
					log.logerror(e.getLocalizedMessage(), e);
					stats.exception(e);
					terminationRequested = true;
				}
			}
			closedown();
		} catch (Exception e) {
			log.logerror(e.getLocalizedMessage(), e);
			stats.exception(e);
		} finally {
			// Good bye!
			threadStopped(this, null, stats.getError());
			notifyTerminated();
		}
	}

	private void runScript(BaseConfiguration item) throws Exception {
		String script = item.getScript();
		se.declareBean("work", work);
		if (script != null && script.length() > 0)
			se.interpret(script);
	}

	private void runAL(BaseConfiguration bc, TaskCallBlock tcb) throws Exception {

		String alName = getParam(bc, "assemblyLine");
		if (alName == null)
			throw new Exception(res.getString("Sequence.no.AssemblyLine"));
		boolean isSequence = alName.contains("/" + MetamergeConfig.DEFAULT_SEQUENCE_FOLDER + "/");

		// Set the operation
		String operation = bc.getStringParameter("operation");
		if (operation != null && operation.length() > 0)
			tcb.setALOperation(operation);

		// Get the init params off the raw configuration
		for (String key: bc.getKeys(BaseConfiguration.ONE_LEVEL)) {
			if (key.startsWith(AssemblyLineFC.OPERATION_INIT_PREFIX)) {
				String param = key.substring(AssemblyLineFC.OPERATION_INIT_PREFIX.length());
				tcb.setOperationInitParam(param, bc.getParameter(key));
			}
		}

		boolean inheritWork = bc.getBooleanParameter("inheritWork", false);
		if (inheritWork && work != null) 
			tcb.setInitialWorkEntry(work);

		String server = getServerName(bc);
		String configID = getParam(bc, "config");

		boolean runInBackground = bc.getBooleanParameter("runInBackground", false);
		TaskStatistics result;

		if (server != null) {
			ConfigInstance configInstance = getConfigInstance(server, configID);
			com.ibm.di.api.remote.AssemblyLine al = configInstance.startAssemblyLine(alName, tcb, !runInBackground);
			declareAL(bc, al);
			if (runInBackground)
				return;
			work = al.getResult();
			result = al.getStatistics();
		} else {
			RS tmpRS = configID != null ? RS.getServer(configID) : parent;
			if (tmpRS == null)
				throw new Exception(res.getString("Scheduler.cannot.find.configinstance", configID));

			Vector<Object> params = new Vector<Object>();
			params.add(tcb);
			if (bc.getBooleanParameter("shareScript", false))
				params.add(se);
			if (bc.getBooleanParameter("shareLog", false))
				params.add(log);

			if (isSequence) {
				Sequence s = tmpRS.startSequence(alName, params);
				declareAL(bc, s);
				if (runInBackground)
					return;
				activeSeq = s;
				s.join();
				activeSeq = null;
				work = s.getResult();
				result = s.getStats();
			} else {
				AssemblyLine al = tmpRS.startAL(alName, params);
				declareAL(bc, al);
				if (runInBackground)
					return;
				activeAL = al;
				al.join();
				activeAL = null;
				work = al.getResult();
				result = al.getStats();
			}
		}

		stats.addStats(result);

		if (bc.getBooleanParameter("stopOnFailure", false) && result.getError() != null) {
			stats.exception(result.getError());
			terminationRequested = true;
		}
	}

	private void declareAL(BaseConfiguration b, Object al) {
		String s = b.getShortName();
		if (s == null || s.trim().length() == 0)
			s = b.getStringParameter("assemblyLine");
		if (s == null || s.trim().length() == 0)
			return;
		try {
			se.declareStaticBean(s.trim(), al);
		} catch (Exception ignore) {
			// Can't happen
			SystemFunctions.doNothing();
		}
	}

	private String getParam(BaseConfiguration bc, String name) {
		String s = bc.getStringParameter(name);
		if ( s == null )
			return null;
		s = s.trim();
		if (s.equals(""))
			return null;
		return s;
	}

	private String getServerName(BaseConfiguration bc) {
		String s = getParam(bc, "server");
		if (s == null || s.equalsIgnoreCase("local"))
			return null;
		if (s.indexOf(":") == -1)
			s += ":1099";

		return s;
	}

	private ConfigInstance getConfigInstance(String server, String configID) throws Exception {
		SessionFactory sf = (SessionFactory) Naming.lookup("rmi://"
				+ server + "/SessionFactory");
		Session session = sf.createSession();

		ConfigInstance configInstance = session.getConfigInstance(configID);
		if (configInstance == null)
			configInstance = session.startConfigInstance(configID);
		return configInstance;
	}

	private void closedown() {
		// Log statistics
		log.info("total", stats.getMsg());

		// Report status
		Exception eperror = stats.getError();

		if (eperror != null) {
			String reason = eperror.getMessage() != null ? eperror.getMessage() : eperror.toString();
			log.error("failed.because", reason);
			if (!(eperror instanceof AbortALException) && se.lastException(eperror) != null) {
				String errorMessage = res.getString("msterminate.error.caused", se.lastException(eperror).toString());
				log.logerror(errorMessage);
			}
		} else {
			log.info("terminated.successfully", Integer.valueOf(stats.err));
		}

		if (se != null && !seIsInherited) {
			se.terminate();
		}

	}

	private void initScriptEngine() throws Exception {
		// Initialize script engine

		if (se == null) {
			se = new ScriptEngine(null, getParent(), tcb.hasProperty(AssemblyLine.TCB_DEBUG_JAVASCRIPT));
		}
		se.declareStaticBean("task", this);
		se.declareStaticBean("main", parent);
		se.declareStaticBean("status", stats);

		// Declare Java Class libraries
		se.declareUserFunctions();

		// Include global scripts
		autoIncludeScripts();
	}

	public TaskStatistics getStats() {
		return stats;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	public void debugBreak(Object arg0) throws Exception {
		// No debugging for now
	}

	public void debugMsg(Object arg0) throws Exception {
		// No debugging for now
	}

	public Object getConfig(String name) {
		return config.getParameter(name);
	}

	public AssemblyLineComponent getConnector(String arg0) {
		return null;
	}

	public Entry getCurrentWork() {
		return work;
	}

	public Log getLog() {
		return log;
	}

	public String getNullBehavior() {
		return parent.getNullBehavior();
	}

	public String getNullBehaviorValue() {
		return parent.getNullBehaviorValue();
	}

	public String getNullDefinition() {
		return parent.getNullDefinition();
	}

	public String getNullDefinitionValue() {
		return parent.getNullDefinitionValue();
	}

	public RS getParent() {
		return parent;
	}

	public Entry getResult() {
		return work;
	}

	public ScriptEngine getScriptEngine() {
		return se;
	}

	public Entry getWork() {
		return work;
	}

	public void logmsg(Object msg) {
		log.loginfo("" + msg);		
	}

    /**
     * Prints the contents of an entry to the Log.
     * 
     * @param entry
     *            The entry object to print
     */
    public void dumpEntry(Entry entry) {
            log.dumpEntry(entry);
    }

	/**
	 * Request controlled shutdown of this {@link Sequence}
	 */
	public void shutdown() {
		terminationRequested = true;

		if (activeAL != null)
			activeAL.shutdown();

		if (activeSeq != null)
			activeSeq.shutdown();
	}

	/**
	 * Request controlled shutdown of this {@link Sequence},
	 * and waits for it to stop.
	 * @param sync If true, creates a new Thread to do the waiting.
	 * @throws Exception If the current Thread tries to stop itself.
	 */
	public void shutdown(boolean sync) throws AbortALException {
		terminationRequested = true;

		if (activeAL != null)
			activeAL.shutdown(sync);

		if (activeSeq != null)
			activeSeq.shutdown(sync);
	}

	/**
	 * Includes all scripts tagged as auto-include in the current script engine.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void autoIncludeScripts() throws Exception {

		List<String> includePrologs = new ArrayList<String>();
		Set<ScriptConfig> excludePrologs = new HashSet<ScriptConfig>();

		String aic = config.getStringParameter("includePrologs");
		if (aic != null) {
			StringTokenizer st = new StringTokenizer(aic, "\r\n");
			while (st.hasMoreTokens()) {
				String p = st.nextToken();
				if (p.startsWith("-")) {
					try {
						String ref = p.substring(1);
						ref = ref.replaceAll(":", ":/Scripts/");
						ScriptConfig sc = (ScriptConfig) this.config.getMetamergeConfig().lookup(ref);
						if (sc != null)
							excludePrologs.add(sc);
					} catch (Exception e) {
						SystemFunctions.doNothing();
					}
				} else {
					includePrologs.add(p);
				}
			}
		}

		aic = config.getStringParameter("includeGlobalPrologs");
		if (aic == null || aic.equals("true")) {
			se.includeAllScripts(config.getMetamergeConfig(), excludePrologs);
		} else {
			log.debug("include.from.script.library.disabled.by.configuration");
		}

		for (String p : includePrologs) {
			se.loadScript(parent, "prolog", p, true);
		}
	}
	/**
	 *  Register a listener for events
	 * 
	 * @param listener
	 *            A listener.
	 */
	private void addListener(AssemblyLineListener listener) {
		eventSource.addListener(listener);
	}

	/**
	 * Notifies listeners that the Sequence has terminated
	 */
	private void notifyTerminated() {
		eventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<AssemblyLineListener>() {
			public void visit(AssemblyLineListener listener) {
				listener.assemblyLineTerminated(null);
			}
		});
	}
	

}
