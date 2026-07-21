/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Allows modification of ASCII files. This class is intended to provide the
 * functionality of the update ASCII file modifications in ISMP 11.5 for the
 * update installer.
 * 
 * @author Alan Watkins
 * 
 */
public class ASCIIFileUpdate {

	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * Filename to alter
	 */
	private String filename = null;

	/**
	 * BufferedReader used to read in file
	 */
	private BufferedReader in = null;

	/**
	 * List of items to search for in a file
	 */
	private Vector<String> searches = null;

	/**
	 * After a search is found, this is what replaces the matches
	 */
	private Vector<String> replacements = null;

	/**
	 * List of files to search
	 */
	private Vector<String> file = null;

	/**
	 * Line mode means that an entire line will be replaced
	 */
	private static final int LINE_MODE = 0;

	/**
	 * Text mode means that just the matched item will be replaced
	 */
	private static final int TEXT_MODE = 1;

	/**
	 * Replacement mode: LINE_MODE or TEXT_MODE
	 */
	private int mode;

	/**
	 * Class constructor that takes a filename to make replacements on. Searches
	 * and replacements must be added.
	 * 
	 * @param filename
	 *            The file that will be altered
	 * @throws Exception
	 *             If there is an error processing the file
	 */
	public ASCIIFileUpdate(String filename) throws Exception {
		this.filename = filename;
		FileInputStream infile = null;
		try {
			infile = new FileInputStream(filename);
		} catch (Exception e) {
			System.out.println(UpdateInstallerMsgs.getString(
					"ASCIIFILEUPDATE.FILE.OPEN.ERROR", filename, e
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"ASCIIFILEUPDATE.FILE.OPEN.ERROR", filename, e
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
			throw e;
		}
		InputStreamReader inputReader = new InputStreamReader(infile);
		in = new BufferedReader(inputReader);

		searches = new Vector<String>();
		replacements = new Vector<String>();
		file = new Vector<String>();
		mode = LINE_MODE;
	}

	/**
	 * Sets the mode for this replacement.
	 * 
	 * @param mode
	 *            LINE_MODE or TEXT_MODE; the default is LINE_MODE
	 */
	public void setMode(int mode) {
		if (mode < LINE_MODE || mode > TEXT_MODE)
			this.mode = LINE_MODE;
		else
			this.mode = mode;
	}

	/**
	 * Class constructor that takes a filename to make replacements on, an array
	 * of matches, and an array of replacements.
	 * 
	 * @param filename
	 *            The file that will be altered
	 * @param matches
	 *            An array of regular expressions to search for
	 * @param replace
	 *            A parallel array to matches which represents the replacements
	 * @throws Exception
	 *             If there is an error processing the file
	 */
	public ASCIIFileUpdate(String filename, String[] matches, String[] replace)
			throws Exception {
		this(filename);
		for (int i = 0; i < matches.length; i++) {
			searches.add(matches[i]);
			replacements.add(replace[i]);
		}
	}

	/**
	 * Adds a search and replace term to the array of items to search for
	 * 
	 * @param search
	 *            A regexp to search for
	 * @param replace
	 *            A replacement for the corresponding search
	 */
	public void addPattern(String search, String replace) {
		searches.add(search);
		replacements.add(replace);
	}

	/**
	 * Closes the BufferedReader associated with the current file
	 */
	private void close() {
		try {
			in.close();
		} catch (IOException ioe) {
			System.out.println(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", filename, ioe
							.getLocalizedMessage()));
			UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
					"GENERIC.FILE.CLOSE.ERROR", filename, ioe
							.getLocalizedMessage()), UpdateInstallerMsgs.ERROR);
		}
	}

	/**
	 * Performs the actual alteration of the file.
	 * 
	 * @throws Exception
	 *             if an error is encountered when reading or writing to the
	 *             file
	 */
	public void alterFile() throws Exception {
		String temp = "";

		while (temp != null) {
			// Read line from file
			try {
				temp = in.readLine();
				if (temp != null)
					file.add(temp);
			} catch (Exception e) {
				System.out.println(UpdateInstallerMsgs.getString(
						"ASCIIFILEUPDATE.READ.ERROR", filename, e
								.getLocalizedMessage()));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"ASCIIFILEUPDATE.READ.ERROR", filename, e
								.getLocalizedMessage()),
						UpdateInstallerMsgs.ERROR);
				close();
				throw (e);
			}
		}
		close();

		boolean need2Write = false;
		String cur_regexp = "";
		for (int i = 0; i < file.size(); i++) {
			String line = (String) file.elementAt(i);
			for (int x = 0; x < searches.size(); x++) {
				cur_regexp = (String) searches.elementAt(x);
				if (line.indexOf(cur_regexp) != -1) {
					need2Write = true;
					if (mode == LINE_MODE) {
						file.remove(line);
						file.add(i, (String) replacements.elementAt(x));
						break; // Only one line replacement...
					} else { // text mode
						int z = line.indexOf(cur_regexp);
						file.remove(line);
						while (z != -1) {
							line = line.substring(0, z)
									+ replacements.elementAt(x)
									+ line.substring(z + cur_regexp.length());
							z = line.indexOf(cur_regexp);
						}
						file.add(i, line);
					}
				}

			}
		}
		if (need2Write) {
			FileWriter outputFile = null;
			try {
				outputFile = new FileWriter(filename);
			} catch (Exception e) {
				System.out.println(UpdateInstallerMsgs.getString(
						"ASCIIFILEUPDATE.FILE.OPEN.ERROR", filename, e
								.getLocalizedMessage()));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"ASCIIFILEUPDATE.FILE.OPEN.ERROR", filename, e
								.getLocalizedMessage()),
						UpdateInstallerMsgs.ERROR);
				throw e;
			}
			PrintWriter outfile = new PrintWriter(outputFile);

			for (int i = 0; i < file.size(); i++)
				outfile.println((String) file.elementAt(i));

			try {
				outfile.close();
			} catch (Exception e) {
				System.out.println(UpdateInstallerMsgs.getString(
						"GENERIC.FILE.CLOSE.ERROR", filename, e
								.getLocalizedMessage()));
				UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
						"GENERIC.FILE.CLOSE.ERROR", filename, e
								.getLocalizedMessage()),
						UpdateInstallerMsgs.ERROR);
			}
		}
	}

	/**
	 * Prints a usage statement to standard out.
	 */
	private static void usage() {
		System.out.println(UpdateInstallerMsgs
				.getString("ASCIIFILEUPDATE.USAGE"));
	}

	/**
	 * Performs a modification to a file. If the modification to be made only
	 * takes one search and replace term, use this method instead of
	 * instantiating an object.
	 * 
	 * @param args
	 *            An array of arguments for this modification: file search
	 *            replacement [LINE|TEXT]<BR>
	 *            If the last argument is omitted, LINE_MODE is used.
	 * 
	 * @return 0 if the modification was successful, nonzero otherwise
	 */
	public static int simpleModify(String[] args) {
		int rc = 0;
		int mode = ASCIIFileUpdate.LINE_MODE;
		if (args.length != 3 && args.length != 4) {
			usage();
			rc = -1;
		} else if (args.length == 4) {
			if (!args[3].equalsIgnoreCase("LINE")
					&& !args[3].equalsIgnoreCase("TEXT")) {
				usage();
				rc = -1;
			} else if (args[3].equalsIgnoreCase("TEXT"))
				mode = ASCIIFileUpdate.TEXT_MODE;
		}

		if (rc == 0) {
			try {
				ASCIIFileUpdate e = new ASCIIFileUpdate(args[0]);
				e.setMode(mode);
				e.addPattern(args[1], args[2]);
				e.alterFile();
			} catch (Exception e) {
				if (e instanceof java.io.FileNotFoundException) {
					// Already output to stdout
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"FILE.NOT.FOUND", args[0]),
							UpdateInstallerMsgs.DEBUG);
				} else {
					// Already output to stdout
					UpdateInstallerMsgs.log(UpdateInstallerMsgs.getString(
							"FILE.NOT.FOUND", e.getLocalizedMessage()),
							UpdateInstallerMsgs.DEBUG);
				}
				rc = -1;
			}
		}
		return rc;
	}
}
