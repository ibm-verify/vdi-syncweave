/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.connector.maximo.util.Dom;
import com.ibm.di.connector.maximo.util.TemplateLoader;
import com.ibm.di.entry.Entry;
import com.ibm.di.parser.xml.XMLParser2;
import com.ibm.di.server.Log;

/**
 * Connector support for <b>Iterator</b> mode.
 * 
 * @since 7.1
 * @see AbstractMxConnMode
 */
public class MxConnIterator extends AbstractMxConnMode {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String XML_RSSTART_ATTR = "rsStart";
	public static final String XML_RSTOTAL_ATTR = "rsTotal";

	public static final String XML_QUERY_TAG = "Query";
	public static final String XML_RESPONSE_TAG = "Response";

	private int currentEntry;

	private int pageSize;

	private NodeList resultSet;

	private int rsStart;

	private int rsTotal;

	private Schema schema;

	private SchemaElement selectedMbo;

	private final TemplateLoader tlp;

	private int numReadEntries;

	private XMLParser2 xmlParser;

	/**
	 * Flag indicating whether flat or hierarchical entries will be returned.
	 */
	protected boolean returnHierarchicalEntries = false;

	/**
	 * Constructs a {@link MxConnIterator}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 */
	public MxConnIterator(final MxConnConfiguration cfg, Log log) {
		this(cfg, log, false);
	}

	/**
	 * Constructs a {@link MxConnIterator}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param log
	 *            logger of the connector
	 * @param returnHierarchicalEntries
	 *            if <code>true</code> hierarchical entries will be used
	 */
	public MxConnIterator(final MxConnConfiguration cfg, Log log, boolean returnHierarchicalEntries) {
		super(cfg, log);
		this.returnHierarchicalEntries = returnHierarchicalEntries;
		tlp = new TemplateLoader(TemplateLoader.TYPE_QUERY, log);

		if (returnHierarchicalEntries) {
			xmlParser = new XMLParser2();
			xmlParser.setParam("xpath.expr", "*/*/*");
			xmlParser.setParam("entry.tag", "");
		}
	}

	/**
	 * Sends the query request to Maximo.
	 * 
	 * @throws MxConnectorException
	 *             if any of the required parameters (page size and query
	 *             enterprise service) is missing
	 * @throws MxConnectorException
	 *             if any sort of comctMxmunication problem occurs
	 * @see MxConnConfiguration#checkIterator()
	 */
	public final void executeQuery() throws MxConnectorException {

		getCfg().checkIterator();

		schema = getCfg().getSchema();
		pageSize = getCfg().getPageSize();

		tlp.setProperty(TemplateLoader.MOS_HOLDER, schema.getMos().getName());
		tlp.setProperty(TemplateLoader.MBO_HOLDER, getCfg().getQueryCriteria());
		tlp.setProperty(TemplateLoader.QUERY_ARGS, getCfg().getParameter(MxConnConfiguration.PARAM_QUERY_ARGS, ""));
		tlp.setProperty(TemplateLoader.MAXITEMS_HOLDER, pageSize);
		tlp.setProperty(TemplateLoader.UNIQUERES_HOLDER, "false");

		numReadEntries = 0;
		rsStart = 0;
		rsTotal = Integer.MAX_VALUE;

		nextPage();
	}

	/**
	 * Returns the next entry found in the query result.
	 * 
	 * @return next entry found, or <code>null</code> if there is no more
	 *         entries
	 * @throws MxConnectorException
	 *             if the query result can not be converted into an
	 *             {@link Entry} object
	 * @see #executeQuery()
	 */
	public final Entry getNext() throws MxConnectorException {
		if (returnHierarchicalEntries) {
			return getHierarchicalEntry();
		}
		return getEntry();
	}

	/**
	 * Returns the next entry found in the query result.
	 * 
	 * @return next entry found, or <code>null</code> if there is no more
	 *         entries
	 * @throws MxConnectorException
	 *             if the query result can not be converted into an
	 *             {@link Entry} object
	 * @see #executeQuery()
	 */
	private final Entry getEntry() throws MxConnectorException {

		if (resultSet == null) {
			return null;
		}

		if (currentEntry >= resultSet.getLength() && hasNextPage()) {
			nextPage();
		}

		if (currentEntry < resultSet.getLength()) {
			numReadEntries++;
			final Node n = resultSet.item(currentEntry++);
			return EntryConverter.xmlToEntry(schema.getRootMbo(), selectedMbo, n);
		}

		return null;
	}

	/**
	 * @return hierarchical entry
	 * @throws MxConnectorException
	 *             if an error occurs while parsing
	 */
	private Entry getHierarchicalEntry() throws MxConnectorException {

		if (currentEntry >= getCfg().getPageSize() && hasNextPage()) {
			nextPage();
		}

		Entry entry = null;
		try {
			entry = xmlParser.readEntry();
		} catch (Exception e) {
			throw new MxConnectorException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.PARSE.XML"), e);
		}

		numReadEntries++;
		currentEntry++;

		return entry;
	}

	private boolean hasNextPage() {
		logger.debug("Get: " + numReadEntries);
		logger.debug(XML_RSTOTAL_ATTR + " = " + rsTotal);
		logger.debug("has next page = " + (numReadEntries < rsTotal));

		return numReadEntries < rsTotal;
	}

	private void nextPage() throws MxConnectorException {
		tlp.setProperty(TemplateLoader.RSSTART_HOLDER, rsStart);

		final String response = post(tlp, getCfg().getUrlListForQuery());

		logger.debug(SimpleTpaeIFConnector.getResHash().getString("MXCONN.PARSING.XML"));

		if (returnHierarchicalEntries) {
			xmlParser.setInputStream(response);

			try {
				xmlParser.initParser();
			} catch (Exception e) {
				throw new MxConnectorException(SimpleTpaeIFConnector.getResHash().getString("MXCONN.CANNOT.INIT.XML.PARSER"), e);
			}
		}

		final Document doc = Dom.parse(response);
		final String responseTagName = XML_QUERY_TAG + schema.getMos().getName() + XML_RESPONSE_TAG;
		final Node n = doc.getElementsByTagName(responseTagName).item(0);

		rsStart = Integer.valueOf(Dom.getAttributeValue(n, XML_RSSTART_ATTR));
		rsTotal = Integer.valueOf(Dom.getAttributeValue(n, XML_RSTOTAL_ATTR));
		rsStart += pageSize;

		selectedMbo = schema.getMboByName(getCfg().getMbo());
		resultSet = Dom.getElements(selectedMbo.getName(), doc);
		currentEntry = 0;
	}
}
