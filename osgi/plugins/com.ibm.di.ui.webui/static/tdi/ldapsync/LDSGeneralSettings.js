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
	"dojo/text!./templates/LDSGeneralSettings.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Toolbar, Button, 
		Form, FormWidget, tdiapi, tdiconstants, tdisession, tdiutil, tdiconfigentry, tdicientry, tdiconnector, idx, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		getTitle: function() {
			return this.getString("FDS.connectionSettings");
		},
		
		browseConnection: function() {
			this.onBrowseConnection(LDSUtil.getGeneralSettingsConnector(this.config), false);
		},
		
		onBrowseConnection: function(config, source) {
			
		},
		
		testConnection: function() {
			this.didStartTest = true;
			this.testConnectionPending = false;
			idx.showProgressDialog(this.getString("FDS.verifyConnection"));
			LDSUtil.testDirectoryServerConnection(this.config);
		},
		
		handleOnClose: function() {
			// summary:
			//		Called by LDSFlows to execute a connection test if user forgot
			if(this.testConnectionPending) {
				LDSUtil.testDirectoryServerConnection(this.config);
			}
		},
		
		startup: function() {
			var t = this;
			var cfg = LDSUtil.getGeneralSettingsConnector(t.config); 

			t.conn = cfg;
			t.params = cfg.getConnectionConfig();
			
			if(LDSUtil.getCustomTarget()) {
				t._browseButton.set("style", {display:"none"});
			}
			
			var conn = tdiapi.getNamespace(LDSUtil.getTargetProjectName()).getConnector(LDSUtil.generalSettingsConn);
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
						t.gpform.onModify = function(param, value) {
							t.testConnectionPending = true;
						};
					});
				}
			}

			this.own(tdiapi.subscribeServerEvents(function(event) {
				if (event.id == t.config.getConfigName() + ".GeneralSettings" && event.type == "user.fds.testconnection") {
					var json = dojo.fromJson(event.data.value);
					if(t.didStartTest) {
						t.didStartTest = false;
						if(json.status == "success") {
							idx.hideProgressDialog();
							LDSUtil.updateTestConnectionSchema(t.conn, json);
							idx.info(t.getString("FDS.connectionOK"));
						} else {
							idx.hideProgressDialog();
							tdiutil.error(json.message);
						}
					}
				}
			}));
			
			t.inherited(arguments);
		}
	})
});