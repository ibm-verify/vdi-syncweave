/**
 * LDSPta
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/form/_FormWidget",
	"dijit/form/TextBox",
	"dijit/Tree",
	"tdi/model/ConfigTreeStore",
	"dijit/tree/ObjectStoreModel",
	"tdi/NlsMixin",
	"dojo/text!./templates/ConfigBrowser.html"
], function(declare, array, lang, _FormWidget, TextBox, Tree, ConfigTreeStore, ObjectStoreModel, nls, template) {
return declare(
	[_FormWidget, nls],
	{
		templateString: template,
		baseClass: "dijitTextBox",

		resize: function(obj) {
			if(obj && obj.h && this.tree) {
				this.tree.resize(obj);
			}
		},
		
		createConfigTree: function() {
			var t = this;
			
			this.store = new ConfigTreeStore({
			});
		    var model = new ObjectStoreModel({
		        store: this.store,
		        query:{},
		        mayHaveChildren: function(object) {
		        	return (object && object.type != "assemblyline");
		        }
		    });
			this.tree = new Tree({
				model:model,
				showRoot:false,
				style:"height:300px; width:98%",
				getIconClass: function(/*dojo.store.Item*/ item, /*Boolean*/ opened){
				    return !item || item.type!="assemblyline" ? (opened ? "dijitFolderOpened" : "dijitFolderClosed") : "tdiAssemblyLineImage"
				},
			})
			this.own(this.tree.on("click", function(item, node, event) {
				if(item && item.name) {
					t.selectedConfig = item.config ? item.config : item.name;
					if(item.type == "assemblyline") {
						t.set("value", item.config + ":/AssemblyLines/" + item.name);
						t.onChange(t.get("value"));
						t.onExecute(true);
					}
				}
			}));
			this.tree.placeAt(this.treenode);
			this.tree.startup();
		},
		
		_getValueAttr: function() {
			return this.textbox.value;
			this.inherited(arguments);
		},
		
		_setValueAttr: function(value) {
			this.inherited(arguments);
			this.textbox.value = value;
			if(!this.tree) {
				this.createConfigTree();
			}
		},
		
		onExecute: function() {},
		
		onChange: function(value) {},
		
		isLoaded: function() {
			return this.tree;
		},
		
		startup: function() {
			this.inherited(arguments);
		}
	})
});