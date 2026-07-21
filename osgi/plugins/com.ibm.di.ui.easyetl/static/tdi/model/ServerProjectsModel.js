dojo.provide("tdi.model.ServerProjectsModel");

dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("tdi.model.ServerProjectsModel",
	[dojo.data.ItemFileWriteStore],
		
	{
		installedSolutions: false,
		installedTemplates: false,
		loadedSolutions: false,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			
			this.data = {
				identifier: "id",
				label: "name",
				items : []
			}
			
			this.loadData();
			
		},
		
		_updateTemplates : function(result) {
			dojo.forEach(result.ConfigTemplate, dojo.hitch(this, function(obj) {
				this.addItem({name:obj.Name, id:obj.Name, type:"template", active:false, installed:false});
			}));
		},
		
		_updateTree : function(parent, result) {
			// summary:
			//		Called when we get the server projects list
			//		from the server api.
			
			try {
				this.fetch({
					onComplete:function(items, request) {
						for(i = 0; i < items.length; i++) {
							this.deleteItem(items[i]);
						}
					}
				});
				this.save();
			} catch(err) {
				console.log("updateTree.removeallitems: " + err);
			}
			
			
			for(f in result.items) {
				var item = result.items[f];
				item.active = false;
				item.installed = true;
				if(item.entry.getCategory("directory") != null) {
//					var myparent = parent != null ? parent.parent : null;
//					item.items = [];
//					item.type = "directory";
//					item.path = myparent != null ? myparent.path + "/" + item.name : item.name;
//					var parentInfo = {
//						parent: this.newItem(item, {parent:myparent, attribute:"items"}),
//						attribute: "items"
//					}
//					dojo.when(tdiapi.getConfigObjects(item.path), dojo.hitch(this, "_updateTree", parentInfo))
					
				} else {
					this.addItem(item, parent);
				}
			}
			
		},

		_updateRunStatusEvent: function(event) {
			if(event.type == "di.ci.stop") {
				var storeItem = null;
				this.fetch({
					query:{name:event.id},
					onItem:dojo.hitch(this, function(item) {
						this.setValue(item, "active", false);
						this.setValue(item, "cientry", []);
						storeItem = item;
					})
				});
				
				// When a temporary config stops we remove it from the tree
				if(storeItem && this.getValue(storeItem, "type") == "temporary") {
					this.deleteItem(storeItem);
					this.save();
				}
			}
			
			if(event.type == "di.ci.start") {
				this.fetch({
					query:{name:event.id},
					onItem:dojo.hitch(this, function(item) {
						this.setValue(item, "active", true);
					})
				})
				dojo.when(tdiapi.getConfigInstances(), dojo.hitch(this, "_updateRunStatus"), tdiapi.defaultErrHandler);
			}
			
			if(event.type == "di.ci.file.updated") {
				
			}
		},
		
		_updateRunStatus: function(result) {
			// summary:
			//		Called when we get running config instances status
			//		from the ConfigInstanceStatusPoller api.
			
			var arr = new Array();
			if(dojo.isArray(result.entry)) {
				arr = result.entry;
			} else if(dojo.isObject(result.entry)) {
				arr.push(result.entry);
			}
			
			var present = new Object();
			dojo.forEach(result.entry, dojo.hitch(this, function(item) {
				present[item.title.value] = item;
			}));
			
			var tempRE = /^TDIDashboard_TEMP/;
			var tempRE2 = /^Temp_/;
			
			this.fetch({
				queryOptions: {
					deep:true
				},
				onItem:dojo.hitch(this, function(item,request) {
					var name = this.getValue(item, "name");
					if(tempRE.test(name) || tempRE2.test(name) ) {
						present[name] = null;
					} else {
						if(present[name] != null) {
							this.setValue(item, "active", true);
							this.setValue(item, "icon", "activeALIcon");
							this.setValue(item, "cientry", new tdi.tdicientry({atom:present[name]}));
						} else {
							try {
								this.setValue(item, "active", false);
								this.setValue(item, "icon", []);
								this.setValue(item, "cientry", []);
								if(this.getValue(item, "type") == "temporary") {
									this.deleteItem(item);
								}
							} catch(err) {
								console.log("Error updating item: " + this.getValue(item, "name") + "; " + err);
							}
						}
						present[name] = null;
					}
				})
			});
			
			// -- Add new items
			for(var f in present) {
				var item = present[f];
				if(item != null && !tempRE.test(item.title.value)) {
					var newitem = {
							id: item.id,
							name: item.title.value,
							icon: "activeALIcon",
							type: "temporary",
							active: true,
							installed: false,
							cientry: new tdi.tdicientry({atom:item})
					}
					try {
						this.addItem(newitem, parent);
					} catch(err) {
						console.log("error adding " + item.title.value + "; " + err)
					}
				}
			}
			
			this.save();
			
			dojo.when(tdiapi.getActiveSchedules(), dojo.hitch(this, "updateSchedules"));			
		},
		
		updateSchedules : function(data) {
			var scheds = {};
			for(f in data) {
				if(data.hasOwnProperty(f)) {
					if(dojo.isArray(data[f]) && data[f].length > 0) {
						scheds[f] = true;
					}
				}
			}
			this.fetch({onItem:function(item, request) {
				var name = request.store.getValue(item, "name");
				var s = request.store.getValue(item, "schedule");
				if(scheds[name]) {
					if(!s)
						request.store.setValue(item, "schedule", "true");
				} else {
					if(s)
						request.store.setValue(item, "schedule", []);
				}
			}});

			this.save();
		},
		
		loadData : function() {
			// summary:
			//		Requests data to populate the tree
		    if(this.installedSolutions) {
		    	dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, function(parent, result) {
		    		this._updateTree(parent, result);
				    if(this.loadedSolutions) {
				    	//this._subcribeHandler = this._subscribeHandler || tdiapi.subscribeConfigStatus(dojo.hitch(this, "_updateRunStatus"));
						// Subscribe to server events for quicker updates to run status
						this._eventsHandler = dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_updateRunStatusEvent"));
						dojo.when(tdiapi.getConfigInstances(), dojo.hitch(this, "_updateRunStatus"), tdiapi.defaultErrHandler);
				    }
		    	}, null));
		    	
		    } else if(this.loadedSolutions) {
		    	this._subcribeHandler = this._subscribeHandler || tdiapi.subscribeConfigStatus(dojo.hitch(this, "_updateRunStatus"));
		    }
		    
		    if(this.installedTemplates) {
		    	dojo.when(tdiapi.getWebCeTemplates(), dojo.hitch(this, "_updateTemplates"));
		    }
		},
		
		addItem : function(item, parentInfo) {
			// summary:
			// 		Add a node to the tree
			return this.newItem(item, parentInfo);
		}
	}
);