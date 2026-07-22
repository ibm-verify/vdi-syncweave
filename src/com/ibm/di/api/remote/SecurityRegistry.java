/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ibm.di.api.DIException;

/**
 * 
 * This interface provides information about various restrictions a user may
 * have.It lets you query what rights a user is granted and whether he/she is
 * authorized to execute a specific action.
 * 
 */
public interface SecurityRegistry extends Remote {

	/**
	 * Returns whether specified user is granted admin role.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return <code>true</code> if the user is granted the admin role
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userIsAdmin(String aUserId) throws DIException,
			RemoteException;

	/**
	 * Returns whether specified user is allowed to read given configuration.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @param aConfigId
	 *            the id of the configuration
	 * @return true only if <code>aUserId</code> is allowed to read
	 *         <code>aConfigId</code>.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanReadConfig(String aUserId, String aConfigId)
			throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanExecuteAL(String aUserId, String aConfigId,
			String aAssemblyLine) throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanExecuteConfig(String aUserId, String aConfigId)
			throws DIException, RemoteException;

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
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanExecuteConfigALs(String aUserId, String aConfigId)
			throws DIException, RemoteException;

	/**
	 * Returns whether specified user is allowed to execute everything.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to execute everything.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanExecuteAll(String aUserId) throws DIException,
			RemoteException;

	/**
	 * Returns whether specified user is allowed to read everything.
	 * 
	 * @param aUserId
	 *            the id of the user
	 * @return return <code>true</code> only if <code>aUserId</code> is
	 *         allowed to read everything.
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public boolean userCanReadAll(String aUserId) throws DIException,
			RemoteException;

}
