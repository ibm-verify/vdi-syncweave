/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * This was the configuration for one item of the Connector schema.
 * @deprecated We use SchemaItemConfig for all Schema Items now.
 * @see SchemaItemConfig
 */
public interface ConnectorSchemaItemConfig extends BaseConfiguration {

	/**
	 * Gets the attributeName attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The attributeName value
	 */
	public String getAttributeName();

	/**
	 * Sets the attributeName attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param name
	 *            The new attributeName value
	 */
	public void setAttributeName(String name);

	/**
	 * Gets the internalSyntax attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The internalSyntax value
	 */
	public String getInternalSyntax();

	/**
	 * Sets the internalSyntax attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param syntax
	 *            The new internalSyntax value
	 */
	public void setInternalSyntax(String syntax);

	/**
	 * Gets the externalSyntax attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The externalSyntax value
	 */
	public String getExternalSyntax();

	/**
	 * Sets the externalSyntax attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param syntax
	 *            The new externalSyntax value
	 */
	public void setExternalSyntax(String syntax);

	/**
	 * Gets the sample attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The sample value
	 */
	public Object getSample();

	/**
	 * Sets the sample attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param sample
	 *            The new sample value
	 */
	public void setSample(Object sample);

	/**
	 * Gets the excluded attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The excluded value
	 */
	public boolean getExcluded();

	/**
	 * Sets the excluded attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param excluded
	 *            The new excluded value
	 */
	public void setExcluded(boolean excluded);

	/**
	 * Gets the inputRequired attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The inputRequired value
	 */
	public boolean getInputRequired();

	/**
	 * Sets the inputRequired attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param required
	 *            The new inputRequired value
	 */
	public void setInputRequired(boolean required);

	/**
	 * Gets the outputRequired attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The outputRequired value
	 */
	public boolean getOutputRequired();

	/**
	 * Sets the outputRequired attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param required
	 *            The new outputRequired value
	 */
	public void setOutputRequired(boolean required);

	/**
	 * Gets the defaultValue attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @return The defaultValue value
	 */
	public Object getDefaultValue();

	/**
	 * Sets the defaultValue attribute of the ConnectorSchemaItemConfig object
	 * 
	 * @param value
	 *            The new defaultValue value
	 */
	public void setDefaultValue(Object value);
}
