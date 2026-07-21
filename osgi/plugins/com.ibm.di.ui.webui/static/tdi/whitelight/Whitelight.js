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
	"tdi/tdiutil",
	"tdi/tdiconstants",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/ldapsync/LDSUtil",
	"tdi/whitelight/Person",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"dojo/text!./templates/Whitelight.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, TabContainer, 
		Button, CheckBox, ComboBox, Form, Textarea, MenuItem, TableWidget, tdiapi, tdiutil, tdiconstants, tdiconfigentry, tdicientry, LDSUtil, Person, idx, HeaderPane, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : template,
		
		resize: function(obj) {
			if(obj) {
				this.Header.resize(obj);
			}
		},
		
		showEntry: function(sel) {
			var item = this.table.getItem(sel.rowId);
			var cn = this.table.getItemValue(item, "cn") || "(no name)";
			var mail = this.table.getItemValue(item, "mail") || "";
			var mobile = this.table.getItemValue(item, "mobile") || "";
			if(this.person) {
				this.person.destroyRecursive();
			}
			this.person = new Person({
				cn:cn, mail:mail, mobile:mobile, item:item,
				showEntry:lang.hitch(this, "showEntry")
			}).placeAt(this.Details);
			this.person.startup();
		},
		
		onSearch: function() {
			if(this.filterTimeout) {
				clearTimeout(this.filterTimeout);
			}
			if(this.Search.get("value") == "") {
				return;
			}
			this.runSearch(this.Search.get("value"));
//			this.filterTimeout = setTimeout(dojo.hitch(this, "runSearch", this.Search.get("value")), 1500);
		},
		
		runSearch: function(filter) {
			var t = this;
			var ldapfilter = filter;
			
			var params = {};
			if(filter.indexOf("*") == -1)
				params.filter = "(|" + "(cn=*" + filter + "*)(sn=*" + filter + "*))";
			else if(filter != "")
				params.filter = "cn=*" + filter + "*";
			
			idx.showProgressDialog("Searching " + params.filter);
			
			var req = {
					url:tdiapi._url_prefix + "/ldapsync/wp/search",
					headers: {
						"Content-Type":"application/json",
						"Accept":"application/json"
					},
					postData:dojo.toJson(params),
					handleAs:"json"
			};
			dojo.xhrPost(req).then(function(data) {
				idx.hideProgressDialog();
				t.updateResultList(data);
			}, function(err) {
				idx.hideProgressDialog();
				tdiutil.error(err);
			});
		},
		
		updateResultList: function(data) {
			this.table.setData(data.sort(function(a,b){
				if(a.cn < b.cn)
					return -1;
				else if(a.cn > b.cn)
					return 1;
				else
					return 0;
			}));
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			this.table = new TableWidget({
				idProperty:"$dn",
				structure:[{id:"cn", field:"cn", name:"Name", width:"auto"}],
				style:"width:100%; height:100%; margin:0; padding:0",
				onRowClick:lang.hitch(this, "showEntry")
			}).placeAt(this.tableDiv);
			this.table.startup();
			
		}

	})
});
