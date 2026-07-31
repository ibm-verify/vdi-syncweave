/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.Serializable;

import com.ibm.di.script.ScriptEngine;

/**
 * This object contains information about a break point.
 * 
 */
public class Breakpoint implements Serializable, Comparable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final long serialVersionUID = 1;

	private boolean enabled;

	private String location;

	private String expression;

	public Breakpoint(String location, boolean enabled, String expression) {
		this.enabled = enabled;
		this.location = location;
		this.expression = expression;
	}

	public int compareTo(Object arg0) {
		if (arg0 instanceof Breakpoint)
			return location.compareTo(((Breakpoint) arg0).getLocation());
		else
			return location.compareTo("" + arg0);
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getLocation() {
		return location;
	}

	public boolean match(ScriptEngine se) {
		if (expression == null || expression.trim().length() == 0)
			return isEnabled();

		try {
			return Boolean.valueOf("" + se.eval(expression)).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String toString() {
		return "[location=" + location + ", enabled=" + enabled + ", expression=" + expression + "]";
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Breakpoint getClone() {
		return new Breakpoint(location, enabled, expression);
	}
}
