/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;

/**
 * The concrete implementation of the connectors' findEntry() method. It's
 * execute method performs a XSL transform using the {@link #getPreCallXsl()},
 * then execute an RFC using the result, then transform the RFC XML result using
 * the {@link #getPostCallXsl()}.
 * 
 */
final class XslFindMethod extends XslFindMethodBase {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Create a new find method.
	 * 
	 * @param cfg
	 *            The connector configuration. The pre and post XSL style sheet
	 *            file names must be configured.
	 */
	public XslFindMethod(Configuration cfg) {
		super(cfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.FindMethod#getPreCallXsl()
	 */
	public File getPreCallXsl() {
		return (getConfig()
				.getParamAsFile(ConfigurationNames.PARAM_FIND_PRE_STYLESHEET));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.connector.sapr3.user.FindMethod#getPostCallXsl()
	 */
	public File getPostCallXsl() {
		return (getConfig()
				.getParamAsFile(ConfigurationNames.PARAM_FIND_POST_STYLESHEET));
	}

}
