/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;
import com.ibm.di.server.Trace;

/**
 * The command line Connector enables you to read the output from a command line
 * or pipe data to a command line's standard input. Every command argument is
 * separated by a space character, and quotes are ignored. The command is
 * executed on the local machine. The Connector supports Iterator and AddOnly
 * mode, as well as CallReply mode.
 * 
 */
public class CommandLineConnector extends Connector implements
		ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "commandlineconnector";

	/**
	 * {@link Process}
	 */
	private Process proc;

	/**
	 * Name of the component.
	 */
	private static final String myName = "CommandLine Connector";

	/**
	 * The name of the attribute in which the command line has to be put in
	 * CallReply mode
	 */
	public static final String ATTR_COMMAND_LINE = "command.line";

	/**
	 * The name of the attribute in which the result of the executed command in
	 * CallReply mode is stored
	 */
	public static final String ATTR_COMMAND_OUTPUT = "command.output";

	/**
	 * Flag that indicates whether to use sh to parse the command line on Linux
	 * or not.
	 */
	private boolean mShParsing = false;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = ResourceHash.getHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor. Initializes the connector to work in AddOnly, Iterator and
	 * CallReply modes
	 */
	public CommandLineConnector() {
		Trace.entrymid(this, "CommandLineConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE, ConnectorConfig.CALL_REPLY_MODE });
		Trace.exitmid(this, "CommandLineConnector");
	}

	/**
	 * Initialize the connector. The connector may be passed a parameter of any
	 * kind by the user. It is up to the connector to determine whether this
	 * object can be used or not. The parameter is typically provided by a user
	 * script. When an AssemblyLine initializes it's Connectors, they are passed
	 * a ConnectorMode object.
	 * 
	 * @param o
	 *            Expects the ConnectorMode
	 */
	@Override
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);

		mShParsing = Boolean.valueOf(getParam("shParsing")).booleanValue();

		int connectorMode = -1;
		if (o instanceof ConnectorMode)
			connectorMode = ((ConnectorMode) o).getMode();

		if (connectorMode == ServerConstants.TYPE_CALLREPLY)
			return;

		String commandline = getParam("commandLine");
		if (commandline == null || commandline.length() == 0) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.COMMANDLINE.COMMANDLINEPARAM.MISSING.EXCEPTION"));
		}

		if (mShParsing)
			proc = Runtime.getRuntime().exec(
					new String[] { "sh", "-c", commandline });
		else
			proc = Runtime.getRuntime().exec(commandline);

		if (connectorMode == ServerConstants.TYPE_ITERATOR) {
			initParser(proc.getInputStream(), null);
			try {
				proc.getOutputStream().close();
			} catch (IOException ignore) {
				Trace.exception(this, "initialize", ignore, "");
			}
		} else if (connectorMode == ServerConstants.TYPE_ADDONLY) {
			initParser(null, proc.getOutputStream());
		} else {
			initParser(proc.getInputStream(), proc.getOutputStream());
		}

		Trace.exitmin(this, "initialize");
	}

	/**
	 * Terminate the connector. If a parser exists it is being closed.
	 * 
	 */
	@Override
	public void terminate() throws Exception {
		Trace.entrymin(this, "terminate");
		if (getParser() != null) {
			getParser().closeParser();
		}
		Trace.exitmin(this, "terminate");
	}

	/**
	 * Empty.
	 */
	@Override
	public void selectEntries() throws Exception {
	}

	/**
	 * Return the next Entry from the connector.
	 * 
	 * @return - the next Entry, or null if no more data
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		Trace.entrymin(this, "getNextEntry");
		Trace.exitmin(this, "getNextEntry");
		return getParser().readEntry();
	}

	/**
	 * Add a new entry to the data source
	 * 
	 * @param entry
	 *            The entry data to add
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "putEntry", entry);
		getParser().writeEntry(entry);
		Trace.exitmin(this, "putEntry");
	}

	/**
	 * In a CallReply mode executes the command stored in the attribute of the
	 * given Entry with name CommandLineConnector.ATTR_COMMAND_LINE and stores
	 * the result in the returned Entry in an attribute with name
	 * CommandLineConnector.ATTR_COMMAND_OUTPUT.
	 * 
	 * @param aEntry
	 *            The Entry with the command line to be executed.
	 * @return an Entry containing the result from the executed command line.
	 */
	@Override
	public Entry queryReply(Entry aEntry) throws Exception {
		String cmdLine = aEntry.getString(ATTR_COMMAND_LINE);
		if (cmdLine == null || cmdLine.length() == 0) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.COMMANDLINE.COMMANDLINEPARAM.MISSING2.EXCEPTION"));
		}

		Process process = execCommand(cmdLine);
		String outputString = inputStreamToString(process);

		Entry resultEntry;
		if (hasParser()) {
			initParser(outputString, null);
			resultEntry = getParser().readEntry();
			if (resultEntry == null)
				resultEntry = new Entry(); // or maybe return null?
		} else { // no parser
			resultEntry = new Entry();
		}

		resultEntry.setAttribute(ATTR_COMMAND_OUTPUT, outputString);

		return resultEntry;
	}

	/**
	 * Executes a given command. Depending on the type of executing shell, the
	 * command is executed in a proper way for Windows and Unix
	 * 
	 * @param aCommand
	 *            The command to execute.
	 * @return A new Process object for managing the subprocess
	 * @throws Exception
	 */
	private Process execCommand(String aCommand) throws Exception {
		Process process;

		if (mShParsing) {
			process = Runtime.getRuntime().exec(
					new String[] { "sh", "-c", aCommand });
		} else {
			process = Runtime.getRuntime().exec(aCommand);
		}

		return process;
	}

	/**
	 * Reads the data in the input stream connected to the given process and
	 * saves it into a String.
	 * 
	 * @param aProcess
	 *            The process which receives input from an input stream
	 * @return The accumulated String
	 * @throws Exception
	 *             An IO Exception can arise.
	 */
	private String inputStreamToString(Process aProcess) throws Exception {
		if (aProcess == null || aProcess.getInputStream() == null) {
			return null;
		}

		StringBuffer resultString = new StringBuffer();
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(aProcess.getInputStream()));
		String inputLine;
		while ((inputLine = bufferedReader.readLine()) != null) {
			resultString.append(inputLine);
			resultString.append("\n");
		}
		bufferedReader.close();

		return resultString.toString();
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

}
