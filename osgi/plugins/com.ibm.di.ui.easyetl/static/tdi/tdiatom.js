/*
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     1.3, 5/9/11
 * @owner       
 * @history
 */

if (!dojo._hasResource["tdi.tdiatom"]) {
	dojo._hasResource["tdi.tdiatom"] = true;
	
	
	dojo.provide("tdi.tdifeed");
	dojo.declare("tdi.tdifeed", null, {
		
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
			//		A tdi.tdiatom with the entry data
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
				return new tdi.tdiatom({atom:arr[arr.length-1]});
			else
				return null;
		},
		
		getEntries : function(pfilter) {
			// summary:
			//		Returns all feed entries or those matched by pfilter
			// pfilter: function(entry:tdi.tdiatom)
			//		Returns true if entry is to be included in the result
			// return:
			//		Array of entries in the feed
			if(pfilter == null)
				return this.feed.entry;
			else
				return dojo.filter(this.feed.entry, pfilter);
		}
	});
	
	dojo.provide("tdi.tdiatom");
	dojo.declare("tdi.tdiatom", null, {
		
		atom : null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getId : function() {
			return this.atom.id;
		},
		
		getUpdated : function() {
			if(this.atom)
				return this.atom.updated;
			else
				return "";
		},
		
		getTitle : function() {
			if(dojo.isObject(this.atom.title))
				return this.atom.title.value;
			else
				return null;
		},
		
		getLink : function(rel) {
			var arr = dojo.filter(this.atom.link, function(item) {
				return item.rel == rel;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		getLinkData : function(rel, handleas) {
			var format = (handleas != null) ? handleas : tdiapi._format;
			var link = this.getLink(rel);
			if(link == null)
				return null;
			
			return dojo.xhrGet({
				handleAs: format,
				preventCache: true,
				url: link.href
			});
		},
		
		getCategory : function(term) {
			var arr = dojo.filter(this.atom.category, function(item) {
				return item.term == term;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		}
		
	});
	
	dojo.provide("tdi.tdicientry");	
	dojo.declare("tdi.tdicientry", [tdi.tdiatom], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getConfig : function() {
			return this.getLinkData("config")
		}
		
	});
	
	dojo.provide("tdi.tdialentry");
	dojo.declare("tdi.tdialentry", [tdi.tdiatom], {
		
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
	
	dojo.provide("tdi.tdiconfigentry");
	dojo.declare("tdi.tdiconfigentry", [tdi.tdiatom], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
		
	});

	dojo.provide("tdi.tdialhandle");
	dojo.declare("tdi.tdialhandle", null, {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
		
	});

	dojo.provide("tdi.tdientry");
	dojo.declare("tdi.tdientry", null, {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this._createMap();
		},
		
		_createMap : function() {
			this._map = {};
			if(this.entry.attribute == null)
				return;
			
			for(var i = 0; i < this.entry.attribute.length; i++) {
				var attr = new tdi.tdiattribute({data:this.entry.attribute[i]});
				this._map[attr.getName()] = attr;
			}
		},
		
		getAttribute : function(name) {
			return this._map[name];
		},
		
		getAttributeNames : function() {
			var arr = new Array();
			for(var key in this._map)
				arr.push(key);
			return key;
		}
	});
		
	dojo.provide("tdi.tdiattribute");
	dojo.declare("tdi.tdiattribute", null, {
		
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
	
	dojo.provide("tdi.tdibatchevent");
	dojo.declare("tdi.tdibatchevent", null, {
		
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
}
