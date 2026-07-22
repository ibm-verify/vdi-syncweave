/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import com.ibm.di.api.DIException;

/**
 * Base MBean interface, that all MBeans from the management package inherit.
 */
public interface BaseMBean {

	/**
	 * Reads attribute "Type".
	 * <p>
	 * <code>getType()</code> and <code>getId()</code> are used in a common
	 * schema for object names for all MBeans in the management package. The key
	 * properties part of the object name of each MBean is defined as
	 * <code>"type=" + getType() + ",id=" + getId()</code>, for example
	 * "type=AssemblyLine,id=Hello".
	 * 
	 * @return the type of this MBean.
	 * @throws DIException
	 *             if an error occurs while obtaining MBean's type.
	 */
	public String getType() throws DIException;

	/**
	 * Reads attribute "Id". The "Id" value should be different for different
	 * MBeans of the same type.
	 * <p>
	 * <code>getType()</code> and <code>getId()</code> are used in a common
	 * schema for object names for all MBeans in the management package. The key
	 * properties part of the object name of each MBean is defined as
	 * <code>"type=" + getType() + ",id=" + getId()</code>, for example
	 * "type=AssemblyLine,id=Hello".
	 * 
	 * @return the Id of this MBean.
	 * @throws DIException
	 *             if an error occurs while obtaining MBean's Id.
	 */
	public String getId() throws DIException;

}
