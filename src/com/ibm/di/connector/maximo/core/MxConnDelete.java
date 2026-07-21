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
 * Connector support for <b>Delete</b> mode.
 * 
 * @since 7.1
 * @see AbstractMxConnMode
 */
public final class MxConnDelete extends AbstractMxConnMode {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	//private final TemplateLoader deleteTlp;

	//private final TemplateLoader updateTlp;
	
	private final TemplateLoader syncTlp;

	/**
	 * Constructs a {@link MxConnDelete}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 */
	public MxConnDelete(final MxConnConfiguration cfg, Log log) {
		this(cfg, log, false);
	}

	/**
	 * Constructs a {@link MxConnDelete}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 * @param isHierEntriesUsed
	 *            if <code>true</code> hierarchical entries will be used
	 */
	public MxConnDelete(final MxConnConfiguration cfg, Log log, boolean isHierEntriesUsed) {
		super(cfg, log);
		//deleteTlp = new TemplateLoader(TemplateLoader.TYPE_DELETE, log);
		//updateTlp = new TemplateLoader(TemplateLoader.TYPE_UPDATE, log);
		syncTlp = new TemplateLoader(TemplateLoader.TYPE_SYNC, log);
	}

	/**
	 * Deletes the specified entry.
	 * 
	 * @param entry
	 *            entry to be deleted
	 * @throws MxConnectorException
	 *             if any of the required parameters (delete enterprise service
	 *             and update enterprise service) is missing
	 * @throws MxConnectorException
	 *             if the entry to be deleted does not comply with the XML
	 *             schema descriptor
	 * @throws MxConnectorException
	 *             if any sort of communication problem occurs
	 * @see MxConnConfiguration#checkDelete()
	 */
	public void delete(final Entry entry) throws MxConnectorException {

		getCfg().checkDelete();

		final List<String> urlList;
		final TemplateLoader tlp;
		final Schema schema = getCfg().getSchema();
		final SchemaElement rootMbo = schema.getRootMbo();
		final SchemaElement selectedMbo = schema.getMboByName(getCfg().getMbo());
		final Entry keyOnlyEntry = new Entry();

		if (entry != null) {
			EntryConverter.copyUniqueKeys(rootMbo, keyOnlyEntry, entry);
		}
		//Added as part of defect 15327
		urlList = getCfg().getUrlListForSync();
		tlp = syncTlp;
		
		if (rootMbo.equals(selectedMbo)) {
			//urlList = getCfg().getUrlListForDelete();
			//tlp = deleteTlp;
			keyOnlyEntry.setAttribute(ACTION_ATTR, DELETE_ACTION);
		} else {
			//urlList = getCfg().getUrlListForUpdate();
			//tlp = updateTlp;
			keyOnlyEntry.setAttribute(ACTION_ATTR, CHANGE_ACTION);
			keyOnlyEntry.setAttribute(selectedMbo.getPathRelativeTo(rootMbo) + ACTION_ATTR_PREFIXED, DELETE_ACTION);

			SchemaElement mbo = selectedMbo.getParent();
			while (!mbo.equals(rootMbo)) {
				keyOnlyEntry.setAttribute(mbo.getPathRelativeTo(rootMbo) + ACTION_ATTR_PREFIXED, CHANGE_ACTION);
				mbo = mbo.getParent();
			}
		}

		final String xml = EntryConverter.entryToXml(rootMbo, selectedMbo, keyOnlyEntry, getCfg().isErrorOnExcedentSizeEnabled());
		tlp.setProperty(TemplateLoader.MOS_HOLDER, schema.getMos().getName());
		tlp.setProperty(TemplateLoader.MBO_HOLDER, xml);

		post(tlp, urlList);
	}
}
