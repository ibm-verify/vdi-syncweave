/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
import java.util.*;
/**
 * Implements Configuration Instance parameters,
 * e.g the name of the Config Instance and which AssemblyLines should
 * be automatically started.
 * Note that these parameters are only used when getEnabled() returns true.
 *
 */
public class InstanceConfigImpl extends BaseConfigurationImpl implements
		InstanceConfig {

	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7052997089129596762L;

	private final static String P_ID = "instanceID";

	private final static String P_AUTOSTART = "autoStart";

	private ContainerConfig autoStart;

	/**
	 * Constructors
	 */
	public InstanceConfigImpl() {
		super();
	}

	public InstanceConfigImpl(Object data) {
		super(data);
	}

	/**
	 * init - method called after internal data structure is set
	 */
	public void init() throws Exception {
		if (autoStart == null) {
			autoStart = new ContainerConfigImpl(getParameter(P_AUTOSTART,
					new TreeMap<String,Object>()));
			autoStart.init();
			autoStart.setParent(this);
		}
	}

	/**
	 * Returns the instance identifier for this configuration.
	 */
	public String getInstanceID() {
		return getStringParameter(P_ID);
	}

	/**
	 * Returns the instance identifier for this configuration.
	 */
	public void setInstanceID(String id) {
		setStringParameter(P_ID, id);
	}

	/**
	 * Returns the "list" of AssemblyLines that should be auto-started
	 */
	public ContainerConfig getStartupItems() {
		return autoStart;
	}

	/**
	 * Return self clone
	 * 
	 * @return A cloned object of this
	 */
	public Object getClone() throws Exception {
		InstanceConfigImpl bc = new InstanceConfigImpl(deepClone(null));
		bc.setName(getName());
		bc.init();
		bc.setMetamergeConfig(getMetamergeConfig());
		bc.setModTS(getModTS());

		return bc;
	}
}
