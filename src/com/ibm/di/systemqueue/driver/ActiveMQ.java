/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import java.util.Hashtable;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * The ActiveMQ JMS Driver implementation. It initialize the JMS Driver and
 * provides specific way for obtaining JMS
 * QueueConnectionFactory/TopicConnectionFactory.
 */
public class ActiveMQ implements JMSDriver {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * JMS Driver property name used to define broker.
	 */
	public static final String PROP_BROKER_URL = "jms.broker";

	/**
	 * Hold broker URL.
	 */
	private String brokerURL = null;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void initialize(Hashtable env) throws Exception {
		final Object broker = env.get(PROP_BROKER_URL);
		if (broker instanceof String) {
			brokerURL = (String) broker;
		}
		if (brokerURL != null) {
			if (!brokerURL.contains("://")) {
				String useSSL = (String) env.get("jms.sslUseFlag");
				if (useSSL != null && useSSL.equalsIgnoreCase("true")) {
					brokerURL = "ssl://" + brokerURL;
				} else {
					brokerURL = "tcp://" + brokerURL;
				}
			}
		} else {
			brokerURL = "vm://localhost";
		}
	}

	/**
	 * Create new Queue/Topic Connection Factory.
	 * 
	 * @return Queue/Topic Connection Factory.
	 */
	private ActiveMQConnectionFactory getConnectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		connectionFactory.setTrustAllPackages(true);
		connectionFactory.setBrokerURL(brokerURL);
		return connectionFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public QueueConnectionFactory getQueueFactory() throws Exception {
		return getConnectionFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	public TopicConnectionFactory getTopicFactory() throws Exception {
		return getConnectionFactory();
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
	}

}
