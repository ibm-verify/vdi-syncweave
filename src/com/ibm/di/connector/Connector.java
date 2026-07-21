/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.CommunicationException;
import javax.swing.JComponent;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.ServerConstants;
import com.ibm.di.server.VersionInfoInterface;
import com.ibm.di.server.AssemblyLine;

/**
 * The Connector class provides a default implementation of the
 * ConnectorInterface. <h4>Overview</h4> This class includes useful methods for
 * sub-classing connectors. In general, you should sub-class this class when you
 * write new connectors since it provides a default implementation for most
 * methods. You only have to override data access methods (e.g. getNextEntry
 * etc..) to build a complete connector that handles logging and configuration
 * management. If for some reason you cannot sub-class you could implement the
 * ConnectorInterface instead. This requires more coding though.
 * 
 * <h4>Search Criteria</h4> Worth mentioning is the usage of the SearchCriteria
 * class. This class is used as a parameter to various methods (findEntry,
 * deleteEntry ...) and is meant to provide enough information for the connector
 * to identify a single entry in the connected data source. However, some
 * connectors, because of their inner semantics, may not use this parameter if
 * the entry object passed to it contains enough information to identify the
 * target entry. The LDAP connector will typically use the provided
 * distinguished name ($dn) rather than performing a search to obtain the $dn.
 * It is all about unique identifiers really. If the provided <i>Entry</i>
 * object contains a unique identifier the connector should use this, if no
 * unique identifier is provided the connector should resort to the information
 * passed in the <i>search</i> entry to locate the target entry.
 * 
 * <h4>Dealing with multiple entries</h4> Methods like modEntry and deleteEntry
 * are supposed to modify and delete exactly one single entry. However, there is
 * nothing that prevents a connector from deleting multiple entries but <b>this
 * should be made thoroughly clear</b> in the documentation for the connector. A
 * connector could either delete a single entry if the entry object passed to it
 * contained a unique identifier or the connector could delete all entries found
 * by the search criteria if no unique identifier was passed to it. Again, this
 * kind of connector behavior should be documented well since existing
 * components assume single entry behavior.
 * <p>
 * If the entry/search results in multiple candidates for modify, delete etc the
 * connector should use the <i>clearFindEntries</i> and <i>addFindEntry</i>
 * methods to signal that the connector was unable to identify a single target
 * entry. For example, the <i>getFindEntryCount</i> method is used by the
 * AssemblyLine to detect this condition and branch off to the <i>Multiple
 * Entries Found</i> hook so the user can deal with those situations. You should
 * also use the <i>getMaxDuplicateEntries</i> method to obtain the maximum
 * number of entries the user wants back in this case.
 * 
 * <h4>Using Parsers</h4>
 * If the connector is a transport connector the connector also needs a parser.
 * One such connector is the FileSystem connector that only provides input and
 * output streams. You can use parsers explicitly by loading them or you could
 * include a connector parameter called <i>parser</i> in which case this class
 * will do the loading and initialization of the parser. You only have to call
 * the <i>initParser</i> method and pass the input and/or output streams the
 * parser should use. Then, in your <i>getNextEntry</i> method you call the
 * <i>getParser().readEntry()</i> or <i>getParser().writeEntry(entry)</i>
 * methods. You can also intercept the parser before and after it has
 * read/written an entry. The parser uses buffered readers and writers so if you
 * read or write to the streams used by the parser you should always get the
 * reader/writer objects and use them (e.g. getParser().getWriter() and
 * getParser().getReader()). If not, you may accidently write data to the output
 * stream before the buffered writer has flushed its data to the output stream.
 * See the ParserImpl class for input/output stream behavior.
 */

public abstract class Connector implements ConnectorInterface, VersionInfoInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Deprecated since 7.2
	 */
	@Deprecated
	public final static String PROPERTY_MESSAGE = "$MESSAGE";

	/**
	 * Deprecated since 7.2
	 */
	@Deprecated
	public final static String PROPERTY_READER = "$READER";

	/**
	 * Deprecated since 7.2
	 */
	@Deprecated
	public final static String PROPERTY_WRITER = "$WRITER";

	/**
	 * String array containing all of the available modes.
	 */
	public final static String[] ALL_MODES = new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.DELETE_MODE,
			ConnectorConfig.ITERATOR_MODE, ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE, ConnectorConfig.DELTA_MODE,
			ConnectorConfig.CALL_REPLY_MODE, ConnectorConfig.SERVER_MODE };

	/**
	 * The connector name
	 */
	private String myName;

	/**
	 * Possible modes for this Connector
	 */
	private Vector<String> myModes;

	/**
	 * The connector's configuration
	 */
	private ConnectorConfig myConfiguration;

	/**
	 * The log object for logging messages
	 */
	protected Log myLog;

	/**
	 * The ResourceHash object for translating strings
	 */
	private ResourceHash myRes = ResourceHash.getHash("miserver");

	/**
	 * The parser instance if the connector has a parser in the configuration
	 */
	private ParserInterface myParser;

	/**
	 * Debug flag. May be accessed by different threads.
	 */
	private volatile boolean myDebug = false;

	/**
	 * The RSInterface from which we get additional configuration data
	 */
	private RSInterface myRS;

	/**
	 * The pushback entry might be used to peek at the next entry and then
	 * return it on the next getnext call.
	 */
	private Entry myPushbackEntry = null;

	/**
	 * Max duplicated entries collected by a connector during lookups with
	 * multiple entries found.
	 */
	private int myMaxDuplicateEntriesReturned = 10;

	/**
	 * This vector contains maxdupentries Entry objects after a lookup
	 */
	private Vector<Object> myFindEntries = new Vector<Object>();

	/**
	 * Current index when using getnextfindentry
	 */
	private int myFindIndex = 0;

	/**
	 * Connector properties
	 */
	private Hashtable<Object, Object> myProperties = null;

	/**
	 * Context object.
	 */
	private Object myContext = null;

	/**
	 * Default constructor. Allocates the properties table for use by
	 * getProperty/setProperty methods.
	 */
	public Connector() {
		myProperties = new Hashtable<Object, Object>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLog(Log logger) {
		myLog = logger;
	}

	/**
	 * Returns the current Log object.
	 * 
	 * @return The current Log object or null if no log object exists.
	 * @see com.ibm.di.server.Log
	 */
	public Log getLog() {
		return myLog;
	}

	/**
	 * Returns the value of a config parameter as a Boolean
	 * 
	 * @param p1
	 *            the key that boolean value is mapped under.
	 * @return the Boolean value or null if not found.
	 */
	public Boolean getBoolean(Object p1) {
		Boolean boolVal = null;
		String p2 = getParam(p1.toString());
		if (p2 != null) {
			if (p2.equalsIgnoreCase("true"))
				boolVal = Boolean.TRUE;
			if (p2.equalsIgnoreCase("false"))
				boolVal = Boolean.FALSE;
		}

		return boolVal;
	}

	/**
	 * Return true if we have a configuration value for configName. The value
	 * must be present and have a value not equal to the empty string.
	 * 
	 * @param p1
	 *            the key name of the value
	 * 
	 * @return true if a value with that key is present, false otherwise.
	 */
	public boolean hasConfigValue(Object p1) {
		String str = getParam(p1.toString());
		return (str != null && str.length() > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMaxDuplicateEntries(int mde) {
		myMaxDuplicateEntriesReturned = mde;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxDuplicateEntries() {
		return myMaxDuplicateEntriesReturned;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getPushbackEntry() {
		Entry e = myPushbackEntry;
		myPushbackEntry = null;
		return e;
	}

	/**
	 * {@inheritDoc}
	 */
	public void pushback(Entry e) {
		myPushbackEntry = e;
	}

	/**
	 * Log a message to the connector's log. The message is prefixed by the
	 * connector's name.
	 * 
	 * @param msg
	 *            The message to write to the log
	 */
	public void logmsg(String msg) {
		if (myLog != null)
			myLog.loginfo(msg);
		else if (myRS != null)
			myRS.logmsg(msg);

	}

	/**
	 * Log a debug message to the connector's log
	 * 
	 * @param msg
	 *            The message to write to the log
	 */
	public void debug(String msg) {
		if (myLog != null) {
			myLog.logdebug(msg);
		} else if (myRS != null && myDebug) {
			myRS.logmsg(msg);
		}
	}

	/**
	 * Log an error message to the connector's log
	 * 
	 * @param msg
	 *            The message to write to the log
	 * @since 7.0
	 */
	public void logError(String msg) {
		if (myLog != null)
			myLog.logerror(msg);
		else if (myRS != null)
			myRS.logerror(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(String name) {
		myName = name;
		if (myLog != null)
			myLog.setPrefix("[" + name + "] ");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return myName;
	}

	/**
	 * Sets the connector's modes.
	 * 
	 * @param modes
	 *            The connector's modes
	 */
	public void setModes(Vector<String> modes) {
		myModes = modes;
	}

	/**
	 * Sets the connector's modes.
	 * 
	 * @param modes
	 *            The connector's modes
	 */
	public void setModes(String[] modes) {
		myModes = new Vector<String>(Arrays.asList(modes));
	}

	/**
	 * Returns the connector's modes.
	 * 
	 * @return The connector's modes
	 */
	public Vector<String> getModes() {
		return getModes(null);
	}

	/**
	 * Returns the connector's modes.
	 * 
	 * @param config
	 *            return the modes given this configuration
	 * @return The connector's modes
	 */
	public Vector<String> getModes(ConnectorConfig config) {
		if (myModes != null)
			return myModes;
		else
			return allModes();
	}

	/**
	 * Returns all Modes
	 * 
	 * @return All possible modes
	 */
	public static Vector<String> allModes() {
		return new Vector<String>(Arrays.asList(ALL_MODES));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParam(String param, String value) {
		myConfiguration.getConnectionConfig().setParameter(param, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getParam(String param) {
		if (myConfiguration == null)
			return null;

		return myConfiguration.getConnectionConfig().getStringParameter(param);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setConfiguration(Object config) {
		myConfiguration = (ConnectorConfig) config;

		boolean debug = myConfiguration.getConnectionConfig().getDebug(false);
		setDebugMode(debug);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getConfiguration() {
		return myConfiguration;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getRawConnectorConfiguration() {
		if (myConfiguration != null)
			return myConfiguration.getConnectionConfig();
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRSInterface(RSInterface rsi) {
		myRS = rsi;
	}

	/**
	 * Returns the current RSInterface object in use by this connector. If not
	 * set, return <code>com.ibm.di.server.RS.getServer()</code>
	 * 
	 * @return Connector's RSInterface object
	 */
	public RSInterface getRSInterface() {
		return myRS != null ? myRS : SystemFunctions.getServer();
	}

	/**
	 * Returns whether debug mode is set or not. May be accessed by different
	 * threads.
	 * 
	 * @return True if debug mode is set
	 */
	public boolean debugMode() {
		return myDebug;
	}

	/**
	 * <p>
	 * Modify the debug mode of the component. Also modifies the debug mode of
	 * the associated Parser. May be accessed by different threads.
	 * </p>
	 * 
	 * <p>
	 * This method is for internal use only. Do not call it from user code.
	 * </p>
	 * 
	 * @param debug
	 *            the debug mode setting
	 */
	public void setDebugMode(boolean debug) {
		myDebug = debug;
		ParserInterface p = getParser();
		if (p != null) {
			p.setDebug(myDebug);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExceptionFatal(Exception e) {
		return true;
	}

	/**
	 * Returns a class instance of the specified class name
	 * 
	 * @param className
	 *            The fully qualified Java class name
	 * @return A class object for the class
	 * @exception Exception
	 *                exception thrown by the class loader
	 */
	public static Object getClassInstance(String className) throws Exception {
		String cls = className;

		if (className.indexOf(".") == -1)
			cls = "com.ibm.di.connector." + className;

		Class<?> t1 = Class.forName(cls);
		return t1.newInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object o) throws Exception {
	}

	/**
	 * Check if the configuration includes a parser.
	 * 
	 * @return True if a parser is specified
	 */
	public boolean hasParser() {
		ParserConfig parserConfig = myConfiguration.getParserConfig();
		if (parserConfig != null) {
			return parserConfig.getStringParameter("class") != null;
		} else {
			return false;
		}
	}

	/**
	 * Initialize the connector's parser with input and output streams. If the
	 * parser has not been loaded then an attempt is made to load it. The input
	 * and output objects may be one of InputStream, OutputStream or Socket.
	 * 
	 * @param is
	 *            The input object.
	 * @param os
	 *            the output object.
	 * @exception Exception
	 *                if a parser is not configured or the exception is derived
	 *                from the parser
	 * @see #getParser
	 */
	public void initParser(Object is, Object os) throws Exception {
		if (myParser == null && !hasParser())
			throw new Exception(myRes.getString("this.connector.has.no.configured.parser"));

		if (myParser == null)
			myParser = (ParserInterface) SystemFunctions.loadParser(myConfiguration.getParserConfig());

		myParser.setContext(this);

		if (is instanceof InputStream)
			myParser.setInputStream((InputStream) is);
		else if (is instanceof Reader)
			myParser.setInputStream((Reader) is);
		else if (is instanceof Socket)
			myParser.setInputStream(((Socket) is).getInputStream());
		else if (is instanceof String)
			myParser.setInputStream(new StringReader((String) is));
		else if (is instanceof StringBuffer)
			myParser.setInputStream(new StringReader(((StringBuffer) is).toString()));
		else if (is != null && is.getClass().getName().equals("[C"))
			myParser.setInputStream(new CharArrayReader((char[]) is));
		else if (is != null && is.getClass().getName().equals("[B"))
			myParser.setInputStream(new ByteArrayInputStream((byte[]) is));
		else
			myParser.setInputStream((InputStream) null);

		if (os instanceof OutputStream)
			myParser.setOutputStream((OutputStream) os);
		else if (os instanceof Writer)
			myParser.setOutputStream((Writer) os);
		else if (os instanceof Socket)
			myParser.setOutputStream(((Socket) is).getOutputStream());
		else
			myParser.setOutputStream((OutputStream) null);

		myParser.setDebug(myDebug);

		myParser.initParser();
		if (getContext() instanceof AssemblyLine)
			registerScriptBeans(((AssemblyLine) getContext()).getScriptEngine());

	}

	/**
	 * Sets the connector's parser.
	 * 
	 * @param parser
	 *            The parser interface to use
	 * @throws IOException
	 *             if an error occurs.
	 */
	public void setParser(ParserInterface parser) throws IOException {
		myParser = parser;
	}

	/**
	 * Returns the parser interface used by this connector.
	 * 
	 * @return The parser interface object or null if no parser exists
	 */
	public ParserInterface getParser() {
		return myParser;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() throws Exception {
		if (myParser != null) {
			myParser.closeParser();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.getnextentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectorInterface getNextClient() throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.getnextclient"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void replyEntry(Entry entry) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.replyentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.modentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {
		modEntry(entry, search); // If not using old
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.deleteentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.findentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void putEntry(Entry entry) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.putentry"));
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry queryReply(Entry entry) throws Exception {
		throw new com.ibm.di.exceptions.UnsupportedOperation(myRes.getString("connector.does.not.support.queryreply"));
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getFirstFindEntry() throws Exception {
		myFindIndex = 0;
		return getNextFindEntry();
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextFindEntry() throws Exception {
		if (myFindIndex < getFindEntryCount())
			return (Entry) myFindEntries.get(myFindIndex++);
		else
			return null;
	}

	/**
	 * Locates an entry based on a key/value pair. The search is translated to
	 * the connector's format for searching.
	 * 
	 * @param key
	 *            The attribute name
	 * @param value
	 *            The attribute value match
	 * @return The entry found or null if no entries were found
	 */
	public Entry findEntry(Object key, Object value) {
		try {
			SearchCriteria sc = new SearchCriteria(key.toString(), SearchCriteria.EXACT, value);
			return findEntry(sc);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFindEntryCount() {
		return myFindEntries.size();
	}

	/**
	 * Removes all entries from the duplicate entry list.
	 * 
	 */
	public void clearFindEntries() {
		myFindEntries.removeAllElements();
		myFindIndex = 0;
	}

	/**
	 * Adds an entry to the list of duplicate entries found.
	 * 
	 * @param entry
	 *            The entry object to add
	 * @return true if the entry was successfully added, false if the passed
	 *         entry is <code>null</code> or the found entries are more than the
	 *         maximumDuplicatEntriesReturned limit.
	 * 
	 * 
	 */

	public boolean addFindEntry(Object entry) {
		if (entry == null)
			return false;
		if (myMaxDuplicateEntriesReturned > 0 && myFindEntries.size() >= myMaxDuplicateEntriesReturned)
			return false;
		return myFindEntries.add(entry);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCurrent(Entry entry, SearchCriteria search) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void queryOperations(ConnectorConfig config) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {

		Object result = null;

		if (getParser() != null) {
			result = getParser().querySchema(source);
		}

		return result;
	}

	/**
	 * This function translates to whatever means a connector have to discover
	 * "tables" for a connection. The connector may implement this in which case
	 * a Vector of strings is returned. For a database connector this would
	 * typically be a list of table/view names the user can access with the
	 * current set of parameters. For other connectors this might translate to
	 * different concepts.
	 * 
	 * @return A list of table names
	 * @see java.util.Vector
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Vector<String> queryTables() throws Exception {
		throw new Exception(myRes.getString("connector.does.not.support.querytables"));
	}

	/**
	 * Return a connector property. Each connector may have static or runtime
	 * properties which can be accessed with this method. Note that connector
	 * properties are very different from the connector configuration.
	 * 
	 * @param p1
	 *            The property name
	 * @return The property value/object
	 */
	public Object getProperty(Object p1) {
		if (myProperties != null)
			return myProperties.get(p1);
		else
			return null;
	}

	/**
	 * Set connector property. This method sets a named property to a given
	 * value. Note that connector properties are very different from the
	 * connector configuration.
	 * 
	 * 
	 * @param p1
	 *            The property name
	 * @param p2
	 *            The property value
	 */
	public void setProperty(Object p1, Object p2) {
		if (myProperties == null)
			myProperties = new Hashtable<Object, Object>();

		myProperties.put(p1, p2);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception {
		if (myDebug) {
			debug(myRes.getString("registerscriptbeans"));
		}
		if (getParser() != null) {
			getParser().registerScriptBeans(se);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void reconnect(Object o) throws Exception {
		terminate();
		initialize(o);
		if (o instanceof ConnectorMode && ((ConnectorMode) o).getMode() == ServerConstants.TYPE_ITERATOR)
			selectEntries();
	}

	/**
	 * Reconnect to the underlying data source.
	 * <p>
	 * The following code could be placed in the 'On Connection lost' hook to
	 * change the connector's <code>ldapUrl</code> to another server in case the
	 * connection is lost.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * thisConnector.setParam(&quot;ldapURL&quot;, &quot;ldap://backupserver.acme.com:389&quot;);
	 * 
	 * // reconnect to backup server
	 * thisConnector.reconnect();
	 * </pre>
	 * 
	 * @see #initialize(Object)
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void reconnect() throws Exception {
		reconnect(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDeltaSupported() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIOException(Throwable e) {
		return e instanceof IOException || e instanceof CommunicationException;
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminateServer() throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getContext() {
		return myContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContext(Object aContext) {
		myContext = aContext;
	}

	/**
	 * This method returns the user interface component presented to the user to
	 * configure the connector.
	 * 
	 * @return null by default, indicating no special user interface.
	 * @since 7.0
	 */
	public JComponent getUI() {
		return null;
	}

	/**
     * Extracts additional information about a connector specific exception.
     * The method will usually be called automatically to add information to  the <code>error</code>  Entry.
     * The default behavior is to do nothing.
     * @param error
     *            an Entry object containing the exception in its "exception"
     *            attribute.
     */
     public void extractExceptionInformation(Entry error) {
      }
	
}
