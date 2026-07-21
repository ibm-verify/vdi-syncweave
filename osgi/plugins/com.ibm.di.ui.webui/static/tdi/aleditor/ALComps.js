/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/dom-style",
	"dojo/dom-class",
	"dojo/aspect",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/form/TextBox",
	"dijit/form/Button",
	"dijit/form/ToggleButton",
	"dijit/form/RadioButton",
	"dijit/form/CheckBox",
	"dijit/layout/BorderContainer",
	"dijit/layout/ContentPane",
	"dijit/layout/AccordionContainer",
	"dijit/layout/StackContainer",
	"dijit/Tree",
	"dijit/Toolbar",
	"dijit/tree/dndSource",
	"tdi/layout/ListPane",
	"tdi/model/ComponentsModel",
	"tdi/tdiconfig",
	"tdi/TableWidget",
	"tdi/aleditor/Border",
	"idx/layout/HeaderPane",
	"idx/layout/TitlePane",
	"idx/layout/ButtonBar",
	"dijit/form/Select",
	"dojo/data/ItemFileWriteStore",
	"dijit/tree/ForestStoreModel",
	"dijit/layout/TabContainer",
	"dojo/text!./templates/ALComps.html"
], function(declare, lang, domStyle, domClass, aspect, _Widget, _TemplatedMixin, TextBox, Button, ToggleButton, RadioButton, CheckBox, BorderContainer, ContentPane, 
		Accordion, StackContainer, Tree, Toolbar, TreeDnd, ListPane, ComponentsModel, tdiconfig, TableWidget, Border, HeaderPane, TitlePane, ButtonBar, Select, Store, TreeModel, TabContainer, template) {
	
return declare(
	[ _Widget, _TemplatedMixin ],
	{
		templateString : "<div data-dojo-attach-point='Main' style='width:100%; height:100%; margin:0; padding:0'></div>",
	
		storeNames: ["Connections", "Flow", "Scripts", "All"],
		storeTypes: ["connector", "flow", "script", "*"],
		
		stores: {},
		
		getId: function() {
			return this.id;
		},
		
		createScriptStore: function(parent) {
			var arr = ["Dump Work Entry", "Empty Script", "AssemblyLine Prolog", "AssemblyLine Epilog"];
			for(var i = 0; i <  arr.length; i++) {
				this.store.newItem({
					name:arr[i], id:arr[i], type:"script", qtype:this.storeTypes[2]
				});
			}
		},
		
		createControlStore: function(parent) {
			var arr = ["AttributeValue Loop", "Connector Loop", "While Loop", "IF", "ELSE IF", "ELSE", "SWITCH", "CASE"];
			var flowtype = ["@AttrLoop", "@ConnLoop", "@While", "@If", "@Elseif", "@Else", "@Switch", "@Case"];
			for(var i = 0; i <  arr.length; i++) {
				this.store.newItem({
					name:arr[i], id:arr[i], type:flowtype[i], qtype:this.storeTypes[1]
				});
			}
		},
		
		createComponentStore: function() {
			this.store = new Store({
				data:{
					identifier:"id",
					label:"name",
					items:[]
				}
			});
			
//			this.createScriptStore();
//			this.createControlStore();
			return new ComponentsModel({
				addItem: dojo.hitch(this, function(item) {
					this.store.newItem({id:item.id, name:item.name, type:"connector", qtype:this.storeTypes[0]});
				}),
				onLoadComplete:dojo.hitch(this, "buildUI")
			});
		},
		
		setStore: function(store) {
			if(this.grid) {
				this.model.query = {qtype:store};
				this.model._requeryTop();
			}
		},
		
		resize: function(obj) {
			if(this.stackContainer) {
				this.stackContainer.resize(obj);
			} else if (this.headerPane) {
				this.headerPane.resize(obj);
			}
		},
		
		miscView: function() {
			var branchImage = "../static/images/v2/localproc_dgm48.png";
			var loopImage = "../static/images/v2/dowhile_dgm48.png";
			var connImage = "../static/images/v2/datastore_wiz.gif";
			var attmapImage = "../static/images/v2/map_dgm48.png";
			var scriptImage = "../static/images/v2/newjscriptfile_wiz.gif";
			var comps = ["IF", "ELSE IF", "ELSE", "SWITCH", "CASE", "Connector Loop", "Attribute Value Loop", "While", "Attribute Map", "Script"];
			var images = [branchImage, branchImage, branchImage, branchImage, branchImage, connImage, loopImage, loopImage, attmapImage, scriptImage];
			var flowtype = ["@If", "@ElseIf", "@Else", "@Switch", "@Case", "@ConnLoop", "@AttLoop", "@While", "@Attmap", "@Script"];
			
//			var grid = new TableWidget({
//				style:"width:100%; height:100%",
//				onSelected:lang.hitch(this, function(row) {
//					console.log("row: " + row);
//				})
//			});
			
			var data = [];
			for(var i = 0; i < comps.length; i++) {
				//store.newItem({
				data.push({
					id:comps[i],
					name:"<img width=16 height=16 src='" + images[i] + "'></img>&nbsp;" + comps[i],
					type:flowtype[i],
					image:images[i]
				});
			}
//			grid.setData(data);
			
			var store = new Store({
				data:{
					identifier:"id",
					label:"name",
					items:data
				}
			});
			
			var model = new TreeModel({store:store, rootId:"bongo", rootLabel:"funky"});
			
			var grid = new Tree({
				model:model,
				showRoot: false,
				dndController: "dijit.tree.dndSource",
				dndParams: ["checkItemAcceptance", "copyOnly"],
				copyOnly:true,
				checkItemAcceptance: function() {return false},
	            _createTreeNode: function(/*Object*/ args){
	                var tnode = new dijit._TreeNode(args);
	                tnode.labelNode.innerHTML = args.label;
	                return tnode;
	            },
	            getIconClass: function(item) {return "";}
	 		});
			this.flowcontrols = grid;
			return grid;
		},
		
		connectorView: function() {
			var model = new TreeModel({store:this.store, rootId:"bongo", rootLabel:"funky"});
			var grid = new Tree({
				model:model,
				showRoot: false,
				dndController: "dijit.tree.dndSource",
				dndParams: ["checkItemAcceptance", "copyOnly"],
				copyOnly:true,
				checkItemAcceptance: function() {return false},
	            getIconClass: function(item) {return "";}
			});
			this.connectors = grid;
			return grid;
		},
		
		solutionView: function() {
			var scriptImage = "/fds/static/images/v2/newjscriptfile_wiz.gif";
			var connectorImage = "/fds/static/images/Connector_16.gif";
			var store = new Store({
				data:{
					identifier:"id",
					label:"name",
					items:[]
				}
			});
			
			var comps = this.config.getConnectorNames();
			for(var i = 0; i < comps.length; i++) {
				store.newItem({
					id:comps[i],
					name:"<img width=16 height=16 src='" + connectorImage + "'></img>&nbsp;" + comps[i],
					type:"@Connector",
					image:connectorImage
				});
			}
			
			var comps = this.config.getScriptNames();
			for(var i = 0; i < comps.length; i++) {
				store.newItem({
					id:comps[i],
					name:"<img width=16 height=16 src='" + scriptImage + "'></img>&nbsp;" + comps[i],
					type:"@Script",
					image:scriptImage
				});
			}
			
			var model = new TreeModel({store:store, rootId:"bongo", rootLabel:"funky"});
			
			var grid = new Tree({
				model:model,
				showRoot: false,
				dndController: "dijit.tree.dndSource",
				dndParams: ["checkItemAcceptance", "copyOnly"],
				copyOnly:true,
				checkItemAcceptance: function() {return false},
	            _createTreeNode: function(/*Object*/ args){
	                var tnode = new dijit._TreeNode(args);
	                tnode.labelNode.innerHTML = args.label;
	                return tnode;
	            },
	            getIconClass: function(item) {return "";}
			});
			
			this.resources = grid;
			return grid;
		},
		
		filterGrid: function(value) {
			var query = {name:new RegExp(".*"+value+".*", "i")};
			this.connectors.model.query = query;
			this.connectors.model._requeryTop();
			this.flowcontrols.model.query = query;
			this.flowcontrols.model._requeryTop();
			this.resources.model.query = query;
			this.resources.model._requeryTop();
		},
		
		selectConnectors: function(key) {
			this.stackContainer.selectContainerPane("Connectors");
		},
		
		selectFlowControl: function(key) {
			this.stackContainer.selectContainerPane("Flow & Control");
		},
		
		selectResources: function(key) {
			this.stackContainer.selectContainerPane("Solution Resources");
		},
		
		onDrag: function() {
		},
		
		onDragEnd: function() {
		},
		
		buildUI: function() {
			var view = null;
			if(this.connectors)
				view = this.connectorView();
			if(this.flowControl)
				view = this.miscView();
			if(this.library)
				view = this.solutionView();
			
			aspect.after(view.dndController, "onDndStart", lang.hitch(this, "onDrag"), true);
			aspect.after(view.dndController, "onDndCancel", lang.hitch(this, "onDragEnd"), true);
			
			var tbox = new TextBox({
				style:"width:10em",
				placeHolder:"search",
				region:"titleActions",
				onChange:dojo.hitch(this, function(value) {
					// call filter after 400msec idle
					if(this.filterTimeout) {
						clearTimeout(this.filterTimeout);
					}
					this.filterTimeout = setTimeout(dojo.hitch(this, "filterGrid", value), 400);
				}),
				intermediateChanges:true
			});
			
			var header = new HeaderPane({
				title:this.title,
				style:"width:100%; height:100%; margin:0; padding:0",
				content:view
			}).placeAt(this.Main);
			header.addChild(tbox);
			header.startup();
			
			this.headerPane = header;
			
			this.onLoad();
		},
		
		onLoad: function() {
		},
		
		reload: function() {
			if(this.headerPane) {
				this.headerPane.destroyRecursive();
			}
			this.buildUI();
		},
		
		startup: function() {
			if(this.connectors)
				this.createComponentStore();
			else
				this.buildUI();
		}
	})
});