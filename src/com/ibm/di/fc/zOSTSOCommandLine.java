/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Locale;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * The zOSTSOCommandLine Function Component executes specified commands in zOS
 * TSO command line. It uses APPC protocol to communicate with the zOS
 * environment.
 * <p>
 * In order for the zOS TSO Command Line Function Component to successfully
 * connect and execute commands the APPC should be started and running on the
 * zOS machine. Also this function component can execute commands only on the
 * same machine on which the IBM Tivoli Directory Integrator is running.
 * <p>
 * The connection to the MVS is maintained by four native methods implemented on
 * the program language 'C' which on the other hand use REXX script to execute
 * commands on behalf of the IBM Tivoli Directory Integrator.
 */
public class zOSTSOCommandLine extends Function {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Used for testing purposes on z/OS to log on the console
	 */
	private static final boolean CONSOLE_LOG = false;

	/**
	 * Parameter Name: {@value #LIB_NAME}
	 */
	public static final String LIB_NAME = "tdiappc";

	/**
	 * Parameter Name: {@value #PARM_PARTNERTP}
	 */
	public static final String PARM_PARTNERTP = "partnerTP";

	/**
	 * Parameter Name: {@value #PARM_DEST_LU_NAME}
	 */
	public static final String PARM_DEST_LU_NAME = "destLuName";

	/**
	 * Parameter Name: {@value #PARM_SRC_LU_NAME}
	 */
	public static final String PARM_SRC_LU_NAME = "srcLuName";

	/**
	 * Parameter Name: {@value #PARM_MODE_NAME}
	 */
	public static final String PARM_MODE_NAME = "modeName";

	/**
	 * Parameter Name: {@value #PARM_USER_ID}
	 */
	public static final String PARM_USER_ID = "userID";

	/**
	 * Parameter Name: {@value #PARM_USER_PSW}
	 */
	public static final String PARM_USER_PSW = "userPSW";

	/**
	 * ID of the parameter: {@value #PARM_PARTNERTP_ID}
	 */
	public static final int PARM_PARTNERTP_ID = 0;

	/**
	 * ID of the parameter: {@value #PARM_DEST_LU_NAME_ID}
	 */
	public static final int PARM_DEST_LU_NAME_ID = 1;

	/**
	 * ID of the parameter: {@value #PARM_SRC_LU_NAME_ID}
	 */
	public static final int PARM_SRC_LU_NAME_ID = 2;

	/**
	 * ID of the parameter: {@value #PARM_MODE_NAME_ID}
	 */
	public static final int PARM_MODE_NAME_ID = 3;

	/**
	 * ID of the parameter: {@value #PARM_USER_ID_ID}
	 */
	public static final int PARM_USER_ID_ID = 4;

	/**
	 * ID of the parameter: {@value #PARM_USER_PSW_ID}
	 */
	public static final int PARM_USER_PSW_ID = 5;

	/**
	 * Allowed length of parameter
	 */
	public static final int[] PARAMS_LEN = new int[6];

	/**
	 * Attribute name: {@value #ENTRY_ATTR_CMD}
	 */
	public static final String ENTRY_ATTR_CMD = "command";

	/**
	 * Attribute name: {@value #ENTRY_ATTR_COMMAND_OUTPUT}
	 */
	public static final String ENTRY_ATTR_COMMAND_OUTPUT = "commandOutput";

	/**
	 * Attribute name: {@value #ENTRY_ATTR_APPC_RETCODE}
	 */
	public static final String ENTRY_ATTR_APPC_RETCODE = "appcReturnCode";

	/**
	 * Attribute name: {@value #ENTRY_ATTR_TSO_CMD_RETCODE}
	 */
	public static final String ENTRY_ATTR_TSO_CMD_RETCODE = "tsoCommandReturnCode";

	/**
	 * Message indicating that no response was received
	 */
	public static final String MSG_NO_RESPONSE = "NO RESPONSE RECEIVED";

	/**
	 * Flag for loaded library. Default <code>false</code>.
	 */
	public static boolean isLibLoaded = false;

	/**
	 * APPC parameters
	 */
	private String[] mAPPCParams = null;

	/**
	 * Flag for performed initialization. Default <code>false</code>.
	 */
	private boolean mInitialized = false;

	/**
	 * Value of the returned code for 'OK'
	 */
	public int mATBOK = 0;

	/**
	 * Currently used byte array output stream
	 */
	private ByteArrayOutputStream mBuffer = new ByteArrayOutputStream(4096);

	/**
	 * ID of the conversation initialized using APPC.
	 */
	private byte mAppcConvID[] = new byte[9];

	/**
	 * Last returned code of native called functions.
	 */
	private int mLastRetCode = -1;

	static {
		PARAMS_LEN[PARM_PARTNERTP_ID] = 8;
		PARAMS_LEN[PARM_DEST_LU_NAME_ID] = 17;
		PARAMS_LEN[PARM_SRC_LU_NAME_ID] = 17;
		PARAMS_LEN[PARM_MODE_NAME_ID] = 8;
		PARAMS_LEN[PARM_USER_ID_ID] = 10;
		PARAMS_LEN[PARM_USER_PSW_ID] = 10;
	}

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "zostsocommandlinefc";

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Initializes the function component.
	 * 
	 * @param aParam
	 *            Object to initialize; not used
	 * @throws Exception
	 */
	public void initialize(Object aParam) throws Exception {
		if (mInitialized) {
			return;
		}

		zOSTSOCommandLine.loadLibrary();

		mAPPCParams = new String[6];
		readZOSParam(PARM_PARTNERTP, PARM_PARTNERTP_ID, true, true, true);
		readZOSParam(PARM_DEST_LU_NAME, PARM_DEST_LU_NAME_ID, false, true, true);
		readZOSParam(PARM_SRC_LU_NAME, PARM_SRC_LU_NAME_ID, false, true, true);
		readZOSParam(PARM_MODE_NAME, PARM_MODE_NAME_ID, false, true, true);
		readZOSParam(PARM_USER_ID, PARM_USER_ID_ID, false, true, true);
		readZOSParam(PARM_USER_PSW, PARM_USER_PSW_ID, false, false, false);

		initAndPrepareForConversation();

		mInitialized = true;

		super.initialize(null);
	}

	/**
	 * Retrieves zOS parameter.
	 * 
	 * @param aParamName
	 *            parameter name
	 * @param aParamId
	 *            parameter ID
	 * @param aRequired
	 *            <code>true</code> indicates required parameter
	 * @param aToLog
	 *            if <code>true</code> to log to the console if debug/detailed
	 *            logging is turned on
	 * @param aToUpperCase
	 *            <code>true</code> if we want to uppercase parameter value;
	 *            <code>false</code> otherwise.
	 * @throws Exception
	 *             <li>if invalid parameter value is encountered</li> <li>if
	 *             required parameter value is <code>null</code></li>
	 */
	protected void readZOSParam(String aParamName, int aParamId,
			boolean aRequired, boolean aToLog, boolean aToUpperCase)
			throws Exception {
		String paramValue = (String) getParam(aParamName);
		if (paramValue != null) {
			paramValue = paramValue.trim();
		}

		if (aToLog && getDebug()) {
			debug(sResHash.getString("FC.ZOSTSOCOMMANDLINE.PARAMETER",
					new Object[] { aParamName, paramValue }));
		}

		if (paramValue == null || paramValue.length() == 0) {
			if (aRequired) {
				logErrorAndThrowException(sResHash.getString(
						"FC.ZOSTSOCOMMANDLINE.REQUIRED.PARAMETER.MISSING",
						aParamName));
			} else {
				return;
			}
		}

		if (aToUpperCase) {
			String paramToUpper = paramValue.toUpperCase(Locale.ENGLISH);
			if (!paramToUpper.equals(paramValue)) {
				paramValue = paramToUpper;
				if (aToLog) {
					logmsg(sResHash
							.getString(
									"FC.ZOSTSOCOMMANDLINE.PARAMETER.TRANSFORMED.FROM.TO",
									new Object[] { aParamName, paramValue,
											paramToUpper }));
				} else {
					logmsg(sResHash
							.getString(
									"FC.ZOSTSOCOMMANDLINE.PARAMETER.TRANSFORMED.TO.UPPER.CASE",
									aParamName));
				}
			}
		}

		if (paramValue.length() > PARAMS_LEN[aParamId]) {
			logErrorAndThrowException(sResHash.getString(
					"FC.ZOSTSOCOMMANDLINE.PARAMETER.TOO.LONG", new Object[] {
							aParamName, paramValue,
							Integer.valueOf(PARAMS_LEN[aParamId]) }));
		}

		// fill in the value to the maximum length
		StringBuffer buf = new StringBuffer();
		buf.append(paramValue);

		while (buf.length() < PARAMS_LEN[aParamId]) {
			buf.append(' ');
		}

		paramValue = buf.toString();

		mAPPCParams[aParamId] = paramValue;
	}

	/**
	 * Initializes and prepares the FC.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initAndPrepareForConversation() throws Exception {
		mATBOK = getCMOK();
		if (getDebug()) {
			debug(sResHash.getString("FC.ZOSTSOCOMMANDLINE.ATB.OK.CODE.IS",
					Integer.valueOf(mATBOK)));
		}

		String encoding = System.getProperty("file.encoding");
		Charset charSet = Charset.forName(encoding);
		encoding = charSet.name();

		mLastRetCode = initConversation(mAppcConvID, mAPPCParams, encoding
				.getBytes());
		if (mLastRetCode == mATBOK) {
			logmsg(sResHash
					.getString("FC.ZOSTSOCOMMANDLINE.APPC.CONVERSATION.SUCCESSFULLY.ALLOCATED"));
		} else {
			logErrorAndThrowException(sResHash
					.getString(
							"FC.ZOSTSOCOMMANDLINE.COULD.NOT.ALLOCATE.APPC.CONVERSATION",
							Integer.valueOf(mLastRetCode)));
		}
	}

	/**
	 * Loads IBM Tivoli Directory Integrator library for APPC.
	 */
	public static synchronized void loadLibrary() {
		if (!isLibLoaded) {
			System.loadLibrary(LIB_NAME);
			isLibLoaded = true;
		}
	}

	/**
	 * Executes command on zOS TSO command line.
	 * 
	 * @param aInObject
	 *            initial entry to proceed
	 * @return Entry object containing command to execute
	 * @throws Exception
	 *             <li>if invalid parameter is passed</li> <li>or invalid TSO
	 *             code returned</li>
	 */
	public Object perform(Object aInObject) throws Exception {
		verifyInitialized();

		if (!(aInObject instanceof Entry)) {
			throw new Exception(
					sResHash
							.getString("FC.ZOSTSOCOMMANDLINE.NON.ENTRY.OBJECTS.ARE.NOT.SUPPORTED"));
		}

		Entry inEntry = (Entry) aInObject;

		String command = inEntry.getString(ENTRY_ATTR_CMD);
		String cmdOutput = executeTSO(command);
		if (cmdOutput == null) {
			logErrorAndThrowException(sResHash
					.getString("FC.ZOSTSOCOMMANDLINE.CONVERSATION.FAILED.C.LAYER.EXECUTE.RET.NULL"));
		}

		Entry outEntry = new Entry();

		// get APPC return code
		outEntry.addAttributeValue(ENTRY_ATTR_APPC_RETCODE, String
				.valueOf(mLastRetCode));

		// get TSO return code
		int slashInd = cmdOutput.indexOf("//");
		if (slashInd > 0) {
			outEntry.addAttributeValue(ENTRY_ATTR_TSO_CMD_RETCODE, cmdOutput
					.substring(0, slashInd));
		} else {
			logErrorAndThrowException(sResHash.getString(
					"FC.ZOSTSOCOMMANDLINE.INVALID.DATA.RETURNED.FROM.C.LAYER",
					cmdOutput));
		}

		// get command output
		if (slashInd + 3 < cmdOutput.length()) {
			cmdOutput = cmdOutput.substring(slashInd + 3);
		} else {
			if (getDebug()) {
				debug(sResHash
						.getString("FC.ZOSTSOCOMMANDLINE.NO.OUTPUT.RETURNED.BY.THE.TSO.COMMAND"));
			}
			cmdOutput = MSG_NO_RESPONSE;
		}

		outEntry.addAttributeValue(ENTRY_ATTR_COMMAND_OUTPUT, cmdOutput);

		return outEntry;
	}

	/**
	 * Executes the TSO command passed as parameter.
	 * 
	 * @param aTsoCmd
	 *            The TSO command to be executed.
	 * @return The result of the z/OS TSO command.
	 * @throws Exception
	 */
	protected String executeTSO(String aTsoCmd) throws Exception {
		if (mBuffer.size() > 0)
			mBuffer.reset();

		if (getDebug()) {
			debug(sResHash.getString("FC.ZOSTSOCOMMANDLINE.COMMAND.TO.EXECUTE",
					aTsoCmd));
		}
		mLastRetCode = executeCommand(mAppcConvID, aTsoCmd, mBuffer);
		if (mLastRetCode != mATBOK) {
			logErrorAndThrowException(sResHash.getString(
					"FC.ZOSTSOCOMMANDLINE.COULD.NOT.EXECUTE.TSO.COMMAND",
					Integer.valueOf(mLastRetCode)));
		}

		String commandOutput = null;
		if (mBuffer.size() > 0) {
			commandOutput = mBuffer.toString();
		}
		if (getDebug()) {
			debug(sResHash.getString("FC.ZOSTSOCOMMANDLINE.TSOCOMMAND.OUTPUT",
					commandOutput));
		}

		return commandOutput;
	}

	/**
	 * Terminates the function component.
	 * 
	 * @throws Exception
	 */
	public void terminate() throws Exception {
		try {
			if (!mInitialized) {
				return;
			}

			if (mLastRetCode == mATBOK) {
				int dealocateCode = deallocConversation(mAppcConvID);
				if (dealocateCode == mATBOK) {
					logmsg(sResHash
							.getString("FC.ZOSTSOCOMMANDLINE.CONVERSATION.DEALLOCATED.SUCCESSFULLY"));
				} else {
					logmsg(sResHash
							.getString(
									"FC.ZOSTSOCOMMANDLINE.ERROR.ON.DEALLOCATING.THE.CONVERSATION",
									Integer.valueOf(dealocateCode)));
				}
			}
			mInitialized = false;
		} finally {
			super.terminate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void logmsg(String msg) {
		if (CONSOLE_LOG) {
			System.out.println(msg);
		} else {
			super.logmsg(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void debug(String msg) {
		if (CONSOLE_LOG) {
			System.out.println(msg);
		} else {
			super.debug(msg);
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Logs an error message and throws an exception.
	 * 
	 * @param aErrorMsg
	 *            message text
	 * @throws Exception
	 */
	private void logErrorAndThrowException(String aErrorMsg) throws Exception {
		logmsg(sResHash.getString("FC.ZOSTSOCOMMANDLINE.LOG.ERROR", aErrorMsg));
		throw new Exception(aErrorMsg);
	}

	/* Native Calls */

	/**
	 * Native call to initialize conversation
	 * 
	 * @return returnCode
	 * 
	 * @param convID
	 *            conversation ID
	 * @param params
	 *            array of parameters
	 * @param encoding
	 *            encoding to be used
	 */
	private native int initConversation(byte[] convID, String params[],
			byte encoding[]);

	/**
	 * Native call to execute command
	 * 
	 * @return returnCode
	 * 
	 * @param convID
	 *            conversation ID
	 * @param cmd
	 *            command to execute
	 * @param output
	 *            output
	 */
	private native int executeCommand(byte[] convID, String cmd,
			ByteArrayOutputStream output);

	/**
	 * Native call to de-allocate conversation
	 * 
	 * @return returnCode
	 * 
	 * @param convID
	 *            conversation ID
	 */
	private native int deallocConversation(byte[] convID);

	/**
	 * Returns the code for successfully performed operation (typically 0)
	 * 
	 * @return the code for OK
	 */
	private native int getCMOK();

}
