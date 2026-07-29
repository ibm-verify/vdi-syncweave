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
dojo.provide("tdi.ConnectorEditor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Toolbar");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.StackContainer");
dojo.require("dijit.form.SimpleTextarea");

dojo.require("tdi.AttributeMap");
dojo.require("tdi.AttributeMapItemEditor");
dojo.require("tdi.FormWidget");
dojo.require("tdi.HookEditor");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.ParserEditor");
dojo.require("tdi.SelectComponent");
dojo.require("tdi.model.ComponentsModel");
dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdisession");

dojo.declare("tdi.ConnectorEditor", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for basic editing of a connector.
	// description:
	//		The widget provides simple editing of the attribute maps
	//		and its hooks.

	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/ConnectorEditor.html"),
	widgetsInTemplate : true,
	
	// config: tdi.connector
	//		The connector configuration
	config: null,
	
	// title: String
	//		The title string before the config button (default is connector name)
	title: null,
	
	// hideNullValues: boolean
	//		Hides parameters with no value set
	hideNullValues: true,
	
	// visibleButtons
	//		The buttons to show in the formt
	visibleButtons: [true, true, false, true],
	
	// _input: boolean
	//		Input connector?
	_input: false,
	
	// options: object
	//
	options: {
		ShowConfig:true,
		ShowAttMap: true
	},
	
	querySchema : function() {
		
	},
	
	_testConnection : function() {
		this.testsession = new tdi.tdisession();
		this._connectorform.setButtonEnabled(this._connectorform.testButtonId, false);
		dojo.when(this.testsession.openSessionForConnector(this.config), dojo.hitch(this, function(data) {
			tdiapi.stopConfig(this.testsession.cientry);
			this._connectorform.setButtonEnabled(this._connectorform.testButtonId, true);
			tdiutil.alert(this.getString("ConfigSettingsEditor.TestConnection.OK", [this.config.getName()]));
		}), dojo.hitch(this, function(err) {
			tdiapi.stopConfig(this.testsession.cientry);
			this._connectorform.setButtonEnabled(this._connectorform.testButtonId, true);
			tdiutil.error(err);
		}));
	},
	
	onReadNext : function(map) {
		this.attmap.enableReadNext(false);
		this.attmap.enableClose(false);
		if(this.session == null) {
			this.session = new tdi.tdisession();
			dojo.when(this.session.openSessionForConnector(this.config), dojo.hitch(this, function(data) {
				this.onReadNext();
			}), dojo.hitch(this, function(err) {
				this.onCloseConnection(null);
				tdiutil.error(err);
			}));
		} else {
			dojo.when(this.session.getNextEntry(), dojo.hitch(this, function(entry) {
				if(entry == null) {
					this.attmap.showEODMsg(true);
					this.onCloseConnection(null);
				} else {
					this.attmap.showEODMsg(false);
					this.attmap.setEntry(entry);
					this.attmap.enableClose(true);
					this.attmap.enableReadNext(true);
				}
			}), dojo.hitch(this, function(err) {
				this.onCloseConnection(null);
				tdiutil.error(err);
			}));
		}
	},
	
	onCloseConnection : function(map) {
		if(this.session != null) {
			dojo.when(tdiapi.stopConfig(this.session.cientry), dojo.hitch(this, function() {
				this.session = null;
				this.attmap.enableReadNext(true);
				this.attmap.enableClose(false);
			}));
		}
	},
	
	_loadAttMap : function() {
		this._input = (this.config.getMode() == "Iterator" || this.config.getMode() == "Lookup");
		var map = new tdi.AttributeMap({config:this.config, input:this._input});
		var pane = new dijit.layout.ContentPane({
			title:this.getString("attributeMap"),
			content:map
		});
		map.onReadNext = dojo.hitch(this, "onReadNext");
		map.onCloseConnection = dojo.hitch(this, "onCloseConnection");
		this.attmap = map;
		this.TabContainer.addChild(pane);
	},
	
	_loadHooks : function() {
		dojo.forEach(this.config.getHookNames(), dojo.hitch(this, function(name) {
			var hook = new tdi.HookEditor({config:this.config.getHook(name)}).placeAt(this.Hooks);
		}));
	},
	
	_loadParserForm : function() {
		this._parser = new tdi.ParserEditor({config:this.config, hideNullValues:this.hideNullValues});
		this._parser.placeAt(this.Configuration, "last");
	},
		
	_editHook : function(hook, div) {
		
	},
	
	_editAttribute : function(input, div) {
		// summary:
		//		Opens the attribute map editor for the specified attmap item
		// div: tdi.AttributeMapItem
		//		The attribute map item config
		// input: boolean
		//		True if the map is from the input connector
		var attr = div.getName(); // childNodes[0].innerHTML;
		var map = input ? this.config.getAttributeMap(input) : this.config.getAttributeMap(input);
		var ami = map.getItem(attr);
		var schema = this.config.getSchema(input).getNames();
		try {
			this.editor.editAttribute({ami:ami, attr:attr, map:map, input:input, source:this, availableAttributes:schema})
			div.openDropDown();
		} catch (err) {
			alert(err)
		}
	},
	
	_getTooltipDialog : function() {
		if(this.editor == null) {
			this.editor = new tdi.AttributeMapItemEditor({});
			this.tooltipDialog = new dijit.TooltipDialog({
				content:this.editor
			});
		}
		return this.tooltipDialog;
	},
	
	_loadForm : function(data) {
		
		this._connectordiv = dojo.create("div", {innerHTML:"<p></p><b>" + this._getTitle(data) + "</b>", style:"border-bottom:1px solid #cdcdcd"}, this.Configuration);

		this._connectorform = new tdi.FormWidget({
			formData:data,
			verticalLayout:true,
			config:this.config.getConnectionConfig(),
			hideNullValues:this.hideNullValues,
			visibleButtons:this.visibleButtons,
			visibleParams:this.options.ConnectorParamsVisible,
			editableParams:this.options.ConnectorParamsEditable,
			toolbarButtons:this.toolbarButtons}
		);
		
		this._connectorform.placeAt(this.Configuration, "last");
		this._connectorform.reloadForm = dojo.hitch(this, "_reloadForm");
		this._connectorform.resetForm = dojo.hitch(this, "_resetForm");
		this._connectorform.testConnection = dojo.hitch(this, "_testConnection");
		this._connectorform.querySchema = dojo.hitch(this, "querySchema");
		if(data.useParser == "required" || data.useParser == "optional") {
			this._parserdiv = dojo.create("div", {innerHTML:"<p/>"}, this.Configuration);
			this._loadParserForm();
		} else if(this._parser != null) {
			this._parser.destroy();
			this._parser = null;
		}
	},
	
	_loadConnectorForm : function() {
		if(this.Configuration == null) {
			this.Configuration = dojo.create("div", null);
			var tab = new dijit.layout.ContentPane({
				title:this.getString("PropertyStore.Connector"),
				content:this.Configuration
			});
			this.TabContainer.addChild(tab);
		}
		var type = this._getConnType();
		if(this.config != null && type && type != "[parent]") {
			dojo.when(tdiapi.getConnectorForm(type, "en"), dojo.hitch(this, "_loadForm"));
		} else {
			this._resetForm();
		}
	},
	
	_getTitle : function(data) {
		var nls = "en";
		var str = "";
		dojo.forEach(data.name, function(label) {
			if(label.lang == nls) {
				str = label.value; // p.required ? labs.value + " *" : labs.value;
			}
		});
		return str;
	},
	
	_reloadForm : function() {
		if(this._connectorform != null) {
			this._connectorform.destroy();
			this._connectorform = null;
			dojo.destroy(this._connectordiv);
			if(this._parser != null) {
				this._parser.destroy();
				this._parser = null;
			}
		}
		this._loadConnectorForm();
	},
	
	_resetForm : function() {
		
		if(this._connectorform != null) {
			this._connectorform.destroy();
			this._connectorform = null;
			dojo.destroy(this._connectordiv);
			if(this._parser != null) {
				this._parser.destroy();
				this._parser = null;
			}
		}
		
		this._selectForm = dojo.create("div", {style:"width:100%;height:100%;position:relative; top:20px; left:20px; "}, this.Configuration);
		dojo.create("div", {innerHTML:this.getString("WebCE.chooseType")}, this._selectForm);
		this._modelCB = new dijit.form.FilteringSelect({
			store:new tdi.model.ComponentsModel({componentType:"connector"})
		}).placeAt(this._selectForm);
		this._modelCB.onChange = dojo.hitch(this, function(item, label) {
			if(item != null && item.length > 0) {
				try {
					if(item.match(/^adapter:/))
						this.config.setConnectorType(item);
					else
						this.config.setConnectorType("system:/Connectors/" + item);
					this._modelCB.destroy();
					this._modelCB = null;
					dojo.destroy(this._selectForm);
					this._loadConnectorForm();
				} catch(err) {
					alert("setInherit: " + item + ": " + err);
				}
			}
		});
	},

	_getConnType : function() {
		var con = null;
		if(this.config) {
			con = this.config.getConnectorType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
		}
		return con;
	},
	
	resize : function(obj) {
		if(this.TabContainer != null)
			this.TabContainer.layout();
	},
	
	postCreate : function() {
		if(this.itemDef != null && dojo.isArray(this.itemDef.options)) {
			if(this.itemDef.options.length == 1) {
				this.options = this.itemDef.options[0];
				if(this.options.ShowConfig == null)
					this.options.ShowConfig = true;
				if(this.options.ShowAttMap == null)
					this.options.ShowAttMap = true;
			}
		}
		if(this.options.ShowConfig)
			this._loadConnectorForm();
		
		if(this.options.ShowAttMap)
			this._loadAttMap();
	}
});
