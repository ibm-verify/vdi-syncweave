/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

/**
 * The configuration of an AssemblyLine Branch component.
 */
public interface BranchingConfig extends ContainerConfig {

	/**
	 * BRANCH_IF - This is the pre 6.1 branching config
	 */
	public static int BRANCH_IF = 0;

	/**
	 * BRANCH_ELSEIF - This is an ELSE-IF branch with conditions
	 */
	public static int BRANCH_ELSEIF = 1;

	/**
	 * BRANCH_ELSE- The last part of an if/else sequence with no conditions
	 */
	public static int BRANCH_ELSE = 2;

	/**
	 * BRANCH_SWITCH - The container of a number of BRANCH_CASE objects
	 */
	public static int BRANCH_SWITCH = 3;

	/**
	 * BRANCH_SWITCH - The child container of a BRANCH_SWITCH object
	 */
	public static int BRANCH_CASE = 4;

	/**
	 * Returns the total number of components in this Branch. This includes all
	 * components plus components in sub branches.
	 * 
	 * @return total number of components in this Branch
	 */
	public int totalSize();

	/**
	 * @return The total number of LoopConfig items in this Branch and its child
	 *         branches/loops. Since BranchingConfig also add an extra invisible
	 *         EndBranch component we also count these.
	 */
	public int numberLoops();

	/**
	 * @return The conditions container.
	 */
	public ContainerConfig getConditions();

	/**
	 * @return A new populated condition config.
	 */
	public BranchCondition newCondition();

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

	/**
	 * @return The branch type (e.g. BRANCH_IF, BRANCH_ELSEIF, BRANCH_ELSE,
	 * BRANCH_SWITCH, BRANCH_CASE) Returns BRANCH_IF if the parameter is not
	 * set.
	 */
	public int getBranchType();

	/**
	 * Sets the branch type (e.g. BRANCH_IF, BRANCH_ELSEIF, BRANCH_ELSE,
	 * BRANCH_SWITCH, BRANCH_CASE).
	 * 
	 * @param type
	 *            The type of the branch component.
	 */
	public void setBranchType(int type);
}
