/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// PasswordException.java
//
//
//
package com.ibm.di.exceptions;

public class PasswordException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public PasswordException(String reason) {
		super(reason);
	}
}
