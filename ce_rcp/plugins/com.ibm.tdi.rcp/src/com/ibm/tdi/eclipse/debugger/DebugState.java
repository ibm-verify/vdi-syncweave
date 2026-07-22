/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.debugger;

import com.ibm.tdi.eclipse.debugger.DebugClient.DebugBreak;

public class DebugState {
	
	/**
	 * Idle - no assemblyline running
	 */
	public final static int STATE_IDLE = 0;
	
	/**
	 * The assemblyline is running and we are waiting for a break/error event
	 */
	public final static int STATE_RUNNING = 1;
	
	/**
	 * The assemblyline is running and we have sent a pause/stop signal to the assemblyline to generate a break event
	 */
	public final static int STATE_PENDING = 2;
	
	/**
	 * The assemblyline is waiting for a continue/step/stop command.
	 */
	public final static int STATE_WAITING = 3;

	private final static String STATES[] = new String[] {
		"Idle", "Running", "Pending", "Waiting"
	};
	
	private int previousState = -1;
	private int state;
	private long cycleCounter;
	private String currentLocation;
	private String prevLocation;
	private DebugBreak currentDebugBreak;
	private DebugBreak previousDebugBreak;
	
	public DebugState() {
		state = STATE_IDLE;
		cycleCounter = 0;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.previousState = this.state;
		this.state = state;
	}

	public int getPreviousState() {
		return previousState;
	}

	public long getCycleCounter() {
		return cycleCounter;
	}

	public void setCycleCounter(long cycleCounter) {
		this.cycleCounter = cycleCounter;
	}
	
	public void incrementCycleCounter() {
		cycleCounter++;
	}

	public boolean isConnected() {
		return state != STATE_IDLE;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		setPrevLocation(getCurrentLocation());
		this.currentLocation = currentLocation;
	}

	public String getPrevLocation() {
		return prevLocation;
	}

	public void setPrevLocation(String prevLocation) {
		this.prevLocation = prevLocation;
	}

	public String toString() {
		return "[state=" + STATES[getState()] + ", location=" + getCurrentLocation() + ", cycle=" + getCycleCounter() + "]";
	}

	public DebugBreak getCurrentDebugBreak() {
		return currentDebugBreak;
	}

	public void setCurrentDebugBreak(DebugBreak currentDebugBreak) {
		setPreviousDebugBreak(getCurrentDebugBreak());
		this.currentDebugBreak = currentDebugBreak;
	}

	public DebugBreak getPreviousDebugBreak() {
		return previousDebugBreak;
	}

	public void setPreviousDebugBreak(DebugBreak previousDebugBreak) {
		this.previousDebugBreak = previousDebugBreak;
	}
}
