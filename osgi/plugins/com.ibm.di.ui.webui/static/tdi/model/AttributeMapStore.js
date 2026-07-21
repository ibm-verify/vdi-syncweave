/**
 * AttributeMapStore
 * 
 * This model requires an attribute map configuration object.
 * 
 * config  - The attribute map configuration object.
 * 
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/request",
	"dojo/store/Memory",
	"dojo/store/util/QueryResults",
	"tdi/tdiutil",
	"tdi/tdiapi"
], function(declare, lang, array, request, Memory, QueryResults, tdiutil, tdiapi) {
	
return declare( [Memory], {
	
	idProperty: "name",
	
	_validProps: {
		"name":true,
		"add":true,
		"modify":true,
		"mapsTo":true,
		"enabled":true,
		"type":true
	},
	
	put: function(item) {
		var map = this.config.getItem(item.name);
		var obj = this.cleanItem(item);
		if(!map) {
			this.config.newItem(obj);
			this.add(obj);
		} else {
			for(var f in obj) {
				if(map.config[f] != obj[f]) {
					map.setObject(f, obj[f]);
				}
			}
			this.inherited(arguments);
		}
	},
	
	remove: function(id) {
		this.config.removeItem(id);
		this.inherited(arguments);
	},
	
	cleanItem: function(item, map) {
		// summary:
		//		Returns an object with only valid properties for
		//		an attribute map item.
		var obj = {};
		for(var f in item) {
			if(f in this._validProps) {
				obj[f] = item[f];
			}
		}
		return obj;
	},
	
	constructor: function(args) {
		dojo.mixin(this, args);
		
		var config = this.config;
		var t = this;
		var arr = [];
		array.forEach(config.getNames(), function(name) {
			var item = config.getItem(name);
			var clone = {};
			for(var f in item.config) {
				clone[f] = item.config[f];
			}
			arr.push(clone);
		});
		this.setData(arr);
	}
});
});
	