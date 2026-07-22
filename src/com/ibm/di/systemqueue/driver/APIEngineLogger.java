/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.systemqueue.driver;

import com.ibm.di.api.APIEngine;

public class APIEngineLogger implements JMSDriverLog {

	public void logErrorAndThrowException(String msg) throws Exception {
		APIEngine.logErrorAndThrowException(msg);
	}

	public void logError(String msg) {
		APIEngine.logError(msg);

	}

	public void logErrorAndThrowException(String msg, Exception e)
			throws Exception {
		APIEngine.logErrorAndThrowException(msg, e);
	}

	public void logDebug(String msg) {
		APIEngine.logDebug(msg);
	}

	public void logInfo(String msg) {
		APIEngine.logInfo(msg);
	}

	public void logWarn(String msg) {
		APIEngine.logWarn(msg);
	}
}
