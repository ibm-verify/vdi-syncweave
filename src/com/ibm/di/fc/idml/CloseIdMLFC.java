/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This Function is used to close explicitly the used IdML book, if it is not
 * closed already. Depending on the type of the closed book, it will return
 * either its content, represented as a String (for in-memory IdMLs), or the
 * full path of the IdML file (for books stored as files).
 */
public class CloseIdMLFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "closeidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The name of book name parameter from FC's configuration panel.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * The name of the book used by this Component.
	 */
	private String bookName;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object object) throws Exception {
		super.initialize(object);

		bookName = getStringParameter(PARAM_BOOK_NAME);
		printDebugMessage("CLOSE.IDML.FC.BOOKNAME.INITIALIZED", new Object[] { bookName });
	}

	/**
	 * The FC updates the name of the used IdML book (if it has been modified),
	 * and a attempts to close the book. If the book has already been closed or
	 * never opened a message is displayed.
	 * 
	 * @param obj
	 *            the work entry passed to the FC.
	 * @return an entry object, containing the {@link IdMLConstants#BOOK_ATTR}
	 *         attribute.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object obj) throws Exception {
		if (!(obj instanceof Entry)) {
			throw new Exception(resHash.getString("CLOSE.IDML.FC.EXPECTS.ENTRY"));
		}

		Entry work = (Entry) obj;

		String newBookName = work.getString(IdMLConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !bookName.equals(newBookName)) {
			// the Component should work with a different book
			printDebugMessage("CLOSE.IDML.FC.BOOKNAME.OVERRIDDEN", new Object[] { bookName, newBookName });
			bookName = newBookName;
		}

		if (bookName == null) {
			throw new Exception(resHash.getString("CLOSE.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		Entry returnEntry = new Entry();
		ItdiBook book = ItdiBookMapper.getBook(bookName);
		if (book != null && book.isOpened()) {
			book.close();
			printDebugMessage("CLOSE.IDML.FC.BOOK.CLOSED", new Object[] { bookName });

			String idmlFileName = book.getFileName();
			if (book.isInMemory()) {
				returnEntry.setAttribute(IdMLConstants.BOOK_ATTR, book.getContents());
				book.reset();
			} else {
				returnEntry.setAttribute(IdMLConstants.BOOK_ATTR, idmlFileName);
			}

		} else {
			printDebugMessage("CLOSE.IDML.FC.BOOK.ALREADY.CLOSED", new Object[] { bookName });
		}

		return returnEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param parameterName
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 */
	private String getStringParameter(String parameterName) {
		String parameter = (String) getParam(parameterName);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}

	/**
	 * Prints a debug message if debug mode for the Components is enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            place holder for debug messages
	 */
	private void printDebugMessage(String msgKey, Object[] params) {
		if (params == null || params.length == 0) {
			debug(resHash.getString(msgKey));
		} else if (params.length == 1) {
			debug(resHash.getString(msgKey, params[0]));
		} else {
			debug(resHash.getString(msgKey, params));
		}
	}
}
