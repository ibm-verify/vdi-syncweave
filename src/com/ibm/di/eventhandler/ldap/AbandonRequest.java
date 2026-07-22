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

public class AbandonRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public AbandonRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.ABANDON_REQUEST, binattrs);

		// Special decoding of the MessageID to abandon
		int messageID = 0;
		for (int i = 0; i < buffer.capacity(); i++) {
			messageID = messageID << 8;
			messageID += buffer.get() & 0xff;
		}

		entry.setAttribute("ldap.abandon", Integer.valueOf(messageID));
	}

	public String getCommandString() {
		return "abandon";
	}

	public int getResponseOp() {
		// no response to abandon requests
		return -1;
	}
}
