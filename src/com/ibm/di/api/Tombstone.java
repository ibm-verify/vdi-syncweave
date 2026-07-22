/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.ibm.di.entry.Entry;

/**
 * This class is used to represent tombstone objects. Tombstone objects contain
 * information for the termination of an AssemblyLine or Config Instance.
 */
public class Tombstone implements Serializable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID for deserialization.
	 */
	static final long serialVersionUID = 5178569311755396746L;

	// Constants, used to denote the key values of the Map object, used for
	// Tombstone's construction
	/**
	 * Property that specifies the type of the component.<br>
	 * Possible values:
	 * <ul>
	 * <li>0 = Config Instance</li>
	 * <li>1 = AssemblyLine</li>
	 * </ul>
	 */
	public static final String PROPERTY_NAME_COMPONENT_TYPE_ID = "componentTypeID";

	/**
	 * Property that specifies the type of the event. Possible values:<br>
	 * <ul>
	 * <li>0 = Stop Event</li>
	 * </ul>
	 */
	public static final String PROPERTY_NAME_EVENT_TYPE_ID = "eventTypeID";

	/**
	 * Property that specifies the time the component was started.
	 */
	public static final String PROPERTY_NAME_START_TIME = "startTime";

	/**
	 * Property that specifies the time the component was created.
	 */
	public static final String PROPERTY_NAME_CREATED_ON = "createdOn";

	/**
	 * Property that specifies the name of the component.
	 */
	public static final String PROPERTY_NAME_COMPONENT_NAME = "componentName";

	/**
	 * Property that specifies the configuration id which the AssemblyLine is
	 * started from.
	 */
	public static final String PROPERTY_NAME_CONFIGURATION = "configuration";

	/**
	 * Property that specifies the code which the Component exited with.
	 * Possible Values:<br>
	 * <ul>
	 * <li>0 = Normal termination</li>
	 * <li>1 = Error</li>
	 * </ul>
	 * 
	 */
	public static final String PROPERTY_NAME_EXIT_CODE = "exitCode";

	/**
	 * Property that specifies the description of the error the component ended
	 * with (if any).
	 */
	public static final String PROPERTY_NAME_ERROR_DESCR = "errorDescription";

	/**
	 * Property that specifies the statistics for the component. The statistics
	 * are stored as an {@link Entry} object.
	 */
	public static final String PROPERTY_NAME_STAT = "stat";

	/**
	 * Property that specifies the globally unique identifier of the tombstone
	 * record.
	 */
	public static final String PROPERTY_NAME_GUID = "guid";

	/**
	 * Property that specifies the user message this tombstone will contain.
	 */
	public static final String PROPERTY_NAME_USER_MESSAGE = "userMessage";

	// Tombstone object properties
	/**
	 * Number, denoting the ID of the current tombstone component. Its values
	 * could be one of the following digits: 0 = configuration instance, 1 =
	 * assembly line, 2 = event handler
	 */
	private int mComponentTypeID = -1;

	/**
	 * Number, denoting the ID of the event, which triggered the creation of the
	 * tombstone. Current version supports only stop events and denote their
	 * event type ID with value 0.
	 */
	private int mEventTypeID = -1;

	/**
	 * Date object, denoting the start time of the object represented by the
	 * tombstone (AssemblyLine or Config Instance).
	 */
	private Date mStartTime = null;

	/**
	 * Date object, denoting the creation of the tombstone record.
	 */
	private Date mCreatedOn = null;

	/**
	 * The name of the tombstone component object (AssemblyLine or Config
	 * Instance)
	 */
	private String mComponentName = "";

	/**
	 * The name of the configuration to which the AssemblyLine belonged.
	 */
	private String mConfiguration = "";

	/**
	 * Status indicator for how the component terminated. 0 means normal
	 * termination; 1 means termination with error.
	 */
	private int mExitCode = -1;

	/**
	 * Description of the error, in case of abnormal component termination.
	 */
	private String mErrorDescription = "";

	/**
	 * Entry object, whose Attributes keep various statistics data like number
	 * of Entries retrieved, modified, deleted, etc. Only relevant for
	 * AssemblyLines.
	 */
	private Entry mStatistics = null;

	/**
	 * Global Unique Identifier is a unique string for each tombstone record
	 * created
	 */
	private String mGUID = "";

	/**
	 * User specified tombstone message.
	 */
	private String mUserMessage = "";

	/**
	 * Construct tombstone object from a map, containing tombstone properties.
	 * 
	 * @param aData
	 *            contains the data that will be written as a tombstone.
	 */
	public Tombstone(Map<String, Object> aData) {
		mComponentTypeID = ((Integer) aData
				.get(PROPERTY_NAME_COMPONENT_TYPE_ID)).intValue();
		mEventTypeID = ((Integer) aData.get(PROPERTY_NAME_EVENT_TYPE_ID))
				.intValue();

		mStartTime = (Date) aData.get(PROPERTY_NAME_START_TIME);
		mCreatedOn = (Date) aData.get(PROPERTY_NAME_CREATED_ON);

		mComponentName = (String) aData.get(PROPERTY_NAME_COMPONENT_NAME);
		mConfiguration = (String) aData.get(PROPERTY_NAME_CONFIGURATION);

		mExitCode = ((Integer) aData.get(PROPERTY_NAME_EXIT_CODE)).intValue();
		mErrorDescription = (String) aData.get(PROPERTY_NAME_ERROR_DESCR);

		mStatistics = (Entry) aData.get(PROPERTY_NAME_STAT);
		mGUID = (String) aData.get(PROPERTY_NAME_GUID);
		mUserMessage = (String) aData.get(PROPERTY_NAME_USER_MESSAGE);
	}

	/**
	 * Returns component type ID. Possible values are: 0 - configuration
	 * instance 1 - assembly line 2 - event handler
	 * 
	 * @return component type ID value
	 */
	public int getComponentTypeID() {
		return mComponentTypeID;
	}

	/**
	 * Returns event type ID. Current version supports only stop event, which
	 * has 0 value
	 * 
	 * @return event type ID value
	 */
	public int getEventTypeID() {
		return mEventTypeID;
	}

	/**
	 * Returns the start time of current tombstone component object (Config
	 * Instance or AssemblyLine).
	 * 
	 * @return Tombstone component start time
	 */
	public Date getStartTime() {
		return mStartTime;
	}

	/**
	 * Returns tombstone record creation time.
	 * 
	 * @return Tombstone creation time
	 */
	public Date getTombstoneCreateTime() {
		return mCreatedOn;
	}

	/**
	 * Returns the name of the tombstone component (Config Instance or
	 * AssemblyLine).
	 * 
	 * @return Tombstone component name
	 */
	public String getComponentName() {
		return mComponentName;
	}

	/**
	 * Returns the name of the configuration to which the AssemblyLine belonged.
	 * 
	 * @return configuration name
	 */
	public String getConfiguration() {
		return mConfiguration;
	}

	/**
	 * Returns status indicator for how the tombstone component terminated.
	 * 
	 * @return Tombstone component exit code. 0 means normal termination. 1
	 *         means termination with error.
	 */
	public int getExitCode() {
		return mExitCode;
	}

	/**
	 * Returns error description in case of abnormal tombstone component
	 * termination.
	 * 
	 * @return error description
	 */
	public String getErrorDescription() {
		return mErrorDescription;
	}

	/**
	 * Returns tombstone Global Unique Identifier (GUID). The GUID is a unique
	 * string object for each tombstone record created.
	 * 
	 * @return Tombstone GUID
	 */
	public String getGUID() {
		return mGUID;
	}

	/**
	 * Returns AssemblyLine's Entry statistics object.
	 * 
	 * @return Entry statistics object
	 */
	public Entry getStatistics() {
		return mStatistics;
	}

	/**
	 * Returns the user specified tombstone message.
	 * 
	 * @return the user specified tombstone message
	 */
	public String getUserMessage() {
		return mUserMessage;
	}

}
