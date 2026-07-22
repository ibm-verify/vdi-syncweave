/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * This interface provides access to the Solution interface settings of a
 * configuration. Most of these elements are used to define external aspects of
 * a configuration such as which assemblylines and properties are
 * visible/editable by a user at runtime.
 * 
 */
public interface SolutionInterface extends ContainerConfig {

	/**
	 * Returns the name of the health assemblyline. The return value is null if
	 * no assemblyline is configured.
	 * 
	 * @return The name of the Health assemblyline
	 */
	public String getHealthAssemblyLine();

	/**
	 * Sets the name of the health assemblyline for this configuration (null or
	 * empty string to clear).
	 * 
	 * @param name
	 *            The name of the Health assemblyline (using empty string will
	 *            translate to null value).
	 */
	public void setHealthAssemblyLine(String name);

	/**
	 * Returns a container of assemblyline names that are exposed by this
	 * solution. Each child of the list is a BaseConfiguration object where the
	 * name is set to that of the assemblyline. The BaseConfiguration object for
	 * each assemblyline can be used to hold additional custom information about
	 * each AL.
	 * 
	 * @return ContainerConfig of assemblyline names (String values)
	 */
	public ContainerConfig getExposedAssemblyLines();

	/**
	 * Convenience method to create a BaseConfiguration object in the exposed
	 * assemblylines container. Use
	 * getExposedAssemblyLines().getConfig()/removeConfig() etc to manipulate
	 * contents. If an assemblyline with that name is already defined the
	 * current configuration is returned.
	 * 
	 * @param name
	 *            Name of the AL to expose
	 * @return The new/current configuration
	 * @throws Exception
	 */
	public BaseConfiguration addExposedAssemblyLine(String name)
			throws Exception;

	/**
	 * Returns the ExposedProperty object using name and storename
	 * 
	 * @param propertyName
	 *            The propery name.
	 * @param storeName
	 *            The store name.
	 * 
	 * @return Returns the ExposedProperty object using name and store name.
	 */
	public ExposedProperty getExposedProperty(String propertyName,
			String storeName);

	/**
	 * Returns a container of ExposedProperty objects.
	 * 
	 * @return ContainerConfig of ExposedProperty objects.
	 */
	public ContainerConfig getExposedProperties();

	/**
	 * Convenience method to create an ExposedProperty object in the exposed
	 * properties container. Use
	 * getExposedProperties().getConfig()/removeConfig() etc to manipulate
	 * contents. If a property with that name/storename is already defined the
	 * current configuration is returned.
	 * 
	 * When creating a new ExposedProperty object the concatenation of
	 * "propertyName.propertyStore" is used to generate a unique name for the
	 * object.
	 * 
	 * @param propertyName
	 *            Name of the AL to expose.
	 * @param storeName
	 *            Name of the store.
	 * 
	 * @return The new/current configuration
	 * @throws Exception
	 */
	public ExposedProperty addExposedProperty(String propertyName,
			String storeName) throws Exception;

	/**
	 * Convenience method that returns a list of unique category names found in
	 * the exposed properties container.
	 * 
	 * @return List of category names (String values)
	 */
	public List<String> getPropertyCategoryNames();

	/**
	 * Convenience method that returns a list of unique store names found in the
	 * exposed properties container.
	 * 
	 * @return List of store names (String values)
	 */
	public List<String> getPropertyStoreNames();

	/**
	 * Returns the instance ID for this configuration or null if none is
	 * defined.
	 * 
	 */
	public String getInstanceID();

	/**
	 * Sets the instance ID for this configuration or null if none is defined.
	 * 
	 * @param id
	 *            The config instance id or null
	 */
	public void setInstanceID(String id);

	/**
	 * Returns the poll interval for the health assemblyline.
	 * 
	 * @return The poll interval seconds or -1 if not configured
	 */
	public int getHealthPollInterval();

	/**
	 * Sets the poll interval for the health assemblyline.
	 * 
	 * @param seconds
	 *            The poll interval in seconds
	 */
	public void setHealthPollInterval(int seconds);
}
