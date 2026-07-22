/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.server.ServerConstants;

public class HookTree {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String name;
	private String title;
	private HooksConfig hooks;
	private ConnectorConfig config;
	private Object[] children;

	public enum Phase {SERVER, INIT, LOOP, REPLY, CLOSE, RECONNECT};
	
	private final static ResourceHash messages = ResourceHash.getHash("miserver");

	// Common hooks	
	private final static Object[] defaultHooks = {
		"default_ok",
		"default_fail",
	};

	private final static Object[] initializeHooks = {
		"before_initialize",
		"after_initialize",
		"initialize_fail",
	};

	private final static Object[] closeHooks = {
		"before_close",
		"after_close",
		"close_fail",
	};

	final static Object[] reconnectHooks = {
		"connect_init",
		"on_connection_failure",
		"reconnect_ok",
		"reconnect_fail",
		"failover_ok",
		"failover_fail",
		"failback_ok",
		"failback_fail",
	};

	// ITERATOR
	private final static Object[] iteratorLoop1 = {
		"before_getnext",
		"after_getnext",
		"input_attribute_map",
	};

	private final static Object[] iteratorLoop = {
		"before_execute",
		"override_getnext",
		iteratorLoop1,
		"get_ok",
		"default_ok",
		"get_fail",
		"default_fail",
		"end_of_data",
	};

	private final static Object[] initializeHooksIterator = {
		"before_initialize",
		"before_selectEntries",
		"after_selectEntries",
		"after_initialize",
		"initialize_fail",
	};

	private final static Object[] iteratorMode = {
		initializeHooksIterator,
		iteratorLoop,
		closeHooks,
		reconnectHooks,
	};

	// LOOKUP
	private final static String[] lookupLoop1 = {	
		"before_lookup",
		"lookup_multiple",
		"lookup_nomatch",
		"after_lookup",
		"input_attribute_map",
	};

	private final static Object[] lookupLoop = {
		"before_execute",
		"override_lookup",
		lookupLoop1,
		"lookup_ok",
		"default_ok",
		"lookup_fail",
		"default_fail",
	};

	private final static Object[] lookupMode = {
		initializeHooks,
		lookupLoop,
		closeHooks,
		reconnectHooks
	};

	// ADDONLY
	private final static String[] addonlyLoop1 = {
		"output_attribute_map",
		"before_add",
		"after_add",
	};

	private final static Object[] addonlyLoop = {
		"before_execute",
		"override_add",
		addonlyLoop1,
		"addonly_ok",
		"default_ok",
		"addonly_fail",
		"default_fail",
	};

	private final static Object[] addonlyMode = {
		initializeHooks,
		addonlyLoop,
		closeHooks,
		reconnectHooks
	};

	// DELETE
	private final static String[] deleteLoop1 = {
		"before_lookup",
		"delete_multiple",
		"delete_nomatch",
		"after_lookup",
		"input_attribute_map",
		"before_delete",
		"after_delete",
	};

	private final static Object[] deleteLoop = {
		"before_execute",
		"override_delete",
		deleteLoop1,
		"delete_ok",
		"default_ok",
		"delete_fail",
		"default_fail",
	};

	private final static Object[] deleteMode = {
		initializeHooks,
		deleteLoop,
		closeHooks,
		reconnectHooks
	};

	// UPDATE	

	// Modify
	private final static String[] computeChanges = {
		"modify_apply"
	};

	private final static Object[] updateModify = {
		"override_modify",
		"output_attribute_map",
		"before_modify",
		computeChanges,
		"modify_nochange",
		"after_modify",
	};		

	// Add
	private final static String[] updateAdd = {
		"override_add",
		"output_attribute_map",
		"before_add",
		"add_abandon",
		"after_add",
	};

	private final static Object[] updateLoop1 = {
		"before_lookup",
		"update_multiple",
		"after_lookup",
		updateModify,
		updateAdd,
		"after_update",
	};

	private final static Object[] updateLoop = {
		"before_execute",
		"before_update",
		"override_update",
		updateLoop1,
		"update_ok",
		"default_ok",
		"update_fail",
		"default_fail",
	};

	private final static Object[] updateMode = {
		initializeHooks,
		updateLoop,
		closeHooks,
		reconnectHooks
	};

	// DELTA

	private final static String[] deltaDelete = {
		"override_delete",
		"before_delete",
		"after_delete",
	};

	private final static Object[] deltaModify = {
		"override_modify",
		"output_attribute_map",
		"before_modify",
		"modify_nochange",
		"after_modify",
	};		

	private final static Object[] deltaLoop1 = {
		"before_lookup",
		"lookup_multiple",
		"lookup_nomatch",
		"after_lookup",
		deltaModify,
		updateAdd,
		deltaDelete,
		"after_delta",
	};

	private final static Object[] deltaLoop = {
		"before_execute",
		"before_delta",
		"override_delta",
		deltaLoop1,
		"delta_ok",
		"default_ok",
		"delta_fail",
		"default_fail",
	};

	private final static Object[] deltaMode = {
		initializeHooks,
		deltaLoop,
		closeHooks,
		reconnectHooks
	};

	// FUNCTION COMPONENT
	private final static String[] functionLoop1 = {
		"output_attribute_map",
		"before_functioncall",
		"after_functioncall",
		"input_attribute_map",
	};

	private final static Object[] functionLoop = {
		"before_execute",
		functionLoop1,
		"functioncall_ok",
		"default_ok",
		"functioncall_fail",
		"default_fail",
		"no_reply",
	};

	private final static Object[] functionComponentMode = {
		initializeHooks,
		functionLoop,
		closeHooks
	};

	private final static Object[] branchComponentMode = {
	};

	// CALLREPLY
	private final static String[] callreplyLoop1 = {
		"output_attribute_map",
		"before_call",
		"after_reply",
		"input_attribute_map",
	};

	private final static Object[] callreplyLoop = {
		"before_execute",
		"override_callreply",
		callreplyLoop1,
		"callreply_ok",
		"default_ok",
		"callreply_fail",
		"default_fail",
		"no_reply",
	};

	private final static Object[] callreplyMode = {
		initializeHooks,
		callreplyLoop,
		closeHooks,
		reconnectHooks
	};

	// OVERRIDE HOOKS
	private final static Object[] overrideHooks = {
		"override_getnext",
		"override_add",
		"override_lookup",
		"override_update",
		"override_delete",
		"override_callreply",
		"override_delta",
	};

	// SCRIPT 
	private final static Object[] scriptMode = {
	};

	// REPLY
	private final static String[] replyLoop1 = {	
		"output_attribute_map",
		"before_reply",
		"after_reply2",
	};

	private final static Object[] replyLoop = {
		"before_execute_reply",
		"override_reply",
		replyLoop1,
		"reply_ok",
		"reply_fail",
	};

	// SERVER
	private final static Object[] serverLoop = {
		"before_getnextclient",
		"after_getnextclient",
		"getnextclient_fail",
	};

	private final static Object[] serverMode = {
		initializeHooksIterator,
		serverLoop,
		iteratorLoop,
		replyLoop,
		closeHooks,
	};

	/**
	 * This array and the two next must be kept in sync
	 */
	private final static Object[] arrayNamesCtl = {
		defaultHooks,
		initializeHooksIterator,
		initializeHooks,
		iteratorLoop,
		addonlyLoop,
		deleteLoop,
		updateLoop,
		lookupLoop,
		callreplyLoop,
		replyLoop,
		deltaLoop,
		computeChanges,
		updateModify,
		updateAdd,
		deltaDelete,
		deltaModify,
		closeHooks,
		reconnectHooks,
		iteratorLoop1,
		addonlyLoop1,
		updateLoop1,
		deleteLoop1,
		lookupLoop1,
		callreplyLoop1,
		replyLoop1,
		deltaLoop1,
		overrideHooks,
		functionLoop1,
		serverLoop,
		functionLoop,
	};

	private final static String[] arrayNames = {
		"Hooks.TopLevelHooks",
		"Hooks.PrologIterator",
		"Hooks.PrologOther",
		"Hooks.DataFlowIterator",
		"Hooks.DataFlowAddOnly",
		"Hooks.DataFlowDelete",
		"Hooks.DataFlowUpdate",
		"Hooks.DataFlowLookup",
		"Hooks.DataFlowCallReply",
		"Hooks.DataFlowReply",
		"Hooks.DataFlowDelta",
		"Hooks.OnComputeChanges",
		"Hooks.OnModify",
		"Hooks.OnAdd",
		"Hooks.OnDelete",
		"Hooks.OnModify",
		"Hooks.Epilog",
		"Hooks.Reconnect",
		"Hooks.Iterator",
		"Hooks.AddOnly",
		"Hooks.Update",
		"Hooks.Delete",
		"Hooks.Lookup",
		"Hooks.CallReply",
		"Hooks.Reply",
		"Hooks.Delta",
		"Hooks.Override",
		"Hooks.Function",
		"Hooks.Server",
		"Hooks.DataFlowFunction",
	};

	private final static String[] arrayNames2 = {
		"TopLevelHooks",
		"PrologIterator",
		"PrologOther",
		"DataFlowIterator",
		"DataFlowAddOnly",
		"DataFlowDelete",
		"DataFlowUpdate",
		"DataFlowLookup",
		"DataFlowCallReply",
		"DataFlowReply",
		"DataFlowDelta",
		"OnComputeChanges",
		"OnModify",
		"OnAdd",
		"OnDelete",
		"OnModify",
		"[epilog]",
		"Reconnect",
		"Iterator",
		"AddOnly",
		"Update",
		"Delete",
		"Lookup",
		"CallReply",
		"Reply",
		"Delta",
		"Override",
		"Function",
		"Server",
		"DataFlowFunction",
	};

	private static Map<String, Object[]> modeHooks = new HashMap<String, Object[]>();
	private static Map<Object, String> arrayNameMap = new HashMap<Object, String>();
	private static Map<Object, String> arrayNameMap2 = new HashMap<Object, String>();

	static {
		modeHooks.put(ConnectorConfig.ITERATOR_MODE, iteratorMode);
		modeHooks.put(ConnectorConfig.UPDATE_MODE, updateMode);
		modeHooks.put(ConnectorConfig.LOOKUP_MODE, lookupMode);
		modeHooks.put(ConnectorConfig.DELETE_MODE, deleteMode);
		modeHooks.put(ConnectorConfig.ADDONLY_MODE, addonlyMode);
		modeHooks.put(ConnectorConfig.CALL_REPLY_MODE, callreplyMode);
		modeHooks.put(ConnectorConfig.SCRIPT_MODE, scriptMode);
		modeHooks.put(ConnectorConfig.FUNCTION_MODE, functionComponentMode);
		modeHooks.put(ConnectorConfig.BRANCH_MODE, branchComponentMode);
		modeHooks.put(ConnectorConfig.REPLY_MODE, replyLoop);
		modeHooks.put(ConnectorConfig.SERVER_MODE, serverMode);
		modeHooks.put(ConnectorConfig.DELTA_MODE, deltaMode);

		for(int i = 0; i < arrayNamesCtl.length; i++) {
			String s = messages.getString(arrayNames[i]);
			arrayNameMap.put(arrayNamesCtl[i], s != null ? s : arrayNames[i]);
			arrayNameMap2.put(arrayNamesCtl[i], arrayNames2[i]);
		}
	}

	private final static Object[] mainAssemblyLine = {
		InternalSchema.AL_PROLOG_INIT,
		InternalSchema.AL_PROLOG,
		InternalSchema.AL_STARTCYCLE,
		InternalSchema.AL_EPILOG,
		InternalSchema.AL_EPILOG2,
		InternalSchema.AL_ONSUCCESS,
		InternalSchema.AL_ONFAILURE,
		InternalSchema.AL_SHUTDOWN
	};

	/**
	 * Returns Internal Hook names for a Connector mode.
	 * The returned Objects are either Strings, or new Object arrays
	 * with Strings or Object arrays. An empty array means no hooks
	 * are used in this mode, null means that the mode was unknown.
	 * @param mode A connector mode
	 * @return The hook names for the given mode, or null for an unknown mode.
	 */
	public static Object[] getHookNames(String mode) {
		return modeHooks.get(mode);
	}
	
	/**
	 * Return the name of an Object[] used for constructing a HookTree.
	 * @param element really an Object[]
	 * @return a String suitable for showing the user.
	 */
	public static String getArrayName(Object element) {
		return arrayNameMap.get(element);
	}

	/**
	 * Return a user friendly label for the given HookConfig.
	 * @param hc
	 * @return a String
	 */
	public static String getHookLabel(HookConfig hc) {
		if (hc == null)
			return null;
		String s = (String)hc.getHookName();
		if (s == null)
			s = hc.getShortName();
		if (s == null)
			return null;
		return getHookLabel(s);	
	}
	
	/**
	 * Return a user friendly label for the given hook name.
	 * @param hookName - The internal name of the hook.
	 * @return The translated String, or the internal name if nothing found.
	 */
	public static String getHookLabel(String hookName) {
		String s = messages.getString("Hook." + hookName);
		return s != null ? s : hookName;
	}

	/**
	 * Return a new HookTree for a ConnectorConfig
	 * @param cc The ConnectorConfig
	 * @return a HookTree
	 */
	public static HookTree getHookTree(ConnectorConfig cc) {
		String mode = ServerConstants.getTypeString(ServerConstants.getType(cc.getMode()));
		return new HookTree(cc.getShortName(), modeHooks.get(mode), cc.getHooks());
	}

	/**
	 * Return a new HookTree for a ConnectorConfig in a given Phase.
	 * Associates the ConnectorConfig with the root of the Tree.
	 * @param cc The ConnectorConfig
	 * @param phase The Phase
	 * @return a HookTree, null if nothing defined for this Phase and mode.
	 */
	public static HookTree getHookTree(ConnectorConfig cc, Phase phase) {
		String mode = ServerConstants.getTypeString(ServerConstants.getType(cc.getMode()));
		if (ConnectorConfig.SERVER_MODE.equals(mode)) {
			if (phase == Phase.SERVER)
				return new HookTree(cc, serverLoop);
			if (phase == Phase.REPLY)
				return new HookTree(cc, replyLoop);
			if (phase == Phase.RECONNECT)
				return null;
			mode = ConnectorConfig.ITERATOR_MODE;
		}
		
		Object[] hookList = modeHooks.get(mode);
		if (hookList == null || hookList.length < 3)
			return null;
		
		if (phase == Phase.INIT)
			return new HookTree(cc, (Object[])hookList[0]);
		if (phase == Phase.LOOP)
			return new HookTree(cc, (Object[])hookList[1]);
		if (phase == Phase.CLOSE)
			return new HookTree(cc, (Object[])hookList[2]);
		if (phase == Phase.RECONNECT && hookList.length >= 4)
			return new HookTree(cc, (Object[])hookList[3]);			
		
		return null;
	}

	/**
	 * Return a new HookTree for an AssemblyLineConfig
	 * @param alc The AssemblyLineConfig
	 * @return a HookTree
	 */
	public static HookTree getHookTree(AssemblyLineConfig alc) {
		return new HookTree("AL", mainAssemblyLine, alc.getHooks());
	}

	/**
	 * Return a new HookTree for a HooksConfig.
	 * Tries to guess if this is an AssemblyLine or Connector.
	 * @param hc The HooksConfig
	 * @return a HookTree
	 */

	public static HookTree getHookTree(HooksConfig hc) {
		BaseConfiguration parent = hc.getParent();
		if (parent instanceof ConnectorConfig)
			return getHookTree((ConnectorConfig) parent);
		return new HookTree("AL", mainAssemblyLine, hc);
	}

	private HookTree(ConnectorConfig cc, Object[] object) {
		this(cc.getShortName(), object, cc.getHooks());
		config = cc;
	}
	
	private HookTree(String name, Object[] object, HooksConfig hc) {
		if (name == null)
			name = "";
		this.name = name;
		hooks = hc;

		if(object != null) {
			children = new Object[object.length];
			for (int i = 0; i < object.length; i++) {
				Object obj = object[i];
				if(obj instanceof String) {
					children[i] = new HookTree((String)obj, null, hc);
				} else {
					children[i] = new HookTree(arrayNameMap2.get(obj), (Object[]) obj, hc);
				}
			}
			title = getArrayName(object);
		} else {
			children = new Object[0];
			title = getHookLabel(name);
		}
	}

	/**
	 * Return the HookConfig using the name of this HookTree.
	 * @param create - If true, create a HookConfig if it does not exist
	 * @return The HookConfig, or null if not found and create is false.
	 */
	public HookConfig getHookConfig(boolean create) {
		if (hooks != null)
			return hooks.getHook(name, create);
		return null;
	}

	/**
	 * Return the HooksConfig used by this HookTree.
	 * @return The HooksConfig.
	 */	
	public HooksConfig getHooksConfig() {
		return hooks;
	}
	
	/**
	 * Returns true if the HookConfig with this name is enabled.
	 * @return true if the HookConfig with this name is enabled.
	 */
	public boolean isEnabled() {
		HookConfig hc = getHookConfig(false);
		if(hc != null && hc.getEnabled())
			return true;
		else
			return false;
	}

	/**
	 * Set the HookConfig with this name to enabled.
	 * Creates the HookConfig if needed.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		HookConfig hc = getHookConfig(true);
		hc.setEnabled(enabled);
	}

	/**
	 * Returns true is this HookTree has children
	 * @return true is this HookTree has children
	 */
	public boolean hasChildren() {
		return children.length > 0;
	}

	/**
	 * Gets the children of this HookTree.
	 * @return an array of HookTree, empty if no children
	 */
	public Object[] getChildrenArray() {
		return children;
	}

	/**
	 * Returns the name of the hook matching this HookTree.
	 * If there are no children, this will typically be an internal Hook Name,
	 * or input_attribute_map/output_attribute_map.
	 * If this HookTree has children, the name will be a user friendly String.
	 * @return The name of the hook matching this HookTree
	 */
	public String getName() {
		return name;
	}

	@Override
	/**
	 * Returns the user friendly name of this HookTree.
	 * @return a String suitable for showing the user
	 */
	public String toString() {
		return title;
	}

	/**
	 * Returns the Associated ConnectorConfig
	 * @return the Associated ConnectorConfig
	 */
	public ConnectorConfig getConfig() {
		return config;
	}

	@Override
	public boolean equals(Object o) {
		if ( o == this )
			return true;
		if(! (o instanceof HookTree))
			return false;
		HookTree target = (HookTree) o;
		if ( hooks != target.hooks )
			return false;	
		if (name == null)
			return target.name == null;
		return name.equals(target.name);
	}

	@Override
	public int hashCode() {
		int h = 0;
		if (name != null)
			h = name.hashCode();
		if ( hooks != null)
			h ^= hooks.hashCode();
		return h;
	}

}
