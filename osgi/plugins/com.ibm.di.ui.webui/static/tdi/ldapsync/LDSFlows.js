/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
    	"dojo/_base/declare",
    	"dojo/_base/array",
    	"dojo/_base/lang",
    	"dojo/dom-class",
    	"dojo/dom-style",
    	"dojo/aspect",
    	"tdi/tdiconfig",
    	"dojo/data/ItemFileWriteStore",
    	"dojo/topic",
    	"dijit/_Widget",
    	"dijit/_TemplatedMixin",
    	"dijit/_WidgetsInTemplateMixin",
    	"dijit/layout/BorderContainer",
    	"dijit/layout/ContentPane",
    	"dijit/layout/TabContainer",
    	"dijit/form/Button",
    	"dijit/form/CheckBox",
    	"dijit/form/ComboBox",
    	"dijit/form/Form",
    	"dijit/form/Textarea",
    	"dijit/MenuItem",
    	"dijit/Dialog",
    	"dijit/ProgressBar",
    	"dijit/Toolbar",
    	"dijit/TooltipDialog",
    	"tdi/model/LDAPTreeStore",
    	"tdi/tdiapi",
    	"tdi/tdiconstants",
    	"tdi/atom/tdiconfigentry",
    	"tdi/atom/tdicientry",
    	"idx/dialogs",
    	"tdi/CreateSolution",
    	"tdi/DialogContent",
    	"tdi/LDAPEditor",
    	"tdi/tdiutil",
    	"tdi/RunAssemblyLineInstance",
    	"tdi/ToolbarLabel",
    	"idx/layout/HeaderPane",
    	"idx/layout/TitlePane",
    	"idx/widget/Dialog",
    	"idx/form/Link",
    	"idx/form/buttons",
    	"tdi/NlsMixin",
    	"tdi/FormWidget",
    	"tdi/config/connector",
    	"./LDSEndpoint",
    	"./LDSFlowStatus",
    	"./LDSGeneralSettings",
    	"./LDSWelcome",
    	"./LDSManageWriteBack",
    	"./LDSAttributeMaps2",
    	"./LDSLogSettings",
    	"./LDSSource",
    	"./LDSUtil",
    	"./LDSSimulation",
    	"./LDSPtaManager",
    	"./LDSMonitorSettings",
    	"dojo/text!./templates/LDSFlows.html"
    ], function(declare, array, lang, domClass, domStyle, aspect, tdiconfig, ItemFileWriteStore, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, ContentPane, TabContainer, 
    		Button, CheckBox, ComboBox, Form, Textarea, MenuItem, Dialog, ProgressBar, Toolbar, TooltipDialog, LDAPTreeStore, tdiapi, tdiconstants, tdiconfigentry, tdicientry, idx,
    		CreateSolution, DialogContent, LDAPEditor, tdiutil, RunAssemblyLineInstance, ToolbarLabel, 
    		HeaderPane, TitlePane, idxDialog, Link, idxButtons, NlsMixin, FormWidget, tdiconnector, LDSEndpoint, LDSFlowStatus, LDSGeneralSettings, LDSWelcome, LDSManageWriteBack, LDSAttributeMaps, LDSLogSettings, LDSSource, LDSUtil, LDSSimulation, LDSPtaManager, LDSMonitorSettings, template) {

return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, NlsMixin],
	{
		templateString : template,
		
		// flows: Object
		//		The flow definitions go here
		flows: {},
		deletedFlows: {},
		
		// endpoints: Object
		//		The endpoint definitions go here
		endpoints: new Array(),
		deletedEndpoints: {},
		
		// global: Object
		//		The global definitions go here
		global: {},
		
		// 
		_endpointWidgets: {},
		
		//
		_toolbarItems: {},
		
		// The suffix used by the individual ldapsync als
		syncSuffix: "_LDAPSync",
		
		// Default config name
		defaultConfigname: "SE_DefaultFDS",
		
		// Use tab layout
		useTabs: true,
		tabList: {},
		
		// auto save/update
		_autoSaveOptions: {
			autoSave: true,
			autoUpdate: true,
		},
		
		onBrowseConnection: function(config, source) {
			var title = config.getName();
			if(title == "GeneralSettings") {
				title = this.getString("FDS.browseDirectory");
			} else {
				title += " (" + this.getString("DataBrowser.title") + ")";
			}
			var browser = null;
			if(!this.hasEditorPane(title)) { 
				browser = new LDAPEditor({
					config:config,
					source:source
				});
			}
			this.showEditorPane(title, browser);
		},
		
		createFlow: function() {
			var flow = new LDSFlow({
				style:"width:600px; height:500px"
			});
			return flow;
		},
		
		addFlow: function() {
			var t = this;
			var templatePath = require.toUrl("tdi/ldapsync/templates/SEAddFlow.html") 
			var content = new DialogContent({templatePath:templatePath});
			if(content.selectWidget) {
				var items = [];
				array.forEach(t.getEndpointNames(), function(name) {
					items.push({
						id:"Source_" + name,
						name:name
					});
				});
				var store = new ItemFileWriteStore({
					data: {
						identifier: "id",
						label: "name",
						items : items
					}
				});
				content.selectWidget.setStore(store);
			}
			
			if(content.name) {
				aspect.around(content.name, "isValid", function(orgValid) {
					return function() {
						var name = content.name.get("value");
						if(!orgValid) {
							return orgValid.apply(this, arguments);
						} else if( (name.length > 0) && ( name.indexOf("<") == -1 ) && ( name.indexOf(">") == -1 ) && ( name.indexOf(" ") == -1 ) && t.config.getAssemblyLine("Flow_"+name) == null ) {
							return true;
						} else {
							return false;
						}
					}
				});
			}
			
			var dlg = new Dialog({
				title: this.getString("FDS_addFlow"),
				instruction: this.getString("FDS_addFlowInst"),
				content: content,
				reference: {
					name: "IBM",
					link: "http://www.ibm.com/"
				},
				onExecute: lang.hitch(this, function(args) {
					dlg.content.handleOK();
				}),
				onCancel: function() {
					dlg.hide();
				}
			});
			content.handleOK = function(values) {
				var name = dlg.content.name.get("value");
				var type = dlg.content.selectWidget.get("value");
				if(name && type && (name.indexOf("<") == -1) && (name.indexOf(">") == -1) && (name.indexOf("alert") == -1) && (name.indexOf(" ") == -1) && dlg.content.name.isValid()) {
					name = "Flow_" + name;
					dlg.hide();
					t.createFlowAL(type, name, true);
				}
				else return;
			};
			content.handleCancel = function() {
				dlg.hide();
			};
			
			dlg.show();
		},
		
		createFlowAL: function(endpoint, name, edit) {
			var al = this.config.createAssemblyLine(name);
			
			// -- Source
			var conn = al.createFeedConnector("Input", "Iterator");
			conn.setConnectorType("/Connectors/" + endpoint);
			conn.getAttributeMap(true).newItem({name:"*"});
			conn.setState("Disabled");
			// -- make sure we inherit hooks as well
			conn.setHookInheritance("[parent]");
			
			// -- Target
			var target = al.createDataFlowConnector("Output");
			target.setConnectorType(LDSUtil.getTargetFlowConnector());
			target.setState("Disabled");
			
			// -- Writeback
			var wb = al.createDataFlowConnector("WriteBack");
			wb.setConnectorType("system:/Connectors/ibmdi.ScriptConnector");
			wb.setState("Disabled");
			
			// -- Apply endpoint specific flow settings
			LDSUtil.applyCustomFlowSettings(conn, target);
			
			// -- Launcher
			al.createDataFlowScript("launchFlow").setInheritFrom(LDSUtil.projectName + ":/Scripts/launchFlow");
			
			// -- Create flowstatus widget for 
			this.addFlowStatus(name, edit);
		},
		
		deleteFlow: function(id) {
			var t = this;
			var name = LDSUtil.pretty(id);

			tdiutil.ask(this.getString("WebCE.del") + ": " + name, function(ok) {
				if(ok) {
					t._flowWidgets[id].deleteFlow();
					t._flowWidgets[id].destroyRecursive();
					delete t._flowWidgets[id];
					t.config.deleteAssemblyLine(id);
					t.config.deleteSchedule(id);
					t.checkEmptyConfig();
				}
			});
		},
		
		saveFlow: function(name, params) {
		},
		
		onShowGlobalProps: function() {
			this.settings = new LDSGeneralSettings({
				config:this.config,
				onBrowseConnection: lang.hitch(this, "onBrowseConnection")
			});
			this.settings.startup();
			this.showEditorPane(this.settings.getTitle(), this.settings);
		},
		
		onShowLogSettings: function() {
			this.logsettings = new LDSLogSettings({config:this.config});
			this.logsettings.startup();
			this.showEditorPane(this.logsettings.getTitle(), this.logsettings);
		},
		
		addEndpoint: function() {
			var templatePath = require.toUrl("tdi/ldapsync/templates/SEAddEndpoint.html");
			var content = new DialogContent({templatePath:templatePath});
			if(content.selectWidget) {
				var store = new ItemFileWriteStore({
					data: {
						identifier: "id",
						label: "name",
						items : LDSUtil.getSyncEngineConnectors()
					}
				});
				content.selectWidget.setStore(store);
			}
			
			var dlg = new Dialog({
				title: this.getString("FDS.addEndpoint"),
				instruction: this.getString("FDS.addEndpointInst"),
				content: content,
				reference: {
					name: "IBM",
					link: "http://www.ibm.com/"
				},
				onExecute: lang.hitch(this, function(args) {
					dlg.content.handleOK();
				}),
				onCancel: function() {
					dlg.hide();
				}
			});	

			var nameRef = dlg.content.name;
			nameRef.isValid = lang.hitch(this, function(isFocused) {
				var name = nameRef.get("value");
				if(this.config.getConnector("Source_"+name)) {
					return false;
				}
				return nameRef.validator(nameRef.textbox.value, nameRef.constraints);
			});
			content.handleOK = lang.hitch(this, function(values) {
				var name = dlg.content.name.get("value");
				var type = dlg.content.selectWidget.get("value");
				if(name && type && dlg.content.name.isValid()) {
					dlg.hide();
					this.createEndpoint(type, name);
				}
			});
			content.handleCancel = function() {
				dlg.hide();
			};
			
			dlg.show();
		},
		
		createEndpoint: function(type, defname) {
			var name = defname;
			var counter = 1;
			while(this.config.getConnector("Source_" + name) != null) {
				name = type + "_" + counter++;
			}
			
			var conn = this.config.createLibraryConnector("Source_" + name, "Iterator");
			conn.setInheritFrom(type);
			
			this.onEditEndpoint(conn);
			
			var lds = new LDSEndpoint({
				config:conn,
				editable:true,
				onEditEndpoint:lang.hitch(this, "onEditEndpoint"),
				onBrowseEndpoint:lang.hitch(this, "onBrowseConnection"),
				onDeleteEndpoint:lang.hitch(this, "onDeleteEndpoint")
			});
			lds.placeAt(this.EndpointTable).startup();
			this._endpointWidgets[conn.getName()] = lds;
			
			this.sourceEndpoints.newItem({id:"/Connectors/Source_" + name, name:name});
			topic.publish("ldapsync/endpoints");
		},
		
		onEditEndpoint: function(obj) {
			this.flow = new LDSSource({
					config:obj,
					onDelete: lang.hitch(this, "onDeleteEndpoint"),
					onBrowseConnection: lang.hitch(this, "onBrowseConnection")
				});
			this.flow.startup();
			this.showEditorPane(LDSUtil.pretty(obj.getName()), this.flow);
		},
		
		onDeleteEndpoint: function(endpoint) {
			var t = this;
			tdiutil.ask(this.getString("WebCE.del") + " " + endpoint.getName(), function(ok) {
				if(ok) {
					t._endpointWidgets[endpoint.getName()].destroyRecursive();
					t._endpointWidgets[endpoint.getName()] = null;
					t.config.deleteConnector(endpoint.getName());
					var item = t.sourceEndpoints._getItemByIdentity("/Connectors/" + endpoint.getName());
					if(item) {
						t.sourceEndpoints.deleteItem(item);
					}
					t.closeEditorPane(LDSUtil.pretty(endpoint.getName()));
					topic.publish("ldapsync/endpoints");
				}
			}, this.getString("FDS.deleteEndpoint"));
			t.checkEmptyConfig();
		},
		
		addAttributeMap: function() {
			
		},
		
		onCustomizeAttributeMaps: function() {
			var attributeMaps = new LDSAttributeMaps({config:this.config});
			attributeMaps.startup();
			this.showEditorPane(attributeMaps.getTitle(), attributeMaps);
		},
		
		onCustomizeMonitoring: function() {
			var monitor = new LDSMonitorSettings({config:this.config});
			monitor.startup();
			this.showEditorPane(this.getString("FDS.configureMonitoring"), monitor);
		},
		
		onManageWriteback: function() {
			var manageWriteBack = new LDSManageWriteBack({config:this.config});
			this.showEditorPane(manageWriteBack.getTitle(), manageWriteBack);
		},
		
		onBrowseDirectory: function() {
			this.onBrowseConnection(LDSUtil.getGeneralSettingsConnector(this.config), false, this.getString("FDS.browseDirectory"));
		},
		
		updateEndpointStatus: function(data) {
			var t = this;
			
			// -- update from test run
			if(data && data.resultEntry && data.resultEntry.attribute) {
				array.forEach(data.resultEntry.attribute, function(attr) {
					var arr = attr.name.split("_");
					if(arr && arr.length == 2) {
						var name = arr[0];
						var conn = arr[1];
						if(t._flowWidgets[name]) {
							if(conn == "SourceLDAP")
								conn = t._flowWidgets[name].source.endpoint;
							else if (conn == "TargetLDAP" && t._flowWidgets[name].target)
								conn = t._flowWidgets[name].target.endpoint;
							if(conn && t._endpointWidgets[conn]) {
								t._endpointWidgets[conn].setStatus(attr.children);
							}								
						}
					}
				});
			}
		},
		
		checkALRunStatus : function(err) {
			var t = this;
			if(this.alentry) {
				dojo.when(this.alentry.getStatus(), lang.hitch(this, function(data) {
					tdiapi.getResultEntry(this.alentry).then(function(data) {
						t.debugMsg("Result");
						t.debugMsg(data);
					});
				}), dojo.hitch(this, function(err) {
					// no longer running most likely
					t.debugMsg(err);
				}));
			}
		},

		checkConfigLock: function(project) {
			return dojo.when(tdiapi.getConfigEntry(project), function(centry) {
				if(array.some(centry.atom.category, function(cat) {
					return cat.term == "locked";
				})) {
					return tdiapi.unlockConfig(centry);
				} else {
					return null;
				}
			});
		},
		
		readConfigFile: function(project) {
			var t = this;
			return t.checkConfigLock(project).then(function(result) {
				return t.loadConfigFile(project);
			});
		},
		
		loadConfigFile: function(project) {
			var t = this;
			if(t.closeCurrentConfig()) {
				return dojo.when(tdiapi.getConfigEntry(project), function(centry) {
					t.configEntry = centry;
					tdiapi.getConfig(centry).then(function(data) {
						t.config = new tdiconfig({config:data});
						// TODO: Uncomment when server api includes this folder
//						if(!t.config.getInclude("LDAPSync")) {
//							t.config.createInclude("LDAPSync", "LDAPSync");
//						}
						t._modid = aspect.after(t.config, "onModify", lang.hitch(t, function(modified) {
							if(t._timer) {
								clearTimeout(t._timer);
							}
							if(t._autoSaveOptions.autoSave) {
								t.statusLabel.setLabel("<span style='color:red'>*****</span>");
								t._timer = setTimeout(lang.hitch(this, "doAutoSave"), 3*1000);
							} else {
								t.onConfigModified();
							}
						}));
						t.currentConfig = project;
						t.updateTables();
						t.btnSave.set("disabled", true);
						t.btnUpdate.set("disabled", true);
						t.statusLabel.setLabel("");
						dojo.cookie("TDI.LDAPSync.lastsolution", project);
						t.getCurrentConfigStatus();
						t.loadSnapshots();
					}, tdiapi.defaultErrHandler);
				});
			}
		},
		
		createConfig: function() {
			var t = this;
			tdiutil.openDialog(null, this.getString("FDS.createProject"), "CreateJob.html", function(result) {
				if(result && result.name && result.name.trim().length > 0) {
					var configname = "SE_" + result.name.trim();
					LDSUtil.createConfig(configname).then(
						function ok(data) {
							if(data && data == "OK") {
								dojo.cookie("TDI.LDAPSync.lastsolution", configname);
								t.reloadServerProjects();
							}
						},
						function err(error) {
							tdiutil.error(error);
						}
					);
				}
			});
		},
		
		/*
		 * ===============================
		 * FIX #2 — SAFE @type ASSIGNMENT
		 * ===============================
		 */
		saveConfig: function() {
			var t = this;
			
			t.config.getTombstoneSettings().setParam("AssemblyLines", "true");
			t.config.getTombstoneSettings().setParam("Configuration", "true");
			
			var solution = t.config.config.solution;
			
			if (solution && solution.container) {
				array.forEach(solution.container, function (container) {
					
					if (container.name === "AssemblyLines" && container.config) {
						array.forEach(container.config, function (al) {
							if (!al.container) return;
							
							array.forEach(al.container, function (flowContainer) {
								
								if (!flowContainer["@type"]) {
									flowContainer["@type"] = "ALComponentsBinding";
								}
								
								if (!flowContainer.component) return;
								
								array.forEach(flowContainer.component, function (comp) {
									
									/*
									 * ONLY tag CONNECTOR components
									 */
									if (
										!comp["@type"] &&
										(comp.name === "Input" ||
										 comp.name === "Output" ||
										 comp.name === "WriteBack" ||
										 comp.name === "Augment")
									) {
										comp["@type"] = "ALComponentBinding";
									}
									
									if (comp.connectionConfig && !comp.connectionConfig["@type"]) {
										comp.connectionConfig["@type"] = "ConnectionConfigBinding";
									}
									
									if (comp.complexConfig && comp.complexConfig.map) {
										array.forEach(comp.complexConfig.map, function (map) {
											if (!map["@type"]) {
												map["@type"] = "AttributeMapBinding";
											}
											if (map.item && lang.isArray(map.item)) {
												array.forEach(map.item, function (item) {
													if (!item["@type"]) {
														item["@type"] = "AttributeMapItemBinding";
													}
												});
											}
										});
									}
								});
							});
						});
					}
					
					// ===============================
					// FIX: Library Connectors (Source_*, GeneralSettings, etc.)
					// ===============================
					if (container.name === "Connectors" && container.config) {
						console.log("✅ Processing Library Connectors - COUNT:", container.config.length);
						array.forEach(container.config, function (conn) {
							console.log("  - Connector:", conn.name, "Type:", conn["@type"]);
							if (!conn["@type"]) {
								conn["@type"] = "ConnectorBinding";
							}
							
							if (conn.connectionConfig && !conn.connectionConfig["@type"]) {
								conn.connectionConfig["@type"] = "ConnectionConfigBinding";
							}
							
							if (conn.map) {
								array.forEach(conn.map, function (map) {
									if (!map["@type"]) {
										map["@type"] = "AttributeMapBinding";
									}
									if (map.item && lang.isArray(map.item)) {
										array.forEach(map.item, function (item) {
											if (!item["@type"]) {
												item["@type"] = "AttributeMapItemBinding";
											}
										});
									}
								});
							}
						});
					}
				});
				 console.log("=== SAVECONFIG: Saving to server ===");
			}
			
			t.checkConfigLock(t.currentConfig).then(function() {
				return tdiapi.checkOutConfig(t.configEntry);
			}).then(function(data) {
				return tdiapi.checkInConfig(t.configEntry, solution);
			}).then(function(data) {
				return tdiapi.unlockConfig(t.configEntry);
			}).then(function(data) {
				t.onConfigSaved();
				// -- give server time to update internal namespace
				setTimeout(function() {
					dojo.publish(tdiconstants.serverEventsSubject, [{type:"di.ci.file.reloaded", ciId:t.currentConfig}]);
				}, 1500);
			}, function(err) {
				// Error handler for the promise chain
				t.debugMsg("Save error: " + err);
				tdiutil.error("Failed to save configuration: " + err);
			})
		},
		
		getEndpointNames: function() {
			var result = new Array();
			array.forEach(this.config.getConnectorNames(), function(conn) {
				var arr = /^(Source_)(.*)/.exec(conn);
				if(arr && arr.length == 3) {
					arr[2]=arr[2].replace(/ /g, "%20");
					result.push(arr[2]);
				}
			});
			return result.sort();
		},
		
		updateTables: function() {
			var t = this;
			var carr = new Array();
			array.forEach(this.config.getConnectorNames(), function(conn) {
				var arr = /^(Source_)(.*)/.exec(conn);
				if(arr && arr.length == 3) {
					var obj = t.config.getConnector(conn);
					var lds = new LDSEndpoint({
						config:obj,
						editable:true,
						onEditEndpoint:lang.hitch(t, "onEditEndpoint"),
						onBrowseEndpoint:lang.hitch(t, "onBrowseConnection"),
						onDeleteEndpoint:lang.hitch(t, "onDeleteEndpoint")
					});
					lds.placeAt(t.EndpointTable).startup();
					t._endpointWidgets[conn] = lds;
					carr.push(conn);
				}
			});
			
			//
			// -- Run a connection test on all endpoints after loading a config
			//
			setTimeout(lang.hitch(t, "testConnections"), 3000);
			
			t.loadEndpointDefs(carr);

			var data = new Array();
			array.forEach(this.config.getAssemblyLineNames(), function(conn) {
				var arr = /^(Flow_)(.*)/.exec(conn);
				if(arr && arr.length == 3) {
					t.addFlowStatus(conn);
				}
			});

			
			// -- bring up help bubbles if needed
			t.checkEmptyConfig();
			
			// -- check writeback schedule now
//			t.checkSchedule();
			
			this.tabContainer.watch("selectedChildWidget", function(name, oval, nval){
				t.checkEmptyConfig(nval);
			});
		},
		
		addFlowStatus: function(alid, edit) {
			var t = this;
			var obj = t.config.getAssemblyLine(alid);
			var item = {
				id:alid,
				config:obj
			};
			t._flowWidgets[alid] = new LDSFlowStatus({
				item:item,
				onDeleteFlow:lang.hitch(this, "deleteFlow"),
				endpoints:{sources:t.sourceEndpoints, targets:t.targetEndpoints},
				parent:this
			}).placeAt(t.TableDiv);
			
			if(edit) {
				t._flowWidgets[alid]._onEditFlow();
			}
		},
		
		getSource: function(al) {
			var conn = al.getConnector("Input");
			if (conn && typeof conn.getConnectionConfig === "function") {
				return conn.getConnectionConfig().getParam("assemblyLine");
			}
			return "";
		},
		
		/*
		 * ===============================
		 * FIX #1 — CORRECT CONNECTOR API
		 * ===============================
		 * Output is a CONNECTOR, not a component.
		 * With @type enabled, getComponentByName() will NOT work.
		 */
		getTarget: function(al) {
			var conn = al.getConnector("Output");
			if (conn && typeof conn.getConnectionConfig === "function") {
				return conn.getConnectionConfig().getParam("assemblyLine");
			}
			return "";
		},
		
		hasEditorPane: function(title) {
			return this.tabList[title];
		},
		
		showEditorPane: function(title, content, style) {
			this.showEditorPaneTab(title, content, style);
		},
		
		showEditorPaneTab: function(title, content, domstyle) {
			var t = this;
			for(f in t.tabList) {
				if(title == f && t.tabList[f] == content) {
					t.tabContainer.selectChild(t.tabList[f]);
					return;
				}
			}
			
			if(!t.tabList[title]) {
				var cp = new ContentPane({
					title:title,
					content:content,
					closable:true,
					onClose:function() {
						if(content.handleOnClose) {
							content.handleOnClose();
						}
						delete t.tabList[title];
						t.checkEmptyConfig();
						return true;
					},
					style:domstyle ? domstyle : "width:100%; height:100%;"
				}) ;
				t.tabList[title] = cp;
				t.tabContainer.addChild(cp);
			}
			t.tabContainer.selectChild(t.tabList[title]);
		},
		
		deleteEditorPane: function() {
			
		},
		
		closeEditorPane: function(title) {
			var t = this;
			if(t.tabList[title]) {
				t.tabContainer.removeChild(t.tabList[title]);
				t.tabList[title] = null;
			}
			this.checkEmptyConfig();
		},
		
		closeCurrentConfig: function() {
			for(f in this._endpointWidgets) {
				this._endpointWidgets[f].destroyRecursive();
			};
			this._endpointWidgets = {};
			
			if(this._modid && this._modid.remove) {
				this._modid.remove();
				this._modid = null;
			}
			
			for(f in this._flowWidgets) {
				this._flowWidgets[f].destroyRecursive();
			};
			this._flowWidgets = {};
			
			return true;
		},
		
		loadEndpointDefs: function(arr) {
			var sources = new Array();
			var targets = new Array();
			array.forEach(arr, function(name) {
				var arr = /^(Source_)(.*)/.exec(name);
				if(arr && arr.length == 3)
					sources.push({name:arr[2], id:"/Connectors/" + name});
				else
					targets.push({name:name, id:"/Connectors/" + name});
			});
			
			this.addSyncEngineEndpoints(sources, targets);
			
			this.sourceEndpoints = new ItemFileWriteStore({data:{label:"name", identifier:"id", items:sources}});
			this.targetEndpoints = new ItemFileWriteStore({data:{label:"name", identifier:"id", items:targets}});
			
		},
		
		addSyncEngineEndpoints: function(sources, targets) {
			try {
				var ns = "LDAPSync";
				array.forEach(tdiapi.getNamespace(ns).getConnectorNames(), function(al) {
					var arr = /^(Target_)(.*)/.exec(al);
					if(arr && arr.length == 3) {
						var item = {
							id:ns+":/Connectors/" + al,
							name:arr[2] + " (" + ns + ")"
						}
						targets.push(item);
					}
				});
			} catch(err) {
				tdiutil.error(err);
			}
		},
		
		reloadServerProjects: function() {
			var t = this;
			tdiapi.getServerProjects().then(function(data) {
				data.items = data.items.sort(function(a,b) {
					var v1 = a.name.toLowerCase();
					var v2 = b.name.toLowerCase();
					return v1.localeCompare(v2);
				});
				data.items = array.filter(data.items, function(item) {
					return item.name.substring(0,3) == "SE_";
				});
				data.items = array.map(data.items, function(item) {
					item.name = item.name.substring(3);
					item.label = item.name.substring(3);
					item.value = item.name;
					return item;
				});
				var store = new ItemFileWriteStore({data:data});
				var last = dojo.cookie("TDI.LDAPSync.lastsolution");
				t.project.setStore(store, last);
				
				if(data.items.length == 0) {
					t.promptCreateProject();
					t.showWelcomePage();
				} else if(data.items.length == 1) {
					t.project.set("style", {display:"none"});
					t.projectlabel.set("style", {display:"none"});
					t.btnNewProject.set("style", {display:"none"});
				} else {
					t.project.set("style", {display:""});
					t.projectlabel.set("style", {display:""});
					t.btnNewProject.set("style", {display:""});
				}
				
			});
		},
		
		promptCreateProject: function() {
			var t = this;
			LDSUtil.createConfig(t.defaultConfigname).then(
				function ok(data) {
					if(data && data == "OK") {
						dojo.cookie("TDI.LDAPSync.lastsolution", t.defaultConfigname);
						t.reloadServerProjects();
					}
				},
				function err(error) {
					tdiutil.error(error);
				}
			);
		},
		
		addToolbarItem: function(name, item) {
			this._toolbar.addChild(item);
			this._toolbarItems[name] = item;
		},
		
		getToolbarItem: function(name) {
			return this._toolbarItems[name];
		},
		
		removeToolbarItem: function(name) {
			var child = this._toolbarItems[name];
			if(child) {
				this._toolbar.removeChild(child);
				this._toolbarItems[name] = null;
			}
		},
		
		doAutoSave: function() {
			var now = new Date();
			try {
				this.saveConfig();
				this.statusLabel.setLabel(
					this.getString("FDS.autoSaved",  [
					  now.getHours() + ":" + (now.getMinutes() > 9 ? now.getMinutes() : "0" + now.getMinutes()) + ":" +
						(now.getSeconds() > 9 ? now.getSeconds() : "0" + now.getSeconds())
				    ]
				));
			} catch(err) {
				this.debugMsg(err);
				this._timer = setTimeout(lang.hitch(this, "doAutoSave"), 3*1000);
			}
		},
		
		getCurrentConfigStatus: function() {
			var t = this;
			
			tdiapi.getConfigInstance(t.currentConfig).then(function(data) {
				var cientry = new tdicientry({atom:data});
				tdiapi.getAssemblyLineList(cientry).then(function(data) {
					if(lang.isArray(data.entry)) {
						array.forEach(data.entry, function(item) {
							var arr = item.title.value.match(/AssemblyLines\/(.*)\.(\d*)/);
							if(arr && arr.length == 3) {
								var id = item.id.split("/");
								var event = {
									type:"di.al.start",
									id:"AssemblyLines/" + arr[1],
									data: {
										value:id[id.length-1]
									},
									ciId:t.currentConfig
								};
								dojo.publish(tdiconstants.serverEventsSubject, [event]);
							}
						});
					}
				});
			}, function notrunning() {
				tdiapi.startConfig(t.configEntry);
			});
		},
		
		showWelcomePage: function() {
			this.showEditorPane(this.getString("FDS.welcomePageTitle"), new LDSWelcome());
		},
		
		onManagePta: function() {
			this.showEditorPane(this.getString("FDS.configurePTA"), new LDSPtaManager({
				config:this.config
			}), "width:100%; height:100%; padding:0; border:0");
		},
		
		checkEmptyConfig: function() {
			var t = this;
			
			var selectedWidget = t.tabContainer.selectedChildWidget;
			
			// -- First check if SDS has a URL defined
			var cfg = t.config.getConnector(LDSUtil.generalSettingsConn);
			var url = null;
			if(cfg) {
				if(LDSUtil.getCustomTarget()) {
					url = cfg.getConnectionConfig().getNames().length > 0 ? "dummy" : "";
				} else {
					url = cfg.getConnectionConfig().getParam("target.ldap.url");
				}
			}
			if(!url || url == "") {
				t.showHelp(t._helpSDS);
				return t.getString("FDS.preConfigureSDS");
			}
			
			// -- Next check if we have endpoints
			if(!array.some(t.config.getConnectorNames(), function(name) {
				return name.match(/^Source_/);
			})) {
				t.showHelp(t._helpEndpoint);
				return t.getString("FDS.preConfigureEP");
			}
			
			
			// -- Finally check if there are flows defined
			// -- But only show if Flow tab has focus
			if(selectedWidget && selectedWidget.title == this.getString("FDS.flows")) {
				if(t.config.getAssemblyLineNames().length == 0) {
					t.showHelp(t._helpFlow);
					return t.getString("FDS.preConfigureFlow");
				}
			}
			
			t.showHelp(null);
			
			return null;
		},
		
		showHelp: function(widget) {
			// summary:
			//		Show the help hover widget
			//		Due to problems with IE10 and idx.widget.HelpHover we have to
			//		load and create it on demand. On IE10 this will fail and no help
			//		hover is displayed.
			var t = this;
			if(widget && !widget.hoverHelp) {
				try {
					require(["idx/widget/HoverHelp"], function(help) {
						widget.hoverHelp = new help({
							message:widget.getAttribute("message")
						}).placeAt(widget);
						widget.hoverHelp.startup();
					})
				} catch(err) {
				}
			}
			var arr = [this._helpSDS, this._helpEndpoint, this._helpFlow];
			array.forEach(arr, function(w) {
				if(w.hoverHelp) {
					domStyle.set(w, "display", "none");
					w.hoverHelp._button.closeDropDown();
				} else if(dojo.isIE) {
					w.innerHTML = "";
					domClass.remove(w, "tdiDropBox");
				}
			});
			if(widget && widget.hoverHelp) {
				setTimeout(function() {
					domStyle.set(widget, "display", "");
					t.Header.resize();
					widget.hoverHelp._doOpen();
				}, 1000);
			} else if (widget && dojo.isIE) {
				widget.innerHTML = widget.getAttribute("message");
				domClass.add(widget, "tdiDropBox");
			}
		},
		
		testConnections: function() {
			this.debugMsg("testConnections");
			var t = this;
			for(var id in t._endpointWidgets) {
				t._endpointWidgets[id].testConnection();
			}
			LDSUtil.testDirectoryServerConnection(t.config);
		},
		
		isWriteBackEnabled: function() {
			var gs = LDSUtil.getGeneralSettingsConnector(this.config);
			return gs.getConnectionConfig().getParamBoolean("writeback.enabled", false);
		},
		
		checkSchedule: function() {
			// summary:
			//		If the target config is LDAPSync then check if we need to
			//		start/stop the WriteBack scheduler.
			if(LDSUtil.getTargetProjectName() != "LDAPSync")
				return;
			
			var t = this;
			
			// 
			if(t.isWriteBackEnabled() == t.lastWriteBackEnabled)
				return;
			
			tdiapi.getActiveSchedules().then(function(data) {
				var arr = data[LDSUtil.getTargetProjectName()];
				if(lang.isArray(arr)) {
					var arr = array.filter(arr, function(sched) {
						return sched.assemblyLineName == LDSUtil.writebackAL;
					});
					var running = arr && arr.length == 1;
					if(running && t.isWriteBackEnabled()) {
						t.lastWriteBackEnabled = t.isWriteBackEnabled();
						return; // all ok
					} else if(!running && t.isWriteBackEnabled()) {
						// Start it
						tdiapi.startSchedule("LDAPSync", "WriteBackMain KeepAlive");
					} else if(running && !t.isWriteBackEnabled()) {
						// Stop it
						tdiapi.stopSchedule("LDAPSync", "WriteBackMain KeepAlive");
					}
					t._updateWritebackStatus(t.ds_status.title == "" || t.ds_status.title == t.getString("FDS.connectionOK"));
				}
			});
		},
		
		saveSnapShot: function(title) {
			var t = this;
			LDSUtil.saveSnapshot(this.currentConfig, title).then(function(data) {
				t.loadSnapshots(data.config);
			});
		},
		
		loadSnapShot: function(path) {
			if(!path)
				return;
			
			var t = this;
			idx.confirm(this.getString("FDS.revertTo") + "<p>" + path, function(ok) {
				LDSUtil.loadSnapshot(t.currentConfig, path).then(function() {
					t.readConfigFile(t.currentConfig);
				});
			});
		},
		
		deleteSnapShot: function(path) {
			if(!path)
				return;

			var t = this;
			idx.confirm(this.getString("general.delete.label") + "<p>" + path, function(ok) {
				LDSUtil.deleteSnapshot(path).then(function() {
					t.loadSnapshots();
				});
			});
		},
		
		setSnapshots: function(select) {
			this._snapshots = select;
		},
		
		loadSnapshots: function(selection) {
			var t = this;
			LDSUtil.listSnapshots(this.currentConfig).then(function(data) {
				var store = new ItemFileWriteStore({
					data: {
						identifier: "config",
						label: "description",
						items : data
					}
				});
				t._snapshots.setStore(store, selection);
			});
		},
		
		updateFDS: function() {
			// summary:
			//		Reloads the in-memory config with the newer version on disk
			var t = this;
			tdiapi.reloadConfig(t.currentConfig).then(function(data) {
				t.btnUpdate.set("disabled", true);
				dojo.publish(tdiconstants.serverEventsSubject, [{type:"di.ci.file.updated", ciId:t.currentConfig}]);
				t.onFDSUpdated();
			});
		},
		
		updateToolbar: function() {
//			this.btnSave.set("style", {display:this._autoSaveOptions.autoSave?"none":""});
//			this.btnUpdate.set("style", {display:this._autoSaveOptions.autoUpdate?"none":""});
		},
		
		onConfigSaved: function() {
			this.btnSave.set("disabled", true);
			if(this._autoSaveOptions.autoUpdate) {
				this.updateFDS();
			} else {
				this.btnUpdate.set("disabled", false);
			}
			// -- check writeback schedule now
			this.checkSchedule();
			topic.publish("ldapsync/configsave", this.config);
		},
		
		onFDSUpdated: function() {
			
		},
		
		onConfigModified: function() {
			this.btnSave.set("disabled", false);
		},
		
		onAutoSaveModified: function(options) {
			this._autoSaveOptions = options;
			this.updateToolbar();
		},
		
		handleSimulateMessage: function(event) {
			var t = this;
			var json = dojo.fromJson(event.data.value);
			var title = event.id + " (" + t.getString("FDS.runSimulateTitle") + ")";
			if(json.state == "starting") {
				var simulate = new LDSSimulation({});
				t.showEditorPaneTab(title, simulate);
			} else {
				var tab = t.tabList[title];
				if(tab && tab.content && tab.content.simulateEvent) {
					tab.content.simulateEvent(event);
				}
			}	
		},
		
		startupCheck: function() {
			var t = this;
			
			// -- Check for custom target
			t.customTargetCheck();
			
			// -- verify that LDAPSync is accessible
			tdiapi.getConfigEntry(LDSUtil.projectName).then(function ok() {
				idx.hideProgressDialog();
				t.performStartup();
			}, function fail() {
				t.debugMsg("LDAPSync not available - retrying in 3 seconds")
				idx.showProgressDialog(t.getString("StartLocalServerAction.starting"));
				setTimeout(lang.hitch(t, "startupCheck"), 3*1000);
			});
		},
		
		customTargetCheck: function() {
			var t = this;
			
			var arr = window.location.href.match(/.*target=(\w*)/);
			if(arr && arr.length == 2) {
				var customTarget = arr[1];
				if(customTarget != "LDAPSync") {
					LDSUtil.setCustomTarget(customTarget);
					domStyle.set(t._directoryConfigTable, "display", "none");
					domStyle.set(t._customConfigTable, "display", "");
					this._dsHeaderPane.set("title", t.getString("FDS.customTargetConfig"));
					t.setCustomTargetTitle();
				}
				return;
			}

			//
			// -- Check if there is a FDS_Target
			//
			tdiapi.getServerProjects().then(function(data) {
				if(data && lang.isArray(data.items)) {
					if(array.some(data.items, function(item) {
						return item.name == LDSUtil.projectNameCustomTarget;
					})) {
						LDSUtil.setCustomTarget(LDSUtil.projectNameCustomTarget);
						domStyle.set(t._directoryConfigTable, "display", "none");
						domStyle.set(t._customConfigTable, "display", "");
						t._dsHeaderPane.set("title", t.getString("FDS.customTargetConfig"));
						t.setCustomTargetTitle();
					}
				}
			});
		},
		
		setCustomTargetTitle: function() {
			try {
				var targetConfig = tdiapi.getNamespace(LDSUtil.getTargetProjectName());
				if(targetConfig) {
					var title = targetConfig.getSolutionInterface().getUserComment();
					if(title && title.length > 0) {
						this._dsHeaderPane.set("title", title);
					}
				}
			} catch(e) {
			}
		},
		
		performStartup: function() {
			var t = this;
			t.inherited(arguments);
			t._flowWidgets = new Object();
			t._endpointWidgets = new Object();
			LDSUtil.loadSyncEngineLabels();
			t.reloadServerProjects();
			
			this.own(tdiapi.subscribeServerEvents(function(event) {
				if (event.type == "user.fds.simulate") {
					t.handleSimulateMessage(event);
				} else if (event.id == "SE_DefaultFDS.GeneralSettings" && event.type == "user.fds.testconnection") {
					var json = dojo.fromJson(event.data.value);
					t._enableTargetButtons(json.status == "success");
					if(json.status == "success") {
						t.ds_status.src = "/fds/static/images/validate.gif"; 
						t.ds_status.title = t.getString("FDS.connectionOK");
						t.ds_status_custom.src = "/fds/static/images/validate.gif"; 
						t.ds_status_custom.title = t.getString("FDS.connectionOK");;
					} else {
						t.ds_status.src = "/fds/static/images/st24_critical.gif";
						t.ds_status.title = json.message || json.exception;
						t.ds_status_custom.src = "/fds/static/images/st24_critical.gif";
						t.ds_status_custom.title = json.message || json.exception;
					}
					if(typeof(json.changelog) == "undefined") {
						json.changelog = true;
					}
					t._changelogMissing = json.changelog;
					t._updateWritebackStatus(json.status == "success");
				}
			}));
			
			t.resize();
		},
		
		_updateWritebackStatus: function(directoryServerAvailable) {
			// summary:
			//		If the directory server is available we check for the "changelog" attribute
			//		in the DS' root DSE. If write-back is not "available" we set a tooltip to
			//		reflect what the problem is.
			var error = null;
			var t = this;
			
			// -- Save this for later so we can check if we should go through
			// -- and stop/start the writeback assemblyline schedule
			t.lastWriteBackEnabled = t.isWriteBackEnabled();
			
			if(!t.isWriteBackEnabled()) {
				t.wb_status.style.display = "none";
				
			} else if(!directoryServerAvailable) {
				t.wb_status.style.display = "";
				t.wb_status.src = "/fds/static/images/st24_critical.gif";
				t.wb_status.title = this.getString("FDS.checkConnectionSettings");
				
			} else {
				var config = LDSUtil.getGeneralSettingsConnector(this.config);
				var params = {"target.ldap.url":"", "target.ldap.user":"", "target.ldap.password":""};
				for(var f in params) {
					params[f] = config.getConnectionConfig().getParam(f);
				}
				var store = new LDAPTreeStore({ldap:params});
				store.readEntry("").then(function(data) {
					data = lang.isArray(data) ? data.pop() : data;
					t.wb_status.style.display = "";
					if(!data.changelog) {
						t.wb_status.src = "/fds/static/images/st24_critical.gif";
						t.wb_status.title = t.getString("FDS.changelogNotEnabled");
					} else {
						t.wb_status.src = "/fds/static/images/validate.gif";
						t.wb_status.title = "";
					}
				})

			}
		},
		
		_enableTargetButtons: function(enable) {
			// summary:
			//		Sets the disabled state for those buttons/links that rely
			//		on a connected target server (e.g. directory settings).
			this._configPtaBtn.set("disabled", !enable);
			this._browseDirectoryBtn.set("disabled", !enable);
			
			// set a tooltip when it's not availale
			this._configPtaBtn.set("title", enable ? "" : this.getString("FDS.checkConnectionSettings")); 
			this._browseDirectoryBtn.set("title", enable ? "" : this.getString("FDS.checkConnectionSettings")); 
		},
		
		onBeforeUnload: function() {
			// summary:
			//		called before the page is unloaded.
			//		return warning if there are pending changes
			//		so user can choose to abandon or not.
			if(!this.btnSave.get("disabled")) {
				return this.getString("FDS.abandonChanges");
			}
			try {
				tdiapi.stopServerEventNotifications();
			} catch(err) {
				; // -- totally ignore any errors here
			}
			return null;
		},
		
		debugMsg: function(msg) {
			//
		},
		
		/*
		onShowEditor: function() {
			var ed = new OrionEditor();
			this.showEditorPane("Example Orion Editor", ed);
		},
		*/
		
		resize: function(obj) {
			this.inherited(arguments);
		},
		
		_onBrowseTarget: function(params) {
			var config = LDSUtil.getGeneralSettingsConnector(this.config);
			var title = params.title || this.getString("FDS.browseDirectory");
			var browser = null;
			if(!this.hasEditorPane(title)) {
				browser = new LDAPEditor({
					config:config,
					configOverride:params
				});
			}
			this.showEditorPane(title, browser);
		},
		
		startup: function() {
			this.inherited(arguments);
			// -- make sure this is disabled to avoid
			// -- annoying confirmation dialog on leaving page
			this.btnSave.set("disabled", true);
			
			// Disable buttons that talk to the target server
			// until a successful test connections is received
			this._enableTargetButtons(false);
			
			// subscribe to topics 
			topic.subscribe("ldapsync/autosave", lang.hitch(this, "onAutoSaveModified"))
			topic.subscribe("ldapsync/browsetarget", lang.hitch(this, "_onBrowseTarget"))
			
			// Proceed with initialization
			this.startupCheck();
		}
	})
});
