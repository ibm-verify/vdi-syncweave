/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/form/Link",
	"tdi/NlsMixin",
	"./_ALTooltip",
	"dojo/text!./templates/ALConnectorTooltip.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Link, NlsMixin, ALTooltip, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ALTooltip],
	{
		templateString : template,
		
		onEditConnection: function() {
			this.onClick(this, event, "Connection");
		},
		
		onEditLinkCriteria: function() {
			this.onClick(this, event, "LinkCriteria");
		},
		
		onEditParser: function() {
			this.onClick(this, event, "Connection");
		},
		
		onEditAttributeMap: function() {
			this.onClick(this, event, "AttributeMap");
		},
		
		onEditHooks: function() {
			this.onClick(this, event, "Hooks");
		},
		
		getEntryValue: function(attr) {
			if(this.entry && this.entry[attr]) {
				return " (" + this.entry[attr] + ")";
			} else {
				return "";
			}
		},
		
		postMixInProperties: function() {
			this.name = this.config.getName();
			this.mode = this.config.getMode();
			this.type = this.config.getSimpleConnectorType();
			if(!this.type)
				this.type="";
			
			if(this.config.isInput()) {
				this.mapname = "Input";
			} else {
				this.mapname = "Output";
			}
			
			var map = this.config.getAttributeMap(this.config.isInput());
			var names = this.config.getAttributeMap(this.config.isInput()).getNames();
			var arr = new Array();
			for(var i = 0; i < names.length; i++) {
				arr.push("- " + names[i] + this.getEntryValue(names[i]));
			}
			if(arr.length == 0) {
				arr.push("<i>No mapping defined</i>");
			}
			this.mapping = arr.join("<br>");
			
			// -- link criteria
			this.linkcriteria = "";
			this.linkDisplay = "";
			if(this.config.getMode() == "Lookup" || this.config.getMode() == "Update") {
				var arr = new Array();
				array.forEach(this.config.getLinkCriteria().getLinkAttributes(), function(attr) {
					var lc = this.config.getLinkCriteria().getCriteriaFor(attr);
					if(lc) {
						arr.push(lc.attribute + (lc.negate ? " not " : " ") + lc.operator + " " + lc.value);
					}
				}, this);
				this.linkcriteria = arr.join("<br>");
			} else {
				this.linkDisplay = "none";
			}
			
			// -- parameters
			var arr = new Array();
			array.forEach(this.config.getConnectionConfig().getNames(), function(p) {
				var value = this.config.getConnectionConfig().getParamByName(p);
				if(value) {
					arr.push(p + ": " + value.value);
				}				
			}, this);
			this.connectionparams = arr.join("<br>");
			
			// -- parser parameters
			var arr = new Array();
			this.parserDisplay = "";
			if(this.config.getParserType()) {
				array.forEach(this.config.getParserConfig().getConfig().getNames(), function(p) {
					var value = this.config.getParserConfig().getConfig().getParamByName(p);
					if(value) {
						arr.push(p + ": " + value.value);
					}				
				}, this);
			} else {
				this.parserDisplay = "none";
			}
			this.parserparams = arr.join("<br>");
			
			// -- hooks
			var nls = new NlsMixin();
			var arr = new Array();
			array.forEach(this.config.getHookNames(), function(name) {
				arr.push(nls.getString("Hook." + name));
			}, this);
			if(arr.length == 0)
				arr.push("<i>No active hooks</i>");
			this.hooks = arr.join("<br>");
			this.hooksDisplay = "";
			
		}
	
	})
});
