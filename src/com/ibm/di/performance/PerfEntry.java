/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.performance;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.ibm.di.server.ResourceHash;

/**
 * PerfEntry class represents the Performance Entry object.
 */
public class PerfEntry {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String component; // Component name from where the message is coming

	private long timeStamp; // Time of Entry

	private long usedMemory; // Used memory = Total memory - Free Memory

	private int dual; // dual message is used for statistics generation

	private String baseComponent;

	public static final int BEGIN = 0;

	public static final int END = 1;

	public static final int NOT_DUAL = 2;

	public static final int RESULT = 3;

	public static final int USER = 3;

	private static final String PROPERTIES_FILE = "miserver";

	Logger logger = LogManager.getLogger("performance");

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private static String[] suffix = { ".BEGIN", ".END", ".MSG", ".RESULT",
			".USR" };

	/**
	 * Constructor for the Performance Entry
	 */
	public PerfEntry() {
	}

	/**
	 * Constructor for the Performance Entry-Message mode
	 * 
	 * @param component
	 *            The name of the component name.
	 */
	public PerfEntry(String component) {
		this(component, PerfEntry.NOT_DUAL);
	}

	/**
	 * Constructor for the Performance Entry-BEGIN and END mode
	 * 
	 * @param component
	 *            Component name
	 * @param dual
	 *            Type of message PerfEntry.BEGIN or PerfEntry.END
	 */
	public PerfEntry(String component, int dual) {
		setBaseComponent(component);
		this.component = component + suffix[dual];
		this.dual = dual;
		this.timeStamp = System.currentTimeMillis();
		this.usedMemory = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();
		// TODO:Comment the logger
		logger.debug(sResHash.getString("MISERVER.PERFENTRY.DUMP.PERFENTRY",
				new Object[] { suffix[dual], this.dumpEntry() }));
	}

	/**
	 * Constructor for the Performance Entry - ALL modes
	 * 
	 * @param component
	 *            Component name
	 * @param usedTime
	 *            Time
	 * @param usedMemory
	 *            memory
	 * @param dual
	 *            type of message PerfEntry.BEGIN or PerfEntry.END
	 */
	public PerfEntry(String component, long usedTime, long usedMemory, int dual) {
		setBaseComponent(component);
		this.component = component + suffix[dual];
		this.dual = dual;
		this.timeStamp = usedTime;
		this.usedMemory = usedMemory;
		// TODO:Comment the logger
		logger.debug(sResHash.getString(
				"PERFENTRY.OVERLOADING.CONSTRUCTOR.DUMP.PERFENTRY",
				new Object[] { suffix[dual], this.dumpEntry() }));
	}

	/**
	 * Checks if the equality of two PerfEntry objects. Checks if they have the
	 * same Component name.
	 * 
	 * @param entry
	 *            PerfEntry object
	 * 
	 * @return true if Components and modes are equal
	 */
	public boolean equalsPerfEntry(PerfEntry entry) {
		if ((this.component != null) && (this.component.equals(entry.component)) && (this.dual == entry.dual))
			return true;

		return false;
	}

	/**
	 * Checks if the PerfEntry objects are pairs. Checks if they have the same
	 * Component name.
	 * 
	 * @param entry
	 *            PerfEntry object
	 * 
	 * @return true if Components are equal
	 */
	public boolean isPair(PerfEntry entry) {
		if (this.component.equals(entry.component))
			return true;
		return false;

	}

	/**
	 * Gets the difference of the time stamps and Memory usage of 2 PerfEntry
	 * objects.
	 * 
	 * @param begin
	 *            begin PerfEntry object
	 * @param end
	 *            end PerfEntry object
	 * 
	 * @return PerfEntry object with difference of time stamps and memory usage
	 */
	public static PerfEntry diffPerfEntries(PerfEntry begin, PerfEntry end) {
		long usedTime = end.getTimeStamp() - begin.getTimeStamp();
		long usedMemory = end.getUsedMemory() - begin.getUsedMemory();
		PerfEntry entry = new PerfEntry(begin.getBaseComponent(), usedTime,
				usedMemory, PerfEntry.RESULT);
		return entry;
	}

	/**
	 * Gets the sum of the time stamps and Memory usage of 2 PerfEntry objects.
	 * 
	 * @param entry1
	 *            PerfEntry object
	 * @param entry2
	 *            PerfEntry object
	 * 
	 * @return PerfEntry object with sum of time stamps and memory usage.
	 */
	public static PerfEntry addPerfEntries(PerfEntry entry1, PerfEntry entry2) {
		long UsedTime = entry1.getTimeStamp() + entry2.getTimeStamp();
		long UsedMemory = entry1.getUsedMemory() + entry2.getUsedMemory();
		PerfEntry entry = new PerfEntry(entry1.getBaseComponent(), UsedTime,
				UsedMemory, PerfEntry.RESULT);
		return entry;
	}

	/**
	 * Dumps the performance Entry Object in display format
	 * 
	 * @return String containing a dump of the entry Object in display format.
	 */
	public String dumpEntry() {
		// |component name |<5>|Time used |<5>|<memory used>
		// |<--------40----------->|<5>|<-----25---->|<5>|<---
		String colldiff = "     ";
		StringBuffer str = new StringBuffer(getBaseComponent());
		for (int i = getBaseComponent().length(); i < 40; i++) {
			str.append(" ");
		}
		str.append(colldiff);
		str.append(getTimeStamp());
		for (int i = Long.toString(getTimeStamp()).length(); i < 25; i++) {
			str.append(" ");
		}
		str.append(colldiff);
		str.append(getUsedMemory());

		return str.toString();
	}

	/**
	 * Gets only the time stamp and memory usage for an entry .
	 * 
	 * @return String containing the time stamp and memory usage.
	 */
	public String getPerfEntryStats() {
		return "{" + getTimeStamp() + "}{" + getUsedMemory() + "}";
	}

	/**
	 * Gets the time stamp of this PerfEntry
	 * 
	 * @return The value of the time stamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Gets the memory used.
	 * 
	 * @return Returns the memory used.
	 */
	public long getUsedMemory() {
		return usedMemory;
	}

	/**
	 * Sets the time stamp value.
	 * 
	 * @param time
	 *            The time stamp to set.
	 */
	public void setTimeStamp(long time) {
		timeStamp = time;
	}

	/**
	 * Sets the memory used value.
	 * 
	 * @param memory
	 *            The memory used value.
	 */
	public void setUsedMemory(long memory) {
		usedMemory = memory;
	}

	/**
	 * Method gets the component name.
	 * 
	 * @return Returns a String containing the name of the component.
	 */
	public String getComponent() {
		return component;
	}

	/**
	 * @param string
	 */
	public void setComponent(String string) {
		component = string;
	}

	/**
	 * Method returns the name of the base component.
	 * 
	 * @return Returns the name of the base component.
	 */
	public String getBaseComponent() {
		return baseComponent;
	}

	/**
	 * Sets the name of the base component.
	 * 
	 * @param string
	 *            The base component name to set.
	 */
	public void setBaseComponent(String string) {
		baseComponent = string;
	}

	/**
	 * Gets the dual message.
	 * 
	 * @return Returns the dual message.
	 */
	public int getDual() {
		return dual;
	}

	/**
	 * @param i
	 */
	public void setDual(int i) {
		dual = i;
	}

}
