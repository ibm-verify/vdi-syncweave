/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.remote;

import java.rmi.Remote;
import java.util.EventListener;

/**
 * This is a marker interface only. Listener from the Remote Server API should
 * implement it.
 * 
 * @since 7.1
 */
public interface RemoteListener extends EventListener, Remote {

}
