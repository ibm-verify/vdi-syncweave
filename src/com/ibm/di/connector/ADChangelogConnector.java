/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.SortControl;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.store.StoreFactory;

/**
 * The class ADChangelogConnector represents the Active Directory Changelog
 * connector class that will be accessed by IBM Tivoli Directory Integrator. It
 * extends the LDAP connector class (LDAPConnector) and overrides some of its
 * methods to implement AD-specific functionality.
 */
public class ADChangelogConnector extends LDAPConnector implements
		ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// connector parameters names
	/**
	 * Parameter Name: {@value #PARAM_LDAP_SEARCH_BASE}
	 */
	public static final String PARAM_LDAP_SEARCH_BASE = "ldapSearchBase";

	/**
	 * Parameter Name: {@value #PARAM_USN_FILE_NAME}
	 */
	public static final String PARAM_USN_FILE_NAME = "usnFileName";

	/**
	 * Parameter Name: {@value #PARAM_USN_STORE_PARAM_NAME}
	 */
	public static final String PARAM_USN_STORE_PARAM_NAME = "iteratorStateKey";

	/**
	 * Parameter Name: {@value #PARAM_START_AT}
	 */
	public static final String PARAM_START_AT = "startAt";

	/**
	 * Parameter Name: {@value #PARAM_SLEEP_INTERVAL}
	 */
	public static final String PARAM_SLEEP_INTERVAL = "sleepInterval";

	/**
	 * Parameter Name: {@value #PARAM_TIMEOUT}
	 */
	public static final String PARAM_TIMEOUT = "timeout";

	/**
	 * Start at predefined value - '0'
	 */
	private static final String START_AT_ZERO = "0";
	/**
	 * Start at predefined value - 'EOD'
	 */
	private static final String START_AT_END_OF_DATA = "EOD";

	// ADChangelog Connector additional entry attributes
	/**
	 * An {@link Entry} attribute name: {@value #ATTR_CHANGE_TYPE}
	 */
	public static final String ATTR_CHANGE_TYPE = "changeType";

	/**
	 * An {@link Entry} attribute value ({@value #CHANGE_TYPE_ADD}) for
	 * attribute with name: {@value #ATTR_CHANGE_TYPE}
	 */
	public static final String CHANGE_TYPE_ADD = "add";

	/**
	 * An {@link Entry} attribute value ({@value #CHANGE_TYPE_MODIFY}) for
	 * attribute with name: {@value #ATTR_CHANGE_TYPE}
	 */
	public static final String CHANGE_TYPE_MODIFY = "modify";

	/**
	 * An {@link Entry} attribute value ({@value #CHANGE_TYPE_DELETE}) for
	 * attribute with name: {@value #ATTR_CHANGE_TYPE}
	 */
	public static final String CHANGE_TYPE_DELETE = "delete";

	// AD object attributes
	/**
	 * AD object attribute name - "usnchanged"
	 */
	private static final String AD_ATTR_USN_CHANGED = "usnchanged";
	/**
	 * AD object attribute name - "usncreated"
	 */
	private static final String AD_ATTR_USN_CREATED = "usncreated";
	/**
	 * AD object attribute name - "isDeleted"
	 */
	private static final String AD_ATTR_IS_DELETED = "isDeleted";
	/**
	 * AD object attribute name - "objectGUID"
	 */
	private static final String AD_ATTR_OBJECT_GUID = "objectGUID";
	/**
	 * AD object attribute name - "objectGUIDStr"
	 */
	private static final String AD_ATTR_OBJECT_GUID_STR = "objectGUIDStr";

	/**
	 * AD object attribute name - "highestCommittedUSN"
	 */
	private static final String AD_ATTR_HIGHEST_COMMITTED_USN = "highestCommittedUSN";

	// USN properties file parameters
	/**
	 * USN property file parameter: {@link #PROP_START_USN}
	 */
	public static final String PROP_START_USN = "START_USN";

	/**
	 * USN property file parameter: {@link #PROP_END_USN}
	 */
	public static final String PROP_END_USN = "END_USN";

	/**
	 * USN property file parameter: {@link #PROP_CURRENT_USN_CREATED}
	 */
	public static final String PROP_CURRENT_USN_CREATED = "CURRENT_USN_CREATED";

	/**
	 * USN property file parameter: {@link #PROP_CURRENT_USN_CHANGED}
	 */
	public static final String PROP_CURRENT_USN_CHANGED = "CURRENT_USN_CHANGED";

	/**
	 * TMS Filename used in the Connector for info, error and debug messages
	 */
	private static final String PROPERTIES_FILE = "adchangelog";

	/**
	 * ResourceHash used for access of the TMS messages
	 */
	private static ResourceHash sResHash = new ResourceHash(PROPERTIES_FILE);

	/**
	 * USN header property value.
	 */
	private static final String PROP_USN_HEADER = sResHash
			.getString("CONNECTOR.ADCHANGELOG.USN.VALUES.NEEDED.FOR.THE.NEXT.SYNCHONIZATION");

	/**
	 * Used to set the maximum number of AD objects returned by the search; the
	 * value is 1, since only the first object returned by AD is used.
	 */
	private static final long RETURNED_ENTRIES_COUNT_LIMIT = 1;

	/**
	 * Synchronization session's start USN.
	 */
	private int mStartUsn = 0;

	/**
	 * Synchronization session's end USN
	 */
	private int mEndUsn = 0;

	/**
	 * Synchronization session's current uSNCreated - used in the first pass of
	 * the synchronization when entry creations are delivered.
	 */
	private int mCurrentUsnCreated = 0;

	/**
	 * Synchronization session's current uSNChanged - used in the second pass of
	 * the synchronization when entry modifications and deletions are delivered.
	 */
	private int mCurrentUsnChanged = 0;

	/**
	 * Stores the "Sleep Interval" Connector parameter.
	 */
	private long mSleepInterval = 0;

	/**
	 * Stores the "Timeout" Connector parameter.
	 */
	private long mTimeout = 0;

	/**
	 * Stores the name of the file where USN values are read from and saved to.
	 */
	private String mUsnFileName = null;

	/**
	 * Stores the name of the persistent parameter storing the USN values.
	 */
	private String mUsnStoreParamName = null;

	/**
	 * Specifies predefined start position - start or end of data. Only used
	 * when the persistent parameter is not found in the store.
	 */
	private String mStartAt = null;

	/**
	 * LDAP search constraints.
	 */
	private SearchControls mSearchConstraints;

	/**
	 * Request control for sorting the results by the uSNChanged attribute.
	 */
	private Control mSortUsnChangedControl = null;

	/**
	 * Request control for sorting the results by the uSNCreated attribute.
	 */
	private Control mSortUsnCreatedControl = null;

	/**
	 * Request control for showing deleted objects.
	 */
	private Control mShowDeletedControl = null;

	/**
	 * Control array used for setting request controls on AD.
	 */
	private Control[] mCtrlArray = null;

	/**
	 * Calls the super constructor and assigns supported Connector modes.
	 */
	public ADChangelogConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Reads connector parameters' values and prepares LDAP search constraints.
	 * 
	 * @param aObj
	 *            This parameter is usually null but can be any type of object
	 *            the caller chooses to pass on. Normally the parameter is some
	 *            kind of input stream or Reader object. This Connector ignores
	 *            this parameter.
	 * 
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	@Override
	public void initialize(Object aObj) throws Exception {
		// Specify to the JNDI library that the "objectGUID" and "objectSid"
		// attributes must be transfered as binary,
		// and not as ASCII data
		setParam("jndiExtraProviderParams",
				"java.naming.ldap.attributes.binary:objectGUID objectSid");

		// initialize LDAP Connector
		super.initialize(aObj);

		// read the USN file name (backward compatibility)
		mUsnFileName = getParam(PARAM_USN_FILE_NAME);
		if (mUsnFileName != null && mUsnFileName.trim().length() == 0) {
			mUsnFileName = null;
		}

		// read USN state persistent parameter name
		mUsnStoreParamName = getParam(PARAM_USN_STORE_PARAM_NAME);
		if (mUsnStoreParamName != null
				&& mUsnStoreParamName.trim().length() == 0) {
			mUsnStoreParamName = null;
		}

		// read the "Start at" value - it is only used when the specified
		// persistent parameter is not found in the store
		mStartAt = getParam(PARAM_START_AT);
		if (mStartAt != null && mStartAt.trim().length() == 0) {
			mStartAt = null;
		}

		// read, parse and store the "Sleep Interval" parameter
		String sleepIntervalStr = getParam(PARAM_SLEEP_INTERVAL);
		try {
			mSleepInterval = Long.parseLong(sleepIntervalStr);
		} catch (NumberFormatException e) {
			mSleepInterval = 0l;
			logmsg(sResHash
					.getString(
							"CONNECTOR.ADCHANGELOG.INVALID.SLEEP.INTERVAL.VALUE.SPECIFIED",
							new Object[] { sleepIntervalStr,
									Long.valueOf(mSleepInterval) }));
		}
		if (mSleepInterval < 0 || (mSleepInterval * 1000) < 0) {
			mSleepInterval = 0;
			logmsg(sResHash
					.getString(
							"CONNECTOR.ADCHANGELOG.INVALID.SLEEP.INTERVAL.VALUE.SPECIFIED",
							new Object[] { sleepIntervalStr,
									Long.valueOf(mSleepInterval) }));
		}

		// read, parse and store the "Timeout" parameter
		String timeoutStr = getParam(PARAM_TIMEOUT);
		try {
			mTimeout = Long.parseLong(timeoutStr);
		} catch (NumberFormatException e) {
			mTimeout = 5l;
			logmsg(sResHash.getString(
					"CONNECTOR.ADCHANGELOG.INVALID.TIMEOUT.VALUE.SPECIFIED",
					new Object[] { timeoutStr, Long.valueOf(mTimeout) }));
		}
		if (mTimeout < 0 || (mTimeout * 1000) < 0) {
			mTimeout = 0;
			logmsg(sResHash.getString(
					"CONNECTOR.ADCHANGELOG.INVALID.TIMEOUT.VALUE.SPECIFIED",
					new Object[] { timeoutStr, Long.valueOf(mTimeout) }));
		}

		// define search constraints for use in search operations
		mSearchConstraints = new SearchControls();
		mSearchConstraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
		mSearchConstraints.setCountLimit(RETURNED_ENTRIES_COUNT_LIMIT);

		// define request controls for sending to Active Directory
		mSortUsnChangedControl = new SortControl(
				new String[] { AD_ATTR_USN_CHANGED }, Control.CRITICAL);
		mSortUsnCreatedControl = new SortControl(
				new String[] { AD_ATTR_USN_CREATED }, Control.CRITICAL);
		mShowDeletedControl = new ShowDeletedControl();
	}

	/**
	 * Reads USN values from a given Entry and populates them in member
	 * variables.
	 * 
	 * @param aUsnEntry
	 *            {@link Entry} to read from.
	 * 
	 * @throws Exception
	 *             If the Entry specified does not contain the necessary USN
	 *             values.
	 */
	private void getUsnValuesFromEntry(Entry aUsnEntry) throws Exception {
		mStartUsn = getUsnIntValue(aUsnEntry, PROP_START_USN);
		mEndUsn = getUsnIntValue(aUsnEntry, PROP_END_USN);
		mCurrentUsnCreated = getUsnIntValue(aUsnEntry, PROP_CURRENT_USN_CREATED);
		mCurrentUsnChanged = getUsnIntValue(aUsnEntry, PROP_CURRENT_USN_CHANGED);
	}

	/**
	 * Read a single USN value from an Entry's Attribute.
	 * 
	 * @param aUsnEntry
	 *            {@link Entry} to read from
	 * @param aAttrName
	 *            name of the attribute
	 * @return int value of the attribute.
	 * 
	 * @throws Exception
	 *             If the specified USN Attribute is not found in the given
	 *             Entry or has an invalid numeric value.
	 */
	private int getUsnIntValue(Entry aUsnEntry, String aAttrName)
			throws Exception {
		Object attrValue = aUsnEntry.getObject(aAttrName);
		if (attrValue == null) {
			throw new Exception(
					sResHash
							.getString(
									"CONNECTOR.ADCHANGELOG.USN.ENTRY.DOES.NOT.CONTAIN.ATTRIBUTE",
									aAttrName));
		}

		if (attrValue instanceof Integer) {
			return ((Integer) attrValue).intValue();
		} else {
			return Integer.parseInt(attrValue.toString());
		}
	}

	/**
	 * Initializes USN member variables to 0, so that a full synchronization
	 * will be performed.
	 */
	private void setZeroUsnValues() {
		mStartUsn = 0;
		mEndUsn = 0;
		mCurrentUsnCreated = 0;
		mCurrentUsnChanged = 0;

		logmsg(sResHash
				.getString("CONNECTOR.ADCHANGELOG.USN.VALUES.SET.TO.ZERO.FULL.SYNCHRONIZATION"));
	}

	/**
	 * Logs the Connector's current USN values.
	 */
	private void logUsnValues() {
		logmsg(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_START_USN, "" + mStartUsn }));
		logmsg(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_END_USN, "" + mEndUsn }));
		logmsg(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_CURRENT_USN_CREATED,
						"" + mCurrentUsnCreated }));
		logmsg(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_CURRENT_USN_CHANGED,
						"" + mCurrentUsnChanged }));
	}

	/**
	 * Debugs the Connector's current USN values.
	 */
	private void debugUsnValues() {
		debug(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_START_USN, "" + mStartUsn }));
		debug(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_END_USN, "" + mEndUsn }));
		debug(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_CURRENT_USN_CREATED,
						"" + mCurrentUsnCreated }));
		debug(sResHash.getString("CONNECTOR.ADCHANGELOG.PRINTATTR.VALUE",
				new Object[] { PROP_CURRENT_USN_CHANGED,
						"" + mCurrentUsnChanged }));
	}

	/**
	 * Reads start USN values. Sources for start USN values are checked in this
	 * order:
	 * <p>
	 * (1) File name parameter is specified - backward compatibility mode;
	 * <p>
	 * (2) Persistent parameter - if the specified parameter is not found in the
	 * store, the "Start at" value is used.
	 * 
	 * <br>
	 * The method will reset the USN Parameters (using the
	 * <code>setZeroUsnValues()</code> private method) if the USN properties
	 * file does not exists, could not be read or the properties in it could be
	 * parsed.
	 * 
	 * @throws Exception
	 *             if an error working with the USN properties occurs.
	 */
	protected void getStartUsnValues() throws Exception {
		// if file name is specified - run in backward compatibility mode
		if (mUsnFileName != null) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.ADCHANGELOG.WILL.READ.START.USN.VALUES.FROM.FILE",
							mUsnFileName));
			try {
				Properties usnFileProps = new Properties();
				FileInputStream in = new FileInputStream(mUsnFileName);
				try {
					usnFileProps.load(in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.ADCHANGELOG.COULD.NOT.CLOSE.USN.PROPERTIES.FILE", new Object[] {
									mUsnFileName, e.toString() }));
						}
					}
				}

				mStartUsn = Integer.parseInt(usnFileProps
						.getProperty(PROP_START_USN));
				mEndUsn = Integer.parseInt(usnFileProps
						.getProperty(PROP_END_USN));
				mCurrentUsnCreated = Integer.parseInt(usnFileProps
						.getProperty(PROP_CURRENT_USN_CREATED));
				mCurrentUsnChanged = Integer.parseInt(usnFileProps
						.getProperty(PROP_CURRENT_USN_CHANGED));
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.ADCHANGELOG.ERROR.COULD.NOT.READ.START.USN.VALUES.FROM.FILE",
								new Object[] { mUsnFileName, e.toString() }));
				setZeroUsnValues();
			}
			return;
		}

		// Use the persistent parameter storage
		if (mUsnStoreParamName == null) {
			logmsg(sResHash
					.getString("CONNECTOR.ADCHANGELOG.ERROR.ITERATOR.STATE.STORE.PARAM.NOT.SPECIFIED"));
			logmsg(sResHash
					.getString("CONNECTOR.ADCHANGELOG.AT.LEAST.ONE.OF.THE.PARAMETERS.SHOULD.BE.SPECIFIED"));
			throw new Exception(
					sResHash
							.getString("CONNECTOR.ADCHANGELOG.USN.STORAGE.NOT.SPECIFIED"));
		}

		Entry usnStoreProp = null;
		try {
			usnStoreProp = (Entry) StoreFactory.getDefaultPropertyStore()
					.getProperty(mUsnStoreParamName);
		} catch (Exception e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.ADCHANGELOG.ERROR.ON.ACCESS.TO.THE.PERSISTENT.PARAMETER.STORE",
							e.toString()));
			throw e;
		}

		// get start USN values from the Persistent Parameter Store
		if (usnStoreProp != null) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.ADCHANGELOG.WILL.READ.START.USN.VALUES",
						mUsnStoreParamName));
			}
			try {
				getUsnValuesFromEntry(usnStoreProp);
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.ADCHANGELOG.ERROR.COULD.NOT.RETRIEVE.START.USN.VALUES",
								new Object[] { mUsnStoreParamName, e.toString() }));
				throw e;
			}
		}
		// if the persistent parameter is not found, use the "start at" value
		else {
			logmsg(sResHash.getString(
					"CONNECTOR.ADCHANGELOG.PARAMETER.NOT.FOUND",
					mUsnStoreParamName));
			logmsg(sResHash
					.getString("CONNECTOR.ADCHANGELOG.WILL.USE.THE.START.AT.PARAMETER.VALUE"));
			if (mStartAt == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.ADCHANGELOG.ERROR.START.AT.PARAMETER.NOT.SPECIFIED"));
			}

			if (mStartAt.equalsIgnoreCase(START_AT_ZERO)) {
				setZeroUsnValues();
			} else if (mStartAt.equalsIgnoreCase(START_AT_END_OF_DATA)) {
				try {
					mStartUsn = getHighestCommittedUsn() + 1;
					mEndUsn = 0;
					mCurrentUsnCreated = 0;
					mCurrentUsnChanged = 0;
					logmsg(sResHash
							.getString("CONNECTOR.ADCHANGELOG.SYNCHRONIZATION.FROM.END.OF.DATA"));
				} catch (Exception e) {
					logmsg(sResHash
							.getString(
									"CONNECTOR.ADCHANGELOG.ERROR.SYNCHRONIZATION.FROM.END.OF.DATA",
									e.toString()));
					throw e;
				}
			} else {
				String funcmsg = sResHash
						.getString(
								"CONNECTOR.ADCHANGELOG.INVALID.START.AT.VALUE.SPECIFIED",
								mStartAt);
				logmsg(funcmsg);
				throw new Exception(funcmsg);
			}
		}
	}

	/**
	 * Reads the initial USN values from file and sets necessary request
	 * controls.
	 * 
	 * @throws Exception
	 *             If cannot obtain the highestCommitedUsn from Active
	 *             Directory.
	 * 
	 */
	public void selectEntries() throws Exception {
		getStartUsnValues();

		// debug start USN values
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.ADCHANGELOG.START.USN.VALUES"));
			debugUsnValues();
		}

		// handle special cases
		boolean startUsnValuesAdjusted = false;
		if (mEndUsn < mStartUsn) {
			mEndUsn = getHighestCommittedUsn();
			mCurrentUsnCreated = mStartUsn;
			mCurrentUsnChanged = 0;
			startUsnValuesAdjusted = true;
		}
		if (mCurrentUsnCreated > mEndUsn) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.ADCHANGELOG.INITIAL.PROP.CURRENT.USN.CREATED.IS.GREATER"));
			}
			mCurrentUsnCreated = mStartUsn;
			mCurrentUsnChanged = 0;
			startUsnValuesAdjusted = true;
		}
		if (mCurrentUsnCreated < mStartUsn && mCurrentUsnChanged < mStartUsn) {
			if (debugMode()) {
				debug(sResHash
						.getString("CONNECTOR.ADCHANGELOG.BOTH.PROPS.ARE.SMALLER"));
			}
			mCurrentUsnCreated = mStartUsn;
			mCurrentUsnChanged = 0;
			startUsnValuesAdjusted = true;
		}

		if ((startUsnValuesAdjusted) && (debugMode())) {
			debug(sResHash.getString("CONNECTOR.ADCHANGELOG.START.USN.VALUES.INTERNALLY.ADJUSTED"));
			debugUsnValues();
		}

		setRequestControls();
	}

	/**
	 * Sets the necessary request controls.
	 * 
	 * @throws Exception
	 *             If an error occurs while setting the request controls.
	 */
	private void setRequestControls() throws Exception {
		// set the necessary request controls
		if (mCurrentUsnCreated >= mStartUsn) {
			mCtrlArray = new Control[] { mShowDeletedControl,
					mSortUsnCreatedControl };
		} else {
			mCtrlArray = new Control[] { mShowDeletedControl,
					mSortUsnChangedControl };
		}
		getLdapContext().setRequestControls(mCtrlArray);
	}

	/**
	 * Retrieves the highest committed USN from Active Directory.
	 * 
	 * @return The Active Directory's "highestCommittedUsn" Attribute.
	 * @throws Exception
	 *             If cannot retrieve the highest committed USN number.
	 */
	protected int getHighestCommittedUsn() throws Exception {
		int highestCommittedUsn = -1;
		Attributes attrs = getLdapContext().getAttributes("");
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs
					.get(AD_ATTR_HIGHEST_COMMITTED_USN);
			if (attr != null) {
				Object obj = attr.get();
				highestCommittedUsn = new Integer(obj.toString()).intValue();
			}
		}

		if (highestCommittedUsn == -1) {
			logmsg(sResHash
					.getString("CONNECTOR.ADCHANGELOG.GETHIGHESTCOMMITTEDUSN.COULD.NOT.RETRIEVE"));
			throw new Exception(
					sResHash
							.getString("CONNECTOR.ADCHANGELOG.COULD.NOT.RETRIEVE.HIGHEST.COMMITTED.NUMBER"));
		}

		return highestCommittedUsn;
	}

	/**
	 * Inserts leading zeros. For example, if the number 21 is required to be
	 * represented by exactly 4 digits, then the call "insertLeadingZeros("21",
	 * 4)" will return "0021". If the number is already represented by the
	 * required number of digits or more, no leading zeros will be inserted.
	 * 
	 * @param aStrNumber
	 *            The String representation of a number.
	 * @param aRequiredDigits
	 *            The number of digits required to represent the number.
	 * @return The String representation of the number with the necessary number
	 *         of leading zeros.
	 */
	private String insertLeadingZeros(String aStrNumber, int aRequiredDigits) {
		String result = aStrNumber;
		while (result.length() < aRequiredDigits) {
			result = "0" + result;
		}
		return result;
	}

	/**
	 * Generates the hexadecimal String representation of the GUID based on its
	 * 128-bit binary representation. The String representation of a GUID has
	 * the form "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}". The digits used are
	 * the hexadecimal digits 0,1,2,3,4,5,6,7,8,9,A,B,C,D,E and F.
	 * 
	 * @param aBinaryData
	 *            A 16-byte byte array, holding the 128-bit binary
	 *            representation of the GUID.
	 * 
	 * @return The hexadecimal String representation of the binary GUID.
	 */
	protected String binaryGUIDtoString(byte[] aBinaryData) {
		StringBuffer stringGUID = new StringBuffer("{");
		String str;
		int n;

		// generate the first 8 hexadecimal digits
		n = 0;
		n |= ((int) (aBinaryData[0])) & 0x000000FF;
		n |= (((int) (aBinaryData[1])) << 8) & 0x0000FF00;
		n |= (((int) (aBinaryData[2])) << 16) & 0x00FF0000;
		n |= (((int) (aBinaryData[3])) << 24) & 0xFF000000;
		str = java.lang.Integer.toHexString(n);
		stringGUID.append(insertLeadingZeros(str, 8));
		stringGUID.append("-");

		// generate the first and the second groups of 4 hexadecimal digits
		for (int i = 2; i < 4; i++) {
			n = 0;
			n |= ((int) (aBinaryData[i * 2])) & 0x000000FF;
			n |= (((int) (aBinaryData[i * 2 + 1])) << 8) & 0x0000FF00;
			str = java.lang.Integer.toHexString(n);
			stringGUID.append(insertLeadingZeros(str, 4));
			stringGUID.append("-");
		}

		// generate the third group of 4 hexadecimal digits
		n = 0;
		n |= (((int) (aBinaryData[8])) << 8) & 0x0000FF00;
		n |= (((int) (aBinaryData[9]))) & 0x000000FF;
		str = java.lang.Integer.toHexString(n);
		stringGUID.append(insertLeadingZeros(str, 4));
		stringGUID.append("-");

		// generate the last 12 hexadecimal digits
		for (int i = 5; i < 8; i++) {
			n = 0;
			n |= (((int) (aBinaryData[i * 2 + 1]))) & 0x000000FF;
			n |= (((int) (aBinaryData[i * 2])) << 8) & 0x0000FF00;
			str = java.lang.Integer.toHexString(n);
			stringGUID.append(insertLeadingZeros(str, 4));
		}

		stringGUID.append("}");
		return stringGUID.toString().toUpperCase(Locale.ENGLISH);
	}

	/**
	 * Adds the "objectGUIDStr" Attribute to the Entry. The value of this
	 * Attribute is set to the String representation of the binary value of this
	 * Entry's "objectGUID" Attribute.
	 * 
	 * @param aEntry
	 *            The Entry to add the "objectGUIDStr" Attribute to.
	 */
	protected void addGUIDStrAttribute(Entry aEntry) {
		Attribute attr = aEntry.getAttribute(AD_ATTR_OBJECT_GUID);
		if (attr != null) {
			byte[] binaryData = (byte[]) attr.getValue(0);
			String stringGUID = binaryGUIDtoString(binaryData);
			aEntry.setAttribute(AD_ATTR_OBJECT_GUID_STR, stringGUID);
		}
	}

	/**
	 * Retrieves an Entry from AD based on the given filter.
	 * 
	 * @param aFilter
	 *            The LDAP filter for retrieving the Entry.
	 * 
	 * @return The first Entry that matched the filter; "null" if no AD Entry
	 *         matches the filter.
	 * @throws NamingException
	 *             If error occurs while retrieving the Entry from Active
	 *             Directory.
	 */
	protected Entry retrieveEntry(String aFilter) throws NamingException {
		Entry entry = null;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.ADCHANGELOG.LDAP.SEARCH.QUERY",
					aFilter));
		}

		try {
			NamingEnumeration<SearchResult> results = getLdapContext().search(
					getParam(PARAM_LDAP_SEARCH_BASE), aFilter,
					mSearchConstraints);
			if (results.hasMore()) {
				SearchResult searchRes = results.next();

				entry = entry2at(searchRes);
				addGUIDStrAttribute(entry);
			}
		}
		// In some cases where there are no Active Directory objects that match
		// the filer, PartialResultException is thrown.
		catch (PartialResultException e) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.ADCHANGELOG.PARTIALRESULTEXCEPTION.CAUGHT",
						e.toString()));
			}
			entry = null;
		}

		return entry;
	}

	/**
	 * Retrieves an Attribute value as int.
	 * 
	 * @param aEntry
	 *            The Entry which Attribute will be read.
	 * @param aAttributeName
	 *            The name of the Attribute to read.
	 * @return The "int" value of the Attribute; if the value is not an integer
	 *         "0" is returned.
	 */
	private int getAttributeIntValue(Entry aEntry, String aAttributeName) {
		int value;
		try {
			value = Integer.parseInt(aEntry.getString(aAttributeName));
		} catch (NumberFormatException e) {
			value = 0;
		}
		return value;
	}

	/**
	 * Retrieves the next "changed" object from the AD.
	 * 
	 * @return The next "changed" Entry object.
	 * @throws Exception
	 *             If retrieving the next "changed" Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		Entry entry = null;

		long startTime;
		long currentTime;

		startTime = System.currentTimeMillis();

		boolean isEntryRetrieved = false;
		boolean isTimeout = false;

		// counts the number of sessions without Entry retrieval
		int noEntrySesions = 0;

		do {
			String filter = null;

			// check if we are on the first pass - creation of objects
			if (mCurrentUsnCreated >= mStartUsn) {
				if (mStartUsn <= mEndUsn) {
					filter = "(&(" + AD_ATTR_USN_CREATED + ">="
							+ mCurrentUsnCreated + ")" + "("
							+ AD_ATTR_USN_CREATED + "<=" + mEndUsn + ")"
							+ "(!(&(" + AD_ATTR_IS_DELETED + "=TRUE)("
							+ AD_ATTR_USN_CHANGED + "<=" + mEndUsn + "))))";

					entry = retrieveEntry(filter);
				}

				boolean creationPassFinished = false;
				if (entry == null) {
					isEntryRetrieved = false;
					creationPassFinished = true;
				} else {
					isEntryRetrieved = true;
					entry.addAttributeValue(ATTR_CHANGE_TYPE, CHANGE_TYPE_ADD);

					int usnCreated = getAttributeIntValue(entry,
							AD_ATTR_USN_CREATED);
					mCurrentUsnCreated = usnCreated + 1;
					if (mCurrentUsnCreated > mEndUsn) {
						creationPassFinished = true;
					}
				}

				if (creationPassFinished) {
					// the creation pass has finished - go for the modify&delete
					// pass
					mCtrlArray = new Control[] { mShowDeletedControl,
							mSortUsnChangedControl };
					getLdapContext().setRequestControls(mCtrlArray);

					mCurrentUsnCreated = mStartUsn - 1;
					mCurrentUsnChanged = mStartUsn;
				}

				storeUSNForNextSynch();
			}
			// we are on the second pass - modications and deletions
			else {
				if (mStartUsn <= mEndUsn) {
					filter = "(&(" + AD_ATTR_USN_CHANGED + ">="
							+ mCurrentUsnChanged + ")" + "("
							+ AD_ATTR_USN_CHANGED + "<=" + mEndUsn + ")" + "("
							+ AD_ATTR_USN_CREATED + "<=" + (mStartUsn - 1)
							+ "))";

					entry = retrieveEntry(filter);
				}

				boolean updatePassFinished = false;
				if (entry == null) {
					isEntryRetrieved = false;
					updatePassFinished = true;
				} else {
					isEntryRetrieved = true;

					String isDeleted = entry.getString(AD_ATTR_IS_DELETED);
					int usnChanged = getAttributeIntValue(entry,
							AD_ATTR_USN_CHANGED);

					String changeTypeValue;
					if (isDeleted != null && isDeleted.equalsIgnoreCase("TRUE")) {
						changeTypeValue = CHANGE_TYPE_DELETE;
					} else {
						changeTypeValue = CHANGE_TYPE_MODIFY;
					}
					entry.addAttributeValue(ATTR_CHANGE_TYPE, changeTypeValue);

					mCurrentUsnChanged = usnChanged + 1;
					if (mCurrentUsnChanged > mEndUsn) {
						updatePassFinished = true;
					}
				}

				if (updatePassFinished) {
					// this session is over - go for the next one with the new
					// highestCommittedUsn
					mCtrlArray = new Control[] { mShowDeletedControl,
							mSortUsnCreatedControl };
					getLdapContext().setRequestControls(mCtrlArray);

					mStartUsn = mEndUsn + 1;
					mEndUsn = getHighestCommittedUsn();
					mCurrentUsnCreated = mStartUsn;
					mCurrentUsnChanged = 0;

					noEntrySesions++;
				}

				storeUSNForNextSynch();
			}

			if (!isEntryRetrieved) {
				entry = null;

				// perform sleep and timeout only after a full session (add and
				// modify/delete)
				if (noEntrySesions >= 2) {
					if (mSleepInterval > 0) {
						// make sure that the thread will not sleep, if its
						// sleeping would delay
						// the requested Conntector's timing-out
						currentTime = System.currentTimeMillis();
						if ((mTimeout == 0)
								|| ((mTimeout > 0) && ((currentTime + mSleepInterval * 1000) - startTime) < (mTimeout * 1000))) {
							Thread.sleep(mSleepInterval * 1000);

							// check whether new changes have been made while
							// sleeping
							// and if so update the highest USN for the new
							// session
							int newHighestCommittedUsn = getHighestCommittedUsn();
							if (newHighestCommittedUsn > mEndUsn) {
								mEndUsn = newHighestCommittedUsn;
								storeUSNForNextSynch();
							}
						} else {
							isTimeout = true;
						}
					}

					// check if the "Timeout" has expired
					if ((mTimeout > 0) && (!isTimeout)) {
						currentTime = System.currentTimeMillis();
						if ((currentTime - startTime) > (mTimeout * 1000)) {
							isTimeout = true;
						}
					}

					if (isTimeout && debugMode()) {
						debug(sResHash
								.getString("CONNECTOR.ADCHANGELOG.TIMEOUT.WAITING.FOR.NEXT.CHANGED.ENTRY"));
					}

					// sleep or check for timeout again after the next session
					noEntrySesions = 1;
				}
			}
		} while ((!isEntryRetrieved) && (!isTimeout));

		return entry;
	}

	/**
	 * @return an Entry containing the current Connector's USN values as
	 *         properties. Such Entries are used for storage in the persistent
	 *         store.
	 */
	protected Entry packUsnValues() {
		Entry usnEntry = new Entry();
		usnEntry.setAttribute(PROP_START_USN, Integer.valueOf(mStartUsn));
		usnEntry.setAttribute(PROP_END_USN, Integer.valueOf(mEndUsn));
		usnEntry.setAttribute(PROP_CURRENT_USN_CREATED, Integer
				.valueOf(mCurrentUsnCreated));
		usnEntry.setAttribute(PROP_CURRENT_USN_CHANGED, Integer
				.valueOf(mCurrentUsnChanged));
		return usnEntry;
	}

	/**
	 * Stores the USN values for the next synchronization.
	 */
	protected void storeUSNForNextSynch() {
		if (mUsnFileName != null) {
			try {
				Properties usnFileProps = new Properties();

				usnFileProps.setProperty(PROP_START_USN, Integer.valueOf(
						mStartUsn).toString());
				usnFileProps.setProperty(PROP_END_USN, Integer.valueOf(mEndUsn)
						.toString());
				usnFileProps.setProperty(PROP_CURRENT_USN_CREATED, Integer
						.valueOf(mCurrentUsnCreated).toString());
				usnFileProps.setProperty(PROP_CURRENT_USN_CHANGED, Integer
						.valueOf(mCurrentUsnChanged).toString());
				
				FileOutputStream usnFile = new FileOutputStream(mUsnFileName);
				try {
					usnFileProps.store(usnFile, PROP_USN_HEADER);
				} finally {
					usnFile.close();
				}
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.ADCHANGELOG.ERROR.COULD.NOT.STORE.USN.VALUES.IN.FILE",
								new Object[] { mUsnFileName, e.toString() }));
				logmsg(sResHash
						.getString("CONNECTOR.ADCHANGELOG.CURRENT.USN.VALUES.ARE"));
				logUsnValues();
			}
		}

		if (mUsnStoreParamName != null) {
			try {
				StoreFactory.getDefaultPropertyStore().setProperty(
						mUsnStoreParamName, packUsnValues());
			} catch (Exception e) {
				logmsg(sResHash
						.getString(
								"CONNECTOR.ADCHANGELOG.ERROR.COULD.NOT.STORE.USN.VALUES.IN.PERSISTENT.PARAMETER.STORE",
								e.toString()));
				logmsg(sResHash
						.getString("CONNECTOR.ADCHANGELOG.CURRENT.USN.VALUES.ARE.2"));
				logUsnValues();
			}
		}
	}

	/**
	 * The class ShowDeletedControl represents the "show deleted objects" LDAPv3
	 * request control. The "show deleted objects" request control instructs AD
	 * to return deleted as well as ordinary objects.
	 */
	static class ShowDeletedControl implements Control {

		/**
		 * Unique ID used for deserialization.
		 */
		private static final long serialVersionUID = -8314383064934538383L;

		/**
		 * The code of the "show deleted objects" request control
		 */
		public static final String LDAP_SERVER_SHOW_DELETED_OID = "1.2.840.113556.1.4.417";

		/**
		 * @return an empty byte array - no parameters needed
		 */
		public byte[] getEncodedValue() {
			return new byte[] {};
		}

		/**
		 * @return the {@link #LDAP_SERVER_SHOW_DELETED_OID} value.
		 */
		public String getID() {
			return LDAP_SERVER_SHOW_DELETED_OID;
		}

		/**
		 * The control is critical, since the ADChangelog Connector definitely
		 * needs to report deleted objects
		 * 
		 * @return the value of the {@link Control#CRITICAL} constant - true.
		 */
		public boolean isCritical() {
			return Control.CRITICAL;
		}
	}

	// **************************************************************
	// APIs for public access to the USN values
	// **************************************************************

	/**
	 * Retrives USN synchronization values.
	 * @return an Entry object containing the current USN synchronization
	 *         values.
	 */
	public Entry getUsnValues() {
		return packUsnValues();
	}

	/**
	 * Sets the Connector's current USN synchronization values to the values
	 * specified in the "aUsnEntry" parameter.
	 * 
	 * @param aUsnEntry
	 *            the {@link Entry} object containing the USN values.
	 * 
	 * @throws Exception
	 *             If the given Entry object does not contain the necessary USN
	 *             values.
	 */
	public void setUsnValues(Entry aUsnEntry) throws Exception {
		getUsnValuesFromEntry(aUsnEntry);
	}

	/**
	 * Version information.
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I%, 20%E%";
	}

	/**
	 * {@inheritDoc}
	 */
	public void reconnect(Object o) throws Exception {
		terminate();
		initialize(o);
		setRequestControls();
	}

}
