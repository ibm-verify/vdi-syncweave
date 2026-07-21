/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/widget/Dialog",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/TextBox",
	"idx/form/Link",
	"idx/dialogs",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"tdi/FormWidget",
	"tdi/ldapsync/LDSMap3",
	"tdi/ldapsync/LDSPostOpItem",
	"tdi/TableWidget",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSPostOp.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, Button, CheckBox, TextBox, Link, idx, tdiapi, tdiutil, FormWidget, LDSMap, LDSPostOpItem, TableWidget, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		
		// value: Object
		//		the properties of this form
		value: null,
		
		// props: Object mapping fields to property names in config
		propnames: {
		    "init":"hook.prolog",
		    "user":"hook.postoperation.user",
		    "group":"hook.postoperation.group",
		    "error":"hook.error",
		    "finish":"hook.epilog",
		    "before":"hook.beforewrite",
		    "single":"hook.postoperation.singleAL",
		    "monitor":"monitor"
		},
		
		// keys: Array
		keys: [
		    "init",
		    "before",
		    "user",
		    "group",
		    "error",
		    "finish"
		],
		
		_setValueAttr: function(value) {
			this.value = value;
//			this.Form.set("value", value);
		},
		
		_getValueAttr: function(value) {
//			this.value = this.Form.get("value");
			return this.value;
		},
		
		enableAL: function(al, enable) {
			var key = this.propnames[al] + ".enabled";
			this.config.setParam(key, enable);
		},
		
		selectAL: function(al, label) {
			var t = this;
			var key = t.propnames[al] + ".al";
			var value = t.config.getParam(key);
			tdiutil.selectAssemblyLine(value, label, function(newvalue) {
				t.config.setParam(key, newvalue);
				var obj = {};
				obj[al] = newvalue;
				t.Form.set("value", obj);
			});
		},
		
		configureAL: function(al, title) {
			var alcomp = this.config.getAssemblyLine();
			var fcname = "PostOp_" + al;
			var alfc = alcomp.getComponentByName(fcname);
			if(!alfc) {
				alfc = alcomp.createDataFlowFunction(fcname);
				alfc.setConnectorType("ibmdi.AssemblyLineFC");
				alfc.setState("Disabled");
			}
			tdiapi.getFunctionForm("ibmdi.AssemblyLineFC").then(function(data) {
				var form = new FormWidget({
					formData:data,
					config:alfc
				});
				var dlg = new Dialog({
					title:fcname,
					content:form
				})
				dlg.show();
			});
		},
		
		getPostOpAL: function(key) {
			var alcomp = this.config.getAssemblyLine();
			var fcname = "PostOp_" + key;
			var alfc = alcomp.getComponentByName(fcname);
			if(!alfc) {
				alfc = alcomp.createDataFlowFunction(fcname);
				alfc.setConnectorType("ibmdi.AssemblyLineFC");
				alfc.setState("Disabled");
			}
			return alfc;
		},
		
		configureMap: function(al, title) {
			idx.info("Not yet implemented");
		},
		
		configureMonitor: function(al, value) {
			idx.info("Not yet implemented");
		},
		
		createRows: function() {
			var t = this;
			
			var labels = [];
			labels.push(t.getString("FDS.postOpFlowInit"));
			labels.push(t.getString("FDS.postOpFlowBefore"));
			labels.push(t.getString("FDS.postOpFlowUser"));
			labels.push(t.getString("FDS.postOpFlowGroup"));
			labels.push(t.getString("FDS.postOpFlowError"));
			labels.push(t.getString("FDS.postOpFlowFinished"));
			
			this.postop = {};
			for(var i = 0; i < this.keys.length; i++) {
				this.createRow(this.keys[i], labels[i], this._table, false, i == 0, i == 3 /* Group should have extra checkbox */);
			};
		},
		
		getParam: function(config, key) {
			return config.getConnectionConfig().getParam("hook." + key);
		},
		
		getParamBoolean: function(config, key, def) {
			return config.getConnectionConfig().getParamBoolean("hook." + key, def);
		},
		
		createRow: function(key, label, div, hideMonitor, open, isgroup) {
			var alfc = this.getPostOpAL(key);
			this.postop[key] = new LDSPostOpItem({
				key:this.propnames[key],
				title:label,
				config:alfc,
				open:open,
				hideMonitor:hideMonitor,
				style:"padding-top:10px",
				isgroup:isgroup,
				onUpdateSingleAL:lang.hitch(this, "updateSingleAL"),
				singleAL:this.config.getParamBoolean("hook.postoperation.singleAL", true)
			}).placeAt(div);
		},

		getEnabledFor: function(key, def) {
			var key = this.propnames[key] + ".enabled";
			return this.config.getParamBoolean(key, def);
		},
		
		getAssemblyLineFor: function(key) {
			var key = this.propnames[key] + ".al";
			return this.config.getParam(key);
		},
		
		updateSingleAL: function(value) {
			var isset = this.config.getParamBoolean("hook.postoperation.singleAL", true);
			if(isset != value)
				this.config.setParam("hook.postoperation.singleAL", value);
		},
		
		resize: function(obj) {
			this.inherited(arguments);
			if(!obj) {
				obj = html.marginBox(this.domNode);
			}
			if(this.table) {
				this.table.resize(obj);
			}
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			// make sure the default value is written for new flows
			var value = this.config.getParam("hook.postoperation.singleAL");
			if(value == null) {
				this.config.setParam("hook.postoperation.singleAL", "true");
			}
			this.createRows();
		}

	})
});
