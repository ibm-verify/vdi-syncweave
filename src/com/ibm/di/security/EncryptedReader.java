/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// EncryptedReader.java
//
//
//
package com.ibm.di.security;

import com.ibm.di.server.ResourceHash;
import java.io.*;

public class EncryptedReader extends BufferedReader {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	SecurityCrypto ec;

	InputStream input;

	BufferedReader reader;

	String cipherAlgorithm;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public EncryptedReader(InputStream stream) {
		super(new InputStreamReader(stream));
		input = stream;
	}

	public EncryptedReader(Reader stream) {
		super(stream);
	}

	public InputStream getInputStream() throws Exception {
		String sig = EncryptedWriter.SIGNATURE;

		for (int i = 0; i < sig.length(); i++) {
			if (sig.charAt(i) != input.read()) {
				throw new Exception(sResHash
						.getString("bad.signature.input.stream"));
			}
		}

		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int rc;

		while ((rc = input.read(buf)) > 0) {
			ba.write(buf, 0, rc);
		}

		byte[] nb = ec.decrypt(ba.toByteArray());
		byte[] check = sig.getBytes("UTF-8");

		if (check.length > nb.length) {
			throw new com.ibm.di.exceptions.PasswordException(sResHash
					.getString("incorrect.password"));
		}

		for (int i = 0; i < check.length; i++) {
			if (check[i] != nb[i]) {
				throw new com.ibm.di.exceptions.PasswordException(sResHash
						.getString("incorrect.password"));
			}
		}

		return new ByteArrayInputStream(nb, check.length, nb.length
				- check.length);
	}

	public void prefetch() throws Exception {
		reader = new BufferedReader(new InputStreamReader(getInputStream()));
	}

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

	public String readLine() throws IOException {
		if (reader != null)
			return reader.readLine();

		String str = super.readLine();
		if (str == null)
			return null;

		try {
			str = ec.getDecrypted(str);
			if (str.endsWith("\r\n"))
				str = str.substring(0, str.length() - 2);
			return str;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(sResHash.getString("readline.exception", e));
		}
	}

	public static boolean isEncrypted(File f) throws IOException {

		InputStream inp = new FileInputStream(f);
		String sig = EncryptedWriter.SIGNATURE;

		try {
			for (int i = 0; i < sig.length(); i++) {
				if (sig.charAt(i) != inp.read())
					return false;
			}

			return true;
		} catch (Exception err) {
		} finally {
			inp.close();
		}

		return false;
	}

	public static byte[] decrypt(String cipher, String pwd, byte[] data)
			throws Exception {
		SecurityCrypto ec = new SecurityCrypto(pwd, cipher);
		byte[] nb = ec.decrypt(data);
		byte[] check = EncryptedWriter.SIGNATURE.getBytes("UTF-8");

		if (check.length > nb.length) {
			throw new com.ibm.di.exceptions.PasswordException(sResHash
					.getString("incorrect.password"));
		}

		for (int i = 0; i < check.length; i++) {
			if (check[i] != nb[i]) {
				throw new com.ibm.di.exceptions.PasswordException(sResHash
						.getString("incorrect.password"));
			}
		}

		byte[] result = new byte[nb.length - check.length];
		for (int i = 0; i < result.length; i++)
			result[i] = nb[i + check.length];

		return result;
	}
}
