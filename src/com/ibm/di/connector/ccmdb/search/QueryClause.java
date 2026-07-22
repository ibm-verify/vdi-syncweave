/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.ccmdb.search;

import com.ibm.di.server.SearchCriteria;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class QueryClause {

	/**
	 * This is an utilities class representing an 'where' clause of an SQL statement.
	 * 
	 * @param columnName
	 * @param criterion
	 * @return QueryClause
	 */
	public static QueryClause getClause(String columnName, SearchCriteria.rscSearch criterion) {
		QueryClause clause = new QueryClause();
		switch (criterion.match) {
		case SearchCriteria.NOT_STRING:
			clause.setValue(criterion.value);
			clause.setExpression(" (" + columnName + " != ?) ");
			break;
		case SearchCriteria.SUBSTRING:
			clause.setValue("%" + criterion.value + "%");
			clause.setExpression(" (" + columnName + " LIKE ?) ");
			break;
		case SearchCriteria.INITIAL_STRING:
			clause.setValue(criterion.value + "%");
			clause.setExpression(" (" + columnName + " LIKE ?) ");
			break;
		case SearchCriteria.FINAL_STRING:
			clause.setValue("%" + criterion.value );
			clause.setExpression(" (" + columnName + " LIKE ?) ");
			break;
		case SearchCriteria.GREATER_THAN:
			clause.setValue(criterion.value );
			clause.setExpression(" (" + columnName + " > ?) ");
			break;
		case SearchCriteria.GREATER_THAN_OR_EQUAL:
			clause.setValue(criterion.value );
			clause.setExpression(" (" + columnName + " >= ?) ");
			break;
		case SearchCriteria.LESS_THAN:
			clause.setValue(criterion.value );
			clause.setExpression(" (" + columnName + " < ?) ");
			break;
		case SearchCriteria.LESS_THAN_OR_EQUAL:
			clause.setValue(criterion.value );
			clause.setExpression(" (" + columnName + " <= ?) ");
			break;
		default:
			clause.setValue(criterion.value );
			clause.setExpression(" (" + columnName + " = ?) ");
		}
		
		return clause;
	}	
	
	//-------------------------------------------------------------------------
	
	private String expression = null;
	private Object value = null;
	
	/**
	 * 
	 * @return String
	 */
	public String getExpression() {
		return expression;
	}
	
	/**
	 * 
	 * @param expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	/**
	 * 
	 * @return String
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
}
