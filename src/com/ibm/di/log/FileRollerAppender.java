/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.log;

import java.io.File;

import com.ibm.di.server.ResourceHash;

import org.apache.logging.log4j.LogManager;

public class FileRollerAppender {

	private int rollCount = 5;

	private String fileName;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public FileRollerAppender(String fileName) {
		this.fileName = fileName;
	}

	public void setRollCount(String count) throws NumberFormatException {
		rollCount = Integer.parseInt(count);
		if (rollCount <= 0)
			throw new NumberFormatException("RollCount must be positive");
	}

	public void setFile(String file) {
		fileName = file;
	}

	public void rotateFiles() {

		// Rotate log files
		File file = new File(fileName);
		if (file.exists() && file.length() > 0) {
			for (int i = rollCount; i >= 0; i--) {
				File next = new File(fileName + (i == 0 ? "" : "." + i));
				if (!next.exists())
					continue;

				if (i == rollCount) {
					if (!next.delete()) {
						LogManager.getRootLogger().error(
								sResHash.getString("MISERVER.FILEROLLERAPPENDER.CANNOT.DELETE.FILE", next));
					}
				} else {
					if (!next.renameTo(new File(fileName + "." + (i + 1)))) {
						LogManager.getRootLogger().error(
								sResHash.getString("MISERVER.FILEROLLERAPPENDER.CANNOT.RENAME.FILE", next));
					}
				}
			}
		}
	}
}
