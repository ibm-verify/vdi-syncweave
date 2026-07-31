/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.server.ResourceHash;

/**
 * This class is used for static sharing of IdML books.
 */
public class ItdiBookMapper {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The properties file containing messages.
	 */
	private static final String PROPERTIES_FILE = "openidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The map for storing books.
	 */
	private static Map<String, ItdiBook> bookMap = new HashMap<String, ItdiBook>();

	/**
	 * The map for storing availability of the Itdi books.
	 */
	private static Map<String, Boolean> bookLockMap = new HashMap<String, Boolean>();

	/**
	 * Returns the ItdiBook object corresponding to the given book name. This
	 * method does not take into account if the book is exclusively locked.
	 * 
	 * @param bookName
	 *            the name of the needed book.
	 * @return the needed ItdiBook object.
	 */
	public static synchronized ItdiBook getBook(String bookName) {
		return bookMap.get(bookName);
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
	public static synchronized ItdiBook getExclusiveBook(String bookName) throws Exception {
		Boolean isLocked = bookLockMap.get(bookName);
		if (isLocked != null && isLocked) {
			throw new Exception(resHash.getString("ITDI.BOOK.CURRENTLY.IN.USE", bookName));
		}
		bookLockMap.put(bookName, true);
		ItdiBook book = bookMap.get(bookName);
		if (book == null) {
			book = new ItdiBook(bookName);
			bookMap.put(bookName, book);
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
	public static synchronized ItdiBook freeBook(String bookName) {
		bookLockMap.remove(bookName);
		return bookMap.remove(bookName);
	}

}
