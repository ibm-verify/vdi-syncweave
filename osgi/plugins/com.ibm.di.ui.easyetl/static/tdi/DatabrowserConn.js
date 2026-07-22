/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
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
dojo.provide("tdi.DatabrowserConn");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("dijit.form.Button");
dojo.require("dijit.form.ComboBox");

dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");

dojo.require("tdi.ConnectorEditor");
dojo.require("tdi.SelectComponent");
dojo.require("tdi.TreeTableWidget");
dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");

dojo.declare("tdi.DatabrowserConn", [dijit._Widget, dijit._Templated, tdi.NlsMixin], {
	
	templatePath : dojo.moduleUrl("tdi", "templates/DatabrowserConn.html"),
	widgetsInTemplate : true,
	
	formWidget: null,
	
	resize : function(obj) {
		if(this.top)
			this.top.resize(obj);
		if(this.formWidget)
			this.formWidget.resize(obj);
	},
	
	connectTarget : function() {
		
	},
	
	saveTarget : function() {
	},
	
	addTarget : function() {
		var copy = tdiutil.clone(this.config.config);
		var name = copy.name;
		var count = 1;
		while(this.savedConfig.getConnector(name) != null) {
			name = copy.name + "_" + count++;
		}
		this.savedConfig.addConnector(name, copy);
		this.oldConnectors.getStore().newItem({
			id:name,
			name:name
		});
	},
	
	setSaveLabel : function(label) {
		this._saveButton.set("label", label);
	},
	
	deleteForms : function() {
		this.deleteConnectorForm();
		this.deleteParserForm();
	},
	
	deleteConnectorForm: function() {
		if(this.formWidget) {
			this.tabs.removeChild(this.formWidget);
			this.formWidget.destroyRecursive();
			this.formWidget = null;
		}
	},
	
	deleteParserForm : function() {
		if(this.formWidgetParser) {
			this.tabs.removeChild(this.formWidgetParser);
			this.formWidgetParser.destroyRecursive();
			this.formWidgetParser = null;
		}
	},
	
	loadForm : function(conn) {
		
		this.deleteForms();
		
		dojo.when(tdiapi.getConnectorForm(conn, "en"), dojo.hitch(this, function(formdata) {
			var form = new tdi.FormWidget({
				formData:formdata,
				verticalLayout:true,
				config:this.config.getConnectionConfig(),
				hideNullValues:false,
				visibleButtons:[false, false, false, false],
				style:"width:100%; height:100%"
			});
			this.formWidget = new dijit.layout.ContentPane({
				title:this.getString("PropertyStore.Connector"),
				content:form
			});
			this.tabs.addChild(this.formWidget);
			this.tabs.selectChild(this.formWidget);
			this.config.setConnectorType("system:/Connectors/" + conn);

			this.newParsers.set("disabled", formdata.useParser == "prohibit");
						
			if(formdata.useParser == "required" || formdata.useParser == "optional") {
				setTimeout(dojo.hitch(this, function() {
					this.tabs.selectChild(this.formWidget);
				}), 100);
			} else {
				this.deleteParserForm();
				this.config.getParserConfig().setParserType("[parent]");
			}
		}));
	},
	
	loadParserForm : function(parser, select) {
		this.deleteParserForm();
		dojo.when(tdiapi.getParserForm(parser, "en"), dojo.hitch(this, function(formdata) {
			this.config.getParserConfig().setParserType("system:/Parsers/" + parser);
			var form = new tdi.FormWidget({
				formData:formdata,
				verticalLayout:true,
				config:this.config.getParserConfig(),
				hideNullValues:false,
				visibleButtons:[false, false, false, false],
				style:"width:100%; height:100%"
			});
			this.formWidgetParser = new dijit.layout.ContentPane({
				title:this.getString("parser"),
				content:form
			});
			this.tabs.addChild(this.formWidgetParser);
			this.tabs.selectChild(this.formWidgetParser);
			
		}));
	},
	
	openConnector : function(arg) {
		var store = this.newConnectors.store;
		store.fetch({
			query: {name:arg},
			onItem: dojo.hitch(this, function(item) {
				this.loadForm(store.getValue(item, "id"))
			})
		});
	},
	
	openParser : function(arg) {
		var store = this.newParsers.store;
		store.fetch({
			query: {name:arg},
			onItem: dojo.hitch(this, function(item) {
				this.loadParserForm(store.getValue(item, "id"))
			})
		});
	},
	
	findSavedConnectors : function() {
		dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, function(result) {
			for(f in result.items) {
				var item = result.items[f];
				if(item.name == "Databrowser") {
					this.savedConnectors = item.entry;
					this.loadSavedConnectors();
					return;
				}
			}
		}));
	},
	
	loadSavedConnectors : function() {
		dojo.when(tdiapi.getConfig(this.savedConnectors), dojo.hitch(this, function(config) {
			this.savedConfig = new tdi.tdiconfig({data:config});
			this.createSavedTable();
		}));
	},
	
	createSavedTable : function() {
		// -- old connectors from browser config
		this.oldConnectors = new tdi.TreeTableWidget({
			treeTableLayout: [
				{field:"name", name:"Saved Connectors", width:"auto"}
			],
			getTreeTableStyle : function() {
				return "padding:0px; margin:0px";
			},
			toolbarVisible: false,
			storeLabelAttribute: "name"
		});
		
		dojo.forEach(this.savedConfig.getConnectorNames(), function(name) {
			this.oldConnectors.getStore().newItem({
				id:name,
				name:name
			});
		});
		
		
		this.tableCP.set("content", this.oldConnectors);
		this._addButton.set("disabled", false);
	},
	
	getConnectorID : function() {
		var type = this.config.getConnectorType();
		if(type) {
			var match = type.match(/system:\/Connectors\/(.*)/)
			if(!match && type)
				match = type.match(/rest\/internal\/system\/Connectors\/(.*)/)
			if(match && match.length == 2) {
				return match[1];
			}
		}
		return null;
	},
	
	getParserID : function() {
		var type = this.config.getParserType();
		if(type) {
			var match = type ? type.match(/system:\/Parsers\/(.*)/) : [];
			if(!match && type)
				match = type.match(/rest\/internal\/system\/Parsers\/(.*)/)
			if(match && match.length == 2) {
				return match[1];
			}
		}
		return null;
	},
	
	createDatabrowserStore : function() {
		var sol = new tdi.tdiconfig({});
		sol.setConfigName("Databrowser");
		sol.getSolutionInterface().setUserComment("This solution contains connectors that are used by the Databrowser.");
		dojo.when(tdiapi.createSolution(sol), dojo.hitch(this, function(atom) {
			// we need a little time for the remote api to register
			setTimeout(dojo.hitch(this, "findSavedConnectors"), 1000);
		}), tdiapi.defaultErrHandler);
	},
	
	showDatabrowserCollection : function() {
		var tmp = dojo.create("div", {style:"margin:15px"});
		var div = dojo.create("div", {innerHTML:"You must create a solution called 'Databrowser' to save connectors in this view."}, tmp);
		var btn = new dijit.form.Button({
			label:"Create it for me",
			onClick:dojo.hitch(this, "createDatabrowserStore")
		}).placeAt(tmp, "last");
		this.tableCP.set("content", tmp);
		this._addButton.set("disabled", true);

		this.findSavedConnectors();	
	},
	
	postCreate: function() {
		
		// disable parser drop down
		this.newParsers.set("disabled", true);
		this.newConnectors.set("disabled", true);
		
		// -- new connectors model
		this.model = new tdi.model.ComponentsModel({
			onLoadComplete:dojo.hitch(this, function() {
				this.newConnectors.set("store", this.model);
				this.model.fetch({
					query: {id:this.getConnectorID()},
					onItem: dojo.hitch(this, function(item) {
						this.newConnectors.set("value", this.model.getValue(item, "name"))
					})
				});
				this.newConnectors.set("disabled", false);
			})
		});

		this.parsermodel = new tdi.model.ComponentsModel({
			componentType:"parser",
			onLoadComplete:dojo.hitch(this, function() {
				this.newParsers.set("store", this.parsermodel);
				this.parsermodel.fetch({
					query: {id:this.getParserID()},
					onItem: dojo.hitch(this, function(item) {
						this.newParsers.set("value", this.parsermodel.getValue(item, "name"))
					})
				});
			})
		});
		
		if(this.tableCP) {
			dojo.style(this._addButton.domNode, "display", "none");
			// this.showDatabrowserCollection();
		}		
	}
	
});
