/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskStatistics;

public interface Sequence extends Remote {
	/**
	 * Returns the configuration instance of the Sequence.
	 * @return Returns ConfigInstance object
	 * @throws DIException
	 *             if an error occurs while retrieving the configuration
	 *             instance information.
	 */
	public ConfigInstance getConfigInstance() throws DIException, RemoteException;

	/**
	 * Returns the name of the Sequence.
	 * @return String object representing the Sequence's name.
	 * @throws DIException
	 *             if an error occurs while retrieving the name
	 */
	public String getName() throws DIException, RemoteException;

	/**
	 * Returns the unique code of the Sequence.
	 * @return int value representing the unique code of the Sequence.
	 * @throws DIException
	 *             if an error occurs while retrieving the unique code.
	 */
	public int getUniqueCode() throws DIException, RemoteException;

	/**
	 * This method returns the TaskStatistics object for this Sequence.
	 * @return The accumulated TaskStatistics object.
	 * @throws DIException
	 *             if an error occurs while getting the Sequence statistics.
	 */
	public TaskStatistics getStatistics() throws DIException, RemoteException;

	/**
	 * Checks if the Sequence is active.
	 * @return true if the Sequence thread is alive, false otherwise.
	 * @throws DIException
	 *             if an error occurs while getting the state.
	 */
	public boolean isActive() throws DIException, RemoteException;

	/**
	 * This method returns the result entry object. This object is a copy of the
	 * working entry.
	 * 
	 * @return The last "work" entry object.
	 * @throws DIException
	 *             if an error occurs while getting the result Entry.
	 */
	public Entry getResult() throws DIException, RemoteException;

	/**
	 * Stops the execution of the Sequence.
	 * 
	 * @throws DIException
	 *             if an error occurs while stopping the AssemblyLine.
	 */
	public void stop() throws DIException, RemoteException;

	/**
	 * Stops the execution of the Sequence, and waits for it to stop.
	 * @param sync If true, create a new Thread to do the waiting.
	 * @throws DIException
	 *             if an error occurs while stopping the Sequence.
	 */
	public void stop(boolean sync) throws DIException, RemoteException;
}
