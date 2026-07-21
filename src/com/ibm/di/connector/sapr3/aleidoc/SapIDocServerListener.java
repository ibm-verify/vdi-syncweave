/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidoc;

import java.io.PrintWriter;
import java.io.StringWriter;
import com.ibm.di.server.Trace;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Server;

/**
 * Implementation of JCo.Server listeners for internal middleware exceptions,
 * errors, state changes and trace messages.
 */
public class SapIDocServerListener implements JCO.ServerExceptionListener,
		JCO.ServerErrorListener, JCO.ServerStateChangedListener,
		JCO.TraceListener {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SapALEIDocConnector conn;

	/**
	 * Disabled. Connector object must be provided.
	 */
	private SapIDocServerListener() {
		super();
	}

	/**
	 * Only available constructor.
	 * 
	 * @param conn -
	 *            The Connector providing the log and trace mechanisims.
	 */
	public SapIDocServerListener(SapALEIDocConnector conn) {
		this.conn = conn;
	}

	/**
	 * Overridden method of JCoIDoc.ServerExceptionListener. Listens for JCo
	 * middleware internal exceptions.
	 * 
	 * @param srvr -
	 *            The JCo Server who throw the exception.
	 * @param ex -
	 *            The internal JCo exception.
	 */
	public void serverExceptionOccurred(Server srvr, Exception ex) {
		Object[] args = new Object[] { srvr.getProgID(), ex.getMessage() };
		conn.logmsg(
				LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0043,
						args));
		if (conn.mDebugEnabled) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			if (conn.mDebugEnabled) {
				conn.debug(sw.toString());
			}
		}
	}

	/**
	 * Overridden method of JCoIDoc.ServerErrorListener. Listens for JCo
	 * middleware internal error messages.
	 * 
	 * @param srvr -
	 *            The JCo Server who throw the exception.
	 * @param error -
	 *            The internal JCo error message.
	 */
	public void serverErrorOccurred(Server srvr, Error error) {
		Object[] args = new Object[] { srvr.getProgID(), error.getMessage() };
		conn.logmsg(
				LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0044,
						args));
	}

	/**
	 * Overridden method of JCoIDoc.ServerStateChangedListener Listens for JCo
	 * Server state changes. Some states are not exposed and so a valid message
	 * is not available, only the state identifier.
	 * 
	 * @param srvr -
	 *            The JCo Server who throw the exception.
	 * @param old_state -
	 *            The previous Server state.
	 * @param new_state -
	 *            The new Server state.
	 */
	public void serverStateChangeOccurred(Server srvr, int old_state,
			int new_state) {

		String oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0069, new Object[] { old_state });

		if (old_state == JCO.STATE_STOPPED)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0070);
		if (old_state == JCO.STATE_STARTED)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0071);
		if (old_state == JCO.STATE_SUSPENDED)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0072);
		if (old_state == JCO.STATE_LISTENING)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0073);
		if (old_state == JCO.STATE_BUSY)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0074);
		if (old_state == JCO.STATE_TRANSACTION)
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0075);

		String newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0069, new Object[] { new_state });
		
		if (new_state == JCO.STATE_STOPPED)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0070);
		if (new_state == JCO.STATE_STARTED)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0071);
		if (new_state == JCO.STATE_SUSPENDED)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0072);
		if (new_state == JCO.STATE_LISTENING)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0073);
		if (new_state == JCO.STATE_BUSY)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0074);
		if (new_state == JCO.STATE_TRANSACTION)
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0075);
		if (conn.getJcoServerProgId().equalsIgnoreCase(srvr.getProgID())) {
			Object[] args = new Object[] { srvr.getProgID(), oldStateStr,
					newStateStr };
			conn.logmsg(
					LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0045, args));
		}
	}

	/**
	 * Overridden method of JCoIDoc.TraceListener
	 * 
	 * Listens for JCo Server internal trace messages.
	 * 
	 * @param trcLevel -
	 *            The trace level.
	 * @param trcMessage -
	 *            The trace message.
	 */
	public void trace(int trcLevel, String trcMessage) {
		String traceL = "Trace level:" + trcLevel;
		String traceM = " Trace Message:'" + trcMessage + "'";
		Trace.text(this, traceL, traceM);
		if (conn.mDebugEnabled) {
			Object[] args = new Object[] { Integer.valueOf(trcLevel).toString(),
					trcMessage };
			if (conn.mDebugEnabled) {
				conn.debug(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0046, args));
			}
		}
	}
}
