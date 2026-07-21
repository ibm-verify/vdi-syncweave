/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.jackson.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * Configures the {@link JacksonJaxbJsonProvider} provider to support
 * polymorphic JAXB objects, by including additional data into the returned
 * serialized JSON payload. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Provider
@Consumes( { MediaType.APPLICATION_JSON, "text/json" })
@Produces( { MediaType.APPLICATION_JSON, "text/json" })
public class PolymorphicJacksonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private JacksonJaxbJsonProvider provider;

	public PolymorphicJacksonProvider() {
		this(null, null);
	}

	public PolymorphicJacksonProvider(ResolvableTypesFilter filter, TypeIdResolver res) {
		provider = new JacksonJaxbJsonProvider();
		provider.setMapper(getCustomObjectMapper(filter, res));
	}

	/**
	 * @param filter
	 * @return the configured ObjectMapper
	 */
	private static ObjectMapper getCustomObjectMapper(final ResolvableTypesFilter filter, TypeIdResolver res) {
		ObjectMapper mapper = new ObjectMapper();

		// Don't write useless information out.
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		
		// Configure to ignore unknown properties (like @type field sent by frontend)
		// This prevents "Unrecognized field" errors when deserializing JSON
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// Create annotation introspectors - cast to AnnotationIntrospector for Jackson 2.15.2 compatibility
		AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
		AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		AnnotationIntrospector pair = AnnotationIntrospector.pair(jaxbIntrospector, jacksonIntrospector);
		mapper.setAnnotationIntrospector(pair);

		// AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		// mapper.setAnnotationIntrospector(jacksonIntrospector);		
		
		if (res != null) {
			// Plug into the default resolving mechanism to annotate some of the
			// classes and provide polymorphic capabilities
			TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT) {
				private static final long serialVersionUID = 1L;
				
				public boolean useForType(JavaType type) {
					return !filter.isTypeResolvable(type);
				}
			}.init(JsonTypeInfo.Id.CUSTOM, res).inclusion(As.PROPERTY).typeProperty("@type");
			mapper.setDefaultTyping(typer);
		}

		return mapper;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class,
	 * java.lang.reflect.Type, java.lang.annotation.Annotation[],
	 * javax.ws.rs.core.MediaType)
	 */
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return provider.isReadable(arg0, arg1, arg2, arg3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
	 * java.lang.reflect.Type, java.lang.annotation.Annotation[],
	 * javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
	 * java.io.InputStream)
	 */
	public Object readFrom(Class<Object> arg0, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4,
			InputStream arg5) throws IOException {
		return provider.readFrom(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object,
	 * java.lang.Class, java.lang.reflect.Type,
	 * java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
		return provider.getSize(arg0, arg1, arg2, arg3, arg4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class,
	 * java.lang.reflect.Type, java.lang.annotation.Annotation[],
	 * javax.ws.rs.core.MediaType)
	 */
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return provider.isWriteable(arg0, arg1, arg2, arg3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object,
	 * java.lang.Class, java.lang.reflect.Type,
	 * java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
	 * javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	public void writeTo(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException {
		provider.writeTo(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}
}
