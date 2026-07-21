/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/dialogs",
	"./LDSMapItem",
	"./LDSUtil",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSMap.html"
], function(declare, array, lang, html, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Button, CheckBox, idx, LDSMapItem, LDSUtil, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		itemList: [],
		
		createItems: function() {
			var t = this;
			var f = null;
			
			array.forEach(t.itemList, function(it) {
				try {
					it.destroyRecursive();
				} catch(err) {
					; // ignore - probably destroyed already
				}
			});
			t.itemList = [];
			
			var arr = [];
			for(f in t.config.map) {
				arr.push(f);
			}
			array.forEach(arr.sort(), function(f) {
				var item = t.config.map[f];
				t.addItem(item);
			});
		},
		
		addItem: function(item, loc) {
			var t = this;
			var mapitem = new LDSMapItem({
				item:item
			}).placeAt(t.tableBody, loc ? loc : "last");
			mapitem.startup();
			mapitem.onDeleteAttribute = lang.hitch(this, function(map, item) {
				idx.confirm(this.getString("FDS.deleteAttributeMapItem",[item.name]),
					function ok() {
						delete t.config.map[item.name];
						map.destroyRecursive();
						t._saveMap.set("disabled", false);
					}
				);
			}, mapitem, item);
			mapitem.watch("item", lang.hitch(t, "onModify"));
			t.itemList.push(mapitem);
		},
		
		onAddAttribute: function() {
			this.addItem({name:"", script:"", add:true, mod:true}, "first");
		},
		
		onModify: function(prop, item, values) {
			if(!values.name || values.name == "") {
				return;
			}
			if(!item.name || item.name == "") {
				this.config.map[values.name] = values;
			} else {
				if(values.name != item.name) {
					this.config.map[values.name] = this.config.map[item.name];
					delete this.config.map[item.name];
					item.name = values.name;
				}
				var f;
				for(f in values) {
					this.config.map[item.name][f] = values[f];
				}
			}
			this._saveMap.set("disabled", false);
			
			if(this.customMapName) {
				if(!this._saveHandle) {
					this._saveHandle = setTimeout(lang.hitch(this, "autoSave"), 3000);
				}
			}
		},
		
		_onDeleteMap: function() {
			this.onDeleteMap(this);
		},
		
		onDeleteMap: function() {
			// summary:
			//		callback
		},
		
		_onDuplicateMap: function() {
			this.onDuplicateMap(this);
		},
		
		onDuplicateMap: function() {
			// summary:
			//		callback
		},
		
		onSaveMap: function() {
			var t = this;
			dojo.when(LDSUtil.saveServerMap(this.config), function() {
				t._saveMap.set("disabled", true);
				t.config.isnew=false;
				topic.publish("ldapsync/maps", "save");
				idx.info(this.getString("FDS.mapSaved"));
			},
			function(err) {
				tdiutil.error(err);
			});
		},
		
		isModified: function() {
			return !this._saveMap.get("disabled");
		},
		
		useMap: function(config) {
			var t = this;
			t.config = config;
			t.createItems();
			if(!t.config.isnew) {
				setTimeout(function() {
					t._saveMap.set("disabled", true)
				}, 1000);
			}
		},
		
		setAutoSaveTo: function(customMapName) {
			this.customMapName = customMapName;
		},
		
		getAutoSaveTo: function() {
			return this.customMapName;
		},
		
		autoSave: function() {
			var t = this;
			t.config = tdiutil.clone(t.config);
			t.config.name = t.customMapName;
			t.config.label = t.customMapName;
			t.config.value = t.customMapName;
			dojo.when(LDSUtil.saveServerMap(t.config), function() {
				t._saveMap.set("disabled", true);
				t.config.isnew=false;
				delete t._saveHandle;
				t.onAutoSaved(t.config);
			},
			function(err) {
				tdiutil.error(err);
				delete t._saveHandle;
			});
		},
		
		onAutoSaved: function() {
			// summary:
			//		Callback after auto save
		},
		
		addToolbarItem: function(child, position) {
			this.toolbar.addChild(child, position);
		},
		
		startup: function() {
			var t = this;
			this.inherited(arguments);
			if(t.config) {
				t.useMap(t.config);
			}
			if(t.hidefilebuttons) {
				html.style(t._saveMap.domNode, "display", "none");
				html.style(t._duplicateMap.domNode, "display", "none");
				html.style(t._deleteMap.domNode, "display", "none");
			}
		}
	});
});
		
