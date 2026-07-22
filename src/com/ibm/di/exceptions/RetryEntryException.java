/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

import java.lang.Exception;

public class RetryEntryException extends NonFatalException {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public RetryEntryException(String msg) {
		super(msg);
	}
}
