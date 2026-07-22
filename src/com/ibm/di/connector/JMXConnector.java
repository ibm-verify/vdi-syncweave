/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * The JMX Connector listens for local or remote JMX notifications depending on
 * the mode selected by setting a Connector parameter. In remote mode the
 * Connector hooks into remote JMX systems and registers for notifications. In
 * local mode the Connector registers for JMX notifications emitted in the TDI
 * JVM - normally such notifications can be emitted by the JMX layer of the
 * Server API or by other TDI components. When the Connector receives a
 * notification it stores it into an internal Connector buffer for later
 * retrieval by the standard getNextEntry() Connector method which is called by
 * the AssemblyLine for Connectors in Iterator mode.
 */
public class JMXConnector extends Connector implements ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "jmxconnector";

	/**
	 * The delimiters for parsing notification and MBean types
	 */
	private final static String PARAM_DELIMITER = "\r\n\t ;";

	/**
	 * NotificationBroadcaster Full className
	 */
	private final static String JMX_NOTIFICATION_BROADCASTER_CLASSNAME = "javax.management.NotificationBroadcaster";

	/**
	 * Default value for system property "java.naming.factory.initial"
	 */
	private final static String INITIAL_FACTORY = "com.sun.jndi.rmi.registry.RegistryContextFactory";

	/**
	 * Parameter name in the configuration for mode
	 */
	private final static String PARAM_JMX_MODE = "mode";

	/**
	 * Value of the "mode" Connector parameter when the mode chosen is remote
	 * mode
	 */
	private final static String REMOTE_MODE = "remote";

	/**
	 * Value of the "mode" Connector parameter when the mode chosen is local
	 * mode
	 */
	private final static String LOCAL_MODE = "local";

	/**
	 * Parameter name in the configuration for urlPath.
	 */
	private final static String PARAM_JMX_URL = "url";

	/**
	 * Parameter name in the configuration for objectName.
	 */
	private final static String PARAM_JMX_ALLMBEANS = "allMBeans";

	/**
	 * Parameter name in the configuration for objectName.
	 */
	private final static String PARAM_MBEANS_TYPES = "mBeanTypes";

	/**
	 * Parameter name in the configuration for eventTypes.
	 */
	private final static String PARAM_EVENT_TYPES = "eventTypes";

	/**
	 * The ObjectMutex used for wait and notify AL thread
	 */
	private static final Object mObjMutex = new Object();

	/**
	 * The MBeanServerConnection connection to JMX Server receiving
	 * notifications
	 */
	private MBeanServerConnection mMBeanServer;

	/**
	 * Buffer for received notifications
	 */
	private List<Notification> mNotificationsList;

	/**
	 * The NotificationListener that listen for notifications
	 */
	private NotificationListener mListener;

	/**
	 * All registered listeners for notifications
	 */
	private ArrayList<ObjectName> mRegisteredListeners = new ArrayList<ObjectName>();

	/**
	 * mBeansTypes used in Connector
	 */
	private String mMBeanTypes;

	/**
	 * mEventTypes used in Connector
	 */
	private String mEventTypes;

	/**
	 * allMBeans used in Connector
	 */
	private boolean mAllMBeans;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor for the JMXConnector object
	 */
	public JMXConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Reads connector parameter's values and initialize the Connector.
	 * 
	 * @param aObj
	 *            Null, Socket or ConnectorMode class
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object aObj) throws Exception {
		String modeStr = (String) getParam(PARAM_JMX_MODE);
		if (modeStr == null || modeStr.trim().length() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JMX.MISSING.MODE.EXCEP"));
		}
		boolean isLocal;

		if (LOCAL_MODE.equalsIgnoreCase(modeStr)) {
			isLocal = true;
		} else if (REMOTE_MODE.equalsIgnoreCase(modeStr)) {
			isLocal = false;
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.JMX.MODEBAD.EXCEP", modeStr));
		}

		mNotificationsList = Collections
				.synchronizedList(new ArrayList<Notification>());
		String allMBeansStr = getParam(PARAM_JMX_ALLMBEANS);
		mAllMBeans = false;
		if (allMBeansStr != null) {
			mAllMBeans = Boolean.valueOf(allMBeansStr).booleanValue();
		}

		if (!mAllMBeans) {
			mMBeanTypes = (String) getParam(PARAM_MBEANS_TYPES);
			if (mMBeanTypes == null || mMBeanTypes.trim().length() == 0) {
				throw new Exception(sResHash
						.getString("CONNECTOR.JMX.MISSING.MBEANTYPES.EXCEP"));
			}
		}
		mEventTypes = (String) getParam(PARAM_EVENT_TYPES);

		if (isLocal) {
			mMBeanServer = JMXAgent.getMBeanServer();
		} else {
			String urlStr = (String) getParam(PARAM_JMX_URL);
			if (urlStr == null || urlStr.trim().length() == 0) {
				throw new Exception(sResHash
						.getString("CONNECTOR.JMX.MISSING.URL.EXCEP"));
			}

			// The address of the connector server
			JMXServiceURL url = new JMXServiceURL(urlStr);

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_FACTORY);

			javax.management.remote.JMXConnector cntor = JMXConnectorFactory
					.connect(url, env);
			mMBeanServer = cntor.getMBeanServerConnection();
		}
		NotificationFilter filter = buildFilter();
		registerForNotifications(filter);
	}

	/**
	 * Creates NotificationListener that waits for notifications
	 */
	private void createListener() {
		mListener = new NotificationListener() {

			/**
			 * Called when a notification occurs.
			 * 
			 * @param aNotification
			 *            The notification object
			 * @param aHandback
			 *            Helps in associating information regarding the
			 *            listener.
			 */
			public void handleNotification(Notification aNotification,
					Object aHandback) {

				synchronized (mObjMutex) {
					mNotificationsList.add(aNotification);
					mObjMutex.notify();
				}
			}
		};
	}

	/**
	 * Registers NotificationListener for all MBeans
	 * 
	 * @param aFilter
	 *            The NotificationFilter for notification types that will be
	 *            processed.
	 * @throws Exception
	 *             if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	private void registerForNotifications(NotificationFilter aFilter)
			throws Exception {

		createListener();
		if (mAllMBeans) {
			for (ObjectName on : (Set<ObjectName>) mMBeanServer.queryNames(
					null, null))
				if (isNotificationBroadcaster(on))
					addListener(on, aFilter);
		} else {
			ObjectName objectName = null;
			for (String sn : createList(mMBeanTypes)) {
				objectName = new ObjectName(sn);
				if (isNotificationBroadcaster(objectName)) {
					addListener(objectName, aFilter);
				}
			}
		}
	}

	/**
	 * Add NotificationListener that waits for notifications
	 * 
	 * @param aObjectName
	 *            The ObjectName of the source MBean on which the listener
	 *            should be added.
	 * @param aFilter
	 *            The NotificationFilter for notification types that will be
	 *            processed.
	 */
	private void addListener(ObjectName aObjectName, NotificationFilter aFilter) {
		if (aObjectName == null) {
			return;
		}
		try {
			mMBeanServer.addNotificationListener(aObjectName, mListener,
					aFilter, null);
			mRegisteredListeners.add(aObjectName);
		} catch (InstanceNotFoundException e) {
			logmsg(sResHash.getString("CONNECTOR.JMX.MBEAN.NOT.FOUND",
					aObjectName));
		} catch (IOException e) {
			logmsg(sResHash.getString("CONNECTOR.JMX.COMMUNICATION.PROBLEM", e));
		}
	}

	/**
	 * Checks if ObjectName is instance of NotificationBroadcaster
	 * 
	 * @param aObjectName
	 *            The ObjectName
	 * @return <code>true</code> if instance of NotificationBroadcaster
	 */
	private boolean isNotificationBroadcaster(ObjectName aObjectName) {
		boolean broadcaster = false;
		try {
			if (mMBeanServer.isInstanceOf(aObjectName,
					JMX_NOTIFICATION_BROADCASTER_CLASSNAME)) {
				broadcaster = true;
			}
		} catch (InstanceNotFoundException e) {
			logmsg(sResHash.getString("CONNECTOR.JMX.MBEAN.NOT.FOUND.IS",
					aObjectName));
		} catch (IOException e) {
			logmsg(sResHash.getString("CONNECTOR.JMX.COMMUNICATION.PROBLEM.IS",
					e));
		}
		return broadcaster;
	}

	/**
	 * Build notification filter for notification types, specified by user
	 * 
	 * @return The Filter
	 */
	private NotificationFilter buildFilter() {
		NotificationFilterSupport filter = new NotificationFilterSupport();
		List<String> notificationTypes = createList(mEventTypes);
		if (notificationTypes != null) {
			Iterator<String> iter = notificationTypes.iterator();
			while (iter.hasNext()) {
				filter.enableType(iter.next());
			}
		} else {
			filter = null;
		}
		return filter;
	}

	/**
	 * Create List from specified parameter
	 * 
	 * @param aParamTypes
	 *            String that will be used to get values from
	 * 
	 * @return The List
	 */
	private List<String> createList(String aParamTypes) {
		ArrayList<String> result = new ArrayList<String>();
		if (aParamTypes != null && aParamTypes.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(aParamTypes,
					PARAM_DELIMITER);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (!result.contains(token)) {
					result.add(token);
				}
			}
		} else {
			result = null;
		}
		return result;
	}

	/**
	 * Remove NotificationListener that waits for notifications
	 */
	private void removeListeners() {
		Iterator<ObjectName> it = mRegisteredListeners.iterator();
		while (it.hasNext()) {
			ObjectName aObjectName = it.next();
			try {
				mMBeanServer.removeNotificationListener(aObjectName, mListener);
				logmsg(sResHash.getString("CONNECTOR.JMX.REMOVE.LISTENER",
						aObjectName));
			} catch (InstanceNotFoundException e) {
				logmsg(sResHash.getString(
						"CONNECTOR.JMX.MBEAN.NOT.FOUND.REMOVE", aObjectName));
			} catch (ListenerNotFoundException e) {
				logmsg(sResHash.getString(
						"CONNECTOR.JMX.LISTENER.NOT.FOUND.REMOVE", aObjectName));
			} catch (IOException e) {
				logmsg(sResHash.getString(
						"CONNECTOR.JMX.COMMUNICATION.PROBLEM.REMOVE", e
								.getMessage()));
			}
		}
	}

	/**
	 * Sleep until notification received
	 * 
	 * @return if false AL is interrupted
	 */
	private boolean waitForNotification() {
		boolean result = true;
		synchronized (mObjMutex) {
			try {
				while (mNotificationsList.size() < 1)
					mObjMutex.wait();
			} catch (InterruptedException e) {
				result = false;
			}
		}
		return result;
	}

	/*
	 * Wake up the thread. Notification is received.
	 */
	// private void wakeUp() {
	// this method was used incorrectly its execution block was moved in the
	// handleNotification(Notification, Object) method.
	// }
	/**
	 * Gets the next notification object
	 * 
	 * @return The next Entry
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		boolean result = true;

		result = waitForNotification();

		Entry e = null;
		if (result) {
			synchronized (mNotificationsList) {
				if (mNotificationsList.size() > 0) {
					Notification notif = (Notification) mNotificationsList
							.remove(0);
					e = notification2entry(notif);
				}
			}
		}
		return e;
	}

	/**
	 * Sets all needed attributes in the Entry.
	 * 
	 * @param aNotification
	 *            Notification object
	 * 
	 * @return The Entry
	 */
	private Entry notification2entry(Notification aNotification) {
		if (aNotification == null) {
			return null;
		}
		String type = aNotification.getType();
		logmsg(sResHash.getString("CONNECTOR.JMX.NOTIFICATION.RECEIVED",
				new Object[] { type,
						String.valueOf(aNotification.getSequenceNumber()) }));

		Entry entry = new Entry();
		entry.setAttribute("event.originator", this);
		entry.setAttribute("event.type", type);
		entry.setAttribute("event.rawNotification", aNotification);
		entry.setAttribute("event.timestamp", Long.valueOf(aNotification
				.getTimeStamp()));
		entry.setAttribute("event.sequenceNumber", Long.valueOf(aNotification
				.getSequenceNumber()));
		entry.setAttribute("event.message", aNotification.getMessage());

		entry.setAttribute("event.mbean.objectName", aNotification.getSource());
		entry.setAttribute("event.mbean.name", aNotification.getSource()
				.toString());

		Object userData = aNotification.getUserData();
		if (userData != null) {
			entry.setAttribute("event.userData", userData);
		}
		Object source = aNotification.getSource();
		if (source != null) {
			entry.setAttribute("event.source", source);
		}
		return entry;
	}

	/**
	 * Terminate the connector. This function closes all connection and releases
	 * all resources used by the connector. This function also calls the
	 * parser's closeParser function if a parser is active.
	 * 
	 * @throws Exception
	 *             If terminate fails.
	 */
	public void terminate() throws Exception {
		super.terminate();
		removeListeners();
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
