/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ParserImpl.java
//
//
//
package com.ibm.di.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.fc.Function;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.security.EncryptedReader;
import com.ibm.di.security.EncryptedWriter;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.Trace;
import com.ibm.di.server.VersionInfoInterface;

/**
 * The ParserImpl class provides the base class for parser implementations. This
 * class provides common methods and properties that apply to all parsers.
 * <p>
 */

public abstract class ParserImpl implements ParserInterface,
		VersionInfoInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The parser's configuration.
	 */
	protected ParserConfig myConfiguration;

	/**
	 * <code>True</code> if debug is enabled; <code>false</code> otherwise.
	 * May be accessed by different threads.
	 */
	protected volatile boolean _debug = false;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * Uses the input stream to read data.
	 */
	private BufferedReader in;

	/**
	 * Uses the output stream to wite data.
	 */
	private BufferedWriter out;

	/**
	 * {@link InputStream} to read from.
	 */
	private InputStream is;

	/**
	 * {@link OutputStream} to write data.
	 */
	private OutputStream os;

	/**
	 * User defined context.
	 */
	private Object context;

	/**
	 * Chained parser (future implementation).
	 */
	private ParserInterface chainedParser;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Default constructor.
	 */
	public ParserImpl() {
		Trace.entrymid(this, "ParserImpl");
		Trace.exitmid(this, "ParserImpl");
	}

	/**
	 * Return the context in which this parser is running (e.g. AssemblyLine
	 * object). The context is called upon when the parser needs to access
	 * resources outside it's scope. The logmsg method is one such build-in
	 * method but sub-classes may use the context object to communicate with the
	 * "housing" object. This object is typically a connector. If the object
	 * using this class is not setting the context then the return value is
	 * null.
	 * 
	 * @return The object housing this parser
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * Set the context in which the parser runs. This method is optionally
	 * called by any object that wants to make itself visible to the parser.
	 * 
	 * @param context
	 *            The context object
	 */
	public void setContext(Object context) {
		this.context = context;
	}

	/**
	 * Set the chained parser (future implementation).
	 * 
	 * @param parser
	 *            The chained parser
	 */
	public void setParser(ParserInterface parser) {
		chainedParser = parser;
	}

	/**
	 * Return the chained parser (future implementation).
	 * 
	 * @return The chained parser
	 */
	public ParserInterface getParser() {
		return chainedParser;
	}

	/**
	 * Set parser configuration value.
	 * 
	 * @param param
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 */
	public void setParam(String param, String value) {
		if (myConfiguration == null)
			myConfiguration = new com.ibm.di.config.base.ParserConfigImpl();
		myConfiguration.setParameter(param, value);
	}

	/**
	 * Return configuration value.
	 * 
	 * @param param
	 *            The name of the parameter to return
	 * @return The value associated with the parameter or null.
	 */
	public String getParam(String param) {
		// System.out.println ( "getParam: " + param + " = " +
		// myConfiguration.get (param));
		// String value = (String) myConfiguration.get(param);
		// if (value.compareToIgnoreCase("$connector.selectedAttributes") == 0)
		// {
		// }
		if (myConfiguration == null)
			return null;
		else
			return myConfiguration.getStringParameter(param);
	}

	/**
	 * Set the parser configuration. This method is called by instantiating
	 * classes to provide the parser with a configuration object.
	 * 
	 * @param config
	 *            The configuration object
	 */
	public void setConfiguration(ParserConfig config) {
		myConfiguration = config;
		_debug = config.getDebug(false);
	}

	/**
	 * Set the parser input stream. This method sets the input stream object
	 * from which the parser will read it's data. If the configuration has an
	 * <i>encryptionKey</i> parameter set, then this method will make an
	 * instance of the EncryptedReader to decrypt the underlying input stream.
	 * If no encryptionKey is specified then an instance of
	 * java.io.BufferedReader is used to provide buffered access to the input
	 * stream. Also, if the <i>characterSet</i> configuration parameter is set
	 * the input stream is read/decoded according the character set specified by
	 * that parameter.
	 * 
	 * @param is
	 *            The InputStream object (e.g. FileInputStream,
	 *            SocketInputStream etc. ..)
	 */
	public void setInputStream(InputStream is) {
		Trace.entrymax(this, "setInputStream", is);
		this.is = is;

		if (getParam("encryptionKey") != null) {
			if (is != null) {
				in = new EncryptedReader(is);
				try {
					((EncryptedReader) in).useKey(getParam("encryptionKey"));
				} catch (Exception e) {
					System.err.println(sResHash
							.getString("MISERVER.PARSERIMPL.ENCRYPTEDREADER", e
									.toString()));
					in = null;
				}
			} else {
				in = null;
			}

			return;
		}

		String charset = getParam("characterSet");

		if (is != null) {
			if (charset != null && charset.length() > 0) {
				try {
					in = new BufferedReader(new InputStreamReader(is, charset));
					return;
				} catch (UnsupportedEncodingException uee) {
					logmsg(sResHash.getString("pareser.use.default.encoding",
							uee.toString()));
				}
			}
			in = new BufferedReader(new InputStreamReader(is));
		} else {
			in = null;
		}
		Trace.exitmax(this, "setInputStream");
	}

	/**
	 * Set the parser output stream. This method sets the output stream object
	 * to which the parser will write it's data. If the configuration has an
	 * <i>encryptionKey</i> parameter set, then this method will make an
	 * instance of the EncryptedWriter to encrypt data to the underlying output
	 * stream. If no encryptionKey is specified then an instance of
	 * java.io.BufferedWriter is used to provide buffered access to the output
	 * stream. Also, if the <i>characterSet</i> configuration parameter is set
	 * the output stream is written according the character set specified by
	 * that parameter.
	 * 
	 * @param os
	 *            The OutputStream object (e.g. FileOutputStream,
	 *            SocketOutputStream etc. ..)
	 */
	public void setOutputStream(OutputStream os) {
		Trace.entrymax(this, "setOutputStream", os);
		this.os = os;

		if (getParam("encryptionKey") != null) {
			if (os != null) {
				out = new EncryptedWriter(os);
				try {
					((EncryptedWriter) out).useKey(getParam("encryptionKey"));
				} catch (Exception e) {
					System.err.println(sResHash
							.getString("MISERVER.PARSERIMPL.ENCRYPTEDWRITER", e
									.toString()));
					out = null;
				}
			} else {
				out = null;
			}

			return;
		}

		String charset = getParam("characterSet");

		if (os != null) {
			if (charset != null && charset.length() > 0) {
				try {
					out = new BufferedWriter(
							new OutputStreamWriter(os, charset));
					return;
				} catch (UnsupportedEncodingException uee) {
					logmsg(sResHash.getString("pareser.use.default.encoding",
							uee.toString()));
				}
			}
			out = new BufferedWriter(new OutputStreamWriter(os));

		} else {
			out = null;
		}
		Trace.exitmax(this, "setOutputStream");
	}

	/**
	 * Use a string as input. This method creates an instance of the
	 * StringReader class to read data from a String rather than an input
	 * stream.
	 * 
	 * @param is
	 *            String to read data from
	 */
	public void setInputStream(String is) {
		setInputStream(new StringReader(is));
	}

	/**
	 * Use Reader object for input. This method creates an instance of the
	 * BufferedReader class providing the <i>is</i> reader object as input. Use
	 * this method to pass an already created instance of any Reader class (e.g.
	 * FileReader, StringReader ...). If the <i>encryptionKey</i> parameter is
	 * set an instance of the EncryptedReader is used on top of the provided
	 * Reader to decrypt the input stream.
	 * 
	 * @param is
	 *            Reader object
	 */
	public void setInputStream(Reader is) {
		// in = new BufferedReader (is);
		if (getParam("encryptionKey") != null) {
			if (is != null)
				in = new EncryptedReader(is);
			else
				in = null;

			return;
		}

		if (is != null)
			in = new BufferedReader(is);
		else
			in = null;
	}

	/**
	 * Use Writer object for output. This method creates an instance of the
	 * BufferedWriter class providing the <i>os</i> writer object as input. Use
	 * this method to pass an already created instance of any Writer class (e.g.
	 * FileReader, StringReader ...). If the <i>encryptionKey</i> parameter is
	 * set an instance of the EncryptedWriter is used on top of the provided
	 * Writer to encrypt the output stream.
	 * 
	 * @param os
	 *            Writer object
	 */
	public void setOutputStream(Writer os) {
		Trace.entrymax(this, "setOutputStream", os);
		if (getParam("encryptionKey") != null) {
			if (os != null)
				out = new EncryptedWriter(os);
			else
				out = null;

			return;
		}

		if (os != null)
			out = new BufferedWriter(os);
		else
			out = null;
		Trace.exitmax(this, "setOutputStream");
	}

	/**
	 * Return current reader object.
	 * 
	 * @return The reader object being used for input
	 */
	public BufferedReader getReader() {
		return this.in;
	}

	/**
	 * Return current writer object.
	 * 
	 * @return The writer object being used for output
	 */
	public BufferedWriter getWriter() {
		return this.out;
	}

	/**
	 * Return current input-stream object. This may be null if the parser was
	 * initialized with a Reader object.
	 * 
	 * @return The inputstream object being used for input
	 */
	public InputStream getInputStream() {
		return this.is;
	}

	/**
	 * Return current output-stream object. This may be null if the parser was
	 * initialized with a Writer object.
	 * 
	 * @return The outputstream object being used for output
	 */
	public OutputStream getOutputStream() {
		return this.os;
	}

	/**
	 * Perform initialization of parser.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void initParser() throws Exception {
	}

	/**
	 * Close parser and deallocate resources. This method closes the input and
	 * output streams.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void closeParser() throws Exception {
		try {
			if (in != null && is != System.in) {
				if (debugMode()) {
					debug(sResHash.getString("close.parser.oninput.stream"));
				}
				in.close();
				in = null;
			}
			if (out != null && os != System.out) {
				if (debugMode()) {
					debug(sResHash.getString("close.parser.onoutput.stream"));
				}
				out.close();
				out = null;
			}
		} catch (IOException ignore) {
			logmsg(sResHash.getString("close.parser.exception", ignore));
		}
	}

	/**
	 * Create a parser instance. This method dynamically creates a new instance
	 * of a parser class based on the class name provided.
	 * 
	 * @param className
	 *            The complete Java class name
	 * @return The parser class instance
	 * @throws Exception
	 *             <li>if the <code>className</code> could not be found.</li>
	 *             <li>if the constructor is not visible to the sender.</li>
	 *             <li>if the instance could not be created.</li>
	 */
	public static Object getClassInstance(String className) throws Exception {
		String cls = className;

		if (className.indexOf(".") == -1)
			cls = "com.ibm.di.parser." + className;

		Class<?> t1 = Class.forName(cls);
		return t1.newInstance();
	}

	/**
	 * Writes a message to the log. If the parser has a context then the context
	 * is examined for a known class (like connector, switchboard) and then
	 * calls the context's logmsg method. If no context exists or the context
	 * object type is unknown nothing is done.
	 * 
	 * @param msg
	 *            The log message
	 */
	public void logmsg(String msg) {
		if (context instanceof Connector) {
			((Connector) context).logmsg(msg);
		} else if (context instanceof Function) {
			((Function) context).logmsg(msg);
		}
	}

	/**
	 * Writes a message to the log if debug mode is set.
	 * 
	 * @param msg
	 *            The log message
	 */
	public void debug(String msg) {
		if (debugMode()) {
			logmsg(msg);
		} else if (context instanceof Connector) {
			((Connector) context).debug(msg);
		} else if (context instanceof Function) {
			((Function) context).debug(msg);
		}
	}

	/**
	 * Returns current status of the debugMode flag. May be called by different
	 * threads.
	 * 
	 * @return <code>true</code> if debug mode is enabled, <code>false</code>
	 *         otherwise.
	 */
	public boolean debugMode() {
		return _debug;
	}

	/**
	 * Returns current status of the debugMode flag. May be called by different
	 * threads.
	 * 
	 * @return True if debug mode is enabled, false otherwise.
	 */
	public boolean getDebug() {
		return _debug;
	}

	/**
	 * Sets the status of the debugMode flag. May be called by different
	 * threads.
	 * 
	 * @param debug
	 *            True if debug mode is enabled, false otherwise.
	 */
	public void setDebug(boolean debug) {
		this._debug = debug;
	}

	/**
	 * Register objects in the script engine. This method may be called by a
	 * hosting object to let the parser register objects in the script engine.
	 * This allows the user to access parser specific objects using predefined
	 * names in the scripting environment. The default implementation is to
	 * declare <i>parser</i> as a reference to the parser class instance.
	 * 
	 * @param se
	 *            The script engine
	 * @throws Exception
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception {
		se.declareStaticBean("parser", this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void flush() throws Exception {
	}

	/**
	 * Returns true if this connector is able to perform delta updates
	 * 
	 * @return <code>false</code>
	 */
	public boolean isDeltaSupported() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {
		return null;
	}
}
