/**
 * The ActivityMonitor maintains a tree view of active configurations and
 * assemblylines.
 */
define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/dom-style",
   "dojo/dom-class",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "dijit/form/CheckBox",
   "dijit/form/TextBox",
   "tdi/tdiutil",
   "tdi/SimpleForm",
   "tdi/NlsMixin",
   "dojo/text!./templates/LDSPostOpItem.html"
], function(declare, array, lang, domstyle, domClass, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, CheckBox, TextBox,
		tdiutil, SimpleForm, nls, template) {

	return declare([ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
		{
		templateString : template,

		selectAL : function(event) {
			var t = this;
			var value = t.getParam(t.key);
			tdiutil.selectAssemblyLine(t.title, value, function(
					newvalue) {
				t.setParam("al", newvalue);
				t._form.set("value", {
					al : newvalue
				});
				t.updateALInitParams();
			});
		},

		getParam : function(key) {
			// Get the actual connector (might be this.config or this.config.config)
			var connector = this.config;
			if (connector && !connector.getConnectionConfig && connector.config) {
				connector = connector.config;
			}
			
			// Safety check for getConnectionConfig
			if (!connector || typeof connector.getConnectionConfig !== "function") {
				console.warn("LDSPostOpItem: No valid connector for getParam", key);
				return "";
			}
			return connector.getConnectionConfig().getParam("hook." + key);
		},
	
		getParamBoolean : function(key, def) {
			// Get the actual connector (might be this.config or this.config.config)
			var connector = this.config;
			if (connector && !connector.getConnectionConfig && connector.config) {
				connector = connector.config;
			}
			
			// Safety check for getConnectionConfig
			if (!connector || typeof connector.getConnectionConfig !== "function") {
				console.warn("LDSPostOpItem: No valid connector for getParamBoolean", key);
				return def || false;
			}
			return connector.getConnectionConfig().getParamBoolean("hook." + key, def);
		},
	
		setParam : function(key, value) {
			// Get the actual connector (might be this.config or this.config.config)
			var connector = this.config;
			if (connector && !connector.getConnectionConfig && connector.config) {
				connector = connector.config;
			}
			
			// Safety check for getConnectionConfig
			if (!connector || typeof connector.getConnectionConfig !== "function") {
				console.warn("LDSPostOpItem: No valid connector for setParam", key);
				return;
			}
			connector.getConnectionConfig().setParam("hook." + key, value);
		},

		onUpdateSingleAL: function(value) {
			// summary:
			//		Callback for single AL checkbox in group display
		},
		
		updateSingleAL: function(value) {
			if(this.isgroup) {
				this._selectAlRow.style.display = value ? "none" : "";
				this.singleAL = value;
				this.updateALInitParams();
				this.onUpdateSingleAL(value);
			}
		},
		
		updateAL : function(value) {
			var al = this.getParam("al");
			if (al != value) {
				this.setParam("al", value);
				
				// Get the actual connector
				var connector = this.config;
				if (connector && !connector.getConnectionConfig && connector.config) {
					connector = connector.config;
				}
				
				// Safety check before calling getConnectionConfig
				if (connector && typeof connector.getConnectionConfig === "function") {
					connector.getConnectionConfig().setParam("assemblyLine", value);
				}
				this.updateALInitParams();
			}
			
			// Get the actual connector for getParamBoolean
			var connector = this.config;
			if (connector && !connector.getParamBoolean && connector.config) {
				connector = connector.config;
			}
			
			if (connector && typeof connector.getParamBoolean === "function") {
				var isset = connector.getParamBoolean("hook.postoperation.singleAL", true);
				if(isset != value && typeof connector.setParam === "function")
					connector.setParam("hook.postoperation.singleAL", value);
			}
		},

		updateEnabled : function(value) {
			var current = this.getParamBoolean("enabled");
			if (current != value) {
				this.setParam("enabled", value);
			}
		},

		updateMonitor : function(value) {
			this.setParam("monitor", value);
		},

		_getALInitParams : function() {
			// summary:
			// Returns the initParams schema def for the called AL if
			// there
			// are items in the array.
			var al = this.getParam("al");
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

		updateALInitParams : function() {
			if (this.initParams) {
				this.initParams.destroyRecursive();
				this.initParams = null;
			}

			var params = this._getALInitParams();
			// domstyle.set(this._initParams, "display", params ? "" :
			// "none");
			if(this.isgroup && this.singleAL) {
				params = null;
			}
			
			this._initParams.style.display = params ? "" : "none";
			this._initParamsLabel.style.display = params ? "" : "none";

			if (!params)
				return;

			// Get the actual connector
			var connector = this.config;
			if (connector && !connector.getConnectionConfig && connector.config) {
				connector = connector.config;
			}
			
			// Safety check before calling getConnectionConfig
			var connConfig = null;
			if (connector && typeof connector.getConnectionConfig === "function") {
				connConfig = connector.getConnectionConfig();
			}
			
			this.initParams = new SimpleForm({
				alInitParams : params,
				config : connConfig,
				configPrefix : "$initialize.",
				copyDefaultParams:true
			}).placeAt(this._initParams);
			this.initParams.startup();
		},

		resize : function(obj) {
			if (obj && obj.h) {
				this._form.resize(obj);
			}
		},

		_prepareAttributeMap : function() {
			var map = this.config.getAttributeMap(false)
			var names = map.getNames();
			if (names.length == 0) {
				map.newItem({
					name : "*"
				});
				map.newItem({
					name : "$writeStatus"
				});
				map.newItem({
					name : "$operation"
				});
			}
			return map;
		},
		
		_toggleByKey: function(event) {
			if(event && event.keyCode == dojo.keys.ENTER || event.keyCode == dojo.keys.SPACE) {
				this._toggleToggle();
			}
		},

		_toggleToggle: function() {
			if(domClass.contains(this.arrowNode, "tdiRightArrow")) {
				domClass.remove(this.arrowNode, "tdiRightArrow");
				domClass.add(this.arrowNode, "tdiDownArrow");
				this._alParams.style.display = "block";
			} else {
				domClass.remove(this.arrowNode, "tdiDownArrow");
				domClass.add(this.arrowNode, "tdiRightArrow");
				this._alParams.style.display = "none";
			}
		},

		postCreate : function() {
			this.inherited(arguments);
			this._form.set("value", {
				al : this.getParam("al"),
				enabled : this.getParamBoolean("enabled", false) ? "on"
						: ""
			});
			if (this.hideMonitor && this._monitor)
				domstyle.set(this._monitor, "display", "none");

			this.updateALInitParams();
			this._isGroupRow.style.display = this.isgroup ? "" : "none";
			if(this.isgroup) {
				this._form.set("value", {
					"singleAL":this.singleAL ? "on" : ""
				});
			}
		}
	})
})
