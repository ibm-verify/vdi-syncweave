/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.script.ScriptEngine;

/**
 * This class is used by an AssemblyLine
 */
public class SwitchComponent extends BranchingComponent {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	// The value of the last expression evaluation
	private String lastExpression;

	private BranchCondition condition;

	private String caseExecutedBy;

	private SwitchComponent parentSwitch;

	private boolean isSwitch;

	/**
	 * Constructor for the BranchingComponent object
	 * 
	 * @param parent
	 *            The AssemblyLine that contains this SwitchComponent
	 * @param name
	 *            The name of this SwitchComponent
	 * @param config
	 *            The configuration for this SwitchComponent
	 */
	public SwitchComponent(AssemblyLine parent, String name,
			BranchingConfig config) throws Exception {
		super(parent, name, config);
		isSwitch = config.getBranchType() == BranchingConfig.BRANCH_SWITCH;
	}

	/**
	 * Initializes this SwitchComponent
	 * 
	 */
	public void initialize() throws Exception {
		super.initialize();
		condition = (BranchCondition) branchingConfig.getConditions()
				.getConfig(0);
	}

	/**
	 * Gets the type attribute of the BranchingComponent object
	 * 
	 * @return The type value
	 */
	public int getType() {
		return isSwitch ? ServerConstants.TYPE_SWITCH
				: ServerConstants.TYPE_CASE;
	}

	/**
	 * Return true/false if this component should be executed. Return true if
	 * this is a SWITCH, or if the expression in this CASE matches the
	 * expression in the containing SWITCH
	 * 
	 * @param work
	 *            The work Entry
	 * @return see above
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		if (isSwitch) {
			ScriptEngine engine = parent.getScriptEngine();
			try {
				engine.pushStackFrame();
				engine.declareBean("work", work);
				lastExpression = psc.substitute(condition.getRightHand(), "work",
					work);
			} finally {
				engine.popStackFrame();
			}
			caseExecutedBy = null;
			if (log.getDebug()) {
				log.debug(sResHash.getString("switch.component.expression",
						new Object[] { getName(), lastExpression }));
			}
			stats.switches();
			return true;
		}

		// if the AL is simulating and the component is in Disabled simulation
		// state then set it to false
		boolean exec = !(((AssemblyLine) parent).isSimulating() && getSimulatingState()
				.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE));

		if ("*".equals(condition.getOperator())) {
			// Default case
			exec = exec && !getParentSwitch().getCaseExecuted();
		} else {
			// Update left hand size with SWITCH value and compare with our own
			String a = getParentSwitch().getLastExpression();
			String b = psc.substitute(condition.getRightHand(), "work", work);

			if (log.getDebug()) {
				log.debug(sResHash.getString("case.component.a.b",
						new Object[] { getName(), a, b }));
			}

			if (a == null || b == null)
				exec = false;
			else
				exec = exec && a.equals(b);
		}

		if (exec) {
			stats.branchtrue();
			getParentSwitch().setCaseExecutedBy(getName());
		} else {
			stats.branchfalse();
		}

		setSuccessful(exec);
		return exec;
	}

	/**
	 * This method does nothing
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {
	}

	/**
	 * Returns the value from the last evaluated expression. This is used by the
	 * CASE components when they compare to their own values.
	 */
	public String getLastExpression() {
		return lastExpression;
	}

	/**
	 * Returns the SWITCH component to which this CASE belongs.
	 */
	public SwitchComponent getParentSwitch() {
		if (parentSwitch == null) {
			if (getParentIndex() < 0)
				return null;
			parentSwitch = (SwitchComponent) parent.getStateConnectors().get(getParentIndex());
		}
		return parentSwitch;
	}

	/**
	 * Returns true if any contained CASE component executed this cycle
	 * 
	 * @return true if any contained CASE component executed this cycle
	 */
	public boolean getCaseExecuted() {
		return (caseExecutedBy != null);
	}

	/**
	 * Returns the name of the CASE component that executed this cycle
	 * 
	 * @return the name of the CASE component that executed this cycle
	 */
	public String getCaseExecutedBy() {
		return caseExecutedBy;
	}

	/**
	 * Sets the name of the CASE component that executed this cycle (called by
	 * CASE components)
	 * 
	 * @param name
	 *            The name of the CASE component that executed this cycle
	 */
	public void setCaseExecutedBy(String name) {
		caseExecutedBy = name;
	}

}
