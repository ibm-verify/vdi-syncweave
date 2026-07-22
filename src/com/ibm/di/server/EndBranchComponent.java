/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.List;

import com.ibm.di.config.interfaces.BranchingConfig;

/**
 * The EndBranchComponent is an internal component that is associated with a
 * BranchComponent. Its job is to check if we are in an if/else-if/else
 * construct and skip any else-if/else type branch components based on the
 * execute status of the associated BranchComponent.
 * 
 */

public class EndBranchComponent extends AssemblyLineComponent {

	/**
	 * The reference to our associated branching component.
	 */
	private BranchingComponent bc;

	/**
	 * The Constructor for the EndBranchComponent
	 * 
	 * @param parent
	 *            The AssemblyLine that contains this EndBranchComponent
	 * @param bc
	 *            The BranchingComponent that this is an end for
	 */
	public EndBranchComponent(AssemblyLine parent, BranchingComponent bc) {
		setName(bc.getName() + ".endbranch");
		this.bc = bc;
		this.parent = parent;
		this.log = parent.getLog();
	}

	/**
	 * This method does nothing.
	 */
	public void initialize() {
	}

	/**
	 * This method always returns true. Our test is performed in the add
	 * operation.
	 * 
	 * @param work
	 *            The work Entry. Not used.
	 * @return true
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) {
		return true;
	}

	/**
	 * This method returns the AL component type (ServerConstants.TYPE_BRANCH)
	 * 
	 * @return The type value (ServerConstants.TYPE_BRANCH)
	 */
	public int getType() {
		return ServerConstants.TYPE_BRANCH;
	}

	/**
	 * This method is called by the hosting AssemblyLine. Check for
	 * if/else-if/else construct and set the next connector to execute based on
	 * branch type and execution status.
	 * 
	 * @param meta
	 *            The work entry (not used)
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {

		// If this is the ELSE branch then we should the last one
		if (bc.getBranchType() == BranchingConfig.BRANCH_ELSE
				|| bc.getBranchType() == BranchingConfig.BRANCH_SWITCH
				|| bc.getBranchType() == BranchingConfig.BRANCH_CASE) {
			return;
		}

		// skip remaining else_if/else components if we did execute
		if (!bc.isExecuted())
			return;

		List<AssemblyLineComponent> connectors = parent.getStateConnectors();

		int next = getEndComponentIndex() + 1;
		while (next < connectors.size()) {
			AssemblyLineComponent nextBranch = connectors.get(next);
			if (!(nextBranch instanceof BranchingComponent))
				break;
			int type = ((BranchingComponent) nextBranch).getBranchType();
			if (type != BranchingConfig.BRANCH_ELSE
					&& type != BranchingConfig.BRANCH_ELSEIF)
				break;
			next = nextBranch.getEndComponentIndex() + 1;
		}

		parent.getAlState().setNext(next, true);
	}

	/**
	 * Returns the type of the original branch to which this end branch belongs.
	 * 
	 * @return the type of the parrent branch
	 */
	public int getParentBranchType() {
		return bc.getBranchType();
	}

	/**
	 * Any errors during execution of add() ends up here. We don't have error
	 * hooks so we just rethrow the exception.
	 * 
	 * @param oper
	 *            Not used.
	 * @param e
	 *            Exception
	 * @param meta
	 *            Not used.
	 * @exception Exception
	 *                The "e" exception
	 */
	public void handleException(String oper, Throwable e,
			com.ibm.di.entry.Entry meta) throws Exception {
		e.printStackTrace();
		if (e instanceof Exception) {
			throw (Exception) e;
		} else {
			throw new Exception(e);
		}
	}

	/**
	 * NOOP. This component has no triggers.
	 * 
	 * @param oper
	 *            Not used.
	 * @param work
	 *            Not used.
	 * @param conn
	 *            Not used.
	 * @return false
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) {
		return false;
	}

	/**
	 * NOOP. This component has no triggers.
	 * 
	 * @param oper
	 *            Not used.
	 * @param work
	 *            Not used.
	 * @return false
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) {
		return false;
	}

	/**
	 * Release resources.
	 * 
	 * @exception Exception
	 *                None
	 */
	public void close() throws Exception {
		bc = null;
		parent = null;
		log = null;
	}
}
