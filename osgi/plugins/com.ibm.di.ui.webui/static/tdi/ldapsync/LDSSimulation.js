/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/dialogs",
	"idx/widget/SingleMessage",
	"./LDSUtil",
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
	"tdi/ToolbarLabel",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSSimulation.html"
], function(declare, array, lang, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, idx, SingleMessage, LDSUtil,
		Grid, CellWidget, ColumnResizer, Edit, Focus, SingleSort, ToolBar, SelectRow, 
		IndirectSelect, RowSelect, RowHeader, Filter, QuickFilter, Async, Memory, ToolbarLabel, tdiapi, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		

		createGrid: function() {
			this.mapStore = new Memory({
				idProperty:"id",
				data: []
			});
			
			var struc = [
			    {id:"timestamp", 	field:"timestamp", name:this.getString("FDS_writeBack_timestamp"), width:"15%"},
			    {id:"operation",	field:"operation", name:this.getString("WebCE.operation"), width:"15%"},
			    {id:"changes",		field:"changes", name:this.getString("FDS_writeBack_changes"), width:"auto"}
			];
			
			this.mapGrid = new Grid({
				cacheClass: Async,
				store:this.mapStore,
				'class': 'gridxAlternatingRows',
				structure: struc,
				modules: [
				   RowHeader, SingleSort, ToolBar, Focus, ColumnResizer, Filter, QuickFilter
				],
				style:"width:100%; height:100%"
			}).placeAt(this._grid);
			this.mapGrid.startup();
		},
		
		getTitle: function() {
			return this.getString("FDS.writeBack");
		},
		
		resize: function(obj) {
			if(this._border && this._border.resize) {
				this._border.resize(obj);
			}
			if(this.mapGrid) {
				this.mapGrid.resize();
			}
		},
		
		simulateEvent: function(event) {
			// -- Add writeback event
			try {
				var data = dojo.fromJson(event.data.value);
				if(data.entry) {
					data.changes = dojo.toJson(data.entry);
				} else {
					data.changes = "";
				}
				if(data.state == "completed") {
					data.changes = this.getString("WebCE.stopped");
				}
				if(data.timestamp) {
					data.timestamp = tdiutil.formatDate(new Date(data.timestamp));
				}
				this.mapStore.add(data);
			} catch(err) {
				console.log(err);
			}
		},
		
		startup: function() {
			var t = this;
			t.createGrid();
			t.resize();
		}
	})
});