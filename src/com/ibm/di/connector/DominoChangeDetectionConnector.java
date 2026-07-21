/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesError;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.icu.text.SimpleDateFormat;

/**
 * This connector is used for receiving change notification from a Domino server
 * and feeding the AL with the applied on the server changes.
 */
public class DominoChangeDetectionConnector extends Connector implements
		ConnectorInterface, ChangelogInterface, Runnable, Comparator {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "dominochangedetectionconnector";

	/**
	 * Name of the connector.
	 */
	private static final String CONN_NAME = "Domino Change Detection Connector";

	/**
	 * Constant for document change type - Created
	 */
	private static final String CHANGE_TYPE_CREATED = "Created";

	/**
	 * Constant for document change type - Modified
	 */
	private static final String CHANGE_TYPE_MODIFIED = "Modified";

	/**
	 * Constant for document change type - Deleted
	 */
	private static final String CHANGE_TYPE_DELETED = "Deleted";

	/**
	 * Constant for document change type - Unknown
	 */
	private static final String CHANGE_TYPE_UNKNOWN = "unknown";

	/**
	 * Full date format
	 */
	private static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * Long date format
	 */
	private static final String DATE_FORMAT_LONG = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Short date format
	 */
	private static final String DATE_FORMAT_SHORT = "yyyy-MM-dd";

	/**
	 * Used for formatting a date with date, time and milliseconds
	 */
	private static final SimpleDateFormat mDateFormatFull = new SimpleDateFormat(
			DATE_FORMAT_FULL);

	/**
	 * Used for formatting a date with date and time
	 */
	private static final SimpleDateFormat mDateFormatLong = new SimpleDateFormat(
			DATE_FORMAT_LONG);

	/**
	 * Used for formatting a date (only date without time)
	 */
	private static final SimpleDateFormat mDateFormatShort = new SimpleDateFormat(
			DATE_FORMAT_SHORT);

	/**
	 * Attribute name - universal id.
	 */
	private static final String ATTR_UNIVERSAL_ID = "$$UNID";

	/**
	 * Attribute name - note id.
	 */
	private static final String ATTR_NOTE_ID = "$$NoteID";

	/**
	 * Attribute name - change type.
	 */
	private static final String ATTR_CHANGE_TYPE = "$$ChangeType";

	/**
	 * Attribute name - date created.
	 */
	private static final String ATTR_DATE_CREATED = "$$DateCreated";

	/**
	 * Attribute name - date modified.
	 */
	private static final String ATTR_DATE_MODIFIED = "$$DateModified";

	/**
	 * Parameter name - domino server IP.
	 */
	private static final String PARAM_DOMINO_SERVER_IP = "dominoServerIP";

	/**
	 * Parameter name - http port.
	 */
	private static final String PARAM_DOMINO_HTTP_PORT = "httpPort";

	/**
	 * Parameter name - ior.
	 */
	private static final String PARAM_IOR = "ior";

	/**
	 * Parameter name - user name.
	 */
	private static final String PARAM_DOMINO_LOGIN = "userName";

	/**
	 * Parameter name - internet password.
	 */
	private static final String PARAM_DOMINO_PASSWORD = "internetPassword";

	/**
	 * Parameter name - database.
	 */
	private static final String PARAM_DOMINO_DATABASE = "database";

	/**
	 * Parameter name - delivery mode.
	 */
	private static final String PARAM_DELIVERY_MODE = "deliveryMode";

	/**
	 * Parameter name - system store key.
	 */
	private static final String PARAM_PERSISTENT_STORE = "systemStoreKey";

	/**
	 * Parameter name - start at.
	 */
	private static final String PARAM_START_AT = "startAt";

	/**
	 * Parameter name - start date.
	 */
	private static final String PARAM_START_DATE_TIME = "startDate";

	/**
	 * Parameter name - sleep interval.
	 */
	private static final String PARAM_SLEEP_INTERVAL = "sleepInterval";

	/**
	 * Parameter name - timeout.
	 */
	private static final String PARAM_TIMEOUT = "timeout";

	/**
	 * Parameter name - iiop SSL.
	 */
	private static final String PARAM_IIOP_SSL = "iiopSSL";

	/**
	 * Parameter name - sort.
	 */
	private static final String PARAM_SORT = "sort";

	/**
	 * Parameter name - session type.
	 */
	private static final String PARAM_USE_LOCAL_SESSION = "sessionType";

	/**
	 * Parameter name - local client.
	 */
	private static final String LOCAL_CLIENT_MODE = "LocalClient";

	/**
	 * Value for starting at the begin of the data
	 */
	private static final String VAL_SA_START_OF_DATA = "Start Of Data";

	/**
	 * Value for starting at the end of the data
	 */
	private static final String VAL_SA_END_OF_DATA = "End Of Data";

	/**
	 * Value for starting at specific date.
	 */
	private static final String VAL_SA_SPECIFIC_DATE = "Specific date";

	/**
	 * Value for delivery mode normal
	 */
	private static final String VAL_DM_NORMAL = "Normal assured delivery";

	/**
	 * Value for delivery mode once and only once
	 */
	private static final String VAL_DM_ONCE_AND_ONLY_ONCE = "Assured once and only once delivery";

	/**
	 * Property name for synchronization time
	 */
	private static final String PROP_SYNC_TIME = "SYNC_TIME";

	/**
	 * Property name for check docs flag
	 */
	private static final String PROP_SYNC_CHECK_DOCS = "SYNC_CHECK_DOCS";

	/**
	 * Protocol delimiter - ':'
	 */
	private static final char PROTOCOL_DELIMITER = ':';

	/**
	 * Value of document state - deleted
	 */
	private static final String DOC_STATE_DELETED = "DEL";

	/**
	 * Value of document state - normal
	 */
	private static final String DOC_STATE_NORMAL = "NOR";

	/**
	 * Value for null date
	 */
	private static final String NULL_DATE_VALUE = "NULL_DATE";

	/**
	 * Prefix for the property 'store table'
	 */
	private static final String PROP_STORE_TABLE_PREFIX = "domch_";

	/**
	 * The IP address of the Domino Server.
	 */
	private String mDominoServerIP = null;

	/**
	 * The port on which the HTTP task of the Domino Server is launched
	 */
	private int mHTTPPort = 80;

	/**
	 * The FullName of the Internet user used for the IIOP session.
	 */
	private String mUser = null;

	/**
	 * The Internet password used for the IIOP session.
	 */
	private String mPass = null;

	/**
	 * The name of the database that will be polled for changes.
	 */
	private String mDatabase = null;

	/**
	 * The name of the property that stores the synchronization state in the
	 * User Property Store.
	 */
	private String mSystemStorePropName = null;

	/**
	 * Marks if the native Notes session is intialized.
	 */
	private boolean mNativeSessionInitialized = false;

	/**
	 * <code>true</code> when the documents must be sorted
	 */
	private boolean sortingEnabled = false;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	/**
	 * Text string required by the Domino Java API in order to establish an IIOP
	 * session to the Domino Server.
	 */
	private String mIOR = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Specifies predefined start position - start of data, specific date or end
	 * of data. Only used when the persistent parameter is not found in the
	 * system store.
	 */
	private String mStartAt = null;

	/**
	 * Holds the start date parameter value.
	 */
	private Date mStartDateParamValue = null;

	/**
	 * Specifies the start date used for the current poll.
	 */
	private Date mStartDate = null;

	/**
	 * Stores the "Sleep Interval" Connector parameter.
	 */
	private int mSleepInterval = 0;

	/**
	 * Stores the "Timeout" Connector parameter.
	 */
	private int mTimeout = 0;

	/**
	 * Holds the end date returned by the native search call that will be used
	 * as a start date on the next poll for changes.
	 */
	private Date mEndDate = new Date();

	/**
	 * The IIOP session object.
	 */
	private Session mSession = null;

	/**
	 * The IIOP database object.
	 */
	private Database mDB = null;

	/**
	 * The list of UnIDs of changed documents returned by the native layer.
	 */
	private Vector mModifiedNotesUnIDs = null;

	/**
	 * Holds the current position in the UnIDs list.
	 */
	private int mCurrentUnIDIdx = 0;

	/**
	 * The current document object retrieved through the UnIDs list.
	 */
	private Document mCurrentDocument = null;

	/**
	 * Indicates whether checks need to be made in the System Store for
	 * documents processed on previous runs.
	 */
	private boolean mCheckProcessedUnIDs = false;

	/**
	 * The System Store custom table used by the Connector to store the UnIDs of
	 * processed documents in "once and only once" delivery mode.
	 */
	private PropertyStore mPropertyStore = null;

	/**
	 * Default property store instance. Cache it as an instance variable to
	 * improve performance.
	 */
	private PropertyStore defaultPropStore = null;

	/**
	 * The name of the custom System Store table.
	 */
	private String mPropertyStoreTableName = null;

	/**
	 * Indicates whether SSL should be used; should contain "true" or "false"
	 */
	private String mUseIIOPSSL = null;

	/**
	 * indicates whether a local session will be used
	 */
	private boolean mUseLocalSession = false;

	/**
	 * Holds the dates the documents were last modified on.
	 */
	private HashMap<String, Date> datesHashMap = null;

	// **************************************
	// members for the inner Notes thread
	// **************************************
	/**
	 * Index for command for the inner Notes thread - no command
	 */
	private static final int NO_COMMAND = 0;

	/**
	 * Index for command for the inner Notes thread - initialize notes
	 */
	private static final int COMMAND_INIT_NOTES = 1;

	/**
	 * Index for command for the inner Notes thread - get modified notes
	 */
	private static final int COMMAND_GET_MODIFIED_NOTES = 2;

	/**
	 * Index for command for the inner Notes thread - command stop
	 */
	private static final int COMMAND_STOP = 3;

	/**
	 * Index for command for the inner Notes thread - command initialize
	 */
	private static final int COMMAND_INITIALIZE = 4;

	/**
	 * Index for command for the inner Notes thread - command select entries
	 */
	private static final int COMMAND_SELECT_ENTRIES = 5;

	/**
	 * Index for command for the inner Notes thread - command get next entry
	 */
	private static final int COMMAND_GET_NEXT_ENTRY = 6;

	/**
	 * Index for command skip current entry document if error.
	 */
	private static final int COMMAND_SKIP_CURRENT_DOCUMENT = 7; // Defect # 11966

	/**
	 * Notes thread that processes commands
	 */
	private Thread mNotesThread = null;

	/**
	 * Command to be processed from the notes thread
	 */
	private int mNotesThreadCommand = NO_COMMAND;

	/**
	 * Indicates whether a thread should wait the processing of the execution of
	 * another command
	 */
	private boolean mWaitForNotesThread = false;

	/**
	 * Argument list
	 */
	private Object[] mNotesThreadCallArgList = null;

	/**
	 * Return value from Notes thread call
	 */
	private Object mNotesThreadCallReturnValue = null;

	/**
	 * Error occurred during the execution of the Notes thread
	 */
	private Throwable mNotesThreadCallError = null;

	/**
	 * Indicates whether the native library is loaded
	 */
	private static boolean mLibLoaded = false;

	/**
	 * Indicates whether the state should be saved after read.
	 */
	private boolean mAfterRead = true;

	/**
	 * Method used for saving the state to the System Store
	 */
	private int mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_AFTER_READ;

	/* RL-1 @@@ */
	/**
	 * Current document universal ID
	 */
	private String currentDocUNID = null;

	/**
	 * End date of current document
	 */
	private Object currentDocEndDate = null;

	/**
	 * Maps Notes documents <br>
	 * <b>key</b> document's UNID<br>
	 * <b>value</b> document's end date
	 */
	private Hashtable<String, Object> notesDocs = null;

	// **************************************
	// native methods
	// **************************************

	/**
	 * Native method for initializing Notes
	 *
	 * @param aDominoServer
	 *            server
	 * @param aDbName
	 *            database name
	 * @param aPassword
	 *            password
	 * @throws Exception
	 *             if an error occrs
	 */
	private native void initNotes(String aDominoServer, String aDbName,
			String aPassword) throws Exception;

	/**
	 * Native method for retrieval of modified Notes
	 *
	 * @param aStartDate
	 *            start date
	 * @param aEndDate
	 *            end date
	 * @return vector
	 * @throws Exception
	 *             if an error occurs
	 */
	private native Vector getModifiedNotes(String aStartDate,
			StringBuffer aEndDate) throws Exception;

	/**
	 * Native method that terminates notes
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	private native void termNotes() throws Exception;

	/**
	 * Connector's public constructor. Sets Connector's name and supported
	 * modes.
	 */
	public DominoChangeDetectionConnector() {
		setName(CONN_NAME);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	// *************************************************************************
	// routines for the inner Notes thread
	// *************************************************************************

	/**
	 * Executes Notes commands until termination is requested
	 */
	public synchronized void run() {

		try {
			boolean terminate = false;
			while (!terminate) {
				try {
					switch (mNotesThreadCommand) {

					case COMMAND_INITIALIZE:

						try {
							inner_initNotes();
							inner_initialize();
						} catch (Exception ie) {
							terminate = true;
							throw ie;
						}

						break;

					case COMMAND_SELECT_ENTRIES:
						inner_selectEntries();
						break;

					case COMMAND_GET_NEXT_ENTRY:
						mNotesThreadCallReturnValue = inner_getNextEntry();
						break;

					case COMMAND_GET_MODIFIED_NOTES:
						mNotesThreadCallReturnValue = getModifiedNotes(
								(String) mNotesThreadCallArgList[0],
								(StringBuffer) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_STOP: {
						terminate = true;
						inner_termNotes();
						break;
					}
					case COMMAND_SKIP_CURRENT_DOCUMENT:
						++ mCurrentUnIDIdx ;
						break;
					}
				} catch (Throwable e) {
					mNotesThreadCallError = e;
				} finally {
					// mark and notify that command processing has finished
					mNotesThreadCommand = NO_COMMAND;
					notifyAll();
				}

				if (!terminate) {
					while (mNotesThreadCommand == NO_COMMAND) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		} finally {
			mNotesThread = null;
		}
	}

	/**
	 * Skip the current document. Use this method to skip problem documents when
	 * the Connector will otherwise die with an exception.
	 * <p>
	 * For example use the following script in the "Default On Error" hook of
	 * the Connector:
	 *
	 * <pre>
	 * thisConnector.connector.skipCurrentDocument();
	 * </pre>
	 *
	 * </p>
	 *
	 *
	 * @throws Exception
	 *             If the Notes thread is not running or the Notes thread
	 *             encounters an error while processing the command.
	 */
	public void skipCurrentDocument()throws Exception{  // Method added to fix defect 11966

		executeCommand(COMMAND_SKIP_CURRENT_DOCUMENT, null, false);

	}

	/**
	 * This method tells whether the AssemblyLine is being shutdown. Its purpose
	 * is to allow the notes thread to exit when the AssemblyLine is shutdown.
	 *
	 * @return the status of termination requested flag
	 */
	private boolean getTerminationRequested() {
		boolean ret = false;

		Object ctx = getContext();
		if (ctx instanceof com.ibm.di.server.AssemblyLine) {
			ret = ((com.ibm.di.server.AssemblyLine) ctx)
					.getTerminationRequested();
		}

		return ret;
	}

	/**
	 * Executes command.
	 *
	 * @param aCommand
	 *            command index
	 * @param aArgList
	 *            argument list
	 * @param aHasReturnValue
	 *            has the command return value
	 * @return <code>null</code> or the return value of the execution of the
	 *         command
	 * @throws Exception
	 */
	private synchronized Object executeCommand(int aCommand, Object[] aArgList,
			boolean aHasReturnValue) throws Exception {
		if (!isNotesThreadAlive()) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.THREADDOWN.EXCEP"));
		}

		// if a command is set by another thread, wait for the Notes thread to
		// process it
		while (mWaitForNotesThread) {
			try {
				wait();
			} catch (InterruptedException e) {

				// the thread of the Assembly Line is interrupted => return
				// immediately
				return null;
			}
		}

		// set command type and arguments
		mNotesThreadCommand = aCommand;
		mNotesThreadCallArgList = aArgList;

		// notify Notes thread to process the command
		// (other threads, trying to execute a command will also be notified)
		notifyAll();

		// wait for Notes thread to finish command execution
		mWaitForNotesThread = true;
		while (mNotesThreadCommand != NO_COMMAND) {
			try {
				wait();
			} catch (InterruptedException e) {

				// the thread of the Assembly Line is interrupted => return
				// immediately
				return null;
			}
		}
		mWaitForNotesThread = false;

		// notify other threads that might be waiting to execute a command
		notifyAll();

		// get command execution result
		if (mNotesThreadCallError != null) {
			Throwable callError = mNotesThreadCallError;
			mNotesThreadCallError = null;

			if (callError instanceof Exception) {
				throw (Exception) callError;
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.FATAL.EXCEP", callError
								.toString()));
			}
		}

		if (aHasReturnValue) {
			Object callReturnValue = mNotesThreadCallReturnValue;
			mNotesThreadCallReturnValue = null;
			return callReturnValue;
		} else {
			return null;
		}
	}

	/**
	 * Checks if Notes thread is alive.
	 *
	 * @return <code>true</code> if the Notes thread is not <code>null</code>
	 *         and is alive, <code>false</code> otherwise.
	 */
	private boolean isNotesThreadAlive() {
		return (mNotesThread != null && mNotesThread.isAlive());
	}

	/**
	 * Must perform a safe initialization.
	 *
	 * @param aDominoServer
	 *            name of the domino server
	 * @param aDbName
	 *            database name
	 * @throws Exception
	 *             if an error occurs
	 */
	private void safeInitNotes(String aDominoServer, String aDbName)
			throws Exception {
		executeCommand(COMMAND_INIT_NOTES, new Object[] { aDominoServer,
				aDbName }, false);
	}

	/**
	 *
	 * Must perform a safe retrieval of modified notes
	 *
	 * @param aStartDate
	 *            start date
	 * @param aEndDate
	 *            end date
	 * @return value from Notes thread call
	 * @throws Exception
	 */
	private Vector safeGetModifiedNotes(String aStartDate, StringBuffer aEndDate)
			throws Exception {
		mNotesThreadCallReturnValue = getModifiedNotes(aStartDate, aEndDate);
		if (sortingEnabled) {
			datesHashMap = new HashMap<String, Date>();
			debug(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.SORTING"));
			Collections.sort((Vector) mNotesThreadCallReturnValue, this);
		}
		return (Vector) mNotesThreadCallReturnValue;

	}

	// *************************************************************************
	// ConnectorInterface implementation
	// *************************************************************************

	/**
	 * Reads Connector parameters and initializes the local and IIOP Notes
	 * sessions. Opens the specified database in both local and IIOP session.
	 *
	 * @param aObj
	 *            Object.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object aObj) throws Exception {
		/**
		 * Loads the native library.
		 */
		if (!mLibLoaded) {
			System.loadLibrary("domchdet");
			mLibLoaded = true;
		}

		readConnectorParams();

		// start internal thread
		if (isNotesThreadAlive()) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.THREADACTIVE.EXCEP"));
		}

		mNotesThread = new Thread(this);
		mNotesThreadCommand = NO_COMMAND;
		mNotesThread.start();

		executeCommand(COMMAND_INITIALIZE, new Object[] { aObj }, false);
	}

	/**
	 * Initializes connector.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void inner_initialize() throws Exception {

		try {

			if (mUseLocalSession) {
				String host = null;
				String user = null;
				try {
					mSession = NotesFactory.createSession(host, user, mPass);
					logmsg(sResHash
							.getString("CONNECTOR.DOMINOCHGDETECT.LOCAL.CLIENT.SESSION.CREATED"));
				} catch (NotesException e) {
					logmsg(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
					throw new Exception(
							sResHash
									.getString(
											"CONNECTOR.DOMINOCHGDETECT.LOCAL.CLIENT.SESSION.EXCEP",
											new Object[] {
													e.getClass().getName(),
													e.text }));
				}

			} else {
				// create IIOP session
				String[] args = null;
				String[] iorArgs = null;

				boolean isSslEnabled = false;
				if (mUseIIOPSSL != null && mUseIIOPSSL.equalsIgnoreCase("true")) {
					isSslEnabled = true;
					args = new String[1];
					args[0] = "-ORBEnableSSLSecurity";

					iorArgs = new String[1];
					iorArgs[0] = "-HTTPEnableSSLSecurity";
				}

				if (mIOR != null && mIOR.startsWith("IOR:")) {
					try {
						mSession = NotesFactory.createSessionWithIOR(mIOR, args, mUser, mPass);
						if (isSslEnabled) {
							logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.IOR.USED.SSL.ON.SESSION.CREATED"));
						} else {
							logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.CONFIGURED.IOR.USED.SSL.OFF.SESSION.CREATED"));
						}
					} catch (NotesException e) {
						logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT", new Object[] {
								Integer.valueOf(e.id), e.text }));
						if (isSslEnabled) {
							throw new Exception(sResHash.getString(
									"CONNECTOR.DOMINOCHGDETECT.IOR.INVALID.SSL.ON.SESSION.NOTCREATED.EXCEP", new Object[] {
											e.getClass().getName(), e.text }));
						} else {
							throw new Exception(sResHash.getString(
									"CONNECTOR.DOMINOCHGDETECT.CONFIGURED.IOR.INVALID.SSL.OFF.EXCEP", new Object[] {
											e.getClass().getName(), e.text }));
						}
					}
				} else {
					// We explicitly get the IOR by providing username and
					// password.
					// This way the IOR can be obtained even if Domino's
					// HTTP task does not permit anonymous connections.
					try {
						mIOR = NotesFactory.getIOR(mDominoServerIP + ":" + mHTTPPort, iorArgs, mUser, mPass);
						mSession = NotesFactory.createSessionWithIOR(mIOR, args, mUser, mPass);
						if (isSslEnabled) {
							logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.PORT.USED.SSL.ON.SESSION.CREATED"));
						} else {
							logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.PORT.USED.SSL.OFF.SESSION.CREATED"));
						}
					} catch (NotesException e) {
						logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT", new Object[] {
								Integer.valueOf(e.id), e.text }));
						if (isSslEnabled) {
							throw new Exception(sResHash.getString(
									"CONNECTOR.DOMINOCHGDETECT.PORT.USED.SSL.ON.SESSION.NOTCREATED.EXCEP", new Object[] {
											e.getClass().getName(), e.text }));
						} else {
							throw new Exception(sResHash.getString(
									"CONNECTOR.DOMINOCHGDETECT.PORT.USED.SSL.OFF.SESSION.NOTCREATED.EXCEP", new Object[] {
											e.getClass().getName(), e.text }));
						}
					}
				}
			}

			logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.CONNECTED.INFO", new Object[] { mSession.getServerName(),
					mSession.getNotesVersion(), mSession.getPlatform() }));

			try {
				mDB = mSession.getDatabase(mDominoServerIP, mDatabase, false);
			} catch (NotesException e) {
				logmsg(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
						new Object[] { Integer.valueOf(e.id), e.text }));
				logmsg(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.DATABASE.EXCEPTION",
						new Object[] { mDatabase, mDominoServerIP }));
				mDB = null;
				mSession = null;
				throw e;
			}

			if (mDB == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.DATABASE.EXCEPTION.2",
						new Object[] { mDatabase, mDominoServerIP }));
			}

			// open the System Store table
			try {
				if (mPropertyStoreTableName != null) {
					mPropertyStore = new PropertyStore(mPropertyStoreTableName);
				}
			} catch (Exception e) {
				logmsg(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.NOSYSTEMSTORE.EXCEPTION", e
								.toString()));
				throw e;
			}

		} catch (Exception e) {
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.NOINIT.EXCEPTION", e.toString()));

			if (mNativeSessionInitialized) {
				try {
					inner_termNotes();
				} catch (Exception te) {

					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOCHGDETECT.STOPSESSION.WARNING",
										te.toString()));
					}
				}
			}

			try {
				if (mDB != null) {
					mDB.recycle();
					mDB = null;
				}

				if (mSession != null) {
					mSession.recycle();
					mSession = null;
				}
			} catch (NotesException te) {
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.DOMINOCHGDETECT.IIOP.WARNING",
							new Object[] { "" + te.id, te.text }));
				}
			}

			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.SESSION.EXCEPTION",
					new Object[] { e.getClass().getName(), e.getMessage() }));

			throw e;
		}

	}

	/**
	 * Initializes the C layer and in case of local calls initializes Notes
	 * thread
	 *
	 * @throws Exception
	 *             if an error occurs.
	 *
	 */
	private void inner_initNotes() throws Exception {

		try {
			String idPassword = getParam("IDFilePassword");
			if (idPassword == null || idPassword.trim().length() == 0)
				idPassword = mPass;
			initNotes(mDominoServerIP, mDatabase, idPassword);
			mNativeSessionInitialized = true;
		} catch (Exception e) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.NOSESSION.EXCEP", e));
		}

		if (mUseLocalSession) {
			NotesThread.sinitThread();
		}
	}

	/**
	 * Terminates worker thread and Notes thread in the correct sequence
	 *
	 * @throws Exception
	 */
	private void inner_termNotes() throws Exception {

		if (mUseLocalSession) {
			NotesThread.stermThread();
		}

		if (mNativeSessionInitialized) {
			try {
				termNotes();
			} catch (Exception e) {
				debug(sResHash.getString(
									"CONNECTOR.DOMINOCHGDETECT.TERMINATE.COULD.NOT.TERMINATE.THE.LOCAL.NOTES.SESSION",
									e.toString()));
			}
		}

	}

	/**
	 * Reads Connector parameters.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void readConnectorParams() throws Exception {

		mUseIIOPSSL = getParam(PARAM_IIOP_SSL);
		if ((mUseIIOPSSL == null) || (mUseIIOPSSL.trim().length() == 0)) {
			mUseIIOPSSL = "false";
		}

		String mSessionType = getParam(PARAM_USE_LOCAL_SESSION);

		if ((mSessionType != null)
				&& (LOCAL_CLIENT_MODE.equalsIgnoreCase(mSessionType))) {
			mUseLocalSession = true;
		}

		mDominoServerIP = getParam(PARAM_DOMINO_SERVER_IP);
		if (((mDominoServerIP == null) || (mDominoServerIP.trim().length() == 0))
				&& !mUseLocalSession) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOCHGDETECT.DOMINOSERVERIP.EXCEPTION"));
		}

		String mHTTPTaskPort = getParam(PARAM_DOMINO_HTTP_PORT);

		if (mHTTPTaskPort != null && (mHTTPTaskPort.trim().length() != 0)) {

			try {
				mHTTPPort = Integer.parseInt(mHTTPTaskPort);
			} catch (NumberFormatException e) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.UNPARSEABLE", mHTTPTaskPort));
			}
		}

		mIOR = getParam(PARAM_IOR);

		String sort = getParam(PARAM_SORT);
		if (sort != null) {
			sortingEnabled = Boolean.valueOf(sort).booleanValue();
		} else {
			sortingEnabled = false;
		}

		mUser = getParam(PARAM_DOMINO_LOGIN);
		if ((!mUseLocalSession)
				&& ((mUser == null) || (mUser.trim().length() == 0))) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.USERNAME.EXCEPTION"));
		}

		mPass = getParam(PARAM_DOMINO_PASSWORD);
		if (mPass == null) {
			mPass = "";
		}

		mDatabase = getParam(PARAM_DOMINO_DATABASE);
		if ((mDatabase == null) || (mDatabase.trim().length() == 0)) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOCHGDETECT.DATABASE.EXCEPTION.3"));
		}
		mDatabase = mDatabase.trim();

		String deliveryMode = getParam(PARAM_DELIVERY_MODE);
		if ((deliveryMode != null) && (deliveryMode.trim().length() > 0)) {
			// we have an old configuration
			// i.e. created with release previous from TDI 6.1.1
			deliveryMode = deliveryMode.trim();
			if (deliveryMode.equalsIgnoreCase(VAL_DM_NORMAL)
					|| deliveryMode.equalsIgnoreCase(VAL_DM_ONCE_AND_ONLY_ONCE)) {
				if (debugMode()) {
					logmsg(sResHash
							.getString("CONNECTOR.DOMINOCHGDETECT.OLD.CONFIG.DELIVERY.MODE"));
				}
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.BADDELIVERYMODE.EXCEPTION",
						deliveryMode));
			}
		}

		mSystemStorePropName = getParam(PARAM_PERSISTENT_STORE);
		if ((mSystemStorePropName == null)
				|| (mSystemStorePropName.trim().length() == 0)) {
			logmsg(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.MISSING.SYSTEMSTOREKEY.WARN"));
			mSystemStorePropName = null;
			mPropertyStoreTableName = null;
		} else {
			mSystemStorePropName = mSystemStorePropName.trim();
			mPropertyStoreTableName = PROP_STORE_TABLE_PREFIX
					+ mSystemStorePropName;
			defaultPropStore = StoreFactory.getDefaultPropertyStore();
		}

		// read the "Start at" value - it is only used when the specified
		// persistent parameter is not found in the store
		mStartAt = getParam(PARAM_START_AT);
		if (mStartAt != null) {
			mStartAt = mStartAt.trim();
		}

		String startDate = getParam(PARAM_START_DATE_TIME);
		if ((startDate != null) && (startDate.trim().length() > 0)) {
			mStartDateParamValue = stringToDate(startDate);
		}

		// read, parse and store the "Sleep Interval" parameter
		String sleepIntervalStr = getParam(PARAM_SLEEP_INTERVAL);
		try {
			mSleepInterval = Integer.parseInt(sleepIntervalStr);
		} catch (NumberFormatException e) {
			mSleepInterval = 0;
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.SLEEPINT.WARN", new Object[] {
							sleepIntervalStr, "" + mSleepInterval }));
		}
		if (mSleepInterval < 0) {
			mSleepInterval = 0;
		}

		// read, parse and store the "Timeout" parameter
		String timeoutStr = getParam(PARAM_TIMEOUT);
		try {
			mTimeout = Integer.parseInt(timeoutStr);
		} catch (NumberFormatException e) {
			mTimeout = 5;
			logmsg(sResHash.getString("CONNECTOR.DOMINOCHGDETECT.TIMEOUT.WARN",
					new Object[] { timeoutStr, "" + mTimeout }));
		}
		if (mTimeout < 0) {
			mTimeout = 0;
		}

		String stateKeyPersistence = getParam(ChangelogInterface.CONN_PARAM_STATE_KEY_PERSISTENCE);
		if (stateKeyPersistence != null
				&& stateKeyPersistence.trim().length() > 0
				&& ((deliveryMode == null) || (deliveryMode
						.equalsIgnoreCase(VAL_DM_NORMAL)))) {
			if (stateKeyPersistence
					.equals(ChangelogInterface.PARAM_VAL_END_OF_CYCLE)) {
				mAfterRead = false;
				mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_END_OF_CYCLE;
			} else if (stateKeyPersistence
					.equals(ChangelogInterface.PARAM_VAL_MANUAL)) {
				mAfterRead = false;
				mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_MANUAL;
				notesDocs = new Hashtable<String, Object>();
			}
		}
	}

	/**
	 * Reads and sets the start synchronization state.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	public void selectEntries() throws Exception {
		executeCommand(COMMAND_SELECT_ENTRIES, null, false);
	}

	/**
	 * Prepares connector for sequential read.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void inner_selectEntries() throws Exception {

		Entry storePropEntry = null;
		try {
			if (mSystemStorePropName != null) {
				storePropEntry = (Entry) defaultPropStore.getProperty(mSystemStorePropName);
			}
		} catch (Exception e) {
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.NOSYNCSYSSTORE.EXCEPTION", e
							.toString()));
			throw e;
		}

		if (storePropEntry != null) {
			try {
				getSynchronizationState(storePropEntry);
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.DOMINOCHGDETECT.NOSRETRIEVESYSSTORE.EXCEPTION",
								e.toString()));
				throw e;
			}

			if ((mAfterRead || mStateKeySaveMethod == ChangelogInterface.SAVE_STATE_END_OF_CYCLE)
					&& !mCheckProcessedUnIDs) {
				// a new check for whether the table size is greater than 0
				// should
				// be added here, when the System Store API call is available.
				clearPropertyStore();
			}
		}
		// use the startAt parameter
		else {
			if (mStartAt == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOCHGDETECT.SYSTEM.STORE.KEY.NOT.FOUND.AND"));
			}

			if (mStartAt.equalsIgnoreCase(VAL_SA_START_OF_DATA)) {
				logmsg(sResHash
						.getString("CONNECTOR.DOMINOCHGDETECT.START.AT.START.OF.DATA.WILL.PERFORM"));
				mStartDate = null;
			} else if (mStartAt.equalsIgnoreCase(VAL_SA_END_OF_DATA)) {
				logmsg(sResHash
						.getString("CONNECTOR.DOMINOCHGDETECT.START.AT.END.OF.DATA.WILL.LISTEN"));
				// get the time of the last modification and add 1/100 of the
				// second to
				// ensure that the last modification is not reported
				mStartDate = new Date(mDB.getLastModified().toJavaDate()
						.getTime() + 1000);
			} else if (mStartAt.equalsIgnoreCase(VAL_SA_SPECIFIC_DATE)) {
				mStartDate = mStartDateParamValue;
				logmsg(sResHash
						.getString(
								"CONNECTOR.DOMINOCHGDETECT.START.AT.SPECIFIC.DATE.WILL.USE.START.DATE",
								mStartDate));
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.INVALID.START.AT.VALUE",
						mStartAt));
			}
		}

		// clear the UnID list
		mModifiedNotesUnIDs = null;
	}

	/**
	 * Retrieves the synchronization state from the System Store Entry.
	 *
	 * @param aEntry
	 *            {@link Entry}
	 * @throws Exception
	 *             if an error occurs
	 */
	private void getSynchronizationState(Entry aEntry) throws Exception {
		// get start date
		Object attrValue = aEntry.getObject(PROP_SYNC_TIME);
		if (attrValue == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOCHGDETECT.SYSTEM.STORE.ENTRY.DOES.NOT.CONTAIN.THE.ATTR"));
		}

		if (attrValue instanceof Date) {
			mStartDate = (Date) attrValue;
		} else if (attrValue instanceof String) {
			if (NULL_DATE_VALUE.equalsIgnoreCase((String) attrValue)) {
				mStartDate = null;
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.INVALID.VALUE.OF.THE.ATTR",
						new Object[] { PROP_SYNC_TIME, (String) attrValue }));
			}
		} else {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.SYSTEM.STORE.ENTRY.ATTRIBUTE.IS.UNKNOWN",
									new Object[] { attrValue.getClass()
											.getName() }));
		}

		if (debugMode()) {
			debug(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.GETSYNCHRONIZATIONSTATE.START.DATE",
							mStartDate));
		}

		// get check docs flag
		Boolean checkDocs = (Boolean) aEntry.getObject(PROP_SYNC_CHECK_DOCS);
		mCheckProcessedUnIDs = (checkDocs != null && checkDocs.booleanValue() == true);

		if (mCheckProcessedUnIDs && mAfterRead) {
			logmsg(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.WILL.CHECK.IF.REPORTED.UNIDS.ARE"));
		}
	}

	/**
	 * Retrieves the next changed entry.
	 *
	 * @return next Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	public Entry getNextEntry() throws Exception {
		return (Entry) executeCommand(COMMAND_GET_NEXT_ENTRY, null, true);
	}

	/**
	 * Retrieves the next changed entry.
	 *
	 * @return next Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry inner_getNextEntry() throws Exception {

		Entry entry = null;
		boolean doTimeout = false;

		long startTime = System.currentTimeMillis();
		long currentTime;

		do {
			// the first time this method is called, no UnIDs are retrieved
			// and the internal storage mModifiedNotesUnIDs == NULL
			if (mModifiedNotesUnIDs != null) {
				entry = getEntryFromList();

				if (entry == null) {
					// complete Connector's poll
					mStartDate = mEndDate;
					mCheckProcessedUnIDs = false;
					if (mStateKeySaveMethod != ChangelogInterface.SAVE_STATE_MANUAL) {
						storeSynchronizationState(false);
					}
					if ((mAfterRead || mStateKeySaveMethod == ChangelogInterface.SAVE_STATE_END_OF_CYCLE)) {
						clearPropertyStore();
					}
				}
			}

			if (entry == null) {
				while (!doTimeout && retrieveChangedUnIDs() == 0) {

					if (getTerminationRequested()) {

						// The Assembly Line is being shutdown => return
						// immediately
						// This must be done in order to break a possible
						// endless loop, which
						// would prevent the Assembly Line to shutdown due to
						// synchronization issues:
						// The 'wait' call in 'executeCommand' needs a lock to
						// complete even if the
						// thread is interrupted, so the notes thread must not
						// be blocked in an endless
						// loop to be able to release the lock.
						return null;
					}

					currentTime = System.currentTimeMillis();
					if ((mTimeout == 0)
							|| ((mTimeout > 0) && ((currentTime + mSleepInterval * 1000) - startTime) < (mTimeout * 1000))) {
						Thread.sleep(mSleepInterval * 1000);
					} else {
						doTimeout = true;
						if (debugMode()) {
							debug(sResHash
									.getString("CONNECTOR.DOMINOCHGDETECT.TIMEOUT.WAITING.FOR.NEXT.CHANGED"));
						}
					}
				}

				if ((mAfterRead) && (mModifiedNotesUnIDs.size() > 0)) {
					// a new Connector poll started
					// mark in the System Store that restart will be
					// necessary
					storeSynchronizationState(true);
				}
			}
		} while (entry == null && !doTimeout);

		return entry;
	}

	/**
	 * Builds an Entry object from the first valid document from the list.
	 *
	 * @return the Entry built; <code>null</code> if no valid document is
	 *         found in the list.
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry getEntryFromList() throws Exception {

		if (!retrieveNextValidDocumentFromList()) {
			return null;
		}

		String strUnID = getCurrentDocumentUnID();

		Entry entry = null;
		Object documentEndDate = mEndDate;
		if (mCurrentDocument != null) {
			entry = buildEntry(mCurrentDocument);

			mCurrentDocument.recycle();
			mCurrentDocument = null;
		} else {
			String strNoteID = getCurrentDocumentNoteID();
			entry = buildDeletedEntry(strUnID, strNoteID);
			documentEndDate = NULL_DATE_VALUE;
		}

		mCurrentUnIDIdx++;

		if (mAfterRead && mPropertyStore != null) {
			mPropertyStore.updateProperty(strUnID, documentEndDate, true);
		} else if (mStateKeySaveMethod == ChangelogInterface.SAVE_STATE_MANUAL) {
			notesDocs.put(strUnID, documentEndDate);
		}

		currentDocUNID = strUnID;
		currentDocEndDate = documentEndDate;

		return entry;
	}

	/**
	 * Retrieves the next valid document from the list of changed document
	 * UnIDs. Documents that are further modified or deleted after the poll for
	 * changes was made, or are already processed on previous runs are not
	 * considered valid.
	 *
	 * @return <code>true</code> if a valid document is found;
	 *         <code>false</code> otherwise.
	 * @throws Exception
	 *             if an error occurs
	 */
	private boolean retrieveNextValidDocumentFromList() throws Exception {
		if (mModifiedNotesUnIDs == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.UNID.LIST.IS.NULL"));
		}

		while (mCurrentUnIDIdx < mModifiedNotesUnIDs.size()) {
			if (mCurrentDocument != null) {
				mCurrentDocument.recycle();
				mCurrentDocument = null;
			}

			boolean documentFurtherModified = false;
			String unID = getCurrentDocumentUnID();

			if (!getCurrentDocumentState().equalsIgnoreCase(DOC_STATE_DELETED)) {
				try {
					mCurrentDocument = mDB.getDocumentByUNID(unID);

					// documents modified after the getModifiedNotes call are
					// not processed;
					// they will be reported on the next poll
					Date dateModified = mCurrentDocument.getLastModified()
							.toJavaDate();
					if (dateModified.compareTo(mEndDate) >= 0) {
						documentFurtherModified = true;
					}
				} catch (NotesException e) {
					if (e.id == NotesError.NOTES_ERR_BAD_UNID) {
						documentFurtherModified = true;
						logmsg(sResHash
								.getString(
										"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
										new Object[] { Integer.valueOf(e.id),
												e.text }));
						logmsg(sResHash
								.getString(
										"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.RETRIEVE.DOCUMENT.WITH.UNID",
										unID));
						logmsg(sResHash
								.getString("CONNECTOR.DOMINOCHGDETECT.CHECK.DATABASE.PERMISSIONS"));
						logmsg(sResHash
								.getString("CONNECTOR.DOMINOCHGDETECT.IF.THE.DOCUMENT.IS.ALREADY"));
					} else {
						logmsg(sResHash
								.getString(
										"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
										new Object[] { Integer.valueOf(e.id),
												e.text }));
						throw new Exception(
								sResHash
										.getString(
												"CONNECTOR.DOMINOCHGDETECT.ERROR.ON.RETRIEVING.DOCUMENT.WITH.UNID",
												new Object[] { unID,
														String.valueOf(e.id),
														e.text }));
					}
				}
			}

			if ((!documentFurtherModified)
					&& (!isDocumentAlreadyProcessed(mCurrentDocument, unID))) {
				return true;
			}

			mCurrentUnIDIdx++;
		}

		return false;
	}

	/**
	 * Checks if the document passed has not been processed on previous
	 * Connector's runs.
	 *
	 * @param aDocument
	 *            {@link Document}
	 * @param aUnID
	 *            universal id
	 *
	 * @return <code>true</code> if the document is already processed;
	 *         <code>false</code> otherwise.
	 * @throws Exception
	 *             if an error occurs
	 */
	private boolean isDocumentAlreadyProcessed(Document aDocument, String aUnID)
			throws Exception {
		if (!mCheckProcessedUnIDs) {
			return false;
		}

		Object endDate = null;
		if (mPropertyStore != null) {
			endDate = mPropertyStore.getProperty(aUnID);
		}

		if (endDate != null) {
			if (endDate instanceof Date) {
				if (aDocument != null) {
					try {
						Date dateModified = aDocument.getLastModified()
								.toJavaDate();
						if (dateModified.compareTo((Date) endDate) < 0) {
							return true;
						}
					} catch (NotesException e) {
						logmsg(sResHash
								.getString(
										"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.READ.DOCUMENT.LAST.MODIFICATION.DATE",
										new Object[] { String.valueOf(e.id),
												e.text }));
						throw e;
					}
				}
			} else if (endDate instanceof String) {
				if (((String) endDate).equalsIgnoreCase(NULL_DATE_VALUE)) {
					return true;
				}
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.INVALID.END.DATE.VALUE",
						endDate.toString()));
			}
		}

		return false;
	}

	/**
	 * Deletes all stored document UnIDs from the System Store.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	private void clearPropertyStore() throws Exception {
		if (mPropertyStore != null) {
			// close the store
			try {
				mPropertyStore.closeStore();
			} catch (Exception e) {
				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.CLEARPROPERTYSTORE.ERROR.ON.CLOSING.THE.STORE",
									e.toString()));
				}
			}

			// drop and recreate the table
			try {
				if (mPropertyStoreTableName != null) {
					StoreFactory.dropTable(PropertyStore.TABLE_PREFIX + mPropertyStoreTableName);
					mPropertyStore = new PropertyStore(mPropertyStoreTableName);
				}
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.DOMINOCHGDETECT.ERROR.COULD.NOT.CLEAR.PROPERTY.STORE.TABLE",
								e.toString()));
				throw e;
			}
		}
	}

	/**
	 * Stores the synchronization state in the System Store.
	 *
	 * @param aCheckDocs
	 *            should documents be checked.
	 * @throws Exception
	 *             if an error occurs
	 */
	private void storeSynchronizationState(boolean aCheckDocs) throws Exception {
		if (debugMode()) {
			debug(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.STORESYNCHRONIZATION.STATE.START.DATE.IS",
							mStartDate));

			debug(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.STORESYNCHRONIZATION.STATE.CHECK.DOCS.IS",
							String.valueOf(mCheckProcessedUnIDs)));
		}
		try {
			if (mSystemStorePropName != null) {
				defaultPropStore.updateProperty(mSystemStorePropName, packSynchronizationState(aCheckDocs), true);
			}
		} catch (Exception e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.STORE.THE.SYNCHRONIZATION.STATE.IN.THE.SYSTEM.STORE",
							e.toString()));
			throw e;
		}
	}

	/**
	 * Packs the synchronization state into an Entry object.
	 *
	 * @param aCheckDocs
	 *            should documents be checked.
	 * @return Entry object
	 */
	private Entry packSynchronizationState(boolean aCheckDocs) {
		Entry entry = new Entry();

		if (mStartDate != null) {
			entry.setAttribute(PROP_SYNC_TIME, mStartDate);
		} else {
			entry.setAttribute(PROP_SYNC_TIME, NULL_DATE_VALUE);
		}
		entry.setAttribute(PROP_SYNC_CHECK_DOCS, Boolean.valueOf(aCheckDocs));

		return entry;
	}

	/**
	 * Terminates the local and IIOP Notes sessions.
	 */
	public void terminate() {
		if (!mUseLocalSession) {
			// terminate IIOP objects and session
			try {
				if (mDB != null) {
					mDB.recycle();
					mDB = null;
				}
				if (mSession != null) {
					mSession.recycle();
					mSession = null;
				}
			} catch (NotesException e) {
				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.TERMINATE.COULD.NOT.TERMINATE.THE.IIOP.SEESION",
									new Object[] { String.valueOf(e.id), e.text }));
				}
			}
		}

		// stop internal thread
		if (isNotesThreadAlive()) {
			try {
				executeCommand(COMMAND_STOP, null, false);
			} catch (Exception e) {
				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.TERMINATE.ERROR.ON.STOPPING.INTERNAL.THREAD",
									e.toString()));
				}
			}
		}

		// close the System Store table
		if (mPropertyStore != null) {
			try {
				mPropertyStore.closeStore();
			} catch (Exception e) {
				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.TERMINATE.ERROR.ON.CLOSING.THE.SYSTEM.STORE.TABLE",
									e.toString()));
				}
			}
		}
	}

	/**
	 * Builds an Entry object from a deleted Notes document.
	 *
	 * @param aUnID
	 *            document universal id
	 * @param aNoteID
	 *            notes id
	 * @return Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry buildDeletedEntry(String aUnID, String aNoteID)
			throws Exception {
		Entry entry = new Entry();

		entry.setAttribute(ATTR_UNIVERSAL_ID, aUnID);
		entry.setAttribute(ATTR_NOTE_ID, aNoteID);
		entry.setAttribute(ATTR_CHANGE_TYPE, CHANGE_TYPE_DELETED);
		entry.setOp(Entry.OP_DEL);

		return entry;
	}

	/**
	 * Builds an Entry object from a normal (non-deleted) Notes document.
	 *
	 * @param aDoc
	 *            {@link Document}
	 * @return Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry buildEntry(Document aDoc) throws Exception {
		Entry entry = new Entry();

		// set IDs
		entry.setAttribute(ATTR_UNIVERSAL_ID, aDoc.getUniversalID());
		entry.setAttribute(ATTR_NOTE_ID, aDoc.getNoteID());

		// set creation date
		try {
			 if (aDoc.getCreated() != null )
				entry.setAttribute(ATTR_DATE_CREATED, aDoc.getCreated()
					.toJavaDate());
			else
				entry.setAttribute(ATTR_DATE_CREATED, null);
		} catch (NotesException e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.RETRIEVE.CREATED.DATE.FOR.DOCUMENT",
							new Object[] { aDoc.getUniversalID(),
									String.valueOf(e.id), e.text }));
			logmsg(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.WILL.ASSIGN.VALUE.OF.NULL.TO.ATTRIBUTE"));
			entry.setAttribute(ATTR_DATE_CREATED, null);
		}

		// set modification date
		try {
			 if (aDoc.getLastModified() != null )
					entry.setAttribute(ATTR_DATE_MODIFIED, aDoc.getLastModified()
					.toJavaDate());
			else
				entry.setAttribute(ATTR_DATE_MODIFIED, null);

		} catch (NotesException e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.RETRIEVE.LAST.MODIFIED.DATE.FOR.DOCUMENT",
							new Object[] { aDoc.getUniversalID(),
									String.valueOf(e.id), e.text }));
			logmsg(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.WILL.ASSIGN.A.VALUE.OF.NULL.TO.ATTRIBUTE"));
			entry.setAttribute(ATTR_DATE_MODIFIED, null);
		}

		// set change type
		entry.setAttribute(ATTR_CHANGE_TYPE, getDocChangeType(aDoc));
		if (entry.getString(ATTR_CHANGE_TYPE).equals(CHANGE_TYPE_CREATED)) {
			entry.setOp(Entry.OP_ADD);
		}
		if (entry.getString(ATTR_CHANGE_TYPE).equals(CHANGE_TYPE_MODIFIED)) {
			entry.setOp(Entry.OP_MOD);
		}

		// add document items
		Vector itemList = aDoc.getItems();
		if (itemList != null) {
			for (int i = 0; i < itemList.size(); i++) {
				Item item = (Item) itemList.get(i);
				addItemToEntry(entry, item);
			}
		}

		return entry;
	}

	/**
	 * Adds a Notes item to an Entry as an Attribute object.
	 *
	 * @param aEntry
	 *            Entry to add to
	 * @param aItem
	 *            Notes Item
	 * @throws Exception
	 *             if an error occurs
	 */
	private void addItemToEntry(Entry aEntry, Item aItem) throws Exception {
		if (aItem == null) {
			return;
		}

		if (aItem.getType() == Item.ATTACHMENT) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DOMINOCHGDETECT.ADD.ITEM.TO.ENTRY", aItem
								.getName()));
			}
		} else {
			Vector itemValues = null;
			try {
				itemValues = aItem.getValues();
			} catch (NotesException e) {
				if (e.id == NotesError.NOTES_ERR_INVALID_OBJECT) {// Notes API
					// could not
					// recognize
					// the item
					// value
					logmsg(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.RETRIEVE.ITEM.VALUES",
									new Object[] { String.valueOf(e.id), e.text }));

					return;
				} else {
					logmsg(sResHash
							.getString(
									"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
					logmsg(sResHash.getString(
							"CONNECTOR.DOMINOCHGDETECT.ITEM.TYPE.NOT.HANDLED",
							new Object[] { aItem.getName(),
									Integer.valueOf(aItem.getType()) }));
					return;
				}
			}

			Attribute attribute = aEntry.getAttribute(aItem.getName());
			if (attribute == null) {
				attribute = new Attribute(aItem.getName());
				aEntry.setAttribute(attribute);

				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.DOMINOCHGDETECT.NEW.ATTRIBUTE",
							attribute.getName()));

					debug(sResHash.getString(
							"CONNECTOR.DOMINOCHGDETECT.ITEM.TYPE", String
									.valueOf(aItem.getType())));
				}
			}

			if (itemValues != null) {
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.DOMINOCHGDETECT.THE.DATA.IS", itemValues
									.toString()));
				}

				for (int i = 0; i < itemValues.size(); i++) {
					Object itemValue = itemValues.get(i);

					// convert Domino dates to Java dates
					if (itemValue instanceof lotus.domino.DateTime) {
						try {
							itemValue = ((lotus.domino.DateTime) itemValue)
									.toJavaDate();
						} catch (NotesException e) {
							logmsg(sResHash
									.getString(
											"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.CONVERT.NOTES.DATE.TO.JAVA.DATE",
											new Object[] {
													String.valueOf(e.id),
													e.text }));
							logmsg(sResHash
									.getString(
											"CONNECTOR.DOMINOCHGDETECT.DATE.VALUE.FOR.ATTRIBUTE",
											attribute.getName()));
							continue;
						}
					}
					attribute.addValue(itemValue);
				}
			}
		}
	}

	/**
	 * Determines whether a non-deleted document is newly added or modified.
	 *
	 * @param aDoc
	 *            Document
	 * @return change type
	 * @throws NotesException
	 *             if an error occurs
	 */
	private String getDocChangeType(Document aDoc) throws NotesException {
		String changeType = null;

		try {
			changeType = CHANGE_TYPE_CREATED;
			if (mStartDate != null) {
				if(aDoc.getCreated() ==null)  {                //Added Null Check
									logmsg(sResHash.getString(
										"CONNECTOR.DOMINOCHGDETECT.WILL.ASSIGN.CHANGE.TYPE",
										new Object[] { aDoc.getUniversalID() }));
									changeType = CHANGE_TYPE_UNKNOWN;
								} else {
									Date dateCreated = aDoc.getCreated().toJavaDate();
									if (dateCreated.compareTo(mStartDate) < 0) {
										changeType = CHANGE_TYPE_MODIFIED;
									}
								}
							}
		} catch (NotesException e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.DETERMINE.CHANGE.TYPE.FOR.DOCUMENT.WITH.UNID",
							new Object[] { aDoc.getUniversalID(),
									String.valueOf(e.id), e.text }));
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.WILL.ASSIGN.CHANGE.TYPE",
					new Object[] { aDoc.getUniversalID() }));
			changeType = CHANGE_TYPE_UNKNOWN;
		}

		return changeType;
	}

	/**
	 * Retrieves the UnIDs of all changed Domino documents.
	 *
	 * @return The number of retrieved changed documents.
	 * @throws Exception
	 *             if an error occurs
	 */
	private int retrieveChangedUnIDs() throws Exception {
		String startDate = null;
		if (mStartDate != null) {
			startDate = dateToString(mStartDate);
		}
		StringBuffer endDate = new StringBuffer();

		mModifiedNotesUnIDs = safeGetModifiedNotes(startDate, endDate);
		if (mModifiedNotesUnIDs == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.NATIVE.CALL.ERROR"));
		}

		mEndDate = stringToDate(endDate.toString());
		if (mEndDate == null) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.COULD.NOT.PARSE.END.DATE.VALUE",
					endDate));
		}

		mCurrentUnIDIdx = 0;

		return mModifiedNotesUnIDs.size();
	}

	/**
	 * Given a string representation of a date, builds a {@link java.util.Date}
	 * object.
	 *
	 * @param aDate
	 *            Date in String representation
	 * @return Date object
	 * @throws Exception
	 *             if format is not recognized
	 */
	private Date stringToDate(String aDate) throws Exception {
		SimpleDateFormat dateFormat = null;
		String dateStr = aDate.trim();

		if (dateStr.length() == DATE_FORMAT_FULL.length()) {
			dateFormat = mDateFormatFull;
		} else if (dateStr.length() == DATE_FORMAT_LONG.length()) {
			dateFormat = mDateFormatLong;
		} else if (dateStr.length() == DATE_FORMAT_SHORT.length()) {
			dateFormat = mDateFormatShort;
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.STRING.TO.DATE", dateStr));
		}

		return dateFormat.parse(dateStr);
	}

	/**
	 * Returns the string representation of the given {@link java.util.Date}
	 * object.
	 *
	 * @param aDate
	 *            {@link Date} object
	 * @return string representation
	 */
	private String dateToString(Date aDate) {
		return mDateFormatFull.format(aDate);
	}

	// **************************************************************
	// Comparator implementation
	// **************************************************************

	/**
	 * Compares two documents by the dates they are modified
	 *
	 * @param arg0
	 *            the unID of the first document
	 * @param arg1
	 *            the unID of the second document
	 * @return <code>-1</code> if the first document was modified before the
	 *         second; <code>1</code> if the first document was modified afrer
	 *         the second; <code>0</code> if the time of modification was the
	 *         same
	 */
	public int compare(Object arg0, Object arg1) {

		String unID0 = arg0.toString();
		String unID1 = arg1.toString();
		if (unID0.endsWith(DOC_STATE_DELETED)) {
			if (unID1.endsWith(DOC_STATE_DELETED)) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if (unID1.endsWith(DOC_STATE_DELETED)) {
				return -1;
			}
		}

		if (unID0.indexOf(PROTOCOL_DELIMITER) > -1) {
			unID0 = unID0.substring(0, unID0.indexOf(PROTOCOL_DELIMITER));
		}
		if (unID1.indexOf(PROTOCOL_DELIMITER) > -1) {
			unID1 = unID1.substring(0, unID1.indexOf(PROTOCOL_DELIMITER));
		}
		Date dateModifiedDoc0 = datesHashMap.get(unID0);
		Date dateModifiedDoc1 = datesHashMap.get(unID1);

		try {
			if (dateModifiedDoc0 == null) {
				Document doc0 = mDB.getDocumentByUNID(unID0);
				dateModifiedDoc0 = doc0.getLastModified().toJavaDate();
				datesHashMap.put(unID0, dateModifiedDoc0);
				doc0.recycle();
			}
			if (dateModifiedDoc1 == null) {
				Document doc1 = mDB.getDocumentByUNID(unID1);
				dateModifiedDoc1 = doc1.getLastModified().toJavaDate();
				datesHashMap.put(unID1, dateModifiedDoc1);
				doc1.recycle();
			}
		} catch (NotesException e) {
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOCHGDETECT.NOTESEXCEPTION.ID.AND.TEXT",
					new Object[] { Integer.valueOf(e.id), e.text }));
			return 0;
		}

		if (dateModifiedDoc0.before(dateModifiedDoc1)) {
			return -1;
		} else if (dateModifiedDoc0.after(dateModifiedDoc1)) {
			return 1;
		} else {
			return 0;
		}
	}

	// **************************************************************
	// routines for processing C layer documents info
	// **************************************************************

	/**
	 * Returns the UnID of the current document from the document info returned
	 * by the native layer.
	 *
	 * @return UnID
	 * @throws Exception
	 *             if an error occurs
	 */
	private String getCurrentDocumentUnID() throws Exception {
		if (mModifiedNotesUnIDs == null
				|| mModifiedNotesUnIDs.size() <= mCurrentUnIDIdx) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.NODOCUMENTS1.EXCEP"));
		}

		// protocol: <UnID>:<NoteID>:<DEL|NOR>
		String documentData = mModifiedNotesUnIDs.get(mCurrentUnIDIdx)
				.toString();
		return documentData.substring(0, documentData
				.indexOf(PROTOCOL_DELIMITER));
	}

	/**
	 * Returns the Note ID of the current document from the document info
	 * returned by the native layer.
	 *
	 * @return Note ID
	 * @throws Exception
	 *             if an error occurs
	 */
	private String getCurrentDocumentNoteID() throws Exception {
		if (mModifiedNotesUnIDs == null
				|| mModifiedNotesUnIDs.size() <= mCurrentUnIDIdx) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.NODOCUMENTS2.EXCEP"));
		}

		// protocol: <UnID>:<NoteID>:<DEL|NOR>
		String documentData = mModifiedNotesUnIDs.get(mCurrentUnIDIdx)
				.toString();
		return documentData.substring(
				documentData.indexOf(PROTOCOL_DELIMITER) + 1, documentData
						.lastIndexOf(PROTOCOL_DELIMITER));
	}

	/**
	 * Returns the state (normal or deleted) of the current document from the
	 * document info returned by the native layer.
	 *
	 * @return document state
	 * @throws Exception
	 *             if an error occurs
	 */
	private String getCurrentDocumentState() throws Exception {
		if (mModifiedNotesUnIDs == null
				|| mModifiedNotesUnIDs.size() <= mCurrentUnIDIdx) {
			throw new Exception(sResHash
					.getString("CONNECTOR.DOMINOCHGDETECT.NODOCUMENTS3.EXCEP"));
		}

		// protocol: <UnID>:<NoteID>:<DEL|NOR>
		String documentData = mModifiedNotesUnIDs.get(mCurrentUnIDIdx)
				.toString();
		return documentData.substring(documentData
				.lastIndexOf(PROTOCOL_DELIMITER) + 1);
	}

	/**
	 * Version information.
	 *
	 * @return the version of the Connector.
	 */
	public String getVersion() {
		return "2.1-di7.1.1 2016-12-07";
	}

	/**
	 * {@inheritDoc}
	 */
	public int getStateKeySaveMethod() throws Exception {
		return mStateKeySaveMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveStateKey() throws Exception {
		if (!mAfterRead && mPropertyStore != null) {
			if (mStateKeySaveMethod == ChangelogInterface.SAVE_STATE_END_OF_CYCLE) {
				if (currentDocUNID == null)
					return; // Nothing to save
				synchronized (currentDocUNID) {
					mPropertyStore.updateProperty(currentDocUNID, currentDocEndDate, true);
				}
				storeSynchronizationState(true);
			} else {
				if (notesDocs == null)
					return; // Nothing to save
				synchronized (notesDocs) {
					Enumeration<String> keys = notesDocs.keys();
					while (keys.hasMoreElements()) {
						String docUNID = keys.nextElement();
						Object docEndDate = notesDocs.get(docUNID);
						mPropertyStore.updateProperty(docUNID, docEndDate, true);
					}
					storeSynchronizationState(true);
					notesDocs.clear();
				}
			}
		}
	}

	/**
	 * Retrieves synchronization state.
	 *
	 * @return the synchronization state into an Entry object.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Object getStateKeyObject() throws Exception {
		return packSynchronizationState(true);
	}
}
