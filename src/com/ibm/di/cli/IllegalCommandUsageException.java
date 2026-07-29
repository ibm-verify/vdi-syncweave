/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cli;

/**
 * Used to throw an illegal command usage exception.
 */
public class IllegalCommandUsageException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The error message.
	 */
	private String errorMessage = "";

	/**
	 * The option that was incorrect (if any).
	 */
	private String helpOption = null;

	/**
	 * Default empty constructor.
	 * 
	 */
	public IllegalCommandUsageException() {
	}

	public IllegalCommandUsageException(String errMessage) {
		this(errMessage, null);
	}

	public IllegalCommandUsageException(String errMessage, String hOption) {
		this.errorMessage = errMessage;
		this.helpOption = hOption;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getHelpOption() {
		return helpOption;
	}
}
