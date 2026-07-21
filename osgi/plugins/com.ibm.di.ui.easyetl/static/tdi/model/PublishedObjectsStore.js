dojo.provide("tdi.model.PublishedObjectsStore");

dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.model.PublishedObjectsStore",
	[dojo.data.ItemFileWriteStore, tdi.NlsMixin],
	{
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.data = {
					identifier: "id",
					label: "displayname",
					items : this.items || []
				}
			
			this.loadData();
		},
		
		getResourcesItem : function() {
			if(!this._resourcesItem) {
				this._resourcesItem = this.newItem({
					id:"%%resources%%",
					name:this.getString("miadmin.foldernames.Resources"),
					displayname:this.getString("miadmin.foldernames.Resources")
				});
			}
			return this._resourcesItem;
		},
		
		loadProperties: function() {
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
			
		},
		
		loadAssemblyLines: function() {
			dojo.forEach(this.config.getAssemblyLineNames(), dojo.hitch(this, function(al) {
				var parent = this.newItem({
					id:al,
					name:al,
					displayname:al,
					config:this.config,
					type:"assemblyline"
				});
				var alconfig = this.config.getAssemblyLine(al);
				if(!alconfig.isEasyETL() && !alconfig.isConfigReport()) {
					this.addALComponents(alconfig.getEntryFeedComponent(), parent);
					this.addALComponents(alconfig.getDataFlowComponent(), parent);
				}
			}));
			
			this.loadConnectors();
			this.loadParsers();
			this.loadScripts();
//			this.loadSchedules();
		},
		
		loadConnectors: function() {
			var lib = this.getString("miadmin.foldernames.Connectors");
			var parent = null;
			dojo.forEach(this.config.getConnectorNames(), dojo.hitch(this, function(c) {
				if(parent == null) {
					parent = this.newItem({
						id:lib,
						name:lib,
						displayname:lib
					}, {parent:this.getResourcesItem(), attribute:"items"});
				}
				var cfg = this.config.getConnector(c);
				this.newItem({
						id:lib + "." + c,
						type:cfg.getSubType(),
						mode:cfg.getMode ? cfg.getMode() : "no mode",
						config:cfg,
						displayname:c,
						name:c
					},
					{parent:parent, attribute:"items"}
				);
			}));
		},
		
		loadScripts: function() {
			var lib = this.getString("miadmin.foldernames.Scripts");
			var parent = null;
			dojo.forEach(this.config.getScriptNames(), dojo.hitch(this, function(c) {
				if(parent == null) {
					parent = this.newItem({
						id:"%%lib%%"+lib,
						name:lib,
						displayname:lib
					}, {parent:this.getResourcesItem(), attribute:"items"});
				}
				var cfg = this.config.getScript(c);
				this.newItem({
						id:lib + "." + c,
						type:"script",
						config:cfg,
						displayname:c,
						name:c
					},
					{parent:parent, attribute:"items"}
				);
			}));
		},
		
		loadParsers: function() {
			var lib = this.getString("miadmin.foldernames.Parsers");
			var parent = null;
			dojo.forEach(this.config.getParserNames(), dojo.hitch(this, function(c) {
				if(parent == null) {
					parent = this.newItem({
						id:lib,
						name:lib,
						displayname:lib
					}, {parent:this.getResourcesItem(), attribute:"items"});
				}

				var cfg = this.config.getParser(c);
				this.newItem({
						id:lib + "." + c,
						type:"parser",
						config:cfg,
						displayname:c,
						name:c
					},
					{parent:parent, attribute:"items"}
				);
			}));
		},
				
		loadSchedules: function() {
			var lib = "Schedules Library";
			var parent = this.newItem({
				id:lib,
				name:lib,
				displayname:lib
			});
			dojo.forEach(this.config.getScheduleNames(), dojo.hitch(this, function(c) {
				var cfg = this.config.getSchedule(c);
				this.newItem({
						id:lib + "." + c,
						type:"schedule",
						config:cfg,
						displayname:c,
						name:c
					},
					{parent:parent, attribute:"items"}
				);
			}));
		},
				
		addALComponents : function(container, parent) {
			var prefix = parent ? parent.id[0] : "";
			dojo.forEach(container.getComponentNames(), dojo.hitch(this, function(c) {
				var cfg = container.getComponent(c);
				var item = this.newItem({
					id:prefix + "." + c,
					type:cfg.getSubType(),
					mode:cfg.getMode ? cfg.getMode() : "no mode",
					config:cfg,
					//assemblyline:al,
					displayname:c,
					name:c},
				{parent:parent, attribute:"items"});
				if(cfg.isContainer()) {
					this.addALComponents(cfg, item);
				} else if(cfg.isConnector()) {
					this.addActiveHooks(cfg, item);
				}
			}));
		},
		
		addActiveHooks : function(cfg, parent) {
			var prefix = parent ? parent.id[0] : "";
			var thisObj = this;
			dojo.forEach(cfg.getHookNames(), function(str) {
				var hook = cfg.getHook(str);
				if(hook && hook.isEnabled()) {
					thisObj.newItem({
						id:prefix + "." + str,
						type:"script",
						config:hook,
						displayname:thisObj.getString("Hook." + str),
						name:str
					}, {parent:parent, attribute:"items"});
				}
			});
		},
		
		loadAssemblyLinesOld: function() {
			dojo.forEach(this.config.getAssemblyLineNames(), dojo.hitch(this, function(al) {
				var parent = this.newItem({
					id:al,
					name:al,
					displayname:al,
					type:"assemblyline"
				});
				var alconfig = this.config.getAssemblyLine(al);
				if(!alconfig.isEasyETL()) {
					dojo.forEach(alconfig.getConnectorNames(), dojo.hitch(this, function(c) {
						var cfg = alconfig.getConnector(c.name);
						this.newItem({
							id:al + "." + c.name,
							type:"connector",
							mode:cfg.getMode(),
							assemblyline:al,
							displayname:c.name,
							name:c.name},
						{parent:parent, attribute:"items"});
					}));
				}
			}));
		},
		
		getAllItems : function() {
			// summary:
			//		Returns a flattened view (array) of all exposed items 
			var arr = new Array();
			dojo.forEach(this.config.getAssemblyLineNames(), dojo.hitch(this, function(al) {
				arr.push({
					id:al,
					name:al,
					type:"assemblyline"
				});
				var alconfig = this.config.getAssemblyLine(al);
				dojo.forEach(alconfig.getConnectorNames(), dojo.hitch(this, function(c) {
					var cfg = alconfig.getConnector(c.name);
					arr.push({
						id:al + "." + c.name,
						type:"connector",
						mode:cfg.getMode(),
						assemblyline:al,
						name:c.name}
					);
				}));
			}));
			return arr;
		},
		
		loadData: function() {
			// summary:
			//		Reads the exposed assemblylines array and builds a structured
			//		data store based on that list. The list includes encoded options
			//		and also refers to property sets (Category field in the exposed props).
			//
			
			this.dataArray = new Array();
			
			var solution = this.config.getSolutionInterface();
			var list = solution.getExposedAssemblyLines();
			if(list == null) 
				list = [];

			// Properties grouped on Category
			this.groups = {};
			dojo.forEach(solution.getExposedProperties(), dojo.hitch(this, function(item) {
				if(this.groups[item.category] == null) {
					this.groups[item.category] = [];
				}
				this.groups[item.category].push(item);
			}));

			// Parse each line in exposed assemblylines
			dojo.forEach(list, dojo.hitch(this, function(str) {
				this.parseItem(str);
			}));
		},
		
		numberItems: function() {
			return this.dataArray != null ? this.dataArray.length : 0;
		},
		
		getItems: function() {
			return this.dataArray;
		},
		
		parseItem: function(str) {
			// summary:
			//		Parses an exposed assemblyline string
			//
			// description:
			//		AssemblyLine[/{options....}]
			//		AssemblyLine/Connector[/{options....}]
			//		@PropertySet
			//
			//	TODO: Options are JSON encoded at the moment to avoid excessive changes in TDI
			//
			var id = str.name;
			var index = str.name.indexOf("!");
			var opts = null;
			if(index != -1) {
				id = str.name.substring(0,index);
				opts = str.name.substring(index+1);
			}
			
			var arr = id.split(":");
			var name = arr[0];
			var conn = null;
			if(arr.length > 1)
				conn = arr[1];
			
			var item = {};
			if(opts != null) {
				item.options = dojo.fromJson(opts);
			} else {
				item.options = {};
			}
			item.id = id;
			item.name = name;
			item.orgvalue = str;
			item.type = "assemblyline";
			
			if(id.charAt(0) == '@') {
				item.name = id.substring(1);
				item.type = "properties";
				item.properties = this.groups[item.name];
				if(item.properties == null)
					item.properties = [];
			} else if(conn != null) {
				item.assemblyline = item.name;
				item.type = "connector";
				item.name = conn;
				item.config = this.config.getAssemblyLine(item.assemblyline).getComponentByName(item.name);
				if(item.config.getMode)
					item.mode = item.config.getMode();
			} else {
				item.config = this.config.getAssemblyLine(item.name);
			}

			var displayname = item.name;
			if(item.options.DisplayName && item.options.DisplayName.length > 0)
				displayname = item.options.DisplayName;
			
			item.displayname = displayname;
			
			this.dataArray.push(item);
			this.newItem(item);
		}
		
	}
);
