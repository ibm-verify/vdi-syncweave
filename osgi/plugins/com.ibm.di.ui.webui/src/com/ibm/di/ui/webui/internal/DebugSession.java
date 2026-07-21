/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DebugSession extends Thread {
	
	private ServerSocket serverSocket;
	private IOException error;

	public DebugSession() throws IOException {
		serverSocket = new ServerSocket();
	}
	
	public String getHostname() {
		return serverSocket.getLocalSocketAddress().toString();
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public void run() {
		try {
			Socket s = serverSocket.accept();
		} catch (IOException e) {
			this.error = e;
			return;
		}
	}

}
