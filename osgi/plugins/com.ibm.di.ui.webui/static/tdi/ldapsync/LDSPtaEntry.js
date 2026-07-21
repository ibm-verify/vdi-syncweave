/**
 * LDSPta
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/dom-class",
	"dojo/store/Memory",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"dijit/MenuItem",
	"idx/grid/PropertyGrid",
	"idx/grid/PropertyFormatter",
	"idx/widget/EditController",
	"./LDSUtil",
	"tdi/LDAPBrowser", 
	"tdi/tdiapi", 
	"tdi/NlsMixin",
	"tdi/forms/_FormWidgetMixinFunctions",
	"dojo/text!./templates/LDSPtaEntry.html"
], function(declare, array, lang, html, domClass, Memory, dTopic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TextArea, TextBox, MenuItem, PropertyGrid, PropertyFormatter, EditController, LDSUtil, LDAPBrowser, tdiapi, nls, forms, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		editable: true,

		_toggleClose: function(mouse) {
			domClass.toggle(this.closeNode, "dijitTabCloseButtonHover");
		},
		
		_onDeleteEntry: function() {
			alert("Delete PTA entry");
		},
		
		_onEditEntry: function() {
			alert("Enable editing")
		},
		
		_onPtaLogin: function() {
			
			this.onPtaLogin(this.data, this);
		},
		
		onSaveEntry: function() {
			// summary:
			//		callback function
		},
		
		_onSaveEntry: function() {
			var data = this.getModifiedFormData();
			if(!this.isNew)
				delete data["cn"];
			
			for(var f in data) {
				if(!data[f] || data[f] == "") {
					idx.info(this.getString("FDS.ptaAllFieldsAreRequired"));
					return;
				}
			}
			this.onSaveEntry(this);
		},
		
		onDeleteEntry: function() {
			// summary:
			//		callback function
		},
		
		_onDeleteEntry: function() {
			this.onDeleteEntry(this);
		},
		
		onVerifyConnection: function() {
			// summary:
			//		callback function
		},
		
		normalizeDN: function(dn) {
			if(dn) {
				dn = dn.replace(/\W?,\W?/g, ",");
				return dn.toLowerCase();
			} else {
				return dn;
			}
		},
		
		updateTemplateMenu: function() {
			var t = this;
			var top = t.parent.config.getTop();
			var ldapForms = {"Form_AD":"", "Form_TDS":"", "Form_SUN":"", "Form_LDAP":""};
			array.forEach(top.getConnectorNames(), function(name) {
				var conn = top.getConnector(name);
				var type = conn.getConnectionConfig().getParam("source.form");
				if(type in ldapForms) {
					var m = new MenuItem({
						label:name.substring("Source_".length),
						onClick:lang.hitch(t, "populateFromEndpoint", conn)
					})
					t.templateMenu.addChild(m);
				}
			});
		},
		
		updateAffectedFlows: function() {
			var t = this;
			var top = t.parent.config.getTop();
			var subtree = t.normalizeDN(t.data["ibm-slapdptasubtree"]);
			array.forEach(top.getAssemblyLineNames(), function(name) {
				var alc = top.getAssemblyLine(name);
				try {
					var output = alc.getConnector("Output");
					if (!output || typeof output.getConnectionConfig !== "function") return;
					
					var mirror = output.getConnectionConfig().getParamBoolean("global.preserveSourceContainers", false);
					var sb = t.normalizeDN(output.getConnectionConfig().getParam( mirror ? "target.ldap.searchBase" : "target.suffixForUsers"));
					if(sb && sb.indexOf(subtree) != -1) {
						var a = t.data["affectedflows"] = t.data["affectedflows"] || [];
						a.push(name.substring("Flow_".length) + " --> " + sb);
					}
				} catch(err) {
					console.log(err);
				}
			});
		},
		
		setParamValue: function(param, value) {
			var obj = {};
			obj[param] = value;
			this.setFormData(obj);
		},
		
		getParamValue: function(param) {
			return this.getFormData()[param];
		},
		
		updateControl: function() {
			// dummy function called by form mixin function
		},
		
		onBrowseData: function() {
			dTopic.publish("ldapsync/browsetarget", {
				title:"PTA:"+this.getFormData()["ibm-slapdptasubtree"],
				options:{
					loginButton:true
				},
				"target.ldap.searchBase":this.getFormData()["ibm-slapdptasubtree"]
			});
		},
		
		onBrowseSDS: function() {
			forms._selectLDAPSearchBase(
				LDSUtil.getGeneralSettingsConnector(this.parent.config),
				"ibm-slapdptasubtree",
				this,
				this.getString("FDS.targetSubtree")
			);
		},
		
		onBrowseTarget: function() {
			var ldap = this.getFormData();
			ldap["target.ldap.url"] = ldap["ibm-slapdptaurl"];
			ldap["target.ldap.user"] = ldap["ibm-slapdptabinddn"];
			ldap["target.ldap.password"] = ldap["ibm-slapdptabindpw"];
			forms._selectLDAPSearchBase(
				ldap,
				"ibm-slapdptasearchbase",
				this,
				this.getString("FDS.searchBase")
			);
		},
		
		updateSubtree: function() {
			var t = this;
			var arr = [];
			array.forEach(t.parent.config.getAssemblyLineNames(), function(name) {
				if(name.indexOf("Flow_") == -1)
					return;
				
				var base = t.parent.config.getAssemblyLine(name).getConnector("Output").getConnectionConfig().getParam("target.ldap.searchBase");
				if(base != null && base != "")
					arr.push({id:base, name:base});
			});
			this.subtree.set("store", new Memory({data:arr}))
		},
		
		getFormData: function() {
			var data = this.Form.get("value");
			data["ibm-slapdptamigratepwd"] = 
				data["ibm-slapdptamigratepwd"] == "true" ? "true" : "false"; 
			return data;
		},
		
		setFormData: function(obj) {
			var migpwd = obj["ibm-slapdptamigratepwd"];
			if(typeof(migpwd) != "undefined") {
				obj["ibm-slapdptamigratepwd"] = migpwd == "true" ? "true" : "";
			}
			this.Form.set("value", obj);
		},
		
		getModifiedFields: function() {
			var data = this.getFormData();
			var changed = [];
			for(var f in data) {
				if(this.isNew || this.data[f] != data[f]) {
					if( (this.data[f] == "" || typeof(this.data[f]) == "undefined") && data[f] == "false") {
						;
					} else {
						changed.push(f);
					}
				}
			}
			return changed;
		},
		
		getModifiedFormData: function() {
			var data = this.getFormData();
			var changed = {};
			array.forEach(this.getModifiedFields(), function(f) {
				changed[f] = data[f];
			});
			return changed;
		},
		
		populateFromEndpoint: function(conn) {
			var obj = {};
			var cfg = conn.getConnectionConfig();
			obj["ibm-slapdptaurl"] = cfg.getParam("source.ldap.url");
			obj["ibm-slapdptasearchbase"] = cfg.getParam("source.ldap.searchBase");
			obj["ibm-slapdptabinddn"] = cfg.getParam("source.ldap.user");
			obj["ibm-slapdptabindpw"] = cfg.getParam("source.ldap.password");
			this.setFormData(obj);
		},
		
		setNew: function(isnew) {
			this._name.style.display = isnew?"":"none";
			this.isNew = isnew;
			this._deleteBtn.set("disabled", isnew);
		},
		
		setData: function(data) {
			this.data = data;
			this.setFormData(this.data);
		},
		
		postCreate: function() {
			this.setNew(this.isNew);
			this.updateAffectedFlows();
			this.updateTemplateMenu();
			this.setFormData(this.data);
//			this.affectedFlows.innerHTML = this.data.affectedflows ? this.data.affectedflows.join("<br>") : "";  
			this.updateSubtree();
		}
	})
});