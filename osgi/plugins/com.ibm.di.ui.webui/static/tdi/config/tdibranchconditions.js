define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/config/basecfg"
], function(declare, array, basecfg) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.tdibranchconditions", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getItems: function() {
			return this.getArray("item");
		},
		
		getConditionCount : function() {
			var arr = this.getArray("item");
			return arr ? arr.length : 0;
		},
		
		getCondition : function(index) {
			var arr = this.getArray("item");
			if(arr && arr.length > index)
				return arr[index];
			else
				return null;
		},
		
		deleteCondition: function(cond) {
			var arr = array.filter(this.getItems(), function(c) {
				return (c.leftHand != cond.leftHand ||
						c.rightHand != cond.rightHand ||
						c.operator != cond.operator ||
						c.negate != cond.negate);
			});
			this.setObject("item", arr);
		},
		
		newCondition: function() {
			var arr = this.getArray("item");
			var obj = {leftHand:"", operator:"equals", rightHand:"", negate:false};
			arr.push(obj);
			return obj;
		},
		
		getLabel : function() {
			var cond = this.getCondition(0);
			if(cond) {
				return cond.rightHand;
			} else {
				return this.getScript();
			}
		}
	});
		
});
