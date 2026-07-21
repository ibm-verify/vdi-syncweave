/**
 * The Border class is a base template for a border or stack layout based widget
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/dom-attr",
	"dojo/dom-class",
	"dijit/_CssStateMixin",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dojo/text!./templates/ALButton.html"
], function(declare, array, lang, domAttr, domClass, CssStateMixin, Widget, TemplatedWidget, WidgetsInTemplateMixin, template) {
	
	return declare(
		[Widget, TemplatedWidget, WidgetsInTemplateMixin, CssStateMixin],
		{
			templateString: template,
			
			baseClass: "tdiWebDevNavButton",
			
			hoverImage: "Gear.png",
			normalImage: "Gear_gray.png",
			
//			cssStateNodes: {
//				"imageNode":"tdiGearImage"
//			},

	         // Attributes
	         title: "unknown",
	         _setTitleAttr: { node: "titleNode", type: "innerHTML" },
	         
	         _setStateClass: function() {
	        	 this.inherited(arguments);
	        	 if(this.hovering) {
	        		 this.imageNode.setAttribute("src", "/fds/static/images/" + this.hoverImage);
	        	 } else {
	        		 this.imageNode.setAttribute("src", "/fds/static/images/" + this.normalImage);
	        	 }
	         },

	         _onClick: function(event) {
	        	 this.onClick(event);
	         },
	         
	         onClick: function(event) {
	        	 // summary:
	        	 //		Callback for button clicks
	         },
	         
	         postCreate: function() {
	        	 this.inherited(arguments);
	        	 this._applyAttributes();
	         }
		});
});
