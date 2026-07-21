define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/config/basecfg",
	"tdi/config/schemaconfig",
	"tdi/config/connector",
	"tdi/config/scriptconfig",
	"tdi/config/tdicontainer",
	"tdi/config/tdioperation"
], function(declare, array, tdibasecfg, tdischemaconfig, tdiconnector, tdiscriptconfig, tditdicontainer, tdioperation) {
return declare("tdi.config.assemblyline",
	[tdibasecfg],
	{
// 	dojo.declare("tdi.assemblyline", [tdi.basecfg], {
		
		// connector handles
		connectors: null,
		settings: null,
		
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this.connectors = new Object();
			this.operations = new Object();
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
		
		getOperations: function() {
			if(!this.config.operations)
				this.config.operations = {};
			if(!this.config.operations.operation)
				this.config.operations.operation = [];
			return this.config.operations.operation;
		},
		
		getOperation: function(name) {
			var oper = this.operations[name];
			if(!oper) {
				var arr = array.filter(this.getOperations(), function(oper) {
					return oper.name == name;
				});
				
				if(arr.length == 1) {
					oper = new tdioperation({config:arr[0]});
					this.operations[name] = oper;
				}
			}
			return oper;
		},
		
		getOperationNames: function() {
			var arr = array.map(this.getOperations(), function(oper) {
				return oper.name;
			});			
			return arr;
		},
		
		createOperation: function(name) {
			var operation = this.getOperation(name);
			if(!operation) {
				var oper = this.getOperations();
				operation = {};
				operation.name = name;
				operation.schema = [];
				operation.attributeMap = [];
				operation.schema.push({
					"@type":"schema",
					"name": "Input",
					item: []
				});
				operation.schema.push({
					"@type":"schema",
					"name": "Output",
					item: []
				});
				operation.attributeMap.push({
					"@type":"map",
					"name": "Input",
					item: []
				});
				operation.attributeMap.push({
					"@type":"map",
					"name": "Output",
					item: []
				});
				oper.push(operation);
				this.setModified(true);
			}
			return this.getOperation(name);
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
				this.initParams = new tdischemaconfig({config:this.config.initParams.schema, parentConfig:this});
			}
			return this.initParams;
		},
		
		getSettings : function() {
			if(this.settings == null) {
				this.settings = new tdibasecfg({config:this.config.settings});
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
			this.getEntryFeedComponent().addComponent(conn);
			this.setModified(true);
		},
		
		addDataFlowComponent : function(conn) {
			this.getDataFlowComponent().addComponent(conn); // component.push(conn.config);
			this.setModified(true);
		},
		
		
		getDataFlowComponent : function() {
			// summary:
			//		Returns the container object for the data flow component
			if(!this._dataFlowComponent) {
				this._dataFlowComponent = new tditdicontainer({config:this.getDataFlow(), parentConfig:this});
			}
			return this._dataFlowComponent;
		},
		
		getEntryFeedComponent : function() {
			// summary:
			//		Returns the container object for the entry feed component
			if(!this._entryFeedComponent) {
				this._entryFeedComponent = new tditdicontainer({config:this.getEntryFeed(), parentConfig:this});
			}
			return this._entryFeedComponent;
		},
		
		getComponentByName : function(name) {
			// summary:
			//		Returns the config for the named component
			if(name == "DataFlowContainer")
				return this.getDataFlowComponent();
			else if(name == "EntryFeedContainer")
				return this.getEntryFeedComponent();
			
			var comp = this.getDataFlowComponent().getComponent(name, true);
			if(!comp)
				comp = this.getEntryFeedComponent().getComponent(name, true);
			return comp;
		},
		
		_addComponentNames : function(container, recursive, list) {
			dojo.forEach(container.getComponentNames(), dojo.hitch(this, function(name) {
				list.push(name);
				var comp = container.getComponent(name);
				if(comp.isContainer() && recursive)
					this._addComponentNames(comp, recursive, list);
			}));
		},
		
		getComponentNames : function(recursive) {
			var names = new Array();
			dojo.forEach(this.getConnectorNames(), dojo.hitch(this, function(obj) {
				var comp = this.getComponentByName(obj.name);
				names.push(obj.name);
				if(comp.isContainer() && recursive)
					this._addComponentNames(comp, recursive, names);
			}));
			return names;
		},
		
		getConnectorNames : function() {
			var conn = dojo.filter(this.getArray("component", this.getEntryFeed()), function(item) {
				if(item.complexConfig != null)
					return item.complexConfig["@type"] == "connector" || item.complexConfig["@type"] == "function";
				else if(item["@type"] !== undefined)
					return item["@type"] == "connector";
				else
					return false;
			});
			return conn.concat(dojo.filter(this.getArray("component", this.getDataFlow()), function(item) {
				if(item.complexConfig != null)
					return item.complexConfig["@type"] == "connector" || item.complexConfig["@type"] == "function";
				else if(item.simpleConfig)
					return item.simpleConfig["@type"] == "script";
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
				return new tdiconnector({config:conn[0], parentConfig:this});
			
			var df = this.getDataFlow();
			var arr = this.getArray("component", df);
			var conn = dojo.filter(arr, function(item) {
				return item.name == name;
			});
			if(conn.length == 1)
				return new tdiconnector({config:conn[0], parentConfig:this});
			else
				return null;
		},
		
		getActiveHookNames : function() {
			
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
		
		getSchedules: function() {
			// summary:
			//		Returns the schedules that calls this assemblyline
			return this.getTop().getSchedulesForAssemblyLine(this.getName());
		},
		
		
		getWorkAttributes : function(untilComponent) {
			// summary:
			//		Returns the work attributes available to untilComponent
			var arr = new Array();
			dojo.every(this.getConnectorNames(), dojo.hitch(this, function(conn) {
				if(conn.name == untilComponent)
					return false;
				var c = this.getConnector(conn.name);
				if(c.isFunction() || c.getMode() == "Iterator" || c.getMode() == "Lookup" || c.getMode() == "Server") {
					try {
						var cfg = c.getAttributeMap(true).getNames();
//						if(cfg && cfg.length == 0) {
//							if(/ibmdi.AssemblyLineConnector|ibmdi.AssemblyLineFC/.match(c.getInheritFrom()) {
//								
//							}
//						}
						arr = arr.concat(cfg);
					} catch(ignore) {}
				}
				
				return true;
			}));
			return arr;
		},
		
		getWorkAttributeMaps : function(untilComponent) {
			// summary:
			//		Returns the work attributes' attribute map itmes available until untilComponent is found
			var arr = new Array();
			
			dojo.every(this.getComponentNames(true), dojo.hitch(this, function(obj) {
				var comp = this.getComponentByName(obj);
				if(untilComponent && comp.getName() == untilComponent)
					return false;
				
				if(comp.isConnector() || comp.isFunction()) {
					if(comp.isFunction() || comp.getMode() == "Iterator" || comp.getMode() == "Lookup" || comp.getMode() == "Server") {
						try {
							var carr = new Array();
							dojo.forEach(comp.getAttributeMap(true).getNames(), function(attr) {
								carr.push({name:attr, component:obj, id:obj+"_"+attr});
							});
							arr = arr.concat(carr);
						} catch(ignore) {}
					}
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
			return conn;
		},
		
		createDataFlowFunction: function(name) {
			var conn = this.createFunction(name);
			conn.parentConfig = this;
			this.addDataFlowComponent(conn);
			return conn;
		},
		
		createDataFlowScript: function(name) {
			var conn = this.createScriptComponent(name, null);
			conn.parentConfig = this;
			this.addDataFlowComponent(conn);
			return conn;
		},
		
		createConnector : function(name, mode) {
			return new tdiconnector({
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
		
		createFunction: function(name) {
			return new tdiconnector({
				config: {
					"@type":"complex",
					name: name,
					initialize: "onStartup",
					sandboxPlayback: false,
					sandboxRecord: false,
					simulateState: "Enabled",
					state: "Enabled",
					complexConfig: {
						"@type":"function",
						inheritFrom: "[parent]",
						rawConfig: {
							inheritFrom:"[parent]",
							parameter: []
						},
						hooks: {},
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
						schema: []
					}
				}
			});			
		},
		
		createScriptComponent : function(name, script) {
			var conn = new tdiscriptconfig({
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
			return conn;
		}
	});

});
