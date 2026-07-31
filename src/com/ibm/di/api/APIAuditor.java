/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This is the class responsible for auditing auth* events in the Server API. It
 * defines the names of the Attributes for every Entry, which is created to
 * store the relevant information for each audited event. The class provides
 * methods for storing context specific audit information.
 * 
 * @since 7.0
 */

public class APIAuditor {

	/**
	 * Defines the name of the attribute specifying the type of audited event.
	 */
	public static final String AUDIT_TYPE = "eventType";

	/**
	 * Specify the name of the field representing the audited class by the
	 * authorization events.
	 */
	public static final String AUDIT_AUTHORIZATION_CLASS = "authorizationClass";

	/**
	 * Specify the name of the field representing the audited method by the
	 * authorization events.
	 */
	public static final String AUDIT_AUTHORIZATION_METHOD = "authorizationMethod";

	/**
	 * Define the prefix used for authentication events.
	 */
	public static final String EVT_AUDIT_AUTHENTICATE = "di.server.api.authenticate";

	/**
	 * Define the prefix used for authorization events.
	 */
	public static final String EVT_AUDIT_AUTHORIZATION_PREFIX = "di.server.api.authorize.";

	/**
	 * Defines the ID of each event. By authentication events this is the type
	 * of authentication. By authorization ones, it is the same as the TDI
	 * Object name, on which authorization takes part. The two exceptions are:
	 * custom invocation of java code, where the event id is represented by the
	 * TDI server id and the name of the object is the method name; deleting
	 * tombstones using GUID - the event ID is the GUID itself.The value for the
	 * event id could be: TDI Server ID, ConfigInstance ID, Assembly Line name.
	 * If an AL name can be determined for the event, then it is stored,
	 * otherwise a Config Instance ID is used. In case this is also not
	 * possible, then the TDI server ID is provided as event ID.
	 */
	public static final String AUDIT_ID = "eventID";

	/**
	 * Defines if possible the ConfigInstanceID on which the event has occurred.
	 */
	public static final String AUDIT_CONFIG = "eventConfigInstanceId";

	/**
	 * Defines the date on which the event has occurred.
	 */
	public static final String AUDIT_DATE = "eventDate";

	/**
	 * Defines the name of the TDI user, who has caused the event.
	 */
	public static final String AUDIT_LOGONNAME = "logonname";

	/**
	 * Defines the result of the auth* event.
	 */
	public static final String AUDIT_SUCCESS = "success";

	/**
	 * Define the platform name on which the TDI server is running.
	 */
	public static final String AUDIT_SERVER_PLATFORM = "os.name";

	/**
	 * Define the platform version on which the TDI server is running.
	 */
	public static final String AUDIT_SERVER_PLATFORM_VERSION = "os.version";

	/**
	 * Defines the host name on which the TDI server is running.
	 */
	public static final String AUDIT_HOSTNAME = "hostname";

	/**
	 * Defines the IP address of the client machine.
	 */
	public static final String AUDIT_IP = "clientIP";

	/**
	 * Defines the Session type.
	 */
	public static final String AUDIT_SESSIONTYPE = "session.type";

	/**
	 * Defines the name of the Attribute filled in with the physical path to the
	 * TDI instance (e.g. path to the config file).
	 */
	public static final String AUDIT_PATH = "path";

	/**
	 * Defines the name of the Attribute filled in with the name of the TDI
	 * object on which the event takes place. The value could contain: the TDI
	 * Server name, the ConfigInstance ID, or the AssemblyLine name). Two
	 * special cases are the invocation of custom java code, where the name of
	 * the method is passed as value, and the deletion of tombstones using GUID.
	 * In this case the parameter stores the GUID.
	 */
	public static final String AUDIT_NAME = "name";

	/**
	 * Constant value used to mark the auth* event as successful.
	 */
	public static final String AUDIT_MESSAGE_SUCCESS = "true";

	/**
	 * Constant value used to mark the auth* event as failed.
	 */
	public static final String AUDIT_MESSAGE_FAILURE = "false";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_NO} = {@value #AUDIT_AUTH_ID_NO}
	 */
	public static final String AUDIT_AUTH_ID_NO = "default";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_CUSTOM} = {@value #AUDIT_AUTH_ID_CUSTOM}
	 */
	public static final String AUDIT_AUTH_ID_CUSTOM = "custom";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_LDAP} = {@value #AUDIT_AUTH_ID_LDAP}
	 */
	public static final String AUDIT_AUTH_ID_LDAP = "LDAP";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_SSL} = {@value #AUDIT_AUTH_ID_SSL}
	 */
	public static final String AUDIT_AUTH_ID_SSL = "SSL";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_HOST} = {@value #AUDIT_AUTH_ID_HOST}
	 */
	public static final String AUDIT_AUTH_ID_HOST = "host";

	/**
	 * Constant representing a type of authentication. <br>
	 * {@link #AUDIT_AUTH_ID_JAAS} = {@value #AUDIT_AUTH_ID_JAAS}
	 */
	public static final String AUDIT_AUTH_ID_JAAS = "JAAS";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Name of api.audit.on flag.
	 */
	private final static String PROP_API_AUDIT_ON = "api.audit.on";

	/**
	 * Obtain common information for the platform, on which the TDI server is
	 * running.
	 * 
	 * @return An Entry object containing the necessary information in form of
	 *         Attributes
	 */
	private static Entry getCommonAuditInformation() {
		Entry entry = new Entry();

		String operatingSystem = System.getProperty("os.name");
		String sessionType = null;
		String IP = null;
		String hostName = null;
		if (operatingSystem != null) {
			entry.setAttribute(AUDIT_SERVER_PLATFORM, operatingSystem);
			entry.setAttribute(AUDIT_SERVER_PLATFORM_VERSION, System
					.getProperty("os.version"));
		}

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
		}

		entry.setAttribute(AUDIT_HOSTNAME, hostName);

		try {
			IP = RemoteServer.getClientHost();
			sessionType = "RemoteSession";
		} catch (ServerNotActiveException se) {
			IP = "localhost";
			sessionType = "LocalSession";
		}

		entry.setAttribute(AUDIT_IP, IP);
		entry.setAttribute(AUDIT_SESSIONTYPE, sessionType);

		return entry;
	}

	/**
	 * The method adds specific authentication audit information in a TDI
	 * Entry's Attributes and sends this information as Notification using the
	 * leveraged notification mechanism in TDI. It is called from every
	 * authentication point in the server API code, where auditing is wanted.
	 * 
	 * @param userID
	 *            Defines the name of the TDI user, who has caused the event.
	 * @param isSuccessful
	 *            Defines the result of the auth* event.
	 * @param authenticationType
	 *            Defines the type of authentication.
	 */
	public static void sendAuthenticationAuditData(String userID,
			boolean isSuccessful, String authenticationType) {
		boolean isAuditOn = false;
		if (System.getProperty(PROP_API_AUDIT_ON) != null) {
			isAuditOn = Boolean.getBoolean(PROP_API_AUDIT_ON);
		}
		if (!isAuditOn) {
			return;
		}
		Entry entry = new Entry();

		entry.setAttribute(AUDIT_LOGONNAME, userID);
		entry.setAttribute(AUDIT_TYPE, APIAuditor.EVT_AUDIT_AUTHENTICATE);
		entry.setAttribute(AUDIT_ID, authenticationType);

		entry.setAttribute(AUDIT_DATE, new java.util.Date());

		if (isSuccessful) {
			entry.setAttribute(AUDIT_SUCCESS, AUDIT_MESSAGE_SUCCESS);
		} else {
			entry.setAttribute(AUDIT_SUCCESS, AUDIT_MESSAGE_FAILURE);
		}

		entry.merge(APIAuditor.getCommonAuditInformation());

		try {
			APIEngine.sendNotification(APIAuditor.EVT_AUDIT_AUTHENTICATE,
					authenticationType, entry, null);
		} catch (Exception e) {
			APIEngine.logError(sResHash
					.getString("SEVER.API.AUDIT.SEND.NOTIFICATION"));
		}
	}

	/**
	 * The method adds specific audit information in a TDI Entry's Attributes
	 * and sends this information as Notification using the leveraged
	 * notification mechanism in TDI. It is called from every point in the
	 * server API code, where auditing is wanted.
	 * 
	 * @param userID
	 *            Defines the name of the TDI user, who has caused the event.
	 * @param path
	 *            Stores the physical path to the TDI instance (e.g. path to the
	 *            config file). By invocation of custom java code the name of
	 *            the class is set as path.
	 * @param tdiObjectName
	 *            Presents the name of the TDI object, on which the event takes
	 *            place. The value could be one of the following: TDI Server ID,
	 *            ConfigInstance ID, AL Name or java method (by custom
	 *            invocations).
	 * @param isSuccessful
	 *            Expects the result of the auth* event.
	 * @param authorizationClass
	 *            Defines the interface name for the class, where the
	 *            authorization event takes place.
	 * @param authorizationMethod
	 *            Stores the name of the method, where the authorization takes
	 *            place.
	 * @param eventID
	 *            Specifies an ID to the audit notification. The field is
	 *            similar to the tdiObjectName parameter.
	 * @param eventConfigInstanceId
	 *            Gives the ID of the ConfigInstance, on which the authorization
	 *            event is performed. Takes null, if no such ID can be assigned.
	 */
	public static void sendSessionAuditData(String userID, String path,
			String tdiObjectName, boolean isSuccessful,
			String authorizationClass, String authorizationMethod,
			String eventID, String eventConfigInstanceId) {

		boolean isAuditOn = false;
		if (System.getProperty(PROP_API_AUDIT_ON) != null) {
			isAuditOn = Boolean.getBoolean(PROP_API_AUDIT_ON);
		}
		if (!isAuditOn) {
			return;
		}

		Entry entry = new Entry();
		String eventType = null;

		entry.setAttribute(AUDIT_LOGONNAME, userID);
		entry.setAttribute(AUDIT_PATH, path);
		entry.setAttribute(AUDIT_NAME, tdiObjectName);

		entry.setAttribute(AUDIT_AUTHORIZATION_CLASS, authorizationClass);
		entry.setAttribute(AUDIT_AUTHORIZATION_METHOD, authorizationMethod);

		eventType = EVT_AUDIT_AUTHORIZATION_PREFIX + authorizationClass + "."
				+ authorizationMethod;
		entry.setAttribute(AUDIT_TYPE, eventType);
		entry.setAttribute(AUDIT_ID, eventID);
		entry.setAttribute(AUDIT_CONFIG, eventConfigInstanceId);
		entry.setAttribute(AUDIT_DATE, new java.util.Date());

		if (isSuccessful) {
			entry.setAttribute(AUDIT_SUCCESS, AUDIT_MESSAGE_SUCCESS);
		} else {
			entry.setAttribute(AUDIT_SUCCESS, AUDIT_MESSAGE_FAILURE);
		}

		entry.merge(APIAuditor.getCommonAuditInformation());

		try {
			APIEngine.sendNotification(eventType, eventID, entry,
					eventConfigInstanceId);
		} catch (Exception e) {
			APIEngine.logError(sResHash
					.getString("SEVER.API.AUDIT.SEND.NOTIFICATION"));
		}
	}

}
