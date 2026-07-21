/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.ALEvent;
import com.ibm.di.api.DIEvent;
import com.ibm.di.config.base.FunctionConfigImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.exceptions.ContinueLoopException;
import com.ibm.di.exceptions.ExitBranchException;
import com.ibm.di.exceptions.IgnoreEntryException;
import com.ibm.di.exceptions.NonFatalException;
import com.ibm.di.exceptions.RestartEntryException;
import com.ibm.di.exceptions.RetryEntryException;
import com.ibm.di.exceptions.ReturnException;
import com.ibm.di.exceptions.SkipEntryException;
import com.ibm.di.exceptions.SkipToException;
import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.log.LogUtils;
import com.ibm.di.performance.PerfEntry;
import com.ibm.di.performance.PerformanceStats;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.util.AssemblyLineScripts;
import com.ibm.di.util.DebugServer;
import com.ibm.di.util.HookTree;
import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSNull;

/**
 * This class represents a IBM Tivoli Directory Integrator AssemblyLine.
 *
 * When an AssemblyLine needs to be started the com.ibm.di.server.RS object
 * creates an instance of the AssemblyLine class and then calls the start()
 * method on this instance. Since AssemblyLine inherits (indirectly) from
 * java.lang.Thread this causes the run() method of the AssemblyLine to be
 * called.
 *
 * An alternative method of running AssemblyLines is running them manually. This
 * means that only a single cycle of the AssemblyLine is executed at a time,
 * returning the work Entry result at the end of the cycle. An AssemblyLine can
 * be run in manual mode by using the AssemblyLine constructor which is passed a
 * com.ibm.di.server.TaskCallBlock object. This TaskCallBlock object must have
 * had its AssemblyLine.TCB_RUNMODE_PROPNAME property set to the value of
 * AssemblyLine.RUNMODE_MANUAL. Then a cycle can be executed by invoking the
 * Entry executeCycle(Entry workEntry) AssemblyLine method.
 */

public final class AssemblyLine extends Monitor implements TaskInterface, RecordPlaybackInterface,
Listenable<AssemblyLine.AssemblyLineListener> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This constant is used when passing parameters to the AL using the TCB
	 * object. <br>
	 * This constant have the meaning of starting the AL in normal mode.
	 */
	public final static String RUNMODE_NORMAL = "normal";

	/**
	 * This constant is used when passing parameters to the AL using the TCB
	 * object. <br>
	 * This constant have the meaning of starting the AL in record mode.
	 */
	public final static String RUNMODE_RECORD = "record";

	/**
	 * This constant is used when passing parameters to the AL using the TCB
	 * object. <br>
	 * This constant have the meaning of starting the AL in playback mode.
	 */
	public final static String RUNMODE_PLAYBACK = "playback";

	/**
	 * This constant is used when passing parameters to the AL using the TCB
	 * object. <br>
	 * This constant have the meaning of starting the AL in manual mode.
	 */
	public final static String RUNMODE_MANUAL = "manual";

	/**
	 * This constant is used when autodebug has been configured for the config
	 * instance. To force a non-debug run callers can explicitly override the
	 * autodebug behaviour. <br>
	 *
	 * This constant have the meaning of starting the AL in normal mode.
	 */
	public final static String RUNMODE_NODEBUG = "nodebug";

	/**
	 * This array contains all the run modes as strings.
	 */
	public final static String[] RUNMODES = { RUNMODE_NORMAL, RUNMODE_RECORD, RUNMODE_PLAYBACK, RUNMODE_MANUAL, RUNMODE_NODEBUG };

	/**
	 * This is the position of the {@link #RUNMODE_NORMAL} String in the
	 * {@link #RUNMODES} array
	 */
	public final static int RUNMODE_I_NORMAL = 0;

	/**
	 * This is the position of the {@link #RUNMODE_RECORD} String in the
	 * {@link #RUNMODES} array
	 */
	public final static int RUNMODE_I_RECORD = 1;

	/**
	 * This is the position of the {@link #RUNMODE_PLAYBACK} String in the
	 * {@link #RUNMODES} array
	 */
	public final static int RUNMODE_I_PLAYBACK = 2;

	/**
	 * This is the position of the {@link #RUNMODE_MANUAL} String in the
	 * {@link #RUNMODES} array
	 */
	public final static int RUNMODE_I_MANUAL = 3;

	/**
	 * This is the position of the {@link #RUNMODE_MANUAL} String in the
	 * {@link #RUNMODES} array
	 */
	public final static int RUNMODE_I_NODEBUG = 4;

	/**
	 *
	 * The key parameter passed to the
	 * {@link TaskCallBlock#setProperty(Object, Object)} method.
	 *
	 * @deprecated This kind of usage is deprecated, use the
	 *             {@link TaskCallBlock#setRunMode(String)} method instead.
	 *
	 */
	public final static String TCB_RUNMODE_PROPNAME = "assemblyline.runmode";

	/**
	 * When this flag is set in the {@link TaskCallBlock} object, and then the
	 * TCB is passed to the {@link AssemblyLine}, the AL will not make any
	 * changes to external systems (e.g. Databases, LDAP servers, etc.). This
	 * flag is not used instead of any of the provided run modes. <br>
	 * <br>
	 * <b>Script Example:</b>
	 *
	 * <pre>
	 * // construct the object used for configurating the AL
	 * TaskCallBlock tcb = new TaskCallBlock();
	 *
	 * // set the name of the AL to be started
	 * tcb.setAssemblyLineName(&quot;anALName&quot;);
	 * // set the run mode of the AL
	 * tcb.setRunMode(AssemblyLine.RUNMODE_NORMAL);
	 * // tell the AL to skip the changes in any of the back-end systems used in the AL, just simulate the changes
	 * tcb.setProperty(AsseblyLine.TCB_SIMULATE_MODE, Boolean.valueOf(true));
	 *
	 * // kick off the AL thread
	 * main.startAL(tcb);
	 *
	 * </pre>
	 */
	public final static String TCB_SIMULATE_MODE = "assemblyline.simulate";

	/**
	 * @deprecated CheckPoint/Restart is deprecated
	 */
	public final static String TCB_CP_FORCECLEAN = "assemblyline.resetcheckpoint";

	/**
	 * @deprecated CheckPoint/Restart is deprecated
	 */
	public final static String TCB_CP_CHECKPOINTID = "assemblyline.checkpointid";

	/**
	 * @deprecated CheckPoint/Restart is deprecated
	 */
	public final static String TCB_CP_CHECKPOINTOBJECT = "assemblyline.checkpointobject";

	/**
	 * Discard IWE? Typically used when IWE contains params but should not be
	 * used in the cycle. <br>
	 * <br>
	 * Usage:
	 *
	 * <pre>
	 * tcb.setProperty(TCB_CP_DISCARD_IWE, Boolean.valueOf(true));
	 * </pre>
	 */
	public final static String TCB_CP_DISCARD_IWE = "assemblyline.discardiwe";

	/**
	 * Force replay channel.
	 */
	public final static String TCB_FORCE_REPLYCHANNEL = "assemblyline.forcereplychannel";

	/**
	 * The name of the attribute in the op-entry which value tells the operation
	 * the called AL is running in.
	 */
	public final static String OPENTRY_OPERATION = "$operation";

	/**
	 * The TCP port number of the remote debugger client. Used when establishing
	 * session between this AL and a remote debugger client (e.g. CE).
	 */
	public final static String TCB_DEBUG_PORT = "assemblyline.debugport";

	/**
	 * The host of the remote debugger client. Used when establishing session
	 * between this AL and a remote debugger client (e.g. CE).
	 */
	public final static String TCB_DEBUG_HOST = "assemblyline.debughost";

	/**
	 * Used to set the onerror flag. When true breakpoints are disabled except
	 * when there is an error. Used when establishing session between this AL
	 * and a remote debugger client (e.g. CE).
	 */
	public final static String TCB_DEBUG_ONERROR = "assemblyline.onerror";

	/**
	 * Used to prepare the Javascript engine for debugging. Set this property
	 * (to any value) to enable stepping through Javascript statements when
	 * debugging.
	 */
	public final static String TCB_DEBUG_JAVASCRIPT = "assemblyline.debugjavascript";

	/**
	 * The name of the properties file from which this component will read its
	 * localized message strings.
	 */
	public final static String PROPERTIES_FILE = "miserver";

	/**
	 * <p>
	 * This interface is for internal use only. Users must not rely on it.
	 * </p>
	 *
	 * <p>
	 * A listener for AssemblyLine events. The listener will be invoked on the
	 * AssemblyLine thread, so the listener should synchronize its internals.
	 * </p>
	 *
	 * @since 7.0
	 */
	public interface AssemblyLineListener {

		/**
		 * The AssemblyLine started.
		 *
		 * @param al
		 *            The AssemblyLine.
		 * @since 7.1
		 */
		void assemblyLineStarted(AssemblyLine al);

		/**
		 * An AssemblyLine cycle (iteration) is complete.
		 *
		 * @param al
		 *            The AssemblyLine.
		 * @param work
		 *            The work Entry.
		 * @throws Exception
		 *             Will stop the AssemblyLine, so be careful.
		 */
		void assemblyLineCycleEnded(AssemblyLine al, Entry work) throws Exception;

		/**
		 * The AssemblyLine terminated.
		 *
		 * @param al
		 *            The AssemblyLine.
		 */
		void assemblyLineTerminated(AssemblyLine al);
	}

	/**
	 * A piece of code that will be executed on the AssemblyLine component loop.
	 *
	 * @since 7.0
	 */
	private static abstract class AssemblyLineCommand {

		/**
		 * Generic command type. Use when type does not matter.
		 */
		public static int AL_CMD_GENERIC = 1;

		/**
		 * Add debugger command.
		 */
		public static int AL_CMD_ADD_DEBUGGER = 2;

		/**
		 * The type of the command.
		 */
		private int type;

		/**
		 * Create a generic command.
		 */
		public AssemblyLineCommand() {
			this(AL_CMD_GENERIC);
		}

		/**
		 * Create a command of the specified type.
		 *
		 * @param type
		 *            Command type.
		 */
		public AssemblyLineCommand(int type) {
			this.type = type;
		}

		/**
		 * @return The type of the command.
		 */
		public int getType() {
			return type;
		}

		/**
		 * The logic of the command.
		 *
		 * @throws Exception
		 *             If an error occurs. It may stop the AssemblyLine.
		 */
		public abstract void execute() throws Exception;
	}

	/** RSInterface (our creator) */
	private RSInterface parent;

	/**
	 * Remote debugger. This is the actual debugger, which the AssemblyLine uses
	 * currently. Modify it only on the AssemblyLine thread. May be read by
	 * other threads.
	 */
	private volatile DebugServer debugger;

	/** Accumulated statistics for all connectors */
	private TaskStatistics stats = new TaskStatistics();

	/** Performance Stats Object */
	private PerformanceStats perfSt = new PerformanceStats();

	/** Runtime provided connector */
	private ConnectorInterface conn;

	/** Initial Work Entry */
	private Entry entry;

	/** Operations Entry */
	private Entry opentry = new Entry();

	/** Result Entry */
	private Entry result = null;

	/** Assemblyline configuration */
	private AssemblyLineConfig config;

	/** User defined persistent parameters */
	private Hashtable<String, Object> taskParam = new Hashtable<String, Object>();

	/** Log object */
	private Log log;

	/**
	 * If non-null, the Log object created by this AssemblyLine
	 */
	private Log internalLog = null;

	/**
	 * log4j category name used when requesting a logger
	 */
	private String logCategoryName;

	/** Script Engine */
	private ScriptEngine se;

	/**
	 * True if we have inherited the ScriptEngine.
	 */
	private boolean seIsInherited;

	/** Null behavior */
	private String nullBehavior;

	/**
	 * Value of the null behavior object
	 */
	private String nullBehaviorValue;

	/** Null definition */
	private String nullDefinition;

	/**
	 * Value of the Null definition object
	 */
	private String nullDefinitionValue;

	/** TaskCallBlock */
	private TaskCallBlock tcb;

	/** Terminate flag */
	private volatile boolean terminationRequested = false;

	/** the currently active Server Connector (if there is one) */
	private ConnectorInterface mActiveServerConnector = null;

	/** AssemblyLine state object */
	private ALState state;

	/**
	 * <p>
	 * The list of AssemblyLineComponent objects this AssemblyLine hosts.
	 * AssemblyLineComponent objects can be Connectors, Function Components,
	 * Script Components, Loop Components, Branching Components, etc.
	 * </p>
	 * <p>
	 * May be read by different threads (only the collection is thread-safe, not
	 * the operations to its components). Will be modified only by one thread.
	 * </p>
	 */
	private List<AssemblyLineComponent> connectors = Collections.synchronizedList(new ArrayList<AssemblyLineComponent>());

	/**
	 * List of AssemlyLineComponet objects .These objects must be iterator
	 * connectors.
	 */
	private List<AssemblyLineComponent> stateIterators = new ArrayList<AssemblyLineComponent>();
	/**
	 * List of AssemlyLineComponet objects .These objects must be connectors ,
	 * but not in Iterator mode.
	 */
	private List<AssemblyLineComponent> stateConnectors = new ArrayList<AssemblyLineComponent>();

	/** Work entry */
	private Entry meta = null;

	/** Logging interval & limits */
	private int interval = 0;

	/**
	 * The AssemblyLine will terminate after having this number of errors
	 */
	private int maxerrors = 0;

	/**
	 * The AssemblyLine will terminate after reading this number of entries
	 */
	private int maxread = 0;

	/**
	 * Number of read entries.
	 */
	private int numread = 0;

	/** AssemblyLine run mode */
	private int runMode = RUNMODE_I_NORMAL;

	/**
	 * If <code>true</code> then the Input Components will be ignored, otherwise
	 * they will be used.
	 */
	private boolean ignoreInputComponents = false;

	/**
	 * Holds operation types for components.
	 */
	private final static String[] TYPE_OPER = { "get", "update", "lookup", "delete", "addonly", "callreply", "script",
		"functioncall", "Branch", "reply", "Server", "delta", "loop", "attributemap", "Switch", "Case" };

	/**
	 * A custom user message, used when storing TombStones
	 */
	private String tombstoneUserMessage = "";

	/** The map for parameter substitutions */
	private Map<String, Object> parameterSubstitutionMap = new HashMap<String, Object>();

	/** To enable-disable performance messages */
	private boolean perfEnabled = false;

	/** Message Resource Hash */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/** HashCode Implementation */
	private static int alCounter;

	/**
	 * Unique identifier for the AL object.
	 */
	private final int alHashCode;

	/**
	 * This flag is responsible for AL's simulation
	 */
	private boolean mSimulating = false;

	/**
	 * This is the proxy AL which is used by all components when the AL
	 * simulation is enabled. This reference is guarded by the proxyALLock and
	 * if you need to set/get the value, then use it in synchronized block.
	 */
	private AssemblyLineFC proxyAL = null;

	/**
	 * This is the object protecting the proxyAL instance variable.
	 */
	private Object proxyALLock = new Object();

	private ThreadSafeListenableImpl<AssemblyLineListener> eventSource = new ThreadSafeListenableImpl<AssemblyLineListener>();

	/**
	 * <p>
	 * If the AssemblyLine has a Server mode Connector, it will not execute the
	 * components in the flow by itself, but will instead listen for client
	 * requests and dispatch these requests to an AssemblyLine Pool. The
	 * AssemblyLine Pool keeps a number of identical AssemblyLines that can
	 * handle client requests.
	 * </p>
	 * <p>
	 * May be read by many threads. Will be set only once.
	 * </p>
	 */
	private AssemblyLinePool alPool = null;

	/**
	 * This map keeps track of the desired debug mode of the AssemblyLine
	 * components (Connectors and Function Components). Contains an entry for
	 * each component of the AssemblyLine. It is used as a temporary store of
	 * the settings until the Connectors get loaded. May be accessed by
	 * different threads.
	 */
	private Map<String, Boolean> componentDebugModes = new Hashtable<String, Boolean>();

	/**
	 * A list of commands that need to be executed in the AssemblyLine component
	 * loop. Used to provide thread-safe asynchronous operations. May be
	 * accessed by different threads, so get a lock over the object before you
	 * use it.
	 */
	private List<AssemblyLineCommand> commands = new ArrayList<AssemblyLineCommand>();

	/**
	 * Used as the "thisScriptObject" bean when calling AssemblyLine Hooks
	 */
	private Entry scriptObject = new Entry();

	/**
	 * Remember which AssemblyLine started this AssemblyLine.
	 */
	private WeakReference<AssemblyLine> parentAL = null;

	/**
	 * Remember which Scheduler started this AssemblyLine.
	 */
	private WeakReference<Scheduler> scheduler = null;
	
	/**
	 * Hashcode for parent.
	 */
	private int parentHash;
	
	/**
	 * Synchronization object used to determine whether the AssemblyLine has
	 * shut down.
	 *
	 * @see #waitForShutdown()
	 */
	private CountDownLatch shutdownLatch = new CountDownLatch(1);

	/**
	 * Constructor for the AssemblyLine object that is available in JavaScript
	 * via the pre-registered variable task. <b>Note</b> that if you want to run
	 * an AssemblyLine then use the startAL() method of the com.ibm.di.server.RS
	 * class.
	 */
	public AssemblyLine() {
		alHashCode = createHashCode();
		setParentAL();
	}
	
	static int createHashCode() {
		synchronized (AssemblyLine.class) {
			return ++alCounter;
		}		
	}

	private void setParentAL() {
		Thread t = Thread.currentThread();
		if (t instanceof AssemblyLine)
			parentAL = new WeakReference<AssemblyLine>((AssemblyLine) t);
		else if (t instanceof Scheduler)
			scheduler = new WeakReference<Scheduler>((Scheduler) t);
		parentHash = t.hashCode();
	}

	/**
	 * Constructor for the AssemblyLine object that is available in JavaScript
	 * via the pre-registered variable task. <b>Note</b> that if you want to run
	 * an AssemblyLine then use the startAL() method of the com.ibm.di.server.RS
	 * class.
	 *
	 * @param parent
	 *            RSInterface of caller/creator
	 * @param taskName
	 *            Name of AssemblyLine to run
	 * @param param
	 *            Optional parameter(s)
	 * @param log
	 *            Optional Log object. Will be used as the parent of the logger
	 *            of this AssemblyLine.
	 * @exception Exception
	 *                if taskName is an unknown AssemblyLine or if any of the
	 *                connectors cannot be initialized
	 */
	public AssemblyLine(RSInterface parent, String taskName, Object param, Log log) throws Exception {
		this(parent, taskName, param, log, null);
	}

	/**
	 * Constructor for the AssemblyLine object that is available in JavaScript
	 * via the pre-registered variable task. <b>Note</b> that if you want to run
	 * an AssemblyLine then use the startAL() method of the com.ibm.di.server.RS
	 * class.
	 *
	 * @param parent
	 *            RSInterface of caller/creator
	 * @param taskName
	 *            Name of AssemblyLine to run
	 * @param param
	 *            Optional parameter(s)
	 * @param log
	 *            Optional Log object. Will be used as the parent of the logger
	 *            of this AssemblyLine.
	 * @param alc
	 *            Optional AssemblyLine configuration object. This object will
	 *            be owned by the AssemblyLine - no private copy will be
	 *            created. If null is specified the AssemblyLine will make its
	 *            own copy of the master configuration. Note that copying the
	 *            configuration object is expensive.
	 * @exception Exception
	 *                if taskName is an unknown AssemblyLine or if any of the
	 *                connectors cannot be initialized
	 */
	public AssemblyLine(RSInterface parent, String taskName, Object param, Log log, AssemblyLineConfig alc) throws Exception {

		super( parent instanceof RS ? ((RS)parent).getThreadGroup() : null, taskName);
		
		this.parent = parent;
		this.log = log;

		alHashCode = createHashCode();
		setParentAL();

		// Parse parameter object(s)
		if (param != null) {
			if (param instanceof Vector<?>) {
				for (Object o : (Vector<?>) param) {
					parseALParameter(o);
				}
			} else {
				parseALParameter(param);
			}
		}

		if (alc == null) {
			config = parent.getTask(taskName);
			if (config == null) {
				logException("al.config.notfound", taskName);
			}
			// Clone config object
			config = (AssemblyLineConfig) config.getClone();
		} else {
			config = alc;
		}

		// Give cloned AL config a reference to our param substitution map
		parameterSubstitutionMap.put("task", this);
		parameterSubstitutionMap.put("op-entry", opentry);
		parameterSubstitutionMap.put("mc", config.getMetamergeConfig());
		config.setSubstitutionMap(parameterSubstitutionMap);

		// Force use of TCB?
		if (tcb == null) {
			tcb = new TaskCallBlock(taskName, config, null);
			if (getWork() != null)
				tcb.setInitialWorkEntry(getWork());
		} else {

			// Retrieve runMode from runtime provided TCB
			String str = tcb.getRunMode();
			if (RUNMODE_NORMAL.equalsIgnoreCase(str) || str.equals("0"))
				setRunMode(RUNMODE_I_NORMAL);
			else if (RUNMODE_RECORD.equalsIgnoreCase(str) || str.equals("1"))
				setRunMode(RUNMODE_I_RECORD);
			else if (RUNMODE_PLAYBACK.equalsIgnoreCase(str) || str.equals("2"))
				setRunMode(RUNMODE_I_PLAYBACK);
			else if (RUNMODE_MANUAL.equalsIgnoreCase(str) || str.equals("3"))
				setRunMode(RUNMODE_I_MANUAL);
			else if (RUNMODE_NODEBUG.equalsIgnoreCase(str) || str.equals("4"))
				setRunMode(RUNMODE_I_NODEBUG);
			else {
				String errorMessage = sResHash.getString("bad.runmode.value", str);
				throw new Exception(errorMessage);
			}

			if (tcb.hasProperty(TCB_SIMULATE_MODE)) {
				mSimulating = tcb.getBoolProperty(TCB_SIMULATE_MODE, false);
			}

			// AL Settings
			tcb.applyALSettings(config);
		}

		// AL Operation - set and verify
		int opcount = config.getOperations().size() - (config.getOperation("Default") != null ? 1 : 0);

		String operation = tcb.getALOperation();
		if (operation != null) {
			// startup operation is blank for al adapters (e.g. AL
			// implementing conn interface methods)
			if (!("*".equals(operation))) {
				opentry.setAttribute(OPENTRY_OPERATION, operation);
				if (opcount > 0 && config.getOperation(operation) == null)
					logException("al.invalid.operation", operation);
			}
		} else if (config.getOperation("Default") != null) {
			tcb.setALOperation("Default");
		} else if (opcount > 0) {
			logException("al.invalid.operation", "(null)");
		}

		// Params to the initialize operation are migrated to
		// op-entry.<paramname>
		opentry.merge(tcb.getOperationInitParams());

		// Get anonymous runtime connector
		if (this.conn == null)
			this.conn = tcb.getRuntimeConnector(null);

		initializeTask(tcb.getTaskName() != null ? tcb.getTaskName() : config.getName().toString());

		// we are interested only in modifications done by the user
		config.setModified(false);

		// The thread has not really started yet, but will very soon.
		// Mark the thread as started, to avoid the server accidentally
		// stopping before the thread really has started.
		if (runMode != RUNMODE_I_MANUAL)
			threadStarted(this, null);
		
		if(getParent() != null)
			this.log.info("AssemblyLine.started.by", getParent().getName());
		else
			this.log.info("AssemblyLine.started.by", currentThread().getName());
	}

	/**
	 * Gets the proxy AL which is used by all components when the AL simulation
	 * is enabled
	 *
	 * @return The {@link AssemblyLineFC} instance object used to start the
	 *         configured Proxy AL, or null if the {@link #initializeProxyAL()}
	 *         has not been called yet.
	 * @throws Exception
	 *             if an initialization error occurs.
	 * @since 7.0
	 */
	AssemblyLineFC getProxyAL() throws Exception {
		checkProxyALInitialized();
		return proxyAL;
	}

	/**
	 * Sets an operation parameter that will be passed as an Attribute to the
	 * called Proxy AL in its "op-entry" Entry object on the next execution of
	 * the Proxy AL. <br>
	 * Passing a <code>null</code> as the <code>paramValue</code> parameter will
	 * remove the Attribute named with the value of <code>paramName</code> <br>
	 * <b>Note:</b> There are some reserved attribute names as $operation,
	 * $method, search, etc. that are set using that method. They are set just
	 * before the Proxy AL is called by the connector, so they might replace any
	 * existing, user defined, Attributes.
	 *
	 * @param paramName
	 *            the name of the attribute, if this is null the call will be
	 *            ignored
	 * @param paramValue
	 *            the attribute value, if this is null the attribute found by
	 *            the <code>paramName</code> will be removed
	 * @throws Exception
	 *             if an error occurs.
	 * @since 7.0
	 */
	public void setProxyALOperationParam(String paramName, Object paramValue) throws Exception {
		checkProxyALInitialized();

		proxyAL.getTCB().setOperationInitParam(paramName, paramValue);
	}

	/**
	 * This method transfers the Attributes from the passed entry object to the
	 * op-entry object of the Proxy AL. <br>
	 * <br>
	 * <b>Note:</b> There are some reserved attribute names as $operation,
	 * $method, search, etc. that are set using that method. They are set just
	 * before the Proxy AL is called by the connector, so they might replace any
	 * existing, user defined, Attributes.
	 *
	 * @param opentry
	 *            the entry which Attribute will be merged
	 * @throws Exception
	 *             if an error occurs.
	 * @since 7.0
	 */
	public void setProxyALOperationParams(Entry opentry) throws Exception {
		if (opentry == null)
			return;
		checkProxyALInitialized();

		proxyAL.getTCB().getOperationInitParams().merge(opentry);

	}

	/**
	 * Creates a new {@link AssemblyLineFC} object that is responsible for
	 * calling the Proxy AL configured using this AL's {@link SimulationConfig}
	 * object.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 * @since 7.0
	 */
	private void checkProxyALInitialized() throws Exception {

		synchronized (proxyALLock) {

			if (proxyAL == null) {
				// use the AssemblyLineFC to start the a remote proxy AL
				proxyAL = new AssemblyLineFC();

				FunctionConfig fc = new FunctionConfigImpl();
				fc.init();

				SimulationConfig sc = getSimulationConfig();
				// set the needed parameters
				fc.setParameter(AssemblyLineFC.ASSEMBLYLINE, sc.getProxyALName());

				fc.setIntegerParameter(AssemblyLineFC.CYCLE_MODE, sc.getProxyALMode());
				fc.setDebug(sc.getProxyALDebug());

				fc.setParameter(AssemblyLineFC.SERVER, sc.getProxyALServer() == null ? "" : sc.getProxyALServer());

				fc.setParameter(AssemblyLineFC.CONFIG, sc.getProxyALConfigInstance());

				proxyAL.setConfiguration(fc);
				proxyAL.initialize(null);
			}
		}
	}

	/**
	 * @return the unique identifier for this AL object. All the AL objects in
	 *         the JVM they run have a different number.
	 */
	public int hashCode() {
		return alHashCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof AssemblyLine)) {
			return false;
		} else {
			return super.equals(o);
		}
	}

	/**
	 * log an exception creating the log object if it is null
	 *
	 * @param res
	 *            resource for the exception
	 * @param param
	 *            parameter that identifies the exception.
	 * @throws Exception
	 *             the logged exception is rethrown.
	 */
	private void logException(String res, Object param) throws Exception {
		if (log == null)
			log = new Log("miserver", getLogCategory());
		log.exception(res, param);
	}

	/**
	 * Interpret object passed as parameter to this AL
	 *
	 * @param p
	 *            {@link TaskCallBlock} , {@link Entry} ,
	 *            {@link ConnectorInterface} , {@link Log} or String(AL runmode)
	 *            object.
	 * @throws Exception
	 *             if the provided parameter is not recognized.
	 */
	private void parseALParameter(Object p) throws Exception {
		if (p instanceof TaskCallBlock) {
			tcb = (TaskCallBlock) p;
		} else if (p instanceof Entry) {
			setWork((Entry) p);
		} else if (p instanceof ConnectorInterface) {
			this.conn = (ConnectorInterface) p;
		} else if (p instanceof Log) {
			this.log = (Log) p;
		} else if (p instanceof String) {
			for (int i = 0; i < RUNMODES.length; i++) {
				if (RUNMODES[i].equalsIgnoreCase((String) p)) {
					this.runMode = i;
					return;
				}
			}

			throw new Exception(sResHash.getString("unable.interpret.parameter", p));
		} else if (p instanceof AssemblyLineListener) {
			addListener((AssemblyLineListener) p);
		} else if (p instanceof ScriptEngine) {
			se = (ScriptEngine) p;
			seIsInherited = true;
		} else {
			if (parent != null) {
				parent.logmsg(sResHash.getString("unknown.object.parameter", p.getClass().getName()));
			}
		}
	}

	/**
	 * This method sets the initial work entry object. On the first/next cycle
	 * of the AssemblyLine, this object will present itself as the "work" object
	 * in the AssemblyLine. Any Iterators will be ignored for that cycle.If you
	 * wish to remove the Initial Work Entry and restore normal operation of
	 * Iterators for that cycle, use
	 *
	 * <pre>
	 * task.setWork(null)
	 * </pre>
	 *
	 * .
	 *
	 * @param entry
	 *            The initial work entry
	 */
	public void setWork(Entry entry) {
		this.entry = entry;
		this.result = entry;
		if (entry != null)
			parameterSubstitutionMap.put("work", entry);
		else
			parameterSubstitutionMap.put("work", new Entry());
	}

	/**
	 * setParam sets the value for the user-defined parameter. The user-defined
	 * parameter list is persisted between runs in the file specified in the
	 * AssemblyLine settings panel. This file is a simple text file with a
	 * "keyword:value" pair on each line.
	 *
	 * @param name
	 *            The user-defined parameter name
	 * @param value
	 *            The value associated with name
	 */
	public void setParam(String name, Object value) {
		if (name == null)
			return;
		name = name.toLowerCase(Locale.ENGLISH);
		if (value != null)
			taskParam.put(name, value);
		else
			taskParam.remove(name);
	}

	/**
	 * setConfig sets a named parameter value in the AssemblyLine's
	 * configuration. The AssemblyLine configuration is <i>not</i> persisted
	 * between runs.
	 *
	 * @param name
	 *            The AssemblyLine configuration parameter name
	 * @param value
	 *            The value associated with name
	 */
	public void setConfig(String name, Object value) {
		config.setParameter(name, value);
	}

	/**
	 * This method returns the RSInterface (main) object.
	 *
	 * @return The main thread
	 */
	public RSInterface getParent() {
		return parent;
	}

	/**
	 * This method returns the result Entry object. This object is the working
	 * Entry left from the last cycle of the AssemblyLine.
	 *
	 * @return The last "work" Entry object
	 */
	public Entry getResult() {
		return result;
	}

	/**
	 * This method returns the initial work Entry object. If there are no
	 * Iterators in the AssemblyLine then this object should be set with the
	 * setWork method.
	 *
	 * @return The initial work Entry object
	 */
	public Entry getWork() {
		return entry;
	}

	/**
	 * This method returns the current work Entry object.
	 *
	 * @return The current work Entry object
	 */
	public Entry getCurrentWork() {
		if (state == null)
			return entry;
		if (state.mainStep <= ALState.MS_PROLOG)
			return entry;
		if (state.mainStep >= ALState.MS_EPILOG)
			return result;
		return (meta != null) ? meta : entry;
	}

	/**
	 * Returns the logfile path relative to the working directory.
	 *
	 * @return The logfile path to which this AssemblyLine is logging
	 *         information
	 */
	public String getLogFilePath() {
		// return log.getLogPath();
		return "metamerge.log";
	}

	/**
	 * getParam returns the value for a user-defined parameter. The user-defined
	 * parameter list is persisted between runs in the file specified in the
	 * AssemblyLine settings panel. This file is a simple text file with a
	 * "keyword:value" pair on each line.
	 *
	 * @param name
	 *            The user-defined parameter name
	 * @return The value or null if the parameter is undefined
	 */

	public Object getParam(String name) {
		return (String) taskParam.get(name.toLowerCase(Locale.ENGLISH));
	}

	/**
	 * getConfig returns a named value from the AssemblyLine's configuration.
	 *
	 * @param name
	 *            The AssemblyLine parameter name
	 * @return The value or null if the parameter is undefined
	 */
	public Object getConfig(String name) {
		return config.getSettings().getParameter(name);
	}

	/**
	 * getConfigStr returns a named string value from the AssemblyLine's
	 * configuration.
	 *
	 * @param name
	 *            The AssemblyLine parameter name
	 * @return The value or null if the parameter is undefined
	 * @see #getConfig
	 */
	public String getConfigStr(String name) {
		return (String) config.getSettings().getParameter(name);
	}

	/**
	 * getScriptEngine returns the ScriptEngine object for the AssemblyLine. The
	 * ScriptEngine object allows you to define additional beans and also
	 * execute script code on the fly.
	 *
	 * @return The com.ibm.di.script.ScriptEngine object
	 */
	public ScriptEngine getScriptEngine() {
		return se;
	}

	/**
	 * This method returns the TaskStatistics object for this AssemblyLine.
	 *
	 * @return The accumulated TaskStatistics object
	 */

	public TaskStatistics getStats() {
		return stats;
	}

	/**
	 * <p>
	 * Returns the AssemblyLineComponent with the given name. Mostly for
	 * internal use, when scripting you already have this object available using
	 * the name you gave this component, or the special variable thisConnector
	 * that always references the currently scoped component.
	 * </p>
	 * <p>
	 * May be called by different threads. Although the method is thread-safe,
	 * accessing the returned object is not - see the documentation of
	 * {@link com.ibm.di.server.AssemblyLineComponent} for information on the
	 * thread-safety of the returned object.
	 * </p>
	 *
	 * @param name
	 *            The connector name as specified in the configuration
	 * @return The AssemblyLineComponent
	 */
	public AssemblyLineComponent getConnector(String name) {
		synchronized (connectors) {
			for (int i = 0; i < connectors.size(); i++) {
				AssemblyLineComponent c = connectors.get(i);
				if (c.getName().compareToIgnoreCase(name) == 0) {
					return c;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a connectors index
	 *
	 * @param name
	 *            The connector name as specified in the configuration
	 * @return The connectors index
	 * @exception Exception
	 *                if the named connector could not be found.
	 */
	public int getConnectorIndex(String name) throws Exception {
		for (int i = 0; i < connectors.size(); i++) {
			AssemblyLineComponent c = connectors.get(i);
			if (c.getName().compareToIgnoreCase(name) == 0) {
				return i;
			}
		}

		log.exception("skipto.unknown.connector", name);
		return 0; // dummy return
	}

	/**
	 * Gets the list of AssemblyLineComponent objects this AssemblyLine hosts.
	 * AssemblyLineComponent objects can be Connectors, Function Components,
	 * Script Components, Loop Components, Branching Components, etc. <br>
	 * <b>Note: </b> This is not an immutable object. Any changes done over this
	 * object and the items in it will affect the AssemblyLine's
	 * structure/work-flow.
	 *
	 * @return The connectors value
	 */
	public List<AssemblyLineComponent> getConnectors() {
		return connectors;
	}

	/**
	 * Gets the list of components this AssemblyLine hosts in the data flow.
	 * AssemblyLineComponent objects can be Connectors, Function Components,
	 * Script Components, Loop Components, Branching Components, etc. <br>
	 * <b>Note: </b> This is not an immutable object. Any changes done over this
	 * object and the items in it will affect the AssemblyLine's
	 * structure/work-flow.
	 *
	 * @return The connectors value
	 */
	List<AssemblyLineComponent> getStateConnectors() {
		return stateConnectors;
	}

	/**
	 * Gets the log attribute of the AssemblyLine object
	 *
	 * @return The log value
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * Gets the nullBehavior attribute of the AssemblyLine object
	 *
	 * @return The nullBehavior value
	 */
	public String getNullBehavior() {
		if (nullBehavior == null || nullBehavior.length() == 0 || nullBehavior.equals("Default Behavior")) {
			return parent.getNullBehavior();
		} else {
			return nullBehavior;
		}
	}

	/**
	 * Gets the nullBehaviorValue attribute of the AssemblyLine object
	 *
	 * @return The nullBehaviorValue value
	 */
	public String getNullBehaviorValue() {
		if (nullBehaviorValue == null || !"Value".equals(nullBehavior)) {
			return parent.getNullBehaviorValue();
		} else {
			return nullBehaviorValue;
		}
	}

	/**
	 * Gets the nullDefinition attribute of the AssemblyLine object
	 *
	 * @return The nullDefinition value
	 */
	public String getNullDefinition() {
		if (nullDefinition == null || nullDefinition.length() == 0 || nullDefinition.equals("Default")) {
			return parent.getNullDefinition();
		} else {
			return nullDefinition;
		}
	}

	/**
	 * Gets the nullDefinitionValue attribute of the AssemblyLine object
	 *
	 * @return The nullDefinitionValue value
	 */
	public String getNullDefinitionValue() {
		if (nullDefinitionValue == null || !"Value".equals(nullDefinition)) {
			return parent.getNullDefinitionValue();
		} else {
			return nullDefinitionValue;
		}
	}

	/**
	 * The run method for the AssemblyLine thread.
	 */
	@Override
	public void run() {

		// We dont execute EH in manual mode
		if (runMode != RUNMODE_I_MANUAL) {

			notifyStarted();

			// See if we have server mode connectors defined
			if (alPool != null) {
				executeWithALPool();
			} else {
				executeAL();
			}
		}
	}

	/**
	 * Checks if this AssemblyLine contains an enabled server mode connector.
	 *
	 * @return true if the AL contains an enabled server mode connector.
	 */
	private static boolean hasServerModeConnector(AssemblyLineConfig config) {
		List<BaseConfiguration> list = config.getEntryFeedComponents().getConfigurations(null);
		for (BaseConfiguration bc : list) {
			ConnectorConfig cc = (ConnectorConfig) bc;
			if (ConnectorConfig.SERVER_MODE.equals(cc.getMode()) && cc.getEnabled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update statistics, close log files, disable debug etc ...
	 *
	 * @param eperror
	 *            {@link Exception} object
	 */
	private void cleanup(Exception eperror) {

		setParam("dt_end", Long.toString(System.currentTimeMillis()));
		if (eperror != null) {
			setParam("ex_class", eperror.getClass());
			setParam("ex_message", eperror.getMessage());
			setParam("exit_status", "fail");
		} else {
			setParam("ex_message", null);
			setParam("ex_class", null);
			setParam("exit_status", "ok");
		}
		saveParams(getConfigStr("set_history"));

		disableDebug();

		if (se != null && !seIsInherited) {
			se.terminate();
		}
		se = null;

		connectors.clear();

		if (stateConnectors != null) {
			stateConnectors.clear();
			stateConnectors = null;
		}

		if (stateIterators != null) {
			stateIterators.clear();
			stateIterators = null;
		}

		if (internalLog != null) {
			internalLog.close();
			internalLog = null;
		}

		log = null;
	}

	/**
	 * The close method terminates all connectors
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void close() throws Exception {
		closeRegInfo();

		for (int i = 0; i < connectors.size(); i++) {
			AssemblyLineComponent c = connectors.get(i);
			try {
				startPerfRecording(getPerfComponent(ALState.MS_CLOSECONN, c.getName()));
				c.close();
				stopPerfRecording(getPerfComponent(ALState.MS_CLOSECONN, c.getName()));
			} catch (Exception err) {
				// vish
				// log.error("closing " + c.getName(), err);
				stats.exception(err);
			}
		}

		if (conn != null) {
			conn.terminate();
		}
		conn = null;

	}

	/**
	 * Closes all connectors and releases resources.It is recommended that you
	 * use the shutdown() method if you want to halt an AssemblyLine before it's
	 * normal completion.
	 *
	 * @exception Exception
	 *                in case an error occurs.
	 * @deprecated Use shutdown()
	 */
	public void terminate() throws Exception {
		close();
		log = null;
		stats = null;
		config = null;
	}

	/**
	 * This method loads all connectors and prepares them for the AssemblyLine
	 * run.
	 *
	 * @exception Exception
	 *                derived by an AL component throwing an exception
	 */
	private void loadConnectors() throws Exception {
		int dupentries = 10;
		if (getConfigStr("findreturncount") != null)
			dupentries = parseIntParam("findreturncount");

		// Load iterators
		if (!ignoreInputComponents) {
			List<BaseConfiguration> list = config.getEntryFeedComponents().getConfigurations(null);
			for (BaseConfiguration bc : list) {
				AssemblyLineComponent alc = loadConnector(bc, dupentries);
				if (alc == null)
					continue;
				if (alc.getType() == ServerConstants.TYPE_ITERATOR)
					stateIterators.add(alc);
				else {
					// Misplaced component, fix it to work for backwards comp.
					log.warn("AssemblyLine.misplaced.connector", bc.getShortName());
					stateConnectors.add(alc);
					alc.setParentIndex(-1);
					if (bc instanceof ContainerConfig)
						loadContainerConnectors((ContainerConfig) bc, dupentries, stateConnectors.size() - 1);
					addEndComponent(alc, bc, -1);
				}
			}
		}

		// Load other connectors
		loadContainerConnectors(config.getDataFlowComponents(), dupentries, -1);

	}

	/**
	 * Loads configuration form a container object.
	 *
	 * @param container
	 *            {@link ContainerConfig} object which holds config objects
	 * @param dupentries
	 *            number of duplicate entries
	 * @param parentIndex
	 *            the index of the parent object
	 * @throws Exception
	 *             if an error occurs
	 */
	private void loadContainerConnectors(ContainerConfig container, int dupentries, int parentIndex) throws Exception {

		for (int i = 0; i < container.size(); i++) {
			BaseConfiguration bc = container.getConfig(i);
			AssemblyLineComponent alc = loadConnector(bc, dupentries);
			if (alc == null)
				continue; // Probably a disabled component
			stateConnectors.add(alc);
			alc.setParentIndex(parentIndex);

			if (bc instanceof ContainerConfig) {
				loadContainerConnectors((ContainerConfig) bc, dupentries, stateConnectors.size() - 1);
			}
			addEndComponent(alc, bc, parentIndex);
		}
	}

	private void addEndComponent(AssemblyLineComponent alc, BaseConfiguration bc, int parentIndex) {
		AssemblyLineComponent endComponent = null;
		// Add end-loop component
		if (alc instanceof LoopComponent) {
			endComponent = new EndLoopComponent(this, (LoopComponent) alc);
		} else if (alc instanceof BranchingComponent) {
			endComponent = new EndBranchComponent(this, (BranchingComponent) alc);
		}

		if (endComponent != null) {
			connectors.add(endComponent);
			stateConnectors.add(endComponent);
			endComponent.setParentIndex(parentIndex);
			endComponent.setEndComponentIndex(stateConnectors.size() - 1);
		}
		alc.setEndComponentIndex(stateConnectors.size() - 1);

	}

	/**
	 * This method inserts a connector interface as an Iterator in the current
	 * AL. The name parameter must match a connector in the AL config. The
	 * config is cloned and changed to Iterator mode and inherits from Virtual
	 * connector to force the ALComponent to use the provided connector
	 * interface. No initialize() is called for the connector. This method must
	 * be called AFTER the AL has loaded its connectors.
	 *
	 * @param name
	 *            the name of the {@link ConnectorConfig} object used as a
	 *            configuration object for the connector
	 * @param conn
	 *            the connector to put in Iterator mode
	 * @param executeProlog
	 *            tells whether the load/init has completed, if true then the
	 *            new {@link AssemblyLineComponent} will be initialized and
	 *            added to the iterator list
	 * @throws Exception
	 *             if the ConnectorConfig object could not be found using the
	 *             provided <code>name</code>. If the creation/initialization of
	 *             the new {@link AssemblyLineComponent} fails.
	 */
	public void addRuntimeIterator(String name, ConnectorInterface conn, boolean executeProlog) throws Exception {
		ConnectorConfig bc = (ConnectorConfig) config.getComponent(name);
		if (bc == null) {
			throw new Exception(sResHash.getString("connector.not.defined", name));
		}

		bc = (ConnectorConfig) bc.getClone();
		bc.setupInheritanceChain();
		bc.getConnectionConfig().setInheritsFromRef(ServerConstants.VIRTUAL_CONNECTOR_NAME);
		bc.setMode(ConnectorConfig.ITERATOR_MODE);

		AssemblyLineComponent c = new AssemblyLineComponent(this, name, bc, conn);
		addRuntimeComponent(c, executeProlog);
	}

	/**
	 * This method creates an ALComponent from the provided connector config.
	 * The connector must be Iterator or Server mode.
	 *
	 * @param name
	 *            used for error messaging only.
	 * @param config
	 *            the connector configuration object used for creating the new
	 *            {@link AssemblyLineComponent}
	 * @param executeProlog
	 *            tells whether the load/init has completed, if true then the
	 *            new {@link AssemblyLineComponent} will be initialized
	 * @throws Exception
	 *             if the connector is not in Iterator or Server mode. If the
	 *             {@link AssemblyLineComponent} creation/initialization fails.
	 */
	public void addRuntimeConnector(String name, ConnectorConfig config, boolean executeProlog) throws Exception {
		if (!config.isEntryFeed()) {
			String errorMessage = sResHash.getString("not.entryfeed.mode", new Object[] { name, config.getMode() });
			throw new Exception(errorMessage);
		}

		AssemblyLineComponent c = loadConnector(config, 0);
		addRuntimeComponent(c, executeProlog);
	}

	/**
	 * Adds a server/iterator connector the the list of execution and crates a
	 * reply channel if required
	 *
	 * @param c
	 *            connector
	 * @param executeProlog
	 *            tells whether the load/init has completed, if true then the
	 *            new {@link AssemblyLineComponent} will be initialized
	 * @throws Exception
	 *             if an error occurs
	 */
	private void addRuntimeComponent(AssemblyLineComponent c, boolean executeProlog) throws Exception {

		// add the connector to our lists of connectors
		connectors.add(c);
		stateIterators.add(c);

		// declare the connector
		se.declareStaticBean(c.getName(), c);

		// Set parent stats
		if (c.getStats() != null)
			c.getStats().setParentStats(stats);

		// If the load/init has completed then we must initialize the component
		if (executeProlog) {
			c.initialize();
			if (c.getConfiguration().getReplyRequired())
				addReplyChannel(c);
		}
	}

	/**
	 * This method removes a runtime connector from the AL.
	 *
	 * @param name
	 *            the name of the connector which will be removed
	 * @throws Exception
	 *             if a connector with that name does not exists
	 */
	public void removeRuntimeConnector(String name) throws Exception {
		AssemblyLineComponent c = getConnector(name);
		if (c == null) {
			throw new Exception(sResHash.getString("unknown.connector.name", name));
		}

		try {
			se.declareStaticBean(c.getName(), null);
			c.close();
		} finally {
			connectors.remove(c);
			stateIterators.remove(c);
			int i = getConnectorIndex2(name + ".reply");
			if (i >= 0)
				stateConnectors.remove(i);
		}
	}

	/**
	 * Add reply channel for a component.
	 *
	 * @param c
	 *            {@link AssemblyLineComponent}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void addReplyChannel(AssemblyLineComponent c) throws Exception {
		ReplyChannelComponent r = new ReplyChannelComponent(c);
		stateConnectors.add(r);
		r.setParentIndex(-1);
		r.setEndComponentIndex(stateConnectors.size() - 1);
	}

	/**
	 * This method enables the user to load an AssemblyLine component given a
	 * configuration. This method differs from the system.loadConnector() in
	 * that it returns a complete AL connector with attribute maps and hooks
	 * enabled. The returned connector is not registered in the scripting
	 * environment and is not inserted into the general flow of the AL.
	 *
	 * @param config
	 *            The component configuration (e.g. ConnectorConfig,
	 *            FunctionConfig etc).
	 *
	 * @return The AssemblyLine component
	 * @throws Exception
	 *             if the creation of the new {@link AssemblyLineComponent}
	 *             fails.
	 */
	public AssemblyLineComponent loadConnector(BaseConfiguration config) throws Exception {

		ConnectorConfig cfg = null;
		BranchingConfig bcg = null;
		BaseConfiguration bc = config;
		ScriptConfig scc = null;

		if (bc instanceof ConnectorConfig)
			cfg = (ConnectorConfig) bc;
		else if (bc instanceof BranchingConfig)
			bcg = (BranchingConfig) bc;
		else if (bc instanceof ScriptConfig)
			scc = (ScriptConfig) bc;
		else
			return null;

		String curtask = bc.getShortName();
		log.debug("load", curtask);

		AssemblyLineComponent c;

		// Runtime provided connector
		ConnectorInterface ci = conn;
		if (tcb != null && tcb.getRuntimeConnector(curtask) != null)
			ci = tcb.getRuntimeConnector(curtask);

		int type = (cfg == null ? 0 : ServerConstants.getType(cfg.getMode()));

		if (cfg instanceof FunctionConfig) {
			c = new FunctionComponent(this, curtask, (FunctionConfig) cfg);
		} else if (cfg instanceof ALMappingConfig) {
			c = new AttributeMapComponent(this, curtask, (ALMappingConfig) cfg);
		} else if (bcg != null) {
			if (bcg.getBranchType() == BranchingConfig.BRANCH_SWITCH || bcg.getBranchType() == BranchingConfig.BRANCH_CASE)
				c = new SwitchComponent(this, curtask, bcg);
			else
				c = new BranchingComponent(this, curtask, bcg);
		} else if (type == ServerConstants.TYPE_ITERATOR && CSDeltaTaskComponent.deltaEnabled(cfg)) {
			c = new CSDeltaTaskComponent(this, curtask, cfg, ci, false, false);
		} else {
			if (type == ServerConstants.TYPE_SCRIPT) {
				c = new ScriptComponent(this, curtask, cfg, ci);
			} else if (scc != null) {
				c = new ScriptComponent(this, curtask, scc, ci);
			} else {
				c = new AssemblyLineComponent(this, curtask, cfg, ci);
			}
		}

		return c;
	}

	/**
	 * Loads AssemblyLine component.
	 *
	 * @param bc
	 *            {@link BaseConfiguration}instance , object to load.
	 * @param dupentries
	 *            number of duplicate entries
	 * @return null if configuration instance is not recognized, or the loaded
	 *         {@link AssemblyLineComponent}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private AssemblyLineComponent loadConnector(BaseConfiguration bc, int dupentries) throws Exception {

		ConnectorConfig cfg = null;
		BranchingConfig bcg = null;
		LoopConfig lcg = null;
		ScriptConfig scc = null;

		if (bc instanceof ConnectorConfig)
			cfg = (ConnectorConfig) bc;
		else if (bc instanceof LoopConfig)
			lcg = (LoopConfig) bc;
		else if (bc instanceof BranchingConfig)
			bcg = (BranchingConfig) bc;
		else if (bc instanceof ScriptConfig)
			scc = (ScriptConfig) bc;
		else
			return null;

		String curtask = bc.getShortName();
		if (cfg != null && cfg.getState().equals(ConnectorConfig.DISABLED_STATE)) {
			log.debug("component.disabled", curtask);
			try {
				se.declareStaticBean(curtask, null);
			} catch (Exception e) {
				log.warn(sResHash.getString("problem.declare.bean", curtask));
			}
			return null;
		} else if (cfg == null && !bc.getEnabled()) {
			log.debug("component.disabled", curtask);
			try {
				se.declareStaticBean(curtask, null);
			} catch (Exception ignore) {
				// Exception is not thrown in the JS Engine code,
				// but Exception is declared in the method's signature and
				// try/catch is needed.
			}
			return null;
		}

		log.debug("load", curtask);

		try {

			AssemblyLineComponent c;

			// Runtime provided connector
			ConnectorInterface ci = conn;
			FunctionInterface fi = null;
			boolean forceRuntime = false;
			if (tcb != null && tcb.getRuntimeConnector(curtask) != null) {
				ci = tcb.getRuntimeConnector(curtask);
				forceRuntime = true;
			} else if (tcb != null && tcb.getRuntimeFunction(curtask) != null) {
				fi = tcb.getRuntimeFunction(curtask);
				forceRuntime = true;
			}

			int type = (cfg == null ? 0 : ServerConstants.getType(cfg.getMode()));

			if (cfg instanceof FunctionConfig) {
				c = new FunctionComponent(this, curtask, (FunctionConfig) cfg, fi);
			} else if (cfg instanceof ALMappingConfig) {
				c = new AttributeMapComponent(this, curtask, (ALMappingConfig) cfg);
			} else if (lcg != null) {
				c = new LoopComponent(this, curtask, lcg);
			} else if (scc != null) {
				c = new ScriptComponent(this, curtask, scc, ci);
			} else if (bcg != null) {
				if (bcg.getBranchType() == BranchingConfig.BRANCH_SWITCH || bcg.getBranchType() == BranchingConfig.BRANCH_CASE)
					c = new SwitchComponent(this, curtask, bcg);
				else
					c = new BranchingComponent(this, curtask, bcg);
				// Verify that a CASE, ELSE or ELSIF is preceded by a SWITCH,
				// IF or ELSEIF component (D-4620)
				verifyBranch(bcg);

			} else if (type == ServerConstants.TYPE_SCRIPT) {
				c = new ScriptComponent(this, curtask, cfg, ci);
			} else if (type == ServerConstants.TYPE_ITERATOR && CSDeltaTaskComponent.deltaEnabled(cfg)) {

				c = new CSDeltaTaskComponent(this, curtask, cfg, ci, false, forceRuntime);
			} else {
				c = new AssemblyLineComponent(this, curtask, cfg, ci, forceRuntime);
			}

			if (c.getStats() != null)
				c.getStats().setParentStats(stats);

			c.setDuplicateEntryCount(dupentries);

			connectors.add(c);
			return c;

		} catch (Exception e) {
			log.error("loading.connector", curtask, e);
			close();

			Entry err = new Entry();
			err.setAttribute("status", "fail");
			err.setAttribute("exception", e);
			err.setAttribute("message", e.getMessage());
			err.setAttribute("class", e.getClass().getName());
			err.setAttribute("operation", "loadConnector");
			err.setAttribute("connectorname", curtask);
			se.declareStaticBean("error", err);

			log.exception("initialization.failed", curtask, e.toString());
		}

		return null;
	}

	/**
	 * Verify that a CASE, ELSE or ELSIF is preceded by a CASE, IF or ELSEIF
	 * component
	 *
	 * @param bcg
	 *            {@link BranchingConfig}
	 * @throws Exception
	 *             if the condition is not satisfied.
	 */
	private void verifyBranch(BranchingConfig bcg) throws Exception {
		int type = bcg.getBranchType();
		if (type == BranchingConfig.BRANCH_IF || type == BranchingConfig.BRANCH_SWITCH)
			return;

		if (connectors.size() < 1) {
			throw new Exception(sResHash.getString("MISERVER.AL.BRANCH.COMPONENT.WITH.NO.PRECEEDING.IF.SWITCH.COMPONENT", bcg
					.getName()));
		}

		Object comp = connectors.get(connectors.size() - 1);
		int prev;
		if (type == BranchingConfig.BRANCH_CASE && comp instanceof SwitchComponent) {
			prev = ((SwitchComponent) comp).getBranchType();
		} else if (comp instanceof EndBranchComponent) {
			prev = ((EndBranchComponent) comp).getParentBranchType();
		} else {
			throw new Exception(sResHash.getString("MISERVER.AL.BRANCH.COMPONENT.WITH.NO.PRECEEDING.IF.SWITCH.COMPONENT", bcg
					.getName()));
		}

		if (type == BranchingConfig.BRANCH_CASE) {
			if (prev != BranchingConfig.BRANCH_CASE && prev != BranchingConfig.BRANCH_SWITCH) {
				throw new Exception(sResHash.getString("MISERVER.AL.CASE.BRANCH.WITHOUT.PRECEEDING.SWITCH.OR.CASE.COMPONENT", bcg
						.getName()));
			}
		} else if (type == BranchingConfig.BRANCH_ELSE) {
			if (prev != BranchingConfig.BRANCH_IF && prev != BranchingConfig.BRANCH_ELSEIF) {
				throw new Exception(sResHash.getString("MISERVER.AL.ELSE.BRANCH.WITHOUT.PRECEEDING.IF.ELSEIF.COMPONENT", bcg
						.getName()));
			}
		} else if (type == BranchingConfig.BRANCH_ELSEIF) {
			if (prev != BranchingConfig.BRANCH_IF && prev != BranchingConfig.BRANCH_ELSEIF) {
				throw new Exception(sResHash.getString("MISERVER.AL.ELSEIF.BRANCH.WITHOUT.PRECEEDING.IF.ELSEIF.COMPONENT", bcg
						.getName()));
			}
		} else {
			// We cannot end up here, but findbugs complains about the code
			// unless we add a dummy statement here.
			return;
		}
	}

	/**
	 * This method initializes all connectors and prepares them for the
	 * AssemblyLine run.
	 *
	 * @exception Throwable
	 *                Exceptions thrown by AL components
	 */
	private void initConnectors() throws Throwable {
		for (int i = 0; i < connectors.size(); i++) {
			AssemblyLineComponent c = connectors.get(i);
			log.debug("component.initialize", c.getName());
			startPerfRecording(getPerfComponent(ALState.MS_INITCONN, c.getName()));

			c.initialize();

			stopPerfRecording(getPerfComponent(ALState.MS_INITCONN, c.getName()));
		}

	}

	/**
	 * This method sends the specified message (msg) to whatever Log Appenders
	 * have been defined for this AssemblyLine.
	 *
	 * @param msg
	 *            The message
	 */
	public void logmsg(Object msg) {
		log.loginfo(msg.toString());
	}

	/**
	 * This method sends the specified message (msg) to whatever Log Appenders
	 * have been defined for this AssemblyLine. This method logs a message with
	 * the specified level to the AssemblyLine log.
	 *
	 * @param level
	 *            Loglevel. Legal values are FATAL, ERROR, WARN, INFO, DEBUG.
	 *            Unrecognized keyword means DEBUG.
	 * @param msg
	 *            The message
	 */
	public void logmsg(String level, String msg) {
		log.log(level, msg);
	}

	/**
	 * Logs a message to the AssemblyLine log file
	 *
	 * @param msg
	 *            The message
	 */
	public void debug(Object msg) {
		log.logdebug(msg.toString());
	}

	/**
	 * Logs a message to the monitor. The monitor may be a Metamerge Monitor
	 * instance or a topic on the JMS system bus (if one is configured)
	 *
	 * @param msg
	 *            The message
	 */
	public void logmonitor(String msg) {
		setMonitorMessage(this, msg);
		// It appears the Monitor.setMonitorMessage is a dead end
		// since there is no listener for those events. Add an ALEvent
		// and send it via the APIEngine.
		try {
			DIEvent event = new ALEvent(DIEvent.EVT_AL_MSG, getName(), hashCode() + ":" + msg,
					getParent() != null ? getParent().getName() : "", getStats());
			APIEngine.sendNotification(event);
		} catch (Exception e) {
		}
	}

	/**
	 * This method dumps an object to whatever Log Appenders have been defined
	 * for this AssemblyLine.
	 *
	 * @param o
	 *            The object to dump
	 */
	public void dump(Object o) {
		log.dump(o);
	}

	/**
	 * Prints the contents of an entry to whatever Log Appenders have been
	 * defined for this AssemblyLine. .
	 *
	 * @param entry
	 *            The entry object to print
	 */
	public void dumpEntry(Entry entry) {
		log.dumpEntry(entry);
	}

	/**
	 * Remove the current debugger.
	 */
	public void disableDebug() {
		disableDebug(null);
	}

	/**
	 * This method sends a message to the remote UI and closes the debugger
	 * connection if it is open.
	 *
	 * @param msg
	 *            Message to be sent
	 * @see #disableDebug()
	 */
	public void disableDebug(Object msg) {
		disableDebug(msg, false);
	}

	/**
	 * <p>
	 * This method sends a message to the remote UI and closes the debugger
	 * connection if it is open.
	 * </p>
	 * <p>
	 * This method is for internal use only. Do not call it from user code.
	 * </p>
	 *
	 * @param msg
	 *            Message to be sent
	 * @param async
	 *            If true will remove the debugger asynchronously (not right
	 *            away but on the next AssemblyLine step). Always set to true if
	 *            you call this method from another thread. Always set to false
	 *            if you call the method from the AssemblyLine thread.
	 */
	public void disableDebug(final Object msg, boolean async) {

		if (alPool == null) {
			AssemblyLineCommand cmd = new AssemblyLineCommand() {
				public void execute() {
					try {
						if (debugger != null && msg != null) {
							debugMsg(msg);
						}
					} catch (Exception exc) {
						log.warn("ASSEMBLYLINE.FAIL.TO.CLOSE.DEBUGGER", exc);
					}
					removeDebugger();
				}
			};

			if (async) {
				submitCommand(cmd);
			} else {
				try {
					cmd.execute();
				} catch (Exception ex) {
					log.logerror(ex.toString(), ex);
				}
			}
		} else {
			alPool.disableDebug(msg);
		}
	}

	/**
	 * <p>
	 * This method establishes a debugging session between this AL and a remote
	 * debugger client (e.g. CE).
	 * </p>
	 * <p>
	 * Must not call this method from other threads, while the AssemblyLine is
	 * running.
	 * </p>
	 *
	 * @param port
	 *            The TCP port number of the remote debugger client
	 *
	 * @param host
	 *            The host name of the remote debugger client
	 * @param onerror
	 *            if true breakpoints are disabled except when there is an
	 *            error.
	 * @throws Exception
	 *             If there is already a debugger or if the debugger cannot be
	 *             initialized.
	 */
	public void enableDebug(int port, String host, boolean onerror) throws Exception {
		enableDebug(port, host, onerror, false);
	}

	/**
	 * <p>
	 * This method establishes a debugging session between this AL and a remote
	 * debugger client (e.g. CE).
	 * </p>
	 * <p>
	 * This method is for internal use only. Do not call it from user code.
	 * </p>
	 *
	 * @param port
	 *            The TCP port number of the remote debugger client
	 *
	 * @param host
	 *            The host name of the remote debugger client
	 * @param onerror
	 *            if true breakpoints are disabled except when there is an
	 *            error.
	 * @param async
	 *            If true will attach the debugger asynchronously - the debugger
	 *            will not get attached right away and the AssemblyLine will
	 *            pick it up after its current step. Always set to true if you
	 *            call this method from another thread. Always set to false if
	 *            you call the method from the AssemblyLine thread.
	 * @throws Exception
	 *             If there is already a debugger or if the debugger cannot be
	 *             initialized.
	 */
	public void enableDebug(final int port, final String host, final boolean onerror, boolean async) throws Exception {

		if (alPool == null) {
			synchronized (commands) {
				if (debugger != null) {
					throw new Exception(sResHash.getString("AL.ALREADY.HAS.DEBUGGER"));
				}

				// check if there is a pending add debugger command
				for (AssemblyLineCommand cmd : commands) {
					if (cmd.getType() == AssemblyLineCommand.AL_CMD_ADD_DEBUGGER) {
						throw new Exception(sResHash.getString("AL.ALREADY.HAS.DEBUGGER"));
					}
				}

				// there is no debugger - prepare ours
				AssemblyLineCommand cmd = new AssemblyLineCommand(AssemblyLineCommand.AL_CMD_ADD_DEBUGGER) {
					public void execute() throws Exception {
						// do not add a debugger if it is too late
						if (getCurrentStep() >= ALState.MS_TERMINATE)
							return;

						DebugServer d = new DebugServer(getName());
						if (! d.debugConnect(host, port, onerror)) {
//							throw new Exception(sResHash.getString("AL.CANNOT.CONNECT.TO.DEBUGGER", new Object[] { host, port }));
							return;
						}
						addDebugger(d);
					}
				};
				if (async) {
					submitCommand(cmd);
				} else {
					cmd.execute();
				}
			}
		} else {
			alPool.enableDebug(port, host, onerror);
		}
	}

	/**
	 * Returns status of the debugger session
	 *
	 * @return true if there is a debugger session active, false otherwise
	 */
	public boolean debuggerEnabled() {
		if (debugger == null) {
			return false;
		}
		if (debugger.isConnected()) {
			return true;
		}

		return false;
	}

	/**
	 * This method sends an object for display to the debug panel.
	 *
	 * @param obj
	 *            The object to display. This object must be
	 *            {@link Serializable}
	 * @exception Exception
	 *                Serialization exception
	 */
	public void debugMsg(Object obj) throws Exception {
		if (debuggerEnabled()) {
			debugger.debugMsg(obj);
		}
	}

	/**
	 * This method sends an object for display to the debug panel and waits for
	 * a continue/stop message. If the AL is running in Step (Paused) mode, then
	 * execution is paused and control returned to the user.
	 *
	 * @param obj
	 *            The object to display
	 * @exception Exception
	 *                Serialization exception
	 */
	public void debugBreak(Object obj) throws Exception {
		if (debuggerEnabled()) {
			debugger.debugBreak(obj);
		}
	}

	/**
	 * This method forces the debugger to stop if it runs in OnError mode.
	 * Otherwise this method is identical to debugBreak(Object obj).
	 *
	 * @param obj
	 *            The error
	 *
	 * @exception Exception
	 *                Serialization exception
	 */
	public void debugBreakError(Object obj) throws Exception {
		if (debuggerEnabled()) {
			debugger.debugBreak(obj, false);
		}
	}

	/**
	 * This method creates the script engine instance. Main beans are registered
	 * and and user defined scripts are included from the library.
	 *
	 * @exception Exception
	 *                Script engine exceptions
	 */
	public void initScriptEngine() throws Exception {
		// Initialize script engine

		startPerfRecording(getPerfComponent(ALState.MS_START, "InitSE"));

		if (se == null) {
			boolean debugjs = debuggerEnabled() || (tcb != null && tcb.hasProperty(TCB_DEBUG_JAVASCRIPT));
			debugjs = true;  // TODO: Find a better formula
			se = new ScriptEngine(getConfigStr("ScriptEngine"), getParent(), debugjs);
		}
		se.declareStaticBean("task", this);
		se.declareStaticBean("main", parent, RSInterface.class);
		se.declareStaticBean("status", stats);
		se.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);

		// Declare Java Class libraries
		se.declareUserFunctions();

		// Give debugger our script engine.
		if (debuggerEnabled()) {
			debugger.setScriptEngine(se);
		}

		if (tcb != null)
			tcb.loadMapping();
		stopPerfRecording(getPerfComponent(ALState.MS_START, "InitSE"));
	}

	/**
	 * This method performs the initialization of the AssemblyLine.
	 *
	 * @param taskName
	 *            The name of the assemblyline
	 * @exception Exception
	 *                Any
	 */
	private void initializeTask(String taskName) throws Exception {

		//Patch around a problem when starting ALs using RMI.
		boolean hasSetServer = false;

		if (SystemFunctions.getServer() == null ) {
			SystemFunctions.setServer(parent);
			hasSetServer = true;
		}

		setName(taskName);

		if (log == null) {
			log = internalLog = new Log("miserver", getLogCategory());
		} else {
			// Redefine log so we have our own.
			log = internalLog = new Log(log);
		}

		log.setDebug(config.getDebug(false));
		// Make sure TCB has a reference to correct task context
		tcb.setTask(this);

		nullBehavior = config.getSettings().getNullBehavior();
		nullBehaviorValue = config.getSettings().getNullBehaviorValue();
		nullDefinition = config.getSettings().getNullDefinition();
		nullDefinitionValue = config.getSettings().getNullDefinitionValue();

		// Throw an Exception if checkpoint is enabled.
		if (config.getCheckpointConfig().getEnabled()) {
			log.exception("AssemblyLine.checkpoint.disabled");
		}

		// collect the debug settings of the components
		componentDebugModes.putAll(getComponentDebugModes(config));

		// initialize the AL pool if necessary
		if (runMode != RUNMODE_I_MANUAL && hasServerModeConnector(config)) {
			alPool = new AssemblyLinePool(getName(), getLog(), getParent(), config, tcb.getStringProperty(TCB_DEBUG_HOST,
			"localhost"), tcb.getIntProperty(TCB_DEBUG_PORT, -1), tcb.getBoolProperty(TCB_DEBUG_ONERROR, false),
			componentDebugModes);
		}
		scriptObject.setAttribute("AssemblyLine", taskName);

		if (hasSetServer) {
			SystemFunctions.setServer(null);
		}
	}

	/**
	 * Logs the Stats attribute of the AssemblyLine object
	 */
	private void logStats() {
		log.info("begin.connector.statistics");
		stats.end();
		try {
			for (int i = 0; i < connectors.size(); i++) {
				AssemblyLineComponent tc = connectors.get(i);
				String msg;

				if (tc instanceof EndLoopComponent || tc instanceof EndBranchComponent) {
					continue;
				} else {
					TaskStatistics s = tc.getStats();
					if (s == null)
						msg = log.getString("no.statistics");
					else if (tc instanceof LoopComponent)
						msg = s.getLoopStats();
					else if (tc instanceof SwitchComponent)
						msg = s.getSwitchStats();
					else if (tc instanceof BranchingComponent)
						msg = s.getBranchStats();
					else if (tc instanceof ScriptComponent)
						msg = s.getScriptStats();
					else
						msg = s.getMsg();
				}
				log.loginfo(" [" + tc.getName() + "] " + msg);
			}
		} catch (Exception ignore) {
			log.error("no.statistics", ignore);
		}
		log.info("total", stats.getMsg());
		log.info("end.connector.statistics");
	}

	/**
	 * This method reads the "task_params" file.
	 *
	 * @param name
	 *            Name of the file to read as expose as AL params
	 */
	private void loadParams(String name) {
		if (name == null) {
			return;
		}

		if (name.compareTo("(none)") == 0) {
			return;
		}

		if (name.length() < 1) {
			return;
		}

		String path = name;

		try {
			BufferedReader input = new BufferedReader(new FileReader(path));
			String str;

			while ((str = input.readLine()) != null) {
				int i = str.indexOf(":");
				if (i > 0) {
					setParam(str.substring(0, i), str.substring(i + 1));
					log.debug("taskparam.in", str.substring(0, i), str.substring(i + 1));
				}
			}
			input.close();
		} catch (IOException e) {
			log.error("error.restoring.taskparam", e);
		}
	}

	/**
	 * This method saves the current AL params to the file configured in the AL
	 * settings
	 */
	public void saveParams() {
		saveParams(getConfigStr("set_history"));
	}

	/**
	 * This method saves the current AL params to the specified file
	 *
	 * @param name
	 *            The name of the target file
	 */
	private void saveParams(String name) {
		if (name == null) {
			return;
		}

		if (name.compareTo("(none)") == 0) {
			return;
		}

		if (name.length() < 1) {
			return;
		}

		String path = name;

		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(path));
			String str;

			for (Enumeration<String> en = taskParam.keys(); en.hasMoreElements();) {
				str = en.nextElement();
				Object obj = taskParam.get(str);
				output.write(str + ":" + obj.toString() + "\r\n");
				log.debug("taskparam.out", str, obj.toString());
			}
			output.flush();
			output.close();

		} catch (IOException e) {
			log.error("error.saving.taskparam", e);
		}
	}

	/**
	 * Returns the int value of a configuration parameter
	 *
	 * @param paramName
	 *            Parameter name
	 * @return The int value
	 */
	private int parseIntParam(String paramName) {
		String str = getConfigStr(paramName);
		if (str != null) {
			if (str.length() == 0) {
				str = "0";
			}
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException nfe) {
				log.error("bad.number.parameter", paramName, str);
				return 0;
			}
		}
		return 0;
	}

	/**
	 * @return false
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean wasRestarted() {
		return false;
	}

	/**
	 * @return false
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean isRestarting() {
		return false;
	}

	/**
	 * This method runs the prolog before initialize hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runProlog0() throws Exception {
		se.declareBean("work", getWork());
		runScript(this, InternalSchema.AL_PROLOG_INIT);
	}

	/**
	 * This method runs the prolog after initialize hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runProlog() throws Exception {
		se.declareBean("work", getWork());
		runScript(this, InternalSchema.AL_PROLOG);
	}

	/**
	 * This method runs the epilog after close hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runEpilog2() throws Exception {
		if (se == null)
			return;
		se.declareBean("work", getResult());
		runScript(this, InternalSchema.AL_EPILOG2);
	}

	/**
	 * This method runs the epilog before close hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runEpilog() throws Exception {
		if (se == null) {
			log.warn("problem.setup.assemblyline");
			return;
		}

		se.declareBean("work", getResult());
		runScript(this, InternalSchema.AL_EPILOG);
	}

	/**
	 * This method runs the terminate hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runExitHook() throws Exception {
		if (se == null)
			return;
		se.declareBean("work", getWork());
		runScript(this, InternalSchema.AL_SHUTDOWN);
	}

	/**
	 * This method runs the startcycle hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runStartCycle() throws Exception {
		se.declareBean("work", getWork());
		runScript(this, InternalSchema.AL_STARTCYCLE);
	}

	/**
	 * This method runs the InternalSchema.AL_ONSUCCESS hook
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runOnSuccess() throws Exception {
		if (se == null)
			return;
		se.declareBean("work", getResult());
		runScript(this, InternalSchema.AL_ONSUCCESS);
	}

	/**
	 * This method runs the InternalSchema.AL_ONFAILURE hook
	 */
	private void runOnFailure() {
		if (se == null)
			return;
		try {
			se.declareBean("work", getResult());
			runScript(this, InternalSchema.AL_ONFAILURE);
		} catch (InterpretException iExc) {
			log.warn("ASSEMBLYLINE.DECLARE.WORK.EXCEPTION", iExc);
		} catch (Exception exc) {
			log.warn("ASSEMBLYLINE.RUN.SCRIPT.EXCEPTION", exc);
		}
	}

	/**
	 * Declare all connectors/components as beans in the script engine
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void declareConnectorBeans() throws Exception {
		for (int i = 0; i < connectors.size(); i++) {
			AssemblyLineComponent tc = connectors.get(i);
			if (!(tc instanceof ScriptComponent)) {
				log.fine("declare.connector.bean", tc.getName());
				se.declareStaticBean(tc.getName(), tc);
			}
		}

		Entry e = new Entry();
		e.setAttribute("status", "ok");
		se.declareStaticBean("error", e);
	}

	/**
	 * Run a named hook in the script engine.
	 *
	 * @param task
	 *            The {@link AssemblyLine} object
	 * @param type
	 *            The hook name (internal name)
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void runScript(Object task, String type) throws Exception {

		HookConfig hook = config.getHook(type);

		if (debuggerEnabled()) {
			debugBreak(type);
		}

		if (!hook.getEnabled()) {
			return;
		}

		String script = hook.getScript();

		if (script == null || script.length() == 0) {
			log.debug("no.script.for", log.getString(type));
			return;
		}

		if (!InternalSchema.AL_STARTCYCLE.equals(type)) {
			log.info("begin", type);
			startPerfRecording(getPerfComponent(type));
		}

		scriptObject.setAttribute("HookName", HookTree.getHookLabel(type));
		scriptObject.setAttribute("InternalHookName", type);
		se.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);

		try {
			log.debug("execute", type, script);
			se.exec(script, type);

		} catch (AbortALException aal) {
			throw aal;
		} catch (ReturnException re) {
			SystemFunctions.doNothing();
		} catch (Exception e) {
			if (e instanceof ExitBranchException) {
				if (type == InternalSchema.AL_STARTCYCLE)
					throw e; // Will be handled in msGetNextIteratorEntry
				if ("AssemblyLine".equalsIgnoreCase(e.getMessage())) {
					// We only understand "AssemblyLine"
					log.info("exit.assemblyline.request");
					if (state.mainStep < ALState.MS_EPILOG) 
						state.mainStep = ALState.MS_EPILOG - 1; // Will be incremented
					return;
				}
			}
			log.error("script.execution.failed", e);
			log.error("script.was", script);
			Entry err = new Entry();
			err.setAttribute("status", "fail");
			err.setAttribute("exception", e);
			err.setAttribute("message", e.getMessage());
			err.setAttribute("class", e.getClass().getName());
			err.setAttribute("operation", type);
			err.setAttribute("connectorname", HookTree.getHookLabel(type));

			se.declareStaticBean("error", err);
			throw e;
		}

		if (type != InternalSchema.AL_STARTCYCLE) {
			log.info("end", type);
			stopPerfRecording(getPerfComponent(type));
		}
	}

	/**
	 * Includes all scripts tagged as auto-include in the current script engine.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void autoIncludeScripts() throws Exception {
		if (config.getSettings().getBooleanParameter("includeGlobalPrologs", true))
			verifyNameSpaces();

		AssemblyLineScripts scripts = new AssemblyLineScripts(config);
		for (String name: scripts.getAllNames()) {
			debugBreak(name);
			se.loadScript(name, scripts.getScript(name), true);
		}

		// In case some scripts were not found, provoke an error
		for (String name: scripts.getNotFound()) {
			se.loadScript(parent, "prolog", name, true);
		}
	}

	/**
	 * Verifies that all namespaces can be found
	 * @throws Exception if a namespace cannot be found
	 */
	private void verifyNameSpaces() throws Exception {
		MetamergeConfig mc = config.getMetamergeConfig();

		MetamergeFolder table;
		try {
			table = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		} catch (Exception e) {
			return;
		}

		if (table == null)
			return;

		for (String name: table.getNames()) {
			if (MetamergeConfigFactory.getLocalNamespace(mc, name) == null) {
				throw new Exception(sResHash.getString("SCRIPT.ENGINE.CANNOTGETLOCAL.NAMESPACE.ERROR", name));
			}
		}
	}

	/**
	 * Request controlled shutdown of {@link AssemblyLine}
	 */
	public void shutdown() {
		terminationRequested = true;
		if (log == null)
			return; // Already stopped

		if (state != null)
			state.bailout = true;

		try {
			if (mActiveServerConnector != null) {
				mActiveServerConnector.terminateServer();
			}
		} catch (Exception serverConnExc) {
			String serverConnName = "";
			if (mActiveServerConnector != null) {
				serverConnName = "'" + mActiveServerConnector.getName() + "' ";
			}
			String errorMessage = sResHash.getString("cannot.terminate.server.conn", new Object[] { serverConnName,
					serverConnExc.toString() });
			log.logerror(errorMessage);
		}

		if (isAlive() && currentThread() != this)
			interrupt();
	}

	/**
	 * Shutdown an AssemblyLine, and wait a while for it to stop. Then try to
	 * provoke a stop of the AssemblyLine if needed. If the parameter aSync is
	 * true, create a new Thread to do the waiting. If trying to stop the
	 * current AssemblyLine, the method will throw an AbortALException, to force
	 * the script calling this method to quit.
	 *
	 * @param async
	 *            If true, stop the AssemblyLine in a new Thread.
	 * @throws AbortALException
	 *             if this method stops the AssemblyLine that called it.
	 * @since 7.1
	 */
	public void shutdown(boolean async) throws AbortALException {
		terminationRequested = true;
		if (log == null)
			return; // Already stopped

		String msg = sResHash.getString("shutting.down.al");
		logmsg(msg);
		shutdown();

		if (async) {
			new Thread() {
				@Override
				public void run() {
					waitForShutdown();
				}
			}.start();
		}
		if (Thread.currentThread() == this)
			throw new AbortALException(msg);

		if (!async)
			waitForShutdown();
	}

	/**
	 * Wait a while for the AssemblyLine to shutdown. If it does not, try to
	 * provoke it to stop.
	 */
	private void waitForShutdown() {
		if (state == null)
			return;
		doWait(4000);

		if (state.mainStep >= ALState.MS_TERMINATE)
			return;

		// Try to provoke the AssemblyLine to end.
		// The code must be here, because the AssemblyLine seems to be stuck,
		// and we want to try to force it to stop.
		try {
			if (runMode == RUNMODE_I_MANUAL) {
				if (state.mainStep < ALState.MS_EPILOG)
					executeTerminateAL();
			} else {
				AssemblyLineComponent conn = null;
				if (state.mainStep == ALState.MS_NEXTITER && state.iteratorIndex < stateIterators.size()) {
					conn = stateIterators.get(state.iteratorIndex);
				} else if (state.mainStep == ALState.MS_NEXTCONN && state.connectorIndex > 0
						&& state.connectorIndex <= stateConnectors.size()) {
					conn = stateConnectors.get(state.connectorIndex - 1);
				}
				if (conn != null)
					conn.close();
				else
					interrupt();
			}
		} catch (Throwable t) {
			if (log != null)
				log.logerror(t.getMessage(), t);
		}
		doWait(2000);

		if (state.mainStep < ALState.MS_TERMINATE && parent != null)
			parent.getLog().warn(sResHash.getString("AssemblyLine.not.stopped", getName()));
	}

	/**
	 * Wait a while to see if the AssemblyLine stops.
	 *
	 * @param ms
	 *            How long to wait.
	 */
	private void doWait(int ms) {
		try {
			shutdownLatch.await(ms, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			if (log != null)
				log.logerror(ie.toString(), ie);
		}
	}

	/**
	 * @return the status of termination requested flag
	 */
	public boolean getTerminationRequested() {
		return terminationRequested;
	}

	/**
	 * Interprets the TCB before AL starts executing
	 *
	 * @param tcb
	 *            The {@link TaskCallBlock} object to interpret.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void enterTCB(TaskCallBlock tcb) throws Exception {
		log.debug("begin.enter.taskcallblock");
		if (tcb.getALOperation() != null)
			opentry.setAttribute(OPENTRY_OPERATION, tcb.getALOperation());
		if (tcb.getOperationInitParams() != null)
			opentry.merge(tcb.getOperationInitParams());
		tcb.setConnectorParameters(this);
		setWork(tcb.buildInitialWorkEntry());
		log.debug("end.enter.taskcallblock");
	}

	/**
	 * Interprets the TCB after AL finished executing
	 *
	 * @param tcb
	 *            The {@link TaskCallBlock} object to interpret.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void exitTCB(TaskCallBlock tcb) throws Exception {
		if (tcb != null) {
			log.debug("begin.exit.taskcallblock");
			startPerfRecording(getPerfComponent(ALState.MS_BUILDTCB));
			tcb.setResultEntry(getResult());
			result = tcb.buildResultEntry();
			stopPerfRecording(getPerfComponent(ALState.MS_BUILDTCB));
			// Invoke callback if defined
			log.debug("end.exit.taskcallblock");
		}
	}

	/**
	 * Retrieves log4j category name.
	 *
	 * @return the log4j category name used by this AssemblyLine.
	 */
	public String getLogCategory() {
		if (logCategoryName == null) {
			logCategoryName = "AssemblyLine." + getName();
			if (Boolean.getBoolean("com.ibm.di.logging.addALcounter"))
				logCategoryName += "." + hashCode();
		}

		return logCategoryName;
	}

	/**
	 * Sets the log4j category name used when requesting a logger. If a logger
	 * object has been provided to the AL already this method returns false to
	 * signal that setting the value has no effect.
	 *
	 * @param categoryName
	 *            The logj4 category name
	 * @return True if setting the value will have effect.
	 */
	public boolean setLogCategory(String categoryName) {
		this.logCategoryName = categoryName;
		return (log == null);
	}

	/**
	 * Add iterators and other connectors to state list.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void stateSetupConnectors() throws Exception {
		//
		// Add reply channels for stateIterator connectors
		//
		for (int i = 0; i < stateIterators.size(); i++) {
			AssemblyLineComponent c = stateIterators.get(i);
			if ((c.getConfiguration().getReplyRequired())
					&& (c.getType() == ServerConstants.TYPE_SERVER || c.connector.getParam(TCB_FORCE_REPLYCHANNEL) != null)) {
				debug(sResHash.getString("add.reply.channel.conn", c.getName()));
				addReplyChannel(c);
			}
		}
	}

	/**
	 * This method performs the main cycle of the AL.
	 */
	private void executeAL() {

		// Initialize AL
		executeInitializeAL();

		// main loop
		executeMainLoop();
	}

	/**
	 * Internal method used by AssemblyLinePool.
	 */
	public void executeInitializeAL() {

		if (state != null)
			return;

		if (perfEnabled) {
			perfSt.initPerfStats();
			startPerfRecording(getPerfComponent(0));
		}

		// Add Specific logging for this instance
		if (tcb.getAddLogAppenders()) {
			try {
				LogUtils.addLoggers("AssemblyLine", getShortName(), log, config.getLogConfig(), getParent());
			} catch (Exception e) {
				log.error("Error while adding loggers: " + e.toString(), e);
			}
		}

		if (runMode == RUNMODE_I_NORMAL || runMode == RUNMODE_I_NODEBUG) {
			log.info("assemblyline.started", getName());
		} else if (runMode == RUNMODE_I_RECORD) {
			log.info("assemblyline.started.record", new Object[] { getName(), config.getSandboxConfig().getIdentifier() });
		} else if (runMode == RUNMODE_I_PLAYBACK) {
			log.info("assemblyline.started.playback", new Object[] { getName(), config.getSandboxConfig().getIdentifier() });
		} else if (runMode == RUNMODE_I_MANUAL) {
			log.info("assemblyline.started.manual", getName());
		}

		state = new ALState();
	}

	/**
	 * This method is automatically called by the AL when it is started in mode
	 * different than {@link #RUNMODE_MANUAL}
	 */
	public void executeMainLoop() {
		executeMainLoop(ALState.MS_TERMINATE);
	}

	/**
	 * This method runs the AL and continue running until the specific state is
	 * reached. <br>
	 * <br>
	 * <b>Note: </b> the specified state is also executed.
	 *
	 * @see ALState
	 * @param untilState
	 *            the state until the AL should run.
	 */
	public void executeMainLoop(int untilState) {
		while (state.mainStep <= untilState) {
			try {

				executeMainStep();

				/**
				 * Perform after cycle operations
				 */
				mainStepHook();

			} catch (Throwable error) {
				bailout(error);
			}
		}
	}

	/**
	 * Call this method when you want to terminate the AL that was started in
	 * Cycle mode.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void executeTerminateAL() throws Exception {
		if (runMode != RUNMODE_I_MANUAL) {
			String errorMessage = sResHash.getString("callable.only.al.cycle.mode");
			throw new Exception(errorMessage);
		}

		if (state.mainStep < ALState.MS_EPILOG)
			state.mainStep = ALState.MS_EPILOG;

		executeMainLoop();

	}

	/**
	 * Resets each of the Iterator Connectors. This call requests from each
	 * Iterator Connector to reconnect to its back-end data source.
	 *
	 * @throws Throwable
	 *             derived from the reseted ALComponent
	 */
	public void resetIterators() throws Throwable {
		if (stateIterators == null)
			return;

		for (int i = 0; i < stateIterators.size(); i++) {
			stateIterators.get(i).reconnect();
		}
	}

	/**
	 * This method is automatically called for AssemblyLines running in manual
	 * mode by the startAL() method.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	public void initExecuteProlog() throws Exception {

		notifyStarted();
		try {
			executeInitializeAL();

			while (state.mainStep < ALState.MS_NEXTITER) {
				executeMainStep();
				mainStepHook();
			}
		} catch (Throwable t) {
			Exception e = t instanceof Exception ? (Exception) t : new Exception(t);
			notifyTerminated();
			cleanup(e);
			throw e;
		}
	}

	/**
	 * This method call executeCycle with a null initial work and processTCB set
	 * to true.
	 *
	 * @return The work entry at the end of the cycle.
	 *
	 * @throws Throwable
	 *             when method is called and AL was not started in "Cycle" mode
	 *             or this method is called after having returned a null entry
	 *             (e.g. end of iteration)
	 */
	public Entry executeCycle() throws Throwable {
		return executeCycle(null, true);
	}

	/**
	 * This method call executeCycle with the provided work entry and processTCB
	 * set to true.
	 *
	 * @param workEntry
	 *            the work entry to use.
	 * @return The work entry at the end of the cycle.
	 *
	 * @throws Throwable
	 *             when method is called and AL was not started in "Cycle" mode
	 *             or this method is called after having returned a null entry
	 *             (e.g. end of iteration)
	 */
	public Entry executeCycle(Entry workEntry) throws Throwable {
		return executeCycle(workEntry, true);
	}

	/**
	 * This method is called by scripts and other classes that run the
	 * assemblyline in its own context. The AL must be created in "Cycle" mode
	 * to execute this method. If the workEntry is provided then this becomes
	 * the work entry and any iterators in the AL is not called.
	 *
	 * If the AL contains iterator(s) and you don't provide a work entry, the AL
	 * will keep returning results as long as the iterator(s) return data. At
	 * end of data, a null entry is returned.
	 *
	 * @param workEntry
	 *            The work entry to use, or null to use an empty work
	 *            entry/iterator entry
	 * @param processTCB
	 *            If true, the AL's call/return attribute maps are applied to
	 *            the provided entry and returned entry
	 * @return The work entry at the end of the cycle
	 * @throws Throwable
	 *             when method is called and AL was not started in "Cycle" mode
	 *             or this method is called after having returned a null entry
	 *             (e.g. end of iteration)
	 */
	public Entry executeCycle(Entry workEntry, boolean processTCB) throws Throwable {

		if (runMode != RUNMODE_I_MANUAL) {
			String errorMessage = sResHash.getString("assemblyline.not.in.cycle.mode");
			throw new Exception(errorMessage);
		}

		if (state.mainStep >= ALState.MS_EPILOG) {
			String errorMessage = sResHash.getString("cannot.execute.beyond.end");
			throw new Exception(errorMessage);
		}

		if (workEntry == null && stateIterators.size() == 0)
			workEntry = new Entry();

		// Make sure script engine is initialized before we enterTCB
		while (state.mainStep <= ALState.MS_START)
			executeMainStep();

		if (workEntry != null && processTCB)
			tcb.setInitialWorkEntry(workEntry);

		if (processTCB)
			enterTCB(tcb);
		else if (workEntry != null)
			setWork(workEntry);

		while (state.mainStep < ALState.MS_BEGINITER)
			executeMainStep();

		while (state.mainStep < ALState.MS_EPILOG) {

			/**
			 * Next main step
			 */
			try {
				executeMainStep();
			} catch (Throwable t) {
				mainStepHook();
				// Start at the beginning of a new cycle if the AL is used again
				if (state.mainStep < ALState.MS_EPILOG) {
					state.mainStep = ALState.MS_NEXTITER;
					// We should probably not increase the cycleCounter for an error?
					// state.cycleCounter++;  
				}
				throw t;
			}

			/**
			 * Perform after cycle operations
			 */
			mainStepHook();

			if (state.mainStep == ALState.MS_ENDCYCLE) {
				msEndCycle();
				if (meta != null && processTCB) {
					exitTCB(tcb);
				}
				return getResult();
			}
		}

		return null;
	}

	/**
	 * Returns the current AssemblyLine step position.
	 *
	 * @return the current state as {@link String}
	 */
	public String getCurrentState() {
		if (state != null)
			return ALState.MAIN_STEPS[state.mainStep];
		else
			return "NOT YET INITIALIZED";
	}

	/**
	 * This method is used for handling thrown during the AL's work-flow errors.
	 * The first step that is done is to give the debugger a chance to react.
	 * After that the error is accumulated in the statistics object. As a final
	 * step the AL state jumps to the {@link ALState#MS_EPILOG} and prepares for
	 * closure.
	 *
	 * @param error
	 *            the error which have to be handled.
	 */
	public void bailout(Throwable error) {

		// Let the debugger have a chance first
		if (debuggerEnabled() && !debugger.isAborted()) {
			// But only if the user didn't send a STOP already
			try {
				debugBreakError(getComponentException(error));
				// Maybe return if the user has fixed the problem?
				// return;
			} catch (Exception e) {
				// The debug session seems to be over. No need to log the
				// Exception.
			}
		}

		if (getTerminationRequested()) {
			// In this case the error is probably artificial, and we do not want
			// to log it.
			error = null;
			try {
				runExitHook();
			} catch (Exception e) {
				error = e;
			}
		}

		if (error != null) {
			String errorMessage = sResHash.getString("al.bailout.error", new Object[] { ALState.MAIN_STEPS[state.mainStep],
					error.toString() });
			if (log != null)
				log.error(errorMessage, error);
			stats.exception(error);
			stats.err();
		}
		state.bailout = true;

		if (state.mainStep < stats.bailoutStep)
			stats.bailoutStep = state.mainStep;

		try {
			if (debuggerEnabled())
				debugger.aborted(error);
		} catch (Exception e) {
			// No need to log an Exception.
			SystemFunctions.doNothing();
		}

		if (state.mainStep < ALState.MS_EPILOG)
			state.mainStep = ALState.MS_EPILOG;
		else
			state.mainStep++;

		// Check if we should do a state auto dump
		String path = System.getProperty("com.ibm.tdi.autodump.directory");
		if (path != null && path.length() > 0) {
			dumpAssemblyLineState(path + "/" + getName() + ".txt", true);
		}
	}

	/**
	 * Return a new Exception with the connector name.
	 *
	 * @param t
	 * @return
	 */
	private Throwable getComponentException(Throwable t) {
		Object o = se.getBean("error");
		if (o instanceof Entry) {
			String s = ((Entry) o).getString("connectorname");
			if (s != null) {
				String msg = t.toString();
				if (msg == null)
					msg = "";
				else if (msg.startsWith("java.lang.Exception: "))
					msg = msg.substring(21);
				else if (msg.startsWith("java.lang."))
					msg = msg.substring(10);
				msg = sResHash.getString("AssemblyLine.Problem", new Object[] { s, msg });
				return new Exception(msg, t);
			}
		}
		return t;
	}

	/**
	 * This method is called to initialize the debugger peer session
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msInitDebugger() throws Exception {
		// Debug mode - (don't enable debugger for first thread)
		if (tcb != null && tcb.hasProperty(TCB_DEBUG_PORT) && alPool == null) {
			enableDebug(tcb.getIntProperty(TCB_DEBUG_PORT, -1), tcb.getStringProperty(TCB_DEBUG_HOST, null), tcb.getBoolProperty(
					TCB_DEBUG_ONERROR, false));
		}

		// -- We also check the java system properties to see if any client has
		// requested
		// -- auto debugging of new assemblylines.
		if (debugger == null) {
			autoActivateDebugger();
		}

		state.mainStep++;
	}

	/**
	 * This method is called to terminate the debugger peer session
	 */

	// private void msCloseDebugger() { removed #13264
	// disableDebug();
	// state.mainStep++;
	// }

	/**
	 * This method is called after each cycle has completed.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void mainStepHook() throws Exception {

		if (state.mainStep >= ALState.MS_EPILOG)
			return;

		// log.info ( "MainStep: " + ALState.MAIN_STEPS[state.mainStep] );
		// External stop signal - run exit hook and return
		if (getTerminationRequested()) {
			state.bailout = true;
			runExitHook();
			state.mainStep = ALState.MS_EPILOG;
		}

		if (maxerrors > 0 && stats.err > maxerrors) {
			log.warn("max.errors.encountered", Integer.valueOf(maxerrors));
			state.mainStep = ALState.MS_EPILOG;
		}
	}

	/**
	 * This method executes the next step in the AL.
	 *
	 * @throws Throwable
	 *             any error that can occur during execution.
	 */
	private void executeMainStep() throws Throwable {

		// log.info ( "executeAL: step=" + state.MAIN_STEPS[state.mainStep] );

		executeAvailableCommands();

		switch (state.mainStep) {

		case ALState.MS_DEBUG_INIT:
			msInitDebugger();
			break;

		case ALState.MS_START:
			msStart();
			break;

		case ALState.MS_LOADCONN:
			msLoadConn();
			break;

		case ALState.MS_PROLOG0:
			msProlog0();
			break;

		case ALState.MS_INITCONN:
			msInitConn();
			break;

		case ALState.MS_PROLOG:
			msProlog();
			break;

		case ALState.MS_BEGINITER:
			msBeginIter();
			break;

		case ALState.MS_NEXTITER:
			msGetNextIteratorEntry();
			break;

		case ALState.MS_NEXTCONN:
			msExecuteNextConnector();
			break;

		case ALState.MS_ENDCYCLE:
			msEndCycle();
			break;

		case ALState.MS_ENDITER:
			msEndIter();
			break;

		case ALState.MS_EPILOG:
			msEpilog();
			break;

		case ALState.MS_CLOSECONN:
			msCloseConn();
			break;

		case ALState.MS_BUILDTCB:
			msBuildTCB();
			break;

		case ALState.MS_EPILOG2:
			msEpilog2();
			break;

			// case ALState.MS_DEBUG_CLOSE: Removed #13264
			// msCloseDebugger();
			// break;

		case ALState.MS_TERMINATE:
			msTerminate();
			break;
		}

		executeAvailableCommands();
	}

	/**
	 * Returns a connectors index
	 *
	 * @param name
	 *            The connector name as specified in the configuration
	 * @return The connectors index
	 * @throws Exception
	 *             if the connector could not be found by the provided name
	 */
	public int getConnectorIndex2(String name) throws Exception {
		for (int i = 0; i < stateConnectors.size(); i++) {
			AssemblyLineComponent c = stateConnectors.get(i);
			if (c.getName().compareToIgnoreCase(name) == 0) {
				return i;
			}
		}

		log.exception("skipto.unknown.connector", name);
		return -1; // dummy return
	}

	/**
	 * Perform initial setup
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msStart() throws Exception {

		startPerfRecording(getPerfComponent(ALState.MS_START));

		// Debug mode
		log.setDebug(config.getDebug());

		// Initialize script engine
		initScriptEngine();

		// Load task parameters from file
		loadParams(getConfigStr("get_history"));
		setParam("dt_begin", Long.toString(System.currentTimeMillis()));
		setParam("name", getName());

		stats.start();

		interval = parseIntParam("verbose");
		maxerrors = parseIntParam("maxerr");
		maxread = parseIntParam("maxread");
		numread = 0;

		log.debug("interval.maxerror.maxread", new Object[] { Integer.valueOf(interval), Integer.valueOf(maxerrors),
				Integer.valueOf(maxread) });

		openRegressionInfo();

		stopPerfRecording(getPerfComponent(ALState.MS_START));
		state.mainStep++;
	}

	/**
	 * This method is called to load connectors.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msLoadConn() throws Exception {
		startPerfRecording(getPerfComponent(ALState.MS_LOADCONN));

		// Load connectors
		log.debug("begin.load.connectors");
		loadConnectors();
		log.debug("end.load.connectors");

		stopPerfRecording(getPerfComponent(ALState.MS_LOADCONN));

		// Enable assemblyline debugger?
		if (debuggerEnabled()) {
			debugger.debugInit();
		}

		// Include global scripts
		autoIncludeScripts();

		// Validate task call block and apply connector settings
		if (tcb != null) {
			log.debug("begin.enter.taskcallblock");
			tcb.setConnectorParameters(this);
			if (getRunMode() != RUNMODE_I_MANUAL)
				setWork(tcb.buildInitialWorkEntry());
			log.debug("end.enter.taskcallblock");
		}

		// Declare script beans
		declareConnectorBeans();

		// Configure new arrays of connectors
		stateSetupConnectors();

		// Apply debug mode settings
		synchronized (componentDebugModes) {
			for (AssemblyLineComponent alc : connectors) {
				Boolean b = componentDebugModes.get(alc.getName());
				if (b != null) {
					alc.setDebug(b);
				}
			}
		}

		if (isSimulating())
			log.warn("Running.Simulation");

		state.mainStep++;
	}

	/**
	 * This method runs the prolog before initialize connectors
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msProlog0() throws Exception {
		runProlog0();
		state.mainStep++;
	}

	/**
	 * This method is called to initialize the connectors.
	 *
	 * @throws Throwable
	 *             if any error occurs.
	 */
	private void msInitConn() throws Throwable {
		startPerfRecording(getPerfComponent(ALState.MS_INITCONN));
		try {
			// Initialize connectors
			log.debug("begin.initialize.connectors");
			initConnectors();
			log.debug("end.initialize.connectors");
			state.mainStep++;
		} catch (ExitBranchException e) {
			if ("AssemblyLine".equalsIgnoreCase(e.getMessage())) {
				// We only understand "AssemblyLine"
				log.info("exit.assemblyline.request");
				state.mainStep = ALState.MS_EPILOG;
			} else {
				throw e;
			}
		}
		stopPerfRecording(getPerfComponent(ALState.MS_INITCONN));
	}

	/**
	 * This method runs the prolog after initialize connectors
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msProlog() throws Exception {
		runProlog();

		// Discard IWE? Typically used when IWE contains params but should not
		// be used in the cycle
		if (tcb.getBoolProperty(TCB_CP_DISCARD_IWE, false)) {
			entry = null;
		}

		state.mainStep++;
	}

	/**
	 * This method is called immediately before we start cycling input entries
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msBeginIter() throws Exception {

		if (!ignoreInputComponents) {
			startPerfRecording(getPerfComponent(ALState.MS_BEGINITER));
			log.info("begin.iteration");
		}

		state.mainStep = ALState.MS_NEXTITER;

		if (!ignoreInputComponents) {
			boolean onlyPassive = true;

			for (int i = 0; i < stateIterators.size(); i++)
				if (stateIterators.get(i).isEnabled())
					onlyPassive = false;

			if (onlyPassive) {
				log.info("no.iterator");

				// Make sure we always run once through the connector list
				if (entry == null)
					entry = new Entry();
			}
		}
	}

	/**
	 * This method sets the "meta" object to the next input entry.
	 *
	 * @throws Throwable
	 *             if any error occurs.
	 */
	private void msGetNextIteratorEntry() throws Throwable {

		if (interval > 0 && numread > 0 && (numread % interval) == 0) {
			log.info("entries.processed", Integer.valueOf(numread));
		}

		if (maxread > 0 && numread >= maxread) {
			log.info("max.entries.read", Integer.valueOf(maxread));
			state.mainStep = ALState.MS_EPILOG;
			return;
		}

		// Reset connector index
		state.setNext(0, true);

		if (debuggerEnabled()) {
			debugger.reachedCycle(state.cycleCounter+1);
		}

		startPerfRecording(getPerfComponent(InternalSchema.AL_STARTCYCLE));
		try { 
			runStartCycle();
		} catch (ExitBranchException ebe) {
			if ("AssemblyLine".equalsIgnoreCase(ebe.getMessage())) {
				state.mainStep = ALState.MS_EPILOG;
				log.info("exit.assemblyline.request");
				return;
			} // Could recognize other names.

			throw ebe;
		}
		stopPerfRecording(getPerfComponent(InternalSchema.AL_STARTCYCLE));

		if (entry != null) {
			// Use Runtime provided entry
			if (runMode != RUNMODE_I_MANUAL)
				log.info("runtime.provided.entry");
			result = meta = entry;
			parameterSubstitutionMap.put("work", meta);
			entry = null;
			state.mainStep = ALState.MS_NEXTCONN;
		} else if (state.iteratorIndex >= stateIterators.size()) {
			state.mainStep = ALState.MS_ENDITER;
		} else {
			AssemblyLineComponent iterator = stateIterators.get(state.iteratorIndex);
			startPerfRecording(getPerfComponent(iterator.getName()));
			meta = new Entry();
			parameterSubstitutionMap.put("work", meta);

			se.clear();

			if (debuggerEnabled()) {
				se.declareBean("work", meta);
				debugBreak(iterator.getName());
				se.clear();
			}

			try {
				if (iterator.willExecute(meta)) {
					iterator.resetStatus();
					meta = iterator.getnext(meta);
					if (meta == null)
						iterator.trigger("end_of_data", null, null);
				} else {
					meta = null;
				}

				if (meta == null) {
					state.mainStep = ALState.MS_ENDITER;
				} else {
					// Save reference for exit status
					result = meta;
					setOperationFromConnector(meta, iterator);
					iterator.handleSuccess("get", meta);
					writeRegressionInfo(iterator, meta);
					numread++;
					state.mainStep = ALState.MS_NEXTCONN;
					parameterSubstitutionMap.put("work", meta);
				}
			} catch (Throwable e) {
				try {
					writeRegressionInfo(iterator, meta);
					iterator.handleException("get", e, meta);
					state.mainStep = ALState.MS_NEXTCONN;
				} catch (SkipEntryException sk) {
					iterator.stats.skip();
					logskipmsg(iterator, sk);
					meta = null;
					numread++;
					state.mainStep = ALState.MS_NEXTITER;
				} catch (IgnoreEntryException ei) {
					iterator.stats.ignore();
					logskipmsg(iterator, ei);
					state.mainStep = ALState.MS_NEXTCONN;
				} catch (RestartEntryException re) {
					logskipmsg(iterator, re);
					state.mainStep = ALState.MS_NEXTCONN;
				} catch (RetryEntryException ree) {
					logskipmsg(iterator, ree);
					state.mainStep = ALState.MS_NEXTITER;
				} catch (SkipToException st) {
					iterator.stats.skip();
					log.debug("skip.to", st.getMessage());
					state.setNext(getConnectorIndex2(st.getMessage()), true);
					state.mainStep = ALState.MS_NEXTCONN;
				} catch (ExitBranchException ebe) {
					log.debug("skip.to", ebe.getMessage());
					exitBranch(iterator, ebe.getMessage());
				}
			}

			stopPerfRecording(getPerfComponent(iterator.getName()));
		}
	}

	/**
	 * This method executes the next connector operation.
	 *
	 * @throws Throwable
	 *             if any error occurs.
	 */
	private void msExecuteNextConnector() throws Throwable {

		if (state.connectorIndex >= stateConnectors.size()) {
			state.mainStep = ALState.MS_ENDCYCLE;
			return;
		}

		AssemblyLineComponent c = stateConnectors.get(state.getNext());
		String oper = TYPE_OPER[c.getType()];
		startPerfRecording(getPerfComponent(c.getName()));

		se.clear();

		if (debuggerEnabled() && !(c instanceof EndBranchComponent) && !(c instanceof EndLoopComponent)) {
			se.declareBean("work", meta);
			debugBreak(c.getName());
			se.clear();
		}

		try {
			// Make sure work is available for param subst in component configs
			if (meta != null)
				parameterSubstitutionMap.put("work", meta);

			if (!c.willExecute(meta)) {
				state.setNext(c.getEndComponentIndex() + 1, false);
				return;
			}
			c.resetStatus();

			switch (c.getType()) {
			case ServerConstants.TYPE_SCRIPT:
				c.add(meta);
				break;
			case ServerConstants.TYPE_LOOKUP:
				c.lookup(meta);
				break;
			case ServerConstants.TYPE_UPDATE:
				c.update(meta);
				break;
			case ServerConstants.TYPE_ADDONLY:
				c.add(meta);
				break;
			case ServerConstants.TYPE_DELETE:
				c.delete(meta);
				break;
			case ServerConstants.TYPE_DELTA:
				c.delta(meta);
				break;
			case ServerConstants.TYPE_CALLREPLY:
			case ServerConstants.TYPE_FUNCTION:
				c.callreply(meta);
				break;
			case ServerConstants.TYPE_REPLYCHANNEL:
				c.reply(meta);
				break;
			case ServerConstants.TYPE_LOOP:
				c.add(meta);
				break;
			case ServerConstants.TYPE_ATTRIBUTEMAP:
				c.add(meta);
				break;
			case ServerConstants.TYPE_BRANCH:
			case ServerConstants.TYPE_SWITCH:
			case ServerConstants.TYPE_CASE:
				c.add(meta);
				break;
			case ServerConstants.TYPE_ITERATOR:
				Entry e = c.getnext(meta);
				if (e == null)
					c.trigger("end_of_data", meta, null);
				break;
			}

			setOperationFromConnector(meta, c);

			c.handleSuccess(oper, meta);
			writeRegressionInfo(c, meta);

		} catch (Throwable e) {
			try {
				writeRegressionInfo(c, meta);
				c.handleException(oper, e, meta);

			} catch (RestartEntryException re) {
				logskipmsg(c, re);
				state.setNext(0, true);

			} catch (RetryEntryException ree) {
				logskipmsg(c, ree);
				state.setNext(stateConnectors.indexOf(c), true);
			} catch (IgnoreEntryException ei) {
				logskipmsg(c, ei);
				c.stats.ignore();
			} catch (SkipEntryException sk) {
				logskipmsg(c, sk);
				c.stats.skip();

				if ((runMode == RUNMODE_I_MANUAL) && (stateIterators.size() == 0))
					state.mainStep = ALState.MS_ENDCYCLE;
				else
					state.mainStep = ALState.MS_NEXTITER;

			} catch (SkipToException st) {
				c.stats.skip();
				log.debug("skip.to", st.getMessage());
				state.setNext(getConnectorIndex2(st.getMessage()), true);

			} catch (AbortALException aa) {
				log.info("abort.al.requested", aa.getMessage());
				throw aa;
			} catch (ExitBranchException ebe) {
				log.debug("skip.to", ebe.getMessage());
				exitBranch(c, ebe.getMessage());
			} catch (ContinueLoopException cle) {
				log.debug("skip.to", cle.getMessage());
				continueLoop(c, cle.getMessage());
			} catch (Throwable t) {
				// If this component is directly followed by an ELSE, go there.
				int i = c.getEndComponentIndex() + 1;
				if (i < stateConnectors.size() && !(c instanceof BranchingComponent)
						&& (stateConnectors.get(i) instanceof BranchingComponent)
						&& ((BranchingComponent) stateConnectors.get(i)).getBranchType() == BranchingConfig.BRANCH_ELSE)
					state.setNext(i, true);
				else
					throw t;
			}

		} finally {
			stopPerfRecording(getPerfComponent(c.getName()));
		}
	}

	/**
	 * Logs non fatal error.
	 *
	 * @param c
	 *            {@link AssemblyLineComponent}
	 * @param e
	 *            Exception to log.
	 */
	private void logskipmsg(AssemblyLineComponent c, NonFatalException e) {
		String msg = e.getMessage();
		if (msg == null || msg.equals("by script"))
			msg = "";
		else if (msg.length() > 0)
			msg = ", " + msg;

		msg = "[" + c.getName() + "]" + msg;

		if (e instanceof RestartEntryException) {
			log.debug("restarting.entry.from", msg);
		} else if (e instanceof RetryEntryException) {
			log.debug("retry.operation.in", msg);
		} else if (e instanceof IgnoreEntryException) {
			log.debug("ignore.entry.in", msg);
		} else if (e instanceof SkipEntryException) {
			log.debug("skip.entry.from", msg);
		}
	}

	/**
	 * Exit the nearest enclosing branch (if branch == null) or the explicitly
	 * named branch. We do this by scanning backwards for either the first
	 * BranchComponent (branch == null) or one with a matching name. If the
	 * branch is not found, an exception is thrown. If exitBranch is called from
	 * an entry feed or response component, the only valid branches are the
	 * predefined names "Cycle" or null (system.skipEntry) and "AssemblyLine"
	 * (system.abortAL).
	 *
	 * @param curcomp
	 *            current {@link AssemblyLineComponent} object
	 * @param branchName
	 *            name of the branch to exit.
	 * @throws Exception
	 *             if branch is not found.
	 */
	private void exitBranch(AssemblyLineComponent curcomp, String branchName) throws Exception {

		String branch = branchName;
		boolean dataFlow = true;

		// Called from an entry feed or data flow?
		switch (curcomp.getType()) {
		case ServerConstants.TYPE_REPLYCHANNEL:
		case ServerConstants.TYPE_ITERATOR:
		case ServerConstants.TYPE_SERVER:
			dataFlow = false;
		}

		log.debug("exitbranch.curcomp.branch.dataflow", new Object[] { curcomp.getName(), branch, Boolean.toString(dataFlow) });

		// Check user configured branch names (if called from data flow)
		if (dataFlow) {
			AssemblyLineComponent c = curcomp;
			while (c != null) {
				boolean match;
				if (branch == null) {
					match = (c instanceof BranchingComponent || c != curcomp);
				} else {
					match = branch.equalsIgnoreCase(c.getName());
					if (!match) {
						if (c instanceof LoopComponent)
							match = branch.equalsIgnoreCase("Loop");
						else if (c instanceof SwitchComponent)
							match = branch.equalsIgnoreCase(c.getType() == ServerConstants.TYPE_CASE ? "Case" : "Switch");
						else if (c instanceof BranchingComponent)
							match = branch.equalsIgnoreCase("Branch");
					}
				}

				if (match) {
					if (c instanceof LoopComponent || c instanceof SwitchComponent || c instanceof ALMappingConfig)
						state.setNext(c.getEndComponentIndex() + 1, true);
					else
						state.setNext(c.getEndComponentIndex(), true);

					if (state.connectorIndex < stateConnectors.size()) {
						c = stateConnectors.get(state.connectorIndex);
						log.debug("exitbranch.skipping.to", c.getName());
					} else {
						log.debug("exitbranch.exitflow");
					}
					return;
				}
				if (c.getParentIndex() >= 0)
					c = stateConnectors.get(c.getParentIndex());
				else
					c = null;
			}
		}

		// Defaults for null value
		if (branch == null)
			branch = dataFlow ? "Flow" : "Cycle";

		// Predefined names
		// * exit "Flow" --> Jump to Response section
		// * exit "AssemblyLine" --> Jump to Epilog
		// * exit "Response"/"Cycle" --> Jump to end of cycle

		if ("Cycle".equals(branch) || "Reponse".equals(branch)) {
			state.mainStep = ALState.MS_ENDCYCLE;
			return;
		} else if ("AssemblyLine".equalsIgnoreCase(branch)) {
			state.mainStep = ALState.MS_EPILOG;
			log.info("exit.assemblyline.request");
			return;
		} else if ("Flow".equalsIgnoreCase(branch) &&
				curcomp.getType() != ServerConstants.TYPE_REPLYCHANNEL) {
			for (int i = 0; i < stateConnectors.size(); i++) {
				if (stateConnectors.get(i) instanceof ReplyChannelComponent) {
					log.debug("skip.response.section");
                    state.mainStep = ALState.MS_NEXTCONN;
					state.setNext(i, true);
					return;
				}
			}
			// if we dont have any response components then goto next iterator
			state.mainStep = ALState.MS_ENDCYCLE;
			return;
		}

		String errorMessage = sResHash.getString("cannot.locate.branch.comp", new Object[] { branch, curcomp.getName() });
		throw new Exception(errorMessage);
	}

	/**
	 * Continue the nearest enclosing loop (if name == null) or the explicitly
	 * named loop. We do this by scanning backwards for either the first
	 * LoopComponent (branch == null) or one with a matching name. If the loop
	 * is not found, an exception is thrown.
	 *
	 * @param curcomp
	 *            current {@link AssemblyLineComponent} object.
	 * @param name
	 *            name of the loop to exit.
	 * @throws Exception
	 *             if the loop is not found.
	 */
	private void continueLoop(AssemblyLineComponent curcomp, String name) throws Exception {
		AssemblyLineComponent c = curcomp;
		while (c != null) {
			if ((c instanceof LoopComponent) && (name == null || name.equalsIgnoreCase(c.getName()))) {
				state.setNext(c.getEndComponentIndex(), true);
				return;
			}

			if (c.getParentIndex() >= 0)
				c = stateConnectors.get(c.getParentIndex());
			else
				c = null;
		}

		String errorMessage = sResHash.getString("cannot.locate.loop.comp", new Object[] { name, curcomp.getName() });
		throw new Exception(errorMessage);
	}

	/**
	 * This method is called to end current cycle.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msEndCycle() throws Exception {

		if (meta != null && tcb != null) {
			tcb.accumulateEntry(meta);
		}

		final AssemblyLine al = this;
		eventSource.visitListeners(new ThreadSafeListenableImpl.ThrowingVisitor<AssemblyLineListener>() {
			public void visit(AssemblyLineListener listener) throws Exception {
				listener.assemblyLineCycleEnded(al, meta);
			}
		});

		if (state.iteratorIndex < stateIterators.size()) {
			AssemblyLineComponent alc = stateIterators.get(state.iteratorIndex);

			if (alc.connector instanceof com.ibm.di.connector.ChangelogInterface) {
				com.ibm.di.connector.ChangelogInterface chlog = (com.ibm.di.connector.ChangelogInterface) alc.connector;
				if (chlog.getStateKeySaveMethod() == com.ibm.di.connector.ChangelogInterface.SAVE_STATE_END_OF_CYCLE) {
					chlog.saveStateKey();
				}
			}

			if (alc.connector instanceof com.ibm.di.connector.MemQConnector) {
				com.ibm.di.connector.MemQConnector memqConnector = (com.ibm.di.connector.MemQConnector) alc.connector;
				// Release lock on memq object at end of AL cycle
				if (memqConnector.isReleaseOnALEnd())
					memqConnector.releaseLock();
			}

			if (alc instanceof CSDeltaTaskComponent) {
				((CSDeltaTaskComponent) alc).commitOnEndIter();
			}
		}
		for (AssemblyLineComponent alcc : stateConnectors) {
			if (alcc.connector instanceof com.ibm.di.connector.JDBCConnector) {
				com.ibm.di.connector.JDBCConnector jdbcConnector = (com.ibm.di.connector.JDBCConnector) alcc.connector;
				if (jdbcConnector.isEOCflag())
					jdbcConnector.commit();
			}

			if (alcc.connector instanceof com.ibm.di.connector.PESConnector) {
				com.ibm.di.connector.PESConnector pesconnector = (com.ibm.di.connector.PESConnector) alcc.connector;
				if (pesconnector.isEOCFlag())
					pesconnector.commit();
			}

		}

		state.mainStep = ALState.MS_NEXTITER;
		state.cycleCounter++;

	}

	/**
	 * This method is called when the AL has exhausted data from one iterator
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msEndIter() throws Exception {
		int iterSize = stateIterators.size();

		// switch to next iterator and try again
		state.iteratorIndex++;

		if (state.iteratorIndex < iterSize) {
			log.info("switch.next.iterator");
			state.mainStep = ALState.MS_NEXTITER;
		} else {
			stopPerfRecording(getPerfComponent(ALState.MS_BEGINITER));
			log.info("end.iteration");
			state.mainStep = ALState.MS_EPILOG;
		}
	}

	/**
	 * Executes AL epilog step.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msEpilog() throws Exception {
		runEpilog();
		state.mainStep++;
	}

	/**
	 * Builds {@link TaskCallBlock} object
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msBuildTCB() throws Exception {
		if (getRunMode() != RUNMODE_I_MANUAL)
			exitTCB(tcb);
		state.mainStep++;
	}

	/**
	 * Terminates all connections.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msCloseConn() throws Exception {
		close();
		state.mainStep++;
	}

	/**
	 * Executes epilog after close.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msEpilog2() throws Exception {
		runEpilog2();
		state.mainStep++;
	}

	/**
	 * Terminates execution of the AL.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void msTerminate() throws Exception {
		// Log statistics
		logStats();

		// Log performance statistics
		logPerfStats();

		// Report status
		Exception eperror = stats.getError();

		Log plog = parent instanceof RS ? ((RS) parent).getLog() : null;

		if (eperror != null) {
			runOnFailure();
			String reason = eperror.getMessage() != null ? eperror.getMessage() : eperror.toString();
			log.error("failed.because", reason);
			if (!(eperror instanceof AbortALException) && se.lastException(eperror) != null) {
				String errorMessage = sResHash.getString("msterminate.error.caused", se.lastException(eperror).toString());
				log.logerror(errorMessage);
			}
			if (plog != null)
				plog.error("assemblyline.failed.because", getName(), reason);
		} else {
			runOnSuccess();
			stopPerfRecording(getPerfComponent(0));
			log.info("terminated.successfully", Integer.valueOf(stats.err));
			if (plog != null)
				plog.info("assemblyline.terminated.success", getName());
		}
		disableDebug();

		if (parent instanceof RS)
			((RS) parent).invokeServerHook("TDI_ALStopped", parent, this);

		/*
		 * Notify before cleanup, so that AL internals are available to
		 * listeners
		 */
		notifyTerminated();

		// Cleanup & Close
		cleanup(eperror);

		// Good bye!
		if (runMode != RUNMODE_I_MANUAL) {
			threadStopped(this, null, eperror);
		}

		state.mainStep++;

		// Tell any process waiting for us that we are finished.
		shutdownLatch.countDown();
	}

	/**
	 * @return null
	 *
	 * @throws Exception
	 *             never
	 * @deprecated
	 */
	@Deprecated
	public Entry getUserCheckpoint() throws Exception {
		return null;
	}

	/**
	 * @return null
	 * @param cp
	 *            not used
	 * @throws Exception
	 *             never
	 * @deprecated
	 */
	@Deprecated
	public Exception setUserCheckpoint(Entry cp) throws Exception {
		return null;
	}

	/**
	 * This method sets the runMode and must be called before the thread is
	 * started.
	 *
	 * @param runMode
	 *            0 = Normal, 1 = Record, 2 = Playback, 3 = Manual
	 */
	private void setRunMode(int runMode) {
		this.runMode = runMode;
	}

	/**
	 * Returns the runMode for the AL.
	 *
	 * @return the runMode index.
	 *
	 */
	public int getRunMode() {
		return runMode;
	}

	/**
	 * @return false
	 *
	 * @deprecated
	 */
	@Deprecated
	public boolean getResetCheckpoint() {
		return false;
	}

	/**
	 * This method does nothing
	 *
	 * @param resetCheckpointStore
	 *            not used
	 *
	 * @deprecated
	 */
	@Deprecated
	public void setResetCheckpoint(boolean resetCheckpointStore) {
	}

	/**
	 * Checks if the runmode is record mode.
	 *
	 * @return true if runMode == {@link #RUNMODE_I_RECORD}.
	 */
	public boolean isRecording() {
		return (runMode == RUNMODE_I_RECORD);
	}

	/**
	 * Checks if the runmode is playback mode;
	 *
	 * @return true if runMode == {@link #RUNMODE_I_PLAYBACK}.
	 */
	public boolean isPlaying() {
		return (runMode == RUNMODE_I_PLAYBACK);
	}

	/**
	 * @return true if the object <code>obj</code> should be recorded.
	 *
	 * @param obj
	 *            the object to check
	 */
	public boolean isRecording(Object obj) {
		if (!isRecording())
			return false;

		if (obj instanceof ConnectorInterface) {
			return ((ConnectorConfig) ((ConnectorInterface) obj).getConfiguration()).getSandboxConfig().getRecordEnabled();
		} else if (obj instanceof FunctionInterface) {
			return ((FunctionConfig) ((FunctionInterface) obj).getConfiguration().getParent()).getSandboxConfig()
			.getRecordEnabled();
		}

		return false;
	}

	/**
	 * @return true if the object <i>obj</i> should be played back from the
	 *         server store.
	 *
	 * @param obj
	 *            the object to check
	 */
	public boolean isPlaying(Object obj) {
		if (!isPlaying())
			return false;

		if (obj instanceof ConnectorInterface) {
			return ((ConnectorConfig) ((ConnectorInterface) obj).getConfiguration()).getSandboxConfig().getPlaybackEnabled();
		} else if (obj instanceof FunctionInterface) {
			return ((FunctionConfig) ((FunctionInterface) obj).getConfiguration().getParent()).getSandboxConfig()
			.getPlaybackEnabled();
		}

		return false;
	}

	/**
	 * @return the database path/url where obj is recorded/played back.
	 */
	public String getDatabase() {
		return config.getSandboxConfig().getIdentifier();
	}

	/**
	 * This method returns the short name of this AL.
	 *
	 * @return The name without the "AssemblyLines/" prefix.
	 */
	public String getShortName() {
		if (tcb.getTaskName() != null)
			return tcb.getTaskName();
		return config.getShortName();
	}

	/**
	 * Used if there is a Server Mode Connector. Create an AssemblyLinePool and
	 * use this for executing
	 */
	public void executeWithALPool() {

		try {
			int dupentries = parseIntParam("findreturncount");
			if (dupentries < 1)
				dupentries = 10;

			executeInitializeAL();
			
			if (se == null) {
				initScriptEngine();
				autoIncludeScripts();
			}

			// TODO: This is probably not needed?
			initConnectors();

			if (mSimulating) {
				config.setBooleanParameter(InternalSchema.AL_SIMULATE_MODE, mSimulating);
			}

			// Drain Iterators
			List<BaseConfiguration> list = config.getEntryFeedComponents().getConfigurations(null);
			for (BaseConfiguration bc : list) {
				ConnectorConfig cc = (ConnectorConfig) bc;
				if (!cc.getEnabled())
					continue;

				// Run Iterators in a separate thread
				if (ConnectorConfig.ITERATOR_MODE.equals(cc.getMode())) {
					log.debug("exec.alpool.connector.draining", cc.getShortName());
					Thread t = alPool.startThread(cc.getShortName(), cc);
					t.join();
					log.debug("exec.alpool.connector.drained", cc.getShortName());
				} else {
					AssemblyLineComponent c = null;
					try {
						log.debug("exec.alpool.connector.draining", cc.getShortName());
						c = loadConnector(cc, dupentries);
						if (c != null) {
							if (tcb != null)
								enterTCB(tcb);
							c.initialize();
							se.declareStaticBean(c.getName(), c);
							mActiveServerConnector = c.connector;
							drainConnector(c, alPool);
							mActiveServerConnector = null;
						} else {
							se.declareStaticBean(cc.getShortName(), null);
						}
					} finally {
						log.debug("exec.alpool.connector.drained", cc.getShortName());
						if (c != null) {
							c.close();
							connectors.remove(c);
						}
					}
				}
			}

		} catch (Throwable error) {
			log.error("exec.alpool.problem", error);
			stats.exception(error);
			stats.err();
			if (alPool != null)
				alPool.terminate();
		}

		// Poll ALP for completion
		try {
			log.info("wait.terminate.alpool");
			if (getTerminationRequested() && alPool != null)
				alPool.terminate();
			while (alPool != null && alPool.hasRunningThreads()) {
				Thread.sleep(5000);
			}
			log.info("terminated.successfully", Integer.valueOf(stats.err));
		} catch (Exception ignore) {
		}

		if (runMode != RUNMODE_I_MANUAL) {
			threadStopped(this, null, null);
		}

		notifyTerminated();
		cleanup(stats.ex);
	}

	/**
	 * Drains entries through the connector.
	 *
	 * @param c
	 *            Connector object
	 * @param p
	 *            AssemblyLinePool used for executing , if connector is in
	 *            server mode
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void drainConnector(AssemblyLineComponent c, AssemblyLinePool p) throws Exception {
		try {
			Entry e = null;
			ConnectorInterface ci = null;
			boolean entrymode = (c.getType() != ServerConstants.TYPE_SERVER);
			log.debug("drain.connector.entrymode", Boolean.toString(entrymode));

			while (true) {
				if (entrymode) {
					e = c.getnext();
					p.executeEvent(this, e, false);
				} else {
					try {
						ci = c.getnextClient();
					} catch (RetryEntryException ex) {
						// a recoverable error occurred and reconnect was
						// performed, so try again with the next client
						continue;
					}
					if (ci != null) {
						ci.setParam(TCB_FORCE_REPLYCHANNEL, "true");
						p.startThread(ci.getName(), ci);
					}
				}
				if (e == null && ci == null)
					break;
			}
		} finally {
			p.releaseAssemblyLine(this);
		}
	}

	/**
	 *
	 * Sets the flag that tells whether the Feed Components should be loaded and
	 * used during the AL cycle.
	 *
	 * @param value
	 *            if this is true then the Input Components will be ignored,
	 *            otherwise they will be used.
	 */
	public void setIgnoreInputComponents(boolean value) {
		ignoreInputComponents = value;
	}

	/**
	 * @see ALState
	 * @return the ID of the step the AL is currently in.
	 */
	public int getCurrentStep() {
		if (state != null) {
			return state.mainStep;
		} else {
			return ALState.MS_NOT_INITIALIZED;
		}
	}

	/**
	 * Sets the next connector to execute.
	 *
	 * @param alc
	 *            The connector to execute
	 *
	 * @return Returns true if the next connector to execute is set. Otherwise,
	 *         false is returned.
	 */
	public boolean setNextConnector(AssemblyLineComponent alc) {
		return setNextConnector(alc, false);
	}

	/**
	 * @deprecated
	 */
	public boolean setNextConnector(AssemblyLineComponent alc, boolean continueAfter) {
		int index = continueAfter ? alc.getEndComponentIndex() + 1 : stateConnectors.indexOf(alc);
		if (index < 0)
			return false;
		state.setNext(index, true);
		return true;
	}

	/**
	 * Gets a cone of the configuration object of this AssemblyLine
	 *
	 * @return the clone, or null
	 *
	 * @throws Exception
	 *             if an error occurs while the cloning process.
	 */
	public AssemblyLineConfig getConfigClone() throws Exception {
		if (config == null) {
			return null;
		}
		return (AssemblyLineConfig) config.getClone();
	}

	/**
	 * Sets a custom user message, used when storing TombStones.
	 *
	 * @param aUserMessage
	 *            message to set.
	 */
	public void setTombstoneUserMessage(String aUserMessage) {
		tombstoneUserMessage = aUserMessage;
	}

	/**
	 * Retrieves a custom user message, used when storing TombStones.
	 *
	 * @return the tombstone message.
	 */
	public String getTombstoneUserMessage() {
		return tombstoneUserMessage;
	}

	/**
	 * Retrieves operational entry.
	 *
	 * @return the operational entry
	 */
	public Entry getOpEntry() {
		return opentry;
	}

	/**
	 * This method returns the unique section code from where the performance
	 * message is coming in eg: al1.XXXXXX.prolog. If performance recording is
	 * disabled an empty string is returned.
	 *
	 * @param state
	 *            index of the state
	 *
	 * @return The AssemblyLineSection
	 */
	private String getPerfComponent(int state) {
		if (perfEnabled) {
			if ((state >= 1) || (state < (ALState.MAIN_STEPS.length - 1)))
				return config.getShortName() + "." + hashCode() + "." + ALState.MAIN_STEPS[state];
			return config.getShortName() + "." + hashCode();
		} else {
			return "";
		}
	}

	/**
	 * This method returns the unique section code from where the performance
	 * message is coming in eg: al1.XXXXXX.prolog. If performance recording is
	 * disabled an empty string is returned.
	 *
	 * @param section
	 *            : section name
	 *
	 * @return The AssemblyLineSection
	 */
	private String getPerfComponent(String section) {
		if (perfEnabled) {
			return config.getShortName() + "." + hashCode() + "." + section;
		} else {
			return "";
		}
	}

	/**
	 * This method returns the unique section code from where the performance
	 * message is coming in eg: al1.XXXXXX.prolog. If performance recording is
	 * disabled an empty string is returned.
	 *
	 * @param state
	 *            index of the state
	 * @param section
	 *            : section name
	 *
	 * @return The AssemblyLineSection
	 */
	private String getPerfComponent(int state, String section) {
		if (perfEnabled) {
			if ((state >= 1) || (state < (ALState.MAIN_STEPS.length - 1)))
				return config.getShortName() + "." + hashCode() + "." + ALState.MAIN_STEPS[state] + "." + section;
			return config.getShortName() + "." + hashCode() + "." + section;
		} else {
			return "";
		}
	}

	/**
	 * Starts Performance Recording
	 *
	 * @param component
	 *            Unique ComponentID
	 */
	public void startPerfRecording(String component) {
		if (perfEnabled)
			perfSt.startPerfRecording(component);
	}

	/**
	 * Stops Performance Recording
	 *
	 * @param component
	 *            Unique ComponentID
	 * @return The performance statistics for the component.
	 */
	public String stopPerfRecording(String component) {
		if (perfEnabled) {
			String str = perfSt.stopPerfRecording(component);
			if (!component.equals(getPerfComponent(0))) {
				log.debug("perf.stats.performance", str);
			} else {
				log.info("perf.stats.performance", str);
			}
			return str;
		}
		return "";
	}

	/**
	 *
	 * Gets the performance statistics for a component.
	 *
	 * @param component
	 *            The name of the component to get the performance statistics
	 *            for
	 * @return A String containing the performance statistics of the specified
	 *         component
	 */
	public String getPerfStats(String component) {
		if (perfEnabled)
			return perfSt.getPerfStats(component);
		return "";
	}

	/**
	 * Dumps a formatted message to the log file the contents of a performance
	 * entry.
	 *
	 * @see #dump(Object)
	 */
	public void logPerfStats() {
		if (perfEnabled) {
			log.info("Performance.entry.dump");
			Enumeration<PerfEntry> enum1 = perfSt.getAllResultStats();
			while (enum1.hasMoreElements()) {
				PerfEntry entry = enum1.nextElement();
				// Skip the total assemblyline entry
				if (entry.getBaseComponent().equals(getPerfComponent(0)))
					continue;
				log.info("perf.stats.performance", entry.dumpEntry());
			}
			log.info("Performance.entry.dump.end");
		}
	}

	/**
	 * Enable the performance statistics recording.
	 */
	public void setPerfEnabled() {
		this.perfEnabled = true;
	}

	/**
	 * Sets a operation from connector to the entry as attribute.
	 *
	 * @param e
	 *            {@link Entry}
	 * @param alc
	 *            {@link AssemblyLineComponent}
	 */
	private void setOperationFromConnector(Entry e, AssemblyLineComponent alc) {
		ConnectorConfig cc = alc.getConfiguration();
		if (cc == null)
			return;

		if (cc.getOperationCarrier() == null)
			return;

		Object oper = null;
		if (cc.getOperationCarrierIsProperty())
			oper = e.getProperty(cc.getOperationCarrier());
		else
			oper = e.getString(cc.getOperationCarrier());

		opentry.setAttribute(OPENTRY_OPERATION, oper);
	}

	/**
	 * Return the {@link ALState}, for use when reconnecting a connector
	 *
	 * @return state of the AssemblyLine
	 */
	ALState getAlState() {
		return state;
	}

	/**
	 * Returns the TaskCallBlock for this AssemblyLine.
	 *
	 * @return the TaskCallBlock for this AssemblyLine.
	 * @since 7.0
	 */
	public TaskCallBlock getTCB() {
		return tcb;
	}

	/**
	 * Define the simulation behaviour of the AL.
	 *
	 * @param mSimulating
	 * <br />
	 *            + <b>true</b> - turn simulation on.<br />
	 *            + <b>false</b> - turn simulation off. <br />
	 * @since 7.0
	 */
	public void setSimulating(boolean mSimulating) {
		this.mSimulating = mSimulating;
	}

	/**
	 * Retrieves flag for AL's simulation.
	 *
	 * @return boolean <br />
	 *         + <b>true</b> - the simulation is on.<br />
	 *         + <b>false</b> - the simulation is off. <br />
	 * @since 7.0
	 */
	public boolean isSimulating() {
		return mSimulating;
	}

	/**
	 * @return the SimulationConfig for this AL.
	 * @since 7.0
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SimulationConfig getSimulationConfig() throws Exception {
		return config.getSimulationConfig();
	}

	/**
	 * The key name of the java system property used for the AutoDebug
	 */
	public static final String AUTODEBUG_PREFIX = "com.ibm.tdi.autodebug";

	/**
	 * This method will activate the AL debugger based on java system
	 * properties.
	 *
	 * <pre>
	 * &quot;com.ibm.tdi.autodebug&quot; is the prefix.
	 * &lt;id&gt; is the configuration id (of the running config instance)
	 * &lt;name&gt; is the name of the assembly line (short name)
	 *
	 * 		// Match specific assemblyline in specific config instance
	 * 		prefix.&lt;id&gt;.&lt;name&gt;  = host,port,break-on-error
	 *
	 * 		// Match all assemblylines in specific config instance
	 * 		prefix.&lt;id&gt; 		= host,port,break-on-error
	 *
	 * 		// Match ALL assemblylines in ALL config instances
	 * 		prefix				= host,port,break-on-error
	 * </pre>
	 *
	 * @return true if a new debugger session is activated, false otherwise
	 */
	public boolean autoActivateDebugger() throws Exception {

		if (debugger != null || getRunMode() == RUNMODE_I_NODEBUG)
			return false;

		String id = APIEngine.getConfigId(parent);
		String name = config.getShortName();

		// First try "id.name" match.
		String connection = getAutodebugProperty(id + "." + name);
		if (connection == null) {
			// Second try "id" match
			connection = getAutodebugProperty(id);
			if (connection == null) {
				// Third try prefix match
				connection = getAutodebugProperty(null);
			}
		}

		// If we have a connection string enable debugger
		if (connection != null) {
			String[] arr = connection.split(",");
			if (arr.length == 3) {
				enableDebug(Integer.valueOf(arr[1]).intValue(), arr[0], Boolean.valueOf(arr[2]).booleanValue());
			} else {
				parent.logmsg("Expected 'host,port,break-on-error' value got '" + connection + "'");
			}
		}

		return (debuggerEnabled());
	}

	/**
	 * Retrieves the values of the java system property used for AutoDebug.
	 *
	 * @param suffix
	 *            the suffix for the property.
	 * @return value of the property
	 */
	private String getAutodebugProperty(String suffix) {
		String value = System.getProperty(AUTODEBUG_PREFIX + (suffix == null ? "" : "." + suffix));
		if (value == null || value.length() == 0)
			return null;
		else
			return value;
	}

	/**
	 * <p>
	 * This method is for internal use only. Users must not rely on it.
	 * </p>
	 *
	 * <p>
	 * Register a listener for AssemblyLine events. Can be called by other
	 * threads.
	 * </p>
	 *
	 * @param listener
	 *            A listener.
	 * @since 7.0
	 */
	public void addListener(AssemblyLine.AssemblyLineListener listener) {
		eventSource.addListener(listener);
	}

	/**
	 * <p>
	 * This method is for internal use only. Users must not rely on it.
	 * </p>
	 *
	 * <p>
	 * Unregister a listener for AssemblyLine events. Can be called by other
	 * threads.
	 * </p>
	 *
	 * <p>
	 * Beware that the listener may get notified a few times after it was
	 * unregistered. The only way to ensure this does not happen is to
	 * unregister the listener on the AssemblyLine thread, e.g. in a listener
	 * methods.
	 * </p>
	 *
	 * @param listener
	 *            The listener object.
	 * @return
	 * @since 7.0
	 */
	public AssemblyLineListener removeListener(AssemblyLine.AssemblyLineListener listener) {
		return eventSource.removeListener(listener);
	}

	/**
	 * Add a new debugger.
	 *
	 * @param d
	 *            Debugger.
	 * @throws Exception
	 *             If the initialization of the debugger fails.
	 */
	private void addDebugger(DebugServer d) throws Exception {

		if (d == null) {
			return;
		}

		d.setLog(getLog());
		d.setTask(this);
		if (getScriptEngine() != null) {
			d.setScriptEngine(getScriptEngine());
		}

		// Set the identifier of the config instance and this al instance
		// This is used by the CE to identify which particular instance of the
		// AL is debugged.
		d.setUniqueID(getParent().getName() + ":" + hashCode());

		debugger = d;

		/*
		 * If the connectors are already loaded, initialized the debugger,
		 * otherwise leave it to the MS_LOADCONN step.
		 */
		if (getCurrentStep() > ALState.MS_LOADCONN) {
			d.debugInit();
		}
	}

	/**
	 * Remove the current debugger.
	 * For internal use, to stop debugging.
	 */
	public void removeDebugger() {

		if (debugger == null) {
			return;
		}

		debugger.debugClose(getName());
		debugger = null;
	}

	/**
	 * Query the debug mode of a component from this AssemblyLine. May be called
	 * by different threads.
	 *
	 * @param componentName
	 *            The name of a component as it appears in the configuration.
	 * @return the debug mode of the component
	 * @throws Exception
	 *             If the component name is invalid.
	 */
	public boolean getComponentDebugMode(String componentName) throws Exception {
		boolean isDebugMode = false;
		if (alPool == null) {

			synchronized (componentDebugModes) {
				if (componentName == null || !componentDebugModes.containsKey(componentName)) {
					throw new Exception(sResHash.getString("AL.GET.COMPONENT.DEBUG.INVALID.COMPONENT.NAME", new Object[] {
							componentName, getName() }));
				}

				AssemblyLineComponent alc = getConnector(componentName);
				if (alc != null) {
					isDebugMode = alc.getDebug();
				} else {
					isDebugMode = componentDebugModes.get(componentName);
				}
			}
		} else {
			isDebugMode = alPool.getComponentDebugMode(componentName);
		}
		return isDebugMode;
	}

	/**
	 * Modify the debug mode of a component from this AssemblyLine. May be
	 * called by different threads.
	 *
	 * @param componentName
	 *            The name of a component as it appears in the configuration.
	 * @param debug
	 *            The new debug mode of the component.
	 * @throws Exception
	 *             If the component name is invalid.
	 */
	public void setComponentDebugMode(String componentName, boolean debug) throws Exception {
		if (alPool == null) {

			synchronized (componentDebugModes) {
				if (componentName == null || !componentDebugModes.containsKey(componentName)) {
					throw new Exception(sResHash.getString("AL.SET.COMPONENT.DEBUG.INVALID.COMPONENT.NAME", new Object[] {
							componentName, getName() }));
				}
				componentDebugModes.put(componentName, debug);
				AssemblyLineComponent alc = getConnector(componentName);
				if (alc != null) {
					alc.setDebug(debug);
				}
			}
		} else {
			alPool.setComponentDebugMode(componentName, debug);
		}
	}

	/**
	 * Collect the debug mode settings for all Connectors and Function
	 * Components in the specified AssemblyLine configuration.
	 *
	 * @param alConfig
	 *            AssemblyLine configuration
	 * @return Mapping between the names of the components and their debug mode.
	 * @throws Exception
	 *             If an error occurs while analyzing the configuration object.
	 */
	private static Map<String, Boolean> getComponentDebugModes(AssemblyLineConfig alConfig) throws Exception {

		Map<String, Boolean> res = new Hashtable<String, Boolean>();

		// loop through the Connectors and Function Components only
		for (int i = 0; i < alConfig.getConnectorCount(); ++i) {
			ConnectorConfig cc = alConfig.getConnector(i);
			String componentName = cc.getShortName();
			boolean debug = AssemblyLineComponent.getComponentDebugMode(cc);
			res.put(componentName, debug);
		}
		return res;
	}

	/**
	 * Batch variant of {@link #setComponentDebugMode(String, boolean)}. May be
	 * called by different threads.
	 *
	 * @param debugModes
	 *            Mapping of component name to debug mode.
	 * @throws Exception
	 *             If a component name is invalid.
	 */
	void setComponentDebugModes(Map<String, Boolean> debugModes) throws Exception {
		for (Map.Entry<String, Boolean> e : debugModes.entrySet()) {
			setComponentDebugMode(e.getKey(), e.getValue());
		}
	}

	/**
	 * Submit an asynchronous command. It will get executed by the AssemblyLine
	 * on its next step.
	 *
	 * @param cmd
	 *            Asynchronous command.
	 */
	private void submitCommand(AssemblyLineCommand cmd) {
		synchronized (commands) {
			commands.add(cmd);
		}
	}

	/**
	 * Execute all accumulated asynchronous commands and clear the command list.
	 *
	 * @throws Exception
	 *             If some command throws and exception.
	 */
	private void executeAvailableCommands() throws Exception {
		synchronized (commands) {
			if (commands.size() > 0) {
				try {
					for (AssemblyLineCommand cmd : commands) {
						cmd.execute();
					}
				} finally {
					commands.clear();
				}
			}
		}
	}

	/**
	 * Dumps the state of the AssemblyLine to the log or the output file
	 * specified.
	 *
	 * @param output
	 *            Output file name or null. If null the log is used to print out
	 *            the dump.
	 * @param append
	 *            In case a file is specified setting this to true will append
	 *            the dump to the file
	 */
	public void dumpAssemblyLineState(String file, boolean append) {
		StringBuffer buf = new StringBuffer();
		buf.append("----------------------------------------------------------------------------\n");
		buf.append("Dump AssemblyLine State\n");
		buf.append("-- AssemblyLine Name: " + getName() + "\n");
		buf.append("-- AssemblyLine ID: " + getId() + "\n");
		buf.append("-- Date: " + new java.util.Date().toString() + "\n");
		buf.append("\n");

		buf.append("[TaskCallBlock]\n");
		buf.append(tcb == null ? "(null)" : tcb.toDeltaString());
		buf.append("\n\n");

		buf.append("[JavaScript Engine Variables]\n");
		if (se != null) {
			FBSGlobalObject g = se.getJsengine().getGlobalObject();
			for (Iterator<String> i = g.getPropertyKeys(); i.hasNext();) {
				String prop = i.next();
				Object value = null;
				IValue iv = null;
				try {
					iv = g.getProperty(prop);
					if (iv != null && !(iv instanceof FBSNull)) {
						value = iv.toJavaObject();
					}
				} catch (Exception e) {
					value = e;
				}

				if (value == null)
					value = iv; // fall back, maybe use another method?

				buf.append(prop + ": " + value + "\n");
			}
		} else {
			buf.append("(null)");
		}
		buf.append("\n\n");

		if (file == null || file.trim().length() == 0) {
			logmsg(buf.toString());
		} else {
			FileWriter writer = null;
			try {
				File target = new File(file);

				// -- make sure the parent directory exists
				if (target.getParentFile() != null && !target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
					SystemFunctions.doNothing();
				}

				writer = new FileWriter(new File(file), append);
				writer.write(buf.toString());
			} catch (Exception err) {
				logmsg(err.toString());
				logmsg(buf.toString());
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Exception ex) {
						logmsg(ex.toString());
					}
				}
			}
		}
	}

	/**
	 * @return The underlying configuration object of this AssemblyLine. Do not
	 *         modify this object while the AssemblyLine is alive.
	 * @since 7.0
	 */
	AssemblyLineConfig getInternalConfig() {
		return config;
	}

	/**
	 * Returns the AssemblyLine Thread that started this AssemblyLine. If the
	 * parent AssemblyLine is no longer running, null may be returned.
	 *
	 * @since 7.1
	 */
	public AssemblyLine getParentAL() {
		return parentAL != null ? parentAL.get() : null;
	}

	/**
	 * Returns the Scheduler that started this AssemblyLine. If the
	 * Scheduler is no longer running, null may be returned.
	 *
	 * @since 7.2.0.1
	 */
	public Scheduler getScheduler() {
		return scheduler != null ? scheduler.get() : null;
	}
	
	/**
	 * Returns the hash value for the Thread that started this AL.
	 * @since 7.2.0.3
	 */
	public int getParentHash() {
		return parentHash;
	}

	/**
	 * Notifies listeners that the AL has started
	 */
	void notifyStarted() {
		eventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<AssemblyLineListener>() {
			public void visit(AssemblyLineListener listener) {
				listener.assemblyLineStarted(AssemblyLine.this);
			}
		});
	}

	/**
	 * Notifies listeners that the AL has terminated
	 */
	private void notifyTerminated() {
		eventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<AssemblyLineListener>() {
			public void visit(AssemblyLineListener listener) {
				listener.assemblyLineTerminated(AssemblyLine.this);
			}
		});
	}
	
	/**
	 * return the current assemblyline pool
	 * @return AssemblyLinePool object
	 * @since 7.1.1
	 */
	public AssemblyLinePool getALPool() {
		return this.alPool;
	}

	/**
	 * return the current debugger, if any
	 * @return
	 * @since 7.2
	 */
	public DebugServer getDebugger() {
		return debugger;
	}

	// Code to handle read and write of regression info
	private BufferedReader regressionReader;

	private BufferedWriter regressionWriter;

	private boolean writeWork;
	private boolean readWork;
	private boolean skipWork;

	private static final String INCLUDES_WORK = "work=true";
	private static final String DONT_INCLUDE_WORK = "work=false";

	private void openRegressionInfo() {
		if (tcb == null)
			return;
		String outFileName = tcb.getRegressionOutputName();
		if ( outFileName != null ) {
			try {
				regressionWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8"));
			} catch (Exception e) {
				log.error("AssemblyLine.cannot.read.regression", e.toString());
			}
		}

		String inFileName = tcb.getRegressionInputName();
		if ( inFileName != null && ! inFileName.equals(outFileName)) {
			try {
				regressionReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName), "UTF-8"));
			} catch (Exception e) {
				log.error("AssemblyLine.cannot.write.regression", e.toString());
			}
		}

		if (regressionReader == null && regressionWriter == null)
			return;	

		if (regressionWriter != null) {
			writeRegressionLine(getName());
			writeWork = !tcb.getRegressionIgnoreWork();
			writeRegressionLine(writeWork ? INCLUDES_WORK : DONT_INCLUDE_WORK );
		}

		if (regressionReader != null) {
			String line = readRegressionLine();
			if (line != null && ! line.equals(getName())) {
				log.warn("AssemblyLine.regression.not.this");
				closeRegressionReader();
			}
			line = readRegressionLine();
			if (INCLUDES_WORK.equals(line)) {
				if (tcb.getRegressionIgnoreWork())
					skipWork = true;
				else
					readWork = true;
			} else if (DONT_INCLUDE_WORK.equals(line)) {
				readWork = false;
			} else if (line != null){
				log.warn("AssemblyLine.regression.not.this");
				closeRegressionReader();
			}
		}
	}

	private void writeRegressionInfo(AssemblyLineComponent alc, Entry work) {
		if (regressionWriter != null) {
			if (writeWork)
				writeReg(alc.name, "work", work);
			writeReg(alc.name, "conn", (Entry) alc.get(AssemblyLineComponent.LAST_CONN));
		}

		if (regressionReader != null) {
			if (skipWork)
				doSkipWork();
			else if (readWork)
				readReg(alc.name, "work", work);

			readReg(alc.name, "conn", (Entry) alc.get(AssemblyLineComponent.LAST_CONN));		
		}	
	}

	private void writeReg(String connName, String name, Entry e) {
		String info = "@" + connName + ": " + name + "=" + (e == null ? "null" : e.toDeltaString()) + "\n@";
		writeRegressionLine(info);
	}

	private void readReg(String connName, String name, Entry e) {
		String info = "@" + connName + ": " + name + "=" + (e == null ? "null" : e.toDeltaString()) + "\n@";
		for (String s: info.split("\n")) {
			String line = readRegressionLine();
			if (line == null)
				return;
			if (! s.equals(line)) {
				log.warn("AssemblyLine.regression.not.same", new Object[] {
						name, connName, state.cycleCounter+1, line, s });
				while (line != null && !line.equals("@")) {
					line = readRegressionLine();
				}
				break;
			}
		}
	}

	private void doSkipWork() {
		String line;
		do {
			line = readRegressionLine();
		} while (line != null && !line.equals("@"));
	}

	private void writeRegressionLine(String out) {
		if (regressionWriter != null) {
			try {
				regressionWriter.write(out + "\n");
			} catch (IOException err) {
				log.error("AssemblyLine.cannot.write.regression", err.toString());
				try {
					regressionWriter.close();
				} catch (IOException e2) {
					SystemFunctions.doNothing();
				}
				regressionWriter = null;
			}
		}
	}

	private String readRegressionLine() {
		if (regressionReader == null)
			return null;
		try {
			String line = regressionReader.readLine();
			if (line == null) {
				log.warn("AssemblyLine.regression.premature", state.cycleCounter+1);
				closeRegressionReader();
			}
			return line;
		} catch (IOException err) {
			log.error("AssemblyLine.cannot.read.regression", err.toString());
			closeRegressionReader();
			return null;
		}
	}

	private void closeRegressionReader() {
		try {
			if (regressionReader != null)
				regressionReader.close();
		} catch (IOException err) {
			SystemFunctions.doNothing();
		}
		regressionReader = null;	
	}

	private void closeRegInfo() {
		if (regressionWriter != null) {
			try {
				regressionWriter.close();
			} catch (IOException err) {
				log.error("AssemblyLine.cannot.write.regression", err.toString());
			}
			regressionWriter = null;
		}
		if (regressionReader != null) {
			try {
				String s = regressionReader.readLine();
				if (s != null)
					log.warn("AssemblyLine.regression.continued", state.cycleCounter);
			} catch (IOException err) {
				SystemFunctions.doNothing();
			}
			closeRegressionReader();
		}	
	}
}
