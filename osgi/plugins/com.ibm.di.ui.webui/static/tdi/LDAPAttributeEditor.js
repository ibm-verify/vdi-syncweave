/**
 * LDAPAttributeEditor
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/dom-construct",
	"dojo/Deferred",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/ProgressBar",
	"dijit/Tree",
	"dijit/form/Button",
	"dijit/form/Textarea",
	"dijit/form/ValidationTextBox",
	"tdi/model/LDAPTreeStore",
	"dijit/tree/ObjectStoreModel",
	"idx/dialogs",
	"idx/form/Link",
	"idx/grid/PropertyGrid",
	"idx/grid/PropertyFormatter",
	"idx/widget/EditController",
	"tdi/forms/_FormWidgetMixinFunctions",
	"tdi/tdiutil",
	"tdi/TableWidget",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDAPAttributeEditor.html"
], function(declare, array, lang, html, domConstruct, Deferred, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ProgressBar, Tree, Button, TextArea, TextBox, LDAPTreeStore, ObjectStoreModel, idx, Link, PropertyGrid, PropertyFormatter, EditController, FormFunctions, tdiutil, TableWidget, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		fields: [],
		
		value: "",
		
		title: "",
		
		tooltip: "",
		
		// schema: Object
		//		Start in edit mode
		/*
		      "description" : {
		        "SYNTAX" : "1.3.6.1.4.1.1466.115.121.1.15",
		        "SUBSTR" : "caseIgnoreSubstringsMatch",
		        "SyntaxString" : "Directory String",
		        "USAGE" : "userApplications",
		        "EQUALITY" : "caseIgnoreMatch",
		        "NUMERICOID" : "2.5.4.13",
		        "NAME" : "description",
		        "X-SCHEMA" : "core",
		        "DESC" : "RFC2256: descriptive information"
		      },
		 * 
		 */
		schema: {
			"SYNTAX":"1.3.6.1.4.1.1466.115.121.1.15"		
		},
		
		_valueEditors: null,
		
		_editorRows: null,
		
		_setValueAttr: function(value) {
			this.inherited(arguments);
			this.value = value;
			this._setvalue = true;
			if(!lang.isArray(this.value))
				this.value = [this.value];
			
			var t = this;
			array.forEach(this.value, function(val, index) {
				var editor = t._getEditor(index, true);
				if(editor) {
					editor.set("value", val);
				}
			});
			this._setvalue = false;
		},
		
		_getValueAttr: function() {
			return this.value;
		},
		
		_updateValue: function() {
			if(this._setvalue)
				return;
			var newvalue = [];
			array.forEach(this._valueEditors, function(ed) {
				var val = ed.get("value");
				if(val && val.length)
					newvalue.push(val);
			}, this);
			
			this.value.sort();
			newvalue.sort();
			var haschanges = this.value.length != newvalue.length;
			if(!haschanges) {
				for(var i = 0; i < this.value.length; i++) {
					if(this.value[i] != newvalue[i]) {
						haschanges = true;
					}
				}
			}

			if(haschanges) {
				this.value = newvalue;
				this.onChange(this.value);
			}
		},
		
		_addEditor: function(value) {
			this._valueEditors = this._valueEditors || new Array();
			var editor = new TextBox({
				style:'width:100%; margin-top:3px; margin-bottom:3px',
				value:value?value:"",
				intermediateChanges:true
			});
			this.own(editor.watch("value", lang.hitch(this, "_updateValue")));
			
			var tr = html.create("tr", {}, this._editorTable);
			html.create("td", {}, tr);
			var td = html.create("td", {}, tr);
			this._valueEditors.push(editor);
			editor.placeAt(td);
			td = html.create("td", {}, tr);
			var button = new Button({
				label:"-"
			}).placeAt(td);
			button.onClick = lang.hitch(this, "_onDeleteRow", editor.id);

			
			this._editorRows = this._editorRows || {};
			this._editorRows[editor.id] = tr;
			
			this.onRowAdded(this, value);
			if(value)
				this._updateValue();
			return editor;
		},
		
		_onAddRow: function(evt) {
			var t = this;
			if(t.isObjectClass()) {
				t.store.readSchema("*").then(function(data) {
					tdiutil.selectFromTable(data.objectclasses.sort(), t.syntax.DESC, function(value) {
						t._addEditor(value);
					});
				});
			} else if(t.attribute == "dn") {
				FormFunctions._selectLDAPSearchBase(t.store.ldap, "dn", t, t.syntax.DESC);
			} else {
				t._addEditor();
			}
		},
		
		_setDnAttr: function(value) {
			// This is called from FormFunctions._selectLDAPSearchBase
			// when the editor is editing a "dn" attribute
			this._getEditor(0, true).set("value", value);
		},
		
		_getDnAttr: function() {
			// This is called from FormFunctions._selectLDAPSearchBase
			// when the editor is editing a "dn" attribute
			return this._getEditor(0, true).get("value");
		},
		
		_onDeleteRow: function(id, evt) {
			var delIndex = -1;
			var arr = array.filter(this._valueEditors, function(ed, index) {
				if(ed.id == id)
					delIndex = index;
				return ed.id == id;
			});
			if(arr.length > 0) {
				domConstruct.destroy(this._editorRows[id]);
				var deleted = this._valueEditors.splice(delIndex, 1);
				this.onRowDeleted(this, deleted);
				this._updateValue();
			}
		},
		
		_getEditor: function(index, create) {
			this._valueEditors = this._valueEditors || [this._editor];
			var editor = null;
			do {
				editor = index < this._valueEditors.length ? this._valueEditors[index] : null;
				if(!editor && create) {
					this._addEditor();
				}
			} while(!editor && create);
			return editor;
		},
		
		isObjectClass: function() {
			// summary:
			//		Returns true if this editor edits the objectClass attribute
			var p = /objectclass/i;
			return p.test(this.syntax.NAME);
		},
		
		onChange: function(value) {
			// summary:
			//		Callback on change
		},
		
		onRowAdded: function(source, value) {
			// summary:
			//		Callback on change
		},
		
		onRowDeleted: function(source, value) {
			// summary:
			//		Callback on change
		},
		
		focus: function() {
			var editor = this._getEditor(0);
			if(editor && lang.isFunction(editor.focus)) {
				editor.focus();
			}
		},
		
		setSyntax: function(syntax) {
			this.syntax = syntax;
		},
		
		startup: function() {
			this.inherited(arguments);
			array.forEach(this._valueEditors, function(txt) {
				txt.startup();
			});
		},
		
		postMixinProperties: function() {
			this.inherited(arguments);
			if(this.tooltip)
				this.title = this.tooltip;
		},
		
		postCreate: function() {
			this.inherited(arguments);
			this.own(this._editor.watch("value", lang.hitch(this, "_updateValue")));
	}
	})
});