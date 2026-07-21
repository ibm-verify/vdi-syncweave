/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// NoRealChanges.java
//
//
//
package com.ibm.di.exceptions;

public class NoChangesException extends Exception {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public NoChangesException(String reason) {
		super(reason);
	}
}
