define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdibatchevent",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdibatchevent", null, {
		
		// event: json object
		//		The batch event json object from the rest call
		event: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		size : function() {
			// summary:
			//		Returns number of events
			if(dojo.isArray(this.event.event))
				return this.event.event.length;
			else if(dojo.isObject(this.event.event))
				return 1;
			else
				return 0;
		},
		
		get : function(index) {
			var ix = index != null ? index : 0;
			if(dojo.isArray(this.event.event))
				return this.event.event[ix];
			else if(dojo.isObject(this.event.event) && ix == 0)
				return this.event.event;
			else
				return null;
		}
		
	});
});
