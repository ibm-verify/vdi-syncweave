/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import com.ibm.di.server.TaskStatistics;

/**
 * This class is the object that describes the event that have been triggered.
 * Its purpose is to be an information carrier for events that have impact on an
 * <code>AssemblyLine</code>.
 */
public class ALEvent extends DIEvent {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID for deserialization.
	 */
	static final long serialVersionUID = 5631772256973692972L;

	/**
	 * {@link TaskStatistics} object which carries information about the running
	 * <code>AssemblyLine</code> instance at the time when the event had
	 * occurred.
	 */
	private TaskStatistics mStats = null;

	/**
	 * Global unique ID.
	 */
	private String mGUID = null;

	/**
	 * A custom user message.
	 */
	private String mUserMessage = null;

	/**
	 * Creates new {@link ALEvent} instance.
	 * 
	 * @param aType
	 *            the type of the event that had occurred. <br>
	 *            Predefined constants: <br>
	 *            {@link #EVT_CI_START}<br>
	 *            {@link #EVT_CI_STOP}<br>
	 *            {@link #EVT_CI_UPDATED}<br>
	 *            {@link #EVT_AL_START}<br>
	 *            {@link #EVT_AL_STOP}<br>
	 *            {@link #EVT_SRV_STOP}<br>
	 *            {@link #EVT_SRV_STOP}
	 * @param aId
	 *            the ID of the event <b>Note: </b>This ID should not to be
	 *            think of as a Unique Identifier used for distinguishing
	 *            different events occurring in the system.
	 * @param aData
	 *            the additional information this event carrier might contain.
	 * @param aConfigInstanceId
	 *            the configInstance id, this might be <code>null</code>.
	 * @param aStats
	 *            the {@link TaskStatistics} object which carries information
	 *            about the running <code>AssemblyLine</code> instance at the
	 *            time when the event had occurred.
	 * @throws DIException
	 *             if the <code>aType</code> parameter is <code>null</code>.
	 */
	public ALEvent(String aType, String aId, Object aData,
			String aConfigInstanceId, TaskStatistics aStats) throws DIException {
		super(aType, aId, aData, aConfigInstanceId);
		mStats = aStats;
	}

	/**
	 * Creates new {@link ALEvent} instance.
	 * 
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
	 *            the configInstance id, this might be <code>null</code>.
	 * @param aStats
	 *            the {@link TaskStatistics} object which carries information
	 *            about the running <code>AssemblyLine</code> instance at the
	 *            time when the event had occurred.
	 * @param aGUID
	 *            the globally unique identifier (GUID) of the AssemblyLine.
	 * @param aUserMessage
	 *            a custom user message, usually used when storing TombStones
	 * @throws DIException
	 *             if the <code>aType</code> parameter is <code>null</code>.
	 */
	public ALEvent(String aType, String aId, Object aData,
			String aConfigInstanceId, TaskStatistics aStats, String aGUID,
			String aUserMessage) throws DIException {

		this(aType, aId, aData, aConfigInstanceId, aStats);
		mGUID = aGUID;
		mUserMessage = aUserMessage;
	}

	/**
	 * Retrives staristics.
	 * @return the {@link TaskStatistics} object, that holds the statistics
	 *         information about the running <code>AssemblyLine</code> at the
	 *         time the event has occurred.
	 */
	public TaskStatistics getStatistics() {
		return mStats;
	}

	/**
	 * Retrieves global ID.
	 * @return the globally unique identifier (GUID) of the AssemblyLine.
	 */
	public String getGUID() {
		return mGUID;
	}

	/**
	 * Retrieves a custom user message.
	 * @return a custom user message, usually used when storing TombStones
	 */
	public String getUserMessage() {
		return mUserMessage;
	}

	/**
	 * Converts event to readable output.
	 * @return the String representation of this {@link ALEvent} object,
	 *         containing all of its attributes.
	 */
	public String toString() {
		String diEvent = super.toString();
		if (diEvent == null || diEvent.length() == 0) {
			diEvent = "[]";
		}

		diEvent = diEvent.substring(0, diEvent.length() - 1) + ", stats="
				+ mStats + ", GUID=" + mGUID + ", UserMessage=" + mUserMessage
				+ "]";

		return diEvent;
	}

}
