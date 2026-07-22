/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// DebugServer.java
//
//
//
package com.ibm.di.util;

public class DebugServer  {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

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

}
