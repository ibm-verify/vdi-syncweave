/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/*
 CompareRequest ::= [APPLICATION 14] SEQUENCE {
 entry           LDAPDN,
 ava             AttributeValueAssertion }
 */

package com.ibm.di.eventhandler.ldap;

import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import com.ibm.di.entry.*;

public class CompareRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public CompareRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.COMPARE_REQUEST, binattrs);
		entry.setAttribute("ldap.dn", BER.getString(buffer, charset));

		ByteBuffer ava = Sequence.getBuffer(buffer, true);
		String name = BER.getString(ava);
		entry.setAttribute("ldap.compareattribute", name);
		if (isBinary(name))
			entry.setAttribute("ldap.comparevalue", BER.getBytes(ava));
		else
			entry
					.setAttribute("ldap.comparevalue", BER.getString(ava,
							charset));

	}

	public String getCommandString() {
		return "compare";
	}

	public int getResponseOp() {
		return LDAPMessage.COMPARE_RESPONSE;
	}
}
