/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.exception;

/**
 * TPAE IF Connector's excedent size exception. Thrown to indicate that
 * the value of a text attribute exceeds its size.
 * 
 * @since 7.1
 */
public class MxConnExcedentSizeException extends MxConnSchemaException {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = 1;

	private final String attributeName;

	private final int maxSize;

	private final int size;

	/**
	 * Constructs a new {@link MxConnExcedentSizeException}.
	 * 
	 * @param msg
	 *            exception message
	 * @param attributeName
	 *            name of the attribute
	 * @param maxSize
	 *            maximum size of the attribute
	 * @param size
	 *            current size of the attribute
	 * @see Exception#Exception(String)
	 */
	public MxConnExcedentSizeException(final String msg, final String attributeName, final int maxSize, final int size) {
		super(msg, attributeName, maxSize, size);
		this.attributeName = attributeName;
		this.maxSize = maxSize;
		this.size = size;
	}

	/**
	 * Returns the attribute name.
	 * 
	 * @return attribute name
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the maximum size of the attribute.
	 * 
	 * @return maximum size of the attributes
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Returns the current size of the attribute.
	 * 
	 * @return current size of the attributes
	 */
	public int getSize() {
		return size;
	}
}
