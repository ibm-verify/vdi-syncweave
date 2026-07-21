/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.DIException;

/**
 * 
 * This interface provides information about various restrictions a user may
 * have. It lets you query what rights a user is granted and whether he/she is
 * authorized to execute a specific action.
 * 
 */
public interface SecurityRegistryMBean {

	/**
	 * Returns whether specified user is granted admin role.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return <code>true</code> if the user is granted the admin role
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userIsAdmin(String aUserId) throws DIException;

	/**
	 * Returns whether specified user is allowed to read given configuration.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @param aConfigId
	 *            the id of the configuration
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to read <code>aConfigId</code>.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanReadConfig(String aUserId, String aConfigId)
			throws DIException;

	/**
	 * Returns whether specified user is allowed to execute given AL from a
	 * given configuration.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @param aConfigId
	 *            the id of the configuration
	 * @param aAssemblyLine
	 *            the name of the assembly line
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to execute <code>aAssemblyLine</code> from
	 *         configuration <code>aConfigId</code>.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanExecuteAL(String aUserId, String aConfigId,
			String aAssemblyLine) throws DIException;

	/**
	 * Returns whether specified user is allowed to execute given configuration.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @param aConfigId
	 *            the id of the configuration
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to execute configuration <code>aConfigId</code>.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanExecuteConfig(String aUserId, String aConfigId)
			throws DIException;

	/**
	 * Returns whether specified user is allowed to execute assembly lines from
	 * a given configuration.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @param aConfigId
	 *            the id of the configuration
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to execute assembly lines from configuration
	 *         <code>aConfigId</code>.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanExecuteConfigALs(String aUserId, String aConfigId)
			throws DIException;

	/**
	 * Returns whether specified user is allowed to execute everything.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to execute everything.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanExecuteAll(String aUserId) throws DIException;

	/**
	 * Returns whether specified user is allowed to read everything.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to read everything.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 */
	public Boolean userCanReadAll(String aUserId) throws DIException;

}
