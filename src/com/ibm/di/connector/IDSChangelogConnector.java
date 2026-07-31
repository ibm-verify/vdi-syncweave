/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.event.EventContext;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;
import com.ibm.ldap.bp.asn1.BERDecoder;
import com.ibm.ldap.bp.asn1.BEREncoder;

/**
 * The IDSChangelogConnector provides a way to to detect changes in a IBM Tivoli
 * Directory Server. The Connector connects to the underline directory through
 * the JNDI interface and gets the changes done on a specific context. The
 * Connector regularly saves current state into the System Store to avoid
 * duplications when retrieving Entries.
 */
public class IDSChangelogConnector extends ChangelogConnector implements
		ConnectorInterface, ChangelogInterface, UnsolicitedNotificationListener {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "idschglogconnector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

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
	 * Parameter Name: {@value #PARAM_PAGE_SIZE}
	 */
	public static final String PARAM_PAGE_SIZE = "pageSize";

	/**
	 * Parameter Name: {@value #PARAM_USE_NOTIFICATIONS}
	 */
	public static final String PARAM_USE_NOTIFICATIONS = "useNotifications";

	/**
	 * Parameter Name: {@value #PARAM_BATCH_RETRIEVAL}
	 */
	public static final String PARAM_BATCH_RETRIEVAL = "batchRetrieval";

	// start at predefined values
	/**
	 * Attribute name for starting at predefined value - EOD
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
	 * Private attribute name - last change number
	 */
	private static final String IDS_ATTR_LAST_CHANGENUMBER = "lastchangenumber";

	/**
	 * The default value used for page size.
	 */
	public static final int PAGE_SIZE_DEFAULT_VALUE = 500;

	/**
	 * The id of the specific control supported by IBM TDS, used for registering
	 * to the server.
	 */
	public final static String EVT_REG_OID = "1.3.18.0.2.12.1";

	/**
	 * The id of the specific control supported by IBM TDS, used for
	 * unregistering from the server.
	 */
	public final static String EVT_UNREG_OID = "1.3.18.0.2.12.3";

	/**
	 * The type of change that we will get notified for.
	 */
	public final static int CHANGE_ALL = 15;

	private static final String TARGET_DN = "targetDn";

	/**
	 * This is the changenumber to start reading at, EOD means position at end
	 * of changelog. Only used when there is no stored value in the PPS.
	 */
	private long mChangenumber = 1;

	/**
	 * Number of seconds between each poll.
	 */
	private int mSleepInterval = 60;

	/**
	 * Max number of seconds before returning EOF (0 = Wait Forever)
	 */
	private int mTimeout = 0;

	/**
	 * Change number property name.
	 */
	private String mCNPropName;

	/**
	 * This parameter specifies the size of the pages IDS will return entries on
	 * (default value is 500)
	 */
	private int mPageSize = 0;

	/**
	 * Search constraints used in search operations
	 */
	private SearchControls mSearchConstraints;
	/**
	 * Request control to sort on a list of attributes.
	 */
	private Control mSortChangedControl = null;
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
	 * Specifies that the StateKey should be persisted after each received
	 * entry.
	 */
	private int mStateKeySaveMethod = ChangelogInterface.SAVE_STATE_AFTER_READ;

	/**
	 * Use notifications when waiting for changes. If set to 'true' Connector
	 * will not sleep or timeout
	 */
	private boolean mUseNotifications = false;

	/**
	 * Connector performs Incremental lookups when is 'false' and paged searches
	 * when is 'true'
	 */
	private boolean mBatchRetrieval = false;

	/**
	 * Stores the last change number.
	 */
	private long mLastChangeNumber = 0;

	/**
	 * Contains methods for registering/deregistering listeners to be notified
	 * of events fired when objects named in a context changes.
	 *
	 * @see EventContext
	 */
	private EventContext mEvtCtx = null;

	/**
	 * Used for thread safe modification.
	 */
	private Object lock = new Object();

	/**
	 * Notification received flag.
	 */
	private volatile boolean mNotificationReceived = false;

	/**
	 * Default property store instance. Cache it as an instance variable to
	 * improve performance.
	 */
	private PropertyStore defaultPropStore = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructs this object and sets it supported mode.
	 */
	public IDSChangelogConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * Initializes the connector. All the configuration properties are
	 * considered and applied before a connection is made. After everything is
	 * set the connection is established and the connector is registered for
	 * receiving change notification.
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

		// read, parse and store the "Page Size" parameter
		mPageSize = PAGE_SIZE_DEFAULT_VALUE;
		String pageSizeStr = getParam(PARAM_PAGE_SIZE);
		if (pageSizeStr != null && pageSizeStr.trim().length() > 0) {
			try {
				mPageSize = Integer.parseInt(pageSizeStr);
			} catch (NumberFormatException e) {
				mPageSize = 0;
				logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.PAGESIZE.WARN",
						pageSizeStr));
			}
			if (mPageSize < 0) {
				logmsg(sResHash.getString(
						"CONNECTOR.IDSCHGLG.NEGPAGESIZE.WARN", "" + mPageSize));
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
			// Add event listener
			mEvtCtx = (EventContext) getLdapContext().lookup(
					getParam(PARAM_LDAP_SEARCH_BASE));
			if (mEvtCtx == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.IDSCHGLG.NODIRCONEXT.EXCEP",
						getParam(PARAM_LDAP_SEARCH_BASE)));
			}
			mEvtCtx.addNamingListener("", SearchControls.SUBTREE_SCOPE, this);
			getLdapContext().extendedOperation(
					new EVTRequest(getParam(PARAM_LDAP_SEARCH_BASE),
							SearchControls.SUBTREE_SCOPE, CHANGE_ALL));
		}

		mBatchRetrieval = Boolean.valueOf(getParam(PARAM_BATCH_RETRIEVAL))
				.booleanValue();
		if (mBatchRetrieval) {
			// define search constraints for use in search operations
			mSearchConstraints = new SearchControls();
			mSearchConstraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			// define request controls for sending to IDS
			mSortChangedControl = new SortControl(
					new String[] { ATTRIBUTE_CHANGENUMBER }, Control.CRITICAL);
			if (mPageSize > 0) {
				mPagedResultsControl = new PagedResultsControl(mPageSize, true);
				mControlsPaged = new Control[] { mSortChangedControl,
						mPagedResultsControl };
			} else {
				mControlsNoPaged = new Control[] { mSortChangedControl };
			}
		}

		mCNPropName = getParam(PARAM_STORE_PARAM_NAME);
		if(mCNPropName == null || mCNPropName.isEmpty()) {
			mCNPropName = null;
			logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.EMPTY.ITERATORSTATEKEY"));
		} else {
			defaultPropStore = StoreFactory.getDefaultPropertyStore();
		}
	}

	/**
	 * Prepares the connector for listening to change notifications.
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
				logmsg(sResHash.getString(
						"CONNECTOR.IDSCHGLG.NOSTARTCHGNUMBER.INFO", startAt));
			}
		}
		if (mCNPropName != null) {
			Object n = defaultPropStore.getProperty(mCNPropName);
			if (n instanceof Long) {
				mChangenumber = ((Long) n).longValue();
			} else if (n != null) {
				mChangenumber = Integer.parseInt(n.toString());
			}
			logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.STARTCHANGE.INFO",
					new Object[] { "" + mChangenumber, mCNPropName }));
		}

	}

	/**
	 * This method will get the next change if the number of changes is smaller
	 * than the actual changes, that were done on the TDS. If no more changes
	 * are available this method will block until notified that a change was
	 * done.
	 *
	 * @return the changes as an Entry object
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {

		Entry entry = null;
		if (mBatchRetrieval) {
			entry = getNextSearchResult();
			if (entry != null) {
				return entry;
			}
		}
		boolean isTimeout = false;
		long startTime = System.currentTimeMillis();
		long currentTime;

		do {
			mLastChangeNumber = getLastChangeNumber();

			if (mUseNotifications && mLastChangeNumber < mChangenumber) {
				try {
					blockForNotifications();
				} catch (InterruptedException e) {
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.IDSCHGLG.THREAD.INTERUPTED", e.toString()));
					}
					isTimeout = true;
					break;
				}

			} else {
				while ((mLastChangeNumber < mChangenumber) && (!isTimeout)) {
					// sleep and timeout
					if (mSleepInterval > 0) {
						// make sure that the thread will not sleep, if its
						// sleeping would delay
						// the requested Conntector's timing-out
						currentTime = System.currentTimeMillis();
						if ((mTimeout == 0)
								|| ((mTimeout > 0) && ((currentTime + mSleepInterval * 1000) - startTime) < (mTimeout * 1000))) {
							Thread.sleep(mSleepInterval * 1000);
							mLastChangeNumber = getLastChangeNumber();
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
								.getString("CONNECTOR.IDSCHGLG.TIMEOUT.WAITING.FOR.NEXT.CHANGED"));
					}
				}
			}

			if (!isTimeout) {
				if (mBatchRetrieval) {
					mFilter = "(" + ATTRIBUTE_CHANGENUMBER + ">="
							+ mChangenumber + ")";

					if (debugMode()) {
						debug(sResHash
								.getString(
										"CONNECTOR.IDSCHGLG.LDAP.SEARCH.QUERY",
										mFilter));
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
				} else {
					String cn = ATTRIBUTE_CHANGENUMBER + "=" + mChangenumber
							+ "," + getParam(PARAM_LDAP_SEARCH_BASE);
					if (debugMode()) {
						debug(sResHash.getString("CONNECTOR.IDSCHGLG.READ", cn));
					}
					// Read next changelog entry
					entry = findEntry(new SearchCriteria("$dn",
							SearchCriteria.EXACT, cn));
					
					// Increment the changenumber only when
					// we are not using notifications,
					// or we have found an entry that is not null,
					// See DI01419 or APAR IO14037 for more information
					boolean increment = !mUseNotifications || entry!=null || mChangenumber <= mLastChangeNumber;
					if (increment) {
						mChangenumber++;                                    
					
						if (mAfterRead) {
							storeChangenumberForNextSynch();
						}
 					}	

					if (entry != null) {
						if (debugMode()) {
							debug(sResHash.getString("CONNECTOR.IDSCHGLG.GOT.NEXT.CHANGELOG", cn));
						}
						return parseEntry(entry);
					} else {
						logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.ENTRY.NOT.FOUND.IN.CHANGELOG", cn));
						if (increment)
							logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.SKIPPING.AND.TRYING.NEXT",
								String.valueOf(mChangenumber)));
					}
				}
			}
		} while ((entry == null) && (!isTimeout));
		return entry;
	}

	private void blockForNotifications() throws Exception {
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.IDSCHGLG.BLOCK.FOR.NOTIFICATIONS"));
		}
		while (!mNotificationReceived) {
			synchronized (lock) {
				if (!mNotificationReceived) {
					lock.wait(150000);
				}
			}
			if (!mNotificationReceived) {
				// Dummy lookup to force error if LDAP connection has been closed for any reason
				getLdapContext().lookup("");                            
			}
		}
		mNotificationReceived = false;
	}

	/**
	 * Retrieves the next entry from result search.
	 *
	 * @return the next entry
	 * @throws Exception
	 */
	private Entry getNextSearchResult() throws Exception {
		if (mResults == null) {
			return null;
		}
		Entry entry = null;
		boolean hasMore = false;
		byte[] cookie;

		do {
			cookie = null;
			try {
				hasMore = mResults.hasMore();
			} catch (PartialResultException e) {
				if (debugMode()) {
					debug(sResHash
							.getString(
									"CONNECTOR.IDSCHGLG.PARTIAL.RESULT.EXCEPTION.CAUGHT",
									e.toString()));
				}
			}

			if (hasMore) {
				SearchResult searchRes = mResults.next();
				entry = parseEntry(entry2at(searchRes));
			} else if (mPageSize > 0) {
				Control[] controls = getLdapContext().getResponseControls();
				if (controls != null) {
					for (int i = 0; i < controls.length; i++) {
						if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl pagedControl = (PagedResultsResponseControl) controls[i];
							cookie = pagedControl.getCookie();
							if (cookie != null) {
								break;
							}
						}
					}

					if (cookie != null) {
						if (debugMode()) {
							debug(sResHash
									.getString(
											"CONNECTOR.IDSCHGLG.GET.NEXT.PAGE",
											mFilter));
						}
						getLdapContext().setRequestControls(
								new Control[] {
										mSortChangedControl,
										new PagedResultsControl(mPageSize,
												cookie, Control.CRITICAL) });
						mResults = getLdapContext().search(
								getParam(PARAM_LDAP_SEARCH_BASE), mFilter,
								mSearchConstraints);
					}
				}
			}
		} while ((entry == null) && (cookie != null));

		if (entry != null) {
			long changed = getAttributeLongValue(entry, ATTRIBUTE_CHANGENUMBER);
			mChangenumber = changed + 1;
		} else {
			// session finished
			mResults = null;
			if (mLastChangeNumber >= mChangenumber) {
				mChangenumber = mLastChangeNumber + 1;
			}
		}
		if (mAfterRead) {
			storeChangenumberForNextSynch();
		}
		return entry;
	}

	/**
	 * Retrieves the last detected change number.
	 *
	 * @return the last change number.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private long getLastChangeNumber() throws Exception {
		long lastChangeNumber = -1;
		String [] attrID={IDS_ATTR_LAST_CHANGENUMBER};
		getLdapContext().setRequestControls(new Control[] {});
		Attributes attrs = getLdapContext().getAttributes("",attrID);
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs
					.get(IDS_ATTR_LAST_CHANGENUMBER);
			if (attr != null) {
				Object obj = attr.get();
				lastChangeNumber = new Long(obj.toString()).longValue();
			}
		}
		if (lastChangeNumber == -1) {
			logmsg(sResHash
					.getString("CONNECTOR.IDSCHGLG.COULD.NOT.RETRIEVE.LAST.CHANGE.NUMBER.I"));
			throw new Exception(
					sResHash
							.getString("CONNECTOR.IDSCHGLG.COULD.NOT.RETRIEVE.LAST.CHANGE.NUMBER.E"));
		}

		return lastChangeNumber;
	}

	/**
	 * Retrieves the long value of the attribute from the provided entry.
	 *
	 * @param aEntry
	 *            entry to read from
	 * @param aAttributeName
	 *            attribute name
	 * @return the parse value or '0' if parsing fails.
	 */
	private long getAttributeLongValue(Entry aEntry, String aAttributeName) {
		long value;
		try {
			value = Long.parseLong(aEntry.getString(aAttributeName));
		} catch (NumberFormatException e) {
			value = 0;
		}
		return value;
	}

	/**
	 * Stores the change number value
	 */
	private void storeChangenumberForNextSynch() {
		try {
			if (mCNPropName != null) {
				defaultPropStore.updateProperty(mCNPropName, Long
						.valueOf(mChangenumber), true);
			}
		} catch (Exception e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.IDSCHGLG.ERROR.COULD.NOT.STORE.THE.CHANGENUMBER.VALUE.IN.SYSTEM.STORE",
							e.toString()));
			logmsg(sResHash.getString(
					"CONNECTOR.IDSCHGLG.CURRENT.CHANGENUMBER.VALUE.IS", String
							.valueOf(mChangenumber)));
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
	 * Retrieves the state key object.
	 *
	 * @return the change detection wrapped as a {@link Long} object.
	 * @throws Exception -
	 *             never
	 */
	public Object getStateKeyObject() throws Exception {
		return Long.valueOf(mChangenumber);
	}

	/**
	 * {@inheritDoc}
	 */
	public void terminate() {
		// Removing the Event Listener and closing contexts
		try {
			if (mEvtCtx != null && mUseNotifications) {
				mEvtCtx.removeNamingListener(this);
			}

			if (mEvtCtx != null) {
				mEvtCtx.close();
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.IDSCHGLG.TERMINATE.EXC", e
					.getMessage()));
		}

		super.terminate();
	}

	/**
	 * Event request class.
	 */
	private static class EVTRequest implements ExtendedRequest {

		/**
		 * Unique ID used for deserialization.
		 */
		private static final long serialVersionUID = -110412414149529545L;

		/**
		 * Operation ID.
		 */
		String oid;

		/**
		 * ber value
		 */
		byte berVal[];

		/**
		 * Constructor for this object used for registering.
		 *
		 * @param base
		 *            the context that is going to be used.
		 * @param scope
		 *            the type of search to perform
		 * @param changeType
		 *            the type of change
		 * @throws Exception
		 *             if an error occurs.
		 */
		public EVTRequest(String base, int scope, int changeType)
				throws Exception {
			// registration request
			oid = EVT_REG_OID;

			BEREncoder enc = new BEREncoder();
			int seq_nr = enc.encodeSequence();

			enc.encodeEnumeration(0);
			enc.encodeOctetString(base.getBytes("UTF8"));
			enc.encodeInteger(scope);

			if (changeType != CHANGE_ALL)
				enc.encodeInteger(changeType);

			enc.endOf(seq_nr);
			berVal = enc.toByteArray();
		}

		/**
		 * Constructor for this object used for unregistering.
		 *
		 * @param regID
		 * @throws Exception
		 */
		public EVTRequest(String regID) throws Exception {
			// unregistration request
			oid = EVT_UNREG_OID;

			BEREncoder enc = new BEREncoder();
			enc.encodeOctetString(regID.getBytes("UTF8"));
			berVal = enc.toByteArray();
		}

		/**
		 * Creates a new EVTResponse object.
		 *
		 * @param id
		 *            the extended operation id
		 * @param ber
		 *            the ber value
		 * @param off
		 *            the offset to start reading from
		 * @param len
		 *            the count of bytes to read
		 *
		 * @return the new EVTResponse instance.
		 * @throws NamingException
		 *             if an error occurs.
		 */
		public ExtendedResponse createExtendedResponse(String id, byte ber[],
				int off, int len) throws NamingException {
			try {
				return new EVTResponse(id, ber, off, len);
			} catch (Exception e) {
				throw new NamingException(e.getMessage());
			}
		}

		/**
		 * @return the berValue
		 * @throws IllegalStateException
		 *             never.
		 */
		public byte[] getEncodedValue() throws IllegalStateException {
			return berVal;
		}

		/**
		 * @return the extended operation id.
		 */
		public String getID() {
			return oid;
		}
	}

	/**
	 * Event response class.
	 */
	private static class EVTResponse implements ExtendedResponse {

		/**
		 * Unique ID used for deserialization.
		 */
		private static final long serialVersionUID = 4612627272713226626L;

		/**
		 * Operation ID.
		 */
		String oid;

		/**
		 * Regional ID.
		 */
		String regID;

		/**
		 * the ber value
		 */
		byte berVal[];

		/**
		 * Creates a new EVTResponse object.
		 *
		 * @param id
		 *            the extended operation id
		 * @param berValue
		 *            the ber value
		 * @param offset
		 *            the offset to start reading from
		 * @param length
		 *            the count of bytes to read
		 * @throws Exception
		 *             in case of an error
		 *
		 */
		public EVTResponse(String id, byte[] berValue, int offset, int length)
				throws Exception {
			oid = id;
			berVal = berValue;

			if (berVal.length > 0) {
				BERDecoder dec = new BERDecoder(berValue);
				byte tmp[] = dec.decodeOctetString();
				regID = new String(tmp);
			}
		}

		/**
		 * @return the berVal
		 * @throws IllegalStateException -
		 *             never
		 */
		public byte[] getEncodedValue() throws IllegalStateException {
			return berVal;
		}

		/**
		 * @return the operation id
		 */
		public String getID() {
			return oid;
		}

		/**
		 * @return the regID
		 */
		public String getRegID() {
			return regID;
		}
	}

	// ***************************************************************
	// Notification methods
	// ***************************************************************
	/**
	 * Callback method for handling {@link NamingEnumeration}s
	 *
	 * @param evt
	 *            the naming exception event object
	 */
	public void namingExceptionThrown(NamingExceptionEvent evt) {
		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.IDSCHGLG.NAMING.EXCEPTION.THROWN", evt.toString()));
		}

		/**
		 * #14225
		 * If TDS server has been restarted, or connection has been closed.
		 * We should stop waiting and get an error, which will probably cause a reconnect.
		 */
		synchronized (lock) {
			mNotificationReceived = true;
			lock.notify();
		}

	}

	/**
	 * Callback method called when a change is made.
	 *
	 * @param evt
	 *            the event transport object
	 */
	public void notificationReceived(UnsolicitedNotificationEvent evt) {
		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.IDSCHGLG.NOTIFICATION.RECEIVED", evt.toString()));
		}

		synchronized (lock) {
			mNotificationReceived = true;
			lock.notify();
		}
	}

	/**
	 * Version information.
	 *
	 * @return the version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 2017-09-28";
	}

	/**
	 * {@inheritDoc}
	 */
	public void reconnect(Object o) throws Exception {
		terminate();
		initialize(o);
	}

	@Override
	protected Entry parseEntry(Entry aEntry) throws Exception {
		Entry p = super.parseEntry(aEntry);
		if (onlyChanges && p != null && aEntry != null &&
				aEntry.getString(TARGET_DN) != null && 
				p.getString(TARGET_DN) == null)
			p.setAttribute(TARGET_DN, aEntry.getString(TARGET_DN));
		return p;
	}
}
