/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import java.util.Hashtable;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import com.ibm.di.server.ResourceHash;
import com.ibm.msg.client.mqtt.MQTTConstants;
import com.ibm.msg.client.mqtt.MqttQueueConnectionFactory;
import com.ibm.msg.client.mqtt.MqttTopicConnectionFactory;

/**
 * 
 * A MicroBroker JMS Driver responsible for connection to a MicroBroker server
 * and creating both the QueueConnectionFactore and the TopicConnectionFactory.
 * 
 * FOR INTERNAL USE ONLY! THIS CLASS IS NOT SUPPORTED FOR THE 7.0 or 7.1 RELEASES!
 */
public class IBMMB implements JMSDriver {

	@SuppressWarnings("unused")
	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected static final String MB_JMS_BROKER = "jms.broker";

	protected static final int MB_DEFAULT_PORT = 1883;

	protected String url = null;

	protected String host = null;

	protected int port = MB_DEFAULT_PORT;

	protected JMSDriverLog log = null;

	protected ResourceHash resHash = ResourceHash.getHash(JMS_DRIVER_TMS_FILE);

	/**
	 * @return The {@link QueueConnectionFactory} after connecting to the
	 *         MicroBroker server.
	 * @exception Exception
	 *                in case the provided during the initialization URL is
	 *                incorrect.
	 */
	public QueueConnectionFactory getQueueFactory() throws Exception {

		MqttQueueConnectionFactory qf = new MqttQueueConnectionFactory();
		qf.setStringProperty(MQTTConstants.MQTT_CONNECTION_URL,
				MQTTConstants.MQTT_TCP_SCHEMA + host + ':' + port);

		return qf;
	}

	/**
	 * @return The {@link TopicConnectionFactory} after connecting to the
	 *         MicroBroker server.
	 * @exception Exception
	 *                in case the provided during the initialization URL is
	 *                incorrect.
	 */
	public TopicConnectionFactory getTopicFactory() throws Exception {

		MqttTopicConnectionFactory tf = new MqttTopicConnectionFactory();
		tf.setStringProperty(MQTTConstants.MQTT_CONNECTION_URL,
				MQTTConstants.MQTT_TCP_SCHEMA + host + ':' + port);

		return tf;
	}

	/**
	 * @param env
	 *            a hashtable containing configuration parameters used during
	 *            the connecting process. This Driver expects only a parameter
	 *            with a key {@link #MB_JMS_BROKER} and a value that follows
	 *            this syntax: &lt;host&gt;:&lt;port&gt; if the port is omitted
	 *            the default port 1883 is assumed.
	 * 
	 * @exception Exception
	 *                in case the the required parameter is not provided or if
	 *                the provided URL is not following the specific format.
	 */
	public void initialize(Hashtable env) throws Exception {

		Object obj = env.get(ENVIRONMENT_LOG);
		if (obj instanceof JMSDriverLog)
			log = (JMSDriverLog) obj;
		else
			log = new NullLogger();

		url = ((String) env.get(MB_JMS_BROKER)).trim();
		if (url == null || url.trim().length() == 0) {
			log.logErrorAndThrowException(resHash
					.getString(
							// KK: change this message
							"JMSDRIVER.IBMMB.PARAMETER.MUST.BE.PROVIDED",
							MB_JMS_BROKER));
		}
		int lastColPos = url.lastIndexOf(':');
		if (lastColPos < 0) {
			host = url;
		} else {
			try {
				host = url.substring(0, lastColPos);

				port = Integer.parseInt(url.substring(lastColPos + 1));

			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception(resHash.getString(
				// KK: change this message
						"JMSDRIVER.IBMMB.BAD.BROKER.PARAMETER", err));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
	}
}
