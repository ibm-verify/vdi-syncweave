/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AttributeMapping;
//import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.DebugServer;

/**
 * This is a function component that relays FC operations to a user defined
 * script.
 */
public class ScriptedFC extends Function implements ActionListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "scriptedfc";

	/**
	 * {@link ScriptEngine}
	 */
	private ScriptEngine se;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	//private final static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	// The Entry with the name of the called method
	private Entry scriptObject = new Entry();

	private final static String FUNCTION = "Function";

	/**
	 * Called once to initialize the function
	 * 
	 * @param obj
	 *            an initialization object passed directly to the script
	 *            function "initialize"
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void initialize(Object obj) throws Exception {

		AssemblyLine al = null;
		if (getContext() instanceof AssemblyLine) {
			al = (AssemblyLine)getContext();
		} else if (Thread.currentThread() instanceof AssemblyLine) {
			al = (AssemblyLine)Thread.currentThread();			
		}
		DebugServer deb = al != null ? al.getDebugger() : null;

		se = new ScriptEngine(null, getRSInterface(), deb != null);
		se.declareUserFunctions();
		se.declareStaticBean("main", getRSInterface());
		se.declareStaticBean("config", getConfiguration());
		if (al != null)
			se.declareStaticBean("task", al);
		else
			se.declareTaskBean();

		String name = PROPERTIES_FILE;
		if (getConfiguration() != null) {
			if (getConfiguration().getParent() != null)
				name = getConfiguration().getParent().getShortName();
			else
				name = getConfiguration().getShortName();
		}
		scriptObject.setAttribute("Component", name);
		if (al != null)
			scriptObject.setAttribute("AssemblyLine", al.getName());
		se.declareStaticBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);

		if (deb != null) {
			deb.addScriptEngine(se, name + ".Function");
		}
		se.exec(getConfiguration().getStringParameter("script") , name + ".Function");
		callFunction("initialize", new Object[] { this, obj });
		super.initialize(null);
	}

	/**
	 * This method is/should be called once before the object is released.
	 * 
	 * @throws Exception
	 */
	public void terminate() throws Exception {
		try {
			callFunction("terminate", new Object[] { this });
		} catch (Exception error) {
			error.printStackTrace();
		}
		super.terminate();
	}

	/**
	 * Calls a script function with given parameters.
	 * 
	 * @param func
	 *            Name of the function
	 * @param params
	 *            Array of positional parameters
	 * @return The result from the function call
	 * 
	 * @throws Exception
	 *             if any error occurs.
	 */
	public Object callFunction(String func, Object[] params) throws Exception {
		scriptObject.setAttribute(FUNCTION, func);
		return se.call(func, params, true);
	}

	/**
	 * This method enables the script to use action listeners. The script must
	 * use addActionListener(fc), which in turn will forward the action event to
	 * the script function "actionPerformed(fc, event)".
	 * 
	 * @param event
	 *            {@link ActionEvent}
	 * 
	 */
	public void actionPerformed(ActionEvent event) {
		try {
			callFunction("actionPerformed", new Object[] { this, event });
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * If this method is called with an object of type java.lang.String,
	 * java.io.File, java.io.InputStream or java.io.Reader the configured parser
	 * is provided that object as input and the returned value is an Entry
	 * object resulting from the parsing. If this method is called with an Entry
	 * object, the parser is used to generate a byte stream that is returned
	 * either as a byte array or java.lang.String object. The latter depends on
	 * the configuration switch "returnString" setting.
	 * 
	 * @param obj
	 *            the input object for the function
	 * @return the output object for the function
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Object perform(Object obj) throws Exception {
		verifyInitialized();
		Object result = se.call("perform", new Object[] { this, obj });
		return result;
	}

	/**
	 * Return version information
	 * 
	 * @return The version value
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

}
