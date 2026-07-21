/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.http.jetty.listener.internal.impl;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

import com.ibm.di.http.jetty.listener.internal.HttpSessionListenerProvider;

/**
 * Dispatches events to the {@link HttpSessionListener}s registered using an
 * {@link HttpSessionListenerProvider}. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class HttpSessionEventsDispatcher implements HttpSessionListener, ServiceListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private final BundleContext ctx;

	public HttpSessionEventsDispatcher() {
		try {
			ctx = FrameworkUtil.getBundle(HttpSessionEventsDispatcher.class).getBundleContext();
			if (ctx != null) {
				ctx.addServiceListener(this, "(" + Constants.OBJECTCLASS + "="
						+ HttpSessionListenerProvider.class.getCanonicalName() + ")");
			}
		} catch (InvalidSyntaxException e) {
			throw new InternalError();
		}
	}

	private List<HttpSessionListenerProvider> providers = new LinkedList<HttpSessionListenerProvider>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http
	 * .HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
		synchronized (providers) {
			for (HttpSessionListenerProvider prov : providers) {
				prov.getListener().sessionCreated(se);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet
	 * .http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent se) {
		synchronized (providers) {
			for (HttpSessionListenerProvider prov : providers) {
				prov.getListener().sessionDestroyed(se);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
	 * ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
			synchronized (providers) {
				providers.add((HttpSessionListenerProvider) ctx.getService(event.getServiceReference()));
			}
			break;
		case ServiceEvent.UNREGISTERING:
			synchronized (providers) {
				providers.remove((HttpSessionListenerProvider) ctx.getService(event.getServiceReference()));
			}
			break;
		}
	}
}
