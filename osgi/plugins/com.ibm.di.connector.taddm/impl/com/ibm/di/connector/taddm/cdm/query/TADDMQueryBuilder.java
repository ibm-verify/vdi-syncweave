/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.query;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_CYCLE_SYSTEM_ATTRIBUTE;

import java.util.Iterator;

import com.ibm.di.function.UserFunctions;
import com.ibm.di.server.SearchCriteria;
import com.ibm.di.server.SearchCriteria.rscSearch;

/**
 * Class used for constructing the TADDM MQL query based on the user provided
 * data.
 * 
 */
public class TADDMQueryBuilder {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Default value for the depth parameter.
	 */
	private static final int UNKNOWN = -1;

	/**
	 * A filter used for removing unwanted tokens once the query is assembled.
	 */
	private QueryFilter filter;

	/**
	 * The basic class type of the query.
	 */
	private String classType;

	/**
	 * The query depth.
	 */
	private int depth = UNKNOWN;

	/**
	 * A full MQL select statement.
	 */
	private String mqlSelect;

	/**
	 * A search criteria to be used as am MQL where clause.
	 */
	private SearchCriteria criteria;

	/**
	 * Constructor.
	 * 
	 * @param filter
	 *            the filter object.
	 */
	public TADDMQueryBuilder(QueryFilter filter) {
		this.filter = filter;
	}

	/**
	 * Sets a class type to be used by the Builder.
	 * 
	 * @param classType
	 *            a class type.
	 */
	public void setClassType(String classType) {
		this.classType = classType;
	}

	/**
	 * Sets a query depth to be used by the Builder.
	 * 
	 * @param depth
	 *            the depth which this query should reach.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * Sets an MQL Select query to be used by the Builder.
	 * 
	 * @param mqlSelect
	 *            an MQL Select query.
	 */
	public void setMQLSelect(String mqlSelect) {
		this.mqlSelect = mqlSelect;
	}

	/**
	 * Sets a Search Criteria to be used by the Builder.
	 * 
	 * @param criteria
	 *            the input criteria.
	 */
	public void setSearchCriteria(SearchCriteria criteria) {
		this.criteria = criteria;
	}

	/**
	 * Creates an MQL query based on the provided data.
	 * <p>
	 * <b>Note:</b> Please note that either a class type or an MQL select
	 * statement should be provided. If both are set the MQL select will take
	 * precedence.<br>
	 * Also, if a search criteria is provided for an MQL select statement that
	 * already has a WHERE clause the new conditions are added to it using an
	 * AND logical operator.
	 * </p>
	 * 
	 * @return the created string.
	 * @throws Exception
	 *             if not enough data is provided to the builder, or there is a
	 *             problem during the filtering phase.
	 */
	public String buildQuery() throws Exception {
		StringBuilder query = new StringBuilder();
		if (mqlSelect != null) {
			if (depth == 0) {
				query.append("SELECT guid");
				int fromIndex = mqlSelect.indexOf(" FROM ");
				if (fromIndex < 0) {
					fromIndex = 0;
				}
				query.append(mqlSelect.substring(fromIndex));
			} else {
				query.append(mqlSelect);
			}
		} else if (classType != null) {
			if (depth == 0) {
				query.append("SELECT guid ");
			} else {
				query.append("SELECT * ");
			}
			query.append("FROM ");
			query.append(classType);
		} else {
			throw new NotEnoughQueryParametersException();
		}

		if (criteria != null) {
			if (query.indexOf(" WHERE ") >= 0) {
				query.append(" AND");
			} else {
				query.append(" WHERE");
			}
			query.append(createWhereClause(criteria));
		}
		return filter.filter(query.toString());
	}

	/**
	 * Creates a WHERE clause form the provided Search Criteria.
	 * 
	 * @param searchCriteria
	 *            criteria
	 * @return the generated WHERE clause.
	 */
	public static String createWhereClause(SearchCriteria searchCriteria) {
		final String WHERE_CLAUSE = "WHERE ";
		StringBuilder query = new StringBuilder(" ");
		String criteriaString = searchCriteria.getScriptFilter();
		if (criteriaString != null) {
			// remove starting WHERE clause
			criteriaString = criteriaString.trim();
			if (UserFunctions.startsWithIC(criteriaString, WHERE_CLAUSE)) {
				criteriaString = criteriaString.substring(WHERE_CLAUSE.length());
			}
			query.append(criteriaString);
		} else {
			Iterator<?> it = searchCriteria.getCriteria().iterator();
			if (it.hasNext()) {
				query.append(createCriterion((rscSearch) it.next()));
			}
			while (it.hasNext()) {
				query.append(searchCriteria.getType() == SearchCriteria.SEARCH_OR ? " OR " : " AND ");
				query.append(createCriterion((rscSearch) it.next()));
			}
		}
		return query.toString();
	}

	/**
	 * Converts the provided {@link rscSearch} to a MQL WHERE token.
	 * 
	 * @param rsc
	 *            the search criterion.
	 * @return the MQL equivalent.
	 */
	private static String createCriterion(SearchCriteria.rscSearch rsc) {
		String result = null;
		if (CDM_CLASSTYPE_SYSTEM_ATTRIBUTE.equalsIgnoreCase(rsc.name) || CDM_CYCLE_SYSTEM_ATTRIBUTE.equalsIgnoreCase(rsc.name)
				|| rsc.name.endsWith("." + CDM_CLASSTYPE_SYSTEM_ATTRIBUTE) || rsc.name.endsWith("." + CDM_CYCLE_SYSTEM_ATTRIBUTE)) {
			// These attributes are not supported in a TADDM MQL so as a
			// usability improvement we skip them when provided in the UI Link
			// Criteria. Note that if Advanced Link Criteria is used, they will
			// still be present.
			result = "";
		} else {
			result = rsc.name + " " + getOperator(rsc.match) + " '" + rsc.value + "'";
		}
		return result;
	}

	/**
	 * Converts the TDI match conditions to TADDM ones.
	 * 
	 * @param match
	 *            TDO match condition.
	 * @return the TADDM match condition.
	 */
	private static String getOperator(int match) {
		String op = null;

		switch (match) {
		case SearchCriteria.NOT_STRING:
			op = "!=";
			break;
		case SearchCriteria.SUBSTRING:
			op = "contains";
			break;
		case SearchCriteria.INITIAL_STRING:
			op = "starts-with";
			break;
		case SearchCriteria.FINAL_STRING:
			op = "ends-with";
			break;
		case SearchCriteria.GREATER_THAN:
			op = ">";
			break;
		case SearchCriteria.GREATER_THAN_OR_EQUAL:
			op = ">=";
			break;
		case SearchCriteria.LESS_THAN:
			op = "<";
			break;
		case SearchCriteria.LESS_THAN_OR_EQUAL:
			op = "<=";
			break;
		default:
			op = "==";
		}

		return op;
	}

}
