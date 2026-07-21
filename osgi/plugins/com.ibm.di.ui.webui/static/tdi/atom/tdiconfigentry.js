define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdiconfigentry",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdiconfigentry", [tdi.tdiatom], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
		
	});

});
