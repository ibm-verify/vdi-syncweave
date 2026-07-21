dojo.provide("tdi.Dashboard");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.TitlePane");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.form.TextBox");

// These cause a problem with Firefox
//dojo.require("dojoe.multimessagearea.MultiMessageArea");
//dojo.require("dojoe.messagearea.MessageArea");

dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiutil");
dojo.require("tdi.model.ServerProjectsModel");

dojo.require("tdi.ConfigEditor");
dojo.require("tdi.ConfigInstance");
dojo.require("tdi.CreateSolution");
dojo.require("tdi.Databrowser");
dojo.require("tdi.EasyETL");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.RunAssemblyLineInstance");
dojo.require("tdi.ServerInfo");
dojo.require("tdi.ServerInfoSmall");
dojo.require("tdi.ServerProjects");
dojo.require("tdi.UploadSolution");
dojo.require("tdi.Welcome");

dojo.declare("tdi.Dashboard", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		This widget provides a list of configuration files
	//		and running instances (optional) on a server in the left
	//		navigator panel. The right hand part of this widget contains
	//		the editor area, where individual editors are shown.
	//
	
	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/Dashboard.html"),
	widgetsInTemplate : true,
	
	// Map of instances we show
	_map : null,

	// Map of config files we edit
	_configmap: null,
	
	// Subscriptions
	_subscriptions: null,

	constructor : function(/* Object */args) {
		dojo.safeMixin(this, args);
		this._map = {};
		this._configmap = {};
		this._subscriptions = [];
	},
	
	_showAbout : function() {
		var key = "__about__";
		if (this._map[key] == null) {
			var cci = new tdi.ServerInfo({});
			var child = new dijit.layout.ContentPane({
				title : this.getString("serverInfo"),
				content : cci,
				closable : true,
				uniqueId: key,
				iconClass: "activeALIcon",
				style:"height:95%",
				onClose : dojo.hitch(this, "closeALTab")
			});
			this.TabContainer.addChild(child);
			this._map[key] = child;
			child.startup();
		}
		this.TabContainer.selectChild(this._map[key]);
	},

	_openAssemblyLine : function(cientry, assemblyline) {
		var key = cientry.getId();
		if (this._map[key] == null) {
			var cci = new tdi.RunAssemblyLineInstance({
				cientry : cientry,
				logoptions : "small",
				assemblyline : assemblyline
			});
			var child = new dijit.layout.ContentPane({
				title : cientry.getTitle(),
				content : cci,
				closable : true,
				uniqueId: key,
				iconClass: "activeALIcon",
				onClose : dojo.hitch(this, "closeALTab")
			});
			this.TabContainer.addChild(child);
			this._map[key] = child;
		}
		this.TabContainer.selectChild(this._map[key]);
	},
	
	_openConfigInstance : function(item, config) {
		var key = null;
		var title = null;
		if(item != null) {
			key = item.getId();
			title = item.getTitle();
		} else {
			key = config.getId();
			title = config.getTitle();
		}
		
		// Always use the same identifier for config instances
		key = key.replace("config/e%3A", "ci/");
		
		if (this._map[key] == null) {
			var cci = new tdi.ConfigInstance({
				cientry : item,
				configEntry: config
			});
			var child = new dijit.layout.ContentPane({
				title : title,
				content : cci,
				closable : true,
				uniqueId: key,
				iconClass: "activeALIcon",
				onClose : dojo.hitch(this, "closeCITab")
			});
			this.TabContainer.addChild(child);
			this._map[key] = child;
		}
		this.TabContainer.selectChild(this._map[key]);
	},

	_reopenConfigEditor : function(item) {
		
	},
	
	openConfig : function() {
		dojo.forEach(this.Projects.getSelectedRows(), dojo.hitch(this, function(row) {
			var entry = row.entry[0];
			if(entry != null) {
				this._openConfigEditor(entry);
			}
		}));
	},
	
	_unlockConfig : function(entry) {
		dojo.when(tdiapi.unlockConfig(entry), 
				dojo.hitch(this, function(data) {
					this.Projects.refresh();
					tdiutil.alert(this.getString("solutionUnlocked"));
				}),
				function(err) {
					tdiutil.error(err)
				}
		);
	},
	
	_onConfigLoad : function(item, config) {
		var key = item.getId();
		var cfg = new tdi.tdiconfig({config:config});
		var cci = new tdi.ConfigEditor({
			config:cfg,
			configEntry : item
		});
		
		var child = new dijit.layout.ContentPane({
			title : item.getTitle(),
			content : cci,
			uniqueId: key,
			closable : true,
			iconClass: "tdiConfigImage",
			onClose : dojo.hitch(this, "closeConfigTab")
		});
		this.TabContainer.addChild(child);
		this._configmap[key] = child;
		this.TabContainer.selectChild(this._configmap[key]);
	},
	
	_openConfigEditor : function(item) {
		var key = item.getId();
		if (this._configmap[key] == null) {
			return dojo.when(tdiapi.getConfig(item), dojo.hitch(this, "_onConfigLoad", item), tdiapi.defaultErrHandler);
		} else {
			this.TabContainer.selectChild(this._configmap[key]);
		}
	},
	
	_stopConfigInstance : function(cientry, configentry) {

		var active = new Array();
		var thisObj = this;
		dojo.when(cientry.getLinkData("assembly-line"), function(data) {
			dojo.forEach(data.entry, function(e) {
				active.push(e.title.value);
			});
			return tdiapi.getActiveSchedules();
		}).then(dojo.hitch(this, function(data) {
			dojo.forEach(data[cientry.atom.title.value], function(sched) {
				var d = new Date(sched.nextRun);
				active.push(sched.assemblyLineName + " (<small>" + d + "</small>)");
			})
			
			if(active.length > 0) {
				tdiutil.confirm(thisObj.getString("WebCE.activeScheduledAL") + "<p>" + active.join("<br>"), function(button) {
					if(button == 0)
						thisObj._stopConfigInstanceNP(cientry, configentry);
				});
			} else {
				this._stopConfigInstanceNP(cientry, configentry);
			}
		}));
	},
	
	_stopConfigInstanceNP : function(cientry, configentry) {
		dojo.when(tdiapi.stopConfig(cientry), dojo.hitch(this, function() {
			//tdiutil.alert(this.getString("solutionStopped"));
		}), tdiapi.defaultErrHandler);
	},

	_startConfigInstance : function(cientry, configentry) {
		this._openConfigInstance(null, configentry);
		dojo.when(tdiapi.startConfig(configentry), dojo.hitch(this, function(entry) {
			//tdiutil.alert(this.getString("solutionStarted"));
			//this._openConfigInstance(new tdi.tdicientry({atom:entry}), null);
		}), tdiapi.defaultErrHandler);
	},
	
	_viewSystemLog : function() {
		var key = "__view syslog__";
		if (this._map[key] == null) {
			var cci = new tdi.FilteredLogViewer({});
			var child = new dijit.layout.ContentPane({
				title : this.getString("systemLog"),
				content : cci,
				closable : true,
				uniqueId: key,
				iconClass: "activeALIcon",
				style:"height:95%",
				onClose : dojo.hitch(this, "closeALTab")
			});
			this.TabContainer.addChild(child);
			this._map[key] = child;
			child.startup();
			cci.openLogfile(null, null, null);
		}
		this.TabContainer.selectChild(this._map[key]);
	},
	
	_createSolutionFromTemplate : function(templateId) {
		var create = new tdi.CreateSolution();
		var dlg = new dijit.Dialog({
			title:this.getString("createSolution"),
			content: create,
			style: "width: 500px"
		});
		line ('line 261');
		create.uploadCompleted = dojo.hitch(this, function() {
			var solname = null;
			try {
				solname = create.Solution.get("value");
			} catch(err) {
			}
			dlg.hide();
			this.Projects.refresh();
			
			// -- wait for refresh before attempting to open config
			if(solname != null) {
				setTimeout(dojo.hitch(this, function(solname) {
					var item = this.Projects.getItemByName(solname)
					if(item != null) {
						this._openConfigEditor(item.entry[0]);
					}
				}, solname), 500);
			}
		});
		dlg.show();
	},
	
	_deleteConfig : function(configentry) {
		tdiutil.confirm(this.getString("deleteItem") + ": " + configentry.getTitle(), dojo.hitch(this, function(configentry, buttonId, msg, check) {
			if(buttonId == 0) {
//				dojo.when(tdiapi.deleteConfig(configentry), dojo.hitch(this, function() {
				dojo.when(tdiapi.deleteConfigAndData(configentry.getTitle()), dojo.hitch(this, function() {
					this._closeTab(configentry.getId(), 1, null, false);
					this.Projects.refresh();
				}), tdiapi.defaultErrHandler);
			}
		}, configentry));
	},
	
	_uploadConfig : function(template) {
		var upload = new tdi.UploadSolution();
		var dlg = new dijit.Dialog({
			title:this.getString("uploadSolution"),
			content: upload,
			style: "width: 500px"
		});
		upload.uploadCompleted = dojo.hitch(this, function() {
			dlg.hide();
			this.Projects.refresh();
		});
		dlg.show();
	},
	
	closeAllTabs : function() {
		alert("Closing all tabs ....")
	},
	
	closeALTab : function(cont, tab) {
		var child = this._map[tab.uniqueId];
		if (child != null) {
			this._map[tab.uniqueId] = null;
		}
		return true;
	},
	
	closeCITab : function(cont, tab) {
		var child = this._map[tab.uniqueId];
		if (child != null) {
			this._map[tab.uniqueId] = null;
		}
		return true;
	},

	_closeTab : function(tabid, buttonId, messageId, checked) {
		if(this._configmap[tabid]) {
			if(buttonId == 0 || buttonId == 1) {
				if(buttonId == 0) {
					this._configmap[tabid].content.saveConfig();
				}
				this.TabContainer.removeChild(this._configmap[tabid]);
				this._configmap[tabid] = null;
			}
		}
	},
	
	closeConfigTab : function(cont, tab) {
		var child = this._configmap[tab.uniqueId];
		if (child != null && child.content != null) {
			if(child.content.config != null) {
				if(child.content.config.isModified()) {
					//tdiutil.confirm(this.getString("abandonChanges"), dojo.hitch(this, "_closeTab", tab.uniqueId));
					tdiutil.askYesNoCancel(this.getString("saveChangesBeforeClose"), dojo.hitch(this, "_closeTab", tab.uniqueId));
					return false;
				}
			}
		}
		this._configmap[tab.uniqueId] = null;
		return true;
	},
	
	hasPendingChanges : function() {
		var changes = [];
		var map = this._configmap;
		for(item in map) {
			var child = map[item];
			if (child != null && child.content != null) {
				if(child.content.config != null) {
					if(child.content.config.isModified()) {
						changes.push(child.content.config.getConfigName());
					}
				}
			}
		}
		return changes.length == 0 ? null : changes.join("\n");
	},
	
	openItem : function(entry, cientry) {
		// Default is to monitor
		this._openConfigInstance(cientry, entry);
		/*
		if (cientry != null) {
			this._openConfigInstance(cientry, entry);
		} else if(entry != null) {
			this._openConfigEditor(entry);
		}
		*/
	},
	
	runAssemblyLine : function(obj) {
		// summary:
		//		Starts an assemblyline in its own temp config
		//		and shows a tab with the log output and process info/controls.
		var uniqueId = obj.assemblyline + "_" + new Date().getTime();
		dojo.when(tdiapi.startTempConfig(obj.config, uniqueId), dojo.hitch(this, function(data) {
			this._openAssemblyLine(new tdi.tdicientry({atom:data}), obj.assemblyline);
		}), tdiapi.defaultErrHandler);
	},
	
	openEditorWithCommand : function(configEntry, command, params) {
		var cmap = this._configmap;
		if(!command && params && cmap[configEntry.getId()]) {
			params.open = true;
		} else {
			dojo.when(this._openConfigEditor(configEntry), function() {
				var child = cmap[configEntry.getId()];
				if(child && child.content && child.content[command])
					child.content[command](params);
			});
		}
	},
	
	createDropButton : function() {
		// toolbar
		
		var handlers = new Array();
//		handlers.push({
//			label:this.getString("showServerLog"),
//			onClick:dojo.hitch(this, "_showServerLog")
//		});
		handlers.push({
			label:this.getString("showServerInfo"),
			menubar:true,
			onClick:dojo.hitch(this, "_showAbout")
		});
		handlers.push({
			label:this.getString("uploadSolution"),
			menubar:true,
			onClick:dojo.hitch(this, "_uploadConfig")
		});
		handlers.push({
			label:this.getString("createSolution"),
			onClick:dojo.hitch(this, "_createSolutionFromTemplate"),
			menubar:true,
			toolbar:false
		});
		handlers.push({
			label:this.getString("viewSyslog"),
			onClick:dojo.hitch(this, "_viewSystemLog"),
			menubar:true
		});
		handlers.push({
			label:this.getString("DataBrowser.title"),
			onClick:dojo.hitch(this, "_browseData"),
			menubar:false,
			toolbar:true
		});
		
		
		dojo.forEach(handlers, dojo.hitch(this, function(obj) {
			var item = new dijit.MenuItem(obj);
			if(item.menubar)
				this.Projects.addToolbarAction(item);
			obj.actionItem = item;
			if(obj.toolbar) {
				obj.toolItem = new dijit.form.Button(obj);
				this.Projects.addToolbarButton(obj.toolItem);
			}
		}));
		this.Projects.addToolbarAction(new dijit.MenuSeparator());

		this.Projects.updateToolbar();
		
//		this.Projects.addToolbarButton(new dijit.form.Button({
//			label:"Mem leak",
//			onClick:dojo.hitch(this, function() {
//				this._reglen = this._reglen || dijit.registry.length;
//				var diff = dijit.registry.length - this._reglen;
//				console.log("Current registered dijit widgets: " + dijit.registry.length + "; difference=" + diff)
//				if(diff != 0) {
//					console.log("******** Non TDI widgets");
//					for(var f in dijit.registry._hash) {
//						if(f.match("tdi_") == null)
//							console.log("Leaked: " + f);
//					}
//					console.log("******** TDI widgets");
//					for(var f in dijit.registry._hash) {
//						if(f.match("tdi_") != null)
//							console.log("Leaked: " + f);
//					}
//				}
//				this._reglen = dijit.registry.length;
//			})
//		}));
		
	},


	_browseData : function() {
		var key = "%%browsedata%%" + new Date();
		var title = this.getString("DataBrowser.title");
		if (this._map[key] == null) {
			var cci = new tdi.Databrowser({
				onOpenEditor: dojo.hitch(this, function(entry) {
					this.Projects.refresh();
					setTimeout(dojo.hitch(this, function(entry) {
						var item = this.Projects.getItemByName(entry);
						if(item != null && item.entry[0])
							this.openItem(item.entry[0], null);
					}, entry), 500);
				}),
				_id: key
			});
			var child = new dijit.layout.ContentPane({
				title : title,
				content : cci,
				closable : true,
				uniqueId: key,
				iconClass: "activeALIcon",
				onClose : dojo.hitch(this, "closeCITab")
			});
			this.TabContainer.addChild(child);
			this._map[key] = child;
		}
		this.TabContainer.selectChild(this._map[key]);
	},
	
	createMessageArea : function() {
//		var msgArea = new dojoe.multimessagearea.MultiMessageArea({
//			timeSort: true
//		}, this.MessageAreaDiv);
	},
	
	_findConfigInstanceEditor : function(configId) {
		for(f in this._map) {
			var arr = f.split("/");
			if(arr && arr.length > 0 && arr[arr.length-1] == configId) {
				return this._map[f];
			}
		}
		return null;
	},
	
	_updateRunStatusEvent : function(event) {
//		if(event.type == "di.ci.stop" || event.type == "di.ci.start") {
//			var tabitem = this._findConfigInstanceEditor(event.id);
//			if(tabitem) {
//				dojo.attr(tabitem, "iconClass", (event.type == "di.ci.start" ? "activeALIcon" : "stopIcon"));
//			}
//		}
		if(event.type == "di.server.start" || event.type == "di.ci.file.updated") {
			this.Projects.refresh();
		}
	},

	destroy : function() {
		dojo.forEach(this._subscriptions, function(handle) {
			dojo.unsubscribe(handle);
		});
	},
	
	postMixInProperties : function() {
    	this.inherited('postMixInProperties', arguments);
	},

	postCreate : function() {
		
		this.Projects.openItem = dojo.hitch(this, "openItem");
		this.Projects.stopConfigInstance = dojo.hitch(this, "_stopConfigInstance");
		this.Projects.monitorConfigInstance = dojo.hitch(this, "_openConfigInstance");
		
		this.Projects.openItem = dojo.hitch(this, "openItem");
		this.Projects.startConfigInstance = dojo.hitch(this, "_startConfigInstance");
		this.Projects.configureConfigInstance = dojo.hitch(this, "_openConfigEditor");
		this.Projects.deleteConfig = dojo.hitch(this, "_deleteConfig");
		this.Projects.uploadConfig = dojo.hitch(this, "_uploadConfig");
		this.Projects.unlockConfig = dojo.hitch(this, "_unlockConfig");
		
		this.Projects.createSolutionFromTemplate = dojo.hitch(this, "_createSolutionFromTemplate");
		this.Projects.uploadConfig = dojo.hitch(this, "_uploadConfig");
		
		this._subscriptions.push(dojo.subscribe(tdiconstants.runAssemblyLineSubject, dojo.hitch(this, "runAssemblyLine")));
		this._subscriptions.push(dojo.subscribe(tdiconstants.openEditorWithCommandSubect, dojo.hitch(this, "openEditorWithCommand")));
		this.Projects.showAbout = dojo.hitch(this, "_showAbout");
		
		this.VMInfo._openServerInfo = dojo.hitch(this, "_showAbout");
		
		dojo.when(tdiapi.getServerInfo(), dojo.hitch(this, function(data) {
			document.title = "Dashboard - " + data.hostname;
			var child = new dijit.layout.ContentPane({
				title : "About Dashboard",
				content : new tdi.Welcome(),
				closable : false,
				style: "margin:10px",
				uniqueId: "_%_"
			});
			this.TabContainer.addChild(child);
		}));
		this.createDropButton();
		this.createMessageArea();
		
		tdiapi.getStdNamespaces();
		this._eventsHandler = dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_updateRunStatusEvent"));
		tdiapi.startServerEventNotifications();

	}
});