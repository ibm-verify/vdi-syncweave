/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.InstanceConfig;

/**
 * Read/Write {@link InstanceConfig} elements in XML format.
 *
 */
public class InstanceFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String INSTANCE_TAG = "InstanceProperties";

	public final static String AUTOSTART_TAG = "AutoStart";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration bconfig, Element elem) throws Exception {
		InstanceConfig config = (InstanceConfig) bconfig;
		Element e;

		config.init();

		// Get name and initialize
		getBaseName(config, elem);

		// Instance ID
		config.setInstanceID(getNodeTextByName(elem, INSTANCE_TAG));

		// Startup items container
		if ((e = getSingleElement(elem, AUTOSTART_TAG)) != null)
			new ContainerFactory().parse(config.getStartupItems(), e);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration bconfig, Element elem) throws Exception {
		InstanceConfig config = (InstanceConfig) bconfig;

		// Set name and initialize
		setBaseName(config, elem);

		// Instance ID
		setSingleElement(elem, INSTANCE_TAG, config.getInstanceID());

		// Startup items container
		Element e = elem.getOwnerDocument().createElement(AUTOSTART_TAG);
		elem.appendChild(e);
		new ContainerFactory().build(config.getStartupItems(), e);
	}

}
