define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/html",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/_Container",
	"dijit/_HasDropDown",
	"dijit/Dialog",
	"dijit/Menu",
	"dijit/MenuItem",
	"dijit/Toolbar",
	"dijit/ToolbarSeparator",
	"dijit/form/Button",
	"tdi/JavascriptEditor",
	"dijit/form/TextBox",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"tdi/AttributeMapItem2",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/tdientry",
	"tdi/NlsMixin",
	"tdi/TableWidget",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"dojo/text!./templates/AttributeMap2.html"
], function(declare, array, html, lang, Widget, TemplatedMixin, WidgetsInTemplate, Container, DropDown, Dialog, Menu, MenuItem, Toolbar, Separator, Button,
		JavascriptEditor, TextBox, BorderContainer, ContentPane, AttributeMapItem2, tdiconfig, tdiutil, tdientry,
		tdiNlsMixin, TableWidget, idx, HeaderPane, template) {

return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, tdiNlsMixin],	
	{
		templateString: template,

		// config: tdi.connector
		// 		The connector config
		config : null,
		
		// attribute map object
		attributeMapConfig: null,
	
		// input: boolean
		// 		Shows the input map (otherwise the output map)
		input : false,
	
		// editor:
		// 		The tooltip editor
		editor : null,

		// showToolbar: boolean
		//		Show toolbar on/off
		showToolbar: true,
		
		_editors: new Array(),
		

		_newAttribute : function(e) {
			
			var until = this.config ? this.config.getName() : this.attributeMapConfig.getParent().getName();
			var map = this.config ? this.config.getAttributeMap(this.input) : this.attributeMapConfig;
			var store = this.DG.getStore();
			tdiutil.createNewAttribute(map, until, store, lang.hitch(this, function(newitem) {
				this.DG.reloadGrid();
			}));
		},
		
		_deleteAttribute : function(e) {
			var names = this.DG.getSelectedRows();
			if(names.length == 0)
				return;
			
			if(tdiutil.confirm(this.getString("general.delete.label") + "\n" + names.join("\n"), lang.hitch(this, function(names, buttonId) {
				if(buttonId == 0) {
					var map = this.attributeMapConfig;
					dojo.forEach(names, function(str) {
						map.removeItem(str);
					});
					var parent = this.attributeMapConfig.getParentConfigType("tdi.connector");
					if(parent) {
						var schema = parent.getSchema(this.input);
						dojo.forEach(names, function(str) {
							schema.removeItem(str);
						});
					}
					var store = this.DG.getStore();
					dojo.forEach(this.DG.getSelectedRows(), function(item) {
						store.deleteItem(item);
					});
					store.save();
				}
			}, names)));
			var map = this.attributeMapConfig;
		},
		
		getMapAttributes: function() {
			var map = this.attributeMapConfig;
			var data = [];
			if (map != null) {
				 dojo.forEach(map.getNames(), lang.hitch(this, function(m) {
					 var item = map.getItem(m);
					 var value = item.isSimple() ? item.getSimple() : item.getAdvanced();
					 data.push({id:item.getName(), name:item.getName(), mapping:value, value:""});
				 }));
			}
			
			if(this.config) {
				var schema = this.config.getSchema(this.input);
				dojo.forEach(schema.getNames(), lang.hitch(this, function(name) {
					if(!map.isMapped(name)) {
						try {						
							data.push({id:name, name:name, mapping:"", value:""});
						} catch(err) {
							console.log("AttributeMap: " + name + "; " + err);
						}
					}
				}));
			}
			return data;
		},
		
		populateTable : function() {
			// source connector providing input attributes 
			var source = this.config.getConnector("Input");
			
			// Target connector we are modifying 
			var conn = this.config.getConnector("Output");
			
			// Modify the output map to reflect what the
			// called assemblyline expects in terms of input
			var map = conn.getAttributeMap(false);
			
			// Target AssemblyLine
			var alname = conn.getConnectionConfig().getParam("assemblyLine");
			var alconfig = null;
			var targetAttrs = null;
			
			if(alname) {
				alconfig = this.config.getTop().getAssemblyLine(alname);
			}
			
			if(alconfig) {
				var ops = array.filter(alconfig.getOperationNames(), function(name) {
					return name != "Default";
				});
				
				array.forEach(ops, function(op) {
					array.forEach(alconfig.getOperation(op).getSchema(true).getNames(), function(attr) {
						var item = map.getItem(attr);
						if(!item) {
							map.newItem({name:attr, mapsTo:attr, value:"// Used for " + op + " objects"});
						}
					});
				});
			}
			this.rebuildAttMaps();
		},
		
		rebuildAttMaps: function() {
			array.forEach(this._editors, function(ed) {
				ed.destroyRecursive();
			});
			
			// Target connector we are modifying 
			var conn = this.config.getConnector("Output");
			
			// Modify the output map to reflect what the
			// called assemblyline expects in terms of input
			var map = conn.getAttributeMap(false);
			
			var t = this;
			array.forEach(map.getNames(), function(attr) {
				var item = map.getItem(attr);
				var ed = new AttributeMapItem2({config:item});
				ed.placeAt(t.TableBody, "last")
				t._editors.push(ed);
			});
			
		},
		
		resize : function(obj) {
			if(this.BorderPane != null)
				this.BorderPane.resize(obj);
		},
		
		setConfig: function(config, input) {
			this.config = config;
			this.populateTable();
		},
		
		postCreate : function() {
			this._editors = new Array();
			if(this.config) {
				this.setConfig(this.config, this.input);
			}
		}
	})
});