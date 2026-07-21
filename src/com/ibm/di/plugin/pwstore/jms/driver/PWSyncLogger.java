/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.pwstore.jms.driver;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.systemqueue.driver.JMSDriverLog;

public class PWSyncLogger implements JMSDriverLog {
	private static final String PREFIX = "JMSDriver";

	private PWSyncLog log = null;

	public PWSyncLogger(PWSyncLog log) {
		this.log = log;
	}

	public void logErrorAndThrowException(String msg) throws Exception {
		Exception e = new Exception(msg);
		log.error(PREFIX, msg, e);
		throw e;
	}

	public void logError(String msg) {
		log.error(PREFIX, msg);
	}

	public void logErrorAndThrowException(String msg, Exception e)
			throws Exception {
		log.error(PREFIX, msg, e);
		throw e;
	}

	public void logDebug(String msg) {
		log.debug(PREFIX, msg);

	}

	public void logInfo(String msg) {
		log.info(PREFIX, msg);
	}

	public void logWarn(String msg) {
		log.warn(PREFIX, msg);
	}

}
