/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// DebugServer.java
//
//
//
package com.ibm.di.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLineComponent;
import com.ibm.di.server.BranchingComponent;
import com.ibm.di.server.EndBranchComponent;
import com.ibm.di.server.EndLoopComponent;
import com.ibm.di.server.Log;
import com.ibm.di.server.LoopComponent;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ScriptComponent;
import com.ibm.di.util.Breakpoint;
import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.JSExpression;
import com.ibm.jscript.engine.FunctionContext;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.types.FBSDefaultObject;
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSNull;

public class DebugServer {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	public final static int DEBUG_PORT = 9987;

	// Commands
	public final static String CONTROL = "ctl";

	public final static String STOP = "stop";

	public final static String CONT = "continue";

	public final static String BREAK = "break";

	public final static String LOGMSG = "logmsg";

	public final static String HELLO = "hello";

	public final static String EVAL = "eval";

	public final static String STATS = "stats";

	public final static String QUIT = "quit";

	public final static String INIT = "init";

	public final static String FLAGS = "flags";

	public final static String BREAKPOINTS = "breakpoints";

	public final static String BREAKPOINT = "breakpoint";

	public final static String ENGINE_VARS = "enginevars";

	public final static String STATUS = "status";

	public final static String ABORTED = "abort";

	public final static String STEP_OVER = "stepover";

	public final static String STEP = "step";

	public final static String BREAKAT = "breakat";

	public final static String CONFIG = "config";

	public final static String UNIQUE_ID = "assemblyline.id";

	public final static String INIT_BREAK = " INIT ";

	public final static String SCRIPT = "script";

	public final static String CONTEXT = "context";
	
	public final static String RUN_TO_CYCLE = "runToCycle";

	// Local variables
	private Socket socket;

	private ObjectInputStream is;

	private ObjectOutputStream os;

	private String taskname;

	private Log log;

	private ScriptEngine currentEngine;

	private ScriptEngine defaultEngine;

	private Object task;

	private Hashtable<String, Object> scriptObjects = new Hashtable<String, Object>();

	private Hashtable<String, Breakpoint> breakpoints;

	private HashSet<String> components = new HashSet<String>();

	private boolean step;

	private boolean stepOver;

	private String breakAt;

	private boolean onerror;

	private String lastKnownLocation;

	private boolean stopReceived;

	private boolean abortSent;

	private String uniqueID;

	private long runToCycle = -1;
	
	/**
	 * This field provides String.hashCode to Source reference ID. The hash code of a script then provides the location
	 * where the script was defined. We use this to send context switches to the remote debugger client when script
	 * function calls go from for example a Hook to a function defined in the Script library.
	 */
	private HashMap<Integer, String> scriptRefID = new HashMap<Integer,String>();

	/**
	 * These two variables track the node and context that we are stepping over.
	 */
	private ASTNode stepOverNode;
	private IExecutionContext stepOverContext;

	private IExecutionContext currentContext;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Map to associate ScriptEngines with DebugListeners
	 */
	private Map<ScriptEngine, DebugListener> listenerMap = new HashMap<ScriptEngine, DebugListener>();

	public DebugServer(String taskname) {
		this.taskname = taskname;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setScriptEngine(ScriptEngine se) {
		defaultEngine = se;
		addDebugListener(se);
	}

	public void setTask(Object task) {
		this.task = task;
	}

	public void addScriptObject(String key, Object value) {
		scriptObjects.put(key, value);
	}

	public void clearScriptObjects() {
		scriptObjects.clear();
	}

	public void debugInit() throws Exception {
		os.writeObject(BREAKPOINTS);
		os.writeObject(getBreakpoints());
		os.writeObject(FLAGS);
		os.writeObject(getDebuggerFlags());
		sendFlattenedConfig();
		os.writeObject(INIT);
		os.writeObject("");
		os.writeObject(UNIQUE_ID);
		os.writeObject(getUniqueID());
		os.flush();
		debugBreak(INIT_BREAK);
	}

	private void sendFlattenedConfig() throws Exception {
		if (!(task instanceof AssemblyLine))
			return;

		os.writeObject(CONFIG);

		try {
			AssemblyLine assemblyline = (AssemblyLine) task;
			AssemblyLineConfig config = assemblyline.getConfigClone();
			if (config == null)
				return;

			config.flatten(new ArrayList<String>());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			config.getMetamergeConfig().commitChangesNoEncryption(bos);
			DebugMessage msg = new DebugMessage(CONFIG, bos.toByteArray());
			msg.setProp("name", config.getShortName());
			os.writeObject(msg);
		} catch (Exception e) {
			if (log != null)
				log.logerror("DebugServer: send config: " + e);
			e.printStackTrace();
			os.writeObject(new DebugMessage("ERROR", e));
		}
	}

	public void switchTo(String name) throws Exception {

		DebugMessage m = new DebugMessage("switch", name);
		os.writeObject(CONTROL);
		os.writeObject(m);
		os.flush();
	}

	public boolean debugConnect() {
		return debugConnect("localhost", -1);
	}

	public boolean debugConnect(int port) {
		return debugConnect("localhost", port);
	}

	public boolean debugConnect(String host, int tcpport) {
		return debugConnect(host, tcpport, false);
	}

	public boolean debugConnect(String host, int tcpport, boolean onerror) {
		try {
			String remote = host;
			int port = tcpport;
			this.onerror = onerror;

			if (remote == null || remote.equals("localhost"))
				remote = InetAddress.getLocalHost().getHostAddress();

			if (port == -1)
				port = DEBUG_PORT;

			logmsg(sResHash.getString("connect.to.remote.port", new Object[] {
					remote, "" + port }));
			socket = new Socket(remote, port);
			is = new ObjectInputStream(socket.getInputStream());
			is.readObject();

			os = new ObjectOutputStream(socket.getOutputStream());
			os.writeObject(HELLO);
			os.writeObject(taskname);
			os.flush();

			return true;
		} catch (Exception err) {
			err.printStackTrace();
			logmsg(sResHash.getString("server.debug.connect.exp.info", err));
			return false;
		}

	}

	public void debugClose() {
		try {
			if (socket != null)
				socket.close();
		} catch (Exception ignore) {
			SystemFunctions.doNothing();
		}
		stopReceived = false;
		socket = null;
		removeAllListeners();
	}

	public boolean isConnected() {
		return (socket != null);
	}

	public void debugMsg(Object msg) throws Exception {
		if (socket == null)
			return;

		os.writeObject(LOGMSG);
		safeWriteObject(msg);
		os.flush();
	}

	public void debugClose(Object msg) {
		if (socket == null)
			return;

		try {
			os.writeObject(QUIT);
			os.writeObject(msg);
			os.flush();
		} catch (Exception ignore) {
		}

		debugClose();
	}

	public void aborted(Throwable t) throws Exception {
		if (abortSent)
			return;

		abortSent = true;
		os.writeObject(ABORTED);
		safeWriteObject(t);
		os.flush();
		debugBreak(ABORTED);
	}

	public void debugBreak(Object obj) throws Exception {
		debugBreak(obj, isOnerror(), -1);
	}

	public void debugBreak(Object obj, boolean onerror) throws Exception {
		debugBreak(obj, onerror, -1);
	}

	public void debugBreak(Object obj, boolean onerror, int linenumber) throws Exception {
		currentEngine = defaultEngine;
		debugBreak(obj, onerror, linenumber, null);
	}

	public void debugBreak(Object obj, boolean onerror, int linenumber, Integer scriptRef)
	throws Exception {

		// just in case
		if (socket == null || breakpoints == null) {
			return;
		}

		// Don't break if user has sent stop commmand
		if (stopReceived) {
			if (!isAborted())
				throw new Exception(sResHash
						.getString("process.stopped.by.debugger"));
			else
				return;
		}

		/**
		 * BranchComponents and LoopComponents use a "#0" ending to indicate that
		 * we are at the script.
		 * Split into obj and linenumber.
		 */
		if (obj instanceof String && linenumber == -1) {
			String s = (String) obj;
			if (s.endsWith("#0")) {
				obj = s.substring(0, s.length() - 2);
				linenumber = 0;
			}
		}

		// Save last known loc
		if (breakpoints.get(obj) != null) {
			lastKnownLocation = "" + obj;
		}

		// Check if debug client has sent a STEP command
		if (!(obj instanceof Throwable)) {
			if (socket.getInputStream().available() > 0) {
				obj = is.readObject();
				if (QUIT.equals(obj)) {
					stopDebugging();
					return;
				}
				if (!STEP.equals(obj) && !CONT.equals(obj)) {
					return;
				}

				// code further down will send a switchTo
				step = true;
				obj = "";

				// when pausing a break-on-error al we turn off this flag
				this.onerror = false;

				runToCycle = -1;

			} else if (onerror) {
				return;
			}
		}

		if (runToCycle >= 0)
			return;

		Object val = obj;
		if (val == null)
			val = getLastKnownLocation();

		// Run Until ... just set step to true to force a break
		if (breakAt != null &&
				breakAt.equals(linenumber == -1 ? obj : obj + "#" + linenumber)) {
			val = breakAt;
			breakAt = null;
			step = true;
		} else if (stepOver && linenumber <= 0 && components.contains(obj)) {
			step = true;
		}

		// If break location is unknown or explicitly enabled we break (match
		// also check expression)
		Breakpoint b = breakpoints.get(obj);

		// -- Check script level breaks
		if (linenumber != -1) {
			Breakpoint b2 = breakpoints.get(obj + "#" + linenumber);
			if (b2 != null) {
				// Re-use expression
				if (b != null)
					b2.setExpression(b.getExpression());
				b = b2;
			}
		}

		if (b != null) {
			// If we are stepping then stop anyway
			if (!step) {
				removeDebugListener(currentEngine);
				boolean match = b.match(currentEngine, currentContext);
				addDebugListener(currentEngine);
				if (! match)
					return;
			}

			// Only send location updates if we are pausing
			val = obj.toString() + (linenumber != -1 ? "#" + linenumber : "");
			switchTo("" + val);
		} else if (lastKnownLocation != null) {
			switchTo(lastKnownLocation);
		}

		step = false;
		stepOver = false;
		stepOverContext = null;

		String cmd = BREAK;
		DebugMessage db;
		String reply;

		// -- Transfer source ref id to let CE know where we are
		if(scriptRef != null) {
			os.writeObject(SCRIPT);
			os.writeObject(scriptRef);
			os.flush();
		}

		while (true) {

			os.writeObject(cmd);

			if (val != null) {
				safeWriteObject(val);
			} else {
				os.writeObject(new NullValue());
			}
			os.flush();

			try {
				reply = (String) is.readObject();
			} catch (IOException ioe) {
				if (log != null)
					log.logerror("DebugServer: readObject: ", ioe);
				stopDebugging();
				return;
			}

			val = null;

			if (reply.equals(STOP)) {
				stopReceived = true;
				throw new Exception(sResHash
						.getString("process.stopped.by.debugger"));
			} else if (reply.equals(CONT)) {
				break;
			} else if (reply.equals(STEP)) {
				step = true;
				break;
			} else if (reply.equals(STEP_OVER)) {
				stepOver = true;
				stepOverContext = currentContext;
				break;
			} else if (reply.equals(BREAKAT)) {
				breakAt = (String) is.readObject();
				break;
			} else if (reply.equals(RUN_TO_CYCLE)) {
				runToCycle = (Long) is.readObject();
				break;
			} else if (reply.equals(FLAGS)) {
				cmd = reply;
				val = getDebuggerFlags();
			} else if (reply.equals(STATS)) {
				cmd = STATS;
				val = getConnectorList(true);

			} else if (reply.equals(BREAKPOINT)) {
				cmd = BREAKPOINT;
				val = is.readObject();
				if (val instanceof Breakpoint) {
					b = (Breakpoint) val;
					breakpoints.put(b.getLocation(), b);
					val = "OK";
				} else {
					val = "Expected Breakpoint got "
						+ (val == null ? "null object" : val.getClass().getName());
				}

			} else if (reply.equals(EVAL)) {
				cmd = reply;
				reply = (String) is.readObject();
				db = new DebugMessage(cmd, reply);
				db.setProp("value", doEval(reply));
				val = db;

			} else if (reply.equals(ENGINE_VARS)) {
				val = getScriptEngineVariables("");

			} else if (reply.equals(QUIT)) {
				stopDebugging();
				return;

			}
		}
	}

	private void stopDebugging() {
		if (task instanceof AssemblyLine) {
			((AssemblyLine) task).removeDebugger();
		}
	}

	public DebugMessage getDebuggerFlags() {
		TreeMap flags = new TreeMap();
		if ((task instanceof AssemblyLine)
				&& (((AssemblyLine) task).getConfig("debuggerFlags") instanceof TreeMap)) {
			flags = (TreeMap) ((AssemblyLine) task).getConfig("debuggerFlags");
		}
		return new DebugMessage(FLAGS, flags);
	}

	private Object doEval(String cmd) {
		Object val;
		if (cmd != null && cmd.length() > 0) {
			try {
				for (Map.Entry<String, Object> entry : scriptObjects.entrySet())
					currentEngine.declareBean(entry.getKey(), entry.getValue());

				if (cmd.equals("ScriptEngine")) {
					val = getScriptEngineVariables("");
				} else {
					try {
						// Make sure we dont get callbacks while
						// evaluating expressions from the CE or we'll
						// enter an infinite loop
						removeDebugListener(currentEngine);
						if (currentContext == null)
							val = currentEngine.eval(cmd);
						else
							val = currentEngine.eval(cmd, currentContext);
					} catch (Exception e) {
						val = e;
					} finally {
						addDebugListener(currentEngine);
					}
					val = convertObject(val);
				}

			} catch (Exception err) {
				val = err.toString();
			} finally {
				// Clean up
				for (String key : scriptObjects.keySet())
					currentEngine.undeclareBean(key);
			}
		} else {
			val = "*** empty input expression";
		}
		return val;
	}

	/**
	 * This method returns a table of Breakpoints. There is one Breakpoint
	 * object for each component and its hooks in this table.
	 *
	 * @return Returns a Hashtable with a Breakpoint object for each possible
	 *         breakpoint in the AL
	 */
	public Hashtable<String, Breakpoint> getBreakpoints() throws Exception {
		if (breakpoints == null) {
			breakpoints = new Hashtable<String, Breakpoint>();

			// Components
			if (task instanceof AssemblyLine) {
				AssemblyLine assemblyline = (AssemblyLine) task;
				AssemblyLineConfig config = assemblyline.getConfigClone();

				// AL Hooks
				Breakpoint bp;
				String[] alhooks = { InternalSchema.AL_PROLOG,
						InternalSchema.AL_PROLOG_INIT,
						InternalSchema.AL_EPILOG2, InternalSchema.AL_EPILOG,
						InternalSchema.AL_SHUTDOWN,
						InternalSchema.AL_STARTCYCLE,
						InternalSchema.AL_ONSUCCESS,
						InternalSchema.AL_ONFAILURE, };

				for (int i = 0; i < alhooks.length; i++) {
					String str = alhooks[i];
					HookConfig hook = config.getHook(str);
					bp = new Breakpoint(str, (hook == null ? false : hook
							.getDebugBreak(false)), null);
					breakpoints.put(bp.getLocation(), bp);
					components.add(bp.getLocation());
				}

				List<AssemblyLineComponent> c = assemblyline.getConnectors();
				for (AssemblyLineComponent tc : c) {
					if (tc instanceof EndLoopComponent
							|| tc instanceof EndBranchComponent)
						continue;

					BaseConfiguration cfg = tc.getConfiguration();
					ConnectorConfig cc = null;

					if (tc instanceof LoopComponent
							&& ((LoopComponent) tc).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
						cfg = ((LoopComponent) tc).getBaseConfiguration();
						cc = ((LoopConfig) cfg).getLoopConnector();
					} else if (tc instanceof BranchingComponent) {
						bp = new Breakpoint(tc.getName(),
								((BranchingComponent) tc)
								.getBaseConfiguration().getDebugBreak(
										false), null);
						breakpoints.put(bp.getLocation(), bp);
						components.add(bp.getLocation());
						continue;
					} else if (tc.getConfiguration() != null) {
						cc = tc.getConfiguration();
					}

					// Loop comp with conn has no tc.getConfig()
					if (cfg == null) {
						continue;
					}

					bp = new Breakpoint(tc.getName(), cfg.getDebugBreak(false),
							null);
					breakpoints.put(bp.getLocation(), bp);
					components.add(bp.getLocation());

					// No hooks for script component
					if (tc instanceof ScriptComponent)
						continue;

					Object[] hooks = HooksUtil.getHookTree(cc);

					if (hooks != null) {
						for (String hookName : getHookNames(tc, hooks)) {
							if (hookName == null)
								continue;
							bp = new Breakpoint(tc.getName() + "." + hookName,
									false, null);
							HookConfig hc = null;
							if (cc.getHooks() != null)
								hc = cc.getHooks().getHook(hookName);
							if (hc != null) {
								bp.setEnabled(hc.getDebugBreak(false));
								breakpoints.put(bp.getLocation(), bp);
							} else {
								bp = new Breakpoint(hookName, false, null);
								breakpoints.put(bp.getLocation(), bp);
							}
						}
					}
					addMapBreakPoints(cc, true);
					addMapBreakPoints(cc, false);
					addScriptBreakPoints(cc);
				}
				
				AssemblyLineScripts scripts = new AssemblyLineScripts(config);
				for (String name: scripts.getAllNames()) {
					addBP(name);
					components.add(name);
				}
			}
		}

		return breakpoints;
	}

	private void addMapBreakPoints(ConnectorConfig cc, boolean input) {
		AttributeMapConfig map = cc.getAttributeMap(input);
		String prefix = cc.getShortName() + "." + map.getShortName() + ".";
		for (Object attr : (map.getAttributeNames())) {
			addBP(prefix + attr);
		}
	}

	private void addScriptBreakPoints(ConnectorConfig cc) {
		RawConnectorConfig rcc = cc.getConnectionConfig();
		if (rcc != null && "com.ibm.di.connector.ScriptConnector".equals(rcc.getJavaClass()))
			addBP(cc.getShortName() + ".Connector");
		ParserConfig pc = cc.getParserConfig();
		if (pc != null && "com.ibm.di.parser.ScriptParser".equals(pc.getJavaClass()))
			addBP(cc.getShortName() + ".Parser");
		if (cc instanceof FunctionConfig) {
			RawFunctionConfig rfc = ((FunctionConfig)cc).getFunctionConfig();
			if (rfc != null && "com.ibm.di.fc.ScriptedFC".equals(rfc.getJavaClass()))
				addBP(cc.getShortName() + ".Function");
		}
	}

	/**
	 * Add a breakpoint for the given location
	 */
	private void addBP(String location) {
		breakpoints.put(location, new Breakpoint(location, false, null));
	}

	/**
	 * Returns a table with the script engine variables. The variables that are
	 * not Serializable are returned as NotSerializable Objects instead of the
	 * actual object.
	 *
	 * @param filter
	 *            regex to filter specific variables. Not used.
	 * @return Returns a Hashtable of script engine variables
	 */
	public Hashtable<String, Object> getScriptEngineVariables(String filter) {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		NotSerializable.SimpleMap smap = new NotSerializable.SimpleMap(); // To avoid loops
		try {
			FBSGlobalObject g = currentEngine.getJsengine().getGlobalObject();
			for (Iterator<String> i = g.getPropertyKeys(); i.hasNext();) {
				String prop = i.next();
				IValue iv = g.getProperty(prop);
				if (iv == null || iv instanceof FBSNull) {
					ht.put(prop, new NullValue());
					continue;
				}

				Object value = iv.toJavaObject();
				if (value == null)
					value = iv; //fall back, maybe use another method?

				ht.put(prop, NotSerializable.convertObject(value, smap));
			}

			if (currentContext != null) {
				FBSDefaultObject vars = currentContext.getVariableObject();
				for (String prop:currentContext.getJSVariables()) {
					IValue iv = vars.propGet(prop);
					if (iv == null || iv instanceof FBSNull) {
						if (iv != null || !ht.containsKey(prop))
							ht.put(prop, new NullValue());
						continue;
					}

					Object value = iv.toJavaObject();
					if (value == null)
						value = iv; //fall back, maybe use another method?

					ht.put(prop, NotSerializable.convertObject(value, smap));
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		return ht;
	}

	/**
	 * Checks if entry is of type Entry and traverses all attributes and values
	 * to see if any of them are not serializable. Non serializable values are
	 * converted to its string representation in the new returned entry.
	 *
	 * @param entry
	 *            The Entry object to convert
	 * @return The converted Entry
	 */
	public Entry convertEntry(Entry entry) {
		return NotSerializable.convertEntry(entry);
	}

	/**
	 * Converts the values in the attribute to a serializable representation if
	 * needed.
	 *
	 * @param a
	 *            The attribute to convert
	 * @return A new Attribute with serializable values
	 */
	public Attribute convertAttribute(Attribute a) {
		return NotSerializable.convertAttribute(a);
	}

	/**
	 * Converts an object to a serializable representation if needed.
	 *
	 * @param value
	 *            Object to convert.
	 * @return A serializable representation of the object.
	 */
	public Object convertObject(Object value) {
		return NotSerializable.convertObject(value);
	}

	public List<String> getHookNames(AssemblyLineComponent tc, Object[] hooks) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < hooks.length; i++) {
			Object hook = hooks[i];
			if (hook instanceof String) {
				list.add((String) hook);
			} else {
				list.addAll(getHookNames(tc, (Object[]) hook));
			}
		}
		return list;
	}

	public Object getConnectorList(boolean includestats) throws Exception {

		if (task instanceof AssemblyLine) {
			Vector<String> s = new Vector<String>();
			for (AssemblyLineComponent tc : ((AssemblyLine) task).getConnectors()) {
				if (includestats)
					s.add(tc.getName() + ": " + tc.getStats());
			}
			return s;
		}

		return sResHash.getString("MISERVER.DEBUGSERVER.NO.CONNECTOR.LIST.IN.TASK");
	}

	public void logmsg(Object str) {
		if (log != null)
			log.loginfo(str.toString());
		else
			System.out.println(str);
	}

	/**
	 * This method write an object or an exception to the debugger.
	 */
	public void safeWriteObject(Object obj) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream testos = new ObjectOutputStream(bos);
		try {
			testos.writeObject(obj);
			testos.close();
			os.writeObject(bos.toByteArray());
		} catch (Throwable nse) {
			if (obj instanceof Throwable)
				os.writeObject(obj.toString());
			else
				os.writeObject(new NotSerializable(obj, nse));
		}
	}

	/**
	 * @return the onerror flag
	 */
	public boolean isOnerror() {
		return onerror;
	}

	/**
	 * Sets the onerror flag. When true breakpoints are disabled except when
	 * there is an error.
	 *
	 * @param onerror
	 */
	public void setOnerror(boolean onerror) {
		this.onerror = onerror;
	}

	/**
	 * Returns the last known location. This is the last call to debugBreak with
	 * a valid breakpoint name (e.g. conn.after_getnext etc).
	 *
	 * @return Last known location
	 */
	public String getLastKnownLocation() {
		return lastKnownLocation;
	}

	/**
	 * Returns true if an ABORT message has been sent to the remote CE
	 *
	 * @return Returns true if an ABORT message has been sent to the remote CE.
	 *         Otherwise, false is returned.
	 */
	public boolean isAborted() {
		return abortSent;
	}

	private void _debugStatement(ASTNode statement, IExecutionContext context)
	throws JavaScriptException {

		if (breakpoints == null || runToCycle >= 0)
			return;

		String script = statement.getExpr();
		Integer iref = script.hashCode();
		String ref = scriptRefID.get(iref);

		// -- make sure CE has the script, hashCode and source ref id (could be a script included dynamically etc)
		// -- This is done once when the script is first executed in its ProgramContext

		if(ref == null) {
			try {
				String contextScript = context.getExpression().getExpr();

				// -- If this is the first call we should get the source ref from current context
				if(script.equals(contextScript)) {
					ref = context.getSourceReferenceID();
				}

				// -- If scripts don't have executable (e.g. only functions) we are not called here.
				// -- Check with the script engine for a compiled expression matching this script to
				// -- find the source reference id.
				if(ref == null || JSExpression.DYNAMIC_SOURCE_ID.equals(ref) ) {
					ref = currentEngine.getRefID(script);
				}

				// -- Still no reference means it was loaded from elsewhere (unknown to us)
				if(ref == null)
					ref = "Unknown-" + iref;

				scriptRefID.put(iref, ref);
				Object[] params = new Object[] {
						iref,
						ref,
						script
				};
				os.writeObject(SCRIPT);
				os.writeObject(params);
				os.flush();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// We should stop if either
		// 1. We are doing a step into
		// 2. We are doing a step over and have finished stepping over
		// 3. We have reached a breakpoint or a "run and break here".
		// 4. The user has tried to stop the execution from the CE
		if(! (step || isStepOverBreak(statement, context) || isBreak(ref, statement.getBeginLine()) || inputAvailable())) {
			return;
		}

		// -- Send the break
		String saveLastKnown = lastKnownLocation;
		try {
			// -- save the current node/context
			stepOverNode = statement;
			currentContext = context;
			if (ref == null || breakpoints.get(ref) == null)
				ref = lastKnownLocation;
			debugBreak(ref, isOnerror(), statement.getBeginLine(), iref);
		} catch (Exception e) {
			throw new InterpretException(e);
		} finally {
			currentContext = null;
			lastKnownLocation = saveLastKnown;
		}
	}

	private boolean inputAvailable() {
		try {
			return socket.getInputStream().available() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return true if there is an enabled breakpoint at the line, or we are doing a "Run and break here"
	 * @param ref
	 * @param beginLine
	 * @return
	 */
	private boolean isBreak(String ref, int beginLine) {
		if (ref == null)
			return false;
		String loc = ref + "#" + beginLine;
		if (loc.equals(breakAt))
			return true;
		Breakpoint b = breakpoints.get(loc);
		// This method does not evaluate breakpoint conditions, that is done in debugBreak()
		return b != null && b.isEnabled();
	}

	/**
	 * Returns true if we are stepping over, and have exited the node or function call we are stepping over
	 *
	 * @param node
	 * @param ctx
	 * @return
	 */
	private boolean isStepOverBreak(ASTNode node, IExecutionContext ctx) {

		if (! stepOver || stepOverContext == null)
			return false;

		// -- If we are stepping over then we check if the current node/ctx is a child of
		// -- the step-over node/ctx. If it is, we have not yet "returned" to the point where
		// -- the step-over was initiated.

		// -- check if node is child of currentCall
		if(stepOverContext == ctx) {
			for(ASTNode n = node; n != null; n = n.getParent()) {
				if(n == stepOverNode) {
					return false;
				}
			}
		} else {
			// Check if we are inside a function call that we are stepping over
			IExecutionContext e = ctx;
			while (e instanceof FunctionContext) {
				e = ((FunctionContext)e).getCallerContext();
				if ( e == stepOverContext )
					return false;
			}
		}

		// We must have stepped over what we should step over. Force a break.
		step = true; // force a break
		return true;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String id) {
		uniqueID = id;
	}

	private void addDebugListener(ScriptEngine se) {
		if (se == null)
			return;
		DebugListener l = listenerMap.get(se);
		if (l == null) {
			l = new DebugListener(se);
			listenerMap.put(se, l);
		}
		se.addDebugListener(l);
	}

	private void removeDebugListener(ScriptEngine se) {
		if (se == null)
			return;
		DebugListener l = listenerMap.get(se);
		if (l != null) {
			se.removeDebugListener(l);
		}
	}

	private void removeAllListeners() {
		for (DebugListener l : listenerMap.values()) {
			l.se.removeDebugListener(l);
		}
		listenerMap.clear();
	}

	/**
	 * Add a ScriptEngine to be listened to
	 * @param engine
	 * @param name  Component name used to identify the script
	 * @since 7.2
	 */
	public void addScriptEngine(ScriptEngine engine, String name) {
		if (engine == null)
			return;
		DebugListener l = new DebugListener(engine, name);
		listenerMap.put(engine, l);
		engine.addDebugListener(l);
	}

	/**
	 * Tells the debugger that we have reached this cycle in the AL run.
	 * @param cycle
	 */
	public void reachedCycle(long cycle) {
		if (runToCycle >= 0 && cycle >= runToCycle) {
			runToCycle = -1;
			step = true;
		}
	}
	
	/**
	 * private class to be able to tell which ScriptEngine a debugStatement comes from
	 * @author Administrator
	 *
	 */
	private class DebugListener implements ScriptEngineOptions.TDIDebugListener {
		private ScriptEngine se;
		private String name;

		public DebugListener(ScriptEngine se) {
			super();
			this.se = se;
		}

		public DebugListener(ScriptEngine se, String name) {
			super();
			this.se = se;
			this.name = name;
		}

		/**
		 * This method is called from ASTDebug for each node while debugging.
		 * First sets currentEngine, then calls the method that decides if we should break here.
		 */
		public void debugStatement(ASTNode statement, IExecutionContext context)
		throws JavaScriptException {
			currentEngine = se;
			if (name != null)
				lastKnownLocation = name;
			_debugStatement(statement, context);	
		}
	}
}
