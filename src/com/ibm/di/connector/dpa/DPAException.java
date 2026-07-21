/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DPAException extends Exception {

	private static final long serialVersionUID = -4704627574203769751L;
	
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param message
	 */
	public DPAException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param cause
	 */
	public DPAException(Exception cause) {
		super(cause);
	}		
	
}
