/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

final class SapRfcBuilderException extends Exception {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public SapRfcBuilderException(String msg) {
		super(msg);
	}

	public SapRfcBuilderException(Throwable root) {
		super(root);
	}

	public SapRfcBuilderException(String msg, Throwable root) {
		super(msg, root);
	}
}
