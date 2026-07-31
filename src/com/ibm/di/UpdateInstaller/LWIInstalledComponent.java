/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * Represents an LWI (Embedded Web Platform) component. Different from a regular
 * InstalledComponent in that it also must remember a config ID to be used when
 * rolling back.
 * 
 * @author Alan Watkins
 * 
 */
public class LWIInstalledComponent extends InstalledComponent {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Internal variable used to indicate if we have fully populated the data
	 */
	private boolean ready2Add = false;

	/**
	 * Indicates if an LWI service name is present on a Windows system
	 */
	private boolean serviceNameSet = false;

	/**
	 * The config ID of this LWI fix
	 */
	private String curID = "";

	/**
	 * The Windows LWI service name, if present
	 */
	private String serviceName = "";

	/**
	 * Holds a list on configuration IDs for this and previously installed LWI
	 * fixes
	 */
	private Vector<String> configIDs;

	/**
	 * Gets the list of config IDs.
	 * 
	 * @return An array representing the config IDs from this and previously
	 *         applied LWI fixes in the correct order
	 */
	public Object[] getConfigIDs() {
		return configIDs.toArray();
	}

	/**
	 * Gets the Windows LWI service name.
	 * 
	 * @return The Windows LWI service name if present
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Adds the name of a fix to the specified position in the list of fixes
	 * that have been applied to this component.
	 * 
	 * @param pos
	 *            Position to add this fix in the list
	 * @param fixName
	 *            Name of the fix
	 */
	public void addFix(int pos, String fixName) {
		if (!serviceNameSet) {
			serviceNameSet = true;
			if (!fixName.equalsIgnoreCase(FixUtils.NONE))
				serviceName = fixName;
			return;
		}

		if (ready2Add) {
			fixes.add(pos, fixName);
			configIDs.add(pos, curID);
		} else
			curID = fixName;

		ready2Add = !ready2Add;
	}

	/**
	 * Adds the name of a fix to the end of the list of fixes that have been
	 * applied to this component.
	 * 
	 * @param fixName
	 *            Name of the fix
	 */
	public void addFix(String fixName) {
		addFix(fixes.size(), fixName);
	}

	/**
	 * Class constructor specifying the name of this component.
	 * 
	 * @param name
	 *            Component name
	 */
	public LWIInstalledComponent(String name) {
		super(name);
		configIDs = new Vector<String>();
	}

	/**
	 * Removes a fix from this component's list.
	 * 
	 * @param fixName
	 *            The name of the fix to remove
	 * @return The position at which the fix was located before removal, -1 if
	 *         not found
	 */
	public int remove(String fixName) {
		int pos = super.remove(fixName);
		if (pos >= 0)
			configIDs.removeElementAt(pos);
		return pos;
	}

	/**
	 * Clears the list of applied fixes for this component.
	 */
	public void removeAll() {
		super.removeAll();
		while (configIDs.size() > 0)
			configIDs.removeElementAt(0);
	}

	/**
	 * Write this component's information in the appropriate format to specified
	 * open file.
	 * 
	 * @param outfile
	 *            The file that is being created
	 */
	public void writeFixes(PrintWriter outfile) {
		outfile.println("<" + getName() + ">");

		if (serviceName.equals(""))
			outfile.println("   " + FixUtils.NONE);
		else
			outfile.println("   " + serviceName);

		for (int i = 0; i < getFixes().length; i++) {
			outfile.println("   " + getConfigIDs()[i]);
			outfile.println("   " + getFixes()[i]);
		}
		outfile.println("</" + getName() + ">");
	}

	/**
	 * Tells if two InstalledComponents are equal by comparing their names.
	 * 
	 * @param obj
	 *            An InstalledComponent to compare
	 * @return true if the objects have the same name, false otherwise
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * Tells if two LWIInstalledComponents are equal by comparing their names.
	 * 
	 * @param name
	 *            A String with the name of the component to compare
	 * @return true if the names are equal, false otherwise
	 */
	public boolean equals(String name) {
		return super.equals(name);
	}

	/**
	 * Computes this InstalledComponent's hash code.
	 * 
	 * @return the hashcode of the component's name
	 */
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Converts the current object to a String for printing.
	 * 
	 * @return A string representation of this object
	 */
	public String toString() {
		String msg = getName() + "\n";

		Object[] fixes = getFixes();
		Object[] configIDs = getConfigIDs();
		for (int x = 0; x < fixes.length; x++)
			msg += "   -"
					+ fixes[x]
					+ ","
					+ configIDs[x]
					+ "\n"
					+ (serviceName.equals("") ? FixUtils.NONE : serviceName
							+ "\n");
		return msg;
	}
}
