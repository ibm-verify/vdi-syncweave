/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class CCMDBException extends Exception {
	
	private static final long serialVersionUID = -1;
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param message
	 */
	public CCMDBException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param cause
	 */
	public CCMDBException(Exception cause) {
		super(cause);
	}
	
}
