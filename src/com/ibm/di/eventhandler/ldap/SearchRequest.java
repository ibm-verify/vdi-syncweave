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

public class SearchRequest extends Request {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String[] SCOPE = { "base", "onelevel", "subtree" };

	private final static String[] ALIAS = { "never", "insearch", "inbase",
			"always" };

	public SearchRequest(ByteBuffer buffer, Entry entry, String charset,
			Vector binattrs) throws Exception {

		super(LDAPMessage.SEARCH_REQUEST, binattrs);

		/*
		 * SearchRequest ::= [APPLICATION 3] SEQUENCE { baseObject LDAPDN, scope
		 * ENUMERATED { baseObject (0), singleLevel (1), wholeSubtree (2) },
		 * derefAliases ENUMERATED { neverDerefAliases (0), derefInSearching
		 * (1), derefFindingBaseObj (2), derefAlways (3) }, sizeLimit INTEGER (0 ..
		 * maxInt), timeLimit INTEGER (0 .. maxInt), typesOnly BOOLEAN, filter
		 * Filter, attributes AttributeDescriptionList }
		 */

		entry.setAttribute("ldap.searchbase", BER.getString(buffer));
		entry.setAttribute("ldap.scope", SCOPE[BER.getEnum(buffer)]);
		entry.setAttribute("ldap.derefalias", ALIAS[BER.getEnum(buffer)]);
		entry.setAttribute("ldap.sizelimit",
				Integer.valueOf(BER.getInteger(buffer)));
		entry.setAttribute("ldap.timelimit",
				Integer.valueOf(BER.getInteger(buffer)));
		entry.setAttribute("ldap.typesonly",
				Boolean.valueOf(BER.getBoolean(buffer)));
		entry.setAttribute("ldap.searchfilter", SearchFilter.parseFilter(
				buffer, charset));

		// Get returnAttributes list
		ByteBuffer retAttrs = Sequence.getBuffer(buffer);
		Attribute attr = entry.newAttribute("ldap.attributes");
		while (retAttrs.hasRemaining()) {
			attr.addValue(BER.getString(retAttrs));
		}

	}

	public String getCommandString() {
		return "search";
	}

	public int getResponseOp() {
		return LDAPMessage.SEARCH_RESULT_DONE;
	}
}
