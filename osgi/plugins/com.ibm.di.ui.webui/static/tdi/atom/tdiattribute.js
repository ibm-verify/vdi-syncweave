define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdiattribute",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdiattribute", null, {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		size : function() {
			if(this.data.children == null)
				return 0;
			else
				return this.data.children.length;
		},
		
		getValue : function(index) {
			if(index == undefined) {
				if(this.size() == 0)
					return null;
				else
					return this.data.children[0].value.value;
			}
			return this.data.children[index].value.value;
		},
		
		setValue : function(index, value) {
			this.data.children[index].value = value;
		},
		
		getName : function() {
			return this.data.name;
		},
		
		setName : function(name) {
			this.data.name = name;
		},
		
		isProtected : function() {
			return this.data.protect;
		},
		
		setProtected : function(protect) {
			this.data.protect = protect;
		}
	});
	
});
