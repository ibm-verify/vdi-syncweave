/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

/**
 * 
 * Enum type to support the request response type.
 */
final class RequestResponseType implements Comparable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static int newType;

	private final int type;

	/* initialise states */
	private RequestResponseType() {
		this.type = RequestResponseType.newType++;
	}

	/** Prepare to send RFC data. */
	static final RequestResponseType XMLSTRING = new RequestResponseType();

	/** Import the RFC function metadata. */
	static final RequestResponseType MVA = new RequestResponseType();

	/** Executing the RFC. */
	static final RequestResponseType DOMDOC = new RequestResponseType();

	public int compareTo(Object o) {
		return this.type - ((RequestResponseType) o).type;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		boolean result = false;
		
		if (obj instanceof RequestResponseType) {
			result = (compareTo(obj) == 0);
		}
		
		return result;
	}
	
	@Override
	public int hashCode() {
		return type;
	}

}
