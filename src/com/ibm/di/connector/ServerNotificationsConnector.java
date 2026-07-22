/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.SessionFactory;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;

/**
 * The Connector listens for local or remote Server API notifications depending
 * on the mode selected by setting a Connector parameter. In remote mode the
 * Connector hooks into remote TDI systems and registers for notifications. In
 * local mode the Connector registers for Server API notifications emitted in
 * the TDI JVM - the notifications can be emitted by the Server API layer. When
 * the Connector receives a notification it stores it into an internal Connector
 * buffer for later retrieval by the standard getNextEntry() Connector method
 * which is called by the AssemblyLine for Connectors in Iterator mode.
 */
public class ServerNotificationsConnector extends Connector implements
		ConnectorInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Object used for access of the TMS messages
	 */
	private ResourceHash mResHash;

	/**
	 * TMS Filename used in the Connector for error and debug messages
	 */
	private final static String TMS_FILE_NAME = "srvnotifconnector";

	/**
	 * Parameter name in the configuration for connectionType
	 */
	private final static String PARAM_CONNECTION_TYPE = "connectionType";

	/**
	 * Value of the "connectionType" Connector parameter when the connectionType
	 * chosen is remote
	 */
	private final static String REMOTE_MODE = "remote";

	/**
	 * Value of the "connectionType" Connector parameter when the connectionType
	 * chosen is local
	 */
	private final static String LOCAL_MODE = "local";

	/**
	 * Parameter name in the configuration for url.
	 */
	private final static String PARAM_URL = "url";

	/**
	 * Parameter name in the configuration for username.
	 */
	private final static String PARAM_USERNAME = "username";

	/**
	 * Parameter name in the configuration for password.
	 */
	private final static String PARAM_PASSWORD = "password";

	/**
	 * Parameter name in the configuration for configInstanceId.
	 */
	private final static String PARAM_CONFIG_INSTANCE_ID = "configInstanceId";

	/**
	 * Parameter name in the configuration for notificationId.
	 */
	private final static String PARAM_NOTIFICATION_ID = "notificationId";

	/**
	 * Parameter name in the configuration for timeOut.
	 */
	private final static String PARAM_TIMEOUT = "timeOut";

	/**
	 * Parameter name in the configuration for di_all.
	 */
	private final static String PARAM_DI_ALL = "di_all";

	/**
	 * Parameter name in the configuration for di_ci_all.
	 */
	private final static String PARAM_DI_CI_ALL = "di_ci_all";

	/**
	 * Parameter name in the configuration for di_ci_start.
	 */
	private final static String PARAM_DI_CI_START = "di_ci_start";

	/**
	 * Parameter name in the configuration for di_ci_stop.
	 */
	private final static String PARAM_DI_CI_STOP = "di_ci_stop";

	/**
	 * Parameter name in the configuration for di_ci_file_updated.
	 */
	private final static String PARAM_DI_CI_FILE_UPDATED = "di_ci_file_updated";

	/**
	 * Parameter name in the configuration for di_al_all.
	 */
	private final static String PARAM_DI_AL_ALL = "di_al_all";

	/**
	 * Parameter name in the configuration for di_al_start.
	 */
	private final static String PARAM_DI_AL_START = "di_al_start";

	/**
	 * Parameter name in the configuration for di_al_stop.
	 */
	private final static String PARAM_DI_AL_STOP = "di_al_stop";

	/**
	 * Parameter name in the configuration for di_server_stop.
	 */
	private final static String PARAM_DI_SERVER_STOP = "di_server_stop";

	/**
	 * Parameter name in the configuration for hasCustomNotifications.
	 */
	private final static String PARAM_HAS_CUSTOM_NOTIFICATIONS = "hasCustomNotifications";

	/**
	 * Parameter name in the configuration for customNotifications.
	 */
	private final static String PARAM_CUSTOM_NOTIFICATIONS = "customNotifications";

	/**
	 * The ObjectMutex used for wait and notify AL thread
	 */
	private final Object mObjMutex = new Object();

	/**
	 * The chosen mode of the Connector
	 */
	private int mConnectorMode = ServerConstants.TYPE_ITERATOR;

	/**
	 * The remote or local session. The connection to the Server API layer
	 */
	private Object mSession;

	/**
	 * Buffer for received notifications
	 */
	private List<DIEvent> mEventsBufferList;

	/**
	 * The remote or local DIEventListener that listen for notifications
	 */
	private Object mListener;

	/**
	 * isLocal used in Connector
	 */
	private boolean mIsLocal;

	/**
	 * configInstanceId used in Connector
	 */
	private String mConfigInstanceId;

	/**
	 * timeOut used in Connector
	 */
	private int mTimeOut;

	/**
	 * typeFilter builded and used in Connector
	 */
	private String mTypeFilter;

	/**
	 * Constructor for the ServerNotificationListenerConnector object
	 */
	public ServerNotificationsConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE, });
	}

	/**
	 * Reads connector parameter's values and initialize the Connector.
	 * 
	 * @param aObj
	 *            Null, Socket or ConnectorMode class
	 * 
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object aObj) throws Exception {
		mResHash = new ResourceHash(TMS_FILE_NAME);
		String modeStr = (String) getParam(PARAM_CONNECTION_TYPE);
		if (modeStr == null || modeStr.trim().length() == 0) {
			throw new Exception(mResHash
					.getString("INVALID.PARAMETER.CONNECTION.TYPE"));
		}
		if (LOCAL_MODE.equalsIgnoreCase(modeStr)) {
			mIsLocal = true;
		} else if (REMOTE_MODE.equalsIgnoreCase(modeStr)) {
			mIsLocal = false;
		} else {
			throw new Exception(mResHash.getString("INVALID.PARAMETER.VALUE",
					modeStr));
		}

		mSession = null;

		if (mIsLocal) {
			mSession = com.ibm.di.api.APIEngine.getLocalSession();
		} else {
			String url = (String) getParam(PARAM_URL);
			if (url == null || url.trim().length() == 0) {
				throw new Exception(mResHash.getString("INVALID.PARAMETER.URL"));
			}
			String username = (String) getParam(PARAM_USERNAME);
			if (username == null || username.trim().length() == 0) {
				username = null;
			}

			String password = (String) getParam(PARAM_PASSWORD);
			if (password == null || password.trim().length() == 0) {
				password = null;
			}

			SessionFactory sessionFactory = (SessionFactory) Naming.lookup(url);

			if (username == null) {
				mSession = sessionFactory.createSession();
			} else {
				mSession = sessionFactory.createSession(username, password);
			}
		}
		if (aObj instanceof ConnectorMode) {
			mConnectorMode = ((ConnectorMode) aObj).getMode();
			if (mConnectorMode == ServerConstants.TYPE_ITERATOR) {

				String notificationId = getParam(PARAM_NOTIFICATION_ID);
				if (notificationId == null
						|| notificationId.trim().length() == 0) {
					notificationId = null;
				} else {
					notificationId = notificationId.trim();
				}
				mConfigInstanceId = getParam(PARAM_CONFIG_INSTANCE_ID);
				if (mConfigInstanceId == null
						|| mConfigInstanceId.trim().length() == 0) {
					mConfigInstanceId = null;
				} else {
					mConfigInstanceId = mConfigInstanceId.trim();
				}

				buildTypeFilter();

				mEventsBufferList = Collections
						.synchronizedList(new LinkedList<DIEvent>());

				createListener();
				if (mIsLocal) {
					((com.ibm.di.api.local.Session) mSession).addEventListener(
							(com.ibm.di.api.local.DIEventListener) mListener,
							mTypeFilter, notificationId);
				} else {
					((com.ibm.di.api.remote.Session) mSession)
							.addEventListener(
									(com.ibm.di.api.remote.DIEventListener) mListener,
									mTypeFilter, notificationId);
				}
				String timeOutStr = getParam(PARAM_TIMEOUT);
				try {
					mTimeOut = Integer.valueOf(timeOutStr).intValue();
				} catch (NumberFormatException e) {
					mTimeOut = 0;
				}
				mTimeOut *= 1000;
			}
		}
	}

	/**
	 * Creates remote or local DIEventListener that waits for notifications
	 * 
	 * @throws RemoteException
	 * @throws Exception
	 */
	private void createListener() throws RemoteException, Exception {
		EventListener el;
		if (mIsLocal) {
			el = new com.ibm.di.api.local.DIEventListener() {
				public void handleEvent(DIEvent aEvent) throws DIException {
					handleDIEvent(aEvent);
				}
			};
			mListener = (com.ibm.di.api.local.DIEventListener) el;
		} else {
			el = new com.ibm.di.api.remote.DIEventListener() {
				public void handleEvent(DIEvent aEvent) throws DIException,
						RemoteException {
					handleDIEvent(aEvent);
				}
			};
			mListener = com.ibm.di.api.remote.impl.DIEventListenerBase
					.createInstance((com.ibm.di.api.remote.DIEventListener) el,
							((com.ibm.di.api.remote.Session) mSession)
									.isSSLon());
		}
	}

	/**
	 * Called when a event occurs.
	 * 
	 * @param aEvent
	 *            The event object
	 * @throws DIException
	 *             never
	 */
	private void handleDIEvent(DIEvent aEvent) throws DIException {
		if (mConfigInstanceId == null
				|| mConfigInstanceId.equals(aEvent.getConfigInstanceId())) {

			if (debugMode()) {
				debug(mResHash.getString("EVENT.RECEIVED", aEvent));
			}
			synchronized (mEventsBufferList) {
				mEventsBufferList.add(aEvent);
			}
			wakeUp();
		}
	}

	/**
	 * Build notification filter for notification types, specified by user
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	private void buildTypeFilter() throws Exception {
		mTypeFilter = "";

		if (!addFilter(PARAM_DI_ALL, "di.*")) {
			if (!addFilter(PARAM_DI_CI_ALL, "di.ci.*")) {
				addFilter(PARAM_DI_CI_START, DIEvent.EVT_CI_START);
				addFilter(PARAM_DI_CI_STOP, DIEvent.EVT_CI_STOP);
				addFilter(PARAM_DI_CI_FILE_UPDATED, DIEvent.EVT_CI_UPDATED);
			}
			if (!addFilter(PARAM_DI_AL_ALL, "di.al.*")) {
				addFilter(PARAM_DI_AL_START, DIEvent.EVT_AL_START);
				addFilter(PARAM_DI_AL_STOP, DIEvent.EVT_AL_STOP);
			}
			addFilter(PARAM_DI_SERVER_STOP, DIEvent.EVT_SRV_STOP);
		}

		String customNotifications = getParam(PARAM_CUSTOM_NOTIFICATIONS);
		addFilter(PARAM_HAS_CUSTOM_NOTIFICATIONS, customNotifications);
		if (mTypeFilter.endsWith(";")) {
			mTypeFilter = mTypeFilter.substring(0, mTypeFilter.length() - 1);
		}
	}

	/**
	 * Add notification type to the notification filter
	 * 
	 * @param aParam
	 *            The Connector checkbox/parameter name that specifies whether
	 *            to add filter value to the filter or not
	 * @param aValue
	 *            The type of notification that must be added in the filter
	 * @return whether to add filter value to the filter or not
	 */
	private boolean addFilter(String aParam, String aValue) {
		boolean paramValue = getBoolParam(aParam);

		if ((paramValue) && (aValue != null) && (aValue.trim().length() != 0)) {
			mTypeFilter += (aValue.trim() + ";");
		}
		return paramValue;
	}

	/**
	 * Check if the Connector checkbox/parameter specified by user is true or
	 * false.
	 * 
	 * @param aParam
	 *            The Connector checkbox/parameter name
	 * 
	 * @return true if the Connector checkbox/parameter is checked
	 */
	private boolean getBoolParam(String aParam) {
		String paramStr = getParam(aParam);
		boolean returnVal = false;
		if (paramStr != null) {
			returnVal = Boolean.valueOf(paramStr).booleanValue();
		}
		return returnVal;
	}

	/**
	 * Remove NotificationListener that waits for notifications.
	 * 
	 * @throws DIException
	 * @throws RemoteException
	 */
	private void removeListener() throws DIException, RemoteException {
		if (mSession != null) {
			if (mIsLocal) {
				((com.ibm.di.api.local.Session) mSession)
						.removeEventListener((com.ibm.di.api.local.DIEventListener) mListener);
			} else {
				((com.ibm.di.api.remote.Session) mSession)
						.removeEventListener((com.ibm.di.api.remote.DIEventListener) mListener);
			}
		}
	}

	/**
	 * Sleep until notification received.
	 * 
	 * @return if false AL is interrupted
	 */
	private boolean waitForNotification() {
		boolean result = true;
		synchronized (mObjMutex) {
			try {
				mObjMutex.wait(mTimeOut);
			} catch (InterruptedException e) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Wake up the thread. Notification is received.
	 */
	private void wakeUp() {
		synchronized (mObjMutex) {
			mObjMutex.notify();
		}
	}

	/**
	 * Gets the next notification object
	 * 
	 * @return The next Entry
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		boolean result = true;
		if (mEventsBufferList.size() < 1) {
			result = waitForNotification();
		}
		Entry e = null;
		if (result) {
			synchronized (mEventsBufferList) {
				if (mEventsBufferList.size() > 0) {
					DIEvent event = (DIEvent) mEventsBufferList.remove(0);
					e = event2entry(event);
				}
			}
		}
		return e;
	}

	/**
	 * Emits a custom Server API notification
	 * 
	 * @param entry
	 *            The entry that holds the notification data
	 * @exception Exception
	 *                If emiting notification fails.
	 */
	public void putEntry(Entry entry) throws Exception {
		Attribute typeAttr = entry.getAttribute("event.type");
		if (typeAttr == null) {
			throw new Exception(mResHash
					.getString("ATTRIBUTE_EVENT_TYPE_REQUIRED"));
		}
		String type = typeAttr.getValue();

		Attribute idAttr = entry.getAttribute("event.id");
		if (idAttr == null) {
			throw new Exception(mResHash
					.getString("ATTRIBUTE_EVENT_ID_REQUIRED"));
		}
		String id = idAttr.getValue();

		Attribute dataAttr = entry.getAttribute("event.userData");
		Object data = null;
		if (dataAttr != null) {
			data = dataAttr.getValue();
		}

		if (mIsLocal) {
			((com.ibm.di.api.local.Session) mSession).sendCustomNotification(
					type, id, data);
		} else {
			((com.ibm.di.api.remote.Session) mSession).sendCustomNotification(
					type, id, data);
		}
	}

	/**
	 * Sets all needed attributes in the Entry.
	 * 
	 * @param aEvent
	 *            DIEvent object
	 * @return The Entry
	 */
	private Entry event2entry(DIEvent aEvent) {
		if (aEvent == null) {
			return null;
		}
		Entry entry = new Entry();

		entry.setAttribute("event.rawNotification", aEvent);
		entry.setAttribute("event.type", aEvent.getType());
		entry.setAttribute("event.id", aEvent.getId());
		if (aEvent.getData() instanceof Entry) {
			Entry userData = (Entry) aEvent.getData();
			String[] attNames = userData.getAttributeNames();
			for (int i = 0; i < attNames.length; i++) {
				entry.setAttribute("event.userData." + attNames[i], userData
						.getObject(attNames[i]));
			}
		}
		entry.setAttribute("event.userData", aEvent.getData());
		entry.setAttribute("event.configInstanceId", aEvent
				.getConfigInstanceId());
		entry.setAttribute("event.dateCreated", aEvent.getDateCreated());
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
		if (mConnectorMode == ServerConstants.TYPE_ITERATOR) {
			removeListener();
		}
	}

	/**
	 * Version information.
	 * @return the version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
}
