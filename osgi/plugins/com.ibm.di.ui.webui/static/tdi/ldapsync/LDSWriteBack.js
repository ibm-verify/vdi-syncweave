/**
 * The LDSWriteBack widget configures the write-back connector in a flow.
 *
*/

define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/aspect",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dojo/data/ItemFileWriteStore",
	"gridx/Grid", 
	"gridx/core/model/cache/Sync", 
	"gridx/modules/VirtualVScroller", 
	"gridx/modules/ColumnResizer", 
	"gridx/modules/extendedSelect/Row", 
	"gridx/modules/SingleSort", 
	"dojo/store/Memory", 
	"idx/layout/BorderContainer",
	"idx/layout/ContentPane",
	"idx/form/CheckBox",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"idx/dialogs",
	"./LDSUtil",
	"./LDSMap2",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSWriteBack.html"
], function(declare, array, lang, html, aspect, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ItemFileWriteStore,
		Grid, Cache, VirtualVScroller, ColumnResizer, SelectRow, SingleSort, Store,
		BorderContainer, ContentPane, CheckBox, tdiapi, tdiconstants, idx, LDSUtil, LDSMap, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		ignoreUpdates: true,
		
		onModify: function() {
			// summary:
			//		callback function when Join config changes
		},

		getWriteBackConnector: function() {
			return LDSUtil.getWriteBackConnector(this.config.getAssemblyLine());
		},
		
		getWriteBackConfig: function() {
			return this.getWriteBackConnector().getConnectionConfig();
		},
		
		getParam: function(param) {
			return this.getWriteBackConfig().getParam(param);
		},
		
		getParamBoolean: function(param) {
			return this.getWriteBackConfig().getParamBoolean(param);
		},
		
		setParam: function(param, value) {
			var current = this.getWriteBackConfig().getParam(param);
			if(typeof(value) == "boolean" )
				current = this.getWriteBackConfig().getParamBoolean(param);
			
			if(value != current)
				this.getWriteBackConfig().setParam(param, value);
		},
		
		onEnabled: function() {
			var enabled = this.wbEnabled.get("value") == "on";
			this.setParam("writeback.perform", enabled);
			this.onModify(this);
		},
		
		updateValueList: function(source) {
			var t = this;
			var m = source.match(/^\/Connectors\/(\w+)/);
			if(m) {
				source = m[1];
			}
			var conn = t.config.getTop().getConnector(source);
			var arr = [];
			if(conn) {
				array.forEach(conn.getSchema().getNames(), function(key) {
					arr.push(key);
				});
			}
			this.map.set("targetAttributes", arr);
		},
		
		setConfig: function(config) {
			var t = this;
			t.config = config;
			if(t.getParamBoolean("writeback.perform")) {
				t.wbEnabled.set("value", "on");
			}
			
			// -- Always save to <Flow>_WriteBack.map
			var mapname = t.config.getAssemblyLine().getName() + "_WriteBack.map";
			var current = t.getParam("writeback.person.mapFile");
			if(mapname != current)
				t.setParam("writeback.person.mapFile", mapname);
			t.map.setAutoSaveTo(mapname);
			
			// -- mapping from AL -> endpoint
			t.map.set("sourceObject", "work");
			t.map.setFlowContext(t.config.getAssemblyLine(), "writeback");
			
			// -- Since we write back to the endpoint we use the Flow's person map
			// -- as source attributes (or person.map if none specified)
			var output = t.config.getConnector("Output");
			var sourcemap = "";
			if (output && typeof output.getConnectionConfig === "function") {
				sourcemap = output.getConnectionConfig().getParam("target.person.mapFile");
			}
			if(!sourcemap || sourcemap == "")
				sourcemap = "person.map";

			// -- Load maps we need
			LDSUtil.getServerMaps().then(function(data) {
				var sel = array.filter(data, function(item) {
					return item.name == mapname;
				});
				if(sel && sel.length == 1) {
					// -- load existing map
					t.map.setConfig(sel[0]);
				} else {
					// -- else create a new blank map
					t.map.setConfig({
						name:t.map.getAutoSaveTo(),
						map:{}
					});
				}
				
				// -- let map get map names as source attributes
				var src = array.filter(data, function(item) {
					return item.name == sourcemap;
				});
				if(src && src.length == 1) {
					var arr = [];
					for(f in src[0].map) {
						arr.push(f);
					}
					t.map.set("sourceAttributes", arr.sort());
				}
			});
			
			//
			// -- Update targetAttributes when input source changes
			//
			t._modid = aspect.after(t.config, "onModify", lang.hitch(t, function(modified, args) {
				if(args && args.length == 2) {
					var mod = args[1];
					if(mod && mod.param == "complexConfig.inheritFrom") {
						if(mod.source && mod.source.name == "Input") {
							t.updateValueList(t.config.getConnector("Input").getInheritFrom());
						}
					}
				}
			}));
			t.updateValueList(t.config.getConnector("Input").getInheritFrom());
		},
		
		setEndpoints: function(endpoints) {
			this.endpoints = endpoints;
		},
		
		onTabSelected: function() {
			// summary:
			// 		called internally to make sure Gridx resizes correctly
			if(this.map) {
				this.map.resize();
			}
		}
	})
});


