/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.NT4UserMetaDataConnector;

import java.util.Vector;

/**
 * This class represents the Windows Users and Groups Connector JNI layer. It
 * encapsulates all native methods to access the WinAPI functions.
 */
public class NTMetaData {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// *************************************************************************
	// Server routines
	// *************************************************************************

	/**
	 * Tries to log on an arbitrary Windows machine. Can be local or PDC machine
	 * in the domain as well as machine from another domain.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine to log on.
	 * @param aUserName
	 *            The user name of the account to log on with.
	 * @param aPassword
	 *            The password of the account to log on with.
	 * @return "true" if the log on operation is successful, "false" otherwise.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while trying to log on.
	 */
	public static native boolean logOn(String aComputerName, String aUserName,
			String aPassword) throws NT4UserMetaDataException;

	/**
	 * Checks if the machine given is a Primary Domain Controller machine.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine to check for PDC.
	 * @return "true" if the machine specified is PDC, "false" otherwise.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native boolean isPrimaryDomainController(String aComputerName)
			throws NT4UserMetaDataException;

	// *************************************************************************
	// Users routines
	// *************************************************************************

	/**
	 * Retrieves all information accessible for the user specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            resides.
	 * @param aUserName
	 *            The name of the user (the account name) which information will
	 *            be retrieved.
	 * @return com.ibm.di.connector.NT4UserMetaDataConnector.UserInfo
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native UserInfo userGetInfo(String aComputerName,
			String aUserName) throws NT4UserMetaDataException;

	/**
	 * Retrieves a "paged" list of the users accounts on the specified machine.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine which users will be
	 *            retrieved.
	 * @param aStartIndex
	 *            A resume-index for enumerating users. Specify "0" at the first
	 *            call; specify function's return value on subsequent calls.
	 * @param aEntriesRequested
	 *            The number of Entries requested, i.e. the "page size". Should
	 *            be between 1 and 100.
	 * @param aUsersNames
	 *            a java.util.Vector that will be populated with String elements
	 *            each one specifying a user account name. All previous vector
	 *            data is erased.
	 * @return int The resume-index that should be used on the next function's
	 *         call, i.e. this value should be passed to the aStartIndex
	 *         parameter on the next call. A value of "0" specifies that there
	 *         are no more Entries to read.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native int getUsersNames(String aComputerName,
			int aStartIndex, int aEntriesRequested, Vector aUsersNames)
			throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all local groups that the specified user is member
	 * of.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            and the local group accounts reside.
	 * @param aUserName
	 *            The name of the user (the account name) which local groups
	 *            will be retrieved.
	 * @return Vector of String elements each one specifying a local group
	 *         account name.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native Vector getUserLocalGroups(String aComputerName,
			String aUserName) throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all global groups that the specified user is member
	 * of.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            resides.
	 * @param aUserName
	 *            The name of the user (the account name) which global groups
	 *            will be retrieved.
	 * @return Vector of String elements each one specifying a global group
	 *         account name.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native Vector getUserGlobalGroups(String aComputerName,
			String aUserName) throws NT4UserMetaDataException;

	/**
	 * Adds a user account to the machine specified. If PDC machine is specified
	 * domain user will be added.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            will be added.
	 * @param aUserInfo
	 *            UserInfo structure describing the user that will be added
	 *            (along with all his details).
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding the user in Windows security
	 *             database.
	 */
	public static native void userAdd(String aComputerName, UserInfo aUserInfo)
			throws NT4UserMetaDataException;

	/**
	 * Removes the specified user account from the specified machine.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            will be removed from.
	 * @param aUserName
	 *            The name of the user that will be removed.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the user from Windows
	 *             security database.
	 */
	public static native void userDel(String aComputerName, String aUserName)
			throws NT4UserMetaDataException;

	/**
	 * Modifies user account properties. Rename functionality is included in
	 * this method.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where is the user
	 *            account that will be modified.
	 * @param aUserName
	 *            The name of the user that will be modified.
	 * @param aUserInfo
	 *            UserInfo structure describing the new user's details.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying the user in Windows
	 *             security database.
	 */
	public static native void userSetInfo(String aComputerName,
			String aUserName, UserInfo aUserInfo)
			throws NT4UserMetaDataException;

	/**
	 * Sets user's global group membership. This method removes user's
	 * membership in all global groups and makes the user member of just the
	 * global groups specified. Can be applied just on PDC machines.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            resides (should be a PDC machine).
	 * @param aUserName
	 *            The name of the user which global group membership will be
	 *            set.
	 * @param aGlobalGroups
	 *            Vector of String elements each one specifying the name of a
	 *            global group account.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while setting user's global groups
	 *             membership in Windows security database.
	 */
	public static native void userSetGlobalGroups(String aComputerName,
			String aUserName, Vector aGlobalGroups)
			throws NT4UserMetaDataException;

	/**
	 * Sets user's Primary Group. Applies just on PDC machines. If the user is
	 * not member of the global group specified, it is first added as member of
	 * this global group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            resides (should be a PDC machine).
	 * @param aUserName
	 *            The name of the user which Primary Group will be set.
	 * @param aPrimaryGroup
	 *            The name of the user's new Primary Group (should be a global
	 *            group).
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while setting user's Primary Group in
	 *             Windows security database.
	 */
	public static native void userSetPrimaryGroup(String aComputerName,
			String aUserName, String aPrimaryGroup)
			throws NT4UserMetaDataException;

	/**
	 * Retrieves user's Primary Group. Applies just on PDC machines.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the user account
	 *            resides (should be a PDC machine).
	 * @param aUserName
	 *            The name of the user which Primary Group will be retrieved.
	 * @return String
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while retrieving user's Primary Group from
	 *             Windows security database.
	 */
	public static native String userGetPrimaryGroup(String aComputerName,
			String aUserName) throws NT4UserMetaDataException;

	// *************************************************************************
	// Global Groups routines
	// *************************************************************************

	/**
	 * Retrieves all information accessible for the global group specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account resides.
	 * @param aGlobalGroupName
	 *            The name of the global group (the account name) which
	 *            information will be retrieved.
	 * @return com.ibm.di.connector.NT4UserMetaDataConnector.GroupInfo
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native GroupInfo globalGroupGetInfo(String aComputerName,
			String aGlobalGroupName) throws NT4UserMetaDataException;

	/**
	 * Retrieves a "paged" list of the global groups accounts on the specified
	 * machine (should be PDC machine). These are in fact the global groups
	 * accessible in the whole domain.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine which global
	 *            groups will be retrieved.
	 * @param aStartIndex
	 *            A resume-index for enumerating global groups. Specify "0" at
	 *            the first call; specify function's return value on subsequent
	 *            calls.
	 * @param aEntriesRequested
	 *            The number of Entries requested, i.e. the "page size". Should
	 *            be between 1 and 100.
	 * @param aGroupsNames
	 *            a java.util.Vector that will be populated with String elements
	 *            each one specifying a global group account name. All previous
	 *            vector data is erased.
	 * @return int The resume-index that should be used on the next function's
	 *         call, i.e. this value should be passed to the aStartIndex
	 *         parameter on the next call. A value of "0" specifies that there
	 *         are no more Entries to read.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native int getGlobalGroupsNames(String aComputerName,
			int aStartIndex, int aEntriesRequested, Vector aGroupsNames)
			throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all users that are members of the global group
	 * specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account resides.
	 * @param aGlobalGroupName
	 *            The name of the global group (the account name) which users
	 *            will be retrieved.
	 * @return Vector of String elements each one specifying a user account
	 *         name.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native Vector getGlobalGroupUsers(String aComputerName,
			String aGlobalGroupName) throws NT4UserMetaDataException;

	/**
	 * Adds a global group account to the machine specified (should be PDC
	 * machine).
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account will be added.
	 * @param aGroupInfo
	 *            GroupInfo structure describing the global group that will be
	 *            added (along with all its details).
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding the global group in Windows
	 *             security database.
	 */
	public static native void globalGroupAdd(String aComputerName,
			GroupInfo aGroupInfo) throws NT4UserMetaDataException;

	/**
	 * Makes the specified user member of the specified global group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account resides.
	 * @param aGlobalGroupName
	 *            The name of the global group where the user will be added as
	 *            member.
	 * @param aUserName
	 *            The name of the user that will be added as member of the
	 *            global group.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding member in the global group.
	 */
	public static native void globalGroupAddUser(String aComputerName,
			String aGlobalGroupName, String aUserName)
			throws NT4UserMetaDataException;

	/**
	 * Removes the specified global group account from the specified machine
	 * (should be PDC machine).
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account will be removed from.
	 * @param aGlobalGroupName
	 *            The name of the global group that will be removed.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the global group from
	 *             Windows security database.
	 */
	public static native void globalGroupDel(String aComputerName,
			String aGlobalGroupName) throws NT4UserMetaDataException;

	/**
	 * Removes the specified user from the member list of the specified global
	 * group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account resides.
	 * @param aGlobalGroupName
	 *            The name of the global group account.
	 * @param aUserName
	 *            The name of the user that will be removed from the global
	 *            group's members.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the user from the global
	 *             group members list.
	 */
	public static native void globalGroupDelUser(String aComputerName,
			String aGlobalGroupName, String aUserName)
			throws NT4UserMetaDataException;

	/**
	 * Sets global group's user membership. This method cancels all users'
	 * membership in the global group specified and makes the users specified
	 * members of the global group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where the global
	 *            group account resides.
	 * @param aGlobalGroupName
	 *            The name of the global group which user membership will be
	 *            set.
	 * @param aUsers
	 *            Vector of String elements each one specifying the name of a
	 *            user account.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while setting global group's user
	 *             membership in Windows security database.
	 */
	public static native void globalGroupSetUsers(String aComputerName,
			String aGlobalGroupName, Vector aUsers)
			throws NT4UserMetaDataException;

	/**
	 * Modifies global group account properties. Rename functionality is
	 * included in this method.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the PDC machine where is the
	 *            global group account that will be modified.
	 * @param aGlobalGroupName
	 *            The name of the global group that will be modified.
	 * @param aGroupInfo
	 *            GroupInfo structure describing the new global group's details.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying the global group in
	 *             Windows security database.
	 */
	public static native void globalGroupSetInfo(String aComputerName,
			String aGlobalGroupName, GroupInfo aGroupInfo)
			throws NT4UserMetaDataException;

	// *************************************************************************
	// Local Groups routines
	// *************************************************************************

	/**
	 * Retrieves all information accessible for the local group specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group (the account name) which
	 *            information will be retrieved.
	 * @return com.ibm.di.connector.NT4UserMetaDataConnector.GroupInfo
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native GroupInfo localGroupGetInfo(String aComputerName,
			String aLocalGroupName) throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all local group accounts on the specified machine.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine which local groups
	 *            will be retrieved.
	 * @return Vector of String elements each one specifying a local group
	 *         account name.
	 * @throws NT4UserMetaDataException
	 *             If an error occurred while querying the machine specified.
	 */
	public static native Vector getLocalGroupsNames(String aComputerName)
			throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all users that are members of the local group
	 * specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group (the account name) which users
	 *            will be retrieved.
	 * @return Vector of String elements each one specifying a user account
	 *         name. Account names are returned in the following format: domain
	 *         users - <DOMAIN_NAME>\<USER_NAME>; non-domain users -
	 *         <USER_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while querying the machine specified.
	 */
	public static native Vector getLocalGroupUsers(String aComputerName,
			String aLocalGroupName) throws NT4UserMetaDataException;

	/**
	 * Retrieves a list of all global groups that are members of the local group
	 * specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group (the account name) which global
	 *            groups will be retrieved.
	 * @return Vector of String elements each one specifying a global group
	 *         account name. Account names are returned in the following format:
	 *         <DOMAIN_NAME>\<USER_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occured while quering the machine specified.
	 */
	public static native Vector getLocalGroupGlobalGroups(String aComputerName,
			String aLocalGroupName) throws NT4UserMetaDataException;

	/**
	 * Adds a local group account to the machine specified.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account will be added.
	 * @param aGroupInfo
	 *            GroupInfo structure describing the local group that will be
	 *            added (along with all its details).
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding the local group in Windows
	 *             security database.
	 */
	public static native void localGroupAdd(String aComputerName,
			GroupInfo aGroupInfo) throws NT4UserMetaDataException;

	/**
	 * Removes the specified local group account from the specified machine.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account will be removed from.
	 * @param aLocalGroupName
	 *            The name of the local group that will be removed.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the local group from
	 *             Windows security database.
	 */
	public static native void localGroupDel(String aComputerName,
			String aLocalGroupName) throws NT4UserMetaDataException;

	/**
	 * Makes the specified users members of the specified local group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group where the users will be added as
	 *            members.
	 * @param aUsers
	 *            Vector of String elements each one specifying the name of a
	 *            user to be added as member of the local group. Domain users
	 *            should be specified in the format <DOMAIN_NAME>\<USER_NAME>;
	 *            non-domain users can be specified either by <COMPUTER_NAME>\<USER_NAME>
	 *            or just by <USER_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding members in the local group.
	 */
	public static native void localGroupAddUsers(String aComputerName,
			String aLocalGroupName, Vector aUsers)
			throws NT4UserMetaDataException;

	/**
	 * Makes the specified global groups members of the specified local group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group where the global groups will be
	 *            added as members.
	 * @param aGlobalGroups
	 *            Vector of String elements each one specifying the name of a
	 *            global group to be added as member of the local group. Global
	 *            group names can be specified either in the format
	 *            <DOMAIN_NAME>\<GLOBAL_GROUP_NAME> or just by
	 *            <GLOBAL_GROUP_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while adding members in the local group.
	 */
	public static native void localGroupAddGlobalGroups(String aComputerName,
			String aLocalGroupName, Vector aGlobalGroups)
			throws NT4UserMetaDataException;

	/**
	 * Removes the specified users from the member list of the specified local
	 * group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group account.
	 * @param aUsers
	 *            Vector of String elements each one specifying the name of a
	 *            user to be removed from the local group's members. User names
	 *            can be specified either in the format <DOMAIN_NAME>\<USER_NAME>
	 *            or just by <USER_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the users from the local
	 *             group members list.
	 */
	public static native void localGroupDelUsers(String aComputerName,
			String aLocalGroupName, Vector aUsers)
			throws NT4UserMetaDataException;

	/**
	 * Removes the specified global groups from the member list of the specified
	 * local group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group account.
	 * @param aGlobalGroups
	 *            Vector of String elements each one specifying the name of a
	 *            global group to be removed from the local group's members.
	 *            Global group names can be specified either in the format
	 *            <DOMAIN_NAME>\<GLOBAL_GROUP_NAME> or just by
	 *            <GLOBAL_GROUP_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while removing the global groups from the
	 *             local group members list.
	 */
	public static native void localGroupDelGlobalGroups(String aComputerName,
			String aLocalGroupName, Vector aGlobalGroups)
			throws NT4UserMetaDataException;

	/**
	 * Modifies local group account properties. Rename functionality is included
	 * in this method.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where is the local
	 *            group account that will be modified.
	 * @param aLocalGroupName
	 *            The name of the local group that will be modified.
	 * @param aGroupInfo
	 *            GroupInfo structure describing the new local group's details.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while modifying the local group in Windows
	 *             security database.
	 */
	public static native void localGroupSetInfo(String aComputerName,
			String aLocalGroupName, GroupInfo aGroupInfo)
			throws NT4UserMetaDataException;

	/**
	 * Sets local group's user/global group membership. This method cancels all
	 * users' and global groups' membership in the local group specified and
	 * makes the users and global groups specified members of the local group.
	 * 
	 * @param aComputerName
	 *            The name (or IP address) of the machine where the local group
	 *            account resides.
	 * @param aLocalGroupName
	 *            The name of the local group which membership will be set.
	 * @param aMembers
	 *            Vector of String elements each one specifying the name of
	 *            either a user or a global group account. Domain members (users
	 *            or groups) should be specified in the format <DOMAIN_NAME>\<MEMBER_NAME>;
	 *            non-domain users can be specified either by <COMPUTER_NAME>\<USER_NAME>
	 *            or just by <USER_NAME>.
	 * @throws NT4UserMetaDataException
	 *             If an error occurs while setting local group's membership in
	 *             Windows security database.
	 */
	public static native void localGroupSetMembers(String aComputerName,
			String aLocalGroupName, Vector aMembers)
			throws NT4UserMetaDataException;

	/**
	 * Loads the DLL with the native code.
	 */
	static {
		System.loadLibrary("WindowsUsers");
	}

}
