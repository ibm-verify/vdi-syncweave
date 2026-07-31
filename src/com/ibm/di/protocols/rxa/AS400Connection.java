/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.di.entry.Entry;
import com.ibm.tivoli.remoteaccess.AS400Protocol;
import com.ibm.tivoli.remoteaccess.ProgramOutput;
import com.ibm.tivoli.remoteaccess.ProgramOutputAS400;
import com.ibm.tivoli.remoteaccess.RemoteAccess;
import com.ibm.tivoli.remoteaccess.RemoteAccessAuthException;

/**
 * This class encapsulates the RXA library's AS400 Connection related objects
 */
public class AS400Connection extends ConnectionImpl {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The protocol used to begin a session with the target machine
	 */
	private AS400Protocol as400Protocol;

	/**
	 * The connection protocol being used
	 */
	public static final String TYPE = "AS400";

	/**
	 * To be used in logged messages
	 */
	protected Object[] args = { TYPE };

	/**
	 * @param log
	 */
	public AS400Connection(LogProxy log) {
		super(log);
	}

	/**
	 * Begin a session with the target machine using the SSH protocol
	 * 
	 * @return RemoteAccess The RXA connection object
	 * @throws RemoteConnectException
	 */
	public RemoteAccess beginSession() throws RemoteConnectException {
		lp.debug(MessageHelper.getMsgResource().getMessage(
				MsgIds.CREATING_CONNECTION,
				new Object[] { TYPE, hostName, userName }));
		// Prepare to work with the local iSeries system.
		// Create the RXA connection object
		as400Protocol = new AS400Protocol(userName, password, hostName);
		if (as400SSL) {
			as400Protocol.setForceSecure(true);
		} else {
			as400Protocol.setForceSecure(false);
		}
		if (port != 0) {
			lp.info(MessageHelper.getMsgResource().getMessage(
					MsgIds.WIN_NO_PORT_ALLOWED));
		}
		try {
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_BEGIN, args));
			as400Protocol.beginSession();
			lp.debug(MessageHelper.getMsgResource().getMessage(
					MsgIds.SESSION_STARTED));
		} catch (RemoteAccessAuthException e) {
			as400Protocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		} catch (ConnectException e) {
			as400Protocol = null;
			throw ExceptionFactory.createRemoteConnectException(e, lp);
		}
		setRXAProtocol(as400Protocol);
		return as400Protocol;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType() {
		return TYPE;
	}

	/**
	 * Transform the ProgramOutput into an Entry object containing the results
	 * 
	 * @param p
	 *            The ProgramOutput object containing the results from executing
	 *            the command
	 * @return Entry object containing three attributes: command.out,
	 *         command.error, command.returnCode
	 */
	protected Entry getEntryResponse(ProgramOutput p) {
		ProgramOutputAS400 response = (ProgramOutputAS400) p;
		Entry rsp = new Entry();
		rtnCodeAttr.clear();
		stdOutputAttr.clear();
		stdErrorAttr.clear();
		rtnCodeAttr.addValue(Integer.valueOf((response.getReturnCode())));
		stdOutputAttr.addValue(response.getStdout());
		AS400Message[] as400Msgs = response.getAS400Messages();
		for (int i = 0; i < as400Msgs.length; i++) {
			String msg = (as400Msgs[i]).getText();
			if (msg != null && msg.length() > 0) {
				stdOutputAttr.addValue(msg);
			}
		}
		stdErrorAttr.addValue(response.getStderr());
		rsp.setAttribute(COMMAND_RETURN_CODE, rtnCodeAttr);
		rsp.setAttribute(COMMAND_STD_ERROR, stdErrorAttr);
		rsp.setAttribute(COMMAND_STD_OUTPUT, stdOutputAttr);
		return rsp;
	}

	/**
	 * Run the specified command on the target machine
	 * 
	 * @param c
	 *            String containing the command to be executed
	 * @param args
	 *            command arguments
	 * @param delim
	 *            command argument delimiter
	 * 
	 * @return Entry containing the results from executing the command. It
	 *         contains three attributes: command.out, command.error and
	 *         command.returnCode.
	 * 
	 * @throws GeneralCLFCException
	 */
	public Entry runAS400Program(String c, String[] args, String delim)
			throws GeneralCLFCException {
		String cmd = c;
		Entry rsp = null;
		ProgramOutput po = null;
		try {
			ProgramParameter[] progArgs = new ProgramParameter[args.length];
			String logArgStr = "";
			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i < args.length; i++) {
				if (null == args[i]) {
					lp.error(MessageHelper.getMsgResource().getMessage(
							MsgIds.WRONG_TYPE_FOR_ATTR,
							new Object[] { "Unknown", "Unobtainable" }));
					throw ExceptionFactory.createGeneralCLFCException(
							MsgIds.WRONG_TYPE_FOR_ATTR, lp);
				}
				ProgramParameter progArg = null;

				if (cmdArgsCharEncode != null) {
					try {
						progArg = new ProgramParameter(
								((String) args[i]).getBytes(cmdArgsCharEncode));
					} catch (java.io.UnsupportedEncodingException e) {
						throw ExceptionFactory
								.createGeneralCLFCException(e, lp);
					}
				} else {
					progArg = new ProgramParameter(
							((String) args[i]).getBytes());
				}
				strBuf.append(logArgStr);
				strBuf.append(args[i]);
				strBuf.append(" ");
				logArgStr = strBuf.toString();
				progArgs[i] = progArg;
			}
			if (((AS400Protocol) getRXAProtocol()) != null) {
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.PRIOR_TO_EXECUTION,
						new Object[] { cmd + logArgStr }));
				po = ((AS400Protocol) getRXAProtocol()).runProgram(c, progArgs);
				lp.debug(MessageHelper.getMsgResource().getMessage(
						MsgIds.EXECUTION_COMPLETE, null));
			} else {
				lp.error(MessageHelper.getMsgResource().getMessage(
						MsgIds.REMOTE_CONNECT_ERROR, null));
			}
		} catch (ConnectException e) {
			throw ExceptionFactory.createGeneralCLFCException(e, lp);
		}

		if (po != null) {
			if (po.isTimeoutExpired()) {
				if (!(randomDir.equals(""))) {
					try {
						lp.debug(MessageHelper.getMsgResource().getMessage(
								MsgIds.REMOVE_DIR, new Object[] { randomDir }));
						((AS400Protocol) getRXAProtocol())
								.setCurrentDirectory(parentDirToStdin);
						((AS400Protocol) getRXAProtocol()).rm(randomDir, true,
								true);
					} catch (ConnectException ce) {
						throw ExceptionFactory.createGeneralCLFCException(ce,
								lp);
					} catch (FileNotFoundException fnfe) {
						throw ExceptionFactory.createGeneralCLFCException(fnfe,
								lp);
					} catch (IOException io) {
						throw ExceptionFactory.createGeneralCLFCException(io,
								lp);
					}
				}
				throw ExceptionFactory.createGeneralCLFCException(
						MsgIds.TIMEOUT, lp);
			}
			rsp = getEntryResponse(po);
		}

		return rsp;
	}

	/**
	 * Transfer the specified file from the local machine to a specified
	 * destination on the remote target
	 * 
	 * @param localPath
	 *            Path to the source file on the local machine
	 * @param remotePath
	 *            Path to where the file is to be stored on the remote machine
	 * @throws GeneralCLFCException
	 *             if the transfer fails
	 */
	public void transferFile(String localPath, String remotePath)
			throws GeneralCLFCException {
		try {
			((AS400Protocol) getRXAProtocol()).putFile(localPath, remotePath);
		} catch (ConnectException ce) {
			throw ExceptionFactory.createGeneralCLFCException(ce, lp);
		} catch (FileNotFoundException fnfe) {
			throw ExceptionFactory.createGeneralCLFCException(fnfe, lp);
		} catch (IOException io) {
			throw ExceptionFactory.createGeneralCLFCException(io, lp);
		}
	}
}
