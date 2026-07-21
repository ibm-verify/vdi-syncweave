dojo.provide("tdi.ConfigInstance");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.Dialog");
dojo.require("dijit.TitlePane");
dojo.require("dijit.Toolbar");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.tree.ForestStoreModel");

dojo.require("dojox.timing")

dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("dojo.date.locale");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");
dojo.require("tdi.AssemblyLineInstance");
dojo.require("tdi.ConfigInstanceControl");
dojo.require("tdi.FilteredLogViewer");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.LogfilesView");
dojo.require("tdi.TombstoneGraph");
dojo.require("tdi.TombstoneWidget");
dojo.require("tdi.TreeTableWidget");

dojo.declare("tdi.ConfigInstance",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// Widget and Templated
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/ConfigInstance.html"),
		
		// Constructor parameters
		//
		// This is the instance identifier we are editing/monitoring
		configInstance: null,
		
		// This is the running instance data (atom entry from REST call)
		cientry: null,
		
		// This keeps track of al controls created for the table widget
		_alControls: [],
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.graph = {};
		},
		
		addItem : function(item) {
			return this.treetable.getStore().newItem(item);
		},
		
		findItem : function(alname) {
			var item = null;
			this.treetable.getStore().fetch({query: {assemblyline:alname}, onComplete:function(items, request) {
				if(items.length == 1)
					item = items[0];
			}});
			return item;
		},
		
		getItemValue : function(item, attr) {
			return this.treetable.getItemValue(item, attr);
		},
		
		setItemValue : function(item, attr, value) {
			this.treetable.setItemValue(item, attr, value);
		},
		
		clearRunStatus : function() {
			var store = this.treetable.getStore();
			store.fetch({query:{assemblyline:"*"}, onComplete:function(items, request) {
				dojo.forEach(items, function(item) {
					store.setValue(item, "status", []);
					store.setValue(item, "entry", []);
					store.setValue(item, "cycles", []);
				});
			}});
		},
		
		getStats : function(event, name) {
			var arr = dojo.filter(event.taskStatistics.stat, function(item) {
				return item.name == name;
			});
			if(arr.length == 1)
				return arr[0].value;
			else
				return null;
		},
		
		_serverEvent : function(event) {
			if(!this.cientry) {
				if(this.configEntry && event.ciId && event.ciId == this.configEntry.getTitle() ) {
					dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
						this.cientry = new tdi.tdicientry({atom:data});
						this.updateStartStopTime();
						this.checkConfigModified();
					}));
				}
				
			} else if(event.ciId && event.ciId == this.cientry.getTitle()) {
				if(event.type == "di.al.stop") {
					var item = this.findItem(event.id.split("/")[1]);
					if(item) {
						this.setItemValue(item, "status", []);
						var date = this.getStats(event,"end");
						if(date)
							this.setItemValue(item, "stopped", dojo.date.locale.format(new Date(date), {datePattern:"yyyy-MM-dd", timePattern:"HH:mm:ss"}));
					}
				} else if(event.type == "di.al.start") {
					var item = this.findItem(event.id.split("/")[1]);
					if(item) {
						this.setItemValue(item, "status", "running");
						var date = this.getStats(event,"start");
						if(date)
							this.setItemValue(item, "started", dojo.date.locale.format(new Date(date), {datePattern:"yyyy-MM-dd", timePattern:"HH:mm:ss"}));
						this.setItemValue(item, "stopped", "");
					}
				} else if (event.type == "di.al.msg") {
					var item = this.findItem(event.id.split("/")[1]);
					if(item) {
						var msg = event.data.value;
						var index = msg.indexOf(":");
						if(index != -1)
							msg = msg.substring(index+1);
						this.setItemValue(item, "usermessage", msg);
					}
				} else if (event.type == "di.ci.start" && this.configEntry) {
					// Get the cientry for newly started config instance 
					dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
						this.cientry = new tdi.tdicientry({atom:data});
						this.updateStartStopTime();
						this.checkConfigModified();
					}));
				} else if (event.type == "di.ci.stop") {
					// Clear cientry when config instance stops 
					this.cientry = null;
					this.updateStartStopTime();
					
				} else if (event.type == "di.ci.file.updated") {
					// save this for later if the config is started/reloaded
					this.configUpdated = true;
				}
				
				try {
					this.enableMenuItems();
				} catch (err) {
					// Problem with dijit/form/_FormWidget trying to set "this.focusNode" disabled when this.focusNode==null
				}
			}
		},
		
		checkConfigModified : function() {
			if(this.configUpdated) {
				this.configUpdated = false;
				dojo.when(this.cientry.getLinkData("configuration"), dojo.hitch(this, "createALWidgets"), tdiapi.defaultErrHandler);
			}
		},
			
		createInstanceRow : function(entry) {
			// summary:
			//		Creates a row with the label and control for a parameter descriptor(p). If hideNullValue
			//		is true then the row is only created if the config has a value for this parameter.
			// description:
			//		Creates a tr with a td for the label and control
			//
			var arr = entry.title.value.match(/AssemblyLines\/(\w*)\.\d*/i);
			if(arr.length == 2) {
				var item = this.findItem(arr[1]);
				if(item != null) {
					var alentry = new tdi.tdialentry({atom:entry});
					this.setItemValue(item, "status", "running");
					this.setItemValue(item, "entry", alentry);
					dojo.when(alentry.getStatus(), dojo.hitch(this, function(item, data) {
						var stats = tdiutil.dataValueArray2Object(data.stat);
						this.setItemValue(item, "cycles", stats.get);
						this.setItemValue(item, "started", dojo.date.locale.format(new Date(stats.start), {formatLength: "short"}));
					}, item));
					
					var idarr = entry.id.split("/");
					if(idarr && idarr.length > 0) {
						var alid = idarr[idarr.length-1];
						this.updateChildren(item, alid);
					}
				}
			}
		},
		
		updateChildren : function(parent, alid) {
			dojo.when(tdiapi.getChildAssemblyLines(alid), dojo.hitch(this, function(data) {
				var store = this.treetable.getStore(); 
				store.fetch({query:{parentId:alid}, queryOptions:{deep:true}, onItem:function(item, request) {
					if(store.getValue(item, "parentId") == alid) {
						store.deleteItem(item);
					}
				}});
				store.save();
				
				dojo.forEach(data, dojo.hitch(this, function(obj) {
					this.treetable.getStore().newItem({
						id:parent.id + "." + obj.id,
						assemblyline:"   " + parent.assemblyline + "(" + obj.id + ")",
						started:dojo.date.locale.format(new Date(obj.stats.start), {formatLength: "short"}),
						stopped:"",
						cycles:obj.stats.get,
						parentId:""+obj.parentId,
						usermessage:obj.usermessage,
						status:"running",
						nextrun:""
					}, {parent:parent, attribute:"items"})
				}));
				store.save();
			}));
		},
		
		updateTombstones : function() {
			var allist = this.config.getAssemblyLineNames();
			dojo.forEach(allist, dojo.hitch(this, function(name) {
				this.updateTombstone(name);
			}));
		},
		
		updateTombstone : function(alname) {
			dojo.when(tdiapi.getTombstones(this.config.getConfigName(), alname), dojo.hitch(this, function(data) {
				var ts = null;
				if(data && data.tombstone) {
					if(data.tombstone.length > 0)
						ts = data.tombstone.pop();
				}
				var item = this.findItem(alname);
				if(item != null) {
					// -- only update start/stop if it is not running
					if(this.treetable.getStore().getValue(item, "entry") != null) {
						this.setItemValue(item, "stopped", "");
					} else {
						this.setItemValue(item, "started", ts ? tdiutil.formatDate(ts.started) : "");
						this.setItemValue(item, "stopped", ts ? tdiutil.formatDate(ts.terminated) : "");
						if(ts && ts.statistics && ts.statistics.get)
							this.setItemValue(item, "cycles", ts.statistics.get);
						else
							this.setItemValue(item, "cycles", "");
						if(ts && ts.userMessage)
							this.setItemValue(item, "usermessage", ts.userMessage);
						else
							this.setItemValue(item, "usermessage", "");
					}
				}
			}));
		},
		
		updateTable : function(atom) {
			if(dojo.isArray(atom.entry)) {
				dojo.forEach(atom.entry, this.createInstanceRow, this);
			} else if(dojo.isObject(atom.entry)) {
				this.createInstanceRow(atom.entry);
			} else {
				this.clearRunStatus();
				dojo.forEach(this._alControls, function(cic) {
					cic.setRunning(false);
				});
			}
		},
		
		updateSchedules : function(schedules) {
			if(this.cientry) {
				var inst = this.cientry.getTitle();
				var list = schedules[inst];
				if(list) {
					for(sched in list) {
						this.updateSchedule(list[sched]);
					}
				}
			} else {
				var store = this.treetable.getStore();
				store.fetch({query:{assemblyline:"*"}, onComplete:function(items, request) {
					dojo.forEach(items, function(item) {
						store.setValue(item, "nextrun", []);
					});
				}});
			}
		},
		
		updateSchedule : function(data) {
			var item = this.findItem(data.assemblyLineName);
			if(item) {
				var nextRun = new Date(data.nextRun);
				var year = nextRun.getFullYear();
				var month = nextRun.getMonth() + 1;
				if(month < 10)
					month = "0" + month;
				var day = nextRun.getDate();
				if(day < 10)
					day = "0" + day;
				var hour = nextRun.getHours();
				if(hour < 10)
					hour = "0" + hour;
				var min = nextRun.getMinutes();
				if(min < 10)
					min = "0" + min;
				var sec = nextRun.getSeconds();
				if(sec < 10)
					sec = "0" + sec;
				
				this.setItemValue(item, "nextrun", year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec);
			}
		},
		
		createALWidgets : function(config) {
			var cfg = new tdi.tdiconfig({config:config});
			this.config = cfg;
			var altrow = false;
			var allist = cfg.getAssemblyLineNames();
			
			this._createALWidgets = true;
			this.treetable.removeAllItems();
			
			dojo.forEach(allist, dojo.hitch(this, function(alname) {
				try {
					this.addItem({
						id:alname,
						assemblyline:alname,
						started:"",
						stopped:"",
						cycles:0,
						nextrun:"",
						alconfig:cfg.getAssemblyLine(alname),
						cientry:this.cientry
					});
				} catch(err) {
					console.log(err);
				}
			}));
			
//			this.createTombstoneTab();
//			this.createLogfilesTab();
//			this.tombstones.setLogViewer(this.logfiles);
			this._createALWidgets = false;
			
			this.onTick();
			this.timer.start();
		},
		
		createTombstoneTab : function() {
		    this.tombstones = new tdi.TombstoneWidget({config:this.config.getConfigName()}); 
			var ts_cp = new dijit.layout.ContentPane({title:this.getString("miadmin.foldernames.Tombstones"), content:this.tombstones, closable:false});
			this.Tabs.addChild(ts_cp);
			this.Tabs.selectChild(ts_cp);
		},
		
		createLogfilesTab : function() {
		    this.logfiles = new tdi.LogfilesView({config:this.config}); 
			var ts_cp = new dijit.layout.ContentPane({title:this.getString("WebCE.logfiles"), content:this.logfiles, closable:false});
			this.Tabs.addChild(ts_cp);
			this.Tabs.selectChild(ts_cp);
		},
		
		
		viewALLog : function(alentry) {
			var alentry = this.treetable.getStore().getValue(this.getSelectedItem(), "entry");
			if(alentry != null) {
				var link = alentry.getLink("log");
				if(link != null && link.href != null) {
					var tp = new tdi.LogWidget({url:link.href, poll:false});
					var child = new dijit.layout.ContentPane({title:alentry.getTitle(), content:tp, closable:true});
					this.Tabs.addChild(child);
					this.Tabs.selectChild(child);
				}
			}
		},
		
		onTick : function() {
			if(this._createALWidgets) {
				return;
			}
			if(this.cientry != null) {
				dojo.when(this.cientry.getLinkData("assembly-line"), dojo.hitch(this, function(data) {
					this.updateTable(data);
					this.enableMenuItems();
				}));
				dojo.when(tdiapi.getActiveSchedules(), dojo.hitch(this, "updateSchedules"));
			} else {
				// It may happen that the cientry is out of synch.
				// Just poll for the cientry to recover silently
				dojo.when(tdiapi.getCIEntry(this.configEntry.getTitle()), dojo.hitch(this, function(data) {
					this.cientry = new tdi.tdicientry({atom:data});
				}));
			}
			this.updateTombstones();
		},
		

		destroy : function() {
			if(this.timer != null) {
				this.timer.stop();
				this.timer = null;
			}
			this.inherited("destroy", arguments);
		},
		
		resize : function(obj) {
			// summary:
			//		Overridden to resize border container
			this.inherited("resize", arguments);
			if(!obj) {
				obj = {w:this.domNode.clientWidth, h:this.domNode.clientHeight};
			}
			this.BorderContainer.resize(obj);
		},

		toggleRun : function(start) {
			dojo.forEach(this.treetable.getSelectedRows(), dojo.hitch(this, function(item) {
				this.toggleRunState(item, !this.isRunning(item));
			}));
		},
		
		toggleRunState: function(item, start) {
			var entry = this.getItemValue(item, "entry");
			var config = this.getItemValue(item, "alconfig");
			if(entry != null && !start) {
				dojo.when(tdiapi.stopAssemblyLine(entry), dojo.hitch(this, function() {
					this._store.setValue("entry", []);
					this._store.setValue("status", "");
					this.enableMenuItems();
				}));
			} else {
				if(this.cientry == null && start) {
					// start config instance and assemblyline
					dojo.when(tdiapi.startConfig(this.configEntry), dojo.hitch(this, function(data) {
						this.cientry = new tdi.tdicientry({atom:data});
						dojo.when(tdiapi.startAssemblyLine(this.cientry, config.getName()), dojo.hitch(this, function() {
							this.enableMenuItems();
							if(this.configTab == null)
								this.startConfigLogListener();
						}), tdiapi.defaultErrHandler);
					}));
				} else if (this.cientry != null && start) {
					// start assemblyline
					dojo.when(tdiapi.startAssemblyLine(this.cientry, config.getName()), dojo.hitch(this, "enableMenuItems"), tdiapi.defaultErrHandler);
				}
			}
		},
		
		_createRunReport : function() {
			var configentry = this.configEntry;
			var params = {};
			dojo.publish(tdiconstants.openEditorWithCommandSubect, [this.configEntry, "createRunReport"]);
			/*
			dojo.publish(tdiconstants.openEditorWithCommandSubect, [this.configEntry, null, params]);
			if(params.open) {
				dojo.publish(tdiconstants.openEditorWithCommandSubect, [this.configEntry, "createRunReport"]);
			} else {
				dojo.when(tdiapi.checkOutConfig(configentry), function(ok) {
					alert("Create and edit new runreport");
					tdiapi.unlockConfig(configentry);
				}, function notok(err) {
					tdiutil.error(err);
				});
			}
			*/
		},
		
		_createSchedule : function() {
			var al = this.treetable.getStore().getValue(this.getSelectedItem(), "assemblyline");
			dojo.publish(tdiconstants.openEditorWithCommandSubect, [this.configEntry, "selectItem", al]);
		},
		
		enableMenuItems : function() {
			var selItem = this.getSelectedItem();
			var running = selItem == null ? false : this.treetable.getStore().getValue(selItem, "status") == "running";
			dojo.forEach(this.menu.getChildren(), function(m) {
				m.set("disabled", m.cid == "start" && running);
				m.set("disabled", m.cid == "stop" && !running);
				m.set("disabled", m.cid == "log" && !running);
			});
			this._startBtn.set("disabled", running);
			this._stopBtn.set("disabled", !running);
//			this._graphBtn.set("disabled", this.getSelectedItem() == null);
			this._createScheduleBtn.set("disabled", this.getSelectedItem() == null);
			//this._createRunRepBtn.set("disabled", this.getSelectedItem() == null);
//			this.logfiles.selectTarget(this.treetable.getStore().getValue(selItem, "assemblyline"));
//			this.tombstones.selectTarget(this.treetable.getStore().getValue(selItem, "assemblyline"));
		},
		
		createMenu : function() {
			this.menu = new dijit.Menu({});
			
			this._startBtn = new dijit.form.Button({
				showLabel: true,
				label: this.getString("start"),
				onClick: dojo.hitch(this, "toggleRun", true)
			});
			this._startBtn.set("disabled", true);
			this.treetable.addToToolbar(this._startBtn);
			
			this._stopBtn = new dijit.form.Button({
				showLabel: true,
				label: this.getString("stop"),
				onClick: dojo.hitch(this, "toggleRun", false)
			});
			this._stopBtn.set("disabled", true);
			this.treetable.addToToolbar(this._stopBtn);
			
//			this._graphBtn = new dijit.form.Button({
//				showLabel: true,
//				label: this.getString("showALHistory"),
//				onClick: dojo.hitch(this, "toggleGraph", false)
//			});
//			this._graphBtn.set("disabled", true);
//			this.treetable.addToToolbar(this._graphBtn);

			this._createRunRepBtn = new dijit.form.Button({
				showLabel: true,
				label: this.getString("WebCE.createRunReport"),
				onClick: dojo.hitch(this, "_createRunReport", false)
			});
			this.treetable.addToToolbar(this._createRunRepBtn);
			
			this._createScheduleBtn = new dijit.form.Button({
				showLabel: true,
				label: this.getString("CreateALSchedule.label"),
				onClick: dojo.hitch(this, "_createSchedule", false)
			});
			this._createScheduleBtn.set("disabled", true);
			this.treetable.addToToolbar(this._createScheduleBtn);
			
//			this._viewLogBtn = new dijit.form.Button({
//				label:this.getString("viewLog"),
//				cid:"log",
//				onClick:dojo.hitch(this, "viewALLog")
//			});
//			this._viewLogBtn.set("disabled", true);
//			this.treetable.addToToolbar(this._viewLogBtn, 4, 1);
		},
		
		getSelectedItem : function() {
			return this.treetable.getSelectedItem();
		},
		
		getStatus : function(status) {
			if(status == "running")
				return "<div class='activeALIcon' style='margin-left:13px; width:14px; height:14px'></div>";
			else
				return "";
		},
		
		isRunning: function(item) {
			return this.getItemValue(item, "status");
		},
		
		startOrStopAL : function(alname, sender) {
			sender.disableRun();
			this.toggleRunState(sender.item, !this.isRunning(sender.item));
		},
		
		hasSchedule : function(alname) {
			if(this.config) {
				var sched = this.config.getScheduleForAssemblyLine(alname);
				if(sched) {
					sched = this.config.getSchedule(sched);
					if(sched && sched.getEnabled())
						return true;
				}
			}
			return false;			
		},
		
		formatAssemblyLine : function(alname, index) {
			if(this._alControls[alname])
				this._alControls[alname].destroyRecursive();
			
			var item = this.treetable.getItem(index);
			this._alControls[alname] = new tdi.ConfigInstanceControl({
				name:alname,
				running:false,
				onClick:dojo.hitch(this, "startOrStopAL"),
				item:item,
				running:this.isRunning(item),
				showSchedule:this.hasSchedule(alname),
				onScheduleClick:dojo.hitch(this, function(alname, sender) {
					dojo.publish(tdiconstants.openEditorWithCommandSubect, [this.configEntry, "selectItem", alname]);
				})
			});
			return this._alControls[alname];
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
					}), tdiapi.defaultErrHandler);
				}
			}));
		},
		
		updateStartStopTime : function() {
			if(this._status) {
				if(this.cientry) {
					this._status.innerHTML = this.getString("WebCE.started") + ": " + tdiutil.formatDate(this.cientry.getUpdated());
				} else {
					this._status.innerHTML = "";
				}
			}
		},
		
		configureTable : function() {
			
			this._tableHdr1 = dojo.create("H1", {innerHTML:this.getString("activeScheduledAL")}, this.DGDiv, "last");
			this._status = dojo.create("span", {innerHTML:"", style:"float:right"}, this._tableHdr1, "last");
			
			var layout = [
				{field:"status", name:" ", width:"30px", formatter:this.getStatus},
				{field:"assemblyline", name:this.getString("assemblyline"), width:"150px", formatter:dojo.hitch(this, "formatAssemblyLine")},
				{field:"started", name:this.getString("started"), width:"150px"},
				{field:"stopped", name:this.getString("stopped"), width:"150px"},
				{field:"nextrun", name:this.getString("nextrun"), width:"150px"},
				{field:"usermessage", name:this.getString("WebCE.userMessage"), width:"150px"},
				{field:"cycles", name:this.getString("cycles"), width:"70px"}
			];
			
			this.treetable = new tdi.TreeTableWidget({
				treeTableLayout:layout,
				getRowMenu: dojo.hitch(this, function() { return this.menu; }),
				onSelectionChanged: dojo.hitch(this, function() {
					this.enableMenuItems();
					this.toggleGraph();
				}),
				//onRowDblClick: dojo.hitch(this, "toggleGraph"),
				toolbarOptions: {
					actionMenu:false,
					refreshIcon:false,
					refreshMenu:false
				}
			}).placeAt(this.DGDiv);
			
			this._table = this.treetable.getTreeTable();
		},
		
		resizeTables : function(obj) {
			var cb = dojo.contentBox(this.HeaderCP.containerNode);
			if(cb != null && cb.h > 0) {
				var p1 = dojo.marginBox(this._tableHdr1);
				if(p1)
					cb.h -= p1.h;
				
				if(this._warning != null) {
					p1 = dojo.marginBox(this._warning);
					if(p1)
						cb.h -= p1.h;
				}
				
				var p2 = dojo.style(this.DGDiv, "margin");
				var p3 = dojo.style(this.DGDiv, "padding");
				if(p2 != null)
					cb.h -= (p2 * 2);
				if(p3 != null)
					cb.h -= (p3 * 2);
				
				cb.h -= 15;
				
				this.treetable.resize({h:cb.h, w:cb.w});
			}
		},
		
		createLogViewer : function(data) {
			
		},
		
		startConfigLogListener : function() {
//			dojo.when(this.cientry.getLinkData("listener"), dojo.hitch(this, function(data) {
//				var feed = new tdi.tdifeed({feed:data});
//				var entry = feed.getEntry("poll");
//				if(entry != null && entry.getLink("poll") != null) {
//					var tp = new tdi.FilteredLogViewer({url:entry.getLink("poll").href});
//					var child = new dijit.layout.ContentPane({title:"Config Log", content:tp, closable:false});
//					this.Tabs.addChild(child);
//					this.Tabs.selectChild(child);
//					this.configTab = child;
//				}
//			}), tdiutil.error);
			var configid = this.cientry ? this.cientry.getTitle() : this.configEntry.getTitle();
			var tp = new tdi.FilteredLogViewer({filter:{source:"[" + configid + "]"}, hideFileButton:true});
			tp.openLogfile(null, null, null);
			var child = new dijit.layout.ContentPane({title:this.getString("miadmin.foldernames.Logging"), content:tp, closable:false});
			this.Tabs.addChild(child);
			this.Tabs.selectChild(child);
			this.configTab = child;
		},
		
		_checkLogSettings : function(log) {
			if(!log.enabled) {
				this._warning = dojo.create("H3", {innerHTML:"Default logging is <b>OFF</b> - go to Actions/Server Info to turn on default logging"},
						this.DGDiv, "last");
			}
		},
		
		toggleGraph : function() {
			var al = this.treetable.getStore().getValue(this.getSelectedItem(), "assemblyline");
			if(al == null)
				return;
			
			if(!this.graph[al]) {
			    this.graph[al] = new tdi.TombstoneGraph({config:this.config.getConfigName()}); 
				var ts_cp = new dijit.layout.ContentPane({
					title:al,
					content:this.graph[al],
					onClose : dojo.hitch(this, function(tab) {
						this.graph[al] = null;
						return true;
					}),
					closable:true});

				this.Tabs.addChild(ts_cp);
				this.Tabs.selectChild(ts_cp);
//				var store = this.tombstones.getStore();
				this.graph[al].updateGraph(al); // store, al);
				this.graph[al] = ts_cp;
			} else {
				this.Tabs.selectChild(this.graph[al]);
			}
		},
		
		postCreate : function() {
			this._store = new dojo.data.ItemFileWriteStore({
				data: {
						identifier: "id",
						label: "assemblyline",
						items : []
				}
			});
			
		    this._model = new dijit.tree.ForestStoreModel({
		        store: this._store,
		        rootId: "configInstanceRoot",
		        rootLabel: "configInstanceItems",
		        childrenAttrs: ["items"]
		    });
		    		    
			this.configureTable();
		    this.createMenu();
		    
		    // Override layout children to compute proper size for tables
		    this.HeaderCP._layoutChildren = dojo.hitch(this, "resizeTables");
		    
			this.timer = new dojox.timing.Timer(5000);
			this.timer.onTick = dojo.hitch(this, "onTick");
			
			if(this.cientry != null) {
				dojo.when(this.cientry.getLinkData("configuration"), dojo.hitch(this, "createALWidgets"), tdiapi.defaultErrHandler);
			} else if(this.configEntry != null) {
				dojo.when(tdiapi.getConfig(this.configEntry), dojo.hitch(this, function(data) {
					this.createALWidgets(data);
				}), function(err) {
					console.log(err);
					alert("Unable to load configuration: " + err);
				});
			}

			this.updateStartStopTime();
			
			this.startConfigLogListener();
			
			dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_serverEvent"));
			
			// dojo.when(tdiapi.getLogSettings(), dojo.hitch(this, "_checkLogSettings"));
		}
	}
);