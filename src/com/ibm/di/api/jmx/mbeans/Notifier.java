/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx.mbeans;

import java.util.Date;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.JMXAgent;
import com.ibm.di.server.ResourceHash;

/**
 * This class provides methods for sending JMX notifications.
 */
public class Notifier extends NotificationBroadcasterSupport implements
		NotifierMBean {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Type of the MBean.
	 */
	public static final String MBEAN_TYPE = "Notifier";

	/**
	 * ID of the MBean.
	 */
	public static final String MBEAN_ID = "Notifier";

	// notification types
	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for config instance starting.
	 */
	public static final String EVT_CI_START = "di.ci.start";

	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for config instance stopping.
	 */
	public static final String EVT_CI_STOP = "di.ci.stop";

	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for updating of the config instance file.
	 */
	public static final String EVT_CI_UPDATED = "di.ci.file.updated";

	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for assembly line starting.
	 */
	public static final String EVT_AL_START = "di.al.start";

	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for assembly line stopping.
	 */
	public static final String EVT_AL_STOP = "di.al.stop";

	/**
	 * Final variable used for distinguishing the type of the notification.
	 * Indicates notification for server stopping.
	 */
	public static final String EVT_SRV_STOP = "di.server.stop";

	// notification groups
	/**
	 * Final variable used for distinguishing the type of the notification
	 * groups. Indicates notification group containing all notifications.
	 */
	public static final String TYPE_ALL = "di";

	/**
	 * Final variable used for distinguishing the type of the notification
	 * groups. Indicates notification group containing all notifications for
	 * config instances.
	 */
	public static final String TYPE_CONFIG_INSTANCE = "di.ci";

	/**
	 * Final variable used for distinguishing the type of the notification
	 * groups. Indicates notification group containing all notifications for
	 * assembly lines.
	 */
	public static final String TYPE_ASSEMBLY_LINE = "di.al";

	// operational members
	/**
	 * {@link Notifier}
	 */
	private static Notifier mNotifier = null;

	/**
	 * Flag for enabled notification.
	 */
	private boolean mNotificationEnabled = true;

	/**
	 * {@link ObjectName}
	 */
	private ObjectName mObjectName = null;

	/**
	 * Holds information for the sequence number.
	 */
	private long mSequenceNumber = 0;

	/**
	 * Used for locking the sequence number.
	 */
	private Object mSequenceLock = new Object();

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Initializes the Notifier.
	 * 
	 * @return the initialized Notifier.
	 * @throws DIException
	 *             if the JMX notification service is already initialized.
	 */
	public static Notifier init() throws DIException {
		if (mNotifier == null) {
			mNotifier = new Notifier();
		} else {
			throw new DIException(
					sResHash
							.getString("SEVER.API.JMX.NOTIFICATION.SERVICE.ALREADY.INITIALIZED"));
		}

		return mNotifier;
	}

	/**
	 * Returns the Notifier.
	 * 
	 * @return the Notifier object.
	 * @throws DIException
	 *             if the JMX notification service is not initialized.
	 */
	public static Notifier getNotifier() throws DIException {
		if (mNotifier == null) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.JMX.NOTIFICATION.SERVICE.IS.NOT.INITIALIZED"));
		}

		return mNotifier;
	}

	// constructor

	/**
	 * Class constructor.
	 * 
	 * @throws DIException
	 *             if notifier cannot be created.
	 */
	private Notifier() throws DIException {
		try {
			mObjectName = new ObjectName(JMXAgent.MBEAN_SERVER_DOMAIN + ":"
					+ getKeyPropertyList());
		} catch (MalformedObjectNameException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.CREATE.JMX.NOTIFIER.OBJECT.NAME"),
							e);
		}
	}

	/**
	 * Gets the object name of the Notifier.
	 * 
	 * @return the object name
	 */
	public ObjectName getObjectName() {
		return mObjectName;
	}

	// service calls

	/**
	 * Checks if the notification is enabled.
	 * 
	 * @return true if the notification is enabled, false otherwise.
	 */
	public synchronized boolean isEnabled() {
		return mNotificationEnabled;
	}

	/**
	 * Enables the Notifier.
	 */
	public synchronized void enable() {
		mNotificationEnabled = true;
	}

	/**
	 * Disables the Notifier.
	 */
	public synchronized void disable() {
		mNotificationEnabled = false;
	}

	// calls for sending notifications

	/**
	 * Sends notification for assembly line starting.
	 * 
	 * @param aAssemblyLineId
	 *            the assembly line id.
	 */
	public void assemblyLineStarted(String aAssemblyLineId) {
		Notification notification = new Notification(EVT_AL_START, mObjectName,
				getNextSequenceNumber(), "AssemblyLine '" + aAssemblyLineId
						+ "' started.");
		notification.setUserData(aAssemblyLineId);

		sendNotification(notification);
	}

	/**
	 * Sends notification for assembly line stopping.
	 * 
	 * @param aAssemblyLineId
	 *            the assembly line id.
	 */
	public void assemblyLineFinished(String aAssemblyLineId) {
		Notification notification = new Notification(EVT_AL_STOP, mObjectName,
				getNextSequenceNumber(), "AssemblyLine '" + aAssemblyLineId
						+ "' terminated.");
		notification.setUserData(aAssemblyLineId);

		sendNotification(notification);
	}

	/**
	 * Sends notification for config instance starting.
	 * 
	 * @param aConfigId
	 *            the config instance id.
	 */
	public void configInstanceStarted(String aConfigId) {
		Notification notification = new Notification(EVT_CI_START, mObjectName,
				getNextSequenceNumber(), "ConfigInstance '" + aConfigId
						+ "' started.");
		notification.setUserData(aConfigId);

		sendNotification(notification);
	}

	/**
	 * Sends notification for config instance stopping.
	 * 
	 * @param aConfigId
	 *            the config instance id.
	 */
	public void configInstanceStopped(String aConfigId) {
		Notification notification = new Notification(EVT_CI_STOP, mObjectName,
				getNextSequenceNumber(), "ConfigInstance '" + aConfigId
						+ "' stopped.");
		notification.setUserData(aConfigId);

		sendNotification(notification);
	}

	/**
	 * Sends notification for updating the config instance file.
	 * 
	 * @param aConfigId
	 *            the config instance id.
	 */
	public void configInstanceUpdated(String aConfigId) {
		Notification notification = new Notification(EVT_CI_UPDATED,
				mObjectName, getNextSequenceNumber(), "ConfigInstance '"
						+ aConfigId + "' stopped.");
		notification.setUserData(aConfigId);

		sendNotification(notification);
	}

	/**
	 * Sends notification for server stopping.
	 * 
	 * @param aBootTime
	 *            the boot time.
	 */
	public void serverStopped(Date aBootTime) {
		Notification notification = new Notification(EVT_SRV_STOP, mObjectName,
				getNextSequenceNumber(), "Server stopped.");
		notification.setUserData(aBootTime);

		sendNotification(notification);
	}

	/**
	 * Sends custom notification.
	 * 
	 * @param aType
	 *            the type of the notification.
	 * @param aId
	 *            the id of the notification. Used for building the message of
	 *            the notification.
	 * @param aData
	 *            The user data object. It is used for whatever data the
	 *            notification source wishes to communicate to its consumers.
	 */
	public void sendCustomNotification(String aType, String aId, Object aData) {
		Notification notification = new Notification(aType, mObjectName,
				getNextSequenceNumber(), "Custom notification ID: " + aId);
		notification.setUserData(aData);

		sendNotification(notification);
	}

	// MBean attributes

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return MBEAN_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return MBEAN_ID;
	}

	// MBean related

	/**
	 * Returns the MBean key property list. If the MBean type is
	 * <code>Notifier</code> and the id is <code>Notifier</code>, the
	 * result of this method would be
	 * <code>&quot;type=Notifier,id=Notifier&quot;</code>
	 * 
	 * @return string representing the MBean attributes.
	 */
	public String getKeyPropertyList() {
		return "type=" + getType() + ",id=" + getId();
	}

	/**
	 * Sends a notification.
	 * 
	 * @param aNotification
	 *            The notification to send.
	 */
	public synchronized void sendNotification(Notification aNotification) {
		if (mNotificationEnabled) {
			super.sendNotification(aNotification);
		}
	}

	/**
	 * Increments sequence number by 1.
	 * 
	 * @return current sequence number.
	 */
	// private methods
	private long getNextSequenceNumber() {
		synchronized (mSequenceLock) {
			long currentValue = mSequenceNumber;
			mSequenceNumber++;

			return currentValue;
		}
	}

}
