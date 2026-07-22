/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import javax.management.ObjectName;

import com.ibm.di.api.DIException;
import com.ibm.di.entry.Entry;

/**
 * 
 * This interface provides several methods to handle an AssemblyLine.
 * 
 */
public interface AssemblyLineHandlerMBean extends BaseAdminMBean {

	/**
	 * Returns an ObjectName generated from the AssemblyLine's name and
	 * AssemblyLine's unique code.
	 * 
	 * @return the ObjectName of the AssemblyLine.
	 * @throws DIException
	 *             if an error occurs while getting the AssemblyLine's
	 *             ObjectName.
	 */
	public ObjectName getAssemblyLine() throws DIException;

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
	 */
	public Entry executeCycle(Entry aEntry, Boolean aProcessTCB)
			throws DIException;

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
	 */
	public Entry executeCycle(Entry aEntry) throws DIException;

	/**
	 * Executes an AssemblyLine cycle with null work Entry attribute.
	 * 
	 * @return the work entry at the end of the cycle
	 * @throws DIException
	 *             if an error occurs while executing the AssemblyLine.
	 */
	public Entry executeCycle() throws DIException;

	/**
	 * Closes the Assembly LineHandler MBean.
	 * 
	 * @throws DIException if error occurs while closing the MBean.
	 */
	public void close() throws DIException;

}
