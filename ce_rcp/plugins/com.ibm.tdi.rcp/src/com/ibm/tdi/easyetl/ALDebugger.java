/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.easyetl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.impl.AssemblyLineListenerBase;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.TaskCallBlock;
import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugMessage;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.stepper.StepperEvent;
import com.ibm.tdi.eclipse.stepper.StepperListener;
import com.ibm.tdi.eclipse.stepper.StepperThread;

/**
 * This class simplifies debugging assemblylines on a remote TDI server.
 */
public class ALDebugger {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Special breakpoint that occurs at the end-of-cycle.
	 */
	public static final String END_OF_CYCLE_BREAKPOINT = "%%end_of_cycle  - end_of_cycle%%";

	private StepperThread serverThread;

	private RMIServerAPI api;

	private ConfigInstance configInstance;

	private StepperThread sessionThread;

	protected Exception lastError;

	private AssemblyLineConfig alc;

	protected Entry currentEntry;

	protected int cycleCounter;

	private String configID;

	private long startTime;

	private long stopTime;

	private boolean waiting;
	
	private boolean connected = false;
	
	private boolean breakpointsSkippedWhenRunUntil = false;
	
	private String runUntil = null;

	/*
	 * Only fire event when an active breakpoint is reached
	 */
	private ArrayList<String> activeBreakpoints = new ArrayList<String>();

	/*
	 * Expressions we monitor on breakpoints and end-of-cycle conditions
	 */
	private ArrayList<String> watchList = new ArrayList<String>();

	/*
	 * Listeners
	 */
	private ArrayList<ALDebuggerEventListener> listeners = new ArrayList<ALDebuggerEventListener>();

	private com.ibm.di.api.remote.AssemblyLine assemblyLineHandle;

	public ALDebugger(RMIServerAPI api) throws Exception {
		this.api = api;
	}

	/**
	 * Adds the breakpoint to the list of active breakpoints.
	 * 
	 * @param breakpoint
	 */
	public void addBreakpoint(String breakpoint) {
		if (!activeBreakpoints.contains(breakpoint))
			activeBreakpoints.add(breakpoint);
	}

	/**
	 * Removes the breakpoint to the list of active breakpoints.
	 * 
	 * @param breakpoint
	 */
	public void removeBreakpoint(String breakpoint) {
		activeBreakpoints.remove(breakpoint);
	}

	/**
	 * Returns the current runUntil component name
	 * @return
	 */
	public String getRunUntil() {
		return runUntil;
	}

	/**
	 * Sets the runUntil component name
	 * @param runUntil
	 */
	public void setRunUntil(String runUntil) {
		this.runUntil = runUntil;
	}

	/**
	 * Clears all breakpoints.
	 */
	public void clearBreakpoints() {
		activeBreakpoints.clear();
	}

	/**
	 * Adds an expression to the watch list. The watch list expressions are
	 * updated on breakpoints and end-of-cycle.
	 * 
	 * @param expression
	 */
	public void addWatchExpression(String expression) {
		if (!watchList.contains(expression))
			watchList.add(expression);
	}

	/**
	 * Removes an expression from the watch list.
	 * 
	 * @param expression
	 */
	public void removeWatchExpression(String expression) {
		watchList.remove(expression);
	}

	/**
	 * Clears the watch list.
	 */
	public void clearWatchExpressions() {
		watchList.clear();
	}

	/**
	 * Adds the listener to receive notifications on breakpoint and expression
	 * change events.
	 * 
	 * @param listener
	 */
	public void addEventListener(ALDebuggerEventListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Removes the listener
	 * 
	 * @param listener
	 */
	public void removeEventListener(ALDebuggerEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes all event listeners
	 */
	public void clearEventListeners() {
		listeners.clear();
	}

	/**
	 * Returns the time the assemblyline was started
	 * 
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Returns the time the assemblyline stopped
	 * 
	 * @return
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * Returns true if the remote assemblyline is paused waiting for a continue.
	 */
	public boolean isWaiting() {
		return waiting;
	}

	/**
	 * Returns true if there is an active assemblyline (paused or running)
	 */
	public boolean isStarted() {
		return sessionThread != null && configInstance != null;
	}

	/**
	 * Returns the work entry as it was at end of cycle
	 */
	public Entry getEndOfCycleWorkEntry() {
		return currentEntry;
	}

	/**
	 * Sends a continue command to the remote debugger
	 * 
	 * @throws Exception
	 */
	public void runContinue() throws Exception {
		if(sessionThread != null) {
			sessionThread.sendCommand(StepperEvent.CONT);
		} else {
			throw new Exception("AssemblyLine has not been started");
		}		
	}
	
	/**
	 * Sends a STEP command to the running assemblyline to halt execution at the next available breakpoint.
	 * 
	 * @throws Exception
	 */
	public void pauseAssemblyLine() throws Exception {
		if (sessionThread != null) {
			sessionThread.sendCommand(StepperEvent.STEP);
		} else {
			throw new Exception("AssemblyLine has not been started");
		}		
	}
	
	/**
	 * Runs the assemblyline until the next active breakpoint is reached or
	 * until the <i>untilComponent</i> is reached.
	 * 
	 * @param untilComponent
	 * @throws Exception
	 */
	public void runAssemblyLine(String untilComponent) throws Exception {

		setRunUntil(untilComponent);
		
		// -- already running via Next button so just send a continue command
		if (sessionThread != null) {
			if (untilComponent != null) {
				sessionThread.sendCommand(StepperEvent.BREAKAT);
				sessionThread.sendData(untilComponent);
			}
			sessionThread.sendCommand(StepperEvent.CONT);
		} else {
			throw new Exception("AssemblyLine has not been started");
		}
	}

	/**
	 * Returns true if breakpoints are ignored when using runUntil
	 * 
	 * @return
	 */
	public boolean isBreakpointsSkippedWhenRunUntil() {
		return breakpointsSkippedWhenRunUntil;
	}

	/**
	 * Sets the flag whether breakpoints are ignored when runUntil has been specified
	 * @param breakpointsSkippedWhenRunUntil
	 */
	public void setBreakpointsSkippedWhenRunUntil(boolean breakpointsSkippedWhenRunUntil) {
		this.breakpointsSkippedWhenRunUntil = breakpointsSkippedWhenRunUntil;
	}

	/**
	 * Starts the assemblyline.
	 * 
	 * @param debug
	 *            If false the assemblyline is run until completion.
	 * @param untilComponent 
	 * 
	 * @return
	 * @throws Exception
	 */
	public void startAssemblyLine(AssemblyLineConfig alc, boolean debug, String untilComponent) throws Exception {

		if (sessionThread != null)
			terminate();

		// Verify that the server has not been stopped since we last ran an AL.
		try {
			api.ping();
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		
		this.alc = alc;
		setRunUntil(untilComponent);

		MetamergeConfigXML mx = new MetamergeConfigXML();
		mx.initializeConfig();
		mx.rebind(MetamergeConfig.DEFAULT_ASSEMBLYLINE_FOLDER + "/" + alc.getShortName(), alc);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mx.commitChanges(bos);

		configID = api.startTempConfig(UUID.randomUUID().toString(), new ByteArrayInputStream(bos.toByteArray()));
		configInstance = api.getSession().getConfigInstance(configID);

		TaskCallBlock tcb = new TaskCallBlock();

		if (debug) {
			createServerThread();
			tcb.setProperty(AssemblyLine.TCB_DEBUG_HOST, serverThread.getHostName());
			tcb.setProperty(AssemblyLine.TCB_DEBUG_PORT, serverThread.getLocalPort());
			tcb.setProperty(AssemblyLine.TCB_DEBUG_ONERROR, false);
		}

		AssemblyLineListener listener = new AssemblyLineListener() {
			public void assemblyLineCycleDone(Entry arg0) throws DIException, RemoteException {
				currentEntry = arg0;
				cycleCounter++;
				if (activeBreakpoints.contains(END_OF_CYCLE_BREAKPOINT)) {
					fireDebugEvent(StepperEvent.BREAK, END_OF_CYCLE_BREAKPOINT, null);
				}
			}

			public void assemblyLineFinished() throws DIException, RemoteException {
				sessionThread = null;
				fireDebugEvent(StepperEvent.SS_DISCONNECT, getALC().getShortName(), getALC());
			}

			public void messageLogged(String arg0) throws DIException, RemoteException {
				fireDebugEvent(StepperEvent.LOGMSG, null, arg0);
			}

		};

		lastError = null;

		AssemblyLineListenerBase alListener = AssemblyLineListenerBase.createInstance(listener, api.isSsl());

		startTime = System.currentTimeMillis();
		stopTime = 0;
		cycleCounter = 0;
		waiting = false;
		connected = false;
		assemblyLineHandle = configInstance.startAssemblyLine(alc.getShortName(), tcb, alListener, true);
		
		// -- wait at most 10 seconds before giving up
		long timeout = 10 * 1000;
		while(!connected && timeout > 0) {
			Thread.sleep(200);
			timeout -= 200;
		}
		
		if(!connected)
			throw new Exception("Debugger session timed out");
	}

	/**
	 * Returns the assemblyline handle for the started assemblyline.
	 * 
	 * @return
	 */
	public com.ibm.di.api.remote.AssemblyLine getAssemblyLineHandle() {
		return assemblyLineHandle;
	}

	/**
	 * Terminates the current assemblyline.
	 */
	public void terminate() {
		if (configInstance != null) {
			try {
				configInstance.stop();
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}
		if (sessionThread != null) {
			try {
				sessionThread.sendCommand(StepperEvent.STOP);
				sessionThread = null;
			} catch (Exception e) {
				sessionThread = null;
			}
		}
	}

	private void createServerThread() throws Exception {
		if (serverThread == null) {
			serverThread = new StepperThread(alc.getShortName());
			serverThread.addStepperListener(new StepperListener() {
				public void handleEvent(StepperEvent event) {
					switch (event.getCommand()) {
					case StepperEvent.SS_CONNECT:
						try {
							createSessionThread(event);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						break;

					case StepperEvent.SS_ERROR:
						((Throwable) event.getData()).printStackTrace();
						break;

					}
				}
			});
			serverThread.start();
		}
	}

	private void createSessionThread(StepperEvent event) throws Exception {
		if (sessionThread != null) {
			sessionThread.shutdown();
			sessionThread = null;
		}

		sessionThread = new StepperThread(event.getSocket());
		sessionThread.addStepperListener(new StepperListener() {

			public void handleEvent(StepperEvent event) {
				StepperThread st = event.getThread();
				String component = null;

				switch (event.getCommand()) {
				case StepperEvent.EVAL:
					if (event.getData() instanceof DebugMessage) {
						DebugMessage msg = (DebugMessage) event.getData();
						fireDebugEvent(StepperEvent.EVAL, (String) msg.getDefault(), msg.getProp("value"));
					}
					break;

				case StepperEvent.BREAK:
					waiting = false;
					if (event.getData() instanceof String) {
						component = event.getData().toString();
						
						// -- Transmit breakpoints when we reach init part
						if ("".equals(component) || " INIT ".equals(component)) {
							try {
								for (String str : activeBreakpoints) {
									Breakpoint bp = new Breakpoint(str, true, null);
									st.sendCommand(StepperEvent.BREAKPOINT);
									st.sendData(bp);
								}
							} catch (Exception e1) {
								fireDebugEvent(StepperEvent.SS_ERROR, null, e1);
							}
							connected = true;
							fireDebugEvent(StepperEvent.SS_CONNECT, null, null);
						}

						if (shouldBreakOn(component)) {
							
							// -- make sure we don't send a continue
							waiting = true;

							// -- only run through the watch list when we're taking a break
							for (String str : watchList) {
								try {
									st.sendCommand(StepperEvent.EVAL);
									st.sendData(str);
								} catch (Exception e) {
									fireDebugEvent(StepperEvent.SS_ERROR, null, e);
								}
							}

							// -- fire the break event
							fireDebugEvent(StepperEvent.BREAK, component, null);
						}
						
					} else if (event.getData() instanceof Exception) {
						// -- make sure we don't send a continue
						waiting = true;

						fireDebugEvent(StepperEvent.SS_ERROR, null, event.getData());
					}
					
					// -- if we're not interested in the break the keep going
					if (!isWaiting()) {
						try {
							st.sendCommand(StepperEvent.CONT);
						} catch (Exception e) {
							fireDebugEvent(StepperEvent.SS_ERROR, null, e);
						}
					}
					break;

				case StepperEvent.SS_DISCONNECT:
					if(serverThread != null)
						serverThread.shutdown();
					serverThread = null;
					if(sessionThread != null)
						sessionThread.shutdown();
					sessionThread = null;
					waiting = false;
					connected = false;
					try {
						api.stopConfigInstance(configID);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		});
		sessionThread.start();

	}

	/**
	 * Returns true if we should pause on a breakpoint received from the remote AL debugger. The method also resets the runUntil
	 * field if that is the component to break on.
	 * 
	 * @param component
	 * @return
	 */
	protected boolean shouldBreakOn(String component) {
		if(runUntil != null && runUntil.equals(component)) {
			runUntil = null;
			return true;
		} else if(runUntil != null && isBreakpointsSkippedWhenRunUntil()) {
			return false;
		} else {
			return activeBreakpoints.contains(component);
		}
	}

	protected void fireDebugEvent(int mode, String name, Object value) {
		
		if(mode == ALDebuggerEvent.BREAKPOINT && runUntil != null && isBreakpointsSkippedWhenRunUntil()) {
			return;
		}
		
		try {
			ALDebuggerEvent event = new ALDebuggerEvent(this, mode, name, value);
			for (ALDebuggerEventListener listener : new ArrayList<ALDebuggerEventListener>(listeners)) {
				listener.handleEvent(event);
			}
		} catch (Throwable t) {
			SystemFunctions.doNothing();
		}
	}
	
	/**
	 * Returns the AssemblyLineConfig used when this debugger was started
	 * 
	 * @return
	 */
	protected AssemblyLineConfig getALC() {
		return alc;
	}

	/*
	 * Interface for debug event listeners
	 */
	public static interface ALDebuggerEventListener {
		public void handleEvent(ALDebuggerEvent event);
	}

	/*
	 * Base class for debugger events
	 */
	public static class ALDebuggerEvent {
		/**
		 * The assemblyline debugger has connected
		 */
		public final static int STARTED = StepperEvent.SS_CONNECT;

		/**
		 * The assemblyline terminated - getName returns the assemblyline name
		 * and getValue returns the assemblyline configuration.
		 */
		public final static int TERMINATED = StepperEvent.SS_DISCONNECT;

		/**
		 * An expression evaluation result - getName()/getValue() returns the
		 * expression/value.
		 */
		public final static int EXPRESSION = StepperEvent.EVAL;

		/**
		 * A breakpoint has paused the assemblyline - getName() returns the
		 * breakpoint name.
		 */
		public final static int BREAKPOINT = StepperEvent.BREAK;

		/**
		 * An error occurred - getValue returns the Exception object.
		 */
		public final static int ERROR = StepperEvent.SS_ERROR;

		/**
		 * A log message was received - getValue returns message
		 */
		public final static int MESSAGE = StepperEvent.LOGMSG;

		private String name;
		private Object value;
		private int event;
		private ALDebugger debugger;

		public ALDebuggerEvent(ALDebugger debugger, int event, String name, Object value) {
			super();
			this.debugger = debugger;
			this.event = event;
			this.name = name;
			this.value = value;
		}

		/**
		 * Returns the ALDebugger object that generated the event
		 * 
		 * @return
		 */
		public ALDebugger getDebugger() {
			return debugger;
		}

		public void setEvent(int event) {
			this.event = event;
		}

		/**
		 * Returns the event code that generated this event.
		 * 
		 * @return
		 */
		public int getEvent() {
			return event;
		}

		/**
		 * Returns the name of the event that generated this event.
		 * 
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * Returns the value associated with the event.
		 * 
		 * @return
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * Returns the first part of the getName() if it contains dots in the value; otherwise, getName() is returned.
		 * @return
		 */
		public String getComponentName() {
			String str = getName();
			if(str.indexOf(".") != -1)
				str = str.substring(0, str.indexOf("."));
			return str;
		}
	}

}
