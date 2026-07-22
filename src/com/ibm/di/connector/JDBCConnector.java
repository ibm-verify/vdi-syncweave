/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.AttributeValue;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.ServerConstants;
import com.ibm.di.server.Trace;
import com.ibm.di.store.StoreFactory;
import com.ibm.di.util.ParameterSubstitution;
import com.ibm.di.util.StringUtils;
import com.ibm.icu.text.DateFormat;
import java.text.SimpleDateFormat;
import com.ibm.icu.util.StringTokenizer;
/**
 * This connector provides access to JDBC/ODBC based systems. The connector will
 * attempt to perform as much conversion between types as possible.
 *
 */

public class JDBCConnector extends Connector implements ConnectorInterface,
		SkipLookupInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The Connector version.
	 */
	public static final String VERSION_INFO = "2.3-di7.1.1 %I%, 20%E%";

	/**
	 * Possible Connector modes.
	 */
	public static final String[] CONNECTOR_MODES = { ConnectorConfig.ADDONLY_MODE,
		ConnectorConfig.DELETE_MODE, ConnectorConfig.ITERATOR_MODE,
		ConnectorConfig.LOOKUP_MODE, ConnectorConfig.UPDATE_MODE,
		ConnectorConfig.DELTA_MODE };
	
	/**
	 * Name of the properties file
	 */
	private static final String PROPERTIES_FILE = "jdbcconnector";

	/**
	 * Component name
	 */
	private static final String myName = "JDBC/ODBC Connector";
	// Connector flags and options
	/**
	 * Should null values be exposed as empty strings
	 */
	private boolean exposeNullValuesAsEmptyStrings = false;

	/**
	 * should errors in fields be ignored
	 */
	private boolean ignoreFieldErrors = false;

	/**
	 * auto commit enabled flag
	 */
	private boolean autoCommit = true;

	/**
	 * auto commit flag for all transactions
	 */
	private boolean autoCommitAll = false;

	/**
	 * flag for enabling to commit transactions on close
	 */
	private boolean commitOnClose = true;

	/**
	 * true if padding is enabled for lookup
	 */
	// Member variables added for defect 8565
	private boolean padLookupString = true;

	/**
	 * true if padding is enabled for update
	 */
	private boolean padUpdString = true;
	/**
	 * true if padding is enabled for insert
	 */
	private boolean padInsString = true;

	/**
	 * Connection object to the DB
	 */
	private Connection con;

	/**
	 * List of Entries containing the metaData (schema) from the DB
	 */
	private Object metaData;

	/**
	 * flag indicates if query schema is called
	 */
	private boolean querySchemaCalled = false;
	/**
	 * flag indicates if query schema is failed
	 */
	private Exception querySchemaFail;

	// Objects for iteration mode
	/**
	 * Statement to execute against the DB (iterator mode)
	 */
	private Statement stmt1;

	/**
	 * Result from executed statement. (iterator mode)
	 */
	private ResultSet rs1;

	/**
	 * SQL map for iterator mode
	 */
	private SqlMap map1;

	/**
	 * Property to Enable or Disable Parameter substitution ,It is required to
	 * have backward compatibility
	 */
	private boolean enableParamSubstitute = true;

	/**
	 * additional provided parameters
	 */
	private String jdbcProvParams;

	/**
	 * true if additional parameters are going to be used.
	 */
	private boolean useProvParams = false;

	// Objects for execSQL
	/**
	 * Statement to execute against the DB. (not iterator mode)
	 */
	private Statement stmt2;
	/**
	 * Result from executed statement. (not iterator mode)
	 */
	private ResultSet rs2;

	/**
	 * SQL map (not iterator mode)
	 */
	private SqlMap map2;

	// Mapping tables for syntax/class conversion
	// This is common for all modes, hope to catch as many names as possible
	/**
	 * DB type to java class mapping
	 */
	private Map<String, Integer> nameToType = new Hashtable<String, Integer>();

	/**
	 * holds name of the column and size
	 */
	private Map<String, Integer> nameSize = new Hashtable<String, Integer>();

	/**
	 * metadata
	 */
	private Entry metadataEntry = new Entry();

	/**
	 * Resource Hash object for accessing TMS messages
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Should prepared statements be used.
	 */
	private boolean usePreparedStatement = true;

	/**
	 * is the DB MS SQL Server
	 */
	private boolean isMSSQLServerDB;

	/**
	 * number of affected entries
	 */
	private int numEntriesAffected = -1;

	/**
	 * end of cycle reached
	 */
	private boolean end_of_cycle_flag = false;

	/**
	 * Cache for Prepared Statements
	 */
	private Map<String, PSCache> psCache;

	/**
	 * Strings used to refer to SQL statements
	 */
	private final static String SELECT = "jdbcSelect";
	private final static String FIND = "jdbcLookup";
	private final static String INSERT = "jdbcInsert";
	private final static String MODIFY = "jdbcUpdate";
	private final static String DELETE = "jdbcDelete";

	/**
	 * Use custom PreparedStatements
	 */
	private boolean customPreparedStatements = false;

	/**
	 * Keep track of last String used to construct a PreparedStatement
	 */
	private String preparedString;
	
	/**
	 * Keep track of last SQL string
	 */
	private String lastSQL;
	
	/**
	 * The Schema separator, normally a single dot.
	 */
	private String schemaSeparator = ".";
	
	/**
	 * Constructor
	 */
	public JDBCConnector() {
		setName(myName);
		setModes(CONNECTOR_MODES);
	}

	/**
	 * Initialize the connector and discover syntax
	 *
	 * @param o
	 *            Ignored
	 * @exception Exception
	 *                Throws exception if required JDBC parameters are not set.
	 *
	 */
	public void initialize(Object o) throws Exception {

		// Load JDBC/ODBC driver
		String driver = getParam("jdbcDriver");
		if (driver == null || driver.length() == 0) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.JDBC.REQUIRED.JDBCDRIVER.PARAMETER.NOT.SET"));
		}

		Class.forName(driver.trim());

		psCache = new HashMap<String,PSCache>();

		// jdbc provider params
		jdbcProvParams = getParam("jdbcProviderParams");

		// If user wants to specify a complete set of JDBC properties use these,
		// otherwise
		// use the standard parameters for source and login

		if (jdbcProvParams != null && jdbcProvParams.length() > 0) {
			useProvParams = true;
		}

		String dbName = getParam("jdbcSource");
		if (dbName == null || dbName.trim().length() == 0)
			throw new Exception(sResHash
					.getString("CONNECTOR.JDBC.DB.NAME.NULL"));

		// MS SQL Drivers (Type-4).
		isMSSQLServerDB = StoreFactory.isMSSQLDriver(driver) ||
			(driver.equals("sun.jdbc.odbc.JdbcOdbcDriver") &&
				dbName.contains("Microsoft"));

		// retain this so that we don't break existing AL which use this in the
		// hooks.
		String useProp = getParam("jdbcUseProperties");

		try {
			if ((useProp != null && useProp.equalsIgnoreCase("true"))
					|| useProvParams) {
				con = DriverManager.getConnection(getParam("jdbcSource"),
						getJdbcProperties());
			} else {
				con = DriverManager.getConnection(getParam("jdbcSource"),
						getParam("jdbcLogin"), getParam("jdbcPassword"));
			}
		} catch (SQLException ex) {
			String msg = sResHash.getString(
					"CONNECTOR.JDBC.DB.UNABLE.TO.CONNECT", ex.getMessage());
			if (getLog() != null)
				getLog().logerror(msg, ex);
			// Add exception asking user to give pwd/usrname
			throw new Exception(msg, ex);
		}
		try {
			con.setAutoCommit(false);
		} catch (SQLException sqle) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.SQL.ALWAYS.AUTOCOMMIT.WARNING"));
			}
		}

		// Auto create table?
		Boolean b = getBoolean("jdbcAutoCreateTable");
		if(b != null && b.booleanValue())
			createDefaultTable();

		// See if the user wants to use PreparedStatement
		if (getBoolean("jdbcPreparedStatement") != null)
			usePreparedStatement = getBoolean("jdbcPreparedStatement");

		// See if the user wants to use Custom PreparedStatements
		if (getBoolean("jdbcCustomPreparedStatements") != null)
			customPreparedStatements = getBoolean("jdbcCustomPreparedStatements");

		// Prepare statements
		stmt2 = con.createStatement();

		// Set Database Session Parameters
		setSessionParameters();

		// Expose null values
		String exp = getParam("jdbcExposeNullValues");
		if (exp != null && exp.compareToIgnoreCase("true") == 0) {
			exposeNullValuesAsEmptyStrings = true;
		}

		// Other Connector flags
		String str = getParam("connectorFlags");
		if (str != null) {
			str = str.toLowerCase(Locale.ENGLISH);
			if (str.indexOf("ignorefielderrors") != -1) {
				ignoreFieldErrors = true;
			}
		}

		str = getParam("jdbcCommit");
		if (str != null && str.length() > 0 && !setCommitMode(str)) {
			logmsg(sResHash.getString("CONNECTOR.JDBC.COMMITMODE.WARNING", str));
		}

		// Try to discover the table/view columns and syntax by querying the
		// server.
		// First check if we should try to do so, skip it if we have a select
		// statement and maybe are in Iterator mode
		boolean maybeIteratorMode = true;
		if (o instanceof ConnectorMode)
			maybeIteratorMode = (((ConnectorMode) o).getMode() == ServerConstants.TYPE_ITERATOR);
		boolean tryQuerySchema = true;
		String sql = getParam(SELECT);
		if (sql != null && sql.length() > 0 && maybeIteratorMode)
			tryQuerySchema = false;

		querySchemaCalled = false;
		// Not all servers support this so we may have to do a "SELECT"
		// (MetaQuery)
		String table = getParam("jdbcTable");
		if (tryQuerySchema && table != null && table.length() > 0) {
			try {
				metaData = querySchema(table);
			} catch (Exception error) {
				logmsg(sResHash.getString("CONNECTOR.JDBC.SCHEMA.WARNING",
						error.toString()));
			}
		}

		// try to do a select statement to discover columns and types.
		// This must be historical code, the parameter is not in the idi.inf
		// file
		String metaq = getParam("jdbcMetaQuery");
		if (metaq != null && metaq.length() > 0) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.METAQUERY.INFO", metaq));
			}
			lastSQL = metaq;
			ResultSet rs = stmt2.executeQuery(metaq);
			try {
				saveMetaData(rs.getMetaData());
			} finally {
				rs.close();
				if (autoCommitAll)
					con.commit();
			}
		}

		// Defect 11529
		if (nameToType.size() == 0 && (o instanceof ConnectorMode)
				&& (((ConnectorMode) o).getMode() == ServerConstants.TYPE_LOOKUP)
				&& sql != null && sql.length() > 0 && !customPreparedStatements) {
			lastSQL = sql;
			ResultSet rs = stmt2.executeQuery(sql);
			try {
				saveMetaData(rs.getMetaData());
			} finally {
				rs.close();
				if (autoCommitAll)
					con.commit();
			}
		}

		// Code changes for feature FN-33
		// Setting the padding style.
		setPaddingStyle();

	}

	/**
	 * The method sets the padding style for the various modes and operation of
	 * the Connector. If the connector mode is any thing other than ADD ONLY,
	 * UPDATE AND LOOKUP then as a fallback mechanism the default behavior of
	 * enbaling padding is set.
	 *
	 */
	private void setPaddingStyle() {
		setPaddingInInsert(!Boolean.valueOf(getParam("jdbcDisablePaddingInsert")));
		setPaddingInUpdate(!Boolean.valueOf(getParam("jdbcDisablePaddingUpdate")));
		setPaddingInLookup(!Boolean.valueOf(getParam("jdbcDisablePaddingLookup")));
	}

	/**
	 * Used by initialize create a list of jdbc properties from user properties
	 *
	 * @return Properties
	 */
	private Properties getJdbcProperties() {
		Trace.entrymax(this, "getJdbcProperties");
		Properties props = new Properties();

		String p = getParam("jdbcLogin");
		if (p != null && p.length() > 0)
			props.put("user", p);

		p = getParam("jdbcPassword");
		if (p != null && p.length() > 0)
			props.put("password", p);

		for (Iterator<String> i = getRawConnectorConfiguration()
				.getDataIterator(); i.hasNext();) {
			String key = (String) i.next();
			if (key.startsWith("jdbc.")) {
				props.put(key.substring(5), getParam(key));
			}
		}

		if (jdbcProvParams != null && jdbcProvParams.length() > 0) {
			StringTokenizer st = new StringTokenizer(jdbcProvParams, "\r\n");
			while (st.hasMoreTokens()) {
				String nt = st.nextToken();
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.JDBC.NEXT.EXTRAPARAM.INFO", nt));
				}
				if (nt.length() < 1) {
					continue;
				}

				int index = nt.indexOf(":");
				if (index == -1) {
					logmsg(sResHash.getString(
							"CONNECTOR.JDBC.EXTRAPARAM.WARNING", nt));
					continue;
				}

				String param = nt.substring(0, index);
				String value = nt.substring(index + 1);
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.JDBC.PROVIDER.PARAM.INFO", new Object[] {
									param, value }));
				}
				props.put(param, value);
			}
		}

		Trace.exitmax(this, "getJdbcProperties", props);

		return props;
	}

	/**
	 * Sets the sessionParameters attribute of the JDBCConnector object
	 *
	 * @exception SQLException
	 *                Contains exception thrown during database operations
	 */
	public void setSessionParameters() throws SQLException {
		String sp = getParam("jdbcSessionParameters");
		if (sp == null) {
			return;
		}

		StringTokenizer st = new StringTokenizer(sp, "\r\n");
		while (st.hasMoreTokens()) {
			alterSession(st.nextToken());
		}
	}

	/**
	 * Create a ALTER SESSION Sql statement, and execute it.
	 *
	 * @param command
	 *            The rest of the ALTER SESSION statement
	 * @exception SQLException
	 *                Contains exception thrown during database operations
	 */
	public void alterSession(String command) throws SQLException {
		String alter = "ALTER SESSION " + command;
		if (debugMode()) {
			debug(sResHash
					.getString("CONNECTOR.JDBC.ALTER.SESSION.INFO", alter));
		}
		lastSQL = alter;
		stmt2.execute(alter);
		stmt2.clearWarnings();
		con.commit();
	}

	/**
	 * Returns true if this connector is able to perform delta updates
	 *
	 * @return true if delta updates are supported, false otherwise
	 */
	public boolean isDeltaSupported() {
		return true;
	}

	/**
	 * Method checks exception type
	 *
	 * @param e
	 *            Exception
	 * @return true if exception of type IOException ,if of type SQLException
	 *         return false
	 *
	 */
	public boolean isIOException(Throwable e) {
		if (e instanceof IOException)
			return true;
		if (!(e instanceof SQLException))
			return false;
		String msg = e.getMessage();
		if (msg == null)
			return false;
		return (msg.startsWith("I/O") || msg.startsWith("Io")
				|| msg.startsWith("IO") || msg.startsWith("ORA-01089") || msg
				.startsWith("Closed Connection"));
	}

	/**
	 * terminate - close handles and connections
	 */
	public void terminate() {
		try {
			if (con != null) {
				if (rs1 != null) {
					rs1.clearWarnings();
					rs1.close();
					rs1 = null;
				}

				if (rs2 != null) {
					rs2.clearWarnings();
					rs2.close();
					rs2 = null;
				}

				if (stmt1 != null) {
					stmt1.clearWarnings();
					stmt1.close();
					stmt1 = null;
				}

				if (stmt2 != null) {
					stmt2.clearWarnings();
					stmt2.close();
					stmt2 = null;
				}

				if (psCache != null) {
					for (PSCache psc: psCache.values()) {
						if (psc.ps != null) {
							psc.ps.clearWarnings();
							psc.ps.close();
						}
						if (psc.psql != null)
							psc.psql.close();
					}
					psCache.clear();
				}

				if (commitOnClose) {
					con.commit();
				} else {
					con.rollback();
				}
			}
		} catch (SQLException se) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.UNABLE.TO.CLOSE", se
						.getMessage()));
			}
		}

		try {
			if (con != null)
				con.clearWarnings();
		} catch (SQLException e) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.UNABLE.TO.CLEAR.WARNINGS", e
								.getMessage()));
			}

		}

		// Regardless what happened in clearWarning, close connection.....
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.UNABLE.TO.CLOSE", e
						.getMessage()));
			}
		}

		con = null;
		metaData = null;
		nameToType.clear();
	}

	/**
	 * Prepares for getNextEntry().
	 *
	 * @see #getNextEntry
	 * @exception Exception
	 *                Exception thrown during database operations
	 */
	public void selectEntries() throws Exception {

		PSCache psc = getPrepared(SELECT, null, null, false);
		if ( psc != null) {
			rs1 = psc.ps.executeQuery();
		} else  {
			String sql = getParam(SELECT);

			if (sql == null || sql.length() == 0) {
				sql = "SELECT * FROM ";

				String schema = getParam("jdbcSchema");
				if ((schema != null) && (schema.length() > 0))
					sql += schema + schemaSeparator;

				sql += getParam("jdbcTable");
			} else {
				if (enableParamSubstitute) {
					sql = ParameterSubstitution.substitute(sql,
							getMap(null, null, true));
				}
				sql = sql.trim();
			}

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.SELECT.ENTRIES.INFO", sql));
			}
			stmt1 = con.createStatement();
			lastSQL = sql;
			rs1 = stmt1.executeQuery(sql);

			if (rs1 == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.JDBC.EXECUTE.SQL.ERROR", sql));
			}
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.SOURCE.COLUMNS.INFO"));
		}

		map1 = new SqlMap(rs1);

		if (autoCommitAll) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.COMMIT.FOR.SELECT.INFO"));
			}
			con.commit();
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.ENTRIES.SELECTED.INFO"));
		}
	}

	/**
	 * Gets the nextEntry attribute of the JDBCConnector object
	 *
	 * @return The nextEntry value
	 * @exception Exception
	 *                Exception thrown during database operations
	 */
	public Entry getNextEntry() throws Exception {
		if (map1 == null) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.NOENTRIES.FROM.SELECT.INFO"));
			}
			return null;
		}
		return map1.buildEntry();
	}

	/**
	 * Enables and disables the padding while insert
	 *
	 * @param val
	 */
	public void setPaddingInInsert(boolean val) {
		padInsString = val;
	}

	/**
	 * Enables and disables the padding while lookup
	 *
	 * @param val
	 *            value to set
	 */
	public void setPaddingInLookup(boolean val) {
		padLookupString = val;
	}

	/**
	 * Enables and disables the padding while update
	 *
	 * @param val
	 *            value to set
	 *
	 */
	public void setPaddingInUpdate(boolean val) {
		padUpdString = val;
	}

	/**
	 * Returns true if padding is disabled for lookup
	 *
	 * @return boolean
	 *
	 */

	public boolean isLookupPaddingDisabled() {
		return !padLookupString;
	}

	/**
	 * Returns true if padding is disabled for update
	 *
	 * @return boolean
	 *
	 */
	public boolean isUpdatePaddingDisabled() {
		return !padUpdString;
	}

	/**
	 * Returns true if padding is disabled for insert
	 *
	 * @return boolean
	 *
	 */
	public boolean isInsertPaddingDisabled() {
		return !padInsString;
	}

	/**
	 * Finds an existing entry. The search criteria specifies which entry to
	 * modify.
	 *
	 * @param search
	 *            The search criteria used to locate the entry to be modified
	 * @return the single entry found or null
	 * @exception Exception
	 *                derived from the connector's underlying classes
	 */
	public Entry findEntry(SearchCriteria search) throws Exception {
		clearFindEntries();

		ResultSet rs = null;

		Exception psErr = null;
		
		String sql = getParam(FIND);

		PSCache psc = getPrepared(FIND, search, null, padLookupString);
		if (psc != null && psc.type != 2 ) {
			rs = psc.ps.executeQuery();
		} else if (sql == null || sql.length() == 0) {
			if (!customPreparedStatements)
				sql = getParam(SELECT);
			if (sql == null || sql.length() == 0 ) {
				sql = "SELECT * FROM ";

				String schema = getParam("jdbcSchema");
				if ((schema != null) && (schema.length() > 0))
					sql += schema + schemaSeparator;
				sql += getParam("jdbcTable");
			}

			sql += " WHERE ";

			if (usePreparedStatement) {
				try {
					String pstmt = sql + getWhereClause(search, true);
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.FINDENTRYPS.INFO", pstmt));
					}
					PreparedSql ps = getPSql(FIND, pstmt);
					ps.setValues(search);
					rs = ps.executeQuery();
				} catch (Exception err) {
					if (isIOException(err))
						throw err;

					psErr = err;

					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.NOPREPARED.STATEMENT.WARNING",
								err.getMessage()));
					}
				}
			}
			if (rs == null)
				sql += getWhereClause(search, false);
		} else {
			if (enableParamSubstitute) {
				sql = ParameterSubstitution.substitute(sql, getMap(null,
						search, padLookupString));
			}
			sql = sql.trim();
		}

		Statement stmt = null;
		try {
			if (rs == null) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.FINDENTRY.INFO",
							sql));
				}
				stmt = con.createStatement();
				lastSQL = sql;
				rs = stmt.executeQuery(sql);
				psErr = null;
			}

			try {
				SqlMap map = new SqlMap(rs);

				boolean checkAddEntry = addFindEntry(map.buildEntry());
				while (checkAddEntry) {
					checkAddEntry = addFindEntry(map.buildEntry());
				}
			} finally {
				rs.close();
				if (autoCommitAll)
					con.commit();
			}

			if (getFindEntryCount() == 1)
				return getFirstFindEntry();
			else
				return null;

		} catch (Exception e) {
			if (psErr != null)
				throw psErr;
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
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

		PSCache psc = getPrepared(MODIFY, search, entry, padUpdString);
		if (psc != null && psc.type != 2 ) {
			numEntriesAffected = psc.ps.executeUpdate();

			if (autoCommit)
				con.commit();

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.EXECUTE.UPDATE.RETURN.INFO",
						String.valueOf(numEntriesAffected)));
			}
			return;
		}

		String[] names = entry.getAttributeNames();
		entry = cleanDeltaEntry(entry, names);

		if (names.length == 0) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.MODENTRY.EXIT.WARNING"));
			}
			throw new com.ibm.di.exceptions.NoChangesException(sResHash
					.getString("CONNECTOR.JDBC.NOCHANGES.EXCEPTION.ERROR"));
		}

		Exception err = null;
		int retValue = -1;

		String update = getParam(MODIFY);

		if (update == null || update.length() == 0) {

			update = "UPDATE ";

			String schema = getParam("jdbcSchema");
			if ((schema != null) && (schema.length() > 0))
				update += schema + schemaSeparator;
			update += getParam("jdbcTable") + " SET ";

			if (usePreparedStatement) {
				try {
					StringBuilder pstmt = new StringBuilder(update);

					for (int i = 0; i < names.length; i++) {
						if (i > 0)
							pstmt.append(",");
						pstmt.append(names[i]);
						pstmt.append(" = ?");
					}
					pstmt.append(" WHERE ");
					pstmt.append(getWhereClause(search, true));

					PreparedSql ps = getPSql(MODIFY, pstmt.toString());

					for (int i = 0; i < names.length; i++) {
						if (debugMode()) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.SET.UPDATEATTRIB.INFO",
									names[i]));
						}
						ps.setPSValue(names[i], entry.getAttribute(names[i]),
								padUpdString);

					}

					ps.setValues(search);
					retValue = ps.executeUpdate();

				} catch (Exception pse) {
					if (isIOException(pse))
						throw pse;

					err = pse;
					
					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.JDBC.PREP.STATEMENT.NOTUSED.WARNING",
										pse.getMessage()));
					}
				}
			}

			if (retValue == -1) {
				StringBuilder values = new StringBuilder();

				for (int i = 0; i < names.length; i++) {
					if (i > 0)
						values.append(", ");
					values.append(names[i]);
					values.append(" = ");
					values.append(sqlValue(names[i], entry
							.getAttribute(names[i]), padUpdString));
				}
				values.append(" WHERE ");
				values.append(getWhereClause(search, false));

				update += values.toString();
			}

		} else {
			if (enableParamSubstitute) {
				update = ParameterSubstitution.substitute(update, getMap(entry,
						search, padUpdString));
			}
			update = update.trim();
		}

		if (retValue == -1) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.MODENTRY.INFO",
						update));
			}
			try {
				retValue = executeUpdate(update);
				err = null;
			} catch (Exception e) {
				if (isIOException(e))
					throw e;
				if (err == null)
					err = e;
			}
		}

		if (autoCommit)
			con.commit();

		if (err != null) {
			throw err;
		}

		numEntriesAffected = retValue;

		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.JDBC.EXECUTE.UPDATE.RETURN.INFO", Integer
							.valueOf(retValue)));
		}
	}

	/**
	 * Execute SQL update statment.
	 *
	 * @param updateSQL
	 *            Update SQL statement.
	 * @return the count of updated rows, or 0 for a statement that returns
	 *         nothing.
	 * @throws SQLException
	 *             Database error.
	 */
	private int executeUpdate(String updateSQL) throws SQLException {
		int retValue;
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			lastSQL = updateSQL;
			retValue = stmt.executeUpdate(updateSQL);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return retValue;
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

		PSCache psc = getPrepared(INSERT, null, entry, padInsString);
		if (psc != null && psc.type != 2) {
			int rows = psc.ps.executeUpdate();

			if (autoCommit)
				con.commit();

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.PUTENTRY.EXIT.INFO",
						String.valueOf(rows)));
			}
			return;
		}

		String[] names = entry.getAttributeNames();

		Exception err = null;
		int retValue = -1;

		String sql = getParam(INSERT);
		if (sql == null || sql.length() == 0) {
			sql = "INSERT into ";

			String schema = getParam("jdbcSchema");
			if ((schema != null) && (schema.length() > 0))
				sql += schema + schemaSeparator;
			sql += getParam("jdbcTable") + " ("
					+ getColumns(entry, 0, padInsString) + ") VALUES (";

			if (usePreparedStatement) {
				try {
					// Try prepared statement
					PreparedSql ps = getPSql(INSERT,
							sql + getColumns(entry, 1, padInsString) + ")");
					for (int i = 0; i < names.length; i++) {
						Attribute a = entry.getAttribute(names[i]);
						// We don't add NULL values
						if (a.getValue() == null)
							continue;

						ps.setPSValue(names[i], a, padInsString);
					}

					retValue = ps.executeUpdate();
				} catch (Exception pse) {
					if (isIOException(pse))
						throw pse;
					err = pse;

					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.PREPSTATEMENT.NOTUSE.WARNING",
								pse.getMessage()));
					}
				}
			}
			if (retValue == -1) {
				sql += getColumns(entry, 2, padInsString) + ")";
			}
		} else {
			sql = sql.trim();
			if (enableParamSubstitute) {
				sql = ParameterSubstitution.substitute(sql, getMap(entry, null,
						padInsString));
			}
		}

		if (retValue == -1) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.PUTENTRY.INFO",
						sql));
			}
			try {
				retValue = executeUpdate(sql);
				err = null;
			} catch (Exception e) {
				if (isIOException(e))
					throw e;
				if (err == null)
					err = e;
			}
		}

		if (autoCommit)
			con.commit();

		if (err != null) {
			throw err;
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.PUTENTRY.EXIT.INFO",
					Integer.valueOf(retValue)));
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
	public void deleteEntry(Entry entry, SearchCriteria search)
			throws Exception {

		PSCache psc = getPrepared(DELETE, search, entry, padLookupString);
		if (psc != null && psc.type != 2) {
			numEntriesAffected = psc.ps.executeUpdate();

			if (autoCommit)
				con.commit();

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DELETERET.INFO",
						Integer.valueOf(numEntriesAffected)));
			}
			return;
		}

		Exception err = null;
		int retValue = -1;

		String schema = getParam("jdbcSchema");

		String sql = getParam(DELETE);

		if (sql == null || sql.length() == 0) {

			sql = "DELETE FROM ";

			if ((schema != null) && (schema.length() > 0))
				sql += schema + schemaSeparator;

			sql += getParam("jdbcTable") + " WHERE ";

			if (usePreparedStatement) {
				try {
					PreparedSql ps = getPSql(DELETE, sql + getWhereClause(search, true));
					ps.setValues(search);
					retValue = ps.executeUpdate();
				} catch (Exception pse) {
					if (isIOException(pse))
						throw pse;
					err = pse;

					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.PREPSTATEMENT.NOTUSE.WARNING",
								pse.getMessage()));
					}
				}
			}
			if (retValue == -1) {
				sql += getWhereClause(search, false);
			}
		} else {
			sql = sql.trim();
			if (enableParamSubstitute) {
				sql = ParameterSubstitution.substitute(sql, getMap(entry,
						search, padLookupString));
			}
		}

		if (retValue == -1) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DELENTRY.INFO",
						sql));
			}
			try {
				retValue = executeUpdate(sql);
				err = null;
			} catch (Exception e) {
				if (isIOException(e))
					throw e;
				if (err == null)
					err = e;
			}
		}

		if (autoCommit)
			con.commit();

		if (err != null) {
			throw err;
		}

		numEntriesAffected = retValue;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.DELETERET.INFO", Integer
					.valueOf(retValue)));
		}
	}

	/**
	 * Clears the status for the operations performed
	 *
	 * @param entry
	 *            the entry to clean
	 * @param names
	 *            name of the attributes
	 * @return cleaned entry
	 */
	private Entry cleanDeltaEntry(Entry entry, String[] names) {
		Entry ret = new Entry();

		for (int i = 0; i < names.length; i++) {
			Attribute a = entry.getAttribute(names[i]);
			switch (a.getOper()) {
			case Attribute.ATTRIBUTE_MOD:
				Attribute b = ret.newAttribute(names[i],
						Attribute.ATTRIBUTE_MOD);
				for (int j = 0; j < a.size(); j++) {
					if (a.getValueOper(j) == AttributeValue.AV_DELETE)
						continue;
					b.setValue(a.getValue(j));
					break;
				}
				break;
			case Attribute.ATTRIBUTE_DELETE:
				ret.newAttribute(names[i], Attribute.ATTRIBUTE_DELETE);
				break;
			default:
				ret.setAttribute(names[i], a);
			}
		}

		return ret;
	}

	/**
	 * Executes an SQL statement.
	 *
	 * @param sql
	 *            The SQL statement to execute
	 * @return An empty string if no error, otherwise a string describing the
	 *         error
	 */
	public String execSQL(String sql) {
		try {
			lastSQL = sql;
			stmt2.execute(sql);
			stmt2.clearWarnings();
			if (autoCommit)
				con.commit();
			return "";
		} catch (SQLException e) {
			return "ERROR: " + e.toString();
		}
	}

	/**
	 * Executes an SQL statement, the returned values can be retrieved using
	 * getNextSQLSelectEntry()
	 *
	 * @param sql
	 *            The SQL statement to execute
	 * @return An empty string if no error, otherwise a string describing the
	 *         error
	 */
	public String execSQLSelect(String sql) {
		try {
			lastSQL = sql;
			rs2 = stmt2.executeQuery(sql);
			map2 = new SqlMap(rs2);
			stmt2.clearWarnings();
			if (autoCommitAll) {
				if (debugMode()) {
					debug(sResHash
							.getString("CONNECTOR.JDBC.COMMIT.FOR.SELECT.INFO"));
				}
				con.commit();
			}
			return "";
		} catch (SQLException e) {
			return "ERROR: " + e.toString();
		}
	}

	/**
	 * Gets the nextSQLSelectEntry entry
	 *
	 * @return The nextSQLSelectEntry value, null if no value available
	 * @throws Exception
	 *             if an error occurs
	 * @see #execSQLSelect
	 */
	public Entry getNextSQLSelectEntry() throws Exception {
		try {
			if (map2 != null) {
				return map2.buildEntry();
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.JDBC.NEXTSQL.WARNING", e.toString()));
			throw e;
		}
		return null;
	}

	/**
	 * Commit the last transactions
	 *
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commit() throws SQLException {
		if (con != null)
			con.commit();
	}

	/**
	 * Rolls back the transactions since the last commit
	 *
	 * @see #commit
	 * @see #setCommitMode
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void rollback() throws SQLException {
		if (con != null)
			con.rollback();
	}

	/**
	 * Set the commit behavior of this JDBC Connector.
	 *
	 * @param mode
	 *            The intended behavior. Possible values are:
	 *            <ul>
	 *            <li>"After every database operation"
	 *            <li>"On Connector close"
	 *            <li>"Manual"
	 *            </ul>
	 *
	 * @return false if the requested mode is not a legal value
	 * @see #commit
	 */
	public boolean setCommitMode(String mode) {
		if ("After every database operation (Including Select)"
				.equalsIgnoreCase(mode)) {
			autoCommit = true;
			autoCommitAll = true;
			return true;
		}
		if ("After every database operation".equalsIgnoreCase(mode)) {
			setEOCflag(false);
			autoCommit = true;
			autoCommitAll = false;
			return true;
		}
		if ("On Connector close".equalsIgnoreCase(mode)) {
			setEOCflag(false);
			autoCommit = false;
			commitOnClose = true;
			autoCommitAll = false;
			return true;
		}
		if ("End of Cycle".equalsIgnoreCase(mode)) {
			autoCommit = false;
			commitOnClose = false;
			autoCommitAll = false;
			setEOCflag(true);
			return true;
		}
		if ("Manual".equalsIgnoreCase(mode)) {
			setEOCflag(false);
			autoCommit = false;
			commitOnClose = false;
			autoCommitAll = false;
			return true;
		}

		return false;
	}

	/**
	 * Queries for list Of tables
	 *
	 * @return Vector List of tables
	 * @exception Exception
	 *                Thrown if error occurs
	 */
	public Vector<String> queryTables() throws Exception {
		String schema = getParam("jdbcSchema");

		// Backwards compatibility code ?
		if (schema == null || schema.length() == 0) {
			if (!isMSSQLServerDB)
				schema = getParam("jdbcLogin");
			else
				schema = null;
		}
		if (schema == null && !isMSSQLServerDB)
			schema = "";

		DatabaseMetaData dmd = con.getMetaData();
		ResultSet rs = null;
		Vector<String> list = new Vector<String>();
		String[] types = { "TABLE", "VIEW" };
		Exception fail = null;

		try {
			if (isMSSQLServerDB)
				rs = dmd.getTables(null, schema, "%", types);
			else
				rs = dmd.getTables(null, schema, null, types);
		} catch (Exception error) {
			fail = error;
		}

		if (rs == null || !rs.next()) {
			// if no tables are available, try upper case schema name
			if (rs != null) {
				rs.clearWarnings();
				rs.close();
			}

			try {
				rs = dmd.getTables(null, schema.toUpperCase(), null, types); // D717
			} catch (Exception error) {
				fail = error;
			}

			if (rs == null && fail != null)
				throw fail;
		} else {
			// we already called next(), add entry to list
			addTableName(rs, list);
		}

		if (rs != null) {
			while (rs.next()) {
				addTableName(rs, list);
			}
			Collections.sort(list);

			rs.clearWarnings();
			rs.close();
		}

		return list;
	}
	
	/**
	 * Add the TABLE_NAME field, if it exists, to the list.
	 */
	private void addTableName(ResultSet rs, List<String> list) {
		String name = null;
		try {
			name = rs.getString("TABLE_NAME");
		} catch (SQLException e) {}

		if (name == null || name.length() == 0) {
			try {
				name = rs.getString(3);
			} catch (SQLException e) {}
		}

		if (name != null && name.length() > 0)
			list.add(name);
	}

	/**
	 * Query for schema of table
	 *
	 * @param table
	 *            Entry object
	 * @return MetaData of queried table
	 * @exception Exception
	 *                Thrown if error occurs
	 */
	public Object querySchema(Object table) throws Exception {
		String tablename = getParam("jdbcTable");

		// If tablename is not specified try to fetch schema from the jdbcSelect
		// or jdbcLookup parameters if they exist.
		// This would be applicable only in Iterator and Lookup mode
		if (tablename == null || tablename.trim().length() == 0) {
			String connectorMode = ((ConnectorConfig) getConfiguration())
					.getMode();
			String sql = null;

			if (!customPreparedStatements) {
				if (connectorMode.equals(ConnectorConfig.ITERATOR_MODE))
					sql = getParam(SELECT);
				else if (connectorMode.equals(ConnectorConfig.LOOKUP_MODE))
					sql = getParam(FIND);
			}

			if (sql != null && (sql = sql.trim()).length() > 0) {
				return querySchemaSelect(sql);
			}
		}

		String username = getParam("jdbcLogin");

		boolean userTable = false;
		if ((table != null) && !table.toString().equalsIgnoreCase(tablename)) {
			userTable = true;
		}

		if (querySchemaCalled && !userTable) {
			if (metaData == null) {
				if (querySchemaFail != null) {
					throw querySchemaFail;
				}

				throw new Exception(sResHash.getString(
						"CONNECTOR.JDBC.NOMETADATA.ERROR", tablename));
			}
			return metaData;
		}

		if (userTable) {
			tablename = table.toString();
		} else {
			querySchemaCalled = true;
		}

		if ((tablename == null) || (tablename.length() == 0)) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JDBC.MISSING.TABLENAME.ERROR"));
		}

		if (username == null) {
			username = "";
		}

		String schema = getParam("jdbcSchema"); // D533
		if ((schema == null) || (schema.length() == 0)) {
			if (!isMSSQLServerDB)
				schema = username;
			else
				schema = null;
		}

		Exception fail = null;

		try {
			if (schema != null)
				metaData = getList(schema.toUpperCase(), tablename
						.toUpperCase(), "username and tablename in uppercase");
			else
				metaData = getList(null, tablename.toUpperCase(),
						"tablename in uppercase");
		} catch (Exception error) {
			metaData = null;
			fail = error;
		}

		if (metaData == null && schema != null) {
			try {
				metaData = getList(schema, tablename.toUpperCase(),
						"username without uppercase");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		if (metaData == null && schema != null) {
			try {
				metaData = getList(schema.toUpperCase(), tablename,
						"tablename without uppercase");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		if (metaData == null) {
			try {
				metaData = getList(schema, tablename,
						"username and tablename without uppercase");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		if (metaData == null && schema != null) {
			try {
				metaData = getList(null, tablename.toUpperCase(), "no username");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		if (metaData == null && schema != null) {
			try {
				metaData = getList(null, tablename,
						"no username and tablename without uppercase");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		int sepIndex = tablename.indexOf(schemaSeparator);
		if (metaData == null && sepIndex > 0) {
			String s = tablename.substring(0, sepIndex);
			String t = tablename.substring(sepIndex + 1);
			try {
				metaData = getList(s, t, " without uppercase");
			} catch (Exception ignore) {
				metaData = null;
			}
		}

		if (metaData == null) {
			//Try without quotes
			if (tablename.length() > 2 && (
				(tablename.startsWith("'") && tablename.endsWith("'")) ||
				(tablename.startsWith("\"") && tablename.endsWith("\"")) ||
				(tablename.startsWith("`") && tablename.endsWith("`")) ||
				(tablename.startsWith("[") && tablename.endsWith("]")))) {
				return querySchema(tablename.substring(1, tablename.length() - 1));
			}
			if (fail != null) {
				querySchemaFail = fail;
				throw fail;
			}
			metaData = new Vector<Entry>();
		}

		return metaData;

	}

	/**
	 * Queries the column names.
	 *
	 * @param sql
	 *            SQL statement to be executed
	 * @return list with the table columns
	 * @throws Exception
	 */
	private Vector<Entry> querySchemaSelect(String sql) throws Exception {
		if (enableParamSubstitute) {
			sql = ParameterSubstitution.substitute(sql,
					getMap(null, null, true));
		}

		Vector<Entry> list = new Vector<Entry>();
		if (con == null)
			return list;

		Statement stmt = con.createStatement();
		try {
			lastSQL = sql;
			ResultSet rs = stmt.executeQuery(sql);
			if (rs == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.JDBC.EXECUTE.SQL.ERROR", sql));
			}

			try {
				ResultSetMetaData md = rs.getMetaData();

				for (int i = 1; i <= md.getColumnCount(); i++) {
					Entry e = new Entry();
					String name = md.getColumnName(i);
					Integer type = md.getColumnType(i);
					Integer size = md.getColumnDisplaySize(i);
					e.setAttribute("name", name);
					e.setAttribute("syntax", md.getColumnTypeName(i));
					e.setAttribute("size", size);
					e.setAttribute("type", type);
					list.add(e);
					
					nameToType.put(name.toUpperCase(Locale.ENGLISH), type);
					nameSize.put(name.toUpperCase(Locale.ENGLISH), size);

				}
			} finally {
				rs.close();
				if (autoCommitAll)
					con.commit();
			}

		} finally {
			stmt.close();
		}

		return list;
	}

	/**
	 * Returns list of entry objects with meta data information
	 *
	 * @param schema
	 *            Schema
	 * @param tablename
	 *            Name of table
	 * @param msg
	 *            Debug Message
	 *
	 * @return List of entry objects
	 * @exception Exception
	 *                Thrown if error occurs
	 */
	private List<Entry> getList(String schema, String tablename, String msg)
			throws Exception {

		ResultSet rs = null;

		if (con == null)
			return null;

		rs = con.getMetaData().getColumns(null, schema, tablename, null);

		if (rs == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JDBC.GETCOLUMNS.NULL.ERROR"));
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.QUERYSCHEMA.SUCCEED.INFO",
					msg));
		}

		Vector<Entry> list = new Vector<Entry>();

		while (rs.next()) {
			Entry e = new Entry();
			String name = rs.getString("COLUMN_NAME");
			Integer type = rs.getInt("DATA_TYPE");
			Integer size = rs.getInt("COLUMN_SIZE");

			e.setAttribute("name", name);
			e.setAttribute("syntax", rs.getString("TYPE_NAME"));
			e.setAttribute("size", size);
			e.setAttribute("type", type);
			list.add(e);

			metadataEntry.setAttribute(name, type.toString());

			nameToType.put(name.toUpperCase(Locale.ENGLISH), type);
			nameSize.put(name.toUpperCase(Locale.ENGLISH), size);
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.QUERYSCHEMA.RETURN.INFO",
					Integer.valueOf(list.size())));
		}

		rs.close(); // D758

		if (list.size() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JDBC.GETCOLUMNS.EMPTY.LIST.ERROR", tablename));
		}

		return list;
	}

	/**
	 * Returns the Statement handle for the currently open session
	 *
	 * @return The statement value
	 */
	public Statement getStatement() {
		return stmt2;
	}

	/**
	 * Instructs this connector to use the provided result set instead of its
	 * own.
	 *
	 * @param rs
	 *            The new resultSet value
	 *
	 * @exception Exception
	 *                Thrown if error occurs
	 */
	public void setResultSet(ResultSet rs) throws Exception {
		map1 = new SqlMap(rs);
	}

	/**
	 * Returns the Connection handle for the currently open session.
	 *
	 * @return The connection value
	 */
	public Connection getConnection() {
		return con;
	}

	/**
	 * Creates where clause for SQL statement
	 *
	 * @param search
	 *            SearchCriteria
	 * @param ps
	 *            Boolean which determines if is a prepared statement.
	 * @return sql SQL string with where clause
	 *
	 * @throws Exception
	 *             Thrown if error occurs
	 */

	public String getWhereClause(SearchCriteria search, boolean ps)
			throws Exception {

		if (search.getScriptFilter() != null) {
			if (ps) {
				throw new Exception(sResHash
						.getString("CONNECTOR.JDBC.CANNOTBUILD.ADVANCED.ERROR"));
			} else {
				return search.getScriptFilter();
			}
		}

		Vector<?> criteria = search.getCriteria();
		if (criteria.size() == 0) {
			throw new Exception(sResHash
					.getString("CONNECTOR.JDBC.CANNOTBUILD.CRITERIA.ERROR"));
		}

		StringBuffer sql = new StringBuffer();
		for (int i = 0; i < criteria.size(); i++) {

			if (i > 0) {
				sql
						.append((search.getType() == SearchCriteria.SEARCH_AND) ? " AND "
								: " OR ");
			}

			Object obj = criteria.get(i);

			if (obj instanceof SearchCriteria.rscSearch) {
				sql.append(getSubClause((SearchCriteria.rscSearch) obj, ps));
			} else if (obj instanceof SearchCriteria) {
				sql.append("( ");
				sql.append(getWhereClause((SearchCriteria) obj, ps));
				sql.append(" )");
			} else {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.JDBC.CANNOTBUILD.UNCRITERIA.ERROR"));
			}
		}
		return sql.toString();
	}

	/**
	 * Constructs an SQL where expression from an rscSearch class.
	 *
	 * @param rs
	 *            search criteria
	 * @param ps
	 *            Boolean which determines if it is a prepared statement.
	 *
	 * @return The SQL where expression
	 * @throws Exception
	 */
	public String getSubClause(SearchCriteria.rscSearch rs, boolean ps)
			throws Exception {

		String neg = "";
		if (rs.negate)
			neg = " NOT ";

		switch (rs.match) {
		case SearchCriteria.NOT_STRING:
			neg = (neg.equals("") ? "NOT " : "");

			// No matter what SCA tools says, there must not be a break here.
			// Please allow the flow to continue to the next statement, and
			// do not insert a break here.
		case SearchCriteria.EXACT:
			if (ps)
				return (neg + rs.name + "= ?");
			else
				return (neg + rs.name + "=" + sqlValue(rs.name, rs.value,
						padLookupString));
		case SearchCriteria.LESS_THAN:
			if (ps)
				return (neg + rs.name + "< ?");
			else
				return (neg + rs.name + "<" + sqlValue(rs.name, rs.value,
						padLookupString));
		case SearchCriteria.LESS_THAN_OR_EQUAL:
			if (ps)
				return (neg + rs.name + "<= ?");
			else
				return (neg + rs.name + "<=" + sqlValue(rs.name, rs.value,
						padLookupString));
		case SearchCriteria.GREATER_THAN:
			if (ps)
				return (neg + rs.name + "> ?");
			else
				return (neg + rs.name + ">" + sqlValue(rs.name, rs.value,
						padLookupString));
		case SearchCriteria.GREATER_THAN_OR_EQUAL:
			if (ps)
				return (neg + rs.name + ">= ?");
			else
				return (neg + rs.name + ">=" + sqlValue(rs.name, rs.value,
						padLookupString));
		case SearchCriteria.SUBSTRING:
			if (ps) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.JDBC.CANNOTBUILD.SUBSTRMATCH.ERROR"));
			} else {
				return (rs.name + neg + " LIKE '%" + rs.value + "%'");
			}
		case SearchCriteria.INITIAL_STRING:
			if (ps) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.JDBC.CANNOTBUILD.INITSTRINGMATCH.ERROR"));
			} else {
				return (rs.name + neg + " LIKE '" + rs.value + "%'");
			}
		case SearchCriteria.FINAL_STRING:
			if (ps) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.JDBC.CANNOTBUILD.FINALSTRINGMATCH.ERROR"));
			} else {
				return (rs.name + neg + " LIKE '%" + rs.value + "'");
			}
		}

		throw new Exception(sResHash
				.getString("CONNECTOR.JDBC.CANNOTBUILD.UNCRITERIA.ERROR"));
	}

	/**
	 * Converts java type to SQL type
	 *
	 * @param name
	 *            name of the type
	 * @param value
	 *            type
	 * @param padString
	 *            true if padding enabled
	 * @return the SQL type
	 */
	public String sqlValue(String name, Object value, boolean padString) {

		if (value instanceof Attribute) {
			value = ((Attribute) value).getValue(0);
		}

		if (value == null)
			return "null";

		if (value instanceof byte[])
			return bytesAsHex((byte[]) value);

		Integer type = getMapValue(nameToType, name);

		if (type == null)
			type = typeFromValue(value);

		switch (type.intValue()) {
		case Types.INTEGER:
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.BIT:

			return value.toString();

		case Types.TIMESTAMP:
			String str2 = value.toString();
			if (str2.startsWith("{"))
				return str2;
			else
				return "{ts '" + str2 + "'}";

		case Types.DATE:
		case Types.TIME:
		case 11:

			String str = value.toString();
			if (str.startsWith("{"))
				return str;
			else
				return "{d '" + str + "'}";

		case Types.CHAR:
			StringBuffer sb = new StringBuffer(value.toString());
			Integer size = getMapValue(nameSize, name);
			if (size != null) {
				int n = size.intValue();
				if (padString) {
					while (sb.length() < n)
						sb.append(' ');
				}
				while (sb.length() > n) {
					int l = sb.length() - 1;
					if (Character.isWhitespace(sb.charAt(l))) {
						sb.deleteCharAt(l);
					} else
						break;
				}
			}
			return "'" + formatSQLString(sb.toString()) + "'";

		default:
			return "'" + formatSQLString(value.toString()) + "'";
		}
	}

	/**
	 * Return a Hex String representation of a byte array
	 *
	 * @param b
	 *            byte array
	 * @return the corresponding String
	 */
	private String bytesAsHex(byte[] b) {
		StringBuffer s;
		if (!isMSSQLServerDB)
			s = new StringBuffer("X'");
		else
			s = new StringBuffer("0X");

		for (int i = 0; i < b.length; i++)
			s.append(StringUtils.toHex(b[i]));
		if (!isMSSQLServerDB)
			s.append("'");
		return s.toString();
	}

	/**
	 * Guess the SQL type given a Java Object
	 * @param value the Java Object
	 * @return The SQL tyoe
	 */
	private Integer typeFromValue(Object value) {
		if (value instanceof String || value == null)
			return Types.VARCHAR;
		if (value instanceof Integer || value instanceof Long
				|| value instanceof java.math.BigInteger)
			return Types.INTEGER;
		else if (value instanceof Float)
			return Types.FLOAT;
		else if (value instanceof Double || value instanceof java.math.BigDecimal)
			return Types.DOUBLE;
		else if (value instanceof Boolean)
			return Types.BIT;
		else if (value instanceof java.sql.Date)
			return Types.DATE;
		else if (value instanceof java.sql.Time)
			return Types.TIME;
		else if (value instanceof java.util.Date
				|| value instanceof java.sql.Timestamp)
			return Types.TIMESTAMP;
		else
			return Types.VARCHAR;
	}

	/**
	 * Formats the SQL string
	 *
	 * @param str
	 *            String to be formatted
	 * @return String Formatted String
	 */
	private String formatSQLString(String str) {
		if (str.length() == 0)
			return " ";

		StringBuffer res = new StringBuffer();

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\'') {
				res.append("'");
			}
			res.append(str.charAt(i));
		}
		return res.toString();
	}

	/**
	 * Saves the Meta Data info
	 *
	 * @param md
	 *            ResultSetMetaData
	 * @throws SQLException
	 *             Thrown if there is an error
	 */
	public void saveMetaData(ResultSetMetaData md) throws SQLException {

		for (int i = 1; i <= md.getColumnCount(); i++) {
			String name = md.getColumnName(i).toUpperCase(Locale.ENGLISH);
			metadataEntry.setAttribute(name, md.getColumnTypeName(i));
			nameToType.put(name, md.getColumnType(i));
			nameSize.put(name, md.getColumnDisplaySize(i));
		}
	}

	/**
	 * Class for constructing prepared SQL statements
	 *
	 */
	private class PreparedSql {
		/**
		 * prepared statements
		 */
		private PreparedStatement ps;

		/**
		 * Index of value to set in the prepared statement
		 */
		private int index;

		/**
		 * Constructs prepared statement
		 *
		 * @param sql
		 *           SQL statement
		 * @throws Exception
		 *             if an error occurs
		 */
		public PreparedSql(String sql) throws Exception {

			ps = con.prepareStatement(sql);
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.PREPARED.STATEMENT.INFO", sql));
			}
			index = 0;
		}

		/**
		 * Execute Query
		 *
		 * @return ResultSet
		 * @throws Exception
		 */
		public ResultSet executeQuery() throws Exception {
			return ps.executeQuery();
		}

		/**
		 * Execute Update
		 *
		 * @return Number of rows affected
		 * @throws Exception
		 */
		public int executeUpdate() throws Exception {
			return ps.executeUpdate();
		}

		/**
		 *
		 * @param search
		 *            SearchCriteria
		 * @throws Exception
		 */
		public void setValues(SearchCriteria search) throws Exception {

			for (Object obj : search.getCriteria()) {

				if (obj instanceof SearchCriteria.rscSearch) {
					setPSValue(((SearchCriteria.rscSearch) obj).name,
							((SearchCriteria.rscSearch) obj).value,
							padLookupString);
					/*
					 * setPSValue(((SearchCriteria.rscSearch) obj).name,
					 * ((SearchCriteria.rscSearch) obj).value);
					 */
				} else if (obj instanceof SearchCriteria) {
					setValues((SearchCriteria) obj);
				}
			}
		}

		/**
		 * Resets the index so that setValues() start from the beginning
		 */
		public void reset() {
			index = 0;
		}

		/**
		 * Sets a value in the prepared statement
		 *
		 * @param name
		 *            DB name of the type
		 * @param val
		 *            value to set
		 * @param padString
		 *            use padding
		 * @throws Exception
		 *             if an error occurs
		 */
		private void setPSValue(String name, Object val, boolean padString)
				throws Exception {

			index++;

			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.INFO",
						new Object[] { name, val, Integer.valueOf(index) }));
			}

			Integer type = getMapValue(nameToType, name);

			if (type == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.JDBC.NOMETADATA.DESCR.ERROR", name));
			}

			int colsize = getMapValue(nameSize, name);

			setPreparedValue(ps, val, index, type, colsize, padString, name);
		}

		/**
		 * closes the prepared statement
		 */
		public void close() {
			try {
				if (ps.getWarnings() != null) {
					logmsg(sResHash.getString(
							"CONNECTOR.JDBC.SQL.WARN.WARNING", ps.getWarnings()
									.toString()));
				}
				ps.clearWarnings();
				ps.close();
			} catch (Exception ignore) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.SQL.WARN.WARNING",
							ignore.getMessage()));
				}
			}
		}
	}

	/**
	 * Sets a value in a PreparedStatement
	 * @param ps The PreparedStatement
	 * @param val The value to set
	 * @param index The index in the PreparedStatement
	 * @param type The type that val should be converted to
	 * @param colsize The column size, used when padding
	 * @param padString True if a String should be padded
	 * @param name The name of the column, if possible
	 * @throws Exception If the value could not be set
	 */
	private void setPreparedValue(PreparedStatement ps, Object val, int index,
			Integer type, int colsize, boolean padString, String name) throws Exception {
		if (val instanceof Attribute) {
			val = ((Attribute) val).getValue(0);
		}

		if (val == null) {
			ps.setNull(index, type.intValue());
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.SETNULLPSVALUE.INFO", Integer
								.valueOf(index)));
			}
			return;
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.JDBC.VALCLASS.INFO", val
					.getClass().getName()));
			debug(sResHash.getString("CONNECTOR.JDBC.VALTYPE.INFO", type));
		}

		switch (type.intValue()) {
		// String & Character Data
		case Types.LONGVARCHAR:
		case Types.VARCHAR:
			if (debugMode()) {
				if (type.intValue() == Types.LONGVARCHAR)
					debug(sResHash.getString(
							"CONNECTOR.JDBC.LONGVARCHAR.INFO", val
									.toString()));
				else
					debug(sResHash.getString("CONNECTOR.JDBC.VARCHAR.INFO",
							val.toString()));
			}

			ps.setString(index, val.toString());
			return;
		case Types.CHAR:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.CHAR.INFO", val
						.toString()));
			}

			StringBuffer strval = new StringBuffer(val.toString());
			if (colsize > strval.length()) {
				if (debugMode()) {
					if (padString) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.CHARINF.INFO",
								new Object[] { Integer.valueOf(colsize),
										Integer.valueOf(strval.length()) }));
					} else {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.CHAR.PAD.DISABLE.INFO",
								new Object[] { Integer.valueOf(colsize),
										Integer.valueOf(strval.length()) }));
					}
				}

				if (padString) {
					while (colsize > strval.length()) {
						strval.append(' ');
					}
				}

				while (colsize < strval.length()) {
					int trm = strval.length() - 1;
					if (Character.isWhitespace(strval.charAt(trm))) {
						strval.deleteCharAt(trm);
					} else {
						break;
					}
				}
			}

			ps.setString(index, strval.toString());
			return;
		case Types.OTHER:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.OTHERVALUE.INFO",
						val.toString()));
			}
			ps.setString(index, val.toString());
			return;
			// Numbers
		case Types.INTEGER:
		case Types.TINYINT:
		case Types.SMALLINT:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.NUMVALUE.INFO",
						val.toString()));
			}

			if (val instanceof Integer) {
				ps.setInt(index, ((Integer) val).intValue());
			} else if (val instanceof Float) {
				ps.setInt(index, ((Float) val).intValue());
			} else if (val instanceof Double) {
				ps.setInt(index, ((Double) val).intValue());
			} else if (val instanceof Long) {
				ps.setLong(index, ((Long) val).longValue());
			} else if (val instanceof java.math.BigInteger) {
				ps.setLong(index, ((java.math.BigInteger) val).longValue());
			} else {
				try {
					ps.setInt(index, Integer.parseInt(val.toString()));
				} catch (java.lang.NumberFormatException nfe) {
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.PARSINT.WARNING", nfe));
					}
					java.math.BigInteger bigint = new java.math.BigInteger(
							val.toString());
					ps.setLong(index, bigint.longValue());
				}
			}
			return;
		case Types.DECIMAL:
		case Types.BIGINT:
		case Types.NUMERIC:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DEC.BIG.NUM.INFO",
						val.toString()));
			}
			if (val instanceof Integer) {
				ps.setInt(index, ((Integer) val).intValue());
			} else if (val instanceof Float) {
				ps.setFloat(index, ((Float) val).floatValue());
			} else if (val instanceof Double) {
				ps.setDouble(index, ((Double) val).doubleValue());
			} else if (val instanceof Long) {
				ps.setLong(index, ((Long) val).longValue());
			} else if (val instanceof java.math.BigInteger) {
				ps.setLong(index, ((java.math.BigInteger) val).longValue());
			} else if (val instanceof Long) {
				ps.setLong(index, ((Long) val).longValue()); // pmr 37394
			} else if (val instanceof String) {
				if (type.intValue() == Types.BIGINT) {
					ps.setLong(index, Long.parseLong(val.toString()));
				} else {
					ps.setDouble(index, Double.parseDouble(val.toString()));
				}
			} else {
				try {
					ps.setDouble(index, Double.parseDouble(val.toString()));
				} catch (java.lang.NumberFormatException nfe) {
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.PARSDOUBLE.WARNING", nfe));
					}
					java.math.BigDecimal bigdec = new java.math.BigDecimal(
							val.toString());
					ps.setDouble(index, bigdec.doubleValue());
				}
			}
			return;
		case Types.FLOAT:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.PARSFLOAT.INFO",
						val.toString()));
			}
			if (val instanceof Float) {
				ps.setFloat(index, ((Float) val).floatValue());
			} else if (val instanceof Integer) {
				ps.setFloat(index, ((Integer) val).intValue());
			} else if (val instanceof Double) {
				ps.setFloat(index, ((Double) val).floatValue());
			} else if (val instanceof Long) {
				ps.setFloat(index, ((Long) val).floatValue());
			} else if (val instanceof java.math.BigInteger) {
				ps.setFloat(index, ((java.math.BigInteger) val)
						.floatValue());
			} else {
				ps.setFloat(index, Float.parseFloat(val.toString()));
			}
			return;
		case Types.REAL:
		case Types.DOUBLE:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.REALDOUBLE.INFO",
						val.toString()));
			}
			if (val instanceof Double) {
				ps.setDouble(index, ((Double) val).doubleValue());
			} else if (val instanceof Float) {
				ps.setDouble(index, ((Float) val).doubleValue());
			} else if (val instanceof Integer) {
				ps.setDouble(index, ((Integer) val).intValue());
			} else if (val instanceof Long) {
				ps.setDouble(index, ((Long) val).doubleValue());
			} else if (val instanceof java.math.BigInteger) {
				ps.setDouble(index, ((java.math.BigInteger) val)
						.doubleValue());
			} else {
				ps.setDouble(index, Double.parseDouble(val.toString()));
			}
			return;
			// Boolean
		case Types.BIT:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.BIT.INFO", val
						.toString()));
			}
			if (val instanceof java.lang.Boolean) {
				ps.setBoolean(index, ((java.lang.Boolean) val)
						.booleanValue());
			} else {
				String stringValue = val.toString();
				if (stringValue.compareToIgnoreCase("true") == 0
						|| stringValue.compareToIgnoreCase("yes") == 0
						|| stringValue.compareToIgnoreCase("Y") == 0) {
					ps.setBoolean(index, true);
				} else if (stringValue.compareToIgnoreCase("false") == 0
						|| stringValue.compareToIgnoreCase("no") == 0
						|| stringValue.compareToIgnoreCase("N") == 0) {
					ps.setBoolean(index, false);
				} else {
					// The best is probably to throw an Exception here,
					// with the name of the field, and the value we could
					// not parse.
					throw new Exception(sResHash.getString(
							"CONNECTOR.JDBC.BIT.INFO.NOTSET", new Object[] {
									name, val.toString() }));

				}
			}
			return;

			// Date/Time
		case Types.TIMESTAMP:
		case 11:
			// Java 1.2 TIMESTAMP
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DATETIME.INFO",
						new Object[] { val.getClass().getName(), val.toString() }));
			}
			if (val instanceof java.sql.Timestamp) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.TIMESTAMP.INFO", val.toString()));
				}
				ps.setTimestamp(index, (java.sql.Timestamp) val);
				return;
			}
			if (val instanceof java.util.Date) {
				// Convert the java.util.Date to a java.sql.Timestamp
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.UTILDATE.INFO", val.toString()));
				}
				java.sql.Timestamp dd = new java.sql.Timestamp(((java.util.Date) val).getTime());
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.UTILDATE.CONVERTED.INFO", dd.toString()));
				}
				ps.setTimestamp(index, dd);
				return;
			}

			java.util.Date d1 = parseDate(val.toString(), "yyyy-MM-dd HH:mm:ss");
			if (d1 != null) {
				ps.setTimestamp(index, new java.sql.Timestamp(d1.getTime()));
				return;
			}

			try {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.CONVERT.TO.LONGVAL.INFO", val.toString()));
				}
				long dt = Long.parseLong(val.toString());
				ps.setTimestamp(index, new java.sql.Timestamp(dt));
			} catch (Exception ignore) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.WARNING.NOTSET",
							new Object[] { ignore.toString(),
									val.getClass().getName(),
									val.toString() }));
				}
			}
			return;

		case Types.DATE:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DATETIME.INFO",
						new Object[] { val.getClass().getName(), val.toString() }));
			}
			if (val instanceof java.sql.Date) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.DATE.INFO", val.toString()));
				}
				ps.setDate(index, (java.sql.Date) val);
				return;
			}

			if (val instanceof java.sql.Timestamp) {
                                  if (debugMode()) {
                                          debug(sResHash.getString("CONNECTOR.JDBC.TIMESTAMP.INFO", val.toString()));
                                  }
                                  ps.setTimestamp(index, (java.sql.Timestamp) val);
                                  return;
                        }

			if (val instanceof java.util.Date) {
				// Convert the java.util.Date to a java.sql.Date
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.UTILDATE.INFO", val.toString()));
				}
				java.sql.Date dd = new java.sql.Date(((java.util.Date) val).getTime());
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.DATE.INFO", dd.toString()));
				}
				ps.setDate(index, dd);
				return;
			}

			java.util.Date d2 = parseDate(val.toString(), "yyyy-MM-dd");
			if (d2 != null) {
				ps.setDate(index, new java.sql.Date(d2.getTime()));
				return;
			}

			try {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.CONVERT.TO.LONGVAL.INFO", val.toString()));
				}
				long dt = Long.parseLong(val.toString());
				ps.setDate(index, new java.sql.Date(dt));
			} catch (Exception ignore) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.WARNING.NOTSET",
							new Object[] { ignore.toString(),
									val.getClass().getName(),
									val.toString() }));
				}
			}
			return;

		case Types.TIME:
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.DATETIME.INFO",
						new Object[] { val.getClass().getName(), val.toString() }));
			}
			if (val instanceof java.sql.Time) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.TIME.INFO", val.toString()));
				}
				ps.setTime(index, (java.sql.Time) val);
				return;
			}
			if (val instanceof java.util.Date) {
				// Convert the java.util.Date to a java.sql.Time
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.UTILDATE.INFO", val.toString()));
				}
				java.sql.Time dd = new java.sql.Time(((java.util.Date) val).getTime());
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.TIME.INFO", dd.toString()));
				}
				ps.setTime(index, dd);
				return;
			}

			java.util.Date d3 = parseDate(val.toString(), "HH:mm:ss");
			if (d3 != null) {
				ps.setTime(index, new java.sql.Time(d3.getTime()));
				return;
			}

			try {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.CONVERT.TO.LONGVAL.INFO", val.toString()));
				}
				long dt = Long.parseLong(val.toString());
				ps.setTime(index, new java.sql.Time(dt));
			} catch (Exception ignore) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.WARNING.NOTSET",
							new Object[] { ignore.toString(),
									val.getClass().getName(),
									val.toString() }));
				}
			}
			return;

		case Types.BLOB:
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.CONVERT.TO.BLOB.INFO", val
								.toString()));
			}
			// ps.setBlob(index, (java.sql.Blob) val);
			ps.setObject(index, val);
			return;
		case Types.LONGVARBINARY:
			ps.setObject(index, val, Types.LONGVARBINARY);
			return;

		case Types.VARBINARY:
			ps.setObject(index, val, Types.VARBINARY);
			return;
		case Types.BINARY:
			ps.setObject(index, val, Types.BINARY);
			return;

		case Types.CLOB:
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.CONVERT.TO.CLOB.INFO", val
								.toString()));
			}
			if (val instanceof java.sql.Clob)
				ps.setClob(index, (java.sql.Clob) val);
			else
				ps.setObject(index, val);
			return;

		default:
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.UNHANDLED.TYPE.INFO", new Object[] {
								type, name }));
			}

			ps.setString(index, val.toString());
			return;
		}
	}

	/**
	 * Parse a String into a Date, trying a few different formats.
	 * @param value The String to parse
	 * @param simpleFormat A guess at the format a String could possibly have
	 * @return The Date, or null if not possible to parse
	 */
	private java.util.Date parseDate(String value, String simpleFormat) {
		try {
			String defform = getParam("jdbcDateFormat");
			if (defform != null && defform.trim().length() > 0) {
				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.TRY.SAMPLE.DATEFORMAT.INFO", defform));
				}
				SimpleDateFormat df1 = new SimpleDateFormat(defform);
				return df1.parse(value);
			}
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.WARNING", ignore.toString()));
			}
		}

		try {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.TRY.DATEFORMAT.INFO", value));
			}
			return DateFormat.getInstance().parse(value);
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.DATEFORMAT.WARNING", ignore.toString()));
			}
		}

		try {
			SimpleDateFormat df1 = new SimpleDateFormat(simpleFormat);
			java.util.Date d = df1.parse(value);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.TRY.SIMPLE.DATE.INFO", d.toString()));
			}
			return d;
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.TRY.SIMPLE.DATEFORMAT.PAR.INFO"));
			}
		}

		try {
			// We keep this format for backwards compatibility
			SimpleDateFormat df1 = new SimpleDateFormat("yyyy.MM.dd hh:mm");
			java.util.Date d = df1.parse(value);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.TRY.SIMPLE.DATE.INFO", d.toString()));
			}
			return d;
		} catch (Exception ignore) {
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.JDBC.SETPSVALUE.SETSIMPLE.WARNING", ignore.toString()));
			}
		}
		return null;
	}

	/**
	 * Private class for converting SQL result to an entry
	 *
	 */
	private class SqlMap {
		/**
		 * Result set returned from the DB
		 */
		private ResultSet rs;

		/**
		 * Meta data returned from the DB
		 */
		private ResultSetMetaData md;

		/**
		 * Constructs result set and meta data
		 *
		 * @param rs
		 *            result set from the DB
		 * @throws SQLException
		 *             if an error occurs
		 */
		public SqlMap(ResultSet rs) throws SQLException {
			this.rs = rs;
			md = rs.getMetaData();
			saveMetaData(md);
		}

		/**
		 * This method is used to build entry objects where the column names are
		 * mapped to attribute names and column valus to attribute value
		 *
		 * @return Entry object
		 * @exception Exception
		 *                Any exceptions thrown by the connector's underlying
		 *                classes
		 */
		public Entry buildEntry() throws Exception {

			if (!rs.next())
				return null;

			Entry entry = new Entry();

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.ENTER.BUILD.ENTRY.INFO"));
			}

			for (int i = 1; i <= md.getColumnCount(); i++) {

				String name = md.getColumnName(i);
				Object val = null;

				if (debugMode()) {
					debug(sResHash.getString("CONNECTOR.JDBC.COLUMN.NAME.INFO",
							name));
				}

				switch (md.getColumnType(i)) {

				// String & Character Data
				case Types.CHAR:
				case Types.VARCHAR:
					val = rs.getString(i);
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.CHAR.VARCHAR.INFO", val));
					}
					break;
				// Probably NCHAR or NVARCHAR ?
				case Types.OTHER:
					val = rs.getString(i);
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.OTHER.INFO", val));
					}
					break;
				case Types.LONGVARBINARY:
					if (debugMode()) {
						debug(sResHash
								.getString("CONNECTOR.JDBC.GET.LONGVARBINARY.INFO"));
					}
					java.io.InputStream bis = rs.getBinaryStream(i);
					if (bis == null)
						break;
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
						if (ignoreFieldErrors) {
							val = ioe;
						} else {
							throw ioe;
						}
					}

					break;
				case Types.LONGVARCHAR:
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.JDBC.GET.LONGVARCHAR.INFO"));
					}
					val = rs.getString(i);
					break;

				// Numbers
				case Types.INTEGER:
				case Types.TINYINT:
				case Types.SMALLINT:
					val = Integer.valueOf(rs.getInt(i));

					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.DECIMAL.INTEGER.INFO", val));
					}

					if (rs.wasNull()) {
						if (debugMode()) {
							debug(sResHash
									.getString(
											"CONNECTOR.JDBC.NULL.VALUE.FOR.INT.WARNING",
											val));
						}
						val = null;
					}
					break;

				// Real & Float
				case Types.BIGINT:
				case Types.DECIMAL:
				case Types.NUMERIC:
					val = rs.getObject(i);
					/*
					 * if ( md.getColumnDisplaySize(i) > 10 ) val = new Long
					 * (rs.getLong (i)); else val = new Integer(rs.getInt(i));
					 */
					// val = new java.lang.Double(rs.getDouble(i));
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.NUMERIC.INFO", val));
					}
					if (rs.wasNull()) {
						if (debugMode()) {
							debug(sResHash
									.getString(
											"CONNECTOR.JDBC.NULL.VALUE.FOR.NUMERIC.WARNING",
											val));
						}
						val = null;
					}
					break;
				case Types.REAL:
				case Types.FLOAT:
				case Types.DOUBLE:
					val = new java.lang.Double(rs.getDouble(i));
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.REAL.FLOAT.DOUBLE.INFO",
								val));
					}

					if (rs.wasNull()) {
						if (debugMode()) {
							debug(sResHash
									.getString(
											"CONNECTOR.JDBC.NULL.VALUE.FOR.REAL.FLOAT.DOUBLE.WARNING",
											val));
						}
						val = null;
					}
					break;
				// Boolean
				case Types.BIT:
					// val = (rs.getBoolean(i) ? "true" : "false");
					val = Boolean.valueOf(rs.getBoolean(i));
					break;
				// Date/Time
				case Types.DATE:
					val = rs.getDate(i);
					if (debugMode()) {
						if (val != null) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.GET.DATE.INFO", val
											.getClass().getName()));
						} else {
							debug(sResHash
									.getString("CONNECTOR.JDBC.NULL.VALUE.FOR.DATE.WARNING"));
						}
					}
					break;
				case Types.TIME:
					val = rs.getTime(i);
					if (debugMode()) {
						if (val != null) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.GET.TIME.INFO", val
											.getClass().getName()));
						} else {
							debug(sResHash
									.getString("CONNECTOR.JDBC.NULL.VALUE.FOR.TIME.WARNING"));
						}
					}
					break;
				case Types.TIMESTAMP:
				case 11:
					val = rs.getTimestamp(i);
					if (debugMode()) {
						if (val != null) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.GET.TIMESTAMP.INFO", val
											.getClass().getName()));
						} else {
							debug(sResHash
									.getString("CONNECTOR.JDBC.NULL.VALUE.FOR.TIMESTAMP.WARNING"));
						}
					}
					break;
				case Types.VARBINARY:
					try {
						val = rs.getBytes(i);
					} catch (Exception ignore) {
						val = rs.getObject(i);
					}
					if (debugMode()) {
						if (val != null) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.GET.VARBINARY.INFO", val
											.getClass().getName()));
						} else {
							debug(sResHash
									.getString("CONNECTOR.JDBC.NULL.VALUE.FOR.VARBINARY.WARNING"));
						}
					}
					break;

				case Types.BLOB:
					Blob blob = rs.getBlob(i);
					if (null != blob) {
						java.io.InputStream bisb = blob.getBinaryStream();

						if (bisb == null)
							break;
						try {
							int ch;
							ByteArrayOutputStream ba = new ByteArrayOutputStream();
							while ((ch = bisb.read()) != -1) {
								ba.write(ch);
							}
							ba.close();
							val = ba.toByteArray();
						} catch (java.io.IOException ioe) {
							bisb.close();
							if (ignoreFieldErrors) {
								val = ioe;
							} else {
								throw ioe;
							}
						}
					}
					break;

				case Types.CLOB:
					Clob clob = rs.getClob(i);
					if (clob == null)
						break;
					java.io.InputStream isc = clob.getAsciiStream();
					if (isc == null)
						break;
					try {
						int ch;
						StringBuffer buf = new StringBuffer();
						while ((ch = isc.read()) != -1) {
							buf.append((char) ch);
						}
						isc.close();
						val = buf.toString();
					} catch (java.io.IOException ioe) {
						isc.close();
						if (ignoreFieldErrors) {
							val = ioe;
						} else {
							throw ioe;
						}
					}
					break;

				case Types.BINARY:
				case Types.JAVA_OBJECT:
				case Types.DISTINCT:
				case Types.STRUCT:
				case Types.ARRAY:

				case Types.REF:
				default:
					val = rs.getObject(i);
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.GET.OBJECT.INFO",
								(val == null ? "null" : val.getClass()
										.getName())));
					}
					break;
				}
				if (debugMode()) {
					if (val != null) {
						if (val instanceof Object) {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.RESULT.CLASS.INFO",
									new Object[] { name, val,
											val.getClass().getName() }));
						} else {
							debug(sResHash.getString(
									"CONNECTOR.JDBC.RESULT.BASIC.INFO",
									new Object[] { name, val }));
						}
					} else {
						debug(sResHash.getString(
								"CONNECTOR.JDBC.RESULT.NULL.INFO", name));
					}
				}

				if (val == null && exposeNullValuesAsEmptyStrings) {
					entry.newAttribute(name);
				} else {
					entry.setAttribute(name, val);
				}
			}

			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.JDBC.EXIT.BUILD.ENTRY.INFO"));
			}

			return entry;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCurrent(Entry entry, SearchCriteria search) {
		if (entry == null || search == null)
			return;

		String[] params = entry.getAttributeNames();
		for (int j = 0; j < params.length; j++) {
			Object value = entry.getObject(params[j]);
			if (value != null) {
				search.replaceCriteria(params[j], value);
			}
		}
	}

	/**
	 * Returns a map with information about connector, DB metadata, columns ,
	 * search criteria and where clause of the statement
	 *
	 * @param conn
	 *            entry object
	 * @param search
	 *            search criteria for the entry
	 * @param padString
	 *            use padding
	 * @return hash map with the DB and connector information
	 */
	private HashMap<String, Object> getMap(Entry conn, SearchCriteria search,
			boolean padString) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("config", getRawConnectorConfiguration());

		if (getConfiguration() != null) {
			map.put("mc", ((ConnectorConfig) getConfiguration())
					.getMetamergeConfig());
		}

		map.put("Connector", this);
		map.put("metadata", metadataEntry);
		if (conn != null) {
			map.put("conn", conn);
			map.put("columns", getColumns(conn, 0, padString));
		}

		if (search != null) {
			map.put("work", search.getCurrentEntry());

			Vector<?> criteria = search.getCriteria();

			// Put "link" in for javascript, and "link[0]" and so on for
			// ParameterSubstitution
			map.put("link", criteria.toArray());

			for (int i = 0; i < criteria.size(); i++) {

				Object obj = criteria.get(i);

				if (obj instanceof SearchCriteria.rscSearch) {
					SearchCriteria.rscSearch s = (SearchCriteria.rscSearch) obj;
					Entry e = new Entry();
					e.setAttribute("name", s.name);
					e.setAttribute("value", s.value);
					e.setAttribute("match", new String(
							new char[] { (char) s.match }));
					e.setAttribute("negate", "" + s.negate);
					map.put("link[" + i + "]", e);
				}
			}

			try {
				map.put("whereClause", getWhereClause(search, false));
			} catch (Exception ignore) {
				debug(sResHash.getString(
						"CONNECTOR.JDBC.ERROR.WHILE.CREATING.SQL", ignore
								.toString()));
			}
		}

		return map;
	}

	/**
	 * Returns String with listed column names
	 *
	 * @param conn
	 *            entry holding the name of the columns as attributes
	 * @param type
	 *            0-appends the column name, 1-appends '?'
	 * @param padString
	 * @return string with the names
	 */
	private String getColumns(Entry conn, int type, boolean padString) {
		String[] names = conn.getAttributeNames();
		StringBuffer res = new StringBuffer();
		boolean empty = true;

		for (int i = 0; i < names.length; i++) {
			Attribute a = conn.getAttribute(names[i]);
			if (a.getValue() == null)
				continue;

			if (!empty)
				res.append(",");

			if (type == 0)
				res.append(names[i]);
			else if (type == 1)
				res.append("?");
			else
				res.append(sqlValue(names[i], a, padString));

			empty = false;
		}
		return res.toString();
	}

	/**
	 * Return version information
	 *
	 * @return The version value
	 */
	public String getVersion() {
		return VERSION_INFO;
	}

	/**
	 * set enableParamSubstitute parameter
	 *
	 * @param val
	 *            true if parameter substitution is enabled
	 *
	 */
	public void setParameterSubstitution(boolean val) {
		enableParamSubstitute = val;
	}

	/**
	 * Returns value of enableParamSubstitute parameter
	 *
	 * @return true
	 *
	 */
	public boolean getParameterSubstitution() {
		return enableParamSubstitute;
	}

	/**
	 * Returns the number of affected entries after skipping lookup
	 *
	 * @return number of affected entries
	 */
	public int getNumSkipLookupAffected() {
		return numEntriesAffected;
	}

	// handle end of cycle flag
	/**
	 * Checks whether the end of cycle is reached
	 *
	 * @return true if EOC is reached
	 */
	public boolean isEOCflag() {
		return end_of_cycle_flag;
	}

	/**
	 * Sets the EOC flag
	 *
	 * @param eoc
	 *            true if EOC is reached
	 */
	public void setEOCflag(boolean eoc) {
		this.end_of_cycle_flag = eoc;
	}

	/**
	 * Sets a prepared statement for future calls to selectEntries().
	 * This method must be called after the connector has been initialized.
	 * As an example of how to use this, this code could be put in the Before Selection Hook:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedSelectStatement("Select * from tableName where fieldName = ? and field2= ?")
		ps.setInteger(1, someValue)
		ps.setObject(2, someObject)
		</pre>
	 * Once setPreparedSelectStatement is called, that PreparedStatement will be used for every selectEntries() from then on.
	 * Calling the method with null as parameter could reset the behavior to using the GUI defined parameters.
	 * @param preparedSql The prepared statement (string) to use.
	 * @return the PreparedStatement. null is returned if a PreparedStatement could not be created.
	 * @throws SQLException if the PreparedStatement could not be created
	 * @since 7.1
	 */
	public PreparedStatement setPreparedSelectStatement(String preparedSql) throws Exception {
		return setPrepared(SELECT, preparedSql);
	}

	/**
	 * Sets a prepared statement for future calls to findEntries().
	 * This method must be called after the connector has been initialized.
	 * As an example of how to use this, this code could be put in the After Initialize Hook:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedFindStatement("Select * from tableName where fieldName = ? and field2= ?")
		</pre>
	 * And this code could be put in the Before Lookup Hook:
	 * <pre>
		ps.setInteger(1, someValue)
		ps.setObject(2, someObject)
		</pre>
	 * Once setPreparedFindStatement is called, that PreparedStatement will be used for every findEntries() from then on,
	 * effectively overriding any Link Criteria.
	 * Calling the method with null as parameter could reset the behavior to using the GUI defined parameters.
	 * @param preparedSql The prepared statement (string) to use.
	 * @return the PreparedStatement. null is returned if a PreparedStatement could not be created.
	 * @throws SQLException if the PreparedStatement could not be created
	 * @since 7.1
	 */
	public PreparedStatement setPreparedFindStatement(String preparedSql) throws Exception {
		return setPrepared(FIND, preparedSql);
	}

	/**
	 * Sets a prepared statement for future calls to modEntry().
	 * This method must be called after the connector has been initialized.
	 * As an example of how to use this, this code could be put in the Before Modify Hook:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedModifyStatement("UPDATE tableName SET fieldName1 = ?, field2 = ? WHERE field3 = ?")
		ps.setTime(1, conn.fieldName1)
		ps.setObject(2, conn.field2)
		ps.setInteger(3, conn.field3)
		</pre>
	 * Once setPreparedModifyStatement is called, that PreparedStatement will be used for every modEntry() from then on,
	 * effectively overriding any Link Criteria, and also ignoring all values in the conn Entry.
	 * Calling the method with null as parameter will reset the behavior to using the GUI defined parameters.
	 * @param preparedSql The prepared statement (string) to use.
	 * @return the PreparedStatement. null is returned if a PreparedStatement could not be created.
	 * @throws SQLException if the PreparedStatement could not be created
	 * @since 7.1
	 */
	public PreparedStatement setPreparedModifyStatement(String preparedSql) throws Exception {
		return setPrepared(MODIFY, preparedSql);
	}

	/**
	 * Sets a prepared statement for future calls to putEntry().
	 * This method must be called after the connector has been initialized.
	 * As an example of how to use this, this code could be put in the Before Add Hook:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedInsertStatement("INSERT into tableName (fieldName1,field2,field3) VALUES (?,?,?)")
		ps.setString(1, conn.fieldName1)
		ps.setObject(2, conn.field2)
		ps.setInteger(3, conn.field3)
		</pre>
	 * Once setPreparedInsertStatement is called, that PreparedStatement will be used for every putEntry() from then on,
	 * effectively ignoring all values in the <tt>conn</tt> Entry.
	 * Calling the method with null as parameter will reset the behavior to using the GUI defined parameters.
	 * @param preparedSql The prepared statement (string) to use.
	 * @return the PreparedStatement. null is returned if a PreparedStatement could not be created.
	 * @throws SQLException if the PreparedStatement could not be created
	 * @since 7.1
	 */
	public PreparedStatement setPreparedInsertStatement(String preparedSql) throws Exception {
		return setPrepared(INSERT, preparedSql);
	}

	/**
	 * Sets a prepared statement for future calls to deleteEntry().
	 * This method must be called after the connector has been initialized.
	 * As an example of how to use this, this code could be put in the Before Delete Hook:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedDeleteStatement("DELETE from tableName where fieldName1 = ? and field2 = ?")
		ps.setTime(1, conn.fieldName1)
		ps.setObject(2, conn.field2)
		</pre>
	 * Here is an example showing how to delete several Entries:
	 * <pre>
	 	ps = thisConnector.connector.setPreparedDeleteStatement("DELETE from tableName where fieldName1 = ? and field2 = ?")
	 	while (...) {
			ps.setTime(1, ...)
			ps.setObject(2, ...)
			thisConnector.connector.deleteEntry(null); //No need to provide an Entry when using prepared statement
		}
		thisConnector.connector.setPreparedDeleteStatement(null)
		</pre>
	 *
	 * Once setPreparedDeleteStatement has been called, that PreparedStatement will be used for every deleteEntry() from then on,
	 * effectively overriding any Link Criteria.
	 * Calling the method with null as parameter will reset the behavior to using the GUI defined parameters.
	 * @param preparedSql The prepared statement (string) to use.
	 * @return the PreparedStatement. null is returned if a PreparedStatement could not be created.
	 * @throws SQLException if the PreparedStatement could not be created
	 * @since 7.1
	 */
	public PreparedStatement setPreparedDeleteStatement(String preparedSql) throws Exception {
		return setPrepared(DELETE, preparedSql);
	}
	/**
	 *  Class for caching PreparedStatements
	 *
	 */
	private final class PSCache {
		/**
		 * The PreparedStatement (type == 0, or type == 1)
		 */
		PreparedStatement ps;
		/**
		 * A PreparedSql instead of the PreparedStatement (type == 2)
		 */
		PreparedSql psql;
		/**
		 * The String that was used to construct the PreparedStatement
		 */
		String sql;

		/**
		 * The String that the previous String was constructed from (type == 1)
		 */
		String sqlBeforeParsing;

		/**
		 * The string is parsed into a ParameterSubstitution.
		 */
		ParameterSubstitution parameterSubstitution;

		/**
		 * The metadata used when inserting the arguments. (type == 1)
		 */
		Vector<Integer> argType;
		/**
		 * The type of this PreparedStatement.
		 * 0: Created by the user using the API, e.g setPreparedSelectStatement
		 * 1: From the configuration of the connector
		 * 2: Created on the fly using using the conn object
		 */
		int type;

		/**
		 * Constructor taking a SQL String and the type.
		 * @param sql The SQL string
		 * @param type The type of this PreparedStatement
		 * @throws Exception if the SQL String could not be parsed
		 */
		public PSCache(String sql, int type) throws Exception{
			this.sql = sql;
			this.type = type;
			if (type == 0)
				ps = con.prepareStatement(sql);
			else
				psql = new PreparedSql(sql);
			sqlBeforeParsing = sql;
		}

		/**
		 * Constructor taking a SQL String and a ParameterSubstitution.
		 * The type will be 1.
		 * @param sqlBeforeParsing The SQL String before substitution.
		 * @param subst The ParameterSubstitution used to construct the SQL String.
		 * This parameter also has knowledge of which expressions to insert into the PreparedStatement.
		 */
		public PSCache(String sqlBeforeParsing, ParameterSubstitution subst) {
			this.sql = null;
			this.sqlBeforeParsing = sqlBeforeParsing;
			this.parameterSubstitution = subst;
			this.ps = null;

			type = 1;
		}

		/**
		 * Sets the SQL String, and constructs a new PreparedStatement.
		 * @param sql The SQL String used for the PreparedStatement.
		 * @throws SQLException If the SQL statement could not be parsed.
		 */
		public void setSql(String sql) throws SQLException {
			if (ps != null) {
				ps.clearWarnings();
				ps.close();
			}

			ps = con.prepareStatement(sql);
			this.sql = sql;
			argType = null;
			try {
				ParameterMetaData pmd = ps.getParameterMetaData();
				argType = new Vector<Integer>();
				for (int index = 1; index <= pmd.getParameterCount(); index ++) {
					argType.add(pmd.getParameterType(index));
				}
			} catch (SQLException e) {
				if (getLog() != null)
					getLog().logwarn(sResHash.getString(
						"unable.to.get.parameter.metadata", e.toString()));
			}
		}

		/**
		 * Sets the values in the PreparedStatement using the map
		 * @param map A Map that can be used for setting values
		 * @throws Exception If some of the values cannot be converted.
		 */
		public void setValues(Map<String,Object> map) throws Exception {
			if (type != 1)
				return;
			Object[] args = parameterSubstitution.getPreparedArgList(map);
			if (argType == null) {
				// No MetaData, just use setObjet
				for (int i = 0; i < args.length; i++)
					ps.setObject(i+1, args[i]);
			} else {
				if (args.length != argType.size())
					throw new Exception(sResHash.getString(
						"not.enough.prepared.parameters", sqlBeforeParsing));

				for (int i = 0; i < args.length; i++) {
					String name = parameterSubstitution.getPreparedArgName(i);
					setPreparedValue(ps, args[i], i + 1, argType.get(i), 0, false, name);
				}
			}
		}
	}

	/**
	 * Used for constructing a user defined PreparedStatement.
	 * @param name The name of the PreparedStatement in the cache.
	 * @param preparedSql The SQL String provided by the user.
	 * @return The PreparedStatement
	 * @throws Exception If the SQL String could not be parsed.
	 */
	private PreparedStatement setPrepared(String name, String preparedSql) throws Exception {
		PSCache psc = psCache.remove(name);
		if (psc != null) {
			psc.ps.clearWarnings();
			psc.ps.close();
		}
		if (preparedSql == null)
			return null;

		psc = new PSCache(preparedSql, 0);
		psCache.put(name, psc);
		return psc.ps;

	}

	/**
	 * Returns the named PreparedStatement from the cache, or constructs a new one.
	 * Also sets the values in the PreparedStatement if type == 1.
	 * @param name The cache name for the PreparedStatement
	 * @param search SearchCriteria used to set the values in the PreparedStatement.
	 * @param conn Entry used to set the values in the PreparedStatement
	 * @param pad True if Strings should be padded.
	 * @return A PSCache with the PreparedStatement or null if not found or PreparedStatement should not be used.
	 * @throws Exception If the values could not be converted.
	 */
	private PSCache getPrepared(String name, SearchCriteria search, Entry conn, boolean pad) throws Exception {
		PSCache psc = psCache.get(name);
		if (psc != null && psc.type != 1) {
			preparedString = psc.sql;
			return psc;
		}

		if (!customPreparedStatements)
			return null;

		String sql = getParam(name);
		if (sql == null || (sql = sql.trim()).length() == 0)
			return null;

		if ( psc == null || !sql.equals(psc.sqlBeforeParsing)) {
			if (psc != null && psc.ps != null)
				psc.ps.close();
			psc = new PSCache(sql, new ParameterSubstitution(sql, 1));
			psCache.put(name, psc);
		}

		Map<String, Object> map = getMap(conn, search, pad);

		if (enableParamSubstitute) {
			sql = psc.parameterSubstitution.substitute(map);
		}

		preparedString = sql;

		if (! sql.equals(psc.sql))
			psc.setSql(sql);

		psc.setValues(map);

		return psc;
	}

	/**
	 * Gets a PreparedSQL from the cache.
	 * @param name The name of the PreparedSQL
	 * @param sql The SQL String to use
	 * @return The PreparedSQL.
	 * @throws Exception if The SQL String cannot be parsed.
	 */
	private PreparedSql getPSql(String name, String sql) throws Exception {
		preparedString = sql;
		PSCache psc = psCache.get(name);
		if (psc == null || psc.type != 2 || ! sql.equals(psc.sql)) {
			if (psc != null && psc.psql != null)
				psc.psql.close();
			psc = new PSCache(sql, 2);
			psCache.put(name, psc);
		} else {
			psc.psql.reset();
		}
		return psc.psql;
	}

	/**
	 * Checks if the table exists in the database and attempts to create it if it doesn't exist.
	 */
	private void createDefaultTable() throws Exception {
		// -- if we have meta data then the table exists

		try {
			if(querySchema(getParam("jdbcTable")) != null) {
				return;
			}
		} catch (Exception e) {
			debug(e.getLocalizedMessage());
			querySchemaCalled = false;
		}

		// -- only create for output modes
		ConnectorConfig cc = (ConnectorConfig)getConfiguration();
		if(!ConnectorConfig.ADDONLY_MODE.equals(cc.getMode()) && !ConnectorConfig.UPDATE_MODE.equals(cc.getMode()))
			return;

		List<String> columns = cc.getAttributeMap(false).getAttributeNames();
		if(columns.size() == 0)
			columns = cc.getSchema(false).getItemNames();

		if(columns.size() == 0)
			return;

		StringBuffer sql92 = new StringBuffer();
		sql92.append("CREATE TABLE " + getParam("jdbcTable") + " (");

		for(int i = 0; i < columns.size(); i++) {

			String str = columns.get(i);
			if(i > 0)
				sql92.append(", ");

			// -- get schema item and set default type
			SchemaItemConfig sic = cc.getSchema(false).getItem(str);
			String type = "VARCHAR(255)";
			if(sic != null) {
				String ext = sic.getExternalSyntax();
				String cls = sic.getJavaClass();
				if(ext != null && ext.length() > 0) {
					// -- use external syntax
					type = sic.getExternalSyntax();
				} else if(cls != null) {
					// -- map java class
					if(cls.equalsIgnoreCase("string") || cls.equalsIgnoreCase("java.lang.string"))
						SystemFunctions.doNothing();
					else if(cls.equalsIgnoreCase("integer") || cls.equalsIgnoreCase("java.lang.integer"))
						type = "INTEGER";
					else if(cls.equalsIgnoreCase("double") || cls.equalsIgnoreCase("java.lang.double"))
						type = "DOUBLE";
					else if(cls.equalsIgnoreCase("date") || cls.equalsIgnoreCase("java.lang.date"))
						type = "TIMESTAMP";
				}
			}
			sql92.append(str + " " + type);
		}
		sql92.append(")");

		Statement st = null;
		try {
			lastSQL = sql92.toString();
			logmsg(lastSQL);
			st = con.createStatement();
			st.execute(lastSQL);
		} finally {
			st.close();
		}
	}

	/**
	 * Returns the last String used to construct a PreparedStatement.
	 * The Connector will usually try to use a PreparedStatement when modifying
	 * or retrieving information, except in Iterator Mode,
	 * but Connector parameters may change the behavior.
	 * As an example of how to use this, this code could be put in the Default Success Hook:
	 * <pre>
	    ps = thisConnector.connector.getPreparedString();
	    task.logmsg("The Prepared Statement was :\n" + ps);
	   </pre>
	 * @return the last String used to construct a PreparedStatement.
	 */
	public String getPreparedString() {
		return preparedString;
	}
	/**
	 * Returns true if the DB is a isMSSqlServerDB database
	 * @return
	 */
	public boolean isMSSqlServerDB() {
		return isMSSQLServerDB;
	}

	public void extractExceptionInformation(Entry error) {
		if (lastSQL != null)
			error.setAttribute("lastSQL", lastSQL);
		if (preparedString != null)
			error.setAttribute("lastPreparedString", preparedString);
	}
	
	/**
	 * Returns the last SQL String used.
	 * The Connector will usually try to use a PreparedStatement when modifying
	 * or retrieving information.
	 * If that fails, a SQL String is usually constructed,
	 * and this method returns that String.
	 * @return the last SQLString used.
	 * @since 7.2
	 */
	public String getLastSqlString() {
		return lastSQL;
	}

	/**
	 * Returns the Schema Separator, normally a single dot.
	 * @return the schemaSeparator
	 * 
	 */
	public String getSchemaSeparator() {
		return schemaSeparator;
	}

	/**
	 * Sets the Schema Separator.
	 * E.g. on Iseries you may want to set this to a slash.
	 * 
	 * @param schemaSeparator the schemaSeparator to set
	 */
	public void setSchemaSeparator(String schemaSeparator) {
		this.schemaSeparator = schemaSeparator;
	}
	/**
	 * Get a value from the nameToType or nameSize Maps
	 * @param map
	 * @param name
	 * @return
	 */
	private Integer getMapValue(Map<String, Integer> map, String name) {
		String lookupName = name.toUpperCase(Locale.ENGLISH);
		Integer val = map.get(lookupName);
		if (val != null)
			return val;

		// try removing quoting around the attribute name
		lookupName = removeQuoting(lookupName);
		val = map.get(lookupName);
		if (val != null)
			return val;

		//Try splitting at schemaSeparator
		int i = lookupName.indexOf(schemaSeparator);
		if (i > 0)
			val = map.get(lookupName.substring(i+1));

		return val;
	}
	
	/**
	 * Remove quoting
	 *
	 * @param name
	 *            String to convert
	 * @return The converted value
	 */
	private String removeQuoting(String name) {
		int n = name.length();
		if (n <= 2)
			return name;
		char c = name.charAt(0);
		if ((c == '\'' || c == '"') && name.charAt(n - 1) == c)
			return name.substring(1, n - 1);
		return name;
	}

}
