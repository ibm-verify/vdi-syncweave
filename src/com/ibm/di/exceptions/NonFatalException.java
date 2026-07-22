/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// NonFatalException.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class NonFatalException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NonFatalException(String msg) {
		super(msg);
	}
}
