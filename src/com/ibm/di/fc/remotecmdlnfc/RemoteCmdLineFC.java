/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.remotecmdlnfc;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.protocols.rxa.ExceptionFactory;
import com.ibm.di.protocols.rxa.GeneralCLFCException;
import com.ibm.di.protocols.rxa.LogProxy;
import com.ibm.di.protocols.rxa.LogProxyImpl;
import com.ibm.di.protocols.rxa.MessageHelper;
import com.ibm.di.protocols.rxa.MsgIds;
import com.ibm.di.protocols.rxa.ParamException;
import com.ibm.di.protocols.rxa.TDIRXALogAdapter;
import com.ibm.di.server.Log;
import com.ibm.tivoli.remoteaccess.BaseProtocol;
import com.ibm.tivoli.remoteaccess.log.Level;
import com.ibm.tivoli.remoteaccess.log.Logger;

/**
 * <p>
 * TDI Remote Command Line Function Component.
 * </p>
 *
 * <p>
 * This function component (FC) provides the ability to execute a specified
 * command on a remote machine.
 * </p>
 *
 * <p>
 * This FC establishes connections with and invokes commands on remote machines
 * using the IBM Remote Execution and Access toolkit. To use this function
 * component you must have the Remote Command Line FC (with its included jar
 * files) installed correctly on your local machine. The remote machine you wish
 * to execute commands on must have at least one of the following connection
 * protocols configured and running:
 * <ul>
 * <li>RSH
 * <li>REXEC
 * <li>SSH
 * <li>A windows connection protocol such as SMB,CIFS,DCE-RPC
 * </ul>
 * </p>
 *
 * <p>
 * Configuration is accomplished by setting logon parameters for client
 * connections to the specified remote machine (target) where the command is to
 * be executed. See {@link #initialize} for more details on how to initialize.
 * </p>
 *
 * <p>
 * {@link #initialize} must be the first operation called in this class.<br>
 * {@link #perform} can then be called one or more times.<br>
 * {@link #terminate} must be called to allow connection cleanup before the
 * class is destroyed.
 * </p>
 *
 * <p>
 * The following configuration parameters are available and should be set prior
 * to calling {@link #initialize}. These values can be set through script by
 * setting the available parameter or on the configurable TDI GUI interface for
 * the FC.
 *
 * <table border = "1">
 * <tr>
 * <th>Configuration Item</th>
 * <th>Available Parameter</th>
 * <tr>
 * <td>Hostname</td>
 * <td>PARAM_CONFIG_HOST</td>
 * </tr>
 * <tr>
 * <td>Remote User Name</td>
 * <td>PARAM_CONFIG_USER</td>
 * </tr>
 * <tr>
 * <td>User Password</td>
 * <td>PARAM_CONFIG_PASSWD</td>
 * </tr>
 * <tr>
 * <td>Connection Protocol</td>
 * <td>PARAM_CONFIG_CONNTYPE</td>
 * </tr>
 * <tr>
 * <td>Path to Keystore</td>
 * <td>PARAM_CONFIG_KEYSTORE</td>
 * </tr>
 * <tr>
 * <td>Passphrase</td>
 * <td>PARAM_CONFIG_PASSPHRASE</td>
 * </tr>
 * <tr>
 * <td>Connection Port</td>
 * <td>PARAM_CONFIG_PORT</td>
 * </tr>
 * <tr>
 * <td>Timeout Duration (ms)</td>
 * <td>PARAM_CONFIG_TIMEOUT</td>
 * </tr>
 * <tr>
 * <td>Command to Execute</td>
 * <td>PARAM_CONFIG_COMMAND</td>
 * </tr>
 * <tr>
 * <td>Local Path to Stdin Source File</td>
 * <td>PARAM_CONFIG_STDIN_SOURCE</td>
 * </tr>
 * <tr>
 * <td>Remote Path to Stdin Destination File</td>
 * <td>PARAM_CONFIG_STDIN_DESTN</td>
 * </tr>
 * </table>
 *
 * </p>
 * <p>
 * Not all of the configuration parameters must be provided as described above.
 * Three of the configuration parameters can alternatively be provided as
 * attributes in the supplied TDI Entry object. The Entry object is supplied as
 * a parameter to the FC {@link #perform} method. <br>
 * <table border = "1">
 * <tr>
 * <th>Configuration Item</th>
 * <th>Available Attribute</th>
 * <tr>
 * <td>Command to Execute</td>
 * <td>PARAM_INPUT</td>
 * </tr>
 * <tr>
 * <td>Local Path to Stdin Source File</td>
 * <td>PARAM_STDIN_SRC</td>
 * </tr>
 * <tr>
 * <td>Remote Path to Stdin Destination File</td>
 * <td>PARAM_STDIN_DEST</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * The value provided within the TDI Entry object will have precedence over any
 * value that may have been supplied as a configuration parameter on the GUI.
 * For instance, if a value is provided for the command both on the GUI and as
 * an attribute called PARAM_INPUT in the Entry object then the value configured
 * on the GUI will be disregarded.
 * </p>
 * <p>
 * <b>Using the FC</b><br>
 * It can be placed in an assembly line or invoked directly from script. It is
 * the callers' responsibility to check the returned Entry object for any errors
 * that may have resulted from invoking the remote command. <br>
 * The following return attributes are available in the Entry object returned by
 * the FC after the {@link #perform} method has been executed: <br>
 * <table border = "1">
 * <tr>
 * <th>Returned Information</th>
 * <th>Available Attribute</th>
 * <tr>
 * <td>Standard Output</td>
 * <td>PARAM_STD_OUTPUT</td>
 * </tr>
 * <tr>
 * <td>Standard Error</td>
 * <td>PARAM_STD_ERROR</td>
 * </tr>
 * <tr>
 * <td>Return Code</td>
 * <td>PARAM_RETURN_CODE</td>
 * </tr>
 * </table>
 * <br>
 * The connection protocol (PARAM_CONFIG_CONNTYP) to be used when establishing a
 * connection:
 * <ul>
 * <li>RSH
 * <li>REXEC
 * <li>SSH
 * <li>WIN
 * <li>ANY
 * </ul>
 * <p>
 * Note that the 'WIN' option will allow you to connect to a Windows host
 * running any of the following protocols: SMB,CIFS or DCE-RPC.
 * </p>
 * <br>
 * The FC can be invoked directly from script. As an example the following code
 * can be used to invoke an remote command from JavaScript using the XML string
 * style: </p>
 *
 * <code>
 * var fc = system.getFunction("ibmdi.RemoteCmdLineFC");<br>
 * var response;<br>
 * fc.setParam(fc.PARAM_CONFIG_HOST, "rhat3");<br>
 * fc.setParam(fc.PARAM_CONFIG_USER, "SMITH");<br>
 * fc.setParam(fc.PARAM_CONFIG_PASSWD, "PASSWORD");<br>
 * fc.setParam(fc.PARAM_CONFIG_CONNTYPE, "SSH");<br>
 * fc.initialize(null);<br>
 * //Create an entry object with an input attribute containing the command to execute <br>
 * //Alternatively, the command can be specifed as the value of PARAM_CONFIG_COMMAND. <br>
 * var myEntry = system.newEntry();<br>
 * myEntry.setAttribute(fc.PARAM_INPUT, "ifconfig");<br>
 * myEntry = fc.perform(myEntry);<br>
 * var output = myEntry.getAttribute(fc.PARAM_STD_OUTPUT);<br>
 * response = output.getValue(0);<br>
 * fc.terminate();<br>
 * </code>
 *
 * <p>
 * Note that configuration parameters must be set before {@link #initialize} is
 * called, and {@link #terminate} should be called to cleanup.
 * </p>
 */
public class RemoteCmdLineFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** Hostname of Remote Machine (target) */
	public static final String PARAM_CONFIG_HOST = "hostname";

	/** The username to be used when connecting to target */
	public static final String PARAM_CONFIG_USER = "user";

	/** The port to be used when establishing connection with target */
	public static final String PARAM_CONFIG_PORT = "port";

	/** User password to authenticate to the target */
	public static final String PARAM_CONFIG_PASSWD = "passwd";

	/** Character Encoding for AS400 Program cmdArgs */
	public static final String PARAM_CONFIG_AS400CMDARGSENC = "as400ArgEnc";

	/** The connection type string. */
	public static final String PARAM_CONFIG_CONNTYPE = "connectionType";

	/** Command to execute on target */
	public static final String PARAM_CONFIG_COMMAND = "command";

	/** Timeout duration for executing the command (ms) */
	public static final String PARAM_CONFIG_TIMEOUT = "timeout";

	/**
	 * Path to the local keystore to be used when connecting to the remote
	 * machine using the SSH protocol
	 */
	public static final String PARAM_CONFIG_KEYSTORE = "keystore";

	/** The passphrase for the keystore */
	public static final String PARAM_CONFIG_PASSPHRASE = "passphrase";

	/** The local path to the standard input source file */
	public static final String PARAM_CONFIG_STDIN_SOURCE = "stdinSource";

	/** The remote path to the standard input destination file */
	public static final String PARAM_CONFIG_STDIN_DESTN = "stdinDestn";

	/** Enable or disable SSL over an AS400 connection */
	public static final String PARAM_CONFIG_AS400_SSL = "as400SSL";

	/** Proxy to be used if required for AS400 connection */
	public static final String PARAM_CONFIG_AS400_PROXY = "as400Proxy";

	/** Secondary mechanism for executing AS400 Programs */
	public static final String PARAM_CONFIG_AS400_RUNPROG = "as400RunProg";

	/** Enable or disable RXA internal logging */
	public static final String PARAM_CONFIG_RXA_LOG = "enableRXALog";

	/**
	 * The name of the entry attribute containing the command to be executed on
	 * the target. This attribute should be contained in the TDI Entry object
	 * passed to the FC perform method.
	 */
	public static final String PARAM_INPUT = "command.line";

	/**
	 * The name of the entry attribute containing the command arguments. This
	 * multi-valued attribute contains String values, where each string value is
	 * a simple, or complex argument. Simple Argument example. ls -l /tmp
	 * (command.line="ls", command.args={"-l", "/tmp" } find . -name '*.log'
	 * -exec grep 'ERROR' {} \; -print (command.line="find", command.args={".",
	 * "-name", "'*.log'", "-exec", "grep", "'ERROR'", "{} \; -print" }
	 */
	public static final String PARAM_ARGS = "command.args";

	/**
	 * The name of the entry attribute specifing the command argument delimiter.
	 * If not specified the default is a single white space character.
	 */
	public static final String PARAM_ARGS_DELIM = "command.args.delim";

	/**
	 * The return code from the executed command indicating the success of the
	 * requested operation. This attribute is contained in the TDI Entry object
	 * returned by the FC perform method.
	 */
	public static final String PARAM_RETURN_CODE = "command.returnCode";

	/**
	 * The standard output from the executed command. This attribute is
	 * contained in the TDI Entry object returned by the FC perform method.
	 */
	public static final String PARAM_STD_OUTPUT = "command.out";

	/**
	 * The standard error from the executed command. This attribute is contained
	 * in the TDI Entry object returned by the FC perform method.
	 */
	public static final String PARAM_STD_ERROR = "command.error";

	/**
	 * The name of the entry attribute containing the path to the standard input
	 * source file on the local machine. This attribute should be contained in
	 * the TDI Entry object passed to the FC perform method.
	 */
	public static final String PARAM_STDIN_SRC = "stdin.source";

	/**
	 * The name of the entry attribute containing the path to where the standard
	 * input file can be copied on the remote machine. This attribute should be
	 * contained in the TDI Entry object passed to the FC perform method.
	 */
	public static final String PARAM_STDIN_DEST = "stdin.destination";

	/**
	 * Parameter to handle as text file
	 */
	public static final String PARAM_HANDLE_TEXT_FILE = "handleAsTextFile";

	/**
	 * The version string returned by getVersion().
	 */
	private static final String VERSION_INFO = "2.0-di7.1.1 %I% 20%E%";

	/**
	 * The filename of the standard input
	 */
	private static final String STDIN_FILENAME = "stdin";

	/**
	 * The string for pipe in the standard input
	 */
	private static final String PIPE_STDIN = " < ";

	/**
	 * Empty string
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * The string for dot (.)
	 */
	private static final String FULL_STOP = ".";

	/**
	 * The string for password
	 */
	private static final String PASSWD_STRING = "*******";

	/**
	 * Timeout parameter of the connection
	 */
	public static final String PARAM_CONFIG_INITIAL_TIMEOUT = "connection_timeout";

	/**
	 * This array stores the names of the TDI GUI String parameters that can be
	 * configured (excluding those that may be set as attributes in the TDI
	 * input Entry).
	 */
	protected final String[] PARAM_STR_CONFIG_OPTIONS = { PARAM_CONFIG_HOST,
			PARAM_CONFIG_USER, PARAM_CONFIG_PASSWD, PARAM_CONFIG_CONNTYPE,
			PARAM_CONFIG_KEYSTORE, PARAM_CONFIG_PASSPHRASE,
			PARAM_CONFIG_AS400_PROXY };

	/**
	 * This array stores the names of the TDI GUI int parameters that can be
	 * configured.
	 */
	protected final String[] PARAM_INT_CONFIG_OPTIONS = { PARAM_CONFIG_PORT,
			PARAM_CONFIG_TIMEOUT, PARAM_CONFIG_INITIAL_TIMEOUT };

	/**
	 * This array stores the names of the TDI GUI boolean parameters that can be
	 * configured (excluding those that may be set as attributes in the TDI
	 * input Entry).
	 */
	protected final String[] PARAM_BOOL_CONFIG_OPTIONS = {
			PARAM_CONFIG_AS400_SSL, PARAM_CONFIG_AS400_RUNPROG,
			PARAM_CONFIG_RXA_LOG, PARAM_HANDLE_TEXT_FILE };

	/**
	 * The executor object that connects to the appropriate machine to execute
	 * the command.
	 */
	protected CmdLineExecutor executor = null;

	/**
	 * The command that is to be executed. Specified either through the TDI GUI
	 * or as an entry attribute called command.line
	 */
	protected String cmdToExecute = null;

	/**
	 * The command arguments to be appended.
	 */
	protected String[] cmdArgs = null;

	/**
	 * The command arguments delimiter character. default is the space
	 * character.
	 */
	protected String cmdArgsDelim = " ";

	/**
	 * The standard input details for the command that is to be executed.
	 * Specified either through the TDI GUI or as an entry attributes
	 */
	protected String stdinSrc = null;

	/**
	 * The standard input details for the command that is to be executed.
	 * Specified either through the TDI GUI or as an entry attributes
	 */
	protected String stdinDest = null;

	/** keep track of whether the fc has been initialized */
	protected boolean initialized;

	/**
	 * Stores the connection properties that have been configured
	 */
	protected Properties props;

	/** The way to perform logging. */
	protected LogProxy logproxy = null;

	/**
	 * TDIRXALogAdapter to be used
	 */
	protected TDIRXALogAdapter RXAlogger;

	/**
	 * Class name of the logger
	 */
	static final String loggerName = "com.ibm.tivoli.remoteaccess.rxalogger";

	/**
	 * Remote Command Line FC constructor
	 */
	public RemoteCmdLineFC() {
		super();
		/*
		 * Initialise the required variables.
		 */
		props = new Properties();
		initialized = false;

	}

	/**
	 * <p>
	 * Execute the Command on the specified target. Initialize must be called
	 * prior to calling this method.
	 * </p>
	 *
	 * <p>
	 * The perform() method accepts an Entry object. If anything else is passed
	 * an Exception is thrown.
	 * </p>
	 * <p>
	 * The suppplied Entry object may contain zero or more of the following
	 * attributes: <br>
	 * <table border = "1">
	 * <tr>
	 * <th>Configuration Item</th>
	 * <th>Available Attribute</th>
	 * <tr>
	 * <td>Command to Execute</td>
	 * <td>PARAM_INPUT</td>
	 * </tr>
	 * <tr>
	 * <td>Local Path to Stdin Source File</td>
	 * <td>PARAM_STDIN_SRC</td>
	 * </tr>
	 * <tr>
	 * <td>Remote Path to Stdin Destination File</td>
	 * <td>PARAM_STDIN_DEST</td>
	 * </tr>
	 * </table>
	 * </p>
	 * <p>
	 * NOTE: Values for the command and standard input parameters may also be
	 * configured on the TDI GUI. In the event of one or more of these parameter
	 * values being specified both on the TDI GUI and as attributes in the Entry
	 * object, the value provided within the TDI Entry object will have
	 * precedence over the value supplied on the GUI. For instance, if a value
	 * is provided for the command both on the GUI and as an attribute called
	 * PARAM_INPUT in the Entry object then the value configured on the GUI will
	 * be disregarded.
	 * </p>
	 * <p>
	 * On response the Entry will contain the attributes
	 * <code>command.out</code> and <code>command.error</code> and
	 * <code>command.returnCode<code>. Attributes <code>command.out</code> and
	 *
	 * <code>command.error<code> will have java.lang.String values representing the standard output and
	 * standard error returned from executing the command respectively.  The attribute <code>command.returnCode<code>
	 * will have a java.lang.Integer value containing the return code that resulted from executing the command.
	 * These attributes can be used to determine the success/failure of the operation.
	 * </p>
	 *
	 *
	 * @param arg0
	 *            must be Entry
	 *
	 * @return Entry
	 * @throws GeneralCLFCException
	 *             when errors occur executing the command
	 * @throws ParamException
	 *             when insufficient/incorrect parameters provided
	 */
	public Object perform(Object arg0) throws ParamException,
			GeneralCLFCException {
		if (!initialized) {
			throw ExceptionFactory.createGeneralCLFCException(
					MsgIds.NOT_INITIALIZED, logproxy);
		}

		String cmdGUI = (String) getParam(PARAM_CONFIG_COMMAND);
		if (cmdGUI.length() != 0)
			cmdToExecute = cmdGUI;
		Entry response = new Entry();
		if (arg0 instanceof Entry) {
			Entry request = (Entry) arg0;
			if (request.size() != 0) {
				processInputEntryAttr(request);
			}
			if ((cmdToExecute != null) && (!(cmdToExecute.equals("")))) {
				logmsg(MessageHelper.getMsgResource().getMessage(
						MsgIds.OPTION_VALUE_SET,
						new Object[] { PARAM_CONFIG_COMMAND, cmdToExecute }));
				if (stdinSrc != null) {
					File input = new File(stdinSrc);
					if ((input.exists()) && (!input.isDirectory())) {
						response = processStandardInput();
					} else {
						logmsg(MessageHelper.getMsgResource().getMessage(
								MsgIds.NOSUCHFILE,
								new Object[] { stdinSrc }));
						throw ExceptionFactory.createGeneralCLFCException(
								MsgIds.INVALIDSRCFILE, logproxy);
					}
				} else {
					if ((Boolean.valueOf(props
							.getProperty(PARAM_CONFIG_AS400_RUNPROG))
							.booleanValue())) {
						if (null != cmdArgs && cmdArgs.length > 0) {
							String cmdArgsCharEncoding = (String) getParam(PARAM_CONFIG_AS400CMDARGSENC);
							if ((cmdArgsCharEncoding != null)
									&& (cmdArgsCharEncoding.trim().length() != 0)) {
								executor
										.setExecutorCmdArgsEncoding(cmdArgsCharEncoding);
							}
							response = executor.executeCommand(cmdToExecute,
									cmdArgs, cmdArgsDelim);
						} else {
							response = executor.executeCommand(cmdToExecute,
									new String[] {}, cmdArgsDelim);
						}
					} else {
						if (null != cmdArgs && cmdArgs.length > 0) {
							for (int i = 0; i < cmdArgs.length; i++) {
								if (null != cmdArgs[i]
										&& cmdArgs[i].length() > 0) {
									cmdToExecute = cmdToExecute + cmdArgsDelim
											+ cmdArgs[i];
								}
							}
						}
						response = executor.executeCommand(cmdToExecute);
					}
				}
			} else {
				throw ExceptionFactory.createParamException(
						MsgIds.NO_COMMAND, logproxy);
			}
		} else {
			throw ExceptionFactory.createParamException(
					MsgIds.INVALID_PERFORM_OBJ, logproxy);
		}
		return response;
	}

	/**
	 * Process the attributes contained within the provided Entry object. If
	 * valid attribute(s) have been provided containing data of the expected
	 * type then store them in the appropriate variables to be used when
	 * performing the remote command execution. This will wipe over any values
	 * for cmdToExecute, stdinSrc and stdinDestn that were configured on the
	 * GUI. An appropriate error will be logged if this occurs.
	 *
	 * @param request
	 *            Entry to be processed
	 */
	private void processInputEntryAttr(Entry request) {
		if (request.getAttribute(PARAM_INPUT) != null) {
			Object cmdEntryObj = request.getAttribute(PARAM_INPUT).getValue(0);
			if (cmdEntryObj instanceof String) {
				String cmdEntry = (String) cmdEntryObj;
				if (cmdToExecute != null) {
					logInfo(MessageHelper.getMsgResource().getMessage(
							MsgIds.INPUT_ATTR_EXISTS,
							new Object[] { PARAM_INPUT, PARAM_CONFIG_COMMAND,
									cmdToExecute }));
				}
				cmdToExecute = cmdEntry;
			} else {
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.WRONG_TYPE_FOR_ATTR,
						new Object[] { PARAM_INPUT }));
			}
		}
		if (request.getAttribute(PARAM_ARGS) != null) {
			// multi-valued argument.
			Vector argsAttrVals = request.getAttribute(PARAM_ARGS)
					.getValuesVector();
			if (null != argsAttrVals && argsAttrVals.size() > 0) {
				cmdArgs = new String[argsAttrVals.size()];
				for (int i = 0; i < argsAttrVals.size(); i++) {
					if (argsAttrVals.get(i) != null
							&& (argsAttrVals.get(i) instanceof String)) {
						cmdArgs[i] = (String) argsAttrVals.get(i);
					} else {
						logInfo(MessageHelper.getMsgResource().getMessage(
								MsgIds.WRONG_TYPE_FOR_ATTR,
								new Object[] { PARAM_ARGS }));
					}
				}
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.INPUT_ATTR_EXISTS,
						new Object[] { PARAM_ARGS, PARAM_ARGS,
								Arrays.toString(cmdArgs) }));
			} else {
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.WRONG_TYPE_FOR_ATTR,
						new Object[] { PARAM_ARGS }));
			}
		}
		if (request.getAttribute(PARAM_ARGS_DELIM) != null) {
			Object argsDelimObj = request.getAttribute(PARAM_ARGS_DELIM)
					.getValue(0);
			if (argsDelimObj instanceof String) {
				cmdArgsDelim = (String) argsDelimObj;
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.INPUT_ATTR_EXISTS,
						new Object[] { PARAM_ARGS_DELIM, PARAM_ARGS_DELIM,
								cmdArgsDelim }));
			} else {
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.WRONG_TYPE_FOR_ATTR,
						new Object[] { PARAM_INPUT }));
			}
		}
		if (request.getAttribute(PARAM_STDIN_SRC) != null) {
			Object stdinSrcObj = request.getAttribute(PARAM_STDIN_SRC)
					.getValue(0);
			if (stdinSrcObj instanceof String) {
				String stdinSrcEntry = (String) stdinSrcObj;
				if (stdinSrc != null) {
					logInfo(MessageHelper.getMsgResource().getMessage(
							MsgIds.INPUT_ATTR_EXISTS,
							new Object[] { PARAM_STDIN_SRC,
									PARAM_CONFIG_STDIN_SOURCE, stdinSrc }));
				}
				stdinSrc = stdinSrcEntry;
			} else {
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.WRONG_TYPE_FOR_ATTR,
						new Object[] { PARAM_STDIN_SRC }));
			}
		}
		if (request.getAttribute(PARAM_STDIN_DEST) != null) {
			Object stdinDestObj = request.getAttribute(PARAM_STDIN_DEST)
					.getValue(0);
			if (stdinDestObj instanceof String) {
				String stdinDestEntry = (String) stdinDestObj;
				if (stdinDest != null) {
					logInfo(MessageHelper.getMsgResource().getMessage(
							MsgIds.INPUT_ATTR_EXISTS,
							new Object[] { PARAM_STDIN_DEST,
									PARAM_CONFIG_STDIN_DESTN, stdinDest }));
				}
				stdinDest = stdinDestEntry;
			} else {
				logInfo(MessageHelper.getMsgResource().getMessage(
						MsgIds.WRONG_TYPE_FOR_ATTR,
						new Object[] { PARAM_STDIN_DEST }));
			}
		}
	}

	/**
	 * If stdinSrc has been configured then standard input needs to be used when
	 * executing the command remotely. If a stdinDest directory was specified
	 * then the stdinSrc file will be copied over to a random directory created
	 * under stdinDest. If no destination directory was provided then a random
	 * directory will be created in the target's machine's standard temporary
	 * directory such as C:\temp on a Windows machine. The command to be
	 * executed is appended with the standard input i.e. command < stdin. Once
	 * the command is executed the random directory that was created for the
	 * stdin file on the target is deleted.
	 *
	 * @return Entry
	 *
	 * @throws GeneralCLFCException
	 */
	private Entry processStandardInput() throws GeneralCLFCException {
		Entry resp = null;
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.OPTION_VALUE_SET,
				new Object[] { PARAM_CONFIG_STDIN_SOURCE, stdinSrc }));
		String destinationDir = null;
		if (stdinDest != null) {
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.OPTION_VALUE_SET,
					new Object[] { PARAM_CONFIG_STDIN_DESTN, stdinDest }));
			destinationDir = executor.getRandomDir(stdinDest);
		} else {
			destinationDir = executor.getRandomDir(null);
		}
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.STDIN_PROVIDED));
		logmsg(MessageHelper.getMsgResource()
				.getMessage(MsgIds.DESTN_DIR_SET,
						new Object[] { destinationDir }));
		String filePath = null;
		// transfer the specified localfile (stdinSrc) across to the
		// destinationDir
		String extension = EMPTY_STRING;
		int lastIndex = stdinSrc.lastIndexOf(FULL_STOP);
		if (lastIndex != -1) {
			extension = stdinSrc.substring(lastIndex);
		}
		filePath = destinationDir + executor.getOSSeparator() + STDIN_FILENAME
				+ extension;
		executor.transferFile(stdinSrc, filePath);
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.FILE_TRANSFERRED,
				new Object[] { stdinSrc, filePath }));
		cmdToExecute = cmdToExecute + PIPE_STDIN + "\"" + filePath + "\"";
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.COMPLETE_CMD, new Object[] { cmdToExecute }));
		resp = executor.executeCommand(cmdToExecute);
		executor.removeDir(destinationDir);
		return resp;
	}

	/**
	 * Gets the version of this FC.
	 *
	 * @return String version + build date
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * This function is called when the connector is no longer needed by the
	 * user in the Assembly Line or script. Always calls the superclass
	 * terminate method which will take care of releasing resources, closing
	 * parsers etc.
	 *
	 * @throws Exception
	 * @throws GeneralCLFCException
	 */
	public void terminate() throws GeneralCLFCException, Exception {
		if (!initialized) {
			throw ExceptionFactory.createGeneralCLFCException(
					MsgIds.NOT_INITIALIZED, logproxy);
		}
		executor.close();
		super.terminate();

	}

	/**
	 * This function is called once after the connector configuration file has
	 * been provided by the caller.
	 *
	 * @param o
	 *            The configuration object from TDI.
	 *
	 * @throws Exception
	 *             If super class initialize fails.
	 * @throws GeneralCLFCException
	 *             If the connection cannot be prepared i.e. not all attributes
	 *             have been provided.
	 */
	public void initialize(Object o) throws GeneralCLFCException, Exception {
		try {
			super.initialize(o);
			if (null != o && (o instanceof Log)) {
				logproxy = new LogProxyImpl((Log) o);
			} else {
				logproxy = new LogProxyImpl(this.getLog());
			}
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.START_INIT,
					new Object[] { getClass().getName() }));
			BaseProtocol.setLogger(getRXACompatableLogger());
			RXAlogger.text(Level.INFO, this, "initialize",
					"Starting RXA Internal Logging.");
			BaseProtocol.startLogging();
			initConfigProperties();
			executor = new RemoteCmdLineExecutor(props, logproxy);
			initialized = executor.prepareConnection();
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.COMPLETE_INIT,
					new Object[] { getClass().getName(),
							(Boolean.valueOf(initialized)).toString() }));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Initialize the configuration parameters provided for the FC on the TDI
	 * GUI.
	 */
	private void initConfigProperties() {
		int i;
		// Initialize the command and stdin options provided on the GUI
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.INITIALIZING_PARAMS));
		String cmdGUI = (String) getParam(PARAM_CONFIG_COMMAND);
		if (cmdGUI.length() != 0) {
			cmdToExecute = cmdGUI;
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.INIT_OPTION,
					new Object[] { PARAM_CONFIG_COMMAND, cmdGUI }));
		}
		String stdinSrcGUI = (String) getParam(PARAM_CONFIG_STDIN_SOURCE);
		if (stdinSrcGUI.length() != 0) {
			stdinSrc = stdinSrcGUI;
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.INIT_OPTION,
					new Object[] { PARAM_CONFIG_STDIN_SOURCE, stdinSrcGUI }));
		}
		String stdinDestGUI = (String) getParam(PARAM_CONFIG_STDIN_DESTN);
		if (stdinDestGUI.length() != 0) {
			stdinDest = stdinDestGUI;
			logmsg(MessageHelper.getMsgResource().getMessage(
					MsgIds.INIT_OPTION,
					new Object[] { PARAM_CONFIG_STDIN_DESTN, stdinDestGUI }));
		}
		String connectionType = (String) getParam(PARAM_CONFIG_CONNTYPE);
		if (connectionType.length() == 0) {
			props.setProperty(PARAM_CONFIG_CONNTYPE, "ANY");
		}
		/*
		 * Get all provided remote String parameters.
		 */
		for (i = 0; i < PARAM_STR_CONFIG_OPTIONS.length; i++) {
			String paramVal = (String) getParam(PARAM_STR_CONFIG_OPTIONS[i]);
			if (null != paramVal && paramVal.length() != 0) {
				props.setProperty(PARAM_STR_CONFIG_OPTIONS[i], paramVal);
				// Don't print out password
				if ((PARAM_STR_CONFIG_OPTIONS[i].equals(PARAM_CONFIG_PASSWD))
						|| (PARAM_STR_CONFIG_OPTIONS[i]
								.equals(PARAM_CONFIG_PASSPHRASE))) {
					logmsg(MessageHelper.getMsgResource().getMessage(
							MsgIds.OPTION_VALUE_SET,
							new Object[] { PARAM_STR_CONFIG_OPTIONS[i],
									PASSWD_STRING }));
				} else {
					logmsg(MessageHelper.getMsgResource().getMessage(
							MsgIds.OPTION_VALUE_SET,
							new Object[] { PARAM_STR_CONFIG_OPTIONS[i],
									paramVal }));
				}
			}
		}
		/*
		 * Get all provided remote Integer parameters.
		 */
		for (i = 0; i < PARAM_INT_CONFIG_OPTIONS.length; i++) {
			String paramVal = null;
			String param_name = PARAM_INT_CONFIG_OPTIONS[i];
			paramVal = (String) getParam(param_name);
			if ((paramVal != null) && (paramVal.trim().length() > 0)) {
				props.put(param_name, paramVal);
				logmsg(MessageHelper.getMsgResource().getMessage(
						MsgIds.OPTION_VALUE_SET,
						new Object[] { param_name, paramVal }));
			}
		}
		/*
		 * Get all provided remote boolean parameters.
		 */
		for (i = 0; i < PARAM_BOOL_CONFIG_OPTIONS.length; i++) {
			String paramVal = null;

			paramVal = (String) getParam(PARAM_BOOL_CONFIG_OPTIONS[i]);
			if (paramVal != null) {
				props.put(PARAM_BOOL_CONFIG_OPTIONS[i], paramVal);
				logmsg(MessageHelper.getMsgResource()
						.getMessage(
								MsgIds.OPTION_VALUE_SET,
								new Object[] { PARAM_BOOL_CONFIG_OPTIONS[i],
										paramVal }));
			}

		}
		logmsg(MessageHelper.getMsgResource().getMessage(
				MsgIds.INIT_PARAMS_DONE));
	}

	/**
	 * Gets an RXA compatible logger
	 *
	 * @return the logger
	 */
	protected Logger getRXACompatableLogger() {
		try {
			/*
			 * // Example of Getting the jre native logger
			 * java.util.logging.Logger javaLogger = java.util.logging.Logger
			 * .getLogger(loggerName);
			 * javaLogger.setLevel(java.util.logging.Level.FINEST);
			 * javaLogger.setUseParentHandlers(false); // create logging dir
			 * File logDir = new File("./logs"); if (!logDir.exists()) { boolean
			 * created = logDir.mkdir(); if (!created || !logDir.canWrite()) {
			 * throw new IOException("Can't write to log dir: " +
			 * logDir.getAbsolutePath() + " Logging disabled."); } } // set-up
			 * handlers for the trace and message logs java.util.logging.Handler
			 * msgHndlr = new java.util.logging.FileHandler(
			 * "./logs/rxa_message.log", 1000000, 1, true);
			 * msgHndlr.setLevel(java.util.logging.Level.INFO);
			 * msgHndlr.setFormatter(new SimpleFormatter());
			 * java.util.logging.Handler trcHndlr = new
			 * java.util.logging.FileHandler( "./logs/rxa_trace.log", 10000000,
			 * 1, true); trcHndlr.setLevel(java.util.logging.Level.FINEST);
			 * trcHndlr.setFormatter(new SimpleFormatter());
			 * javaLogger.addHandler(msgHndlr); javaLogger.addHandler(trcHndlr);
			 * // Configure RXA logging using the remoteaccess JreLogAdapter
			 * Logger RXAlogger = JreLogAdapter.getLogger(loggerName);
			 */
			if (null == RXAlogger) {
				RXAlogger = new TDIRXALogAdapter(logproxy.getLog());
				RXAlogger.text(Level.INFO, this, "getRXACompatableLogger",
						"TDIRXALogAdaptor Created.");
			}
			Boolean loggingWidget = Boolean
					.valueOf((String) getParam(PARAM_CONFIG_RXA_LOG));
			if (loggingWidget != null) {
				RXAlogger.setLogEnabled(loggingWidget.booleanValue());
			} else {
				RXAlogger.setLogEnabled(false);
			}
			return RXAlogger;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Log the specified error message.
	 *
	 * @param err
	 *            Error to be logged
	 */
	public void logError(String err) {
		if (logproxy != null) {
			logproxy.error(err);
		}
	}

	/**
	 * Log the specified debug message.
	 *
	 * @param msg
	 *            Message to be logged
	 */
	public void logmsg(String msg) {
		if (logproxy != null) {
			logproxy.debug(msg);
		}
	}

	/**
	 * Log the specified info message
	 *
	 * @param msg
	 *            Message to be logged
	 */
	public void logInfo(String msg) {
		if (logproxy != null) {
			logproxy.info(msg);
		}
	}

	/**
	 * Set the log for the Function Component to the specified Log
	 *
	 * @param lg
	 *            The desired log
	 */
	public void setLog(Log lg) {
		super.setLog(lg);
		logproxy = new LogProxyImpl(lg);
	}

	/**
	 * This function is called to return the CmdLineExecutor that has been
	 * initialized by the FC. In the event that a remote connection has been
	 * established, the RemoteCmdLineExecutor.getRXAProtocol() method can be
	 * used to return a RXA connection protocol object that can be used to
	 * perform operations permitted by the RXA toolkit.
	 *
	 * @return CmdLineExecutor object that has been initialised to perform the
	 *         command.
	 */
	public CmdLineExecutor getExecutor() {
		return executor;
	}
}
