/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.query;

import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_IMPLICIT_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_SOURCE_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_SOURCE_NAME;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_TARGET_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_TARGET_NAME;

/**
 * Filters a MQL query removing $implicit $source and $target tokens and
 * replacing them, if necessary, with their corresponding TADDM names.
 * 
 */
public class DefaultQueryFilter implements QueryFilter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * {@inheritDoc}
	 */
	public String filter(String query) {
		query = query.replace(TADDM_IMPLICIT_ATTRIBUTE + ".", "");
		query = query.replace(TADDM_SOURCE_ATTRIBUTE + ".", TADDM_SOURCE_NAME + ".");
		query = query.replace(TADDM_TARGET_ATTRIBUTE + ".", TADDM_TARGET_NAME + ".");
		query = query.replace(CDM_ID_SYSTEM_ATTRIBUTE, "guid");

		return query;
	}

}
