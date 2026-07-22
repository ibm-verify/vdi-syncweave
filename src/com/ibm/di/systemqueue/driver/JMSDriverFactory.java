/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import java.util.Hashtable;

/**
 * The JMS Driver Factory is an internal class that is used by components like
 * the JMS Connector and the System Queue to create and return an appropriate
 * JMS Driver object providing access to the desired JMS Provider.
 */
public class JMSDriverFactory {

	/**
	 * Constructor for the JMSDriverFactory object
	 */
	protected JMSDriverFactory() {
	}

	/**
	 * Instantiates a new JMS driver from the class specified in aClassName and
	 * initializes it with the environment parameters provided in aEnv.
	 * 
	 * @param aClassName
	 *            The fully qualified name of the class that will be used as JMS
	 *            Driver
	 * @param aEnv
	 *            Hashtable that holds Driver specific properties. Note: If
	 *            there is a key with the value of
	 *            {@link JMSDriver#ENVIRONMENT_LOG} and the value of that key is
	 *            not an instance of {@link JMSDriverLog} then that value will
	 *            be replaced.
	 * @return JMSDriver specific JMS Driver
	 * @throws Exception
	 *             if an error occur.
	 */
	public static JMSDriver getDriver(String aClassName, Hashtable aEnv)
			throws Exception {

		Object obj = aEnv.get(JMSDriver.ENVIRONMENT_LOG);
		if (!(obj instanceof JMSDriverLog)) {
			aEnv.put(JMSDriver.ENVIRONMENT_LOG, new APIEngineLogger());
		}

		JMSDriver jmsDriver = (JMSDriver) Class.forName(aClassName)
				.newInstance();
		jmsDriver.initialize(aEnv);
		return jmsDriver;
	}
}
