/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector;

import java.util.List;
import java.util.Vector;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Trace;

/**
 * This connector is used for sending Administration Process requests to a
 * Domino server.
 */
public class DominoAdminPConnector extends DominoConnector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The string name of the Component.
	 */
	protected static final String myName = "Domino AdminP Connector";

	/**
	 * Request type index.
	 */
	private String requestType = "-1";

	/**
	 * Constructor for the DominoAdminPConnector object
	 */
	public DominoAdminPConnector() {
		Trace.entrymid(this, "DominoAdminPConnector");
		setName(myName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.ITERATOR_MODE });
		Trace.exitmid(this, "DominoAdminPConnector");
	}

	/**
	 * This is an internal method used during connector's initialization. It is
	 * created in order to have only one thread accessing the Domino database
	 * 
	 * @param o ignored
	 * 
	 * @exception Exception
	 *                An exception is thrown if this method fails.
	 */
	protected void inner_initialize(Object o) throws Exception {
		super.inner_initialize(o);

		requestType = getParam("requestType");

		if ((selection == null) || (selection.trim().length() < 1)) {
			SchemaConfig config = ((ConnectorConfig) getConfiguration())
					.getSchema(requestType);
			SchemaItemConfig proxyAction = config.getItem("ProxyAction");

			if (proxyAction != null) {
				selection = "Select ProxyAction=\"" + proxyAction.getSample()
						+ "\"";
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object querySchema(Object source) throws Exception {
		SchemaConfig config = ((ConnectorConfig) getConfiguration())
				.getSchema(requestType);

		Vector<Entry> result = new Vector<Entry>();

		List names = config.getItemNames();

		for (int i = 0; i < names.size(); i++) {
			SchemaItemConfig configItem = (SchemaItemConfig) config
					.getItem(names.get(i));

			Entry e = new Entry();

			e.setAttribute("name", configItem.getAttributeName());
			e.setAttribute("class", configItem.getJavaClass());
			e.setAttribute("syntax", configItem.getExternalSyntax());
			e.setAttribute("sample", configItem.getSample());

			result.add(e);
		}

		return result;
	}

}
