/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ModificationItem.java
//
//
//
package com.ibm.di.entry;

public class ModificationItem {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static char REPLACE_ATTRIBUTE = '=';

	public final static char ADD_ATTRIBUTE = '+';

	public final static char REMOVE_ATTRIBUTE = '-';

	char operation;

	Attribute attribute;

	public ModificationItem(char p1, Attribute p2) {
		operation = p1;
		attribute = p2;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public char getModificationOp() {
		return operation;
	}

}
