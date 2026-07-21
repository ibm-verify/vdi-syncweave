/**
 * The LinkCriteria widget shows a list of inline editable rows of link criteria
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/Toolbar",
	"tdi/aleditor/Border",
	"tdi/LinkCriteriaItem",
	"tdi/orion/OrionEditor",
	"tdi/ToolbarLabel"
], function(declare, lang, array, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, Toolbar, Border, LinkCriteriaItem, 
		JavascriptEditor, ToolbarLabel) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : "<div style='margin:0; padding:0; width:100%; height:100%'><div dojoType='tdi/aleditor/Border' containerType='stack' style='margin:0; padding:0; width:100%; height:100%' data-dojo-attach-point='border'</div>",
		
		resize: function(obj) {
			if(this.border)
				this.border.resize(obj);
		},
		
		newCriteria: function() {
			var lc = this.config.getLinkCriteria().newCriteria();
			this.appendCriteria(lc);
		},
		
		appendCriteria: function(lc) {
			var lci = new LinkCriteriaItem({config:lc}).placeAt(this.linkDiv);
			lci.onDelete = lang.hitch(this, function() {
				lci.destroyRecursive();
				this.border.resize();
			});
			lci.startup();
		},
		
		toggleScript: function(event) {
			var lc = this.config.getLinkCriteria();
			lc.setAdvanced(!lc.isAdvanced());
			if(lc.isAdvanced()) {
				this.border.selectContainerPane("Script");
			} else {
				this.border.selectContainerPane("Link");
			}				
		},
		
		toggleMatchAny: function(event) {
			var lc = this.config.getLinkCriteria();
			lc.setMatchAny(!lc.isMatchAny());
		},
		
		postCreate: function() {
			
			this.linkDiv = html.create("div", {style:"margin:0; padding:0; width:100%; height:100%"});
			
			var link = this.config.getLinkCriteria();
			array.forEach(link.getLinkAttributes(), function(attr) {
				var lc = link.getCriteriaFor(attr);
				this.appendCriteria(lc);
			}, this);
			this.border.addContainerPane(this.linkDiv, {title:"Link"});
			this.border.addContainerPane(new JavascriptEditor({config:link, autoUpdate:true}), {title:"Script"});
			
			if(link.isAdvanced())
				this.border.selectContainerPane("Script");
			else
				this.border.selectContainerPane("Link");
			
			
			var toolbar = new Toolbar({});
			toolbar.addChild(new Button({
				label:"Add",
				onClick:lang.hitch(this, "newCriteria")
			}));
			toolbar.addChild(new CheckBox({
				selected:link.isAdvanced(),
				onClick:lang.hitch(this, "toggleScript")
			}));
			toolbar.addChild(new ToolbarLabel({
				label:"Scripted criteria",
				style:"padding-right:5px"
			}));
			
			toolbar.addChild(new CheckBox({
				selected:link.isMatchAny(),
				onClick:lang.hitch(this, "toggleMatchAny")
			}));
			toolbar.addChild(new ToolbarLabel({
				label:"Match any"
			}));
			this.border.setTop(toolbar, {style:"margin:0; padding:0; width:100%;"});
		}
	
	});
});
