/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.Vector;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class implements the functionality for creating and maintaining a single
 * Delta Store table.
 * <p>
 * Note: For internal use only!
 */
public class DeltaStore {

	public static final String TABLE_PREFIX = "IDI_DS_";

	private static final String PROPERTIES_FILE = "miserver";

	private PreparedStatement insEntry = null;
	private PreparedStatement updEntry = null;
	private PreparedStatement findEntry = null;
	private PreparedStatement updSeqNum = null;

	/**
	 * The SQL Statements used to create a new DeltaStore table
	 */
	private final String SQL1 = "CREATE TABLE {0} (ID VARCHAR(" + StoreFactory.getVarcharLength()
			+ ") NOT NULL, SEQUENCEID int, ENTRY long varbinary )";

	private String unique = "{UNIQUE}";

	private String SEPARATOR = ";";

	/**
	 * The table in use
	 */
	private String table;

	/*
	 * Connection objects
	 */
	private Connection conn;

	private ResultSet deletedEntries = null;

	private Statement deletedSt;

	private Vector<String> queries = new Vector<String>();

	private int sequenceid;
	
	private boolean removeDeleted = false;

	private boolean alwaysCommit = true;

	private boolean commitOnClose = false;

	private boolean commitOnEndIter = false;

	private boolean allowDuplicateDeltaKeys = false;

	private int isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	private Log logger;
	
	/**
	 * Determines whether a SolidDB database is used.
	 */
	private boolean isSolidDB = false;

	public DeltaStore(String identifier, boolean isRestarting, Log logger, boolean removeDeleted) throws Exception {
		this(identifier, isRestarting);
		this.logger = logger;
		this.removeDeleted = removeDeleted;
		setRowLocking(isolationLevel);
	}

	public DeltaStore(String identifier, boolean isRestarting) throws Exception {

		// Examine the database URL to determine whether SolidDB is used
		if (StoreFactory.getDefaultDatabase() != null) {
			isSolidDB = StoreFactory.getDefaultDatabase().toLowerCase().contains("solid") ? true : false;
		}

		String sql1 = StoreFactory.getProperty("com.ibm.di.store.create.delta.store");

		if (sql1 == null || sql1.equals("")) {
			sql1 = SQL1;
			queries.add(sql1);
		} else {
			StringTokenizer tk = new StringTokenizer(sql1, SEPARATOR);

			while (tk.hasMoreTokens()) {
				queries.add(tk.nextToken().replaceFirst(StoreFactory.REGEX, StoreFactory.getVarcharLength()));
			}
		}

		StoreFactory.debugmsg(sResHash.getString("the.delta.sql1", sql1));

		if (identifier == null || identifier.trim().length() == 0) {
			throw new Exception(sResHash.getString("invalid.deltastore.identifier", identifier));
		}

		if (identifier.startsWith(TABLE_PREFIX))
			table = identifier;
		else
			table = TABLE_PREFIX + identifier;

		
		table = "\""+table+"\""; 
		// Get connection to default database with autocommit set to false
		// meaning transactions are enabled.
		conn = StoreFactory.getConnection(false);

		// Verify tables
		int uniqueIndex = -1;
		Vector<String> v = new Vector<String>();
		String time = Long.toHexString(System.currentTimeMillis());
		for (int i = 0; i < queries.size(); i++) {
			uniqueIndex = queries.elementAt(i).indexOf(unique);
			if (uniqueIndex != -1) {
				queries.set(i, queries.elementAt(i).replace(unique, "{1}"));
				v.add(MessageFormat.format(queries.elementAt(i), new Object[] { table, time }));
			} else {
				v.add(MessageFormat.format(queries.elementAt(i), new Object[] { table }));
			}
		}
		StoreFactory.verifyTable(conn, table, v);

		// Get next sequence id from delta systable
		sequenceid = DeltaSysTable.getNextDeltaSequence(identifier, !isRestarting);

		// Initialize the prepared statements in order to reuse them.
		// The sequenceid will not change during the whole AL execution
		// so we can set it directly here
		updSeqNum = conn.prepareStatement("UPDATE " + table + " SET sequenceid = " + sequenceid + " WHERE id = ?");
		insEntry = conn.prepareStatement("INSERT INTO " + table + " (id,sequenceid,entry) VALUES (?," + sequenceid + ",?)");
		updEntry = conn.prepareStatement("UPDATE " + table + " SET sequenceid = " + sequenceid + ", entry = ? WHERE id = ?");
		findEntry = conn.prepareStatement("SELECT entry,sequenceid FROM " + table + " WHERE id = ?");
	}

	/**
	 * Set the commit behavior of Delta.
	 * 
	 * @param mode
	 *            The intended behavior. Possible values are:
	 *            <ul>
	 *            <li>"After every database operation"
	 *            <li>"On Connector close"
	 *            <li>"On end of AL cycle"
	 *            <li>"No autocommit"
	 *            </ul>
	 * @return false if the requested mode is not a legal value
	 */

	public boolean setCommitMode(String mode) {
		if ("After every database operation".equalsIgnoreCase(mode)) {
			alwaysCommit = true;
			commitOnClose = true;
			commitOnEndIter = true;
		} else if ("On end of AL cycle".equalsIgnoreCase(mode)) {
			alwaysCommit = false;
			commitOnClose = false;
			commitOnEndIter = true;
		} else if ("On Connector close".equalsIgnoreCase(mode)) {
			alwaysCommit = false;
			commitOnClose = true;
			commitOnEndIter = false;
		} else if ("No autocommit".equalsIgnoreCase(mode)) {
			alwaysCommit = false;
			commitOnClose = false;
			commitOnEndIter = false;
		} else {
			return false;
		}
		return true;
	}

	public void closeDelta() throws SQLException {
		if (commitOnClose) {
			conn.commit();
		} else {
			conn.rollback();
		}
		closeReusedStatements();
		conn.close();
	}

	public void closeReusedStatements() throws SQLException{
		// close all reused PreparedStatements
		if (updSeqNum != null && insEntry != null && updEntry != null && findEntry != null) {
			updSeqNum.close();
			insEntry.close();
			updEntry.close();
			findEntry.close();
		}
		
		updSeqNum = null;
		insEntry = null;
		updEntry = null;
		findEntry = null;
	}
	
	public void updateSequence(String key) throws Exception {
		updSeqNum.setString(1, key);
		int count = updSeqNum.executeUpdate();
		
		if (count != 1) {
			throw new Exception(sResHash.getString("update.sequence.for.key", new Object[] { key, Integer.valueOf(count) }));
		}

		if (alwaysCommit)
			conn.commit();
	}

	public void updateEntry(String key, Entry entry) throws Exception {
		byte[] entryBytes = StoreFactory.serializeObject(entry);
		updateEntryBytes(key, entryBytes);
	}
	
	public void updateEntryBytes(String key, byte[] entryBytes) throws Exception {
		updEntry.setBytes(1, entryBytes);
		updEntry.setString(2, key);
		int count = updEntry.executeUpdate();
		
		if (count != 1) {
			throw new Exception(sResHash.getString("update.entry.for.key", new Object[] { key, Integer.valueOf(count) }));
		}

		if (alwaysCommit)
			conn.commit();
	}

	public void insertEntry(String key, Entry entry) throws Exception {
		int count;
		try {
			insEntry.setString(1, key);
			insEntry.setBytes(2, StoreFactory.serializeObject(entry));
			count = insEntry.executeUpdate();
		} catch (SQLException sql) {
			throw new Exception(sResHash.getString("DeltaStore.Insert.Failed", sql.getMessage()), sql);
		}

		if (count != 1) {
			throw new Exception(sResHash.getString("insert.entry.for.key", new Object[] { key, Integer.valueOf(count) }));
		}

		if (alwaysCommit)
			conn.commit();
	}

	public Entry findEntry(String key) throws Exception {
		Entry entry = null;
		PreparedStatement findEntry = conn.prepareStatement("SELECT entry,sequenceid FROM " + table + " WHERE id = ?");
		try {
			findEntry.setString(1, key);
			ResultSet rs = findEntry.executeQuery();
			try {
				if (rs.next()) {
					entry = (Entry) StoreFactory.deserializeObject(rs.getObject(1));
				}
			} finally {
				rs.close();
			}
		} finally {
			findEntry.close();
		}
		return entry;
	}

	public Entry findEntryVerify(String key) throws Exception {
		byte[] entryBytes = findEntryBytesVerify(key);
		Entry entry = (Entry) StoreFactory.deserializeObjectFromBytes(entryBytes);
		return entry;
	}
	
	public byte[] findEntryBytesVerify(String key) throws Exception {
		byte[] entryBytes = null;
		int entryseq = -1;

		findEntry.setString(1, key);
		ResultSet rs = findEntry.executeQuery();
		try {
			if (rs.next()) {
				entryBytes = StoreFactory.getObjectBytes(rs.getObject(1));
				entryseq = rs.getInt(2);
			}
		} finally {
			rs.close();
		}

		if (!allowDuplicateDeltaKeys && entryseq >= sequenceid) {
			throw new Exception(sResHash.getString("duplicate.delta.key", key));
		}

		return entryBytes;
	}

	public void deleteEntry(String key) throws Exception {
		PreparedStatement delEntry = conn.prepareStatement("DELETE FROM " + table + " WHERE id = ?");
		int count;
		try {
			delEntry.setString(1, key);
			count = delEntry.executeUpdate();
		} finally {
			delEntry.close();
		}
		if (count != 1) {
			throw new Exception(sResHash.getString("delete.entry.key", new Object[] { key, Integer.valueOf(count) }));
		}
	}

	/**
	 * Selects the deleted entries from the Delta Store table. 
	 * 
	 * @throws Exception
	 *             if a database access error occurs or the given parameters are
	 *             not ResultSet constants indicating type and concurrency
	 */
	public void selectDeletedEntries() throws Exception {
		// Always use updatable result sets with solidDB. 
		// Committing during iteration of non-updatable ResultSet
		// causes solidDB to throw exception
		if (removeDeleted || isSolidDB) {
			deletedSt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		} else {
			deletedSt = conn.createStatement();
		}
		deletedEntries = deletedSt.executeQuery("SELECT id, entry FROM " + table + " WHERE sequenceid < " + sequenceid
				+ " FOR UPDATE");
	}

	/**
	 * Returns the next deleted Entry.
	 */
	public Entry getNextDeletedEntry(boolean deleteEntry) throws Exception {
		return getNextDeletedEntry(deleteEntry, null);
	}

	/**
	 * Returns the next deleted Entry, given a Set of keys for unchanged
	 * Entries. Will not return any Entry with a key in the given Set.
	 * 
	 * @param deleteEntry
	 *            - If set, also delete the Entry from the deltaStore
	 * @param keys
	 *            - Should be null, or a Set containing keys for unchanged
	 *            entries
	 * @return The next deleted Entry
	 */
	public Entry getNextDeletedEntry(boolean deleteEntry, Set<String> keys) throws Exception {
		if (deletedEntries == null)
			throw new Exception(sResHash.getString("get.next.deleted.entry.without.prior.seldelentry"));

		while (deletedEntries.next()) {
			String key = deletedEntries.getString(1);
			if (keys != null && keys.contains(key))
				continue;

			Entry e = (Entry) StoreFactory.deserializeObject(deletedEntries.getBytes(2));
			if (deleteEntry) {
				// deleteRow() is faster than deleteEntry(key) method
				deletedEntries.deleteRow();
			}
			return e;
		}

		// EOF
		deletedEntries.close();
		deletedEntries = null;
		deletedSt.close();
		deletedSt = null;
		if (alwaysCommit || commitOnEndIter)
			conn.commit();

		return null;

	}

	public Entry getStatistics() {

		Entry e = new Entry();

		e.setAttribute("Sequence ID", Integer.valueOf(sequenceid));

		try {
			Statement st = conn.createStatement();
			try {
				ResultSet rs;
				
				rs = st.executeQuery("SELECT COUNT(ID) FROM " + table);
				try {
					if (rs.next()) {
						e.setAttribute("Number entries", rs.getObject(1));
					} else {
						e.setAttribute("Number entries", Integer.valueOf(0));
					}
				} finally {
					rs.close();
				}
	
				rs = st.executeQuery("SELECT COUNT(ID) FROM " + table + " WHERE sequenceid < " + sequenceid);
				try {
					if (rs.next()) {
						e.setAttribute("Number old entries", rs.getObject(1));
					} else {
						e.setAttribute("Number old entries", Integer.valueOf(0));
					}
				} finally {
					rs.close();
				}
			} finally {
				st.close();
			}
		} catch (Exception error) {
			e.setAttribute("Error", error);
		}

		return e;
	}

	/**
	 * 
	 * @return The statistics for this run
	 * @since 7.0
	 */
	public String getStatisticsString() {

		try {
			Statement st = conn.createStatement();
			Object numEntries;
			Object oldEntries;
			try {
				ResultSet rs;
				
				rs = st.executeQuery("SELECT COUNT(ID) FROM " + table);
				try {
					numEntries = Integer.valueOf(0);
					if (rs.next()) {
						numEntries = rs.getObject(1);
					}
				} finally {
					rs.close();
				}
	
				rs = st.executeQuery("SELECT COUNT(ID) FROM " + table + " WHERE sequenceid < " + sequenceid);
				try {
					oldEntries = Integer.valueOf(0);
					if (rs.next()) {
						oldEntries = rs.getObject(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				st.close();
			}
			return sResHash.getString("DeltaStore.statistics", new Object[] { sequenceid, numEntries, oldEntries });
		} catch (Exception error) {
			return sResHash.getString("DeltaStore.error.statistics", error.getLocalizedMessage());
		}
	}

	/**
	 * Commit the last transactions. The operation will NOT be executed if at
	 * the moment the Delta Store is iterating deleted entries.
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commit() throws SQLException {
		if (conn != null)
			conn.commit();
	}

	/**
	 * Rollback the last transactions. The operation will NOT be executed if at
	 * the moment the Delta Store is iterating deleted entries.
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void rollback() throws SQLException {
		if (conn != null)
			conn.rollback();
	}

	/**
	 * Commit if in commit mode "On end of AL cycle". The operation will NOT be
	 * executed if at the moment the Delta Store is iterating deleted entries.
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commitOnEndIter() throws SQLException {
		if (conn != null && commitOnEndIter)
			conn.commit();
	}

	/**
	 * Indicates whether the duplicate delta keys are allowed.
	 * 
	 * @param allowDuplicateDeltaKeys
	 *            <code>true</code> if duplicate delta keys are allowed
	 *            <code>false</code> otherwise
	 */
	public void setAllowDuplicateDeltaKeys(boolean allowDuplicateDeltaKeys) {
		this.allowDuplicateDeltaKeys = allowDuplicateDeltaKeys;
	}

	/**
	 * This method sets the transaction isolation level used when working with
	 * the Delta Store. Setting higher isolation level reduces the transaction
	 * anomalies known as 'dirty reads', 'repeatable reads' and 'phantom reads'
	 * by using row and table locks.
	 * <p>
	 * The transaction level is set only if transactions and the specified level
	 * are supported by the underlying database.
	 * 
	 * @param level
	 *            the integer value of the level as defined in the
	 *            {@link Connection}
	 * @throws SQLException
	 *             if a database access error occurs.
	 */
	public void setRowLocking(int level) throws Exception {
		if (conn == null) {
			return;
		}

		if (!conn.getMetaData().supportsTransactions()) {
			logger.logwarn(sResHash.getString("delta.transaction.not.supported"));
			return;
		}

		isolationLevel = level;
		if (conn.getMetaData().supportsTransactionIsolationLevel(isolationLevel)) {
			conn.setTransactionIsolation(isolationLevel);
			
			// Some JDBC drivers change only the default isolation level for
			// future transactions, not the isolation level of the current
			// transaction. Therefore commit the current transaction
			// just to be sure the level is set successfully.
			conn.commit();
		} else {
			logger.logwarn(sResHash.getString("delta.transaction.level.not.supported"));
		}
	}
}
