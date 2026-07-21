define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.connectionconfig", [tdi.basecfg], {
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getInheritedObj : function() {
			var ref = this.getInheritFrom();
			if(ref == "[parent]") {
				var conn = null;
				if(this.getParent() != null) {
					conn = this.getParent().getInheritedObj();
				}
				if(conn && conn.getConnectionConfig) {
					return conn.getConnectionConfig();
				}
			}
			return this.inherited(arguments);
		}
	});
	
});
