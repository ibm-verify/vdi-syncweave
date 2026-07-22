/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.File;

/**
 * Allows wrapper scripts to see if the Update Installer is being updated by a
 * fix or rolled back.
 * 
 * @author Alan Watkins
 * 
 */
public class CheckForUpdateInstallerUpdate {

	/**
	 * Return code indicating an Update Installer update is required
	 */
	private static int UPDATE = 255;

	/**
	 * Return code indicating an Update Installer update is not required
	 */
	private static int NO_UPDATE = 254;

	/**
	 * main function - does checking for an Update Installer update
	 * 
	 * @param args
	 *            arguments in the form of one of the following:
	 * 
	 *            installDir backupDir -update zipFile installDir backupDir
	 *            -rollback
	 */
	public static void main(String args[]) {
		if (args.length < 3) {
			report(NO_UPDATE);
		}

		String installDir = args[0];
		String backupDir = args[1];

		try {
			if (args[2].equalsIgnoreCase("-update")) {
				if (args.length != 4)
					report(NO_UPDATE);

				String zipFile = args[3];

				if (!Unzip.isFileInArchive("TDI_Base.zip", zipFile))
					report(NO_UPDATE);

				boolean replUpdInst = Unzip.findFileInsideInnerZip(
						"maintenance/UpdateInstaller.jar", zipFile,
						"TDI_Base.zip");
				if (replUpdInst == true)
					report(UPDATE);
				report(NO_UPDATE);
			} else if (args[2].equalsIgnoreCase("-rollback")) {
				if (args.length != 3)
					report(NO_UPDATE);

				// Get the registry
				Registry registry = null;
				try {
					registry = new Registry(installDir);
				} catch (Exception e) {
					report(NO_UPDATE);
				}

				// Get name of last fix
				Object[] fixes = registry.getFixes();
				if (fixes == null || fixes.length == 0) {
					report(NO_UPDATE);
				}

				String fixName = (String) fixes[0];
				// If rolling back a fixpack, we need to get the real fix name
				// without a level on it
				if (fixName.matches(".*\\(.*\\)")) {
					fixName = fixName.substring(0, fixName.indexOf('(')).trim();
				}

				String updateInstallerDir = backupDir + "/" + fixName
						+ "/maintenance/UpdateInstaller.jar";
				File f = new File(updateInstallerDir);
				if (f.exists())
					report(UPDATE);
				else
					report(NO_UPDATE);
			} else { // No case...
				report(NO_UPDATE);
			}
		} catch (Exception e) {
			report(NO_UPDATE);
		}
		report(NO_UPDATE);
	}

	/**
	 * Exits reporting the return code specified so a script can access it.
	 * 
	 * @param rc
	 *            The return code
	 */
	public static void report(int rc) {
		System.exit(rc);
	}
}
