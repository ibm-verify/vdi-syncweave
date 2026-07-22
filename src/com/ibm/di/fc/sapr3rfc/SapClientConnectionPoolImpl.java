/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import java.util.Properties;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

final class SapClientConnectionPoolImpl implements SapClientConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String poolName;

	private int maxConnections = 1;

	// number of times we will allow a retry */
	private static int retries = 3;

	private Properties jcoProperties;

	private IRepository rfcRepository;

	SapClientConnectionPoolImpl(String poolName, int maxConnections,
			Properties jcoProperties) {
		this.poolName = poolName;
		this.maxConnections = maxConnections;
		this.jcoProperties = jcoProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#setup()
	 */
	public void setup() throws SapR3RfcFCException {
		try {
			// Create the client pool.
			JCO.addClientPool(poolName, maxConnections, jcoProperties);
		} catch (JCO.Exception x) {
			Object[] msgArgs = new Object[] { poolName, x.toString() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0016, msgArgs);
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.CONNECTION_POOL_EXISTS, msg);
		}

		// create the rfc repositry as well.
		setRfcRepository(JCO.createRepository(poolName.concat("_RFCREPOS"),
				poolName));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#connect()
	 */
	public JCO.Client connect() throws SapR3RfcFCException {
		try {
			return JCO.getClient(poolName);
		} catch (JCO.Exception x) {
			if (x.getGroup() == JCO.Exception.JCO_ERROR_RESOURCE) {
				Object[] msgArgs = new Object[] { poolName, x.toString() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0017, msgArgs);
				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.CONNECTION_POOL_EXISTS, msg);

			}

			Object[] msgArgs = new Object[] { poolName, x.toString() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0018, msgArgs);
			throw new SapR3RfcFCException(
					SapR3RfcFCErrorCodes.CONNECTION_ESTABLISHMENT, msg);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#disconnect(com.sap.mw.jco.JCO.Client)
	 */
	public void disconnect(JCO.Client client) throws SapR3RfcFCException {
		if (client != null) {
			try {
				JCO.releaseClient(client);
			} catch (JCO.Exception x) {
				throw new SapR3RfcFCException(
						SapR3RfcFCErrorCodes.DISCONNECTION, x.getMessage(), x);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfc.SapClientConnection#teardown()
	 */
	public void teardown() throws SapR3RfcFCException {
		JCO.removeClientPool(poolName);
	}

	private void setRfcRepository(IRepository rfcRepository) {
		this.rfcRepository = rfcRepository;
	}

	public IRepository getRfcRepository() {
		return rfcRepository;
	}

	public int maxRetries() {
		return retries;
	}
}
