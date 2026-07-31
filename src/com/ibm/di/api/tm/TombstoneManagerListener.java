/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.tm;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import com.ibm.di.api.ALEvent;
import com.ibm.di.api.APIEngine;
import com.ibm.di.api.CIEvent;
import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.local.ConfigInstance;
import com.ibm.di.api.local.DIEventListener;
import com.ibm.di.api.local.Session;
import com.ibm.di.config.base.MetamergeConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.TombstonesConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;

/**
 * This class handles the stop events for AssmeblyLine and Config Instances and
 * triggers the Tombstones Manager logic.
 */
public class TombstoneManagerListener implements DIEventListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_EVENT_TYPE_ID} property. <br>
	 * The value of this constant is: {@value #EVENT_TYPE_STOP}
	 */
	public static final int EVENT_TYPE_STOP = 0;

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_EXIT_CODE} property. <br>
	 * The value of this constant is: {@value #EXIT_CODE_NORMAL}
	 */
	public static final int EXIT_CODE_NORMAL = 0;

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_EXIT_CODE} property. <br>
	 * The value of this constant is: {@value #EXIT_CODE_ERROR}
	 */
	public static final int EXIT_CODE_ERROR = 1;

	/**
	 * This is the parameter in the Tombstone config object of the specific
	 * AssemblyLine that specifies whether tombstones are turned on/off.
	 */
	public static final String AL_TS_SETTING_PARAMETER = "createTombstones";

	/**
	 * This is the parameter in the Tombstone config object that specifies
	 * whether tombstones are turned on/off for all the AssemblyLines.
	 */
	public static final String AL_ALL_TS_PARAMETER = "AssemblyLines";

	/**
	 * This is the parameter in the Tombstone config object that specifies
	 * whether tombstones are turned on/off for the specific ConfigInstance.
	 */
	public static final String CI_TS_PARAMETER = "Configuration";

	/**
	 * Handles the stop events for AssmeblyLine and Config Instances and
	 * triggers the Tombstones Manager logic.
	 */
	private static TombstoneManagerListener mServerAPIListener = null;

	/**
	 * Represents the local session.
	 */
	private Session mLocalSession = null;

	/**
	 * Manages {@link Tombstone} objects.
	 */
	private TombstoneManager mTombstoneManager = null;

	/**
	 * This variable specifies the total number of tombstone records that will
	 * trigger the logic for leveling the number of tombstone records to a
	 * certain number.
	 */
	private int mRecordsTriggerOn = 0;

	/**
	 * Maximal records to keep.
	 */
	private int mRecordsMax = 0;

	/**
	 * Number of records.
	 */
	private int mRecordsCount = 0;

	/**
	 * Flag for monitor record count.
	 */
	private boolean mMonitorRecordCount = false;

	/**
	 * Used to lock resource.
	 */
	private Object mCounterLock = new Object();
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Initializes the listener.
	 * 
	 * @param aLocalSession
	 *            The local session used for registration for events.
	 * @param aTombstoneManager
	 *            the manager responsible for initialization/usage of this
	 *            listener.
	 * @throws DIException
	 */
	public static void initializeListener(Session aLocalSession,
			TombstoneManager aTombstoneManager) throws DIException {
		if (mServerAPIListener != null) {
			throw new DIException(
					sResHash
							.getString("SEVER.API.TOMBSTONE.MANAGER.LISTENER.ALREADY.INITIALIZED"));
		}

		mServerAPIListener = new TombstoneManagerListener(aLocalSession,
				aTombstoneManager);
	}

	/**
	 * Class constructor.
	 * 
	 * @param aLocalSession
	 *            {@link Session} instance.
	 * @param aTombstoneManager
	 *            {@link TombstoneManager} instance.
	 * @throws DIException
	 *             if any of the parameters is null.
	 */
	private TombstoneManagerListener(Session aLocalSession,
			TombstoneManager aTombstoneManager) throws DIException {
		if (aLocalSession == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.LOCAL.SESSION.IS.NULL.1"));
		}

		mTombstoneManager = aTombstoneManager;
		mLocalSession = aLocalSession;
		mLocalSession.addEventListener(this, "di.*", "*");

		if (APIEngine.isDebugEnabled()) {
			APIEngine
					.logDebug(sResHash
							.getString("SEVER.API.TOMBSTONE.MANAGER.SERVER.API.LISTENER.SUCCESSFULLY.REGISTERED"));
		}

		initRecordCounters();
	}

	/**
	 * This method handles tombstone events.
	 * 
	 * @see DIEvent
	 * @param aEvent
	 *            event object, which will be handled
	 * @throws DIException
	 */
	public void handleEvent(DIEvent aEvent) throws DIException {

		boolean tombstoneCreated = false;
		String eventType = aEvent.getType();
		if (eventType.equals(DIEvent.EVT_AL_STOP)) {
			if (aEvent instanceof ALEvent) {
				if (!createALTombstone((ALEvent) aEvent)) {
					return;
				}
				handleComponentStopEvent(aEvent);
				tombstoneCreated = true;
			}
		} else if ((eventType.equals(DIEvent.EVT_CI_STOP))
				&& (aEvent instanceof CIEvent)) {
			if (!createCITombstone((CIEvent) aEvent)) {
				return;
			}
			handleCIStopEvent(aEvent);
			tombstoneCreated = true;
		}

		if (tombstoneCreated) {
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString(
						"SEVER.API.TOMBSTONE.CREATED.FOR.EVENT", aEvent
								.toString()));
			}
			if (mMonitorRecordCount) {
				synchronized (mCounterLock) {
					mRecordsCount++;
					if (mRecordsCount >= mRecordsTriggerOn) {
						if (APIEngine.isDebugEnabled()) {
							APIEngine.logDebug(sResHash.getString(
									"SEVER.API.CURRENT.RECORD.COUNT.IS", String
											.valueOf(mRecordsCount)));
							APIEngine
									.logDebug(sResHash
											.getString(
													"SEVER.API.INITIATING.RECORD.DELETION.TO.MAX.RECORDS",
													String.valueOf(mRecordsMax)));
						}
						mTombstoneManager.keepMostRecentTombstones(mRecordsMax);
						if (APIEngine.isDebugEnabled()) {
							APIEngine
									.logDebug(sResHash
											.getString("SEVER.API.FINISHED.RECORD.DELETION"));
						}
						initRecordCounters();
					}
				}
			}
		}
	}

	/**
	 * Handles stop ALEvent.
	 * 
	 * @param aEvent
	 *            {@link DIEvent} instance
	 * @throws DIException
	 */
	private void handleComponentStopEvent(DIEvent aEvent) throws DIException {

		// Initialize properties, which differ for assembly lines and event
		// handlers
		int componentTypeID = -1;
		TaskStatistics statistics = null;
		String guid = "";
		String userMessage = "";

		componentTypeID = TombstoneManager.COMPONENT_TYPE_ASSEMBLY_LINE;
		statistics = ((ALEvent) aEvent).getStatistics();
		guid = ((ALEvent) aEvent).getGUID();
		userMessage = ((ALEvent) aEvent).getUserMessage();

		int eventTypeID = EVENT_TYPE_STOP;

		Entry tombstoneEntry = getTombstoneEntryFromTaskStats(statistics);

		long startTime = statistics.getStart();
		long createdOn = System.currentTimeMillis();

		String componentName = aEvent.getId();
		String configuration = aEvent.getConfigInstanceId();

		int getErr = statistics.numErrors();

		int exitCode = EXIT_CODE_NORMAL;
		String errorDescr = "";

		if (getErr > 0) {
			exitCode = EXIT_CODE_ERROR;
			if (statistics.getError() != null) {
				errorDescr = statistics.getError().toString();
			}
		}

		byte[] stat = null;

		try {

			stat = serializeObject(tombstoneEntry);

		} catch (Exception e) {
			APIEngine
					.logErrorAndThrowException(
							sResHash
									.getString("SEVER.API.TOMBSTONE.STATISTICS.SERIALIZATION.ERROR"),
							e);
		}

		mTombstoneManager.doInsert(componentTypeID, eventTypeID, startTime,
				createdOn, componentName, configuration, exitCode, errorDescr,
				guid, stat, userMessage);
	}

	/**
	 * Handles stop {@link CIEvent}
	 * 
	 * @param aEvent
	 *            {@link DIEvent} instance
	 * @throws DIException
	 */
	private void handleCIStopEvent(DIEvent aEvent) throws DIException {

		int componentTypeID = TombstoneManager.COMPONENT_TYPE_CONFIG_INSTANCE;
		int eventTypeID = EVENT_TYPE_STOP;

		long startTime = ((CIEvent) aEvent).getStarted();
		long createdOn = System.currentTimeMillis();

		String componentName = aEvent.getId();
		String configuration = aEvent.getConfigInstanceId();

		int exitCode = EXIT_CODE_NORMAL;
		String errorDescr = "";
		String userMessage = "";

		String guid = ((CIEvent) aEvent).getGUID();

		mTombstoneManager.doInsert(componentTypeID, eventTypeID, startTime,
				createdOn, componentName, configuration, exitCode, errorDescr,
				guid, null, userMessage);
	}

	/**
	 * Retrieves Tombstone {@link Entry} from {@link TaskStatistics} instance
	 * 
	 * @param aStatistics
	 *            TaskStatistics instance
	 * @return Entry
	 */
	private Entry getTombstoneEntryFromTaskStats(TaskStatistics aStatistics) {

		Entry result = new Entry();

		Entry taskStatEntry = aStatistics.getEntry();

		result.setAttribute("add", taskStatEntry.getAttribute("add"));
		result.setAttribute("mod", taskStatEntry.getAttribute("mod"));
		result.setAttribute("del", taskStatEntry.getAttribute("del"));
		result.setAttribute("get", taskStatEntry.getAttribute("get"));
		result.setAttribute("request", taskStatEntry.getAttribute("getclient"));
		result.setAttribute("callReply", taskStatEntry
				.getAttribute("callreply"));
		result.setAttribute("err", taskStatEntry.getAttribute("err"));
		result.setAttribute("skip", taskStatEntry.getAttribute("skip"));
		result.setAttribute("lookup", taskStatEntry.getAttribute("lookup"));
		result.setAttribute("ignore", taskStatEntry.getAttribute("ignore"));
		result.setAttribute("reconnect", taskStatEntry
				.getAttribute("reconnect"));
		result.setAttribute("exception", taskStatEntry
				.getAttribute("exception"));
		result.setAttribute("getTries", taskStatEntry.getAttribute("getTries"));
		result.setAttribute("getclientTries", taskStatEntry
				.getAttribute("getclientTries"));
		result.setAttribute("nochange", taskStatEntry.getAttribute("nochange"));
		result.setAttribute("branchtrue", taskStatEntry
				.getAttribute("branchtrue"));
		result.setAttribute("branchfalse", taskStatEntry
				.getAttribute("branchfalse"));
		result.setAttribute("loopstart", taskStatEntry
				.getAttribute("loopstart"));
		result.setAttribute("loopcycles", taskStatEntry
				.getAttribute("loopcycles"));
		result.setAttribute("reconnectTime", taskStatEntry
				.getAttribute("reconnectTime"));

		return result;
	}

	/**
	 * Serialize an object to a byte array.
	 * 
	 * @param aObjToSerialize
	 *            object, which will be serialized
	 * @return byte array, containing the serialized object
	 * @throws DIException
	 *             if serialization fails.
	 */
	private byte[] serializeObject(Object aObjToSerialize) throws DIException {

		byte[] result = null;

		try {
			if (aObjToSerialize != null) {
				ByteArrayOutputStream byteArrOutputStr = new ByteArrayOutputStream();
				ObjectOutputStream objOutputStr = new ObjectOutputStream(
						byteArrOutputStr);

				objOutputStr.writeObject(aObjToSerialize);
				objOutputStr.close();

				result = byteArrOutputStr.toByteArray();
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.OBJECT.SERIALIZATION.FAILED"), e);
		}
		return result;
	}

	/**
	 * Creates  AL Tombstone.
	 * @param aALEvent
	 *            {@link ALEvent} instance.
	 * @return <code>true</code> if a TombStorne was created for this event,
	 *         <code>false</code> otherwise.
	 * @throws DIException
	 */
	private boolean createALTombstone(ALEvent aALEvent) throws DIException {
		if (Boolean.getBoolean(TombstoneManager.PROP_CREATE_ALL)) {
			return true;
		}
		ConfigInstance ci = mLocalSession.getConfigInstance(aALEvent
				.getConfigInstanceId());
		if (ci != null) {
			MetamergeConfig mc = ci.getConfiguration();
			if (mc != null) {
				try {
					TombstonesConfig tc = (TombstonesConfig) ((MetamergeConfigImpl) mc)
							.lookupInFolder(
									MetamergeConfig.DEFAULT_SERVER_FOLDER,
									MetamergeConfig.DEFAULT_SERVER_TOMBSTONES);
					boolean create = Boolean.valueOf(
							(String) tc.getParameter(AL_ALL_TS_PARAMETER))
							.booleanValue();
					if (create) {
						return true;
					}
					AssemblyLineConfig alc = mc.getAssemblyLine(aALEvent
							.getId());
					create = Boolean.valueOf(
							(String) alc.getSettings().getParameter(
									AL_TS_SETTING_PARAMETER)).booleanValue();
					if (create) {
						return true;
					}
				} catch (Exception e) {
					APIEngine
							.logError(sResHash
									.getString(
											"SEVER.API.UNABLE.TO.GET.ASSEMBLYLINECONFIG.FOR.ASSEMBLYLINE",
											aALEvent.getId()));
				}
			}
		}
		return false;
	}

	/**
	 * Creates a Tomstone to a config instance event.
	 * @param aCIEvent
	 *            {@link CIEvent} instance.
	 * @return <code>true</code> if a TombStorne was created for this event,
	 *         <code>false</code> otherwise.
	 * @throws DIException
	 */
	private boolean createCITombstone(CIEvent aCIEvent) throws DIException {
		if (Boolean.getBoolean(TombstoneManager.PROP_CREATE_ALL)) {
			return true;
		}
		if (aCIEvent.createTombstone()) {
			return true;
		}
		return false;
	}

	/**
	 * Initializes record counter.
	 * 
	 * @throws DIException
	 *             if an error occurs
	 */
	private void initRecordCounters() throws DIException {
		String recTrigger = System
				.getProperty(TombstoneManager.PROP_AUTODEL_RECORDS_TRIGGER);
		if (recTrigger != null && recTrigger.trim().length() > 0) {
			mRecordsTriggerOn = Integer.parseInt(recTrigger);
		}
		String recMax = System
				.getProperty(TombstoneManager.PROP_AUTODEL_RECORDS_MAX);
		if (recMax != null && recMax.trim().length() > 0) {
			mRecordsMax = Integer.parseInt(recMax);
		}
		mRecordsCount = mTombstoneManager.getTombstonesCount();

		if (APIEngine.isDebugEnabled()) {
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.INITRECORDCOUNTERS.RECORDSTRIGGER", String
							.valueOf(mRecordsTriggerOn)));
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.INITRECORDCOUNTERS.RECORDSMAX", String
							.valueOf(mRecordsMax)));
			APIEngine.logDebug(sResHash.getString(
					"SEVER.API.INITRECORDCOUNTERS.RECORDSCOUNT", String
							.valueOf(mRecordsCount)));
		}
		if (mRecordsMax < mRecordsTriggerOn) {
			mMonitorRecordCount = true;
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString("SEVER.API.INITRECORDCOUNTERS.RECORDS.COUNT.MONITORING.TURNED.ON"));
			}
		} else {
			mMonitorRecordCount = false;
			if (APIEngine.isDebugEnabled()) {
				APIEngine
						.logDebug(sResHash
								.getString("SEVER.API.INITRECORDCOUNTERS.RECORDS.COUNT.MONITORING.DISABLED"));
			}
		}
	}

}
