/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidocv3;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.ibm.di.server.Trace;
import com.sap.conn.jco.JCoTraceListener;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;

/**
 * Implementation of JCo.Server listeners for internal middleware exceptions,
 * errors, state changes and trace messages.
 */
public class SapIDocServerListenerV3 implements JCoServerExceptionListener,
		JCoServerErrorListener, JCoServerStateChangedListener,
		JCoTraceListener {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SapALEIDocConnectorV3 conn;

	/**
	 * Disabled. Connector object must be provided.
	 */
	private SapIDocServerListenerV3() {
		super();
	}

	/**
	 * Only available constructor.
	 * 
	 * @param conn -
	 *            The Connector providing the log and trace mechanisims.
	 */
	public SapIDocServerListenerV3(SapALEIDocConnectorV3 conn) {
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
	@Override
	public void serverExceptionOccurred(JCoServer srvr, String arg1,
			JCoServerContextInfo arg2, Exception ex) {
//	public void serverExceptionOccurred(JCoServer srvr, Exception ex) {
		//Object[] args = new Object[] { srvr.getProgID(), ex.getMessage() };
		Object[] args = new Object[] { srvr.getProgramID(), ex.getMessage() };
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
	public void serverErrorOccurred(JCoServer srvr, Error error) {
		Object[] args = new Object[] { srvr.getProgramID(), error.getMessage() };
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
	public void serverStateChangeOccurred(JCoServer srvr, int old_state,
			int new_state) {

		String oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0069, new Object[] { old_state });

		if (old_state == JCoServerState.STOPPED.ordinal())
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0070);
		if (old_state == JCoServerState.STARTED.ordinal())
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0071);
		if (old_state == JCoServerState.STOPPING.ordinal())
			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0072);
//		if (old_state == JCO.STATE_LISTENING)
//			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0073);
//		if (old_state == JCO.STATE_BUSY)
//			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0074);
//		if (old_state == JCO.STATE_TRANSACTION)
//			oldStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0075);

		String newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0069, new Object[] { new_state });
		
		if (new_state == JCoServerState.STOPPED.ordinal())
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0070);
		if (new_state == JCoServerState.STARTED.ordinal())
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0071);
		if (new_state == JCoServerState.STOPPING.ordinal())
			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0072);
//		if (new_state == JCO.STATE_LISTENING)
//			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0073);
//		if (new_state == JCO.STATE_BUSY)
//			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0074);
//		if (new_state == JCO.STATE_TRANSACTION)
//			newStateStr = LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0075);
		if (conn.getJcoServerProgId().equalsIgnoreCase(srvr.getProgramID())) {
			Object[] args = new Object[] { srvr.getProgramID(), oldStateStr,
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

	@Override
	public void serverStateChangeOccurred(JCoServer arg0, JCoServerState arg1,
			JCoServerState arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serverErrorOccurred(JCoServer arg0, String arg1,
			JCoServerContextInfo arg2, Error arg3) {
		// TODO Auto-generated method stub
		
	}

}
