/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * Compile time constants for configuration parameter names.
 */
final class ConfigurationNames {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * Config parameter names.
	 */
	static final String PARAM_PUT_STYLESHEET_LIST = "sapr3.conn.putStylesheets";

	static final String PARAM_MODIFY_STYLESHEET_LIST = "sapr3.conn.modifyStylesheets";

	static final String PARAM_DELETE_STYLESHEET_LIST = "sapr3.conn.deleteStylesheets";

	static final String PARAM_FIND_PRE_STYLESHEET = "sapr3.conn.findPreStylesheet";

	static final String PARAM_FIND_POST_STYLESHEET = "sapr3.conn.findPostStylesheet";

	static final String PARAM_SELECT_ENTRIES_PRE_STYLESHEET = "sapr3.conn.selectEntriesPreStylesheet";

	static final String PARAM_SELECT_ENTRIES_POST_STYLESHEET = "sapr3.conn.selectEntriesPostStylesheet";

	static final String PARAM_GETNEXT_PRE_STYLESHEET = "sapr3.conn.getNextPreStylesheet";

	static final String PARAM_GETNEXT_POST_STYLESHEET = "sapr3.conn.getNextPostStylesheet";

	static final String PARAM_BOR_CLASS_NAME = "sapr3.conn.borObjName";

	static final String PARAM_RFC_FC = "sapr3.conn.rfcFC";

	static final String SAP_FC_PARAM_CLIENT = "client";

	static final String SAP_FC_PARAM_USER = "user";

	static final String SAP_FC_PARAM_PASSWD = "passwd";

	static final String SAP_FC_PARAM_SYSNR = "sysnr";

	static final String SAP_FC_PARAM_ASHOST = "ashost";

	static final String SAP_FC_PARAM_GWHOST = "gwhost";

	static final String SAP_FC_PARAM_TRACE = "trace";

	static final String SAP_FC_PARAM_OPTIONAL = "optional";

	/**
	 * Disabled.
	 */
	private ConfigurationNames() {
		super();
	}
}
