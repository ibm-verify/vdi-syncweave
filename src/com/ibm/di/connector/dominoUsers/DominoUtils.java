/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * Utility class
 */
public class DominoUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * View name : {@value #VIEW_NAME_DENY_GROUPS}
	 */
	public static final String VIEW_NAME_DENY_GROUPS = "Server\\Deny Access Groups";

	/**
	 * New name for deny list view
	 */
	public static final String VIEW_NAME_DENY_GROUPS_2 = "DenyLists";

	/**
	 * Item name : {@value #ITEM_NAME_GROUP_NAME}
	 */
	public static final String ITEM_NAME_GROUP_NAME = "ListName";
	/**
	 * Item name : {@value #ITEM_NAME_GROUP_MEMBERS}
	 */
	public static final String ITEM_NAME_GROUP_MEMBERS = "Members";
	/**
	 * Item name : {@value #ITEM_NAME_FORM}
	 */
	public static final String ITEM_NAME_FORM = "Form";
	/**
	 * Item value : {@value #ITEM_VALUE_PERSON}
	 */
	public static final String ITEM_VALUE_PERSON = "Person";

	/**
	 * Group search filter : {@value #GROUP_SEARCH_FILTER}
	 */
	public static final String GROUP_SEARCH_FILTER = "Form = \"Group\"";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = DominoUsersConnector
			.getResHash();

	/**
	 * Checks if the provided group name exists in the database.
	 * 
	 * @param aDatabase
	 *            date base to look into.
	 * @param aGroupName
	 *            name of the group
	 * @return <code>true</code> if exists.
	 * @throws NotesException
	 */
	public static boolean groupExist(Database aDatabase, String aGroupName)
			throws NotesException {
		boolean found = false;

		DocumentCollection dc = aDatabase.search(GROUP_SEARCH_FILTER);
		if (dc != null) {
			try {
				Document groupDoc = dc.getFirstDocument();
				while (groupDoc != null && found == false) {
					if (groupDoc.getItemValueString(ITEM_NAME_GROUP_NAME)
							.equalsIgnoreCase(aGroupName)) {
						found = true;
					}
					dc.deleteDocument(groupDoc);
					groupDoc.recycle();
					groupDoc = dc.getFirstDocument();
				}
			} finally {
				dc.recycle();
			}
		}

		return found;
	}

	/**
	 * Checks if the provided group name is in the data base view with the
	 * denied groups.
	 * 
	 * @param aDatabase
	 *            database to look into
	 * @param aDenyGroupName
	 *            group name to look for
	 * @param denyGroupViewName
	 * 			  name of view containing deny groups
	 * @return true if group is denied.
	 * @throws NotesException
	 *             if an error occurs.
	 */
	public static boolean denyGroupExist(Database aDatabase,
			String aDenyGroupName, String denyGroupViewName) throws NotesException {
		boolean found = false;

		View denyGroups = aDatabase.getView(denyGroupViewName);

		if (denyGroups != null) {
			try {
				Document groupDoc = denyGroups.getFirstDocument();
				while (groupDoc != null) {
					if (groupDoc.getItemValueString(ITEM_NAME_GROUP_NAME)
							.equalsIgnoreCase(aDenyGroupName)) {
						found = true;
						break;
					}
					groupDoc = denyGroups.getNextDocument(groupDoc);
				}
			} finally {
				denyGroups.recycle();
			}
		}

		return found;
	}

	/**
	 * Checks whether the provided name is in the denied list.
	 * 
	 * @param aDenyGroupDoc
	 *            deny group {@link Document} to look into
	 * @param aUserName
	 *            user name to check
	 * @return <code>true</code> if match found
	 * @throws Exception
	 *             if an error occurs.
	 * 
	 */
	public static boolean isDenyGroupMember(Document aDenyGroupDoc,
			String aUserName) throws Exception {
		if (aDenyGroupDoc == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.DENY.GROUP.DOCUMENT.PARAMETER.IS.NULL"));
		}
		if (aUserName == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USER.NAME.PARAMETER.IS.NULL"));
		}

		boolean isMember = false;

		Vector members = aDenyGroupDoc.getItemValue(ITEM_NAME_GROUP_MEMBERS);
		for (int i = 0; i < members.size(); i++) {
			Object denyGroup = members.get(i);
			if (denyGroup != null
					&& aUserName.equalsIgnoreCase(denyGroup.toString())) {
				isMember = true;
				break;
			}
		}

		return isMember;
	}

	/**
	 * Checks whether a user is in the deny groups view of the data base.
	 * 
	 * @param aDatabase
	 *            data base to check
	 * @param aUserName
	 *            user name to check
	 * @return <code>true</code> if match found
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static boolean isDenyGroupMember(Database aDatabase, String aUserName)
			throws Exception {
		boolean isMember = false;

		View denyGroups = aDatabase.getView(VIEW_NAME_DENY_GROUPS);
		if (denyGroups == null)
			denyGroups = aDatabase.getView(VIEW_NAME_DENY_GROUPS_2);
		if (denyGroups != null) {
			try {
				Document groupDoc = denyGroups.getFirstDocument();
				while (groupDoc != null) {
					if (isDenyGroupMember(groupDoc, aUserName)) {
						isMember = true;
						break;
					}
					groupDoc = denyGroups.getNextDocument(groupDoc);
				}
			} finally {
				denyGroups.recycle();
			}
		}

		return isMember;
	}

	/**
	 * Performs verification if the the group is part of the deny groups of the
	 * data base and if so is the user part of this group.
	 * 
	 * @param aDatabase
	 *            data base to search into
	 * @param aUserName
	 *            user name to check
	 * @param aDenyGroupName
	 *            name of the deny group
	 * @return <code>true</code> if the user is part of the deny group.
	 * @throws Exception
	 *             if the provided group deny name is not in the database view
	 *             or if some other error occurs.
	 */
	public static boolean isDenyGroupMember(Database aDatabase,
			String aUserName, String aDenyGroupName) throws Exception {
		boolean isMember = false;

		View denyGroups = aDatabase.getView(VIEW_NAME_DENY_GROUPS);
		if (denyGroups != null) {
			try {
				boolean groupFound = false;
				Document groupDoc = denyGroups.getFirstDocument();
				while (groupDoc != null) {
					if (groupDoc.getItemValueString(ITEM_NAME_GROUP_NAME)
							.equalsIgnoreCase(aDenyGroupName)) {
						groupFound = true;
						isMember = isDenyGroupMember(groupDoc, aUserName);
						break;
					}
					groupDoc = denyGroups.getNextDocument(groupDoc);
				}

				if (!groupFound) {
					throw new Exception(
							sResHash
									.getString(
											"CONNECTOR.DOMINOUSERSCONN.DENY.ACCESS.GROUP.DOES.NOT.EXISTS",
											aDenyGroupName));
				}
			} finally {
				denyGroups.recycle();
			}
		}

		return isMember;
	}

	/**
	 * Retrieves the canonical name of the user.
	 * 
	 * @param aSession
	 *            {@link Session}
	 * @param aUserName
	 *            user name
	 * @return user's canonical name
	 * @throws NotesException
	 *             if an error occurs
	 */
	public static String getUserCanonicalName(Session aSession, String aUserName)
			throws NotesException {
		String canonicalName = aSession.createName(aUserName).getCanonical();
		return canonicalName;
	}

	/**
	 * Retrieves user's abbreviated name.
	 * 
	 * @param aSession
	 *            {@link Session}
	 * @param aUserName
	 *            user name
	 * @return user's abbreviated name
	 * @throws NotesException
	 */
	public static String getUserAbbreviatedName(Session aSession,
			String aUserName) throws NotesException {
		String abbreviatedName = aSession.createName(aUserName)
				.getAbbreviated();
		return abbreviatedName;
	}

	/**
	 * Clones the search criteria.
	 * 
	 * @param aSearch
	 *            {@link SearchCriteria} to copy
	 * @return exact copy of the provided SearchCriteria
	 */
	public static SearchCriteria cloneSearchCriteria(SearchCriteria aSearch) {
		SearchCriteria newSearch = new SearchCriteria();
		newSearch.setType(aSearch.getType());

		Vector criteria = aSearch.getCriteria();
		if (criteria != null) {
			for (int i = 0; i < criteria.size(); i++) {
				Object obj = criteria.get(i);
				if (obj instanceof SearchCriteria.rscSearch) {
					SearchCriteria.rscSearch criteriaEl = (SearchCriteria.rscSearch) obj;
					newSearch.addCriteria(criteriaEl.name, criteriaEl.match,
							criteriaEl.value);
				}
			}
		}

		return newSearch;
	}

	// This method is introduced here because "SearchCriteria.getNotesFilter()"
	// does not build correctly Lotus formulas (at the time when the
	// DominoUsersConnectorConnector is implemented)
	// "SearchCriteria.getNotesFilter()" uses improperly "AND" instetad of "&"
	// and "OR" instead of "|"
	/**
	 * Builds Notes formula.
	 * 
	 * @param aSearch
	 *            {@link SearchCriteria} instance
	 * @return Notes formula from the provided search criteria
	 * @see SearchCriteria#getNotesFilter()
	 */
	public static String getNotesFormula(SearchCriteria aSearch) {
		StringBuffer formula = new StringBuffer("");

		String op = null;
		if (aSearch.getType() == SearchCriteria.SEARCH_AND) {
			op = " & ";
		} else {
			op = " | ";
		}

		for (int i = 0; i < aSearch.getCriteria().size(); i++) {
			if (formula.length() > 0) {
				formula.append(op);
			}

			Object criteria = aSearch.getCriteria().get(i);
			if (criteria instanceof SearchCriteria.rscSearch) {
				formula
						.append(buildNotesFormulaElement((SearchCriteria.rscSearch) criteria));
			} else {
				return "Hierarchical search criteria not supported for Notes";
			}
		}

		return formula.toString();
	}

	// This method is introduced here because
	// "SearchCriteria.buildNotesFilter()"
	// does not build correctly Lotus formulas (at the time when the
	// DominoUsersConnectorConnector is implemented)
	// "SearchCriteria.buildNotesFilter()" uses "*" wildchars instead of
	// "@Contains", "@Begins", "@Ends"
	/**
	 * Builds Notes formula element.
	 * 
	 * @param criteria
	 *            search criteria
	 * @return the built element
	 */
	public static String buildNotesFormulaElement(
			SearchCriteria.rscSearch criteria) {

		String value = criteria.value != null ? criteria.value.toString() : "";
		if (value.contains("\\") || value.contains("\""))
			value = value.replace("\\", "\\\\").replace("\"", "\\\"");
		
		switch (criteria.match) {
		case SearchCriteria.EXACT: // '='
			return criteria.name + "=\"" + value + "\"";

		case SearchCriteria.SUBSTRING: // '~'
			return "@Contains(" + criteria.name + ";\"" + value
					+ "\")";

		case SearchCriteria.INITIAL_STRING: // '^'
			return "@Begins(" + criteria.name + ";\"" + value + "\")";

		case SearchCriteria.FINAL_STRING: // '$'
			return "@Ends(" + criteria.name + ";\"" + value + "\")";

		case SearchCriteria.NOT_STRING: // '!'
			return criteria.name + "!=\"" + value + "\"";

		default:
			return "";
		}
	}

	/**
	 * Try to guess the name of the view that contains the deny groups
	 * @param database
	 * @return
	 * @throws NotesException 
	 */
	public static String getDenyGroupViewName(Database database) throws NotesException {
		View denyGroups = database.getView(VIEW_NAME_DENY_GROUPS);
		if (denyGroups != null) {
			denyGroups.recycle();
			return VIEW_NAME_DENY_GROUPS;
		}

		denyGroups = database.getView(VIEW_NAME_DENY_GROUPS_2);
		if (denyGroups != null) {
			denyGroups.recycle();
			return VIEW_NAME_DENY_GROUPS_2;
		}
		
		return null;
	}
}
