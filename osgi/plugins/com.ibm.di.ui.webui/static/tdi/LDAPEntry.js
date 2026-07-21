/**
 * LDAPEntry
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
	"dijit/ProgressBar",
	"dijit/Tree",
	"dijit/form/Button",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"./model/LDAPTreeStore",
	"dijit/tree/ObjectStoreModel",
	"idx/dialogs",
	"idx/form/buttons",
	"idx/form/Link",
	"idx/grid/PropertyGrid",
	"idx/grid/PropertyFormatter",
	"idx/widget/EditController",
	"./tdiutil",
	"./TableWidget",
	"./LDAPEntryEditor",
	"./NlsMixin",
	"dojo/text!./templates/LDAPEntry.html"
], function(declare, array, lang, html, Deferred, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ProgressBar, Tree, Button, TextArea, Textbox, LDAPTreeStore, ObjectStoreModel, idx, idxButtons, Link, PropertyGrid, PropertyFormatter, EditController, tdiutil, TableWidget, LDAPEntryEditor, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		// showAllAttributes: Boolean
		//		Show all attributes - e.g. include attributes with no values
		showAllAttriutes: false,
		
		// showOperationalAttributes: Boolean
		//		Show operational attributes 
		showOperationalAttributes: false,
		
		// changes: Object
		//		Contains the changed properties
		changes: {},
		
		// editMode: Boolean
		//		Start in edit mode 
		editMode: false,
		
		// sortRequiredFirst: Boolean
		//		If true, required attributes sort before optional attributes
		//		otherwise all attributes are sorted equally.
		sortRequiredFirst: false,
		
		
		onSaveComplete: function() {
			// Callback when a save/modify operation completed successfully
			this.changes = {};
			this._saveBtn.set("disabled", true);
			if(this.editMode) {
				this._showAllAttrCB.set("checked", false);
				this._editEntry();
			}
		},
		
		_saveEntry: function() {
			this.onSaveEntry(this.item, this.changes, this);
		},
		
		_deleteEntry: function(pg) {
			this.onDeleteEntry(this.item);
		},
		
		_loginEntry: function() {
			this.onLoginEntry(this.item);
		},
		
		isModified: function() {
			return !this._saveBtn.get("disabled");
		},
		
		onDeleteEntry: function() {
			// summary:
			//		callback function
		},

		onSaveEntry: function() {
			// summary:
			//		callback function
		},
		
		onLoginEntry: function() {
			// summary:
			//		callback function
		},
		
		resize: function(obj) {
			if(obj && obj.h) {
				this._border.resize(obj);
			}
		},
		
		_updateValue: function(prop, value) {
			if(value.indexOf("\n") != -1)
				value = value.split("\n");
			
			if(this.item[prop] == value)
				return;
			this.item[prop] = value;
			this.changes[prop] = value;
			this._saveBtn.set("disabled", false);
		},
		
		_buildAttributeList: function(obj) {
			// summary:
			//		Returns a new sorted array based on the input object of
			//		attributes and the showAllAttributes, showOperationalAttributes.
			var arr = [];
			for(var attr in obj) {
				if(this.showAllAttributes || this.item[attr])
					arr.push(attr);
			}
			return arr;
		},

		_buildAttributeObject: function(schema, prop, attrRequired, value) {
			var list = schema[prop];
			if(!list)
				return;
			if(typeof(list) == "string")
				list = [list];
			array.forEach(list, function(attr) {
				attrRequired[attr] = value;
			});
		},
		
		showDetails: function(item, schema) {
			var t = this;
			var arr = [];
			var attrRequired = {};
			
			if(schema && this.editMode) {
				var mayMust = ["MAY", "MUST"];
				for(var f in schema) {
					this._buildAttributeObject(schema[f], "MAY", attrRequired, false);
					this._buildAttributeObject(schema[f], "MUST", attrRequired, true);
				}
				
				// -- Build the array of properties to show
				arr = this._buildAttributeList(attrRequired);
				
			} else {
				// -- Build the array of properties to show
				arr = this._buildAttributeList(this.item);
			}
			
			var content = null;
			if(this.editMode) {
				content = this.createEditForm(item, arr, attrRequired);
			} else {
				content = this.createTable(item, arr, attrRequired);
			}
			this._center.set("content", content);
			this.onLoad();
			this._border.layout();
		},
			
		createEditForm: function(item, arr, attrRequired) {
			return new LDAPEntryEditor({
				item:item,
				list: arr,
				attrRequired:attrRequired,
				schema:this.schema,
				syntax:this.syntax,
				store:this.ldapStore,
				onChange:lang.hitch(this, "_updateValue")
			});
		},
		
		createTable: function(item, arr) {
			var t = this;
			var layout = [
			   {
				   name:t.getString("DataBrowser_20"), // Attribute
				   id:"id",
				   field:"id",
				   width:"25%",
				   decorator: function(value, prop, row) {
					   if(t.editMode && attrRequired[prop])
						   return "<b>"+value+"</b>";
					   if(t.syntax && t.syntax[prop] && t.syntax[prop]) {
						   return "<div alt'" + t.syntax[prop].DESC + "'>" + value + "</div>";
					   }
					   
					   return value;
				   }
			   },
			   {
				   name:t.getString("DataBrowser_21"), // Value
				   id:"value",
				   field:"value",
				   alwaysEditing:this.editMode,
				   decorator:function(value, prop, row) {
					   if(prop == "jpegPhoto")
						   return "<img src='data:image/jpeg;base64," + value + "'>";
					   if(lang.isArray(t.item[prop])) {
						   return t.item[prop].join("<br>");
					   }
					   return value;
				   },
				   editable:true,
				   editorArgs: {
					   fromEditor: function(value, cell) {
						   t._updateValue(cell.row.id, value);
						   return value;
					   },
					   toEditor: function(storeData, gridData, cell, editor) {
						   if(lang.isFunction(editor.setSyntax)) {
							   editor.setSyntax(t.syntax[cell.row.id]);
						   }
						   if(lang.isArray(t.item[cell.row.id]))
							   return t.item[cell.row.id].join("\n");
						   else
							   return storeData;
					   }
				   },
				   editor:"dijit/form/Textarea"
			   }
			];

			// -- Build data array for table
			var data = [];
			array.forEach(arr, function(prop) {
				if(prop != "$dn") {
					var val = item[prop];
					data.push({id:prop, value:val});
				}
			});
			
			var tableOptions = {
				structure:layout,
				toolbar:false
			};
			// -- Sort table on attribute, unless we are editing
			// -- in which case required should appear topmost
			if(!this.sortRequiredFirst) {
				tableOptions.sortInitialOrder = { colId: 'id', descending: false }
			}
			
			this.table = new TableWidget(tableOptions);
			this.table.setData(data);
			this.table.startup();
			return this.table;
		},
		
		onLoad: function() {},
		

		getProp: function(name) {
			for(var f in this.item) {
				if(name.toLowerCase() == f.toLowerCase())
					return this.item[f];
			}
			return null;
		},
		
		getSchema: function() {
			var t = this;
			var oc = this.getProp("objectclass");
			if(lang.isArray(oc))
				oc = oc.join(",");
			
			idx.showProgressDialog(t.getString("FDS.loadingSchema"));
			this.ldapStore.readSchema(oc).then(function(schema) {
				idx.hideProgressDialog();
				if(lang.isArray(schema))
					schema = schema.pop();
				t.schema = schema;
				t.syntax = schema["__syntax__"];
				t.showDetails(t.item, schema);
			}, function(err) {
				idx.hideProgressDialog();
				tdiutil.error(err);
			});
		},
		
		_showEntry: function() {
			if(this.showAllAttributes && !this.schema)
				this.getSchema();
			else
				this.showDetails(this.item, this.schema);
		},
		
		_editEntry: function() {
			this.editMode = !this.editMode;
			// -- no point in showing all attrs unless
			// -- we are editing.
			this.showAllAttributes = this.editMode;
			this.sortRequiredFirst = this.editMode;
			this.getSchema();
		},
		
		_toggleAllAttributes: function(value) {
			this.showAllAttributes = value;
			this._showEntry();
		},

		postCreate: function() {
			this.inherited(arguments);
			this._showAllAttrCB.set("checked", this.showAllAttributes);
			this._showEntry();
		}
	})
});