/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.TopicConnectionFactory;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.systemqueue.driver.JMSDriver;
import com.ibm.di.systemqueue.driver.JMSDriverFactory;

/**
 * The System Queue holds single connection to JMS Provider. It capsulate JMS
 * specific methods and expose API for working with JMS. It receive all needed
 * specific parameters to create JMS Driver, Connection Factories, Queues,
 * Topics. It provides methods for sending and receiving messages to/from JMS
 * Provider.
 */
public class SystemQueue {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * ResourceHash used for access of the TMS messages
	 */
	private static ResourceHash sResHash = SystemQueueEngine.sResHash;

	/*
	 * Specific JMS Driver
	 */
	private JMSDriver mJMSDriver;

	/*
	 * QueueConnectionFactory used for creating QueueConnections
	 */
	private QueueConnectionFactory mQueueFactory;

	/*
	 * QueueConnection used for creating Queues and Topics
	 */
	private QueueConnection mConnection;

	/*
	 * QueueConnection used for creating QueueSession
	 */
	private QueueSession mQueueSession;

	/*
	 * Cache for Queues that are previously obtained
	 */
	private Map<String, Queue> mQueueMap;

	/*
	 * Cache for Senders that are previously obtained
	 */
	private Map<String, MessageProducer> mSenderMap;

	/*
	 * Cache for Receivers that are previously obtained
	 */
	private Map<String, QueueReceiver> mReceiverMap;

	/*
	 * Logger used in the System Queue
	 */
	private Log mLogger;

	/*
	 * Parameter name in the global.properties/solution.properties that
	 * specifies the client ID used by the configured JMS Driver
	 */
	private static final String PROP_JMS_DRIVER_CLIENTID = "jms.clientID";

	/**
	 * Constructor for the SystemQueueEngine object
	 * 
	 * @param aJmsDriverName
	 *            the JMS Driver classname
	 * @param aDriverParams
	 *            properties used for JMS Driver initialization
	 * 
	 * @throws Exception
	 *             if JMS Driver cannot be initialized and used
	 */
	protected SystemQueue(String aJmsDriverName, String username,
			String password, Hashtable aDriverParams) throws Exception {

		mQueueMap = new HashMap<String, Queue>();
		mSenderMap = new HashMap<String, MessageProducer>();
		mReceiverMap = new HashMap<String, QueueReceiver>();

		if (RS.gRS != null)
			mLogger = RS.gRS.getLog();
		else
			mLogger = new Log("miserver");

		mJMSDriver = JMSDriverFactory.getDriver(aJmsDriverName, aDriverParams);

		if (mJMSDriver == null) {
			String errorMessage = sResHash.getString("CANNOT.GET.JMSDRIVER",
					aJmsDriverName);
			logError(errorMessage);
			throw new Exception(errorMessage);
		}

		try {
			mQueueFactory = mJMSDriver.getQueueFactory();
		} catch (Exception e) {
			String errorMessage = sResHash.getString("ERROR.GET.QCONNFACTORY",
					e.getMessage());
			logError(errorMessage);
			throw new Exception(errorMessage);
		}

		if (mQueueFactory == null) {
			String errorMessage = sResHash.getString("CANNOT.GET.QCONNFACTORY");
			logError(errorMessage);
			throw new Exception(errorMessage);
		}
		try {
			if (username == null) {
				mConnection = mQueueFactory.createQueueConnection();
			} else {
				mConnection = mQueueFactory.createQueueConnection(username,
						password);
			}

			if (aDriverParams != null) {
				String clientID = (String) aDriverParams
						.get(PROP_JMS_DRIVER_CLIENTID);

				if (clientID != null && clientID.length() > 0)
					mConnection.setClientID(clientID.trim());
			}

			mQueueSession = mConnection.createQueueSession(false,
					QueueSession.AUTO_ACKNOWLEDGE);

			mConnection.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Public getter of QueueConnectionFactory
	 * 
	 * @return QueueConnectionFactory object that holds Queue connection to JMS
	 *         Provider
	 * @throws Exception
	 *             i f QueueConnectionFactory cannot be obtained
	 */
	public QueueConnectionFactory getQueueConnectionFactory() throws Exception {
		return mJMSDriver.getQueueFactory();
	}

	/**
	 * Public getter of TopicConnectionFactory
	 * 
	 * @return TopicConnectionFactory object that holds Topic connection to JMS
	 *         Provider
	 * @throws Exception
	 *             if TopicConnectionFactory cannot be obtained
	 */
	public TopicConnectionFactory getTopicConnectionFactory() throws Exception {
		return mJMSDriver.getTopicFactory();
	}

	/**
	 * Gets a Message from the System Queue
	 * 
	 * @param aQueueName
	 *            queue name from what the message is get
	 * @param aTimeOut
	 *            Specifies the maximum time of waiting for a new message
	 * 
	 * @return the javax.jms.Message object.
	 * @throws Exception
	 *             if an error occurs during receiving
	 */
	public Message getMessage(String aQueueName, int aTimeOut) throws Exception {
		QueueReceiver receiver = getReceiver(aQueueName);

		Message msg;
		try {
			if (aTimeOut < 0) {
				msg = receiver.receive(0L);
			} else if (aTimeOut == 0) {
				msg = receiver.receiveNoWait();
			} else {
				msg = receiver.receive(aTimeOut);
			}
		} catch (Exception e) {
			String errorMessage = sResHash.getString("ERROR.RECEIVE.MESSAGE",
					new Object[] { aQueueName, e.getMessage() });
			logError(errorMessage);
			throw new Exception(errorMessage);
		}
		return msg;
	}

	/**
	 * Puts a Message to the System Queue
	 * 
	 * @param aQueueName
	 *            queue name to what the message have to be sent
	 * @param aMessage
	 *            object that have to be sent
	 * 
	 * @throws Exception
	 *             if an error occurs during sending
	 */
	public void putMessage(String aQueueName, Message aMessage)
			throws Exception {
		MessageProducer sender = getSender(aQueueName);

		try {
			((QueueSender) sender).send(aMessage);
		} catch (Exception e) {
			String errorMessage = sResHash.getString("ERROR.SEND.MESSAGE",
					new Object[] { aQueueName, e.getMessage() });
			logError(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	/**
	 * Gets a TextMessage from the System Queue
	 * 
	 * @param aQueueName
	 *            queue name from what the message is get
	 * @param aTimeOut
	 *            Specifies the maximum time of waiting for a new message
	 * 
	 * @return The text of the message
	 * @throws Exception
	 *             if an error occurs during receiving
	 */
	public String getTextMessage(String aQueueName, int aTimeOut)
			throws Exception {
		Message msg = getMessage(aQueueName, aTimeOut);
		if (msg instanceof TextMessage) {
			return ((TextMessage) msg).getText();
		} else {
			String errorMessage = sResHash.getString("NOT.TEXT.MESSAGE",
					aQueueName);
			logError(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	/**
	 * Puts a TextMessage to the System Queue
	 * 
	 * @param aQueueName
	 *            queue name to what the message have to be sent
	 * @param aMessageText
	 *            The text of the message to be sent
	 * 
	 * @throws Exception
	 *             if an error occurs during sending
	 */
	public void putTextMessage(String aQueueName, String aMessageText)
			throws Exception {
		TextMessage textMessage = mQueueSession.createTextMessage(aMessageText);
		putMessage(aQueueName, textMessage);
	}

	/**
	 * Gets a BytesMessage from the System Queue.
	 * 
	 * @param queueName
	 *            queue name from what the message is get
	 * @param aTimeOut
	 *            Specifies the maximum time of waiting for a new message
	 * 
	 * @return The bytes of the message
	 * @throws Exception
	 *             if an error occurs during receiving
	 */
	public byte[] getBytesMessage(String queueName, int aTimeOut)
			throws Exception {
		Message msg = getMessage(queueName, aTimeOut);
		if (msg instanceof BytesMessage) {
			BytesMessage bytesMessage = (BytesMessage) msg;
			byte[] data = new byte[Long.valueOf(bytesMessage.getBodyLength())
					.intValue()];
			bytesMessage.readBytes(data);
			return data;
		} else {
			String errorMessage = sResHash.getString("NOT.BYTES.MESSAGE",
					queueName);
			logError(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	/**
	 * Puts a BytesMessage to the System Queue.
	 * 
	 * @param aQueueName
	 *            Queue name to what the message have to be sent.
	 * @param aMessageBytes
	 *            The bytes of the message to be sent.
	 * 
	 * @throws Exception
	 *             if an error occurs during sending
	 */
	public void putBytesMessage(String aQueueName, byte[] aMessageBytes)
			throws Exception {
		BytesMessage bytesMessage = mQueueSession.createBytesMessage();
		bytesMessage.writeBytes(aMessageBytes);
		putMessage(aQueueName, bytesMessage);
	}

	/**
	 * Gets an Entry object from the System Queue
	 * 
	 * @param aQueueName
	 *            queue name from what the message is get
	 * @param aTimeOut
	 *            Specifies the maximum time of waiting for a new message
	 * 
	 * @return The Entry
	 * @throws Exception
	 *             if an error occurs during receiving
	 */
	public Entry getEntry(String aQueueName, int aTimeOut) throws Exception {
		Message msg = getMessage(aQueueName, aTimeOut);

		if (msg != null) {
			if (msg instanceof ObjectMessage) {
				ObjectMessage objMsg = (ObjectMessage) msg;
				Object entry = objMsg.getObject();
				if (entry instanceof Entry) {
					return (Entry) entry;
				} else {
					String errorMessage = sResHash.getString(
							"NOT.ENTRY.OBJECT", aQueueName);
					logError(errorMessage);
					throw new Exception(errorMessage);
				}
			} else {
				String errorMessage = sResHash.getString("NOT.OBJECT.MESSAGE",
						aQueueName);
				logError(errorMessage);
				throw new Exception(errorMessage);
			}
		}
		return null;
	}

	/**
	 * Puts an Entry object to the System Queue
	 * 
	 * @param aQueueName
	 *            queue name to what the message have to be sent
	 * @param aEntry
	 *            The Entry to be sent
	 * 
	 * @throws Exception
	 *             if an error occurs during sending
	 */
	public void putEntry(String aQueueName, Entry aEntry) throws Exception {
		ObjectMessage objectMessage = mQueueSession.createObjectMessage(aEntry);
		putMessage(aQueueName, objectMessage);
	}

	/**
	 * Creates/gets a Queue with given name. Queues are cached.
	 * 
	 * @param aQueueName
	 *            the name of the queue to be obtained.
	 * 
	 * @return The javax.jms.Queue
	 * @throws Exception
	 *             if error occurs when creating Queue
	 */
	private Queue getQueue(String aQueueName) throws Exception {
		Queue queue = mQueueMap.get(aQueueName);

		if (queue == null) {
			try {
				queue = mQueueSession.createQueue(aQueueName);
			} catch (Exception e) {
				String errorMessage = sResHash.getString("ERROR.GET.QUEUE",
						new Object[] { aQueueName, e.getMessage() });
				logError(errorMessage);
				throw new Exception(errorMessage);
			}

			if (queue == null) {
				String errorMessage = sResHash.getString("CANNOT.GET.QUEUE",
						aQueueName);
				logError(errorMessage);
				throw new Exception(errorMessage);
			}

			mQueueMap.put(aQueueName, queue);
		}

		return queue;
	}

	/**
	 * Creates/gets a QueueReceiver with given name. QueueReceivers are cached.
	 * 
	 * @param aQueueName
	 *            the name of the queue to be obtained
	 * @return The javax.jms.QueueReceiver
	 * @throws Exception
	 *             if error occurs when creating QueueReceiver
	 */
	private QueueReceiver getReceiver(String aQueueName) throws Exception {
		QueueReceiver receiver = mReceiverMap.get(aQueueName);

		if (receiver == null) {
			Queue queue = getQueue(aQueueName);
			try {
				receiver = mQueueSession.createReceiver(queue);
			} catch (Exception e) {
				String errorMessage = sResHash.getString(
						"ERROR.CREATE.RECEIVER", new Object[] { aQueueName,
								e.getMessage() });
				logError(errorMessage);
				throw new Exception(errorMessage);
			}
			mReceiverMap.put(aQueueName, receiver);
		}

		return receiver;
	}

	/**
	 * Creates/gets a MessageProducer with given name. MessageProducers are
	 * cached.
	 * 
	 * @param aQueueName
	 *            the name of the queue to be obtained
	 * 
	 * @return The javax.jms.MessageProducer
	 * @throws Exception
	 *             if error occurs when creating MessageProducer
	 */
	private MessageProducer getSender(String aQueueName) throws Exception {
		MessageProducer sender = mSenderMap.get(aQueueName);

		if (sender == null) {
			Queue queue = getQueue(aQueueName);
			try {
				sender = mQueueSession.createSender(queue);
			} catch (Exception e) {
				String errorMessage = sResHash.getString("ERROR.CREATE.SENDER",
						new Object[] { aQueueName, e.getMessage() });
				logError(errorMessage);
				throw new Exception(errorMessage);
			}
			mSenderMap.put(aQueueName, sender);
		}

		return sender;
	}

	/**
	 * Logs an error
	 * 
	 * @param aErrorMessage
	 *            error message text
	 */
	private void logError(String aErrorMessage) {
		if (mLogger != null) {
			mLogger.logerror(aErrorMessage);
		}
	}
}
