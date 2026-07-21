/**
 * The LDSAttributeMaps shows all LDAPSync map files and lets the user edit/add/delete them.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Container",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/widget/Dialog",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/TextBox",
	"idx/layout/TitlePane",
	"idx/dialogs",
	"tdi/tdiutil",
	"./LDSMap3",
	"./LDSUtil",
	"tdi/TableWidget",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSAttributeMaps2.html"
], function(declare, array, lang, html, _Container, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, Button, CheckBox, TextBox, TitlePane, idx, tdiutil, LDSMap, LDSUtil, TableWidget, nls, template) {
	
return declare(
	[ _Container, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		createItems: function() {
			var t = this;
			LDSUtil.getServerMaps().then(function(data) {
				t.mapData = data;
				t.createTable(data);
			},
			function error(err) {
				tdiutil.error(err);
			});
		},
		
		createTable: function(data) {
			var t = this;
			t.mapData = data;
			t.mapDataKey = {};
			var arr = array.map(data, function(item) {
				t.mapDataKey[item.name] = item;
				return {
					name:item.name
				}
			});
			this.grid = new TableWidget({
				idProperty:"name",
				structure: [
				    {	field:"name",
				    	id:"name",
				    	name:this.getString("WebCE.name"),
				    	width:"auto"
				    }
				],
				onSelected: lang.hitch(this, "editMap"),
				data:arr
			});
			this.grid.startup();
			this.grid.sort("name", true);
			this._maps.set("content", this.grid);
			this._maps.resize();
		},
		
		editMap: function(item) {
			this.addItem(this.mapDataKey[item.id]);
		},
		
		addItem: function(map) {
			var t = this;
			var mapwidget = new LDSMap({
				onDeleteMap:lang.hitch(this, "deleteMap"),
				onDuplicateMap:lang.hitch(this, "duplicateMap"),
				projectconfig:t.config,
			});
			mapwidget.startup();
			t._editor.set("title", map.name);
			t._editor.set("content", mapwidget);
			mapwidget.setConfig(map);
		},
		
		resize: function() {
			
		},
		
		getTitle: function() {
			return this.getString("WorkEntryWidget.2");
		},
		
		deleteMap: function(map) {
			var t = this;
			idx.confirm(this.getString("FDS.deleteMap", [map.config.name]), function ok() {
				if(map.config.isnew) {
					t.grid.deleteItem(map.config.name);
				} else {
					LDSUtil.deleteServerMap(map.config.name).then(function() {
						t.grid.deleteItem(map.config.name);
					}, tdiutil.error);
				}
				t.mapDataKey[map.config.name] = null;
				t.mapData = array.filter(t.mapData, function(item) {
					return item.name != map.config.name;
				});
				t._editor.set("title","");
				t._editor.set("content","");
			});
		},
		
		duplicateMap: function(map) {
			var t = this;
			tdiutil.prompt({label:this.getString("FDS.newMap"), regExp:"^[A-Z|a-z|0-9|_|-]+$"}, function(args) {
				var name = args && args.length ? args[0] : null;
				if(name) {
					if(!name.match(/\.map$/))
						name += ".map";
					var arr = array.filter(t.mapData, function(map) {
						return map.name == name;
					});
					if(arr.length > 0) {
						idx.error(t.getString("FDS.newMapExists"));
					} else {
						var config = tdiutil.clone(map.config);
						config.name = name;
						config.isnew = true;
						t.addMapItem(config);
						t.grid.selectById(config.name, true);
					}
				}
			}, this.getString("FDS.duplicate"));
		},
		
		addMapItem: function(map) {
			this.grid.addItem(map);
			this.mapData.push(map);
			this.mapDataKey[map.name] = map;
		},
		
		createMap: function() {
			
		},
		
		confirmClose: function() {
			if(array.some(this.getChildren(), function(child) {
				return child.content.isModified();
			})) {
				return this.getString("FDS.abandonChanges");
			}
			return null;
		},
		
		createNewMap: function() {
			var div = html.create("div", {innerHTML:this.getString("FDS.newMap")});
			var text = new TextBox({style:"width:20em"}).placeAt(div, "last");
			var dlg = new Dialog({
				content:div,
				title:this.getString("WebCE.newItem"),
				closeButtonLabel:this.getString("WebCE.cancel"),
				onExecute:function() {
					alert("Create map: " + text.get("value"));
					return false;
				},
				buttons: [new Button({label:this.getString("WebCE.ok"), onClick:function() {
					dlg.onExecute();
				}})]
			})
			dlg.show();
		},
		
		createToolbar: function() {
//			this._maps.addChild(new Button({
//				label:this.getString("WebCE.newItem"),
//				onClick:lang.hitch(this, "createNewMap"),
//				region:"majorActions"
//			}));
		},
		
		startup: function() {
			this.inherited(arguments);
			this.createItems();
			this.createToolbar();
			this._maps.resize();
		}
	});
});
		
