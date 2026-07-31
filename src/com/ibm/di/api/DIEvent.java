/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.io.Serializable;
import java.util.Date;

import com.ibm.di.server.ResourceHash;

/**
 * 
 * This class is the object that describes the event that have been triggered.
 * Its purpose is to be a general event information carrier.
 * 
 */
public class DIEvent implements Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID for deserialization.
	 */
	static final long serialVersionUID = -8664533477452491219L;

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_CI_START} = {@value #EVT_CI_START}
	 */
	public static final String EVT_CI_START = "di.ci.start";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_CI_STOP} = {@value #EVT_CI_STOP}
	 */
	public static final String EVT_CI_STOP = "di.ci.stop";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_CI_UPDATED} = {@value #EVT_CI_UPDATED}
	 */
	public static final String EVT_CI_UPDATED = "di.ci.file.updated";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_AL_START} = {@value #EVT_AL_START}
	 */
	public static final String EVT_AL_START = "di.al.start";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_AL_STOP} = {@value #EVT_AL_STOP}
	 */
	public static final String EVT_AL_STOP = "di.al.stop";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_SRV_STOP} = {@value #EVT_SRV_STOP}
	 */
	public static final String EVT_SRV_STOP = "di.server.stop";

	/**
	 * If the type of the {@link DIEvent} starts with this prefix then it means
	 * that the this is a custom user defined event that have been triggered.
	 * <br>
	 * {@link #EVT_USER_PREFIX} = {@value #EVT_USER_PREFIX}
	 */
	public static final String EVT_USER_PREFIX = "user.";

	/**
	 * A constant value used to tell the type of the {@link DIEvent} object.
	 * <br>
	 * {@link #EVT_USER_PREFIX} = {@value #EVT_USER_PREFIX}
	 */
	public static final String EVT_AL_MSG = "di.al.msg";

	/**
	 * The type of the event that had occurred.
	 */
	private String mType = null;

	/**
	 * The Identifier of the {@link DIEvent}
	 */
	private String mId = null;

	/**
	 * The additional data this event might carry
	 */
	private Object mData = null;

	/**
	 * The identifier of the configInstance
	 */
	private String mConfigInstanceId = null;

	/**
	 * The time when this {@link DIEvent} instance was created.
	 */
	private Date mDateCreated = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * 
	 * Create a new {@link DIEvent} instance.
	 * 
	 * @param aType
	 *            the type of the event that had occurred. <br>
	 *            Predefined constants: <br>
	 *            {@link #EVT_CI_START} = {@value #EVT_CI_START}<br>
	 *            {@link #EVT_CI_STOP} = {@value #EVT_CI_STOP}<br>
	 *            {@link #EVT_CI_UPDATED} = {@value #EVT_CI_UPDATED}<br>
	 *            {@link #EVT_AL_START} = {@value #EVT_AL_START}<br>
	 *            {@link #EVT_AL_STOP} = {@value #EVT_AL_STOP}<br>
	 *            {@link #EVT_SRV_STOP} = {@value #EVT_SRV_STOP}
	 * 
	 * @param aId
	 *            the ID of the event <b>Note: </b>This ID should not to be
	 *            think of as a Unique Identifier used for distinguishing
	 *            different events occurring in the system.
	 * @param aData
	 *            the additional information this event carrier might contain.
	 * @throws DIException
	 *             if the <code>aType</code> is <code>null</code>.
	 */
	public DIEvent(String aType, String aId, Object aData) throws DIException {
		this(aType, aId, aData, null);
	}

	/**
	 * 
	 * Create a new {@link DIEvent} instance.
	 * 
	 * @param aType
	 *            the type of the event that had occurred. <br>
	 *            Predefined constants: <br>
	 *            {@link #EVT_CI_START} = {@value #EVT_CI_START}<br>
	 *            {@link #EVT_CI_STOP} = {@value #EVT_CI_STOP}<br>
	 *            {@link #EVT_CI_UPDATED} = {@value #EVT_CI_UPDATED}<br>
	 *            {@link #EVT_AL_START} = {@value #EVT_AL_START}<br>
	 *            {@link #EVT_AL_STOP} = {@value #EVT_AL_STOP}<br>
	 *            {@link #EVT_SRV_STOP} = {@value #EVT_SRV_STOP}
	 * @param aId
	 *            the ID of the event <b>Note: </b>This ID should not to be
	 *            think of as a Unique Identifier used for distinguishing
	 *            different events occurring in the system.
	 * @param aData
	 *            the additional information this event carrier might contain.
	 * @param aConfigInstanceId
	 *            the configInstance id, this might be <code>null</code>.
	 * @throws DIException
	 *             if the <code>aType</code> is <code>null</code>.
	 */
	public DIEvent(String aType, String aId, Object aData,
			String aConfigInstanceId) throws DIException {
		if (aType == null) {
			throw new DIException(sResHash
					.getString("SEVER.API.EVENT.TYPE.IS.NULL"));
		}

		mType = aType;
		mId = aId;
		mData = aData;
		mConfigInstanceId = aConfigInstanceId;
		mDateCreated = new Date();
	}

	/**
	 * Retrieves the type of the event.
	 * 
	 * @return the type of this event.
	 */
	public String getType() {
		return mType;
	}

	/**
	 * Retrieves the config instance ID.
	 * 
	 * @return the identifier of the configInstance in which the event had
	 *         occurred, or <code>null</code> if the event had occurred in
	 *         different place.
	 */
	public String getConfigInstanceId() {
		return mConfigInstanceId;
	}

	/**
	 * Retrieves the ID of the {@link DIEvent}
	 * 
	 * @return the identifier of this {@link DIEvent} instance.
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Retrives tha data from the object.
	 * 
	 * @return the information this object carries.
	 */
	public Object getData() {
		return mData;
	}

	/**
	 * Retrieves the cration of the {@link DIEvent}
	 * 
	 * @return the time this {@link DIEvent} object was created.
	 */
	public Date getDateCreated() {
		return mDateCreated;
	}

	/**
	 * Converts the event to a readable output.
	 * 
	 * @return the {@link String} representation of this {@link DIEvent} object,
	 *         containing all of its attributes.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer("[");
		buffer.append("type=").append(mType).append(", ");
		buffer.append("id=").append(mId).append(", ");
		buffer.append("data=").append(mData).append(", ");
		if (mConfigInstanceId != null) {
			buffer.append("configid=").append(mConfigInstanceId).append(", ");
		}
		buffer.append("created=").append(mDateCreated);
		buffer.append("]");
		return buffer.toString();
	}

}
