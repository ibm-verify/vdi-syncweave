/**
 * The ALComponent2 shows a short representation of a component with menus and links.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/_base/html",
	"dojo/_base/fx",
	"dojo/dom-class",
	"dojo/dom-style",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/_CssStateMixin",
	"dijit/InlineEditBox",
	"tdi/tdiconfig",
	"tdi/tdiutil",
	"tdi/aleditor/Colors",
	"dijit/popup",
	"dijit/TooltipDialog",
	"dijit/Toolbar",
	"dijit/TitlePane",
	"idx/layout/HeaderPane",
	"dijit/form/Button",
	"tdi/aleditor/quicklook/ALConnectorTooltip",
	"tdi/aleditor/quicklook/ALScriptTooltip",
	"tdi/aleditor/quicklook/ALBranchTooltip",
	"tdi/aleditor/TooltipMixin",
	"dojo/text!./templates/ALComponent2.html"
], function(declare, lang, array, html, fx, domClass, domStyle, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, CssStateMixin, InlineEditBox, tdiconfig, tdiutil, TDI, dPopup, TooltipDialog, Toolbar, TitlePane, HeaderPane, Button,
		ALConnectorTooltip, ALScriptTooltip, ALBranchTooltip, TooltipMixin, template) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TooltipMixin, CssStateMixin],
	{
		templateString: template,
		
		baseClass: "tdiWebDevComponent",
	
		cssStateNodes: {
		    "titleNode":"tdiWebDevComp"
		},
		
		// config: Object
		//		The connector config object
		config: null,
		
		// isComponent: Boolean
		//		True if this is a component
		isComponent: true,
		
		// title: String
		//		The title
		title: "",
		_setTitleAttr: { node: "titleNode", type: "innerHTML" },		
		
		// content: String
		//		The title
		content: "",
		_setContentAttr: { node: "contentNode", type: "innerHTML" },
		
		// image: String
		//		The image class
		image: "",
		_setImageAttr: { node: "imageNode", type: "class" },
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		getId: function() {
			return this.config ? this.config.getName() : null;
		},
		
		updateHooks: function() {
			if(!this.config.getHookNames)
				return;
			
			var t = this;
			array.forEach(t.config.getHookNames(), function(name) {
				var hook = t.config.getHook(name);
				if(hook.isEnabled() && hook.getScript()) {
					dojo.create("div", {"class":"tdiFormLabel", innerHTML:name},t.hooksNode);
				}
			})
		},
		
		updateTooltip: function() {
			var arr = new Array();
			var widget = null;
			
			if(this._tooltipContent)
				return;
			
			this.set("title", this.config.getName());
			this.titleNode.innerHTML = this.config.getName();
//			if(!this.ieb) {
//				this.ieb = new InlineEditBox({
//					autoSave:true
//				}, this.titleNode);
//				this.own(this.ieb.watch("value", lang.hitch(this, function(prop, oldval, value) {
//					this.config.setName(value);
//					this.config.setModified(true, "name");
//				})));
//			}
			
			var cls;
			if(this.config.getMode)
				cls = "tdiConnector" + this.config.getMode() + "Image";
			else if(this.config.isScript())
				cls = "tdiScriptImage";
			else if(this.config.isBranch())
				cls = "tdiBranchImage";
			else if(this.config.isLoop())
				cls = "tdiLoopImage";
			
			this.set("image", cls);
			
			arr.push("<b>" + this.getId() + "</b>");
			if(this.config.isConnector()) {
				widget = new ALConnectorTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				
//				this.set("content", widget.mapping);
				
			} else if(this.config.isScript()) {
				widget = new ALScriptTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				
			} else if(this.config.isBranch()) {
				widget = new ALBranchTooltip({
					config:this.config,
					onClick:lang.hitch(this, "onTooltipClick"),
					onDelete:lang.hitch(this, "onTooltipDelete")
				});
				var conditions = this.config.getBranchConfig().getConditions();
				var arr = new Array();
				for(var i = 0; i < conditions.getConditionCount(); i++) {
					var c = conditions.getCondition(i);
					arr.push(c.leftHand + (c.negate ? " not " : " ") + c.operator + " " + c.rightHand);
				}
				this.set("content", arr.join("<br>"));
				
			} else if(this.config.isLoop()) {
				arr.push("Loop Condition");
				arr.push("Type: " + this.config.getBranchType());
			}
			this.setTooltipContent(widget ? widget : arr.join("<br>"), this.config.getName());
		},
		
		onTooltipClick: function(caller, event, tab) {
			this.clearFocus();
			this.hideTooltip();
			this.setTooltip(null);
			this.onClick(this, event, tab);
		},
		
		onTooltipDelete: function() {
			this.onDelete(this);
		},
		
		onTooltipCopy: function() {
			this.onCopy(this);
		},
		
		onTooltipEnable: function(value) {
			if(value != this.config.getEnabled()) {
				this.config.setEnabled(value);
			}
			this.set("disabled", !(this.isEnabled() && this.isConfigured()));
		},
		
		onDelete: function(caller) {
			// callback
		},
		
		onCopy: function(caller) {
			// callback
		},
		
		getConfig: function() {
			return this.config;
		},
		
		isContainer: function() {
			return this.getConfig().isContainer();
		},
		
		setComponentType: function(type) {
			this.config.setConnectorType("system:/Connectors/" + type);
		},

		onClick: function(caller, event, tab) {
			// callback
			// Called when component is clicked
		},
		
		_toggleClose: function() {
			domClass.toggle(this.closeNode, "dijitTabCloseButtonHover");
		},
		
		animateMouseOver: function(args) {
			if(!this.isEnabled())
				return;
			
			var color = this.selected ? TDI.selectionColor : TDI.hoverColor;
			fx.animateProperty({
				node:this.domNode,
				duration: 100,
				properties: {
					"border-color":color,
					"border-width": "2"
				}
			}).play();
		},
		
		animateMouseOut: function(args) {
			if(!this.isEnabled())
				return;
			
			if(!this.selected) {
				fx.animateProperty({
					node:this.domNode,
					duration: 100,
					properties: {
						"border-color":TDI.lineColor,
						"border-width": "1"
					}
				}).play();
			}
		},
		
		setSelected: function(selected) {
			if(!this._destroyed) {
				var sel = this.selected;
				
				this.titleNode.selected = selected;
				this.set("selected", selected);
				
				
				if(sel && !selected)
					this.animateMouseOut();
				else if(!sel && selected)
					this.animateMouseOver();
				
				domStyle.set(this._toolbar.domNode, "display", (this.selected & this.isConfigured()) ? "" : "none");
			}
		},
		
		getBoundingBox: function() {
			var box = {
				y: parseFloat(this.domNode.style.top.replace("px", "")),
				x: parseFloat(this.domNode.style.left.replace("px", "")),
				width: parseFloat(this.domNode.style.width.replace("px", "")),
				height: parseFloat(this.domNode.style.height.replace("px", ""))
			};
			return box;
		},
		
		setBoundingBox: function(box) {
			this.domNode.style.top = box.top + "px";
			this.domNode.style.left = box.left + "px";
			this.domNode.style.width = box.width + "px";
			this.domNode.style.height = box.height + "px";
			this.set("box", box);
		},
		
		setTooltip: function(content) {
			this._tooltipContent = content;
			if(!content)
				this.updateTooltip();
			else
				this.setTooltipContent(content);
		},
		
		isEnabled: function() {
			if(this.config)
				return this.config.isEnabled();
			else
				return true;
		},
		
		isConfigured: function() {
			// summary:
			//		Returns true if component is configured (e.g. is not a placeholder)
			if(this.config && (this.config.isConnector() ||this.config.isFunction()) ) {
				var type = this.config.getConnectorType();
				return (type && type.indexOf("/") != -1);
			} else {
				return true;
			}
		},
		
        _setStateClass: function() {
			this.set("disabled", !(this.isEnabled() && this.isConfigured()));
        	this.inherited(arguments);
        },
        
		postCreate: function() {
			this.connect(this.config, "onModify", lang.hitch(this, "updateTooltip"));
			this.updateTooltip();
			this.updateHooks();
			
			if(this.config.isConnector() || this.config.isFunction()) {
				this._enabled.set("value", this.config.getEnabled());
			} else {
				this._enabled.set("disabled", true);
			}
			
			this.connect(this.domNode, "onmouseover", lang.hitch(this, "animateMouseOver"));
			this.connect(this.domNode, "onmouseout", lang.hitch(this, "animateMouseOut"));
			
			this.connect(this.domNode, "onclick", lang.hitch(this, function(event) {
				this.clearFocus();
				this.hideTooltip();
				this.onClick(this, event);
			}, this));
			
			this.dropTarget = new dojo.dnd.Target(this.titleNode);
			dojo.connect(this.dropTarget, "onDrop", function(event) {
				console.log(event);
			});
			
			this.inherited(arguments);
		}
	})
});

		
		
