/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.debug;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.util.DebugServer;

public class StepperThread extends Thread {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String SS_ERROR = "stepper.error";
	public static final String SS_CONNECT = "stepper.connected";
	public static final String SS_DISCONNECT = "stepper.disconnected";

	private Socket socket;
	private ArrayList<StepperListener> listeners = new ArrayList<StepperListener>();
	private ServerSocket server;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private boolean alWaiting;
	private int lastCommand;

	private boolean shutdown;

	public StepperThread(String string) throws Exception {
		super(string);
		server = new ServerSocket(0, 1, InetAddress.getLocalHost());
	}

	public StepperThread(Socket socket) throws Exception {
		super(socket.toString());
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			if (socket == null) {
				while (!shutdown) {
					socket = server.accept();
					eventReceived(new StepperEvent(SS_CONNECT, socket, this, socket));
				}
			} else {
				os = new ObjectOutputStream(socket.getOutputStream());
				writeObject(os, com.ibm.di.util.DebugServer.HELLO);

				// System.out.println("-- waiting for remote al input");
				// System.out.flush();
				is = new ObjectInputStream(socket.getInputStream());

				boolean quitReceived = false;

				while (!quitReceived) {

					Object cmd = null;
					try {
						cmd = readObject();
					} catch (IOException eof) {
						break;
					}
					if (cmd.equals(DebugServer.QUIT)) {
						quitReceived = true;
					}

					Object obj = readObject();

					StepperEvent event = new StepperEvent(cmd.toString(), obj, this, socket);
					if (event.getCommand() == StepperEvent.BREAK)
						setAlWaiting(true);

					eventReceived(event);
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
			eventReceived(new StepperEvent(SS_ERROR, e, this, socket));

		} finally {
			eventReceived(new StepperEvent(SS_DISCONNECT, null, this, socket));

		}
	}

	private Object readObject() throws Exception {
		Object obj = is.readObject();
		if (obj instanceof byte[]) {
			ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) obj);
			obj = (new ObjectInputStream(bis)).readObject();
		}
		return obj;
	}

	/**
	 * This method is invoked to write an object to the remote side. The method
	 * is synchronized to prevent the debugger thread and the UI thread to send
	 * a message at the same time.
	 * 
	 * @param os
	 *            The output stream (e.g. connection stream)
	 * @param obj
	 *            The object to send
	 * @throws Exception
	 */
	public synchronized void writeObject(ObjectOutputStream os, Object obj) throws Exception {
		try {
			os.writeObject(obj);
		} catch (java.io.WriteAbortedException wae) {
			if (wae.toString().indexOf("java.io.NotSerializableException") > 0)
				os.writeObject(obj.toString());
		}
		os.flush();
	}

	private void eventReceived(StepperEvent event) {
		// Make a copy to avoid ConcurrentModification if one of the listeners
		// unregister when they get the Event
		List<StepperListener> copy = new ArrayList<StepperListener>(listeners);
		for (StepperListener l : copy)
			l.handleEvent(event);
	}

	public void addStepperListener(StepperListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeStepperListener(StepperListener listener) {
		listeners.remove(listener);
	}

	public void sendData(Object data) throws Exception {
		writeObject(os, data);
	}

	public int getLocalPort() {
		return server.getLocalPort();
	}

	public String getHostName() {
		return server.getInetAddress().getHostAddress();
	}

	public void sendCommand(int command) throws Exception {
		sendCommand(command, null);
	}

	public void sendCommand(int command, Object data) throws Exception {
		setLastCommand(command);
		sendData(StepperEvent.getCommandName(command));
		switch(command) {
		case StepperEvent.STEP:
		case StepperEvent.STEP_OVER:
		case StepperEvent.CONT:
		case StepperEvent.RUN_TO_CYCLE:
			setAlWaiting(false);
		}

		if(data != null)
			sendData(data);
	}

	public boolean isAlWaiting() {
		return alWaiting;
	}

	public void setAlWaiting(boolean alWaiting) {
		this.alWaiting = alWaiting;
	}

	public int getLastCommand() {
		return lastCommand;
	}

	public void setLastCommand(int lc) {
		this.lastCommand = lc;
	}

	public void shutdown() {
		shutdown = true;
		interrupt();
	}

	public void shutdown(boolean interrupt) {
		shutdown = true;
		if (interrupt)
			interrupt();
	}
}
