/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.filemanagement;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class used as file filter, matching Glob Expression pattern, when traversing
 * a directory tree.
 * <p>
 * <b>Important: Functionality of this class will be replaced by the new Java 7
 * API.</b> <br>
 * Code used instead: <code>
 * <pre>
 * String globPatter = "**.{java,class}"
 * PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPatter);
 * 
 * Path filename = ...;
 * if (matcher.matches(filename)) {
 * 	System.out.println(filename);
 * }
 * </pre>
 * </code>
 * </p>
 * 
 * @since 7.2
 */
class GlobFileFilter extends RegExFileFilter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * End OF Line char.
	 */
	private static final char EOL = 0;

	/**
	 * Contains all chars used in regular expression as meta chars.
	 */
	private static final String REGEX_META_CHARS = ".^$+{[]|()";

	/**
	 * Contains all chars used in glob expression as meta chars.
	 */
	private static final String GLOB_META_CHARS = "\\*?[{";

	/**
	 * Constructs initial file filter based on a Pattern String.
	 * 
	 * @param startDirecotry
	 *            start directory path
	 * @param patternString
	 *            the pattern to be used when matching.
	 */
	GlobFileFilter(String startDirecotry, String patternString) {
		super(startDirecotry, patternString);
	}

	/**
	 * Check char if is a regular expression meta char.
	 * 
	 * @param c
	 *            char to be checked.
	 * @return true if char is a regular expression meta char, else false.
	 */
	private boolean isRegexMeta(char c) {
		return REGEX_META_CHARS.indexOf(c) != -1;
	}

	/**
	 * Check if the provided char is a glob expression meta char.
	 * 
	 * @param c
	 *            char to be checked.
	 * @return true if char is a glob expression meta char, else false.
	 */
	private boolean isGlobMeta(char c) {
		return GLOB_META_CHARS.indexOf(c) != -1;
	}

	/**
	 * Return next char from String by index.
	 * 
	 * @param glob
	 *            String used for source.
	 * @param i
	 *            index of char.
	 * @return next char from String by index.
	 */
	private char next(String glob, int i) {
		if (i < glob.length()) {
			return glob.charAt(i);
		}
		return EOL;
	}

	/**
	 * Creates a regular expression pattern from the given glob expression
	 * taking into account the host OS.
	 * 
	 * @param patternString
	 *            String used to create the regular expression pattern.
	 * @return regular expression pattern.
	 */
	@Override
	protected Pattern createPattern(String patternString) {
		Pattern pattern = null;
		if (patternString != null && patternString.trim().length() > 0) {

			boolean inGroup = false;
			StringBuilder regex = new StringBuilder("^");

			int i = 0;
			while (i < patternString.length()) {
				char c = patternString.charAt(i++);
				switch (c) {
				case '\\':
					// escape special characters
					if (i == patternString.length()) {
						throw new PatternSyntaxException("No character to escape", patternString, i - 1); //$NON-NLS-1$
					}
					char next = patternString.charAt(i++);
					if (isGlobMeta(next) || isRegexMeta(next)) {
						regex.append('\\');
					}
					regex.append(next);
					break;
				case '/':
					if (isDos) {
						regex.append("\\\\");
					} else {
						regex.append(c);
					}
					break;
				case '[':
					// don't match name separator in class
					if (isDos) {
						regex.append("[[^\\\\]&&[");
					} else {
						regex.append("[[^/]&&[");
					}
					if (next(patternString, i) == '^') {
						// escape the regex negation char if it appears
						regex.append("\\^");
						i++;
					} else {
						// negation
						if (next(patternString, i) == '!') {
							regex.append('^');
							i++;
						}
						// hyphen allowed at start
						if (next(patternString, i) == '-') {
							regex.append('-');
							i++;
						}
					}
					boolean hasRangeStart = false;
					char last = 0;
					while (i < patternString.length()) {
						c = patternString.charAt(i++);
						if (c == ']') {
							break;
						}
						if (c == '/' || (isDos && c == '\\')) {
							throw new PatternSyntaxException("Explicit 'name separator' in class", patternString, i - 1); //$NON-NLS-1$
						}
						if (c == '\\' || c == '[' || c == '&' && next(patternString, i) == '&') {
							// escape '\', '[' or "&&" for regex class
							regex.append('\\');
						}
						regex.append(c);

						if (c == '-') {
							if (!hasRangeStart) {
								throw new PatternSyntaxException("Invalid range", patternString, i - 1); //$NON-NLS-1$
							}
							if ((c = next(patternString, i++)) == EOL || c == ']') {
								break;
							}
							if (c < last) {
								throw new PatternSyntaxException("Invalid range", patternString, i - 3); //$NON-NLS-1$
							}
							regex.append(c);
							hasRangeStart = false;
						} else {
							hasRangeStart = true;
							last = c;
						}
					}
					if (c != ']') {
						throw new PatternSyntaxException("Missing ']", patternString, i - 1); //$NON-NLS-1$
					}
					regex.append("]]");
					break;
				case '{':
					if (inGroup) {
						throw new PatternSyntaxException("Cannot nest groups", patternString, i - 1); //$NON-NLS-1$
					}
					regex.append("(?:(?:");
					inGroup = true;
					break;
				case '}':
					if (inGroup) {
						regex.append("))");
						inGroup = false;
					} else {
						regex.append('}');
					}
					break;
				case ',':
					if (inGroup) {
						regex.append(")|(?:");
					} else {
						regex.append(',');
					}
					break;
				case '*':
					if (next(patternString, i) == '*') {
						// crosses directory boundaries
						regex.append(".*");
						i++;
					} else {
						// within directory boundary
						if (isDos) {
							regex.append("[^\\\\]*");
						} else {
							regex.append("[^/]*");
						}
					}
					break;
				case '?':
					if (isDos) {
						regex.append("[^\\\\]");
					} else {
						regex.append("[^/]");
					}
					break;

				default:
					if (isRegexMeta(c)) {
						regex.append('\\');
					}
					regex.append(c);
				}
			}

			if (inGroup) {
				throw new PatternSyntaxException("Missing '}", patternString, i - 1); //$NON-NLS-1$
			}

			String regExPattern = regex.append('$').toString();
			if (isDos) {
				pattern = Pattern.compile(regExPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			} else {
				pattern = Pattern.compile(regExPattern);
			}
		}
		return pattern;
	}

}
