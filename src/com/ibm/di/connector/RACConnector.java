/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.hyades.logging.core.LoggingAgent;
import org.eclipse.hyades.logging.events.cbe.CommonBaseEvent;
import org.eclipse.hyades.logging.events.cbe.util.EventFormatter;
import org.eclipse.tptp.platform.execution.client.agent.IAgent;
import org.eclipse.tptp.platform.execution.client.core.ConnectionInfo;
import org.eclipse.tptp.platform.execution.client.core.IAgentController;
import org.eclipse.tptp.platform.execution.client.core.IDataProcessor;
import org.eclipse.tptp.platform.execution.client.core.INode;
import org.eclipse.tptp.platform.execution.client.core.NodeFactory;
import org.eclipse.tptp.platform.execution.exceptions.InactiveAgentException;
import org.eclipse.tptp.platform.execution.exceptions.InactiveProcessException;
import org.eclipse.tptp.platform.execution.exceptions.NotConnectedException;
import org.eclipse.tptp.platform.execution.util.TPTPDataPath;

import com.ibm.di.config.base.FunctionConfigImpl;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.FunctionInterface;
import com.ibm.di.fc.cbe.CBEGeneratorFC;
import com.ibm.di.server.ResourceHash;

/**
 * <p>
 * The RAC Connector collaborates with the Agent Controller and Generic Log
 * Adapter technologies to: - supply a TDI Assembly Line with log data from the
 * log of a remote software system (in Iterator mode) - allow a TDI Assembly
 * Line to publish data to remote clients (in AddOnly mode) (Agent Controller is
 * the new name of the Remote Agent Controller (RAC))
 * </p>
 * 
 * 
 * <p>
 * When operating in AddOnly mode, the Connector registers a Logging Agent with
 * the local Agent Controller. All Common Base Event objects, received from the
 * Assembly Line, are serialized as XML and written to the Logging Agent. The
 * Logging Agent stays operational as long as the process of the TDI server is
 * alive. During its lifetime it can be monitored by clients even if the
 * Connector which registered it has already closed. When the TDI server stops
 * (or crashes), however, the Agent Controller (RAC) terminates the TDI Logging
 * Agent's registration. The Connector could wait a specified amount of time for
 * a monitoring client to arrive before starting to write data to the Logging
 * Agent. Particularly, it can wait forever. When a client starts monitoring the
 * agent, the agent starts transferring data to the Agent Controller. The Agent
 * Controller then sends the data to the client. Waiting happens before each
 * write attempt (the 'putEntry' method of the Connector). If the waiting time
 * expires and there is still no monitoring client, the Connector throws an
 * Exception. Internally the Connector uses the TPTP Logging Agent
 * implementation - org.eclipse.hyades.logging.core.LoggingAgent.
 * </p>
 * 
 * <p>
 * In Iterator mode the RAC Connector acts as a client of a remote Agent
 * Controller. It connects to the Agent Controller to obtain a handle to the
 * Logging Agent, whose name is specified in the Connector's configuration.
 * After that the Connector starts monitoring the Logging Agent. During the
 * monitoring, the Connector receives data, produced by the Logging Agent. If
 * there is no active agent with the specified name, when the Connector contacts
 * the Agent Controller, the Connector waits until such agent is registered. If
 * at some point the agent gets unregistered (while the Connector is listening
 * for events), the Connector will wait for another agent with the same name to
 * appear. Essentially the Connector never stops unless its connection to the
 * Agent Controller fails. The implementation of the Connector relies on the
 * Java client library, which is part of the Agent Controller technology.
 * </p>
 * 
 * @since 6.1.1
 * 
 */
public class RACConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "racconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * A static cache with Logging Agents. Agents are unregistered only when the
	 * JVM exits (the Agent Controller automatically unregisters the agent, when
	 * the agent process dies). At most one agent with a given name will be
	 * registered during the lifetime of the JVM. The instance will be shared
	 * between all Connectors, which need it, so synchronization is necessary.
	 */
	private static Map<String, LoggingAgent> gRegisteredLoggingAgents = new TreeMap<String, LoggingAgent>();

	/* Connector parameters for Iterator mode */

	/**
	 * The name of the remote Logging Agent to be monitored.
	 */
	public static final String PARAM_REMOTE_LOGGING_AGENT_NAME = "remoteLoggingAgentName";

	/**
	 * Host of the remote Agent Controller.
	 */
	public static final String PARAM_AGENT_CONTROLLER_HOST = "agentControllerHost";

	/**
	 * Port of the remote Agent Controller.
	 */
	public static final String PARAM_AGENT_CONTROLLER_PORT = "agentControllerPort";

	/**
	 * The size of the queue, where the received events are buffered before the
	 * Connector manages to read them.
	 */
	public static final String PARAM_RECEIVING_QUEUE_SIZE = "receivingQueueSize";

	/**
	 * Timeout (in seconds) for each data reception after the remote agent dies.
	 * If this timeout expires, the agent's data is considered depleted and the
	 * Connector starts looking for another agent with the same name.
	 */
	public static final String PARAM_WAIT_FOR_DEAD_AGENT_DATA_TIMEOUT = "waitForDeadAgentDataTimeout";

	/**
	 * Timeout (in seconds) for the connection to the Agent Controller.
	 */
	public static final String PARAM_CONNECTION_TIMEOUT = "connectionTimeout";

	/* Connector parameters for AddOnly mode */

	/**
	 * The name of the Logging Agent within the local Agent Controller.
	 */
	public static final String PARAM_LOGGING_AGENT_NAME = "loggingAgentName";

	/**
	 * Time to wait (in seconds) for the agent to be monitored. If zero, waits
	 * forever. Potentially, waiting can be performed before each write.
	 */
	public static final String PARAM_WAIT_TO_BE_MONITORED = "waitToBeMonitored";

	/**
	 * Name of the connector
	 */
	private static final String CONNECTOR_NAME = "RACConnector";

	/**
	 * The mode in which the Connector currently operates.
	 */
	private String mConnectorMode = null;

	/* ITERATOR mode data members */

	/**
	 * Stores the last CBE retrieved by 'getNextEntry'.
	 */
	private CommonBaseEvent mCurrentCbeObject = null;

	/**
	 * A handle to the remote Agent Controller. In Iterator mode the Connector
	 * uses a single Agent Controller during its life-time.
	 */
	private IAgentController mAgentController = null;

	/**
	 * Data reception occurs in a separate thread, so this queue is used to
	 * transfer received events from the data processor to the Connector.
	 */
	private BlockingQueue<Serializable> mEventsQueue = null;

	/**
	 * A thread to find a remote Logging Agent and monitor its life.
	 */
	private LoggingAgentLifeMonitor mAgentLifeMonitorThread = null;

	/* ADDONLY mode data members */

	/**
	 * A handle to the local Logging Agent. The Connector publishes events
	 * through this agent.
	 */
	private LoggingAgent mLoggingAgent = null;

	/**
	 * Time to wait (in milliseconds) for the Logging Agent to be monitored by a
	 * client.
	 */
	private int mWaitToBeMonitored = 0;

	/**
	 * CBE Generator FC instance to help convert the input schema attributes to
	 * a single Common Base Event object.
	 */
	private FunctionInterface mCbeFromEntryFC = null;

	/**
	 * Constructor for the RAC Connector object
	 */
	public RACConnector() {
		super();
		setName(CONNECTOR_NAME);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.ADDONLY_MODE, });
	}

	/* Parameter extraction and validation routines. */

	/**
	 * Reads a required parameter from the Connector's configuration.
	 * 
	 * @param aParamName
	 *            the name of the parameter to retrieve
	 * @return the trimmed value of the Connector's parameter
	 * @exception Exception
	 *                if the parameter value is missing or blank (whitespace
	 *                only)
	 */
	private String getRequiredParam(String aParamName) throws Exception {
		String p = getParam(aParamName);
		if (p != null) {
			p = p.trim();
			if (p.length() > 0) {
				return p;
			}
		}

		throw new Exception(sResHash.getString(
				"CONNECTOR.RAC.MISSING.CONFIGURATION.PARAMETER", aParamName));
	}

	/**
	 * Reads a required number parameter from the Connector's configuration. The
	 * number must be in a specified interval.
	 * 
	 * @param aParamName
	 *            the name of the paremeter to retrieve
	 * @param aLowerBound
	 *            lower limit for the number
	 * @param aIncludeLowerBound
	 *            whether the number is allowed to reach the lower limit
	 * @param aUpperBound
	 *            upper limit for the number
	 * @return the numeric value of the Connector's parameter
	 * @exception Exception
	 *                if the parameter value is missing or is not a number if
	 *                the number is not in the specified interval
	 */
	private int getRequiredNumberParam(String aParamName, int aLowerBound,
			boolean aIncludeLowerBound, int aUpperBound) throws Exception {
		String p = getRequiredParam(aParamName);

		try {
			int num = Integer.parseInt(p);

			if (num <= aLowerBound && !aIncludeLowerBound) {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.RAC.PARAMETER.MUST.BE.NUMBER.GREATER.THAN",
										new Object[] { aParamName,
												Integer.valueOf(aLowerBound) }));
			}

			if (num < aLowerBound && aIncludeLowerBound) {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.RAC.PARAMETER.MUST.BE.NUMBER.GREATER.OR.EQUAL",
										new Object[] { aParamName,
												Integer.valueOf(aLowerBound) }));
			}

			if (num > aUpperBound) {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.RAC.PARAMETER.MUST.BE.NUMBER.LESS.OR.EQUAL",
										new Object[] { aParamName,
												Integer.valueOf(aUpperBound) }));
			}

			return num;

		} catch (NumberFormatException ex) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.RAC.PARAMETER.MUST.BE.NUMBER", aParamName));
		}
	}

	/**
	 * <p>
	 * Reads and validates the Connector's configuration parameters.
	 * </p>
	 * 
	 * <p>
	 * In Iterator mode establishes a connection to the remote Agent Controller.
	 * </p>
	 * 
	 * <p>
	 * In AddOnly mode registers a Logging Agent within the local Agent
	 * Controller. If a Logging Agent with the specified name is already
	 * registered, it is reused.
	 * </p>
	 * 
	 * @param aObj -
	 *            ignored.
	 * 
	 * @exception Exception
	 *                if some of the configuration parameters has invalid value
	 *                in Iterator mode, if a problem occurs while connecting to
	 *                the remote Agent Controller
	 */
	public void initialize(Object aObj) throws Exception {

		mConnectorMode = ((ConnectorConfig) getConfiguration()).getMode();

		if (mConnectorMode.equals(ConnectorConfig.ITERATOR_MODE)) {

			String remoteLoggingAgentName = "org.eclipse.tptp.legacy."
					+ getRequiredParam(PARAM_REMOTE_LOGGING_AGENT_NAME);

			String agentControllerHost = getRequiredParam(PARAM_AGENT_CONTROLLER_HOST);

			int agentControllerPort = getRequiredNumberParam(
					PARAM_AGENT_CONTROLLER_PORT, 0, false, 65535);

			int receivingQueueSize = getRequiredNumberParam(
					PARAM_RECEIVING_QUEUE_SIZE, 0, false, Integer.MAX_VALUE);

			long waitForDeadAgentDataTimeout = 1000 * getRequiredNumberParam(
					PARAM_WAIT_FOR_DEAD_AGENT_DATA_TIMEOUT, 0, true,
					Integer.MAX_VALUE);

			mEventsQueue = new LinkedBlockingQueue<Serializable>(
					receivingQueueSize);

			// Set the connection parameters required
			ConnectionInfo connInfo = new ConnectionInfo();
			String timeoutStr = getRequiredParam(PARAM_CONNECTION_TIMEOUT);
			DecimalFormat df = new DecimalFormat("0.000");

			try {
				float setTimeout = df.parse(timeoutStr).floatValue();
				if (setTimeout < 0) {
					throw new ParseException(timeoutStr, 0);
				}
				connInfo.setSoTimeout((int) (setTimeout * 1000));
			} catch (ParseException parseEx) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.RAC.CONNECTION.TIMEOUT.INVALID.VALUE"));
			}
			connInfo.setHostName(agentControllerHost);
			connInfo.setPort(agentControllerPort);

			// Create a Node that represents the target machine and agent
			// controller
			INode acNode = NodeFactory.createNode(agentControllerHost);

			if (null == acNode) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.RAC.CANNOT.CREATE.CONNECTION.TO.AC"));
			}

			// Connect to the Agent Controller.
			mAgentController = acNode.connect(connInfo);

			// Find the remote Logging Agent and monitor its life.
			mAgentLifeMonitorThread = new LoggingAgentLifeMonitor(
					remoteLoggingAgentName, waitForDeadAgentDataTimeout);
			mAgentLifeMonitorThread.start();
		}

		if (mConnectorMode.equals(ConnectorConfig.ADDONLY_MODE)) {

			String loggingAgentName = getRequiredParam(PARAM_LOGGING_AGENT_NAME);

			mWaitToBeMonitored = 1000 * getRequiredNumberParam(
					PARAM_WAIT_TO_BE_MONITORED, 0, true, Integer.MAX_VALUE);

			/*
			 * Look in the static cache to see if someone has already registered
			 * a Logging Agent under the specified name. If so - reuse it, if
			 * not - create a new one and put it in the cache.
			 */
			synchronized (gRegisteredLoggingAgents) {

				mLoggingAgent = gRegisteredLoggingAgents.get(loggingAgentName);

				if (null == mLoggingAgent) {

					mLoggingAgent = new org.eclipse.hyades.logging.core.LoggingAgent(
							loggingAgentName);
					gRegisteredLoggingAgents.put(loggingAgentName,
							mLoggingAgent);
				}
			}

			mCbeFromEntryFC = new CBEGeneratorFC();
			FunctionConfig config = new FunctionConfigImpl();
			mCbeFromEntryFC.setConfiguration(config);
			mCbeFromEntryFC.initialize(null);
		}

	}

	/**
	 * Wait until the logging agent is being monitored by a client, or the
	 * timeout expires.
	 * 
	 * @param aTimeToWait
	 *            the time to wait (in milliseconds); if zero waits forever
	 * @exception Exception
	 *                if the timeout expires and no monitoring client has
	 *                appeared
	 */
	private void waitAgentToBeMonitored(int aTimeToWait) throws Exception {

		if (isLogging()) {
			return; // nothing to wait for - the agent is currently being
			// monitored
		}

		if (aTimeToWait > 0) {

			logmsg(sResHash.getString(
					"CONNECTOR.RAC.WAITING.TIME.FOR.MONITORING.CLIENT", Integer
							.valueOf(aTimeToWait)));

			// not really need synchronization here, internally the LoggingAgent
			// handles it reasonably well
			boolean isBeingMonitored = mLoggingAgent
					.waitUntilLogging(aTimeToWait);

			if (isBeingMonitored) {
				logmsg(sResHash
						.getString("CONNECTOR.RAC.MONITORING.CLIENT.ARRIVED"));
			} else {
				throw new Exception(sResHash
						.getString("CONNECTOR.RAC.NO.MONITORING.CLIENT"));
			}
		} else {

			logmsg(sResHash
					.getString("CONNECTOR.RAC.WAITING.FOR.MONITORING.CLIENT"));

			/*
			 * waitTime==0 so wait forever for the logging agent to be monitored
			 * by a client not really need synchronization here, internally the
			 * LoggingAgent handles it reasonably well
			 */
			boolean isMonitored;
			do {
				isMonitored = mLoggingAgent.waitUntilLogging(1000);
			} while (!isMonitored);

			logmsg(sResHash
					.getString("CONNECTOR.RAC.MONITORING.CLIENT.ARRIVED.2"));
		}
	}

	/**
	 * Retrieves the next CommonBaseEvent produced by the remote Logging Agent.
	 * 
	 * @return the next CBE or null if no more are available.
	 * 
	 * @exception Exception
	 *                if some error occurred during the interaction with the
	 *                remote Agent Controller
	 */
	public Entry getNextEntry() throws Exception {

		Object obj = null;

		// Get a CBE out of the queue (this call will block, if the queue is
		// empty).
		try {
			obj = mEventsQueue.take();
		} catch (InterruptedException ignore) {
		}

		CommonBaseEvent cbe = null;

		/*
		 * Normally the queue contains only CBEs. If some other object is
		 * encountered, this means that the data is deleted and the Connector
		 * should terminate.
		 */
		if (obj instanceof CommonBaseEvent) {
			cbe = (CommonBaseEvent) obj;
		}

		/*
		 * Check if the life monitor has died and left some evidence on the
		 * queue.
		 */
		if (obj instanceof Exception) {
			throw (Exception) obj;
		}

		mCurrentCbeObject = cbe;

		Entry entry = null;
		if (null != cbe) {
			entry = new Entry();
			entry.addAttributeValue("$rawCBE", cbe);
			CBEGeneratorFC.mapCbeToEntry(cbe, entry);
		}

		return entry;
	}

	/**
	 * <p>
	 * Creates a CommonBaseEvent object using the attributes of the given Entry.
	 * After that serializes the Common Base Event object as a XML fragment and
	 * publishes the XML fragment through the Logging Agent.
	 * </p>
	 * 
	 * <p>
	 * Before doing anything, however, the method will wait a client to monitor
	 * the Logging Agent. (If there is already a monitoring client, the method
	 * will not wait.) If the Connector is configured to wait for a finite time
	 * and that time expires, an exception will be thrown.
	 * </p>
	 * 
	 * @param aEntry
	 *            the CBE object (as entry) to publish.
	 * 
	 * @exception Exception
	 *                if there is no monitoring client within the configured
	 *                waiting time if the specified Entry cannot be converted
	 *                into a CommonBaseEvent
	 */
	public void putEntry(Entry aEntry) throws Exception {

		waitAgentToBeMonitored(mWaitToBeMonitored);

		Entry entryWithCbe = (Entry) mCbeFromEntryFC.perform(aEntry);
		CommonBaseEvent cbe = (CommonBaseEvent) entryWithCbe.getObject("event");

		String cbeAsXML = EventFormatter.toCanonicalXMLString(cbe, false);

		// Other Connectors may use the same agent too, so synchronize the call.
		synchronized (mLoggingAgent) {
			mLoggingAgent.write(cbeAsXML);
		}
	}

	/**
	 * Terminate the Connector.
	 * 
	 * <p>
	 * In Iterator mode stops monitoring and detaches from the remote agent, so
	 * that other clients can monitor it.
	 * </p>
	 * 
	 * <p>
	 * In AddOnly mode does nothing - does not deregister the Logging Agent.
	 * This way the agent can be reused by other Connectors inside the same TDI
	 * server instance. All agents will get automatically unregistered by the
	 * Agent Controller when the TDI server process exits (the JVM shuts down).
	 * </p>
	 */
	public void terminate() {

		if (mConnectorMode.equals(ConnectorConfig.ITERATOR_MODE)) {

			// Stop the life monitoring thread
			try {
				if (null != mAgentLifeMonitorThread) {
					mAgentLifeMonitorThread.shutdown();
				}
			} catch (InterruptedException ignore) {
			}

			if (null != mAgentController) {
				mAgentController.disconnect();
			}
		}

		if (mConnectorMode.equals(ConnectorConfig.ADDONLY_MODE)) {

			/*
			 * Do not deregister the Logging Agent at this point. All agents
			 * will be unregistered automatically by the Agent Controller when
			 * the TDI JVM is shutdown.
			 */
			mLoggingAgent = null;
		}

	}

	/**
	 * This method determines whether the Logging Agent, which the Connector
	 * uses, is currently being monitored by a client. (The Connector will not
	 * write anything to the agent until it is monitored.) The method can be
	 * used only in AddOnly mode.
	 * 
	 * @return true if the Logging Agent is logging, false otherwise.
	 */
	public boolean isLogging() {

		boolean ret = false;

		if (mConnectorMode.equals(ConnectorConfig.ADDONLY_MODE)) {

			synchronized (mLoggingAgent) {
				ret = mLoggingAgent.isLogging();
			}
		}

		return ret;
	}

	/**
	 * Returns the Common Base Event object, obtained by the Connector on the
	 * current Assembly Line iteration (the last event, processed by the
	 * 'getNextEntry' method of the Connector).
	 * 
	 * @return the current CBE.
	 */
	public CommonBaseEvent getCurrentCbeObject() {
		return mCurrentCbeObject;
	}

	/**
	 * Version information.
	 * @return The version of the Connector.
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

	/* Auxiliary classes below */

	/**
	 * This time interval (in milliseconds) determines the regularity at which
	 * the Logging Agent life monitor performs its checking.
	 */
	private static final int LIFE_MONITOR_TIME_UNIT = 2000;

	/**
	 * This thread continuously performs the following cycle: - obtain handle to
	 * an agent with a specified name (wait if none is available) - start
	 * monitoring the agent (register data processor for incoming agent data) -
	 * wait until the agent is unregistered (for example when the agent process
	 * dies) - wait until all data produced by the agent is received.
	 */
	private class LoggingAgentLifeMonitor extends Thread {

		/**
		 * remote agent name.
		 */
		private String mRemoteLoggingAgentName = null;

		/**
		 * timeout in milliseconds
		 */
		private long mWaitForDeadAgentDataTimeout = 5000; // in milliseconds

		/**
		 * whether the life monitor should keep running
		 */
		private volatile boolean mKeepLifeMonitorThread = true; // whether the

		// life monitor should keep running

		/**
		 * The constructor.
		 * 
		 * @param aRemoteLoggingAgentName
		 *            the name
		 * @param aWaitForDeadAgentDataTimeout
		 *            the timeout
		 */
		public LoggingAgentLifeMonitor(String aRemoteLoggingAgentName,
				long aWaitForDeadAgentDataTimeout) {

			this.mRemoteLoggingAgentName = aRemoteLoggingAgentName;
			this.mWaitForDeadAgentDataTimeout = aWaitForDeadAgentDataTimeout;
		}

		/**
		 * Synchronously and gracefully terminates the thread. To be called by
		 * other thread.
		 * 
		 * @throws InterruptedException
		 *             if the thread was interrupted.
		 */
		public void shutdown() throws InterruptedException {

			mKeepLifeMonitorThread = false;
			this.interrupt(); // in case the thread is blocked on a call
			this.join();
		}

		/**
		 * Does the real stuff here.
		 */
		public void run() {

			IAgent remoteLoggingAgent = null;
			LoggingAgentDataProcessor dataProcessor = null;

			try {

				while (mKeepLifeMonitorThread) {

					// Start the data reception.
					dataProcessor = new LoggingAgentDataProcessor();
					remoteLoggingAgent = startMonitoringRemoteAgent(
							mRemoteLoggingAgentName, dataProcessor);

					if (debugMode()) {
						debug(sResHash
								.getString("CONNECTOR.RAC.STARTED.MONITORING.AGENT"));
					}

					/*
					 * Wait for the logging agent to die.
					 */
					while (mKeepLifeMonitorThread) {

						Thread.sleep(LIFE_MONITOR_TIME_UNIT);

						if (isAgentDead(remoteLoggingAgent)) {
							logmsg(sResHash
									.getString("CONNECTOR.RAC.AGENT.IS.DEAD"));
							break;
						}
					}

					/*
					 * Wait for the accumulated agent data to deplete: If no
					 * data is received within a given timeout, the data is
					 * considered depleted. (Note that here it is sure that the
					 * agent is already dead but we are still iterating over its
					 * data.)
					 */
					while (mKeepLifeMonitorThread) {

						Thread.sleep(LIFE_MONITOR_TIME_UNIT);

						/*
						 * Do not include the internal processing time, when
						 * calculating the reception timeout. If the data stream
						 * has really ended, the data processor will not be
						 * forever busy processing, so after a while the end of
						 * data will get detected.
						 */
						if (!dataProcessor.isBusy()) {

							// calculate the time since last data reception
							long elapsedTime = System.currentTimeMillis()
									- dataProcessor.getLastReceptionTime();

							if (elapsedTime > mWaitForDeadAgentDataTimeout) {

								logmsg(sResHash
										.getString("CONNECTOR.RAC.DATA.FROM.DEAD.AGENT.DEPLETED"));
								break;
							}
						}

					}
				}

			} catch (InterruptedException ex) {

				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.RAC.LIFE.MONITOR.INTERRUPTED"));
				}
			} catch (NotConnectedException ex) {

				logmsg(sResHash
						.getString("CONNECTOR.RAC.CONNECTION.TO.AC.DISRUPTED"));

				try {
					mEventsQueue.put(ex); // put the exception in the queue,
					// so that the Connector can rethrow
					// it
				} catch (InterruptedException ignore) {
				}

			} finally {

				/*
				 * Stop monitoring and detach from the Logging Agent. This is
				 * very important, because if this procedure is skipped, other
				 * clients will not be able to monitor the same Logging Agent.
				 */
				try {

					if (null != remoteLoggingAgent) {

						remoteLoggingAgent.stopMonitoring();
						remoteLoggingAgent.releaseAccess();
					}
				} catch (Exception ignore) {
				}

				try {
					mEventsQueue.put("OVER");
					// Mark that the Connector should stop.
				} catch (InterruptedException ignore) {
				}
			}
		}

		/**
		 * Determine if the specified agent is not active.
		 * 
		 * @param aRemoteAgent
		 *            a handle to the remote agent
		 * @return if no agent active
		 * @exception NotConnectedException
		 *                if the connection to the remote Agent Controller has
		 *                failed
		 */
		private boolean isAgentDead(IAgent aRemoteAgent)
				throws NotConnectedException {

			if (null == aRemoteAgent) {
				return true; // no agent - dead agent
			}

			String remoteLoggingAgentName = aRemoteAgent.getName();
			long remoteLoggingAgentPid = 0;
			try {
				remoteLoggingAgentPid = aRemoteAgent.getProcess()
						.getProcessId();
			} catch (InactiveProcessException ex) {
				return true; // the process, which registered the agent is
				// not active, so the agent is surely dead
			}

			/*
			 * Obtain a list of all currently running agents and check if our
			 * agent is among them. If it is not in the list, then it is dead.
			 * 
			 * This approach will break if someone has unregistered our agent
			 * and has registered another agent with the same name and all this
			 * has happened within the same process. Still this is the best we
			 * can get without messing with the internals of the client library
			 * (at least its current version - 4.2).
			 */
			boolean isDead = true;
			IAgent[] agents = mAgentController.queryRunningAgents();
			for (int i = 0; i < agents.length; ++i) {

				long agentPid = 0;

				try {
					agentPid = agents[i].getProcess().getProcessId();
				} catch (InactiveProcessException ignore) {
				}

				if (remoteLoggingAgentPid == agentPid
						&& remoteLoggingAgentName.equals(agents[i].getName())) {

					isDead = false;
					break;
				}
			}

			return isDead;
		}

		/**
		 * Find an agent with the specified name and start monitoring it with
		 * the specified data processor. If no appropriate agent is running, the
		 * method will wait for one to appear.
		 * 
		 * @param aRemoteAgentName
		 *            name
		 * @param aDataProcessor
		 *            {@link IDataProcessor}
		 * @return IAgent
		 * 
		 * @exception InterruptedException
		 *                if the calling thread has been interrupted, while the
		 *                method was waiting NotConnectedException if the
		 *                connection to the remote Agent Controller has failed
		 * @throws NotConnectedException
		 */
		private IAgent startMonitoringRemoteAgent(String aRemoteAgentName,
				IDataProcessor aDataProcessor) throws InterruptedException,
				NotConnectedException {

			IAgent remoteLoggingAgent = null;

			while (null == remoteLoggingAgent && mKeepLifeMonitorThread) {

				// find an agent with the specified name
				while (null == remoteLoggingAgent && mKeepLifeMonitorThread) {

					Thread.sleep(LIFE_MONITOR_TIME_UNIT);

					remoteLoggingAgent = mAgentController
							.getAgent(aRemoteAgentName,
									"org.eclipse.tptp.platform.execution.client.agent.IAgent");
				}

				// start monitoring the agent
				if (mKeepLifeMonitorThread) {
					try {
						remoteLoggingAgent.startMonitoring(
								TPTPDataPath.DATA_PATH_RECEIVE, aDataProcessor);
					} catch (InactiveAgentException ex) {

						// The agent died before we can monitor it - find
						// another agent.
						remoteLoggingAgent = null;
						continue;
					}
				}
			}

			return remoteLoggingAgent;
		}

	} // END of class LoggingAgentLifeMonitor

	/**
	 * This class is registered within the Agent Controller client library to
	 * receive agent data. It is being executed in a different thread.
	 */
	private class LoggingAgentDataProcessor implements IDataProcessor {

		/**
		 * The last time data was received from the agent.
		 */
		private volatile long mLastReceptionTime = 0;

		/**
		 * Is the Data Processor busy with its internal computations and so
		 * unavailable for data reception from the remote agent.
		 */
		private volatile boolean mIsBusy = false;

		/**
		 * The last time data was received from the agent.
		 * 
		 * @return the time as long
		 */
		public long getLastReceptionTime() {
			return mLastReceptionTime;
		}

		/**
		 * Is the Data Processor busy with its internal computations and so
		 * unavailable for data reception from the remote agent.
		 * 
		 * @return true if it's busy, false otherwise.
		 */
		public boolean isBusy() {
			return mIsBusy;
		}

		/**
		 * Method of IDataProcessor.
		 * 
		 * @param aBuffer
		 *            the data as bytes.
		 * @param aLength
		 *            the length of the data.
		 * @param aPeer
		 *            the address of the peer that sends this.
		 */
		public void incomingData(byte[] aBuffer, int aLength,
				java.net.InetAddress aPeer) {

			mIsBusy = true;

			try {

				final int offset = 10; // first ten bytes hold system
				// information

				// Common Base Events come as UTF-8 XML fragments
				String cbeAsXML = new String(aBuffer, offset, aLength - offset,
						"UTF-8");

				CommonBaseEvent event = EventFormatter
						.eventFromCanonicalXML(cbeAsXML);

				mEventsQueue.put(event); // this call may block if the queue
				// is full

				// Refresh latest reception time:
				mLastReceptionTime = System.currentTimeMillis();

			} catch (Exception ex) {
				logmsg(sResHash.getString(
						"CONNECTOR.RAC.PROCESSING.INCOMING.DATA.ERROR", ex));
			}

			mIsBusy = false;
		}

		/**
		 * Method of IDataProcessor.
		 * 
		 * @param aBuffer
		 *            the data as chars.
		 * @param aLength
		 *            the length of the data.
		 * @param aPeer
		 *            the address of the peer that sends this.
		 */
		public void incomingData(char[] aBuffer, int aLength,
				java.net.InetAddress aPeer) {

			mIsBusy = true;

			try {

				// Common Base Events come as UTF-8 XML fragments
				String cbeAsXML = new String(aBuffer);

				CommonBaseEvent event = EventFormatter
						.eventFromCanonicalXML(cbeAsXML);

				mEventsQueue.put(event); // this call may block if the queue
				// is full

				// Refresh latest reception time:
				mLastReceptionTime = System.currentTimeMillis();

			} catch (Exception ex) {
				logmsg(sResHash.getString(
						"CONNECTOR.RAC.PROCESSING.INCOMING.DATA.ERROR.2", ex));
			}

			mIsBusy = false;
		}

		/**
		 * Method of IDataProcessor.
		 * 
		 * @param aData
		 *            the data as bytes.
		 * @param aLength
		 *            the length of the data.
		 * @param aPeer
		 *            the address of the peer that sends this.
		 */
		public void invalidDataType(byte[] aData, int aLength,
				java.net.InetAddress aPeer) {

			// Normally this does not happen - the code does not handle this
			// data.
			logmsg(sResHash
					.getString("CONNECTOR.RAC.DATA.PROCESSOR.RECEIVED.INVALID.DATA"));
		}

		/**
		 * Method of IDataProcessor.
		 */
		public void waitingForData() {

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.RAC.DATA.PROCESSOR.WAITING"));
			}
		}

	} // END of class LoggingAgentDataProcessor

}
