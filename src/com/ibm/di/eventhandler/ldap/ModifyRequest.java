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

public class ModifyRequest extends Request {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String[] OPERS = { "add", "delete", "replace" };

	public ModifyRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {

		super(LDAPMessage.MODIFY_REQUEST, binattrs);

		/*
		 * ModifyRequest ::= [APPLICATION 6] SEQUENCE { object LDAPDN,
		 * modification SEQUENCE OF SEQUENCE { operation ENUMERATED { add (0),
		 * delete (1), replace (2) }, modification AttributeTypeAndValues } }
		 */

		entry.setAttribute("ldap.dn", BER.getString(buffer, charset));

		buffer = Sequence.getBuffer(buffer);

		Entry addentry = new Entry();
		while (buffer.hasRemaining()) {

			ByteBuffer b = Sequence.getBuffer(buffer);

			int oper = BER.getEnum(b);

			ByteBuffer c = Sequence.getBuffer(b);

			String name = BER.getString(c);
			Attribute attr = addentry.newAttribute(name);
			boolean binary = isBinary(name);

			ByteBuffer d = Sequence.getBuffer(c);

			attr.addValue(OPERS[oper]);
			while (d.hasRemaining()) {
				if (binary)
					attr.addValue(BER.getBytes(d));
				else
					attr.addValue(BER.getString(d, charset));
			}
		}
		entry.setAttribute("ldap.entry", addentry);

	}

	public String getCommandString() {
		return "modify";
	}

	public int getResponseOp() {
		return LDAPMessage.MODIFY_RESPONSE;
	}
}
