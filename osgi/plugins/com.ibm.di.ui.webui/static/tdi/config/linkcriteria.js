define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, basecfg) {
return declare(
	[basecfg],
	{
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this.items = this.getArray("item");
		},

		size : function() {
			return this.items.length;
		},
		
		hasLinkAttribute : function(attribute) {
			return dojo.some(this.items, function(item) {
				return item.attribute == attribute;
			});
		},
		
		isAdvanced: function() {
			return this.getObject("advanced");
		},
		
		setAdvanced: function(on) {
			return this.setObject("advanced", on);
		},
		
		isMatchAny: function() {
			return this.getObject("matchAny");
		},
		
		setMatchAny: function(on) {
			return this.setObject("matchAny", on);
		},
		
		getLinkAttributes : function() {
			// summary:
			//		Returns the attribute names for which there are
			//		link criteria set.
			// returns:
			//		An array of attribute names or null if link is not set
			var arr = [];
			dojo.forEach(this.items, function(item) {
				if(dojo.indexOf(arr, item.attribute) == -1) {
					arr.push(item.attribute);
				}
			});
			return arr;
		},
		
		getCriteriaList: function() {
			// summary:
			//		Returns the array of criteria items. Any modifications
			//		to the array should be accompanied by a call to setModified to
			//		notify other objects of changes.
			return this.items;
		},
		
		getCriteriaFor : function(attribute) {
			// summary:
			//		Returns the LinkCriteria definition for a specific attribute
			var arr = dojo.filter(this.items, function(item) {
				return item.attribute == attribute;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		newCriteria: function() {
			var item = {
				attribute:"",
				operator:"",
				value:"",
				key:new Date().getTime()
			};
			this.items.push(item);
			this.setModified(true);
			return item;
		},
		
		setCriteriaFor : function(attribute, oper, value) {
			// summary:
			//		Sets the LinkCriteria definition for a specific attribute
			//		This function overwrites existing link criteria.
			var crit = this.getCriteriaFor(attribute);
			if(crit == null) {
				this.items.push({
					attribute:attribute,
					operator:oper,
					value:value,
					key:new Date().getTime()
				});
			} else {
				crit.oper = oper;
				crit.value = value;
			}
			this.setModified(true);
		}
		
	});
	
});
