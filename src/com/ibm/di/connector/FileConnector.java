/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.CSVParser;
import com.ibm.di.parser.ParserInterface;
import com.ibm.di.server.ConnectorMode;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;
import com.ibm.di.server.Trace;

/**
 * The file system Connector is a transport Connector that requires a Parser to
 * operate. The file system Connector reads and writes files available on the
 * system it runs on. Concurrent usage of a file can be controlled by means of a
 * locking mechanism. This Connector can only be used in Iterator or AddOnly
 * mode, or for the equivalent operations in Passive state.
 * 
 */
public class FileConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "fileconnector";

	/**
	 * Wait available flag.
	 */
	private boolean waitavailable = false;

	/**
	 * Timeout in seconds for attempting to lock a file, or for reading an
	 * unlocked file (<=0 means Wait Forever).
	 */
	private long timeout = 0;

	/**
	 * File opened flag.
	 */
	private boolean fileOpened = false;

	/**
	 * System output file.
	 */
	private File outFile = null;

	/**
	 * Exclusive lock flag.
	 */
	private boolean exclusiveLock = false;

	/**
	 * A token representing a lock on a region of a file.
	 */
	private FileLock lock = null;

	/**
	 * Component name.
	 */
	private static final String myName = "File Connector";
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Constructor. Initializes the connector to work in AddOnly and Iterator
	 * mode.
	 */
	public FileConnector() {
		super();
		Trace.entrymid(this, "FileConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE });
		Trace.exitmid(this, "FileConnector");
	}

	/**
	 * Initialize the connector. The connector may be passed a parameter of any
	 * kind by the user. It is up to the connector to determine whether this
	 * object can be used or not. The parameter is typically provided by a user
	 * script. When an AssemblyLine initializes it's Connectors, they are passed
	 * a ConnectorMode object.
	 * 
	 * @param o
	 *            Expects Reader or Writer to initialize the Parser with, or a
	 *            ConnectorMode with the mode in which the Iterator is set to
	 *            open the specified file in the configuration for reading or
	 *            writing respectively for Iterator and AddOnly modes. If the
	 *            object is not an instance of those classes, the iterator
	 *            checks for a configuration parameter with name "fileMode" and
	 *            if exists, checks if equals to "input" (and opens the file
	 *            specified in the configuration for reading) or equals to
	 *            "output" (and opens the file specified in the configuration
	 *            for writing). An exception is thrown if none of these
	 *            situations occur.
	 */
	@Override
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);
		String param;

		fileOpened = false;

		waitavailable = false;
		param = getParam("fileAwaitDataTimeout");
		if (param != null) {
			param = param.trim();
			if (param.length() > 0) {
				waitavailable = true;
				timeout = Long.parseLong(param);
			}
		}

		param = getParam("exclusiveLock");
		if (param != null && param.length() > 0)
			exclusiveLock = param.equalsIgnoreCase("true");

		if (o instanceof Reader) {
			initParser(o, null);
			fileOpened = true;
		} else if (o instanceof Writer) {
			initParser(null, o);
			fileOpened = true;
		} else if (o instanceof ConnectorMode) {
			int mode = ((ConnectorMode) o).getMode();
			if (mode == ServerConstants.TYPE_ITERATOR) {
				openReadFile();
			} else if (mode == ServerConstants.TYPE_ADDONLY) {
				openWriteFile();
			}
		} else {
			// fileMode Check here is for backward compatibility
			String fileMode = getParam("fileMode");

			if ("input".equalsIgnoreCase(fileMode)) {
				openReadFile();
			} else if ("output".equalsIgnoreCase(fileMode)) {
				openWriteFile();
			}

			if (getParam("filePath") == null) {
				throw new Exception(
						sResHash
								.getString("CONNECTOR.FILE.FILEPATHPARAM.MISSING.EXCEPTION"));
			}
		}
		Trace.exitmin(this, "initialize");
	}

	/**
	 * Prepare the Connector for sequential read. Opens the file specified in
	 * FilePath field in the Config Tab. When the Connector is used as an
	 * Iterator in an AssemblyLine, this method will be called.
	 */
	@Override
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		if (!fileOpened || getParser() == null
				|| getParser().getReader() == null) {
			openReadFile();
		}
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * Return the next Entry from the connector.
	 * 
	 * @return - the next Entry, or null if no more data
	 * @see #selectEntries()
	 */
	@Override
	public Entry getNextEntry() throws Exception {
		Trace.entrymax(this, "getNextEntry");
		if (waitavailable) {

			long counter = 0;
			Entry e = null;

			while (e == null) {

				e = getParser().readEntry();

				if (e == null) {
					if (debugMode()) {
						debug(sResHash.getString(
								"CONNECTOR.FILE.DATAWAIT.INFO", new Object[] {
										"" + counter, "" + timeout }));
					}
					Thread.sleep(1000);
					if (timeout < 1) {
						continue;
					}

					if (++counter > timeout) {
						return null;
					}
				}

			}

			return e;

		}
		Trace.exitmax(this, "getNextEntry");
		return getParser().readEntry();
	}

	/**
	 * Add a new entry to the data source
	 * 
	 * @param entry
	 *            The entry data to add
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		Trace.entrymin(this, "getNextEntry", entry);
		if (!fileOpened || getParser() == null
				|| getParser().getWriter() == null) {
			openWriteFile();
		}
		getParser().writeEntry(entry);
		Trace.exitmin(this, "getNextEntry");
	}

	/**
	 * Opens the file specified by the FilePath field in the Config Tab for
	 * reading. If the default value <Use Standard I/O streams> is specified,
	 * then the standard input is used. The parser is initialized with the
	 * acquired stream.
	 * 
	 * @throws Exception
	 *             If the FilePath field is empty, an Exception is thrown
	 */
	public void openReadFile() throws Exception {
		Trace.entrymin(this, "openReadFile");
		String path = getParam("filePath");
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.FILE.OPEN.INPUT", path));
		}
		if (path == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.FILE.FILEPATHPARAMREAD.MISSING.EXCEPTION"));
		}
		InputStream in;
		if (path.startsWith("<") && path.endsWith(">")) {
			in = System.in;
		} else {
			in = new FileInputStream(path);
			if (exclusiveLock)
				acquireLock(((FileInputStream) in).getChannel(), timeout, true);
		}
		initParser(in, null);
		fileOpened = true;

		Trace.exitmin(this, "openReadFile");
	}

	/**
	 * Opens the file specified by the FilePath field in the Config Tab for
	 * writing. If the default value <Use Standard I/O streams> is specified,
	 * then the standard output is used. If the "fileMode" parameter is set to
	 * "append", then the stream is opened for appending. The parser is
	 * initialized with the acquired stream. If appending and the parser is a
	 * CSV parser, the method takes care for what is necessary to append to the
	 * stream.
	 * 
	 * @throws Exception
	 *             If the FilePath field is empty, an Exception is thrown
	 */
	public void openWriteFile() throws Exception {
		Trace.entrymin(this, "openWriteFile");
		String path = getParam("filePath");
		if (debugMode()) {
			debug(sResHash.getString("CONNECTOR.FILE.OPEN.OUTPUT", path));
		}
		if (path == null) {
			throw new Exception(
					sResHash
							.getString("CONNECTOR.FILE.FILEPATHPARAMWRITE.MISSING.EXCEPTION"));
		}

		boolean append = false;
		String param = getParam("fileMode");
		if (param != null && param.equalsIgnoreCase("append"))
			append = true;

		param = getParam("fileAppend");
		if (param != null && param.length() > 0)
			append = param.equalsIgnoreCase("true");

		OutputStream out;
		if (path.startsWith("<") && path.endsWith(">")) {
			out = System.out;
			outFile = null;
		} else {
			out = new FileOutputStream(path, append);
			outFile = new File(path);
			if (exclusiveLock)
				acquireLock(((FileOutputStream) out).getChannel(), timeout,
						false);
		}

		initParser(null, out);
		fileOpened = true;

		if (outFile != null && outFile.length() > 0) {
			// appending to a nonempty file
			ParserInterface p = getParser();
			if (p instanceof CSVParser)
				((CSVParser) p).setHeadersWritten(true);
		}
		Trace.exitmin(this, "openWriteFile");
	}

	/**
	 * Attempts to acquire a lock on a File Channel
	 * 
	 * @param fc
	 *            The File channel we are attempting to lock
	 * @param timeout
	 *            The max time in seconds in which the lock has to be acquired.
	 * @param shared
	 *            true to request a shared lock, in which case this channel must
	 *            be open for reading (and possibly writing); false to request
	 *            an exclusive lock, in which case this channel must be open for
	 *            writing (and possibly reading)
	 * @throws Exception
	 *             If unable to acquire the lock within timeout time, an
	 *             Exception is thrown
	 */
	public void acquireLock(FileChannel fc, long timeout, boolean shared)
			throws Exception {
		waitavailable = false;

		if (timeout < 1) {
			lock = fc.lock(0, Long.MAX_VALUE, shared);
			return;
		}

		int counter = 0;
		lock = fc.tryLock(0, Long.MAX_VALUE, shared);

		while (lock == null && counter < timeout) {
			Thread.sleep(1000);
			counter++;
			lock = fc.tryLock(0, Long.MAX_VALUE, shared);
		}

		if (lock == null) {
			throw new Exception(sResHash
					.getString("CONNECTOR.FILE.LOCKTIMEOUT.EXCEPTION"));
		}
	}

	/**
	 * Releases the acquired lock.
	 */
	public void releaseLock() {
		if (lock != null) {
			try {
				lock.release();
			} catch (Exception ignore) {
			}
			lock = null;
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.3-di7.1.1 %I%, 20%E%";
	}

	/**
	 * Reconnect to the underlying data source. Terminates the current
	 * connector, initializes it again and if in Iterator mode, prepares the
	 * connector for sequential read.
	 */
	@Override
	public void reconnect() throws Exception {
		terminate();
		initialize(this);
		if (((ConnectorConfig) getConfiguration()).getMode().equals(
				ConnectorConfig.ITERATOR_MODE)) {
			selectEntries();
		}
	}

}
