/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueRequestor;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicRequestor;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.systemqueue.driver.IBMMQ;
import com.ibm.di.systemqueue.driver.JMSDriver;
import com.ibm.di.systemqueue.driver.JMSDriverFactory;
import com.ibm.di.systemqueue.driver.JMSScriptDriver;
import com.ibm.icu.util.StringTokenizer;

/**
 * The JMS Connector uses underlying implementations to access JMS based
 * systems. Since the constructor is not defined in the JMS specification we
 * actually need a java class for each JMS system.
 */
public class JMSConnector extends Connector implements MessageListener, ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The attribute name used for plain text messages
	 */
	public final static String MESSAGE_ATTRIBUTE = "message";

	/**
	 * The name under which the JMS Property is stored in the Entry
	 */
	public final static String JMS_MESSAGE_PROPERTY = "$jms.message";

	/**
	 * The name under which the JMS message type is stored in the Entry
	 * property.
	 */
	private final static String JMS_MESSAGE_TYPE = "$jms.messageType";

	/**
	 * The name under which the JMS message ID is stored in the Entry property.
	 */
	private final static String JMS_MESSAGE_ID = "$jms.messageid";

	/**
	 * Prefix of the JMS driver.
	 */
	private final static String JMS_DRIVER_PREFIX = "com.ibm.di.systemqueue.driver.";

	/**
	 * Name of the component.
	 */
	private static final String myName = "JMS Connector";

	/** The URL to the external broker */
	public static final String JMS_BROKER = IBMMQ.PROP_MQ_BROKER;

	/** Websphere MQ server channel */
	public static final String JMS_SERVER_CHANNEL = IBMMQ.PROP_MQ_CHANNEL;

	/** Websphere MQ encrypted server channel */
	public static final String JMS_SSL_SERVER_CHANNEL = "jms.sslServerChannel";

	/** The name of the queue manager parameter */
	public static final String JMS_Q_MANAGER = IBMMQ.PROP_MQ_QMANAGER;

	/** Tells whether a SSL should be used */
	public static final String JMS_SSL_USE_FLAG = IBMMQ.PROP_MQ_SSL_USE_FLAG;

	/**
	 * The name of the cipher the IBMMQ driver to use. <br />
	 * Possibilities: <br
	 * />
	 * SSL_RSA_WITH_DES_CBC_SHA <br />
	 * SSL_RSA_WITH_NULL_MD5<br />
	 * SSL_RSA_WITH_NULL_SHA<br />
	 * SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5<br />
	 * SSL_RSA_WITH_RC4_128_MD5<br />
	 * SSL_RSA_EXPORT_WITH_RC4_40_MD5<br />
	 * SSL_RSA_WITH_RC4_128_SHA<br />
	 * SSL_RSA_WITH_3DES_EDE_CBC_SHA<br />
	 * SSL_RSA_EXPORT1024_WITH_RC4_56_SHA<br />
	 * SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA<br />
	 * SSL_RSA_WITH_AES_128_CBC_SHA<br/>
	 * SSL_RSA_WITH_AES_256_CBC_SHA<br />
	 * SSL_RSA_FIPS_WITH_DES_CBC_SHA<br />
	 * SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA<br />
	 */
	public static final String JMS_SSL_CIPHER = IBMMQ.PROP_MQ_SSL_CIPHER;

	/** The username to use for establishing connection with the queue/topic. */
	public static final String JMS_USERNAME = "jms.username";

	/** The password to use for establishing connection with the queue/topic. */
	public static final String JMS_PASSWORD = "jms.password";

	/**
	 * Holds arrived messages.
	 */
	private List<Message> queue = new ArrayList<Message>();

	/**
	 * Filter criteria
	 */
	private String messageFilter = null;

	/**
	 * Topic
	 */
	private TopicConnectionFactory topicFactory = null;

	/**
	 * Topic listener thread.
	 */
	private TopicConnection topicConnection = null;

	/**
	 * Topic session.
	 */
	private TopicSession topicSession = null;

	/**
	 * Topic publisher.
	 */
	private TopicPublisher publisher = null;

	/**
	 * Topic subscriber.
	 */
	private TopicSubscriber subscriber = null;

	/**
	 * {@link Topic}
	 */
	private Topic topic = null;

	/**
	 * Creates queue connection.
	 */
	private QueueConnectionFactory queueFactory = null;

	/**
	 * The queue connection
	 */
	private QueueConnection queueConnection = null;

	/**
	 * The queue session.
	 */
	private QueueSession queueSession = null;

	/**
	 * Used to create queues
	 */
	private QueueSession sendSession = null;

	/**
	 * Receives queues
	 */
	private QueueReceiver qReceiver = null;

	/**
	 * Sends queues.
	 */
	private QueueSender qSender = null;

	/**
	 * Queue to be read.
	 */
	private Queue readQueue = null;

	/**
	 * Queue to be sent.
	 */
	private Queue sendQueue = null;

	/**
	 * Name of the queue
	 */
	private String queueName;

	/**
	 * Properties as attributes flag
	 */
	private boolean propertiesAsAttributes = false;

	/**
	 * Holds jms properties as attributes.
	 */
	private String[] specificPropertiesAsAttributes = null;
	/**
	 * Holds jms headers as attributes.
	 */
	private String[] specificHeadersAsAttributes = null;

	/**
	 * Holds JMS headers.
	 */
	private static final String[] JMS_HEADERS = { "jms.JMSCorrelationID", "jms.JMSDeliveryMode", "jms.JMSExpiration",
			"jms.JMSMessageID", "jms.JMSPriority", "jms.JMSTimestamp", "jms.JMSType", "jms.JMSReplyTo" };

	/**
	 * Next timeout index.
	 */
	private long getnextTimeout = -1;

	/**
	 * Holds the current received message.
	 */
	private Message consumedMsg = null;

	/**
	 * Mode index.
	 */
	private int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	/**
	 * Transacted session
	 */
	private boolean transactedSession = false;
	
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "jmsconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Define supported message type.
	 */
	private enum MessageType {
		/** A byte message */
		BytesMessage, //
		/** A test message */
		TextMessage, //
		/** A object message */
		ObjectMessage;
	}

	/**
	 * Holds the type of message when read/write message.
	 */
	private String jmsMsgTypeValue = null;

	/**
	 * Constructor for the JMSConnector object
	 */
	public JMSConnector() {
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.CALL_REPLY_MODE });
	}

	/**
	 * Gets the topic member of the JMSConnector object
	 *
	 * @return The topic value
	 */
	public Topic getTopic() {
		return topic;
	}

	/**
	 * Sets the topic member of the JMSConnector object
	 *
	 * @param topic
	 *            The topic value
	 */
	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	/**
	 * Gets the sendQueue member of the JMSConnector object
	 *
	 * @return The sendQueue value
	 */
	public Queue getQueue() {
		return sendQueue;
	}

	/**
	 * Gets the sendQueue member of the JMSConnector object
	 *
	 * @return The sendQueue value
	 */
	public Queue getSendQueue() {
		return sendQueue;
	}

	/**
	 * Gets the readQueue member of the JMSConnector object
	 *
	 * @return The readQueue value
	 */
	public Queue getReadQueue() {
		return readQueue;
	}

	/**
	 * Sets the sendQueue member of the JMSConnector object
	 *
	 * @param queue
	 *            The sendQueue value
	 */
	public void setSendQueue(Queue queue) {
		this.sendQueue = queue;
	}

	/**
	 * Sets the readQueue member of the JMSConnector object
	 *
	 * @param queue
	 *            The readQueue value
	 */
	public void setReadQueue(Queue queue) {
		this.readQueue = queue;
	}

	/**
	 * Gets the Session member of the JMSConnector object If topicSession non
	 * equals to null then return topicSession else return queueSession
	 *
	 * @return The topicSession value
	 */
	public Session getSession() {
		if (this.topicSession != null) {
			return topicSession;
		} else {
			return queueSession;
		}
	}

	/**
	 * Sets topicFactory or queueFactory members of the JMSConnector object If
	 * the parameter is instance of TopicConnectionFactory set topicFactory If
	 * the parameter is instance of QueueConnectionFactory set queueFactory
	 *
	 * @param factory
	 *            The TopicConnectionFactory or QueueConnectionFactory object
	 */
	public void setFactory(ConnectionFactory factory) {
		if (factory instanceof TopicConnectionFactory) {
			this.topicFactory = (TopicConnectionFactory) factory;
		} else if (factory instanceof QueueConnectionFactory) {
			this.queueFactory = (QueueConnectionFactory) factory;
		}
	}

	/**
	 * Resets object's properties.
	 */
	private void resetJMSObjects() {
		topicFactory = null;
		queueFactory = null;
		topic = null;
		readQueue = null;
		sendQueue = null;
		qSender = null;
	}

	/**
	 * Gets the nextEntry attribute of the JMSConnector object
	 *
	 * @return The nextEntry value
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public Entry getNextEntry() throws Exception {
		Message m = null;
		/*
		 * if (topicSession != null) { if (getnextTimeout == -1) m =
		 * subscriber.receive(0L); else if (getnextTimeout == 0) m =
		 * subscriber.receiveNoWait(); else m =
		 * subscriber.receive(getnextTimeout); } else if (queueSession != null)
		 * { if (getnextTimeout == -1) m = qReceiver.receive(0L); else if
		 * (getnextTimeout == 0) m = qReceiver.receiveNoWait(); else m =
		 * qReceiver.receive(getnextTimeout); }
		 */
		if (topicSession != null) {
			m = subscriber.receiveNoWait();

		} else {
			if (queueSession != null)
				m = qReceiver.receiveNoWait();
		}

		if (m == null && getnextTimeout != 0)
			m = processReceiverThread();

		consumedMsg = m; // +++Tai

		if (m == null) {
			return null;
		}

		if (debugMode()) {
			debug(sResHash.getString("RECEIVE.MESSAGE.TYPE", m.getClass().getName()));
		}

		return message2entry(m);
	}

	/**
	 * Process the RecieverThread, when we need to get int wait state for next
	 * message.
	 *
	 * @return the next message from the RecieverThread.
	 * @throws Exception
	 *             if an error occurs.
	 */

	private Message processReceiverThread() throws Exception {
		try {
			Message m = null;
			ReceiverThread rt = new ReceiverThread();
			rt.start();
			rt.join();
			if (rt.getError() != null) {
				throw rt.getError();
			}
			m = rt.getMessage();
			return m;
		} catch (InterruptedException ioe) {
			logmsg(sResHash.getString("INTERRUPTED.WAITING.FOR.MESSAGES"));
			throw ioe;
		}
	}

	/**
	 * Acknowledge all messages received in this topicSession.
	 *
	 * @return - null if OK, a string containing the error message if failure
	 */
	public String acknowledge() {
		if ((acknowledgeMode == Session.CLIENT_ACKNOWLEDGE) && (consumedMsg != null)) {
			try {
				consumedMsg.acknowledge();
				return null;
			} catch (JMSException je) {
				return je.getMessage();
			}
		}
		return sResHash.getString("NO.MESSAGE.TO.ACKNOWLEDGE");
	}

	/**
	 * Close the connection
	 */
	public void terminate() {
		if (getSession() != null) {
					try {
						if (transactedSession)
							commit();
						getSession().close();
					} catch (Exception ignore) {
						logmsg("Problem closing session: " + ignore);
					}
		}

		// Checks if the connection is an ActiveMQConnection. ActiveMQ has a
		// problem to close its connection gracefully in case of
		// Thread.interrupt() call. So we close the connection in a separate
		// thread. The problem might be solved in version 5.5 of ActiveMQ.
		// https://issues.apache.org/jira/browse/AMQ-2648
		if (isActiveMQConnection()) {
			terminateActiveMQConnection();
		} else {
			if (topicConnection != null) {
				try {
					topicConnection.stop();
					topicConnection.close();
				} catch (Exception ignore) {
					logmsg(sResHash.getString("ERROR.CLOSING.TOPIC.CONNECTION", ignore));
				}
			}
			if (queueConnection != null) {
				try {
					queueConnection.stop();
					queueConnection.close();
				} catch (Exception ignore) {
					logmsg(sResHash.getString("ERROR.CLOSING.QUEUE.CONNECTION", ignore));
				}
			}
		}

		resetJMSObjects();
	}

	/**
	 * Checks if the connection is an ActiveMQConnection.
	 *
	 * @return <b>true</b> if the connection is an ActiveMQConnection, otherwise
	 *         <b>false</b>.
	 */
	private boolean isActiveMQConnection() {
		Class<?> activeMQConnectionClass = null;
		boolean isActiveMQConn = false;
		try {
			Connection connection = topicConnection != null ? topicConnection : queueConnection;
			activeMQConnectionClass = Class.forName("org.apache.activemq.ActiveMQConnection");
			if (activeMQConnectionClass.isAssignableFrom(connection.getClass())) {
				isActiveMQConn = true;
			}
		} catch (ClassNotFoundException e) {
			isActiveMQConn = false;
		}
		return isActiveMQConn;
	}

	/**
	 * Close the connection in a separate thread if the JMS provider is
	 * ActiveMQ.
	 */
	private void terminateActiveMQConnection() {
		String errorMessageKey = null;
		CloseThread closeThread = null;
		if (topicConnection != null) {
			closeThread = new CloseThread(topicConnection);
			errorMessageKey = "ERROR.CLOSING.TOPIC.CONNECTION";
		} else {
			closeThread = new CloseThread(queueConnection);
			errorMessageKey = "ERROR.CLOSING.QUEUE.CONNECTION";
		}
		closeThread.start();
		try {
			closeThread.join();
		} catch (Exception ignore) {
			logmsg(sResHash.getString(errorMessageKey, ignore));
		}
		if (closeThread.getException() != null) {
			logmsg(sResHash.getString(errorMessageKey, closeThread.getException()));
		}
	}

	/**
	 * Creates a {@link TopicSession}
	 *
	 * @param env
	 *            not used
	 * @param jmsDriver
	 *            {@link JMSDriver}
	 * @throws Exception
	 *             if an error occurs
	 */
	private void establishTopicSession(Map<String, String> env, JMSDriver jmsDriver) throws Exception {
		String clientid = getParam("jms.clientID");
		String durable = getParam("jms.durable");
		String topicName = getParam("jms.topic");
		String username = getParam("jms.username");
		String password = getParam("jms.password");

		if (topicFactory == null) {
			topicFactory = jmsDriver.getTopicFactory();
			if (debugMode()) {
				debug(sResHash.getString("GET.TOPIC.CONNECTION.FACTORY"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("TOPIC.FACTORY.OBTAINED.FROM.SCRIPT"));
			}
		}

		if (username == null || username.equals(""))
			topicConnection = topicFactory.createTopicConnection();
		else
			topicConnection = topicFactory.createTopicConnection(username, password);

		if (clientid != null && clientid.length() > 0)
			topicConnection.setClientID(clientid);

		topicSession = topicConnection.createTopicSession(transactedSession, acknowledgeMode);

		if (topic == null) {
			topic = topicSession.createTopic(topicName);
		} else {
			if (debugMode()) {
				debug(sResHash.getString("TOPIC.OBTAINED.FROM.SCRIPT"));
			}
		}

		String str = getParam("jms.mode");
		if (str == null) {
			str = "both";
		}

		publisher = topicSession.createPublisher(topic);

		if (durable != null && durable.equalsIgnoreCase("true")) {
			if (username== null || username.length() == 0)
					throw new Exception("No User Name provided for durable subscription");
			if (clientid == null || clientid .length() == 0)
					throw new Exception("No Client ID provided for durable subscription");
			if (messageFilter != null)
				subscriber = topicSession.createDurableSubscriber(topic, username, messageFilter, false);
			else
				subscriber = topicSession.createDurableSubscriber(topic, username);
		} else {
			if (messageFilter != null)
				subscriber = topicSession.createSubscriber(topic, messageFilter, false);
			else
				subscriber = topicSession.createSubscriber(topic);
		}

		if (debugMode()) {
			debug(sResHash.getString("JMS.TOPIC.SESSION.ESTABLISHED"));
		}
	}

	/**
	 * Creates a {@link QueueSession}
	 *
	 * @param env
	 *            not used
	 * @param jmsDriver
	 *            {@link JMSDriver}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void establishQueueSession(Map<String, String> env, JMSDriver jmsDriver) throws Exception {
		String username = getParam("jms.username");
		String password = getParam("jms.password");

		if (queueFactory == null) {
			queueFactory = jmsDriver.getQueueFactory();
			if (debugMode()) {
				debug(sResHash.getString("GET.QUEUE.CONNECTION.FACTORY"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("QUEUE.FACTORY.OBTAINED.FROM.SCRIPT"));
			}
		}

		if (username == null || username.equals(""))
			queueConnection = queueFactory.createQueueConnection();
		else
			queueConnection = queueFactory.createQueueConnection(username, password);

		if (debugMode()) {
			debug(sResHash.getString("QUEUE.CONNECTION.CREATED"));
		}

		String clientid = getParam("jms.clientID");

		if (clientid != null && clientid.length() > 0) {
			queueConnection.setClientID(clientid);
		}

		queueSession = queueConnection.createQueueSession(transactedSession, acknowledgeMode);
		sendSession = queueConnection.createQueueSession(transactedSession, acknowledgeMode);
		if (debugMode()) {
			debug(sResHash.getString("QUEUE.AND.SEND.SESSIONS.CREATED"));
		}

		// Create queues
		queueName = getParam("jms.topic");
		if (readQueue == null) {
			readQueue = queueSession.createQueue(queueName);
			if (debugMode()) {
				debug(sResHash.getString("READ.QUEUE.CREATED", queueName));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("READ.QUEUE.INIT.FROM.SCRIPT"));
			}
		}

		if (sendQueue == null) {
			sendQueue = sendSession.createQueue(queueName);
			if (debugMode()) {
				debug(sResHash.getString("SEND.QUEUE.CREATED", queueName));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("SEND.QUEUE.INIT.FROM.SCRIPT"));
			}
		}

		boolean setNonJMS = true;
		String driverAttributes = getParam("jms.driverAttributes");
		if (driverAttributes != null && driverAttributes.indexOf("mq_nonjms=false") > -1) {
			setNonJMS = false;
		}

		if (getParam("jms.driver").equalsIgnoreCase("IBMMQ")) {
			Class<?> mqQueueClass = Class.forName("com.ibm.mq.jms.MQQueue");
			Class<?> jmscClass = Class.forName("com.ibm.mq.jms.JMSC");

			if (setNonJMS && mqQueueClass.isInstance(sendQueue)) {
				Method setTargetClientMethod = mqQueueClass.getMethod("setTargetClient", int.class);
				Field nonJMSMQ = jmscClass.getField("MQJMS_CLIENT_NONJMS_MQ");

				setTargetClientMethod.invoke(sendQueue, nonJMSMQ.get(jmscClass));
				Method setCCSIDMethod = mqQueueClass.getMethod("setCCSID", int.class);
				setCCSIDMethod.invoke(sendQueue, 819);
			}
		}

		if (debugMode()) {
			debug(sResHash.getString("JMS.QUEUE.SESSION.ESTABLISHED"));
		}
	}

	/**
	 * Initialize the Connector
	 *
	 * @param o
	 *            The mode of the Connector
	 * @exception Exception
	 *                if the initialization fails
	 */
	public void initialize(Object o) throws Exception {
		String broker = getParam(JMS_BROKER);
		String standardChannel = getParam(JMS_SERVER_CHANNEL);
		String sslChannel = getParam(JMS_SSL_SERVER_CHANNEL);
		String qManager = getParam(JMS_Q_MANAGER);
		String sslUseFlag = getParam(JMS_SSL_USE_FLAG);
		String sslCipher = getParam(JMS_SSL_CIPHER);
		String username = getParam(JMS_USERNAME);
		String password = getParam(JMS_PASSWORD);
		String connType = getParam("jms.connectionType");
		String specificHeaders = getParam("jms.specificHeadersAsAttributes");
		String allHeaders = getParam("jms.headersAsAttributes");
		String allProperties = getParam("jms.propertiesAsAttributes");
		String readtmo = getParam("jms.getnextTimeout");
		String specificProp = getParam("jms.specificPropertiesAsAttributes");

		if ("false".equalsIgnoreCase(getParam("jms.autoAcknowledge")))
			acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;

		transactedSession = "true".equalsIgnoreCase(getParam("jms.transactedSession"));

		messageFilter = getParam("jms.messageFilter");
		if (messageFilter != null && messageFilter.trim().length() == 0)
			messageFilter = null;

		jmsMsgTypeValue = getJmsMsgType();

		if (readtmo != null && readtmo.trim().length() > 0)
			getnextTimeout = Long.parseLong(readtmo.trim());

		String channel = (sslUseFlag.equalsIgnoreCase("true")) ? sslChannel : standardChannel;

		Hashtable<String, String> env = new Hashtable<String, String>();

		String driverAttributes = getParam("jms.driverAttributes");

		if (driverAttributes != null && driverAttributes.length() > 0) {
			StringTokenizer st = new StringTokenizer(driverAttributes, "\r\n");
			while (st.hasMoreTokens()) {
				String str = st.nextToken();
				int iEqual = str.indexOf('=');
				if (iEqual == -1)
					continue;
				env.put(str.substring(0, iEqual), str.substring(iEqual + 1));
			}
		}

		if (username != null)
			env.put(JMS_USERNAME, username);
		if (password != null)
			env.put(JMS_PASSWORD, password);
		if (qManager != null)
			env.put(JMS_Q_MANAGER, qManager);
		if (broker != null)
			env.put(JMS_BROKER, broker);
		if (channel != null)
			env.put(JMS_SERVER_CHANNEL, channel);
		if (sslCipher != null)
			env.put(JMS_SSL_CIPHER, sslCipher);

		env.put(JMS_SSL_USE_FLAG, sslUseFlag);

		String drvScript = getParam("jms.driverScript");
		if (drvScript != null && drvScript.trim().length() > 0) {
			env.put(JMSScriptDriver.PROP_JS_SCRIPT, drvScript);
		}

		String drvClass = getParam("jms.driver");
		if (drvClass == null || drvClass.equals("")) {
			throw new Exception(sResHash.getString("NO.DEFAULT.JMS.DRIVER"));
		}

		if (drvClass.equalsIgnoreCase("IBMMQ") || drvClass.equalsIgnoreCase("IBMMQe")
				|| drvClass.equalsIgnoreCase("JMSScriptDriver") || drvClass.equalsIgnoreCase("WebsphereESB")
				|| drvClass.equalsIgnoreCase("ActiveMQ")) {
			drvClass = JMS_DRIVER_PREFIX + drvClass;
		}

		if (debugMode()) {
			debug(sResHash.getString("JMS.LOAD.DRIVER", drvClass));
		}

		JMSDriver jmsDriver = (JMSDriver) JMSDriverFactory.getDriver(drvClass, env);

		// Connect to topic/queue
		if (connType == null || connType.equalsIgnoreCase("topic")) {
			establishTopicSession(env, jmsDriver);
		} else {
			establishQueueSession(env, jmsDriver);
		}

		// JMS Headers
		if (allHeaders != null && allHeaders.equalsIgnoreCase("true")) {
			specificHeadersAsAttributes = JMS_HEADERS;
			if (debugMode()) {
				debug(sResHash.getString("MAP.JMS.HEADERS.TO.ATTRIBUTES"));
			}
		} else if (specificHeaders != null && specificHeaders.length() > 0) {
			StringTokenizer st = new StringTokenizer(specificHeaders, "\r\n");
			Vector<String> v = new Vector<String>();
			while (st.hasMoreTokens()) {
				String str = st.nextToken();
				if (!str.startsWith("jms."))
					str = "jms." + str;
				v.add(str);
			}
			specificHeadersAsAttributes = new String[v.size()];
			v.copyInto(specificHeadersAsAttributes);
			if (debugMode()) {
				debug(sResHash.getString("MAP.SPECIFIC.JMS.HEADERS.TO.ATTRIBUTES"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("NO.SPECIFIC.MAP.JMS.HEADERS.TO.ATTRIBUTES"));
			}
		}

		// JMS Properties
		if (allProperties != null && allProperties.equalsIgnoreCase("true")) {
			propertiesAsAttributes = true;
			specificProp = null;
			if (debugMode()) {
				debug(sResHash.getString("MAP.ALL.JMS.PROPERTIES.TO.ATTRIBUTES"));
			}
		} else if (specificProp != null && specificProp.length() > 0) {
			StringTokenizer st = new StringTokenizer(specificProp, "\r\n");
			Vector<String> v = new Vector<String>();
			while (st.hasMoreTokens()) {
				String str = st.nextToken();
				if (!str.startsWith("jms."))
					str = "jms." + str;
				v.add(str);
			}
			specificPropertiesAsAttributes = new String[v.size()];
			v.copyInto(specificPropertiesAsAttributes);
			if (debugMode()) {
				debug(sResHash.getString("MAP.SPECIFIC.JMS.PROPERTIES.TO.ATTRIBUTES"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("USING.BACK.COMPATIBLE.MAPPING"));
			}
		}

	}

	/**
	 * Determinate message type from UI option
	 *
	 * @return message type
	 */
	private String getJmsMsgType() {
		String useText = getParam("jms.usetextmessages");
		String jmsMsgTypeValue = null;

		// Default behavior is to serialize the entry object
		if ("true".equalsIgnoreCase(useText)) {
			jmsMsgTypeValue = MessageType.TextMessage.toString();
		} else {
			jmsMsgTypeValue = MessageType.BytesMessage.toString();
		}

		String messageType = getParam("jms.messageType");
		if (messageType != null) {
			jmsMsgTypeValue = messageType;
		}

		return jmsMsgTypeValue;
	}

	/**
	 * We only start the queue/topic listener thread if we are iterating
	 *
	 * @exception Exception
	 *                if an exception occurs while listening for messages
	 */
	public void selectEntries() throws Exception {
		if (subscriber != null) {
			topicConnection.start();
			if (debugMode()) {
				debug(sResHash.getString("TOPIC.LISTENER.STARTED"));
			}
		}

		if (queueSession != null) {
			if (messageFilter != null)
				qReceiver = queueSession.createReceiver(readQueue, messageFilter);
			else
				qReceiver = queueSession.createReceiver(readQueue);
			queueConnection.start();
			if (debugMode()) {
				debug(sResHash.getString("QUEUE.LISTENER.STARTED"));
			}
		}
	}

	/**
	 * Query the queue for specific messages. If jms.lookupConsumesMessage is
	 * true then we use the JMS QueueReceiver otherwise the QueueBrowser is
	 * used.
	 *
	 * @param search
	 *            The search criteria for message selection
	 *
	 * @return Returns the first entry found or null if no entries were found
	 * @exception Exception
	 *                Any JMS error
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {

		// Allow both "MessageID" and "jms.MessageID"
		Vector<?> v = search.getCriteria();
		for (int i = 0; i < v.size(); i++) {
			SearchCriteria.rscSearch flt = (SearchCriteria.rscSearch) v.get(i);
			if (flt.name != null && flt.name.startsWith("jms.") && flt.name.length() > 4)
				flt.name = flt.name.substring(4);
		}

		String filter = search.getSQLFilter();
		boolean remove = false;
		int retries = 10;
		int timeout = 1000;

		// Clear list of multiple entries found
		clearFindEntries();

		if (hasConfigValue("jms.lookupRetries")) {
			retries = Integer.parseInt(getParam("jms.lookupRetries"));
		}
		if (hasConfigValue("jms.lookupTimeout")) {
			timeout = Integer.parseInt(getParam("jms.lookupTimeout"));
		}
		if (hasConfigValue("jms.lookupConsumesMessage")) {
			remove = (getParam("jms.lookupConsumesMessage").equalsIgnoreCase("true") ? true : false);
		}

		// Create QBrowser
		if (debugMode()) {
			debug(sResHash.getString("LOOKUP.FILTER", filter));
		}

		if (queueSession != null) {

			// If we use the QueueReceiver we will retry until we get at least
			// one entry
			// and then we'll get subsequent messages using a very small timeout
			// until queuereceiver returns null.
			if (remove) {
				int maxfind = getMaxDuplicateEntries();
				if (debugMode()) {
					debug(sResHash.getString("LOOKUP.WILL.REMOVE.JMS.MESSAGE"));
				}
				javax.jms.QueueReceiver qr = queueSession.createReceiver(readQueue, filter);
				queueConnection.start();
				while (retries-- > 0) {
					Message message = null;
					if ((message = qr.receive(timeout)) != null) {
						do {
							addFindEntry(message2entry(message));
							maxfind--;
						} while (maxfind > 0 && (message = qr.receiveNoWait()) != null);

						queueConnection.stop();
						if (getFindEntryCount() == 1)
							return getFirstFindEntry();
						else
							return null;
					}

					if (debugMode()) {
						debug(sResHash.getString("NO.ENTRIES.RECEIVER.RETRIES", String.valueOf(retries)));
					}
					Thread.sleep(timeout);
				}

				queueConnection.stop();

			} else {
				if (debugMode()) {
					debug(sResHash.getString("LOOKUP.WILL.NOT.REMOVE.JMS.MESSAGE"));
				}
				javax.jms.QueueBrowser q = queueSession.createBrowser(readQueue, filter);
				queueConnection.start();
				while (retries-- > 0) {

					Enumeration<?> e = q.getEnumeration();
					if (e.hasMoreElements()) {

						while (e.hasMoreElements()) {
							Message msg = (Message) e.nextElement();
							boolean added = addFindEntry(message2entry(msg));
							if (!added) {
								break;
							}
						}
						queueConnection.stop();
						if (getFindEntryCount() == 1)
							return getFirstFindEntry();
						else
							return null;
					}

					if (debugMode()) {
						debug(sResHash.getString("NO.ENTRIES.BROWSER.RETRIES", String.valueOf(retries)));
					}

					Thread.sleep(timeout);
				}
				queueConnection.stop();
			}

		} else if (topicSession != null) {
			if (debugMode()) {
				debug(sResHash.getString("LOOKUP.MODE.NOT.SUPPORTED.FOR.TOPIC"));
			}
			return null;
		}

		return null;
	}

	/**
	 * Send an entry to the JMS server.
	 *
	 * @param entry
	 *            The entry to send
	 * @exception Exception
	 *                If the sending of a message fails
	 */
	public void putEntry(Entry entry) throws Exception {
		if(sendSession != null && qSender == null ) 
			initQSender();  
			
		if (entry != null) {
			int messageJMSDeliverymode = Message.DEFAULT_DELIVERY_MODE;
			if (entry.getAttribute("jms.JMSDeliveryMode") != null) {
				Object value = entry.getAttribute("jms.JMSDeliveryMode").getValue(0);
				messageJMSDeliverymode = (parseInt(value));
			}

			long messageJMSExpiration = Message.DEFAULT_TIME_TO_LIVE;
			if (entry.getAttribute("jms.JMSExpiration") != null) {
				Object value = entry.getAttribute("jms.JMSExpiration").getValue(0);
				messageJMSExpiration = parseLong(value);
			}

			int messageJMSPriority = Message.DEFAULT_PRIORITY;
			if (entry.getAttribute("jms.JMSPriority") != null) {
				Object value = entry.getAttribute("jms.JMSPriority").getValue(0);
				messageJMSPriority = (parseInt(value));
			}

			Message message = entry2message(entry);

			if (publisher != null) {
				publisher.publish(message, messageJMSDeliverymode, messageJMSPriority, messageJMSExpiration);
			} else if (qSender != null) {
				qSender.send(message, messageJMSDeliverymode, messageJMSPriority, messageJMSExpiration);
			}

			entry.setProperty(JMS_MESSAGE_ID, message.getJMSMessageID());
		}
	}

	/**
	 * Initialize qSender. 
	 * @throws Exception
	 */
	private void initQSender() throws Exception{
		if( sendSession != null && sendQueue !=null )
			qSender = sendSession.createSender(sendQueue);
		
	}

	/**
	 * Parse integer value from a Integer or String object.
	 *
	 * @param value
	 *            to be parsed.
	 * @return integer value.
	 */
	private int parseInt(Object value) {
		return value instanceof Integer ? ((Integer) value).intValue() : Integer.parseInt(value.toString());
	}

	/**
	 * Parse long value from a Long or String object.
	 *
	 * @param value
	 *            to be parsed.
	 * @return long value.
	 */
	private long parseLong(Object value) {
		return value instanceof Long ? ((Long) value).longValue() : Long.parseLong(value.toString());
	}

	/**
	 * This method uses temporary queues/topics to implement the queryReply
	 * method. The receiver of the message must heed the replyTo property of the
	 * message.
	 *
	 * @param entry
	 *            the entry to send.
	 * @return the response as entry
	 * @throws Exception
	 *             if a JMS exception occurs
	 */
	public Entry queryReply(Entry entry) throws Exception {

		Message m = entry2message(entry);
		Message reply = null;

		if (sendSession != null) {
			QueueRequestor qr = new QueueRequestor(sendSession, sendQueue);
			queueConnection.start();
			reply = qr.request(m);
			queueConnection.stop();
		} else {
			TopicRequestor tr = new TopicRequestor(topicSession, topic);
			reply = tr.request(m);
		}
		if (reply != null)
			return message2entry(reply);
		else
			return null;
	}

	/**
	 * Handles the arrived message
	 *
	 * @param message
	 *            The received message
	 */
	public void onMessage(Message message) {
		try {
			if (debugMode()) {
				debug(sResHash.getString("ON.MESSAGE", message.getJMSType()));
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		synchronized (this) {
			queue.add(message);
			notify();
		}
	}

	/**
	 * Convert an entry to a JMS message. Use the configuration to determine how
	 * the generated JMS message should be like.
	 *
	 * @param entry
	 *            The entry to convert
	 *
	 * @return The JMS message
	 * @exception Exception
	 *                if an error occurs
	 */
	public Message entry2message(Entry entry) throws Exception {

		Session session;

		if (this.topicSession != null) {
			session = this.topicSession;
		} else {
			session = this.queueSession;
		}

		Message m = null;

		Object jmsMsgType = entry.getProperty(JMS_MESSAGE_TYPE);
		if (jmsMsgType instanceof String) {
			jmsMsgTypeValue = (String) jmsMsgType;
		}

		MessageType msgType = MessageType.valueOf(jmsMsgTypeValue);

		switch (msgType) {
		case TextMessage: {
			m = session.createTextMessage();
			break;
		}
		case BytesMessage: {
			m = session.createBytesMessage();
			break;
		}
		case ObjectMessage: {
			m = session.createObjectMessage();
			break;
		}
		}

		// Set JMS headers
		setJMSHeaders(m, entry);

		// Set JMS properties
		setJMSProperties(m, entry);

		switch (msgType) {
		case TextMessage: {
			if (hasParser()) {
				if (debugMode()) {
					logmsg(sResHash.getString("TEXT.MESSAGE.USING.PARSER"));
				}

				StringWriter sr = new StringWriter();
				initParser(null, sr);
				getParser().writeEntry(entry);
				getParser().closeParser();
				((TextMessage) m).setText(sr.toString());
			} else {
				if (debugMode()) {
					logmsg(sResHash.getString("TEXT.MESSAGE.USING.MESSAGE.ATTRIBUTE") + entry.getString(MESSAGE_ATTRIBUTE));
				}
				((TextMessage) m).setText(entry.getString(MESSAGE_ATTRIBUTE));
			}
			break;
		}
		case BytesMessage: {

			if (debugMode()) {
				logmsg(sResHash.getString("SERIALIZED.BYTES.MESSAGE"));
			}

			Object messageAttr = entry.getObject(MESSAGE_ATTRIBUTE);
			if (messageAttr instanceof byte[]) {
				if (debugMode()) {
					logmsg("Using provided byte array");
				}
				((BytesMessage) m).writeBytes((byte[]) messageAttr);		
				return m;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(entry);
			oos.close();
			bos.close();

			((BytesMessage) m).writeInt(bos.size());
			((BytesMessage) m).writeBytes(bos.toByteArray());
			break;
		}
		case ObjectMessage: {
			if (debugMode()) {
				logmsg(sResHash.getString("OBJECT.MESSAGE"));
			}
			((ObjectMessage) m).setObject(entry);
			break;
		}
		}
		return m;
	}

	/**
	 * Converts the provided bytesMessage into byte array
	 *
	 * @param bytesMessage
	 *            {@link BytesMessage}
	 * @return byte array.
	 * @throws JMSException
	 *             if an error occurs.
	 */
	private byte[] extractBytes(BytesMessage bytesMessage) throws JMSException {
		byte[] message = null;
		if (bytesMessage != null) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			while (true) { // rely on MessageEOFException to get out
				try {
					buffer.write(bytesMessage.readByte());
				} catch (MessageEOFException eofEx) {
					break;
				}
			}
			if (buffer != null)
				message = buffer.toByteArray();
		}
		return message;
	}

	/**
	 * Checks if Message is from Websphere MQ Series.
	 *
	 * @param bytesmessage
	 *            message to be checked.
	 * @param ibmCharset
	 *            Encoding
	 *
	 * @return if bytesmessage is from Websphere MQ Series returns
	 *         <code>true</code>, otherwise <code>false</code>
	 * @throws JMSException
	 *             if an error occurs
	 * @throws UnsupportedEncodingException
	 *             if an error occurs
	 */
	private boolean isMQTextMessage(BytesMessage bytesmessage, String ibmCharset) throws JMSException, UnsupportedEncodingException {
		String mqFormat = bytesmessage.getStringProperty("JMS_IBM_Format");
		boolean isMQTextMessage = false;
		if (mqFormat != null) {
			mqFormat = new String(mqFormat.getBytes(), ibmCharset);
			if (debugMode()) {
				debug(sResHash.getString("MQ.FORMAT.IS", mqFormat));
			}

			// it returns 'MQSTR '
			if (mqFormat.equals("MQSTR   ")) {
				isMQTextMessage = true;
			}
		}
		return isMQTextMessage;
	}

	/**
	 * Gets the name of the charset set as a property to a {@link BytesMessage}
	 *
	 * @param bytesmessage
	 *            the byte message.
	 * @return the name of the charset
	 * @throws JMSException
	 *             if an error occurs while retrieving the specific property.
	 */
	public String getIBMCharset(BytesMessage bytesmessage) throws JMSException {
		String alias = bytesmessage.getStringProperty("JMS_IBM_Character_Set");
		Charset charset = Charset.forName(alias);
		return charset.name();
	}

	/**
	 * Converts a {@link BytesMessage} to a {@link String}
	 *
	 * @param bytesMessage
	 *            the message to convert.
	 * @param ibmCharset
	 *            encoding
	 * @return the string representation of the message.
	 * @throws JMSException
	 *             if error while reading bytes occurs.
	 * @throws UnsupportedEncodingException
	 *             if the provided encoding is not valid.
	 */
	private String convertMQBytesMessagetoString(BytesMessage bytesMessage, String ibmCharset) throws JMSException,
			UnsupportedEncodingException {
		byte[] msg = extractBytes(bytesMessage);
		return new String(msg, ibmCharset);
	}

	/**
	 * Convert a JMS message to an entry.
	 *
	 * @param m
	 *            The JMS message to convert
	 * @return An entry object
	 * @exception Exception
	 *                if an error occurs
	 */
	public Entry message2entry(Message m) throws Exception {

		Entry entry = null;
		String messageType = null;

		if (m instanceof TextMessage) {

			messageType = MessageType.TextMessage.toString();
			if (debugMode()) {
				logmsg(sResHash.getString("TEXT.MESSAGE") + ((TextMessage) m).getText());
			}

			// Use provided parser to parse text message
			if (hasParser()) {
				if (debugMode()) {
					logmsg(sResHash.getString("TEXT.MESSAGE.USING.PARSER.LT"));
				}
				StringReader sr = new StringReader(((TextMessage) m).getText());
				initParser(sr, null);
				entry = getParser().readEntry();
				getParser().closeParser();
			} else {
				if (debugMode()) {
					logmsg(sResHash.getString("TEXT.MESSAGE.NO.PARSER"));
				}
				entry = new Entry();
				entry.setAttribute(MESSAGE_ATTRIBUTE, ((TextMessage) m).getText());
			}

			// ... or pull out the serialized metamerge.entry object (if any)
		} else {

			if (m instanceof BytesMessage) {

				BytesMessage bytesMessage = (BytesMessage) m;

				boolean isMQTextMessage = false;

				try {
					String ibmCharset = getIBMCharset(bytesMessage);
					if (isMQTextMessage(bytesMessage, ibmCharset)) {
						String strBuffer = convertMQBytesMessagetoString(bytesMessage, ibmCharset);

						// Use provided parser to parse text message
						if (hasParser()) {
							if (debugMode()) {
								logmsg(sResHash.getString("TEXT.MESSAGE.USING.PARSER.LT"));
							}
							StringReader sr = new StringReader(strBuffer);
							initParser(sr, null);
							entry = getParser().readEntry();
							getParser().closeParser();
						} else {
							if (debugMode()) {
								logmsg(sResHash.getString("TEXT.MESSAGE.NO.PARSER"));
							}
							entry = new Entry();
							entry.setAttribute(MESSAGE_ATTRIBUTE, strBuffer);
						}
						isMQTextMessage = true;
					}
				} catch (Exception ex) {
					isMQTextMessage = false;
				}

				if (!isMQTextMessage) {
					byte[] buffer = null;
					if (! "false".equalsIgnoreCase(getParam("jms.deserializeBytesMessage"))) {
						try {
							int length = bytesMessage.readInt();

							if (length > 0) {
								buffer = new byte[length];
								bytesMessage.readBytes(buffer);

								ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
								ObjectInputStream ois = new ObjectInputStream(bis);

								if (debugMode()) {
									logmsg(sResHash.getString("BYTES.MESSAGE.LT"));
								}
								Object obj = ois.readObject();
								if (obj instanceof Entry) {
									entry = (Entry) obj;
								}
							}
						} catch (Exception error) {
							logmsg(sResHash.getString("UNABLE.TO.DESERIALIZE.BYTES.MESSAGE", error));
						}
					}

					if (entry == null) {
						if (debugMode()) {
							logmsg(sResHash.getString("BYTES.MESSAGE.AS.ATTRIBUTE"));
						}
						if (buffer==null) {
							bytesMessage.reset();
							buffer = new byte[(int)bytesMessage.getBodyLength()];
							bytesMessage.readBytes(buffer);
						}
						entry = new Entry();
						entry.setAttribute(MESSAGE_ATTRIBUTE, buffer);
					} else {
						if (debugMode()) {
							logmsg(sResHash.getString("BYTES.MESSAGE.AS.ENTRY"));
						}
					}
				}
				messageType = MessageType.BytesMessage.toString();
			}

			if (m instanceof ObjectMessage) {

				Object obj = ((ObjectMessage) m).getObject();
				if (obj instanceof Entry) {
					entry = (Entry) obj;
				}

				if (entry == null) {
					entry = new Entry();
					entry.setAttribute("java.object", obj);
					entry.setAttribute("java.objectClass", obj.getClass().getName());
				}
				messageType = MessageType.ObjectMessage.toString();
			}
		}

		// Populate entry from JMS headers
		getJMSHeaders(m, entry);

		// Get JMS properties
		getJMSProperties(m, entry);

		// Save the message just in case some monkey needs it
		entry.setProperty(JMS_MESSAGE_PROPERTY, m);

		// Save the message type
		entry.setProperty(JMS_MESSAGE_TYPE, messageType);

		return entry;
	}

	/**
	 * Extract the JMS properties from the message and writes them to the entry
	 * object.
	 *
	 * @param m
	 *            {@link Message}
	 * @param entry
	 *            {@link Entry}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void getJMSProperties(Message m, Entry entry) throws Exception {
		if (m == null || entry == null)
			return;

		// Backwards compatible mapping of JMS properties
		if (!propertiesAsAttributes && specificPropertiesAsAttributes == null) {
			if (debugMode()) {
				debug(sResHash.getString("PERFORM.BACKUP.COMPATIBLE.MAPPING"));
			}
			for (Enumeration<?> e = m.getPropertyNames(); e != null && e.hasMoreElements();) {
				String name = (String) e.nextElement();
				entry.setProperty("jms." + name, m.getStringProperty(name));
			}
		} else if (propertiesAsAttributes) {
			if (debugMode()) {
				debug(sResHash.getString("MAP.ALL.PROPERTIES"));
			}
			for (Enumeration<?> e = m.getPropertyNames(); e != null && e.hasMoreElements();) {
				String name = (String) e.nextElement();
				entry.setAttribute("jms." + name, m.getObjectProperty(name));
			}
		} else if (specificPropertiesAsAttributes != null) {
			if (debugMode()) {
				debug(sResHash.getString("MAP.CERTAIN.PROPERTIES"));
			}
			for (int i = 0; i < specificPropertiesAsAttributes.length; i++) {
				String name = specificPropertiesAsAttributes[i];
				entry.setAttribute(name, m.getObjectProperty(name.substring(4)));
			}
		}
	}

	/**
	 * Extract the JMS properties from the entry and writes them to the message
	 * object.
	 *
	 * @param m
	 *            {@link Message}
	 * @param entry
	 *            {@link Entry}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void setJMSProperties(Message m, Entry entry) throws Exception {

		// Backwards compatible mapping of JMS properties
		if (!propertiesAsAttributes && specificPropertiesAsAttributes == null) {
			String[] props = entry.getPropertyNames();
			for (int i = 0; i < props.length; i++) {
				if (props[i].startsWith("jms.")) {
					m.setObjectProperty(props[i].substring(4), entry.getProperty(props[i]));
				}
			}
		} else if (propertiesAsAttributes || specificPropertiesAsAttributes != null) {
			String[] names;
			if (propertiesAsAttributes)
				names = entry.getAttributeNames();
			else
				names = specificPropertiesAsAttributes;

			for (int i = 0; i < names.length; i++) {
				if (!names[i].startsWith("jms.") || names[i].startsWith("jms.JMS")) {
					if (debugMode()) {
						debug(sResHash.getString("HEADER.DOES.NOT.START.WITH.JMS", names[i]));
					}
					continue;
				}
				m.setObjectProperty(names[i].substring(4), entry.getObject(names[i]));
			}
		}
	}

	/**
	 * Extract the JMS headers from the message and writes them to the entry
	 * object.
	 *
	 * @param m
	 *            {@link Message}
	 * @param entry
	 *            {@link Entry}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void getJMSHeaders(Message m, Entry entry) throws Exception {

		if (specificHeadersAsAttributes == null)
			return;

		for (int i = 0; i < specificHeadersAsAttributes.length; i++) {
			String hdr = (String) specificHeadersAsAttributes[i];

			if (debugMode()) {
				debug(sResHash.getString("GET.JMS.HEADER", hdr));
			}
			if (hdr.equalsIgnoreCase("jms.JMSCorrelationID"))
				entry.setAttribute(hdr, m.getJMSCorrelationID());
			else if (hdr.equalsIgnoreCase("jms.JMSDeliveryMode"))
				entry.setAttribute(hdr, Integer.valueOf(m.getJMSDeliveryMode()));
			else if (hdr.equalsIgnoreCase("jms.JMSExpiration"))
				entry.setAttribute(hdr, Long.valueOf(m.getJMSExpiration()));
			else if (hdr.equalsIgnoreCase("jms.JMSMessageID"))
				entry.setAttribute(hdr, m.getJMSMessageID());
			else if (hdr.equalsIgnoreCase("jms.JMSPriority"))
				entry.setAttribute(hdr, Integer.valueOf(m.getJMSPriority()));
			else if (hdr.equalsIgnoreCase("jms.JMSTimestamp"))
				entry.setAttribute(hdr, Long.valueOf(m.getJMSTimestamp()));
			else if (hdr.equalsIgnoreCase("jms.JMSType"))
				entry.setAttribute(hdr, m.getJMSType());
			else if (hdr.equalsIgnoreCase("jms.JMSReplyTo"))
				entry.setAttribute(hdr, m.getJMSReplyTo());
			else
				throw new Exception(sResHash.getString("UNKNOWN.JMS.HEADER", hdr));
		}
	}

	/**
	 * Extract the JMS headers from the entry and writes them to the message
	 * object.
	 *
	 * @param m
	 *            {@link Message}
	 * @param entry
	 *            {@link Entry}
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void setJMSHeaders(Message m, Entry entry) throws Exception {

		if (specificHeadersAsAttributes == null)
			return;

		for (int i = 0; i < specificHeadersAsAttributes.length; i++) {
			String hdr = (String) specificHeadersAsAttributes[i];
			Attribute attr = entry.getAttribute(hdr);
			if (attr == null) {
				if (debugMode()) {
					debug(sResHash.getString("NO.ATTRIBUTE.FOR", hdr));
				}
				continue;
			}
			entry.removeAttribute(hdr);

			Object value = attr.getValue(0);

			if (debugMode()) {
				debug(sResHash.getString("SET.JMS.HEADER", hdr));
			}
			if (hdr.equals("jms.JMSCorrelationID"))
				m.setJMSCorrelationID(value != null ? value.toString() : null);
			else if (hdr.equals("jms.JMSDeliveryMode"))
				m.setJMSDeliveryMode(parseInt(value));
			else if (hdr.equals("jms.JMSExpiration"))
				m.setJMSExpiration(parseLong(value));
			else if (hdr.equals("jms.JMSMessageID"))
				m.setJMSMessageID(value != null ? value.toString() : null);
			else if (hdr.equals("jms.JMSPriority"))
				m.setJMSPriority(parseInt(value));
			else if (hdr.equals("jms.JMSType"))
				m.setJMSType(value != null ? value.toString() : null);
			else if (hdr.equals("jms.JMSReplyTo")) {
				if (value instanceof Destination) {
					m.setJMSReplyTo((Destination) value);
				} else if (value == null || value.equals("%this%")) {
					if (topic != null)
						m.setJMSReplyTo(topic);
					else
						m.setJMSReplyTo(readQueue);
				} else {
					if (debugMode()) {
						debug(sResHash.getString("SET.JMS.REPLY.TO", value));
					}
					if (topicSession != null)
						m.setJMSReplyTo(topicSession.createTopic(value.toString()));
					else
						m.setJMSReplyTo(queueSession.createQueue(value.toString()));
				}
			} else
				throw new Exception(sResHash.getString("UNKNOWN.JMS.HEADER", hdr));
		}
	}

	/**
	 * Return version information
	 *
	 * @return The version value
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * {@inheritDoc}
	 */
	public void reconnect() throws Exception {
		terminate();

		initialize(this);
		if (((ConnectorConfig) getConfiguration()).getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
			selectEntries();
		}
	}

	/**
	 * Commits all messages done in this transaction.
	 * This method can only be used with a transacted session.
	 * @since 7.2.0.1
	 */
	public void commit() throws JMSException {
		if (!transactedSession)
			return;

		if (topicSession != null) {
			topicSession.commit();
		} else {
			if (queueSession != null)
				queueSession.commit();
			if (sendSession != null)
				sendSession.commit();	
		}

	}
	
	/**
	 * Rolls back any messages done in this transaction.
	 * This method can only be used with a transacted session.
	 * @since 7.2.0.1
	 */
	public void rollback() throws JMSException {
		if (!transactedSession)
			return;

		if (topicSession != null) {
			topicSession.rollback();
		} else {
			if (queueSession != null)
				queueSession.rollback();
			if (sendSession != null)
				sendSession.rollback();	
		}
	}
	
	/**
	 * Thread waiting for messages.
	 */
	private class ReceiverThread extends Thread {
		/**
		 * Holds the next received message
		 */
		Message m;

		/**
		 * Error information.
		 */
		Exception err;

		/**
		 * @return the retrieved {@link Message} object, or null.
		 */
		public Message getMessage() {
			return m;
		}

		/**
		 * @return the exception that have been raised, or null.
		 */
		public Exception getError() {
			return err;
		}

		/**
		 * Waits for messages, when a message arrives the thread's execution
		 * ends.
		 */
		public void run() {
			m = null;
			err = null;
			try {
				if (topicSession != null) {
					if (getnextTimeout == -1)
						m = subscriber.receive(0L);
					else
						m = subscriber.receive(getnextTimeout);
				} else {
					if (queueSession != null) {
						if (getnextTimeout == -1)
							m = qReceiver.receive(0L);
						else
							m = qReceiver.receive(getnextTimeout);
					}
				}
			} catch (Exception e) {
				err = e;
				// ioe.printStackTrace();
			}
		}
	}

	/**
	 * Class used to close the connection in a separate thread.
	 */
	private static class CloseThread extends Thread {

		/**
		 * The connection that will be closed.
		 */
		private Connection connection = null;

		/**
		 * Contains an exception occurred during the closing of the connection.
		 */
		private Exception exception = null;

		/**
		 * Constructor.
		 *
		 * @param connection
		 *            The connection that will be set for closing.
		 */
		private CloseThread(Connection connection) {
			this.connection = connection;
		}

		/**
		 * Closes connection to the JMS provider.
		 */
		public void run() {
			if (connection != null) {
				try {
					connection.stop();
					connection.close();
				} catch (JMSException e) {
					exception = e;
				}
			}
		}

		/**
		 * Returns the exception occurred during the closing action.
		 *
		 * @return the exception that have been raised, or null.
		 */
		private Exception getException() {
			return exception;
		}
	}
}
