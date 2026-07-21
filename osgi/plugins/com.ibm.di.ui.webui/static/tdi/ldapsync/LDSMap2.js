/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/topic",
	"dojo/data/ItemFileWriteStore",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/widget/Dialog",
	"idx/dialogs",
	"./LDSMapItemEditor",
	"./LDSUtil",
	"./LDSSelectAttribute",
	"gridx/Grid",
    "gridx/modules/pagination/Pagination", 
    "gridx/modules/pagination/PaginationBar", 
    "gridx/modules/filter/Filter", 
    "gridx/modules/filter/FilterBar", 
    "gridx/modules/Bar", 
    "gridx/modules/CellWidget", 
    "gridx/modules/ColumnResizer", 
    "gridx/modules/Edit", 
    "gridx/modules/Focus",
    "gridx/modules/SingleSort",
    "gridx/modules/ToolBar",
    "gridx/modules/select/Row",
    "idx/gridx/modules/filter/QuickFilter",
    "gridx/modules/IndirectSelect",
	"gridx/modules/extendedSelect/Row",
	"gridx/modules/RowHeader",
    "gridx/core/model/cache/Async",
    "dojo/store/Memory",
	"tdi/ToolbarLabel",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSMap2.html"
], function(declare, array, lang, html, topic, ItemFileWriteStore, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, Dialog, idx, LDSMapItemEditor, LDSUtil,
		LDSSelectAttribute, Grid, Pagination, PaginationBar, Filter, FilterBar, Bar, CellWidget, ColumnResizer, Edit, Focus, SingleSort, ToolBar, SelectRow, QuickFilter, 
		IndirectSelect, RowSelect, RowHeader, Async, Memory, ToolbarLabel, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		// sourceObject: String
		//		The object to use as source for maps
		sourceObject: "conn",
		
		hidefilebuttons: false,
		showSaveButton: false,
		
		createItems: function() {
			var t = this;
			var f = null;
			
			t.mapStore = new Memory({
				idProperty:"id",
				data: []
			});
			
			var arr = [];
			for(f in t.config.map) {
				arr.push(f);
			}
			
			var items = [];
			array.forEach(arr.sort(), function(f) {
				var item = t.config.map[f];
				t.updateItemAssign(item);
				t.mapStore.add(item);
			});
			t.mapGrid.setStore(t.mapStore);
		},
		
		updateItemAssign: function(item) {
			// summary:
			//		Updates the "assign" property of an item
			if(item.subst) {
				item.assign = item.subst;
			}
			if(item.script) {
				var simple = this.getSimpleAssignment(item);
				if(simple == item.name) {
					item.simple = simple;
					delete item.script;
				}
				item.assign = simple;
			}
			if(item.simple) {
				item.assign = item.simple;
			}
			if(item.enabled == false)
				item.assign = "<i>" + item.assign + "</i>";
		},
		
		matchSimple: function(value) {
			if(this.get("sourceObject") == "work") {
				return value.match(/^work\["(.*)"\];?$/);
			} else {
				return value.match(/^conn\["(.*)"\];?$/);				
			}
		},
		
		getSimpleAssignment: function(item) {
			if(item.script) {
				var arr = this.matchSimple(item.script);
				if(arr && arr.length == 2) {
					return arr[1];
				}
				return item.script;
			} else {
				return item.name;
			}
		},	
		
		createToolbar: function() {
			var labels = ["AttributeMap.toolbar.Add.name", "AttributeMap.toolbar.Remove.name", "general.save.label", "FDS.duplicate", "FDS.deleteMap"];
			var cb = ["onAddAttribute", "onRemoveAttribute", "onSaveMap", "_onDuplicateMap", "_onDeleteMap"];
			var prop = ["_addAttribute", "_removeAttribute", "_saveMap", "_duplicateMap", "_deleteMap"];
			for(var i = 0; i < labels.length; i++) {
				var button = new Button({
					label:this.getString(labels[i]),
					onClick:lang.hitch(this, cb[i])
				});
				this.addToolbarItem(button);
				this.set(prop[i], button);
			}
			this.updateToolbar();
			
			this._saveStatus = new ToolbarLabel({label:""});
			this.mapGrid.toolBar.widget.addChild(this._saveStatus);
		},
		
		updateToolbar: function() {
			var t = this;
			if(t.hidefilebuttons) {
				html.style(t._saveMap.domNode, "display", "none");
				html.style(t._duplicateMap.domNode, "display", "none");
				html.style(t._deleteMap.domNode, "display", "none");
			}
			if(t.showSaveButton) {
				html.style(t._saveMap.domNode, "display", "");
			}
			t._removeAttribute.set("disabled", true);
		},
		
		createGrid: function() {
			if(this.mapGrid) {
				return;
			}
			
			this.mapStore = new Memory({
				idProperty:"id",
				data: []
			});
			
			var label1 = "";
			var label2 = "";
			if(this.isWriteBack()) {
				label1 = this.getString("FDS.endpointAttribute"); 
				label2 = this.getString("FDS.directoryAttribute") + " / " + this.getString("WebCE.assignment"); 
			} else {
				label1 = this.getString("FDS.directoryAttribute"); 
				label2 = this.getString("FDS.endpointAttribute") + " / " + this.getString("WebCE.assignment"); 
			}
			
			this.mapGrid = new Grid({
				cacheClass: Async,
				store:this.mapStore,
				'class': 'gridxAlternatingRows',
				structure: [
				  {
					  id:"name",
					  field:"name",
					  name:label1,
					  width:"20%",
					  editable:true,
					  editor: "dijit/form/TextBox",
					  editorArgs:{
						  fromEditor:lang.hitch(this, function(value, p) {
							  var item = this.mapStore.get(p.row.id);
							  if(item) {
								  if(item.simple && item.simple == item.name) {
									  item.simple = value;
									  item.assign = value;
								  }
								  item.name = value;
								  this.mapStore.put(item);
								  this.onModify();
							  }
							  return value;
						  })
					  }
				  },
				  {
					  id:"assign",
					  field:"assign", 
					  name:label2,
					  width:"auto",
					  editable: true, 
					  editor:"tdi/ldapsync/LDSMapItemEditor",
					  editorArgs:{
						  toEditor:lang.hitch(this, "toEditor"),
						  fromEditor:lang.hitch(this, "fromEditor")
					  }
				  }
				],
				modules: [
				   RowSelect, RowHeader, IndirectSelect, SingleSort, ToolBar, Focus, Edit, CellWidget, ColumnResizer, Bar
				],
				style:"width:100%; height:100%"
			}).placeAt(this.domNode);
			
			this.connect(this.mapGrid.select.row, 'onSelectionChange', function(sel) {
				this._removeAttribute.set("disabled", !(sel && sel.length > 0));
				this.selection = sel;
			});
			
			this.createToolbar();
		},
		
		toEditor: function(storeData, gridData, cell, editor) {
			// summary:
			//		Called by Gridx to obtain the value it passes to the editor
			//		Instead of the synthetic "assignment" attribute we pass the entire item
			//		object.
			editor.setParentMap(this);
			return this.mapStore.get(cell.row.id);
		},
		
		fromEditor: function(item) {
			// summary:
			//		Rewrites the item object back to the store
			// 		and returns a formatted string suitable for assignment
			
			try {
				this.mapStore.put(item);
			} catch(err) {
				tdiutil.error(err);
			}
			
			this.onModify();
			if(item.enabled == false)
				return "<i>" + item.assign + "</i>";
			else
				return item.assign;
		},
		
		addItem: function(item, loc) {
			
		},
		
		onAddAttribute: function() {
			var t = this;
			var sourceObj = this.get("sourceObject");
			var sourceArr = [];
			
			// -- get list of directory attributes or endpoint attrs
			if(t.targetMap && !t.isWriteBack()) {
				for(var f in t.targetMap.map) {
					var obj = t.targetMap.map[f];
					if(obj && obj.enabled) {
						sourceArr.push(f);
					}
				}
				
			} else if(this.get("targetAttributes")) {
				sourceArr = this.get("targetAttributes");
				
			} else if(t.flowconfig || t.projectconfig) {
				var top = t.flowconfig ? t.flowconfig.getTop() : t.projectconfig;
				var tds = top.getConnector(LDSUtil.generalSettingsConn);
				if(tds) {
					sourceArr = tds.getSchema().getNames();
				}
			}

			var selattr = new LDSSelectAttribute({config:sourceArr});
			var dlg = new Dialog({
				content:selattr,
				title:t.getString("AttributeMap.toolbar.Add.name"),
				closeButtonLabel:this.getString("WebCE.cancel"),
				onExecute: function() {
					if(selattr.validate()) {
						var str = dlg.content.get("value");
						if(str && str != "") {
							var item = {name:str, script:t.get("sourceObject") + '["'+str+'"]', add:true, mod:true};
							t.updateItemAssign(item);
							t.mapStore.add(item);
							t.onModify();
						}
						dlg.hide();
						return true;
					} else {
						return false;
					}
				},
				isValid: function() {
					return selattr.validate();
				},
				buttons: [new Button({label:t.getString("WebCE.ok"), onClick:function() {
					if(dlg.isValid()) {
						dlg.onExecute();
					} else {
						dlg.validate();
					}
				}})]
			});
			dlg.show();
		},
		
		onRemoveAttribute: function() {
			var t = this;
			var arr = [];
			array.forEach(t.selection, function(id) {
				arr.push(t.mapStore.get(id).name);
			});
			idx.confirm(this.getString("SimpleListUI.prompt.Delete"), function() {
				array.forEach(t.selection, function(id) {
					t.mapStore.remove(id);
				});
				t._removeAttribute.set("disabled", true);
				t.onModify();
			});
		},
		
		onModify: function() {
			var t = this;
			if(t._saveMap)
				t._saveMap.set("disabled", false);
			
			if(!LDSUtil.getOption("autoSave", true)) {
				if(t.projectconfig)
					t.projectconfig.setModified(true);
				
				else if(t.flowconfig)
					t.flowconfig.setModified(true);
				t.savePending = true;
			}
			
			// t._saveStatus.setLabel("<span style='color:red'>*****</span>");
			if(t.customMapName) {
				if(!t._saveHandle) {
					t._saveHandle = setTimeout(lang.hitch(this, "autoSave"), 1000);
				}
			} else if (LDSUtil.getOption("autoSave")) {
				t.onSaveMap();
			}
		},
		
		_onDeleteMap: function() {
			this.onDeleteMap(this);
		},
		
		onDeleteMap: function() {
			// summary:
			//		callback
		},
		
		_onDuplicateMap: function() {
			this.onDuplicateMap(this);
		},
		
		onDuplicateMap: function() {
			// summary:
			//		callback
		},
		
		onSaveMap: function() {
			var t = this;
			
			t.config.map = {};
			if(!t.config.name)
				t.config.name = t.getAutoSaveTo();
			
			array.forEach(this.mapStore.query({}), function(item) {
				t.config.map[item.name] = item;
			});
			
			dojo.when(LDSUtil.saveServerMap(this.config), function() {
				t._saveMap.set("disabled", true);
				t.config.isnew=false;
				topic.publish("ldapsync/maps", "save");
				t._saveStatus.setLabel("");
				t.savePending = false;
//				idx.info(this.getString("FDS.mapSaved"));
			},
			function(err) {
				tdiutil.error(err);
			});
		},
		
		isModified: function() {
			return !this._saveMap.get("disabled");
		},
		
		useMap: function(config) {
			var t = this;
			t.config = config;
			t.createGrid();
			t.createItems();
			if(!t.config.isnew) {
				setTimeout(function() {
					t._saveMap.set("disabled", true)
				}, 1000);
			}
		},
		
		setAutoSaveTo: function(customMapName) {
			this.customMapName = customMapName;
		},
		
		getAutoSaveTo: function() {
			return this.customMapName;
		},
		
		autoSave: function() {
			var t = this;
			t.config = tdiutil.clone(t.config);
			t.config.name = t.customMapName;
			t.config.label = t.customMapName;
			t.config.value = t.customMapName;
			
			t.config.map = {};
			array.forEach(t.mapStore.query({}), function(item) {
				t.config.map[item.name] = item;
			});
			
			if(LDSUtil.getOption("autoSave")) {
				dojo.when(LDSUtil.saveServerMap(t.config), function() {
					t._saveMap.set("disabled", true);
					t.config.isnew=false;
					delete t._saveHandle;
					var now = new Date();
					t._saveStatus.setLabel(
						t.getString("FDS.autoSaved",  [
						  now.getHours() + ":" + (now.getMinutes() > 9 ? now.getMinutes() : "0" + now.getMinutes()) + ":" + 
							(now.getSeconds() > 9 ? now.getSeconds() : "0" + now.getSeconds())
					    ]
					));
					t._saveStatus.set("title", t.customMapName);
					t.onAutoSaved(t.config);
				},
				function(err) {
					tdiutil.error(err);
					t._saveStatus.setLabel("");
					delete t._saveHandle;
				});
			} else {
				t.savePending = true;
			}
		},
		
		onAutoSaved: function() {
			// summary:
			//		Callback after auto save
		},
		
		addToolbarItem: function(child, position) {
			var t = this;
			if(t.mapGrid && t.mapGrid.toolBar) {
				if(this._toolbarItems) {
					array.forEach(t._toolbarItems, function(child) {
						t.mapGrid.toolBar.widget.addChild(child);
					});
				}
				t.mapGrid.toolBar.widget.addChild(child);
			} else {
				t._toolbarItems = t._toolbarItems || [];
				t._toolbarItems.push(child);
			}
		},
		
		resize: function(obj) {
			if(this.mapGrid)
				this.mapGrid.resize(obj);
		},
		
		postCreate: function() {
			this.inherited(arguments);
		},
		
		setFlowContext: function(flowconfig, type) {
			// summary:
			//		Provide the Flow config so the mapping
			//		can provide help when selecting attributes etc.
			this.flowconfig = flowconfig;
			this.type = type;
			this.set("sourceObject", type == "writeback" ? "work" : "conn");
			this.set("objectType", type == "writeback" ? "person" : type);
			if(this.flowconfig) {
				this.reloadTargetMap();
			}
		},
		
		reloadTargetMap: function() {
			var t = this;
			t.targetMap = null;
			if(t.isJoin() && t.flowconfig) {
				// target attributes are determined by the Flow's target attribute map
				var conn = t.flowconfig.getConnector("Output");
				
				// Current map file/config
				var mapConfig = conn.getConnectionConfig().getParam("target.person.mapFile");
				if(mapConfig) {
					LDSUtil.getServerMap(mapConfig).then(function(data) {
						t.targetMap = data;
					});
				}
			} else if(t.isWriteBack() && t.flowconfig) {
				// target attributes are determined by the Flow's source connector schema
				var conn = t.flowconfig.getConnector("Input").getInheritFrom();
				t.updateWriteBackSourceAttributes(conn);
			}

		},
		
		updateWriteBackSourceAttributes: function(source) {
			var t = this;
			var m = source.match(/^\/Connectors\/(\w+)/);
			if(m) {
				source = m[1];
			}
			var conn = t.flowconfig.getTop().getConnector(source);
			var arr = [];
			if(conn) {
				array.forEach(conn.getSchema().getNames(), function(key) {
					arr.push(key);
				});
			}
			this.set("sourceAttributes", arr.sort());
		},
		
		isWriteBack: function() {
			return this.type == "writeback";
		},
		
		isJoin: function() {
			return this.type == "join";
		},
		
		setConfig: function(config) {
			var t = this;
			if(config) {
				t.useMap(config);
			}
			topic.subscribe("ldapsync/configsave", function(config) {
				if(!LDSUtil.getOption("autoSave") && t.savePending) {
					t.onSaveMap();
				}
				if(t.isJoin()) {
					t.reloadTargetMap();
				}
			});
		}
	});
});
		
