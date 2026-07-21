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
	"tdi/tdiconfig",
	"dojo/dnd/Source"	
], function(declare, domStyle, domClass, domConstruct, _Widget, _TemplatedMixin, _WidgetsInTemplate, tdiconfig, DndSource) { 
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplate ],
	{
		templateString : "<div data-dojo-attach-point='Main' style='cursor:pointer; width:100%; height:100%; margin-top:4px; margin-bottom:4px; padding:4; border-radius:5; border-bottom: 1px solid grey;'></div>",
		
		postCreate: function() {
			if(this.title) {
				this.titleNode = domConstruct.create("span", {innerHTML:this.title}, this.Main);
				domClass.add(this.titleNode, "dojoDndItem");
//				new DndSource(this.Main, {selfAccept:false, copyOnly:true, accept:""});
			}
		}
	})
});