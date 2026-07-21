require({cache:{
'url:tdi/ldapsync/templates/LDSPtaManager.html':"<div class=\"dijitReset\" style=\"width:100%; height:100%; padding:0px; margin:0px;\">\n\n\t<div data-dojo-type=\"idx/layout/BorderContainer\" style=\"width:100%; height:100%; padding:0; margin:0\" data-dojo-attach-point=\"Border\" gutters=\"false\">\n\t\n\t\t<div data-dojo-type=\"idx/layout/HeaderPane\" region=\"left\" title=\"${nls.FDS_ptaEntriesTitle}\" style=\"width:33%; height:100%; padding:0px; margin:0px;\" data-dojo-attach-point=\"Header\" splitter=\"true\">\n\t\t\t<div data-dojo-type=\"dijit/form/Button\" region=\"majorActions\" data-dojo-attach-event=\"onClick:addEntry\">${nls.SolutionInterfaceWidget_Add}</div>\n\t\t\t<div data-dojo-type=\"dijit/form/Button\" data-dojo-attach-event=\"onClick:onRestartSDS\" region=\"minorActions\">${nls.FDS_restartSDS}</div>\n\t\t\t<i></i>\n\t\t</div>\n\n\t\t<div data-dojo-type=\"idx/layout/HeaderPane\" region=\"center\" title=\"${nls.DataBrowser_32}\" style=\"width:98%; height:100%; padding:0px; margin:0px;\" data-dojo-attach-point=\"Details\">\n\t\t\t<i></i>\n\t\t</div>\n\t</div>\n</div>"}});
/**
 * LDSPta
 */
define("tdi/ldapsync/LDSPtaManager", [
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/query",
	"dojo/Deferred",
	"dojo/request",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/registry",
	"dijit/Tree",
	"tdi/TableWidget",
	"tdi/model/LDAPTreeStore",
	"dijit/tree/ObjectStoreModel",
	"./LDSUtil",
	"./LDSPtaEntry",
	"./LDSPtaSummary",
	"idx/form/buttons",
	"idx/dialogs",
	"tdi/tdiapi", 
	"tdi/tdiutil", 
	"tdi/NlsMixin", 
	"dojo/text!./templates/LDSPtaManager.html"
], function(declare, array, lang, query, Deferred, request, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, registry, Tree, TableWidget, LDAPTreeStore, ObjectStoreModel, LDSUtil, LDSPtaEntry, LDSPtaSummary, idxButtons, idx, tdiapi, tdiutil, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		// restartAL: String
		//		The URL to restart the SDS server
		restartAL: tdiapi._url_prefix + "/ldapsync/runal/LDAPSync/UI_RestartSDS",
		
		createGridView: function(data) {
			var t = this;
			var gs = LDSUtil.getGeneralSettingsConnector(this.config);
			this.ldap = {
				"target.ldap.url":gs.getConnectionConfig().getParam("target.ldap.url"),
				"target.ldap.user":gs.getConnectionConfig().getParam("target.ldap.user"),
				"target.ldap.password":gs.getConnectionConfig().getParam("target.ldap.password"),
				"target.ldap.rootdn":"cn=Passthrough Authentication,CN=CONFIGURATION"
			};
			
			this.store = new LDAPTreeStore({
				ldap: this.ldap
			});
			
			this.reloadData();
			
		},
		
		listPTA: function(callback) {
			var t = this;
			var results = this.store.query({parent:this.ldap["target.ldap.rootdn"]});
			var data = results;
			if(results.map) {
				data = results.map(function(item) {
					return item;
				});
			}
			if(data.then) {
				data.then(function(arr) {
					callback(arr);
				}, function(err) {
					console.log(err);
				})
			} else {
				callback(data);
			}
		},
		
		reloadData: function() {
			this.listPTA(lang.hitch(this, "createGridViewWithData"));
		},
		
		createGridViewWithData: function(data) {
			var t = this;
			t.data = data;
			
			this.updateGlobalPasswordCacheProperty(data);
			
			if(this.grid) {
				this.grid.destroyRecursive();
				this.grid = null;
			}
			
			this.grid = new TableWidget({
				idProperty:"$dn",
				cellWidget:true,
				structure: [
				    {	field:"cn",
				    	id:"cn",
				    	name:t.getString("WebCE.name"),
				    	width:"auto",
				    	widgetsInCell:true,
						decorator: function() {
							return [
								"<div data-dojo-attach-point='ptaSummary' data-dojo-type='tdi/ldapsync/LDSPtaSummary' ",
								"class='gridxHasGridCellValue' style='width: 100%;'></div>"
							].join('');
						},
						setCellValue: function(gridData, storeData, cellWidget, more) {
							if(cellWidget.ptaSummary) {
								t.toEditor("", "", cellWidget.cell, cellWidget.ptaSummary);
							}
						}
				    }
				],
				onSelected: lang.hitch(this, "showDetails"),
				data:data
			});
			this.grid.startup();
			
			this.Header.set("content", this.grid);
			this.Header.resize();
			
			idx.hideProgressDialog();
			
		},
		
		getItem: function(id) {
			var data = null;
			if(!this.grid) {
				var arr = array.filter(this.data, function(item) {
					return item["$dn"] == id;
				});
				if(arr && arr.length == 1)
					data = arr[0];
			} else {
				data = this.grid.getItem(id);
			}
			if(!data)
				return data;
			else
				return this.lowercaseItem(data);
		},
		
		lowercaseItem: function(data) {
			var d = {};
			for(var f in data) {
				if(lang.isArray(data[f]) && data[f].length == 1) {
					d[f.toLowerCase()] = data[f][0];
				} else {
					d[f.toLowerCase()] = data[f];
				}
			}
			return d;
		},
		
		toEditor: function(storeData, gridData, cell, editor) {
			var data = this.getItem(cell.row.id);
			editor.setData(this, data);
			editor.cell = cell;
			return storeData;
		},
		
		addEntry: function() {
			this.setDetails({
			}, true)
		},
		
		resize: function(obj) {
			if(obj && obj.h) {
				this.Border.resize(obj);
				this.Border.layout();
			}
		},
		
		showDetails: function(row) {
			var item = this.getItem(row.id);
			if(item && !item.contextroot) {
				this.setDetails(item, false);
			}
		},
		
		setDetails: function(item, isnew) {
			var t = this;
			if(this.pta && this.pta.getModifiedFields().length > 0) {
				tdiutil.ask(this.getString("WebCE.abandonChanges"), function(ok) {
					if(ok == 1)
						t._setDetails(item, isnew);
				})
			} else {
				t._setDetails(item, isnew);
			}
		},
		
		_setDetails: function(item, isnew) {
			if(isnew) {
				this.grid.clearSelection();
			}
			this.pta = new LDSPtaEntry({
				parent:this,
				data:item,
				onSaveEntry:lang.hitch(this, "onSavePta", false),
				onDeleteEntry:lang.hitch(this, "onDeletePta"),
				onVerifyConnection:lang.hitch(this, "onVerifyConnection"),
				isNew:isnew
			});
			this.pta.startup();
			this.Details.set("content", this.pta);
			this.Details.set("title", item.cn ? item.cn : this.getString("SolutionInterfaceWidget.Add"));
		},
		
		onDeletePta: function(pta) {
			var t = this;
			idx.confirm(t.getString("WebCE.deleteItem") + " " + pta.data.cn, function() {
				idx.showProgressDialog(t.getString("FDS.ptaDeleteEntry"), 30000);
				t.store.removeEntry(pta.data["$dn"]).then(function() {
					idx.hideProgressDialog();
					t.Details.set("content", "");
					t.Details.set("title", "");
					t.grid.deleteItem(pta.data["$dn"]);
					idx.info(t.getString("FDS.restartServerInfo"));
				}, function(err) {
					idx.hideProgressDialog();
					tdiutil.error(err);
				});
			});
		},
		
		onSavePta: function(restart) {
			var t = this;			
			var timeout = 30000;
			
			if(t.pta && !t.pta._destroyed && t.pta.getModifiedFields().length > 0) {
				
				idx.showProgressDialog(t.getString("FDS.ptaCreateRoot"), timeout);
				
				t._enablePta().then(function() {
					return t._createPtaRoot();
					
				}).then(function(ok) {
					var item = lang.mixin({}, t.pta.getModifiedFormData());
					item["$dn"] = t.pta.data["$dn"];
					if(t.pta.isNew) {
						item["$dn"] = "cn=" + item.cn + ",cn=Passthrough Authentication,cn=Configuration";
						item.objectclass = ["ibm-slapdConfigEntry","ibm-slapdPta","ibm-slapdPtaExt"];
						item["ibm-slapdPtaAttrMapping"] = "cn $ cn";
						idx.showProgressDialog(t.getString("FDS.ptaAddEntry"), timeout);
						return t.store.addEntry(item["$dn"], item);
					} else {						
						idx.showProgressDialog(t.getString("FDS.ptaModifyEntry"), timeout);
						return t.store.modifyEntry(item["$dn"], item);
					}
					
				}).then(function(data) {
					var item = data[0];
					idx.showProgressDialog(t.getString("FDS.readEntry"), timeout);
					return t.store.readEntry(item["$dn"]);
					
				}, function(writeErr) {
					idx.hideProgressDialog();
					var exception = lang.getObject("response.data.exception", false, writeErr);
					if(exception && exception.match(/InvalidAttributeValueException/)) {
						idx.error(t.getString("FDS.ptaDuplicateBranch"));
					} else if(exception && exception.match(/NameAlreadyBoundException/)) {
						idx.error(t.getString("FDS.ptaDuplicateName"));
					} else {
						tdiutil.error(writeErr);
					}
					
				}).then(function(data) {
					var item = data[0];
					idx.hideProgressDialog();
					if(t.pta.isNew)
						t.grid.addItem(item);
					
					t.pta.setNew(false);
					t.pta.setData(item);
					t.Details.set("title", t.pta.getFormData()["cn"]);
					
					t.updateGlobalPasswordCacheProperty(null, item);
					t.listPTA(lang.hitch(t, "updateGlobalPasswordCacheProperty"));;
					
					idx.info(t.getString("FDS.restartServerInfo"));
				}, function(err) {
					idx.hideProgressDialog();
					tdiutil.error(err);
				})
			}
		},
		
		onVerifyConnection: function() {
			var t = this;
			var user = t.pta.getParamValue("ibm-slapdptabinddn");
			tdiutil.prompt([
			    {
			    	label:t.getString("WebCE.username"),
			    	value:user
			    },
			    {
			    	label:t.getString("WebCE.password"), 
			    	type:"password",
			    	tabIndex:1
			    }
			], function(value) {
				t._verifyConnection(value[0], value[1]);
			}, t.getString("FDS_ptaEnterCredentials"));
		},
		
		_verifyConnection: function(user, password) {
			var t = this;
			idx.showProgressDialog(t.getString("FDS.verifyConnection"));
			var cfg = {
				"target.ldap.url": t.pta.getParamValue("ibm-slapdptaurl"),
				"target.ldap.user": user,
				"target.ldap.password": password
			};
	
			var ld = new LDAPTreeStore({ldap:cfg});
			ld.readEntry("").then(function(data) {
				idx.hideProgressDialog();
				idx.info(t.getString("FDS.connectionOK"));
			}, function(err) {
				idx.hideProgressDialog();
				tdiutil.error(err);
			});
		},
		
		onRestartSDS: function() {
			var t = this;
			idx.confirm(t.getString("FDS.restartSDS"), function() {
				idx.showProgressDialog(t.getString("FDS.restartingSDS"));
				var gs = LDSUtil.getGeneralSettingsConnector(t.config);
				var ldap = {
					initParams: {
						"target.ldap.url":		gs.getConnectionConfig().getParam("target.ldap.url"),
						"target.ldap.user": 	gs.getConnectionConfig().getParam("target.ldap.user"),
						"target.ldap.password":	gs.getConnectionConfig().getParam("target.ldap.password"),
						"target.ldap.searchfilter":	"objectclass=*"
					}
				};
				
				request.post(t.restartAL, {
					handleAs: "json",
					headers: {
						"Accept":"application/json",
						"Content-Type":"application/json"
					},
					data: dojo.toJson(ldap)
				}).then(function() {
					t._recoverFromRestart();
				}, function(err) {
					idx.hideProgressDialog();
					tdiutil.error(err);
				});
			});
		},
		
		_recoverFromRestart: function() {
			// summary:
			//		After a restart, it may take some time before the server is
			//		back in business.
			var t = this;
			idx.showProgressDialog(this.getString("FDS.restartSDSrecover"))
			this.store.readEntry("").then(function(ok) {
				idx.hideProgressDialog();
				t.reloadData();
			}, function(err) {
				setTimeout(lang.hitch(t, "_recoverFromRestart"), 2000);
			});
		},
		
		_readSdsConfiguration: function() {
			// summary:
			//		Reads the "cn=Configuration" object from SDS
			var t = this;
			return t.store.readEntry("cn=configuration").then(function(data) {
				t._sdsConfiguration = {};
				if(lang.isArray(data) && data.length == 1) {
					t._sdsConfiguration = t.lowercaseItem(data[0]);
				}
				return t._sdsConfiguration;
			});
		},
		
		_enablePta: function() {
			// summary:
			//		Sets the ibm-ptaslapdPtaEnabled attribute to "true"
			//		in "cn=configuration".
			var deferred = new Deferred();
			var pta = this._sdsConfiguration ? this._sdsConfiguration["ibm-slapdptaenabled"] : "";
			if(pta.toLowerCase() == "true") {
				deferred.resolve(true);
			} else {
				deferred = this.store.modifyEntry("cn=configuration", {
					"$dn":"cn=configuration",
					"ibm-slapdPtaEnabled":"true"
				});
			}
			return deferred;
		},
		
		_createPtaRoot: function() {
			// summary:
			//		Creates the "cn=Passthrough Authentication" object.
			//		under "cn=configuration".
			var deferred = new Deferred();
			deferred.resolve(true);
			return deferred;
		},
		
		updateGlobalPasswordCacheProperty: function(data, item) {
			// summary:
			//		Creates a list of DNs from the array of PTA entries in data
			//		This array is compared with the existing one in the configuration
			//		and updates the configuration if there are changes.
			var gs = LDSUtil.getGeneralSettingsConnector(this.config);		
			var curval = gs.getConnectionConfig().getParam("password.cache.enabled");
			var arr = [];
			
			// -- On item we add/remove a single DN
			if(item) {
				var dn = item["ibm-slapdptasubtree"]
				if(item["ibm-slapdptamigratepwd"] != "true" && item["ibm-slapdptamigratepwd"] != "on") {
					arr = array.filter(curval.split("&&"), function(odn) {
						return dn.toLowerCase() != odn;
					});
				} else {
					if(!array.some(curval.split("&&"), function(odn) {
						return dn.toLowerCase() == odn;
					})) {
						arr.push(dn.toLowerCase());
					}
				}
			} else {
				array.forEach(data, function(item) {
					if(item["ibm-slapdptamigratepwd"] == "true") {
						arr.push(item["ibm-slapdptasubtree"].toLowerCase());
					}
				});
			}
			arr = arr.sort();
			var newval = arr.join("&&");
			if(newval != curval) {
				gs.getConnectionConfig().setParam("password.cache.enabled", newval);
			}
		},
		
		startup: function() {
			this.createGridView();
			this._readSdsConfiguration();
			this.Header.onRefresh = lang.hitch(this, "reloadData");
			this.inherited(arguments);
		}
	})
});