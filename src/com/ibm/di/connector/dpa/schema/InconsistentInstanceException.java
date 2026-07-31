/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.schema;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class InconsistentInstanceException extends RuntimeException {

	private static final long serialVersionUID = -1097723906690697541L;

	/**
	 * 
	 * @param message
	 */
	public InconsistentInstanceException(String message) {
		super(message);
	}
	
}
