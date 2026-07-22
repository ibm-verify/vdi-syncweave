/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.ibm.di.exceptions.ErrorListener;
import com.ibm.di.security.EncryptedReader;
import com.ibm.di.security.SecurityCrypto;
import com.ibm.di.util.FileUtils;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class parses configurations from .inf files. The parsed configurations
 * are being accumulated in the state of the FileConfig object. (Some time ago,
 * this class was used to load TDI configurations (configurations were stored in
 * .inf format rather than in XML). That is why some of the features (such as
 * encryption) exist.)
 * 
 * The configurations are represented by TreeMap objects and are organized into
 * a hierarchy. The hierarchy is composed by embedding TreeMap objects as values
 * inside other TreeMap objects. Navigation through the configuration hierarchy
 * is performed via the key names. Several keys can be combined to form a
 * compound key, which identifies a configuration (i.e. TreeMap object) in the
 * hierarchy. The individual parts of a compound key are delimited by a forward
 * slash : '/'. For example: "connectors/ibmdi.URLConnector/connectorConfig".
 * 
 * There are some predefined keys for objects in the configuration hierarchy.
 * These predefined keys are used to logically organize configurations into
 * categories such as Connectors, Parsers, Forms, ... The predefined keys are
 * available as String constants (C_CONNECTOR, C_PARSER, C_FORMDEF, ...) in the
 * FileConfig class.
 * 
 */
public class FileConfig {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// guiLoadProgress progress;

	/**
	 * The default property folder used.
	 */
	private static String DEFAULT_PROPERTY_FOLDER = "JavaProperties";

	/**
	 * he default folder for libraries used.
	 */
	private static String DEFAULT_LIBRARY_FOLDER = "JavaLibraries";

	/**
	 * The default folder for external properties used.
	 */
	private static String DEFAULT_EXTPROP_FOLDER = "ExternalProperties";

	/**
	 * Reserved keywork for included elements.
	 */
	private static String INCLUDED_COMPONENT = "%%EXTERNAL_PATH%%";

	/**
	 * Begin multilined string constant.
	 */
	private static String BEGIN_MULTILINE_STRING = "<<$$BEGIN_TEXT$$>>";

	/**
	 * End multilined string constant.
	 */
	private static String END_MULTILINE_STRING = "<<$$END_TEXT$$>>";

	/**
	 * AssemblyLine configurations are stored under this key.
	 */
	public static final String C_TASK = "task";

	/**
	 * Connector configurations are stored under this key.
	 */
	public static final String C_CONNECTOR = "connectors";

	/**
	 * Trigger configurations are stored under this key.
	 */
	public static final String C_TRIGGER = "trigger";

	/**
	 * Listener configurations are stored under this key.
	 */
	public static final String C_LISTENER = "listener";

	/**
	 * Library configurations are stored under this key.
	 */
	public static final String C_LIBRARIES = "libraries";

	/**
	 * Properties configurations are stored under this key.
	 */
	public static final String C_PROPERTIES = "properties";

	/**
	 * ParserTypes configurations are stored under this key.
	 */
	public static final String C_PARSER = "parsertypes";

	/**
	 * Script configurations are stored under this key.
	 */
	public static final String C_SCRIPTS = "scripts";

	/**
	 * Form configurations are stored under this key.
	 */
	public static final String C_FORMDEF = "form";

	/**
	 * Java properties configurations are stored under this key.
	 */
	public static final String C_JAVAPROPERTIES = "properties/java";

	/**
	 * Java libraries configurations are stored under this key.
	 */
	public static final String C_JAVALIBRARIES = "libraries/userFunctions";

	/**
	 * Message bus configurations are stored under this key.
	 */
	public static final String C_MBUS = "messagebus";

	/**
	 * Includes configurations are stored under this key.
	 */
	public static final String C_INCLUDE = "include";

	/**
	 * Security configurations are stored under this key.
	 */
	public static final String C_SECURITY = "security";

	/**
	 * Shared Connector configurations are stored under this key.
	 */
	public static final String C_SHAREDCONN = "sharedconnector";

	/**
	 * Includes configurations are stored under this key.
	 */
	public static final String C_INCLUDEFILES = "include";

	/**
	 * File separator.
	 */
	final static String fieldSep = "=";

	/**
	 * Password (key) for decrypting the configuration input stream/file. Must
	 * be null if the stream is not encrypted.
	 */
	public String password = null;

	/**
	 * Whether newly read configurations are allowed to overwrite previously
	 * stored configurations.
	 */
	public boolean dontOverwriteConfig = false;

	/**
	 * Whether the configuration currently being parsed is read from an include
	 * configuration file.
	 */
	public boolean isInclude = false;

	/**
	 * All include configuration files (the absolute file system paths).
	 */
	public Vector includedFiles = new Vector();

	/**
	 * The tables attribute of the FileConfig folder.
	 */
	TreeMap tables;

	/**
	 * Reader used for reading configuration files.
	 */
	BufferedReader input;

	/**
	 * The configPath attribute of the FileConfig folder.
	 */
	String path;

	/**
	 * The errorListener attribute of the FileConfig object. It is used to
	 * handle internally errors that have occurred.
	 */
	ErrorListener errorListener;

	/**
	 * The line attribute of the FileConfig object.
	 */
	private int linenumber;

	/**
	 * The last line read from the configuration file.
	 */
	private String lastlineread;

	/**
	 * The import value attribute of the FileConfig object.
	 */
	private Vector impvec = null;

	/**
	 * Version string.
	 */
	private String version = null;

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private ResourceHash res = ResourceHash.getHash("miserver");

	/**
	 * Constructor for the FileConfig object
	 * 
	 * @param path
	 *            the file system path or URL for the configuration file (the
	 *            file is not actually parsed at this time)
	 * @exception IOException
	 *                a problem while constructing the object
	 */
	public FileConfig(String path) throws IOException {
		Trace.entrymid(this, "FileConfig", path);
		this.path = path;
		tables = new TreeMap();
		password = null;
		Trace.exitmid(this, "FileConfig", path);
		// loadConfig();
	}

	/**
	 * Returns the version string.
	 * 
	 * @return a string representing the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version string.
	 * 
	 * @param version
	 *            a new version string to be set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Sets the password attribute of the FileConfig object
	 * 
	 * @param pwd
	 *            The new password value
	 */
	public void setPassword(String pwd) {
		password = pwd;
	}

	/**
	 * Sets the errorListener attribute of the FileConfig object
	 * 
	 * @param listener
	 *            The new errorListener value
	 */
	public void setErrorListener(ErrorListener listener) {
		this.errorListener = listener;
	}

	/**
	 * Sets the config attribute of the FileConfig object
	 * 
	 * @param key
	 *            The new config value
	 * @param data
	 *            The new config value
	 */
	public void setConfig(String key, Object data) {
		tables.put(key, data);
	}

	/**
	 * Sets the configPath attribute of the FileConfig object
	 * 
	 * @param path
	 *            The new configPath value
	 */
	public void setConfigPath(String path) {
		this.path = path;
	}

	/**
	 * Sets the tables attribute of the FileConfig object
	 * 
	 * @param tables
	 *            The new tables value
	 */
	public void setTables(TreeMap tables) {
		this.tables = tables;
	}

	/**
	 * Sets the import attribute of the FileConfig object
	 * 
	 * @param v
	 *            The new import value
	 */
	public void setImport(Vector v) {
		impvec = v;
	}

	/**
	 * Gets the config attribute of the FileConfig object
	 * 
	 * @param key
	 *            compound key, which identifies an object in the configuration
	 *            hierarchy
	 * @return The config value
	 */
	public TreeMap getConfig(String key) {
		// return (TreeMap) tables.get(key);
		return (TreeMap) getKey(tables, key);
	}

	/**
	 * Gets the configPath attribute of the FileConfig object
	 * 
	 * @return The configPath value
	 */
	public String getConfigPath() {
		return path;
	}

	/**
	 * Gets the tables attribute of the FileConfig object
	 * 
	 * @return The tables value
	 */
	public TreeMap getTables() {
		return tables;
	}

	/**
	 * Gets the line attribute of the FileConfig object
	 * 
	 * @return The line value
	 * @exception IOException
	 *                problem while reading the current line from the
	 *                configuration file
	 */
	public String getLine() throws IOException {

		String line;
		while ((line = input.readLine()) != null) {
			linenumber++;
			// if ( path != null && path.equals( "<stdin>" ) && line.equals(
			// "[EOF]" ) ) {
			// Soft end of file?
			if (line.equals("[EOF]")) {
				return null;
			}
			if (line.endsWith(BEGIN_MULTILINE_STRING)) {
				int len = line.length() - BEGIN_MULTILINE_STRING.length();
				StringBuffer multiLine = new StringBuffer(line
						.substring(0, len));
				multiLine.append("\n");
				do {
					String s = input.readLine();
					linenumber++;
					if (s == null || s.indexOf(END_MULTILINE_STRING) != -1)
						break;
					multiLine.append(s);
					multiLine.append("\n");
				} while (true);
				line = multiLine.toString();
			}
			lastlineread = line;
			return line;
		}
		return null;
	}

	/**
	 * Gets the task attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired
	 *            AssemblyLine configuration object
	 * @return The task value
	 */
	public TreeMap getTask(String name) {
		TreeMap tm = (TreeMap) tables.get(C_TASK);
		if (tm == null) {
			return tm;
		}

		return (TreeMap) tm.get(name);
	}

	/**
	 * Gets the includes attribute of the FileConfig object
	 * 
	 * @return The includes value
	 */
	public Vector getIncludes() {
		TreeMap tm = (TreeMap) tables.get(C_INCLUDE);
		if (tm == null) {
			return null;
		}

		return (Vector) tm.get("files");
	}

	/**
	 * Gets the connector attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired
	 *            Connector configuration object
	 * @return The connector value
	 */
	public TreeMap getConnector(String name) {
		Trace.entrymax(this, "getConnector", name);
		TreeMap tm = (TreeMap) tables.get(C_CONNECTOR);
		if (tm == null) {
			Trace.exitmax(this, "getConnector");
			return tm;
		}
		Trace.exitmax(this, "getConnector");
		return (TreeMap) tm.get(name);
	}

	/**
	 * Gets the parser attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired Parser
	 *            configuration object
	 * @return The parser value
	 */
	public TreeMap getParser(String name) {
		Trace.entrymax(this, "getParser", name);
		TreeMap tm = (TreeMap) tables.get(C_PARSER);
		if (tm == null) {
			Trace.exitmax(this, "getParser");
			return tm;
		}
		Trace.exitmax(this, "getParser");
		return (TreeMap) tm.get(name);
	}

	/*
	 * public TreeMap getTrigger (String name) { TreeMap tm = (TreeMap)
	 * tables.get(C_TRIGGER); if (tm == null) return tm; return (TreeMap)
	 * tm.get(name); }
	 */
	/**
	 * Gets the library attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired
	 *            Library configuration object
	 * @return The library value
	 */
	public TreeMap getLibrary(String name) {
		TreeMap tm = (TreeMap) tables.get(C_LIBRARIES);
		if (tm == null) {
			return null;
		}

		if (name == null) {
			return null;
		} else {
			return (TreeMap) tm.get(name);
		}
	}

	/**
	 * Gets the listener attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired
	 *            Listener configuration object
	 * @return The listener value
	 */
	public TreeMap getListener(String name) {
		TreeMap tm = (TreeMap) tables.get(C_LISTENER);
		if (tm == null) {
			return tm;
		}

		if (name == null) {
			return tm;
		} else {
			return (TreeMap) tm.get(name);
		}

	}

	/**
	 * Gets the script attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired Script
	 *            configuration object
	 * @return The script value
	 */
	public TreeMap getScript(String name) {
		Trace.entrymid(this, "getScript", name);
		TreeMap tm = (TreeMap) tables.get(C_SCRIPTS);
		if (tm == null) {
			Trace.exitmid(this, "getScript");
			return tm;
		}

		if (name == null) {
			Trace.exitmid(this, "getScript", tm);
			return tm;
		} else {
			Trace.exitmid(this, "getScript", tm);
			return (TreeMap) tm.get(name);
		}
	}

	/**
	 * Gets the property attribute of the FileConfig object
	 * 
	 * @param type
	 *            the key (simple key, not a compound one) of the desired
	 *            Properties configuration object
	 * @return The property value
	 */
	public TreeMap getProperty(String type) {

		TreeMap tm = (TreeMap) tables.get(C_PROPERTIES);
		if (tm == null) {
			return tm;
		}

		if (type == null) {
			return null;
		}

		return (TreeMap) tm.get(type);
	}

	/**
	 * Gets the form attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired Form
	 *            configuration object
	 * @return The form value
	 */
	public TreeMap getForm(String name) {
		TreeMap tm = (TreeMap) tables.get(C_FORMDEF);
		if (tm == null) {
			return tm;
		}

		if (name == null) {
			return tm;
		} else {
			return (TreeMap) tm.get(name);
		}
	}

	/**
	 * Gets the include attribute of the FileConfig object
	 * 
	 * @param name
	 *            the key (simple key, not a compound one) of the desired
	 *            Includes configuration object
	 * @return The include value
	 */
	public TreeMap getInclude(String name) {
		TreeMap tm = (TreeMap) tables.get(C_INCLUDEFILES);
		if (tm == null) {
			return tm;
		}

		if (name == null) {
			return tm;
		} else {
			return (TreeMap) tm.get(name);
		}
	}

	/*
	 * public BufferedReader loadFromServer (String url) throws Exception {
	 * String newurl = "http:" + url.substring(3); URL u = new URL(newurl);
	 * /rsUtil.messageInfo("New URL = " + newurl); String host = u.getHost();
	 * int port = u.getPort(); String ui = ""; //u.getUserInfo(); String user =
	 * ""; String pass = ""; adminPort ap = new adminPort (host, port, ui);
	 * StringBuffer sb = ap.getConfigurationStr(); /progress.setCount
	 * (sb.length()); return new BufferedReader (new StringReader
	 * (sb.toString())); }
	 */
	/**
	 * Gets the key attribute of the FileConfig object
	 * 
	 * @param map
	 *            the root configuration object of the configuration hierarchy
	 * @param key
	 *            a compound key, which identifies the searched configuration
	 *            object in the hierarchy
	 * @return The key value
	 */
	public Object getKey(TreeMap map, String key) {
		Object result = map;
		StringTokenizer st = new StringTokenizer(key, "/");

		if (map == null) {
			return map;
		}

		// System.out.println ("getKey: " + key);
		while (st.hasMoreTokens()) {
			String next = st.nextToken();
			// System.out.println ("-- next: " + next);
			result = ((TreeMap) result).get(next);
			if (result == null) {
				return null;
			}
		}

		// System.out.println ("getKey: " + result.toString());
		return result;
	}

	/**
	 * Parses the currently set configuration file. The configurations are
	 * accumulated in the state of the FileConfig object.
	 * 
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration file
	 */
	public TreeMap loadConfig() throws Exception {
		return loadConfig(path);
	}

	/**
	 * Opens the specified configuration file for reading. If the file is
	 * encrypted, the password must be already set.
	 * 
	 * @param path
	 *            file system path or URL for the configuration file; if the
	 *            parameter is set to " <stdin>", the configuration will be read
	 *            from the standard input
	 * @exception Exception
	 *                problem while opening the configuration file for reading
	 */
	public void openInputFile(String path) throws Exception {
		Trace.entrymin(this, "openInputFile", path);

		if (path.equals("<stdin>")) {
			input = new BufferedReader(new InputStreamReader(System.in));
			Trace.exitmax(this, "openInputFile");
			return;
		}

		try {
			URL u = new URL(path);
			input = new BufferedReader(new InputStreamReader(u.openStream()));
		} catch (MalformedURLException e) {
			input = new BufferedReader(new FileReader(path));
		}

		if (password != null) {
			input = new com.ibm.di.security.EncryptedReader(
					new FileInputStream(path));
			SecurityCrypto key = new SecurityCrypto(password);
			((EncryptedReader) input).setKey(key);
			((EncryptedReader) input).prefetch();
		}
		Trace.exitmin(this, "openInputFile");
	}

	/**
	 * Parses the specified configuration file. Configurations are accumulated
	 * in the state of the FileConfig object.
	 * 
	 * @param urlPath
	 *            file system path or URL for the configuration file if the
	 *            parameter is set to " <stdin>", the configuration will be read
	 *            from the standard input
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration file
	 */
	public TreeMap loadConfig(String urlPath) throws Exception {
		Trace.entrymin(this, "loadConfig", urlPath);
		// System.out.println ("Load config: "+ urlPath);
		// TreeMap ht;
		// progress = new guiLoadProgress(urlPath, 9999);
		try {
			this.path = urlPath;
			tables = loadConfig2(urlPath);
			// progress.dispose ();
		} catch (Exception e) {
			// progress.dispose();
			throw e;
		}
		Trace.exitmin(this, "loadConfig", tables);
		return tables;
	}

	/**
	 * Parses the specified configuration input stream. Configurations are
	 * accumulated in the state of the FileConfig object.
	 * 
	 * @param input
	 *            the configuration input stream
	 * 
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration input stream
	 */
	public TreeMap loadConfig(BufferedReader input) throws Exception {
		Trace.entrymid(this, "loadConfig", input);
		this.input = input;
		try {
			Trace.exitmid(this, "loadConfig");
			return loadConfig3();
		} catch (Exception error) {
			throw new Exception(res.getString(
					"parsing.error.line.last.exeption", new Object[] {
							Integer.valueOf(linenumber), lastlineread,
							error.toString() }));
		}
	}

	/**
	 * Parses the specified configuration data. Configurations are accumulated
	 * in the state of the FileConfig object.
	 * 
	 * @param data
	 *            the configuration data
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration data
	 */
	public TreeMap loadConfig(byte[] data) throws Exception {
		Trace.entrymid(this, "loadConfig", data);
		ByteArrayInputStream ba = new ByteArrayInputStream(data);
		input = new BufferedReader(new InputStreamReader(ba));
		try {
			Trace.exitmid(this, "loadConfig", data);
			return loadConfig3();
		} catch (com.ibm.di.exceptions.PasswordException pwdexp) {
			throw pwdexp;
		} catch (Exception error) {
			throw new Exception(res.getString(
					"parsing.error.line.last.exeption", new Object[] {
							Integer.valueOf(linenumber), lastlineread,
							error.toString() }));
		}
	}

	/**
	 * Parses the specified configuration data. Configurations are accumulated
	 * in the state of the FileConfig object.
	 * 
	 * @param urlPath
	 *            file system path or URL for the configuration file; if the
	 *            parameter is set to " <stdin>", the configuration will be read
	 *            from the standard input
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration file
	 */
	public TreeMap loadConfig2(String urlPath) throws Exception {
		Trace.entrymid(this, "loadConfig2", urlPath);
		openInputFile(urlPath);
		try {
			Trace.exitmid(this, "loadConfig2");
			return loadConfig3();
		} catch (com.ibm.di.exceptions.PasswordException pwdexp) {
			throw pwdexp;
		} catch (Exception error) {
			throw new Exception(res.getString(
					"parsing.error.urlpath.line.last.exeption", new Object[] {
							urlPath, Integer.valueOf(linenumber), lastlineread,
							error.toString() }));
		}
	}

	/**
	 * Parses the configuration stream, which is currently open for reading.
	 * Configurations are accumulated in the state of the FileConfig object.
	 * 
	 * @return the accumulated configurations in the state of the FileConfig
	 *         object
	 * @exception Exception
	 *                problem while parsing the configuration file
	 */
	public TreeMap loadConfig3() throws Exception {
		Trace.entrymin(this, "loadConfig3");
		String line;
		String type;
		String name;
		TreeMap config;

		// openInputFile (urlPath);
		linenumber = 0;
		boolean doInclude = true;

		while ((line = getLine()) != null) {

			//
			if (linenumber == 1 && line.startsWith("VERSION: ")) {
				this.version = line;
				linenumber++;
				line = getLine();
				if (line == null)
					break;
			}

			if (line.startsWith("{AS ENCRYPTED}")) {
				input.close();
				throw new com.ibm.di.exceptions.PasswordException(res
						.getString("encrypted.file.and.no.password"));
			}

			if (line.startsWith("[")) {

				line = line.substring(1).trim();
				int ix = line.indexOf(" ");
				if (ix < 0) {
					throw new Exception(res.getString("unexpected.line.num",
							line));
				}
				type = line.substring(0, ix);
				name = line.substring(ix + 1);
				if (name.endsWith("]")) {
					name = name.substring(0, name.length() - 1);
				}

				if (!type.equals("include") && doInclude) {
					doInclude = false;
					includeFiles(true);
				}

				config = (TreeMap) tables.get(type);
				if (config == null) {
					config = new TreeMap();
					tables.put(type, config);
				}

				if (config.get(name) != null && this.dontOverwriteConfig) {
					// System.out.println( "Dont overwrite: " + name + ",
					// source=" + path );
					while ((line = getLine()) != null) {
						if (line.trim().equals("[end]"))
							break;
					}
					continue;
				}
				/*
				 * if (config.get(name) != null && this.dontOverwriteConfig) {
				 * if (rsUtil.confirm ("Overwrite " + type + ": " + name) != 0)
				 * continue; }
				 */
				if (impvec != null) {
					impvec.add(type + "/" + name);
				}

				addSection(config, name);
			}
		}

		includeFiles(false);

		input.close();
		Trace.exitmin(this, "loadConfig3");
		return tables;
	}

	/**
	 * Parses the configurations from the include files, which are currently
	 * accumulated for reading. Configurations are accumulated in the state of
	 * the FileConfig object.
	 * 
	 * @param first
	 *            whether this is the first include file for the currently
	 *            parsed configuration stream
	 * @exception Exception
	 *                problem while parsing the configurations
	 */
	public void includeFiles(boolean first) throws Exception {

		// System.err.println ("includeFiles: " + first );
		TreeMap tm = (TreeMap) tables.get("include");
		if (tm == null) {
			// System.err.println ( "No include files");
			return;
		}

		tables.remove("include");

		for (Iterator i = tm.entrySet().iterator(); i.hasNext();) {
			java.util.Map.Entry mapEntry = (java.util.Map.Entry) i.next();
			String key = (String) mapEntry.getKey();
			TreeMap include = (TreeMap) mapEntry.getValue();
			String sFirst = (String) include.get("includeFirst");
			if (sFirst == null) {
				sFirst = "false";
			}
			String overwrite = (String) include.get("overwriteCurrent");
			if (overwrite == null) {
				overwrite = "false";
			}

			if (first && sFirst.equals("true")) {
				includeFile((String) include.get("URL"), overwrite
						.equals("true"));
			}
			if (!first && !sFirst.equals("true")) {
				includeFile((String) include.get("URL"), overwrite
						.equals("true"));
			}
		}

		tables.put("include", tm);
	}

	/**
	 * Parses the configurations from the specified include file. Configurations
	 * are accumulated in the state of the FileConfig object.
	 * 
	 * @param path
	 *            the include configuration file
	 * @param overwrite
	 *            whether to overwrite existing configurations
	 * @exception Exception
	 *                problem while parsing the configurations
	 */
	public void includeFile(String path, boolean overwrite) throws Exception {
		File f = new File(path);
		String fp = f.getAbsolutePath().toLowerCase(Locale.ENGLISH);
		if (includedFiles.contains(fp)) {
			// System.out.println( "Already included: " + path );
			// System.out.flush();
			return;
		}

		includedFiles.addElement(fp);
		// System.out.println( "Begin include: " + path );
		// System.out.flush();

		boolean save = this.dontOverwriteConfig;
		boolean saveInclude = this.isInclude;
		String savePath = this.path;
		int saveLine = linenumber;
		BufferedReader saveInput = this.input;
		String savePassword = this.password;
		this.password = null;
		Exception error = null;

		isInclude = true;
		this.dontOverwriteConfig = !overwrite;

		try {
			try {
				loadConfig(path);
			} catch (com.ibm.di.exceptions.PasswordException pe) {
				this.password = savePassword;
				loadConfig(path);
			}
		} catch (Exception err) {
			if (!fireError(err)) {
				error = err;
			} else {
				error = null;
			}
		}

		// System.err.println ( "Reset include: " + savePath );

		this.dontOverwriteConfig = save;
		this.path = savePath;
		this.input = saveInput;
		this.isInclude = saveInclude;
		this.linenumber = saveLine;
		this.password = savePassword;
		if (error != null) {
			throw error;
		}
	}

	/**
	 * Parses a configuration section (TreeMap) from the configuration stream
	 * which is currently open for reading. The read configuration section is
	 * added in the specified TreeMap under the specified key name.
	 * 
	 * @param config
	 *            the TreeMap where the parsed section will be added
	 * @param name
	 *            key name for the parsed section
	 * @exception IOException
	 *                problem while parsing the configurations
	 */
	public void addSection(TreeMap config, String name) throws IOException {
		TreeMap m = new TreeMap();
		String str;

		if (isInclude && tables.containsValue(config)) {
			if (name.equals("java") || name.equals("userFunctions")
					|| name.equals("state")) {
				// System.err.println ("Ignore include section: " + name );
				while ((str = getLine()) != null) {
					if (str.compareToIgnoreCase("[end]") == 0) {
						return;
					}
				}
				return;
			}
			m.put(INCLUDED_COMPONENT, path);

		}

		// System.out.println ("begin section " + name);
		while ((str = getLine()) != null) {

			if (str.startsWith("#")) {
				continue;
			}

			str = str.trim();
			if (str.length() < 1) {
				continue;
			}

			// System.out.println (" >> '" + str + "'");
			if (str.compareToIgnoreCase("[end]") == 0) {
				break;
			}

			if (str.endsWith("{")) {
				String map = str.substring(0, str.indexOf("{") - 1);
				// System.out.println ("Add TreeMap at: " + map);
				addSection(m, map);
				continue;
			}

			if (str.compareTo("}") == 0) {
				break;
			}

			if (str.endsWith("[")) {
				String map = str.substring(0, str.indexOf("[") - 1);
				// System.out.println ("Add Vector at: " + map);
				addVector(m, map);
				continue;
			}

			int i = str.indexOf(":");
			if (i > 0) {
				m.put(str.substring(0, i), com.ibm.di.util.StringUtils
						.fromPrint(str.substring(i + 1)));
			} else {
				addAttribute(m, str);
			}
		}
		// System.out.println ("end section " + name);
		config.put(name, m);
	}

	/**
	 * Reads a configuration Vector from the configuration stream which is
	 * currenlty open for reading. The read Vector with configuration objects is
	 * added to the specified TreeMap under the specified key name.
	 * 
	 * @param h
	 *            the TreeMap configuration object, where the parsed Vector with
	 *            configurations will be added
	 * @param name
	 *            the key name for the configurations Vector
	 * @exception IOException
	 *                problem while parsing the configurations
	 */
	public void addVector(TreeMap h, String name) throws IOException {
		Vector v = new Vector();
		String str;

		while ((str = getLine()) != null) {

			str = str.trim();
			if (str.length() < 1) {
				continue;
			}

			if (str.compareTo("]") == 0) {
				break;
			}

			if (str.endsWith("{")) {
				TreeMap c = new TreeMap();
				if (str.equals("{")) {
					addSection(c, "%DUMMY%");
					v.add(c.get("%DUMMY%"));
					continue;
				} else {
					addSection(c, str.substring(0, str.lastIndexOf(" ")));
					v.add(c);
					continue;
				}
			}

			v.add(com.ibm.di.util.StringUtils.fromPrint(str));
		}

		h.put(name, v);
	}

	/**
	 * Adds an empty TreeMap to the specified TreeMap under the specified key
	 * name.
	 * 
	 * @param h
	 *            the TreeMap where where the addition will happen
	 * @param name
	 *            the key name
	 * @exception IOException
	 *                problem
	 */
	public void addMap(TreeMap h, String name) throws IOException {
		TreeMap map = new TreeMap();
		h.put(name, map);
	}

	/**
	 * Reads attribute values from the configuration stream which is currently
	 * open for reading. The read values are placed in a Vector and added to the
	 * specified TreeMap under the specified key name (the name of the
	 * attribute).
	 * 
	 * @param h
	 *            the TreeMap where the attrbute will be added
	 * @param name
	 *            the name of the attribute
	 * @exception IOException
	 *                problem while parsing the configurations
	 */
	public void addAttribute(TreeMap h, String name) throws IOException {
		String line;
		String key;
		String val;
		String lastname = "";
		Vector tm = new Vector();

		// System.out.println ("Add attribute: " + name);

		while ((line = getLine()) != null) {
			line = line.trim();
			// System.out.println (" process line: " + line);
			if (line.length() == 0) {
				break;
			}

			tm.add(com.ibm.di.util.StringUtils.fromPrint(line));
		}

		h.put(name, tm);
	}

	/**
	 * Writes the internally accumulated configurations to the currently set
	 * configuration file.
	 * 
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void saveConfig() throws Exception {
		Trace.entrymid(this, "saveConfig");
		String tmp = getConfigPath() + ".tmp";
		String oldtmp = getConfigPath() + ".sav";

		File fOld = new File(getConfigPath());
		File fOldtmp = new File(oldtmp);
		File fTmp = new File(tmp);

		saveConfig(new FileOutputStream(tmp));

		// Move current config to old tmp
		FileUtils.delete(fOldtmp);
		FileUtils.renameTo(fOld, fOldtmp);

		// Move new file to current config name
		FileUtils.renameTo(fTmp, fOld);

		// Remove old tmp
		FileUtils.delete(fOldtmp);
		Trace.exitmid(this, "saveConfig");
	}

	/**
	 * Saves the configuration to an output stream. An EOF marker is also
	 * written. This method is used by the guiRunner in the miadmin tool.
	 * 
	 * @param output
	 *            the output stream
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveConfig(OutputStream output) throws IOException {
		Trace.entrymid(this, "saveConfig", output);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(output));

		out.write(version);
		out.newLine();

		saveConfig(out, C_INCLUDEFILES, (TreeMap) tables.get(C_INCLUDEFILES));
		for (Iterator e = tables.keySet().iterator(); e.hasNext();) {
			String type = (String) e.next();
			if (!type.equals(C_INCLUDEFILES)) {
				saveConfig(out, type, (TreeMap) tables.get(type));
			}
		}

		out.write("[EOF]");
		out.newLine();
		out.close();
		Trace.exitmid(this, "saveConfig");
	}

	/**
	 * Overwrites the specified file with the configurations from the specified
	 * TreeMap.
	 * 
	 * @param config
	 *            the configuration TreeMap
	 * @param path
	 *            the file path
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveConfig(TreeMap config, String path) throws IOException {
		Trace.entrymid(this, "saveConfig", config, path);
		BufferedWriter out;

		out = new BufferedWriter(new FileWriter(path));
		/*
		 * for (Enumeration e = config.keys(); e.hasMoreElements(); ) { String
		 * type = (String) e.nextElement(); saveConfig (out, type, (TreeMap)
		 * config.get(type)); }
		 */

		out.write(version);
		out.newLine();

		if (tables.get(C_INCLUDEFILES) != null) {
			saveConfig(out, C_INCLUDEFILES, (TreeMap) tables
					.get(C_INCLUDEFILES));
		}

		for (Iterator e = config.entrySet().iterator(); e.hasNext();) {
			Map.Entry mpEntry = (Map.Entry) e.next();
			String type = (String) mpEntry.getKey();
			if (!type.equals(C_INCLUDEFILES)) {
				saveConfig(out, type, (TreeMap) mpEntry.getValue());
			}
		}
		out.close();
		Trace.exitmid(this, "saveConfig");
	}

	/**
	 * Writes the internally accumulated configurations to the specified output
	 * stream.
	 * 
	 * @param out
	 *            the output stream
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveConfig(BufferedWriter out) throws IOException {
		Trace.entrymid(this, "saveConfig", out);
		out.write(version);
		out.newLine();

		saveConfig(out, C_INCLUDEFILES, (TreeMap) tables.get(C_INCLUDEFILES));
		for (Iterator e = tables.keySet().iterator(); e.hasNext();) {
			String type = (String) e.next();
			if (!type.equals(C_INCLUDEFILES)) {
				saveConfig(out, type, (TreeMap) tables.get(type));
			}
		}
		out.flush();
		Trace.exitmid(this, "saveConfig");
	}

	/**
	 * Overwrites the currently set configuration file with the configurations
	 * from the specified TreeMap.
	 * 
	 * @param config
	 *            the configuration TreeMap
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveConfig(TreeMap config) throws IOException {
		Trace.entrymid(this, "saveConfig");
		BufferedWriter out = new BufferedWriter(new FileWriter(path));

		out.write(version);
		out.newLine();

		saveConfig(out, C_INCLUDEFILES, (TreeMap) tables.get(C_INCLUDEFILES));
		for (Iterator e = config.entrySet().iterator(); e.hasNext();) {
			Map.Entry mpEntry = (Map.Entry) e.next();
			String type = (String) mpEntry.getKey();
			if (!type.equals(C_INCLUDEFILES)) {
				saveConfig(out, type, (TreeMap) mpEntry.getValue());
			}
		}
		out.close();
		Trace.exitmid(this, "saveConfig");
	}

	/**
	 * Writes the configurations from the specified TreeMap into the specified
	 * output stream. The specified TreeMap is supposed be a top-most container
	 * which hold configurations for a single category (AssemblyLines,
	 * Connectors, Forms,...).
	 * 
	 * @param out
	 *            the output stream
	 * @param type
	 *            the name of the category
	 * @param cf
	 *            the configurations TreeMap
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveConfig(BufferedWriter out, String type, TreeMap cf)
			throws IOException {

		if (cf == null)
			return;

		// Special handling for new style cfg file
		if (type.equals(DEFAULT_PROPERTY_FOLDER)
				|| type.equals(DEFAULT_LIBRARY_FOLDER)
				|| type.equals(DEFAULT_EXTPROP_FOLDER)) {
			// Write component header
			out.write("[Java " + type + "]");
			out.newLine();
			saveArray(out, "\t", cf);
			out.write("[end]");
			out.newLine();
			out.newLine();
			return;
		}

		Set s = cf.entrySet();

		for (Iterator e = s.iterator(); e.hasNext();) {
			Map.Entry mpEntry = (Map.Entry) e.next();
			String name = (String) mpEntry.getKey();
			Object obj = mpEntry.getValue();
			if (!(obj instanceof TreeMap)) {
				// System.out.println ( "SaveConfig - unexpected: type: " + type
				// + ", key=" + name + ", class=" + obj.getClass().getName() + "
				// ( " + obj + ")" );
				continue;
			}
			TreeMap tm = (TreeMap) obj;

			// Don't save null items
			if (tm == null)
				continue;

			// Don't save included components
			if (tm.get(INCLUDED_COMPONENT) != null)
				continue;

			// Write component header
			out.write("[" + type + " " + name + "]");
			out.newLine();
			saveArray(out, "\t", tm);
			out.write("[end]");
			out.newLine();
			out.newLine();
		}
	}

	/**
	 * Writes the specified Vector with configurations to the specified output
	 * stream.
	 * 
	 * @param out
	 *            the output stream
	 * @param prefix
	 *            indentation prefix
	 * @param v
	 *            the configuration Vector
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveVector(BufferedWriter out, String prefix, Vector v)
			throws IOException {
		for (int i = 0; i < v.size(); i++) {
			Object o = v.get(i);
			if (o instanceof TreeMap) {
				out.write(prefix + "{");
				out.newLine();
				saveArray(out, prefix + "\t", (TreeMap) o);
				out.write(prefix + "}");
				out.newLine();
			} else {
				out.write(prefix
						+ com.ibm.di.util.StringUtils.toPrint(o.toString()));
				out.newLine();
			}
		}
	}

	/**
	 * Writes the contents of the specified configuration TreeMap into the
	 * specified output stream. The specified TreeMap is not supposed to be a
	 * top-most container for configurations from a category (AssemblyLines,
	 * Connectors, Forms,...).
	 * 
	 * @param out
	 *            the output stream
	 * @param prefix
	 *            indentation prefix
	 * @param cf
	 *            the configuration TreeMap
	 * @exception IOException
	 *                problem while writing the configurations
	 */
	public void saveArray(BufferedWriter out, String prefix, TreeMap cf)
			throws IOException {
		Set s = cf.entrySet();

		for (Iterator e = s.iterator(); e.hasNext();) {
			Map.Entry mpEntry = (Map.Entry) e.next();
			String name = (String) mpEntry.getKey();
			Object value = mpEntry.getValue();

			if (value instanceof String) {
				out.write(prefix + name + ":"
						+ com.ibm.di.util.StringUtils.toPrint((String) value));
				out.newLine();
			}

			if (value instanceof Vector) {
				out.write(prefix + name + " [");
				out.newLine();
				saveVector(out, prefix + "\t", (Vector) value);
				out.write(prefix + "]");
				out.newLine();
			}

			if (value instanceof TreeMap && cf.get("%%EXTERNAL_PATH%%") == null) {
				out.write(prefix + name + " {");
				out.newLine();
				saveArray(out, prefix + "\t", (TreeMap) value);
				out.write(prefix + "}");
				out.newLine();

			}
		}
	}

	/**
	 * A NOOP method.
	 * 
	 * @exception IOException
	 *                problem
	 */
	public void loadEncrypted() throws IOException {
	}

	/**
	 * The method tells whether a specified error should be raised or is
	 * internally handled.
	 * 
	 * @param error
	 *            the error object
	 * @return true if the error should not be raised
	 * @exception Exception
	 *                problem
	 */
	public boolean fireError(Exception error) throws Exception {
		if (errorListener == null) {
			return false;
		}

		return errorListener.handleError(this, error);
	}

	/**
	 * Removes included components from our tables.
	 */
	public void removeIncludedComponents() {
		for (Iterator e = tables.keySet().iterator(); e.hasNext();) {
			Object name = e.next();
			// System.out.println ( "REMOVE INCLUDED IN: " + name );
			if (name.equals(DEFAULT_LIBRARY_FOLDER)
					|| name.equals(DEFAULT_PROPERTY_FOLDER)
					|| name.equals(DEFAULT_EXTPROP_FOLDER))
				continue;

			TreeMap tm = (TreeMap) tables.get(name);

			if (tm == null) {
				continue;
			}

			for (Iterator e2 = tm.keySet().iterator(); e2.hasNext();) {
				Object map = tm.get(e2.next());

				if (map instanceof TreeMap
						&& ((TreeMap) map).get(INCLUDED_COMPONENT) != null)
					e2.remove();
			}
		}
	}
}
