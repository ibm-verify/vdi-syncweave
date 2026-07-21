define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
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
    "tdi/NlsMixin"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button,
		Grid, CellWidget, ColumnResizer, Edit, Focus, SingleSort, ToolBar, SelectRow, 
		IndirectSelect, RowSelect, RowHeader, Filter, QuickFilter, Async, Memory, tdiNlsMixin) {

return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiNlsMixin ],
	{
		templateString : "<div style='width:!00%; height:100%'></div>",
		
		// viewAttributes: boolean
		//		If true, the grid shows each property in each item in data array
		//		If data is an object viewAttributes is implicit.
		viewAttributes: true,
		
		// data: Array
		//		The initial data array - can be a single object as well
		data: [],
		
		// idProperty: String
		//		The identifier property 
		idProperty: "%%__  ignore this __%%",
		
		setData: function() {
			// summary:
			//		Set the content of the grid
			this.data = data;
			this._createGrid();
		},
		
		_createStore: function() {
			this.mapStore = new Memory({
				idProperty:this._idProperty,
				data:[]
			});
		},

		_createGrid: function() {
			this._createStore();
			var struc = [];
			var arr = this.data;
			if(arr) {
				if(!lang.isArray(arr) || this.viewAttributes) {
					struc.push({
						id:"attr", field:"attr", name:this.getString("WebCE.attributeName"), width:"25%"
					});
					struc.push({
						id:"value", field:"value", name:this.getString("WebCE.value"), width:"auto"
					});
				} else if(lang.isArray(arr)){
					var names = {};
					array.forEach(arr, function(item) {
						if(item) {
							for(f in item) {
								names[f] = "";
							}
						}
					});
					for(f in names) {
						if(f != this._idProperty) {
							struc.push({
								id:f, field:f, name:f, width:"auto"
							});
						}
					}
				}
				
				this._updateMapStore();
			}
			
			if(this.mapGrid) {
				this.mapGrid.destroyRecursive();
				delete this.mapGrid;
			}
			
			this.mapGrid = new Grid({
				cacheClass: Async,
				store:this.mapStore,
				'class': 'gridxAlternatingRows',
				structure: struc,
				modules: [
				   RowHeader, SingleSort, ToolBar, Focus, ColumnResizer, Filter, QuickFilter
				],
				style:"width:100%; height:100%"
			}).placeAt(this.domNode);

			if(this.viewAttributes) {
				// @see idx.form.buttons for standard button types
				this.prevItem = new Button({
					buttonType:"previousPage",
					onClick:lang.hitch(this, "_selectPrevMapItem")
				});
				this.prevItem.startup();
				this.mapGrid.toolBar.widget.addChild(this.prevItem);

				this.nextItem = new Button({
					buttonType:"nextPage",
					onClick:lang.hitch(this, "_selectNextMapItem")
				});
				this.nextItem.startup();
				this.mapGrid.toolBar.widget.addChild(this.nextItem);
			}
			
			this.mapGrid.startup();
			this._updateButtonStates();
		},
		
		_getDataArray: function() {
			return lang.isArray(this.data) ? this.data : [this.data];
		},
		
		_getItem: function(index) {
			var arr = this._getDataArray();
			if(arr && index < arr.length)
				return arr[index];
			else
				return null;
		},
		
		_hasNext: function() {
			return this._getItem(this.mapIndex+1) != null;
		},
		
		_hasPrev: function() {
			return this.mapIndex > 0;
		},
		
		_selectMapIndex: function(index) {
			var item = this._getItem(index);
			if(item) {
				this._createStore();
				for(var f in item) {
					this.mapStore.add({
						attr:f,
						value:item[f]
					});
				}
				if(this.mapGrid)
					this.mapGrid.setStore(this.mapStore);
			}
			this._updateButtonStates();
		},
		
		_selectNextMapItem: function() {
			if(this._hasNext()) {
				this._selectMapIndex(++this.mapIndex);
			}
		},
		
		_selectPrevMapItem: function() {
			if(this.mapIndex > 0) {
				this._selectMapIndex(--this.mapIndex);
			}
		},
		
		_updateMapStore: function() {
			var arr = lang.isArray(this.data) ? this.data : [this.data];
			if(this.viewAttributes) {
				this.mapIndex = 0;
				this._selectMapIndex(0);
			} else {
				// -- Add items
				array.forEach(lang.isArray(arr) ? arr : [arr], function(item) {
					mapStore.add(item);					
				});
			}
		},

		_updateButtonStates: function() {
			if(this.nextItem)
				this.nextItem.set("disabled", !this._hasNext());
			if(this.prevItem)
				this.prevItem.set("disabled", !this._hasPrev());
		},
		
		resize: function(obj) {
			this.inherited(arguments);
			if(this.mapGrid) {
				this.mapGrid.resize(obj);
			}
		},
		
		startup: function() {
			if(this.data) {
				this._createGrid();
			}
		}
	})
});
