/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.query;

import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_RELATIONSHIP_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_SOURCE_CI_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.CDM_TARGET_CI_PREFIX;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_SOURCE_NAME;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_TARGET_NAME;

import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.connector.taddm.TADDMConnector;
import com.ibm.di.connector.taddm.cdm.TADDMMetaData;
import com.ibm.di.entry.NameTokenizer;
import com.ibm.icu.util.StringTokenizer;

/**
 * Filters a MQL query removing all CDM tokens and replacing them with their
 * corresponding TADDM names.
 * 
 */
public class CDM2TADDMQueryFilter implements QueryFilter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * The characters used for splitting the MQL to tokens.
	 */
	private static final String SPLIT_CHARACTERS = "\t\n\r\f !=><>,[]";

	/**
	 * The class type of all not fully qualified attributes (without class
	 * prefix).
	 */
	private String lastClassType;

	/**
	 * Meta-data.
	 */
	private TADDMMetaData metaData;

	/**
	 * Tokenizer used for parsing attribute names.
	 */
	private NameTokenizer nameTokenizer;

	/**
	 * Constructor.
	 * 
	 * @param metaData
	 *            the meta-data.
	 */
	public CDM2TADDMQueryFilter(TADDMMetaData metaData) {
		this.metaData = metaData;
		nameTokenizer = new NameTokenizer();
		nameTokenizer.setEscapeChar('\\');
	}

	/**
	 * {@inheritDoc}
	 */
	public String filter(String query) throws Exception {
		int fromIndex = query.indexOf(" FROM ");
		String result = null;
		if (fromIndex >= 0) {
			result = filterString(query.substring(fromIndex + 1));
			result = filterString(query.substring(0, fromIndex)) + " " + result;
		} else {
			result = filterString(query);
		}
		result = result.replace(CDM_ID_SYSTEM_ATTRIBUTE, "guid");
		return result;
	}

	/**
	 * Filters the provided string and returns the result.
	 * 
	 * @param inputString
	 *            input string.
	 * @return filtered string.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private String filterString(String inputString) throws Exception {
		StringBuilder result = new StringBuilder(inputString.length() / 2);
		StringTokenizer tokenizer = new StringTokenizer(inputString, SPLIT_CHARACTERS, true);
		while (tokenizer.hasMoreElements()) {
			final char SEPARATOR = '.';
			String cdmString = tokenizer.nextToken();

			if (isCDMToken(cdmString)) {
				nameTokenizer.setName(cdmString);

				String cdmToken = null;
				boolean multipleTokens = false;
				while ((cdmToken = nameTokenizer.getNextToken(SEPARATOR)) != null) {
					if (multipleTokens) {
						result.append(".");
					}

					if (cdmToken.startsWith(CDM_PREFIX) || cdmToken.startsWith("*")) {
						// CI class type, Relationship class type or
						// explicit attribute
						cdmToken = CDMUtils.removePrefix(cdmToken);
						String param = null;
						try {
							param = metaData.getTADDMClassType(cdmToken);
							lastClassType = param;
						} catch (IllegalArgumentException iae) {
							param = metaData.getTADDMExplicitAttributeName(cdmToken);
						}
						result.append(param);
					} else if (cdmToken.startsWith(CDM_RELATIONSHIP_PREFIX)) {
						if (lastClassType == null) {
							throw new Exception(getMessage("TADDM.CONN.INCORRECT.MQL.NO.CLASS.TYPE"));
						}
						// implicit attribute
						String relationshipType = CDMUtils.removePrefix(cdmToken);
						cdmToken = nameTokenizer.getNextToken(SEPARATOR);
						if (cdmToken == null
								|| (!cdmToken.startsWith(CDM_SOURCE_CI_PREFIX) && !cdmToken.startsWith(CDM_TARGET_CI_PREFIX))) {
							throw new Exception(getMessage("TADDM.CONN.INCORRECT.MQL.INVALID.IMPLICIT.ATTRIBUTE"));
						}
						boolean isForward = cdmToken.startsWith(CDM_TARGET_CI_PREFIX);
						String relatedClassType = CDMUtils.removePrefix(cdmToken);
						result.append(metaData.getTADDMImplicitAttributeName(lastClassType, relationshipType, relatedClassType,
								isForward));

					} else if (cdmToken.startsWith(CDM_SOURCE_CI_PREFIX) || cdmToken.startsWith(CDM_TARGET_CI_PREFIX)) {
						// Handle relationship

						if (lastClassType == null) {
							throw new Exception(getMessage("TADDM.CONN.INCORRECT.MQL.NO.CLASS.TYPE"));
						}
						if (metaData.isRelationship(lastClassType)) {
							result.append(cdmToken.startsWith(CDM_SOURCE_CI_PREFIX) ? TADDM_SOURCE_NAME : TADDM_TARGET_NAME);
						} else {
							throw new Exception(getMessage("TADDM.CONN.NOT.VALID.RELATIONSHIP", lastClassType));
						}
					}
					multipleTokens = true;
				}
			} else {
				result.append(cdmString);
			}
		}
		return result.toString().trim();
	}

	/**
	 * Check if the provided token has a 'cdm' prefix.
	 * 
	 * @param token
	 *            to be checked.
	 * @return <b>true</b> if the string is a valid 'cdm' token, <b>false</b>
	 *         otherwise.
	 */
	private boolean isCDMToken(String token) {
		return token.startsWith(CDM_PREFIX) || token.startsWith(CDM_RELATIONSHIP_PREFIX) || token.startsWith(CDM_SOURCE_CI_PREFIX)
				|| token.startsWith(CDM_TARGET_CI_PREFIX);
	}

	/**
	 * Gets a localized message using the provided key and adding the available
	 * values.
	 * 
	 * @param key
	 *            the message's key.
	 * @param values
	 *            the values to be added to the message.
	 * @return the formatted localized string.
	 */
	private String getMessage(String key, Object... values) {
		return TADDMConnector.L10N.getString(key, values);
	}

}