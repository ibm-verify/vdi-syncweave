define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.propstore", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getName : function() {
			return this.config.name;
		}

	});
	
});
