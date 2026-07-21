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
		
		createElement: function(item, id) {
			var div = dojo.create("div", {
				"class":"dojoDndItem",
				dndData:id,
				style:"border:1px solid; border-radius: 5px; margin:5px"
			},this.Main);
			dojo.create("img", {src:"/dashboard/static/images/Branch_Enabled.gif"}, div);
			dojo.create("span", {innerHTML:item}, div);
		},
		
		postCreate: function() {
			var flow = ["IF Branch", "ELSE If Branch", "ELSE Branch", "Switch branch", "Case branch", "While branch",
			            "Attribute Loop", "Connector Loop"];
			var flowid = ["@If", "@ElseIf", "@Else", "@Switch", "@Case", "@while","@aloop", "@cloop"];
			for(i = 0; i <  flow.length; i++) {
				this.createElement(flow[i], flowid[i]);
			}
			this.source = new dnd(this.Main, {
				copyOnly:true,
				selfAccept:false
			});
		},		
	});

});