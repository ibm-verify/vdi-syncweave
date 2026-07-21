/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojox/gfx",
	"dojox/gfx/utils",
	"tdi/tdiconfig",
	"tdi/aleditor/Colors",
	"dijit/Tooltip"
], function(declare, Gfx, GfxUtils, tdiconfig, TDI, dTooltip) {
	
return declare(
	null,
	{
		isDropPoint: true,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
			this.createDropPoint(this.x, this.y);
		},
		
		createDropPoint: function(x, y) {
			this.dropPointShape = this.surface.createRect({
				x:x,
				y:y,
				width:1,
				height:1,
				r: 0
			}).setStroke({color:TDI.lineColor, width:TDI.lineWidth, cap:"round"});
		},
		
		addDropHandler: function(func) {
			if(this.dropPointShape) {
				this.dropTarget = new dojo.dnd.Target(this.dropPointShape.rawNode);
				dojo.connect(this.dropTarget, "onDrop", func);
			}
		},
		
		getBoundingBox: function() {
			if(this.dropPointShape) {
				return this.dropPointShape.getBoundingBox();
			} else {
				return null;
			}
		},
		
		getId: function() {
			return "droppoint_" + this.x + "_" + this.y;
		}
	})
});