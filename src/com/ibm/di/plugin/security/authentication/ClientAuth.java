/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.security.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.server.ResourceHash;

/**
 * Mutual authentication between the proxy and a client. A client can be either
 * the plug-in module of a Password Synchronizer or an administration tool. This
 * class handles the authentication protocol from the client side.
 */
public class ClientAuth {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * Container for internationalized messages.
	 */
	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	/**
	 * Prefix for the messages which this module logs.
	 */
	private static final String PREFIX = "ClientAuth";

	/**
	 * <p>
	 * Perform mutual authentication between the proxy and a client. This method
	 * handles the client side of the protocol.
	 * </p>
	 * @param socket
	 *            A connection to the client.
	 * @param authFolder
	 *            Authentication folder.
	 * @param log
	 *            Logger.
	 * @return Whether the authentication succeeded.
	 * @see com.ibm.di.plugin.security.authentication.ProxyAuth#authenticate(Socket, String, PWSyncLog)
	 */
	public static boolean authenticate(Socket socket, String authFolder,
			PWSyncLog log) {

		if (authFolder == null || authFolder.trim().length() == 0) {
			return false;
		}

		try {

			final String clientPwdFile = authFolder + File.separator
					+ ProxyAuth.CLIENT_PASSWORD_FILE_NAME;
			final String proxyPwdFile = authFolder + File.separator
					+ ProxyAuth.PROXY_PASSWORD_FILE_NAME;

			int oldTimeout = socket.getSoTimeout();
			int newTimeout = 10000; // milliseconds
			socket.setSoTimeout(newTimeout);// set new timeout parameter
			InputStream sInStream = socket.getInputStream();
			OutputStream sOutStream = socket.getOutputStream();

			if (sInStream.read() != 0) {
				log.error(PREFIX, resHash
						.getString("CLIENT.AUTH.PASS.FILE.CREATION.FAILED"));
				return false;
			}

			byte[] clientPWD = readPWDFromFile(clientPwdFile);
			byte[] proxyPWD = readPWDFromFile(proxyPwdFile);

			log.info(PREFIX, resHash.getString("CLIENT.AUTH.PASS.FILE.READ"));

			log.info(PREFIX, resHash
					.getString("CLIENT.AUTH.SENDING.PROXY.PASS"));
			sOutStream.write(proxyPWD);
			sOutStream.flush();

			byte[] receivedPWD = new byte[ProxyAuth.PASSWORD_LENGTH];
			int rCode = sInStream.read(receivedPWD);
			if (rCode != ProxyAuth.PASSWORD_LENGTH) {
				log.error(PREFIX, resHash
						.getString("CLIENT.AUTH.PROXY.UNEXPECTED.RESPONSE"));
				return false;
			}

			boolean result = Arrays.equals(clientPWD, receivedPWD);
			if (result) {
				sOutStream.write(0);
				sOutStream.flush();
				log.info(PREFIX, resHash
						.getString("CLIENT.AUTH.PROXY.PASS.VALID"));
			} else {
				sOutStream.write(1);
				sOutStream.flush();
				log.error(PREFIX, resHash
						.getString("CLIENT.AUTH.PROXY.PASS.INVALID"));
				return false;
			}

			socket.setSoTimeout(oldTimeout);// return the original timeout value
		} catch (Exception exc) {
			log.error(PREFIX, resHash.getString("CLIENT.AUTH.FAILED", exc));
			return false;
		}

		return true;
	}

	/**
	 * Read a password from a file.
	 * 
	 * @param filePath The path of the password file.
	 * @return The read password.
	 * @throws Exception If the file does not exist, or an I/O related error occurs or
	 * the length of the password is incorrect. 
	 */
	private static byte[] readPWDFromFile(String filePath) throws Exception {

		File f = new File(filePath);
		if (!f.exists()) {
			throw new Exception(resHash.getString(
					"CLIENT.AUTH.PASS.FILE.MISSING", f.getAbsolutePath()));
		}

		byte[] password = new byte[ProxyAuth.PASSWORD_LENGTH];

		FileInputStream pwdFileInStream = null;
		try {
			pwdFileInStream = new FileInputStream(f);
			int rCode = pwdFileInStream.read(password);
			if (rCode != ProxyAuth.PASSWORD_LENGTH) {
				throw new Exception(resHash.getString(
						"CLIENT.AUTH.PASS.FILE.INCORRECT", f.getAbsolutePath()));
			}
		} finally {
			if (pwdFileInStream != null) {
				pwdFileInStream.close();
			}
		}

		return password;
	}
}
