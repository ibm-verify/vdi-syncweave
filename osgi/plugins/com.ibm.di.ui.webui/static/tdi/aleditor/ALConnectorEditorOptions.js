/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/tdiutil",
	"tdi/model/ComponentsModel",
	"dojo/text!./templates/ALConnectorEditorOptions.html"
], function(declare, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiutil, TDIComponentsModel, template) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString: template,
		
		baseClass: "tdiWebDevNavPane",
		
		onModeChange: function(value) {
			if(this.config.getMode() != value) {
				this.config.setMode(value);
			}
		},

		_onTypeChange: function(item) {
			var t = this;
			if(item == this.config.getSimpleConnectorType())
				return;
			tdiutil.confirm("You are about to change the connector type", function(ok) {
				if(ok == 0 && item != null && item.length > 0) {
					try {
						if(item.match(/^adapter:/) || item.match(/^\//))
							t.config.setConnectorType(item);
						else
							t.config.setConnectorType("system:/Connectors/" + item);
						t.onTypeChange(item);
					} catch(err) {
						alert("setInherit: " + item + ": " + err);
					}
				}
			});
		},
		
		onTypeChange: function(value) {
		},
		
		postCreate: function() {
			var t = this;
			
			t._mode.set("value", this.config.getMode());
			var model = new TDIComponentsModel({
				componentType:"connector",
				onLoadComplete: function(model) {
					t._type.set("store", model);	
					t._type.set("value", t.config.getSimpleConnectorType());	
				}
			});
		}
	})
});
