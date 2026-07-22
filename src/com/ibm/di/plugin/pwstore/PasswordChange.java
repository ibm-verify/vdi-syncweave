/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore;

import java.util.Vector;

/**
 * This interface defines information specific to a password change.
 * <p>
 * Note: This class is intended to be implemented only by {@link BasePasswordChange}
 */
public interface PasswordChange {

	public static int NO_CHANGE = 0;
	public static int ADD_CHANGE = 1;
	public static int MODIFY_CHANGE = 2; 
	public static int DELETE_CHANGE = 3;
	public static int MODIFY_EXTENDED_DATA_CHANGE = 4;
	
	/**
	 * @return type of change
	 */
	public int getType();
	
	/**
	 * @return user ID
	 */
	public String getID();

	/**
	 * @return changed passwords
	 */
	public Vector<String> getPasswords();

	/**
	 * @return extended data about user
	 */
	public String getExtData();

	/**
	 * @return custom data
	 */
	public String getCustomData();

	/**
	 * @return timestamp of the change
	 */
	public long getTimestamp();
}
