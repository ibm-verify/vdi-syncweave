package com.ibm.di.test.utils.func;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class PortProbe {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final int INITIAL_PORT = 1025;

	private static int lastPort = INITIAL_PORT;

	public static int getAvailablePort() throws IOException {
		boolean restart = false;
		int start = lastPort;

		while (true) {
			if (lastPort == Short.MAX_VALUE) {
				lastPort = INITIAL_PORT;
				restart = true;
			} else if (restart && lastPort == start) {
				// back to where we came from...
				throw new IOException("Couldn't find an available port.");
			} else {
				try {
					ServerSocket socket = new ServerSocket(lastPort);
					socket.close();
					return lastPort;
				} catch (IOException e) {
					// e.printStackTrace();
					// address already in use... keep trying
				} finally {
					lastPort++;
				}
			}
		}
	}
}
