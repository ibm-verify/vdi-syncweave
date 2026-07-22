/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

import java.util.Vector;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;

/**
 * The GroupInfo class represents Windows Group's data structure and
 * encapsulates methods that import and export that data to an Entry object.
 */
public class GroupInfo {
	/**
	 * Copyright message
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the group.
	 */
	private String mGroupName;
	/** Comment */
	private String mComment;
	/** Global status */
	private Boolean mIsGlobal;

	// group entry attributes' names
	/** Group entry attribute name: GroupName */
	public static final String GROUP_ATTR_GROUP_NAME = "GroupName";
	/** Group entry attribute name: Comment */
	public static final String GROUP_ATTR_COMMENT = "Comment";
	/** Group entry attribute name: IsGlobal */
	public static final String GROUP_ATTR_IS_GLOBAL = "IsGlobal";
	/** Group entry attribute name: Users */
	public static final String GROUP_ATTR_USERS = "Users";
	/** Group entry attribute name: Groups */
	public static final String GROUP_ATTR_GROUPS = "Groups";

	// *************************************************************************
	// Constructors
	// *************************************************************************

	/**
	 * Default constructor.
	 */
	public GroupInfo() {
		super();
	}

	/**
	 * Constructs the GroupInfo object and populates its members with Group data
	 * from the given Entry parameter.
	 * 
	 * @param aGroupEntry
	 *            The Group Entry object containing data to initialize the new
	 *            GroupInfo object with.
	 */
	protected GroupInfo(Entry aGroupEntry) {
		super();
		this.copyDataFromGroupEntry(aGroupEntry);
	}

	// *************************************************************************
	// get methods
	// *************************************************************************
	/**
	 * Retrieves the group name.
	 * 
	 * @return String , group name
	 */
	public String getGroupName() {
		return mGroupName;
	}

	/**
	 * Retrieves comment.
	 * 
	 * @return String , comment
	 */
	public String getComment() {
		return mComment;
	}

	/**
	 * Checks if the group is global.
	 * 
	 * @return Boolean , global information
	 */
	public Boolean getIsGlobal() {
		return mIsGlobal;
	}

	// *************************************************************************
	// set methods
	// *************************************************************************
	/**
	 * Sets the group name.
	 * 
	 * @param aGroupName -
	 *            String , sets group name
	 */
	public void setGroupName(String aGroupName) {
		mGroupName = aGroupName;
	}

	/**
	 * Sets group comment.
	 * 
	 * @param aComment -
	 *            String , comment to set
	 */
	public void setComment(String aComment) {
		mComment = aComment;
	}

	/**
	 * Sets parameter that indicates whether the group is global or not.
	 * 
	 * @param aIsGlobal -
	 *            Boolean , sets if the group is global
	 */
	public void setIsGlobal(Boolean aIsGlobal) {
		mIsGlobal = aIsGlobal;
	}

	// *************************************************************************
	// utilities
	// *************************************************************************

	/**
	 * Copies data from a group entry.
	 * 
	 * @param aGroupEntry
	 *            The group entry to copy from.
	 */
	protected void copyDataFromGroupEntry(Entry aGroupEntry) {
		mGroupName = InfoUtil.getStringEntryAttributeValue(aGroupEntry,
				GROUP_ATTR_GROUP_NAME);
		mComment = InfoUtil.getStringEntryAttributeValue(aGroupEntry,
				GROUP_ATTR_COMMENT);
		mIsGlobal = InfoUtil.getBooleanEntryAttributeValue(aGroupEntry,
				GROUP_ATTR_IS_GLOBAL);
	}

	/**
	 * All "null" data members are assigned the values of the corresponding
	 * aGroupInfo's data members.
	 * 
	 * @param aGroupInfo
	 *            The GroupInfo instance which data members' data will be
	 *            copied.
	 */
	protected void completeGroupData(GroupInfo aGroupInfo) {
		if (mGroupName == null) {
			setGroupName(aGroupInfo.getGroupName());
		}

		if (mComment == null) {
			setComment(aGroupInfo.getComment());
		}

		if (mIsGlobal == null) {
			setIsGlobal(aGroupInfo.getIsGlobal());
		}
	}

	/**
	 * Given a group attributes' values creates and adds those attributes to the
	 * given entry.
	 * 
	 * @param aGroupEntry
	 *            The group entry object where attributes will be added.
	 * @param aGroupInfo
	 *            The GroupInfo structure containing the base group attributes.
	 * @param aUserNames
	 *            Vector of Strings containing the names of the group's users.
	 * @param aGroupNames
	 *            Vector of Strings containing the names of the group's groups.
	 *            When populating a global group entry this parameter is null.
	 */
	protected static void populateGroupEntryAttributes(Entry aGroupEntry,
			GroupInfo aGroupInfo, Vector aUserNames, Vector aGroupNames) {
		// create and add base attributes
		InfoUtil.createAndAddEntryAttribute(aGroupEntry, GROUP_ATTR_GROUP_NAME,
				aGroupInfo.getGroupName());
		InfoUtil.createAndAddEntryAttribute(aGroupEntry, GROUP_ATTR_COMMENT,
				aGroupInfo.getComment());
		InfoUtil.createAndAddEntryAttribute(aGroupEntry, GROUP_ATTR_IS_GLOBAL,
				aGroupInfo.getIsGlobal());

		// create and add the Users attribute
		Attribute users = aGroupEntry.newAttribute(GROUP_ATTR_USERS);

		if (aUserNames != null) {
			int userNamesCnt = aUserNames.size();
			for (int i = 0; i < userNamesCnt; i++) {
				users.addValue(aUserNames.elementAt(i));
			}
		}

		// create and add the Groups attribute
		Attribute groups = aGroupEntry.newAttribute(GROUP_ATTR_GROUPS);

		if (aGroupNames != null) {
			int groupNamesCnt = aGroupNames.size();
			for (int i = 0; i < groupNamesCnt; i++) {
				groups.addValue(aGroupNames.elementAt(i));
			}
		}
	}

	/**
	 * Retrieves connector group entry's structure.
	 * 
	 * @return Vector of elements of type Entry describing each groups's
	 *         attribute structure.
	 */
	protected static Vector queryGroupSchema() {
		Vector schema = new Vector();

		InfoUtil.addSchemaEntry(schema, GROUP_ATTR_GROUP_NAME,
				InfoUtil.QSS_STRING, Integer.valueOf(256));
		InfoUtil.addSchemaEntry(schema, GROUP_ATTR_COMMENT,
				InfoUtil.QSS_STRING, Integer.valueOf(256));
		InfoUtil.addSchemaEntry(schema, GROUP_ATTR_IS_GLOBAL,
				InfoUtil.QSS_BOOLEAN, null);
		InfoUtil.addSchemaEntry(schema, GROUP_ATTR_USERS, InfoUtil.QSS_VECTOR,
				null);
		InfoUtil.addSchemaEntry(schema, GROUP_ATTR_GROUPS, InfoUtil.QSS_VECTOR,
				null);

		return schema;
	}

}
