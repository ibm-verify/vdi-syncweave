/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.maximo.core;

import com.ibm.di.connector.maximo.exception.MxConnectorException;
import com.ibm.di.connector.maximo.parsing.EntryConverter;
import com.ibm.di.connector.maximo.parsing.Schema;
import com.ibm.di.connector.maximo.parsing.SchemaElement;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.SearchCriteria;

/**
 * Connector support for <b>Lookup</b> mode.
 * 
 * @since 7.1
 * @see MxConnIterator
 */
public final class MxConnLookup extends MxConnIterator {
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Constructs a {@link MxConnLookup}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param logger
	 *            logger of the connector
	 */
	public MxConnLookup(final MxConnConfiguration cfg, Log logger) {
		this(cfg, logger, false);
	}

	/**
	 * Constructs a {@link MxConnLookup}.
	 * 
	 * @param cfg
	 *            the connector configuration object
	 * @param logger
	 *            logger of the connector
	 * @param returnHierarchicalEntries
	 *            if <code>true</code> hierarchical entries will be returned
	 */
	public MxConnLookup(final MxConnConfiguration cfg, Log logger, boolean returnHierarchicalEntries) {
		super(cfg, logger, returnHierarchicalEntries);
	}

	/**
	 * Defines the criteria used in the query operation.
	 * 
	 * @param searchCriteria
	 *            criteria used in the query operation
	 * @throws MxConnectorException
	 *             if hierarchical entries are not used and
	 *             <code>criteria.getType() != SearchCriteria.SEARCH_AND</code>
	 * @throws MxConnectorException
	 *             if hierarchical entries are not used and some
	 *             SearchCriteria.rscSearch has <tt>match</tt> attribute
	 *             different than <tt>SearchCriteria.EXACT</tt> and
	 *             <tt>SearchCriteria.NOT_STRING</tt>
	 * @see MxConnConfiguration#PARAM_QUERY_CRITERIA
	 * @see MxConnIterator#executeQuery()
	 * @see EntryConverter#searchCriteriaToXml(SearchCriteria, SchemaElement,
	 *      boolean)
	 */
	public void setSearchCriteria(final SearchCriteria searchCriteria) throws MxConnectorException {

		String xml = null;
		Schema schema = getCfg().getSchema();

		EntryFilter.checkSearchCriteria(searchCriteria);

		if (returnHierarchicalEntries) {
			boolean errorOnExcedentSize = getCfg().isErrorOnExcedentSizeEnabled();
			Entry entry = EntryConverter.searchCriteriaToEntry(searchCriteria, schema, errorOnExcedentSize);
			xml = EntryConverter.entryToXml(getCfg().getSchema(), entry, errorOnExcedentSize, false);
		} else {
			xml = EntryConverter.searchCriteriaToXml(searchCriteria, schema.getRootMbo(), getCfg().isErrorOnExcedentSizeEnabled());
		}

		getCfg().setParameter(MxConnConfiguration.PARAM_QUERY_CRITERIA, xml);
	}
}
