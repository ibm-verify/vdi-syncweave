/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// ServerConstants.java
//
//
//
package com.ibm.di.server;

public class ServerConstants {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String VIRTUAL_CONNECTOR_NAME = "(runtime provided)";

	//
	// Connector modes
	//	
	public final static int TYPE_ITERATOR = 0;

	public final static int TYPE_UPDATE = 1;

	public final static int TYPE_LOOKUP = 2;

	public final static int TYPE_DELETE = 3;

	public final static int TYPE_ADDONLY = 4;

	public final static int TYPE_CALLREPLY = 5;

	public final static int TYPE_SCRIPT = 6;

	public final static int TYPE_FUNCTION = 7;

	public final static int TYPE_BRANCH = 8;

	public final static int TYPE_REPLYCHANNEL = 9;

	public final static int TYPE_SERVER = 10;

	public final static int TYPE_DELTA = 11;

	public final static int TYPE_LOOP = 12;

	public final static int TYPE_ATTRIBUTEMAP = 13;

	public final static int TYPE_SWITCH = 14;

	public final static int TYPE_CASE = 15;

	final static String[] STR_TYPES = { "Iterator", "Update", "Lookup",
			"Delete", "AddOnly", "CallReply", "Script", "Function", "Branch",
			"ReplyChannel", "Server", "Delta", "Loop", "Mapping", "Branch3",
			"Branch4" };

	//
	// Null value behavior
	// "Null" is empty attribute
	public final static String[] NVB_BEHAVIOR = { "Default Behavior", "Delete",
			"Null", "Empty String", "Error", "Value" };

	//
	// Null value behavior
	//
	public final static String[] NVD_DEFINITION = { "Default",
			"AbsentAttribute", "EmptyAttribute", "EmptyString", "Value" };

	//
	// Delta behaviors
	//
	public final static String[] DELTA_BEHAVIOR = { "DeltaNormal",
			"DeltaNoDelete" };

	public static int getDeltaType(String type) {
		return indexOfType(DELTA_BEHAVIOR, type);
	}

	public static int getType(String type) {
		return indexOfType(STR_TYPES, type);
	}

	public static int indexOfType(String[] table, String type) {
		if (type == null)
			return -1;

		for (int i = 0; i < table.length; i++) {
			if (table[i].compareToIgnoreCase(type) == 0)
				return i;
		}
		return TYPE_CALLREPLY;
	}

	public static String getTypeString(int type) {
		if (type >= STR_TYPES.length || type < 0)
			return null;
		else
			return STR_TYPES[type];
	}
}
