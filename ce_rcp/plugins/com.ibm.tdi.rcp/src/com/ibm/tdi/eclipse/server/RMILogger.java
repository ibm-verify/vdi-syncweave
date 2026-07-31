/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.server;

import java.io.BufferedWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.api.remote.impl.AssemblyLineListenerBase;
import com.ibm.di.entry.Entry;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class RMILogger extends RestServerLogger implements AssemblyLineListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private ArrayList<String> buffer = new ArrayList<String>();

	public RMILogger() throws Exception {
	}

	private boolean finished = false;

	private AssemblyLineListenerBase listenerImpl;

	private AssemblyLineListener chain;

	private BufferedWriter logWriter;

	private int maxLinesToBuffer;

	public boolean isFinished() {
		return finished;
	}

	public void assemblyLineCycleDone(Entry aEntry) throws DIException, RemoteException {
		if(chain != null)
			chain.assemblyLineCycleDone(aEntry);
	}
	
	public void setCycleDoneListener(AssemblyLineListener chain) {
		this.chain = chain;
	}

	public void assemblyLineFinished() throws DIException, RemoteException {
		finished = true;
		synchronized (buffer) {
			buffer.notifyAll();
		}
	}

	public void messageLogged(String msg) {
		if (msg == null)
			return;
		while (msg.endsWith("\n")||msg.endsWith("\r")||msg.endsWith(" ")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			msg = msg.substring(0, msg.length()-1);

		if (logWriter != null) {
			try {
				logWriter.write(msg + "\n");
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
				logWriter = null;
			}
		}
		
		synchronized (buffer) {
			if (maxLinesToBuffer > 0 && buffer.size() > 2 * maxLinesToBuffer) {
				while (buffer.size() > maxLinesToBuffer)
					buffer.remove(0);
				buffer.set(0, "[...]");
			}
			buffer.add(msg);
			buffer.notifyAll();
		}
	}

	@Override
	public void close() {
		buffer.clear();
	}

	@Override
	public String getNextMessage() throws Exception {
		synchronized (buffer) {
			while (buffer.size() == 0 && !finished)
				buffer.wait();
			if (buffer.size() == 0)
				return null;
			else
				return buffer.remove(0);
		}
	}

	public void setProxy(AssemblyLineListenerBase listenerImpl) {
		this.listenerImpl = listenerImpl;
	}

	public AssemblyLineListenerBase getProxy() {
		return listenerImpl;
	}

	public void setLogWriter(BufferedWriter logWriter) {
		this.logWriter = logWriter;
	}

	public void setMaxLinesToBuffer(int value) {
		maxLinesToBuffer = value > 0 ? value : 10000;
	}
	
	
}
