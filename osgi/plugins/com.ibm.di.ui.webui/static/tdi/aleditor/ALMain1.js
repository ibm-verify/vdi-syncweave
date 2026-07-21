/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Menu",
	"dijit/MenuItem",
	"dijit/PopupMenuItem",
	"dijit/form/Button",
	"dijit/form/DropDownButton",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/layout/TabContainer",
	"dijit/Toolbar",
	"dojo/topic",
	"dijit/popup",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/aleditor/ALComponent",
	"tdi/aleditor/ALComps",
	"tdi/aleditor/ALConnection",
	"tdi/aleditor/ALDataCollector",
	"tdi/aleditor/ALEditor",
	"tdi/aleditor/ALFlow",
	"tdi/aleditor/ALScripts",
	"tdi/aleditor/ALView",
	"tdi/aleditor/Colors",
	"idx/layout/HeaderPane",
	"idx/layout/TitlePane",
	"idx/form/Link",
	"dojo/data/ItemFileWriteStore",
	"dojox/grid/DataGrid"
], function(declare, array, _Widget, _TemplatedMixin, _WidgetsInTemplate, dMenu, dMenuItem, dPopupMenuItem, Button, DropButton, BorderContainer, ContentPane, 
		TabContainer, dToolbar, dTopic, dPopup, tdiapi, tdiconfig, tdiutil, ALComponent, ALComps, ALConnection, ALDataCollector, ALEditor, ALFlow, ALScripts, ALView, TDI,
		HeaderPane, TitlePane, idxLink, ItemFileWriteStore, DataGrid) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplate ],
	{
		templateString : "<div data-dojo-attach-point='Main' style='height:100%; width:100%'></div>",
		
		// configurations
		configurations: {},
		
		// title panels for configs
		configpanels: {},
		
		// open editors
		_editors: {},

		getSolutions: function() {
			dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, function(data) {
				for(i = 0; i < data.items.length; i++) {
					this.addSolutionPanel(data.items[i].entry, data.items[i].name);
				}
				this.buildUI();
			}));
		},
		
		addSolutionPanel: function(configentry, configlabel) {
			var self = this;
			dojo.when(tdiapi.getConfig(configentry), dojo.hitch(this, function(data) {
				
				var pane = new TitlePane({
					title:configlabel, 
					open:false
				}).placeAt(this.alSolutions);
				
				var div = dojo.create("div");
				var conf = new tdiconfig({config:data});
				this.configurations[configlabel] = conf;
				var arr = conf.getAssemblyLineNames();
				for(var i = 0; i < arr.length; i++) {
					new ALView({
						config:conf.getAssemblyLine(arr[i]),
						onClick:dojo.hitch(this, "openConfiguration", configentry, configlabel, arr[i])
					}).placeAt(div);;
				}
				pane.set("content", div);
				
				dojo.connect(conf, "onModify", dojo.hitch(this, function(saveLink) {
					var link = new idxLink({
						region:"titleActions",
						label:"Save",
						onClick:dojo.hitch(self, "saveConfiguration", pane, configentry)
					}).placeAt(pane);
					pane._saveLink = link; 
				}, pane, self));
				pane.startup();
			}));
		},
		
		checkinConfig : function() {
			return dojo.when(tdiapi.checkInConfig(this.configEntry, this.config.config.solution), dojo.hitch(this, function() {
				tdiapi.unlockConfig(this.configEntry);
				this.config.setModified(false);
				this._updateButtonStates();
				this._tree.grid.setModel(this._createTreeModel());
			}), tdiapi.defaultErrHandler);
		},
		
		saveConfiguration: function(pane, configentry, event) {
			pane._saveLink.set("label", "Saving...");
			pane._saveLink.set("disabled", true);
			
			var thisObj = this;
			dojo.when(tdiapi.checkOutConfig(configentry), dojo.hitch(this, function() {
				return tdiapi.checkInConfig(configentry, this.configurations[pane.title].config.solution);
			}), function(err) {
				pane._saveLink.set("label", "Save");
				pane._saveLink.set("disabled", false);
				tdiutil.error(err);
			}).then(function() {
				tdiapi.unlockConfig(configentry);
				if(pane._saveLink) {
					pane.removeChild(pane._saveLink);
					pane._saveLink.destroyRecursive();
					pane._saveLink = null;
				}
				tdiutil.alert(pane.title + " saved successfully");
//				if(thisObj.cientry) {
//					tdiutil.confirm(thisObj.getString("WebCE.restart"), function(button) {
//						if(button == 0) {
//							dojo.when(tdiapi.stopConfig(thisObj.cientry), function() {
//								return tdiapi.startConfig(thisObj.configEntry);
//							});
//						}
//					});
//				}
			});
		},
		
		openConfiguration: function(configentry, configlabel, assemblyline, event) {
			//
			// -- Gfx editor pane (center)
			//
			var key = configlabel + "_" + assemblyline;
			var tab = null;
			if(this._editors[key]) {
				tab = this._editors[key];
			} else {
				var alEditor = new ALEditor({});
//				alEditor.startAssemblyLine = dojo.hitch(this, "startAssemblyLine");
				var tab = new ContentPane({
					title:assemblyline + "(" + configlabel + ")",
					content:alEditor,
					closable:true,
					style:"margin:0; padding:0",
					onClose: dojo.hitch(this, function() {
						delete this._editors[key];
						return true;
					})
				});
				alEditor.openConfig(configentry, configlabel, assemblyline, this.configurations[configlabel]);
				this.tabPane.addChild(tab);
				this._editors[key] = tab;
			}
			this.tabPane.selectChild(tab);
		},
		
		startAssemblyLine: function(config, configentry, configlabel, assemblyline, event) {
			var key = configlabel + "_" + assemblyline + "_" + event;
			var tab = this._editors[key];
			if(!tab) {
				
				if(event != "run" && event != "step") {
					var menu = new dMenu({});
					menu.addChild(new dMenuItem({
						label:"Run with log display (fast execution)",
						onClick:dojo.hitch(this, "startAssemblyLine", config, configentry, configlabel, assemblyline, "run")
					}));
					
					menu.addChild(new dMenuItem({
						label:"Step through the assemblyline",
						onClick:dojo.hitch(this, "startAssemblyLine", config, configentry, configlabel, assemblyline, "step")
					}));
					dPopup.open({
						parent:this,
						popup:menu,
						around: event.currentTarget,
						onExecute: function() {
							dPopup.close(menu);
						},
						onCancel: function() {
							dPopup.close(menu);
						},
						onClose: function() {
							dPopup.close(menu);
						}
					});
					return;
				} else {
					var alDataCollector = new ALDataCollector({});
					tab = new ContentPane({
						title:assemblyline + "(" + event + ")",
						content:alDataCollector,
						closable:true,
						style:"margin:0; padding:0",
						onClose: dojo.hitch(this, function() {
							delete this._editors[key];
							return true;
						})
					});
					this.tabPane.addChild(tab);
					alDataCollector.startAssemblyLine(config, configentry, assemblyline);
					this._editors[key] = tab;
				}
			}
			this.tabPane.selectChild(tab);
		},
		
		createAssemblyLine: function() {
			alert("New al dialog")
		},
		
		buildUI: function() {
			this.borderContainer = new BorderContainer({
				gutters:false,
				style:"width:100%; height:100%"
			}).placeAt(this.Main);

			//
			// -- Left border container
			//
			this.leftBorder = new BorderContainer({
				region:"leading",
				gutters:false,
				splitter:true,
				style:"width:25%; height:100%; margin:0; padding:0"
			});
			
			//
			// -- Left: Solution selection (top)
			//
			var headerPane = new HeaderPane({title:"Solutions", content:this.alSolutions, style:"height:100%; width:100%"});
			this.solutionPane = new ContentPane({
				region:"top",
				content:headerPane,
				splitter:true,
				style:"height:25%; margin:0; padding:0"
			});
			new Button({
				label:"New...",
				region:"majorActions",
				onClick:dojo.hitch(this, "createAssemblyLine")
			}).placeAt(headerPane);
			this.leftBorder.addChild(this.solutionPane);
			
			// -- Left: Sources (center)
			var headerPane = new HeaderPane({title:"Sources", content:"sources go here", style:"height:100%; width:100%"});
			this.sourcesPane = new ContentPane({
				region:"center",
				content:headerPane,
				splitter:true,
				style:"height:25%; margin:0; padding:0"
			});
			this.leftBorder.addChild(this.sourcesPane);
			
			// -- Left: Components (bottom)
			this.alComps = new ALComps({});
			this.componentsPane = new ContentPane({
				region:"bottom",
				content:this.alComps,
				splitter:true,
				style:"height:50%; margin:0; padding:0"
			});
			this.leftBorder.addChild(this.componentsPane);
			this.leftBorder.startup();
			
			this.borderContainer.addChild(this.leftBorder);
			//
			// -- Tab container for editors and run sessions
			//
			this.tabPane = new TabContainer({
				region:"center",
				splitter:true,
				style:"margin:0; padding:0"
			});
			this.borderContainer.addChild(this.tabPane);
			
			this.borderContainer.startup();
		},
		
		startup: function() {
			
			this.alSolutions = dojo.create("div");
			
			this.getSolutions();
			
			this.inherited(arguments);
		}
	})
});
		