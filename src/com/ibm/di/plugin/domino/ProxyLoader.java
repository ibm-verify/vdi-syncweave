/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.plugin.domino;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import lotus.notes.addins.JavaServerAddin;
import lotus.notes.internal.MessageQueue;

import com.ibm.di.plugin.proxy.Proxy;
import com.ibm.di.server.ResourceHash;

/**
 * This is the Class started by the Domino Server. This class is responsible for
 * booting up the common Java Proxy on the Domino Server.
 */
public class ProxyLoader extends JavaServerAddin {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.plugin.CopyRight.OBJECT_CODE;

	// Add-in name
	private static final String ADDIN_NAME = "IDIPwSync";

	private static final String ADDIN_DISPLAY_NAME = "Java Proxy";

	private static final String Q_NAME = MSG_Q_PREFIX + "IDIPWSYNC";

	private static final int PKG_MISC = 0x0400;

	private static final int ERR_MQ_QUITTING = PKG_MISC + 102;

	// MessageQueue Constants
	private static final int MQ_MAX_MSGSIZE = 256;

	private static final int MQ_WAIT_FOR_MSG = MessageQueue.MQ_WAIT_FOR_MSG;

	// Domino Proxy configuration properties names
	/** Domino plugin's authentication folder */
	public static final String DOMINO_CONFIG_FOLDER = "idipwsync";

	/** Domino plugin's configuration file name */
	protected static final String DOMINO_CONFIG_FILE_NAME = "pwsync.props";

	/** Domino plugin's path to the configuration file */
	protected static final String DOMINO_FULL_CONFIG_FILE_NAME = DOMINO_CONFIG_FOLDER
			+ "/" + DOMINO_CONFIG_FILE_NAME;

	private static final ResourceHash resHash = ResourceHash.getHash("domino");

	/**
	 * Default constructor that sets the name of this Add-in.
	 */
	public ProxyLoader() {
		setName(ADDIN_NAME);
	}

	/**
	 * This method is the entry point in this class. It is called by the Domino
	 * framework.
	 */
	public void runNotes() {
		int taskID = AddInCreateStatusLine(ADDIN_DISPLAY_NAME);
		AddInSetStatusLine(taskID, resHash.getString("DOMINO.INITIALIZING"));
		consolePrint(resHash.getString("DOMINO.INITIALIZING"));
		MessageQueue mq = new MessageQueue();
		try {

			int mqError = mq.create(Q_NAME, 0, 0);
			if (mqError != NOERROR) {
				consolePrint(resHash.getString("DOMINO.ERROR.CREATE.MQ",
						mqError));
				terminateDominoAddin(taskID, null);
				return;
			}

			mqError = mq.open(Q_NAME, 0);
			if (mqError != NOERROR) {
				consolePrint(resHash.getString("DOMINO.ERROR.OPEN.MQ", mqError));
				terminateDominoAddin(taskID, null);
				return;
			}

			System.setProperty(Proxy.PROXY_CONFIG_FILE, new File(
					DOMINO_FULL_CONFIG_FILE_NAME).getAbsolutePath());

			Proxy proxy = new Proxy();

			proxy.init();

			// start the Proxy.
			new ProxyRunner(proxy, this).start();
			AddInSetStatusLine(taskID, "");

			// start the MQ reader.
			StringBuffer msg = new StringBuffer();
			int retCode = mq.get(msg, MQ_MAX_MSGSIZE, MQ_WAIT_FOR_MSG, 0);

			while (retCode != ERR_MQ_QUITTING && !proxy.proxyStopRequested()) {

				if (retCode == ERR_MQ_QUITTING) {
					proxy.requestProxyStop();
					break;
				} else {
					msg.delete(0, msg.length());
					retCode = mq.get(msg, MQ_MAX_MSGSIZE, MQ_WAIT_FOR_MSG, 0);
				}
			}

		} catch (Exception e) {
			consolePrint(resHash.getString("DOMINO.EXCEPTION", e.toString()));
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(b, true);

			e.printStackTrace(ps);
			ps.close();

			consolePrint(new String(b.toByteArray()));
		} finally {
			terminateDominoAddin(taskID, mq);
		}
	}

	/**
	 * Terminates this task by releasing all the used resources.
	 * 
	 * @param taskID
	 *            the identifier of the task this class represents
	 * @param mq
	 *            the QueueManager used for communication.
	 */
	private void terminateDominoAddin(int taskID, MessageQueue mq) {
		try {
			AddInSetStatusLine(taskID, resHash.getString("DOMINO.TERMINATING"));
			consolePrint(resHash.getString("DOMINO.TERMINATING"));
			AddInDeleteStatusLine(taskID);

			if (mq != null) {
				mq.close(0);
			}

			consolePrint(resHash.getString("DOMINO.TERMINATED"));
		} catch (Exception e) {
			consolePrint(resHash.getString("DOMINO.EXCEPTION", e.toString()));
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(b, true);

			e.printStackTrace(ps);
			ps.close();

			consolePrint(new String(b.toByteArray()));
		}
	}

	/**
	 * Dumps a message on the Domino Server Console.
	 * 
	 * @param aMsg
	 *            the message to print.
	 */
	protected void consolePrint(String aMsg) {
		AddInLogMessageText(ADDIN_NAME + ": " + aMsg, 0);
	}

	private static class ProxyRunner extends Thread {

		private Proxy proxy;
		private ProxyLoader proxyLoader;

		/**
		 * Creates a new instance.
		 * 
		 * @param proxy
		 * @param proxyLoader
		 */
		public ProxyRunner(Proxy proxy, ProxyLoader proxyLoader) {
			this.proxy = proxy;
			this.proxyLoader = proxyLoader;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			try {
				proxy.runProxy();
			} catch (Exception e) {
				proxyLoader.consolePrint(resHash.getString("DOMINO.EXCEPTION",
						e.toString()));
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(b, true);

				e.printStackTrace(ps);
				ps.close();

				proxyLoader.consolePrint(new String(b.toByteArray()));
			}
		}
	};
}
