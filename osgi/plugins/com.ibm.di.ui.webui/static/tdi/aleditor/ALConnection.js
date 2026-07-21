/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojox/gfx",
	"dojox/gfx/utils",
	"dojox/fx",
	"tdi/tdiconfig",
	"tdi/aleditor/Colors",
	"dijit/Destroyable",
	"dijit/Tooltip",
	"tdi/aleditor/TooltipMixin",
	"idx/form/Link"
], function(declare, lang, html, Gfx, GfxUtils, Fx, tdiconfig, TDI, Destroyable, dTooltip, TooltipMixin, Link) {
	
return declare(
	[TooltipMixin, Destroyable],
	{
		// surface: GFX surface
		//		The surface to draw on
		surface: null,
		
		// source: GFX node
		//		The source node for the connection line
		source: null,
		
		// target: GFX node
		//		The target node of the connection line
		target: null,
		
		// dropPoint: boolean
		//		If true, a drop point is created where components
		//		can be dropped to insert new components.
		dropPoint: true,
		
		// expandPoint: boolean
		//		If true, an expander is created to expand/collapse children
		expandPoint: false,
		
		// arrowHead: boolean
		//		If true, an arrowhead is added to the end of line
		arrowHead: true,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
			this.refresh();
		},
		
		destroyRecursive: function() {
			this.inherited(arguments);
			this.destroyShapes();
		},
		
		destroyShapes: function() {
			if(this.lineShape) {
				this.lineShape.destroy();
				this.lineShape = null;
			}
			if(this.dropPointShape) {
				this.dropPointShape.destroy();
				this.dropPointShape = null;
			}
		},
		
		refresh: function() {
			this.destroyShapes();
			this.createConnectorLine(this.source, this.target);
		},
		
		getSource: function() {
			return this.source;
		},
		
		getTarget: function() {
			return this.target;
		},
		
		edgePoint: function(box, loc) {
			// summary:
			//		Returns the edge-point for a box (middle point along side loc)
			var spacer = 4;
			switch(loc) {
			case "left":
				return {x:box.x  - spacer, y:box.y + (box.height/2)};
			case "top":
				return {x:box.x + (box.width/2), y:box.y - (3*spacer)};
			case "bottom":
				return {x:box.x + (box.width/2), y:box.y + box.height + spacer};
			case "right":
				return {x:box.x + box.width + spacer, y:box.y + (box.height/2)};
			}
		},
		
		createConnectorLine: function(source, target) {
			// summary:
			//		Draws the line shapes that make up the line between
			//		source and target w/optional arrow.
			var b1 = source.getBoundingBox();
			var b2 = target.getBoundingBox();
			var x = 0, y = 0;
			var path = this.surface.createPath();
			
			var sourceLeft = this.edgePoint(b1, "left");
			var sourceTop = this.edgePoint(b1, "top");
			var sourceBottom = this.edgePoint(b1, "bottom");
			var sourceRight = this.edgePoint(b1, "right");
			
			var targetLeft = this.edgePoint(b2, "left");
			var targetTop = this.edgePoint(b2, "top");
			var targetBottom = this.edgePoint(b2, "bottom");
			var targetRight = this.edgePoint(b2, "right");
			
			var fromPoint = null;
			var toPoint = null;
			
			// -- Return goes from sourceBottom -> down -> left (relative to source bottom(
			if(this.type == "return") {
				fromPoint = sourceBottom;
				path.moveTo(fromPoint.x, fromPoint.y);
				fromPoint.y += 30;
				path.lineTo(fromPoint.x, fromPoint.y)
				toPoint = targetBottom;
				toPoint.x += 10;
				toPoint.y = fromPoint.y;
				path.lineTo(toPoint.x, fromPoint.y);
				
			} else {
				if(this.type == "branch") {
					// -- Branch goes from source-rigth to target-left
					fromPoint = sourceRight;
					toPoint = targetLeft;

				} else if(sourceRight.x < targetLeft.x) {
					// -- Target is east of right edge
					fromPoint = sourceRight;
					if(b2.y > b1.y + b1.height ) {
						toPoint = targetTop;
					} else if(b1.y > b2.y + b2.height ) {
						toPoint = targetBottom;
					} else {
						toPoint = targetLeft;
					}
				} else if(sourceLeft.x > targetRight.x) {
					// -- Target is west of left edge
					fromPoint = sourceBottom;
					if(targetBottom.y < sourceTop.y)
						toPoint = targetTop;
					else
						toPoint = targetRight;
				} else {
					// -- Source on top of target (we assume)
					fromPoint = sourceBottom;
					toPoint = targetTop;
				}
				
	
				// -- Draw lines
				path.moveTo(fromPoint.x, fromPoint.y);
				if(fromPoint.x < toPoint.x) {
					path.lineTo(toPoint.x, fromPoint.y);
				} else {
					path.lineTo(fromPoint.x, toPoint.y);
				}		
				path.lineTo(toPoint.x, toPoint.y);
			}
			
			// -- Draw arrow head
			if(this.arrowHead) {
				path.lineTo(toPoint.x, toPoint.y);
				if(toPoint == targetTop) {
					path.lineTo(toPoint.x - 4, toPoint.y - 12);
					path.moveTo(toPoint.x, toPoint.y);
					path.lineTo(toPoint.x + 4, toPoint.y - 12);
				} else if (this.type == "return" || toPoint == targetRight) {
					path.lineTo(toPoint.x + 8, toPoint.y - 4);
					path.moveTo(toPoint.x, toPoint.y);
					path.lineTo(toPoint.x + 8, toPoint.y + 4);
				} else if (toPoint == targetLeft) {
					path.lineTo(toPoint.x - 8, toPoint.y - 4);
					path.moveTo(toPoint.x, toPoint.y);
					path.lineTo(toPoint.x - 8, toPoint.y + 4);
				}
					
			}

			path.setStroke({color:TDI.lineColor, width:TDI.lineWidth, cap:"round"});
			
			this.lineShape = path;
			
			if(this.dropPoint) {
				console.log("** " + this.sourceId + ": type=" + this.type + "; " + fromPoint.y + ":" + toPoint.y);
				this.createDropPoint(fromPoint, toPoint);
				if(this._dropHandlerTarget) {
					this.updateDropHandler(this.updateDropHandler);
				}
			}
			
			var t = this;
			this.lineShape.connect("onclick", function() {
				alert("this.sourceId = " + t.sourceId);
			});
			
		},
		
		createDropPoint: function(fromPoint, toPoint) {
			// summary:
			//		Creates a shape where components can be dropped
			var x = 0;
			var y = 0;
			if(fromPoint.x < toPoint.x) {
				x = toPoint.x - ((toPoint.x - fromPoint.x) / 2);
				y = fromPoint.y;
			} else {
				x = fromPoint.x;
				y = toPoint.y - ((toPoint.y - fromPoint.y) / 2);
			}
			var width = 10;
			var height = 10;
//			this.dropPointShape = this.surface.createRect({
//				x:x - (width/2),
//				y:y - (height/2),
//				width:width,
//				height:height,
//				r: 0
//			});
			this.dropPointShape = this.surface.createCircle({
				cx:x,
				cy:y,
				r: 10
			});
			this.dropPointShape.setStroke({color:"green", width:3, cap:"round"});
//			this.dropPointShape.setStroke({color:TDI.lineColor, width:TDI.lineWidth, cap:"round"});
			this.dropPointShape.setFill("white");
			
			// remove it from surface as we only display this
			// when user is dragging stuff.
			this.hideDropPoint();
		},
		
		showDropPoint: function() {
			if(this.dropPointShape)
				this.surface.add(this.dropPointShape);
		},
		
		hideDropPoint: function() {
			if(this.dropPointShape)
				this.dropPointShape.removeShape();
		},
		
		toggleExpanded: function() {
			this.expanded = !this.expanded;
			this.onToggleExpanded(this.expanded);
		},
		
		onToggleExpanded: function(expanded) {
			// summary:
			//		callback - called when expanded state is toggled
		},
		
		onInsertComponent: function() {
			// summary:
			//		Callback when component is dropped on drop shape
		},
		
		updatePosition: function() {
			// summary:
			//		Destroys existing line shapes and draws new ones
			if(this.lineShape) {
				this.surface.remove(this.lineShape);
				this.lineShape = null;
			}
			if(this.dropPointShape) {
				this.surface.remove(this.dropPointShape);
				this.dropPointShape = null;
			}
			this.createConnectorLine(this.source, this.target);
		},

		updateDropHandler: function() {
			// summary:
			//		Create DND target and connect drop event to handler.
			if(this.dropPointShape) {
				this.dropTarget = new dojo.dnd.Target(this.dropPointShape.rawNode, {accept:["text", "treeNode"]});
				dojo.connect(this.dropTarget, "onDrop", this._dropHandlerTarget);
				this.dropPointShape.connect("onclick", dojo.hitch(this, "onInsertComponent", this));
			}
		},
		
//		set
		
		addDropHandler: function(func) {
			// summary:
			//		Creates DND drop target with func as callback
			this._dropHandlerTarget = func;
			this.updateDropHandler();
		}
	})
});
