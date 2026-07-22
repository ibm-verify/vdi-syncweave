/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.Serializable;

/**
 * This class contains the main steps of the AssemblyLine execution.
 */
public class ALState implements Serializable {

	/**
	 * Unique ID used for serialization.
	 */
	static final long serialVersionUID = 669938312260868491L;

	/**
	 * Not initialized step.
	 */
	public final static int MS_NOT_INITIALIZED = -1;

	/**
	 * Debug initialization step.
	 */
	public final static int MS_DEBUG_INIT = 0;

	/**
	 * Start step.
	 */
	public final static int MS_START = 1;

	/**
	 * Load connectors step.
	 */
	public final static int MS_LOADCONN = 2;

	/**
	 * Prolog0 step.
	 */
	public final static int MS_PROLOG0 = 3;

	/**
	 * Initialize connectors step.
	 */
	public final static int MS_INITCONN = 4;

	/**
	 * Prolog step.
	 */
	public final static int MS_PROLOG = 5;

	/**
	 * Begin iteration step.
	 */
	public final static int MS_BEGINITER = 6;

	/**
	 * Next iteration step.
	 */
	public final static int MS_NEXTITER = 7;

	/**
	 * Next connector operation step.
	 */
	public final static int MS_NEXTCONN = 8;

	/**
	 * End iteration cycle step.
	 */
	public final static int MS_ENDCYCLE = 9;

	/**
	 * End iteration step.
	 */
	public final static int MS_ENDITER = 10;

	/**
	 * Epilog step.
	 */
	public final static int MS_EPILOG = 11;

	/**
	 * Close connector step.
	 */
	public final static int MS_CLOSECONN = 12;

	/**
	 * Build Task Call Block step.
	 */
	public final static int MS_BUILDTCB = 13;

	/**
	 * Epilog2 step.
	 */
	public final static int MS_EPILOG2 = 14;

	/**
 	 * @deprecated
	 * Not used.
	 */
	public final static int MS_DEBUG_CLOSE = 15;

	/**
	 * Termination step.
	 */
	public final static int MS_TERMINATE = 15;

	/**
	 * Array of the main steps in the AssemblyLine.
	 */
	static final String[] MAIN_STEPS = { "Debug Init", "Start",
			"LoadConnectors", "Prolog0", "InitConnectors", "Prolog",
			"BeginIteration", "NextIteratorEntry", "NextConnectorOperation",
			"EndCycle", "EndIteration", "Epilog", "CloseConnectors",
			"BuildTCB", "Epilog2", "Terminate", "Completed" };

	/**
	 * Current MAIN Step
	 */
	public volatile int mainStep = MS_DEBUG_INIT;

	/**
	 * Next Connector index
	 */
	public int connectorIndex = 0;

	/**
	 * Current Iterator index
	 */
	public int iteratorIndex = 0;

	/**
	 * Current cycle count
	 */
	public long cycleCounter = 0;

	/**
	 * True if exiting "abnormally"
	 */
	public boolean bailout = false;

	/**
	 * Determines whether the next connector's index is set.
	 */
	private boolean hasSetNextConnector = false;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash res = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Returns a string containing the fields
	 * 
	 * @return contained fields
	 */
	public String toString() {
		return res.getString("MISERVER.ALSTATE.MAINSTEP", new Object[] {
				MAIN_STEPS[mainStep], Integer.valueOf(connectorIndex),
				Integer.valueOf(iteratorIndex), Long.valueOf(cycleCounter) });
	}

	/**
	 * Set the index that getNext() should return.
	 * 
	 * @param index
	 *            the index value
	 * @param force
	 *            if it is true, the value will be set even if it has already
	 *            been changed
	 */
	void setNext(int index, boolean force) {
		if (force || !hasSetNextConnector)
			connectorIndex = index;
		hasSetNextConnector = true;
	}

	/**
	 * Returns the next Connector index. Also increments the index, so that the
	 * next time we get the next connector. Also allows us to set the index
	 * without forcing.
	 * 
	 * @return the connector index
	 */
	int getNext() {
		hasSetNextConnector = false;
		return connectorIndex++;
	}
}
