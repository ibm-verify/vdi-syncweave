/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

/**
 * <p>
 * This interface is for internal use only. Users must not rely on it.
 * </p>
 * 
 * <p>
 * Interface for entities that support listener notification.
 * </p>
 * 
 * @param <ListenerT>
 *            Type of listener.
 * 
 * @since 7.0
 */
public interface Listenable<ListenerT> {

	/**
	 * Register new listener.
	 * 
	 * @param listener
	 *            Listener.
	 */
	void addListener(ListenerT listener);

	/**
	 * Unregister listener.
	 * 
	 * @param listener
	 *            Registered listener.
	 * @return the actual listener being registered. This is useful when the
	 *         passed in instance is only used for identification and the actual
	 *         listener needs to be properly disposed of.
	 */
	ListenerT removeListener(ListenerT listener);
}
