define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/data/ItemFileReadStore",
	"tdi/NlsMixin"
	], function(declare, lang, array, ItemFileReadStore, nls) {
	
	return declare( [ItemFileReadStore], {
		// Common hooks
		defaultHooks: [ "default_ok", "default_fail" ],
	
		initializeHooks: [ "before_initialize",
				"after_initialize", "initialize_fail" ],
	
		closeHooks: [ "before_close", "after_close",
				"close_fail"],
	
		reconnectHooks: [ "connect_init", "on_connection_failure",
			"reconnect_ok", "reconnect_fail",
			"failover_ok", "failover_fail",
			"failback_ok", "failback_fail"],
	
		// ITERATOR
		iteratorLoop1: [ "before_getnext",
				"after_getnext", "input_attribute_map"],
	
		iteratorLoop: [ "before_execute",
				"override_getnext", "iteratorLoop1", "get_ok", "default_ok",
				"get_fail", "default_fail", "end_of_data"],
	
		initializeHooksIterator: [
				"before_initialize", "before_selectEntries", "after_selectEntries",
				"after_initialize", "initialize_fail"],
	
		iteratorMode: [ "initializeHooksIterator",
		                "iteratorLoop", "closeHooks", "reconnectHooks"],
	
		// LOOKUP
		lookupLoop1: [ "before_lookup",
				"lookup_multiple", "lookup_nomatch", "after_lookup",
				"input_attribute_map"],
	
		lookupLoop: [ "before_execute",
				"override_lookup", "lookupLoop1", "lookup_ok", "default_ok",
				"lookup_fail", "default_fail"],
	
		lookupMode: [ "initializeHooks", "lookupLoop",
		              "closeHooks", "reconnectHooks"],
	
		// ADDONLY
		addonlyLoop1: [ "output_attribute_map",
				"before_add", "after_add"],
	
		addonlyLoop: [ "before_execute",
				"override_add", "addonlyLoop1", "addonly_ok", "default_ok",
				"addonly_fail", "default_fail"],
	
		addonlyMode: [ "initializeHooks", "addonlyLoop",
		               "closeHooks", "reconnectHooks"],
	
		// DELETE
		deleteLoop1: [ "before_lookup",
				"delete_multiple", "delete_nomatch", "after_lookup",
				"input_attribute_map", "before_delete", "after_delete"],
	
		deleteLoop: [ "before_execute",
				"override_delete", "deleteLoop1", "delete_ok", "default_ok",
				"delete_fail", "default_fail"],
	
		deleteMode: [ "initializeHooks", "deleteLoop",
		              "closeHooks", "reconnectHooks"],
	
		// UPDATE
	
		// Modify
		computeChanges: [ "modify_apply" ],
	
		updateModify: [ "override_modify",
				"output_attribute_map", "before_modify", "computeChanges",
				"modify_nochange", "after_modify"],
	
		// Add
		updateAdd: [ "override_add",
				"output_attribute_map", "before_add", "add_abandon", "after_add"],
	
		updateLoop1: [ "before_lookup",
				"update_multiple", "after_lookup", "updateModify", "updateAdd",
				"after_update"],
	
		updateLoop: [ "before_execute",
				"before_update", "override_update", "updateLoop1", "update_ok",
				"default_ok", "update_fail", "default_fail"],
	
		updateMode: [ "initializeHooks", "updateLoop",
		              "closeHooks", "reconnectHooks"],
	
		// DELTA
	
		// Delete
		deltaDelete: [ "override_delete",
				"before_delete", "after_delete"],
	
		deltaModify: [ "override_modify",
				"output_attribute_map", "before_modify", "modify_nochange",
				"after_modify"],
	
		deltaLoop1: [ "before_lookup",
				"lookup_multiple", "lookup_nomatch", "after_lookup", "deltaModify",
				"updateAdd", "deltaDelete", "after_delta"],
	
		deltaLoop: [ "before_execute",
				"before_delta", "override_delta", "deltaLoop1", "delta_ok",
				"default_ok", "delta_fail", "default_fail"],
	
		deltaMode: [ "initializeHooks", "deltaLoop",
		             "closeHooks", "reconnectHooks"],
	
		// FUNCTION COMPONENT
		functionLoop1: [ "output_attribute_map",
				"before_functioncall", "after_functioncall", "input_attribute_map"],
	
		functionLoop: [ "before_execute",
		                "functionLoop1", "default_ok", "default_fail", "no_reply"],
	
		functionComponentMode: [ "initializeHooks",
		                         "functionLoop", "closeHooks" ],
	
		branchComponentMode: [],
	
		// CALLREPLY
		callreplyLoop1: [ "output_attribute_map",
				"before_call", "after_reply", "input_attribute_map"],
	
		callreplyLoop: [ "before_execute",
				"override_callreply", "callreplyLoop1", "callreply_ok", "default_ok",
				"callreply_fail", "default_fail", "no_reply"],
	
		callreplyMode: [ "initializeHooks",
		                 "callreplyLoop", "closeHooks", "reconnectHooks" ],
	
		// OVERRIDE HOOKS
		overrideHooks: [ "override_getnext",
				"override_add", "override_lookup", "override_update",
				"override_delete", "override_callreply", "override_delta"],
	
		// SCRIPT
		scriptMode: [ "before_execute"],
	
		// REPLY
		replyLoop1: [ "output_attribute_map",
				"before_reply", "after_reply2"],
	
		replyLoop: [ "before_execute_reply",
				"override_reply", "replyLoop1", "reply_ok", "reply_fail"],
	
		// SERVER
		serverLoop: [ "before_getnextclient",
				"after_getnextclient", "getnextclient_fail"],
	
		serverMode: [ "initializeHooksIterator",
		              "serverLoop", "iteratorLoop", "replyLoop", "closeHooks" ],
	
		modes: {
				"Iterator":"iteratorMode",
				"Update":"updateMode",
				"Lookup":"lookupMode",
				"Delete":"deleteMode",
				"AddOnly":"addonlyMode",
				"CallReply":"callreplyMode",
				"Script": "scriptMode",
				"Function": "functionMode",
				"Branch": "branchMode",
				"Server": "serverMode",
				"Delta": "deltaMode"
		},
		
		addItems: function(path) {
			var nls = this.nls;
			if(!lang.isArray(path))
				path = [path];
			var arr = array.map(path, function(id) {
				var item = {id:id, name:nls.getString("Hook." + id)};
				var obj = this[id];
				if(lang.isArray(obj)) {
					item.folder = true;
					item.name = item.id;
					item.id = "@" + item.id;
					item.children = this.addItems(obj);
				}
				return item;
			}, this);
			return arr;
		},
		
		postMixinProperties: function() {
			var path = this.modes[this.mode];
			if(path)
				path = this[path];
			if(path) {
				this.data = this.addItems(path);
			}
		}
		
	});
});
		
