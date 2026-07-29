/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

/**
 * Allow different mechanism for client to connect to SAP.
 */
interface SapClientConnection {

	/**
	 * Called once to setup any information required before establishing a
	 * connection.
	 * 
	 * @throws SapR3RfcFCException
	 */
	void setup() throws SapR3RfcFCException;

	/**
	 * Called once to clean up a connection that has already been established.
	 */
	void teardown() throws SapR3RfcFCException;

	/**
	 * This is called to retrieve a client connection. It is up to the
	 * implmenter of to determine when the connection is actually established.
	 * 
	 * @return JCO.Client a reference of the SAP R/3 client
	 * @throws SapR3RfcFCException
	 */
	JCO.Client connect() throws SapR3RfcFCException;

	/**
	 * Called once after the current connection has been finished with. It is up
	 * to the implementer to determine when the connection will actually be
	 * released - either here or in teardown().
	 * 
	 * @param client
	 * @throws SapR3RfcFCException
	 */
	void disconnect(JCO.Client client) throws SapR3RfcFCException;

	IRepository getRfcRepository();

	int maxRetries();
}
