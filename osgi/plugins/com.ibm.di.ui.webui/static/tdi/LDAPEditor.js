/**
 * LDAPEditor 
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/Deferred",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/ProgressBar",
	"dijit/Tree",
	"dijit/form/Button",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"tdi/model/LDAPTreeStore",
	"dijit/tree/ObjectStoreModel",
	"idx/dialogs",
	"idx/form/Link",
	"idx/grid/PropertyGrid",
	"idx/grid/PropertyFormatter",
	"idx/widget/EditController",
	"tdi/tdiutil",
	"tdi/LDAPEntry",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDAPEditor.html"
], function(declare, array, lang, html, Deferred, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ProgressBar, Tree, Button, TextArea, Textbox, LDAPTreeStore, ObjectStoreModel, idx, Link, PropertyGrid, PropertyFormatter, EditController, tdiutil, LDAPEntry, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		editable: true,

		ldap: {
		},
		
		ldapParams: {
		    "ldapUrl":"target.ldap.url",
			"ldapUsername":"target.ldap.user",
			"ldapPassword":"target.ldap.password",
		    "target.ldap.url":"target.ldap.url",
			"target.ldap.user":"target.ldap.user",
			"target.ldap.password":"target.ldap.password",
		    "source.ldap.url":"target.ldap.url",
			"source.ldap.user":"target.ldap.user",
			"source.ldap.password":"target.ldap.password"
		},
		
		constructor: function(args) {
			this.ldap = new Object();
			if(args)
				lang.mixin(this, args);
		},
		
		resize: function(obj) {
			if(obj && obj.h && this.Border) {
				this.Border.resize(obj);
			}
		},
		
		deleteEntry: function() {
			alert("Delete entry")
		},
		
		showDetails: function(name, oldSelection, selection, edit) {
			var t = this;
			if(t.ldapEntry && t.ldapEntry.isModified()) {
				idx.confirm(this.getString("FDS.confirmDiscardChanges"), function(ok) {
					t.showLDAPEntry(name, oldSelection, selection, edit);
				})
			} else {
				t.showLDAPEntry(name, oldSelection, selection, edit);
			}
		},
		
		showLDAPEntry: function(name, oldSelection, selection, edit) {
			var t = this;
			var div = html.create("div", {style:"width:100%; height:100%; padding:0; margin:0"});
			var pbar = null;
			array.forEach(selection, function(item) {
				var deferred = new Deferred();
				// -- always read entry from server (unless we're adding a new entry)
				if(item["$dn"] == "" && edit) {
					deferred.resolve(item);
				} else {
					pbar = new ProgressBar({
						indeterminate:true,
						style:"width:200px;"
					}).placeAt(div);
					deferred = t.store.readEntry(item["$dn"]);
				}
				
				deferred.then(function(item) {
					if(lang.isArray(item))
						item = item.pop();
					if(pbar) {
						pbar.destroyRecursive();
						pbar = null;
					}
					t.ldapEntry = new LDAPEntry({
						item:item,
						editMode:edit,
						showAllAttributes:edit,
						editable:true,
						sortRequiredFirst:edit,
						onSaveEntry:lang.hitch(t, "_saveDataEntry"),
						onDeleteEntry:lang.hitch(t, "_deleteDataEntry"),
						ldapStore:t.store,
						onLoad:function() {
							t.Border.layout();
						},
						onLoginEntry:lang.hitch(t, "_testLogin")
					});
					t.Details.set("content", t.ldapEntry); 
					t.Details.set("title", item["$dn"]);
					t.Border.layout();
					t._currentDN = item["$dn"];
				});
				
			});
		},
		
		getParam: function(key) {
			var value = null;
			if(this.configOverride && this.configOverride[key]) {
				value = this.configOverride[key];
			}
			if(this.config && !value) {
				value = this.config.getConnectionConfig().getParam(key);
			}
			return value;
		},
		
		createLdapTree: function(rootdn, customQuery) {
			var t = this;
			
			if(rootdn === null || typeof(rootdn) == "undefined") {
				array.forEach(["target.ldap.searchBase", "source.ldap.searchBase"], function(p) {
					var value = t.getParam(p);
					if(value)
						t.ldap["target.ldap.rootdn"] = value;
				});
			} else {
				t.ldap["target.ldap.rootdn"] = rootdn;
			}

			// Get LDAP parameters
			for(var p in t.ldapParams) {
				var value = t.getParam(p);
				if(value) {
					t.ldap[t.ldapParams[p]] = value;
				}
			}
			
			this.store = new LDAPTreeStore({
				ldap: this.ldap
			});
			
			var query = customQuery || {
				"$dn":t.ldap["target.ldap.rootdn"]					
			};
			
		    var model = new ObjectStoreModel({
		        store: this.store,
		        query: query,
		        getLabel: function(item) {
					var dn = item["$dn"];
					if(dn && dn.length > 0) {
						if(item.contextroot || item["$type"] == "Search" || dn == t.ldap["target.ldap.rootdn"]) {
							return dn;
						} else {
							var ix = dn.search(/[^\\],/);
							if (ix >= 0)
								return dn.substr(0, ix+1);
							else
								return dn.split(",")[0];
						}
					} else {
						return t.ldap["target.ldap.rootdn"] ? t.ldap["target.ldap.rootdn"] : t.ldap["target.ldap.url"];
					}
		        }
		    });
		    
			this.tree = new Tree({
				model:model,
				showRoot:true,
				style:"height:300px; width:98%",
				title:this.getString("view.name.0")
			})
			
			this.own(
				this.tree.watch(
					"selectedItems",
					lang.hitch(this, "showDetails")
				)
			);
			
			this.tree.startup();
			this.Header.set("content", this.tree);
		},
		
		createModel: function() {
			var t = this;
			this.store = new LDAPTreeStore({
				ldap: this.ldap
			});
						
		    var model = new ObjectStoreModel({
		        store: this.store,
		        query:{"$dn":t.ldap["target.ldap.rootdn"]},
		        getLabel: function(item) {
					var dn = item["$dn"];
					if(dn && dn.length > 0) {
						if(item.contextroot || item["$type"] == "Search" || dn == t.ldap["target.ldap.rootdn"]) {
							return dn;
						} else {
							var ix = dn.search(/[^\\],/);
							if (ix >= 0)
								return dn.substr(0, ix+1);
							else
								return dn.split(",")[0];
						}
					} else {
						return t.ldap["target.ldap.rootdn"] ? t.ldap["target.ldap.rootdn"] : t.ldap["target.ldap.url"];
					}
		        }
		    });
		    return model;
		},
		
		showRoot: function() {
			this.createLdapTree("");
		},
		
		showSearchBase: function(base) {
			this.createLdapTree();
		},
		
		_search: function() {
			var filter = this._searchText.get("value");
			if(filter.indexOf("=") == -1) {
				filter = "cn=*" + (filter.length == 0 ? "" : filter + "*");
			}
			var base = this._searchBase.get("value");
			this.createLdapTree(base, {
				"$dn":base + " (" + filter + ")",
				"$type":"Search",
				base:base,
				filter:filter,
				scope:"subtree"
			});
			this._searchDlgButton.closeDropDown(false);
		},
		
		_saveEntry: function(pg) {
			var data = pg.get("data");
			this._saveDataEntry(data, data);
		},
		
		_testLogin: function(data) {
			var t = this;
			var ldap = {};
			if(data && data["$dn"]) {
				// Get LDAP parameters
				for(var p in t.ldapParams) {
					var value = t.getParam(p);
					if(value) {
						ldap[t.ldapParams[p]] = value;
					}
				}
				tdiutil.prompt({label:t.getString("WebCE.password"), type:"password"}, function(pwd) {
					if(pwd) {
						idx.showProgressDialog(data["$dn"]);
						ldap["target.ldap.user"] = data["$dn"];
						ldap["target.ldap.password"] = pwd;
						var store = new LDAPTreeStore({
							ldap: ldap
						});
						store.readEntry(data["$dn"]).then(function(data) {
							idx.hideProgressDialog();
							idx.info(t.getString("FDS.ptaLoginSuccess"));
						}, function(err) {
							idx.hideProgressDialog();
							if(err.response.data && err.response.data.message)
								idx.error(err.response.data.message);
							else
								tdiutil.error(err);
						})
					}			
				}, t.getString("FDS.ptaTestLogin"));
			}
		},
		
		_saveDataEntry: function(data, changes, source) {
			if(changes && changes.dn) {
				delete changes.dn;
			}
			
			var t = this;
			if(data.dn) {
				data["$dn"] = data.dn;
				delete data.dn;
				this.store.addEntry(data["$dn"], data).then(function() {
					if(source && source.onSaveComplete)
						source.onSaveComplete();
					t.Details.set("title", data["$dn"] + " - " + t.getString("FDS_entrySaved"));
				}, function(err) {
					tdiutil.error(err);
				});
			} else {
				this.store.modifyEntry(data["$dn"], changes).then(function() {
					if(source && source.onSaveComplete)
						source.onSaveComplete();
					t.Details.set("title", data["$dn"] + " - " + t.getString("FDS_entryModified"));
				}, function(err) {
					tdiutil.error(err);
				});
			}
		},
		
		_deleteEntry: function(pg) {
			var data = pg.get("data");
			this._deleteDataEntry(data);
		},
		
		_deleteDataEntry: function(data) {
			var t = this;
			idx.confirm(t.getString("WebCE.deleteItem") + "\n"  + data["$dn"], function() {
				t.store.removeEntry(data["$dn"]).then(function() {
					t.Details.set("title", data["$dn"] + " - " + t.getString("FDS_entryDeleted"));
					t.Details.set("content", "");
					t.ldapEntry = null;
				}, function(err) {
					tdiutil.error(err);
				});
			});
		},
		
		_addEntry: function() {
			var t = this;
			var dn = this._currentDN;
			idx.showProgressDialog(t.getString("FDS.loadingSchema"));
			this.store.readSchema("*", true).then(function(data) {
				idx.hideProgressDialog();
				tdiutil.selectFromFilteringSelect(data.objectclasses.sort(), t.getString("WebCE.newItem"), function(value) {
					t.store.readSchema(value, true).then(function(data) {
						var oc = [];
						for(var f in data) {
							if(f != "__syntax__")
								oc.push(f);
						}
						t.showDetails("", [], [{"$dn":"","objectClass":oc}], true);
					});
				}, "inetOrgPerson");
			}, function(err) {
				idx.hideProgressDialog();
				tdiutil.error(err);
			});
		},
		
		startup: function() {
			this.inherited(arguments);
			
			this.createLdapTree();
			this.Border.resize();
			
			if(this.ldap["target.ldap.rootdn"] && this._searchBase) {
				this._searchBase.set("value", this.ldap["target.ldap.rootdn"]);
			}
		}
	})
});