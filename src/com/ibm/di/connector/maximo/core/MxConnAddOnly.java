/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import java.util.List;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Connector support for <b>AddOnly</b> mode.
 * 
 * @since 7.1
 * @see AbstractMxConnMode
 */
public final class MxConnAddOnly extends AbstractMxConnMode {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final TemplateLoader createTlp;

	private final TemplateLoader updateTlp;

	/**
	 * Constructs a {@link MxConnAddOnly}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 */
	public MxConnAddOnly(final MxConnConfiguration cfg, Log log) {
		this(cfg, log, false);
	}

	/**
	 * Constructs a {@link MxConnAddOnly}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 * @param isHierEntriesUsed
	 *            if <code>true</code> hierarchical entries will be used
	 */
	public MxConnAddOnly(final MxConnConfiguration cfg, Log log, boolean isHierEntriesUsed) {
		super(cfg, log);
		createTlp = new TemplateLoader(TemplateLoader.TYPE_CREATE, log);
		updateTlp = new TemplateLoader(TemplateLoader.TYPE_UPDATE, log);
	}

	/**
	 * Creates the specified entry.
	 * 
	 * @param newEntry
	 *            entry to be created
	 * @throws MxConnectorException
	 *             if any of the required parameters (create enterprise service
	 *             and update enterprise service) is missing
	 * @throws MxConnectorException
	 *             if the entry to be created does not comply with the XML
	 *             schema descriptor
	 * @throws MxConnectorException
	 *             if any sort of communication problem occurs
	 * @see MxConnConfiguration#checkAddOnly()
	 */
	public void create(final Entry newEntry) throws MxConnectorException {
		getCfg().checkAddOnly();
		
		final List<String> urlList;
		final TemplateLoader tlp;
		final Schema schema = getCfg().getSchema();
		final SchemaElement rootMbo = schema.getRootMbo();
		final SchemaElement selectedMbo = schema.getMboByName(getCfg().getMbo());

		if (rootMbo.equals(selectedMbo)) {
			urlList = getCfg().getUrlListForCreate();
			tlp = createTlp;
			newEntry.setAttribute(ACTION_ATTR, ADD_ACTION);
		} else {
			urlList = getCfg().getUrlListForUpdate();
			tlp = updateTlp;
			newEntry.setAttribute(ACTION_ATTR, CHANGE_ACTION);
			newEntry.setAttribute(selectedMbo.getPathRelativeTo(rootMbo) + ACTION_ATTR_PREFIXED, ADD_ACTION);

			SchemaElement mbo = selectedMbo.getParent();
			while (!mbo.equals(rootMbo)) {
				newEntry.setAttribute(mbo.getPathRelativeTo(rootMbo) + ACTION_ATTR_PREFIXED, CHANGE_ACTION);
				mbo = mbo.getParent();
			}
		}

		final String xml = EntryConverter.entryToXml(rootMbo, selectedMbo, newEntry, getCfg().isErrorOnExcedentSizeEnabled());
		tlp.setProperty(TemplateLoader.MOS_HOLDER, schema.getMos().getName());
		tlp.setProperty(TemplateLoader.MBO_HOLDER, xml);

		post(tlp, urlList);
	}
}
