/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidocv3;

import com.ibm.di.server.ResourceHash;

/**
 * Provides method access to bundle messages and time constants for message
 * bundle keys.
 */
final class LogMessageHelper {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final String PROPERTIES_FILE = "sapidocconnector";

	//
	// urcmessages must be loadable by this classes classloader.
	// CTGDIK301E (301-400)
	//  
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/*
	 * Messages keys loaded within the message resource.
	 */
	// loginfo"SAP_ALEIDOC_0001: Started JCo IDoc Server and listening for
	// requests."
	static final String SAP_ALEIDOC_0001 = "SAP_ALEIDOC_0001";

	// logerror"SAP_ALEIDOC_0002: Exception generated during Connector
	// initialization: {0}"
	static final String SAP_ALEIDOC_0002 = "SAP_ALEIDOC_0002";

	// logerror"SAP_ALEIDOC_0003: RFC Connection parameter {0} is mandatory.
	static final String SAP_ALEIDOC_0003 = "SAP_ALEIDOC_0003";

	// logdebug"SAP_ALEIDOC_0004: Optional RFC Connection parameter {0} provided
	// with value {0}."
	static final String SAP_ALEIDOC_0004 = "SAP_ALEIDOC_0004";

	// logdebug"SAP_ALEIDOC_0005: Connector configuration parameter {0} supplied
	// with value {0}."
	static final String SAP_ALEIDOC_0005 = "SAP_ALEIDOC_0005";

	// logdebug"SAP_ALEIDOC_0006: Checking if TID Manager for TID {0} is
	// available for processing."
	static final String SAP_ALEIDOC_0006 = "SAP_ALEIDOC_0006";

	// logerror"SAP_ALEIDOC_0007: No TID Manager found for TID {0}."
	static final String SAP_ALEIDOC_0007 = "SAP_ALEIDOC_0007";

	// loginfo"SAP_ALEIDOC_0008: TIDManager for tid {0} is available for
	// processing"."
	static final String SAP_ALEIDOC_0008 = "SAP_ALEIDOC_0008";

	// logwarn"SAP_ALEIDOC_0009: Provided poll time value is 0 or less. Single
	// request will be processed only."
	static final String SAP_ALEIDOC_0009 = "SAP_ALEIDOC_0009";

	// logdebug"SAP_ALEIDOC_0010: polling for next IDoc request in
	// getNextEntry()"
	static final String SAP_ALEIDOC_0010 = "SAP_ALEIDOC_0010";

	// logerror"SAP_ALEIDOC_0011: Exception generated while polling in call to
	// Thread.sleep(pollTime)."
	static final String SAP_ALEIDOC_0011 = "SAP_ALEIDOC_0011";

	// logerror"SAP_ALEIDOC_0012: Null Entry found in the Entry list of the TID
	// Manager for TID {0}.""
	static final String SAP_ALEIDOC_0012 = "SAP_ALEIDOC_0012";

	// logwarn"SAP_ALEIDOC_0013: No TID provided with this RFM call. Manualy
	// removing the TIDManager to complete the TID Management cycle."
	static final String SAP_ALEIDOC_0013 = "SAP_ALEIDOC_0013";

	// logdebug"SAP_ALEIDOC_0014: No Parser Attached to the Connector.""
	static final String SAP_ALEIDOC_0014 = "SAP_ALEIDOC_0014";

	// loginfo"SAP_ALEIDOC_0015: Finished processing TID Manager for TID {0}.
	// TID ready for confirmation."
	static final String SAP_ALEIDOC_0015 = "SAP_ALEIDOC_0015";

	// logerror"SAP_ALEIDOC_0016: getNextEntry() should never return null in
	// iterator mode with a valid poll time."
	static final String SAP_ALEIDOC_0016 = "SAP_ALEIDOC_0016";

	// logwarn"SAP_ALEIDOC_0017: Entry contains no XML attributes to parse."
	static final String SAP_ALEIDOC_0017 = "SAP_ALEIDOC_0017";

	// logdebug"SAP_ALEIDOC_0018: "Attempting to parse XML content with attached
	// Parser."
	static final String SAP_ALEIDOC_0018 = "SAP_ALEIDOC_0018";

	// logwarn"SAP_ALEIDOC_0019: The attached Parser returned a null Entry."
	static final String SAP_ALEIDOC_0019 = "SAP_ALEIDOC_0019";

	// logerror"SAP_ALEIDOC_0020: Exception generated while parsing: {0}"
	static final String SAP_ALEIDOC_0020 = "SAP_ALEIDOC_0020";

	// logwarn"SAP_ALEIDOC_0021: Exception generated while generating schema:
	// {0}"
	static final String SAP_ALEIDOC_0021 = "SAP_ALEIDOC_0021";

	// logerror"SAP_ALEIDOC_0022: Unable to start JCo IDoc Server."
	static final String SAP_ALEIDOC_0022 = "SAP_ALEIDOC_0022";

	// logdebug"SAP_ALEIDOC_0023: polling for next IDoc request in
	// getNextClient()"
	static final String SAP_ALEIDOC_0023 = "SAP_ALEIDOC_0023";

	// logdebug"SAP_ALEIDOC_0024: onCheckTID({0}) invoked. TID Management
	// already in progress for this TID."
	static final String SAP_ALEIDOC_0024 = "SAP_ALEIDOC_0024";

	// logdebug"SAP_ALEIDOC_0025: onCheckTID({0}) invoked. TID Management
	// beginning now for this TID. Next handleRequest() call will begin the
	// inbound processing."
	static final String SAP_ALEIDOC_0025 = "SAP_ALEIDOC_0025";

	// logdebug"SAP_ALEIDOC_0026: TID Manager for TID {0} is not yet confirmed.
	// Current status is {0}"
	static final String SAP_ALEIDOC_0026 = "SAP_ALEIDOC_0026";

	// loginfo"SAP_ALEIDOC_0027: TID Manager for TID {0} is now confirmed."
	static final String SAP_ALEIDOC_0027 = "SAP_ALEIDOC_0027";

	// logdebug"SAP_ALEIDOC_0028: handleRequest(documentList) invoked. Current
	// TID value is {0}. Incoming IDoc list request contains {0} IDocs"
	static final String SAP_ALEIDOC_0028 = "SAP_ALEIDOC_0028";

	// logerror"SAP_ALEIDOC_0029: writeIDocToTDIEntry() reutned null IDoc
	// Entry."
	static final String SAP_ALEIDOC_0029 = "SAP_ALEIDOC_0029";

	// logerror"SAP_ALEIDOC_0030: No value provided for current TID. This list
	// of IDocs will not be processed.
	static final String SAP_ALEIDOC_0030 = "SAP_ALEIDOC_0030";

	// logerror"SAP_ALEIDOC_0031: Current TID {0} already has a TID Manager and
	// is being processed. Ignoring this request.
	static final String SAP_ALEIDOC_0031 = "SAP_ALEIDOC_0031";

	// logerror"SAP_ALEIDOC_0032: No IDoc Entries available. Ignoring request
	// for TID {0}.
	static final String SAP_ALEIDOC_0032 = "SAP_ALEIDOC_0032";

	// loginfo"SAP_ALEIDOC_0033: Craeted TID Manager for TID {0} with {0} IDoc
	// entries for processing.
	static final String SAP_ALEIDOC_0033 = "SAP_ALEIDOC_0033";

	// logerror"SAP_ALEIDOC_0034: Passed JCoIDoc.Document object was null."
	static final String SAP_ALEIDOC_0034 = "SAP_ALEIDOC_0034";

	// logdebug"SAP_ALEIDOC_0035: handleRequest(function) invoked. Current TID
	// value is {0}. Incoming RFM request is {0}."
	static final String SAP_ALEIDOC_0035 = "SAP_ALEIDOC_0035";

	// logwarn"SAP_ALEIDOC_0036: No TID received with this RFM request.
	// Defaulting for TID Management processing."
	static final String SAP_ALEIDOC_0036 = "SAP_ALEIDOC_0036";

	// logdebug"SAP_ALEIDOC_0037: RFM as XML: {0}"
	static final String SAP_ALEIDOC_0037 = "SAP_ALEIDOC_0037";

	// logerror"SAP_ALEIDOC_0038: No RFMs provided. TID will not be added for
	// TID Management."
	static final String SAP_ALEIDOC_0038 = "SAP_ALEIDOC_0038";

	// logerror"SAP_ALEIDOC_0039: Passed JCO.Function object was null."
	static final String SAP_ALEIDOC_0039 = "SAP_ALEIDOC_0039";

	// logdebug"SAP_ALEIDOC_0040: Storing RFM request as XML file '{0}'"
	static final String SAP_ALEIDOC_0040 = "SAP_ALEIDOC_0040";

	// logerror"SAP_ALEIDOC_0041: Failed to read RFM request XML file '{0}' into
	// TDI Entry. No contents found in file."
	static final String SAP_ALEIDOC_0041 = "SAP_ALEIDOC_0041";

	// logerror"SAP_ALEIDOC_0042: Exception encounted while processing RFM XML
	// file. {0}"
	static final String SAP_ALEIDOC_0042 = "SAP_ALEIDOC_0042";

	// logerror"SAP_ALEIDOC_0043: Exception trapped from JCo Server with Program
	// ID of '{0}': {0}
	static final String SAP_ALEIDOC_0043 = "SAP_ALEIDOC_0043";

	// logerror"SAP_ALEIDOC_0044: Error message trapped from JCo Server with
	// Program ID of '{0}': {0}
	static final String SAP_ALEIDOC_0044 = "SAP_ALEIDOC_0044";

	// loginfo"SAP_ALEIDOC_0045: State Change occured for JCo Server with
	// Program ID of '{0}'. Previous State:{0} New State:{0}
	static final String SAP_ALEIDOC_0045 = "SAP_ALEIDOC_0045";

	// logdebug"SAP_ALEIDOC_0046: "Trace message trapped from JCo Server. Trace
	// Level:{0} Trace Message:{0}
	static final String SAP_ALEIDOC_0046 = "SAP_ALEIDOC_0046";

	// logerror"SAP_ALEIDOC_0047: Error encountered when attempting to delete
	// file. {0}"
	static final String SAP_ALEIDOC_0047 = "SAP_ALEIDOC_0047";
	
	static final String SAP_ALEIDOC_0048 = "SAP_ALEIDOC_0048";
	
	static final String SAP_ALEIDOC_0049 = "SAP_ALEIDOC_0049";
	
	static final String SAP_ALEIDOC_0050 = "SAP_ALEIDOC_0050";
	
	static final String SAP_ALEIDOC_0051 = "SAP_ALEIDOC_0051";
	
	static final String SAP_ALEIDOC_0052 = "SAP_ALEIDOC_0052";
	
	static final String SAP_ALEIDOC_0053 = "SAP_ALEIDOC_0053";
	
	static final String SAP_ALEIDOC_0054 = "SAP_ALEIDOC_0054";
	
	static final String SAP_ALEIDOC_0055 = "SAP_ALEIDOC_0055";
	
	static final String SAP_ALEIDOC_0056 = "SAP_ALEIDOC_0056";
	
	static final String SAP_ALEIDOC_0057 = "SAP_ALEIDOC_0057";
	
	static final String SAP_ALEIDOC_0058 = "SAP_ALEIDOC_0058";
	
	static final String SAP_ALEIDOC_0059 = "SAP_ALEIDOC_0059";
	
	static final String SAP_ALEIDOC_0060 = "SAP_ALEIDOC_0060";
	
	static final String SAP_ALEIDOC_0061 = "SAP_ALEIDOC_0061";
	
	static final String SAP_ALEIDOC_0062 = "SAP_ALEIDOC_0062";
	
	static final String SAP_ALEIDOC_0063 = "SAP_ALEIDOC_0063";
	
	static final String SAP_ALEIDOC_0064 = "SAP_ALEIDOC_0064";
	
	static final String SAP_ALEIDOC_0065 = "SAP_ALEIDOC_0065";
	
	static final String SAP_ALEIDOC_0066 = "SAP_ALEIDOC_0066";
	
	static final String SAP_ALEIDOC_0067 = "SAP_ALEIDOC_0067";
	
	static final String SAP_ALEIDOC_0068 = "SAP_ALEIDOC_0068";
	
	static final String SAP_ALEIDOC_0069 = "SAP_ALEIDOC_0069";
	
	static final String SAP_ALEIDOC_0070 = "SAP_ALEIDOC_0070";
	
	static final String SAP_ALEIDOC_0071 = "SAP_ALEIDOC_0071";
	
	static final String SAP_ALEIDOC_0072 = "SAP_ALEIDOC_0072";
	
	static final String SAP_ALEIDOC_0073 = "SAP_ALEIDOC_0073";
	
	static final String SAP_ALEIDOC_0074 = "SAP_ALEIDOC_0074";

	static final String SAP_ALEIDOC_0075 = "SAP_ALEIDOC_0075";

	/**
	 * Get a message from the loaded messages resource.
	 * 
	 * @param msgID -
	 *            The message ID for the loaded message.
	 * @return The loaded message
	 */
	public static String getMessage(String msgID) {
		return sResHash.getString(msgID);
	}

	/**
	 * Get a message from the loaded messages resource that requires input
	 * arguments in it's construction.
	 * 
	 * @param msgID -
	 *            The message ID for the loaded message.
	 * @param args -
	 *            The message input arguments.
	 * @return The loaded message
	 */
	public static String getMessage(String msgID, Object[] args) {
		return sResHash.getString(msgID, args);
	}

	/**
	 * Disabled Constructor.
	 */
	private LogMessageHelper() {
		super();
	}
}
