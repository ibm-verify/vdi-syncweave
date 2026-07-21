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
	"tdi/AttributeMapItemEditor3",
	"tdi/ldapsync/LDSUtil",
	"tdi/ldapsync/LDSSelectAttribute",
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
	"tdi/model/AttributeMapStore",
	"tdi/ToolbarLabel",
	"tdi/tdientry",
	"tdi/tdiutil",
	"tdi/tdisession",
	"tdi/NlsMixin",
	"dojo/text!./templates/AttributeMap3.html"
], function(declare, array, lang, html, topic, ItemFileWriteStore, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, Dialog, idx, AttributeMapItemEditor3, LDSUtil,
		LDSSelectAttribute, Grid, Pagination, PaginationBar, Filter, FilterBar, Bar, CellWidget, ColumnResizer, Edit, Focus, SingleSort, ToolBar, SelectRow, QuickFilter, 
		IndirectSelect, RowSelect, RowHeader, Async, Memory, AttributeMapStore, ToolbarLabel, tdientry, tdiutil, tdisession, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		// sourceObject: String
		//		The object to use as source for maps
		sourceObject: "conn",
		
		// sourceAttributes: Array
		//		An array of attributes available for mapping
		sourceAttributes: [],
		
		// toolbar: boolean
		// 		If true a dijit toolbar is used for buttons
		toolbar: true,
		
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
		
		onConnect: function() {
			this.onConnectNext();
		},
		
		onConnectNext: function() {
			this._connectNext.set("disabled", true);
			this._connectClose.set("disabled", true);
			
			if(this.session == null) {
				this.session = new tdisession();
				dojo.when(this.session.openSessionForConnector(this.config.getParent()), lang.hitch(this, function(data) {
					this._connect.set("disabled", true);
					this._connectClose.set("disabled", false);
					this._columns = this.mapGrid.structure;
					this.mapGrid.setColumns([
						  {
							  id:"name", field:"name", name:"Attribute", width:"20%", editable:false, editor: "dijit/form/ComboBox"
						  },
						  {
							  id:"value",
							  field:"valueTo", 
							  name:"Value",
							  width:"auto"
						  }
						]
					);
					this.onConnectNext();
				}), dojo.hitch(this, function(err) {
					this.onConnectClose(null);
					tdiutil.error(err);
				}));
			} else {
				dojo.when(this.session.getNextEntry(), lang.hitch(this, function(entry) {
					if(entry == null) {
						this.onConnectClose(null);
						this._connectNext.set("disabled", false);
					} else {
						this._connectNext.set("disabled", false);
						this._connectClose.set("disabled", false);
						this.mapGrid.setStore(this.entry2store(entry));
					}
				}), dojo.hitch(this, function(err) {
					this._connectNext.set("disabled", true);
					this._connectClose.set("disabled", true);
					this.onConnectClose(null);
					tdiutil.error(err);
				}));
			}
		},
		
		onConnectClose: function(map) {
			if(this.session != null) {
				this.session.close();
				this._connect.set("disabled", false);
				this._connectNext.set("disabled", true);
				this._connectClose.set("disabled", true);
			}
			if(this._columns)
				this.mapGrid.setColumns(this._columns);
			if(this.mapStore)
				this.mapGrid.setStore(this.mapStore);
		},
		
		entry2store: function(entry) {
			var items = [];
			array.forEach(entry.attribute, function(a) {
				items.push({
					name:a.name,
					value:a.children ? a.children[0].value.value : ""
				});
			});
			var store = new Memory({
				idProperty:"name",
				data:items
			});
			return store;
		},
		
		createToolbar: function() {
			var labels = ["AttributeMap.toolbar.Add.name", "AttributeMap.toolbar.Remove.name"];
			var cb = ["onAddAttribute", "onRemoveAttribute"];
			var prop = ["_addAttribute", "_removeAttribute"];
			
			if(this.connectButtons) {
				labels = labels.concat(["DiscoverSchemaWidget.connect", "DiscoverSchemaWidget.next", "DiscoverSchemaWidget.close"]);
				cb = cb.concat(["onConnect", "onConnectNext", "onConnectClose"]);
				prop = prop.concat(["_connect", "_connectNext", "_connectClose"]);
			}
			
			// If toolbar is true we use Grid's toolbar implementation
			this._toolbar.style.display = this.toolbar ? "none" : "";
			
			for(var i = 0; i < labels.length; i++) {
				var button = new Button({
					label:this.getString(labels[i]),
					onClick:lang.hitch(this, cb[i])
				});
				if(this.toolbar)
					this.addToolbarItem(button);
				else 
					button.placeAt(this._toolbar);
				this.set(prop[i], button);
			}
			
			this._saveStatus = new ToolbarLabel({label:""});
			if(this.toolbar)
				this.mapGrid.toolBar.widget.addChild(this._saveStatus);
			else
				this._saveStatus.placeAt(this._toolbar);

			if(this._connect) {
				this._connectNext.set("disabled", true);
				this._connectClose.set("disabled", true);
			}
			
			this._removeAttribute.set("disabled", true);
		},
		
		createGrid: function() {
			if(this.mapGrid) {
				return;
			}
			
			this.mapStore = new AttributeMapStore({
				config:this.config
			});
			
			var label1 = "Attribute";
			var label2 = "Assignment";

			this.mapGrid = new Grid({
				cacheClass: Async,
				store:this.mapStore,
				'class': 'gridxAlternatingRows',
				structure: [
				  {
					  id:"name", field:"name", name:label1, width:"20%", editable:false, editor: "dijit/form/ComboBox"
				  },
				  {
					  id:"mapsTo",
					  field:"mapsTo", 
					  name:label2,
					  width:"auto",
					  decorator: lang.hitch(this, "decorateMapsTo")
				  }
				],
				modules: [
			  	    {
			  	    	moduleClass:SelectRow,
			  	    	triggerOnCell:true
			  	    },
			  	    SingleSort, ToolBar, Focus, Edit, CellWidget, ColumnResizer, Bar
				],
				style:"margin:0; padding:0"
			});
			
			this._grid.set("content", this.mapGrid);
			
			this.connect(this.mapGrid.select.row, 'onSelectionChange', function(sel) {
				this._removeAttribute.set("disabled", !(sel && sel.length > 0));
				this.selection = sel;
			});
			this.connect(this.mapGrid, 'onRowDblClick', function(evt) {
				if(evt.rowId) {
					var item = this.mapStore.get(evt.rowId);
					this.editAttribute(item);
				}
			});

			this.createToolbar();
		},

		decorateMapsTo: function(data, id, index) {
			var item = this.config.getItem(id);
			var str = data;
			if(item && item.isSimple()) {
				str = item.getSimple();
			} else {
				str = item.getScript();
			}
			if(!item.isEnabled()) {
				str = "<i>" + str + "</i>";
			}
			return str;
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
		
		addItem: function(item) {
			this.mapStore.put(item);
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
							var item = {name:str, mapsTo:t.get("sourceObject") + '["'+str+'"]', add:true, mod:true};
							t.updateItemAssign(item);
							t.addItem(item);
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
		
		_setConfigAttr: function(config) {
			this.config = config;
			this.createGrid();
		},
		
		isInput: function() {
			return !this.isOutput();
		},
		
		isOutput: function() {
			return this.config && this.config.getName() == "Output";
		},
		
		editAttribute: function(item) {
			var t = this;
			var editor = new AttributeMapItemEditor3({});
			editor.setParentMap(this);
			editor.set("value", item);
			var dlg = new Dialog({
				title:item.name,
				closeButtonLabel:this.getString("WebCE.cancel"),
				content:editor,
				buttons: [new Button({label:t.getString("WebCE.ok"), onClick:function() {
					t.fromEditor(editor.get("value"));
					dlg.onExecute();
				}})]
			});
			dlg.show();
		},
		
		resize: function(obj) {
			if(this.mapGrid) {
				var box = dojo.getMarginSize(this.domNode);
				this.mapGrid.resize(box);
			}
		},
		
		postCreate: function() {
			this.inherited(arguments);
		}
		
	});
});
		
