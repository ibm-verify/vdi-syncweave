/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.proxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.security.authentication.ClientAuth;
import com.ibm.di.server.ResourceHash;

/**
 * This class represents a utility that connects to a running Java Proxy and
 * sends a stop request.
 */
public class StopProxy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final int OPCODE_TERMINATE = 250;

	private static final ResourceHash resHash = ResourceHash.getHash("proxy");

	private static final PWSyncLog log = new PWSyncLog(System.out, null, true);

	/**
	 * This is the entry point of the Java StopProxy utility.
	 * 
	 * @param args
	 *            no specific arguments are checked or expected.
	 * @throws FileNotFoundException
	 *             if the config file could not be found.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		if (args.length != 1) {
			showUsage();
			return;
		}
		
		System.setProperty(Proxy.PROXY_CONFIG_FILE, args[0]);
		
		Proxy.readProxyConfigurationFile();

		int port = Proxy.DEFAULT_SERVER_PORT;

		try {
			port = Integer.parseInt(System.getProperty(Proxy.PROXY_PORT));
		} catch (NumberFormatException e) {
			log.warn(resHash.getString("PWSYNC.INCORRECT.PORT", new Object[] {
					Proxy.PROXY_PORT, Proxy.DEFAULT_SERVER_PORT }));
		}

		Socket socket = null;
		String host = null;
		try {
			host = InetAddress.getByName(null).getHostAddress();
			socket = new Socket(host, port);
			if (ClientAuth.authenticate(socket, System
					.getProperty(Proxy.PROXY_AUTH_FOLDER), new PWSyncLog(
					System.out, null, true))) {
				log.info(resHash.getString("PWYSYNC.AUTH.SUCCESS"));
			} else {
				log.warn(resHash.getString("PWSYNC.AUTH.FAILURE"));
				if (socket != null) {
					socket.close();
				}
				System.exit(0);
			}
		} catch (IOException e) {
			log.error(resHash.getString("PWSYNC.ERROR.CONNECTING.TO.PROXY",
					new Object[] { host, port }), e);
			System.exit(0);
		}

		try {
			log.info(resHash.getString("PWSYNC.SENT.STOP.REQUEST"));
			// BOM
			socket.getOutputStream().write(0);
			socket.getOutputStream().write(0);
			socket.getOutputStream().write(0xEF);
			socket.getOutputStream().write(0xBB);
			socket.getOutputStream().write(0xBF);
			socket.getOutputStream().flush();
			if (socket.getInputStream().read() != 1)
				log.warn(resHash.getString("PWSYNC.OLD.PROXY"));

			// Operation
			socket.getOutputStream().write(0);
			socket.getOutputStream().write(0);
			socket.getOutputStream().write(OPCODE_TERMINATE);
			socket.getOutputStream().write(0);
			socket.getOutputStream().write(0);
			socket.getOutputStream().flush();

			if (socket.getInputStream().read() != 1)
				log.warn(resHash.getString("PWYSINC.STOP.REJECTED"));
		} catch (IOException e) {
			log.error(resHash.getString("PWSYNC.IO.EXCEPTION"), e);
		}

		log.info(resHash.getString("PWSYNC.PROXY.IS.STOPPING",
				(ProxyCommandReceiver.SOCKET_TIMEOUT / 1000)));

		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			log.error(resHash.getString("PWSYNC.IO.EXCEPTION"), e);
		}
		socket = null;
	}

	private static void showUsage() {
		System.out.println(resHash.getString("PWSYNC.STOP.USAGE.1"));
		System.out.println(resHash.getString("PWSYNC.STOP.USAGE.2"));
	}
}
