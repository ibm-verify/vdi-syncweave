/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Connector support for <b>Update</b> mode.
 * 
 * @since 7.1
 * @see AbstractMxConnMode
 */
public final class MxConnUpdate extends AbstractMxConnMode {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final TemplateLoader tlp;

	/**
	 * Constructs a {@link MxConnUpdate}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 */
	public MxConnUpdate(final MxConnConfiguration cfg, Log log) {
		this(cfg, log, false);
	}

	/**
	 * Constructs a {@link MxConnUpdate}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 * @param isHierEntriesUsed
	 *            if <code>true</code> hierarchical entries will be used
	 */
	public MxConnUpdate(final MxConnConfiguration cfg, Log log, boolean isHierEntriesUsed) {
		super(cfg, log);
		tlp = new TemplateLoader(TemplateLoader.TYPE_UPDATE, log);
	}

	/**
	 * Updates the specified entry.
	 * 
	 * @param newEntry
	 *            entry to be updated
	 * @param oldEntry
	 *            old version of the entry to be updated
	 * @throws MxConnectorException
	 *             if any of the required parameters (update enterprise service)
	 *             is missing
	 * @throws MxConnectorException
	 *             if the entry to be updated does not comply with the XML
	 *             schema descriptor
	 * @throws MxConnectorException
	 *             if any sort of communication problem occurs
	 * @see MxConnConfiguration#checkUpdate()
	 */
	public void update(final Entry newEntry, final Entry oldEntry) throws MxConnectorException {
		getCfg().checkUpdate();

		final Schema schema = getCfg().getSchema();
		final SchemaElement rootMbo = schema.getRootMbo();
		final SchemaElement selectedMbo = schema.getMboByName(getCfg().getMbo());
		
		// if 'Skip lookup' is checked this will be null
		if (oldEntry != null) {
			
			// Check if user has tried to change any unique keys
			EntryConverter.checkForOverridenUniqueKeys(rootMbo, newEntry, oldEntry, logger);
			
			// Make sure the new entry contains all unique keys
			EntryConverter.copyUniqueKeys(rootMbo, newEntry, oldEntry);
		}

		newEntry.setAttribute(ACTION_ATTR, CHANGE_ACTION);

		SchemaElement mbo = selectedMbo;
		while (!mbo.equals(rootMbo)) {
			newEntry.setAttribute(mbo.getPathRelativeTo(rootMbo) + ACTION_ATTR_PREFIXED, CHANGE_ACTION);
			mbo = mbo.getParent();
		}

		final String xml = EntryConverter.entryToXml(rootMbo, selectedMbo, newEntry, getCfg().isErrorOnExcedentSizeEnabled());
		tlp.setProperty(TemplateLoader.MOS_HOLDER, schema.getMos().getName());
		tlp.setProperty(TemplateLoader.MBO_HOLDER, xml);
		post(tlp, getCfg().getUrlListForUpdate());
	}
}
