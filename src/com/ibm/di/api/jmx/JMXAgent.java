/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.mbeans.BaseAdmin;
import com.ibm.di.api.jmx.mbeans.DIServer;
import com.ibm.di.api.jmx.mbeans.Notifier;
import com.ibm.di.api.jmx.mbeans.SecurityRegistry;
import com.ibm.di.api.jmx.mbeans.ServerInfo;
import com.ibm.di.api.jmx.mbeans.SystemLog;
import com.ibm.di.api.jmx.mbeans.SystemQueue;
import com.ibm.di.api.jmx.mbeans.TombstoneManager;
import com.ibm.di.server.ResourceHash;

/**
 * 
 * Class used to to expose all Server API calls through a JMX interface locally
 * and remotely.
 * 
 */
public class JMXAgent {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the server domain.
	 */
	public static final String MBEAN_SERVER_DOMAIN = "ServerAPI";

	/**
	 * A path to the connector in the JNDI path.
	 */
	public static final String CONN_JNDI_PATH = "/jmxconnector";

	/**
	 * Represents the local session.
	 */
	private static com.ibm.di.api.local.Session mLocalSession = null;

	/**
	 * {@link MBeanServer} instance.
	 */
	private static MBeanServer mMBeanServer = null;

	/**
	 * Initialization flag.
	 */
	private static boolean mIsInitialized = false;

	/**
	 * Checks security rights.
	 */
	private static com.ibm.di.api.local.SecurityRegistry mSecRegistry = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Initializes the JMX layer of the Server API. This method creates MBean
	 * server and registers as mbeans the system log, security register, system
	 * queue, tombstone manager etc.
	 * 
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is already initialized.
	 */
	public static void initialize() throws DIException {
		if (mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.ALREADY.INITIALIZED"));
		}

		try {
			mLocalSession = com.ibm.di.api.APIEngine.getLocalSession();
			mMBeanServer = MBeanServerFactory
					.createMBeanServer(MBEAN_SERVER_DOMAIN);
			mIsInitialized = true;

			JMXServerAPIListener.intializeListener(mLocalSession);

			ServerInfo si = new ServerInfo(mLocalSession.getServerInfo());
			registerMBean(si);

			SystemLog sysLog = new SystemLog(mLocalSession.getSystemLog());
			registerMBean(sysLog);

			SecurityRegistry secRegistry = new SecurityRegistry(mLocalSession
					.getSecurityRegistry());
			registerMBean(secRegistry);

			com.ibm.di.api.local.SystemQueue systemQueue = null;
			try {
				systemQueue = mLocalSession.getSystemQueue();
				registerMBean(new SystemQueue(systemQueue));
			} catch (Exception ex) {
				// This is a normal situation when the System Queue is not
				// started
			}

			// Register TombstoneManager MBean if needed
			if (APIEngine.getTombstoneManager() != null) {
				TombstoneManager tm = new TombstoneManager(mLocalSession
						.getTombstoneManager());
				registerMBean(tm);
			}

			DIServer diServer = new DIServer(mLocalSession);
			registerMBean(diServer);

			initializeAndRegisterNotificationService();

			mSecRegistry = mLocalSession.getSecurityRegistry();

			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString("SEVER.API.SERVER.API.JMX.AGENT.SUCCESSFULLY.INITIALIZED"));
			}
		} catch (Exception e) {
			mIsInitialized = false;
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.INITIALIZE.SERVER.API.JMX.AGENT"),
							e);
		}
	}

	/**
	 * Initializes the remotely exposed JMX layer of the Server API. This method
	 * creates and starts the RMIConnectorServer.
	 * 
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is not initialized.
	 */
	public static void initializeRemote() throws DIException {
		if (!mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.IS.NOT.INITIALIZED"));
		}

		try {
			APIEngine.initRMIRegistry();

			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi://localhost/jndi/rmi://localhost:"
							+ APIEngine.getNamingPort() + CONN_JNDI_PATH);
			Map environment = new HashMap();

			environment.put(
					RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,
					APIEngine.getClientSF());
			environment.put(
					RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,
					APIEngine.getServerSF());

			// Create and start the RMIConnectorServer
			JMXConnectorServer connectorServer = JMXConnectorServerFactory
					.newJMXConnectorServer(url, environment, null);
			ObjectName connectorServerName = ObjectName
					.getInstance("connectors:protocol=" + url.getProtocol());
			mMBeanServer.registerMBean(connectorServer, connectorServerName);
			connectorServer.start();

			String funcmsg = sResHash
					.getString(
							"SEVER.API.JMX.REMOTE.SERVER.CONNECTOR.STARTED.AT.URL",
							url);
			APIEngine.logInfo(funcmsg);
			System.out.println(funcmsg);
		} catch (Exception e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.INITIALIZE.SERVER.API.JMX.REMOTING"),
							e);
		}
	}

	/**
	 * Registers a pre-existing object as an MBean with the MBean server. This
	 * method creates the object name which is passed with the
	 * <code>aBaseAdmin</code> parameter to the
	 * <code>javax.management.MBeanServer.registerMBean(Object, ObjectName)</code>
	 * method.
	 * 
	 * @param aBaseAdmin
	 *            The MBean to be registered as an MBean.
	 * @return an <code>ObjectName</code> of the newly registered MBean.
	 * 
	 * @throws JMException
	 *             <li>if the MBean is already under the control of the MBean
	 *             server.</li>
	 *             <li> if the <code>preRegister</code> (<code>MBeanRegistration</code>
	 *             interface) method of the MBean has thrown an exception. The
	 *             MBean will not be registered.
	 *             <li>if <code>aBaseAdmin</code> is not a JMX compliant
	 *             MBean.</li>
	 *             <li> if <code>aBaseAdmin</code> passed in parameter is null
	 *             or no object name is specified.
	 *             <li>
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is not initialized.
	 */
	public static synchronized ObjectName registerMBean(BaseAdmin aBaseAdmin)
			throws JMException, DIException {
		if (!mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.NOT.INITIALIZED.1"));
		}

		ObjectName objectName = new ObjectName(MBEAN_SERVER_DOMAIN + ":"
				+ aBaseAdmin.getKeyPropertyList());
		mMBeanServer.registerMBean(aBaseAdmin, objectName);
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.JMX.MBEAN.REGISTERED.OBJECTNAME", objectName));
		}

		return objectName;
	}

	/**
	 * Unregisters an MBean from the MBean server. The MBean is identified by
	 * its object name. Once the method has been invoked, the MBean may no
	 * longer be accessed by its object name.
	 * 
	 * @param aObjectName
	 *            The object name of the MBean to be unregistered.
	 * 
	 * @throws JMException
	 *             <li>if the MBean specified is not registered in the MBean
	 *             server.</li>
	 *             <li>if the preDeregister ((<CODE>MBeanRegistration</CODE>
	 *             interface) method of the MBean has thrown an exception. </li>
	 *             <li> if <code>aObjectName</code> in parameter is null or
	 *             the MBean you are when trying to unregister is the
	 *             {@link javax.management.MBeanServerDelegate
	 *             MBeanServerDelegate} MBean.</li>
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is not initialized.
	 */
	public static synchronized void unregisterMBean(ObjectName aObjectName)
			throws JMException, DIException {
		if (!mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.NOT.INITIALIZED.2"));
		}

		mMBeanServer.unregisterMBean(aObjectName);
		if (APIEngine.isDebugEnabled()) {
			APIEngine
					.logDebug(sResHash.getString(
							"SEVER.API.JMX.MBEAN.UNREGISTERED.OBJECTNAME",
							aObjectName));
		}
	}

	/**
	 * Returns the security register of the local session.
	 * 
	 * @return the security register
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is not initialized.
	 */
	public static com.ibm.di.api.local.SecurityRegistry getSecRegistry()
			throws DIException {
		if (!mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.NOT.INITIALIZED.3"));
		}

		return mSecRegistry;
	}

	/**
	 * Returns the created MBean server.
	 * 
	 * @return the created MBean server
	 * @throws DIException
	 *             if the <code>JMXAgent</code> is not initialized.
	 */
	public static MBeanServer getMBeanServer() throws DIException {
		if (!mIsInitialized) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.SERVER.API.JMX.AGENT.NOT.INITIALIZED.4"));
		}

		return mMBeanServer;
	}

	/**
	 * Initialize and register <code>Notifier</code>
	 * 
	 * @throws DIException
	 *             <li>if the <code>JMXAgent</code> is not initialized.</li>
	 *             <li>if could not initialize and/or register notification
	 *             service.</li>
	 */
	private static void initializeAndRegisterNotificationService()
			throws DIException {
		try {
			Notifier notifier = Notifier.init();

			mMBeanServer.registerMBean(notifier, notifier.getObjectName());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.COULD.NOT.INITIALIZE.REGISTER.NOTIFICATION.SERVICE",
									e.toString()));
		}
	}
}
