/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.ArrayList;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.event.EventDirContext;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;
import javax.naming.ldap.Control;
import javax.naming.ldap.SortControl;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.store.PropertyStore;
import com.ibm.di.store.StoreFactory;

/**
 * The NetscapeChangelogConnector is a specialized instance of the LDAP
 * Connector.
 *
 * In earlier versions of iPlanet Directory Server, the change log was
 * accessible through LDAP. Now the changelog is intended for internal use by
 * the server only. If you have applications that must read the changelog, you
 * will need to use the iPlanet Retro Change Log Plug-in for backward
 * compatibility.
 */
public class NetscapeChangelogConnector extends ChangelogConnector implements
		NamespaceChangeListener, ObjectChangeListener, ConnectorInterface,
		ChangelogInterface {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "sundirectorycdconnector";
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

	/**
	 * Parameter Name: {@value #PARAM_DELIVERY_MODE}
	 */
	public static final String PARAM_DELIVERY_MODE = "deliveryMode";

	// start at predefined values
	/**
	 * End Of Data String: {@value #START_AT_END_OF_DATA}
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
	 * Attribute name - last change number
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
	 * Change number property name.
	 */
	private String mCNPropName;
	/**
	 * Search constraints used in search operations
	 */
	private SearchControls mSearchConstraints;
	/**
	 * Request control to sort on a list of attributes.
	 */
	private Control mSortChangedControl = null;
	/**
	 * Result returned from search operation.
	 */
	private NamingEnumeration<SearchResult> mResults = null;
	/**
	 * The filter expression to use for search.
	 */
	private String mFilter = null;
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
	 * @see EventDirContext
	 */
	private EventDirContext mEvtCtx = null;
	/**
	 * Notification received flag.
	 */
	private volatile boolean mNotificationReceived = false;

	/**
	 *
	 */
	private ArrayList<Entry> mEntries = new ArrayList<Entry>();

	/**
	 * Delivery mode flag
	 */
	private boolean mRealtime = false;
	/**
	 * Used for thread safe modification.
	 */
	private final Object lock = new Object();

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
	public NetscapeChangelogConnector() {
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

		String deliveryMode = getParam(PARAM_DELIVERY_MODE);
		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.DELIVERYMODE.INFO", deliveryMode));
		}
		if (deliveryMode != null && deliveryMode.equalsIgnoreCase("Realtime")) {
			mRealtime = true;
		}

		mUseNotifications = Boolean.valueOf(getParam(PARAM_USE_NOTIFICATIONS))
				.booleanValue();
		if (mUseNotifications || mRealtime) {
			// Add event listener
			mEvtCtx = (EventDirContext) getLdapContext().lookup(
					getParam(PARAM_LDAP_SEARCH_BASE));
			if (mEvtCtx == null) {
				throw new Exception(sResHash.getString(
						"CONNECTOR.NETSCAPECHGLOG.NODIRCONEXT.EXCEP",
						getParam(PARAM_LDAP_SEARCH_BASE)));
			}
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			setReturnAttributes(constraints);
			mEvtCtx.addNamingListener("", "objectclass=*", constraints, this);
		}

		if (mRealtime) {
			return;
		}

		String str = getParam(PARAM_TIMEOUT);
		if (str != null) {
			mTimeout = Integer.parseInt(str);
		}
		str = getParam(PARAM_SLEEP_INTERVAL);
		if (str != null) {
			mSleepInterval = Integer.parseInt(str);
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

		mBatchRetrieval = Boolean.valueOf(getParam(PARAM_BATCH_RETRIEVAL))
				.booleanValue();

		if (mBatchRetrieval) {
			// define search constraints for use in search operations
			mSearchConstraints = new SearchControls();
			mSearchConstraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			setReturnAttributes(mSearchConstraints);

			// define request controls for sending to LDAP Server
			mSortChangedControl = new SortControl(
					new String[] { ATTRIBUTE_CHANGENUMBER }, Control.CRITICAL);
			mControlsNoPaged = new Control[] { mSortChangedControl };
		}
		
		mCNPropName = getParam(PARAM_STORE_PARAM_NAME);
		if (mCNPropName != null && mCNPropName.trim().length() > 0) {
			defaultPropStore = StoreFactory.getDefaultPropertyStore();
		} else {
			mCNPropName = null;
			logmsg(sResHash.getString("CONNECTOR.NETSCAPECHGLOG.EMPTY.ITERATORSTATEKEY"));
		}

	}

	/**
	 * Prepares the connector for listening to change notifications.
	 *
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void selectEntries() throws Exception {

		if (mRealtime) {
			return;
		}
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
						"CONNECTOR.NETSCAPECHGLOG.NOSTARTCHGNUMBER.INFO", ""
								+ startAt));
			}
		}
		if (mCNPropName != null) {
			Object n = defaultPropStore.getProperty(mCNPropName);
			if (n instanceof Long) {
				mChangenumber = ((Long) n).longValue();
			} else if (n != null) {
				mChangenumber = Integer.parseInt(n.toString());
			}
			logmsg(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.STARTCHANGE.INFO", new Object[] {
							"" + mChangenumber, mCNPropName }));
		}
	}

	/**
	 * This method will get the next change if the number of changes is smaller
	 * than the actual changes, that were done on the Directory Server. If no
	 * more changes are available this method will block until notified that a
	 * change was done.
	 *
	 * @return the changes as an Entry object
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Entry getNextEntry() throws Exception {

		Entry entry = null;

		if (mRealtime) {
			while (entry == null) {
				if (mEntries.size() == 0) {
					blockForNotifications();
				}
				if (mEntries.size() > 0) {
					entry = mEntries.remove(0);
				}
			}
			if (entry.getProperty("ldap.error") instanceof Exception)
				throw (Exception) entry.getProperty("ldap.error");

			return entry;
		}
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
				blockForNotifications();
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
					if (debugMode() && isTimeout) {
						debug(sResHash
								.getString("CONNECTOR.NETSCAPECHGLOG.TIMEOUT.INFO"));
					}
				}
			}

			if (!isTimeout) {
				if (mBatchRetrieval) {
					mFilter = "(" + ATTRIBUTE_CHANGENUMBER + ">="
							+ mChangenumber + ")";
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.NETSCAPECHGLOG.LDAPFILTER.INFO",
								mFilter));
					}
					getLdapContext().setRequestControls(mControlsNoPaged);
					mResults = getLdapContext().search(
							getParam(PARAM_LDAP_SEARCH_BASE), mFilter,
							mSearchConstraints);
					entry = getNextSearchResult();
				} else {
					String cn = ATTRIBUTE_CHANGENUMBER + "=" + mChangenumber
							+ "," + getParam(PARAM_LDAP_SEARCH_BASE);

					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.NETSCAPECHGLOG.READINGENTRY.INFO",
								new Object[] { "" + mChangenumber,
										getParam(PARAM_LDAP_SEARCH_BASE) }));
					}
					// Read next changelog entry
					entry = findEntry(new SearchCriteria("$dn",
							SearchCriteria.EXACT, cn));

					if (entry == null && mUseNotifications && mLastChangeNumber < mChangenumber) {
						// We got a notification, but no changelog entry available.
						// Proceed to top of loop, where we will read last change number and act accordingly.
						continue;
					}

					mChangenumber++;
					if (mAfterRead) {
						storeChangenumberForNextSynch();
					}
					if (entry != null) {
						if (debugMode()) {
							debug(sResHash.getString(
									"CONNECTOR.NETSCAPECHGLOG.READENTRY.INFO",
									new Object[] {"" + (mChangenumber-1),
											getParam(PARAM_LDAP_SEARCH_BASE) }));
						}
						return parseEntry(entry);
					} else {
						logmsg(sResHash
								.getString(
										"CONNECTOR.NETSCAPECHGLOG.READENTRYNOTFOUND.INFO",
										new Object[] {"" + (mChangenumber-1),
												getParam(PARAM_LDAP_SEARCH_BASE) }));
					}
				}
			}
		} while ((entry == null) && (!isTimeout));
		return entry;
	}

	/**
	 * Retrieves the next search result.
	 *
	 * @return Entry
	 * @throws Exception
	 *             if an error occurs
	 */
	private Entry getNextSearchResult() throws Exception {

		if (mResults == null) {
			return null;
		}

		Entry entry = null;
		boolean hasMore = false;
		try {
			hasMore = mResults.hasMore();
		} catch (PartialResultException e) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.NETSCAPECHGLOG.CAUGHTEXCEP.INFO", e
								.toString()));
			}
		}

		if (hasMore) {
			SearchResult searchRes = mResults.next();
			entry = parseEntry(entry2at(searchRes));
		}

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
	 * Retrieves last change number.
	 *
	 * @return the last change number.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private long getLastChangeNumber() throws Exception {
		long lastChangeNumber = -1;
		getLdapContext().setRequestControls(new Control[] {});
		String [] attrID = {ATTR_LAST_CHANGENUMBER};
		Attributes attrs = getLdapContext().getAttributes("", attrID);
		if (attrs != null) {
			javax.naming.directory.Attribute attr = attrs.get(ATTR_LAST_CHANGENUMBER);
			if (attr != null) {
				Object obj = attr.get();
				lastChangeNumber = Long.valueOf(obj.toString()).longValue();
			}
		}
		if (lastChangeNumber == -1) {
			throw new Exception(
					sResHash.getString("CONNECTOR.NETSCAPECHGLOG.COULD.NOT.RETRIEVE.LAST.CHANGE"));
		}

		return lastChangeNumber;
	}

	/**
	 * Retrieves attribute's long value
	 *
	 * @param aEntry
	 *            entry holding name/value pair for the attribute
	 * @param aAttributeName
	 *            attribute name
	 * @return long value
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
				defaultPropStore.updateProperty(mCNPropName, Long.valueOf(mChangenumber), true);
			}
		} catch (Exception e) {
			logmsg(sResHash
					.getString(
							"CONNECTOR.NETSCAPECHGLOG.ERROR.COULD.NOT.STORE.THE.CHANGENUMBER.VALUE.IN.SYSTEM.STORE",
							e.toString()));
			logmsg(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.CURRENT.CHANGENUMBER.VALUE.IS",
					String.valueOf(mChangenumber)));
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
	 *
	 * @return the change number wrapped as a {@link Long} object.
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
		if (mResults != null) {
			try {
				mResults.close();
			} catch (NamingException ignore) {
			}
			mResults = null;
		}
		try {
			if (mEvtCtx != null && (mUseNotifications || mRealtime)) {
				mEvtCtx.removeNamingListener(this);
			}

			if (mEvtCtx != null) {
				mEvtCtx.close();
			}
		} catch (Exception e) {
			logmsg(sResHash.getString("CONNECTOR.NETSCAPECHGLOG.TERMINATE.EXC",
					e.getMessage()));
		}
		super.terminate();
	}

	/**
	 * Callback method called when an object is added.
	 *
	 * @param aEvent
	 *            the event transport object.
	 */
	public void objectAdded(NamingEvent aEvent) {
		if (mRealtime) {
			callEventFunction("objAdded", "add", aEvent, null);
		}
		notificationReceived(aEvent);
	}

	/**
	 * Callback method called when an object is removed.
	 *
	 * @param aEvent
	 *            the event transport object.
	 */
	public void objectRemoved(NamingEvent aEvent) {
		if (mRealtime) {
			callEventFunction("objRemoved", "delete", aEvent, null);
		}
		notificationReceived(aEvent);
	}

	/**
	 * Callback method called when an object is renamed.
	 *
	 * @param aEvent
	 *            the event transport object.
	 */
	public void objectRenamed(NamingEvent aEvent) {
		if (mRealtime) {
			callEventFunction("objRenamed", "modrdn", aEvent, null);
		}
		notificationReceived(aEvent);
	}

	/**
	 * Callback method called when an object is changed.
	 *
	 * @param aEvent
	 *            the event transport object.
	 */
	public void objectChanged(NamingEvent aEvent) {
		if (mRealtime) {
			callEventFunction("objModified", "modify", aEvent, null);
		}
		notificationReceived(aEvent);
	}

	/**
	 * Callback method for handling {@link NamingEnumeration}s
	 *
	 * @param aEvent
	 *            the naming exception event object
	 */
	public void namingExceptionThrown(NamingExceptionEvent aEvent) {
		if (debugMode()) {
			debug(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.NAMINGEXCEPTION.THROWN", aEvent
							.getException()));
		}
		if (mRealtime) {
			callEventFunction("handleError", null, null, aEvent.getException());
		}
		notificationReceived(null);
	}

	/**
	 * Callback method called when a change is made.
	 *
	 * @param aEvent
	 *            the event transport object
	 */
	private void notificationReceived(NamingEvent aEvent) {
		if ((debugMode()) && (aEvent != null)) {
			debug(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.NOTIFICATION.RECEIVED", aEvent
							.toString()));
		}

		synchronized (lock) {
			mNotificationReceived = true;
			lock.notify();
		}
	}

	/**
	 * Makes a call to the provided operation name.
	 *
	 * @param aOperation
	 *            operation to call
	 * @param aChangetype
	 *            type of modification operation
	 * @param aEvent
	 *            the event transport object.
	 * @param aError
	 *            ldap error information
	 */
	private void callEventFunction(String aOperation, String aChangetype,
			NamingEvent aEvent, NamingException aError) {

		String oldName = null;
		Object oldEntry = null;
		String newName = null;
		Object newEntry = null;
		Entry tmp = null;

		if (aEvent != null) {
			Object obj = aEvent.getOldBinding();
			if (obj != null) {
				oldName = aEvent.getOldBinding().getName();
				if (obj instanceof SearchResult) {
					oldEntry = obj;
				}
			}
			obj = aEvent.getNewBinding();
			if (obj != null) {
				newName = aEvent.getNewBinding().getName();
				if (obj instanceof SearchResult) {
					newEntry = obj;
				}
			}
		}

		try {
			boolean isChangelogUsed = getParam(PARAM_LDAP_SEARCH_BASE).equals(
					"cn=changelog");
			Entry entry = new Entry();

			if (aOperation != null && !isChangelogUsed) {
				entry.setProperty("ldap.operation", aOperation);
			}
			if (aChangetype != null) {
				entry.setAttribute("changeType", aChangetype);
				if (aChangetype.equals("delete")) {
					entry.setOp(Entry.OP_DEL);
				}
				if (aChangetype.equals("add")) {
					entry.setOp(Entry.OP_ADD);
				}
				if (aChangetype.equals("modify")
						|| aChangetype.equals("modrdn")) {
					entry.setOp(Entry.OP_MOD);
				}
			}
			if (aError != null) {
				entry.setProperty("ldap.error", aError);
			}
			if (oldName != null) {
				entry.setProperty("ldap.dn", oldName);
				entry.setAttribute("targetDn", oldName);
			}
			if (newName != null) {
				entry.setProperty("ldap.newdn", newName);
				entry.setAttribute("newrdn", newName);
			}
			if (oldEntry != null) {
				tmp = entry2at((SearchResult) oldEntry);
				entry.setProperty("ldap.entry", tmp);
				entry.merge(tmp);

				if (isChangelogUsed) {
					entry = parseEntry(entry);
				}
			}
			if (newEntry != null) {
				tmp = entry2at((SearchResult) newEntry);
				entry.setProperty("ldap.newentry", tmp);
				entry.merge(tmp);

				if (isChangelogUsed) {
					entry = parseEntry(entry);
				}
			}
			mEntries.add(entry);

		} catch (Exception error) {
			logmsg(sResHash.getString(
					"CONNECTOR.NETSCAPECHGLOG.CALL.EVENT.FUNCTION.ERROR", error
							.toString()));
		}
	}

	/**
	 * Release lock until notification signal is set.
	 */
	private void blockForNotifications() {
		if (debugMode()) {
			debug(sResHash
					.getString("CONNECTOR.NETSCAPECHGLOG.BLOCK.FOR.NOTIFICATIONS"));
		}

		try {
			while(!mNotificationReceived) {
				synchronized (lock) {
					if (!mNotificationReceived) {
						lock.wait(60000);
					}
				}
				if (!mNotificationReceived) {
					verifyConnectionAlive();
				}
			}
		} catch (InterruptedException e) {
			if (debugMode()) {
				debug(sResHash.getString(
						"CONNECTOR.NETSCAPECHGLOG.THREAD.INTERUPTED", e
								.toString()));
			}
		}
		mNotificationReceived = false;
	}

	private void verifyConnectionAlive() {
		try {
			getLdapContext().lookup("");				
		} catch (NamingException e) {
			callEventFunction("handleError", null, null, e);
		}
	}

	/**
	 * Version information.
	 *
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
	
	private void setReturnAttributes(SearchControls constraints) {
		String param = getParam("ldapReturnAttributes");
		if (param == null || param.trim().isEmpty())
			return;
		
		String[] retattr = param.trim().split("[\r\n ]+");

		if (debugMode()) {
			debug("ldapReturnAttributes:");
			for (int lr = 0; lr < retattr.length; lr++) {
				debug(" - " + retattr[lr]);
			}
		}
		constraints.setReturningAttributes(retattr);
	}

}
