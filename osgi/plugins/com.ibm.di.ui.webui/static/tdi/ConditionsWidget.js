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
	"tdi/ConditionItem",
	"tdi/orion/OrionEditor",
	"tdi/ToolbarLabel",
	"idx/layout/HeaderPane"
], function(declare, lang, array, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, Toolbar, Border, ConditionItem, 
		JavascriptEditor, ToolbarLabel, HeaderPane) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : "<div style='margin:0; padding:0; min-height:50px; width:100%; height:100%'><div dojoType='tdi/aleditor/Border' containerType='stack' style='margin:0; padding:0; width:100%; height:100%' data-dojo-attach-point='border'</div>",
		
		resize: function(obj) {
			if(this.border)
				this.border.resize(obj);
		},
		
		newCondition: function() {
			var lc = this.config.getBranchConfig().getConditions().newCondition();
			lc.leftHand = "(attribute)";
			lc.operator = "equal to";
			lc.rightHand = "(value)";
			this.appendCondition(lc);
		},
		
		appendCondition: function(lc) {
			var lci = new ConditionItem({config:lc}).placeAt(this.linkDiv);
			lci.onDelete = lang.hitch(this, function() {
				lci.destroyRecursive();
				this.border.resize();
				this.config.getBranchConfig().getConditions().deleteCondition(lci.config);
			});
			lci.startup();
			this.connect(lci, "onChange", lang.hitch(this, function(data) {
				this.config.setModified(true);
			}));
		},
		
		showScript: function() {
			if(!this.javascript) {
				this.javascript = new JavascriptEditor({config:this.config.getBranchConfig(), autoUpdate:true});
				var hp = new HeaderPane({title:"Script Condition", content:this.javascript, style:"width:100%; height:100%; margin:0; padding:0"});
				this.border.setBottom(hp, {style:"height:40%; margin:0; padding:0", splitter:true});
			}
		},
		
		postCreate: function() {
			var bc = this.config.getBranchConfig();
			this.linkDiv = html.create("div", {style:"margin:0; padding:0; width:100%; height:100%"});
			array.forEach(bc.getConditions().getItems(), function(item) {
				this.appendCondition(item);
			}, this);
			this.border.addContainerPane(this.linkDiv, {title:"Conditions"});
			this.border.selectContainerPane("Conditions");
			
			if(bc.getScript() && bc.getScript().length > 0) {
				this.showScript();
			}
		}
	
	});
});
