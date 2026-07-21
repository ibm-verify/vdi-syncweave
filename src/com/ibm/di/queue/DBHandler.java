/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.queue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.ibm.di.connector.PESConnector;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.store.StoreFactory;

/**
 * 
 */
public class DBHandler {
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	// protected MemQ chunk;

	protected int size;

	protected PESConnector connect;

	protected static boolean bDirty = false;

	protected int pageSize;

	IDGenerator gen;

	Log log = RS.getServer() != null ? RS.getServer().getLog() : new Log("miserver");

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * constructor
	 * 
	 * @param size
	 *            threshold if loading pagesize if storing
	 * @param gen
	 */
	public DBHandler(int size, IDGenerator gen) throws Exception {

		this.pageSize = size;
		this.gen = gen;
	}

	/**
	 * Initializes the system store parameters
	 * 
	 * @param sDBName
	 *            system store database name
	 * @param jdbcLogin
	 *            username to connect to the db
	 * @param jdbcPassword
	 *            password to connect to the db
	 * @param sTblName
	 *            table name
	 * 
	 * @throws Exception
	 *             if system store is not initialized properly
	 */
	public void initialize(String sDBName, String jdbcLogin,
			String jdbcPassword, String sTblName) throws Exception {

		if ((sDBName == null || sDBName.equals(""))
				|| (sTblName == null || sTblName.equals(""))) {
			log.info("mbuffer.thread.initialization.failed");
		}
		try {
			connect = (PESConnector) SystemFunctions
					.loadConnector("system:/Connectors/ibmdi.SystemStoreConnector");
		} catch (Exception err) {
			throw new Exception(log
					.getString("MEMQ.ERROR.CREATE.SYSTEMSTORECONNECTOR"));
		}
		connect.setParam("sDBName", sDBName);
		connect.setParam("jdbcLogin", jdbcLogin);
		connect.setParam("jdbcPassword", jdbcPassword);
		connect.setParam("dbTableName", sTblName);
		connect.setParam("selectionMode", "all");
		connect.setParam("pesCommit", "After every database operation");
		connect.setParam("keyAttribute", "ID");
		connect.setParam("deleteOnClose", "true");
		connect.initialize(new Object());
	}

	/**
	 * terminates the system store operation
	 * 
	 */
	public void terminate() {
		if (connect != null)
			connect.terminate();
	}

	public boolean isStoreEmpty() {
		try {
			if (connect != null) {
				connect.selectEntries();
				Entry e = connect.getNextEntry();
				if (e == null)
					return true;
				else
					return false;

			}
			// Properties prop = null;
			// Connection conn = StoreFactory.getConnection(connect
			// .getParam("sDBName"), false, connect.getParam("jdbcLogin"),
			// connect.getParam("jdbcPassword"), prop);
			// String sql = "SELECT * FROM " + ""
			// + connect.getParam("dbTableName");
			// Statement stmt1 = conn.createStatement();
			// ResultSet rs1 = stmt1.executeQuery(sql);
			// boolean found = rs1.next();
			// rs1.close();
			// stmt1.close();
			// conn.commit();
			// conn.close();
			// return !found;
		} catch (Exception e) {
			log.error("MEMQ.ERROR.CONNECTING.DB");
			return false;
		}
		return false;
	}

	/**
	 * Deletes contents of table associated with queue
	 */

	public void emptySystemStore() throws Exception {
		Entry e = null;
		SearchCriteria sc = null;
		try {
			connect.selectEntries();
			do {
				e = connect.getNextEntry();
				if (e != null) {
					sc = new SearchCriteria("ID", SearchCriteria.EXACT, e
							.getAttribute("ID").getValue());
					connect.deleteEntry(e, sc);
				} else {
					break;
				}
			} while (true);

		} catch (Exception ex) {
			throw new Exception(log.getString("MEMQ.ERROR.EMPTY.SYSTEMSTORE"));
		}
	}

	/**
	 * thread run method stores pages from chunk to the system store
	 */
	public void addToDB(MemQ chunk) throws Exception {

		while (!chunk.isEmpty()) {
			if (((MemQ) chunk.firstElement()).size() == pageSize) {
				try {
					Entry e = new Entry();
					int ID = Integer.valueOf(gen.getID());
					e.addAttributeValue("ID", ID);
					e.addAttributeValue("ENTRY", (MemQ) chunk.read());
					connect.putEntry(e);
				} catch (Exception ex) {
					// e.printStackTrace();
					throw new Exception(log.getString("ERROR.ADDING.TEMP"));
				}
			}
		}
	}

	public PESConnector getConnect() {
		return connect;
	}

	// public static void setConnect(PESConnector connect) {
	// DBHandler.connect = connect;
	// }

}
