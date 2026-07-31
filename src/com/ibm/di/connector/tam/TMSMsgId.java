/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.tam;

/**
 * Typesafe enum of the TAM Connector message ids
 */
public class TMSMsgId {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final String msgID;

	private TMSMsgId(String code) {
		msgID = code;
	}

	/**
	 * Return the TAM Connector message ID
	 * 
	 * @return String TAM Connector message ID
	 */
	public String toString() {
		return msgID;
	}

	// Error Messages
	public static final TMSMsgId INVALID_ENTRY_TYPE = new TMSMsgId("CTGDIK401E");

	public static final TMSMsgId MISSING_ENTRY_TYPE = new TMSMsgId("CTGDIK402E");

	public static final TMSMsgId INVALID_CONFIGFILE = new TMSMsgId("CTGDIK403E");

	public static final TMSMsgId COULD_NOT_LOG_ON = new TMSMsgId("CTGDIK404E");

	public static final TMSMsgId COULD_NOT_QUERY_DOMAINS = new TMSMsgId(
			"CTGDIK405E");

	public static final TMSMsgId COULD_NOT_SHUT_DOWN = new TMSMsgId(
			"CTGDIK406E");

	public static final TMSMsgId SELECT_ERROR = new TMSMsgId("CTGDIK407E");

	public static final TMSMsgId SELECT_NEXT_ERROR = new TMSMsgId("CTGDIK408E");

	public static final TMSMsgId FIND_ERROR = new TMSMsgId("CTGDIK409E");

	public static final TMSMsgId CREATE_ERROR = new TMSMsgId("CTGDIK410E");

	public static final TMSMsgId MODIFY_ERROR = new TMSMsgId("CTGDIK411E");

	public static final TMSMsgId DELETE_ERROR = new TMSMsgId("CTGDIK412E");

	public static final TMSMsgId PD_INVALID_MSG = new TMSMsgId("CTGDIK413E");

	public static final TMSMsgId PD_SERVER_ERROR = new TMSMsgId("CTGDIK414E");

	public static final TMSMsgId JAVA_ERROR = new TMSMsgId("CTGDIK415E");

	public static final TMSMsgId MISSING_ATTRIBUTE = new TMSMsgId("CTGDIK416E");

	public static final TMSMsgId UNSUPPORTED_SEARCH_CRITERIA = new TMSMsgId(
			"CTGDIK417E");

	public static final TMSMsgId POLICY_DELETE_FAIL = new TMSMsgId("CTGDIK418E");

	public static final TMSMsgId INVALID_RESOURCETYPE = new TMSMsgId(
			"CTGDIK419E");

	public static final TMSMsgId GROUP_RESERVED = new TMSMsgId("CTGDIK420E");

	public static final TMSMsgId USER_RESERVED = new TMSMsgId("CTGDIK421E");

	public static final TMSMsgId GROUP_LOAD_ERROR = new TMSMsgId("CTGDIK422E");

	public static final TMSMsgId NO_CREDS_FOR_USER = new TMSMsgId("CTGDIK423E");

	public static final TMSMsgId INVALID_OPER_CODE = new TMSMsgId("CTGDIK424E");

	// Information Messages
	public static final TMSMsgId TAM_SHUTDOWN = new TMSMsgId("CTGDIK425I");

	public static final TMSMsgId TAM_INITIALIZE = new TMSMsgId("CTGDIK426I");

	public static final TMSMsgId CREATING_CONTEXT = new TMSMsgId("CTGDIK427I");

	public static final TMSMsgId DATA_GET = new TMSMsgId("CTGDIK428I");

	public static final TMSMsgId USER_GROUPS = new TMSMsgId("CTGDIK429I");

	public static final TMSMsgId IMPORTING_USER = new TMSMsgId("CTGDIK430I");

	public static final TMSMsgId ADD_USER_TO_GROUP = new TMSMsgId("CTGDIK431I");

	public static final TMSMsgId GROUPS_TO_ADD = new TMSMsgId("CTGDIK432I");

	public static final TMSMsgId GROUPS_TO_DELETE = new TMSMsgId("CTGDIK433I");

	public static final TMSMsgId DELETE_USER_FROM_GROUP = new TMSMsgId(
			"CTGDIK434I");

	public static final TMSMsgId USERS_TO_ADD_TO_GROUP = new TMSMsgId(
			"CTGDIK435I");

	public static final TMSMsgId USERS_TO_DELETE_FROM_GROUP = new TMSMsgId(
			"CTGDIK436I");

	public static final TMSMsgId REMOVING_FROM_GROUP = new TMSMsgId(
			"CTGDIK437I");

	public static final TMSMsgId ADDING_TO_GROUP = new TMSMsgId("CTGDIK438I");

	public static final TMSMsgId DELETE_GROUP = new TMSMsgId("CTGDIK439I");

	public static final TMSMsgId DELETE_USER = new TMSMsgId("CTGDIK440I");

	public static final TMSMsgId POLICY_ADD = new TMSMsgId("CTGDIK441I");

	public static final TMSMsgId POLICY_DELETE = new TMSMsgId("CTGDIK442I");

	public static final TMSMsgId DOMAIN_CREATE = new TMSMsgId("CTGDIK443I");

	public static final TMSMsgId DOMAIN_DELETE = new TMSMsgId("CTGDIK444I");

	public static final TMSMsgId SSOCRED_CREATE = new TMSMsgId("CTGDIK445I");

	public static final TMSMsgId SSOCRED_DELETE = new TMSMsgId("CTGDIK446I");

	public static final TMSMsgId SCHEMA_INFO = new TMSMsgId("CTGDIK447I");

	public static final TMSMsgId INVALID_CONFIG_PARAM = new TMSMsgId(
			"CTGDIK448E");

	public static final TMSMsgId PD_INVALID_CONTEXT = new TMSMsgId("CTGDIK449E");

	public static final TMSMsgId PD_UNKNOWN_MSG_TYPE = new TMSMsgId(
			"CTGDIK450E");
}
