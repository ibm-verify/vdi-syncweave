define([
	"dojo/_base/declare",
	"tdi/atom/tdiatom"
], function(declare, tdiatom) {
return declare("tdi.tdifeed",
	[tdiatom],
	{
// 	dojo.declare("tdi.tdifeed", null, {
		
		feed: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getId : function() {
			return this.feed.id;
		},
		
		getEntry : function(term) {
			// summary:
			//		Traverse the feed's entries that has a category
			//		matching term.
			// term: String
			//		The term to match
			// return:
			//		A tdiatom with the entry data
			var entries = [];
			if(dojo.isArray(this.feed.entry))
				entries = this.feed.entry;
			else if(dojo.isObject(this.feed.entry))
				entries.push(this.feed.entry);
			
			var arr = dojo.filter(entries, function(e) {
				return dojo.some(e.category, function(c) {
					return c.term == term;
				})
			});
			if(arr.length > 0)
				return new tdiatom({atom:arr[arr.length-1]});
			else
				return null;
		},
		
		getEntries : function(pfilter) {
			// summary:
			//		Returns all feed entries or those matched by pfilter
			// pfilter: function(entry:tdiatom)
			//		Returns true if entry is to be included in the result
			// return:
			//		Array of entries in the feed
			if(pfilter == null)
				return this.feed.entry;
			else
				return dojo.filter(this.feed.entry, pfilter);
		}
	});
	
});
