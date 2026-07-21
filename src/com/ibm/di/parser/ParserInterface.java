/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.script.ScriptEngine;

/**
 * This is the interface implemented by all TDI parsers.
 */
public interface ParserInterface {
	/**
	 * Sets the inputStream attribute of the ParserInterface object.
	 * 
	 * @param is
	 *            The new inputStream value
	 * 
	 */
	public void setInputStream(InputStream is);

	/**
	 * Sets the outputStream attribute of the ParserInterface object.
	 * 
	 * @param os
	 *            The new outputStream value
	 * 
	 */
	public void setOutputStream(OutputStream os);

	/**
	 * Sets the inputStream attribute of the ParserInterface object.
	 * 
	 * @param is
	 *            The new inputStream value
	 * 
	 */
	public void setInputStream(String is);

	/**
	 * Sets the inputStream attribute of the ParserInterface object.
	 * 
	 * @param is
	 *            The new inputStream value
	 * 
	 */
	public void setInputStream(Reader is);

	/**
	 * Sets the outputStream attribute of the ParserInterface object.
	 * 
	 * @param os
	 *            The new outputStream value
	 * 
	 */
	public void setOutputStream(Writer os);

	/**
	 * Sets the parser attribute of the ParserInterface object.
	 * 
	 * @param parser
	 *            The new parser value
	 * 
	 */
	public void setParser(ParserInterface parser);

	/**
	 * Gets the parser attribute of the ParserInterface object.
	 * 
	 * @return The parser value
	 * 
	 */
	public ParserInterface getParser();

	/**
	 * Gets the reader attribute of the ParserInterface object.
	 * 
	 * @return The reader value
	 * 
	 */
	public BufferedReader getReader();

	/**
	 * Gets the writer attribute of the ParserInterface object.
	 * 
	 * @return The writer value
	 * 
	 */
	public BufferedWriter getWriter();

	/**
	 * Write an entry to the current output stream.
	 * 
	 * @param entry
	 *            The entry to write
	 * @exception Exception
	 *                if an error occurs.
	 * 
	 */
	public void writeEntry(Entry entry) throws Exception;

	/**
	 * Return the next entry from the current input stream.
	 * 
	 * @return The next entry from the input stream
	 * @exception Exception
	 * 
	 */
	public Entry readEntry() throws Exception;

	/**
	 * This method is called by the hosting component (e.g. connector) to
	 * initialize the parser.
	 * 
	 * @exception Exception
	 *                if an error occurs.
	 * 
	 */
	public void initParser() throws Exception;

	/**
	 * This method is called by the hosting component (e.g. connector) to close
	 * and release parser resources.
	 * 
	 * @throws Exception
	 *             If an I/O error occurs
	 * 
	 */
	public void closeParser() throws Exception;

	/**
	 * This method is called by some hosting components to flush any in-memory
	 * data to the current output stream.
	 * 
	 * @throws Exception
	 */
	public void flush() throws Exception;

	/**
	 * Sets the configuration attribute of the ParserInterface object
	 * 
	 * @param config
	 *            The new configuration value
	 */
	public void setConfiguration(ParserConfig config);

	/**
	 * Gets the param attribute of the ParserInterface object.
	 * 
	 * @param param
	 *            The parameter name
	 * @return The param value
	 * 
	 */
	public String getParam(String param);

	/**
	 * Sets the param attribute of the ParserInterface object.
	 * 
	 * @param param
	 *            The new param value
	 * @param value
	 *            The new param value
	 * 
	 */
	public void setParam(String param, String value);

	/**
	 * Sets the context attribute of the ParserInterface object.
	 * 
	 * @param context
	 *            The new context value
	 * 
	 */
	public void setContext(Object context);

	/**
	 * Gets the context attribute of the ParserInterface object.
	 * 
	 * @return The context value
	 * 
	 */
	public Object getContext();

	/**
	 * Called by the hosting component (e.g. connector) to let the parser
	 * register its own script beans in the script engine.
	 * 
	 * @param se
	 *            The script engine
	 * @exception Exception
	 * 
	 */
	public void registerScriptBeans(ScriptEngine se) throws Exception;

	/**
	 * Gets the debug attribute of the ParserInterface object.
	 * 
	 * @return The debug value
	 * 
	 */
	public boolean getDebug();

	/**
	 * Sets the debug attribute of the ParserInterface object.
	 * 
	 * @param debug
	 *            The new debug value
	 * 
	 */
	public void setDebug(boolean debug);

	/**
	 * Returns <code>true</code> if this connector is able to perform delta
	 * updates.
	 * 
	 * @return <code>true</code> if delta updates are supported,
	 *         <code>false</code> otherwise
	 */
	public boolean isDeltaSupported();

	/**
	 * Discover the schema for the Parser. For example, a XML Parser could
	 * return a representation of the XML Schema or the DTD referenced in a XML
	 * file.
	 * 
	 * @param source
	 *            The object on which to discover schema
	 * @return A Vector of com.ibm.di.entry.Entry objects describing each entity
	 * @throws Exception
	 *             If an I/O error occurs
	 * 
	 * @since 7.0
	 */
	public Object querySchema(Object source) throws Exception;
}
