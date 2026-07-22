/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import com.ibm.di.api.DIException;

import com.ibm.di.entry.Entry;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface provides several methods to handle an AssemblyLine.
 */
public interface AssemblyLineHandler extends Remote {

	/**
	 * Returns an AssemblyLine instance.
	 * 
	 * @return <code>this</code> AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while retrieving the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public AssemblyLine getAssemblyLine() throws DIException, RemoteException;

	/**
	 * Executes an AssemblyLine cycle. If an Entry is provided then it becomes
	 * the work entry.
	 * 
	 * @param aEntry
	 *            the work entry to use, or null to use an empty work
	 *            entry/iterator entry.
	 * @param aProcessTCB
	 *            if true, the AL's call/return attribute maps are applied to
	 *            the provided entry and returned entry.
	 * @return the work entry at the end of the cycle.
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Entry executeCycle(Entry aEntry, boolean aProcessTCB) throws DIException, RemoteException;

	/**
	 * Executes an AssemblyLine cycle. If an Entry is provided then it becomes
	 * the work entry.
	 * 
	 * @param aEntry
	 *            the work entry to use, or null to use an empty work
	 *            entry/iterator entry.
	 * @return the work entry at the end of the cycle.
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Entry executeCycle(Entry aEntry) throws DIException, RemoteException;

	/**
	 * Executes an AssemblyLine cycle with null work Entry attribute.
	 * 
	 * @return the work entry at the end of the cycle
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public Entry executeCycle() throws DIException, RemoteException;

	/**
	 * Evaluates the passed in as parameter script in the context of this
	 * AssemblyLine. You can access all the script beans defined in the scope of
	 * this AssemblyLine.
	 * 
	 * @param script
	 *            the script to evaluate.
	 * @return the serializable object returned by the script or null if no
	 *         return statement.
	 * @throws DIException
	 *             if an error occurs while evaluating the script
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 * @since 7.2
	 */
	public Serializable eval(String script) throws DIException, RemoteException;

	/**
	 * Closes the handler
	 * 
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void close() throws DIException, RemoteException;

}
