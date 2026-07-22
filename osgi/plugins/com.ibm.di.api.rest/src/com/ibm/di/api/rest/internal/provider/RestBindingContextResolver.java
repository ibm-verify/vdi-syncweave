/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.bind.ObjectFactory;

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
@Provider
public class RestBindingContextResolver implements ContextResolver<JAXBContext> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private JAXBContext restCtx;

	public RestBindingContextResolver() {
		try {
			restCtx = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName() + ":"
					+ com.ibm.di.model.descriptor.ObjectFactory.class.getPackage().getName(), RestBindingContextResolver.class
					.getClassLoader());
		} catch (JAXBException je) {
			throw new RuntimeException(je);
		}
	}

	public JAXBContext getContext(Class<?> type) {
		if (ObjectFactory.class.getPackage().equals(type.getPackage())
				|| com.ibm.di.model.descriptor.ObjectFactory.class.getPackage().equals(type.getPackage())) {
			return restCtx;
		}

		return null;
	}
}
