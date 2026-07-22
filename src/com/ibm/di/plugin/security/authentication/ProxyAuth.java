/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.security.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Arrays;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.server.ResourceHash;

/**
 * Mutual authentication between the proxy and a client. A client can be either
 * the plug-in module of a Password Synchronizer or an administration tool. This
 * class handles the authentication protocol from the proxy side.
 */
public class ProxyAuth {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	/**
	 * The name of the password file of client (plugin or admin tool).
	 */
	public static final String CLIENT_PASSWORD_FILE_NAME = "plgb.dat";

	/**
	 * The name of the password file of the proxy.
	 */
	public static final String PROXY_PASSWORD_FILE_NAME = "plga.dat";

	/**
	 * The length of a password in bytes.
	 */
	public static final int PASSWORD_LENGTH = 16;

	/**
	 * Container for internationalized messages.
	 */
	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	/**
	 * Prefix for the messages which this module logs.
	 */
	private static final String PREFIX = "ProxyAuth";

	/**
	 * A random number generator which is used to generate one-time passwords
	 * for the authentication.
	 */
	private static SecureRandom rand = new SecureRandom();

	/**
	 * Create a password file using the specified password.
	 * 
	 * @param filePath
	 *            The path of the password file that will be created.
	 * @param password
	 *            The password that will be written in the file.
	 * @throws IOException
	 *             Error while writing the file.
	 */
	public static void createPWDFile(String filePath, byte[] password)
			throws IOException {

		FileOutputStream pwdFileOutStream = null;

		try {
			pwdFileOutStream = new FileOutputStream(filePath);
			pwdFileOutStream.write(password);
			pwdFileOutStream.flush();
		} finally {
			if (pwdFileOutStream != null) {
				pwdFileOutStream.close();
			}
		}
	}

	/**
	 * Generate a random password.
	 * 
	 * @return The generated password.
	 */
	private static byte[] createPWD() {
		byte[] proxyPWD = new byte[PASSWORD_LENGTH];
		rand.nextBytes(proxyPWD);
		return proxyPWD;
	}

	/**
	 * <p>
	 * Perform mutual authentication between the proxy and a client. This method
	 * handles the proxy side of the protocol.
	 * </p>
	 * <p>
	 * The authentication protocol goes like this: First the proxy creates both
	 * password files. After that the proxy notifies the client that the
	 * authentication may begin. Then in turns the client and the each other's
	 * passwords over the connection: the client sends the proxy's password to
	 * the proxy, and the proxy sends the client's password to the client.
	 * </p>
	 * 
	 * @param socket
	 *            A connection to the client.
	 * @param authFolder
	 *            Authentication folder.
	 * @param log
	 *            Logger.
	 * @return Whether the authentication succeeded.
	 */
	public static synchronized boolean authenticate(Socket socket,
			String authFolder, PWSyncLog log) {

		if (authFolder == null || authFolder.trim().length() == 0) {
			return false;
		}

		try {

			final String clientPwdFile = authFolder + File.separator
					+ CLIENT_PASSWORD_FILE_NAME;
			final String proxyPwdFile = authFolder + File.separator
					+ PROXY_PASSWORD_FILE_NAME;

			int oldTimeout = socket.getSoTimeout();
			int newTimeout = 10000; // milliseconds
			socket.setSoTimeout(newTimeout);// set new timeout parameter
			InputStream sInStream = socket.getInputStream();
			OutputStream sOutStream = socket.getOutputStream();

			// create proxy and client password files
			byte[] proxyPWD = createPWD();
			byte[] clientPWD = createPWD();
			try {
				createPWDFile(proxyPwdFile, proxyPWD);
				createPWDFile(clientPwdFile, clientPWD);
			} catch (Exception exc) {
				sOutStream.write(1);
				sOutStream.flush();
				log.error(PREFIX, resHash.getString(
						"PROXY.AUTH.PASS.FILE.CREATION.FAILED", exc));
				return false;
			}

			// notify the client that the files are created
			sOutStream.write(0);
			sOutStream.flush();

			/* reading the proxy password from client */
			byte[] receivedPWD = new byte[PASSWORD_LENGTH];
			int rCode = sInStream.read(receivedPWD, 0, PASSWORD_LENGTH);

			if (rCode != PASSWORD_LENGTH) {
				log.error(PREFIX, resHash
						.getString("PROXY.AUTH.CLIENT.UNEXPECTED.RESPONSE"));
				return false;
			}

			boolean result = Arrays.equals(proxyPWD, receivedPWD);
			if (!result) {
				log.error(PREFIX, resHash
						.getString("PROXY.AUTH.WRONG.PASS.FROM.CLIENT"));
				sOutStream.write(1);
				sOutStream.flush();
				return false;
			}

			log.info(PREFIX, resHash
					.getString("PROXY.AUTH.CORRECT.PASS.FROM.CLIENT"));
			log.info(PREFIX, resHash
					.getString("PROXY.AUTH.SENDING.PASS.TO.CLIENT"));

			/* sending client password to client */
			sOutStream.write(clientPWD);// password written to the
			// socket OutputStream
			sOutStream.flush();// password sent to client for verification

			byte[] clientResponse = new byte[1];
			clientResponse[0] = 1;
			rCode = sInStream.read(clientResponse, 0, 1);// the client must
			// send 0 to confirm
			// that the password
			// is correct
			if (rCode != 1) {// expecting only one byte to be received.
				log.error(PREFIX, resHash
						.getString("PROXY.AUTH.CLIENT.UNEXPECTED.RESPONSE.2"));
				return false;
			}

			if (clientResponse[0] != 0) {// the password sent to the client
				// was wrong.
				log.error(PREFIX, resHash
						.getString("PROXY.AUTH.CLIENT.REJECTED.PASS"));
				return false;
			}

			log.info(PREFIX, resHash
					.getString("PROXY.AUTH.CLIENT.ACCEPTED.PASS"));

			socket.setSoTimeout(oldTimeout);// restore the original timeout
			// value
		} catch (Exception exc) {
			log.error(PREFIX, resHash.getString("PROXY.AUTH.FAILED", exc));
			return false;
		} finally {
			cleanUpAuthFolder(authFolder, log);
		}

		return true;
	}

	/**
	 * Delete the password files from the authentication folder.
	 * 
	 * @param authFolder
	 *            The authentication folder.
	 * @param log
	 *            Logger.
	 */
	private static void cleanUpAuthFolder(String authFolder, PWSyncLog log) {
		File f = new File(authFolder + File.separator
				+ PROXY_PASSWORD_FILE_NAME);
		if (!f.delete()) {
			log.warn(PREFIX, resHash.getString(
					"PROXY.AUTH.CANNOT.DELETE.PROXY.PASS.FILE", f
							.getAbsolutePath()));
		} else {
			log.info(PREFIX, resHash
					.getString("PROXY.AUTH.PROXY.PASS.FILE.DELETED"));
		}

		f = new File(authFolder + File.separator + CLIENT_PASSWORD_FILE_NAME);
		if (!f.delete()) {
			log.warn(PREFIX, resHash.getString(
					"PROXY.AUTH.CANNOT.DELETE.CLIENT.PASS.FILE", f
							.getAbsolutePath()));
		} else {
			log.info(PREFIX, resHash
					.getString("PROXY.AUTH.CLIENT.PASS.FILE.DELETED"));
		}
	}

}
