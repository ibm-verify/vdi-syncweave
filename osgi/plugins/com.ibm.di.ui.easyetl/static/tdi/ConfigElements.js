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
 * @version     1.4, 8/19/11
 * @owner       
 * @history
 */
dojo.provide("tdi.ConfigElements");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.Tree");
dojo.require("dijit.Toolbar");
dojo.require("dijit.form.Form");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.TitlePane");

dojo.require("dijit.tree.dndSource");

dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("dojo.dnd.Source");

dojo.require("tdi.model.ConfigElementsModel");
dojo.require("tdi.model.PublishedObjectsStore");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.ConfigElements", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/ConfigElements.html"),
	widgetsInTemplate : true,

	// config: tdi.tdiconfig
	//		The configuration file loaded from the server
	config: null,
	
	alOptionsForm : {
		parameterMapDescriptor: {
			sectionDescriptor: [
			     {
			    	 label: [
			    	    {lang:"en", value:"General"}
			    	 ]
			     }
			],
			parameterDescriptor: [
				{
					section:"General",
					key: "ShowRun",
					label: [
						{lang:"en", value:"Show run tab"}
					],
					type: "boolean"
				},
				{
					section:"General",
					key: "ShowConfig",
					label: [
						{lang:"en", value:"Show schedules tab"}
					],
					type: "boolean"
				},
				{
					section:"General",
					key: "ShowConnectors",
					label: [
						{lang:"en", value:"Include all connectors"}
					],
					type: "boolean"
				},
				{
					section:"General",
					key: "DisplayName",
					label: [
						{lang:"en", value:"Display name"}
					],
					type: "text"
				}
			]
		}
	},
	
	connectorOptionsForm : {
		parameterMapDescriptor: {
			sectionDescriptor: [
			     {
			    	 label: [
			    	    {lang:"en", value:"General"}
			    	 ]
			     }
			],
			parameterDescriptor: [
				{
					section:"General",
					key: "ShowAttMap",
					label: [
						{lang:"en", value:"Show attributemap"}
					],
					type: "boolean"
				},
				{
					section:"General",
					key: "ShowConfig",
					label: [
						{lang:"en", value:"Show connector config"}
					],
					type: "boolean"
				},
				{
					section:"General",
					key: "ConnectorParamsEditable",
					label: [
						{lang:"en", value:"Editable parameters"}
					],
					type: "textarea",
					webScript:"selectEditableConnectorParameters",
					webLabel:"..."
				},
				{
					section:"General",
					key: "ConnectorParamsVisible",
					label: [
						{lang:"en", value:"Visible parameters"}
					],
					webScript:"selectVisibleConnectorParameters",
					webLabel:"...",
					type: "textarea"
				},
				{
					section:"General",
					key: "DisplayName",
					label: [
						{lang:"en", value:"Display name"}
					],
					type: "text"
				}
			]
		}
	},	
    
    propertyOptions: [
    ],
                   
	checkAcceptance : function(tree) {
		return true;
	},
	
	checkItemAcceptance : function(target, source, position) {
		console.log("checkItemAcceptance: target=" + target + ", source=" + source + ", pos=" + position)
		return true;
	},
	
	pasteItem: function(/*Item*/ childItem, /*Item*/ oldParentItem, /*Item*/ newParentItem, /*Boolean*/ bCopy, /*int?*/ insertIndex) {
		console.log("pasteItem: " + childItem + "; " + insertIndex);
	},
	
	itemCreator: function(/*DomNode[]*/ nodes, target, /*dojo.dnd.Source*/ source) {
		dojo.forEach(nodes, function(node) {
			var tn = dijit.byId(node.id);
		});
		return [];
	},
	
	removeExposedItem : function(source, nodes, copy) {
		console.log("Remove exposed items");
		this._tree.dndController.onDndCancel();
	},
	
	_applySettings : function() {
		this.updateConfig();
		this.applySettings();
	},
	
	applySettings : function() {
		this.updateConfig();
	},
	
	addPropertySet : function() {
		var str = prompt("Enter property set name: ");
		if(str != null) {
			var item = {id:"@"+str, name:str, properties:[]};
			this.addElement(new tdi.ConfigElementsOptions({title:str, item:item, isProperty:true, propertySet:item.properties}));
			this.config.getSolutionInterface().getExposedAssemblyLines().push({name:item.id});
		}
	},
	
	addExposedItem : function() {
		var div = null;
		var item = this._selectedTreeItem;
		if(item == null)
			return;
		
		var ident = item.id[0].replace("/", ":");
		var label = item.name[0];
		var item = {
				id:ident, 
				name:label,
				options:[]
		}
		if(ident.indexOf(":") == -1) {
			div = new tdi.ConfigElementForm({title:label, item:item, config:this.config, formData:this.alOptionsForm})
		} else {
			div = new tdi.ConfigElementForm({title:label, item:item, config:this.config, formData:this.connectorOptionsForm})
		}
		this.addElement(div);
	},
	
	createTree : function() {
		this._store = new tdi.model.ConfigElementsModel({config:this.config, cientry:this.cientry, allNames:true});
		this._model = new dijit.tree.ForestStoreModel({
	        store: this._store,
	        rootId: "configRoot",
	        rootLabel: "Server Projects",
	        childrenAttrs: ["items"]
	    });
		this._tree = new dijit.Tree({
			model:this._model,
			showRoot:false,
			dndController:"dijit.tree.dndSource",
			onDndDrop: dojo.hitch(this, "removeExposedItem"),
			itemCreator: dojo.hitch(this, "itemCreator"),
			checkAcceptance: function() {return true},
			checkItemAcceptance: function() {return true}			
		});
		
		this.menu = new dijit.Menu({});
		this.menu.addChild(new dijit.MenuItem({
			label:"Add to right",
			onClick:dojo.hitch(this, "addExposedItem")
		}));
		this.menu.startup();
		
		dojo.connect(this._tree, "onRowContextMenu", this, function(e) {
			this._selectedTreeItem = this._tree.getItem(e.rowIndex);
			this.menu.bindDomNode(e.cellNode);
			this.menu._scheduleOpen(e.cellNode);
		});
		
		dojo.connect(this._tree, "onDblClick", this, function(e) {
			this._selectedTreeItem = e; //this._tree.getItem(e.rowIndex);
			this.addExposedItem();
		});
		
		dojo.connect(this._tree, "onClick", this, function(e) {
			this._selectedTreeItem = e; // this._tree.getItem(e.rowIndex);
		});
		
		this._tree.placeAt(this.Left);
	},

	createElementEditors : function() {
		var div = dojo.create("div", {}, this.Right);
		
		this.elementsDiv = div;
		
		var pos = new tdi.model.PublishedObjectsStore({config:this.config});
		dojo.forEach(pos.dataArray, dojo.hitch(this, function(item) {
			var elem = null;
			if(item.type == "connector")
				elem = new tdi.ConfigElementForm({config:this.config, title:item.name + " (Connector)", item:item, formData:this.connectorOptionsForm});
			else if(item.type == "properties")
				elem = new tdi.ConfigElementsOptions({title:item.name + " (Properties)", options:this.propertyOptions, propertySet:item.properties, item:item});
			else {
				elem = new tdi.ConfigElementForm({config:this.config, title:item.name + " (AssemblyLine)", item:item, formData:this.alOptionsForm});
			}
			this.addElement(elem);
		}));

		
		this.dnd = new dojo.dnd.Source(div, {});
		this.dnd.checkAcceptance = dojo.hitch(this, function(source, nodes) {
			return true;
		});
		this.dnd.checkItemAcceptance = dojo.hitch(this, function(target, source, position) {
			return true;
		});
		this.dnd.onDropExternalOrg = this.dnd.onDropExternal;
		this.dnd.onDropExternal = dojo.hitch(this, function(source, nodes, copy) {
			var wasTreeDrop = false;
			dojo.forEach(nodes, dojo.hitch(this, function(item) {
				var wid = dijit.getEnclosingWidget(item);
				if(wid != null && wid.item != null) {
					varTreeDrop = true;
					var ident = wid.item.id[0].replace("/", ":");
					var label = wid.item.name[0];
					if(wid.item.storeName != null) {
						if(this.dnd.targetAnchor != null) {
							var ceo = dijit.getEnclosingWidget(this.dnd.targetAnchor);
							if(ceo != null && ceo.propertySet != null) {
								ceo.addProperty(this.config.getSolutionInterface().getExposedProperty(wid.item.storeName[0], label));
							}
						}
					} else {
						var div = null;
						var item = {
								id:ident, 
								name:label,
								options:[]
						}
						if(ident.indexOf(":") == -1) {
							div = new tdi.ConfigElementForm({title:label, item:item, config:this.config, formData:this.alOptionsForm})
						} else {
							div = new tdi.ConfigElementForm({title:label, item:item, config:this.config, formData:this.connectorOptionsForm})
						}
						this.addElement(div);
						this.dnd.insertNodes(true, [div.domNode], true, this.dnd.current);
					}
				}
			}));
		});
	},
	
	addElement: function(element) {
		element.placeAt(this.elementsDiv);
		this._elements.push(element);
	},
	
	removeElement: function(element) {
		this._elements = dojo.filter(this._elements, function(e) {
			return e.item.orgvalue != element.item.orgvalue;
		});
	},
	
	updateConfig: function() {
		var arr = [];
		dojo.forEach(this.elementsDiv.childNodes, function(e) {
			var wid = dijit.byId(e.id);
			if(wid != null) {
				var data = wid.item.options;
				if(dojo.isArray(data))
					data = data[0];
				
				var str = wid.item.id + (data != null ? "!" + dojo.toJson(data) : "");
				arr.push({name:str});
			}
		});
		this.config.getSolutionInterface().setExposedAssemblyLines(arr);
	},
	
	loadProperties: function(data) {
		this.cientry = new tdi.tdicientry({atom:data});
		this.createTree();
		this.createElementEditors();
	},
	
	stopTempInstance : function() {
		if(this.instanceId != null) {
			tdiapi.stopConfig(this.cientry);
			this.cientry = null;
		}
	},
	
	destroyRecursive : function() {
		this.stopTempInstance();
		this.inherited(arguments);
	},
	
	postCreate : function() {
		this._elements = [];
		if(this.cientry == null) {
			this.instanceId = tdiutil.generateInstanceId(this.config) 
			dojo.when(tdiapi.startTempConfig(this.config, this.instanceId), dojo.hitch(this, "loadProperties"));
		} else {
			this.createTree();
			this.createElementEditors();
		}
	}
});

dojo.declare("tdi.ConfigElementsOptions", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	templateString : "<div class='dojoDndItem'><div dojoType='dijit.TitlePane' open='false' dojoAttachPoint='Main'></div></div>",
	widgetsInTemplate : true,
	
	options: [],

	title: null,

	_createCombo : function(item, table) {
		var tr = dojo.create("tr", {}, table);
		var td = dojo.create("td", {}, tr);
		dojo.create("span", {innerHTML:item.label + ":"}, td);
		var data = {
			identifier: "id",
			label: "name",
			items : []
		}
		var store = new dojo.data.ItemFileWriteStore({data:data});
		dojo.forEach(item.values, function(f) {
			store.newItem({id:f, name:f});
		});
		td = dojo.create("td", {}, tr);
		item.control = new dijit.form.ComboBox({name:item.name, searchAttr:"name", store:store}).placeAt(td);
		dojo.connect(item.control, "onChange", dojo.hitch(this, "saveValues"));
	},
	
	_createText : function(item, table) {
		var tr = dojo.create("tr", {}, table);
		var td = dojo.create("td", {}, tr);
		dojo.create("span", {innerHTML:item.label + ":"}, td);
		td = dojo.create("td", {}, tr);
		item.control = new dijit.form.TextBox({name:item.name}).placeAt(td);
		dojo.connect(item.control, "onChange", dojo.hitch(this, "saveValues"));
	},
	
	_createTextArea : function(item, table) {
		var tr = dojo.create("tr", {}, table);
		var td = dojo.create("td", {}, tr);
		dojo.create("span", {innerHTML:item.label + ":"}, td);
		td = dojo.create("td", {}, tr);
		item.control = new dijit.form.Textarea({name:item.name}).placeAt(td);
		dojo.connect(item.control, "onChange", dojo.hitch(this, "saveValues"));
	},
	
	_createCheckBox : function(item, table) {
		var tr = dojo.create("tr", {}, table);
		var td = dojo.create("td", {colspan:"2"}, tr);
		item.control = new dijit.form.CheckBox({name:item.name}).placeAt(td);
		dojo.connect(item.control, "onChange", dojo.hitch(this, "saveValues"));
		dojo.create("span", {innerHTML:item.label}, td);
	},
	
	_setOptions : function() {
		for(var i = 0; i < this.options.length; i++) {
			var item = this.options[i];
			var tr = dojo.create("tr", {}, this.table);
			if(item.type == "boolean") {
				this._createCheckBox(item, this.table);
			} else if(item.type == "dropdown") {
				this._createCombo(item, this.table);
			} else if(item.type == "textarea") {
				this._createTextArea(item, this.table);
			} else if(item.type == "connector_params") {
				this._createTextArea(item, this.table);
			} else {
				this._createText(item, this.table);
			}
		}		
	},
	
	addProperty : function(item) {
		var tr = dojo.create("tr", {}, this.table);
		var td = dojo.create("td", {colspan:"2"}, tr);
		var options = [
		    {name:"name", label:"Property name"},
		    {name:"label", label:"Property label"},
		    {name:"storeName", label:"Property store"},
		    {name:"userComment", label:"Description", type:"textarea"}
//		    {name:"inputType", label:"Type", type:"dropdown", values:["Text", "Checkbox"]}
		]
		item.category = this.item.name;
		var ceo = new tdi.ConfigElementsOptions({title:item.storeName+":"+item.name, item:item, isProperty:true, options:options}).placeAt(td);
	},
	
	saveValues: function() {
		var obj = this.form.get("value");
		if(this.isProperty) {
			for (var f in obj) {
				this.item[f] = obj[f];
			}
		} else {
			this.item.options = obj;
		}
		this.config.setModified(true);
	},
	
	_setProperties : function() {
		dojo.forEach(this.propertySet, dojo.hitch(this, function(item) {
			this.addProperty(item);
		}));
	},
	
	postCreate : function() {
		this.Main.attr("title", this.title);
		this.list = new Array();
		this.form = new dijit.form.Form({}).placeAt(this.Main.containerNode);
		this.table = dojo.create("table", {}, this.form.domNode);
		if(this.options != null) {
			this._setOptions();
		}
		if(this.propertySet != null) {
			this._setProperties();
		}
		
		this.form.startup();
		
		if(this.item != null) {
			if(this.item.options != null)
				this.form.set("value", this.item.options);
			else if(this.isProperty)
				this.form.set("value", this.item);
		}
	}
	
});

dojo.declare("tdi.SelectConnectorParams", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:

	templateString : "<div><ul dojoAttachPoint='Main'></ul></div>",
	widgetsInTemplate : true,
	
	postCreate : function() {
		var id = this.item.id;
		if(id == null)
			return;
		var assemblyline = id;
		var connector = null;
		if(id.indexOf(":")) {
			assemblyline = id.substring(0,id.indexOf(":"));
			connector = id.substring(id.indexOf(":")+1);
		}
		var conn = this.config.getAssemblyLine(assemblyline).getConnector(connector).getConnectorType();
		var div = dojo.create("div", null, this.Main);
		if(this.item.options[this.key] == null)
			this.item.options[this.key] = {};
		
		dojo.when(tdiapi.getConnectorForm(conn), dojo.hitch(this, function(data) {
			dojo.forEach(data.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
				var d = dojo.create("div", null, div);
				var cb = new dijit.form.CheckBox({name:p.key}).placeAt(d);
				var val = this.item.options[this.key][p.key];
				if(val != null)
					cb.set("value", val);
				dojo.connect(cb, "onChange", dojo.hitch(this, function(key, value) {
					this.item.options[this.key][key] = value;
				}, p.key));
				dojo.create("span", {innerHTML:tdiutil.getFormLabel(p) + " (" + p.key + ")"}, d);
			}));
		}));
	}
});

dojo.declare("tdi.ConfigElementForm", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	templateString : "<div class='dojoDndItem'><div dojoType='dijit.TitlePane' open='false' dojoAttachPoint='Main'></div></div>",
	widgetsInTemplate : true,
	
	options: [],

	title: null,
	
	formCB : function(source, formItem) {
		if(this[formItem.webScript] != null)
			this[formItem.webScript](source, formItem);
	},
	
	selectConnectorParameters: function(source, formItem, title, param) {
		var sel = new tdi.SelectConnectorParams({config:this.config, item:this.item, key:formItem.key});
		var dlg = new dijit.Dialog({
			title:title,
			content:sel
		});
		dojo.connect(dlg, "onCancel", dojo.hitch(this, function() {
			this.form.updateControl(param);
		}));
		dlg.show();
		return dlg;
	},
	
	selectEditableConnectorParameters: function(source, formItem) {
		this.selectConnectorParameters(source, formItem, "Select editable connector parameters", "ConnectorParamsEditable");
	},
	
	selectVisibleConnectorParameters: function(source, formItem) {
		this.selectConnectorParameters(source, formItem, "Select visible connector parameters", "ConnectorParamsVisible");
	},
	
	getParamValue: function(param) {
		var value = dojo.getObject("options." + param, false, this.item);
		if((param == "ConnectorParamsEditable" || param == "ConnectorParamsVisible") && dojo.isObject(value)) {
			var val = "";
			for(var f in value) {
				if(value[f] == true)
					val += f + "\n";
			}
			value = val.trim();
		}
		return value;
	},
	
	setParamValue: function(param, value) {
		var val = value;
		if((param == "ConnectorParamsEditable" || param == "ConnectorParamsVisible")) {
			var arr = value.split("\n");
			val = {};
			dojo.forEach(arr, function(p) {
				val[p] = true;
			});
		}
		dojo.setObject("options." + param, val, this.item);
	},
	
	postCreate : function() {
		this.Main.attr("title", this.title);
		if(this.item == null)
			this.item = {};
		this.form = new tdi.FormWidget({
			callback:dojo.hitch(this, "formCB"),
			verticalLayout:true,
			formData:this.formData,
			visibleButtons: [false, false, false, false],
			getParamValue: dojo.hitch(this, "getParamValue"),
			setParamValue: dojo.hitch(this, "setParamValue")
		});
		this.Main.set("content", this.form);
	}
	
});
