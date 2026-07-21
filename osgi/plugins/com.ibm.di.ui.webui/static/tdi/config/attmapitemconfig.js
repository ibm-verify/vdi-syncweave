define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, tdibasecfg) {
return declare(
	[ tdibasecfg ],
	{
		constructor: function(args) {
			declare.safeMixin(this, args);
		},	
		
		isSimple : function() {
			return "Simple" == this.config.type;
		},
		
		isAdvanced : function() {
			return "Advanced" == this.config.type;
		},
		
		getSimple : function() {
			return this.getMapsTo();
		},
		
		setSimple : function(simple) {
			this.setType("Simple");
			this.setMapsTo(simple);
		},
		
		getAdvanced : function() {
			return this.getMapsTo();
		},
		
		setAdvanced : function(script) {
			this.setType("Advanced");
			this.setMapsTo(script);
		},
		
		getMapsTo : function() {
			return this.config.mapsTo;
		},
		
		setMapsTo : function(str) {
			this.setObject("mapsTo", str);
			this.setEnabled(true);
		},
		
		setType : function(type) {
			this.setObject("type", type);
		},
		
		setScript: function(str) {
			this.setAdvanced(str);
		},
		
		getScript: function() {
			return this.getAdvanced();
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			return arr.sort();
		}

	});
	
});
