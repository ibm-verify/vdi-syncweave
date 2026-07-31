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
public class TypeNotFoundException extends CCMDBException {
	
	private static final long serialVersionUID = -1;
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param message
	 */
	public TypeNotFoundException(String message) {
		super(message);
	}
	
}
