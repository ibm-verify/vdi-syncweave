/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/DropDownMenu",
	"dijit/Menu",
	"dijit/form/DropDownButton",
	"dijit/form/TextBox",
	"gridx/Grid",
	"gridx/core/model/cache/Async",
	"dojo/store/Memory",
	"dojo/data/ItemFileWriteStore",
	"gridx/modules/select/Row",
	"gridx/modules/dnd/Row",
	"gridx/modules/move/Row",
	"gridx/modules/SingleSort",
	"gridx/modules/ColumnResizer",
	"gridx/modules/ToolBar",
	"gridx/modules/Menu",
	"gridx/modules/Dod",
	"gridx/modules/Tree",
	"gridx/modules/Edit",
	"gridx/modules/Focus",
	"gridx/modules/CellWidget",
	"gridx/modules/VirtualVScroller"
], function(declare, array, lang, Widget, TemplatedWidget, DropDownMenu, Menu, DropdownButton, TextBox, Grid, Cache, Memory, WriteStore, SelectRow, DndRow, MoveRow,
		SingleSort, ColumnResizer, GToolbar, GMenu, GDod, GTree, GEdit, GFocus, GCellWidget, GVirtualVScroller) {
	
	return declare(
		[Widget, TemplatedWidget],
		{
			templateString: "<div data-dojo-attach-point='Main' style='height:100%; width:100%; margin:0px; padding:0px'></div>",
			// idProperty: string
			//		The property in the store to use as identifier (default 'id')
			idProperty: "id",
			
			// detailsOnDemand: boolean
			//		If true a detailsOnDemand button is added to the table (default false)
			detailsOnDemand: false,
			
			// toolbar: boolean
			//		If true include the toolbar module
			toolbar: false,
			
			// tree: boolean
			//		If true include the Tree module and add get/hasChildren to the store
			tree: false,
			
			// cellWidget: boolean
			//		If true include the CellWidget module
			cellWidget: false,
			
			// childProperty: string
			//		The property name of the item that holds child items (default "children")
			childProperty: "children",
			
			constructor: function(args) {
				declare.safeMixin(this, args);
				this.structure = this.structure || [
		  			{field:"name", id:"name", name:"Name", width:"auto"}
		  		];
				this.modules = this.modules || [
			  	    {
			  	    	moduleClass:SelectRow,
			  	    	triggerOnCell:true
			  	    },
			  	    DndRow,
			  	    MoveRow,
			  	    SingleSort,
			  	    ColumnResizer,
			  	    GMenu
//			  	    GFocus
				];
				if(this.detailsOnDemand) {
					this.modules.push(
				  	    {
				  	    	moduleClass: GDod,
				  	    	detailProvider:lang.hitch(this, "detailProvider"),
				  	    	duration:250
				  	    }
				  	);
				}
				if(this.toolbar) {
					this.modules.push(GToolbar);
				}
				if(this.tree) {
					this.modules.push(GTree);
					this.modules.push(GVirtualVScroller);
				}
				if(this.cellWidget) {
					this.modules.push(GCellWidget);
				}
				
				if(array.some(this.structure, function(field) {
					return field.editable && field.editor;
				})) {
					this.modules.push(GEdit);
					if(!this.cellWidget) {
						this.modules.push(GCellWidget);
					}
				}
				
				this.rowMenu = new Menu({});
				this.rowMenu.startup();
			},
			
			onSelected: function(row) {
				// description:
				//		Called when selection changes in the grid
				// row: Object
				//		Row object selected
			},
			
			onDeselected: function(row) {
				// description:
				//		Called when selection changes in the grid
				// row: Object
				//		Row object deselected
			},
			
			onHighlightChange: function() {
				// description:
				//		Called when highlight changes in the grid
			},
			
			onRowClick : function(item) {
				// description:
				//		Called when a row is clicked
				// item: Object
				//		Item clicked
			},
			
			onRowDblClick : function(item) {
				// description:
				//		Called when a row is double clicked
				// item: Object
				//		Item clicked
			},
			
			
			detailProvider: function(grid, rowId, detailNode, rendered) {
				detailNode.innerHTML = "<p>Sample detail provider content for rowId=" + rowId + "</p";
				rendered.callback();
				return rendered;
			},
			
			addToToolbar: function(wid) {
				// description:
				//		Adds a button to the toolbar
				this.grid.toolBar.widget.addChild(wid);
			},
			
			addToActionList: function(wid) {
				// description:
				//		Adds an item to the drop-down action button
				if(!this.subMenu) {
					this.subMenu = new DropDownMenu({});
					this.addToToolbar(new DropdownButton({
						label:"Actions",
						dropDown:this.subMenu
					}));
				}
				this.subMenu.addChild(wid);
			},

			getMenuItems: function() {
				// description:
				//		Returns the children of the row menu
				return this.rowMenu.getChildren();
			},
			
			addToRowMenu: function(wid) {
				// description:
				//		Adds an item to the row menu
				this.rowMenu.addChild(wid);
			},
			
			setData : function(data) {
				// description:
				//		Creates a new store based on the items in data
				// data: array
				//		Array of items for the new store
				var store = this.newStore(data);
				this.setStore(store);
			},
			
			getStore : function() {
				// description:
				//		Returns the ItemFileWriteStore used by the grid
				return this.grid.store;
			},
			
			newStore : function(data) {
				var store = new WriteStore({
					data: {
						identifier:this.idProperty,
						label:this.idProperty,
						items:data || []
					}
				});
				store.put = function(item) {
					return store.newItem(item);
				};
				store.get = function(id) {
					var obj = null;
					store.fetch({
						onItem: function(data) {
							if(data.id == id)
								obj = data;
						}
					});
					return obj;
				};

				if(this.tree) {
					var childProperty = this.childProperty;
					store.hasChildren = function(id, item) {
						var cp = item[childProperty];
						return cp && cp.length > 0;
					};
					store.getChildren = function(item) {
						return item[childProperty];
					};
				}
				return store;
			},
			
			setStore : function(store) {
				if(!this.grid)
					this.grid = this.newGrid(store);
				else
					this.grid.setStore(store);
			},
			
			reloadGrid: function() {
				if(this.grid) {
					this.grid.setStore(this.grid.store);
				}
			},
			
			newGrid: function(store) {
				this.grid = new Grid({
					autoSize:true,
					autoUpdate:true,
					cacheClass: Cache,
					store: store,
					structure: this.structure,
					modules: this.modules,
					columnWidthAutoResize:true,
					sortInitialOrder: this.sortInitialOrder,
					dodDetailProvider:lang.hitch(this, "detailProvider")
				}).placeAt(this.Main);
				this.grid.startup();
				this.grid.menu.bind(this.rowMenu, "row");
				this._supportingWidgets.push(this.grid);
				this.connect(this.grid, "onRowClick", "onRowClick");
				this.connect(this.grid, "onRowDblClick", "onRowDblClick");
				this.connect(this.grid.select.row, "onSelected", "onSelected");
				this.connect(this.grid.select.row, "onDeselected", "onDeselected");
				this.connect(this.grid.select.row, "onHighlightChange", "onHighlightChange");
				return this.grid;
			},
			
			createGrid : function(data) {
				var store = this.newStore();
				array.forEach(data, function(item) {
					store.put(item);
				});
				if(!this.grid) {
					this.newGrid(store);
				} else {
					this.grid.setStore(store);
				}
			},
			
			resize : function(obj) {
				if(this.grid && obj) {
					this.grid.resize(obj);
				}
			},
			
			setConfig : function(alconfig) {
				this.alconfig = alconfig;
				this.createGrid(alconfig.getWorkAttributeMaps());
			},
			
			getSelectedRows: function() {
				return this.grid.select.row.getSelected();
			},
			
			getSelectedItem: function() {
				var sel = this.getSelectedRows();
				if(sel && sel.length == 1) {
					return this.getItem(sel[0]);
				} else {
					return null;
				}
			},
			
			getItem: function(id) {
				var result = null;
				var idprop = this.idProperty;
				this.getStore().fetch({
					onItem:function(item) {
						if(item[idprop][0] == id)
							result = item;
					}
				});
				return result;
			},
			
			getItemValue: function(item, attr) {
				if(lang.isString(item)) {
					item = this.getItem(item);
				}
				if(!item)
					item = this.getSelectedItem();
				if(item) {
					return this.getStore().getValue(item, attr);
				} else {
					return null;
				}
			},
			
			setItemValue: function(item, attr, value) {
				if(lang.isString(item)) {
					item = this.getItem(item);
				}
				if(item) {
					this.getStore().setValue(item, attr, value);
				}
			},
			
			deleteItem: function(id) {
				var obj = id || this.getSelectedItem();
				if(typeof(id) == "string") {
					obj = this.getItem(id);
				}
				var item = this.getStore().deleteItem(obj);
				this.getStore().save();
				return item;
			},
			
			addItem: function(obj) {
				return this.getStore().newItem(obj);
			},
			
			selectById: function(id, clearSelection) {
				if(clearSelection)
					this.clearSelection();
				this.grid.select.row.selectById(id);
			},
			
			clearSelection: function() {
				this.grid.select.row.clear();
			},
			
			sort: function(column, ascending) {
				this.grid.column(column).sort(!ascending);
			},
			
			postCreate: function() {
				if(this.data) {
					this.setData(this.data);
				}
			}
			
		}
	);
	
});
