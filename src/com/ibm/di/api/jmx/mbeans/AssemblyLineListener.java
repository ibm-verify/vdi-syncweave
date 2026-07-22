/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.DIException;

import com.ibm.di.entry.Entry;

import java.rmi.RemoteException;

/**
 * 
 * This listener listens for AssemblyLine events.
 * <p>
 * In order to listen for the specified events an implementation of this class
 * should be provided by the user. Then reference to this implementation could
 * be passed to methods like: <code>
 * com.ibm.di.api.jmx.ConfigInstance.startAssemblyLine()</code>
 * 
 */
public interface AssemblyLineListener extends LogListener {

	/**
	 * Called when specified entry drives a complete cycle trough the assembly
	 * line which AssemblyLineListener listens.
	 * 
	 * @param aEntry
	 *            the entry object
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void assemblyLineCycleDone(Entry aEntry) throws DIException,
			RemoteException;

	/**
	 * Called when the assembly line which AssemblyLineListener listens has
	 * finished.
	 * 
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * @throws RemoteException
	 *             if a communication-related exception occurs.
	 */
	public void assemblyLineFinished() throws DIException, RemoteException;

}
