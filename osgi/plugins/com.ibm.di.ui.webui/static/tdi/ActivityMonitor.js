/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/MenuItem",
	"tdi/TableWidget",
	"tdi/model/ServerProjectsModel",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"tdi/tdiutil",
	"idx/dialogs",
	"dojo/text!./templates/ActivityMonitor.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplate, MenuItem, TableWidget, ServerProjectsModel, tdiapi, tdiconstants, tdiconfigentry, tdicientry, tdiutil, idx, template) {
	
return declare(
	[ _Widget, _TemplatedMixin ],
	{
		templateString : "<div style='width:100%; height:100%; margin:0; padding:0' data-dojo-attach-point='Main'></div>",
		
		status: {
			"di.ci.start":"Running",
			"di.ci.stop": "Stopped"
		},		
		
		resize: function(obj) {
			this.inherited(arguments);
			if(obj && obj.h && this.table) {
				this.table.resize(obj);
			}
		},
	
		_addEvent : function(data) {
			var id = data.id;
			if(id) {
				if(data.type == "di.ci.file.updated") {
					this.refreshConfigs();
					
				} else if ( data.type == "di.ci.start" || data.type == "di.ci.stop" ){
					var item = this.table.getItem(id);
					if(!item) {
						item = {id:id};
						this.table.addItem({id:id});
						item = this.table.getItem(id);
					}
					var status = this.status[data.type];
					this.table.setItemValue(item, "status", status);
					if(status == "Stopped") {
						if(id.indexOf("TDIDashboard_TEMP") == 0 || !item.entry) {
							this.table.deleteItem(item);
						}
					}
				}
			}
			this.table.setStore(this.store);
		},
		
		unlockConfig: function() {
			array.forEach(this.table.getSelectedRows(), function(id) {
				var configentry = this.table.getItemValue(id, "entry");
				if(configentry.getLink("lock")) {
					dojo.when(tdiapi.unlockConfig(configentry), function() {
						idx.info("Solution unlocked: " + id);
					}, tdiapi.defaultErrHandler);
				} else {
					idx.info("Solution is not locked: " + id);
				}
			}, this);
		},
		
		startConfig: function() {
			var table = this.table;
			var item = table.getSelectedItem();
			var entry = item ? table.getItemValue(item, "entry") : null;
			if(entry) {
				tdiapi.startConfig(entry);
			}
		},
		
		stopConfig: function() {
			var table = this.table;
			var item = table.getSelectedItem();
			var cientry = item ? table.getItemValue(item, "cientry") : null;
			if(cientry) {
				tdiutil.ask("Stop " + item.id, function(ok) {
					if(ok) {
						tdiapi.stopConfig(cientry);
					}
				});
			}
		},
		
		deleteConfig: function() {
			var table = this.table;
			var item = table.getSelectedItem();
			var entry = item ? table.getItemValue(item, "entry") : null;
			if(entry) {
				tdiutil.ask("Permanently delete " + item.id, function(ok) {
					if(ok) {
						tdiapi.deleteConfig(entry);
						table.deleteItem(item);
					}
				});
			}
		},
		
		openConfig: function(event) {
			var item = this.store.get(event.rowId);
//			if(item.cientry) {
//				
		},
		
		refreshConfigs: function() {
			var table = this.table;
			tdiapi.getServerProjects().then(function(data) {
				array.forEach(data.items, function(item) {
					if(table.getItem(item.id) == null) {
						table.addItem(item);
					}
				});
			});
		},
		
		addMenuItems: function() {
			this.table.addToRowMenu(new MenuItem({
				label:"Stop",
				onClick:lang.hitch(this, "stopConfig")
			}));
			this.table.addToRowMenu(new MenuItem({
				label:"Start",
				onClick:lang.hitch(this, "startConfig")
			}));
			this.table.addToRowMenu(new MenuItem({
				label:"Delete",
				onClick:lang.hitch(this, "deleteConfig")
			}));
			this.table.addToRowMenu(new MenuItem({
				label:"Unlock",
				onClick:lang.hitch(this, "unlockConfig")
			}));
			dojo.subscribe(tdiconstants.serverEventsSubject, dojo.hitch(this, "_addEvent"));
			tdiapi.startServerEventNotifications();
		},
		
		postCreate : function() {
			var structure = [
	  			{field:"id", id:"id", name:"Solution", width:"70%"},
	  			{field:"status", id:"status", name:"Status", width:"30%"}
	  		];
			var table = this.table = new TableWidget({
				structure:structure,
				toolbar:false,
				onRowClick:lang.hitch(this, "openConfig")
			}).placeAt(this.Main);
			this.table = table;
			
			//
			// -- Update menu items based on selection
			//
			this.connect(table, "onSelected", function(row) {
				var item = row.id;
				var menuItems = table.getMenuItems();
				menuItems[0].set("disabled", table.getItemValue(item, "cientry") == null);
				menuItems[1].set("disabled", table.getItemValue(item, "cientry") != null);
				menuItems[2].set("disabled", table.getItemValue(item, "entry") == null);
				var entry = table.getItemValue(item, "entry");
				if(entry && entry.getLink("lock"))
					menuItems[3].set("disabled", false);
				else
					menuItems[3].set("disabled", true);
			});
			table.startup();
			
			dojo.when(tdiapi.getServerProjects(), lang.hitch(this, function(data) {
				this.store = this.table.newStore(data.items);
			})).then(lang.hitch(this, function() {
				dojo.when(tdiapi.getConfigInstances(), lang.hitch(this, function(data) {
					array.forEach(data.entry, function(entry) {
						var item = this.store.get(entry.title.value);
						if(!item) {
							this.store.put({id:entry.title.value});
							item = this.store.get(entry.title.value);
						}
						item.cientry = [new tdicientry({atom:entry})];
						item.status = [this.status["di.ci.start"]];
					}, this);
					this.table.setStore(this.store);
					this.addMenuItems();
				}));
			}));
			
			this.inherited(arguments);
		}
	});

});