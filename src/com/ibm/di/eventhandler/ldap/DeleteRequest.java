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

public class DeleteRequest extends Request {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public DeleteRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.DELETE_REQUEST, binattrs);
		entry.setAttribute("ldap.dn", BER.getString(buffer, buffer.limit(),
				charset));
	}

	public String getCommandString() {
		return "delete";
	}

	public int getResponseOp() {
		return LDAPMessage.DELETE_RESPONSE;
	}
}
