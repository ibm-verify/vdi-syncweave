/**
 * The ALDataCollector displays data tables for AL components
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/layout/ContentPane",
	"tdi/aleditor/ALUtils",
	"tdi/tdiconfig",
	"tdi/tdiapi",
	"tdi/FilteredLogViewer",
	"tdi/orion/OrionEditor",
	"tdi/TableWidget",
	"tdi/atom/tdicientry",
	"dijit/layout/BorderContainer",
	"idx/layout/HeaderPane"
], function(declare, array, Widget, TemplatedMixin, ContentPane, alutils, tdiconfig, tdiapi, FilteredLogViewer, JavascriptEditor, TableWidget, tdicientry, BorderContainer, HeaderPane) {
	
return declare(
	[Widget, TemplatedMixin],
	{
		templateString: "<div data-dojo-attach-point='Main' style='width:100%; height:100%; margin:0; padding:0'></div>",
		title: null,
		
		setRuntime: function(runtime) {
			this.runtime = runtime;
			if(runtime) {
				this.createGrid(null);
			} else {
				this.createGrid(this.config.getWorkAttributeMaps());
			}
			this.center.set("content", this.grid);
		},
		
		displayData: function(data) {
			if(!this.grid) {
				this.createGrid();
			}
			var store = this.grid.newStore();
			for(f in data) {
				try {
					store.put({
						attribute:f,
						value:data[f]
					});
				} catch(err) {
					console.log(err);
				}
			}
			this.grid.setStore(store);
		},
		
		resize: function(obj) {
			if(this.border && obj && obj.h > 0) {
				this.border.resize(obj);
			}
		},
		
		getTitle : function() {
			if(this.title)
				return this.title;
			else
				return this.config.getName();
		},
		
		getEditStructure: function() {
			this.idProperty = "id";
			this.layout = [
			    {id:"name", field:"name", name:"Attribute", width:"auto"},
			    {id:"component", field:"component", name:"Component", width:"auto"}
			];
			return this.layout;
		},
		
		getRuntimeStructure: function() {
			this.idProperty = "attribute";
			this.layout = [
			    {id:"attribute", field:"attribute", name:"Attribute", width:"auto"},
			    {id:"value", field:"value", name:"Value", width:"auto"}
			];
			return this.layout;
		},

		createGrid: function(data) {
			if(this.grid) {
				this.grid.destroyRecursive();
				this.grid = null;
			}
			var layout = this.runtime ? this.getRuntimeStructure() : this.getEditStructure();
			this.grid = new TableWidget({
				idProperty: this.idProperty,
				structure:layout,
				style:"width:100%; height:100%; margin:0; padding:0"
			});
			this.grid.createGrid(data ? data : []);
			this.grid.startup();
			return this.grid;
		},
		
		createJavascriptEditor: function() {
			return new JavascriptEditor({config:this.config, header:false});
		},
		
		getCenterPane : function() {
			if(!this.config)
				return "";
			else if(this.config.isAssemblyLine && this.config.isAssemblyLine()) {
				return this.createGrid(this.config.getWorkAttributeMaps(), false); // this.config.getWorkAttributeMaps());
			} else if(this.config.isScript && this.config.isScript())
				return this.createJavascriptEditor();
			else if(this.config.isBranch && this.config.isBranch())
				return alutils.getTooltip(this.config);
			else if(this.config.isLoop && this.config.isLoop())
				return alutils.getTooltip(this.config);
			else
				return this.createGrid();
		},
		
		startup : function() {
			this.border = new BorderContainer({gutters:false, style:"width:100%; height:100%; margin:0; padding:0"}).placeAt(this.Main);
			this._supportingWidgets.push(this.border);
			
			if(this.config && this.config.isScript && this.config.isScript()) {
				; // no header
			} else {
				this.top = new ContentPane({
					region:"top",
					style:"width:100%",
					content:"<b>" + this.getTitle() + "<b>"
				});
				this.border.addChild(this.top);
			}

			this.center = new ContentPane({
				region:"center",
				style:"width:100%; margin:0; padding:0",
				content:this.getCenterPane()
			});
			this.border.addChild(this.center);
			
			this.border.startup();
			
			this.inherited(arguments);
		}
	});
});