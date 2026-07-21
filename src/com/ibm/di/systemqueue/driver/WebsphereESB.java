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
import com.ibm.websphere.sib.api.jms.*;

/**
 * The JMS Client implementation for Enterprise Servcie Bus. It initialize
 * the JMS client and provides specific way for obtaining JMS ConnectionFactory.
 * JMS Client is limited for only one ConnectionFactory for Queue/Topic at a JVM.
 */

public class WebsphereESB implements JMSDriver {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * JMS Driver property name used for broker
	 */
	public static final String PROP_WESB_BROKER = "jms.broker";

	/**
	 * JMS Driver property name used for channel/Bus name of WebsphereESB
	 */
	public static final String PROP_WESB_BUSNAME = "jms.serverChannel";

	/**
	 * Driver properties for Url
	 */
	protected String wesbURL = null;

	/**
	 * Driver properties for Channel
	 */
	protected String wesbChannel = null;
	
	/**
	 * Driver properties for Host
	 */
	protected String wesbHost = null;
	
	/**
	 * Driver properties for Port
	 */
	protected int wesbPort = 7276;
	
	/**
	 * Driver properties for SIB End Point
	 */
	protected String wesbSIB = null;
	
	/**
	 * Driver properties for log
	 */
	protected JMSDriverLog log = null;

	/**
	 * ResourceHash used for access of the TMS messages
	 */
	protected ResourceHash resHash = ResourceHash.getHash(JMSDriver.JMS_DRIVER_TMS_FILE);

	/**
	 * {@inheritDoc}
	 */
	public javax.jms.QueueConnectionFactory getQueueFactory() throws Exception {

		JmsConnectionFactory qf = JmsFactoryFactory.getInstance().createQueueConnectionFactory();
		
		if (wesbChannel != null)
			qf.setBusName(wesbChannel);

		if (wesbSIB != null)
			qf.setProviderEndpoints(wesbURL);

		return (QueueConnectionFactory) qf;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public javax.jms.TopicConnectionFactory getTopicFactory() throws Exception {

		JmsConnectionFactory tf = JmsFactoryFactory.getInstance().createTopicConnectionFactory();
		
		if (wesbChannel != null)
			tf.setBusName(wesbChannel);

		if (wesbSIB != null)
			tf.setProviderEndpoints(wesbURL);

		return (TopicConnectionFactory) tf;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void initialize(Hashtable env) throws Exception {
		
		Object obj = env.get(ENVIRONMENT_LOG);
		if (obj instanceof JMSDriverLog)
			log = (JMSDriverLog) obj;
		else
			log = new NullLogger();

		wesbURL = (String) env.get(PROP_WESB_BROKER);
		if (wesbURL == null || wesbURL.trim().length() == 0) {
			log.logErrorAndThrowException(resHash.getString(
					"JMSDRIVER.WebsphereESB.PARAMETER.MUST.BE.PROVIDED",
					PROP_WESB_BROKER));
		}else {
			try {
				wesbHost = wesbURL.substring(0, wesbURL.indexOf(":"));

				String portAsString = wesbURL.substring(wesbURL.indexOf(":") + 1, wesbURL.lastIndexOf(":"));
				wesbPort = Integer.parseInt(portAsString);
				
				wesbSIB = wesbURL.substring(wesbURL.lastIndexOf(":")+1);
				
			} catch (Exception err) {
				err.printStackTrace();
				throw new Exception(resHash.getString(
						"JMSDRIVER.WebsphereESB.BAD.BROKER.PARAMETER", err));
			}
		}

		wesbChannel = (String) env.get(PROP_WESB_BUSNAME);
		if (wesbChannel == null || wesbChannel.trim().length() == 0) {
			log.logErrorAndThrowException(resHash.getString(
					"JMSDRIVER.WebsphereESB.PARAMETER.MUST.BE.PROVIDED",
					PROP_WESB_BUSNAME));
		}		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
			
	}
}
