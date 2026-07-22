/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.util.Properties;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO.Client;
import com.sap.mw.jco.JCO;

final class SapClientConnectionDirectImpl implements SapClientConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Properties jcoProperties;

	private IRepository rfcRepository;

	private Client jcoClient; // JCO client

	private static final String REPOSITORY_NAME = "IDISAPR3_REPOS";

	private int retries = 1;

	SapClientConnectionDirectImpl(Properties jcoProperties) {
		this.jcoProperties = jcoProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#setup() For this
	 *      implmentation, we create the connection at startup.
	 */
	public void setup() throws SapR3RfcFCException {
		initJcoClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#teardown()
	 */
	public void teardown() throws SapR3RfcFCException {
		try {
			getJcoClient().disconnect();
		} catch (JCO.Exception x) {
			Object[] msgArgs = new Object[] { x.getMessage() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0015, msgArgs);
			throw new SapR3RfcFCException(SapR3RfcFCErrorCodes.DISCONNECTION,
					msg, x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#connect()
	 */
	public JCO.Client connect() throws SapR3RfcFCException {
		try {
			if (getJcoClient().getState() == JCO.STATE_DISCONNECTED) {
				getJcoClient().connect();
			}
		} catch (JCO.Exception jcoe) {
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.CONNECTION_ESTABLISHMENT, jcoe
							.getMessage());
		}
		return getJcoClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#disconnect(com.sap.mw.jco.JCO.Client)
	 *      Since we want to keep connected for as long as possible, we don't do
	 *      anything here.
	 */
	public void disconnect(Client client) throws SapR3RfcFCException {
		// We don't want to disconnect the session here, keep it
		// open for as long as possible to avoid connection setup cost.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#getRfcRepository()
	 */
	public IRepository getRfcRepository() {
		if (rfcRepository == null) {
			initRfcRepository();
		}
		return rfcRepository;
	}

	private void initJcoClient() {
		jcoClient = JCO.createClient(getJcoProperties());
	}

	/**
	 * Retrieve our local JCO client.
	 * 
	 * @return JCO.Client
	 */
	private Client getJcoClient() {
		if (jcoClient == null) {
			initJcoClient();
		}
		return jcoClient;
	}

	/**
	 * Retun the local copy of the JCO properties we are to use.
	 * 
	 * @return Properties
	 */
	private Properties getJcoProperties() {
		return jcoProperties;
	}

	private void initRfcRepository() {
		setRfcRepository(JCO
				.createRepository((REPOSITORY_NAME), getJcoClient()));
	}

	/**
	 * Set the RFC repository.
	 * 
	 * @param repository
	 *            JCO.IRepository Set object used to access SAPs RFM Meta data.
	 */
	private void setRfcRepository(IRepository repository) {
		rfcRepository = repository;
	}

	/**
	 * Get the number of retries to be attempted.
	 * 
	 * @return int the maximum number of retries to restablish a connection
	 */
	public int maxRetries() {
		return retries;
	}
}
