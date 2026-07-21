// summary:
//		The log file widget shows log files and their contents
//		for a specific config/assemblyline
define(
[
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/TableWidget",
	"tdi/NlsMixin"
], function(declare, array, lang, html, tdiapi, tdiconstants, TableWidget, NlsMixin) {
	
return declare(
	[TableWidget],
	{
		// config: tdi.tdiconfig
		// 		Users must either provide a tdi config or a config/assemblyline pair
		config: null,
		
		setLogViewer : function(logviewer) {
			
		},
		
		flagErrors : function(str, index) {
			var item = this.getItem(index);
			if(item.exitCode && item.exitCode[0] > 0)
				return "<span style='color:red'>" + str + "</span>";
			else
				return str;
		},
		
		onRowDblClick : function(item) {
			
		},
		
		selectTarget : function(alname, config) {
			this.filterAL = alname;
			if(config)
				this.config = config;
			this.onRefresh();
		},
		
		onRefresh : function() {
			
//			this.getStore().close();
//			this.createTreeModel();
//			this.setModel(this.getModel());
			
			// Deleting is extremely slow
//			var store = this.getStore();
//			store.fetch({
//				queryOptions:{deep:true},
//				onComplete:function(items, request) {
//					for(i = 0; i < items.length; i++) {
//						store.deleteItem(items[i]);
//					}
//				}
//			});
//			store.save();
			
			if(this.filterAL) {
				this._allist = [this.filterAL];
				this.readTombstones();
			}
//			else
//				this._allist = this.config.getAssemblyLineNames();
		},
		
		onRefreshComplete : function() {
			// summary:
			//		Called when the store has been refreshed
		},
		
		readTombstones : function() {
			if(this._allist.length < 1) {
				this.onRefreshComplete();
				this._allist = null;
				return;
			}
			
			var alname = this._allist.pop();
			
			dojo.when(tdiapi.getTombstones(this.config, alname), dojo.hitch(this, function(alname, data) {
				
				if(data.tombstone == null || data.tombstone.length == 0)
					return;
				
				var item = null;
				this.getStore().fetch({query: {assemblyline:alname}, onComplete:function(items, request) {
					if(items.length == 1)
						item = items[0];
				}});
				
				var parent = item;
				if(parent == null && !this.filterAL) {
					var ts = data.tombstone.pop();
					ts.assemblyline = alname;
					ts.cycles = ts.statistics.get;
					ts.id = ts.guuid;
					parent = this.getStore().newItem(ts);
				}
				
				while(data.tombstone.length > 0) {
					var ts = data.tombstone.pop();
					item = null;
					this.getStore().fetch({queryOptions:{deep:true}, query: {id:ts.guuid}, onComplete:function(items, request) {
						if(items.length == 1)
							item = items[0];
					}});
					
					if(item == null) {
						ts.parent = parent;
						ts.id = ts.guuid;
						ts.cycles = ts.statistics.get;
						this.getStore().newItem(ts, {parent:parent, attribute:"items"});
					}
				}
			}, alname)).then(dojo.hitch(this, function next() {
				this.readTombstones();
			}));
			
		},
		
		_serverEvent : function(event) {
			if(event.ciId && event.ciId == this.config) {
				if(event.type == "di.al.stop") {
					if(!this._allist)
						this.onRefresh();
				}
			}
		},
		
		postCreate : function() {
			this.inherited("postCreate", arguments);
			this._sevents = dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_serverEvent"));

			var nls = new NlsMixin({});
			
			this.structure = [
				{field:"started", name:nls.getString("started"), width:"auto", formatter:dojo.hitch(this, "flagErrors")},
				{field:"terminated", name:nls.getString("stopped"), width:"auto"},
				{field:"exitCode", name:nls.getString("exitStatus"), width:"auto"},
				{field:"userMessage", name:nls.getString("userMessage"), width:"auto"},
				{field:"cycles", name:nls.getString("cycles")}
			];
			this.setData(null);
		},
		
		destroy : function() {
			if(this._sevents) {
				dojo.unsubscribe(this._sevents);
			}
			this.inherited("destroy", arguments);
		}
	});
});
