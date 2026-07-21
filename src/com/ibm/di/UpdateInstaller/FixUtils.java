/*
 * Copyright IBM Corp. 2003, 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * A variety of utilities and constants available for use throughout the update
 * installer.
 * 
 * @author Alan Watkins
 * 
 */
public class FixUtils {

	/**
	 * @deprecated No longer contains proprietary license text.
	 */
	@Deprecated
	public static final String OBJECT_CODE = "";
	/**
	 * String representing the BASE TDI component
	 */
	public static final String BASE = "BASE";

	/**
	 * String representing the SERVER TDI component
	 */
	public static final String SERVER = "SERVER";

	/**
	 * String representing the CE TDI component
	 */
	public static final String CE = "CE";

	/**
	 * String representing the CE Update TDI component
	 */
	public static final String CE_UPDATE = "CE UPDATE";

	/**
	 * String representing the JavaDocs TDI component
	 */
	public static final String JDOCS = "JAVADOCS";

	/**
	 * String representing the Examples TDI component
	 */
	public static final String EXAMPLES = "EXAMPLES";

	/**
	 * String representing the IEHS TDI component
	 */
	public static final String IEHS = "IEHS";

	/**
	 * String representing the BASE TDI component
	 */
	public static final String EWP = "EMBEDDED WEB PLATFORM";

	/**
	 * String representing the AMC TDI component
	 */
	public static final String AMC = "AMC";

	/**
	 * String representing the PLUGINS TDI component
	 */
	public static final String PLUGINS = "PLUGINS";

	/**
	 * String representing the FIXES section of the .registry file
	 */
	public static final String FIXES = "FIXES";

	/**
	 * String representing the LEVEL section of the .registry file
	 */
	public static final String LEVEL = "LEVEL";

	/**
	 * String representing the PREREQ section of the .manifest file
	 */
	public static final String PREREQ = "PREREQ";

	/**
	 * String representing the NAME section of the .manifest file
	 */
	public static final String NAME = "NAME";

	/**
	 * String representing the EDITION section of the .registry file
	 */
	public static final String EDITION = "EDITION";

	/**
	 * String representing the LICENSE section of the .registry file
	 */
	public static final String LICENSE = "LICENSE";

	/**
	 * String representing the trial license version of TDI
	 */
	public static final String TRIAL = UpdateInstallerMsgs.getString("LICENSE.TRIAL");

	/**
	 * String representing the full license version of TDI
	 */
	public static final String FULL = UpdateInstallerMsgs.getString("LICENSE.FULL");

	/**
	 * String representing the General Purpose version of TDI
	 */
	public static final String GENERAL = UpdateInstallerMsgs
			.getString("GEN.PURPOSE.ED");

	/**
	 * String representing the Identity version of TDI
	 */
	public static final String IDENTITY = UpdateInstallerMsgs
			.getString("IDENTITY.ED");

	/**
	 * String representing the value "None"
	 */
	public static final String NONE = UpdateInstallerMsgs
			.getString("NONE.LABEL");

	/**
	 * Array of TDI components
	 */
	private static final String[] COMPONENTS = { BASE, SERVER, CE, CE_UPDATE,
			JDOCS, EXAMPLES, IEHS, EWP, AMC, PLUGINS };

	/**
	 * Gets the list of TDI components
	 * 
	 * @return An array representing TDI components
	 */
	public static final String[] getComponents() {
		String[] copyOfComps = new String[COMPONENTS.length];
		System.arraycopy(COMPONENTS, 0, copyOfComps, 0, COMPONENTS.length);
		return copyOfComps;
	}

	// May need more of these...notice how all of the Linuxes start with
	// Linux...so you can use startsWith("Linux") to verify...not sure if zLinux
	// should be included in that scheme or not...
	/**
	 * String representing a Windows operating system
	 */
	public static final String WINDOWS = "Windows";

	/**
	 * String representing the Windows AMD64
	 */
	public static final String WINDOWS64 = "Windows AMD64";

	/**
	 * String representing the I5OS operating system
	 */
	public static final String I5OS = "I5OS";

	/**
	 * String representing the zLinux operating system
	 */
	public static final String ZLINUX = "zLinux";

	/**
	 * String representing the AIX operating system
	 */
	public static final String AIX = "AIX";
	/**
	 * String representing the AIX 64-bit operating system
	 */
	public static final String AIX64 = "AIX ppc64";

	/**
	 * String representing a Solaris operating system
	 */
	public static final String SOLARIS = "Solaris";

	/**
	 * String representing the Solaris Opteron operating system
	 */
	public static final String OPTERON = "Solaris Opteron";

	/**
	 * String representing the Linux PPC operating system
	 */
	public static final String LINUX_PPC = "Linux PPC";

	/**
	 * String representing the Linux AMD64 operating system
	 */
	public static final String LINUX_AMD64 = "Linux AMD64";

	/**
	 * String representing a Linux operating system
	 */
	public static final String LINUX = "Linux";

	/**
	 * String representing the HP-UX 64 bit Itanium operating system
	 */
	public static final String HPIA64 = "HP-UX IA";

	/**
	 * String representing an HP-UX operating system
	 */
	public static final String HPUX = "HP-UX";

	/**
	 * String representing an unknown operating system
	 */
	public static final String UNKNOWN = "Unknown";

	/**
	 * String representing a fixpack. If a fix name starts with this, it is a
	 * fixpack.
	 */
	public static final String FIXPACK_INDICATOR = "TDI-7.1-FP";

	/**
	 * Given an InputStream, will read a file into an array.
	 * 
	 * @param infile
	 *            The InputStream to be read
	 * @return An array of the lines in the file, or null if the file is empty
	 *         or an error occurred
	 */
	public static String[] readFile(InputStream infile) {
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(infile));
			Vector<String> fileLines = new Vector<String>();
			String temp = in.readLine();
			while (temp != null) {
				temp = temp.trim();
				fileLines.add(temp);
				temp = in.readLine();
			}
			in.close();
			if (fileLines.size() == 0)
				return null;

			String[] lines = new String[fileLines.size()];
			for (int i = 0; i < fileLines.size(); i++)
				lines[i] = fileLines.get(i);
			return lines;
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"GENERIC.STREAM.READ.ERROR", e.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"GENERIC.STREAM.READ.ERROR", e.getLocalizedMessage()),
					UpdateInstallerMsgs.ERROR);
			return null;
		}
	}

	/**
	 * Given a filename, will read that file into an array.
	 * 
	 * @param filename
	 *            The name of the file to be read
	 * @return An array of the lines in the file, or null if the file is empty
	 *         or an error occurred
	 */
	public static String[] readFile(String filename) {
		FileInputStream infile = null;
		try {
			infile = new FileInputStream(filename);
			String[] lines = readFile(infile);
			return lines;
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"FIXUTILS.FILE.READ.ERROR", filename, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIXUTILS.FILE.READ.ERROR", filename, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			return null;
		}
	}

	/**
	 * Executes an operating system command.
	 * 
	 * @param commands
	 *            An array representing a command to run and its arguments
	 * @return 0 if the command completed without an error, nonzero otherwise
	 */
	public static int executeCommand(String[] commands) {
		int rc = 0;

		try {

			Process child = Runtime.getRuntime().exec(commands);

			InputStream stdOut = child.getInputStream();
			InputStream stdErr = child.getErrorStream();

			GetStreamOutput errorThread = new GetStreamOutput(stdErr);
			GetStreamOutput outputThread = new GetStreamOutput(stdOut);

			// kick them off
			errorThread.start();
			outputThread.start();

			int rc1 = child.waitFor();

			errorThread.join();
			outputThread.join();

			int rc2 = child.waitFor();

			if (rc1 != 0)
				rc = rc1;
			else if (rc2 != 0)
				rc = rc2;

			if (rc != 0) {
				String message = errorThread.getMessage();
				if (message == null || message.trim().isEmpty())
					message = outputThread.getMessage();
				System.out.println(UpdateInstallerMsgs.getString(
						"FIXUTILS.EXECUTE.ERROR", Integer.valueOf(rc), message));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"FIXUTILS.EXECUTE.ERROR", Integer.valueOf(rc),
						message), UpdateInstallerMsgs.ERROR);
				UpdateInstallerMsgs.log(outputThread.getMessage(),
						UpdateInstallerMsgs.DEBUG);
			}
		} catch (Exception ioe) {
			System.out.println(UpdateInstallerMsgs.getString(
					"FIXUTILS.EXECUTE.EXCEPTION", ioe.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIXUTILS.EXECUTE.EXCEPTION", ioe.getLocalizedMessage()),
					UpdateInstallerMsgs.ERROR);
		}
		return rc;
	}

	/**
	 * Determines the current operating system.
	 * 
	 * @return A string from the list above representing the current operating
	 *         system
	 */
	public static String getOS() {
		String osName = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");

		if (osName.startsWith("Win")) {
			if (arch.startsWith("amd64"))
				return WINDOWS64;
			else
				return WINDOWS;
		}
		if (osName.startsWith("AIX")) {
			if (arch.startsWith("ppc64"))
				return AIX64;
			else
				return AIX;
		}
		if (osName.startsWith("SunOS")) {
			if (arch.startsWith("x86"))
				return OPTERON;
			else
				return SOLARIS;
		}

		if (osName.startsWith("Linux")) {
			if (arch.startsWith("ppc"))
				return LINUX_PPC;
			else if (arch.startsWith("s390"))
				return ZLINUX;
			else if (arch.startsWith("amd64"))
				return LINUX_AMD64;
			else
				return LINUX;
		}

		if (osName.startsWith("HP")) {
			if (arch.startsWith("PA_RISC")) // PA_RISC or PA_RISC2.0
				return HPUX;    //Could be 32 or 64 bit
			else if (arch.startsWith("IA")) // Maybe IA64W 
				return HPIA64;  //Could be 32 or 64 bit
			else return HPUX; //not sure which HP...
		}
		
		if (osName.equalsIgnoreCase("OS/400"))
			return I5OS;
		
		UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString("OS.UNKNOWN",
				osName), UpdateInstallerMsgs.DEBUG);
		return UNKNOWN;

	}

	/**
	 * Creates a temporary script named ./tempScript.sh from the commands in the
	 * passed in array and then executes that script. The temporary script is
	 * deleted after it is executed. This method is only intended for use on a
	 * UNIX operating system. The purpose of this function is to be able to
	 * executed that you normally can't by simply calling a regular exec, or if
	 * you want to group several shell cmds together in one call.
	 * 
	 * @param cmds
	 *            Commands to write to the temporary script file
	 * @return 0 if no error was detected, nonzero otherwise
	 */
	public static int createAndExecUnixScript(String[] cmds) {
		String tempFile = "./tempScript.sh";

		FileWriter outputFile = null;
		try {
			outputFile = new FileWriter(tempFile);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"FIXUTILS.FILE.CREATE.ERROR", tempFile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"FIXUTILS.FILE.CREATE.ERROR", tempFile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			return -1;
		}
		PrintWriter outfile = new PrintWriter(outputFile);

		if (cmds.length > 0 && !cmds[0].startsWith("!"))
			outfile.println("#!/bin/sh");

		for (int i = 0; i < cmds.length; i++) {
			outfile.println(cmds[i]);
		}

		// Close the file
		try {
			outfile.close();
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", tempFile, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", tempFile, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			return -1;
		}

		// Make it executable...
		executeCommand(new String[] { "chmod", "+x", tempFile });

		// Now execute it...
		int rc = executeCommand(new String[] { tempFile });

		// Now delete it
		executeCommand(new String[] { "rm", "-f", tempFile });

		return rc;
	}

	/**
	 * Default class constructor
	 */
	private FixUtils() {
	}
}
