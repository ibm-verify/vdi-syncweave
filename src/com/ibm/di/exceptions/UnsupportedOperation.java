/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// UnsupportedOperation.java
//
//
//
package com.ibm.di.exceptions;

import java.lang.Exception;

public class UnsupportedOperation extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public UnsupportedOperation(String msg) {
		super(msg);
	}
}
