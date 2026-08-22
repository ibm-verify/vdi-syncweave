/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/app/AppFrame",
	"idx/app/AppMarquee",
	"idx/form/DropDownLink",
	"idx/form/CheckBox",
	"idx/form/Link",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/form/Button",
	"dijit/form/ValidationTextBox",
	"dijit/form/Select",
	"dijit/TooltipDialog",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"idx/dialogs",
	"tdi/ldapsync/LDSFlows",
	"tdi/ldapsync/LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSMain.html"
], function(declare, array, lang, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, AppFrame, AppMarquee, DropDownLink, 
		CheckBox, Link, BorderContainer, ContentPane, Button, ValidationTextBox, Select,  
		TooltipDialog, tdiapi, tdiconstants, tdiconfigentry, tdicientry, idx, LDSFlows, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		
		onLogout: function() {
			window.location = "logout.html";
		},
		
		onShowHelp: function() {
			window.open("https://www.ibm.com/docs/en/vdi/11.0.0?topic=server-federated-directory", "_FDS_HELP");
		},
		
		toggleAutoUpdate: function(auto) {
			this.saveCookies();
		},
		
		toggleAutoSave: function(auto) {
			this.saveCookies();
		},
		
		saveSnapShot: function() {
			if(this._snapshotTitle.isValid())
				this.flows.saveSnapShot(this._snapshotTitle.get("value"));
		},
		
		loadSnapShot: function() {
			this.flows.loadSnapShot(this._snapshots.get("value"));
		},
		
		deleteSnapShot: function() {
			this.flows.deleteSnapShot(this._snapshots.get("value"));
		},
		
		onShowWelcome: function() {
			this.flows.showWelcomePage();
		},
		
		loadCookies: function() {
			LDSUtil.loadCookies();
			this._autosave.set("value", LDSUtil.getOption("autoSave", true));
			this._autoupdate.set("value", LDSUtil.getOption("autoUpdate", true));
		},
		
		saveCookies: function() {
			LDSUtil.setOptions({
				autoSave:this._autosave.get("value"),
				autoUpdate:this._autoupdate.get("value")
			});
			this.btnSave.set("style", {display:LDSUtil.getOption("autoSave", true)?"none":""});
			this.btnUpdate.set("style", {display:LDSUtil.getOption("autoUpdate", true)?"none":""});
		},
		
		saveConfig: function() {
			this.flows.saveConfig();
		},
		
		updateFDS: function() {
			this.flows.updateFDS();
		},
		
		onBeforeUnload: function() {
			return this.flows.onBeforeUnload();
		},
		
		resize: function(obj) {
			this.inherited(arguments);
			if(this.flows) {
				this.flows.resize(obj);
			}
		},
		
		postCreate: function() {
			// request status page to install/create files if needed
			dojo.xhrGet({
				handleAs : "json",
				headers: {
					"Accept" : "application/json"
				},
				url : tdiapi._url_prefix + "/ldapsync"
			});
			
			var t = this;
			t.loadCookies();
			
			t.btnSave.set("disabled", true);
			t.btnUpdate.set("disabled", true);
			
			t.own(t.flows.on("ConfigModified", function() {
				t.btnSave.set("disabled", false);
			}));
			
			t.own(t.flows.on("ConfigSaved", function() {
				t.btnSave.set("disabled", true);
				t.btnUpdate.set("disabled", LDSUtil.getOption("autoUpdate",true));
			}));
			
			t.own(t.flows.on("FDSUpdated", function() {
				t.btnUpdate.set("disabled", true);
			}));
			t.flows.setSnapshots(this._snapshots);
		}
		
	})
});
	
		
