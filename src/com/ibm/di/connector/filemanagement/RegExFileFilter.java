/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.filemanagement;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Class used as file filter when traversing a directory tree.
 * 
 * @since 7.2
 */
class RegExFileFilter implements FileFilter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Windows/Dos OS flag;
	 */
	protected boolean isDos = (File.separatorChar == '\\');
	/**
	 * Regular expression pattern
	 */
	private Pattern pattern;

	/**
	 * Start directory path.
	 */
	private String startDir;

	/**
	 * Constructs initial file filter based on a Pattern String.
	 * 
	 * @param startDirecotry
	 *            start directory path
	 * @param patternString
	 *            the pattern to be used when matching.
	 */
	RegExFileFilter(String startDirecotry, String patternString) {
		startDir = startDirecotry;
		pattern = createPattern(patternString);
	}

	/**
	 * Creates regular expression pattern from given string, taking into account
	 * the host OS.
	 * 
	 * @param patternString
	 *            String used to create the regular expression pattern.
	 * @return regular expression pattern.
	 */
	protected Pattern createPattern(String patternString) {
		Pattern pattern = null;
		if (patternString != null && patternString.trim().length() > 0) {
			if (isDos) {
				pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			} else {
				pattern = Pattern.compile(patternString);
			}
		}
		return pattern;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean accept(File pathName) {
		if (pathName == null) {
			return false;
		}
		if (pathName.isDirectory()) {
			return true;
		}
		return checkPath(pathName.getAbsolutePath());
	}

	/**
	 * Check if the file's path matches the pattern.
	 * 
	 * @param fullPath
	 *            file path to be checked.
	 * @return true if the file path matches the pattern, otherwise false.
	 */
	final boolean checkPath(String fullPath) {
		String path = fullPath.substring(startDir.length() + 1);
		if (pattern == null) {
			return true;
		}
		return pattern.matcher(path).matches();
	}
}
