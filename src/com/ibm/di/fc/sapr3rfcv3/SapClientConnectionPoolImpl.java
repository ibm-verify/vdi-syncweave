/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;

final class SapClientConnectionPoolImpl implements SapClientConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// number of times we will allow a retry */
	private static int retries = 3;

	private JCoRepository rfcRepository;
	public String destinationName;
	
//	public static final String DESTINATION_NAME = "DESTINATION_WITH_POOL";

	SapClientConnectionPoolImpl(String poolName, int maxConnections,
			Properties jcoProperties) throws IOException {
		this.destinationName = generateDestinationName(jcoProperties);
		createDestinationDataFile(this.destinationName, jcoProperties);
	}

	/**
	 * Generates the temp name used for the destination file.
	 * 
	 * @return String with name of file
	 * @throws IOException
	 */
	public static String generateDestinationName(Properties props)
			throws IOException {
		String destinationName = File.createTempFile("SAPALE_", "").getName();
		props.setProperty("DESTINATION_NAME", destinationName);
		props.setProperty("jco.client.dest", destinationName);
		return destinationName;
	}
	
	static void createDestinationDataFile(String destinationName, Properties connectionProperties)
	{
		File destCfg = new File(destinationName+".jcoDestination");
		Properties conn = new Properties();
		if(connectionProperties != null)
		{
			setProp(conn, DestinationDataProvider.JCO_ASHOST, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_APPLICATION_SERVER));
			setProp(conn, DestinationDataProvider.JCO_GWHOST, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_GATEWAY_HOST));
			setProp(conn, DestinationDataProvider.JCO_USER, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_USER));
			setProp(conn, DestinationDataProvider.JCO_CLIENT, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_CLIENT));
			setProp(conn, DestinationDataProvider.JCO_SYSNR, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_SYSNUMBER));
			setProp(conn, DestinationDataProvider.JCO_PASSWD, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_PASSWORD));
			setProp(conn, DestinationDataProvider.JCO_LANG, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_LANGUAGE));
			setProp(conn, DestinationDataProvider.JCO_POOL_CAPACITY, "1");
			setProp(conn, DestinationDataProvider.JCO_PEAK_LIMIT, "1");
			setProp(conn, DestinationDataProvider.JCO_MSHOST, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_MESSAGE_SERVER));
			setProp(conn, DestinationDataProvider.JCO_R3NAME, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_R3NAME));
			setProp(conn, DestinationDataProvider.JCO_GROUP, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_GROUP));
			setProp(conn, DestinationDataProvider.JCO_TYPE, connectionProperties.getProperty(SapR3RfcFCV3.PARAM_CONFIG_TYPE));
		}
		
		try
		{
			FileOutputStream fos = new FileOutputStream(destCfg, false);
			conn.store(fos, "tests!");
			fos.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Unable to create the destination files", e);
		}
	}
	
	private static void setProp(Properties p, String key, String value) {
		if (value != null && value.length()>0)
			p.setProperty(key, value);
	}
	
	/* This will not be called with the JCO3
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#setup()
	 */
	public void setup() throws SapR3RfcFCException {
//		try {
			// Create the client pool.
//			JCO.addClientPool(poolName, maxConnections, jcoProperties);
//		} catch (JCoException x) {
//			Object[] msgArgs = new Object[] { poolName, x.toString() };
//			String msg = LogMessageHelper.getMsgResource().getMessage(
//					LogMessageHelper.SAPR3_RFCFC_0016, msgArgs);
//			throw new SapR3RfcFCException(
//					SapR3RfcFCErrorCodes.CONNECTION_POOL_EXISTS, msg);
//		}
//
		// create the rfc repositry as well.
//		setRfcRepository(JCO.createRepository(poolName.concat("_RFCREPOS"),
//				poolName));
	}

	/* This is not used with JCO3
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#connect()
	 */
//	public JCO.Client connect() throws SapR3RfcFCException {
//		try {
//			return JCO.getClient(poolName);
//		} catch (JCO.Exception x) {
//			if (x.getGroup() == JCO.Exception.JCO_ERROR_RESOURCE) {
//				Object[] msgArgs = new Object[] { poolName, x.toString() };
//				String msg = LogMessageHelper.getMsgResource().getMessage(
//						LogMessageHelper.SAPR3_RFCFC_0017, msgArgs);
//				throw new SapR3RfcFCException(
//						SapR3RfcFCErrorCodes.CONNECTION_POOL_EXISTS, msg);
//
//			}
//
//			Object[] msgArgs = new Object[] { poolName, x.toString() };
//			String msg = LogMessageHelper.getMsgResource().getMessage(
//					LogMessageHelper.SAPR3_RFCFC_0018, msgArgs);
//			throw new SapR3RfcFCException(
//					SapR3RfcFCErrorCodes.CONNECTION_ESTABLISHMENT, msg);
//		}
//	}

	/* Connection handling is done by JCO 3
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#disconnect(com.sap.mw.jco.JCO.Client)
	 */
//	public void disconnect(JCO.Client client) throws SapR3RfcFCException {
//		if (client != null) {
//			try {
//				JCO.releaseClient(client);
//			} catch (JCO.Exception x) {
//				throw new SapR3RfcFCException(
//						SapR3RfcFCErrorCodes.DISCONNECTION, x.getMessage(), x);
//			}
//		}
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#teardown()
	 */
//	public void teardown() throws SapR3RfcFCException {
//		JCO.removeClientPool(poolName);
//	}

	private void setRfcRepository(JCoRepository rfcRepository) {
		this.rfcRepository = rfcRepository;
	}

	public JCoRepository getRfcRepository() {
		return rfcRepository;
	}

	public int maxRetries() {
		return retries;
	}

	@Override
	public String getDestinationName() {
		// TODO Auto-generated method stub
		return this.destinationName;
	}
	
	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		File file = new java.io.File(destinationName + ".jcoDestination");
		file.delete();
	}

	@Override
	public void unregister() {
		// TODO Auto-generated method stub
		
	}
}
