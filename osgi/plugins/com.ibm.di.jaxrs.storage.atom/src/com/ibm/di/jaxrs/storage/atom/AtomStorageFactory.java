/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.storage.atom;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;

import com.ibm.di.jaxrs.storage.atom.internal.AtomStorageImpl;
import com.ibm.di.jaxrs.storage.atom.internal.DisabledAtomStorage;
import com.ibm.di.jaxrs.storage.atom.internal.JAXRSProviderSerializerImpl;
import com.ibm.di.jaxrs.storage.atom.internal.RawDataStorage;
import com.ibm.di.jaxrs.storage.atom.internal.RawDataStorageFileSystemImpl;
import com.ibm.di.jaxrs.storage.atom.internal.Serializer;

/**
 * Factory for creating Atom storage components.
 * 
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class AtomStorageFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * <p>
	 * Create an Atom storage component which uses the file system as a back-end
	 * (puts files in the specified persistence directory). The created storage
	 * component is not thread-safe.
	 * </p>
	 * 
	 * <p>
	 * The storage implementation imposes the following restrictions on the
	 * syntax of keys: Keys cannot be empty and must not contain curly brackets
	 * or back-slashes. When a key is separated into tokens using forward slash
	 * "/" as a separator, none of the tokens is allowed to be empty or "." or
	 * "..".
	 * </p>
	 * 
	 * @param providers
	 *            Interface to the providers of a JAX-RS implementation. Atom
	 *            objects will be serialized to XML and deserialized from XML
	 *            using the available JAX-RS providers.
	 * @param storageLocation
	 *            Storage location. Existing folder on the file system, where
	 *            the storage component will write its data. The folder must be
	 *            dedicated exclusively to the storage component. Do not alter
	 *            the content in any way.
	 * @throws StorageException
	 *             The storage location does not exist and cannot be created or
	 *             exists an is not a folder.
	 * 
	 * @return storage component
	 */
	public static AtomStorage createAtomStorage(Providers providers, String storageLocation) throws StorageException {
		final MediaType mediaType = MediaType.APPLICATION_ATOM_XML_TYPE;
		Serializer<AtomEntry> entrySerializer = new JAXRSProviderSerializerImpl<AtomEntry>(providers, AtomEntry.class, mediaType);
		Serializer<AtomFeed> feedSerializer = new JAXRSProviderSerializerImpl<AtomFeed>(providers, AtomFeed.class, mediaType);
		RawDataStorage storage = new RawDataStorageFileSystemImpl(new File(storageLocation));
		return new AtomStorageImpl(storage, entrySerializer, feedSerializer);
	}

	/**
	 * Creates a default AtomStorage which is not persisting anywhere. You can
	 * think of this storage as /dev/null. This might be useful when need a
	 * dummy object when insufficient parameters are provided for creating a
	 * real storage component.
	 * 
	 * @return an instance of a storage component.
	 */
	public static AtomStorage createAtomStorage() {
		return new DisabledAtomStorage();
	}
}
