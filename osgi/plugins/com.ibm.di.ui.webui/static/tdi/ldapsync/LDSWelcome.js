/**
 * LDSWelcome
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"idx/dialogs",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSWelcome.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiapi, tdiconstants, idx, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		
		postCreate: function() {
		}
	})	
})