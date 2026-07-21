define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdientry",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdientry", null, {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this._createMap();
		},
		
		_createMap : function() {
			this._map = {};
			if(this.entry.attribute == null)
				return;
			
			for(var i = 0; i < this.entry.attribute.length; i++) {
				var attr = new tdi.tdiattribute({data:this.entry.attribute[i]});
				this._map[attr.getName()] = attr;
			}
		},
		
		getAttribute : function(name) {
			return this._map[name];
		},
		
		getAttributeNames : function() {
			var arr = new Array();
			for(var key in this._map)
				arr.push(key);
			return arr;
		}
	});
		
});
