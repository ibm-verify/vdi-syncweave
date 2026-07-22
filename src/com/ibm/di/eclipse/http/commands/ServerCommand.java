/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import com.ibm.di.server.RS;

public class ServerCommand extends RestCommand {
	
	public final static String SERVER_STOP = "stop";
	public final static String SERVER_LOG = "log";

	public void execute() throws Exception {
		String cmd = getPath(0);
		if(SERVER_STOP.equals(cmd))
			RS.gRS.shutdownServer(77);
	}

}
