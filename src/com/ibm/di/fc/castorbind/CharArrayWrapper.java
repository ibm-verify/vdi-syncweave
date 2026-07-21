/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

/**
 * This class wraps an array of characters around an objects
 * 
 */
public class CharArrayWrapper {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Character array to wrap.
	 */
	private char[] mCharacters = null;

	/**
	 * Class constructor
	 */
	public CharArrayWrapper() {
	}

	/**
	 * This method returns the member variable
	 * 
	 * @return an array of {@link Character} objects
	 */
	public Character[] getChars() {
		Character[] chars = null;
		if (mCharacters != null) {
			chars = new Character[mCharacters.length];
			for (int i = 0; i < mCharacters.length; i++) {
				chars[i] = Character.valueOf(mCharacters[i]);
			}
		}
		return chars;
	}

	/**
	 * This method sets the member variable
	 * 
	 * @param aChars
	 *            array of characters
	 */
	public void setCharacters(char[] aChars) {
		mCharacters = aChars;
	}

	/**
	 * This method returns the member variable
	 * 
	 * @return array of characters
	 */
	public char[] getCharacters() {
		return mCharacters;
	}

}
