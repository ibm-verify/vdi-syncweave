/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// UpdateNoChangesException.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class UpdateNoChangesException extends NonFatalException {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public UpdateNoChangesException(String msg) {
		super(msg);
	}
}
