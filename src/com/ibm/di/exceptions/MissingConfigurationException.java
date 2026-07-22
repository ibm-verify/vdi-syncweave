/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// MissingConfigurationException.java
//
//
//
package com.ibm.di.exceptions;

public class MissingConfigurationException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String missingParamName;

	public MissingConfigurationException(String msg, String param) {
		super(msg);
		this.missingParamName = param;
	}

	public String toString() {
		return super.toString() + " [parameter name = " + missingParamName
				+ "]";
	}
}
