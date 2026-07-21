define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/dom-style",
	"./ALConnection"
], function(declare, array, lang, domStyle, ALConnection) {
	
return declare(
	[],
	{
		// global id
		_globalIdCounter: 1,
		
		// component: Target UI component
		component: null,
		
		// children: Array of child ALEditorLayout objects
		children: null,
		
		// expanded: Boolean
		expanded: true,
		
		// visible: Boolean
		visible: true,
		
		// nextSibling: ALEditorLayout
		nextSibling: null,
		
		// prevSibling: ALEditorLayout
		prevSibling: null,
		
		// surface: GFX surface where connections are drawn
		surface: null,
		
		constructor: function(arguments) {
			lang.mixin(this, arguments);
			this.id = this.component.getId ? this.component.getId() : this._globalIdCounter++; 
			this.setPrevSibling(this.prevSibling);
		},
		
		getComponent: function() {
			// summary:
			//		Returns the UI component
			return this.component;
		},
		
		getConfig: function() {
			// summary:
			//		Returns the component's TDI configuration
			if(this.component && this.component.getConfig)
				return this.component.getConfig();
			else
				return null;
		},
		
		getBoundingBox: function() {
			// summary:
			//		Returns the bounding box of the target UI component
			//		If comp has getBoundingBox method use that otherwise
			//		compute from component.domNode.
			if(this.component.getBoundingBox) {
				return this.component.getBoundingBox();
			}
			var domNode = this.component.domNode;
			var box = {
				y: parseFloat(domNode.style.top.replace("px", "")),
				x: parseFloat(domNode.style.left.replace("px", "")),
				width: parseFloat(domNode.style.width.replace("px", "")),
				height: parseFloat(domNode.style.height.replace("px", ""))
			};
			return box;
		},
		
		setBoundingBox: function(box) {
			// summary:
			//		Sets the bounding for the UI component.
			//		Box must contains numbers only (e.g. no 10px etc)
			var style = {
					position:"absolute",
					top:box.y + "px",
					left:box.x + "px",	
					width:box.width + "px",
					height:box.height + "px"
			};
			for(var f in style) {
				this.component.domNode.style[f] = style[f];
			}
			
			console.log(this.id + ": " + dojo.toJson(style) )
			
			this._refreshConnections();
		},

		setPrevSibling: function(layout) {
			// summary:
			//		Retargets this layout's prev sibling			
			this.prevSibling = layout === this ? null : layout;
			if(this.connections && this.connections.sibling) {
				this.destroyConnection("sibling");
			}
		},
			
		setNextSibling: function(layout) {
			// summary:
			//		Retargets this layout's next sibling
			this.nextSibling = layout === this ? null : layout;
			
			// -- tell next sibling we're previous
			if(this.nextSibling) {
				this.nextSibling.setPrevSibling(this);
			}
		},
		
		hasChildren: function() {
			// summary:
			//		Returns true if this layout has child layouts
			return this.children ? this.children.length > 0 : false;
		},
		
		getChildren: function() {
			// summary:
			//		Returns the array of children
			return this.children || [];
		},
		
		addChild: function(child) {
			// summary:
			//		Adds an ALEditorLayout object to the child list
			this.children = this.children || new Array();
			child.parent = this;
			this.children.push(child);
		},
		
		removeChild: function(child) {
			// summary:
			//		Removes an ALEditorLayout object from the child list
			this.children = this.children || new Array();
			this.children = array.filter(this.children, function(item) {
				return child.id != item.id;
			});
		},
		
		expand: function(level) {
			// summary:
			//		Expands this branch revealing child objects
			// level: integer
			//		Number of levels to expand (1 = default)
			this.expanded = true;
		},
		
		collapse: function() {
			// summary:
			//		Collapses his branch hiding child objects
			this.expanded = false;
		},
		
		show: function() {
			// summary:
			//		Sets style.display=block for this component and its children
			this.visible = true;
		},
		
		hide: function() {
			// summary:
			//		Sets style.display=none for this component and its children
			this.visible = false;
		},
		
		addConnection: function(params) {
			// summary:
			//		Adds a connection object with the provided params
			this.connections = c = this.connections || new Object();
			if(!c[params.type]) {
//				c[params.type].destroyRecursive();
				if(!params.onInsertComponent && !params.nodrop) {
					params.onInsertComponent = lang.hitch(this, "_onInsertComponent");
				}
				
				var drop = typeof(params.dropPoint) != "undefined" ? params.dropPoint : true;
				
				// provide reasonable defaults
				params.surface = params.surface || this.surface;
				params.source = params.source || this.component;
				params.target = params.target || this.component;
				params.sourceId = params.sourceId || this.id;
				
				c[params.type] = new ALConnection(params);
				if(drop)
					c[params.type].addDropHandler(lang.hitch(this, "_onDropAdd", params.type));
			} else {
				c[params.type].updatePosition();
			}
			return c[params.type];
		},
		
		destroyConnection: function(type) {
			// summary:
			//		Destroys the ALConnection object with the give type (branch, return, sibling)
			if(this.connections && this.connections[type]) {
				this.connections[type].destroyRecursive();
				delete this.connections[type];
			}
		},
		
		destroyConnections: function() {
			// summary:
			//		Destroys all connections between this layout and other layout nodes
			for(var type in this.connections) {
				this.connections[type].destroyRecursive();
			}
			this.connections = new Object();
		},
		
		showDropPoints: function() {
			for(var type in this.connections) {
				this.connections[type].showDropPoint();
			}
		},
		
		hideDropPoints: function() {
			for(var type in this.connections) {
				this.connections[type].hideDropPoint();
			}
		},
		
		destroy: function() {
			// summary:
			//		Destroy this layout+children and all UI components associated with it
			//		Siblings are retargetted as well.
			if(this.prevSibling) {
				this.prevSibling.setNextSibling(this.nextSibling);
			} else if(this.nextSibling) {
				this.nextSibling.setPrevSibling(this.prevSibling);
			}
			
			if(this.component) {
				this.component.destroyRecursive();
				this.component = null;
			}
			
			this.destroyConnections();
			
			this._recursiveCall(function(item) {
				item.destroy();
			})
		},
		
		_refreshConnections: function() {
			// summary:
			//		Call to repaint connector lines
			// 
			// If we have no prev sibling but a parent, we add a connection back to parent level
			// Or: If we have prev sibling add a connection to that
			// In either above case if we have no next sibling but a parent add a connection
			// back to parent			
			
			if(this.prevSibling) {
				this.addConnection({
					source:this.prevSibling.component,
					target:this.component,
					surface:this.surface,
					type:"sibling"
				});
			} else {
				this.destroyConnection("sibling");
			}
			
			if(this.parent && !this.prevSibling) {
				this.addConnection({
					source:this.parent,
					target:this.component,
					surface:this.surface,
					type:"branch"
				});
			} else {
				this.destroyConnection("branch");
			}

			if(this.parent && !this.nextSibling) {
				this.addConnection({
					source:this.component,
					target:this.parent,
					surface:this.surface,
					type:"return"
				});
			} else {
				this.destroyConnection("return");
			}
			
			for(var type in this.connections) {
				this.connections[type].refresh();
			}
		},
		
		_recursiveCall: function(pfunc) {
			var t = this;
			array.forEach(this.children, function(child) {
				pfunc(child);
			})
		},
		
		_onDropAdd: function(type, event) {
			var drops = [];
			for(var f in event.selection) {
				drops.push(event.selection[f]);
			}
			this.onDropAdd(this, drops[0].item, this.connections[type]);
		},
		
		onDropAdd: function(layout, items) {
			// summary:
			//		Called when a component is dropped onto one of this layout's
			//		connections.
		},
		
		_onInsertComponent: function(connection) {
			this.onInsertComponent(connection, this);
		},
		
		onInsertComponent: function(connection, layout) {
			// summary:
			//		Called when an insert-component command is issued onto one of this layout's
			//		connections.
		}
	});

});
