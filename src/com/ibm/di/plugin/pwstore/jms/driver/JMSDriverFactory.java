/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.jms.driver;

import java.util.Hashtable;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.systemqueue.driver.JMSDriver;
import com.ibm.di.systemqueue.driver.JMSDriverLog;

/**
 * The JMS Driver Factory is an internal class that is used by components like
 * the JMS Connector and the System Queue to create and return an appropriate
 * JMS Driver object providing access to the desired JMS Provider.
 */
public class JMSDriverFactory {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;
	private PWSyncLogger logger = null;

	/**
	 * Constructor for the JMSDriverFactory object
	 * 
	 * @param log
	 *            this is the {@link PWSyncLog} object used for logging.
	 */
	public JMSDriverFactory(PWSyncLog log) {
		this.logger = new PWSyncLogger(log);
	}

	/**
	 * Instantiates a new JMS driver from the class specified in aClassName and
	 * initializes it with the environment parameters provided in aEnv.
	 * 
	 * @param aClassName
	 *            The fully qualified name of the class that will be used as JMS
	 *            Driver
	 * @param aEnv
	 *            Hashtable that holds Driver specific properties. Note: Note:
	 *            If there is a key with the value of
	 *            {@link JMSDriver#ENVIRONMENT_LOG} and the value of that key is
	 *            not an instance of {@link JMSDriverLog} then that value will
	 *            be replaced by the provided when constructing log.
	 * @return JMSDriver specific JMS Driver
	 * @throws Exception
	 *             if the specified class could not be found.
	 */
	public JMSDriver getDriver(String aClassName, Hashtable aEnv)
			throws Exception {

		if (!(aEnv.get(JMSDriver.ENVIRONMENT_LOG) instanceof JMSDriverLog)) {
			aEnv.put(JMSDriver.ENVIRONMENT_LOG, logger);
		}

		JMSDriver jmsDriver = (JMSDriver) Class.forName(aClassName)
				.newInstance();
		jmsDriver.initialize(aEnv);
		return jmsDriver;
	}
}
