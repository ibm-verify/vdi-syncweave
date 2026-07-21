/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/aleditor/Border",
	"tdi/orion/OrionEditor",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/ToolbarLabel",
	"dijit/form/CheckBox",
	"dijit/form/DropDownButton",
	"dijit/Dialog",
	"dijit/Toolbar",
	"dijit/TooltipDialog",
	"dijit/Tree",
	"dijit/tree/ForestStoreModel",
	"dijit/layout/ContentPane",
	"dijit/layout/AccordionContainer",
	"dojo/data/ItemFileReadStore",
	"tdi/NlsMixin",
	"idx/layout/TitlePane",
	"idx/form/Link"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Border, OrionEditor, tdiconfig, tdiutil, ToolbarLabel, CheckBox,
		DropDownButton, Dialog, Toolbar, TooltipDialog, Tree, TreeModel, ContentPane, AccordionContainer, ItemFileReadStore, nlsmixin, TitlePane, Link) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString: "<div><div style='margin:0; padding:0; width:100%; height:100%' containerType='none' data-dojo-type='tdi/aleditor/Border' data-dojo-attach-point='border'></div></div>",
		
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
		
		constructor: function(args) {
			if(args) {
				declare.safeMixin(this, args);
			}
			this.nls = new nlsmixin();
			this.counter = 0;
			this.openHooks = {};
		},
		
		getHookTree: function(mode) {
			if ( mode == null)
				return null;
			else
				return this.modes[mode];
		},
		
		resize : function(obj) {
			if(obj && obj.h > 0 && this.border) {
				this.border.resize(obj);
			}
		},
		
		createStore: function() {
			var path = this.modes[this.config.getMode()];
			if(path)
				path = this[path];
			var data = [];
			if(path) {
				data = this.addItems(path);
			}
			
			return new ItemFileReadStore({
				data:{
					identifier:"id",
					label:"name",
					items:data
				}
			});
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
		
		openHook: function(event) {
			this.addHook(event.id[0]);
		},
		
		simpleNameDecorator: function(data, rowId, index) {
			var hook = this.config.getHook(rowId);
			if(hook && hook.getEnabled()) {
				return "<b>" + data + "</b>";
			} else {
				return data;
			}
		},
		
		addEnabledHooks: function() {
			array.forEach(this.config.getHookNames(), function(name) {
				this.addHook(name);
			}, this);
		},
		
		deleteHook: function(name) {
		},
		
		addHook: function(name) {
			var hook = this.config.getHook(name, true);
			if(hook) {
				this.hookEditor.setConfig(hook);
			}
		},
		
		addHookX: function(name) {
			if(!this.openHooks[name]) {
				var title = this.nls.getString("Hook." + name);
				if(!title)
					return;
				
				var hook = this.config.getHook(name, true);
				var panel = new OrionEditor({
					config:hook,
					autoUpdate:true,
					defaultText:"// " + this.nls.getString("Hook.tooltip." + name) + "\n",
					style:"height:100%; width:100%"
				});
				panel.startup();
				
				var hp = new ContentPane({
					title:title,
					content:panel,
					closable:false,
					onClose:lang.hitch(this, function() {
						if(tdiutil.confirm("Delete hook?", function(ok) {
							return true;
						}))
						return false;
					}),
					tooltip:this.nls.getString("Hook.tooltip." + name)
				});
				this.hookDiv.addChild(hp);
//				.placeAt(this.hookDiv);

//				var enabledLink = new Link({
//					label:"Enabled",
//					onClick:lang.hitch(this, function(value) {
//						var hook = this.config.getHook(name);
//						var enabled = !hook.getEnabled();
//						hook.setEnabled(enabled);
//						enabledLink.attr("label", enabled ? "Enabled" : "Disabled")
//					}, name),
//					region:"titleActions"
//				});
//				hp.addChild(enabledLink);
//				hp.startup();
				
				this.openHooks[name] = hp;
				this.border.resize();
			}
			try {
				dojo.window.scrollIntoView(this.openHooks[name].domNode);
			} catch(e) {
				console.log("Scroll: ");
				console.log(e);
			}
		},
		
		postCreate: function() {
			var t = this;
			
			this.tree = new Tree({
				model:new TreeModel({
					store:this.createStore(),
					getLabel: function(item) {
						if(item === this.root) {
							return item.label;
						}
						var value = this.store.getValue(item, "name");
						var id = this.store.getValue(item, "id");
						var cfg = t.config ? t.config.getHook(id, false) : null;
						if(cfg && cfg.getScript() && cfg.getEnabled()) {
							return "* " + value;
						}
						return value;
					}
				}),
				showRoot:false,
				autoExpand:true,
				openOnClick:true,
				style:"height:100%; width:100%;",
				onClick:lang.hitch(this, "openHook"),
				
			});
			
			this.border.setTop(this.tree, {style:"width:100%; height:50%; margin:0; padding:0"});
			
			this.hookEditor = new OrionEditor({
				autoUpdate:true,
//				defaultText:"// " + this.nls.getString("Hook.tooltip." + name) + "\n",
				style:"height:100%; width:100%"
			});
			this.border.setCenter(this.hookEditor);
			this.hookEditor.startup();
			
//			this.hookDiv = new AccordionContainer({style:"width:100%; height:50%; margin:0; padding:0;"});
//			this.hookDiv = html.create("div", {style:"width:100%; height:100%; margin:0; padding:0; overflow:scroll"});
			
//			this.addEnabledHooks();
		}
	})
});
