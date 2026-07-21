dojo.provide("tdi.ServerProjects");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.RadioButton");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");		
dojo.require("dijit.Menu");
dojo.require("dijit.TitlePane");
dojo.require("dijit.Tree");
dojo.require("dijit.tree.ForestStoreModel");

dojo.require("dojoe.treetable.TreeTableAll");
dojo.require("dojoe.table.plugins.DnD");
dojo.require("dojoe.table.plugins.Menu");
dojo.require("dojoe.table.plugins.NestedSorting");
dojo.require("dojoe.table.plugins.IndirectSelection");
dojo.require("dojoe.table.plugins.Pagination");
dojo.require("dojoe.table.plugins.GridFilter");
dojo.require("dojoe.table.plugins.exporter.CSVWriter");
dojo.require("dojoe.table.plugins.Printer");
dojo.require("dojoe.table.plugins.Selector");

dojo.require("dojo.data.ItemFileWriteStore");

dojo.require("tdi.tdiapi");
dojo.require("tdi.EasyETL");
dojo.require("tdi.ServerInfo");
dojo.require("tdi.model.ServerProjectsModel");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiatom");

dojo.declare("tdi.ServerProjects",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
	// summary:
	//		A tree widget that shows installed, running and template
	//		configuration files on a server. Users of this widget must
	//		implement the extension points.
	//
	
		// Inline template string for this widget
		widgetsInTemplate: false,
		templateString : "<div dojoAttachPoint=\"TreeDiv\" style='width:100%; height:100%'></div>",
	
		// loadedSolutions: boolean
		//		Include loaded solutions
		loadedSolutions: false,

		// installedSolutions: boolean
		//		Include installed solutions
		installedSolutions: false,

		// installedTemplates: boolean
		//		Include installed templates
		installedTemplates: false,
		
		// fullView: boolean
		//		Show all columns(true) or just the name(false)
		fullView: true,
		
		// Context menu
		menu : null,
		
		// The tree store
		_store: null,
		
		// The tree store mode
		_model: null,
		
		// The table
		_table: null,
		
		// Selection mode (multiple, single, none)
		selectionMode: "single",
		
		addItem : function(item, parentInfo) {
			// summary:
			// 		Add a node to the tree
			var parent = _parentSolution;
			if(parentInfo != null) {
				parent = parentInfo;
			}
			return this._store.newItem(item, parent);
		},
		
		getItemByName : function(name) {
			var item = null;
			this._store.fetch({
				query: {
					"name":name
				},
				onComplete: function(items, req) {
					if(items != null && items.length == 1)
						item = items[0];
				}
			});
			return item;
		},
		
		_createSolutionFromTemplate : function() {
			if(this.getSelectedItem() == null)
				return null;
			
			var id = this._store.getValue(this.getSelectedItem(), "id");
			if(id != null) {
				this.createSolutionFromTemplate(id);
			}
		},
		
		createSolutionFromTemplate : function(templateId) {
			// summary:
			//		Called when user chooses create solution on a template
			// tags:
			//		extension
		},
		
		addToolbarAction : function(action) {
			// summary:
			// 		Adds a dijit.MenuItem to the Action dropdown
			//
			this._table.addToActionList(action);
		},
		
		addToolbarButton : function(button) {
			// summary:
			// 		Adds a dijit.MenuItem to the Action dropdown
			//
			this._table.addToToolbar(button, 1);
		},
		
		getSelectedRows : function() {
			// summary:
			//		Returns the currently selected rows
			return this._table.grid.selection.getSelected();
			// return this._table.getSelectedRows();
		},
		
		getSelectedItem : function() {
			if(this._selectedItem == null) {
				var arr = this.getSelectedRows();
				if(arr.length > 0)
					this._selectedItem = arr[0];
			}
			return this._selectedItem;
		},
		
		setSelectedItem : function(item) {
			this._selectedItem = item;
		},
		
		_openItem : function(item) {
			this.setSelectedItem(item);
			this.openItem(this.getSelectedConfigEntry(), this.getSelectedCIEntry());
		},
		
		openItem : function(configentry, cientry) {
			// summary:
			//		Called when user clicks an item
			// description:
			//		This function is passed the config entry and the CI entry
			//		if they exists (e.g. either one may be null but not both). 
			// tags:
			//		extension
		},
	
		_pollServerEvents: function() {
			// abort polling when widget has been destroyed
			if(this._destroyed)
				return;
			
			dojo.when(dojo.xhrGet({
				handleAs: "json",
				url: this.eventsUrl
			}),
			dojo.hitch(function(data) {
				// process event data
				dojo.forEach(data.event, function(d) {
					alert(dojo.toJson(d));
				});
				this._pollServerEvents();
			}),
			dojo.hitch(this, function(err) {
				this._pollServerEvents();
			}));
		},
		

		_createTreeMenu : function() {
			this.menu = new dijit.Menu({});
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("monitor"),
				enableOn:"selection",
				onClick:dojo.hitch(this, "_monitorConfigInstance")
			}));
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("start"),
				enableOn:"config",
				onClick:dojo.hitch(this, "_startConfigInstance")
			}));
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("stop"),
				enableOn:"cientry",
				onClick:dojo.hitch(this, "_stopConfigInstance")
			}));
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("WebCE.restart"),
				enableOn:"cientry",
				onClick:dojo.hitch(this, "_restartConfigInstance")
			}));
			
			this.menu.addChild(new dijit.MenuSeparator());
			
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("WebCE.edit"),
				enableOn:"config",
				onClick:dojo.hitch(this, "_configureConfigInstance")
			}));
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("WebCE.deleteItem"),
				enableOn:"config",
				onClick:dojo.hitch(this, "_deleteConfig")
			}));
			this.menu.addChild(new dijit.MenuItem({
				label:this.getString("unlockSolution"),
				enableOn:"locked",
				onClick:dojo.hitch(this, "_unlockConfig")
			}));
			this.menu.startup();
		},
		
		updateToolbar : function() {
			var arr = new Array();
			dojo.forEach(this.menu.getChildren(), dojo.hitch(this, function(item, index) {
				if(item.label == "") {
					arr.push(new dijit.MenuSeparator())
				} else if(item.label != null) {
					var menu = new dijit.MenuItem({
						label:item.label,
						enableOn: item.enableOn,
						onClick:item.onClick
					});
					arr.push(menu);
				}
			}));
			
			this.solutionMenu = new dijit.Menu({});
			arr = arr.reverse();
			while(arr.length > 0) {
				this.solutionMenu.addChild(arr.pop());
			}
			
			var button = new dijit.form.DropDownButton({
	            label: "Solution",
	            dropDown: this.solutionMenu
	        });
			this.addToolbarButton(button);
			
			this.enableMenuItems(null, null, false);
		},

		_enableMenuItem : function(m, config, cientry, template) {
			if(m.enableOn == null)
				m.set("disabled", false);
			else if(/.*locked.*/.test(m.enableOn) && config != null)
				m.set("disabled", config.getCategory("locked") == null);
			else if(/.*cientry.*/.test(m.enableOn) && (cientry != null || this.isSelectedItemActive()))
				m.set("disabled", false);
			else if(/.*config.*/.test(m.enableOn) && config != null)
				m.set("disabled", false);
			else if(/.*template.*/.test(m.enableOn) && template)
				m.set("disabled", false);
			else if(/.*selection.*/.test(m.enableOn) && (config != null || cientry != null))
				m.set("disabled", false);
			else
				m.set("disabled", true);
		},
		
		enableMenuItems : function(config, cientry, template) {
			dojo.forEach(this.menu.getChildren(), dojo.hitch(this, function(m) {
				this._enableMenuItem(m, config, cientry, template);
			}));
			
			dojo.forEach(this.solutionMenu.getChildren(), dojo.hitch(this, function(m) {
				this._enableMenuItem(m, config, cientry, template);
			}));
			
			dojo.forEach(this._table._toolbar.actionListWidget.dropDown.getChildren(), dojo.hitch(this, function(m) {
				this._enableMenuItem(m, config, cientry, template);
			}));
		},
		
		_configureConfigInstance : function() {
			this.configureConfigInstance(this.getSelectedConfigEntry());
		},
		
		_monitorConfigInstance : function(e) {
			var focus = this._table.grid.focus;
			var cell = null;
			if(focus != null)
				cell = focus.cell;
			this.monitorConfigInstance(this.getSelectedCIEntry(), this.getSelectedConfigEntry());
		},
		
		_startConfigInstance : function() {
			this.startConfigInstance(this.getSelectedCIEntry(), this.getSelectedConfigEntry());
		},
		
		_stopConfigInstance : function() {
			this.stopConfigInstance(this.getSelectedCIEntry(), this.getSelectedConfigEntry());
		},
		
		_restartConfigInstance : function() {
			dojo.when(tdiapi.stopConfig(this.getSelectedCIEntry()), dojo.hitch(this, function() {
				return tdiapi.startConfig(this.getSelectedConfigEntry());
			}));
		},
		
		_deleteConfig : function() {
			this.deleteConfig(this.getSelectedConfigEntry());
		},
		
		_cloneConfig : function() {
			this.cloneConfig(this.getSelectedConfigEntry());
		},
		
		_deleteTemplate : function() {
			if(this.getSelectedItem() == null)
				return null;
			
			var id = this._store.getValue(this.getSelectedItem(), "id");
			if(id != null) {
				this.deleteTemplate(id);
			}
		},
		
		_unlockConfig : function() {
			this.unlockConfig(this.getSelectedConfigEntry());
		},
		
		uploadConfig : function(template /* boolean */) {
			// summary:
			//		Called when user chooses Upload
			// template: boolean
			//		True if a template upload is requested 
			// tags:
			//		extension
			
		},
		
		deleteTemplate : function(id) {
			// summary:
			//		Called when use chooses Delete on a template entry
			// id: string
			//		The template identifier
			// tags:
			//		extension
		},
		
		cloneConfig : function(configentry) {
			// summary:
			//		Called when user chooses Clone on a config entry
			// tags:
			//		extension
		},
		
		deleteConfig : function(configentry) {
			// summary:
			//		Called when use chooses Delete on a config entry
			// tags:
			//		extension
		},
		
		isSelectedItemTemplate : function() {
			// summary:
			// 		Returns true if the selected config entry is a template
			if(this.getSelectedItem() == null)
				return null;
			else
				return this._store.getValue(this.getSelectedItem(), "type") == "template";
		},
		
		isSelectedItemActive : function() {
			// summary:
			// 		Returns true if the selected config entry is running
			if(this.getSelectedItem() == null)
				return false;
			else if(this._store.getValue(this.getSelectedItem(), "cientry"))
				return true;
			else if(this._store.getValue(this.getSelectedItem(), "active"))
				return true;
			else
				return false;
		},
		
		getSelectedConfigEntry : function() {
			// summary:
			// 		Returns the selected config entry or null if no selection exists
			// returns:
			//		tdi.tdiconfigentry for the selected item
			var item = this.getSelectedItem();
			if(item == null) {
				
			}
			if(item == null)
				return null;
			else
				return this._store.getValue(item, "entry");
		},
		
		getSelectedCIEntry : function() {
			// summary:
			// 		Returns the selected config instance entry or null if no selection exists
			// returns:
			//		tdi.tdicientry for the selected item
			//	
			if(this.getSelectedItem() == null)
				return null;
			else
				return this._store.getValue(this.getSelectedItem(), "cientry");
		},
		
		configureConfigInstance : function(configentry) {
			// summary:
			//		Called when user chooses configure...
			// tags:
			//		extension
		},
		
		monitorConfigInstance : function(cientry, configentry) {
			// summary:
			//		Called when user chooses monitor...
			// tags:
			//		extension
		},

		startConfigInstance : function(cientry, configentry) {
			// summary:
			//		Called when user chooses start config...
			// tags:
			//		extension
		},

		stopConfigInstance : function(cientry, configentry) {
			// summary:
			//		Called when user chooses stop config...
			// tags:
			//		extension
		},
		
		showAbout : function() {
			// summary:
			//		Called when user chooses About...
			// tags:
			//		extension
			
		},
		
		unlockConfig : function(config) {
			// summary:
			// 		Called when user chooses Unlock on a config
		},
		
		updateSchedules : function(data) {
			this._schedules = {};
			for(f in data) {
				if(data.hasOwnProperty(f)) {
					if(dojo.isArray(data[f]) && data[f].length > 0)
						this._schedules[f] = "true";
				}
			}
		},
		
		refresh : function() {
			this.createStoreAndModel();
			this._table.grid.setStore(this._store);
			//dojo.when(tdiapi.getActiveSchedules(), dojo.hitch(this, "updateSchedules"));			
		},
		
		createStoreAndModel: function() {
			this._store = new tdi.model.ServerProjectsModel({
				installedSolutions:this.installedSolutions,
				installedTemplates:this.installedTemplates,
				loadedSolutions:this.loadedSolutions
			});
		    this._model = new dijit.tree.ForestStoreModel({
		        store: this._store,
		        rootId: "configRoot",
		        rootLabel: "Server Projects",
		        childrenAttrs: ["items"]
		    });
		},
		
		postCreate : function() {
			this.createStoreAndModel();
			this.postCreateGrid();
		},
		
		getTree : function() {
			// summary:
			//		Returns the tree widget used to display configs
			return this._table;
		},
		
		resize : function(obj) {
			if(obj.h != null && this._table != null) {
				try {
					this._table.resize(obj);
				} catch(err) {
					console.log("Resize: " + err)
				}
			}
		},
		
		postCreateGrid : function() {
			var layout = [
			    {field:"name", name:this.getString("name"), width:"auto", formatter:dojo.hitch(this, "getCheckName")},
			    {field:"active", name:this.getString("active"), width:"80px", formatter:dojo.hitch(this, "getCheckActive")}
			];
			if(!this.fullView) {
				layout.pop();
			}
			
		    this._createTreeMenu();
		    
		    var menubars = {
		    		"rowMenu":this.menu
		    };
		    
			var toolbarOptions = {
					refreshIcon:true,
					actionMenu:true,
					configTableIcon:false, configTableMenu: false,
					exporterIcon: false, exporterMenu: false,
					printerIcon:false, printerMenu: false,
					
					refreshMenu:false,
					
					selectAllIcon:false,
					expandAllIcon: false,
					clearSortIcon: false,
					configureTreeTableIcon:false,
						
					selectAllMenu: false,
					expandAllMenu : false,
					clearSortMenu: false,
					configureTreeTableMenu:false
				};
			
			var plugins = {
					indirectSelection: true,
					nestedSorting: true,
					menus:menubars,
					dnd:false,
					//filter:filtersForTableTwo,
					printer: true,
					exporter: true,			
					pagination: false
				};
			
			var gparams = {
				treeModel: this._model,
				structure: layout,
				rowsPerPage: 20,
				plugins: plugins,
				autoWidth:false,
				selectionMode: this.selectionMode,
				filter: {
					showQuickFilter:false,
					showQuickFilterButton:false
				}
			};
			
			this._table = new dojoe.treetable.TreeTable({
				gridParams:gparams,
				toolbarOptions: toolbarOptions,
				enableResize: true,
				filter: true,
				footerVisible:false,
				width: "100%",
				height: "100%"
			}).placeAt(this.TreeDiv);
			
			this._table.addContextMenuToTreeTable("rowMenu", this.menu);
			
			this._table.startup();
			
			dojo.connect(this._table.grid, "onRowContextMenu", this, function(e) {
				this.setSelectedItem(this._table.grid.getItem(e.rowIndex));
				this.enableMenuItems(this.getSelectedConfigEntry(), this.getSelectedCIEntry(), this.isSelectedItemTemplate());
			});
			
			dojo.connect(this._table, "onRowClick", this, function(e) {
				var item = this._table.grid.getItem(e.rowIndex);
				this.setSelectedItem(item);
				this.enableMenuItems(this.getSelectedConfigEntry(), this.getSelectedCIEntry(), this.isSelectedItemTemplate());
				this._openItem(item);
			});
			
			dojo.connect(this._table.grid, "onRowDblClick", this, function(e) {
				var item = this._table.grid.getItem(e.rowIndex);
				this.setSelectedItem(item);
				this._openItem(item);
			});

			dojo.connect(this._table.grid.selection, "onChanged", dojo.hitch(this, function() {
				var firstSel = this._table.grid.selection.getFirstSelected();
				this.setSelectedItem(firstSel);
				this.enableMenuItems(this.getSelectedConfigEntry(), this.getSelectedCIEntry(), this.isSelectedItemTemplate());
				this._openItem(firstSel);
			}));
			
//			dojo.connect(this._table.grid, "onKeyDown", dojo.hitch(this, function(event) {
//				if(event.key == 1)
//					this._deleteConfig();
//			}));
			
			// -- Hook refresh so we can refresh from the server
			dojo.connect(this._table, "refresh", dojo.hitch(this, "refresh"));
			
			// Dojo 1.6 + TED does not work with initial model
			setTimeout(dojo.hitch(this, "refresh"), 100);			
		},
		
		getCheckName : function(name, index) {
			var item = this._table.getItem(index);
			var schedule = item ? this._store.getValue(item, "schedule") : false;
			if(schedule)
				return name + "<img style='width:12px; height:12px; margin-right:3px' src='images/schedule.gif'></img>";
			else
				return name;
		},
		
		getCheckActive : function(active) {
			if(active == true)
				return "<div class='activeALIcon' style='margin-left:30px; width:14px; height:14px'></div>";
			else
				return "<div></div>";
		},
		
		getCheckInstalled : function(installed) {
			if(installed)
				return "<div class='activeALIcon' style='margin-left:30px; width:14px; height:14px'></div>";
			else
				return "<div></div>";
		},
		
		resize : function() {
			if(this._table != null) {
				this._table.resize();
			}
		}
	}
);
