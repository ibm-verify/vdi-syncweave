/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.ibm.di.api.security.CryptoUtils;
import com.ibm.di.connector.ConnectorInterface;
import com.ibm.di.connector.PropertiesConnector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.Trace;

/**
 * Represents a property store
 */
public class TDIPropertyStore implements Iterator, Serializable {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 198251115520372634L;

	/**
	 * This is the logger object used by this class and other configuration
	 * drivers.
	 */
	public final static Log logger = new Log("com.ibm.di.TDIProperties");

	public final static String PROTECT_VAL_PREFIX = "{encr}";

	private final static int ITER_KEYS = 0;

	private final static int ITER_VALUES = 1;

	private final static int ITER_BOTH = 2;

	// Don't serialize connector
	private transient ConnectorInterface connector;

	private Entry entry;

	private PropertyStoreConfig psc;

	private int iteratorType;

	private Map<String, CacheEntry> cache = new Hashtable<String, CacheEntry>();

	private long cacheTimeout = 0;

	private boolean modified = false;

	private Map<String,Boolean> encrypted = new Hashtable<String,Boolean>();

	private Exception lastException;

	private final static ResourceHash sResHash = MetamergeConfigFactory
			.getResHash();

	/**
	 * Inner class used in the cache
	 */
	private static class CacheEntry implements Serializable {
		private static final long serialVersionUID = 6096373876193865428L;

		public Entry entry;

		public long ts;

		public CacheEntry(Entry entry) {
			this.entry = entry;
			this.ts = System.currentTimeMillis();
		}

		public boolean hasExpired(long timeout) {
			return ((System.currentTimeMillis() - ts) > timeout);
		}

		public Entry entry() {
			ts = System.currentTimeMillis();
			return this.entry;
		}

		public String toString() {
			return new java.util.Date(ts) + " ### " + entry;
		}
	}
	
	public TDIPropertyStore() {}

	/**
	 * Constructor for the TDIPropertyStore object
	 * 
	 * @param psc
	 *            Configuration of the property store
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public TDIPropertyStore(PropertyStoreConfig psc) throws Exception {
		this(psc, null);
	}

	/**
	 * Constructor for the TDIPropertyStore object
	 * 
	 * @param psc
	 *            The PropertyStoreConfig
	 * @param context
	 *            The TDIProperties context
	 * @throws Exception
	 */
	public TDIPropertyStore(PropertyStoreConfig psc, TDIProperties context)
			throws Exception {
		this.psc = psc;
		try {
			initialize(context);
		} catch (Exception e) {
			if (logger != null)
				logger.error(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.CONSTRUCTOR.ERROR",
					new Object[] { psc.toString(), e.toString() }));
		}
	}

	/**
	 * Initializes data structures. Load and initialize the connector, and read
	 * data if they should be read initially.
	 * 
	 * @param context
	 *            The TDIProperties context. Not used.
	 * @throws Exception
	 *             If any problem occurs during loading or initializing the
	 *             connector.
	 */
	public synchronized void initialize(TDIProperties context) throws Exception {
		Trace.entrymin(this, "initialize", context);
		connector = SystemFunctions.loadConnector(psc);
		connector.initialize(null);
		entry = null;
		setModified(false);
		cacheTimeout = psc.getCacheTimeout() * 1000;
		if (cacheTimeout == 0) {
			log(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.CACHE.DISABLED",
					Integer.valueOf(psc.getCacheTimeout())));
		} else {
			log(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.CACHE.ENABLED",
					Integer.valueOf(psc.getCacheTimeout())));
		}

		// Read data source to local cache?
		if (psc.getInitialLoad() && cacheTimeout > 0) {
			reload();
		}
		Trace.exitmin(this, "initialize");
	}

	/**
	 * Logs a message.
	 * 
	 * @param msg
	 *            The message to log
	 */
	public void log(String msg) {
		if (logger != null)
			logger.logdebug("[" + psc.getName() + "] " + msg);
	}

	/**
	 * Logs an error message.
	 * 
	 * @param msg
	 *            The message to log
	 * @param err
	 *            The Exception
	 */
	public void logerror(String msg, Exception err) {
		if (logger != null)
			logger.error("[" + psc.getName() + "] " + msg, err);
	}

	/**
	 * Reloads the information from the connector into a local cache.
	 * Calling this will only have an effect if caching is enabled
	 * by setting Cache Timeout to a value greater than 0.
	 */
	public synchronized void reload() {
		Trace.entrymin(this, "reload");
		if (cacheTimeout <= 0)
			return;
		long count = 0;
		log(sResHash
				.getString("MICONFIG.TDIPROPERTYSTORE.CACHING.ALL.PROPERTIES.FROM.DATA.SOURCE"));
		for (Iterator<Entry> e = entries(); e.hasNext();) {
			Entry entry = e.next();
			String key = entry.getString(TDIProperties.KEY_ATTRIBUTE);
			if (key == null)
				continue;
			cache.put(key, new CacheEntry(entry));
			count++;
		}
		log(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.CACHED.PROPERTIES",
				Long.valueOf(count)));
		Trace.exitmin(this, "reload");
	}

	/**
	 * Reinitializes the connector.
	 * This will also cause all stored information to be saved, if needed.
	 * 
	 * @throws Exception
	 *             If there is any problem with initializing the connector
	 */
	public synchronized void reconnect() throws Exception {
		Trace.entrymax(this, "reconnect");
		ConnectorInterface oldConnector = connector;
		if (connector != null) {
			connector.terminate();
		}

		try {
			initialize(null);
		} catch (Exception e) {
			if (oldConnector != null)
				connector = oldConnector;
			throw e;
		}
		Trace.exitmax(this, "reconnect");
	}

	/**
	 * Terminates the connector
	 * 
	 * @throws Exception
	 *             If there is a problem with terminating the connector
	 */
	public synchronized void terminate() throws Exception {
		if (connector != null) {
			connector.terminate();
		}
		connector = null;
		entry = null;
	}

	/**
	 * Flushes changes to target system
	 */
	public synchronized void commit() throws Exception {
		Trace.entrymax(this, "commit");

		if (connector != null && isModified()) {

			// Make sure that we save the file
			if (connector instanceof PropertiesConnector)
				((PropertiesConnector) connector).setModified();
			connector.terminate();
			connector.initialize(null);
			setModified(false);
		}
		Trace.exitmax(this, "commit");
	}

	/**
	 * Returns true if the connector supports Iterator mode
	 * 
	 * @return true if the connector supports Iterator mode
	 */
	public boolean canIterate() {
		return supportsMode("Iterator");
	}

	/**
	 * Returns true if the connector supports Update mode and is not readonly
	 * 
	 * @return true if the connector supports Update mode and is not readonly
	 */
	public boolean canWrite() {
		return (supportsMode("Update") && !psc.getReadOnly());
	}

	/**
	 * Returns true if the connector supports Delete mode and is not readonly
	 * 
	 * @return true if the connector supports Delete mode and is not readonly
	 */
	public boolean canDelete() {
		return (supportsMode("Delete") && !psc.getReadOnly());
	}

	/**
	 * Returns true if the connector supports Lookup mode
	 * 
	 * @return true if the connector supports Lookup mode
	 */
	public boolean canRead() {
		return supportsMode("Lookup");
	}

	/**
	 * Returns true if we accept this propertyname.
	 * 
	 * @param propertyName
	 *            Property name to check
	 * @return true if we accept this propertyname
	 */
	public boolean accepts(String propertyName) {
		String str = psc.getNameFilters();
		if (str != null && str.trim().length() > 0) {
			for (String s: com.ibm.di.util.StringUtils.splitstring(str, "\r\n")) {
				if (propertyName.startsWith(s))
					return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the short name of this TDIPropertyStore
	 * 
	 * @return The name value
	 */
	public String getName() {
		return psc.getShortName();
	}

	/**
	 * Returns true if the connector supports this mode
	 * 
	 * @param mode
	 *            The mode to check
	 * @return true if the connector supports this mode
	 */
	public synchronized boolean supportsMode(String mode) {
		Trace.entrymax(this, "supportsMode");
		if (connector == null) {
			Trace.exitmax(this, "supportsMode", false);
			return false;
		} else {
			Trace.exitmax(this, "supportsMode");
			return ((com.ibm.di.connector.Connector) connector).getModes()
					.contains(mode);
		}
	}

	/**
	 * Returns the named property
	 * 
	 * @param key
	 *            The property name to find
	 * @return The property value or null if not found
	 * @exception Exception
	 *                Any Exception thrown by the connector
	 */
	public synchronized Object getProperty(String key) throws Exception {
		Entry e = getPropertyEntry(key);
		if (e != null) {
			Attribute a = e.getAttribute(TDIProperties.VALUE_ATTRIBUTE);
			if (a != null && a.size() > 1)
				return a;
			else if (a != null)
				return a.getValue(0);
		}
		return null;
	}

	/**
	 * Returns the Entry object for a key. The Entry object has at least two
	 * Attributes, a "key" Attribute and a "value" Attribute. If the value is
	 * protected, there will also be a "protect" Attribute.
	 * 
	 * @param key
	 *            The name of the attribute to find
	 * @return The Entry containing the name and value
	 * @throws Exception
	 *             Any Exception thrown by the connector
	 */
	public synchronized Entry getPropertyEntry(String key) throws Exception {
		Trace.entrymax(this, "getPropertyEntry", key);
		// Use cache (and remove if expired)
		CacheEntry c = cache.get(key);
		if (c != null) {
			if (!c.hasExpired(cacheTimeout)) {
				log(sResHash.getString(
						"MICONFIG.TDIPROPERTYSTORE.RETURN.CACHED.ENTRY", key));
				Trace.exitmax(this, "getPropertyEntry");
				return c.entry();
			} else {
				log(sResHash.getString(
						"MICONFIG.TDIPROPERTYSTORE.REMOVE.EXPIRED.CACHE.ENTRY",
						key));
				cache.remove(key);
			}
		}

		SearchCriteria sc = new SearchCriteria(psc.getKeyAttribute(),
				SearchCriteria.EXACT, key);
		Entry e = connector.findEntry(sc);
		if (e != null) {
			e = entry2propEntry(e);
			if (cacheTimeout > 0) {
				log(sResHash.getString(
						"MICONFIG.TDIPROPERTYSTORE.ADD.CACHE.ENTRY", key));
				cache.put(key, new CacheEntry(e));
			}
			Trace.exitmax(this, "getPropertyEntry", e);
			return e;
		} else if (connector.getFindEntryCount() > 1) {
			throw new Exception(
					sResHash
							.getString(
									"MICONFIG.TDIPROPERTYSTORE.PROPERTY.KEY.IS.NOT.UNIQUE",
									key));
		} else {
			Trace.exitmax(this, "getPropertyEntry", null);
			return null;
		}
	}

	/**
	 * Sets the named property to the given value
	 * 
	 * @param key
	 *            The name of the property
	 * @param value
	 *            The new property value, null means delete
	 * @exception Exception
	 *                If key is missing, or any Exception thrown by the
	 *                connector
	 */
	public void setProperty(String key, Object value) throws Exception {
		Trace.entrymax(this, "setProperty", key, value);
		if (key == null || key.length() == 0) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}
		Entry e = new Entry();
		e.setAttribute(TDIProperties.KEY_ATTRIBUTE, key);
		e.setAttribute(TDIProperties.VALUE_ATTRIBUTE, value);
		setProperty(e);
		Trace.exitmax(this, "setProperty");
	}

	/**
	 * Sets the named property to the given value, with optional protection
	 * 
	 * @param key
	 *            The name of the property
	 * @param value
	 *            The new property value, null means delete
	 * @param protect
	 *            True if the value should be protected (encrypted)
	 * @exception Exception
	 *                If key is missing, or any Exception thrown by the
	 *                connector
	 */
	public void setProperty(String key, Object value, boolean protect)
			throws Exception {
		if (key == null || key.length() == 0) {
			throw new Exception(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		Entry e = new Entry();
		e.setAttribute(TDIProperties.KEY_ATTRIBUTE, key);
		e.setAttribute(TDIProperties.VALUE_ATTRIBUTE, value, protect);
		e.setAttribute(TDIProperties.PROTECT_ATTRIBUTE, String.valueOf(protect));
		encrypted.put(key, Boolean.valueOf(protect));
		setProperty(e);
	}

	/**
	 * Sets a named property to the given value. The Entry object must contains
	 * the "key" and "value" attributes and optionally "protect" (boolean)
	 * 
	 * @param entry
	 *            The Entry object containing property name and value
	 * @throws Exception
	 *             If key is missing, or any Exception thrown by the connector
	 */
	public synchronized void setProperty(Entry entry) throws Exception {
		Trace.entrymax(this, "setProperty", entry);
		if (psc.getReadOnly()) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.PROPERTY.STORE.IS.CONFIGURED.AS.READ.ONLY",
									getName()));
		}

		String key = entry.getString(TDIProperties.KEY_ATTRIBUTE);
		if (key == null || key.length() == 0) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		Object value = entry.getObject(TDIProperties.VALUE_ATTRIBUTE);

		// Cache it?
		if (cacheTimeout > 0)
			cache.put(key, new CacheEntry(entry));

		Entry ce = propEntry2entry(entry);

		SearchCriteria sc = new SearchCriteria(psc.getKeyAttribute(),
				SearchCriteria.EXACT, key);
		Entry e = connector.findEntry(sc);
		if (e != null) {
			if (value == null)
				connector.deleteEntry(entry, sc);
			else
				connector.modEntry(ce, sc, e);
			setModified(true);
		} else if (connector.getFindEntryCount() > 1) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.PROPERTY.KEY.IS.NOT.UNIQUE",
									key));
		} else if (value != null) {
			// Adding a new property
			connector.putEntry(ce);
			setModified(true);
		}
		Trace.exitmax(this, "setProperty");
	}

	/**
	 * Removes a named property.
	 * 
	 * @param key
	 *            The name of the property to remove
	 * @throws Exception
	 *             If key is missing, or the Property store is readonly, or any
	 *             Exception thrown by the connector
	 */
	public synchronized void removeProperty(String key) throws Exception {
		Trace.entrymax(this, "removeProperty", key);
		if (key == null || key.length() == 0) {
			throw new Exception(
					sResHash
							.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		if (psc.getReadOnly()) {
			throw new Exception(
					sResHash
							.getString(
									"MICONFIG.TDIPROPERTYSTORE.PROPERTY.STORE.IS.CONFIGURED.AS.READ.ONLY",
									getName()));
		}
		SearchCriteria sc = new SearchCriteria(psc.getKeyAttribute(),
				SearchCriteria.EXACT, key);
		Entry e = new Entry();
		e.setAttribute(psc.getKeyAttribute(), key);
		connector.deleteEntry(e, sc);
		cache.remove(key);
		setModified(true);
		Trace.exitmax(this, "removeProperty");
	}

	/**
	 * Returns an Iterator over all property keys
	 * 
	 * @return an Iterator over all property keys
	 */
	public synchronized Iterator keys() {
		try {
			iteratorType = ITER_KEYS;
			if(connector==null) //Defect # 11367
				initialize(null);
			connector.selectEntries();
			entry = null;
			return this;
		} catch (Exception err) {
			lastException = err;
			err.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns an Iterator over all property values
	 * 
	 * @return an Iterator over all property values
	 */
	public synchronized Iterator values() {
		try {
			iteratorType = ITER_VALUES;
			connector.selectEntries();
			entry = null;
			return this;
		} catch (Exception err) {
			lastException = err;
			err.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns an Iterator over all property Entries
	 * 
	 * @return an Iterator over all property Entries
	 */
	public synchronized Iterator entries() {
		try {
			iteratorType = ITER_BOTH;
			entry = null;
			connector.selectEntries();
			return this;
		} catch (Exception err) {
			lastException = err;
			err.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns true if the Iterator has more values. Only call this method if
	 * you have previously called keys(), values() or entries().
	 * 
	 * @return true if the Iterator has a next
	 */
	public boolean hasNext() {
		advanceIterator();
		return (entry != null);
	}

	/**
	 * Return the next value from an Iterator. Only call this method if you have
	 * previously called keys(), values() or entries(), and hasNext() returns
	 * true.
	 * 
	 * @return The next value for the Iterator, it will be an Entry if you have
	 *         called entries().
	 */
	public synchronized Object next() {
		Object value = null;

		advanceIterator();
		if (entry != null) {
			switch (iteratorType) {
			case ITER_KEYS:
				value = entry.getObject(psc.getKeyAttribute());
				break;
			case ITER_VALUES:
				value = entry.getObject(psc.getValueAttribute());
				break;
			case ITER_BOTH:
				value = entry2propEntry(entry);
			}
		}

		entry = null;
		return value;
	}

	/**
	 * Dummy method to implement Iterator. This method does nothing.
	 */
	public void remove() {
	}

	/**
	 * advance the Iterator to the next Entry
	 */
	private synchronized void advanceIterator() {
		try {
			if (entry == null) {
				entry = connector.getNextEntry();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	private Entry entry2propEntry(Entry e) {
		Entry ret = new Entry();
		String key = e.getString(psc.getKeyAttribute());
		Object value = e.getObject(psc.getValueAttribute());
		boolean protect = Boolean.valueOf(e.getString("protect"))
				.booleanValue();
		if (value != null && !(connector instanceof PropertiesConnector)) {
			String s = value.toString();
			if (s.startsWith(PROTECT_VAL_PREFIX)) {
				protect = true;
				try {
					String toDecrypt = s.substring(PROTECT_VAL_PREFIX.length());
					byte[] decryptVal = CryptoUtils.decryptWithServerKey(
							UserFunctions.base64Decode(toDecrypt));
					value = new String(decryptVal, "UTF-8");
				} catch (Exception err) {
					logerror(s, err);
				}
			}
		}
		if (protect) {
			encrypted.put(key, Boolean.TRUE);
			ret.setAttribute(TDIProperties.PROTECT_ATTRIBUTE, "true");
		}

		ret.setAttribute(TDIProperties.KEY_ATTRIBUTE, key);
		ret.setAttribute(TDIProperties.VALUE_ATTRIBUTE, value, protect);
		return ret;
	}

	private Entry propEntry2entry(Entry pe) {
		Entry e = new Entry();
		String key = pe.getString(TDIProperties.KEY_ATTRIBUTE);
		Attribute attr = pe.getAttribute(TDIProperties.VALUE_ATTRIBUTE);
		Object value = (attr == null) ? null : attr.getValue(0);

		Boolean protect = encrypted.get(key);
		if (attr != null && attr.getProtected())
			protect = Boolean.TRUE;
		if (pe.getString(TDIProperties.PROTECT_ATTRIBUTE) != null)
			protect = Boolean.valueOf(pe.getString(TDIProperties.PROTECT_ATTRIBUTE));

		if (value != null && protect != null && protect && 
				!(connector instanceof PropertiesConnector)) {
			try {
				byte[] cryptVal = CryptoUtils.encryptWithServerKey(
						value.toString().getBytes("UTF-8"));
				value = PROTECT_VAL_PREFIX
						+ UserFunctions.base64Encode(cryptVal);
			} catch (Exception err) {
				logerror("", err);
			}
		}

		e.setAttribute(psc.getKeyAttribute(), key);
		e.setAttribute(psc.getValueAttribute(), value, protect != null ? protect : false);
		e.setAttribute("protect", protect);
		return e;
	}

	/**
	 * Returns the last Exception thrown by the connector while iterating
	 * 
	 * @return the last Exception thrown by the connector while iterating
	 */
	public Exception getException() {
		return lastException;
	}

	/**
	 * Returns true if the properties have been modified, but not saved yet
	 * 
	 * @return true if the properties have been modified, but not saved yet
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Sets the modified flag. This can be useful if you want to force a save,
	 * or avoid a save
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * Return the config for the underlying Connector.
	 * For internal use.
	 * @return
	 */
	public RawConnectorConfig getConnectionConfig() {
		return psc.getConnectionConfig();
	}
}
