/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

/**
 * This class wraps an array of {@link Byte} objects into one object
 * 
 */
public class ByteArrayWrapper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Byte array to wrap.
	 */
	private Byte[] mBytes = null;

	/**
	 * Class constructor
	 */
	public ByteArrayWrapper() {
	}

	/**
	 * Sets the member variable of the class
	 * 
	 * @param aBytes
	 *            array of Byte objects
	 */
	public void setBytes(Byte[] aBytes) {
		mBytes = aBytes;
	}

	/**
	 * Returns the member variable
	 * 
	 * @return an array of Byte objects
	 */
	public Byte[] getBytes() {
		return mBytes;
	}

}
