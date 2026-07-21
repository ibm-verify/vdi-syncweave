dojo.provide("tdi.Databrowser");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.form.Button");

dojo.require("tdi.ConnectorEditor");
dojo.require("tdi.DatabrowserConn");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.TreeTableWidget");
dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");

dojo.declare("tdi.Databrowser", [dijit._Widget, dijit._Templated, tdi.NlsMixin], {
	
	// templatePath : dojo.moduleUrl("tdi", "templates/Databrowser.html"),
	templateString: "<div dojoAttachPoint='Root'></div>",
	widgetsInTemplate : true,
	useSimpleConfig: false,
	
	// config: (tdi.connector config object)
	tdiconfig: null,
	
	// show attributes as columns with row data
	isTableView: true,
	
	// identifier used if there is none
	identifier: new Date().getTime(), 
	
	createPanels : function() {
		this.borderContainer = new dijit.layout.BorderContainer({
			style:"width:100%; height:100%; border-width:0px; margin:0; padding:0",
			gutters:false
		}).placeAt(this.Root);
		
		this.configPane = new dijit.layout.ContentPane({
			region:"top",
			splitter:true,
			style:"padding:5px; margin:0; height:30%"
		}).placeAt(this.borderContainer);
		
		this.dataPane = new dijit.layout.ContentPane({
			region:"center",
			splitter:false,
			style:"padding:0; margin:0"
		}).placeAt(this.borderContainer);
	},
	
	onOpenEditor : function(entry) {
		// summary:
		//		Callback after config has been saved
	},
	
	setSaveLabel : function(label) {
		this.editor.setSaveLabel(label);
	},

	resize: function(obj) {
		if(obj && obj.h > 0) {
			var objs = [this.borderContainer];
			dojo.forEach(objs, function(o) {
				if(o != null) {
					o.resize(obj);
				}
			});
		}
	},
	
	destroyRecursive : function() {
		this.inherited(arguments);
		this.borderContainer.destroyRecursive(false);
	},

	postCreate : function() {
		// Create the temp configs
		if(this.config != null) {
			this.conn = this.config;
		} else {
			this.tdiconfig = new tdi.tdiconfig({});
			this.al = this.tdiconfig.createAssemblyLine("TDIDashboard");
			this.conn = this.al.createFeedConnector("Input", "Iterator");
			this.conn.setConnectorType("system:/Connectors/ibmdi.FileSystem");
			this.al.createDataFlowConnector("Output", "AddOnly");
		}
		
		// Main ui layout
		this.createPanels();
		
		// 
		this._connect = new dijit.form.Button({label:this.getString("DiscoverSchemaWidget_connect"), onClick:dojo.hitch(this, "_connectTarget")});
		this._next = new dijit.form.Button({label:this.getString("DiscoverSchemaWidget_next"), onClick:dojo.hitch(this, "_nextPage")});
		this._close = new dijit.form.Button({label:this.getString("close"), onClick:dojo.hitch(this, "_closeTarget")});
		this._toggle = new dijit.form.Button({label:this.getString("toggleView"), onClick:dojo.hitch(this, "_toggleStructure")});
		
		// choose template for connection panel
		var template = this.useSimpleConfig ? "DatabrowserConnTiny.html" : "DatabrowserConn.html";
		
		this.editor = new tdi.DatabrowserConn({
			useSimpleConfig: this.useSimpleConfig,
			templatePath: dojo.moduleUrl("tdi", "templates/" + template),
			config: this.conn,
			style:"width:100%; height:300px; padding:0; margin:0"
		});		
		this.editor.saveTarget = dojo.hitch(this, "_saveTarget");
		
		this.configPane.set("content", this.editor);
		
		this._createTable();
		
		this.borderContainer.startup();
		this._updateButtonStates();
	},
	
	_saveTarget : function() {
		tdiutil.openDialog(null, this.getString("saveIntegration"), "CreateJob.html", dojo.hitch(this, function(form) {
			this.tdiconfig.renameAssemblyLine(this.al.getName(), form.name);
			this.tdiconfig.setConfigName(form.name);
			dojo.when(tdiapi.createSolution(this.tdiconfig), dojo.hitch(this, function(atom) {
				this.onOpenEditor(this.tdiconfig.getConfigName());
			}), function(err) {
				tdiutil.error(err);
			})
		}));
	},
	
	_createTable : function() {
		this.table = new tdi.TreeTableWidget({
			toolbarOptions: {
				actionMenu:false,
				configureTreeTableIcon:true
			},
			getTreeTableLayout : function() {
	  			return [{field:"", name:""}];
	  		},
	  		onClick:dojo.hitch(this, function(event) {
	  			this.currentSelection = event.rowIndex;
	  		})
		});
		
		this.table.addToToolbar(this._connect);
		this.table.addToToolbar(this._next);
		this.table.addToToolbar(this._close);
		this.table.addToToolbar(this._toggle);
		this.dataPane.set("content", this.table);
	},
	
	_updateButtonStates : function() {
		this._connect.set("disabled", this.session);
		this._next.set("disabled", !this.session);
		this._close.set("disabled", !this.session);
		this._toggle.set("disabled", this._getEntries().length == 0);
	},
	
	_getEntries : function() {
		this.entries = this.entries || [];
		return this.entries;
	},
	
	_addEntry : function(entry) {
		this._getEntries().push(entry);
		this._addEntry2Store(entry);
	},
	
	_addEntry2Store : function(entry) {
		var store = this.table.getStore();
		if(this.isTableView) {
			// Add a row with each attribute as a field (column)
			var data = {};
			dojo.forEach(this.layout, function(col) {
				var value = entry.getAttributeValue(col.name);
				if(!value)
					value = "";
				data[col.name] = value;
			});
			// store requires an id
			if(!data.id)
				data.id = this.identifier++;
			try {
				store.newItem(data);
			} catch(err) {
				console.log(err);
			}
		} else {
			// clear current data
			this.table.removeAllItems();
			
			// Add a row for each attribute/value
			dojo.forEach(entry.getNames(), dojo.hitch(this, function(attr) {
				var data = {};
				var value = entry.getAttributeValue(attr);
				if(!value)
					value = "";
				data.id = this.identifier++;
				data.name = attr;
				data.value = value;
				try {
					store.newItem(data);
				} catch(err) {
					console.log(err);
				}
			}));
		}
	},
	
	_recreateModel : function(entryIndex) {
		if(this.isTableView) {
			if(this._getEntries().length > 0) {
				dojo.forEach(this._getEntries(), dojo.hitch(this, function(entry) {
					this._addEntry2Store(entry);
				}));
			} else {
				this._nextPage();
			}
		} else {
			var index = this._getEntries().length-1;
			if(index > -1) {
				if(entryIndex && entryIndex > -1)
					index = entryIndex;
				this._addEntry2Store(this._getEntries()[index]);
			} else {
				this._nextPage();
			}
		}
	},
	
	_toggleStructure : function() {
		this.isTableView = !this.isTableView;
		this.table.removeAllItems();
		if(this.isTableView) {
			this.layout = null;
			this._updateStructure(this._getEntries()[0]);
		} else {
			this.table.getGrid().set("structure",
				[
					{field:"name", name:this.getString("WebCE.name")},
					{field:"value", name:this.getString("WebCE.value")}					
				] 
			);
		}
		this._recreateModel(this.currentSelection);
	},
	
	_updateStructure : function(entry) {
		if(this.layout)
			return;
		this.layout = [];
		dojo.forEach(entry.getNames(), dojo.hitch(this, function(name) {
			this.layout.push({field:name, name:name});
		}));
		this.table.getGrid().set("structure", this.layout);
	},

	_connectTarget : function() {
		this.session = new tdi.tdisession({});
		this.entries = [];
		dojo.when(this.session.openSessionForConnector(this.conn), dojo.hitch(this, function() {
			this._updateButtonStates();
			this._nextPage(true);
		}), dojo.hitch(this, function(err) {
			tdiutil.error(err);
			this._closeTarget();
		}));
	},
	
	_nextPage : function(mouseevent) {
		if(mouseevent) {
			this.entries = [];
			this.table.removeAllItems();
		}
		
		dojo.when(this.session.getNextEntry(), dojo.hitch(this, function(data) {
			if(data == null) {
				this._next.set("disabled", true);
			} else {
				var entry = new tdi.tdientry({data:data});
				this._updateStructure(entry);
				this._addEntry(entry);
				this._updateButtonStates();
				if(this.isTableView) {
					if(this._getEntries().length < 5) {
						this._nextPage();
					}
				}
			}
		}));
	},
	
	_closeTarget : function() {
		if(this.session) {
			this.session.close();
			this.session = null;
			this.layout = null;
			this.entries = null;
			this._updateButtonStates();
			this.table.removeAllItems();
			this.entries = [];
		}
	}
});
