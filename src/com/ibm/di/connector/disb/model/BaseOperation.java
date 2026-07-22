/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb.model;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class BaseOperation {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String timeStamp;
	private ConfigurationItem[] configurationItems;
	private String[] configurationItemIds;
	private Relationship[] relationships;

	/**
	 * @return the timeStamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp
	 *            the timeStamp to set
	 */
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the configurationItems
	 */
	public ConfigurationItem[] getConfigurationItems() {
		return configurationItems;
	}

	/**
	 * @param configurationItems
	 *            the configurationItems to set
	 */
	public void setConfigurationItems(ConfigurationItem[] configurationItems) {
		this.configurationItems = configurationItems;
		configurationItemIds = new String[configurationItems.length];
		for (int i = 0; i < configurationItems.length; i++) {
			configurationItemIds[i] = (String) configurationItems[i].getId();
		}
	}

	/**
	 * @return the configurationItemIds
	 */
	public String[] getConfigurationItemIds() {
		return configurationItemIds;
	}

	/**
	 * @return the relationships
	 */
	public Relationship[] getRelationships() {
		return relationships;
	}

	/**
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(Relationship[] relationships) {
		this.relationships = relationships;
	}

}
