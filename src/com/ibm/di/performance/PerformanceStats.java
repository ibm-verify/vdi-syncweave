/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.performance;

import java.util.Enumeration;

public class PerformanceStats {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public PerfEntryList entryList;

	/**
	 * Initializes the Performance Recording for a particular AssemblyLine.
	 */
	public void initPerfStats() {
		entryList = new PerfEntryList();
	}

	/**
	 * Cleanup the Performance Stats List for a particular AssemblyLine.
	 */
	public void cleanPerfStats() {
		if (entryList != null) {
			entryList.clearAllPerfEntrys();
			entryList = null;
		}
	}

	/**
	 * Starts the Performance Recording for a particular component.
	 * 
	 * @param component
	 *            This is a unique value which will be set, usually to indicate
	 *            the location from where the recording is started
	 */
	public void startPerfRecording(String component) {
		PerfEntry entry = new PerfEntry(component, PerfEntry.BEGIN);
		entryList.addPerfEntry(entry);
	}

	/**
	 * Stops the Performance Recording for a particular component. The
	 * corresponding startPerfRecording(Comonent) must be started before
	 * stopping Performance Recording.
	 * 
	 * @param Component
	 *            This is a unique value which will be set, usually to indicate
	 *            the location from where the recording is started.
	 * 
	 * @return The performance statistics for the component.
	 */
	public String stopPerfRecording(String Component) {
		PerfEntry entry = new PerfEntry(Component, PerfEntry.END);
		PerfEntry pair = entryList.findPerfPair(entry);
		if (pair != null) {
			// Get the Difference in the Begin and End Values
			PerfEntry diffEntry = PerfEntry.diffPerfEntries(pair, entry);

			entryList.addPerfEntry(diffEntry);

			String perfStats = diffEntry.dumpEntry();
			entryList.deletePerfEntry(pair.getComponent());
			return perfStats;
		}
		return Component + ": Corresponding Start Entry Not Found";
	}

	/**
	 * Dumps the Timestamp and Memory usage.
	 * 
	 * @return Returns a String containing a dump of the performance statistics.
	 */
	public String dumpPerfStats() {
		PerfEntry entry = new PerfEntry("", PerfEntry.NOT_DUAL);
		return entry.getPerfEntryStats();
	}

	/**
	 * Get a list of all the entries in the pool.
	 * 
	 * @return An Enumeration of all the entries in the pool.
	 */
	public Enumeration getAllResultStats() {
		return entryList.getAllPerfEntries();
	}

	/**
	 * Returns the performance statistics for a component.
	 * 
	 * @param component
	 *            The name of the component to get the performance statistics
	 *            for.
	 * 
	 * @return A String containing the performance statistics of the specified
	 *         component.
	 */
	public String getPerfStats(String component) {
		return entryList.getPerfEntry(component).getPerfEntryStats();
	}

}
