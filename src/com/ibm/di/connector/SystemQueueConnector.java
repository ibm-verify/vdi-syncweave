/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.rmi.Naming;

import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * The System Queue Connector provides a way to use the functionality provided
 * by the System Queue component in TDI AssemblyLines. The Connector uses Server
 * API sessions remote or local, depending on the mode selected by setting a
 * Connector parameter, to connect to the System Queue. In remote mode the
 * Connector hooks into remote TDI systems and uses Server API and System Queue
 * in the Java Virtual Machine of that remote system. In local mode the
 * Connector use local Server API and System Queue in the local Java Virtual
 * Machine.
 */
public class SystemQueueConnector extends Connector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * TMS Filename used in the Connector for info, error and debug messages
	 */
	private static final String PROPERTIES_FILE = "systemqueueconnector";

	/**
	 * ResourceHash used for access of the TMS messages
	 */
	private static final ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Parameter name in the configuration for connectionType
	 */
	private final static String PARAM_CONNECTION_TYPE = "connectionType";

	/**
	 * Value of the "connectionType" Connector parameter when the connectionType
	 * chosen is remote
	 */
	private final static String REMOTE_MODE = "remote";

	/**
	 * Value of the "connectionType" Connector parameter when the connectionType
	 * chosen is local
	 */
	private final static String LOCAL_MODE = "local";

	/**
	 * Parameter name in the configuration for url.
	 */
	private final static String PARAM_URL = "url";

	/**
	 * Parameter name in the configuration for username.
	 */
	private final static String PARAM_USERNAME = "username";

	/**
	 * Parameter name in the configuration for password.
	 */
	private final static String PARAM_PASSWORD = "password";

	/**
	 * Parameter name in the configuration for queueName.
	 */
	private static final String PARAM_QUEUE_NAME = "queueName";

	/**
	 * Parameter name in the configuration for timeOut.
	 */
	private final static String PARAM_TIMEOUT = "timeOut";

	/**
	 * The remote or local session. The connection to the Server API layer
	 */
	private Object mSession;

	/**
	 * isLocal used in Connector
	 */
	private boolean mIsLocal;

	/**
	 * queueName used in Connector
	 */
	private String mQueueName = null;

	/**
	 * timeOut used in Connector
	 */
	private int mTimeOut;

	/**
	 * Constructor for the SystemQueueConnector object
	 */
	public SystemQueueConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Reads connector parameter's values and initialize the Connector.
	 * 
	 * @param aObj
	 *            Null, Socket or ConnectorMode class
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object aObj) throws Exception {
		String modeStr = (String) getParam(PARAM_CONNECTION_TYPE);
		if (modeStr == null || modeStr.trim().length() == 0) {
			String errorMessage = sResHash.getString(
					"REQUIRED.PARAMETER.NOT.SET", PARAM_CONNECTION_TYPE);
			logmsg(errorMessage);
			throw new Exception(errorMessage);
		}
		if (LOCAL_MODE.equalsIgnoreCase(modeStr)) {
			mIsLocal = true;
		} else if (REMOTE_MODE.equalsIgnoreCase(modeStr)) {
			mIsLocal = false;
		} else {
			throw new Exception(sResHash.getString("INVALID.PARAMETER.VALUE",
					modeStr));
		}

		if (mIsLocal) {
			mSession = com.ibm.di.api.APIEngine.getLocalSession();
		} else {
			String url = (String) getParam(PARAM_URL);
			if (url == null || url.trim().length() == 0) {
				throw new Exception(sResHash.getString(
						"REQUIRED.PARAMETER.NOT.SET", PARAM_URL));
			}
			String username = (String) getParam(PARAM_USERNAME);
			if (username == null || username.trim().length() == 0) {
				username = null;
			}

			String password = (String) getParam(PARAM_PASSWORD);
			if (password == null || password.trim().length() == 0) {
				password = null;
			}

			SessionFactory sessionFactory = (SessionFactory) Naming.lookup(url);

			if (username == null) {
				mSession = sessionFactory.createSession();
			} else {
				mSession = sessionFactory.createSession(username, password);
			}
		}
		mQueueName = (String) getParam(PARAM_QUEUE_NAME);
		if (mQueueName == null || mQueueName.trim().length() == 0) {
			String errorMessage = sResHash.getString(
					"REQUIRED.PARAMETER.NOT.SET", PARAM_QUEUE_NAME);
			logmsg(errorMessage);
			throw new Exception(errorMessage);
		} else {
			mQueueName = mQueueName.trim();
		}
		String timeOutStr = getParam(PARAM_TIMEOUT);
		try {
			mTimeOut = (Integer.valueOf(timeOutStr).intValue()) * 1000;
		} catch (NumberFormatException e) {
			mTimeOut = -1;
		}
	}

	/**
	 * Gets the next Entry object from the JMS server.
	 * 
	 * @return The next Entry
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		Entry e;
		if (mIsLocal) {
			e = ((com.ibm.di.api.local.Session) mSession).getSystemQueue()
					.getEntry(mQueueName, mTimeOut);
		} else {
			e = ((com.ibm.di.api.remote.Session) mSession).getSystemQueue()
					.getEntry(mQueueName, mTimeOut);
		}
		return e;
	}

	/**
	 * Send an entry to the JMS server.
	 * 
	 * @param aEntry
	 *            The entry to send
	 * 
	 * @throws Exception
	 *             If sending the Entry fails.
	 */
	public void putEntry(Entry aEntry) throws Exception {
		if (mIsLocal) {
			((com.ibm.di.api.local.Session) mSession).getSystemQueue()
					.putEntry(mQueueName, aEntry);
		} else {
			((com.ibm.di.api.remote.Session) mSession).getSystemQueue()
					.putEntry(mQueueName, aEntry);
		}
	}

	/**
	 * Version information.
	 * @return the version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
