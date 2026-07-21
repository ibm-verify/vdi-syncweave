/**
 * ALProjects contains widgets to display projects/assemblylines and controls to manipulate them.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Tree",
	"dojo/data/ItemFileWriteStore",
	"dijit/tree/ForestStoreModel",
	"dojo/text!./templates/ALProjects.html"
], function(declare, array, lang, Widget, TemplatedWidget, WidgetsInTemplateMixin, Tree, Store, TreeModel, template) {
	
	return declare(
	[Widget, TemplatedWidget, WidgetsInTemplateMixin],
	{
		templateString: template,
		
		onChange: function(value) {
		},
		
		onSelectionChanged: function(value) {
		},
		
		onAddAssemblyline: function() {
		},
		
		_onDeleteAssemblyline: function() {
			if(this.grid.selectedItem && this.grid.selectedItem) {
				this.onDeleteAssemblyline(this.grid.selectedItem.id[0]);
			}
		},
		
		onDeleteAssemblyline: function(alname) {
			alert(alname);
		},
		
		setAssemblyLineData: function(data) {
			var store = new Store({
				data:{
					identifier:"id",
					label:"id",
					items:data
				}
			});
			
			var model = new TreeModel({store:store, rootId:"bongo", rootLabel:"funky"});
			
			if(this.grid) {
				this.grid.destroyRecursive();
				this.grid = null;
			}
			
			this.grid = new Tree({
				model:model,
				showRoot: false
	 		});
			this.grid.startup();
			this._assemblylinesNode.set("content", this.grid);
			this.own(
				this.grid.watch(
					"selectedItems",
					lang.hitch(this, "_openAssemblyLine")
				)
			);
		},
		
		_openAssemblyLine: function(name, oldSelection, selection) {
			var t = this;
			array.forEach(selection, function(item) {
				t.onSelectionChanged(item.id);
			});
		},
		
		resize: function(obj) {
			this.inherited(arguments);
			this._border.resize(obj);
			this._border.layout();
			this._assemblylinesNode.resize();
		},
		
		postCreate: function() {
			this.inherited(arguments);
		}
	});
});
