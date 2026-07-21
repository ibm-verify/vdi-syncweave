define([
	"dojo/_base/declare",
	"tdi/tdiapi"
], function(declare, tdiapi) {
return declare("tdi.tdiatom",
	null,
	{
// 	dojo.declare("tdi.tdiatom", null, {
		
		atom : null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getId : function() {
			return this.atom.id;
		},
		
		getUpdated : function() {
			if(this.atom)
				return this.atom.updated;
			else
				return "";
		},
		
		getTitle : function() {
			if(dojo.isObject(this.atom.title))
				return this.atom.title.value;
			else
				return null;
		},
		
		getLink : function(rel) {
			var arr = dojo.filter(this.atom.link, function(item) {
				return item.rel == rel;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		getLinkData : function(rel, handleas) {
			var format = (handleas != null) ? handleas : tdiapi._format;
			var link = this.getLink(rel);
			if(link == null)
				return null;
			
//			var headers = {};
//			if(link.type)
//				headers["Accept"] = link.type;
//			else
//				headers["Accept"] = "application/json";
			
			return dojo.xhrGet({
//				headers: headers,
				preventCache: true,
				handleAs: format,
				url: link.href
			});
		},
		
		getCategory : function(term) {
			var arr = dojo.filter(this.atom.category, function(item) {
				return item.term == term;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		}
		
	});
	
});
