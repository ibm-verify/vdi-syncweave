/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/json",
	"dojo/aspect",
	"dojo/cookie",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Menu",
	"dijit/MenuItem",
	"dijit/CheckedMenuItem",
	"dijit/PopupMenuBarItem",
	"dijit/MenuBar",
	"dijit/DropDownMenu",
	"dijit/Dialog",
	"dijit/form/Button",
	"dijit/form/DropDownButton",
	"idx/layout/HeaderPane",
	"idx/form/Select",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/Toolbar",
	"dijit/ToolbarSeparator",
	"dijit/TooltipDialog",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/UploadSolution",
	"tdi/CreateSolution",
	"tdi/aleditor/ALComponent",
	"tdi/aleditor/ALComps",
	"tdi/aleditor/ALConnection",
	"tdi/aleditor/ALDataCollector",
	"tdi/aleditor/ALEditor2",
	"tdi/aleditor/ALEditorMain",
	"tdi/aleditor/ALFlow",
	"tdi/aleditor/ALProjects",
	"tdi/aleditor/ALScripts",
	"tdi/aleditor/Border",
	"tdi/aleditor/Colors",
	"tdi/ToolbarLabel",
	"dojo/data/ItemFileReadStore",
	"tdi/NlsMixin",
	"dojo/text!./templates/ALMain.html"
], function(declare, array, lang, json, aspect, cookie, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, dMenu, dMenuItem, dCheckedMenuItem, dPopupMenuBarItem, dMenuBar, dDropDownMenu, Dialog, Button, DropButton, HeaderPane, Select, BorderContainer, ContentPane, 
		dToolbar, ToolbarSeparator, TooltipDialog, tdiapi, tdiconfig, tdiutil, UploadSolution, CreateSolution, ALComponent, ALComps, ALConnection, ALDataCollector, ALEditor, ALEditorMain, ALFlow, ALProjects, ALScripts, 
		Border, TDI, ToolbarLabel, ItemFileReadStore, NlsMixin, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, NlsMixin ],
	{
		templateString : template,
		
//		templateString: "<div data-dojo-attach-point='Main' style='height:100%; width:100%; margin:0; padding:0'></div>",
		
		// configurations
		configurations: {},
		
		// title panels for configs
		configpanels: {},
		
		// open editors
		_editors: {},
		
		// stack or tabs for solutions
		containerType: "stack", // "tab"
		
		// stack or tabs for assemblylines
		containerTypeAL: "stack", // "tab"
		
		showMemLeak: function() {
			this._reglen = this._reglen || dijit.registry.length;
			
			var diff = dijit.registry.length - this._reglen;
			console.log("Current registered dijit widgets: " + dijit.registry.length + "; difference=" + diff)
			if(diff != 0) {
				// console.log("******** Non TDI widgets");
				for(var f in dijit.registry._hash) {
					if(this._regcont) {
						if(!this._regcont[f]) {
							console.log("Leaked: " + f + ": " + dijit.registry._hash[f]);
						}
					}
				}
			}
			alert("Current registered dijit widgets: " + dijit.registry.length + "; difference=" + diff)
			this._reglen = dijit.registry.length;
			this._regcont = {};
			for(f in dijit.registry._hash) {
				this._regcont[f] = true;
			}
		},
		
		getSolutions: function() {
			// summary:
			//		Populates the solution dropdown with available solutions
			//		on the TDI server
			array.forEach(this.solutionSelect.getChildren(), function(child) {
				child.destroy();
			}, this);
			
			var t = this;
			
			tdiapi.getServerProjects().then(function(data) {
				var last = dojo.cookie("TDIDashboard.lastsolution");
				var qs = dojo.queryToObject(dojo.doc.location.search.substr((dojo.doc.location.search[0] === "?" ? 1 : 0)));
				if(qs && qs.config)
					last = qs.config;
				
				if(qs && qs.assemblyline)
					t.lastAssemblyLine = qs.assemblyline;
				
				var selected = -1;
				for(i = 0; i < data.items.length; i++) {
					if(!t.isConfigExcluded(data.items[i].name)) {
						if(data.items[i].name == last) {
							selected = i;
						}
						// -- good grief; dont use ints on the value
						t.solutionSelect.addOption({value:i+"", label:data.items[i].name});
					}
				}
				if(selected != -1) {
					t.solutionSelect.set("value", selected+"");
				} else {
					t.solutionSelect.set("value", "0");
				}
				t.data = data;
				t.enableButtons(false);
			}, function(err) {
				alert(err);
			});
		},
		
		createSolution : function(templateId) {
			var create = new CreateSolution();
			var dlg = new Dialog({
				title:"Create solution",
				content: create,
				style: "width: 500px"
			});
			create.uploadCompleted = dojo.hitch(this, function() {
				var solname = null;
				try {
					solname = create.Solution.get("value");
				} catch(err) {
				}
				dlg.hide();
				
				// -- wait for refresh before attempting to open config
				if(solname != null) {
					setTimeout(dojo.hitch(this, function(solname) {
						alert("Created " + solname)
					}, solname), 500);
				}
			});
			dlg.show();
		},
		
		importSolution: function() {
			var upload = new UploadSolution();
			var dlg = new Dialog({
				title:"Upload Solution", // this.getString("uploadSolution"),
				content: upload,
				style: "width: 500px"
			});
			upload.uploadCompleted = dojo.hitch(this, function() {
				dlg.hide();
				this.getSolutions();
			});
			dlg.show();
		},
		
		saveConfig: function(config) {
			var config = this.getSelectedSolution().tdiconfig;
			var configentry = this.getSelectedSolution().entry;
			
			try {
				config.getTombstoneSettings().setParam("AssemblyLines", "true");
				config.getTombstoneSettings().setParam("Configuration", "true");
			} catch(err) {
				console.log("While enabling tombstones for configuration: " + err);
			}
			
			var thisObj = this;
			dojo.when(
				tdiapi.checkOutConfig(configentry),
				function() {
					return dojo.when(
						tdiapi.checkInConfig(configentry, config.config.solution), function() {
							tdiapi.unlockConfig(configentry);
							config.setModified(false);
						}, tdiapi.defaultErrHandler);
				},
				tdiapi.defaultErrHandler)
			.then(lang.hitch(this, function() {
				this._mainSaveButton.set("disabled", true); 
//				this._mainSaveButton.set("label", "Save (" + now + ")"); 
			}));
		},
		
		selectSolution: function(index) {
			// summary:
			//		callback from solution dropdown
			//		open/select the editor for this solution
			this.selectedSolution = index;
			if(index >= 0) {
				var key = this.getSelectedSolution().name;
				dojo.cookie("TDIDashboard.lastsolution", key);
				if(!this.tabPane.hasContainerPane(key)) {
					dojo.when(tdiapi.getConfig(this.data.items[index].entry), dojo.hitch(this, function(data) {
						var conf = new tdiconfig({config:data});
						this.data.items[index].tdiconfig = conf;
						this.openALMainEditor();
					}));
				} else {
					this.tabPane.selectContainerPane(key);
					this.tabSelected(key);
				}
			}
			this.enableButtons(index >= 0);
		},
		
		enableButtons: function(enable) {
			this.saveButton.set("disabled", true);
		},
		
		selectAssemblyLine: function(alname) {
			// summary:
			//		callback from assemblyline dropdown
			//		open/select the assemblyline editor
			
			var configlabel = this.getSelectedSolution().name;
			var aleditor = this.tabPane.getContainerPaneWidget(configlabel);
			if(aleditor && aleditor.openAssemblyLine) {
				aleditor.openAssemblyLine(alname);
			}
		},
		
		createAssemblyLine: function() {
			// summary:
			//		callback to create new AL in current config
			//		delegate to ALEditorMain
			var configlabel = this.getSelectedSolution().name;
			var aleditor = this.tabPane.getContainerPaneWidget(configlabel);
			if(aleditor && aleditor.openAssemblyLine) {
				aleditor.createAssemblyLine();
			}
		},
		
		deleteAssemblyLine: function(name) {
			// summary:
			//		callback to create new AL in current config
			//		delegate to ALEditorMain
			var t = this;
			tdiutil.confirm("Delete " + name, function() {
				var config = t.getSelectedSolution().tdiconfig;
				config.deleteAssemblyLine(name);
				t.updateProjectsData(config);
				var aleditor = t.tabPane.getContainerPaneWidget(t.getSelectedSolution().name);
				if(aleditor && aleditor.removeAssemblyLine) {
					aleditor.removeAssemblyLine(name);
				}
			});
		},
		
		doAutoSave: function() {
			var now = new Date();
			try {
				this.saveConfig();
			} catch(err) {
				tdiutil.error(err);
			}
		},
		
		openALMainEditor: function() {
			// summary:
			//		Opens/selects the assemblyline editor for the current solution
			
			var configentry = this.getSelectedSolution().entry;
			var configlabel = this.getSelectedSolution().name;
			var config = this.getSelectedSolution().tdiconfig;
			
			config.onModify = lang.hitch(this, function(modified, args) {
				this.saveButton.set("disabled", !modified);
				if(modified) {
					if(this._timer) {
						clearTimeout(this._timer);
					}
					this._timer = setTimeout(lang.hitch(this, "doAutoSave"), 3*1000);
					this._mainSaveButton.set("disabled", false); 
//					this._mainSaveButton.set("label", "Save"); 
				}
			});
			
			//
			// -- Gfx editor pane (center)
			//
			var key = configlabel;
			var alEditor = new ALEditorMain({
				config:config,
				configentry:configentry,
				containerType:this.containerTypeAL,
				onEvent:lang.hitch(this, "solutionEvent"),
				projects:this.projects
			});
			this.tabPane.addContainerPane(alEditor, {
				title:configlabel,
				closable:true,
				onClose: dojo.hitch(this, function() {
					delete this._editors[key];
					return true;
				})
			});
			this.tabPane.selectContainerPane(key);
			this.tabSelected(key);
		},
		
		getSelectedSolution: function() {
			// summary:
			//		returns the selected solution object
			if(this.selectedSolution >= 0) {
				return this.data.items[this.selectedSolution];
			}
			return null;
		},
		
		tabSelected: function(key) {
			// summary:
			//		callback when solution changes
			//		update assemblyline dropdown with names from solution
			var config = this.getSelectedSolution().tdiconfig;
			var data = array.map(config.getAssemblyLineNames(), function(name) {
				return {value:name, label:name, name:name, id:name};
			});
			data = data.sort(function(a,b) {
				// cannot have equal names so just check for less
				if(a.name < b.name)
					return -1;
				else
					return 1;
			});
						
			var store = new ItemFileReadStore({
				data:{
					identifier:"value",
					label:"label",
					items:data
				}
			});
			this.assemblylineSelect.setStore(store, this.lastAssemblyLine);

			this.updateProjectsData(config);
		},
		
		updateProjectsData: function(config) {			
			var data2 = array.map(config.getAssemblyLineNames(), function(name) {
				return {id:name};
			});
			data2 = data2.sort(function(a,b) {
				// cannot have equal names so just check for less
				if(a.id < b.id)
					return -1;
				else
					return 1;
			});
			this.projects.setAssemblyLineData(data2);
		},
		
		delegateAction: function(action) {
			// summary:
			//		Delegate action to current editor
			var solution = this.getSelectedSolution();
			if(solution) {
				var editor = this.tabPane.getContainerPaneWidget(solution.name);
				if(editor && editor.delegateAction) {
					editor.delegateAction(action);
				}
			}
		},
		
		solutionEvent: function(event) {
			// summary:
			//		Called by the editors to signal changes/events that
			//		may need actions by the ALMain widget.
			
			// refresh dropdown list
			this.tabSelected(event.name);
			if(event.action == "create") {
				// created assemblyline - make it current selection and open it
				this.assemblylineSelect.set("value", event.assemblyline);
			}
		},
		
		addHeaderPaneTools: function(headerPane) {
			// summary:
			//		Add solution level tools
			
			headerPane.addChild(new ToolbarLabel({label:"Select solution: "}));
			
			this.solutionSelect = new Select({
				onChange:dojo.hitch(this, "selectSolution"),
				style:"width:200px;",
				region:"majorActions"
			});
			this._supportingWidgets.push(this.solutionSelect);
			headerPane.addChild(this.solutionSelect);
			
			this.solutionSelect = this.projects._projects;
			
			this.saveButton = new Button({
				label:"Save",
				onClick:lang.hitch(this, "saveConfig")
			});
			headerPane.addChild(this.saveButton);
			
			var subMenu = new dDropDownMenu({});
			subMenu.addChild(new dMenuItem({
				label:"Import solution...",
				onClick:lang.hitch(this, "importSolution")
			}));
			subMenu.addChild(new dMenuItem({
				label:"Create solution...",
				onClick:lang.hitch(this, "createSolution")
			}));
			subMenu.addChild(new dMenuItem({
				label:"Delete solution",
				onClick:lang.hitch(this, "showMemLeak")
			}));
			headerPane.addChild(new DropButton({
				label:"Actions",
				dropDown:subMenu
			}));
		},
		
		addHeaderPaneTools2: function(headerPane) {
			// summary:
			//		Add assemblyline level tools
			
			headerPane.addChild(new ToolbarSeparator());
			
			headerPane.addChild(new ToolbarLabel({label:"Select assemblyline: "}));
			
			this.assemblylineSelect = new Select({
				onChange:dojo.hitch(this, "selectAssemblyLine"),
				style:"width:200px"
			});
			this._supportingWidgets.push(this.assemblylineSelect);
			headerPane.addChild(this.assemblylineSelect);
			
			var subMenu = new dDropDownMenu({});
			subMenu.addChild(new dMenuItem({
				label:"Rename assemblyline...",
				onClick:lang.hitch(this, "delegateAction", "rename")
			}));
			subMenu.addChild(new dMenuItem({
				label:"New assemblyline...",
				onClick:lang.hitch(this, "delegateAction", "create")
			}));
			subMenu.addChild(new dMenuItem({
				label:"Delete assemblyline",
				onClick:lang.hitch(this, "delegateAction", "delete")
			}));
			headerPane.addChild(new DropButton({
				label:"Actions",
				dropDown:subMenu
			}));
		},
		
		addHeaderPaneTools3: function(headerPane) {
			// summary:
			//		Add misc tools
			
			headerPane.addChild(new ToolbarSeparator());

			var subMenu = new dDropDownMenu({});
			subMenu.addChild(new dCheckedMenuItem({
				label:"Use tabs for solutions",
				checked:this.containerType == "tab",
				onClick:lang.hitch(this, function(value) {
					if(value)
						this.containerType = "tab";
					else
						this.containerType = "stack";
					this.saveOptions();
				})
			}));
			
			subMenu.addChild(new dCheckedMenuItem({
				label:"Use tabs for assemblylines",
				checked:this.containerTypeAL == "tab",
				onClick:lang.hitch(this, function(value) {
					if(value)
						this.containerTypeAL = "tab";
					else
						this.containerTypeAL = "stack";
					this.saveOptions();
				})
			}));
			
			headerPane.addChild(new DropButton({
				label:"Options",
				dropDown:subMenu
			}));
			
		},
		
		loadOptions: function() {
			var json = cookie("TDIDashboard");
			if(json) {
				var options = json.fromJson(json);
				declare.safeMixin(this, options);
			}
		},
		
		saveOptions: function() {
			var options = {
					containerType:this.containerType,
					containerTypeAL:this.containerTypeAL
			};
			cookie("TDIDashboard", json.toJson(options));
		},

		resize: function(obj) {
			if(this.borderContainer) {
				this.borderContainer.resize(obj);
			}
			this.inherited(arguments);
		},
		
		createProjectsPane: function() {
			this.projects = new ALProjects();
			this.projects.startup();
			this.projects.onChange = lang.hitch(this, "selectSolution");
			this.projects.onSelectionChanged = lang.hitch(this, "selectAssemblyLine");
			this.projects.onAddAssemblyline = lang.hitch(this, "createAssemblyLine");
			this.projects.onDeleteAssemblyline = lang.hitch(this, "deleteAssemblyLine");

//			this.projectsPane = new HeaderPane({
//				title:"Projects",
//				style:"width:100%; height:100%; margin:0px; padding:0px; background:white",
//				content:this.projects
//			});
//			this.projectsPane.startup();
		},
		
		buildUI: function() {
			//
			// -- Main border container
			//
			if(!this.borderContainer) {
				this.borderContainer = new Border({
					gutters:false,
					design:"sidebar",
					containerType:"none",
					style:"width:100%; height:100%; margin:0; padding:0"
				}).placeAt(this.Main);
				this._supportingWidgets.push(this.borderContainer);
			}
			
			// -- Widget to contain list of projects/assemblylines
			this.createProjectsPane();
			
			//
			// -- Top: Toolbar
			//
			this.headerPane = new dToolbar({style:"width:100%; padding:5px"});
			this.addHeaderPaneTools(this.headerPane);
			this.addHeaderPaneTools2(this.headerPane);
//			this.addHeaderPaneTools3(this.headerPane);
//			this.borderContainer.setTop(this.headerPane, {style:"width:100%; margins:120 0 0 0; padding:0", splitter:false, title:"Solutions"});
			
			//
			// -- Center: editors
			//
			this.tabPane = new Border({
				style:"width:100%; height:100%; margin:0; padding:0",
				containerType:this.containerType
			});
			this.tabPane.startup();
			this.borderContainer.setCenter(this.tabPane, {splitter:true});
			
			this.borderContainer.startup();
		},
		
		_subEditSolution: function(sol) {
			var tabml = dijit.byId("TDILauncher");
			if(tabml && tabml.selectWorkspace) {
				tabml.selectWorkspace("Editor");
				var result = array.filter(this.solutionSelect.getOptions(), function(opt) {
					return (opt.label == sol);
				});
				if(result.length == 1) {
					this.solutionSelect.set("value", result[0].value);
				}
			}
		},
		
		onLogout: function() {
			window.location = "logout.html";
		},
		
		onShowHelp: function() {
			window.open("https://www.ibm.com/docs/en/vdi/11.0.0?topic=server-federated-directory", "_FDS_HELP");
		},
		
		toggleAutoUpdate: function(auto) {
			this.saveCookies();
		},
		
		toggleAutoSave: function(auto) {
			this.saveCookies();
		},
		
		saveSnapShot: function() {
//			this.flows.saveSnapShot(this._snapshotTitle.get("value"));
		},
		
		loadSnapShot: function() {
//			this.flows.loadSnapShot(this._snapshots.get("value"));
		},
		
		deleteSnapShot: function() {
//			this.flows.deleteSnapShot(this._snapshots.get("value"));
		},
		
		onShowWelcome: function() {
//			this.flows.showWelcomePage();
		},
		
		loadCookies: function() {
//			LDSUtil.loadCookies();
//			this._autosave.set("value", LDSUtil.getOption("autoSave", true));
//			this._autoupdate.set("value", LDSUtil.getOption("autoUpdate", true));
		},
		
		saveCookies: function() {
//			LDSUtil.setOptions({
//				autoSave:this._autosave.get("value"),
//				autoUpdate:this._autoupdate.get("value")
//			});
//			this.btnSave.set("style", {display:LDSUtil.getOption("autoSave", true)?"none":""});
//			this.btnUpdate.set("style", {display:LDSUtil.getOption("autoUpdate", true)?"none":""});
		},
		
		isConfigExcluded: function(configId) {
			return (configId == "LDAPSync" || configId == "SE_DefaultFDS");
		},
		
		postCreate: function() {
			try {
				this.loadOptions();
			} catch(e) {
				console.log(e);
			}
			this.createProjectsPane();
			this.buildUI();
			this.getSolutions();
			dojo.subscribe("/tdi/dashboard/edit/solution", lang.hitch(this, "_subEditSolution"));
		}
	})
});
		
