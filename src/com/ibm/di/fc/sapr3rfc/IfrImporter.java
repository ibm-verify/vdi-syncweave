/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.JCO;

/**
 * Deserialize a persistent representation of a JCO.Function data into a given
 * JCO.Function instance.
 */

interface IfrImporter {

	String ITEM = "item";

	/**
	 * Populate the JCO.Function parameters from a persistent format. Typcally
	 * done before exectuing the function.
	 * 
	 * @param func
	 * @throws IfrImporterException
	 */
	void importData(JCO.Function func) throws IfrImporterException;

	/**
	 * The name of the function represented by this importer.
	 * 
	 * @return
	 */
	String getFunctionName();

}
