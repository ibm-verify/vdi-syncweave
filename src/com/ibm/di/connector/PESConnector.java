/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;
import com.ibm.di.store.DeltaStore;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * The PES Connector provides access to the underlying System Store. The primary
 * use of the System Store Connector/PES Connector is to store Entry objects
 * into the System Store tables.
 * 
 */
public class PESConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the properties file
	 */
	private static final String PROPERTIES_FILE = "systemstoreconnector";

	/**
	 * specifies read existing selection mode
	 */
	public final static int READ_EXISTING = 0;
	/**
	 * specifies read all selection mode
	 */
	public final static int READ_ALL = 1;
	/**
	 * specifies read deleted selection mode
	 */
	public final static int READ_DELETED = 2;

	// parameter list.
	/**
	 * name of the database
	 */
	private String sDBName;

	/**
	 * user name
	 */
	private String jdbcLogin;

	/**
	 * password
	 */
	private String jdbcPassword;

	/**
	 * key attribute name
	 */
	private String keyAttribute;

	/**
	 * DB name of the table
	 */
	private String dbTableName = null;

	/**
	 * logging object
	 */
	private Log log;
	// Connector flags
	/**
	 * specifies auto commit
	 */
	private boolean autoCommit = true;
	/**
	 * specifies commit on close
	 */
	private boolean commitOnClose = true;

	/**
	 * specifies auto commit all transactions
	 */
	private boolean autoCommitAll = false;

	/**
	 * mode of reading
	 */
	private int readMode;

	/**
	 * connection object
	 */
	private Connection conn;

	/**
	 * prepared statements for add
	 */
	private PreparedStatement insEntry;

	/**
	 * prepared statements for update
	 */
	private PreparedStatement updEntry;

	/**
	 * Statement for executing DB operations in Iterator mode
	 */
	private Statement stmt1 = null;

	/**
	 * Result returned from the DB(for iterator mode)
	 */
	private ResultSet rs1 = null;

	/**
	 * SQL map
	 */
	private SqlMap map1 = null;

	/**
	 * indicates end of cycle
	 */
	private boolean EOCFlag = false; // set the EOC FLAG

	/**
	 * Holds field name to DB type mappings
	 */
	private Map<String, Integer> nameToType = new Hashtable<String, Integer>();

	/**
	 * Delta store table prefix
	 */
	public static final String DELTA_PREFIX = DeltaStore.TABLE_PREFIX;
	/**
	 * Property store table prefix
	 */
	public static final String PS_PREFIX = PropertyStore.TABLE_PREFIX;

	/**
	 * separator for multi keys
	 */
	private String multiKeySep = "+";

	/**
	 * Vector holding the key attributes
	 */
	private Vector<String> keyAttributes;

	/**
	 * indicates if multi key attributes are used
	 */
	private boolean multiKeyAttrs = false;

	/**
	 * The SQL Statements used to create a new table in the System store
	 * database.
	 */
	private final static String CREATE_TABLE1 = "CREATE TABLE {0} (ID VARCHAR(" + StoreFactory.VARCHAR_LENGTH
			+ ") NOT NULL, ENTRY BLOB )";

	/**
	 * Alter table statement
	 */
	private final static String CREATE_TABLE2 = "ALTER TABLE {0} ADD CONSTRAINT {0}_PRIMARY Primary Key (ID)";

	/**
	 * statement separator
	 */
	private static final String SEPARATOR = ";";

	/**
	 * Resource Hash object for accessing TMS messages
	 */
	private final static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * indicates whether the System store is MS SQL DB
	 */
	private boolean isMSSQLServer = false;

	/**
	 * use wrapper/unwrapped entry
	 */
	private boolean m_wrappedEntry = false;

	/**
	 * indicates if the provided table name exists in the system store
	 */
	private boolean tableVerified = false;

	public String[] CONNECTOR_MODES = { 
			ConnectorConfig.ITERATOR_MODE,
			ConnectorConfig.ADDONLY_MODE, 
			ConnectorConfig.DELETE_MODE, 
			ConnectorConfig.LOOKUP_MODE, 
			ConnectorConfig.UPDATE_MODE };
	
	public String VERSION_INFO = "2.0-di7.1.1 %I% 20%E%";
	
	/**
	 * Constructor
	 */
	public PESConnector() {
		readMode = READ_ALL;

		setModes(CONNECTOR_MODES);
	}

	/**
	 * Method initializes the connector
	 * 
	 * @param p1
	 *            Entry object
	 * @exception Exception
	 *                Thrown if error occurs during initialization
	 */

	public void initialize(Object p1) throws Exception {
		Trace.entrymid(this, "initialize");
		try {
			log = getLog();
		} catch (Exception ignore) {
		}

		// Determine if new behvaiour or stay with old behvior
		m_wrappedEntry = determineIfReturnWrappedEntry();
		sDBName = getParam("sDBName");
		jdbcLogin = getParam("jdbcLogin");
		jdbcPassword = getParam("jdbcPassword");
		SQLWarning sqlwarn;

		if (sDBName == null || sDBName == "" || sDBName.length() <= 0)
			sDBName = StoreFactory.getDefaultDatabase();

		if (jdbcLogin == null || jdbcLogin.equals(""))
			jdbcLogin = StoreFactory.getJdbcUser();
		/*
		 * else { we cannot set default uid/passwds as it will break if DB2 is
		 * used as a system store. jdbcLogin = "APP"; }
		 */

		if (jdbcPassword == null || jdbcPassword.equals(""))
			jdbcPassword = StoreFactory.getJdbcPassword();
		/*
		 * else { we cannot set default uid/passwds as it will break if DB2 is
		 * used as a system store. jdbcPassword = "APP"; }
		 */

		// MS SQL Drivers (Type-4).
		isMSSQLServer = StoreFactory.isMSSQLDriver(StoreFactory.getJdbcDriver());

		String connMode = ((ConnectorConfig) getConfiguration()).getMode();

		keyAttribute = getParam("keyAttribute");

		if (keyAttribute == null || keyAttribute.length() == 0) {
			// 'keyAttribute' is not used in Iterator mode
			if (!ConnectorConfig.ITERATOR_MODE.equals(connMode))
				throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.MISSING.KEYATTRIBUTE"));
		} else if (keyAttribute.indexOf(multiKeySep) != -1) {
			multiKeyAttrs = true;
			keyAttributes = com.ibm.di.util.StringUtils.splitstring(keyAttribute, multiKeySep);
		} else {
			multiKeyAttrs = false;
		}

		String rdo = getParam("selectionMode");
		if (rdo != null && rdo.equalsIgnoreCase("existing"))
			readMode = READ_EXISTING;

		if (rdo != null && rdo.equalsIgnoreCase("all"))
			readMode = READ_ALL;

		if (rdo != null && rdo.equalsIgnoreCase("deleted"))
			readMode = READ_DELETED;

		String str = getParam("pesCommit");
		if (str != null && str.length() > 0 && !setCommitMode(str)) {
			logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.SETCOMMIT.WARN", str));
		}

		// If jdbcDriver is not specified use the configured in SystemStore
		// settings or a better match among the already loaded drivers
		String selectDBDriver = getParam("selectDBDriver");
		if (selectDBDriver == null || selectDBDriver.trim().length() <= 0) {
			// if not specified use the driver from SystemStore settings
			selectDBDriver = StoreFactory.getJdbcDriver();
		}

		// try to load user specified driver
		Class.forName(selectDBDriver);

		String createTable = getParam("createTable");
		if (createTable == null || createTable.trim().length() <= 0) {

			// If the user did not provide a createTable parameter
			// try to guess it using the driver name
			createTable = StoreFactory.getSysStoreCreateStmtByDriver(selectDBDriver);
			if (createTable != null) {
				// Set the createTable parameter explicitly for those
				// who use the connector in scripts
				setParam("createTable", createTable);
			} else {
				// The driver is not specified or unknown,
				// so createTable must be specified.
				throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.MISSING.CREATETABLE"));
			}
		}

		conn = StoreFactory.getConnection(sDBName, autoCommit, jdbcLogin, jdbcPassword, null);

		sqlwarn = conn.getWarnings();
		while (sqlwarn != null)
			sqlwarn = sqlwarn.getNextWarning();

		dbTableName = getParam("dbTableName");
		tableVerified = false;

		if (p1 != null)
			verifyTable();

		Trace.exitmid(this, "initialize");

	}

	/**
	 * Terminate the connector. This function closes all connection and releases
	 * all resources used by the connector.
	 * 
	 */
	public void terminate() {
		Trace.entrymid(this, "terminate");

		if (conn != null) {
			if ("true".equals(getParam("deleteOnClose")) && dbTableName != null) {
				dropPesTable(dbTableName);
			}

			try {
				if (!autoCommit) {
					if (commitOnClose) {
						conn.commit();
					} else {
						conn.rollback();
					}
				}
			} catch (SQLException se) {
				Trace.exception(this, "terminate", se, "exception while conn.commit()");
				logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.TERMBAD.WARN", se.toString()));
				se.printStackTrace();
			}
		}

		try {
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ignore) {
			logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.UNABLE.TO.CLOSE", ignore.getMessage()));
		}

		conn = null;
		Trace.exitmid(this, "terminate");

	}

	/**
	 * Drop table of given name.
	 * 
	 * Note: This method does not work for Delta and Property Store tables.
	 * 
	 * @param table
	 *            Table Name
	 */
	public void dropPesTable(String table) {
		Trace.entrymin(this, "dropPesTable");
		if (table != null && table.trim().length() > 0 && chkforDML(dbTableName))
			StoreFactory.dropTable(conn, table);
		Trace.exitmin(this, "dropPesTable");
	}

	/**
	 * Set selection mode
	 * 
	 * @param mode
	 *            mode to be set
	 */
	public void setSelectionMode(int mode) {
		readMode = mode;
	}

	/**
	 * Check whether DML operations are allowed on the specified table.
	 * 
	 * @param table
	 *            Table Name
	 * @return true; if the table is not a Delta, Property store or the Systable
	 */
	public boolean chkforDML(String table) {
		if (table.startsWith(DELTA_PREFIX) || table.startsWith(PS_PREFIX) || table.startsWith("IDI_SYS_TABLE")
				|| table.startsWith("IDI_DELTA_SYSTABLE"))
			return false;
		return true;
	}

	/**
	 * Generate unique key from list of key Attributes
	 * 
	 * @param e
	 *            Entry object
	 * @param keys
	 *            List of keys
	 * @return uniqueKey from list of keys
	 */

	public String getUniqueKey(Entry e, Vector<String> keys) {
		Trace.entrymax(this, "getUniqueKey");
		StringBuffer key = new StringBuffer();
		for (String ka : keys) {
			Attribute keyAttr = e.getAttribute(ka.trim());
			if (keyAttr == null)
				continue;
			else if (keyAttr.size() > 1) {
				logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.ENTRY.NOKEY", ka.trim()));
			}

			String kv = keyAttr.getValue();
			if (kv != null && kv.length() > 0)
				key.append(kv);
		}
		Trace.exitmax(this, "getUniqueKey", key);
		return key.toString();

	}

	/**
	 * Returns Attr name if not keyAttribute
	 * 
	 * @param entry
	 *            Entry object
	 * @return Returns a String object representing the name of the first
	 *         non-key Attribute in the supplied Entry. The key Attribute is
	 *         specified by the "keyAttribute" Connector configuration
	 *         parameter. If there are no non-key Attributes present in the
	 *         supplied Entry, then the String "ENTRY" is returned.
	 */
	public String getAttrName(Entry entry) {

		String[] names = entry.getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			if (!names[i].equalsIgnoreCase(getParam("keyAttribute")))
				return names[i];
		}
		return "ENTRY";

	}

	/**
	 * Adds a new entry.
	 * 
	 * @param entry
	 *            The entry object
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */

	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "putEntry", entry);

		if (entry == null) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.ADDENTRY.NULL"));
		}

		String key = "";

		if (multiKeyAttrs) {
			key = getUniqueKey(entry, keyAttributes);
		} else {
			key = entry.getString(keyAttribute);
		}

		verifyTable();

		if (!chkforDML(dbTableName)) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.NODML.ADDENTRY", dbTableName));
		}

		if (key == null) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.KEYATTRMISSING.ADDENTRY", keyAttribute));
		}

		String sql = "INSERT INTO " + dbTableName + " (ID, ENTRY) VALUES (?,?)";
		insEntry = conn.prepareStatement(sql);
		insEntry.setString(1, key);
		insEntry.setBytes(2, StoreFactory.serializeObject(entry));

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.PREPARED.STATEMENT.SQL", sql));
		}

		insEntry.executeUpdate();
		insEntry.close();
		Trace.exitmin(this, "putEntry");
	}

	/**
	 * Modify the entry in the System Store. The supplied entry should contain a
	 * the Attribute(s) which are modified. The old entry object has the
	 * attributes which are persisted in the System Store
	 * 
	 * @param entry
	 *            An Entry containing the new values to be set in the System
	 *            Store.
	 * @param search
	 *            Search Criteria used for updating the specific record in the
	 *            System Store.
	 * @param old
	 *            The old values persisted in the System Store.
	 * @exception Exception
	 *                If no distinguished record is found in the System Store.
	 */
	public void modEntry(Entry entry, SearchCriteria search, Entry old) throws Exception {

		Trace.entrymin(this, "modEntry", new Object[] { entry, search, old });

		verifyTable();

		if (!chkforDML(dbTableName)) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.NODML.MODENTRY", dbTableName));
		}

		if (entry == null || old == null) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.MODENTRY.NULL"));
		}

		String[] names = old.getAttributeNames();
		for (int iCnt = 0; iCnt < names.length; iCnt++) {
			String oldAttrName = names[iCnt];
			if (entry.getAttribute(oldAttrName) == null) {
				entry.setAttribute(old.getAttribute(oldAttrName));
			}
		}
		String sql = "UPDATE " + dbTableName + " SET ENTRY = ? WHERE ID = ?";
		updEntry = conn.prepareStatement(sql);

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.PREPARED.STATEMENT.SQL", sql));
		}

		updEntry.setString(2, search.getFirstCriteriaValue());
		updEntry.setBytes(1, StoreFactory.serializeObject(entry));
		updEntry.executeUpdate();

		updEntry.close();

		Trace.exitmin(this, "modEntry");
	}

	/**
	 * Modifies an existing entry. The new entry data is given by the <i>entry
	 * </i> parameter and the search criteria specifies which entry to modify.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * 
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void modEntry(Entry entry, SearchCriteria search) throws Exception {
		Trace.entrymin(this, "modEntry", entry, search);
		modEntry(entry, search, findEntry(search));
		Trace.exitmin(this, "modEntry");
	}

	/**
	 * Finds an existing entry. The search criteria specifies which entry to
	 * modify.
	 * 
	 * @param search
	 *            The search criteria used to locate the entry to be modified *
	 * @exception Exception
	 *                derived from the connector's underlying classes
	 * @return Entry obj
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		Trace.entrymin(this, "findEntry", search);
		Entry e = findEntryWithFlag(search, m_wrappedEntry);
		Trace.exitmin(this, "findEntry", null);
		return e;
	}

	/**
	 * Wrapped functionality for backward compatibility. See method findEntry( )
	 * and determineIfReturnWrappedEntry().Call this method with
	 * returnWrappedEntry to "true", and this method will be findEntry( )
	 * equivalent of TDI6.0 and prior. In TDI61, we wanted an unwrapped Entry,
	 * therefore the findEntry( ) method calls this with returnWrappedEntry set
	 * to "false".
	 * 
	 * @param search
	 *            search criteria
	 * @param returnWrappedEntry
	 *            type of the returned entry
	 * @return Entry {@link Entry} object
	 * 
	 * 
	 * @throws Exception
	 *             if an error occurs
	 * 
	 */
	public Entry findEntryWithFlag(SearchCriteria search, boolean returnWrappedEntry) throws Exception {
		clearFindEntries();
		String sql = getParam("searchFilter");

		verifyTable();

		if (sql == null || sql.length() < 1) {
			sql = "SELECT * FROM " + dbTableName;
		}

		sql += " WHERE ";

		ResultSet rs = null;
		Statement stmt = null;
		PreparedSql ps = null;

		try {
			String where = getWhereClause(search, true);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.FINDENTRYPS.INFO", sql + where));
			}
			ps = new PreparedSql(sql + where);
			ps.setValues(search);
			rs = ps.executeQuery();
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.NOPREPARED.STATEMENT.WARNING", ignore.getMessage()));
			}
		}

		try {
			if (rs == null) {

				String where = getWhereClause(search, false);
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.FINDENTRY", sql + where));
				}
				stmt = conn.createStatement();
				rs = stmt.executeQuery(sql + where);
			}

			SqlMap map = new SqlMap(rs);

			boolean checkAddEntry = addFindEntry(map.buildEntry());
			while (checkAddEntry) {
				checkAddEntry = addFindEntry(map.buildEntry());
			}

			if (getFindEntryCount() == 1) {
				Entry ret = getFirstFindEntry();
				// Modify the returned entry to return the "ENTRY" value from
				// this object,
				if (ret != null && !returnWrappedEntry && ret.getObject("ENTRY") instanceof Entry) {
					ret = (Entry) ret.getObject("ENTRY");
				}
				return ret;
			} else {
				return null;
			}

		} finally {
			if (ps != null) {
				ps.close();
			}
			if (stmt != null) {
				stmt.clearWarnings();
				stmt.close();
			}
		}

	}

	/**
	 * Deletes an existing entry. The search criteria specifies which entry to
	 * modify.
	 * 
	 * @param entry
	 *            The entry data
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @exception Exception
	 *                derived from the connector's underlying classes
	 */

	public void deleteEntry(Entry entry, SearchCriteria search) throws Exception {
		Trace.entrymin(this, "deleteEntry", entry, search);

		verifyTable();

		if (!chkforDML(dbTableName)) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.NODML.DELETEENTRY", dbTableName));
		}

		String sql = "DELETE FROM " + dbTableName + " WHERE "; // D533

		int retValue = -1;

		PreparedSql ps = null;
		try {
			ps = new PreparedSql(sql + getWhereClause(search, false));
			ps.setValues(search);
			retValue = ps.executeUpdate();
		} catch (Exception pse) {
			Trace.exception(this, "deleteEntry", pse, "Will not use prepared statement");
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.PREPSTATEMENT.NOTUSE.WARNING", pse.getMessage()));
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		if (retValue == -1) {
			Statement stmt = null;
			try {
				String where = getWhereClause(search, false);
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.DELETEENTRY", sql + where));
				}
				stmt = conn.createStatement();
				retValue = stmt.executeUpdate(sql + where);
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}

		Trace.exitmin(this, "deleteEntry", Integer.valueOf(retValue));
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.DELETE.RETURNED", Integer.valueOf(retValue)));
		}

	}

	/**
	 * Prepares for getNextEntry(). If no SQL stament is specified use
	 * <tt>"SELECT * FROM " + getParam("dbTableName")</tt> as default
	 * 
	 * @see #getNextEntry
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void selectEntries() throws Exception {
		Trace.entrymin(this, "selectEntries");
		String sql = getParam("searchFilter");

		if (sql == null || sql.length() < 1) {
			sql = "SELECT * FROM " + "" + dbTableName;
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.SELECTENTRIES.SQL", sql));
		}
		stmt1 = conn.createStatement();
		rs1 = stmt1.executeQuery(sql);
		if (rs1 == null) {
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.EXECUTE.SQL.ERROR", sql));
		}

		map1 = new SqlMap(rs1);

		if (autoCommitAll) {
			conn.commit();
		}

		Trace.exitmin(this, "selectEntries");

	}

	/**
	 * Returns the next entry from the result set created by selectEntries
	 * 
	 * @return The nextEntry value
	 * @exception Exception
	 *                Exception thrown during database operations
	 */
	public Entry getNextEntry() throws Exception {
		if (map1 == null) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.NO.ENTRIES.FROM.SELECTENTRIES"));
			}
			return null;
		}

		Entry entry = map1.buildEntry();

		if (entry == null || m_wrappedEntry || dbTableName.startsWith(DELTA_PREFIX))
			return entry;

		if (entry.getObject("ENTRY") instanceof Entry)
			return (Entry) entry.getObject("ENTRY");

		return entry;
	}

	/**
	 * Creates where clause for SQL statement
	 * 
	 * @param search
	 *            SearchCriteria
	 * @param ps
	 *            Boolean which determines if is a prepared statement.
	 * @return sql SQL string with generated where clause
	 * 
	 * @throws Exception
	 *             Any exceptions thrown by the connector's underlying classes
	 */
	public String getWhereClause(SearchCriteria search, boolean ps) throws Exception {
		Trace.entrymax(this, "getWhereClause", search, Boolean.valueOf(ps));
		String flt;
		String retValue;

		if (search.getSQLFilter() != null) {
			if (ps) {
				throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.CANNOTBUILD.ADVANCED.ERROR"));
			} else {
				String name = search.getFirstCriteriaName();
				if (name != null && name.equalsIgnoreCase(keyAttribute)) {
					Object obj = search.getCriteria().get(0);
					if (obj instanceof SearchCriteria.rscSearch) {
						((SearchCriteria.rscSearch) obj).name = "ID";
						return search.getSQLFilter();
					}
				}
				flt = search.getSQLFilter();
				return flt.replaceAll(getParam("keyAttribute"), "ID");
			}
		}
		retValue = search.getSQLFilter();
		Trace.exitmax(this, "getWhereClause", retValue);
		return retValue;

	}

	/**
	 * Saves the Meta Data info
	 * 
	 * @param md
	 *            ResultSetMetaData
	 * @throws SQLException
	 *             Any exceptions thrown by the connector's underlying classes
	 */

	public void saveMetaData(ResultSetMetaData md) throws SQLException {
		for (int i = 1; i <= md.getColumnCount(); i++) {
			String name = md.getColumnName(i).toUpperCase(Locale.ENGLISH);
			int itype = md.getColumnType(i);
			nameToType.put(name, Integer.valueOf(itype));
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.SAVEMETA.INFO", new Object[] { name, "" + itype }));
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object table) throws Exception {
		return null;
	}

	/**
	 * Instructs this connector to use the provided result set instead of its
	 * own.
	 * 
	 * @param rs
	 *            The new resultSet value
	 * 
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void setResultSet(ResultSet rs) throws Exception {
		map1 = new SqlMap(rs);
	}

	/**
	 * SqlMap for iterating thru the delta tables.Used for building entry from
	 * the DB data.
	 */
	private class SqlMap {
		/**
		 * DB result set
		 */
		private ResultSet rs;

		/**
		 * meta data from the DB
		 */
		private ResultSetMetaData md;

		/**
		 * Class constructor
		 * 
		 * @param rs
		 *            result set from the DB
		 * @throws SQLException
		 */
		public SqlMap(ResultSet rs) throws SQLException {
			this.rs = rs;
			md = rs.getMetaData();
			saveMetaData(md);
		}

		/**
		 * This method is used to build entry objects where the column anmes are
		 * mapped to Attribute names and column valus to attribute value
		 * 
		 * @return Entry object
		 * @exception Exception
		 *                Any exceptions thrown by the connector's underlying
		 *                classes
		 */
		public Entry buildEntry() throws Exception {
			Trace.entrymin(this, "buildEntry");

			if (!rs.next()) {
				rs.close();
				return null;
			}

			Entry entry = new Entry();

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.ENTER.BUILD.ENTRY.INFO"));
			}

			for (int i = 1; i <= md.getColumnCount(); i++) {
				String name = md.getColumnName(i);
				Object val = null;
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.COLUMN.NAME.INFO", name));
				}

				if (name.equals("ID") && !dbTableName.startsWith(DELTA_PREFIX)) {
					continue;
				}

				// the column types in the sys store tables are: VARCHAR, LONG
				// VARBINARY, int.
				switch (md.getColumnType(i)) {
				// String & Character Data
				case Types.CHAR:
				case Types.VARCHAR:
					val = rs.getString(i);
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.GET.CHAR.VARCHAR.INFO", val));
					}
					break;

				case Types.LONGVARBINARY:
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.GET.LONGVARBINARY.INFO", val));
					}
					java.io.InputStream bis = rs.getBinaryStream(i);

					if (null != bis) {
						try {
							int ch;
							ByteArrayOutputStream ba = new ByteArrayOutputStream();
							while ((ch = bis.read()) != -1) {
								ba.write(ch);
							}
							ba.close();
							val = ba.toByteArray();

						} catch (java.io.IOException ioe) {
							bis.close();
							val = ioe;
						}
						// val = StoreFactory.deserializeObject ( (byte[]) val
						// );
						val = StoreFactory.deserializeObject(val);
					}
					break;
				// CS behaves badly in n/w mode. The long varbinmary is
				// interpreted as long varchatr and deserilization screws up.
				// To avoid this CS bug, do not handle Types.LONGVARCHAR. we
				// just handle it as a default obj.
				// case Types.LONGVARCHAR:
				// java.io.InputStream is = rs.getAsciiStream(i);
				// int ch;
				// StringBuffer buf = new StringBuffer();
				// try {
				// while ((ch = is.read()) != -1) {
				// buf.append((char) ch);
				// }
				// val = buf.toString();
				// is.close();
				// } catch (java.io.IOException ioe) {
				// is.close();
				// val = ioe;
				// }
				//			
				// val = StoreFactory.deserializeObject ( val );
				// break;

				// Integer value
				case Types.INTEGER:
					val = Integer.valueOf(rs.getInt(i));
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.GET.DECIMAL.INTEGER.INFO", val));
					}
					if (rs.wasNull()) {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.NULL.VALUE.FOR.INT.INFO", val));
						}
						val = null;
					}
					break;

				case Types.BINARY:
				case Types.JAVA_OBJECT:
				case Types.DISTINCT:
				case Types.STRUCT:
				case Types.ARRAY:
				case Types.BLOB:
				case Types.CLOB:
				case Types.REF:
				default:
					val = rs.getObject(i);
					val = StoreFactory.deserializeObject(val);
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.GET.OBJECT.WARNING", (val == null ? "null" : val.getClass()
								.getName())));
					}
					break;
				}

				if (debugMode()) {
					if (val != null) {
						if (val instanceof Object) {
							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.RESULT.CLASS.INFO", new Object[] { name, val,
										val.getClass().getName() }));
							}
						} else {
							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.RESULT.BASIC.INFO", new Object[] { name, val }));
							}
						}
					} else {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.RESULT.NULL.INFO", name));
						}
					}
				}

				if (val == null) {
					entry.newAttribute(name);
				} else {
					entry.setAttribute(name, val);
				}

				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.EXIT.BUILD.ENTRY.INFO"));
				}

			}

			// ReadModes work only with delta
			if ((dbTableName.startsWith(DELTA_PREFIX)) && (readMode == READ_ALL)) {
				return entry;
			}

			Trace.exitmin(this, "buildEntry", entry);
			return entry;

		}
	}

	/**
	 * Commit the last transactions
	 * 
	 * @exception SQLException
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */
	public void commit() throws SQLException {
		if (conn != null && !autoCommit)
			conn.commit();
	}

	/**
	 * Rollback the last transactions since the last commit
	 * 
	 * @see #commit
	 * @see #setCommitMode
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void rollback() throws SQLException {
		if (conn != null && !autoCommit)
			conn.rollback();
	}

	/**
	 * Set the commit behavior of this PES Connector.
	 * 
	 * @param mode
	 *            The intended behavior. Possible values are:
	 *            <ul>
	 *            <li>"After every database operation"
	 *            <li>"On Connector close"
	 *            <li>"Manual"
	 *            </ul>
	 * @return false if the requested mode is not a legal value
	 * @see #commit
	 */
	public boolean setCommitMode(String mode) {
		if ("After every database operation".equalsIgnoreCase(mode)) {
			autoCommit = true;
			autoCommitAll = false;
			setEOCFlag(false);
			return true;
		}

		if ("On Connector close".equalsIgnoreCase(mode)) {
			autoCommit = false;
			commitOnClose = true;
			autoCommitAll = false;
			setEOCFlag(false);
			return true;
		}
		if ("Manual".equalsIgnoreCase(mode)) {
			autoCommit = false;
			commitOnClose = false;
			autoCommitAll = false;
			setEOCFlag(false);
			return true;
		}
		if ("End Of Cycle".equalsIgnoreCase(mode)) {
			autoCommit = false;
			commitOnClose = false;
			autoCommitAll = false;
			setEOCFlag(true);
			return true;
		}
		return false;
	}

	/**
	 * @param driver
	 *            java class name of the JDBC driver
	 * @return the configured createTable or proper 'CREATE TABLE' statement if
	 *         the specified <code>driver</code> is known
	 */
	public String getCreateTable(String driver) {
		String createTable = getParam("createTable");
		if (createTable != null && createTable.trim().length() > 0) {
			return createTable.trim();
		}

		// Keep backward compatibility by checking the old values of
		// selectDBDriver parameter
		if (driver != null && (driver.equalsIgnoreCase("CloudScape") || driver.equalsIgnoreCase("DB2"))) {
			createTable = CREATE_TABLE1 + ";" + CREATE_TABLE2;
		} else {
			// Provide 'CREATE TABLE' statement based on the used JDBC driver
			createTable = StoreFactory.getSysStoreCreateStmtByDriver(driver);
		}
		return createTable;
	}

	/**
	 * Method returns list of table names
	 * 
	 * @return List of tables
	 * @exception Exception
	 *                Any exceptions thrown by the connector's underlying
	 *                classes
	 */

	public Vector<String> queryTables() throws Exception {
		Trace.entrymid(this, "queryTables");
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = null;
		Vector<String> list = new Vector<String>();
		String[] types = { "TABLE", "VIEW" };
		String username = getParam("jdbcLogin");

		if (username == null || username.equals(""))
			username = StoreFactory.getJdbcUser();

		Exception fail = null;

		try {
			if (isMSSQLServer) {
				rs = dmd.getTables(null, null, null, types);
			} else {
				rs = dmd.getTables(null, username, null, types);
			}

		} catch (Exception error) {
			fail = error;
		}

		if (fail != null) {
			throw fail;
		}

		if ((rs == null) || !rs.next()) {
			// if no tables are available, try upper case schema name
			try {
				rs = dmd.getTables(null, username.toUpperCase(), null, types);
			} catch (Exception error) {
				fail = error;
			}

			if (fail != null) {
				throw fail;
			}
		} else {
			// if an entry is available add it to list before it calls next.
			list.add(rs.getString("TABLE_NAME"));
		}

		if (rs != null) {
			while (rs.next()) {
				list.add(rs.getString("TABLE_NAME"));

			}
			Collections.sort(list);

			rs.clearWarnings();
			rs.close();
		}
		Trace.exitmin(this, "queryTables", list);

		return list;
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * This method will determine if the entry that is returned should be
	 * unwrapped or should it be returned as an "ENTRY". This method has been
	 * added because in TDI6.0 and prior versions, the findEntry( ) method would
	 * return and Entry of the format: [ENTRY: <Instance of Entry object
	 * containing Attributes passed by user>] Because of this to obtain the
	 * original passed attributes, users needed to write code like: Entry e =
	 * (Entry)returned_entry.getAttribute("ENTRY"). This would cause the
	 * PESConnector in UPDATE MODE to always make updates... because the Entry
	 * from the Feed section of AL, would never match the entry returned by the
	 * PESConnector's findEntry method.
	 * 
	 * In TDI6.1, by default this entry is now unwrapped and retunred....
	 * therefore all attributes passed by the user are now directly available as
	 * attributes in Entry.
	 * 
	 * But, if users wish to switch to the old "unwrapped" entry behavior, they
	 * will need to set a TDI Property called
	 * <b>tdi.pesconnector.return.wrapped.entry=true </b>. If this property is
	 * found and set to true, then only the old behavior will be used, other
	 * wise by default the new behavior of retunring an unwrapped entry will be
	 * used.
	 * 
	 * This method will return false by default(new behviour), and will only
	 * return true if property <b>tdi.pesconnector.return.wrapped.entry </b> is
	 * set to true.
	 * 
	 * @return true for old behavior , false for new
	 * 
	 */
	private boolean determineIfReturnWrappedEntry() {

		String flag = null;
		try {
			flag = System.getProperty("tdi.pesconnector.return.wrapped.entry");
		} catch (Exception ex) {
			log.debug("The tdi.pesconnector.return.wrapped.entry is not defined,continue with default behavior", ex.getMessage());
		}

		if (flag == null) {
			return false;
		}
		if (flag.equals("true")) {
			return true; // Old behavior
		}
		return false; // Default new behavior

	}

	/**
	 * Class used for creating prepared statements.
	 * 
	 */
	private class PreparedSql {
		/**
		 * prepared statement
		 */
		private PreparedStatement ps;

		/**
		 * Index of a value to be replaced
		 */
		private int index;

		/**
		 * Class constructor
		 * 
		 * @param sql
		 *            SQL statement
		 * @throws Exception
		 *             if an error occurs
		 */
		public PreparedSql(String sql) throws Exception {
			ps = conn.prepareStatement(sql);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.PREPARED.STATEMENT.SQL", sql));
			}
			index = 1;
		}

		/**
		 * Executes Query
		 * 
		 * @return ResultSet of query
		 * @throws Exception
		 *             Any exceptions thrown by the connector's underlying
		 *             classes
		 */
		public ResultSet executeQuery() throws Exception {
			return ps.executeQuery();
		}

		/**
		 * Executes Update
		 * 
		 * @return the number of rows affected
		 * @throws Exception
		 *             Any exceptions thrown by the connector's underlying
		 *             classes
		 */
		public int executeUpdate() throws Exception {
			return ps.executeUpdate();
		}

		/**
		 * Method sets search criteria values
		 * 
		 * @param search
		 *            SearchCriteria
		 * @throws Exception
		 *             Any exceptions thrown by the connector's underlying
		 *             classes
		 */
		public void setValues(SearchCriteria search) throws Exception {

			for (Object obj : search.getCriteria()) {
				if (obj instanceof SearchCriteria.rscSearch) {
					setPSValue(((SearchCriteria.rscSearch) obj).name, ((SearchCriteria.rscSearch) obj).value);
				} else if (obj instanceof SearchCriteria) {
					setValues((SearchCriteria) obj);
				}
			}
		}

		/**
		 * Sets a value in the prepared statement
		 * 
		 * @param name
		 *            type of the parameter to set
		 * @param val
		 *            value of the parameter
		 * @throws Exception
		 */
		private void setPSValue(String name, Object val) throws Exception {

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.SETPSVALUE.INFO",
						new Object[] { name, val, Integer.valueOf(index) }));
			}

			Integer type = nameToType.get(name.toUpperCase(Locale.ENGLISH));

			if (type == null) {
				throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.NOMETADATA.DESCR.ERROR", name));
			}

			if (val instanceof Attribute) {
				val = ((Attribute) val).getValue(0);
			}

			if (val == null) {
				ps.setNull(index, type.intValue());
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.SETNULLPSVALUE.WARNING", Integer.valueOf(index)));
				}
			} else {

				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.VALCLASS.INFO", val.getClass().getName()));
					debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.VALTYPE.INFO", type));
				}

				switch (type.intValue()) {
				// String & Character Data
				case Types.LONGVARCHAR:
				case Types.VARCHAR:
					if (debugMode()) {
						if (type.intValue() == Types.LONGVARCHAR)
							debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.LONGVARCHAR.INFO", val.toString()));
						else
							debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.VARCHAR.INFO", val.toString()));
					}
					ps.setString(index, val.toString());
					break;

				case Types.OTHER:
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.OTHERVALUE.INFO", val.toString()));
					}
					ps.setString(index, val.toString());
					break;
				// Numbers
				case Types.INTEGER:
				case Types.TINYINT:
				case Types.SMALLINT:
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.NUMVALUE.INFO", val.toString()));
					}

					if (val instanceof Integer) {
						ps.setInt(index, ((Integer) val).intValue());
					} else if (val instanceof Float) {
						ps.setInt(index, ((Float) val).intValue());
					} else if (val instanceof Double) {
						ps.setInt(index, ((Double) val).intValue());
					} else if (val instanceof java.math.BigInteger) {
						ps.setLong(index, ((java.math.BigInteger) val).longValue());
					} else {
						try {
							ps.setInt(index, Integer.parseInt(val.toString()));
						} catch (java.lang.NumberFormatException nfe) {
							if (debugMode()) {
								debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.PARSINT.WARNING", nfe));
							}
							java.math.BigInteger bigint = new java.math.BigInteger(val.toString());
							ps.setLong(index, bigint.longValue());
						}
					}
					break;
				default:
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.SYSTEMSTORE.UNHANDLED.TYPE.INFO", new Object[] { type, name }));
					}

					ps.setString(index, val.toString());
				}
			}

			index++;
		}

		/**
		 * Method closes PrepareStatement
		 * 
		 */
		public void close() {
			try {
				if (ps.getWarnings() != null) {
					logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.SQL.WARN.WARNING", ps.getWarnings().toString()));
				}
				ps.clearWarnings();
				ps.close();
			} catch (Exception exc) {
				logmsg(sResHash.getString("CONNECTOR.SYSTEMSTORE.UNABLE.TO.CLOSE", exc.getMessage()));
			}
		}
	}

	/**
	 * Checks the specified table name.
	 * 
	 * @throws Exception
	 *             if the name is not valid
	 */
	private void verifyTable() throws Exception {
		if (tableVerified)
			return;

		if (dbTableName == null || dbTableName.trim().length() == 0)
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.MISSING.TABLENAME.ERROR"));

		dbTableName = dbTableName.trim();

		if (!Character.isJavaIdentifierStart(dbTableName.charAt(0)))
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.TABLENAME.LETTER.FIRST"));

		for (int i = 1; i < dbTableName.length(); i++)
			if (!Character.isJavaIdentifierPart(dbTableName.charAt(i)))
				throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.TABLENAME.ALPHANUMERIC"));

		String createCmd = getCreateTable(getParam("selectDBDriver"));

		if (createCmd == null || createCmd.length() == 0)
			throw new Exception(sResHash.getString("CONNECTOR.SYSTEMSTORE.MISSING.CREATETABLE"));

		Vector<String> v = new Vector<String>();
		StringTokenizer tokens = new StringTokenizer(createCmd, SEPARATOR);
		while (tokens.hasMoreTokens()) {
			v.add(MessageFormat.format(tokens.nextToken().trim(), new Object[] { dbTableName }));
		}

		try {
			StoreFactory.verifyTable(conn, dbTableName, v);
		} catch (Exception ex) {
			log.error(sResHash.getString("CONNECTOR.SYSTEMSTORE.CREATE.TABLENAME.ERROR"), dbTableName, ex);
			throw new Exception(ex);
		}

		tableVerified = true;
	}

	/**
	 * Is end of cycle reached
	 * 
	 * @return true if EOC is reached
	 */
	public boolean isEOCFlag() {
		return EOCFlag;
	}

	/**
	 * Sets the value for EOC reached
	 * 
	 * @param flag
	 *            true , if EOC is reached, false otherwise
	 */
	public void setEOCFlag(boolean flag) {
		EOCFlag = flag;
	}

}
