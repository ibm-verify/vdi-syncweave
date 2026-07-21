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
	"dijit/layout/AccordionContainer",
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
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"./Card",
	"dojo/text!./templates/Person.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, AccordionContainer, BorderContainer, TabContainer, 
		Button, CheckBox, ComboBox, Form, Textarea, MenuItem, TableWidget, tdiapi, tdiutil, tdiconstants, tdiconfigentry, tdicientry, LDSUtil, idx, HeaderPane, Card, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : template,
		
		resize: function(obj) {
			this.Border.resize(obj);
		},

		runQuery: function(param, value, table) {
			var params = {};
			params[param] = value;
			var req = {
				url:tdiapi._url_prefix + "/ldapsync/wp/search",
				headers: {
					"Content-Type":"application/json",
					"Accept":"application/json"
				},
				postData:dojo.toJson(params),
				handleAs:"json"
			};
			var t = this;
			dojo.xhrPost(req).then(function(data) {
				for(var i = 0; i < data.length; i++) {
					var item = data[i];
					var tr = dojo.create("tr", {}, table);
					var td = dojo.create("td", {}, tr);
					try {
						new Card({
							item:item,
							showEntry:lang.hitch(this, "showEntry")
						}).placeAt(td);
					} catch(err) {
						console.log(err);
					}
				}
			}, function(err) {
				tdiutil.error(err);
			});
		},
		
		getShortDesc: function(item) {
			return item.cn[0] + " (" + this.getTitle(item) + ", " + item.co + ")";			
		},
		
		getTitle: function(item) {
			var prop = null;
			if(item.jobresponsibilities)
				prop = item.jobresponsibilities;
			else if(item.title)
				prop = item.title;
			
			if(prop) {
				if(lang.isArray(prop))
					return prop[0];
				else
					return prop;
			}
			return "";
		},
		
		showEntry: function(item) {
		},
		
		postMixInProperties: function() {
			if(this.item) {
				if(!this.item.jobresponsibilities) {
					this.item.jobresponsibilities = [""];
				}
				if(!this.item.co) {
					this.item.co = [""];
				}
			}
		},
		
		postCreate: function() {
			this.inherited(arguments);
			var table = dojo.create("table", {cellspacing:"5px"});
			var item = this.item;
			var arr = new Array();
			for(prop in item) {
				if(prop.charAt(0) != "_")
					arr.push(prop);
			}
			arr = arr.sort();
			array.forEach(arr, function(prop) {
				var tr = dojo.create("tr", {valign:"top"}, table);
				dojo.create("td", {innerHTML:prop}, tr);
				dojo.create("td", {innerHTML:item[prop].join("<br>")}, tr);
			});
			this.Details.set('content', table);
			
			if(lang.isArray(this.item.manager) && this.item.manager.length > 0) {
				var t = this;
				t.runQuery("dnlist", this.item.manager, this.Manager);
				t.runQuery("filter", "manager=" + t.item.manager[0], this.Peers);
				t.runQuery("filter", "manager=" + t.item["$dn"][0], this.Subs);
			}
		}

	})
});
