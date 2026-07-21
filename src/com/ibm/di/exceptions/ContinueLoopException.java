/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.exceptions;

public class ContinueLoopException extends Exception {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ContinueLoopException() {
		super();
	}

	public ContinueLoopException(String name) {
		super(name);
	}
}
