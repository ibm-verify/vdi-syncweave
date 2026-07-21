define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.tdisolution", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getExposedAssemblyLines : function() {
			return this.getArray("al");
		},
		
		setExposedAssemblyLines : function(/*array*/ arr) {
			this.setObject("al", arr);
		},
		
		getExposedAssemblyLineNames : function() {
			// summary:
			//		Returns an array with the exposed al names
			var arr = new Array();
			dojo.forEach(this.getExposedAssemblyLines(), function(obj) {
				arr.push(obj.name);
			});
			return arr;
		},
		
		getSolutionName : function() {
			return this.getObject("solutionName");
		},
		
		setSolutionName : function(name) {
			this.setObject("solutionName", name);
		},
		
		getExposedProperties : function() {
			return this.getArray("property");
		},
		
		getExposedProperty : function(store, name) {
			var arr = dojo.filter(this.getExposedProperties(), function(prop) {
				if(store != null)
					return prop.name == name && prop.storeName == store;
				else
					return prop.name == name;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		}
	});
	
});
