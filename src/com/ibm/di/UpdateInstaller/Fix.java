/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Represents a single fix. Populated by parsing the .manifest file inside the
 * associated fix file.
 * 
 * @author Alan Watkins
 * 
 */
public class Fix {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Name of the zip file which contains this fix
	 */
	private String fixFileName;

	/**
	 * A list of prereqs necessary to install this fix
	 */
	private Vector<String> preReqs;

	/**
	 * Minimum level TDI must be at to apply this fix
	 */
	private String minLevel = "0.0.0.0";

	/**
	 * Maximum level TDI can be at to apply this fix
	 */
	private String maxLevel = "9.9.9.9";

	/**
	 * The name of this fix
	 */
	private String fixName;

	/**
	 * The level of TDI this fix will bring the installation to
	 */
	private String level;

	/**
	 * If this fix contains an update to LWI, this variable will contain the
	 * configID for a rollback
	 */
	private String LWIConfigID = "";

	/**
	 * List of components this fix will update
	 */
	private Vector<String> components;

	private List<String> deleteFiles = new ArrayList<String>();
	
	/**
	 * Gets the fix filename.
	 * 
	 * @return The filename this object was created from.
	 */
	public String getFixFileName() {
		return fixFileName;
	}

	/**
	 * Gets pre-requisite fixes.
	 * 
	 * @return An array containing the names of pre-requisite fixes
	 */
	public Object[] getPreReqs() {
		return preReqs.toArray();
	}

	/**
	 * gets the fixname.
	 * 
	 * @return The name of this fix
	 */
	public String getfixName() {
		return fixName;
	}

	/**
	 * Adds a component to the list that this fix will update.
	 * 
	 * @param component
	 *            A TDI component name
	 */
	private void addComponent(String component) {
		components.add(component);
	}

	/**
	 * Adds a fix name to the list of pre-requisite fixes that this fix
	 * requires.
	 * 
	 * @param preReq
	 *            Name of the pre-requisite fix.
	 */
	private void addPreReq(String preReq) {
		preReqs.add(preReq);
	}

	/**
	 * Gets a list of the components this fix will update.
	 * 
	 * @return An array representing the components affected by this fix
	 */
	public Object[] getComponents() {
		return components.toArray();
	}

	/**
	 * Gets the LWI config ID needed to roll back this fix if present.
	 * 
	 * @return the LWI config ID
	 */
	public String getConfigID() {
		return LWIConfigID;
	}

	/**
	 * Sets the LWI config ID based on the specified JAR name.
	 * 
	 * @param jarName
	 *            An LWI fix JAR name
	 */
	public void setConfidIDFromJarName(String jarName) {
		LWIConfigID = jarName.substring(0, jarName.indexOf(".jar"));
	}

	/**
	 * Gets the minimum required TDI level to apply this fix.
	 * 
	 * @return Minimum required TDI level to apply this fix.
	 */
	public String getMinLevel() {
		return minLevel;
	}

	/**
	 * Gets the maximum required TDI level to apply this fix.
	 * 
	 * @return Maximum required TDI level to apply this fix.
	 */
	public String getMaxLevel() {
		return maxLevel;
	}

	/**
	 * Gets the level to which this fix will bring the current TDI installation.
	 * 
	 * @return The level TDI will be at after applying this fix, if applicable.
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * Removes a component from the list that this fix will update.
	 * 
	 * @param name
	 *            The TDI component name to be removed
	 */
	public void remove(String name) {
		for (int i = 0; i < components.size(); i++)
			if (name.equalsIgnoreCase(components.elementAt(i))) {
				components.remove(i);
				break;
			}
	}

	/**
	 * If this method is called, it will remove from the list of affected
	 * components any InstalledComponent with the same name from the specified
	 * list.
	 * 
	 * @param installedComponents
	 *            An InstalledComponent array of components
	 * @see InstalledComponent
	 */
	public void removeClobberedComponents(Object[] installedComponents) {
		for (int i = 0; i < installedComponents.length; i++) {
			InstalledComponent comp = (InstalledComponent) installedComponents[i];
			if (comp.posOfFixInstalled(fixName) >= 0)
				remove(comp.getName());
			System.out.println(UpdateInstallerMsgs.getString(
					"COMPONENT.FIX.APPLIED", comp.getName()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"COMPONENT.FIX.APPLIED", comp.getName()),
					UpdateInstallerMsgs.INFO);
		}
	}

	/**
	 * Class constructor that creates a Fix from the specified file.
	 * 
	 * @param fixFile
	 *            Name of a zip file representing a TDI fix
	 * @throws Exception
	 *             If an exception occurs while reading the fix file
	 */
	public Fix(String fixFile) throws Exception {
		this.fixFileName = fixFile;
		fixName = "";
		level = "";
		components = new Vector<String>();
		preReqs = new Vector<String>();
		String lines[] = Unzip.unzipAndReadSingleFile(".manifest", fixFileName);
		int rc = 0;
		if (lines == null)
			rc = -1;
		else
			rc = populate(lines);
		if (rc == -1) {
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIX.FILE.READ.ERR", fixFile), UpdateInstallerMsgs.DEBUG);
			throw new Exception(UpdateInstallerMsgs.getString(
					"FIX.FILE.READ.ERR", fixFile));
		}
	}

	/**
	 * Populates the object's structures with information from the data in a
	 * .manifest file.
	 * 
	 * @param fixFileData
	 *            .manifest file data
	 * @return 0 if successful, -1 on failure
	 */
	private int populate(String[] fixFileData) {
		if (fixFileData == null) {
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("EMPTY.FIX.FILE"), UpdateInstallerMsgs.DEBUG);
			return -1;
		}

		for (int i = 0; i < fixFileData.length; i++) {
			if (fixFileData[i].trim().isEmpty())
				continue;
			if (fixFileData[i].equalsIgnoreCase("<" + FixUtils.NAME + ">")) {
				i++;
				while (!fixFileData[i].equalsIgnoreCase("</" + FixUtils.NAME
						+ ">")) {
					fixName = fixFileData[i];
					i++;
				}
			} else if (fixFileData[i].equalsIgnoreCase("<" + FixUtils.PREREQ
					+ ">")) {
				i++;
				while (!fixFileData[i].equalsIgnoreCase("</" + FixUtils.PREREQ
						+ ">")) {
					if (minLevel.equals("0.0.0.0"))
						minLevel = fixFileData[i];
					else if (maxLevel.equals("9.9.9.9"))
						maxLevel = fixFileData[i];
					else
						addPreReq(fixFileData[i]);
					i++;
				}
			} else if (fixFileData[i].equalsIgnoreCase("<" + FixUtils.LEVEL
					+ ">")) {
				i++;
				while (!fixFileData[i].equalsIgnoreCase("</" + FixUtils.LEVEL
						+ ">")) {
					level = fixFileData[i];
					i++;
				}
			} else if (fixFileData[i].equalsIgnoreCase("<DELETE_FILES>")) {
				i++;
				while (!fixFileData[i].equalsIgnoreCase("</DELETE_FILES>")) {
					if (!fixFileData[i].isEmpty())
						deleteFiles.add(fixFileData[i]);
					i++;
					if (i >= fixFileData.length) {
						UpdateInstallerMsgs.log("Syntax error in .manifest", UpdateInstallerMsgs.DEBUG);
						return -1;						
					}
				}
			} else {
				for (String component: FixUtils.getComponents()) {
					if (fixFileData[i].equalsIgnoreCase("<" + component + ">")) {
						addComponent(component);
						i++;
						while (!fixFileData[i].equalsIgnoreCase("</" + component + ">")) {
							if (component.equalsIgnoreCase(FixUtils.EWP))
								LWIConfigID = fixFileData[i].trim();
							i++;
						}
						break;
					}
				}
			}
		}

		return 0;
	}

	/**
	 * Indicates if the specified component will be updated by this fix.
	 * 
	 * @param component
	 *            The component in question
	 * @return true or false indicating if the component is in this fix's update
	 *         list
	 */
	public boolean isUpdating(String component) {
		for (int i = 0; i < components.size(); i++)
			if (components.elementAt(i).equals(component))
				return true;
		return false;
	}

	/**
	 * Outputs information about this fix to standard out.
	 */
	public void dump() {
		StringBuilder prereqinfo = new StringBuilder();
		if (preReqs.size() == 0)
			prereqinfo.append(FixUtils.NONE + "\n");
		else
			for (int i = 0; i < preReqs.size(); i++) {
				prereqinfo.append(preReqs.elementAt(i));
				prereqinfo.append("\n");
			}

		StringBuilder componentinfo = new StringBuilder();

		for (int i = 0; i < components.size(); i++) {
			componentinfo.append(components.elementAt(i));
			componentinfo.append("\n");
		}

		System.out.println(UpdateInstallerMsgs.getString("FIX.DUMP",
				fixFileName, fixName, minLevel, maxLevel, prereqinfo,
				componentinfo));
		if (!LWIConfigID.equals(""))
			System.out.println(UpdateInstallerMsgs.getString("FIX.DUMP.EWP",
					LWIConfigID));
	}
	
	public List<String> getDeleteFiles() {
		return deleteFiles;
	}
}
