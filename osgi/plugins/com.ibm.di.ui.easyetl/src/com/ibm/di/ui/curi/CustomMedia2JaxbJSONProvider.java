/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.curi;

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

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

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
@Consumes( { CustomMedia2JaxbJSONProvider.MT_TIP_JSON })
@Produces( { CustomMedia2JaxbJSONProvider.MT_TIP_JSON })

public class CustomMedia2JaxbJSONProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;
	
	public final static String MT_TIP_JSON = "application/vnd.ibm.com.tivolidis.json";
	
	private JacksonJaxbJsonProvider provider;

	public CustomMedia2JaxbJSONProvider() {
		provider = new JacksonJaxbJsonProvider();
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
