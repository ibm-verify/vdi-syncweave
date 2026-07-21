/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.*;

import com.ibm.di.config.interfaces.*;

/**
 * Read/Write {@link SandboxConfig} elements in XML.
 */
public class SandboxFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SANDBOX_TAG = "SandboxConfig";

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
