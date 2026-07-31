/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.naming.Name;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.DeltaEntry;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.AssemblyLine;
import com.ibm.di.server.AssemblyLineComponent;
import com.ibm.di.server.CSDeltaTaskComponent;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.TaskStatistics;
import com.ibm.di.store.DeltaStore;
import com.ibm.di.store.DeltaSysTable;
import com.ibm.di.store.StoreFactory;
import com.ibm.icu.util.StringTokenizer;

/**
 * The Delta Function Component encapsulates the Delta functionality for
 * tracking changes in an input source using underlying database for comparison.
 * <p>
 * The main logic of this component is reused by the
 * {@link CSDeltaTaskComponent} to maintain the Delta tab functionality for
 * connectors in Iterator mode.
 * 
 * @since TDI 7.1
 */
public class DeltaFC extends Function {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "deltafc";

	/**
	 * A delta store.
	 */
	private DeltaStore delta;

	/**
	 * A key attribute.
	 */
	private String keyAttribute;

	/**
	 * A collection of key attributes.
	 */
	private List<String> keyAttributes;

	/**
	 * A collection of ignored attributes.
	 */
	private List<String> attributeList;

	/**
	 * Separator used between multiple attributes whose changes will be ignored
	 * during compute changes process.
	 */
	private String attributeSep = ",";

	/**
	 * If this is <code>true</code> changes in attributes specified in
	 * "Attribute List" parameter are not ignored. Instead changes in all other
	 * attributes are ignored.
	 */
	private boolean isInvertedIgnore = false;

	/**
	 * Whether to iterate deleted.
	 */
	private boolean iterateDeleted = false;

	/**
	 * Whether to remove deleted.
	 */
	private boolean removeDeleted = false;

	/**
	 * Whether to return the entry unchanged.
	 */
	private boolean returnUnchanged = false;

	/**
	 * Determines whether deleted entries are iterated at the moment.
	 */
	private boolean readDeleted = false;

	/**
	 * Determines the comparison depth when computing changes. Note used.
	 * Consider removing.
	 */
	private int deltaLevel = 3;

	/**
	 * A set of the keys processed. These keys are constructed from each entry
	 * and are unique.
	 */
	private HashSet<String> processedKeys = null;

	/**
	 * Whether to use fast algorithm or not.
	 */
	private boolean fastAlgorithm = true;

	/**
	 * This is the statistics object for the component
	 */
	public TaskStatistics stats = null;

	/**
	 * If true duplicate delta keys are allowed in delta store.
	 */
	private boolean allowDuplicateDeltaKeys = false;

	/**
	 * Valid Delta Table Identifier.
	 */
	private String dbPath = null;

	/**
	 * Separator used between multiple keys.
	 */
	private String multiKeySep = "+";

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * True when using Delta FC logic from DeltaEngine.
	 */
	private boolean isFromIteratorDelta = false;

	/**
	 * Level of transaction isolation for the connection to the Delta Store.
	 */
	private int rowLocking;

	public final static String PARAM_READ_UNCOMMITTED = "READ_UNCOMMITTED";

	public final static String PARAM_READ_COMMITTED = "READ_COMMITTED";

	public final static String PARAM_REPEATABLE_READ = "REPEATABLE_READ";

	public final static String PARAM_SERIALIZABLE = "SERIALIZABLE";

	/**
	 * Parameter name. When selected changes in attributes listed in
	 * "Attribute List" parameter will be ignored.
	 */
	public final static String PARAM_IGNORE_ATTRIBUTES = "IGNORE_ATTRIBUTES";

	/**
	 * Parameter name. When selected only changes in attributes listed in
	 * "Attribute List" parameter will be detected.
	 */
	public final static String PARAM_DETECT_ATTRIBUTES = "DETECT_ATTRIBUTES";

	/**
	 * Parameter name. When selected change sin all attributes will be selected
	 * and "Attribute List" parameter will be disabled.
	 */
	public final static String PARAM_DETECT_ALL = "DETECT_ALL";

	/**
	 * Used by the script 'deleteDelta' in the tdi.xml for this component.
	 * 
	 * @return The resource object.
	 * 
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * Called once to initialize the Delta Function Component.
	 * 
	 * @param obj
	 *            ignored
	 * @throws Exception
	 *             if an error occurs
	 */
	public void initialize(Object obj) throws Exception {
		iterateDeleted = getBooleanParam(InternalSchema.CONNECTOR_DELTA_ITER_DELETED);
		removeDeleted = getBooleanParam(InternalSchema.CONNECTOR_DELTA_REMOVE_DELETED);
		returnUnchanged = getBooleanParam(InternalSchema.CONNECTOR_DELTA_RETURN_UNCHANGED);
		allowDuplicateDeltaKeys = getBooleanParam(InternalSchema.CONNECTOR_DELTA_ALLOW_DUPLICATE_KEYS);
		fastAlgorithm = getBooleanParam(InternalSchema.CONNECTOR_DELTA_FAST_ALGORITHM);
		dbPath = getStringParam(InternalSchema.CONNECTOR_DELTA_DB);
		keyAttribute = getStringParam(InternalSchema.CONNECTOR_DELTA_UNIQUE_ATTR);
		rowLocking = getRowLocking();
		setKeyAttributes();
		setChangeDetectionMode();

		// This parameter is for internal use only! It is set to true only when
		// CSDeltaTaskComponent is using the DeltaFC.
		isFromIteratorDelta = getBooleanParam("iteratorDelta");

		// Clear it just to be sure.
		setParam("iteratorDelta", null);

		String str = getStringParam(InternalSchema.CONNECTOR_DELTA_LEVEL);
		if (str != null) {
			try {
				deltaLevel = Integer.parseInt(str);
			} catch (NumberFormatException ignore) {
				deltaLevel = 3;
			}
		}

		if (fastAlgorithm && iterateDeleted) {
			processedKeys = new HashSet<String>();
		}

		super.initialize(null);
	}

	/**
	 * This method reads the 'Row Locking' parameter from the configuration and
	 * sets the proper transaction isolation level to be used. Using higher
	 * level ensures that transaction anomalies will be reduced using row and
	 * table locks.
	 * 
	 * @return integer value of the level as defined in {@link Connection}
	 *         interface
	 */
	private int getRowLocking() {
		String param = getStringParam(InternalSchema.CONNECTOR_DELTA_ROW_LOCKING);

		// If transaction level is not specified in the configuration use -1.
		// This will ensure that for older than 7.1 configurations we won't set
		// the transaction level to our default value.
		int level = -1;

		if (param == null) {
			return level;
		}

		if (param.equalsIgnoreCase(PARAM_READ_UNCOMMITTED)) {
			level = Connection.TRANSACTION_READ_UNCOMMITTED;
		} else if (param.equalsIgnoreCase(PARAM_READ_COMMITTED)) {
			level = Connection.TRANSACTION_READ_COMMITTED;
		} else if (param.equalsIgnoreCase(PARAM_REPEATABLE_READ)) {
			level = Connection.TRANSACTION_REPEATABLE_READ;
		} else if (param.equalsIgnoreCase(PARAM_SERIALIZABLE)) {
			level = Connection.TRANSACTION_SERIALIZABLE;
		}

		return level;
	}

	private void initDelta() throws Exception {
		
		// Pass Log object so the Delta Store could log.
		// Let the Delta Store knows if we are going to delete entries.
		delta = DeltaSysTable.getDeltaStore(dbPath, false, logger, removeDeleted);

		if (delta != null) {
			delta.setAllowDuplicateDeltaKeys(allowDuplicateDeltaKeys);
			delta.setCommitMode(getStringParam(InternalSchema.CONNECTOR_DELTA_WHEN_TO_COMMIT));

			// Set the transaction level only if specified in the configuration.
			if (rowLocking != -1) {
				delta.setRowLocking(rowLocking);
			}

			logdebug(sResHash.getString("DELTAFC.USING.DERBY.DELTA", new Object[] { dbPath, StoreFactory.getSystemDatabaseURL() }));
			logdebug(sResHash.getString("DELTAFC.USING.KEY", keyAttribute + "@" + dbPath));
		}
	}

	/**
	 * If 'Attribute List' parameter contains more that one attribute names
	 * concatenated using comma initialize the attributeList variable.
	 * 
	 * @throws Exception
	 */
	private void setAttributeList() {
		String attrListParam = getStringParam(InternalSchema.CONNECTOR_DELTA_ATTRIBUTE_LIST);

		if (attrListParam != null && !attrListParam.equals("")) {
			if (attrListParam.indexOf(attributeSep) != -1) {
				attributeList = splitString(attrListParam, attributeSep, true);
			} else {
				attributeList = new ArrayList<String>();
				attributeList.add(attrListParam);
			}
		}
	}

	/**
	 * Check the 'Change Detection Mode' parameter and set the proper boolean
	 * variables: <code>isInvertedIgnore</code> and
	 * <code>ignoreAttributeList</code>.
	 * <p>
	 * Also if 'Change Detection Mode' is not set to 'Use all Attributes for
	 * change detection' the <code>attributeList</code> variable is initialized.
	 * 
	 * @throws Exception
	 */
	private void setChangeDetectionMode() {
		String param = getStringParam(InternalSchema.CONNECTOR_DELTA_CHANGE_DETECTION_MODE);
		boolean initAttributeList = true;
		
		if (param == null) {
			return;
		}

		if (param.equalsIgnoreCase(PARAM_IGNORE_ATTRIBUTES)) {
			isInvertedIgnore = false;
		} else if (param.equalsIgnoreCase(PARAM_DETECT_ATTRIBUTES)) {
			isInvertedIgnore = true;
		} else if (param.equalsIgnoreCase(PARAM_DETECT_ALL)) {
			isInvertedIgnore = true;
			initAttributeList = false;
		}

		if (initAttributeList) {
			setAttributeList();
		}
	}

	/**
	 * Validates keyAttribute's value. If it contains more that one attributes
	 * concatenated using '+' symbol initialize keyAttributes variable too.
	 * 
	 * @throws Exception
	 */
	private void setKeyAttributes() throws Exception {
		if (keyAttribute == null || keyAttribute.equals(""))
			throw new Exception(sResHash.getString("DELTAFC.KEY.ATTRIB.NOTDEFINED"));

		if (keyAttribute.indexOf(multiKeySep) != -1) {
			keyAttributes = splitString(keyAttribute, multiKeySep, false);
		} else {
			keyAttributes = new ArrayList<String>();
			keyAttributes.add(keyAttribute);
		}
	}

	/**
	 * This method splits string into tokens.
	 * 
	 * @param str
	 *            string to be splitted
	 * @param sep
	 *            separator for the splitting
	 * @param trimTokens
	 *            if <code>true</code> trim tokens before adding them to the
	 *            ArrayList object
	 * @return ArrayList containing the tokens
	 */
	private ArrayList<String> splitString(String str, String sep, boolean trimTokens) {
		StringTokenizer st = new StringTokenizer(str, sep);
		ArrayList<String> al = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			al.add(token);
		}

		return al;
	}

	/**
	 * @return a valid delta store table identifier if such is not provided.
	 */
	private String getValidDBPath() {
		if (getContext() != null) {
			String parentName = ((AssemblyLine) getContext()).getName();
			Name compName = ((FunctionConfig) getConfiguration().getParent()).getName();

			// CREATE TABLE statement fails if parentName
			// contains '/' so replace them with '_'
			StringBuilder sb = new StringBuilder(parentName.replace('/', '_'));

			if (compName != null) {
				sb.append("_" + compName.toString());
			}

			return sb.toString();
		}
		return null;
	}

	/**
	 * This method initializes the DeltaStore and TaskStatistics instances if
	 * they are not already initialized.
	 * 
	 * @throws Exception
	 */
	private void checkInitialized() throws Exception {
		// Initialize delta and stats here because getContext()
		// returns null if initialize() is not finished
		if (delta == null) {
			// In case the Delta DB is not specified used the
			// 'AssemblyLines_<AL name>_<comp name> as DB identifier
			if (dbPath == null || dbPath.length() < 1) {
				dbPath = getValidDBPath();
			}
			initDelta();
		}

		if (stats == null) {
			AssemblyLine al = (AssemblyLine) getContext();
			Name name = ((FunctionConfig) (getConfiguration().getParent())).getName();

			if (al != null && name != null) {
				AssemblyLineComponent alc = al.getConnector(name.toString());
				stats = alc.stats;
			}
		}
	}

	/**
	 * This method accepts an object of type Entry and computes Delta changes
	 * based on the used underlying Delta Store table.
	 * 
	 * @param obj
	 *            Entry object
	 * @return Returns Delta tagged Entry
	 * @throws Exception
	 *             if the parameter is not an instance of the {@link Entry}
	 *             class, if the FC has not been initialized or if the method
	 *             fails
	 */
	public Object perform(Object obj) throws Exception {

		checkInitialized();

		Entry work = new Entry();

		if (obj != null) {
			if (obj instanceof Entry) {
				work = (Entry) obj;
			} else {
				String errorMessage = sResHash.getString("DELTAFC.INVALID.PERFORM.PARAMETER.TYPE");
				logerror(errorMessage);
				throw new Exception(errorMessage);
			}
		}

		// If we are iterating the deleted entries
		if (readDeleted) {
			return nextDeletedEntry(work);
		}

		/*
		 * DeltaEngine: Receiving empty Entry throws Exception and null starts
		 * iteration over deleted entries only if iterateDelted is true else
		 * returns null
		 * 
		 * Delta FC: Receiving null or empty Entry starts iteration over deleted
		 * entries only if iterateDelted is true else returns empty entry
		 */
		if (obj == null || !((work.size() != 0) || isFromIteratorDelta)) {
			if (iterateDeleted) {
				return nextDeletedEntry(work);
			}

			if (isFromIteratorDelta) { // DeltaEngine
				return null;
			} else { // DeltaFC
				return work;
			}
		}

		// Make sure we have the unique key attribute
		String key = getKey(work);

		if (!allowDuplicateDeltaKeys && processedKeys != null && processedKeys.contains(key)) {
			throw new Exception(sResHash.getString("DELTAFC.DUPLICATE.DELTA.KEY", key));
		}

		// Find old entry (throws exception on previously processed entry)
		byte[] oldEntryBytes = delta.findEntryBytesVerify(key);

		// If not found then this is a new entry
		if (oldEntryBytes == null) {
			logdebug(sResHash.getString("DELTAFC.NEW.ENTRY.KEY", key));
			delta.insertEntry(key, work);
			work.setOp(Entry.OP_ADD);
			stats.add();
			return work;
		}
		
		byte[] newEntryBytes = StoreFactory.serializeObject(work);
		work.setOp(Entry.OP_UNCHANGED);

		if (!Arrays.equals(newEntryBytes, oldEntryBytes)) {
			
			Entry old = (Entry) StoreFactory.deserializeObjectFromBytes(oldEntryBytes);

			// Compute delta changes
			Entry deltaEntry = DeltaEntry.compareEntries(work, old, true, deltaLevel, null, attributeList, isInvertedIgnore);
			
			if (deltaEntry.getOp() == Entry.OP_MOD) {
				stats.mod();

				logdebug(sResHash.getString("DELTAFC.MODIFIED.ENTRY.KEY", key));
				work.setOp(Entry.OP_MOD);

				// Store new entry to delta
				delta.updateEntryBytes(key, newEntryBytes);
				if (deltaLevel > 1) {
					deltaEntry.setProperty("delta.old", old);
					return deltaEntry;
				} else {
					work.setProperty("delta.old", old);
					return work;
				}
			}
		}

		if (!fastAlgorithm) {
			// Update sequence number
			delta.updateSequence(key);
		} else if (iterateDeleted) {
			processedKeys.add(key);
		}

		stats.nochange();

		logdebug(sResHash.getString("DELTAFC.NO.CHANGES.KEY", key));

		if (returnUnchanged) {
			return work;
		} else if (isFromIteratorDelta) {
			// Default behavior of the Delta engine is to skip entry when
			// returnUnchanged is false.
			throw new com.ibm.di.exceptions.SkipEntryException(sResHash.getString("DELTAFC.SKIPPING.UNCHANGED.ENTRY"));
		} else {
			// FC can not return null so return an empty 'conn'
			// when returnUnchanged is false
			return new Entry();
		}
	}

	/**
	 * Return the next deleted entry.
	 * 
	 * @param work
	 *            the work entry to fill in
	 * @return the work entry filled with the next deleted entry
	 * @throws Exception
	 *             if a problem occurs
	 */
	public Entry nextDeletedEntry(Entry work) throws Exception {
		if (!readDeleted) {
			logdebug(sResHash.getString("DELTAFC.BEGIN.ITERATION.OF.DELETED.ENTRIES"));
			readDeleted = true;
			delta.selectDeletedEntries();
		} else {
			logdebug(sResHash.getString("DELTAFC.GET.NEXT.DELETED.ENTRY"));
		}

		Entry e = delta.getNextDeletedEntry(removeDeleted, processedKeys);

		if (e == null) {
			if (isFromIteratorDelta) { // DeltaEngine
				return null;
			} else { // DeltaFC
				readDeleted = false;
				return work;
			}
		}

		if (removeDeleted) {
			logdebug(sResHash.getString("DELTAFC.REMOVE.DELETED.ENTRY", getKey(e)));
			stats.del();
		}

		work.merge(e);
		work.setOp(Entry.OP_DEL);

		return work;
	}

	/**
	 * Returns the unique key constructed from this Entry. Uses the key
	 * Attributes specified.
	 * 
	 * @param work
	 *            The Entry returned from the underlying Connector
	 * @return The unique key
	 * @throws Exception
	 *             If one of the key attributes have more than one value, or we
	 *             don't find any values at all.
	 */
	private String getKey(Entry work) throws Exception {
		StringBuffer key = new StringBuffer();
		for (String ka : keyAttributes) {
			Attribute keyAttr = work.getAttribute(ka);
			if (keyAttr == null)
				continue;
			else if (keyAttr.size() > 1)
				throw new Exception(sResHash.getString("DELTAFC.ENTRY.WITH.MANY.VALUES.FOR.KEY.ATTRIBUTE", new Object[] { ka,
						Integer.valueOf(keyAttr.size()) }));

			String kv = keyAttr.getValue();
			if (kv != null && kv.trim().length() > 0)
				key.append(kv);
		}

		// Verify we have a value for the key
		if (key.length() == 0)
			throw new Exception(sResHash.getString("DELTAFC.ENTRY.WITHOUT.VALUE.FOR.KEY.ATTRIBUTE", keyAttribute));

		return key.toString();
	}

	/**
	 * Marks an Entry in the Delta Store. This can be useful if the current
	 * change can not be propagated properly, and you want to roll back the
	 * delta state. You can then use code like this, assuming this Component is
	 * called MyIterator:
	 * 
	 * <pre>
	 * MyComponent.rollbackDeltaState();
	 * MyComponent.markEntryInDeltaStore(work);
	 * MyComponent.commitDeltaState();
	 * </pre>
	 * 
	 * @param work
	 *            The Entry that contains the key information
	 * @return <code>true</code> if the Entry contained a meaningful key and
	 *         could be marked in the Delta Store
	 */
	public boolean markEntryInDeltaStore(Entry work) {
		if (work == null)
			return false;

		try {
			checkInitialized();

			String key = getKey(work);

			if (!fastAlgorithm) {
				delta.updateSequence(key);
			} else if (iterateDeleted) {
				processedKeys.add(key);
			} else {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param param
	 *            name of the parameter , String.
	 * @return the value of the parameter.
	 * @throws Exception
	 *             : never
	 */
	private boolean getBooleanParam(String param) {
		String str = getStringParam(param);
		if (str != null && str.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieves a value, specified by the user.
	 * 
	 * @param param
	 *            name of the parameter , String.
	 * @return the value of the parameter as String.
	 * @throws Exception
	 *             : never
	 */
	private String getStringParam(String param) {
		String parameter = (String) getParam(param);
		if (parameter != null) {
			parameter = parameter.trim();
		}
		return parameter;
	}

	/**
	 * Rollback the last transactions in Derby database
	 * 
	 * @see #markEntryInDeltaStore(Entry)
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void rollbackDeltaState() throws SQLException {
		if (delta != null) {
			delta.rollback();
		}
	}

	/**
	 * Commit the last transactions in Derby database (alias for
	 * commitDeltaState)
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void saveDeltaState() throws SQLException {
		if (delta != null) {
			delta.commit();
		}
	}

	/**
	 * Commit if in commit mode "On end of AL cycle"
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commitOnEndIter() throws SQLException {
		if (delta != null && !readDeleted) {
			delta.commitOnEndIter();
		}
	}

	/**
	 * Commit the last transactions in Derby database
	 * 
	 * @exception SQLException
	 *                Thrown if an error occurs
	 */
	public void commitDeltaState() throws SQLException {
		if (delta != null) {
			delta.commit();
		}
	}

	/**
	 * Closes connection to the Delta Store.
	 * 
	 * @throws Exception
	 */
	public void closeDelta() throws Exception {
		if (delta != null) {
			delta.closeDelta();
		}
	}

	/**
	 * This method is used to report different types of made changes.
	 * 
	 * @return The statistics for this run;
	 */
	public String getStatisticsString() {
		if (delta != null) {
			return delta.getStatisticsString();
		}
		return "";
	}

	/**
	 * Logs debug message if the component is in debug mode.
	 * 
	 * @param debugMessage
	 *            message to write.
	 */
	private void logdebug(String debugMessage) {
		if (logger != null) {
			logger.logdebug(debugMessage);
		}
	}

	/**
	 * Logs an error message.
	 * 
	 * @param errorMessage
	 *            the message.
	 */
	private void logerror(String errorMessage) {
		if (logger != null) {
			logger.logerror(errorMessage);
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}
	
	/**
	 * This method closes the internally used Delta Store.
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public void terminate() throws Exception {
		closeDelta();
		super.terminate();
	}
	
	/**
	 * Returns true if we are reading deleted entries at the moment
	 */
	public boolean isReadingDeleted() {
		return readDeleted;
	}
}
