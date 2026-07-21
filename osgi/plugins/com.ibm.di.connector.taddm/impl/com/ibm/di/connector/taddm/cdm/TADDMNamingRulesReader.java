/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.collation.platform.model.AttributeNotSetException;
import com.collation.platform.model.topology.meta.ObjectClass;
import com.collation.proxy.api.client.ApiException;
import com.collation.proxy.api.client.CMDBApi;
import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRulesReader;

/**
 * The reader for TADDM Naming Rule meta-data.
 */
public class TADDMNamingRulesReader extends NamingRulesReader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The TADDM API.
	 */
	private CMDBApi api;

	/**
	 * Constructor.
	 * 
	 * @param api
	 *            the TADDM API.
	 */
	public TADDMNamingRulesReader(CMDBApi api) {
		this.api = api;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NamingRule> getAllNamingRules(String cdmClassName) throws Exception {
		List<NamingRule> rules = new ArrayList<NamingRule>();
		ObjectClass metaData = getMetaData(cdmClassName);
		try {
			if (metaData.hasNamingRules()) {
				com.collation.platform.model.topology.meta.NamingRule[] taddmRules = metaData.getNamingRules();
				for (com.collation.platform.model.topology.meta.NamingRule rule : taddmRules) {
					rules.add(new TADDMNamingRule(rule));
				}
			}
		} catch (AttributeNotSetException anse) {
			// ignore, we explicitly check if this attribute is set
		}

		// get the parent's naming rules.
		try {
			if (metaData.hasBaseClass()) {
				rules.addAll(getAllNamingRules(metaData.getBaseClass()));
			}
		} catch (AttributeNotSetException anse) {
			// ignore, we explicitly check if this attribute is set
		}

		// sort by priority
		Collections.sort(rules);
		return rules;
	}

	/**
	 * Gets the meta-data for the provided class type.
	 * 
	 * @param cdmClassType
	 *            the class type.
	 * @return the meta-data
	 * @throws ApiException
	 *             if a problem occurs.
	 */
	private ObjectClass getMetaData(String cdmClassType) throws ApiException {
		ObjectClass metaData = null;
		try {
			metaData = api.getMetaData(getShortName(cdmClassType));
		} catch (ApiException ex) {
			// This MAY mean that the concrete class has a naming conflict
			// (another class with the same name but a different prefix).
			// Thus, we will try using its fully qualified name. Please note,
			// that using the qualified name will throw an Exception in the
			// general case.
			metaData = api.getMetaData(getFullyQualifiedName(cdmClassType));
		}
		return metaData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getFullyQualifiedName(String cdmClassName) {
		return CDMConstants.TADDM_CDM_NAMESPACE + cdmClassName;
	}

}