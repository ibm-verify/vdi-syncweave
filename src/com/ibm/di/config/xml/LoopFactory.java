/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LoopConfig;

/**
 * Read/Write {@link LoopConfig} elements in XML format.
 * 
 */
public class LoopFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String LOOP_TAG = "Loop";

	public final static String LOOP_TYPE_TAG = "LoopType";

	public final static String LOOP_COLLECTION_TAG = "LoopCollection";

	public final static String LOOP_WORK_NAME = "WorkAttributeName";

	public final static String LOOP_ATTR_NAME = "LoopAttributeName";

	public final static String LOOP_INIT_OPTION = "LoopInitOption";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration bconfig, Element elem) throws Exception {
		parse((LoopConfig) bconfig, elem);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration bconfig, Element elem) throws Exception {
		build((LoopConfig) bconfig, elem);
	}

	public void parse(LoopConfig config, Element elem) throws Exception {
		config.init();
		getBaseName(config, elem);

		// Loop Type
		String str = getNodeTextByName(elem, LOOP_TYPE_TAG);
		if (str != null && str.length() > 0)
			config.setLoopType(Integer.parseInt(str));

		// Loop init option (connector only)
		str = getNodeTextByName(elem, LOOP_INIT_OPTION);
		if (str != null && str.length() > 0)
			config.setInitConnectorOption(Integer.parseInt(str));

		// Loop attributes
		str = getNodeTextByName(elem, LOOP_WORK_NAME);
		if (str != null && str.length() > 0)
			config.setWorkAttributeName(str);

		str = getNodeTextByName(elem, LOOP_ATTR_NAME);
		if (str != null && str.length() > 0)
			config.setLoopAttributeName(str);

		switch (config.getLoopType()) {
		case LoopConfig.LOOP_CONNECTOR_FC:
			new ConnectorFactory().parse(config.getLoopConnector(),
					getSingleElement(elem, ConnectorFactory.CONNECTOR_TAG));
			config.getLoopConnector().setupInheritanceChain();
			break;
		case LoopConfig.LOOP_CONDITIONS:
			break;
		case LoopConfig.LOOP_COLLECTION:
			config.setParameter(LOOP_COLLECTION_TAG, getNodeTextByName(elem,
					LOOP_COLLECTION_TAG));
			break;
		}

		new BranchingFactory().parse(config, getSingleElement(elem,
				BranchingFactory.BRANCH_TAG));
	}

	public void build(LoopConfig config, Element elem) throws Exception {
		setBaseName(config, elem);

		Element e;

		setSingleElement(elem, LOOP_TYPE_TAG, config, InternalSchema.LOOP_TYPE);
		setSingleElement(elem, LOOP_WORK_NAME, config,
				InternalSchema.LOOP_WORK_NAME);
		setSingleElement(elem, LOOP_ATTR_NAME, config,
				InternalSchema.LOOP_ATTR_NAME);
		setSingleElement(elem, LOOP_INIT_OPTION, config,
				InternalSchema.LOOP_INIT_OPTION);

		if (config.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
			e = elem.getOwnerDocument().createElement(
					ConnectorFactory.CONNECTOR_TAG);
			elem.appendChild(e);
			new ConnectorFactory().build(config.getLoopConnector(), e);
		}

		// Save config container
		e = elem.getOwnerDocument().createElement(BranchingFactory.BRANCH_TAG);
		elem.appendChild(e);
		new BranchingFactory().build(config, e);
	}

}
