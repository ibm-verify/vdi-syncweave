/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eclipse.http.commands;

import java.util.ArrayList;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.local.AssemblyLineListener;
import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.entry.Entry;

public class LogCommand extends RestCommand implements AssemblyLineListener, DIEventListener {
	
	// Maximum number of messages cached 
	private static final int MAX_CACHE_SIZE = 100;
	private ArrayList<String> cachedMessages = new ArrayList<String>();
	private boolean terminated = false;

	public void execute() throws Exception {

		if("server".equalsIgnoreCase(getPath(0))) {
			getSession().addEventListener(this, "*", "*");
			logmsg("Server notifications: * *");
		}
		
		dumpCache();
		
		while(!isTerminated()) {
			Thread.sleep(100);
		}
		
		dumpCache();
		
		// Just to prevent caller to generate the body for us
		appendResult(HTTP_BODY, "** end **");
	}

	public void assemblyLineCycleDone(Entry entry) throws DIException {
		//logmsg("+" + entry + "\n");
	}

	public void assemblyLineFinished() throws DIException {
		logmsg("-\n");
		setTerminated(true);
	}

	public void handleEvent(DIEvent event) throws DIException {
		logmsg(event.toString());
	}

	public void messageLogged(String message) throws DIException {
		StringBuilder buf = new StringBuilder(" ");
		buf.append(message);
		if (! message.endsWith("\n"))
			buf.append("\n");
		logmsg(buf.toString());
	}
	
	public synchronized void logmsg(String msg) {
		try {
			if(getApi() == null) {
				if (cachedMessages.size() > MAX_CACHE_SIZE)
					cachedMessages.remove(0);
				if(msg != null)
					cachedMessages.add(msg);
			} else {
				dumpCache();
				Entry e = new Entry();
				e.setAttribute(HTTP_BODY, msg);
				getConnector().putEntry(e);
			}
		} catch (Exception e) {
			setTerminated(true);
		}
	}
	
	private void dumpCache() throws Exception {
		synchronized (cachedMessages) {
			Entry e = new Entry();
			while(cachedMessages.size() > 0) {
				e.setAttribute(HTTP_BODY, cachedMessages.remove(0));
				getConnector().putEntry(e);
			}
		}
	}

	public void setTerminated(boolean b) {
		this.terminated = b;
	}

	public boolean isTerminated() {
		return terminated;
	}

}
