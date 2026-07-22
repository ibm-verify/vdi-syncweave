/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.provider;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DataProcessingException extends Exception {
	
	private static final long serialVersionUID = -1;
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param message
	 */
	public DataProcessingException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param cause
	 */
	public DataProcessingException(Exception cause) {
		super(cause);
	}
	
}
