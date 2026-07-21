/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * A Delta Configuration can be used by a Connector in Iterator mode.
 * 
 */
public interface DeltaConfig extends BaseConfiguration {

	/**
	 * @returns the attribute used as unique key
	 */
	public String getUniqueAttribute();

	/**
	 * Sets the attribute name used as unique key
	 */
	public void setUniqueAttribute(String attrName);

	/**
	 * @returns delta db file name
	 */
	public String getDeltaDB();

	/**
	 * Sets the delta db file name
	 */
	public void setDeltaDB(String deltaDB);

	/**
	 * @returns true if Iterate Deleted flag is set
	 */
	public boolean getIterateDeleted();

	/**
	 * Sets the Iterate Deleted flag
	 */
	public void setIterateDeleted(boolean iterateDeleted);

	/**
	 * @returns true if Remove Deleted flag is set
	 */
	public boolean getRemoveDeleted();

	/**
	 * Sets the Remove Deleted flag
	 */
	public void setRemoveDeleted(boolean removeDeleted);

	/**
	 * Returns true if returnUnchanged flag is set
	 */
	public boolean getReturnUnchanged();

	/**
	 * Sets the returnUnchanged flag
	 */
	public void setReturnUnchanged(boolean returnUnchanged);

	/**
	 * @returns the driver to use for backend storage.
	 */
	public String getDriver();

	/**
	 * Sets the driver to use for backend storage.
	 * 
	 * @param driver
	 *            The driver to use. Currently specify the following for Derby:
	 *            CloudScape
	 */
	public void setDriver(String driver);

	/*
	 * Returns the delta level
	 */
	public int getDeltaLevel();

	/*
	 * Sets the delta level.
	 * 
	 * @param level The level to use. Currently specify: 1 - Entry, 2 -
	 * Attribute, 3 - Value
	 */
	public void setDeltaLevel(int level);

	/**
	 * @returns when to commit delta configuration parameter
	 */
	public String getWhenToCommit();

	/**
	 * Sets when to commit delta configuration parameter
	 */
	public void setWhenToCommit(String value);

	/**
	 * @returns true if fastAlgorithm flag is set
	 * 
	 * @since 6.1.1
	 */
	public boolean getFastAlgorithm();

	/**
	 * Sets the fastAlgorithm flag
	 * 
	 * @since 6.1.1
	 */
	public void setFastAlgorithm(boolean value);

	/**
	 * @returns true if allow duplicate keys flag is set
	 * 
	 * @since 7.0
	 */
	public boolean getAllowDuplicateDeltaKeys();

	/**
	 * Sets the allowDuplicateKeys flag
	 * 
	 * @since 7.0
	 */
	public void setAllowDuplicateDeltaKeys(boolean value);

	/**
	 * @returns the specified transaction isolation level.
	 * 
	 * @since 7.1
	 */
	public String getRowLocking();

	/**
	 * Sets the transaction isolation level.
	 * 
	 * @since 7.1
	 */
	public void setRowLocking(String value);

	/**
	 * @returns the specified list with attributes whose changes will be
	 *          detected or ignored during compute changes process.
	 * 
	 * @since 7.1
	 */
	public String getAttributeList();

	/**
	 * Sets the list with attributes whose changes will be detected or ignored
	 * during compute changes process.
	 * 
	 * @since 7.1
	 */
	public void setAttributeList(String value);
	
	/**
	 * @returns the specified change detection mode.
	 * 
	 * @since 7.1
	 */
	public String getChangeDetectionMode();

	/**
	 * Sets the change detection mode.
	 * 
	 * @since 7.1
	 */
	public void setChangeDetectionMode(String value);

}
