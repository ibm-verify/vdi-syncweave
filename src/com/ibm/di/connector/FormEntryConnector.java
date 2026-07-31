/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.io.ByteArrayInputStream;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Trace;

/**
 * This connector feeds an AssemblyLine with entries provided as raw data. It
 * has two parameters:
 * <br>
 * <li><code>isLoop</code> - flag that enables looping trough the input data.</li>
 * <br>
 * <li><code>entryRawData</code> - the input entries saved in UTF-8 format; this
 * content can be set at runtime, by using setParam().</li>
 * 
 */
public class FormEntryConnector extends Connector implements ConnectorInterface {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The name of the Connector
	 */
	private static final String myName = "Form Entry Connector";

	/**
	 * Default constructor.
	 */
	public FormEntryConnector() {
		Trace.entrymid(this, "FormEntryConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ITERATOR_MODE });
		Trace.exitmid(this, "FormEntryConnector");
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Object o) throws Exception {
		Trace.entrymin(this, "initialize", o);
		readRaw();
		Trace.exitmin(this, "initialize");
	}

	/**
	 * {@inheritDoc}
	 */
	public void selectEntries() throws Exception {
		Trace.entrymax(this, "selectEntries");
		readRaw();
		Trace.exitmax(this, "selectEntries");
	}

	/**
	 * {@inheritDoc}
	 */
	public Entry getNextEntry() throws Exception {
		Trace.entrymax(this, "getNextEntry");
		Entry e = null;
		if (e == null) {
			e = getParser().readEntry();
			if (e == null) {
				boolean isLoop = Boolean.valueOf(getParam("isLoop"))
						.booleanValue();
				if (isLoop) {
					readRaw();
					e = getParser().readEntry();
				}
			} else {
				return e;
			}
		}
		Trace.exitmax(this, "getNextEntry");
		return e;
	}

	/**
	 * This method read raw entries using the provided parser.
	 * 
	 * @throws Exception
	 *             if a parser is not configured or the exception is derived
	 *             from the parser
	 */
	public void readRaw() throws Exception {
		Trace.entrymin(this, "readRaw");
		byte bytes[] = getParam("entryRawData").getBytes();
		java.io.InputStream in = new ByteArrayInputStream(bytes);
		initParser(in, null);
		Trace.exitmin(this, "readRaw");
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.1-di7.1.1 %I%, 20%E%";
	}
}
