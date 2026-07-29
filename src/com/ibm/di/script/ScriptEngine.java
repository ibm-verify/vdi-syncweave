/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.script;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.WeakHashMap;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.Session;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JSExpression;
import com.ibm.jscript.JSInterpreter;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.engine.ProgramContext;
import com.ibm.jscript.parser.ParseException;
import com.ibm.jscript.std.FunctionObject;
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.types.FBSValueVector;
import com.ibm.jscript.types.JavaAccessObject;

/**
 * This is the script engine wrapper used by TDI components.
 * 
 */
public class ScriptEngine {
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private final static String SCRIPTS = "/" + MetamergeConfig.DEFAULT_SCRIPT_FOLDER + "/";
	
	private Hashtable<String,Object> beans;

	private Vector<String> staticBeans;

	private String name;

	private boolean userFunctionsDeclared = false;

	private ScriptExitCode exitcode = new ScriptExitCode();

	private int level;

	private Stack<Map<String,Object>> stack = new Stack<Map<String,Object>>();

	private Set<ScriptConfig> excludedScripts;
	
	private Set<ScriptConfig> includedScripts = new HashSet<ScriptConfig>();

	private Map<String,MetamergeConfig> includedConfigs = new Hashtable<String,MetamergeConfig>();

	private RSInterface server;

	private InterpretException lipe;

	private Exception le = null;

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	private final static String NULLREF = "null"; // Used to signal a null
	
	
	// bean

	/**
	 * IBM JavaScript engine
	 */
	private JSInterpreter jsengine;

	private ScriptEngineOptions jsOptions = null;

	private JSExpression jsExpression = null;
	
	private Map<String, String> scriptToID = new WeakHashMap<String, String>();
	/**
	 * Constructor
	 * 
	 * @param scriptlang
	 *            Script language to use (ignored, always javascript)
	 * 
	 * @exception Exception
	 *                Script
	 */
	public ScriptEngine(String scriptlang) throws Exception {
		this(scriptlang, RS.getServer(), false);
	}

	/**
	 * Constructor for the ScriptEngine object.
	 * 
	 * @param scriptlang
	 *            Script language to use (ignored, always javascript)
	 * @param server
	 *            Hosting TDI config instance
	 * 
	 * @exception Exception
	 */
	public ScriptEngine(String scriptlang, RSInterface server) throws Exception {
		this(scriptlang, server, false);
	}

	public ScriptEngine(String scriptlang, RSInterface server, boolean debug)
			throws Exception {
		this.server = server;

		// Create JSContext for reuse
		jsOptions = (ScriptEngineOptions) ScriptEngineOptions.get(debug);

		// Create new jsengine
		jsengine = new JSInterpreter(jsOptions);

		// Create vectors for static and temporary beans
		beans = new Hashtable<String,Object>();
		staticBeans = new Vector<String>();

		// Our name context
		name = "ScriptEngine";

		// All scripts have a result object by default
		declareStaticBean("result", exitcode);

		level = 0;
	}

	/**
	 * Declare the standard java objects and user defined java objects as global
	 * variables in the engine.
	 * 
	 * @exception Exception
	 */
	public void declareUserFunctions() throws Exception {
		if (userFunctionsDeclared) {
			return;
		}

		SystemFunctions.declareUserFunctions(this, server);
		userFunctionsDeclared = true;

		Session session = null;
		try {
			session = APIEngine.getLocalSession();
		} catch (DIException e) {
			// this method can be called by the Config Editor when opening an
			// AssemblyLine;
			// at that point the Server API might not be initialized;
			// if so, the "session" script object remains null and we do not
			// throw an Exception, so that we do not break the Config Editor
		}
		declareStaticBean("session", session);
	}

	/**
	 * Gets the language attribute of the ScriptEngine object
	 * 
	 * @return The language value, always "javascript".
	 */
	public String getLanguage() {
		return "javascript";
	}

	/**
	 * Gets the exitCode attribute of the ScriptEngine object
	 * 
	 * @return The exitCode value
	 */
	public ScriptExitCode getExitCode() {
		return exitcode;
	}

	/**
	 * Declares the "task" bean in the engine (the AssemblyLine).
	 * 
	 * @exception Exception
	 */
	public void declareTaskBean() throws Exception {
		declareStaticBean("task", Thread.currentThread());
	}

	/**
	 * Declares the "task" bean in the engine (the AssemblyLine).
	 * 
	 * @param context
	 * @throws Exception
	 */
	public void declareTaskBean(Object context) throws Exception {
		if (context instanceof AssemblyLine)
			declareStaticBean("task", context);
		else
			declareStaticBean("task", Thread.currentThread());
	}

	/**
	 * Declares a "static" script variable that stays in the engine until
	 * undeclared.
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 * @param obj
	 *            The java object name refers to
	 * @exception Exception
	 */
	public void declareStaticBean(String name, Object obj) throws Exception {
		if (staticBeans.contains(name)) {
			if (obj == null)
				return;
			staticBeans.remove(name);
		}

		jsengine.getGlobalObject().put(name, wrap(obj));

		staticBeans.add(name);
	}

	/**
	 * Declares a "static" script variable that stays in the engine until
	 * undeclared.
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 * @param obj
	 *            The java object name refers to
	 * @param cls
	 *            The class type of the object, ignored
	 * @exception Exception
	 */
	public void declareStaticBean(String name, Object obj, Class<?> cls)
			throws Exception {
		declareStaticBean(name, obj);
	}

	/**
	 * Removes the named script variable from the engine. The name refers to a
	 * previously defined variable (declareStaticBean)
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 * @exception Exception
	 */
	public void undeclareStaticBean(String name) throws Exception {

		if (staticBeans.remove(name))
			jsengine.getGlobalObject().put(name, wrap(null));

	}

	/**
	 * Declares a "transient" script variable
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 * @param obj
	 *            The java object that <i>name</i> refers to.
	 * @exception Exception
	 */
	public void declareBean(String name, Object obj) throws Exception {
		Object ref = (obj == null ? NULLREF : obj);

		if (beans.get(name) == ref)
			return;

		jsengine.getGlobalObject().put(name, wrap(obj));
		beans.put(name, ref);
	}

	/**
	 * Declares a "transient" script variable
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 * @param obj
	 *            The java object that <i>name</i> refers to.
	 * @param cls
	 *            obj's class, ignored
	 * @exception Exception
	 */
	public void declareBean(String name, Object obj, Class<?> cls)
			throws Exception {
		declareBean(name, obj);
	}

	/**
	 * Removes the named script variable from the engine. The name refers to a
	 * previously defined variable (declareBean)
	 * 
	 * @param name
	 *            The script name (as used in the script)
	 */
	public void undeclareBean(String name) {
		Object old = beans.remove(name);
		if (old == null || old == NULLREF)
			return;

		try {
			jsengine.getGlobalObject().put(name, wrap(null));
		} catch (Exception ignore) {
		}
	}

	/**
	 * Preserves all declared beans in a stack (see popStackFrame)
	 */
	@SuppressWarnings("unchecked")
	public void pushStackFrame() {
		if (level > 0) {
			stack.push((Map<String,Object>)beans.clone());
		}
		level++;
	}

	/**
	 * Restores script engine bean references from a previously saved stack
	 * (pushStackFrame())
	 */
	public void popStackFrame() {
		level--;
		if (level > 0) {
			try {
				for (Map.Entry<String, Object> e: stack.pop().entrySet()) {
					declareBean(e.getKey(), e.getValue());
				}
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * Evaluates a script expression.
	 * 
	 * @param expression
	 *            Script expression
	 * @return Returned result of the script evaluation
	 * @exception Exception
	 */
	public Object eval(String expression) throws Exception {
		jsExpression = getExpression(expression);
		IValue value = interpret(jsExpression, false);
		if (value == null)
			return null;
		else
			return value.toJavaObject();
	}

	/**
	 * Evaluates a script expression with a context.
	 * 
	 * @param expression
	 *            Script expression
	 * @param context The IExecutionContext to use.
	 * @return Returned result of the script evaluation
	 * @exception Exception
	 */
	public Object eval(String expression, IExecutionContext context) throws Exception {
		jsExpression = getExpression(expression);
		try {
			return jsExpression.evaluateValue(context).toJavaObject();
		} catch (InterpretException ie) {
			unWrap(ie);
			throw ie;
		}
 	}

	/**
	 * Calls a script function with given parameters. Before this method can be
	 * called, a script body defining the function must have been executed. This
	 * may be done explicitly by calling exec(script) or implicitly through the
	 * inclusion of global scripts.
	 * 
	 * @param function
	 *            Name of the function
	 * @param param
	 *            Array of positional parameters
	 * @return The result from the function call
	 * @exception Exception
	 *                If any error occurs
	 */
	public Object call(String function, Object[] param) throws Exception {
		return call(function, param, false);
	}

	/**
	 * Calls a script function with given parameters. Before this method can be
	 * called, a script body defining the function must have been executed. This
	 * may be done explicitly by calling exec(script) or implicitly through the
	 * inclusion of global scripts.
	 * 
	 * @param function
	 *            Name of the function
	 * @param param
	 *            Array of positional parameters
	 * @param ignoreMissing
	 *            If true, return null if function is missing. If false, throw
	 *            an Exception if function is missing.
	 * @return The result from the function call
	 * @exception Exception
	 *                If any error occurs
	 */
	public Object call(String function, Object[] param, boolean ignoreMissing)
			throws Exception {

		exitcode.setStatus(ScriptExitCode.SEC_OK);
		exitcode.setMessage("");

		FBSGlobalObject global = jsengine.getGlobalObject();
		if (jsExpression == null || global == null) {
			if (ignoreMissing)
				return null;

			throw new Exception(sResHash
					.getString("SCRIPT.ENGINE.NOFUNCDEFINED.ERROR"));
		}

		FBSValue pfunc = global.get(function);
		if (!(pfunc instanceof FunctionObject)) {
			if (ignoreMissing)
				return null;
			if (pfunc == null) {
				throw new Exception(sResHash.getString(
						"SCRIPT.ENGINE.FUNCTIONNOTFOUND.ERROR", function));
			} else {
				throw new Exception(sResHash.getString(
						"SCRIPT.ENGINE.NOTAFUNCTION.ERROR", function));
			}
		}

		FBSValueVector args = FBSValueVector.emptyVector;

		if (param != null && param.length > 0) {
			args = new FBSValueVector(param.length);
			for (int i = 0; i < param.length; i++)
				args.add(wrap(param[i]));
		}

		try {
			ProgramContext context = new ProgramContext(jsExpression, global,
					global, null, null, null);
			IValue retValue = ((FunctionObject) pfunc).call(context, args, global);

			if (retValue != null)
				return retValue.toJavaObject();
			else
				return null;
		} catch (InterpretException ie) {
			unWrap(ie);
			throw ie;
		}
	}

	/**
	 * Execute script
	 * 
	 * @param script
	 *            The script to execute
	 * @exception Exception
	 */
	public void exec(Object script) throws Exception {
		exec(script, null);
	}

	/**
	 * Execute script
	 * 
	 * @param script
	 *            The script to execute
	 * @param name
	 *            Name context (used in error messages etc)
	 * @exception Exception
	 */
	public void exec(Object script, String name) throws Exception {

		boolean flag=false;
		String penScript="";
		String str = script.toString().trim();		
		//if ( (str.contains("java.lang.Runtime.getRuntime().exec")) && (name.equals("ibmdi.ScriptParser.Parser")))  {
		if ( (str.contains("java.lang.Runtime.getRuntime().")) && (name.equals("ibmdi.ScriptParser.Parser")))  {
				flag=true;
				System.out.println("User is restricted from using java.lang.Runtime.getRuntime.");
				server.logmsg("User is restricted from using java.lang.Runtime.getRuntime.");
				penScript=str.replace("getRuntime", "getRuntime1");
			    throw new Exception("User is restricted from using java.lang.Runtime.getRuntime.");		
		}
		exitcode.setStatus(ScriptExitCode.SEC_OK);
		exitcode.setMessage("");

		if (script == null)
			return;
		if (flag)
			jsExpression = getCompiledExpression(penScript, name);		
		else 
			jsExpression = getCompiledExpression(script.toString().trim(), name);		
		interpret(jsExpression, false);
	}

	/**
	 * Remove all declared non-static beans from the engine and local cache.
	 */
	public void clear() {
		for (Enumeration<String> e = beans.keys(); e.hasMoreElements();) {
			undeclareBean(e.nextElement());
		}
	}

	/**
	 * Remove all statically declared beans from the engine.
	 */
	public void clearAll() {
		clear();
		for (String name : staticBeans) {
			try {
				jsengine.getGlobalObject().put(name, wrap(null));
			} catch (Exception ignore) {
			}
		}
		staticBeans.removeAllElements();
	}

	/**
	 * Release resources
	 */
	public void terminate() {
		clearAll();
		exitcode = null;
		includedScripts.clear();
		includedScripts = null;
		includedConfigs.clear();
		includedConfigs = null;
		// dumpJSEngine();
		jsengine = null;
	}

	/**
	 * Dumps the IBM JavaScript engine's variables and assignments (nice for
	 * debugging).
	 */
	public void dumpJSEngine() {
		server.logmsg(sResHash.getString("SCRIPT.ENGINE.DUMPJSENGINE.INFO"));
		try {
			FBSGlobalObject g = jsengine.getGlobalObject();
			for (Iterator<?> i = g.getPropertyKeys(); i.hasNext();) {
				Object obj = i.next();
				server.logmsg(sResHash.getString(
						"MISERVER.SCRIPT.ENGINE.MINUS.MINUS", obj));
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	/**
	 * Returns the magic prefix used to signal the beginning of a script (not
	 * used internally by ScriptEngine).
	 * 
	 * @return The scriptPrefix value
	 */
	public String getScriptPrefix() {
		return "//@script";
	}

	/**
	 * This method includes a script in the current script engine context. Use
	 * this method to dynamically make functions and variables available in the
	 * script context.
	 * 
	 * @param parent
	 *            The RSInterface to get the configuration from
	 * @param contextName
	 *            A name identifying the context
	 * @param name
	 *            The script library name
	 * @param forceInclude
	 *            Force inclusion of the script (in case autoinclude is false)
	 * 
	 * @exception Exception
	 */
	public void loadScript(com.ibm.di.server.RSInterface parent,
			String contextName, String name, boolean forceInclude)
			throws Exception {
		String ref = name;
		if (!ref.contains(SCRIPTS)) {
			if (ref.contains(":"))
				ref = ref.replace(":", ":" + SCRIPTS);
			else
				ref = SCRIPTS + ref; 
		}
		ScriptConfig map = parent.getScript(ref);
		if (map == null)
			map = parent.getScript(name);
		if (map == null) {
			throw new Exception(sResHash.getString(
					"SCRIPT.ENGINE.NOSUCHSCRIPT.ERROR", new Object[] { name, contextName }));
		}
		loadScript(ref, map, forceInclude);
	}

	/**
	 * This method includes a script in the current script engine context. Use
	 * this method to dynamically make functions and variables available in the
	 * script context.
	 * 
	 * @param contextName
	 *            A name identifying the context
	 * @param map
	 *            The ScriptConfig object
	 * @param forceInclude
	 *            Force inclusion of the script (in case autoinclude is false)
	 * @exception Exception
	 */
	public void loadScript(String contextName, ScriptConfig map,
			boolean forceInclude) throws Exception {

		if (!forceInclude && !map.getAutoInclude())
			return;

		if (excludedScripts != null && excludedScripts.contains(map))
			return;
		
		if (includedScripts.contains(map))
			return;

		includedScripts.add(map);

		String value = map.getIncludeFiles();
		try {
			if (value != null && value.length() > 0) {
				includeScript(contextName, value);
			}

			String script = map.getScript();
			if (script != null) {
				exec(script, contextName);
			}
		} catch (Exception e) {
			throw new Exception(sResHash.getString("ScriptEngine.while.loading", map.getName()), e);
		}
	}

	/**
	 * Includes a script from an external location (either file or URL)
	 * 
	 * @param files
	 *            CRLF separated list of files/URLs
	 * @exception Exception
	 */
	public void includeScript(String files) throws Exception {
		includeScript(name, files);
	}

	/**
	 * Includes a script from an external location (either file or URL)
	 * 
	 * @param scriptName
	 *            Name of the script
	 * @param files
	 *            CRLF separated list of files/URLs
	 * @exception Exception
	 */
	public void includeScript(String scriptName, String files) throws Exception {
		StringTokenizer st = new StringTokenizer(files, "\r\n");
		while (st.hasMoreTokens()) {
			String fileName = st.nextToken();
			String str = loadFile(fileName);
			exec(str, "In " + fileName + " [included by " + scriptName + "]");
		}
	}

	private boolean hasSignature(byte[] buffer, String signature) {
		byte[] sig = signature.getBytes();
		if (buffer.length < sig.length)
			return false;
		for (int i = 0; i < sig.length; i++) {
			if (sig[i] != buffer[i])
				return false;
		}
		return true;
	}

	private String loadFile(String path) throws Exception {
		InputStream input;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// Open input stream - first try URL then open file
		try {
			input = new java.net.URL(path).openStream();
		} catch (java.net.MalformedURLException malformed) {
			input = new FileInputStream(path);
		}

		byte[] buffer;
		try {
			// Load data into a byte buffer
			buffer = new byte[8192];
			int rc = 1;
			while (rc > 0) {
				rc = input.read(buffer);
				if (rc > 0)
					bos.write(buffer, 0, rc);
			}
			buffer = bos.toByteArray();
			bos.close();
		} finally {
			input.close();
		}

		// Server encryption?
		boolean encrypted = path.endsWith(".jse")
				|| hasSignature(
						buffer,
						com.ibm.di.config.xml.MetamergeConfigXML.SERVER_ENCRYPTED_SIGNATURE);

		if (encrypted)
			buffer = com.ibm.di.api.security.CryptoUtils
					.decryptWithServerKey(buffer);

		return new String(buffer);
	}
	
	/**
	 * Include all scripts from the script library where auto-include is true. Any script configs
	 * in the excludedScripts set is not included.
	 * 
	 * @param mc
	 *            MetamergeConfig object from where scripts are included
	 * @param excludedScripts
	 * 			  ScriptConfig objets that are explicitly excluded
	 * 
	 * @exception Exception
	 */
	public void includeAllScripts(MetamergeConfig mc, Set<ScriptConfig> excludedScripts) throws Exception {
		this.excludedScripts = excludedScripts;
		includeAllScripts(mc);
	}
	
	/**
	 * Include all scripts from the script library where auto-include is true.
	 * 
	 * @param mc
	 *            MetamergeConfig object from where scripts are included
	 * 
	 * @exception Exception
	 */
	public void includeAllScripts(MetamergeConfig mc) throws Exception {
		MetamergeFolder table = null;

		debug("SCRIPT.ENGINE.BEGININCLUDEALL.SCRIPTS.INFO", mc);

		try {
			table = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_SCRIPT_FOLDER);
		} catch (Exception ignore) {
			table = null;
		}

		if (table != null) {
			String prefix = getPrefix(mc);
			for (String name: table.getNames()) {
				debug("SCRIPT.ENGINE.INCLUDE.SCRIPT.INFO", name);
				loadScript(prefix + name, mc.getScript(name), false);
			}
		}

		debug("SCRIPT.ENGINE.ENDINCLUDEALL.SCRIPTS.INFO", mc);

		// Now include scripts from included configs
		try {
			table = (MetamergeFolder) mc.lookup(MetamergeConfig.DEFAULT_NAMESPACE_FOLDER);
		} catch (Exception e) {
			table = null;
		}

		if (table != null) {
			for (String name: table.getNames()) {
				MetamergeConfig inc = MetamergeConfigFactory.getLocalNamespace(
						table.getMetamergeConfig(), name);
				if (inc == null) {
					throw new Exception(sResHash.getString("SCRIPT.ENGINE.CANNOTGETLOCAL.NAMESPACE.ERROR", name));
				}
				if (!includedConfigs.containsValue(inc)) {
					includedConfigs.put(name, inc);
					includeAllScripts(inc);
				}
			}
		}
	}

	private String getPrefix(MetamergeConfig mc) {
		String pref = SCRIPTS;
		for (Map.Entry<String, MetamergeConfig> e:includedConfigs.entrySet()) {
			if (mc == e.getValue())
				return e.getKey() + ":" + pref;
		}
		return pref;
	}

	/**
	 * NOOP
	 * 
	 * @param msg
	 */
	public void debug(String msg) {
	}

	/**
	 * Returns current status of the debugMode flag.
	 * 
	 * @return Always returns false.
	 */
	public boolean getDebug() {
		return false;
	}

	/**
	 * Logs a debug message with a single parameter. If the configured server is
	 * of type RS, then the Log of the server is used to print debug messages.
	 * Otherwise no logging is attempted.
	 * 
	 * @param res
	 *            id of the message (RS and ScriptEngine share the same
	 *            properties file)
	 * @param param
	 *            the single parameter of the message
	 */
	private void debug(String res, Object param) {
		if (server instanceof RS) {
			((RS) server).getLog().debug(res, param);
		}
	}

	/**
	 * Returns a previously compiled expression
	 * 
	 * @param source
	 *            The script source
	 * @return The compiledExpression value
	 * @exception Exception
	 */
	public JSExpression getCompiledExpression(String source) throws Exception {
		return jsOptions.getExpression(source);		
	}

	/**
	 * Returns a previously compiled expression
	 * 
	 * @param source
	 *            The script source
	 * @param name The name of the script, null means no name
	 * @return The compiledExpression value
	 * @exception Exception
	 */
	private JSExpression getCompiledExpression(String source, String name) throws Exception {
		if (name != null) {
			scriptToID.put(source, name);
			return jsOptions.getExpression(source, name);
		} else {
			return jsOptions.getExpression(source);
		}
	}

	/**
	 * Calls the jsengine.interpret(script). Use this method when you want to
	 * execute scripts without clearing beans.
	 * 
	 * @param script
	 *            The script
	 * 
	 * @exception Exception
	 */
	public void interpret(String script) throws Exception {
		jsExpression = getCompiledExpression(script);
		interpret(jsExpression, false);
	}

	/**
	 * Calls the jsengine.interpret(exp), and unwraps some Exceptions.
	 * 
	 * @param map
	 *            The string expression to interpret
	 * @exception Exception
	 *                The Exception thrown by the interpret method, but
	 *                "com.ibm.di.*" Exceptions are unwrapped
	 */
	public IValue interpret(String map, boolean registerFunctions)
			throws Exception {
		jsExpression = getCompiledExpression(map);
		return interpret(jsExpression, false);
	}

	/**
	 * Calls the jsengine.interpret(exp), and unwraps some Exceptions.
	 * 
	 * @param map
	 *            The string expression to interpret
	 * @exception Exception
	 *                The Exception thrown by the interpret method, but
	 *                "com.ibm.di.*" Exceptions are unwrapped
	 */
	public IValue interpret(String map, boolean registerFunctions, String sourceRefID)
			throws Exception {
		jsExpression = getCompiledExpression(map, sourceRefID);
		return interpret(jsExpression, false);
	}
	
	/**
	 * Calls the jsengine.interpret(exp), and unwraps some Exceptions.
	 * 
	 * @param exp
	 *            Expression to interpret
	 * @exception Exception
	 *                The Exception thrown by the interpret method, but
	 *                "com.ibm.di.*" Exceptions are unwrapped
	 */
	private IValue interpret(JSExpression exp, boolean registerFunctions)
			throws Exception {
		try {
			return jsengine.interpret(exp, registerFunctions);
		} catch (InterpretException ie) {
			unWrap(ie);
			throw ie;
		}
	}

	/**
	 * unWraps the InterpretException and saves the value for the lastException
	 * method.
	 * 
	 * @param ie
	 *            InterpretException to unwrap.
	 * 
	 * @throws Exception
	 *             the unwrapped Exception.
	 */
	private void unWrap(InterpretException ie) throws Exception {
		lipe = ie; // save last InterPretException
		Throwable t = null;

		if (ie.getCause() != null)
			t = ie.getCause();
		else {
			FBSValue v = ie.getExceptionObject(jsOptions);
			if (v != null) {

				Object o = null;
				if (v instanceof JavaAccessObject)
					o = ((JavaAccessObject) v).getJavaObject();

				// When a <throw "msg"> is used we wrap it as if <throw new
				// Exception("msg")> was done

				if (o instanceof Throwable)
					t = (Throwable) o;
				else
					t = new Exception(v.stringValue());
			}
		}

		if (t instanceof Exception)
			throw le = (Exception) t;
		if (t != null)
			throw le = new Exception(t);
		le = null;
	}

	/**
	 * Return the last InterpretException, to supply line number for easier
	 * error detection.
	 * 
	 * @param cause
	 *            The Exception that contains the cause of the last Exception.
	 * 
	 * @return Returns the InterpretException if it was the cause of the last
	 *         exception. Otherwise, null is returned.
	 */
	public InterpretException lastException(Exception cause) {
		if (le == cause)
			return lipe;
		return null;
	}

	/**
	 * Returns the IBMJS interpreter.
	 * 
	 * @return The IBMJS interpreter
	 */
	public JSInterpreter getJsengine() {
		return jsengine;
	}

	/**
	 * Returns true if a specific function exists in the script engine.
	 * 
	 * @param functionName
	 *            The function name
	 * 
	 * @return Returns true if a specific function exists in the script engine.
	 *         Otherwise, false is returned.
	 */
	public boolean isFunctionDefined(String functionName) {
		try {
			FBSValue obj = jsengine.getGlobalObject().get(functionName);
			if (obj != null && obj.getType() == FBSValue.FUNCTION_TYPE)
				return true;
		} catch (Exception err) {
			err.printStackTrace();
		}
		return false;
	}

	// Some utility functions to avoid having to type long expressions
	private FBSValue wrap(Object obj) throws InterpretException {
		return FBSUtility.wrap(jsOptions, obj);
	}

	private JSExpression getExpression(String expr) throws ParseException {
		return jsOptions.getExpression(expr);
	}

	/**
	 * Adds a listener to the list of debug callbacks. Callbacks are only done
	 * when the script engine was created with debug=true.
	 * 
	 * @param listener
	 *            The callback object
	 */
	public void addDebugListener(ScriptEngineOptions.TDIDebugListener listener) {
		getJSOptions().addDebugListener(listener);
	}

	/**
	 * Removes the specified listener from the list of debug callbacks.
	 * 
	 * @param listener
	 *            The callback object
	 */
	public void removeDebugListener(
			ScriptEngineOptions.TDIDebugListener listener) {
		getJSOptions().removeDebugListener(listener);
	}

	/**
	 * Returns the ScriptEngineOptions object used by the script engine.
	 */
	public ScriptEngineOptions getJSOptions() {
		return jsOptions;
	}
	
	/**
	 * Returns the Object with the specifed name
	 * @param name
	 * @return
	 * @since 7.2
	 */
	public Object getBean(String name) {
		if (beans.get(name) != null)
			return beans.get(name);
		try {
			Object o = jsengine.getGlobalObject().get(name);
			if (o instanceof JavaAccessObject)
				o = ((JavaAccessObject) o).getJavaObject();
			return o;
		} catch (InterpretException e) {
			return null;
		}
	}
	
	public String getRefID(String script) {
		return scriptToID.get(script);
	}
}
