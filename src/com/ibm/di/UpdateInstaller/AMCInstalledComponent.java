/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.PrintWriter;

/**
 * Represents an AMC component. Different from a regular InstalledComponent in
 * that it also must remember whether or not AMC was deployed during the initial
 * installation of the component.
 * 
 * @author Alan Watkins
 * 
 */
public class AMCInstalledComponent extends InstalledComponent {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Indicates if AMC was deployed during the initial installation of TDI.
	 */
	private boolean deferredDeployment;

	/**
	 * internal variable representing if deferredDeployment has been set yet.
	 */
	private boolean deploymentSet = false;

	/**
	 * Sets whether or not AMC deployment occurred during the initial
	 * installation of TDI.
	 * 
	 * @param deferred
	 *            true or false
	 */
	public void setDeferredDeployment(boolean deferred) {
		deferredDeployment = deferred;
		deploymentSet = true;
	}

	/**
	 * Sets whether or not AMC deployment occurred during the initial
	 * installation of TDI.
	 * 
	 * @param deferred
	 *            "true" or "false"
	 */
	public void setDeferredDeployment(String deferred) {
		setDeferredDeployment(Boolean.parseBoolean(deferred.trim()));
	}

	/**
	 * Gets a value indicating whether or not AMC deployment was deferred during
	 * the initial TDI installation.
	 * 
	 * @return true or false
	 */
	public boolean getDeferredDeployment() {
		return deferredDeployment;
	}

	/**
	 * Class constructor specifying the name of this component.
	 * 
	 * @param name
	 *            Component name
	 */
	public AMCInstalledComponent(String name) {
		super(name);
		deferredDeployment = false;
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
		if (deploymentSet)
			fixes.add(pos, fixName);
		else
			setDeferredDeployment(fixName);
	}

	/**
	 * Adds the name of a fix to the end of the list of fixes that have been
	 * applied to this component.
	 * 
	 * @param fixName
	 *            Name of the fix
	 */
	public void addFix(String fixName) {
		if (deploymentSet)
			fixes.add(fixName);
		else
			setDeferredDeployment(fixName);
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
		outfile.println("   " + deferredDeployment);
		for (int i = 0; i < getFixes().length; i++) {
			outfile.println("   " + getFixes()[i]);
		}
		outfile.println("</" + getName() + ">");
	}

	/**
	 * Tells if two AMCInstalledComponents are equal by comparing their names.
	 * 
	 * @param obj
	 *            An InstalledComponent to compare
	 * @return true if the objects have the same name, false otherwise
	 */
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * Tells if two InstalledComponents are equal by comparing their names.
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
		StringBuilder msg = new StringBuilder(getName() + "\n");

		Object[] fixes = getFixes();
		if (deferredDeployment == true)
			msg.append("   " + UpdateInstallerMsgs.getString("DEFER.TRUE")
					+ "\n");
		else
			msg.append("   " + UpdateInstallerMsgs.getString("DEFER.FALSE")
					+ "\n");

		for (int x = 0; x < fixes.length; x++) {
			msg.append("   -");
			msg.append(fixes[x]);
			msg.append("\n");
		}
		return msg.toString();
	}
}
