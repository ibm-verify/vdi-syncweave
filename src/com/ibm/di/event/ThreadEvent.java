/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ThreadEvent.java
//
//
//
package com.ibm.di.event;

import java.awt.event.*;

public class ThreadEvent extends ActionEvent {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String RT_LASTSTARTED = "%%LASTSTARTED%%";

	public final static String RT_LASTEXIT = "%%LASTEXIT%%";

	public final static String RT_LASTSTATUS = "%%LASTSTATUS%%";

	public final static int TE_UNKNOWN = 0;

	public final static int TE_ALSTART = 1;

	public final static int TE_PLSTART = 2;

	public final static int TE_ALSTOP = 3;

	public final static int TE_PLSTOP = 4;

	public final static int TE_LOGMSG = 5;

	public final static String[] TE_MAP = { "Undefined Event", "Running",
			"Running", "Stopped", "Stopped", "" };

	private int eventID;

	private String eventText;

	private Exception eventException;

	public ThreadEvent(Object source, int eventid, String text,
			Exception exception) {
		super(source, eventid, text);
		this.eventID = eventid;
		this.eventException = exception;
		this.eventText = text;
	}

	public int getThreadEvent() {
		return eventID;
	}

	public String getThreadEventText() {
		return TE_MAP[eventID];
	}

	public Exception getThreadException() {
		return eventException;
	}

	public String getThreadText() {
		return eventText;
	}

	public boolean terminated() {
		if (eventID < TE_ALSTOP)
			return false;
		else
			return true;
	}

	public boolean normalTermination() {
		if (eventException == null)
			return true;
		else
			return false;
	}

	public String toString() {
		String name = ((Thread) getSource()).getName();
		return name + ": " + TE_MAP[eventID] + ": " + eventText;
	}

}
