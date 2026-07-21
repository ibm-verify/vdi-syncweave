dojo.provide("tdi.AttributeMap");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit._Container")
dojo.require("dijit._HasDropDown")
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");
dojo.require("dijit.Toolbar");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.SimpleTextarea");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");

dojo.require("dojoe.progressbar.EnhancedProgressBar");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");
dojo.require("tdi.AttributeMapItem");
dojo.require("tdi.AttributeMapItemEditor");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.TreeTableWidget");

dojo.declare("tdi.AttributeMap", [ dijit._Widget, dijit._Templated,
		tdi.NlsMixin ], {
	widgetsInTemplate : true,
//	templateString: '<div dojoAttachPoint="DGDiv" style="margins:0px;"></div>',
	templatePath : dojo.moduleUrl("tdi", "templates/AttributeMap.html"),

	// config: tdi.connector
	// 		The connector config
	config : null,
	
	// attribute map object
	attributeMapConfig: null,

	// input: boolean
	// 		Shows the input map (otherwise the output map)
	input : false,

	// editor:
	// 		The tooltip editor
	editor : null,

	onReadNext : function(attributemap) {
		// summary:
		// 		Called when user presses read-next button
	},
	
	onCloseConnection : function(attributemap) {
		// summary:
		// 		Called when user presses close-connection button
	},
	
	showEODMsg : function(show) {
//		if(this.eodmsg == null && show) {
//			this.eodmsg = new dojoe.messagearea.MessageArea({
//				type:"info",
//				message:"No more data to read."
//			});
//			this.DG.addToToolbar(this.eodmsg, 1, 2);
//			this.eodmsg.show();
//		} else if(this.eodmsg != null && !show) {
//			this.DG.removeItemFromToolbar(this.eodmsg.id);
//			this.eodmsg = null;
//		}
	},
	
	showProgressBar : function(show) {
		if(this.progress == null && show) {
			this.progress = new dojoe.progressbar.EnhancedProgressBar({
				progressIndicatorType:0,
				cancellable: true,
				hidden:false,
				closeable:false,		 
				pauseable:false,
				processingLabel:'Reading data...',		
				maximumValue : 100,
				width :'250px',
				height :'50px',
				size:"medium",
				showmessagetext:true
			});
			// Add the bar to the second row
			this.DG.addToToolbar(this.progress, 1, 2);
			this.progress.doProgress(1, "Reading data");
			dojo.subscribe(this.progress.id + "#onCancelClick", dojo.hitch(this, "onCloseConnection"));
		} else if (this.progress != null && !show) {
			this.DG.removeItemFromToolbar(this.progress.id);
			// remoteItemFromToolbar destroys this.progress
			this.progress = null;
		}
	},
	
	_mapSelectedAttributes : function(unmap, e) {
		var arr = this.DG.getSelectedRows();
		var map = this.attributeMapConfig;
		dojo.forEach(arr, dojo.hitch(this, function(item) {
			var name = this.DG.getStore().getValue(item, "name");
			if(!map.isMapped(name) && !unmap) {
				var ami = map.newItem({name:name});
				this.DG.getStore().setValue(item, "mapping", ami.getMapsTo());
			} else if(map.isMapped(name) && unmap) {
				map.removeItem(name);
				this.DG.getStore().setValue(item, "mapping", null);
			}
		}));
	},

	_editAttribute : function(e) {
		// summary:
		// 		Opens the attribute map editor for the specified attmap item
		// div: tdi.AttributeMapItem
		// 		The attribute map item config
		// input: boolean
		// 		True if the map is from the input connector
		var arr = this.DG.getSelectedRows();
		if(arr.length == 0)
			return;
		
		var item = arr[0];
		var attr = this.DG.getStore().getValue(item, "name");
		var map = this.attributeMapConfig;;
		var ami = map.getItem(attr);
		var value = attr;
		if(ami != null)
			value = ami.getMapsTo();
		if(ami == null || ami.isSimple())
			value = this.input ? "conn." + value : "work." + value;

		var div = dojo.create("div");
		var text = new dijit.form.SimpleTextarea({value:value, rows:"12", style:"width:400px"}).placeAt(div);
		var center = dojo.create("center", {}, div);
		var ok = new dijit.form.Button({type:"submit", label:this.getString("ok")}).placeAt(center);
		try {
			var dlg = new dijit.Dialog({
				content:div,
				title:attr
			});
			dojo.connect(dlg, "onExecute", dojo.hitch(this, function(dlg, text, ami, attr) {
				if(ami == null)
					ami = this.attributeMapConfig.newItem({name:attr})
				this.DG.getStore().setValue(item, "mapping", text.get("value"))
				ami.setAdvanced(text.get("value"));
				dlg.hide();
			}, dlg, text, ami, attr));
			dlg.show();
		} catch (err) {
			tdiutil.error(err)
		}
	},

	_getTooltipDialog : function() {
		if (this.editor == null) {
			this.editor = new tdi.AttributeMapItemEditor({});
			this.tooltipDialog = new dijit.TooltipDialog({
				content : this.editor
			});
		}
		return this.tooltipDialog;
	},
	
	_newAttribute : function(e) {
		
		var until = this.config ? this.config.getName() : this.attributeMapConfig.getParent().getName();
		var map = this.config ? this.config.getAttributeMap(this.input) : this.attributeMapConfig;
		
		tdiutil.createNewAttribute(map, until, this.DG.getStore(), dojo.hitch(this, function(newitem) {
		}));
	},
	
	_deleteAttribute : function(e) {
		var arr = this.DG.getSelectedRows();
		if(arr.length == 0)
			return;
		var names = dojo.map(arr, function(item) {
			return item.name[0];
		});
		if(tdiutil.confirm(this.getString("general.delete.label") + "\n" + names.join("\n"), dojo.hitch(this, function(names, buttonId) {
			if(buttonId == 0) {
				var map = this.attributeMapConfig;
				dojo.forEach(names, function(str) {
					map.removeItem(str);
				});
				var parent = this.attributeMapConfig.getParentConfigType("tdi.connector");
				if(parent) {
					var schema = parent.getSchema(this.input);
					dojo.forEach(names, function(str) {
						schema.removeItem(str);
					});
				}
				var store = this.DG.getStore();
				dojo.forEach(this.DG.getSelectedRows(), function(item) {
					store.deleteItem(item);
				});
				store.save();
			}
		}, names)));
		var map = this.attributeMapConfig;
	},
	
	_readNext : function() {
		this.onReadNext(this);
	},
	
	_closeConnection : function() {
		this.onCloseConnection(this);
	},

	setEntry : function(entry) {
		var store = this.DG.getStore();
		var schema = this.config ? this.config.getSchema(this.input) : null;
		var e2 = new tdi.tdientry({data:entry});
		if(schema) {
			dojo.forEach(e2.getNames(), function(attr) {
				if(schema.getItem(attr) == null) {
					schema.newItem({name:attr});
				}
			});
		}
		
		store.fetch({
			query : {},
			onComplete : function (items, request) {
				dojo.forEach(items, function(item) {
					var attr = store.getValue(item, "name");
					var value = e2.getAttributeValue(attr);
					if(value != null) {
						store.setValue(item, "value", value);
						store.setValue(item, "updated", true);
						e2.removeAttribute(attr);
					} else {
						store.setValue(item, "updated", false);
					}
				});
			}
		});
		
		dojo.forEach(e2.getNames(), function(attr) {
			var value = e2.getAttributeValue(attr);
			store.newItem({id:attr, name:attr, value:value, updated:true});
		});
	},
	
	getTreeTableLayout : function() {
	},
	
	populateTable : function() {
		var store = new dojo.data.ItemFileWriteStore({
			data: {
				identifier: "id",
				label: "name",
				items : []
			}
		});
		
		var map = this.attributeMapConfig;
		if (map != null) {
			 dojo.forEach(map.getNames(), dojo.hitch(this, function(m) {
				 var item = map.getItem(m);
				 var value = item.isSimple() ? item.getSimple() : item.getAdvanced();
				 store.newItem({id:item.getName(), name:item.getName(), mapping:value, value:""});
			 }));
		}
		
		if(this.config) {
			var schema = this.config.getSchema(this.input);
			dojo.forEach(schema.getNames(), dojo.hitch(this, function(name) {
				if(!map.isMapped(name)) {
					try {						
						store.newItem({id:name, name:name});
					} catch(err) {
						console.log("AttributeMap: " + name + "; " + err);
					}
				}
			}));
		}
		
		this.DG = new tdi.TreeTableWidget({
			store:store,
			getTreeTableLayout:function() {
				return [
		  			{field:"name", name:this.getString("attributeName"), width:"150px"},
		  			{field:"mapping", name:this.getString("assignment"), width:"150px"},
		  			{field:"value", name:this.getString("value"), width:"auto"}
		  		];
			},
			onRowDblClick:dojo.hitch(this, "_editAttribute"),
			toolbarOptions: {
				refreshIcon:false,
				refreshMenu:false
			},
			getToolbarVisible : function() {
				return true;
			},
			getTreeTableStyle: function() {
				return "padding:0px; margin:0px"
			}
		});
		this.DG.placeAt(this.DGDiv);
		this.DG.startup();
		
		var pos = 1;

		this.DG.addToToolbar(new dijit.form.Button({
			label:this.getString("newItem"),
			onClick:dojo.hitch(this, "_newAttribute")
		}), pos++, 1);
		
		if(this.config) {
			this.btnRead = new dijit.form.Button({
				label:this.getString("readNext"),
				onClick:dojo.hitch(this, "_readNext")
			});
			this.DG.addToToolbar(this.btnRead, pos++, 1);
			
			this.btnClose = new dijit.form.Button({
				label:this.getString("closeConnection"),
				onClick:dojo.hitch(this, "_closeConnection")
			});
			this.DG.addToToolbar(this.btnClose, pos++, 1);
		}
				
		this.DG.addToActionList(new dijit.MenuItem({
			label:this.getString("general.delete.label"),
			onClick:dojo.hitch(this, "_deleteAttribute")
		}));
		
		this.DG.addToActionList(new dijit.MenuItem({
			label:this.getString("WebCE.unmapAttribute"),
			onClick:dojo.hitch(this, "_mapSelectedAttributes", true)
		}));
		
		this.DG.addToActionList(new dijit.MenuItem({
			label:this.getString("action.label.3"),
			onClick:dojo.hitch(this, "_mapSelectedAttributes", false)
		}));
		
		this.DG.addToActionList(new dijit.MenuItem({
			label:this.getString("TaskCallParam.cmdEdit.name"),
			onClick:dojo.hitch(this, "_editAttribute", false)
		}));
		
		//
		// -- Style rows italic for attributes that are not mapped
		//
		dojo.connect(this.DG.grid, "onStyleRow", dojo.hitch(this, function(row) {
			var item = this.DG.grid.getItem(row.index);
			var mapping = this.DG.getStore().getValue(item, "mapping");
			var updated = this.DG.getStore().getValue(item, "updated");
			if(mapping == null)
				row.customStyles += ";font-style:italic";
			if(updated)
				row.customStyles += ";font-color:blue";
		}));
		
	},
	
	resize : function(obj) {
		if(this.BorderPane != null)
			this.BorderPane.resize(obj);
		
		if(obj.h > 0 && this.DG != null) {
			this.DG.resize(obj);
		}
	},
	
	enableReadNext : function(enable) {
		if(this.config) {
			this.btnRead.set("disabled", !enable);
			this.showProgressBar(!enable);
		}
	},
	
	enableClose : function(enable) {
		if(this.config) {
			this.btnClose.set("disabled", !enable);
		}
	},
	
	postCreate : function() {
		if(this.config) {
			this.attributeMapConfig = this.config.getAttributeMap(this.input);
		}
		this.populateTable();
		this.enableClose(false);
	}
});