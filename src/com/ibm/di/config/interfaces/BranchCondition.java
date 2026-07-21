/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration for a single BranchCondition in a BranchingConfig.
 * @see BranchingConfig.
 */
public interface BranchCondition extends BaseConfiguration {

	/**
	 * Condition operator. Both sides of the condition must be equal.
	 */
	public final static String BRANCH_EQUALS = "equals";

	/**
	 * Condition operator. The left side must contain the right side.
	 */
	public final static String BRANCH_CONTAINS = "contains";

	/**
	 * Condition operator. The left side must start with the string specified by
	 * the right side.
	 */
	public final static String BRANCH_STARTS_WITH = "startsWith";

	/**
	 * Condition operator. The left side must end with the string specified by
	 * the right side.
	 */
	public final static String BRANCH_ENDS_WITH = "endsWith";

	/**
	 * Condition operator. The left side must resolve to an Attribute that
	 * contains the right side as a value.
	 */
	public final static String BRANCH_HAS_VALUE = "hasValue";

	/**
	 * Condition operator. The left side must exists. There is no right side for
	 * this operator.
	 */
	public final static String BRANCH_EXISTS = "exists";

	/**
	 * Condition operator. The left side is less than the right side.
	 */
	public final static String BRANCH_LT = "less";

	/**
	 * Condition operator. The left side is less than or equal to the right
	 * side.
	 */
	public final static String BRANCH_LTE = "lessequal";

	/**
	 * Condition operator. The left side is greater than the right side.
	 */
	public final static String BRANCH_GT = "greater";

	/**
	 * Condition operator. The left side is greater than or equal to the right
	 * side.
	 */
	public final static String BRANCH_GTE = "greaterequal";

	/**
	 * @return The left-hand side of the conditional expression.
	 */
	public String getLeftHand();

	/**
	 * Set the left-hand side of the conditional expression.
	 * 
	 * @param str
	 *            The left-hand side of the conditional expression.
	 */
	public void setLeftHand(String str);

	/**
	 * @return The operator of the conditional expression, e.g.
	 *         {@link #BRANCH_EQUALS}.
	 */
	public String getOperator();

	/**
	 * Set the operator of the conditional expression, e.g.
	 * {@link #BRANCH_EQUALS}.
	 * 
	 * @param str
	 *            The name of the conditional operator.
	 */
	public void setOperator(String str);

	/**
	 * @return The right-hand side of the conditional expression.
	 */
	public String getRightHand();

	/**
	 * Set the right-hand side of the conditional expression.
	 * 
	 * @param str
	 *            The right-hand side of the conditional expression.
	 */
	public void setRightHand(String str);

	/**
	 * @return The case sensitivity flag.
	 */
	public boolean getCaseSensitive();

	/**
	 * Set whether the condition (e.g. equality) is case sensitive.
	 * 
	 * @param cs
	 *            Whether the condition is case sensitive.
	 */
	public void setCaseSensitive(boolean cs);

	/**
	 * @return Whether the condition is negated.
	 */
	public boolean getNegate();

	/**
	 * Set whether the condition is negated.
	 * 
	 * @param negate
	 *            Whether the condition is negated.
	 */
	public void setNegate(boolean negate);
	
	/**
	 * @return The match any flag.
	 */
	public boolean getMatchAny();

	/**
	 * Sets the match any flag.
	 * 
	 * @param matchAny
	 *            The value of the match any flag.
	 */
	public void setMatchAny(boolean matchAny);
}
