define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/attmapconfig",
	"tdi/config/schemaconfig",
	"tdi/config/connectionconfig",
	"tdi/config/parserconfig",
	"tdi/config/linkcriteria",
	"tdi/tdiapi"
], function(declare, tdibasecfg, tdiattmapconfig, tdischemaconfig, tdiconnectionconfig, tdiparserconfig, tdilinkcriteria, tdiapi) {
return declare("tdi.config.connector",
	[tdibasecfg],
	{
// 	dojo.declare("tdi.connector", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this._maps = new Object();
		},
		
		requiresLinkCriteria: function() {
			// summary:
			//		Returns true if this component requires link criteria
			return this.getMode() == "Lookup" ||
				this.getMode() == "Update" ||
				this.getMode() == "Delta" ||
				this.getMode() == "Delete";
		},
		
		isInput : function() {
			// summary:
			//		returns true if mode is Iterator, Server or Lookup
			return this.isFunction() || this.getMode() == "Iterator" || this.getMode() == "Lookup" || this.getMode() == "Server"
		},
		
		isOutput : function() {
			// summary:
			//		returns true if output map is used
			if(this.isInput())
				return this.isFunction() || this.getMode() == "Server";
			else
				return true;
		},
		
		isAssemblyLineConnector: function() {
			return /ibmdi.AssemblyLineConnector|ibmdi.AssemblyLineFC/.test(this.getInheritFrom());
		},
		
		getConnectorType : function() {
			var ref = this.getConnectionConfig().getObject("inheritFrom");
			if(ref == undefined || ref == "[parent]") {
				ref = this.getObject(this.getKey("inheritFrom"))
			}
			if(ref == undefined)
				return ref;
			
			var match = ref.match("^/Connectors/(.*)");
			if(match && match.length == 2) {
				var conn = this.getTop().getConnector(match[1]);
				return conn.getConnectorType();
			}
			
			match = ref.match("(.*):/(Connectors)/(.*)");
			if(match && match.length == 4 && match[1] != "system") {
				var ns = tdiapi.getNamespace(match[1]);
				if(ns) {
					var inh = ns.getConnector(match[3]);
					return inh.getConnectorType();
				}
			}
			
			match = ref.match("http[s]?:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
			if(match && match.length == 4 && match[1] != "system") {
				var ns = tdiapi.getNamespace(match[1]);
				if(ns) {
					var inh = ns.getConnector(match[3]);
					ref = inh.getConnectorType();
				}
			}
			
			return ref;
		},
		
		getParserType : function() {
			var ref = this.getParserConfig().getObject("inheritFrom");
			if(ref == undefined || ref == "[parent]") {
				ref = this.getObject(this.getKey("inheritFrom"))
			}
			if(ref == undefined)
				return ref;
			
			var match = ref.match("^/Parsers/(.*)");
			if(match && match.length == 2) {
				var conn = this.getTop().getParser(match[1]);
				return conn.getParserType();
			}
			
			match = ref.match("(.*):/(Parsers)/(.*)");
			if(match && match.length == 4 && match[1] != "system") {
				var ns = tdiapi.getNamespace(match[1]);
				if(ns) {
					var inh = ns.getParser(match[3]);
					if(inh)
						return inh.getParserType();
					else
						return null;
				}
			} else if(match && match.length == 4 && match[1] == "system") {
				return match[3];
			}
			
			match = ref.match("http[s]?:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
			if(match && match.length == 4 && match[1] != "system") {
				var ns = tdiapi.getNamespace(match[1]);
				if(ns) {
					var inh = ns.getParser(match[3]);
					if(inh)
						ref = inh.getParserType();
					else
						return null;
				}
			} else if(match && match.length == 4 && match[1] == "system") {
				return match[3];
			}
			
			return null;
		},
		
		getSimpleConnectorType : function() {
			var con = this.getConnectorType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
			return con;
		},
		
		setConnectorType : function(type) {
			this.setObject(this.getKey("inheritFrom"), type);
		},
		
		setEnabled: function(enabled) {
			this.setState(enabled ? "Enabled" : "Disabled");
		},
		
		getEnabled: function() {
			return "Enabled" == this.getState();
		},
		
		setState : function(state) {
			this.setObject("state", state);
		},
		
		getState : function() {
			return this.getObject("state");
		},
		
		setInitializeOption: function(opt) {
			// summary:
			//		onStartup, onFirstUse, onEveryUse, onConfigModify
			this.setObject("initialize", opt);
		},
		
		getInitializeOption: function() {
			
		},
		
		getMap : function(map, name) {
			var arr = dojo.filter(map, function(item) {
				return item.name == name;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		getAttributeMap : function(input) {
			if(typeof(input) == "undefined")
				input = this.isInput();
			var key = "Attributemap_" + input;
			if(this._maps[key] == null) {
				var map = this.getObject(this.getKey("map"));
				this._maps[key] = new tdiattmapconfig({config:this.getMap(map, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getSchema : function(input) {
			if(typeof(input) == "undefined")
				input = this.isInput();
			var key = "Schema_" + input;
			if(this._maps[key] == null) {
				var schema = this.getObject(this.getKey("schema"));
				this._maps[key] = new tdischemaconfig({config:this.getMap(schema, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getConnectionConfig : function() {
			var key = "ConnectionConfig";
			if(this._maps[key] == null) {
				this._maps[key] = new tdiconnectionconfig({config:this.getObject(this.getKey("rawConfig")), parentConfig:this});
			}
			return this._maps[key];
		},
			
		getParserConfig : function() {
			var key = "ParserConfig";
			if(this._maps[key] == null) {
				this._maps[key] = new tdiparserconfig({config:this.getObject(this.getKey("parser")), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getMode : function() {
			return this.getObject(this.getKey("mode"));
		},
		
		setMode : function(mode) {
			this.setObject(this.getKey("mode"), mode);
			return this.getMode();
		},
		
		_getHooks : function() {
			if(this._hooks == null) {
				this._hooks = new Object();
				var arr = this.getObject("hook", this.getObject(this.getKey("hooks")));
				if(arr != null) {
					dojo.forEach(arr, dojo.hitch(this, function(hook) {
						this._hooks[hook.name] = new tdibasecfg({config:hook, parentConfig:this});
					}));
				} else {
					// hook array missing; add it
					var obj = this.getObject(this.getKey("hooks"));
					if(obj) {
						obj.hook = [];
					}
				}
			}
			return this._hooks;
		},
		
		setHookInheritance: function(inh) {
			this.getObject(this.getKey("hooks")).inheritFrom = inh;
		},
		
		getHookInheritance: function() {
			return this.getObject(this.getKey("hooks")).inheritFrom;
		},
		
		getHookNames : function() {
			this._getHooks();
			var arr = new Array();
			for(var h in this._hooks) {
				arr.push(h);
			}
			return arr;
		},
		
		getHook : function(name, create) {
			this._getHooks();
			var hook = this._hooks[name];
			if(!hook && create) {
				var arr = this.getObject("hook", this.getObject(this.getKey("hooks")));
				var hookdata = {
					name:name,
					enabled:true
				};
				arr.push(hookdata);
				this._hooks[name] = hook = new tdibasecfg({config:hookdata, parentConfig:this});
				this.setModified(true);
			}
			return hook;
		},
		
		getLinkCriteria : function() {
			if(this._linkCriteria == null) {
				if(this.config.complexConfig) {
					if(this.config.complexConfig.linkCriteria === undefined) {
						this.config.complexConfig.linkCriteria = {};
					}
				} else {
					if(this.config.linkCriteria === undefined) {
						this.config.linkCriteria = {};
					}
				}
				this._linkCriteria = new tdilinkcriteria({config:this.getObject(this.getKey("linkCriteria"))});
			}
			return this._linkCriteria;
		}
		
	});

});
