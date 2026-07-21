define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdialentry",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdialentry", [tdi.tdiatom], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getConfig : function() {
			return this.getLinkData("config")
		},
		
		getLog : function() {
			return this.getLinkData("log", "text")
		},
		
		getResult : function() {
			return this.getLinkData("result")
		},
		
		getStatus : function() {
			return this.getLinkData("status")
		},
		
		getListener : function() {
			return this.getLinkData("listener")
		}
	});
	
});
