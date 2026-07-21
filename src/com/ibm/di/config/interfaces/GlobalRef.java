/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * Used by the Old Configuration Editor to create a global reference to an object in a
 * configuration file.
 * @deprecated This was used by the old Configuration Editor.
 */

import java.io.Serializable;
import java.util.Arrays;

import javax.naming.Name;

public class GlobalRef implements Serializable {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 366178307603105225L;

	private Name name;

	private String configURL;

	private BaseConfiguration config;

	private boolean isRemote;

	public GlobalRef(BaseConfiguration config) {
		this(config, false);
	}

	public GlobalRef(BaseConfiguration config, boolean ignoreMC) {
		this.name = config.getName();

		MetamergeConfig mc = null;

		if (config instanceof MetamergeConfig)
			mc = (MetamergeConfig) config;
		else if (config.getMetamergeConfig() != null)
			mc = config.getMetamergeConfig();

		isRemote = (mc == null) ? false : mc.isRemote();

		if (ignoreMC || mc == null) {
			this.configURL = null;
			this.config = config;
		} else {
			this.configURL = mc.toString();
			this.config = isRemote ? config : null;
		}
	}

	/**
	 * Returns the object this refers to Since a GlobalRef is
	 * serialized/deserialized, it may not be the same object that was
	 * originally used.
	 */
	public Object getObject() throws Exception {
		MetamergeConfig mc = getMetamergeConfig();
		if (mc == null)
			return config;
		else
			return mc.lookup(name);
	}

	/**
	 * Returns the object this refers to, in the local context if possible.
	 * Since a GlobalRef is serialized/deserialized, it may not be the same
	 * object that was originally used.
	 * 
	 * @param destination
	 *            The local MetamergeConfig we should try to find the object in
	 */
	public Object getObject(BaseConfiguration destination) throws Exception {
		MetamergeConfig local = null;
		if (destination instanceof MetamergeConfig)
			local = (MetamergeConfig) destination;
		else if (destination != null)
			local = destination.getMetamergeConfig();

		MetamergeConfig mc = null;
		if (configURL != null) {
			if (local != null && configURL.equals(local.toString()))
				mc = local;
			else if (!isRemote)
				mc = MetamergeConfigFactory.loadNamespace(configURL);
		}

		if (mc == null)
			return config;

		Object ret = mc.lookup(name);

		if (mc == local || local == null)
			return ret;

		// When either the original or receiving context is remote.
		// we had better flatten the object, I think. It is hard to
		// set up inheritance across file systems.
		if (ret instanceof BaseConfiguration
				&& (mc.isRemote() || local.isRemote())) {
			try {
				((BaseConfiguration) ret)
						.flatten(Arrays
								.asList(new String[] { MetamergeConfigFactory.SYSTEM_NAMESPACE }));
			} catch (Exception e) {
				// Should probably log this
			}
		}

		return ret;

	}

	public MetamergeConfig getMetamergeConfig() throws Exception {
		if (configURL != null && !isRemote)
			return MetamergeConfigFactory.loadNamespace(configURL);
		else
			return null;
	}

	public String toString() {
		return configURL + ":/" + name.toString();
	}

	public String getURL() {
		return configURL;
	}

}
