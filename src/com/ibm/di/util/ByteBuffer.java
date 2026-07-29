/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ByteBuffer.java
//
//
//
package com.ibm.di.util;

import java.io.*;

public class ByteBuffer {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private byte[] buffer;

	private int index;

	private int readIndex;

	public ByteBuffer() {
		buffer = new byte[4096];
		index = 0;
	}

	public ByteBuffer(byte[] buffer) {
		this.buffer = new byte[4096];
		index = 0;
		append(buffer);
	}

	public void append(ByteBuffer buf) {
		byte[] src = buf.getBuffer();
		for (int i = 0; i < buf.length(); i++) {
			append(src[i]);
		}
	}

	public void append(char ch) {
		append((byte) ch);
	}

	public void append(byte ch) {
		if (index >= buffer.length)
			realloc();

		buffer[index++] = ch;
	}

	public void append(byte[] buf) {
		for (int i = 0; i < buf.length; i++)
			append(buf[i]);
	}

	public void append(int ch) {
		append((byte) ((ch >> 24) & 0xff));
		append((byte) ((ch >> 16) & 0xff));
		append((byte) ((ch >> 8) & 0xff));
		append((byte) ((ch & 0xff)));
	}

	public void append(Object p1) {
		append(p1.toString());
	}

	public void append(String p1) {
		for (int i = 0; i < p1.length(); i++)
			append((byte) p1.charAt(i));

		// Append NULL byte
		append((byte) 0);
	}

	private void realloc() {
		byte[] tmp = new byte[buffer.length * 2];

		for (index = 0; index < buffer.length; index++) {
			tmp[index] = buffer[index];
		}

		buffer = tmp;
	}

	public int length() {
		return index;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public byte[] getBytes() {
		byte[] b = new byte[index];
		copyBytes(buffer, b, 0, index);
		return b;
	}

	private void copyBytes(byte[] p1, byte[] p2, int off, int len) {
		for (int i = off; i < len; i++)
			p2[i] = p1[i];
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < index; i++)
			s.append((char) buffer[i]);
		return s.toString();
	}

	public void resetRead() {
		readIndex = 0;
	}

	public String nextCString() {
		StringBuffer str = new StringBuffer();
		while (buffer[readIndex] != 0) {
			str.append( (char) buffer[readIndex++] );
		}
		readIndex++;

		if (str.length() < 1)
			return null;
		else
			return str.toString();
	}

	public int nextByte() {
		return buffer[readIndex++];
	}

	public int nextInt() {
		int val = 0;

		val += (nextByte() << 24);
		val += (nextByte() << 16);
		val += (nextByte() << 8);
		val += nextByte();

		return val;
	}
}
