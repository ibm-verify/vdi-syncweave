/*
 *
 *  OCO Source Materials
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
dojo.provide("tdi.EditSolutionSettings");

dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Form");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.TitlePane");

dojo.require("tdi.DialogContent");
dojo.require("tdi.TreeTableWidget");

dojo.declare("tdi.EditSolutionSettings", [dijit._Widget, dijit._Templated, tdi.NlsMixin ], {

	templateString : "<div dojoAttachPoint='Main' style='width:100%; height:100%'></div>",
	widgetsInTemplate: true,
	
	onClose : function() {
	},
	
	onSave : function() {
		
		var arr = [];
		var exposedAssemblyLines = [];
		var store = this.exposedStore;
		store.fetch({onComplete:function(list,req) {arr = list}});
		dojo.forEach(arr, dojo.hitch(this, function(item) {
			var str = store.getValue(item, "id");
			str = str.replace(/\./g, ":");
			var form = store.getValue(item, "form");
			var options = null;
			if(form && form.Form) {
				options = form.Form.value;
			}
			if(options) {
				this.exposedTable.convertFormBooleans(options, false);
				str += "!" + dojo.toJson(options);
			}
			exposedAssemblyLines.push({
				name:str
			});
		}));
		
		this.config.getSolutionInterface().setExposedAssemblyLines(exposedAssemblyLines);
		
		this.onClose();
	},
	
	onAdd : function() {
		dojo.forEach(this.componentsTable.getSelectedRows(), dojo.hitch(this, function(item) {
			var id = this.componentsTable.getItemValue(item, "id");
			var name = this.componentsTable.getItemValue(item, "name");
			var type = this.componentsTable.getItemValue(item, "type");
			var item = this.exposedTable.findItem("id", name);
			if(item.length == 0) {
				this.exposedTable.getStore().newItem({
					id:id,
					name:name,
					type:type,
					options:{}
				});
			}
		}));
	},
	
	resize : function(obj) {
		if(obj && obj.h > 0 && this._border) {
			this._border.resize(obj);
		}
		this.inherited(arguments);
	},
	
	postCreate : function() {
		this._border = new dijit.layout.BorderContainer({gutters:false, liveSplitters:true, style:"width:100%;height:100%"}).placeAt(this.Main);
		
		this._top = new dijit.layout.ContentPane({region:"top", splitter:true});
		this._left = new dijit.layout.ContentPane({region:"left", splitter:true, style:"width:25%"});
		this._center = new dijit.layout.ContentPane({region:"center", splitter:true, style:"width:70%"});
		
		this._border.addChild(this._top);
		this._border.addChild(this._left);
		this._border.addChild(this._center);
		
		var div = dojo.create("h1", {style:"padding:5px", innerHTML:this.getString("WebCE.wizard_label")});
		this._top.set("content", div);
		
		//
		// Stores
		//
		this.exposedStore = new tdi.model.PublishedObjectsStore({config:this.config});
		this.componentsStore = new tdi.model.PublishedObjectsStore({config:this.config});

		//
		// -- Left panel contains all components
		//
		var left = new tdi.EditSolutionSettingsLeft({
			store: this.componentsStore,
			config:this.config,
			onSave:dojo.hitch(this, "onSave"),
			onClose:dojo.hitch(this, "onClose"),
			onAdd:dojo.hitch(this, "onAdd")
		});
		this._left.set("content", left);
		
		//
		// -- right panel contains list of exposed components
		//
		var center = new tdi.EditSolutionSettingsExposed({
			store: this.exposedStore,
			config:this.config
		});
		this._center.set("content", center);
		
		this.componentsTable = left;
		this.exposedTable = center;
	},
	
	destroy : function() {
		if(this.componentsTable)
			this.componentsTable.destroyRecursive();
		if(this.exposedTable)
			this.exposedTable.destroyRecursive();
	}
	
});

dojo.declare("tdi.EditSolutionSettingsLeft", [ tdi.TreeTableWidget ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	// config: tdi.tdiconfig
	//		The configuration file loaded from the server
	config: null,
	
	onClose : function() {
	},
	
	onSave : function() {
	},
	
	onAdd : function() {
	},
	
	onToggleAll : function() {
	},
	
	constructor : function(/* Object */args) {
		dojo.safeMixin(this, args);
	},

	toolbarOptions: {
		actionMenu:false,
		expandAllIcon: false,
		refreshIcon: false
	},
	
	getTreeTableLayout : function() {
		return [
			{field:"name", name:this.getString("name"), width:"auto"},
		];
	},
	
	getTreeTableSize : function() {
		return ({height:"100%", width:"100%"})
	},
	
	postCreate : function() {
		this.inherited("postCreate", arguments);
		
		this.addToToolbar(new dijit.form.Button({
			label:this.getString("save"),
			onClick:dojo.hitch(this, "onSave")
		}));
		
		this.addToToolbar(new dijit.form.Button({
			label:this.getString("cancel"),
			onClick:dojo.hitch(this, "onClose")
		}));
		
		this.addToToolbar(new dijit.form.Button({
			label:this.getString("WebCE.add"),
			onClick:dojo.hitch(this, "onAdd")
		}));
		
		this.removeAllItems();
		this.store.loadAssemblyLines();
	}
});

dojo.declare("tdi.EditSolutionSettingsExposed", [ tdi.TreeTableWidget ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	// config: tdi.tdiconfig
	//		The configuration file loaded from the server
	config: null,
	
	constructor : function(/* Object */args) {
		dojo.safeMixin(this, args);
	},

	getTreeTableLayout : function() {
		return [
			{field:"name", name:this.getString("WebCE.wizard_label"), width:"auto", formatter:dojo.hitch(this, "formatItem")},
		];
	},
	
	getTreeTableSize : function() {
		return ({height:"100%", width:"100%"})
	},
	
	toolbarOptions: {
		actionMenu:false,
		expandAllIcon: false,
		refreshIcon: false
	},
	
	onMoveUp : function(event) {
		if(this.rowItem) {
			this.getStore().deleteItem(this.rowItem);
		}
	},
	
	onMoveDown : function(event) {
		
	},
	
	onDeleteItem : function(event) {
		var store = this.getStore();
		dojo.forEach(this.getSelectedRows(), function(item) {
			store.deleteItem(item);
		});
	},
	
	onRowClick : function(item, event) {
		this.rowItem = item;
		this.rowIndex = event.rowIndex;
	},
	
	convertFormBooleans : function(formvals, toform) {
		for(f in formvals) {
			// convert array[1] boolean to proper boolean
			if(typeof formvals[f] == "object" && !toform) {
				var fv = formvals[f][0];
				if(fv == "on" || fv == "off") {
					formvals[f] = (fv == "on"); 
				} else if (formvals[f].length == 0) {
					formvals[f] = false;
				}
			} else if(typeof formvals[f] == "boolean" && toform) {
				formvals[f] = (formvals[f] ? "on" : "off");
			} else if((f == "ConnectorParamsEditable" || f == "ConnectorParamsVisible") && toform) {
				if(typeof formvals[f] == "object") {
					var arr = [];
					for(key in formvals[f]) {
						arr.push(key);
					}
					formvals[f] = arr.join(",");
				}
			} else if((f == "ConnectorParamsEditable" || f == "ConnectorParamsVisible") && !toform) {
				var obj = {};
				if(typeof formvals[f] == "string") {
					var arr = formvals[f].split(",");
					for(i = 0; i < arr.length; i++) {
						obj[arr[i]] = true;
					}
					formvals[f] = obj;
				}
			}
		}
	},
	
	createForm : function(item, template, values) {
		var formvals = values;
		var old = this.getItemValue(item, "form");
		if(old) {
			formvals = old.Form.value;
			old.destroy();
		} else {
			this.convertFormBooleans(formvals, true);
		}
		
		var form = new tdi.DialogContent({
			name: this.getItemValue(item, "name"),
			templatePath:dojo.moduleUrl("tdi", "templates/" + template + ".html"),
			selectEditableParams:dojo.hitch(this, "selectConnectorParameters", item, "ConnectorParamsEditable", this.getString("BranchingConfig.Edit.label")),
			selectVisibleParams:dojo.hitch(this, "selectConnectorParameters", item, "ConnectorParamsVisible", this.getString("general.show.label"))
		});
		
		if(formvals)
			form.setFormValues(formvals);
		
		this.setItemValue(item, "form", form);
		
		return form;
	},
	
	selectConnectorParameters: function(item, param, title) {
		var sel = new tdi.EditConnectorParams({config:this.config, item:item, key:param});
		var dlg = new dijit.Dialog({
			title:title,
			content:sel
		});
		var form = this.getItemValue(item, "form");
		dojo.connect(dlg, "onCancel", dojo.hitch(this, function(form) {
			var values = this.getItemValue(item, "options");
			if(!dojo.isObject(values)) {
				values = {};
				this.setItemValue(item, "options", values);
			}
			this.convertFormBooleans(values, true);
			form.setFormValues(values);
		}, form));
		dojo.connect(dlg, "onExecute", dojo.hitch(this, function(form) {
			var values = this.getItemValue(item, "options");
			this.convertFormBooleans(values, true);
			form.setFormValues(values);
		}, form));
		dlg.show();
		return dlg;
	},
	
	formatItem : function(text, rowIndex) {
		var item = this.getItem(rowIndex);
		if(item) {
			var type = this.getStore().getValue(item, "type");
			var options = this.getStore().getValue(item, "options");
			if(type == "assemblyline") {
				return this.createForm(item, "EditSolutionAssemblyLine", options);
			} else if (type == "connector") {
				return this.createForm(item, "EditSolutionConnector", options);
			}
		}
		return text;
	},
	
	postCreate : function() {
		this.inherited("postCreate", arguments);
		
		this.addToToolbar(new dijit.form.Button({
			label:this.getString("WebCE.deleteItem"),
			onClick:dojo.hitch(this, "onDeleteItem")
		}));
		
//		this.addToToolbar(new dijit.form.Button({
//			label:this.getString("general.moveup.label"),
//			onClick:dojo.hitch(this, "onMoveUp")
//		}));
//		
//		this.addToToolbar(new dijit.form.Button({
//			label:this.getString("general.movedown.label"),
//			onClick:dojo.hitch(this, "onMoveDown")
//		}));
		
	}
});

dojo.declare("tdi.EditConnectorParams", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:

	templateString : "<div><ul dojoAttachPoint='Main'></ul></div>",
	widgetsInTemplate : true,
	
	postCreate : function() {
		var id = this.item.id;
		if(dojo.isArray(this.item.id))
			id = this.item.id[0];
		if(id == null)
			return;
		
		var assemblyline = id;
		var connector = null;
		var arr = id.split(/[.:]/);
		if(arr.length > 1) {
			assemblyline = arr[0];
			connector = arr[arr.length-1];
		}
		
		if(!this.item.options) {
			this.options = {};
			this.item.options = this.options;
		} else if(dojo.isArray(this.item.options)) {
			this.options = this.item.options[0];
		} else {
			this.options = this.item.options;
		}
		
		if(!this.options[this.key]) {
			this.options[this.key] = {};
		} else if(typeof this.options[this.key] == "string") {
			var obj = {};
			var arr = this.options[this.key].split(",");
			for(i = 0; i < arr.length; i++) {
				obj[arr[i]] = "true";
			}
			this.options[this.key] = obj;
			this.item.options[this.key] = obj;
		}
		
		var conn = this.config.getAssemblyLine(assemblyline).getConnector(connector).getConnectorType();
		var div = dojo.create("div", null, this.Main);
		
		dojo.when(tdiapi.getConnectorForm(conn), dojo.hitch(this, function(data) {
			dojo.forEach(data.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
				var d = dojo.create("div", null, div);
				var cb = new dijit.form.CheckBox({name:p.key}).placeAt(d);
				var val = this.options[this.key][p.key];
				if(val != null)
					cb.set("value", val);
				dojo.connect(cb, "onChange", dojo.hitch(this, function(key, value) {
					this.options[this.key][key] = value;
				}, p.key));
				dojo.create("span", {innerHTML:tdiutil.getFormLabel(p) + " (" + p.key + ")"}, d);
			}));
		}));
	}
});
