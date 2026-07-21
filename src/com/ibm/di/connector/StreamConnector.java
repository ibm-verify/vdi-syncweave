/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * The Memory Stream Connector can read from or write to any Java(TM) stream,
 * but is most often used to write into memory, where the formatted data can be
 * retrieved later. The allocated buffer is retrieved/accessed as needed.
 */
public class StreamConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * name of the properties file
	 */
	private static final String PROPERTIES_FILE = "memorystreamconnector";

	/**
	 * object used for writing
	 */
	private StringWriter writer;

	/**
	 * Resource hash object used for accessing TMS messages
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class constructor
	 */
	public StreamConnector() {
		super();
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE, });
	}

	/**
	 * Default implementation
	 * 
	 * @throws Exception
	 *             never
	 */
	public void selectEntries() throws Exception {
	}

	/**
	 * The Connector can only operate in Iterator mode, AddOnly mode, or Passive
	 * state. The behavior of the Connector depends on the way it has been
	 * initialized.
	 * 
	 * initialize(null) This is the default behavior. The Connector writes into
	 * memory, and the formatted data can be retrieved with the method
	 * getDataBuffer(), only available in Memory Stream Connectors. Assuming the
	 * Connector is named MM, this code can be used anywhere (for example,
	 * Prolog, Epilog, all Hooks, script components, and even inside attribute
	 * mapping):
	 * 
	 * <pre>
	 * 	var str = MM.connector.getDataBuffer();
	 * 	// use str for something.
	 * 	// To clear the data buffer and ready the Connector 
	 * 			for more output, re-initialize
	 * 	MM.connector.initialize(null); 
	 * 	
	 * </pre>
	 * 
	 * <br>
	 * initialize(Reader r): The Connector reads from r. This can be used if you
	 * want to read from a stream. <br>
	 * initialize(Writer w): The Connector writes to w. <br>
	 * initialize(Socket s): The Connector can both read from and write to a
	 * Socket s.
	 * 
	 * @param o
	 *            null/Writer/Reader or Socket
	 * @throws Exception
	 *             if an error occurs
	 */
	public void initialize(Object o) throws Exception {
		if (o instanceof Reader || o instanceof InputStream
				|| o instanceof String || o instanceof StringBuffer) {
			initParser(o, null);
		} else if (o instanceof Writer || o instanceof OutputStream) {
			initParser(null, o);
		} else if (o instanceof Socket) {
			initParser(o, o);
		} else {
			writer = new StringWriter();
			initParser(null, writer);
		}

	}

	/**
	 * uses the provided parser to read an entry
	 * 
	 * @return the read entry
	 * @throws Exception
	 *             if no parser provided or an error occurs
	 * 
	 */
	public Entry getNextEntry() throws Exception {
		if (getParser() == null)
			return null;

		return getParser().readEntry();
	}

	/**
	 * Writes an entry using the provided parser
	 * 
	 * @param entry
	 *            the entry to be written
	 * @throws Exception
	 *             if no parser provided or an error occurs
	 */
	public void putEntry(Entry entry) throws Exception {
		if (getParser() == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.MEMORYSTREAM.NOPARSER"));
		}

		getParser().writeEntry(entry);
	}

	/**
	 * Retrieves data from the buffer
	 * 
	 * @return the buffered data
	 * @throws Exception
	 *             if an I/O error occurs
	 */
	public String getDataBuffer() throws Exception {
		if (writer != null) {
			getParser().flush();
			return writer.toString();
		} else {
			return null;
		}
	}

	/**
	 * Return version information
	 * 
	 * @return version info
	 */
	public String getVersion() {
		return "2.0-di7.1.1 %I% 20%E%";
	}

}
