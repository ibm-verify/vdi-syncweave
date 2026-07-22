/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.di.tp.server.handler.Initializer;
import com.ibm.di.tp.server.handler.error.SCMPExceptionMapper;
import com.ibm.di.tp.server.handler.node.TPNodeFeed;
import com.ibm.di.tp.server.model.config.ObjectFactory;

/**
 * This is the class representing a JAX-RS {@link ResourceConfig}. This is the entry
 * point in the JAX-RS framework. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TPServerApplication extends ResourceConfig {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final Logger log = LoggerFactory.getLogger("com.ibm.di.tp.server");

	public TPServerApplication() {
		// Register classes (per-request instances)
		register(Initializer.class);
		register(SCMPExceptionMapper.class);

		// Register singletons
		register(new TPNodeFeed());
		register(new CustomJAXBContextResolver());
	}

	public static Logger getLog() {
		return log;
	}

	@Provider
	public static class CustomJAXBContextResolver implements ContextResolver<JAXBContext> {

		private JAXBContext customCtx;

		public CustomJAXBContextResolver() {
			try {
				customCtx = new ObjectFactory().getSchemaLocationAwareJaxContext();
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			}
		}

		public JAXBContext getContext(Class<?> type) {
			if (ObjectFactory.class.getPackage().equals(type.getPackage())) {
				return customCtx;
			}
			return null;
		}
	}
}
