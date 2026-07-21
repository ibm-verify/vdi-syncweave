define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/html",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/CheckBox",
	"tdi/JavascriptEditor",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"idx/dialogs",
	"dojo/text!./templates/AttributeMapItem2.html"
], function(declare, array, html, lang, Widget, TemplatedMixin, WidgetsInTemplate, CheckBox, JavascriptEditor, tdiconfig, tdiutil,
		NlsMixin, idx, template) {

return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, NlsMixin],	
	{
		templateString: template,
		
		_onSimpleChange: function(event) {
			html.style(this.Combo.domNode, "display", event ? "" : "none");
			html.style(this.JScript.domNode, "display", event ? "none" : "");
		},
		
		postCreate: function() {
			if(this.config) {
				this.Target.innerHTML = this.config.getName();
				this.JScript.setConfig(this.config);
			}
		}
	})
});
