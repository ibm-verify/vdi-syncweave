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
	"dojo/text!./templates/ALScriptTooltip.html"
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Link, ALTooltip, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ALTooltip],
	{
		templateString : template,
		
		postMixInProperties: function() {
			this.title = this.config.getName();
		}
	
	});
});
