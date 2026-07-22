/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.util.FileUtils;

/**
 * This is a base class which could be extended in order to provide an utility
 * for migrating configuration files. This class provides the ability to work
 * with Java properties files (descendant classes can change this). It expects
 * the child classes to define the changes, that will be done over the
 * configuration file, using the {@link #defineChanges(Map)} method.
 * 
 * @since TDI 7.1
 */
public abstract class BaseMigrationUtility {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected static final ResourceHash resHash = ResourceHash.getHash("basemig");

	// The common options switches..
	/**
	 * The switch used for providing the source file to be migrated.
	 */
	protected static final String SWITCH_MIG_FILE = "-f";

	/**
	 * The switch used for providing the name of the backup file.
	 */
	protected static final String SWITCH_MIG_FILE_BAKUP = "-b";

	/**
	 * The switch used for providing the name of the file used for output of the
	 * migration.
	 */
	protected static final String SWITCH_MIG_FILE_NEW = "-n";

	/**
	 * The switch used for enabling verbose output.
	 */
	protected static final String SWITCH_VERBOSE = "-v";

	/**
	 * The switch used for requesting help information.
	 */
	protected static final String SWITCH_HELP = "-?";

	private static final char CONTROL_COMMENT_CHAR1 = '#';
	private static final char CONTROL_COMMENT_CHAR2 = '!';

	private static final char CONTROL_ASSIGN_CHAR1 = '=';
	private static final char CONTROL_ASSIGN_CHAR2 = ':';

	private static final String CONTROL_VALUE_SEPARATOR = "\\";

	/**
	 * Map between command line switches (the keys) and arguments to the
	 * switches (the values). Command line switches start with a dash and each
	 * switch could have up to one value. If a space in the value is required
	 * you will need to use quotes. If a standalone switch is provided, e.g. -h,
	 * the corresponding value will be an empty string. If a standalone value is
	 * provided (one that does not have a switch in front of it) it will be put
	 * in the commandValuesList.
	 */
	Map<String, String> commandLineOptions;

	/**
	 * Contains the standalone command line arguments.
	 */
	List<String> commandValuesList;

	/**
	 * The log4j logger for the utility.
	 */
	private Logger log;

	/**
	 * Field specifying if the program is going to run in verbose mode.
	 */
	boolean verboseMode;

	/**
	 * Specifies the sequence of characters denoting the end of each line.
	 */
	String eol;

	/**
	 * Specifies whether a help information has been requested.
	 */
	boolean printHelp;

	/**
	 * This is the {@link String} representing the path to the file to be
	 * migrated.
	 */
	String migFileSrcPath;

	/**
	 * This is the {@link String} representing the path to the file containing
	 * the migration output.
	 */
	String migFileDestPath;

	/**
	 * This is the {@link String} representing the path to the file to be used
	 * as backup.
	 */
	String migFileBakPath;

	/**
	 * This is the {@link File} to be migrated.
	 */
	private File migFileSrc;

	/**
	 * This is the {@link File} containing the migration output.
	 */
	private File migFileDest;

	/**
	 * This is the {@link File} to be used as backup.
	 */
	private File migFileBak;

	/**
	 * This is the temporary buffer containing the file which is being migrated.
	 */
	private StringBuilder stringBuffer;

	/**
	 * Holds a list of the properties read from the source file.
	 */
	private Map<String, String> properties;

	/**
	 * This is the {@link Map} which the descendant classes fill in to define
	 * the changes which are going to be performed over the read properties.
	 */
	private List<ChangeDescription> changes;

	/**
	 * This is a temporary {@link StringBuilder} for helping with string
	 * manipulations.
	 */
	private StringBuilder temp = new StringBuilder();

	/**
	 * Default utility for UnitTesting purposes only!
	 */
	BaseMigrationUtility() {
	}

	/**
	 * Create an instance by passing in the command line arguments.
	 * 
	 * @param args
	 *            the command line arguments passed to the main method of the
	 *            implementing class.
	 * @param log
	 *            the destination to log into. If this is null a new log will be
	 *            created.
	 */
	public BaseMigrationUtility(String[] args, Logger log) {
		parseArgs(args);
		setLog(log);
	}

	/**
	 * Parses the command line arguments by filling {@link #commandLineOptions}
	 * and {@link #commandValuesList} structures.
	 * 
	 * @param args
	 *            the array of all the command line arguments.
	 */
	protected void parseArgs(String[] args) {
		commandLineOptions = new HashMap<String, String>(args != null ? args.length : 10);

		boolean prevIsSwitch = false;
		String switchStr = null;

		if (args != null) {
			for (String arg : args) {
				arg = arg.trim();
				if (isSwitch(arg)) {
					if (prevIsSwitch) {
						// the previous is a stand alone switch...
						commandLineOptions.put(switchStr, "");
					}

					// we have a switch
					prevIsSwitch = true;
					switchStr = arg;
				} else if (prevIsSwitch) {
					// we have a switch with a value...
					commandLineOptions.put(switchStr, arg);

					prevIsSwitch = false;
					switchStr = null;
				} else {
					// standalone value...
					getCommandStandaloneValuesList().add(arg);
				}
			}
		}

		if (prevIsSwitch) {
			// one standalone switch at the end...
			commandLineOptions.put(switchStr, "");
			prevIsSwitch = false;
			switchStr = null;
		}
	}

	/**
	 * Checks whether the specified string complies with the syntax of a command
	 * line switch.
	 * 
	 * @param arg
	 *            the string to check.
	 * @return <code>true</code> if the passed argument is a switch,
	 *         <code>false</code> otherwise.
	 */
	protected boolean isSwitch(String arg) {
		return arg != null && arg.length() > 1 && arg.charAt(0) == '-';
	}

	/**
	 * @return the list of all the command line arguments which have not been
	 *         prepended with a switch. This method returns the actual reference
	 *         to the internal list.
	 */
	protected List<String> getCommandStandaloneValuesList() {
		if (commandValuesList == null) {
			commandValuesList = new ArrayList<String>();
		}
		return commandValuesList;
	}

	/**
	 * Requests the value of the switch which have been passed to the command
	 * line.
	 * 
	 * @param switchStr
	 *            the switch which value to look for.
	 * @return the value as String, empty string (meaning that the switch is
	 *         stand alone) or <code>null</code> (meaning no such switch has been
	 *         provided).
	 */
	protected String getCommandValueBySwitch(String switchStr) {
		return commandLineOptions != null ? commandLineOptions.get(switchStr) : null;
	};

	/**
	 * Sets the provided log for this utility. If null a default log is
	 * initialized.
	 * 
	 * @param log
	 *            the log for this utility to use. Could be <code>null</code>
	 *            which will create a new log automatically.
	 */
	protected void setLog(Logger log) {
		if (log != null) {
			this.log = log;
		} else {
			this.log = Logger.getLogger(this.getClass());
		}
	}

	/**
	 * @return the reference to the log object.
	 */
	public Logger getLog() {
		return this.log;
	}

	/**
	 * @return <code>true</code> if the user has requested more verbose logging.
	 */
	public boolean isVerboseMode() {
		return verboseMode;
	}

	/**
	 * This is the entry point which drives the common flow of a standard
	 * migration utility. If the user has requested help information using the
	 * {@link #SWITCH_HELP} switch this method will return ignoring all the
	 * other switches that might have been provided.<br>
	 * <br>
	 * The standard flow is as follows:
	 * <ul>
	 * <li> {@link #interpretCommandLineOptions()}</li>
	 * </ul>
	 * <ul>
	 * -- Help is requested --
	 * <li>{@link #printHelpInformation()}</li>
	 * </ul>
	 * <ul>
	 * -- Help is not requested --
	 * <li>{@link #validateCommandLineOptions()}</li>
	 * <li>{@link #backupFile()}</li>
	 * <li> {@link #parseFile(File)}</li>
	 * <li>{@link #defineChanges(Map)}</li>
	 * <li> {@link #readFile(File)}</li>
	 * <li>
	 * {@link #findEndOfLineCharacterSequence(StringBuilder)}</li>
	 * <li>
	 * {@link #applyChanges(StringBuilder, Map, Map)}</li>
	 * <li>
	 * {@link #writeFile(StringBuilder)}</li>
	 * </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if an argument provided to the command line is invalid.
	 * 
	 * @throws RuntimeException
	 *             if an error occurs while manipulating files.
	 */
	public void migrateFile() {
		interpretCommandLineOptions();

		if (!printHelp) {
			validateCommandLineOptions();
			backupFile();

			properties = parseFile(migFileSrc);

			// the parsed properties are only for looking, not for touching.
			Map<String, String> immutableView = Collections.unmodifiableMap(properties);

			changes = defineChanges(immutableView);

			stringBuffer = readFile(migFileSrc);
			eol = findEndOfLineCharacterSequence(stringBuffer);

			applyChanges(stringBuffer, immutableView, changes);

			writeFile(stringBuffer);
		} else {
			printHelpInformation();
		}
	}

	/**
	 * Called to apply the defined changes to the in-memory buffer. This could
	 * be overrided by the child class to get access to the updated in-memory
	 * buffer if there is the need to make some changes unsupported by the
	 * current design.<br>
	 * <br>
	 * The default implementation applies the changes in the following way:
	 * <ol>
	 * <li><b>Comment</b> - Searches for an existing (uncommented) property and
	 * comments it (if not found this operation is ignored). If the value is on
	 * multiple lines this operation will comment each new line if the previous
	 * ends with "\"</li>
	 * <li><b>Uncomment</b> - Searches for an existing (commented) property and
	 * uncomments it (if not found this operation is ignored). If the value is
	 * on multiple lines this operation will comment each new line if the
	 * previous ends with "\"</li>
	 * <li><b>Add</b> -</li>
	 * <li><b>Modify</b> -</li>
	 * <li><b>Delete</b> -</li>
	 * </ol>
	 * 
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param props
	 *            the {@link Map} of properties read from the source file.
	 *            <b>Note</b> this is an immutable representation of the map.
	 * @param chngs
	 *            the changes defined by the descendant class using the method
	 *            {@link #defineChanges(Map)};
	 */
	protected void applyChanges(StringBuilder sb, Map<String, String> props, List<ChangeDescription> chngs) {
		// copy the props map so we can track the changes
		props = new HashMap<String, String>(props);

		for (ChangeDescription change : chngs) {
			if (change.isCommented() && props.containsKey(change.getPropertyKey())) {
				performPropertyCommenting(sb, props, change);
			}

			if (change.isUncommented() && !props.containsKey(change.getPropertyKey())) {
				performPropertyUncommenting(sb, props, change);
			}

			if (change.isAdded() && !props.containsKey(change.getPropertyKey())) {
				performPropertyAddition(sb, props, change);
			}

			if (change.isModifyed()) {
				performPropertyModification(sb, props, change);
			}

			if (change.isDeleted()) {
				performPropertyDeletion(sb, props, change);
			}
		}
	}

	/**
	 * Perform actual uncommenting of the commented property. Unable to
	 * uncomment properties with multi-line values.
	 * 
	 * @param sb
	 *            the {@link StringBuffer} to perform the change on.
	 * @param props
	 *            the state of the {@link StringBuffer} as a {@link Properties}
	 *            structure.
	 * @param changeKey
	 *            the key of the property to uncomment
	 */
	protected void performPropertyUncommenting(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
		int lineStart = firstIndexOfPropertyKey(sb, change.getPropertyKey(), true);

		if (lineStart != -1) {
			int propStart = sb.indexOf(change.getPropertyKey(), lineStart);
			int valEnd = lastIndexOfPropertyValue(sb, propStart, change.getPropertyKey().length());

			if (propStart != -1 && valEnd != -1) {
				// make the end index exclusive - most probably will point to
				// the beginning of the "eol" or to sb.length()
				valEnd++;

				// uncomment the property key
				sb.delete(lineStart, propStart);
				valEnd -= propStart - lineStart;
				propStart = lineStart;

				boolean hasSeparator = false;
				int i = -1;
				int ls = propStart;
				// uncomment any other lines that comprise the property value.
				for (int le = getEndIndex(sb.indexOf(eol, propStart), valEnd); ls < valEnd
						&& sb.substring(ls, le).trim().endsWith(CONTROL_VALUE_SEPARATOR); ls = le, le = getEndIndex(sb.indexOf(eol,
						le + 1), valEnd)) {
					hasSeparator = true;
					while ((i = firstIndexOfCommentChar(sb, ls, le)) != -1) {
						sb.deleteCharAt(i);
						valEnd--;
						le--;
					}
				}

				if (hasSeparator) {
					// the last value's comment char has not been removed...
					while ((i = firstIndexOfCommentChar(sb, ls, valEnd)) != -1) {
						sb.deleteCharAt(i);
						valEnd--;
					}
				}

				// add it in case later we try to to modify or delete it.
				// 
				// Note: we are putting dummy value here, we need to
				// actually look through the stringBuilder to tell where
				// the value resides.
				props.put(change.getPropertyKey(), "");
			}
		}
	}

	/**
	 * Perform actual commenting of an existing property.
	 * 
	 * @param sb
	 *            the {@link StringBuffer} to perform the change on.
	 * @param props
	 *            the state of the {@link StringBuffer} as a {@link Properties}
	 *            structure.
	 * @param change
	 *            .getPropertyKey() the key of the property to comment
	 */
	protected void performPropertyCommenting(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
		int propStart = firstIndexOfPropertyKey(sb, change.getPropertyKey(), false);
		int valEnd = lastIndexOfPropertyValue(sb, propStart, change.getPropertyKey().length());

		if (propStart > -1) {
			// comment the property key
			sb.insert(propStart, CONTROL_COMMENT_CHAR1);

			if (valEnd > -1) {
				// if there is no vale... what end are we searching for...

				// make the end index exclusive:
				// +1 to move with the inserted comment char and
				// another +1 to start pointing to the eol
				// char
				valEnd += 2;

				// comment any other lines that comprise the property value.
				for (int i = sb.indexOf(eol, propStart); i < valEnd - 1 && i != -1; i = sb.indexOf(eol, i + 1)) {
					sb.insert(i + eol.length(), CONTROL_COMMENT_CHAR1);
					valEnd++;
				}
			}

			// remove this in case some of the bellow methods look for
			// it.
			props.remove(change.getPropertyKey());
		}
	}

	/**
	 * Perform actual addition of the new property.
	 * 
	 * @param sb
	 *            the {@link StringBuffer} to perform the change on.
	 * @param props
	 *            the state of the {@link StringBuffer} as a {@link Properties}
	 *            structure.
	 * @param changeKey
	 *            the key of the property to add
	 * @param changeValue
	 *            specifies how the property should be added as well as its
	 *            value.
	 */
	protected void performPropertyAddition(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
		// calculate the EOLs
		temp.delete(0, temp.length());
		for (int i = 0; i < change.getAddNewLinesBefore(); i++, temp.append(eol))
			;
		String newLinesBefore = temp.toString();

		temp.delete(0, temp.length());
		for (int i = 0; i < change.getAddNewLinesAfter(); i++, temp.append(eol))
			;
		String newLinesAfter = temp.toString();

		temp.delete(0, temp.length());
		if (change.getAddComment() != null) {
			for (String commentLine : change.getAddComment()) {
				temp.append(CONTROL_COMMENT_CHAR1);
				temp.append(commentLine);
				temp.append(eol);
			}
		}
		String commentLines = temp.toString();

		// gather the property string.
		String stringToAdd = newLinesBefore + eol + commentLines + change.getPropertyKey() + CONTROL_ASSIGN_CHAR1
				+ change.getValue() + newLinesAfter;

		String addAfterKey = change.getAddAfterKey();
		int startIndex = -1;
		if (addAfterKey != null) {
			startIndex = sb.indexOf(addAfterKey);
		}

		// update the stringBuilder
		if (startIndex == -1) {
			sb.append(stringToAdd);
		} else {
			startIndex = lastIndexOfPropertyValue(sb, startIndex, addAfterKey.length());
			
			// -1 means no value is specified
			if(startIndex == -1) {
				int afterKeyEndIndex = sb.indexOf(addAfterKey) + addAfterKey.length();
				if ((sb.length() - afterKeyEndIndex) == 1) {
					startIndex = afterKeyEndIndex;
				} else if ((sb.length() - afterKeyEndIndex) == 0) {
					startIndex = afterKeyEndIndex - 1;
				} else {
					startIndex = sb.indexOf(eol, afterKeyEndIndex)-1;
				}
			}

			// move the start right after the last character
			startIndex++;
			sb.insert(startIndex, stringToAdd);
		}

		props.put(change.getPropertyKey(), change.getValue());

	}

	/**
	 * Perform actual modification of the existing property.
	 * 
	 * @param sb
	 *            the {@link StringBuffer} to perform the change on.
	 * @param props
	 *            the state of the {@link StringBuffer} as a {@link Properties}
	 *            structure.
	 * @param changeKey
	 *            the key of the property to modify
	 * @param newValue
	 *            the value to set on the existing property.
	 */
	protected void performPropertyModification(StringBuilder sb, Map<String, String> props, ChangeDescription change) {
		int valStart = firstIndexOfPropertyValue(sb, change.getPropertyKey(), !props.containsKey(change.getPropertyKey()));
		int valEnd = lastIndexOfPropertyValue(sb, valStart);
		String newValue = change.getValue();

		if (valStart > -1 && valEnd > -1) {
			sb.replace(valStart, ++valEnd, newValue);
			props.put(change.getPropertyKey(), newValue);
		} else if (valStart == -1 && valEnd == -1) {
			// we have reached to a property without a value, e.g. "syncBase="
			// or "syncBase"
			valStart = firstIndexOfPropertyKey(sb, change.getPropertyKey(), !props.containsKey(change.getPropertyKey()));
			if (valStart > -1) {
				valEnd = getEndIndex(sb.indexOf(eol, valStart), sb.length());
				valStart = indexOfAssignmentCharacter(sb, valStart, valEnd);

				if (valStart == -1) {
					newValue = CONTROL_ASSIGN_CHAR1 + newValue;
					valStart = valEnd;
				} else {
					valStart++;
				}

				if (valEnd > valStart) {
					sb.replace(valStart, valEnd, newValue);
				} else {
					sb.insert(valStart, newValue);
				}
				props.put(change.getPropertyKey(), newValue);
			}
		}
	}

	/**
	 * Perform actual deletion of the existing property.
	 * 
	 * @param sb
	 *            the {@link StringBuffer} to perform the change on.
	 * @param props
	 *            the state of the {@link StringBuffer} as a {@link Properties}
	 *            structure.
	 * @param changeKey
	 *            the key of the property to modify
	 * @param deletingComments
	 */
	protected void performPropertyDeletion(StringBuilder sb, Map<String, String> propsAvailable, ChangeDescription change) {
		int propStart = firstIndexOfPropertyKey(sb, change.getPropertyKey(), !propsAvailable.containsKey(change.getPropertyKey()));
		int valEnd = lastIndexOfPropertyValue(sb, propStart, change.getPropertyKey().length());

		if (valEnd > -1) {
			valEnd = getEndIndex(valEnd
			// move the index after the last character of the value
					+ 1
					// include the last eol as well.
					+ eol.length(), sb.length());

		} else if (propStart > -1) {
			// we have a beginning but don't have an end...
			valEnd = getEndIndex(sb.indexOf(eol, propStart), sb.length());
			int asgnPos = indexOfAssignmentCharacter(sb, propStart, valEnd);
			valEnd = asgnPos == -1 ? propStart + change.getPropertyKey().length() : asgnPos + 1;
		}

		if (propStart > -1) {
			sb.delete(propStart, valEnd);

			if (change.isDeletingComments()) {
				// look for the first white-spaced line
				int comStart = firstIndexOfCommentBlock(sb, propStart - 1
				/*
				 * move one char left for the cases when the propStart is
				 * pointing to the beginning of a eol char
				 */);

				if (comStart != -1 && propStart > comStart) {
					sb.delete(comStart, propStart);
				}
			}

			propsAvailable.remove(change.getPropertyKey());
		}
	}

	/**
	 * Parses the source file. <br>
	 * <br>
	 * The default implementation uses the {@link Properties} class to parse the
	 * source file.
	 * 
	 * @param srcFile
	 *            the file to parse.
	 * @return a {@link Map} of all the properties from the source file.
	 */
	protected Map<String, String> parseFile(File srcFile) {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(srcFile);
			return parseFile(fis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Parses the source stream. <br>
	 * <br>
	 * The default implementation uses the {@link Properties} class to parse the
	 * source stream.
	 * 
	 * @param source
	 *            the input stream to read from
	 * @return a {@link Map} of all the properties from the source stream.
	 */
	protected Map<String, String> parseFile(InputStream source) throws IOException {
		HashMap<String, String> result = null;

		Properties props = new Properties();
		props.load(source);

		result = new HashMap<String, String>(props.size());

		for (Entry<Object, Object> e : props.entrySet()) {
			result.put(e.getKey().toString(), e.getValue() != null ? e.getValue().toString() : null);
		}

		return result;
	}

	/**
	 * Searches the in-memory buffer for the character denoting the end of a
	 * line.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @return a {@link String} representing the EndOfLine sequence.
	 */
	String findEndOfLineCharacterSequence(StringBuilder sb) {
		/* Determine what the end of line character is */
		if (sb.indexOf("\r\n") != -1) {
			return "\r\n";
		} else if (sb.indexOf("\r") != -1) {
			return "\r";
		} else {
			return "\n";
		}
	}

	/**
	 * Called to parse the passed command line arguments. <br>
	 * <br>
	 * The default implementation is to get the values of the common switches:
	 * {@link #SWITCH_HELP}, {@link #SWITCH_MIG_FILE_BAKUP},
	 * {@link #SWITCH_MIG_FILE_NEW}, {@link #SWITCH_MIG_FILE} and
	 * {@link #SWITCH_VERBOSE}
	 * 
	 * <br>
	 * The {@link #SWITCH_HELP} is checked first, if it is specified then the
	 * rest of the common switches will not be considered.
	 * 
	 */
	protected void interpretCommandLineOptions() {
		verboseMode = "".equals(commandLineOptions.get(SWITCH_VERBOSE));
		printHelp = "".equals(commandLineOptions.get(SWITCH_HELP)) || commandLineOptions.size() == 0
				|| (commandLineOptions.size() == 1 && verboseMode);

		if (!printHelp) {
			migFileSrcPath = commandLineOptions.get(SWITCH_MIG_FILE);
			migFileBakPath = commandLineOptions.get(SWITCH_MIG_FILE_BAKUP);
			migFileDestPath = commandLineOptions.get(SWITCH_MIG_FILE_NEW);
		}
	}

	/**
	 * Called to check whether the parsed arguments are valid enough for the
	 * migration utility to continue. If an invalid argument is found an
	 * {@link IllegalArgumentException} should be thrown.
	 * 
	 * <br>
	 * <br>
	 * The default implementation checks the validity of the common command line
	 * parameters - file names, existence, permissions, etc.
	 */
	protected void validateCommandLineOptions() {
		if (migFileSrcPath == null || migFileSrcPath.trim().length() == 0) {
			throw new IllegalArgumentException(resHash.getString("MIGBASE.MISSING.SOURCE.FILE", SWITCH_MIG_FILE));
		}

		migFileSrc = new File(migFileSrcPath);

		if (!migFileSrc.canRead()) {
			throw new IllegalArgumentException(resHash.getString("MIGBASE.READING.FAILED", migFileSrc.getAbsolutePath()));
		}

		if (migFileBakPath == null) {
			migFileBakPath = migFileSrcPath + ".backup";
		}

		migFileBak = new File(migFileBakPath);
		checkFileWritable(migFileBak);

		if (migFileDestPath == null) {
			migFileDest = migFileSrc;
		} else {
			migFileDest = new File(migFileDestPath);
		}
		checkFileWritable(migFileSrc);

		if (migFileSrc.equals(migFileBak) || migFileDest.equals(migFileBak)) {
			throw new IllegalArgumentException(resHash.getString("MIGBASE.DUPLICATE.FILES"));
		}
	}

	/**
	 * Checks whether the specified file exists and is writable by this
	 * application.
	 * 
	 * @param fileToWrite
	 *            the file to check.
	 * @throws {@link IllegalArgumentException} if the file could not be
	 *         written.
	 */
	private void checkFileWritable(File fileToWrite) {
		if (fileToWrite.exists() && !fileToWrite.canWrite()) {
			throw new IllegalArgumentException(resHash.getString("MIGBASE.WRITING.FAILED", migFileBak.getAbsolutePath()));
		}
	}

	/**
	 * Called to create a copy of the source file. <br>
	 * <br>
	 * The default implementation checks if the source and the destination files
	 * match. If they do the source file is only renamed to the backup file. If
	 * they don't the source file is copied as the backup file.
	 * 
	 * @throws IllegalArgumentException
	 *             if an error occurs manipulating the files provided by the
	 *             user.
	 * @throws RuntimeException
	 *             if an error occurs while reading/writing the backup file.
	 */
	protected void backupFile() {
		if (migFileSrc.equals(migFileDest)) {
			if (migFileBak.exists() && !migFileBak.delete()) {
				throw new IllegalArgumentException(resHash.getString("MIGBASE.DELETING.FAILED", migFileBak.getAbsolutePath()));
			}
			if (!migFileSrc.renameTo(migFileBak)) {
				throw new IllegalArgumentException(resHash.getString("MIGBASE.RENAME.FAILED", new Object[] {
						migFileSrc.getAbsolutePath(), migFileBak.getAbsolutePath() }));
			}
			migFileSrc = migFileBak;
		} else {
			try {
				FileUtils.copyFile(migFileSrcPath, migFileBakPath, true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Called to read the configuration file and put its content into the
	 * returned {@link StringBuilder}.
	 * 
	 * @param srcFile
	 *            the file to read.
	 * @return a {@link StringBuilder} holding the file content.
	 */
	protected StringBuilder readFile(File srcFile) {
		StringBuilder sb = new StringBuilder((int) srcFile.length() + ((int) srcFile.length() / 4));

		InputStreamReader isr = null;

		try {
			isr = new InputStreamReader(new FileInputStream(srcFile));

			char[] buf = new char[1024];
			int n;

			while ((n = isr.read(buf)) != -1) {
				sb.append(buf, 0, n);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return sb;
	}

	/**
	 * Called to write the in-memory buffer to the destination file.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 */
	protected void writeFile(StringBuilder sb) {
		OutputStreamWriter osw = null;

		try {
			osw = new OutputStreamWriter(new FileOutputStream(migFileDest));
			osw.write(sb.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * 
	 * Finds the first property in the in-memory buffer. If commented is true
	 * then the first commented property is looked up, otherwise the first
	 * uncommented property is looked up.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param property
	 *            the property key name.
	 * @param commented
	 *            specifies whether the property is commented or not.
	 * @return the index of the first character of the line on which the
	 *         property is defined. If the property is commented the index will
	 *         point to the comment character, otherwise to the first character
	 *         of the property key name.
	 */
	int firstIndexOfPropertyKey(StringBuilder sb, String property, boolean commented) {

		int prevEolIndex = -1;
		int nextEolIndex = -1;
		int assignmetIndex = -1;
		int startIndex = sb.indexOf(property);

		boolean startWithComment = false;

		while (startIndex != -1) {
			// check if this is the property we are looking for
			nextEolIndex = getEndIndex(sb.indexOf(eol, startIndex), sb.length());

			assignmetIndex = getEndIndex(indexOfAssignmentCharacter(sb, startIndex, nextEolIndex), nextEolIndex);

			if (!property.equals(sb.substring(startIndex, assignmetIndex).trim())) {
				startIndex = sb.indexOf(property, nextEolIndex);
			} else {
				// previous eol index
				prevEolIndex = sb.lastIndexOf(eol, startIndex);
				if (prevEolIndex == -1) {
					prevEolIndex = 0;
				} else {
					prevEolIndex += eol.length();
				}

				startWithComment = containsChar(sb, prevEolIndex, startIndex, CONTROL_COMMENT_CHAR1)
						|| containsChar(sb, prevEolIndex, startIndex, CONTROL_COMMENT_CHAR2);

				if (startWithComment == commented) {
					if (startWithComment) {
						// we want the beginning of the comment characters
						startIndex = prevEolIndex;
					}

					return startIndex;
				} else {
					startIndex = sb.indexOf(property, nextEolIndex);
				}
			}
		}

		return startIndex;
	}

	/**
	 * Checks whether the in-memory buffer region contains the specified
	 * character.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param start
	 *            the start of the character region (inclusive)
	 * @param end
	 *            the end of the character region (exclusive)
	 * @param chr
	 *            the character to search for
	 * @return <code>true</code> if the character is found in the specified
	 *         region, <code>false</code> otherwise.
	 */
	boolean containsChar(StringBuilder sb, int start, int end, char chr) {
		return indexOf(sb, start, end, chr) != -1;
	}

	/**
	 * Finds the index of the first occurrence of the character specified by
	 * <code>chr</code>, in the in-memory buffer region.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param start
	 *            the start of the character region (inclusive)
	 * @param end
	 *            the end of the character region (exclusive)
	 * @param chr
	 *            the character to search for
	 * @return the index of the character if found in the specified region or -1
	 *         otherwise
	 */
	int indexOf(StringBuilder sb, int start, int end, char chr) {
		end = getEndIndex(end, sb.length());
		for (int i = start; i < end; i++) {
			if (sb.charAt(i) == chr) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds the first character of the value of the property. The resultant
	 * index will point to the first character after the assignment character.
	 * Unlike the {@link Properties} class this code does not ignore the white
	 * spaces so the first index after the assignment character is considered as
	 * the beginning of the value and that one will be returned.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param propStart
	 *            points to the index of the first character of the property key
	 *            name.
	 * @param propLen
	 *            specifies the length of the property key name.
	 * @return the index of the first character right after the assignment
	 *         character separating the property key and the property value.
	 */
	int firstIndexOfPropertyValue(StringBuilder sb, int propStart, int propLen) {
		int startIndex = propStart;

		if (startIndex > -1) {
			int eolIndex = -1;
			startIndex = startIndex + propLen;

			eolIndex = getEndIndex(sb.indexOf(eol, startIndex), sb.length());
			startIndex = indexOfAssignmentCharacter(sb, startIndex, eolIndex);

			if (startIndex != -1) {
				if (startIndex + 1 == eolIndex) {
					startIndex = -1;
				} else {
					startIndex++;
				}
			}
		}

		return startIndex;
	}

	/**
	 * Finds the first character of the value of the property. The resultant
	 * index will point to the first character after the assignment character.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param prop
	 *            the property key name which value's first character to index
	 * @param commented
	 *            specifies whether the property is commented out or not.
	 * @return the index of the first character right after the assignment
	 *         character separating the property key and the property value.
	 */
	private int firstIndexOfPropertyValue(StringBuilder sb, String prop, boolean commented) {

		int lineStart = firstIndexOfPropertyKey(sb, prop, commented);
		int propLen = prop.length();

		if (commented && lineStart > -1) {
			// if the property is commented out then
			// lineStart < sb.indexOf(prop, lineStart)
			lineStart = sb.indexOf(prop, lineStart);
		}

		return firstIndexOfPropertyValue(sb, lineStart, propLen);
	}

	/**
	 * Returns the index of the last character of the string comprising the
	 * property value.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param propStart
	 *            the index of the first character of the property key name
	 * @param propLen
	 *            the length of the property's key name
	 * @return the index of the last character of the string comprising the
	 *         property value.
	 */
	private int lastIndexOfPropertyValue(StringBuilder sb, int propStart, int propLen) {
		return lastIndexOfPropertyValue(sb, firstIndexOfPropertyValue(sb, propStart, propLen));
	}

	/**
	 * Returns the index of the last character of the string comprising the
	 * property value.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param valStart
	 *            the index of the first character of the property value
	 * @return the index of the last character of the string comprising the
	 *         property value.
	 */
	int lastIndexOfPropertyValue(StringBuilder sb, int valStart) {

		String propertyValue = null;
		int eolIndex = -1;

		if (valStart > -1) {
			eolIndex = getEndIndex(sb.indexOf(eol, valStart), sb.length());
			propertyValue = sb.substring(++valStart, eolIndex).trim();

			while (propertyValue.endsWith(CONTROL_VALUE_SEPARATOR) && eolIndex > -1
					&& (valStart = eolIndex + eol.length()) < sb.length()) {
				eolIndex = getEndIndex(sb.indexOf(eol, valStart), sb.length());

				propertyValue = sb.substring(valStart, eolIndex).trim();
			}
		}

		return eolIndex > -1 ? /* we want the index of the last character */--eolIndex : -1;
	}

	/**
	 * Finds the index of the assignment character (i.e.
	 * {@link #CONTROL_ASSIGN_CHAR1} and {@link #CONTROL_ASSIGN_CHAR2} )
	 * separating the property key name and the its value. The search is limited
	 * to the specified characters region.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param start
	 *            the start of the character region (inclusive)
	 * @param end
	 *            the end of the character region (exclusive)
	 * @return the index of an assignment character or -1 if not found.
	 */
	int indexOfAssignmentCharacter(StringBuilder sb, int start, int end) {
		int assignChar = indexOf(sb, start, end, CONTROL_ASSIGN_CHAR1);
		assignChar = assignChar == -1 ? indexOf(sb, start, end, CONTROL_ASSIGN_CHAR2) : assignChar;

		return assignChar;
	}

	/**
	 * Finds the index of the comment character (i.e.
	 * {@link #CONTROL_COMMENT_CHAR1} and {@link #CONTROL_COMMENT_CHAR2} )
	 * specified as the first non-white character. The search is limited to the
	 * specified characters region.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param start
	 *            the start of the character region (inclusive)
	 * @param end
	 *            the end of the character region (exclusive)
	 * @return the index of a comment character or -1 if not found.
	 */
	int firstIndexOfCommentChar(StringBuilder sb, int start, int end) {
		end = getEndIndex(end, sb.length());

		int index = -1;
		char chr = 0;
		for (int i = start; i < end; i++) {
			chr = sb.charAt(i);
			if (chr <= ' ') {
				continue;
			} else if (chr == CONTROL_COMMENT_CHAR1 || chr == CONTROL_COMMENT_CHAR2) {
				index = i;
			}
			break;
		}

		return index;
	}

	/**
	 * Finds the index of the first character in the block of comments which end
	 * is denoted by the <code>propertyKeyFirstIndex</code> parameter. The
	 * algorithm searches until an uncommented line is found. If there is no
	 * comment right next to that property (no empty lines between the comments
	 * block and the property) then -1 will be returned, meaning that could not
	 * find the beginning of the comments block for the specified property.
	 * 
	 * @param sb
	 *            the in-memory representation of the source file
	 * @param end
	 *            denotes where to start the lookup form. This should be the
	 *            first index of the property which comment block to search for.
	 * @return the index of the first character of the block of comments, i.e.
	 *         the first comment char.
	 */
	int firstIndexOfCommentBlock(StringBuilder sb, int propertyKeyFirstIndex) {
		int endIndex = -1;

		if (propertyKeyFirstIndex > -1) {

			endIndex = sb.lastIndexOf(eol, propertyKeyFirstIndex);

			if (endIndex >= eol.length()) {
				int result = -1;
				int startIndex = getStartIndex(sb.lastIndexOf(eol, endIndex - eol.length()));

				for (int i = -1; endIndex > 0 && (i = firstIndexOfCommentChar(sb, startIndex, endIndex)) != -1; endIndex = startIndex, startIndex = getStartIndex(sb
						.lastIndexOf(eol, endIndex - 1))) {
					result = i;
				}

				endIndex = result;
			} else {
				endIndex = -1;
			}
		}

		return endIndex;
	}

	/**
	 * Called to print the help information to the stdOut.
	 */
	protected void printHelpInformation() {
		System.out.println(resHash.getString("MIGBASE.HELP"));
	}

	public boolean isHelpRequested() {
		return printHelp;
	}

	/**
	 * Checks whether the actual index is not negative and is not greater than
	 * the last index. If it is then the actual index is returned instead.
	 * 
	 * @param idx
	 *            the index to check whether it is in range or not
	 * @param actualIdx
	 *            the index of the last character
	 * @return either <code>idx</code> (if is valid) or <code>actualIdx</code>
	 *         (if <code>idx</code> is not valid)
	 */
	int getEndIndex(int idx, int actualIdx) {
		return idx > -1 && idx <= actualIdx ? idx : actualIdx;
	}

	/**
	 * Checks whether the specified index is not negative. If it is then 0 is
	 * returned instead.
	 * 
	 * @param idx
	 *            the index to check whether it is in range or not
	 * @return either <code>idx</code> (if is valid) or 0 (if <code>idx</code>
	 *         is not valid)
	 */
	int getStartIndex(int idx) {
		return idx < 0 ? 0 : idx;
	}

	/**
	 * Defines the changes that will be done over the properties of the source
	 * file.
	 * 
	 * @see ChangeDescription
	 * 
	 * @param props
	 *            the {@link Map} of properties read from the source file.
	 *            <b>Note</b> this is an immutable representation of the map.
	 * @return a {@link Map} defining the changes that will be done. The keys of
	 *         this map represent the names of the properties. The corresponding
	 *         values describe the particular changes that should be done done
	 *         over the specific property. Must not be <code>null</code>
	 */
	protected abstract List<ChangeDescription> defineChanges(Map<String, String> props);
}
