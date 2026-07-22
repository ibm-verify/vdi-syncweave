/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore;

/**
 * This interface should be implemented by all password stores. It replaces the
 * deprecated {@link IPasswordSynchronizer} interface.
 *<p>
 * Note: User password stores must not implement this interface directly.
 * Instead they must extend from the {@link BasePasswordStore} class in order
 * to stay forward compatible.
 * 
 * @see IPasswordSynchronizer
 */
public interface PasswordStore {

	/**
	 * This method check the password store availability,
	 * 
	 * @param change
	 *            object describing the password change
	 * @return <code>true</code> if password store is available;
	 *         <code>false</code> otherwise
	 */
	public boolean isAvailable(PasswordChange change);

	/**
	 * This method stores password change in the password store.
	 * 
	 * @param change
	 *            object describing the password change
	 * @return <code>true</code> if the operation is successful;
	 *         <code>false</code> otherwise
	 */
	public boolean store(PasswordChange change);

	/**
	 * This method sends additional information about a user.
	 * <p>
	 * Currently only the Windows Password Synchronizer plug-in sends extended
	 * data.
	 * 
	 * @param change
	 *            object describing the password change
	 * @return <code>true</code> if the operation is successful;
	 *         <code>false</code> otherwise
	 */
	public boolean setExtendedData(PasswordChange change);

	/**
	 * This method initializes the password store.
	 * 
	 * @param aObj
	 *            object of type {@link PWSyncLog} used for logging
	 */
	public void initialize(Object aObj) throws Exception;

	/**
	 * This method cleans any reserved resources such as files, connections etc.
	 */
	public void terminate();
}
