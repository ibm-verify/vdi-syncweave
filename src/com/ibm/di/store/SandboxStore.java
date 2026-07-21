/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.store;

import java.sql.*;
import java.util.Vector;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * The SandboxStore class provides methods to store and retrieve java objects
 * for use in a sandbox context.
 */

import com.ibm.di.server.*;
import com.ibm.di.entry.*;
import com.ibm.di.config.interfaces.AssemblyLineConfig;

public class SandboxStore {

	private String unique = "{UNIQUE}";

	private String SEPARATOR = ";";

	private Vector<String> queries = new Vector<String>();

	private Connection conn;

	private Log log;

	private String table = "SANDBOX_MASTER_TABLE";

	private long entryIndex = 1;

	public SandboxStore(String database, Log log, boolean initialize)
			throws Exception {

		this.log = log;

		// Get connection to default database
		conn = StoreFactory.getConnection(database);

		// Drop table?
		if (initialize)
			StoreFactory.dropTable(conn, table);

		String createTable = StoreFactory.getProperty("com.ibm.di.store.create.sandbox.store");

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

		// Verify tables
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
	}

	public void close() {
		try {
			if (conn != null)
				conn.close();
		} catch (Exception ignore) {
		}

		conn = null;
	}

	/**
	 * Return a previously stored TCB
	 */
	public TaskCallBlock getTCB(AssemblyLineConfig alc, TaskInterface task)
			throws Exception {
		
		Entry tcb = null;
		
		Statement st = conn.createStatement();
		try {
			String qry = "select entry from " + table + " where id = '$TCB'";
			ResultSet rs = st.executeQuery(qry);
			try {
				if (rs.next()) {
					tcb = (TaskCallBlock) StoreFactory.deserializeObject(rs.getObject(1));
				}
			} finally {
				rs.close();
			}
		} finally {
			st.close();
		}

		if (tcb == null)
			return null;

		return new TaskCallBlock(tcb, alc, task);
	}

	/**
	 * Save TCB
	 */
	public void setTCB(TaskCallBlock tcb) throws Exception {
		try {
			Statement st = conn.createStatement();
			try {
				st.executeUpdate("delete from " + table + " where id = '$TCB'");
			} finally {
				st.close();
			}
		} catch (Exception ex) {
			log.logdebug(ex.toString());
		}

		PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " (ID, ENTRY) " + " VALUES (?,?) ");
		try {
			ps.setString(1, "$TCB");
			ps.setBytes(2, StoreFactory.serializeObject((tcb == null) ? null : tcb.clone()));
			ps.executeUpdate();
		} finally {
			ps.close();
		}
	}

	public void addEntry(Entry entry) throws Exception {
		PreparedStatement psAdd = conn.prepareStatement("INSERT INTO " + table + " (ID, ENTRY) " + " VALUES (?,?) ");
		try {
			psAdd.setString(1, "ENTRY-" + entryIndex++);
			psAdd.setBytes(2, StoreFactory.serializeObject(entry));
			psAdd.executeUpdate();
		} finally {
			psAdd.close();
		}
	}

	public Entry getNextEntry() throws Exception {
		Entry entry = null;

		PreparedStatement psGet = conn.prepareStatement("SELECT ENTRY FROM " + table + " WHERE ID = ?");
		try {
			psGet.setString(1, "ENTRY-" + entryIndex++);

			ResultSet rs = psGet.executeQuery();
			try {
				if (rs.next()) {
					entry = (Entry) StoreFactory.deserializeObject(rs.getObject(1));
				}
			} finally {
				rs.close();
			}
		} finally {
			psGet.close();
		}

		return entry;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public Log getLog() {
		return log;
	}

}
