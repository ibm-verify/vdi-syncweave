/*
/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
dojo.provide("tdi.ConfigEditor");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");
dojo.require("dijit.Menu");
dojo.require("dijit.Toolbar");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.StackContainer");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.Textarea");


//TED Widgets 1.1
dojo.require("dojoe.treetable.TreeTable"); 
dojo.require("dojoe.table.TableAll"); 
dojo.require("dojoe.table.plugins.DnD");
dojo.require("dojoe.table.plugins.Menu");
dojo.require("dojoe.table.plugins.NestedSorting");
dojo.require("dojoe.table.plugins.IndirectSelection");
dojo.require("dojoe.table.plugins.Pagination");
dojo.require("dojoe.table.plugins.GridFilter");
dojo.require("dojoe.table.plugins.exporter.CSVWriter");
dojo.require("dojoe.table.plugins.Printer");
dojo.require("dojoe.table.plugins.Selector");
dojo.require("dojoe.slidingpane.SlidingPane");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");
dojo.require("tdi.ConnectorEditor");
dojo.require("tdi.ConfigEditorProps");
dojo.require("tdi.ConfigElements");
dojo.require("tdi.EasyETL");
dojo.require("tdi.PropertyEditor");
dojo.require("tdi.ALSchedule");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.model.PublishedObjectsStore");
dojo.require("tdi.RunAssemblyLineInstance");
dojo.require("tdi.TextEditor");
dojo.require("tdi.JavascriptEditor");
dojo.require("tdi.BranchConditions");
dojo.require("tdi.AttributeLoop");
dojo.require("tdi.EditSolutionSettings");
dojo.require("tdi.AttributeMap");

dojo.declare("tdi.ConfigEditor", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the "public" information in a configuration
	//		file. The developer of the config has tagged properties, connectors
	//		and assemblylines that this widget will expose editing capabilities for.

	// Widget/Templated
	//templatePath: dojo.moduleUrl("tdi", "templates/ConfigEditor.html"),
	templateString: "<div style='width:100%; height:100%; xpadding:0; xmargin:0' dojoAttachPoint='Root'></div>",
	widgetsInTemplate : true,

	// configEntry: tdi.tdiconfigentry
	// 		The config entry atom
	configEntry : null,
	
	// config: tdi.tdiconfig
	//		The configuration file loaded from the server
	config: null,
	
	// _editors: Object
	//		Open editors
	_editors: null,
	
	// contents: Array
	//		The content that goes into the navigator tree.
	//		Valid keys are: connector.<name>, assemblyline.<name>, assemblyline.<name>.<connector>
	contents : null,
	
	// buttons: Array
	//		The buttons in the toolbar
	buttons: [],

	constructor : function(/* Object */args) {
		dojo.safeMixin(this, args);
		this._editors = new Object();
		this._editorsConfig = new Object();
	},
	
	startConfig : function() {
		// summary:
		//		Launches the configuraiton (called from the Start button in the template)
		dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
			this.cientry = new tdi.tdicientry({atom:data});
		}), function(data) {
			this.cientry = null;
		}).then(dojo.hitch(this, function(data) {
			if(this.cientry != null) {
				tdiutil.alert(this.getString("stopSolutionFirst"));
			} else {
				dojo.when(tdiapi.startConfig(this.configEntry), dojo.hitch(this, function(data) {
					this.cientry = new tdi.tdicientry({atom:data});
					this._updateButtonStates();
					tdiutil.alert(this.getString("solutionStarted"));
				}, tdiapi.defaultErrHandler));
			}
		}));
	},
	
	stopConfig : function() {
		dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
			this.cientry = new tdi.tdicientry({atom:data});
		}), function(data) {
			this.cientry = null;
		}).then(dojo.hitch(this, function() {
			if(this.cientry != null) {
				dojo.when(tdiapi.stopConfig(this.cientry), dojo.hitch(this, function() {
					tdiutil.alert(this.getString("solutionStopped"));
					this.cientry = null;
					this._updateButtonStates();
				}), tdiapi.defaultErrHandler);
			}
		}));
	},
	
	saveConfig : function() {
		// summary:
		//		Checks in the configuration
		if(this._editors["%%elements%%"] != null) {
			this._editors["%%elements%%"].updateConfig();
		}
		
		try {
			this.config.getTombstoneSettings().setParam("AssemblyLines", "true");
			this.config.getTombstoneSettings().setParam("Configuration", "true");
		} catch(err) {
			console.log("While enabling tombstones for configuration: " + err);
		}
		
		var thisObj = this;
		dojo.when(tdiapi.checkOutConfig(this.configEntry), function() {
			return thisObj._checkinConfig();
		}, tdiapi.defaultErrHandler).then(function() {
			if(thisObj.cientry) {
				tdiutil.confirm(thisObj.getString("WebCE.restart"), function(button) {
					if(button == 0) {
						dojo.when(tdiapi.stopConfig(thisObj.cientry), function() {
							return tdiapi.startConfig(thisObj.configEntry);
						});
					}
				});
			}
		});
	},
	

	selectItem : function(name) {
		this._store.fetch({
			query:{name:name},
			onComplete: dojo.hitch(this, function(sel, items, req) {
				sel.deselectAll();
				sel.addToSelection(items[0]);
				this.openItem(items[0]);
			}, this._tree.grid.selection)
		});
	},
	
	createRunReport : function() {
		tdiutil.openDialog(null, this.getString("createRunReport"), "CreateJob.html", dojo.hitch(this, function(formData) {
			var name = formData.name;
			
			if(this.config.getAssemblyLine(name)) {
				tdiutil.error(this.getString("RenameConfigAction.AlreadyExists"));
				return;
			}
			
			var al = this.config.createAssemblyLine(name);
			if(al == null)
				return false;
			
			var alip = al.getInitParams();
			/*
			 * Generate the assemblyline params for the report
			 */
			var params = [
			  {name:"mail.OKSubject"},
			  {name:"mail.failureSubject"},
			  {name:"mail.from"},
			  {name:"mail.recipient"},
			  {name:"mail.assemblyline", nativeSyntax:"assemblyline"},
			  {name:"smtp.Host"},
			  {name:"smtp.User"},
			  {name:"smtp.Password", nativeSyntax:"password"}
			];
			dojo.forEach(params, function(p) {
				alip.newItem(p);
			});
			
			dojo.when(dojo.xhrGet({
				url:"/dashboard/static/RunReport.js",
				handleAs:"text"
			}), dojo.hitch(this, function(data) {
				al.createScriptComponent("RunReport", data);

				// create schedule so it appears initially
				var init = this.config.createSchedule(name).getInitParams();
//				init.setParam("mail.OKSubject","");
//				init.setParam("mail.failureSubject", "");
//				init.setParam("mail.from", "");
//				init.setParam("mail.recipient", "");
				init.setParam("mail.assemblyline", this.config.getAssemblyLineNames().join(","));
				var item = this._store.newItem({
					id:name,
					name:name,
					displayname:name,
					type:"assemblyline"
				});			
				this.openItem(item);
//				init.setParam("smtp.Host", "");
//				init.setParam("smtp.User", "");
//				init.setParam("smtp.Password", "");
				this.config.setModified(true);
			}));
			
		}));	
	},
	
	createAssemblyLine : function() {
		tdiutil.openDialog(null, this.getString("newAssemblyLine"), "CreateJob.html", dojo.hitch(this, function(formData) {
			var name = formData.name;
			
			if(this.config.getAssemblyLine(name)) {
				tdiutil.error(this.getString("RenameConfigAction.AlreadyExists"));
				return;
			}
			
			var al = this.config.createAssemblyLine(name);
			if(al == null)
				return false;
			var input = al.createFeedConnector("Input", "Iterator");
			var output = al.createDataFlowConnector("Output", "AddOnly");
			
			this._store.newItem({
				id:name,
				name:name,
				displayname:name,
				type:"assemblyline"
			});
			this.config.setModified(true);
		}));
	},
	
	_checkinConfig : function() {
		return dojo.when(tdiapi.checkInConfig(this.configEntry, this.config.config.solution), dojo.hitch(this, function() {
			tdiapi.unlockConfig(this.configEntry);
			this.config.setModified(false);
			this._updateButtonStates();
			this._tree.grid.setModel(this._createTreeModel());
		}), tdiapi.defaultErrHandler);
	},
	
	_configModified : function() {
		this._updateButtonStates();
		if(this._tree) {
			this._tree.refresh();
		}
	},
	
	_updateButtonStates : function() {
		dojo.forEach(this.buttons, dojo.hitch(this, function(button) {
			if(button.enableOn == "cientry")
				button.set("disabled", this.cientry == null);
			if(button.enableOn == "!cientry")
				button.set("disabled", this.cientry != null);
			if(button.enableOn == "configmodified")
				button.set("disabled", !this.config.isModified());
		}));
		if(this._tree) {
			var item = this._tree.grid.selection.getFirstSelected();
			var enabled = (item != null && item.type[0] == "assemblyline");
			this._deleteAssemblyLine.set("disabled", !enabled);
			this._renameAssemblyLine.set("disabled", !enabled);
		}
	},
	
	renameAssemblyLine : function() {
		var item = this._tree.grid.selection.getFirstSelected();
		var name = item.name[0];
		tdiutil.openDialog(null, this.getString("renameAssemblyLine"), "RenameDialog.html", dojo.hitch(this, function(formData) {
			var newname = formData.newname;
			var alconfig = this.config.getAssemblyLine(name);
			if(this.config.renameAssemblyLine(name, newname)) {
				this._store.deleteItem(item);
				this._store.save();
				// cannot change "id" so we recreate the item to avoid
				// problems if user creates a new assemblyline with the old name
				var parent = this._store.newItem({
					id:newname,
					name:newname,
					displayname:newname,
					type:"assemblyline"
				});				
				
				if(!alconfig.isEasyETL()) {
					this._store.addALComponents(alconfig.getEntryFeedComponent(), parent);
					this._store.addALComponents(alconfig.getDataFlowComponent(), parent);
				}
				
				if(this._editors[name] != null) {
					this._editors[newname] = this._editors[name];
					this._editors[name] = null;
				}
			}
		}));		
	},
	
	deleteAssemblyLine : function() {
		var item = this._tree.grid.selection.getFirstSelected();
		var name = item.name[0];
		tdiutil.confirm(this.getString("deleteAssemblyLine") + "\n" + name, dojo.hitch(this, function(buttonId, messageId, checked) {
			if(buttonId == 0) {
				this.config.deleteAssemblyLine(name);
				this.config.deleteAssemblyLineSchedules(name);
				this._store.deleteItem(item);
				this._store.save();
				if(this._editors[name] != null) {
					this.EditorArea.removeChild(this._editors[name]);
					this._editors[name].destroyRecursive();
					this._editors[name] = null;
				}
				this._tree.grid.selection.deselect(item);
				this._updateButtonStates();
			}
		}));
	},

	_createActions : function() {
		
		this.buttons = [];
		
		this.buttons.push(new dijit.form.Button({
			label: this.getString("save"),
			onClick: dojo.hitch(this, "saveConfig"),
			enableOn: "configmodified"
		}));
		
//		this.buttons.push(new dijit.form.Button({
//			label: this.getString("start"),
//			onClick: dojo.hitch(this, "startConfig"),
//			enableOn: "!cientry"
//		}));
//		
//		this.buttons.push(new dijit.form.Button({
//			label: this.getString("stop"),
//			onClick: dojo.hitch(this, "stopConfig"),
//			enableOn: "cientry"
//		}));
		
		var tree = this._tree;
		dojo.forEach(this.buttons, function(button) {
			tree.addToToolbar(button);
		});
		
		this._tree.addToActionList(new dijit.MenuItem({
			label:this.getString("createRunReport"),
			onClick:dojo.hitch(this, "createRunReport")
		}));
		
		this._tree.addToActionList(new dijit.MenuSeparator({}));
		
		this._deleteAssemblyLine = new dijit.MenuItem({
			label:this.getString("deleteAssemblyLine"),
			onClick:dojo.hitch(this, "deleteAssemblyLine")
		});
		this._tree.addToActionList(this._deleteAssemblyLine);
		
		this._renameAssemblyLine = new dijit.MenuItem({
			label:this.getString("renameAssemblyLine"),
			onClick:dojo.hitch(this, "renameAssemblyLine")
		});
		this._tree.addToActionList(this._renameAssemblyLine);
		
		this._tree.addToActionList(new dijit.MenuItem({
			label:this.getString("newAssemblyLine"),
			onClick:dojo.hitch(this, "createAssemblyLine")
		}));
		
		this._tree.addToActionList(new dijit.MenuSeparator({}));
		
		this._tree.addToActionList(new dijit.MenuItem({
			label:this.getString("editConfigItems"),
			onClick:dojo.hitch(this, "_editConfigItems")
		}));
		
		this._deleteAssemblyLine.set("disabled", true);
		this._renameAssemblyLine.set("disabled", true);
		
		this._updateButtonStates();
	},
	
	_createTreeModel : function() {
		// Add Description item
		var items = [{
			id:"%desc%",
			type:"description",
			name:this.getString("solutionDescription"),
			displayname:this.getString("solutionDescription")
		}];
		
		this._store = new tdi.model.PublishedObjectsStore({config:this.config, items:items});
		if(this._store.numberItems() == 0)
			this._store.loadAssemblyLines();
		
	    this._model = new dijit.tree.ForestStoreModel({
	        store: this._store,
	        rootId: "configRoot",
	        rootLabel: "Server Projects",
	        childrenAttrs: ["items"]
	    });
	    
		return this._model;
	},
	
	hasSchedule : function(config, str) {
		var sched = config.getScheduleForAssemblyLine(str);
		if(sched) {
			sched = config.getSchedule(sched);
			if(sched && sched.getEnabled())
				return true;
		} else {
			return false;
		}
	},
	
	addIconToItem: function(str, item) {
		var images = {
				"script": "Script_16.gif",
				"branch": "Branch_Enabled.gif",
				"function": "FC_16.gif",
				"loop": "Connector_Loop.gif",
				"map": "AttributeMap_16.gif"
		};
		
		if(this._tree) {
			var storeItem = this._tree.getItem(item);
			var config = this._store.getValue(storeItem, "config");
			var maintype = this._store.getValue(storeItem, "type");
			var image = null;
			if(config) {
				var type = config.getSubType();
				if(!type)
					type = maintype;
				image = images[type];
				if(!image && config.getMode) {
					image = "Connector_" + config.getMode() + "_Enabled.gif";
				} else if(maintype == "assemblyline") {
					if(this.hasSchedule(config, str)) {
						image = "schedule.gif";
					}
				}
				if(type == "branch") {
					var conditions = config.getBranchConfig().getConditions();
					if(conditions) {
						if(config.isSwitch()) {
							str = this.getString("ConfigLabelProvider.Branch.3", [conditions.getLabel()]);
							
						} else if(config.isCase()) {
							var label = conditions.getLabel();
							// default case?
							if(!label)
								label = this.getString("ConfigLabelProvider.7");
							str = this.getString("ConfigLabelProvider.Branch.4", [label]);
							
						} else if(config.isIf()) {
							str = this.getString("ConfigLabelProvider.Branch.0", [config.getName()]);
							
						} else if(config.isElseIf()) {
							str = this.getString("ConfigLabelProvider.Branch.1", [config.getName()]);
							
						} else if(config.isElse()) {
							str = this.getString("ConfigLabelProvider.Branch.2", [config.getName()]);
							
						} else {
							console.log("Unknown branch type: " + config.getBranchType())
							
						}
					}
				}
			}
			
			if(image) {
				if(image == "schedule.gif")
					return str + "<img style='width:12px; height:12px; margin-right:3px' src='images/" + image + "'></img>";
				else
					return "<img style='margin-right:3px' src='images/" + image + "'></img>" + str;
			}
		}
		return str;
	},
	
	_createTree : function() {
		if(this._tree != null) {
			this._tree.destroyRecursive(false);
		}
		
		var layout = [
		    {field:"displayname", name:this.getString("name"), width:"auto", formatter:dojo.hitch(this, "addIconToItem")}
		];
		
		var toolbarOptions = {
			refreshIcon : false,
			refreshMenu : false,
			selectAllIcon : false,
			expandAllIcon : false,
			clearSortIcon: false,
			configureTreeTableIcon:false,
			actionMenu:true,
			selectAllMenu: false,
			expandAllMenu : false,
			clearSortMenu: false,
			configureTreeTableMenu:false				
		};
	
		var plugins = {
				indirectSelection: true,
				nestedSorting: true,
				//menus:menubars,
				dnd:false,
				filter:{},
				printer: false,
				exporter: false,			
				pagination: false
			};
		
		var gparams = {
			treeModel: this._createTreeModel(),
			structure: layout,
			rowsPerPage: 20,
			noDataMessage: "No configurable items in solution",
			plugins: plugins,
			autoWidth:false,
			filter: {
				hideFilterBar:true,
				showQuickFilter:false,
				showQuickFilterButton:false
			}
		};
		
		this._tree = new dojoe.treetable.TreeTable({
			gridParams:gparams,
			toolbarOptions: toolbarOptions,
			enableResize: true,
			footerVisible:true,
			width: "100%",
			height: "100%"
		});
		
		
		this._centerBorder = new dijit.layout.BorderContainer({
			style:"width:100%; height:100%; border-width:0px; margin:5px; padding:0",
			gutters:"false"
		});
		
		this._centerPane.set("content", this._centerBorder);
		
//		this._centerTop = new dijit.layout.ContentPane({
//			region:"top",
//			splitter:false,
//			style:"border-width:0px; padding:0; margin:0"
//		}).placeAt(this._centerBorder);
//		var h1 = dojo.create("h1", {innerHTML:"Configure Solution"});
//		this._centerTop.set("content", h1);
		
		this._centerCenter = new dijit.layout.ContentPane({
			region:"center",
			splitter:true,
			style:"border-width:0px; padding:0; margin:0"
		}).placeAt(this._centerBorder);
		this._centerCenter.set("content", this._tree);
		
		dojo.connect(this._tree.grid, "onSelected", dojo.hitch(this, function(id) {
			var item = this._tree.getItem(id);
			this.openItem(item);
			this._updateButtonStates();
		}));
	},
	
	openItem : function(item) {

		var id = this._store.getValue(item, "id");
		
		if(this._editors[item.id] == null) {
			var type = this._store.getValue(item, "type");
			var name = this._store.getValue(item, "name");
			var cfg = this._store.getValue(item, "config");
			
			if(type == "assemblyline") {
				var alconfig = this.config.getAssemblyLine(name);
				if(alconfig.isEasyETL()) {
					this._editors[id] = new tdi.EasyETL({config:this.config, assemblylineName:name, configentry:this.configEntry});
				} else {
					this._editors[id] = new tdi.ConfigEditorAssemblyline({config:alconfig, itemDef:item});
				}
				this.EditorArea.addChild(this._editors[id]);
				
			} else if (type == "properties") {
				this._editors[id] = new tdi.ConfigEditorProps({cientry:this.cientry, config:this.config, properties:item.properties});
				this.EditorArea.addChild(this._editors[id]);
				
			} else if (type == "connector" || type == "function") {
				this._editors[id] = new tdi.ConnectorEditor({title:cfg.getName(), itemDef:item, cientry:this.cientry, config:cfg, hideNullValues:false});
				this.EditorArea.addChild(this._editors[id]);
				
			} else if (type == "script") {
				this._editors[id] = new tdi.JavascriptEditor({
					config:cfg,
					editable:false,
					style:"border-width:1px; width:98%; height:98%"
				});
				this.EditorArea.addChild(this._editors[id]);

			} else if (type == "map") {
				this._editors[id] = new tdi.AttributeMap({attributeMapConfig:cfg.getAttributeMap()});
				this.EditorArea.addChild(this._editors[id]);
				
			} else if (type == "branch") {
				this._editors[id] = new tdi.BranchConditions({
					config:cfg,
					editable:false,
					style:"border-width:1px"
				});
				this.EditorArea.addChild(this._editors[id]);

			} else if (type == "loop") {
				var loop = cfg.getBranchConfig();
				if(loop.getBranchType() == "ConnectorLoop") {
					var cc = loop.getConnectorConfig();
					this._editors[id] = new tdi.ConnectorEditor({title:cfg.getName(), itemDef:item, cientry:this.cientry, config:cc, hideNullValues:false});
					this.EditorArea.addChild(this._editors[id]);
					
				} else if (loop.getBranchType() == "AttributeLoop") {
					this._editors[id] = new tdi.AttributeLoop({config:loop.getAttributeConfig(), title:cfg.getName()});
					this.EditorArea.addChild(this._editors[id]);
					
				} else if (loop.getBranchType() == "WhileLoop") {
					this._editors[id] = new tdi.BranchConditions({
						config:cfg,
						editable:false,
						style:"border-width:1px"
					});
					this.EditorArea.addChild(this._editors[id]);
				}

			} else if (type == "elements") {
				this._editors[id] = new tdi.ConfigElements({config:this.config, cientry:this.cientry});
				this._editors[id].applySettings = dojo.hitch(this, "resetTreeView");
				this.EditorArea.addChild(this._editors[id]);
				
			} else if (type == "description") {
				var desc = this.solution.getUserComment();
				if(desc == null) {
					desc = "";
				}
				this._editors[item.id] = new tdi.TextEditor({
					text:desc,
					textChanged: dojo.hitch(this, function(newvalue) {
						this.solution.setUserComment(newvalue);
						this.config.setModified(true);
					}),
					style:"border-width:1px"
				});
				this._editors[item.id].setValue(desc);
				this.EditorArea.addChild(this._editors[item.id]);
				
			}
		}
		if(this._editors[id])
			this.EditorArea.selectChild(this._editors[id]);

		// Save last edited item in a cookie
		var path = [];
		path.push("configRoot");
		if(item.alname != null)
			path.push("A" + item.alname[0]);
		path.push(item.id[0]);
		tdiutil.setCookie(this.config.getConfigName(), path.join("/"));
	},

	onConfigLoad: function(data) {
		this.config = new tdi.tdiconfig({config:data});
		this.onConfigLoad2();
	},
	
	onConfigLoad2: function() {
		this.solution = this.config.getSolutionInterface();
		var defaultSelection = null;
		this._createTree();
		this._createActions();
	    this.borderTop.startup();
	    this.config.setModified(false);
	    this._updateButtonStates();
	    dojo.connect(this.config, "onModify", dojo.hitch(this, "_configModified"));
	},
	
	_runAssemblyLine : function() {
		dojo.publish(tdiconstants.runAssemblyLineSubject, [{config:this.config, assemblyline:this._selectedTreeItem.al[0]}]);
	},
	
	_updateConfigItems : function(editor, save) {
		this._editSolutionDlg.hide();
		if(save)
			editor.updateConfig();
		editor.stopTempInstance();
		this.destroyEditors();
		this._tree.grid.setModel(this._createTreeModel());
	},
	
	_editConfigItems : function() {
		if(this._editSolutionWidget) {
			this.stackContainer.removeChild(this._editSolutionWidget);
			this._editSolutionWidget.destroyRecursive();
			delete this._editSolutionWidget;
			this.destroyEditors();
			this._tree.grid.selection.deselectAll();
			this._tree.grid.setModel(this._createTreeModel());
			this.stackContainer.selectChild(this.borderTop);
		} else {
			this._editSolutionWidget = new tdi.EditSolutionSettings({config:this.config, onClose:dojo.hitch(this, function() {
				this._editConfigItems();
			})});
			this.stackContainer.addChild(this._editSolutionWidget);
			this.stackContainer.selectChild(this._editSolutionWidget);
		}
	},
	
	_showDatabrowser : function(id, input) {
			var db = new tdi.Databrowser({
				config:(input ? etl.input : etl.output),
				style:"width:100%; height:100%; border-width:0px; margin:5px; padding:0",
				_saveTarget: dojo.hitch(this, function() {
					this.stackContainer.removeChild(this.databrowser);
					this.databrowser.destroyRecursive();
					this.databrowser = null;
					this.stackContainer.selectChild(this.borderTop);
				})
			});
			db.setSaveLabel(this.getString("close"));
			this.stackContainer.addChild(db);
			this.stackContainer.selectChild(db);
			this.databrowser = db;
	},
	
	_buildUI : function() {
		// description:
		//		We build the BorderContainer hierarchy via script instead of templates
		//		as the template approach messes up the layout big time (because it is inside a TabContainer).
		
		// +---- border ----------------------------------------+
		// +
		// +   +--- topContentPane (pane) ----------------------+
		// +   +
		// +   +--- center (pane) ------------------------------+
		// +   +
		// +   +   +-- border2 ---------------------------------+
		// +   +   +
		// +   +   +  +-- left(pane) --- + center2 (pane) ------+
		// +   +   +  +
		// +   +   +  +-----------------------------------------+
		// +   +   +  
		// +   +   +--------------------------------------------+
		
		this.stackContainer = new dijit.layout.StackContainer({
			style:"width:100%; height:100%; border-width:0px; margin:0; padding:0"
		}).placeAt(this.Root);
		
		this.borderTop = new dijit.layout.BorderContainer({
			style:"width:100%; height:100%; border-width:0px; margin:0; padding:0",
			gutters:"false"
		});
		this.stackContainer.addChild(this.borderTop);
		this.stackContainer.selectChild(this.borderTop);
		
		this._centerTop = new dijit.layout.ContentPane({
			region:"top",
			splitter:false,
			style:"border-width:0px; padding:0px; margin-left:5px; margin-top:5px; margin-bottom:0px"
		}).placeAt(this.borderTop);
		
		var h1div = dojo.create("div");
		var h1 = dojo.create("h1", {innerHTML:"Configure Solution", style:"margin:0px; padding:0px"}, h1div);
//		this._saveButton = new dijit.form.Button({
//			label:this.getString("WebCE.save"),
//			onClick:dojo.hitch(this, "saveConfig")
//		}).placeAt(h1div);
//		new dijit.form.Button({
//			label:this.getString("WebCE.close")
//		}).placeAt(h1div);
		this._centerTop.set("content", h1div);

		this._centerPane1 = new dijit.layout.ContentPane({
			region:"center",
			splitter:true,
			style:"border-width:0px; padding:0; margin:0"
		}).placeAt(this.borderTop);
		
		this.borderContainer = new dijit.layout.BorderContainer({
			style:"border-width:0px; margin:0; padding:0",
			gutters:"false"
		});
		this._centerPane1.set("content", this.borderContainer);
		
		this._centerPane = new dijit.layout.ContentPane({
			region:"left",
			style:"border-width:0px; padding:0; margin:0; width:25%",
			splitter:true
		}).placeAt(this.borderContainer);

		this._editorPane = new dijit.layout.ContentPane({
			region:"center",
			style:"border-width:0px; padding:0; margin:0"
		}).placeAt(this.borderContainer);
		
		this.EditorArea = new dijit.layout.StackContainer({style:"padding:0; margin:0"});
		this._editorPane.set("content", this.EditorArea);
	},
	
	resize: function(obj) {
		if(obj.h > 0 && this.stackContainer != null) {
			// Resize so that editor/view resizes properly
			// with the topmost border layout.
			this.stackContainer.resize(obj);
		}
		if(obj.h > 0 && this.borderTop != null) {
			// Resize so that editor/view resizes properly
			// with the topmost border layout.
			this.borderTop.resize(obj);
		}
		if(obj.h > 0 && this.borderContainer != null) {
			// Resize so that editor/view resizes properly
			// with the topmost border layout.
			this.borderContainer.resize(obj);
		}
		if(this._tree != null) {
			// Resize so that editor/view resizes properly
			// with the topmost border layout.
			this._tree.resize();
		}
		if(this.EditorArea != null) {
			// Resize so that editor/view resizes properly
			// with the topmost border layout.
			this.EditorArea.resize();
		}
	},
	
	destroyEditors : function() {
		for(var f in this._editors) {
			try {
				if(this.EditorArea) {
					this.EditorArea.removeChild(this._editors[f]);
				}
				this._editors[f].destroyRecursive();
				this._editors[f] = null;
			} catch(err) {
				console.log(err);
			}
		}
	},
	
	_updateRunStatusEvent: function(event) {
		if(event.ciId && event.ciId == this.configEntry.getTitle()) {
			if(event.type == "di.ci.stop") {
				this.cientry = null;
			} else if(event.type == "di.ci.start") {
				dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
					this.cientry = new tdi.tdicientry({atom:data});
				}));
			}
		}
	},
	
	destroyRecursive : function() {
		this.inherited(arguments);
		this.destroyEditors();
		this.EditorArea.destroyRecursive(false);
		this._centerPane.destroyRecursive(false);
		this._centerPane1.destroyRecursive(false);
		this.borderTop.destroyRecursive(false);
	},
	
	postCreate: function() {
		this._buildUI();
		if(this.configEntry != null) {
			if(this.config == null) {
				dojo.when(tdiapi.getConfig(this.configEntry), dojo.hitch(this, "onConfigLoad"), tdiapi.defaultErrHandler);
			} else {
				this.onConfigLoad2();
			}
			dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
				this.cientry = new tdi.tdicientry({atom:data});
				this._updateButtonStates();
			}));
			
			setTimeout(dojo.hitch(this, function() {
				if(this._tree.grid.selection.getSelectedCount() == 0)
					this._tree.grid.selection.addToSelection(0);
			}), 500);
			
			this._eventsHandler = dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_updateRunStatusEvent"));
		}
	}

});



dojo.declare("tdi.ConfigEditorAssemblyline", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the components in an assemblyline.
	//		Only connectors and script components are editable.

	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/ConfigEditorAssemblyline.html"),
	widgetsInTemplate : true,
	
	// config: tdi.assemblyline
	//		The assemblyline to edit
	config: null,
	
	runAssemblyLine : function() {
		// summary:
		//		Starts an assemblyline in its own temp config
		//		and shows a tab with the log output and process info/controls.
		var uniqueId = tdiutil.generateInstanceId(this.config);
		dojo.when(tdiapi.startTempConfig(this.config.getParent(), uniqueId), dojo.hitch(this, function(data) {
			this._openAssemblyLine(new tdi.tdicientry({atom:data}), this.config.getName());
		}), tdiapi.defaultErrHandler);
	},
	
	stopAssemblyLine : function() {
		if(this.cci != null)
			this.cci.stopAssemblyLine();
	},
	
	_openAssemblyLine : function(cientry, assemblyline) {
		var key = cientry.getId();
		if(this.cci != null) {
			this.cci.destroyRecursive();
		}
		this.cci = new tdi.RunAssemblyLineInstance({
			cientry : cientry,
			logoptions : "small",
			config:this.config,
			assemblyline : assemblyline,
			assemblylineStarted : dojo.hitch(this, function() {
				this.runButton.set("disabled", true);
				this.stopButton.set("disabled", false);
			}),
			assemblylineStopped : dojo.hitch(this, function() {
				this.runButton.set("disabled", false);
				this.stopButton.set("disabled", true);
			})
			
		}).placeAt(this.RunLog);
	},
	
	postCreate : function() {
		var tablist = {};
		dojo.forEach(this.Tabs.getChildren(), function(child) {
			if(child.title != null) {
				tablist[child.title] = child;
			}
		});
		if(this.itemDef != null && dojo.isArray(this.itemDef.options)) {
			var options = this.itemDef.options[0];
			
			var run = this.getString("run");
			if(options.ShowRun != null && !options.ShowRun)
				this.Tabs.removeChild(tablist[run]);
			
			var schedule = this.getString("schedule");
			if (options.ShowConfig != null && !options.ShowConfig)
				this.Tabs.removeChild(tablist[schedule]);
			else
				this.Configuration.setConfig(this.config);
				
		} else {
			this.Configuration.setConfig(this.config);
		}
		
		this.runButton.set("disabled", false);
		this.stopButton.set("disabled", true);
		
//		setTimeout(dojo.hitch(this, function() {
//			this.config.setModified(false);
//		}), 250);
	}
});
