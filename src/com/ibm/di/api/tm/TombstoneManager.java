/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.tm;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.ibm.di.api.APIEngine;
import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.store.StoreFactory;

/**
 * This class is used to manage Tombstone objects. It contains methods to
 * retrieve and delete tombstone objects on a specific criteria.
 */
public class TombstoneManager {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// Configuration properties
	/**
	 * When this property is present and contains an integer value greater than
	 * 0 the Tombstone Manager will automatically delete all tombstone records
	 * that are older than the specified number of days. The logic for tombstone
	 * record deletion is triggered on TDI Server startup and once a day on a
	 * long running TDI Server.<br>
	 * <br>
	 * The default value for this property is "0".
	 * 
	 */
	public static final String PROP_AUTODEL_AGE = "com.ibm.di.tm.autodel.age";

	/**
	 * This property specifies the total number of tombstone records that will
	 * trigger the logic for leveling the number of tombstone records to a
	 * certain number.<br>
	 * <br>
	 * The default value for this property is "10000".
	 * 
	 */
	public static final String PROP_AUTODEL_RECORDS_TRIGGER = "com.ibm.di.tm.autodel.records.trigger.on";

	/**
	 * This property specifies the number of tombstone records to keep when the
	 * logic for leveling is triggered as per the
	 * {@link #PROP_AUTODEL_RECORDS_TRIGGER} property. Alter the cleanup only
	 * the most recent (the value of this property) number of records will be
	 * kept.<br>
	 * <br>
	 * The default value for this property is "5000".
	 * 
	 */
	public static final String PROP_AUTODEL_RECORDS_MAX = "com.ibm.di.tm.autodel.records.max";

	/**
	 * When this property is set to "true" the Tombstone Manager will create
	 * tombstones for every AssemblyLine and Config Instance regardless of the
	 * values specified in the configurations. This is useful to turn on
	 * tombstone creation for pre-6.1 configurations that do not have tombstone
	 * values without modifying the configurations. <br>
	 * <br>
	 * The default value for this property is "false".
	 * 
	 */
	public static final String PROP_CREATE_ALL = "com.ibm.di.tm.create.all";

	/**
	 * This property is used to override the default "CREATE TABLE..." SQL
	 * statement. Use it if you want to add more records in the new table.
	 */
	public static final String PROP_CREATE_TABLE = "com.ibm.di.store.create.tombstones";

	/**
	 * This property is used to override the default "INSERT INTO..." SQL
	 * statement. Use it if you want to add more records in the new table.
	 */
	public static final String PROP_UPDATE_TABLE = "com.ibm.di.store.update.tombstones";

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_COMPONENT_TYPE_ID} property. <br>
	 * The value of this constant is: {@value #COMPONENT_TYPE_CONFIG_INSTANCE}
	 */
	public static final int COMPONENT_TYPE_CONFIG_INSTANCE = 0;

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_COMPONENT_TYPE_ID} property. <br>
	 * The value of this constant is: {@value #COMPONENT_TYPE_ASSEMBLY_LINE}
	 */
	public static final int COMPONENT_TYPE_ASSEMBLY_LINE = 1;

	/**
	 * This constant could be used as value for the
	 * {@link Tombstone#PROPERTY_NAME_COMPONENT_TYPE_ID} property. <br>
	 * The value of this constant is: {@value #COMPONENT_TYPE_EVENT_HANDLER}
	 */
	public static final int COMPONENT_TYPE_EVENT_HANDLER = 2;

	/**
	 * This constant has the name of the table that will be created/queried.
	 */
	public static final String TABLE_NAME = "IDI_TOMBSTONE";

	/**
	 * A constant for the column - ID
	 */
	public static final String PROP_FIELD_ID = "ID";

	/**
	 * A constant for the column {@value #PROP_FIELD_COMPONENT_TYPE_ID}
	 */
	public static final String PROP_FIELD_COMPONENT_TYPE_ID = "COMPONENT_TYPE_ID";

	/**
	 * A constant for the column {@value #PROP_FIELD_EVENT_TYPE_ID}
	 */
	public static final String PROP_FIELD_EVENT_TYPE_ID = "EVENT_TYPE_ID";

	/**
	 * A constant for the column {@value #PROP_FIELD_START_TIME}
	 */
	public static final String PROP_FIELD_START_TIME = "START_TIME";

	/**
	 * A constant for the column {@value #PROP_FIELD_CREATED_ON}
	 */
	public static final String PROP_FIELD_CREATED_ON = "CREATED_ON";

	/**
	 * A constant for the column {@value #PROP_FIELD_COMPONENT_NAME}
	 */
	public static final String PROP_FIELD_COMPONENT_NAME = "COMPONENT_NAME";

	/**
	 * A constant for the column {@value #PROP_FIELD_CONFIGURATION}
	 */
	public static final String PROP_FIELD_CONFIGURATION = "CONFIGURATION";

	/**
	 * A constant for the column {@value #PROP_FIELD_EXIT_CODE}
	 */
	public static final String PROP_FIELD_EXIT_CODE = "EXIT_CODE";

	/**
	 * A constant for the column {@value #PROP_FIELD_ERROR_DESCR}
	 */
	public static final String PROP_FIELD_ERROR_DESCR = "ERROR_DESCR";

	/**
	 * A constant for the column {@value #PROP_FIELD_GUID}
	 */
	public static final String PROP_FIELD_GUID = "GUID";

	/**
	 * A constant for the column {@value #PROP_FIELD_STATS}
	 */
	public static final String PROP_FIELD_STATS = "STATS";

	/**
	 * A constant for the column {@value #PROP_FIELD_USER_MESSAGE}
	 */
	public static final String PROP_FIELD_USER_MESSAGE = "USER_MESSAGE";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = APIEngine.getResHash();

	/**
	 * Connection object, used for database operations
	 */
	private Connection mConnection = null;

	/**
	 * Object that locks DB access.
	 */
	private Object mDBLock = new Object();

	/**
	 * {@link PreparedStatement}
	 */
	private PreparedStatement updatePS = null;

	private PreparedStatement selectCI = null;

	private PreparedStatement checkCI = null;

	private PreparedStatement selectAL = null;

	private PreparedStatement checkAL = null;

	/**
	 * Default constructor for this object. Constructing this object will try to
	 * create a new table in the back-end store, if the table does not already
	 * exist.
	 * <p>
	 * From 7.0 the cleaning process will not be triggered in the constructor
	 * but should be started immediately after that using startAutoCleaner()
	 * method.
	 * 
	 * @throws DIException
	 */
	public TombstoneManager() throws DIException {
		// init connection
		try {
			setConnection(StoreFactory.getConnection());
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.COULD.NOT.INITIALIZE.TOMBSTONE.MANAGER.DB.CONNECTION"), e);
		}

		// inititialize Tombstone Manager database table
		createTable();

		// initialize Tombstone Manager listener
		try {
			TombstoneManagerListener.initializeListener(com.ibm.di.api.APIEngine.getLocalSession(), this);
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.COULD.NOT.INITIALIZE.TOMBSTONE.MANAGER.LISTENER"), e);
		}

		try {
			String updateSQL = System.getProperty(PROP_UPDATE_TABLE);
			if (updateSQL == null || updateSQL.trim().length() == 0)
				updateSQL = "INSERT INTO " + TABLE_NAME + "("
				+ PROP_FIELD_COMPONENT_TYPE_ID + ", " + PROP_FIELD_EVENT_TYPE_ID + ", "
				+ PROP_FIELD_START_TIME + ", " + PROP_FIELD_CREATED_ON + ", "
				+ PROP_FIELD_COMPONENT_NAME + ", " + PROP_FIELD_CONFIGURATION + ", "
				+ PROP_FIELD_EXIT_CODE + ", " + PROP_FIELD_ERROR_DESCR + ", "
				+ PROP_FIELD_STATS + ", " + PROP_FIELD_GUID + ", "
				+ PROP_FIELD_USER_MESSAGE + ")" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			updatePS = mConnection.prepareStatement(updateSQL);

			selectCI = mConnection.prepareStatement("SELECT DISTINCT " + PROP_FIELD_CONFIGURATION + " FROM " + TABLE_NAME
					+ " WHERE " + PROP_FIELD_COMPONENT_TYPE_ID + "=" + COMPONENT_TYPE_CONFIG_INSTANCE);
			checkCI = mConnection.prepareStatement("SELECT COUNT(" + TombstoneManager.PROP_FIELD_ID + ") FROM "
					+ TombstoneManager.TABLE_NAME + " WHERE " + PROP_FIELD_COMPONENT_TYPE_ID + "=" + COMPONENT_TYPE_CONFIG_INSTANCE
					+ " AND " + PROP_FIELD_CONFIGURATION + "=?");
			selectAL = mConnection.prepareStatement("SELECT DISTINCT " + PROP_FIELD_COMPONENT_NAME + " FROM " + TABLE_NAME
					+ " WHERE " + PROP_FIELD_COMPONENT_TYPE_ID + "=" + COMPONENT_TYPE_ASSEMBLY_LINE + " AND "
					+ PROP_FIELD_CONFIGURATION + "=?");
			checkAL = mConnection.prepareStatement("SELECT COUNT(" + TombstoneManager.PROP_FIELD_ID + ") FROM "
					+ TombstoneManager.TABLE_NAME + " WHERE " + PROP_FIELD_COMPONENT_TYPE_ID + "=" + COMPONENT_TYPE_ASSEMBLY_LINE
					+ " AND " + PROP_FIELD_CONFIGURATION + "=?" + " AND " + PROP_FIELD_COMPONENT_NAME + "=?");
		} catch (SQLException e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.INSERT.OPERATION.OF.TOMBSTONE.RECORD.IN.DATABASE.FAILED"), e);
		}
	}

	/**
	 * Starts a new thread for the TombstoneAutoCleaner
	 */
	public void startAutoCleaner() {
		String autoDeleteAge = System.getProperty(PROP_AUTODEL_AGE);
		if (autoDeleteAge != null && autoDeleteAge.trim().length() > 0) {
			int age = Integer.parseInt(autoDeleteAge);
			if (age > 0) {
				TombstoneAutoCleaner cleaner = new TombstoneAutoCleaner(this, age);
				cleaner.start();
			}
		}
	}

	/**
	 * This method is used to create database table for tombstone records. Does
	 * nothing, if table is already created.
	 * 
	 * @throws DIException
	 */
	private void createTable() throws DIException {

		try {
			if (StoreFactory.tableExists(mConnection, TABLE_NAME)) {
				// table, containing tombstone records is already created; exit
				// method
				return;
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.CHECKING.FOR.TOMBSTONE.RECORDS.TABLE"), e);
		}

		try {
			Statement createStatement = mConnection.createStatement();
			String createSQL = System.getProperty(PROP_CREATE_TABLE);
			if (createSQL == null || createSQL.trim().length() < 1) {
				createSQL = "CREATE TABLE " + TABLE_NAME + " ( " + PROP_FIELD_ID + " INT GENERATED ALWAYS AS IDENTITY, "
						+ PROP_FIELD_COMPONENT_TYPE_ID + " INT, " + PROP_FIELD_EVENT_TYPE_ID + " INT, " + PROP_FIELD_START_TIME
						+ " TIMESTAMP, " + PROP_FIELD_CREATED_ON + " TIMESTAMP, " + PROP_FIELD_COMPONENT_NAME + " VARCHAR(1024), "
						+ PROP_FIELD_CONFIGURATION + " VARCHAR(1024), " + PROP_FIELD_EXIT_CODE + " INT, " + PROP_FIELD_ERROR_DESCR
						+ " VARCHAR(1024), " + PROP_FIELD_STATS + " LONG VARCHAR FOR BIT DATA, " + PROP_FIELD_GUID
						+ " VARCHAR(1024) NOT NULL, " + PROP_FIELD_USER_MESSAGE + " VARCHAR(1024), " + "UNIQUE (" + PROP_FIELD_ID
						+ ", " + PROP_FIELD_GUID + ")" + ")";
			}
			if (APIEngine.isDebugEnabled()) {
				APIEngine.logDebug(sResHash.getString("SEVER.API.TABLE.NAME.CREATE", new Object[] { TABLE_NAME, createSQL }));
			}

			synchronized (mDBLock) {
				for (String sql:createSQL.split(";"))
					createStatement.execute(sql);
			}
			createStatement.close();

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.CREATING.TOMBSTONE.TABLE"), e);
		}
	}

	// Methods for extracting Tombstone objects

	/**
	 * Get single tombstone object, uniquely identified by the specified GUID
	 * 
	 * @param aGUID
	 *            The GUID of the requested tombstone
	 * @return Tombstone object
	 * @throws DIException
	 */
	public Tombstone getTombstone(String aGUID) throws DIException {

		Tombstone result = null;

		try {
			PreparedStatement ps = mConnection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE " + PROP_FIELD_GUID
					+ " = ?");
			try {
				ps.setString(1, aGUID);

				ResultSet rs = null;
				synchronized (mDBLock) {
					rs = ps.executeQuery();
					try {
						if (rs.next()) {
							result = constructTombstoneFromResultSet(rs);
						}
					} finally {
						silentCloseResultSet(rs);
					}
				}
			} finally {
				silentCloseStatement(ps);
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.GETTING.TOMBSTONE.RECORD.BY.GUID"), e);
		}

		return result;
	}

	/**
	 * Gets the recent n number of tombstones for a specified AssemblyLine
	 * 
	 * @since 6.1.1
	 * @param assemblyLineName
	 *            The name of the AssemblyLine.
	 * @param configID
	 *            The name of the AssmeblyLine's configuration.
	 * @param recentNumberOfTombstones
	 *            The recent n number of tombstones to be returned.
	 * @return an array of Tombstone objects.
	 * @throws DIException
	 */
	public Tombstone[] getAssemblyLineTombstones(String assemblyLineName, String configID, int recentNumberOfTombstones)
			throws DIException {

		Tombstone[] result = null;
		boolean isComponentParamSet = !assemblyLineName.equals("");
		boolean isConfigIDParamSet = !configID.equals("");
		if (isComponentParamSet == false || isConfigIDParamSet == false || recentNumberOfTombstones < 1) {
			return result; // return null if AL and configuration name not set
		}

		try {

			List<Tombstone> tmList = readAssemblyLineTombstonesFromDB(assemblyLineName, configID, recentNumberOfTombstones);
			result = getTombStonesFromArrayList(tmList);

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS.2"), e);
		}

		return result;
	}

	/**
	 * Read tombstones for an AssemblyLine from the database.
	 * 
	 * @param assemblyLineName
	 *            The name of the AssemblyLine.
	 * @param configID
	 *            The id of the configuration instance which is a parent of the
	 *            AssemblyLine.
	 * @param recentNumberOfTombstones
	 *            The recent number of tombstones to be returned.
	 * @return A list of tombstones for the specified AssemblyLine.
	 * @throws SQLException
	 *             Database error.
	 * @throws DIException
	 *             If a tombstone cannot be constructed from a database record.
	 */
	private List<Tombstone> readAssemblyLineTombstonesFromDB(String assemblyLineName, String configID, int recentNumberOfTombstones)
			throws SQLException, DIException {

		int counter = 0;

		/*
		 * The next group of code lines are used to create following SQL
		 * statements, required for selection of particular group of tombstone
		 * objects
		 * 
		 * SELECT * FROM IDI_TOMBSTONE WHERE (0=0) AND COMPONENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? AND CONFIGURATION = ? ODER BY START_TIME DESC
		 */

		String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROP_FIELD_COMPONENT_TYPE_ID + " = ? AND "
				+ PROP_FIELD_COMPONENT_NAME + "= ? AND " + PROP_FIELD_CONFIGURATION + " = ? ORDER BY " + PROP_FIELD_CREATED_ON
				+ " DESC ";

		// Create prepared statement, based on constructed SQL and set
		// proper group of parameters to
		// this statement
		List<Tombstone> resultArrayList = new ArrayList<Tombstone>();

		PreparedStatement ps = mConnection.prepareStatement(selectSQL);
		try {
			// Set parameters of the properly constructed prepared statement
			ps.setInt(1, COMPONENT_TYPE_ASSEMBLY_LINE);
			ps.setString(2, assemblyLineName);
			ps.setString(3, configID);

			ResultSet rs = null;
			synchronized (mDBLock) {
				rs = ps.executeQuery();
				try {
					while (rs.next()) {
						if (counter < recentNumberOfTombstones) {
							Tombstone temp = constructTombstoneFromResultSet(rs);
							resultArrayList.add(temp);
							counter++;
						}
					}
				} finally {
					silentCloseResultSet(rs);
				}
			}
		} finally {
			silentCloseStatement(ps);
		}

		return resultArrayList;
	}

	/**
	 * Get all available tombstones for a specified AssemblyLine.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine whose tombstones will be
	 *            extracted.
	 * @param aConfigID
	 *            The AssemblyLine's configuration.
	 * @return An array of Tombstone objects for the specified AssemblyLine.
	 * @throws DIException
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID) throws DIException {
		return getTombstones(aAssemblyLineName, aConfigID, null, null, COMPONENT_TYPE_ASSEMBLY_LINE);
	}

	/**
	 * Get all available tombstones for a specified AssemblyLine with timestamps
	 * in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine whose tombstones will be
	 *            extracted.
	 * @param aConfigID
	 *            The AssemblyLine's configuration.
	 * @param aStartTime
	 *            The start time of the period for which tombstones are
	 *            retrieved.
	 * @param aEndTime
	 *            The end time of the period for which tombstones are retrieved.
	 * @return An array of Tombstone objects for the specified AssemblyLine.
	 * @throws DIException
	 */
	public Tombstone[] getAssemblyLineTombstones(String aAssemblyLineName, String aConfigID, Date aStartTime, Date aEndTime)
			throws DIException {
		return getTombstones(aAssemblyLineName, aConfigID, aStartTime, aEndTime, COMPONENT_TYPE_ASSEMBLY_LINE);
	}

	/**
	 * Get all available tombstones for a specified Configuration Instance.
	 * 
	 * @param aConfigID
	 *            The configuration ID.
	 * @return An array of Config Instance Tombstone objects.
	 * @throws DIException
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID) throws DIException {
		// Set configID parameter as component name instead of configID. In this
		// case the configID
		// is used as a component name and not as a configuration identifier of
		// a specified component.
		return getTombstones(aConfigID, "", null, null, COMPONENT_TYPE_CONFIG_INSTANCE);
	}

	/**
	 * Get all available tombstones for a specified ConfigInstance with
	 * timestamps in the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aConfigID
	 *            The configuration ID.
	 * @param aStartTime
	 *            The start time of the period for which tombstone records are
	 *            retrieved.
	 * @param aEndTime
	 *            The end time of the period for which tombstone records are
	 *            retrieved.
	 * @return An array of Config Instance Tombstone objects.
	 * @throws DIException
	 */
	public Tombstone[] getConfigInstanceTombstones(String aConfigID, Date aStartTime, Date aEndTime) throws DIException {
		// Set configID parameter as component name instead of configID. In this
		// case the configID
		// is used as a component name and not as a configuration identifier of
		// a specified component.
		return getTombstones(aConfigID, "", aStartTime, aEndTime, COMPONENT_TYPE_CONFIG_INSTANCE);
	}

	/**
	 * Get all available tombstones with timestamps in the interval specified by
	 * aStartTime and aEndTime.
	 * 
	 * @param aStartTime
	 *            The start time of the period for which tombstone records are
	 *            retrieved.
	 * @param aEndTime
	 *            The end time of the period for which tombstone records are
	 *            retrieved.
	 * @return An array of Tombstone objects.
	 * @throws DIException
	 */
	public Tombstone[] getTombstones(Date aStartTime, Date aEndTime) throws DIException {
		return getTombstones("", "", aStartTime, aEndTime, -1);
	}

	// Methods for clearing old tombstone records

	/**
	 * Deletes all Tombstone objects that are older than the specified number of
	 * days.
	 * 
	 * @param aDaysCount
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteTombstones(int aDaysCount) throws DIException {
		return deleteTombstones("", "", -1, aDaysCount, -1, null, null);
	}

	/**
	 * Deletes all tombstone object records except a specified number of most
	 * recently created ones.
	 * 
	 * @param aRecentTombstonesToKeep
	 *            number of recent tombstones to keep.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 */
	public int keepMostRecentTombstones(int aRecentTombstonesToKeep) throws DIException {
		return deleteTombstones("", "", -1, -1, aRecentTombstonesToKeep, null, null);
	}

	/**
	 * Deletes all Tombstone objects created for the specified AssemblyLine.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The AssmeblyLine configuration.
	 * @return The number of deleted records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID) throws DIException {
		return deleteTombstones(aConfigID, aAssemblyLineName, COMPONENT_TYPE_ASSEMBLY_LINE, -1, -1, null, null);
	}

	/**
	 * Deletes all Tombstone objects for a specified AssemblyLine that are older
	 * than the specified number of days.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The AssemblyLine configuration.
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, int aDays) throws DIException {
		return deleteTombstones(aConfigID, aAssemblyLineName, COMPONENT_TYPE_ASSEMBLY_LINE, aDays, -1, null, null);
	}

	/**
	 * Deletes all Tombstone objects for a specified AssemblyLine that are older
	 * than the specified date.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The AssemblyLine configuration.
	 * @param olderThanDate
	 *            Date
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date olderThanDate) throws DIException {
		return deleteTombstones(aConfigID, aAssemblyLineName, COMPONENT_TYPE_ASSEMBLY_LINE, -1, -1, null, olderThanDate);
	}

	/**
	 * Deletes all Tombstone objects for a specified AssemblyLine that are in
	 * the specified <code>Date</code> range.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine.
	 * @param aConfigID
	 *            The AssemblyLine configuration.
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteALTombstones(String aAssemblyLineName, String aConfigID, Date startDate, Date endDate) throws DIException {
		return deleteTombstones(aConfigID, aAssemblyLineName, COMPONENT_TYPE_ASSEMBLY_LINE, -1, -1, startDate, endDate);
	}

	/**
	 * Deletes all tombstones for a specified AssemblyLine except a specified
	 * number of most recently created ones.
	 * 
	 * @param aAssemblyLineName
	 *            The name of the AssemblyLine whose tombstone records will be
	 *            deleted.
	 * @param aConfigID
	 *            The AssemblyLine configuration.
	 * @param aRecentTombstonesToKeep
	 *            Number of recent tombstones to keep.
	 * @return The number of deleted records.
	 * @throws DIException
	 */
	public int keepMostRecentALTombstones(String aAssemblyLineName, String aConfigID, int aRecentTombstonesToKeep)
			throws DIException {
		return deleteTombstones(aConfigID, aAssemblyLineName, COMPONENT_TYPE_ASSEMBLY_LINE, -1, aRecentTombstonesToKeep, null, null);
	}

	/**
	 * Deletes all tombstones for a specified Config Instance.
	 * 
	 * @param aConfigID
	 *            The configuration ID.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteCITombstones(String aConfigID) throws DIException {
		// Set configID parameter as component name instead of configID, since
		// in this case the configID
		// is used as component name not as configuration identifier of a
		// specified component
		return deleteTombstones("", aConfigID, COMPONENT_TYPE_CONFIG_INSTANCE, -1, -1, null, null);
	}

	/**
	 * Deletes all Tombstone objects for a specified Config Instance that are
	 * older than the specified number of days.
	 * 
	 * @param aConfigID
	 *            The configuration ID.
	 * @param aDays
	 *            Number of days.
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int deleteCITombstones(String aConfigID, int aDays) throws DIException {
		// Set configID parameter as component name instead of configID, since
		// in this case the configID
		// is used as component name not as configuration identifier of a
		// specified component
		return deleteTombstones("", aConfigID, COMPONENT_TYPE_CONFIG_INSTANCE, aDays, -1, null, null);
	}

	/**
	 * Deletes all tombstones for a Config Instance except a specified number of
	 * recently created ones.
	 * 
	 * @param aConfigID
	 *            The configuration ID.
	 * @param aRecentTombstonesToKeep
	 *            The number of most recent tombstones to keep.
	 * @return The number of deleted records.
	 * @throws DIException
	 */
	public int keepMostRecentCITombstones(String aConfigID, int aRecentTombstonesToKeep) throws DIException {
		// Set configID parameter as component name instead of configID, since
		// in this case the configID
		// is used as component name not as configuration identifier of a
		// specified component
		return deleteTombstones("", aConfigID, COMPONENT_TYPE_CONFIG_INSTANCE, -1, aRecentTombstonesToKeep, null, null);
	}

	/**
	 * Deletes a tombstone.
	 * 
	 * @param aGUID
	 *            The GUID of the tombstone to delete.
	 * @return true only when the tombstone with the specified GUID is found and
	 *         deleted.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public boolean deleteTombstone(String aGUID) throws DIException {

		boolean result = false;

		try {
			PreparedStatement ps = mConnection
					.prepareStatement(" DELETE FROM " + TABLE_NAME + " WHERE " + PROP_FIELD_GUID + " = ?");
			try {
				ps.setString(1, aGUID);

				int recordsDeleted = 0;
				synchronized (mDBLock) {
					recordsDeleted = ps.executeUpdate();
				}
				result = (recordsDeleted > 0);
			} finally {
				silentCloseStatement(ps);
			}

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.DELETING.TOMBSTONE.RECORD.BY.GUID"), e);
		}

		return result;
	}

	// Util methods, used for tombstone operations

	// Private methods, used for tombstone selection
	/**
	 * Constructs a {@link Tombstone} object from {@link ResultSet}
	 * 
	 * @param aResultSet
	 *            {@link ResultSet}
	 * @return Tombstone
	 * @throws DIException
	 */
	private Tombstone constructTombstoneFromResultSet(ResultSet aResultSet) throws DIException {

		Tombstone result = null;

		try {
			Integer componentTypeID = ((Integer) aResultSet.getObject(PROP_FIELD_COMPONENT_TYPE_ID));
			Integer eventTypeID = ((Integer) aResultSet.getObject(PROP_FIELD_EVENT_TYPE_ID));

			Timestamp startTimeTimestamp = (Timestamp) aResultSet.getObject(PROP_FIELD_START_TIME);
			Timestamp createdOnTimestamp = (Timestamp) aResultSet.getObject(PROP_FIELD_CREATED_ON);

			Date startTime = new Date(startTimeTimestamp.getTime());
			Date createdOn = new Date(createdOnTimestamp.getTime());

			String componentName = (String) aResultSet.getObject(PROP_FIELD_COMPONENT_NAME);
			String configuration = (String) aResultSet.getObject(PROP_FIELD_CONFIGURATION);

			Integer exitCode = ((Integer) aResultSet.getObject(PROP_FIELD_EXIT_CODE));
			String errorDescription = (String) aResultSet.getObject(PROP_FIELD_ERROR_DESCR);

			Entry statistics = (Entry) deserializeObject(aResultSet.getObject(PROP_FIELD_STATS));
			String guid = (String) aResultSet.getObject(PROP_FIELD_GUID);
			String userMessage = (String) aResultSet.getObject(PROP_FIELD_USER_MESSAGE);

			Map<String, Object> tombstoneProperties = new HashMap<String, Object>();

			tombstoneProperties.put(Tombstone.PROPERTY_NAME_COMPONENT_TYPE_ID, componentTypeID);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_EVENT_TYPE_ID, eventTypeID);

			tombstoneProperties.put(Tombstone.PROPERTY_NAME_START_TIME, startTime);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_CREATED_ON, createdOn);

			tombstoneProperties.put(Tombstone.PROPERTY_NAME_COMPONENT_NAME, componentName);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_CONFIGURATION, configuration);

			tombstoneProperties.put(Tombstone.PROPERTY_NAME_EXIT_CODE, exitCode);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_ERROR_DESCR, errorDescription);

			tombstoneProperties.put(Tombstone.PROPERTY_NAME_STAT, statistics);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_GUID, guid);
			tombstoneProperties.put(Tombstone.PROPERTY_NAME_USER_MESSAGE, userMessage);

			result = new Tombstone(tombstoneProperties);

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.ERROR.IN.CONSTRUCTING.TOMBSTONE.FROM.DATABASE.RECORD"), e);
		}

		return result;
	}

	/**
	 * Converts array list of Tombstones to an array of Tombstones.
	 * 
	 * @param aArrayList
	 *            array list of Tombstones
	 * @return array of Tombstones
	 */
	private Tombstone[] getTombStonesFromArrayList(List<Tombstone> aArrayList) {

		int count = aArrayList.size();

		Tombstone[] result = new Tombstone[count];

		for (int i = 0; i < count; i++) {
			result[i] = (Tombstone) aArrayList.get(i);
		}

		return result;
	}

	/**
	 * Get all available tombstones for a specified component with timestamps in
	 * the interval specified by aStartTime and aEndTime.
	 * 
	 * @param aComponentName
	 *            The name of the AssemblyLine whose tombstones will be
	 *            extracted.
	 * @param aConfigID
	 *            The component's configuration.
	 * @param aStartTime
	 *            The start time of the period for which tombstones are
	 *            retrieved.
	 * @param aEndTime
	 *            The end time of the period for which tombstones are retrieved.
	 * @param aComponentTypeID
	 *            ID of the component type
	 * @return An array of Tombstone objects for the specified component.
	 * @throws DIException
	 */
	private Tombstone[] getTombstones(String aComponentName, String aConfigID, Date aStartTime, Date aEndTime, int aComponentTypeID)
			throws DIException {

		Tombstone[] result = null;
		String simpleComponentName = null;

		String selectSQL = "SELECT * FROM " + TABLE_NAME + " WHERE (0=0) ";

		boolean isComponentParamSet = !aComponentName.equals("");
		boolean isConfigIDParamSet = !aConfigID.equals("");
		boolean isSelectPeriodSet = (aStartTime != null && aEndTime != null);

		if (aComponentTypeID == COMPONENT_TYPE_ASSEMBLY_LINE && isComponentParamSet && !aComponentName.startsWith("AssemblyLines/")) {
			simpleComponentName = aComponentName;
			aComponentName = "AssemblyLines/" + aComponentName;
		} else if(aComponentName.startsWith("AssemblyLines/")) {
			simpleComponentName = aComponentName.substring("AssemblyLines/".length());
		}

		/*
		 * The next group of code lines are used to create following SQL
		 * statements, required for selection of particular group of tombstone
		 * objects
		 * 
		 * SELECT * FROM IDI_TOMBSTONE WHERE (0=0) AND CREATED_ON BETWEEN ? AND
		 * ?
		 * 
		 * SELECT * FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? SELECT * FROM IDI_TOMBSTONE WHERE (0=0) AND
		 * COMPNENT_TYPE_ID = ? AND COMPONENT_NAME = ? AND CREATED_ON BETWEEN ?
		 * AND ?
		 * 
		 * SELECT * FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? AND CONFIGURATION = ? SELECT * FROM IDI_TOMBSTONE
		 * WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND COMPONENT_NAME = ? AND
		 * CONFIGURATION = ? AND CREATED_ON BETWEEN ? AND ?
		 */

		if (isComponentParamSet) {
			selectSQL += "AND " + PROP_FIELD_COMPONENT_TYPE_ID + " = ? ";
			selectSQL += "AND (" + PROP_FIELD_COMPONENT_NAME + " = ? ";
			selectSQL += "OR " + PROP_FIELD_COMPONENT_NAME + " = ? )";

			if (isConfigIDParamSet) {
				selectSQL += "AND " + PROP_FIELD_CONFIGURATION + " = ? ";
			}
		}

		if (isSelectPeriodSet) {
			selectSQL += "AND " + PROP_FIELD_CREATED_ON + " BETWEEN ? AND ?";
		}

		PreparedStatement ps = null;
		try {
			// Create prepared statement, based on constructed SQL and set
			// proper group of parameters to
			// this statement
			ps = mConnection.prepareStatement(selectSQL);

			int index = 1;

			// Set parameters of the properly constructed prepared statement
			if (isComponentParamSet) {

				ps.setInt(1, aComponentTypeID);
				ps.setString(2, aComponentName);
				ps.setString(3, simpleComponentName);
				index += 3; // i.e. if clause for "CREATED_ON BETWEEN ? AND ?"
				// exists, its arguments are placed as third and
				// forth

				if (isConfigIDParamSet) {
					ps.setString(index, aConfigID);
					index++; // i.e. if clause for "CREATED_ON BETWEEN ? AND
					// ?" exists, its arguments are placed as forth
					// and fifth
				}
			}

			if (isSelectPeriodSet) {
				Timestamp startTime = new Timestamp(aStartTime.getTime());
				Timestamp endTime = new Timestamp(aEndTime.getTime());

				ps.setTimestamp(index, startTime);
				ps.setTimestamp((index + 1), endTime);
			}

			ArrayList<Tombstone> resultArrayList = null;
			ResultSet rs = null;
			synchronized (mDBLock) {
				rs = ps.executeQuery();
				resultArrayList = new ArrayList<Tombstone>();
				try {
					while (rs.next()) {
						Tombstone temp = constructTombstoneFromResultSet(rs);
						resultArrayList.add(temp);
					}
				} finally {
					silentCloseResultSet(rs);
				}
			}

			result = getTombStonesFromArrayList(resultArrayList);

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS"), e);
		} finally {
			silentCloseStatement(ps);
		}

		return result;
	}

	// Private methods, used for tombstone deletion

	/**
	 * Creates a {@link Timestamp} object containing the difference between the
	 * day specified and current day
	 * 
	 * @param aDaysCount
	 *            number of days
	 * @return Timestamp object
	 */

	private Timestamp getMinAllowedTimestamp(int aDaysCount) {
		Date currentDate = new Date();

		long currentDateTime = currentDate.getTime();
		long oneDayInMilisecs = 86400000; // 24 hours * 60 min * 60 sec * 1000

		long minAllowedTime = currentDateTime - (aDaysCount * oneDayInMilisecs);

		return new Timestamp(minAllowedTime);
	}

	/**
	 * Retrieves the ID of the last record
	 * 
	 * @param aConfigID
	 *            configuration name
	 * @param aComponentName
	 *            name of the component
	 * @param aComponentTypeID
	 *            component type ID
	 * @param aRecentTombstonesToKeep
	 *            number of Tombstones to keep
	 * @return ID of the column in the DB or '-1' if nothing found
	 * @throws DIException
	 */
	private int getLastRecordToKeepID(String aConfigID, String aComponentName, int aComponentTypeID, int aRecentTombstonesToKeep)
			throws DIException {

		int result = -1;

		String selectSQL = "SELECT " + PROP_FIELD_ID + " FROM " + TABLE_NAME + " WHERE (0=0) ";
		String suffixSQL = " ORDER BY " + PROP_FIELD_ID + " DESC";

		boolean isComponentParamSet = !aComponentName.equals("");
		boolean isConfigIDParamSet = !aConfigID.equals("");

		/*
		 * The next group of code lines is used to create SQL statements,
		 * required for selection of particular group of tombstone objects
		 * 
		 * SELECT FROM IDI_TOMBSTONE WHERE (0=0) ORDER BY ID DESC
		 * 
		 * SELECT FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? ORDER BY ID DESC SELECT FROM IDI_TOMBSTONE WHERE
		 * (0=0) AND COMPNENT_TYPE_ID = ? AND COMPONENT_NAME = ? AND
		 * CONFIGURATION = ? ORDER BY ID DESC
		 */

		if (isComponentParamSet) {
			selectSQL += "AND " + PROP_FIELD_COMPONENT_TYPE_ID + " = ? ";
			selectSQL += "AND " + PROP_FIELD_COMPONENT_NAME + " = ? ";

			if (isConfigIDParamSet) {
				selectSQL += "AND " + PROP_FIELD_CONFIGURATION + " = ? ";
			}
		}

		selectSQL += suffixSQL;

		try {
			// Create prepared statement, based on constructed SQL and set
			// proper group of parameters to
			// this statement
			PreparedStatement ps = mConnection.prepareStatement(selectSQL);
			try {
				// Set parameters of the properly constructed prepared statement
				if (isComponentParamSet) {
					ps.setInt(1, aComponentTypeID);
					ps.setString(2, aComponentName);

					if (isConfigIDParamSet) {
						ps.setString(3, aConfigID);
					}
				}
				result = getIntValueFromDatabaseQueryResults(ps, aRecentTombstonesToKeep - 1, PROP_FIELD_ID);
			} finally {
				silentCloseStatement(ps);
			}

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.RETRIEVING.LAST.RECORD.TO.KEEP"), e);
		}

		return result;
	}

	/**
	 * Get individual integer value from database query results.
	 * 
	 * @param ps
	 *            Database query statement.
	 * @param recordIndex
	 *            Index of the record, which contains the value of interest.
	 * @param columnName
	 *            The name of the table column, which contains the value of
	 *            interest.
	 * @return The integer value.
	 * @throws SQLException
	 *             Database error.
	 */
	private int getIntValueFromDatabaseQueryResults(PreparedStatement ps, int recordIndex, String columnName) throws SQLException {
		int result = -1;

		synchronized (mDBLock) {
			ResultSet rs = ps.executeQuery();
			try {
				int recordCounter = 0;
				while (rs.next()) {
					if (recordCounter == recordIndex) {
						result = ((Integer) rs.getObject(columnName)).intValue();
						break;
					}
					recordCounter++;
				}
			} finally {
				silentCloseResultSet(rs);
			}
		}

		return result;
	}

	/**
	 * Deletes all Tombstone objects for a specified AssemblyLine that are older
	 * than the specified date.
	 * 
	 * @param aComponentName
	 *            The name of the component.
	 * @param aConfigID
	 *            The component's configuration.
	 * @param aComponentTypeID
	 *            ID of the component type
	 * @param aDaysToKeepRecords
	 *            number of days , the records should be kept
	 * @param aNumbOfTombstonesToKeep
	 *            the count of the TombStones to be kept
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return The number of deleted tombstone records.
	 * @throws DIException
	 *             if an error occurs.
	 */
	private int deleteTombstones(String aConfigID, String aComponentName, int aComponentTypeID, int aDaysToKeepRecords,
			int aNumbOfTombstonesToKeep, Date startDate, Date endDate) throws DIException {

		int result = 0;

		String deleteSQL = "DELETE FROM " + TABLE_NAME + " WHERE (0=0) ";

		boolean isComponentParamSet = !aComponentName.equals("");
		boolean isConfigIDParamSet = !aConfigID.equals("");
		boolean isDaysParamSet = aDaysToKeepRecords >= 0;
		boolean isNumbOfTombstonesParamSet = aNumbOfTombstonesToKeep > 0;
		boolean isStartDateParamSet = startDate != null;
		boolean isEndDateParamSet = endDate != null;

		/*
		 * The next group of code lines is used to create SQL statements,
		 * required for deletion of particular group of tombstone objects:
		 * 
		 * DELETE FROM IDI_TOMBSTONE WHERE (0=0) // in case
		 * aNumbOfTombstonesToKeep equals 0
		 * 
		 * DELETE FROM IDI_TOMBSTONE WHERE (0=0) AND ID < ? DELETE FROM
		 * IDI_TOMBSTONE WHERE (0=0) AND CREATED_ON <= ?
		 * 
		 * DELETE FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? DELETE FROM IDI_TOMBSTONE WHERE (0=0) AND
		 * COMPNENT_TYPE_ID = ? AND COMPONENT_NAME = ? AND CONFIGURATION = ?
		 * 
		 * DELETE FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? AND ID < ? DELETE FROM IDI_TOMBSTONE WHERE (0=0)
		 * AND COMPNENT_TYPE_ID = ? AND COMPONENT_NAME = ? AND CREATED_ON <= ?
		 * 
		 * DELETE FROM IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND
		 * COMPONENT_NAME = ? AND CONFIGURATION = ? AND ID < ? DELETE FROM
		 * IDI_TOMBSTONE WHERE (0=0) AND COMPNENT_TYPE_ID = ? AND COMPONENT_NAME
		 * = ? AND CONFIGURATION = ? AND CREATED_ON <= ?
		 */

		Timestamp endDateAllowed = null;
		Timestamp startDateAllowed = null;
		int lastRecordToKeepID = -1;

		if (isComponentParamSet) {
			deleteSQL += "AND " + PROP_FIELD_COMPONENT_TYPE_ID + " = ? ";
			deleteSQL += "AND " + PROP_FIELD_COMPONENT_NAME + " = ? ";

			if (isConfigIDParamSet) {
				deleteSQL += "AND " + PROP_FIELD_CONFIGURATION + " = ? ";
			}
		}

		if (isStartDateParamSet && isEndDateParamSet) {
			deleteSQL += "AND " + PROP_FIELD_CREATED_ON + " BETWEEN ? AND ?";
			long starttime = startDate.getTime();
			startDateAllowed = new Timestamp(starttime);

			long endtime = endDate.getTime();
			endDateAllowed = new Timestamp(endtime);
		} else if (isDaysParamSet || isEndDateParamSet) {
			deleteSQL += "AND " + PROP_FIELD_CREATED_ON + " <= ?";
			if (aDaysToKeepRecords != -1)
				endDateAllowed = getMinAllowedTimestamp(aDaysToKeepRecords);
			else {
				long time = endDate.getTime();
				endDateAllowed = new Timestamp(time);
			}
		}

		synchronized (mDBLock) {
			if (isNumbOfTombstonesParamSet) {
				deleteSQL += "AND " + PROP_FIELD_ID + " < ?";
				lastRecordToKeepID = getLastRecordToKeepID(aConfigID, aComponentName, aComponentTypeID, aNumbOfTombstonesToKeep);
			}

			try {
				// Create prepared statement, based on constructed SQL and set
				// proper group of parameters to
				// this statement
				PreparedStatement ps = mConnection.prepareStatement(deleteSQL);

				// Next variable set index for clauses "ID < ?" and "CREEATED_ON
				// <= ?"; it is used
				// as first parameter in methods for parameters setting of
				// prepared statement
				int index = 1;

				// Set parameters of the properly constructed prepared statement
				if (isComponentParamSet) {
					ps.setInt(1, aComponentTypeID);
					ps.setString(2, aComponentName);
					index += 2; // i.e. if clauses for "ID < ?" and "CREEATED_ON
					// <= ?" exist, their arguments are placed as
					// third argument

					if (isConfigIDParamSet) {
						ps.setString(3, aConfigID);
						index++; // i.e. if clauses for "ID < ?" and
						// "CREEATED_ON <= ?" exist, their arguments
						// are placed as forth argument
					}
				}
				if (isStartDateParamSet && isEndDateParamSet) {
					ps.setTimestamp(index, startDateAllowed);
					index++;
					ps.setTimestamp(index, endDateAllowed);
				} else if (isDaysParamSet || isEndDateParamSet) {
					ps.setTimestamp(index, endDateAllowed);
				}

				if (isNumbOfTombstonesParamSet) {
					ps.setInt(index, lastRecordToKeepID);
				}

				result = ps.executeUpdate();

				ps.close();

			} catch (Exception e) {
				APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.DELETING.TOMBSTONE.RECORDS"), e);
			}
		} // end synchronized

		return result;
	}

	/**
	 * Deserialize a byte array into a Java object.
	 * 
	 * @param aSerializedObject
	 *            byte array, containing serialized Java object
	 * @return deserialized object value
	 * @throws DIException
	 *             if an error occurs.
	 */
	private Object deserializeObject(Object aSerializedObject) throws DIException {

		Object result = null;

		try {
			byte[] array = null;

			if (aSerializedObject instanceof byte[]) {
				array = (byte[]) aSerializedObject;
			} else if (aSerializedObject instanceof Blob) {
				array = ((Blob) aSerializedObject).getBytes(0L, (int) ((Blob) aSerializedObject).length() + 1);
			} else {
				return null;
			}

			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(array));
			result = ois.readObject();
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.OBJECT.DESERIALIZATION"), e);
		}

		return result;
	}

	/**
	 * Sets the JDBC connection.
	 * 
	 * @param aConnection
	 *            the connection to use.
	 */
	public void setConnection(Connection aConnection) {
		mConnection = aConnection;
	}

	/**
	 * Gets the JDBC connection.
	 * 
	 * @return the connection to the database.
	 */
	public Connection getConnection() {
		return mConnection;
	}

	/**
	 * @return the number of Tombstones in the back-end store.
	 * @throws DIException
	 *             if an error occurs.
	 */
	public int getTombstonesCount() throws DIException {

		int count = -1;
		try {

			PreparedStatement countStatement = mConnection.prepareStatement("SELECT COUNT(" + TombstoneManager.PROP_FIELD_ID
					+ ") FROM " + TombstoneManager.TABLE_NAME);

			ResultSet rsc = null;
			synchronized (mDBLock) {
				rsc = countStatement.executeQuery();
				if (rsc.next()) {
					count = ((Integer) rsc.getObject(1)).intValue();
					if (APIEngine.isDebugEnabled()) {
						APIEngine.logDebug(sResHash.getString("SEVER.API.TOMBSTONES.RECORD.COUNT", String.valueOf(count)));
					}
				}
				rsc.close();
			}
			countStatement.close();

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.IN.COUNT.TOMBSTONE.RECORDS"), e);
		}

		return count;
	}

	/**
	 * 
	 * Inserts a tombstone record in the back-end database.
	 * 
	 * @param aComponentTypeID
	 *            the type of the component.
	 * @param aEventTypeID
	 *            the type of the event.
	 * @param aStartTime
	 *            the time the component was started.
	 * @param aCreatedOn
	 *            the time the component was created.
	 * @param aComponentName
	 *            the name of the component.
	 * @param aConfiguration
	 *            the configuration id of the configInstance the AL was started
	 *            in.
	 * @param aExitCode
	 *            the code the component exited with.
	 * @param aErrorDescr
	 *            the description of the error (if any) the component ended
	 *            with.
	 * @param aGUID
	 *            the globally unique identifier of the tombstone.
	 * @param aStats
	 *            the statistics as a serialized Entry object.
	 * @param aUserMessage
	 *            the user message.
	 * @throws DIException
	 *             if an error occurs.
	 */
	protected void doInsert(int aComponentTypeID, int aEventTypeID, long aStartTime, long aCreatedOn, String aComponentName,
			String aConfiguration, int aExitCode, String aErrorDescr, String aGUID, byte[] aStats, String aUserMessage)
			throws DIException {

		if (updatePS == null) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.INSERT.OPERATION.OF.TOMBSTONE.RECORD.IN.DATABASE.FAILED"));
		}

		try {
			synchronized (mDBLock) {
				updatePS.setInt(1, aComponentTypeID);
				updatePS.setInt(2, aEventTypeID);

				updatePS.setTimestamp(3, new Timestamp(aStartTime));
				updatePS.setTimestamp(4, new Timestamp(aCreatedOn));

				updatePS.setString(5, aComponentName);
				updatePS.setString(6, aConfiguration);

				updatePS.setInt(7, aExitCode);
				updatePS.setString(8, aErrorDescr);

				updatePS.setBytes(9, aStats);

				updatePS.setString(10, aGUID);
				updatePS.setString(11, aUserMessage);

				updatePS.executeUpdate();
			}

		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash
					.getString("SEVER.API.INSERT.OPERATION.OF.TOMBSTONE.RECORD.IN.DATABASE.FAILED"), e);
		}
	}

	/**
	 * Close the prepared statement.
	 */
	@Override
	protected void finalize() throws Throwable {

		try {
			if (updatePS != null) {
				updatePS.close();
			}
			if (selectCI != null) {
				selectCI.close();
			}
			if (checkCI != null) {
				checkCI.close();
			}
			if (selectAL != null) {
				selectAL.close();
			}
			if (checkAL != null) {
				checkAL.close();
			}
		} finally {
			super.finalize();
		}
	}

	/**
	 * Close a statement without throwing an exception.
	 * 
	 * @param s
	 *            JDBC statement.
	 */
	private void silentCloseStatement(Statement s) {
		if (s != null) {
			try {
				s.close();
			} catch (Exception ex) {
				APIEngine.logDebug(ex.getMessage());
			}
		}
	}

	/**
	 * Close a result set without throwing an exception.
	 * 
	 * @param s
	 *            JDBC result set.
	 */
	private void silentCloseResultSet(ResultSet s) {
		if (s != null) {
			try {
				s.close();
			} catch (Exception ex) {
				APIEngine.logDebug(ex.getMessage());
			}
		}
	}

	public List<String> getConfigInstanceIDs() throws DIException {
		List<String> result = null;
		ResultSet rs = null;
		try {
			synchronized (mDBLock) {
				rs = selectCI.executeQuery();
			}
			result = new LinkedList<String>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS.2"), e);
		} finally {
			silentCloseResultSet(rs);
		}
		return result;
	}

	public boolean hasTombstones(String configInstanceId) throws DIException {
		ResultSet rs = null;
		try {
			synchronized (mDBLock) {
				checkCI.setString(1, configInstanceId);
				rs = checkCI.executeQuery();
			}

			return rs.next() && ((Integer) rs.getObject(1)).intValue() > 0;
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS.2"), e);
		} finally {
			silentCloseResultSet(rs);
		}

		return false;
	}

	public List<String> getAssemblyLineNames(String configInstanceId) throws DIException {
		if (configInstanceId == null) {
			throw new IllegalArgumentException();
		}

		ResultSet rs = null;
		List<String> result = null;
		try {
			synchronized (mDBLock) {
				selectAL.setString(1, configInstanceId);
				rs = selectAL.executeQuery();
			}
			result = new LinkedList<String>();
			while (rs.next()) {
				String alName = rs.getString(1);
				if (alName.startsWith("AssemblyLines/")) {
					alName = alName.substring(14);
				}
				result.add(alName);
			}
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS.2"), e);
		} finally {
			silentCloseResultSet(rs);
		}
		return result;
	}

	public boolean hasTombstones(String configInstanceId, String alName) throws DIException {
		ResultSet rs = null;
		try {
			if (!alName.startsWith("AssemblyLines/")) {
				alName = "AssemblyLines/" + alName;
			}

			synchronized (mDBLock) {
				checkAL.setString(1, configInstanceId);
				checkAL.setString(2, alName);
				rs = checkAL.executeQuery();
			}

			return rs.next() && ((Integer) rs.getObject(1)).intValue() > 0;
		} catch (Exception e) {
			APIEngine.logErrorAndThrowException(sResHash.getString("SEVER.API.ERROR.ON.RETRIEVING.TOMBSTONE.RECORDS.2"), e);
		} finally {
			silentCloseResultSet(rs);
		}

		return false;
	}
}
