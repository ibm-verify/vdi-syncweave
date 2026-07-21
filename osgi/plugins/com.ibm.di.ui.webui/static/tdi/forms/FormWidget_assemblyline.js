/*
/*
 */
define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/store/Memory",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "dijit/form/ComboButton",
   "dijit/form/CheckBox",
   "dijit/form/TextBox",
   "tdi/tdiapi",
   "tdi/tdiutil",
   "tdi/tdiconfig",
   "tdi/SimpleForm",
   "tdi/NlsMixin",
   "dojo/text!./templates/FormWidget_assemblyline.html"
], function(declare, array, lang, Memory, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, TextBox,
		tdiapi, tdiutil, tdiconfig, SimpleForm, nls, template) {

	return declare([ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
		{
		templateString : template,
		
		// label: String
		label: "",
		
		_onChange: function() {
			this.set("value", this._text.get("value"));
			this.onChange(this._text.get("value"));
		},
		
		onChange: function(value) {
		},
		
		updateCombo: function() {
			var t = this;
			var cfg = tdiapi.getNamespace(this.selectConfig);
			if(cfg) {
				var data = [];
				array.forEach(cfg.getAssemblyLineNames(), function(name) {
					data.push({id:name, value:name, name:name});
				});
				var store = new Memory({data:data});
				t._text.set("store", store);
			}
		},
		
		setConfigName: function(configName) {
			this.selectConfig = configName;
			if(configName) {
				this._selectButton.set("style", {display:"none"});
				this.updateCombo();
			} else {
				this._selectButton.set("style", {display:""});
			}
		},

		_setValueAttr: function(value) {
			this.inherited(arguments);
			var match = value ? value.match("(.*):/AssemblyLines/(.*)") : null;
			if(match && match.length == 3) {
				this.targetConfig = match[1];
				this.targetAssemblyLine = match[2];
			} else {
				this.targetConfig = null;
				this.targetAssemblyLine = value;
			}
			if(!this.formWidget.onEditAssemblyline) {
				this._editButton.set("disabled", true);
			}
			if(this.selectConfig)
				this._text.set("value", this.targetAssemblyLine);
			else
				this._text.set("value", value);
			this._updateALInitParams();
		},
		
		_getValueAttr: function() {
			return this._text.get("value");
		},
		
		_selectAssemblyLine: function() {
			var t = this;
			tdiutil.selectAssemblyLine(this.label, this.get("value"), function(value) {
				t.set("value", value);
			}, this.targetConfig);
		},
		
		_createAssemblyLine: function() {
			alert("Create new assemblyline")
		},
		
		_editAssemblyLine: function() {
			this.formWidget.onEditAssemblyline(this.selectConfig ? this.selectConfig : this.targetConfig, this.targetAssemblyLine);
		},
		
		_getALInitParams : function() {
			// summary:
			// Returns the initParams schema def for the called AL if
			// there
			// are items in the array.
			var al = this.get("value");
			if (al && al.length > 0) {
				var target = this.config.getTop().lookup(al);
				if (target) {
					var params = target.getInitParams();
					if (params && params.getNames().length > 0)
						return params;
				}
			}
			return null;
		},
		
		_updateALInitParams : function() {
			if (this.initParams) {
				this.initParams.destroyRecursive();
				this.initParams = null;
			}

			var params = this._getALInitParams();
			
			this._initParams.style.display = params ? "" : "none";
			this._initParamsLabel.style.display = params ? "" : "none";

			if (!params)
				return;

			this.initParams = new SimpleForm({
				alInitParams : params,
				config : this.config,
				configPrefix : "$initialize.",
				copyDefaultParams:true
			}).placeAt(this._initParams);
			
			this.initParams.startup();
		},
		
		postCreate : function() {
			if(!this.formWidget.onEditAssemblyline) {
				this._editButton.se
			}
		}
	})
});