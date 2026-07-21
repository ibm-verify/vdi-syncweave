/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.jmx;

import java.util.Date;

import javax.management.JMException;
import javax.management.ObjectName;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.jmx.mbeans.ConfigInstance;
import com.ibm.di.api.jmx.mbeans.Notifier;
import com.ibm.di.api.jmx.mbeans.TDIProperties;
import com.ibm.di.api.local.AssemblyLine;
import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.api.local.Session;
import com.ibm.di.server.ResourceHash;

/**
 * This is a JMX Listener for Server API events.
 */
public class JMXServerAPIListener implements DIEventListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This is a JMX Listener for Server API events.
	 */
	private static JMXServerAPIListener mServerAPIListener = null;

	/**
	 * Represents the local sesstion.
	 */
	private Session mLocalSession = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Initializes the listener.
	 * 
	 * @param aLocalSession
	 *            the local session
	 * @throws DIException
	 *             if an error occurs while initializing the listener.
	 */
	public static void intializeListener(Session aLocalSession)
			throws DIException {
		if (mServerAPIListener != null) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.JMX.SERVER.API.LISTENER.ALREADY.INITIALIZED"));
		}

		mServerAPIListener = new JMXServerAPIListener(aLocalSession);
	}

	/**
	 * Class constructor.
	 * 
	 * @param aLocalSession
	 *            {@link Session} instance.
	 * @throws DIException
	 *             if session parameter is <code>null</code> or listener
	 *             cannot be appended.
	 */
	private JMXServerAPIListener(Session aLocalSession) throws DIException {
		if (aLocalSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.SESSION.IS.NULL"));
		}
		mLocalSession = aLocalSession;
		mLocalSession.addEventListener(this, null, "*");
		if (APIEngine.isDebugEnabled()) {
			APIEngine
					.logDebug(sResHash
							.getString("SEVER.API.JMX.SERVER.API.LISTENER.SUCCESSFULLY.REGISTERED"));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(DIEvent aEvent) throws DIException {
		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.DIEVENT.RECEIVED.IS", aEvent));
		}

		String eventType = aEvent.getType();
		if (eventType.equals(DIEvent.EVT_AL_START)) {
			handleALStart(aEvent);
		} else if (eventType.equals(DIEvent.EVT_AL_STOP)) {
			handleALStop(aEvent);
		} else if (eventType.equals(DIEvent.EVT_CI_START)) {
			handleCIStart(aEvent);
		} else if (eventType.equals(DIEvent.EVT_CI_STOP)) {
			handleCIStop(aEvent);
		} else if (eventType.equals(DIEvent.EVT_CI_UPDATED)) {
			handleCIUpdate(aEvent);
		} else if (eventType.equals(DIEvent.EVT_SRV_STOP)) {
			handleServerStop(aEvent);
		} else if (eventType.startsWith(DIEvent.EVT_USER_PREFIX)) {
			handleCustomNotification(aEvent);
		} else {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString(
						"SEVER.API.UNRECOGNIZED.EVENT.TYPE", eventType));
			}
		}
	}

	/**
	 * Handles starting an assembly line event event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleALStart(DIEvent aEvent) throws DIException {
		int alCode = ((Integer) aEvent.getData()).intValue();
		AssemblyLine[] als = mLocalSession.getAssemblyLines();

		int found = -1;
		for (int i = 0; i < als.length; i++) {
			if (als[i].getUniqueCode() == alCode) {
				found = i;
				break;
			}
		}

		if (found > -1) {
			com.ibm.di.api.jmx.mbeans.AssemblyLine alMBean = new com.ibm.di.api.jmx.mbeans.AssemblyLine(
					als[found]);
			try {
				JMXAgent.registerMBean(alMBean);
			} catch (JMException e) {
				APIEngine
						.logErrorAndThrowException(
								sResHash
										.getString("SEVER.API.COULD.NOT.REGISTER.ASSEMBLYLINE.MBEAN"),
								e);
			}

			// send JMX notification
			try {
				Notifier.getNotifier().assemblyLineStarted(alMBean.getId());
			} catch (Exception e) {
				APIEngine
						.logError(sResHash
								.getString(
										"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.EVENT.1",
										e.toString()));
			}
		}
	}

	/**
	 * Handles starting a config instance event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleCIStart(DIEvent aEvent) throws DIException {
		com.ibm.di.api.local.ConfigInstance ci = mLocalSession
				.getConfigInstance(aEvent.getId());
		com.ibm.di.api.jmx.mbeans.ConfigInstance ciMBean = new com.ibm.di.api.jmx.mbeans.ConfigInstance(
				ci);
		try {
			JMXAgent.registerMBean(ciMBean);
		} catch (JMException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.REGISTER.CONFIG.INSTANCE.MBEAN"),
							e);
		}

		// Register the TDI Properties MBean
		try {
			com.ibm.di.api.jmx.mbeans.TDIProperties tdiBean = new com.ibm.di.api.jmx.mbeans.TDIProperties(
					ci.getTDIProperties(), aEvent.getId());

			JMXAgent.registerMBean(tdiBean);
		} catch (JMException e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.REGISTER.TDIPROPERTIES.MBEAN.1"),
							e);
		} catch (Exception e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.REGISTER.TDIPROPERTIES.MBEAN.2"),
							e);
		}

		// send JMX notification
		try {
			Notifier.getNotifier().configInstanceStarted(ciMBean.getId());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.EVENT.2",
									e.toString()));
		}
	}

	/**
	 * Handles stopping an assembly line event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleALStop(DIEvent aEvent) throws DIException {
		ObjectName objectName = com.ibm.di.api.jmx.mbeans.AssemblyLine
				.genObjectName(aEvent.getId(), ((Integer) aEvent.getData())
						.intValue());
		try {
			JMXAgent.unregisterMBean(objectName);
		} catch (JMException e) {
			APIEngine.logError(sResHash.getString(
					"SEVER.API.COULD.NOT.UNREGISTER.ASSEMBLYLINE.MBEAN", e
							.toString()));
		}

		// send JMX notification
		try {
			Notifier.getNotifier().assemblyLineFinished(
					aEvent.getId() + ((Integer) aEvent.getData()).intValue());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.EVENT.FOR.ASSEMBLYLINE.STOP",
									e.toString()));
		}
	}

	/**
	 * Handles stopping a config instance event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleCIStop(DIEvent aEvent) throws DIException {
		ObjectName objectName = ConfigInstance.genObjectName(aEvent.getId());
		try {
			JMXAgent.unregisterMBean(objectName);
		} catch (JMException e) {
			APIEngine.logError(sResHash.getString(
					"SEVER.API.COULD.NOT.UNREGISTER.CONFIG.INSTANCE.MBEAN", e
							.toString()));
		}

		// Unregister TDIP
		try {
			ObjectName objectNameTDIP = TDIProperties.genObjectName(aEvent
					.getId());

			JMXAgent.unregisterMBean(objectNameTDIP);
		} catch (JMException e) {
			APIEngine.logError(sResHash.getString(
					"SEVER.API.COULD.NOT.UNREGISTER.TDIPROPERTIES.MBEAN.1", e
							.toString()));
		} catch (Exception e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.COULD.NOT.UNREGISTER.TDIPROPERTIES.MBEAN.2"),
							e);
		}

		// send JMX notification
		try {
			Notifier.getNotifier().configInstanceStopped(aEvent.getId());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.EVENT.FOR.CONFIG.INSTANCE.STOP",
									e.toString()));
		}
	}

	/**
	 * Handles updating a config instance event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleCIUpdate(DIEvent aEvent) throws DIException {
		try {
			Notifier.getNotifier().configInstanceUpdated(aEvent.getId());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.EVENT.FOR.CONFIG.INSTANCE.FILE.UPDATED",
									e.toString()));
		}
	}

	/**
	 * Handles stopping the server event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleServerStop(DIEvent aEvent) throws DIException {
		try {
			Notifier.getNotifier().serverStopped((Date) aEvent.getData());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.SERVER.STOP.EVENT",
									e.toString()));
		}
	}

	/**
	 * Handles custom notifications.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	private void handleCustomNotification(DIEvent aEvent) throws DIException {
		try {
			Notifier.getNotifier().sendCustomNotification(aEvent.getType(),
					aEvent.getId(), aEvent.getData());
		} catch (Exception e) {
			APIEngine
					.logError(sResHash
							.getString(
									"SEVER.API.JMX.NOTIFICATION.SERVICE.COULD.NOT.HANDLE.CUSTOM.NOTIFICATION.EVENT",
									e.toString()));
		}
	}
}
