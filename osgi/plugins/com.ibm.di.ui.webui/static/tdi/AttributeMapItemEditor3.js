/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
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
	"dojo/text!./templates/AttributeMapItemEditor3.html"
], function(declare, array, lang, Memory, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		Button, CheckBox, Select, Textarea, ValidationTextBox, Link, idx, tdiutil, JavascriptEditor, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		// work: boolean
		//		if true generated script expression use "work" instead of "conn"
		work: true,
		
		onDeleteAttribute: function() {
			// summary:
			//		callback to delete this attribute
		},
		
		updateValue: function(item, prop, value) {
			if(item[prop] != value) {
				if(prop == "simple" && value != "") {
					if(item.name == value) {
						delete item.mapsTo;
						item.simple = true;
					} else {
						delete item.simple;
						item.mapsTo = this.getSourceObject() + '[\"' + value + '"]';
					}
				} else if(prop == "addmodify") {
					item.add = (value == "add" || value == "both");
					item.modify = (value == "mod" || value == "both");
				} else if(typeof(value) == "boolean") {
					item[prop] = value;
				} else {
					if(!value || value == "")
						delete item[prop];
					else
						item[prop] = value.trim();
				}
			}
		},
		
		updateFields: function() {
			var simple = this.isSimpleAssignment();
			
			if(simple)
				this._attribute.set("value", this.getSimpleAssignment());
			
			if(this.item.mapsTo)
				this._script.set("value", this.item.mapsTo);
			else if(this.item.subst)
				this._script.set("value", this.item.subst);
			
			this._type.set("value", simple ? "simple" : "script");
			
			if(this.item.enabled == false)
				this._enabled.set("value", false);
			else
				this._enabled.set("value", true);
			
			if(!this.item.add && !this.item.modify) {
				this.item.add=true;
				this.item.modify=true;
			}
			if(this.item.add && this.item.modify)
				this._addmodify.set("value", "both");
			else if(this.item.add)
				this._addmodify.set("value", "add");
			else
				this._addmodify.set("value", "mod");
		},
		
		_onAddMod: function(args, arg2) {
			this.item.add = (args == "add" || args == "both");
			this.item.modify = (args == "mod" || args == "both");
		},
		
		_toggleEnabled: function(enabled) {
			this.updateValue(this.item, "enabled", enabled);
		},
		
		_toggleType: function(value) {
			var simple = (value == "simple");
			this._attribute.set("style", {display: simple ? "" : "none"});
			this._script.set("style", {display: simple ? "none" : ""});
			// -- generate a default script when going from simple to advanced
			if(!simple) {
				if(this.item.simple && !this.item.mapsTo) {
					this.updateValue(this.item, "mapsTo", this.getSourceObject() + '[\"' + this.item.simple + '"]'); 
					this._script.set("value", this.item.mapsTo);
				}
			}
		},
		
		_setValueAttr: function(value) {
			this.item = value;
			this.updateFields();
//			this._attribute.watch("value", lang.hitch(this, "updateValue", "simple"));
//			this._add.watch("checked", lang.hitch(this, "updateValue", "add"));
//			this._modify.watch("checked", lang.hitch(this, "updateValue", "mod"));
//			this._script.watch("value", lang.hitch(this, "updateValue", "script"));
		},
		
		_getValueAttr: function() {
			var item = this.item;
			var type = this._type.get("value");
			this.updateValue(item, "addmodify", this._addmodify.get("value"));
			if(type == "simple") {
				item.type = "Simple";
				this.updateValue(item, "mapsTo", this._attribute.get("value"));
			} else {
				item.type = "Advanced";
				this.updateValue(item, "mapsTo", this._script.get("value"));
			}
			if(this.isSimpleAssignment()) {
				item.assign = this.getSimpleAssignment();
			} else {
				item.assign = item.mapsTo;
			}
			return item;
		},
		
		matchSimple: function(value) {
			if(this.getSourceObject() == "work") {
				return value.match(/^work\["(.*)"\];?$/);
			} else {
				return value.match(/^conn\["(.*)"\];?$/);				
			}
		},
		
		isSimpleAssignment: function() {
			if(this.item.mapsTo == this.item.name) {
				return true;
			} else if(this.item.mapsTo) {
				var arr = this.matchSimple(this.item.mapsTo);
				if(arr && arr.length == 2) {
					return true;
				}
				return false;
			} else {
				return true;
			}
		},
		
		getSimpleAssignment: function() {
			if(this.item.mapsTo) {
				var arr = this.matchSimple(this.item.mapsTo);
				if(arr && arr.length == 2) {
					return arr[1];
				}
				return this.item.mapsTo;
			} else {
				return this.item.name;
			}
		},
		
		getSourceObject: function() {
			// summary:
			//		Returns the source object used in script expressions
			//		Returns "work" if parentMap is for writeback, "conn" otherwise
			var obj = this.get("sourceObject");
			if(!obj && this.parentmap)
				obj = this.parentmap.get("sourceObject");
			
			return obj || "conn";
		},
		
		populateAttributes: function() {
			var arr = this.parentmap.get("sourceAttributes");
			var store = arr;
			if(lang.isArray(arr)) {
				store = new Memory({
					idProperty:"id",
					data: []
				});
				array.forEach(arr, function(key) {
					store.put({id:key, name:key, label:key});
				});
			}
			if(store)
				this._attribute.set("store", store);
		},
		
		setParentMap: function(parentmap) {
			this.parentmap = parentmap;
			if(parentmap) {
				this.populateAttributes();
			}
		},
		
		startup: function() {
			this.inherited(arguments);
			this._script.on("keydown", function(event) {
				if (event.keyCode == dojo.keys.ENTER) {
					// -- processed by textarea
					// -- stop prop to prevent Gridx to close editor
					event.stopPropagation();
				} else if(event.keyCode == dojo.keys.TAB) {
					// -- not handled by anyone but stop it
					// -- to prevent Gridx to close editor
					dojo.stopEvent(event);
				}
			});
			
			//
			// -- for writeback we do not show update options
			if(this.getSourceObject() == "work") {
				this._addmodify.set("style", {display:"none"})				
			}
		}
	});
});
