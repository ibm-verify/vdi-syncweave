/**
 * LDSPta
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/aspect",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/Form",
	"dijit/form/TextBox",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/tdisession",
	"tdi/tdiutil",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/config/connector",
	"idx/dialogs",
	"idx/widget/SingleMessage",
	"./LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSPta.html"
], function(declare, array, lang, aspect, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		Button, CheckBox, Form, TextBox, tdiapi, tdiconstants, tdisession, tdiutil, tdiconfigentry, tdicientry, tdiconnector, idx, SingleMessage, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		ptaEnabled: false,
		
		verifyEP: function() {
			var t = this;
			var vals = t.Form1.get("value");
			idx.showProgressDialog(t.getString("FDS.ptaLogin1"));
			var sess = new tdisession();
			var sourceldap = t.getFlowConnector(true);
			var clone = tdiutil.clone(sourceldap.config);
			var conn = new tdiconnector({config:clone});
			conn.setInheritFrom("system:/Connectors/ibmdi.LDAP");
			conn.getConnectionConfig().setParam("ldapUrl", sourceldap.getConnectionConfig().getParam("source.ldap.url"));
			conn.getConnectionConfig().setParam("ldapUsername", vals.user1);
			conn.getConnectionConfig().setParam("ldapPassword", vals.pass1);
			sess.openSessionForConnector(conn, true).then(
					function(ok) {
						idx.hideProgressDialog();
						idx.info(t.getString("FDS.ptaLoginSuccess"));
						sess.close();
					},
					function(err) {
						idx.hideProgressDialog();
						sess.close();
						tdiutil.error(err);
					}
			);
		},

		getPTAParams: function() {
			var t = this;
			var source = this.getFlowConnector(true).getConnectionConfig();
//			var flow = this.getFlowConnector(false).getConnectionConfig();
			var flow = this.getGeneralSettings().getConnectionConfig();
			var iwe = {
				"ibm-slapdptasubtree": t.getPTASubTree(),
				"ibm-slapdptaurl": source.getParam("source.ldap.url"),
				"ibm-slapdptasearchbase": source.getParam("source.ldap.searchBase"),
				"ibm-slapdptabinddn": source.getParam("source.ldap.user"),
				"ibm-slapdptaattrmapping": "cn $ cn",
				"ibm-slapdptabindpw": source.getParam("source.ldap.password"),
				"target.ldap.url": flow.getParam("target.ldap.url"),
				"target.ldap.user": flow.getParam("target.ldap.user"),
				"target.ldap.password": flow.getParam("target.ldap.password")
			};
			return iwe;
		},
		
		getPTAConfig: function() {
			var flow = this.getGeneralSettings().getConnectionConfig();
			var value = this.Form2.get("value");
			delete value.enabled;
			value["target.ldap.url"] = flow.getParam("target.ldap.url");
			value["target.ldap.user"] = flow.getParam("target.ldap.user");
			value["target.ldap.password"] = flow.getParam("target.ldap.password");
			return value;
		},
	
		setPTAEnabled: function(enabled) {
			var result = {
					"enabled": enabled ? "on" : ""
			};
			this.Form2.set("value", result);
		},
		
		writePTA: function() {
			var t = this;
			if(!t.Form2.validate()) {
				t.setPTAEnabled(t._ptaEnabled);
				return;
			}
			
			if(t._ptaEnabled) {
				if(idx.confirm(this.getString("FDS.ptaConfirmDelete"), function() {
					t.deletePTA();
				}, function() {
					t.setPTAEnabled(true);
				}));
			} else {
				if(idx.confirm(this.getString("FDS.ptaConfirmCreate"), function() {
					t.addPTA();
				}, function() {
					t.setPTAEnabled(false);
				}));
			}
		},
		
		deletePTA: function() {
			var t = this;
			var iwe = t.getPTAConfig();
			iwe.operation = "Delete";
			idx.showProgressDialog(this.getString("FDS.ptaDeleting"));
			
			LDSUtil.executePTAService(iwe).then(
				function(data) {
					idx.hideProgressDialog();
					if(lang.isArray(data) && data.length > 0) {
						var result = data[0];
						if(t.hasError(result)) {
							t.showError(result);
							t.setPTAEnabled(true);
							t._ptaEnabled = true;
						} else {
							idx.info(t.getString("FDS.ptaDeleteSuccess"));
							t.getPTARecords();
						}
					} else {
						idx.info(t.getString("FDS.ptaUnexpectedError"));
					}
				},
				function(err) {
					idx.hideProgressDialog();
					tdiutil.error(err);
					t.setPTAEnabled(true);
				}
			);			
			
		},
		
		addPTA: function() {
			var t = this;
			var iwe = t.getPTAParams();
			iwe.operation = "Add";
			
			idx.showProgressDialog(this.getString("FDS.ptaCreating"));
			
			LDSUtil.executePTAService(iwe).then(
				function(data) {
					idx.hideProgressDialog();
					if(lang.isArray(data) && data.length > 0) {
						var result = data[0];
						if(t.hasError(result)) {
							t.showError(result);
							t.setPTAEnabled(false);
						} else {
							idx.info(t.getString("FDS.ptaAddSuccess"));
							t.getPTARecords();
						}
					} else {
						idx.info(t.getString("FDS.ptaUnexpectedError"));
					}
				},
				function(err) {
					idx.hideProgressDialog();
					tdiutil.error(err);
					t.setPTAEnabled(false);
				}
			);			
		},
		
		restartTDS: function() {
			idx.info(this.getString("FDS.restartSDSNeeded"));
		},
		
		
		verifyPTA: function() {
			var t = this;
			idx.showProgressDialog(t.getString("FDS.ptaLogin2"));
			var vals = t.Form4.get("value");
			var sess = new tdisession();
			var targetldap = t.getGeneralSettings(); // this.getFlowConnector(false);
			var clone = tdiutil.clone(targetldap.config);
			var conn = new tdiconnector({config:clone});
			//conn.parentConfig = targetldap.parentConfig;
			LDSUtil.mapProps2LDAP(conn);
			conn.getConnectionConfig().setParam("ldapUsername", vals.user4);
			conn.getConnectionConfig().setParam("ldapPassword", vals.pass4);
			sess.openSessionForConnector(conn, true).then(
					function(ok) {
						idx.hideProgressDialog();
						idx.info(t.getString("FDS.ptaLoginSuccess"));
						sess.close();
					},
					function(err) {
						idx.hideProgressDialog();
						sess.close();
						tdiutil.error(err);
					}
			);
		},
		
		getFlowConnector: function(input) {
			if(!this.config)
				return false;
			
			var name = input ? "Input" : "Output";
			
			var conn = this.config.getConnector(name);
			if(!conn)
				return false;
			else
				return conn;
		},
		
		getGeneralSettings: function() {
			if(!this.config)
				return null;
			
			return LDSUtil.getGeneralSettingsConnector(this.config.getTop());
		},
		
		getFlowParameters: function() {
			return this.config.getConnector("Output").getConnectionConfig().getNames();
		},
		
		getFlowParameter: function(param) {
			return this.config.getConnector("Output").getConnectionConfig().getParam(param);
		},
		
		checkPTA: function() {
			return this.getFlowConnector(true).getConnectionConfig().getParamBoolean("supportsPTA", false);
		},
		
		isMirroring: function() {
			return this.getFlowConnector(false).getConnectionConfig().getParamBoolean("global.preserveSourceContainers", false);
		},
		
		getPTASubTree: function() {
			if(this.isMirroring())
				return this.getFlowParameter("target.ldap.searchBase");
			else
				return this.getFlowParameter("target.suffixForUsers");			
		},
		
		showError: function(result) {
			var ptaParams = [];
			var t = this;
			for(f in result) {
				if(f.indexOf("ibm-") == 0) {
					ptaParams.push(f + ": " + result[f]);
				}
			}
			
			var ldapParams = [];
			for(f in result) {
				if(f.indexOf(".") > 0) {
					if(f == "target.ldap.password")
						ldapParams.push(f + ": (password not shown)");
					else
						ldapParams.push(f + ": " + result[f]);
				}
			}
			if(result.status == "fail") {
				/*
				var error = {
					detail:"<pre>\n" + result.exception + "\n" + result.message + "</pre>",
					summary:this.getString("FDS.ptaFailed", [result.connectorname]),
					moreContent:this.getString("FDS.ptaFailedMore", [ptaParams.join("<br>"), ldapParams.join("<br>")])
				};
				idx.error(error);
				 */
				if(this.singleMessageInstance && !this._destroyed) {
					this.singleMessageInstance.destroyRecursive();
				}
				
				this.singleMessageInstance = new SingleMessage({ 
				      type: "error",  
				      title: this.getString("FDS.sdsConnectionFailed"), 
				      dateFormat:{ 
				         selector: 'time',  
				         timePattern: 'hh:mm a' 
				      },  
				      style: 'width: 90%;', 
				      showAction: false,
				      description: result.exception
				   }); 
				this.singleMessageInstance.placeAt(this.messageArea); 
				this.singleMessageInstance.startup();			
			}
		},
		
		hasError: function(result) {
			return (result && result.status == "fail");
		},
		
		getPTARecords: function() {
			var t = this;
			var path = t.getPTASubTree(); 
			if(!path)
				path = "";
			
			// clear error/warning
			t.ptaStatus.innerHTML = "";
			
			if(path.length > 0) {
				var source = this.getFlowConnector(true).getConnectionConfig();
				var flow = this.getGeneralSettings().getConnectionConfig(); // getFlowConnector(false).getConnectionConfig();
				var iwe = {
					"ibm-slapdptasubtree": path,
					"target.ldap.url": flow.getParam("target.ldap.url"),
					"target.ldap.user": flow.getParam("target.ldap.user"),
					"target.ldap.password": flow.getParam("target.ldap.password"),
					"operation":"Read"
				};
				
				if(this.singleMessageInstance && !this._destroyed) {
					this.singleMessageInstance.destroyRecursive();
					this.singleMessageInstance = null;
				}
				
				LDSUtil.executePTAService(iwe).then(function(data) {
					if(data && data.length == 1) {
						var result = data[0];
						if(t.hasError(result)) {
							t.showError(result);
						} else {
							if(result["$dn"]) {
								t._ptaEnabled = true;
								t.Form2.set("value", t.mapResult(result));
								t.setPTAEnabled(true);
							} else {
								t.Form2.set("value", t.getPTAParams());
								t.setPTAEnabled(false);
								t._ptaEnabled = false;
								t.ptaStatus.innerHTML = "<center>" + this.getString("FDS.noPtaFound", path) + "</center>";
							}
						}
					}
				});
			}
		},
		
		mapResult: function(obj) {
			var nobj = {};
			for(f in obj) {
				nobj[f.toLowerCase()] = obj[f];
			}
			return nobj;
		},
		
		setConfig: function(config) {
			var t = this;
			t.config = config;
			t.removeListeners();
			t._modid = aspect.after(t.config, "onModify", lang.hitch(t, function(modified, args) {
				if(!t.togglePTA(false)) {
					return;
				}
				
				// if any of these props change it will affect current PTA
				var modprops = [
						"source.ldap.url",
						"source.ldap.user",
						"source.ldap.password",
						"global.preserveSourceContainers"
				];
				if(t.isMirroring())
					modprops.push("target.ldap.searchBase");
				else
					modprops.push("target.suffixForUsers");
				
				try {
					var param = args[1].param;
					if(array.indexOf(modprops, param.name) == -1) {
//						if(param.name == "source.entryTypes") {
//							if(!param.value.match(/.*person.*/i)) {
//								idx.info("")
//							}
//						}
						return;
					}
				
					var ptaSubtree = t.getPTASubTree();
					if(!t._ptaEnabled) {
						var value = t.getPTAParams();
						value["ibm-slapdptasubtree"] = ptaSubtree;
						t.Form2.set("value", value);
					} else {
						var src = t.getPTAParams();
						src["ibm-slapdptasubtree"] = ptaSubtree;
						var trg = t.Form2.get("value");
						var changes = false;
						for(f in src) {
							if(src[f] != trg[f]) {
								changes = true;
							}
						}
						if(changes && t.ptaStatus.innerHTML == "") {
							var msg = this.getString("FDS.ptaParamsChangedWarn"); 
							t.ptaStatus.innerHTML = "<i style='color:red'>" + msg + "</i>";
							idx.info(msg);
						}
					}
				} catch(ignore) {}
			}));
			
			this.togglePTA();
		},
		
		togglePTA: function(update) {
			if(this.checkPTA()) {
				dojo.style(this.ptaEnabled, "display", "");
				dojo.style(this.ptaDisabled, "display", "none");
				if(update)
					this.getPTARecords();
				return true;
			} else {
				dojo.style(this.ptaEnabled, "display", "none");
				dojo.style(this.ptaDisabled, "display", "");
				return false;
			}
		},
		
		removeListeners: function() {
			var t = this;
			if(t._modid && t._modid.remove) {
				t._modid.remove();
				t._modid = null;
			}
		},
		
		destroyRecursive: function() {
			this.removeListeners();
			this.inherited(arguments);
		},
		
		postCreate: function() {
		}
	})
});