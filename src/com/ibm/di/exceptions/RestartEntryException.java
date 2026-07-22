/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// RestartEntryException.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class RestartEntryException extends NonFatalException {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public RestartEntryException(String msg) {
		super(msg);
	}
}
