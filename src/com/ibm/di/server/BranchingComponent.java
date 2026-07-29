/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.util.Date;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.SimulationConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.util.ParameterSubstitutionCache;
import com.ibm.jscript.IValue;
import com.ibm.jscript.types.FBSNull;

/**
 * This class is used by the AssemblyLine for branches
 */
public class BranchingComponent extends AssemblyLineComponent {

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * The configuration
	 */
	protected BranchingConfig branchingConfig;

	/**
	 * The return value from script execution
	 */
	public Object value = null;

	/**
	 * The script attribute of the BranchingComponent.
	 */
	private String script;

	/**
	 * The ScriptEngine object of the AssemblyLine.
	 */
	private ScriptEngine engine;

	/**
	 * A boolean flag.
	 */
	private boolean executed;

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Helper object for parameter substitutions
	 */
	protected ParameterSubstitutionCache psc = new ParameterSubstitutionCache();

	private com.ibm.di.entry.Entry scriptObject = new com.ibm.di.entry.Entry();
	private String debugLine;
	
	/**
	 * Constructor.
	 */
	public BranchingComponent() {
	}

	/**
	 * Constructor for the BranchingComponent object
	 * 
	 * @param parent
	 *            The AssemblyLine that contains this BranchingComponent
	 * @param name
	 *            The name of this BranchingComponent
	 * @param config
	 *            The configuration for this BranchingComponent
	 * @throws Exception
	 *             if problem occurs
	 */
	public BranchingComponent(AssemblyLine parent, String name,
			BranchingConfig config) throws Exception {
		this.branchingConfig = config;
		this.parent = parent;
		setName(name);

		log = new Log(parent.getLog());
		log.setDebug(config.getDebug(false));
		log.setPrefix("[" + getName() + "] ");

		script = config.getScript();
		if ("".equals(script) || config.getBooleanParameter("ScriptDeleted", false))
			script = null;

		engine = parent.getScriptEngine();
		psc.put("op-entry", parent.getOpEntry());
		psc.put("config", getBaseConfiguration());
		psc.put("task", parent);

		stats = new TaskStatistics();
	}

	/**
	 * Initializes the component.
	 * 
	 * @throws Exception
	 *             Not really, but subclasses may throw Exceptions
	 */
	public void initialize() throws Exception {
		scriptObject.setAttribute("AssemblyLine", parent.getName());
		scriptObject.setAttribute("Component", name);
		debugLine = name + "#0";
	}

	/**
	 * This method closes the script engine.
	 * 
	 * @throws Exception
	 *             if problem occurs
	 */
	public void close() throws Exception {
		engine = null;
		script = null;
		branchingConfig = null;
		parent = null;
		log = null;
	}

	/**
	 * This method returns the Connector configuration
	 * 
	 * @return null
	 */
	public ConnectorConfig getConfiguration() {
		return null;
	}

	/**
	 * This method returns the BaseConfiguration
	 * 
	 * @return The configuration for this component
	 */
	public BaseConfiguration getBaseConfiguration() {
		return branchingConfig;
	}

	/**
	 * Gets the type attribute of the BranchingComponent object
	 * 
	 * @return ServerConstants.TYPE_BRANCH
	 */
	public int getType() {
		return ServerConstants.TYPE_BRANCH;
	}

	/**
	 * Return true or false, depending on what the expression in the branch
	 * evaluates to This method is called by the AssemblyLine.
	 * 
	 * @param work
	 *            The work Entry
	 * @return The boolean value of the expression
	 * @exception Exception
	 *                Any Exception that might be thrown
	 */
	public boolean willExecute(com.ibm.di.entry.Entry work) throws Exception {
		executed = checkWillExecute(work)
				&& !(parent.isSimulating() && getSimulatingState()
						.equalsIgnoreCase(SimulationConfig.SIM_DISABLED_STATE));
		if (executed)
			stats.branchtrue();
		else
			stats.branchfalse();

		return executed;
	}

	/**
	 * Evaluates the expression in this branch
	 * 
	 * @param work
	 *            The work Entry
	 * @return The boolean value of the expression
	 * @exception Exception
	 *                Any Exception that might be thrown during script execution
	 */
	public boolean checkWillExecute(com.ibm.di.entry.Entry work)
			throws Exception {

		// No conditions for ELSE clause
		if (branchingConfig.getBranchType() == BranchingConfig.BRANCH_ELSE)
			return true;

		boolean match = evaluateConditions(work);

		if (!match && !branchingConfig.getMatchAny()) {
			// If conditions explicitly says no, then ignore the script unless
			// we do match any
			return false;
		} else if (match && branchingConfig.getMatchAny()) {
			// No need to exec script if we get here on a matchany
			if (branchingConfig.getConditions().size() > 0)
				return true;
		}

		// No script
		if (script == null) {
			return match;
		}

		// Evaluate script condition
		try {
			engine.pushStackFrame();
			add1(work);

			if (value == null) {
				throw new Exception(sResHash
						.getString("conditional.script.didnt.do.retvalue"));
			} else if (value instanceof String) {
				return Boolean.valueOf((String) value);
			} else if (!(value instanceof Boolean)) {
				throw new Exception(sResHash.getString("invalid.return.value",
						new Object[] { value, value.getClass().getName() }));
			}
		} catch (Exception e) {

			if (!e.getClass().getName().startsWith("com.ibm.di.exceptions.")) {
				com.ibm.di.entry.Entry err = new com.ibm.di.entry.Entry();
				err.setAttribute("status", "fail");
				err.setAttribute("exception", e);
				err.setAttribute("message", e.getMessage());
				err.setAttribute("class", e.getClass().getName());
				err.setAttribute("operation", "execute");
				err.setAttribute("connectorname", getName());

				engine.declareStaticBean("error", err);
			}
			throw e;
		} finally {
			engine.popStackFrame();
		}

		return (Boolean) value;
	}

	/**
	 * Returns the result of the test conditions
	 * 
	 * @param work
	 *            The work Entry
	 * @return The evaluation of the simple test conditions *
	 * @throws Exception
	 *             if problem occurs
	 */
	public boolean evaluateConditions(com.ibm.di.entry.Entry work)
			throws Exception {
		boolean match = true;
		ContainerConfig conditions = branchingConfig.getConditions();
		for (int i = 0; i < conditions.size(); i++) {
			if (conditions.getConfig(i) instanceof BranchCondition) {
				BranchCondition c = (BranchCondition) conditions.getConfig(i);
				boolean ret = evaluateCondition(work, c, i);
				if (c.getNegate())
					ret = !ret;

				if (!ret && !branchingConfig.getMatchAny())
					return false;
				else if (ret && branchingConfig.getMatchAny())
					return true;
				else if (!ret)
					match = false;

			}
		}

		return match;
	}

	/**
	 * Returns the result of a single condition
	 * 
	 * @param work
	 *            The work Entry
	 * @param c
	 *            One BranchCondition
	 * @param index
	 *            The number of this BranchCondition, for error messages throws
	 *            Exception if the left hand side of the condition is null
	 * @return the result of the conditions
	 * 
	 * @throws Exception
	 *             if problem occurs
	 */
	public boolean evaluateCondition(com.ibm.di.entry.Entry work,
			BranchCondition c, int index) throws Exception {
		String oper = c.getOperator();
		String leftHand = c.getLeftHand();
		String value = c.getRightHand();

		// CE may have added an empty condition, just say that it is true.
		if (index == 0 && 
			(leftHand == null || leftHand.length()==0) &&
			(value == null || value.length() == 0) &&
			BranchCondition.BRANCH_EQUALS.equals(oper) ) 
			return true;
			
		if (leftHand == null) {
			throw new Exception(sResHash
					.getString("null.attribute.name.in.condnumber", Integer
							.valueOf(index)));
		}

		Object[] leftValues = null;
		Object leftObject;
				
		if (ParameterSubstitutionCache.isExpression(leftHand)) {
			leftObject = psc.substitute(leftHand, "work", work);
			if (leftObject == null)
				return false;
		} else {
			Attribute attr = work.getAttribute(leftHand);
			if (attr == null)
				return false;
			
			leftObject = attr.getValue(0);

			if (leftObject == null)
				return BranchCondition.BRANCH_EXISTS.equals(oper);
			
			// -- match any of the left hand values
			if(c.getMatchAny())
				leftValues = attr.getValues();
		}

		if (BranchCondition.BRANCH_HAS_VALUE.equals(oper)
				|| BranchCondition.BRANCH_EXISTS.equals(oper))
			return true; // The attribute exists and has a value.

		if (value == null)
			return false;

		Object rightObject = value;

		if (ParameterSubstitutionCache.isExpression(value)) {
			rightObject = psc.substitute(value, "work", work);
		} else if (value.startsWith("$")) {
			Object temp = work.getObject(value.substring(1));
			if (temp != null)
				rightObject = temp;
		} else if (value.startsWith("@")) {
			Attribute a = work.getAttribute(value.substring(1));
			if (a != null) {
				for (int i = 0, n = a.size(); i < n; i++) {
					if(leftValues != null) {
						for(Object obj : leftValues) {
							if (doCompare(obj, oper, a.getValue(i), c.getCaseSensitive()))
								return true;
						}
					} else {
						if (doCompare(leftObject, oper, a.getValue(i), c.getCaseSensitive()))
							return true;
					}
				}
				return false;
			}
		}
		
		if(leftValues != null) {
			for(Object obj : leftValues) {
				if (doCompare(obj, oper, rightObject, c.getCaseSensitive()))
					return true;
			}
			return false;
		} else {
			return doCompare(leftObject, oper, rightObject, c.getCaseSensitive());
		}
	}
	
	/**
	 * Checks if the condition specified by the parameters is fulfilled.
	 * 
	 * @param leftObject
	 *            parameter that will be checked
	 * @param oper
	 *            operation between leftObject and rightObject (e.g. equals, contains)
	 * @param rightObject
	 *            the value against which the leftObject is checked
	 * @param cs
	 *            <code>true</code> if the comparison is case sensitive,
	 *            otherwise <code>false</code>
	 * @return <code>true</code> if the operation returns true, otherwise <code>false</code>
	 */
	private boolean doCompare(Object leftObject, String oper, Object rightObject, boolean cs) {
		if (rightObject == null) {
			// we are asked to compare something to null. Maybe log an error or
			// throw an Exception
			return false;
		}

		String str = leftObject.toString();
		String value = rightObject.toString();
		if (BranchCondition.BRANCH_EQUALS.equals(oper)) {
			if (cs)
				return str.equals(value);
			else
				return str.equalsIgnoreCase(value);

		} else if (BranchCondition.BRANCH_CONTAINS.equals(oper)) {
			if (cs)
				return (str.indexOf(value) != -1);
			else
				return UserFunctions.containsIC(str, value);

		} else if (BranchCondition.BRANCH_STARTS_WITH.equals(oper)) {
			if (cs)
				return str.startsWith(value);
			else
				return UserFunctions.startsWithIC(str, value);

		} else if (BranchCondition.BRANCH_ENDS_WITH.equals(oper)) {
			if (cs)
				return str.endsWith(value);
			else
				return UserFunctions.endsWithIC(str, value);
		}

		// The rest of the operations are comparisons.
		int result = 0;
		boolean gotResult = false;
		if (leftObject instanceof Number) {
			try {
				result = compareNum((Number)leftObject, rightObject);
				gotResult = true;
			} catch (NumberFormatException e) {
				gotResult = false;
			}
		}

		if (!gotResult) {
			if (leftObject instanceof Date && rightObject instanceof Date) {
				result = ((Date)leftObject).compareTo((Date)rightObject);
			} else {
				result = cs ? str.compareTo(value) : str.compareToIgnoreCase(value);
			}
		}

		if (BranchCondition.BRANCH_LT.equals(oper)) {
			return (result < 0);

		} else if (BranchCondition.BRANCH_LTE.equals(oper)) {
			return (result <= 0);

		} else if (BranchCondition.BRANCH_GT.equals(oper)) {
			return (result > 0);

		} else if (BranchCondition.BRANCH_GTE.equals(oper)) {
			return (result >= 0);

		}

		return false; // Cannot happen
	}

	/**
	 * Compare two numbers
	 * @param left The left operand
	 * @param right The right operand
	 * @return -1 if the left operand is smallest, 0 if equal, 1 if the left is greater.
	 * @throws NumberFormatException if the right operand is not a number
	 */
	private int compareNum(Number left, Object right) throws NumberFormatException {
		if (left instanceof Long) {
			try {
				long l1 = left.longValue();
				long l2 = (right instanceof Number) ? ((Number) right).longValue() : Long.valueOf(right.toString());
				return (l1 < l2) ? -1 : (l1 == l2) ? 0 : 1;
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}
		}

		double d1 = left.doubleValue();
		double d2 = (right instanceof Number) ? ((Number) right).doubleValue() : Double.valueOf(right.toString());
		return (d1 < d2) ? -1 : (d1 == d2) ? 0 : 1;
	}

	/**
	 * @deprecated
	 * @return an int count
	 */
	public int componentCount() {
		return branchingConfig.totalSize() + branchingConfig.numberLoops();
	}

	/**
	 * Do nothing in the add method
	 * 
	 * @param meta
	 *            parameter
	 * 
	 * @throws Exception
	 *             if problem occurs
	 */
	public void add(com.ibm.di.entry.Entry meta) throws Exception {
	}

	/**
	 * This method executes the script.
	 * 
	 * @param meta
	 *            the work Entry
	 * 
	 * @throws Exception
	 *             if problem occurs
	 */
	public void add1(com.ibm.di.entry.Entry meta) throws Exception {

		this.value = FBSNull.nullValue;

		engine.declareBean("work", meta);
		engine.declareBean("thisConnector", this);
		engine.declareBean("thisComponent", this);
		engine.declareBean("ret", this);
		engine.declareBean(AttributeMapping.SCRIPT_OBJECT, scriptObject);

		parent.debugBreak(debugLine);
		
		IValue v = engine.interpret(script, false);
		if (value == FBSNull.nullValue) {
			if (v != null)
				value = v.toJavaObject();
			else
				value = null;
		}
	}

	/**
	 * Calls the hook named oper.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper) {
		return false;
	}

	/**
	 * Calls the hook named oper, declaring work as the corresponding bean. The
	 * trigger function calls one of the AssemblyLine hooks defined for this
	 * Connector using the provided work.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @param work
	 *            This will be the work bean in the hook.
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work) {
		return false;
	}

	/**
	 * Calls the hook named oper, declaring work and conn as the corresponding
	 * beans. The trigger function calls one of the AssemblyLine hooks defined
	 * for this Connector using the provided conn/work.
	 * 
	 * @param oper
	 *            Name of the hook to call.
	 * @param work
	 *            This will be the work bean in the hook.
	 * @param conn
	 *            This will be the conn bean in the hook
	 * @return True if the hook was executed, false if the hook is not defined
	 *         or disabled.
	 */
	public boolean trigger(String oper, com.ibm.di.entry.Entry work,
			com.ibm.di.entry.Entry conn) {
		return false;
	}

	/**
	 * Returns the branch type.
	 * 
	 * @return an <b>int</b> representing the configuration branch type, if the
	 *         configuration is <b>null</b> returns -1.
	 */
	public int getBranchType() {
		return branchingConfig == null ? -1 : branchingConfig.getBranchType();
	}

	/**
	 * Determines whether the component has been executed.
	 * 
	 * @return <b>true</b> if the component has been executed otherwise
	 *         <b>false</b>.
	 */
	public boolean isExecuted() {
		return executed;
	}
}
