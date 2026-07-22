/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a single item in a LinkCriteriaConfig\
 * @see LinkCriteriaConfig 
 */
public interface LinkCriteriaItem extends BaseConfiguration {

	public final static int EXACT = '=';

	public final static int LESS_THAN = '<';
	
	public final static int LESS_THAN_OR_EQUAL = '\u2264';
	
	public final static int GREATER_THAN = '>';

	public final static int GREATER_THAN_OR_EQUAL = '\u2265';
	
	public final static int SUBSTRING = '~';

	public final static int INITIAL_STRING = '^';

	public final static int FINAL_STRING = '$';

	public final static int NOT_STRING = '!';

	public final static String LC_EXACT = "equals";

	public final static String LC_LESS_THAN = "less than";

	public final static String LC_LESS_THAN_OR_EQUAL = "less than or equal";

	public final static String LC_GREATER_THAN = "greater than";

	public final static String LC_GREATER_THAN_OR_EQUAL = "greater than or equal";

	public final static String LC_SUBSTRING = "contains";

	public final static String LC_INITIAL = "starts with";

	public final static String LC_FINAL = "ends with";

	public final static String LC_NOT = "not equals";

	public final static int SEARCH_AND = 1;

	public final static int SEARCH_OR = 2;

	public final static int SEARCH_NOT = 3;

	public Object getAttribute();

	public void setAttribute(Object attribute);

	public Object getValue();

	public void setValue(Object value);

	public Object getOper();

	public void setOper(Object oper);

	public int getMatch();
}
