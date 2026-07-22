/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

import com.ibm.di.entry.*;

public class UnbindRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public UnbindRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.UNBIND_REQUEST, binattrs);
	}

	public String getCommandString() {
		return "unbind";
	}

	public int getResponseOp() {
		return 0;
	}
}
