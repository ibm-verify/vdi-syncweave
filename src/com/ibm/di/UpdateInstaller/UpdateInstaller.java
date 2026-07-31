/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UpdateInstaller is the class that handles applying TDI fixes. It is not
 * intended that instances of this class be instantiated, but rather that this
 * class' main function be executed. The UpdateInstaller uses a zip file which
 * represents a TDI fix along with a TDI installation's .registry file to
 * install maintenance. The applyUpdates.bat/sh scripts in the product's bin
 * directory are the accepted ways of running the update installer.
 *
 * The update installer can be run to update, rollback, show information about a
 * fix, or to show information about the current TDI installation.
 *
 * @author Alan Watkins
 *
 */
public class UpdateInstaller {
	/**
	 * The copyright notice for binary java code required by legal.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Enumerated type representing the valid options for the Update Installer
	 */
	private enum FIXTYPE {
		/**
		 * -update
		 */
		UPDATE,
		/**
		 * -rollback
		 */
		ROLLBACK,
		/**
		 * -queryfix
		 */
		QUERYFIX,
		/**
		 * -queryreg
		 */
		QUERYREG,
		/**
		 * -enroll
		 */
		ENROLL
	};

	/**
	 * Represents the state the update installer was run under.
	 */
	private FIXTYPE fixtype;

	/**
	 * String representing a quote character
	 */
	private static final String QUOTE = "\"";

	/**
	 * TDI installation directory
	 */
	private String installDir;

	/**
	 * Directory for backed up files for the current fix. This is a child
	 */
	private String backupDir;

	/**
	 * Backup directory. This directory may contain backup directories for
	 * several fixes.
	 */
	private String topBackupDir;

	/**
	 * Zip file representing a TDI fix.
	 */
	private String zipFile;

	/**
	 * Represents the current operating system.
	 */
	private String os;

	/**
	 * The clean flag is set if we want all backup directories to be cleared out
	 * during a fixpack installation.
	 */
	private boolean clean;

	/**
	 * The silent flag will suppress the confirmation prompt presented before
	 * clearing out backup directories when the clean flag is set.
	 */
	private boolean silent;

	/**
	 * The UnzipError flag will check for error while Unzipping the Directory Structure.
	 * Return -1 : If Error Occurs
	 * Return 0 : Unzip Directory Successfully.
	 */
	private int unzipError;

	/**
	 * The cmdExecutionError flag checks for execution of DOS Commands
	 * Return 0 : Successful execution of Command
	 * Return nonzero : If error occurs
	 */
	private int cmdExecuteError;

	/**
	 * UpdateInstaller main function. The update installer is invoked through
	 * this method.
	 *
	 * @param args
	 *            Array representing command line arguments to the program.<br/>
	 *            Usage:<br>
	 *
	 *            <pre>
	 * UpdateInstaller  -queryreg |
	 *                  -rollback |
	 *                  -update fix_file.zip [-clean [-silent]] |
	 *                  -queryfix fix_file.zip |
	 *                  -enroll fix_file.zip
	 * </pre>
	 *
	 */
	public static void main(String[] args) {
		UpdateInstaller p = new UpdateInstaller();
		p.installUpdateLaunch(args);
	}

	/**
	 * Method invoked after creating a UpdateInstaller instance.
	 *
	 * @param args
	 *            Command line arguments
	 * @see #main(String[] args)
	 */
	private void installUpdateLaunch(String[] args) {
		int argsOK = parseArgsAndSetup(args);
		switch (argsOK) {
		case 0:
			break;
		case -2:
			return;
		default:
			usage();
			return;
		}

		if (fixtype == FIXTYPE.UPDATE)
			installFix();
		else if (fixtype == FIXTYPE.ROLLBACK)
			rollbackFix();
		else if (fixtype == FIXTYPE.QUERYFIX)
			queryFix();
		else if (fixtype == FIXTYPE.QUERYREG)
			queryReg();
		else if (fixtype == FIXTYPE.ENROLL)
			enrollLicense();

		FileGarbageCan.empty();
	}

	/**
	 * LUM license enrollment has been removed as part of open-source preparation.
	 * This method is retained as a stub to preserve the call-site interface.
	 *
	 * @return 0 always
	 */
	private int enrollLicense() {
		return 0;
	}

	/**
	 * Displays information about a TDI fix file. Used in conjunction with the
	 * -queryfix command line argument.
	 *
	 * @return 0 if successful, nonzero otherwise
	 */
	private int queryFix() {
		Fix fix = null;
		int rc = 0;
		try {
			fix = new Fix(zipFile);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString("FIX.FILE.ERROR",
					zipFile));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIX.FILE.ERROR", zipFile), UpdateInstallerMsgs.ERROR);
			rc = -1;
		}
		if (rc == 0)
			fix.dump();

		return rc;
	}

	/**
	 * Displays information about the current TDI installation. Used in
	 * conjunction with the -queryreg command line argument.
	 *
	 * @return 0 if successful, nonzero otherwise
	 */

	private int queryReg() {
		Registry registry = null;
		try {
			registry = new Registry(installDir);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString("FIX.FILE.ERROR",
					installDir));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIX.FILE.ERROR", installDir), UpdateInstallerMsgs.ERROR);
			return -1;
		}
		registry.dump();
		return 0;
	}

	/**
	 * Rolls back the most recently applied fix. Updates the .registry file
	 * based on the rollback.
	 *
	 * @return 0 if successful, nonzero otherwise
	 */
	private int rollbackFix() {
		int rc = -1;
		// 1. Read registry
		// 2. Get name of last fix
		// 3. Restore based on fix name
		// 4. Rollback Tag Files
		// 5. Update registry

		// 1. Read registry
		UpdateInstallerMsgs.log(UpdateInstallerMsgs
				.getString("READING.REGISTRY"), UpdateInstallerMsgs.DEBUG);

		Registry registry = null;
		try {
			registry = new Registry(installDir);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"REGISTRY.PROCESS.ERROR", installDir));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"REGISTRY.PROCESS.ERROR", installDir),
					UpdateInstallerMsgs.ERROR);
			return rc;
		}

		// 2. Get name of last fix
		Object[] fixes = registry.getFixes();
		if (fixes == null || fixes.length == 0) {
			System.out.println(UpdateInstallerMsgs
					.getString("ROLLBACK.NO.FIXES"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("ROLLBACK.NO.FIXES"), UpdateInstallerMsgs.INFO);
			return rc;
		}

		String fixName = (String) fixes[0];
		// If we're rolling back and this will result in a level change, we need
		// to signal that
		if (fixName.matches(".*\\(.*\\)")) {
			String level = fixName.substring(fixName.indexOf('(') + 1, fixName
					.indexOf(')'));
			fixName = fixName.substring(0, fixName.indexOf('(')).trim();
			registry.setLevel(level);
		}

		String rollbackDir = backupDir + "/" + fixName;
		System.out.println(UpdateInstallerMsgs.getString("ROLLBACK.GOING",
				fixName, rollbackDir));
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("ROLLBACK.GOING",
				fixName, rollbackDir), UpdateInstallerMsgs.INFO);

		// 3. Restore based on fix name
		// AMC - Figure out if we need to call amcmigrate
		boolean runmigrate = false;
//		if (registry.isInstalled(FixUtils.AMC)) {
//			runmigrate = !((AMCInstalledComponent) registry
//					.getComponent(FixUtils.AMC)).getDeferredDeployment();
//			if (runmigrate) {
//				File f = new File(rollbackDir + "/amc/tdiamc.war");
//				runmigrate = f.exists();
//			}
//		}
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
				"CALL.AMC.MIGRATE", runmigrate), UpdateInstallerMsgs.DEBUG);

		// LWI - Get the name of the configID to use when calling lwiUpdate for
		// a rollback
		String lwiJarName = "";
		String lwiServiceName = "";
//		if (registry.isInstalled(FixUtils.EWP)) {
//			LWIInstalledComponent lwi_comp = ((LWIInstalledComponent) registry
//					.getComponent(FixUtils.EWP));
//			if (lwi_comp != null) {
//				if (lwi_comp.isFirstFix(fixName))
//					lwiJarName = (String) lwi_comp.getConfigIDs()[0];
//				lwiServiceName = lwi_comp.getServiceName();
//			}
//		}
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("LWI.JAR.NAME",
				lwiJarName), UpdateInstallerMsgs.DEBUG);
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("LWI.SVC.NAME",
				lwiServiceName), UpdateInstallerMsgs.DEBUG);

		// Stop the AM servers if we are rolling back AM jars
//		File AMJars = new File(rollbackDir + "/bin/amc/ActionManager/jars");
//		if (AMJars.exists())
//			stopAMServers(lwiServiceName);

		prepareForCERollback(rollbackDir, installDir);
		deleteNewFilesFrom(rollbackDir);

		if (os.startsWith(FixUtils.WINDOWS)) {
			FixUtils.executeCommand(new String[] { "cmd", "/c", QUOTE+"xcopy", "/Y",
					"/K", "/R", "/I", "/V", "/E", QUOTE+rollbackDir+QUOTE, QUOTE+installDir+QUOTE+QUOTE });
			if (runmigrate) {
				if (lwiServiceName.trim().equals("")) {
					FixUtils.executeCommand(new String[] { "cmd", "/c",
							QUOTE+QUOTE+installDir + "\\bin\\amc\\migrateamc.bat"+QUOTE, "-d",
							QUOTE+backupDir + "\\backup_tdiamc_" + fixName+QUOTE,
							"-rollback"+QUOTE });
				} else {
					FixUtils.executeCommand(new String[] { "cmd", "/c",
							QUOTE+QUOTE+installDir + "\\bin\\amc\\migrateamc.bat"+QUOTE, "-d",
							QUOTE+backupDir + "\\backup_tdiamc_" + fixName+QUOTE,
							"-rollback", "-s", lwiServiceName+QUOTE });
				}
			}

			if (!lwiJarName.trim().equals("")) {
				FixUtils.executeCommand(new String[] { "cmd", "/c",
						QUOTE+QUOTE+installDir + "\\bin\\amc\\lwiUpdate.bat"+QUOTE,
						"-rollback",
						lwiJarName,
						lwiServiceName+QUOTE });
			}
		} else {
			String shell = "/bin/sh";
			if (os.startsWith(FixUtils.I5OS))
				shell = "/bin/qsh";
			FixUtils.executeCommand(new String[] { shell, "-c",
					"cp -r " + rollbackDir + "/* " + installDir });

			if (runmigrate) {
				FixUtils.executeCommand(new String[] {
						installDir + "/bin/amc/migrateamc.sh", "-d",
						backupDir + "/backup_tdiamc_" + fixName, "-rollback" });
			}

			if (!lwiJarName.equals("")) {
				FixUtils.executeCommand(new String[] {
						installDir + "/bin/amc/lwiUpdate.sh", "-rollback",
						lwiJarName });
			}
		}

		// 4. Rollback Tag Files
		rollbackTagFiles(registry);

		// 5. Restore
		registry.restore();
		rc = 0;
		return rc;
	}

	private void deleteNewFilesFrom(String backupDir) {
		File inFile = new File(backupDir, ".newFiles");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(inFile));
			String name;
			while ((name = in.readLine()) != null) {
				if (!name.isEmpty())
					new File(name).delete();
			}
		} catch (Exception e) {
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e1) {}
		}
		inFile.delete();
	}

	/**
	 * Removes corresponding .fxtag file
	 */
		private void rollbackTagFiles(Registry reg)
		{
			String fixlevel = reg.getLevel();
			File tagFileDir = new File( installDir + "/properties/version");
			final String FILENAME = "IBM_Tivoli_Directory_Integrator." + fixlevel + ".fxtag";
			File[] arrFile = tagFileDir.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return (name.matches(FILENAME));
				}
			});

			if (arrFile.length != 0)
				arrFile[0].delete();

		}

	/**
	 * Shuts down CEe
	 */
//	private int shutdownCE()
//	{
//		String cmds[];
//		if (os.startsWith(FixUtils.WINDOWS)) {
//			cmds = new String[] { "cmd", "/c", (QUOTE+QUOTE+installDir + "\\ibmditk.bat"+QUOTE),
//					"-tdishutdown", "-nosplash", "-data", getCEWorkspace()+QUOTE};
//
//		} else {
//			cmds = new String[] { (installDir + "/ibmditk"), "-tdishutdown",
//					"-nosplash", "-data", getCEWorkspace() };
//		}
//		return FixUtils.executeCommand(cmds);
//	}
	/**
	 * Names of jars may have changed when we applied fixes to CE or CE Update
	 * Site
	 *
	 * @param rollbackDir
	 * @param installDir
	 */
	private void prepareForCERollback(String rollbackDir, String installDir) {
		// If the directory ce exists in the rollback directory
		// parse it for files that match com.ibm.tdi.??*&.jar
		// If these exist, match in the directory we're restoring to, delete the
		// file in that directory

		String curSubDir = "ce";
		File curDir = new File(rollbackDir + "/" + curSubDir);

		if (!curDir.isDirectory())
			return;

		//shutdownCE();

		// Build matching file list
		boolean foundDirs = true;
		Vector<String> files = new Vector<String>();
		Vector<String> dirs = new Vector<String>();
		while (foundDirs) {
			File[] curFiles = curDir.listFiles();
			for (int i = 0; curFiles != null && i < curFiles.length; i++) {
				if (curFiles[i].isDirectory()) {
					dirs.add(curSubDir + "/" + curFiles[i].getName());
				} else if (curFiles[i]
						.getName()
						.matches(
								"^.*com\\.ibm\\.tdi\\..*\\d+\\.\\d+\\.\\d+\\.\\d\\.jar$")) {
					files.add(curSubDir + "/" + curFiles[i].getName());
				}
			}
			if (dirs.isEmpty())
				foundDirs = false;
			else {
				curSubDir = dirs.remove(0);
				curDir = new File(rollbackDir + "/" + curSubDir);
			}

		}

		// Now go through and if there's anything in the install directory
		// that matches these files, delete them.
		for (int x = 0; x < files.size(); x++) {
			String filename = files.elementAt(x);

			int i = filename.lastIndexOf('/');
			String origDir = filename.substring(0, i);
			String filename2Cmp = filename;
			String actualDirectory = installDir;
			if (i > 0) {
				actualDirectory += "/" + origDir;
				filename2Cmp = filename.substring(i + 1);
			}

			Pattern myPattern = Pattern.compile("^(.*)(\\d+\\.){4}jar$");
			Matcher myMatch = myPattern.matcher(filename2Cmp);
			if (myMatch.find())
				filename2Cmp = myMatch.group(1);

			File f = new File(actualDirectory);
			File[] filesInInstallDir = f.listFiles();
			if (filesInInstallDir != null)
				for (int z = 0; z < filesInInstallDir.length; z++) {
					String fileName = filesInInstallDir[z].getName();
					if (fileName.matches("^.*" + filename2Cmp
							+ "(\\d+\\.){3}\\d+\\.jar$")) {
						FileGarbageCan.add(actualDirectory + "/" + fileName);
					}
				}
		}
	}

	/**
	 * Installs a fix. Used in conjunction with the -update command line
	 * argument.
	 *
	 * @return 0 if no errors occurred during installation of the fix, nonzero
	 *         otherwise
	 */
	private int installFix() {
		int rc = -1;
		UpdateInstallerMsgs.log(UpdateInstallerMsgs
				.getString("READING.REGISTRY"), UpdateInstallerMsgs.DEBUG);
		Registry registry = null;
		try {
			registry = new Registry(installDir);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"REGISTRY.PROCESS.ERROR", installDir));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"REGISTRY.PROCESS.ERROR", installDir),
					UpdateInstallerMsgs.ERROR);
			return rc;
		}

		UpdateInstallerMsgs.log(UpdateInstallerMsgs
				.getString("READING.MANIFEST"), UpdateInstallerMsgs.DEBUG);
		Fix fix = null;
		try {
			fix = new Fix(zipFile);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString("FIX.FILE.ERROR",
					zipFile));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIX.FILE.ERROR", zipFile), UpdateInstallerMsgs.ERROR);
			return rc;
		}

		// We need to see if we're going to continue...
		topBackupDir = backupDir; // used during cleaning
		backupDir += "/" + fix.getfixName();

		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
				"FINAL.BACKUPDIR.NAME", backupDir), UpdateInstallerMsgs.DEBUG);
		File f = new File(backupDir);
		boolean dirOK = true;
		if (!f.exists())
			dirOK = f.mkdirs();

		if (dirOK == false) {
			System.out.println(UpdateInstallerMsgs.getString(
					"BACKUPDIR.CREATE.ERROR", backupDir, zipFile));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"BACKUPDIR.CREATE.ERROR", backupDir, zipFile),
					UpdateInstallerMsgs.ERROR);
			return rc;
		}

		// 1. Check pre-reqs/clobber
		// 2. Install fix
		// a. backup files
		// b. copy new files
		// c. perform whatever
		// 3. Perform clean if necessary
		// 4. update registry

		// 1
		boolean ok2Fix = verifyPrereqs(registry, fix);
		if (!ok2Fix) {
			// Already logged
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("FIX.ALREADY.INSTALLED"),
					UpdateInstallerMsgs.DEBUG);
			return rc;
		}
		if (clean && registry.getClobber()) {
			System.out.println(UpdateInstallerMsgs
					.getString("BACKUP.DIRS.NOT.CLEARED"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("BACKUP.DIRS.NOT.CLEARED"),
					UpdateInstallerMsgs.INFO);
			clean = false;
		}

		System.out.println(UpdateInstallerMsgs.getString("APPLYING.FIX", fix
				.getfixName(), backupDir));
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("APPLYING.FIX",
				fix.getfixName(), backupDir), UpdateInstallerMsgs.INFO);
		// 2
		Unzip.setDefaultExtractionLocation(topBackupDir);

		Object[] components = fix.getComponents();

		// Indicates if any of the component is updated.
		boolean isCompUpdated = false;

		for (int x = 0; x < components.length; x++) {

			// If any error occurs while updating the fix then return Non-zero value.
			int compUpdateError = 0;

			if (components[x].equals(FixUtils.BASE)) {
				if (registry.isInstalled(FixUtils.BASE)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixBase();
				}
			} else if (components[x].equals(FixUtils.SERVER)) {
				if (registry.isInstalled(FixUtils.SERVER)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixServer(registry, fix);
				}

			} else if (components[x].equals(FixUtils.CE)) {
				if (registry.isInstalled(FixUtils.CE)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixCE();
				}

			} else if (components[x].equals(FixUtils.CE_UPDATE)) {
				if (registry.isInstalled(FixUtils.CE_UPDATE)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixCE_Update();
				}

			} else if (components[x].equals(FixUtils.JDOCS)) {
				if (registry.isInstalled(FixUtils.JDOCS)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixJavaDocs();
				}
			} else if (components[x].equals(FixUtils.EXAMPLES)) {
				if (registry.isInstalled(FixUtils.EXAMPLES)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixExamples(registry);
				}

			} else if (components[x].equals(FixUtils.IEHS)) {
				if (registry.isInstalled(FixUtils.IEHS)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixIEHS();
				}
			} else if (components[x].equals(FixUtils.EWP)) {
//				if (registry.isInstalled(FixUtils.EWP)) {
//					isCompUpdated = true;
//					System.out.println(UpdateInstallerMsgs.getString(
//							"UPDATING.COMPONENT", components[x]));
//					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
//							"UPDATING.COMPONENT", components[x]),
//							UpdateInstallerMsgs.INFO);
//					Unzip.setDefaultExtractionLocation(".");
//
//					compUpdateError = fixEWP(registry, fix);
//					Unzip.setDefaultExtractionLocation(topBackupDir);
//				}
			} else if (components[x].equals(FixUtils.AMC)) {
//				if (registry.isInstalled(FixUtils.AMC)) {
//					isCompUpdated = true;
//					System.out.println(UpdateInstallerMsgs.getString(
//							"UPDATING.COMPONENT", components[x]));
//					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
//							"UPDATING.COMPONENT", components[x]),
//							UpdateInstallerMsgs.INFO);
//					compUpdateError = fixAMC(registry, fix);
//				}
			} else if (components[x].equals(FixUtils.PLUGINS)) {
				if (registry.isInstalled(FixUtils.PLUGINS)) {
					isCompUpdated = true;
					System.out.println(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"UPDATING.COMPONENT", components[x]),
							UpdateInstallerMsgs.INFO);
					compUpdateError = fixPlugins();
				}
			} else {
				System.out.println(UpdateInstallerMsgs.getString(
						"UNRECOGNIZED.COMPONENT", components[x]));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"UNRECOGNIZED.COMPONENT", components[x]),
						UpdateInstallerMsgs.ERROR);
				compUpdateError = -1;
			}
			// If any Error occurs while updating fix then Break the FOR Loop
			// Return 0 : Fix applied successfully
			if(compUpdateError != 0){
				break;
			}
		} // end of for

	
			//Apply .fxtag file if any component's fix is getting applied.
			if (isCompUpdated)
				applyTagFile();

			// 3
			if (clean) {
				// Delete backup directories
				if (os.startsWith(FixUtils.WINDOWS)) {
					FixUtils.executeCommand(new String[] { "cmd", "/c", QUOTE+"rmdir",
							"/S", "/Q", QUOTE+topBackupDir+QUOTE+QUOTE });

				} else {
					String shell = "/bin/sh";
					if (os.startsWith(FixUtils.I5OS))
						shell = "/bin/qsh";
					FixUtils.executeCommand(new String[] { shell, "-c", "rm",
							"-rf", topBackupDir });
				}
			} else {
				Unzip.writeNewFiles(backupDir);
			}
			// 4
			registry.update(fix, clean);
	
		return rc;
	}

	/**
	 * Applies a fix to the BASE component of TDI.
	 */
	private int fixBase() {
		if (Unzip.isFileInArchive("TDI_Base.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_Base.zip", zipFile, false);

		   if (System.getenv("UPDATE_UI").equalsIgnoreCase("255")){
			   String[] filestoExclude = new String[] {"/maintenance/UpdateInstaller.jar"};
			   unzipError = Unzip.unzipToDirectory("TDI_Base.zip", installDir, backupDir, filestoExclude);
			   // Error occured while Unzip the Directory or File
			   if(unzipError==-1)
				   return -1;

		   }
//		   if (os.startsWith(FixUtils.WINDOWS)) {
//				ASCIIFileUpdate.simpleModify(new String[] {
//						installDir + "\\etc\\updateinstaller-log4j.properties",
//						"$change$", installDir, "TEXT" });
//				ASCIIFileUpdate.simpleModify(new String[] {
//						installDir + "\\etc\\updateinstaller-log4j.properties",
//						"\\", "/", "TEXT" });
//			} else
//				ASCIIFileUpdate.simpleModify(new String[] {
//						installDir + "/etc/updateinstaller-log4j.properties",
//						"$change$", installDir, "TEXT" });
		}

		if (os.startsWith(FixUtils.WINDOWS)) {
			if (Unzip.isFileInArchive("TDI_BaseWin.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_BaseWin.zip", zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_BaseWin.zip", installDir,
								 backupDir);
			}
		} else {
			if (os.equals(FixUtils.I5OS)) {
				if (Unzip.isFileInArchive("TDI_BaseI5OS.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_BaseI5OS.zip", zipFile,
							false);
					unzipError = Unzip.unzipToDirectory("TDI_BaseI5OS.zip", installDir,
							backupDir);
				}
			} else {
				if (Unzip.isFileInArchive("TDI_BaseUNIX.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_BaseUNIX.zip", zipFile,
							false);
					unzipError = Unzip.unzipToDirectory("TDI_BaseUNIX.zip", installDir,
							backupDir);
				}
			}
		}

		// License component...
		if (Unzip.isFileInArchive("TDI_License.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_License.zip", zipFile, false);
			// We don't want to rollback a license, so pass in null for the
			// backup directory
			unzipError=Unzip.unzipToDirectory("TDI_License.zip", installDir, null);

		}

		// JRE...
		if (Unzip.isFileInArchive(FixUtils.getOS() + "/JRE.zip", zipFile)) {

			Unzip.unzipSingleBinaryFile(FixUtils.getOS() + "/JRE.zip", zipFile,
					false, "JRE.zip");
			unzipError=Unzip.unzipToDirectory("JRE.zip", installDir, backupDir);

		}
		if(unzipError==-1)
		   return -1;

		return 0;
	}

	/**
	 * Applies fix pack tag file of TDI.
	 *
	 */
	private void applyTagFile()
	{
		//Tag File...
			if (Unzip.isFileInArchive("TDI_tags_update.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_tags_update.zip", zipFile,
						false);
				Unzip.unzipToDirectory("TDI_tags_update.zip", installDir,
						backupDir);
			}
	}

	/**
	 * Applies a fix to the SERVER component of TDI.
	 *
	 * @param registry
	 *            Object representing the current TDI installation
	 */
	private int fixServer(Registry registry, Fix fix) {

		boolean migrateGlobalProps = false;
		boolean modify_needed = false;

		if (Unzip.isFileInArchive("TDI_Server.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_Server.zip", zipFile, false);
			unzipError = Unzip.unzipToDirectory("TDI_Server.zip", installDir, backupDir);
			if(unzipError==-1)
			   return -1;
		}

		if (registry.getEdition().equals(FixUtils.IDENTITY)) {
			if (Unzip.isFileInArchive("TDI_ServerIdentity.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_ServerIdentity.zip", zipFile,
						false);
				unzipError = Unzip.unzipToDirectory("TDI_ServerIdentity.zip", installDir,
						backupDir);
				if(unzipError==-1)
				   return -1;
			}
		}

		deleteFiles(fix.getDeleteFiles());
		if (os.startsWith(FixUtils.WINDOWS)) {
			if (Unzip.isFileInArchive("TDI_ServerWin.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_ServerWin.zip", zipFile,
								false);

				if (Unzip.isFileInArchive("etc/global.properties", Unzip
						.getDefaultExtractionLocation()
						+ "/" + "TDI_ServerWin.zip")) {
					migrateGlobalProps = true;
				}

				unzipError = Unzip.unzipToDirectory("TDI_ServerWin.zip", installDir,
						backupDir);
				if(unzipError == -1)
					return -1;

				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "\\win32_service\\ibmdiservice.props",
						"$change$", installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "\\win32_service\\ibmdiservice.props",
						"$jvmRoot$", installDir + "\\jvm", "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "\\etc\\global.properties", "$change$",
						installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "\\etc\\tdisrvctl-log4j.properties",
						"$change$", installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "\\etc\\tdisrvctl-log4j.properties", "\\",
						"/", "TEXT" });
			}

			// Unzip LUM file if present
			if (os.equals(FixUtils.WINDOWS)) {
				if (Unzip.isFileInArchive("TDI_ServerWinX86.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_ServerWinX86.zip",
							zipFile, false);
					unzipError = Unzip.unzipToDirectory("TDI_ServerWinX86.zip", installDir,
							backupDir);
				}
			}

			if (os.equals(FixUtils.WINDOWS64)) {
				if (Unzip.isFileInArchive("TDI_ServerWinX86_64.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_ServerWinX86_64.zip",
							zipFile, false);
					unzipError = Unzip.unzipToDirectory("TDI_ServerWinX86_64.zip",
							installDir, backupDir);
				}
			}
		} else {
			if (os.equals(FixUtils.I5OS)) {
				if (Unzip.isFileInArchive("TDI_ServerI5OS.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_ServerI5OS.zip", zipFile,
							false);

					if (Unzip.isFileInArchive("etc/global.properties", Unzip
							.getDefaultExtractionLocation()
							+ "/" + "TDI_ServerI5OS.zip")) {
						migrateGlobalProps = true;
						modify_needed=true;
					}

					if (Unzip.isFileInArchive("etc/tdisrvctl-log4j.properties", Unzip
							.getDefaultExtractionLocation()
							+ "/" + "TDI_ServerI5OS.zip"))
						modify_needed = true;

					unzipError = Unzip.unzipToDirectory("TDI_ServerI5OS.zip", installDir,
							backupDir);
				}
			} else {
				if (Unzip.isFileInArchive("TDI_ServerUNIX.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_ServerUNIX.zip", zipFile,
							false);

					if (Unzip.isFileInArchive("etc/global.properties", Unzip
							.getDefaultExtractionLocation()
							+ "/" + "TDI_ServerUNIX.zip")) {
						migrateGlobalProps = true;
						modify_needed = true;
					}

					if (Unzip.isFileInArchive("etc/tdisrvctl-log4j.properties", Unzip
							.getDefaultExtractionLocation()
							+ "/" + "TDI_ServerUNIX.zip"))
						modify_needed = true;

					unzipError = Unzip.unzipToDirectory("TDI_ServerUNIX.zip", installDir,
							backupDir);
				}

				// Unzip LUM file if present
				if (os.equals(FixUtils.LINUX)) {// 32 bit only
					if (Unzip
							.isFileInArchive("TDI_ServerLinuxX86.zip", zipFile)) {
						Unzip.unzipSingleBinaryFile("TDI_ServerLinuxX86.zip",
								zipFile, false);
						unzipError = Unzip.unzipToDirectory("TDI_ServerLinuxX86.zip",
								installDir, backupDir);
					}
				}

					if (Unzip.isFileInArchive("TDI_ServerAix_x86.zip", zipFile)) {
									if (os.equals(FixUtils.AIX)) {// 32 bit only
										Unzip.unzipSingleBinaryFile("TDI_ServerAix_x86.zip",
												zipFile, false);
										unzipError = Unzip.unzipToDirectory(
												"TDI_ServerAix_x86.zip", installDir, backupDir);
									}
								}
							}

			// Make necessary file modifications
			if (modify_needed) {
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "/etc/global.properties", "$change$",
						installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "/etc/tdisrvctl-log4j.properties",
						"$change$", installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						installDir + "/etc/tdisrvctl-log4j.properties", "\\",
						"/", "TEXT" });
			}
		}

		// Migrate global.properties if it was in the fixpack...this is common to windows and Unix...
		if (migrateGlobalProps)
			if (os.startsWith(FixUtils.WINDOWS)) {
				cmdExecuteError = FixUtils.executeCommand(new String[] { "cmd", "/c",
						QUOTE+QUOTE+installDir + "\\bin\\tdimiggbl.bat"+QUOTE, "-f",
						QUOTE+backupDir + "\\etc\\global.properties"+QUOTE, "-m", "-n",
						QUOTE+installDir + "\\etc\\global.properties"+QUOTE+QUOTE });
			} else {
				cmdExecuteError = FixUtils.executeCommand(new String[] {
						installDir + "/bin/tdimiggbl", "-f",
						backupDir + "/etc/global.properties", "-m", "-n",
						installDir + "/etc/global.properties" });
			}
		   if(unzipError==-1)
			   return -1;
		   if(cmdExecuteError != 0)
			   return -1;
		   return 0;
	}

	private void deleteFiles(List<String> deleteFiles) {
		if (deleteFiles == null || deleteFiles.size() == 0)
			return;
		
		String backup = backupDir;
		
		if (backup != null) {
			backup = backup.replace('\\', '/');
			if (!backup.endsWith("/"))
				backup += "/";
		}
		
		for (String name: deleteFiles) {
			File f = new File(installDir, name);
			if (f.exists()) {
				if (backup == null) {
					f.delete();
				} else {
					if (name.indexOf('/') > 0) {
						File toDir = new File(backup, name.substring(0, name.lastIndexOf('/')));
						if (!toDir.exists())
							toDir.mkdirs();
					}
					File target = new File(backup, name);
					if (target.exists())
						target.delete();
					f.renameTo(target);
				}
			}
		}
	}

	/**
	 * Applies a fix to the CE component of TDI.
	 */
	private int fixCE() {
		Unzip.setUseCEAlg(true);

		String SLASH="/";
		if (os.startsWith(FixUtils.WINDOWS))
			SLASH="\\";
        String ce_installDir=installDir+SLASH+"ce"; //Some ce zips need to be extracted one level deeper
        String ce_backupDir=backupDir+SLASH+"ce";

		// We need to make sure CE has been shutdown before performing
		// maintenance
        //cmdExecuteError = shutdownCE();
        //if(cmdExecuteError != 0)
        //	return -1;

		if (Unzip.isFileInArchive("TDI_CEBase.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_CEBase.zip", zipFile, false);
			unzipError = Unzip.unzipToDirectory("TDI_CEBase.zip", ce_installDir, ce_backupDir);
			if(unzipError == -1)
				return -1;

		}

		if (os.startsWith(FixUtils.WINDOWS)) {
			if (Unzip.isFileInArchive("TDI_CEWin.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_CEWin.zip", zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_CEWin.zip", installDir, backupDir);
			}
			if (!os.equals(FixUtils.WINDOWS64)) {
				if (Unzip.isFileInArchive("eclipsece-win32.win32.x86.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile(
							"eclipsece-win32.win32.x86.zip", zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-win32.win32.x86.zip",
							ce_installDir, ce_backupDir);
				}
			} else { // 64 bits
				if (Unzip.isFileInArchive("eclipsece-win32.win32.x86_64.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile(
							"eclipsece-win32.win32.x86_64.zip", zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-win32.win32.x86_64.zip",
							ce_installDir, ce_backupDir);
				}
			}
		} else {
			if (Unzip.isFileInArchive("TDI_CEUNIX.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_CEUNIX.zip", zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_CEUNIX.zip", installDir, backupDir);
			}

			if (os.equals(FixUtils.AIX) || os.equals(FixUtils.AIX64)) {
				if (Unzip.isFileInArchive("eclipsece-aix.motif.ppc.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile("eclipsece-aix.motif.ppc.zip",
							zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-aix.motif.ppc.zip",
							ce_installDir, ce_backupDir);
				}
			}

			if (os.equals(FixUtils.SOLARIS)) {
				if (Unzip.isFileInArchive("eclipsece-solaris.gtk.sparc.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile(
							"eclipsece-solaris.gtk.sparc.zip", zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-solaris.gtk.sparc.zip",
							ce_installDir, ce_backupDir);
				}
			}

			if (os.equals(FixUtils.LINUX)) {
				if (Unzip.isFileInArchive("eclipsece-linux.gtk.x86.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile("eclipsece-linux.gtk.x86.zip",
							zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-linux.gtk.x86.zip",
							ce_installDir, ce_backupDir);
				}
			}

			if (os.equals(FixUtils.LINUX_AMD64)) {
				if (Unzip.isFileInArchive("eclipsece-linux.gtk.x86_64.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile(
							"eclipsece-linux.gtk.x86_64.zip", zipFile, false);
					unzipError = Unzip.unzipToDirectory("eclipsece-linux.gtk.x86_64.zip",
							ce_installDir, ce_backupDir);
				}
			}
		}
		Unzip.setUseCEAlg(false);
		if(unzipError == -1)
			return -1;

		return 0;
	}

	/**
	 * Applies a fix to the CE Update component of TDI.
	 */
	private int fixCE_Update() {
		Unzip.setUseCEAlg(true);
		String SLASH="/";
		if (os.startsWith(FixUtils.WINDOWS))
			SLASH="\\";
		String ce_update_installDir=installDir+SLASH+"ce"+SLASH+"update_site"; //ce update zips need to be extracted here
		String ce_update_backupDir=backupDir+SLASH+"ce"+SLASH+"update_site";

		Unzip.unzipSingleBinaryFile("TDI_CEUpdateSite.zip", zipFile, false);
		unzipError = Unzip.unzipToDirectory("TDI_CEUpdateSite.zip", ce_update_installDir, ce_update_backupDir);
		Unzip.setUseCEAlg(false);
		return unzipError;
	}

	/**
	 * Applies a fix to the JavaDocs component of TDI.
	 */
	private int fixJavaDocs() {
		Unzip.unzipSingleBinaryFile("TDI_Docs.zip", zipFile, false);
		unzipError = Unzip.unzipToDirectory("TDI_Docs.zip", installDir, backupDir);
		return unzipError;
	}

	/**
	 * Applies a fix to the Examples component of TDI.
	 *
	 * @param registry
	 *            Object representing the current TDI installation
	 */
	private int fixExamples(Registry registry) {

		if (os.startsWith(FixUtils.WINDOWS)) {

			if (Unzip.isFileInArchive("TDI_Examples.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_Examples.zip", zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_Examples.zip", installDir,
						backupDir);
			}

			if (registry.getEdition().equals(FixUtils.IDENTITY)
					&& Unzip.isFileInArchive("TDI_ExamplesIdentity.zip",
							zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_ExamplesIdentity.zip",
						zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_ExamplesIdentity.zip", installDir,
						backupDir);
			}
		} else {
			if (registry.getEdition().equals(FixUtils.IDENTITY)
					&& Unzip.isFileInArchive("TDI_ExamplesUNIXIdentity.zip",
							zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_ExamplesUNIXIdentity.zip",
						zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_ExamplesUNIXIdentity.zip",
						installDir, backupDir);
			}

			if (Unzip.isFileInArchive("TDI_ExamplesUNIX.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_ExamplesUNIX.zip", zipFile,
						false);
				unzipError = Unzip.unzipToDirectory("TDI_ExamplesUNIX.zip", installDir,
						backupDir);
			}
		}
		if(unzipError == -1)
			return -1;

		return 0;
	}

	/**
	 * Applies a fix to the IEHS component of TDI.
	 */
	private int fixIEHS() {
		if (os.startsWith(FixUtils.WINDOWS)) {
			if (Unzip.isFileInArchive("TDI-Help_Win.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI-Help_Win.zip", zipFile, false,
						"TDI-Help.zip");
				unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir, backupDir);
			}
		} else { // This may need to change
			if (os.equals(FixUtils.I5OS)) {
				if (Unzip.isFileInArchive("TDI-Help_I5OS.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_I5OS.zip", zipFile,
							false, "TDI-Help.zip");
				}
			} else if (os.equals(FixUtils.LINUX)
					|| os.equals(FixUtils.LINUX_AMD64)) {
				if (Unzip.isFileInArchive("TDI-Help_Linux.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_Linux.zip", zipFile,
							false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			} else if (os.equals(FixUtils.ZLINUX)) {
				if (Unzip.isFileInArchive("TDI-Help_zLinux.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_zLinux.zip", zipFile,
							false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			} else if (os.equals(FixUtils.LINUX_PPC)) {
				if (Unzip.isFileInArchive("TDI-Help_ppcLinux.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_ppcLinux.zip",
							zipFile, false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			} else if (os.equals(FixUtils.AIX)) {
				if (Unzip.isFileInArchive("TDI-Help_AIX.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_AIX.zip", zipFile,
							false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			} else if (os.startsWith(FixUtils.SOLARIS)) {
				if (Unzip.isFileInArchive("TDI-Help_Solaris.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_Solaris.zip",
							zipFile, false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			} else if (os.startsWith(FixUtils.HPUX)) {
				if (Unzip.isFileInArchive("TDI-Help_HPUX.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI-Help_HPUX.zip", zipFile,
							false, "TDI-Help.zip");
					unzipError = Unzip.unzipToDirectory("TDI-Help.zip", installDir,
							backupDir);
				}
			}
		}
		if(unzipError == -1)
			return -1;

		return 0;
	}

	/**
	 * Applies a fix to the Embedded Web Platform component of TDI.
	 *
	 * @param registry
	 *            Object representing the current TDI installation
	 * @param fix
	 *            Object representing the current fix being applied
	 * @return 0 if no errors occurred during installation, nonzero otherwise
	 */
	private int fixEWP(Registry registry, Fix fix) {
		int rc = 0;
		String jarName = Unzip.findMatchingFileSpecInZip("^lwiUpdate.*$",
				zipFile);
		if (jarName.equals("")) {
			System.out.println(UpdateInstallerMsgs
					.getString("LWI.JAR.NOT.FOUND"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("LWI.JAR.NOT.FOUND"), UpdateInstallerMsgs.ERROR);
			rc = -1;
			return rc;
		}

		fix.setConfidIDFromJarName(jarName);
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("LWI.JAR.CONFIG",
				jarName, fix.getConfigID()), UpdateInstallerMsgs.DEBUG);
		LWIInstalledComponent lwi_comp = (LWIInstalledComponent) registry
				.getComponent(FixUtils.EWP);
		Unzip.unzipSingleBinaryFile(jarName, zipFile, false);

		String LWIServiceName=lwi_comp.getServiceName();
		if (os.startsWith(FixUtils.WINDOWS)) {
			FileGarbageCan.add(Unzip.getDefaultExtractionLocation()+"\\"+jarName);
			if (LWIServiceName.equals(""))
				rc = FixUtils.executeCommand(new String[] { "cmd", "/c",
						(QUOTE+QUOTE+installDir + "\\bin\\amc\\lwiUpdate.bat"+QUOTE),
						"-update",
						QUOTE+Unzip.getDefaultExtractionLocation()+"\\"+jarName+QUOTE+QUOTE});
			else
				rc = FixUtils.executeCommand(new String[] { "cmd", "/c",
						QUOTE+QUOTE+installDir + "\\bin\\amc\\lwiUpdate.bat"+QUOTE,
						"-update",
						QUOTE+Unzip.getDefaultExtractionLocation()+"\\"+jarName+QUOTE,
						lwi_comp.getServiceName()+QUOTE });
		} else if (!os.equals(FixUtils.I5OS)) {
			FileGarbageCan.add(Unzip.getDefaultExtractionLocation()+"/"+jarName);
			if (LWIServiceName.equals(""))
				rc = FixUtils.executeCommand(new String[] {
						installDir + "/bin/amc/lwiUpdate.sh", "-update",
						Unzip.getDefaultExtractionLocation()+"/"+jarName});
			else
				rc = FixUtils.executeCommand(new String[] {
						installDir + "/bin/amc/lwiUpdate.sh", "-update",
						Unzip.getDefaultExtractionLocation()+"/"+jarName,
						lwi_comp.getServiceName() });
		}
		else
			FileGarbageCan.add(Unzip.getDefaultExtractionLocation()+"/"+jarName);
		return rc;
	}

	/**
	 * Stops the network servers when applying maintenance to AM
	 *
	 * @param serviceName
	 *            The AMC service name or the empty string if none exists
	 */
	private int stopAMServers(String serviceName) {
		int rc=0;
		if (os.startsWith(FixUtils.WINDOWS)) {
			if (serviceName.equals(""))
				rc = FixUtils.executeCommand(new String[] { "cmd", "/c",
						QUOTE+installDir + "\\bin\\amc\\stopAM.bat"+QUOTE });
			else
				rc = FixUtils.executeCommand(new String[] { "cmd", "/c",
						QUOTE+QUOTE+installDir + "\\bin\\amc\\stopAM.bat"+QUOTE, serviceName+QUOTE });

			rc = FixUtils.executeCommand(new String[] { "cmd", "/c",
					QUOTE+installDir + "\\bin\\amc\\stopNetworkServer.bat"+QUOTE });
		} else {
			rc = FixUtils.executeCommand(new String[] { installDir
					+ "/bin/amc/stopAM.sh" });
			rc = FixUtils.executeCommand(new String[] { installDir
					+ "/bin/amc/stopNetworkServer.sh" });
		}
		return rc;
	}

	/**
	 * Applies a fix to the AMC component of TDI.
	 *
	 * @param registry
	 *            Object representing the current TDI installation
	 * @param fix
	 *            Object representing the current fix being applied
	 */
	private int fixAMC(Registry registry, Fix fix) {
		boolean updateWarWab = false;
		//boolean containsAMJars = false; Defect 13042

		// Get the AMC service name
		String lwiServiceName = getAMCServiceName(registry);

		if (Unzip.isFileInArchive("TDI_AMC.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_AMC.zip", zipFile, false);

			if (registry.isInstalled(FixUtils.AMC))
				updateWarWab = !((AMCInstalledComponent) registry
						.getComponent(FixUtils.AMC)).getDeferredDeployment();
			if (updateWarWab)
				updateWarWab = Unzip.isFileInArchive("amc/tdiamc.war", Unzip
						.getDefaultExtractionLocation()
						+ "/" + "TDI_AMC.zip");

			// If there are AM jars in this fix, call stopAM.bat/sh and
			// stopNetworkServer.bat/sh
			//Defect 13042
			String amJars = Unzip.findMatchingFileSpecInZip("^bin/amc/ActionManager/jars/.*$", Unzip.getDefaultExtractionLocation()+"/"+"TDI_AMC.zip");
			if (!amJars.equals(""))
				stopAMServers(lwiServiceName);

			unzipError = Unzip.unzipToDirectory("TDI_AMC.zip", installDir, backupDir);
		}
		if(unzipError == -1)
			return -1;
		if(cmdExecuteError != 0)
			return -1;
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
				"CALL.AMC.MIGRATE", updateWarWab), UpdateInstallerMsgs.DEBUG);

		if (os.startsWith(FixUtils.WINDOWS)) {
			if (Unzip.isFileInArchive("TDI_AMCWin.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_AMCWin.zip", zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_AMCWin.zip", installDir, backupDir);
			}
			if (updateWarWab) {
				if (lwiServiceName.equals(""))
					cmdExecuteError = FixUtils.executeCommand(new String[] {
							"cmd",
							"/c",
							QUOTE+QUOTE+installDir + "\\bin\\amc\\migrateamc.bat"+QUOTE,
							"-d",
							QUOTE+backupDir + "\\..\\backup_tdiamc_"
									+ fix.getfixName()+QUOTE+QUOTE });
				else
					cmdExecuteError = FixUtils.executeCommand(new String[] {
							"cmd",
							"/c",
							QUOTE+QUOTE+installDir + "\\bin\\amc\\migrateamc.bat"+QUOTE,
							"-d",
							QUOTE+backupDir + "\\..\\backup_tdiamc_"
									+ fix.getfixName()+QUOTE, "-s", lwiServiceName+QUOTE });
			}
		} else {
			if (os.equals(FixUtils.I5OS)) {
				if (Unzip.isFileInArchive("TDI_AMCI5OS.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_AMCI5OS.zip", zipFile,
							false);
					unzipError = Unzip.unzipToDirectory("TDI_AMCI5OS.zip", installDir,
							backupDir);
				}
			} else {
				if (Unzip.isFileInArchive("TDI_AMCUnix.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_AMCUnix.zip", zipFile,
							false);
					unzipError = Unzip.unzipToDirectory("TDI_AMCUnix.zip", installDir,
							backupDir);
				}
			}

			if (updateWarWab)
				cmdExecuteError = FixUtils.executeCommand(new String[] {
						installDir + "/bin/amc/migrateamc.sh", "-d",
						backupDir + "/../backup_tdiamc_" + fix.getfixName() });
		}
		if(cmdExecuteError != 0)
			return -1;
		if(unzipError == -1)
			return -1;

		return 0;
	}

	/**
	 * Get the AMC service name
	 *
	 * @param registry
	 *            Existing registry
	 * @return the AMC service name or the empty string if there is none.
	 */
	public String getAMCServiceName(Registry registry) {
		// Get the AMC service name
		String lwiServiceName = "";
		if (registry.isInstalled(FixUtils.EWP)) {
			LWIInstalledComponent lwi_comp = ((LWIInstalledComponent) registry
					.getComponent(FixUtils.EWP));
			if (lwi_comp != null)
				lwiServiceName = lwi_comp.getServiceName();
		}
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("LWI.SVC.NAME",
				lwiServiceName), UpdateInstallerMsgs.DEBUG);
		return lwiServiceName;
	}

	/**
	 * Applies a fix to the Plugins component of TDI.
	 */
	private int fixPlugins() {
		if (Unzip.isFileInArchive("TDI_Plugins_Base.zip", zipFile)) {
			Unzip.unzipSingleBinaryFile("TDI_Plugins_Base.zip", zipFile, false);
			unzipError = Unzip.unzipToDirectory("TDI_Plugins_Base.zip", installDir,
							backupDir);

			if(unzipError == -1)
				return -1;
		}

		boolean need2replace = false;

		if (os.startsWith(FixUtils.WINDOWS)) {

			if (Unzip.isFileInArchive("TDI_Plugins_BaseWin.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_Plugins_BaseWin.zip", zipFile,
						false);
				unzipError = Unzip.unzipToDirectory("TDI_Plugins_BaseWin.zip", installDir,
						backupDir);
				need2replace = true;
			}

			if (os.equals(FixUtils.WINDOWS64)) {
				if (Unzip
						.isFileInArchive("TDI_Plugins_win_x86_64.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_Plugins_win_x86_64.zip",
							zipFile, false, "TDI_Plugins_win.zip");
					unzipError = Unzip.unzipToDirectory("TDI_Plugins_win.zip", installDir,
							backupDir);
				}
			} else {
				if (Unzip.isFileInArchive("TDI_Plugins_win_x86.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_Plugins_win_x86.zip",
							zipFile, false, "TDI_Plugins_win.zip");
					unzipError = Unzip.unzipToDirectory("TDI_Plugins_win.zip", installDir,
							backupDir);
				}
			}
		} else {
			if (Unzip.isFileInArchive("TDI_Plugins_BaseUNIX.zip", zipFile)) {
				Unzip.unzipSingleBinaryFile("TDI_Plugins_BaseUNIX.zip",
						zipFile, false);
				unzipError = Unzip.unzipToDirectory("TDI_Plugins_BaseUNIX.zip", installDir,
						backupDir);
				need2replace = true;
			}

			if (os.equals(FixUtils.LINUX)) {
				if (Unzip.isFileInArchive("TDI_Plugins_linux_x86.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_Plugins_linux_x86.zip",
							zipFile, false, "TDI_Plugins_unix.zip");
					unzipError = Unzip.unzipToDirectory("TDI_Plugins_unix.zip", installDir,
							backupDir);
				}
			} else if (os.equals(FixUtils.LINUX_AMD64)) {
				if (Unzip.isFileInArchive("TDI_Plugins_linux_x86_64.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_Plugins_linux_x86_64.zip",
							zipFile, false, "TDI_Plugins_unix.zip");
					unzipError =  Unzip.unzipToDirectory("TDI_Plugins_unix.zip", installDir,
							backupDir);
				}
			} else if (os.equals(FixUtils.AIX)) {
				if (Unzip.isFileInArchive("TDI_Plugins_aix_ppc.zip", zipFile)) {
							Unzip.unzipSingleBinaryFile("TDI_Plugins_aix_ppc.zip",
							zipFile, false, "TDI_Plugins_unix.zip");
							unzipError = Unzip.unzipToDirectory("TDI_Plugins_unix.zip", installDir,
							backupDir);
							}
				} else if (os.equals(FixUtils.AIX64)) {
							if (Unzip.isFileInArchive("TDI_Plugins_aix_ppc_64.zip", zipFile)) {
								Unzip.unzipSingleBinaryFile("TDI_Plugins_aix_ppc_64.zip",
										zipFile, false, "TDI_Plugins_unix.zip");
								unzipError = Unzip.unzipToDirectory("TDI_Plugins_unix.zip",
										installDir, backupDir);
				}
			} else if (os.startsWith(FixUtils.SOLARIS)) {
				if (Unzip.isFileInArchive("TDI_Plugins_solaris_sparc.zip",
						zipFile)) {
					Unzip.unzipSingleBinaryFile(
							"TDI_Plugins_solaris_sparc.zip", zipFile, false,
							"TDI_Plugins_unix.zip");
					unzipError = Unzip.unzipToDirectory("TDI_Plugins_unix.zip", installDir,
							backupDir);
				}
			} else if (os.startsWith(FixUtils.HPUX)) {
				if (Unzip.isFileInArchive("TDI_Plugins_hp_parisc.zip", zipFile)) {
					Unzip.unzipSingleBinaryFile("TDI_Plugins_hp_parisc.zip",
							zipFile, false, "TDI_Plugins_unix.zip");
					unzipError = Unzip.unzipToDirectory("TDI_Plugins_unix.zip", installDir,
							backupDir);
				}
			}
		}

		if (need2replace) {
			String pluginDir = installDir + "/pwd_plugins";
			String replacement_installDir = installDir;
			if (os.startsWith(FixUtils.WINDOWS)) {
				replacement_installDir = replacement_installDir.replaceAll(
						"\\\\", "\\\\\\\\");
				ASCIIFileUpdate.simpleModify(new String[] {
						pluginDir + "/windows/pwsync.props", "$change$",
						replacement_installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						pluginDir + "/windows/registerpwsync.reg", "$change$",
						replacement_installDir, "TEXT" });
				ASCIIFileUpdate.simpleModify(new String[] {
						pluginDir + "/windows/unregisterpwsync.reg",
						"$change$", replacement_installDir, "TEXT" });
			} else
				// the one in pam isn't on Windows...
				ASCIIFileUpdate.simpleModify(new String[] {
						pluginDir + "/pam/pwsync.props", "$change$",
						replacement_installDir, "TEXT" });

			ASCIIFileUpdate.simpleModify(new String[] {
					pluginDir + "/domino/pwsync.props", "$change$",
					replacement_installDir, "TEXT" });
			ASCIIFileUpdate.simpleModify(new String[] {
					pluginDir + "/sun/pwsync.props", "$change$",
					replacement_installDir, "TEXT" });
			ASCIIFileUpdate.simpleModify(new String[] {
					pluginDir + "/tds/pwsync.props", "$change$",
					replacement_installDir, "TEXT" });

		}

		if(unzipError == -1)
			return -1;

		return 0;
	}

	/**
	 * Verifies that the prereqs as specified in the TDI fix file have been met
	 * by this installation.
	 *
	 * @param reg
	 *            Object representing the current TDI installation
	 * @param fix
	 *            Object representing the current fix being applied
	 * @return true if the prereqs have been met, false otherwise
	 */
	private boolean verifyPrereqs(Registry reg, Fix fix) {
		Object[] prereqs = fix.getPreReqs();
		Object[] fixes = reg.getFixes();

		// Check general prereqs...
		if (prereqs.length > 0) {
			for (int i = 0; i < prereqs.length; i++) {
				boolean found = false;
				for (int x = 0; x < fixes.length; x++) {
					if ((Registry.getFixName((String)fixes[i]))
							.equalsIgnoreCase((String) prereqs[i])) {
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println(UpdateInstallerMsgs.getString(
							"MISSING.PREREQ", prereqs[i]));
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"MISSING.PREREQ", prereqs[i]),
							UpdateInstallerMsgs.ERROR);
					return false;
				}
			}
		}

		// check min/max
		boolean minOK = compareVersions(reg.getLevel(), fix.getMinLevel(), ">=");
		boolean maxOK = compareVersions(reg.getLevel(), fix.getMaxLevel(), "<=");
		if (!minOK || !maxOK) {
			System.out.println(UpdateInstallerMsgs.getString(
					"LEVEL.RANGE.MISMATCH", reg.getLevel(), fix.getMinLevel(),
					fix.getMaxLevel()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"LEVEL.RANGE.MISMATCH", reg.getLevel(), fix.getMinLevel(),
					fix.getMaxLevel()), UpdateInstallerMsgs.ERROR);
			return false;
		}

		// Check for clobber
		boolean clobber = false;
		for (int i = 0; i < fixes.length; i++) {
			if ((Registry.getFixName((String)fixes[i])).equalsIgnoreCase(fix.getfixName())) {
				clobber = true;
				break;
			}
		}

		// If we are clobbering, we will remove this component from the list...
		// we will only consider it a full clobber if we are left with nothing
		// to install.
		if (clobber) {
			reg.setClobber(true);
			fix.removeClobberedComponents(reg.getInstalledComponents());
			if (fix.getComponents().length == 0) {
				System.out.println(UpdateInstallerMsgs
						.getString("FIX.ALREADY.APPLIED"));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs
						.getString("FIX.ALREADY.APPLIED"),
						UpdateInstallerMsgs.INFO);
				return false;
			}
		}

		return true;
	}

	/**
	 * Prints a usage statement to standard out.
	 */
	private void usage() {
		System.out.println(UpdateInstallerMsgs
				.getString("UPDATEINSTALLER.USAGE"));
	}

	/**
	 * Parses the arguments passed in via the command line and populates the
	 * necessary internal variables required to perform the current task.
	 *
	 * @param args
	 *            Command line arguments
	 * @return 0 commands are valid, nonzero otherwise
	 */
	private int parseArgsAndSetup(String[] args) {
		int rc = -1;
		if (args.length < 3)
			return rc;

		for (int i = 0; i < args.length; i++)
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("ARG.NO",
					Integer.toString(i + 1), args[i]),
					UpdateInstallerMsgs.DEBUG);

		if (args[2].trim().equals("-?"))
			return rc; // Just looking for usage info

		installDir = ".."; // Should be in a dir underneath the install dir
		zipFile = "";
		clean = false;
		silent = false;

		if (args[2].equalsIgnoreCase("-rollback"))
			fixtype = FIXTYPE.ROLLBACK;
		else if (args[2].equalsIgnoreCase("-update"))
			fixtype = FIXTYPE.UPDATE;
		else if (args[2].equalsIgnoreCase("-queryfix"))
			fixtype = FIXTYPE.QUERYFIX;
		else if (args[2].equalsIgnoreCase("-queryreg"))
			fixtype = FIXTYPE.QUERYREG;
		else if (args[2].equalsIgnoreCase("-enroll"))
			fixtype = FIXTYPE.ENROLL;
		else
			return rc;

		installDir = args[0];
		backupDir = args[1];

		if (fixtype == FIXTYPE.ROLLBACK || fixtype == FIXTYPE.QUERYREG) {
			if (args.length == 3)
				rc = 0;
		} else if (fixtype == FIXTYPE.QUERYFIX || fixtype == FIXTYPE.ENROLL) {
			if (args.length == 4) {
				zipFile = args[3];
				rc = 0;
			}
		} else {

			// -update fix_file.zip [-clean [-silent]]

			switch (args.length) {
			case 0:
			case 1:
			case 2:
			case 3:
				break;

			case 4:
				zipFile = args[3];
				rc = 0;
				break;

			case 5:
				if (args[4].equalsIgnoreCase("-clean")) {
					clean = true;
					zipFile = args[3];
					rc = 0;
				}
				break;

			case 6:
				if (args[4].equalsIgnoreCase("-clean")
						&& args[5].equalsIgnoreCase("-silent")) {
					clean = true;
					silent = true;
					zipFile = args[3];
					rc = 0;
				}
				break;

			default:
				break;
			}
		}

		if (rc != 0)
			return rc;

		os = FixUtils.getOS();

		zipFile = zipFile.replace('"', ' ').trim();
		if (clean && !zipFile.contains(FixUtils.FIXPACK_INDICATOR)) {
			System.out.println(UpdateInstallerMsgs
					.getString("CLEAN.ONLY.DURING.FIXPACK"));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs
					.getString("CLEAN.ONLY.DURING.FIXPACK"),
					UpdateInstallerMsgs.INFO);
			clean = false;
		}

		if (clean && !silent) {
			System.out.println(UpdateInstallerMsgs.getString("CLEAN.CONFIRM"));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String resp = "N";
			try {
				resp = in.readLine();
				in.close();
			} catch (Exception e) {
			}

			if (resp != null && !resp.equalsIgnoreCase("y")) {
				rc = -2;
			}
		}

		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("PARAM.INFO",
				fixtype, backupDir, installDir, zipFile, os),
				UpdateInstallerMsgs.DEBUG);
		return rc;
	}

	/**
	 * Compares two versions of an installation
	 *
	 * @param v1
	 *            First version to be compared
	 * @param v2
	 *            Second version to be compared
	 * @param op
	 *            Comparison operation; one of =, >, <, >=, <=, !=
	 * @return true if the comparison (v1 op v2) is true, false otherwise
	 */
	private boolean compareVersions(String v1, String v2, String op) {
		// It is assumed that both versions are 'dot' separated
		String[] ver1_temp = v1.split("\\.");
		String[] ver2_temp = v2.split("\\.");
		int max = (ver2_temp.length > ver1_temp.length ? ver2_temp.length
				: ver1_temp.length);
		String[] ver1 = new String[max];
		String[] ver2 = new String[max];
		for (int i = 0; i < max; i++) {
			ver1[i] = "0";
			ver2[i] = "0";
		}
		for (int i = 0; i < ver1_temp.length; i++)
			ver1[i] = ver1_temp[i];
		for (int i = 0; i < ver2_temp.length; i++)
			ver2[i] = ver2_temp[i];

		boolean match = true;
		boolean gt_lt_flag = false;
		for (int i = 0; i < ver1.length; i++) {
			int one = Integer.parseInt(ver1[i]);
			int two = Integer.parseInt(ver2[i]);
			if (op.equals("=") && one == two)
				continue;
			else if (op.equals(">")
					&& (one > two || (one == two && i != ver1.length - 1))) {
				if (one > two) {
					gt_lt_flag = true;
					break;
				}
				continue;
			} else if (op.equals(">=") && one >= two) {
				if (one > two) {
					gt_lt_flag = true;
					break;
				}
				continue;
			} else if (op.equals("<")
					&& (one < two || (one == two && i != ver1.length - 1))) {
				if (one < two)
					break;
				continue;
			} else if (op.equals("<=") && one <= two) {
				if (one < two)
					break;
				continue;
			} else if (op.equals("!=") && one != two)
				continue;
			else {
				match = false;
				break;
			}
		}
		if (match == true) {
			if (op.equals(">") && (gt_lt_flag == false))
				match = false;
			else if (op.equals("<") && (gt_lt_flag == false))
				match = false;
		}
		return match;
	}

	/**
	 * Get the CE Workspace in order to call ibmditk -tdishutdown
	 *
	 * @return the CE's workspace path
	 */
//	private String getCEWorkspace() {
//		String CEWorkspace = "XXX";
//
//		String prefs_file = installDir
//				+ "/ce/eclipsece/configuration/.settings/org.eclipse.ui.ide.prefs";
//		String matchLine = "RECENT_WORKSPACES=";
//
//		File prefs = new File(prefs_file);
//		if (!prefs.exists()) {
//			return CEWorkspace;
//		}
//
//		FileInputStream infile = null;
//		try {
//			infile = new FileInputStream(prefs_file);
//			BufferedReader in = new BufferedReader(
//					new InputStreamReader(infile));
//			String temp = in.readLine();
//			while (temp != null) {
//				if (temp.startsWith(matchLine)) {
//					String line = temp;
//					line = line.substring(matchLine.length());
//					if (line.indexOf('\n') != -1)
//						line = line.substring(0, line.indexOf('\n'));
//					else {
//						int pos=line.indexOf("\\n");
//						if (pos != -1 && (!os.startsWith(FixUtils.WINDOWS) ||
//								          (os.startsWith(FixUtils.WINDOWS) && line.charAt(pos+4)==':')))
//							line = line.substring(0, line.indexOf("\\n"));
//					}
//					line = line.trim();
//					if (os.startsWith(FixUtils.WINDOWS)) {
//						line = line.replace("\\:", ":");
//						line = line.replace("\\\\", "\\");
//						line = "\"" + line + "\"";
//					}
//					CEWorkspace = line;
//					break;
//				}
//				temp = in.readLine();
//			}
//			in.close();
//		} catch (IOException e) {
//			// We will attempt to continue if we can't process the file.
//			System.out.println(UpdateInstallerMsgs
//					.getString("CE.SHUTDOWN.EXCEPTION"));
//			UpdateInstallerMsgs.log(UpdateInstallerMsgs
//					.getString("CE.SHUTDOWN.EXCEPTION"),
//					UpdateInstallerMsgs.INFO);
//			return CEWorkspace;
//		}
//		return CEWorkspace;
//	}
}
