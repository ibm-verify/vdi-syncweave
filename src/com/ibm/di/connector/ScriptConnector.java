/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Vector;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.script.ScriptExitCode;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AttributeMapping;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.util.DebugServer;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Script Connector enables you to write your own Connector in JavaScript.
 * 
 * A Script Connector must implement a few functions to operate. If you plan to
 * use it for iteration purposes only (for example, reading, not searching or
 * updating), you can operate with two functions only. If you plan to use it as
 * a fully qualified Connector, you must implement all functions. The functions
 * do not use parameters. Passing data between the hosting Connector and the
 * script is enabled by using predefined objects. One of these predefined
 * objects is the result object, which is used to communicate status
 * information. Upon entry in either function, the status field is set to
 * normal, which causes the hosting Connector to continue calls. Signaling
 * end-of-input or error is done by setting the status and message fields in
 * this object. Two other script objects are defined upon function entry, the
 * entry object and the search object.
 */
public class ScriptConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the properties file.
	 */
	private static final String PROPERTIES_FILE = "scriptconnector";

	/**
	 * A parameter which determines if the Script Engine should be
	 * re-initialized each time the Connector's initialize() method is called.
	 */
	private static final String KEEP_GLOBAL_STATE = "keepGlobalState";

	/**
	 * Script engine used by the Connector.
	 */
	private ScriptEngine engine;

	/**
	 * Name of the Connector.
	 */
	private static final String myName = "Generic Script Connector";

	/**
	 * Resource hash for accessing TMS messages
	 */
	private static final ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * The Entry with the name of the called method.
	 */
	private Entry scriptObject = new Entry();

	/**
	 * This key is used for storing the currently executed method in the script
	 * object (e.g. selectEntries(), getNextEntry(), etc.).
	 */
	private final static String FUNCTION = "Function";

	// All the methods we might call
	private final static String INITIALIZE = "initialize";
	private final static String SELECT_ENTRIES = "selectEntries";
	private final static String GET_NEXT_ENTRY = "getNextEntry";
	private final static String MOD_ENTRY = "modEntry";
	private final static String DELETE_ENTRY = "deleteEntry";
	private final static String FIND_ENTRY = "findEntry";
	private final static String PUT_ENTRY = "putEntry";
	private final static String QUERY_REPLY = "queryReply";
	private final static String TERMINATE = "terminate";
	private final static String QUERY_SCHEMA = "querySchema";

	/**
	 * Class constructor.
	 */
	public ScriptConnector() {
		setName(myName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object obj) throws Exception {
		Trace.entrymin(this, "initialize", obj);

		boolean keepGlobalState = Boolean.valueOf(getParam(KEEP_GLOBAL_STATE));
		if (!keepGlobalState || engine == null) {
			initializeScriptEngine();
		} else {
			// reuse the old script engine, thus keeping all global
			// variables from the script
			debug(sResHash.getString("CONNECTOR.SCRIPT.KEEP.GLOBAL.STATE.INFO"));
		}

		// call the initialize() method of the script, if existent
		scriptObject.setAttribute(FUNCTION, INITIALIZE);
		engine.call(INITIALIZE, new Object[] { obj }, true);
		if (engine.getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.SELECTENTRIES.ERROR", engine.getExitCode().getMessage()));
		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * Initializes the Script Engine used by this Connector.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void initializeScriptEngine() throws Exception {
		scriptObject.setAttribute("Component", getName());
		DebugServer deb = null;
		if (getContext() instanceof AssemblyLine) {
			AssemblyLine al = (AssemblyLine)getContext();
			scriptObject.setAttribute("AssemblyLine", al.getName());
			deb = al.getDebugger();
		}
		engine = new ScriptEngine(getParam("ScriptEngine"), getRSInterface(), deb != null);
		engine.declareUserFunctions();
		engine.declareStaticBean("connector", this);
		engine.declareStaticBean("config", getConfiguration());
		engine.declareStaticBean("main", getRSInterface());
		engine.declareTaskBean(getContext());
		engine.declareStaticBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);

		if (deb != null) {
			deb.addScriptEngine(engine, getName() + ".Connector");
		}

		String exec = getParam("includeFiles");
		if (exec != null && exec.length() > 0) {
			engine.includeScript(getName(), exec);
		}

		String inc = (String) getParam("includePrologs");
		if (inc != null) {
			StringTokenizer st = new StringTokenizer(inc, "\r\n");
			while (st.hasMoreTokens()) {
				engine.loadScript(getRSInterface(), getName(), st.nextToken(), true);
			}
		}

		String aic = (String) getParam("includeGlobalPrologs");
		if (aic != null && aic.equals("true")) {
			if (getConfiguration() instanceof BaseConfiguration) {
				engine.includeAllScripts(((BaseConfiguration) getConfiguration()).getMetamergeConfig());
			} else {
				logmsg(sResHash.getString("CONNECTOR.SCRIPT.NOLOADLIBS.WARNING"));
			}
		}

		exec = getParam("script");

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SCRIPT.RUNSCRIPT.INFO", exec));
		}
		engine.exec(exec, getName() + ".Connector");
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
		Trace.entrymax(this, SELECT_ENTRIES);
		scriptObject.setAttribute(FUNCTION, SELECT_ENTRIES);
		engine.call(SELECT_ENTRIES, null);
		if (engine.getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.SELECTENTRIES.ERROR", engine.getExitCode().getMessage()));
		}
		Trace.exitmax(this, SELECT_ENTRIES);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymax(this, GET_NEXT_ENTRY);
		Entry entry = new Entry();

		engine.declareBean("entry", entry);
		scriptObject.setAttribute(FUNCTION, GET_NEXT_ENTRY);
		engine.call(GET_NEXT_ENTRY, null);
		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_ERROR) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.GETNEXTENTRY.ERROR", engine.getExitCode().getMessage()));
		}

		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_EOF) {
			Trace.exitmax(this, GET_NEXT_ENTRY);
			return null;
		}

		Trace.exitmax(this, GET_NEXT_ENTRY);
		return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		engine.declareBean("entry", entry);
		engine.declareBean("search", search);
		scriptObject.setAttribute(FUNCTION, MOD_ENTRY);

		engine.call(MOD_ENTRY, null);
		if (engine.getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.MODENTRY.ERROR", engine.getExitCode().getMessage()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		engine.declareBean("old", old);
		engine.declareBean("current", old);
		modEntry(entry, search);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		engine.declareBean("entry", entry);
		engine.declareBean("search", search);
		scriptObject.setAttribute(FUNCTION, DELETE_ENTRY);

		engine.call(DELETE_ENTRY, null);
		if (engine.getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.DELETEENTRY.ERROR", engine.getExitCode().getMessage()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		Trace.entrymin(this, FIND_ENTRY, search);
		Entry entry = new Entry();

		engine.declareBean("entry", entry);
		engine.declareBean("search", search);
		scriptObject.setAttribute(FUNCTION, FIND_ENTRY);

		engine.call(FIND_ENTRY, null);
		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_ERROR) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.FINDENTRY.ERROR", engine.getExitCode().getMessage()));
		}
		/**
		 * {@inheritDoc}
		 */
		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_EOF) {
			Trace.exitmin(this, FIND_ENTRY);
			return null;
		} else {
			Trace.exitmin(this, FIND_ENTRY);
			return entry;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, PUT_ENTRY, entry);
		engine.declareBean("entry", entry);
		scriptObject.setAttribute(FUNCTION, PUT_ENTRY);

		engine.call(PUT_ENTRY, null);
		if (engine.getExitCode().getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.PUTENTRY.ERROR", engine.getExitCode().getMessage()));
		}
		Trace.exitmin(this, PUT_ENTRY);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry queryReply(Entry entry) throws Exception {
		engine.declareBean("entry", entry);
		scriptObject.setAttribute(FUNCTION, QUERY_REPLY);

		engine.call(QUERY_REPLY, null);

		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_ERROR) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.QUERYREPLY.ERROR", engine.getExitCode().getMessage()));
		}

		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_EOF)
			return null;
		else
			return entry;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		scriptObject.setAttribute(FUNCTION, TERMINATE);

		engine.call(TERMINATE, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {
		Vector<Entry> list = new Vector<Entry>();
		engine.declareBean("source", source);
		engine.declareBean("list", list);
		scriptObject.setAttribute(FUNCTION, QUERY_SCHEMA);

		// -- queryschema is optional for backwards compat
		engine.call(QUERY_SCHEMA, null, true);
		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_ERROR) {
			throw new Exception(sResHash.getString("CONNECTOR.SCRIPT.QUERYSCHEMA.ERROR", engine.getExitCode().getMessage()));
		}

		if (engine.getExitCode().getStatus() == ScriptExitCode.SEC_EOF)
			return null;
		else
			return list;
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

}
