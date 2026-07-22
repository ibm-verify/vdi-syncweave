/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
 package com.ibm.di.systemqueue.driver;

public interface JMSDriverLog {

	public void logErrorAndThrowException(String msg) throws Exception;

	public void logErrorAndThrowException(String msg, Exception e)
			throws Exception;

	public void logError(String msg);

	public void logDebug(String msg);

	public void logInfo(String msg);

	public void logWarn(String msg);
}
