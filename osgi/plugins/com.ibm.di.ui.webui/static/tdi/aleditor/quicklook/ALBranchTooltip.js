/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/form/Link",
	"./_ALTooltip",
	"dojo/text!./templates/ALBranchTooltip.html",
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Link, ALTooltip, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ALTooltip],
	{
		templateString : template,
		
		getConditionString: function() {
			var conditions = this.config.getBranchConfig().getConditions();
			var arr = new Array();
			for(var i = 0; i < conditions.getConditionCount(); i++) {
				var c = conditions.getCondition(i);
				arr.push(c.leftHand + (c.negate ? " not " : " ") + c.operator + " " + c.rightHand);
			}
			return arr.join("<br>");
		},
		
		postMixInProperties: function() {
			this.title = this.config.getName();
			this.branchType = this.config.getBranchType();
			this.script = this.config.getBranchConfig().getScript() || "";
			if(this.script.length == 0)
				this.scriptDisplay = "none";
			this.conditions = this.getConditionString();
		}
	
	});
});
