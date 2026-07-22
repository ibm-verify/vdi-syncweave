/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.Serializable;

import com.ibm.di.script.ScriptEngine;
import com.ibm.jscript.engine.IExecutionContext;

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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Breakpoint) {
			return compareTo(obj) == 0;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return location.hashCode();
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
		return match(se, null);
	}
	
	public boolean match(ScriptEngine se, IExecutionContext context) {
		if ( !isEnabled() )
			return false;

		if (expression == null || expression.trim().length() == 0)
			return true;

		try {
			Object val = ((context == null) ? se.eval(expression) : se.eval(expression, context));
			if (val == null)
				return false;
			return Boolean.valueOf(val.toString());
		} catch (Exception e) {
			return false;
		}
	}

	public String toString() {
		return "[location=" + location + ", enabled=" + enabled
				+ ", expression=" + expression + "]";
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
