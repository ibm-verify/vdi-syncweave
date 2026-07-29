/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.Log;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.SearchCriteria;

/**
 * The interface object which all Connectors should implement.
 * 
 * @see Connector
 */
public interface ConnectorInterface {

	/**
	 * Set the name for the connector. This name is used by the logmsg/debug
	 * functions.
	 * 
	 * @param name
	 *            The name for the connector
	 */
	public void setName(String name);

	/**
	 * Returns the name for this connector.
	 * 
	 * @return The connector's name
	 */
	public String getName();

	/**
	 * Adds or replaces a connector configuration parameter.
	 * 
	 * @param param
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 */
	public void setParam(String param, String value);

	/**
	 * Returns the value of a parameter as a java.lang.String object.
	 * 
	 * @param param
	 *            The connector configuration parameter name
	 * @return The parameter's value or null if no such parameter exists
	 */
	public String getParam(String param);

	/**
	 * Sets the configuration for use by this connector.
	 * 
	 * @param config
	 *            The configuration object (an instance of ConnectorConfig)
	 */
	public void setConfiguration(Object config);

	/**
	 * Returns the current configuration for this connector
	 * 
	 * @return The configuration object (an instance of ConnectorConfig), or
	 *         null if the configuration is not established
	 */
	public Object getConfiguration();

	/**
	 * Returns the current configuration for the raw connector
	 * 
	 * @return The configuration object or null if the configuration is not
	 *         established
	 */
	public BaseConfiguration getRawConnectorConfiguration();

	/**
	 * Sets the RSInterface for this connector to use. The RSInterface object is
	 * used to get access system wide parameters such as parser configurations.
	 * 
	 * @param rsi
	 *            The RSInterface object
	 */
	public void setRSInterface(RSInterface rsi);

	/**
	 * Sets the Log object to use for logging messages
	 * 
	 * @param logger
	 *            The {@link Log} object
	 * @see RSInterface#logmsg(String)
	 */
	public void setLog(Log logger);

	/**
	 * Sets the maximum number of duplicate entries to buffer up. This is used
	 * by the lookup functions when a lookup returns more than one entry.
	 * 
	 * @param mde
	 *            The maximum number of entries to buffer up
	 */
	public void setMaxDuplicateEntries(int mde);

	/**
	 * Returns the current maximum number of duplicate entries buffered up by
	 * the connector.
	 * 
	 * @return Max number of duplicate entries
	 */
	public int getMaxDuplicateEntries();

	/**
	 * This method is called to register specific objects in the script context.
	 * If the connector has a parser, the parser's registerScriptBeans method is
	 * also called.
	 * 
	 * @param se
	 *            The script engine context
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception;

	/**
	 * Discover the operations for a connection given the provided
	 * configuration. The implementation should update the configuration object
	 * with available operations. If this method requires the connector to be
	 * initialized then the connector must perform init/terminate operations
	 * itself.
	 * 
	 * @param config
	 *            The configuration
	 * @exception Exception
	 *                if an error occurs in the derived class implementation.
	 */
	public void queryOperations(ConnectorConfig config) throws Exception;

	/**
	 * This function translates to whatever means a connector has to discover
	 * schema for a connection. The connector may implement this, in which case
	 * a Vector of Entry objects is returned for each column/attribute it
	 * discovered. For a database connector this would typically be column names
	 * and their attributes.
	 * <p>
	 * 
	 * Each Entry in the Vector returned should contain the following
	 * attributes:
	 * <p>
	 * <table border="1">
	 * <tr>
	 * <th>Name</th>
	 * <th>Value</th>
	 * </tr>
	 * <tr>
	 * <td>name</td>
	 * <td>The name of the column/attribute/field ....</td>
	 * </tr>
	 * <tr>
	 * <td>syntax</td>
	 * <td>The syntax or expected value type</td>
	 * </tr>
	 * <tr>
	 * <td>size</td>
	 * <td>If specified this will give the user a hint as to how long the field
	 * may be</td>
	 * </tr>
	 * </table>
	 * <p>
	 * 
	 * @param source
	 *            The object on which to discover schema. This may be an Entry
	 *            or a string value
	 * @return A Vector of com.ibm.di.entry.Entry objects describing each entity
	 * @see com.ibm.di.entry.Entry
	 * @see java.util.Vector
	 * @throws Exception
	 *             if an error while retrieving the schema occurs.
	 */
	public Object querySchema(Object source) throws Exception;

	/**
	 * Initialize the connector. The connector may be passed a parameter of any
	 * kind by the user. It is up to the connector to determine whether this
	 * object can be used or not. The parameter is typically provided by a user
	 * script. When an AssemblyLine initializes it's Connectors, they are passed
	 * a ConnectorMode object.
	 * 
	 * @param o
	 *            User provided parameter
	 * @throws Exception
	 *             if the initialization of this connector fails.
	 */
	public void initialize(Object o) throws Exception;

	/**
	 * Terminate the connector. This function closes all connection and releases
	 * all resources used by the connector. This function also calls the
	 * parser's closeParser() method if a parser is active.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void terminate() throws Exception;

	/**
	 * Returns true if the exception is considered to be fatal. This governs
	 * whether the AssemblyLine logs the error as a warning or terminates. By
	 * default all exceptions are fatal.
	 * 
	 * @param e
	 *            The exception object
	 * @return whether the exception is fatal or not
	 */
	public boolean isExceptionFatal(Exception e);

	/**
	 * Server mode - return a clone of self that handles the next client
	 * instance when running in server mode. The returned connector may be used
	 * in its own thread to handle a "client" request so if the returned
	 * instance is returned more than once it must be thread safe.
	 * 
	 * @return the clone of itself
	 * @throws Exception
	 *             if an error occurs.
	 */
	public ConnectorInterface getNextClient() throws Exception;

	/**
	 * Prepare the Connector for sequential read. If necessary, create a result
	 * set to be used for getNextEntry(). When the Connector is used as an
	 * Iterator in an AssemblyLine, this method will be called. Default is an
	 * empty method.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void selectEntries() throws Exception;

	/**
	 * Returns the next Entry from the connector. The entry is populated with
	 * attributes and values from the next entry in the input set.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ctor = input.getConnector();
	 * var entry = ctor.getNextEntry();
	 * 
	 * for (; entry != null; entry = ctor.getNextEntry()) {
	 * 	main.logmsg(&quot;Read entry...&quot;);
	 * 	main.dumpEntry(entry);
	 * }
	 * </pre>
	 * 
	 * @return - the next Entry, or null if no more data
	 * @see #selectEntries()
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception;

	/**
	 * Returns the first entry from the list of duplicate entries found. Also
	 * resets the counter for getNextFindEntry
	 * 
	 * @return The first entry from the list or null if list is empty
	 * @exception Exception
	 *                if an error occurs.
	 */
	public Entry getFirstFindEntry() throws Exception;

	/**
	 * Returns the next entry from the list of duplicate entries found.
	 * 
	 * @return The next entry from the list or null if list is empty or at the
	 *         end
	 * @exception Exception
	 *                if an error occurs.
	 */
	public Entry getNextFindEntry() throws Exception;

	/**
	 * Adds a new entry to the data source
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ctor = write.getConnector();
	 * 
	 * for (i = 0; i &lt; 10; i++) {
	 * 	var entry = system.newEntry();
	 * 	entry.setAttribute(&quot;linenumber&quot;, i);
	 * 	entry.setAttribute(&quot;line&quot;, i + &quot; line of text...&quot;);
	 * 
	 * 	main.logmsg(&quot;Writes entry to output...&quot;);
	 * 	main.dumpEntry(entry);
	 * 	ctor.putEntry(entry);
	 * }
	 * </pre>
	 * 
	 * @param entry
	 *            The entry data to add
	 * @exception Exception
	 *                if an error occurs.
	 */
	public void putEntry(Entry entry) throws Exception;

	/**
	 * Send a reply to the connector.
	 * 
	 * @param entry
	 *            the information as an Entry
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void replyEntry(Entry entry) throws Exception;

	/**
	 * Modifies an existing entry. The new entry data is given by the <i>entry</i>
	 * parameter and the search criteria specifies which entry to modify.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @exception Exception
	 *                if an error occurs.
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception;

	/**
	 * Modifies an existing entry. The new entry data is given by the <i>entry</i>
	 * parameter and the search criteria specifies which entry to modify.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @param old
	 *            The old entry found by the search criteria
	 * @exception Exception
	 *                if an error occurs.
	 * @since 5.1
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old)
			throws Exception;

	/**
	 * Deletes an existing entry. The search criteria specifies which entry to
	 * modify. Some connectors may silently ignore the search criteria. For
	 * example, the LDAP connector will use the distinguished name ($dn) from
	 * the <i>entry</i> parameter (if it exists) rather than expanding the
	 * search criteria and search for the entry. Each connector's inner
	 * semantics governs whether the <i>search</i> parameter is used or not.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be deleted
	 * @exception Exception
	 *                if an error occurs.
	 */
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws Exception;

	/**
	 * Finds an existing entry. The search criteria specifies which entry to
	 * locate
	 * <p>
	 * Here is an example of how to find all people with names starting with 'J'
	 * which are from IBM organization in US.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ctor = input.getConnector();
	 * var crit = new com.ibm.di.SearchCriteria(&quot;$dn&quot;,
	 * 		com.ibm.di.SearchCriteria.SUBSTRING, &quot;c=US,o=IBM&quot;);
	 * crit.addCriteria(&quot;name&quot;, com.ibm.di.SearchCriteria.INITIAL_STRING, &quot;J&quot;);
	 * crit.addCriteria(&quot;objectclass&quot;, com.ibm.di.SearchCriteria.SUBSTRING, &quot;person&quot;);
	 * 
	 * var res = ctor.findEntry(crit);
	 * if (res != null) {
	 * 	main.logmsg(&quot;Found entry:&quot;);
	 * 	main.dumpEntry(res);
	 * } else {
	 *  if( getFindEntryCount()> 1 ){
	 *   main.logmsg(&quot;Found these entries:&quot;);
	 *   while ( (entry = ctor.getNextFindEntry()) != null ) {
	 *     main.dumpEntry(entry);
	 *   }
	 *  }
	 *  else {
	 * 	 main.logmsg(&quot;Entry not found!&quot;);
	 *  }
	 * }
	 * </pre>
	 * 
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @return The entry found, or null if no or multiple entries found
	 * @exception Exception
	 *                if an error occurs.
	 */
	public Entry findEntry(SearchCriteria search) throws Exception;

	/**
	 * Performs a query/reply operations.
	 * 
	 * @param entry
	 *            The data used in outgoing call
	 * @return The entry returned by the peer
	 * @exception Exception
	 *                if an error occurs.
	 */
	public Entry queryReply(Entry entry) throws Exception;

	/**
	 * Returns the number of duplicate entries in the list.
	 * 
	 * @return Number of duplicate entries in the list
	 */
	public int getFindEntryCount();

	/**
	 * Change the SearchCriteria search to find the entry sent as a parameter.
	 * Used when multiple entries found, and you want to modify or delete one of
	 * them. Only a few connectors need to implement this.
	 * 
	 * @param entry
	 *            The entry we want to find for modification/delete
	 * @param search
	 *            The SearchCriteria we want to change
	 */
	public void setCurrent(Entry entry, SearchCriteria search);

	/**
	 * Returns the pushed back entry for the connector.
	 * 
	 * @return The pushed back entry or null if no such entry exists.
	 * @see #pushback( Entry )
	 */
	public Entry getPushbackEntry();

	/**
	 * Make an entry the next entry to be iterated from the connector. This
	 * method along with getPushbackEntry is used by the AssemblyLineComponent
	 * when it iterates the connector. A connector may read one record and then
	 * push it back for the subsequent {@link #getNextEntry()} call. The task
	 * component first checks if there is a pushback entry available, and if so,
	 * returns that entry as the next input entry.
	 * 
	 * @param e
	 *            The entry to be returned at the next {@link #getNextEntry()}
	 *            call
	 */
	public void pushback(Entry e);

	/**
	 * Reconnect to the underlying data source
	 * 
	 * @param o
	 *            User provided parameter, which is sent to initialize()
	 * @see #initialize(Object)
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void reconnect(Object o) throws Exception;

	/**
	 * Returns true if this connector is able to perform delta updates
	 * 
	 * @return true if delta updates are supported, false otherwise
	 */
	public boolean isDeltaSupported();

	/**
	 * Returns <code>true</code> if this connector considers the
	 * {@link Throwable} to be an {@link IOException}. This is needed because
	 * we only try reconnection for IO Exceptions, and some Connectors do not
	 * return an IOException for an IO Exception.
	 * 
	 * @param e
	 *            The {@link Throwable} to be determined
	 * @return true if this Throwable is an IO Exception
	 */
	public boolean isIOException(Throwable e);

	/**
	 * Interrupts and shuts down the Connector if it runs in Server Mode. Does
	 * not have effect if the Connector is not running in Server Mode.
	 * 
	 * @throws Exception
	 *             if an error occurs
	 */
	public void terminateServer() throws Exception;

	/**
	 * Returns the parent task of the connector - an {@link AssemblyLine}
	 * object.
	 * <p>
	 * Here is an example hook of how to print the names of all enabled or
	 * passive components in the current running assembly line.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * var ctx = thisConnector.getConnector().getContext();
	 * var clist = ctx.getConnectors();
	 * 
	 * for (i = 0; i &lt; clist.size(); i++)
	 * 	main.logmsg(clist.get(i).getName());
	 * </pre>
	 * 
	 * @return the context to which the connector belongs
	 */
	public Object getContext();

	/**
	 * Sets the parent task of the connector - an {@link AssemblyLine} object.
	 * 
	 * @param aContext
	 *            the new context to which the connector will belong.
	 */
	public void setContext(Object aContext);
}
