/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

/**
 * Enum class for the states we transition to during an RFC call.
 * 
 */
final class SendReceiveState implements Comparable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static int newState;

	private final int state;

	/* initialise states */
	private SendReceiveState() {
		this.state = newState++;
	}

	static final SendReceiveState INIT = new SendReceiveState();

	static final SendReceiveState IMPORT = new SendReceiveState();

	static final SendReceiveState EXECUTE = new SendReceiveState();

	static final SendReceiveState END = new SendReceiveState();

	public int compareTo(Object o) {
		return this.state - ((SendReceiveState) o).state;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		boolean result = false;
		
		if (obj instanceof SendReceiveState) {
			result = (compareTo(obj) == 0);
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return state;
	}
}
