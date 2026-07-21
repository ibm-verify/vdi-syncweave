define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/on",
	"tdi/tdiapi"
], function(declare, lang, on, tdiapi) {
return declare(
	null,
	{
// 	dojo.declare("tdi.basecfg", null, {

		// The JSON config data
		config: null,
		
		// Parent config
		parentConfig: null,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		isAssemblyLine : function() {
			// summary:
			//		returns true if this is an assemblyline config
			return this.getType() == "assemblyLine";
		},
		
		isContainer : function() {
			// summary:
			//		returns true if this is an composite config with children
			if(this.declaredClass == "tdi.config.tdicontainer")
				return true;
			else if(this.getType)
				return this.getType() == "composite";
			else
				return false;
		},
		
		isConnector : function() {
			// summary:
			//		returns true if this is an connector config
			return this.getSubType() == "connector";
		},
		
		isFunction: function() {
			// summary:
			//		returns true if this is an connector config
			return this.getSubType() == "function";
		},
		
		isScript : function() {
			// summary:
			//		returns true if this is a script component
			return this.getSubType() == "script";
		},
		
		isBranch: function() {
			// summary:
			//		returns true if this is a script component
			return this.getSubType() == "branch";
		},
		
		isLoop: function() {
			// summary:
			//		returns true if this is a script component
			return this.getSubType() == "loop";
		},
		
		isExternalRef: function(ref) {
			// summary:
			//		returns true if ref is a reference to a different config
			if(!ref)
				return false;
			else
				return ref.indexOf(":") != -1;
		},
		
		getType : function() {
			// summary:
			//	returns the type of config as designated by the @type property
			return this.config ? this.config["@type"] : null;
		},
		
		getSubType : function() {
			// summary:
			//		Returns the subtype of the configuration. Some configuration are complex, simple etc
			// 		and the main type is "complex" whereas the subtype is complexConfig.type (e.g connector, function)
			return this.getObject(this.getKey("@type"));
		},
		
		onModify : function(modified, params) {
			// summary:
			//		Called when the modified flag is set
			// modified:
			//		The current value of the modified flag
			// params:
			//		More info about the change such as parameter info etc
			// tags:
			//		callback
		},
		
		getConfigName: function() {
			// summary:
			//		Returns the name of the configuration this component belongs to
			var top = this.getTop();
			if(top)
				return top.getConfigName();
			else
				return null;
		},
		
		getAssemblyLine : function() {
			// summary:
			//		Returns the assemblyline object this component belongs to
			return this.getParentConfigType("tdi.config.assemblyline");
		},
		
		getParentConfigType : function(clazz) {
			var c = this;
			while(c) {
				if(c.declaredClass == clazz)
					return c;
				else
					c = c.getParent();
			}
			return c;
		},
		
		getTop : function() {
			var top = this;
			while(top != null && top.parentConfig != null) {
				top = top.parentConfig;
			}
			return top;
		},
		
		getParent : function() {
			return this.parentConfig;
		},
		
		
		setModified : function(modified, param) {
			this._modified = (modified != null ? modified : true);
			this.onModify(this.isModified(), param);
			if(this.parentConfig)
				this.parentConfig.setModified(this.isModified(), param);
			else if(this.getTop() != null && this.getTop() !== this)
				this.getTop().setModified(this.isModified(), param);
		},
		
		isModified : function() {
			return this._modified;
		},
		
		
		getKey : function(key) {
			if(this.config && this.config.complexConfig)
				return "complexConfig." + key;
			
			else if(this.config && this.config.simpleConfig)
				return "simpleConfig." + key;
			
			else if(this.config && this.config.compositeConfig)
				return "compositeConfig." + key;
			
			else
				return key;
		},
		
		getArray : function(path, obj) {
			var arr = [];
			obj = obj || this.config;
			var list = this.getObject(path, obj);
			if(dojo.isArray(list)) {
				arr = list;
			} else if(list != undefined) {
				arr.push(list);
			} else {
				// only create "parameter" if we're not at @type level
				if(path == "parameter" && !obj["@type"])
					this.setObject(path, arr, obj, false);
				else if(path != "parameter")
					this.setObject(path, arr, obj, false);
			}
			return arr;
		},
		
		getObject : function(path, obj) {
			var src = obj || this.config;
			var value = dojo.getObject(path, false, src);
			if(!value && this.getInheritedObj()) {
				return this.getInheritedObj().getObject(path, obj);
			}
			return value;
		},
		
		setObject : function(path, value, obj, setmodify) {
			var src = obj || this.config;
			var ret = dojo.setObject(path, value, src);
			if((setmodify === undefined || setmodify) && ret != undefined)
				this.setModified(true, {param:path, source:src});
			return ret;
		},
		
		getNames : function() {
			var arr = [];
			if(dojo.isArray(this.config.parameter)) {
				dojo.forEach(this.config.parameter, function(item) {
					arr.push(item.name);
				});
			}
			return arr;
		},
		
		getParamByName : function(name, ignoreInherit) {
			var arr = dojo.filter(this.getArray("parameter"), function(item) {
				return name == item.name;
			});
			if(arr.length == 1)
				return arr[0];
			
			if(arr.length == 0 && this.getInheritedObj() && !ignoreInherit) {
				return this.getInheritedObj().getParamByName(name);
			}
			return null;
		},
		
		getParamBoolean: function(param, defval) {
			var value = this.getParam(param);
			if(lang.isString(value)) {
				return value == "true";
			} else if(typeof(value) == "boolean") {
				return value;
			} else {
				return defval;
			}
		},
		
		getParam : function(param) {
			var p = this.getParamByName(param);
			if(p == undefined) {
				return null;
			} else {
				return p.value;
			}
		},
		
		setParam : function(param, value, protect) {
			var p = this.getParamByName(param, true);
			if(p == undefined) {
				p = new Object();
				p.name = param;
				this.config.parameter.push(p);
			}
			p.value = value;
			if(protect)
				p.isProtected = true;
			
			this.setModified(true, {param:p, source:this});
//			if(this.getTop() != null)
//				this.getTop().setModified();
		},
		
		getInheritedObj : function() {
			if(this._inheritsFrom) {
				return this._inheritsFrom;
			}
			
			var inh = this.getInheritFrom();
			if(inh == "[parent]") {
				if(this.getParent())
					return this.getParent().getInheritedObj();
				else
					return this.getParent();
			} else if (inh == "[no inheritance]") {
				return null;
			} else if (inh) {
				var match = inh.match(/^\/(Connectors|Parsers)\/(.*)/);
				if(match && match.length == 3) {
					if(match[1] == "Connectors")
						this._inheritsFrom = this.getTop().getConnector(match[2]);
					else
						this._inheritsFrom = this.getTop().getParser(match[2]);
					return this._inheritsFrom;
				}
				
				match = inh.match("(.*):/(Connectors|Parsers)/(.*)");
				if(match && match.length == 4) {
					var ns = tdiapi.getNamespace(match[1]);
					if(ns) {
						if(match[2] == "Connectors")
							this._inheritsFrom = ns.getConnector(match[3]);
						else
							this._inheritsFrom = ns.getParser(match[3]);
					}
				}
				
				match = inh.match("http[s]?:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
				if(match && match.length == 4) {
					var ns = tdiapi.getNamespace(match[1]);
					if(ns) {
						if(match[2] == "Connectors")
							this._inheritsFrom = ns.getConnector(match[3]);
						else
							this._inheritsFrom = ns.getParser(match[3]);
					}
				}
			}
			return this._inheritsFrom;
		},
		
		getInheritFrom : function() {
			var key = this.getKey("inheritFrom");
			return dojo.getObject(key, false, this.config);
		},
		
		setInheritFrom : function(inheritFrom) {
			var key = this.getKey("inheritFrom");
			this._inheritsFrom = null;
			this.setObject(key, inheritFrom, this.config);
		},
		
		getUserComment : function() {
			return this.getObject(this.getKey("userComment"));
		},
		
		setUserComment : function(comment) {
			this.setObject(this.getKey("userComment"), comment);
		},
		
		getEnabled : function() {
			var obj = this.getObject(this.getKey("enabled"));
			if(obj === undefined)
				return true;
			else
				return obj ? true : false;
		},
		
		isEnabled : function() {
			return this.getEnabled();
		},
		
		setEnabled : function(enabled) {
			if(enabled != this.isEnabled())
				return this.setObject(this.getKey("enabled"), enabled);
			else
				return this.isEnabled();
		},
		
		getName : function() {
			return this.config.name;
		},
		
		setName : function(name) {
			this.config.name = name;
		},
		
		getScript : function() {
			return this.getObject(this.getKey("script"));
		},
		
		setScript : function(str) {
			this.setObject(this.getKey("script"), str);
		}
		
	});
	
});
