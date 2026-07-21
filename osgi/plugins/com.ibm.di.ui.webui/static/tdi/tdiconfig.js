define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/config/basecfg",
	"tdi/config/almapconfig",
	"tdi/config/assemblyline",
	"tdi/config/attmapconfig",
	"tdi/config/attmapitemconfig",
	"tdi/config/attributeloop",
	"tdi/config/connectionconfig",
	"tdi/config/connector",
	"tdi/config/linkcriteria",
	"tdi/config/parserconfig",
	"tdi/config/propstore",
	"tdi/config/schedule",
	"tdi/config/schemaconfig",
	"tdi/config/scriptconfig",
	"tdi/config/tdibranch",
	"tdi/config/tdibranchconditions",
	"tdi/config/tdicontainer",
	"tdi/config/tdisolution",
	"tdi/tdiapi"
], function(
	declare,					// dojo/_base/declare
	array, 						// dojo/_base/array
	tdibasecfg, 				// tdi/config/basecfg
	tdialmapconfig, 			// tdi/config/almapconfig
	tdiassemblyline, 			// tdi/config/assemblyline
	tdiattmapconfig, 			// tdi/config/attmapconfig
	tdiattmapitemconfig, 		// tdi/config/attmapitemconfig
	tdiattributeloop, 			// tdi/config/attributeloop
	tdiconnection, 				// tdi/config/connectionconfig
	tdiconnector, 				// tdi/config/connector
	tdilinkcriteria,			// tdi/config/linkcriteria
	tdiparserconfig, 			// tdi/config/parserconfig
	tdipropstore, 				// tdi/config/propstore
	tdischedule, 				// tdi/config/schedule
	tdischemaconfig, 			// tdi/config/schemaconfig
	tdiscriptconfig, 			// tdi/config/scriptconfig
	tditdibranch, 				// tdi/config/tdibranch
	tditdibranchconditions, 	// tdi/config/tdibranchconditions
	tditdicontainer,			// tdi/config/tdicontainer
	tditdisolution, 			// tdi/config/tdisolution
	tdiapi						// tdi/tdiapi
) {
	
	var arr = [
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/config/basecfg",
	"tdi/config/almapconfig",
	"tdi/config/assemblyline",
	"tdi/config/attmapconfig",
	"tdi/config/attmapitemconfig",
	"tdi/config/attributeloop",
	"tdi/config/connectionconfig",
	"tdi/config/connector",
	"tdi/config/linkcriteria",
	"tdi/config/parserconfig",
	"tdi/config/propstore",
	"tdi/config/schedule",
	"tdi/config/schemaconfig",
	"tdi/config/scriptconfig",
	"tdi/config/tdibranch",
	"tdi/config/tdibranchconditions",
	"tdi/config/connector",
	"tdi/config/tdicontainer",
	"tdi/config/tdisolution",
	"tdi/tdiapi"
];

	return declare(
	[tdibasecfg],
	{
// 	dojo.declare("tdi.tdiconfig", [tdi.basecfg], {
		
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
		
		// includes library
		includes: null,
		
		// script library
		scripts: null,

		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
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
			if(!this.config) {
				this.config = {};
			}
			
			this.folders = new Object();
			this.schedules = new Object();
			this.propertyStores = new Object();
			this.assemblylines = new Object();
			this.connectors = new Object();
			this.parsers = new Object();
			this.scripts = new Object();
			this.includes = new Object();
			
			// Checkouts use solution.container whereas running configs
			// exclude the "solution" property.
			if(this.config.container != null) {
				return;
			}
			if(!this.config.solution) {
				this.config.solution = new Object();
			}
			if(!this.config.solution.container) {
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
				this.config.solution.container.push({
					"@type":"propertyStores",
					name:"Properties",
					config: []
				});
//				this.config.solution.container.push({
//					"@type":"container",
//					name:"Includes",
//					config: []
//				});
			}
			if(!this.config.solution.context ) {
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
			else if(folder == "Includes")
				return cfg.getInclude(obj);
			
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
			if(!this.propertyStores) {
				this.propertyStores = new Object();
				var stores = this.getArray("config", this.folders.Properties);
				for(var i = 0; i < stores.length; i++) {
					var store = stores[i];
					this.propertyStores[store.name] = new tdipropstore({config:store, parentConfig:this});
				}
			}
			for(var name in this.propertyStores) {
				arr.push(name);
			}
			return arr;
		},
		
		addPropertyStore: function(name, path) {
			// description:
			//		Adds a property store using path as the filename.
			//		If path is not defined the name is used as the property filename in
			//		the configs directory.
			var stores = this.getArray("config", this.folders.Properties);
			var file = path ? path : "${config.directory}/" + name + ".properties";
			var store = {
					"@type": "propertyStore",
					"connector": {
						"inheritFrom": "system:/Connectors/ibmdi.Properties",
						"parameter": [
							{
								"value": file,
								"name": "collection"
							},
							{
								"value": name,
								"name": "collectionType"
							}
						]
					},
					"parser": {
						"parameter": []
					},
					"name": name,
					"keyName": "key",
					"valueName": "value",
					"readOnly": false,
					"initialLoad": true,
					"cacheTimeout": 0
			};
			stores.push(store);
			this.propertyStores[store.name] = new tdipropstore({config:store, parentConfig:this});
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
				return name.toLowerCase() == obj.name.toLowerCase();
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
			var nameic = name.toLowerCase();
			if(!this.connectors[nameic]) {
				var config = this.getObjectFromFolder(this.folders.Connectors, name);
				if(config != null) {
					this.connectors[nameic] = new tdiconnector({config:config, parentConfig: this});
				}
			}
			return this.connectors[nameic];
		},
		
		addConnector : function(name, config) {
			if(!this.getConnector(name)) {
				var arr = this.getArray("config", this.folders.Connectors);
				if(arr) {
					config.name = name;
					arr.push(config);
				}
				this.setModified(true);
			}
		},
		
		deleteConnector: function(name) {
			var cfg = this.getArray("config", this.folders.Connectors);
			var arr = dojo.filter(cfg, function(conn) {
				return (conn.name != name);
			});
			this.folders.Connectors.config = arr;
			delete this.connectors[name];
			this.setModified(true);
		},
		
		createLibraryConnector: function(name, mode) {
			var conn = this.createConnector(name, mode);
			this.addConnector(name, conn.config);
			return conn;
		},
		
		createConnector : function(name, mode) {
			return new tdiconnector({
				parentConfig: this,
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
							schema: [
							      {
							    	  "@type":"schema",
							    	  name: "Input"
							      },
							      {
							    	  "@type":"schema",
							    	  name: "Output"
							      }
							],
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
						schema: [
						      {
						    	  "@type":"schema",
						    	  name: "Input"
						      },
						      {
						    	  "@type":"schema",
						    	  name: "Output"
						      }
						]
					}
				}
			});			
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
			if(!this.parsers[name]) {
				var config = this.getObjectFromFolder(this.folders.Parsers, name);
				if(config != null) {
					this.parsers[name] = new tdiparserconfig({config:config, parentConfig: this});
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
			if(!this.scripts[name]) {
				var config = this.getObjectFromFolder(this.folders.Scripts, name);
				if(config != null) {
					this.scripts[name] = new tdibasecfg({config:config, parentConfig: this});
				}
			}
			return this.scripts[name];
		},
		
		addScript : function(name, config) {
			if(!this.getScript(name)) {
				var arr = this.getArray("config", this.folders.Scripts);
				if(arr) {
					config.name = name;
					arr.push(config);
				}
				this.setModified(true);
			}
		},
		
		
		getIncludeNames : function() {
			return this.getFolderNames(this.folders.Includes);
		},
		
		getInclude : function(name) {
			// summary:
			//		Returns the configuration for a specific include
			// returns:
			//		tdi.assemblyline object
			if(this.isExternalRef(name))
				return this.lookup(name);
			
			if(!this.includes[name])
				this.includes[name] = this._getInclude(name);
			return this.includes[name];
		},
		
		_getInclude : function(name) {
			var cfg = this.getArray("config", this.folders.Includes);
			var arr = dojo.filter(cfg, function(inc) {
				return (name == inc.name);
			});
			
			if(arr.length == 1)
				return new tdibasecfg({config:arr[0], parentConfig:this});
			else
				return null;
		},

		createInclude: function(name, ref) {
			var current = this.getInclude(name);
			if(current != null)
				return null;
			
			var newinc = {
				"@type":"include",
				"java.naming.provider.url":ref,
				name:name
			};
			this.folders.Includes.config.push(newinc);
			return this.getInclude(name);
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
			if(this.isExternalRef(assemblyline))
				return this.lookup(assemblyline);
			
			if(!this.assemblylines[assemblyline])
				this.assemblylines[assemblyline] = this._getAssemblyLine(assemblyline);
			return this.assemblylines[assemblyline];
		},
		
		_getAssemblyLine : function(assemblyline) {
			var alcfg = this.getArray("config", this.folders.AssemblyLines);
			var arr = dojo.filter(alcfg, function(al) {
				return (assemblyline == al.name);
			});
			
			if(arr.length == 1)
				return new tdiassemblyline({config:arr[0], parentConfig:this});
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
			if(!this.schedules[schedule])
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
				return new tdischedule({config:arr[0], parentConfig:this});
			else
				return null;
		},
		
		getScheduleForAssemblyLine : function(assemblyline) {
			// summary:
			//		Returns the schedule name for the first schedule config
			// 		that calls assemblyline
			var arr = this.getSchedulesForAssemblyLine(assemblyline);
			if(arr.length > 0)
				return arr[0];
			else
				return null;
		},
		
		getSchedulesForAssemblyLine: function(assemblyline) {
			// summary:
			//		Returns the name of all schedules that invoke assemblyline 
			if(this.folders.Schedules == undefined)
				return [];
			
			var arr = this.getArray("config", this.folders.Schedules) || [];
			arr = dojo.filter(arr, function(item) {
				if(item == null) {
					console.log("NULL item in Schedules folder");
					return;
				}
				return item.assemblyLine == assemblyline;
			});
			arr = array.map(arr, function(item) {
				return item.name;
			});
			return arr;
		},
		
		getSolutionInterface : function() {
			if(!this.solution) {
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
				this.solution = new tditdisolution({config:obj, parentConfig:this});
			}
			return this.solution;
		},
		
		getTombstoneSettings : function() {
			if(this.tombstones == null) {
				var obj = this.getObject("solution.context.tombstone");
				if(obj == null)
					obj = this.getObject("context.tombstone");
				if(obj != null)
					this.tombstones = new tdibasecfg({config:obj});	
			}
			return this.tombstones;
		}
	});
	
});
