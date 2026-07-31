/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.debug;

/**
 * This interface is used by the DebugClient to send debug events to listeners.
 *
 */
public interface DebugClientListener {

	public void handleEvent(DebugClientEvent event);
}
