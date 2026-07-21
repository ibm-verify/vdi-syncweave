/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
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
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSMapItem.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		Button, CheckBox, Select, Textarea, ValidationTextBox, Link, idx, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		onDeleteAttribute: function() {
			// summary:
			//		callback to delete this attribute
		},
		
		updateValue: function(prop, watchval, oldvalue, value) {
			if(this.item[prop] != value) {
				var item = tdiutil.clone(this.item);
				item[prop] = value;
				// call set to trigger watch handlers
				this.set("item", item);
			}
		},
		
		updateFields: function() {
			this._attribute.set("value", this.item.name);
			if(this.item.script)
				this._assignment.set("value", this.item.script);
			else if(this.item.subst)
				this._assignment.set("value", this.item.subst);
			
			if(!this.item.add && !this.item.mod) {
				this.item.add=true;
				this.item.mod=true;
			}
			if(this.item.add == true)
				this._add.set("value", "on");
			if(this.item.mod)
				this._modify.set("value", "on");
			
		},
		
		startup: function() {
			this.inherited(arguments);
			this.updateFields();
			this._attribute.watch("value", lang.hitch(this, "updateValue", "name"));
			this._add.watch("checked", lang.hitch(this, "updateValue", "add"));
			this._modify.watch("checked", lang.hitch(this, "updateValue", "mod"));
			this._assignment.watch("value", lang.hitch(this, "updateValue", "script"));
		}
	});
});
