/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

import java.io.File;

/**
 * The class represents the main method implementation for an SyncWeave connector
 * putEntry().
 * 
 */
final class XslAddMethod extends XslWriteMethod {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String XSL_FILES_PARAM_NAME = ConfigurationNames.PARAM_PUT_STYLESHEET_LIST;

	/**
	 * Create new Add method.
	 * 
	 * @param config
	 *            Should have XSL files configured for putEntry() scenario.
	 * @throws IllegalArgumentException
	 *             if <code>config</code> is <code>null</code>.
	 */
	XslAddMethod(Configuration config) {
		super(config);
	}

	/**
	 * Gets the list of configured XSL files from the config set during
	 * construction.
	 * 
	 * @see com.ibm.di.connector.sapr3.user.XslWriteMethod#getXslFiles()
	 */
	File[] getXslFiles() throws ConnectorMethodException {
		File[] result = getConfig().getParamAsFileArray(
				XslAddMethod.XSL_FILES_PARAM_NAME);
		if (result == null) {
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_BOR_0005);
			getConfig().getLog().logwarn(msg);
			throw new ConnectorMethodException(msg);
		}

		return result;
	}

}
