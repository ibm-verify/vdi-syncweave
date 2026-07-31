/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import javax.naming.Name;

/**
 * This class contains a utility method for creating a configuration object.
 *
 */
public class ConfigUtils {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Creates a new object in the default folder location for the type.
	 * 
	 * @param config
	 *            configuration
	 * @param type
	 *            type of configuration
	 * @param name
	 *            name of configuration
	 * @return the newly created object
	 * @throws Exception
	 *             if could not add the newly created object
	 */
	public static BaseConfiguration createStandardObject(
			MetamergeConfig config, int type, String name) throws Exception {
		MetamergeFolder defaultFolder = config.getDefaultFolder(type);
		BaseConfiguration base = config.newInstanceOf(type);
		Name newName = (Name) defaultFolder.getName().clone();
		newName.add(name);
		base.setName(newName);
		config.bind(newName, base);
		return base;
	}

}
