/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal;

import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.ibm.di.web.common.atom.AtomText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.http.jetty.listener.internal.HttpSessionListenerProvider;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class SessionCleaner implements HttpSessionListenerProvider, HttpSessionListener {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private final ServletContext sc;
	private final Logger log = LoggerFactory.getLogger(SessionCleaner.class);

	/**
	 * @param servletContext
	 */
	public SessionCleaner(ServletContext servletContext) {
		this.sc = servletContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.jaxrs.HttpSessionListenerProvider#getContext()
	 */
	public ServletContext getContext() {
		return sc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.jaxrs.HttpSessionListenerProvider#getListener()
	 */
	public HttpSessionListener getListener() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http
	 * .HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet
	 * .http.HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent se) {
		QueueSession qs = (QueueSession) se.getSession().getAttribute(QueueSession.class.getName());
		if (qs != null) {
			try {
				qs.close();
			} catch (JMSException e) {
				log.error(e.getMessage(), e);
			} finally {
				se.getSession().removeAttribute(QueueSession.class.getName());
			}
		}
	}
}
