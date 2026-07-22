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

public class AddRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public AddRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.ADD_REQUEST, binattrs);

		/*
		 * AddRequest ::= [APPLICATION 8] SEQUENCE { entry LDAPDN, attributes
		 * AttributeList }
		 * 
		 * 
		 * AttributeList ::= SEQUENCE OF SEQUENCE { type AttributeDescription,
		 * vals SET OF AttributeValue }
		 */

		entry.setAttribute("ldap.dn", BER.getString(buffer, charset));

		if (!buffer.hasRemaining())
			return;

		ByteBuffer attributeList = Sequence.getBuffer(buffer, true);
		Entry addentry = new Entry();

		// Convenience
		addentry.setAttribute("$dn", entry.getString("ldap.dn"));

		while (attributeList.hasRemaining()) {

			// Attribute name
			ByteBuffer b = Sequence.getBuffer(attributeList, true);
			Attribute attr = addentry.newAttribute(BER.getString(b));
			boolean binary = isBinary(attr.getName());

			// Attribute values
			ByteBuffer c = Sequence.getBuffer(b, true);
			while (c.hasRemaining()) {
				if (binary)
					attr.addValue(BER.getBytes(c));
				else
					attr.addValue(BER.getString(c, charset));
			}

		}

		entry.setAttribute("ldap.entry", addentry);
	}

	public String getCommandString() {
		return "add";
	}

	public int getResponseOp() {
		return LDAPMessage.ADD_RESPONSE;
	}
}
