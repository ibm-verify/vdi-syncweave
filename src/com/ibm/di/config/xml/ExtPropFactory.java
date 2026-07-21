/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ExternalPropertiesConfig;

/**
 * Read/write {@link ExternalPropertiesConfig} elements in XML format.
 * @deprecated ExternalProperties are deprecated.
 */
public class ExtPropFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String EXTPROP_TAG = "ExternalProperties";

	public final static String EXTPROP_PATH = "Path";

	public final static String EXTPROP_ENCRYPTED = "Encrypted";

	public final static String EXTPROP_PASSWORD = "Password";

	public final static String EXTPROP_SIBLINGS = "QuerySiblings";

	public final static String EXTPROP_CIPHER = "Cipher";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		ExternalPropertiesConfig ep = (ExternalPropertiesConfig) config;

		// Set name and initialize
		getBaseName(ep, elem);

		String str = getNodeTextByName(elem, EXTPROP_PATH);
		if (str != null)
			ep.setFilePath(str);

		str = getNodeTextByName(elem, EXTPROP_ENCRYPTED);
		if (str != null)
			ep.setEncrypted(Boolean.valueOf(str).booleanValue());

		str = getNodeTextByName(elem, EXTPROP_PASSWORD);
		if (str != null)
			ep.setPassword(str);

		str = getNodeTextByName(elem, EXTPROP_CIPHER);
		if (str != null)
			ep.setCipher(str);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		ExternalPropertiesConfig ep = (ExternalPropertiesConfig) config;

		setBaseName(ep, elem);

		setSingleElement(elem, EXTPROP_PATH, ep,
				InternalSchema.EXTPROP_FILE_PATH);
		setSingleElement(elem, EXTPROP_ENCRYPTED, ep,
				InternalSchema.EXTPROP_ENCRYPTED);
		setSingleElement(elem, EXTPROP_PASSWORD, ep,
				InternalSchema.EXTPROP_PASSWORD);
		setSingleElement(elem, EXTPROP_CIPHER, ep,
				InternalSchema.EXTPROP_CIPHER);
	}
}
