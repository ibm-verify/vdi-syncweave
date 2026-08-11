/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

/*import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
*/
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.net.ServerSocketFactory;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.InstanceConfig;
import com.ibm.di.config.interfaces.LibraryConfig;
import com.ibm.di.config.interfaces.LogConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.NamespaceConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.PoolDefConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Entry;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.log.LogInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.security.Crypto;
import com.ibm.di.security.CryptoFactory;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.systemqueue.SystemQueueEngine;
import com.ibm.di.util.FileUtils;
import com.ibm.di.util.PropertiesFile;
import com.ibm.icu.util.StringTokenizer;

/**
 * This is the main class for the TDI Server, and represents the primary thread
 * from which all others are launched, including AssemblyLines and Server mode
 * listeners. The pre-registered script variable main gives you JavaScript
 * access to the server-level methods available in this class.
 * 
 * For example, if you want to launch a new AssemblyLine then you use the main
 * variable to do so:
 * 
 * <pre>
 * // Start my AL
 * var al = main.startAL(&quot;myAssemblyLine&quot;);
 * // Wait for the AL to complete 
 * al.join();
 * </pre>
 * 
 * Just like com.ibm.di.server.AssemblyLine (the task variable in JavaScript),
 * RS also provides a logmsg() method. Note also that in order to load a
 * Connector Interface, like JDBC or LDAP Connector, you use the loadConnector()
 * of the AssemblyLine class:
 * 
 * <pre>
 * task.loadConnector(connectorConfig)
 * </pre>
 * 
 * . To get the Connector Config, use the getConnector() method in RS. The same
 * goes for Parsers and Function components.
 * 
 * RS also provides the commandLineParam() method for retrieving commandline
 * arguments, including the user-defined ones (-0 through -9), that were
 * specified when the TDI Server was started.
 */
public class RS extends Thread implements RSInterface, AssemblyLine.AssemblyLineListener, Listenable<ConfigInstanceListener> {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Command line switch - ?
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_USAGE = "?";

	/**
	 * Command line switch - r
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_START_AL = "r";

	/**
	 * Command line switch - D
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_NO_AUTOSTART = "D";

	/**
	 * Command line switch - c
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_CONFIG = "c";

	/**
	 * Command line switch - w
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_WAIT = "w";

	/**
	 * Command line switch - P
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_PASSWORD = "P";

	/**
	 * Command line switch - p
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_DUMP_PROPS = "p";

	/**
	 * Command line switch - v
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_VERSION_INFO = "v";

	/**
	 * Command line switch - x
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_EXECUTE_SCRIPT = "x";

	/**
	 * Command line switch - d
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_RUN_DAEMON = "d";

	/**
	 * Command line switch - e
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_SECURE_MODE = "e";

	/**
	 * Command line switch - f
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_EXT_PROP_FILE = "f";

	/**
	 * Command line switch - b
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_DEBUG_OPTIONS = "b";

	/**
	 * Command line switch - B
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_AL_DEBUG = "B";

	/**
	 * Command line switch - Q
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_DEBUG_PORT = "Q";

	/**
	 * Command line switch - S
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_CONFIG_STDIN = "S";

	/**
	 * Command line switch - q
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_RUN_MODE = "q";

	/**
	 * Command line switch - Y
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_CONFIG_DRIVER = "Y";

	/**
	 * Command line switch - l
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_LOGFILE = "l";

	/**
	 * Command line switch - W
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_NO_TERMINATE = "W";

	/**
	 * Command line switch - R
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_DISABLE_REMOTE_API = "R";

	/**
	 * Command line switch - n
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_CONFIG_ENCODING = "n";

	/**
	 * Command line switch - T
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_DUMP_PERFPROPS = "T";

	/**
	 * Command line switch - M
	 * 
	 * See the parameters' usage.
	 */
	public static final String CL_SIMULATION_MODE = "M";

	/**
	 * Command line switch - g
	 * 
	 * See the parameter's usage.
	 */
	public static final String CL_CREATE_SOLDIR = "g";

	/**
	 * Command line switch - i<br>
	 * <br>
	 * if this option is specified, the server will not read global.properties;
	 * solution.properties will still be read if present
	 */
	public static final String CL_IGNORE_GLOBAL_PROPERTIES = "i";

	/**
	 * Command line switch - j<br>
	 * <br>
	 * if this option is specified, the AssemblyLine will read regression info
	 * from a file with that name.
	 */
	public static final String CL_READ_REGRESSION = "j";
	
	/**
	 * Command line switch - J<br>
	 * <br>
	 * if this option is specified, the AssemblyLine will write regression info
	 * to a file with that name.
	 */
	public static final String CL_WRITE_REGRESSION = "J";
	
	/**
	 * Command line switch - k<br>
	 * <br>
	 * If this option is specified, the work Entry will be ignored when reading
	 * or writing regression info.
	 */
	public static final String CL_REGRESSION_IGNORE_WORK = "k";
	
	/**
	 * Command line switch -X<br>
	 * <br>
	 * If this option is specified, Derby will be started and the server will exit.
	 */
	public static final String CL_START_DERBY = "X";

	/**
	 * Command line switch -Y<br>
	 * <br>
	 * If this option is specified, Derby will be stopped and the server will exit.
	 */
	public static final String CL_STOP_DERBY = "Y";
	
	/**
	 * This is not a command line option but an internal parameter that lets you
	 * start a Config Instance with a name different than the config file name.
	 * It is used for starting temporary Config Instances on load for editing.
	 */
	public static final String CL_INTERNAL_CONFIG_NSTANCE_NAME = "INTERNAL_CONFIG_INSTANCE_NAME";

	/**
	 * This is not a command line option but an internal parameter that lets you
	 * start a Config Instance by providing the configuration XML as a string.
	 */
	public static final String CL_INTERNAL_CONFIG_AS_STRING = "INTERNAL_CONFIG_AS_STRING";

	/**
	 * This is not a command line option but an internal parameter that lets you
	 * specify an additional config instance listener. The listener must
	 * implement the <code>com.ibm.di.server.ConfigInstanceListener</code>
	 * interface.
	 */
	public static final String CL_INTERNAL_ADD_LISTENER = "INTERNAL_ADD_LISTENER";

	/**
	 * The server will look for a property with this name when registering a
	 * shutdown hook. A System property with that name could hold a path the an
	 * executable file which will be executed when the server shuts down.
	 */
	public static final String PROPERTY_JVM_SHUTDOWN_HOOK = "jvm.shutdown.hook";

	/**
	 * This is the prefix of a property in the global.properties or
	 * solution.properties files.
	 */
	public static final String PROTECT_PREFIX = "{protect}-";

	/**
	 * This is the prefix of the value of a protected property in the
	 * global.properties or solution.properties files.
	 */
	public static final String PROTECT_VAL_PREFIX = "{encr}";

	/**
	 * System property that specifies the encoding to be used when
	 * reading/writing configuration files.
	 */
	public static final String PROP_CONFIG_ENCODING = "com.ibm.di.config.encoding";

	/**
	 * System property that specifies whether the Server is run in secure mode.
	 */
	public static final String PROP_SECURE_MODE = "com.ibm.di.server.securemode";

	/*
	 * Exit codes
	 */
	/**
	 * The exit code returned when REST fails.
	 */
	public static final int EXIT_CODE_REST_FAILED = 101;

	/**
	 * Flag that indicates whether the Server runs in secure mode.
	 */
	private static boolean serverInSecureMode = false;

	/**
	 * This is the main thread's Log object
	 */
	// public static Log LOG = new Log("miserver", "server");
	private Log log;

	/**
	 * Logger for the Connector Pool
	 */
	private static Log logConnPool = new Log("server.connpool");

	/**
	 * This is the configuration file in use
	 */
	private MetamergeConfig serverConfig;

	/**
	 * This is the templates configuration file loaded from the rs.jar file
	 */
	public static MetamergeConfig gSysConfig;

	/**
	 * This is the current configuration path
	 */
	private String configPath;

	/**
	 * If another process has requested termination of the server this flag is
	 * set.
	 */
	private volatile boolean exitRequested = false;

	private static Integer mTerminationExitCode = null;

	/**
	 * This flag is set (by command line) if the server is to terminate after
	 * running AssemblyLines
	 */
	// public static boolean exitAfterRun = false;
	private boolean exitAfterRun = false;

	/**
	 * This field tracks the total number of errors across all AssemblyLines
	 * when running in exit-after-run mode (-w flag)
	 */
	private int totalAssemblyLineErrors = 0;

	/**
	 * This field holds the exit code
	 */
	private int exitCode = 0;

	/**
	 * This is the exception object of the RS instance (in case it failed). May
	 * be read by different threads.
	 */
	private volatile Throwable exitError = null;

	/**
	 * This table holds startup parameters.
	 */
	public Hashtable<String, Object> params = new Hashtable<String, Object>();

	/**
	 * Time when the master {@link RS} started
	 */
	public long mmServerStarted;

	/**
	 * Time when the threaded {@link RS} started
	 */
	public long mmStarted;

	/**
	 * Time when the {@link #reload()} method was called.
	 */
	public long mmReloaded;

	/**
	 * Global static reference to the master RS object (non-threaded)
	 */
	public static RS gRS;

	/**
	 * Null value behavior
	 */
	private String nullBehavior;

	private String nullBehaviorValue;

	/**
	 * Null value definition
	 */
	private String nullDefinition;

	private String nullDefinitionValue;

	/**
	 * Active server instances. This is accessed by several threads so it needs
	 * to be synchronized.
	 */
	private static Hashtable<String, RS> activeServers = new Hashtable<String, RS>();

	/**
	 * The master thread-group used when creating new thread groups
	 */
	private static ThreadGroup masterGroup;

	/**
	 * The command line args
	 */
	private String[] commandLine;

	private String mCommandLineConfigId;

	/**
	 * Source of notifications for this config instance.
	 */
	private ThreadSafeListenableImpl<ConfigInstanceListener> ciEventSource = new ThreadSafeListenableImpl<ConfigInstanceListener>();

	/**
	 * ScriptEngine for server Hooks
	 */
	private static ScriptEngine se = null;

	/**
	 * Performance stats property
	 */
	private static String PROP_PERF_STATS = "com.ibm.di.server.perfStats";

	/**
	 * Contains the Connector Pool objects for this Config Instance.
	 */
	private Hashtable<String, ConnectorPool> connectorPools = new Hashtable<String, ConnectorPool>();

	/**
	 * Synchronization object used to determine whether the configuration
	 * instance thread has completed initialization.
	 * 
	 * @see #waitForInitializationToComplete(long)
	 */
	private CountDownLatch initializationLatch = new CountDownLatch(1);

	/**
	 * Synchronization object used to determine whether a shutdown request has
	 * been posted by another thread.
	 */
	private CountDownLatch shutdownLatch = new CountDownLatch(1);

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * This engine decides what to do when a connector raises an error.
	 */
	private static ReconnectRuleEngine reconnectRuleEngine = null;

	/**
	 * The policy, which the Server uses when selecting a name for a
	 * configuration instance.
	 */
	private static ConfigInstanceNamingPolicy configInstanceNamingPolicy = new DefaultConfigInstanceNamingPolicy();

	/**
	 * Map of Schedulers started with this RS
	 */
	private Map<String, Scheduler> schedulerMap = new HashMap<String, Scheduler>();

	/**
	 * The main methods creates the Log object and kicks off the main thread.
	 * 
	 * @param args
	 *            an array of the command line arguments
	 * @exception Exception
	 *                if error while preparing the solution directory occurs.
	 */
	public static void main(String[] args) throws Exception {

		prepareSolutionDirectory(new Log(PROPERTIES_FILE));
		gRS = new RS(args);
		gRS.run();
	}

	/**
	 * Check if the server is running in secured mode.
	 * 
	 * @return true if the server is in secure mode, false otherwise.
	 */
	public static boolean isSecured() {
		return serverInSecureMode;
	}

	/**
	 * Gets the reconnect engine.
	 * 
	 * @return {@link ReconnectRuleEngine} object responsible for handling
	 *         errors.
	 */
	public static ReconnectRuleEngine getReconnectRuleEngine() {
		return reconnectRuleEngine;
	}

	/**
	 * Default constructor
	 */
	public RS() {
		super();
	}

	protected RS(String[] args) {
		this.commandLine = args;
	}

	protected RS(ThreadGroup group, String name) {
		super(group, name);
	}

	/**
	 * Initialize System Queue when Server is started
	 */
	private boolean initializeSystemQueue() {
		Trace.entrymax(this, "initializeSystemQueue");
		try {
			SystemQueueEngine.getInstance();
			Trace.exitmax(this, "initializeSystemQueue");
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private void initializeAPIEngine() {
		Trace.entrymin(this, "initializeAPIEngine");
		if (Boolean.getBoolean(APIEngine.PROP_API_ON)) {
			try {

				APIEngine.initialize();

				if (commandLineParam(CL_DISABLE_REMOTE_API) != null)
					return;

				if (Boolean.getBoolean(APIEngine.PROP_API_REMOTE_ON)) {
					try {
						APIEngine.initializeRemote();
						APIEngine.startThreadDetectingIPChange(10000);
					} catch (Exception e) {
						checkExitOnServerApiError("error.on.remote.api.init", e);
						getLog().info("remote.api.engine.not.started");
					}
				}

				if (Boolean.getBoolean(APIEngine.PROP_REST_SERVER_ON)) {
					APIEngine.initializeRestServer();
				}

				if (Boolean.getBoolean(APIEngine.PROP_API_JMX_REMOTE_ON)) {
					try {
						com.ibm.di.api.jmx.JMXAgent.initializeRemote();
					} catch (Exception e) {
						getLog().info("error.on.remote.jmx.init", e.toString());
						getLog().info("remote.jmx.sconn.not.started");
					}
				}
			} catch (Exception e) {
				checkExitOnServerApiError("error.on.server.api.init", e);
				getLog().info("server.api.engine.not.started");
			}
		}
		Trace.exitmin(this, "initializeAPIEngine");
	}

	private void checkExitOnServerApiError(String res, Exception e) {
		// -- This is set by the CE to avoid "zombie" processes
		if ("true".equalsIgnoreCase(System.getenv("TDI_API_EXITONERR"))) {
			getLog().error(res, e.toString(), e);
			System.out.println(getLog().getString(res, e.toString()));
			System.exit(EXIT_CODE_REST_FAILED);
		}
		getLog().error(res, e);
	}

	/**
	 * 
	 * Sets the configuration that will be used by the server.
	 * 
	 * @param config
	 *            the configuration object.
	 */
	public void setConfiguration(MetamergeConfig config) {
		serverConfig = config;
	}

	// ***************** CONFIGURATION FILE METHODS ********************

	/**
	 * {@inheritDoc}
	 */
	public Object getConfiguration(String key) {
		try {
			if (key == null)
				return getMetamergeConfig().lookup("");
			else
				return getMetamergeConfig().lookup(key);
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getLibrary(String name) {
		try {
			return getMetamergeConfig().lookup("Libraries/" + name);
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public FunctionConfig getFunction(String name) throws Exception {
		try {
			return (FunctionConfig) getMetamergeConfig().getFunction(name);
		} catch (Exception trysysconfig) {
			return (FunctionConfig) gSysConfig.getFunction(name);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public LibraryConfig getLibraries() {
		try {
			return (LibraryConfig) getMetamergeConfig().lookup(MetamergeConfig.DEFAULT_LIBRARY_FOLDER);
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ScriptConfig getScript(String name) {
		try {
			return getMetamergeConfig().getScript(name);
		} catch (Exception err) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorConfig getConnector(String name) {
		try {
			return getMetamergeConfig().getConnector(name);
		} catch (Exception err) {
			// We did not find a component with that name. No need to throw or
			// log an Exception.
		}

		try {
			return gSysConfig.getConnector(name);
		} catch (Exception err) {
			// We did not find a component with that name. No need to throw or
			// log an Exception.
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ParserConfig getParser(String name) {
		try {
			return getMetamergeConfig().getParser(name);
		} catch (Exception err) {
			// We did not find a component with that name. No need to throw or
			// log an Exception.
		}

		try {
			return gSysConfig.getParser(name);
		} catch (Exception err) {
			// We did not find a component with that name. No need to throw or
			// log an Exception.
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public AttributeMapConfig getAttributeMap(String name) {
		try {
			ConnectorConfig cc = getConnector(name);
			return cc.getAttributeMap();
		} catch (Exception err) {
			getLog().error("error.getAttributeMap", name, err);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLineConfig getTask(String name) {
		try {
			return getMetamergeConfig().getAssemblyLine(name);
		} catch (Exception err) {
			getLog().error("getTask", name, err);
			return null;
		}
	}

	/**
	 * Check for existence of "AssemblyLine" name, or throw an exception
	 * 
	 * @param name
	 *            The AssemblyLine name
	 * @exception Exception
	 *                in case the AssemblyLine referred by the provided name
	 *                could not be found.
	 */
	public void checkTask(String name) throws Exception {
		try {
			getMetamergeConfig().getAssemblyLine(name);
		} catch (Exception err) {
			getLog().exception("al.config.notfound", name);
		}
	}

	/**
	 * Returns the SequenceConfig with the give name.
	 * @param name
	 * @return the SequenceConfig with the give name.
	 */
	private SequenceConfig getSequence(String name) {
		try {
			return getMetamergeConfig().getSequence(name);
		} catch (Exception err) {
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getSysProp(String name) {
		return System.getProperty(name);
	}

	// ******************************* MAIN THREAD LOOP ************************

	/**
	 * Used when starting the server instance.
	 * 
	 * @return the exit code the server ended with.
	 */
	public int runServer() {
		Trace.entrymin(this, "runServer");

		int initStatusCode = initializeConfigInstance();

		// signal that initialization is complete
		initializationLatch.countDown();

		if (initStatusCode != 0) {
			return initStatusCode;
		}

		String runMode = commandLineParam(CL_RUN_MODE);

		// Only run provided AssemblyLines?
		exitAfterRun = (commandLineParam(CL_WAIT) != null);

		// Start AssemblyLines - command line
		try {

			// Start AL in autostart folder (unless -D specified)
			if (commandLineParam(CL_NO_AUTOSTART) == null) {
				autoStartFolder();
				startSchedulers();
			}

			String sal = commandLineParam(CL_START_AL);
			if (sal != null) {
				StringTokenizer st = new StringTokenizer(sal, ",");
				while (st.hasMoreTokens()) {
					String al = (String) st.nextToken();
					getLog().info("start.AssemblyLine", al);

					TaskCallBlock tcb = new TaskCallBlock();
					if (runMode != null)
						tcb.setRunMode(runMode);
					if (commandLineParam(CL_SIMULATION_MODE) != null)
						tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, "true");
					if (commandLineParam(CL_READ_REGRESSION) != null) {
						tcb.setRegressionInputName(commandLineParam(CL_READ_REGRESSION));
						params.remove(CL_READ_REGRESSION);
					}
					if (commandLineParam(CL_WRITE_REGRESSION) != null) {
						tcb.setRegressionOutputName(commandLineParam(CL_WRITE_REGRESSION));
						params.remove(CL_WRITE_REGRESSION);
					}
					if (commandLineParam(CL_REGRESSION_IGNORE_WORK) != null) {
						tcb.setRegressionIgnoreWork(true);
					}
					AssemblyLine tr = startAL(al, tcb);
					if (exitAfterRun) {
						getLog().info("complete.AssemblyLine", al);
						tr.join();
						// Accumulate errors from this AssemblyLine
						TaskStatistics alStats = tr.getStats();
						if (alStats != null) {
							totalAssemblyLineErrors += alStats.err;
						}
					}
					tr = null;
				}
			}
		} catch (Exception e) {
			getLog().error("error.starting.AssemblyLines", e);
			System.err.println(getLog().getString("error.starting.AssemblyLines") + ": " + e.toString());
		}

		// Run AL debug
		if (commandLineParam(CL_AL_DEBUG) != null) {
			try {
				String al = commandLineParam(CL_AL_DEBUG);
				getLog().info("start.AssemblyLine.debug", al);

				TaskCallBlock tcb = new TaskCallBlock();
				if (runMode != null)
					tcb.setRunMode(runMode);
				tcb.setProperty(AssemblyLine.TCB_DEBUG_PORT, Integer.valueOf(commandLineParam(CL_DEBUG_PORT)));

				// run in simulation mode
				if (commandLineParam(CL_SIMULATION_MODE) != null)
					tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, "true");

				// Debugger options : [!]hostname where "!" means debug-on-error
				String opts = commandLineParam(CL_DEBUG_OPTIONS);
				if (opts != null) {
					if (opts.startsWith("!")) {
						tcb.setProperty(AssemblyLine.TCB_DEBUG_ONERROR, "false");
						tcb.setProperty(AssemblyLine.TCB_DEBUG_HOST, opts.substring(1));
					} else {
						tcb.setProperty(AssemblyLine.TCB_DEBUG_ONERROR, "true");
						tcb.setProperty(AssemblyLine.TCB_DEBUG_HOST, opts);
					}
				}
				AssemblyLine tr = startAL(al, tcb);

				getLog().info("complete.AssemblyLine", al);
				tr.join();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(sResHash.getString("error.start.assemblyline", e.toString()));
			}
			exitAfterRun = true;
		}

		// Exit if requested after running AssemblyLines
		if (exitAfterRun) {
			getLog().info("exit.after.run");
			if (totalAssemblyLineErrors > 0) {
				getLog().warn("exit.with.errors", Integer.valueOf(totalAssemblyLineErrors));
			}
			Trace.exitmin(this, "runServer");
			return (totalAssemblyLineErrors > 0 ? 1 : 0);
		}

		// If we run in server mode keep checking for active threads. When no
		// more threads
		// are running then there is no more to do.
		while (true) {
			try {
				shutdownLatch.await(2, TimeUnit.SECONDS);

				if (exitRequested) {
					getLog().info("terminate.requested");
					Trace.exitmin(this, "runServer");
					return (1);
				}

				// Don't die if we are requested to stay alive
				if (commandLineParam(CL_NO_TERMINATE) != null)
					continue;

				int ac = Thread.activeCount();
				if (ac == 1) {
					getLog().info("terminate.one.thread");
					Trace.exitmin(this, "runServer");
					return (0);
				}

				if (Monitor.allThreadsStopped()) {
					getLog().info("all.threads.stopped");
					Trace.exitmin(this, "runServer");
					return (0);
				}

			} catch (Exception ignore) {
				SystemFunctions.doNothing();
			}
		}

	}

	// ************************* RSInterface implementation
	// **************************** //

	/**
	 * Start a proxy-server thread.
	 */
	/*
	 * public synchronized void startThread (Remote conn) { RemoteConnect rs =
	 * new RemoteConnect(this, conn); rs.start(); }
	 */

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public AssemblyLine restartAL(String assemblyLine, String checkpointID) throws Exception {
		return startAL(assemblyLine, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAL(String assemblyLine) throws Exception {
		return startAL(assemblyLine, null);
	}

	/**
	 * Start named AssemblyLine by providing the run mode.
	 * 
	 * @see AssemblyLine#RUNMODE_I_NORMAL
	 * @see AssemblyLine#RUNMODE_I_RECORD
	 * @see AssemblyLine#RUNMODE_I_PLAYBACK
	 * @see AssemblyLine#RUNMODE_I_MANUAL
	 * 
	 * @param assemblyLine
	 *            Name of AssemblyLine to start
	 * @param runMode
	 *            the run mode in which the AssemblyLine should start
	 * @return The AssemblyLine Thread object
	 * @exception Exception
	 *                in case the AssemblyLine initialization fails.
	 */
	public AssemblyLine startAL(String assemblyLine, int runMode) throws Exception {

		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setRunMode(String.valueOf(runMode));

		return startAL(assemblyLine, tcb);
	}

	/**
	 * Start named AssemblyLine in debug mode.
	 * 
	 * @param assemblyLine
	 *            Name of AssemblyLine to start
	 * @param port
	 *            the port which the debug console will connect to.
	 * @return The AssemblyLine Thread object
	 * @exception Exception
	 *                if the AssemblyLine initialization fails.
	 */
	public AssemblyLine startALDebug(String assemblyLine, int port) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setProperty(AssemblyLine.TCB_DEBUG_PORT, Integer.valueOf(port));
		return startAL(assemblyLine, tcb);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAL(String assemblyLine, Connector connector, Entry work) throws Exception {
		TaskCallBlock tcb = new TaskCallBlock();
		tcb.setInitialWorkEntry(work);
		tcb.setRuntimeConnector(null, connector);
		return startAL(assemblyLine, tcb);
	}

	/**
	 * Start the AssemblyLine named in the TCB
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var tcb = system.newTCB();
	 * 
	 * tcb.setAssemblyLineName(&quot;ALName&quot;);
	 * tcb.setRunMode(com.ibm.di.server.AssemblyLine.RUNMODE_NORMAL); // &quot;normal&quot;
	 * 
	 * var al = main.startAL(tcb);
	 * al.join(); // Wait for called AL to complete
	 * </pre>
	 * 
	 * @param tcb
	 *            The TaskCallBlock
	 * @return The AssemblyLine Thread object
	 * @exception Exception
	 *                if the AssemblyLine initialization fails.
	 */
	public AssemblyLine startAL(TaskCallBlock tcb) throws Exception {
		if (tcb.getAssemblyLineName() == null) {
			getLog().exception("tcb.not.name.assembly.line");
			return null;
		} else
			return startAL(tcb.getAssemblyLineName(), tcb);
	}

	/**
	 * {@inheritDoc}
	 */
	public AssemblyLine startAL(String assemblyLine, Object io) throws Exception {
		return startAL(assemblyLine, io, null);
	}

	/**
	 * Start named AssemblyLine providing a parameter
	 * 
	 * @param assemblyLine
	 *            Name of AssemblyLine to start
	 * @param io
	 *            Parameter to AssemblyLine (Work Entry, Connector or Vector
	 *            with both )
	 * @param logAppender
	 *            An additional logger to use with the AssemblyLine
	 * @return The AssemblyLine Thread object
	 * @exception Exception
	 *                if an error while starting the AL thread occurs.
	 */
	public AssemblyLine startAL(String assemblyLine, Object io, LogInterface logAppender) throws Exception {
		return startAL(assemblyLine, io, logAppender, null);
	}

	/**
	 * Start named AssemblyLine providing parameters
	 * 
	 * @param assemblyLine
	 *            Name of AssemblyLine to start
	 * @param io
	 *            Parameter to AssemblyLine (Work Entry, Connector or Vector
	 *            with both )
	 * @param logAppender
	 *            An additional logger to use with the AssemblyLine
	 * @param alc
	 *            The AssemblyLineConfig to use. Note that when an AssemblyLineConfig is
	 *            specified, it will not be cloned. Therefore the same object should not
	 *            be used in multiple calls to this method, since each AssemblyLine
	 *            needs a unique AssemblyLineConfig.
	 *            
	 * @return The AssemblyLine Thread object
	 * @exception Exception
	 *                if an error while starting the AL thread occurs.
	 */
	public AssemblyLine startAL(String assemblyLine, Object io, LogInterface logAppender, AssemblyLineConfig alc) throws Exception {

		if (exitRequested)
			log.exception("cannot.start.while.shutdown", assemblyLine);
		limitNumberOfThreads();
		final AssemblyLine tr;
		synchronized (this) {
			if (alc == null)
				checkTask(assemblyLine);
			tr = new AssemblyLine(this, assemblyLine, io, null, alc);

			if (logAppender != null) {
				tr.getLog().addLogger(logAppender);
			}

			if ((commandLineParam(CL_DUMP_PERFPROPS) != null) || Boolean.getBoolean(PROP_PERF_STATS)) {
				tr.setPerfEnabled();
			}

			// listen for AL starup/termination to notify the Server API
			tr.addListener(this);

			tr.start();
		}

		if (tr.getRunMode() == AssemblyLine.RUNMODE_I_MANUAL) {
			tr.initExecuteProlog();
		}

		getLog().debug("started.AssemblyLine", assemblyLine);
		invokeServerHook("TDI_ALStarted", this, tr);
		return tr;
	}

	/**
	 * Starts the Sequence with the given name
	 * @param name Name of the Sequence
	 * @return The Sequence that was started
	 * @throws Exception
	 * @since 7.2
	 */
	public Sequence startSequence(String name) throws Exception {
		return startSequence(name, null, null);
	}
	
	/**
	 * Starts the Sequence with the given name
	 * @param name Name of the Sequence
	 * @param io Parameters to the Sequence, e.g a ScriptEngine or a Vector of parameters.
	 * These will be passed to all AssemblyLines in the Sequence.
	 * @return The Sequence that was started
	 * @throws Exception
	 * @since 7.2
	 */
	public Sequence startSequence(String name, Object io) throws Exception {
		return startSequence(name, io, null);
	}
	
	/**
	 * Starts the Sequence with the given name
	 * @param name Name of the Sequence
	 * @param io Parameters to the Sequence, e.g a ScriptEngine or a Vector of parameters.
	 * These will be passed to all AssemblyLines in the Sequence.
	 * @param logger A LogInterface that will be used for logging.
	 * @return The Sequence that was started
	 * @throws Exception
	 * @since 7.2
	 */
	public Sequence startSequence(String name, Object io, LogInterface logger) throws Exception {
		if (exitRequested)
			getLog().exception("cannot.start.while.shutdown", name);
		limitNumberOfThreads();
		final Sequence tr;
		SequenceConfig sc = getSequence(name);
		if (sc == null)
			getLog().exception("seq.config.notfound", name);
		synchronized (this) {
			tr = new Sequence(this, sc, io);
			if (logger != null)
				tr.getLog().addLogger(logger);
			tr.start();
		}

		getLog().debug("started.Sequence", name);
		// TODO: invokeServerHook("TDI_ALStarted", this, tr);
		return tr;	
	}
	
	/**
	 * This method launches the assembly lines specified in the Config/AutoStart
	 * folder.
	 */
	private void autoStartFolder() throws Exception {
		InstanceConfig ic = (InstanceConfig) serverConfig.lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER + "/"
				+ MetamergeConfig.DEFAULT_SERVER_AUTOSTART);
		ContainerConfig cc = ic.getStartupItems();
		for (int i = 0; i < cc.size(); i++) {
			BaseConfiguration b = cc.getConfig(i);
			String name = b.getStringParameter(InstanceConfig.AUTOSTART_NAME);
			BaseConfiguration c;
			try {
				c = (BaseConfiguration) serverConfig.lookup(name);
			} catch (Exception t) {
				String errorMessage = getLog().getString("autostart.error.find.load", name, t.toString());
				throw new Exception(errorMessage);
			}
			if (c instanceof AssemblyLineConfig)
				startAL(name);
			else {
				String errorMessage = getLog().getString("autoload.not.know.start", name);
				throw new Exception(errorMessage);
			}
		}
	}

	/**
	 * This method launches the schedulers.
	 */
	private void startSchedulers(){
		Enumeration<?> list;
		try {
			MetamergeFolder folder = (MetamergeFolder) serverConfig.lookup(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER);
			list = folder.list();
		} catch (Exception e) {
			return; // Not found
		}

		while (list.hasMoreElements()) {
			Object o = list.nextElement();
			if(o instanceof Binding)
				o = ((Binding) o).getObject();
			if (o instanceof SchedulerConfig && ((SchedulerConfig) o).getEnabled()) {
				new Scheduler(this, (SchedulerConfig) o).start();			
			}
		}
	}

	/**
	 * Stop named AssemblyLine in all Config Instances.
	 * 
	 * @param name
	 *            Name of AssemblyLine to stop
	 * @return The number of AssemblyLines we tried to stop
	 */
	public static int stopAL(String name) {
		int n = 0;
		for (AssemblyLine al : UserFunctions.getRunningALs(name)) {
			al.shutdown();
			n++;
		}
		return n;
	}

	// ************************* UTILITY METHODS ****************************

	/**
	 * Parse command line parameters and put them into the params table.
	 * 
	 * @param args
	 *            The command line parameter String array
	 * @return a map with the parsed command line parameters.
	 */
	private static Hashtable<String, String> parseCommandline(String[] args) {

		Hashtable<String, String> params = new Hashtable<String, String>();

		// SystemFunctions.main = this;

		for (int i = 0; i < args.length; i++) {
			String str = unquote(args[i]);
			if (str.startsWith("-") && str.length() > 1) {
				String key = str.substring(1, 2);
				StringBuffer val = new StringBuffer(unquote(str.substring(2)));

				// Ugly hack because MSDOS removes commas
				while (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					if (val.length() > 0)
						val.append(",");
					val.append(unquote(args[i + 1]));
					i++;
				}

				params.put(key, val.toString());
			}
		}

		return params;

	}

	/**
	 * {@inheritDoc}
	 */
	public void logmsg(String msg) {
		getLog().loginfo(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void logmsg(String level, String msg) {
		getLog().log(level, msg);
	}

	/**
	 * Prints a message to the log, marked as an Error. This method accepts an
	 * instance of the {@link Throwable} class which information is also put in
	 * the log.
	 * 
	 * @param msg
	 *            the String message
	 * @param error
	 *            the exception object
	 */
	public void logerror(String msg, Throwable error) {
		getLog().logerror(msg, error);
	}

	/**
	 * {@inheritDoc}
	 */
	public void logerror(String msg) {
		getLog().logerror(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dump(Object o) {
		getLog().dump(o);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dumpEntry(Entry e) {
		getLog().dumpEntry(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void reload() throws Exception {
		if (configPath == null && commandLineParam(CL_INTERNAL_CONFIG_AS_STRING) == null) {
			getLog().exception("exception.noconfig");
		}

		synchronized (this) {

			MetamergeConfig saveConfig = getMetamergeConfig();
			// Remove current from namespace (or it won't reload)
			if (saveConfig != null) {
				MetamergeConfigFactory.removeNamespace(saveConfig.toString());
			}

			MetamergeConfig mc;
			try {
				Hashtable<String, Object> env = new Hashtable<String, Object>();
				if (configPath == null) {
					String xmlConfig = commandLineParam(CL_INTERNAL_CONFIG_AS_STRING);
					if (xmlConfig != null) {
						// a temporary instance without an associated file
						env.put(javax.naming.Context.PROVIDER_URL, xmlConfig.getBytes("UTF-8"));
						env.put(MetamergeConfigFactory.MC_CREATE, "false");
						env.put(MetamergeConfigFactory.MC_DRIVER, "com.ibm.di.config.xml.MetamergeConfigXML");
						env.put(MetamergeConfigFactory.MC_ENCRYPT, "false");
					}
				} else if ("<stdin>".equals(configPath)) {
					getLog().info("loading.stdin.config");
					env.put(javax.naming.Context.PROVIDER_URL, System.in);
					// Remove the config if we read from stdin, to make sure we
					// read it from stdin.
					MetamergeConfigFactory.removeNamespace(getName());
				} else {
					getLog().info("loading.file.config", configPath);
					env.put(javax.naming.Context.PROVIDER_URL, configPath);
					if (commandLineParam(CL_PASSWORD) != null)
						env.put(javax.naming.Context.SECURITY_CREDENTIALS, commandLineParam(CL_PASSWORD));
					env.put(MetamergeConfigFactory.MC_CREATE, "false");
				}
				if (commandLineParam(CL_CONFIG_DRIVER) != null)
					env.put(MetamergeConfigFactory.MC_DRIVER, commandLineParam(CL_CONFIG_DRIVER));

				/*
				 * Normally config objects are named after their filepaths,
				 * which causes instances using the same configuration file to
				 * share the same MetamergeConfig object. To avoid that, name
				 * each MetamergeConfig after the instance that uses it.
				 */
				env.put(MetamergeConfigFactory.MC_NAMESPACE, getName());

				mc = MetamergeConfigFactory.getInstance(env);
				mmReloaded = System.currentTimeMillis();
				setConfiguration(mc);

			} catch (Exception error) {
				// Re-register current config in case of errors
				if (saveConfig != null) {
					MetamergeConfigFactory.registerNamespace(saveConfig.toString(), saveConfig);
					setConfiguration(saveConfig);
				}

				throw error;
			}

		}

	}

	/**
	 * {@inheritDoc}
	 */
	public String getConfigPath() {
		return configPath;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfigPath(String path) {
		configPath = path;
	}

	/**
	 * {@inheritDoc}
	 */
	public void persistConfiguration() throws Exception {
		getMetamergeConfig().commitChanges(null);
	}

	/**
	 * This method writes a log header to the Log object.
	 * 
	 * @param log
	 *            The log output object
	 */
	public static void showLogHeader(Log log) {

		log.info("header.version", Version.version());
		String fullOSName = "Unknown OS";
		final String SEARCH_TERM = "OS Name:";
		String str = System.getProperty("os.name");

		if (str.toLowerCase().contains("win")) {
			try {
	            Process pr = Runtime.getRuntime().exec("SYSTEMINFO");
	            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String line;
	            while((line=in.readLine()) != null) {	            	
	               if(line.contains(SEARCH_TERM)) {
	                  fullOSName = line.split(SEARCH_TERM)[1].trim();
					  log.info("header.os_name", fullOSName.trim());
	                  break;
	               }
	            }
	        }
	         catch(IOException ioe) {      
	            System.err.println(ioe.getMessage());
				log.info(ioe.getMessage());
	        }
		}   
		else
			log.info("header.os_name", str);
			
		String javaVersion = System.getProperty("java.vm.info");
		if (javaVersion == null)
			javaVersion = System.getProperty("java.version") + " (" + System.getProperty("java.vm.version") + ")";
		else if (javaVersion.indexOf('\n') > 0)
			javaVersion = javaVersion.substring(0, javaVersion.indexOf('\n')).trim();
		log.info("header.runtime", System.getProperty("java.vm.vendor"), javaVersion);
		log.info("header.library", System.getProperty("sun.boot.library.path"));
		log.info("header.workdir", System.getProperty("user.dir"));
		log.info("header.cfgfile", getServer().configPath);
		log.loginfo(sResHash.getString("info.dashes.begin.end"));
	}

	/**
	 * This method returns the default directory path where log files are
	 * stored. TODO: implement this
	 * 
	 * @param owner
	 *            The Java class or string denoting the type (e.g. AssemblyLine)
	 * @return The log file directory
	 */
	public String getLogDirectory(Object owner) {
		return ".";
	}

	/**
	 * This method removes single and double quotes from a string
	 * 
	 * @param str
	 *            The string to remove the quotes from.
	 * @return The unquoted string
	 */
	private static String unquote(String str) {
		if (str.startsWith("\"") && str.endsWith("\"")) {
			str = str.substring(1, str.length() - 1);
		}
		if (str.startsWith("'") && str.endsWith("'")) {
			str = str.substring(1, str.length() - 1);
		}

		return str;
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdownServer() {
		shutdownServer(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void shutdownServer(int aExitCode) {
		Trace.entrymax(this, "shutdownServer");
		setTerminationExitCode(aExitCode);
		exitRequested = true;
		shutdownLatch.countDown();
		if (this == gRS) {
			Trace.exitmax(this, "shutdownServer");
			return;
		}
		// this is a config instance, stop all children
		stopSchedulers();
		stopALs();
		Trace.exitmax(this, "shutdownServer");
	}

	/**
	 * Raise the shutdown request flag. This method requests controlled shutdown
	 * of all AssemblyLines running on the server at the time of calling, and
	 * waits for the AssemblyLines to stop.
	 * 
	 * @param exitCode
	 *            the code to return when the application exits.
	 * @param async
	 *            If true, crate new Threads to wait for the AssemblyLines to
	 *            stop
	 * @since 7.1
	 */
	public void shutdownServer(int exitCode, boolean async) {
		Trace.entrymax(this, "shutdownServer");
		setTerminationExitCode(exitCode);
		exitRequested = true;
		shutdownLatch.countDown();
		if (this == gRS) {
			Trace.exitmax(this, "shutdownServer");
			return;
		}
		// this is a config instance, stop all children
		stopSchedulers();
		stopALs(async);
		Trace.exitmax(this, "shutdownServer");
	}

	/**
	 * Set the termination exit code using a static method, to maybe stop findbugs from complaining.
	 * @param i
	 */
	private static void setTerminationExitCode(int i) {
		mTerminationExitCode = i;
	}

	/**
	 * Stop all AssemblyLines started by this RS
	 */
	private void stopALs() {
		for (AssemblyLine al : Monitor.runningALs()) {
			if (al.getParent() == this)
				try {
					al.shutdown();
				} catch (Exception e) {
					// Cannot happen? Continue stopping the other AssemblyLines
					SystemFunctions.doNothing();
				}
		}
	}

	/**
	 * Stop all AssemblyLines started by this RS, and wait for them to stop
	 * 
	 * @param async
	 *            If true, create new Threads to wait for the AssemblyLines to
	 *            stop.
	 */
	private void stopALs(boolean async) {
		for (AssemblyLine al : Monitor.runningALs()) {
			if (al.getParent() == this)
				try {
					al.shutdown(async);
				} catch (Exception e) {
					// Cannot happen? Continue stopping the other AssemblyLines
					SystemFunctions.doNothing();
				}
		}
	}

	/**
	 * Stops specified running AssemblyLines.
	 * 
	 * @param name
	 *            The name of the AssemblyLine, or null for any name.
	 * @param hashCode
	 *            The hashCode for the AssemblyLine, as seen in the log files.
	 *            -1 is any hashCode.
	 * @param recurse
	 *            If true, recursively stop any AssemblyLines started by the
	 *            AssemblyLine(s) to stop.
	 * @param async
	 *            If true, crate new Threads to wait for the AssemblyLines to
	 *            stop
	 * @param rs
	 *            If null, stop AssemblyLines in any RS. If non-null, only stop
	 *            AssemblyLines in that RS.
	 * @throws AbortALException
	 *             if this method stops the AssemblyLine that called it.
	 * @since 7.1
	 */
	public static void stopAssemblyLines(String name, int hashCode, boolean recurse, boolean async, RS rs) throws AbortALException {
		if (name == null && hashCode == -1)
			recurse = false; // We will stop all ALs anyway, no need for
		// recursion.

		AbortALException exception = null;
		for (AssemblyLine al : UserFunctions.getRunningALs(name)) {
			if (rs != null && al.getParent() != rs)
				continue;
			if (hashCode != -1 && hashCode != al.hashCode())
				continue;
			try {
				al.shutdown(async);
			} catch (AbortALException e) {
				exception = e;
			}
			if (recurse)
				try {
					stopChildAssemblyLines(al, async);
				} catch (AbortALException e) {
					exception = e;
				}
		}
		if (exception != null)
			throw exception;
	}

	/**
	 * Recursively stop all children of one AssemblyLine. Only the children are
	 * stopped, not the AssemblyLine itself.
	 * 
	 * @param al
	 *            The Assemblyline whose children are to be stopped.
	 * @param async
	 *            If true, crate new Threads to wait for the AssemblyLines to
	 *            stop
	 * @throws AbortALException
	 *             if this method shuts down the AssemblyLine that called it.
	 * @since 7.1
	 */
	public static void stopChildAssemblyLines(AssemblyLine al, boolean async) throws AbortALException {
		AbortALException exception = null;
		for (AssemblyLine child : Monitor.runningALs()) {
			if (child.getParentAL() == al) {
				try {
					child.shutdown(async);
				} catch (AbortALException e) {
					exception = e;
				}
				try {
					stopChildAssemblyLines(child, async);
				} catch (AbortALException e) {
					exception = e;
				}
			}
		}
		if (exception != null)
			throw exception;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehavior() {
		if (nullBehavior == null || nullBehavior.equals("Default Behavior"))
			return "Delete";
		else {
			return nullBehavior;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullBehaviorValue() {
		return nullBehaviorValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullDefinition() {
		if (nullDefinition == null || nullDefinition.equals("Default"))
			return "AbsentAttribute";
		else {
			return nullDefinition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNullDefinitionValue() {
		return nullDefinitionValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public ServerSocketFactory getServerSocketFactory(boolean useSSL) {
		BindAddressPolicy bindAddrPolicy = new BindAddressPolicyImpl(System.getProperties());
		return new ServerSocketFactoryEX(bindAddrPolicy, useSSL);
	}

	/**
	 * This method is called by the
	 * {@link #startAL(String, Object, LogInterface)} method to check whether a
	 * limit of the maximum running threads exists.
	 * <p>
	 * The limit is set using the Java property
	 * "com.ibm.di.server.maxThreadsRunning". If there is such a property and
	 * its value is greater than 3 then the thread that calls this method
	 * (usually an AssemblyLine thread) will have to wait until another thread
	 * ends. If the value of the Java property is not a string representation of
	 * an <code>int</code> then the maximum running threads limit check is not
	 * done.
	 * <p>
	 * If the thread calling this method is waiting for one of the running
	 * threads to end is unable to start in timely fashion then a warning for
	 * possible deadlock will be output to the log. In that case the calling
	 * thread will start.
	 */
	public void limitNumberOfThreads() {
		String num = System.getProperty("com.ibm.di.server.maxThreadsRunning");
		if (num == null || num.length() == 0)
			return;

		int n;
		try {
			n = Integer.parseInt(num);
		} catch (Exception e) {
			return;
		}
		if (n < 3)
			return;

		int count = 0;
		while (Thread.activeCount() >= n && count < 1000) {
			try {
				Thread.sleep(10);
				count++;
			} catch (InterruptedException ie) {
			}
		}
		if (count == 1000) {
			getLog().warn("warn.deadlock");
		}
	}

	/**
	 * This method loads the global.properties/solution.properties and parses
	 * their content.
	 */
	public static void setGlobalProperties() {
		if (gRS != null)
			setGlobalProperties(gRS.getLog());
		else
			setGlobalProperties(new Log("miserver", "server"));
	}

	private static void setGlobalProperties(Log log) {
		String str = System.getProperty("IDILoader.jars");
		boolean loadGlobalProperties = (gRS == null || null == gRS.commandLineParam(CL_IGNORE_GLOBAL_PROPERTIES));
		if (str != null && loadGlobalProperties) {
			loadPropertiesFromFile(str + File.separator + "etc" + File.separator + "global.properties", log);
		}

		File solutionProps = new File("solution.properties");
		if (solutionProps.exists()) {
			loadPropertiesFromFile("solution.properties", log);
		}

		// Register additional protocol handlers (e.g for https)
		String provider = System.getProperty("com.metamerge.handlerPkgs");
		if (provider != null) {
			String current = System.getProperty("java.protocol.handler.pkgs");
			if (current != null) {
				provider += "|";
				provider += current;
			}
			System.setProperty("java.protocol.handler.pkgs", provider);
		}

		// Register JSSE provider?
		provider = System.getProperty("com.ibm.di.sslProvider");
		try {
			if (provider != null && provider.length() > 0)
				Security.addProvider((Provider) Class.forName(provider).newInstance());
		} catch (Throwable e) {
			log.error("error.addProvider", provider, e);
		}

		// com.ibm.crypto.provider.IBMJCE
		provider = System.getProperty(CryptoFactory.CRYPTO_PROVIDER_CLASS_PROPERTY);
		try {
			if (provider != null && provider.trim().length() > 0) {
				Provider provObj = CryptoFactory.loadProvider(provider);
				Security.addProvider(provObj);
				System.setProperty(CryptoFactory.CRYPTO_PROVIDER_NAME_PROPERTY, provObj.getName());
			}
		} catch (Throwable e) {
			log.error("error.addProvider.crypto", provider, e);
		}
		// }
	}

	/**
	 * Checks if the solution.properties file should be created.
	 * 
	 * @return false if the solution.properties file exists, true if the file
	 *         does not exist and should be created
	 */
	public static boolean shouldCreateSolutionProps() {
		// Always create the file, even in installation directory.
		// File workDir = new File(new File("").getAbsolutePath());
		// File tdiDir = new File(System.getProperty("com.ibm.di.installdir"));
		// //If install dir and solution dir are the same then return
		// if (tdiDir.equals(workDir))
		// return false;

		return !new File("solution.properties").exists();
	}

	/**
	 * Prepare the solution directory (working directory) by ensuring
	 * solution.properties is present.
	 * 
	 * @param log
	 *            the log object to log in.
	 * @throws Exception
	 *             if I/O error occurs while working with files.
	 */
	public static void prepareSolutionDirectory(Log log) throws Exception {
		if (!shouldCreateSolutionProps())
			return;

		// Create logs directory
		File target = new File("logs");
		if (!target.exists())
			FileUtils.mkdir(target, log);

		// Create configs directory (api.config.folder)
		target = new File("configs");
		if (!target.exists())
			FileUtils.mkdir(target, log);

		// Copy files if necessary
		File tdiDir = new File(System.getProperty("com.ibm.di.installdir"));
		UserFunctions uf = new UserFunctions();

		// solution.properties
		File source = new File(tdiDir + "/etc/", "global.properties");
		target = new File("solution.properties");
		if (UserFunctions.copyFile(source, target, false))
			log.info("solution.file.created", target.getAbsolutePath(), source);

		// the stash file (idisrv.sth)
		source = new File(tdiDir, StashFile.STASH_FILE_NAME);
		target = new File(StashFile.STASH_FILE_NAME);
		if (UserFunctions.copyFile(source, target, false))
			log.info("solution.file.created", target.getAbsolutePath(), source);

		// testserver.jks
		source = new File(tdiDir, "testserver.jks");
		target = new File("testserver.jks");
		if (source.exists() && UserFunctions.copyFile(source, target, false))
			log.info("solution.file.created", target.getAbsolutePath(), source);

		// copy the etc directory into the solution directory
		File etcDir = new File("etc");
		if (!etcDir.exists()) {
			log.info("copying.etc.files", tdiDir);
			uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "etc", "etc", true, true, log);
		}

		File apiDir = new File("serverapi");
		if (!apiDir.exists()) {
			log.info("copy.serverapi.files", tdiDir);
			uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "serverapi", "serverapi", true, true, log);
			//RTC Defect#10740
			File cryptoutils = null;
			if(System.getProperty("os.name").toLowerCase().contains("win")){
				cryptoutils = new File("serverapi"+File.separator+"cryptoutils.bat");
			} else {
				cryptoutils = new File("serverapi"+File.separator+"cryptoutils.sh");
			}
			if(cryptoutils.exists() && cryptoutils.isFile()){
				cryptoutils.delete();					
			}
		}

		// copy the osgi directory into the solution directory
		File osgiInstDir = new File(tdiDir, "osgi");
		if (osgiInstDir.exists()) {
			File osgiSolDir = new File("osgi");

			if (!osgiSolDir.exists()) {
				FileUtils.mkdir(osgiSolDir, log);
			}

			File consoleIniInst = new File(osgiInstDir, "console.ini");
			File consoleIniSol = new File(osgiSolDir, "console.ini");
			if (consoleIniInst.exists() && UserFunctions.copyFile(consoleIniInst, consoleIniSol, false)) {
				log.info(sResHash.getString("copy.osgi.files", new Object[] { consoleIniInst.getAbsolutePath(),
						consoleIniSol.getAbsolutePath() }));
			}

			File launchIniInst = new File(osgiInstDir, "launch.ini");
			File launchIniSol = new File(osgiSolDir, "launch.ini");
			if (launchIniInst.exists() && UserFunctions.copyFile(launchIniInst, launchIniSol, false)) {
				log.info(sResHash.getString("copy.osgi.files", new Object[] { launchIniInst.getAbsolutePath(),
						launchIniSol.getAbsolutePath() }));
			}

			// copy the osgi directory into the solution directory
			File osgiConfigInstDir = new File(osgiInstDir, "configuration");
			if (osgiConfigInstDir.exists()) {
				File osgiConfigSolDir = new File(osgiSolDir, "configuration");

				if (!osgiConfigSolDir.exists()) {
					FileUtils.mkdir(osgiConfigSolDir, log);
				}

				File configIniInst = new File(osgiConfigInstDir, "config.ini");
				File configIniSol = new File(osgiConfigSolDir, "config.ini");
				if (configIniInst.exists() && UserFunctions.copyFile(configIniInst, configIniSol, false)) {
					log.info(sResHash.getString("copy.osgi.files", new Object[] { configIniInst.getAbsolutePath(),
							configIniSol.getAbsolutePath() }));
				}
			}
		}
		
		File scimDir = new File("SCIM");
		if (!scimDir.exists()) {
			uf.copyDirectory(tdiDir.getAbsolutePath() + File.separator + "SCIM", "SCIM", true, true, log);
		}

	}

	/**
	 * Load the properties from the specified file and set them as system
	 * properties. If the properties file references other properties files,
	 * those will be loaded too. Encrypted properties are skipped. The method
	 * does not throw - all errors are logged in the specified logger.
	 * 
	 * @param path
	 *            a properties file
	 * @param log
	 *            logger
	 */
	private static void loadPropertiesFromFile(String path, Log log) {

		// ignore encrypted properties at this time
		loadPropertiesFromFile(path, log, false);
	}

	/**
	 * Load the properties from the specified file and set them as system
	 * properties. If the properties file references other properties files,
	 * those will be loaded too. If decryption is required, the method decrypts
	 * encrypted properties. If decryption fails, an error message is logged and
	 * operation continues. If decryption is not required, the method skips
	 * encrypted properties. The method does not throw - all errors are logged
	 * in the specified logger.
	 * 
	 * @param path
	 *            a properties file
	 * @param log
	 *            logger
	 * @param decrypt
	 *            whether to decrypt encrypted properties; if set to false,
	 *            encrypted properties will be skipped
	 */
	private static void loadPropertiesFromFile(String path, Log log, boolean decrypt) {

		log.debug("load.properties.from", path);

		Crypto decryptor = null;
		if (decrypt) {
			// provide a decrypting object only if decryption is required
			try {
				decryptor = CryptoUtils.getDefaultCrypto();
			} catch (Exception error) {
				log.error("error.decrypt.protected.property", error.toString());
			}
		}

		PropertiesFile propsFile = null;
		try {
			propsFile = new PropertiesFile(decryptor, path, true);
		} catch (Exception error) {
			log.error("error.loading.global.properties", path, error.getMessage());
			System.err.println(log.getString("error.loading.global.properties", path, error.getMessage()));
			System.exit(1);
		}

		Iterator<?> it = propsFile.keys();
		while (it.hasNext()) {

			String key = (String) it.next();
			String value = null;

			try {
				// Skip encrypted properties when decryption is not required.
				if (decrypt || !propsFile.isPropertyEncrypted(key)) {

					value = propsFile.getProperty(key, log);
				}
			} catch (Exception decryptError) {
				log.error("error.decrypt.protected.property", decryptError.toString());
			}

			// override encoding property with the command-line argument
			if (key.equals(PROP_CONFIG_ENCODING)) {
				RS rs = getServer();
				if (rs != null) {
					String encoding = null;
					if ((encoding = rs.commandLineParam(CL_CONFIG_ENCODING)) != null) {
						value = encoding;
					}
				}
			}

			// set as system property
			if (value != null) {
				System.setProperty(key, value);
			}

		}

		// Use the logging enabled property from the properties file
		Log.setLoggingEnabled();

	}

	/**
	 * Loads the properties from the specified file and sets them as system
	 * properties. If the properties file references other properties files,
	 * those will be loaded too. Decrypts encrypted properties. If decryption
	 * fails, an error message is logged and operation continues.
	 * 
	 * After loading, the method overwrites the file to ensure that all
	 * protected properties are encrypted. The file will not be overwritten, if
	 * all its protected properties are already encrypted. The file will also
	 * not be overwritten, if there is no write-access to it.
	 * 
	 * The method does not throw - all errors are logged in the specified
	 * logger.
	 * 
	 * @param path
	 *            a properties file
	 * @param log
	 *            logger
	 */
	public static void encryptPropertiesfile(String path, Log log) {

		/*
		 * Also load encrypted properties - the cryptographic module of the
		 * Server must have been already initialized.
		 */
		loadPropertiesFromFile(path, log, true);

		/*
		 * Now load the same file again, this time without loading the other
		 * properties files that it references. We are going to write the
		 * properties back to disk after encryption, so we do not to include
		 * properties from other files.
		 */
		PropertiesFile propsFile = null;
		try {
			propsFile = new PropertiesFile(CryptoUtils.getDefaultCrypto(), path, false);
		} catch (Exception error) {
			log.error("error.loading.global.properties", path, error.getMessage());
			return;
		}

		Iterator<?> it = propsFile.keys();
		while (it.hasNext()) {
			String key = (String) it.next();

			if (propsFile.isPropertyProtected(key)) {
				try {
					propsFile.setPropertyEncrypted(key, true);
				} catch (Exception e) {
					log.error("error.encrypt.protected.property", e.toString());
				}
			}
		}

		if (new File(path).canWrite() && propsFile.isModified()) {

			try {

				propsFile.store(path, null, null);
			} catch (Exception e) {
				log.error("error.rewrite.properties.file", e.toString());
			}
		}
	}

	/**
	 * Thread main
	 */
	public void run() {
		Trace.entrymax(this, "run");
		// Rhino 1.5R2 class loading problem
		setContextClassLoader(getClass().getClassLoader());

		// Need log object in loadSystemNamespace()
		if (gRS == this) {
			log = new Log("miserver", "server");
		} else {
			log = new Log("miserver", getName());
			final Object startupListener = getParamObj(CL_INTERNAL_ADD_LISTENER);
			if (startupListener instanceof ConfigInstanceListener) {
				addListener((ConfigInstanceListener) startupListener);
			}
			final RSInterface ci = this;
			ciEventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<ConfigInstanceListener>() {
				public void visit(ConfigInstanceListener listener) {
					listener.configInstanceStarted(ci);
				}
			});
		}

		// If we are the master RS with no config
		if (gRS == this) {
			mmServerStarted = System.currentTimeMillis();
			runMaster();
		}

		// Else we are a server instance
		try {
			mmStarted = System.currentTimeMillis();
			invokeServerHook("TDI_ConfigStarted", this, this);
			exitCode = runServer();
			getLog().info("rs.exited.with.status", getName(), "" + exitCode);
		} catch (Throwable t) {
			t.printStackTrace();
			getLog().error("error.running.instance", t);
		} finally {
			invokeServerHook("TDI_ConfigStopped", this, this);
			unregisterServer(this);
			terminateConnectorPools();
			Trace.exitmax(this, "run");
		}
	}

	private void securityInitError(String aErrorMsg, boolean aCrit) {
		if (aCrit) {
			logerror(aErrorMsg);
			System.out.println(sResHash.getString("rs.security.init.error", aErrorMsg));
			System.out.println();
			invokeServerHook("TDI_Shutdown", this, Integer.valueOf(1));
			System.exit(1);
		} else {
			Log log = getLog();
			log.logwarn(aErrorMsg);
			log.logwarn(log.getString("server.security.not.inited"));
			log.logwarn(log.getString("cannot.use.pki.encrypt"));
		}
	}

	/**
	 * Method reads the password from stash file and sets it in CryptoUtils for
	 * encryption/decryption .
	 * 
	 */
	private void initializeSecurity() {
		Trace.entrymin(this, "initializeSecurity");
		boolean secureMode = RS.isSecured();

		// read keystore passwords
		Vector<String> stashFilePasswords = null;
		try {
			// When running inside ibmditk the CE has already called
			// StashFile.readPasswords() which sets a one-shot static flag.
			// Fall back to readPasswordsFromFile() in that case so the server
			// can still obtain the passwords it needs.
			stashFilePasswords = StashFile.readPasswords();
		} catch (Exception e) {
			try {
				String stashPath = StashFile.STASH_FILE_NAME;
				File f = new File(stashPath);
				if (!f.exists())
					f = new File(System.getProperty("com.ibm.di.installdir", ""), stashPath);
				stashFilePasswords = StashFile.readPasswordsFromFile(f.getAbsolutePath());
			} catch (Exception e2) {
				String initError = getLog().getString("cannot.read.stash.file", e2.toString());
				securityInitError(initError, secureMode);
				return;
			}
		}

		if (stashFilePasswords == null || stashFilePasswords.size() == 0) {
			String initError = getLog().getString("no.password.found.in.stash");
			securityInitError(initError, secureMode);
			return;
		}

		getLog().info("stash.file.read");

		String keyStorePassword = stashFilePasswords.get(0);
		String keyPassword = null;
		if (stashFilePasswords.size() > 1) {
			keyPassword = stashFilePasswords.get(1);
		} else {
			getLog().info("no.key.password.in.stash");
			keyPassword = keyStorePassword;
		}

		// safely distribute passwords to only those components that need them;
		// it is responsibility of each component that gets passwords to protect
		// them from public access
		try {
			com.ibm.di.api.security.CryptoUtils.init(keyStorePassword, keyPassword);

			// The APIEngine pwd will be set later on after the global/soln
			// props file
			// has been decrypted and passwords set to system properties.
			// com.ibm.di.api.APIEngine.setKeyStorePasswords(keyStorePassword,
			// keyPassword);
		} catch (Exception e) {
			// When running inside ibmditk the CE has already initialized
			// CryptoUtils. If it is already initialized we can safely continue.
			if (!com.ibm.di.api.security.CryptoUtils.isInitialized()) {
				String initError = getLog().getString("cannot.setup.server.keystore", e.toString());
				securityInitError(initError, secureMode);
				return;
			}
		}

		Log log = getLog();
		String securityInitialized = log.getString("server.security.inited");
		log.loginfo(securityInitialized);
		if (secureMode) {
			System.out.println(securityInitialized);
		}
		Trace.exitmin(this, "initializeSecurity");
	}

	/**
	 * Run the master instance
	 */
	private void runMaster() {
		Trace.entrymin(this, "runMaster");
		masterGroup = getThreadGroup();

		params.putAll(parseCommandline(commandLine));

		if (params.size() == 0) {
			usage(1);
		}

		redirectConsoleOutput();

		if (commandLineParam(CL_USAGE) != null) {
			usage(0);
		}

		// -- Create solution directory and exit
		if (commandLineParam(CL_CREATE_SOLDIR) != null) {
			invokeServerHook("TDI_Shutdown", this, Integer.valueOf(0));
			System.exit(0);
		}

		setGlobalProperties();

		if (commandLineParam(CL_START_DERBY) != null) {
			startDerbyServerAndExit();
		}

		if (commandLineParam(CL_STOP_DERBY) != null) {
			stopDerbyServerAndExit();
		}
		
		initializeProviders();
		loadUserJars();

		// as an optimization turn off listeners which only the CE uses
		MetamergeConfigFactory.setUseConfigListeners(false);

		// Make sure system namespace is available for packages
		loadSystemNamespace();

		// Load the packages
		MetamergeConfigFactory.addPackages(com.ibm.di.loader.IDILoader.getInstalledPackages());

		if (commandLineParam(CL_VERSION_INFO) != null) {
			try {
				// this relies on the system namespaces so make sure they are
				// loaded
				System.out.println(sResHash.getString("rs.version.information", com.ibm.di.util.VersionInformation
						.getVersionNumbers()));
			} catch (Exception e) {
				System.out.println(sResHash.getString("rs.version.information.error", e.getMessage()));
			}
			invokeServerHook("TDI_Shutdown", this, Integer.valueOf(0));
			System.exit(0);
		}

		if ("true".equals(System.getProperty("com.ibm.di.server.fipsmode.on")) && !FIPSCompliantMode.isFIPSenabled()) {
			/*
			 * isFIPSenabled() checks if FIPS mode has been initialized in order
			 * to initialize it only once
			 */
			try {
				FIPSCompliantMode.initializeFIPSMode();
			} catch (Exception exc) {
				logerror(sResHash.getString("RS.FIPS.MODE.FAILED.TO.INITIALIZE", exc));
				System.exit(1);
			}
			getLog().loginfo(sResHash.getString("RS.FIPS.MODE.INITIALIZED.SUCCESSFULLY"));
		}

		// This hook is always invoked on UNIX systems and only when pressing
		// Ctrl+C in the server console on Windows. Note that when
		// ibmdiservice.exe shuts down this JVM shutdown hook is not invoked.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					// Always shutdown server gracefully
					RS.shutdownAllServers(0, true, true);

					String externalApp = System.getProperty(PROPERTY_JVM_SHUTDOWN_HOOK);
					if (externalApp != null && externalApp.trim().length() > 0) {
						Runtime.getRuntime().exec(externalApp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		String extApp = System.getProperty(PROPERTY_JVM_SHUTDOWN_HOOK);
		if (extApp != null && extApp.trim().length() > 0) {
			getLog().info("added.jvm.shutdown.hook", extApp);
		}

		// check for secure mode
		if (commandLineParam(CL_SECURE_MODE) != null || Boolean.getBoolean(PROP_SECURE_MODE) )
		{
			serverInSecureMode = true;
			getLog().info("server.run.secure.mode");
		} else {
			getLog().info("server.run.standard.mode");
		}
		// Sets the passwords required for the encryption
		initializeSecurity();

		if (null == commandLineParam(CL_IGNORE_GLOBAL_PROPERTIES)) {
			encryptPropertiesfile(System.getProperty("IDILoader.jars") + File.separator + "etc" + File.separator
					+ "global.properties", new Log("miserver", "server"));
		}

		File solutionProps = new File("solution.properties");
		if (solutionProps.exists()) {
			encryptPropertiesfile("solution.properties", new Log("miserver", "server"));
		}

		try {
			initializeServerHooks();
		} catch (Exception err) {
			getLog().error("error.init.server.hook", err.toString());
			System.exit(EXIT_CODE_REST_FAILED);
		}

		autoStartCloudScape();

		//initializeSystemQueue();

		initializeAPIEngine();

		// Most of the time the server will be running with OSGi turned on due
		// to the REST API app, so don't do a file based lookup if the container
		// is booting up anyway.
		attachIntegrationComponents(gSysConfig);

		// Create the reconnect rule engine
		if (reconnectRuleEngine == null) {
			reconnectRuleEngine = new ReconnectRuleEngine(getLog());
			try {
				reconnectRuleEngine.loadRules("etc/reconnect.rules", gSysConfig);
			} catch (Exception ex) {
				getLog().error("error.loading.reconnect.rules", ex);
			}
		}

		if (commandLineParam(CL_RUN_DAEMON) == null) {
			// we run in backwards compatibility mode
			try {
				ALFailureListener alFailureListener = new ALFailureListener();
				RS ci = createConfigInstance(null, params);
				ci.addListener(alFailureListener);
				ci.start();
				ci.join();
				int ciExitCode = ci.getExitStatus();
				/*
				 * set error code for the Server process if there is some error,
				 * but do not override an explicitly specified exit code
				 */
				if (mTerminationExitCode == null) {
					if (ciExitCode != 0) {
						setTerminationExitCode(ciExitCode);
					} else if (alFailureListener.anyAssemblyLineFailed()) {
						setTerminationExitCode(1);
					}
				}
			} catch (Exception error) {
				handleError("com.ibm.di.startServer", error, commandLine);
			}

		} else {
			// If we start in daemon mode start all configs
			String str = commandLineParam(CL_CONFIG);
			if (str != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				while (st.hasMoreTokens()) {
					String cfg = st.nextToken();
					getLog().info("start.server.for.config", cfg);
					try {
						startServer(cfg, null, commandLineParam(CL_PASSWORD), true, params);
					} catch (Exception error) {
						handleError("com.ibm.di.startServer", error, cfg);
					}
				}
			} else {
				autoload(true);
				autoloadScheduledConfigs();
			}

			startALsFromCommandLine();
			
			// Main loop
			getLog().info("server.enter.daemon.mode");
			if ("close".equals(commandLineParam(CL_RUN_DAEMON))) {
				System.out.close();
				System.err.close();
			}
			while (!exitRequested) {
				try {
					shutdownLatch.await();
				} catch (InterruptedException ignore) {
				}
			}
		}
		getLog().info("tdi.shutdown");

		try {
			APIEngine.serverStopped(mmServerStarted);
		} catch (Exception error) {
			getLog().info("error.send.shutdown.event", error.toString());
		}

		if (APIEngine.getTombstoneManager() != null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignore) {
			}
		}

		final Integer serverExitCode;
		if (mTerminationExitCode != null) {
			serverExitCode = mTerminationExitCode;
		} else {
			serverExitCode = 0;
		}

		com.ibm.di.store.StoreFactory.shutdown();
		invokeServerHook("TDI_Shutdown", this, serverExitCode);
		Trace.exitmax(this, "runMaster");
		System.exit(serverExitCode);
	}

	private void startDerbyServerAndExit() {
		if (StoreFactory.isDerbyNetworkDriver(StoreFactory.getJdbcDriver())) {
			try {
				StoreFactory.startDerbyServer(null, null, Boolean.getBoolean("com.ibm.di.store.sysibm"));
				System.out.println(sResHash.getString("RS.Network.Server.Started"));
				invokeServerHook("TDI_Shutdown", this, Integer.valueOf(0));
				System.exit(0);
			} catch (Exception e) {
				getLog().error("error.start.derby.server", e.toString());
				System.out.println(sResHash.getString("error.start.derby.server", e.toString()));
				e.printStackTrace();
			}
		} else {
			System.out.println(sResHash.getString("RS.Derby.Server.Only"));
		}
		invokeServerHook("TDI_Shutdown", this, Integer.valueOf(1));
		System.exit(1);
	}

	private void stopDerbyServerAndExit() {
		if (StoreFactory.isDerbyNetworkDriver(StoreFactory.getJdbcDriver())) {
			try {
				int port;
				try {
					port = Integer.valueOf(System.getProperty("com.ibm.di.store.port"));
				} catch (Exception e) {
					port = 1527;
				}
				StoreFactory.stopDerbyServer(null, port);
				System.out.println(sResHash.getString("RS.Network.Server.Stopped"));
				invokeServerHook("TDI_Shutdown", this, Integer.valueOf(0));
				System.exit(0);
			} catch (Exception e) {
				System.out.println(sResHash.getString("RS.Network.Server.Not.Stopped"));
				e.printStackTrace();
			}
		} else {
			System.out.println(sResHash.getString("RS.Derby.Server.Only"));
		}
		invokeServerHook("TDI_Shutdown", this, Integer.valueOf(1));
		System.exit(1);
	}

	/**
	 * Start AssemblyLines specified on command Line, in daemon mode.
	 * @since 7.2
	 */
	private void startALsFromCommandLine() {
		// Start AssemblyLines - command line
		String sal = commandLineParam(CL_START_AL);
		if (sal == null)
			return;

		try {
			sleep(1000); // Give the config instances some time to start up.
			String runMode = commandLineParam(CL_RUN_MODE);
			boolean simulate = commandLineParam(CL_SIMULATION_MODE) != null;

			for (String al: sal.split(",")) {

				RS rs = null;
				String alName = al;
				
				if (al.contains(":")) {
					String ns = al.substring(0, al.indexOf(':'));
					alName = al.substring(al.indexOf(':') + 1);
					rs = getServer(ns);
					if (rs == null) {
						MetamergeConfig mc = MetamergeConfigFactory.getNamespace(ns);
						if (mc != null)
							rs = getServerByConfig(mc);
					}
				} else {
					for (RS r:activeServers.values()) {
						try {
							// Check if r has the AL
							r.getMetamergeConfig().getAssemblyLine(al);
							rs = r;
							break;
						} catch (Exception err) {
							// Continue loop
							SystemFunctions.doNothing();
						}
					}
				}
				if (rs == null)
					getLog().exception("al.config.notfound", al);
				getLog().info("start.AssemblyLine", al);
				TaskCallBlock tcb = new TaskCallBlock();
				if (runMode != null)
					tcb.setRunMode(runMode);
				if (simulate)
					tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, "true");
				rs.startAL(alName, tcb);
			}
		} catch (Exception e) {
			getLog().error("error.starting.AssemblyLines", e);
			System.err.println(getLog().getString("error.starting.AssemblyLines") + ": " + e.toString());
		}

	}

	/**
	 * Users could specify additional classpath in the
	 * global.properties/solution.properties file using the property
	 * "com.ibm.di.loader.userjars". These additional jar file might contain TDI
	 * components (Connectors, Parsers, FCs...) which need to be loaded in the
	 * system namespace. This is why this method should be called once right
	 * after the properties file is read and just before the system namespaces
	 * are being registered.
	 */
	private void loadUserJars() {
		String userJarsStr = System.getProperty("com.ibm.di.loader.userjars");
		if (userJarsStr != null && userJarsStr.trim().length() > 0) {
			log.info(sResHash.getString("loading.user.jars", userJarsStr));

			// Update cache with files from user jars
			StringTokenizer classPath = new StringTokenizer(userJarsStr.trim(), File.pathSeparator);
			while (classPath.hasMoreTokens()) {
				com.ibm.di.loader.IDILoader.getInstance().addFiles(classPath.nextToken().trim());
			}
		}
	}

	private void initializeProviders() {
		String httpsHandler = System.getProperty("java.protocol.handler.pkgs", "");
		if (httpsHandler.trim().length() == 0) {
			System.setProperty("java.protocol.handler.pkgs", "com.ibm.net.ssl.www2.protocol");
		} else if (!httpsHandler.contains("com.ibm.net.ssl.www2.protocol")) {
			System.setProperty("java.protocol.handler.pkgs", httpsHandler + " | com.ibm.net.ssl.www2.protocol");
		}
		// End #11625
	}

	/**
	 * This code attempts to start the derby server if the user has specified
	 * automatic start and the driver is configured to be a derby network
	 * driver. Not such a good thing actually - because its possible that the
	 * derby URL is referring a remote CS server. In that case starting a CS
	 * server locally is of no need. But if user has set it to "automatic" we
	 * are still starting it.
	 */
	private void autoStartCloudScape() {
		if ("automatic".equalsIgnoreCase(System.getProperty("com.ibm.di.store.start.mode"))
				&& StoreFactory.isDerbyNetworkDriver(StoreFactory.getJdbcDriver())) {
			try {
				StoreFactory.startDerbyServer(System.getProperty("com.ibm.di.store.hostname"), System
						.getProperty("com.ibm.di.store.port"), Boolean.getBoolean("com.ibm.di.store.sysibm"));
			} catch (Exception e) {
				getLog().error("error.start.derby.server", e.toString());
			}
		}
	}

	private void handleError(String id, Throwable error, Object info) {
		logmsg(sResHash.getString("MISERVER.RS.ID.ERROR", new Object[] { id, error }));
		log.info("handle.error.user", info.toString());
		error.printStackTrace();
	}

	private void usage(int status) {
		System.out.println(sResHash.getString("RS.USAGE.IBMDISERVER"));
		invokeServerHook("TDI_Shutdown", this, Integer.valueOf(status));
		System.exit(status);
	}

	/**
	 * Returns the value for a command line parameter
	 * 
	 * @param param
	 *            is switch which value to look for.
	 * @return the value of a switch, or <code>null</code> if it does not exist
	 *         in the map.
	 * 
	 */
	public String commandLineParam(String param) {
		String value = null;
		Object obj = getParamObj(param);
		if (obj instanceof String) {
			value = (String) obj;
		}
		return value;
	}

	/**
	 * @param param
	 *            Name of startup parameter.
	 * @return The value of the startup parameter. Use
	 *         {@link #commandLineParam(String)} if you expect a String value.
	 */
	private Object getParamObj(String param) {
		return params.get(param);
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig getMetamergeConfig() {
		return serverConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMetamergeConfig(MetamergeConfig config) {
		serverConfig = config;
	}

	/**
	 * Returns the Log for the current instance
	 * 
	 * @return the main thread's Log object
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * Gets the exit code of the instance
	 * 
	 * @return the exit code.
	 */
	public int getExitStatus() {
		return exitCode;
	}

	/**
	 * Returns the exit error of the instance
	 * 
	 * @return the exception object of the RS instance (in case it failed)
	 */
	public Throwable getExitError() {
		return exitError;
	}

	/**
	 * Gets the server instance that uses the specified by the
	 * <code>aConfig</code> configuration.
	 * 
	 * @param aConfig
	 *            the configuration object used by the server instance
	 * @return The {@link RS} object that correspond to the provided
	 *         configuration, or null if it couldn't be found.
	 */
	public static RS getServerByConfig(MetamergeConfig aConfig) {
		RS rs = null;
		synchronized (activeServers) {
			java.util.Enumeration<RS> enumServers = activeServers.elements();
			while (enumServers.hasMoreElements()) {
				RS server = enumServers.nextElement();
				if (server.serverConfig == aConfig) {
					rs = server;
					break;
				}
			}
		}

		return rs;
	}

	/**
	 * Returns the RS instance associated with the current ThreadGroup. Although
	 * this method is public, it is meant for internal use. The usual way to get
	 * the current RS instance would be to use the <code>main</code> object in
	 * JavaScript.
	 * 
	 * @return the {@link RS} instance or <code>null</code> if it couldn't be
	 *         found, e.g. because the current Thread was not created by the TDI
	 *         framework.
	 */
	public static RS getServer() {
		Thread currentThread = Thread.currentThread();
		String name = currentThread.getThreadGroup().getName();
		RS rs = getServer(name);

		// if the thread comes from a non-RS context (e.g. RMI) try to get the
		// RS context through AL/ALWorker/Scheduler/Sequence object
		try {
			if (rs == null) {
				RSInterface parentThread = null;
				if (currentThread instanceof AssemblyLine) {
					parentThread = ((AssemblyLine) currentThread).getParent();
				} else if (currentThread instanceof AssemblyLinePool.ALWorker) {
					AssemblyLinePool pool = ((AssemblyLinePool.ALWorker) currentThread).getParent();
					if (pool != null)
						parentThread = pool.getParent();
				} else if (currentThread instanceof Scheduler) {
					return ((Scheduler) currentThread).getRS();
				} else if (currentThread instanceof Sequence) {
					return ((Sequence) currentThread).getParent();
					}

				if (parentThread instanceof RS)
					return (RS) parentThread;

				if (parentThread instanceof Thread) {
					ThreadGroup parentThreadGroup = ((Thread) parentThread).getThreadGroup();
					if (parentThreadGroup != null) {
						rs = getServer(parentThreadGroup.getName());
					}
				}
			}
		} catch (Exception e) {
			if (gRS != null && gRS.getLog() != null) {
				gRS.getLog().error("error.obtain.rs.context", e.toString());
			}
		}

		return rs;
	}

	/**
	 * Returns a named {@link RS} instance
	 * 
	 * @param name
	 *            the name of the instance
	 * @return the {@link RS} if it is found, <code>null</code> otherwise.
	 */
	public static RS getServer(String name) {
		// already synchronized... no need to lock.
		return activeServers.get(name);
	}

	/**
	 * Registers the config object to be associated with the current ThreadGroup
	 * 
	 * @param server
	 *            the server to be registered, should not be <code>null</code>
	 */
	public static synchronized void registerServer(RS server) {
		String name = server.getThreadGroup().getName();

		// already synchronized... no need to lock.
		activeServers.put(name, server);
		try {
			APIEngine.configInstanceStarted(server);
		} catch (DIException e) {
			gRS.getLog().info("cannot.register.server", e.toString());
		}
		gRS.getLog().info("register.server", name);
	}

	/**
	 * Registers the config object to be associated with the current ThreadGroup
	 * 
	 * @param server
	 *            the server to unregister, should not be <code>null</code>
	 */
	public static synchronized void unregisterServer(final RS server) {
		String name = server.getThreadGroup().getName();
		gRS.getLog().info("unregister.server", name);

		// already synchronized... no need to lock.
		activeServers.remove(name);
		MetamergeConfigFactory.unregisterNamespace(server.getMetamergeConfig());
		try {
			APIEngine.configInstanceStopped(server);
		} catch (DIException e) {
			gRS.getLog().info("cannot.unregister.server", e.toString());
		}
		server.ciEventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<ConfigInstanceListener>() {
			public void visit(ConfigInstanceListener listener) {
				listener.configInstanceStopped(server);
			}
		});
	}

	/**
	 * Starts server using <code>null</code> for the unique name of the server
	 * instance and command line parameters parsed to params table.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var cmdline = &quot;-&quot;+com.ibm.di.server.RS.CL_CONFIG;
	 * cmdline += &quot; test_config.xml -&quot;
	 * cmdline += com.ibm.di.server.RS.CL_START_AL+&quot; al1, al2&quot;
	 * 
	 * var srv = main.startServer(cmdline);
	 * </pre>
	 * 
	 * @param args
	 *            the "command line arguments" to use when starting.
	 * 
	 * @return The new {@link RS} instance
	 * @exception Exception
	 *                if an error occurs.
	 */
	public static RS startServer(String[] args) throws Exception {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		params.putAll(parseCommandline(args));
		return startServer(null, params);
	}

	/**
	 * Starts server using <code>null</code> for the unique name of the server
	 * instance. This method starts specified assembly lines from specified
	 * configuration.
	 * <p>
	 * The server started with this method shutdowns after the assembly line has
	 * finished and it can only start configurations not protected with
	 * passwords.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var config = &quot;//configs//test_config.xml&quot;
	 * var srv = main.startServer(config, &quot;al2&quot;, null);
	 * </pre>
	 * 
	 * @param config
	 *            the configuration to load
	 * @param assemblyLines
	 *            the assembly lines to start
	 * @return the new {@link RS} instance object
	 * @throws Exception
	 *             if the server initialization fails
	 */
	public static RS startServer(String config, String assemblyLines) throws Exception {
		return startServer(config, assemblyLines, null);
	}

	/**
	 * Starts server using <code>null</code> for the unique name of the server
	 * instance. This method starts specified assembly lines from specified
	 * configuration.
	 * <p>
	 * The server started with this method shutdowns after the assembly line has
	 * finished.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var config = &quot;/configs/test_config.xml&quot;
	 * var pass = &quot;secret&quot;;
	 * 
	 * var srv = main.startServer(config, &quot;al2&quot;, null, pass);
	 * </pre>
	 * 
	 * @param config
	 *            the configuration to load
	 * @param assemblyLines
	 *            the assembly lines to start
	 * @param passw
	 *            the password used for the configuration file
	 * 
	 * @return the new {@link RS} instance object
	 * @throws Exception
	 *             if the server initialization fails
	 */
	public static RS startServer(String config, String assemblyLines, String passw) throws Exception {
		return startServer(config, assemblyLines, passw, false);
	}

	/**
	 * Starts server using <code>null</code> for the unique name of the server
	 * instance. This method starts specified assembly lines from specified
	 * configuration.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var config = &quot;/configs/test_config.xml&quot;
	 * var pass = &quot;secret&quot;;
	 * 
	 * var srv = main.startServer(config, &quot;al1&quot;, null, pass, true);
	 * srv.startAL(&quot;al2&quot;);
	 * </pre>
	 * 
	 * @param config
	 *            the configuration to load
	 * @param assemblyLines
	 *            the assembly lines to start
	 * @param passw
	 *            the password used for the configuration file
	 * @param dontTerminate
	 *            whether to wait instead of shutting down the server after it
	 *            has finished
	 * 
	 * @return the new {@link RS} instance object
	 * @throws Exception
	 *             if the server initialization fails
	 * 
	 */
	public static RS startServer(String config, String assemblyLines, String passw, boolean dontTerminate) throws Exception {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if (assemblyLines != null)
			params.put(CL_START_AL, assemblyLines);
		if (passw != null)
			params.put(CL_PASSWORD, passw);
		if (dontTerminate)
			params.put(CL_NO_TERMINATE, "");

		params.put(CL_CONFIG, config);

		return startServer(null, params);
	}

	/**
	 * Starts server using <code>null</code> for the unique name of the server
	 * instance. This method includes -D option for autostarting if user has
	 * specified it.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var config = &quot;/configs/test_config.xml&quot;
	 * var pass = &quot;secret&quot;;
	 * var userParam = new java.util.Hashtable();
	 * userParam.put(com.ibm.di.server.CL_NO_AUTOSTART, &quot;true&quot;);
	 * 
	 * var srv = main.startServer(config, &quot;al1&quot;, null, pass, true, userParam);
	 * srv.startAL(&quot;al2&quot;);
	 * </pre>
	 * 
	 * @param config
	 *            the configuration to load
	 * @param assemblyLines
	 *            the assembly lines to start
	 * @param passw
	 *            the password used for the configuration file
	 * @param dontTerminate
	 *            whether to wait instead of shutting down the server after it
	 *            has finished
	 * @param userParams
	 *            checks this map if the {@link #CL_NO_AUTOSTART} exists.
	 * 
	 * @return the new {@link RS} instance object
	 * @throws Exception
	 *             if the server initialization fails
	 */
	public static RS startServer(String config, String assemblyLines, String passw, boolean dontTerminate,
			Hashtable<String, Object> userParams) throws Exception {
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		if (assemblyLines != null)
			params.put(CL_START_AL, assemblyLines);
		if (passw != null)
			params.put(CL_PASSWORD, passw);
		if (dontTerminate)
			params.put(CL_NO_TERMINATE, "");

		params.put(CL_CONFIG, config);

		if (userParams.get(CL_NO_AUTOSTART) != null) {
			params.put(CL_NO_AUTOSTART, "");
		}

		return startServer(null, params);
	}

	/**
	 * Starts a new server instance.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var par = new java.util.Hashtable();
	 * par.put(com.ibm.di.server.RS.CL_CONFIG, &quot;tast_config.xml&quot;);
	 * par.put(com.ibm.di.server.RS.CL_START_AL, &quot;al1&quot;);
	 * par.put(com.ibm.di.server.RS.CL_AL_DEBUG, &quot;true&quot;);
	 * 
	 * var srv = main.startServer(null, par);
	 * </pre>
	 * 
	 * @param groupName
	 *            The unique name for the server instance
	 * @param params
	 *            Hashtable of assembly lines to start (same syntax as command
	 *            line)
	 * 
	 * @return the new {@link RS} instance object
	 * @exception Exception
	 *                if there is another instance using the same groupName
	 */
	public static synchronized RS startServer(String groupName, Hashtable<String, Object> params) throws Exception {
		RS ci = createConfigInstance(groupName, params);
		ci.start();
		return ci;
	}

	/**
	 * Create a new config instance but do not start it.
	 * 
	 * @param groupName
	 *            The unique name for the server instance
	 * @param params
	 *            Hashtable of assembly lines to start (same syntax as command
	 *            line)
	 * 
	 * @return the new {@link RS} instance object
	 * @exception Exception
	 *                if there is another instance using the same groupName
	 */
	public static synchronized RS createConfigInstance(String groupName, Hashtable<String, Object> params) throws Exception {

		String name = configInstanceNamingPolicy.getConfigInstanceName(params);

		if (name == null) {
			gRS.getLog().exception("no.config.or.group");
		}

		// already synchronized... no need to lock.
		if (activeServers.get(name) != null) {
			gRS.getLog().exception("instance.already.running", name);
		}

		ThreadGroup group = new ThreadGroup(masterGroup, name);
		RS server = new RS(group, name);

		server.params = params;
		server.setName(name);

		registerServer(server);

		return server;
	}

	/**
	 * @return the identifier of the loaded configuration
	 */
	public String getCommandLineConfigId() {
		return mCommandLineConfigId;
	}

	/**
	 * This method looks for config files in the directory specified by the java
	 * system property com.ibm.di.server.autoload. Every *.xml file in the
	 * directory is loaded and started as an instance.
	 * 
	 * @param continueOnError
	 *            If true, the load process will continue even if there is a
	 *            problem with one of the config files
	 */
	private void autoload(boolean continueOnError) {
		String dir = System.getProperty("com.ibm.di.server.autoload");
		if (dir == null || dir.trim().equals("")) {
			log.info("no.autload.directory");
			return;
		}

		File ald = new File(dir);
		if (!ald.isDirectory()) {
			log.info("autoload.no.directory", ald.getAbsolutePath());
			return;
		}

		log.info("autoload.search.directory", dir);
		File[] files = new File(dir).listFiles(new XMLFilter());
		for (int i = 0; i < files.length; i++) {
			try {
				log.info("autoload.file.load", files[i].getAbsolutePath());
				startServer(files[i].getAbsolutePath(), null, commandLineParam(CL_PASSWORD), true);
			} catch (Exception error) {
				handleError("com.ibm.di.startServer", error, files[i].getAbsolutePath());
				if (!continueOnError)
					return;
			}
		}
	}

	/**
	 * This method loads the configurations that have one or more active schedules
	 * but only if it is not running already.
	 */
	private void autoloadScheduledConfigs() {
		for(File file : APIEngine.getConfigurationRegistry().getConfigsWithActiveSchedules()) {
			try {
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put(CL_CONFIG, file.getAbsolutePath());
				String passw = commandLineParam(CL_PASSWORD);
				if (passw != null)
					params.put(CL_PASSWORD, passw);
				String name = configInstanceNamingPolicy.getConfigInstanceName(params);
				if(getServer(name) == null) {
					log.info("autoload.file.load", file.getAbsolutePath());
					startServer(file.getAbsolutePath(), null, commandLineParam(CL_PASSWORD), true);
				}
			} catch (Exception error) {
				handleError("com.ibm.di.startServer", error, file.getAbsolutePath());
			}
		}
	}

	/**
	 * Read and execute all files in the serverhooks directory.
	 */
	private void initializeServerHooks() throws Exception {
		File hooksDir = new File("serverhooks");
		if (!hooksDir.exists() || !hooksDir.isDirectory())
			return;

		List<String> list = Arrays.asList(hooksDir.list());
		if (list.size() == 0)
			return;

		Collections.sort(list);

		if (se == null) {
			se = new ScriptEngine(null);
			se.declareUserFunctions();
		}

		for (Iterator<String> i = list.iterator(); i.hasNext();) {
			String name = i.next();
			File f = new File(hooksDir, name);
			try {
				if (f.isFile())
					se.includeScript(f.getAbsolutePath());
			} catch (Exception err) {
				getLog().error("error.execute.hook.file", name, err.toString());
			}
		}
	}

	/**
	 * Invokes a server hook.
	 * 
	 * @param name
	 *            The name of the hook
	 * @param caller
	 *            The object invoking the hook
	 * @param userInfo
	 *            Arbitrary information to the hook from the caller
	 * @return The result from the function call or <code>null</code> if a hook
	 *         with that name could not be found.
	 */
	public Object invokeServerHook(String name, Object caller, Object userInfo) {
		if (se != null) {
			Entry scriptObject = new Entry();
			scriptObject.setAttribute("InternalHookName", name);
			synchronized (se) {
				try {
					se.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);
					return se.call(name, new Object[] { this, caller, userInfo }, true);
				} catch (Exception err) {
					getLog().error("error.calling.hook", name, err.toString());
				}
			}
		}
		return null;
	}

	private static class XMLFilter implements FileFilter {

		/**
		 * {@inheritDoc}
		 */
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return false;
			return pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
		}
	}

	private void initializeConnectorPools(MetamergeConfig aServerConfig) {
		try {
			MetamergeFolder connectorFolder = aServerConfig.getDefaultFolder(MetamergeConfig.CONNECTOR_FOLDER);
			Enumeration<?> connectorConfigEnum = connectorFolder.list();
			if (connectorConfigEnum == null) {
				return;
			}

			while (connectorConfigEnum.hasMoreElements()) {
				javax.naming.Binding connBind = (javax.naming.Binding) connectorConfigEnum.nextElement();

				String connName = connBind.getName();
				ConnectorConfig connConfig = (ConnectorConfig) connBind.getObject();

				PoolDefConfig poolDefConfig = connConfig.getPoolDefConfig();
				if (poolDefConfig != null && poolDefConfig.getPoolEnabled()) {
					try {
						ConnectorPool connPool = new ConnectorPool(connName, poolDefConfig, logConnPool);

						connectorPools.put(connName, connPool);
					} catch (Exception e) {
						log.error("could.not.create.connector.pool", connName, e.toString());
					}
				}
			}
		} catch (Exception e) {
			log.error("error.init.connector.pool", e.toString());
		}
	}

	/**
	 * 
	 * @return an Array of String objects containing the names of the
	 *         {@link ConnectorPool}s
	 */
	public String[] getConnectorPoolNames() {
		if (connectorPools == null) {
			return new String[0];
		}

		Object[] poolKeySet = connectorPools.keySet().toArray();
		String[] poolNames = new String[poolKeySet.length];
		for (int i = 0; i < poolKeySet.length; i++) {
			poolNames[i] = (String) poolKeySet[i];
		}

		return poolNames;
	}

	/**
	 * 
	 * @param connName
	 *            the name of the {@link ConnectorPool} object
	 * @return the {@link ConnectorPool} instance that corresponds of the
	 *         provided name, if the name is not found <code>null</code> is
	 *         returned.
	 */
	public ConnectorPool getConnectorPool(String connName) {
		return connectorPools.get(connName);
	}

	private void terminateConnectorPools() {
		if (connectorPools == null) {
			return;
		}

		Iterator<ConnectorPool> pools = connectorPools.values().iterator();
		while (pools.hasNext()) {
			ConnectorPool pool = pools.next();
			try {
				pool.terminate();
			} catch (Exception e) {
				log.error("error.terminate.connector.pool", e.toString());
			}
		}
	}

	private String verifyValue(String propName, String[] legalValues) {
		String ret = System.getProperty(propName);
		if (ret == null)
			return ret;

		for (int i = 0; i < legalValues.length; i++) {
			if (ret.equalsIgnoreCase(legalValues[i]))
				return legalValues[i];
		}

		log.warn(sResHash.getString("RS.DO.NOT.RECOGNIZE.PROPERTY.VALUE", new Object[] { propName, ret, legalValues[0] }));

		return legalValues[0];
	}

	/**
	 * Overrides the file paths associated with certain property stores. Thus at
	 * runtime property stores can be redirected to load their contents from
	 * different files than those specified in the configuration file. The
	 * functionality is driven by the {@link #CL_EXT_PROP_FILE} parameter.
	 */
	private void overridePropertyStores() {

		/*
		 * Check if the override is triggered by a command-line parameter at
		 * Server startup.
		 */
		if (gRS.params.containsKey(CL_EXT_PROP_FILE)) {
			String commLineConfig = gRS.commandLineParam(CL_CONFIG);
			String currConfig = commandLineParam(CL_CONFIG);

			if (commLineConfig != null && commLineConfig.indexOf(currConfig) != -1) {
				this.params.put(CL_EXT_PROP_FILE, gRS.params.get(CL_EXT_PROP_FILE));
			}
		}

		if (this.params.containsKey(CL_EXT_PROP_FILE)) {
			try {

				PropertyManager propManager = (PropertyManager) getConfiguration(MetamergeConfig.DEFAULT_PROPSTORE_FOLDER);

				String commandValues = commandLineParam(CL_EXT_PROP_FILE);
				StringTokenizer configPairsTokenizer = new StringTokenizer(commandValues, ",");

				Hashtable<String, String> configPairs = new Hashtable<String, String>();
				while (configPairsTokenizer.hasMoreTokens()) {
					String token = configPairsTokenizer.nextToken();
					StringTokenizer keyValuePairs = new StringTokenizer(token, "=");
					String extProp = keyValuePairs.nextToken();

					PropertyStoreConfig propStoreConfig = propManager.getPropertyStore(extProp);
					if (propStoreConfig != null) {
						try {
							String newFile = keyValuePairs.nextToken();

							if (configPairs.containsKey(extProp)) {
								getLog().info("external.property.overwritten", extProp);
							}
							configPairs.put(extProp, newFile);

							RawConnectorConfig rawConnectorConfig = propStoreConfig.getConnectionConfig();
							rawConnectorConfig.setParameter("collection", newFile);
						} catch (Exception ex) {
							getLog().info("no.external.file.specified.f.param");
						}
					} else {
						getLog().info("no.prop.store.in.cl.config", extProp, mCommandLineConfigId);
					}
				}
			} catch (Exception exception) {
				getLog().info("error.proccessing.cl.param.f");
			}
		}
	}

	/**
	 * Load the dynamically discovered components from IDILoader
	 */
	private void loadSystemNamespace() {
		if (gSysConfig != null)
			return; // We already have it

		getLog().debug("loading.system.templates");
		gSysConfig = MetamergeConfigFactory.createSysInstance(com.ibm.di.loader.IDILoader.getAllSysConfigs());
		MetamergeConfigFactory.registerNamespace(MetamergeConfigFactory.SYSTEM_NAMESPACE, gSysConfig);
		SystemFunctions.setupSystemConnectorInheritance();
	}

	/**
	 * Check if the RuntimeEnvironment could be found and if available use it to
	 * find osgi's IntegrationComponent services.
	 * 
	 * @param mc
	 */
	private void attachIntegrationComponents(MetamergeConfig mc) {
		try {
			Class<?> reClass = Class.forName("com.ibm.di.osgi.RuntimeEnvironment");
			reClass.getMethod("attachIntegrationComponentConfigs", new Class[] { MetamergeConfig.class }).invoke(null, mc);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A policy which defines how configuration instances are named. The policy
	 * should be predictable - it should always return the same name for the
	 * same parameters.
	 * 
	 * @since 7.0
	 */
	public static interface ConfigInstanceNamingPolicy {

		/**
		 * @param params
		 *            configuration instance start parameters
		 * @return the configuration instance name
		 * @throws Exception
		 *             error while determining the name of the configuration
		 *             instance
		 */
		String getConfigInstanceName(java.util.Map<String, Object> params) throws Exception;
	}

	/**
	 * The default naming policy. If the instance is being started with an
	 * explicit <code>CL_INTERNAL_CONFIG_NSTANCE_NAME</code> parameter, the
	 * value of that parameter is used as the configuration instance name.
	 * Otherwise the canonical file system path of the configuration file is
	 * used as the name of the configuration instance.
	 * 
	 * @since 7.0
	 */
	public static class DefaultConfigInstanceNamingPolicy implements ConfigInstanceNamingPolicy {

		public String getConfigInstanceName(java.util.Map<String, Object> params) throws IOException {

			String name = (String) params.get(CL_INTERNAL_CONFIG_NSTANCE_NAME);
			if (name == null) {
				name = (String) params.get(CL_CONFIG_STDIN);
				if (name == null) {
					name = (String) params.get(CL_CONFIG);
				}
				if (name != null) {
					name = (new java.io.File(name)).getCanonicalPath();
				}
			}

			return name;
		}
	}

	/**
	 * @return The configuration instance naming policy of the Server.
	 * @since 7.0
	 */
	public static synchronized ConfigInstanceNamingPolicy getConfigInstanceNamingPolicy() {
		return configInstanceNamingPolicy;
	}

	/**
	 * @param newPolicy
	 *            A new configuration instance naming policy for the Server.
	 * @since 7.0
	 */
	public static synchronized void setConfigInstanceNamingPolicy(ConfigInstanceNamingPolicy newPolicy) {
		configInstanceNamingPolicy = newPolicy;
	}

	/**
	 * Initialize the configuration instance - parse the configuration xml file,
	 * populate internal structures (e.g. Connector Pools), etc.
	 * 
	 * @return Zero if initialization succeeded, non-zero otherwise.
	 */
	private int initializeConfigInstance() {

		redirectConsoleOutput();

		// Obtain the configuration file ( defaults to "rs.xml" in current
		// directory )
		try {
			if (commandLineParam(CL_CONFIG_STDIN) != null) {
				configPath = "<stdin>";
			} else if (commandLineParam(CL_CONFIG) != null) {
				File cfp = new File(commandLineParam(CL_CONFIG));
				configPath = cfp.getCanonicalPath();
			} else if (commandLineParam(CL_INTERNAL_CONFIG_AS_STRING) != null) {
				configPath = null;
			} else {
				File cfp = new File("rs.xml");
				if (!cfp.exists()) {
					cfp = new File("rs.cfg");
					if (!cfp.exists()) {
						getLog().error("error.no.config.file");
						System.err.println(getLog().getString("error.no.config.file"));
						Trace.exitmin(this, "runServer");
						return 1;
					}
				}
				configPath = cfp.getCanonicalPath();
			}
		} catch (IOException e) {
			exitError = e;
			getLog().fatal("error.loading.config.file", configPath, e);
			System.err.println(getLog().getString("error.loading.config.file", configPath) + "\n" + e.getMessage());
			return (1);
		}

		showLogHeader(log);

		mCommandLineConfigId = commandLineParam(CL_CONFIG_STDIN);
		if (mCommandLineConfigId == null) {
			mCommandLineConfigId = configPath;
		}

		// Load configuration
		try {
			reload();
		} catch (Exception e) {
			String arg = commandLineParam(CL_CONFIG);
			if (arg != null && arg.indexOf(',') >= 0) {
				getLog().fatal("error.loading.multiple.config.files", arg);
				System.err.println(getLog().getString("error.loading.multiple.config.files", arg));
			} else {
				exitError = e;
				getLog().fatal("error.loading.config.file", configPath, e);
				System.err.println(getLog().getString("error.loading.config.file", configPath) + "\n" + e.getMessage());
			}
			return (1);
		}

		getLog().info("GENERATED.CONFIG.ID.FOR.CONFIG.INSTANCE", configPath, this.getName());

		overridePropertyStores();

		// System Properties
		try {
			SystemFunctions.setSystemProperties(getMetamergeConfig(), /*
																	 * included
																	 * configs
																	 */
			true);
		} catch (Exception ex) {
			exitError = ex;
			getLog().error("error.setting.properties", ex);
			System.err.println(getLog().getString("error.setting.properties") + ": " + ex.toString());
			return 9;
		}

		// Dump a list of all system properties ?
		if (commandLineParam(CL_DUMP_PROPS) != null) {
			System.out.println(getLog().getString("dump.system.properties"));
			System.getProperties().list(System.out);
			System.out.println(sResHash.getString("RS.ASTERISKS"));
		}

		// Specific logging for this instance
		try {
			LogConfig logconfig = (LogConfig) getMetamergeConfig().lookup(
					MetamergeConfig.DEFAULT_SERVER_FOLDER + "/" + MetamergeConfig.DEFAULT_SERVER_LOG);
			if (logconfig.getItems().size() > 0)
				com.ibm.di.log.LogUtils.addLoggers("", "server", log, logconfig, this);

		} catch (Exception error) {
			exitError = error;
			getLog().error("error.setting.log", error);
			return 9;
		}

		// Null value behavior
		nullBehavior = verifyValue("rsadmin.attribute.nullBehavior", ServerConstants.NVB_BEHAVIOR);
		nullBehaviorValue = System.getProperty("rsadmin.attribute.nullBehaviorValue");

		// Null value definition
		nullDefinition = verifyValue("rsadmin.attribute.nullDefinition", ServerConstants.NVD_DEFINITION);
		nullDefinitionValue = System.getProperty("rsadmin.attribute.nullDefinitionValue");

		// Initialize Connector Pools
		initializeConnectorPools(serverConfig);

		/*
		 * TODO // Load user defined parameter resources -
		 * remove.rsConfiguration.initializeUserResource();
		 */

		// Execute script file?
		String scriptFile = commandLineParam(CL_EXECUTE_SCRIPT);
		if (scriptFile != null) {
			try {
				if (se == null) {
					se = new ScriptEngine(null);
					se.declareUserFunctions();
				}
				synchronized (se) {
					se.includeScript(scriptFile);
				}
			} catch (Exception error) {
				error.printStackTrace();
			}
		}

		// Check that properties used as parameters exist.
		checkProperties(getMetamergeConfig());
		
		// Check that referenced files exists.
		checkFileReferences(getMetamergeConfig());

		return 0;
	}

	private void redirectConsoleOutput() {
		// Get log file path and set the global System.out and System.err instances
		String path = commandLineParam(CL_LOGFILE);
		if (path != null && path.compareTo("-") != 0) {
			try {
				// Enable auto-flush to ensure output from Assembly Line threads is written immediately
				PrintStream ps = new PrintStream(new FileOutputStream(path), true);
				System.setOut(ps);
				System.setErr(ps);
			} catch (Exception e) {
				exitError = e;
				System.err.println(getLog().getString("error.setting.logfile", e.toString()));
			}
		}
	}

	/**
	 * <p>
	 * This method is for internal use only. Users must not rely on it.
	 * </p>
	 * 
	 * <p>
	 * Wait for the configuration instance (a.k.a <code>RS</code> instance) to
	 * complete its initialization. Normally the initialization procedure of a
	 * config instance involves activities such as parsing the configuration xml
	 * file and preparing internal structures like Connector Pools. You may care
	 * about initialization status, because it is not a good idea to start
	 * AssemblyLines on the RS instance before initialization is complete. Note
	 * that when initialization is complete the <code>RS</code> instance might
	 * be in error state.
	 * </p>
	 * 
	 * @param milliseconds
	 *            Timeout in milliseconds to wait for the initialization
	 *            completion. If the time is less than or equal to zero, the
	 *            method will not wait at all.
	 * 
	 * @return <code>true</code> if the count reached zero and
	 *         <code>false</code> if the waiting time elapsed before the count
	 *         reached zero.
	 * 
	 * @throws InterruptedException
	 *             If the calling thread is interrupted while waiting for
	 *             initialization status.
	 * @since 7.0
	 */
	public boolean waitForInitializationToComplete(long milliseconds) throws InterruptedException {
		return initializationLatch.await(milliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineStarted(final AssemblyLine al) {
		try {
			APIEngine.assemblyLineStarted(al);
		} catch (DIException e) {
			// throwing externalized message.
			getLog().logerror(e.getMessage(), e);
		}

		ciEventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<ConfigInstanceListener>() {
			public void visit(ConfigInstanceListener listener) {
				listener.assemblyLineStarted(al);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineCycleEnded(AssemblyLine al, Entry work) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void assemblyLineTerminated(final AssemblyLine al) {
		APIEngine.assemblyLineTerminated(al);
		ciEventSource.visitListeners(new ThreadSafeListenableImpl.Visitor<ConfigInstanceListener>() {
			public void visit(ConfigInstanceListener listener) {
				listener.assemblyLineStopped(al);
			}
		});
	}

	/**
	 * <p>
	 * This method is for internal use only. Users must not rely on it.
	 * </p>
	 * 
	 * <p>
	 * Register listener to this configuration instance.
	 * </p>
	 * 
	 * @param listener
	 *            Listener for configuration instance notifications.
	 * 
	 * @since 7.0
	 */
	public void addListener(ConfigInstanceListener listener) {
		ciEventSource.addListener(listener);
	}

	/**
	 * <p>
	 * This method is for internal use only. Users must not rely on it.
	 * </p>
	 * 
	 * <p>
	 * Unregister listener from this configuration instance.
	 * </p>
	 * 
	 * @param listener
	 *            Registered listener.
	 * 
	 * @return the actual listener being removed. Note this might differ from
	 *         the passed in which is only used for identification. This makes
	 *         it easy to properly close the actual listener.
	 * @since 7.0
	 */
	public ConfigInstanceListener removeListener(ConfigInstanceListener listener) {
		return ciEventSource.removeListener(listener);
	}

	/**
	 * This method shuts down all Config Instances.
	 * 
	 * @param exitCode
	 *            The exit code to use
	 * @param master
	 *            If true, also stop the master server
	 * @param async
	 *            If true, crate new Threads to wait for the AssemblyLines to
	 *            stop
	 * @since 7.1
	 */
	public static void shutdownAllServers(int exitCode, boolean master, boolean async) {
		// clone the list of RSs because a concurrent modification exception
		// will be thrown when starting modification of it in the bellow
		// for-loop block.
		List<RS> clone = null;

		synchronized (activeServers) {
			// sync the access to the list of RSs to ensure no one is modifying
			// while cloning
			clone = new ArrayList<RS>(activeServers.values());
		}

		for (RS rs : clone) {
			rs.shutdownServer(exitCode, async);
		}
		if (master && gRS != null)
			gRS.shutdownServer(exitCode);
	}

	/**
	 * Observe the AssemblyLines executed from a given config instance and
	 * detect if any of them failed with an exception.
	 * 
	 * @since 7.1
	 */
	private static class ALFailureListener implements ConfigInstanceListener {

		private boolean anyFailed = false;

		public synchronized boolean anyAssemblyLineFailed() {
			return anyFailed;
		}

		public synchronized void assemblyLineStarted(AssemblyLine assemblyLine) {
		}

		public synchronized void assemblyLineStopped(AssemblyLine assemblyLine) {
			if (assemblyLine.getStats() != null && assemblyLine.getStats().getError() != null) {
				anyFailed = true;
			}
		}

		public synchronized void configInstanceStarted(RSInterface configInstance) {
		}

		public synchronized void configInstanceStopped(RSInterface configInstance) {
		}
	}
	
	/**
	 * Checks that properties used as parameters in the configuration exist.
	 * Warning messages will be logged by the config subsystem for properties that do not exist.
	 * @param bc The configuration to check
	 */
	private void checkProperties(BaseConfiguration bc) {
		if (bc == null)
			return;
		for (String key: bc.getKeys(BaseConfiguration.ONE_LEVEL)) {
			String pps = bc.getParameterPropertySource(key);
			if (pps == null)
				continue;
			// Verify that we can evaluate any property.
			if (pps.startsWith("{property")) //$NON-NLS-1$
				bc.getParameter(key);
		}

		try {
			if (bc instanceof MetamergeConfig) {
				checkProperties(((MetamergeConfig) bc).getDefaultFolder(MetamergeConfig.ASSEMBLYLINE_FOLDER));
			} else if (bc instanceof MetamergeFolder) {
				Enumeration<Binding> e = ((MetamergeFolder) bc).list();
				while (e.hasMoreElements())
					checkProperties((BaseConfiguration)e.nextElement().getObject());
			} else if (bc instanceof AssemblyLineConfig) {
				AssemblyLineConfig alc = (AssemblyLineConfig) bc;
				List<BaseConfiguration> assemblylineItems = alc.getEntryFeedComponents().getConfigurations(null);
				alc.getDataFlowComponents().getConfigurations(assemblylineItems);
				for (BaseConfiguration b : assemblylineItems)
					checkProperties(b);
			} else if (bc instanceof ConnectorConfig) {
				checkProperties(((ConnectorConfig)bc).getConnectionConfig());
				checkProperties(((ConnectorConfig)bc).getParserConfig());
				if (bc instanceof FunctionConfig)
					checkProperties(((FunctionConfig)bc).getFunctionConfig());
				checkProperties(((ConnectorConfig)bc).getAttributeMap(true));
				checkProperties(((ConnectorConfig)bc).getAttributeMap(false));
			} else if (bc instanceof AttributeMapConfig) {
				for (String name: bc.getChildNames()) {
					if (bc.hasParameter(name))
						checkProperties(bc.getChild(name));
				}
			} else if (bc instanceof LoopConfig && ((LoopConfig)bc).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
					checkProperties(((LoopConfig)bc).getLoopConnector());
			}
		} catch (Exception e){
			SystemFunctions.doNothing();
		}
	}
	
	private void checkFileReferences(MetamergeConfig mc) {
		try {
			MetamergeFolder folder = mc.getDefaultFolder(MetamergeConfig.SCRIPT_FOLDER);
			Enumeration<Binding> e = ((MetamergeFolder) folder).list();
			while (e.hasMoreElements()) {
				ScriptConfig bc = (ScriptConfig)e.nextElement().getObject();
				String includes = bc.getIncludeFiles();
				if (includes != null) {
					for (String s: includes.split("\n"))
						checkFileReference(s, MetamergeConfig.DEFAULT_SCRIPT_FOLDER, bc);
				}
			}
		} catch (Exception e){
			SystemFunctions.doNothing();
		}

		try {
			MetamergeFolder folder = mc.getDefaultFolder(MetamergeConfig.NAMESPACE_FOLDER);
			Enumeration<Binding> e = ((MetamergeFolder) folder).list();
			while (e.hasMoreElements()) {
				NamespaceConfig bc = (NamespaceConfig)e.nextElement().getObject();
				checkFileReference(bc.getURL(), MetamergeConfig.DEFAULT_NAMESPACE_FOLDER, bc);
			}
		} catch (Exception e){
			SystemFunctions.doNothing();
		}
	}

	private void checkFileReference(String fileName, String folder, BaseConfiguration bc) {
		if (fileName == null || fileName.trim().length() == 0)
			return;
		fileName = fileName.trim();
		if ( !new File(fileName).exists())
			log.warn("RS.locate.file", fileName, "/" + folder + "/" + bc.getShortName());
	}

	

	/**
	 * Shuts down the Scheduler with the given name
	 * @param name Name of the Scheduler
	 * @since 7.2
	 */
	public void shutdownScheduler(String name) {
		Scheduler s = getScheduler(name);
		if (s!= null)
			s.shutdown();
	}
	
	/**
	 * Pauses the Scheduler with the given name
	 * @param name Name of the Scheduler
	 * @since 7.2
	 */
	public void pauseScheduler(String name) {
		Scheduler s = getScheduler(name);
		if (s!= null)
			s.pauseScheduler();
	}
	
	/**
	 * Resumes the Scheduler with the given name
	 * @param name Name of the Scheduler
	 * @since 7.2
	 */
	public void resumeScheduler(String name) {
		Scheduler s = getScheduler(name);
		if (s!= null)
			s.resumeScheduler();
	}
	
	/**
	 * 
	 * Starts the Scheduler with the given name.
	 * This may be useful if the Scheduler has been stopped.
	 * @param name Name of the Scheduler
	 * @throws Exception If the Scheduler cannot be found.
	 * @since 7.2
	 */
	public void startScheduler(String name) throws Exception{
		Scheduler s = getScheduler(name);
		if (s != null) {
			s.resumeScheduler();
			return;
		}
		
		Name cn = MetamergeConfigFactory.parseName(MetamergeConfig.DEFAULT_SCHEDULER_FOLDER);
		cn.add(name);
		Object obj = serverConfig.lookup(cn);
		if (! (obj instanceof SchedulerConfig))
			throw new NameNotFoundException(name); // Cannot happen, lookup will throw the Exception

		new Scheduler(this, (SchedulerConfig) obj).start();			
	}
	
	/**
	 * Stops all Schedulers belonging to this RS.
	 * @since 7.2
	 */
	public void stopSchedulers() {
		synchronized (schedulerMap) {
			for (Scheduler s:schedulerMap.values()) {
				s.shutdown();
			}
		}
	}

	/**
	 * Returns the Scheduler with the given name.
	 * @param name The name
	 * @return The Scheduler with the given name
	 */
	public Scheduler getScheduler(String name) {
		synchronized (schedulerMap) {
			return schedulerMap.get(name);
		}
	}

	/**
	 * Registers the given scheduler
	 * @param scheduler
	 */
	void registerScheduler(Scheduler scheduler) {
		synchronized (schedulerMap) {
			schedulerMap.put(scheduler.getName(), scheduler);
		}
	}

	/**
	 * Deregisters the given scheduler
	 * @param scheduler
	 */
	void deregisterScheduler(Scheduler scheduler) {
		synchronized (schedulerMap) {
			schedulerMap.remove(scheduler.getName());
		}
	}
	
	/**
	 * Returns information about the named Scheduler.
	 * If the Scheduler is not found, returns null.
	 * @see Scheduler.getInfo();
	 * @param name - Name of Scheduler
	 * @return
	 */
	public Map<String, Object> getSchedulerInfo(String name) {
		Scheduler s = getScheduler(name);
		if (s != null)
			return s.getInfo();
		else
			return null;
	}
	
	/**
	 * Returns information about all Schedulers in this RS.
	 * If no Schedulers are found, returns an empty List.
	 * @see Scheduler.getInfo();
 	 * @return
	 */
	public List<Map<String, Object>> getSchedulersInfo() {
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		synchronized (schedulerMap) {
			for (Scheduler s:schedulerMap.values()) {
				res.add(s.getInfo());
			}
		}
		return res;
	}

}
