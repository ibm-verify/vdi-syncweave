/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.itregistry;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.server.ResourceHash;

/**
 * This class is used for static sharing of IT registry books.
 */
public class ITRegistryBookMapper {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The properties file containing messages.
	 */
	private static final String PROPERTIES_FILE = "inititregistryfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * A collection of the IT registry books.
	 */
	private static Map<String, ITRegistryBook> books = new HashMap<String, ITRegistryBook>();

	/**
	 * The map for storing availability of the IT registry books.
	 */
	private static Map<String, Boolean> booksLock = new HashMap<String, Boolean>();

	/**
	 * Returns an IT registry book from the mapper.
	 * 
	 * @param the
	 *            name of the book.
	 * @return the book.
	 */
	public static synchronized ITRegistryBook getBook(String name) {
		return books.get(name);
	}

	/**
	 * Set an IT registry book in the mapper.
	 * 
	 * @param name
	 *            name to be used as key.
	 * @param book
	 *            the book.
	 */
	public static void setBook(String name, ITRegistryBook book) {
		books.put(name, book);
	}

	/**
	 * If the book with the specified name is free it is returned. Otherwise an
	 * exception is thrown. If such a book does not exist at all it is created
	 * and passed to the caller.
	 * 
	 * @param bookName
	 *            the book name which we want exclusively.
	 * @return the requested book object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public static synchronized ITRegistryBook getExclusiveBook(String bookName) throws Exception {
		ITRegistryBook book = books.get(bookName);
		if (book != null) {
			Boolean isLocked = booksLock.get(bookName);
			if (isLocked == null || isLocked) {
				throw new Exception(resHash.getString("IT.REGISTRY.BOOK.CURRENTLY.IN.USE", bookName));
			} else {
				booksLock.put(bookName, true);
			}
		} else {
			book = new ITRegistryBook(bookName);
			books.put(bookName, book);
			booksLock.put(bookName, true);
		}
		return book;
	}

	/**
	 * Removes a specified book from the static mapper and returns it to the
	 * caller.
	 * 
	 * @param bookName
	 *            the name of the book to remove.
	 * @return the removed ItdiBook.
	 */
	public static synchronized ITRegistryBook freeBook(String bookName) {
		booksLock.remove(bookName);
		return books.remove(bookName);
	}

}
