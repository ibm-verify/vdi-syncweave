/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.util.*;

public abstract class Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected int protocolOp;

	private Vector binattrs;

	public abstract String getCommandString();

	public abstract int getResponseOp();

	public Request(int protocolOp, Vector binattrs) {
		this.protocolOp = protocolOp;
		this.binattrs = binattrs;
	}

	public int getProtocolOp() {
		return protocolOp;
	}

	public void log(Object obj) {
		// System.out.println("[" + getCommandString() + "] " + obj);
	}

	public boolean isBinary(String attribute) {
		if (binattrs != null)
			return binattrs.contains(attribute);
		else
			return false;
	}
}
