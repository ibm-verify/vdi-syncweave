/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.PropertyConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AssemblyLine.AssemblyLineListener;

/**
 * This class implements a pool of AssemblyLines. The purpose of this pool is to
 * make using AssemblyLines more efficient by re-using AssemblyLine objects.
 * This pool is especially useful when Server mode Connectors are used in an
 * AssemblyLine. By configuring a pool of AssemblyLines it is possible to speed
 * up the processing of requests directed at a Server mode Connector, because
 * each request can be serviced by a different AssesmblyLine from the pool.
 * Moreover when a request has been processed by an AssemblyLine object that
 * object does not have to terminate (thus wasting system resources on
 * AssemblyLine termination) but is returned to the pool to be used for
 * servicing other requests.
 */
public final class AssemblyLinePool {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final int TM_NEWAL_THREAD = 1;

	public static final int TM_NEWAL_EVENT = 2;

	public static final int TM_REUSE_AL = 3;

	private static final String PROPERTIES_FILE = "miserver";

	// Excluded connector types
	private Vector<String> excludedConnectorTypes;

	private PropertyConfig threadOptions;

	private AssemblyLineConfig alConfig;

	private String assemblyLine;

	private Log log;

	private RSInterface parent;

	private int prepare;

	private int maxinst;

	private int threadmode;

	private boolean executeProlog = false;

	private boolean terminateRequested = false;

	private boolean propertiesToAttributes = false;

	/**
	 * Lock that guards the debugger fields: {@link #debugPort},
	 * {@link #debugOnError}, {@link #debugHost}. When you want both this lock
	 * and the lock on {@link #activePool}, obtain first this lock to avoid
	 * deadlocks (the order is not so important but must always be the same).
	 */
	private Object debugLock = new Object();

	/**
	 * TCP port on which the remote debugger listens.
	 * 
	 * @see #debugLock
	 */
	private int debugPort = -1;

	/**
	 * If true breakpoints are disabled except when there is an error.
	 * 
	 * @see #debugLock
	 */
	private boolean debugOnError;

	/**
	 * Host of the remote debugger.
	 * 
	 * @see #debugLock
	 */
	private String debugHost;

	private Vector<ALWorker> workerThreads = new Vector<ALWorker>();

	private Vector<AssemblyLine> freePool = new Vector<AssemblyLine>();

	private Map<String, AssemblyLine> activePool = new Hashtable<String, AssemblyLine>();

	private boolean simulateMode = false;

	/**
	 * This map keeps track of the desired debug mode of the AssemblyLine
	 * components (Connectors and Function Components). There is one entry for
	 * each component in the AssemblyLine configuration. Applies to all
	 * AssemblyLines in the pool. May be accessed by different threads. When you
	 * want both the lock on this map and the lock on {@link #activePool},
	 * obtain first this lock to avoid deadlocks (the order is not so important
	 * but must always be the same).
	 */
	private Map<String, Boolean> componentDebugModes = new Hashtable<String, Boolean>();

	// Message Resource Hash
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * connectorPool contains a list of PooledConnectorSet classes that are used
	 * when creating new AL instances.
	 */
	private Vector<PooledConnectorSet> connectorPool = new Vector<PooledConnectorSet>();

	/**
	 * This specifies how long a PooledConnectorSet can stay in the
	 * connectorPool before it is closed/removed.
	 */
	private long connectorPoolTimeout = Long.getLong(
			"com.ibm.di.server.connectorpooltimeout", 42).longValue();

	private Timer timer;
	
	
	//	 Added for defect 11861.
	private  long counter =0;
	private Object counterLock = new Object();
	
	public AssemblyLinePool(String assemblyLine, Log log, RSInterface parent,
			AssemblyLineConfig alConfig) throws Exception {
		this(assemblyLine, log, parent, alConfig, -1);
	}

	public AssemblyLinePool(String assemblyLine, Log log, RSInterface parent,
			AssemblyLineConfig alConfig, int debugPort) throws Exception {
		this(assemblyLine, log, parent, alConfig, "localhost", debugPort,
				false, null);
	}

	public AssemblyLinePool(String assemblyLine, Log log, RSInterface parent,
			AssemblyLineConfig alConfig, String debugHost, int debugPort,
			boolean debugOnError, Map<String, Boolean> componentDebugModes)
			throws Exception {
		this.assemblyLine = assemblyLine;
		this.log = log;
		this.parent = parent;
		this.alConfig = alConfig;
		this.threadOptions = alConfig.getThreadOptions();
		this.debugPort = debugPort;
		this.debugOnError = debugOnError;
		this.debugHost = debugHost;
		if (componentDebugModes != null) {
			this.componentDebugModes.putAll(componentDebugModes);
		}
		initialize();
	}

	private void initialize() throws Exception {
		Trace.entrymin(this, "initialize");
		String exclude = System
				.getProperty("com.ibm.di.server.connectorpoolexclude");
		if (exclude == null) {
			exclude = "com.ibm.di.connector.FileConnector,com.ibm.di.connector.ScriptConnector";
			log.debug("alpool.property.undefined", new Object[] {
					"com.ibm.di.server.connectorpoolexclude", exclude });
		}
		excludedConnectorTypes = com.ibm.di.util.StringUtils.splitstring(
				exclude, " ,");
		executeProlog = threadOptions.getBooleanParameter(
				InternalSchema.AL_EXECUTE_PROLOG, executeProlog);
		propertiesToAttributes = threadOptions.getBooleanParameter(
				InternalSchema.AL_EH_PROPS2ATTRS, propertiesToAttributes);
		prepare = threadOptions.getIntegerParameter(
				"assemblyline.ehc.minPrepare", 0);
		maxinst = threadOptions.getIntegerParameter(
				"assemblyline.ehc.maxInstance", 0);
		threadmode = threadOptions.getIntegerParameter(
				"assemblyline.ehc.options", 1);

		if (threadmode < TM_NEWAL_THREAD || threadmode > TM_REUSE_AL) {
			String errorMessage = sResHash.getString(
					"alpool.invalid.thread.mode", new Object[] {
							Integer.toString(threadmode),
							Integer.toString(TM_NEWAL_THREAD),
							Integer.toString(TM_REUSE_AL) });
			throw new Exception(errorMessage);
		}

		if (maxinst == 0)
			maxinst = 5;

		// If debug mode then we permit only one instance
		synchronized (debugLock) {
			if (debugPort != -1) {
				if (maxinst > 1) {
					log.warn(""); // Add a blank line?
					log.warn(sResHash.getString("debugging.reduced.size"));
				}
				maxinst = 1;
				prepare = 1;
			}
		}

		if (maxinst < prepare) {
			log.warn("alpool.max.lt.prepare", new Object[] {
					Integer.toString(maxinst), Integer.toString(prepare) });
			maxinst = prepare;
		}

		log.debug("alpool.init.info", new Object[] { Integer.toString(prepare),
				Integer.toString(maxinst) });

		simulateMode = alConfig.getBooleanParameter(
				InternalSchema.AL_SIMULATE_MODE, false);

		// Load assemblyline instances
		for (int i = 0; i < prepare; i++) {
			freePool.add(prepareAssemblyLine());
		}

		// Schedule connector pool cleaner
		log.debug("alpool.conn.pool.timeout", Long
				.toString(connectorPoolTimeout));
		timer = new Timer();
		if (connectorPoolTimeout > 0) {
			if (connectorPoolTimeout > 120)
				timer.scheduleAtFixedRate(new PoolTimer(),
						connectorPoolTimeout * 1000, 60000);
			else
				timer.scheduleAtFixedRate(new PoolTimer(),
						connectorPoolTimeout * 1000,
						(connectorPoolTimeout * 1000) / 10);
		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * Starts a new thread with the provided Iterator. This method is typically
	 * called from the AssemblyLine containing a Server mode connector. The
	 * server mode connector returns a connector interface that is used to
	 * handle a "client" request of some kind. The thread started will insert
	 * the connector as the sole iterator and drive the AssemblyLine instance
	 * until the connector returns EOF.
	 */
	public ALWorker startThread(String name, ConnectorInterface iterator)
			throws Exception {
		Trace.entrymid(this, "startThread", name, iterator);
		ALWorker worker = new ALWorker(name, iterator, this);
		synchronized (workerThreads) {
			workerThreads.add(worker);
			worker.start();
		}
		Trace.exitmid(this, "startThread", worker);
		return worker;
	}

	/**
	 * Starts a new thread with the provided connector config. This method is
	 * typically called when a server mode connector is being started in its own
	 * thread.
	 */
	public ALWorker startThread(String name, ConnectorConfig config)
			throws Exception {
		Trace.entrymid(this, "startThread", name, config);
		ALWorker worker = new ALWorker(name, config, this);
		synchronized (workerThreads) {
			workerThreads.add(worker);
			worker.start();
		}
		Trace.exitmid(this, "startThread", worker);
		return worker;
	}

	/**
	 * Starts a new thread with the provided work entry. This method is
	 * typically called to execute an AL instance with a single work entry.
	 */
	public ALWorker startThread(String name, Entry workEntry) throws Exception {
		Trace.entrymid(this, "startThread", name, workEntry);
		ALWorker worker = new ALWorker(name, workEntry, this);
		synchronized (workerThreads) {
			workerThreads.add(worker);
			worker.start();
		}
		Trace.exitmid(this, "startThread", worker);
		return worker;
	}

	/**
	 * Called by ALWorker thread to signal it is no longer executing.
	 */
	private void workerStopped(ALWorker worker) {
		synchronized (workerThreads) {
			workerThreads.remove(worker);
		}
	}

	/**
	 * Called by a thread to release its reserved AssemblyLine instance.
	 * 
	 * @param owner
	 *            The Thread object that previously called executeEvent)
	 */
	public void releaseAssemblyLine(Thread owner) {
		releaseAssemblyLine(owner.getName() + "." + owner.hashCode());
	}

	private void releaseAssemblyLine(String owner) {

		AssemblyLine al = null;

		// Remove it from the activePool list
		synchronized (activePool) {
			al = activePool.remove(owner);
		}

		if (al != null)
			addFreePool(al);

		synchronized (activePool) {
			activePool.notifyAll();
		}
	}

	private void addFreePool(AssemblyLine al) {

		if (threadmode == TM_REUSE_AL && al.getStats().getError() == null
				&& al.getCurrentStep() < ALState.MS_EPILOG) {

			// Only return the AL to the pool if it did not fail and we are
			// below the prepare watermark
			synchronized (freePool) {
				if (freePool.size() < prepare) {

					// make AL's simulation level to be the same as the pool's.
					al.setSimulating(simulateMode);

					freePool.add(al);
					return;
				}
			}

		}

		// Not reusing AL - terminate it
		try {
			// We may reuse connectors
			PooledConnectorSet reuse = saveRuntimeConnectors(al);
			al.executeTerminateAL();
			
			// Reuse the configuration object if it is clean
			AssemblyLineConfig alc = al.getInternalConfig();
			if (!alc.getModified()) {
				reuse.setAssemblyLineConfig(alc);
				synchronized (connectorPool) {
					connectorPool.add(reuse);
				}
			} else if (reuse.size() > 0 ){
				reuse.release();
			}

			al = null;
		} catch (Throwable err) {
			//leave it empty. no user action is required.
		}

		// Create new AL instance in freePool
		synchronized (freePool) {
			if (freePool.size() < prepare) {
				try {
					freePool.add(prepareAssemblyLine());
				} catch (Exception ignore) {
					//leave it empty. no user action is required.
				}
			}
		}

	}

	/**
	 * Returns the connector pool timeout in milliseconds.
	 */
	public long getConnectorPoolTimeout() {
		return connectorPoolTimeout;
	}

	/**
	 * Sets the connector pool timeout in milliseconds.
	 */
	public void setConnectorPoolTimeout(long connectorPoolTimeout) {
		this.connectorPoolTimeout = connectorPoolTimeout;
	}

	/**
	 * Returns true if the connector type is excluded from pooling.
	 */
	public boolean runtimeConnectorTypeExcluded(ConnectorInterface connector) {
		return excludedConnectorTypes.contains(connector.getClass().getName());
	}

	/**
	 * Extract connector interfaces that we will reuse in a subsequent AL
	 * instance.
	 */
	private PooledConnectorSet saveRuntimeConnectors(AssemblyLine al) {
		Trace.entrymax(this, "saveRuntimeConnectors", al);
		PooledConnectorSet reuse = new PooledConnectorSet();
		if (connectorPoolTimeout < 0) {
			log.debug("alpool.conn.pool.disabled");
			return reuse;
		}
		if (al.getStats() == null || al.getStats().getError() != null)
			return reuse;

		log.debug("alpool.save.runtime.conn", al.toString());
		for (AssemblyLineComponent alc: al.getConnectors()) {
			if (!alc.componentInitialized() || alc.isFailOvered())
				continue;

			if (alc instanceof FunctionComponent) {
				FunctionComponent fc = (FunctionComponent) alc;
				if (fc.function == null) {
					log.debug("alpool.null.func.not.pool");
					continue;
				}
				log.debug("alpool.add.to.pool", new Object[] { fc.getName(),
						fc.function.getClass().getName() });
				reuse.addFunction(fc.getName(), fc.function);
				fc.function = null;
				continue;
			}

			if (alc.connector == null) {
				log.debug("alpool.null.conn.not.pool");
				continue;
			}

			if (runtimeConnectorTypeExcluded(alc.connector)) {
				log.debug("alpool.conn.type.exclude", alc.connector.getClass().getName());
				continue;
			}

			switch (alc.getType()) {
			case ServerConstants.TYPE_LOOP:
			case ServerConstants.TYPE_UPDATE:
			case ServerConstants.TYPE_LOOKUP:
			case ServerConstants.TYPE_DELETE:
			case ServerConstants.TYPE_ADDONLY:
			case ServerConstants.TYPE_CALLREPLY:
			case ServerConstants.TYPE_DELTA:
				log.debug("alpool.add.to.pool", new Object[] { alc.getName(),
						alc.connector.getClass().getName() });
				reuse.addConnector(alc.getName(), alc.connector);
				alc.connector = null;
				break;
			}
		}

		Trace.exitmax(this, "saveRuntimeConnectors");
		return reuse;
	}

	/**
	 * Returns a vector of runtime connectors to use in a new al instance.
	 */
	private PooledConnectorSet getRuntimeConnectors() {
		synchronized (connectorPool) {
			if (connectorPool.size() > 0) {
				log.debug("alpool.reuse.pooled.connset");
				return connectorPool.remove(0);
			}
		}
		return null;
	}

	/**
	 * Closes and removes runtime connectors that have timed out.
	 */
	private void garbageCollectPool() {
		if (connectorPoolTimeout <= 0) {
			log.debug("alpool.garbage.pool.timeout.disabled");
			return;
		}
		synchronized (connectorPool) {
			log.debug("alpool.connpool.size", Integer.toString(connectorPool
					.size()));
			for (int i = (connectorPool.size() - 1); i >= 0; i--) {
				PooledConnectorSet pcs = connectorPool.get(i);
				if (pcs.hasTimedOut()) {
					log.debug("alpool.remove.pooled.connset");
					pcs.release();
				}
			}
		}
	}

	private AssemblyLine getAssemblyLine(String owner) throws Exception {
		Trace.entrymax(this, "getAssemblyLine", owner);

		AssemblyLine al = activePool.get(owner);
		if (al != null) {
			Trace.exitmax(this, "getAssemblyLine", al);
			return al;
		}

		while (al == null) {
			// Get a free AL
			synchronized (freePool) {
				if (freePool.size() > 0)
					al = freePool.remove(0);
			}

			synchronized (activePool) {
				// If no free ALs then try to create one
				if (al == null) {
					if (activePool.size() < maxinst) {
						al = prepareAssemblyLine();
						activePool.put(owner, al);
					} else {
						// If activePool is full we have to wait for someone to release first
						log.debug("alpool.get.al.wait.release");
						activePool.wait();
					}
				} else {
					activePool.put(owner, al);
				}
			}
		}
		Trace.exitmax(this, "getAssemblyLine", al);
		return al;
	}

	private AssemblyLine prepareAssemblyLine() throws Exception {
		Trace.entrymax(this, "prepareAssemblyLine");
		log.debug("alpool.prepare.al", assemblyLine);
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setRunMode("manual");
		tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, Boolean
				.valueOf(simulateMode));
		if (debugPort != -1)
			tcb.setProperty(AssemblyLine.TCB_DEBUG_JAVASCRIPT, Boolean.TRUE);

		PooledConnectorSet pcs = getRuntimeConnectors();
		if (pcs != null) {
			for (int i = 0; i < pcs.size(); i++) {
				PooledConnector pc = pcs.get(i);
				log.debug("alpool.add.pool.runtime.conn", pc.name);
				if (pc.conn != null)
					tcb.setRuntimeConnector(pc.name, pc.conn);
				else if (pc.func != null)
					tcb.setRuntimeFunction(pc.name, pc.func);
			}
		}

		if (log != parent.getLog())
			tcb.setAddLogAppenders(false);

		// reuse a configuration object if possible
		AssemblyLineConfig alc = (pcs != null ? pcs.getAssemblyLineConfig() : null);
		if (alc == null && alConfig != null)
			alc = (AssemblyLineConfig) alConfig.getClone();
		AssemblyLine al = new AssemblyLine(parent, assemblyLine, tcb, log, alc);
		if (parent instanceof AssemblyLineListener)
			al.addListener((AssemblyLineListener) parent);
		al.setIgnoreInputComponents(true);
		synchronized (debugLock) {
			if (debugPort != -1) {
				al.enableDebug(debugPort, debugHost, debugOnError);
			} else {
				al.autoActivateDebugger();
			}
		}
		al.executeInitializeAL();
		al.initScriptEngine();

		declareEHbeans(al.getScriptEngine());
		al.start(); // dummy call to avoid memory leak (bug in JVM?)
		if (executeProlog) {
			al.initExecuteProlog();
		} else {
			al.notifyStarted();
		}
		Trace.exitmax(this, "prepareAssemblyLine", al);
		return al;
	}

	/**
	 * Returns true if there are active eventhandlers using this ALPool as
	 * interceptor.
	 */
	public boolean hasRunningThreads() {
		if (activePool.size() > 0) {
			return true;
		}
		int count = 0;
		synchronized (workerThreads) {
			for (ALWorker s : workerThreads) {
				log.debug("alpool.alworker", new Object[] { s.getName(),
						Boolean.toString(s.isAlive()) });
				if (s.isAlive())
					count++;
			}
		}
		return (count > 0);
	}

	/**
	 * Declare beans pointing to all AssemblyLine Pool instances.
	 */
	private void declareEHbeans(ScriptEngine se) {
		synchronized (workerThreads) {
			for (Thread t : workerThreads) {
				try {
					se.declareStaticBean(t.getName(), t);
				} catch (Exception e) {
					log.debug("ALPOOL.FAILED.TO.DECLARE.BEAN");
				}
			}
		}
	}

	/**
	 * Called by a thread to execute a cycle in the AssemblyLine
	 * 
	 * @param source
	 *            The caller's thread object
	 * @param event
	 *            The entry passed as the initial work entry
	 */
	public Entry executeEvent(Thread source, Entry event) throws Exception {
		return executeEvent(source, event, true);
	}

	public Entry executeEvent(Thread source, Entry event, boolean processTCB)
			throws Exception {

		if (terminateRequested) {
			String errorMessage = sResHash
					.getString("alpool.termination.pending");
			throw new Exception(errorMessage);
		}

		log.debug("alpool.execute.event", new Object[] {
				Integer.toString(source.hashCode()), event.toString(),
				Integer.toString(threadmode) });

		AssemblyLine al = getAssemblyLine(source.getName() + "."
				+ source.hashCode());
		Entry result = null;
		try {
			result = al.executeCycle(event, processTCB);
			if (threadmode == TM_NEWAL_EVENT)
				releaseAssemblyLine(source);
		} catch (Throwable error) {
			error.printStackTrace();
			al.bailout(error);
			releaseAssemblyLine(source);
			throw new Exception(error);
		}
		return result;
	}

	/**
	 * Called to block future calls from EventHandlers from executing. When an
	 * EH calls this ALPool after this method has completed, an exception is
	 * thrown to end the eventhandler.
	 */
	public void terminate() {
		terminateRequested = true;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		
		for(AssemblyLine al : getActiveAssemblyLines()) {
			try {
				al.shutdown(true);
			} catch(Exception err) {
				log.warn("While terminating: " + err);
			}
		}
		
	}

	public RSInterface getParent() {
		return parent;
	}

	/**
	 * This is the worker thread that drives a client request.
	 */
	public final class ALWorker extends Thread {
		private ConnectorInterface conn;

		private ConnectorConfig config;

		private Entry workEntry;

		private String name;

		private AssemblyLinePool parent;
		
		private ScriptEngine se;

		// Method added for Defect # 11861
		private String getALWorkerID(){
			synchronized(counterLock){
				return assemblyLine + "." + name + "." + Long.toString(counter++);
			}
		}

		public ALWorker(String name, Entry workEntry, AssemblyLinePool pool) {
			this.name = name;
			this.workEntry = workEntry;
			this.parent = pool;
			setName(getALWorkerID()); // Defect # 11861
		}

		public ALWorker(String name, ConnectorInterface conn, AssemblyLinePool pool) {
			this.name = name;
			this.conn = conn;
			this.parent = pool;
			setName(getALWorkerID()); // Defect # 11861
		}

		public ALWorker(String name, ConnectorConfig config, AssemblyLinePool pool) {
			this.name = name;
			this.config = config;
			this.parent = pool;
			setName(getALWorkerID()); //Defect # 11861
		}

		public void run() {
			Trace.entrymid(this, "run");
			log.info("alworker.thread.started", getName());
			AssemblyLine al = null;
			try {
				al = getAssemblyLine(getName());
				se = al.getScriptEngine();
				if (conn != null) {
					log.info("alworker.add.runtime.conn", conn.getName());
					al.addRuntimeIterator(name, conn, executeProlog);
					synchronized (componentDebugModes) {
						al.setComponentDebugModes(componentDebugModes);
					}
					// Execute until just before we close connectors (we'll
					// reuse some of them)
					al.executeMainLoop(ALState.MS_EPILOG);
				} else if (config != null) {
					log.info("alworker.add.runtime.conn.config", config
							.getName());
					al.addRuntimeConnector(name, config, executeProlog);
					synchronized (componentDebugModes) {
						al.setComponentDebugModes(componentDebugModes);
					}
					al.executeMainLoop();
				} else if (workEntry != null) {
					log.info("alworker.execute.cycle", workEntry.toString());
					synchronized (componentDebugModes) {
						al.setComponentDebugModes(componentDebugModes);
					}
					al.executeCycle(workEntry);
				}

			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				log.info("alworker.thread.stopped", getName());
				releaseAssemblyLine(getName());
				workerStopped(this);
			}
			Trace.exitmid(this, "run");
		}

		public AssemblyLinePool getParent() {
			return this.parent;
		}
		
		public ScriptEngine getScriptEngine() {
			return se;
		}
	}

	private final class PooledConnectorSet {
		private long timestamp;

		private Vector<PooledConnector> pool = new Vector<PooledConnector>();

		private AssemblyLineConfig alc;
		
		public PooledConnectorSet() {
			this.timestamp = System.currentTimeMillis();
		}

		public void addConnector(String name, ConnectorInterface conn) {
			pool.add(new PooledConnector(name, conn));
		}

		public void addFunction(String name, FunctionInterface conn) {
			pool.add(new PooledConnector(name, conn));
		}

		public int size() {
			return pool.size();
		}

		public PooledConnector get(int index) {
			return pool.get(index);
		}

		public boolean hasTimedOut() {
			return pool.size() > 0 && ((System.currentTimeMillis() - timestamp) > connectorPoolTimeout);
		}

		public void release() {
			for (int i = 0; i < size(); i++) {
				try {
					PooledConnector pc = get(i);
					if (pc.conn != null) {
						log.debug("alpool.conn.pool.conn.close", new Object[] {
								pc.name, pc.conn.getClass().getName() });
						pc.conn.terminate();
					} else if (pc.func != null) {
						log.debug("alpool.conn.pool.func.close", new Object[] {
								pc.name, pc.func.getClass().getName() });
						pc.func.terminate();
					}
				} catch (Throwable ignore) {
				}
			}
			pool.removeAllElements();
		}

		public AssemblyLineConfig getAssemblyLineConfig() {
			return alc;
		}

		public void setAssemblyLineConfig(AssemblyLineConfig alc) {
			this.alc = alc;
		}
	}

	private final static class PooledConnector {
		private String name;

		private ConnectorInterface conn;

		private FunctionInterface func;

		public PooledConnector(String name, ConnectorInterface conn) {
			this.name = name;
			this.conn = conn;
		}

		public PooledConnector(String name, FunctionInterface func) {
			this.name = name;
			this.func = func;
		}
	}

	private final class PoolTimer extends TimerTask {
		public void run() {
			log.debug("alpool.pool.timer.garbage.pool");
			garbageCollectPool();
		}
	}

	/**
	 * Query the debug mode of the specified component (Connector or Function
	 * Component). May be called by different threads.
	 * 
	 * @param componentName
	 *            The name of the component as it appears in the configuration
	 *            of the AssemblyLine.
	 * @return The debug mode of the component.
	 * @throws Exception
	 *             If the component name is invalid.
	 */
	public boolean getComponentDebugMode(String componentName) throws Exception {
		synchronized (componentDebugModes) {
			if (componentName == null
					|| !componentDebugModes.containsKey(componentName)) {
				throw new Exception(sResHash.getString(
						"AL.POOL.GET.COMPONENT.DEBUG.INVALID.COMPONENT.NAME",
						new Object[] { componentName, assemblyLine }));
			}
			return componentDebugModes.get(componentName);
		}
	}

	/**
	 * Modify the debug mode of the specified component. May be called by
	 * different threads.
	 * 
	 * @param componentName
	 *            The name of the component.
	 * @param debug
	 *            The new debug mode setting.
	 * @throws Exception
	 *             If the component name is invalid.
	 */
	public void setComponentDebugMode(String componentName, boolean debug)
			throws Exception {
		synchronized (componentDebugModes) {
			if (componentName == null
					|| !componentDebugModes.containsKey(componentName)) {
				throw new Exception(sResHash.getString(
						"AL.POOL.SET.COMPONENT.DEBUG.INVALID.COMPONENT.NAME",
						new Object[] { componentName, assemblyLine }));
			}
			componentDebugModes.put(componentName, debug);
			synchronized (activePool) {
				// apply the setting to all running AssemblyLines
				for (AssemblyLine al : activePool.values()) {
					al.setComponentDebugMode(componentName, debug);
				}
			}
		}
	}

	/**
	 * Configure a debugger. May be called by other threads.
	 * 
	 * @param port
	 *            The TCP port number of the remote debugger client
	 * 
	 * @param host
	 *            The host name of the remote debugger client
	 * @param onerror
	 *            If true breakpoints are disabled except when there is an
	 *            error.
	 * @throws Exception
	 *             If there is already a debugger, if the debugger cannot be
	 *             initialized or if the Pool size is more than one.
	 */
	public void enableDebug(int port, String host, boolean onerror)
			throws Exception {
		synchronized (debugLock) {
			synchronized (activePool) {
				// apply the setting to all running AssemblyLines
				for (AssemblyLine al : activePool.values()) {
					al.enableDebug(port, host, onerror, true);
				}
			}
			debugPort = port;
			debugHost = host;
			debugOnError = onerror;
		}
	}

	/**
	 * Remove the configured debugger. May be called by other threads.
	 * 
	 * @param msg
	 *            Message to be sent.
	 */
	public void disableDebug(Object msg) {
		synchronized (debugLock) {
			synchronized (activePool) {
				// apply the setting to all running AssemblyLines
				for (AssemblyLine al : activePool.values()) {
					al.disableDebug(msg, true);
				}
			}
			debugPort = -1;
		}
	}
	
	public List<AssemblyLine> getActiveAssemblyLines() {
		List<AssemblyLine> list = new ArrayList<AssemblyLine>();
		synchronized(activePool) {
			list.addAll(activePool.values());
		}
		return list;
	}
}
