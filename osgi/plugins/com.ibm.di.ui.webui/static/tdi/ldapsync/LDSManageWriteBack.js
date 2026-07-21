/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/dialogs",
	"idx/widget/SingleMessage",
	"./LDSUtil",
	"gridx/Grid",
    "gridx/modules/CellWidget", 
    "gridx/modules/ColumnResizer", 
    "gridx/modules/Edit", 
    "gridx/modules/Focus",
    "gridx/modules/SingleSort",
    "gridx/modules/ToolBar",
    "gridx/modules/select/Row",
    "gridx/modules/IndirectSelect",
	"gridx/modules/extendedSelect/Row",
	"gridx/modules/RowHeader",
	"gridx/modules/filter/Filter",
	"idx/gridx/modules/filter/QuickFilter",
    "gridx/core/model/cache/Async",
    "dojo/store/Memory",
	"tdi/ToolbarLabel",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSManageWriteBack.html"
], function(declare, array, lang, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, idx, SingleMessage, LDSUtil,
		Grid, CellWidget, ColumnResizer, Edit, Focus, SingleSort, ToolBar, SelectRow, 
		IndirectSelect, RowSelect, RowHeader, Filter, QuickFilter, Async, Memory, ToolbarLabel, tdiapi, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		

		createGrid: function() {
			this.mapStore = new Memory({
				idProperty:"id",
				data: []
			});
			
			var struc = [
			    {id:"timestamp", 	field:"timestamp", name:this.getString("FDS_writeBack_timestamp"), width:"15%"},
			    {id:"flow",			field:"flow", name:this.getString("FDS_flow"), width:"15%"},
			    {id:"changenumber", field:"changenumber", name:this.getString("FDS_writeBack_changenumber"), width:"15%"},
			    {id:"changes",		field:"changes", name:this.getString("FDS_writeBack_changes"), width:"10%"},
			    {id:"targetdn",		field:"targetdn", name:this.getString("FDS_writeBack_targetdn"), width:"30%"},
			    {id:"adDn",			field:"adDn", name:this.getString("FDS_writeBack_addn"), width:"15%"}
			];
			
			this.mapGrid = new Grid({
				cacheClass: Async,
				store:this.mapStore,
				'class': 'gridxAlternatingRows',
				structure: struc,
				modules: [
				   RowHeader, SingleSort, ToolBar, Focus, ColumnResizer, Filter, QuickFilter
				],
				style:"width:100%; height:100%"
			}).placeAt(this._grid);
			this.mapGrid.startup();
		},
		
		getTitle: function() {
			return this.getString("FDS.writeBack");
		},
		
		resize: function(obj) {
			if(this._border && this._border.resize) {
				this._border.resize(obj);
			}
			if(this.mapGrid) {
				this.mapGrid.resize();
			}
		},
		
		_toggleSchedule: function() {
			var t = this;
			var enable = t._enabled.get("value") ? "true" : "false";
			/*
			if(enable && !t._schedule) {
				tdiapi.startSchedule(LDSUtil.projectName, LDSUtil.writebackSchedule).then(function(data) {
					t.checkSchedule();
				}, function(err) {
					t.checkSchedule();
					tdiutil.error(err);
				});
			} else if(!enable && t._schedule) {
				tdiutil.ask(t.getString("FDS.confirmStopWriteBack"), function(ok) {
					if(!ok) {
						t.checkSchedule();
					} else {
						tdiapi.stopSchedule(LDSUtil.projectName, LDSUtil.writebackSchedule).then(function(data) {
							tdiapi.stopAssemblyLine2(LDSUtil.projectName, LDSUtil.writebackAL).then(function(data) {
								t.checkSchedule();
							}, function(err) {
								t.checkSchedule();
							});
						}, function(err) {
							t.checkSchedule();
							tdiutil.error(err);
						})
					}
				});					
			}
			*/
			var conn = this.config.getConnector(LDSUtil.generalSettingsConn).getConnectionConfig();
			if(conn.getParam("writeback.enabled") != enable) {
				conn.setParam("writeback.enabled", enable);
			}
		},
		
		checkSchedule: function() {
			var t = this;
			/*
			tdiapi.getActiveSchedules().then(function(data) {
				var arr = data[LDSUtil.projectName];
				if(lang.isArray(arr)) {
					var arr = array.filter(arr, function(sched) {
						return sched.assemblyLineName == LDSUtil.writebackAL;
					});
					if(arr && arr.length == 1) {
						t._schedule = arr[0];
						t._enabled.set("value", true);
					} else {
						t._schedule = null;
						t._enabled.set("value", false);
					}
				}
			});
			*/
		},
		
		loadItems: function() {
			var t = this;
			dojo.xhrGet({
				handleAs:"json",
				url:"/fds/ldapsync/writeback/items?count=50&config=" + t.config.getConfigName()
			}).then(function(data) {
				array.forEach(data, function(item) {
					t.addItem(item);
				})
			});
		},
		
		_serverEvent: function(event) {
			if(event.type == "user.fds.writeback") {
				// -- Add writeback event
				try {
					var data = dojo.fromJson(event.data.value);
					this.addItem(data);
				} catch(err) {
					console.log(err);
				}
			} else if(event.id == "AssemblyLines/WritebackMain" && event.ciId == "LDAPSync") {
				// -- Recheck schedule whenever main WB starts/stops
				if(event.type == "di.al.start" || event.type == "di.al.stop") {
					this.checkSchedule();
				}
			}
		},
		
		setErrorMessage: function(msg) {
			if(msg)
				this._status.setLabel(msg);
			else
				this._status.setLabel("");
			/*
			if(!msg) {
				if(this._message) {
					this._message.destroyRecursive();
					delete this._message;
				}
			} else {
				this._message = new SingleMessage({ 
				  type: "error",  
				  title: msg, 
				  dateFormat:{ 
				     selector: "time",  
				     timePattern: "hh:mm a" 
				  },
				  style:"width:500px",
				  messageNumber: 1,  
				  description: msg 
				});
				this._message.startup();
				this._toolbar.addChild(this._message);
			}
			*/
		},
		
		addItem: function(item) {
			if(item.config && item.config == this.config.getConfigName()) {
				if(item.timestamp) {
					item.timestamp = tdiutil.formatDate(new Date(item.timestamp));
				}
				if(item.flow && item.flow.match(/^Flow_/))
					item.flow = item.flow.substring(5);
				this.mapStore.add(item);
			}
		},
		
		onIgnoreSDI: function() {
			var enabled = this.wbIgnoreSDI.get("value") == "on";
			var conn = LDSUtil.getGeneralSettingsConnector(this.config).getConnectionConfig();
			
			var ignoreChanges= conn.getParamBoolean("writeback.ignoreChangesBySDI", true);
			if(enabled != ignoreChanges)
				conn.setParam("writeback.ignoreChangesBySDI", enabled);
		},

		startup: function() {
			var t = this;
			var conn = LDSUtil.getGeneralSettingsConnector(t.config).getConnectionConfig();
			var enabled = conn.getParam("writeback.enabled");
			if(enabled && enabled != "false") {
				t._enabled.set("value", true);
			}
			
			var ignoreChanges= conn.getParamBoolean("writeback.ignoreChangesBySDI", true);
			if(ignoreChanges) {
				t.wbIgnoreSDI.set("value", true);
			}
						
			t.createGrid();
			t.checkSchedule();
			t.loadItems();
			this.own(tdiapi.subscribeServerEvents(lang.hitch(this, "_serverEvent")));
			t.resize();
		}
	})
});