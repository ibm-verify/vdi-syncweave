/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.core;

import com.ibm.di.cdm.jar.JarNamingRulesReader;
import com.ibm.tivoli.dataintegration.metadata.MetadataService;

/**
 * A static Factory for creating readers for the different CDM-aware systems
 * (e.g. TADDM, DIS, IdML books).
 * 
 */
public class NamingRulesReaderFactory {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Returns the {@link NamingRulesReader} for JAR meta-data.
	 * 
	 * @return the {@link NamingRulesReader}.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public static final NamingRulesReader getJarNamingRulesReader() throws Exception {
		return new JarNamingRulesReader();
	}

	/**
	 * Returns the {@link NamingRulesReader} for IT registry (DIS) meta-data.
	 * <b>Note:</b> Will be added later during the DII harmonization item.
	 * 
	 * @param service
	 *            the IT registry meta-data service.
	 * @return the {@link NamingRulesReader}.
	 */
	public static final NamingRulesReader getITRegistryNamingRulesReader(MetadataService service) {
		throw new UnsupportedOperationException();
	}

}