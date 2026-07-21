define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/aspect",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"idx/widget/Dialog",
	"idx/form/Link",
	"idx/layout/HeaderPane",
	"./aleditor/ALInitParams",
	"tdi/NlsMixin",
	"dojo/text!./templates/ExpressionEditor.html"
], function(declare, lang, array, aspect, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, Dialog, Link, HeaderPane, ALInitParams, nls, template) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		prefix: {
			"property":"(P) ",
			"javascript":"(JS) ",
			"substitution":"(S) ",
			"initparam":"(I) "
		},
		
		onChange: function(value) {
		},
		
		_onEditExpression: function() {
			this._editor.show();
		},
		
		_onResetValue: function() {
			this._removeControl(this._expr);
			this._showControl(this.control);
		},
		
		_setValueAttr: function(value) {
			if(this.control) {
				this.control.set("value", value);
			}
		},
		
		updateValue: function() {
			var str = this._updateWidgets();
			this.config.setParam(this.param, str);
		},
		
		_updateWidgets: function() {
			var type = this._type.get("value").trim();
			var str = "";
			if(type == "reset") {
				this._removeControl(this._expr);
				this._showControl(this.control);
			} else {
				str = this["_"+type].get("value").trim();
				this._expr.set("label", this.prefix[type] + str);
				if(type == "property")
					str = "@SUBSTITUTE{property." + str + "}";
				else if(type == "initparam")
					str = "@SUBSTITUTE{javascript return task.getOpEntry().getString('" + str + "');}";
				else if(type == "javascript")
					str = "@SUBSTITUTE{javascript " + str + "}";
				this._removeControl(this.control);
				this._showControl(this._expr);
			}
			return str;
		},
		
		_removeControl: function(c) {
			try {
				var node = c.domNode ? c.domNode : c;
				if(node && node.parentNode) {
					node.parentNode.removeChild(node);
				}
			} catch(err) {}
		},
		
		_showControl: function(c) {
			c.placeAt(this._control);
			c.startup();
			this._contextMenu.bindDomNode(c.domNode);
		},
		
		_selectEditor: function(value) {
			this._property.set("style", {display:value == "property" ? "" : "none"});
			this._initparamDiv.style.display = value == "initparam" ? "" : "none";
			this._javascript.set("style", {display:value == "javascript" ? "" : "none"});
		},
		
		_editInitParams: function() {
			var alconfig = this.config.getAssemblyLine();
			if(!alconfig) {
				alert("Can only be used when component is part of an assemblylines");
				return;
			}
			var initp = new ALInitParams({config:alconfig, style:"width:100%;height:100%"});
			var hp = new HeaderPane({
				style:"width:600px;height:400px",
				content:initp
			});
			initp.set("headerPane", hp);
			var t = this;
			var dlg = new Dialog({
				title:"AssemblyLine Parameters",
				content:hp,
				onCancel:function() {
					t._updateInitParams();
				}
			});
			dlg.show();
		},
		
		_updateInitParams: function() {
			
			var alconfig = this.config.getAssemblyLine();
			if(!alconfig) {
				return;
			}
			
			this._initparam.removeOption(this._initparam.getOptions());
			
			var initParams = alconfig.getInitParams();
			var data = [];
			array.forEach(initParams.getNames().sort(), lang.hitch(this, function(name) {
				this._initparam.addOption({value:name, label:name});
			}));
		},
		
		startup: function() {
			var t = this;
			this._updateInitParams();
			if(this.control) {
				var t = this;
				var value = this.config.getParam(this.param);
				var patterns = [
				    {value:"property", re:/@SUBSTITUTE{property\.(.*)}/},
				    {value:"javascript", re:/@SUBSTITUTE{javascript (.*)/m},
				    {value:"initparam", re:/@SUBSTITUTE{javascript return task\.getOpEntry\(\)\.getString\('(.*)'\);}/}
				];
				var label = null;
				if(value) {
					array.forEach(patterns, function(p) {
						var arr = value.match(p.re);
						if(arr) {
							t._property.set("value", arr[1].trim());
							t._javascript.set("value", arr[1].trim());
							t._initparam.set("value", arr[1].trim());
							t._type.set("value", p.value);
							label = arr[1];
						}
					})
				}

				new Button({
					label:"OK",
					onClick: function(evt) {
						t.updateValue();
						t._editor.onCancel();
						event.stop(evt);
					}
				}).placeAt(this._editor.closeButton.domNode, "before");
				
				this._expr = new Link({
					label:"(P): " + label,
					title:"Click to edit expression",
					onClick:lang.hitch(this, "_onEditExpression")
				});
				
//				this._updateWidgets();
				if(this.control) {
					this.own(aspect.after(this.control, "onChange", function(value) {
						t.onChange(value);
					}, true));
				}
				
				this._showControl(label ? this._expr : this.control);
			}
		}
	})
});
