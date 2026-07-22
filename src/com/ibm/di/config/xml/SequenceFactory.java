/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.config.interfaces.SequenceConfig;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SequenceFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String SEQUENCE_TAG = "Sequence";

	private static final String ITEM_TAG = "Item";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration bconfig, Element elem) throws Exception {
		SequenceConfig config = (SequenceConfig) bconfig;
		config.init();
		config.setEnabled(true);

		getBaseName(config, elem);

		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			String tag = list.item(i).getNodeName();
			if (tag.equals(ITEM_TAG)) {
				BaseConfiguration impl = new BaseConfigurationImpl();
				impl.init();
				getParameters((Element) list.item(i), impl);
				config.addConfig(impl);
			} else if (tag.equals( ScriptFactory.SCRIPT_TAG )) {
				ScriptConfig sc = (ScriptConfig) getImpl(tag);
				getFactory(tag).parse(sc, (Element) list.item(i));
				config.addConfig(sc);
			} else if (tag.equals( LoggingFactory.LOGGING_TAG)) {
				getFactory(tag).parse(config.getLogConfig(), (Element) list.item(i));				
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration bconfig, Element elem) throws Exception {
		SequenceConfig config = (SequenceConfig) bconfig;

		setBaseName(config, elem);

		for (int i = 0; i < config.size(); i++) {
			BaseConfiguration item = config.getConfig(i);
			if (item instanceof ScriptConfig) {
				Element e = elem.getOwnerDocument().createElement(ScriptFactory.SCRIPT_TAG);
				elem.appendChild(e);
				getFactory(ScriptFactory.SCRIPT_TAG).build(item, e);
			} else {
				setParameters(elem, item, ITEM_TAG);
			}
		}

		// Log settings
		Element e = elem.getOwnerDocument().createElement(LoggingFactory.LOGGING_TAG);
		elem.appendChild(e);
		getFactory(LoggingFactory.LOGGING_TAG).build(config.getLogConfig(), e);

	}

}
