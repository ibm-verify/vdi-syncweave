/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb;

/**
 * This class contains various constants used by the DISB Component classes
 * (e.g. attribute names, configuration constants, etc.).
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class DISBConstants {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * JSON/Entry create.
	 */
	public static final String JSONMSG_ATTR_CREATE = "create";

	/**
	 * JSON/Entry modify.
	 */
	public static final String JSONMSG_ATTR_MODIFY = "modify";

	/**
	 * JSON/Entry delete.
	 */
	public static final String JSONMSG_ATTR_DELETE = "delete";

	/**
	 * JSON/Entry refresh.
	 */
	public static final String JSONMSG_ATTR_REFRESH = "refresh";

	/**
	 * JSON/Entry reference.
	 */
	public static final String JSONMSG_ATTR_REFERENCE = "reference";

	/**
	 * JSON/Entry timeStamp.
	 */
	public static final String JSONMSG_ATTR_TIMESTAMP = "timeStamp";

	/**
	 * JSON/Entry operationSet.
	 */
	public static final String JSONMSG_ATTR_OPSET = "operationSet";

	/**
	 * JSON/Entry opid.
	 */
	public static final String JSONMSG_ATTR_OPSETID = "opid";

	/**
	 * DIS JSON message in Entry.
	 */
	public static final String JSONMSG_ATTR_MESSAGE = "DISJSONMessage";

	/**
	 * JMS Message in Entry.
	 */
	public static final String JMSMSG_ATTR_MSG = "message";

	/**
	 * JSON/Entry relationship.
	 */
	public static final String JSONMSG_ATTR_RELATIONSHIP = "relationship";

	/**
	 * JSON/Entry source.
	 */
	public static final String JSONMSG_ATTR_SOURCE = "source";

	/**
	 * JSON/Entry target.
	 */
	public static final String JSONMSG_ATTR_TARGET = "target";

	/**
	 * JSON/Entry modelObject.
	 */
	public static final String JSONMSG_ATTR_MODELOBJECT = "modelObject";

	/**
	 * JSON/Entry timeStamp.
	 */
	public static final String JSONMSG_IDML_ATTR_TIMESTAMP = "timestamp";
}
