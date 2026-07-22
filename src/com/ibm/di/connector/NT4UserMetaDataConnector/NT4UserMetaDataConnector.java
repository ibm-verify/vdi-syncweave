/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

import java.util.Enumeration;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * This class represents a Connector for managing Windows User and Group
 * accounts.
 */
public class NT4UserMetaDataConnector extends Connector implements
		ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/** Category of the component, used for the {@link ResourceHash} */
	private static final String PROPERTIES_FILE = "windowsusrgrpconnector";

	/** Connector`s parameter name */
	public static final String PARAM_COMPUTER_NAME = "ComputerName";
	/** Connector`s parameter name */
	public static final String PARAM_USER_NAME = "UserName";
	/** Connector`s parameter name */
	public static final String PARAM_PASSWORD = "Password";
	/** Connector`s parameter name */
	public static final String PARAM_ENTRY_TYPE = "EntryType";
	/** Connector`s parameter name */
	public static final String PARAM_PAGE_SIZE = "PageSize";

	/** Value for the PARAM_ENTRY_TYPE connector's parameter */
	public static final String PARAM_VALUE_ENTRY_USER = "User";
	/** Value for the PARAM_ENTRY_TYPE connector's parameter */
	public static final String PARAM_VALUE_ENTRY_GROUP = "Group";

	/** Constants indicating the value of PARAM_ENTRY_TYPE connector's parameter */
	private static final int ENTRY_USER = 0;
	/** Constants indicating the value of PARAM_ENTRY_TYPE connector's parameter */
	private static final int ENTRY_GROUP = 1;

	/** Active Directory maximup "page size" for the "NetQueryDisplayInformation" */
	public static final int MAX_PAGE_SIZE = 100;

	/** Exception messages */
	private static final String EXC_INVALID_ENTRY_TYPE = "Invalid ENTRY type.";
	/** Exception messages */
	private static final String EXC_MISSING_ENTRY_TYPE = "Missing parameter ENTRY type.";
	/** Exception messages */
	private static final String EXC_COULD_NOT_LOG_ON = "Could not log on. Check connector's parameters.";
	/** Exception messages */
	private static final String EXC_CANT_ADD_GLOBAL_GROUP_ON_NON_PDC = "Cannot add global group on a machine that is not Primary Domain Controller.";
	/** Exception messages */
	private static final String EXC_NO_USER_NAME_IN_DELETE = "Deleting user failed. User name is not specified.";
	/** Exception messages */
	private static final String EXC_NO_GROUP_NAME_IN_DELETE = "Deleting group failed. Group name is not specified.";
	/** Exception messages */
	private static final String EXC_NO_IS_GLOBAL_IN_DELETE = "Deleting group failed. Not specified global/local identifier.";
	/** Exception messages */
	private static final String EXC_CANT_DEL_GLOBAL_GROUP_FROM_NON_PDC = "Cannot delete global group from machine that is not Primary Domain Controller.";
	/** Exception messages */
	private static final String EXC_UNSUPPORTED_SEARCH_CRITERIA = "Unsupported Link Criteria structure.";
	/** Exception messages */
	private static final String EXC_NO_USER_NAME_IN_SET_MEMBERSHIP = "Cannot set user's membership. User name is not specified.";

	// parameter values
	/** Parameter values */
	private String mComputerName = "";
	/** Parameter values */
	private String mUserName = null;
	/** Parameter values */
	private String mPassword = null;
	/** Parameter values */
	private String mEntryTypeName = PARAM_VALUE_ENTRY_USER;
	/** Parameter values */
	private int mEntryType = ENTRY_USER;
	/** Parameter values */
	private int mPageSize = MAX_PAGE_SIZE;

	/**
	 * Indicates whether the Windows machine associated with the connector is a
	 * Primary Domain Controller.
	 */
	private boolean mIsPDC = false;

	/**
	 * Container for the connector's user entries. Only account names are hold.
	 */
	private Vector mUsers = null;

	/**
	 * Enumeration for the connector's users container.
	 */
	private Enumeration mUsersEnum = null;

	/**
	 * The resume index for retrieving users on pages.
	 */
	private int mUsersResumeIndex = 0;

	/**
	 * Container for the connector's local group entries. Only account names are
	 * hold.
	 */
	private Vector mLocalGroups = null;

	/**
	 * Enumeration for the connector's local groups container.
	 */
	private Enumeration mLocalGroupsEnum = null;

	/**
	 * Container for the connector's global group entries. Only account names
	 * are hold.
	 */
	private Vector mGlobalGroups = null;

	/**
	 * Enumeration for the connector's global groups container.
	 */
	private Enumeration mGlobalGroupsEnum = null;

	/**
	 * The resume index for retrieving global groups on pages.
	 */
	private int mGlobalGroupsResumeIndex = 0;

	/**
	 * Class constructor
	 */
	public NT4UserMetaDataConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
	}

	/** NLS String Property set */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Reads connector parameters' values. If UserName parameter is set tries to
	 * log on to the machine indicated by the ComputerName parameter.
	 * 
	 * @param aObj
	 *            This parameter is usually null but can be any type of object
	 *            the caller chooses to passon. Normally the parameter is some
	 *            kind of input stream or Reader object.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If bad parameter values are given.
	 */
	public void initialize(Object aObj) throws NT4UserMetaDataException {

		// read parameter values
		mComputerName = getParam(PARAM_COMPUTER_NAME);
		if (mComputerName != null) {
			mComputerName = mComputerName.trim();
		} else {
			mComputerName = "";
		}

		mUserName = getParam(PARAM_USER_NAME);
		if (mUserName != null) {
			mUserName = mUserName.trim();
			if (mUserName.length() == 0) {
				mUserName = null;
			}

			if (mUserName != null) {
				mPassword = getParam(PARAM_PASSWORD);
				if (mPassword != null) {
					mPassword = mPassword.trim();
				}
			}
		}

		mEntryTypeName = getParam(PARAM_ENTRY_TYPE);
		if (mEntryTypeName != null) {
			mEntryTypeName = mEntryTypeName.trim();

			if (!mEntryTypeName.equals(PARAM_VALUE_ENTRY_USER)
					&& !mEntryTypeName.equals(PARAM_VALUE_ENTRY_GROUP)) {
				throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
			} else if (mEntryTypeName.equals(PARAM_VALUE_ENTRY_USER)) {
				mEntryType = ENTRY_USER;
			} else {
				mEntryType = ENTRY_GROUP;
			}
		} else {
			throw new NT4UserMetaDataException(EXC_MISSING_ENTRY_TYPE);
		}

		String pageSizeValue = getParam(PARAM_PAGE_SIZE);
		if (pageSizeValue != null) {
			pageSizeValue = pageSizeValue.trim();
			try {
				mPageSize = Integer.parseInt(pageSizeValue);
				if ((mPageSize < 1) || (mPageSize > MAX_PAGE_SIZE)) {
					mPageSize = MAX_PAGE_SIZE;
					logmsg(sResHash.getString(
							"CONNECTOR.WINDOWSURSGRPS.BADRANGE.PAGESIZE",
							new Object[] { pageSizeValue, "" + MAX_PAGE_SIZE,
									"" + mPageSize }));
				}
			} catch (NumberFormatException e) {
				mPageSize = MAX_PAGE_SIZE;
				logmsg(sResHash.getString(
						"CONNECTOR.WINDOWSURSGRPS.INVALID.PAGESIZE",
						new Object[] { pageSizeValue, "" + mPageSize }));
			}
		} else {
			mPageSize = MAX_PAGE_SIZE;
			logmsg(sResHash
					.getString("CONNECTOR.WINDOWSURSGRPS.MISSING.PAGESIZE", ""
							+ mPageSize));
		}

		// log on to remote machine if UserName parameter is set
		if (mUserName != null) {
			boolean successfulLogOn = NTMetaData.logOn(mComputerName,
					mUserName, mPassword);
			if (!successfulLogOn) {
				throw new NT4UserMetaDataException(EXC_COULD_NOT_LOG_ON);
			}
		}

		mIsPDC = NTMetaData.isPrimaryDomainController(mComputerName);
	}

	// *************************************************************************
	// Retrieve data from Windows security database
	// *************************************************************************

	/**
	 * Reads all entries from Windows security database in a connector's
	 * container. Depending on the value of EntryType connector's parameter this
	 * method dispatches the execution to either selectUserEntries or
	 * selectGroupEntries method.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database.
	 */
	public void selectEntries() throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			selectUserEntries();
		} else if (mEntryType == ENTRY_GROUP) {
			selectGroupEntries();
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Reads all users' names from Windows database in a connector's container.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining users' data from Windows
	 *             database.
	 */
	private void selectUserEntries() throws NT4UserMetaDataException {
		mUsers = new Vector();
		mUsersResumeIndex = NTMetaData.getUsersNames(mComputerName, 0,
				mPageSize, mUsers);
		mUsersEnum = mUsers.elements();
	}

	/**
	 * Reads all groups' names from Windows database in a connector's container.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining groups' data from Windows
	 *             database.
	 */
	private void selectGroupEntries() throws NT4UserMetaDataException {
		mLocalGroups = NTMetaData.getLocalGroupsNames(mComputerName);
		mLocalGroupsEnum = mLocalGroups.elements();

		if (mIsPDC) {
			mGlobalGroups = new Vector();
			mGlobalGroupsResumeIndex = NTMetaData.getGlobalGroupsNames(
					mComputerName, 0, mPageSize, mGlobalGroups);
			mGlobalGroupsEnum = mGlobalGroups.elements();
		}
	}

	/**
	 * Retrieves the next entry from the connector's containers. Depending on
	 * the value of EntryType connector's parameter this method dispatches the
	 * execution to either getNextUserEntry or getNextGroupEntry method.
	 * 
	 * @return The consecutive Entry object.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows database
	 *             or the EntryType value is invalid.
	 */
	public Entry getNextEntry() throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			return getNextUserEntry();
		} else if (mEntryType == ENTRY_GROUP) {
			return getNextGroupEntry();
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Given a user account name retrieves the corresponding user entry from
	 * Windows database.
	 * 
	 * @param aUserName
	 *            String
	 * 
	 * @return The constructed user Entry object.
	 * @throws NT4UserMetaDataException
	 *             If the specified user account cannot be found.
	 */
	private Entry retrieveUserEntry(String aUserName)
			throws NT4UserMetaDataException {
		UserInfo userInfo = NTMetaData.userGetInfo(mComputerName, aUserName);

		Entry userEntry = new Entry();
		UserInfo.createAndAddUserBasicAttributes(userEntry, userInfo);

		Vector localGroupNames = NTMetaData.getUserLocalGroups(mComputerName,
				aUserName);
		UserInfo
				.createAndAddUserLocalGroupAttribute(userEntry, localGroupNames);

		Vector globalGroupNames = NTMetaData.getUserGlobalGroups(mComputerName,
				aUserName);
		UserInfo.createAndAddUserGlobalGroupAttribute(userEntry,
				globalGroupNames);

		return userEntry;
	}

	/**
	 * Retrieves the next user entry from the connector's user container.
	 * 
	 * @return The consecutive user Entry object.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database.
	 */
	private Entry getNextUserEntry() throws NT4UserMetaDataException {
		Entry entry = null;

		if ((!mUsersEnum.hasMoreElements()) && (mUsersResumeIndex > 0)) {
			mUsers.clear();
			mUsersResumeIndex = NTMetaData.getUsersNames(mComputerName,
					mUsersResumeIndex, mPageSize, mUsers);
			mUsersEnum = mUsers.elements();
		}

		if (mUsersEnum.hasMoreElements()) {
			String userName = (String) mUsersEnum.nextElement();
			entry = retrieveUserEntry(userName);
		}

		return entry;
	}

	/**
	 * Retrieves the next group entry from the connector's groups containers. By
	 * this method sequentially are obtained all local groups and all global
	 * groups (if Primary Domain Controller is addressed). Dispatches the
	 * execution to either getNextLocalGroupEntry or getNextGlobalGroupEntry
	 * method.
	 * 
	 * @return The consecutive group Entry object.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database.
	 */
	private Entry getNextGroupEntry() throws NT4UserMetaDataException {
		Entry entry = null;

		if (mLocalGroupsEnum.hasMoreElements()) {
			String localGroupName = (String) mLocalGroupsEnum.nextElement();
			entry = retrieveLocalGroupEntry(localGroupName);
		} else if (mIsPDC) {
			if ((!mGlobalGroupsEnum.hasMoreElements())
					&& (mGlobalGroupsResumeIndex > 0)) {
				mGlobalGroups.clear();
				mGlobalGroupsResumeIndex = NTMetaData.getGlobalGroupsNames(
						mComputerName, mGlobalGroupsResumeIndex, mPageSize,
						mGlobalGroups);
				mGlobalGroupsEnum = mGlobalGroups.elements();
			}

			if (mGlobalGroupsEnum.hasMoreElements()) {
				String globalGroupName = (String) mGlobalGroupsEnum
						.nextElement();
				entry = retrieveGlobalGroupEntry(globalGroupName);
			}
		}

		return entry;
	}

	/**
	 * Given a local group account name retrieves the corresponding local group
	 * entry from Windows database.
	 * 
	 * @param aLocalGroupName
	 *            String
	 * 
	 * @return The constructed local group Entry object.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If the specified local group account cannot be found.
	 */
	private Entry retrieveLocalGroupEntry(String aLocalGroupName)
			throws NT4UserMetaDataException {
		GroupInfo groupInfo = NTMetaData.localGroupGetInfo(mComputerName,
				aLocalGroupName);
		Vector userNames = NTMetaData.getLocalGroupUsers(mComputerName,
				aLocalGroupName);
		Vector groupNames = NTMetaData.getLocalGroupGlobalGroups(mComputerName,
				aLocalGroupName);

		Entry localGroupEntry = new Entry();
		GroupInfo.populateGroupEntryAttributes(localGroupEntry, groupInfo,
				userNames, groupNames);

		return localGroupEntry;
	}

	/**
	 * Given a global group account name retrieves the corresponding global
	 * group entry from Windows database.
	 * 
	 * @param aGlobalGroupName
	 *            String
	 * 
	 * @return The constructed global group Entry object.
	 * @throws NT4UserMetaDataException
	 *             If the specified global group account cannot be found.
	 */
	private Entry retrieveGlobalGroupEntry(String aGlobalGroupName)
			throws NT4UserMetaDataException {
		GroupInfo groupInfo = NTMetaData.globalGroupGetInfo(mComputerName,
				aGlobalGroupName);
		Vector userNames = NTMetaData.getGlobalGroupUsers(mComputerName,
				aGlobalGroupName);

		Entry globalGroupEntry = new Entry();
		GroupInfo.populateGroupEntryAttributes(globalGroupEntry, groupInfo,
				userNames, null);

		return globalGroupEntry;
	}

	/**
	 * Retrieves a single entry object matching the given search criteria
	 * parameter. Depending on the value of EntryType connector's parameter this
	 * method dispatches the execution to either findUserEntry or findGroupEntry
	 * method.
	 * 
	 * @param aRscSearchCriteria
	 *            The search criteria to locate the entry. Should contain just
	 *            one rscSearch element (holding the entry's account name value)
	 *            when operating with Users and two rscSearch elements (1st -
	 *            group's account name and 2nd - global/local indicator) when
	 *            operating with Groups.
	 * @return Entry
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database. If unsupported search criteria is given.
	 */
	public Entry findEntry(SearchCriteria aRscSearchCriteria)
			throws NT4UserMetaDataException {
		clearFindEntries();

		if (mEntryType == ENTRY_USER) {
			return findUserEntry(aRscSearchCriteria);
		} else if (mEntryType == ENTRY_GROUP) {
			return findGroupEntry(aRscSearchCriteria);
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Retrieves a single user entry object matching the given search criteria
	 * parameter.
	 * 
	 * @param aRscSearchCriteria
	 *            The search criteria should contain just one rscSearch element
	 *            with the following values (name: UserInfo.USER_ATTR_USER_NAME;
	 *            match: SearchCriteria.EXCACT; value: the name of the user
	 *            account we look for).
	 * @return The Entry object matching the given criteria parameter or null if
	 *         such account doesn't exist.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database. If unsupported search criteria parameter is given.
	 */
	private Entry findUserEntry(SearchCriteria aRscSearchCriteria)
			throws NT4UserMetaDataException {
		String entryAttrName = aRscSearchCriteria.getFirstCriteriaName();
		int match = aRscSearchCriteria.getFirstCriteriaMatch();
		String entryAttrValue = aRscSearchCriteria.getFirstCriteriaValue();

		if ((!entryAttrName.equals(UserInfo.USER_ATTR_USER_NAME))
				|| (match != SearchCriteria.EXCACT)) {
			throw new NT4UserMetaDataException(EXC_UNSUPPORTED_SEARCH_CRITERIA);
		}

		try {
			return retrieveUserEntry(entryAttrValue);
		} catch (NT4UserMetaDataException e) {
			return null;
		}
	}

	/**
	 * Retrieves a single group entry object matching the given search criteria
	 * parameter.
	 * 
	 * @param aRscSearchCriteria
	 *            The search criteria should contain exactly two rscSearch
	 *            elements with the following values: 1st(name:
	 *            GroupInfo.GROUP_ATTR_GROUP_NAME; match: SearchCriteria.EXCACT;
	 *            value: the name of the group account we look for), 2nd(name:
	 *            GroupInfo.GROUP_ATTR_IS_GLOBAL, match: SearchCriteria.EXCACT;
	 *            value: Boolean indicating true for global and false for local
	 *            group).
	 * 
	 * @return The Entry object matching the given criteria parameter or null if
	 *         such account doesn't exist.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while obtaining data from Windows
	 *             database. If unsupported search criteria parameter is given.
	 */
	private Entry findGroupEntry(SearchCriteria aRscSearchCriteria)
			throws NT4UserMetaDataException {
		com.ibm.di.server.SearchCriteria.rscSearch accountName = aRscSearchCriteria
				.getCriteria(0);
		com.ibm.di.server.SearchCriteria.rscSearch isGlobal = aRscSearchCriteria
				.getCriteria(1);

		if ((!accountName.name.equals(GroupInfo.GROUP_ATTR_GROUP_NAME))
				|| (accountName.match != SearchCriteria.EXCACT)
				|| (accountName.value == null)
				|| (!isGlobal.name.equals(GroupInfo.GROUP_ATTR_IS_GLOBAL))
				|| (isGlobal.match != SearchCriteria.EXCACT)
				|| (isGlobal.value == null)) {

			throw new NT4UserMetaDataException(EXC_UNSUPPORTED_SEARCH_CRITERIA);
		}

		boolean isGlobalGroup = Boolean.valueOf(isGlobal.value.toString())
				.booleanValue();
		try {
			if (isGlobalGroup) {
				return retrieveGlobalGroupEntry((String) accountName.value);
			} else {
				return retrieveLocalGroupEntry((String) accountName.value);
			}
		} catch (NT4UserMetaDataException e) {
			return null;
		}
	}

	// *************************************************************************
	// Insert data into Windows security database
	// *************************************************************************

	/**
	 * Inserts given entry (user or group) into Windows security database.
	 * Depending on the value of EntryType connector's parameter this method
	 * dispatches the execution to either putUser or putGroup method.
	 * 
	 * @param aEntry
	 *            The Entry object to be inserted.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while inserting data into Windows
	 *             database.
	 */
	public void putEntry(Entry aEntry) throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			putUser(aEntry);
		} else if (mEntryType == ENTRY_GROUP) {
			putGroup(aEntry);
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Inserts given user entry into Windows security database.
	 * 
	 * @param aUserEntry
	 *            The user Entry object to be inserted.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurred while inserting data into Windows
	 *             database.
	 */
	private void putUser(Entry aUserEntry) throws NT4UserMetaDataException {
		// retrieve user data from the entry
		UserInfo userInfo = new UserInfo(aUserEntry);

		// set default values, if needed
		if (userInfo.getFlags() == null) {
			userInfo.setFlags(UserInfo.DEFAULT_FLAGS_VALUE);
		}
		if (userInfo.getAccountExpDate() == null) {
			userInfo.setAccountExpDate(UserInfo.ACC_NEVER_EXPIRES);
		}

		// add the user
		NTMetaData.userAdd(mComputerName, userInfo);

		// make the user member of all groups specified
		addUserToGroups(userInfo.getUserName(), aUserEntry);
	}

	/**
	 * Makes the user specified member of all groups specified and sets his
	 * PrimaryGroup if the user is domain user.
	 * 
	 * @param aUserName
	 *            Then name of the User which group membership will be set.
	 * @param aUserEntry
	 *            The entry structure (of type User) containing the lists of the
	 *            user's global and local groups.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while setting user's membership in Windows
	 *             database.
	 */
	private void addUserToGroups(String aUserName, Entry aUserEntry)
			throws NT4UserMetaDataException {
		// add the user to all local groups specified
		Attribute localGroupsAttr = aUserEntry
				.getAttribute(UserInfo.USER_ATTR_LOCAL_GROUPS);
		if (localGroupsAttr != null) {
			Vector userVector = new Vector();
			userVector.addElement(aUserName);

			int localGroupsCnt = localGroupsAttr.size();
			for (int i = 0; i < localGroupsCnt; i++) {
				String localGroupName = (String) localGroupsAttr.getValue(i);
				NTMetaData.localGroupAddUsers(mComputerName, localGroupName,
						userVector);
			}
		}

		// if the user is a domain user
		if (mIsPDC) {
			// set user's Primary Group
			Attribute primaryGroupAttr = aUserEntry
					.getAttribute(UserInfo.USER_ATTR_PRIMARY_GROUP);
			if (primaryGroupAttr != null) {
				String primaryGroup = (String) primaryGroupAttr.getValue(0);
				if (primaryGroup != null) {
					NTMetaData.userSetPrimaryGroup(mComputerName, aUserName,
							primaryGroup);
				}
			}

			// add the user to all global groups specified
			Attribute globalGroupsAttr = aUserEntry
					.getAttribute(UserInfo.USER_ATTR_GLOBAL_GROUPS);
			if (globalGroupsAttr != null) {
				Vector globalGroups = new Vector();
				int globalGroupsCnt = globalGroupsAttr.size();
				for (int i = 0; i < globalGroupsCnt; i++) {
					globalGroups.addElement(globalGroupsAttr.getValue(i));
				}

				if (globalGroups.size() > 0) {
					NTMetaData.userSetGlobalGroups(mComputerName, aUserName,
							globalGroups);
				}
			}
		}
	}

	/**
	 * Inserts given group entry into Windows security database. Depending on
	 * whether the group specified by the given entry is global or local this
	 * method dispatches the execution to either putGlobalGroup or putLocalGroup
	 * method.
	 * 
	 * @param aGroupEntry
	 *            The group Entry object to be inserted.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while inserting data into Windows
	 *             database.
	 */
	private void putGroup(Entry aGroupEntry) throws NT4UserMetaDataException {
		// retrieve group data from the entry
		GroupInfo groupInfo = new GroupInfo(aGroupEntry);

		Vector users = null;
		Attribute usersAttr = aGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_USERS);
		if (usersAttr != null) {
			users = new Vector();
			int usersCnt = usersAttr.size();
			for (int i = 0; i < usersCnt; i++) {
				users.addElement(usersAttr.getValue(i));
			}
		}

		Vector groups = null;
		Attribute groupsAttr = aGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_GROUPS);
		if (groupsAttr != null) {
			groups = new Vector();
			int groupsCnt = groupsAttr.size();
			for (int i = 0; i < groupsCnt; i++) {
				groups.addElement(groupsAttr.getValue(i));
			}
		}

		// add the group in Windows security database
		if (groupInfo.getIsGlobal().booleanValue() == true) {
			putGlobalGroup(groupInfo, users);
		} else {
			putLocalGroup(groupInfo, users, groups);
		}
	}

	/**
	 * Inserts given global group entry into Windows security database.
	 * 
	 * @param aGroupInfo
	 *            The base data of the group to be inserted.
	 * @param aUsers
	 *            Vector containing the names of the users members of the given
	 *            global group.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while inserting data into Windows
	 *             database.
	 */
	private void putGlobalGroup(GroupInfo aGroupInfo, Vector aUsers)
			throws NT4UserMetaDataException {
		if (!mIsPDC) {
			throw new NT4UserMetaDataException(
					EXC_CANT_ADD_GLOBAL_GROUP_ON_NON_PDC);
		}

		// add the global group in Windows security database
		NTMetaData.globalGroupAdd(mComputerName, aGroupInfo);

		// add group's users
		if ((aUsers != null) && (aUsers.size() > 0)) {
			NTMetaData.globalGroupSetUsers(mComputerName, aGroupInfo
					.getGroupName(), aUsers);
		}
	}

	/**
	 * Inserts given local group entry into Windows security database.
	 * 
	 * @param aGroupInfo
	 *            the base data of the group to be inserted.
	 * @param aUsers
	 *            Vector containing the names of the users members of the given
	 *            local group.
	 * @param aGlobalGroups
	 *            Vector containing the names of the global groups members of
	 *            the given local group.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occured while inserting data into Windows
	 *             database.
	 */
	private void putLocalGroup(GroupInfo aGroupInfo, Vector aUsers,
			Vector aGlobalGroups) throws NT4UserMetaDataException {
		// add the local group in Windows security database
		NTMetaData.localGroupAdd(mComputerName, aGroupInfo);

		// add group's members
		if ((aUsers != null) || (aGlobalGroups != null)) {
			Vector members = new Vector();

			if (aUsers != null) {
				int usersCnt = aUsers.size();
				for (int i = 0; i < usersCnt; i++) {
					members.addElement(aUsers.elementAt(i));
				}
			}

			if (aGlobalGroups != null) {
				int groupsCnt = aGlobalGroups.size();
				for (int i = 0; i < groupsCnt; i++) {
					members.addElement(aGlobalGroups.elementAt(i));
				}
			}

			if (members.size() > 0) {
				NTMetaData.localGroupSetMembers(mComputerName, aGroupInfo
						.getGroupName(), members);
			}
		}
	}

	// *************************************************************************
	// Modify data in Windows security database
	// *************************************************************************

	/**
	 * Modifies in Windows database the entry identified by the
	 * aRscSearchCriteria parameter with the data given in the aEntry parameter.
	 * Depending on the value of EntryType connector's parameter this method
	 * dispatches the execution to either modUserEntry or modGroupEntry method.
	 * 
	 * @param aEntry
	 *            Entry object containing the new data.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to locate this
	 *            entry.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying data into Windows
	 *             database.
	 */
	public void modEntry(Entry aEntry, SearchCriteria aRscSearchCriteria)
			throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			modUserEntry(aEntry, aRscSearchCriteria);
		} else if (mEntryType == ENTRY_GROUP) {
			modGroupEntry(aEntry, aRscSearchCriteria);
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Modifies in Windows database the user entry identified by the
	 * aRscSearchCriteria parameter with the data given in the aUserEntry
	 * parameter.
	 * 
	 * @param aUserEntry
	 *            Entry object containing the new user data.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to locate this
	 *            user entry. It should contain just one rscSearch element with
	 *            the following values (name: UserInfo.USER_ATTR_USER_NAME;
	 *            match: SearchCriteria.EXCACT; value: the name of the user
	 *            account to modify.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying user data in Windows
	 *             database. If unsupported search criteria parameter is given.
	 */
	private void modUserEntry(Entry aUserEntry,
			SearchCriteria aRscSearchCriteria) throws NT4UserMetaDataException {
		String entryAttrName = aRscSearchCriteria.getFirstCriteriaName();
		int match = aRscSearchCriteria.getFirstCriteriaMatch();
		String entryAttrValue = aRscSearchCriteria.getFirstCriteriaValue();

		if ((!entryAttrName.equals(UserInfo.USER_ATTR_USER_NAME))
				|| (match != SearchCriteria.EXCACT)) {
			throw new NT4UserMetaDataException(EXC_UNSUPPORTED_SEARCH_CRITERIA);
		}

		// get current basic user attributes
		UserInfo oldUserInfo = NTMetaData.userGetInfo(mComputerName,
				entryAttrValue);

		// construct the new basic user attributes
		UserInfo userInfo = new UserInfo(aUserEntry);
		userInfo.completeUserData(oldUserInfo);

		// modify basic user attributes
		NTMetaData.userSetInfo(mComputerName, entryAttrValue, userInfo);

		// modify user's group membership
		Attribute localGroupsAttr = aUserEntry
				.getAttribute(UserInfo.USER_ATTR_LOCAL_GROUPS);
		Attribute globalGroupsAttr = aUserEntry
				.getAttribute(UserInfo.USER_ATTR_GLOBAL_GROUPS);
		Attribute primaryGroupAttr = aUserEntry
				.getAttribute(UserInfo.USER_ATTR_PRIMARY_GROUP);

		if (localGroupsAttr != null) {
			// remove the user from all local groups he is member of
			Vector localGroupNames = NTMetaData.getUserLocalGroups(
					mComputerName, userInfo.getUserName());
			Vector userVector = new Vector();
			userVector.addElement(userInfo.getUserName());
			int localGroupNamesCnt = localGroupNames.size();
			for (int i = 0; i < localGroupNamesCnt; i++) {
				String localGroupName = (String) localGroupNames.elementAt(i);
				NTMetaData.localGroupDelUsers(mComputerName, localGroupName,
						userVector);
			}
		}

		if (mIsPDC && (globalGroupsAttr != null)) {
			// if the user is domain user clear all its global groups except its
			// current Primary Group
			String primaryGroupName = NTMetaData.userGetPrimaryGroup(
					mComputerName, userInfo.getUserName());

			Vector primaryGroupVector = new Vector();
			primaryGroupVector.addElement(primaryGroupName);
			NTMetaData.userSetGlobalGroups(mComputerName, userInfo
					.getUserName(), primaryGroupVector);
		}

		if ((localGroupsAttr != null)
				|| (mIsPDC && ((globalGroupsAttr != null) || (primaryGroupAttr != null)))) {
			// add the user to all groups specified
			addUserToGroups(userInfo.getUserName(), aUserEntry);
		}
	}

	/**
	 * Modifies in Windows database the group entry identified by the
	 * aRscSearchCriteria parameter with the data given in the aGroupEntry
	 * parameter. Depending on whether the specified group is global or local
	 * this method dispatches the execution to either modLocalGroupEntry or
	 * modGlobalGroupEntry method.
	 * 
	 * @param aGroupEntry
	 *            Entry object containing the new group data.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to locate this
	 *            group entry.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying group data in Windows
	 *             database.
	 */
	private void modGroupEntry(Entry aGroupEntry,
			SearchCriteria aRscSearchCriteria) throws NT4UserMetaDataException {
		com.ibm.di.server.SearchCriteria.rscSearch accountName = aRscSearchCriteria
				.getCriteria(0);
		com.ibm.di.server.SearchCriteria.rscSearch isGlobal = aRscSearchCriteria
				.getCriteria(1);

		if ((!accountName.name.equals(GroupInfo.GROUP_ATTR_GROUP_NAME))
				|| (accountName.match != SearchCriteria.EXCACT)
				|| (accountName.value == null)
				|| (!isGlobal.name.equals(GroupInfo.GROUP_ATTR_IS_GLOBAL))
				|| (isGlobal.match != SearchCriteria.EXCACT)
				|| (isGlobal.value == null)) {

			throw new NT4UserMetaDataException(EXC_UNSUPPORTED_SEARCH_CRITERIA);
		}

		boolean isGlobalGroup = Boolean.valueOf(isGlobal.value.toString())
				.booleanValue();
		if (isGlobalGroup) {
			modGlobalGroupEntry(aGroupEntry, accountName);
		} else {
			modLocalGroupEntry(aGroupEntry, accountName);
		}
	}

	/**
	 * Modifies in Windows database the global group entry identified by the
	 * aAccountName parameter with the data given in the aGlobalGroupEntry
	 * parameter.
	 * 
	 * @param aGlobalGroupEntry
	 *            Entry object containing the new data of the global group.
	 * @param aAccountName
	 *            Contains the account name of the global group to be modified.
	 * @throws NT4UserMetaDataException
	 *             If an error occured while modifying global group data in
	 *             Windows database.
	 */
	private void modGlobalGroupEntry(Entry aGlobalGroupEntry,
			com.ibm.di.server.SearchCriteria.rscSearch aAccountName)
			throws NT4UserMetaDataException {
		// get current basic global group attributes
		GroupInfo oldGroupInfo = NTMetaData.globalGroupGetInfo(mComputerName,
				(String) aAccountName.value);

		// construct the new basic global group attributes
		GroupInfo groupInfo = new GroupInfo(aGlobalGroupEntry);
		groupInfo.completeGroupData(oldGroupInfo);

		// modify basic global group attributes
		NTMetaData.globalGroupSetInfo(mComputerName,
				(String) aAccountName.value, groupInfo);

		Attribute usersAttr = aGlobalGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_USERS);

		if (usersAttr != null) {
			int usersCnt = usersAttr.size();

			if (usersCnt > 0) {
				// add the new members (the users specified)
				Vector users = new Vector();
				for (int i = 0; i < usersCnt; i++) {
					users.addElement(usersAttr.getValue(i));
				}

				NTMetaData.globalGroupSetUsers(mComputerName, groupInfo
						.getGroupName(), users);
			} else {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.WINDOWSURSGRPS.GLOBALGRP.EMPTY"));
				}
				NTMetaData.globalGroupSetUsers(mComputerName, groupInfo
						.getGroupName(), new Vector());
			}
		}
	}

	/**
	 * Modifies in Windows database the local group entry identified by the
	 * aAccountName parameter with the data given in the aLocalGroupEntry
	 * parameter.
	 * 
	 * @param aLocalGroupEntry
	 *            Entry object containing the new data of the local group.
	 * @param aAccountName
	 *            Contains the account name of the local group to be modified.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occured while modifying local group data in
	 *             Windows database.
	 */
	private void modLocalGroupEntry(Entry aLocalGroupEntry,
			com.ibm.di.server.SearchCriteria.rscSearch aAccountName)
			throws NT4UserMetaDataException {
		// get current basic local group attributes
		GroupInfo oldGroupInfo = NTMetaData.localGroupGetInfo(mComputerName,
				(String) aAccountName.value);

		// construct the new basic local group attributes
		GroupInfo groupInfo = new GroupInfo(aLocalGroupEntry);
		groupInfo.completeGroupData(oldGroupInfo);

		// modify basic local group attributes
		NTMetaData.localGroupSetInfo(mComputerName,
				(String) aAccountName.value, groupInfo);

		// modify users and groups membership
		Attribute usersAttr = aLocalGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_USERS);
		Attribute groupsAttr = aLocalGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_GROUPS);

		if ((usersAttr != null) || (groupsAttr != null)) {
			// get current membership
			Vector oldUsers = NTMetaData.getLocalGroupUsers(mComputerName,
					groupInfo.getGroupName());
			Vector oldGroups = NTMetaData.getLocalGroupGlobalGroups(
					mComputerName, groupInfo.getGroupName());

			Vector members = new Vector();

			if (usersAttr != null) {
				// add the new users
				int usersCnt = usersAttr.size();
				for (int i = 0; i < usersCnt; i++) {
					members.addElement(usersAttr.getValue(i));
				}
			} else {
				// users have not changed - get old users
				if (oldUsers != null) {
					int oldUsersCnt = oldUsers.size();
					for (int i = 0; i < oldUsersCnt; i++) {
						members.addElement(oldUsers.elementAt(i));
					}
				}
			}

			if (groupsAttr != null) {
				// add the new groups
				int groupsCnt = groupsAttr.size();
				for (int i = 0; i < groupsCnt; i++) {
					members.addElement(groupsAttr.getValue(i));
				}
			} else {
				// groups have not changed - get old groups
				if (oldGroups != null) {
					int oldGroupsCnt = oldGroups.size();
					for (int i = 0; i < oldGroupsCnt; i++) {
						members.addElement(oldGroups.elementAt(i));
					}
				}
			}

			if (members.size() > 0) {
				NTMetaData.localGroupSetMembers(mComputerName, groupInfo
						.getGroupName(), members);
			} else {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.WINDOWSURSGRPS.LOCALGRP.EMPTY"));
				}
				NTMetaData.localGroupSetMembers(mComputerName, groupInfo
						.getGroupName(), new Vector());
			}
		}
	}

	// *************************************************************************
	// Delete data from Windows security database
	// *************************************************************************

	/**
	 * Deletes given entry (user or group) from Windows security database.
	 * Depending on the value of EntryType connector's parameter this method
	 * dispatches the execution to either deleteUserEntry or deleteGroupEntry
	 * method.
	 * 
	 * @param aEntry
	 *            The entry object to be deleted.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to locate this
	 *            entry.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occured while deleting data from Windows
	 *             database.
	 */
	public void deleteEntry(Entry aEntry, SearchCriteria aRscSearchCriteria)
			throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			deleteUserEntry(aEntry, aRscSearchCriteria);
		} else if (mEntryType == ENTRY_GROUP) {
			deleteGroupEntry(aEntry, aRscSearchCriteria);
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Deletes given user entry from Windows security database.
	 * 
	 * @param aUserEntry
	 *            The user entry object to be deleted.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to find this
	 *            user entry.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occured while deleting user account from Windows
	 *             database.
	 */
	private void deleteUserEntry(Entry aUserEntry,
			SearchCriteria aRscSearchCriteria) throws NT4UserMetaDataException {
		Attribute userNameAttr = aUserEntry
				.getAttribute(UserInfo.USER_ATTR_USER_NAME);

		if (userNameAttr == null) {
			throw new NT4UserMetaDataException(EXC_NO_USER_NAME_IN_DELETE);
		}

		String userName = (String) userNameAttr.getValue(0);
		NTMetaData.userDel(mComputerName, userName);
	}

	/**
	 * Deletes given group entry from Windows security database.
	 * 
	 * @param aGroupEntry
	 *            The group entry object to be deleted.
	 * @param aRscSearchCriteria
	 *            The search criteria used by the assembly line to find this
	 *            group entry.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If an error occured while deleting group account from Windows
	 *             database.
	 */
	private void deleteGroupEntry(Entry aGroupEntry,
			SearchCriteria aRscSearchCriteria) throws NT4UserMetaDataException {
		// get group name
		Attribute groupNameAttr = aGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_GROUP_NAME);
		if (groupNameAttr == null) {
			throw new NT4UserMetaDataException(EXC_NO_GROUP_NAME_IN_DELETE);
		}
		String groupName = (String) groupNameAttr.getValue(0);

		// get global/local identifier
		Attribute isGlobalAttr = aGroupEntry
				.getAttribute(GroupInfo.GROUP_ATTR_IS_GLOBAL);
		if (isGlobalAttr == null) {
			throw new NT4UserMetaDataException(EXC_NO_IS_GLOBAL_IN_DELETE);
		}
		Boolean isGlobal = (Boolean) isGlobalAttr.getValue(0);

		// delete the group
		if (isGlobal.booleanValue() == false) {
			NTMetaData.localGroupDel(mComputerName, groupName);
		} else {
			if (mIsPDC) {
				NTMetaData.globalGroupDel(mComputerName, groupName);
			} else {
				throw new NT4UserMetaDataException(
						EXC_CANT_DEL_GLOBAL_GROUP_FROM_NON_PDC);
			}
		}
	}

	// *************************************************************************
	// Query Schema
	// *************************************************************************

	/**
	 * Retrieves connector entry's structure. Depending on the value of
	 * EntryType connector's parameter this method dispatches the execution to
	 * either UserInfo.queryUserSchema or GroupInfo.queryGroupSchema method.
	 * 
	 * @param aObj
	 *            Object
	 * 
	 * @return Vector of elements of type Entry describing each attribute's
	 *         structure.
	 * 
	 * @throws NT4UserMetaDataException
	 *             If the value of the private member mEntryType is invalid.
	 */
	public Object querySchema(Object aObj) throws NT4UserMetaDataException {
		if (mEntryType == ENTRY_USER) {
			return UserInfo.queryUserSchema();
		} else if (mEntryType == ENTRY_GROUP) {
			return GroupInfo.queryGroupSchema();
		} else {
			throw new NT4UserMetaDataException(EXC_INVALID_ENTRY_TYPE);
		}
	}

	/**
	 * Version information.
	 * @return version information.
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I% 20%E%";
	}

}
