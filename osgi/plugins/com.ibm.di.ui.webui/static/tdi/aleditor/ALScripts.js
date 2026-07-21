/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"dojo/dnd/Source"
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplate, tdiapi, tdiconfig, dnd) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplate ],
	{
		templateString : "<div data-dojo-attach-point='Main' style='height:100%; width:100%; overflow:scroll'></div>",
		widgetsInTemplate: true,
	
		onClick: function(e) {
			alert(e);
		},
		
		createElement: function(item) {
			var div = dojo.create("div", {
				"class":"dojoDndItem",
				dndData:item,
				style:"border:1px solid; border-radius: 5px; margin:5px"
			},this.Main);
			dojo.create("img", {src:"/dashboard/static/images/Script_16.gif"}, div);
			dojo.create("span", {innerHTML:item}, div);
		},
		
		postCreate: function() {
			var flow = ["Dump Work Entry", "Empty Script", "AssemblyLine Prolog", "AssemblyLine Epilog"];
			for(i = 0; i <  flow.length; i++) {
				this.createElement(flow[i]);
			}
			this.source = new dnd(this.Main, {
				copyOnly:true,
				selfAccept:false
			});
		},		
	});

});