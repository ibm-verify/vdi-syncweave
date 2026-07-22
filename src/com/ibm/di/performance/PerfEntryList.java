/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.performance;

import com.ibm.di.server.ResourceHash;
import java.util.Enumeration;
import java.util.Hashtable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * PerfEntryList class represents the Performance Entry pool.
 */
public class PerfEntryList {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private Hashtable<String, PerfEntry> PerfEntryPool = new Hashtable<>();

	// TODO: Comment this line
	Logger logger = LogManager.getLogger("performance");

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Add a performance entry to the pool
	 * 
	 * @param entry
	 *            Performance entry to be added to the pool
	 */

	public synchronized void addPerfEntry(PerfEntry entry) {
		if (entry == null) {
			// TODO: Comment this line
			logger.error(sResHash
					.getString("entrylist.addentry.perfentry.null"));
			throw new IllegalArgumentException();
		}

		if (PerfEntryPool != null) {
			// ComponentName is the key for the hashtableEntry
			String key = entry.getComponent();
			if (perfEntryExists(key)) {
				// If the Entry is of Result type, Add current Result to it
				switch (entry.getDual()) {
				case PerfEntry.RESULT:
					if (getPerfEntry(key).getDual() == PerfEntry.RESULT) {
						// Result already exists So add to existing
						PerfEntry sumEntry = PerfEntry.addPerfEntries(entry,
								getPerfEntry(key));
						PerfEntryPool.put(sumEntry.getComponent(), sumEntry);
					} else {
						// First time result. Simple put to table
						PerfEntryPool.put(entry.getComponent(), entry);
					}
					break;
				case PerfEntry.BEGIN: {
					// We should not be here it is an error
					// TODO: Throw an error here
					logger.error(sResHash.getString("entryexidst.notadded"));
				}
				}

			} else {
				// There is a BEGIN Entry. Add this to the Pool
				PerfEntryPool.put(key, entry);
			}
			// For Code Debug only
			// dumpAllEntrys();

		}
	}

	/**
	 * Checks if the object corresponding to the key exists
	 * 
	 * @param key
	 *            key to the object
	 * @return boolean true if exists
	 */
	public synchronized boolean perfEntryExists(String key) {
		if (PerfEntryPool.containsKey(key))
			return true;
		return false;
	}

	/**
	 * Returns the Performance entry object for a given key
	 * 
	 * @param key
	 *            key to the object
	 */
	public synchronized PerfEntry getPerfEntry(String key) {
		if (PerfEntryPool.containsKey(key))
			return (PerfEntry) PerfEntryPool.get(key);
		return null;
	}

	/**
	 * Checks all the objects in the pool
	 * 
	 * @return Enumneration of Performance entries in pool
	 */
	public synchronized Enumeration<PerfEntry> getAllPerfEntries() {
		return PerfEntryPool.elements();
	}

	/**
	 * Locates the PerfEntry in BEGIN mode for the corresponding Perfentry in
	 * pool
	 * 
	 * @param newEntry
	 *            Entry to find pair
	 * @return PerfEntry if found else null
	 */
	public synchronized PerfEntry findPerfPair(PerfEntry newEntry) {
		String key = newEntry.getBaseComponent() + ".BEGIN";
		if ((perfEntryExists(key)) && (getPerfEntry(key).getDual() == PerfEntry.BEGIN)) {
			return getPerfEntry(key);
		}
		return null;
	}

	/**
	 * Cleans the pool
	 */
	public synchronized void clearAllPerfEntrys() {
		if (PerfEntryPool != null)
			PerfEntryPool.clear();
	}

	/**
	 * Deletes a particular perfEntry object from the pool
	 * 
	 * @param key
	 *            Hastable key
	 */
	public synchronized void deletePerfEntry(String key) {
		if (PerfEntryPool != null)
			PerfEntryPool.remove(key);
	}

	/**
	 * Dumps all the Perfentries in the pool
	 */
	public synchronized void dumpAllPerfEntries() {
		if (PerfEntryPool != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(sResHash.getString("info.asterisks.begin.end"));
			}
			Enumeration<PerfEntry> enum1 = PerfEntryPool.elements();
			while (enum1.hasMoreElements()) {
				PerfEntry entry = enum1.nextElement();
				if (logger.isDebugEnabled()) {
					logger.debug(sResHash.getString(
							"MISERVER.PERFENTRYLIST.DUMP.PERFENTRY", entry
									.dumpEntry()));
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(sResHash.getString("info.asterisks.begin.end"));
			}
		}
	}

}
