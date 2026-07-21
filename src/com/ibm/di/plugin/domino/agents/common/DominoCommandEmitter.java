/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino.agents.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import com.ibm.di.plugin.domino.ProxyLoader;
import com.ibm.di.plugin.log.PWSyncLog;
import com.ibm.di.plugin.security.authentication.ClientAuth;
import com.ibm.di.server.ResourceHash;

/**
 * Common library class used for sending password commands to the remote proxy.
 */
public abstract class DominoCommandEmitter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	private static final String DOMINO_PLUGIN_LOG_PROP = "logFile";
	private static final String DOMINO_PLUGIN_DEBUG_PROP = "debug";
	private static final String DOMINO_PLUGIN_DEBUG_PORT = "serverPort";

	private static final String PREFIX = "CommLib";
	private static final int DEFAULT_PROXY_PORT = 18001;

	private static final int OPCODE_READY_TO_SYNC = 1;
	private static final int OPCODE_SYNC_PASS = 2;
	private static final int OPCODE_DISCONNECT = 5;
	private static final int OPCODE_STOP_PROXY = 250;

	private static final String PROPS_FILE = "idipwsync/pwsync.props";
	private static final String PROP_NAME_USE_UNID = "useUniqueID";
	private static final String PROP_NAME_IGNORE_MISSING_UNID = "ignoreMissingUniqueID";
	private static final String PROP_NAME_USERNAME_PREFIX = "usernamePrefix";
	private static final String PROP_NAME_CHECK_REPOSITORY = "checkRepository";

	private static final String UTF8_ENCODING = "UTF-8";

	private static boolean useUNID = false;
	private static boolean ignoreMissingUNID = false;
	private static String userNamePrefix = "";
	private static int port = DEFAULT_PROXY_PORT;

	private static PWSyncLog log = null;

	private static boolean preliminaryCheck = true;

	private static boolean init = false;

	private static final ResourceHash resHash = ResourceHash.getHash("domino");

	/**
	 * this method is used to synchronized the provided as parameters user
	 * credentials.
	 * 
	 * @param uid
	 *            the user's name.
	 * @param password
	 *            the user's password.
	 * @return true if the synchronization completed successfully, false
	 *         otherwise.
	 */
	public static boolean syncPass(String uid, String password) {

		if (uid == null) {
			log.error(PREFIX, resHash
					.getString("DOMINO.COMMAND.SYNC.NULL.USER"));
			return false;
		}

		String unid = uid;
		if (useUNID) {
			try {
				Session session = NotesFactory.createSession();

				Database db = session.getDatabase(null, "names.nsf");

				String filter = "FullName='" + uid + "'";
				log.debug(PREFIX, resHash.getString(
						"DOMINO.COMMAND.SEARCH.FILTER", filter));

				DocumentCollection dc = db.search(filter);
				int matches = dc.getCount();
				log.debug(PREFIX, resHash.getString("DOMINO.SEARCH.RESULTS",
						matches));

				if (matches != 1) {
					if (ignoreMissingUNID) {
						log.warn(PREFIX, resHash
								.getString("DOMINO.COMMAND.IGNORE.UNID"));
						return true;
					} else {
						log.debug(PREFIX, resHash
								.getString("DOMINO.COMMAND.USING.DN"));
						if (userNamePrefix != null
								&& userNamePrefix.length() > 0) {
							log.debug(PREFIX, resHash.getString(
									"DOMINO.COMMAND.USING.DN.PREFIX",
									userNamePrefix));
							if (uid.indexOf('=') > -1) {
								unid = uid.substring(0, uid.indexOf('=') + 1)
										+ userNamePrefix
										+ uid.substring(uid.indexOf('=') + 1);
							} else {
								unid = userNamePrefix + uid;
							}
						} else {
							unid = uid;
						}
					}
				} else {
					Document doc = dc.getFirstDocument();
					unid = doc.getUniversalID();
					log.info(PREFIX, resHash.getString(
							"DOMINO.COMMAND.USING.UNID", unid));
				}

				// cleanup
				try {
					session.recycle();
				} catch (NotesException ne) {
					log.error(PREFIX, resHash
							.getString("DOMINO.NOTES.EXCEPTION"), ne);
				}
			} catch (NotesException ne) {
				log.error(PREFIX, resHash.getString("DOMINO.NOTES.EXCEPTION"),
						ne);
				return false;
			}
		}

		log.debug(PREFIX, resHash.getString("DOMINO.COMMAND.SYNC.USER", unid));

		return executeProxyCommand(unid, password, OPCODE_SYNC_PASS);
	}

	/**
	 * This method only checks that the remote proxy is up and the Password
	 * Store is ready.
	 * 
	 * @param uid
	 *            not required.
	 * @param password
	 *            not required.
	 * @return true if the password synchronization operation is ready to
	 *         proceed, false otherwise.
	 */
	public static boolean readyToSync(String uid, String password) {
		if (preliminaryCheck)
			return executeProxyCommand(uid, password, OPCODE_READY_TO_SYNC);
		else
			return true;
	}

	/**
	 * This method is the actual worker. Connects to the remote Java Proxy and
	 * sends the specified command, credentials and reads the response.
	 * 
	 * @param uid
	 *            the user to send
	 * @param password
	 *            the password to send
	 * @param aOpCode
	 *            the operation code to send.
	 * @return true if the operation returned success (1), false if it returned
	 *         failed (0).
	 */
	protected static boolean executeProxyCommand(String uid, String password,
			int aOpCode) {

		// we should be already initialized, but just in case.
		initClass(DOMINO_PLUGIN_LOG_PROP);

		Socket socket = null;
		try {

			socket = new Socket(InetAddress.getByName(null), port);
			if (ClientAuth.authenticate(socket,
					ProxyLoader.DOMINO_CONFIG_FOLDER, log)) {
				log.debug(PREFIX, resHash.getString("DOMINO.AUTH.SUCCESS"));
			} else {
				log.debug(PREFIX, resHash.getString("DOMINO.AUTH.FAILURE"));
				socket.close();
				return false;
			}
		} catch (IOException e) {
			log.error(PREFIX, resHash.getString("DOMINO.CONNECTION.FAILURE",
					port));
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
			return false;
		}

		// prototcol:
		// "00" + BOM + "00" (readResp) + opcode + "00" + pass_num (0 or 1) +
		// "00" + (bytesLen + dn) (nothing if null) (if pass_num == 1)(bytesLen
		// + pass)
		// (readResp)
		int response = 0;

		try {
			OutputStream socketOut = socket.getOutputStream();
			// send the UTF-8 BOM
			socketOut.write(new byte[] { 0x00, 0x00, (byte) 0xEF, (byte) 0xBB,
					(byte) 0xBF });
			socketOut.flush();

			if ((response = socket.getInputStream().read()) == 1) {

				socketOut.write(0);
				socketOut.write(0);
				socketOut.write(aOpCode);
				socketOut.write(0);
				socketOut.write(0);

				if (aOpCode != OPCODE_STOP_PROXY) {

					if (password != null) {
						socketOut.write(1);
					} else {
						socketOut.write(0);
					}

					socketOut.write(0);
					socketOut.write(0);

					if (uid != null) {
						socketOut.write(getBytes(uid));
					}

					if (password != null) {
						socketOut.write(getBytes(password));
					}
				}

				socketOut.flush();
				response = socket.getInputStream().read();

				// tell the proxy to stop the client handler thread and close
				// the socket.
				socketOut.write(0);
				socketOut.write(0);
				socketOut.write(OPCODE_DISCONNECT);
				socketOut.write(0);
				socketOut.write(0);
				socketOut.flush();

				socket.getInputStream().read();
			}
		} catch (IOException e) {
			log.error(PREFIX, resHash.getString("DOMINO.JAVA.EXCEPTION"), e);
			return false;
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				log
						.error(PREFIX, resHash
								.getString("DOMINO.JAVA.EXCEPTION"), e);
			}
			socket = null;
		}

		if (response == 1) {
			return true;
		} else {
			return false;
		}
	}

	private static void initClass(String propName) {
		synchronized (DominoCommandEmitter.class) {
			if (!init) {
				Properties props = loadConfig();
				openLog(props, propName);
				init = true;
			}
		}
	}

	private static Properties loadConfig() {

		Properties props = new Properties();

		InputStream file;
		try {
			file = new FileInputStream(PROPS_FILE);

			try {
				props.load(file);
			} finally {
				file.close();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println(resHash.getString(
					"DOMINO.COMMAND.FILE.NOT.FOUND", PROPS_FILE));
		} catch (IOException e) {
			System.out.println(resHash.getString(
					"DOMINO.COMMAND.FILE.NOT.EADABLE", PROPS_FILE));
			e.printStackTrace();
		}

		if ("true".equalsIgnoreCase(props.getProperty(PROP_NAME_USE_UNID))
				|| "1".equalsIgnoreCase(props.getProperty(PROP_NAME_USE_UNID))) {
			useUNID = true;
		} else {
			useUNID = false;
		}

		if ("true".equalsIgnoreCase(props
				.getProperty(PROP_NAME_IGNORE_MISSING_UNID))
				|| "1".equalsIgnoreCase(props
						.getProperty(PROP_NAME_IGNORE_MISSING_UNID))) {
			ignoreMissingUNID = true;
		} else {
			ignoreMissingUNID = false;
		}

		if ("true".equalsIgnoreCase(props
				.getProperty(PROP_NAME_CHECK_REPOSITORY))
				|| "1".equalsIgnoreCase(props
						.getProperty(PROP_NAME_CHECK_REPOSITORY))) {
			preliminaryCheck = true;
		} else {
			preliminaryCheck = false;
		}

		userNamePrefix = props.getProperty(PROP_NAME_USERNAME_PREFIX);

		try {
			port = Integer
					.parseInt(props.getProperty(DOMINO_PLUGIN_DEBUG_PORT));
		} catch (NumberFormatException nfe) {
			port = DEFAULT_PROXY_PORT;
		}

		return props;
	}

	private static void openLog(Properties props, String propName) {
		try {
			log = new PWSyncLog(new FileOutputStream(props
					.getProperty(propName), true), null, Boolean
					.parseBoolean(props.getProperty(DOMINO_PLUGIN_DEBUG_PROP)));
		} catch (FileNotFoundException e) {
			log = new PWSyncLog(null, null, false);
		}
	}

	/**
	 * Gets the common for all the domino agents log. This method will do a lazy
	 * loading of the configuration parameters. If the specified file could not
	 * be opened the System.in shall be used as output.
	 * 
	 * @param propName
	 *            the name of the property which points to the log file which
	 *            will be used to log in.
	 * 
	 * @return {@link PWSyncLog} instance.
	 */
	public static PWSyncLog getLog(String propName) {
		initClass(propName);

		return log;
	}

	private static byte[] getBytes(String str)
			throws UnsupportedEncodingException {
		if (str == null)
			return null;

		byte[] bStr = str.getBytes(UTF8_ENCODING);
		byte[] result = new byte[bStr.length + 2];

		result[1] = (byte) (bStr.length & 0x00ff);
		result[0] = (byte) (bStr.length >>> 8);

		for (int i = 2, j = 0; i < result.length; i++, j++) {
			result[i] = bStr[j];
		}

		return result;
	}

	/** sends a request to the running Java Proxy to stop. */
	public static void stopProxy() {
		executeProxyCommand(null, null, OPCODE_STOP_PROXY);
	}
}
