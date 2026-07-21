/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import java.util.Hashtable;

import com.ibm.di.server.ResourceHash;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQTopicConnectionFactory;

/**
 * The Websphere MQ Series JMS Driver implementation. It initialize the JMS
 * Driver and provides specific way for obtaining JMS QueueConnectionFactory.
 */
public class IBMMQ implements JMSDriver {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * JMS Driver property name used for broker
	 */
	public static final String PROP_MQ_BROKER = "jms.broker";

	/**
	 * JMS Driver property name used for channel
	 */
	public static final String PROP_MQ_CHANNEL = "jms.serverChannel";

	/**
	 * JMS Driver property name used for queue manager
	 */
	public static final String PROP_MQ_QMANAGER = "jms.qManager";

	/**
	 * JMS Driver property name used for sslCipher
	 */
	public static final String PROP_MQ_SSL_CIPHER = "jms.sslCipher";

	/**
	 * JMS Driver property name used for ssl flag
	 */
	public static final String PROP_MQ_SSL_USE_FLAG = "jms.sslUseFlag";

	/**
	 * Driver properties for SSL
	 */
	protected boolean mSSL = false;

	/**
	 * Driver properties for Url
	 */
	protected String mURL = null;

	/**
	 * Driver properties for Channel
	 */
	protected String mChannel = null;

	/**
	 * Driver properties for QueueManager
	 */
	protected String mQmgr = null;

	/**
	 * Driver properties for Host
	 */
	protected String mHost = null;

	/**
	 * Driver properties for CipherSuit
	 */
	protected String mCipherSuite = null;

	/**
	 * Driver properties for Port
	 */
	protected int mPort = 1414;

	protected JMSDriverLog log = null;

	/**
	 * ResourceHash used for access of the TMS messages
	 */
	protected ResourceHash resHash = ResourceHash
			.getHash(JMSDriver.JMS_DRIVER_TMS_FILE);

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
	 * @param env
	 *            Hashtable that holds Driver properties
	 * 
	 * @throws Exception
	 *             if JMS Driver cannot be initialized
	 */
	public void initialize(Hashtable env) throws Exception {

		Object obj = env.get(ENVIRONMENT_LOG);
		if (obj instanceof JMSDriverLog)
			log = (JMSDriverLog) obj;
		else
			log = new NullLogger();

		mURL = (String) env.get(PROP_MQ_BROKER);
		if (mURL == null || mURL.trim().length() == 0) {
			log.logErrorAndThrowException(resHash.getString(
					"JMSDRIVER.IBMMQ.PARAMETER.MUST.BE.PROVIDED",
					PROP_MQ_BROKER));
		} else if (mURL.indexOf(":") < 0) {
			mHost = mURL;
		} else {
			try {
				mHost = mURL.substring(0, mURL.lastIndexOf(":"));

				String portAsString = mURL.substring(mURL.lastIndexOf(":") + 1);
				mPort = Integer.parseInt(portAsString);
			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception(resHash.getString(
						"JMSDRIVER.IBMMQ.BAD.BROKER.PARAMETER", err));
			}
		}

		mChannel = (String) env.get(PROP_MQ_CHANNEL);
		if (mChannel == null || mChannel.trim().length() == 0) {
			log.logErrorAndThrowException(resHash.getString(
					"JMSDRIVER.IBMMQ.PARAMETER.MUST.BE.PROVIDED",
					PROP_MQ_CHANNEL));
		}

		mSSL = false;
		String sslStr = (String) env.get(PROP_MQ_SSL_USE_FLAG);
		if (sslStr != null) {
			mSSL = Boolean.valueOf(sslStr).booleanValue();
		}
		if (mSSL) {
			mQmgr = (String) env.get(PROP_MQ_QMANAGER);
			if (mQmgr == null || mQmgr.trim().length() == 0) {
				log.logErrorAndThrowException(resHash.getString(
						"JMSDRIVER.IBMMQ.PARAMETER.MUST.BE.PROVIDED",
						PROP_MQ_QMANAGER));
			}
			mCipherSuite = (String) env.get(PROP_MQ_SSL_CIPHER);
			if (mCipherSuite == null || mCipherSuite.trim().length() == 0) {
				log.logErrorAndThrowException(resHash.getString(
						"JMSDRIVER.IBMMQ.PARAMETER.MUST.BE.PROVIDED",
						PROP_MQ_SSL_CIPHER));
			}
		}
	}

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.QueueConnectionFactory object
	 * 
	 * @return QueueConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if QueueConnectionFactory cannot be created
	 */
	public javax.jms.QueueConnectionFactory getQueueFactory() throws Exception {
		MQQueueConnectionFactory qf = new MQQueueConnectionFactory();
		qf.setTransportType(com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
		qf.setHostName(mHost);
		qf.setPort(mPort);
		if (mChannel != null)
			qf.setChannel(mChannel);

		if (mSSL) {
			if (mQmgr != null)
				qf.setQueueManager(mQmgr);
			if (mCipherSuite != null)
				qf.setSSLCipherSuite(mCipherSuite);

			if (Boolean.getBoolean("com.ibm.di.server.fipsmode.on")) {
				qf.setSSLFipsRequired(true);
			}
		}

		return qf;
	}

	/**
	 * This method retrieves the provider-specific
	 * javax.jms.TopicConnectionFactory object
	 * 
	 * @return TopicConnectionFactory object of the JMS Driver
	 * @throws Exception
	 *             if TopicConnectionFactory cannot be created
	 */
	public javax.jms.TopicConnectionFactory getTopicFactory() throws Exception {

		MQTopicConnectionFactory qf = new MQTopicConnectionFactory();
		qf.setTransportType(com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
		qf.setHostName(mHost);
		qf.setPort(mPort);
		if (mChannel != null)
			qf.setChannel(mChannel);

		if (mSSL) {
			if (mQmgr != null)
				qf.setQueueManager(mQmgr);
			if (mCipherSuite != null)
				qf.setSSLCipherSuite(mCipherSuite);

			if (Boolean.getBoolean("com.ibm.di.server.fipsmode.on")) {
				qf.setSSLFipsRequired(true);
			}
		}

		return qf;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
	}
}
