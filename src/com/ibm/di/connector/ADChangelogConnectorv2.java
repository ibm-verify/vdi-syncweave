/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.IOException;
import java.util.Locale;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;

/**
 * The class ADChangelogConnector represents the Active Directory Changelog
 * connector class that will be accessed by IBM Tivoli Directory Integrator. It
 * extends the LDAP connector class (LDAPConnector) and overrides some of its
 * methods to implement AD-specific functionality.
 */
public class ADChangelogConnectorv2 extends LDAPConnector implements
		ConnectorInterface, ChangelogInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "adchangedetection";

	// connector parameters names
	/**
	 * Parameter Name: {@value #PARAM_LDAP_SEARCH_BASE}
	 */
	public static final String PARAM_LDAP_SEARCH_BASE = "ldapSearchBase";

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
	 * Parameter Name: {@value #PARAM_PAGE_SIZE}
	 */
	public static final String PARAM_PAGE_SIZE = "pageSize";

	/**
	 * Parameter Name: {@value #PARAM_USE_NOTIFICATIONS}
	 */
	public static final String PARAM_USE_NOTIFICATIONS = "useNotifications";

	// start at predefined values
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

	/**
	 * Default value used for page size.
	 */
	public static final int PAGE_SIZE_DEFAULT_VALUE = 500;

	// USN properties file parameters
	/**
	 * USN property file parameter: {@link #PROP_START_USN}
	 */
	public static final String PROP_START_USN = "START_USN";

	/**
	 * Search filter used when performing searches with the "LDAP SERVER
	 * NOTIFICATION" control.
	 */
	private static final String LDAP_SEARCH_FILTER = "objectClass=*";

	/**
	 * Synchronization session's start USN.
	 */
	private long mStartUsn = 0;

	/**
	 * Stores the "Sleep Interval" Connector parameter.
	 */
	private long mSleepInterval = 0;

	/**
	 * Stores the "Timeout" Connector parameter.
	 */
	private long mTimeout = 0;

	/**
	 * Stores the "Page Size" Connector parameter.
	 */
	private int mPageSize = 0;

	/**
	 * Stores the name of the persistent parameter storing the USN values.
	 */
	private String mUsnStoreParamName = null;

	/**
	 * Specifies a start position (including end of data). Only used when the
	 * persistent parameter is not found in the System Store.
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
	 * Request control for showing deleted objects.
	 */
	private Control mShowDeletedControl = null;

	/**
	 * Request control for showing deleted objects.
	 */
	private Control mServerNotificationControl = null;

	/**
	 * Request control for paged searches.
	 */
	private Control mPagedResultsControl = null;

	/**
	 * Result returned from search operation.
	 */
	private NamingEnumeration<SearchResult> mResults = null;

	/**
	 * The filter expression to use for search.
	 */
	private String mFilter = null;
	/**
	 * Stores maximum value of usnCommited attribute
	 */
	private long mHighestCommittedUsn = 0;

	/**
	 * Stores maximum value of usnChanged attribute
	 */
	private long mHighestUsnChanged = 0;

	/**
	 * Paged controls.
	 */
	private Control[] mControlsPaged = null;

	/**
	 * Controls where "Page Size" Connector parameter equals 0.
	 */
	private Control[] mControlsNoPaged = null;

	/**
	 * Indicates whether the state should be saved after read.
	 */
	private boolean mAfterRead = true;
	/**
	 * This constant is usually used to specify that the StateKey should be
	 * persisted after each received entry.
	 */
	private int mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_AFTER_READ;

	/**
	 * Flag for using Notifications.
	 */
	private boolean mUseNotifications = false;
	
	/**
	 * Default property store instance. Cache it as an instance variable to
	 * improve performance.
	 */
	private PropertyStore defaultPropStore = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Calls the super constructor and assigns supported Connector modes.
	 */
	public ADChangelogConnectorv2() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Reads connector parameters' values and prepares LDAP search constraints.
	 * 
	 * @param aObj
	 *            This parameter is usually null but can be any type of object
	 *            the caller chooses to pass on. Normally the parameter is some
	 *            kind of input stream or Reader object.
	 * 
	 * @throws Exception
	 *             If invalid Connector parameter values are supplied.
	 */
	public void initialize(Object aObj) throws Exception {
		Trace.entrymin(this, "initialize");
		// Specify to the JNDI library that the "objectGUID" and "objectSid"
		// attributes must be transfered as binary,
		// and not as ASCII data
		String biAttr = getParam("ldapBinaryAttributes");
		if (biAttr == null || biAttr.trim().length() == 0) {
			setParam("ldapBinaryAttributes", "objectGUID objectSid");
		} else {
			if (biAttr.indexOf("objectGUID") < 0) {
				biAttr = biAttr + " objectGUID";
			}
			if (biAttr.indexOf("objectSid") < 0) {
				biAttr = biAttr + " objectSid";
			}
			setParam("ldapBinaryAttributes", biAttr);
		}

		// initialize LDAP Connector
		super.initialize(aObj);

		// read USN state persistent parameter name
		mUsnStoreParamName = getParam(PARAM_USN_STORE_PARAM_NAME);
		if (mUsnStoreParamName != null
				&& mUsnStoreParamName.trim().length() == 0) {
			mUsnStoreParamName = null;
		}
		if (mUsnStoreParamName == null) {
			if (getParam("usnFileName") != null) {
				logmsg(sResHash.getString("CONNECTOR.ADCHGLG.USNFILENAME.WARN"));
			}
			String funcmsg = sResHash
					.getString("CONNECTOR.ADCHGLG.MISSING.ITERATORSTATEKEY.WARN");
			logmsg(funcmsg);
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
			Trace.exception(this, "initialize", e, sResHash.getString(
					"CONNECTOR.ADCHGLG.INVALID.SLEEP.INTERVAL.VALUE.SPECIFIED",
					new Object[] { sleepIntervalStr,
							Long.valueOf(mSleepInterval) }));
			logmsg(sResHash.getString("CONNECTOR.ADCHGLG.SLEEPINT.WARN",
					new Object[] { sleepIntervalStr, "" + mSleepInterval }));
		}
		if (mSleepInterval < 0 || (mSleepInterval * 1000) < 0) {
			mSleepInterval = 0l;
			logmsg(sResHash.getString("CONNECTOR.ADCHGLG.SLEEPINT.WARN",
					new Object[] { sleepIntervalStr, "" + mSleepInterval }));
		}

		// read, parse and store the "Timeout" parameter
		String timeoutStr = getParam(PARAM_TIMEOUT);
		try {
			mTimeout = Long.parseLong(timeoutStr);
		} catch (NumberFormatException e) {
			mTimeout = 5l;
			Trace.exception(this, "initialize", e, sResHash.getString(
					"CONNECTOR.ADCHGLG.INVALID.TIMEOUT.VALUE.SPECIFIED",
					new Object[] { timeoutStr, Long.valueOf(mTimeout) }));
			logmsg(sResHash.getString("CONNECTOR.ADCHGLG.TIMEOUT.WARN",
					new Object[] { timeoutStr, Long.valueOf(mTimeout) }));
		}
		if (mTimeout < 0 || (mTimeout * 1000) < 0) {
			mTimeout = 0l;
			logmsg(sResHash.getString("CONNECTOR.ADCHGLG.TIMEOUT.WARN",
					new Object[] { timeoutStr, Long.valueOf(mTimeout) }));
		}

		// read, parse and store the "Page Size" parameter
		mPageSize = PAGE_SIZE_DEFAULT_VALUE;
		String pageSizeStr = getParam(PARAM_PAGE_SIZE);
		if (pageSizeStr != null && pageSizeStr.trim().length() > 0) {
			try {
				mPageSize = Integer.parseInt(pageSizeStr);
			} catch (NumberFormatException e) {
				mPageSize = 0;
				Trace.exception(this, "initialize", e, sResHash.getString(
						"CONNECTOR.ADCHGLG.INVALID.PAGE.SIZE.VALUE.SPECIFIED",
						pageSizeStr));
				logmsg(sResHash.getString("CONNECTOR.ADCHGLG.PAGESIZE.WARN",
						pageSizeStr));
			}
			if (mPageSize < 0) {
				logmsg(sResHash.getString("CONNECTOR.ADCHGLG.NEGPAGESIZE.WARN",
						"" + mPageSize));
				mPageSize = 0;
			}
		}

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

		mUseNotifications = Boolean.valueOf(getParam(PARAM_USE_NOTIFICATIONS))
				.booleanValue();
		if (mUseNotifications) {
			mServerNotificationControl = new ServerNotificationControl();
		}

		// define search constraints for use in search operations
		mSearchConstraints = new SearchControls();
		mSearchConstraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		// define request controls for sending to Active Directory
		mSortUsnChangedControl = new SortControl(
				new String[] { AD_ATTR_USN_CHANGED }, Control.CRITICAL);
		mShowDeletedControl = new ShowDeletedControl();
		if (mPageSize > 0) {
			mPagedResultsControl = new PagedResultsControl(mPageSize, true);
			mControlsPaged = new Control[] { mShowDeletedControl,
					mSortUsnChangedControl, mPagedResultsControl };
		} else {
			mControlsNoPaged = new Control[] { mShowDeletedControl,
					mSortUsnChangedControl };
		}
		
		defaultPropStore = StoreFactory.getDefaultPropertyStore();
		
		Trace.exitmin(this, "initialize");
	}

	/**
	 * Retrieves USN value from the given object and sets it for use by the
	 * Connector.
	 * 
	 * @param aUsnValue
	 *            The object that contains the USN value.
	 * @throws Exception
	 *             if the USN value cannot be retrieved from the given object.
	 */
	private void getUsnValueFromObject(Object aUsnValue) throws Exception {
		Trace.entrymin(this, "getUsnValueFromObject", aUsnValue);
		if (aUsnValue == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.ADCHGLG.USN.IS.NULL"));
		}

		Object usnValue = aUsnValue;
		if (aUsnValue instanceof Entry) {
			usnValue = ((Entry) aUsnValue).getObject(PROP_START_USN);
		}

		if (usnValue instanceof String) {
			String usnStrValue = (String) usnValue;
			if (usnStrValue.equalsIgnoreCase(START_AT_END_OF_DATA)) {
				logmsg(sResHash
						.getString("CONNECTOR.ADCHGLG.START.AT.END.OF.DATA"));

				try {
					mStartUsn = getHighestCommittedUsn() + 1;
				} catch (Exception e) {
					logmsg(sResHash
							.getString(
									"CONNECTOR.ADCHGLG.CANT.FIND.HIGHEST.COMMITTED.USN",
									e.toString()));
					throw e;
				}
			} else {
				try {
					mStartUsn = Long.parseLong(usnStrValue);
				} catch (NumberFormatException e) {
					throw new Exception(sResHash.getString(
							"CONNECTOR.ADCHGLG.INVALID.NUMERIC.USN",
							usnStrValue));
				}
			}
		} else if (usnValue instanceof Number) {
			mStartUsn = ((Number) usnValue).longValue();
		} else {
			throw new Exception(sResHash.getString(
					"CONNECTOR.ADCHGLG.USN.IS.INVALID.TYPE", usnValue
							.getClass().getName()));
		}
		Trace.exitmin(this, "getUsnValueFromObject");
	}

	/**
	 * Reads start USN values. Sources for start USN values are checked in this
	 * order:
	 * <p>
	 * (1) System Store parameter;
	 * <p>
	 * (2) If the specified System Store parameter is not found in the System
	 * Store, the "Start at" value is used.
	 * 
	 * @throws Exception
	 *             if the USN values cannot be retrieved or other type of error
	 *             occurs.
	 */
	protected void getStartUsnValues() throws Exception {
		Trace.entrymax(this, "getStartUsnValues");
		// Use the System Store
		Object usnStoreValue = null;
		if (mUsnStoreParamName != null) {
			try {
				usnStoreValue = defaultPropStore.getProperty(mUsnStoreParamName);
			} catch (Exception e) {
				logmsg(sResHash.getString(
						"CONNECTOR.ADCHGLG.ERROR.ACCESS.SYSTEM.STORE", e
								.toString()));
				throw e;
			}
		}

		// get start USN values from the Persistent Parameter Store
		if (usnStoreValue != null) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.ADCHGLG.READ.USN.VALUES.FROM.SYSTEM.STORE",
						mUsnStoreParamName));
			}
			getUsnValueFromObject(usnStoreValue);
		} else { // if the persistent parameter is not found, use the "start
			// at" value
			logmsg(sResHash.getString(
					"CONNECTOR.ADCHGLG.PARAMETER.NOT.FOUND.IN.SYSTEM.STORE",
					mUsnStoreParamName));
			if (mStartAt == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.ADCHGLG.START.AT.PARAMETER.NOT.SPECIFIED"));
			}
			getUsnValueFromObject(mStartAt);
		}
		Trace.exitmax(this, "getStartUsnValues");
	}

	/**
	 * Reads the initial USN value and makes necessary adjustments.
	 * 
	 * @throws Exception
	 *             Exception if the USN values cannot be retrieved or other type
	 *             of error occurs.
	 */
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		getStartUsnValues();

		// debug start USN values
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.ADCHGLG.START.USN.VALUE",
					String.valueOf(mStartUsn)));
		}
		// handle special cases
		if (mStartUsn < 0) {
			logmsg(sResHash.getString(
					"CONNECTOR.ADCHGLG.INVALID.PROP.START.USN.VALUE",
					new Object[] { Long.valueOf(mStartUsn) }));
			mStartUsn = 0;
		}
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * Retrieves the highest committed USN from Active Directory.
	 * 
	 * @return The Active Directory's "highestCommittedUsn" attribute value.
	 * @throws Exception
	 *             If cannot retrieve the highest committed USN number.
	 */
	protected long getHighestCommittedUsn() throws Exception {
		Trace.entrymax(this, "getHighestCommittedUsn");
		long highestCommittedUsn = -1;
		getLdapContext().setRequestControls(new Control[] {});
		Attributes attrs = getLdapContext().getAttributes("");
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs
					.get(AD_ATTR_HIGHEST_COMMITTED_USN);
			if (attr != null) {
				Object obj = attr.get();
				highestCommittedUsn = new Long(obj.toString()).longValue();
			}
		}

		if (highestCommittedUsn == -1) {
			String funcmsg = sResHash
					.getString("CONNECTOR.ADCHGLG.COULD.NOT.RETRIEVE.HIGHEST.COMMITTED.USN.NUMBER");
			logmsg(funcmsg);
			throw new Exception(funcmsg);
		}

		Trace.exitmax(this, "getHighestCommittedUsn");

		return highestCommittedUsn;
	}

	/**
	 * Inserts leading zeros. For example, if the number 21 is required to be
	 * represented by exactly 4 digits, then the call "insertLeadingZeros("21",
	 * 4)" will return "0021". If the number is already represented by the
	 * required number of digits or more, no leading zeros will be inserted.
	 * 
	 * @param aStrNumber
	 *            the String representation of a number.
	 * @param aRequiredDigits
	 *            the number of digits required to represent the number.
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
	 * @return The hexadecimal String representation of the binary GUID.
	 */
	protected String binaryGUIDtoString(byte[] aBinaryData) {
		Trace.entrymin(this, "binaryGUIDtoString");
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

		Trace.exitmin(this, "binaryGUIDtoString");
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
		Trace.entrymax(this, "addGUIDStrAttribute", aEntry);
		Attribute attr = aEntry.getAttribute(AD_ATTR_OBJECT_GUID);
		if (attr != null) {
			byte[] binaryData = (byte[]) attr.getValue(0);
			String stringGUID = binaryGUIDtoString(binaryData);
			aEntry.setAttribute(AD_ATTR_OBJECT_GUID_STR, stringGUID);
		}
		Trace.exitmax(this, "addGUIDStrAttribute");
	}

	/**
	 * Retrieves an Attribute value as long.
	 * 
	 * @param aEntry
	 *            The Entry whose Attribute will be read.
	 * @param aAttributeName
	 *            The name of the Attribute to read.
	 * @return The "long" value of the Attribute; if the value is not a long
	 *         0 is returned.
	 */
	private long getAttributeLongValue(Entry aEntry, String aAttributeName) {
		try {
			return Long.parseLong(aEntry.getString(aAttributeName));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Retrieves next Entry from result set.
	 * 
	 * @return the next Entry from the result set.
	 * @throws IOException
	 *             If an error was encountered while encoding the supplied
	 *             arguments into a control.
	 * @throws NamingException
	 *             If an error occurred while setting the request controls or if
	 *             a naming exception is encountered while attempting to
	 *             determine whether there is another element in the
	 *             enumeration. See NamingException and its subclasses for the
	 *             possible naming exceptions
	 */
	protected Entry getNextSearchResult() throws NamingException, IOException {
		Trace.entrymax(this, "getNextSearchResult");
		if (mResults == null) {
			return null;
		}

		Entry entry = null;

		while (entry == null && mResults != null) {
			boolean hasMore = false;
			try {
				hasMore = mResults.hasMore();
			} catch (PartialResultException e) {
				Trace.exception(this, "getNextSearchResult", e,
						"PartialResultException caught:");
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.ADCHGLG.PARTIAL.RESULT.EXCEPTION", e));
				}
			}

			if (hasMore) {
				entry = entry2at(mResults.next());
			} else {
				mResults = getNextPagedResult();
			}
		} 

		if (entry != null) {
			addGUIDStrAttribute(entry);

			if (Boolean.valueOf(entry.getString(AD_ATTR_IS_DELETED))) {
				entry.addAttributeValue(ATTR_CHANGE_TYPE, CHANGE_TYPE_DELETE);
				entry.setOp(Entry.OP_DEL);
			} else {
				long usnCreated = getAttributeLongValue(entry, AD_ATTR_USN_CREATED);

				// when add and modify occurred right after another
				// only add will be reported
				if (usnCreated >= mStartUsn) {
					entry.addAttributeValue(ATTR_CHANGE_TYPE, CHANGE_TYPE_ADD);
					entry.setOp(Entry.OP_ADD);
				} else {
					entry.addAttributeValue(ATTR_CHANGE_TYPE, CHANGE_TYPE_MODIFY);
					entry.setOp(Entry.OP_MOD);
				}
			}

			long usnChanged = getAttributeLongValue(entry, AD_ATTR_USN_CHANGED);

			if (usnChanged > mHighestUsnChanged) {
				mHighestUsnChanged = usnChanged;
			}
			// if usnChanged is greater than mHighestCommittedUsn then this
			// change occurred after initial search request
			if (usnChanged > mHighestCommittedUsn) {
				// we got an entry so at least one change has been made
				mStartUsn++;
			} else {
				mStartUsn = usnChanged + 1;
			}
		} else {
			// session finished

			// if change occurred after initial search request
			// make sure mHighestCommittedUsn is up to date
			if (mHighestCommittedUsn < mHighestUsnChanged) {
				mHighestCommittedUsn = mHighestUsnChanged;
			}
			if (mHighestCommittedUsn >= mStartUsn) {
				mStartUsn = mHighestCommittedUsn + 1;
			}
		}
		if (mAfterRead) {
			storeUSNForNextSynch();
		}
		Trace.exitmax(this, "getNextSearchResult", entry);
		return entry;
	}

	private NamingEnumeration<SearchResult> getNextPagedResult() {
		if (mPageSize <=0 )
			return null;

		Control[] controls = null;
		try {
			controls = getLdapContext().getResponseControls();
		} catch (NamingException e1) {
			return null;
		}

		if (controls == null)
			return null;

		byte[] cookie = null;
		for (int i = 0; i < controls.length && cookie == null; i++) {
			if (controls[i] instanceof PagedResultsResponseControl) {
				cookie = ((PagedResultsResponseControl) controls[i]).getCookie();
			}
		}

		if (cookie == null)
			return null;

		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.ADCHGLG.GET.NEXT.PAGE", mFilter));
		}

		try {
			getLdapContext().setRequestControls(
					new Control[] {mShowDeletedControl,
							mSortUsnChangedControl,
							new PagedResultsControl(mPageSize, cookie, Control.CRITICAL) });
			return getLdapContext().search(getParam(PARAM_LDAP_SEARCH_BASE), 
					mFilter, mSearchConstraints);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Retrieves the next "changed" object from Active Directory.
	 * 
	 * @return The next "changed" Entry object.
	 * @throws Exception
	 *             If retrieving the next "changed" Entry fails.
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymin(this, "getNextEntry");

		Entry entry = getNextSearchResult();
		if (entry != null) {
			return entry;
		}

		boolean isTimeout = false;

		long startTime = System.currentTimeMillis();
		long currentTime;

		do {
			mHighestCommittedUsn = getHighestCommittedUsn();
			if (mHighestCommittedUsn > mHighestUsnChanged) {
				mHighestUsnChanged = mHighestCommittedUsn;
			}

			if (mUseNotifications && mHighestCommittedUsn < mStartUsn) {
				blockForNewChanges();
			} else {
				while ((mHighestCommittedUsn < mStartUsn) && (!isTimeout)) {
					// sleep and timeout
					if (mSleepInterval > 0) {
						// make sure that the thread will not sleep, if its
						// sleeping would delay
						// the requested Conntector's timing-out
						currentTime = System.currentTimeMillis();
						if ((mTimeout == 0)
								|| ((mTimeout > 0) && ((currentTime + mSleepInterval * 1000) - startTime) < (mTimeout * 1000))) {
							Thread.sleep(mSleepInterval * 1000);
							mHighestCommittedUsn = getHighestCommittedUsn();
							if (mHighestCommittedUsn > mHighestUsnChanged) {
								mHighestUsnChanged = mHighestCommittedUsn;
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
					if ((isTimeout) && (debugMode())) {
						debug(sResHash
								.getString("CONNECTOR.ADCHGLG.TIMEOUT.WAITING.FOR.NEXT.CHANGED.ENTRY"));
					}
				}
			}

			if (!isTimeout) {
				mFilter = "(" + AD_ATTR_USN_CHANGED + ">=" + mStartUsn + ")";
				if (debugMode()) {
					debug(sResHash.getString(
							"CONNECTOR.ADCHGLG.LDAP.SEARCH.QUERY", mFilter));
				}

				if (mPageSize > 0) {
					getLdapContext().setRequestControls(mControlsPaged);
				} else {
					getLdapContext().setRequestControls(mControlsNoPaged);
				}
				mResults = getLdapContext().search(
						getParam(PARAM_LDAP_SEARCH_BASE), mFilter,
						mSearchConstraints);
				entry = getNextSearchResult();
			}
		} while ((entry == null) && (!isTimeout));

		Trace.exitmin(this, "getNextEntry", entry);

		return entry;
	}

	/**
	 * Retrieves USN value as properties.
	 * 
	 * @return an Entry containing the current Connector's USN values as
	 *         properties. Such Entries are used for storage in the persistent
	 *         store.
	 */
	protected Entry packUsnValues() {
		Trace.entrymax(this, "packUsnValues");
		Entry usnEntry = new Entry();
		usnEntry.setAttribute(PROP_START_USN, Long.valueOf(mStartUsn));
		Trace.exitmax(this, "packUsnValues", usnEntry);
		return usnEntry;
	}

	/**
	 * Stores the USN values for the next synchronization.
	 */
	protected void storeUSNForNextSynch() {
		Trace.entrymax(this, "storeUSNForNextSynch");
		if (mUsnStoreParamName != null) {
			try {
				defaultPropStore.updateProperty(mUsnStoreParamName, String.valueOf(mStartUsn), true);
			} catch (Exception e) {
				Trace
						.exception(
								this,
								"storeUSNForNextSynch",
								e,
								sResHash
										.getString("CONNECTOR.ADCHGLG.ERROR.COULD.NOT.STORE.THE.USN.VALUE"));
				logmsg(sResHash
						.getString(
								"CONNECTOR.ADCHGLG.COULD.NOT.STORE.THE.USN.VALUE.IN.SYSTEM.STORE",
								e));
				logmsg(sResHash.getString(
						"CONNECTOR.ADCHGLG.CURRENT.USN.VALUE.IS", String
								.valueOf(mStartUsn)));
			}
		}
		Trace.exitmax(this, "storeUSNForNextSynch");
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
		private static final long serialVersionUID = -3108174229882449494L;

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
	 * Retrieves USN synchronization value.
	 * 
	 * @return the current USN synchronization value.
	 */
	public long getUsnValue() {
		return mStartUsn;
	}

	/**
	 * Sets Connector's current USN synchronization value.
	 * 
	 * @param aUsnValue
	 *            the new USN value.
	 */
	public void setUsnValue(long aUsnValue) {
		mStartUsn = aUsnValue;
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
		if (!mAfterRead) {
			storeUSNForNextSynch();
		}
	}

	/**
	 * Retrives synchronization session's start USN as Object.
	 * 
	 * @return the synchronization session's start USN as Long Object.
	 * @throws Exception -
	 *             never
	 */
	public Object getStateKeyObject() throws Exception {
		return Long.valueOf(mStartUsn);
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Performs a search request to Active Directory that blocks until new
	 * change happens in the Directory.
	 * 
	 * @throws NamingException
	 *             If an error occurs while executing the search operation.
	 */
	private void blockForNewChanges() throws NamingException {
		if (debugMode()) {
			debug(sResHash
					.getString("CONNECTOR.ADCHGLG.BLOCKING.FOR.NEW.CHANGES"));
		}
		getLdapContext().setRequestControls(
				new Control[] { mServerNotificationControl });
		getLdapContext().search(getParam(PARAM_LDAP_SEARCH_BASE),
				LDAP_SEARCH_FILTER, mSearchConstraints);
	}

	/**
	 * The class ServerNotificationControl represents the "ldap server
	 * notification" LDAPv3 request control. This control register the client to
	 * be notified when changes are made to an object in Active Directory.
	 */
	private static class ServerNotificationControl implements Control {

		/**
		 * Unique ID used for deserialization.
		 */
		private static final long serialVersionUID = 1241022422512332764L;

		/**
		 * The code of the "notification" request control
		 */
		public final static String LDAP_SERVER_NOTIFICATION_OID = "1.2.840.113556.1.4.528";

		/**
		 * @return an empty byte array - no parameters needed
		 */
		public byte[] getEncodedValue() {
			// an empty byte array is returned - no parameters needed
			return new byte[] {};
		}

		/**
		 * @return the {@link #LDAP_SERVER_NOTIFICATION_OID} value.
		 */
		public String getID() {
			return LDAP_SERVER_NOTIFICATION_OID;
		}

		/**
		 * The control is critical.
		 * 
		 * @return the value of the {@link Control#CRITICAL} constant - true.
		 */
		public boolean isCritical() {
			return Control.CRITICAL;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {

		if (mResults != null) {
			try {
				mResults.close();
			} catch (NamingException ignore) {
			}
			mResults = null;
		}

		super.terminate();
	}

}
