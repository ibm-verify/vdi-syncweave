/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.jms;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.pwstore.BasePasswordChange;
import com.ibm.di.plugin.pwstore.IPasswordSynchronizer;
import com.ibm.di.plugin.pwstore.PasswordChange;
import com.ibm.di.plugin.pwstore.PasswordStore;
import com.ibm.di.plugin.pwstore.jms.driver.JMSDriverFactory;
import com.ibm.di.plugin.security.SecurityHelper;
import com.ibm.di.plugin.security.pki.IDIPasswordCrypto;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.systemqueue.driver.JMSDriver;
import com.ibm.icu.text.SimpleDateFormat;

public class JMSPasswordStore implements PasswordStore, IPasswordSynchronizer {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	public static final String DEFAULT_PASSWORD_QUEUE = "passwords";

	private static final String UPDATE_TYPE_REPLACE = "replace";

	private static final String UPDATE_TYPE_ADD = "add";

	private static final String UPDATE_TYPE_DELETE = "delete";

	// property names
	public static final String PROP_JMS_DRIVER = "jmsDriverClass";

	public static final String PROP_ENCRYPT = "encrypt";

	public static final String PROP_ENCRYPT_KEY_STORE_FILE_PASSWORD = "encryptKeyStoreFilePassword";

	public static final String PROP_ENCRYPT_KEY_STORE_FILE_PATH = "encryptKeyStoreFilePath";

	public static final String PROP_ENCRYPT_KEY_STORE_CERTIFICATE = "encryptKeyStoreCertificate";

	public static final String PROP_PKCS7 = "pkcs7";

	public static final String PROP_PKCS7_JKS_FILE_PATH = "pkcs7KeyStoreFilePath";

	public static final String PROP_PKCS7_JKS_FILE_PASSWORD = "pkcs7KeyStoreFilePassword";

	public static final String PROP_PKCS7_MQE_STORE_CERTIFICATE = "pkcs7MqeStoreCertificateAlias";

	public static final String PROP_PKCS7_MQE_CONNECTOR_CERTIFICATE = "pkcs7MqeConnectorCertificateAlias";

	public static final String PROP_JMS_CLIENT_ID = "jms.clientId";

	public static final String PROP_JMS_USER = "jms.username";

	public static final String PROP_JMS_PASSWORD = "jms.password";

	// private values

	private boolean mEncrypt = true;

	private String mEncKeyStorePath = null;

	private String mEncKeyStorePassword = null;

	private String mEncKeyStoreCertificate = null;

	private boolean mPkcs7 = false;

	// JMS members
	private QueueConnectionFactory mFactory = null;

	private QueueConnection mConnection = null;

	private QueueSession mSession = null;

	private Queue mPasswordQueue = null;

	private QueueSender mPasswordQueueSender = null;

	private String user;

	private String pass;

	private JMSDriver driver = null;

	static String PREFIX = "JMSStore";

	/**
	 * This is null if the {@link JMSPasswordStore} is not initialized properly.
	 */
	private PWSyncLog log = null;

	private static final ResourceHash resHash = ResourceHash.getHash("jmspwstore");

	public JMSPasswordStore() {
	}

	// IPasswordSynchronizer interface implementation

	@Deprecated
	public boolean readyToSync(String id) {
		return readyToSync(id, null);
	}

	@Deprecated
	public boolean readyToSync(String id, Vector passwords) {
		return isAvailable(getPasswordChange(PasswordChange.NO_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean syncPassword(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.MODIFY_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean addPasswordValues(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.ADD_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean deletePasswordValues(String id, Vector passwords) {
		return store(getPasswordChange(PasswordChange.DELETE_CHANGE, id, passwords, null));
	}

	@Deprecated
	public boolean setExtendedData(String id, String extendedData) {
		// JMS Password Store ignores extended data as designed.
		return true;
	}

	private PasswordChange getPasswordChange(int type, String id, Vector passwords, String extendedData) {
		return new BasePasswordChange(type, id, passwords, extendedData, null);
	}

	// PasswordStore interface implementation

	/**
	 * {@inheritDoc}
	 */
	public synchronized void initialize(Object aObj) throws Exception {

		if (aObj instanceof PWSyncLog)
			log = (PWSyncLog) aObj;

		mEncrypt = getBooleanProperty(PROP_ENCRYPT);
		mPkcs7 = getBooleanProperty(PROP_PKCS7);
		if ((mEncrypt) && (mPkcs7)) {
			throw new Exception(resHash.getString("JMSPWSTORE.PKCS7.PKI.BOTH.TRUE", new Object[] { PROP_PKCS7, PROP_ENCRYPT }));
		}
		if (mEncrypt) {
			mEncKeyStorePath = getRequiredProperty(PROP_ENCRYPT_KEY_STORE_FILE_PATH);
			mEncKeyStorePassword = SecurityHelper.getClearText(getRequiredProperty(PROP_ENCRYPT_KEY_STORE_FILE_PASSWORD));
			mEncKeyStoreCertificate = getRequiredProperty(PROP_ENCRYPT_KEY_STORE_CERTIFICATE);
		}

		user = getProperty(PROP_JMS_USER);
		pass = getProperty(PROP_JMS_PASSWORD);

		user = user == null ? "" : user;
		pass = pass == null ? "" : SecurityHelper.getClearText(pass);

		initJMS(System.getProperties());

		// invoke encryption of a string to initialize encryption provider
		// this moves the time delay necessary to intialize the encryption
		// provider
		// from the time of the first password update in the initialization of
		// the Password Store
		if (mEncrypt) {
			try {
				IDIPasswordCrypto.encrypt("init", mEncKeyStorePath, mEncKeyStorePassword, mEncKeyStoreCertificate);
			} catch (Exception e) {
				log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));

				e.printStackTrace(log.getPrintWriter());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean isAvailable(PasswordChange change) {
		boolean available = false;
		try {
			createJMSSession();
			available = true;
		} catch (Exception e) {
			log.debug(PREFIX, resHash.getString("JMSPWSTORE.JMS.PROVIDER.UNREACHABLE", e));
			available = false;
		} finally {
			closeJMSSession();
		}

		return available;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized boolean store(PasswordChange change) {
		boolean success = false;

		switch (change.getType()) {
		case PasswordChange.ADD_CHANGE:
			success = addPassword(change);
			break;
		case PasswordChange.MODIFY_CHANGE:
			success = modifyPassword(change);
			break;
		case PasswordChange.DELETE_CHANGE:
			success = deletePassword(change);
			break;
		}
		
		return success;
	}

	private synchronized boolean modifyPassword(PasswordChange change) {
		boolean success = false;

		try {
			String message = constructMessageText(UPDATE_TYPE_REPLACE, change);
			sendMessage(message);
			success = true;
		} catch (Exception e) {
			log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
			e.printStackTrace(log.getPrintWriter());
		}

		if (success) {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWSYNC.SUCCESS", change.getID()));
		} else {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWSYNC.FAILURE", change.getID()));
		}
		return success;
	}

	private synchronized boolean addPassword(PasswordChange change) {
		boolean success = false;

		try {
			String message = constructMessageText(UPDATE_TYPE_ADD, change);
			sendMessage(message);
			success = true;
		} catch (Exception e) {
			log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
			e.printStackTrace(log.getPrintWriter());
		}

		if (success) {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWADD.SUCCESS", change.getID()));
		} else {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWADD.FAILURE", change.getID()));
		}
		return success;
	}

	private synchronized boolean deletePassword(PasswordChange change) {
		boolean success = false;

		try {
			String message = constructMessageText(UPDATE_TYPE_DELETE, change);
			sendMessage(message);
			success = true;
		} catch (Exception e) {
			log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
			e.printStackTrace(log.getPrintWriter());
		}

		if (success) {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWDEL.SUCCESS", change.getID()));
		} else {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.PWDEL.FAILURE", change.getID()));
		}
		return success;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void terminate() {
		close();
		try {
			driver.terminate();
		} catch (Exception e) {
			log.error(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"), e);
		}
	}

	// end of PasswordStore interface implementation

	private void initJMS(Properties props) throws Exception {
		try {

			if (mFactory == null) {
				String driverClass = getRequiredProperty(PROP_JMS_DRIVER);

				log.debug(PREFIX, resHash.getString("JMSPWSTORE.LOADING.JMSDRIVER", driverClass));
				JMSDriverFactory factory = new JMSDriverFactory(log);
				driver = factory.getDriver(driverClass, props);

				mFactory = driver.getQueueFactory();
				
				try {
					Method m = mFactory.getClass().getMethod("setLocalAddress", new Class[] { String.class} );
					m.invoke(mFactory, new Object[] {"(1414,1700)"});
					log.debug(PREFIX, resHash.getString("JMSPWSTORE.PORT.RANGE.SUCCESS"));
				} catch (Exception err) {
					log.debug(PREFIX, resHash.getString("JMSPWSTORE.PORT.RANGE.FAILURE", err));
				}
			}
		} catch (Exception e) {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.CONFIGURATION.FAILED", e.toString()));
			if (e instanceof JMSException) {
				Exception le = ((JMSException) e).getLinkedException();
				if (le != null) {
					log.info(PREFIX, resHash.getString("JMSPWSTORE.LINKED.EXCEPTION.MSG", le.toString()));
				}
			}
			throw e;
		}
	}

	private void createJMSSession() throws Exception {
		if (user != null && !user.equals("") && pass != null && !pass.equals("")) {
			mConnection = mFactory.createQueueConnection(user, pass);
		} else {
			mConnection = mFactory.createQueueConnection();
		}

                mConnection.setClientID(getRequiredProperty(PROP_JMS_CLIENT_ID));

		log.debug(PREFIX, resHash.getString("JMSPWSTORE.CONNECTING.TO.JMS.PROVIDER"));
		mSession = mConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

                mPasswordQueue = mSession.createQueue(DEFAULT_PASSWORD_QUEUE);

		mPasswordQueueSender = mSession.createSender(mPasswordQueue);

		mConnection.start();
		log.debug(PREFIX, resHash.getString("JMSPWSTORE.CONNECTION.STARTED"));
	}

	private void closeJMSSession() {
		if (mSession != null) {
			try {
				mSession.close();
			} catch (JMSException e) {
				log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
				e.printStackTrace(log.getPrintWriter());
			} finally {
				mSession = null;
			}
		}

		if (mConnection != null) {
			try {
				mConnection.close();
			} catch (JMSException e) {
				log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
				e.printStackTrace(log.getPrintWriter());
			} finally {
				mConnection = null;
			}
		}
	}

	private String constructMessageText(String aUpdateType, PasswordChange change) throws Exception {
		StringBuffer message = new StringBuffer();

		/*
		 * Message structure: [update_type_length: 1 char] + [update_type:
		 * update_type_length chars] + [user_id_length: 1 char] + [user_id:
		 * user_id_length chars] + [number_of_password_values: 1 char] +
		 * [password_value(1)] + ................... +
		 * [password_value(number_of_password_values)]+ [customData_length: 1
		 * char] + [customData_length chars] + [timestamp_length: 1] +
		 * [timestamp_length chars]
		 * 
		 * Password value structure [password_value(i)]:
		 * [password_value(i)_length: 1 char] + [password_value(i):
		 * password_value(i)_length chars]
		 */

		// update type
		String fieldUpdateType = packageField(aUpdateType);
		message.append((char) fieldUpdateType.length());
		message.append(fieldUpdateType);

		// user id
		String fieldUserId = packageField(change.getID());
		message.append((char) fieldUserId.length());
		message.append(fieldUserId);

		// password values
		Vector<String> passwords = change.getPasswords();
		if (passwords != null) {
			message.append((char) passwords.size());
			for (int i = 0; i < passwords.size(); i++) {
				String fieldPasswordValue = packageField((String) passwords.get(i));
				message.append((char) fieldPasswordValue.length());
				message.append(fieldPasswordValue);
			}
		} else {
			message.append((char) 0);
		}

		// custom data field
		String customData = change.getCustomData();
		if (customData != null && customData.length() > 0) {
			customData = packageField(customData);
			message.append((char) customData.length());
			message.append(customData);
		} else {
			// this indicates that no custom data is provided
			message.append((char) 0);
		}

		String timestamp = packageField(formatDate(change.getTimestamp()));
		if (timestamp != null && timestamp.length() > 0) {
			message.append((char) timestamp.length());
			message.append(timestamp);
		}

		return message.toString();
	}

	private void sendMessage(String aMessage) throws Exception {
		try {
			createJMSSession();
			TextMessage outMessage = mSession.createTextMessage(aMessage);
			mPasswordQueueSender.send(outMessage);
		} catch (Exception e) {
			log.info(PREFIX, resHash.getString("JMSPWSTORE.SENDING.PASSWD.ERROR"));
			throw e;
		} finally {
			closeJMSSession();
		}
	}

	private String packageField(String aField) throws Exception {
		if (mEncrypt) {
			return IDIPasswordCrypto.encrypt(aField, mEncKeyStorePath, mEncKeyStorePassword, mEncKeyStoreCertificate);
		} else {
			return aField;
		}
	}

	private static String getProperty(String propName) {
		String propValue = System.getProperty(propName);
		if (propValue != null) {
			propValue = propValue.trim();
		}
		return propValue;
	}

	public static String getRequiredProperty(String propName) throws Exception {
		String propValue = getProperty(propName);
		if (propValue == null || propValue.length() == 0) {
			throw new Exception(resHash.getString("JMSPWSTORE.MISSING.REQUIRED.PROP", propName));
		}

		return propValue;
	}

	public static boolean getBooleanProperty(String propName) {
		return ("true".equalsIgnoreCase(getProperty(propName)) || "1".equals(getProperty(propName)));
	}

	private synchronized void close() {
		log.info(PREFIX, resHash.getString("JMSPWSTORE.EXIT.REQUESTED"));
		try {
			if (mConnection != null) {
				mConnection.stop();
				mConnection.close();
			}
			log.info(PREFIX, resHash.getString("JMSPWSTORE.JMS.CONN.CLOSED"));
		} catch (Exception e) {
			log.debug(PREFIX, resHash.getString("JMSPWSTORE.JAVA.EXCEPTION"));
			e.printStackTrace(log.getPrintWriter());
		} finally {
			mConnection = null;
		}
	}

	/**
	 * JMS Password Store ignores extended data. It has always been like this.
	 */
	public boolean setExtendedData(PasswordChange change) {
		return true;
	}

	/**
	 * This method formats a date into the LDAPv3 Generalized Time Syntax.
	 * 
	 * @param date
	 *            date in milliseconds
	 * @return string representation of a date
	 */
	private String formatDate(long date) {
		return (new SimpleDateFormat("yyyyMMddHHmmss.SZ")).format(new Date(date));
	}
}
