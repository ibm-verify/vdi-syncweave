/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.debugger;

import com.ibm.di.entry.Entry;
import com.ibm.di.util.DebugServer;
import com.ibm.tdi.eclipse.debugger.DebugClient.DebugBreak;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.stepper.StepperThread;

public class DebugClientEvent {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private final static String[] COMMAND_NAMES = {
		DebugServer.ABORTED,
		DebugServer.BREAK,
		DebugServer.BREAKAT,
		DebugServer.BREAKPOINT,
		DebugServer.BREAKPOINTS,
		DebugServer.CONT,
		DebugServer.CONTROL,
		DebugServer.ENGINE_VARS,
		DebugServer.EVAL,
		DebugServer.FLAGS,
		DebugServer.HELLO,
		DebugServer.LOGMSG,
		DebugServer.QUIT,
		DebugServer.STATS,
		DebugServer.STEP,
		DebugServer.STEP_OVER,
		DebugServer.STOP,
		StepperThread.SS_ERROR,
		StepperThread.SS_CONNECT,
		StepperThread.SS_DISCONNECT,
		DebugServer.INIT,
		DebugServer.CONFIG,
		DebugServer.UNIQUE_ID,
		DebugServer.SCRIPT,
		"StateChange",
		"EvalMessage",
	};
	
	public final static int ABORTED = 0;
	public final static int BREAK = 1;
	public final static int BREAKAT = 2;
	public final static int BREAKPOINT = 3;
	public final static int BREAKPOINTS = 4;
	public final static int CONT = 5;
	public final static int CONTROL = 6;
	public final static int ENGINE_VARS = 7;
	public final static int EVAL = 8;
	public final static int FLAGS = 9;
	public final static int HELLO = 10;
	public final static int LOGMSG = 11;
	public final static int QUIT = 12;
	public final static int STATS = 13;
	public final static int STEP = 14;
	public final static int STEP_OVER = 15;
	public final static int STOP = 16;
	public final static int SS_ERROR = 17;
	public final static int SS_CONNECT = 18;
	public final static int SS_DISCONNECT = 19;
	public final static int INIT = 20;
	public final static int CONFIG = 21;
	public final static int UNIQUE_ID = 22;
	public static final int SCRIPT = 23;
	
	public static final int STATE_CHANGE = SCRIPT + 1;
	public static final int EVAL_MESSAGE = STATE_CHANGE + 1;
	
	private int command;

	private Object data;

	private String commandName;

	private String component;

	private String hook;

	private DebugClient client;

	private static boolean debug = false;

	public DebugClientEvent(int command, Object data, DebugClient client) {
		super();
		this.client = client;
		this.command = command;
		this.commandName = COMMAND_NAMES[command];
		setData(data);
		
		if(debug)
			EclipseAppender.loginfo(toString());
		
		if((getCommand() == BREAK) && (getData() instanceof String)) {
				String str = ""+getData();
				if(str.indexOf(".") != -1) {
					component = str.substring(0, str.indexOf("."));
					hook = str.substring(str.indexOf(".")+1);
				}
		}
	}
	
	public static String getCommandName(int command) {
		return COMMAND_NAMES[command];
	}

	public String getCommandName() {
		return commandName;
	}

	/**
	 * Returns the command sent by the assemblyline or debug client (e.g. BREAK, STOP, LOGMSG etc).
	 * 
	 * @return
	 */
	public int getCommand() {
		return command;
	}

	public void setCommandName(String command) {
		for(int i = 0; i < COMMAND_NAMES.length; i++) {
			if(COMMAND_NAMES[i].equals(command)) {
				this.commandName = command;
				this.command = i;
				return;
			}
		}
		this.command = -1;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DebugClientEvent{ " + client + "cmd=" + commandName + ", data=" + data + " }";
	}

	public String getHook() {
		return hook;
	}

	public String getComponent() {
		return component;
	}

	public static String eventName(int debugEvent) {
		if(debugEvent < COMMAND_NAMES.length)
			return COMMAND_NAMES[debugEvent];
		else
			return "" + debugEvent;
	}

	public String getEval() {
		if(data instanceof String)
			return (String)data;
		else if(data != null)
			return data.toString();
		else
			return "";
	}

	public Entry getEntry() {
		if(data instanceof Entry)
			return (Entry)data;
		else
			return null;
	}

	public DebugBreak getDebugBreak() {
		if(data instanceof DebugBreak)
			return (DebugBreak)data;
		else
			return null;
	}
	
	public Throwable getError() {
		if(data instanceof Throwable)
			return (Throwable) data;
		else
			return null;
	}

}
