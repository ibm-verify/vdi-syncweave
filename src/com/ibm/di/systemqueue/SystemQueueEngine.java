/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue;

import java.util.Enumeration;
import java.util.Hashtable;

import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;

/**
 * The System Queue Engine provides initialization of the default System
 * Queue object. The SystemQueueEngine is Singleton and provide single entry
 * point to create and access connections to specific JMS Drivers. It reads
 * specified parameters in global.properties/solution.properties and creates
 * desired System Queue objects. It provides public static method for creating
 * additional System Queue with given parameters.
 */
public class SystemQueueEngine {

	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * TMS Filename for info, error and debug messages
	 */
	private static final String TMS_FILE = "miserver";

	/*
	 * ResourceHash used for access of the TMS messages
	 */
	public static final ResourceHash sResHash = new ResourceHash(TMS_FILE);

	/*
	 * Parameter name in the global.properties/solution.properties used to turn
	 * on/off System Queue
	 */
	private static final String PROP_SYSTEM_QUEUE_ON = "systemqueue.on";

	/*
	 * Parameter name in the global.properties/solution.properties that
	 * specifies the username
	 */
	private static final String PROP_SYSTEM_QUEUE_USERNAME = "systemqueue.auth.username";

	/*
	 * Parameter name in the global.properties/solution.properties that
	 * specifies the password
	 */
	private static final String PROP_SYSTEM_QUEUE_PASSWORD = "systemqueue.auth.password";

	/*
	 * Parameter name in the global.properties/solution.properties that
	 * specifies the fully qualified name of the class that will be used as JMS
	 * Driver
	 */
	private static final String PROP_JMS_DRIVER_NAME = "systemqueue.jmsdriver.name";

	/*
	 * Parameter name prefix in the global.properties/solution.properties that
	 * specifies the prefix of all JMS Driver properties
	 */
	private static final String PROP_JMSDRIVER_PARAM_PREFIX = "systemqueue.jmsdriver.param.";

	/*
	 * Default System Queue object
	 */
	private SystemQueue mSystemQueue = null;

	/*
	 * Logger used in the System Queue Engine
	 */
	private Log mLogger;

	/*
	 * Singleton object of SystemQueueEngine
	 */
	private static SystemQueueEngine mEngine = null;

	/**
	 * Constructor for the SystemQueueEngine object
	 */
	private SystemQueueEngine() {
	}

	/**
	 * Creator/getter of SystemQueueEngine singleton, single entry point when
	 * creating or getting SystemQueueEngine object
	 * 
	 * @return the instance of SystemQueueEngine
	 * @throws Exception
	 *             If SystemQueueEngine is turned off
	 */
	public static synchronized SystemQueueEngine getInstance() throws Exception {
		if (mEngine == null) {
			if (Boolean.getBoolean(SystemQueueEngine.PROP_SYSTEM_QUEUE_ON)) {
				mEngine = new SystemQueueEngine();
				if (RS.gRS != null)
					mEngine.mLogger = RS.gRS.getLog();
				else
					mEngine.mLogger = new Log("miserver");

				mEngine.initSystemQueue();
			} else {
				throw new Exception(sResHash
						.getString("SYSTEM.QUEUE.ENGINE.OFF"));
			}
		}
		return mEngine;
	}

	/**
	 * Initialize default SystemQueue
	 */
	private void initSystemQueue() {
		String jmsDriverName = System.getProperty(PROP_JMS_DRIVER_NAME);
		if (jmsDriverName == null || jmsDriverName.trim().length() == 0) {
			logWarning(sResHash
					.getString("DEFAULT.QUEUE.REQUIRED.PROP.NOT.SET"));
		} else {
			Hashtable<String, String> driverParams = new Hashtable<String, String>();
			Enumeration<?> propNames = System.getProperties().propertyNames();
			while (propNames.hasMoreElements()) {
				String propertyName = (String) propNames.nextElement();
				if (propertyName.startsWith(PROP_JMSDRIVER_PARAM_PREFIX)) {
					driverParams.put(propertyName
							.substring(PROP_JMSDRIVER_PARAM_PREFIX.length()),
							System.getProperty(propertyName));
				}
			}
			String username = System.getProperty(PROP_SYSTEM_QUEUE_USERNAME);
			if (username == null || username.trim().length() == 0) {
				username = null;
			}

			String password = System.getProperty(PROP_SYSTEM_QUEUE_PASSWORD);
			if (password == null || password.trim().length() == 0) {
				password = null;
			}

			try {
				mSystemQueue = new SystemQueue(jmsDriverName, username,
						password, driverParams);
			} catch (Exception e) {
				logError(sResHash.getString("DEFAULT.QUEUE.CANNOT.INIT", e
						.getMessage()));
			}
		}
	}

	/**
	 * Public getter of default SystemQueue
	 * 
	 * @return SystemQueue object that holds default System Queue
	 * @throws Exception
	 *             If SystemQueueEngine is turned off or default System Queue
	 *             cannot be initialized
	 */
	public static SystemQueue getSystemQueue() throws Exception {
		if (getInstance().mSystemQueue == null) {
			String errorMessage = sResHash.getString("DEFAULT.QUEUE.NOT.INIT");
			mEngine.logError(errorMessage);
			throw new Exception(errorMessage);
		}
		return mEngine.mSystemQueue;
	}

	/**
	 * Logs an error message
	 * 
	 * @param aErrorMessage
	 *            error message text
	 */
	private void logError(String aErrorMessage) {
		if (mLogger != null) {
			mLogger.logerror(aErrorMessage);
		}
	}

	/**
	 * Logs an warning message
	 * 
	 * @param aWarningMessage
	 *            warning message text
	 */
	private void logWarning(String aWarningMessage) {
		if (mLogger != null) {
			mLogger.logwarn(aWarningMessage);
		}
	}
}
