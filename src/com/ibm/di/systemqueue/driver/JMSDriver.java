/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import java.util.Hashtable;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

/**
 * The JMS Driver interface. It exposes the public method that are specific for
 * different JMS Drivers. getQueueFactory() and getTopicFactory() returns
 * specific QueueConnectionFactory/TopicConnectionFactory of the JMS Driver.
 */
public interface JMSDriver {

	/**
	 * This is the key on which is mapped an object of type {@link JMSDriverLog}
	 * during the call to the {@link #initialize(Hashtable)} method.
	 */
	public static final String ENVIRONMENT_LOG = "jms.logger";

	/**
	 * This is the name of the file where the JMS Drivers' translated messages
	 * are placed.
	 */
	public static final String JMS_DRIVER_TMS_FILE = "jmsdriver";

	/**
	 * The initialize(Hastable env) method is passed a java.util.Hashtable
	 * object which stores provider-specific parameters, which can be used for
	 * connecting to a specific instance of the JMS server. Normally this method
	 * would use the supplied parameters to connect to the JMS server and obtain
	 * a javax.jms.TopicConnectionFactory object and/or a
	 * javax.jms.QueueConnectionFactory object. Then the method would store the
	 * object(s) in member variables so that it/they can be later retrieved via
	 * the getQueueFactory() and/or the getTopicFactory() method.
	 * 
	 * @param aEnv
	 *            Hashtable that holds Driver properties
	 * @throws Exception
	 *             if JMS Driver cannot be initialized
	 */
	public void initialize(Hashtable aEnv) throws Exception;

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.QueueConnectionFactory object
	 * 
	 * @return QueueConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if QueueConnectionFactory cannot be created
	 */
	public QueueConnectionFactory getQueueFactory() throws Exception;

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.TopicConnectionFactory object
	 * 
	 * @return TopicConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if TopicConnectionFactory cannot be created
	 */
	public TopicConnectionFactory getTopicFactory() throws Exception;

	/**
	 * This is a call-back method used to notify the JMS Driver so it could
	 * clean any used resources. This method should be used in very rare cases.
	 * 
	 * @throws Exception
	 *             if error while terminating occurs.
	 * @since 7.0
	 */
	public void terminate() throws Exception;
}
