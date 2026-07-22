/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.naming.InvalidNameException;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;
import com.ibm.di.nls.L10N;
import com.ibm.di.nls.L10NFactory;

/**
 * Implementation of Atom storage.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class AtomStorageImpl implements AtomStorage {

	public static final L10N L10N = L10NFactory.getInstance(AtomStorageImpl.class);

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * Storage for binary data.
	 */
	private RawDataStorage rawDataStorage;

	/**
	 * Serializer for Atom entry objects.
	 */
	private Serializer<AtomEntry> entrySerializer;

	/**
	 * Serializer for Atom feed objects.
	 */
	private Serializer<AtomFeed> feedSerializer;

	/**
	 * Create Atom storage.
	 * 
	 * @param rawDataStorage
	 *            Storage for binary data.
	 * @param entrySerializer
	 *            XML serializer for Atom entry objects.
	 * @param feedSerializer
	 *            XML serializer for Atom feed objects.
	 */
	public AtomStorageImpl(RawDataStorage rawDataStorage, Serializer<AtomEntry> entrySerializer, Serializer<AtomFeed> feedSerializer) {
		this.rawDataStorage = rawDataStorage;
		this.entrySerializer = entrySerializer;
		this.feedSerializer = feedSerializer;
	}

	/**
	 * {@inheritDoc}
	 */
	public AtomEntry getAtomEntry(String key) throws StorageException, InvalidNameException {
		return get(key, entrySerializer);
	}

	/**
	 * {@inheritDoc}
	 */
	public AtomFeed getAtomFeed(String key) throws StorageException, InvalidNameException {
		return get(key, feedSerializer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(String key, AtomEntry entry) throws StorageException, InvalidNameException {
		put(key, entry, entrySerializer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(String key, AtomFeed feed) throws StorageException, InvalidNameException {
		put(key, feed, feedSerializer);
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(String key) throws StorageException, InvalidNameException {
		rawDataStorage.remove(key);
	}

	/**
	 * Read value from the storage.
	 * 
	 * @param <T>
	 *            The type of the value.
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @param serializer
	 *            Serializer for the value.
	 * @return The value associated with the specified key.
	 * @throws StorageException
	 *             Storage problem.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	private <T> T get(String key, Serializer<T> serializer) throws StorageException, InvalidNameException {
		byte[] data = rawDataStorage.get(key);
		T value;
		if (data != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			value = serializer.deserialize(bis);
		} else {
			value = null;
		}
		return value;
	}

	/**
	 * Put value in the storage.
	 * 
	 * @param <T>
	 *            The type of the value.
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @param value
	 *            Value.
	 * @param serializer
	 *            Serializer for the node value.
	 * @throws StorageException
	 *             Storage problem.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	private <T> void put(String key, T value, Serializer<T> serializer) throws StorageException, InvalidNameException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serializer.serialize(value, bos);
		rawDataStorage.put(key, bos.toByteArray());
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() throws StorageException {
		rawDataStorage.clear();
	}

}
