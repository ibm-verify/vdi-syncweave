/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeFolder;

/**
 * Read/write {@link MetamergeFolder} elements in XML format.
 *
 */
public class FolderFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String FOLDER_TAG = "Folder";

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element elem) throws Exception {
		getBaseName(config, elem);
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element elem) throws Exception {
		setBaseName(config, elem);
	}
}
