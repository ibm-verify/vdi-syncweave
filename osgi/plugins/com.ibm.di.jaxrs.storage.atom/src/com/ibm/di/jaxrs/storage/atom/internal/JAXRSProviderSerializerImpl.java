/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import com.ibm.di.jaxrs.storage.atom.StorageException;

/**
 * Adapter for the JAX-RS provider concept to a serializer.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class JAXRSProviderSerializerImpl<T> implements Serializer<T> {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * JAX-RS entity reader used to deserialize objects.
	 */
	private MessageBodyReader<T> reader;

	/**
	 * JAX-RS entity writer used to serialize objects.
	 */
	private MessageBodyWriter<T> writer;

	/**
	 * Media type which defines the serialization format.
	 */
	private MediaType mediaType;

	/**
	 * Java type of objects which the serializer handles.
	 */
	private Class<T> type;

	/**
	 * Empty array of annotations.
	 */
	private static final Annotation[] noAnnotations = new Annotation[0];

	/**
	 * Empty collection of HTTP headers.
	 */
	private static final MultivaluedMap<String, String> noHTTPHeaders = null;

	/**
	 * Empty collection of HTTP headers.
	 */
	private static final MultivaluedMap<String, Object> noHTTPHeaderObjects = null;

	/**
	 * Create a serializer which adapts JAX-RS providers obtained through the
	 * specified providers interface.
	 * 
	 * @param providers
	 *            Interface to the providers of a JAX-RS implementation.
	 * @param type
	 *            Java type of the object which the serializer handles.
	 * @param mediaType
	 *            Media type which defines the serialization format.
	 */
	public JAXRSProviderSerializerImpl(Providers providers, Class<T> type, MediaType mediaType) {
		this(providers.getMessageBodyReader(type, type, null, mediaType), providers.getMessageBodyWriter(type, type, null,
				mediaType), type, mediaType);
	}

	/**
	 * Create a serializer which adapts the specified JAX-RS reader and writer.
	 * 
	 * @param reader
	 *            JAX-RS entity reader used to deserialize objects.
	 * @param writer
	 *            JAX-RS entity writer used to serialize objects.
	 * @param type
	 *            Java type of the object which the serializer handles.
	 * @param mediaType
	 *            Media type which defines the serialization format.
	 */
	public JAXRSProviderSerializerImpl(MessageBodyReader<T> reader, MessageBodyWriter<T> writer, Class<T> type, MediaType mediaType) {
		this.reader = reader;
		this.writer = writer;
		this.mediaType = mediaType;
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	public T deserialize(InputStream inp) throws StorageException {
		try {
			return reader.readFrom(type, type, noAnnotations, mediaType, noHTTPHeaders, inp);
		} catch (Exception ex) {
			throw new StorageException(AtomStorageImpl.L10N.getString("JAXRS.PROVIDER.SERIALIZER.READ.ERROR", ex), ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void serialize(T obj, OutputStream out) throws StorageException {
		try {
			writer.writeTo(obj, type, type, noAnnotations, mediaType, noHTTPHeaderObjects, out);
		} catch (Exception ex) {
			throw new StorageException(AtomStorageImpl.L10N.getString("JAXRS.PROVIDER.SERIALIZER.WRITE.ERROR", new Object[] { obj,
					ex }), ex);
		}
	}

}
