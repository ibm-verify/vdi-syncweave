/**
 * LDSGeneralSettings
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Toolbar",
	"dijit/form/Button",
	"dijit/form/Form",
	"tdi/FormWidget",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/tdisession",
	"tdi/tdiutil",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/config/connector",
	"idx/dialogs",
	"./LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSLogSettings.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Toolbar, Button, 
		Form, FormWidget, tdiapi, tdiconstants, tdisession, tdiutil, tdiconfigentry, tdicientry, tdiconnector, idx, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		getTitle: function() {
			return this.getString("assemblyline.tabs.logging.tooltip");
		},
		
		readGlobalProps: function() {
			var t = this;
			try {
				var ldapsync = tdiapi.getNamespace(LDSUtil.projectName);
				var conn = ldapsync.getConnector(LDSUtil.logSettingsConn).getConnectionConfig();
				array.forEach(conn.getNames(), function(p) {
					if(t.gpform.getControl(p)) {
						var old = t.params.getParam(p);
						if(!old) {
							var vals = {};
							vals[p] = conn.getParam(p);
							t.gpform.setValue(vals);
						}
					}
				});
			} catch(err) {
				console.log(err);
			}
		},
		
		startup: function() {
			var t = this;
			
			var cfg = t.config.getConnector(LDSUtil.generalSettingsConn);
			if(!cfg) {
				cfg = t.config.createLibraryConnector(LDSUtil.generalSettingsConn, "Iterator");
				cfg.setInheritFrom("system:/Connectors/ibmdi.LDAP");
			}
			
			t.conn = cfg;
			t.params = cfg.getConnectionConfig();
			
			var conn = tdiapi.getNamespace(LDSUtil.projectName).getConnector(LDSUtil.logSettingsConn);
			if(conn) {
				var type = tdiapi.getConnectorType(conn);
				if(type) {
					tdiapi.getConnectorForm(type).then(function(formdata) {
						t.gpform = new FormWidget({
							formData:formdata,
							verticalLayout:true,
							config:t.params,
							hideNullValues:false
						}).placeAt(t.form);
						t.readGlobalProps();
					});
				}
			}
			this.inherited(arguments);
		}
	})
});