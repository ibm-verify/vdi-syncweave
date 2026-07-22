/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.security;

import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.server.ResourceHash;

/**
 * This class defines a specific role assigned to a user.
 */
public class Role {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A String constant for the ALs.
	 */
	protected static final String ALLIST = "ALLIST";

	/**
	 * no given role. Default when using default constructor
	 */
	private static final int ROLE_NONE = -1;

	/**
	 * The Id of the admin privileges.
	 */
	public static final int ROLE_ADMIN = 0;

	/**
	 * The Id of the read privileges.
	 */
	public static final int ROLE_READ = 1;

	/**
	 * The Id of the execute privileges.
	 */
	public static final int ROLE_EXECUTE = 2;

	/**
	 * The string representation of the available privileges.
	 */
	public static final String[] ROLE_NAMES = { "admin", "read", "execute" };

	/**
	 * contains the id of the given privileges level
	 */
	private int mRole = ROLE_NONE;

	/**
	 * Map structure:<br>
	 * (*) keys contain config ids - key "*" means all configurations<br>
	 * (*) values contain map with two fixed keys ALLIST:<br>
	 * (-) ALLIST contains vector of ALs: AL name "*" means all; empty vector
	 * means that user can not execute ALs<br>
	 */
	private TreeMap<String, TreeMap<String, Vector<String>>> mRawSpecs = null;

	/**
	 * Resource Hash used to log TMS messages.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Constructs a role
	 */
	public Role() {
		mRole = ROLE_NONE;
		mRawSpecs = new TreeMap<String, TreeMap<String, Vector<String>>>();
	}

	/**
	 * Constructs a role
	 * 
	 * @param aRole
	 *            the role name
	 * @param aMap
	 *            the objects this role applies on.
	 * @throws DIException
	 *             if the <code>aRole</code> parameter is either null or an
	 *             unsupportable role.
	 */
	@SuppressWarnings("unchecked")
	public Role(String aRole,
			TreeMap<String, TreeMap<String, Vector<String>>> aMap)
			throws DIException {
		if (aRole == null) {
			throw new DIException(sResHash.getString("SEVER.API.ROLE.IS.NULL"));
		}

		if (aRole.equals(ROLE_NAMES[ROLE_ADMIN])) {
			mRole = ROLE_ADMIN;
		} else if (aRole.equals(ROLE_NAMES[ROLE_READ])) {
			mRole = ROLE_READ;
		} else if (aRole.equals(ROLE_NAMES[ROLE_EXECUTE])) {
			mRole = ROLE_EXECUTE;
		} else {
			throw new DIException(sResHash.getString("SEVER.API.INVALID.ROLE",
					aRole));
		}

		if (aMap != null) {
			mRawSpecs = (TreeMap<String, TreeMap<String, Vector<String>>>) aMap
					.clone();
		}
	}

	/**
	 * Retrives the ID of the given privileges level .
	 * 
	 * @return the role id.
	 * @see #ROLE_ADMIN
	 * @see #ROLE_READ
	 * @see #ROLE_EXECUTE
	 */
	public int getRole() {
		return mRole;
	}

	/**
	 * Retrieves the name of the role.
	 * 
	 * @return the string representation of the role, or null if the inner role
	 *         id is invalid.
	 */
	public String getRoleName() {
		if (mRole < 0 || mRole > 2) {
			return null;
		}

		return ROLE_NAMES[mRole];
	}

	/**
	 * Verify if role is admin.
	 * 
	 * @return true if the inner role id is {@link #ROLE_ADMIN}
	 */
	public boolean isAdmin() {
		return (mRole == ROLE_ADMIN);
	}

	/**
	 * Check that the specific configuration could be read by the user.
	 * 
	 * @param aConfigId
	 *            the configInstance identifier
	 * @return true if the user have been given this privilege.
	 */
	public boolean canReadConfig(String aConfigId) {
		if (isAdmin()) {
			return true;
		}
		if (mRole != ROLE_READ) {
			return false;
		}

		return (mRawSpecs.containsKey("*") || mRawSpecs.containsKey(aConfigId));
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
		if (isAdmin()) {
			return true;
		}
		if (mRole != ROLE_EXECUTE) {
			return false;
		}
		if (mRawSpecs.containsKey("*")) {
			return true;
		}
		TreeMap<String, Vector<String>> config = mRawSpecs.get(aConfigId);
		if (config == null) {
			return false;
		}
		Vector<String> als = config.get(ALLIST);
		if (als == null) {
			return false;
		}
		return (als.contains("*") || als.contains(aAssemblyLine));
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
		if (isAdmin()) {
			return true;
		}
		if (mRole != ROLE_EXECUTE) {
			return false;
		}
		if (mRawSpecs.containsKey("*")) {
			return true;
		}
		TreeMap<String, Vector<String>> config = mRawSpecs.get(aConfigId);
		if (config == null) {
			return false;
		}
		Vector<String> als = config.get(ALLIST);
		if (als == null) {
			return false;
		} else {
			return als.contains("*");
		}
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
		if (isAdmin()) {
			return true;
		}
		if (mRole != ROLE_EXECUTE) {
			return false;
		}
		return mRawSpecs.containsKey("*");
	}

	/**
	 * Returns whether specified user is allowed to read everything.
	 * 
	 * @return true if the user have been given this privilege.
	 */
	public boolean canReadAll() {
		if (isAdmin()) {
			return true;
		}
		if (mRole != ROLE_READ) {
			return false;
		}
		return mRawSpecs.containsKey("*");
	}

}
