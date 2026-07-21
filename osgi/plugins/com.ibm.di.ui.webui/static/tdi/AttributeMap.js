define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/html",
	"dojo/_base/lang",
	"dojo/store/Memory",
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
	"dijit/form/ComboBox",
	"tdi/JavascriptEditor",
	"dijit/form/TextBox",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/tdientry",
	"tdi/NlsMixin",
	"tdi/TableWidget",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"dojo/text!./templates/AttributeMap.html"
], function(declare, array, html, lang, Memory, Widget, TemplatedMixin, WidgetsInTemplate, Container, DropDown, Dialog, Menu, MenuItem, Toolbar, Separator, Button,
		ComboBox, JavascriptEditor, TextBox, BorderContainer, ContentPane, tdiconfig, tdiutil, tdientry,
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
		
	  	// columns:
	  	//		The columns to include in the table (object where props are identifiers)
	  	columns: {
	  		"name": true, 
	  		"mapping": true,
	  		"value": true
	  	},
	  	
		onReadNext : function(attributemap) {
			// summary:
			// 		Called when user presses read-next button
		},
		
		onCloseConnection : function(attributemap) {
			// summary:
			// 		Called when user presses close-connection button
		},
		
		showEODMsg : function(show) {
			if(show)
				idx.info(this.eodmsg || "End of data.");
		},
		
		showProgressBar : function(show) {
			if(show)
				idx.showProgressDialog('Reading data...');
			else
				idx.hideProgressDialog();
		},
		
		_mapSelectedAttributes : function(unmap, e) {
			var arr = this.DG.getSelectedRows();
			var map = this.attributeMapConfig;
			dojo.forEach(arr, lang.hitch(this, function(item) {
				var name = this.DG.getStore().getValue(item, "name");
				if(!map.isMapped(name) && !unmap) {
					var ami = map.newItem({name:name});
					this.DG.getStore().setValue(item, "mapping", ami.getMapsTo());
				} else if(map.isMapped(name) && unmap) {
					map.removeItem(name);
					this.DG.getStore().setValue(item, "mapping", null);
				}
			}));
		},
	
		_showEditAttributeForm: function(grid, rowId, detailNode, rendered) {
			var ami = this.attributeMapConfig.getItem(rowId);
			if(!ami)
				ami = this.attributeMapConfig.newItem({name:rowId})
			var value = "";
			value = ami.getMapsTo();
			if(ami == null || ami.isSimple())
				value = this.input ? "conn." + value : "work." + value;
			
			var js = new JavascriptEditor({value:value, autoUpdate:true, config:ami, style:"padding:0; margin:0; width:100%; height:250px"}).placeAt(detailNode);
			js.resize();
			rendered.callback();
			return rendered;
		},
		
		_editAttribute : function(e) {
			// summary:
			// 		Opens the attribute map editor for the specified attmap item
			// div: tdi.AttributeMapItem
			// 		The attribute map item config
			// input: boolean
			// 		True if the map is from the input connector
			var arr = this.DG.getSelectedRows();
			if(arr.length == 0)
				return;
			
			var attr = arr[0];
			var map = this.attributeMapConfig;
			var ami = map.getItem(attr);
			if(!ami)
				ami = this.attributeMapConfig.newItem({name:attr})
			var value = attr;
			if(ami != null)
				value = ami.getMapsTo();
			if(ami == null || ami.isSimple())
				value = this.input ? "conn." + value : "work." + value;
	
			var text = new JavascriptEditor({value:value, autoUpdate:true, config:ami, style:"padding:0; margin:0"});
			
			var hp = new HeaderPane({
				title:attr,
				content:text,
				style:"height:100%; width:100%; margin:0; padding:0"
			});
			
			this.Editor.set("content", hp);
			html.style(this.Editor, "height", "35%");
			this.BorderPane.layout();
		},
	
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
		
		_readNext : function() {
			this.onReadNext(this);
		},
		
		_closeConnection : function() {
			this.onCloseConnection(this);
		},
		
		setEntry : function(entry) {
			var table = this.DG;
			var schema = this.config ? this.config.getSchema(this.input) : null;
			var e2 = new tdientry({data:entry});
			if(schema) {
				dojo.forEach(e2.getNames(), function(attr) {
					if(schema.getItem(attr) == null) {
						schema.newItem({name:attr});
					}
				});
			}
			
			var i;
			for(i = 0; i < this.DG.grid.rowCount(); i++) {
				var id = this.DG.grid.rows()[i].id;
				var item = table.getItem(id);
				var value = e2.getAttributeValue(id, "<br>");
				if(value) {
					table.setItemValue(item, "value", value);
					e2.removeAttribute(id);
				} else {
					table.setItemValue(item, "value", "");
				}
			}				
			
			dojo.forEach(e2.getNames(), function(attr) {
				var value = e2.getAttributeValue(attr);
				table.addItem({id:attr, name:attr, value:value, mapping:""});
			});
			
//			this.DG.reloadGrid();
		},
		
		getStore : function() {
			return this.store;
		},
		
		getMapAttributes: function() {
			var map = this.attributeMapConfig;
			var data = [];
			var keys = {};
			if (map != null) {
				 dojo.forEach(map.getNames(), lang.hitch(this, function(m) {
					 var item = map.getItem(m);
					 var value = item.isSimple() ? item.getSimple() : item.getAdvanced();
					 data.push({id:item.getName(), name:item.getName(), mapping:value, value:""});
					 keys[item.getName()] = true;
				 }));
			}
			
			if(this.config) {
				var schema = this.config.getSchema(this.input);
				dojo.forEach(schema.getNames(), lang.hitch(this, function(name) {
					if(!keys[name]) {
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
		
		getInputAttributes: function() {
			var data = new Array();
			if(this.input) {
				array.forEach(this.config.getSchema(this.input).getItemNames(), function(name) {
					data.push({
						id:name,
						label:name
					});
				});
			} else {
				array.forEach(this.config.getAssemblyLine().getWorkAttributes(), function(name) {
					data.push({
						id:name,
						label:name
					});
				});
			}
			if(data.length == 0) {
				data.push({id:"*", label:"*"});
			}
			return new Memory({
				data:data
			});
		},
		
		populateTable : function() {
			var data = this.getMapAttributes();
			if(this.DG) {
				this.DG.setData(data);
			} else {
				this.createTable(data);
			}
		},
		
		createTable: function(data) {
			
			var columns = this.columns;
			var structure = [
	  			{field:"name", id:"name", name:this.getString("attributeName"), width:"100px", editable:true, editor:TextBox},
	  			{field:"mapping", id:"mapping", name:this.getString("assignment"), width:"100px", 
	  				editable:true, editor:ComboBox, editorArgs:{props:"searchAttr:\"label\""}},
	  			{field:"value", id:"value", name:this.getString("value"), width:"auto"}
			];

			var struct = array.filter(structure, function(item) {
				return columns[item.id];
			});
			
			this.DG = new TableWidget({
				idProperty: "name",
				detailsOnDemand:false,
				structure:struct,
				style:"width:100%; height:100%; margin:0; padding:0",
			  	onRowClick:lang.hitch(this, "_editAttribute"),
			  	detailProvider:lang.hitch(this, "_showEditAttributeForm")
			});
			this.DG.createGrid(data);
			this.DG.startup();
			this.DGDiv.set("content", this.DG);
	
			this.DG.grid.edit.onBegin = lang.hitch(this, function(cell) {
				cell.editor().set("store", this.getInputAttributes());
			});
			
			var pos = 1;
	
			if(this.showToolbar) {
				this.toolbar.addChild(new Button({
					label:this.getString("newItem"),
					onClick:lang.hitch(this, "_newAttribute")
				}), pos++, 1);
				
				if(this.config) {
					this.btnRead = new Button({
						label:this.getString("readNext"),
						onClick:lang.hitch(this, "_readNext")
					});
					this.toolbar.addChild(this.btnRead, pos++, 1);
					
					this.btnClose = new Button({
						label:this.getString("closeConnection"),
						onClick:lang.hitch(this, "_closeConnection")
					});
					this.toolbar.addChild(this.btnClose, pos++, 1);
					
					this.btnClose.set("disabled", true);
				}
			}
					
			this.DG.addToRowMenu(new MenuItem({
				label:this.getString("general.delete.label"),
				onClick:lang.hitch(this, "_deleteAttribute")
			}));
			
			this.DG.addToRowMenu(new MenuItem({
				label:this.getString("WebCE.unmapAttribute"),
				onClick:lang.hitch(this, "_mapSelectedAttributes", true)
			}));
			
			this.DG.addToRowMenu(new MenuItem({
				label:this.getString("action.label.3"),
				onClick:lang.hitch(this, "_mapSelectedAttributes", false)
			}));
			
			this.DG.addToRowMenu(new MenuItem({
				label:this.getString("TaskCallParam.cmdEdit.name"),
				onClick:lang.hitch(this, "_editAttribute", false)
			}));
//			
//			//
//			// -- Style rows italic for attributes that are not mapped
//			//
//			dojo.connect(this.DG.grid, "onStyleRow", lang.hitch(this, function(row) {
//				var item = this.DG.grid.getItem(row.index);
//				var mapping = this.DG.getStore().getValue(item, "mapping");
//				var updated = this.DG.getStore().getValue(item, "updated");
//				if(mapping == null)
//					row.customStyles += ";font-style:italic";
//				if(updated)
//					row.customStyles += ";font-color:blue";
//			}));
			
		},
		
		resize : function(obj) {
			if(this.BorderPane != null)
				this.BorderPane.resize(obj);
			
			if(obj && obj.h > 0 && this.DG) {
				this.DG.resize(obj);
			}
		},
		
		enableReadNext : function(enable) {
			if(this.config && this.btnRead) {
				this.btnRead.set("disabled", !enable);
			}
		},
		
		enableClose : function(enable) {
			if(this.config && this.btnClose) {
				this.btnClose.set("disabled", !enable);
			}
		},
		
		setConfig: function(config, input) {
			this.config = config;
			this.input = input;
			this.attributeMapConfig = this.config.getAttributeMap(this.input);
			this.populateTable();
		},
		
		postCreate : function() {
			if(this.config) {
				this.setConfig(this.config, this.input);
			}
			this.enableClose(false);
		}
	})
});