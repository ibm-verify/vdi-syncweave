/**
 * The LDSAttributeMaps shows all LDAPSync map files and lets the user edit/add/delete them.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Container",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/layout/TitlePane",
	"idx/dialogs",
	"tdi/tdiutil",
	"./LDSMap2",
	"./LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSAttributeMaps.html"
], function(declare, array, lang, _Container, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, Button, CheckBox, TitlePane, idx, tdiutil, LDSMap, LDSUtil, nls, template) {
	
return declare(
	[ _Container, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		createItems: function() {
			var t = this;
			LDSUtil.getServerMaps().then(function(data) {
				t.mapData = data;
				array.forEach(data, function(map) {
					t.addItem(map);
				});
			},
			function error(err) {
				tdiutil.error(err);
			});
		},
		
		addItem: function(map) {
			var t = this;
			var mapwidget = new LDSMap({
				config:map,
				onDeleteMap:lang.hitch(this, "deleteMap"),
				onDuplicateMap:lang.hitch(this, "duplicateMap"),
				projectconfig:t.config,
			});
			mapwidget.startup();
			var pane = new TitlePane({
				content:mapwidget,
				open:false,
				style:"margin-bottom:5px",
				title:map.name
			});
			
			// -- this.own it so it gets destroyed when this widget is destroyed
			var handle = this.own(pane.watch("open", function(prop, oldval, value) {
				if(value) {
					mapwidget.setConfig(mapwidget.config);
					mapwidget.resize({h:400});
					// only load it once (own returns array)
					handle[0].remove();
				}
			}));
			
			var arr = array.filter(t.getChildren(), function(child) {
				return child.title > map.name;
			});
			if(arr.length > 0) {
				this.addChild(pane, this.getIndexOfChild(arr[0]));
			} else {
				this.addChild(pane);
			}
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
					t.removeChild(map.getParent());
					map.getParent().destroyRecursive();
				} else {
					LDSUtil.deleteServerMap(map.config.name).then(function() {
						t.removeChild(map.getParent());
						map.getParent().destroyRecursive();
					}, tdiutil.error);
				}
			});
		},
		
		duplicateMap: function(map) {
			var name = prompt(this.getString("FDS.newMap"));
			if(name != null) {
				if(!name.match(/\.map$/))
					name += ".map";
				var arr = array.filter(this.mapData, function(map) {
					return map.name == name;
				});
				if(arr.length > 0) {
					idx.error(this.getString("FDS.newMapExists"));
				} else {
					var config = tdiutil.clone(map.config);
					config.name = name;
					config.isnew = true;
					this.addItem(config);
				}
			}
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
		
		startup: function() {
			this.inherited(arguments);
			this.createItems();
		}
	});
});
		
