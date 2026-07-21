/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ScriptExitCode.java
//
//
//
package com.ibm.di.script;

public class ScriptExitCode extends Object {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static int SEC_EOF = 0;

	public final static int SEC_OK = 1;

	public final static int SEC_ERROR = 2;

	int status;

	String message;

	public void setStatus(int i) {
		status = i;
	}

	public void setMessage(String msg) {
		message = msg;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		String str = "[status=" + status + ", msg=" + message + "]";
		return str;
	}
}
