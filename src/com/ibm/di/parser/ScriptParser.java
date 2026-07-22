/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.Vector;

import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.script.ScriptExitCode;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AttributeMapping;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.di.util.DebugServer;
import com.ibm.icu.util.StringTokenizer;

/**
 * To operate, a Script Parser must implement a few functions. The functions do
 * not use parameters. Passing data between the hosting Connector and the script
 * is done by using predefined objects. One of these predefined objects is the
 * result object which is used to communicate status information. Upon entry
 * into either function, the status field is set to normal which causes the
 * hosting Parser to continue calls. Signaling end-of-input or errors is done by
 * setting the status and message fields in this object. The entry object is
 * populated on calls to writeEntry and is expected to be populated in the
 * readEntry function. When reading entries you have the inp BufferedReader
 * object available for reading character data from a stream. When writing
 * entries you have the out BufferedWriter object available for writing
 * character data to a stream.
 * 
 */
public class ScriptParser extends ParserImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "scriptparser";

	/**
	 * {@link ScriptEngine}
	 */
	private ScriptEngine scriptEngine;

	/**
	 * {@link ScriptExitCode}
	 */
	private ScriptExitCode exitcode;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	// The Entry with the name of the called method
	private Entry scriptObject = new Entry();

	private final static String FUNCTION = "Function";

	// All the methods we might call
	private final static String WRITE_ENTRY = "writeEntry";
	private final static String READ_ENTRY = "readEntry";
	private final static String QUERY_SCHEMA = "querySchema";
	private final static String CLOSE_PARSER= "closeParser";
	private final static String FLUSH = "flush";

	/**
	 * Initializes parser's components.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initParser() throws Exception {
		Trace.entrymin(this, "initParser");
		resetProperties();
		
		AssemblyLine al = null;
		if (getContext() instanceof AssemblyLine) {
			al = (AssemblyLine)getContext();
		} else if (Thread.currentThread() instanceof AssemblyLine) {
			al = (AssemblyLine)Thread.currentThread();			
		}
		DebugServer deb = al != null ? al.getDebugger() : null;
		
		scriptEngine = new ScriptEngine(getParam("ScriptEngine"), SystemFunctions.getServer(), deb != null);

		exitcode = scriptEngine.getExitCode();
		if (debugMode()) {
			debug(sResHash.getString("PARSER.SCRIPT.DECLAREFUNC.INFO"));
		}

		scriptEngine.declareUserFunctions();
		if (debugMode()) {
			debug(sResHash.getString("PARSER.SCRIPT.MYCONFIG.INFO"));
		}

		scriptEngine.declareStaticBean("config", myConfiguration);

		// Main object depends on context
		scriptEngine.declareStaticBean("main", SystemFunctions.getServer());
		scriptEngine.declareTaskBean();

		String name = getName();
		if (myConfiguration != null) {
			if (myConfiguration.getParent() != null)
				name = myConfiguration.getParent().getShortName();
			else
				name = myConfiguration.getShortName();
		}
		scriptObject.setAttribute("Component", name);
		if (al != null)
			scriptObject.setAttribute("AssemblyLine", al.getName());
		scriptEngine.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);
		
		if (debugMode()) {
			debug(sResHash.getString("PARSER.SCRIPT.STREAMS.INFO"));
		}
		if (getWriter() != null)
			scriptEngine.declareStaticBean("out", getWriter());

		if (getReader() != null)
			scriptEngine.declareStaticBean("inp", getReader());

		if (getContext() != null
				&& getContext() instanceof com.ibm.di.connector.ConnectorInterface)
			scriptEngine.declareStaticBean("connector", getContext());

		scriptEngine.declareStaticBean("parser", this);

		if (deb != null) {
			deb.addScriptEngine(scriptEngine, name + ".Parser");
		}

		String exec = getParam("includeFiles");
		if (exec != null)
			scriptEngine.includeScript(name, exec);

		includePrologs(name);

		exec = getParam("script");
		if (exec != null) {
			if (debugMode()) {
				debug(sResHash.getString("PARSER.SCRIPT.EXECUTE.INFO", exec));
			}
			scriptEngine.exec(exec, name + ".Parser");
		} else {
			throw new Exception(sResHash
					.getString("PARSER.SCRIPT.MISSING.SCRIPT"));
		}
		Trace.exitmin(this, "initParser");

	}

	/**
	 * Includes all global prologs in this parser's script engine.
	 * 
	 * @throws Exception
	 */
	private void includePrologs(String name) throws Exception {
		Trace.entrymax(this, "includePologs");
		String inc = (String) getParam("includePrologs");
		if (inc == null)
			return;

		com.ibm.di.server.RSInterface parent = SystemFunctions.getServer();

		StringTokenizer st = new StringTokenizer(inc, "\r\n");
		while (st.hasMoreTokens()) {
			scriptEngine.loadScript(parent, name, st.nextToken(), true);
		}

		String aic = (String) getParam("includeGlobalPrologs");
		if (aic != null && aic.equals("true")) {
			MetamergeConfig mc = myConfiguration.getMetamergeConfig();
			if (mc != null)
				scriptEngine.includeAllScripts(mc);
			else
				logmsg(sResHash.getString("PARSER.SCRIPT.NOLIBLOAD.WARNING"));
		}
		Trace.exitmax(this, "includePologs");
	}

	/**
	 * Retrieves name of the FC.
	 * @return component's name.
	 */
	public String getName() {
		return "com.ibm.di.parser.ScriptParser";
	}

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception Exception
	 *                if an error occurs.
	 * 
	 */
	public void writeEntry(Entry entry) throws Exception {
		Trace.entrymax(this, WRITE_ENTRY, entry);
		scriptEngine.declareBean("entry", entry);
		scriptObject.setAttribute(FUNCTION, WRITE_ENTRY);

		scriptEngine.call(WRITE_ENTRY, null);

		if (exitcode.getStatus() != ScriptExitCode.SEC_OK) {
			throw new Exception(sResHash.getString(
					"PARSER.SCRIPT.WRITEENTRY.ERROR", exitcode.getMessage()));
		}
			
		if (getWriter() != null)
			getWriter().flush();
		
		Trace.exitmax(this, WRITE_ENTRY);
	}

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception Exception
	 * 
	 */
	public Entry readEntry() throws Exception {
		Trace.entrymax(this, READ_ENTRY);
		Entry entry = new Entry();
		scriptEngine.declareBean("entry", entry);
		scriptObject.setAttribute(FUNCTION, READ_ENTRY);
		
		scriptEngine.call(READ_ENTRY, null);

		switch (exitcode.getStatus()) {
		case ScriptExitCode.SEC_OK:
			Trace.exitmax(this, WRITE_ENTRY, entry);
			return entry;
		case ScriptExitCode.SEC_EOF:
			entry = null;
			Trace.exitmax(this, WRITE_ENTRY, entry);
			return null;
		default:
			throw new Exception(sResHash.getString(
					"PARSER.SCRIPT.READENTRY.ERROR", exitcode.getMessage()));
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {
		Vector<String> list = new Vector<String>();
		scriptEngine.declareBean("source", source);
		scriptEngine.declareBean("list", list);
		scriptObject.setAttribute(FUNCTION, QUERY_SCHEMA);

		scriptEngine.call(QUERY_SCHEMA, null);
		if (exitcode.getStatus() == ScriptExitCode.SEC_ERROR) {
			throw new Exception(sResHash.getString(
					"PARSER.SCRIPT.QUERYSCHEMA.ERROR", exitcode.getMessage()));
		}

		if (exitcode.getStatus() == ScriptExitCode.SEC_OK) {
			return list;
		} else {
			return null;
		}
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

	/**
	 * Resets parser's parameters
	 */
	private void resetProperties() {
		scriptEngine = null;
		exitcode = null;
	}

	/**
	 * This method is called by the hosting component (e.g. connector) to close
	 * and release parser resources.
	 * 
	 * @throws Exception
	 *             If an I/O error occurs
	 * 
	 */
	public void closeParser() throws Exception {

		flush();

		scriptObject.setAttribute(FUNCTION, CLOSE_PARSER);
		scriptEngine.call(CLOSE_PARSER, null, true);

		super.closeParser();
	}

	/**
	 * This method is called by the hosting component (e.g. connector) to close
	 * and release parser resources.
	 * 
	 * @throws Exception
	 *             If an I/O error occurs
	 * 
	 */
	public void flush() throws Exception {

		scriptObject.setAttribute(FUNCTION, FLUSH);
		scriptEngine.call(FLUSH, null, true);

		super.flush();
	}

}
