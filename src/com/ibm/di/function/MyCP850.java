/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// MyCP850.java
//
//
//
package com.ibm.di.function;

public class MyCP850 {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public MyCP850() {
		// sun.io.ByteToCharConverter.getConverter (
	}

	public boolean canConvert(char ch) {
		if (ch >= 0xe000 && ch <= 0xe0ff) {
			return true;
		}

		return false;
	}

	/*
	 * This method converts the unicode to this font index.
	 */
	public int convert(char[] input, int inStart, int inEnd, byte[] output,
			int outStart, int outEnd) {
		int outIndex = outStart;
		for (int i = inStart; i < inEnd; i++) {
			char ch = input[i];
			if (ch >= 0xe000 && ch <= 0xe0ff) {
				if (outIndex >= outEnd)
					throw new IndexOutOfBoundsException();
				output[outIndex++] = (byte) (ch - 0xe000);
			}
		}
		return outIndex - outStart;
	}

	public String toString() {
		return "MyCP850";
	}
}
