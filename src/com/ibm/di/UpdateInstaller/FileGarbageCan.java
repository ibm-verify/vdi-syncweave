/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.UpdateInstaller;

import java.io.File;
import java.util.Vector;

/**
 * Represents files that need to be deleted after a program completes. Files may
 * be added to the can throughout program execution through a call to add, and
 * may be deleted by a call to empty before the program terminates.
 * 
 * @author Alan Watkins
 * 
 */
public class FileGarbageCan {

	/**
	 * The copyright notice for binary java code required by legal.
	 */
	private static final String COPYRIGHT = com.ibm.di.UpdateInstaller.FixUtils.OBJECT_CODE;

	/**
	 * List of files that need to be deleted.
	 */
	private static Vector<File> can = null;

	/**
	 * Private default constructor - do not make instances of this class
	 */
	private FileGarbageCan() {
	}

	/**
	 * Add a file to the trash can.
	 * 
	 * @param trash
	 *            File to be deleted
	 */
	public static void add(File trash) {
		if (can == null)
			can = new Vector<File>();
		can.add(trash);
	}

	/**
	 * Add a file to the trash can.
	 * 
	 * @param trash
	 *            File to be deleted
	 */
	public static void add(String trash) {
		add(new File(trash));
	}

	/**
	 * Deletes all of the files in the trash can. This method uses the
	 * deleteOnExit() method from File, so the files will not actually be
	 * deleted until the JVM exits.
	 * 
	 * @see java.io.File
	 */
	public static void empty() {
		if (can == null)
			return;
		while (can.size() > 0) {
			File f = can.remove(0);
			f.deleteOnExit();
		}

	}
}
