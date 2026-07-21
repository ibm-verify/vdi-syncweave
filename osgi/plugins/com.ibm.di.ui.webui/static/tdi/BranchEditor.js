define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/_base/html",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "dijit/form/Button",
   "dijit/form/CheckBox",
   "dijit/form/Select",
   "idx/layout/HeaderPane",
   "tdi/ConditionsWidget",
   "tdi/ToolbarLabel",
   "dojo/text!./templates/BranchEditor.html"
], function(declare, array, lang, html, Widget, TemplatedMixin, WidgetsInTemplateMixin, Button, CheckBox, Select, HeaderPane, ConditionsWidget, ToolbarLabel, template) {
	return declare(
		[Widget, TemplatedMixin, WidgetsInTemplateMixin],
		{
			templateString: template,
			/*
			 * 
			 */
			title: null,
			
			constructor: function(args) {
				declare.safeMixin(this, args);
				if(this.config) {
					this.title = this.title || this.config.getName();
				}
			},
			
			postCreate: function() {
				var conditions = new ConditionsWidget({config:this.config}).placeAt(this.hpane);
				conditions.showScript();
				this.hpane.addChild(new Button({
					label:"Add",
					region:"majorActions",
					onClick:lang.hitch(this, function() {
						conditions.newCondition();
					})
				}));
				if(this.config.isLoop()) {
					html.style(this.branchType.domNode, "display", "none");
				}
			}
		}
	);
});