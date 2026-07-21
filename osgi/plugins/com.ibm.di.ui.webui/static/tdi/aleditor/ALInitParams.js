define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/form/Button",
	"tdi/tdiconfig",
	"tdi/TableWidget",
	"tdi/aleditor/ALInitParam",
	"tdi/aleditor/Border"
], function(declare, lang, array, Widget, TemplatedMixin, Button, tdiconfig, TableWidget, ALInitParam, Border) {

return declare(
	[Widget, TemplatedMixin],	
	{
		templateString: "<div data-dojo-attach-point='Main' style='width:100%; height:100%; border:1; border-color:red; margin:0; padding:0'></div>",
		
		// config: tdi.assemblyline
		config: null,
		
		setConfig: function(config) {
			this.config = config;
			if(this.config) {
				var initParams = this.config.getInitParams();
				var data = [];
				array.forEach(initParams.getNames(), lang.hitch(this, function(name) {
					var sic = initParams.getItem(name);
					data.push({
						id:sic.getName(),
						name:sic.getName()
					})
				}));
				this.grid.setData(data);
			}
		},
		
		addInitParam: function() {
			if(this.grid) {
				var str = prompt("Name: ");
				if(str) {
					this.grid.addItem({id:str, name:str});
					this.config.getInitParams().newItem({name:str});
				}
			}
		},

		deleteInitParam: function() {
			this.grid.deleteItem();
		},
		
		resize: function(obj) {
			if(this.border && obj && obj.h > 0) {
				this.border.resize(obj);
			}
		},
		
		_setHeaderPaneAttr: function(hp) {
			hp.addChild(new Button({
				label:"Add",
				onClick:lang.hitch(this, "addInitParam"),
				region:"minorActions"
			}));
			
			hp.addChild(new Button({
				label:"Delete",
				onClick:lang.hitch(this, "deleteInitParam"),
				region:"minorActions"
			}));
			
		},
		
		onSelected: function(row) {
			if(!this.editor) {
				this.editor = new ALInitParam({style:"margin:5px"});
				this.editor.startup();
				this.border.setRight(this.editor, {style:"width:50%; height:100%; margin:0; padding:0", splitter:true});
			}
			this.editor.setConfig(this.config, row.id);
		},
		
		postCreate: function() {
			
			this.border = new Border({style:"width:100%; height:100%; margin:0; padding:0"}).placeAt(this.Main);
			this._supportingWidgets.push(this.border);
			
			var layout = new Array();
			layout.push({id:"name", name:"Name", editable:true, editor:"dijit.form.TextBox"});
			this.grid = new TableWidget({
				structure:layout,
				style:"width:100%; height:100%; margin:0; padding:0"
			}); //.placeAt(this.Main);
			this.grid.startup();
			this.grid.setData([]);
			this._supportingWidgets.push(this.grid);
			
			this.connect(this.grid, "onSelected", "onSelected");
			this.connect(this.grid, "onRowDblClick", "onSelected");
			
			this.border.setCenter(this.grid, {style:"margin:0; padding:0", splitter:true});
			this.border.startup();
			
			if(this.config) {
				this.setConfig(this.config);
			}
		}
	}
)});