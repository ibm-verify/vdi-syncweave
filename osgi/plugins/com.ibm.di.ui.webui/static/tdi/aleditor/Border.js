/**
 * The Border class is a base template for a border or stack layout based widget
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/StackContainer",
	"dijit/layout/TabContainer",
	"dijit/layout/ContentPane"
], function(declare, array, lang, Widget, TemplatedWidget, BorderContainer, StackContainer, TabContainer, ContentPane) {
	
	return declare(
		[BorderContainer],
		{
//			templateString: "<div data-dojo-attach-point='BorderMain' style='height:100%; width:100%; margin:0; padding:0'></div>",
			containerType: "border",
			borderId: {},
			contentId: {},
			
			constructor: function(args) {
				if(args)
					declare.safeMixin(this, args);
				this.borderId = new Object();
				this.contentId = new Object();
			},
			
			setBorderPane: function(pane, region, options) {
				var opts = options || {};
				opts.content = pane;
				opts.region = region;
				opts.style = opts.style || "margin:0; padding:0";
				var cp = new ContentPane(opts);
				this.addChild(cp);
				this.borderId[region] = cp;
			},
			
			setLeft: function(pane, options) {
				this.setBorderPane(pane, "left", options);
			},
			
			setRight: function(pane, options) {
				this.setBorderPane(pane, "right", options);
			},
			
			setTop: function(pane, options) {
				this.setBorderPane(pane, "top", options);
			},
			
			getTop: function(pane, options) {
				return this.borderId.top;
			},
			
			setCenter: function(pane, options) {
				this.setBorderPane(pane, "center", options);
			},
			
			getCenter: function(pane, options) {
				return this.borderId.center;
			},
			
			setBottom: function(pane, options) {
				this.setBorderPane(pane, "bottom", options);
			},
			
			getBottom: function(pane, options) {
				return this.borderId.bottom;
			},
			
			hasContainerPane: function(key) {
				// summary:
				//		returns true if there is a pane with the given title/id
				return this.contentId[key];
			},
			
			getContainerPanes: function() {
				// summary:
				//		returns an array of open panes; use getContainerPane(key) to retrieve.
				var arr = new Array();
				array.forEach(this.contentId, function(key) {
					arr.push(key);
				});
				return arr;
			},
			
			getContainerPane: function(key) {
				// summary:
				//		returns the widget matching key
				return this.contentId[key];
			},
			
			getContainerPaneWidget: function(key) {
				// summary:
				//		returns the widget matching key
				return this.contentId[key] ? this.contentId[key].content : null;
			},
			
			addContainerPane: function(pane, options) {
				// summary:
				//		adds a widget to the container pane
				options = options || {};
				options.content = pane;
				options.style = options.style || "width:100%; height:100%; margin:0; padding:0";
				var cp = new ContentPane(options);
				if(options.title) {
					this.contentId[options.title] = cp;
				}
				this.container.addChild(cp);
				this.selectContainerPane(options.title);
			},
			
			removeContainerPane: function(key) {
				// summary:
				//		removes a widget from the container pane
				if(this.contentId[key]) {
					this.container.removeChild(this.contentId[key]);
					delete this.contentId[key];
				}
			},
			
			selectContainerPane: function(pane) {
				if(this.contentId[pane] && this.container.selectChild) {
					this.container.selectChild(this.contentId[pane]);
					return this.contentId[pane];
				}
				array.forEach(this.contentId, function(str) {
					var comp = this.contentId[str];
					if(comp && comp.pageId == str && this.container.selectChild) {
						this.container.selectChild(comp);
					}
				}, this);
			},
			
			createContainerPane: function(options) {
				var opts = {height:"100%", width:"100%", margin:0, padding:0};
				opts = lang.mixin(opts, options);
				return new ContentPane(opts);
			},
			
			postCreate: function() {
				var params = {
					region:"center",
					style:"width:100%; height:100%; margin:0; padding:0"
				};
				
				if(this.params)
					declare.safeMixin(params, this.params);
				
				if(!this.containerType || this.containerType == "border") {
					params.gutters = false;
					this.container = new BorderContainer(params);
					
				} else if (this.containerType == "stack") {
					this.container = new StackContainer(params);
					
				} else if (this.containerType == "tab") {
					this.container = new TabContainer(params);
					
				}

				if(this.container) {
					this.container.startup();
					this.addChild(this.container);
				}
			}
		}
	);
});