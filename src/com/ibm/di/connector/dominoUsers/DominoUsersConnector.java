/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dominoUsers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Vector;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;

/**
 * The Domino Users Connector enables access to and management of Lotus Domino users.
 *
 * With the Domino Users Connector you can do the following: 
 * - retrieve users documents and their items from the Name and Address Book
 * - create and register Domino users
 * - initiate Domino users deletion (through the Domino Administration Process)
 *  by posting administration requests to the Administration Requests Database
 *  - modify users by modifying their Person documents in the Name and Address Book
 *  - perform users disabling/enabling by adding/removing users names to/from a "Deny Access Group"
 *  - perform "lookup" of Domino users
 *
 * The following features are not currently supported by the Domino Users Connector:
 * - Users recertifying
 *
 */
public class DominoUsersConnector extends Connector implements Runnable,
		ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Corresponds to the Form Item of the Domino document. This item is
	 * mandatory
	 */
	public static final String ATTR_NAME_FORM = "Form";

	/**
	 * Corresponds to the Type Item of the Domino document. This item is
	 * mandatory
	 */
	public static final String ATTR_NAME_TYPE = "Type";

	/** This field corresponds to the FirstName Item of the Domino document. */
	public static final String ATTR_NAME_FIRST_NAME = "FirstName";

	/** This field corresponds to the MiddleInitial Item of the Domino document. */
	public static final String ATTR_NAME_MIDDLE_INITIAL = "MiddleInitial";

	/** This field corresponds to the LastName Item of the Domino document. */
	public static final String ATTR_NAME_LAST_NAME = "LastName";

	/** This field corresponds to the FullName Item of the Domino document. */
	public static final String ATTR_NAME_FULL_NAME = "FullName";

	/** This field corresponds to the MailFile Item of the Domino document. */
	public static final String ATTR_NAME_MAIL_FILE = "MailFile";

	/** This field corresponds to the Location Item of the Domino document. */
	public static final String ATTR_NAME_LOCATION = "Location";

	/** This field corresponds to the Comment Item of the Domino document. */
	public static final String ATTR_NAME_COMMENT = "Comment";

	/** This field corresponds to the AltFullName Item of the Domino document. */
	public static final String ATTR_NAME_ALT_FULL_NAME = "AltFullName";

	/**
	 * This field corresponds to the AltFullNameLanguage Item of the Domino
	 * document.
	 */
	public static final String ATTR_NAME_ALT_FULL_NAME_LANGUAGE = "AltFullNameLanguage";

	/** This field corresponds to the HTTPPassword Item of the Domino document. */
	public static final String ATTR_NAME_HTTP_PASSWORD = "HTTPPassword";

	/**
	 * The name of the server containing the user's mail file. If the Attribute
	 * is missing, the value will be obtained from the current Connector's
	 * Domino Session. When the Connector is running on a Notes client machine
	 * and is registering a user, this Attribute must be specified in order to
	 * create a mail file on the server for the newly registered user.
	 */
	public static final String ATTR_NAME_REG_SERVER = "REG_Server";

	/**
	 * If set to true - creates a mail database.
	 * If set to false - does not create a mail database; it will be created during setup.
	 * If this Attribute is missing, a default value of false will be assumed.
	 */
	public static final String ATTR_NAME_REG_CREATE_MAIL_DB = "REG_CreateMailDb";

	/**
	 * If set to true the Connector will perform user registration.
	 * If this Attribute is missing, or its value is false, the Connector will not
	 * perform user registration, regardless of the presence and the values of
	 * the other REG_ Attributes.
	 */
	public static final String ATTR_NAME_REG_PERFORM = "REG_Perform";

	/**
	 * The filename of a Notes template database, which the Connector will use
	 * to create the user mail file. If this Attribute does not exist the
	 * default mail template is used.
	 */
	public static final String ATTR_NAME_MAIL_TEMPLATE_FILE = "REG_MailTemplateFile";

	/**
	 * If set to true - the user mail database to be created will inherit any
	 * changes to the mail template database design.
	 * If set to false - the user mail database to be created will not inherit any changes to the mail
	 * template database design.
	 * If this Attribute is missing, a default value of false will be assumed.
	 */
	public static final String ATTR_NAME_MAIL_DB_INHERIT = "REG_MailDbInherit";

	/**
	 * The IP address of the Domino server machine on which the mail template
	 * database (specified by REG_MailTemplateFile) resides. If this Attribute
	 * does not exist the local Domino server machine is used.
	 */
	public static final String ATTR_NAME_MAIL_TEMPLATE_SERVER = "REG_MailTemplateServer";

	/**
	 * true if the user does not belong to a "Deny List only" group;
	 * false if the user belongs to at least one group of type "Deny List only".
	 */
	public static final String ATTR_NAME_DER_IS_ENABLED = "DER_IsEnabled";

	/**
	 * The value for the Type and Form Document items
	 */
	private static final String ATTR_VALUE_PERSON = "Person";

	/** This field corresponds to the NoteID Item of the Domino document. */
	public static final String PROP_NOTE_ID = "NoteID";

	/** This field corresponds to the UniversalID Item of the Domino document. */
	public static final String PROP_UNIVERSAL_ID = "UniversalID";

	/** This field corresponds to the Authors Item of the Domino document. */
	public static final String PROP_AUTHORS = "Authors";

	/** This field corresponds to the IsValid Item of the Domino document. */
	public static final String PROP_IS_VALID = "IsValid";

	/** This field corresponds to the IsSigned Item of the Domino document. */
	public static final String PROP_IS_SIGNED = "IsSigned";

	/** This field corresponds to the Verifier Item of the Domino document. */
	public static final String PROP_VERIFIER = "Verifier";

	/** User's Internet name for login on the Domino Server */
	private final static String PARAM_USER_NAME = "userName";

	/**
	 * Either the Notes ID file password or the user's Internet password,
	 * depending on the Authentication Method setting
	 */
	private final static String PARAM_PASSWORD = "password";

	/**
	 * Authentication mechanism. This parameter is used before TDI 7.0 In TDI
	 * 7.0 it is mapped to the Session Type parameter
	 */
	private final static String PARAM_AUTH_MECHANISM = "authMechanism";

	/** The type of session to use */
	private final static String PARAM_USE_SESSION_TYPE = "dominoSessionType";

	/**
	 * IP address (or hostname) of the Domino Server where the 'Name and Address
	 * Book' Database is located
	 */
	private final static String PARAM_DOMINO_SERVER = "dominoServer";

	/** The IOR string used to create the IIOP session */
	private static final String PARAM_IOR = "ior";

	/** The port on which the HTTP task of the Domino Server is running */
	private static final String PARAM_DOMINO_HTTP_PORT = "httpPort";

	/** The name of the 'Name and Address Book' Database; usually 'names.nsf' */
	private final static String PARAM_NAB_DATABASE = "nabDatabase";

	/** Enables encrypted communications with the Domino server */
	private final static String PARAM_USE_SSL = "useSSL";

	/**
	 * Full-text query that will filter the users in Iterator mode. Leave blank
	 * if no filtering is needed. This parameter's value is taken into account
	 * only when "useFTSearch" is "true"
	 */
	private final static String PARAM_FULL_TEXT_FILTER = "fullTextFilter";

	/**
	 * Lotus formula that will filter the users in Iterator mode. The Connector
	 * will automatically append (Form = "Person") to the formula, so the filter
	 * will be applied to user documents only. This parameter's value is taken
	 * into account only when "useFTSearch" is "false"
	 */
	private final static String PARAM_FORMULA_FILTER = "formulaFilter";

	/**
	 * If checked the Iterator and Lookup Connector modes will use full-text
	 * search in view; otherwise regular database search is performed
	 */
	private final static String PARAM_USE_FT_SEARCH = "useFTSearch";

	/**
	 * Constant for an authentication mechanism type used prior to 7.0 In
	 * 7.0 this is mapped to Local Server session
	 */
	private static final String PARAM_VAL_AUTH_INET_PASSWD = "Internet Password";

	/**
	 * Constant for an authentication mechanism type used prior to 7.0 In
	 * 7.0 this is mapped to Local Server session
	 */
	private static final String PARAM_VAL_AUTH_NOTES_ID_FILE = "Notes ID File";

	/** Constant for Local Client session */
	private static final String PARAM_VAL_SESSION_LOCAL_CLIENT = "LocalClient";

	/** Constant for Local Server session */
	private static final String PARAM_VAL_SESSION_LOCAL_SERVER = "LocalServer";

	/** Constant for IIOP session */
	private static final String PARAM_VAL_SESSION_IIOP = "IIOP";

	/** Constant for default 'Name and Address Book' Database */
	protected static final String NAB_DEFAULT_VALUE = "names.nsf";

	/** Constant for default 'Administration' Database */
	protected static final String ADMIN_DATABASE_NAME = "admin4.nsf";

	/** Constant for default view */
	protected static final String VIEW_PEOPLE = "People";

	/** Constant for default formula */
	protected static final String FORMULA_FORM_PERSON = "Form = \"Person\"";

	/** The name of the Connector */
	private static final String mConnectorName = "Domino Users Connector";

	/** Member variable for the userName configuration parameter */
	private String mUserName = null;

	/** Member variable for the password configuration parameter */
	private String mPassword = null;

	/** Member variable for the authMechanism configuration parameter */
	private String mAuthMechanismStr = null;

	/** Member variable for the dominoSessionType configuration parameter */
	private String mSessionType = null;

	/** Member variable for the dominoServer configuration parameter */
	private String mDominoServer = null;

	/** Member variable for the httpPort configuration parameter */
	private int mHTTPPort = 80;

	/** Member variable for the ior configuration parameter */
	private String mIOR = null;

	/** Member variable for the nabDatabase configuration parameter */
	private String mNabDatabase = null;

	/** Member variable for the useSSL configuration parameter */
	private String mUseSSL = null;

	/** Member variable for the useFTSearch configuration parameter */
	private boolean mUseFTSearch = false;

	/** Member variable for the fullTextFilter configuration parameter */
	private String mFullTextFilter = null;

	/** Member variable for the formulaFilter configuration parameter */
	private String mFormulaFilter = null;

	/** This variable is used for session creation */
	private Session mSession = null;

	/** This variable is used to specify database during session creation */
	private Database mDatabase = null;

	/**
	 * This variable is used to specify the administration database during
	 * session creation
	 */
	private Database mAdminDatabase = null;

	/** This variable is used for filtering on a view when selecting entries */
	private View mPeopleView = null;

	/** Entry Collection used to hold the results of Full Text search */
	private ViewEntryCollection mEntryCollection = null;

	/**
	 * Document Collection used to hold the results when no Full Text search is
	 * performed
	 */
	private DocumentCollection mDocumentCollection = null;

	/** Instance to perform special Domino Access Management actions */
	private UserAccess mUserAccess = null;

	/** Instance to perform special Domino User Registration actions */
	private UserRegistration mUserRegistration = null;

	/** Instance to perform special Domino User Deletion actions */
	private UserDeletion mUserDeletion = null;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int NO_COMMAND = 0;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_INITIALIZE = 1;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_SELECT_ENTRIES = 2;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_GET_NEXT_ENTRY = 3;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_FIND_ENTRY = 4;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_MOD_ENTRY = 5;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_PUT_ENTRY = 6;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_DELETE_ENTRY = 7;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_TERMINATE = 8;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_SET_DELETE_GROUP_NAME = 9;

	/** Constant for the inner notes thread specifying which command to execute */
	private static final int COMMAND_SET_DELETE_MAIL = 10;

	/**
	 * Inner notes thread. It is used when establishing a session to the Domino
	 * database and all Connector calls are redirected to be executed by this
	 * thread. This solves a Domino problem with multiple initializations
	 */
	private Thread mNotesThread = null;

	/** The command which the local thread will execute */
	private int mNotesThreadCommand = NO_COMMAND;

	/** Waits for Notes thread to finish command execution */
	private boolean mWaitForNotesThread = false;

	/** Arguments that are passed on thread execution */
	private Object[] mNotesThreadCallArgList = null;

	/** The return value of the thread's execution */
	private Object mNotesThreadCallReturnValue = null;

	/** An error that possibly occurs during thread execution */
	private Throwable mNotesThreadCallError = null;

	/**
	 * Specifies the name of the property file where configuration parameters
	 * are described and localized
	 */
	private static final String PROPERTIES_FILE = "dominousersconnector";

	/**
	 * ResourceHash used to access the TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Getter for the ResourceHash of the TMS messages
	 *
	 * @return the ResourceHash
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/** A local thread that executes the commands through the Notes thread */
	private Object localThread = null;

	/**
	 * Constructor Creates the connector and sets the AddOnly, Delete, Iterator,
	 * Lookup and Update modes
	 */
	public DominoUsersConnector() {
		setName(mConnectorName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
	}

	// *************************************************************************
	// routines for the inner Notes thread
	// *************************************************************************

	/**
	 * This call is needed when the connector makes local calls through a client
	 * or server. The local calls (lotus.domino.local.*) are available only in
	 * the Notes.jar which may not be in the installation. Thus, use reflection
	 * to load and call sinitThread/stermThread. This method is called from the
	 * initialize method when local session is made.
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void initLocalThread() throws Exception {
		// Use reflection to avoid runtime class loading problems
		Object nt;

		try {
			nt = Class.forName("lotus.domino.NotesThread").newInstance();
		} catch (ClassNotFoundException cnf) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.THREADCLASS.EXCEPTION", cnf));
		}

		Method m = nt.getClass().getDeclaredMethod("sinitThread",
				new Class[] {});
		m.invoke(nt, new Object[0]);

		localThread = nt;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.THREADSTART.INFO"));
		}
	}

	/**
	 * This method terminates the Notes API thread to make sure resources are
	 * cleaned up properly. This method is usually called from the terminate
	 * method. It is not recommended to use it directly.
	 */
	public void termLocalThread() {
		if (localThread == null) {
			return;
		}

		try {
			Method m = localThread.getClass().getDeclaredMethod("stermThread",
					new Class[] {});
			m.invoke(localThread, new Object[0]);
		} catch (Exception err) {
			logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.STOPTHREAD.WARNING", err
					.toString()));
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.THREADTERMINATE.INFO"));
		}
		localThread = null;
	}

	/**
	 * Accepts commands from the command line until termination is requested.
	 */
	public synchronized void run() {
		try {
			boolean terminate = false;
			while (!terminate) {
				try {
					switch (mNotesThreadCommand) {
					case COMMAND_INITIALIZE:
						inner_initialize(mNotesThreadCallArgList[0]);
						break;

					case COMMAND_SELECT_ENTRIES:
						inner_selectEntries();
						break;

					case COMMAND_GET_NEXT_ENTRY:
						mNotesThreadCallReturnValue = inner_getNextEntry();
						break;

					case COMMAND_FIND_ENTRY:
						mNotesThreadCallReturnValue = inner_findEntry((SearchCriteria) mNotesThreadCallArgList[0]);
						break;

					case COMMAND_MOD_ENTRY:
						inner_modEntry((Entry) mNotesThreadCallArgList[0],
								(SearchCriteria) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_PUT_ENTRY:
						inner_putEntry((Entry) mNotesThreadCallArgList[0]);
						break;

					case COMMAND_DELETE_ENTRY:
						inner_deleteEntry((Entry) mNotesThreadCallArgList[0],
								(SearchCriteria) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_TERMINATE:
						terminate = true;
						inner_terminate();
						break;

					case COMMAND_SET_DELETE_GROUP_NAME:
						inner_setDeleteGroupName((String) mNotesThreadCallArgList[0]);
						break;

					case COMMAND_SET_DELETE_MAIL:
						inner_setDeleteMailFile(((Integer) mNotesThreadCallArgList[0])
								.intValue());
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
	 * Sets the mNotesThreadCommand to the command that needs to be executed.
	 * The thread started during initialization of the connector in its run()
	 * method constantly checks for this variable's value and when a change is
	 * detected the corresponding inner method is invoked.
	 *
	 * If another command is currently executing this command waits until
	 * notified and the mWaitForNotesThread is set to false.
	 *
	 * @param aCommand
	 *            the command to be executed
	 * @param aArgList
	 *            the arguments of the command
	 * @param aHasReturnValue
	 *            the return value of the command
	 * @return the mNotesThreadCallReturnValue of the execution
	 * @throws Exception
	 */
	private synchronized Object executeCommand(int aCommand, Object[] aArgList,
			boolean aHasReturnValue) throws Exception {
		if (!isNotesThreadAlive()) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.CONNECTOR.NOTES.THREAD.NOT.ALIVE"));
		}

		// if a command is set by another thread, wait for the Notes thread to
		// process it
		while (mWaitForNotesThread) {
			try {
				wait();
			} catch (InterruptedException e) {
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
				if (callError instanceof NotesException) {
					throw new Exception(sResHash.getString(
							"CONNECTOR.DOMINOUSERSCONN.NOTESEXCEPTION",
							((NotesException) callError).text));
				} else {
					throw (Exception) callError;
				}
			} else {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.FATAL.ERROR", callError
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
	 * Checks if the local notes thread is alive
	 *
	 * @return true if thread is alive
	 */
	private boolean isNotesThreadAlive() {
		return (mNotesThread != null && mNotesThread.isAlive());
	}

	// *************************************************************************
	// ConnectorInterface implementation
	// *************************************************************************
	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object aObject) throws Exception {
		if (isNotesThreadAlive()) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.CONNECTOR.ALREADY.INITIALIZED"));
		}

		mNotesThread = new Thread(this);
		mNotesThreadCommand = NO_COMMAND;
		mNotesThread.start();

		executeCommand(COMMAND_INITIALIZE, new Object[] { aObject }, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
		executeCommand(COMMAND_SELECT_ENTRIES, null, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		return (Entry) executeCommand(COMMAND_GET_NEXT_ENTRY, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria aSearch) throws Exception {
		return (Entry) executeCommand(COMMAND_FIND_ENTRY,
				new Object[] { aSearch }, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry aEntry, SearchCriteria aSearch) throws Exception {
		executeCommand(COMMAND_MOD_ENTRY, new Object[] { aEntry, aSearch },
				false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry aEntry) throws Exception {
		executeCommand(COMMAND_PUT_ENTRY, new Object[] { aEntry }, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteEntry(Entry aEntry, SearchCriteria aSearch)
			throws Exception {
		executeCommand(COMMAND_DELETE_ENTRY, new Object[] { aEntry, aSearch },
				false);
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {
		try {
			executeCommand(COMMAND_TERMINATE, null, false);
		} catch (Exception e) {
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.ERROR.WHILE.TERMINATING", e
							.toString()));
		}
	}

	/**
	 * Version information
	 *
	 * @return version information.
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I% 20%E%";
	}

	// *************************************************************************
	// inner methods corresponding to the ConnectorInterface methods
	// *************************************************************************

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @param aObject
	 *            User provided parameter
	 * @throws Exception
	 *             if the initialization of this connector fails.
	 */
	private void inner_initialize(Object aObject) throws Exception {
		// get Connector parameters
		mUserName = getParam(PARAM_USER_NAME);
		mPassword = getParam(PARAM_PASSWORD);
		// This points to the remote server we are going to read from; For the IIOP session this is also the remote server we are establishing a connection to. Introduced in defect 13095
		mDominoServer = getParam(PARAM_DOMINO_SERVER);
		if (mDominoServer != null && mDominoServer.trim().length() == 0) {
			mDominoServer = null;
		}

		String mHTTPTaskPort = getParam(PARAM_DOMINO_HTTP_PORT);

		if (mHTTPTaskPort != null && (mHTTPTaskPort.trim().length() != 0)) {
			try {
				mHTTPPort = Integer.parseInt(mHTTPTaskPort);
			} catch (NumberFormatException e) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.UNPARSEABLE", mHTTPTaskPort));
			}
		}

		mIOR = getParam(PARAM_IOR);

		mNabDatabase = getParam(PARAM_NAB_DATABASE);
		if (mNabDatabase == null || mNabDatabase.trim().length() == 0) {
			mNabDatabase = NAB_DEFAULT_VALUE;
		}

		mSessionType = getParam(PARAM_USE_SESSION_TYPE);

		// For backward compatibility (since authMechanism became
		// dominoSessionType)
		// when using tdi server, a mapping is necessary between the old labels
		// of authMechanism
		// and the new of dominoSessionType.

		// Determines if dominoSessionType parameter is defined in the
		// configuration.
		boolean isDominoSessionTypeUsed = ((ConnectorConfig) getConfiguration())
				.getConnectionConfig().isParameterLocal("dominoSessionType");

		// If dominoSessionType is not defined and authMechanism (old version
		// parameter)
		// exists, the mapping is done.
		mAuthMechanismStr = getParam(PARAM_AUTH_MECHANISM);
		if (!isDominoSessionTypeUsed && mAuthMechanismStr != null) {
			if (PARAM_VAL_AUTH_NOTES_ID_FILE.equals(mAuthMechanismStr)) {
				// maps "Notes ID File" with "LocalClient"
				mSessionType = PARAM_VAL_SESSION_LOCAL_CLIENT;
			} else if (PARAM_VAL_AUTH_INET_PASSWD.equals(mAuthMechanismStr)) {
				// maps "Internet Password" with "LocalServer"
				mSessionType = PARAM_VAL_SESSION_LOCAL_SERVER;
			}
		}

		if (mSessionType == null) {
			mSessionType = "IIOP";
		}

		// Access via local notes client requires null value for host and user
		if (mSessionType.equalsIgnoreCase(PARAM_VAL_SESSION_LOCAL_CLIENT)) {
			// A connection to the Local Client will be created. Nevertheless we need the value of this variable because the queries will be send to that remote server and not to the local client. Introduced in defect 13095
			// mDominoServer = null;
			mUserName = null;
			if (mPassword != null && mPassword.equals("")) {
				mPassword = null;
			}
			initLocalThread();
		}

		// Access to local domino server requires null value for host and
		// non-null for user and pass
		if (mSessionType.equalsIgnoreCase(PARAM_VAL_SESSION_LOCAL_SERVER)) {
		// Setting the DominoServer to null tell the Notes API to query the local instance (either client or server) and not to a remote server.
			mDominoServer = null;
			if (mUserName == null) {
				mUserName = "";
			}
			if (mPassword == null) {
				mPassword = "";
			}
			initLocalThread();
		}

		String args[] = null;
		String iorArgs[] = null;

		mUseSSL = getParam(PARAM_USE_SSL);
		boolean isSslEnabled = false;
		if (mUseSSL != null && mUseSSL.equalsIgnoreCase("true")) {
			isSslEnabled = true;
			args = new String[1];
			args[0] = "-ORBEnableSSLSecurity";

			iorArgs = new String[1];
			iorArgs[0] = "-HTTPEnableSSLSecurity";
		}

		mUseFTSearch = Boolean.valueOf(getParam(PARAM_USE_FT_SEARCH))
				.booleanValue();
		if (debugMode()) {
			if (mUseFTSearch) {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.INITIALIZE.WILL.USE.FULL.TEXT.SEARCH"));
			} else {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.INITIALIZE.WILL.USE.REGULAR.DATABASE.SEARCH"));
			}
		}

		mFullTextFilter = getParam(PARAM_FULL_TEXT_FILTER);
		if (mFullTextFilter != null && mFullTextFilter.trim().length() == 0) {
			mFullTextFilter = null;
		}

		mFormulaFilter = getParam(PARAM_FORMULA_FILTER);
		if (mFormulaFilter != null && mFormulaFilter.trim().length() == 0) {
			mFormulaFilter = null;
		}

		logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.SESSION.INFO",
				new Object[] { mSessionType, mDominoServer, mUserName,
						"" + (args != null) }));

		try {
			if (mSessionType.equalsIgnoreCase(PARAM_VAL_SESSION_IIOP)) {
					if (mIOR != null && mIOR.startsWith("IOR:")) {
						logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.IOR.INFO"));
						mSession = NotesFactory.createSessionWithIOR(mIOR, args, mUserName, mPassword);
						if (isSslEnabled) {
							logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.IOR.USED.SSL.ON.SESSION.CREATED"));
						} else {
							logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.IOR.USED.SSL.OFF.SESSION.CREATED"));
						}
					} else {
						// We explicitly get the IOR by providing username and
						// password.
						// This way the IOR can be obtained even if Domino's
						// HTTP task does not permit anonymous connections.
						mIOR = NotesFactory.getIOR(mDominoServer + ":" + mHTTPPort, iorArgs, mUserName, mPassword);
						mSession = NotesFactory.createSessionWithIOR(mIOR, args, mUserName, mPassword);
						if (isSslEnabled) {
							logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.PORT.USED.SSL.ON.SESSION.CREATED"));
						} else {
							logmsg(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.PORT.USED.SSL.OFF.SESSION.CREATED"));
						}
					}
			} else {
			//mSession = NotesFactory.createSession(mDominoServer, mUserName,mPassword); previous code commented out
				/*
				 * providing null host here means that we are connecting to
				 * whatever Domino instance (client or server) that is running
				 * on the local machine. Note: We don't provide the
				 * mDominoServer because that refers to the server which we are
				 * going to query and not the one we are going to connect to.
				 * For the Local Client case the user is also null but the
				 * password is probably not. For the Local Server case both User
				 * and password are non-null values.
				 */
				mSession = NotesFactory.createSession((String)null, mUserName, mPassword);
				logmsg(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.SESSION.CREATED"));
			}

		} catch (lotus.domino.NotesException ne) {
			logmsg(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.SESSION.EXCEPTION",
					new Object[] { ne.getClass().getName(), ne.getMessage() }));
			ne.printStackTrace();
			throw new Exception(ne.text);
		}

		// create Notes session
		// String host = null;
		// mSession = NotesFactory.createSession(host, mUserName, mPassword);
		logmsg(sResHash.getString(
				"CONNECTOR.DOMINOUSERSCONN.INITIALIZE.CONNECTED.TO",
				new Object[] { mSession.getServerName(),
						mSession.getNotesVersion(), mSession.getPlatform() }));

		// get Name and Address Book database
		mDatabase = mSession.getDatabase(mDominoServer, mNabDatabase);
		if (mDatabase == null) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.UNABLE.TO.OPEN.NAME.AND.ADDRESS.BOOK",
									mNabDatabase));
		}

		if (debugMode()) {
			debug(sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.NAME.AND.ADDRESS.BOOK.IS.OPENED",
							mNabDatabase));

			if (mDatabase.isFTIndexed()) {
				debug(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.DB.IS.FULL.TEXT.INDEXED",
						mNabDatabase));
			} else {
				debug(sResHash
						.getString(
								"CONNECTOR.DOMINOUSERSCONN.DB.IS.NOT.FULL.TEXT.INDEXED",
								mNabDatabase));
			}
		}

		// get Admin database
		mAdminDatabase = mSession.getDatabase(mDominoServer,
				ADMIN_DATABASE_NAME);
		if (mAdminDatabase == null) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.UNABLE.TO.OPEN.ADMIN.DATABASE",
					ADMIN_DATABASE_NAME));
		}

		logmsg(sResHash.getString(
				"CONNECTOR.DOMINOUSERSCONN.ADMINISTRATION.REQUESTS.DB.OPENED",
				ADMIN_DATABASE_NAME));

		// create the objects for performing special operations with Domino
		createDominoActionInstances();
	}

	/**
	 * Create the objects for performing special operations with Domino
	 *
	 * @throws Exception
	 *             If the creation fails
	 */
	private void createDominoActionInstances() throws Exception {
		// setting users enable/disable status
		mUserAccess = new UserAccess(this);

		// users registration
		mUserRegistration = new UserRegistration(this);

		// deleting users
		mUserDeletion = new UserDeletion(this);
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	private void inner_selectEntries() throws Exception {
		if (mUseFTSearch) {
			// recycle old view and associated Domino objects
			if (mEntryCollection != null) {
				mEntryCollection.recycle();
			}
			if (mPeopleView != null) {
				mPeopleView.recycle();
			}

			// get the view and select Entries
			mPeopleView = mDatabase.getView(VIEW_PEOPLE);
			if (mPeopleView == null) {
				throw new Exception(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.VIEW.NOT.FOUND"));
			}

			if (mFullTextFilter != null) {
				try {
					mPeopleView.FTSearch(mFullTextFilter);
				} catch (NotesException e) {
					throw new Exception(
							sResHash
									.getString(
											"CONNECTOR.DOMINOUSERSCONN.COULD.NOT.FILTER.DOCUMENTS",
											new Object[] {
													Integer.valueOf(e.id),
													e.text }));
				}
			}

			mEntryCollection = mPeopleView.getAllEntries();

			if (debugMode()) {
				if (mEntryCollection == null) {
					debug(sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.VIEW.SEARCH.RETURNS.NULL"));
				} else {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.VIEW.SEARCH.RETURNS.DOCUMENTS",
									Integer
											.valueOf(mEntryCollection
													.getCount())));
				}
			}
		} else {
			// recycle old document collection
			if (mDocumentCollection != null) {
				mDocumentCollection.recycle();
			}

			String formula = null;
			if (mFormulaFilter == null) {
				formula = FORMULA_FORM_PERSON;
			} else {
				formula = mFormulaFilter + " & " + FORMULA_FORM_PERSON;
			}

			try {
				mDocumentCollection = mDatabase.search(formula);
			} catch (NotesException e) {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.COULD.PERFORM.DATABASE.SEARCH",
										new Object[] { Integer.valueOf(e.id),
												e.text }));
			}

			if (debugMode()) {
				if (mDocumentCollection == null) {
					debug(sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.DB.SEARCH.RETURNS.NULL"));
				} else {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.DB.SEARCH.RETURNS.DOCUMENTS",
									Integer.valueOf(mDocumentCollection
											.getCount())));
				}
			}
		}
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @return - the next Entry, or null if no more data
	 * @throws Exception
	 *             if an error occurs.
	 */
	private Entry inner_getNextEntry() throws Exception {
		Entry entry = null;

		try {
			if (mUseFTSearch) {
				if (mEntryCollection == null) {
					return null;
				}

				ViewEntry viewEntry = mEntryCollection.getFirstEntry();
				if (viewEntry != null) {
					Document document = viewEntry.getDocument();
					if (document != null) {
						entry = buildEntry(document);
					}

					mEntryCollection.deleteEntry(viewEntry);
					viewEntry.recycle();
				}
			} else {
				if (mDocumentCollection == null) {
					return null;
				}

				Document document = mDocumentCollection.getFirstDocument();
				if (document != null) {
					try {
							entry = buildEntry(document);
						} finally {

						mDocumentCollection.deleteDocument(document);
						document.recycle();
				    }

				}
			}
		} catch (NotesException e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.ERROR.RETRIEVING.NEXT.USER.DOCUMENT",
							new Object[] { Integer.valueOf(e.id), e.text }));
			throw e;
		}

		return entry;
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @param aSearch
	 *            The search criteria used to locate the entry to be modified
	 * @return The entry found, or null if no or multiple entries found
	 * @exception Exception
	 *                if an error occurs.
	 */
	private Entry inner_findEntry(SearchCriteria aSearch) throws Exception {
		clearFindEntries();
		formatProperlyFullNameInCriteria(aSearch, mUseFTSearch);

		Entry foundEntry = null;
		int maxDup = getMaxDuplicateEntries();

		String scriptFilter = aSearch.getScriptFilter();

		if (mUseFTSearch) {
			View peopleView = mDatabase.getView(VIEW_PEOPLE);
			if (peopleView == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOUSERSCONN.FINDENTRY.VIEW.NOT.FOUND"));
			}

			try {
				String filter = null;
				if (scriptFilter == null) {
					filter = aSearch.getNotesFTFilter();
				} else {
					filter = scriptFilter;
				}

				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.FINDENTRY.FTSEARCH.VIEW",
									filter));
				}

				int foundDocumentsCnt = peopleView.FTSearch(filter);

				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.FINDENTRY.FTSEARCH.RETURNS.DOCUMENTS",
									Integer.valueOf(foundDocumentsCnt)));
				}

				if (foundDocumentsCnt == 1) {
					foundEntry = buildEntry(peopleView.getFirstDocument());
				} else {
					int docAdded = 0;
					Document document = peopleView.getFirstDocument();
					Document previousDoc = null;

					while (document != null && docAdded < maxDup) {
						addFindEntry(buildEntry(document));
						docAdded++;

						previousDoc = document;
						document = peopleView.getNextDocument(document);

						previousDoc.recycle();
					}
				}
			} finally {
				peopleView.recycle();
			}
		} else {
			String formula = null;
			if (scriptFilter == null) {
				SearchCriteria searchPlusForm = DominoUtils
						.cloneSearchCriteria(aSearch);
				searchPlusForm.addCriteria(DominoUtils.ITEM_NAME_FORM,
						SearchCriteria.EXACT, DominoUtils.ITEM_VALUE_PERSON);

				formula = DominoUtils.getNotesFormula(searchPlusForm);
			} else {
				formula = scriptFilter;

				if (formula.trim().length() == 0) {
					formula = FORMULA_FORM_PERSON;
				} else {
					formula = formula + " & " + FORMULA_FORM_PERSON;
				}
			}

			if (debugMode()) {
				debug(sResHash
						.getString(
								"CONNECTOR.DOMINOUSERSCONN.FINDENTRY.DATABASE.SEARCH.WITH.FORMULA",
								formula));
			}

			DocumentCollection dc = mDatabase.search(formula);
			if (dc == null) {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.FINDENTRY.DB.SEARCH.RETURNS.NULL"));
				}
			} else {
				try {
					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.FINDENTRY.DB.SEARCH.RETURNS.DOCUMENTS",
										Integer.valueOf(dc.getCount())));
					}
					if (dc.getCount() == 1) {
							//12933
						Document document = dc.getFirstDocument();
				                foundEntry = buildEntry(document);
						if (document != null)
							 document.recycle();

					} else {
						int docAdded = 0;
						Document document = dc.getFirstDocument();
						while (document != null && docAdded < maxDup) {
							addFindEntry(buildEntry(document));
							docAdded++;

							dc.deleteDocument(document);
							document.recycle();
							document = dc.getFirstDocument();
						}
						  if (document != null)
							document.recycle();

					}
				} finally {
					dc.recycle();
				}
			}
		}

		return foundEntry;
	}

	/**
	 * Builds an Entry Object from a Domino Document by putting all Items as
	 * attributes and also adding some additional attributes
	 *
	 * @param aDocument
	 *            the document that needs to be built to Entry
	 * @return the Entry built
	 * @throws Exception
	 *             If an error occurs
	 */
	private Entry buildEntry(Document aDocument) throws Exception {
		if (aDocument == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.BUILDENTRY.GIVEN.DOCUMENT.IS.NULL"));
			}
			return null;
		}

		Entry entry = new Entry();

		entry.setProperty(PROP_NOTE_ID, aDocument.getNoteID());
		entry.setProperty(PROP_UNIVERSAL_ID, aDocument.getUniversalID());
		entry.setProperty(PROP_AUTHORS, aDocument.getAuthors().toString());
		entry.setProperty(PROP_IS_VALID, Boolean.valueOf(aDocument.isValid()));
		entry
				.setProperty(PROP_IS_SIGNED, Boolean.valueOf(aDocument
						.isSigned()));
		entry.setProperty(PROP_VERIFIER, aDocument.getVerifier());

		Vector items = aDocument.getItems();
		if (items == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.DOMINOUSERSCONN.DOCUMENT.VECTOR.IS.NULL"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.BUILDENTRY.ALL.ITEMS", items
								.toString()));
			}

			for (int i = 0; i < items.size(); i++) {
				Item item = (Item) items.get(i);
				addItemToEntry(entry, item);
				item.recycle();
			}
		}

		// set Attribute for user's diable/enable status
		boolean isEnabled = true;
		Attribute attrFullName = entry.getAttribute(ATTR_NAME_FULL_NAME);
		if (attrFullName != null && attrFullName.getValue() != null) {
			isEnabled = !DominoUtils.isDenyGroupMember(mDatabase, attrFullName
					.getValue());
		}
		entry.addAttributeValue(ATTR_NAME_DER_IS_ENABLED, Boolean
				.valueOf(isEnabled));

		return entry;
	}

	/**
	 * Adds an Item as an attribute and value(s) to an Entry
	 *
	 * @param aEntry
	 *            the entry where the item should be added
	 * @param aItem
	 *            the Item that should be added
	 * @throws Exception
	 *             if an error occur
	 */
	private void addItemToEntry(Entry aEntry, Item aItem) throws Exception {
		if (aItem == null) {
			return;
		}

		if (aItem.getType() == Item.ATTACHMENT) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.DOMINOUSERSCONN.ADDITEMTOENTRY.ITEM", aItem
								.getName()));
			}
		} else {
			Attribute attribute = aEntry.getAttribute(aItem.getName());
			if (attribute == null) {
				attribute = new Attribute(aItem.getName());
				aEntry.setAttribute(attribute);

				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.DOMINOUSERSCONN.NEW.ATTRIBUTE",
							attribute.getName()));
					debug(sResHash.getString(
							"CONNECTOR.DOMINOUSERSCONN.ITEM.TYPE", Integer
									.valueOf(aItem.getType())));
				}
			}

			Vector<?> itemValues = null;
			try {
				itemValues = aItem.getValues();
			} catch (NotesException e) {
				byte[] b = getBytes(aItem);
				if (b != null)
					attribute.addValue(b);
				else 
					logmsg("Unable to get value for " + aItem.getName() + ", err = " + e);
				return;
			}
			
			if (itemValues != null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.DATA",
							itemValues.toString()));
				}
				for (int i = 0; i < itemValues.size(); i++) {
					Object itemValue = itemValues.get(i);

					// convert Domino dates to Java dates
					if (itemValue instanceof lotus.domino.DateTime) {
						try{
									itemValue = ((lotus.domino.DateTime) itemValue).toJavaDate();
							}catch(NotesException e)
									{
													logmsg("Invalid Date: " + e.toString());
									}
					}

					attribute.addValue(itemValue);
				}
			}
		}
	}

	private byte[] getBytes(Item item) {
		try {
			InputStream is = item.getInputStream();
			if (is == null)
				return null;

			int nRead;
			byte[] data = new byte[1024];
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
			return buffer.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @param aEntry
	 *            The entry data
	 * @param aSearch
	 *            The search criteria used to locate the entry to be modified
	 * @exception Exception
	 *                if an error occurs.
	 */
	private void inner_modEntry(Entry aEntry, SearchCriteria aSearch)
			throws Exception {
		aEntry.removeAttribute(ATTR_NAME_FORM);
		aEntry.removeAttribute(ATTR_NAME_TYPE);
		formatProperlyFullNameForUpdate(aEntry);

		Entry entryNoFixedAttributes = clearFixedDominoActionAttributes(
				mUserAccess, aEntry);
		entryNoFixedAttributes = clearFixedDominoActionAttributes(
				mUserRegistration, entryNoFixedAttributes);

		Document modDocument = findPersonDocument(aSearch);
		if (modDocument == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.MODENTRY.DOCUMENT.NOT.FOUND"));
		}

		String oldUserFullName = modDocument
				.getItemValueString(ATTR_NAME_FULL_NAME);
		updateDocument(modDocument, entryNoFixedAttributes);

		// register on modify
		if (registerUsingCustomMailTemplate(aEntry, false) != true) {
			checkExtractAndExecuteDominoAction(mUserRegistration, aEntry);
		}

		// enable/disable user
		if (mustPerformDominoAction(mUserAccess, aEntry)) {
			resetAndExtractData(mUserAccess, aEntry);

			String userFullName = null;
			if (mUserAccess.getAccessType().intValue() == UserAccess.ATTR_VALUE_SET_TYPE_DISABLE) {
				userFullName = modDocument
						.getItemValueString(ATTR_NAME_FULL_NAME);
			} else {
				userFullName = oldUserFullName;
			}

			if (userFullName == null || userFullName.length() == 0) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOUSERSCONN.MODENTRY.USER.FULLNAME.NOT.FOUND"));
			}

			mUserAccess.setUserFullName(userFullName);

			performDominoAction(mUserAccess);
		}

		modDocument.recycle();
	}

	/**
	 * Creates a mail database for the user by using a custom template
	 *
	 * @param aEntry
	 *            the entry object for the user for which a mail file will be
	 *            created
	 * @param aMailTemplateFileName
	 *            The filename of a Notes template database, which the Connector
	 *            will use to create the user mail file
	 * @param aCreateMailDBStr
	 *            specifies whether mail database should be created
	 * @param aAddOnlyMode
	 *            if true the user will be registered with the default mail
	 *            template
	 * @throws Exception
	 *             if an error occur
	 */
	private void createDbUsingCustomMailTemplate(Entry aEntry,
			String aMailTemplateFileName, String aCreateMailDBStr,
			boolean aAddOnlyMode) throws Exception {
		// Read & Validate the supplied Attributes
		String mailDbInheritStr = aEntry.getString(ATTR_NAME_MAIL_DB_INHERIT);
		boolean mailDbInherit = false;
		if (mailDbInheritStr != null) {
			mailDbInherit = Boolean.valueOf(mailDbInheritStr).booleanValue();
		}
		String mailTemplateServer = aEntry
				.getString(ATTR_NAME_MAIL_TEMPLATE_SERVER);
		if (mailTemplateServer != null) {
			mailTemplateServer = mailTemplateServer.trim();
			if (mailTemplateServer.length() == 0) {
				mailTemplateServer = null;
			}
		}
		String newMailDbFileName = aEntry.getString(ATTR_NAME_MAIL_FILE);
		if (newMailDbFileName == null || newMailDbFileName.length() == 0) {
			throw new Exception(sResHash.getString(
					"CONNECTOR.DOMINOUSERSCONN.ENTRY.ATTRIBUTE.IS.REQUIRED",
					ATTR_NAME_MAIL_FILE));
		}
		String mailFileServer = aEntry.getString(ATTR_NAME_REG_SERVER);
		if (mailFileServer != null) {
			mailFileServer = mailFileServer.trim();
			if (mailFileServer.length() == 0) {
				mailFileServer = null;
			}
		}

		String firstName = aEntry.getString(ATTR_NAME_FIRST_NAME);
		String lastName = aEntry.getString(ATTR_NAME_LAST_NAME);

		// Register User
		aEntry.setAttribute(ATTR_NAME_REG_CREATE_MAIL_DB, "false");
		if (aAddOnlyMode) {
			inner_putEntry_DefaultMailTemplate(aEntry);
		} else {
			checkExtractAndExecuteDominoAction(mUserRegistration, aEntry);
		}

		// Create Mail DB by template
		Session session = getSession();
		Database templateDb = session.getDatabase(mailTemplateServer,
				aMailTemplateFileName);
		Database newDb = templateDb.createFromTemplate(mailFileServer,
				newMailDbFileName, mailDbInherit);
		templateDb.recycle();

		// Assign ACLs
		String userName = firstName + " " + lastName;
		newDb.grantAccess(userName, ACL.LEVEL_EDITOR);
		newDb.setTitle(userName);
		newDb.recycle();

		// Assign this new MailDb to the registered user
		Entry entry = new Entry();
		entry.setAttribute(ATTR_NAME_FIRST_NAME, firstName);
		entry.setAttribute(ATTR_NAME_LAST_NAME, lastName);
		entry.setAttribute(ATTR_NAME_MAIL_FILE, newMailDbFileName);

		SearchCriteria searchCriteria = new SearchCriteria();
		searchCriteria.addCriteria(ATTR_NAME_FIRST_NAME, SearchCriteria.EXACT,
				aEntry.getString(ATTR_NAME_FIRST_NAME));
		searchCriteria.addCriteria(ATTR_NAME_LAST_NAME, SearchCriteria.EXACT,
				aEntry.getString(ATTR_NAME_LAST_NAME));
		
		if( aEntry.getString(ATTR_NAME_FULL_NAME) != null){
			searchCriteria.addCriteria(ATTR_NAME_FULL_NAME,	SearchCriteria.EXACT,aEntry.getString(ATTR_NAME_FULL_NAME));
		}					
		
		inner_modEntry(entry, searchCriteria);

		aEntry.setAttribute(ATTR_NAME_REG_CREATE_MAIL_DB, aCreateMailDBStr);
	}

	/**
	 * Register a user using custom mail template
	 *
	 * @param aEntry
	 *            the entry object for the user for which a mail file will be
	 *            created
	 * @param aAddOnlyMode
	 *            if true the user will be registered with the default mail
	 *            template
	 * @return true if the user is registered successfully
	 * @throws Exception
	 *             if an error occur
	 */
	private boolean registerUsingCustomMailTemplate(Entry aEntry,
			boolean aAddOnlyMode) throws Exception {
		String mailTemplateFileName = aEntry
				.getString(ATTR_NAME_MAIL_TEMPLATE_FILE);
		String createMailDBStr = aEntry.getString(ATTR_NAME_REG_CREATE_MAIL_DB);
		boolean createMailDB = false;
		if (createMailDBStr != null) {
			createMailDB = Boolean.valueOf(createMailDBStr).booleanValue();
		}
		String regPerformStr = aEntry.getString(ATTR_NAME_REG_PERFORM);
		boolean regPerform = false;
		if (regPerformStr != null) {
			regPerform = Boolean.valueOf(regPerformStr).booleanValue();
		}

		boolean reg = false;
		if (regPerform == true && createMailDB == true
				&& mailTemplateFileName != null
				&& mailTemplateFileName.trim().length() > 0) {
			createDbUsingCustomMailTemplate(aEntry, mailTemplateFileName,
					createMailDBStr, aAddOnlyMode);
			reg = true;
		}

		return reg;
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @param aEntry
	 *            The entry data
	 * @throws Exception
	 *             if an error occurs
	 */
	private void inner_putEntry(Entry aEntry) throws Exception {
		if (registerUsingCustomMailTemplate(aEntry, true) != true) {
			inner_putEntry_DefaultMailTemplate(aEntry);
		}
	}

	/**
	 * If it is specified that the user is not specified to be created and
	 * registered using a custom mail template this method will be called to use
	 * a default template
	 *
	 * @param aEntry
	 *            The entry data
	 * @throws Exception
	 *             if an error occurs
	 */
	private void inner_putEntry_DefaultMailTemplate(Entry aEntry)
			throws Exception {
		aEntry.setAttribute(ATTR_NAME_FORM, ATTR_VALUE_PERSON);
		aEntry.setAttribute(ATTR_NAME_TYPE, ATTR_VALUE_PERSON);
		// Commented out this, as user does not exist yet. See DI02335
		//formatProperlyFullNameForUpdate(aEntry);

		// register user
		checkExtractAndExecuteDominoAction(mUserRegistration, aEntry);

		// update or create user document
		if (mUserRegistration.mustPerform(aEntry)) {
			// update user document
			SearchCriteria search = new SearchCriteria();

			String value = aEntry.getString(ATTR_NAME_LAST_NAME);
			if (value != null) {
				search.addCriteria(ATTR_NAME_LAST_NAME, SearchCriteria.EXACT, value);
			}

			value = aEntry.getString(ATTR_NAME_FIRST_NAME);
			if (value != null) {
				search.addCriteria(ATTR_NAME_FIRST_NAME, SearchCriteria.EXACT, value);
			}

			value = aEntry.getString(ATTR_NAME_MIDDLE_INITIAL);
			if (value != null) {
				search.addCriteria(ATTR_NAME_MIDDLE_INITIAL, SearchCriteria.EXACT, value);
			}

			value = aEntry.getString(ATTR_NAME_MAIL_FILE);
			if(value != null){
				search.addCriteria(ATTR_NAME_MAIL_FILE, SearchCriteria.EXACT, value);
			}			

			if (search.getFirstCriteriaName() == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOUSERSCONN.PUTENTRY.CANNOT.BUILD.SEARCH.CRITERIA"));
			}

			Document modDocument = findPersonDocument(search);

			// Sleep for some time APAR IO13987
			if (modDocument == null) {
				// Maybe the document is not created yet. Try to wait a while and see if that helps
				boolean addedFullName = false;
				for (int i = 1; modDocument == null && i < 7; i++) {
					Thread.sleep(i*100);
					modDocument = findPersonDocument(search);
				
					// Try adding FullName to search criteria, it might help
					if (i == 2 || i == 4) {
						value = aEntry.getString(ATTR_NAME_FULL_NAME);
						if (value != null) {
							search.addCriteria(ATTR_NAME_FULL_NAME, SearchCriteria.EXACT, value);
							addedFullName = true;
						}			
					}
					// Try adding AltFullName
					if (i == 3) {
						if (addedFullName) {
							// Remove again for now, will get added back when i == 4;
							Vector<?> v = search.getCriteria();
							v.remove(v.size() - 1);
						}
						value = aEntry.getString(ATTR_NAME_ALT_FULL_NAME);
						if (value != null && value.length() > 0) {
							// add "CN=" in front
							if (! value.toUpperCase().startsWith("CN="))
								value = "CN=" + value;
							search.addCriteria(ATTR_NAME_ALT_FULL_NAME, SearchCriteria.INITIAL_STRING, value);
						}
					}
				}
			} // End of sleep. 
			
			if (modDocument == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOUSERSCONN.PUTENTRY.CANNOT.UPDATE.USER.DOCUMENT"));
			}

			Entry entryNoFixedAttributes = clearFixedDominoActionAttributes(
					mUserRegistration, aEntry);
			updateDocument(modDocument, entryNoFixedAttributes);

			modDocument.recycle();
		} else {
			// check for the "LastName" Attribute
			String lastName = aEntry.getString(ATTR_NAME_LAST_NAME);
			if (lastName == null || lastName.trim().length() == 0) {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.PUTENTRY.CANNOT.CREATE.PERSON.DOCUMENT",
										ATTR_NAME_LAST_NAME));
			}

			// create new user document
			Document newDocument = mDatabase.createDocument();
			if (newDocument == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.DOMINOUSERSCONN.PUTENTRY.CANNOT.CREATE.NEW.USER.DOCUMENT"));
			}

			Entry entryNoFixedAttributes = clearFixedDominoActionAttributes(
					mUserRegistration, aEntry);
			updateDocument(newDocument, entryNoFixedAttributes);

			newDocument.recycle();
		}
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 *
	 * @param aEntry
	 *            The entry data
	 * @param aSearch
	 *            The search criteria used to locate the entry to be deleted
	 * @exception Exception
	 *                if an error occurs.
	 */
	private void inner_deleteEntry(Entry aEntry, SearchCriteria aSearch)
			throws Exception {
		// find user's document and get user's fullname
		Document delDocument = findPersonDocument(aSearch);
		if (delDocument == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.DELETEENTRY.DOCUMENT.NOT.FOUND"));
		}

		String userFullName = delDocument
				.getItemValueString(ATTR_NAME_FULL_NAME);
		if (userFullName == null || userFullName.length() == 0) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.DELETEENTRY.USER.FULLNAME.NOT.FOUND"));
		}

		String userMailFile = delDocument
				.getItemValueString(ATTR_NAME_MAIL_FILE);
		delDocument.recycle();

		// delete user
		resetAndExtractData(mUserDeletion, aEntry);
		mUserDeletion.setUserFullName(userFullName);
		mUserDeletion.setUserMailFile(userMailFile);

		performDominoAction(mUserDeletion);
	}

	/**
	 * Finds a person Document in the 'Name and Address Book' database based on
	 * a search criteria
	 *
	 * @param aSearch
	 *            the search criteria to be used
	 * @return the Document of the Person found
	 * @throws Exception
	 *             if an error occurs
	 */
	private Document findPersonDocument(SearchCriteria aSearch)
			throws Exception {
		Document document = null;

		String formula = null;
		String scriptFilter = aSearch.getScriptFilter();

		if (scriptFilter == null) {
			SearchCriteria searchPlusForm = DominoUtils
					.cloneSearchCriteria(aSearch);
			searchPlusForm.addCriteria(DominoUtils.ITEM_NAME_FORM,
					SearchCriteria.EXACT, DominoUtils.ITEM_VALUE_PERSON);
			formatProperlyFullNameInCriteria(searchPlusForm, false);

			formula = DominoUtils.getNotesFormula(searchPlusForm);
		} else {
			formula = scriptFilter;

			if (formula.trim().length() == 0) {
				formula = FORMULA_FORM_PERSON;
			} else {
				formula = formula + " & " + FORMULA_FORM_PERSON;
			}
		}
		
		if (debugMode()) {
			debug(sResHash
					.getString(
							"CONNECTOR.DOMINOUSERSCONN.FINDPERSONDOCUMENT.USING.FORMULA",
							formula));
		}

		DocumentCollection dc = mDatabase.search(formula);
		if (dc != null) {
			if (dc.getCount() == 1) {
				document = dc.getFirstDocument();
			} else {
				debug("Found " + dc.getCount() + " documents using formula " + formula);
				dc.recycle();
			}
		}

		return document;
	}

	/**
	 * Updates a document with the attributes from an Entry
	 *
	 * @param aDocument
	 *            the document to be updated
	 * @param aEntry
	 *            the Entry data
	 * @throws Exception
	 *             if an error occur
	 */
	private void updateDocument(Document aDocument, Entry aEntry)
			throws Exception {
		String[] attributeNames = aEntry.getAttributeNames();
		if (attributeNames.length == 0) {
			return;
		}

		for (int i = 0; i < attributeNames.length; i++) {
			String name = attributeNames[i];
			Attribute attribute = aEntry.getAttribute(name);

			if (debugMode()) {
				debug(sResHash
						.getString(
								"CONNECTOR.DOMINOUSERSCONN.UPDATEDOCUMENT.UPDATE.ATTRIBUTE",
								name));
			}

			if (name.equalsIgnoreCase(ATTR_NAME_HTTP_PASSWORD)) {
				String plainPassword = attribute.getValue();
				Vector encPassword;
				try {
					encPassword = mSession.evaluate("@Password(\""
							+ plainPassword + "\")");
				} catch (NotesException e) {
					logmsg(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.ERROR.COULD.NOT.HASH.THE.HTTP.PASSWORD",
									e.toString()));
					throw e;
				}

				aDocument.replaceItemValue(name, encPassword.get(0));

				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.UPDATEDOCUMENT.UPDATE.VALUE.PASSWORD",
									encPassword.get(0)));
				}
			} else {
				if (attribute.size() == 1) {
					aDocument.replaceItemValue(name, attribute.getValue(0));
					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.UPDATEDOCUMENT.UPDATE.VALUE.ATTRIBUTE",
										attribute.getValue()));
					}
				} else {
					Vector attrValues = new Vector(Arrays.asList(attribute
							.getValues()));

					aDocument.replaceItemValue(name, attrValues);

					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.UPDATEDOCUMENT.UPDATE.VALUES",
										attrValues.toString()));
					}
				}
			}
		}

		boolean successfulSave = aDocument.save(true);
		if (!successfulSave) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.DOMINOUSERSCONN.UPDATEDOCUMENT.UNABLE.TO.SAVE.DOCUMENT"));
		}
	}

	/**
	 * Formats Full Name with a criteria
	 *
	 * @param aSearch
	 *            the search criteria go be used
	 * @param aUseFTSearch
	 *            true if Full-text search is set in the configuration
	 * @throws NotesException
	 *             if an error occurs in Domino database
	 */
	private void formatProperlyFullNameInCriteria(SearchCriteria aSearch,
			boolean aUseFTSearch) throws NotesException {
		Vector criteria = aSearch.getCriteria();
		if (criteria != null) {
			for (int i = 0; i < criteria.size(); i++) {
				Object obj = criteria.get(i);
				if (obj instanceof SearchCriteria.rscSearch) {
					SearchCriteria.rscSearch criteriaEl = (SearchCriteria.rscSearch) obj;
					if ((criteriaEl.name != null)
							&& (criteriaEl.name.equalsIgnoreCase(ATTR_NAME_FULL_NAME))
							&& (criteriaEl.match == SearchCriteria.EXACT)
							&& (criteriaEl.value != null)) {
						String oldFullName = criteriaEl.value.toString();
						String properFullName = null;
						if (aUseFTSearch) {
							properFullName = DominoUtils.getUserAbbreviatedName(mSession, oldFullName);
						} else {
							properFullName = DominoUtils.getUserCanonicalName(mSession, oldFullName);
						}

						criteriaEl.value = properFullName;

						if (! oldFullName.equals(properFullName)) {
							debug(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.FULLNAME.CRITERIA.ELEMENT.VALUE.CHANGED",
										new Object[] { oldFullName, properFullName }));
						}
					}
				}
			}
		}
	}

	/**
	 * Formats properly full name for update
	 *
	 * @param aEntry
	 *            the Entry data
	 * @throws NotesException
	 *             if an error occurs in Domino database
	 */
	private void formatProperlyFullNameForUpdate(Entry aEntry)
			throws NotesException {
		Attribute attrFullName = aEntry.getAttribute(ATTR_NAME_FULL_NAME);
		if (attrFullName == null) {
			return;
		}

		// canonize the first value of the FullName attribute
		String oldFullName = attrFullName.getValue();
		if (oldFullName != null) {
			String canonicalFullName = DominoUtils.getUserCanonicalName(
					mSession, oldFullName);
			attrFullName.setValue(0, canonicalFullName);

			if (!oldFullName.equals(canonicalFullName)) {
				debug(sResHash.getString("CONNECTOR.DOMINOUSERSCONN.FULLNAME.ATTRIBUTE.CHANGED",
						new Object[] { oldFullName, canonicalFullName }));
			}
		}
	}

	/**
	 * Inner method corresponding to the ConnectorInterface method
	 */
	private void inner_terminate() {
		// recycle Domino objects
		try {
			if (mDocumentCollection != null) {
				mDocumentCollection.recycle();
				mDocumentCollection = null;
			}

			if (mEntryCollection != null) {
				mEntryCollection.recycle();
				mEntryCollection = null;
			}

			if (mPeopleView != null) {
				mPeopleView.recycle();
				mPeopleView = null;
			}

			if (mDatabase != null) {
				mDatabase.recycle();
				mDatabase = null;
			}

			if (mAdminDatabase != null) {
				mAdminDatabase.recycle();
				mAdminDatabase = null;
			}

			termLocalThread();

			if (mSession != null) {
				mSession.recycle();
				mSession = null;
			}
		} catch (NotesException e) {
			if (debugMode()) {
				debug(sResHash
						.getString(
								"CONNECTOR.DOMINOUSERSCONN.TEMINATE.COULD.NOT.RECYCLE.DOMINO.OBJECT",
								e.toString()));
			}
		}
	}

	// *************************************************************************
	// Connector's "delete" APIs
	// *************************************************************************

	/**
	 * API provided to get the default delete mail file type
	 *
	 * @return the deletion type
	 */
	public int getDeleteMailFile() {
		return mUserDeletion.getDefaultDeleteMailFile();
	}

	/**
	 * API provided to specify how and if the mail file should be deleted Can be
	 * one of: 0 - Don't delete mail file 1 - Delete just the mail file
	 * specified in Person document 2 - Delete mail file specified in Person
	 * document and all replicas
	 *
	 * @param aDeleteType
	 *            the type to be set
	 * @throws Exception
	 *             if an error occur
	 */
	public void setDeleteMailFile(int aDeleteType) throws Exception {
		executeCommand(COMMAND_SET_DELETE_MAIL, new Object[] { Integer
				.valueOf(aDeleteType) }, false);
	}

	/**
	 * API provided to get the group that the user name is placed on deletion.
	 * Typically this is the "Deny List only" group
	 *
	 * @return the group name
	 */
	public String getDeleteGroupName() {
		return mUserDeletion.getDefaultAddToGroup();
	}

	/**
	 * API provided to specify the group that the user name should be placed on
	 * deletion. Typically this is the "Deny List only" group
	 *
	 * @param aGroupName
	 *            the name of the group
	 * @throws Exception
	 *             if an error occur
	 */
	public void setDeleteGroupName(String aGroupName) throws Exception {
		executeCommand(COMMAND_SET_DELETE_GROUP_NAME,
				new Object[] { aGroupName }, false);
	}

	/**
	 * Inner method corresponding to the Connector's method
	 *
	 * @param aDeleteType
	 *            the type of deletion
	 * @throws Exception
	 *             if an error occur
	 */
	private void inner_setDeleteMailFile(int aDeleteType) throws Exception {
		mUserDeletion.setDefaultDeleteMailFile(aDeleteType);
	}

	/**
	 * Inner method corresponding to the Connector's method
	 *
	 * @param aGroupName
	 *            the group name
	 * @throws Exception
	 *             if an error occur
	 */
	private void inner_setDeleteGroupName(String aGroupName) throws Exception {
		mUserDeletion.setDefaultAddToGroup(aGroupName);
	}

	// *************************************************************************
	// Domino actions invokation mechanism
	// *************************************************************************
	/**
	 * Checks if a Domino Action must be performed
	 *
	 * @return true if must be performed
	 */

	/**
	 * Inspects the Attributes of the given Entry and determines if the Domino
	 * Action has to be performed.
	 *
	 * @param aDominoAction
	 *            the action to be checked
	 * @param aEntry
	 *            the Entry date
	 * @return true if must be performed
	 * @throws Exception
	 *             if an error occur
	 */
	protected boolean mustPerformDominoAction(IDominoAction aDominoAction,
			Entry aEntry) throws Exception {
		try {
			return aDominoAction.mustPerform(aEntry);
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.MUSTPERFORMDOMINOACTION.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	/**
	 * Resets the local data holders to empty/default values according the
	 * business logic of the Domino Action.
	 *
	 * @param aDominoAction
	 *            the action
	 * @param aEntry
	 *            the Entry data
	 * @throws Exception
	 *             if an error occur
	 */
	protected void resetAndExtractData(IDominoAction aDominoAction, Entry aEntry)
			throws Exception {
		try {
			aDominoAction.resetData();
			aDominoAction.extractAndStoreData(aEntry);
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.RESETANDEXTRACTDATA.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	/**
	 * Check if a Domino Action could be performed and if so it is performed
	 *
	 * @param aDominoAction
	 *            the action
	 * @throws Exception
	 *             if an error occur
	 */
	protected void performDominoAction(IDominoAction aDominoAction)
			throws Exception {
		try {
			String canPerformMsg = aDominoAction.canPerform();
			if (canPerformMsg == null) {
				aDominoAction.perform();
			} else {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.PERFORMDOMINOACTION.CANNOT.PERFORM.DOMINO.ACTION",
										canPerformMsg));
			}
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.PERFORMDOMINOACTION.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	/**
	 * Extracts and stores data for the Domino Action
	 *
	 * @param aDominoAction
	 *            the action
	 * @param aEntry
	 *            the Entry data
	 * @return the fixed Entry
	 * @throws Exception
	 *             if an error occur
	 */
	protected Entry clearFixedDominoActionAttributes(
			IDominoAction aDominoAction, Entry aEntry) throws Exception {
		try {
			return aDominoAction.extractAndStoreData(aEntry);
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.CLEARFIXEDDOMINOACTIONATTRIBUTES.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	/**
	 * If the Domino Action should be performed this method resets the local
	 * data holders to empty/default values according the action and then
	 * extracts and stores the data
	 *
	 * @param aDominoAction
	 *            the action
	 * @param aEntry
	 *            the Entry data
	 * @throws Exception
	 *             if an error occur
	 */
	protected void checkExtractAndExecuteDominoAction(
			IDominoAction aDominoAction, Entry aEntry) throws Exception {
		try {
			if (!aDominoAction.mustPerform(aEntry)) {
				return;
			}

			aDominoAction.resetData();
			aDominoAction.extractAndStoreData(aEntry);

			String canPerformMsg = aDominoAction.canPerform();
			if (canPerformMsg == null) {
				aDominoAction.perform();
			} else {
				throw new Exception(
						sResHash
								.getString(
										"CONNECTOR.DOMINOUSERSCONN.CANNOT.PERFORM.DOMINO.ACTION",
										canPerformMsg));
			}
		} catch (NotesException e) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.DOMINOUSERSCONN.CHECKEXTRACTANDEXECUTEDOMINOACTION.NOTESEXCEPTION",
									new Object[] { Integer.valueOf(e.id),
											e.text }));
		}
	}

	// *************************************************************************
	// access methods
	// *************************************************************************

	/**
	 * Getter method for the mSession inner variable
	 *
	 * @return the session
	 */
	public Session getSession() {
		return mSession;
	}

	/**
	 * Getter method for the mDatabase inner variable
	 *
	 * @return the database
	 */
	public Database getDatabase() {
		return mDatabase;
	}

	/**
	 * Getter method for the mAdminDatabase inner variable
	 *
	 * @return the Administration database
	 */
	public Database getAdminDatabase() {
		return mAdminDatabase;
	}

}
