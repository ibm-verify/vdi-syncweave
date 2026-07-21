/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This class performs a user access to Domino.
 * 
 */
public class UserAccess implements IDominoAction {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// the Attribute Names making up the fixed schema of Attributes
	/**
	 * Attribute name : {@value #ATTR_NAME_SET_TYPE}
	 */

	public static final String ATTR_NAME_SET_TYPE = "ACC_SetType";

	/**
	 * Attribute name : {@value #ATTR_NAME_DENY_GROUP_NAME}
	 */
	public static final String ATTR_NAME_DENY_GROUP_NAME = "ACC_DenyGroupName";

	// ATTR_NAME_SET_TYPE Attribute values
	/**
	 * Attribute name : {@value #ATTR_VALUE_SET_TYPE_DISABLE}
	 */
	public static final int ATTR_VALUE_SET_TYPE_DISABLE = 0;

	/**
	 * Attribute name : {@value #ATTR_VALUE_SET_TYPE_ENABLE}
	 */
	public static final int ATTR_VALUE_SET_TYPE_ENABLE = 1;

	// local data holders
	/**
	 * Enable/Disable access
	 */
	private Integer mAccessType = null; // 0 - disable; 1 - enable

	/**
	 * Deny group name
	 */
	private String mDenyGroupName = null;

	/**
	 * User full name
	 */
	private String mFullName = null;

	/**
	 * The DominoUsersConnector that created this Domino Action object
	 */
	private DominoUsersConnector mParent = null;

	/**
	 * {@link Session} instance
	 */
	private Session mSession = null;

	/**
	 * Database.
	 */
	private Database mDatabase = null;

	/**
	 * Deny group view name
	 */
	private String denyGroupViewName = null;
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = DominoUsersConnector
			.getResHash();

	/**
	 * Class constructor.
	 * 
	 * @param aParent
	 *            The DominoUsersConnector that created this Domino Action
	 *            object
	 * @throws Exception
	 *             if parent session is not valid
	 */
	public UserAccess(DominoUsersConnector aParent) throws Exception {
		mParent = aParent;

		mSession = mParent.getSession();
		if (mSession == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERACCESS.CANNOT.INSTANTIATE.USERACCESS.NOTES.NULL"));
		}

		mDatabase = mParent.getDatabase();
		if (mDatabase == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.USERACCESS.CANNOT.INSTANTIATE.USERACCESS.DATABASE.NULL"));
		}
	}

	/**
	 * Retrieves acces type.
	 * 
	 * @return access type.
	 */
	// access methods
	public Integer getAccessType() {
		return mAccessType;
	}

	/**
	 * Sets access type.
	 * 
	 * @param aAccessType
	 *            '0' disable , '1' enable.
	 */
	public void setAccessType(Integer aAccessType) {
		mAccessType = aAccessType;
	}

	/**
	 * Retrieves deny group name.
	 * 
	 * @return deny group name.
	 */
	public String getDenyGroupName() {
		return mDenyGroupName;
	}

	/**
	 * Sets deny group name.
	 * 
	 * @param aDenyGroupName
	 *            name
	 */
	public void setDenyGroupName(String aDenyGroupName) {
		mDenyGroupName = aDenyGroupName;
	}

	/**
	 * Retrieves user's full name.
	 * 
	 * @return user`s full name.
	 */
	public String getUserFullName() {
		return mFullName;
	}

	/**
	 * Sets user's full name
	 * 
	 * @param aUserFullName
	 *            full name
	 */
	public void setUserFullName(String aUserFullName) {
		mFullName = aUserFullName;
	}

	// implementation of the IDominoAction methods
	/**
	 * {@inheritDoc}
	 */
	public Entry extractAndStoreData(Entry aEntry) throws Exception {
		Entry entryNoFixedAttr = aEntry.clone();

		Attribute attrSetType = aEntry.getAttribute(ATTR_NAME_SET_TYPE);
		if (attrSetType != null) {
			mAccessType = Integer.valueOf(attrSetType.getValue());

			entryNoFixedAttr.removeAttribute(ATTR_NAME_SET_TYPE);
		}

		Attribute attrDenyGroupName = aEntry
				.getAttribute(ATTR_NAME_DENY_GROUP_NAME);
		if (attrDenyGroupName != null) {
			mDenyGroupName = attrDenyGroupName.getValue();

			entryNoFixedAttr.removeAttribute(ATTR_NAME_DENY_GROUP_NAME);
		}

		return entryNoFixedAttr;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean mustPerform(Entry aEntry) throws Exception {
		Attribute attrSetType = aEntry.getAttribute(ATTR_NAME_SET_TYPE);
		if (attrSetType != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String canPerform() {
		if (mAccessType == null) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERACCESS.MISSING.SETTYPE.VALUE",
							ATTR_NAME_SET_TYPE);
		}

		int accessType = mAccessType.intValue();

		if (accessType != ATTR_VALUE_SET_TYPE_DISABLE
				&& accessType != ATTR_VALUE_SET_TYPE_ENABLE) {
			return sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.USERACCESS.INVALID.SETTYPE.VALUE",
							ATTR_NAME_SET_TYPE);
		}

		if (accessType == ATTR_VALUE_SET_TYPE_DISABLE) {
			if (mDenyGroupName == null) {
				return sResHash
						.getString(
								"CONNECTOR.DOMINOUSERSCONN.USERACCESS.MISSING.DENYGROUPNAME.VALUE",
								ATTR_NAME_DENY_GROUP_NAME);
			}

			try {
				if (getDenyGroupViewName() == null) {
					return sResHash.getString("CONNECTOR.DOMINOUSERSCONN.USERACCESS.ENABLEUSER.COULD.NOT.FIND.VIEW2",
											DominoUtils.VIEW_NAME_DENY_GROUPS);
				}

				if (!DominoUtils.denyGroupExist(mDatabase, mDenyGroupName, getDenyGroupViewName())) {
					return sResHash.getString("CONNECTOR.DOMINOUSERSCONN.GROUP.NOT.EXIST",
							mDenyGroupName);
				}
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		if (mFullName == null || mFullName.length() == 0) {
			return sResHash
					.getString("CONNECTOR.DOMINOUSERSCONN.MISSING.FULLNAME.VALUE");
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void perform() throws Exception {
		if (mAccessType.intValue() == ATTR_VALUE_SET_TYPE_DISABLE) {
			disableUser(mFullName, mDenyGroupName);
		}

		if (mAccessType.intValue() == ATTR_VALUE_SET_TYPE_ENABLE) {
			enableUser(mFullName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void resetData() throws Exception {
		mAccessType = null;
		mDenyGroupName = null;
		mFullName = null;
	}

	/**
	 * Removes a user from any deny group
	 * 
	 * @param aUserName
	 *            user name
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void enableUser(String aUserName) throws Exception {
		try {
			View denyGroups = null;
			if (getDenyGroupViewName() == null ||
					(denyGroups = mDatabase.getView(getDenyGroupViewName())) == null) {
				if (mParent.debugMode()) {
					debug(sResHash.getString(
									"CONNECTOR.DOMINOUSERSCONN.USERACCESS.ENABLEUSER.COULD.NOT.FIND.VIEW",
									DominoUtils.VIEW_NAME_DENY_GROUPS));
				}
				throw new Exception(
						sResHash.getString(
										"CONNECTOR.DOMINOUSERSCONN.USERACCESS.ENABLEUSER.COULD.NOT.FIND.VIEW2",
										DominoUtils.VIEW_NAME_DENY_GROUPS));
			}

			try {
				Document groupDoc = denyGroups.getFirstDocument();
				while (groupDoc != null) {
					Vector members = groupDoc
							.getItemValue(DominoUtils.ITEM_NAME_GROUP_MEMBERS);

					boolean found = false;
					int i = 0;
					while (i < members.size()) {
						Object obj = members.get(i);
						if (obj != null
								&& obj.toString().equalsIgnoreCase(aUserName)) {
							members.remove(i);
							found = true;
						} else {
							i++;
						}
					}

					if (found) {
						groupDoc.replaceItemValue(
								DominoUtils.ITEM_NAME_GROUP_MEMBERS, members);
						groupDoc.save(true);
					}

					groupDoc = denyGroups.getNextDocument(groupDoc);
				}
			} finally {
				denyGroups.recycle();
			}
		} catch (NotesException e) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.USERACCESS.NOTESEXCEPTION",
					new Object[] { Integer.valueOf(e.id), e.text }));
		}
	}

	/**
	 * Adds a user to the deny group if not already there.
	 * 
	 * @param aUserName
	 *            name to add
	 * @param aDenyGroupName
	 *            deny group to add to.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void disableUser(String aUserName, String aDenyGroupName)
			throws Exception {
		try {
			View denyGroups = null;
			if (getDenyGroupViewName() == null ||
					(denyGroups = mDatabase.getView(getDenyGroupViewName())) == null) {
				if (mParent.debugMode()) {
					debug(sResHash.getString(
									"CONNECTOR.DOMINOUSERSCONN.USERACCESS.DISABLEUSER.COULD.NOT.FIND.VIEW",
									DominoUtils.VIEW_NAME_DENY_GROUPS));
				}
				throw new Exception(
						sResHash.getString(
										"CONNECTOR.DOMINOUSERSCONN.USERACCESS.DISABLEUSER.COULD.NOT.FIND.VIEW2",
										DominoUtils.VIEW_NAME_DENY_GROUPS));
			}

			try {
				boolean groupFound = false;
				Document groupDoc = denyGroups.getFirstDocument();
				while (groupDoc != null) {
					if (groupDoc.getItemValueString(
							DominoUtils.ITEM_NAME_GROUP_NAME).equalsIgnoreCase(
							aDenyGroupName)) {
						groupFound = true;
						break;
					}
					groupDoc = denyGroups.getNextDocument(groupDoc);
				}

				if (groupFound) {
					if (!DominoUtils.isDenyGroupMember(groupDoc, aUserName)) {
						Vector members = groupDoc
								.getItemValue(DominoUtils.ITEM_NAME_GROUP_MEMBERS);
						members.add(aUserName);
						groupDoc.replaceItemValue(
								DominoUtils.ITEM_NAME_GROUP_MEMBERS, members);
						groupDoc.save(true);
					}
				} else {
					if (mParent.debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.USERACCESS.DISABLEUSER.DENY.ACCESS.GROUP",
										aDenyGroupName));
					}
					throw new Exception(
							sResHash
									.getString(
											"CONNECTOR.DOMINOUSERSCONN.USERACCESS.DISABLEUSER.DENY.ACCESS.GROUP2",
											aDenyGroupName));
				}
			} finally {
				denyGroups.recycle();
			}
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.USERACCESS.DISABLEUSER.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	/**
	 * Log a debug message to the connector's log
	 * 
	 * @param aMessage
	 *            The message to write to the log
	 */
	void debug(String aMessage) {
		mParent.debug(aMessage);
	}

	private String getDenyGroupViewName() throws NotesException {
		if (denyGroupViewName == null)
			denyGroupViewName = DominoUtils.getDenyGroupViewName(mDatabase);
		return denyGroupViewName;
	}
}
