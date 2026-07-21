/**
 * The ActivityLoop edits the work/loop attributes of an AttributeLoop object.
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/ComboBox",
	"dijit/form/TextBox",
	"idx/layout/HeaderPane",
	"tdi/NlsMixin",
	"dojo/text!./templates/AttributeLoop.html"
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ComboBox, TextBox, HeaderPane, NlsMixin, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString : template,
		
		constructor: function(args) {
			if(args)
				declare.safeMixin(this, args);
			declare.safeMixin(this, new NlsMixin());
		},
		
		updateConfig: function() {
			this.config.setWorkAttributeName(this.workAttr.get("value"));
			this.config.setLoopAttributeName(this.loopAttr.get("value"));
		},
		
		postCreate: function() {
			this.workAttr.set("value", this.config.getWorkAttributeName());
			this.loopAttr.set("value", this.config.getLoopAttributeName());
		}
	})
});
		
