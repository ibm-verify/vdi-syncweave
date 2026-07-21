define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.attributeloop", [tdi.basecfg], {
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		getWorkAttributeName : function() {
			return this.getObject("collectionAttribute");
		},
		
		getLoopAttributeName : function() {
			return this.getObject("assignAttribute");
		},
		
		setWorkAttributeName : function(name) {
			this.setObject("collectionAttribute", name);
		},
		
		setLoopAttributeName : function(name) {
			this.setObject("assignAttribute", name);
		}
	});
		
});
