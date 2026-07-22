/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;

import com.ibm.di.config.base.AttributeMapConfigImpl;
import com.ibm.di.config.base.AttributeMapItemImpl;
import com.ibm.di.config.base.ConnectorConfigImpl;
import com.ibm.di.config.base.FileNamespace;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.config.interfaces.PoolInstanceConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.ReconnectConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.SkipLookupInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.DeltaEntry;
import com.ibm.di.entry.ModificationItem;
import com.ibm.di.exceptions.AbortALException;
import com.ibm.di.exceptions.ContinueLoopException;
import com.ibm.di.exceptions.ExitBranchException;
import com.ibm.di.exceptions.IgnoreEntryException;
import com.ibm.di.exceptions.NoChangesException;
import com.ibm.di.exceptions.RestartEntryException;
import com.ibm.di.exceptions.RetryEntryException;
import com.ibm.di.exceptions.SkipEntryException;
import com.ibm.di.exceptions.SkipToException;
import com.ibm.di.fc.AssemblyLineFC;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.script.ScriptExitCode;

/**
 * This class is used by the AssemblyLine (AssemblyLine) to wrap a Connector
 * object and provide additional functionality over the underlying Connector
 * object. All attribute mapping and hook handling is performed by this class.
 * <p>
 * The underlying Connector object is exposed as a property called
 * <i>connector</i>.
 *
 * This class is also the root class of a hierarchy of classes which represent
 * components which can be hosted in an AssemblyLine (for example Function
 * Components, Script Components, Branch Components, Loop Components, etc.)
 */
public class AssemblyLineComponent implements java.util.Map {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * @deprecated Checkpoint/restart is deprecated
	 */
	public final static String CHECKPOINT_GETS = "$connector.gets";

	final static String OR_GETNEXT = "override_getnext";

	final static String OR_LOOKUP = "override_lookup";

	final static String OR_UPDATE = "override_update";

	final static String OR_DELETE = "override_delete";

	final static String OR_ADD = "override_add";

	final static String OR_MODIFY = "override_modify";

	final static String OR_CALLREPLY = "override_callreply";

	final static String OR_REPLY = "override_reply";

	final static String OR_DELTA = "override_delta";

	final protected static String INITIALIZE = "initialize";

	final protected static String SELECT = "select";

	final static String INITIALIZE_HOOKS = "initialize.hooks";

	/**
	 * The key used to get the number of errors this component has made.
	 */
	public final static String NUM_ERRORS = "numErrors";

	/**
	 * The key used to get the number additions this component has made.
	 */
	public final static String NUM_ADD = "numAdd";

	/**
	 * The key used to get the number of modifications this component has made.
	 */
	public final static String NUM_MODIFY = "numModify";

	/**
	 * The key used to get the number of deletions this component has made.
	 */
	public final static String NUM_DELETE = "numDelete";

	/**
	 * The key used to get the number of entries this component has gotten.
	 */
	public final static String NUM_GET = "numGet";

	/**
	 * The key used to get the number of times we tried to get an Entry.
	 */
	public final static String NUM_GET_TRIES = "numGetTries";

	/**
	 * The key used to get the number of the clients this component has
	 * connected with.
	 */
	public final static String NUM_GETCLIENT = "numGetClient";

	/**
	 * The key used to get the number of times we tried to get a client
	 */
	public final static String NUM_GETCLIENT_TRIES = "numGetClientTries";

	/**
	 * The key used to get the number of call-replays operations this component
	 * has executed.
	 */
	public final static String NUM_CALLREPLY = "numCallreply";

	/**
	 * The key used to get the number of lookups this component has made.
	 */
	public final static String NUM_LOOKUP = "numLookup";

	/**
	 * The key used to get the number of no-change entries.
	 */
	public final static String NUM_NOCHANGE = "numNoChange";

	/**
	 * The key used to get the number of skipped entries.
	 */
	public final static String NUM_SKIPPED = "numSkipped";

	/**
	 * The key used to get the number of ignored entries.
	 */
	public final static String NUM_IGNORED = "numIgnored";

	/**
	 * The key used to get the last error that have occurred.
	 */
	public final static String LAST_ERROR = "lastError";

	/**
	 * The key used to get the status the last operation ended with.
	 */
	public final static String SUCCESSFUL = "success";

	/**
	 * The key used to get the last Entry with information from the Connector,
	 * or the last Entry that was sent to the Connector
	 */
	public final static String LAST_CONN = "lastConn";

	/**
	 * The key used to get the list of invoked hooks in this cycle.
	 */
	public final static String HOOKS_INVOKED = "hooksInvoked";

	/**
	 * The key used to get the flag which is raised (it's value is set to true)
	 * when the the component wrapped by this {@link AssemblyLineComponent} is
	 * in Iterator mode and the feed has been exhausted.
	 */
	public final static String END_OF_DATA = "endOfData";

	/**
	 * This is the input_connector provided as a parameter (to the
	 * AssemblyLine).
	 */
	ConnectorInterface input_connector;

	private boolean forceRuntime;

	/**
	 * This is the connector we are working with. See getConnector()
	 */
	public ConnectorInterface connector;

	/**
	 * This is the statistics object for the component
	 */
	public TaskStatistics stats;

	/**
	 * This is the connector name as given by the AssemblyLine
	 */
	protected String name;

	/**
	 * This is the parent task (AssemblyLine)
	 */
	protected AssemblyLine parent;

	/**
	 * Flag to avoid multiple initializations
	 */
	boolean is_initialized = false;

	/**
	 * If this is true, we have tried to initialize the Connector, and should terminate it.
	 */
	boolean shouldTerminate;

	/**
	 * Flag to avoid closing reused connector
	 */
	boolean reusingConnector = false;

	/**
	 * This is the search criteria object
	 */
	private SearchCriteria link;

	/**
	 * These are the objects performing input and output attribute mapping
	 */
	protected AttributeMapping imap;

	protected AttributeMapping addmap;

	protected AttributeMapping modmap;

	/**
	 * This is the object performing hooks
	 */
	protected AttributeMapping handler;

	/*
	 * This is the object performing compares (compute changes etc ...)
	 */
	private Compare compare;

	/*
	 * This flag governs whether we are computing changes during modify
	 * operations
	 */
	private boolean compute_changes = false;

	/*
	 * This is the Mode in which this connector operates ( e.g. Iterator, Update
	 * .. )
	 */
	private int type;

	/**
	 * This is our configuration (with attribute maps, hooks etc ...)
	 */
	protected ConnectorConfig config;

	/**
	 * This is the configuration of the ConnectorInterface
	 */
	protected RawConnectorConfig connConfig;

	/*
	 * This is the last input entry read from the connector
	 */
	private com.ibm.di.entry.Entry lastRead = null;

	/*
	 * This is the last input entry read and mapped OK
	 */
	private com.ibm.di.entry.Entry lastEntry = null;

	/*
	 * This flag governs whether we report an error on duplicate entries found.
	 * If true we pick the first entry found and store any other duplicates in
	 * the "duplicate entry" array.
	 */
	private boolean allowDuplicates;

	/*
	 * This entry is the user's selection of multiple entries found.
	 */
	private com.ibm.di.entry.Entry currentEntry;

	/**
	 * This is the Log object we use for logging
	 */
	protected Log log;

	/**
	 * Indicates whether this Connector is got from a Connector Pool
	 */
	protected boolean pooledConnector = false;

	/**
	 * Holds the Connector Pool object when a pooled Connector is used
	 */
	protected ConnectorPool connPool = null;

	/*
	 * If this is set, ignore missing Hooks in Lookup
	 */
	private boolean ignoreMissingHooksInLookup = false;

	/*
	 * This engine decides what to do when a connector raises an error.
	 */
	private ReconnectRuleEngine reconnectRuleEngine;

	/**
	 * How many times the component has been initialized
	 */
	protected int initializeCount = 0;

	/*
	 * Values for the Connector parameters last time around
	 */
	private HashMap<String, Object> oldMap = null;

	/*
	 * An object that handles the persisted information.
	 */
	private Map<String, Object> persistedProperties = new HashMap<String, Object>();

	/**
	 * When the component is supposed to be initialized.
	 *
	 * @see ConnectorConfig#COMP_INIT_DEFAULT
	 */
	int initOption;

	/**
	 * Remember how many times we have recently tried to reconnect.
	 */
	private int recentReconnectAttempts;

	/*
	 * The names of all the properties we get from the stats Object
	 */
	private final static ArrayList<String> statsProperties = new ArrayList<String>();
	// Initialize statsProperties
	static {
		statsProperties.add(NUM_ERRORS);
		statsProperties.add(NUM_ADD);
		statsProperties.add(NUM_MODIFY);
		statsProperties.add(NUM_DELETE);
		statsProperties.add(NUM_GET);
		statsProperties.add(NUM_GET_TRIES);
		statsProperties.add(NUM_GETCLIENT);
		statsProperties.add(NUM_GETCLIENT_TRIES);
		statsProperties.add(NUM_CALLREPLY);
		statsProperties.add(NUM_LOOKUP);
		statsProperties.add(NUM_NOCHANGE);
		statsProperties.add(NUM_SKIPPED);
		statsProperties.add(NUM_IGNORED);
		statsProperties.add(LAST_ERROR);
	}

	// Initialize persistedProperties
	{
		persistedProperties.put(LAST_CONN, null);
		persistedProperties.put(SUCCESSFUL, Boolean.TRUE);
		persistedProperties.put(HOOKS_INVOKED, new ArrayList<String>());
		persistedProperties.put(END_OF_DATA, "false");
	}

	/*
	 * index of parent and associated endComponent in the AssemblyLine
	 */
	private int parentIndex = -1;

	private int endComponentIndex = -2;

	private ConnectorInterface origConnector; // Original connector when failoverConnector is set.
	private ConnectorInterface failoverConnector; // The Connector to do a failover to.
	private long tryFailback; // The time when we should try to do a failback to the original Connector.

	int numRead; // Number of Entries read since select
	
	int maxRead; // Maximum number of Entries to read
	

	/**
	 * Constructor for the {@link AssemblyLineComponent} object
	 *
	 * @param parent
	 *            The AssemblyLine using this component
	 * @param name
	 *            The name of this connector
	 * @param config
	 *            The configuration for this connector
	 * @param conn
	 *            An optional connector to use if we need a (runtime provided)
	 *            connector
	 * @exception Exception
	 *                Any exception that might occur while loading the config
	 */
	public AssemblyLineComponent(TaskInterface parent, String name,
			ConnectorConfig config, ConnectorInterface conn) throws Exception {
		this(parent, name, config, conn, false);
	}

	/**
	 * Constructor for the {@link AssemblyLineComponent} object
	 *
	 * @param parent
	 *            The AssemblyLine using this component
	 * @param name
	 *            The name of this connector
	 * @param config
	 *            The configuration for this connector
	 * @param conn
	 *            An optional connector to use if we need a (runtime provided)
	 *            connector
	 * @param forceRuntime
	 *            whether to use the runtime provided connector
	 * @exception Exception
	 *                Any exception that might occur while loading the config
	 */
	public AssemblyLineComponent(TaskInterface parent, String name,
			ConnectorConfig config, ConnectorInterface conn,
			boolean forceRuntime) throws Exception {
		Trace.entrymin(this, "AssemblyLineComponent");

		this.input_connector = conn;
		this.forceRuntime = forceRuntime;

		connConfig = config.getConnectionConfig();
		initCommon(parent, name, config);

		compare = new Compare();
		stats = new TaskStatistics();

		log = new Log(this.parent.getLog());
		log.setDebug(connConfig.getDebug(false));
		log.setPrefix("[" + name + "] ");

		try {
			loadConfig();
			if (!reusingConnector) {
				connector.setName(name);
				connector.setContext(this.parent);
			}
		} catch (Exception e) {
			log.logerror("error.loadConfig", e);
			log.exception("exception.loadConfig", e.toString());
		}

		allowDuplicates = config.getBooleanParameter("allow_duplicates", false);

		reconnectRuleEngine = RS.getReconnectRuleEngine();
		Trace.exitmax(this, "AssemblyLineComponent");

	}

	void initCommon(TaskInterface parent, String name, ConnectorConfig config) {
				this.parent = (AssemblyLine) parent;
				this.config = (ConnectorConfig) config;
				this.initOption = config.getInitializeOption();

				setName(name);
			}

	/**
	 * Empty Constructor for the AssemblyLineComponent object
	 */
	public AssemblyLineComponent() {
		initOption = ConnectorConfig.COMP_INIT_DEFAULT;
	}

	/**
	 * Sets the current entry of the {@link AssemblyLineComponent} object
	 *
	 * @param current
	 *            The new current value
	 */
	public void setCurrent(com.ibm.di.entry.Entry current) {
		currentEntry = current;
		if (connector != null)
			connector.setCurrent(current, link);
	}

	/**
	 * This method sets the the debug mode flag. May be called by different
	 * threads.
	 *
	 * @param debug
	 *            True to enable debug, false to disable
	 */
	public void setDebug(boolean debug) {
		synchronized (log) {
			log.setDebug(debug);
			if (connector instanceof Connector && !reusingConnector) {
				((Connector) connector).setDebugMode(debug);
			}
		}
	}

	/**
	 * This method is called if the connector is used by a LoopComponent
	 */
	void setIgnoreMissingHooksInLookup() {
		ignoreMissingHooksInLookup = true;
	}

	/**
	 * Declare an <i>error</i> object in the script engine
	 *
	 * @param oper
	 *            The new errorObject value
	 * @param reason
	 *            The new errorObject value
	 * @deprecated Not used anymore
	 */
	@Deprecated
	public void setErrorObject(String oper, String reason) {
		com.ibm.di.entry.Entry e = new com.ibm.di.entry.Entry();
		e.setAttribute("status", "fail");
		e.setAttribute("message", reason);
		e.setAttribute("operation", oper);
		e.setAttribute("status", "fail");
		e.setAttribute("connectorname", getName());
		try {
			handler.declareStaticBean("error", e);
		} catch (Exception err) {
			log.error("error.declaring.error.object", err);
		}
	}

	/**
	 * Sets the max number of duplicate entries for the ConnectorInterface. This
	 * is used by the lookup operation when a lookup returns more than one
	 * entry.
	 *
	 * @param maxdup
	 *            The new maximum
	 */
	public void setDuplicateEntryCount(int maxdup) {
		if (connector != null) {
			connector.setMaxDuplicateEntries(maxdup);
		}
	}

	/**
	 * This method returns the name assigned to the Connector by the
	 * AssemblyLine.
	 *
	 * @return The name of this Connector (in scripting terms)
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method assigns a name to the Connector.
	 *
	 * @param name
	 *            the name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the name and class of this component
	 */
	public String toString() {
		if (name == null)
			return super.toString();
		return name;
	}

	/**
	 * Gets the <i>current</i> entry of the AssemblyLineComponent object
	 *
	 * @return The current Entry
	 */
	public com.ibm.di.entry.Entry getCurrent() {
		return currentEntry;
	}

	/**
	 * @return the Connector configuration
	 */
	public ConnectorConfig getConfiguration() {
		return config;
	}

	/**
	 * @return the BaseConfiguration
	 */
	public BaseConfiguration getBaseConfiguration() {
		return config;
	}

	/**
	 * This method returns the configuration value for a parameter
	 *
	 * @param param
	 *            The parameter name
	 * @return The object associated with parameter name
	 */
	public Object getConfig(String param) {
		return config.getParameter(param);
	}

	/**
	 * This method returns the ConnectorInterface attached to this AL component.
	 * <p>
	 * Note that this applies only to Connectors and will return null for any
	 * other type of component.
	 *
	 * @param param
	 *            The parameter name
	 * @return The object associated with parameter name
	 */
	public Object getConnectorParam(String param) {
		return connConfig.getParameter(param);
	}

	/**
	 * This method sets the configuration value for the ConnectorInterface's
	 * parameter.
	 *
	 * @param param
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 */
	public void setConnectorParam(String param, Object value) {
		connConfig.setParameter(param, value);
	}

	/**
	 * Returns the ConnectorInterface we are working with
	 *
	 * @return The Connector we are working with
	 */
	public ConnectorInterface getConnector() {
		return connector;
	}

	/**
	 * This method returns the debug mode flag.
	 *
	 * @return True if debug is enabled, false otherwise
	 */
	public boolean getDebug() {
		return log.getDebug();
	}

	/**
	 * This method returns the link criteria
	 * <p>
	 * This example code demonstrates how to get, modify and set different
	 * SearchCriteria for a component.
	 * <p>
	 * <b>Example:</b>
	 *
	 * <pre>
	 * var conCrit = input.getCriteria();
	 * var newCrit = new com.ibm.di.server.SearchCriteria();
	 *
	 * for (i = 0; i &lt; conCrit.size(); i++) {
	 * 	var rsc = conCrit.getCriteria(i);
	 * 	if ((rsc.name.equals(&quot;pass&quot;))
	 * 			&amp;&amp; (rsc.match == com.ibm.di.server.SearchCriteria.SUBSTRING)) {
	 * 		newCrit.addTemplate(&quot;password&quot;, com.ibm.di.server.SearchCriteria.EXACT,
	 * 				rsc.value);
	 * 	} else {
	 * 		newCrit.addTemplate(rsc.name, rsc.match, rsc.value);
	 * 	}
	 * }
	 * input.setCriteria(newCrit);
	 * </pre>
	 *
	 * @return The link criteria of this AssemblyLineComponent
	 */
	public SearchCriteria getCriteria() {
		return link;
	}

	/**
	 * This method sets the link criteria for this AssemblyLineComponent
	 *
	 * @param mySearchCrit
	 *            The link criteria to set
	 */
	public void setCriteria(SearchCriteria mySearchCrit) {
		link = mySearchCrit;
	}

	/**
	 * This method sets the link criteria template for this
	 * AssemblyLineComponent, and then expands the template using the given work
	 * Entry. If the template is successfully expanded, the method will return
	 * true.
	 *
	 * @param mySearchCrit
	 *            The Criteria to set
	 * @param work
	 *            The Entry to use for expansion.
	 * @return true if the link criteria was successfully expanded.
	 */
	public boolean setCriteria(SearchCriteria mySearchCrit,
			com.ibm.di.entry.Entry work) {
		Trace.entrymax(this, "setCriteria", mySearchCrit, work);
		link = mySearchCrit;
		if (work != null) {
			try {
				link.buildCriteria(work, getBaseConfiguration(), parent);
				// link.buildCriteria(work);
				return true;
			} catch (Exception err) {
				log.warn("ASSEMBLYLINECOMPONENT.LINKCRITERIA.BUILD.EXCEPTION",
						err);
			}
		}
		Trace.exitmax(this, "setCriteria", false);
		return false;
	}

	/**
	 * This method returns the mode of a Connector, or the type constant for any
	 * other type of component.
	 * <p>
	 * {@link ServerConstants#TYPE_ITERATOR} = 0<br>
	 * {@link ServerConstants#TYPE_UPDATE} = 1<br>
	 * {@link ServerConstants#TYPE_LOOKUP} = 2<br>
	 * {@link ServerConstants#TYPE_DELETE} = 3<br>
	 * {@link ServerConstants#TYPE_ADDONLY} = 4<br>
	 * {@link ServerConstants#TYPE_CALLREPLY} = 5<br>
	 * {@link ServerConstants#TYPE_SCRIPT} = 6<br>
	 * {@link ServerConstants#TYPE_FUNCTION} = 7<br>
	 * {@link ServerConstants#TYPE_BRANCH} = 8<br>
	 * {@link ServerConstants#TYPE_REPLYCHANNEL} = 9<br>
	 * {@link ServerConstants#TYPE_SERVER} = 10<br>
	 * {@link ServerConstants#TYPE_DELTA} = 11<br>
	 * {@link ServerConstants#TYPE_LOOP} = 12<br>
	 * {@link ServerConstants#TYPE_ATTRIBUTEMAP} = 13<br>
	 * {@link ServerConstants#TYPE_SWITCH} = 14<br>
	 * {@link ServerConstants#TYPE_CASE} = 15
	 *
	 * @return The integer value corresponding to the execution mode
	 */

	public int getType() {
		return type;
	}

	/**
	 * @return true if we are in Delta mode.
	 */
	public boolean isDeltaMode() {
		return type == ServerConstants.TYPE_DELTA;
	}

	/**
	 * This method returns the last entry read from the connector.
	 *
	 * @return The last input entry (unmapped)
	 */
	public com.ibm.di.entry.Entry getLastReadEntry() {
		if (lastRead != null)
			return lastRead.clone();
		else
			return null;
	}

	/**
	 * This method returns the last entry read and mapped.
	 *
	 * @return The last input entry (mapped)
	 */
	public com.ibm.di.entry.Entry getLastEntry() {
		if (lastEntry == null) {
			return null;
		} else {
			return lastEntry.clone();
		}
	}

	/**
	 * The method returns the next client from the connector.
	 *
	 * @return The next client or null if there are no more clients
	 * @exception Exception
	 *                Errors that may occur
	 */
	public ConnectorInterface getnextClient() throws Exception {
		Trace.entrymax(this, "getnextClient");
		checkInitialize();

		try {
			ConnectorInterface ci = null;

			handler.pushStackFrame(this);

			trigger("before_getnextclient", null, null);

			ci = connector.getNextClient();

			if (ci == null) {
				Trace.exitmax(this, "getnextClient");
				return null;
			}

			com.ibm.di.entry.Entry conn = new com.ibm.di.entry.Entry();
			conn.setAttribute("connectorInterface", ci);
			trigger("after_getnextclient", null, conn);

			ci = (ConnectorInterface) conn.getObject("connectorInterface");

			stats.getclient();
			Trace.exitmax(this, "getnextClient");
			return ci;
		} catch (Exception e) {
			handleException("getnextclient", e, null);
			return null;
		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
	}

	/**
	 * The method returns the next entry from the connector.
	 *
	 * @return The next input entry or null if there are no more entries
	 * @exception Exception
	 *                Errors that may occur
	 */
	public com.ibm.di.entry.Entry getnext() throws Exception {
		return getnext(new com.ibm.di.entry.Entry());
	}

	/**
	 * The method returns the next entry from the connector. It is called by the
	 * {@link AssemblyLine} if we are working in Iterator mode.
	 *
	 * @param work
	 *            The work entry to fill in
	 * @return The work entry filled with the next input entry, or null if there
	 *         are no more entries
	 * @exception Exception
	 *                Errors that may occur
	 */
	public com.ibm.di.entry.Entry getnext(com.ibm.di.entry.Entry work)
			throws Exception {
		Trace.entrymax(this, "getnext");
		checkInitialize();

		if (maxRead > 0 && numRead >= maxRead) {
			// log.info("max.entries.read", maxRead); TODO: Need a new message
			return null;
		}

		try {
			handler.pushStackFrame(this);
			// User may override getNextEntry() routine
			if (handler.hasAttribute(OR_GETNEXT) && willExecuteSafeORHook()) {
				stats.getTries();
				handler.declareBean("entry", work);
				trigger(OR_GETNEXT, work, null);
				numRead++;

				if (work.size() < 1||
						parent.getScriptEngine().getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
					persistedProperties.put(END_OF_DATA, "true");
					return null;
				}
				persistedProperties.put(LAST_CONN, work); // The best we can
				// do
				lastEntry = work;
				lastRead = lastEntry; // for lack of something better
				stats.get();
				Trace.exitmax(this, "getnext", work);
				return work;
			}

			// Default getNextEntry()

			trigger("before_getnext", work, null);

			com.ibm.di.entry.Entry e;

			if ((e = connector.getPushbackEntry()) == null) {
				stats.getTries();
				e = (com.ibm.di.entry.Entry) executeOperation(
						SimulationConfig.SIM_OP_GET_NEXT_ENTRY, work, null,
						null, null);
				numRead++;
			}

			if (e == null) {
				persistedProperties.put(END_OF_DATA, "true");
				return null;
			}
			// Verify schema
			// verifySchema ( e, true );

			lastRead = e;

			persistedProperties.put(LAST_CONN, e);
			trigger("after_getnext", work, e);

			work.setOp(e.getOp());

			imap.declareBean("work", work);
			imap.declareBean("conn", e);
			work = imap.mapEntry(e, work);
			dumpObjects(e, work, null);

			lastEntry = work;
			stats.get();
			Trace.exitmax(this, "getnext", work);
			return work;
		} finally {
			handler.popStackFrame();
			checkTerminate();
		}

	}

	/**
	 * Gets the stats attribute of the AssemblyLineComponent object
	 *
	 * @return The {@link TaskStatistics} object holding the statistics for this
	 *         component.
	 */
	public TaskStatistics getStats() {
		return stats;
	}

	/**
	 * Returns the log object
	 *
	 * @return The {@link Log} object
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * @return the object performing hooks
	 */
	public AttributeMapping getHandler() {
		return handler;
	}

	/**
	 * Returns true if an exception is fatal
	 *
	 * @param e
	 *            The exception to check
	 * @return True if the exception is fatal
	 */
	public boolean isExceptionFatal(Exception e) {
		if (connector == null) {
			return false;
		} else {
			return connector.isExceptionFatal(e);
		}
	}

	/**
	 * Gets the count of duplicate Entries found by the last findEntry by the
	 * ConnectorInterface
	 *
	 * @return The duplicateEntryCount value
	 */
	public int getDuplicateEntryCount() {
		if (connector != null) {
			return connector.getFindEntryCount();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the first Duplicate Entry from the ConnectorInterface. This function
	 * also resets the implicit pointer to the next Entry.
	 *
	 * @return The firstDuplicateEntry value
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public com.ibm.di.entry.Entry getFirstDuplicateEntry() throws Exception {
		if (connector != null) {
			return connector.getFirstFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * Gets the next Duplicate Entry from the ConnectorInterface. This function
	 * also moves an implicit pointer to the next Entry.
	 *
	 * @return The nextDuplicateEntry value, null means no more values
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public com.ibm.di.entry.Entry getNextDuplicateEntry() throws Exception {
		if (connector != null) {
			return connector.getNextFindEntry();
		} else {
			return null;
		}
	}

	/**
	 * Returns true if this Component is enabled in this AssemblyLine
	 *
	 * @return The enabled value
	 */
	public boolean isEnabled() {
		return config.getEnabled();
	}

	/**
	 * This method closes the connector (connector.terminate()) and calls the
	 * before/after close hooks.
	 */
	public void close() throws Exception {
		Trace.entrymax(this, "close");

		doConnectorTerminate();

		if (imap != null) {
			imap.unload();
			imap = null;
		}
		if (addmap != null) {
			addmap.unload();
			addmap = null;
		}
		if (modmap != null) {
			modmap.unload();
			modmap = null;
		}
		if (handler != null) {
			handler.unload();
			handler = null;
		}

		parent = null;

		is_initialized = false;
		Trace.exitmax(this, "close");
	}

	/**
	 * Return true/false if this component should be executed. Also calls the
	 * "Before Execute" hook.
	 *
	 * @param work
	 *            The current work Entry
	 * @return True if this component is enabled
	 * @exception Exception
	 *                Any exception thrown by the executed hook
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		if ((parent.isSimulating() && getSimulatingState().equalsIgnoreCase(
				SimulationConfig.SIM_DISABLED_STATE))
				|| !isEnabled()) {
			return false;
		}

		if (isFailOvered() && tryFailback > 0 && new Date().getTime() > tryFailback) {
			failBack(work, null);
		}

		final String beforeExecuteHook = "before_execute";

		// optimization to avoid pushStackFrame if there is no hook
		boolean useStackFrame = handler.hasAttribute(beforeExecuteHook);

		try {
			if (useStackFrame) {
				handler.pushStackFrame(this);
			}

			trigger(beforeExecuteHook, work, null);

			return true;
		} finally {
			if (useStackFrame) {
				handler.popStackFrame();
			}
		}
	}

	/**
	 * This method initializes the underlying connector and registers the script
	 * beans in the ScriptEngine context.
	 *
	 * @exception Exception
	 *                any exception, which occurs during the initialization of
	 *                the component and is not handled by the reconnect
	 *                mechanism or the error hooks; it is also possible that the
	 *                exception is thrown by some of the initialize hooks
	 */
	public void initialize() throws Exception {
		if (initOption == ConnectorConfig.COMP_INIT_DEFAULT)
			doInitialize();
	}

	void doInitialize() throws Exception {
		Trace.entrymax(this, "doInitialize");

		if (is_initialized)
			return;

		String limit = config.getLimitOption();
		if (limit != null && limit.length() > 0) {
			maxRead = Integer.parseInt(limit);
			connector.setMaxDuplicateEntries(maxRead);
		} else {
			maxRead = 0;
		}

		boolean exceptionHandled = false;

		try {
			handler.pushStackFrame(this);

			// handler.declareBean ("connector", this);
			trigger("before_initialize", parent.getCurrentWork());

			if (reusingConnector) {
				if (connector == null) {
					String s = config.getConnectionConfig().getJavaClass();
					AssemblyLineComponent tc = parent.getConnector(s.substring(1));
					if (tc != null)
						connector = tc.connector;
				}
				log.debug("AssemblyLineComponent.dontinit.reused.connector");
			} else if (connector != this.input_connector) {
				if (!pooledConnector) {
					log.debug("initialize.connector");
					try {
						shouldTerminate = true;
						connector.initialize(new ConnectorMode(getType()));
					} catch (Throwable error) {
						exceptionHandled = true;
						handleException(INITIALIZE, error, parent
								.getCurrentWork());
						exceptionHandled = false;
					}
				}

			} else {
				shouldTerminate = true;
				log.debug("assemblyline.comp.dontinit.connector.info");
			}

			if (!reusingConnector)
				connector.registerScriptBeans(parent.getScriptEngine());

			if (getType() == ServerConstants.TYPE_ITERATOR) {
				log.debug("initialize.iterator");
				try {
					doConnectorSelectEntries();
				} catch (Throwable error) {
					exceptionHandled = true;
					handleException(SELECT, error, parent.getCurrentWork());
					exceptionHandled = false;
				}
			}

			if (connector instanceof Connector
					&& connector != this.input_connector
					&& !reusingConnector ) {
				log.debug("assemblyline.comp.connector.info", connector
						.getClass().getName(), ((Connector) connector)
						.getVersion());
				ParserInterface p = ((Connector) connector).getParser();
				if (p instanceof VersionInfoInterface)
					log.debug("assemblyline.comp.parser.info",
							p.getClass().getName(),
							((VersionInfoInterface) p).getVersion());
			}

			trigger("after_initialize", parent.getCurrentWork());
			is_initialized = true;
			log.debug("end.initialize");
			initializeCount++;
		} catch (Throwable error) {
			if (!exceptionHandled)
				handleException(INITIALIZE_HOOKS, error, parent
						.getCurrentWork());
			else if (error instanceof Exception)
				throw (Exception) error;
			else
				throw new Exception(error);
		} finally {
			handler.popStackFrame();
		}
		Trace.exitmax(this, "doInitialize");
	}

	/**
	 * This method pushes an entry back to the connector. The entry is returned
	 * the next time connector.getNextEntry() is called.
	 *
	 * @param entry
	 *            The entry to push back
	 */
	public void pushback(com.ibm.di.entry.Entry entry) {
		connector.pushback(entry);
	}

	/**
	 * This method calls the appropriate hooks and the connector's selectEntries
	 * method.
	 */
	void doConnectorSelectEntries() throws Exception {
		trigger("before_selectEntries", parent.getCurrentWork());

		numRead = 0;

		executeOperation(SimulationConfig.SIM_OP_SELECT_ENTRIES, null, null,
				null, null);
		persistedProperties.put(END_OF_DATA, "false");
		connector.registerScriptBeans(parent.getScriptEngine());
		trigger("after_selectEntries", parent.getCurrentWork());
	}

	/**
	 * Skip forward entries after a reconnect
	 */
	private void doConnectorSkipForward() throws Exception {

		Trace.entrymax(this, "doConnectorSkipForward");

		if (getType() != ServerConstants.TYPE_ITERATOR) {
			Trace.exitmax(this, "doConnectorSkipForward");
			return;
		}

		int numSkips = stats.numGet();

		if (numSkips == 0) {
			Trace.exitmax(this, "doConnectorSkipForward");
			return;
		}

		log.info("assemblyline.comp.skipping.conname.info", "" + numSkips,
				getName());

		// User may override getNextEntry() routine
		if (handler.hasAttribute(OR_GETNEXT)) {
			com.ibm.di.entry.Entry work = new com.ibm.di.entry.Entry();
			handler.declareBean("entry", work);
			for (int i = 0; i < numSkips; i++)
				trigger(OR_GETNEXT, work, null);
		} else {
			for (int i = 0; i < numSkips; i++)
				connector.getNextEntry();
		}
		Trace.exitmax(this, "doConnectorSkipForward");
	}

	/**
	 * This method calls the appropriate hooks and the connector's initialize
	 * method.
	 *
	 * @throws Exception
	 *             if the connector's initialization fails or an error in one of
	 *             the executed hooks occurred.
	 */
	public void doConnectorInitialize() throws Exception {
		try {
			handler.pushStackFrame(this);
			trigger("before_initialize", parent.getCurrentWork());
			if (!pooledConnector) {
				try {
					shouldTerminate = true;
					connector.initialize(new ConnectorMode(getType()));
				} catch (Throwable error) {
					handleException(INITIALIZE, error, parent.getCurrentWork());
				}
			}

			connector.registerScriptBeans(parent.getScriptEngine());

			if (getType() == ServerConstants.TYPE_ITERATOR) {
				try {
					doConnectorSelectEntries();
				} catch (Throwable error) {
					handleException(SELECT, error, parent.getCurrentWork());
				}
			}

			trigger("after_initialize", parent.getCurrentWork());
			is_initialized = true;

		} catch (Throwable error) {
			handleException(INITIALIZE_HOOKS, error, parent.getCurrentWork());
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * This method calls the appropriate hooks and the connector's terminate
	 * method.
	 *
	 * @throws Exception
	 *             if the connector's termination fails or an error in one of
	 *             the executed hooks occurred.
	 */
	public void doConnectorTerminate() throws Exception {
		doConnectorTerminate(false);
	}

	/**
	 * This method calls the appropriate hooks and the connector's terminate
	 * method.
	 * @param force
	 *            Forces a connector terminate, even if the connector was initialized elsewhere.
	 *
	 * @throws Exception
	 *             if the connector's termination fails or an error in one of
	 *             the executed hooks occurred.
	 */
	public void doConnectorTerminate(boolean force) throws Exception {
		if (handler == null)
			return; // No handler, nothing to terminate.

		Trace.entrymid(this, "doConnectorTerminate");
		try {
			handler.pushStackFrame(this);
			if (connector != null) {
				trigger("before_close", parent.getResult(), null);

				is_initialized = false;
				if (pooledConnector) {
					connPool.returnConnector(connector);
				} else if (force || shouldTerminate) {
					shouldTerminate = false;
					connector.terminate();
				}

				trigger("after_close", parent.getResult(), null);

			}
		} catch (Exception err) {
			handleException("close", err, parent.getResult());
		} finally {
			handler.popStackFrame();
		}
		Trace.exitmid(this, "doConnectorTerminate");
	}

	/**
	 * The method is called to lookup an entry using exact matching for
	 * attribute and value.
	 *
	 * @param attribute
	 *            The attribute name to search
	 * @param value
	 *            The attribute value
	 * @return The entry found or null if no entries or more than one entry was
	 *         found.
	 * @exception Exception
	 *                Any exceptions thrown by the underlying connector
	 */
	public com.ibm.di.entry.Entry lookup(String attribute, String value)
			throws Exception {
		SearchCriteria rs = new SearchCriteria();
		rs.addCriteria(attribute, SearchCriteria.EXACT, value);

		stats.lookup();

		com.ibm.di.entry.Entry e = (com.ibm.di.entry.Entry) executeOperation(
				SimulationConfig.SIM_OP_FIND_ENTRY, null, null, rs, null);
		// verifySchema ( e, true );
		return e;
	}

	/**
	 * The method is called to lookup an entry using the configured Link
	 * Criteria. The link criteria is applied using <i>meta</i> as source for
	 * variable substitution. If you allow duplicate entries by configuration,
	 * the method will return null only when no entries have been found.
	 *
	 * @param meta
	 *            The entry object providing values for variable substitution.
	 * @return The entry found or null if no entries or more than one entry was
	 *         found.
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public com.ibm.di.entry.Entry lookup(com.ibm.di.entry.Entry meta)
			throws Exception {
		Trace.entrymid(this, "lookup", meta);

		checkInitialize();

		try {
			handler.pushStackFrame(this);
			handler.declareBean("search", link);
			handler.declareBean("work", meta);

			if (handler.hasAttribute(OR_LOOKUP) && willExecuteSafeORHook()) {
				link.buildCriteria(meta, getBaseConfiguration(), parent);
				// link.buildCriteria( meta );

				com.ibm.di.entry.Entry entry = new com.ibm.di.entry.Entry();
				handler.declareBean("entry", entry);
				trigger(OR_LOOKUP, meta, null);
				persistedProperties.put(LAST_CONN, entry);

				if (entry.size() < 1) {
					return null;
				}

				stats.lookup();
				Trace.exitmid(this, "lookup", entry);
				return entry;
			}

			trigger("before_lookup", meta, null);

			link.buildCriteria(meta, getBaseConfiguration(), parent);
			com.ibm.di.entry.Entry e = (com.ibm.di.entry.Entry) executeOperation(
					SimulationConfig.SIM_OP_FIND_ENTRY, meta, null, link, null);
			persistedProperties.put(LAST_CONN, e);

			// If we find multiple entries, something needs to be done

			if (connector.getFindEntryCount() > 1) {

				if (ignoreMissingHooksInLookup) {
					// call the proper Hook
					currentEntry = null;
					trigger("lookup_multiple", meta, null);

					if (currentEntry != null)
						e = currentEntry;
					else
						e = connector.getFirstFindEntry();
				} else if (allowDuplicates) {
					// get the first instance and continue with the attribute
					// mapping.
					e = connector.getFirstFindEntry();
				} else {
					// call the proper Hook
					currentEntry = null;
					setSuccessful(false);
					if (!trigger("lookup_multiple", meta, null)) {
						log.exception("multiple.entried.found");
					}

					if (currentEntry != null) {
						setSuccessful(true);
						e = currentEntry;
					} else {
						Trace.exitmid(this, "lookup", null);
						return null;
					}
				}
			}

			persistedProperties.put(LAST_CONN, e);
			handler.declareBean("conn", e);

			if (e == null) {
				setSuccessful(false);
				if (trigger("lookup_nomatch", meta, null)
						|| ignoreMissingHooksInLookup)
					return null;

				log.exception("entry.not.found");
			}

			// Verify schema
			// verifySchema ( e, true );

			// Call hooks
			stats.lookup();
			handler.declareBean("current", e);
			trigger("after_lookup", meta, e);

			// Call attribute map
			imap.declareBean("work", meta);
			imap.declareBean("conn", e);
			meta = imap.mapEntry(e, meta);
			dumpObjects(e, meta, null);
			Trace.exitmid(this, "lookup", meta);
			return meta;
		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
	}

	/**
	 * This method calls the delete method. It is included since JavaScript and
	 * possibly other scripting languages choke on the "delete" keyword.
	 *
	 * @param meta
	 *            The work object to use for the link criteria
	 * @exception Exception
	 *                Any Exception
	 * @see #delete(com.ibm.di.entry.Entry)
	 */
	public void deleteEntry(com.ibm.di.entry.Entry meta) throws Exception {

		delete(meta);

	}

	/**
	 * This method implements the Delete mode operation.
	 *
	 * @param meta
	 *            The work object to use for the link criteria
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public void delete(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "delete", meta);
		checkInitialize();

		try {
			handler.pushStackFrame(this);
			handler.declareBean("search", link);
			handler.declareBean("work", meta);

			if (handler.hasAttribute(OR_DELETE) && willExecuteUnSafeORHook()) {
				if (!isDeltaMode())
					link.buildCriteria(meta, getBaseConfiguration(), parent);
				// For end_of_flow Hooks
				handler.declareBean("current", null);
				trigger(OR_DELETE, meta, null);

				stats.del();
				Trace.exitmid(this, "delete");
				return;
			}

			//
			// If we use delta-optimize at connector level then skip the lookup
			// operation if possible
			//
			com.ibm.di.entry.Entry e = meta;

			if (isDeltaMode()) {
				e = null;

			} else if (config.supportsSkipLookup() && config.getSkipLookup()) {
				// skip just prepare the linkCriteria
				link.buildCriteria(meta, getBaseConfiguration(), parent);
				e = null;

			} else {

				trigger("before_lookup", meta, null);

				link.buildCriteria(meta, getBaseConfiguration(), parent);
				// link.buildCriteria( meta );
				// do the lookup
				e = (com.ibm.di.entry.Entry) executeOperation(
						SimulationConfig.SIM_OP_FIND_ENTRY, meta, null, link,
						null);

				persistedProperties.put(LAST_CONN, e);
				// For old delete_ok Hooks
				handler.declareBean("conn", e);
				handler.declareBean("current", e);

				// Call handler for multiple entries found?
				if (connector.getFindEntryCount() > 1) {
					currentEntry = null;
					setSuccessful(false);
					if (!trigger("delete_multiple", meta, null)) {
						log.exception("multiple.entried.found");
					}

					if (currentEntry != null) {
						setSuccessful(true);
						e = currentEntry;
						handler.declareBean("current", e);
					} else {
						Trace.exitmid(this, "delete");
						return;
					}
				}

				if (e == null) {
					setSuccessful(false);
					if (trigger("delete_nomatch", meta, null)) {
						Trace.exitmid(this, "delete");
						return;
					}

					log.exception("entry.not.found");
				}

				persistedProperties.put(LAST_CONN, e);
				trigger("after_lookup", meta, e);

				// Call attribute map
				imap.declareBean("work", meta);
				imap.declareBean("conn", e);
				meta = imap.mapEntry(e, meta);
				dumpObjects(e, meta, null);
			}

			trigger("before_delete", meta, e);

			// do the delete
			executeOperation(SimulationConfig.SIM_OP_DELETE_ENTRY, meta, e,
					link, null);

			if ((connector instanceof SkipLookupInterface)
					&& config.getSkipLookup()) {
				stats.addMultipleDel(((SkipLookupInterface) connector)
						.getNumSkipLookupAffected());
			} else {
				stats.del();
			}
			trigger("after_delete", meta, e);

		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "delete");
	}

	/**
	 * This method implements the Update mode operation.
	 *
	 * @param meta
	 *            The work entry
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public void update(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "update", meta);
		checkInitialize();

		try {
			handler.pushStackFrame(this);
			trigger("before_update", meta, null);

			handler.declareBean("search", link);
			handler.declareBean("work", meta);

			if (handler.hasAttribute(OR_UPDATE) && willExecuteUnSafeORHook()) {

				// For end_of_flow Hooks
				handler.declareBean("current", null);
				// handler.declareBean ("connector", connector);
				trigger(OR_UPDATE, meta, null);

				stats.lookup(); // for lack of something better
				Trace.exitmid(this, "update");
				return;
			}

			com.ibm.di.entry.Entry e = null;

			if (config.supportsSkipLookup() && config.getSkipLookup()) {
				// prepare the linkCriteria and modify directly
				link.buildCriteria(meta, getBaseConfiguration(), parent);
				modify(e, meta);

			} else {

				// Lookup existing entry
				trigger("before_lookup", meta, null);

				link.buildCriteria(meta, getBaseConfiguration(), parent);
				// link.buildCriteria( meta );
				e = (com.ibm.di.entry.Entry) executeOperation(
						SimulationConfig.SIM_OP_FIND_ENTRY, meta, null, link,
						null);

				if (connector.getFindEntryCount() > 1) {
					setSuccessful(false);
					currentEntry = null;
					if (!trigger("update_multiple", meta, null)) {
						log.exception("multiple.entried.found");
					}

					if (currentEntry != null) {
						setSuccessful(true);
						e = currentEntry;
					} else {
						Trace.exitmid(this, "update");
						return;
					}
				}

				stats.lookup();
				handler.declareBean("current", e);
				handler.declareBean("conn", e);
				trigger("after_lookup", meta, e);

				if (e == null) {
					log.fine("call.add");
					add1(meta);
				} else {
					log.fine("call.modify");
					modify(e, meta);
				}
			}

			trigger("after_update", meta);

		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "update");
	}

	/**
	 * This method implements the Modify operation (called by Update if the
	 * entry exists)
	 *
	 * @param old
	 *            The current entry in the destination system
	 * @param meta
	 *            The new entry to use in the attribute map
	 * @exception Exception
	 *                Any Exception
	 */
	public void modify(com.ibm.di.entry.Entry old, com.ibm.di.entry.Entry meta)
			throws Exception {
		Trace.entrymid(this, "modify", old, meta);

		com.ibm.di.entry.Entry upd; // The mapped output entry (all attributes)
		com.ibm.di.entry.Entry upd3; // Existing entry with attributes
		// corresponding to upd
		com.ibm.di.entry.Entry apply; // The final after applying
		// compute_changes/local_delta
		// to upd3

		//
		// Override operation?
		//
		if (handler.hasAttribute(OR_MODIFY) && willExecuteUnSafeORHook()) {
			handler.declareBean("current", old);
			// handler.declareBean ("connector", connector);
			trigger(OR_MODIFY, meta, null);

			stats.mod();
			Trace.exitmid(this, "modify");
			return;
		}

		//
		// Call the attribute map
		//
		upd = new com.ibm.di.entry.Entry(meta.isDOMEnabled() || (old != null && old.isDOMEnabled()));
		upd.setOp(meta.getOp());
		modmap.declareBean("work", meta);
		modmap.declareBean("conn", upd);
		modmap.declareBean("current", old);
		persistedProperties.put(LAST_CONN, upd);
		upd = modmap.mapEntry(meta, upd);
		dumpObjects(upd, meta, old);

		//
		// Before modify hook
		//
		handler.declareBean("current", old);
		trigger("before_modify", meta, upd);

		//
		// Pick only those attributes selected by the attribute map.
		// Build two entries consisting of assigned attributes and one with the
		// corresponding
		// existing attributes (if we have a current object).
		//

		upd3 = new com.ibm.di.entry.Entry(upd.isDOMEnabled());

		if (old != null) {
			for (String name: upd.getAttributeNames()) {
				// Get current attribute
				Attribute b = old.getAttribute(name);
				if (b != null) {
					log.fine("modify.attribute.included", name);
					upd3.setAttribute(b);
				}
			}
		}

		//
		// Compute changes only if entry is NOT a delta entry, is NOT
		// hierarchical and we are requested to do so.
		// If we compute changes, we generate a delta-entry if the connector
		// understands delta updates.
		//
		if (compute_changes && supportsComputeChanges(old, meta)) {
			// Compute changes for generic entry

			ArrayList<ModificationItem> mods = compare
					.compareEntries(upd, upd3);
			apply = Compare.applyMods(upd, mods);

			if (mods.size() == 0) {
				setSuccessful(false);
				stats.nochange();
				handler.declareBean("current", old);
				trigger("modify_nochange", meta, apply);
				Trace.exitmid(this, "modify");
				return;
			}

		} else if (meta.getOp() != com.ibm.di.entry.Entry.OP_GEN && old != null && !upd.isDOMEnabled()) {
			// Apply delta changes and remove unmodified attributes from
			// new entry if we have a current entry and the entry is not hierarchical
			DeltaEntry.applyDelta(upd3, upd, compute_changes);
			apply = upd3;

		} else {
			// If we don't compute changes for a generic entry, or we have a
			// delta entry for delta savvy connector
			apply = upd;

		}

		if (compute_changes && getType() == ServerConstants.TYPE_UPDATE
				&& apply.size() != 0) {
			// Trigger just before we apply changes
			handler.declareBean("current", old);
			trigger("modify_apply", meta, apply);
		}

		// Any changes at all?
		if (apply.size() == 0) {
			if (modmap.isEmpty())
				log.warn("cannot.modify");
			stats.nochange();
			setSuccessful(false);
			handler.declareBean("current", old);
			trigger("modify_nochange", meta, apply);
			Trace.exitmid(this, "modify");
			return;
		}

		//
		// Each connector may further optimize changes and tells us by throwing
		// the NoChanges exception
		//
		try {

			// do the modify
			executeOperation(SimulationConfig.SIM_OP_MOD_ENTRY, meta, apply,
					link, old);

		} catch (NoChangesException nce) {
			stats.nochange();
			setSuccessful(false);
			handler.declareBean("current", old);
			trigger("modify_nochange", meta, apply);
			Trace.exitmid(this, "modify");
			return;
		}

		//
		// Trigger the after hook
		//
		handler.declareBean("current", old);
		trigger("after_modify", meta, apply);

		//
		// Update mod counter
		//
		if ((connector instanceof SkipLookupInterface)
				&& config.getSkipLookup()) {
			stats.addMultipleMod(((SkipLookupInterface) connector)
					.getNumSkipLookupAffected());
		} else {
			stats.mod();
		}
		Trace.exitmid(this, "modify");
	}

	/**
	 * Checks whether compute changes are supported for these entries.
	 *
	 * @param oldEntry
	 *            old entry.
	 * @param newEntry
	 *            new entry.
	 * @return <code>true</code> if it is supported, <code>false</code>
	 *         otherwise.
	 */
	private boolean supportsComputeChanges(com.ibm.di.entry.Entry oldEntry, com.ibm.di.entry.Entry newEntry) {
		boolean isSupported = true;
		if (oldEntry != null && oldEntry.isDOMEnabled()) {
			log.warn("HIERARCHICAL.ENTRIES.NOT.SUPPORTED.BY.COMPUTE.CHANGES");
			isSupported = false;
		} else if (newEntry.getOp() != com.ibm.di.entry.Entry.OP_GEN) {
			isSupported = false;
		}

		return isSupported;
	}

	/**
	 * This method implements the AddOnly mode operation.
	 *
	 * @param meta
	 *            The work entry to add
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "add", meta);

		try {
			checkInitialize();
			handler.pushStackFrame(this);
			add1(meta);
		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "add");
	}

	private void add1(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "add1", meta);

		// allow overriding script only when it is defined/enabled and we are in
		// appropriate simulation mode if the AL is simulating.
		if (handler.hasAttribute(OR_ADD) && willExecuteUnSafeORHook()) {
			persistedProperties.put(LAST_CONN, meta);
			trigger(OR_ADD, meta, null);

			stats.add();
			Trace.exitmid(this, "add1");
			return;
		}

		com.ibm.di.entry.Entry upd = new com.ibm.di.entry.Entry(meta.isDOMEnabled());
		upd.setOp(meta.getOp());
		addmap.declareBean("work", meta);
		addmap.declareBean("conn", upd);
		persistedProperties.put(LAST_CONN, upd);
		addmap.mapEntry(meta, upd);
		dumpObjects(upd, meta, null);

		trigger("before_add", meta, upd);

		// Verify schema
		// verifySchema ( upd, false );

		if (upd.size() == 0
				&& (getType() == ServerConstants.TYPE_UPDATE || getType() == ServerConstants.TYPE_DELTA)) {
			if (addmap.isEmpty())
				log.warn("cannot.add");
			setSuccessful(false);
			trigger("add_abandon", meta, upd);

			throw new IgnoreEntryException(getLog()
					.getString("novalues.to.add"));
		}

		// do the add
		executeOperation(SimulationConfig.SIM_OP_PUT_ENTRY, meta, upd, null,
				null);

		stats.add();

		trigger("after_add", meta, upd);
		Trace.exitmid(this, "add1");
	}

	/**
	 * This method is used to send a reply entry to a connector
	 *
	 * @param meta
	 *            The work entry to send back
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public void reply(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "reply", meta);
		checkInitialize();

		try {

			handler.pushStackFrame(this);

			if (handler.hasAttribute(OR_REPLY) && willExecuteSafeORHook()) {
				persistedProperties.put(LAST_CONN, meta);
				trigger(OR_REPLY, meta, null);

				stats.add();
				Trace.exitmid(this, "reply");
				return;
			}

			com.ibm.di.entry.Entry conn = new com.ibm.di.entry.Entry(meta != null && meta.isDOMEnabled());
			addmap.declareBean("work", meta);
			addmap.declareBean("conn", conn);
			persistedProperties.put(LAST_CONN, conn);
			conn = addmap.mapEntry(meta, conn);
			dumpObjects(conn, meta, null);

			trigger("before_reply", meta, conn);

			// Do the reply
			executeOperation(SimulationConfig.SIM_OP_REPLY_ENTRY, meta, conn,
					null, null);

			stats.reply();

			trigger("after_reply2", meta, conn);

		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "reply");
	}

	/**
	 * This method implements the CallReply mode operation.
	 *
	 * @param meta
	 *            The work entry to send
	 * @exception Exception
	 *                the component is not initialized or the underlying
	 *                Connector raised an error or some of the user-defined
	 *                hooks raised an error
	 */
	public void callreply(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "callreply", meta);
		checkInitialize();

		try {
			handler.pushStackFrame(this);

			if (handler.hasAttribute(OR_CALLREPLY) && willExecuteSafeORHook()) {
				persistedProperties.put(LAST_CONN, meta);
				trigger(OR_CALLREPLY, meta, null);

				stats.callreply();
				Trace.exitmid(this, "callreply");
				return;

			}

			com.ibm.di.entry.Entry upd = new com.ibm.di.entry.Entry(meta != null && meta.isDOMEnabled());
			addmap.declareBean("work", meta);
			addmap.declareBean("conn", upd);
			persistedProperties.put(LAST_CONN, upd);
			addmap.mapEntry(meta, upd);
			dumpObjects(upd, meta, null);

			trigger("before_call", meta, upd);

			// Verify schema
			// verifySchema ( upd, false );

			// Do the Call
			com.ibm.di.entry.Entry res = (com.ibm.di.entry.Entry) executeOperation(
					SimulationConfig.SIM_OP_QUERY_REPLY, meta, upd, null, null);
			stats.callreply();

			if (res == null) {
				setSuccessful(false);
				if (!trigger("no_reply", meta))
					log.exception("entry.not.found");
			} else {
				// Verify schema
				// verifySchema ( res, true );

				persistedProperties.put(LAST_CONN, res);
				trigger("after_reply", meta, res);

				imap.declareBean("work", meta);
				imap.declareBean("conn", res);
				imap.mapEntry(res, meta);
				dumpObjects(res, meta, null);
			}

		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "callreply");
	}

	/**
	 * This method implements the Delta mode operation.
	 *
	 * @param meta
	 *            The delta entry
	 * @exception Exception
	 *                Any Exception
	 */
	public void delta(com.ibm.di.entry.Entry meta) throws Exception {
		Trace.entrymid(this, "delta", meta);
		com.ibm.di.entry.Entry deltaEntry = null;

		// Skip non-delta and unchanged entries
		if (meta.getOp() == com.ibm.di.entry.Entry.OP_GEN) {
			if (config.getDeltaStrict())
				throw new Exception(getLog().getString(
						"assemblyline.comp.nodeltaentry.received.error"));

			stats.ignore();
			debug("assemblyline.comp.ignoring.nondeltaentry.info");
			Trace.exitmid(this, "delta");
			return;

		} else if (meta.getOp() == com.ibm.di.entry.Entry.OP_UNCHANGED) {
			stats.nochange();
			Trace.exitmid(this, "delta");
			return;

		}

		// If old-style delta received then do some magic with it first
		if (meta.getOp() == com.ibm.di.entry.Entry.OP_MOD
				&& meta.getProperty("sequence") != null
				&& meta.getProperty("delta.old") != null) {
			com.ibm.di.entry.Entry old = (com.ibm.di.entry.Entry) meta
					.getProperty("delta.old");
			deltaEntry = DeltaEntry.compareEntries(meta, old, false,
					DeltaEntry.COMPARE_ATTRIBUTE_VALUES, null);
			if (log.getDebug()) {
				log.debug("assemblyline.comp.generated.newstyle.entry.info");
				log.debug("assemblyline.comp.origin.delta.entry.info");
				log.dumpEntry(meta);
				log.debug("assemblyline.comp.newdelta.entry.info");
				log.dumpEntry(deltaEntry);
			}
		}

		checkInitialize();

		try {
			handler.pushStackFrame(this);
			trigger("before_delta", meta, null);

			handler.declareBean("search", link);
			handler.declareBean("work", meta);

			if (handler.hasAttribute(OR_DELTA) && willExecuteUnSafeORHook()) {
				link.buildCriteria(meta, getBaseConfiguration(), parent);
				handler.declareBean("current", null);
				persistedProperties.put(LAST_CONN, meta);
				trigger(OR_DELTA, meta, null);

				stats.lookup(); // for lack of something better
				Trace.exitmid(this, "delta");
				return;
			}

			com.ibm.di.entry.Entry e = null;

			// Only do lookup if necessary (if connector does not support delta
			// entries)
			if (connector.isDeltaSupported()) {
				link.buildCriteria(meta, getBaseConfiguration(), parent);
				// link.buildCriteria( meta );

			} else if (meta.getOp() != com.ibm.di.entry.Entry.OP_ADD) {
				// no action is needed if the operation is ADD

				trigger("before_lookup", meta, null);

				link.buildCriteria(meta, getBaseConfiguration(), parent);
				// link.buildCriteria( meta );
				e = (com.ibm.di.entry.Entry) executeOperation(
						SimulationConfig.SIM_OP_FIND_ENTRY, meta, null, link,
						null);

				if (connector.getFindEntryCount() > 1) {
					currentEntry = null;
					setSuccessful(false);
					if (!trigger("lookup_multiple", meta, null)) {
						log.exception("multiple.entried.found");
					}

					if (currentEntry != null) {
						setSuccessful(true);
						e = currentEntry;
					} else {
						return;
					}
				}

				if (e == null) {
					setSuccessful(false);
					if (trigger("lookup_nomatch", meta, null))
						return;

					log.exception("entry.not.found");
				}

				stats.lookup();
				handler.declareBean("current", e);
				trigger("after_lookup", meta);
			}

			log.debug("assemblyline.comp.delta.operation.info", meta
					.getOperation());
			switch (meta.getOp()) {
			case com.ibm.di.entry.Entry.OP_ADD:
				log.fine("call.add");
				add1(meta);
				break;
			case com.ibm.di.entry.Entry.OP_MOD:
				log.fine("call.modify");
				modify(e, (deltaEntry != null ? deltaEntry : meta));
				break;
			case com.ibm.di.entry.Entry.OP_DEL:
				if (config.getDeltaBehavior() == ConnectorConfig.DELTA_NO_DELETE) {
					stats.skip();
				} else {
					log.fine("call.delete");
					delete(meta);
				}
				break;
			}

			trigger("after_delta", meta);

		} finally {
			handler.popStackFrame();
			checkTerminate();
		}
		Trace.exitmid(this, "delta");
	}

	/**
	 *
	 * @param meta
	 *            an Entry to be added to the data source by the Connector
	 * @exception Exception
	 *                problem while adding the Entry to the data source
	 * @deprecated replaced by {@link #add(com.ibm.di.entry.Entry)}
	 */
	@Deprecated
	public void dumpEntry(com.ibm.di.entry.Entry meta) throws Exception {
		if (addmap != null) {
			connector.putEntry(addmap.mapEntry(meta, null));
		} else {
			connector.putEntry(meta);
		}
	}

	/**
	 * Write a message to the log file prefixed by this connector's name
	 *
	 * @param msg
	 *            The message to write
	 */
	public void logmsg(String msg) {
		log.loginfo(msg);
	}

	/**
	 * Write a message to the log file prefixed by this connector's name
	 *
	 * @param msg
	 *            The message to write
	 */
	public void debug(String msg) {
		log.logdebug(msg);
	}

	/**
	 * This method is called by the AssemblyLine. The aOper is any of the
	 * standard modes. The mode specific error hook is called first, then the
	 * "default" error hook is called.
	 *
	 * @param aOper
	 *            Hook prefix
	 * @param e
	 *            The error causing this hook to be called, or null for no error
	 * @param meta
	 *            The work entry
	 *
	 * @exception Exception
	 *                Throws the parameter e if we cannot or do not want to
	 *                handle it, or an Exception from the Hooks.
	 */
	public void handleException(String aOper, Throwable e,
			com.ibm.di.entry.Entry meta) throws Exception {

		if (e == null) {
			// impossible
			return;
		}

		// These are not errors
		if (e instanceof RestartEntryException
				|| e instanceof RetryEntryException
				|| e instanceof IgnoreEntryException
				|| e instanceof SkipEntryException
				|| e instanceof SkipToException
				|| e instanceof ExitBranchException
				|| e instanceof ContinueLoopException
				|| e instanceof AbortALException)
			throw (Exception) e;

		setSuccessful(false);

		if (stats != null) {
			stats.err();
			stats.exception(e);
		}

		String oper = (aOper.equals(INITIALIZE_HOOKS) || aOper.equals(SELECT)) ? INITIALIZE
				: aOper;

		if (handler == null) {
			if ((!aOper.equals(INITIALIZE)) && (!aOper.equals(SELECT)))
				log.error("assemblyline.comp.handleex.no.handler", oper, e);
			if (e instanceof Exception)
				throw (Exception) e;
			else
				throw new Exception(e);
		}

		log.debug("assemblyline.comp.handleex.info", oper, e.toString());

		com.ibm.di.entry.Entry src = new com.ibm.di.entry.Entry();

		src.setAttribute("status", "fail");
		src.setAttribute("exception", e);
		src.setAttribute("message", e.getMessage());
		src.setAttribute("class", e.getClass().getName());
		src.setAttribute("operation", oper);
		src.setAttribute("connectorname", getName());

		if(connector instanceof Connector){
			((Connector) connector).extractExceptionInformation(src);
		}
		handler.declareStaticBean("error", src);

		try {
			handler.pushStackFrame(this);

			// The following block is only for connectors:
			if (connector != null && !aOper.equals(INITIALIZE_HOOKS) && reconnectRuleEngine != null &&
					reconnectRuleEngine.getReconnectChoice(connector, e) == ReconnectRuleEngine.RECONNECT) {

				// Try to reconnect
				boolean success = reconnectOnException(meta, src, aOper);

				if (! success) {
					// Try to failOver
					if (isFailOvered())
						success = failBack(meta, src);
					else
						success = failOver(meta, src);
				}

				if (success) {
					if (oper.equals(INITIALIZE)) {
						return;
					} else {
						throw new RetryEntryException(getLog().getString("assemblyline.comp.autoreconnect.info"));
					}
				}
			}

			/*
			 * If we are here, the reconnect option must be
			 * ReconnectRuleEngine.ERROR, or we failed to reconnect,
			 * so admit that there is an error
			 * and let the hooks do their job.
			 */

			boolean handled = trigger(oper + "_fail", meta, null);

			if (!((INITIALIZE.equals(oper)) || "close".equals(oper)))
				handled |= trigger("default_fail", meta, null);

			if (!handled) {
				log.error("assemblyline.comp.handleex.not.handled", oper, e);
				if (e instanceof Exception)
					throw (Exception) e;
				else
					throw new Exception(e);
			}
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * This method is called by handleException.
	 *
	 * @param meta
	 *            The work entry
	 * @exception Exception
	 *                an Exception from the Hook.
	 */
	private boolean reconnectOnException(com.ibm.di.entry.Entry meta,
			com.ibm.di.entry.Entry error, String oper) throws Exception {

		ReconnectConfig rc = config.getReconnectConfig();

		if (oper.equals(INITIALIZE) || oper.equals(SELECT)) {
			trigger("connect_init", meta, null);
			if (!rc.getInitReconnect())
				return false;
		} else {
			trigger("on_connection_failure", meta, null);

			if (!rc.getAutoReconnect())
				return false;
		}

		int retries = rc.getRetries() - recentReconnectAttempts;
		int delay = rc.getDelay();
		Exception newException = null;

		for (; retries > 0; retries--) {
			Thread.sleep(delay * 1000);

			if (stats != null)
				stats.reconnect();

			try {
				shouldTerminate = true;
				if (oper == INITIALIZE) {
					connector.initialize(new ConnectorMode(getType()));
				} else {
					recentReconnectAttempts++;
					connector.reconnect(new ConnectorMode(getType()));
					if (rc.getAutoSkipForward())
						doConnectorSkipForward();
				}
				trigger("reconnect_ok", meta, null);
				return true;
			} catch (Exception err) {
				newException = err;
			}
		}

		if (newException != null
				&& reconnectRuleEngine.getReconnectChoice(connector,
						newException) != ReconnectRuleEngine.RECONNECT) {

			error.setAttribute("operation", "Automatic Reconnect ("
					+ error.getString("message") + ")");
			error.setAttribute("originalexception", error
					.getObject("exception"));
			error.setAttribute("exception", newException);
			error.setAttribute("message", newException.getMessage());
			error.setAttribute("class", newException.getClass().getName());
		}
		trigger("reconnect_fail", meta, null);
		return false;
	}

	/**
	 *  Attempt a failOver.
	 * @param work The work entry
	 * @param weeoe The error Entry (may be filled in if an Exception occurs)
	 * @exception Exception
	 *                an Exception from the Hook.
	 * @return true if we could do a failover
	 */
	public boolean failOver(com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry error) throws Exception {

		if (isFailOvered())
			return false;

		ReconnectConfig rc = config.getReconnectConfig();
		if (! loadFailoverConnector(rc))
			return false;

		Exception newException = null;

		try {
			failoverConnector.initialize(new ConnectorMode(getType()));
			doConnectorTerminate();
			is_initialized = true;
			shouldTerminate = true;
			origConnector = connector;
			connector = failoverConnector;
			if (getType() == ServerConstants.TYPE_ITERATOR) {
				doConnectorSelectEntries();
				if (rc.getAutoSkipForward())
					doConnectorSkipForward();
			}
		} catch (Exception err) {
			newException = err;
		}

		if (newException == null) {
			if (rc.getFailbackAfter() > 0)
				tryFailback = new Date().getTime() + rc.getFailbackAfter()*1000;
			else
				tryFailback = 0;
			trigger("failover_ok", work, null);
			return true;
		} else {
			if (connector == failoverConnector) {
				doConnectorTerminate();
				connector = origConnector;
			}
			if (error == null) {
				error = new com.ibm.di.entry.Entry();
				error.setAttribute("status", "fail");
				error.setAttribute("connectorname", getName());
				error.setAttribute("operation", "Automatic Failover");
				handler.declareStaticBean("error", error);
			} else {
				error.setAttribute("originalexception",
						error.getObject("exception"));
				error.setAttribute("operation", "Automatic Failover ("
						+ error.getString("message") + ")");
			}
			error.setAttribute("exception", newException);
			error.setAttribute("message", newException.getMessage());
			error.setAttribute("class", newException.getClass().getName());

			trigger("failover_fail", work, null);
			return false;
		}
	}

	/**
	 * Attempt to fall back to the original connector after a failOver.
	 * @param work The work entry
	 * @param weeoe The error Entry (may be filled in if an Exception occurs)
	 * @exception Exception
	 *                an Exception from the Hook.
	 * @return true if we could do a failover
	 */
	public boolean failBack(com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry error) throws Exception {

		if (!isFailOvered() || failoverConnector == null)
			return false;

		ReconnectConfig rc = config.getReconnectConfig();
		Exception newException = null;

		try {
			origConnector.initialize(new ConnectorMode(getType()));
			doConnectorTerminate();
			is_initialized = true;
			shouldTerminate = true;
			connector = origConnector;
			failoverConnector = null;
			tryFailback = 0;
			if (getType() == ServerConstants.TYPE_ITERATOR) {
				doConnectorSelectEntries();
				if (rc.getAutoSkipForward())
					doConnectorSkipForward();
			}
		} catch (Exception err) {
			newException = err;
		}

		if (newException == null) {
			trigger("failback_ok", work, null);
			return true;
		} else {
			if (rc.getFailbackAfter() > 0)
				tryFailback = new Date().getTime() + rc.getFailbackAfter()*1000;
			else
				tryFailback = 0;
			if (error == null) {
				error = new com.ibm.di.entry.Entry();
				error.setAttribute("status", "fail");
				error.setAttribute("connectorname", getName());
				handler.declareStaticBean("error", error);
			}
			error.setAttribute("operation", "Automatic Failback");
			error.setAttribute("exception", newException);
			error.setAttribute("message", newException.getMessage());
			error.setAttribute("class", newException.getClass().getName());

			trigger("failback_fail", work, null);
			return false;
		}
	}

	/**
	 * This method is called by the AssemblyLine if the operation oper is
	 * successful. The mode specific success hook is called first, then the
	 * "default" success hook is called.
	 *
	 * @param oper
	 *            Hook prefix
	 * @param meta
	 *            The work entry
	 * @exception Exception
	 *                Any Exception from the Hooks
	 */
	public void handleSuccess(String oper, com.ibm.di.entry.Entry meta)
			throws Exception {
		recentReconnectAttempts = 0;

		if (handler == null)
			return;

		final String operOKHook = oper + "_ok";
		final String defaultOKHook = "default_ok";

		// optimization to avoid pushStackFrame if there is no hook
		boolean useStackFrame = handler.hasAttribute(operOKHook)
				|| handler.hasAttribute(operOKHook);

		try {
			if (useStackFrame) {
				handler.pushStackFrame(this);
			}

			trigger(operOKHook, meta);

			trigger(defaultOKHook, meta);

		} finally {
			if (useStackFrame) {
				handler.popStackFrame();
			}
		}
	}

	/**
	 * Calls the hook named oper, declaring work and conn as the corresponding
	 * beans. The trigger function calls one of the AssemblyLine hooks defined
	 * for this Connector using the provided conn/work.
	 *
	 * @param oper
	 *            Name of the hook to call
	 * @param work
	 *            This will be the work bean in the hook
	 * @param conn
	 *            This will be the conn bean in the hook
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) throws Exception {
		return triggerImpl(oper, work, conn);
	}

	/**
	 * Calls the hook named oper, declaring work as the corresponding bean. The
	 * trigger function calls one of the AssemblyLine hooks defined for this
	 * Connector using the provided work.
	 *
	 * @param oper
	 *            Name of the hook to call
	 * @param work
	 *            This will be the work bean in the hook
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work)
			throws Exception {
		return triggerImpl(oper, work, null);
	}

	/**
	 * Calls the hook named oper
	 *
	 * @param oper
	 *            Name of the hook to call
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	public boolean trigger(String oper) throws Exception {
		return triggerImpl(oper, null, null);
	}

	/**
	 * Calls the hook named oper, declaring work and conn as the corresponding
	 * beans. The trigger function calls one of the AssemblyLine hooks defined
	 * for this Connector using the provided conn/work.
	 *
	 * @param oper
	 *            Name of the hook to call
	 * @param work
	 *            This will be the work bean in the hook. Maybe null.
	 * @param conn
	 *            This will be the conn bean in the hook. Maybe null.
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 * @exception Exception
	 *                Any exception thrown by the execution of the hook
	 */
	protected boolean triggerImpl(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) throws Exception {

		if (work != null) {
			handler.declareBean("work", work);
		}
		if (conn != null) {
			handler.declareBean("conn", conn);
		}

		// If user sets breakpoint then stop no matter what
		if (parent.debuggerEnabled()) {
			parent.debugBreak(getName() + "." + oper);
		}

		if (handler.hasAttribute(oper)) {
			addHookInvoked(oper);
			handler.eval(oper);
			return true;
		} else {
			// handler.releaseBeans();
			log.fine("trigger.dont", oper);
			return false;
		}
	}

	/**
	 * Does nothing!
	 *
	 * @param cf
	 *            ignored
	 * @throws Exception
	 *             never
	 * @deprecated
	 */
	@Deprecated
	public void expandParameters(BaseConfiguration cf) throws Exception {
	}

	/**
	 * Load configuration and ConnectorInterface.
	 *
	 * @throws Exception
	 *             If the ConnectorInterface cannot be loaded
	 */
	private void loadConfig() throws Exception {
		Trace.entrymin(this, "loadConfig");

		String str = connConfig.getJavaClass();
		String inherit = connConfig.getInheritsFromRef();

		log.debug("connector.inherits", str, inherit);

		// Virtual means that if we get a connector provided in the constructor
		// then we will use that one rather than our own.
		if (forceRuntime || str == null
				|| ServerConstants.VIRTUAL_CONNECTOR_NAME.equals(inherit)) {
			connector = input_connector;
			if (connector == null) {
				if (str == null
						&& inherit != null
						&& !ServerConstants.VIRTUAL_CONNECTOR_NAME
								.equals(inherit)) {
					BaseConfiguration bc = connConfig;
					while (bc.getInheritsFrom() != null) {
						bc = bc.getInheritsFrom();
					}

					if (bc.getParent() != null)
						bc = bc.getParent();

					if (bc.getInheritsFromRef() != null
							&& !bc.getInheritsFromRef().equals("[parent]"))
						inherit = bc.getInheritsFromRef();

					log.exception(
							"assemblyline.comp.cannotfind.javaclinherit.warn",
							inherit);
				} else {
					log.exception("runtime.connector.not.provided");
				}
			} else {
				log.info("using.provided.connector");
			}
		} else if (str.startsWith("@")) {
			// We are reusing another connector in the AssemblyLine
			log.debug("reuse.connector", str.substring(1));
			AssemblyLineComponent tc = parent.getConnector(str.substring(1));
			if (tc != null)
				connector = tc.connector;
			reusingConnector = true;
		} else if ((config.getPoolInstanceConfig() != null)
				&& config.getPoolInstanceConfig().getPoolEnabled()) {

			RS rs = (RS) parent.getParent();

			String inheritsFromShortName = config.getInheritsFrom()
					.getShortName();
			connPool = rs.getConnectorPool(inheritsFromShortName);
			if (connPool == null) {
				log.exception("cannot.load.connector.pool.no.pool",
						inheritsFromShortName);
			}

			PoolInstanceConfig poolConfig = config.getPoolInstanceConfig();
			int exhBehavior = poolConfig.getExhaustedPoolBehavior();

			boolean waitForConn = (exhBehavior == PoolInstanceConfig.EXHAUSTED_POOL_WAIT);
			connector = connPool.getConnector(waitForConn);

			if (connector != null) {
				pooledConnector = true;
			} else {
				log.exception("cannot.load.connector.pool",
						inheritsFromShortName);
			}
		} else {
			connector = SystemFunctions.loadConnector(config, null);
			if (connector instanceof Connector) {
				connector.setLog(log);
				connector.setRSInterface(parent.getParent());
				log.debug("loaded.conn.version", str, ((Connector) connector)
						.getVersion());
			}
		}

		if (connector == null) {
			log.exception("cannot.load.connector", str);
		}

		LinkCriteriaConfig lcc = config.getLinkCriteria();
		if (lcc.getAdvancedLinkMode()) {
			log.debug("using.advanced.link.criteria");
			link = new SearchCriteria(lcc.getAdvancedLinkCriteria(), parent
					.getScriptEngine());
		} else {
			link = new SearchCriteria();
			List<String> list = lcc.getCriteriaNames();
			for (int i = 0; i < list.size(); i++) {
				LinkCriteriaItem lci = lcc.getCriteria(list.get(i));
				if (lci.getEnabled() && lci.getAttribute() != null)
					link.addTemplate((String) lci.getAttribute(), lci.getMatch(),
						(String) lci.getValue());
			}
			if (lcc.getMatchAny())
				link.setType(SearchCriteria.SEARCH_OR);
		}

		type = ServerConstants.getType(config.getMode());

		log.debug("load.attribute.map");
		useInputMap(null);
		useOutputMap(null);

		// AssemblyLine Hooks
		log.debug("load.hooks");
		HooksConfig handlerMap = config.getHooks();
		handler = new AttributeMapping(getName(), parent, log, parent
				.getScriptEngine());
		if (handlerMap != null) {
			handler.loadEventMap(handlerMap);
		}

		compute_changes = config.getComputeChanges();
		Trace.exitmin(this, "loadConfig");
	}

	private AttributeMapConfig copyMap(AttributeMapConfig orig) {
		AttributeMapConfig map = new AttributeMapConfigImpl();
		map.setName(orig.getName());
		map.setNullBehavior(orig.getNullBehavior());
		map.setNullBehaviorValue(orig.getNullBehaviorValue());
		map.setNullDefinition(orig.getNullDefinition());
		map.setNullDefinitionValue(orig.getNullDefinitionValue());
		map.setParent(orig.getParent());
		return map;
	}

	/**
	 * Loads the failover Connector.
	 * @throws Exception
	 * @since 7.1.1
	 */
	private boolean loadFailoverConnector(ReconnectConfig rc) throws Exception {
		if (!rc.getFailoverOption())
			return false;
		String ref = rc.getFailoverConnectorName();
		if (ref == null || ref.trim().length() == 0) {
			log.error("AssemblyLineComponent.no.failover.connector");
			return false;
		}
		ConnectorConfig fail = new ConnectorConfigImpl();
		fail.setMetamergeConfig(config.getMetamergeConfig());
		fail.init();
		fail.getConnectionConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		fail.getParserConfig().setInheritsFromRef(BaseConfiguration.INHERIT_PARENT);
		fail.updateInheritsFrom(ref);
		fail.setName(name);
		failoverConnector = SystemFunctions.loadConnector(fail, null, parent.getParent());

		Log failoverLog = new Log(parent.getLog());
		failoverLog.setDebug(fail.getConnectionConfig().getDebug(log.getDebug()));
		failoverLog.setPrefix("[" + name + " [" + ref + "]] ");
		failoverConnector.setLog(failoverLog);

		failoverConnector.setContext(parent);
		return true;
	}

	/**
	 * This method does nothing.
	 *
	 * @param entry
	 *            - ignored
	 * @param input
	 *            - ignored
	 * @throws Exception
	 *             - never
	 * @deprecated
	 */
	@Deprecated
	public void verifySchema(com.ibm.di.entry.Entry entry, boolean input)
			throws Exception {
	}

	/**
	 * This method returns null
	 *
	 * @return null
	 * @throws Exception
	 *             - never
	 * @deprecated
	 */
	@Deprecated
	public com.ibm.di.entry.Entry getRestartInfoEntry() throws Exception {
		return null;
	}

	/**
	 * Always returns false
	 *
	 * @return false
	 * @throws Exception
	 *             - never
	 * @deprecated
	 */
	@Deprecated
	public boolean isCheckpointRestartEnabled() throws Exception {
		return false;
	}

	/**
	 * This method does nothing.
	 *
	 * @param state
	 *            - ignored
	 * @param rsi
	 *            - ignored
	 * @param restartPoint
	 *            - ignored
	 * @throws Exception
	 *             - never
	 *
	 * @deprecated
	 */
	@Deprecated
	public void setRestartInfoEntry(ALState state, com.ibm.di.entry.Entry rsi,
			int restartPoint) throws Exception {
	}

	protected void dumpObjects(com.ibm.di.entry.Entry conn,
			com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry current) {
		if (!getDebug())
			return;

		log.info("attrib.mapping.result");

		if (conn != null) {
			log.info("the.conn.object");
			log.dump(conn);
		}

		if (work != null) {
			log.info("the.work.object");
			log.dump(work);
		}

		if (current != null) {
			log.info("the.current.object");
			log.dump(current);
		}
	}

	/**
	 * Cause the underlying ConnectorInterface to reconnect to it's data source
	 *
	 * @return true if the reconnect operation completed successfully, false
	 *         otherwise.
	 * @throws Exception
	 *             if an error while reconnecting occurs.
	 */
	public boolean reconnect() throws Exception {
		Trace.entrymax(this, "reconnect");
		if (stats != null)
			stats.reconnect();
		try {
			connector.reconnect(new ConnectorMode(getType()));
			Trace.exitmax(this, "reconnect", true);
			return true;
		} catch (IOException ioe) {
			Trace.exitmax(this, "reconnect", false);
			return false;
		} catch (CommunicationException ce) {
			Trace.exitmax(this, "reconnect", false);
			return false;
		}
	}

	/**
	 * The method is called to map an entry using the configured input attribute
	 * map.
	 *
	 * @param work
	 *            The work entry
	 * @param conn
	 *            The conn entry
	 * @return The mapped entry
	 * @exception Exception
	 *                problem while mapping the entry
	 */
	public com.ibm.di.entry.Entry mapEntry(com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) throws Exception {
		try {
			handler.pushStackFrame(this);
			handler.declareBean("search", link);

			// Call attribute map
			imap.declareBean("work", work);
			imap.declareBean("conn", conn);
			work = imap.mapEntry(conn, work);
			dumpObjects(conn, work, null);

			return work;
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * Checks whether the connector needs to be (re)initialized or not.
	 *
	 * @return true if connector was (re)initialized
	 * @throws Exception
	 */
	public boolean checkInitialize() throws Exception {
		Trace.entrymax(this, "checkInitialize");
		boolean wasInitialized = false;
		boolean willInitialize = false;

		switch (initOption) {
		case ConnectorConfig.COMP_INIT_DEFAULT:
			willInitialize = (!is_initialized && initializeCount == 0);
			break;
		case ConnectorConfig.COMP_INIT_USE:
			willInitialize = (!is_initialized);
			break;
		case ConnectorConfig.COMP_INIT_MODIFIED:
			willInitialize = wasConfigurationModified();
			break;
		case ConnectorConfig.COMP_INIT_EVERYTIME:
			willInitialize = (!is_initialized);
			break;
		}

		if (willInitialize) {
			if (is_initialized) {
				doConnectorTerminate();
				is_initialized = false;
			}
			doInitialize();

			wasInitialized = true;
		}
		Trace.exitmax(this, "checkInitialize");

		return wasInitialized;
	}

	/**
	 * Checks whether the connector should be terminated (e.g. init option =
	 * always).
	 *
	 * @return true if connector was terminated
	 * @throws Exception
	 */
	public boolean checkTerminate() throws Exception {
		boolean wasTerminated = false;
		if (initOption == ConnectorConfig.COMP_INIT_EVERYTIME) {
			doConnectorTerminate();
			is_initialized = false;
			wasTerminated = true;
		}
		return wasTerminated;
	}

	/**
	 * @return true if the connection configuration has been modified since the
	 *         last call.
	 *
	 */
	public boolean wasConfigurationModified() {
		boolean ret = !is_initialized && initializeCount == 0;

		BaseConfiguration bc = null;
		if (connector != null)
			bc = connector.getRawConnectorConfiguration();
		else if ((this instanceof FunctionComponent)
				&& ((FunctionComponent) this).function != null)
			bc = ((FunctionComponent) this).function.getConfiguration();

		if (bc != null) {

			if (bc.getModified()) {
				ret = true;
				bc.setModified(false);
			}

			HashMap<String, Object> newMap = new HashMap<String, Object>();
			for (String param : bc
					.getKeys(BaseConfiguration.RECURSIVE_ONELEVEL))
				newMap.put(param, bc.getParameter(param));

			if (!newMap.equals(oldMap)) {
				ret = true;
				oldMap = newMap;
			}
		}
		return ret;
	}

	/**
	 * This methods defines the behavior of a Component when the AL is
	 * simulating
	 *
	 * @param state
	 *            - possible values:<br />
	 *            {@link SimulationConfig#SIM_ENABLED_STATE}<br />
	 *            {@link SimulationConfig#SIM_DISABLED_STATE}<br />
	 *            {@link SimulationConfig#SIM_SIMULATED_STATE}<br />
	 *            {@link SimulationConfig#SIM_PROXY_STATE}<br />
	 *            {@link SimulationConfig#SIM_SCRIPTED_STATE}<br />
	 * @throws Exception
	 *             if an error occurs
	 */
	public void setSimulatingState(String state) throws Exception {
		parent.getSimulationConfig().setComponentSimState(
				config.getShortName(), state);
	}

	/**
	 * The state of this component.
	 *
	 * @return one of the following:<br />
	 *         {@link SimulationConfig#SIM_ENABLED_STATE}<br />
	 *         {@link SimulationConfig#SIM_DISABLED_STATE}<br />
	 *         {@link SimulationConfig#SIM_SIMULATED_STATE}<br />
	 *         {@link SimulationConfig#SIM_PROXY_STATE}<br />
	 *         {@link SimulationConfig#SIM_SCRIPTED_STATE}<br />
	 * @throws Exception
	 *             if an error occurs
	 */
	public String getSimulatingState() throws Exception {

		return parent.getSimulationConfig().getComponentSimState(name);
	}

	/**
	 * This method is used to take care of dangerous/safe operations when the AL
	 * is simulating
	 *
	 * @param operation
	 *            the operation (the method that is called). Recognizable input
	 *            are the values putEntry, modEntry, deleteEntry, perform,
	 *            getNextClient, getNextEntry, findEntry, selectEntries,
	 *            replyEntry and queryReply
	 * @param work
	 *            The work entry. If a null is passed the method will get the
	 *            work entry from its parent (AssemblyLine) and if it still null
	 *            then a new work entry will be created. If the Proxy simulation
	 *            state is executed and the passed operation does not expect an
	 *            entry to be returned then the entry retrieved from the Proxy
	 *            AL will be merged with the work entry
	 * @param conn
	 *            For the operations that expect an entry to be returned and we
	 *            are with Scripted simulation state set then the conn entry
	 *            modified in the script will be used as the returned entry.
	 * @param search
	 *            The {@link SearchCriteria} object used to locate the entry to
	 *            be modified/deleted
	 * @param current
	 *            The old entry found by the search criteria this would be only
	 *            available for the modEntry operation and only then will be
	 *            exposed in a script
	 * @return Operations that expect an object to be returned are findEntry,
	 *         getNextEntry, queryReply and perform. In case one of this
	 *         operations are simulated then either empty Entry is returned or
	 *         the entry retrieved form the proxy AL or the conn entry modified
	 *         by executed script
	 * @throws Exception
	 *             in case the call to one of the methods fails.
	 * @throws ClassCastException
	 *             in case the parent of the AssemblyLineComponent is not an
	 *             instance of the AssemblyLine class
	 * @since 7.0
	 */
	protected Object executeOperation(int operation,
			com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry conn,
			SearchCriteria search, com.ibm.di.entry.Entry current)
			throws Exception {

		if (parent.isSimulating()) {

			// operations considered safe
			boolean safeOp = false;
			switch (operation) {
			case SimulationConfig.SIM_OP_GET_NEXT_CLIENT:
			case SimulationConfig.SIM_OP_GET_NEXT_ENTRY:
			case SimulationConfig.SIM_OP_FIND_ENTRY:
			case SimulationConfig.SIM_OP_SELECT_ENTRIES:
			case SimulationConfig.SIM_OP_REPLY_ENTRY:
			case SimulationConfig.SIM_OP_QUERY_REPLY: {
				safeOp = true;
			}
			}

			// operations that are expected to return a result
			boolean opsExpectingRes = false;
			switch (operation) {
			case SimulationConfig.SIM_OP_GET_NEXT_ENTRY:
			case SimulationConfig.SIM_OP_FIND_ENTRY:
			case SimulationConfig.SIM_OP_PERFORM:
			case SimulationConfig.SIM_OP_QUERY_REPLY: {
				opsExpectingRes = true;
			}
			}

			// operations which can be overrided with a Simulation Script
			// explicitly point the scriptable operations to be clear for the
			// reader
			// those that are not allowed are findEntry when the Mode is not
			// Lookup
			// getNextClient and selectEntries
			boolean scriptableOps = false;
			switch (operation) {
			case SimulationConfig.SIM_OP_GET_NEXT_ENTRY: {
				if (getType() == ServerConstants.TYPE_ITERATOR) {
					scriptableOps = true;
				}
				break;
			}
			case SimulationConfig.SIM_OP_FIND_ENTRY: {
				if (getType() == ServerConstants.TYPE_LOOKUP) {
					scriptableOps = true;
				}
				break;
			}
			case SimulationConfig.SIM_OP_REPLY_ENTRY:
			case SimulationConfig.SIM_OP_PERFORM:
			case SimulationConfig.SIM_OP_PUT_ENTRY:
			case SimulationConfig.SIM_OP_MOD_ENTRY:
			case SimulationConfig.SIM_OP_DELETE_ENTRY:
			case SimulationConfig.SIM_OP_QUERY_REPLY: {
				scriptableOps = true;
			}
			}

			String state = getSimulatingState();

			if (work == null) {
				work = (parent.getWork() != null) ? parent.getWork()
						: new com.ibm.di.entry.Entry();
			}

			if (SimulationConfig.SIM_PROXY_STATE.equalsIgnoreCase(state)) {
				return startProxyAL(operation, work, conn, current, search,
						opsExpectingRes);
			} else if (SimulationConfig.SIM_SCRIPTED_STATE
					.equalsIgnoreCase(state)
					&& scriptableOps) {
				return startScriptSimulation(operation, work, conn, current,
						search, opsExpectingRes);
			} else if ((SimulationConfig.SIM_SIMULATED_STATE
					.equalsIgnoreCase(state) || SimulationConfig.SIM_DISABLED_STATE
					.equalsIgnoreCase(state))
					&& !(safeOp)) {

				// do nothing just skip the operation
				return new com.ibm.di.entry.Entry();
			}
		}

		// execute the operation here
		switch (operation) {
		case SimulationConfig.SIM_OP_PUT_ENTRY:
			connector.putEntry(conn);
			break;
		case SimulationConfig.SIM_OP_MOD_ENTRY:
			connector.modEntry(conn, search, current);
			break;
		case SimulationConfig.SIM_OP_DELETE_ENTRY:
			connector.deleteEntry(conn, search);
			break;
		case SimulationConfig.SIM_OP_PERFORM:
			return ((FunctionComponent) this).function.perform(conn);
		case SimulationConfig.SIM_OP_GET_NEXT_ENTRY:
			return connector.getNextEntry();
		case SimulationConfig.SIM_OP_FIND_ENTRY:
			return connector.findEntry(search);
		case SimulationConfig.SIM_OP_SELECT_ENTRIES:
			connector.selectEntries();
			break;
		case SimulationConfig.SIM_OP_REPLY_ENTRY:
			connector.replyEntry(conn);
			break;
		case SimulationConfig.SIM_OP_QUERY_REPLY:
			return connector.queryReply(conn);
		}

		return null;

	}

	/**
	 * Check whether to allow override hook for safe operations or not
	 *
	 * @return true if the AL is not simulating or it is but the simulation
	 *         state is either Enabled or Simulated. false otherwise.
	 * @since 7.0
	 */
	private boolean willExecuteSafeORHook() throws Exception {

		// true only if the AL is not simulating or if it is simulating but the
		// component in Enabled or Simulated simulation state
		return !parent.isSimulating()
				|| getSimulatingState().equalsIgnoreCase(
						SimulationConfig.SIM_ENABLED_STATE)
				|| getSimulatingState().equalsIgnoreCase(
						SimulationConfig.SIM_SIMULATED_STATE);
	}

	/**
	 * Check either to allow override hook for unsafe operations or not
	 *
	 * @return true if the AL is not simulating or it is simulating but the
	 *         simulation state is Enabled. false otherwise.
	 * @since 7.0
	 */
	private boolean willExecuteUnSafeORHook() throws Exception {

		// true only if the AL is not simulating or if it is simulating but the
		// component in Enabled simulation state
		return !parent.isSimulating()
				|| getSimulatingState().equalsIgnoreCase(
						SimulationConfig.SIM_ENABLED_STATE);
	}

	/**
	 * Start the Proxy AL and returns the result entry after it have finished.
	 * This method will initialize the Proxy AL if it already hasn't been
	 * initialized.
	 *
	 * @param operation
	 *            the ID of the method called to execute the specific operation.
	 *            The name of the method will be passed to the Proxy AL as a
	 *            value to the $method attribute of the op-entry.
	 * @param work
	 *            this is the entry which will be cloned and passed to the Proxy
	 *            AL as IWE when the operation id is
	 *            {@link SimulationConfig#SIM_OP_FIND_ENTRY} and the connector
	 *            type is one of the following:<br>
	 *            {@link ServerConstants#TYPE_UPDATE}<br>
	 *            {@link ServerConstants#TYPE_DELETE}<br>
	 *            {@link ServerConstants#TYPE_LOOKUP}<br>
	 *            {@link ServerConstants#TYPE_DELTA}<br>
	 *            If the above condition is not fulfilled then another check for
	 *            the operation is made. If the operation id is one of the
	 *            following:<br> {@link SimulationConfig#SIM_OP_GET_NEXT_ENTRY}<br>
	 *            {@link SimulationConfig#SIM_OP_SELECT_ENTRIES}<br>
	 *            the work parameter is passed as IWE to the Proxy AL.
	 *
	 * @param conn
	 *            the result of the OtuputMap entry passed to the Proxy AL as
	 *            IWE when the work entry conditions are not applicable.
	 * @param current
	 *            the entry found in the data source. This entry is passed as
	 *            the value of the attribute "current" of the op-entry object.
	 * @param search
	 *            the {@link SearchCriteria} object used when simulating
	 *            operation that requires it.
	 * @param opsExpectingRes
	 *            shows whether the current operation requires a result.
	 * @return the result entry object or null.
	 * @throws Exception
	 *             if an error occurs while initializing/executing the Proxy AL
	 * @since 7.0
	 */
	private Object startProxyAL(int operation, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn, com.ibm.di.entry.Entry current,
			SearchCriteria search, boolean opsExpectingRes) throws Exception {

		// use the AssemblyLineFC to start a remote proxy AL
		AssemblyLineFC proxyAL = parent.getProxyAL();

		proxyAL.getConfiguration().setParameter(AssemblyLineFC.OPERATION, name);
		proxyAL.getTCB().setOperationInitParam("$method",
				SimulationConfig.SIM_OP_AS_STRING[operation]);

		// make the appropriate invocation of the ProxyAL to handle
		// the lookup sub-procedure
		if (SimulationConfig.SIM_OP_FIND_ENTRY == operation
				&& (getType() == ServerConstants.TYPE_UPDATE
						|| getType() == ServerConstants.TYPE_DELETE
						|| getType() == ServerConstants.TYPE_LOOKUP || getType() == ServerConstants.TYPE_DELTA)) {

			proxyAL.getTCB().setOperationInitParam("search", search);
			com.ibm.di.entry.Entry result = (com.ibm.di.entry.Entry) proxyAL
					.perform(work.clone());

			Attribute c = result.getAttribute("conn");
			if (c == null)
				return result;

			if (connector instanceof Connector) {
				((Connector) connector).clearFindEntries();
			}

			for (int i = 0; i < c.size(); i++) {
				Object value = c.getValue(i);
				if (value instanceof com.ibm.di.entry.Entry) {
					if (connector instanceof Connector) {
						((Connector) connector)
								.addFindEntry((com.ibm.di.entry.Entry) value);
					} else {
						break;
					}
				} else {
					log.exception("Simulate.Not.An.Entry.Instance", value);
				}
			}
			return connector.getFirstFindEntry();

		}

		if (SimulationConfig.SIM_OP_MOD_ENTRY == operation) {
			proxyAL.getTCB().setOperationInitParam("current", current);
		}

		Object res = null;

		// we are doing this so the user can access to the external work
		// entry from the proxyAL in order to keep some state of the
		// proxyAL for example. In this operation the conn is null so it
		// is ok to do this. In order to stop iterating the proxyAL must
		// call the task.setWork(null); script
		if (SimulationConfig.SIM_OP_GET_NEXT_ENTRY == operation
				|| SimulationConfig.SIM_OP_SELECT_ENTRIES == operation) {
			res = proxyAL.perform(work.clone());

			// from the user point of view the work entry of the ProxyAL
			// will be the same as the work entry of the calling AL
			if (SimulationConfig.SIM_OP_SELECT_ENTRIES == operation
					&& res instanceof com.ibm.di.entry.Entry)
				work.merge((com.ibm.di.entry.Entry) res);

		} else {
			// execute the al with the initial work entry that is result
			// from the output map
			res = proxyAL.perform(conn);
		}

		// the entry retrieved from the proxy AL is representing the
		// found entry from the findEntry operation or the entry
		// that would be retrieved by the getNextEntry operation. It is
		// also a result of calling the following operations queryReply
		// and perform

		if (opsExpectingRes) {
			return res;
		} else {
			return null;
		}
	}

	/**
	 *
	 * Executes the user defined scripts.
	 *
	 * @param operation
	 *            used to send the name of the method that is simulated.
	 * @param work
	 *            the work entry
	 * @param conn
	 *            the entry which is a result of the OutpuMap
	 * @param current
	 *            the entry which is present on the target system
	 * @param search
	 *            the {@link SearchCriteria} object.
	 * @param opsExpectingRes
	 *            if the operation must return a result another script bean
	 *            called "result" is declared.
	 * @return the entry which is the result of the user-defined script or null
	 *         if such entry was not declared.
	 * @throws Exception
	 *             if the script interpretation fails.
	 * @since 7.0
	 */
	private Object startScriptSimulation(int operation,
			com.ibm.di.entry.Entry work, com.ibm.di.entry.Entry conn,
			com.ibm.di.entry.Entry current, SearchCriteria search,
			boolean opsExpectingRes) throws Exception {

		// simulation hooks are named the same way as the Components
		try {
			handler.pushStackFrame(this);
			com.ibm.di.entry.Entry result = null;

			if (opsExpectingRes) {
				result = new com.ibm.di.entry.Entry();
			}

			handler.declareBean("method",
					SimulationConfig.SIM_OP_AS_STRING[operation]);
			handler.declareBean("current", current);
			handler.declareBean("resEntry", result);
			handler.declareBean("search", search);
			trigger(SimulationConfig.SIMULATE_HOOK_NAME, work, conn);

			// result is the entry that the user provide as a result of
			// the getNextEntry, findEntry, queryReply and perform
			// operations

			return result;
		} finally {
			handler.popStackFrame();
		}
	}

	/**
	 * Returns true if component is initialized
	 *
	 * @return true if component is initialized
	 * @since 7.0
	 */
	public boolean componentInitialized() {
		return is_initialized;
	}

	/**
	 * Returns true if this component was successfully executed the last time it
	 * was called.
	 *
	 * @return true if this component was successfully executed the last time it
	 *         was called, false otherwise.
	 */
	public boolean wasSuccessful() {
		Object o = persistedProperties.get(SUCCESSFUL);
		if (o instanceof Boolean)
			return (Boolean) o;
		if (o instanceof String)
			return Boolean.valueOf((String) o);
		return false;
	}

	/**
	 * Sets the value that will be returned from wasSuccessful().
	 *
	 * @param b
	 *            - The new value
	 */
	public void setSuccessful(boolean b) {
		persistedProperties.put(SUCCESSFUL, Boolean.valueOf(b));
	}

	/**
	 * @return parentIndex
	 * @since 7.0
	 */
	int getParentIndex() {
		return parentIndex;
	}

	/**
	 * @param parentIndex
	 * @since 7.0
	 */
	void setParentIndex(int parentIndex) {
		this.parentIndex = parentIndex;
	}

	/**
	 * @return endComponentIndex
	 * @since 7.0
	 */
	int getEndComponentIndex() {
		return endComponentIndex;
	}

	/**
	 * @param endComponentIndex
	 */
	void setEndComponentIndex(int endComponentIndex) {
		this.endComponentIndex = endComponentIndex;
	}

	/**
	 * This method resets the Properties of this AssemblyLineComponent.
	 * SUCCESSFUL is set to "true", and HOOKS_INVOKED is set to an empty list.
	 */
	public void resetStatus() {
		setSuccessful(true);
		persistedProperties.put(HOOKS_INVOKED, new ArrayList<String>());
	}

	/**
	 * Add the named hook to the list of hooks invoked this cycle.
	 *
	 * @param hook
	 *            the name of the hook
	 */
	private void addHookInvoked(String hook) {
		Object o = persistedProperties.get(HOOKS_INVOKED);
		if (o instanceof List) {
			((List) o).add(hook);
			return;
		}
		List<String> list = new ArrayList<String>();
		if (o instanceof String)
			list.add((String) o);
		list.add(hook);
		persistedProperties.put(HOOKS_INVOKED, list);
	}

	//
	// These methods implement the Map Interface
	//
	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return persistedProperties.size() + statsProperties.size();
	}

	/**
	 * Returns false.
	 */
	public boolean isEmpty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsKey(Object key) {
		if (key == null)
			return false;
		String name = key.toString();
		return persistedProperties.containsKey(name)
				|| statsProperties.contains(name);
	}

	/**
	 * Returns true if a key maps to this value. Does not consider the
	 * statistics values.
	 *
	 * @param val
	 *            The value to look for.
	 */
	public boolean containsValue(Object val) {
		return persistedProperties.containsValue(val);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object get(Object propName) {
		if (propName == null)
			return null;
		String name = propName.toString();
		if (statsProperties.contains(name)) {
			if (name.equals(NUM_ERRORS))
				return Integer.valueOf(stats.numErrors());
			if (name.equals(NUM_ADD))
				return Integer.valueOf(stats.numAdd());
			if (name.equals(NUM_MODIFY))
				return Integer.valueOf(stats.numModify());
			if (name.equals(NUM_DELETE))
				return Integer.valueOf(stats.numDelete());
			if (name.equals(NUM_GET))
				return Integer.valueOf(stats.numGet());
			if (name.equals(NUM_GET_TRIES))
				return Integer.valueOf(stats.numGetTries());
			if (name.equals(NUM_GETCLIENT))
				return Integer.valueOf(stats.numGetClient());
			if (name.equals(NUM_GETCLIENT_TRIES))
				return Integer.valueOf(stats.numGetClientTries());
			if (name.equals(NUM_CALLREPLY))
				return Integer.valueOf(stats.numCallReply());
			if (name.equals(NUM_LOOKUP))
				return Integer.valueOf(stats.numLookup());
			if (name.equals(NUM_NOCHANGE))
				return Integer.valueOf(stats.numNoChange());
			if (name.equals(NUM_SKIPPED))
				return Integer.valueOf(stats.numSkipped());
			if (name.equals(NUM_IGNORED))
				return Integer.valueOf(stats.numIgnored());
			if (name.equals(LAST_ERROR))
				return stats.getError();
		}
		return persistedProperties.get(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object put(Object propName, Object propValue) {
		if (propName == null)
			throw new IllegalArgumentException(log
					.getString("ASSEMBLYLINECOMPONENT.MAPENTRY.NULL.KEY.VALUE"));
		String name = propName.toString();
		if (statsProperties.contains(name))
			throw new IllegalArgumentException(log.getString(
					"ASSEMBLYLINECOMPONENT.PUT.NOT.ALLOWED", name));

		return persistedProperties.put(name, propValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object remove(Object propName) {
		if (propName == null)
			return null;
		return persistedProperties.remove(propName.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void putAll(Map map) {
		Set<Map.Entry> entrySet = map.entrySet();
		for (Map.Entry entry : entrySet) {
			Object key = entry.getKey();
			if (key == null)
				continue;
			String name = key.toString();
			if (!statsProperties.contains(name))
				persistedProperties.put(name, entry.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		persistedProperties.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set keySet() {
		Set<Object> set = new HashSet<Object>(persistedProperties.keySet());
		set.addAll(statsProperties);
		return set;
	}

	/**
	 * Returns the values in this Map. Does not include the statistics values.
	 */
	public Collection values() {
		return persistedProperties.values();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public Set<Map.Entry> entrySet() {
		Set<Map.Entry> set = new HashSet<Map.Entry>(persistedProperties.entrySet());

		if (stats != null) {
			set.add(new MapEntry(NUM_ERRORS, Integer.valueOf(stats.numErrors())));
			set.add(new MapEntry(NUM_ADD, Integer.valueOf(stats.numAdd())));
			set.add(new MapEntry(NUM_MODIFY, Integer.valueOf(stats.numModify())));
			set.add(new MapEntry(NUM_DELETE, Integer.valueOf(stats.numDelete())));
			set.add(new MapEntry(NUM_GET, Integer.valueOf(stats.numGet())));
			set.add(new MapEntry(NUM_GET_TRIES, Integer.valueOf(stats.numGetTries())));
			set.add(new MapEntry(NUM_GETCLIENT, Integer.valueOf(stats.numGetClient())));
			set.add(new MapEntry(NUM_GETCLIENT_TRIES, Integer.valueOf(stats.numGetClientTries())));
			set.add(new MapEntry(NUM_CALLREPLY, Integer.valueOf(stats.numCallReply())));
			set.add(new MapEntry(NUM_LOOKUP, Integer.valueOf(stats.numLookup())));
			set.add(new MapEntry(NUM_NOCHANGE, Integer.valueOf(stats.numNoChange())));
			set.add(new MapEntry(NUM_SKIPPED, Integer.valueOf(stats.numSkipped())));
			set.add(new MapEntry(NUM_IGNORED, Integer.valueOf(stats.numIgnored())));
			set.add(new MapEntry(LAST_ERROR, stats.getError()));
		}
		return set;
	}

	/**
	 * This class an implementation of the Map.Entry class
	 */
	private class MapEntry implements Map.Entry {

		private String key = null;

		private Object val = null;

		MapEntry(Object key, Object value) {
			this.key = (String) key;
			this.val = value;
		}

		/**
		 * {@inheritDoc}
		 */
		public final Object getKey() {
			return key;
		}

		/**
		 * {@inheritDoc}
		 */
		public final Object getValue() {
			return val;
		}

		/**
		 * {@inheritDoc}
		 */
		public final boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;

			Map.Entry e = (Map.Entry) o;
			if (val == null)
				return key.equals(e.getKey()) && e.getValue() == null;
			return key.equals(e.getKey()) && val.equals(e.getValue());
		}

		/**
		 * {@inheritDoc}
		 */
		public final int hashCode() {
			int h = key.hashCode();
			if (val != null)
				h ^= val.hashCode();
			return h;
		}

		/**
		 * {@inheritDoc}
		 */
		public Object setValue(Object val) {
			throw new IllegalArgumentException(log.getString(
					"ASSEMBLYLINECOMPONENT.PUT.NOT.ALLOWED", key));
		}

		/**
		 * @return the String representation of this entry.
		 */
		public final String toString() {
			return key + "=" + val;
		}
	}

	// End of MapEntry class

	/**
	 * @param cc
	 *            Connector or Function Component configuration.
	 * @return The debug mode of the component.
	 */
	static boolean getComponentDebugMode(ConnectorConfig cc) {
		if (cc instanceof FunctionConfig) {
			FunctionConfig fc = (FunctionConfig) cc;
			return fc.getFunctionConfig().getDebug(false);
		} else {
			return cc.getConnectionConfig().getDebug(false);
		}
	}

	/**
	 * Returns true when using the failover connector.
	 * @return true if this connector is failovered.
	 * @since 7.1.1
	 */
	public boolean isFailOvered() {
		return connector == failoverConnector;
	}

	/**
	 * Sets the named Attribute Map to be used for input and output mapping.
	 * If the name is null, mapping will use this Connector's map.
	 * @param attributeMapName Name used to locate the AttributeMap.
	 * @since 7.1.1
	 */
	public void useMap(String attributeMapName) throws Exception {
		useMap(attributeMapName, true);
		useMap(attributeMapName, false);
	}

	/**
	 * Sets the named Attribute Map to be used for input or output mapping.
	 * If the name is null, mapping will use this Connector's map.
	 * @param attributeMapName Name used to locate the AttributeMap.
	 * @param input If true, this is used for input mapping, false means output.
	 * @since 7.1.1
	 */
	public void useMap(String attributeMapName, boolean input) throws Exception {
		AttributeMapConfig map = null;

		if (attributeMapName != null)
			map = config.getMetamergeConfig().getAttributeMap(attributeMapName);

		if (input)
			useInputMap(map, false);
		else
			useOutputMap(map, false);
	}

	/**
	 * Sets the provided AttributeMapConfig to be used for input mapping.
	 * @param map If null, this Connector's Input map is used.
	 * @since 7.1.1
	 */
	public void useInputMap(AttributeMapConfig map) throws Exception {
		if (map == null)
			inputMapFiles.add(null);
		useInputMap(map, false);
	}
	
	private void useInputMap(AttributeMapConfig map, boolean extend) throws Exception {
		if (map == null)
			map = config.getAttributeMap(true);

		if (!extend && imap != null) {
			imap.unload();
			imap = null;
		}

		if (imap == null)
			imap = new AttributeMapping(getName(), parent, log, parent.getScriptEngine());

		if (! ConnectorConfig.INPUT_MAP_NAME.equals(map.getShortName()))
			map.setName(ConnectorConfig.INPUT_MAP_NAME);
		
		imap.loadMap(map);

		if (Boolean.valueOf((String) parent.getConfig("automapattributes"))) {
			imap.setAutomap(true);
		}
	}

	/**
	 * Sets the provided AttributeMapConfig to be used for output mapping.
	 * @param map If null, this Connector's Output map is used.
	 * @since 7.1.1
	 */
	public void useOutputMap(AttributeMapConfig map) throws Exception {
		if (map == null)
			outputMapFiles.add(null);
		useOutputMap(map, false);
	}

	private void useOutputMap(AttributeMapConfig map, boolean extend) throws Exception {
		if (map == null)
			map = config.getAttributeMap(false);

		AttributeMapConfig addAttributes = copyMap(map);
		addAttributes.setName(ConnectorConfig.OUTPUT_MAP_NAME);

		AttributeMapConfig modAttributes = copyMap(map);
		modAttributes.setName(ConnectorConfig.OUTPUT_MAP_NAME);

		boolean addAll = (getType() != ServerConstants.TYPE_UPDATE &&
						  getType() != ServerConstants.TYPE_DELTA);

		for (String attrName : map.getAttributeNames()) {
			AttributeMapItem ami = map.getAttributeMapItem(attrName);
			AttributeMapItem amiClone = AttributeMapItemImpl.clone(ami);

			if (addAll || ami.getAdd())
				addAttributes.setAttributeMapItem(amiClone);
			if (ami.getModify())
				modAttributes.setAttributeMapItem(amiClone);
		}

		if (!extend && addmap != null) {
			addmap.unload();
			addmap = null;
		}
		if (addmap == null)
			addmap = new AttributeMapping(getName(), parent, log, parent.getScriptEngine());
		addmap.loadMap(addAttributes);

		if (!extend && modmap != null) {
			modmap.unload();
			modmap = null;
		}
		if (modmap == null)
			modmap = new AttributeMapping(getName(), parent, log, parent.getScriptEngine());
		modmap.loadMap(modAttributes);

		if (Boolean.valueOf((String) parent.getConfig("automapattributes"))) {
			addmap.setAutomap(true);
			modmap.setAutomap(true);
		}
	}
	
	/**
	 * Load an attribute map from a file and use it for the default map.
	 * This works like <code>useAttributeMap(fileName, false)</code>
	 * @param fileName The name of the external file containing the attribute map.
	 * If null, the mapping will be reset to this Connector's default map.
	 * @throws Exception if the file cannot be read
	 * @since 7.1.1
	 */
	public void useAttributeMap(String fileName) throws Exception{
		useAttributeMap(fileName, false);
	}

	/**
	 * Load an attribute map from a file and use it for the default map.
	 * The default map is the output map in AddOnly, Update and Delta mode.
	 * For all other modes the input map will be used.
	 * If the named file is already being used for this mapping,
	 * nothing will be done.
	 * The format of the external file is described in FileNamespace.
	 * @see com.ibm.di.config.base.FileNamespace
	 * 
	 * @param fileName The name of the external file containing the attribute map.
	 * If null, the mapping will be reset to this Connector's default map.
	 * @param extend If true, the new map will extend the existing map.
	 * 	If false, the new map will replace the existing map.
	 * @throws Exception if the file cannot be read
	 * @since 7.1.1
	 */
	public void useAttributeMap(String fileName, boolean extend) throws Exception{
		boolean input = (type != ServerConstants.TYPE_ADDONLY) &&
						(type != ServerConstants.TYPE_UPDATE) &&
						(type != ServerConstants.TYPE_DELTA);
		useAttributeMap(fileName, input, extend);
	}

	private Set<String> inputMapFiles = new HashSet<String>();
	private Set<String> outputMapFiles = new HashSet<String>();

	/**
	 * Load an attribute map from a file and use it for the specified map.
	 * If the named file is already being used for this mapping,
	 * nothing will be done.
	 * The format of the external file is described in FileNamespace.
	 * @see com.ibm.di.config.base.FileNamespace
	 * 
	 * @param fileName The name of the external file containing the attribute map.
	 * If null, the mapping will be reset to the Connector's default map.
	 * @param input If true, change the input map. If false, change the output map.
	 * @param extend If true, the new map will extend the existing map.
	 * 	If false, the new map will replace the existing map.
	 * @throws Exception if the file cannot be read
	 * @since 7.1.1
	 */
	public void useAttributeMap(String fileName, boolean input, boolean extend) throws Exception{
		Set<String> usedFiles = input ? inputMapFiles : outputMapFiles;

		// Do nothing if we have already used this filename
		if (usedFiles.contains(fileName))
			return;		

		if (!extend)
			usedFiles.clear();
		
		usedFiles.add(fileName);
		
		AttributeMapConfig map;

		if (fileName == null) {
			map = config.getAttributeMap(input);
		} else {
			try {
				// -- Try to load a config object with that name first
				map = config.getMetamergeConfig().getAttributeMap(fileName);
			} catch(Exception notFound) {
				map = FileNamespace.createMap(fileName, config.getMetamergeConfig());
			}
			if (map == null)
				throw new NameNotFoundException(fileName);
		}
		
		if (input)
			useInputMap(map, extend);
		else
			useOutputMap(map, extend);
	}

}
