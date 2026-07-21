/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpSession;

import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugMessage;
import com.ibm.di.util.DebugServer;
import com.ibm.di.util.NullValue;

/**
 * This class is used to control an AssemblyLine on a TDI server. This class
 * establishes a session with the DebugServer object of the AssemblyLine and
 * wraps the complexity of dealing with breakpoints, watch lists and state
 * management that the StepperThread/StepperListener classes does not offer.
 * <p>
 * When the AssemblyLine is running it will send break events at predefined
 * points in the AssemblyLine or when an error occurs. These events are handled
 * first by this class that filters out those that are not registered and
 * immediately tells the AssemblyLine to continue. Once a breakpoint is found
 * that is handled a notifications is fired putting the assemblyline on wait.
 * <p>
 * The debug client session have states that reflect the state of the
 * AssemblyLine and determine whether a command can be sent or not.<br/>
 * <ul>
 * <li>Running - The AssemblyLine is running and all commands are disabled
 * except Pause/Stop which are sent immediately (but only once).
 * <li>Pending - A stop/pause has been sent and the client is waiting for the
 * AssemblyLine to respond.
 * <li>Waiting - The AssemblyLine has responded and is waiting for a command
 * from the client; all commands are enabled.
 * <li>Terminated - The AssemblyLine has terminated and all commands except
 * startAsemblyLine are disabled.
 * </ul>
 * <p/>
 * The current breakpoint/location for the AssemblyLine is available through
 * various methods and reflect the last location update sent by the
 * AssemblyLine. Break events that are sent by this class have an instance of
 * the DebugBreak class to describe the breakpoint details.
 * <p/>
 * The watch list contains expressions that are updated upon entering Waiting
 * state. When this class decides a breakpoint is reached that requires a
 * response to continue (e.g. state changes from Running to Waiting), the list
 * of watch expressions are evaluated and updated before notifying listeners
 * about the state change.
 * <p/>
 * 
 */

public class DebugClient {
	@SuppressWarnings("unused") //$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private boolean debug = true;

	private static final String JAVASCRIPT_VARS = "ScriptEngine";

	/*
	/*
	 * The state of the debug session
	 */
	protected DebugState debugState = new DebugState();

	/**
	 * The session we have going with the AssemblyLine
	 */
	private StepperThread sessionThread = null;

	/**
	 * This is the current cycle number
	 * message
	 */
	private long currentCycleNumber;
	
	/**
	 * This map contains id/script pairs sent by the assemblyline. The first
	 * time a script is executed both the id and script is sent. On subsequent
	 * calls only the id is sent to signal the switch from one script to
	 * another. Although the scripts are typically available in the config there
	 * are cases where the script being executed is either different (loaded
	 * from same file path but different machine) or simply unavailable (e.g.
	 * manually loaded by user's script).
	 */
	private Hashtable<Integer, ScriptData> scriptMap = new Hashtable<Integer, ScriptData>();

	/**
	 * The current or last known script executed by the assemblyline (e.g. hook
	 * script, included script etc).
	 */
	private ScriptData currentScriptData;

	/**
	 * An expression from the user that we are currently evaluating. The result should be shown to the user.
	 */
	private String evaluatingExpression;
	
	/**
	 * Set after a runUntilAssemblyLine() call is made and cleared when a break
	 * occurs.
	 */
	private String runUntilBreakpoint;

	/**
	 * This is the hostname we use to listen for an incoming connection from the AL
	 */
	private String hostname;

	/**
	 * This is the port we use to listen for an incoming connection from the AL
	 */
	private int port;

	/**
	 * Last known breakpoint
	 */
	private String lastBreak;

	private Map<Object,Object> lastScriptEngineEval;

	private StepperListener sessionThreadListener;

	/**
	 * Constructor used to start a new config instance and assemblyline
	 * 
	 * @param session
	 * @throws Exception 
	 */
	public DebugClient(HttpSession session) throws Exception {
		final StepperThread serverConnection = new StepperThread(UUID.randomUUID().toString());
		serverConnection.addStepperListener(new StepperListener() {
			public void handleEvent(StepperEvent event) {
				switch (event.getCommand()) {
				case StepperEvent.SS_CONNECT:
					try {
						// -- accept the connection
						acceptDebugConnection(event);
						// -- terminate the serverConnection; we only accept
						// one debug session
						serverConnection.shutdown(false);
						fireDebugEvent(event.getCommand(), event.getData());
					} catch (Exception e1) {
						e1.printStackTrace();
						fireDebugEvent(StepperEvent.BREAK, e1);
					}
					break;
				}
			}
		});
		
		this.hostname = serverConnection.getHostName();
		this.port = serverConnection.getLocalPort();
		serverConnection.start();
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	public int getPort() {
		return this.port;
	}
	

	/**
	 * Create a session thread for the incoming connection from the assemblyline
	 * 
	 * @param event
	 */
	public void acceptDebugConnection(StepperEvent event) {
		//
		// -- Close down previous session thread
		//
		if (sessionThread != null)
			sessionThread.shutdown();

		//
		// -- Create a new session thread and add ourself as even listener
		//
		try {
			sessionThread = new StepperThread(event.getSocket());
			sessionThreadListener = new StepperListener() {
				public void handleEvent(StepperEvent event) {
					(DebugClient.this).handleEvent(event);
				}
			};
			sessionThread.addStepperListener(this.sessionThreadListener);
			setState(DebugState.STATE_RUNNING);
			sessionThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			setState(DebugState.STATE_IDLE);
		}
	}

	/*
	 * This is the main event handler. All debug events are sent to this method
	 * where we filter out events and forward others to the event listeners. <p>
	 * <b>Breakpoints</b> When a break is received it is checked against the
	 * breakpoints set by the user. If there is a match the break event
	 * notification is sent and the assemblyline is kept on wait. If there is no
	 * matching breakpoint the last command sent is checked. A last command of
	 * STEP or STEP_INTO are implicit breaks so we stop on those as well. <p>
	 * <b>Other events</b>
	 */
	public void handleEvent(StepperEvent event) {
		DebugMessage msg;

		if (debug)
			System.out.println("DebugClient: " + event);
		switch (event.getCommand()) {
		case StepperEvent.CONFIG:
			msg = (DebugMessage) event.getData();
			if (msg.getDefault() instanceof Throwable) {
				((Throwable) msg.getDefault()).printStackTrace();
			} else {
//				Hashtable<String, Object> env = new Hashtable<String, Object>();
				try {
//					assemblylineConfig = mx.getAssemblyLine(assemblylineName);
					fireDebugEvent(StepperEvent.CONFIG, msg.getProp("name"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;

		case StepperEvent.CONTROL:
			if (event.getData() instanceof DebugMessage) {
				DebugMessage dm = (DebugMessage) event.getData();
				if ("switch".equals(dm.getName())) {
					debugState.setCurrentLocation((String) dm.getDefault());
					fireDebugEvent(StepperEvent.CONTROL, dm.getDefault());
				}
			}
			break;

		case StepperEvent.BREAKPOINTS:
			fireDebugEvent(StepperEvent.BREAKPOINTS, event.getData());
			break;

		case StepperEvent.INIT:
			break;

		case StepperEvent.BREAK:
			DebugBreak debugBreak = null;
			Breakpoint b = breakpoints.get(event.getData());
			this.lastBreak = event.getData() == null ? "" : event.getData().toString();
			int lc = sessionThread.getLastCommand();

			if (!(event.getData() instanceof Throwable))
				debugBreak = createDebugBreak(event.getData());

			if (DebugServer.INIT_BREAK.equals(event.getData())) {
				// -- Always stop on first break after init
				fireDebugEvent(StepperEvent.BREAK, debugBreak);

			} else if (isPending()) {
				// -- If pauseAssemblyLine() we break
				fireDebugEvent(StepperEvent.BREAK, debugBreak);

			} else if (event.getData() instanceof Throwable) {
				// -- If an error occurred we break
				fireDebugEvent(StepperEvent.BREAK, event.getData());

			} else if (getRunUntilBreakpoint() != null) {
				// -- If we have reached the run until component we break
				// otherwise
				// -- we send a continue
				if (event.getData().equals(getRunUntilBreakpoint())) {
					setRunUntilBreakpoint(null);
					fireDebugEvent(StepperEvent.BREAK, debugBreak);
				} else {
					try {
						sessionThread.sendCommand(StepperEvent.CONT);
					} catch (Exception e) {
						fireDebugEvent(StepperEvent.BREAK, e);
					}
				}

			} else if (lc == StepperEvent.STEP || lc == StepperEvent.STEP_OVER || lc == StepperEvent.BREAKAT || lc == StepperEvent.RUN_TO_CYCLE) {
				// -- If we are stepping check if we are at the right node since
				// -- the last break
				fireDebugEvent(StepperEvent.BREAK, debugBreak);
				// try {
				// // -- Don't use stepAssemblyLine() methods since they check
				// for state
				// // -- and we don't want to fire unnecessary state changes.
				// System.out.println("DebugClient: auto-run: " + debugBreak);
				// sessionThread.sendCommand(lc);
				// } catch (Exception e) {
				// fireDebugEvent(StepperEvent.BREAK, e);
				// }

			} else if (b != null && b.isEnabled()) {
				// -- Explicitly enabled break
				fireDebugEvent(StepperEvent.BREAK, debugBreak);

			} else {
				// Otherwise just keep going. Don't use continueAssemblyline
				// as it checks for running state and we don't want to toggle
				// run state more often than necessary.
				try {
					sessionThread.sendCommand(StepperEvent.CONT);
				} catch (Exception e) {
					fireDebugEvent(StepperEvent.BREAK, e);
				}
			}
			break;

		case StepperEvent.SS_DISCONNECT:
			fireDebugEvent(StepperEvent.SS_DISCONNECT, event.getData());
			setState(DebugState.STATE_IDLE);
			break;

		case StepperEvent.EVAL:
			try {
				msg = (DebugMessage) event.getData();
				String name = (String) msg.getProp("eval");
				Object value = msg.getProp("value");
				
				if("ScriptEngine".equals(name) && value instanceof Map) {
					this.lastScriptEngineEval = (Map<Object, Object>) value;
				}

				// If we are watching update the watch table, otherwise just
				// fire a logmsg event. All UI components should use addWatch
				// for those variables that are being watched.
				if (isWatching(name) && !name.equals(evaluatingExpression)) {
					getWatchList().put(name, value);
					fireDebugEvent(StepperEvent.EVAL, name);
				} else {
					evaluatingExpression = null;
					fireDebugEvent(DebugClientEvent.EVAL_MESSAGE, name + " >> " + value + "\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case StepperEvent.SCRIPT:
			if (event.getData() instanceof Integer) {
				// -- context switch to this script
				currentScriptData = scriptMap.get((Integer) event.getData());
			} else if (event.getData() instanceof Object[]) {
				// -- initial upload of script data from server
				Object[] params = (Object[]) event.getData();
				ScriptData sd = new ScriptData((Integer) params[0], (String) params[1], (String) params[2]);
				scriptMap.put(sd.iref, sd);
			}
			break;
			
		case StepperEvent.UNIQUE_ID:
			fireDebugEvent(StepperEvent.UNIQUE_ID, event.getData());
			break;
			
		}
	}

	public Object getLastScriptEngineEval() {
		return lastScriptEngineEval;
	}

	private boolean isDebug() {
		return true;
	}

	/**
	 * Sends a stop signal via the debugger (debugging) or via the assemblyline
	 * handle. Can only send this when the assemblyline is Running or Waiting.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void stopAssemblyLine() throws Exception {
		if (isIdle() || isPending())
			throw new IllegalStateException(debugState.toString());
		sessionThread.sendCommand(StepperEvent.STOP);
	}

	/**
	 * Sends a step signal to the assemblyline to make it break on the next
	 * break location. This is a soft break; if the assemblyline is caught in an
	 * infinite loop or some other operation that prevents it from reaching the
	 * next debug break location, the only thing that can be done is to send a
	 * stop signal via the server api (this is not done by this class).
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void pauseAssemblyLine() throws Exception {
		if (isDebug() && isRunning()) {
			if (debug)
				System.out.println("pauseAssemblyLine");
			setState(DebugState.STATE_PENDING);
			sessionThread.sendCommand(StepperEvent.STEP);
		} else {
			throw new IllegalStateException("pauseAssemblyLine: " + debugState.toString());
		}
	}

	/**
	 * Sends a continue signal via the debugger to the assemblyline.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void continueAssemblyLine() throws Exception {
		if (isWaiting()) {
			if (debug)
				System.out.println("continueAssemblyLine");
			setState(DebugState.STATE_RUNNING);
			sessionThread.sendCommand(StepperEvent.CONT);
		} else {
			throw new IllegalStateException("continueAssemblyLine: " + debugState.toString());
		}
	}

	/**
	 * Sends a step into signal via the debugger to the assemblyline.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void stepAssemblyLine() throws Exception {
		if (isDebug() && isWaiting()) {
			if (debug)
				System.out.println("stepAssemblyLine");
			setState(DebugState.STATE_RUNNING);
			sessionThread.sendCommand(StepperEvent.STEP);
		} else {
			throw new IllegalStateException("stepAssemblyLine: " + debugState.toString());
		}
	}

	/**
	 * Sends a step over signal via the debugger to the assemblyline.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void stepOverAssemblyLine() throws Exception {
		if (isDebug() && isWaiting()) {
			if (debug)
				System.out.println("stepOverAssemblyLine");
			setState(DebugState.STATE_RUNNING);
			sessionThread.sendCommand(StepperEvent.STEP_OVER);
		} else {
			throw new IllegalStateException("stepOverAssemblyLine: " + debugState.toString());
		}
	}

	/**
	 * Sends a run-until signal via the debugger to the assemblyline.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void runUntilAssemblyLine(String breakpoint) throws Exception {
		if (isDebug() && isWaiting()) {
			if (debug)
				System.out.println("runUntilAssemblyLine: " + breakpoint);
			setRunUntilBreakpoint(breakpoint);
			setState(DebugState.STATE_RUNNING);
			sessionThread.sendCommand(StepperEvent.BREAKAT, breakpoint);
		} else {
			throw new IllegalStateException("runUntilAssemblyLine: " + debugState.toString());
		}
	}

	/**
	 * Sends a run to AL cycle signal via the debugger to the assemblyline.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void runToCycle(long cycle) throws Exception {
		if (isDebug() && isWaiting()) {
			setState(DebugState.STATE_RUNNING);
			sessionThread.sendCommand(StepperEvent.RUN_TO_CYCLE, Long.valueOf(cycle));
		} else {
			throw new IllegalStateException("runToCycle: " + debugState.toString());
		}
	}

	/**
	 * Sets the run until breakpoint
	 * 
	 * @param breakpoint
	 */
	private void setRunUntilBreakpoint(String breakpoint) {
		this.runUntilBreakpoint = breakpoint;
	}

	/**
	 * Returns the run until breakpoint
	 * 
	 * @return
	 */
	private String getRunUntilBreakpoint() {
		return runUntilBreakpoint;
	}

	//
	// -- State management
	//

	/**
	 * Returns the DebugState object that contains the state information about
	 * the debug session.
	 * 
	 * @return DebugState object
	 */
	public DebugState getState() {
		return debugState;
	}

	/**
	 * Returns true if the debugger state is running (e.g. we don't have
	 * control)
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return debugState.getState() == DebugState.STATE_RUNNING;
	}

	/**
	 * Returns true if a stop/break has been sent to the assemblyline to
	 * stop/pause it
	 * 
	 * @return
	 */
	public boolean isPending() {
		return debugState.getState() == DebugState.STATE_PENDING;
	}

	/**
	 * Returns true if the assemblyline is waiting for a command to continue
	 * execution
	 * 
	 * @return
	 */
	public boolean isWaiting() {
		return debugState.getState() == DebugState.STATE_WAITING;
	}

	/**
	 * Returns true if there is no assemblyline running
	 * 
	 * @return
	 */
	public boolean isIdle() {
		return debugState.getState() == DebugState.STATE_IDLE;
	}

	/**
	 * Called internally to change the debug state.
	 * 
	 * @param state
	 *            The new state
	 */
	protected void setState(int state) {
		if (state != debugState.getState()) {
			debugState.setState(state);
			fireDebugEvent(DebugClientEvent.STATE_CHANGE, getState());
		}
	}

	//
	// -- Breakpoint management
	//
	private Hashtable<String, Breakpoint> breakpoints = new Hashtable<String, Breakpoint>();
	private ArrayList<Breakpoint> pendingBreakpoints = new ArrayList<Breakpoint>();

	/**
	 * Clears all breakpoints
	 */
	public void clearBreakpoints() {
		// Update assemblyline debugger by disabling them.
		if(isWaiting()) {
			// Send commands to assemblyline
			for(Breakpoint bp : breakpoints.values()) {
				Breakpoint brk = new Breakpoint(bp.getLocation(), false, null);
				try {
					sessionThread.sendCommand(StepperEvent.BREAKPOINT, brk);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			// Add to pending list so we can clear on first break.
			// Note that we MUST clone the breakpoints since they might be
			// re-enabled later (e.g. bp.off then bp.on).
			for(Breakpoint bp : breakpoints.values()) {
				Breakpoint brk = new Breakpoint(bp.getLocation(), false, null);
				pendingBreakpoints.add(brk);
			}
		}
		breakpoints.clear();
	}

	/**
	 * Sets a breakpoint at the specified location
	 * 
	 * @param breakPoint
	 * @throws Exception
	 */
	public void addBreakpoint(Breakpoint breakPoint) throws Exception {
		breakpoints.put(breakPoint.getLocation(), breakPoint);
		breakPoint.setEnabled(true);
		if (isWaiting()) {
			sessionThread.sendCommand(StepperEvent.BREAKPOINT, breakPoint.getClone());
		} else {
			pendingBreakpoints.add(breakPoint.getClone());
		}
	}

	/**
	 * Clears the breakpoint at the specified location
	 * 
	 * @param breakPoint
	 * @throws Exception
	 */
	public void removeBreakpoint(Breakpoint breakPoint) throws Exception {
		breakpoints.remove(breakPoint.getLocation());
		if (isWaiting()) {
			sessionThread.sendCommand(StepperEvent.BREAKPOINT, breakPoint.getClone());
		} else {
			pendingBreakpoints.add(new Breakpoint(breakPoint.getLocation(), false, null));
		}
	}

	/**
	 * Clears the breakpoint at the specified location
	 * 
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public Breakpoint removeBreakpoint(String location) throws Exception {
		Breakpoint bp = breakpoints.get(location);
		if(bp == null) {
			bp = new Breakpoint(location, false, null);
			breakpoints.put(location, bp);
		}
		removeBreakpoint(bp);
		return bp;
	}

	/**
	 * Called when the debug client is in waiting state. Sends all pending breakpoint updates to the assemblyline.
	 */
	private void refreshBreakpoints() {
		// Make sure nobody tampers with the array while we are running this one
		synchronized (pendingBreakpoints) {
			for(Breakpoint bp : pendingBreakpoints) {
				try {
					sessionThread.sendCommand(StepperEvent.BREAKPOINT, bp.getClone());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pendingBreakpoints.clear();
		}
	}

	//
	// -- Watch list management
	//
	private HashMap<String, Object> watchList = new HashMap<String, Object>();

	/**
	 * Returns the watch list that contains expressions as the keys and the last
	 * recorded value as the key's value.
	 * 
	 * @return
	 */
	public HashMap<String, Object> getWatchList() {
		return watchList;
	}

	/**
	 * Clears the watch list.
	 */
	public void clearWatchList() {
		watchList.clear();
	}

	/**
	 * Adds an expression to the watch list. If the assemblyline is in waiting
	 * state we send the eval request immediatly; otherwise the watch expression
	 * is updated on the next break. The watch list is updated whenever the
	 * assemblyline transitions to waiting state at which point the DebugClient
	 * executes all expressions and updates the watchList with the evaluated
	 * expression's value.
	 * 
	 * @param expression
	 * @throws Exception
	 */
	public void addWatch(String expression) throws Exception {
		if (watchList.get(expression) == null) {
			watchList.put(expression, new NullValue());

			// -- If assemblyline is waiting we can send the EVAL immediately
			if (isWaiting()) {
				sessionThread.sendCommand(StepperEvent.EVAL, expression);
			}
		} else if (JAVASCRIPT_VARS.equals(expression) && isWaiting()) {
			sessionThread.sendCommand(StepperEvent.EVAL, expression);			
		}
	}

	/**
	 * Returns true if the expression is in the list of watched expressions.
	 * 
	 * @param expression
	 * @return
	 */
	public boolean isWatching(String expression) {
		return watchList.get(expression) != null;
	}

	/**
	 * Removes the expression from the watch list
	 * 
	 * @param expression
	 */
	public void removeWatch(String expression) {
		watchList.remove(expression);
	}

	/**
	 * Returns the last value recorded for a watch expression. If an expression
	 * results in a null value, the NullValue object is returned to distinguish
	 * it from an empty string.
	 * 
	 * @param expression
	 * @return
	 */
	public Object getWatchValue(String expression) {
		return watchList.get(expression);
	}

	/**
	 * Executes the expression in the assemblyline's script engine. This can
	 * only be executed when the assemblyline is waiting.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void evaluateExpression(String expression, boolean fromWatchList) throws Exception {
		if (!fromWatchList)
			evaluatingExpression = expression;
		if (isWaiting()) {
			sessionThread.sendCommand(StepperEvent.EVAL, expression);
		} else {
			throw new IllegalStateException("evaluateExpression: " + debugState.toString());
		}
	}
	
	public void shutdown() {
		if(sessionThread != null) {
			sessionThread.removeStepperListener(sessionThreadListener);
			sessionThread.shutdown(true);
		}
	}

	/**
	 * Sends an EVAL request for each of the expressions in the watch list.
	 */
	private void refreshWatchVariables() {
		// Save last command sent from user
		int lastCommand = sessionThread.getLastCommand();
		currentCycleNumber = debugState.getCycleCounter();
		try {
			for (String str : watchList.keySet())
				if (!JAVASCRIPT_VARS.equals(str))
					evaluateExpression(str, true);
			evaluateExpression(JAVASCRIPT_VARS, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sessionThread.setLastCommand(lastCommand);
	}

	//
	// -- Event listeners
	//
	private ArrayList<DebugClientListener> clientListeners = new ArrayList<DebugClientListener>();

	/**
	 * Send notification to event listeners and update debug state based on the
	 * debugEvent.
	 * 
	 * @param debugEvent
	 * @param data
	 */
	protected void fireDebugEvent(int command, Object data) {
		int debugEvent = command;
		switch (debugEvent) {
		case StepperEvent.BREAK:
			setState(DebugState.STATE_WAITING);
			break;
		case StepperEvent.SS_DISCONNECT:
			setState(DebugState.STATE_IDLE);
			break;
		case StepperEvent.SS_CONNECT:
			setState(DebugState.STATE_RUNNING);
			break;
		}

		if(debug)
			System.out.println("fireDebugEvent: " + DebugClientEvent.eventName(debugEvent) + "; " + data);

		// -- Refresh watch variables and breakpoints
		if (debugEvent == DebugClientEvent.BREAK) {
			refreshWatchVariables();
			refreshBreakpoints();
		}
		
		//
		// -- Send notifications using a copy to avoid concurrent mod errors
		//
		ArrayList<DebugClientListener> copy = new ArrayList<DebugClientListener>(clientListeners);
		DebugClientEvent event = new DebugClientEvent(debugEvent, data, this);
		for (DebugClientListener dcl : copy) {
			dcl.handleEvent(event);
		}

	}

	/**
	 * Create and populate an instance of DebugBreak with the current state
	 * information
	 * 
	 * @param data
	 *            The breakpoint
	 * @return
	 */
	private DebugBreak createDebugBreak(Object data) {
		DebugBreak db = new DebugBreak((String) data, getCurrentScriptData());
		debugState.setCurrentDebugBreak(db);
		return db;
	}

	/**
	 * Adds a listener to receive notifications of debug events
	 * 
	 * @param listener
	 */
	public void addDebugListener(DebugClientListener listener) {
		if (!clientListeners.contains(listener))
			clientListeners.add(listener);
	}

	/**
	 * Removes a listener from the notification list
	 * 
	 * @param listener
	 */
	public void removeDebugListener(DebugClientListener listener) {
		clientListeners.remove(listener);
	}

	//
	// -- Script references
	//
	public static class ScriptData {
		private String script;
		private int iref;
		private String sourceRef;

		public ScriptData(int iref, String sourceRef, String script) {
			super();
			this.iref = iref;
			this.sourceRef = sourceRef;
			this.script = (script == null ? "" : script.trim());
		}

		public String getScript() {
			return script;
		}

		public int getIref() {
			return iref;
		}

		public String getSourceRef() {
			return sourceRef;
		}
	}

	/**
	 * Returns the current/last-known script being executed by the assemblyline.
	 * This script is transmitted by the assemblyline itself upon entering
	 * execution of a script.
	 * 
	 * @return The current/last know script context
	 */
	public ScriptData getCurrentScriptData() {
		return currentScriptData;
	}

	/**
	 * Returns the last command sent via the stepper thread (e.g. STEP,
	 * STEP_INTO etc).
	 * 
	 * @return
	 */
	public int getLastCommand() {
		return sessionThread.getLastCommand();
	}

	/**
	 * This class provides all details for a break event.
	 */
	public static class DebugBreak {
		private String breakpoint;
		private boolean script = false;
		private int linenumber = -1;
		private String component = null;
		private String hookOrAttribute = null;
		private ScriptData scriptData;

		public DebugBreak(String breakpoint, ScriptData scriptData) {
			this.breakpoint = breakpoint;
			this.scriptData = scriptData;
			int index = breakpoint.indexOf("#");
			if (index != -1) {
				component = breakpoint.substring(0, index);
				linenumber = Integer.parseInt(breakpoint.substring(index + 1));
				script = true;
			} else {
				component = breakpoint;
			}

			index = component.indexOf(".");
			if (index != -1) {
				hookOrAttribute = component.substring(index + 1);
				component = component.substring(0, index);
			}
		}

		/**
		 * Returns the fully qualified breakpoint (e.g.
		 * Component.hookOrAttr#scriptline)
		 * 
		 * @return
		 */
		public String getBreakpoint() {
			return breakpoint;
		}

		/**
		 * Returns the breakpoint name without the script line and hook/attr
		 * name.
		 * 
		 * @return
		 */
		public String getComponent() {
			return component;
		}

		/**
		 * Returns the hook or attribute part of the breakpoint
		 * 
		 * @return
		 */
		public String getHookOrAttribute() {
			return hookOrAttribute;
		}

		/**
		 * Returns true if the break is inside a script segment
		 * 
		 * @return
		 */
		public boolean isScript() {
			return script;
		}

		/**
		 * Returns the linenumber in the script where the break occurred. Only
		 * valid if isScript() is true:
		 * 
		 * @return
		 */
		public int getLinenumber() {
			return linenumber;
		}

		/**
		 * Returns the ScriptData where the debug break occurred. This is only
		 * valid if isScript() is true.
		 * 
		 * @return
		 */
		public ScriptData getScriptData() {
			return scriptData;
		}
	}

	/**
	 * Returns the current cycle number as it was when the EVAL command was sent.
	 * @return
	 */
	public long getCurrentCycle() {
		return currentCycleNumber;
	}

	/**
	 * Returns the last BREAK sent by the assemblyline
	 * @return
	 */
	public String getLastBreak() {
		return lastBreak;
	}

}
