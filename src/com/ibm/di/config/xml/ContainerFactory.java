/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Read/write {@link ContainerConfig} elements in XML format
 *
 */
public class ContainerFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String CONTAINER_TAG = "Container";

	public final static String PARAMETER_TAG = "ParameterList";

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration bconfig, Element elem) throws Exception {
		parse(bconfig, elem, true);
	}

	public void parse(BaseConfiguration bconfig, Element elem, boolean getbase)
			throws Exception {
		ContainerConfig config = (ContainerConfig) bconfig;
		config.init();

		if (getbase)
			getBaseName(config, elem);

		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			String tag = list.item(i).getNodeName();
			if (tag.equalsIgnoreCase("#text")) {
				continue;
			} else if (tag.equals(PARAMETER_TAG)) {
				BaseConfiguration impl = new com.ibm.di.config.base.BaseConfigurationImpl();
				config.addConfig(impl);
				impl.init();
				getParameters((Element) list.item(i), impl);
			} else if (!tag.equals(MOD_TS_TAG)){
				BaseConfiguration impl = Factories.getImpl(tag);
				config.addConfig(impl);
				Factories.getFactory(tag).parse(impl, (Element) list.item(i));
				impl.init();
				impl.setupInheritanceChain();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration bconfig, Element elem) throws Exception {
		build(bconfig, elem, true);
	}

	public void build(BaseConfiguration bconfig, Element elem, boolean setbase)
			throws Exception {
		ContainerConfig config = (ContainerConfig) bconfig;
		if (setbase)
			setBaseName(config, elem);

		for (int i = 0; i < config.size(); i++) {
			BaseConfiguration bc = config.getConfig(i);

			if (bc.getClass().getName().equals(
					"com.ibm.di.config.base.BaseConfigurationImpl")) {
				setParameters(elem, bc, PARAMETER_TAG);
			} else {
				String tag = Factories.getClassTag(bc);
				if (tag == null) {
					throw new Exception(sResHash.getString(
							"MMCONFIG.CONTAINERFACT.CANNOT.SERIALIZE", bc
									.getClass().getName()));
				}

				Element e = elem.getOwnerDocument().createElement(tag);
				elem.appendChild(e);
				Factories.getFactory(tag).build(bc, e);
			}
		}
	}

}
