/**
 * LDSPta
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/form/_FormWidget",
	"dijit/form/TextBox",
	"dijit/Tree",
	"tdi/model/LDAPTreeStore",
	"dijit/tree/ObjectStoreModel",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDAPBrowser.html"
], function(declare, array, lang, _FormWidget, TextBox, Tree, LDAPTreeStore, ObjectStoreModel, tdiutil, nls, template) {
return declare(
	[_FormWidget, nls],
	{
		templateString: template,
		baseClass: "dijitTextBox",
		rootQuery: {"$dn":""},

		ldap: {
		},
		
		resize: function(obj) {
			if(obj && obj.h && this.tree) {
				this.tree.resize(obj);
			}
		},
		
		openBrowser: function() {
			
		},
		
		onError: function(err) {
			// summary:
			//		callback function
		},
		
		createLdapTree: function() {
			var t = this;
			
			this.store = new LDAPTreeStore({
				ldap: this.ldap,
				onError: lang.hitch(this, "onError")
			});
		    var model = new ObjectStoreModel({
		        store: this.store,
		        query:this.rootQuery,
		        getLabel: function(item) {
					var dn = item["$dn"];
					if(dn && dn.length > 0) {
						if(item.contextroot) {
							return dn;
						} else if(t.rootQuery["$display"] == "DN") {
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
				showRoot:t.rootQuery["$showRoot"],
				style:"height:300px; width:98%",
				title:"LDAP"
			})
			this.own(this.tree.on("click", function(item, node, event) {
				if(item && item["$dn"]) {
					t.set("value", item["$dn"]);
				}
			}));
			this.tree.placeAt(this.treenode);
			this.tree.startup();
		},
		
		_getValueAttr: function() {
			return this.textbox.value;
			this.inherited(arguments);
		},
		
		_setValueAttr: function(value) {
			this.inherited(arguments);
			this.textbox.value = value;
			if(!this.tree) {
				this.createLdapTree();
			}
		},
		
		startup: function() {
			this.inherited(arguments);
		}
	})
});
