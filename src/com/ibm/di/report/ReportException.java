/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.report;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class ReportException extends Exception {

	private static final long serialVersionUID = -4009815387592637162L;

	/**
	 * 
	 * @param message
	 */
	public ReportException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param cause
	 */
	public ReportException(Exception cause) {
		super(cause);
	}
	
}
