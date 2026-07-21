dojo.provide("tdi.TreeTableWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.tree.ForestStoreModel");
dojo.require("dojo.data.ItemFileWriteStore");

dojo.require("dojoe.treetable.TreeTableAll");
dojo.require("dojoe.table.plugins.DnD");
dojo.require("dojoe.table.plugins.Menu");
dojo.require("dojoe.table.plugins.NestedSorting");
dojo.require("dojoe.table.plugins.IndirectSelection");
dojo.require("dojoe.table.plugins.Pagination");
dojo.require("dojoe.table.plugins.GridFilter");
dojo.require("dojoe.table.plugins.exporter.CSVWriter");
dojo.require("dojoe.table.plugins.Printer");
dojo.require("dojoe.table.plugins.Selector");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiapi");

dojo.declare("tdi.TreeTableWidget",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
	// summary:
	//		The treetable widget is a basic widget to manage a TED treetable
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templateString: "<div dojoAttachPoint='Main'></div>",
		
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
					store.save();
				}
			});
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
			return this._table.addToActionList(item);
		},
		
		addToToolbar: function(item) {
			return this._table.addToToolbar(item);
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
			
			this._table = new dojoe.treetable.TreeTable({
				gridParams:gparams,
				toolbarOptions: this.getToolbarOptions(),
				enableResize: true,
				filter: false,
				footerVisible:false,
				width:this.getTreeTableSize().width,
				height:this.getTreeTableSize().height,
				toolbarVisible:this.getToolbarVisible(),
				style:this.getTreeTableStyle()
			}).placeAt(this.Main);
			
			dojo.connect(this._table, "onRowClick", this, function(e) {
				this.onRowClick(this._table.grid.getItem(e.rowIndex), e);
			});
			
			dojo.connect(this._table.grid, "onRowDblClick", this, function(e) {
				this.onRowDblClick(this._table.grid.getItem(e.rowIndex), e);
			});
			
			dojo.connect(this._table.grid, "onSelectionChanged", this, "onSelectionChanged");

			// -- Hook refresh so we can refresh from the server
			dojo.connect(this._table, "refresh", dojo.hitch(this, "onRefresh"));
			
			this._table.startup();			
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
	
});