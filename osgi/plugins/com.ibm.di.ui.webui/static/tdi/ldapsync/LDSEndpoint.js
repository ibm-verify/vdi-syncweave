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
	"dijit/ProgressBar",
	"tdi/atom/tdialentry",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/tdiutil",
	"tdi/ldapsync/LDSUtil",
	"idx/dialogs",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSEndpoint.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ProgressBar, tdialentry, tdiapi, tdiconstants, tdiutil, LDSUtil, idx, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		
		_onEditEndpoint: function() {
			this.onEditEndpoint(this.config);
		},
		
		_onDeleteEndpoint: function() {
			this.onDeleteEndpoint(this.config);
		},
		
		_onBrowseEndpoint: function() {
			this.onBrowseEndpoint(this.config);
		},
		
		onDeleteEndpoint: function(endpoint) {
		},
		
		onEditEndpoint: function(endpoint) {
		},
		
		onBrowseEndpoint: function(endpoint) {
		},
		
		testConnection: function() {
			this.Status.src = "/fds/static/images/Synchronize.gif";
			this.Status.title = "";
			LDSUtil.testConnection(this.config);
		},
		
		setStatus: function(error, warn) {
			if(error) {
				this.Status.src = "/fds/static/images/st24_critical.gif";
				this.Status.title = error;
			} else if (warn) {
				this.Status.src = "/fds/static/images/Warning.png";
				this.Status.title = warn;
			} else {
				this.Status.src = "/fds/static/images/validate.gif";
				this.Status.title = this.getString("FDS.connectionOK");
			}
		},
		
		getCompLabel: function() {
			var str = this.config.getName();
			var arr = /^(Source_)(.*)/.exec(str);
			if(arr.length == 3) {
				str = arr[2];
			}
			return str;
		},
		
		_updateTestConnectionEvent: function(event) {
			var t = this;
			if(event.type == "user.fds.testconnection" && event.id == t.testId) {
				try {
					var json = dojo.fromJson(event.data.value);
					if(json.status == "failed") {
						t.setStatus(json.message || json.exception);
					} else {
						t.setStatus(null, json.message);
					}
				} catch(err) {
					console.log(err);
				}
			}
		},
		
		postCreate: function() {
			this.CMenu.bindDomNode(this.Name.domNode);
			if(this.config) {
				this.Name.set("label", this.title ? this.title : this.getCompLabel());
			}
			this.testId = this.config.getConfigName() + "." + this.config.getName();
			this._eventsHandler = tdiapi.subscribeServerEvents(lang.hitch(this, "_updateTestConnectionEvent"));
			
			var type = tdiapi.getConnectorType(this.config);
			html.style(this._browseMenu.domNode, "display", "none");
			if(array.some(["LDAPSync:Form_AD","LDAPSync:Form_LDAP","LDAPSync:Form_TDS","LDAPSync:Form_SUN"], function(key) {
				return key === type;
			})) {
				html.style(this._browseMenu.domNode, "display", "");
			}
			
		}
	})	
})