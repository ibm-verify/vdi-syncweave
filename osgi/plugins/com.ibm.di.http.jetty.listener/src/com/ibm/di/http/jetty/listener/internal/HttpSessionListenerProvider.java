/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.http.jetty.listener.internal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Register an OSGi Service to provide an {@link HttpSessionListener}s to
 * receive the {@link HttpSessionEvent}s occurring within the Jetty server
 * 
 * @since 7.2
 */
public interface HttpSessionListenerProvider {

	/**
	 * @return the {@link ServletContext} for which {@link HttpSessionEvent}s
	 *         will be delevered.
	 * */
	public ServletContext getContext();

	/**
	 * @return the {@link HttpSessionListener} that will receive the
	 *         {@link HttpSessionEvent}.
	 */
	public HttpSessionListener getListener();
}
