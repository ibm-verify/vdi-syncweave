/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;

/**
 * The LDAPGroup connector extends the LDAPConnector to read entries from an LDAP directory. Each entry returned
 * is processed by the LDAPGroup connector in the following manner
 * 
 * <ul>
 * <li>If the entry has an attribute called uniquemember or member its member contents
 * is iterated by the LDAPGroup connector.
 * <li>If the entry is not a group entry it is ignored
 * <li>If the member attribute contains other group entries, the current group is completed before nested groups are iterated.
 * </ul>
 * 
 */
public class LDAPGroupConnector extends LDAPConnector {

	private static final String DN_ATTRIBUTE = "$dn";
	/*
	 * The connector param for returned attributes
	 */
	private static final String LDAP_RETURN_ATTRIBUTES = "ldapReturnAttributes";
	
	/*
	 * Number of values to read from ActiveDirectory pr batch read
	 */
	private static final int AD_MEMBER_BATCH_SIZE = 500;

	/*
	 * The member attribute syntax for ActiveDirectory (used with LDAP_RETURN_ATTRIBUTES)
	 */
	private static final String AD_MEMBER_RANGE = "member;range=0-" + (AD_MEMBER_BATCH_SIZE-1) + "\ncn\nobjectClass";
	
	/*
	 * The member attributes for standard LDAP groups (e.g. groupOfNames, groupOfUniqueNames, ibm-memberGroup)
	 */
	private static final String LDAP_DEFAULT_MEMBER_ATTRIBUTES = "member,uniquemember,ibm-memberGroup";
	
	/**
	 * Newline separated list of attribute names that should be returned from searches
	 */
	private String ldapMemberAttributes;
	
	/**
	 * Set of all possible member attribute names
	 */
	private Set<String> ldapMemberAttributeSet = new HashSet<String>();
	
	/*
	 * groupHierarchy is the path to the current group being processed 
	 */
	private ArrayList<GroupEntry> groupHierarchy = new ArrayList<GroupEntry>();
	
	/*
	 * True if we are connected to ActiveDirectory
	 */
	private Boolean isActiveDirectory;
	
	/*
	 * True if we expand group objects within group objects
	 */
	private Boolean expandNestedGroups;
	
	/**
	 * True if we return users even though they have been returned earlier
	 */
	private boolean returnDuplicateUsers;

	/**
	 * The Set of user names (DN) we have returned
	 */
	private Set<String> returnedUsers = new HashSet<String>();
	
	/*
	 * List of processed group DNs
	 */
	private Set<String> processedGroupLists = new HashSet<String>();
	
	/*
	 * The group currently being processed
	 */
	private GroupEntry group;

	public static class GroupEntry {
		Entry entry;
		String dn;
		Attribute groupMembers;
		int groupIndex;
		ArrayList<String> nestedGroups;
		
		public String getGroupDN() {
			return dn;
		}
		public Entry getGroupEntry() {
			return entry;
		}
		public Attribute getMembers() {
			return groupMembers;
		}
		public boolean hasMoreMembers() {
			return groupMembers != null && groupMembers.size() > groupIndex;
		}
		public String getNextMember() {
			return groupMembers.getValue(groupIndex++).toString();
		}
		public boolean hasMoreGroups() {
			return (nestedGroups.size() > 0);
		}
		public String getNextGroup() {
			return nestedGroups.remove(0);
		}
		public void addGroup(String dn) {
			nestedGroups.add(dn);
		}
	}
	
	public LDAPGroupConnector() {
		setName("LDAP Group Connector");
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}
	
	
	/**
	 * Override to request member/uniquemember attributes for non-AD servers and
	 * member:range=0-499 for AD servers.
	 * 
	 * (non-Javadoc)
	 * @see com.ibm.di.connector.LDAPConnector#selectEntries()
	 */
	@Override
	public void selectEntries() throws Exception {
		if(isAD()) {
			setParam(LDAP_RETURN_ATTRIBUTES, AD_MEMBER_RANGE);
		} else {
			setParam(LDAP_RETURN_ATTRIBUTES, ldapMemberAttributes);
		}
		
		super.selectEntries();
	}

	/**
	 * Returns the current group hierarchy
	 * @return
	 */
	public ArrayList<GroupEntry> getGroupHierarchy() {
		return groupHierarchy;
	}
	
	@Override
	public Entry getNextEntry() throws Exception {
		Entry entry;
		String dn;
		
		//
		// -- Check if we have more group members
		//
		while( (dn = getNextGroupMember()) != null) {
			entry = readEntry(dn);
			if(entry != null) {
				Attribute attr = getGroupEntryMembers(entry);
				if(attr != null) {
					
					if(!isExpandingGroups())
						continue;
					
					if(! processedGroupLists.add(normalizeDN(dn))) {
						continue;
					}
					
					// ActiveDirectory returns an empty member attribute if there are
					// more values than 1000/1500. In this case we have to re-read the entry
					// with the "range=x-y" qualifier.
					if(attr.size() == 0 && isAD()) {
						// TODO: is there any point in reading this Entry? It is not used for anything
						entry = findEntry(DN_ATTRIBUTE, dn);
					}
					
					// TODO: should we also save attr or entry, to avoid rereading the Entry later?
					group.addGroup(dn);
				} else {
					if (!returnedUsers.add(normalizeDN(entry.getString(DN_ATTRIBUTE)))) {
						// Duplicate entry, has been returned before.
						if(!returnDuplicateUsers)
							continue;
						entry.setProperty("duplicate", "true");
					}
					// Return next group member entry
					entry.setProperty("groupHierarchy", groupHierarchy);
					
					Entry delta = new Entry();
					delta.setAttribute(DN_ATTRIBUTE, group.getGroupDN());
					delta.setOperation(Entry.OP_MOD2);
					delta.setAttribute("cn", group.getGroupEntry().getString("cn"));
					delta.setAttribute("objectClass", group.getGroupEntry().getAttribute("objectClass"));
					Attribute member = delta.newAttribute("member", Attribute.ATTRIBUTE_MOD);
					member.addValue(entry.getString(DN_ATTRIBUTE), AttributeValue.AV_ADD);
					entry.setProperty("group", delta);
					entry.setProperty("groupEntry", group);
					return entry;
				}
			}
		}

		//
		// -- Call LDAP to get next entry from selection criteria
		// -- Non group entries are discarded
		//
		while( (entry = super.getNextEntry()) != null) {
			Attribute attr = getGroupEntryMembers(entry);
			dn = entry.getString(DN_ATTRIBUTE);
			if(attr != null && dn != null && processedGroupLists.add(normalizeDN(dn))) {
				setGroupMembers(attr, dn, entry);
				return getNextEntry();
			}
		}
		
		return null;
	}

	/**
	 * Clears and restores the ldapReturnAttributes before/after doing a findEntry on dn.
	 * 
	 * @param dn
	 * @param b 
	 * @return
	 */
	private Entry readEntry(String dn) {
		String save = getParam(LDAP_RETURN_ATTRIBUTES);
		try {
			setParam(LDAP_RETURN_ATTRIBUTES, "");
			return super.findEntry(DN_ATTRIBUTE, dn);
		} finally {
			setParam(LDAP_RETURN_ATTRIBUTES, save);
		}
	}

	/**
	 * Returns the attribute from the entry that contains the group member list or null if
	 * no such attribute exists.
	 * 
	 * @param entry
	 * @return
	 */
	private Attribute getGroupEntryMembers(Entry entry) {
		if(entry == null)
			return null;
		for (String member: ldapMemberAttributeSet) {
			Attribute a = entry.getAttribute(member);
			if (a != null)
				return a;
		}

		return getADMember(entry);
	}
	
	/**
	 * Returns the AD member attribute.
	 *  
	 * @param entry
	 * @return
	 */
	private Attribute getADMember(Entry entry) {
		if(entry != null) {
			for(String str : entry.getAttributeNames()) {
				if(str.startsWith("member;range=")) {
					return entry.getAttribute(str);
				}
			}
		}
		return null;
	}

	/**
	 * Sets the primary group member attribute to iterate
	 * 
	 * @param attribute
	 */
	private void setGroupMembers(Attribute attribute, String dn, Entry entry) {
		if(dn != null) {
			// -- When iterating AD groups we may have to read
			// -- several entries (member:range=0-xxxx etc) so we don't create a new group in this case.
			group = new GroupEntry();
			group.dn = dn;
			group.entry = entry;
			group.nestedGroups = new ArrayList<String>();
			groupHierarchy.add(group);
		}
		group.groupIndex = 0;
		group.groupMembers = attribute;

		// Add members from all member attributes
		if (entry != null && attribute != null ) {
			for (String member: ldapMemberAttributeSet) {
				if (! member.equals(attribute.getName())) {
					Attribute a = entry.getAttribute(member);
					if (a != null) {
						for (Object o: a.getValues()) {
							attribute.addValue(o);
						}						
					}
				}
			}
		}
	}

	/**
	 * Returns the next group member DN from the current group. If the current group has been
	 * exhausted the nestedGroups array is checked for nested groups. If the LDAP server is
	 * ActiveDirectory we check the range on the returned attribute to see if there are more
	 * values to be retrieved.
	 * 
	 * @return
	 */
	private String getNextGroupMember() {
		if(group == null)
			return null;
		
		if(group.hasMoreMembers())
			return group.getNextMember();
		
		if(group.groupMembers != null && checkADGroupList()) {
			return getNextGroupMember();
		}
		
		if(group.hasMoreGroups()) {
			Entry entry = findEntry(DN_ATTRIBUTE, group.getNextGroup());
			setGroupMembers(getGroupEntryMembers(entry), entry.getString(DN_ATTRIBUTE), entry);
			return getNextGroupMember();
		}
		
		// -- pop the group entry 
		if(groupHierarchy.size() > 0) {
			groupHierarchy.remove(groupHierarchy.size() - 1);
			if(groupHierarchy.size() > 0) {
				group = groupHierarchy.get(groupHierarchy.size() - 1);
				return getNextGroupMember();
			} else {
				group = null;
			}
		}
		
		return null;
	}

	/**
	 * Checks the returned attribute name if it contains a range modifier. If it does and
	 * the range indicates more values the next batch is requested and the group attribute
	 * is updated.
	 * 
	 * @return true if more values were requested from the AD server
	 */
	private boolean checkADGroupList() {
		Pattern p = Pattern.compile("member;range=(.*)-(.*)");
		Matcher m = p.matcher(group.groupMembers.getName());
		if(m.matches()) {
			String str = m.group(2);
			if(str.equals("*")) {
				setParam(LDAP_RETURN_ATTRIBUTES, AD_MEMBER_RANGE);
				return false;
			}
			
			int end = Integer.parseInt(str);
			setParam(LDAP_RETURN_ATTRIBUTES, "member;range=" + (end+1) + "-" + (end + AD_MEMBER_BATCH_SIZE));
			Entry e = findEntry(DN_ATTRIBUTE, group.dn);
			setGroupMembers(getADMember(e), null, e);
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the current connection is to an ActiveDirectory server.
	 * 
	 * @return
	 * @throws NamingException
	 */
	private boolean isAD() throws NamingException {
		if(isActiveDirectory == null) {
			isActiveDirectory = Boolean.FALSE;
			if(getLdapContext() != null) {
				Attributes attrs = getLdapContext().getAttributes("");
				if (attrs != null && attrs.get("highestCommittedUSN") != null) {
					isActiveDirectory = Boolean.TRUE;
				}
			}
		}
		return isActiveDirectory;
	}
	
	/**
	 * Returns true if we are currently expanding nested groups.
	 * @return
	 */
	public boolean isExpandingGroups() {
		if(expandNestedGroups == null) {
			expandNestedGroups = getBoolean("ldapExpandGroups");
			if (expandNestedGroups == null)
				expandNestedGroups = Boolean.FALSE;
		}
		return expandNestedGroups;
	}
	
	/**
	 * Sets the expandNestedGroup flag to determine if groups are to be expanded.
	 * 
	 * @param expand
	 */
	public void setExpandingGroups(Boolean expand) {
		expandNestedGroups = expand;
	}

	@Override
	public void initialize(Object o) throws Exception {
		groupHierarchy.clear();
		processedGroupLists.clear();
		returnedUsers.clear();
		group = null;

		Boolean b = getBoolean("ldapReturnUsersOnce");
		returnDuplicateUsers = (b != null) && ! b.booleanValue();
		
		super.initialize(o);
		
		ldapMemberAttributeSet.clear();
		ldapMemberAttributes = "cn\nobjectClass";
		String ldapMembers = getParam("ldpaMemberAttributes");
		if (ldapMembers == null || ldapMembers.trim().length() == 0)
			ldapMembers = LDAP_DEFAULT_MEMBER_ATTRIBUTES;
		for (String member: ldapMembers.split(",")) {
			member = member.trim();
			ldapMemberAttributeSet.add(member);
			ldapMemberAttributes += "\n" + member;
		}
	}

	/**
	 * Lowercases the distinguished name, and removes insignificant spaces
	 * 
	 * @param p1
	 *            A distinguished name
	 * @return The lowercased distinguished name with insignificant spaces removed
	 */
	private String normalizeDN(String p1) {
		String copy = p1.toLowerCase();
		int n = p1.length();
		StringBuilder str = new StringBuilder(n);
		for (int i=0; i < n; i++) {
			char c = copy.charAt(i);
			str.append(c);
			if (c == ',') {
				while (i + 1 < n && copy.charAt(i + 1) == ' ')
					i++;
			}
		}
		return str.toString();
	}

}
