/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.local;

import com.ibm.di.api.DIEvent;
import com.ibm.di.api.DIException;

import java.util.EventListener;

/**
 * 
 * This is a base EventListener for Server API events.<p>
 * In order to listen for events an implementation of this class
 * should be provided by the user. Then reference to this implementation could
 * be added/removed with some of these methods:
 * <code>
 * com.ibm.di.api.local.impl.SessionImpl.addEventListener()
 * com.ibm.di.api.local.impl.SessionImpl.removeEventListener()
 * </code>
 */
public interface DIEventListener extends EventListener {

	/**
	 * Handles a specified event.
	 * 
	 * @param aEvent
	 *            the event which needs to be handled.
	 * @throws DIException
	 *             if an error occurs while trying to handle the event.
	 */
	public void handleEvent(DIEvent aEvent) throws DIException;

}
