/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.DeltaConfig;
/**
 * Implements a Delta Configuration, which is used by Connectors in Iterator mode.
 */
public class DeltaConfigImpl extends BaseConfigurationImpl implements
		DeltaConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7250128484588024017L;

	public DeltaConfigImpl() {
		super();
	}

	public DeltaConfigImpl(Object config) {
		super(config);
	}

	public String getUniqueAttribute() {
		return getStringParameter(InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR);
	}

	public void setUniqueAttribute(String attrName) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR, attrName);
	}

	/**
	 * Returns delta db file name
	 */
	public String getDeltaDB() {
		return getStringParameter(InternalSchema.CONNECTOR_DELTA_DB);
	}

	/**
	 * Sets the delta db file name
	 */
	public void setDeltaDB(String deltaDB) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_DB, deltaDB);
	}

	/**
	 * Returns true if Iterate Deleted flag is set
	 */
	public boolean getIterateDeleted() {
		return getBooleanParameter(InternalSchema.CONNECTOR_DELTA_ITER_DELETED,
				false);
	}

	/**
	 * Sets the Iterate Delted flag
	 */
	public void setIterateDeleted(boolean iterateDeleted) {
		setBooleanParameter(InternalSchema.CONNECTOR_DELTA_ITER_DELETED,
				iterateDeleted);
	}

	/**
	 * Returns true if Remove Deleted flag is set
	 */
	public boolean getRemoveDeleted() {
		return getBooleanParameter(
				InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED, false);
	}

	/**
	 * Sets the Remove Deleted flag
	 */
	public void setRemoveDeleted(boolean removeDeleted) {
		setBooleanParameter(InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED,
				removeDeleted);
	}

	/**
	 * Returns true if returnUnchanged flag is set
	 */
	public boolean getReturnUnchanged() {
		return getBooleanParameter(
				InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED, false);
	}

	/**
	 * Sets the returnUnchanged flag
	 */
	public void setReturnUnchanged(boolean returnUnchanged) {
		setBooleanParameter(InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED,
				returnUnchanged);
	}

	/**
	 * We override this method to change the inherited object if we inherit from
	 * a connector.
	 */
	public void setInheritsFrom(BaseConfiguration inheritFrom) {
		if (inheritFrom instanceof ConnectorConfig)
			super.setInheritsFrom(((ConnectorConfig) inheritFrom)
					.getDeltaConfig());
		else
			super.setInheritsFrom(inheritFrom);
	}

	/*
	 * Returns the driver to use for backend storage.
	 */
	public String getDriver() {
		return (String) getParameter(InternalSchema.CONNECTOR_DELTA_DRIVER,
				"CloudScape");
	}

	/*
	 * Sets the driver to use for backend storage.
	 * 
	 * @param driver The driver to use. Currently specify the following for Derby: CloudScape
	 */
	public void setDriver(String driver) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_DRIVER, driver);
	}

	/*
	 * Returns the delta level
	 */
	public int getDeltaLevel() {
		return getIntegerParameter(InternalSchema.CONNECTOR_DELTA_LEVEL, 3);
	}

	/*
	 * Sets the delta level.
	 * 
	 * @param level The level to use. Currently specify: 1 - Entry, 2 -
	 * Attribute, 3 - Value
	 */
	public void setDeltaLevel(int level) {
		setIntegerParameter(InternalSchema.CONNECTOR_DELTA_LEVEL, level);
	}

	/**
	 * Returns when to commit delta configuration parameter
	 */
	public String getWhenToCommit() {
		return (String) getParameter(
				InternalSchema.CONNECTOR_DELTA_WHEN_TO_COMMIT,
				InternalSchema.CONNECTOR_DELTA_COMMIT_ON_ENDITER);
	}

	/**
	 * Sets when to commit delta configuration parameter
	 */
	public void setWhenToCommit(String value) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_WHEN_TO_COMMIT, value);
	}

	public boolean getFastAlgorithm() {
		return getBooleanParameter(
				InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM, false);
	}

	public void setFastAlgorithm(boolean value) {
		setBooleanParameter(InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM,
				value);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getAllowDuplicateDeltaKeys() {

		return getBooleanParameter(
				InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS, false);
	}
	/**
	 * {@inheritDoc}
	 */
	public void setAllowDuplicateDeltaKeys(boolean value) {

		setBooleanParameter(
				InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRowLocking() {
		return (String) getParameter(InternalSchema.CONNECTOR_DELTA_ROW_LOCKING);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRowLocking(String value) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_ROW_LOCKING, value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAttributeList() {
		return (String) getParameter(InternalSchema.CONNECTOR_DELTA_ATTRIBUTE_LIST);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAttributeList(String value) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_ATTRIBUTE_LIST, value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getChangeDetectionMode() {
		return (String) getParameter(InternalSchema.CONNECTOR_DELTA_CHANGE_DETECTION_MODE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChangeDetectionMode(String value) {
		setStringParameter(InternalSchema.CONNECTOR_DELTA_CHANGE_DETECTION_MODE, value);
	}
}
