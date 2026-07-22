/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * A Container containing BaseConfiguration objects.
 *
 */
public interface ContainerConfig extends BaseConfiguration {

	/**
	 * This method returns the number of BaseConfiguration objects in the container
	 * 
	 * @return Number of BaseConfiguration objects
	 */
	public int size();

	/**
	 * Returns the index of the BaseConfiguration
	 * 
	 * @param config
	 *            component BaseConfiguration
	 */
	public int indexOf(BaseConfiguration config);

	/**
	 * Returns the index of the BaseConfiguration
	 * 
	 * @param name
	 *            component BaseConfiguration name to lookup
	 */
	public int indexOf(String name);

	/**
	 * Returns a List of BaseConfiguration items from this object and inherited containers
	 * 
	 * @param list
	 *            If not null, BaseConfiguration items are added to this list
	 * 
	 * @return The provided List or a new List object with the contents of
	 *         inherited configurations
	 */
	public List<BaseConfiguration> getInheritedConfigurations(List<BaseConfiguration> list);

	/**
	 * Returns a flattened list of the entire tree of config items.
	 * 
	 * @param list
	 *            If not null, config items are added to this list
	 * 
	 * @return The provided list or a new list object with the contents of this
	 *         container and child containers
	 */
	public List<BaseConfiguration> getConfigurations(List<BaseConfiguration> list);

	/**
	 * This method returns the configuration object at index
	 * 
	 * @param index
	 *            The configuration object
	 */
	public BaseConfiguration getConfig(int index);

	/**
	 * Returns the BaseConfiguration with the specified name traversing child objects of
	 * type ContainerConfig if recursive is true.
	 * 
	 * @param name
	 *            The config's name
	 * @param recursive
	 *            Traverse child ContainerConfigs if true
	 * 
	 * @return The configuration object or null if config wasn't found
	 */
	public BaseConfiguration getConfig(String name, boolean recursive);

	/**
	 * Returns the BaseConfiguration with the specified name in this container. This method
	 * does not traverse child containers.
	 * 
	 * @param name
	 *            The config's name
	 * 
	 * @return The configuration object or null if config wasn't found
	 */
	public BaseConfiguration getConfig(String name);

	/**
	 * This method adds a configuration object to the container
	 * 
	 * @param config
	 *            The configuration object
	 * 
	 * @return Index of the new configuration object
	 */
	public int addConfig(BaseConfiguration config);

	/**
	 * This method inesrts a configuration object at the specified index
	 * 
	 * @param config
	 *            The configuration object
	 * @param position
	 *            Position of the new object
	 * 
	 * @return Index of the new configuration object
	 */
	public int insertConfig(BaseConfiguration config, int position);

	/**
	 * This method removes a configuration object from the container
	 * 
	 * @param index
	 *            Index of the configuration object
	 */
	public BaseConfiguration removeConfig(int index);

	/**
	 * Removes a component from the container
	 * 
	 * @param config
	 *            component config
	 */
	public boolean removeConfig(BaseConfiguration config);

	/**
	 * This method removes a named configuration object from the container or
	 * its subcontainers
	 * 
	 * @param name
	 *            Name of the configuration object
	 * @param recursive
	 *            True if child containers should be searched
	 */
	public boolean removeConfig(String name, boolean recursive);

	/**
	 * Moves a BaseConfiguration one position up or down
	 * 
	 * @param position
	 *            Current connector position
	 * @param up
	 *            Up (true) or down (false)
	 * 
	 * @return true if the operation succeeded
	 */
	public boolean moveConfig(int position, boolean up);

	/**
	 * Moves a BaseConfiguration one position up or down
	 * 
	 * @param config
	 *            The BaseConfiguration to move
	 * @param up
	 *            Up (true) or down (false)
	 * 
	 * @return true if the operation succeeded
	 */
	public boolean moveConfig(BaseConfiguration config, boolean up);

	/**
	 * Moves a BaseConfiguration to a new position
	 * 
	 * @param config
	 *            Component BaseConfiguration
	 * @param position
	 *            New position
	 * 
	 * @return The components new position
	 */
	public int moveConfig(BaseConfiguration config, int position);

	/**
	 * Returns true if a BaseConfiguration with the provided name can be found.
	 * 
	 * @param name
	 *            The name of the BaseConfiguration item
	 * @param recursive
	 *            If true, a tree walk will be performed when checking for the
	 *            name
	 * 
	 * @return TRUE if there is an object with the provided name
	 */
	public boolean containsConfig(String name, boolean recursive);

}
