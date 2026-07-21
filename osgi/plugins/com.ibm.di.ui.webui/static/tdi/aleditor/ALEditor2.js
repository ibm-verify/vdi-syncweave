/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/_base/fx",
	"dojo/aspect",
	"dojo/dom-attr",
	"dojo/dom-class",
	"dojo/dom-style",
	"dojo/dnd/move",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/form/HorizontalSlider",
	"dijit/form/Select",
	"dijit/form/DropDownButton",
	"dijit/form/NumberSpinner",
	"dijit/form/TextBox",
	"dijit/Dialog",
	"dijit/Toolbar",
	"dijit/TooltipDialog",
	"dijit/form/Button",
	"dijit/layout/StackContainer",
	"dijit/popup",
	"dijit/Calendar",
	"dojo/topic",
	"dojox/gfx",
	"dojox/gfx/utils",
	"idx/widget/Dialog",
	"tdi/ALSchedule",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/config/tdicontainer",
	"./ALAttributeMap",
	"./ALButton",
	"./ALComponent",
	"./ALComponent2",
	"./ALComps",
	"./ALConnection",
	"./ALConnectorEditor",
	"./ALDataCollector",
	"./ALDebugger",
	"./ALDropPoint",
	"./ALEditorLayout",
	"./ALInitParams",
	"./ALPlaceholder",
	"./Colors",
	"./Border",
	"tdi/orion/OrionEditor",
	"tdi/AttributeMap3",
	"tdi/AttributeMapItemEditor3",
	"tdi/ConnectorEditor",
	"tdi/LinkCriteriaWidget",
	"tdi/model/ComponentsModel",
	"tdi/AttributeLoop",
	"tdi/FilteredLogViewer",
	"tdi/BranchEditor",
	"tdi/LinkCriteriaWidget",
	"tdi/ToolbarLabel",
	"tdi/HooksWidget",
	"tdi/TableWidget",
	"tdi/atom/tdifeed",
	"tdi/tdiutil",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"idx/layout/TitlePane",
	"idx/form/Link",
	"tdi/NlsMixin",
	"dojo/text!./templates/ALEditor.html"
], function(declare, array, lang, html, fx, aspect, domAttr, domClass, domStyle, dndMove, _Widget, _TemplatedMixin, _WidgetsInTemplate, BorderContainer, ContentPane,
		HorizontalSlider, Select, DropDownButton, NumberSpinner, TextBox,
		Dialog, Toolbar, TooltipDialog, Button, StackContainer, popup, Calendar, dTopic, Gfx, GfxUtils, idxDialog, ALSchedule, tdiapi, tdiconfig, 
		tdicontainer, ALAttributeMap, ALButton, ALComponent, ALComponent2, ALComps, ALConnection, ALConnectorEditor, ALDataCollector,
		ALDebugger, ALDropPoint, ALEditorLayout, ALInitParams, ALPlaceholder, TDI, Border, TDIJavascriptEditor, 
		TDIAttributeMap, TDIAttributeMapItemEditor, TDIConnectorEditor, TDILinkCriteria, ComponentsModel, AttributeLoop, FilteredLogViewer,
		BranchEditor, LinkCriteriaWidget, ToolbarLabel, HooksWidget, TableWidget, tdifeed, tdiutil, idx, HeaderPane, TitlePane, Link, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, nls ],
	{
		templateString : template,
		
		// options: Object
		options: {
			hideFeed:false
		},
		
		// Config file label
		configlabel: "(Default)",
		
		// componentOffsetX: Offset for components left edge
		attributeOffsetX: 230,
		
		// componentOffsetX: Offset for components left edge
		componentOffsetX: 450,
		
		// components: Array of components
		components: null,
		
		// counter: Used to name new components (initial value)
		counter: 1,
		
		// runalId: Editor id for run assemblyline display
		runalId: "- Run AssemblyLine",
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.startOffset = {x:this.componentOffsetX, y:15, h:30};
			this.componentSize = {
				width:200,
				height:60,
				rowSpacing:30
			};
			this.componentOffset = this.startOffset.y + this.startOffset.h + this.componentSize.rowSpacing;
			this.stopOffset = {y:this.componentOffset, x:this.componentOffsetX + this.componentSize.width + this.componentSize.rowSpacing, h:30};
			this.components = new Array();
			this.editorLayouts = new Object();
		},
		
		getConfig : function() {
			// summary:
			//		Returns the TDI configuration object
			return this.config;
		},
		
		getAssemblyLineConfig : function() {
			// summary:
			//		Returns the AssemblyLine configuration object we're editing
			return this.config.getAssemblyLine(this.assemblyline);
		},
		
		highlightBreakpoint: function(status, entry) {
			// summary:
			//		Highlight the component named by status.component
			//
			if(this.focusRect) {
				fx.animateProperty({
					node:this.focusRect.domNode,
					duration: 100,
					properties: {
						"border-width": 1,
						"border-color": TDI.textColor
					}
				}).play();
			}
			
			if(!status)
				return;
			
			var arr = array.filter(this.components, function(comp) {
				return comp.getId() == status.component;
			});
			
			if(arr && arr.length == 1) {
				var comp = arr[0];
				
				this.focusRect = comp;
				fx.animateProperty({
					node:comp.domNode,
					duration: 100,
					properties: {
						"border-width": 3,
						"border-color": TDI.selectionColor
					}
				}).play();
			}
		},
		
		fadeIn : function(target) {
			// summary:
			//		Fade in by playing opacity to zero 
			html.style(target.domNode, "opacity", "0");
			fx.fadeIn({
				node:target.domNode,
				duration:350
			}).play();
		},
		
		selectCollectorFor : function(comp) {
			// summary:
			//		Fades in the ALDataCollector UI for a specific component
			
			var target = this.alDataCollectorsCP[comp];
			
//			this.editorStack.selectContainerPane(this.runalId);
			
			if(target) {
				var oldtarget = this.alDataCollector.selectedChildWidget;
				if(oldtarget) {
					fx.fadeOut({
						node:oldtarget.domNode,
						duration:350,
						onEnd: lang.hitch(this, function() {
							this.alDataCollector.selectChild(target, true);
							this.fadeIn(target);
						})
					}).play();
				} else {
					this.alDataCollector.selectChild(target, true);
					this.fadeIn(target);
				}
			}
		},
		
		startAssemblyLine: function(breakOnComp) {
			// summary:
			//		Launches a temporary configuration with the current assemblyline in debug mode.
			if(!this.alDebugger) {
				
				// -- Make sure any editors etc are removed
				this.clearPopups();

				this.alDebugger = new ALDebugger({config:this.config, assemblyline:this.assemblyline, breakOnComponents:breakOnComp});
				
				//
				// -- Handle debug break by updating visuals to reflect current state of the AL
				//
				this.alDebugger.onDebugBreak = lang.hitch(this, function(status) {
					
					// -- switch to correct component
					this.selectCollectorFor(status.component);
					
					// TODO: Fix DebugClient on server to return proper error messages
					// error messages uses "java" as component
					if(status.component == "java" && status.breakpoint && status.breakpoint.match(/Exception/)) {
						idx.error(status.breakpoint)
					}
					
					// -- if it has a "conn" entry to display 
					var entry = status.watch[status.component+".lastConn"];
					if(entry && this.alDataCollectorsCP[status.component]) {
						this.alDataCollectors[status.component].displayData(entry, status.watch["work"]);
					}
					
					// -- show selection border on current component
					this.highlightBreakpoint(status, entry);
					
					// -- update work entry attribute list
					this.workEntry.displayData(status.watch["work"]);

					this.enableButtons(false);
				});
				
				//
				// -- Cleanup when AL terminates
				//
				this.alDebugger.onDebugTerminate = lang.hitch(this, function(status) {
					this.stopAssemblyLine();
				});
				
				//
				// -- Open the log viewer for the config log
				//
				this.alDebugger.onConfigStarted = lang.hitch(this, "openLogViewer");
				this.alDebugger.startDebugger();

				//
				// -- These objects hold the ALDataColletcor instances for each component
				//
				this.alDataCollectors = new Object();
				this.alDataCollectorsCP = new Object();
				
				//
				// -- Stack container to display ALDataCollector instances
				//
				this.alDataCollector = new StackContainer({
					style:"width:100%; height:100%;",
					"class":"dijitReset"
				});
				dojo.style(this.alDataCollector.domNode, {
					width:"350px",
					height:"500px",
					top:"10px",
					left:"10px",
					position:"absolute",					
					"border-width":"1px",
					"border-style":"solid",
					"background-color":"white"
				});
				this.alDataCollector.placeAt(this.surfaceDiv);
				
				//
				// -- Create an ALDataCollector instance for each component in the AL
				//
				var alconfig = this.getAssemblyLineConfig();
				dojo.forEach(alconfig.getComponentNames(true), function(comp) {
					var cc = alconfig.getComponentByName(comp);
					this.alDataCollectors[comp] = new ALDataCollector({config:cc, runtime:true});
					this.alDataCollectors[comp].startup();
					var cp = new ContentPane({
						title:comp,
						style:{
							margin:0,
							padding:0
						},
						content:this.alDataCollectors[comp]
					});
					this.alDataCollector.addChild(cp);
					this.alDataCollectorsCP[comp] = cp;
				}, this);
				this.alDataCollector.startup();
				this.alDataCollector.resize();
				
//				this.editorStack.removeContainerPane(this.runalId);
//				this.editorStack.addContainerPane(this.alDataCollector, {title:this.runalId});
				this.setSelectedItem(null);
				
			} else {
				this.alDebugger.runContinue();
			}
			this.enableButtons(true);
		},
		
		openLogViewer: function(cientry) {
			// summary:
			//		Create a FilteredLogViewer to display log messages posted on
			//		the queue defined by this cientry's listener URL.
			//
			dojo.when(cientry.getLinkData("listener"), function(data) {
				return new tdifeed({feed:data});
			}, tdiapi.defaultErrHandler)
			.then(dojo.hitch(this, function(feed) {
				var listener = feed.getEntry("listener");
				if(this.alLogWidget) {
					this.alLogWidget.destroy();
				}
				
				this.alLogWidget = new FilteredLogViewer({url:listener.getLink("poll").href});
				
				var hp = new HeaderPane({
					content:this.alLogWidget,
					title:"AssemblyLine Log",
					closable:true,
					onClose:lang.hitch(this, "closeLogPane"),
					style:{
						margin:0,
						padding:0,
						width:"100%",
						height:"100%"
					}
				});
				
				var tb = new TextBox({
					style:"width:20em",
					region:"minorActions"
				});
				hp.addChild(tb);
				
				var butt = new Button({
					label:">>",
					onClick:lang.hitch(this, "_evalExpression", tb),
					region:"minorActions"
				})
				hp.addChild(butt);
				
				
				this.openLogPane(hp);
			}), tdiapi.defaultErrHandler);
		},
		
		_evalExpression: function(tbox) {
			alert("Eval: " + tbox.get("value"));
		},
		
		setSelectedItem: function(item) {
			// summary:
			//		Marks item as the current selection
			if(this.selectedItem) {
				this.selectedItem.setSelected(false);
			}
			this.selectedItem = item;
			if(item) {
				item.setSelected(true);
			}
		},
		
		clearPopups: function(newSelection) {
			// summary:
			// 		Clears any popup displays on the page
			array.forEach(this.components, function(comp) {
				if(comp.clearPopups)
					comp.clearPopups();
			});
			if(this._popupEditor) {
				this.surfaceDiv.removeChild(this._popupEditor.domNode)
				this._popupEditor = null;
			}
			this.clearAttributeMap();
			this.closeNavPopup();
			popup.close();
		},

		stopAssemblyLine: function() {
			// summary:
			//		Stops the running debugger/assemblyline
			if(this.alDebugger) {
				this.alDebugger.stopDebugger();
				this.alDebugger = null;
				if(this.alDataCollector) {
					this.alDataCollector.destroyRecursive();
					this.alDataCollector = null;
				}
			}
			if(this.alLogWidget) {
				this.alLogWidget.stop();
			}
			this.clearPopups();
			this.highlightBreakpoint(null);
			this.setSelectedItem(null);
			this.setNavBarPane(false);
			this.closeLogPane();
		},
		
		openConfigPage: function(comp, event, tab) {
			// summary:
			//		Opens the UI page for the given component
			var editor = this.getEditorFor(comp, tab);
			var alconfig = this.config.getAssemblyLine(this.assemblyline);
			var compconfig = comp.config; // alconfig.getComponentByName(comp.getId());
			
			if(!compconfig || !compconfig.isConnector) {
				return;
			}
			
			// -- remove current editors
			this.clearPopups(comp);
			
			// -- update selection
			this.setSelectedItem(comp);
			
			// -- show asscoiated attribute map
//			this.showAttributeMap(comp.config);
			
			if(editor) {
				;
			} else if(compconfig.isConnector()) {
				
				editor = new ALConnectorEditor({
					config:compconfig
				});
				
			} else if (compconfig.isScript()) {
				editor = new TDIJavascriptEditor({config:compconfig, autoUpdate:true});
				
			} else if (compconfig.isBranch()) {
				editor = new BranchEditor({config:compconfig, title:compconfig.getSubType() + ": " + compconfig.getName()});
				
			} else if (compconfig.isLoop()) {
				switch(compconfig.getBranchConfig().getBranchType()) {
				case "AttributeLoop":
					editor = new AttributeLoop({
						config:compconfig.getBranchConfig().getAttributeConfig(),
						title:compconfig.getName()
					});
					break;
				case "ConnectorLoop":
					editor = new TDIConnectorEditor({
						config:compconfig.getBranchConfig().getConnectorConfig(), 
						title:"Loop: " + compconfig.getName()
					});
					break;
				case "WhileLoop":
					editor = new BranchEditor({
						config:compconfig, 
						title:compconfig.getSubType() + ": " + compconfig.getName()
					});
					break;
				default:
				}
			}
			
			if(editor) {
				if(!this.getEditorFor(comp, tab)) {
					html.style(editor.domNode, {
						width:"100%",
						height:"100%",
						position:""					
					});
					//
					// -- place editor inside a content pane so we can add titles/tools etc
					//
					editor = this.wrapEditor(editor, comp.getId() + (tab ? " - " + tab : ""), comp.getId());
					this.setEditorFor(comp, tab, editor);
				}
				this.popupEditor(editor);
			}
		},

		clearAttributeMap: function() {
			if(this._attributeEditor) {
				this.surfaceDiv.removeChild(this._attributeEditor.domNode)
				this._attributeEditor = null;
			}
			this.closeAttributeItemEditor();
		},
		
		showAttributeMap: function(comp, uicomponent) {
			// -- If not a component w/attmap remove any on screen
			if(!comp || (!comp.isConnector() && !comp.isFunction())) {
				return;
			}
			
			this.clearAttributeMap();
			
			// -- If already displayed do nothing
			var id = comp.getName() + " - " + comp.getAttributeMap().getName();
			
			var editor = this.getEditorFor(id);
			if(!editor) {
				var t = this;
				// -- Create a new editor for the map and display
				var map = new TDIAttributeMap({
					sourceObject:comp.getAttributeMap().getName() == "Input" ? "conn" : "work",
					toolbar:false,
					connectButtons:true
				});
				editor = this.wrapEditor(map, id, id);
				
				map.set("config", comp.getAttributeMap());
				map.connect(map.mapGrid, 'onRowClick', function(evt) {
					if(evt.rowId) {
						t.showAttributeEditor(editor, comp.getAttributeMap(), evt.rowId);
					}
				});
			}
			
			var pos = dojo.contentBox(this.surfacePane.domNode);
			var npos = dojo.marginBox(uicomponent ? uicomponent.domNode : this.selectedItem.domNode);
			var selLeft = npos.l;
			npos.t = this.startOffset.y;
			npos.l = 10;
			npos.height = pos.h - (npos.t*2); //400;
			if(uicomponent)
				npos.width = selLeft + npos.w - (2*npos.l);
			else
				npos.width = selLeft - (uicomponent ? 0 : (npos.l*2));
			
			dojo.style(editor.domNode, {
				width:npos.width + "px",
				height:npos.height + "px",
				top:npos.t + "px",
				left:npos.l + "px",
				position:"absolute",					
				"border-width":"1px",
				"border-style":"solid",
				"background-color":"white"
			});
			editor.placeAt(this.surfaceDiv);
			if(editor)
				editor.resize();

			this._attributeEditor = editor;
			
			this.setEditorFor(id, editor);
		},
		
		closeAttributeItemEditor: function() {
			// summary:
			//		Removes the attribute editor from screen
			// 		and updates the configuration with current values.
			// return: string
			//		editor id of closed editor or null if nothing closed
			if(this._attributeItemEditor) {
				var editorId = this._attributeItemEditor.editorId;
				var mapEditor = this._attributeItemEditor.mapEditor;
				if(mapEditor) {
					var config = mapEditor.get("config");
					var value = mapEditor.get("value");
					for(var key in value) {
						if(config.config[key])
							config.config[key] = value[key];
					}
					config.setModified(true);
				}
				this.surfaceDiv.removeChild(this._attributeItemEditor.domNode)
				this._attributeItemEditor.destroyRecursive();
				this._attributeItemEditor = null;
				return editorId;
			}
			return null;
		},
		
		showAttributeEditor: function(parent, map, name) {
			// summary:
			//		Display onscreen editor for an attribute item
			var id = map.getName() + "." + name;
			
			// -- close existing editor and immediatly return
			// -- if user has clicked it away (e.g. click on same item)
			if(this.closeAttributeItemEditor() == id) {
				return;
			}
			
			var mapEditor = new TDIAttributeMapItemEditor({
				sourceObject:map.getName() == "Input" ? "conn" : "work"
			});
			var config = map.getItem(name);
			var item = tdiutil.clone(config.config);
			mapEditor.set("value", item);
			mapEditor.set("config", config);
			var editor = this.wrapEditor(mapEditor, name, id);
			editor.mapEditor = mapEditor;
			
			var pos = dojo.marginBox(parent.domNode);
			pos.l = pos.l + pos.w + 3 + "px";
			pos.width = "500px";
			pos.height = "300px";
			dojo.style(editor.domNode, {
				width:pos.width,
				height:pos.height,
				top:pos.t + "px",
				left:pos.l,
				position:"absolute",					
				"border-width":"1px",
				"border-style":"solid",
				"background-color":"white"
			});
			editor.resize();
			editor.startup();
			editor.placeAt(this.surfaceDiv);
			this._attributeItemEditor = editor;
		},
		
		wrapEditor: function(content, title, id) {
			// summary:
			//		Places editor content in a content pane with
			//		special handling to min/max etc
			var border = new Border({
				style:{
					margin:0,
					padding:0,
					zIndex:99
				},
				params:{
					style:"width:100%; height:100%; margin:0; padding:0",
				},
				gutters:false,
				editorId:id
			});
			border.setCenter(content);
			
			// make sure content overlays main page contents
			domStyle.set(content.domNode, "zIndex", 100);
			
			var hdr = new ContentPane({
				style:{
					width:"100%",
					
				},
				"class":"tdiWebDevNavPane"
			});
			hdr.addChild(new ToolbarLabel({label:title, "class":"tdiWebDevTitle"}))
			border.setTop(hdr);
			
			if(content && content.customizeWrappedEditor) {
				content.customizeWrappedEditor(border, title);
			}
			
			return border;
		},
		
		popupEditor: function(editor) {
			// summary:
			//		Places the editor on the surfaceDiv canvas.
			//		The editor either grabs all space on the surface or it
			//		displays around the component's domnode in a 600x400 pixel view.
			
			var pos = dojo.contentBox(this.surfacePane.domNode);
			
			if(!this._maxView) {
				var npos = this.selectedItem ? dojo.marginBox(this.selectedItem.domNode) : 
					{
						t:pos.t + 10,
						l:pos.w - 610,
						w:0
					};
					
				npos.t = this.startOffset.y;
				npos.h = pos.h - (npos.t*2);
				dojo.style(editor.domNode, {
					width:"600px",
					height:npos.h+"px",
					top:(npos.t || npos.y) +"px",
					left:(npos.l || npos.x) + 10 + npos.w +"px",
					position:"absolute",					
					"border-width":"1px",
					"border-style":"solid",
					"background-color":"white"
				});
				
			} else {
				pos.l += 5;
				pos.t += 5;
				pos.w -= 10;
				pos.h -= 10;
				
				dojo.style(editor.domNode, {
					width:pos.w+"px",
					height:pos.h+"px",
					top:pos.t+"px",
					left:pos.l+"px",
					position:"absolute",					
					"border-width":"1px",
					"border-style":"solid",
					"background-color":"white"
				});
			}
			
			editor.placeAt(this.surfaceDiv);
			if(!editor._started)
				editor.startup();
			
			if(editor.resize)
				editor.resize();
			if(editor.layout)
				editor.layout();
			
			editor.domNode.scrollIntoView();
			
			this._popupEditor = editor;
		},
		
		toggleMaxView: function() {
			this._maxView = !this._maxView;
			this.popupEditor(this._popupEditor);
		},
		
		getEditorFor: function(comp, tab) {
			// summary:
			//		Returns the editor widget for a given component/tab combo
			this._editors = this._editors || new Object();
			if(lang.isString(comp))
				return this._editors[comp];
			else
				return this._editors[comp.getId() + "_" + (tab ? tab : "") ];
		},
		
		setEditorFor: function(comp, tab, editor) {
			// summary:
			//		Returns the editor widget for a given component/tab combo
			this._editors = this._editors || new Object();
			if(lang.isString(comp))
				this._editors[comp] = editor;
			else
				this._editors[comp.getId() + "_" + (tab ? tab : "") ] = editor;
		},
		
		addComponent: function(connection, layout) {
			this.showComponentsList();
		},
		
		resize: function(newsize) {
			if(this.borderContainer) {
				this.borderContainer.resize(newsize);
			}
			this.doLayoutComponents();
		},
		
		doLayoutComponents: function() {
			// summary:
			//		Computes and sets the bounding box for each layout component
			var t = this;
			var size = {x:0, y:0};
			
			var layout = this.topLayout;
			var bb = layout.getBoundingBox();
			var prevSibling = null;
			
			while(layout) {
				prevSibling = layout;
				layout = layout.nextSibling;
			}
			prevSibling.setNextSibling(this.stopBulletLayout);
			
			var layout = this.topLayout;
			
			if(layout) {
				bb.y += bb.height;
				layout = layout.nextSibling;
				while(layout) {
					this._layoutChild(layout, bb, size, prevSibling);
					prevSibling = layout;
					layout = layout.nextSibling;
					bb.y = size.y;
				}
			}
			
			
			var pos = dojo.contentBox(this.surfacePane.domNode);
			if(size.y < pos.h)
				size.y = pos.h;
			if(size.x < pos.w)
				size.x = pos.w;
			
			// update attribute maps
			this.createAttributeMaps();
			
			this.surfaceTop.setDimensions(size.x, size.y);
			this.surfacePane.resize();
		},
		
		_layoutChild: function(layout, prev, size, prevSibling) {
			// summary:
			//		Sets the bounding box for layout based on prev box
			//		and lays out children if layout is expanded/visible.
			var t = this;
			
			
			// We center around a mid-axis
			var center = prev.x + (prev.width / 2);

			// vertical space between components
			var rowSpacing = this.componentSize.rowSpacing;
			
			// Current bounding box
			var bb = layout.getBoundingBox();
			
			// Compute new location for layout based on what previous layout grabbed
			bb.y = prev.y + prev.height + rowSpacing;
			bb.x = center - (bb.width / 2);
			layout.setBoundingBox(bb);

			// Update size if we go beyond the current size
			if((bb.x+bb.width) > size.x) {
				size.x = bb.x+bb.width;
			}
			if((bb.y+bb.height) > size.y) {
				size.y = bb.y+bb.height;
			}
			
			// refresh connections
			layout._refreshConnections();
			
			// layout children
			if(layout.expanded && layout.hasChildren()) {
				bb.x += bb.width + (bb.width*0.5);
				bb.y -= (bb.height + rowSpacing);
				prevSibling = null;
				array.forEach(layout.getChildren(), function(layout) {
					t._layoutChild(layout, bb, size, prevSibling);
					bb.y = size.y;
					prevSibling = layout;
				})
			}
		},
		
		createStart: function() {
			var left = this.componentSize.width/2
			var style = {
				position:"absolute",
				left:this.startOffset.x +"px",
				top:this.startOffset.y+"px",
				width:this.componentSize.width/2 + "px",
				height:"20px"
			};
			this.startBullet = new ALPlaceholder({
				style:style,
				title:"Start"
			}).placeAt(this.surfaceDiv);
			this.components.push(this.startBullet);
			
			this.callBullet = new ALPlaceholder({
				style: {
					position:"absolute",
					left:"5px",
					top:(this.startOffset.y + 15) + "px",
					width:"0px",
					height:"0px",
					display:"none"
				},
				title:""
			}).placeAt(this.surfaceDiv);
			
			return this.addLayoutComponent(this.startBullet);
		},
		
		createStop: function(sibling) {
			// summary:
			//		Creates the two stop components that always trail the assemblyline
			var style = {
				top:"0px",
				left:"0px",
				width:(this.componentSize.width/2) + "px",
				height:"20px"
			};
			
			this.stopBullet = new ALPlaceholder({
				style:style,
				title:"End"
			}).placeAt(this.surfaceDiv);
			this.components.push(this.stopBullet);
			
			this.replyBullet = new ALPlaceholder({
				style: {
					position:"absolute",
					left:"5px",
					top:(this.startOffset.y + 15) + "px",
					height:"0px",
					width:"0px",
					display:"none"
				},
				title:""
			}).placeAt(this.surfaceDiv);

			return this.addLayoutComponent(this.stopBullet, sibling);
		},
		
		addSubComponents: function(container, parent, sibling) {
			// summary:
			//		Adds layout components for entries in container.
			// parent:
			//		The parent layout
			// sibling:
			//		The previous sibling (e.g. parent == null)
			var count = container.getComponentCount();
			var layout = null;
			var prevSibling = sibling;
			
			for(var i = 0; i < count; i++) {
				var config = container.getComponentAt(i);
				var comp = this.createComponent(config, parent);
				
				layout = this.addLayoutComponent(comp, prevSibling, parent);
				if(parent) {
					parent.addChild(layout);
				}
				
				// Next time around this layout becomes the new layout's prevSibling
				prevSibling = layout;
				
				if(config.isContainer && config.isContainer()) {
					this.addSubComponents(config, layout);
				}
			}
			
			if(count == 0) {
				var placeholder = container.getPlaceholder();
				var comp = this.createComponent(placeholder);
				layout = this.addLayoutComponent(comp, sibling);
			}
		},
		
		createAttributeMaps: function() {
			// summary:
			//		Add/remove attribute maps for every component in
			//		the assemblyline. Attribute maps appear to the left
			//		with a dotted connector line to/from the associated component.
			//		This method should be called after doLayoutComponents has
			//		positioned the ALEditorLayout objects.
			for(var key in this.editorLayouts) {
				var layout = this.editorLayouts[key];
				var config = layout.getConfig ? layout.getConfig() : null;
				if(config && (config.isConnector() || config.isFunction()) ) {
					var box = layout.getBoundingBox();
					box.width = (box.x - this.attributeOffsetX) - 3;
					box.x = this.attributeOffsetX;
					this._createAttributeMap(config, box);
				}
			}
			
			this.createALInputOutputMaps();
		},
		
		createALInputOutputMaps: function() {
			//
			// -- Update assemblyline input/output map (schema)
			//
			var def = this.getAssemblyLineConfig().getOperation("Default");
			if(!def) {
				return;
			}

			var structure = [
			    {field:"name", id:"name", name:"Input Attribute", width:"auto"}
			];
			
			var inp = def.getSchema(true);
			var arr = [];
			array.forEach(inp.getNames(), function(attr) {
				arr.push({id:attr, name:attr});
			});
			this.alInpAttr = new TableWidget({
				style:"position:absolute; top:50px; border:1px solid black; left:10px; width:200px; height:300px",
				structure:structure
			});
			this.alInpAttr.placeAt(this.surfaceDiv);
			this.alInpAttr.setData(arr);
			this.alInpAttr.startup();
			this.alInpAttr.resize({w:200, h:300});
			
			var out = def.getSchema(false);
			var arr = [];
			array.forEach(out.getNames(), function(attr) {
				arr.push({id:attr, name:attr});
			});
			
			structure = [
 			    {field:"name", id:"name", name:"Output Attribute", width:"auto"}
 			];
			this.alOutAttr = new TableWidget({
				style:"position:absolute; top:450px; border:1px solid black; left:10px; width:200px; height:300px",
				structure:structure
			});
			this.alOutAttr.placeAt(this.surfaceDiv);
			this.alOutAttr.setData(arr);
			this.alOutAttr.startup();
			this.alOutAttr.resize({w:200, h:300});
		},
		
		_createAttributeMap: function(config, box) {
			// summary:
			//		Create/update attribute map for a config
			var t = this;
			var map = config.getAttributeMap();
			var widget = this.attributeWidgets[config.getName()];
			if(!config.isEnabled()) {
				if(widget) {
					widget.destroyRecursive();
					this.attributeWidgets[config.getName()] = null;
				}
				return null;
			}
			var layout = this.editorLayouts[config.getName()];
			if(!widget) {
				widget = this.attributeWidgets[config.getName()] = new ALAttributeMap({
					map:map,
					parent:this,
					enabled:config.isEnabled()
				});
				widget.placeAt(this.surfaceDiv);
			}
			widget.setBoundingBox(box);
			return widget;
		},
		
		updateAttributeMap: function(name, entry, work) {
			// summary:
			//		Updates the attribute map for name by populating
			//		the map with values from entry.
//			if(!this.stepAttributeMap) {
//				this.stepAttributeMap = ;
//			}
			if(this.currentAttributeMap) {
				this.currentAttributeMap.widget.setBoundingBox(this.currentAttributeMap.oldbox);
			}
			
			var widget = this.attributeWidgets[name];
			if(widget) {
				var arr = [];
				for(var attr in entry) {
					arr.push("<div class='tdiFormLabel'>" + attr + "</div>");
					arr.push("<div class='tdiFormValue'>" + entry[attr] + "</div>");
				}
				widget.set("content", arr.join(""));
				
				var oldbox = widget.getBoundingBox();
				var newbox = widget.getBoundingBox();
//				newbox.x -= 100;
//				newbox.width += 100;
				newbox.height += 100;
				
				this.currentAttributeMap = {
					widget:widget,
					oldbox:oldbox
				};
				this.currentAttributeMap.widget.setBoundingBox(newbox);
			}
		},
		
		createComponents: function(sibling) {
			// summary:
			//		Creates ALEditorLayout objects for all components in the assemblyline
			// returns:
			//		The last layout object added
			var layout = sibling;
			var al = this.config.getAssemblyLine(this.assemblyline);
			if(al) {
				var comp = null;
				
				// -- Add entryfeed component or empty one
				if(!this.options.hideFeed) {
					var input = al.getEntryFeedComponent();
					if(input.getComponentCount() > 0) {
						comp = input.getComponentAt(0);
					} else {
						comp = al.createConnector("(main feed)", "Iterator");
						comp.setEnabled(false);
					}
					
					// -- Create UI component
					comp = this.createComponent(comp);
					
					// -- Create LayoutComponent
					layout = this.addLayoutComponent(comp, sibling);
				}
				
				// -- Add dataflow components (as a sibling to the DataFeed component)
				this.addSubComponents(al.getDataFlowComponent(), null, layout);
			}
			
			return layout;
		},
		
		createComponent: function(comp) {
			var style = {
					top:"0px",
					left:"0px",
					width:this.componentSize.width + "px",
					height:this.componentSize.height + "px",
					zIndex:99
			};
			var component = new ALComponent2({
				config:comp,
				style:style,
				onClick: lang.hitch(this, "openConfigPage"),
				onDelete:lang.hitch(this, "onDeleteComponent"),
				onCopy:lang.hitch(this, "onCopyToLibrary")
			}).placeAt(this.surfaceDiv);
			
			this.components.push(component);
			return component;
		},
		
		onInsertComponent: function(event) {
			this.addComponent(event.target);
		},
		
		_deleteComponentByName: function(name) {
			var t = this;
			var cfg = this.config.getAssemblyLine(this.assemblyline).getComponentByName(name);
			var comp = this.editorLayouts[name];
			if(cfg && name) {
				tdiutil.confirm("Delete " + cfg.getName(), function(status) {
					if(status == 0) {
						cfg.getParent().deleteComponent(cfg.getName());
						t.removeLayoutComponent(comp);
						t.doLayoutComponents();
					}
				});
			}
		},
		
		onDeleteComponent: function(comp) {
			var cfg = comp.config;
			this._deleteComponentByName(cfg.getName());
		},
		
		onCopyToLibrary: function(comp) {
			var t = this;
			var cfg = comp.config;
			tdiutil.prompt("Enter name:", function(arr) {
				var clone = tdiutil.clone(cfg.config);
				if(cfg.isConnector() || cfg.isFunction()) {
					comp.config.getTop().addConnector(arr[0], clone);
				} else if(cfg.isScript()) {
					comp.config.getTop().addScript(arr[0], clone);
				}
				this.alLibrary.reload();
			}, "Copy component to Resources");
		},
		
		onDrop: function(comp, event) {
			var str = "";
			var type = "";
			
			for(f in event.selection) {
				str = event.selection[f].item.id[0];
				type = event.selection[f].item.type[0];
			}
			
			if(str) {
				if(comp.config.isPlaceholder) {
					comp.config = comp.config.getParent().createComponent(type);
				}
				if(type == "@Connection") {
					comp.setComponentType(str);
				} else if(type == "@Script") {
				}
				if(this.editor) {
					this.editor.destroyRecursive();
				}
				this.editor = new TDIConnectorEditor({config:comp.getConfig()});
				this.configPane.set("content", this.editor);
			}
		},

		onDropAdd: function(layout, item, connection) {
			// summary:
			//		Insert item into the assemblyline based on connection's location
			
			var str = item.id[0];
			var type = item.type[0];
			var al = this.config.getAssemblyLine(this.assemblyline);
			var target = null;
			var config = null;
			var before = false;
			
			// Where to insert
			if(connection.getTarget && connection.getTarget().isComponent) {
				config = connection.getTarget().config;
				before = true;
			} else if (connection.getSource && connection.getSource().isComponent) {
				config = connection.getSource().config;
			} else {
				return;
			}
			
			target = al.getComponentByName(config.parentConfig.getName());
				
			var conn = null;
			
			switch(type) {
			case "@If":
			case "@Else":
			case "@ElseIf":
			case "@Switch":
			case "@Case":
			case "@While":
				var cfg = {
					"@type":"composite",
					compositeConfig: {
						"@type":"branch",
						type:type.substring(1),
						condition: {
							item: []
						}
					},
					name:str+(this.counter++),
					state:"Enabled",
					component:[]
				};
				conn = new tdicontainer({config:cfg});
				break;
			case "@Script":
				conn = al.createScriptComponent(str+(this.counter++), "//\ntask.dumpEntry(work)\n");
				break;
			case "@AttrLoop":
				break;
			case "@ConnLoop":
				break;
			default:
				conn = al.createConnector(str.replace(".",""), "AddOnly");
				conn.setInheritFrom("system:/Connectors/" + str);
			}
			if(target.isContainer && target.isContainer()) {
				target.addComponent(conn, config.getName(), before);
				var layout = this.editorLayouts[config.getName()]
				if(layout) {
					var comp = this.createComponent(conn);
					if(before) {
						var nlayout = this.addLayoutComponent(comp, layout.prevSibling, layout.parent);
						if(layout.prevSibling)
							layout.prevSibling.setNextSibling(nlayout);
						nlayout.setNextSibling(layout);
					} else {
						var nlayout = this.addLayoutComponent(comp, layout, layout.parent);
						nlayout.setNextSibling(layout.nextSibling);
					}
				}
			} else {
				alert("Insert at non-container point");
			}
			
			this.doLayoutComponents();
		},
		
		openConfig: function(configentry, configlabel, assemblyline, config) {
			this.configentry = configentry;
			this.configlabel = configlabel;
			this.assemblyline = assemblyline;
			
			tdiutil.addRecentFiles(configlabel, assemblyline);
			
			if(!config) {
				dojo.when(tdiapi.getConfig(configentry), lang.hitch(this, function(data) {
					this.config = new tdiconfig({
						config:data
					});
					this.buildGfx();
				}));
			} else {
				this.config = config;
				this.buildGfx();
			}
		},
		
		addLayoutComponent: function(comp, prevSibling, parent) {
			// summary:
			//		Adds a UI component to the layout component chain
			var layout = new ALEditorLayout({
				component:comp,
				prevSibling:prevSibling,
				parent:parent,
				surface:this.surface,
				onDropAdd:lang.hitch(this, "onDropAdd"),
				onInsertComponent:lang.hitch(this, "addComponent")
			});
			
			if(prevSibling) {
				prevSibling.setNextSibling(layout);
			}
			
			// 
			this.editorLayouts[comp.getId()] = layout;

			comp._layoutComponent = layout;
			return layout;
		},
		
		removeLayoutComponent: function(layout) {
			// summary:
			//		Removes a UI component from the layout component chain
			if(!layout) {
				return;
			}
			
			// hide popups
			this.clearPopups();

			// Destroy this layout and its children
			layout.destroy();
			
			// 
			this.components = array.filter(this.components, function(c) {
				return c !== layout.component;
			});
		},
		
		layoutChildren: function() {
			this.doLayoutComponents();
		},
		
		
		buildGfx: function() {
			this.surfaceTop.clear();
			array.forEach(this.components, function(comp) {
				if(comp.destroyRecursive)
					comp.destroyRecursive();
			})
			this.components = [];
			
			this.surface = this.surfaceTop.createGroup({
				width:this.surfaceTop.getDimensions().width,
				height:this.surfaceTop.getDimensions().height,
			});
			this.surfaceTop.connect("onclick", lang.hitch(this, function(args) {
				this.setSelectedItem(null);
				this.clearPopups();
			}));
			
			this.components = [];
			
			this.topLayout = this.createStart();
			var sibling = this.createComponents(this.topLayout);
			this.stopBulletLayout = this.createStop(null);
		},
		
		getLastComponent: function() {
			return this.components[this.components.length-1];
		},
		
		enableButtons: function(running) {
			this.startButton.set("label", running ? "Pause" : "Run");
			this.nextButton.set("disabled", running);
			this.stopButton.set("disabled", false);
		},
		
		createToolbar : function(region, container) {
			this.toolbar = new Toolbar({style:"width:100%"});
			if(container) {
				this.topPane = new ContentPane({
					region:region,
					splitter:false,
					style:"margin:0; padding:0",
					content:this.toolbar
				});
				container.addChild(this.topPane);
			}
			
			this.startButton = new Button({
				label:"Run",
				onClick:lang.hitch(this, "startAssemblyLine", false)
			});
			this.toolbar.addChild(this.startButton);
			
			this.nextButton = new Button({
				label:"Step",
				onClick:lang.hitch(this, "startAssemblyLine", true)
			});
			this.toolbar.addChild(this.nextButton);			
			
			this.stopButton = new Button({
				label:"Stop",
				disabled:true,
				onClick:lang.hitch(this, "stopAssemblyLine")
			});
			this.toolbar.addChild(this.stopButton);
			
			this.toolbar.addChild(new Button({
				label:"Add Component",
				onClick:lang.hitch(this, "showComponentsList")
			}));
			
			this.toolbar.startup();
			
			return this.toolbar;
		},
		
		showComponentsList: function() {
			// summary:
			//		Shows the components popup
			this.showPopup(this.alComps, this.addComponentButton);
		},
		
		showFlowPopup: function() {
			// summary:
			//		Shows the components popup
			this.showPopup(this.alFlow, this.flowComponentButton);
		},
		
		showLibraryPopup: function() {
			// summary:
			//		Shows the components popup
			this.showPopup(this.alLibrary, this.libraryButton);
		},
		
		showProjectsPopup: function() {
			// summary:
			//		Shows the projects dialog popup
			this.showPopup(this.projects, this.projectsButton);
		},
		
		showAlInitParamsPopup: function() {
			// summary:
			//		Shows the projects dialog popup
			if(!this.alInitParams)
				this.createAlInitParams();
			this.showPopup(this.alInitParams, this.projectsButton);
		},
		
		showPopup: function(comp, parent, box) {
			var size = {w:380, h:300};
			if(box)
				size = lang.mixin(size, box);
			if(!this.closeNavPopup(comp)) {
				comp.resize(size);
				this._popupOpts = {
					parent:parent,
					popup:comp,
					around:parent.domNode,
					orient:["after", "above", "below"]
				}
				popup.open(this._popupOpts);
				comp.resize(size);
			}
		},

		closeNavPopup: function(comp) {
			var ret = false;
			if(this._popupOpts) {
				popup.close(this._popupOpts.popup);
				ret = this._popupOpts.popup == comp;
				this._popupOpts = null;
			}
			return ret;
		},
		
		showALSchedule: function() {
			if(!this.alSchedule) {
				this.alSchedule = new ALSchedule({});
				this.alSchedule.setConfig(this.getAssemblyLineConfig());
				var hp = new HeaderPane({
					title:"AsesmblyLine Schedule",
					content:this.alSchedule
				});
				this.editorStack.addContainerPane(hp, {title:"%%alschedule%%"});
			}
			this.editorStack.selectContainerPane("%%alschedule%%");
		},
		
		createAlInitParams: function() {
			if(!this.alInitParams) {
				this.alInitParams = new ALInitParams({});
				var hp = new HeaderPane({
					title:"AssemblyLine Parameters",
					content:this.alInitParams
				});
				this.alInitParams.set("headerPane", hp);
				this.alInitParams.setConfig(this.getAssemblyLineConfig());
				this.alInitParams.startup();
//				this.editorStack.addContainerPane(hp, {title:"%%alinitparams%%"});
			}
//			this.editorStack.selectContainerPane("%%alinitparams%%");
		},
		
		createComponentsPane : function() {
			this.alComps = new ALComps({
				config:this.config,
				style:"width:300px; height:300px",
				connectors:true,
				title:"Components",
				onDrag:lang.hitch(this, "_showDropPoints"),
				onDragEnd:lang.hitch(this, "_hideDropPoints")
			});
			this.alComps.startup();
		},
		
		createFlowPane : function(region, container) {
			this.alFlow = new ALComps({
				config:this.config,
				style:"width:300px; height:300px",
				flowControl:true,
				title:"Flow controls",
				onDrag:lang.hitch(this, "_showDropPoints"),
				onDragEnd:lang.hitch(this, "_hideDropPoints")
			});
			this.alFlow.startup();
		},
		
		createLibraryPane : function(region, container) {
			this.alLibrary = new ALComps({
				config:this.config,
				style:"width:300px; height:300px",
				library:true,
				title:"Resources",
				onDrag:lang.hitch(this, "_showDropPoints"),
				onDragEnd:lang.hitch(this, "_hideDropPoints")
			});
			this.alLibrary.startup();
		},
		
		_showDropPoints: function() {
			for(var key in this.editorLayouts) {
				var layout = this.editorLayouts[key];
				if(layout && layout.showDropPoints) {
					layout.showDropPoints();
				}
			}
			this.clearPopups();
		},
		
		_hideDropPoints: function() {
			for(var key in this.editorLayouts) {
				var layout = this.editorLayouts[key];
				if(layout && layout.hideDropPoints) {
					layout.hideDropPoints();
				}
			}
		},
		
		openLogPane: function(content) {
			if(!this.logPane) {
				this.logPane = new ContentPane({
					region:"bottom",
					style:"height:35%; margin:0; padding:0;",
					content:content
				});
				this.logPane.startup();
				this.borderContainer.addChild(this.logPane);
			} else {
				this.logPane.set("content", content);
			}
			this.workEntry.setRuntime(true);
		},
		
		closeLogPane: function() {
			if(this.logPane) {
				this.borderContainer.removeChild(this.logPane);
			}
			this.logPane = null;
		},
		
		openRecent: function(val) {
			alert(val);
		},
		
		getRecentAssemblyLines: function() {
			var sel = new Select({
				onChange:lang.hitch(this, "openRecent")
			});
			array.forEach(tdiutil.getRecentFiles(), function(rec) {
				var str = rec.solution + ":" + rec.assemblyline;
				sel.addOption({value:str, label:str});
			});
			return sel;
		},
		
		createQuickbar: function() {
			var div = html.create("div");
			
			new Link({
				label:"AssemblyLine Schedule",
				onClick:lang.hitch(this, function() {
					this.showALSchedule();
				})
			}).placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
			
			new Link({
				label:"Solution Resources",
				onClick:lang.hitch(this, function() {
					this.showComponentsList();
					this.alComps.selectResources();
				})
			}).placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
			
			new Link({
				label:"AssemblyLine Parameters",
				onClick:lang.hitch(this, function() {
					this.showALInitParamsPopup();
				})
			}).placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
			
			var alconfig = this.getAssemblyLineConfig();
			array.forEach(alconfig.getOperationNames(), function(oper) {
				var op = alconfig.getOperation(oper);
				new Link({
					label:"Operation: " + oper,
					onClick:lang.hitch(this, function() {
						alert("Oper: " + oper);
					})
				}).placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
			});
			
			return new HeaderPane({
				title:"AssemblyLine",
				style:"width:100%; height:100%; margin:0; padding:0",
				content:div
			});
		},
		
		createLeftPane: function(region, container) {
			this.leftPane = new Border({containerType:"none", style:"width:100%; height:100%; margin:0; padding:0"});
			
			this.alQuickbar = this.createQuickbar();
			this.leftPane.setTop(this.alQuickbar, {style:"height:50%; margin:0; padding:0"});
			
			this.workEntry = new ALDataCollector({
				config:this.getAssemblyLineConfig(),
				title:"Work Entry"
			});
			this.leftPane.setCenter(this.workEntry)
			
			this.workPane = new ContentPane({
				region:region,
				content:this.leftPane,
				splitter:false,
				style:"width:25%; margin:0; padding:0"
			});
			container.addChild(this.workPane);
		},
		
		destroy: function() {
			if(this.surfaceTop) {
				this.surfaceTop.clear();
			}
			this.inherited(arguments);
		},
		
		createNavPaneButtons: function(pane) {
			this.stepButton = new ALButton({
				title:"Start",
				hoverImage:"Play.png",
				normalImage:"Play_gray.png",
				onClick:lang.hitch(this, "setNavBarPane", true)
			});
			pane.addChild(this.stepButton);
			
			this.addComponentButton = new ALButton({
				title:"Add component",
				hoverImage:"Page_new.png",
				normalImage:"Page_new_gray.png",
				onClick:lang.hitch(this, "showComponentsList")
			});
			pane.addChild(this.addComponentButton);
			
			this.flowComponentButton = new ALButton({
				title:"Flow Control",
				hoverImage:"Flow.png",
				normalImage:"Flow_gray.png",
				onClick:lang.hitch(this, "showFlowPopup")
			});
			pane.addChild(this.flowComponentButton);
			
			this.libraryButton = new ALButton({
				title:"Resources",
				hoverImage:"Library.png",
				normalImage:"Library_gray.png",
				onClick:lang.hitch(this, "showLibraryPopup")
			});
			pane.addChild(this.libraryButton);
			
			this.projectsButton = new ALButton({
				title:"Projects",
				hoverImage:"Suitcase.png",
				normalImage:"Suitcase_gray.png",
				onClick:lang.hitch(this, "showProjectsPopup")
			});
			pane.addChild(this.projectsButton);
			
			this.dashboardButton = new ALButton({
				title:"Settings",
				hoverImage:"Terminal.png",
				normalImage:"Termina_gray.png",
				onClick:lang.hitch(this, "showAlInitParamsPopup")
			});
			pane.addChild(this.dashboardButton);
		},

		setNavBarPane: function(runpanel) {
			// summary:
			//		Swap between main and run nav panels
			if(runpanel) {
				this.borderContainer.removeChild(this.navpane);
				this.borderContainer.addChild(this.runNavpane);
			} else {
				this.borderContainer.removeChild(this.runNavpane);
				this.borderContainer.addChild(this.navpane);
			}
		},
		
		createRunNavPaneButtons: function(pane) {
			var b = new ALButton({
				title:"Run",
				hoverImage:"Play.png",
				normalImage:"Play_gray.png",
				onClick:lang.hitch(this, "startAssemblyLine", false)
			});
			pane.addChild(b);
			
			var b = new ALButton({
				title:"Step",
				hoverImage:"Step.png",
				normalImage:"Step_gray.png",
				onClick:lang.hitch(this, "startAssemblyLine", true)
			});
			pane.addChild(b);
			
			var b = new ALButton({
				title:"Stop",
				hoverImage:"Stop.png",
				normalImage:"Stop_gray.png",
				onClick:lang.hitch(this, "stopAssemblyLine")
			});
			pane.addChild(b);
		},
		
		createWaterMark: function() {
			dojo.create("div", {
				innerHTML:this.assemblyline,
				"class":"webDevAssemblyLineName",
				style:{
					position:"absolute",
					top:"10px",
					left:"10px",
					width:"700px",
					height:"40px",
					zIndex:10
				}
			}, this.surfacePane.domNode);
		},
		
		postCreate: function() {
			
			this._expandedState = new Object();
			this.attributeWidgets = new Object();
			
			this.borderContainer = new BorderContainer({
				gutters:false,
				style:"width:100%; height:100%; margin:0; padding:0;" // background-color:#F5DEB3;"
			}).placeAt(this.domNode);
			this.own(this.borderContainer);			
			
			this.leftBorder = new BorderContainer({
				region:"leading",
				gutters:false,
				splitter:true,
				style:"width:20%; height:100%; margin:0; padding:0"
			});
//			this.borderContainer.addChild(this.leftBorder);
			
			//
			// -- al quickbar and work entry in leading pane
			//
			this.createLeftPane("center", this.leftBorder);
			
			//
			// -- Center pane with GFX content
			//
			this.border = new BorderContainer({
				region:"center",
				gutters:false,
				"class":"tdiWebDevPage",
				style:"width:100%; height:100%; margin:0; padding:0;"
			});
			this.borderContainer.addChild(this.border);
			
			var div = dojo.create("div", {zIndex:-1});
			this.surfaceTop = Gfx.createSurface(div, 1000, 1000);
			this.surfaceTop.rawNode.style.zIndex = -1;
			this.surfacePane = new ContentPane({
				splitter:false,
				region:"center",
				content:div
			});
			this.surfaceDiv = div;
			
			this.border.addChild(this.surfacePane);
			
			this.leftBorder.startup();
			
			//
			// -- Top pane with toolbar
			//
			var tools = this.createToolbar("top", null); // this.borderContainer);
			var toolpane = new ContentPane({
				splitter:false,
				region:"top",
				content:tools
			});
//			this.border.addChild(toolpane);
			
			//
			// -- Left pane with nav buttons
			//
			this.navpane = new ContentPane({
				region:"leading",
				style:{
					width:"120px",
					height:"100%",
					padding:"0px",
					margin:"0px"
				},
				"class":"tdiWebDevNavPane"
			});
			this.borderContainer.addChild(this.navpane);
			this.createNavPaneButtons(this.navpane);
			
			//
			// -- Secondary pane with Run buttons
			//
			this.runNavpane = new ContentPane({
				region:"leading",
				style:{
					width:"120px",
					height:"100%",
					padding:"0px",
					margin:"0px"
				},
				"class":"tdiWebDevNavPane"
			});
			this.createRunNavPaneButtons(this.runNavpane);
			
			//
			// -- Create popups for components etc
			//
			this.createComponentsPane();
			this.createFlowPane();
			this.createLibraryPane();

			//
			// -- AL name watermark
			//
			this.createWaterMark();
			
			//
			// -- When components are added/removed or change mode repaint the attmaps
			//
			var t = this;
			aspect.after(this.config, "onModify", function(key, args) {
				if(args && args.length == 2) {
					var p = args[1];
					if(p && p.param == "complexConfig.mode") {
						if(t.attributeWidgets[p.source.name]) {
							t.attributeWidgets[p.source.name].refresh();
						}
					}
				}
			});
			
			this.borderContainer.startup();
		}
	});
});
