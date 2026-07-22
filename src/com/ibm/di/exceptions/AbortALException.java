/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// AbortALException.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class AbortALException extends Exception {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public AbortALException(String msg) {
		super(msg);
	}
}
