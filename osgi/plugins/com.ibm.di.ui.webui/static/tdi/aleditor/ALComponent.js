/**
 * The ALCompoennt draws a component box in GFX
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojox/gfx",
	"dojox/gfx/utils",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/aleditor/Colors",
	"dijit/popup",
	"dijit/TooltipDialog",
	"dijit/Toolbar",
	"dijit/TitlePane",
	"idx/layout/HeaderPane",
	"dijit/form/Button",
	"tdi/aleditor/quicklook/ALConnectorTooltip",
	"tdi/aleditor/quicklook/ALScriptTooltip",
	"tdi/aleditor/quicklook/ALBranchTooltip",
	"tdi/aleditor/TooltipMixin"
], function(declare, lang, html, Gfx, GfxUtils, tdiconfig, tdiutil, TDI, dPopup, TooltipDialog, Toolbar, TitlePane, HeaderPane, Button,
		ALConnectorTooltip, ALScriptTooltip, ALBranchTooltip, TooltipMixin) {
	
return declare(
	[TooltipMixin],
	{
		surface: null,
		config: null,
		shapeSize: null,
		isComponent: true,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
			this.createComponent(this.config);
			this.connectMouseEvents(this.group);
			dojo.connect(this.config, "onModify", lang.hitch(this, "updateTooltip"));
		},
		
		setEntry: function(entry) {
			this.entry = entry;
			this.updateTooltip();
		},
		
		onTooltipClick: function(caller, event, tab) {
			this.clearFocus();
			this.hideTooltip();
			this.onClick(this, event, tab);
		},
		
		onClick: function(caller, event, tab) {
			// callback
			// Called when component is clicked
		},
		
		onTooltipDelete: function() {
			this.onDelete(this);
		},
		
		onDelete: function(caller) {
			// callback
		},
		
		getConfig: function() {
			return this.config;
		},
		
		isContainer: function() {
			return this.getConfig().isContainer();
		},
		
		setComponentType: function(type) {
			this.config.setConnectorType("system:/Connectors/" + type);
		},
		
		getBoundingBox: function() {
			var shape = this.group;
			var cc = shape.getBoundingBox();
			
			if(!cc && shape.children && shape.children.length > 0)
				cc = shape.children[0].getBoundingBox();
			
			var bb = {};
			for(f in cc)
				bb[f] = cc[f];
			
			if(this.path) {
				var pb = this.path.getBoundingBox();
				bb.height += (pb.height - (bb.height/2));
			}
			return bb;
		},
		
		createComponent: function(comp) {
			var componentSize = this.shapeSize;
			var g = this.surface.createGroup();
			var rect = g.createRect({
				x:this.x,
				y:this.y,
				width:componentSize.width,
				height:componentSize.height,
				r: 5
			}).setStroke("black").setFill("white");
			rect.shape.tdiId = comp.getName();
			rect.shape.tdiType = "component";
			
			var bb = rect.getBoundingBox();
			
			var image = tdiutil.getComponentIconURL(comp, "/dashboard/static/images/Connector_Iterator_Enabled.gif");
			if(image) {
				this.image = g.createImage({
					src:image,
					x:bb.x + 5,
					y:bb.y + 5,
					width:32,
					height:32
				});
			}
						
			var tx = bb.x + 5 + 35;
			var text = comp.getName();
			text = text.substring(0, (componentSize.width-40) / 8);
			this.title = g.createText({
				x:tx,
				y:bb.y + 17,
				width:componentSize.width - tx,
				text:text
			}).setStroke("#4682B4"); // .setFont({size:"14pt", family:"sanserif"});
			
			if(!comp.isEnabled())
				this.title.setStroke("red");
			
			this.group = g;
			this.rect = rect;
		},
		
		createDropPoint: function(x, y) {
			var width = 10;
			var height = 10;
			this.dropPointShape = this.surface.createRect({
				x:x - 5,
				y:y,
				width:10,
				height:10,
				r: 2
			}).setStroke("grey").setFill("lightgreen");
		},
		
		getGroup: function() {
			return this.group;
		},
		
		getId: function() {
			return this.config.getName();
		},
		
		addDropHandler: function(func) {
			this.dropTarget = new dojo.dnd.Target(this.group.rawNode, {accept:["text", "treeNode"]});
			dojo.connect(this.dropTarget, "onDrop", func);
		},

		addAddDropHandler: function(func) {
			if(this.dropPointShape) {
				this.dropTargetAdd = new dojo.dnd.Target(this.dropPointShape.rawNode);
				dojo.connect(this.dropTargetAdd, "onDrop", func);
			}
		},
		
		animateStrokeMouseOver: function(args) {
			new dojox.gfx.fx.animateStroke({
				duration: 100,
				shape: this.rect,
				// color: {start: "red", end: "green"},
				width: {end: 3},
				join:  {values: ["miter", "bevel", "round"]},
				onAnimate: function() {
				},
				onEnd: function() {
				}
			}).play();
		},
		
		animateStrokeMouseOut: function(args) {
			if(!this.selected) {
				new dojox.gfx.fx.animateStroke({
					duration: 100,
					shape: this.rect,
					width: {end: 1},
					join:  {values: ["round", "bevel", ""]},
				}).play();
			}
		},
		
		getEntryValue: function(attr) {
			if(this.entry && this.entry[attr]) {
				return " (" + this.entry[attr] + ")";
			} else {
				return "";
			}
		},
		
		updateTooltip: function() {
			var arr = new Array();
			var widget = null;
			
			arr.push("<b>" + this.getId() + "</b>");
			if(this.config.isConnector()) {
				widget = new ALConnectorTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				
			} else if(this.config.isScript()) {
				widget = new ALScriptTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				
			} else if(this.config.isBranch()) {
				widget = new ALBranchTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				
			} else if(this.config.isBranch()) {
				arr.push("Loop Condition");
				arr.push("Type: " + this.config.getBranchType());
			}

			this.setTooltipContent(widget ? widget : arr.join("<br>"), this.config.getName());
		},
		
		connectMouseEvents: function(shape) {
			shape.connect("onmouseover", dojo.hitch(this, "animateStrokeMouseOver"));
			shape.connect("onmouseout", dojo.hitch(this, "animateStrokeMouseOut"));
			// -- Connect mouse events to tooltip mixin
			shape.connect("onmouseover", dojo.hitch(this, "tooltipOnMouseOver"));
			shape.connect("onmouseout", dojo.hitch(this, "tooltipOnMouseOut"));
			shape.connect("onclick", dojo.hitch(this, function(event) {
				this.onClick(this, event);
			}));

			// -- upate the tooltip
			this.updateTooltip();
		},
		
		setSelected: function(selected) {
			var sel = this.selected;
			this.selected = selected;
			if(sel && !selected)
				this.animateStrokeMouseOut();
			else if(!sel && selected)
				this.animateStrokeMouseOver();
		}
	})
});
