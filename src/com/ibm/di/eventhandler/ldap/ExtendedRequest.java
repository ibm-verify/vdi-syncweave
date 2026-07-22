/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import com.ibm.di.entry.*;

public class ExtendedRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ExtendedRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.EXTENDED_REQUEST, binattrs);
		entry.setAttribute("ldap.requestname", BER.getString(buffer, charset));
		entry.setAttribute("ldap.requestvalue", BER.getBytes(buffer));
	}

	public String getCommandString() {
		return "extended";
	}

	public int getResponseOp() {
		return LDAPMessage.EXTENDED_RESPONSE;
	}
}
