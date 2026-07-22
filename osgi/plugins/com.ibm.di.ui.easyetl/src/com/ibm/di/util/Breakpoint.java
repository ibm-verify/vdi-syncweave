/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.Serializable;

/**
 * This object contains information about a break point.
 * 
 */
public class Breakpoint implements Serializable, Comparable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

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
		return compareTo(obj) == 0;
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
