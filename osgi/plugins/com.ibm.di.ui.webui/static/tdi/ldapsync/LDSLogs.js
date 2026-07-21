/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/ComboBox",
	"dijit/form/Form",
	"dijit/form/Textarea",
	"dijit/MenuItem",
	"tdi/TableWidget",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"dojo/text!./templates/LDSLogs.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, TabContainer, 
		Button, CheckBox, ComboBox, Form, Textarea, MenuItem, TableWidget, tdiapi, tdiconstants, tdiconfigentry, tdicientry, idx, HeaderPane, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : template,
		
		getLdapSyncLogFiles: function() {
			return dojo.xhrGet( {
				handleAs : "json",
				headers: {
					"Accept" : "application/json"
				},
				url : tdiapi._url_prefix + "/ldapsync/log"
			});
		},
		
		openLogFile: function() {
			var file = this.table.getItemValue(null, "id");
			var t = this;
			if(file) {
				this.getLdapSyncLogFile(file).then(function(data) {
					t.onShowLogFile(file, data);
				});
			}
		},
		
		onShowLogFile: function(file, content) {
			// summary:
			//		Called to show contents of log file
		},
		
		getLdapSyncLogFile: function(file) {
			// summary:
			// 		Requests the logs for a specific config/al
			// return:
			// 		The dojo.Deferred object from dojo.xhrGet
			//
			return dojo.xhrGet( {
				handleAs : "text",
				headers: {
					"Accept" : "text/plain"
				},
				url : tdiapi._url_prefix + "/ldapsync/log/" + file
			});
		},
		
		resize: function(obj) {
			if(obj) {
				if(this.table)
					this.table.reloadGrid();
			}
		},
		
		onRefresh: function() {
			var table = this.table;
			this.getLdapSyncLogFiles().then(function(data) {
				var map = array.map(data, function(file) {
					return {name:file, id:file}
				});
				table.setData(map);
			});
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			this.table = new TableWidget({
				structure:[{id:"name", field:"name", name:"Logfiles", width:"auto"}],
				style:"width:100%; height:100%; margin:0; padding:0",
				onRowClick:lang.hitch(this, "openLogFile")
			}).placeAt(this.tableDiv);
			this.table.startup();
			
			this.onRefresh();
			this.connect(this.Header, "onRefresh", "onRefresh");
		}

	})
});
