/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ResourceHash.java
package com.ibm.di.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;

import com.ibm.di.function.UserFunctions;
import com.ibm.di.util.ResourceLocator;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.StringTokenizer;

/**
 * Utility class for locating NLS strings. The strings have their special
 * sequences (e.g. "\n", "\t") replaced with corresponding special characters
 * (e.g. new line, tab).
 */

public class ResourceHash {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static Hashtable<String, ResourceHash> instance = new Hashtable<String, ResourceHash>();

	private static Hashtable<String, String> docCache = new Hashtable<String, String>();

	private static Hashtable<String, String> docCacheMissing = new Hashtable<String, String>();

	private static Hashtable<String, URL> resourceCache = new Hashtable<String, URL>();

	private Hashtable<String, String> stringCache;

	private String language;
	
	private static final String PROPERTIES_FILE = "resourcehash";

	private static boolean initializedSelf = false;

	private static ResourceHash sResHash = null;

	public static ResourceHash getHash(String category) {
		synchronized (instance) {
			if (instance.get(category) == null)
				instance.put(category, new ResourceHash(category));

			return instance.get(category);
		}
	}

	/**
	 * A ResourceHash specified by language.
	 * @param category
	 * @param acceptLang - As used in the HTTP Accept-Language header
	 * @return
	 */
	public static ResourceHash getHash(String category, String acceptLang) {
		String lang = null;
		if (acceptLang != null) {
			for (String part: acceptLang.split(",")) {
				lang = guessLanguage(part);
				if (lang != null)
					break;
			}
		}
		if (lang == null) {
			// Use Locale
			Locale loc = Locale.getDefault();
			lang = guessLanguage(loc.getLanguage() + "-" + loc.getCountry());
			if (lang == null)
				lang = guessLanguage(loc.getLanguage());
			if (lang == null)
				lang = "en";
		}
		synchronized (instance) {
			String combine = category + "." + lang;
			if (instance.get(combine) == null)
				instance.put(combine, new ResourceHash(category, lang));
			return instance.get(combine);
		}
	}

	public ResourceHash(String category) {
		fillCache(getDocument(category + ".properties"));
	}

	public ResourceHash(String category, String lang) {
		fillCache(getDocument(category + ".properties", lang));
	}

	private void fillCache(String ma) {
		stringCache = new Hashtable<String, String>();
		if (ma == null)
			return;

		StringTokenizer st = new StringTokenizer(ma, "\n");

		char ch = ':'; // a dummy value
		boolean guess = true;

		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			if (str.startsWith("/") || str.startsWith("#"))
				continue;

			if (guess) {
				if (str.trim().length() == 0)
					continue;
				// heuristic...
				int i1 = str.indexOf('=');
				int i2 = str.indexOf(':');

				if (i1 < 0 && i2 < 0)
					continue;

				if (i1 < 0)
					ch = ':';
				else if (i2 < 0)
					ch = '=';
				else if (i1 < i2)
					ch = '=';
				else
					ch = ':';
				guess = false;
			}

			int i = str.indexOf(ch);
			if (i > 0) {

				String msgKey = str.substring(0, i).trim();
				String msgText = substituteSpecialSequences(str.substring(i + 1));

				stringCache.put(msgKey, msgText);
			}
		}
	}

	public static String getDocument(String path) {

		if (path == null)
			return null;

		if (docCache.get(path) != null)
			return docCache.get(path);

		if (docCacheMissing.get(path) != null)
			return null;

		URL url = getResource(path);

		if (url == null) {
			docCacheMissing.put(path, "");
			return null;
		}
		String result = readDocument(url);
		if (result != null)
			docCache.put(path, result);	
		return result;
	}

	private String getDocument(String path, String lang) {
		if (path == null)
			return null;

		URL url = getResource(path, lang);

		if (url != null)
			setLanguage(lang);
		else
			url = getResource(path);

		return readDocument(url);
	}

	private void setLanguage(String lang) {
		language = lang.replace('_', '-');
	}
	
	public String getLanguage() {
		return language;
	}

	private static String readDocument(URL url) {
		try {
			StringBuffer buf = new StringBuffer();
			StringBuffer linebuf = new StringBuffer();
			BufferedReader is = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			int ch;

			while ((ch = is.read()) != -1) {
				if (ch != '\r')
					linebuf.append((char) ch);
				if (ch == '\n') {
					String line = linebuf.toString();
					if (!line.equals("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n"))
						buf.append(line);
					linebuf.setLength(0);
				}
			}
			if (linebuf.length() > 0)
				buf.append(linebuf.toString());
			is.close();

			String result = buf.toString();
			return result;
		} catch (Exception e) {
			System.err.println(myGetString("RESOURCEHASH.ERROR.GETTING.PROP.FILE", e));
			return null;
		}
	}

	public static URL getResource(String path) {
		URL url = resourceCache.get(path);
		if (url != null)
			return url;

		File f = new File(".", path);
		if (!f.exists())
			f = new File(System.getProperty("com.ibm.di.installdir"), path);

		if (f.exists()) {
			try {
				url = new URL("file", "", f.getAbsolutePath().replace(File.separatorChar, '/'));
			} catch (Exception ignore) {
			}
		}

		if (url == null) {
			Locale locale = Locale.getDefault();
			if (locale != null) {
				String lang = locale.getLanguage();
				String country = "_" + locale.getCountry();

				int index;
				String pre, post;

				// First try to insert the language at the last "."
				index = path.lastIndexOf('.');
				if (index > 0) {
					pre = path.substring(0, index);
					post = path.substring(index);
				} else {
					pre = path;
					post = "";
				}

				url = ResourceLocator.getResourceURL(pre + "_" + lang + country + post);

				if (url == null) {
					url = ResourceLocator.getResourceURL(pre + "_" + lang + post);
				}

				// Then try to insert the language at the last "/"
				index = path.lastIndexOf('/');
				if (url == null && index > 0) {
					pre = path.substring(0, index + 1);
					post = path.substring(index);
					// try with and without NLS/
					url = ResourceLocator.getResourceURL(pre + "NLS/" + lang + country + post);
					if (url == null) {
						url = ResourceLocator.getResourceURL(pre + "NLS/" + lang + post);
					}
					if (url == null) {
						url = ResourceLocator.getResourceURL(pre + lang + country + post);
					}
					if (url == null) {
						url = ResourceLocator.getResourceURL(pre + lang + post);
					}
				}

			}

			if (url == null) {
				url = ResourceLocator.getResourceURL(path);
			}
		}

		if (url != null) {
			resourceCache.put(path, url);
		}
		return url;
	}

	/**
	 * Replaces special characters such as new lines and tabs with special
	 * sequences such as "\n" and "\t". It acts as an opposite of the
	 * <code>substituteSpecialSequences<code/> method.
	 * 
	 * @param str
	 *            a string, to work on
	 * @return the string, having its special characters replaced
	 */
	public static String escapeSpecialChars(String str) {

		StringBuilder res = new StringBuilder(str);

		final int escapeSequenceLength = 2;
		char[] seq = new char[escapeSequenceLength];
		seq[0] = '\\';
		boolean found = false;

		for (int i = 0; i < res.length(); i++) {

			switch (res.charAt(i)) {
			case '\n':
				seq[1] = 'n';
				found = true;
				break;
			case '\r':
				seq[1] = 'r';
				found = true;
				break;
			case '\t':
				seq[1] = 't';
				found = true;
				break;
			case '\\':
				seq[1] = '\\';
				found = true;
				break;
			}

			if (found) {
				found = false;

				// delete one char and insert N number of chars
				res.deleteCharAt(i);
				res.insert(i, seq);

				// have to move the index with (N-1) positions ahead
				i += escapeSequenceLength - 1;
			}

		}
		return res.toString();
	}

	/**
	 * Replaces special sequences with their corresponding special characters:
	 * "\n" with a new line, "\t" a with tab, etc. It acts as an opposite of the
	 * <code>escapeSpecialChars</code> method.
	 * 
	 * @param str
	 *            a String, to work on
	 * @return the String, having its special sequences replaced
	 */
	public static String substituteSpecialSequences(String str) {
		if (str.indexOf('\\') < 0)
			return str;

		char[] out = new char[str.length()];
		int outLen = 0;

		for (int i = 0, n = str.length(); i < n;) {
			char c = str.charAt(i++);
			if (c == '\\' && i < n) {
				c = str.charAt(i++);
				if (c == 'u' || c == 'U') {
					int value = 0;
					for (int j = 0; j < 4; j++) {
						c = str.charAt(i++);
						switch (c) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + c - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + c - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + c - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (c == 't')
						c = '\t';
					else if (c == 'n')
						c = '\n';
					else if (c == 'r')
						c = '\r';
					else if (c == 'f')
						c = '\f';
					out[outLen++] = c;
				}
			} else {
				out[outLen++] = c;
			}
		}
		return new String(out, 0, outLen);
	}

	/**
	 * Return the NLS string given the resource
	 */

	public String getString(String resource) {

		String str = stringCache.get(resource);
		if (str != null)
			return str;
		else
			return resource;
	}

	/**
	 * Return the NLS string given the resource and a parameter
	 */

	public String getString(String resource, Object param) {
		if (stringCache.get(resource) == null)
			return resource + ": " + param;
		else
			return MessageFormat.format(stringCache.get(resource), new Object[] { param });
	}

	/**
	 * Returns the NLS string value for the passed "resource" (key) and replaces
	 * the placeholders {0},{1},etc by the corresponding
	 * params[0],params[1],etc.
	 * 
	 * @param resource
	 *            The key whole value is to be retrieved.
	 * @param params
	 *            An array of strings which will replace placeholders
	 * @return The value with placeholders replaced.
	 */
	public String getString(String resource, Object[] params) {
		if (stringCache.get(resource) == null)
			return resource; // Should maybe add params, but this is only
		// emergency code anyway
		else
			return MessageFormat.format(stringCache.get(resource), params);
	}

	private static String myGetString(String resource, Object e) {
		if (!initializedSelf) {
			initializedSelf = true;
			sResHash = getHash(PROPERTIES_FILE);
		}
		if (sResHash == null)
			return resource + ": " + e; // Emergency code

		return sResHash.getString(resource, e);
	}
	
	/**
	 * 
	 * @param path
	 * @param lang The language, country may be appended with underscore between
	 * @return
	 */
	private static URL getResource(String path, String lang) {
		int index;
		String pre, post;

		// First try to insert the language at the last "."
		index = path.lastIndexOf('.');
		if (index > 0) {
			pre = path.substring(0, index);
			post = path.substring(index);
		} else {
			pre = path;
			post = "";
		}

		URL url = ResourceLocator.getResourceURL(pre + "_" + lang + post);

		// Then try to insert the language at the last "/"
		index = path.lastIndexOf('/');
		if (url == null && index > 0) {
			pre = path.substring(0, index + 1);
			post = path.substring(index);
			// try with and without NLS/
			url = ResourceLocator.getResourceURL(pre + "NLS/" + lang + post);
			if (url == null)
				url = ResourceLocator.getResourceURL(pre + lang + post);
		}
		return url;
	}

	private static String guessLanguage(String part) {
		if (part.indexOf(';') >= 0)
			part = part.substring(0, part.indexOf(';')); // Could verify that q > 0
		part = part.trim();
		if (UserFunctions.startsWithIC(part, "en"))
			return "en";
		if (UserFunctions.startsWithIC(part, "de"))
			return "de";
		if (UserFunctions.startsWithIC(part, "es"))
			return "es";
		if (UserFunctions.startsWithIC(part, "fr"))
			return "fr";
		if (UserFunctions.startsWithIC(part, "it"))
			return "it";
		if (UserFunctions.startsWithIC(part, "ja"))
			return "ja";
		if (UserFunctions.startsWithIC(part, "ko"))
			return "ko";
		if (part.equalsIgnoreCase("pt") || part.equalsIgnoreCase("pt-BR"))
			return "pt_BR";
		if (part.equalsIgnoreCase("zh") || part.equalsIgnoreCase("zh-CN"))
			return "zh_CN";
		if (part.equalsIgnoreCase("zh-TW"))
			return "zh_TW";
		return null;
	}

}
