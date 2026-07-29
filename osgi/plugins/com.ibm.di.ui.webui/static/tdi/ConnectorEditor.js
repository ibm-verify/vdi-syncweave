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
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/InlineEditBox",
	"dijit/Toolbar",
	"dijit/form/Button",
	"idx/form/Select",
	"dijit/form/FilteringSelect",
	"dijit/form/TextBox",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/layout/TabContainer",
	"dijit/layout/StackContainer",
	"dijit/form/SimpleTextarea",
	"idx/layout/HeaderPane",
	"idx/form/Link",
	"idx/grid/PropertyFormatter",
	"tdi/AttributeMap",
	"tdi/aleditor/Border",
	"tdi/FormWidget",
	"tdi/HookEditor",
	"tdi/NlsMixin",
	"tdi/ParserEditor",
	"tdi/TableWidget",
	"tdi/LinkCriteriaWidget",
	"tdi/ToolbarLabel",
	"tdi/model/ComponentsModel",
	"tdi/tdiapi",
	"tdi/tdiatom",
	"tdi/tdisession",
	"tdi/tdiutil",
	"tdi/HooksWidget",
	"dojo/text!./templates/ConnectorEditor.html"
], function(declare, array, lang, html, Widget, TemplatedMixin, WidgetsInTemplateMixin, InlineEditBox, Toolbar, Button, Select, FilterSelect, TextBox, BorderContainer, ContentPane, TabContainer, StackContainer, SimpleTextArea,
			HeaderPane, Link, PropertyFormatter, tdiAttributeMap, tdiBorder, tdiFormWidget, tdiHookEditor, tdiNlsMixin, tdiParserEditor, tdiTableWidget, tdiLinkCriteriaWidget, ToolbarLabel,
			tdiComponentsModel, tdiapi, tdiatom, tdisession, tdiutil, HooksWidget, template) {

return declare(
	[ Widget, TemplatedMixin, WidgetsInTemplateMixin, tdiNlsMixin ], {
		// summary:
		//		A widget for basic editing of a connector.
		// description:
		//		The widget provides simple editing of the attribute maps
		//		and its hooks.
	
		// Widget/Templated
		templateString: "<div style='margin:0; padding:0; height:100%; width:100%'></div>", // template,
		
		// config: tdi.connector
		//		The connector configuration
		config: null,
		
		// title: String
		//		The title string before the config button (default is connector name)
		title: null,
		
		// hideNullValues: boolean
		//		Hides parameters with no value set
		hideNullValues: false,
		
		// visibleButtons
		//		The buttons to show in the formt
		visibleButtons: [true, false, false],
		
		// header
		//		Show/hide header with Mode and State combos
		headerVisible: true,
		
		// _input: boolean
		//		Input connector?
		_input: false,
		
		// options: object
		//
		options: {
			ShowConfig:true,
			ShowAttMap: true,
			ShowHooks: true,
			ShowLinks: true
		},
		
		querySchema: function() {
			
		},
		
		_testConnection: function() {
			this.testsession = new tdisession();
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
		
		onReadNext: function(map) {
			this.attmap.enableReadNext(false);
			this.attmap.enableClose(false);
			if(this.session == null) {
				this.session = new tdisession();
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
						this.attmap.enableReadNext(true);
						this.attmap.enableClose(false);
					} else {
						this.attmap.showEODMsg(false);
						this.attmap.setEntry(entry);
						this.attmap.enableReadNext(true);
						this.attmap.enableClose(true);
					}
				}), dojo.hitch(this, function(err) {
					this.attmap.enableReadNext(true);
					this.attmap.enableClose(true);
					this.onCloseConnection(null);
					tdiutil.error(err);
				}));
			}
		},
		
		onCloseConnection: function(map) {
			if(this.session != null) {
				dojo.when(tdiapi.stopConfig(this.session.cientry), dojo.hitch(this, function() {
					this.session = null;
					this.attmap.enableReadNext(true);
					this.attmap.enableClose(false);
				}));
			}
		},
		

		_loadAttMap: function() {
			this._input = (this.config.getMode() == "Iterator" || this.config.getMode() == "Lookup");
			var map = new tdiAttributeMap({config:this.config, input:this._input});
//			var map;
//			try {
//				map = new tdiTableWidget({}); 
//			} catch(err) {
//				alert(err.message);
//			}
			map.onReadNext = dojo.hitch(this, "onReadNext");
			map.onCloseConnection = dojo.hitch(this, "onCloseConnection");
			this.attmap = map;
			this.border.addContainerPane(map, {
				title:this.getString("attributeMap"),
				pageid:"AttributeMap"
			});
		},
		
		_loadHooks: function() {
			this.border.addContainerPane(new HooksWidget({config:this.config}), {
				title:"Hooks",
				pageid:"Hooks"
			});
		},
		
		_loadParserForm: function() {
			this._parser = new tdiParserEditor({config:this.config, hideNullValues:this.hideNullValues});
			this._parser.placeAt(this.Configuration, "last");
		},
			
		_editHook: function(hook, div) {
			
		},
		
		_editAttribute: function(input, div) {
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
		
//		_getTooltipDialog: function() {
//			if(this.editor == null) {
//				this.editor = new tdiAttributeMapItemEditor({});
//				this.tooltipDialog = new dijit.TooltipDialog({
//					content:this.editor
//				});
//			}
//			return this.tooltipDialog;
//		},
//		
		_loadForm: function(data) {
			
			if(data.supportedModes && data.supportedModes.mode) {
				array.forEach(data.supportedModes.mode, function(mode) {
					if(mode.value != this.config.getMode() && this.modeSelect)
						this.modeSelect.addOption({value:mode.value, label:tdiutil.getFormLabel(mode)});
				}, this);
			}
			
			this._connectordiv = dojo.create("h3", {innerHTML:this._getTitle(data), style:"border-bottom:1px solid #cdcdcd"}, this.Configuration);
			
//			this._selectConnector = new InlineEditBox({
//				editor:this._getConnectorSelectionEditor(),
//				autoSave:true
//			}, this._connectordiv);
			
			this._connectorform = new tdiFormWidget({
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
		
		_loadConnectorForm: function() {
			if(this.Configuration == null) {
				this.Configuration = dojo.create("div", {style:"height:100%; width:100%; margin:0; padding:0; overflow:scroll"});
				this.border.addContainerPane(this.Configuration, {
					title:this.getString("ConnectorTreeUI.Localized.Connection"),
					pageid:"Connection"
				});
			}
			var type = this._getConnType();
			if(this.config != null && type && type != "[parent]") {
				if(this.config.isFunction())
					dojo.when(tdiapi.getFunctionForm(type, "en"), dojo.hitch(this, "_loadForm"));
				else
					dojo.when(tdiapi.getConnectorForm(type, "en"), dojo.hitch(this, "_loadForm"));
			} else {
				this._resetForm();
			}
		},
		
		_loadLinkCriteria: function() {
			if(this.config) {
				this.lcw = new tdiLinkCriteriaWidget({config:this.config});
				this.border.addContainerPane(this.lcw, {
					title:this.getString("ConnectorUI.LinkCriteria.label"),
					pageid:"LinkCriteria"
				});
			}
		},
		
		_getTitle: function(data) {
			var nls = "en";
			var str = "";
			dojo.forEach(data.name, function(label) {
				if(label.lang == nls) {
					str = label.value; // p.required ? labs.value + " *" : labs.value;
				}
			});
			return str;
		},
		
		_reloadForm: function() {
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
		
		_destroyForm: function() {
			if(this._connectorform != null) {
				this._connectorform.destroy();
				this._connectorform = null;
				dojo.destroy(this._connectordiv);
				if(this._parser != null) {
					this._parser.destroy();
					this._parser = null;
				}
			}
		},
		
		_resetForm: function() {
			this._destroyForm();
			this._selectForm = dojo.create("div", {style:"width:100%;height:100%;position:relative; top:20px; left:20px; "}, this.Configuration);
			dojo.create("div", {innerHTML:this.getString("WebCE.chooseType")}, this._selectForm);
			this._modelCB = new dijit.form.FilteringSelect({
				store:new tdiComponentsModel({componentType:"connector"})
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
		
		_getConnectorSelectionEditor: function() {
			if(this._modelCB)
				return this._modelCB;
			
			this._modelCB = new Select({
				store:new tdiComponentsModel({componentType:"connector"}),
				value:this._getConnType()
			});
			this._modelCB.on("change", dojo.hitch(this, function(item, label) {
				if(item != null && item.length > 0) {
					try {
						if(item.match(/^adapter:/))
							this.config.setConnectorType(item);
						else
							this.config.setConnectorType("system:/Connectors/" + item);
						this._destroyForm();						
						this._loadConnectorForm();
					} catch(err) {
						alert("setInherit: " + item + ": " + err);
					}
				}
			}));
			return this._modelCB;
		},
	
		_getConnType: function() {
			var con = null;
			if(this.config) {
				if(this.config.getConnectionConfig().getParam("$form$")) {
					con = this._findConnType(this.config);
				} else {
					con = this.config.getConnectorType();
					if(con != null && con.indexOf("/") != -1) {
						con = con.substring(con.lastIndexOf("/") + 1);
					}
				}
			}
			return con;
		},
		
		_findConnType: function(config) {
			if(!config)
				return null;
			con = config.getTop().getConfigName() + ":" + config.getName();
			if(config.getConnectionConfig().getParamByName("$form$", true))
				return con;
			else
				return this._findConnType(config.getInheritedObj());
		},
		
		selectPage: function(page) {
			this.border.selectContainerPane(page);
//			array.forEach(this.TabContainer.getChildren(), function(child) {
//				if(child.title == page || child.pageid == page) {
//					this.TabContainer.selectChild(child);
//				}
//			}, this);
		},
		
		onModeChanged: function(mode) {
			this.config.setMode(mode);
		},
		
		onStateChanged: function(state) {
			this.config.setState(state);
		},
		
		resize: function(obj) {
			if(this.headerPane)
				this.headerPane.resize(obj);
//			if(this.border)
//				this.border.resize(obj);
			
		},
		
		addHeaderChild: function(child) {
			if(!child.region)
				child.region = "majorActions";
			this.headerPane.addChild(child);
		},
		
		addContainerPane: function(content, title, pageid) {
			this.border.addContainerPane(content, {
				title:title,
				pageid:pageid
			});
		},
		
		postCreate: function() {
			if(this.itemDef != null && dojo.isArray(this.itemDef.options)) {
				if(this.itemDef.options.length == 1) {
					this.options = this.itemDef.options[0];
					if(this.options.ShowConfig == null)
						this.options.ShowConfig = true;
					if(this.options.ShowAttMap == null)
						this.options.ShowAttMap = true;
				}
			}
			
			
			this.border = new tdiBorder({style:"width:100%; height:100%; margin:0; padding:0", containerType:"tab", params:{xnested:true}});
			
			var hp = new HeaderPane({
				title:this.title,
				content:this.border,
				style:"width:100%; height:100%; margin:0; padding:0"
			}).placeAt(this.domNode);
			hp.startup();
			this._supportingWidgets.push(hp);
			this.headerPane = hp;
			

			if(this.headerVisible) {
				//
				// -- Mode select
				//
				hp.addChild(new ToolbarLabel({
					label:"Mode: ",
					region:"majorActions"
				}));
				this.modeSelect = new Select({
					onChange:lang.hitch(this, "onModeChanged"),
					region:"majorActions",
					style:"width:10em"
				});
				this.modeSelect.addOption({value:this.config.getMode(), label:this.config.getMode()});
				this.modeSelect.set("value", this.config.getMode());
				hp.addChild(this.modeSelect)
				
				//
				// -- State select
				//
				hp.addChild(new ToolbarLabel({
					label:" State: ",
					region:"majorActions",
				}));
				
				this.stateSelect = new Select({
					onChange:lang.hitch(this, "onStateChanged"),
					region:"majorActions",
					style:"width:10em"
				});
				this.stateSelect.addOption({value:"Enabled", label:"Enabled"});
				this.stateSelect.addOption({value:"Disabled", label:"Disabled"});
				this.stateSelect.addOption({value:"Passive", label:"Passive"});
				this.stateSelect.set("value", this.config.getState());
				hp.addChild(this.stateSelect);
				
				// Name editor
				var nameEditor = new InlineEditBox({
					editor:TextBox,
					autoSave:true
				}, hp._titleNode);
			}
			
			//
			// -- InitOption select
			//
//			var nls = new tdiNlsMixin();
//			hp.addChild(new ToolbarLabel({
//				label:" " + nls.getString("ConnectorUI.toolbar.InitOptions.label") + ": ",
//				region:"majorActions",
//			}));
//			
//			this.initializeSelect = new Select({
//				onChange:lang.hitch(this, "onStateChanged"),
//				region:"majorActions",
//				style:"width:10em"
//			});
//			
//			var arr = ["Default", "OnUse", "Delta", "Always"];
//			array.forEach(arr, function(opt) {
//				var str = nls.getString("ConnectorUI.InitOptions.compInit" + opt + ".label");
//				this.initializeSelect.addOption({value:str, label:str});
//			}, this);
//			
//			this.initializeSelect.addOption({value:"Enabled", label:"Enabled"});
//			hp.addChild(this.initializeSelect)
			
			
			if(this.options.ShowConfig)
				this._loadConnectorForm();
			
			if(this.options.ShowAttMap)
				this._loadAttMap();
			
			if(this.options.ShowHooks)
				this._loadHooks();

			if(this.config.getMode() == "Lookup" || this.config.getMode() == "Update")
				this._loadLinkCriteria();
			
			this.border.selectContainerPane("Connection");
		}

	})
});
