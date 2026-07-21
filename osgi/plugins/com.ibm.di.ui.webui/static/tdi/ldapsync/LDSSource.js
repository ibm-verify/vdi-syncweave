define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/aspect",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/form/Button",
	"idx/form/CheckBox",
	"dijit/form/ComboBox",
	"dijit/form/Form",
	"dijit/form/Textarea",
	"dijit/form/ToggleButton",
	"dijit/MenuItem",
	"dijit/TitlePane",
	"tdi/tdiconstants",
	"tdi/config/assemblyline",
	"tdi/aleditor/ALInitParams",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/forms/_FormWidgetMixinFunctions",
	"tdi/ConnectorEditor",
	"tdi/FormWidget",
	"tdi/ParserEditor",
	"tdi/tdisession",
	"tdi/tdiutil",
	"tdi/tdiapi",
	"tdi/NlsMixin",
	"tdi/config/connector",
	"tdi/model/ComponentsModel",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"./LDSGrid",
	"./LDSUtil",
	"dojo/text!./templates/LDSSource.html"
], function(declare, array, lang, html, aspect, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, TabContainer, 
		Button, CheckBox, ComboBox, Form, Textarea, ToggleButton, MenuItem, TitlePane, tdiconstants, tdiassemblyline, ALInitParams, tdiconfigentry, tdicientry,
		FormWidgetMixinFunctions, ConnectorEditor, FormWidget, ParserEditor, tdisession, tdiutil, tdiapi, tdiNlsMixin, tdiconnector, tdiComponentsModel, idx,
		HeaderPane, LDSGrid, LDSUtil, template) {

return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiNlsMixin ],
	{
		templateString : template,
		_idProperty: "%%__  ignore this __%%",
		
		constructor: function(args) {
			dojo.safeMixin(this, args);
		},
		
		browseConnection: function() {
			this.onBrowseConnection(this.config, true);
		},
		
		onBrowseConnection: function(config, source) {
			
		},
		
		getConnectorConfig: function() {			
			if(this.config.isAssemblyLine && this.config.isAssemblyLine()) {
				var names = this.config.getComponentNames(true);
				if(names.length == 1) {
					return this.config.getComponentByName(names[0]);
				}
			}
			return this.config;
		},
		
		resize: function(obj) {
			if(this.border) {
				this.border.layout();
				this.border.resize(obj);
			}
		},
		
		changeConnType: function() {
			this.editor._resetForm();
		},
		
		loadParserForm: function() {
			var t = this;
			if(t._parser) {
				t._parser.destroyRecursive();
				t._parser = null;
			}
			
			// -- assign a default parser if none chosen (emtpy string or [parent])
			if(/\[parent\]|^$/.test(t.getConnectorConfig().getParserConfig().getParserType())) {
				t.getConnectorConfig().getParserConfig().setParserType("system:/Parsers/ibmdi.CSV");
			}
			
			t._parserEditor = new ParserEditor({style:"margin-top:20px", config:t.getConnectorConfig(), hideNullValues:false});
			t._parser = new TitlePane({
				title:this.getString("WebCE.parser"),
				expanded:true,
				content:t._parserEditor,
				style:"width:100%"
			});
			t._parser.placeAt(t.Form, "last");
		},

		createParserModel: function() {
			var t = this;
			t.parserStore = new tdiComponentsModel({
				componentType:"parser",
				onLoadComplete: function() {
					array.forEach(t.parserStore._arrayOfAllItems, function(item) {
						t.parserMenu.addChild(new MenuItem({
							label:item.name[0],
							onClick:lang.hitch(t, "onChangeParser", item.id[0])
						}));
					});
					//t.parserSelect.set("store", t.parserStore);
					t.updateParserLabel();
				} 
			});
		},
		
		getParserType : function() {
			var t = this;
			var con = t.getConnectorConfig().getParserConfig().getParserType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
			return con;
		},
		
		updateParserLabel: function() {
			var t = this
			var type = t.getParserType();
			try {
				var pt = t.parserStore._getItemByIdentity(type);
				if(pt)
					type = pt.name[0];
			} catch(err) {
			}
			t.parserButton.set("label", type);
		},
		
		onChangeParser: function(item) {
			var t = this;
			if(item && item.length > 0) {
				try {
					t.getConnectorConfig().getParserConfig().setParserType("system:/Parsers/" + item);
					t.loadParserForm();
					t.updateParserLabel();
				} catch(err) {
					alert("setInherit: " + item + ": " + err);
				}
			}
		},
		
		createEditor: function() {
			var t = this;
			var conn = t.getConnectorConfig();
			var type = tdiapi.getConnectorType(conn);
			if(type) {
				tdiapi.getConnectorForm(type).then(function(formdata) {
					t.formWidget = new FormWidget({
						formData:formdata,
						verticalLayout:true,
						config:conn.getConnectionConfig(),
						hideNullValues:false
					}).placeAt(t.Form);
					
					if(formdata.useParser == "required" || formdata.useParser == "optional") {
						t.loadParserForm();
						t.createParserModel();
					} else {
						html.style(t.parserButton.domNode, "display", "none");
					}
				});
			}
			
			html.style(t.browseButton.domNode, "display", "none");
			if(array.some(["LDAPSync:Form_AD","LDAPSync:Form_LDAP","LDAPSync:Form_TDS","LDAPSync:Form_SUN"], function(key) {
				return key === type;
			})) {
				html.style(t.browseButton.domNode, "display", "");
			}
		},
		
		testConnection: function() {
			this.startedByMe = true;
			this.startedByForm = null;
			idx.showProgressDialog(this.getString("FDS.verifyConnection"), 60*1000);
			LDSUtil.testConnection(this.config, 0, 9);
		},
		
		_updateTestConnectionEvent: function(event) {
			var t = this;
			if(event.type == "user.fds.testconnection" && event.id == t.testId && t.startedByMe) {
				try {
					idx.hideProgressDialog();
					var json = dojo.fromJson(event.data.value);
					if(json.status == "failed") {
						idx.error(json.message);
					} else {
						LDSUtil.updateTestConnectionSchema(t.config, json);
						if(json.entries)
							t.updateAttrTable(json.entries);
						idx.info(t.getString("FDS.connectionOK"));
					}
					t.startedByMe = false;
				} catch(err) {
					console.log(err);
				}
			}
		},
		
		updateAttrTable: function(arr) {
			this.mapGrid = new LDSGrid({
				data:arr
			});
			this._grid.set("content", this.mapGrid);
		},
		
		showSchema: function() {
			var obj = {};
			array.forEach(this.config.getSchema().getNames(), function(name) {
				obj[name] = "";
			});
			this.updateAttrTable([obj]);
		},
		
		startup: function() {
			this.inherited(arguments);
			this.createEditor();
			this.showSchema();
			this.testId = this.config.getConfigName() + "." + this.config.getName();
			this._eventsHandler = tdiapi.subscribeServerEvents(lang.hitch(this, "_updateTestConnectionEvent"));
		}
	})
});
