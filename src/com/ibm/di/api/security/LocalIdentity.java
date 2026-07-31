/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.security;

/**
 * This class represents a local user's identity. This object is created for
 * usage with the local Server API.
 */
public class LocalIdentity extends Identity {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * the default user id
	 */
	private static final String LOCAL_USERID = "local";

	/**
	 * Default constructor for this object.
	 */
	public LocalIdentity() {
	}

	/**
	 * not implemented
	 * 
	 * @return <code>true</code>
	 */
	public boolean isAdmin() {
		return true;
	}

	/**
	 * not implemented
	 * 
	 * @param aConfigId -
	 *            ignored
	 * @return <code>true</code>
	 */
	public boolean canReadConfig(String aConfigId) {
		return true;
	}

	/**
	 * not implemented
	 * @param aConfigId -
	 *            ignored
	 * @param aAssemblyLine -
	 *            ignored
	 * @return <code>true</code>
	 */
	public boolean canExecuteAL(String aConfigId, String aAssemblyLine) {
		return true;
	}

	/**
	 * not implemented
	 * 
	 * @param aConfigId -
	 *            ignored
	 * @return <code>true</code>
	 */
	public boolean canExecuteConfigALs(String aConfigId) {
		return true;
	}

	/**
	 * not implemented
	 * 
	 * @param aConfigId -
	 *            ignored
	 * @return <code>true</code>
	 */
	public boolean canExecuteConfig(String aConfigId) {
		return canExecuteConfigALs(aConfigId);
	}

	/**
	 * not implemented
	 * 
	 * @return <code>true</code>
	 */
	public boolean canExecuteAll() {
		return true;
	}

	/**
	 * not implemented
	 * 
	 * @return <code>true</code>
	 */
	public boolean canReadAll() {
		return true;
	}

	/**
	 * Returns default user ID.
	 * 
	 * @return the default user id
	 */
	public String getUserId() {
		return LOCAL_USERID;
	}

}
