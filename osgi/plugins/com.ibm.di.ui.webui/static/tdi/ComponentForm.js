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
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"tdi/NlsMixin",
	"tdi/FormWidget",
	"tdi/ParserEditor",
	"tdi/model/ComponentsModel",
	"tdi/tdiapi"
], function(declare, array, lang, _Widget, _TemplatedMixin, tdiNlsMixin, tdiFormWidget, tdiParserEditor, tdiComponentsModel, tdiapi) {
	return declare(
		[_Widget, _TemplatedMixin, tdiNlsMixin],
		{
			templateString: "<div style='margin:0; padding:0; height:100%; width:100%'></div>", // template,
			
			// ignoreSections: Boolean
			//		If true, all params are displayed
			ignoreSections: true,
			
			// verticalLayout: Boolean
			//		If true labels appear on a line above the control
			verticalLayout: true,
			
			_loadConnectorForm: function() {
				if(this.Configuration == null) {
					this.Configuration = dojo.create("div", {style:"height:100%; width:100%; overflow:scroll"}, this.domNode);
				}
				var type = this._getConnType();
				if(this.config != null && type && type != "[parent]") {
					dojo.when(tdiapi.getConnectorForm(type, "en"), dojo.hitch(this, "_loadForm"));
				} else {
					this._resetForm();
				}
			},
			
			_loadForm: function(data) {
				
//				array.forEach(data.supportedModes.mode, function(mode) {
//					if(mode.value != this.config.getMode())
//						this.modeSelect.addOption({value:mode.value, label:tdiutil.getFormLabel(mode)});
//				}, this);
				
				this._connectordiv = dojo.create("div"); // , {innerHTML:"Type: " + this._getTitle(data), style:"border-bottom:1px solid #cdcdcd"}, this.Configuration);
		
				this._connectorform = new tdiFormWidget({
					formData:data,
					verticalLayout:this.verticalLayout,
					ignoreSections:this.ignoreSections,
					config:this.config.getConnectionConfig(),
					includeExpressionEditor:true
				});
				
				this._connectorform.placeAt(this.Configuration, "last");
				this._connectorform.reloadForm = dojo.hitch(this, "_reloadForm");
				this._connectorform.resetForm = dojo.hitch(this, "_resetForm");
//				this._connectorform.testConnection = dojo.hitch(this, "_testConnection");
//				this._connectorform.querySchema = dojo.hitch(this, "querySchema");
				if(data.useParser == "required" || data.useParser == "optional") {
					this._parserdiv = dojo.create("div", {innerHTML:"<p/>"}, this.Configuration);
					this._loadParserForm();
				} else if(this._parser != null) {
					this._parser.destroy();
					this._parser = null;
				}
			},
			
			_loadParserForm: function() {
				this._parser = new tdiParserEditor({config:this.config, hideNullValues:this.hideNullValues});
				this._parser.placeAt(this.Configuration, "last");
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
			
			_resetForm: function() {
				
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
		
			_getConnType: function() {
				var con = null;
				if(this.config) {
					if(this.config.getConnectionConfig().getParam("$form$")) {
						con = this.config.getTop().getConfigName() + ":" + this.config.getName();
					} else {
						con = this.config.getConnectorType();
						if(con != null && con.indexOf("/") != -1) {
							con = con.substring(con.lastIndexOf("/") + 1);
						}
					}
				}
				return con;
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
			

			
			resize: function(obj) {
				
			},
			
			postCreate: function() {
				this._loadConnectorForm();
			}
						
		})
});