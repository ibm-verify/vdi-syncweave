/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import java.io.InputStream;
import java.io.OutputStream;

import com.ibm.di.jaxrs.storage.atom.StorageException;

/**
 * 
 * Serializer of Java objects.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface Serializer<T> {

	/**
	 * Serialize Java object to the specified stream.
	 * 
	 * @param obj
	 *            Java object to serialize.
	 * @param out
	 *            Destination of the serialized data.
	 * @throws StorageException
	 *             Serialization problem.
	 */
	void serialize(T obj, OutputStream out) throws StorageException;

	/**
	 * Deserialize Java object from the specified input stream.
	 * 
	 * @param inp
	 *            Source of serialized data.
	 * @return Java object.
	 * @throws StorageException
	 *             Deserialization problem.
	 */
	T deserialize(InputStream inp) throws StorageException;
}
