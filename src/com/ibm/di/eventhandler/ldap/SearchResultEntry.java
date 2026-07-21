/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

/*
 * 
 * 
 * Wahl, et. al. Standards Track [Page 47]
 * 
 * RFC 2251 LDAPv3 December 1997
 * 
 * 
 * SearchResultEntry ::= [APPLICATION 4] SEQUENCE { objectName LDAPDN,
 * attributes PartialAttributeList }
 * 
 * PartialAttributeList ::= SEQUENCE OF SEQUENCE { type AttributeDescription,
 * vals SET OF AttributeValue }
 * 
 * SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
 * 
 * SearchResultDone ::= [APPLICATION 5] LDAPResult
 * 
 */
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;

import com.ibm.di.entry.*;

public class SearchResultEntry extends Sequence {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public SearchResultEntry(Entry entry, String charset) throws Exception {

		super(LDAPMessage.SEARCH_RESULT_ENTRY);

		String dn = entry.getString("$dn");
		if (dn == null)
			dn = "";

		buffer = BER.putString(buffer, dn, charset);

		Sequence seq = new Sequence(BER.SEQUENCE);

		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals("$dn"))
				continue;

			Sequence as = new Sequence(BER.SEQUENCE);
			Attribute attr = entry.getAttribute(names[i]);

			as.buffer = BER.putString(as.buffer, names[i], charset);

			Sequence values = new Sequence(BER.SET_OF);
			for (int j = 0; j < attr.size(); j++) {
				Object val = attr.getValue(j);
				if (val instanceof byte[]) {
					values.buffer = BER.putBytes(values.buffer, (byte[]) val);
				} else {
					values.buffer = BER.putString(values.buffer,
							val.toString(), charset);
				}
			}

			as.addBuffer(values);
			seq.addBuffer(as);
		}

		// Add to sequence
		addBuffer(seq);
	}
}
