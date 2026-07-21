/**
 * The ALComponent2 shows a short representation of a component with menus and links.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/fx",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/aleditor/Colors",
	"dojo/text!./templates/ALPlaceholder.html"
], function(declare, lang, fx, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TDI, template) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString: template,
		
		// isComponent: Boolean
		//		True if this is a component
		isComponent: false,
		
		baseClass: "tdiWebDevPlaceholder",
		
		// title: String
		//		The title
		title: "",
		_setTitleAttr: { node: "titleNode", type: "innerHTML" },		
		
		// content: String
		//		The title
		content: "",
		_setContentAttr: { node: "contentNode", type: "innerHTML" },
		
		// image: String
		//		The image class
		image: "",
		_setImageAttr: { node: "imageNode", type: "class" },
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		getId: function() {
			return this.config ? this.config.getName() : this.id;
		},

		onClick: function(caller, event, tab) {
			// callback
			// Called when component is clicked
		},
		
		animateMouseOver: function(args) {
			var color = this.selected ? TDI.selectionColor : TDI.hoverColor;
			fx.animateProperty({
				node:this.domNode,
				duration: 100,
				properties: {
					"border-color":color,
					"border-width": "3"
				}
			}).play();
		},
		
		animateMouseOut: function(args) {
			if(!this.selected) {
				fx.animateProperty({
					node:this.domNode,
					duration: 100,
					properties: {
						"border-color":TDI.lineColor,
						"border-width": "1"
					}
				}).play();
			}
		},
		
		setSelected: function(selected) {
			var sel = this.selected;
			this.selected = selected;
			if(sel && !selected)
				this.animateMouseOut();
			else if(!sel && selected)
				this.animateMouseOver();
		},
		
		getBoundingBox: function() {
			var box = {
				y: parseFloat(this.domNode.style.top.replace("px", "")),
				x: parseFloat(this.domNode.style.left.replace("px", "")),
				width: parseFloat(this.domNode.style.width.replace("px", "")),
				height: parseFloat(this.domNode.style.height.replace("px", ""))
			};
			return box;
		},
		
		setBoundingBox: function(box) {
			// summary:
			//		Sets the bounding for the UI component.
			//		Box must contains numbers only (e.g. no 10px etc)
			var style = {
					position:"absolute",
					top:box.y + "px",
					left:box.x + "px",	
					width:box.width + "px",
					height:box.height + "px"
			};
			for(var f in style) {
				this.domNode.style[f] = style[f];
			}
		},

		postCreate: function() {
			var t = this;
			t.own(t.connect(t.domNode, "onmouseover", lang.hitch(t, "animateMouseOver")));
			t.own(t.connect(t.domNode, "onmouseout", lang.hitch(t, "animateMouseOut")));
			
//			t.own(t.connect(t.imageNode, "onmouseover", lang.hitch(t, "tooltipOnMouseOver")));
//			t.own(t.connect(t.imageNode, "onmouseout", lang.hitch(t, "tooltipOnMouseOut")));
//			
			t.own(t.connect(t.domNode, "onclick", lang.hitch(t, function(event) {
				t.clearFocus();
				t.hideTooltip();
				t.onClick(t, event);
			}, t)));
		}
	})
});

		
		
