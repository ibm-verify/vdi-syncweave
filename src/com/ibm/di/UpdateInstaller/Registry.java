/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Represents the current TDI installation including installed components and
 * fixes that have been applied.
 * 
 * @author Alan Watkins
 * 
 */
public class Registry {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * TDI install directory
	 */
	private String installDir;

	/**
	 * Indicates if this is a General Purpose or Identity installation
	 */
	private String edition = FixUtils.GENERAL;

	/**
	 * Indicates if this is a Trial or Full TDI installation
	 */
	private String licenseType = FixUtils.TRIAL;

	/**
	 * Fixes applied to this TDI installation
	 */
	private Vector<String> fixes_applied;

	/**
	 * The current level of this TDI install; starts at 7.1.0.0
	 */
	private String level;

	/**
	 * TDI components included in this TDI installation
	 */
	private Vector<InstalledComponent> components;

	/**
	 * Name of the .registry file
	 */
	private String registryFilename;

	/**
	 * Indicates if we are in a clobber case. The idea behind clobber is that if
	 * we are in a clobber case, we don't need to update the main fix list in
	 * the registry.
	 */
	private boolean clobber = false;

	/**
	 * Sets the clobber variable
	 * 
	 * @param c
	 *            true or false indicating if we are in a clobber case
	 */
	public void setClobber(boolean c) {
		clobber = c;
	}

	/**
	 * Gets the clobber variable
	 * 
	 * @return true if this is a clobber case, false otherwise
	 */
	public boolean getClobber() {
		return clobber;
	}

	/**
	 * Sets the level variable
	 * 
	 * @param l
	 *            The TDI level
	 */
	public void setLevel(String l) {
		level = l;
	}

	/**
	 * Gets the TDI level
	 * 
	 * @return The TDI level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * Gets the install directory location
	 * 
	 * @return TDI install directory
	 */
	public String getInstallDir() {
		return installDir;
	}

	/**
	 * Gets the edition
	 * 
	 * @return The edition of this install as indicated in FixUtils
	 * @see FixUtils
	 */
	public String getEdition() {
		return edition;
	}
	
	/**
	 * Gets the license type
	 * 
	 * @return The license type of this install as indicated in FixUtils
	 * @see FixUtils
	 */
	public String getLicenseType() {
		return licenseType;
	}

	/**
	 * Sets the license type
	 * 
	 * @see FixUtils
	 */
	public void setLicenseType(String ltype) {
		licenseType=ltype;
	}
	

	/**
	 * Gets the fixes applied to this TDI installation
	 * 
	 * @return An array with a list of fixes applied
	 */
	public Object[] getFixes() {
		return fixes_applied.toArray();
	}

	/**
	 * Gets the components installed for this TDI installation
	 * 
	 * @return An array representation of the installed TDI components
	 */
	public Object[] getInstalledComponents() {
		return components.toArray();
	}

	/**
	 * Gets the object representing the specified name if this TDI installation
	 * has it installed.
	 * 
	 * @param name
	 *            The name of the component to look for
	 * @return The object representing the specified component or null if not
	 *         found
	 */
	public InstalledComponent getComponent(String name) {
		for (int i = 0; i < components.size(); i++)
			if (components.elementAt(i).equals(name))
				return components.elementAt(i);
		return null;
	}

	/**
	 * Class constructor that takes an installation directory
	 * 
	 * @param installDir
	 *            The TDI installation directory
	 * @throws Exception
	 *             If an error occurs while reading the .registry file in the
	 *             specified location
	 */
	public Registry(String installDir) throws Exception {
		this.installDir = installDir;
		fixes_applied = new Vector<String>();
		components = new Vector<InstalledComponent>();
		int rc = populate();
		if (rc != 0) {
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("REG.POP.PROB"), UpdateInstallerMsgs.DEBUG);
			throw new Exception(UpdateInstallerMsgs.getString("REG.POP.PROB"));
		}
	}

	/**
	 * Populates the object's structures with information from the .registry
	 * file
	 * 
	 * @return 0 if successful, -1 on error
	 */
	private int populate() {
		int rc = 0;
		registryFilename = installDir + "/.registry";
		File registry = new File(registryFilename);
		if (!registry.exists()) {
			System.out.println(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.NOT.FOUND"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.NOT.FOUND"),
					UpdateInstallerMsgs.ERROR);
			rc = -1;
			return rc;
		}

		String[] registryFile = FixUtils.readFile(registryFilename);
		if (registryFile == null) {
			System.out.println(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.NOT.FOUND"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.NOT.FOUND"),
					UpdateInstallerMsgs.ERROR);
			rc = -1;
			return rc;
		}

		for (int i = 0; i < registryFile.length; i++) {
			if (registryFile[i].equalsIgnoreCase("<" + FixUtils.FIXES + ">")) {
				i++;
				while (!registryFile[i].equalsIgnoreCase("</" + FixUtils.FIXES
						+ ">")) {
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"APPL.FIX.FOUND", registryFile[i]),
							UpdateInstallerMsgs.DEBUG);
					fixes_applied.add(registryFile[i]);
					i++;
				}
			} else if (registryFile[i].equalsIgnoreCase("<" + FixUtils.EDITION
					+ ">")) {
				i++;
				while (!registryFile[i].equalsIgnoreCase("</"
						+ FixUtils.EDITION + ">")) {
					edition = registryFile[i];
					i++;
				}
			} else if (registryFile[i].equalsIgnoreCase("<" + FixUtils.LICENSE
					+ ">")) {
				i++;
				while (!registryFile[i].equalsIgnoreCase("</"
						+ FixUtils.LICENSE + ">")) {
					licenseType = registryFile[i];
					i++;
				}
			} else if (registryFile[i].equalsIgnoreCase("<" + FixUtils.LEVEL
					+ ">")) {
				i++;
				while (!registryFile[i].equalsIgnoreCase("</" + FixUtils.LEVEL
						+ ">")) {
					level = registryFile[i];
					i++;
				}
			} else
				for (int x = 0; x < FixUtils.getComponents().length; x++)
					if (registryFile[i].equalsIgnoreCase("<"
							+ FixUtils.getComponents()[x] + ">")) {
						add_component(FixUtils.getComponents()[x]);
						i++;
						while (!registryFile[i].equalsIgnoreCase("</"
								+ FixUtils.getComponents()[x] + ">")) {
							add_component_fix_applied(
									FixUtils.getComponents()[x],
									registryFile[i]);
							i++;
						}
					}
		}
		return rc;
	}

	/**
	 * During registry file processing, adds a fix to the specified component's
	 * list; creates that component if necessary.
	 * 
	 * @param component
	 *            The name of the component being worked on
	 * @param fixName
	 *            The name of the fix to be added to the list
	 * @see FixUtils
	 */
	private void add_component_fix_applied(String component, String fixName) {
		for (int i = 0; i < components.size(); i++)
			if (components.elementAt(i).equals(component)) {
				components.elementAt(i).addFix(fixName);
				return;
			}

		InstalledComponent fc;
		if (component.equals(FixUtils.EWP))
			fc = new LWIInstalledComponent(component);
		else if (component.equals(FixUtils.AMC))
			fc = new AMCInstalledComponent(component);
		else
			fc = new InstalledComponent(component);
		fc.addFix(fixName);
		components.add(fc);
	}

	/**
	 * Creates a new TDI component object of an appropriate type based on the
	 * component name specified.
	 * 
	 * @param component
	 *            Name of the component to create
	 * @see FixUtils
	 */
	private void add_component(String component) {
		InstalledComponent fc;
		if (component.equals(FixUtils.EWP))
			fc = new LWIInstalledComponent(component);
		else if (component.equals(FixUtils.AMC))
			fc = new AMCInstalledComponent(component);
		else
			fc = new InstalledComponent(component);
		components.add(fc);
	}

	/**
	 * Tells if a certain component is installed
	 * 
	 * @param component
	 *            The name of the component to search for
	 * @return true if the component was found, false otherwise
	 * @see FixUtils
	 */
	public boolean isInstalled(String component) {
		for (int i = 0; i < components.size(); i++)
			if (components.elementAt(i).equals(component))
				return true;
		return false;
	}

	/**
	 * Updates the .registry file during a rollback operation
	 */
	public void restore() {
		update(null, false);
	}

	/**
	 * Updates the .registry file after a rollback or update.
	 * 
	 * @param fix
	 *            The fix we are adding or null if we are rolling back
	 * @param clean
	 *            If true, clear the registry
	 * @return 0 if the update was successful, nonzero otherwise
	 */
	public int update(Fix fix, boolean clean) {
		if (fix != null) { // updating...
			// Note that clean and clobber should not occur together.
			if (clean)
				fixes_applied.removeAllElements();

			if (!clobber) { // In the clobber case, we are reapplying a fix for
				// an added feature
				String fixlevel = fix.getLevel();
				if (fixlevel.equals(""))
					fixes_applied.add(0, fix.getfixName());
				else {
					// This is where we will save the previous level for
					// rollback info
					fixes_applied.add(0, fix.getfixName() + "(" + level + ")");
					setLevel(fixlevel);
				}
			}

			for (int i = 0; i < components.size(); i++) {
				InstalledComponent cur_comp = components.get(i);
				if (clean)
					cur_comp.removeAll();
				if (fix.isUpdating(cur_comp.getName())) {
					if (cur_comp.getName().equals(FixUtils.EWP))
						cur_comp.addFix(0, fix.getConfigID());

					cur_comp.addFix(0, fix.getfixName());
				}
			}
		} else { // rollback....
			String fixName = fixes_applied.firstElement();
			fixes_applied.remove(0);

			if (fixName.matches(".*\\(.*\\)"))
				fixName = fixName.substring(0, fixName.indexOf('(')).trim();

			for (int i = 0; i < components.size(); i++) {
				InstalledComponent cur_comp = components.get(i);
				if (cur_comp.isFirstFix(fixName))
					cur_comp.remove(fixName);
			}
		}
        
		return updateCommon();
	}

	/**
	 * performs actual writing of the .registry during an update.
	 * 
	 * @return 0 if the update was successful, nonzero otherwise
	 */

	public int updateCommon()
	{
		int rc = 0;
		
		UpdateInstallerMsgs.log(UpdateInstallerMsgs
				.getString("UPDATING.REGISTRY"), UpdateInstallerMsgs.DEBUG);

		// Open file and prepare for writing
		FileWriter outputFile = null;
		try {
			outputFile = new FileWriter(registryFilename);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.WRITE.ERROR"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("REGISTRY.FILE.WRITE.ERROR"),
					UpdateInstallerMsgs.ERROR);
			rc = -1;
			return rc;
		}
		
		PrintWriter outfile = new PrintWriter(outputFile);
		
		// Fixes section
		outfile.println("<" + FixUtils.FIXES + ">");
		for (int i = 0; i < fixes_applied.size(); i++)
			outfile.println("   " + fixes_applied.elementAt(i));
		outfile.println("</" + FixUtils.FIXES + ">");

		// Edition section
		outfile.println("<" + FixUtils.EDITION + ">");
		outfile.println("   " + edition);
		outfile.println("</" + FixUtils.EDITION + ">");

		// License type section
		outfile.println("<" + FixUtils.LICENSE + ">");
		outfile.println("   " + licenseType);
		outfile.println("</" + FixUtils.LICENSE + ">");

		// Level section
		outfile.println("<" + FixUtils.LEVEL + ">");
		outfile.println("   " + level);
		outfile.println("</" + FixUtils.LEVEL + ">");

		// Components section
		for (int x = 0; x < components.size(); x++) {
			InstalledComponent cur_comp = components.get(x);
			cur_comp.writeFixes(outfile);
		}

		// Close the file
		try {
			outfile.close();
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", registryFilename, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", registryFilename, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			rc = -1;
		}
		return rc;		
	}
	
	/**
	 * Outputs information about this TDI installation to standard out.
	 */
	public void dump() {
		StringBuilder fixinfo = new StringBuilder();
		if (fixes_applied.size() == 0)
			fixinfo.append(FixUtils.NONE);
		else
			for (int i = 0; i < fixes_applied.size(); i++) {
				fixinfo.append(fixes_applied.elementAt(i));
			}

		StringBuilder componentinfo = new StringBuilder();
		for (int i = 0; i < components.size(); i++) {
			componentinfo.append(components.elementAt(i));
		}

		System.out.println(UpdateInstallerMsgs.getString("REGISTRY.DUMP",
				installDir, edition, level, licenseType, fixinfo, componentinfo));
	}

	public static String getFixName(String fix)
	{
		int pos = fix.indexOf('(');
		return pos==-1?fix:fix.substring(0,fix.indexOf('('));
	}
}
