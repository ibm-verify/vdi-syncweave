/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.provider;

import static com.ibm.di.api.rest.internal.AppConstants.*;

import com.ibm.di.web.common.atom.AtomText;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.ibm.di.api.bind.Entry;
import com.ibm.di.api.bind.Event;
import com.ibm.di.api.bind.Listener;
import com.ibm.di.api.bind.TombstoneData;
import com.ibm.di.api.bind.TransportChannel;
import com.ibm.di.config.bind.NamedBinding;
import com.ibm.di.config.bind.ObjectFactory;
import com.ibm.di.jaxrs.jackson.internal.BeanShortNameIdResolver;
import com.ibm.di.jaxrs.jackson.internal.PolymorphicJacksonProvider;
import com.ibm.di.model.descriptor.ComponentDescriptor;

/**
 * Delegates to the Jackson JSON reader/writer. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Provider
@Consumes( { MT_SERVER_INFO_JSON, MT_SERVER_CONTROL_JSON, MT_SERVER_NOTIFY_JSON, MT_COMPONENT_JSON, MT_API_CONFIG_JSON,
		MT_CONFIG_JSON, MT_LISTENER_JSON, MT_PROPERTY_STORE_JSON, MT_ASSEMBLY_LINE_JSON, MT_ENTRY_JSON, MT_TOMBSTONE_JSON })
@Produces( { MT_ATOM_APP_SRVC_JSON, MT_SERVER_INFO_JSON, MT_SERVER_CONTROL_JSON, MT_SERVER_NOTIFY_JSON, MT_COMPONENT_JSON,
		MT_API_CONFIG_JSON, MT_CONFIG_JSON, MT_LISTENER_JSON, MT_PROPERTY_STORE_JSON, MT_ASSEMBLY_LINE_JSON, MT_ENTRY_JSON,
		MT_TOMBSTONE_JSON })
public class CustomMediaTypeToJaxbJSONProviderDelegator implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	private PolymorphicJacksonProvider provider;

	public CustomMediaTypeToJaxbJSONProviderDelegator() {
		BeanShortNameIdResolver resolver = new BeanShortNameIdResolver(new Class[] { NamedBinding.class, ComponentDescriptor.class,
				Entry.class, Listener.class, TransportChannel.class, Event.class, TombstoneData.class }, new Class[] {
				ObjectFactory.class, com.ibm.di.api.bind.ObjectFactory.class, com.ibm.di.model.descriptor.ObjectFactory.class });
		provider = new PolymorphicJacksonProvider();
	}

	public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return provider.getSize(arg0, arg1, arg2, arg3, arg4);
	}

	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return provider.isWriteable(arg0, arg1, arg2, MediaType.APPLICATION_JSON_TYPE);
	}

	public void writeTo(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
		provider.writeTo(arg0, arg1, arg2, arg3, MediaType.APPLICATION_JSON_TYPE, arg5, arg6);
	}

	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return provider.isReadable(arg0, arg1, arg2, arg3);
	}

	public Object readFrom(Class<Object> arg0, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
			InputStream arg5) throws IOException, WebApplicationException {
		return provider.readFrom(arg0, arg1, arg2, arg3, arg4, arg5);
	}
}
