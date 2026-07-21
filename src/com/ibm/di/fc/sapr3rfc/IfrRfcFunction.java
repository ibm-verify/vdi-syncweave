/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.JCO;

/**
 * Representation of a dynamic RFC proxy. Concreate impls are expected to
 * contain a JCO.Function instance. This interfaces allows the JCO.Function to
 * be adapted for IFR standard conformace.
 */
interface IfrRfcFunction {

	/**
	 * Process request data. Should be called before {@link #execute}.
	 * 
	 * @param importer
	 * @throws SapRfcFunctionException
	 */
	void importRequestData(IfrImporter importer) throws SapRfcFunctionException;

	/**
	 * Execute the function.
	 * 
	 * @param conn -
	 *            client connection to R/3
	 */
	void execute(JCO.Client conn);

	/**
	 * Export the response data. Should be called after {@link #execute}.
	 * 
	 * @param serializer
	 * @throws SapRfcFunctionException
	 */
	void exportResponseData(IfrSerializer serializer)
			throws SapRfcFunctionException;
}
