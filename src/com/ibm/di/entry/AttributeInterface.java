/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

@Deprecated
public interface AttributeInterface {
	public void setValue(Object p1);

	public void setValue(int p1, Object p2);

	public void addValue(Object p1);

	public boolean removeValue(Object p1);

	public Object getValue(int p1);

	/**
	 * This method returns the object at the given index. This method differs
	 * from getValue(int) in that it does not unwrap
	 * com.ibm.di.entry.AttributeValue objects.
	 */
	public Object getValueAV(int index);

	public Object[] getValues();

	public String getValue();

	public int size();

	public boolean contains(Object p1);

	public String getName();

	public void setName(String name);

	public void setOper(char oper);

	public char getOper();

	public Attribute setProtected(boolean protect);

	public boolean getProtected();
}
