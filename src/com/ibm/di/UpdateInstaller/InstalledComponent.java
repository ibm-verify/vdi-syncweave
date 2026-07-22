/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * Represents an installed component in TDI. The only other information in this
 * object is a list of fixes that have been applied to the component.
 * 
 * @author Alan Watkins
 * 
 * 
 */
public class InstalledComponent {

	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Component name
	 */
	private String name;

	/**
	 * Fixes that have been applied to this component
	 */
	protected Vector<String> fixes;

	/**
	 * Sets the name of this component.
	 * 
	 * @param name
	 *            Name of the component
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the name of this component.
	 * 
	 * @return The name of this component
	 */
	public String getName() {
		return name;
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
		fixes.add(pos, fixName);
	}

	/**
	 * Adds the name of a fix to the end of the list of fixes that have been
	 * applied to this component.
	 * 
	 * @param fixName
	 *            Name of the fix
	 */
	public void addFix(String fixName) {
		fixes.add(fixName);
	}

	/**
	 * Gets the list of fixes applied to this component as an array.
	 * 
	 * @return The list of fixes
	 */
	public Object[] getFixes() {
		return fixes.toArray();
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
		int pos = posOfFixInstalled(fixName);
		if (pos != -1)
			fixes.removeElementAt(pos);
		return pos;
	}

	/**
	 * Clears the list of applied fixes for this component.
	 */
	public void removeAll() {
		while (fixes.size() > 0)
			fixes.removeElementAt(0);
	}

	/**
	 * Determines if and where the given fixname has been applied.
	 * 
	 * @param fixname
	 *            Name of the fix
	 * @return The location in the list of applied fixes or -1 if not found
	 */
	public int posOfFixInstalled(String fixname) {
		for (int i = 0; i < fixes.size(); i++) {
			if (fixes.elementAt(i).equalsIgnoreCase(fixname))
				return i;
		}
		return -1;
	}

	/**
	 * Determines if the given fixname has been applied.
	 * 
	 * @param fixname
	 *            Name of the fix
	 * @return true if installed, false otherwise
	 */
	public boolean isFixInstalled(String fixname) {
		for (int i = 0; i < fixes.size(); i++) {
			if (fixes.elementAt(i).equalsIgnoreCase(fixname))
				return true;
		}
		return false;
	}

	/**
	 * Determines if the given fix is the most recently applied.
	 * 
	 * @param fixname
	 *            Name of the fix
	 * @return true if this fix was the most recently applied, false otherwise
	 */
	public boolean isFirstFix(String fixname) {
		if (fixes.size() > 0 && fixes.elementAt(0).equalsIgnoreCase(fixname))
			return true;
		return false;
	}

	/**
	 * Tells if two InstalledComponents are equal by comparing their names.
	 * 
	 * @param name
	 *            A String with the name of the component to compare
	 * @return true if the names are equal, false otherwise
	 */
	public boolean equals(String name) {
		if (this.name.equalsIgnoreCase(name))
			return true;
		return false;
	}

	/**
	 * Tells if two InstalledComponents are equal by comparing their names.
	 * 
	 * @param obj
	 *            An InstalledComponent to compare
	 * @return true if the objects have the same name, false otherwise
	 */
	public boolean equals(Object obj) {
		String cname;
		if (obj instanceof InstalledComponent)
			cname = ((InstalledComponent) obj).getName();
		else
			return false;

		if (cname.equalsIgnoreCase(name))
			return true;
		return false;
	}

	/**
	 * Computes this InstalledComponent's hash code.
	 * 
	 * @return the hashcode of the component's name
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Creates a InstalledComponent with the given name
	 * 
	 * @param name
	 *            Name of the component
	 */
	public InstalledComponent(String name) {
		fixes = new Vector<String>();
		setName(name);
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
		for (int i = 0; i < getFixes().length; i++)
			outfile.println("   " + getFixes()[i]);
		outfile.println("</" + getName() + ">");
	}

	/**
	 * Converts the current object to a String for printing.
	 * 
	 * @return A string representation of this object
	 */
	public String toString() {
		StringBuilder msg = new StringBuilder(getName() + "\n");

		Object[] fixes = getFixes();
		for (int x = 0; x < fixes.length; x++) {
			msg.append("   -");
			msg.append(fixes[x]);
			msg.append("\n");
		}

		return msg.toString();
	}
}
