/**
 * The Card displays a summary of an item and a tooltip with all details
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Tooltip",
	"dojo/text!./templates/Card.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TooltipDialog, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : template,
		
		constructor: function(args) {
			if(args)
				dojo.safeMixin(this, args);
			this.cn = this.getField(["cn"], "");
			this.title = this.getField(["jobresponsibilities", "title"], "");
			this.country = this.getField(["co"], "");
			this.mobile = this.getField(["mobile"], "");
			this.mail = this.getField(["mail"], "");
		},

		getField: function(fields, defval) {
			var value = defval;
			var item = this.item;
			array.forEach(fields, function(field) {
				if(lang.isArray(item[field])) {
					value = item[field][0];
				} else if(item[field]) {
					value = item[field];
				}
			});
			return value;
		},
		
		showEntry: function(event) {
		},
		
		postMixinProperties: function() {
			this.inherited(arguments);
		},
		
		postCreate: function() {
			this.inherited(arguments);
//			this.tooltip = new TooltipDialog({
//				content:xxx
//			});
		}

	})
});
