/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/_base/fx",
	"dojo/dom-attr",
	"dojo/dom-class",
	"dojo/dom-style",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/form/HorizontalSlider",
	"dijit/form/Select",
	"dijit/form/DropDownButton",
	"dijit/form/NumberSpinner",
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
	"tdi/aleditor/ALComponent",
	"tdi/aleditor/ALComponent2",
	"tdi/aleditor/ALComps",
	"tdi/aleditor/ALConnection",
	"tdi/aleditor/ALDataCollector",
	"tdi/aleditor/ALDebugger",
	"tdi/aleditor/ALDropPoint",
	"tdi/aleditor/ALInitParams",
	"tdi/aleditor/ALPlaceholder",
	"tdi/aleditor/Border",
	"tdi/orion/OrionEditor",
	"tdi/ConnectorEditor",
	"tdi/model/ComponentsModel",
	"tdi/aleditor/Colors",
	"tdi/AttributeLoop",
	"tdi/FilteredLogViewer",
	"tdi/BranchEditor",
	"tdi/LinkCriteriaWidget",
	"tdi/ToolbarLabel",
	"tdi/atom/tdifeed",
	"tdi/tdiutil",
	"idx/layout/HeaderPane",
	"idx/layout/TitlePane",
	"idx/form/Link",
	"dojo/text!./templates/ALEditor.html"
], function(declare, array, lang, html, fx, domAttr, domClass, domStyle, _Widget, _TemplatedMixin, _WidgetsInTemplate, BorderContainer, ContentPane,
		HorizontalSlider, Select, DropDownButton, NumberSpinner,
		Dialog, Toolbar, TooltipDialog, Button, StackContainer, popup, Calendar, dTopic, Gfx, GfxUtils, idxDialog, ALSchedule, tdiapi, tdiconfig, 
		tdicontainer, ALComponent, ALComponent2, ALComps, ALConnection, ALDataCollector,
		ALDebugger, ALDropPoint, ALInitParams, ALPlaceholder, Border, TDIJavascriptEditor, TDIConnectorEditor, ComponentsModel, TDI, AttributeLoop, FilteredLogViewer,
		BranchEditor, LinkCriteriaWidget, ToolbarLabel, tdifeed, tdiutil, HeaderPane, TitlePane, Link, template) {
	
return declare(
	[ _Widget, _TemplatedMixin ],
	{
		templateString : template, // "<div data-dojo-attach-point='SurfaceContainer' style='height:100%; width:100%'></div>",
		widgetsInTemplate: true,
		configlabel: "(Default)",
		componentOffsetX: 200,
		components: [],
		counter: 1,
		runalId: "- Run AssemblyLine",
		popupEditors: false,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.startOffset = {x:this.componentOffsetX, y:15, h:30};
			this.componentSize = {width:150, height:60, rowSpacing:80};
			this.componentOffset = this.startOffset.y + this.startOffset.h + this.componentSize.rowSpacing;
			this.stopOffset = {y:this.componentOffset, x:this.componentOffsetX + this.componentSize.width + this.componentSize.rowSpacing, h:30};
		},
		
		getConfig : function() {
			return this.config;
		},
		
		getAssemblyLineConfig : function() {
			return this.config.getAssemblyLine(this.assemblyline);
		},
		
		highlightBreakpoint: function(status, entry) {
			var arr = array.filter(this.components, function(comp) {
				return comp.getId() == status.component;
			});
			
			if(arr && arr.length == 1) {
				var comp = arr[0];
				
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
			html.style(target.domNode, "opacity", "0");
			fx.fadeIn({
				node:target.domNode,
				duration:350
			}).play();
		},
		
		selectCollectorFor : function(comp) {
			var target = this.alDataCollectorsCP[comp];
			
			this.editorStack.selectContainerPane(this.runalId);
			
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
			//
			// summary:
			//		Launches a temporary configuration with the current assemblyline in debug mode.
			//		
			if(!this.alDebugger) {
				this.alDebugger = new ALDebugger({config:this.config, assemblyline:this.assemblyline, breakOnComponents:breakOnComp});
				
				//
				// -- Handle debug break by updating visuals to reflect current state of the AL
				//
				this.alDebugger.onDebugBreak = lang.hitch(this, function(status) {
					
					// -- switch to correct component
					this.selectCollectorFor(status.component);
					
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
						style:"margin:0; padding:0",
						content:this.alDataCollectors[comp]
					});
					this.alDataCollector.addChild(cp);
					this.alDataCollectorsCP[comp] = cp;
				}, this);
				this.alDataCollector.startup();
				
				this.editorStack.removeContainerPane(this.runalId);
				this.editorStack.addContainerPane(this.alDataCollector, {title:this.runalId});
				this.setSelectedItem(null);
				
			} else {
				this.alDebugger.runContinue();
			}
			this.enableButtons(true);
		},
		
		openLogViewer: function(cientry) {
			// summary:
			//	Create a FilteredLogViewer to display log messages posted on
			//	the queue defined by this cientry's listener URL.
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
					style:"width:100%; height:100%; margin:0; padding:0"
				});
				this.openLogPane(hp);
			}), tdiapi.defaultErrHandler);
		},
		
		setSelectedItem: function(item) {
			if(this.selectedItem) {
				this.selectedItem.setSelected(false);
			}
			this.selectedItem = item;
			if(item) {
				item.setSelected(true);
			} else {
				this.showComponentsList();
			}
		},
		
		clearPopups: function() {
			array.forEach(this.components, function(comp) {
				if(comp.clearPopups)
					comp.clearPopups();
			});
		},
		
		stopAssemblyLine: function() {
			if(this.alDebugger) {
				this.alDebugger.stopDebugger();
				this.alDebugger = null;
				if(this.focusRect) {
					this.surface.remove(this.focusRect);
					this.focusRect = null;
				}
				if(this.alDataCollector) {
					this.editorStack.removeContainerPane(this.runalId);
//					this.alDataCollector.destroyRecursive();
					this.alDataCollector = null;
				}
			}
			if(this.alLogWidget) {
				this.alLogWidget.stop();
			}
			this.workEntry.setRuntime(false);
			this.enableButtons(false);
			this.stopButton.set("disabled", true);
		},
		
		queryStatus: function() {
			dojo.when(this.alDebugger.getStatus(), function(data) {
				alert(dojo.toJson(data));
			});
		},
		
		openConfigPage: function(comp, event, tab) {
			
			if(this.editorStack.hasContainerPane(this.runalId)) {
				// -- when running we show the stats page for the component
				this.selectCollectorFor(comp.getId());
			} else {
				// -- when not running we show the config page for the component
				if(!this.editorStack.hasContainerPane(comp.getId())) {
					var editor = null;
					var alconfig = this.config.getAssemblyLine(this.assemblyline);
					var compconfig = alconfig.getComponentByName(comp.getId());
					if(!compconfig || !compconfig.isConnector) {
						return;
					}
					
					if(compconfig.isConnector()) {
						editor = new TDIConnectorEditor({config:compconfig, title:"Connector: " + compconfig.getName()});
						
					} else if (compconfig.isScript()) {
						jseditor = new TDIJavascriptEditor({config:compconfig, autoUpdate:true});
						editor = new HeaderPane({
							title:"Script: " + compconfig.getName(),
							content:jseditor,
							style:"width:100%; height:100%"
						});
						
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
					
					if(editor)
						editor.startup();
					
					if(this.popupEditors) {
						if(editor) {
							new idxDialog({
								title:editor.title,
								content:editor,
								style:"width:75%; height:75%"
							}).show();
							editor.resize();
						}
					} else {
						if(editor == null)
							editor = dojo.create("div", {innerHTML:"<b><center>Unknown component type: " + compconfig.getType() + ":" + compconfig.getSubType() + "</center></b>"});
						else
							this.connect(editor, "onDelete", lang.hitch(this, "onDeleteComponent"));
						
						this.editorStack.addContainerPane(editor, {title:comp.getId()});
					}
				}
				
				if(!this.popupEditors) {
					var target = this.editorStack.selectContainerPane(comp.getId());
					if(tab && target && target.content.selectPage) {
						target.content.selectPage(tab);
					}
				}
				this.setSelectedItem(comp);
			}
		},
		
		addComponent: function(node) {
			var comps = new ALComps({});
			comps.onLoad = lang.hitch(this, function() {
				var dlg = new Dialog({
					content:comps,
					style:"width:400px; height:400px"
				});
				popup.open({
					parent:this,
					popup:dlg,
					around: node ? node : this.addBullet.rawNode,
				    onExecute: function(){
						popup.close(dlg);
			    	},
				    onCancel: function(){
				        popup.close(dlg);
				    }
				});
			});
			comps.startup();
		},
		
		resize: function(newsize) {
			if(this.borderContainer) {
				this.borderContainer.resize(newsize);
			}
			if(this.contentPane) {
				this.contentPane.resize(newsize);
				if(this.editor) {
					this.editor.resize(newsize);
				}
			}
			if(this.surfacePane) {
				this.surfacePane.resize(newsize);
			}
			
			
			
			
		},
		
		getBoundingBox: function(shape) {
			var bb = shape.getBoundingBox();
			if(!bb && shape.children && shape.children.length > 0)
				bb = this.getBoundingBox(shape.children[0]);
			return bb;
		},
		
		createStart: function() {
			var style = {
					position:"absolute",
					left:(this.startOffset.x - 50) +"px",
					top:this.startOffset.y+"px",
					width:"100px",
					height:"30px"
			};
			this.startBullet = new ALPlaceholder({
				style:style,
				title:"Call"
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
		},
		
		createStop: function() {
			var bb = this.getBoundingBox(this.getLastComponent());
			var x = this.componentOffsetX;
			var y = bb.y + bb.height + this.componentSize.height;

			var style = {
					position:"absolute",
					left:(x - 50) +"px",
					top:y + "px",
					width:"100px",
					height:"30px"
			};
			
			this.stopBullet = new ALPlaceholder({
				style:style,
				title:"Reply"
			}).placeAt(this.surfaceDiv);
			this.components.push(this.stopBullet);
			
			this.replyBullet = new ALPlaceholder({
				style: {
					position:"absolute",
					left:"5px",
					top:y + 15 + "px",
					height:"0px",
					width:"0px",
					display:"none"
				},
				title:""
			}).placeAt(this.surfaceDiv);
		},
		
		addSubComponents: function(container, offset, xoffset, parent) {
			var count = container.getComponentCount();
			var branchOffsetY = (this.componentSize.height / 2);
			
			for(var i = 0; i < count; i++) {
				var config = container.getComponentAt(i);
				var comp = this.createComponent(config, offset, xoffset, parent);
				offset += comp.getBoundingBox().height || this.componentSize.height;
				offset += this.componentSize.rowSpacing;
				// offset += comp.getBoundingBox().height + this.componentSize.rowSpacing; // + (this.componentSize.height/2);
				if(config.isContainer && config.isContainer()) {
					comp.hasChildren = true;
					if(!this._expandedState[config.getName()]) {
						offset -= branchOffsetY;
						offset = this.addSubComponents(config, offset, xoffset + (this.componentSize.width+20), comp);
						offset -= branchOffsetY;
						var ret = this.createReturnPoint(offset, xoffset);
						this.components.push(ret);
						this.branches.push([comp, ret]);
						offset += branchOffsetY;
					} else {
						var x = xoffset + (this.componentSize.width+20);
						comp = new ALDropPoint({x:x, y:offset, surface:this.surface});
						this.components.push(comp);
					}
				}
			}
			
			if(count == 0) {
				var placeholder = container.getPlaceholder();
				var comp = this.createComponent(placeholder, offset, xoffset);
				offset += comp.getBoundingBox().height + this.componentSize.height;
			}
			
			return offset;
		},
		
		createReturnPoint: function(y, x) {
			return new ALDropPoint({surface:this.surface, x:x, y:y});
		},
		
		createComponents: function() {
			var al = this.config.getAssemblyLine(this.assemblyline);
			var xoffset = this.componentOffsetX;
			if(al) {
				var offset = this.componentOffset;
				var comp = null;
				
				// -- Add entryfeed component or empty one
				var input = al.getEntryFeedComponent();
				if(input.getComponentCount() > 0)
					comp = input.getComponentAt(0);
				else
					comp = al.createConnector("(Unconfigured)", "Iterator");
				comp = this.createComponent(comp, offset, xoffset);
				offset += comp.getBoundingBox().height || this.componentSize.height;
				offset += this.componentSize.rowSpacing;

				// -- Add dataflow components
				offset = this.addSubComponents(al.getDataFlowComponent(), offset, xoffset, true, comp);
			}
			
			//
			// -- Draw connectors lines between start/stop bullets and iterator
			this.createConnectorLine(this.startBullet, this.components[0], true);
//			this.createConnectorLine(this.components[0], this.stopBullet, true);
			
			// -- Draw connector lines between components
			for(i = 1; i < this.components.length; i++) {
				var source = this.components[i-1];
				var target = this.components[i];
				this.createConnectorLine(source, target, true);
			}
			
			// -- Draw connectors between branch and return points
			dojo.forEach(this.branches, lang.hitch(this, function(pair) {
				this.createConnectorLine(pair[0], pair[1], false);
			}));
			
			this.surfaceTop.setDimensions(600, offset+100);
			this.surfacePane.resize();
		},
		
		createComponent: function(comp, offset, xoffset) {
			var x = xoffset - (this.componentSize.width/2);
			var style = {
					position:"absolute",
					left:x+"px",
					top:offset+"px",
					width:this.componentSize.width + "px",
					height:this.componentSize.height + "px",
					"z-index":99
			};
			var component = new ALComponent2({
				config:comp,
				style:style,
				onClick: lang.hitch(this, "openConfigPage"),
				onDelete:lang.hitch(this, "onDeleteComponent")
			}).placeAt(this.surfaceDiv);
			
			this.components.push(component);
			return component;
		},
		
		createConnectorLine: function(source, target, arrowHead, dropPoint) {
			var connection = new ALConnection({
				source: source,
				target: target,
				arrowHead: arrowHead,
				dropPoint: typeof(dropPoint) == "undefined" ? true : dropPoint,
				surface: this.surface,
				expandPoint: source.hasChildren,
				onInsertComponent: lang.hitch(this, "onInsertComponent"),
				onToggleExpanded:lang.hitch(this, function(exp) {
					this._expandedState[source.getId()] = exp;
					this.buildGfx();
				})
			});
			
			connection.addDropHandler(lang.hitch(this, "onDropAdd", connection));
			
			this.connections = this.connections || [];
			this.connections.push(connection);

			return connection;
		},
		
		onInsertComponent: function(event) {
			this.addComponent(event.target);
		},
		
		onDeleteComponent: function(comp) {
			var cfg = comp.config;
			var t = this;
			if(cfg) {
				tdiutil.confirm("Delete " + cfg.getName(), function(status) {
					if(status == 0) {
						cfg.getParent().deleteComponent(cfg.getName());
						t.buildGfx();
					}
				});
			}
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

		onDropAdd: function(connection, event) {
			var str = "";
			var type = "";
			for(f in event.selection) {
				str = event.selection[f].item.id[0];
				type = event.selection[f].item.type[0];
			}

			var al = this.config.getAssemblyLine(this.assemblyline);
			var target = null;
			var config = null;
			var before = false;
			
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
			} else {
				alert("Insert at non-container point");
			}
			
			this.buildGfx();
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
		
		layoutChildren: function() {
			
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
			
			if(this.scaleSlider) {
				var value = this.scaleSlider.get("value");
				this.surface.setTransform(dojox.gfx.matrix.scale({x:value/100, y:value/100}));
			}

			this.components = [];
			this.branches = [];
			
			this.createStart();
			this.createComponents();
			this.createStop();
			
			this.createConnectorLine(this.callBullet, this.startBullet, true, false);
			this.createConnectorLine(this.stopBullet, this.replyBullet, true, false);
			this.createConnectorLine(this.getLastComponent(), this.stopBullet, true, true);
			
		},
		
		getLastComponent: function() {
			return this.components[this.components.length-1];
		},
		
		enableButtons: function(running) {
			this.startButton.set("label", running ? "Pause" : "Run");
			this.nextButton.set("disabled", running);
			this.stopButton.set("disabled", false);
		},
		
		createScaleDialog: function() {
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
			
			var div = html.create("div", {style:"padding:10px"}); // , {style:"height:75px; width:100px;"});
			html.create("div", {innerHTML:"Scale (%):"}, div);
			this.scaleSlider = new HorizontalSlider({
				value:100,
				minimum:25,
				maximum:200,
				intermediateChanges:true,
				style:"width:200px",
				onChange:lang.hitch(this, function(value) {
					this.layoutChildren();
					if(this.surface)
						this.surface.setTransform(dojox.gfx.matrix.scale({x:value/100, y:value/100}));
				})
			});
			this.scaleSlider.set("value", 100).placeAt(div);
			
			html.create("p", {}, div);
			html.create("div", {innerHTML:"Component width:"}, div);
			var compWidth = new NumberSpinner({
				constraints:{min:15,max:200,places:0},
				value:this.componentSize.width,
				intermediateChanges:true,
				onChange:lang.hitch(this, function(value) {
					this.componentSize.width = value;
					this.buildGfx()
				})
			}).placeAt(div);;
			
			html.create("p", {}, div);
			html.create("div", {innerHTML:"Component height:"}, div);
			var compHeight = new NumberSpinner({
				value:this.componentSize.height,
				constraints:{min:15,max:200,places:0},
				intermediateChanges:true,
				onChange:lang.hitch(this, function(value) {
					this.componentSize.height = value;
					this.buildGfx()
				})
			}).placeAt(div);;
			
			var tdialog = new TooltipDialog({
				content:div
			});
			
			this.scaleButton = new DropDownButton({
				label:"Options",
				dropDown:tdialog
			});
			this.toolbar.addChild(this.scaleButton);
			
			this.toolbar.addChild(new Button({
				label:"Add Component",
				onClick:lang.hitch(this, "showComponentsList")
			}));
			
			this.toolbar.startup();
			
			return this.toolbar;
		},
		
		showComponentsList: function() {
			this.editorStack.selectContainerPane("%%components%%");
			this.alComps.selectConnectors();
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
		
		showALInitParams: function() {
			if(!this.alInitParams) {
				this.alInitParams = new ALInitParams({});
				var hp = new HeaderPane({
					title:"AssemblyLine Parameters",
					content:this.alInitParams
				});
				this.alInitParams.set("headerPane", hp);
				this.alInitParams.setConfig(this.getAssemblyLineConfig());
				this.editorStack.addContainerPane(hp, {title:"%%alinitparams%%"});
			}
			this.editorStack.selectContainerPane("%%alinitparams%%");
		},
		
		createComponentsPane : function(region, container) {
			this.editorStack = new Border({containerType:"stack", baseClass:"dijitReset"});
			this.configPane = new ContentPane({
				region:region,
				splitter:"true",
				style:"width:35%; margin:0; padding:0",
				content:this.editorStack
			});
			container.addChild(this.configPane);
			
			this.alComps = new ALComps({config:this.config});
			this.alComps.startup();
			this.editorStack.addContainerPane(this.alComps, {title:"%%components%%"});
		},
		
		getLogPaneDiv : function() {
			if(!this.logPane) {
				this.logPaneDiv = dojo.create("div", {style:"width:100%; height:100%; margin:0; padding:0"});
				this.logPane = new ContentPane({
					region:"bottom",
					style:"height:15%",
					splitter:true,
					closeable:true,
					content:this.logPaneDiv
				});
				this.leftBorder.addChild(this.logPane);
			}
			return this.logPaneDiv;
		},
		
		openLogPane: function(content) {
			if(!this.logPane) {
				this.logPane = new ContentPane({
					region:"bottom",
					style:"height:25%; margin:0; padding:0",
					splitter:true,
					content:content
				});
				this.leftBorder.addChild(this.logPane);
			} else {
				this.logPane.set("content", content);
			}
			this.workEntry.setRuntime(true);
		},
		
		closeLogPane: function() {
			this.leftBorder.removeChild(this.logPane);
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
					this.showALInitParams();
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
			
//			new Link({
//				label:"Recent AssemblyLines",
//				onClick:lang.hitch(this, function() {
//				})
//			}).placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
//			
//			this.getRecentAssemblyLines().placeAt(html.create("div", {style:"margin-bottom:5px"}, div));
			
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
	
		postCreate: function() {
			
			this._expandedState = new Object();
			
			this.borderContainer = new BorderContainer({
				gutters:true,
				style:"width:100%; height:100%; margin:0; padding:0;" // background-color:#F5DEB3;"
			}).placeAt(this.SurfaceContainer);
			this.own(this.borderContainer);			
			
			this.leftBorder = new BorderContainer({
				region:"center",
				gutters:false,
				style:"width:60%; height:100%; margin:0; padding:0"
			});
			this.borderContainer.addChild(this.leftBorder);
			
			//
			// -- al quickbar and work entry in leading pane
			//
			this.createLeftPane("leading", this.leftBorder);
			
			//
			// -- Center pane with GFX content
			//
			this.border = new BorderContainer({
				region:"center",
				gutters:false,
				style:"width:100%; height:100%; margin:0; padding:0; background-color:#ffe4c4;" //F5DEB3
			});
			this.leftBorder.addChild(this.border);
			
			var div = dojo.create("div");
			this.surfaceTop = Gfx.createSurface(div, 1000, 1000);
			this.surfacePane = new ContentPane({
				splitter:true,
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
			this.border.addChild(toolpane);
			
			//
			// -- Right pane with contextual widgets
			//
			this.createComponentsPane("trailing", this.borderContainer);
			
			this.borderContainer.startup();
			
		}
	});
});