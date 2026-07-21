define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdicientry",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdicientry", [tdi.tdiatom], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getConfig : function() {
			return this.getLinkData("config")
		}
		
	});
	
});
