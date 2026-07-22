/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.debugger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ui.PlatformUI;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.remote.impl.AssemblyLineListenerBase;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugMessage;
import com.ibm.di.util.DebugServer;
import com.ibm.di.util.NullValue;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.editors.RunRemoteAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.server.RMILogger;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.stepper.StepperEvent;
import com.ibm.tdi.eclipse.stepper.StepperListener;
import com.ibm.tdi.eclipse.stepper.StepperThread;

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

	private boolean debug = Boolean.getBoolean("com.ibm.tdi.eclipse.debug");

	private static final String JAVASCRIPT_VARS = "ScriptEngine";
	/*
	 * The connection object we use to start/stop assemblylines
	 */
	private RMIServerAPI api;

	/*
	 * The config identifier and the config instance we either started or got
	 * provided. In the latter case we attach to an existing config
	 * instance/assemblyline rather than starting them.
	 */
	private String configID;
	private ConfigInstance configInstance;

	/*
	 * The state of the debug session
	 */
	protected DebugState debugState = new DebugState();

	/*
	 * The remote handle to the assemblyline we started or was provided.
	 */
	private com.ibm.di.api.remote.AssemblyLine assemblyLineHandle;

	/**
	 * The listener we have attached the the AssemblyLine
	 */
	private AssemblyLineListenerBase alListener;
	
	/*
	 * The remote handle to the Sequence we started
	 */
	private com.ibm.di.api.remote.Sequence sequenceHandle;

	/**
	 * The session we have going with the AssemblyLine
	 */
	private StepperThread sessionThread = null;

	/**
	 * Input used to construct temporary configs etc. The assemblyline to run
	 * with any options.
	 */
	private RunAssemblyLineInput input;

	/**
	 * This is the assemblyline name provided by the assemblyline in a debug
	 * message
	 */
	private String assemblylineName;

	/**
	 * This is the assemblyline config provided by the assemblyline in a debug
	 * message
	 */
	private AssemblyLineConfig assemblylineConfig;

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
	 * Constructor used to start a new config instance and assemblyline
	 * 
	 * @param api
	 * @param alc
	 */
	public DebugClient(RMIServerAPI api, RunAssemblyLineInput input) {
		this.api = api;
		this.input = input;
	}

	/**
	 * Constructor used to attach to an already running assemblyline.
	 * 
	 * @param api
	 * @param input
	 */
	public DebugClient(RMIServerAPI api, RunRemoteAssemblyLineInput input) {
		this.api = api;
		this.input = input;
	}

	//
	// ----- START/STOP/CONTINUE Config/AL instances
	//

	/**
	 * Starts the temporary config instance used to run the assemblyline.
	 * 
	 * @throws Exception
	 */
	public void startConfigInstance() throws Exception {
		api.startConfigInstance(configID);
	}

	/**
	 * Stops the config instance started by startConfigInstance().
	 * 
	 * @throws Exception
	 */
	public void stopConfigInstance() {
		if (configInstance != null) {
			try {
				configInstance.stop(true);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		configInstance = null;
	}

	/**
	 * Returns the config instance object. If start is true an instance is
	 * started first.
	 * 
	 * @param start
	 * @return Config instance or null if instance is not running
	 * @throws Exception
	 */
	public ConfigInstance getConfigInstance(boolean start) throws Exception {
		ConfigInstance ci = null;
		if (getSession() != null) {
			ci = getSession().getConfigInstance(configID);
			if (ci == null && start)
				ci = getSession().startConfigInstance(configID);
		}
		return ci;
	}

	/**
	 * Returns the assemblyline's configuration object
	 * 
	 * @return
	 */
	public AssemblyLineConfig getAssemblylineConfig() {
		return assemblylineConfig;
	}

	public RMILogger startAssemblyLine() throws Exception {
		return startAssemblyLine(null, 0);
	}

	/**
	 * Starts the AssemblyLine.
	 * @return
	 * @throws Exception
	 */
	public RMILogger startAssemblyLine(BufferedWriter logWriter, int maxBufferLines) throws Exception {

		// -- Can only do when terminated
		if (!isIdle())
			throw new IllegalStateException("startAssemblyLine: " + getState());

		debugState = new DebugState();

		//
		// -- Get the config and serialize to byte stream
		//
		MetamergeConfigXML mx = input.getMetamergeConfig();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mx.commitChangesNoEncryption(bos);

		//
		// -- Generate a unique config id based on project name or al name
		// (ProjectName(<n>) or ALName(<n>))
		//
		List<String> usedIDs = getSession().getConfigInstancesIDs();
		for (int i = 0; i < 100; i++) {
			String id = input.getProject() != null ? input.getProject().getName() : input.getName();
			if (i > 0)
				id += "(" + i + ")";
			if (! usedIDs.contains(id)) {
				configID = id;
				break;
			}
		}

		//
		// -- Start the temporary config instance
		//
		configInstance = getSession().startTempConfigInstance(new String(bos.toByteArray(), "UTF-8"), true, configID, null);

		//
		// -- Populate the TCB
		//
		TaskCallBlock tcb = new TaskCallBlock();

		// -- Operation
		String op = input.getOperation();
		if (op != null && op.length() > 0)
			tcb.setALOperation(op);

		// -- Initial work entry
		if (input.getWorkEntry() != null)
			tcb.setInitialWorkEntry(input.getWorkEntry());

		// Provide init params?
		if (input.getInitParams() != null)
			tcb.setOperationInitParams(input.getInitParams());

		// -- Simulate flag
		tcb.setProperty(AssemblyLine.TCB_SIMULATE_MODE, "" + input.isSimulateMode()); //$NON-NLS-1$

		//Regression input
		String s = input.getRegressionInputName();
		if (s != null)
			tcb.setRegressionInputName(s);
		
		//Regression output
		s = input.getRegressionOutputName();
		if (s != null)
			tcb.setRegressionOutputName(s);
		
		// -- Set debug properties
		if (input.isDebug()) {
			//
			// -- Launch a thread to accept the first connection from the
			// assemblyline debugger
			// -- and shutdown the serverConnection object afterwards.
			//
			final StepperThread serverConnection = new StepperThread(input.getConfig().getShortName());
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
							EclipseAppender.logerror(e1.toString(), e1);
							fireDebugEvent(StepperEvent.BREAK, e1);
						}
						break;
					}
				}
			});
			serverConnection.start();

			//
			// -- Pass on the host:port to our serverConnection thread to the
			// assemblyline
			//
			tcb.setProperty(AssemblyLine.TCB_DEBUG_HOST, serverConnection.getHostName());
			tcb.setProperty(AssemblyLine.TCB_DEBUG_PORT, serverConnection.getLocalPort());
			tcb.setProperty(AssemblyLine.TCB_DEBUG_ONERROR, false);
		} else {
			tcb.setRunMode(AssemblyLine.RUNMODE_NODEBUG);
		}

		// -- Record/Playback Mode?
		switch (input.getStepMode()) {
		case RunAssemblyLineInput.RUNMODE_PLAYBACK:
			tcb.setRunMode(AssemblyLine.RUNMODE_PLAYBACK);
			break;
		case RunAssemblyLineInput.RUNMODE_RECORD:
			tcb.setRunMode(AssemblyLine.RUNMODE_RECORD);
			break;
		}

		RMILogger logger = new RMILogger() {
			public void assemblyLineCycleDone(Entry entry) throws DIException, RemoteException {
				debugState.incrementCycleCounter();
				super.assemblyLineCycleDone(entry);
			}

			public void assemblyLineFinished() throws DIException, RemoteException {
				fireDebugEvent(StepperEvent.SS_DISCONNECT, this);
				super.assemblyLineFinished();
				if (assemblyLineHandle != null && alListener != null)
					assemblyLineHandle.removeListener(alListener);
				alListener = null;
				assemblyLineHandle = null;
			}

			public void messageLogged(String msg)  {
				fireDebugEvent(StepperEvent.LOGMSG, msg);
				super.messageLogged(msg);
			}
		};

		if (maxBufferLines > 0)
			logger.setMaxLinesToBuffer(maxBufferLines);
		if (logWriter != null)
			logger.setLogWriter(logWriter);

		//
		// -- Create the listener object and only retrieve end-of-cycle work
		// -- entry if requested
		//
		alListener = AssemblyLineListenerBase.createInstance(logger, api.isSsl());
		
		//
		// -- If we are not debugging then we have to change state now to avoid loosing log messages in idle state
		//
		if(!input.isDebug())
			setState(DebugState.STATE_RUNNING);

		if (input.isSequence()) {
			sequenceHandle = configInstance.startSequence(input.getName(), tcb, alListener);
		} else {
			assemblyLineHandle = configInstance.startAssemblyLine(input.getConfig().getShortName(), tcb, alListener, true, false, input
					.isCollectingWork());

			// -- wait at most 10 seconds before giving up
			if(input.isDebug()) {
				long timeout = 10 * 1000;
				while (!debugState.isConnected()) {
					Thread.sleep(200);
					timeout -= 200;
					if (timeout < 0)
						break;
				}

				if (!debugState.isConnected())
					throw new Exception("Debugger session timed out");
			}
		}
		return logger;
	}

	/**
	 * Attaches to a running assemblyline and returns the logger object for the assemblyline.
	 * 
	 * @return
	 * @throws Exception
	 */
	public RMILogger attachAssemblyLine(boolean debug) throws Exception {
		
		if(!(input instanceof RunRemoteAssemblyLineInput))
			throw new IllegalStateException("attachAssemblyLine: input is not remote");
		
		RMILogger logger = new RMILogger() {
			public void assemblyLineCycleDone(Entry entry) throws DIException, RemoteException {
				debugState.incrementCycleCounter();
			}

			public void assemblyLineFinished() throws DIException, RemoteException {
				fireDebugEvent(StepperEvent.SS_DISCONNECT, this);
				if (assemblyLineHandle != null && alListener != null)
					assemblyLineHandle.removeListener(alListener);
				alListener = null;
				super.assemblyLineFinished();
			}

			public void messageLogged(String msg)  {
				fireDebugEvent(StepperEvent.LOGMSG, msg);
				super.messageLogged(msg);
			}
		};

		//
		// -- Create the listener object and only retrieve end-of-cycle work
		// -- entry if requested
		//
		alListener = AssemblyLineListenerBase.createInstance(logger, api.isSsl());
		
		RunRemoteAssemblyLineInput inp = (RunRemoteAssemblyLineInput) input;
		ConfigInstance ci = api.getSession().getConfigInstance(inp.getCid());
		if(ci == null)
			return null;
		
		for(com.ibm.di.api.remote.AssemblyLine al : ci.getAssemblyLines()) {
			if(al.getUniqueCode() == Integer.parseInt(inp.getAlid())) {
				assemblyLineHandle = al;
				assemblyLineHandle.addListener(alListener, true, false);
			}
		}
		
		debugState = new DebugState();
		debugState.setState(DebugState.STATE_RUNNING);
		
		// Create a debug session with the remote AL
		if(debug) {
			final StepperThread serverConnection = new StepperThread(input.getConfig().getShortName());
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
							EclipseAppender.logerror(e1.toString(), e1);
							fireDebugEvent(StepperEvent.BREAK, e1);
						}
						break;
					}
				}
			});
			serverConnection.start();
			assemblyLineHandle.attachDebugger(serverConnection.getLocalPort(), serverConnection.getHostName(), false);
		}
		
		return logger;
	}
	
	/**
	 * Attaches a logger to the specified assemblyline.
	 * 
	 * @param cid
	 * @param alid
	 * @return
	 * @throws Exception
	 */
	public RMILogger attachAssemblyLineLog(String cid, String alid) throws Exception {
		ConfigInstance ci = api.getSession().getConfigInstance(cid);
		if (ci == null)
			return null;

		assemblyLineHandle = ci.getAssemblyLineByUniqueCode(Integer.parseInt(alid));
		if (assemblyLineHandle == null)
			return null;

		RMILogger logger = new RMILogger() {
			public void assemblyLineCycleDone(Entry entry) throws DIException, RemoteException {
				debugState.incrementCycleCounter();
			}

			public void assemblyLineFinished() throws DIException, RemoteException {
				fireDebugEvent(StepperEvent.SS_DISCONNECT, this);
				if (assemblyLineHandle != null && alListener != null)
					assemblyLineHandle.removeListener(alListener);
				alListener = null;
				super.assemblyLineFinished();
			}

			public void messageLogged(String msg)  {
				fireDebugEvent(StepperEvent.LOGMSG, msg);
				super.messageLogged(msg);
			}
		};

		alListener = AssemblyLineListenerBase.createInstance(logger, api.isSsl());
		assemblyLineHandle.addListener(alListener, true, false);
		return logger;
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
			sessionThread.addStepperListener(new StepperListener() {
				public void handleEvent(StepperEvent event) {
					(DebugClient.this).handleEvent(event);
				}
			});
			setState(DebugState.STATE_RUNNING);
			sessionThread.start();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
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
				assemblylineName = (String) msg.getProp("name");
				Hashtable<String, Object> env = new Hashtable<String, Object>();
				env.put(MetamergeConfigFactory.MC_URL, msg.getDefault());
				try {
					MetamergeConfigXML mx = new MetamergeConfigXML(env);
					assemblylineConfig = mx.getAssemblyLine(assemblylineName);
					fireDebugEvent(StepperEvent.CONFIG, assemblylineConfig);
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
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
			int lc = sessionThread.getLastCommand();

			if (!(event.getData() instanceof Throwable))
				debugBreak = createDebugBreak(event.getData());
			currentScriptData = null;

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
			
		case StepperEvent.HELLO:
			fireDebugEvent(DebugClientEvent.HELLO, event.getData());
			break;
		}
	}

	/**
	 * Sends a stop signal via the debugger (debugging) or via the assemblyline
	 * handle. Can only send this when the assemblyline is Running or Waiting.
	 * 
	 * @throws Exception
	 *             , IllegalStateException
	 */
	public void stopAssemblyLine() throws Exception {
		if (input.isDebug()) {
			if (isIdle() || isPending())
				throw new IllegalStateException(debugState.toString());
			if (sessionThread != null)
				sessionThread.sendCommand(StepperEvent.STOP);
		} else if (assemblyLineHandle != null) {
			assemblyLineHandle.stop();
		} else if (sequenceHandle != null) {
			sequenceHandle.stop(true);
		}
	}

	/**
	 * Tells the remote AL that the debug session is over
	 */
	public void stopDebugging() {
		if (!input.isDebug() || isIdle() || sessionThread == null)
			return;
		try {
			sessionThread.sendCommand(StepperEvent.QUIT);
		} catch (Exception ignore) {
			SystemFunctions.doNothing();
		}
	}
	
	/**
	 * Returns the Server API assemblyline handle for the current assemblyline
	 * 
	 * @return assemblyline handle
	 */
	public com.ibm.di.api.remote.AssemblyLine getAssemblyLineHandle() {
		return assemblyLineHandle;
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
		if (input.isDebug() && isRunning()) {
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
		if (input.isDebug() && isWaiting()) {
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
		if (input.isDebug() && isWaiting()) {
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
		if (input.isDebug() && isWaiting()) {
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
		if (input.isDebug() && isWaiting()) {
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
		if (input.isDebug() && isWaiting()) {
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

	/**
	 * Returns the Server API Session object.
	 * 
	 * @return Server API session object
	 * @throws Exception
	 */
	private Session getSession() throws Exception {
		if (api != null)
			return api.getSession();
		else
			return null;
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
					EclipseAppender.logerror(e.toString(), e);
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
					EclipseAppender.logerror(e.toString(), e);
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
			EclipseAppender.logerror(e.toString(), e);
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

		@Override
		public String toString() {
			String s = script;
			if (s.length() > 23)
				s = s.substring(0, 20) + "...";
			return "iref=" + iref + ", sourceRef=" + sourceRef + ". script=" + s;
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

}
