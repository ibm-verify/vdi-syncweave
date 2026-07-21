dojo.provide("tdi.Main");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.RadioButton");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");		
dojo.require("dijit.TitlePane");
dojo.require("dijit.Tree");
dojo.require("dijit.tree.ForestStoreModel");

dojo.require("dojo.data.ItemFileWriteStore");

dojo.require("tdi.tdiapi");
dojo.require("tdi.EasyETL");
dojo.require("tdi.ServerInfo");
dojo.require("tdi.Dashboard");
dojo.require("tdi.NlsMixin");


dojo.declare("tdi.Main",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
	// summary:
	//		The main widget for the easyetl application.
	// description:
	// 		1. After the UI elements have been created it starts the config instance poller
	// 		2. It stops the config instance poller upon destruction
	//
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/Main.html"),

		openConfigOK : function(item, data) {
			// summary:
			// 		Called when we recieve a configuration file
			// description:
			//		We check if the configuration is an EasyETL project and opens
			//		the config in the proper editor, which is EasyETL or Dashboard for
			//		normal configuration files.
			var cfg = new tdi.tdiconfig({config:data});
			var config = cfg.getConfigName();
			var entry = this.store.getValue(item, "entry");
			var id = this.store.getValue(item, "id");
			
			if(cfg.isEasyETL()) {
				var tp = new tdi.EasyETL({cfg:cfg, assemblylineName:config, configentry:entry});
				var child = new dijit.layout.ContentPane({_tdi_id:id, title:config, content:tp, closable:true, onClose:dojo.hitch(this,"confirmCloseTab")});
				this.TabContainer.addChild(child);
				this.TabContainer.selectChild(child);
			} else {
				var tp = new tdi.Dashboard({config:cfg, configentry:entry});
				var child = new dijit.layout.ContentPane({_tdi_id:id, title:config, content:tp, closable:true});
				this.TabContainer.addChild(child);
				this.TabContainer.selectChild(child);
			}
		},
		
		openConfig : function(item) {
			// summary:
			//		Sends a checkout config request and calls openConfigOK/openConfigError
			// item:
			//		The item from the tree store 
			dojo.when(tdiapi.checkOutConfig(this.store.getValue(item, "entry")),
					dojo.hitch(this, "openConfigOK", item),
					tdiapi.defaultErrHandler
			);
		},
		
		confirmCloseTab : function(container, tab) {
			// summary:
			//		Checks if the tab contents has been modified and prompts
			//		the user to confirm close.
			var arr = tab.getChildren();
			if(arr.length > 0) {
				var etl = dijit.byId(arr[0].id);
				if(etl != null && etl.cfg != null && etl.cfg.isModified()) {
					return confirm("There are changes to be saved, do you want to discard changes?");
				}
			}
			return true;
		},
		
		findOpenTab : function(item) {
			// summary:
			// 		Searches opens tabs for a tab with a title matching item's label
			// returns:
			//		Tab or null if not found
			var arr = dojo.filter(this.TabContainer.getChildren(), function(tab) {
				return tab._tdi_id == item.id[0];
			});
			
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		selectItemTab : function(item) {
			// summary:
			// 		Select the tab that is open for item
			// returns:
			//		True if tab was found and selected
			var tab = this.findOpenTab(item);
			if(tab != null) {
				this.TabContainer.selectChild(tab);
			}
			return tab != null;
		},
		
		addItemTab : function(child) {
			// summary:
			// 		Adds a widget to the tab container
			this.TabContainer.addChild(child);
			this.TabContainer.selectChild(child);
		},
		
		openItem : function(item) {
			// summary:
			//		Called when the user clicks an item in the navigator.
			// description:
			//		Open the item in the appropriated editor/widget
			
			if(this.selectItemTab(item))
				return;
			
			var config = this.store.getLabel(item);
			var type = this.store.getValue(item, "type");
			var id = this.store.getValue(item, "id");
			
			if(type == "project") {
				try {
					this.openConfig(item);
				} catch(err) {
					alert("Error proj: " + err);
				}
			} else {
				if(id == "welcome") {
					var child = new dijit.layout.ContentPane({_tdi_id:id, title:config, href:"welcome.html", closable:true});
					this.addItemTab(child);
				} else if (id == "dashboard") {
					window.open("dashboard.html", "_tdi_dashboard");
				} else if (id == "serverInfo") {
					var info = new tdi.ServerInfo({});
					var child = new dijit.layout.ContentPane({_tdi_id:id, title:config, content:info, closable:true});
					this.addItemTab(child);
				} else if (id == "addJob") {
					this.createNewJob();
				} else if (id == "login") {
				}
			}
		},
		
		createNewJob : function() {
			// summary:
			//		Creates a new EasyETL job
			// description:
			//		Sends a create config request and opens the configuration
			//		in an editor.
			var dlg = new dijit.Dialog({
				title:"Create Job",
				href:dojo.moduleUrl("tdi", "dialogs/CreateJob.html")
			});
			dlg.execute = dojo.hitch(this, function(value) {
				dojo.when(tdiapi.createETLProject(value.name, value.user),
				function() {
					this.updateTreeModel();
					alert("Job created");
				},
				function(err) {
					alert("Error creating job:\n" + err);
				});
			});
			dlg.show();
		},
			
		updateTreeModel : function() {
			dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, function(result) {
				this.store.fetch({query:{id:"publicJobs"}, onComplete:function(items) {
					item = items[0];
					}}, tdiapi.defaultErrHandler);
				for(f in result.items) {
					var config = result.items[f];
					var parentInfo = {
							parent:item,
							attribute: "children"
					}
					this.store.newItem(config, parentInfo);
				}
			}));
		},
		
		startConfigInstance : function() {
			dojo.when(tdiapi.startConfig(this.selectedConfig.entry[0]), dojo.hitch(this, function() {
				alert("Config started: " + this.selectedConfig.name[0]);
			}), tdiapi.defaultErrHandler);
		},
		
		downloadConfig : function() {
			alert("Download " + this.selectedConfig.name[0])
		},
		
		deleteConfig : function() {
			var dlg = new dijit.Dialog({
				title:"Delete " + this.selectedConfig.name[0],
				href:dojo.moduleUrl("tdi", "dialogs/DeleteJob.html")
			});
			dlg.execute = dojo.hitch(this, function(value) {
				dojo.when(tdiapi.deleteConfig(this.selectedConfig.entry[0]),
						function() {
							alert("Configuration deleted")
				}, function(err) {
					alert("Delete configuration failed: " + err);
				});
			});
			dlg.show();
		},
		
		unlockConfig : function() {
			dojo.when(tdiapi.unlockConfig(this.selectedConfig.entry[0]), function() {
				alert("Job successfully unlocked.")
			}, function(err) {
				alert("Error unlocking job: " + err)
			});
		},
		
		createTreeMenu : function() {
			var menu = new dijit.Menu({});
			menu.addChild(new dijit.MenuItem({
				label:"Start job",
				onClick:dojo.hitch(this, "startConfigInstance")
			}));
			menu.addChild(new dijit.MenuItem({
				label:"Delete job",
				onClick:dojo.hitch(this, "deleteConfig")
			}));
			menu.addChild(new dijit.MenuItem({
				label:"Download job",
				onClick:dojo.hitch(this, "downloadConfig")
			}));
			menu.addChild(new dijit.MenuItem({
				label:"Upload job",
				onClick:dojo.hitch(this, "downloadConfig")
			}));
			menu.addChild(new dijit.MenuItem({
				label:"Unlock Job",
				onClick:dojo.hitch(this, "unlockConfig")
			}));
			
			menu.bindDomNode(this.tree.domNode);
			
			dojo.connect(menu, "_openMyself", this, function(item) {
				var tn = dijit.getEnclosingWidget(item.target);
				if(tn != null)
					this.selectedConfig = tn.item;
				else
					this.selectedConfig = null;
			});
		},
		
		createTree : function() {
			this.configStoreData = {
					identifier: "id",
					label: "name",
					items : [
					         {
					        	 name:"Create configuration...",
					        	 id:"addJob",
					        	 type:"top"
					         },
					         {
					        	 name:"Installed Configurations",
					        	 id:"publicJobs",
					        	 type:"top"
					         }
					]
			};
			
			this.store = new dojo.data.ItemFileWriteStore({
				data:this.configStoreData
			});
			
		    this.model = new dijit.tree.ForestStoreModel({
		        store: this.store,
		        query: {"type": "top"},
		        rootId: "configRoot",
		        rootLabel: "Projects",
		        childrenAttrs: ["children"]
		    });
	
		    this.tree = new dijit.Tree({
		    	showRoot: false,
		        model: this.model
		    }).placeAt(this.TreeDiv);
		    
		    this.tree.getIconClass = function(/*dojo.data.Item*/ item, /*Boolean*/ opened) {
	              var cls = (!item || this.model.mayHaveChildren(item)) ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "dijitLeaf";
	              if(item.type[0] == "etlproject" || item.type[0] == "project") {
	            	  cls = "tdiConfigImage";
	            	  if(item.icon != undefined)
	            		  cls = item.icon;
	              }
	              return cls;
		    }
		    
		    dojo.connect(this.tree, "onClick", this, "openItem");
		    
		    this.createTreeMenu();
		},
		
		updateRunStatus : function(data) {
			var arr = new Array();
			if(dojo.isArray(data.entry))
				arr = data.entry;
			else if(dojo.isObject(data.entry))
				arr.push(data.entry);
			
			var runStatus = {};
			dojo.forEach(arr, function(item) {
				runStatus[item.title.value] = "1";
			});
			

			var parent = null;
			this.store.fetch({query:{id:"publicJobs"}, onComplete:function(items) {
				parent = items[0];
				}}, tdiapi.defaultErrHandler);

			if(parent != null) {
				this.model.getChildren(parent, dojo.hitch(this, function(item) {
					for(var f in item) {
						if(runStatus[item[f].name[0]] == "1") {
							this.store.setValue(item[f], "icon", "startIcon");
						} else {
							this.store.setValue(item[f], "icon", []);
						}
					}
				}));
			}
			
		},
		
		destroy : function() {
			tdiapi.stopConfigInstanceStatusPoller();
			this.inherited(arguments);
		},
		
		postCreate : function() {
			this.createTree();
			this.updateTreeModel();
			tdiapi.startConfigInstanceStatusPoller();
			tdiapi.subscribeConfigStatus(dojo.hitch(this, "updateRunStatus"));
		}
	}
);
