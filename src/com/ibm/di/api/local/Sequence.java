/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.TaskStatistics;

/**
 * Represents a running Sequence of AssemblyLines
 * @since 7.2
 *
 */
public interface Sequence {
	/**
	 * Returns the configuration instance of the Sequence.
	 * @return Returns ConfigInstance object
	 * @throws DIException
	 *             if an error occurs while retrieving the configuration
	 *             instance information.
	 */
	public ConfigInstance getConfigInstance() throws DIException;

	/**
	 * Returns the name of the Sequence.
	 * @return String object representing the Sequence's name.
	 * @throws DIException
	 *             if an error occurs while retrieving the name
	 */
	public String getName() throws DIException;

	/**
	 * Returns the unique code of the AssemblyLine.
	 * @return int value representing the unique code of the Sequence.
	 * @throws DIException
	 *             if an error occurs while retrieving the unique code.
	 */
	public int getUniqueCode() throws DIException;

	/**
	 * This method returns the TaskStatistics object for this Sequence.
	 * @return The accumulated TaskStatistics object.
	 * @throws DIException
	 *             if an error occurs while getting the Sequence statistics.
	 */
	public TaskStatistics getStatistics() throws DIException;

	/**
	 * Checks if the Sequence is active.
	 * @return true if the Sequence thread is alive, false otherwise.
	 * @throws DIException
	 *             if an error occurs while getting the state.
	 */
	public boolean isActive() throws DIException;

	/**
	 * This method returns the result entry object. This object is a copy of the
	 * working entry.
	 * 
	 * @return The last "work" entry object.
	 * @throws DIException
	 *             if an error occurs while getting the result Entry.
	 */
	public Entry getResult() throws DIException;

	/**
	 * Stops the execution of the Sequence.
	 * 
	 * @throws DIException
	 *             if an error occurs while stopping the AssemblyLine.
	 */
	public void stop() throws DIException;

	/**
	 * Stops the execution of the Sequence, and waits for it to stop.
	 * @param sync If true, create a new Thread to do the waiting.
	 * @throws DIException
	 *             if an error occurs while stopping the Sequence.
	 */
	public void stop(boolean sync) throws DIException;
}
