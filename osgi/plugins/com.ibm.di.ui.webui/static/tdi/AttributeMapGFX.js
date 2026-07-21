dojo.provide("tdi.AttributeMapGFX");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");

dojo.require("dojo.parser");
dojo.require("dojo.dnd.Source");

dojo.require("dojox.gfx");
dojo.require("dojox.gfx.move");
dojo.require("dojox.gfx.utils");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.AttributeMapItem");

dojo.declare("tdi.AttributeMapGFX",
	[dijit._Widget, dijit._Templated],
	{
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/AttributeMapGFX.html"),
		
		// input: tdi.connector
		//		Input connector configuration
		input : null,
		
		// output: tdi.connector
		//		Output connector configuration
		output : null,

		// showSchema: boolean
		//		If true also show unmapped items (e.g. schema items)
		//		in the mapping area.
		showSchema : false,
		
		// The GFX object where links between attributes are drawn
		_surface : null,
		
		// _inputDivMap: Object(tdi.AttributeMapItem)
		//		The input attribute & schema controls
		_inputDivmap : null,
		
		// _outputDivMap: Object(tdi.AttributeMapItem)
		//		The output attribute & schema controls
		_outputDivmap : null,
		
		// _links: {} 
		//		The link DIV objects created for each output map item
		_links : null,
		
		// _events: array
		//		Events that we need to clear on destroy (from dojo.connect)
		_events : null,

		// _normalStroke: Object
		//		The style attributes used for the line
		//		connecting two or more attributes.
		_normalStroke: {width:1, color:"black"},
		
		// _normalStroke: Object
		//		The style attributes used when hovering over the line
		//		connecting two or more attributes.
		_hoverStroke: {width:3, color:"black"},
		
		_markLinks : function(div) {
			// summary:
			//		Highlight lines connected to div
			// div:
			//		The target tdi.AttributeMapItem
			var attr = div.getName();
			if(this._links[attr] != undefined) {
				dojo.forEach(this._links[attr], dojo.hitch(this, function(poly) {
					poly.setStroke(this._hoverStroke);
				}));
			}
			dojo.style(div, "color:blue");
		},
		
		_unmarkLinks : function(div) {
			// summary:
			//		Clears highlight for lines connected to div
			// div:
			//		The target tdi.AttributeMapItem
			var attr = div.getName();
			if(this._links[attr] != undefined) {
				dojo.forEach(this._links[attr], dojo.hitch(this, function(poly) {
					poly.setStroke(this._normalStroke);
				}));
			}
			dojo.style(div, "color:black");
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
			dojo.forEach(map.getNames(), dojo.hitch(this, function(str) {
				arr.push(str);
			}));
			arr.sort();
			
			var arr2 = new Array();
			if(includeschema) {
				dojo.forEach(schema.getNames(), dojo.hitch(this, function(str) {
					if(!map.isMapped(str)) {
						arr2.push(str);
					}
				}));
				arr2.sort();
			}

			// Append schema items to the map array
			return arr.concat(arr2);
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
			if(inp != null)
				this._setMapEntry(this._inputDivmap, inp);
			else
				this._showAll(this._inputDivmap);
			
			if(out != null)
				this._setMapEntry(this._outputDivmap, out);
			else
				this._showAll(this._outputDivmap);
			
			this._createLinkObjects();
		},
		
		_showAll : function(map) {
			// summary:
			//		Set all map items visible in a map and clear the value attribute
			// map:
			//		The internal map of UI controls
			for(item in map) {
				map[item].show();
				map[item].attr("value", "");
			}
		},
		
		_setMapEntry : function(map, entry) {
			// summary:
			//		Update the UI controls in map with values from entry
			// description:
			//		Each UI control for an attribute/schema item has a "value"
			//		attribute to display runtime values. Items in the map are
			//		either cleared (entry has no value) or updated (entry has value).
			// map:
			//		The internal map of UI controls
			// entry: tdi.entry
			//		The data entry
			try {
				for(var item in map) {
					var val = entry.getAttribute(item);
					if(val == undefined) {
						map[item].hide();
					} else {
						map[item].attr("value", val.getValue());
					}
				}
			} catch (err) {
				alert(err);
			}
		},
		
		_editAttribute : function(input, div) {
			// summary:
			//		Opens the attribute map editor for the specified attmap item
			// div: tdi.AttributeMapItem
			//		The attribute map item config
			// input: boolean
			//		True if the map is from the input connector
			var attr = div.getName(); // childNodes[0].innerHTML;
			var map = input ? this.input.getAttributeMap(input) : this.output.getAttributeMap(input);
			var schema = this.input.getSchema(input).getNames();
			var ami = map.getItem(attr);
			try {
				this.editor.editAttribute({ami:ami, attr:attr, map:map, input:input, source:this, availableAttributes:schema})
				div.openDropDown();
			} catch (err) {
				alert(err)
			}
		},
		
		_createAttmapControls: function(connector, input, divmap, target) {
			// summary:
			//		Creates a tdi.AttributeMapItem for each attribute/schema 
			//		in the connector and stores a reference in map
			//		using the map/schema name as the property name.
			// connector: tdi.connector
			//		The connector
			// input: boolean
			//		True if it is an input connector
			// divmap: Object
			//		Object with old UI controls
			// target: string/dijit
			//		The target DOM object for the new items
			// returns: Object
			//		The new UI controls object
			var map = connector.getAttributeMap(input);
			var arr = this._createSortedArray(map, connector.getSchema(input), this.showSchema);
			
			// -- destroy existing attmap items
			for(var f in divmap) {
				divmap[f].destroy();
			}
			divmap = {};
			
			dojo.forEach(arr, dojo.hitch(this, function(str) {
				var div = new tdi.AttributeMapItem({label:str, dropDown:this.getTooltipDialog()}).placeAt(target);
				div.onMouseOver = dojo.hitch(this, "_markLinks");
				div.onMouseOut = dojo.hitch(this, "_unmarkLinks");
				div.onClick = dojo.hitch(this, "_editAttribute", input);

				divmap[str] = div;
				if(!map.isMapped(str)) {
					div.hide();
				}
			}));
			
			return divmap;
		},
		
		_createLinkObjects : function() {
			// summary:
			// 		Create the dojo.gfx objects that links one or more attributes
			//
			this._surface.clear();
			this._offset = 1;
			
			//
			// Recompute height of _surface to match size of largest panel
			// The height is the max height of left/right panel and the width
			// is the scene's DIV.
			//
			var height = Math.max(dojo.position(this.LeftPanel).h, dojo.position(this.RightPanel).h);
			var width = dojo.position(this.scene).w;
			this._surface.setDimensions(width, height);
			
			//
			// Recompute the width of the left panel to minimize graphic noise in the scene object
			//
			var maxw = 0;
			for(var key in this._inputDivmap) {
				var pos = dojo.position(this._inputDivmap[key]._label /*childNodes[0]*/);
				maxw = Math.max(pos.w, maxw);  
			}
			if(maxw > 0)
				dojo.style(this.LeftPanel, "width", maxw+"px");
			

			// Get output attribute map
			var out = this.output.getAttributeMap(false);
			
			// Clear the attribute->link object map
			this._links = {};
			
			// Create a link for each output map item
			dojo.forEach(out.getNames(), dojo.hitch(this, function(name) {
				var item = out.getItem(name);
				if(item.isSimple()) {
					this._createLinkFor(item);
				} else {
					this._createAdvLinkFor(item);
				}
			}));
		},
		
		_createAdvLinkFor : function(item) {
			// summary:
			//		Creates a dojo.gfx item for an advanced type map item 
			var points = new Array();
			var name = item.getName();
			var dst = this._outputDivmap[name];
			var p2 = this.getOutputAnchor(dst);
			var p3 = {x:p2.x-6, y:p2.y};
			var p1 = {x:p2.x-50, y:p2.y};
			
			points.push(p1);
			points.push({x:p1.x-6, y:p1.y-6});
			points.push({x:p1.x-6, y:p1.y+4});
			points.push(p1);
			points.push(p3);
			points.push({x:p2.x, y:p2.y-6});
			points.push({x:p2.x, y:p2.y+4});
			points.push(p3);
			
			var poly = this._surface.createPolyline({points:points}).setStroke("blue");
			if(this._links[name] == undefined) {
				this._links[name] = new Array();
			}
			this._links[name].push(poly);
		},
		
		_createLinkFor : function(item) {
			// summary:
			//		Creates a dojo.gfx item for a simple type map item 
			var name = item.getName();
			var simple = item.getSimple();
			var src = this._inputDivmap[simple];
			var dst = this._outputDivmap[name];
			
			// If a simple map has no corresponding input
			if(src == undefined) {
				this._createAdvLinkFor(item);
				return;
			}
			
			/*
			 */
			var pos = dojo.position(this.LeftPanel);
			var points = new Array();
			var p0 = this.getInputAnchor(src);
			var p1 = {x:pos.w, y:p0.y};
			var p2 = this.getOutputAnchor(dst);
			
			var path = this._surface.createPath().setStroke(this._normalStroke);
			path.moveTo(p0.x, p0.y);
			
			if(p1.y != p2.y) {
				path.lineTo(p1.x, p1.y);
				path.curveTo(p2.x, p1.y, p1.x, p2.y, p2.x, p2.y);
			} else {
				path.lineTo(p2.x, p2.y);
			}
			
			if(this._links[simple] == undefined) {
				this._links[simple] = new Array();
			}
			this._links[simple].push(path);
		},
		

		dropHandler : function(source, nodes, copy, target) {
			// summary:
			//		This method is called when a node is dropped onto
			//		another node in the attribute map.
			
			// DnD is system broadcast so we must check if
			// we really are the target of this dnd op.
			if(target != this.dndTarget)
				return;

			var simple = "";
			dojo.forEach(nodes, dojo.hitch(this, function(node) {
				if(simple.length > 0)
					simple += "\n";
				simple += node.childNodes[0].innerHTML;
			}));
			
			var targetAttr = null;
			if(target.current == null) {
				targetAttr = simple;
			} else {
				targetAttr = target.current.childNodes[0].innerHTML;
			}
			
			try {
				var item = this.output.getAttributeMap(false).getItem(targetAttr);
				if(item != null) {
					var adv = !item.isSimple();
					item.setSimple(simple);
					if(adv)
						this._createLinkFor(item);
				} else {
					if(!this.input.getAttributeMap(true).isMapped(simple))
						this.input.getAttributeMap(true).newItem({name:simple, type:"Simple", mapsTo:simple});
					this.output.getAttributeMap(false).newItem({name:targetAttr, type:"Simple", mapsTo:simple});
				}
			} catch(err) {
				alert(targetAttr + ": " + err);
			}
			this.updateView();
		},
		
		createDragItem : function(item, hint) {
			// summary:
			//		This method is called by the DnD framework to create a
			//		DOM node to display the item being dragged. We simply create a DIV
			//		node with the attribute's name as its inner text.
			var node = dojo.create("div", {innerHTML:item});
			return {node:node, data:item, type:"text"};
		},
		
		updateView : function(firstcall) {
			// summary:
			//		Updates the view by creating all UI controls and synch'ing dnd items.
			this._inputDivmap = this._createAttmapControls(this.input, true, this._inputDivmap, this.LeftPanel);
			this._outputDivmap = this._createAttmapControls(this.output, false, this._outputDivmap, this.RightPanel);
			this._surface.whenLoaded(dojo.hitch(this, function() {
				this._createLinkObjects();
			}));
			
			// TODO: remove this and create dnd objects after creating items
			this.dnd.sync();
		},

		toggleSchema : function() {
			// summary:
			//		Called to toggle display of schema items
			this.showSchema = !this.showSchema;
			var map = this.input.getAttributeMap(true);
			for(var f in this._inputDivmap) {
				if(!map.isMapped(f)) {
					var div = this._inputDivmap[f];
					if(this.showSchema) {
						div.show();
					} else {
						div.hide();
					}
				}
			}
			
			var map = this.output.getAttributeMap(false);
			for(var f in this._outputDivmap) {
				if(!map.isMapped(f)) {
					var div = this._outputDivmap[f];
					if(this.showSchema) {
						div.show();
					} else {
						div.hide();
					}
				}
			}
		},
		
		setConfig : function(obj) {
			// summary:
			//		Called to toggle display of schema items
			this.input = obj.input;
			this.output = obj.output;
			this.updateView();
		},
		
		getInputAnchor : function(div) {
			// summary:
			//		Returns the x:y anchor point for the input div
			// description:
			//		x position should be at the end of the div contents.
			var str = div.getName(); // childNodes[0].innerHTML; //getName();
			var b = dojo.position(this.LeftPanel);
			var box = dojo.position(div.domNode); //.domNode);
			var box2 = dojo.position(div._label /*childNodes[0]*/); //.domNode);
			var x = box2.w + 10;
			var y = box.y + (box.h / 2);
			return {x:x, y:y-b.y};
		},
		
		getTooltipDialog : function() {
			if(this.editor == null) {
				this.editor = new tdi.AttributeMapItemEditor({});
				this.tooltipDialog = new dijit.TooltipDialog({
					content:this.editor
				});
			}
			return this.tooltipDialog;
		},
		
		getOutputAnchor : function(div) {
			// summary:
			//		Returns the x:y anchor point for the output div
			// description:
			//		x position should be at the start of the div contents.
			var env = dojo.position(this.containerNode);
			var b = dojo.position(this.RightPanel);
			var box = dojo.position(div.domNode);
			var y = box.y + (box.h / 2);
			return {x:b.x-env.x, y:y-b.y};
		},
		
		postCreate : function() {
			dojo.addClass(this.RightPanel, "dojoDndItem");
			this._surface = dojox.gfx.createSurface(this.scene, 0, 0);
			this.dnd = new dojo.dnd.Source(this.LeftPanel, {copyOnly:true, selfAccept:false});
			this.dnd.creator = dojo.hitch(this, "createDragItem");
			this.dndTarget = new dojo.dnd.Source(this.RightPanel, {isSource:false, selfAccept:false});
			this._events = new Array();
			this._events.push(dojo.connect(this.dndTarget, "onDndDrop", this, "dropHandler"));

			// Override to discard nodes that otherwise would be inserted
			this.dndTarget.insertNodes = function(data, before, anchor) {
			};
		},
		
		resize : function(ns) {
			if(this._links != null) 
				this._createLinkObjects();
		},
		
		destroy : function() {
			dojo.forEach(this._events, function(evt) {
				dojo.disconnect(evt);
			});
			dojo.empty(this.scene);
			this.inherited("destroy", arguments);
		}
	}
);
