/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

/**
 * This class wraps an array of strings around an object
 * 
 */
public class StringArrayWrapper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * String array to wrap.
	 */
	private String[] mStrings = null;

	/**
	 * Class constructor
	 */
	public StringArrayWrapper() {

	}

	/**
	 * This method sets the member variable
	 * 
	 * @param aStrings
	 *            array of strings
	 */
	public void setStrings(String[] aStrings) {
		mStrings = aStrings;
	}

	/**
	 * This method returns the member variable
	 * 
	 * @return array of strings
	 */
	public String[] getStrings() {
		return mStrings;
	}

}
