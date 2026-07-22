/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom.internal;

import javax.naming.InvalidNameException;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;

import com.ibm.di.jaxrs.storage.atom.AtomStorage;
import com.ibm.di.jaxrs.storage.atom.StorageException;

/**
 * This class is used to represent an AtomStorage that is disabled. All methods
 * return the default (<code>null</code>) values. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class DisabledAtomStorage implements AtomStorage {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.storage.AtomStorage#clear()
	 */
	public void clear() throws StorageException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.storage.AtomStorage#getAtomEntry(java.lang.String)
	 */
	public AtomEntry getAtomEntry(String key) throws StorageException, InvalidNameException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.storage.AtomStorage#getAtomFeed(java.lang.String)
	 */
	public AtomFeed getAtomFeed(String key) throws StorageException, InvalidNameException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.storage.AtomStorage#put(java.lang.String,
	 * org.apache.wink.common.model.atom.AtomEntry)
	 */
	public void put(String key, AtomEntry value) throws StorageException, InvalidNameException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.storage.AtomStorage#put(java.lang.String,
	 * org.apache.wink.common.model.atom.AtomFeed)
	 */
	public void put(String key, AtomFeed value) throws StorageException, InvalidNameException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.storage.AtomStorage#remove(java.lang.String)
	 */
	public void remove(String key) throws StorageException, InvalidNameException {
	}
}
