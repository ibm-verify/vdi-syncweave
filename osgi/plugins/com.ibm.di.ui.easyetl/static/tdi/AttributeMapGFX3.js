dojo.provide("tdi.AttributeMapGFX3");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Dialog");

dojo.require("dojox.gfx");
dojo.require("dojox.gfx.move");
dojo.require("dojox.gfx.utils");
dojo.require("dojox.widget.Dialog");

dojo.require("dojo.dnd.Source");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.TreeTableWidget");
dojo.require("tdi.AttributeMapItem");
dojo.require("tdi.AttributeMapItemEditor");
dojo.require("tdi.ConnectorEditor");
dojo.require("tdi.SelectComponent");
dojo.require("tdi.Toolbar");

dojo.declare("tdi.AttributeMapGFX3",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		widgetsInTemplate: true,
		templateString: "<div dojoAttachPoint='Main'></div>",
		
		// input: tdi.connector
		//		Input connector configuration
		input : null,
		
		// output: tdi.connector
		//		Output connector configuration
		output : null,

		// showSchema: boolean
		//		If true also show unmapped items (e.g. schema items)
		//		in the mapping area.
		showSchema : true,
		
		// 
		lastScrollOffset : -1,
		
		constructor : function(args) {
			this.inherited("constructor", args);
			// console.log("AttributeMapGFX created");
		},
		

		_createSortedArray : function(map, schema, includeschema) {
			// summary:
			//		Creates a sorted array of map and schema names
			// description:
			//		An array consisting of map and schema item names is
			// 		built from the map and schema. The array has all the map
			//		items appear first and then any unmapped schema items.
			// map:	tdi.attmapconfig
			//		The attribute map
			// schema: tdi.schemaconfig
			//		The schema
			// includeSchema: booelan
			//		If true, schema items are included
			// returns: Array
			//		Sorted array of names from map and schema
			var arr = new Array();
			var arrlc = new Array();
			dojo.forEach(map.getNames(), dojo.hitch(this, function(str) {
				arr.push(str);
				arrlc.push(str.toLowerCase())
			}));
			arr.sort(this.caseInsensitiveSort);
			
			var arr2 = new Array();
			if(includeschema) {
				dojo.forEach(schema.getNames(), dojo.hitch(this, function(str) {
					if(arrlc[str.toLowerCase()] == null) {
						if(!map.isMapped(str)) {
							arr2.push(str);
						}
					}
				}));
				arr2.sort(this.caseInsensitiveSort);
			}

			// Append schema items to the map array
			return arr.concat(arr2);
		},
		
		caseInsensitiveSort : function(a, b) {
			var a1 = a.toLowerCase();
			var b1 = b.toLowerCase();
			if(a1 < b1)
				return -1;
			else if(a1 == b1)
				return 0;
			else
				return 1;
		},
		
		setInputOutputEntry : function(inp, out) {
			// summary:
			//		Updates the UI with values for the input and output connector
			// description:
			//		The values for inp and out are shown in the
			//		respective UI control for the connector. A null value for either
			//		inp or out will reset the UI control to its default state.
			//		This function calls this._setMapEntry() if an entry is provided or
			//		this._showAll() to reset the UI control to its default.
			// inp: tdi.entry
			//		The entry object for input data
			// out: tdi.entry
			//		The entry object for output data
			if(inp != null) {
				this._setMapEntry(this.left, inp);
			} else {
				this.showValueColumn(this.left, false);
			}
			if(out != null) {
				this._setMapEntry(this.right, out);
			} else {
				this.showValueColumn(this.right, false);
			}
		},
		
		_setMapEntry : function(target, entry) {
			var store = target.getStore();
			
			// if discovered schema is empty then we populate with
			// attributes from entry
			var count = 0;
			store.fetch({onItem:dojo.hitch(this, function(item) {
				count++;
			})});
			
			if(count == 0) {
				dojo.forEach(entry.getNames(), function(name) {
					store.newItem({id:name, name:name});
				});
				this.input.setModified(true);
			}
			
			// Now update current set of attributes in table
			store.fetch({onItem:dojo.hitch(this, function(item) {
				var name = store.getValue(item, "name");
				if(name != null) {
					var val = entry.getAttributeValue(name);
					store.setValue(item, "value", val ? val : "");
				}
			})});
			
			this.showValueColumn(target, true);
		},
		
		_editAttribute : function(item, input) {
			// summary:
			//		Opens the attribute map editor for the specified attmap item
			// div: tdi.AttributeMapItem
			//		The attribute map item config
			// input: boolean
			//		True if the map is from the input connector
			var map = input ? this.input.getAttributeMap(input) : this.output.getAttributeMap(false);
			var name = item.id[0];
			var attr = map.getItem(name);
			if(!attr)
				attr = map.newItem({name:name});
			
			var value = attr.getMapsTo();
			if(attr.isSimple())
				value = input ? "conn." + value : "work." + value;
			
			var div = dojo.create("div");
			var text = new dijit.form.SimpleTextarea({value:value, rows:"12", style:"width:400px"}).placeAt(div);
			var center = dojo.create("center", {}, div);
			var ok = new dijit.form.Button({type:"submit", label:this.getString("ok")}).placeAt(center);
			try {
				var dlg = new dijit.Dialog({
					content:div,
					title:this.getString("TaskCallParam.cmdEdit.name")
				});
				dojo.connect(dlg, "onExecute", dojo.hitch(this, function(dlg, text) {
					attr.setAdvanced(text.get("value"));
					this.right.setItemValue(item, "map", attr.getMapsTo());
					this.right.getTreeTable().refresh();
					dlg.hide();
					dlg.destroyRecursive(false);
					text.destroyRecursive(false);
				}, dlg, text, attr));
				dlg.show();
			} catch (err) {
				tdiutil.error(err)
			}
		},
		
		onRowDblClick : function(item, e) {
			this._editAttribute(item, e.grid == this.left.getGrid());
		},
		
		getRightRowMenu : function() {
			if(this.rightMenu == null) {
				this.rightMenu = new dijit.Menu({});
				this.rightMenu.addChild(
						new dijit.MenuItem({
							label:this.getString("action.label.3"),
							command: "Map",
							disabled: true,
							selection:{
								selection:true,
								single:true
							},
							onClick:dojo.hitch(this, "_mapAttribute")
						})
					);
				this.rightMenu.addChild(
						new dijit.MenuItem({
							label:this.getString("general.delete.label"),
							command: "Unmap",
							disabled: true,
							selection:{
								selection:true,
								single:false
							},
							onClick:dojo.hitch(this, "_removeSelectedAttributes", this.right)
						})
					);
				this.rightMenu.addChild(
						new dijit.MenuItem({
							label:this.getString("newAttribute"),
							command: "New",
							onClick:dojo.hitch(this, "_newAttribute")
						})
					);
				
				this.rightMenu.startup();
			}
			return this.rightMenu;
		},
		
		getleftRowMenu : function() {
			if(this.leftMenu == null) {
				this.leftMenu = new dijit.Menu({});
				this.leftMenu.addChild(
						new dijit.MenuItem({
							label:this.getString("unmapAttribute"),
							selection:{
								selection:true,
								single:false
							},
							onClick:dojo.hitch(this, "_removeSelectedAttributes", this.left)
						})
					);
				this.leftMenu.startup();
			}
			return this.leftMenu;
		},
		
		findReferencedInputAttributes : function() {
			// summary:
			//		Returns an object with a property for each
			//		input attribute referenced by the output map.
			//
			var refattrs = {};
			var inp = this.input.getAttributeMap(true);
			var map = this.output.getAttributeMap(false);
			var pattern = /work.\w*/g;
			dojo.forEach(map.getNames(), function(name) {
				var ami = map.getItem(name);
				if(ami.isSimple()) {
					refattrs[name] = ami;
				} else {
					var str = ami.getAdvanced();
					var arr = str.match(pattern);
					dojo.forEach(arr, function(ref) {
						refattrs[ref.substring(5)] = ami;
					});
				}
			})
			return refattrs;
		},
		
		_updateMenus : function(table, menu, event, providedItem) {
			var count = table.getSelectionCount();
			var selectedItem = table.getSelectedItem();
			if(providedItem) {
				count = 1;
				selectedItem = providedItem;
			}
			
			dojo.forEach(menu.getChildren(), function(item) {
				if(item.selection) {
					if(item.selection.single)
						item.set("disabled", count != 1);
					else
						item.set("disabled", count == 0);
				} else {
					item.set("disabled", false);
				}
			});
			
			if(table === this.right)
				this._updateRightContextMenu(selectedItem);
			
			this._updateSourceTargetMenu();
		},
		
		_updateSourceTargetMenu : function() {
			var leftsel = this.left.getSelectionCount();
			var rightsel = this.right.getSelectionCount();
			if(leftsel > 0) {
				this.enableMenuItem("mapSelected", true);
				this.enableMenuItem("useAsLinkCriteria", this._canUseLinkCriteria);
			} else {
				this.enableMenuItem("mapSelected", false);
				this.enableMenuItem("useAsLinkCriteria", false);
			}
		},
		
		_updateRightContextMenu : function(selection) {
			var map = this.output.getAttributeMap(false);
			dojo.forEach(this.getRightRowMenu().getChildren(), function(menuitem) {
				var mapped = selection == null ? false : map.isMapped(selection.id[0]);
				if(menuitem.command == "Map") {
					if(selection == null)
						mapped = true;
					menuitem.set("disabled", mapped);
				} else if(menuitem.command == "Unmap") {
					if(selection == null)
						mapped = false;
					menuitem.set("disabled", !mapped);
				}
			});
		},
		
		_mapAttribute : function(e) {
			// var selection = this.right.getGrid().selection.getSelected();
			this._editAttribute(this.contextItem, false); // selection[0], false);
		},

		_removeSelectedAttributes: function(table, menuEvent) {
			
			//
			// silly behaviour in TreeTable. There can be a selection independent of the item
			// that was right-clicked. So, if the contextItem (righ-clicked) is not part of the selected
			// items array we replace the "selection" with a single context item. Otherwise the selection is affected.
			//
			var selectedItems = table.getSelectedRows();
			var contextItem = this.contextItem;
			if(contextItem) {
				var included = dojo.some(selectedItems, function(item) {
					return contextItem.id[0] == item.id[0];
				});
				if(!included || selectedItems.length == 0) {
					selectedItems = [contextItem];
				}
				// should not linger after we acted on it
				this.contextItem = null;
			}
			
			var attributes = [];
			dojo.forEach(selectedItems, function(item) {
				attributes.push(item.id[0]);
			})
			
			
			tdiutil.confirm(this.getString("removeSelectedAttributes") + "<p>" + attributes.join(", "), dojo.hitch(this, function(table, buttonId, msg, check) {
				if(buttonId == 0) {
					if(this.left === table)
						this._unmapSelectedAttributes(table, this.input.getAttributeMap(true), this.input.getSchema(true), selectedItems);
					else
						this._unmapAttribute(table, selectedItems);
				}	
			 }, table));
		},
		
		_unmapSelectedAttributes : function(table, map, schema, selectedItems) {
			var selection = selectedItems || table.getSelectedRows();
			var store = table.getStore();
			dojo.forEach(selection, function(item) {
				var name = item.id[0];
				if(map.isMapped(name)) {
					map.removeItem(name);
				}
				store.deleteItem(item);
				if(schema)
					schema.removeItem(name);
			});
			store.save();
		},
		
		_unmapAttribute : function(table, selectedItems) {
			var selection = selectedItems || table.getGrid().selection.getSelected();
			var inp = this.input.getAttributeMap(true);
			var map = this.output.getAttributeMap(false);
			var schema = this.output.getSchema(false);
			var store = table.getStore();
			this._unmapSelectedAttributes(table, map, schema, selection);

			// -- now unmap those input attributes that are no longer used
			var refattrs = this.findReferencedInputAttributes();
			dojo.forEach(selection, function(item) {
				var name = item.id[0];
				if(refattrs[name] == null) {
					inp.removeItem(name);
				}
			});
		},

		_newAttribute : function(e) {
			tdiutil.createNewAttribute(this.output.getAttributeMap(false), this.output.getName(), this.right.getStore(), dojo.hitch(this, function(newitem) {
				newitem.setAdvanced("work[\"" + newitem.getName() + "\"]");
			}));
		},
		
		_useAsLinkCriteria : function(e) {
			var link = this.output.getLinkCriteria();
			dojo.forEach(this.right.getSelectedAttributes(), function(attr) {
				link.setCriteriaFor(attr, "equals", "$"+attr);
			});
		},

		_updateTableAndButtons : function() {
			this.left.getGrid().noDataMessage = this.getNoDataMessage(this.input);
			this.right.getGrid().noDataMessage = this.getNoDataMessage(this.output);
			this.left.getTreeTable().refresh();
			this.right.getTreeTable().refresh();
		},

		_updateStoreFromMap: function(table, map) {
			var target = table.findSingleItem("id", map.getName());
			if(target)
				table.setItemValue(target, "map", map.getMapsTo());
			else
				table.addItem({"id":map.getName(), "name":map.getName(), "map":map.getMapsTo()});
		},
		
		_mapAll : function(inp, map, sourceAttributes, targetAttributes) {
			dojo.forEach(sourceAttributes, dojo.hitch(this, function(name) {
				if(name != "*" && !map.isMapped(name)) {
					var newmap = map.newItem({name:name});
					this._updateStoreFromMap(this.right, newmap);
					
					if(!inp.isMapped(name)) {
						newmap = inp.newItem({name:name});
						// this._updateStoreFromMap(this.left, newmap);
					}
				}
			}));
		},
		
		_concatenateAll : function(inp, map, sourceAttributes, targetAttributes) {
			var script = [];
			dojo.forEach(sourceAttributes, function(name) {
				script.push("\twork.getString(\"" + name + "\")");
			});
			
			var ami = map.getItem(targetAttributes[0]);
			if(ami == null) {
				ami = map.newItem({name:targetAttributes[0]});
			}
			ami.setAdvanced("var sep = \" \";\nreturn \n" + script.join(" + sep +\n") + ";");
			this._updateStoreFromMap(this.right, ami);				
		},
		
		_mergeAll : function(inp, map, sourceAttributes, targetAttributes) {
			var ami = map.getItem(targetAttributes[0]);
			if(ami == null) {
				ami = map.newItem({name:targetAttributes[0]});
			}
			var str = "var attr = system.newAttribute('" + targetAttributes[0] + "');\n";
			dojo.forEach(sourceAttributes, function(attr) {
				str += "attr.addValues(work['" + attr + "']);\n";
			})
			str += "return attr;\n";
			ami.setAdvanced(str);
			this._updateStoreFromMap(this.right, ami);				
		},
		
		_clearAll : function() {
			tdiutil.confirm(this.getString("clearAttributeMaps"), dojo.hitch(this, function(buttonId, messageId, checked) {
				if(buttonId == 0) {
					this.input.getAttributeMap(true).removeAllItems();
					this.output.getAttributeMap(false).removeAllItems();
					this.repaint();
				}
			}));
		},
		
		_mapSelectedAttributes : function(mapAll) {
			var sourceAttributes = mapAll ? this.left.getAllAttributes() : this.left.getSelectedAttributes();
			var targetAttributes = this.right.getSelectedAttributes();
			if(sourceAttributes.length == 0 && !mapAll)
				return;
			
			var inp = this.input.getAttributeMap(true);
			var map = this.output.getAttributeMap(false);

			var targetAttr = null;
			var targetAttr = null;
			
			// -- Any A to corresponding B
			if (mapAll) {
				this._mapAll(inp, map, sourceAttributes, targetAttributes);
			
			// -- Single A -> B
			} else if(sourceAttributes.length == 1 && targetAttributes.length == 1) {
				var item = null;
				if(!map.isMapped(targetAttributes[0])) {
					item = map.newItem({
						name:targetAttributes[0],
						mapsTo:sourceAttributes[0]
					});
				} else {
					item = map.getItem(targetAttributes[0]);
					item.setSimple(sourceAttributes[0]);
				}
				if(item)
					this._updateStoreFromMap(this.right, item);				
				
				// -- Single/Many A -> None B
			} else if(sourceAttributes.length > 0 && targetAttributes.length != 1) {
				dojo.forEach(sourceAttributes, dojo.hitch(this, function(attr) {
					if(!map.isMapped(attr)) {
						var newmap = map.newItem({
							name:attr
						});
						this._updateStoreFromMap(this.right, newmap);
					}
				}));
				
			// -- Many A to single B (concat or merge)
			} else if(sourceAttributes.length > 1 && targetAttributes.length == 1) {
				var msg = new dojoe.messagedialog.MessageDialog({
					buttons:[this.getString("common.Copy.name"), this.getString("merge"), this.getString("concatenate"), this.getString("cancel")],
					type:"Confirm",
					width:"500px",
					height:"300px",
					messageId: "",
					message: this.getString("WebCE.selectMappingOptions", [targetAttributes[0], targetAttributes[0]]),
//					message:"Do you want to<ul><li>Copy source attributes to its target equivalent (a -> a)" +
//						"<li>Merge source attributes into " + targetAttributes[0] + 
//						"<li>Concatenate source attributes to a single value in " + targetAttributes[0],
					callback:dojo.hitch(this, function(btn, msgid, checked, parms) {
						if(btn == 0)
							this._mapAll(parms.inp, parms.out, parms.sourceAttributes, parms.targetAttributes);
						else if(btn == 1)
							this._mergeAll(parms.inp, parms.out, parms.sourceAttributes, parms.targetAttributes);
						else if(btn == 2)
							this._concatenateAll(parms.inp, parms.out, parms.sourceAttributes, parms.targetAttributes);
						else
							return;
						this.repaint();
					}),
					callbackParms:{inp:inp, out:map, sourceAttributes:sourceAttributes, targetAttributes:targetAttributes}
				})
				msg.show();
			}
		},
		
		repaint : function() {
			this.onRefreshInput();
			this.onRefreshOutput();
		},
		
		updateView : function(firstcall) {
			// summary:
			//		Updates the view by creating all UI controls.
			
			if(this.left != null)
				return;

			// console.log("updateView create tables");
			this._borderContainer = new dijit.layout.BorderContainer({style:"width:100%, height:100%"}).placeAt(this.Main);
			
			// 
			// -- Input map
			//
			this.left = new tdi.TreeTableWidget({
				getNoDataMessage:dojo.hitch(this, "getNoDataMessage", this.input),
				//onRefresh:dojo.hitch(this, "onRefreshInput"),
				getTreeTableLayout:dojo.hitch(this, "getTreeTableLayout"),
				onRowDblClick:dojo.hitch(this, "onRowDblClick"),
				toolbarOptions: {
					refreshIcon:false,
					actionMenu:false
				},
				getToolbarVisible : function() {
					return true;
				},
				getTreeTableStyle: function() {
					return "padding:0px; margin:0px"
				}
			});
			
			this.left.getTreeTable().addContextMenuToTreeTable("rowMenu", this.getleftRowMenu());
			
			dojo.connect(this.left.getGrid(), "onSelected", dojo.hitch(this, "_updateMenus", this.left, this.leftMenu));
			dojo.connect(this.left.getGrid(), "onDeselected", dojo.hitch(this, "_updateMenus", this.left, this.leftMenu));

			//
			// Disable/Enable map/unmap based on selection
			//
			dojo.connect(this.left.getGrid(), "onRowContextMenu", dojo.hitch(this, function(args) {
				var selection = this.left.getItem(args.rowIndex);
				this._updateMenus(this.left, this.leftMenu, args, selection);
				this.contextItem = selection;
			}));
			
			child = new dijit.layout.ContentPane({region:"leading", splitter:true, style:"width:33%"});
			this._borderContainer.addChild(child);
			
			this.leftStack = new dijit.layout.StackContainer({});
			
			this.leftPanel = new dijit.layout.BorderContainer({style:"width:100%; height:100%", gutters:false});
			
			this.leftPanelTop = new dijit.layout.ContentPane({region:"top"});
			var title = dojo.create("h1", {innerHTML:this.getString("ConnectorFlowWidget_source"), style:"padding:5px"});
			this.leftPanelTop.set("content", title);
			
			this.leftPanelCenter = new dijit.layout.ContentPane({region:"center", style:"width:100%; height:100%; margin:0; padding:0"});
			this.leftPanelCenter.set("content", this.left);
			
			this.leftPanel.addChild(this.leftPanelTop);
			this.leftPanel.addChild(this.leftPanelCenter);
			
			this.leftStack.addChild(this.leftPanel);
			
			try {
				child.set("content", this.leftStack);
			} catch(err) {
				alert(err);
			}
			
			// 
			// -- Output map
			//
			this.right = new tdi.TreeTableWidget({
				getNoDataMessage:dojo.hitch(this, "getNoDataMessage", this.output),
				//onRefresh:dojo.hitch(this, "onRefreshOutput"),
				getTreeTableLayout:dojo.hitch(this, "getTreeTableLayoutRight"),
				onRowDblClick:dojo.hitch(this, "onRowDblClick"),
				toolbarOptions: {
					refreshIcon:false,
					actionMenu:false
				},
				getToolbarVisible : function() {
					return true;
				},
				getTreeTableStyle: function() {
					return "padding:0px; margin:0px"
				}
			});
			
			this.right.getTreeTable().addContextMenuToTreeTable("rowMenu", this.getRightRowMenu());
			
			//
			// Disable/Enable map/unmap based on selection
			//
			dojo.connect(this.right.getGrid(), "onRowContextMenu", dojo.hitch(this, function(args) {
				var map = this.output.getAttributeMap(false);
				var selection = this.right.getItem(args.rowIndex);
				dojo.forEach(this.getRightRowMenu().getChildren(), function(menuitem) {
					var mapped = selection == null ? false : map.isMapped(selection.id[0]);
					if(menuitem.command == "Map") {
						if(selection == null)
							mapped = true;
						menuitem.set("disabled", mapped);
					} else if(menuitem.command == "Unmap") {
						if(selection == null)
							mapped = false;
						menuitem.set("disabled", !mapped);
					}
				});
				this.contextItem = selection;
			}));

			dojo.connect(this.right.getGrid(), "onSelected", dojo.hitch(this, "_updateMenus", this.right, this.rightMenu));
			dojo.connect(this.right.getGrid(), "onDeselected", dojo.hitch(this, "_updateMenus", this.right, this.rightMenu));
			dojo.connect(this.right.getGrid(), "onStyleRow", dojo.hitch(this, function(inRow) {
				var row = this.right.getTreeTable().getItem(inRow.index); 
				if(row != null) {
					if(!this.output.getAttributeMap(false).isMapped(row.id[0]))
						inRow.customStyles += "font-style:italic"
				}
			}));
			
			this.rightStack = new dijit.layout.StackContainer({});
			
			
			this.rightStack = new dijit.layout.StackContainer({});
			
			this.rightPanel = new dijit.layout.BorderContainer({style:"width:100%; height:100%", gutters:false});
			
			this.rightPanelTop = new dijit.layout.ContentPane({region:"top"});
			var title = dojo.create("h1", {innerHTML:this.getString("ConnectorFlowWidget_target"), style:"padding:5px"});
			this.rightPanelTop.set("content", title);
			
			this.rightPanelCenter = new dijit.layout.ContentPane({region:"center", style:"width:100%; height:100%; margin:0; padding:0"});
			this.rightPanelCenter.set("content", this.right);
			
			this.rightPanel.addChild(this.rightPanelTop);
			this.rightPanel.addChild(this.rightPanelCenter);
			
			this.rightStack.addChild(this.rightPanel);
			
			child = new dijit.layout.ContentPane({region:"center", splitter:true, style:"width:67%"});
			this._borderContainer.addChild(child);
			child.set("content", this.rightStack);

			this._buttons = {};
			this.addToToolbar("mapSelected", new dijit.form.Button({
		 		cmd: "mapSelected",
				label:this.getString("WorkMapWidget.attributemap.title"),
				onClick:dojo.hitch(this, "_mapSelectedAttributes", false)
			}));
			this.addToToolbar("mapAll", new dijit.form.Button({
				cmd:"mapAll",
				label:this.getString("mapAll"),
				onClick:dojo.hitch(this, "_mapSelectedAttributes", true)
			}));
			this.addToToolbar("deleteAll", new dijit.form.Button({
				cmd:"deleteAll",
				label:this.getString("DiscoverSchemaWidget_8"),
				onClick:dojo.hitch(this, "_clearAll", true)
			}));
			this._borderContainer.startup();
		},

		toggleSchema : function() {
			// summary:
			//		Called to toggle display of schema items
			this.showSchema = !this.showSchema;
		},
		
		setConfig : function(obj) {
			// summary:
			//		Called to toggle display of schema items
			// console.log("setConfig");
			this.input = obj.input;
			this.output = obj.output;
			
			this.onRefreshInput();
			this.onRefreshOutput();
			this.updateForms();
			
			this._updateMenus(this.left, this.getleftRowMenu(), null, null);
			this._updateMenus(this.right, this.getRightRowMenu(), null, null);
		},
		
		addToToolbar : function(id, item, left) {
			if(left) {
				this.left.getTreeTable().addToToolbar(item);
			} else {
				this.right.getTreeTable().addToToolbar(item);				
			}
			this._buttons[id] = item;
		},
		
		showValueColumn : function(table, show) {
			// summary:
			//		Adds the value column if show=true and removes it if show=false
			//
			var struct;
			if(table == this.left) {
				struct = this.getTreeTableLayout(show);
			} else {
				struct = this.getTreeTableLayoutRight(show);
			}
			table.getGrid().set("structure", struct);
			
//			var structure = table.getGrid().get("structure");
//			if(show && structure.length == 1) {
//				table.getGrid().set("structure", [
//				   {field:"name", name:this.getString("name"), width:"auto"},
//				   {field:"value", name:this.getString("value"), width:"auto"}
//				]);
//			} else if (!show && structure.length == 2) {
//				table.getGrid().set("structure", this.getTreeTableLayout());
//			}
		},

		getTreeTableLayout : function(incvalue) {
  			var layout = [
  				{field:"name", name:this.getString("name"), width:"auto"}
  			];
  			if(incvalue) {
  				layout.push(
  		  			{field:"value", name:this.getString("value"), width:"auto"}
  		  		);
  			}
  			return layout;
		},
		
		getMapValue: function(str, index) {
			if(!this.right)
				return str;
			
			var name = this.right.getItemValue(index, "name");
			var map = this.output.getAttributeMap(false).getItem(name);
			var value = str;
			if(map) {
				if(map.isSimple())
					value = map.getSimple();
				else
					value = map.getAdvanced();
			}
			if(value && value.indexOf("\n") != -1) {
				value = value.substring(0, value.indexOf("\n")) + " ...";
			}
			
			return value;
		},
		
		getTreeTableLayoutRight : function(incvalue) {
  			var layout = [
  				{field:"map", name:this.getString("WorkMapWidget.attributemap.title"), width:"75%", formatter:dojo.hitch(this, "getMapValue")},
  				{field:"name", name:this.getString("name"), width:"auto"}
  			];
  			if(incvalue) {
  				layout.push(
  		  			{field:"value", name:this.getString("value"), width:"auto"}
  		  		);
  			}
  			return layout;
		},
		
		getNoDataMessage: function(conn) {
			if(tdiutil.isConnectorConfigured(conn))
				return "The attribute map is empty - use Discover button to detect attributes";
			else
				return "The connector is not configured - use Configure button configure the connector";
		},

		onRefreshInput : function(args) {
			// console.log("onRefreshInput");
			this.onRefresh(this.input, this.left, true);
		},
		
		onRefreshOutput : function(args) {
			// console.log("onRefreshOutput");
			this.onRefresh(this.output, this.right, false);
		},
		
		removeAllItems : function(input) {
			var table = input ? this.left : this.right;
			table.removeAllItems();
		},
		
		onRefresh : function(conn, target, input) {
			if(target && conn) {
				var arr = this._createSortedArray(conn.getAttributeMap(input), conn.getSchema(input), this.showSchema);
				// console.log("-- onRefresh: " + arr.length + " items");
				dojo.forEach(arr, dojo.hitch(this, function(attr) {
					try {
						target.getStore().newItem({
							id:attr,
							name:attr
						});
					} catch(err) {
						// console.log(attr + ": " + err)
					}
				}));
				target.getStore().save();
				target._table.refresh();
				target.getGrid().noDataMessage = this.getNoDataMessage(conn);
			}
		},

		resetEditorComponent : function(component, input) {
			var cp = input ? this.leftStack : this.rightStack;
			var table = input ? this.left : this.right;
			var conn = input ? this.input : this.output;
			cp.selectChild(table);
			cp.removeChild(component);
			this._updateTableAndButtons();
			this.updateForms();
			this.repaint();
		},
		
		setEditorComponent : function(component, input) {
			var cp = input ? this.leftStack : this.rightStack;
			var table = input ? this.left : this.right;
			cp.addChild(component);
			cp.selectChild(component);
		},
		
		startup : function() {
			setTimeout(dojo.hitch(this,function() {
				this.onRefreshInput();
				this.onRefreshOutput();
			}), 500);			
		},
		
		createDragItem : function(item) {
			var node = dojo.create("div", {innerHTML:item});
			return {node:node, data:item, type:"text"};
		},
		
		enableMenuItem : function(command, enable) {
			var button = this._buttons[command]; // this.toolbar.getButton(command);
			if(button != null) {
				button.set("disabled", !enable);
			}
		},
		
		updateForms : function() {
			this._canUseLinkCriteria = false;
			var type = tdiutil.getConnectorType(this.output);
			if(type != "[parent]" && type != null) {
				dojo.when(tdiapi.getConnectorForm(type), dojo.hitch(this, function(formData) {
					this.right.formData = formData;
					if(formData != null) {
						var modes = formData.supportedModes.mode;
						for(i = 0; i < modes.length; i++) {
							if(modes[i].value == "Update") {
								this._canUseLinkCriteria = true;
							} 
						}
					}
					this._updateSourceTargetMenu();
				}));
			} else {
				this._updateSourceTargetMenu();
			}
		},
		
		postCreate : function() {
			this.updateView();

//			this.dnd = new dojo.dnd.Source(this.left._table.grid.domNode, {copyOnly:true, selfAccept:false});
//			this.dnd.creator = dojo.hitch(this, "createDragItem");
//			this.dndTarget = new dojo.dnd.Source(this.right._table.grid.domNode, {isSource:false, selfAccept:false});

			this.inherited("postCreate", arguments);
		},
		
		resize : function(ns) {
			if(!ns) {
				ns = {w:this.domNode.clientWidth, h:this.domNode.clientHeight};
			}
			if(this._borderContainer !== null) {
				this._borderContainer.resize(ns);
				if(!ns)
					this._borderContainer.layout();
			}
		},
		
		destroy : function() {
			if(this._borderContainer)
				this._borderContainer.destroyRecursive(false);
			if(this.left)
				this.left.destroyRecursive(false);
			if(this.right)
				this.right.destroyRecursive(false);
			if(this.toolbar)
				this.toolbar.destroyRecursive(false);
			if(this._surface)
				this._surface.destroy();
			if(this.leftMenu)
				this.leftMenu.destroyRecursive(false);
			if(this.rightMenu)
				this.rightMenu.destroyRecursive(false);
			if(this.leftStack)
				this.leftStack.destroyRecursive(false);
			if(this.rightStack)
				this.rightStack.destroyRecursive(false);
			this.inherited(arguments);
		}
		
	}
);
