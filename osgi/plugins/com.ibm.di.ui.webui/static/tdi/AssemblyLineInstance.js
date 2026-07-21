/*
 * tdi.AssemblyLineInstance
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_Templated",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/Menu",
	"dijit/MenuItem",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/NlsMixin",
	"tdi/tdiatom",
	"dojo/text!./templates/AssemblyLineInstance.html"
], function(declare, _Widget, _Templated, Button, CheckBox, Menu, MenuItem, tdiapi, tdiconfig, NlsMixin, tdiatom, template) {

return declare(
	[_Widget, _Templated, NlsMixin],
	{
		templateString: template,
		
		// config of the assemblyline
		config: null,
		menu: null,

		// attribute map (e.g. dojo.attr(al, "attr", value))
		attributeMap : {
			autoStart: {node:"AutoStart", type:"innerHTML" },
			scheduler : {node:"Scheduler", type:"innerHTML" },
			sequence : {node:"Sequence", type:"innerHTML" },
			assemblyline : {node:"AssemblyLine", type:"innerHTML" },
			started : {node:"Started", type:"innerHTML" },
			lastrun : {node:"Lastrun", type:"innerHTML" },
			nextrun : {node:"Nextrun", type:"innerHTML" },
			cycles : {node:"Cycles", type:"innerHTML" }
		},
		
		_viewLog : function() {
			this.viewLog(this.entry);
		},
		
		viewLog : function(alentry) {
		},
		
		_editConfig : function() {
			this.editConfig(this.entry);
		},
		
		editConfig : function(alentry) {
		},
		
		toggleRun : function() {
			if(this.entry != null) {
				dojo.when(tdiapi.stopAssemblyLine(this.entry), dojo.hitch(this, function() {
					this.entry = null;
					dojo.removeClass(this.ALControl, "activeALIcon");
					this.attr("started", "");
					this.attr("nextrun", "");
					this.attr("cycles", "");
				}));
			} else {
				dojo.when(tdiapi.startAssemblyLine(this.cientry, this.config.getName()), function() {}, tdiapi.defaultErrHandler);
			}
		},
		
		createMenu : function() {
			this.menu = new Menu({
				leftClickToOpen:true
			});
			this.menu.addChild(new MenuItem({
				label:this.getString("WebCE.run"),
				onClick:dojo.hitch(this, "toggleRun")
			}));
			this.menu.addChild(new MenuItem({
				label:this.getString("WebCE.stop"),
				onClick:dojo.hitch(this, "toggleRun")
			}));
			this.menu.addChild(new MenuItem({
				label:this.getString("WebCE.viewLog"),
				onClick:dojo.hitch(this, "_viewLog")
			}));
			this.menu.bindDomNode(this.AssemblyLine);
			dojo.connect(this.menu, "_openMyself", this, function(item) {
				var items = this.menu.getChildren();
				items[0].attr("disabled", this.entry != null);
				items[1].attr("disabled", this.entry == null);
				items[2].attr("disabled", this.entry == null);
			});
		},
		
		updateUI : function(entry) {
			if(entry == null) {
				this.entry = null;
				dojo.removeClass(this.ALControl, "activeALIcon");
			} else {
				this.entry = new tdi.tdialentry({atom:entry});
				dojo.addClass(this.ALControl, "activeALIcon");
				dojo.when(this.entry.getStatus(), dojo.hitch(this, "updateStatus"));
			}
		},
		
		updateStatus : function(data) {
			dojo.forEach(data.stat, function(st) {
				if(st.name == "start") {
					this.attr("started", new Date(st.value).toTimeString());
				} else if(st.name == "get") {
					this.attr("cycles", "" + st.value);
				}
			}, this);
			// Get last tombstone and update lastrun control
			dojo.when(tdiapi.getAssemblyLineTS(this.cientry.getTitle(), this.config.getName()), dojo.hitch(this, "updateLastrun"));
		},
		
		updateLastrun : function(data) {
			if(dojo.isArray(data.tombstone)) {
				this.attr("lastrun", data.tombstone[0].started);
			} else {
				this.attr("lastrun", "?");
			}
		},
		
		updateSchedule : function() {
			var top = this.config.getTop();
			var schedules = top.getScheduleForAssemblyLine(this.config.getName());
			if(schedules == null)
				schedules = "";
			this.attr("scheduler", schedules);
		},
		
		postCreate : function() {
			this.createMenu();
			this.updateSchedule();
		}
	}
)});