/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;

/**
 * This is the class for the TDI Lotus Notes Connector.
 */
public class DominoConnector extends Connector implements ConnectorInterface, Runnable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "lotusnotesconnector";

	/**
	 *
	 */
	final static String eol = "\r\n";

	/**
	 * {@link Session} type
	 */
	private Session session;

	/**
	 * {@link Database}
	 */
	private Database db;

	/**
	 * {@link DocumentCollection}
	 */
	private DocumentCollection dc = null;

	/**
	 * {@link ViewEntryCollection}
	 */
	private ViewEntryCollection vc;

	/**
	 * {@link View}
	 */
	private View view = null;

	/**
	 * Current document
	 */
	private Document curdoc;

	/**
	 * {@link Document} corresponding to provided UnID.
	 */
	private Document foundDoc;

	/**
	 * Used by subclasses.
	 */
	protected String selection;

	/**
	 * database name
	 */
	private String database;

	/**
	 * Notes search view.
	 */
	private String searchView;

	/**
	 * Server name.
	 */
	private String server;

	/**
	 * Full text search flag.
	 */
	private boolean useFTSearch;

	/**
	 * Local Notes API thread
	 */
	private Object localThread = null;

	/**
	 * Name of the component.
	 */
	private static final String myName = "Metamerge Lotus Domino Connector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	// inner Notes thread
	/**
	 * Index for command for the inner Notes thread - no command
	 */
	private static final int NO_COMMAND = 0;

	/**
	 * Index for command for the inner Notes thread - initialize notes
	 */
	private static final int COMMAND_INITIALIZE = 1;

	/**
	 * Index for command for the inner Notes thread - select entries
	 */
	private static final int COMMAND_SELECT_ENTRIES = 2;

	/**
	 * Index for command for the inner Notes thread - get next entry
	 */
	private static final int COMMAND_GET_NEXT_ENTRY = 3;

	/**
	 * Index for command for the inner Notes thread - find entry
	 */
	private static final int COMMAND_FIND_ENTRY = 4;

	/**
	 * Index for command for the inner Notes thread - modify entry
	 */
	private static final int COMMAND_MOD_ENTRY = 5;

	/**
	 * Index for command for the inner Notes thread - put
	 */
	private static final int COMMAND_PUT_ENTRY = 6;

	/**
	 * Index for command for the inner Notes thread - delete entry
	 */
	private static final int COMMAND_DELETE_ENTRY = 7;

	/**
	 * Index for command for the inner Notes thread - terminate
	 */
	private static final int COMMAND_TERMINATE = 8;

	/**
	 * Index for command for the inner Notes thread - query database
	 */
	private static final int COMMAND_QUERY_DATABASE = 9;

	/**
	 * Index for command for the inner Notes thread - query views
	 */
	private static final int COMMAND_QUERY_VIEWS = 10;

	/**
	 * Index for command for the inner Notes thread - set current
	 */
	private static final int COMMAND_SET_CURRENT = 11;

	/**
	 * Index for command for the inner Notes thread - get dmoni view
	 */
	private static final int COMMAND_GET_DOMINO_VIEW = 12;

	/**
	 * Index for command for the inner Notes thread - modify document
	 */
	private static final int COMMAND_MOD_DOC = 13;

	/**
	 * Index for command for the inner Notes thread - build entry
	 */
	private static final int COMMAND_BUILD_ENTRY = 14;

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

	private boolean conserveMemory = false;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor for the DominoConnector object
	 */
	public DominoConnector() {
		Trace.entrymid(this, "DominoConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE });
		Trace.exitmid(this, "DominoConnector");
	}

	/**
	 * Gets the nextEntry attribute of the DominoConnector object
	 *
	 * @return The nextEntry value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Entry getNextEntry() throws Exception {
		return (Entry) executeCommand(COMMAND_GET_NEXT_ENTRY, null, true);
	}

	/**
	 * Gets the nextEntry attribute of the DominoConnector object
	 *
	 * @return The nextEntry value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private Entry inner_getNextEntry() throws Exception {
		Trace.entrymin(this, "getNextEntry");
		if (dc == null && vc == null) {
			Trace.exitmin(this, "getNextEntry");
			return null;
		}

		try {

			if (vc != null) {
				if (curdoc == null) {
					if (vc.getFirstEntry() != null) {
						curdoc = vc.getFirstEntry().getDocument();
					} else {
						curdoc = null;
					}
				} else {
					 Document olddoc = curdoc;
					ViewEntry next = vc.getNextEntry();
					if (next != null) {
						curdoc = next.getDocument();
					} else {
						curdoc = null;
					}
					// The following line is commented because of
					// the defect 6275 in the CMVC. Please see the defect
					// for more information.
					// olddoc.recycle();
					if (conserveMemory)
							olddoc.recycle();

				}
			}

			if (dc != null) {
				if (curdoc == null) {
					curdoc = dc.getFirstDocument();
				} else {
					Document tmp = dc.getNextDocument();
					curdoc.recycle();
					curdoc = tmp;
				}
			}
		} catch (NotesException e) {
			Trace.exception(this, "getNextEntry", e, " ");
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.GETENTRY.EXCEPTION", new Object[] { "" + e.id, e.text }));
		}

		if (curdoc == null) {
			if (dc != null) {
				dc.recycle();
				dc = null;
			}
			if (vc != null) {
				vc.recycle();
				vc = null;
			}
			Trace.exitmin(this, "getNextEntry");
			return null;
		}

		Entry e = inner_buildEntry(curdoc);

		Trace.exitmin(this, "getNextEntry", e);
		return e;
	}

	/**
	 * Gets the dominoSession attribute of the DominoConnector object
	 *
	 * @return The dominoSession value
	 */
	public Session getDominoSession() {
		return session;
	}

	/**
	 * Gets the dominoDatabase attribute of the DominoConnector object
	 *
	 * @param database
	 *            the name of the Domino database file (usually a file with a
	 *            .nsf filename extension)
	 *
	 * @return The dominoDatabase value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Database getDominoDatabase(String database) throws Exception {
		Trace.entrymax(this, "getDominoDatabase", database);
		if (database == null) {
			Trace.exitmax(this, "getDominoDatabase", db);
			return db;
		} else {
			Trace.exitmax(this, "getDominoDatabase");
			return session.getDatabase(server, database);
		}
	}

	/**
	 * Gets the dominoView attribute of the DominoConnector object
	 *
	 * @param view
	 *            the name of the Domino view
	 *
	 * @return The dominoView value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public View getDominoView(String view) throws Exception {
		return (View) executeCommand(COMMAND_GET_DOMINO_VIEW, new Object[] { view }, true);
	}

	/**
	 * Gets the dominoView attribute of the DominoConnector object
	 *
	 * @param view
	 *            the name of the Domino view
	 *
	 * @return The dominoView value
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private View inner_getDominoView(String view) throws Exception {
		if (view == null) {
			return db.getView(searchView);
		} else {
			return db.getView(view);
		}
	}

	/**
	 * Return version information
	 *
	 * @return The version value
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 2017/02/20";
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {
		try {
			termLocalThread(); // added by L3 for defect 13583
			executeCommand(COMMAND_TERMINATE, null, false);
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.NOTES.ERROR.WHILE.TERMINATING", e.getMessage()));
		}
	}

	/**
	 * This method cleans up allocated resources (recycles database/view
	 * objects, terminates threads) and terminates the Connector.
	 */
	private void inner_terminate() {
		Trace.entrymin(this, "terminate");
		try {

			db = null;
			if (dc != null) {
				dc.recycle();
				dc = null;
			}
			if (view != null) {
				view.recycle();
				view = null;
			}

			vc = null;

		} catch (NotesException ne) {
			Trace.exception(this, "terminate", ne, " ");
		}

		termLocalThread();
		try {
			if (session != null) {
				session.recycle();
				session = null;
			}
		} catch (NotesException ne) {
			Trace.exception(this, "terminate", ne, " ");
		}
		Trace.exitmin(this, "terminate");
	}

	/**
	 * This method initializes the Connector.
	 *
	 * @param o
	 *            this parameter is ignored
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void initialize(Object o) throws Exception {
		if (isNotesThreadAlive()) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.CONNECTOR.ALREADY.INITIALIZED"));
		}

		mNotesThread = new Thread(this);
		mNotesThreadCommand = NO_COMMAND;
		mNotesThread.start();

		initLocalThread(); // added by L3 for defect 13583

		executeCommand(COMMAND_INITIALIZE, new Object[] { o }, false);
	}

	/**
	 * Initializes the internal worker thread and the connector configuration.
	 *
	 * @param o
	 *            ignored.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void inner_initialize(Object o) throws Exception {

		Trace.entrymin(this, "initialize", o);

		server = getParam("notesServer");

		if (((ConnectorConfig) getConfiguration()).getMode().equals(ConnectorConfig.ITERATOR_MODE)) {
			selection = getParam("notesSelection");
		} else {
			selection = null;
		}

		database = getParam("notesDatabase");
		if (database == null || database.trim().length() <= 0) {
			database = "names.nsf"; // (D4522)Causes a crash of Domino server if
			// this value is passed empty.
		}
		searchView = getParam("notesSearchView");

		String host = getParam("dominoHost");
		String httpPort = getParam("httpPort");
		String user = getParam("dominoLogin");
		String pass = getParam("dominoPassword");
		String ssl = getParam("iiopSSL");
		String sessionType = getParam("dominoSessionType");

		int port = 80;

		if (httpPort != null) {
			try {
				port = Integer.parseInt(httpPort);
				if ((port <= 0) || (port > 65535)) {
					logmsg(sResHash.getString("CONNECTOR.NOTES.SPECIFIED.PORT.NOT.VALID.WARNING", new Object[] { httpPort }));
				}
			} catch (NumberFormatException e) {
				logmsg(sResHash.getString("CONNECTOR.NOTES.SPECIFIED.PORT.INCORRECT.EXCEPTION", new Object[] { httpPort }));

			}
		}

		if (sessionType == null) {
			sessionType = "IIOP";
		}

		String args[] = null;
		String[] iorArgs = null;

		if (server == null) {
			server = "";
		}

		boolean isSslEnabled = false;
		if (ssl != null && ssl.equalsIgnoreCase("true")) {
			isSslEnabled = true;
			args = new String[1];
			args[0] = "-ORBEnableSSLSecurity";

			iorArgs = new String[1];
			iorArgs[0] = "-HTTPEnableSSLSecurity";
		}

		String setConserveMemory = getParam("ConserveMemory");
		if (setConserveMemory != null && setConserveMemory.equals("true"))
					conserveMemory = true;

		// Access via local notes client requires null value for host and user
		if (sessionType.equalsIgnoreCase("LocalClient")) {
			host = null;
			user = null;
			if (pass != null && pass.equals("")) {
				pass = null;
			}

			initLocalThread();
		}

		// Access to local domino server requires null value for host and
		// non-null for user and pass
		if (sessionType.equalsIgnoreCase("LocalServer")) {
			host = null;
			if (user == null) {
				user = "";
			}
			if (pass == null) {
				pass = "";
			}

			initLocalThread();
		}

		logmsg(sResHash.getString("CONNECTOR.NOTES.SESSION.INFO", new Object[] { sessionType, host, user, "" + (args != null) }));

		try {
			if (sessionType.equalsIgnoreCase("IIOP")) {
				if (host != null && host.startsWith("IOR:")) {
					logmsg(sResHash.getString("CONNECTOR.NOTES.IOR.INFO"));
					session = NotesFactory.createSessionWithIOR(host, args, user, pass);
					if (isSslEnabled) {
						logmsg(sResHash.getString("CONNECTOR.NOTES.IOR.USED.SSL.ON.SESSION.CREATED"));
					} else {
						logmsg(sResHash.getString("CONNECTOR.NOTES.IOR.USED.SSL.OFF.SESSION.CREATED"));
					}
				} else {
					// We explicitly get the IOR by providing username and
					// password.
					// This way the IOR can be obtained even if Domino's
					// HTTP task does not permit anonymous connections.
					host = NotesFactory.getIOR(host + ":" + port, iorArgs, user, pass);
					session = NotesFactory.createSessionWithIOR(host, args, user, pass);
					if (isSslEnabled) {
						logmsg(sResHash.getString("CONNECTOR.NOTES.PORT.USED.SSL.ON.SESSION.CREATED"));
					} else {
						logmsg(sResHash.getString("CONNECTOR.NOTES.PORT.USED.SSL.OFF.SESSION.CREATED"));
					}
				}
			} else {
				session = NotesFactory.createSession(host, args, user, pass);
				logmsg(sResHash.getString("CONNECTOR.NOTES.SESSION.CREATED"));
			}
		} catch (lotus.domino.NotesException ne) {
			Trace.exception(this, "initialize", ne, "lotus.domino.NotesException");
			logmsg(sResHash.getString("CONNECTOR.NOTES.SESSION.EXCEPTION",
					new Object[] { ne.getClass().getName(), ne.getMessage() }));
			ne.printStackTrace();
			throw new Exception(ne.text);
		}

		logmsg(sResHash.getString("CONNECTOR.NOTES.CONNECTED.INFO", new Object[] { session.getServerName(),
				session.getNotesVersion(), session.getPlatform() }));

		db = session.getDatabase(server, database);
		if (db == null) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.DATABASE.EXCEPTION", new Object[] { database, server }));
		}

		String useformula = getParam("alwaysUseFormula");
		if (useformula != null && useformula.equals("true")) {
			useFTSearch = false;
		} else {
			useFTSearch = db.isFTIndexed();
		}

		if (db.isFTIndexed()) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.FULLTXINDEX.INFO"));
			}
		} else {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.NONFULLTXINDEX.INFO"));
			}
		}

		if (searchView != null && searchView.length() > 0) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.WILLFULLTXTSEARCH.INFO"));
			}
		} else {
			if (useFTSearch) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.NOTES.WILLFULLTXTSEARCH.INFO"));
				}
			} else {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.NOTES.WILLNOTFULLTXTSEARCH.INFO"));
				}
			}
		}
		Trace.exitmin(this, "initialize");
	}
	
	public Document getNotesDoc() {
		return foundDoc != null ? foundDoc : curdoc;
	}

	/**
	 * This method is used in Iterator mode. This method retrieves the entries
	 * which will be iterated upon by the getNextEntry method.
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void selectEntries() throws Exception {
		executeCommand(COMMAND_SELECT_ENTRIES, null, false);
	}

	/**
	 * This method retrieves the entries which will be iterated upon by the
	 * getNextEntry method.
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void inner_selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		try {
			if (searchView != null && searchView.length() > 0) {
				View v = db.getView(searchView);
				if (v == null) {
					throw new Exception(sResHash.getString("CONNECTOR.NOTES.VIEWNOTFOUND.ERROR", searchView));
				}
				if (selection == null || selection.length() < 1) {
					vc = v.getAllEntries();
				} else {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.NOTES.DOCSELECTION.INFO", selection));
					}
					v.FTSearch(selection);
					vc = v.getAllEntries();
				}
			} else {
				if (selection == null || selection.length() < 1) {
					dc = db.getAllDocuments();
				} else {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.NOTES.DOCSELECTION.INFO", selection));
					}
					dc = db.search(selection);
				}
			}
		} catch (NotesException e) {
			Trace.exception(this, "selectEntries", e, " ");
			logmsg(sResHash.getString("CONNECTOR.NOTES.SELECT.EXCEPTION", e.text));
			throw e;
		}

		if (dc == null && vc == null) {
			logmsg(sResHash.getString("CONNECTOR.NOTES.NOSEARCHRESULTS.INFO"));
		} else {
			logmsg(sResHash.getString("CONNECTOR.NOTES.SEARCHRESULTS.INFO", "" + (dc != null ? dc.getCount() : vc.getCount())));
		}
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * This method is invoked by the getNextEntry method. This method is used to
	 * create and populate the entry object to be returned to the AssemblyLine.
	 *
	 * @param doc
	 *            the Domino Document object which provides the data for the
	 *            entry being populated
	 * @return the populated TDI entry object
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Entry buildEntry(Document doc) throws Exception {
		return (Entry) executeCommand(COMMAND_BUILD_ENTRY, new Object[] { doc }, true);
	}

	/**
	 * This method is invoked by the buildEntry method. It is used to create and
	 * populate the entry object to be returned to the AssemblyLine.
	 *
	 * @param doc
	 *            the Domino Document object which provides the data for the
	 *            entry being populated
	 * @return the populated TDI entry object
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private Entry inner_buildEntry(Document doc) throws Exception {
		Trace.entrymax(this, "buildEntry", doc);
		Entry e = new Entry();
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.ENTER.BUILD.ENTRY.INFO"));
		}

		e.setAttribute("NoteID", doc.getNoteID());
		e.setAttribute("UNID", doc.getUniversalID());
		
		e.setProperty("document", doc);

		Vector<?> v = doc.getItems();
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.BUILDENTRY.INFO", v.toString()));
		}
		if (v != null) {
			for (int j = 0; j < v.size(); j++) {
				Item item = (Item) v.elementAt(j);
				if (isConvertable(item.getType())) {
					// If the item is RichText and RichText is supported add the
					// attirbute as RichTextItem
					if (item.getType() == Item.RICHTEXT && getParam("supportRichText").equals("true")) {
						e.setAttribute(item.getName(), item);
					} else {

						addValue(e, item.getName(), item.getValues(), item.getType());
					}
				} else {
					e.setAttribute(item.getName(), item);
				}
			}
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.EXIT.BUILD.ENTRY.INFO"));
		}

		Trace.exitmax(this, "buildEntry", e);

		return e;
	}

	/**
	 * This method is used internally.
	 *
	 * @param type
	 *            the type id.
	 * @return true if the Domino type specified by the provided id can be
	 *         converted, false otherwise.
	 */
	public boolean isConvertable(int type) {
		switch (type) {
		case Item.TEXT:
		case Item.NAMES:
		case Item.RICHTEXT:
		case Item.READERS:
		case Item.AUTHORS:
		case Item.DATETIMES:
		case Item.NUMBERS:
			return true;
		}

		return false;
	}

	/**
	 * This method is used in AddOnly mode. This method writes data to the
	 * Domino database.
	 *
	 * @param entry
	 *            the entry to be written to the Domino database
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void putEntry(Entry entry) throws Exception {
		executeCommand(COMMAND_PUT_ENTRY, new Object[] { entry }, false);
	}

	/**
	 * This method writes data to the Domino database.
	 *
	 * @param entry
	 *            the entry to be written to the Domino database
	 *
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void inner_putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "putEntry", entry);
		Document d = db.createDocument();

		if (d == null) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.DOCCREATE.EXCEPTION"));
		}

		inner_modDocument(d, entry);
		Trace.exitmin(this, "putEntry");
	}

	/**
	 * This method is used in Update mode. This method makes changes to an
	 * existing document in the Domino database.
	 *
	 * @param entry
	 *            the entry which stores the new data with which existing data
	 *            will be replaced
	 * @param search
	 *            the search criteria with which the existing database document
	 *            to be changed will be located
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		Trace.entrymin(this, "modEntry");
		modEntry(entry, search, findEntry(search));
		Trace.exitmin(this, "modEntry");
	}

	/**
	 * This method is used in Update mode. This method makes changes to an
	 * existing document in the Domino database.
	 *
	 * @param entry
	 *            the entry which stores the new data with which existing data
	 *            will be replaced
	 * @param search
	 *            the search criteria with which the existing database document
	 *            to be changed will be located
	 * @param old
	 *            used for the implementation of updating documents - it has no
	 *            meaning for the user of the Connector
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		executeCommand(COMMAND_MOD_ENTRY, new Object[] { entry, search, old }, false);
	}

	/**
	 * This method makes changes to an existing document in the Domino database.
	 *
	 * @param entry
	 *            the entry which stores the new data with which existing data
	 *            will be replaced
	 * @param search
	 *            the search criteria with which the existing database document
	 *            to be changed will be located
	 * @param old
	 *            used for the implementation of updating documents - it has no
	 *            meaning for the user of the Connector
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void inner_modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		Trace.entrymin(this, "modEntry", entry, old);
		if (foundDoc != null && old != null && !foundDoc.getNoteID().equals(old.getString("NoteID"))) {
			foundDoc.recycle();
			foundDoc = null;
		}

		if (foundDoc == null || old == null) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.MODDOC.EXCEPTION"));
		}

		// Document d = dc.getFirstDocument();
		inner_modDocument(foundDoc, entry);
		foundDoc = null;
		Trace.exitmin(this, "modEntry");
	}

	/**
	 * This method is used in Delete mode. This methods deletes entries from the
	 * Domino database.
	 *
	 * @param entry
	 *            used for implementation internals only
	 * @param search
	 *            the search criteria with which the existing database document
	 *            to be deleted will be located
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		executeCommand(COMMAND_DELETE_ENTRY, new Object[] { entry, search }, false);
	}

	/**
	 * This methods deletes entries from the Domino database.
	 *
	 * @param entry
	 *            used for implementation internals only
	 * @param search
	 *            the search criteria with which the existing database document
	 *            to be deleted will be located
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void inner_deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		Trace.entrymin(this, "deleteEntry", entry, search);

		if (foundDoc != null && entry != null && !foundDoc.getNoteID().equals(entry.getString("NoteID"))) {
			foundDoc.recycle();
			foundDoc = null;
		}
		if (foundDoc == null && inner_findEntry(search) == null) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.DELETEDOC.EXCEPTION"));
		}

		// Document d = dc.getFirstDocument();
		foundDoc.remove(true);
		// foundDoc.recycle();
		foundDoc = null;

		Trace.exitmin(this, "deleteEntry");
	}

	/**
	 * This method is used in Lookup, Update and Delete modes. This methods
	 * locates an entry by the given search criteria.
	 *
	 * @param search
	 *            the search criteria with which the database document will be
	 *            located
	 *
	 * @return the located entry object
	 * @exception Exception
	 *                if this method fails.
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		return (Entry) executeCommand(COMMAND_FIND_ENTRY, new Object[] { search }, true);
	}

	/**
	 * Internal implementation of the findEntry method executed by the worker
	 * thread.
	 *
	 * @param search
	 *            the search criteria with which the database document will be
	 *            located
	 * @return the located entry object
	 * @throws Exception
	 *             if this method fails.
	 */
	public Entry inner_findEntry(SearchCriteria search) throws Exception {
		Trace.entrymin(this, "findEntry", search);

		clearFindEntries();

		if (foundDoc != null) {
			foundDoc.recycle();
			foundDoc = null;
		}

		DocumentCollection collection = null;

		if (view != null) {

			// The following line is commented, because
			// it causes an Exception to be thrown when the
			// Connector is reused (Requirement US-3: Reusing Connectors
			// in the same AL; CMVC feature #7746)
			// This fix is done in defect #7747

			// view.recycle();
			view = null;
		}

		String filter = null;

		String UniversalID = null;
		boolean isUNIDUsed = false;
		for (int i = 0; i < search.size(); i++) {
			if (search.getCriteria(i).name.equalsIgnoreCase("UNID")) {
				if (search.getCriteria(i).match == SearchCriteria.EXACT) {
					UniversalID = (String) search.getCriteria(i).value;
					isUNIDUsed = true;
					break;
				} else {
					throw new Exception(sResHash.getString("CONNECTOR.NOTES.MATCH.OPERATION.EXCEPTION"));
				}
			}
		}

		String NoteID = null;
		boolean isNoteIDUsed = false;
		for (int i = 0; i < search.size(); i++) {
			if (search.getCriteria(i).name.equalsIgnoreCase("NoteID")) {
				if (search.getCriteria(i).match == SearchCriteria.EXACT) {
					NoteID = (String) search.getCriteria(i).value;
					isNoteIDUsed = true;
					break;
				} else {
					throw new Exception(sResHash.getString("CONNECTOR.NOTES.MATCH.OPERATION.EXCEPTION"));
				}
			}
		}

		if (isUNIDUsed || isNoteIDUsed) {
			try {
				if (isUNIDUsed) {
					foundDoc = db.getDocumentByUNID(UniversalID);
				} else {
					foundDoc = db.getDocumentByID(NoteID);
					if (foundDoc == null) {
						throw new NotesException();
					}
				}
			} catch (NotesException e) {
				throw new Exception(sResHash.getString("CONNECTOR.NOTES.NO.MATCHING.EXCEPTION")+" "+e.text);
			}
			if (foundDoc.isValid()) {
				// doc represents a document but not delition stub
				return inner_buildEntry(foundDoc);
			} else {
				return null;
			}

		} else {

			if (searchView != null && searchView.length() > 0) {

				view = db.getView(searchView);
				filter = search.getNotesFTFilter();

				if (view != null) {
					try {
						if (debugMode()) {
							logmsg(sResHash.getString("CONNECTOR.NOTES.SEARCHVIEW.INFO", searchView));
							logmsg(sResHash.getString("CONNECTOR.NOTES.VIEWFILTER.INFO", filter));
						}
						int res = view.FTSearch(filter);
						if (debugMode()) {
							logmsg(sResHash.getString("CONNECTOR.NOTES.FTSEARCHRC.INFO", "" + res));
						}

						if (res == 1) {
							foundDoc = view.getFirstDocument();
							Trace.exitmin(this, "findEntry");
							return inner_buildEntry(foundDoc);
						}

						// Collect duplicate entries
						while (res > 0) {
							if (foundDoc == null) {
								foundDoc = view.getFirstDocument();
							} else {
								Document tmp = view.getNextDocument(foundDoc);
								foundDoc.recycle();
								foundDoc = tmp;
							}

							if (foundDoc == null || !addFindEntry(inner_buildEntry(foundDoc)))
								res = 1;

							res--;
						}

						if (foundDoc != null) {
							foundDoc.recycle();
							foundDoc = null;
						}
						Trace.exitmin(this, "findEntry");
						return null;
					} catch (NotesException err) {
						logmsg(sResHash.getString("CONNECTOR.NOTES.VIEWFILTER.WARNING", new Object[] { filter, err.toString(),
								err.text }));
					}
				} else {
					logmsg(sResHash.getString("CONNECTOR.NOTES.CANNOT.FIND.VIEW", new Object[] { searchView, db.getFileName() }));
					searchView = null;
				}
			}

			if (useFTSearch) {
				filter = search.getNotesFTFilter();
				if (debugMode()) {
					logmsg(sResHash.getString("CONNECTOR.NOTES.VIEWFILTER2.INFO", filter));
				}
				collection = db.FTSearch(filter);
			} else {
				filter = search.getNotesFilter();
				if (debugMode()) {
					logmsg(sResHash.getString("CONNECTOR.NOTES.VIEWFILTER3.INFO", filter));
				}
				collection = db.search(filter);
			}

			if (collection == null || collection.getCount() == 0) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.NOTES.NODOCSFOUND.INFO"));
				}
				return null;
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.DOCSFOUNDCOUNT.INFO", "" + collection.getCount()));
			}
			if (collection.getCount() == 1) {
				foundDoc = collection.getFirstDocument();
				Trace.exitmin(this, "findEntry");
				return inner_buildEntry(foundDoc);
			}

			// Collect duplicate entries
			while (true) {
				if (foundDoc == null) {
					foundDoc = collection.getFirstDocument();
				} else {
					Document tmp = collection.getNextDocument();
					foundDoc.recycle();
					foundDoc = tmp;
				}

				if (foundDoc == null || !addFindEntry(inner_buildEntry(foundDoc))) {
					if (foundDoc != null)
						foundDoc.recycle();
					foundDoc = null;
					Trace.exitmin(this, "findEntry");
					return null;
				}
			}
		}
	}

	/**
	 * Used when multiple entries found, and you want to modify or delete one of
	 * them. change foundDoc to be this entry.
	 *
	 * @param entry
	 *            The entry we want to find for modification/delete
	 * @param search
	 *            The search
	 */
	public void setCurrent(Entry entry, SearchCriteria search) {
		try {
			executeCommand(COMMAND_SET_CURRENT, new Object[] { entry, search }, false);
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.NOTES.ERROR.PROCESSING.ENTRIES", e.getMessage()));
		}
	}

	/**
	 * Used when multiple entries found, and you want to modify or delete one of
	 * them. change foundDoc to be this entry.
	 *
	 * @param entry
	 *            The entry we want to find for modification/delete
	 * @param search
	 *            The search
	 */
	private void inner_setCurrent(Entry entry, SearchCriteria search) {
		Trace.entrymax(this, "setCurrent", entry, search);

		if (entry == null || search == null)
			return;

		String noteID = entry.getString("NoteID");

		if (noteID == null)
			return;

		search.getCriteria().clear();
		search.addCriteria("NoteID", SearchCriteria.EXACT, noteID);

		try {
			if (foundDoc != null) {
				foundDoc.recycle();
				foundDoc = null;
			}

			if (debugMode()) {
				logmsg(sResHash.getString("CONNECTOR.NOTES.GETIDINFO.INFO", noteID));
			}

			foundDoc = db.getDocumentByID(noteID);

			if (foundDoc == null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.NOTES.DOCUMENTNOTFOUNDBYID.INFO", noteID));
				}
				return;
			}

		} catch (NotesException err) {
			Trace.exception(this, "setCurrent", err, "setCurrent noteID: " + noteID);
			logmsg(sResHash.getString("CONNECTOR.NOTES.GETDOCBYID.WARNING", new Object[] { noteID, err.toString(), err.text }));
		}
		Trace.exitmax(this, "setCurrent");
	}

	/**
	 * This method modifies a Domino database document.
	 *
	 * @param doc
	 *            the database document to modify
	 * @param entry
	 *            the entry which supplies the new values for the document to be
	 *            modified
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void modDocument(Document doc, Entry entry) throws Exception {
		executeCommand(COMMAND_MOD_DOC, new Object[] { doc, entry }, false);
	}

	/**
	 * This method modifies a Domino database document.
	 *
	 * @param doc
	 *            the database document to modify
	 * @param entry
	 *            the entry which supplies the new values for the document to be
	 *            modified
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private void inner_modDocument(Document doc, Entry entry) throws Exception {
		Trace.entrymax(this, "modDocument", doc, entry);

		String[] field = entry.getAttributeNames();

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.DOCUMENTUPDATENUM.INFO", "" + field.length));
		}
		for (int i = 0; i < field.length; i++) {

			String name = (String) field[i];
			Attribute a = entry.getAttribute(name);

			if (a.size() > 1) {
				Vector<Object> v = new Vector<Object>();
				for (int x = 0; x < a.size(); x++) {
					if (a.getValueOper(x) != AttributeValue.AV_DELETE)
						v.add(a.getValue(x));
				}
				doc.replaceItemValue(name, v);
			} else {
				if (debugMode()) {
					if (a.getValue(0) != null) {
						logmsg(sResHash.getString("CONNECTOR.NOTES.UPDATEATTR.ERROR", new Object[] { name, a.getValue(0),
								a.getValue(0).getClass().getName() }));
					} else {
						logmsg(sResHash.getString("CONNECTOR.NOTES.UPDATEATTRWCLASS.ERROR", new Object[] { name, a.getValue(0) }));
					}
				}

				// If the attribute is RichTextItem we handle it more specific
				if (a.getValue(0) instanceof RichTextItem) {
					RichTextItem rti = (RichTextItem) doc.getFirstItem(name);

					// if the item does not exist - create it as
					// RichTextItem. Otherwise it is cleared
					if (rti == null) {
						rti = doc.createRichTextItem(name);
					} else {
						rti = (RichTextItem) doc.replaceItemValue(name, "");
					}

					rti.appendRTItem((RichTextItem) a.getValue(0));
				} else {
					doc.replaceItemValue(name, a.getValue(0));
				}
			}
		}

		try {
			// If we execute this code in the DominoConnector's inheritor -
			// DominoAdminPConnector
			// then all fields of the document should be signed
			// in order to be further processed by the AdminP process
			if (this instanceof DominoAdminPConnector) {
				Vector<?> v = doc.getItems();
				for (int i = 0; i < v.size(); i++) {
					Item it = (Item) v.get(i);
					it.setSigned(true);
				}
				doc.sign();
			}

			if (!doc.save()) {
				throw new Exception(sResHash.getString("CONNECTOR.NOTES.NOSAVE.EXCEPTION"));
			} else {
				doc.recycle();
			}
		} catch (lotus.domino.NotesException dn) {
			Trace.exception(this, "modDocument", dn, "Lotus Domino Exception Text: " + dn.text);
			logmsg(sResHash.getString("CONNECTOR.NOTES.NOSAVE.ERROR", dn.text));
			throw dn;
		}
		Trace.exitmax(this, "modDocument");
	}

	/**
	 * Adds a feature to the Value attribute of the DominoConnector object
	 *
	 * @param e
	 *            {@link Entry}
	 * @param attr
	 *            attribute name
	 * @param value
	 *            attribute value
	 * @param type
	 *            attribute type
	 *
	 */
	public void addValue(Entry e, String attr, Vector value, int type) {
		Trace.entrymax(this, "addValue");
		Attribute a = e.getAttribute(attr);
		if (a == null) {

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.ADDATTR.INFO", attr));
			}
			a = new Attribute(attr);
		}

		if (value == null) {

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.NOTES.ADDATTRNULL.INFO"));
			}
			// Null behavior is governed by upper layers ...
			e.setAttribute(a);
			// e.setAttribute (attr, "");
			return;
		}

		if (debugMode()) {
			logmsg(sResHash.getString("CONNECTOR.NOTES.ADDATTRVALUE.INFO", value.toString()));
		}

		for (int i = 0; i < value.size(); i++) {
			switch (type) {

			case Item.NUMBERS:
				Double dVal = (Double) value.get(i);
				double doubleVal = dVal.doubleValue();
				int intVal = dVal.intValue();

				if (doubleVal != intVal)
					a.addValue(dVal);
				else
					a.addValue(Integer.valueOf(intVal));
				break;
			default:
				a.addValue(value.get(i));
			}
		}

		e.setAttribute(a);
		Trace.exitmax(this, "addValue");
	}

	/**
	 * This methods builds and returns a collection of the available Domino
	 * databases. In order to successfully retreive a list of the databases
	 * through <b>IIOP session</b> the option <b>"Allow HTTP clients to browse
	 * databases"</b> should be set to <b>"yes"</b>. This option can be
	 * configured with the Domino Admin application. It is situated:
	 * <b>"Configuratin" page -> "Internet Protocols" tab -> "HTTP" sub-tab ->
	 * "R5 Basics" sectin</b>
	 *
	 * @return a collection of the available Domino databases
	 * @exception Exception
	 *                if this method fails.
	 */
	public Vector<String> queryDatabases() throws Exception {
		return (Vector<String>) executeCommand(COMMAND_QUERY_DATABASE, null, true);
	}

	/**
	 * Inner implementation of the queryDatabase method executed by the worker
	 * thread.
	 *
	 * @return a collection of the available Domino databases
	 * @throws Exception
	 *             if this method fails.
	 */
	private Vector<String> inner_queryDatabases() throws Exception {
		Trace.entrymax(this, "queryDatabases");
		Vector<String> list = new Vector<String>();
		Database db;

		if (session == null) {
			return null;
		}

		DbDirectory dir = session.getDbDirectory("");
		if (dir == null) {
			return null;
		}

		db = dir.getFirstDatabase(DbDirectory.DATABASE);

		while (db != null) {
			list.add(db.getFilePath());
			db = dir.getNextDatabase();
		}

		Collections.sort(list);
		Trace.exitmax(this, "queryDatabases", list);

		return list;
	}

	/**
	 * Gets the list of views' names for the current Domino database.
	 *
	 * @return the list of views' names for the current Domino database
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Vector<String> queryViews() throws Exception {

		return (Vector<String>) executeCommand(COMMAND_QUERY_VIEWS, null, true);
	}

	/**
	 * Gets the list of views' names for the current Domino database.
	 *
	 * @return the list of views' names for the current Domino database
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	private Vector<String> inner_queryViews() throws Exception {

		Trace.entrymax(this, "queryViews");
		if (db != null) {
			Vector<View> views = db.getViews();
			Vector<String> v = new Vector<String>();

			for (int i = 0; i < views.size(); i++) {
				v.add((views.get(i)).getName());
			}

			Trace.exitmax(this, "queryViews", v);
			return v;
		} else {
			Trace.exitmax(this, "queryViews", null);
			return null;
		}
	}

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
		Trace.entrymax(this, "initLocalThread");

		String sessionType = getParam("dominoSessionType");
		if (!"LocalClient".equalsIgnoreCase(sessionType) && !"LocalServer".equalsIgnoreCase(sessionType)) {
			Trace.exitmax(this, "initLocalThread");
			return;
		}

		Object nt;

		try {
			nt = Class.forName("lotus.domino.NotesThread").newInstance();
		} catch (ClassNotFoundException cnf) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.THREADCLASS.EXCEPTION", cnf));
		}

		Method m = nt.getClass().getDeclaredMethod("sinitThread", new Class[] {});
		m.invoke(nt, new Object[0]);

		localThread = nt;
		Trace.exitmax(this, "initLocalThread");

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.THREADSTART.INFO"));
		}
	}

	/**
	 * This method terminates the Notes API thread to make sure resources are
	 * cleaned up properly. This method is usually called from the terminate
	 * method. It is not recomended to use it directly.
	 */
	public void termLocalThread() {
		Trace.entrymax(this, "termLocalThread");
		if (localThread == null) {
			return;
		}

		try {
			Method m = localThread.getClass().getDeclaredMethod("stermThread", new Class[] {});
			m.invoke(localThread, new Object[0]);
		} catch (Exception err) {
			logmsg(sResHash.getString("CONNECTOR.NOTES.STOPTHREAD.WARNING", err.toString()));
		}
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.NOTES.THREADTERMINATE.INFO"));
		}
		localThread = null;
		Trace.exitmax(this, "termLocalThread");
	}

	/**
	 * Kick off the internal worker thread.
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
						inner_modEntry((Entry) mNotesThreadCallArgList[0], (SearchCriteria) mNotesThreadCallArgList[1],
								(Entry) mNotesThreadCallArgList[2]);
						break;

					case COMMAND_PUT_ENTRY:
						inner_putEntry((Entry) mNotesThreadCallArgList[0]);
						break;

					case COMMAND_DELETE_ENTRY:
						inner_deleteEntry((Entry) mNotesThreadCallArgList[0], (SearchCriteria) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_TERMINATE:
						terminate = true;
						inner_terminate();
						break;

					case COMMAND_QUERY_DATABASE:
						mNotesThreadCallReturnValue = inner_queryDatabases();
						break;

					case COMMAND_QUERY_VIEWS:
						mNotesThreadCallReturnValue = inner_queryViews();
						break;

					case COMMAND_SET_CURRENT:
						inner_setCurrent((Entry) mNotesThreadCallArgList[0], (SearchCriteria) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_GET_DOMINO_VIEW:
						mNotesThreadCallReturnValue = inner_getDominoView((String) mNotesThreadCallArgList[0]);
						break;

					case COMMAND_MOD_DOC:
						inner_modDocument((Document) mNotesThreadCallArgList[0], (Entry) mNotesThreadCallArgList[1]);
						break;

					case COMMAND_BUILD_ENTRY:
						mNotesThreadCallReturnValue = inner_buildEntry((Document) mNotesThreadCallArgList[0]);
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
	 * Checks whether notes thread is alive.
	 *
	 * @return <code>true</code> if the Notes thread is not <code>null</code>
	 *         and is alive, <code>false</code> otherwise.
	 */
	private boolean isNotesThreadAlive() {
		return (mNotesThread != null && mNotesThread.isAlive());
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
	private synchronized Object executeCommand(int aCommand, Object[] aArgList, boolean aHasReturnValue) throws Exception {
		if (!isNotesThreadAlive()) {
			throw new Exception(sResHash.getString("CONNECTOR.NOTES.THREADDOWN.EXCEP"));
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
				throw new Exception(sResHash.getString("CONNECTOR.NOTES.FATAL.EXCEP", callError.toString()));
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

}
