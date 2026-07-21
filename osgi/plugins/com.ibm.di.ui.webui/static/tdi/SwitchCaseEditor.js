define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/_base/html",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "dijit/form/Button",
   "dojo/text!./templates/SwitchCaseEditor.html"
], function(declare, array, lang, html, Widget, TemplatedMixin, WidgetsInTemplateMixin, Button, template) {
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
			
			getCondition: function() {
				var conditions = this.config.getBranchConfig().getConditions();
				var condition = null;
				if(conditions.getConditionCount() == 0) {
					condition = conditions.newCondition();
				} else {
					condition = conditions.getCondition(0);
				}
				return condition;
			},
			
			updateConfig: function(value) {
				var conditions = this.config.getBranchConfig().getConditions();
				var condition = this.getCondition();
				if(condition.rightHand != value) {
					condition.rightHand = value;
					conditions.setModified(true);
				}
			},
			
			postCreate: function() {
				var cfg = this.config.getBranchConfig();
				if(cfg.getBranchType() == 'Switch') {
					html.style(this._switch, "display", "");
				} else {
					html.style(this._case, "display", "");
				}
				this._value.set("value", this.getCondition().rightHand);
			}
		}
	);
});