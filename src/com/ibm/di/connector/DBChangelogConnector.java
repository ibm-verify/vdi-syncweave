/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;

/**
 * The DBChangelogConnector provides a way to to detect changes in specific
 * RDBMS tables. The Connector connects to the underline database through JDBC
 * driver and creates Entries from specific 'change table' containing one record
 * per modified record in the target table.
 * <p>
 * The Connector regularly saves current state into the System Store to avoid
 * duplications when retrieving Entries. Records can also be deleted after the
 * retrieving.
 */
public class DBChangelogConnector extends JDBCConnector implements
		ConnectorInterface, ChangelogInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * TMS Filename used in the Connector for error and debug messages
	 */
	private static final String PROPERTIES_FILE = "rdbmschangelogconnector";

	/**
	 * The name of the Connector
	 */
	private static final String myName = "RDBMS Change Detection Connector";
	/**
	 * Parameter Name: {@value #PARAM_START_AT}
	 */
	private static final String PARAM_START_AT = "startAt";
	/**
	 * Start at predefined value - 'EOD'
	 */
	private static final String START_AT_END_OF_DATA = "EOD";

	/**
	 * Column name used as changelog number. This number shows the last
	 * processed record.
	 */
	private String cseqColName = "ibmsnap_commitseq";

	/**
	 * changeTable name used in Connector
	 */
	private String changeTable;

	/**
	 * PropertyStore key used in Connector to store current changelog number
	 */
	private String ppsKey;

	/**
	 * Start position in the changelog table to start read at
	 */
	private String startAt = null;

	/**
	 * pollInterval used in Connector in seconds
	 */
	private int pollInterval = 60;

	/**
	 * maxWait used in Connector
	 */
	private int maxWait = 0;

	/**
	 * firstWait used in Connector
	 */
	private long firstWait = 0;

	/**
	 * lastWait used in Connector
	 */
	private long lastWait = 0;

	/**
	 * changeToken is the last processed record.
	 */
	private Object changeToken;

	/**
	 * If true, the Connector will delete processed entries from the "change
	 * table".
	 */
	private boolean removeProcessed = false;

	private boolean nolockRead = false;
	/**
	 * PreparedStatement used in Connector for executing SQL queries.
	 */
	private PreparedStatement ps;

	/**
	 * ResultSet returned from database to the Connector.
	 */
	private ResultSet rs;

	/**
	 * PropertyStore object used in the Connector to store current changelog
	 * number
	 */
	private PropertyStore pps;

	/**
	 * If true, the Connector will store current changelog number after read.
	 */
	private boolean mAfterRead = true;

	/**
	 * Variable that holds the method used to store the current changelog number
	 */
	private int mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_AFTER_READ;

	/**
	 * Object used for access of the TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor for the DBChangelogConnector object.
	 */
	public DBChangelogConnector() {
		super();
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Reads Connector parameter's values and initialize the Connector.
	 * 
	 * @param o
	 *            Socket object, ConnectorMode object or <code>null</code>
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object o) throws Exception {
		String str;

		BaseConfiguration rcc = getRawConnectorConfiguration();

		ppsKey = rcc.getStringParameter("iteratorStateKey");
		if (ppsKey == null || ppsKey.trim().length() < 1) {
			ppsKey = null;
		} else {
			pps = StoreFactory.getDefaultPropertyStore();
			changeToken = pps.getProperty(ppsKey);
			if ((changeToken != null) && (debugMode())) {
				debug(sResHash.getString("CONNECTOR.RDBMSCHGLOG.CHGTOKEN.INFO",
						ctString(changeToken)));
			}
		}

		str = rcc.getStringParameter("pollInterval");
		if (str != null && str.length() > 0)
			pollInterval = Integer.parseInt(str);

		str = rcc.getStringParameter("maximumWaitTime");
		if (str != null && str.length() > 0)
			maxWait = Integer.parseInt(str);

		removeProcessed = rcc.getBooleanParameter("removeProcessed", false);
		
		nolockRead = rcc.getBooleanParameter("noLockRead", false);
		
		super.initialize(o);

		String stateKeyPersistence = getParam(ChangelogInterface.CONN_PARAM_STATE_KEY_PERSISTENCE);
		if (stateKeyPersistence != null
				&& stateKeyPersistence.trim().length() > 0) {
			if (stateKeyPersistence
					.equals(ChangelogInterface.PARAM_VAL_END_OF_CYCLE)) {
				mAfterRead = false;
				mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_END_OF_CYCLE;
			} else if (stateKeyPersistence
					.equals(ChangelogInterface.PARAM_VAL_MANUAL)) {
				mAfterRead = false;
				mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_MANUAL;
			}
		}

		// String cdcolname =
		// System.getProperty("com.ibm.di.conn.rdbmschlog.cdcolname");
		String cdcolname = getParam("rdbms.chlog.col");

		if (cdcolname != null)
			cseqColName = cdcolname;
	}

	/**
	 * This method prepares the Connector for a sequential read. Retrieves
	 * records from 'change table'.
	 * 
	 * @throws Exception
	 *             If an error occurs while selecting Entries
	 */
	public void selectEntries() throws Exception {

		String schema = getParam("jdbcSchema"); // D533

		if ((schema == null) || (schema.trim().length() == 0)) {
			schema = "";
		} else {
			schema = schema + ".";
		}
		changeTable = schema + getParam("jdbcTable");
		startAt = getParam(PARAM_START_AT).trim();

		if (startAt != null && startAt.length() > 0) {
			if (startAt.equalsIgnoreCase(START_AT_END_OF_DATA)) {
				if (changeToken == null) {
					changeToken = getEODnumber();
				}
			} else {
				if (changeToken == null) {
					try {
						int start = Integer.parseInt(startAt);
						if (start < 1
								|| (start != 1 && start > getNumberOfRecords(
										changeTable, getConnection()))) {
							throw new Exception(
									sResHash
											.getString(
													"CONNECTOR.RDBMSCHGLOG.INVALIDSTARTAT.ERROR.1",
													new Object[] {
															startAt,
															getNumberOfRecords(
																	changeTable,
																	getConnection()) + 1 }));
						}
						if (start != 1) {
							changeToken = getColumnId(startAt);
						}
					} catch (NumberFormatException e) {
						throw new Exception(sResHash.getString(
								"CONNECTOR.RDBMSCHGLOG.INVALIDSTARTAT.ERROR.2",
								new Object[] {
										startAt,
										getNumberOfRecords(changeTable,
												getConnection()) + 1 }));
					}
				}
			}
		} else {
			startAt = null;
		}
		reselect();
	}

	/**
	 * Retrieves records from the 'change table' using the current value of the
	 * StateKey. If StateKey is: <br>
	 * 		<li><code>null</code> and 'Remove Processed Row' parameter is
	 * 		<code>true</code> - the processed rows are deleted from the 'change
	 * 		table'</li><br>
	 * 
	 * 		<li><code>null</code> and 'Remove Processed Row' parameter is
	 * 		<code>false</code> - all entries are retrieved</li><br>
	 * 
	 * 		<li>not <code>null</code> - only entries with <b>ibmsnap_commitseq</b> &gt;
	 * 		StateKey are retrieved</li>
	 * 
	 * @throws Exception
	 *             If an error occurs while retrieving records from the database
	 */
	public void reselect() throws Exception {
		if (ps != null)
			ps.close();

		if (rs != null)
			rs.close();

		if (changeToken != null && removeProcessed)
			removeProcessedRows();

		String sql = "SELECT * FROM " + changeTable;

		if (changeToken != null) {
			sql += " WHERE " + cseqColName + " > ?";
		}
		
		sql += " ORDER BY " + cseqColName;

		if (nolockRead)
			sql += " WITH UR";

		ps = getConnection().prepareStatement(sql);
		
		if (changeToken != null)
			ps.setObject(1, changeToken);

		rs = ps.executeQuery();
		setResultSet(rs);
	}

	/**
	 * Converts attribute to property in the Entry
	 * 
	 * @param e
	 *            The Entry that contains attribute needed conversion
	 * @param attr
	 *            The Attribute need to be converted
	 */
	private void a2p(Entry e, String attr) {
		Attribute a = e.getAttribute(attr);
		if (a != null) {
			e.setProperty(attr, e.getAttribute(attr).getValue(0));
			e.removeAttribute(attr);
		}
	}

	/**
	 * Removes processed rows from the 'change table'. This method deletes all
	 * rows with <code><b>ibmsnap_commitseq</b> &lt;= StateKey</code>. These rows
	 * are actually already returned as entries by the
	 * <code>getNextEntry()</code> function.
	 * <p>
	 * This method is called from the reselect() method only if the parameter
	 * 'Removed Processed Rows' is set to <code>true</code>.
	 * 
	 * @throws Exception
	 *             If the Connector can not delete processed rows in the
	 *             database
	 */
	public void removeProcessedRows() throws Exception {
		PreparedStatement rm = getConnection().prepareStatement("DELETE FROM " + changeTable + " WHERE " + cseqColName + " <= ?");
		try {
			rm.setObject(1, changeToken);
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.RDBMSCHGLOG.REMOVINGBEFORE.INFO", ctString(changeToken)));
			}
			int count = rm.executeUpdate();
			try {
				getConnection().commit();
			} catch (Exception err) {
				err.printStackTrace();
			}
			if (debugMode()) {
				debug(sResHash.getString("CONNECTOR.RDBMSCHGLOG.REMOVED.INFO", "" + count));
			}
		} finally {
			rm.close();
		}
	}

	/**
	 * Converts the Object to String.
	 * <p>
	 * <b>Example:</b> <br>
	 * By using this method you can print the values of the IBMSNAP_COMMITSEQ
	 * and IBMSNAP_INTENTSEQ properties of the read entry.
	 * 
	 * <pre>
	 * var csn = conn.getProperty(&quot;IBMSNAP_COMMITSEQ&quot;);
	 * task.logmsg(&quot;IBMSNAP_COMMITSEQ: &quot;+RDBMS.connector.ctString(csn);
	 * 
	 * var isn = conn.getProperty(&quot;IBMSNAP_INTENTSEQ&quot;);
	 * task.logmsg(&quot;IBMSNAP_INTENTSEQ: &quot;+RDBMS.connector.ctString(isn);
	 * </pre>
	 * 
	 * @param ct
	 *            the Object that need to be converted
	 * 
	 * @return the converted String
	 */
	public String ctString(Object ct) {
		StringBuffer str = new StringBuffer();

		if (ct instanceof byte[]) {
			byte[] b = (byte[]) ct;
			for (int i = 0; i < b.length; i++)
				str.append(toHex((int) b[i]));
		} else {
			str.append(ct.toString());
		}
		return str.toString();
	}

	/**
	 * Converts integer to Hex String.
	 * 
	 * @param n
	 *            the integer that need to be converted to Hex String
	 * 
	 * @return the converted String
	 */
	public String toHex(int n) {
		String s = Integer.toHexString((n & 0xff));
		if (s.length() == 1) {
			return "0" + s;
		} else {
			return s;
		}
	}

	/**
	 * Gets the next Entry object from the 'change table'.
	 * 
	 * @return The next Entry
	 * 
	 * @throws Exception
	 *             If retrieving the next Entry fails.
	 */
	public Entry getNextEntry() throws Exception {

		while (true) {
			Entry e = super.getNextEntry();
			if (e != null) {
				// Reset timers
				firstWait = 0;
				lastWait = 0;

				Attribute operation = e.getAttribute("IBMSNAP_OPERATION");
				if (operation != null && operation.getValue() != null) {
					char op = operation.getValue().charAt(0);
					switch (op) {
					case 'D':
						e.setOp(Entry.OP_DEL);
						break;
					case 'I':
						e.setOp(Entry.OP_ADD);
						break;
					default:
						e.setOp(Entry.OP_MOD);
						break;
					}
				}

				// Move control attrs to properties
				a2p(e, "IBMSNAP_COMMITSEQ");
				a2p(e, "IBMSNAP_INTENTSEQ");
				a2p(e, "IBMSNAP_OPERATION");
				a2p(e, "IBMSNAP_LOGMARKER");

				// Save change token
				changeToken = e.getProperty("IBMSNAP_COMMITSEQ");
				if ((mAfterRead) && (ppsKey != null)) {
					pps.updateProperty(ppsKey, changeToken, true);
				}
				return e;
			}

			if (!doWait()) {
				return null;
			} else {
				reselect();
			}
		}
	}

	/**
	 * This method sleeps for a number of seconds specified by the 'Sleep
	 * Interval' parameter.
	 * <p>
	 * It is called by the <code>getNextEntry()</code> method after an entry
	 * is retrieved from the 'change table' to make the connector
	 * sleep for a specified interval of time before polling for next entry.
	 * If no new entries are added to the 'change table' for a timeout value
	 * the getNextEntry() method will return <code>null</code>.
	 * 
	 * @return boolean flag, if true everything is fine, if false maxWait > 0 and
	 *         (lastWait - firstWait) > maxWait
	 * @throws Exception
	 *             if an error occurs during sleeping.
	 */
	public boolean doWait() throws Exception {

		if (maxWait > 0 && (lastWait - firstWait) > maxWait) {
			return false;
		}

		if (firstWait == 0) {
			firstWait = System.currentTimeMillis() / 1000;
		}

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.RDBMSCHGLOG.SLEEP.INFO",
					new Object[] { "" + pollInterval, "" + firstWait,
							"" + lastWait }));
		}

		Thread.sleep((pollInterval * 1000));

		lastWait = System.currentTimeMillis() / 1000;

		return true;

	}

	/**
	 * {@inheritDoc}
	 */
	public int getStateKeySaveMethod() throws Exception {
		return mStateKeySaveMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveStateKey() throws Exception {
		if ((!mAfterRead) && (ppsKey != null)) {
			pps.updateProperty(ppsKey, changeToken, true);
		}
	}

	/**
	 * This method returns the StateKey used as our Change Detection number.
	 * This number shows the last processed record and is retrieved from the
	 * <b>ibmsnap_commitseq</b> column in the configured table of the underlying
	 * database.
	 * <p>
	 * <b>Example:</b> <br>
	 * Here is an example how to print the StateKey using this method.
	 * 
	 * <pre>
	 * key statekey = conn.getStateKeyObject();
	 * task.logmsg(&quot;Iterator State Key: &quot; + thisConnector.connector.ctString(statekey));
	 * 
	 * </pre>
	 * return an Object representing the commit sequence number of the last
	 *         processed record
	 * throws Exception
	 *             if an error occurs.
	 * @see ChangelogInterface
	 */
	public Object getStateKeyObject() throws Exception {
		return changeToken;
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This method extracts the number of records in the changelog table.
	 * 
	 * @param tableName
	 *            name of the changelog table
	 * @param con
	 *            Connection handle for the currently opened session to the
	 *            database
	 * @return number of records in the specified table
	 * @throws Exception
	 *             <li>SQLException - if could not retrieve the number of
	 *             records</li><br>
	 *             <li>NumberFormatException - if could not parse the returned
	 *             value</li>
	 */
	public static int getNumberOfRecords(String tableName, Connection con) throws Exception {

		int result = 1;
		String sql = "select count(*) from " + tableName;
		Statement stmt = con.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				if (rs.next()) {
					result = Integer.parseInt(rs.getObject(1).toString());
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return result;
	}

	/**
	 * Retrieves content of last record for the changeToken object
	 * 
	 * @return EOD number as String
	 * @throws Exception
	 *             if any error during executing SQL statement occurs
	 */
	private Object getEODnumber() throws Exception {
		Object result = null;
		String sql = "SELECT " + cseqColName + " FROM " + changeTable + " ORDER BY " + cseqColName + " DESC";
		Statement stmt = getConnection().createStatement();
		try {
			stmt.setFetchSize(2);
			ResultSet rs = stmt.executeQuery(sql);
			try {
				if (rs.next()) {
					result = rs.getObject(1);
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return result;
	}

	/**
	 * This method extracts the content of the change column from the change
	 * table.
	 * 
	 * @param startAt
	 *            position of the column
	 * @return column content
	 * @throws Exception
	 */
	private Object getColumnId(String startAt) throws Exception {

		int max;
		try {
			max = Integer.parseInt(startAt);
		} catch (NumberFormatException e) {
			throw new Exception(sResHash.getString("CONNECTOR.RDBMSCHGLOG.INVALIDSTARTAT.ERROR.3", new Object[] { startAt,
					getNumberOfRecords(changeTable, getConnection()) + 1 }));
		}

		Object result = null;
		String sql = "SELECT " + cseqColName + " from " + changeTable + " ORDER BY " + cseqColName;
		Statement stmt = getConnection().createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				int index = 1;
				while (index < max && rs.next()) {
					index++;
				}
				result = rs.getObject(1);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}

		return result;
	}
}
