/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.server.ResourceHash;

/**
 * This is a function component that wraps parser operations. The configured
 * parser is used in read/write mode depending on the type of object passed to
 * this FC. If the passed type is an com.ibm.di.entry.Entry object, the write
 * operation is used, otherwise the function will try to coerce the input param
 * to an input stream in order to use the parser read operation.
 * 
 * system.getFC("ibmdi.ParserFC").perform (new Entry()); // Returns the encoded
 * string/byte array system.getFC("ibmdi.ParserFC").perform
 * (file|string|inputstream|reader); // Returns the parsed entry
 */
public class ParserFC extends Function {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "parserfc";

	/**
	 * Parser. May be read by different threads.
	 */
	private volatile ParserInterface parser;

	/**
	 * Default attributes of the parser.
	 */
	private String defaultAttribute;

	/**
	 * Initialization status flag.
	 */
	private boolean initParser;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Default attribute parameter
	 */
	private final static String DEFAULT_ATTRIBUTE = "defaultAttribute";

	/**
	 * Parameter specifying if string should be returned
	 */
	private final static String RETURN_STRING = "returnString";

	/**
	 * Defines if the mode is read or write
	 */
	private final static String MODE = "mode";

	/**
	 * If this method is called with an object of type java.lang.String,
	 * java.io.File, java.io.InputStream or java.io.Reader the configured parser
	 * is provided that object as input and the returned value is an Entry
	 * object resulting from the parsing. If this method is called with an Entry
	 * object, the parser is used to generate a byte stream that is returned
	 * either as a byte array or java.lang.String object. The latter depends on
	 * the configuration switch "returnString" setting.
	 * 
	 * Depending on the value of the "mode" configuration parameter this method
	 * calls either readEntry or writeEntry method of the Parser.
	 * 
	 * @param obj
	 *            the input object for the function
	 * @return the output object for the function
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	public Object perform(Object obj) throws Exception {

		if (!isParserLoaded())
			loadParser();

		if (getConfiguration().getIntegerParameter(MODE, 0) == 0) {
			return readEntry(obj);
		} else {
			return writeEntry(obj);
		}
	}

	/**
	 * Returns the string or byte array from the parser write operation
	 * 
	 * @param obj
	 *            The entry object to encode
	 * @return The string or byte array value resulting from the operation
	 * @exception Exception
	 *                Any exception thrown by the parser
	 */
	public Object writeEntry(Object obj) throws Exception {
		Entry entry = null;
		if (obj instanceof Entry)
			entry = (Entry) obj;
		else if (obj instanceof Attribute) {
			entry = new Entry();
			entry.setAttribute(defaultAttribute, (Attribute) obj);
		} else {
			entry = new Entry();
			entry.setAttribute(defaultAttribute, obj);
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		parser.setOutputStream(bos);
		parser.initParser();
		initParser = false;
		parser.writeEntry(entry);
		parser.closeParser();
		if (getConfiguration().getBooleanParameter(RETURN_STRING, true)) {
			return bos.toString((String) getConfiguration().getParameter(
					"characterSet", "UTF-8"));
		} else {
			return bos.toByteArray();
		}
	}

	/**
	 * Returns the entry from the parser read operation
	 * 
	 * @param param
	 *            The parser input object
	 * 
	 * @return The entry resulting from the parser read operation
	 * @exception Exception
	 *                Any exception thrown by the parser
	 */
	public Entry readEntry(Object param) throws Exception {
		Object obj = param;

		if (obj instanceof Entry) {
			Entry e = (Entry) obj;
			if (defaultAttribute != null
					&& e.getAttribute(defaultAttribute) != null)
				obj = e.getObject(defaultAttribute);
			else {
				throw new Exception(sResHash.getString(
						"FC.PARSER.NO.DEFAULTATTR", defaultAttribute));
			}
		}

		if (obj instanceof File) {
			parser.setInputStream(new FileInputStream((File) obj));
		} else if (obj instanceof String) {
			parser.setInputStream(new StringReader((String) obj));
		} else if (obj instanceof InputStream) {
			parser.setInputStream((InputStream) obj);
		} else if (obj instanceof Reader) {
			parser.setInputStream((Reader) obj);
		} else if (obj != null) {
			// If the obj was null, we would expect to read next from
			// the parsed data object. But its not, so we have to throw an
			// exception.
			throw new Exception(sResHash.getString(
					"FC.PARSER.UNKNOWN.OBJECTTYPE", obj.getClass().getName()));
		}

		// If we reset the input stream then we must call initParser again
		if (initParser || obj != null) {
			parser.initParser();
			initParser = false;
		}

		return parser.readEntry();
	}

	/**
	 * This method modifies the schema in the provided configuration. The intent
	 * is to allow the FC to provide a schema definition dynamically based on a
	 * given configuration.
	 * 
	 * @param config
	 *            {@link FunctionConfig}
	 * @return boolean
	 * @throws Exception
	 *             : never
	 */
	public boolean updateSchema(FunctionConfig config) throws Exception {
		BaseConfiguration bc = config.getFunctionConfig();
		defaultAttribute = bc.getStringParameter(DEFAULT_ATTRIBUTE);

		if (bc.getIntegerParameter(MODE, 0) == 0) {
			// Read mode
			SchemaConfig schema = config.getSchema(false);
			if (schema.getItem(defaultAttribute) == null) {
				SchemaItemConfig sic = schema.newItem(defaultAttribute);
				sic.setPresenceFlag("Required");
				sic.setJavaClass("String|File|InputStream|Reader");
			}
		} else {
			// Write mode
			SchemaConfig schema = config.getSchema(true);
			SchemaItemConfig sic = schema.getItem(defaultAttribute);
			if (sic == null)
				sic = schema.newItem(defaultAttribute);
			sic.setPresenceFlag("Always");
			if (bc.getBooleanParameter(RETURN_STRING, true))
				sic.setJavaClass("String");
			else
				sic.setJavaClass("byte[]");
		}

		return true;
	}

	/**
	 * This method queries the schema for this ParserFC.
	 * 
	 * @param o
	 *            Boolean.TRUE for inputschema, Boolean.FALSE for outputschema
	 * @return a Vector of Entry objects or null if no schema found
	 * @throws Exception
	 *             : if the parser fails to load
	 */
	public Object querySchema(Object o) throws Exception {
		if (getConfiguration() == null || !(o instanceof Boolean))
			return null;

		Boolean input = (Boolean) o;
		boolean read = getConfiguration().getIntegerParameter(MODE, 0) == 0;
		if (input == read) {
			// Return parser's schema
			if (!isParserLoaded())
				loadParser();
			if (initParser)
				parser.initParser();
			return parser.querySchema(null);
		}

		// Only one entry in the schema, the default attribute
		Entry e = new Entry();
		e.setAttribute("name", getConfiguration().getStringParameter(
				DEFAULT_ATTRIBUTE));
		e.setAttribute("required", "true");
		if (read)
			e.setAttribute("syntax", "String|File|InputStream|Reader");
		else if (getConfiguration().getBooleanParameter(RETURN_STRING, true))
			e.setAttribute("syntax", "String");
		else
			e.setAttribute("syntax", "byte[]");

		Vector<Entry> v = new Vector<Entry>();
		v.add(e);
		return v;
	}

	/**
	 * This method provides access to the {@link ParserInterface} implementation
	 * used internally.
	 * 
	 * @return the parser this FC uses for handling user requests or
	 *         <code>null</code> if the parser have not been loaded yet.
	 */
	public ParserInterface getParser() {
		return parser;
	}

	/**
	 * Checks whether the internal parser is loaded. This parser is loaded on
	 * demand when the {@link #perform(Object)} method is called. To load/reload
	 * the parser manually use one of the {@link #loadParser()} methods.
	 * 
	 * @return true if a parser is already loaded, false otherwise.
	 */
	private boolean isParserLoaded() {
		return parser != null;
	}

	/**
	 * Loads/Reloads a Parser. If there is a loaded parser already then that
	 * parser is unloaded and a new one is loaded. While loading the Parser this
	 * method checks for the configuration parameter "defaultAttribute", which
	 * sets the name of the attribute which is used for input/output during the
	 * {@link #perform(Object)} method.
	 * 
	 * @throws Exception
	 *             if a problem either when closing the old parser or when
	 *             loading the new parser occurs.
	 * 
	 */
	private void loadParser() throws Exception {
		closeParser();

		defaultAttribute = getConfiguration().getStringParameter(
				DEFAULT_ATTRIBUTE);

		FunctionConfig fc = (FunctionConfig) getConfiguration().getParent();
		parser = SystemFunctions.loadParser(fc.getParserConfig());
		parser.setContext(this);
		initParser = true;
	}

	/**
	 * Version information.
	 * 
	 * @return version information.
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

	/**
	 * Closes the underlying parser if it is loaded. Null the parser and the
	 * parserName fields.
	 * 
	 * @throws Exception
	 *             if an error while closing occurs.
	 */
	private void closeParser() throws Exception {
		if (isParserLoaded()) {
			parser.closeParser();
			parser = null;
		}
	}

	/**
	 * Closes the parser
	 */
	@Override
	public void terminate() throws Exception {
		super.terminate();
		closeParser();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDebug(boolean debug) {
		super.setDebug(debug);
		// must be thread-safe - at least avoid race conditions
		ParserInterface p = parser;
		if (p != null) {
			p.setDebug(getDebug());
		}
	}
}
