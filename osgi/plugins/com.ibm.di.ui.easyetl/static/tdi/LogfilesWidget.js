dojo.provide("tdi.LogfilesWidget");

dojo.require("dijit.form.Button");
dojo.require("tdi.tdiapi");
dojo.require("tdi.TreeTableWidget");

dojo.declare("tdi.LogfilesWidget",
	[tdi.TreeTableWidget],
	{
	// summary:
	//		The log file widget shows log files and their contents
	//		for a specific config/assemblyline
	
		// config: tdi.tdiconfig
		// 		Users must either provide a tdi config or a config/assemblyline pair
		config: null,
		
		toolbarOptions: {
			actionMenu:false
		},
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getTreeTableLayout : function() {
  			return [
				{field:"name", name:this.getString("name"), width:"auto"}
			];
		},
		
		getTreeTableSize : function() {
			return ({height:"100%", width:"100%"})
		},
		
		onRefresh : function() {
			var store = this.getStore();
			store.fetch({
				queryOptions:{deep:true},
				onComplete:function(items, request) {
					for(i = 0; i < items.length; i++) {
						store.deleteItem(items[i]);
					}
				}
			});
			store.save();
//			this._allist = this.config.getAssemblyLineNames();
			if(this.filterAL) {
				this._allist = [this.filterAL];
				this.readLogfiles();
			}
		},
		
		setFilter : function(alname) {
			this.filterAL = alname;
			this.onRefresh();
		},
		
		readLogfiles : function() {
			if(this._allist.length < 1) {
				return;
			}
			
			// process one at a time to avoid updates to store from multiple threads
			var alname = this._allist.pop();
			
			dojo.when(tdiapi.getAssemblyLineLogs(this.config.getConfigName(), alname), dojo.hitch(this, function(data) {
				if(data.items && data.items.length > 0) {
					
					var item = null;
					this.getStore().fetch({query: {id:alname}, onComplete:function(items, request) {
						if(items.length == 1)
							item = items[0];
					}});
					
					var parent = item;
					if(parent == null && !this.filterAL) {
						parent = this.getStore().newItem({
							id:data.assemblyline,
							name:data.assemblyline
						});
					}
					
					while(data.items.length > 0) {
						var ts = data.items.pop();
						ts.parent = parent;
						ts.id = ts.name;
						ts.name= ts.name.replace(data.assemblyline + "_", "");
						ts.name = ts.name.replace(/_/g, " ");
						this.getStore().newItem(ts, {parent:parent, attribute:"items"});
					}
				}
			})).then(dojo.hitch(this, function next() {
				this.readLogfiles();
			}));
			
		},
		
		showLogFile : function(assemblyline, logfile, kbytes) {
			// summary:
			//		Override to show log file
		},
		
		onSelectionChanged : function(e) {
			var item = this.getSelectedItem();
			if(item)
				this.onRowDblClick(item, null);
		},
		
		onRowDblClick : function(item, e) {
			if(this.filterAL) {
				var logfile = this.getStore().getValue(item, "id");
				this.showLogFile(this.filterAL, logfile, 0);
			} else if(this.getStore().hasAttribute(item, "parent")) {
				var parent = this.getStore().getValue(item, "parent");
				if(this.getStore().isItem(parent)) {
					var alname = this.getStore().getValue(parent, "id");
					var logfile = this.getStore().getValue(item, "id");
					this.showLogFile(alname, logfile, 0);
				}
			}
		},
		
		postCreate : function() {
			this.inherited("postCreate", arguments);
		},
		
		destroy : function() {
			this.inherited("destroy", arguments);
		}
	
});