/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.*;

import com.ibm.di.config.interfaces.*;

/**
 * Read/Write {@link ParserConfig} elements in XML
 */
public class ParserFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String PARSER_TAG = "Parser";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		getBaseName(config, elem);
		getParameters(elem, config);
		getSchemas(elem, (ParserConfig) config);
	}

	private void getSchemas(Element elem, ParserConfig config) throws Exception {
		// Make sure the schemas are present
		if (config.getSchema(true) == null)
			config.init();
		NodeList list = elem.getElementsByTagName(SchemaFactory.SCHEMA_TAG);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			String name = e.getAttribute(NAME_ATTRIBUTE);
			getFactory(SchemaFactory.SCHEMA_TAG).parse(config.getSchema(name),
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		setBaseName(config, elem);
		setParameters(elem, config, null);
		setSchemas(elem, (ParserConfig) config);
	}

	private void setSchemas(Element elem, ParserConfig config) throws Exception {
		if (config.getSchema(true) == null)
			config.init();
		setOneSchema(elem, config.getSchema(true));
		setOneSchema(elem, config.getSchema(false));
	}

	private void setOneSchema(Element elem, SchemaConfig config)
			throws Exception {
		// Some code to avoid writing empty schemas
		if (config.getData() == null || config.getData().size() == 0)
			return;
		String ref = config.getInheritsFromRef();
		if (config.getData().size() == 1 && ref != null && ref.equals(BaseConfiguration.INHERIT_PARENT))
			return;
		getFactory(SchemaFactory.SCHEMA_TAG).build(config, elem);
	}
}
