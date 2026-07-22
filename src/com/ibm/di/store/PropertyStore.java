/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.store;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

public class PropertyStore {

	public static final String TABLE_PREFIX = "IDI_PS_";

	private static final String PROPERTIES_FILE = "miserver";

	private String table;

	private static Connection conn;

	private String unique = "{UNIQUE}";

	private String SEPARATOR = ";";

	private Vector<String> queries = new Vector<String>();

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Initializes this object with a connection to the property store table
	 * named <i>name</i>.
	 *
	 * @param identifier
	 *            The table name
	 */
	public PropertyStore(String identifier) throws Exception {

		if (identifier == null) {
			throw new Exception(sResHash.getString("must.specify.table"));
		}

		if (identifier.startsWith(TABLE_PREFIX))
			this.table = identifier;
		else
			this.table = TABLE_PREFIX + identifier;

		// Get database connection
		if (conn == null || conn.isClosed())
			conn = StoreFactory.getConnection(false);

		String createTable = StoreFactory.getProperty("com.ibm.di.store.create.property.store");

		if (createTable == null || createTable.equals("")) {
			createTable = "CREATE TABLE {0} (ID VARCHAR("
					+ StoreFactory.getVarcharLength()
					+ ") NOT NULL, ENTRY long varbinary )";
			queries.add(createTable);
		} else {
			StringTokenizer tk = new StringTokenizer(createTable, SEPARATOR);
			while (tk.hasMoreTokens()) {
				queries.add(tk.nextToken().replaceFirst(StoreFactory.REGEX,
						StoreFactory.getVarcharLength()));
			}

		}

		// Verify/Create table
		Vector<String> v = new Vector<String>();
		String time = Long.toHexString(System.currentTimeMillis());
		int uniqueIndex = -1;
		for (int i = 0; i < queries.size(); i++) {
			uniqueIndex = queries.elementAt(i).indexOf((unique));
			if (uniqueIndex != -1) {
				queries.set(i, queries.elementAt(i).replace(unique, "{1}"));
				v.add(MessageFormat.format(queries.elementAt(i),
						new Object[] { table, time }));
			} else {
				v.add(MessageFormat.format(queries.elementAt(i),
						new Object[] { table }));
			}
		}
		StoreFactory.verifyTable(conn, table, v);

		// Create prepared statements

	}

	/**
	 * Closes all resources open by this object.
	 */
	public void closeStore() throws Exception {
		if (conn != null && !conn.isClosed()) {
			conn.commit();
			conn.close();
			conn = null;
		}
	}

	/**
	 * Adds or updates a value in the property store. If an update is performed
	 * the old value is returned.
	 *
	 * @param key
	 *            The unique identifier
	 * @param obj
	 *            The value
	 * @return The old value in case of an update
	 */
	public synchronized Object setProperty(String key, Object obj) throws Exception {
		Object current = getProperty(key);
		PreparedStatement ps = null;

		if (obj == null) {
			throw new Exception(sResHash.getString("cannot.put.null.for.key",
					key));
		}

		checkConn();

		synchronized(conn) {
		if (current != null)
			ps = conn.prepareStatement("UPDATE " + table
					+ " SET ENTRY = ? WHERE ID = ?");
		else
			ps = conn.prepareStatement("INSERT INTO " + table
					+ " (ENTRY,ID) VALUES (?,?)");
		int count;
		try {
			ps.setBytes(1, StoreFactory.serializeObject(obj));
			ps.setString(2, key);
			count = ps.executeUpdate();
		} finally {
			ps.close();
		}
		if (count != 1) {
			throw new Exception(sResHash.getString("update.entry.for.key",
					new Object[] { key, Integer.valueOf(count) }));
		}

		conn.commit();
		}
		return current;
	}

	/**
	 * Update property value. Can create the property if missing. This method is
	 * supposed to be faster than {@link #setProperty(String, Object)} in cases
	 * where it is much more likely for the property to exist.
	 *
	 * @param key
	 *            The unique identifier
	 * @param obj
	 *            The value
	 * @param createIfMissing
	 *            If true and the property is missing, it will be created.
	 * @throws Exception
	 *             If the value is null or an error occurs in the underlying
	 *             store.
	 */
	public synchronized void updateProperty(String key, Object obj, boolean createIfMissing)
			throws Exception {

		if (obj == null) {
			throw new Exception(sResHash.getString("cannot.put.null.for.key", key));
		}

		checkConn();
		synchronized (conn) {
		int count;
		byte[] serValue;

		PreparedStatement updateStmt = conn.prepareStatement("UPDATE " + table + " SET ENTRY = ? WHERE ID = ?");
		try {
			serValue = StoreFactory.serializeObject(obj);
			updateStmt.setBytes(1, serValue);
			updateStmt.setString(2, key);
			count = updateStmt.executeUpdate();
		} finally {
			updateStmt.close();
		}

		if (count < 1 && createIfMissing) {
			PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO " + table + " (ENTRY,ID) VALUES (?,?)");
			try {
				insertStmt.setBytes(1, serValue);
				insertStmt.setString(2, key);
				insertStmt.executeUpdate();
			} finally {
				insertStmt.close();
			}
		} else if (count != 1) {
			throw new Exception(sResHash.getString("update.entry.for.key", new Object[] { key, Integer.valueOf(count) }));
		}
		conn.commit();
	}
	}

	/**
	 * Returns a value in the property store.
	 *
	 * @param key
	 *            The unique identifier
	 * @return Value in the store or NULL if not found.
	 */
	public synchronized Object getProperty(String key) throws Exception {
		Object v = null;

		checkConn();

		synchronized (conn) {
		PreparedStatement getEntry = conn.prepareStatement("SELECT ENTRY FROM "
				+ table + " WHERE ID = ?");
		try {
			getEntry.setString(1, key);

			ResultSet rs = getEntry.executeQuery();
			try {
				if (rs.next()) {
					v = StoreFactory.deserializeObject(rs.getObject(1));
				}
			} finally {
				conn.commit();
				rs.close();
			}
		} finally {
			getEntry.close();
		}
		}
		return v;
	}

	/**
	 * Removes a value in the property store.
	 *
	 * @param key
	 *            The unique identifier to remove.
	 *
	 * @return The old value or NULL if key were not in the table
	 */
	public synchronized Object removeProperty(String key) throws Exception {
		Object current = getProperty(key);
		if (current != null) {
			checkConn();
			synchronized (conn) {
			int count;
			PreparedStatement delEntry = conn.prepareStatement("DELETE FROM "
					+ table + " WHERE ID = ?");
			try {
				delEntry.setString(1, key);
				count = delEntry.executeUpdate();
			} finally {
				delEntry.close();
			}
			if (count != 1) {
				throw new Exception(sResHash.getString("delete.entry.key",
						new Object[] { key, Integer.valueOf(count) }));
			}

			conn.commit();
		}
		}
		return current;
	}

	/**
	 * Returns an Enumeration of the keys in the store.
	 */
	public List<String> keys() throws Exception {
		checkConn();
		List<String> list = new ArrayList<String>();
		synchronized (conn) {
		Statement st = conn.createStatement();
		try {
			ResultSet rs = st.executeQuery("SELECT ID FROM " + table);
			boolean found = false;
			try {
				found = rs.next();
				while (found) {
					list.add(rs.getString(1));
					if ( !rs.next() ) {
						found = false;
					}
				}
			} finally {
				conn.commit();
				rs.close();
			}
		} finally {
			st.close();
		}
		}
		return list;
	}

	/**
	 * Reopen the connection if it was closed, e.g. by the Config Editor
	 */
	private void checkConn() throws Exception {
		if (conn == null || conn.isClosed())
			conn = StoreFactory.getConnection(false);
	}
}
