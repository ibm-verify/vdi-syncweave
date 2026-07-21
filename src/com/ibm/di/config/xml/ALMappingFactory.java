/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;

/**
 * Implements the reading and writing of a {@link ALMappingConfig}
 */
public class ALMappingFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of tag - {@value #ALMAPPING_TAG}
	 */
	public final static String ALMAPPING_TAG = "ALMap";

	/**
	 * Name of tag - {@value #ENABLED_TAG}
	 */
	public final static String ENABLED_TAG = "Enabled";

	/**
	 * Name of tag - {@value #STATE_TAG}
	 */
	public final static String STATE_TAG = "State";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		Element e;
		String str;
		ALMappingConfig fc = (ALMappingConfig) config;

		fc.init();

		getBaseName(fc, elem);

		// Attribute Map
		if ((e = getSingleElement(elem, ConnectorFactory.ATTRIBUTE_MAP)) != null)
			((ConnectorFactory) Factories
					.getFactory(ConnectorFactory.CONNECTOR_TAG))
					.getAttributeMap(e, fc.getAttributeMap());

		getNullBehavior(elem, fc);
		
		// Enabled flag, backwards compatibility
		if ((str = getNodeTextByName(elem, ENABLED_TAG)) != null)
			config.setEnabled(Boolean.valueOf(str).booleanValue());

		// State flag (the new enabled)
		if ((str = getNodeTextByName(elem, STATE_TAG)) != null)
			fc.setState(str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		ConnectorFactory cf = (ConnectorFactory) Factories
				.getFactory(ConnectorFactory.CONNECTOR_TAG);
		ALMappingConfig fc = (ALMappingConfig) config;

		setBaseName(config, elem);

		// AttributeMap
		cf.setAttributeMap(fc.getAttributeMap(), elem);

		setNullBehavior(fc, elem);
		
		// Enabled flag
		setSingleElement(elem, STATE_TAG, config,
				InternalSchema.CONNECTOR_STATE);
	}

}
