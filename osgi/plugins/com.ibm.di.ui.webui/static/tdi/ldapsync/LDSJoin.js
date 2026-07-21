/**
 * The LDSJoin widget configures the Join connector in a flow.
 *
 * 

JOIN behavior properties
    join.perform= true | yes | false | no  (turns on/off augment)
    join.ifJoinFails= ignore | skip | skip entry | abort (middle two are the same)

  JOIN search specification
    join.search.filter= custom scripted search filter. 
                        !! If you have this, the next 5 properties are ignored
    join.search.attribute= name of the attribute to search for in the
                           connected system (left side of Link Crit)
    join.search.operator= the operator. Supported operations are 
     (same as for Link Crit)
      equal               : equal | = | ==
                           ! is the default value, so you can even 
                             skip having this property!
      lessThan            : lessthan | less than | <
      lessThanOrEqual     : lessthanorequal | <= | =<
      greaterThan         : greaterthan | greater than | >
      greaterThanOrEqual  : greaterthanorequal | >= | =>
      subStringSearch     : contains
      startsWith          : startswith | starts with | ^
      endsWith            : endswith | ends with | $
      notEquals           : notequals | not equals | != | =!=
    join.search.value= right side of Link Crit: $AttrName | @AttrName | {expr}
    join.search.negate= true | yes | false | no
    join.search.combine= and | or

  MAPS (optional)
      join.person.mapFile= map filename/path | Attribute Map name
      join.group.mapFile= ditto
      join.<container objectclass>.mapFile= ditto

  CUSTOM ALs (optional)
      join.person.AL= Name of AL to perform lookup/join. Can be in other config:
                          join.person.AL=MyConfig:/AssemblyLines/MyAL
                       Default value is "JoinEntry" which is part of LDAPSync
      join.group.AL= ditto
      join.container.AL= ditto

  FOR DEFAULT AL (JoinEntry) you must set the following properties
    Note: these are consistent with those properties for non-LDAP source
      join.connector.name= connector name (ibmdi.JDBC, ibmdi.LDAP, ..)
      join.connector.parameter.*= where * is internal parameter name

*/

define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/aspect",
	"dojo/data/ItemFileWriteStore",
	"dojo/topic",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/ComboBox",
	"dijit/form/Form",
	"dijit/form/Textarea",
	"dijit/MenuItem",
	"tdi/TableWidget",
	"tdi/ldapsync/LDSMap2",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"idx/dialogs",
	"idx/layout/HeaderPane",
	"./LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSJoin.html"
], function(declare, array, lang, html, aspect, ItemFileWriteStore, topic, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, TabContainer, 
		Button, CheckBox, ComboBox, Form, Textarea, MenuItem, TableWidget, tdiapi, LDSMap2, tdiconstants, tdiconfigentry, tdicientry, idx, HeaderPane, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,
		ignoreUpdates: true,
		
		onModify: function() {
			// summary:
			//		callback function when Join config changes
		},

		getJoinConfig: function() {
			return this.getJoinConnector().getConnectionConfig();
		},
		
		getJoinConnector: function() {
			var t = this;
			var join = t.config.getConnector("Join");
			if(!join) {
				join = t.config.createDataFlowConnector("Join");
				join.setConnectorType("system:/Connectors/ibmdi.ScriptConnector");
				join.setState("Disabled");
			}
			return join;
		},
		
		getJoinConfig: function() {
			return this.getJoinConnector().getConnectionConfig();
		},
		
		getParam: function(param) {
			return this.getJoinConfig().getParam(param);
		},
		
		getParamBoolean: function(param) {
			return this.getJoinConfig().getParamBoolean(param);
		},
		
		setParam: function(param, value) {
			var current = this.getJoinConfig().getParam(param);
			if(typeof(value) == "boolean" )
				current = this.getJoinConfig().getParamBoolean(param);
			
			if(value != current)
				this.getJoinConfig().setParam(param, value);
		},
		
		onSelectEndpoint: function(event) {
			var conn = this.getJoinConnector();
			var current = conn.getInheritFrom();
			if(event != current) {
				conn.setInheritFrom(event);
				this.updateAttributeList(event);
				this.onModify(this);
			}
		},
		
		onEnabled: function() {
			var enabled = this.joinEnabled.get("value") == "on";
			this.setParam("join.perform", enabled);
			this.joinEndpoint.set("disabled", !enabled);
			this.onModify(this);
		},
		
		onFormChanged: function(event) {
			if(!this.ignoreUpdates) {
				var values = this.Form.get("value");
				for(f in values) {
					var prop = f.replace(/_/g, ".");
					this.setParam(prop, values[f]);
				}
			}
		},
		
		updateAttributeList: function(source) {
			var t = this;
			var m = source.match(/^\/Connectors\/(\w+)/);
			if(m) {
				source = m[1];
			}
			var conn = t.config.getTop().getConnector(source);
			var arr = [];
			if(conn) {
				array.forEach(conn.getSchema().getNames(), function(key) {
					arr.push({id:key, name:key, label:key});
				});
			}
			var store = new ItemFileWriteStore({data:{
				identifier:"id",
				label:"label",
				items:arr.sort()
			}});
			this._joinSearchAttrs.set("store", store);
			this._attrmap.set("sourceAttributes", store);
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
					arr.push({id:key, name:key, label:key});
				});
			}
			var store = new ItemFileWriteStore({data:{
				identifier:"id",
				label:"label",
				items:arr.sort()
			}});
			this._joinSearchValue.set("store", store);
		},
		
		setConfig: function(config) {
			var t = this;
			
			t.ignoreUpdates = true;
			t.config = config;
			if(t.getParamBoolean("join.perform")) {
				t.joinEnabled.set("value", "on");
			} else {
				t.joinEndpoint.set("disabled", true);
			}
			
			var config = t.getJoinConfig();
			var value = {};
			array.forEach(config.getNames(), function(param) {
				value[param.replace(/\./g, "_")] = config.getParam(param)
			});
			t.Form.set("value", value);

			// custom link criteria
			t.lastScriptedCriteria = config.getParam("join.search.filter") || "";
			var customLink = !(t.lastScriptedCriteria.trim() == "");
			if(customLink)
				t.customScript.set("value", "on");

			// let events drain before we start listening to changes
			setTimeout(function() {
				t.ignoreUpdates = false;
			}, 500);

			t.updateAttributeList(t.getJoinConnector().getInheritFrom());
			t.updateValueList(t.config.getConnector("Input").getInheritFrom());

			LDSUtil.getServerMaps().then(function(data) {
				t.serverMaps = data;
				t.loadMap();
			});

			//
			// -- Update drop-down values when target maps change
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
		},
		
		loadMap: function() {
			var t = this;
			
			t._attrmap.setFlowContext(t.config, "join");
			
			var name = t.getParam("join.person.mapFile") || "";
			var arr = array.filter(this.serverMaps, function(map) {
				return map.name == name;
			});
			if(arr && arr.length == 1) {
				t._attrmap.setConfig(arr[0]);
				//t._attrmap.setAutoSaveTo()
			} else {
				// push an empty config to populate map
				t._attrmap.setConfig({map:{}});
			}
			t._attrmap.setAutoSaveTo(t.config.getAssemblyLine().getName() + "_join.map");
			t._attrmap.resize();
			t._attrmap.onAutoSaved = function(cfg) {
				t.setParam("join.person.mapFile", cfg.name);
			};
		},
		
		setEndpoints: function(endpoints) {
			this.endpoints = endpoints;
			var current = this.getJoinConnector().getInheritFrom();
			this.joinEndpoint.setStore(endpoints, current);
		},
		
		toggleLink: function(val) {
			html.style(this.customFilter, "display", val ? "" : "none");
			html.style(this.filter, "display", val ? "none" : "");
			
			// -- if custom script is enabled populate with last value or single space
			// -- to force a config save.
			if(val) {
				if(!this.lastScriptedCriteria) {
					this.lastScriptedCriteria = " ";
				}
				this.Form.set("value", {join_search_filter:this.lastScriptedCriteria});
			} else {
				this.lastScriptedCriteria = this.Form.get("value")["join_search_filter"];
				this.Form.set("value", {join_search_filter:""});
			}
		},

		resize: function(obj) {
			if(this.Form) {
				this.Form.resize(obj);
			}
			if(this._attrmap) {
				this._attrmap.resize();
			}
		},
		

		postCreate: function() {
			var t = this;
			topic.subscribe("ldapsync/endpoints", function() {
				t.setEndpoints(t.endpoints);
			});
		}
	})
});


