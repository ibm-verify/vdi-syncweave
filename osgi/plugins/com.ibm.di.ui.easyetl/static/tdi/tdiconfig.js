/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     1.13, 12/19/11
 * @owner       
 * @history
 */
if (!dojo._hasResource["tdi.tdiconfig"]) {
	dojo._hasResource["tdi.tdiconfig"] = true;

	dojo.provide("tdi.tdiconfig");

	dojo.declare("tdi.basecfg", null, {

		// The JSON config data
		config: null,
		
		// Parent config
		parentConfig: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		isAssemblyLine : function() {
			// summary:
			//		returns true if this is an assemblyline config
			return this.getType() == "assemblyLine";
		},
		
		isContainer : function() {
			// summary:
			//		returns true if this is an composite config with children
			return this.getType() == "composite";
		},
		
		isConnector : function() {
			// summary:
			//		returns true if this is an connector config
			return this.getSubType() == "connector";
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
		
		onModify : function(modified) {
			// summary:
			//		Called when the modified flag is set
			// modified:
			//		The current value of the modified flag
			// tags:
			//		callback
		},
		
		getAssemblyLine : function() {
			return this.getParentConfigType("tdi.assemblyline");
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
		
		
		setModified : function(modified) {
			this._modified = (modified != null ? modified : true);
			this.onModify(this.isModified());
			if(this.getTop() != null && this.getTop() !== this)
				this.getTop().setModified(modified);
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
				this.setModified();
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
		
		getParam : function(param) {
			var p = this.getParamByName(param);
			if(p == undefined) {
				return null;
			} else {
				return p.value;
			}
		},
		
		setParam : function(param, value) {
			var p = this.getParamByName(param, true);
			if(p == undefined) {
				p = new Object();
				p.name = param;
				this.config.parameter.push(p);
			}
			p.value = value;
			if(this.getTop() != null)
				this.getTop().setModified();
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
				
				match = inh.match("http:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
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
			dojo.setObject(key, inheritFrom, this.config);
		},
		
		getUserComment : function() {
			return this.getObject(this.getKey("userComment"));
		},
		
		setUserComment : function(comment) {
			this.setObject(this.getKey("userComment"), comment);
		},
		
		getEnabled : function() {
			return this.getObject(this.getKey("enabled"));
		},
		
		isEnabled : function() {
			return this.getEnabled();
		},
		
		setEnabled : function(enabled) {
			return this.setObject(this.getKey("enabled"), enabled);
		},
		
		getName : function() {
			return this.config.name;
		},
		
		getScript : function() {
			return this.getObject(this.getKey("script"));
		},
		
		setScript : function(str) {
			this.setObject(this.getKey("script"), str);
		},
		
		createTDIComponent : function(config, parent) {
			
			if(config["@type"] == "composite") {
				
				return new tdi.tdicontainer({config:config, parentConfig:parent})
				
			} else if(config["@type"] == "complex") {
				
				if(config.complexConfig["@type"] == "connector") {
					return new tdi.connector({config:config, parentConfig:parent});
					
				} else if(config.complexConfig["@type"] == "function") {
						return new tdi.connector({config:config, parentConfig:parent});
						
				} else if(config.complexConfig["@type"] == "parser") {
					return new tdi.parserconfig({config:config, parentConfig:parent});
					
				}
				
			} else if(config["@type"] == "simple") {
				if(config.simpleConfig["@type"] == "script") {
					return new tdi.scriptconfig({config:config, parentConfig:parent});
					
				} else if(config.simpleConfig["@type"] == "map") {
					return new tdi.almapconfig({config:config, parentConfig:parent});
				}
				
			}
			return new tdi.basecfg({config:config, parentConfig:parent});
		}

	});
	
	dojo.declare("tdi.tdiconfig", [tdi.basecfg], {
		
		// The folders in the config (ref to config.solution....)
		folders: null,
		
		// Assemblyline objects (ref to config.solution....)
		assemblylines: null,
		
		// Schedule objects (ref to config.solution....)
		schedules: null,
		
		// Prop store objects (ref to config.solution....)
		propertyStores: null,
		
		// solution interface (ref to config.solution....)
		solution: null,
		
		// connector library
		connectors: null,
		
		// parser library
		parsers: null,
		
		// script library
		scripts: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this._initConfig();
			if(this.config.container) {
				dojo.forEach(this.config.container, dojo.hitch(this, function(folder) {
					this.folders[folder.name] = folder;
					if(!folder.config)
						folder.config = [];
				}));
			} else {
				dojo.forEach(this.config.solution.container, dojo.hitch(this, function(folder) {
					this.folders[folder.name] = folder;
					if(!folder.config)
						folder.config = [];
				}));
			}
		},
		
		_initConfig : function() {
			if(this.config == null) {
				this.config = {};
			}
			
			this.folders = new Object();
			this.schedules = new Object();
			this.propertyStores = new Object();
			this.assemblylines = new Object();
			this.connectors = new Object();
			this.parsers = new Object();
			this.scripts = new Object();
			
			// Checkouts use solution.container whereas running configs
			// exclude the "solution" property.
			if(this.config.container != null) {
				return;
			}
			if(this.config.solution == null) {
				this.config.solution = new Object();
			}
			if(this.config.solution.container == null) {
				this.config.solution.container = new Array();
				this.config.solution.container.push({
					"@type":"container",
					name:"AssemblyLines",
					config: []
				});
				this.config.solution.container.push({
					"@type":"container",
					name:"Connectors",
					config: []
				});
				this.config.solution.container.push({
					"@type":"container",
					name:"Parsers",
					config: []
				});
			}
			if(this.config.solution.context == null) {
				this.config.solution.context = {};
			}
		},
		
		getConfigName : function() {
			return this.getSolutionInterface().getSolutionName();
			return obj;
		},
		
		setConfigName : function(name) {
			this.getSolutionInterface().setSolutionName(name);
		},
		
		lookup : function(name) {
			var match = name.match("(.*):/(.*)/(.*)");
			var ns = null;
			var folder = null;
			var obj = null;
			if(match && match.length == 4) {
				ns = match[1];
				folder = match[2];
				obj = match[3];
			} else {
				match = name.match("/(.*)/(.*)");
				if(match && match.length == 3) {
					folder = match[1];
					obj = match[2];
				}
			}
			
			var cfg = ns == null ? this : tdiapi.getNamespace(ns);
			if(cfg == null)
				return null;
			
			if(folder == "Connectors")
				return cfg.getConnector(obj);
			else if(folder == "Parsers")
				return cfg.getParser(obj);
			else if(folder == "Scripts")
				return cfg.getScript(obj);
			else if(folder == "AssemblyLines")
				return cfg.getAssemblyLine(obj);
			
			return null;
		},
		
		isEasyETL : function() {
			if(this.getAssemblyLineNames().length == 1) {
				var al = this.getAssemblyLine(this.getConfigName());
				if(al != null) {
					var input = al.getConnector("Input");
					var output = al.getConnector("Output");
					if(input != null && output != null)
						return true;
				}
			}
			return false;
		},
		
		getPropertyStoreNames : function() {
			// summary:
			//		Returns the property store names in the configuration
			var arr = new Array();
			if(this.propertyStores == null) {
				this.propertyStores = new Object();
				var stores = this.getArray("config", this.folders.Properties);
				for(var i = 0; i < stores.length; i++) {
					var store = stores[i];
					this.propertyStores[store.name] = new tdi.propstore({config:store, parentConfig:this});
				}
			}
			for(var name in this.propertyStores) {
				arr.push(name);
			}
			return arr;
		},
		
		getPropertyStore : function(store) {
			// summary:
			//		Returns the configuration for a specific property store
			// returns:
			//		tdi.propstore object
			return this.propertyStores[store];
		},
		
		getObjectFromFolder : function(folder, name) {
			var contents = this.getArray("config", folder);
			var arr = dojo.filter(contents, function(obj) {
				return (name == obj.name);
			});
			
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},

		getFolderNames : function(folder) {
			var contents = this.getArray("config", folder);
			var arr = [];
			dojo.forEach(contents, function(obj) {
				arr.push(obj.name);
			});
			return arr.sort();
		},
		
		getConnectorNames : function() {
			return this.getFolderNames(this.folders.Connectors);
		},
		
		getConnector : function(name) {
			// summary:
			//		Returns the tdi.connector object for the specified
			//		library connector
			if(this.connectors[name] == null) {
				var config = this.getObjectFromFolder(this.folders.Connectors, name);
				if(config != null) {
					this.connectors[name] = new tdi.connector({config:config, parentConfig: this});
				}
			}
			return this.connectors[name];
		},
		
		addConnector : function(name, config) {
			if(this.getConnector(name) == null) {
				var arr = this.getArray("config", this.folders.Connectors);
				if(arr) {
					config.name = name;
					arr.push(config);
				}
			}
		},
		
		getParserNames : function() {
			// summary:
			//		Returns the names of parsers in this config
			return this.getFolderNames(this.folders.Parsers);
		},
		
		getParser : function(name) {
			// summary:
			//		Returns the tdi.parser object for the specified
			//		library parser
			if(this.parsers[name] == null) {
				var config = this.getObjectFromFolder(this.folders.Parsers, name);
				if(config != null) {
					this.parsers[name] = new tdi.parserconfig({config:config, parentConfig: this});
				}
			}
			return this.parsers[name];
		},
		
		getScriptNames : function() {
			// summary:
			//		Returns the names of scripts in this config
			return this.getFolderNames(this.folders.Scripts);
		},
		
		getScript : function(name) {
			// summary:
			//		Returns the tdi.script object for the specified
			//		library script
			if(this.scripts[name] == null) {
				var config = this.getObjectFromFolder(this.folders.Scripts, name);
				if(config != null) {
					this.scripts[name] = new tdi.basecfg({config:config, parentConfig: this});
				}
			}
			return this.scripts[name];
		},
		
		getAssemblyLineNames : function() {
			return this.getFolderNames(this.folders.AssemblyLines);
		},
		
		getAssemblyLines : function() {
			var arr = new Array();
			var als = this.getAssemblyLineNames();
			for(i = 0; i < als.length; i++) {
				arr.push(this.getAssemblyLine(als[i]));
			}
			return arr;
		},
		
		deleteAssemblyLine : function(assemblyline) {
			var alcfg = this.getArray("config", this.folders.AssemblyLines);
			var arr = dojo.filter(alcfg, function(al) {
				return (assemblyline != al.name);
			});
			this.folders.AssemblyLines.config = arr;
			this.assemblylines[assemblyline] = null;
			this.setModified(true);
		},
		
		deleteAssemblyLineSchedules : function(assemblyline) {
			// delete all related schedules
			var sched;
			while((sched = this.getScheduleForAssemblyLine(assemblyline))) {
				this.deleteSchedule(sched);
			}
		},

		renameAssemblyLine : function(assemblyline, newname) {
			var al = this.getAssemblyLine(assemblyline);
			var newal = this.getAssemblyLine(newname);
			if(newal == null && al != null) {
				al.config.name = newname;
				this.assemblylines[assemblyline] = null;
				this.assemblylines[newname] = al;
				this.setModified(true);
				return true;
			}
			return false;
		},
		
		getAssemblyLine : function(assemblyline) {
			// summary:
			//		Returns the configuration for a specific assemblyline
			// returns:
			//		tdi.assemblyline object
			if(this.assemblylines[assemblyline] == null)
				this.assemblylines[assemblyline] = this._getAssemblyLine(assemblyline);
			return this.assemblylines[assemblyline];
		},
		
		_getAssemblyLine : function(assemblyline) {
			var alcfg = this.getArray("config", this.folders.AssemblyLines);
			var arr = dojo.filter(alcfg, function(al) {
				return (assemblyline == al.name);
			});
			
			if(arr.length == 1)
				return new tdi.assemblyline({config:arr[0], parentConfig:this});
			else
				return null;
		},
		
		createAssemblyLine : function(name) {
			var current = this.getAssemblyLine(name);
			if(current != null)
				return null;
			
			var newal = {
				"@type":"assemblyLine",
				container: [
				    {
				    	name:"EntryFeedContainer",
				    	component: []
				    },
				    {
				    	name:"DataFlowContainer",
				    	component: []
				    }
				],
				settings: {
					parameter: []
				},
				name:name
			};
			this.folders.AssemblyLines.config.push(newal);
			return this.getAssemblyLine(name);
		},
		
		deleteSchedule : function(schedule) {
			// summary:
			//		Deletes the named schedule
			if(this.folders.Schedules == undefined)
				return null;
			
			var schedlist = this.getArray("config", this.folders.Schedules);
			for(var i = 0; i < schedlist.length; i++) {
				if(schedlist[i].name == schedule) {
					schedlist.splice(i, 1);
					this.schedules[schedule] = null;
					this.setModified(true);
				}
			}
		},
		
		getScheduleNames : function() {
			// summary:
			//		Returns the names of this configuration's schedules
			var names = new Array();
			if(this.folders.Schedules) {
				var schedlist = this.getArray("config", this.folders.Schedules);
				for(var i = 0; i < schedlist.length; i++) {
					names.push(schedlist[i].name);
				}
			}
			return names;
		},
		
		createSchedule : function(schedule) {
			// summary:
			//		Creates a new Schedule object
			// returns:
			//		Null if schedule already exists, tdi.schedule object otherwise
			if(this.getSchedule(schedule) != null)
				return this.getSchedule(schedule);
			
			var s = new Object();
			s["@type"] = "scheduleAl";
			s.cancelScheduleOnAlFailure = false;
			s.execTimePattern = "* * * * 0 0";
			s.initParams = new Object();
			s.name = schedule;
			s.skipExecIfAlRunning = true;
			s.assemblyLine = schedule;
			s.enabled = true;
			
			if(this.folders.Schedules.config == undefined) {
				this.folders.Schedules.config = new Array();
			}
			this.folders.Schedules.config.push(s);
			this.setModified(true);
			return this.getSchedule(schedule);
		},
		
		getSchedule : function(schedule) {
			// summary:
			//		Returns the configuration for a specific schedule
			// returns:
			//		tdi.schedule object
			if(this.schedules[schedule] == null)
				this.schedules[schedule] = this._getSchedule(schedule);
			return this.schedules[schedule];
		},
		
		_getSchedule : function(schedule) {
			if(this.folders.Schedules == undefined)
				return null;
			
			var schedlist = this.getArray("config", this.folders.Schedules);
			var arr = dojo.filter(schedlist, function(s) {
				return (schedule == s.name);
			});
			
			if(arr.length == 1)
				return new tdi.schedule({config:arr[0], parentConfig:this});
			else
				return null;
		},
		
		getScheduleForAssemblyLine : function(assemblyline) {
			if(this.folders.Schedules == undefined)
				return null;
			
			var arr = this.getArray("config", this.folders.Schedules);
			arr = dojo.filter(arr, function(item) {
				if(item == null) {
					console.log("NULL item in Schedules folder");
					return;
				}
				return item.assemblyLine == assemblyline;
			});
			if(arr.length > 0)
				return arr[0].name;
			else
				return null;
		},
		
		getSolutionInterface : function() {
			if(this.solution == null) {
				var obj = this.getObject("solution.context.interface");
				if(obj == null)
					obj = this.getObject("context.interface");
				if(obj == null) {
					obj = {
						enabled:true,
						pollInterval:-1
					}
					if(this.config.solution)
						this.config.solution.context["interface"] = obj;
					else
						this.config.context["interface"] = obj;
				}
				this.solution = new tdi.tdisolution({config:obj, parentConfig:this});
			}
			return this.solution;
		},
		
		getTombstoneSettings : function() {
			if(this.tombstones == null) {
				var obj = this.getObject("solution.context.tombstone");
				if(obj == null)
					obj = this.getObject("context.tombstone");
				if(obj != null)
					this.tombstones = new tdi.basecfg({config:obj});	
			}
			return this.tombstones;
		}
	});
	
	dojo.provide("tdi.tdisolution");
	dojo.declare("tdi.tdisolution", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getExposedAssemblyLines : function() {
			return this.getArray("al");
		},
		
		setExposedAssemblyLines : function(/*array*/ arr) {
			this.setObject("al", arr);
		},
		
		getExposedAssemblyLineNames : function() {
			// summary:
			//		Returns an array with the exposed al names
			var arr = new Array();
			dojo.forEach(this.getExposedAssemblyLines(), function(obj) {
				arr.push(obj.name);
			});
			return arr;
		},
		
		getSolutionName : function() {
			return this.getObject("solutionName");
		},
		
		setSolutionName : function(name) {
			this.setObject("solutionName", name);
		},
		
		getExposedProperties : function() {
			return this.getArray("property");
		},
		
		getExposedProperty : function(store, name) {
			var arr = dojo.filter(this.getExposedProperties(), function(prop) {
				if(store != null)
					return prop.name == name && prop.storeName == store;
				else
					return prop.name == name;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		}
	});
	
	dojo.provide("tdi.tdibranch");
	dojo.declare("tdi.tdibranch", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getConditions : function() {
			if(!this._conditions) {
				var obj = this.getObject("condition");
				if(!obj)
					obj = this.getObject("whileCondition");
				if(obj) {
					this._conditions = new tdi.tdibranchconditions({config:obj, parentConfig:this});
				}
			}
			return this._conditions;
		},
		
		getBranchType : function() {
			var type = this.getObject("type");
			if(!type) {
				if(this.getObject("collectionCondition"))
					return "AttributeLoop";
				else if(this.getObject("connectorCondition"))
					return "ConnectorLoop";
				else if(this.getObject("whileCondition"))
					return "WhileLoop";
			}
			return type;
		},
		
		getConnectorConfig : function() {
			if(!this._connectorConfig) {
				this._connectorConfig = new tdi.connector({config:this.getObject("connectorCondition.connector"), parentConfig:this});
			}
			return this._connectorConfig;
		},
		
		getWhileConfig : function() {
			return this.getConditions();
		},
		
		getAttributeConfig : function() {
			if(!this._attributeConfig) {
				this._attributeConfig = new tdi.attributeloop({config:this.getObject("collectionCondition"), parentConfig:this});
			}
			return this._attributeConfig;
		}
	});
		
	dojo.provide("tdi.attributeloop");
	dojo.declare("tdi.attributeloop", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getWorkAttributeName : function() {
			return this.getObject("collectionAttribute");
		},
		
		getLoopAttributeName : function() {
			return this.getObject("assignAttribute");
		}
	});
		
	dojo.provide("tdi.tdibranchconditions");
	dojo.declare("tdi.tdibranchconditions", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getConditionCount : function() {
			var arr = this.getArray("item");
			return arr ? arr.length : 0;
		},
		
		getCondition : function(index) {
			var arr = this.getArray("item");
			if(arr && arr.length > index)
				return arr[index];
			else
				return null;
		},
		
		getLabel : function() {
			var cond = this.getCondition(0);
			if(cond) {
				return cond.rightHand;
			} else {
				return this.getScript();
			}
		}
	});
		
	dojo.provide("tdi.tdicontainer");
	dojo.declare("tdi.tdicontainer", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.components = new Object();
		},
		
		isBranch : function() {
			return this.getSubType() == "branch";
		},
		
		isSwitch: function() {
			return this.getBranchType() == "Switch";
		},
		
		isCase: function() {
			return this.getBranchType() == "Case";
		},
		
		isIf: function() {
			return this.getBranchType() == "If";
		},
		
		isElse: function() {
			return this.getBranchType() == "Else";
		},
		
		isElseIf: function() {
			return this.getBranchType() == "ElseIf";
		},
		
		getBranchType : function() {
			// summary:
			//		Returns the branch type of the configuration (Switch, Case ...)
			return this.getObject(this.getKey("type"));
		},
		
		getComponent : function(name) {
			if(!this.components[name]) {
				dojo.forEach(this.getArray("component"), dojo.hitch(this, function(comp) {
					if(comp.name && comp.name == name) {
						this.components[name] = this.createTDIComponent(comp, this);
					}
				}));
			}
			return this.components[name];
		},
		
		getComponentNames : function() {
			var names = new Array();
			dojo.forEach(this.getArray("component"), dojo.hitch(this, function(comp) {
				if(comp.name) {
					names.push(comp.name);
				}
			}));
			return names;
		},
		
		getBranchConfig : function() {
			return this.getCompositeConfig();
		},
		
		getCompositeConfig : function() {
			if(!this.compositeConfig) {
				var type = this.getType();
				if(type == "composite") {
					this.compositeConfig = new tdi.tdibranch({config:this.getObject("compositeConfig"), parentConfig:this});
				}
			}
			return this.compositeConfig;
		},
		
		getContainerType : function() {
			var cfg = this.getConfig();
			if(cfg && cfg["@type"])
				return cfg["@type"];
			else
				return this.getType();
		}
	});
	
	dojo.provide("tdi.assemblyline");
	dojo.declare("tdi.assemblyline", [tdi.basecfg], {
		
		// connector handles
		connectors: null,
		settings: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.connectors = new Object();
		},
		
		isEasyETL : function() {
			var input = this.getConnector("Input");
			var output = this.getConnector("Output");
			if(input != null && output != null)
				return true;
		},
		
		isConfigReport : function() {
			if(this.getEntryFeedComponent().getComponentNames().length == 0) {
				var arr = this.getDataFlowComponent().getComponentNames();
				if(arr && arr.length == 1 && arr[0] == "RunReport")
					return true;
			} 
			return false;
		},
		
		getInitParams : function() {
			if(this.initParams == null) {
				if(!this.config.initParams)
					this.config.initParams = {};
				if(!this.config.initParams.schema) {
					this.config.initParams.schema = {
						"@type":"schema",
						"name": "AssemblyLineInitParams",
						item: []
					};
				}
				this.initParams = new tdi.schemaconfig({config:this.config.initParams.schema, parentConfig:this});
			}
			return this.initParams;
		},
		
		getSettings : function() {
			if(this.settings == null) {
				this.settings = new tdi.basecfg({config:this.config.settings});
			}
			return this.settings;
		},
		
		setAutomapAttributes : function(enabled) {
			this.getSettings().setParam("automapattributes", enabled);
		},
		
		getEntryFeed : function() {
			return this.getContainer("EntryFeedContainer");
		},
		
		getDataFlow : function() {
			return this.getContainer("DataFlowContainer");
		},
		
		getContainer : function(container) {
			var arr = dojo.filter(this.config.container, function(item) {
				return item.name == container;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		addFeedComponent: function(conn) {
			this.getEntryFeed().component.push(conn.config);
			this.setModified(true);
		},
		
		addDataFlowComponent : function(conn) {
			this.getDataFlow().component.push(conn.config);
			this.setModified(true);
		},
		
		
		getDataFlowComponent : function() {
			// summary:
			//		Returns the container object for the data flow component
			if(!this._dataFlowComponent) {
				this._dataFlowComponent = new tdi.tdicontainer({config:this.getDataFlow(), parentConfig:this});
			}
			return this._dataFlowComponent;
		},
		
		getEntryFeedComponent : function() {
			// summary:
			//		Returns the container object for the entry feed component
			if(!this._entryFeedComponent) {
				this._entryFeedComponent = new tdi.tdicontainer({config:this.getEntryFeed(), parentConfig:this});
			}
			return this._entryFeedComponent;
		},
		
		getComponentByName : function(name) {
			// summary:
			//		Returns the config for the named component
			var comp = this.getDataFlowComponent().getComponent(name);
			if(!comp)
				comp = this.getEntryFeedComponent().getComponent(name);
			return comp;
		},
		
		getConnectorNames : function() {
			var conn = dojo.filter(this.getArray("component", this.getEntryFeed()), function(item) {
				if(item.complexConfig != null)
					return item.complexConfig["@type"] == "connector";
				else if(item["@type"] !== undefined)
					return item["@type"] == "connector";
				else
					return false;
			});
			return conn.concat(dojo.filter(this.getArray("component", this.getDataFlow()), function(item) {
				if(item.complexConfignull)
					return item.complexConfig["@type"] == "connector";
				else if(item["@type"] == "composite" && item.component)
					return true;
				else if(item["@type"])
					return item["@type"] == "connector";
				else
					return false;
			}));
		},
		
		getConnector : function(name) {
			if(this.connectors[name] == null) {
				this.connectors[name] = this._getConnector(name);
			}
			return this.connectors[name];
		},
		
		_getConnector : function(name) {
			var ef = this.getEntryFeed();
			var arr = this.getArray("component", ef);
			var conn = dojo.filter(arr, function(item) {
				return item.name == name;
			});
			if(conn.length == 1)
				return new tdi.connector({config:conn[0], parentConfig:this});
			
			var df = this.getDataFlow();
			var arr = this.getArray("component", df);
			var conn = dojo.filter(arr, function(item) {
				return item.name == name;
			});
			if(conn.length == 1)
				return new tdi.connector({config:conn[0], parentConfig:this});
			else
				return null;
		},
		
		getActiveHookNames : function() {
			
		},
		
		getHook : function(name) {
			
		},
		
		getWorkAttributes : function(untilComponent) {
			// summary:
			//		Returns the work attributes available to untilComponent
			var arr = new Array();
			dojo.every(this.getConnectorNames(), dojo.hitch(this, function(conn) {
				if(conn.name == untilComponent)
					return false;
				var c = this.getConnector(conn.name);
				if(c.getMode() == "Iterator" || c.getMode() == "Lookup" || c.getMode() == "Server") {
					try {
						var cfg = c.getAttributeMap(true).getNames();
						arr = arr.concat(cfg);
					} catch(ignore) {}
				}
				
				return true;
			}));
			return arr;
		},
		
		createFeedConnector : function(name, mode) {
			var conn = this.createConnector(name, mode);
			conn.parentConfig = this;
			this.addFeedComponent(conn);
			return conn;
		},
		
		createDataFlowConnector : function(name, mode) {
			var conn = this.createConnector(name, mode);
			conn.parentConfig = this;
			this.addDataFlowComponent(conn);
		},
		
		createConnector : function(name, mode) {
			return new tdi.connector({
				config: {
					"@type":"complex",
					name: name,
					initialize: "onStartup",
					sandboxPlayback: false,
					sandboxRecord: false,
					simulateState: "Enabled",
					state: "Enabled",
					complexConfig: {
						"@type":"connector",
						inheritFrom: "[parent]",
						mode:mode,
						rawConfig: {
							inheritFrom:"[parent]",
							parameter: []
						},
						poolDef: {
							enabled: false,
							initializeAttempts: 1,
							initializeSleepInterval: 0,
							maxSize: 0,
							minSize: 0,
							purgeInterval: 0							
						},
						poolInst: {
							enabled: false,
							onExhausted: "wait"							
						},
						hooks: {},
						deltaConfig: {
							allowDuplicateKeys: false,
							changeDetectionMode: "detectAll",
							commit: "onAlCycle",
							enabled: false,
							fasterAlgorithm: false,
							readDeleted: false,
							removeDeleted: false,
							returnUnchanged: false,
							rowLocking: "serializable"							
						},
						map: [
						      {
						    	  "@type":"map",
						    	  name: "Input"
						      },
						      {
						    	  "@type":"map",
						    	  name: "Output"
						      }
						],
						parser: {
							"@type":"parser",
							inheritFrom:"[parent]",
							rawConfig: {
								inheritFrom:"[parent]",
								parameter: []
							},
							schema: [],
							rawConfig: {
								inheritFrom: "[parent]",
								parameter: []
							}
						},
						reconnect: {
							autoSkipForward: false,
							numberOfRetries: 1,
							onConnectionError: false,
							onInitializationError: false,
							retryDelay: 10							
						},
						schema: []
					}
				}
			});			
		},
		
		createScriptComponent : function(name, script) {
			var conn = new tdi.connector({
				config: {
					"@type":"simple",
					name: name,
					simulateState: "Enabled",
					state: "Enabled",
					simpleConfig: {
						"@type":"script",
						autoInclude:false,
						script:script
					}
				}
			});			
			this.addDataFlowComponent(conn);
			return conn;
		}
	});

	dojo.provide("tdi.connector");
	dojo.declare("tdi.connector", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this._maps = new Object();
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
			
			match = ref.match("http:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
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
					return inh.getParserType();
				}
			}
			
			match = ref.match("http:.*/rest/internal/(.*)/(Connectors|Parsers)/(.*)");
			if(match && match.length == 4 && match[1] != "system") {
				var ns = tdiapi.getNamespace(match[1]);
				if(ns) {
					var inh = ns.getParser(match[3]);
					ref = inh.getParserType();
				}
			}
			
			return ref;
		},
		
		getSimpleConnectorType : function() {
			var con = this.getConnectorType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
			return con;
		},
		
		getAssemblyLine : function() {
			return this.parentConfig;
		},
		
		setConnectorType : function(type) {
			this.setObject(this.getKey("inheritFrom"), type);
		},
		
		setState : function(state) {
			this.setObject("state", state);
		},
		
		getState : function() {
			return this.getObject("state");
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
			var key = "Attributemap_" + input;
			if(this._maps[key] == null) {
				var map = this.getObject(this.getKey("map"));
				this._maps[key] = new tdi.attmapconfig({config:this.getMap(map, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getSchema : function(input) {
			var key = "Schema_" + input;
			if(this._maps[key] == null) {
				var schema = this.getObject(this.getKey("schema"));
				this._maps[key] = new tdi.schemaconfig({config:this.getMap(schema, input ? "Input" : "Output"), parentConfig:this});
			}
			return this._maps[key];
		},
		
		getConnectionConfig : function() {
			var key = "ConnectionConfig";
			if(this._maps[key] == null) {
				this._maps[key] = new tdi.connectionconfig({config:this.getObject(this.getKey("rawConfig")), parentConfig:this});
			}
			return this._maps[key];
		},
			
		getParserConfig : function() {
			var key = "ParserConfig";
			if(this._maps[key] == null) {
				this._maps[key] = new tdi.parserconfig({config:this.getObject(this.getKey("parser")), parentConfig:this});
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
						this._hooks[hook.name] = new tdi.basecfg({config:hook, parentConfig:this});
					}));
				}
			}
			return this._hooks;
		},
		
		getHookNames : function() {
			this._getHooks();
			var arr = new Array();
			for(var h in this._hooks) {
				arr.push(h);
			}
			return arr;
		},
		
		getHook : function(name) {
			this._getHooks();
			return this._hooks[name];
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
				this._linkCriteria = new tdi.linkcriteria({config:this.getObject(this.getKey("linkCriteria"))});
			}
			return this._linkCriteria;
		}
		
	});

	dojo.provide("tdi.connectionconfig");
	dojo.declare("tdi.connectionconfig", [tdi.basecfg], {
		getInheritedObj : function() {
			var ref = this.getInheritFrom();
			if(ref == "[parent]") {
				var conn = null;
				if(this.getParent() != null) {
					conn = this.getParent().getInheritedObj();
				}
				if(conn) {
					return conn.getConnectionConfig();
				}
			}
			return this.inherited(arguments);
		}
	});
	
	dojo.provide("tdi.linkcriteria");
	dojo.declare("tdi.linkcriteria", [tdi.basecfg], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.items = this.getArray("item");
		},
		
		size : function() {
			return this.items.length;
		},
		
		hasLinkAttribute : function(attribute) {
			return dojo.some(this.items, function(item) {
				return item.attribute == attribute;
			});
		},
		
		getLinkAttributes : function() {
			// summary:
			//		Returns the attribute names for which there are
			//		link criteria set.
			// returns:
			//		An array of attribute names or null if link is not set
			var arr = [];
			dojo.forEach(this.items, function(item) {
				if(dojo.indexOf(arr, item.attribute) == -1) {
					arr.push(item.attribute);
				}
			});
			return arr;
		},
		
		getCriteriaFor : function(attribute) {
			// summary:
			//		Returns the LinkCriteria definition for a specific attribute
			var arr = dojo.filter(this.items, function(item) {
				return item.attribute == attribute;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		setCriteriaFor : function(attribute, oper, value) {
			// summary:
			//		Sets the LinkCriteria definition for a specific attribute
			//		This function overwrites existing link criteria.
			var crit = this.getCriteriaFor(attribute);
			if(crit == null) {
				this.items.push({
					attribute:attribute,
					oper:oper,
					value:value,
					key:new Date().getTime()
				});
			} else {
				crit.oper = oper;
				crit.value = value;
			}
			this.setModified(true);
		}
		
	});
	
	dojo.provide("tdi.parserconfig");
	dojo.declare("tdi.parserconfig", [tdi.basecfg], {
		
		rawconfig: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.rawconfig = null;
		},
		
		getInheritedObj : function() {
			var ref = this.getInheritFrom();
			if(ref == "[parent]") {
				var conn = null;
				if(this.getParent() != null) {
					conn = this.getParent().getInheritedObj();
				}
				if(conn) {
					return conn.getParserConfig();
				}
			}
			return this.inherited(arguments);
		},
		
		getParserType : function() {
			var ref = this.getObject("rawConfig.inheritFrom");
			if(ref == undefined || ref == "[parent]")
				ref = this.getObject("inheritFrom");
			
			return ref;
		},
		
		setParserType : function(type) {
			this.setObject("inheritFrom", type);
			this.setObject("rawConfig.inheritFrom", type);
			this._inheritsFrom = null;
			this.rawconfig = null;
		},
		
		getParamByName : function(name, ignoreInherit) {
			return this.getConfig().getParamByName(name, ignoreInherit);
		},
		
		setParam : function(param, value) {
			this.getConfig().setParam(param, value);
		},
		
		getConfig : function() {
			if(this.rawconfig == null)
				this.rawconfig = new tdi.basecfg({config:this.getObject("rawConfig"), parentConfig:this});
			return this.rawconfig;
		}

	});

	
	dojo.provide("tdi.almapconfig");
	dojo.declare("tdi.almapconfig", [tdi.basecfg], {
		
		_map: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getAttributeMap : function() {
			if(!this._map) {
				this._map = new tdi.attmapconfig({config:this.getObject("simpleConfig"), parentConfig:this});
			}
			return this._map;
		}
	
	});
		
	dojo.provide("tdi.attmapconfig");
	dojo.declare("tdi.attmapconfig", [tdi.basecfg], {
		
		_maps: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this._maps = new Object();
		},
		
		getInheritedMap : function() {
			var inh = this.getInheritedObj();
			if(inh) {
				if(inh.declaredClass == "tdi.connector") {
					return inh.getAttributeMap(this.getName());
				} else if (inh.declaredClass == "tdi.attmapconfig") {
					return inh.getAttributeMap();
				}
			}
			return null;
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			
			var map = this.getInheritedMap();
			if(map) {
				dojo.forEach(map.getNames(), function(name) {
					if(dojo.indexOf(arr, name) == -1) {
						arr.push(name);
					}
				});
			}
			return arr.sort();
		},
		
		isMapped : function(name) {
			return dojo.some(this.getArray("item"), function(item) {
				return item.name == name;
			});
		},
		
		removeAllItems : function() {
			this.setObject("item", []);
			this._maps = new Object();
		},
		
		removeItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name != name;
			});
			this.setObject("item", arr);
		},
		
		newItem : function(item) {
			var arr = this.getArray("item");
			if(item.enabled == undefined)
				item.enabled = "true";
			if(item.add == undefined)
				item.add = "true";
			if(item.modify == undefined)
				item.modify = "true";
			if(item.type == undefined)
				item.type = "Simple";
			if(item.mapsTo == undefined)
				item.mapsTo = item.name;
			arr.push(item);
			this.setModified(true);
			var ami = new tdi.attmapitemconfig({config:item, parentConfig:this});
			this._maps[item.name] = ami;
			return ami;
		},
		
		getItem : function(name) {
			if(this._maps[name] == null) {
				this._maps[name] = this._getItem(name);
			}
			return this._maps[name];
		},
		
		_getItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name == name;
			});
			if(arr.length == 1) {
				return new tdi.attmapitemconfig({config:arr[0], parentConfig:this});
			}
			
			var map = this.getInheritedMap();
			if(map)
				return map.getItem(name);
			else
				return null;
		}

	});
	
	dojo.provide("tdi.attmapitemconfig");
	dojo.declare("tdi.attmapitemconfig", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		isSimple : function() {
			return "Simple" == this.config.type;
		},
		
		isAdvanced : function() {
			return "Advanced" == this.config.type;
		},
		
		getSimple : function() {
			return this.getMapsTo();
		},
		
		setSimple : function(simple) {
			this.setType("Simple");
			this.setMapsTo(simple);
		},
		
		getAdvanced : function() {
			return this.getMapsTo();
		},
		
		setAdvanced : function(script) {
			this.setType("Advanced");
			this.setMapsTo(script);
		},
		
		getMapsTo : function() {
			return this.config.mapsTo;
		},
		
		setMapsTo : function(str) {
			this.setObject("mapsTo", str);
		},
		
		setType : function(type) {
			this.setObject("type", type);
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			return arr.sort();
		}

	});
	
	dojo.provide("tdi.schemaconfig");
	dojo.declare("tdi.schemaconfig", [tdi.attmapconfig], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		newItem : function(item) {
			var arr = this.getArray("item");
			arr.push(item);
			this.setModified(true);
			this._maps[item.name] = new tdi.basecfg({config:item, parentConfig:this});
			return this._maps[item.name];
		},
		
		getItem : function(name) {
			if(this._maps[name] == null) {
				this._maps[name] = this._getItem(name);
			}
			return this._maps[name];
		},
		
		_getItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name == name;
			});
			if(arr.length == 1)
				return new tdi.basecfg({config:arr[0], parentConfig:this});
			else
				return null;
		},
		
		removeAllItems : function() {
			this.setObject("item", []);
			this._maps = [];
		},
		
		removeItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name != name;
			});
			this.setObject("item", arr);
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			return arr.sort();
		}

	});


	dojo.provide("tdi.scriptconfig");
	dojo.declare("tdi.scriptconfig", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getType : function() {
			return "script";
		},
		
		getScriptConfig : function() {
			if(!this.scriptConfig) {
				this.scriptConfig = new tdi.basecfg({config:this.getObject("simpleConfig"), parentConfig:this});
			}
			return this.scriptConfig;
		}
		
	});
	
	dojo.provide("tdi.propstore");
	dojo.declare("tdi.propstore", [tdi.basecfg], {
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getName : function() {
			return this.config.name;
		}

	});
	
	dojo.provide("tdi.schedule");
	dojo.declare("tdi.schedule", [tdi.basecfg], {
		
		partMap : {
			"month": 0,
			"wday": 1,
			"mday": 2,
			"hour": 3,
			"minute": 4,
			"second": 5
		},
		
		setTimePattern : function(part, value) {
			var index = this.partMap[part];
			if(index == null)
				return false;
			
			var arr = this.getExecTimePattern().split(" ");
			if(index >= arr.length || index < 0)
				return false;
			
			if (value == null || value == "")
				value = "*";
			
			// convert whitespace to comma
			value = value.replace(/\s+/g, ",");
			
			arr[index] = value;
			this.setExecTimePattern(arr.join(" "));
		},
		
		getTimePattern : function(part) {
			var index = this.partMap[part];
			if(index == null)
				return false;
			
			var arr = this.getExecTimePattern().split(" ");
			if(index >= arr.length || index < 0)
				return false;
			
			return arr[index];
		},
		
		setExecTimePattern : function(value) {
			this.setObject("execTimePattern", value);
		},
		
		getExecTimePattern : function() {
			var value = this.getObject("execTimePattern");
			if(value == null || value == "")
				return "* * * * 0 0";
			else
				return value;
		},
		
		setCancelScheduleOnAlFailure : function(cancel) {
			this.setObject("cancelScheduleOnAlFailure", cancel);
		},
		
		getCancelScheduleOnAlFailure : function() {
			return this.getObject("cancelScheduleOnAlFailure");
		},
		
		setSkipExecIfAlRunning : function(skip) {
			this.setObject("skipExecIfAlRunning", skip);
		},
		
		getSkipExecIfAlRunning : function() {
			return this.getObject("skipExecIfAlRunning");
		},
		
		getAssemblyLine : function() {
			return this.getObject("assemblyLine");
		},
		
		getInitParams : function() {
			if(this.initParams == null) {
				if(!this.config.initParams)
					this.config.initParams = {};
				this.initParams = new tdi.basecfg({config:this.config.initParams, parentConfig:this});
			}
			return this.initParams;
		},
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
		
	});

	dojo.provide("tdi.tdientry");
	
	dojo.declare("tdi.tdientry", null, {
		data: null,
		attributes: null,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			if(this.data != null && this.data.attribute != null) {
				this.buildAttributeList();
			} else {
				this.data = this.data || {};
				this.data.attribute = [];
			}
		},
		
		buildAttributeList : function() {
			this.attributes = new Object();
			dojo.forEach(this.data.attribute, dojo.hitch(this, function(attr) {
				this.attributes[attr.name] = attr;
			}));
		},
		
		getNames : function() {
			var arr = new Array();
			dojo.forEach(this.data.attribute, function(a) {
				arr.push(a.name);
			});
			return arr;
		},
		
		getAttribute : function(name) {
			return this.attributes[name];
		},
		
		removeAttribute : function(name) {
			var attr = this.getAttribute(name);
			if(attr != null) {
				this.attributes[name] = null;
			}
			this.data.attribute = dojo.filter(this.data.attribute, function(item) {
				return item.name != name;
			});
		},
		
		getAttributeValue : function(name, sep) {
			var attr = this.getAttribute(name);
			var separator = sep ? sep : "\n";
			if(attr != null) {
				var arr = dojo.map(attr.children, function(item) {
					return item.value.value;
				});
				
				if(dojo.isArray(arr))
					return arr.join(separator);
				else
					return arr;
			}
			return null;
		}
		
	});
	
}
