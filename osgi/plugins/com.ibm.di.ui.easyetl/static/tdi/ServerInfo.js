dojo.provide("tdi.ServerInfo");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.Tree");
dojo.require("dijit.Dialog");
dojo.require("dojox.grid.DataGrid");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.FormWidget");
dojo.require("tdi.model.InstalledComponentsStore");
dojo.require("tdi.DashboardAuth");
dojo.require("tdi.TreeTableWidget");

dojo.declare("tdi.ServerInfo",
	[dijit._Widget, dijit._Templated,tdi.NlsMixin],
	{
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/ServerInfo.html"),
		
		//
		showInstalledComponents: true,
		showSystemStores: true,
		showLogging: true,
		showDashboard: true,
		
		//
		// widget attributes
		//
		serverVersion: "",
		hostname: "",
		ipAddress: "",
		operatingSystem: "",
		serverBootTime: "",
		serverId: "",
		
		//
		// mapping to DOM nodes in template
		//
		attributeMap : {
			serverVersion: {node:"version", type:"innerHTML" },
			hostname : {node:"hostname", type:"innerHTML" },
			ipAddress : {node:"ipAddress", type:"innerHTML" },
			operatingSystem : {node:"os", type:"innerHTML" },
			serverBootTime : {node:"serverBootTime", type:"innerHTML" },
			serverId : {node:"serverID", type:"innerHTML" }
		},
		
		loadStatus : function(data) {
			for(f in data) {
				this.attr(f, data[f]);
			}
		},
		
		showThreads : function(data) {
			var layout = [
			    {field:"name", name:this.getString("name"), width:"auto"},
			    {field:"state", name:this.getString("WebCE.vmThreadState"), width:"150px"},
			    {field:"id", name:this.getString("WebCE.vmID"), width:"100px"},
			    {field:"group", name:this.getString("WebCE.vmThreadGroup"), width:"150px"}
			];
			
			this.threads = new tdi.TreeTableWidget({
				storeIdAttribute:"id",
				storeLabelAttribute:"name",
				getTreeTableLayout:function() {
					return layout;
				},
				getToolbarVisible : function() {
					return true;
				},
				onRefresh: dojo.hitch(this, function() {
					dojo.when(tdiapi.getActiveThreads(), dojo.hitch(this, "updateThreads"));
				})
			});
	  		this.threads.startup();
			  		
			var tab = new dijit.layout.ContentPane({
				title:this.getString("WebCE.vmstatusThreads"),
				content:this.threads,
				style:"height:100%"
			});		
			this.TabContainer.addChild(tab);
			
			dojo.when(tdiapi.getActiveThreads(), dojo.hitch(this, "updateThreads"));
		},
		
		updateThreads : function(data) {
			this.threads.removeAllItems();
			var store = this.threads.getStore();
			dojo.forEach(data, dojo.hitch(this, function(item) {
				store.newItem(item);
			}));
		},
		
		showComponents:function(data) {
			this.store = new tdi.model.InstalledComponentsStore({
				loadCompleted:dojo.hitch(this, "updateComponents")
			});
			this.store.loadData();
		},
		
		showComponentForm : function(item) {
			dojo.when(tdiapi.getComponentForm(item.url[0]), dojo.hitch(this, function(data) {
				var form = new tdi.FormWidget({
					formData:data,
					visibleButtons: [false, false, false, false]
				});
				var dlg = new dijit.Dialog({
					title:item.name[0],
					content:form
				});
				dlg.show();
			}));
		},
		
		updateComponents : function() {
			var layout = [
			    {field:"name", name:this.getString("name"), width:"auto"},
			    {field:"type", name:this.getString("type"), width:"auto"}
			];
			  		
			this.tree = new tdi.TreeTableWidget({
				store:this.store,
				getTreeTableLayout:function() {
					return layout;
				},
				getToolbarVisible : function() {
					return false;
				},
				onRowDblClick: dojo.hitch(this, "showComponentForm")
			});
	  		this.tree.startup();
			  		
			var tab = new dijit.layout.ContentPane({
				title:this.getString("WebCE.installedComponents"),
				content:this.tree,
				style:"height:100%"
			});		
			this.TabContainer.addChild(tab);
		},
		
		updateTableEditor : function(data) {
			var layout = [];
			dojo.forEach(data.ColumnData.Value, function(str) {
				layout.push({
					field:str,
					width:"150px"
				});
			});
			
			var storeData = {
					id:"ID",
					name:"ID",
					items: []
			};
			
			dojo.forEach(data.RowData.Data, function(row) {
				var obj = {};
				for(var i = 0; i < row.Value.length; i++) {
					obj[data.ColumnData.Value[i]] = row.Value[i];
				}
				storeData.items.push(obj);
				// Tombstones use GUID as its unique key
				if(obj.GUID)
					storeData.id = "GUID";
			});
			
			var store = new dojo.data.ItemFileWriteStore({data:storeData});
			
			if(this.tableEditor != null) {
				this.tableEditor.destroy();
			}
			this.tableEditor = new dojox.grid.DataGrid({
				structure:layout,
				store:store,
				style:"height:100%"
			}).placeAt(this.tableEditorPlaceholder);
			this.tableEditor.startup();
		},
		
		showTableContents : function(e) {
		},
		
		showStores : function(data) {
			var div = dojo.create("table", {width:"100%", height:"100%"});
			var tr = dojo.create("tr", {}, div);
			var td = dojo.create("td", {valign:"top", width:"30%"}, tr);
			
			var store = new dojo.data.ItemFileReadStore({data:data});
		    var model = new dijit.tree.ForestStoreModel({
		        store: store,
		        rootId: "serverStores",
		        rootLabel: "Server Stores",
		        childrenAttrs: ["items"]
		    });
//			var tree = new dijit.Tree({
//				model:model,
//				showRoot:false,
//				style:"height:100%"
//			}, td);
		    this.tree = new dojox.grid.DataGrid({
		    	structure: [{field:"id", name:this.getString("WebCE.serverStores"), width:"auto"}],
		    	store:store,
		    	style:"height:100%"
		    }).placeAt(td);
			dojo.connect(this.tree, "onClick", dojo.hitch(this, function() {
				var table = this.tree.selection.getFirstSelected();
				if(table && table.id)
					table = table.id[0];
				if(table)
					dojo.when(tdiapi.getServerStoreData(table), dojo.hitch(this, "updateTableEditor"));
				
			}));
			
			this.tableEditorPlaceholder = dojo.create("td", {valign:"top", width:"70%"}, tr);
			
			var tab = new dijit.layout.ContentPane({
				title:this.getString("WebCE.serverStores"),
				content:div,
				style:"height:100%"
			});		
			this.TabContainer.addChild(tab);
		},
		
		showDashboard : function() {
			var auth = new tdi.DashboardAuth({});
			var tab = new dijit.layout.ContentPane({
				title:this.getString("WebCE.securityTitle"),
				content:auth,
				style:"height:100%"
			});		
			this.TabContainer.addChild(tab);			
		},
		
		showLogSettings : function(data) {
			var tab = new dijit.layout.ContentPane({
				title:this.getString("WebCE.logAndTombstones"),
				content:new tdi.GlobalLogSettings(),
				style:"height:100%"
			});		
			this.TabContainer.addChild(tab);
		},
		
		postCreate : function() {
			dojo.when(tdiapi.getServerInfo(), dojo.hitch(this, "loadStatus"));
			if(this.showInstalledComponents) {
				this.showComponents();
			}
			if(this.showSystemStores) {
				dojo.when(tdiapi.getServerStores(), dojo.hitch(this, "showStores"));
			}
			if(this.showLogging) {
				this.showLogSettings();
			}
			if(this.showDashboard) {
				this.showDashboard();
			}
			this.showThreads();
		}
	}
);
