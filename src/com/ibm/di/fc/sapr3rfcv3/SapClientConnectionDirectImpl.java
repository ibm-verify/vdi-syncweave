/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;

public final class SapClientConnectionDirectImpl implements SapClientConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Properties jcoProperties;

	private JCoRepository rfcRepository;

	LogProxy logProxy;

	// for encryption algorithm
	private static final String ALGORITHM = "AES";
	private static SecretKey key;
	static {
		try {
			key = KeyGenerator.getInstance(ALGORITHM).generateKey();		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(); // cannot happen, support for AES is required
		}
	}

	// private Client jcoClient; // JCO client

	// private static final String REPOSITORY_NAME = "IDISAPR3_REPOS";
	// public static final String DESTINATION_NAME = "DESTINATION_WITHOUT_POOL";
//	private boolean createdDestination = false;
//	public static boolean createdDestination = false;
	private int retries = 1;
	// Create our own implementation of destination provider so that we can
	// encrypt and decrypt the password field.
	public SapDestinationDataProvider sapDestinationDataProvider;

	public String destinationName;

	SapClientConnectionDirectImpl(Properties jcoProperties) throws IOException {
		logProxy = new LogProxyImpl();
		this.jcoProperties = jcoProperties;

		sapDestinationDataProvider = new SapDestinationDataProvider();

		this.destinationName = generateDestinationName(jcoProperties);
		createDestinationDataFile(jcoProperties);

		Properties connectionProperties = new Properties();
		// Read properties from a file where the password is encrypted.
		// Decrypt password in memory and store it together with the other properties
		// in the connectionProperties object

		String decryptedPasswd = null;

		try {
			decryptedPasswd = decrypt(getPassword());
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setProp(connectionProperties, DestinationDataProvider.JCO_ASHOST,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_APPLICATION_SERVER));
		setProp(connectionProperties, DestinationDataProvider.JCO_GWHOST,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_GATEWAY_HOST));
		setProp(connectionProperties, DestinationDataProvider.JCO_CLIENT,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_CLIENT));
		setProp(connectionProperties, DestinationDataProvider.JCO_USER,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_USER));
		setProp(connectionProperties, DestinationDataProvider.JCO_PASSWD,
				decryptedPasswd);
		setProp(connectionProperties, DestinationDataProvider.JCO_SYSNR,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_SYSNUMBER));

		setProp(connectionProperties, DestinationDataProvider.JCO_LANG,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_LANGUAGE));
		setProp(connectionProperties, DestinationDataProvider.JCO_MSHOST,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_MESSAGE_SERVER));
		setProp(connectionProperties, DestinationDataProvider.JCO_R3NAME,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_R3NAME));
		setProp(connectionProperties, DestinationDataProvider.JCO_GROUP,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_GROUP));
		setProp(connectionProperties, DestinationDataProvider.JCO_TYPE,
				jcoProperties.getProperty("jco.client."
						+ SapR3RfcFCV3.PARAM_CONFIG_TYPE));

		sapDestinationDataProvider.changeProperties("CREATE", destinationName, connectionProperties);

	}
	
	private static void setProp(Properties p, String key, String value) {
		if (value != null && value.length()>0)
			p.setProperty(key, value);
	}
	
	private String getDestinationFileName() {
		return destinationName + ".jcoDestination";
	}

	public String getDestinationName() {
		return this.destinationName;
	}

	public void createDestinationDataFile(Properties connectionProperties) {
//		System.out.println("Creating destination data file " + destinationName
//				+ ".jcoDestination");
		File destCfg = new File(destinationName + ".jcoDestination");

		Properties conn = new Properties();
		if (connectionProperties != null) {
			setProp(conn, DestinationDataProvider.JCO_ASHOST,
						connectionProperties.getProperty("jco.client."
								+ SapR3RfcFCV3.PARAM_CONFIG_APPLICATION_SERVER));
			setProp(conn, DestinationDataProvider.JCO_GWHOST,
					connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_GATEWAY_HOST));
			setProp(conn, DestinationDataProvider.JCO_USER,
						connectionProperties.getProperty("jco.client."
								+ SapR3RfcFCV3.PARAM_CONFIG_USER));
			setProp(conn, DestinationDataProvider.JCO_CLIENT,
						connectionProperties.getProperty("jco.client."
								+ SapR3RfcFCV3.PARAM_CONFIG_CLIENT));
			setProp(conn, DestinationDataProvider.JCO_SYSNR,
						connectionProperties.getProperty("jco.client."
								+ SapR3RfcFCV3.PARAM_CONFIG_SYSNUMBER));

			String password = connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_PASSWORD);
			if (password != null && password.length() > 0) {
				try {
					conn.setProperty(DestinationDataProvider.JCO_PASSWD,
							encrypt(password));
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			setProp(conn, DestinationDataProvider.JCO_LANG,
						connectionProperties.getProperty("jco.client."
								+ SapR3RfcFCV3.PARAM_CONFIG_LANGUAGE));
			setProp(conn, DestinationDataProvider.JCO_MSHOST,
					connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_MESSAGE_SERVER));
			setProp(conn, DestinationDataProvider.JCO_R3NAME,
					connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_R3NAME));
			setProp(conn, DestinationDataProvider.JCO_GROUP,
					connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_GROUP));
			setProp(conn, DestinationDataProvider.JCO_TYPE,
					connectionProperties.getProperty("jco.client."
							+ SapR3RfcFCV3.PARAM_CONFIG_TYPE));
		}

		try {
			FileOutputStream fos = new FileOutputStream(destCfg, false);
			conn.store(fos, "tests!");
			fos.close();
			destCfg.deleteOnExit();
		} catch (Exception e) {
			throw new RuntimeException(
					"Unable to create the destination files", e);
		}
		// createdDestination = true;
	}

	/*
	 * Encryption of a String
	 */
	public static String encrypt(String text) throws GeneralSecurityException, IOException {
	    byte[] input = text.getBytes("UTF8");

	    Cipher cipher = Cipher.getInstance(ALGORITHM);
	    cipher.init(Cipher.ENCRYPT_MODE, key);

	    byte[] output = cipher.doFinal(input);

	    return Base64.getEncoder().encodeToString(output);
	}
		
	/*
	 * Decryption of a String
	 */
	
	public static String decrypt(String data) throws GeneralSecurityException, IOException {
	    Cipher cipher = Cipher.getInstance(ALGORITHM);
	    cipher.init(Cipher.DECRYPT_MODE, key);
	    byte[] decoded = cipher.doFinal(Base64.getDecoder().decode(data));

	    return new String(decoded, "UTF8");
	}
	
	/*
	 * Read properties file
	 */
	private String getPassword() throws IOException {

		Properties prop = new Properties();
		InputStream input = null;
		String passwd = null;
		try {

			input = new FileInputStream(getDestinationFileName());

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			passwd = prop.getProperty("jco.client.passwd");
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return passwd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#setup() For this
	 * implmentation, we create the connection at startup.
	 */
	public void setup() throws SapR3RfcFCException {
		logProxy.info("Setup SapClientDirectConnectionImpl ");
		initJcoClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#teardown()
	 */
	// No teardown is required
	// public void teardown() throws SapR3RfcFCException {
	// try {
	// getJcoClient().disconnect();
	// } catch (JCO.Exception x) {
	// Object[] msgArgs = new Object[] { x.getMessage() };
	// String msg = LogMessageHelper.getMsgResource().getMessage(
	// LogMessageHelper.SAPR3_RFCFC_0015, msgArgs);
	// throw new SapR3RfcFCException(SapR3RfcFCErrorCodes.DISCONNECTION,
	// msg, x);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#connect()
	 */
	// Connection takes place automatically with the destination manager

	// public JCO.Client connect() throws SapR3RfcFCException {
	// try {
	// if (getJcoClient().getState() == JCO.STATE_DISCONNECTED) {
	// getJcoClient().connect();
	// }
	// } catch (JCO.Exception jcoe) {
	// throw new SapR3RfcFCException(
	// SapR3RfcFCErrorCodes.CONNECTION_ESTABLISHMENT, jcoe
	// .getMessage());
	// }
	// return getJcoClient();
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.fc.sapr3rfcv3.SapClientConnection#disconnect(com.sap.mw.jco
	 * .JCO.Client) Since we want to keep connected for as long as possible, we
	 * don't do anything here.
	 */
	// Connection handling is handled by SAP JCO version 3. We do not need to
	// create or clean anything
	// public void disconnect(Client client) throws SapR3RfcFCException {
	// // We don't want to disconnect the session here, keep it
	// // open for as long as possible to avoid connection setup cost.
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.SapClientConnection#getRfcRepository()
	 */
	public JCoRepository getRfcRepository() throws JCoException {
		if (rfcRepository == null) {
			initRfcRepository();
		}
		return rfcRepository;
	}

	private void initJcoClient() {
//		System.out.println("Calling initJcoClient for " + destinationName);
		createDestinationDataFile(getJcoProperties());
//		System.out.println("After calling initJcoClient for " + destinationName);
	}

	/**
	 * Retrieve our local JCO client.
	 * 
	 * @return JCO.Client
	 */
	// private Client getJcoClient() {
	// if (jcoClient == null) {
	// initJcoClient();
	// }
	// return jcoClient;
	// }

	/**
	 * Retun the local copy of the JCO properties we are to use.
	 * 
	 * @return Properties
	 */
	public Properties getJcoProperties() {
		return jcoProperties;
	}

	private void initRfcRepository() throws JCoException {
		JCoDestination dest;
		JCoRepository repository;
		//		System.out.println("initRfcRepository ");
		//			System.out.println("Destination Name " + this.destinationName);
		dest = JCoDestinationManager.getDestination(this.destinationName);
		//			System.out.println(dest.toString());
		repository = dest.getRepository();
		setRfcRepository(repository);
	}

	/**
	 * Set the RFC repository.
	 * 
	 * @param repository
	 *            JCO.IRepository Set object used to access SAPs RFM Meta data.
	 */
	private void setRfcRepository(JCoRepository repository) {
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
	
	public void terminate()
	{
		File file = new java.io.File(destinationName + ".jcoDestination");
		file.delete();
		
		unregister();
	}

	@Override
	public void unregister() {
		if (sapDestinationDataProvider != null) {
			sapDestinationDataProvider.unregister(destinationName);
		}
		
	}

}
