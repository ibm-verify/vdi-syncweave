/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.http.jetty.listener.internal.impl;

import java.lang.reflect.Method;
import java.util.Dictionary;

import org.eclipse.equinox.http.jetty.JettyCustomizer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.HttpConfiguration;
//import org.mortbay.jetty.servlet.Context;

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
public class HttpSessionCleanupEnabler extends JettyCustomizer {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.equinox.http.jetty.JettyCustomizer#customizeContext(java.
	 * lang.Object, java.util.Dictionary)
	 */
	@Override
	public Object customizeContext(Object context, Dictionary settings) {
		if (context instanceof ServletContextHandler) {
			((ServletContextHandler) context).addEventListener(new HttpSessionEventsDispatcher());

			// -- Default session id
			((ServletContextHandler) context).setInitParameter("org.eclipse.jetty.servlet.SessionCookie", "SDISessionID");
			
			// -- for any custom params from property file
			String props = System.getProperty("jetty.params");
			if(props != null) {
				for(String prop : props.split(",")) {
					String value = System.getProperty(prop);
					if(value != null) {
						System.out.println("Jetty: " + prop + "=" + value);
						((ServletContextHandler) context).setInitParameter(prop, value);
					}
				}
			}
			
			
		} else {
// Try to implement this with reflection.
// Not sure if it is needed, since the mortbay classes are outdated.
//			if (context instanceof Context) {
//				((Context) context).getSessionHandler().addEventListener(new HttpSessionEventsDispatcher());
			try {
				Method m = context.getClass().getMethod("getSessionHandler");
				Object obj = m.invoke(context);
				m = obj.getClass().getMethod("addEventListener", HttpSessionEventsDispatcher.class);
				m.invoke(obj, new HttpSessionEventsDispatcher());
			} catch (Exception e) {
				// users cannot recover from this so no point in providing an
				// explanation.
				throw new InternalError();
			}
		}
		return context;
	}
}
