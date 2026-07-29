/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.File;

/**
 * Allows wrapper scripts to replace UpdateInstaller.jar if the Update Installer
 * is being updated by a fix
 * 
 * @author Praveen Thakur
 * 
 */
public class ReplaceUpdateInstallerJar {

	/**
	 * main function - Replace an UpdateInstaller.jar with the one which is
	 * 				   present in fix pack Zip file. This should be called after
	 * 				   calling CheckForUpdateInstallUpdate and only when the return
	 * 				   code from CheckForUpdateInstallUpdate is 255.
	 * 
	 * @param args
	 *            arguments in the form of one of the following:
	 * 
	 *            installDir backupDir -update zipFile installDir backupDir
	 *            -rollback
	 */
	
	/**
	 * String representing a quote character
	 */
	private static final String QUOTE = "\"";
	
	
	public static void main(String args[]) {
		if (args.length < 3) {
			return;
		}

		String installDir = args[0];
		String backupDir = args[1];

		try {
			if (args[2].equalsIgnoreCase("-update")) {
				if (args.length != 4)
					return;

				String zipFile = args[3];
				replaceJar(installDir, backupDir, zipFile);

			} else if (args[2].equalsIgnoreCase("-rollback")) {
				if (args.length != 3)
					return;

				// Get the registry
				Registry registry = null;
				try {
					registry = new Registry(installDir);
				} catch (Exception e) {
					return;
				}

				// Get name of last fix
				Object[] fixes = registry.getFixes();
				if (fixes == null || fixes.length == 0) {
					return;
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
					return;
				
			} else { // No case...
				return;
			}
		} catch (Exception e) {
			return;
		}
	}

	/**
	 * Replaces existing UpdateInstaller.jar also takes backup of the same.
	 * 	 */
	public static void replaceJar(String InstDir, String bacDir, String zf) {
		
		String topBackupDir = bacDir; // used during cleaning
		Unzip.setDefaultExtractionLocation(topBackupDir);
		
		//Unzip "TDI_Base.zip" to topBackupDir
		Unzip.unzipSingleBinaryFile("TDI_Base.zip", zf, false);
		
		//Extracting TDI_Base.zip temporarily to
		//topBackupDir + "/UpdateInstaller"
		Unzip.unzipToDirectory("TDI_Base.zip", topBackupDir + "/UpdateInstaller", null);
		
		String os = FixUtils.getOS();
		
		//Copy UpdateInstaller.jar to <InstallDir>\maintenance directory
		if (os.startsWith(FixUtils.WINDOWS)) {
			FixUtils.executeCommand(new String[] { "cmd", "/c", QUOTE+"copy",
					"/V", "/Y", QUOTE+topBackupDir+ "/UpdateInstaller/maintenance\\UpdateInstaller.jar" + QUOTE + " " + QUOTE + InstDir + "/maintenance\\UpdateInstaller.jar" + QUOTE+QUOTE });
		} else {
			String shell = "/bin/sh";
			if (os.startsWith(FixUtils.I5OS))
				shell = "/bin/qsh";
			FixUtils.executeCommand(new String[] { shell, "-c", "cp",
					"-f", topBackupDir + "/UpdateInstaller/maintenance/UpdateInstaller.jar " +  InstDir + "/maintenance/UpdateInstaller.jar"});
		}
		

		// Delete UpdateInstaller directory/subdirectories

		if (os.startsWith(FixUtils.WINDOWS)) {
			FixUtils.executeCommand(new String[] { "cmd", "/c", QUOTE+"rmdir",
					"/S", "/Q", QUOTE+topBackupDir+ "/UpdateInstaller" + QUOTE+QUOTE });

		} else {
			String shell = "/bin/sh";
			if (os.startsWith(FixUtils.I5OS))
				shell = "/bin/qsh";
			FixUtils.executeCommand(new String[] { shell, "-c", "rm",
					"-rf", topBackupDir + "/UpdateInstaller" });
		}
		
		//Delete TDI_Base.zip		
		if (os.startsWith(FixUtils.WINDOWS)) {
			FixUtils.executeCommand(new String[] { "cmd", "/c", QUOTE+"del",
					"/F", "/Q", QUOTE+topBackupDir+ "\\TDI_Base.zip" + QUOTE+QUOTE });

		} else {
			String shell = "/bin/sh";
			if (os.startsWith(FixUtils.I5OS))
				shell = "/bin/qsh";
			FixUtils.executeCommand(new String[] { shell, "-c", "rm",
					"-f", topBackupDir + "/TDI_Base.zip" });
		}
		
	}
}
