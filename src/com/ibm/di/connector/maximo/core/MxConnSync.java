/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import com.ibm.di.connector.maximo.TpaeIFConnector;
import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * Connector support for <b>AddOnly</b>, <b>Update</b> and <b>Delete</b> mode.
 * Used only by the {@link TpaeIFConnector}.
 * 
 * @since 7.2
 * @see AbstractMxConnMode
 */
public final class MxConnSync extends AbstractMxConnMode {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final TemplateLoader tlp;

	public MxConnSync(MxConnConfiguration cfg, Log log) {
		super(cfg, log);
		tlp = new TemplateLoader(TemplateLoader.TYPE_SYNC, log);
	}

	/**
	 * Syncronizes the <code>newEntry</code> with the target system. This
	 * function handles both create, update and delete operations.
	 * 
	 * @param newEntry
	 *            entry to be added, updated or deleted
	 * @param oldEntry
	 *            old version of the entry when updating
	 * @throws MxConnectorException
	 *             if the entry to be updated does not comply with the XML
	 *             schema descriptor
	 * @throws MxConnectorException
	 *             if any sort of communication problem occurs
	 * @see MxConnConfiguration#checkUpdate()
	 */
	public void sync(final Entry newEntry, final Entry oldEntry) throws MxConnectorException {
		getCfg().checkAUDmodes();

		if (newEntry == null) {
			return;
		}

		final Schema schema = getCfg().getSchema();
		Attribute rootAttr = newEntry.getAttribute(schema.getRootMbo().getName());
		
		
		switch (newEntry.getOp()) {
		case Entry.OP_ADD:
			rootAttr.setAttribute(ACTION_ATTR, ADD_ACTION);
			break;
		case Entry.OP_DEL:
			rootAttr.setAttribute(ACTION_ATTR, DELETE_ACTION);
			break;
		case Entry.OP_MOD:
			if (oldEntry != null) {
				// Check if user has tried to change any unique keys
				EntryConverter.checkForOverridenUniqueKeys(schema.getRootMbo(), newEntry, oldEntry, logger);

				// Make sure the new entry contains all unique keys
				EntryConverter.copyUniqueKeys(schema.getRootMbo(), newEntry, oldEntry);
			}

			// Check if we want to replace entry in Maximo server (this means
			// non-present child MBOs will be deleted in the Tpae server).
			if (getCfg().getReplaceOnUpdate()) {
				// Change the action attribute from "Change" to "Replace"
				rootAttr.setAttribute(ACTION_ATTR, REPLACE_ACTION);
			} else {
				// Set XML action attributes based on Attributes delta tags
				// This makes sense only for root MBO with "Change" action.
				//EntryConverter.setAttributeActions(newEntry);
				/*Somehow the above line is not executing properly & hence by default action code is setting to AddReplace - this is a bug
				hence we are setting it to AddChange to give simillar effect*/
				rootAttr.setAttribute(ACTION_ATTR, ADDCHANGE_ACTION);
			}

			break;
		}
	
		String userSetAction = rootAttr.getAttribute(AbstractMxConnMode.ACTION_ATTR);
		/*
		//Below we are allowing to override default values with user set value
		//Maximo will throw proper error message in case if action attribute value is invalid		
		if(userSetAction.equals(CHANGE_ACTION) || userSetAction.equals(REPLACE_ACTION) || userSetAction.equals(ADDCHANGE_ACTION))*/
		if(userSetAction != null && !userSetAction.isEmpty() && !userSetAction.equals(""))
			rootAttr.setAttribute(ACTION_ATTR, userSetAction);
		//else
		//	throw new MxConnSchemaException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.ILLEGAL.ELEMENT", userSetAction));

		String xml = EntryConverter.entryToXml(getCfg().getSchema(), newEntry, getCfg().isErrorOnExcedentSizeEnabled(), true);
		tlp.setProperty(TemplateLoader.MOS_HOLDER, schema.getMos().getName());
		tlp.setProperty(TemplateLoader.MBO_HOLDER, xml);
		post(tlp, getCfg().getUrlListForSync());
	}
}