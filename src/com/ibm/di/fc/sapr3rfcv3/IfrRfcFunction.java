/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.sap.conn.jco.*;

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
	void importRequestData(IfrImporter importer, JCoFunction jcoFunction) throws SapRfcFunctionException;

	/**
	 * Execute the function.
	 * 
	 * @param conn -
	 *            client connection to R/3
	 */
//	void execute(JCO.Client conn);
	void execute(JCoFunction function) throws JCoException;

	/**
	 * Export the response data. Should be called after {@link #execute}.
	 * 
	 * @param serializer
	 * @throws SapRfcFunctionException
	 */
	void exportResponseData(IfrSerializer serializer)
			throws SapRfcFunctionException;
	
	String getDestinationName();
	void setDestinationName(String destinationName);
}
