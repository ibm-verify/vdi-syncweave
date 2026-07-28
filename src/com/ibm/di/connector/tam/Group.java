/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import com.ibm.di.server.Log;
import com.ibm.di.server.Trace;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.tivoli.pd.jadmin.PDGroup;
import com.tivoli.pd.jutil.PDContext;
import com.tivoli.pd.jutil.PDException;
import com.tivoli.pd.jutil.PDMessages;
import com.tivoli.pd.jutil.PDRgyGroupName;

/**
 * Group class contains the functionality to find, add, modify and delete Groups
 * from TAM using the TAM Connector for SyncWeave.
 */
public class Group extends CommonBase {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mGroupID;

	private String mDN;

	private String mCN;

	private String mDescription;

	private Boolean mIsPDGroup;

	private String mContainer;

	private List mUsers;

	private Attribute mUsersAtt;

	private Boolean mReplaceUsers;

	public static final String GROUP_ATTR_GROUP_ID = "GroupName";

	private static final String GROUP_ATTR_DN = "RegistryGID";

	private static final String GROUP_ATTR_CN = "CommonName";

	private static final String GROUP_ATTR_DESCRIPTION = "Description";

	private static final String GROUP_ATTR_IS_PD_GROUP = "IsPDGroup";

	private static final String GROUP_ATTR_CONTAINER = "ObjectContainer";

	private static final String GROUP_ATTR_USERS = "Users";

	private static final String GROUP_REPLACE_USERS = "ReplaceUsersOnUpdate";

	private static final String GROUP = "Group";

	/**
	 * Constructor for Group.
	 * 
	 * @param s
	 *            Contains the Group name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The Connector log
	 * 
	 * @throws PDException
	 */
	public Group(String s, PDContext context, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "Group Constructor #1");
		if (s == null || s.length() == 0) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, GROUP_ATTR_GROUP_ID));
		}
		debug("Group Name = " + s);
		PDGroup group = new PDGroup(mPDContext, s, mPDMessages);
		processMsgs(mPDMessages);
		set(group);
		Trace.exitmin(this, "Group Constructor #1");
	}

	/**
	 * Constructor for Group.
	 * 
	 * @param entry
	 *            The SyncWeave Data
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The Connector log
	 */
	public Group(Entry entry, PDContext context, Log log) {
		super(context, log);
		Trace.entrymin(this, "Group Constructor #2");
		printEntry(entry);
		mGroupID = getStringEntryAttributeValue(entry, GROUP_ATTR_GROUP_ID);
		mDN = getStringEntryAttributeValue(entry, GROUP_ATTR_DN);
		mCN = getStringEntryAttributeValue(entry, GROUP_ATTR_CN);
		mDescription = getStringEntryAttributeValue(entry,
				GROUP_ATTR_DESCRIPTION);
		mIsPDGroup = getBooleanEntryAttributeValue(entry,
				GROUP_ATTR_IS_PD_GROUP);
		mContainer = getStringEntryAttributeValue(entry, GROUP_ATTR_CONTAINER);
		mReplaceUsers = getBooleanEntryAttributeValue(entry,
				GROUP_REPLACE_USERS);
		if (mReplaceUsers == null)
			mReplaceUsers = Boolean.TRUE;
		// build a list of users in this group
		mUsersAtt = entry.getAttribute(GROUP_ATTR_USERS);
		if (mUsersAtt != null) {
			if ((mUsersAtt.getOperation().equalsIgnoreCase("delete") || mUsersAtt
					.getOper() == Attribute.ATTRIBUTE_DELETE)
					|| ((mUsersAtt.getOperation().equalsIgnoreCase("replace") || mUsersAtt
							.getOper() == Attribute.ATTRIBUTE_REPLACE) && (mUsersAtt
							.getValuesVector().size() < 1))) {
				mUsers = new ArrayList(0);
			} else {
				List users = new ArrayList(mUsersAtt.getValuesVector());
				// purge empty strings
				for (int i = users.size() - 1; i >= 0; i--) {
					String user = (String) users.get(i);
					if (user == null || user.length() == 0)
						users.remove(i);
				}
				mUsers = users;
			}
		} else
			mUsers = new ArrayList(0);
		Trace.exitmin(this, "Group Constructor #2");
	}

	/**
	 * Constructor for Group.
	 * 
	 * @param searchcriteria
	 *            The Group name
	 * @param context
	 *            The TAM Context
	 * @param log
	 *            The Connector log
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public Group(SearchCriteria searchcriteria, PDContext context, Log log)
			throws TAMConnectorException, PDException {
		super(context, log);
		Trace.entrymin(this, "Group Constructor #3", log);
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		if (!s.equalsIgnoreCase(Group.GROUP_ATTR_GROUP_ID)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA, s));
		debug("Search Criteria Value = "
				+ searchcriteria.getFirstCriteriaValue());
		PDGroup group = new PDGroup(mPDContext, searchcriteria
				.getFirstCriteriaValue(), mPDMessages);
		processMsgs(mPDMessages);
		set(group);
		debug("Group Name = " + mGroupID);
		Trace.exitmin(this, "Group Constructor #3");
	}

	/**
	 * Constructor for Group. Designed to be used w/o a previous call to
	 * Locate() to initialise from the existing underlying PDObject
	 * 
	 * @param context
	 *            The TAM Context
	 * @param searchcriteria
	 *            The Group name
	 * @param log
	 *            The Connector log
	 * 
	 * @throws TAMConnectorException
	 */
	public Group(PDContext context, SearchCriteria searchcriteria, Log log)
			throws TAMConnectorException {
		super(context, log);
		Trace.entrymin(this, "Group Constructor #4", log);
		String s = searchcriteria.getFirstCriteriaName();
		int i = searchcriteria.getFirstCriteriaMatch();
		String sv = searchcriteria.getFirstCriteriaValue();
		if (!s.equalsIgnoreCase(Group.GROUP_ATTR_GROUP_ID)
				|| i != SearchCriteria.EXCACT)
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.UNSUPPORTED_SEARCH_CRITERIA, s));
		if (sv == null) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.MISSING_ATTRIBUTE, GROUP_ATTR_GROUP_ID));
		}
		debug("Search Criteria Value = "
				+ searchcriteria.getFirstCriteriaValue());
		mGroupID = sv;
		debug("Group Name = " + mGroupID);
		Trace.exitmin(this, "Group Constructor #4");
	}

	private void set(PDGroup group) throws PDException {
		mGroupID = group.getId();
		mDN = group.getRgyName();
		mDescription = group.getDescription();
		mIsPDGroup = Boolean.valueOf(group.isPDGroup());
		mContainer = null;
		mUsers = group.getMembers();
		if (mUsers == null) {
			mUsers = new ArrayList(0);
		}
		print();
	}

	/**
	 * Retuns the Users associated with the group.
	 * 
	 * @return mUsers
	 * 
	 * @see Vector
	 */
	public List getUsers() {
		return mUsers;
	}

	/**
	 * Returns the Group Name
	 * 
	 * @return mGroupID
	 */
	public String getGroupID() {
		return mGroupID;
	}

	/**
	 * Return the Group Description
	 * 
	 * @return mDescription;
	 */
	public String getDesc() {
		return mDescription;
	}

	public Boolean replaceUsers() {
		return mReplaceUsers;
	}

	public Attribute usersAtt() {
		return mUsersAtt;
	}

	/**
	 * Add a Group
	 * 
	 * @param importReg
	 *            <code>true</code> to import the user details from the
	 *            registry <code>false</code> to create the user details and
	 *            add to the registry
	 * 
	 * @throws TAMConnectorException
	 */
	public void put(boolean importReg) throws TAMConnectorException {
		Trace.entrymin(this, "Group.put");
		HashMap failed = new HashMap(5);
		PDRgyGroupName pdRgyGroupName = new PDRgyGroupName(mDN);
		try {
			if (importReg) {
				PDGroup.importGroup(mPDContext, mGroupID, pdRgyGroupName,
						mContainer, mPDMessages);
				processMsgs(mPDMessages);
			} else {
				PDGroup.createGroup(mPDContext, mGroupID, pdRgyGroupName, null,
						mContainer, mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			throw new TAMConnectorException(getPDMessage(pde));
		}
		try {
			if (mDescription != null && mDescription.length() > 0) {
				PDGroup.setDescription(mPDContext, mGroupID, mDescription,
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(GROUP_ATTR_DESCRIPTION, getPDMessage(pde));
		}
		try {
			if (mUsers != null && mUsers.size() > 0) {
				// have to cast mUsers as method expects an ArrayList, not a
				// List
				PDGroup.addMembers(mPDContext, mGroupID, (ArrayList) mUsers,
						mPDMessages);
				processMsgs(mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(GROUP_ATTR_USERS, getPDMessage(pde));
		}
		if (failed.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.CREATE_ERROR, GROUP));
			throw new TAMConnectorException(failed, TMSMessageGetter
					.getMessage(TMSMsgId.CREATE_ERROR, GROUP));
		}
		Trace.exitmin(this, "Group.put");
	}

	/**
	 * Modify a Group
	 * 
	 * @param changes
	 *            The group entry to modify.
	 * 
	 * @throws TAMConnectorException
	 */
	public void modify(Entry changes) throws TAMConnectorException {
		Trace.entrymin(this, "Group.modify");
		Group newGroup = new Group(changes, mPDContext, mLogProxy);
		HashMap failed = new HashMap(5);
		PDGroup oldGroup = null;
		List oldMembers = null;
		try {
			oldGroup = new PDGroup(mPDContext, mGroupID, mPDMessages);
			oldMembers = oldGroup.getMembers();
		} catch (PDException e) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.GROUP_LOAD_ERROR, mGroupID));
		}
		try {
			// only update the group description and member list
			if (newGroup.getDesc() != null && newGroup.getDesc().length() > 0) {
				oldGroup.setDescription(mPDContext, newGroup.getDesc(),
						mPDMessages);
			}
		} catch (PDException pde) {
			failed.put(GROUP_ATTR_DESCRIPTION, getPDMessage(pde));
		}
		if (newGroup.getUsers().size() > 0) {
			if (newGroup.replaceUsers().booleanValue() == true) {
				// original functionality preserved
				if (!newGroup.getUsers().toString().equalsIgnoreCase(
						oldMembers.toString())) {
					removeUsers(oldGroup, oldMembers, failed);
					addUsers(oldGroup, newGroup.getUsers(), failed);
				}
			} else {
				Attribute usersAtt = newGroup.usersAtt();
				// new functionality for ITIM, step through attribute list
				// and add or delete group based on operation code.
				for (int i = 0; i < usersAtt.size(); i++) {
					String user = usersAtt.getValue(i).toString();
					ArrayList list = new ArrayList();
					list.add(user);
					try {
						// check if the operation code is for deletion or
						// addition of group
						if (usersAtt.getValueOperation(i).equalsIgnoreCase(
								Attribute.OPER[AttributeValue.AV_DELETE])) {
							// remove the group membership (remove the member
							// form the group)
							oldGroup.removeMembers(mPDContext, list,
									mPDMessages);
							processMsgs(mPDMessages);
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.REMOVING_FROM_GROUP, user));
						} else if (usersAtt.getValueOperation(i)
								.equalsIgnoreCase(
										Attribute.OPER[AttributeValue.AV_ADD])) {
							// add to this group membership
							oldGroup.addMembers(mPDContext, list, mPDMessages);
							processMsgs(mPDMessages);
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.ADDING_TO_GROUP, user));
						} else {
							logmsg(TMSMessageGetter.getMessage(
									TMSMsgId.INVALID_OPER_CODE,
									Attribute.OPER[usersAtt.getValueOper(i)]));
						}
					} catch (PDException pde) {
						failed.put(GROUP_ATTR_USERS + i
								+ Attribute.OPER[usersAtt.getValueOper(i)],
								getPDMessage(pde));
					}
				}
			}
		} else if (newGroup.replaceUsers().booleanValue() == true
				&& newGroup.usersAtt() != null) {
			// no users in new group, but attribute was present, so empty
			// existing group of users
			removeUsers(oldGroup, oldMembers, failed);
		}
		if (failed.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.MODIFY_ERROR, GROUP));
			throw new TAMConnectorException(failed, TMSMessageGetter
					.getMessage(TMSMsgId.MODIFY_ERROR, GROUP));
		}
		Trace.exitmin(this, "Group.modify");
	}

	private void removeUsers(PDGroup group, List members, HashMap failed) {
		Trace.entrymin(this, "Group.removeUsers");
		if (members.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.REMOVING_FROM_GROUP,
					members.toString()));
			try {
				PDGroup.removeMembers(mPDContext, group.getId(),
						(ArrayList) members, mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				failed.put(GROUP_ATTR_USERS + "_del", getPDMessage(pde));
			}
		}
		Trace.exitmin(this, "Group.removeUsers");
	}

	private void addUsers(PDGroup group, List members, HashMap failed) {
		Trace.entrymin(this, "Group.addUsers");
		if (members.size() > 0) {
			logmsg(TMSMessageGetter.getMessage(TMSMsgId.ADDING_TO_GROUP,
					members.toString()));
			try {
				PDGroup.addMembers(mPDContext, group.getId(),
						(ArrayList) members, mPDMessages);
				processMsgs(mPDMessages);
			} catch (PDException pde) {
				failed.put(GROUP_ATTR_USERS + "_add", getPDMessage(pde));
			}
		}
		Trace.exitmin(this, "Group.addUsers");
	}

	/**
	 * Returns the Group details in an Entry object
	 * 
	 * @return Entry
	 */
	public Entry getAttributes() {
		Entry entry = new Entry();
		createAndAddEntryAttribute(entry, GROUP_ATTR_GROUP_ID, mGroupID);
		createAndAddEntryAttribute(entry, GROUP_ATTR_DN, mDN);
		createAndAddEntryAttribute(entry, GROUP_ATTR_CN, mCN);
		createAndAddEntryAttribute(entry, GROUP_ATTR_DESCRIPTION, mDescription);
		createAndAddEntryAttribute(entry, GROUP_ATTR_IS_PD_GROUP, mIsPDGroup);
		createAndAddEntryAttribute(entry, GROUP_ATTR_CONTAINER, mContainer);
		createAndAddEntryAttribute(entry, GROUP_REPLACE_USERS, mReplaceUsers);
		Attribute attribute = entry.newAttribute(GROUP_ATTR_USERS);
		if (mUsers != null) {
			int i = mUsers.size();
			for (int j = 0; j < i; j++)
				attribute.addValue(mUsers.get(j).toString());
		}
		return entry;
	}

	/**
	 * Delete a Group
	 * 
	 * @param deleteReg
	 *            <code>true</code> to delete the user from TAM and the
	 *            registry <code>false</code> to delete the user from TAM but
	 *            not the registry
	 * 
	 * @throws TAMConnectorException
	 * @throws PDException
	 */
	public void delete(boolean deleteReg) throws TAMConnectorException,
			PDException {
		Trace.entrymin(this, "Group.delete");
		if (mGroupID.equalsIgnoreCase("SecurityGroup")
				|| mGroupID.equalsIgnoreCase("iv-admin")
				|| mGroupID.equalsIgnoreCase("ivacld-servers")
				|| mGroupID.equalsIgnoreCase("ivmgrd-servers")
				|| mGroupID.equalsIgnoreCase("remote-acl-users")
				|| mGroupID.equalsIgnoreCase("secmgrd-servers")
				|| mGroupID.equalsIgnoreCase("webseal-mpa-servers")
				|| mGroupID.equalsIgnoreCase("webseal-servers")) {
			throw new TAMConnectorException(TMSMessageGetter.getMessage(
					TMSMsgId.GROUP_RESERVED, mGroupID));
		}
		logmsg(TMSMessageGetter.getMessage(TMSMsgId.DELETE_GROUP, mGroupID));
		PDGroup.deleteGroup(mPDContext, mGroupID, deleteReg, mPDMessages);
		processMsgs(mPDMessages);
		Trace.exitmin(this, "Group.delete");
	}

	/**
	 * Get the schema for TAM groups.
	 * 
	 * @return vector with the schema description
	 */
	public static Vector schema() {
		Vector vector = new Vector();
		addSchemaEntry(vector, GROUP_ATTR_GROUP_ID, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, GROUP_ATTR_DN, QSS_STRING, Integer.valueOf(256));
		addSchemaEntry(vector, GROUP_ATTR_CN, QSS_STRING, Integer.valueOf(256));
		addSchemaEntry(vector, GROUP_ATTR_DESCRIPTION, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, GROUP_ATTR_IS_PD_GROUP, QSS_BOOLEAN, null);
		addSchemaEntry(vector, GROUP_ATTR_CONTAINER, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, GROUP_ATTR_USERS, QSS_STRING, Integer
				.valueOf(256));
		addSchemaEntry(vector, GROUP_REPLACE_USERS, QSS_BOOLEAN, null);
		return vector;
	}

	/**
	 * Return a List (ArrayList) of Groups
	 * 
	 * @param pdContext
	 *            The TAM Context
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(PDContext pdContext) throws PDException {
		PDMessages msgs = new PDMessages();
		return PDGroup.listGroups(pdContext, PDGroup.PDGROUP_ALLPATTERN,
				PDGroup.PDGROUP_MAXRETURN, false, msgs);
	}

	/**
	 * Returns a list (ArrayList) of all the Groups for the TAM Context matching
	 * search criteria value
	 * 
	 * @param pdContext
	 *            The TAM Context
	 * 
	 * @param searchCriteria
	 *            The IDI search criteria
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(SearchCriteria searchCriteria, PDContext pdContext)
			throws PDException {
		return Group.list(searchCriteria.getFirstCriteriaValue(), pdContext);
	}

	/**
	 * Return a filtered List (ArrayList) of Groups
	 * 
	 * @param filter
	 *            A TAM Group filter string. The filter is a case-insensitive
	 *            mixture of string constants and wildcards. Filtering is on the
	 *            group name
	 * @param pdContext
	 *            The TAM Context
	 * 
	 * @return List (ArrayList)
	 * 
	 * @throws PDException
	 */
	public static List list(String filter, PDContext pdContext)
			throws PDException {
		PDMessages msgs = new PDMessages();
		if (filter == null || filter.length() == 0) {
			return PDGroup.listGroups(pdContext, PDGroup.PDGROUP_ALLPATTERN,
					PDGroup.PDGROUP_MAXRETURN, false, msgs);
		} else {
			return PDGroup.listGroups(pdContext, filter,
					PDGroup.PDGROUP_MAXRETURN, false, msgs);
		}
	}

	private void print() {
		debug("The users for the group:"
				+ (mUsers == null ? "null" : mUsers.toString()));
		debug("mGroupID = " + (mGroupID == null ? "null" : mGroupID));
		debug("mDN = " + (mDN == null ? "null" : mDN));
		debug("mCN = " + (mCN == null ? "null" : mCN));
		debug("mDescription = "
				+ (mDescription == null ? "null" : mDescription));
		debug("mIsPDGroup = "
				+ (mIsPDGroup == null ? "null" : mIsPDGroup.toString()));
		debug("mContainer = " + (mContainer == null ? "null" : mContainer));
	}
}
