/**
 * This widget contains a form to select an attribute based on context (e.g. endpoint, target SDS, attribute map).
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
    "dojo/store/Memory",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/form/Select",
	"dijit/form/Textarea",
	"dijit/form/ValidationTextBox",
	"idx/form/Link",
	"idx/dialogs",
	"tdi/tdiutil",
	"tdi/JavascriptEditor",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSSelectAttribute.html"
], function(declare, array, lang, Memory, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		Button, CheckBox, Select, Textarea, ValidationTextBox, Link, idx, tdiutil, JavascriptEditor, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		

		_setValueAttr: function(value) {
			this._attribute.set("value", value);
		},
		
		_getValueAttr: function() {
			return this._attribute.get("value");
		},
		

		populateAttributes: function() {
			var store = new Memory({
				idProperty:"id",
				data: []
			});
			array.forEach(this.config, function(key) {
				store.put({id:key, name:key, label:key});
			});
			this._attribute.set("store", store);
		},
		
		validate: function() {
			var value = this._attribute.get("value");
			var re = this._attribute.get("regExp");
			var regex = new RegExp(re);
			return value.match(regex);
		},
		
		startup: function() {
			this.inherited(arguments);
			this.populateAttributes();
			this._attribute.isValid = lang.hitch(this, "validate");
		}
	});
});
