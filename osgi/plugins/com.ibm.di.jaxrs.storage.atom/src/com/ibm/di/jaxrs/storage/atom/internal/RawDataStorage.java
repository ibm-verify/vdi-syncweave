/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import javax.naming.InvalidNameException;

import com.ibm.di.jaxrs.storage.atom.StorageException;

/**
 * Storage for raw binary data.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface RawDataStorage {

	/**
	 * Get value from storage.
	 * 
	 * @param key
	 *            Key.
	 * @return Value.
	 * @throws StorageException
	 *             Storage problem.
	 * @throws InvalidNameException
	 *             The syntax of the key is invalid.
	 */
	byte[] get(String key) throws StorageException, InvalidNameException;

	/**
	 * Put value in storage.
	 * 
	 * @param key
	 *            Key.
	 * @param value
	 *            Value.
	 * @throws StorageException
	 *             Storage problem.
	 * @throws InvalidNameException
	 *             The syntax of the key is invalid.
	 */
	void put(String key, byte[] value) throws StorageException, InvalidNameException;

	/**
	 * Remove value from storage.
	 * 
	 * @param key
	 *            Key.
	 * @throws StorageException
	 *             Storage problem.
	 * @throws InvalidNameException
	 *             The syntax of the key is invalid.
	 */
	void remove(String key) throws StorageException, InvalidNameException;

	/**
	 * Clean up all data in the storage.
	 * 
	 * @throws StorageException
	 *             Storage problem - I/O error.
	 */
	void clear() throws StorageException;
}
