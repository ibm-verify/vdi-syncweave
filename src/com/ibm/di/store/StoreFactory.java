/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import org.apache.derby.drda.NetworkServerControl;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.RSInterface;
import com.ibm.di.server.ResourceHash;

public class StoreFactory {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static Properties derbyProp = new Properties();
	private final static String DERBY_PROPS = "etc/derby.properties";

	public static final String JDBC_DRIVER_DERBY_NET = "org.apache.derby.jdbc.ClientDriver";
	public static final String JDBC_DRIVER_DERBY_EMB = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String JDBC_DRIVER_DB2 = "com.ibm.db2.jcc.DB2Driver";
	public static final String JDBC_DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
	public static final String JDBC_DRIVER_MSSQL = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String JDBC_DRIVER_SOLID = "solid.jdbc.SolidDriver";

	static {
		File derby = new File(DERBY_PROPS);
		String inDir = System.getProperty("com.ibm.di.installdir");
		if (! derby.exists() && inDir != null) {
			derby = new File(inDir + "/" + DERBY_PROPS);
		}
		if (derby.exists()) {
			try {
				FileInputStream fis = new FileInputStream(derby);
				try {
					derbyProp.load(fis);
					if (derbyProp.get("derby.system.home") == null && inDir != null)
						derbyProp.put("derby.system.home", inDir);
				} finally {
					fis.close();
				}
			} catch (Exception fnf) {
				System.err.println(derby.getAbsolutePath());
				fnf.printStackTrace();
			}
		}

		Enumeration<?> propNames = derbyProp.propertyNames();
		while (propNames.hasMoreElements()) {
			String key = (String) propNames.nextElement();
			if (System.getProperty(key) == null)
				System.setProperty(key, derbyProp.getProperty(key));
		}
	}

	public static final String DEFAULT_PROPERTYSTORE = "DEFAULT";

	public static final String REGEX = "VARCHAR_LENGTH|varchar_length";

	private static final String DERBY_NETWORK_DRIVER = "org.apache.derby.jdbc.ClientDriver";

	private static final String PROP_DERBY_USER = "derby.user.";

	private static final String DERBY_USER_AUTHEN_PROVIDER = "BUILTIN";

	public static final String VARCHAR_LENGTH = getVarcharLength();

	public static final String SOLUTION_DIR = "$soldir$";
	
	private static Hashtable<String, PropertyStore> propertyStores = new Hashtable<String, PropertyStore>();

	private static Vector<File> openDB = new Vector<File>();

	private static Hashtable<String, String> retainedDB = new Hashtable<String, String>();

	private static Log log = new Log("miserver");

	private static transient ResourceHash res = ResourceHash.getHash("miserver");

	private static boolean hasMigrated;

	/**
	 * Returns the default property store.
	 */
	public static PropertyStore getDefaultPropertyStore() throws Exception {
		return getPropertyStore(DEFAULT_PROPERTYSTORE);
	}

	/**
	 * Returns the PropertyStore identified by name. Only one instance of a
	 * given name is present at one time.
	 *
	 * @param name
	 *            The property store name
	 * @return The property store object associated with name
	 */
	public static PropertyStore getPropertyStore(String name) throws Exception {

		// -- If config instance overrides we insert a prefix
		String prefixedName = getInstanceOverridePrefix() + name;
		PropertyStore ps = propertyStores.get(prefixedName);
		if (ps == null) {
			ps = new PropertyStore(name);
			propertyStores.put(prefixedName, ps);
		}

		return ps;
	}

	/**
	 * Returns the DeltaStore with <i>identifier </i>
	 *
	 * @param identifier
	 *            The delta table identity/tablename
	 * @param isRestarting
	 *            True if the delta is opened in restart mode
	 * @return The DeltaStore object associated with identifier
	 */
	public static DeltaStore getDeltaStore(String identifier,
			boolean isRestarting) throws Exception {
		return DeltaSysTable.getDeltaStore(identifier, isRestarting);
	}

	/**
	 * Returns the SystemStore JDBC URL.
	 */
	public static String getSystemDatabaseURL() {
		String db = getDefaultDatabase();
		String prefix = getJdbcURL();
		if (prefix == null)
			return db;
		if (db == null)
			return prefix;
		if (db.startsWith(prefix))
			return db;
		return prefix + db;
	}

	/**
	 * Returns the default system database name
	 */
	public static String getSystemDatabase() {
		return getDefaultDatabase();
	}

	/**
	 * Returns a connection object to the default database. The connection
	 * object is in auto-commit mode and uses the default username/password.
	 *
	 * @return A connection object to the named database.
	 */
	public static Connection getConnection() throws Exception {
		return getConnection(getDefaultDatabase());
	}

	/**
	 * Returns a connection object to the default database using the default
	 * username/password.
	 *
	 * @param autoCommit
	 *            The auto-commit flag
	 *
	 * @return A connection object to the named database.
	 */
	public static Connection getConnection(boolean autoCommit) throws Exception {
		return getConnection(getDefaultDatabase(), autoCommit);
	}

	/**
	 * Returns a connection object to the named database with AutoCommit set to
	 * TRUE using the default username/password.
	 *
	 * @param database
	 *            The database name
	 * @return A connection object to the named database.
	 */
	public static Connection getConnection(String database) throws Exception {
		return getConnection(database, true);
	}

	/**
	 * Returns a connection object to the named database with the default
	 * username/password.
	 *
	 * @param database
	 *            The database name
	 * @param autoCommit
	 *            The auto-commit flag
	 *
	 * @return A connection object to the named database.
	 */
	public static Connection getConnection(String database, boolean autoCommit)
			throws Exception {
		return getConnection(database, autoCommit, getJdbcUser(),
				getJdbcPassword(), null);
	}

	/**
	 * Returns a connection to the named database.
	 *
	 * @param database
	 *            If the value starts with "jdbc:" then it is used asis to
	 *            obtain a connection object. Otherwise, this method prepends
	 *            the JDBC_URL setting from the global.properties file and
	 *            appends ";create=true". When prepending JDBC_URL it is assumed
	 *            that Derby is used.
	 * @param autoCommit
	 *            The auto commit flag set on the connection object
	 * @param user
	 *            The username
	 * @param password
	 *            The password
	 * @param info
	 *            If specified, this method will use the database asis (e.g. no
	 *            prepend/append) combined with this parameter to obtain a
	 *            connection object. The info object should contain fields for
	 *            user and pass and other related parameters to the driver.
	 */
	public static Connection getConnection(String database, boolean autoCommit,
			String user, String password, Properties info) throws Exception {

		if (password != null && password.startsWith(RS.PROTECT_VAL_PREFIX)) {
			// decrypt the encrypted passwd with the server key.
			try {
				password = password.substring(6);
				byte[] decrypt = UserFunctions.base64Decode(password);
				byte[] decryptVal = CryptoUtils.decryptWithServerKey(decrypt);
				password = new String(decryptVal);

			} catch (Exception e) {
				// we failed to decrypt the passwd correctly. try setting the
				// passwd to the defaukt n/w passwd APP.
				// NOTE: CS network mode requires the passwd to be APP to access
				// its internal tables.
				password = "APP";
				log.debug("store.fact.error.decrypt.password", e.toString());
			}
		}

		// -- Set default derby user/pass values
		if ((user == null || user.length() == 0) && isDerbyDriver(getJdbcDriver())) {
			user = "APP";
		}
		if ((password == null || password.length() == 0) && isDerbyDriver(getJdbcDriver())) {
			password = "APP";
		}

		if (database == null || database.length() == 0)
			database = "null";

		if (getJdbcDriver() == null) {
			log.error("sysstore.jdbcdriver.null");
			throw new Exception(res.getString("sysstore.jdbcdriver.null"));
		}

		setDerbyUserPassword(user, password);
		if (DERBY_NETWORK_DRIVER.equals(getJdbcDriver())) {
			startDerbyServer(getDbHost(), getDbPort(), Boolean.getBoolean("com.ibm.di.store.sysibm"));
		}

		// Load driver
		Class.forName(getJdbcDriver());

		// Get connection and set autoCommit flag
		Connection conn;
		try {
			if (database.startsWith("jdbc:"))
				conn = DriverManager.getConnection(database, user, password);
			else if (info == null)
				conn = DriverManager.getConnection(getJdbcURL() + database
						+ ";create=true", user, password);
			else
				conn = DriverManager.getConnection(database, info);
		} catch (SQLException err) {
			if (err.getNextException() != null)
				err = err.getNextException();
			throw err;
		}

		migrateTables(conn);

		conn.setAutoCommit(autoCommit);

		File tmpFile = new File(database);

		if (!openDB.contains(tmpFile)) {
			openDB.add(tmpFile);
		}

		return conn;
	}

	public static void setDerbyUserPassword(String user, String password) {
		if (user == null || user.length()==0)
			user = "APP";
		if (password == null)
			password = "APP";
		// Set the user name and password in Derby's BUILTIN repository.
		if (DERBY_USER_AUTHEN_PROVIDER.equalsIgnoreCase(System
				.getProperty("derby.authentication.provider"))) {
			System.setProperty(PROP_DERBY_USER + user, password);
		}
	}

	/**
	 * Drops a table in the database associated with connection.
	 *
	 * @param connection
	 *            The connection object obtained by getConnection()
	 * @param table
	 *            The table to drop
	 */
	public static boolean dropTable(Connection connection, String table) {
		boolean isDropped = false;
		try {
			Statement st = connection.createStatement();
			try {
				String qry = "DROP TABLE " + table;
				isDropped = st.execute(qry);
			} finally {
				st.close();
			}
			connection.commit();
		} catch (Exception error) {
			error.printStackTrace();
			return isDropped;
		}
		return isDropped;
	}

	/**
	 * Verifies that a table is accessible in the database.
	 *
	 * @param connection
	 *            The connection object obtained by getConnection(). If NULL, a
	 *            connection to the default table is obtained.
	 * @param table
	 *            The table name to verify
	 * @param sql
	 *            A vector of SQL statements to create the table if it does not
	 *            exist
	 */
	public static boolean verifyTable(Connection connection, String table,
			Vector<String> sql) throws Exception {

		Connection conn = (connection == null ? getConnection() : connection);

		Exception err = null;
		boolean created = false;

		Statement st = conn.createStatement();
		try {
			if (!tableExists(conn, table)) {
				log.info("store.factory.table.not.exist", table);
				try {
					for (int i = 0; i < sql.size(); i++) {
						log.info("store.factory.sql.to.execute", sql.get(i));
						st.execute(sql.get(i));
					}
					if (!conn.getAutoCommit())
						conn.commit();
					log.info("store.factory.table.created", table);
					created = true;
				} catch (Exception error) {
					log.error("store.factory.table.create.error", new Object[] {
							sql, error });
					err = error;
				}
			}
		} finally {
			st.close();
		}

		// Close if we created our own
		if (connection == null)
			conn.close();

		if (err != null)
			throw err;

		return created;

	}

	/**
	 * Checks if a table is accessible in the database.
	 *
	 * @param connection
	 *            The connection object obtained by getConnection(). If NULL, a
	 *            connection to the default table is obtained.
	 * @param table
	 *            The table name to verify
	 */
	public static boolean tableExists(Connection connection, String table)
			throws Exception {

		Connection conn = (connection == null ? getConnection() : connection);

		// Strip quotes from table name
		if (table.startsWith("\"") && table.endsWith("\""))
			table = table.substring(1, table.length() - 1);

		DatabaseMetaData md = conn.getMetaData();
		
		ResultSet rs = md.getTables(null, null,	table, null);

		boolean exists = rs.next();

		rs.close();

		// Try using uppercase
		if (!exists) {
			
			rs = md.getTables(null, null, table.toUpperCase(), null);
			exists = rs.next();

			rs.close();			
		}

		// Close if we created our own
		if (connection == null)
			conn.close();

		return exists;
	}

	public static Vector<String> getTables(Connection connection, String table)
			throws Exception {
		String user = getJdbcUser();
		if (user != null)
			user = user.toUpperCase(Locale.ENGLISH);
		else if (isDerbyDriver(getJdbcDriver()))
			user = "APP";

		// Check for MS SQL Server
		boolean isMSSQLServerDB = isMSSQLDriver(getJdbcDriver());

		ResultSet rs = null;
		if (!isMSSQLServerDB) {
			rs = connection.getMetaData().getTables(null, user,
					table.toUpperCase(Locale.ENGLISH), null);
		} else {
			rs = connection.getMetaData().getTables(null, null,
					table.toUpperCase(Locale.ENGLISH), null);
		}
		Vector<String> v = new Vector<String>();

		while (rs.next()) {
			v.add(rs.getString("TABLE_NAME"));
		}

		rs.close();
		return v;
	}

	/**
	 * Shuts down all open databases.
	 */
	public static void shutdown() {

		// Only shutdown if Derby is embedded in our JavaVM
		if (!"org.apache.derby.jdbc.EmbeddedDriver".equals(getJdbcDriver())) {
			log.debug("store.fact.shut.down.not.perform");
			return;
		}

		// -- Get user/pass and set default derby values
		String user = getJdbcUser();
		String password = getJdbcPassword();

		if (user == null || user.length() == 0) {
			user = "APP";
		}
		if (password == null || password.length() == 0) {
			password = "APP";
		}

		Connection localConn = null;
		for (int i = openDB.size() - 1; i >= 0; i--) {
			String name = openDB.get(i).toString();
			if (name.startsWith("jdbc:")) {
				log.info("store.fact.shut.down.ignore", name);
			} else if (retainedDB.containsValue(name)) {
				log.info("store.fact.db.retain", name);
			} else {
				log.info("store.fact.shut.down.db", name);
				try {
					localConn = DriverManager.getConnection(getJdbcURL() + name
							+ ";shutdown=true", user, password);
				} catch (Exception bogus) {
					if (!bogus.getMessage().equals(
							"Database '" + name + "' shutdown."))
						bogus.printStackTrace();
				}
				openDB.remove(i);
			}
		}
		try {
			if (localConn != null)
				localConn.close();
		} catch (SQLException e) {
			// ignore the exception since we are closing the connection to the
			// database.
		}
		// Close property stores
		for (Iterator<PropertyStore> i = propertyStores.values().iterator(); i
				.hasNext();) {
			PropertyStore ps = i.next();
			try {
				ps.closeStore();
			} catch (Exception ignore) {
			}
		}
		propertyStores.clear();
	}

	/**
	 * Drops a table in the default database.
	 *
	 * @param tableName
	 *            The name of the table to drop.
	 */
	public static Exception dropTable(String tableName) {
		try {
			Connection conn = getConnection();
			Statement st = conn.createStatement();
			try {
				st.execute("DROP TABLE \"" + tableName + "\"");
			} catch (Exception e) {
				st.execute("DROP TABLE " + tableName);
			} finally {
				st.close();
			}
			if (!conn.getAutoCommit())
				conn.commit();
			conn.close();
			return null;
		} catch (Exception error) {
			return error;
		}
	}

	/**
	 * Serializes an object to a byte array.
	 *
	 * @param obj
	 *            The object to serialize
	 * @return The byte array containing the serialized object
	 */
	public static byte[] serializeObject(Object obj) throws Exception {
		if (obj == null)
			return null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		return bos.toByteArray();
	}

	/**
	 * Deserializes a byte array into a Java object.
	 *
	 * @param o
	 *            The byte array with the serialized Java object
	 * @return The resurrected java object
	 */
	public static Object deserializeObject(Object o) throws Exception {
		byte[] bytes = getObjectBytes(o);
		Object result = deserializeObjectFromBytes(bytes); 
		return result;
	}
	
	static byte[] getObjectBytes(Object o) throws Exception {
		
		byte[] bytes = null;

		if (o instanceof byte[]) {
			bytes = (byte[]) o;
		} else if (o instanceof Blob) {
			Blob b = (Blob) o;
			bytes = b.getBytes(1L, (int) b.length());
		} else if (o instanceof ByteArrayInputStream) {
			// It is for MS SQL binary data type(d5676).
			ByteArrayInputStream bais = (ByteArrayInputStream) o;
			bytes = new byte[bais.available()];
			bais.read(bytes);
		}

		return bytes;
	}
	
	public static Object deserializeObjectFromBytes(byte[] bytes) throws Exception {
		if (bytes == null)
			return null;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	public static void retainDB(String id, String db) {
		retainedDB.put(id, db);
	}

	public static void releaseDB(String id) {
		retainedDB.remove(id);
	}

	public static void logmsg(String msg) {
		log.info(msg);
	}

	public static void debugmsg(String msg) {
		log.debug(msg);
	}

	private static void migrateTables(Connection conn) throws Exception {
		if (hasMigrated)
			return;
		hasMigrated = true;

		conn.setAutoCommit(false);

		Vector<String> tableNames = getTables(conn, "%");

		if (tableNames.contains("IDI_SYSPROPS")) {
			try {
				Statement st = conn.createStatement();
				String str = "ALTER TABLE IDI_SYSPROPS RENAME TO "
						+ PropertyStore.TABLE_PREFIX + DEFAULT_PROPERTYSTORE;
				st.execute(str);
				st.close();
				log.info("store.fact.rename.table");
				if (!conn.getAutoCommit())
					conn.commit();
			} catch (Exception err) {
				log.info("error.in.migratetable.syspros", err.toString());
			}
		}

		if (!tableNames.contains(DeltaSysTable.SYSTABLE))
			return;

		Statement st = conn.createStatement();
		ResultSet rs;
		try {
			rs = st.executeQuery("SELECT ID FROM " + DeltaSysTable.SYSTABLE);
		} catch (Exception err) {
			log.info("error.while.migrate.delta.tables", err.toString());
			st.close();
			return;
		}

		boolean change = false;
		while (rs.next()) {
			String table = rs.getString("ID");
			if (!tableNames.contains(DeltaStore.TABLE_PREFIX
					+ table.toUpperCase(Locale.ENGLISH))) {

				if (!tableNames.contains(table.toUpperCase(Locale.ENGLISH))
						&& !tableNames.contains(table))
					continue;

				if (!change) {
					log.info("begin.migrate.delta.tables");
					change = true;
				}

				try {
					String str = "ALTER TABLE " + table + " RENAME TO "
							+ DeltaStore.TABLE_PREFIX + table;
					log.info("MISERVER.STOREFACTORY.MINUS.MINUS.GREATER", str);
					Statement rename = conn.createStatement();
					rename.execute(str);
					rename.close();
				} catch (SQLException sqle) {
					log.debug("error.in.rename.do.no.exist", sqle.toString());
				}
			}
		}

		if (change) {
			log.info("end.migrate.delta.tables");
			if (!conn.getAutoCommit())
				conn.commit();
		}

		rs.close();
		st.close();
	}

	/**
	 * Start the networked Derby Server. This method does not use the API, but
	 * spawns a new process, to let the database stay around after exiting the
	 * server/CE.
	 *
	 * @param hostname
	 * @param portNo
	 * @param sysIBM
	 * @throws Exception
	 */
	public static void startDerbyServer(String hostname, String portNo,
			boolean sysIBM) throws Exception {

		// create a separate process for Derby network server.
		// a horrible horrible hack

		if (hostname == null || hostname.trim().length() <= 0) {
			// Read the hostname value from global.properties
			hostname = System.getProperty("com.ibm.di.store.hostname");
			if (hostname == null || hostname.trim().length() <= 0) {
				hostname = "localhost"; // Set to default localhost if property
				// not found or not specified.
			}
		}

		int port;
		if (portNo == null)
			portNo = System.getProperty("com.ibm.di.store.port");
		try {
			port = Integer.parseInt(portNo);
		} catch (Exception e) {
			port = 1527;
		}

		NetworkServerControl control = null;
		try {
			control = new NetworkServerControl(InetAddress.getByName(hostname),
					port);
		} catch (Exception e) {
		}

		if (control != null) {
			try {
				control.ping(); // ping returns if database running, throws
				// Exception if not running
				return; // database already running, no need to do anything
			} catch (Exception e) {
				// database not running, proceed to start it with a bad hack
			}
		}

		setDerbyUserPassword(getJdbcUser(), getJdbcPassword());

		String os = System.getProperty("os.name");
		if (os == null)
			os = "";

		List<String> args = new ArrayList<String>(); // args for exec.
		String classpath; // System dependent
		
		if (os.indexOf("Windows") >= 0) {
			String cp = System.getProperty("IDILoader.jars");
			while (cp.startsWith("/"))
				cp = cp.substring(1);
			classpath = cp + "\\jars\\3rdparty\\IBM\\derbynet.jar;"
				+ cp + "\\jars\\3rdparty\\IBM\\db2jcc.jar;"
				+ cp + "\\jars\\3rdparty\\IBM\\derbytools.jar;"
				+ cp + "\\jars\\3rdparty\\IBM\\derbyshared.jar;"
				+ cp + "\\jars\\3rdparty\\IBM\\derby.jar";

			args.add(System.getProperty("java.home") + "\\bin\\java");
		} else {
			String dir = System.getProperty("com.ibm.di.installdir");
			classpath = dir + "/jars/3rdparty/IBM/derbynet.jar:" + dir
			+ "/jars/3rdparty/IBM/db2jcc.jar:" + dir
			+ "/jars/3rdparty/IBM/derbytools.jar:" + dir
			+ "/jars/3rdparty/IBM/derbyshared.jar:" + dir
			+ "/jars/3rdparty/IBM/derby.jar";
			
			args.add("java");
		}

		args.add("-classpath");
		args.add(classpath);

		Enumeration<?> propNames = derbyProp.propertyNames();
		while (propNames.hasMoreElements()) {
			String key = (String) propNames.nextElement();
			args.add("-D" + key + "=" + System.getProperty(key));
		}

		args.add("org.apache.derby.drda.NetworkServerControl");
		args.add("start");
		args.add("-h");
		args.add(hostname);
		if (port > 0) {
			args.add("-p");
			args.add(portNo);
		}
		if (sysIBM)
			args.add("-ld");

		Runtime.getRuntime().exec(args.toArray(new String[args.size()]));

		// Wait a little while for server to start up
		if (control == null)
			return;
		for (int i = 0; i < 15; i++) {
			try {
				Thread.sleep((i + 1) * 100);
				control.ping();
				return;
			} catch (Exception e) {
			}
		}
	}

	public static void stopDerbyServer(String hostname, int port) throws Exception {
		if (hostname == null || hostname.trim().length() <= 0) {
			// Read the hostname value from global.properties
			hostname = System.getProperty("com.ibm.di.store.hostname");
			if (hostname == null || hostname.trim().length() <= 0) {
				hostname = "localhost"; // Set to default localhost if property
				// not found or not specified.
			}
		}
		if (port < 0)
			port = 0;

		NetworkServerControl control;

		String user = getJdbcUser();
		if (user != null && user.length() > 0)
			control = new NetworkServerControl(InetAddress.getByName(hostname), port, user, getJdbcPassword());
		else
			control = new NetworkServerControl(InetAddress.getByName(hostname), port);

		control.shutdown();
	}

	/**
	 * Determines if the passed driver is a Derby Network Driver.
	 *
	 * @return true if the passed driver is a Derby network driver otherwise
	 *         false.
	 */
	public static boolean isDerbyNetworkDriver(String driver) {
		if (driver == null) {
			return false;
		}
		driver = driver.trim();

		// Derby Network Driver (recommended)
		if (driver.equalsIgnoreCase("org.apache.derby.jdbc.ClientDriver")) {
			return true;
		}

		// DB2 Derby network driver (not recommended)
		if (driver.equalsIgnoreCase("com.ibm.db2.jcc.DB2Driver")) {
			return true;
		}

		return false;
	}

	public static boolean isDerbyDriver(String driver) {
		return driver != null && driver.startsWith("org.apache.derby.");
	}

	public static boolean isMSSQLDriver(String driver) {
		return driver != null &&
				(driver.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver")
				 || driver.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")
				 || driver.equals("com.jnetdirect.jsql.JSQLDriver"));
	}

	public static String getDefaultDatabase() {
		String s = getProperty("com.ibm.di.store.database");
		return s != null ? s.replace(SOLUTION_DIR, getProperty("user.dir")) : null;
	}

	public static String getJdbcDriver() {
		return getProperty("com.ibm.di.store.jdbc.driver");
	}

	public static String getJdbcURL() {
		return getProperty("com.ibm.di.store.jdbc.urlprefix");
	}

	public static String getJdbcUser() {
		return getProperty("com.ibm.di.store.jdbc.user");
	}

	public static String getJdbcPassword() {
		return getProperty("com.ibm.di.store.jdbc.password");
	}

	public static String getVarcharLength() {
		return getProperty("com.ibm.di.store.varchar.length");
	}

	public static String getDbHost() {
		String s = getProperty("com.ibm.di.store.jdbc.host");  // A name that was added in a message in 7.0
		return s != null ? s : getProperty("com.ibm.di.store.hostname");
	}

	public static String getDbPort() {
		String s = getProperty("com.ibm.di.store.jdbc.port");   // A name that was added in a message in 7.0
		return s != null ? s : getProperty("com.ibm.di.store.port");
	}

	/**
	 * This method returns the proper 'CREATE TABLE' statement for the System
	 * Store Connector based on the specified JDBC driver name.
	 *
	 * @param driver
	 *            java class name of the JDBC driver
	 * @return the proper 'CREATE TABLE' statement for the used database;
	 *         <code>null</code> if the driver is not recognized.
	 */
	public static String getSysStoreCreateStmtByDriver(String driver) {
		String createTable = null;

		if (driver != null) {
			if (driver.equals(JDBC_DRIVER_DB2) || driver.equals(JDBC_DRIVER_ORACLE)
					|| driver.equals(JDBC_DRIVER_DERBY_NET) || driver.equals(JDBC_DRIVER_DERBY_EMB)) {
				createTable = "CREATE TABLE {0} (ID VARCHAR(" + StoreFactory.VARCHAR_LENGTH
						+ ") NOT NULL, ENTRY BLOB );\nALTER TABLE {0} ADD CONSTRAINT {0}_PRIMARY Primary Key (ID)";
			} else if (driver.equals(JDBC_DRIVER_SOLID)) {
				createTable = "CREATE TABLE {0} (ID VARCHAR(" + StoreFactory.VARCHAR_LENGTH + ") PRIMARY KEY NOT NULL, ENTRY BLOB)";
			} else if (driver.equals(JDBC_DRIVER_MSSQL)) {
				createTable = "CREATE TABLE {0} (ID VARCHAR(" + StoreFactory.VARCHAR_LENGTH
						+ ") NOT NULL, ENTRY VARBINARY(MAX) );\nALTER TABLE {0} ADD CONSTRAINT {0}_PRIMARY Primary Key (ID)";
			}
		}
		return createTable;
	}

	/**
	 * This method will look at the configuration in the caller's thread
	 * (RS.getServer) to see if it overrides the default settings from
	 * global/solution properties. If the custom system store settings are
	 * enabled the value is retrieved from the configuration, otherwise it is
	 * retrieved by System.getProperty().
	 *
	 * @param key
	 *            the property's indentifier
	 * @return the value corresponding to the specified identifier.
	 */
	public static String getProperty(String key) {

		BaseConfiguration bc = getInstanceOverrideConfig();
		String value = null;
		if (bc != null)
			value = bc.getStringParameter(key);
		if (value != null && value.length() > 0)
			return value;
		return System.getProperty(key);
	}

	/**
	 * @return the override config or null if no override is enabled
	 */
	private static BaseConfiguration getInstanceOverrideConfig() {
		RSInterface current = SystemFunctions.getServer();
		if (current == null)
			return null;
		MetamergeConfig mc = current.getMetamergeConfig();
		if (mc == null) {
			// This is most likely the default server being started (before
			// we have the config set)
			return null;
		}
		BaseConfiguration bc = null;
		try {
			ContainerConfigImpl cc = (ContainerConfigImpl) mc
			.lookup(MetamergeConfig.DEFAULT_SERVER_FOLDER
					+ "/SystemStore"); //$NON-NLS-1$
			if (cc != null && cc.size() > 0)
				bc = cc.getConfig(0);
		} catch (Exception e) {
			// No SystemStore defined
			return null;
		}
		if (bc != null && bc.getEnabled())
			return bc;

		return null;
	}

	private static String getInstanceOverridePrefix() {
		BaseConfiguration bc = getInstanceOverrideConfig();
		if (bc == null)
			return "";

		return bc.getMetamergeConfig().getSolutionInterface().getInstanceID() + "_";
	}

}
