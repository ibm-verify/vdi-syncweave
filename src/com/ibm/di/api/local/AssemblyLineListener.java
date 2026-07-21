/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;

/**
 * 
 * This listener listens for AssemblyLine events.
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
	 * 
	 */
	public void assemblyLineCycleDone(Entry aEntry) throws DIException;

	/**
	 * Called when the assembly line which AssemblyLineListener listens has
	 * finished.
	 * 
	 * @throws DIException
	 *             if Runtime or Security exception occurs
	 * 
	 */
	public void assemblyLineFinished() throws DIException;

}
