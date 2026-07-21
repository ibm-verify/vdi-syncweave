/**
 * The FlowStatus widget shows highlevel information about a flow and its state. In addition, access to log files
 * etc are provided via menu choices.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/dom-class",
	"dojo/aspect",
	"dojo/dom-geometry",
	"dojo/dom-style",
	"dojo/date/locale",
	"dojo/topic",
	"dojox/fx",
	"dojox/fx/_base",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/widget/Dialog",
	"idx/layout/HeaderPane",
	"idx/layout/MenuTabController",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/layout/ContentPane",
	"dijit/form/Button",
	"dijit/Menu",
	"dijit/MenuItem",
	"dijit/ProgressBar",
	"tdi/DialogContent",
	"tdi/TableWidget",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"tdi/tdiconstants",
	"tdi/ALSchedule",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/atom/tdialentry",
	"tdi/ldapsync/LDSFlow",
	"tdi/ldapsync/LDSSource",
	"tdi/ldapsync/LDSUtil",
	"tdi/ldapsync/LDSRunOptions",
	"idx/dialogs",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSFlowStatus.html"
], function(declare, array, lang, html, domClass, aspect, domGeometry, domStyle, dateLocale, topic, corefx, fx, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, HeaderPane, MenuTabController, BorderContainer, TabContainer, 
		ContentPane, Button, Menu, MenuItem, ProgressBar, DialogContent, TableWidget, tdiapi, tdiutil, tdiconstants, ALSchedule, tdiconfigentry, tdicientry, tdialentry, LDSFlow, LDSSource, LDSUtil, LDSRunOptions, idx, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		expandedHeight: 500,
		showCycle: 0,
		
		_isNodeOpen: function(node) {
			var h = domStyle.get(node, "height");
			return (h != 0);
		},
		
		_openNode: function(node, args) {

			var h = domStyle.get(node, "height");
			
			this._toggleNode(this._currentNode, args, true);
			
			if(this.testrun)
				this.testrun.resize();
			
			// -- clicking twice closes the expando
			if(node == this._currentNode && h != 0) {
				return;
			} else {
				this._currentNode = node;
				this._toggleNode(node, args)
			}
		},
		
		_toggleNode: function(node, args, close) {
			if(!node)
				return;
			var h = domStyle.get(node, "height");
			if(close && h == 0)
				return;
			
			var params = {
				properties: {
					height: {
						start: h,
						end: (h == 0 ? this.expandedHeight : 0)
					}
				},
				node:node
			};
			
			if(args)
				declare.safeMixin(params, args);
			
			fx.animateProperty(params).play();
		},
		
		_onEditFlow: function(callback) {
			var t = this;
			var flow = t.flowEditor;
			if(!flow) {
				flow = new LDSFlow({});
				flow.setConfig(t.item.config);
				flow.setEndpoints(t.endpoints);
//				flow.orgDestroyRecursive = flow.destroyRecursive;
//				flow.destroyRecursive = function() {};
//				t.flowEditor = flow;
			}
			t.parent.showEditorPane(t.item.config.getName(), flow);
		},
		
		enableSchedules: function(enable) {
			var alconfig = this.item.config;
			var mc = alconfig.getTop();
			var arr = alconfig.getSchedules();
			array.forEach(arr, function(name) {
				var sched = mc.getSchedule(name);
				sched.setEnabled(enable);
			});
		},
		
		getFlowParameterBool: function(param, defval) {
			// summary:
			//		Returns the value of the Output connector's <i>param</i> parameter.
			return this.item.config.getConnector("Output").getConnectionConfig().getParamBoolean(param, defval);
		},
		
		terminateFlow: function() {
			// summary:
			//		Terminate the LDAPSync or LDAPMigrate assemblyline for this flow.
			//		Does not do anything unless the assemblyline is running.
			//
			var t = this;
			LDSUtil.startLdapSync(LDSUtil.projectName).then(function(cientry) {
				return tdiapi.getAssemblyLineList(cientry);
			}).then(function(data) {
				var re = new RegExp("^" + t.getFlowName() + "_(LDAPSync|LDAPMigrate)");
				if(lang.isArray(data.entry)) {
					var arr = array.filter(data.entry, function(item) {
						return re.test(item.title.value);
					});
					array.forEach(arr, function(item) {
						// -- Keep-alive schedules are automatically disabled and stopped
						if(t.getScheduleType() == "keep-alive") {
							if(t.getScheduleConfig().getEnabled()) {
								t.getScheduleConfig().setEnabled(false);
								t.terminateSchedule();
							}
						}
						tdiapi.stopAssemblyLine(tdialentry({atom:item}));
					})
				};
			});
		},
		
		terminateSchedule: function() {
			// summary:
			//		Terminates the active schedule for this flow.
			//		No effect if schedule is not running. 
			tdiapi.stopSchedule(this.getSolutionName(), this.getFlowName());
		},
		
		getScheduleType: function() {
			// summary:
			//		Returns the schedule type for this flow.
			//		It returns "none", "keep-alive" or "timed"
			var schedule = this.getScheduleConfig();
			if(!schedule) {
				return "none";
			} else if(schedule.isKeepalive()) {
				return "keep-alive";
			} else {
				return "timed";
			}
		},
		
		getScheduleConfig: function() {
			// summary:
			//		Returns the Schedule configuration for this Flow (or null if none exists)
			//
			var alconfig = this.item.config;
			var config = alconfig.getTop();
			var schedule = config.getScheduleForAssemblyLine(alconfig.getName());
			if(schedule) {
				schedule = config.getSchedule(schedule);
			}
			return schedule;
		},
		
		_onTestFlow: function() {
			this._openNode(this.testrun.domNode);
		},
		
		_onStopFlow: function() {
			var t = this;
			idx.confirm(t.getString("FDS.terminateConfirm"), function() {
				t.terminateFlow();
			});
		},
		
		_onStartFlow: function(type) {
			var t = this;
			
			var config = this.item.config.getTop();
			var alname = this.item.config.getName();
			var tcb = {
				"@type":"taskCallBlock",
				runtime: {
					initParam: {
					   "@type": "entry",
					   attribute: [
					      {
					         children: [
					            {
					               value: {
					                  value: (type == "sync") ? "LDAPSync" : "LDAPMigrate"
					               }
					            }
					         ],
					         "name": "SyncOper",
					         "protect": false
					      },
					      {
						         children: [
						            {
						               value: {
						                  value: (type == "simulate") ? true : false
						               }
						            }
						         ],
						         "name": "Simulate",
						         "protect": false
						      }
					   ]
					}
				}
			};
			
			LDSUtil.startAssemblyLine(alname, config, null, tcb).then(function(data) {
				t.alentry = new tdialentry({atom:data});
				t._alid = data.id.split("/").pop();
				t.set("runtype", type);
				t.enableRunButtons(false, false);
			},
			function(err) {
				tdsutil.error(err);
			});
		},

		updateStatusIcon: function() {
			// summary:
			//		Updates the status icon to reflect the flow's running state.
			if(this.get("runtype") == "simulate") {
				this._statusIcon.setLabel("<img src='/fds/static/images/Simulate.gif'/>");
			} else if(this.get("runtype") == "sync") {
				this._statusIcon.setLabel("<img src='/fds/static/images/Synchronize.gif'/>");
			} else if(this.get("runtype") == "migrate") {
				this._statusIcon.setLabel("<img src='/fds/static/images/Active.gif'/>");
			} else {
				this._statusIcon.setLabel("");
			}
		},
		
		updateChangesPending: function(forceset) {
			// summary:
			//		Shows the warning icon that there are pending changes to the flow.
			//		Flag is reset once flow is no longer running.
			var running = this.get("runtype") != "";
			if((running && this.changesPending) || forceset) {
				this._pendingIcon.setLabel("<img src='/fds/static/images/Warning.png'/>");
				this._pendingIcon.set("title", this.getString("FDS.flowChangesPendingRestart") );
			} else {
				this.changesPending = false;
				this._pendingIcon.setLabel("");
				this._pendingIcon.set("title", "" );
			}
		},
		
		enableRunButtons: function(enable, clearstats) {
			this.startButton.set("disabled", !enable);
			this.stopFlow.set("disabled", enable);

			if(enable) {
				this._statusIcon.setLabel("");
				if(this.progressBar) {
					this.progressBar.destroyRecursive();
					this.progressBar = null;
				}
				// refresh idle display with new stats
				this.getActivityLog(); 
			} else {
				if(clearstats) {
					this.statDate.innerHTML = "";
					this.statUsers.innerHTML = "----";
					this.statGroups.innerHTML = "----";
				}
				if(!this.progressBar) {
					this.progressBar = new ProgressBar({
						indeterminate:true,
						"aria-valuetext":this.getString("DebuggerPanel.running")
					}).placeAt(this.statDate);
				}
			}
		},
		
		restartConfigAndRun: function() {
			var t = this;
			LDSUtil.getConfigInstance(t.getSolutionName()).then(function(data) {
				if(data) {
					tdiapi.stopConfig(data).then(function() {
						idx.hideProgressDialog();
						t._onStartTest();
					});
				}
			});
		},
		
		_onDeleteFlow: function(event) {
			dojo.stopEvent(event);
			this.onDeleteFlow(this.item.id);
		},
		
		onDeleteFlow: function(id) {
		},
		
		deleteFlow: function() {
			// summary:
			//		Called to clean up artifacts for this flow
			//		Any current schedule or assemblyline is terminated.
			this.terminateFlow();
			this.terminateSchedule();
		},
		
		_isLogViewOpen: function() {
			return this._isNodeOpen(this.testrun.domNode);
		},
		
		_onViewLogs: function(mouseEvent) {
			this._onViewHistory(false);
		},
		
		_onViewHistory: function(ignoreState, showCycle) {
			var t = this;
			var cycle = showCycle ? showCycle : t.showCycle;
			
			if(!ignoreState) {
				if(!t._isLogViewOpen()) {
					t._openNode(t.testrun.domNode);
				} else {
					t._openNode(t.testrun.domNode);
					return;
				}
			}
			
			// -- If toggle pane with same cycle no need to reload
			if(showCycle && cycle == t.showCycle)
				return;
			
			var logfile = t.cycles[cycle]["LDAPMigrate.log"] ? t.cycles[cycle]["LDAPMigrate.log"] : t.cycles[cycle]["LDAPSync.log"]; 
			
			LDSUtil.getLdapSyncSummary(logfile.name, t.getLogFolder()).then(function(data) {
				t.flowSummary.innerHTML = pre + data + "\n</pre>";
			}, function(err) {
				t.flowSummary.innerHTML = t.errmsg(err);
			});
			
			
			//
			// -- Remove old log tabs (always keep summary)
			//
			var tabs = t.tabContainer.getChildren();
			for(var i = 1; i < tabs.length; i++) {
				t.tabContainer.removeChild(tabs[i]);
			};
			
			var pre = "<pre style='font-size:1em; white-space: pre-wrap'>\n";
			var preclose = "\n</pre>";
			
			var targets = [t.errorLog, t.syncLog, t.entriesNotSynced, t.migrateLog, t.entriesNotMigrated, t.groupMembersMissing];
			var filekeys = ["Errors.log", "LDAPSync.log", "EntriesNotSynchronized.ldif", "LDAPMigrate.log", "EntriesNotMigrated.ldif",
			                "GroupMembersMissing.log", ""];
			var labels = [
               t.getString("FDS.errorLog"), t.getString("FDS.syncLog"), t.getString("FDS.entriesNotSynced"),
               t.getString("FDS.migrationLog"), t.getString("FDS.entriesNotMigrated"), t.getString("FDS.groupMembersMissing")
	        ];

			var i = 0;
			var fileobj = t.cycles[cycle ? cycle : "0"];
			
			for(i = 0; i < targets.length; i++) {
				if(fileobj && fileobj[filekeys[i]]) {
					t.loadLogFile(targets[i], fileobj[filekeys[i]].name, labels[i]);
				}
			}
			
			setTimeout(function() {
				t.testrun.resize();
			}, 500);
		},
		
		loadLogFile: function(target, file, label) {
			var t = this;
			var pre = "<pre style='font-size:1em; white-space: pre-wrap; overflow:scroll'>\n";
			var preclose = "\n</pre>";
			
			LDSUtil.getLdapSyncLogFile(t.getLogFolder() + "/" + file).then(function(data) {
				if(data && data.trim() != "") {
					var current = array.filter(t.tabContainer.getChildren(), function(child) {
						return (child.title == label)
					});
					if(current.length > 0) {
						t.tabContainer.removeChild(current.pop());
					}
					var tab = new ContentPane({
						title:label,
						content:pre + data.replace(/</g, "&lt;") + preclose
					});
					tab.startup();
					tab.resize();
					t.tabContainer.addChild(tab);
					t.tabContainer.resize();
				}
			}, function(err) {
				// -- Some files may not exist
				if(err.response && err.response.status != 404)
					alert(err);
			});
		},
		
		errmsg: function(err) {
			if(err.status == 404) {
				return this.getString("FDS.emptyLog");
			}
			if(err.responseText) {
				var arr = err.responseText.match(/<title>(.*)<\/title>/);
				if(arr != null && arr.length > 1) {
					return arr[1];
				}
			}
			return err.message;
			
		},
		
		setStatus: function(msg) {
			this.status.innerHTML = msg;
		},
		
		_updateRunStatusEvent: function(event) {
			var t = this;
			var thisconfig = t.getSolutionName() == event.ciId;
			var alname = t.item.config.getName();
			var thisal = (event.id == alname || event.id == "AssemblyLines/"+alname); // && event.id.indexOf(alname+"_") == 0);
			var syncal = event.id && event.id == (alname+"_LDAPSync");
			var migal = event.id && event.id == (alname+"_LDAPMigrate");
			var logal = event.id == alname+"_WriteToLDAP";
			var re = new RegExp("^" + alname + "_[^_]$");
			
			if(re.test(event.id))
				logal = true;
			
			if(event.type == "di.al.stop" && (syncal || migal || thisal) ) {
				t._alStopped();
				t.updateSchedule();
				t.getActivityLog();
				
			} else if(event.type == "di.al.start" && thisal) {
				t._alStarted(event, migal);
				t.updateSchedule();
				
			} else if(event.type == "di.al.start" && (syncal || migal)) {
				if(migal && t.get("runtype") == "simulate") {
					; // don't overwrite simulate state
				} else {
					t.set("runtype", syncal ? "sync" : "migrate");
					if(!syncal) {
						t.total.users = 0;
						t.total.groups = 0;
						t.updateTotals(t.total);
					}
				}
				
			} else if(event.type == "di.al.msg" && logal) {
				try {
					var json = dojo.fromJson(event.data.value.substring(event.data.value.indexOf("{")));
					t.statUsers.innerHTML = t.summary(json.status.person);
					t.statGroups.innerHTML = t.summary(json.status.group);
					t.updateTotals({
						users:json.status.person.add - json.status.person["delete"],
						groups:json.status.group.add - json.status.group["delete"]
					}, true);
					if(json.type) {
						t.set("runtype", json.type);
					}
					if(json.total && json.total.errors > 0) {
						domStyle.set(t._flowErrorImg, "display", "");
					} else {
						domStyle.set(t._flowErrorImg, "display", "none");
					}
				} catch(err) {
					console.log(err);
				}
				
			} else if(event.type == "di.ci.start" && event.id == t.getSolutionName()) {
				t.updateSchedule();
				
			} else if(event.type == "di.ci.stop" && event.id == t.getSolutionName()) {
				t.updateSchedule();
				
			} else if(event.type == "di.ci.file.reloaded" && thisconfig) {
				if(t.scheduleRestartPending) {
					tdiapi.startSchedule(t.getSolutionName(), t.getFlowName()).then(
						function ok() {
							setTimeout(lang.hitch(t, "updateSchedule"), 3000);
						},
						tdiutil.error
					);
					t.scheduleRestartPending = false;
				}
				
			} else if(event.type == "di.server.start") {
				// -- reconnected
				t._alStopped();
			}
		},
		
		_alStarted: function(event, clearstats) {
			var t = this;
			t.enableRunButtons(false, clearstats);
			t.status.innerHTML = "";
			t._pollAlURL = "/rest/ci/" + t.getSolutionName() + "/al/" + event.data.value;
			t._pollStopEvent(event);
			t._isRunning = true;
		},
		
		_alStopped: function() {
			var t = this;
			t.alentry = null;
			t.enableRunButtons(true);

			// delete the poll url to stop polling for al status
			if(t._pollAlURL)
				delete t._pollAlURL;

			// -- update logs if user is watching it
			if(t._isLogViewOpen()) {
				t._onViewHistory(true);
			}
			
			t._isRunning = false;
			t.set("runtype", "");
		},
		
		_pollStopEvent: function() {
			// summary:
			//		Server events may be lost so the moment we know
			//		a Flow is running we keep checking the run status
			//		to detect its termination in case we lose the event.
			var t = this;
			if(t._pollAlURL) {
				dojo.xhrGet({
					handleAs: "text",
					preventCache: true,
					url:t._pollAlURL
				}).then(
					function(data) {
						// if we get data for the URL it is still running
						setTimeout(lang.hitch(t, "_pollStopEvent"), 5000);
					},
					function (fail) {
						// not there - generate al.stop event and clear poll URL
						if(t._pollAlURL) {
							t._alStopped();
						}
					}
				);
			}
		},
		
		summary: function(obj) {
			return obj.add + " / " + obj.modify + " / " + obj["delete"];
		},
		
		resize: function(obj) {
			if(obj && this.testrun) {
				this.testrun.resize(obj);
			}
		},
		
		getConfigLabel: function() {
			var name = this.item.config.getName();
			var arr = /(Flow_)(.*)/.exec(name);
			if(arr.length == 3)
				return arr[2];
			else
				return name;
		},
		
		pretty: function(str) {
			var arr = /(.*):\/(AssemblyLines|Connectors)\/(.*)/.exec(str);
			if(arr && arr.length == 4)
				return this.pretty(arr[3]);
			arr = /(Source_|Target_)(.*)/.exec(str);
			if(arr && arr.length == 3)
				return arr[2];
			
			return str;
		},
		
		updateFields: function(ctype) {
			var t = this;
			
			if(t.item && t.item.config) {
				var alconfig = t.item.config;
				var config = alconfig.getTop();
				
				t.flowLabel.set("label", "<b>" + t.getConfigLabel() + "</b>");
				
				// Source name
				var conn = alconfig.getConnector("Input");
				if(conn.isAssemblyLineConnector()) {
					t.source.innerHTML = t.pretty(conn.getConnectionConfig().getParam("assemblyLine"));
				} else {
					t.source.innerHTML = t.pretty(conn.getInheritFrom());
				}
				
				// Target name
				/*
				var conn = alconfig.getConnector("Output");
				if(conn) {
					if(conn.isAssemblyLineConnector()) {
						t.target.innerHTML = t.pretty(conn.getConnectionConfig().getParam("assemblyLine"));
					} else {
						t.target.innerHTML = t.pretty(conn.getInheritFrom());
					}
				}
				*/
				
				// Join name
				var conn = alconfig.getConnector("Join");
				if(conn) {
					if(conn.getConnectionConfig().getParamBoolean("join.perform", false)) {
						t.source.innerHTML = t.source.innerHTML  + " (+" + t.pretty(conn.getInheritFrom()) + ")";
					}
				}
				
				// stop/start schedule
				if(ctype == "Schedule") {
					var schedule = config.getScheduleForAssemblyLine(alconfig.getName());
					if(schedule) {
						schedule = config.getSchedule(schedule);
						if(schedule) {
							var simulate = t.getFlowParameterBool("simulate", false);
							if(simulate && schedule.getEnabled()) {
								if(t.scheditor) {
									t.scheditor.setScheduleEnabled(false);
								}
								idx.warn(t.getString("FDS.syncWithSimulateWarning"));
							} else if(schedule.getEnabled()) {
								t.scheduleRestartPending = true;
							} else if(!schedule.getEnabled()) {
								t.scheduleRestartPending = false;
								tdiapi.stopSchedule(t.getSolutionName(), alconfig.getName()).then(
									function ok() {
									},
									tdiutil.error
								);
							}
							setTimeout(lang.hitch(t, "updateSchedule"), 1000);
						}
					} else {
						t.scheduleRestartPending = false;
						tdiapi.stopSchedule(t.getSolutionName(), alconfig.getName());
						setTimeout(lang.hitch(t, "updateSchedule"), 1000);
					}
				}
			}
		},
		
		destroyRecursive: function() {
			this.inherited(arguments);
			if(this._eventsHandler) {
				tdiapi.unsubscribeServerEvents(this._eventsHandler);
			}
			if(this.flowEditor && this.flowEditor.orgDestroyRecursive) {
				try {
					this.flowEditor.destroyRecursive = this.flowEditor.orgDestroyRecursive;
					this.flowEditor.destroyRecursive();
				} catch(err) {}
			} 
		},

		getSolutionName: function() {
			// summary:
			//		returns the config id for this flow
			return this.item.config.getTop().getConfigName();			
		},
		
		getLogFolder: function() {
			// summary:
			//		returns the log folder path for this project
			try {
				var value = this.item.config.getTop().getConnector(LDSUtil.generalSettingsConn).getConnectionConfig().getParam("global.logDirectory");
				if(!value || value.length == 0)
					return "LDAPSync/logs";
				else
					return value;
			} catch(err) {
				console.log(err);
				return "LDAPSync/logs";
			}
		},
		
		getFlowName: function() {
			// summary:
			//		returns the full name for this flow
			return this.item.config.getName();			
		},
		
		getTimeLabel: function(time) {
			// summary:
			//		Returns Time is time is same date as today.
			//		Otherwise returns Date + Time
			return this.formatDate(new Date(time));
			/*
			var ndate = new Date().toLocaleDateString();
			var date = new Date(time).toLocaleDateString();
			var time = new Date(time).toLocaleTimeString();
			if(ndate == date)
				return time;
			else
				return date + " " + time;
			*/
		},
		
		formatDate: function(time) {
			// summary:
			//		Returns a date object in the form yyyy-mm-dd hh:mm:ss
			return dateLocale.format(time, "short");
		},
		
		updateSchedule: function() {
			var t = this;
			tdiapi.getActiveSchedules().then(function(data) {
				var arr = data[t.getSolutionName()];
				if(lang.isArray(arr)) {
					arr = array.filter(arr, function(sched) {
						return sched.assemblyLineName == t.item.config.getName();
					});
					if(arr.length == 1) {
						if(arr[0].isKeepAlive == "true") {
							t.setScheduleLabel(t.getString("FDS.scheduleRealtime"));
						} else {
							var time = t.remoteToLocalTime(arr[0].nextRun);
							t.setScheduleLabel(t.getTimeLabel(time));
						}
					} else {
						t.setScheduleLabel(t.getString("FDS.noSchedule"));
					}
				} else {
					t.setScheduleLabel(t.getString("FDS.noSchedule"));
				}
			});
		},
		
		remoteToLocalTime: function(time) {
			var t = this;
			var tz = t.getServerTimeZone();
			if(tz && tz.offset) {
				// convert javascript minutes offset to msec after UTC
				var localtz = (new Date().getTimezoneOffset() * 60000) * -1;
				
				// make remote time UTC
				time = time - localtz;
				
				// add remote timezone offset
				time = time + tz.offset;
			}
			
			return time;
		},
		
		updateActivityView: function(arr) {
			var t = this;
			
			t.statUsers.innerHTML = "----";
			t.statGroups.innerHTML = "----";
			t.statDate.innerHTML = "----";
			t.statTotal.innerHTML = "----";
			
			if(arr && arr.length > 0) {
				var obj = arr[0];
				if(obj && obj.users) {
					t.statUsers.innerHTML = obj.users.add + " / " + obj.users.mod + " / " + obj.users.del;
				}
				if(obj && obj.groups) {
					t.statGroups.innerHTML = obj.groups.add + " / " + obj.groups.mod + " / " + obj.groups.del;
				}
				if(obj && obj.date) {
					t.statDate.innerHTML = obj.date;
				}
				
				var total = {users:0, groups:0};
				array.forEach(arr, function(item) {
					total.users += parseInt(item.users.add);
					total.users -= parseInt(item.users.del);
					total.groups += parseInt(item.groups.add);
					total.groups -= parseInt(item.groups.del);
				});
				
				//
				// -- Check first entry (the last run) for errors and
				// -- signal the error in the status panel.
				//
				item = arr[0]
				if(item && item.errors > 0) {
					domStyle.set(t._flowErrorImg, "display", "");
				} else {
					domStyle.set(t._flowErrorImg, "display", "none");
				}
				
				// 
				// -- Last item is the migrate run; add mod counts to total entries
				// -- We do this since the most likely scenario is that modifications
				// -- during migrate are to existing entries and not duplicate ones in
				// -- the endpoint. Our total is meant as "affected" entries and not necessarily
				// -- created by the flow.
				//
				var item = arr.pop();
				total.users += parseInt(item.users.mod);
				total.groups += parseInt(item.groups.mod);
				t.updateTotals(total);
			}
		},
		
		showErrorLog: function() {
			if(!this._isLogViewOpen())		
				this._onViewHistory();
		},
		
		updateTotals: function(total, increment) {
			var t = this;
			if(t.total && increment) {
				t.statTotal.innerHTML = (t.total.users + total.users) + " / " + (t.total.groups + total.groups);
			} else {
				t.statTotal.innerHTML = total.users + " / " + total.groups;
				t.total = total;
			}
		},
		
		setScheduleLabel: function(label) {
			this.schedule.set("label", label);
		},
		
		setScheduleTooltip: function(tooltip) {
			this.schedule.set("title", tooltip);
		},
		
		editSchedule: function() {
			var t = this;
			t.scheditor = new ALSchedule({});
			t.scheditor.setConfig(t.item.config);
			t.scheditor.onModify = lang.hitch(this, "updateFields", "Schedule");
			var dlg = new Dialog({
				title:t.getString("WebCE.schedule"),
				content:t.scheditor
			});
			dlg.show();
		},
		
		toggleEditor: function() {
			this._onViewHistory();
		},
		
		getActivityLog: function() {
			var t = this;
			
			// 
			// -- Get and parse the activity log
			//
			LDSUtil.getLdapSyncLogFile(t.getLogFolder() + "/" + t.item.id + "_Activity.log").then(function(data) {
				var arr = new Array();
				var darr = data.split("\n");
				if(darr.length > 0) {
					var fieldnames = darr.shift();
					array.forEach(darr, function(line) {
						var l = line.split(",");
						if(l && l.length > 14) {
							var time = t.getTimeLabel(t.remoteToLocalTime(parseInt(l[0])));
							arr.push({
								date:time,
								users: {
									add:l[1],
									mod:l[2],
									del:l[3]
								},
								groups: {
									add:l[5],
									mod:l[6],
									del:l[7]
								},
								containers: {
									add:l[9],
									mod:l[10],
									del:l[11]
								},
								errors: l[13],
								warnings: l[14]
							});
						} else if(l && l.length > 11) {
							var time = t.getTimeLabel(t.remoteToLocalTime(parseInt(l[0])));
							arr.push({
								date:time,
								users: {
									add:l[1],
									mod:l[2],
									del:l[3]
								},
								groups: {
									add:l[4],
									mod:l[5],
									del:l[6]
								},
								containers: {
									add:l[7],
									mod:l[8],
									del:l[9]
								},
								errors: l[10],
								warnings: l[11]
							});
						}
					});
					arr.reverse();
				}
				t.updateActivityView(arr);
			}, function(err) {
			});

			//
			// -- Get a list of all log files and figure out how many
			// -- cycles there are for this flow.
			//
			var cycles = {};
			LDSUtil.getLdapSyncLogFiles(t.getLogFolder(), true).then(function(data) {
				var me = new RegExp("^" + t.item.id + "_([^_]+)\\.(\\d+)");
				var metop = new RegExp("^" + t.item.id + "_([^_]+)");
				array.forEach(data, function(entry) {
					var line = entry.name;
					var match = me.exec(line);
					if(match && match.length == 3) {
						var cyc = cycles[match[2]] || {};
						cyc[match[1]] = entry;
						cycles[match[2]] = cyc;
					} else {
						match = metop.exec(line);
						if(match && match.length == 2) {
							var cyc = cycles["0"] || {};
							cyc[match[1]] = entry;
							cycles["0"] = cyc;
						}
					}
				});
				t.cycles = cycles;
				t.updatePopupMenu();
			});
		},
		
		updatePopupMenu: function() {
			// summary:
			//		Update the menu items for the log selection dropdown
			var t = this;
			var i = 0;
			var menu = t.logsetMenu;
			array.forEach(menu.getChildren(), function(child) {
				menu.removeChild(child);
			});

			var labels = [];
			for(i = 0; i < 10; i++) {
				if(t.cycles[i]) {
					var arr = [];
					for(f in t.cycles[i]) {
						var obj = t.cycles[i][f];
						if(arr.length == 0) {
							var time = new Date(t.remoteToLocalTime(parseInt(obj.modified)));
							arr.push(t.formatDate(time));
						}
					}
					if(t.cycles[i]["LDAPMigrate.log"])
						arr.push(" - " + this.getString("FDS.runMigrateTitle"));
					else
						arr.push(" - " + this.getString("FDS.runSyncTitle"));
					
					var label = arr.join(" ");
					labels.push(label);
					menu.addChild(new MenuItem({
						label:label,
						onClick:lang.hitch(t, function(cycle, title) {
							t.logsetButton.set("label", title);
							t._onViewHistory(true, cycle);
						}, i, label)
					}));
				}
			}
			if(labels.length > 0)
				t.logsetButton.set("label", labels[t.showCycle ? t.showCycle : 0]);
		},
		
		getServerTimeZone: function() {
			var t = this;
			if(!t._serverTZ) {
				tdiapi.getTimeZone().then(function(tz) {
					t._serverTZ = tz;
				});
			}
			return t._serverTZ;
		},
		
		isRunning: function() {
			return this._isRunning;
		},
		
		_toggleTools: function(mouse) {
//			var display = html.style(this.toggleTools, "display");
//			html.style(this.toggleTools, "display", (display == "none" ? "" : "none"));
		},
		
		_toggleClose: function(mouse) {
			domClass.toggle(this.closeNode, "dijitTabCloseButtonHover");
		},
		
		_run: function(type) {
			var t = this;
			if(type == "migrate" || type == "simulate") {
				tdiapi.getActiveSchedules().then(function(schedules) {
					if(schedules) {
						var s = schedules[t.getSolutionName()];
						if(lang.isArray(s)) {
							if(array.some(s, function(item) {
								return item.assemblyLineName == t.item.config.getName();
							})) {
								idx.info(t.getString("FDS.disableScheduleBeforeMigrate"));
								return;
							}
						}
					}
					t._onStartFlow(type);
				});
			} else {
				this._onStartFlow(type);
			}
		},
		
		_runWithOptions: function() {
			var t = this;
			var runOptions = new LDSRunOptions({});
			runOptions.startup();
			var runButton = new Button({label:t.getString("WebCE.ok")});
			var dlg = new Dialog({
				title: t.getString("FDS.runSync"),
				content: runOptions,
				closeButtonLabel:t.getString("WebCE.cancel"),
				buttons:[runButton]
			});
			runButton.onClick = function() {
				t._run(runOptions.getValue().runoption);
				dlg.hide();
			}
			dlg.show();
		},
		
		startup: function() {
			var t = this;
			t.set("runtype", "");
			t.inherited(arguments);
			t.getServerTimeZone();
			t.enableRunButtons(true);
			t.updateFields();
			t._eventsHandler = tdiapi.subscribeServerEvents(lang.hitch(this, "_updateRunStatusEvent"));
			corefx.chain([
			    fx.fadeOut({node:t.domNode}),
			    fx.fadeIn({node:t.domNode})
			]);
			t.updateSchedule();
			t._schedPoll = setTimeout(lang.hitch(t, "updateSchedule"), 15*1000);
			t.getActivityLog();
			
			//
			// -- listen for changes to Join component so 
			// -- we can update the flow display label
			//
			var join = t.item.config.getConnector("Join");
			if(join) {
				t._modid = aspect.after(t.item.config, "onModify", lang.hitch(t, function(modified, args) {
					if(args.length == 2) {
						var param = args[1] && args[1].param ? args[1].param : null;
						if(param && typeof(param) == "object")
							param = param.name;
						if(param == "complexConfig.inheritFrom" || param == "join.perform") {
							t.updateFields();
						}
						t.changesPending = true;
					}
				}));
			}

			//
			// -- change status icon based on the value of runtype
			//
			this.own(this.watch("runtype", function(name, oldvalue, value) {
				if(value != oldvalue) {
					t.updateStatusIcon();
					t.updateChangesPending();
				}
			}));
			
			// -- when config is saved force the icon to appear
			this.own(topic.subscribe("ldapsync/configsave", function() {
				t.updateChangesPending();
			}));
		}
	})
});
