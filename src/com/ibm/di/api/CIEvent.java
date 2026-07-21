/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

/**
 * This class is the object that describes the event that have been triggered.
 * Its purpose is to be an information carrier for events that have impact on an
 * <code>ConfigInstance</code>.
 */
public class CIEvent extends DIEvent {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = 5178569311755396746L;

	/**
	 * The time the config instance was started.
	 */
	private long mStarted = 0;

	/**
	 * Global Unique ID.
	 */
	private String mGUID = null;

	/**
	 * Flag that indicates whether a Tombstone should be created.
	 */
	private boolean mCreateTS = false;

	/**
	 * @param aType
	 *            the type of the event that had occurred. <br>
	 *            Predefined constants: <br>
	 *            {@link #EVT_CI_START}<br>
	 *            {@link #EVT_CI_STOP}<br>
	 *            {@link #EVT_CI_UPDATED}<br>
	 *            {@link #EVT_AL_START}<br>
	 *            {@link #EVT_AL_STOP}<br>
	 *            {@link #EVT_SRV_STOP}
	 * @param aId
	 *            the ID of the event <b>Note: </b>This ID should not to be
	 *            think of as a Unique Identifier used for distinguishing
	 *            different events occurring in the system.
	 * @param aData
	 *            the additional information this event carrier might contain.
	 * @param aConfigInstanceId
	 *            the config instance id.
	 * @param aStarted
	 *            the time the config instance was started.
	 * @param aGUID
	 *            the globally unique identifier (GUID) of the ConfigInstance.
	 * @param aCreateTS
	 *            tells whether a TombStorne was created for this event.
	 * 
	 * @throws DIException
	 *             if the <code>aType</code> parameter is <code>null</code>.
	 */
	public CIEvent(String aType, String aId, Object aData,
			String aConfigInstanceId, long aStarted, String aGUID,
			boolean aCreateTS) throws DIException {
		super(aType, aId, aData, aConfigInstanceId);
		mStarted = aStarted;
		mGUID = aGUID;
		mCreateTS = aCreateTS;
	}

	/**
	 * Retrives the time the config instance was started.
	 * 
	 * @return the time the config instance was started.
	 */
	public long getStarted() {
		return mStarted;
	}

	/**
	 * Retrieves global ID.
	 * 
	 * @return the globally unique identifier (GUID) of the AssemblyLine.
	 */
	public String getGUID() {
		return mGUID;
	}

	/**
	 * Checks if a tombstone should created.
	 * 
	 * @return true if a TombStone is to be created for this event, false
	 *         otherwise.
	 */
	public boolean createTombstone() {
		return mCreateTS;
	}

	/**
	 * Converts event to a readable output.
	 * 
	 * @return the {@link String} representation of this object.
	 */
	public String toString() {
		String diEvent = super.toString();
		if (diEvent == null || diEvent.length() == 0) {
			diEvent = "[]";
		}

		diEvent = diEvent.substring(0, diEvent.length() - 1) + ", started="
				+ mStarted + ", GUID=" + mGUID + ", createTS=" + mCreateTS
				+ "]";

		return diEvent;
	}

}
