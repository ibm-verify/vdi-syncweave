define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Form",
	"dijit/form/Button",
	"dijit/form/Textarea",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/TableWidget",
	"tdi/aleditor/Border",
	"idx/form/Select",
	"idx/layout/HeaderPane",
	"dojo/data/ItemFileReadStore",
	"dojo/text!./templates/ALInitParam.html"
], function(declare, lang, array, Widget, TemplatedMixin, _WidgetsInTemplateMixin, Form, Button, TextArea, tdiapi, tdiconfig, tdiutil, TableWidget,
		Border, Select, HeaderPane, ItemFileReadStore, template) {

return declare(
	[Widget, TemplatedMixin, _WidgetsInTemplateMixin],	
	{
		templateString: template,
		
		// config: tdi.assemblyline
		config: null,
		
		setConfig: function(config, id) {
			var t = this;
			t.config = config;
			if(t.config) {
				t.item = t.config.getInitParams().getItem(id);
				t.updateConnectorList();
				t.updateForm(t.item);
			}
		},
		
		updateForm: function(item) {
			var data = {
					name:item.getName(),
					syntax:item.getObject("type")
			}
			this.Form.set("value", data);
		},
		
		updateConnectorList: function() {
			var arr = new Array();
			array.forEach(this.config.getConnectorNames(), function(c) {
				arr.push({id:c.name, name:c.name});
			});
			arr = arr.sort();
			var store = new ItemFileReadStore({
				data:{
					identifier:"id",
					label:"name",
					items: arr
				}
			});
			this.connectorList.setStore(store, "");
		},
		
		updateParameterList: function() {
			var str = this.connectorList.get("value");
			if(str) {
				var conn = this.config.getConnector(str);
				var type = tdiutil.getComponentType(conn);
				var plist = this.parameterList;
				return tdiapi.getConnectorForm(type).then(function(data) {
					if(data && data.parameterMapDescriptor) {
						var arr = new Array();
						array.forEach(data.parameterMapDescriptor.parameterDescriptor, function(param) {
							if(!param.hidden) {
								arr.push({id:param.key, name:tdiutil.getFormLabel(param) + " (" + (param.type ? param.type : "string") + ")"});
							}
						});
						arr = arr.sort(function(a,b) {
							return a.name - b.name;
						});
						var store = new ItemFileReadStore({
							data:{
								identifier:"id",
								label:"name",
								items: arr
							}
						});
						plist.setStore(store, "");
					}
					return true;
				});
			} else {
				return null;
			}
		},
		
		updateScript: function() {
			
		},
		
		updateParameterAssignment: function() {
			var hook = this.config.getHook("prolog0", true);
			var script = hook.getScript();
			var begin = "// -- begin " + this.item.getName();
			var end = "// -- end " + this.item.getName();
			var s = script.indexOf(begin);
			var e = script.indexOf(end);
		},
		
		resize: function(obj) {
			if(this.Header) {
				this.Header.resize(obj);
			}
		},
		
		postCreate: function() {
		}
	}
)});