/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// SkipEntryException.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class SkipEntryException extends NonFatalException {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public SkipEntryException(String msg) {
		super(msg);
	}
}
