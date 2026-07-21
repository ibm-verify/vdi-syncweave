/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import com.ibm.di.config.interfaces.ConnectorConfig;

/**
 * Used by DebugServer to find which Hooks to stop at
 */
public class HooksUtil {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// Common hooks
	final static Object[] defaultHooks = { "default_ok", "default_fail", };

	final static Object[] initializeHooks = { "before_initialize",
			"after_initialize", "initialize_fail", };

	final static Object[] closeHooks = { "before_close", "after_close",
			"close_fail", };

	final static Object[] reconnectHooks = { "connect_init", "on_connection_failure",
		"reconnect_ok", "reconnect_fail",
		"failover_ok", "failover_fail",
		"failback_ok", "failback_fail"};

	// ITERATOR
	final static Object[] iteratorLoop1 = { "before_getnext",
			"after_getnext", "input_attribute_map", };

	final static Object[] iteratorLoop = { "before_execute",
			"override_getnext", iteratorLoop1, "get_ok", "default_ok",
			"get_fail", "default_fail", "end_of_data", };

	final static Object[] initializeHooksIterator = {
			"before_initialize", "before_selectEntries", "after_selectEntries",
			"after_initialize", "initialize_fail", };

	final static Object[] iteratorMode = { initializeHooksIterator,
			iteratorLoop, closeHooks, reconnectHooks};

	// LOOKUP
	final static String[] lookupLoop1 = { "before_lookup",
			"lookup_multiple", "lookup_nomatch", "after_lookup",
			"input_attribute_map", };

	final static Object[] lookupLoop = { "before_execute",
			"override_lookup", lookupLoop1, "lookup_ok", "default_ok",
			"lookup_fail", "default_fail", };

	final static Object[] lookupMode = { initializeHooks, lookupLoop,
			closeHooks, reconnectHooks };

	// ADDONLY
	final static String[] addonlyLoop1 = { "output_attribute_map",
			"before_add", "after_add", };

	final static Object[] addonlyLoop = { "before_execute",
			"override_add", addonlyLoop1, "addonly_ok", "default_ok",
			"addonly_fail", "default_fail", };

	final static Object[] addonlyMode = { initializeHooks, addonlyLoop,
			closeHooks, reconnectHooks };

	// DELETE
	final static String[] deleteLoop1 = { "before_lookup",
			"delete_multiple", "delete_nomatch", "after_lookup",
			"input_attribute_map", "before_delete", "after_delete", };

	final static Object[] deleteLoop = { "before_execute",
			"override_delete", deleteLoop1, "delete_ok", "default_ok",
			"delete_fail", "default_fail", };

	final static Object[] deleteMode = { initializeHooks, deleteLoop,
			closeHooks, reconnectHooks };

	// UPDATE

	// Modify
	final static String[] computeChanges = { "modify_apply" };

	final static Object[] updateModify = { "override_modify",
			"output_attribute_map", "before_modify", computeChanges,
			"modify_nochange", "after_modify", };

	// Add
	final static String[] updateAdd = { "override_add",
			"output_attribute_map", "before_add", "add_abandon", "after_add", };

	final static Object[] updateLoop1 = { "before_lookup",
			"update_multiple", "after_lookup", updateModify, updateAdd,
			"after_update", };

	final static Object[] updateLoop = { "before_execute",
			"before_update", "override_update", updateLoop1, "update_ok",
			"default_ok", "update_fail", "default_fail", };

	final static Object[] updateMode = { initializeHooks, updateLoop,
			closeHooks, reconnectHooks };

	// DELTA

	// Delete
	final static String[] deltaDelete = { "override_delete",
			"before_delete", "after_delete", };

	final static Object[] deltaModify = { "override_modify",
			"output_attribute_map", "before_modify", "modify_nochange",
			"after_modify", };

	final static Object[] deltaLoop1 = { "before_lookup",
			"lookup_multiple", "lookup_nomatch", "after_lookup", deltaModify,
			updateAdd, deltaDelete, "after_delta", };

	final static Object[] deltaLoop = { "before_execute",
			"before_delta", "override_delta", deltaLoop1, "delta_ok",
			"default_ok", "delta_fail", "default_fail", };

	final static Object[] deltaMode = { initializeHooks, deltaLoop,
			closeHooks, reconnectHooks };

	// FUNCTION COMPONENT
	final static String[] functionLoop1 = { "output_attribute_map",
			"before_functioncall", "after_functioncall", "input_attribute_map", };

	final static Object[] functionLoop = { "before_execute",
			functionLoop1, "default_ok", "default_fail", "no_reply", };

	final static Object[] functionComponentMode = { initializeHooks,
			functionLoop, closeHooks };

	final static Object[] branchComponentMode = {};

	// CALLREPLY
	final static String[] callreplyLoop1 = { "output_attribute_map",
			"before_call", "after_reply", "input_attribute_map", };

	final static Object[] callreplyLoop = { "before_execute",
			"override_callreply", callreplyLoop1, "callreply_ok", "default_ok",
			"callreply_fail", "default_fail", "no_reply", };

	final static Object[] callreplyMode = { initializeHooks,
			callreplyLoop, closeHooks, reconnectHooks };

	// OVERRIDE HOOKS
	final static Object[] overrideHooks = { "override_getnext",
			"override_add", "override_lookup", "override_update",
			"override_delete", "override_callreply", "override_delta", };

	// SCRIPT
	final static Object[] scriptMode = { "before_execute", };

	// REPLY
	final static String[] replyLoop1 = { "output_attribute_map",
			"before_reply", "after_reply2", };

	final static Object[] replyLoop = { "before_execute_reply",
			"override_reply", replyLoop1, "reply_ok", "reply_fail", };

	// SERVER
	final static Object[] serverLoop = { "before_getnextclient",
			"after_getnextclient", "getnextclient_fail", };

	final static Object[] serverMode = { initializeHooksIterator,
			serverLoop, iteratorLoop, replyLoop, closeHooks, };

	public static Object[] getHookTree(ConnectorConfig cc) {
		String mode = cc.getMode();
		if ( mode == null)
			return null;

		if ( mode.equals(ConnectorConfig.ITERATOR_MODE) ) {
			if ( cc.getReplyRequired() )
				return serverMode;
			else
				return iteratorMode;
		}

		if (mode.equals(ConnectorConfig.UPDATE_MODE))
			return updateMode;
		if (mode.equals(ConnectorConfig.LOOKUP_MODE))
			return lookupMode;
		if (mode.equals(ConnectorConfig.DELETE_MODE))
			return deleteMode;
		if (mode.equals(ConnectorConfig.ADDONLY_MODE))
			return addonlyMode;
		if (mode.equals(ConnectorConfig.CALL_REPLY_MODE))
			return callreplyMode;
		if (mode.equals(ConnectorConfig.SCRIPT_MODE))
			return scriptMode;
		if (mode.equals(ConnectorConfig.FUNCTION_MODE))
			return functionComponentMode;
		if (mode.equals(ConnectorConfig.BRANCH_MODE))
			return branchComponentMode;
		if (mode.equals(ConnectorConfig.REPLY_MODE))
			return replyLoop;
		if (mode.equals(ConnectorConfig.SERVER_MODE))
			return serverMode;
		if (mode.equals(ConnectorConfig.DELTA_MODE))
			return deltaMode;

		return null;
	}
}
