define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"tdi/config/basecfg"
], function(declare, lang, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.tdientry", null, {
		data: null,
		attributes: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			if(this.data != null && this.data.attribute != null) {
				this.buildAttributeList();
			} else {
				this.data = this.data || {};
				this.data.attribute = [];
			}
		},
		
		buildAttributeList : function() {
			this.attributes = new Object();
			dojo.forEach(this.data.attribute, dojo.hitch(this, function(attr) {
				this.attributes[attr.name] = attr;
			}));
		},
		
		getNames : function() {
			var arr = new Array();
			dojo.forEach(this.data.attribute, function(a) {
				arr.push(a.name);
			});
			return arr;
		},
		
		getAttribute : function(name) {
			return this.attributes[name];
		},
		
		removeAttribute : function(name) {
			var attr = this.getAttribute(name);
			if(attr != null) {
				this.attributes[name] = null;
			}
			this.data.attribute = dojo.filter(this.data.attribute, function(item) {
				return item.name != name;
			});
		},
		
		getAttributeValues: function(name) {
			var attr = this.getAttribute(name);
			if(attr != null) {
				var arr = dojo.map(attr.children, function(item) {
					if(lang.isObject(item))
						return item.value.value;
					else
						return item;
				});
				return arr;
			}
			return [];
		},
		
		getAttributeValue : function(name, sep) {
			var attr = this.getAttribute(name);
			var separator = sep ? sep : "\n";
			if(attr != null) {
				var arr = dojo.map(attr.children, function(item) {
					return item.value.value;
				});
				
				if(dojo.isArray(arr))
					return arr.join(separator);
				else
					return arr;
			}
			return null;
		}
		
	});
	
});
