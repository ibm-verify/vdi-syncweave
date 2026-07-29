/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This Component is used to split IdML files based either on the count of
 * artifacts (CIs or Relationships) added to the file or on its size. Thus it is
 * not applicable for in-memory IdMLs.
 */
public class RollingIdMLFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "rollingidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The size of a kilobyte.
	 */
	private static final int KB = 1024;

	/**
	 * Depending whether the FC has split the IdML file this attribute either
	 * contains the name of the closed IdML file or <b>null</b> (if no splitting
	 * has occurred ).
	 */
	private static final String BOOK_PART_NAME_ATTR = "$idmlFileName";

	/**
	 * The name of the artifact count parameter from FC's configuration.
	 */
	private static final String PARAM_ARTIFACT_COUNT = "artifactCount";

	/**
	 * The name of the file size parameter from FC's configuration.
	 */
	private static final String PARAM_FILE_SIZE = "fileSize";

	/**
	 * The name of the book name parameter from FC's configuration.
	 */
	private static final String PARAM_BOOK_NAME = "bookName";

	/**
	 * The name of the IdML book, this component works with.
	 */
	private String bookName;

	/**
	 * The maximum artifact count after which the FC will attempt the split the
	 * IdML file.
	 */
	private long artifactCount;

	/**
	 * The maximum size of the IdML file, after which the FC will attempt to
	 * split it.
	 */
	private long fileSize;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object object) throws Exception {
		super.initialize(object);

		// Read configuration parameters
		bookName = getStringParameter(PARAM_BOOK_NAME);
		printDebugMessage("ROLLING.IDML.FC.BOOKNAME.INITIALIZED", new Object[] { bookName });

		String artifactCountString = getStringParameter(PARAM_ARTIFACT_COUNT);
		if (artifactCountString != null) {
			artifactCount = Long.parseLong(artifactCountString);
			printDebugMessage("ROLLING.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_ARTIFACT_COUNT, artifactCount });
		} else {
			throw new Exception(resHash.getString("ROLLING.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_ARTIFACT_COUNT));
		}

		String fileSizeString = getStringParameter(PARAM_FILE_SIZE);
		if (fileSizeString != null) {
			fileSize = Long.parseLong(fileSizeString) * KB;
			printDebugMessage("ROLLING.IDML.FC.PARAMETER.INITIALIZED", new Object[] { PARAM_FILE_SIZE, fileSize });
		} else {
			throw new Exception(resHash.getString("ROLLING.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_FILE_SIZE));
		}

	}

	/**
	 * The FC checks if either the IdML file size, or the count of the contained
	 * attributes exceeds the configured limit. If so it splits the IdML file
	 * (closes and reopens it) and returns the full path to the already closed
	 * IdML. If none of the splitting conditions are reached, the FC skips the
	 * splitting operation.
	 * 
	 * @param obj
	 *            the work entry passed to the FC.
	 * @return an entry object, containing the {@link #BOOK_PART_NAME_ATTR}
	 *         attribute.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Object perform(Object obj) throws Exception {
		if (!(obj instanceof Entry)) {
			throw new Exception(resHash.getString("ROLLING.IDML.FC.EXPECTS.ENTRY"));
		}

		Entry work = (Entry) obj;

		ItdiBook book = null;

		// The name of the IdML book has been overridden.
		String newBookName = work.getString(IdMLConstants.BOOK_NAME_ATTR);
		if (newBookName != null && !bookName.equals(newBookName)) {
			// The Component should work with a different book
			bookName = newBookName;
			printDebugMessage("ROLLING.IDML.FC.BOOKNAME.OVERRIDDEN", new Object[] { bookName, newBookName });
		}

		if (bookName == null) {
			throw new Exception(resHash.getString("ROLLING.IDML.FC.PARAMETER.NOT.PROVIDED", PARAM_BOOK_NAME));
		}

		Entry returnEntry = new Entry();
		// Check if the book is statically shared
		book = ItdiBookMapper.getBook(bookName);
		if (book != null && book.isOpened()) {
			// The book has been opened

			if (!book.isInMemory()) {
				String idmlFileName = null;
				if ((artifactCount != 0 && book.getArtifactCount() >= artifactCount)
						|| (fileSize != 0 && book.getFileSize() >= fileSize)) {
					idmlFileName = book.split();
				}
				returnEntry.setAttribute(BOOK_PART_NAME_ATTR, idmlFileName);
			} else {
				throw new Exception(resHash.getString("ROLLING.IDML.FC.INMEMORY.NOT.ALLOWED"));
			}
		} else {
			printDebugMessage("ROLLING.IDML.FC.BOOK.ALREADY.CLOSED", new Object[] { bookName });
		}

		return returnEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
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
