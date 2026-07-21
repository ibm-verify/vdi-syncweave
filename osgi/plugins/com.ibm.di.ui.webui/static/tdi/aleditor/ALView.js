/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/tdiconfig",
	"tdi/aleditor/Colors"
], function(declare, Widget, TemplatedMixin, WidgetsInTemplateMixin, tdiconfig, TDI) {
	
return declare(
	[Widget, TemplatedMixin, WidgetsInTemplateMixin],
	{
		templateString: "<div><div style='cursor:pointer' data-dojo-attach-point='Name' data-dojo-attach-event='onclick:onClick'</div></div>", 

		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		onClick: function() {
			alert(this.config.getName());
		},
		
		startup: function() {
			this.Name.innerHTML = this.config.getName();
			this.inherited(arguments);
			dojo.connect(this.Name, "onmouseover", function(evt) {
				dojo.style(evt.target, "color", "blue");
			});
			dojo.connect(this.Name, "onmouseout", function(evt) {
				dojo.style(evt.target, "color", "black");
			});
		}
		
	})
});