/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.xml.MetamergeConfigXML;

/**
 * Class used by the {@link MetamergeConfigImpl} and {@link MetamergeConfigXML}
 * classes as an internal cache of configurations.
 */
public class ConfigCache extends Hashtable<String,BaseConfiguration> {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -3311255731504174416L;

	/**
	 * Adds new configuration to the cache.
	 * 
	 * @param name
	 *            name of the new configuration
	 * @param data
	 *            configuration represented by that name
	 */
	public void addObject(Object name, BaseConfiguration data) {
		put(name.toString(), data);
	}

	/**
	 * @param name
	 *            name of object
	 * @return configuration of the specified object
	 */
	public BaseConfiguration getObject(Object name) {
		return get(name.toString());
	}

	/**
	 * @return vector of all objects in the cache
	 */
	public Vector<String> getDirtyList() {
		Vector<String> dirty = new Vector<String> ();

		for (Enumeration<String> e = keys(); e.hasMoreElements();) {
			String name = e.nextElement();
			BaseConfiguration config = get(name);
			if (config.getModified())
				dirty.add(name);
		}
		return dirty;
	}

	/**
	 * Removes specified configuration from the cache.
	 * 
	 * @param name
	 *            name of configuration
	 */
	public void removeObject(Object name) {
		remove(name.toString());
	}

}
