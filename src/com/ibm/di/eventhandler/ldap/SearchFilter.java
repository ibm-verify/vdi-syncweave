/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.eventhandler.ldap;

import com.ibm.di.connector.LDAPServerConnector;
import com.ibm.di.server.ResourceHash;

import java.util.*;
import java.nio.*;

public class SearchFilter {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static int AND = 0xA0;

	public final static int OR = 0xA1;

	public final static int NOT = 0xA2;

	public final static int EQUALS = 0xA3;

	public final static int SUBSTRING = 0xA4;

	public final static int GREATER = 0xA5;

	public final static int LESS = 0xA6;

	public final static int PRESENT = 0x87;// Strange one!!! Should be 0xA7 but
											// clients use 0x87???

	public final static int APPROXIMATE = 0xA8;

	public final static int EXTENSIBLE = 0xA9;

	public int tag;

	public String name;

	public Object value;

	public Vector children = new Vector();

	private final static ResourceHash sResHash = LDAPServerConnector
			.getResHash();

	public SearchFilter(int tag) {
		this.tag = tag;
	}

	public String tagString() {
		switch (tag) {
		case AND:
			return "and";
		case OR:
			return "or";
		case NOT:
			return "not";
		case EQUALS:
			return "equals";
		case SUBSTRING:
			return "substring";
		case GREATER:
			return "greater";
		case LESS:
			return "less";
		case PRESENT:
			return "present";
		case APPROXIMATE:
			return "approximate";
		case EXTENSIBLE:
			return "extensible";
		default:
			return "unknown";
		}
	}

	public int size() {
		return children.size();
	}

	public SearchFilter get(int index) {
		return (SearchFilter) children.get(index);
	}

	public void add(SearchFilter filter) {
		children.add(filter);
	}

	public String toString() {
		StringBuffer str = new StringBuffer();

		str.append("(");

		switch (tag) {
		case 0xA0:
			// And
			str.append("&");
			for (int i = 0; i < children.size(); i++)
				str.append(children.get(i).toString());
			break;

		case 0xA1:
			// Or
			str.append("|");
			for (int i = 0; i < children.size(); i++)
				str.append(children.get(i).toString());
			break;

		case 0xA2:
			// Not
			str.append("!");
			for (int i = 0; i < children.size(); i++)
				str.append(children.get(i).toString());
			break;

		case 0xA3:
			// Equality
			str.append(name);
			str.append("=");
			str.append(value.toString());
			break;

		case 0xA4:
			// Substring
			str.append(name);
			str.append("=");
			str.append(value.toString());
			break;

		case 0xA5:
			// Greater-than-equal
			str.append(name);
			str.append("=>");
			str.append(value.toString());
			break;

		case 0xA6:
			// Less-than-equal
			str.append(name);
			str.append("<=");
			str.append(value.toString());
			break;

		case 0xA7:
		case 0x87:
			// Present
			str.append(name);
			str.append("=*");
			break;

		case 0xA8:
			// Approximate
			str.append(name);
			str.append("~");
			str.append(value.toString());
			break;

		case 0xA9:
			// Extensible match
			str.append(name);
			break;

		default:
			str.append("Unknown filter code: " + Integer.toHexString(tag));
		}

		str.append(")");

		return str.toString();
	}

	public static SearchFilter parseFilter(ByteBuffer buf, String charset)
			throws Exception {

		/*
		 * Filter ::= CHOICE { and [0] SET OF Filter, 0xA0 or [1] SET OF Filter,
		 * 0xA1 not [2] Filter, 0xA2 equalityMatch [3] AttributeValueAssertion,
		 * 0xA3 substrings [4] SubstringFilter, 0xA4 greaterOrEqual [5]
		 * AttributeValueAssertion, 0xA5 lessOrEqual [6]
		 * AttributeValueAssertion, 0xA6 present [7] AttributeDescription, 0xA7
		 * approxMatch [8] AttributeValueAssertion, 0xA8 extensibleMatch [9]
		 * MatchingRuleAssertion } 0xA9
		 * 
		 * SubstringFilter ::= SEQUENCE { type AttributeDescription, -- at least
		 * one must be present substrings SEQUENCE OF CHOICE { initial [0]
		 * LDAPString, any [1] LDAPString, final [2] LDAPString } }
		 * 
		 * MatchingRuleAssertion ::= SEQUENCE { matchingRule [1] MatchingRuleId
		 * OPTIONAL, type [2] AttributeDescription OPTIONAL, matchValue [3]
		 * AssertionValue, dnAttributes [4] BOOLEAN DEFAULT FALSE }
		 * 
		 */

		Sequence seq = new Sequence(buf, true);
		ByteBuffer buffer = seq.buffer;

		SearchFilter filter = new SearchFilter(seq.tag);

		switch (seq.tag) {
		case 0xA0:
		case 0xA1:
		case 0xA2:
			// And, Or, Not
			while (buffer.hasRemaining())
				filter.add(parseFilter(buffer, charset));
			break;

		case 0xA3:
		case 0xA5:
		case 0xA6:
		case 0xA8:
			// Equality, Greater, Less, Approximate
			filter.name = BER.getString(buffer);
			filter.value = BER.getString(buffer, charset);
			break;

		case 0xA4:
			// Substring
			filter.name = BER.getString(buffer);
			Sequence subs = new Sequence(buffer, true);
			filter.value = BER.getFilterString(subs.buffer, charset);
			break;

		case 0xA7:
		case 0x87:
		case 0xA9:
			// Present, Extensible
			filter.name = BER.getString(buffer, -1);
			break;

		default:
			throw new Exception(sResHash.getString(
					"CONNECTOR.LDAPSERVER.SEARCHFILTER.UNKNOWN.FILTER.CODE",
					Integer.toHexString(seq.tag)));
		}

		return filter;
	}
}
