/**
 * The ConditionItem widget shows a list of inline editable rows of ConditionItems
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/TextBox",
	"dijit/form/ComboBox",
	"dijit/form/Select",
	"idx/grid/PropertyFormatter",
	"idx/grid/PropertyGrid",
	"idx/widget/EditController",
	"idx/form/Link",
	"dojo/text!./templates/ConditionItem.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TextBox, ComboBox, Select, PropertyFormatter, PropertyGrid, EditController, Link, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin ],
	{
		templateString : template,
		
		onDelete: function() {
			// summary:
			//		Callback when delete is clicked
		},
		
		onChange: function(data) {
			// summary:
			//		Callback when data has changed
		},
		
		postCreate: function() {
			this.grid.set("editController", this.editController);
			this.grid.set("data", this.config);
			this.connect(this.grid, "onChange", lang.hitch(this, "onChange"));
		}
	
	});
});
