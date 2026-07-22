/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.protocols.rxa;

/**
 * @author alblair
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/**
 * Produce new exceptions and log appropriate messages
 */
public class ExceptionFactory {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A hashtable used to convert RXA Toolkit exception IDs into exception IDs
	 * specific to the FC.
	 */
	private static Map mapCodes = Collections.synchronizedMap(new HashMap(14));

	/**
	 * Stores if the map has been initialized
	 */
	private static boolean mapInitialized = false;

	/**
	 * Logger
	 */
	private static LogProxy log = null;

	/**
	 * ID of the exception to be thrown
	 */
	private static MsgIds exceptionID = null;

	/**
	 * Contents of the exception to be thrown
	 */
	private static String msgContents = null;

	/*
	 * TMSXML unique error prefixes
	 */
	/**
	 * Error id that prefixes all Remote CLFC exceptions
	 */
	public static final String FC_PREFIX = "CTGDJC";

	/**
	 * Error id that prefixes all RXA Toolkit exceptions
	 */
	public static final String RXA_PREFIX = "CTGRI";

	/**
	 * Construct a ParamException
	 * 
	 * @param err
	 * @param lg
	 * @return ParamException
	 */
	public static ParamException createParamException(MsgIds err,
			LogProxy lg) {
		log = lg;
		exceptionID = err;
		msgContents = null;
		generateLoggedMsg();
		return new ParamException(err, msgContents);
	}

	/**
	 * Construct a RemoteConnectException
	 * 
	 * @param err
	 * @param lg
	 * @return RemoteConnectException
	 */
	public static RemoteConnectException createRemoteConnectException(
			MsgIds err, LogProxy lg) {
		log = lg;
		exceptionID = err;
		msgContents = null;
		generateLoggedMsg();
		return new RemoteConnectException(err, msgContents);
	}

	/**
	 * Construct a GeneralCLFCException
	 * 
	 * @param err
	 * @param lg
	 * @return GeneralCLFCException
	 */
	public static GeneralCLFCException createGeneralCLFCException(
			MsgIds err, LogProxy lg) {
		log = lg;
		msgContents = null;
		exceptionID = err;
		generateLoggedMsg();
		return new GeneralCLFCException(err, msgContents);
	}

	/**
	 * Construct a RemoteConnectException
	 * 
	 * @param root
	 * @param lg
	 * @return RemoteConnectException
	 */
	public static RemoteConnectException createRemoteConnectException(
			Throwable root, LogProxy lg) {
		log = lg;
		msgContents = root.getMessage();
		exceptionID = null;
		generateLoggedMsg();
		return new RemoteConnectException(exceptionID, msgContents, root);
	}

	/**
	 * Construct a GeneralCLFCException
	 * 
	 * @param root
	 * @param lg
	 * @return GeneralCLFCException
	 */
	public static GeneralCLFCException createGeneralCLFCException(
			Throwable root, LogProxy lg) {
		log = lg;
		msgContents = root.getMessage();
		exceptionID = null;
		generateLoggedMsg();
		return new GeneralCLFCException(exceptionID, msgContents, root);
	}

	/**
	 * Log the message that has occurred
	 */
	private static void generateLoggedMsg() {
		if (!mapInitialized) {
			initializeMap();
		}
		if (msgContents != null) {
			if (msgContents.startsWith(RXA_PREFIX)) {
				String rxaMsgID = msgContents.substring(0, 10);
				if (mapCodes.containsKey(rxaMsgID)) {
					exceptionID = (MsgIds) mapCodes.get(rxaMsgID);
					msgContents = MessageHelper.getMsgResource().getMessage(
							exceptionID);
					log.error(msgContents);
				} else {
					exceptionID = MsgIds.GENERAL_RXA_EXCEPTION;
					msgContents = MessageHelper.getMsgResource().getMessage(
							exceptionID, new Object[] { msgContents });
					log.error(msgContents);
				}
			}
		} else {
			msgContents = MessageHelper.getMsgResource()
					.getMessage(exceptionID);
			log.error(msgContents);
		}
	}

	/**
	 * Initializes the hashtable used to convert RXA Toolkit exception IDs into
	 * exception IDs specific to the FC.
	 */
	private static void initializeMap() {
		mapCodes.put(RXAErrorCode.RXATOOLKIT_0E.toString(),
				RXAErrorCode.RXATOOLKIT_0E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_1E.toString(),
				RXAErrorCode.RXATOOLKIT_1E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_3E.toString(),
				RXAErrorCode.RXATOOLKIT_3E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_4E.toString(),
				RXAErrorCode.RXATOOLKIT_4E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_7E.toString(),
				RXAErrorCode.RXATOOLKIT_7E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_9E.toString(),
				RXAErrorCode.RXATOOLKIT_9E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_10E.toString(),
				RXAErrorCode.RXATOOLKIT_10E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_14E.toString(),
				RXAErrorCode.RXATOOLKIT_14E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_15E.toString(),
				RXAErrorCode.RXATOOLKIT_15E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_16E.toString(),
				RXAErrorCode.RXATOOLKIT_16E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_19E.toString(),
				RXAErrorCode.RXATOOLKIT_19E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_20E.toString(),
				RXAErrorCode.RXATOOLKIT_20E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_22E.toString(),
				RXAErrorCode.RXATOOLKIT_22E.getCorrespondingCode());
		mapCodes.put(RXAErrorCode.RXATOOLKIT_23E.toString(),
				RXAErrorCode.RXATOOLKIT_23E.getCorrespondingCode());
		mapInitialized = true;
	}
}
