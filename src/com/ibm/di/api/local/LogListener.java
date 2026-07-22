/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import java.util.EventListener;

import com.ibm.di.api.DIException;

/**
 * 
 * This listener listens for log events.
 * 
 */
public interface LogListener extends EventListener {
	
	/**
	 * Called right after the specified message is logged.
	 * 
	 * @param aMessage
	 *            the message text
	 * @throws DIException
	 *             If an error is encountered.
	 */
	public void messageLogged(String aMessage) throws DIException;

}
