define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dojox/grid/EnhancedGrid",
	"tdi/model/ServerProjectsModel",
	"dojox/grid/enhanced/plugins/DnD",
	"dojo/text!./templates/GridWidget.html",
	"idx/layout/HeaderPane",
	"idx/form/Link",
	"idx/layout/ButtonBar"
], function(declare, Widget, TemplatedMixin, WidgetsInTemplateMixin, Button, Grid, ServerProjectsModel, EnhancedGridDnd, Template, HeaderPane, FormLink, ButtonBar) {
	
return declare(
	[Widget, TemplatedMixin ],
	{
		templateString: Template, // "<div style='height:100%; width:100%' data-dojo-attach-point='Main'></div>",
		
		addItem: function(item) {
			this.store.addItem(item);
		},
		
		addToToolbar: function(widget) {
		},
		
		addToActionList: function(menuitem) {
		},
		
		resize : function(size) {
			if(this.grid) {
				this.grid.resize(size);
			}
		},
		
		onRowClick: function(e) {
			console.log("Clicked item: " + e.index);
		},
		
		getStore: function() {
			return this.store;
		},
		
		postCreate: function() {
			if(!this.store) {
				this.store = new ServerProjectsModel({});
			}
			
			if(!this.layout) {
				this.layout = [
				    {id:"name", field:"name", name:"Name", width:"auto"}
				];
			}
			
			this.grid = new Grid({
				store:this.store,
				structure:this.layout,
				plugins: {dnd:true}
			});
			this.grid.placeAt(this.Main);
			this.grid.startup();
			
			dojo.connect(this.grid, "onRowClick", dojo.hitch(this, "onRowClick"));
		}
	});
});
