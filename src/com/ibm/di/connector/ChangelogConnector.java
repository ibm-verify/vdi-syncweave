/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Vector;

import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import com.ibm.di.entry.Entry;
import com.ibm.di.parser.LDIFParser;
import com.ibm.di.server.ResourceHash;

/**
 * ChangelogConnector class combines the similar changelog behavior of
 * IDSChangelogConnector, NetscapeChangelogConnector and z/OS Changelog
 * Connector classes.
 * 
 * It provides tree different ways to handle merging between original and
 * updated data.
 */
public class ChangelogConnector extends LDAPConnector {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "miserver";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * A flag that tells whether the {@link #parseEntry(Entry)} method should
	 * merge the parsed entry with the passed as parameter entry. <br>
	 * This flag is controlled by the the value of the {@link #PARAM_MERGE_MODE}
	 * parameter and set during the initialization process (
	 * {@link #initialize(Object)}).
	 */
	protected boolean defaultMerge = false;

	/**
	 * A flag that tells whether the {@link #parseEntry(Entry)} method should
	 * merge the operation of the parsed entry with the operation of the passed
	 * as parameter entry. <br>
	 * This flag is controlled by the the value of the {@link #PARAM_MERGE_MODE}
	 * parameter and set during the initialization process (
	 * {@link #initialize(Object)}).
	 */
	protected boolean onlyChanges = false;

	/**
	 * A flag that tells whether the {@link #parseEntry(Entry)} method should
	 * add the passed as parameter entry as an attribute of the parsed entry. <br>
	 * This flag is controlled by the the value of the {@link #PARAM_MERGE_MODE}
	 * parameter and set during the initialization process (
	 * {@link #initialize(Object)}).
	 */
	protected boolean bothSeparated = false;

	/**
	 * The {@link LDIFParser} object used for parsing the entries.
	 */
	protected LDIFParser mLdifParser;

	/**
	 * The parameter name used to control the way the {@link #parseEntry(Entry)}
	 * method merges the parsed entry with the provided as a parameter entry.
	 */
	public static final String PARAM_MERGE_MODE = "mergeMode";

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object aObject) throws Exception {
		super.initialize(aObject);

		mLdifParser = new LDIFParser();
		mLdifParser.initParser();

		String mergeMode = getParam(PARAM_MERGE_MODE);
		if (mergeMode != null && mergeMode.trim().length() > 0) {
			if (mergeMode.equals(ChangelogInterface.PARAM_MERGE_CHANGELOG_AND_DATA)) {
				defaultMerge = true;
			} else if (mergeMode.equals(ChangelogInterface.PARAM_MERGE_ONLY_CHANGED_DATA)) {
				onlyChanges = true;
			} else if (mergeMode.equals(ChangelogInterface.PARAM_MERGE_BOTH_NOT_MERGED)) {
				bothSeparated = true;
			} else {
				onlyChanges = true;
			}
		}
	}

	/**
	 * Method for parsing and merging Changelog Entry attributes and changed
	 * attributes from the actual Directory Entry.
	 * 
	 * @param aEntry
	 *            the actual Directory Entry.
	 * 
	 * @return the result of the parsing and merging operations entry.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected Entry parseEntry(Entry aEntry) throws Exception {
		String type = aEntry.getString("changetype");

		if (type == null) {
			throw new Exception(sResHash.getString("CHLCONNECTOR.ENTRY.DOES.NOT.CONTAIN.A.CHANGETYPE", aEntry));
		}

		if (type.equalsIgnoreCase("delete")) {
			aEntry.setOp(Entry.OP_DEL);
			return aEntry;
		} else if (type.equalsIgnoreCase("add")) {
			aEntry.setOp(Entry.OP_ADD);
		} else if (type.equalsIgnoreCase("modify") || type.equalsIgnoreCase("modrdn")) {
			aEntry.setOp(Entry.OP_MOD);
		}

		// Delete operation and IDS modrdn operation have no changes
		String newDN = aEntry.getString("newrdn");
		String str = aEntry.getString("changes");

		if (newDN != null) {
			StringBuilder sb = new StringBuilder();
			// modrdn treated as modify
			if (str != null) {
				sb.append(str);
				if (!str.startsWith("newrdn:")) {
					// For Netscape modrdn operation append newrdn: <value> to
					// 'changes' attribute
					String newDNString = "newrdn: " + newDN + "\n-\n" + (char) 0;
					sb.replace(sb.length() - 1, sb.length(), newDNString);
				}
			} else {
				sb.append("newrdn: ");
				sb.append(newDN);
				sb.append("\n\n");
			}
			str = sb.toString();
			aEntry.setAttribute("changes", str);
			aEntry.removeAttribute("newrdn");
		} else if (str == null) {
			throw new Exception(sResHash.getString("CHLCONNECTOR.ENTRY.DOES.NOT.CONTAIN.A.VALID.CHANGELOG.ENTRY", type));
		}

		if (mLdifParser == null) {
			mLdifParser = new LDIFParser();
			mLdifParser.setContext(this);
			mLdifParser.setDebug(debugMode());
		}
		mLdifParser.initParser();
		mLdifParser.setInputStream(str);
		Entry p = mLdifParser.readEntry();
		if (p == null) {
			if (debugMode()) {
				debug(sResHash.getString("CHLCONNECTOR.UNABLE.TO.PARSE.CHANGES.ATTRIBUTE"));
				debug("changes: '" + str + "'");
			}
			throw new Exception(sResHash.getString("CHLCONNECTOR.UNABLE.TO.PARSE.CHANGES.ATTRIBUTE"));
		}

		if (defaultMerge) {
			aEntry.merge(p);
		} else if (bothSeparated) {
			p.addAttributeValue("changelog", aEntry);
			p.setOp(aEntry.getOp());
			return p;
		} else if (onlyChanges) {
			p.setOp(aEntry.getOp());
			return p;
		}
		return aEntry;
	}

/**
	 * Query the schema of the LDAP server. If 'Start at' parameter is EOD
	 * return schema depending on the Merge mode parameter: <br>
	 * <li>Merge changelog and changed data - return all 'MAY' and 'MUST'
	 * attributes defined for 'changeLogEntry' LDAP object class</li><br>
	 * <li>Return only changed data - we have no idea what are the changed
	 * attributes of the changed entry so return <code>null</code></li><br>
	 * <li>Return both - return the 'changelog' attribute</li><br>
	 * If 'Start at' parameter is set to a number the LDAP Connector querySchema
	 * is called.
	 * 
	 * @param source
	 *            A distinguished name
	 * 
	 * @return The schema
	 * @exception Exception
	 *                Any Exception thrown by underlying libraries
	 */
	@Override
	public Object querySchema(Object source) throws Exception {
		Vector<Entry> result = new Vector<Entry>();

		if (defaultMerge) {
			DirContext dc = getLdapContext().getSchema("");
			DirContext schema = (DirContext) dc.lookup("ClassDefinition/changeLogEntry");
			if (schema == null) {
				return result;
			}
			add2schema(schema.getAttributes(""), "MUST", result, dc);
			add2schema(schema.getAttributes(""), "MAY", result, dc);
		} else if (bothSeparated) {
			Entry e = new Entry();
			e.setAttribute("name", "changelog");
			e.setAttribute("syntax", "MUST/" + e.getClass().toString());
			result.add(e);
		}
		return result;
	}

	private void add2schema(Attributes attr, String must, Vector<Entry> result, DirContext schema) {
		Entry e = null;
		String mAttr = attr.get(must).toString();
		mAttr = mAttr.replace(" ", "").substring(mAttr.indexOf(":") + 1);
		String[] names = mAttr.split(",");

		for (String attrname : names) {
			e = new Entry();
			e.setAttribute("name", attrname);
			e.setAttribute("syntax", must + "/" + getAttributeSyntax(schema, attrname));
			result.add(e);
		}
	}

	/**
	 * Version information.
	 * 
	 * @return the version information.
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

}
