dojo.provide("tdi.model.ConfigElementsModel");

dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("tdi.model.ConfigElementsModel",
	[dojo.data.ItemFileWriteStore],
		
	{
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.data = {
				identifier: "id",
				label: "name",
				items : []
			}
			this.loadData();
		},
		
		loadData : function() {
			if(this.config != null) {
				if(this.allNames) {
					var names = this.config.getAssemblyLineNames();
					dojo.forEach(names, dojo.hitch(this, function(alname) {
						var alcfg = this.config.getAssemblyLine(alname);
						var parent = this.newItem({id:alname, name:alname});
						dojo.forEach(alcfg.getConnectorNames(), dojo.hitch(this, function(conn) {
							this.newItem({id:alname+"/"+conn.name, name:conn.name}, {parent:parent, attribute:"items"});
						}));
					}));
				} else {
					dojo.forEach(this.config.getSolutionInterface().getExposedAssemblyLines(), dojo.hitch(this, function(item) {
						var arr = item.name.split("?");
						var name = arr[0];
						var newitem = {
								id:name, 
								name:name 
						};
						this.newItem(newitem);
					}));
				}
				
				if(this.cientry != null) {
					dojo.forEach(this.config.getPropertyStoreNames(), dojo.hitch(this, function(store) {
						dojo.when(tdiapi.getPropertyStoreValues(this.cientry, store), dojo.hitch(this, function(data) {
							if(data != null) {						
								var newitem = {
										id:"ps-" + store,
										name:store + " (Properties)"
								};
								var parent = this.newItem(newitem);
								var parentInfo = {
										parent: parent,
										attribute:"items"
								};
								dojo.forEach(data.property, dojo.hitch(this, function(p) {
									this.newItem({id:"pv-" + p.name, name:p.name, storeName:store}, parentInfo);
								}));
							}
						}));
					}));
				}
			}
		}
	}
);