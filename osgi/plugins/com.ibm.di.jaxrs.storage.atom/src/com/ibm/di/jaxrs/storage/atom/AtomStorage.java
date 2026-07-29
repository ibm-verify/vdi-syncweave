/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom;

import javax.naming.InvalidNameException;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;

/**
 * <p>
 * Storage for Apache Wink Atom objects.
 * </p>
 * 
 * <p>
 * Objects are organized in a map - each object (value) is identified by a
 * unique key. The storage implementation may impose restrictions on the syntax
 * of the keys. Violation of the expected syntax is signaled by a
 * <code>javax.naming.InvalidNameException</code>.
 * </p>
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public interface AtomStorage {

	/**
	 * Read an Atom entry from the storage.
	 * 
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @return The Atom entry associated with the specified key.
	 *         <code>null</code> if the value does not exist.
	 * @throws StorageException
	 *             Storage problem - I/O error or serialization error. Also
	 *             thrown when the value associated with the key is not an Atom
	 *             entry.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	AtomEntry getAtomEntry(String key) throws StorageException, InvalidNameException;

	/**
	 * Put an Atom entry in the storage.
	 * 
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @param value
	 *            Atom entry.
	 * @throws StorageException
	 *             Storage problem - I/O error or serialization error.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	void put(String key, AtomEntry value) throws StorageException, InvalidNameException;

	/**
	 * Read an Atom feed from the storage.
	 * 
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @return The Atom feed associated with the specified key.
	 *         <code>null</code> if the value does not exist.
	 * @throws StorageException
	 *             Storage problem - I/O error or serialization error. Also
	 *             thrown when the value associated with the key is not an Atom
	 *             feed.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	AtomFeed getAtomFeed(String key) throws StorageException, InvalidNameException;

	/**
	 * Put an Atom feed in the storage.
	 * 
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @param value
	 *            Atom feed.
	 * @throws StorageException
	 *             Storage problem - I/O error, XML serialization error.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
	 */
	void put(String key, AtomFeed value) throws StorageException, InvalidNameException;

	/**
	 * Remove a value. No error is reported if the value does not exist.
	 * 
	 * @param key
	 *            Key which identifies the value in the storage.
	 * @throws StorageException
	 *             Storage problem - I/O error.
	 * @throws InvalidNameException
	 *             The specified key violates the expected syntax.
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
