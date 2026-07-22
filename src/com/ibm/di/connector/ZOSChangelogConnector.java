/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import javax.naming.directory.Attributes;
import javax.naming.ldap.Control;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.LDIFParser;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;

/**
 * The IDSChangelogConnector provides a way to to detect changes in a zOS
 * Directory Server. The Connector connects to the underline directory through
 * the JNDI interface and gets the changes done on a specific context. The
 * Connector regularly saves current state into the System Store to avoid
 * duplications when retrieving Entries.
 */
public class ZOSChangelogConnector extends ChangelogConnector implements
		ConnectorInterface, ChangelogInterface {

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
	 * Parameter Name: {@value #PARAM_STORE_PARAM_NAME}
	 */
	public static final String PARAM_STORE_PARAM_NAME = "iteratorStateKey";

	/**
	 * Parameter Name: {@value #PARAM_START_AT}
	 */
	public static final String PARAM_START_AT = "nsChangenumber";

	/**
	 * Parameter Name: {@value #PARAM_SLEEP_INTERVAL}
	 */
	public static final String PARAM_SLEEP_INTERVAL = "nsSleepInterval";

	/**
	 * Parameter Name: {@value #PARAM_TIMEOUT}
	 */
	public static final String PARAM_TIMEOUT = "nsTimeout";

	/**
	 * start at predefined values
	 */
	private static final String START_AT_END_OF_DATA = "EOD";

	/**
	 * Attribute name: {@value #ATTRIBUTE_CPR}
	 */
	public static final String ATTRIBUTE_CPR = "changeNumber";

	/**
	 * Attribute name: {@value #ATTRIBUTE_CHANGENUMBER}
	 */
	public static final String ATTRIBUTE_CHANGENUMBER = "changenumber";
	/**
	 * Attribute name: last change number
	 */
	private static final String ATTR_LAST_CHANGENUMBER = "lastchangenumber";
	/**
	 * This is the changenumber to start reading at, EOD means position at end
	 * of changelog. Only used when there is no stored value in the PPS.
	 */
	private long mChangenumber = 1;
	/**
	 * Number of seconds between each poll
	 */
	private int mSleepInterval = 60;
	/**
	 * Max number of seconds before returning EOF (0 = Wait Forever)
	 */
	private int mTimeout = 0;

	/**
	 * {@link LDIFParser}
	 */
	private LDIFParser mLdifParser;
	/**
	 * Change number property name.
	 */
	private String mCNPropName;
	/**
	 * Indicates whether the state should be saved after read.
	 */
	private boolean mAfterRead = true;
	/**
	 * Specifies that the StateKey should be persisted after each received
	 * entry.
	 */
	private int mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_AFTER_READ;
	/**
	 * Stores the last change number.
	 */
	private long mLastChangeNumber = 0;
	
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
		sResHash = new ResourceHash("zoschangelog");
	}

	/**
	 * Constructs this object and sets it supported mode.
	 */
	public ZOSChangelogConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Initializes the connector. All the configuration properties are
	 * considered and applied before a connection is made. After everything is
	 * set the connection is established.
	 * 
	 * @param aObject
	 *            ignored.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initialize(Object aObject) throws Exception {

		super.initialize(aObject);

		String str = getParam(PARAM_TIMEOUT);
		if (str != null) {
			mTimeout = Integer.parseInt(str);
		}
		str = getParam(PARAM_SLEEP_INTERVAL);
		if (str != null) {
			mSleepInterval = Integer.parseInt(str);
		}

		mLdifParser = new LDIFParser();
		mLdifParser.initParser();

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
		
		defaultPropStore = StoreFactory.getDefaultPropertyStore();
	}

	/**
	 * Prepares the connector for iterating over changed objects.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void selectEntries() throws Exception {

		String startAt = getParam(PARAM_START_AT);
		if (startAt != null && startAt.length() > 0) {
			try {
				if (START_AT_END_OF_DATA.equalsIgnoreCase(startAt)) {
					mChangenumber = getLastChangeNumber() + 1;
				} else {
					mChangenumber = Long.parseLong(startAt);
				}
			} catch (Throwable t) {
				logmsg(sResHash.getString("CANNOT.SET.STARTING.CHANGENUMBER",
						startAt));
			}
		}

		mCNPropName = getParam(PARAM_STORE_PARAM_NAME);
		if (mCNPropName != null && mCNPropName.trim().length() > 0) {
			Object n = StoreFactory.getDefaultPropertyStore().getProperty(
					mCNPropName);
			if (n instanceof Long) {
				mChangenumber = ((Long) n).longValue();
			} else if (n != null) {
				mChangenumber = Integer.parseInt(n.toString());
			}
			logmsg(sResHash.getString("STARTING.CHANGENUMBER", new Object[] {
					"" + mChangenumber, mCNPropName }));
		} else {
			mCNPropName = null;
			logmsg(sResHash.getString("EMPTY.ITERATORSTATEKEY"));
		}
	}

	/**
	 * This method will get the next change if the number of changes is smaller
	 * than the actual changes, that were done on the zOS Directory Server. If
	 * no more changes are available this method will sleep as much as the user
	 * have defined and when it wakes up it will check if any change was made.
	 * 
	 * @return the changes as an Entry object
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {

		Entry entry = null;

		boolean isTimeout = false;
		long startTime = System.currentTimeMillis();
		long currentTime;

		do {
			mLastChangeNumber = getLastChangeNumber();

			while ((mLastChangeNumber < mChangenumber) && (!isTimeout)) {
				// sleep and timeout
				if (mSleepInterval > 0) {
					// make sure that the thread will not sleep, if its sleeping
					// would delay
					// the requested Conntector's timing-out
					currentTime = System.currentTimeMillis();
					if ((mTimeout == 0)
							|| ((mTimeout > 0) && ((currentTime + mSleepInterval * 1000) - startTime) < (mTimeout * 1000))) {
						Thread.sleep(mSleepInterval * 1000);
						mLastChangeNumber = getLastChangeNumber();
					} else {
						isTimeout = true;
					}
				} else {
					mLastChangeNumber = getLastChangeNumber();	
				}

				// check if the "Timeout" has expired
				if ((mTimeout > 0) && (!isTimeout)) {
					currentTime = System.currentTimeMillis();
					if ((currentTime - startTime) > (mTimeout * 1000)) {
						isTimeout = true;
					}
				}
				if (debugMode() && isTimeout) {
					debug(sResHash.getString("TIMEOUT.WAITING"));
				}
			}

			if (!isTimeout) {

				String ldapparam = getParam(PARAM_LDAP_SEARCH_BASE);
				String cn = ATTRIBUTE_CHANGENUMBER + "=" + mChangenumber + ","
						+ ldapparam;
				if (debugMode()) {
					debug(sResHash.getString("READ.CHANGENUMBER", new Object[] {
							"" + mChangenumber, ldapparam }));
				}
				// Read next changelog entry
				entry = findEntry(new SearchCriteria("$dn",
						SearchCriteria.EXACT, cn));
				mChangenumber++;
				if (mAfterRead) {
					storeChangenumberForNextSynch();
				}
				if (entry != null) {
					if (debugMode()) {
						debug(sResHash.getString("GOT.NEXT.CHANGENUMBER",
								new Object[] { "" + mChangenumber, ldapparam }));
					}
					return parseEntry(entry);
				} else {
					logmsg(sResHash.getString("ENTRY.NOT.FOUND.IN.CHANGELOG",
							new Object[] { "" + mChangenumber, ldapparam }));
					logmsg(sResHash.getString("SKIPPING.AND.TRY.NEXT", ""
							+ mChangenumber));
				}
			}
		} while ((entry == null) && (!isTimeout));

		return entry;
	}

	/**
	 * Retrieves last change number.
	 * @return the last change number
	 * @throws Exception
	 *             if an error occurs.
	 */
	private long getLastChangeNumber() throws Exception {
		long lastChangeNumber = -1;
		getLdapContext().setRequestControls(new Control[] {});
		Attributes attrs = getLdapContext().getAttributes("");
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs
					.get(ATTR_LAST_CHANGENUMBER);
			if (attr != null) {
				Object obj = attr.get();
				lastChangeNumber = Long.valueOf(obj.toString()).longValue();
			}
		}
		if (lastChangeNumber == -1) {
			String funcmsg = sResHash.getString("ERROR.LAST.CHANGENUMBER");
			logmsg(funcmsg);
			throw new Exception(funcmsg);
		}

		return lastChangeNumber;
	}

	/**
	 * Stores the change number value
	 */
	private void storeChangenumberForNextSynch() {
		try {
			if (mCNPropName != null) {
				defaultPropStore.updateProperty(mCNPropName, Long.valueOf(mChangenumber), true);
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("COULD.NOT.STORE", new Object[] {
					"" + mChangenumber, e.toString() }));
		}
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
			storeChangenumberForNextSynch();
		}
	}

	/**
	 * Retrieves state key.
	 * @return the change number wrapped as a {@link Long} object.
	 * @throws Exception -
	 *             never
	 */
	public Object getStateKeyObject() throws Exception {
		return Long.valueOf(mChangenumber);
	}

	/**
	 * Version information.
	 * @return the version information.
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * {@inheritDoc}
	 */
	public void reconnect(Object o) throws Exception {
		terminate();
		initialize(o);
	}
	/**
	 * Method for parsing and merging Changelog Entry attributes and changed
	 * attributes from the actual Directory Entry.
	 * <p>
	 * Note: The 'changes' attribute is optional for RACF LDAP changelog
	 * entries. Also the 'newrdn' attribute is missing for 'add' operations.
	 * Therefore there is no need to parse entries if both attributes are
	 * missing.
	 * 
	 * @param aEntry
	 *            the actual Directory Entry.
	 * 
	 * @return the result of the parsing and merging operations entry.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected Entry parseEntry(Entry aEntry) throws Exception {
		if (aEntry.getString("changes") == null && aEntry.getString("newrdn") == null) {
			return aEntry;
		}
		return super.parseEntry(aEntry);
	}

}
