define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/attmapconfig",
	"tdi/config/schemaconfig"
], function(declare, tdibasecfg, tdiattmapconfig, tdischemaconfig) {
return declare("tdi.config.operation",
	[tdibasecfg],
	{
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this._maps = new Object();
		},
		
		getMap : function(map, name) {
			var arr = dojo.filter(map, function(item) {
				return item.name == name;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		getAttributeMap : function(input) {
			var key = "Attributemap_" + input;
			if(this._maps[key] == null) {
				var map = this.getObject(this.getKey("attributeMap"));
				this._maps[key] = new tdiattmapconfig({config:this.getMap(map, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getSchema : function(input) {
			var key = "Schema_" + input;
			if(this._maps[key] == null) {
				var schema = this.getObject(this.getKey("schema"));
				this._maps[key] = new tdischemaconfig({config:this.getMap(schema, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		}
		
	});

});
