/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.*;

public class StreamConnection {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	InputStream is;

	OutputStream os;

	InputStreamReader input;

	OutputStreamWriter output;

	public StreamConnection(InputStream is, OutputStream os) {
		input = new InputStreamReader(is);
		output = new OutputStreamWriter(os);
		this.is = is;
		this.os = os;
	}

	public int read() throws IOException {
		return input.read();
	}

	public int read(byte[] buf) throws IOException {
		return is.read(buf);
	}

	public int read(byte[] buf, int off, int length) throws IOException {
		return is.read(buf, off, length);
	}

	public void write(char[] buf) throws IOException {
		output.write(buf);
		output.flush();
	}

	public void write(byte[] buf) throws IOException {
		os.write(buf);
	}

	public void write(ByteBuffer buf) throws IOException {
		os.write(buf.getBuffer(), 0, buf.length());
	}
}
