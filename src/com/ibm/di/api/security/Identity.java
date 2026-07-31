/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.security;

import java.util.Vector;

/**
 * This class represents an authenticated user's identity. This class has
 * several convenient methods for querying the user's permeations over a
 * specific operation.
 */
public class Identity {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * LDAP prefix
	 */
	private static final String LDAP_PREFIX = "[LDAP]";

	/**
	 * the distinguished name of the user
	 */
	private String mUserId = null;

	/**
	 * Vector object containing the authenticating LDAP user as first element
	 * and the groups of the user.
	 */
	private String[] groupIds = null;

	/**
	 * the list of roles the user have.
	 */
	private Vector<Role> mRoles = null;

	/**
	 * default constructor, used by descendants
	 */
	protected Identity() {
	}

	/**
	 * Public constructor used by the {@link Registry}
	 * 
	 * @param aUserId
	 *            the distinguished name of the user
	 * @param aRoles
	 *            the list of roles the user have.
	 */
	public Identity(String aUserId, Vector<Role> aRoles) {
		mUserId = aUserId;
		mRoles = aRoles;
	}

	/**
	 * public constructor used by LDAP authentication with enabled LDAP group
	 * support.
	 * 
	 * @param userAndGroups
	 *            Vector object containing the authenticating LDAP user as first
	 *            element and the groups of the user.
	 * @param roles
	 *            Vector object containing the roles of the user and users'
	 *            groups in the User Registry.
	 * 
	 * @since 7.0
	 */
	public Identity(Vector<String> userAndGroups, Vector<Role> roles) {
		mUserId = LDAP_PREFIX + userAndGroups.get(0);
		groupIds = new String[userAndGroups.size() - 1];
		for (int i = 0; i < groupIds.length; i++) {
			groupIds[i] = userAndGroups.get(i + 1);
		}
		mRoles = roles;
	}

	/**
	 * Retrieves user ID.
	 * 
	 * @return the user's distinguished name.
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * Retrieves LDAP groups.
	 * 
	 * @return Array of String objects representing the names of the LDAP
	 *         groups, which the user is member of. Returns null, in case no
	 *         LDAP Authentication is used or groupSupport is not enabled.
	 * @since 7.0
	 */
	public String[] getGroupIds() {
		return groupIds;
	}

	/**
	 * Retrieves assigned roles.
	 * 
	 * @return Vector object containing the roles assigned to the Identity.
	 * @since 7.0
	 */
	public Vector<Role> getRoles() {
		return mRoles;
	}

	/**
	 * Verifies admin privileges.
	 * 
	 * @return true if the user have admin privileges.
	 */
	public boolean isAdmin() {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null && role.isAdmin()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Check that the specific configuration could be read by the user.
	 * 
	 * @param aConfigId
	 *            the configInstance identifier
	 * @return true if the user have been given this privilege.
	 */
	public boolean canReadConfig(String aConfigId) {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null && (role.canReadConfig(aConfigId))) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Check that the specific AssemblyLine could be started by the user.
	 * 
	 * @param aConfigId
	 *            the configInstance that has this AL
	 * @param aAssemblyLine
	 *            the name of the AL to check for
	 * @return true if the user have been given this privilege.
	 */
	public boolean canExecuteAL(String aConfigId, String aAssemblyLine) {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null
						&& (role.canExecuteAL(aConfigId, aAssemblyLine))) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns whether specified user is allowed to execute assembly lines from
	 * a given configuration.
	 * 
	 * @param aConfigId
	 *            the configInstance identifier
	 * @return true if the user have been given this privilege.
	 */
	public boolean canExecuteConfigALs(String aConfigId) {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null && (role.canExecuteConfigALs(aConfigId))) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns whether specified user is allowed to execute given configuration.
	 * 
	 * @param aConfigId
	 *            the configInstance identifier
	 * @return true if the user have been given this privilege.
	 */
	public boolean canExecuteConfig(String aConfigId) {
		return canExecuteConfigALs(aConfigId);
	}

	/**
	 * Returns whether specified user is allowed to execute everything.
	 * 
	 * @return true if the user have been given this privilege.
	 */
	public boolean canExecuteAll() {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null && (role.canExecuteAll())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns whether specified user is allowed to read everything.
	 * 
	 * @return true if the user have been given this privilege.
	 */
	public boolean canReadAll() {
		boolean result = false;
		if (mRoles != null) {
			for (int i = 0; i < mRoles.size(); i++) {
				Role role = mRoles.get(i);
				if (role != null && (role.canReadAll())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

}
