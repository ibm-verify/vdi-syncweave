/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

public class DeltaSysTable {

	private final static int VERSION = 1;

	private static final String PROPERTIES_FILE = "miserver";

	public final static String SYSTABLE = "IDI_DELTA_SYSTABLE";

	private final static String UNIQUE = "{UNIQUE}";

	private final static String SEPARATOR = ";";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	private final static Object verifyLock = new Object();
	private static boolean verified;

	public static DeltaStore getDeltaStore(String identifier,
			boolean isRestarting) throws Exception {
		return new DeltaStore(identifier, isRestarting);
	}
	
	public static DeltaStore getDeltaStore(String identifier,
			boolean isRestarting, Log logger, boolean removeDeleted) throws Exception {
		return new DeltaStore(identifier, isRestarting, logger, removeDeleted);
	}

	public static void verify() throws Exception {
		if (verified)
			return;
		Connection conn = StoreFactory.getConnection(false);
		verify(conn);
		conn.commit();
		conn.close();
	}

	private static void verify(Connection conn) throws Exception {
		if (verified)
			return;
		String createTable = StoreFactory.getProperty("com.ibm.di.store.create.delta.systable");

		Vector<String> queries = new Vector<String>();

		if (createTable == null || createTable.equals("")) {
			createTable = "CREATE TABLE {0} (ID VARCHAR("
				+ StoreFactory.getVarcharLength()
				+ ") NOT NULL, SEQUENCEID int, VERSION int)";
			queries.add(createTable);
		} else {
			StringTokenizer tk = new StringTokenizer(createTable, SEPARATOR);
			while (tk.hasMoreTokens()) {
				queries.add(tk.nextToken().replaceFirst(StoreFactory.REGEX,
						StoreFactory.getVarcharLength()));
			}
		}

		Vector<String> v = new Vector<String>();
		String time = Long.toHexString(System.currentTimeMillis());
		int uniqueIndex = -1;
		for (int i = 0; i < queries.size(); i++) {
			uniqueIndex = queries.elementAt(i).indexOf((UNIQUE));
			if (uniqueIndex != -1) {
				queries.set(i, queries.elementAt(i).replace(UNIQUE, "{1}"));
				v.add(MessageFormat.format(queries.elementAt(i),
						new Object[] { SYSTABLE, time }));
			} else {
				v.add(MessageFormat.format(queries.elementAt(i),
						new Object[] { SYSTABLE }));
			}
		}

		synchronized (verifyLock) {
			StoreFactory.verifyTable(conn, SYSTABLE, v);
		}
		verified = true;
	}

	/**
	 * Return the sequence counter in the systable for a given id.
	 * 
	 * @param bump
	 *            if true, increment sequenceid and immediately write it back.
	 */
	public static int getNextDeltaSequence(String identifier, boolean bump)
			throws Exception {
		
		Connection conn = StoreFactory.getConnection(false);
		verify(conn);
		
		int sequenceid = 0;
		boolean found = false;
		
		// Get sequenceid from DeltaTable ...
		String qry = "SELECT SEQUENCEID,VERSION FROM " + SYSTABLE + " WHERE ID = ?";
		PreparedStatement st = conn.prepareStatement(qry);

		try {
			st.setString(1, identifier);
			ResultSet rs = st.executeQuery();
			found = rs.next();
			if (found) {
				sequenceid = rs.getInt(1);
				int version = rs.getInt(2);
				if (version > VERSION) {
					throw new Exception(sResHash.getString(
							"incompatible.version.in.systable", SYSTABLE));
				}
			}
		} finally {
			st.close();
		}
		
		if (!found) {
			qry = "INSERT INTO " + SYSTABLE + " (id,version) VALUES (?,?)";
			st = conn.prepareStatement(qry);
			try {
				st.setString(1, identifier);
				st.setInt(2, VERSION);
				st.executeUpdate();
			} finally {
				st.close();
			}
			
			qry = "UPDATE " + SYSTABLE + " SET sequenceid=0 WHERE id = ?";
			st = conn.prepareStatement(qry);
			try {
				st.setString(1, identifier);
				st.executeUpdate();
			} finally {
				st.close();
			}
		}

		if (bump) {
			// ... and write the updated sequenceid back
			sequenceid++;
			qry = "UPDATE " + SYSTABLE + " SET sequenceid=? WHERE id=?";
			st = conn.prepareStatement(qry);
			try {
				st.setInt(1, sequenceid);
				st.setString(2, identifier);
				st.executeUpdate();
			} finally {
				st.close();
			}
		}

		conn.commit();
		conn.close();
		return sequenceid;
	}

	/**
	 * Delete the given id from the systable. Used by the Config Editor.
	 */
	public static void delete(String identifier) {

		try {
			Connection conn = StoreFactory.getConnection(false);
			verify(conn);
			
			String qry = "DELETE FROM " + SYSTABLE + " WHERE ID = ?";
			PreparedStatement st = conn.prepareStatement(qry);
			try {
				st.setString(1, identifier);
				st.executeUpdate();
			} finally {
				st.close();
			}
			conn.commit();
			conn.close();
		} catch (Exception ignore) {
		}
	}
}
