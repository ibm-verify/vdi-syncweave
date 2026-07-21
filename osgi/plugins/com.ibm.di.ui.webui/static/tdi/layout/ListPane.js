/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/dom-style",
	"dojo/dom-class",
	"dojo/dom-construct",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/layout/ListPaneItem",
	"tdi/tdiconfig",
	"dojo/dnd/Source"
], function(declare, domStyle, domClass, domConstruct, _Widget, _TemplatedMixin, _WidgetsInTemplate, ListPaneItem, tdiconfig, dndSource) { 
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplate ],
	{
		templateString : "<div data-dojo-attach-point='Main' style='width:100%; height:100%; margin:0; padding:0'></div>",
		
		// titleAttribute: string
		//		The attribute to use for display
		titleAttribute: "title",
		
		// items: array
		//		Array of all list items on page
		items: [],
		
		addListItem: function(obj) {
			this.items.push(new ListPaneItem({data:obj, title:obj[this.titleAttribute]}).placeAt(this.listNode));
		},
		
		addItems: function(store) {
			if(store && store.fetch) {
				store.fetch({
					onItem:dojo.hitch(this, "addListItem")
				});
			} else {
				console.log("BAD store pased to ListPane: " + store);
			}
		},
		
		addSubHeader: function(header) {
			var hdr = domConstruct.create("div", {innerHTML:"<p><center><b>"+header+"</b></center></p>"}, this.listNode);
		},
		
		enableDnD: function() {
			this.dnd = new dndSource(this.listNode);
		},
		
		postCreate: function() {
			this.titleNode = domConstruct.create("div", {}, this.Main);
			this.listNode = domConstruct.create("div", {style:"overflow:scroll; margin:5px; padding:5px"}, this.Main);
		}
	})
});