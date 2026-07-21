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

public class ModifyDNRequest extends Request {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public ModifyDNRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {
		super(LDAPMessage.MODIFYDN_REQUEST, binattrs);

		entry.setAttribute("ldap.dn", BER.getString(buffer, charset));
		entry.setAttribute("ldap.rdn", BER.getString(buffer, charset));
		entry.setAttribute("ldap.deleteOldRDN", Boolean.valueOf(BER
				.getBoolean(buffer)));
		if (buffer.hasRemaining())
			entry.setAttribute("ldap.newSuperior", BER.getDN(buffer));
	}

	public String getCommandString() {
		return "modifyrdn";
	}

	public int getResponseOp() {
		return LDAPMessage.MODIFYDN_RESPONSE;
	}
}
