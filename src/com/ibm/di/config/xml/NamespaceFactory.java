/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.*;

/**
 * Read/Write {@link NamespaceConfig} elements in XML.
 * 
 */
public class NamespaceFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String NAMESPACE_TAG = "Include";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		getBaseName(config, elem);
		getParameters(elem, config);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		setBaseName(config, elem);
		setParameters(elem, config, null);
	}
}
