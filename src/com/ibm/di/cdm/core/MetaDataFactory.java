/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import com.ibm.di.cdm.itregistry.ITRegistryMetaData;
import com.ibm.di.cdm.jar.JarMetaData;

/**
 * This factory is used to create {@link MetaData} objects which permit to
 * access the CDM meta-data definitions (provided either as a JAR file or by an
 * IT registry).
 */
public class MetaDataFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Returns a {@link MetaData} object which can be used to retrieve CDM
	 * meta-data definitions from an IT registry.
	 * 
	 * @param jdbcUrl
	 *            the JDBC URL for connecting to IT registry.
	 * @param jdbcDriver
	 *            the JDBC driver for connecting to IT registry.
	 * @param dbUsername
	 *            the username for connecting to IT registry.
	 * @param dbPassword
	 *            the password for connecting to IT registry.
	 * 
	 * @return a {@link MetaData} object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public static MetaData getITRegistryMetaData(String jdbcUrl, String jdbcDriver, String dbUsername, String dbPassword)
			throws Exception {
		return new ITRegistryMetaData(jdbcUrl, jdbcDriver, dbUsername, dbPassword);
	}

	/**
	 * Returns a {@link MetaData} object which can be used to retrieve CDM
	 * meta-data definitions from a jar file.
	 * 
	 * @return a {@link MetaData} object.
	 */
	public static MetaData getJarMetaData() {
		return new JarMetaData();
	}
}