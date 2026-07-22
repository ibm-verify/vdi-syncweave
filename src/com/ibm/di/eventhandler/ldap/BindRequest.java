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

import com.ibm.di.connector.LDAPServerConnector;
import com.ibm.di.entry.*;
import com.ibm.di.server.ResourceHash;

public class BindRequest extends Request {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	final static byte SIMPLEBIND = (byte) 0x80;

	final static byte SASLBIND = (byte) 0x83;

	private final static ResourceHash sResHash = LDAPServerConnector
			.getResHash();

	public BindRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.BIND_REQUEST, binattrs);

		entry.setAttribute("ldap.version", Integer.valueOf(BER.getInteger(buffer)));
		entry.setAttribute("ldap.dn", BER.getString(buffer, charset));
		switch (buffer.get()) {
		case SIMPLEBIND:
			entry.setAttribute("ldap.bindmethod", "simple");
			entry.setAttribute("ldap.password", BER.getString(buffer, false));
			break;
		case SASLBIND:
			entry.setAttribute("ldap.bindmethod", "sasl");
			Sequence seq = new Sequence(buffer, true);
			entry.setAttribute("ldap.saslmechanism", BER.getString(seq.buffer,
					false));
			if (seq.buffer.hasRemaining())
				entry.setAttribute("ldap.saslcredentials", BER
						.getString(seq.buffer));
			break;
		default:
			throw new Exception(
					sResHash
							.getString("CONNECTOR.LDAPSERVER.BINDREQUEST.CAN.ONLY.DO.SIMPLE.BINDS"));
		}
	}

	public String getCommandString() {
		return "bind";
	}

	public int getResponseOp() {
		return LDAPMessage.BIND_RESPONSE;
	}
}
