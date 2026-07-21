/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
import java.util.*;

/**
 * Implements the configuration for a single item in a LinkCriteriaConfig.
 * @see LinkCriteriaConfigImpl
 */
public class LinkCriteriaItemImpl extends BaseConfigurationImpl implements
		LinkCriteriaItem {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -952539248920610452L;

	public LinkCriteriaItemImpl() {
		super();
	}

	public LinkCriteriaItemImpl(Object config) {
		super();
		if (config instanceof String)
			parseObject(config);
		else if (config instanceof TreeMap)
			setData((TreeMap<?,?>) config);
	}

	private void parseObject(Object config) {
		if (config instanceof String) {
			String str = config.toString();
			int i = str.indexOf(":");
			if (i != -1) {
				setAttribute(str.substring(0, i));
				switch (str.charAt(i + 1)) {
				case EXACT:
					setOper(LC_EXACT);
					break;
				case LESS_THAN:
					setOper(LC_LESS_THAN);
					break;
				case LESS_THAN_OR_EQUAL:
					setOper(LC_LESS_THAN_OR_EQUAL);
					break;
				case GREATER_THAN:
					setOper(LC_GREATER_THAN);
					break;
				case GREATER_THAN_OR_EQUAL:
					setOper(LC_GREATER_THAN_OR_EQUAL);
					break;
				case SUBSTRING:
					setOper(LC_SUBSTRING);
					break;
				case INITIAL_STRING:
					setOper(LC_INITIAL);
					break;
				case FINAL_STRING:
					setOper(LC_FINAL);
					break;
				case NOT_STRING:
					setOper(LC_NOT);
					break;
				}
				setValue(str.substring(i + 3));
			}
		}
	}

	public Object getAttribute() {
		return getParameter(InternalSchema.LC_ATTRIBUTE);
	}

	public void setAttribute(Object attribute) {
		setParameter(InternalSchema.LC_ATTRIBUTE, attribute);
	}

	public Object getValue() {
		return getParameter(InternalSchema.LC_VALUE);
	}

	public void setValue(Object value) {
		setParameter(InternalSchema.LC_VALUE, value);
	}

	public Object getOper() {
		return getParameter(InternalSchema.LC_OPERATOR, LC_EXACT);
	}

	public void setOper(Object oper) {
		setParameter(InternalSchema.LC_OPERATOR, oper);
	}

	public int getMatch() {
		String oper = (String) getOper();

		if (oper.equals(LC_EXACT))
			return EXACT;
		if (oper.equals(LC_LESS_THAN))
			return LESS_THAN;
		if (oper.equals(LC_LESS_THAN_OR_EQUAL))
			return LESS_THAN_OR_EQUAL;
		if (oper.equals(LC_GREATER_THAN))
			return GREATER_THAN;
		if (oper.equals(LC_GREATER_THAN_OR_EQUAL))
			return GREATER_THAN_OR_EQUAL;
		if (oper.equals(LC_SUBSTRING))
			return SUBSTRING;
		if (oper.equals(LC_INITIAL))
			return INITIAL_STRING;
		if (oper.equals(LC_FINAL))
			return FINAL_STRING;
		if (oper.equals(LC_NOT))
			return NOT_STRING;

		return EXACT;
	}

	@Override
	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.ENABLED, true);
	}
}
