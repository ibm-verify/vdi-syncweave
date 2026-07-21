/**
 * LDSRunOptions
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSRunOptions.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		
		getValue: function() {
			return this.Form.get("value");
		},
		
		postCreate: function() {
		}
	})
})