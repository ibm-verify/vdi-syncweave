/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// EncryptedWriter.java
//
//
//
package com.ibm.di.security;

import java.io.*;

public class EncryptedWriter extends BufferedWriter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SIGNATURE = "{AS ENCRYPTED}\r\n";

	SecurityCrypto ec;

	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	OutputStream output;

	String cipherAlgorithm;

	public EncryptedWriter(OutputStream stream) {
		super(new OutputStreamWriter(stream));
		output = stream;
	}

	// *
	public EncryptedWriter(Writer stream) {
		super(stream);
	}

	public EncryptedWriter(Writer writer, OutputStream stream) {
		super(writer);
		output = stream;
	}

	// */

	public void useKey(String keyPath) throws Exception {
		ec = new SecurityCrypto(keyPath, cipherAlgorithm);
	}

	public void setKey(SecurityCrypto key) {
		ec = key;
	}

	public void setAlgorithm(String cipherAlgorithm) {
		this.cipherAlgorithm = cipherAlgorithm;
	}

	public String getAlgorithm() {
		return cipherAlgorithm;
	}

	public void write(String str) throws IOException {
		getOutputStream().write(str.getBytes("UTF-8"));
	}

	public void newLine() throws IOException {
		write("\r\n");
	}

	public OutputStream getOutputStream() {
		if (bos.size() == 0) {
			try {
				bos.write(SIGNATURE.getBytes("UTF-8"));
			} catch (Exception ignore) {
			}
		}

		return bos;
	}

	public void close() throws IOException {
		byte[] obuf;
		byte[] vrfy;
		try {
			obuf = ec.encrypt(bos.toByteArray());
			// vrfy = ec.encrypt (SIGNATURE.getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IOException(ex.toString());
		}

		output.write(SIGNATURE.getBytes("UTF-8"));
		// output.write (vrfy);
		output.write(obuf);
		output.close();

	}
}
