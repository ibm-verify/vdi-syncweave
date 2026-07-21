define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/attmapconfig"
], function(declare, basecfg, attmapconfig) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.almapconfig", [tdi.basecfg], {
		
		_map: null,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		getAttributeMap : function() {
			if(!this._map) {
				this._map = new attmapconfig({config:this.getObject("simpleConfig"), parentConfig:this});
			}
			return this._map;
		}
	
	});
		
});
