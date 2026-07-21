define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdialhandle",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdialhandle", null, {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
		
	});

});
