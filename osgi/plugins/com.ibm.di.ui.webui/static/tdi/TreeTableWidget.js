define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/tree/ForestStoreModel",
	"dojo/data/ItemFileWriteStore",
	"idx/layout/HeaderPane",
	"tdi/NlsMixin",
	"tdi/tdiapi"
], function(declare, Widget, TemplatedMixin, WidgetsInTemplate, ForestStoreModel, ItemFileWriteStore, HeaderPane, tdiNlsMixin, tdiapi) {
return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, tdiNlsMixin],
	{
		templateString: "<div><div data-dojo-type='idx.layout.HeaderPane' data-dojo-attach-point='Main'></div></div>",
		
		// -- The layout structure (e.g. column names etc)
		treeTableLayout: [],
		
		// -- Visible toolbar or not
		toolbarVisible: true,
		
		// -- Toolbar options
		toolbarOptions: {},
		
		// -- Message to display when table has no data
		noDataMessage: null,
		
		// -- Data store attributes (identifier and label)
		storeIdAttribute: "id",
		storeLabelAttribute: "assemblyline",

		getToolbarVisible : function() {
			return this.toolbarVisible;
		},
		
		getTreeTableStyle : function() {
			// summary:
			//		Returns any custom styles to apply to the table
			//		upon creating it.
			return "padding:0px";
		},
		
		getTreeTableLayout : function() {
			// summary:
			//		Override by subclasses to provide layout
			if(this.treeTableLayout == null)
				throw "TreeTableWidget.getTreeTableLayout must be overridden or provided via treeTableLayout property";
			else
				return this.treeTableLayout;
		},
		
		getTreeTableSize : function() {
			return ({height:"98%", width:"100%"})
		},
		
		getNoDataMessage : function() {
			return this.noDataMessage;
		},
		
		removeAllItems : function() {
			var store = this.getStore()
			store.fetch({
				onComplete:function(items, request) {
					for(i = 0; i < items.length; i++) {
						store.deleteItem(items[i]);
					}
				}
			});
			store.save();
		},
		
		getGrid : function() {
			return this._table.grid;
		},
		
		getRowMenu : function() {
			// summary:
			//		Override to provide context menu
		},
		
		onRefresh : function() {
			// summary:
			//		Override to refresh model data
		},
		
		onSelectionChanged : function() {
			// summary:
			//		Override to handle selection changed events
		},
		
		onRowDblClick : function(item) {
			// summary:
			//		Override to handle double clicks
		},
		
		onRowClick : function(item, event) {
			// summary:
			//		Override to handle double clicks
		},
		
		getStore : function() {
			// summary:
			//		Returns the underlying store
			return this._store;
		},
		
		getModel : function() {
			// summary:
			//		Returns the underlying tree model
			return this._model;
		},
		
		setModel : function(newmodel) {
			this._model = newmodel;
			this.getGrid().setModel(newmodel);
		},
		
		getTreeTable : function() {
			// summary:
			//		Returns the TreeTable
			return this._table;
		},
		
		getItem : function(rowIndex) {
			if(this._table)
				return this._table.getItem(rowIndex);
			else
				return null;
		},
		
		getItemValue : function(itemOrRow, attribute) {
			var item = itemOrRow;
			if(typeof itemOrRow == "number")
				item = this.getItem(itemOrRow);
			return this.getStore().getValue(item, attribute);
		},
		
		setItemValue : function(itemOrRow, attribute, value) {
			var item = itemOrRow;
			if(typeof itemOrRow == "number")
				item = this.getItem(itemOrRow);
			this.getStore().setValue(item, attribute, value);
		},
		
		getSelectedItem : function() {
			return this.getGrid().selection.getFirstSelected();
		},
		
		getSelectedRows : function() {
			return this.getGrid().selection.getSelected();
		},
		
		getSelectionCount : function() {
			return this.getSelectedRows().length;
		},
		
		getSelectedAttributes : function() {
			var attrs = [];
			dojo.forEach(this.getGrid().selection.getSelected(), function(item) {
				if(item != null && item.name)
					attrs.push(item.name[0]);
			})
			return attrs;
		},
		
		getAllAttributes : function() {
			var attrs = [];
			this.getStore().fetch({
				query:{},
				onComplete: function(items, req) {
					dojo.forEach(items, function(item) {
						attrs.push(item.name[0]);
					})
				}
			})
			return attrs;
		},
		
		findItem: function(attribute, value) {
			var items = [];
			var query = {};
			query[attribute] = value;
			this.getStore().fetch({
				query:query,
				onComplete: function(list, req) {
					items = list;
				}
			})
			return items;
		},
		
		findSingleItem : function(attribute, value) {
			var items = this.findItem(attribute, value);
			if(items && items.length == 1)
				return items[0];
			else
				return null;
		},
		
		addItem : function(item) {
			return this.getStore().newItem(item);
		},
		
		addToActionList : function(item) {
			return this.Main.addChild(item);
//			return this._table.addToActionList(item);
		},
		
		addToToolbar: function(item) {
			return this.Main.addChild(item);
//			return this._table.addToToolbar(item);
		},
		
		removeItemFromToolbar : function(id) {
			this._table.removeItemFromToolbar(id);
		},
		
		getToolbarOptions : function() {
			var opts = {
				actionMenu:true,
				configTableIcon:false, configTableMenu: false,
				exporterIcon: false, exporterMenu: false,
				printerIcon:false, printerMenu: false,
				
				refreshIcon:true,
				refreshMenu:true,
				
				selectAllIcon:false,
				expandAllIcon: false,
				clearSortIcon: false,
				configureTreeTableIcon:false,
					
				selectAllMenu: false,
				expandAllMenu : false,
				clearSortMenu: false,
				configureTreeTableMenu:false
			};			
			if(this.toolbarOptions)
				return dojo.mixin(opts, this.toolbarOptions);
			else
				return opts;
		},
		
		createTreeTable : function() {
			// summary:
			//		Constructs the TreeTable
			
		    var menubars = {
		    		"rowMenu":this.getRowMenu()
		    };
		    
			var plugins = {
					indirectSelection: false,
					nestedSorting: true,
					menus:menubars,
					dnd:true,
					printer: false,
					exporter: false,			
					pagination: false
				};
			
			var gparams = {
				treeModel: this._model,
				structure: this.getTreeTableLayout(),
				rowsPerPage: 20,
				noDataMessage: this.getNoDataMessage(),
				plugins: plugins,
				autoWidth:false,
				filter: {
					showQuickFilter:false,
					showQuickFilterButton:false
				}
			};
			
			this._table = new dijit.Tree({
				model:this._model,
				showRoot:false
			});
			
			this.Main.set("content", this._table);
			
//			dojo.connect(this._table, "onRowClick", this, function(e) {
//				this.onRowClick(this._table.grid.getItem(e.rowIndex), e);
//			});
//			
//			dojo.connect(this._table.grid, "onRowDblClick", this, function(e) {
//				this.onRowDblClick(this._table.grid.getItem(e.rowIndex), e);
//			});
//			
//			dojo.connect(this._table.grid, "onSelectionChanged", this, "onSelectionChanged");
//
//			// -- Hook refresh so we can refresh from the server
//			dojo.connect(this._table, "refresh", dojo.hitch(this, "onRefresh"));
			
//			this._table.startup();			
		},
		
		createTreeModel : function() {
			if(this.store != null) {
				this._store = this.store;
			} else {
				this._store = new dojo.data.ItemFileWriteStore({
					data: {
						identifier: this.storeIdAttribute,
						label: this.storeLabelAttribute,
						items : []
					}
				});
			}
			
		    this._model = new dijit.tree.ForestStoreModel({
		        store: this._store,
		        rootId: "tsRoot",
		        rootLabel: "tsItems",
		        childrenAttrs: ["items"]
		    });
		},
		
		resize : function(obj) {
			if(obj != null && this._table != null) {
				var padding = dojo.style(this._table.domNode, "padding");
				if(padding !== undefined)
					obj.h -= (padding*2);
				var margin = dojo.style(this._table.domNode, "margin");
				if(margin !== undefined)
					obj.h -= (margin*2);
				this._table.resize(obj);
			}
		},
		
		postCreate : function() {
			this.createTreeModel();
			this.createTreeTable();
			this.onRefresh();
			this.inherited("postCreate", arguments);
		},
		
		destroy : function() {
			this._table.destroyRecursive(false);
			this.inherited("destroy", arguments);
		}
	})
});