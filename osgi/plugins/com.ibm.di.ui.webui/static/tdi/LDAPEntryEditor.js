/**
 * LDAPEntryEditor
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/Deferred",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"idx/dialogs",
	"idx/form/buttons",
	"idx/form/Link",
	"./LDAPAttributeEditor",
	"./tdiutil",
	"./NlsMixin",
	"dojo/text!./templates/LDAPEntryEditor.html"
], function(declare, array, lang, html, Deferred, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, Textarea, TextBox, idx, idxButtons, Link, LDAPAttributeEditor, tdiutil, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		// showOperationalAttributes: Boolean
		//		Show operational attributes 
		showOperationalAttributes: false,
		
		// changes: Object
		//		Contains the changed properties
		changes: null,
		
		//
		_editors: null,
		
		onChange: function(prop, value) {
			// summary:
			//		callback function
		},
		
		_updateValue: function(prop, value) {
			if(value.indexOf("\n") != -1)
				value = value.split("\n");
			
			if(this.item[prop] == value)
				return;
			this.item[prop] = value;
			this.changes[prop] = value;
		},
		
		_removeObjectClassFields: function(oc) {
			// summary:
			//		When the object class changes we have to remove
			//		those attributes that are no longer included by other object classes. 
			var t = this;
			this.store.readSchema(oc).then(function(data) {
				var obj = lang.isArray(data) ? data[0] : data;
				array.forEach(obj, lang.hitch(t, "_removeAttributeEditor"));
			});			
		},
		
		_addObjectClassFields: function(oc) {
			// summary:
			//		When the object class changes we have to add
			//		attributes for that object class. 
			var t = this;
			this.store.readSchema(oc).then(function(data) {
				var obj = lang.isArray(data) ? data[0] : data;
				
				// copy object class into schema object
				t.schema[oc] = obj[oc];
				
				// mixin syntax for new attributes
				t.syntax = lang.mixin(obj.__syntax__);
				
				var fields = [];
				var list = obj[oc].MUST;
				if(list) {
					if(typeof(list) == "string")
						list = [list];
					array.forEach(list, function(attr) {
						t.attrRequired[attr.toLowerCase()] = true;
						fields.push(attr.toLowerCase());
					});
				}
				list = obj[oc].MAY;
				if(list) {
					if(typeof(list) == "string")
						list = [list];
					array.forEach(list, function(attr) {
						t.attrRequired[attr.toLowerCase()] = false;
						fields.push(attr.toLowerCase());
					});
				}
				
				array.forEach(fields, function(f) {
					if(!t._editors[f.toLowerCase()]) {
						t._addAttributeEditor(f, {title:oc, backgroundColo:"blue"});
					}
				});
			});
		},
		
		createEditForm: function() {
			var t = this;
			
			// 
			if(t.item["$dn"] == "") {
				t.syntax["dn"] = {
					NAME:"dn",
					DESC:"Unique path to entry in directory"
				};
				t.attrRequired.dn = true;
				t._addAttributeEditor("dn")
			}
			
			array.forEach(this.list.sort(), function(prop) {
				t._addAttributeEditor(prop);
			});
		},
		
		_removeAttributeEditor: function(id) {
			t._editors[prop].destroyRecursive();
			delete t._editors[prop];

		},
		
		_addAttributeEditor: function(prop, style) {
			var t = this;
			var editor = new LDAPAttributeEditor({
				syntax:t.syntax[prop.toLowerCase()],
				tooltip:t.syntax[prop.toLowerCase()].DESC,
				value:t.getProp(prop),
				label:prop,
				attribute:prop,
				onChange:lang.hitch(t, function(value) {
					t.onChange(prop, value);
				}),
				store:t.store,
				onRowAdded:function(source, value) {
					if(source.isObjectClass()) {
						t._addObjectClassFields(value);
					}
				},
				onRowDeleted:function(source, value) {
					if(source.isObjectClass()) {
						t._removeObjectClassFields(value);
					}
				},
				style:style || {}
			});
			if(t.attrRequired[prop]) {
				editor.placeAt(t._required);
			} else {
				editor.placeAt(t._optional);
			}
			t._editors = t._editors || {};
			t._editors[prop] = editor;
		},
		
		getProp: function(name) {
			for(var f in this.item) {
				if(name.toLowerCase() == f.toLowerCase())
					return this.item[f];
			}
			return null;
		},
		
		postCreate: function() {
			this.inherited(arguments);
			this.changes = new Object();
			this.createEditForm();
		}
	})
});