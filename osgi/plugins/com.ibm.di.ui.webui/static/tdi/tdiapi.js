define([
    	"dojo/_base/declare",
    	"dojo/_base/lang",
    	"tdi/NlsMixin",
    	"tdi/tdiconstants",
    	"idx/dialogs"
    ], function(declare, lang, tdinls, tdiconstants, idx) {

	var tdiapi = {
		_debug : false,
		_format : "json",
		_url_prefix : "/fds",
		_format_xml : "xml",
		_url_prefix_rest : "/rest",
		_logPollTimeout : 3,
		_namespaces: {},
		_preventCache: true,
		_batchCap: 1,
		_waitTimeout: 1
	};
	
	tdiapi.nls = new tdinls({});
	
	tdiapi.getStdNamespaces = function() {
		this.getNamespace("system");
		this.getNamespace("adapter");
	};

	tdiapi.getNamespace = function(name) {
		if(!this.tdiconfig) {
			require(["tdi/tdiconfig"], dojo.hitch(this, function(config) {
				this.tdiconfig = config;
			}));
		}
		if(!this._namespaces[name]) {
			dojo.xhrGet({
				url:"/rest/internal/" + name,
				handleAs: "json",
				preventCache: this._preventCache,
				sync: true, // this is not good but currently our only reasonable option
				headers: {
					"Accept":"application/com.ibm.di.configuration+json"
				},
				error: function(err) {
					console.log(err);
				},
				load: dojo.hitch(this, function(data) {
					try {
						this._namespaces[name] = new this.tdiconfig({config:data});
					} catch(err) {
						alert(err);
					}
				})
			});
		}
		return this._namespaces[name];
	};
	
	tdiapi.getVMStatus = function() {
		// summary:
		// 		Requests the VMStatus object from the server
		// return:
		// 		The dojo.Deferred object from dojo.xhrDelete
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix + "/server/vmstatus"
		});
	};
	
	tdiapi.deleteConfig = function(configentry) {
		// summary:
		// 		Permanently deletes a configuration from the server
		// description:
		// 		Removes the configuraiton from the server.
		// configentry: tdi.tdiconfigentry
		//		The config entry atom for the instance
		// return:
		// 		The dojo.Deferred object from dojo.xhrDelete
		//
		return dojo.when(dojo.xhrDelete({url : configentry.getLink("self").href}), function() {
			return deleteLogAndTombstones();
		});
//		return dojo.xhrDelete( {
//			url : configentry.getLink("self").href
//		});
	};
	
	tdiapi.deleteConfigAndData = function(configid) {
		// summary:
		//		Deletes the tombstones, logfiles and configuration for configid
		//
		return dojo.xhrDelete({url: this._url_prefix + "/server/configdata/" + configid});
	};
	
	tdiapi.startTempConfig = function(config, instanceId) {
		// summary:
		//		Starts a temporary config on the TDI server
		// config: tdi.tdiconfig
		//		The configuration
		// instanceId: String
		//		The instanceId (null = use solution name)
		// returns:
		//		dojo.Deferred from the post operation
		var runName = instanceId == null ? config.getConfigName() : instanceId;
		var pd = {
			solution:config.config.solution,
			keepAlive: true,
			runName: runName,
			logListener : {
		        "@type": "logListener",
		        channel: {
		            "@type": "pollChannel",
		            waitTimeout: this._logPollTimeout,
		            batchCap: 10,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
		
		var postData = dojo.toJson(pd);
		
		if(this._debug) {
			console.log("startTempConfig");
			console.log(dojo.toJson(pd, true));
		}
			
		return dojo.xhrPost( {
			handleAs : this._format,
			postData: postData,
			headers: {
				"Accept": "application/json",
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			url : this._url_prefix_rest + "/ci"
		});
		
	};
	
	tdiapi.startConfig = function(configentry, instanceId) {
		// summary:
		// 		Starts a config instance on the TDI server
		// description:
		// 		Launches a config instance on the TDI server.
		// 		The configId is the file/identifier of the configuration file and the
		// 		optional instanceId specifies the runtime identifier of the config.
		// configentry: tdi.tdiconfigentry |�String
		//		The config entry atom for the instance or the configRef
		// instanceId: String
		//		The instanceId (null = use solution name)
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var pd = {
			configRef:configentry.getLink("self").href,
			keepAlive: true,
			logListener : {
		        "@type": "logListener",
		        channel: {
		            "@type": "pollChannel",
		            waitTimeout: this._logPollTimeout,
		            batchCap: 7,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
		
		return dojo.xhrPost( {
			handleAs : this._format,
			postData: dojo.toJson(pd),
			headers: {
				"Accept": "application/json",
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			url : this._url_prefix_rest + "/ci"
		});
	};
	
	tdiapi.getCIEntry = function(configId) {
		// summary:
		// 		Stops the config instance on the TDI server
		// configId: String
		//		The instance identifier to find
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
			return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/ci/" + configId 
		});
	};
	
	tdiapi.stopConfig = function(cientry) {
		// summary:
		// 		Stops the config instance on the TDI server
		// cientry: tdi.tdicientry
		//		The config instance entry atom for the instance
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrDelete( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : cientry.getLink("self").href
		});
	};
	
	tdiapi.reloadConfig = function(config) {
		// summary:
		//		Sends a reload-config to the config
		// config: String
		//		The config identifier
		return dojo.xhrGet({
			preventCache: this._preventCache,
			url: "/fds/ldapsync/reloadconfig?id="+config
		});
	};

	tdiapi.getInstalledComponents = function() {
		// summary:
		// 		Queries the server for installed configurations
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/server/info/comp"
		});
	};

	tdiapi.startAssemblyLine = function(cientry, assemblyLine, sync, tcb) {
		// summary:
		// 		Starts an assemblyline instance in the CI specified by ciAtom
		// description:
		// 		Starts the assemblyline in the named configuration. If the
		// 		configuration is not running a new instance is started for the assemblyline to run in.
		// cientry: tdi.tdicientry
		//		The config instance entry atom for the instance
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var pd = {
				name:assemblyLine,
				sync:sync ? sync : false
//				assemblyLineListener : {
//			        "@type": "assemblyLineListener",
//			        deliverEntry: false,
//			        deliverLogs: true,
//			        channel: {
//			            "@type": "pollChannel",
//			            waitTimeout: 10,
//			            batchCap: 7,
//			            fillBatch: true,
//			            onTimeoutGetAll: true
//			       }
//				}
		};
		if(tcb)
			pd.tcb = tcb;
		return dojo.xhrPost( {
			handleAs : this._format,
			headers: {
				"Accept": "application/json",
				"Content-Type": "application/com.ibm.di.api.assembly-line+json"
			},
			postData: dojo.toJson(pd),
			url : cientry.getLink("assembly-line").href
		});
	};

	tdiapi.stopAssemblyLine2 = function(configid, assemblyline) {
		// summary:
		// 		Stops an assemblyline instance on the TDI server
		// description:
		// 		Stops an assemblyline instance on the TDI server
		// configid: string
		//		The config instance id
		// assemblyline: string
		//		The assemblyline to stop. Can either be simple name (in which case all matching assemblylines are terminated)
		//		or the unique identifier for the assemblyline.
		// return:
		// 		The dojo.Deferred object from dojo.xhrDelete
		//
		return dojo.xhrDelete( {
			handleAs : this._format,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix + "/server/ci/" + configid + "/" + assemblyline
		});
	};
	
	tdiapi.stopAssemblyLine = function(alentry) {
		// summary:
		// 		Stops an assemblyline instance on the TDI server
		// description:
		// 		Stops an assemblyline instance on the TDI server
		// alentry: tdi.tdialentry
		//		The assemblyline instance entry atom for the instance
		// return:
		// 		The dojo.Deferred object from dojo.xhrDelete
		//
		return dojo.xhrDelete( {
			url : alentry.getLink("self").href
		});
	};

	tdiapi.stepAssemblyLine = function(cientry, assemblyLine) {
		// summary:
		// 		Starts an assemblyline instance on the TDI server in manual mode.
		// description:
		// 		Starts the assemblyline in the named configuration. The return object contains
		// 		a "handle" property that should be used with the getNextEntry method.
		// cientry: tdi.tdicientry
		//		The config instance entry atom for the instance
		// assemblyLine: String
		//		The assemblyline name to start
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		var pd = {
			name:assemblyLine,
			sync:false,
			manual:false,
			tcb: {
				"@type":"taskCallBlock",
				property: [
				    {"name":"assemblyline.debugport", "value":"1"}
				]
			},
			assemblyLineListener : {
		        "@type": "assemblyLineListener",
		        deliverEntry: false,
		        deliverLogs: true,
		        channel: {
		            "@type": "pollChannel",
		            waitTimeout: 10,
		            batchCap: 7,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
//		property: [
//		           {name:"assemblyline.debugport", value:"1"}
//		]
		return dojo.xhrPost( {
			handleAs : this._format,
			headers: {
				"Accept": "application/json",
				"Content-Type": "application/com.ibm.di.api.assembly-line+json"
			},
			postData: dojo.toJson(pd),
			url : cientry.getLink("assembly-line").href
		});
	};
	
	tdiapi.manualAssemblyLine = function(cientry, assemblyLine) {
		// summary:
		// 		Starts an assemblyline instance on the TDI server in manual mode.
		// description:
		// 		Starts the assemblyline in the named configuration. The return object contains
		// 		a "handle" property that should be used with the getNextEntry method.
		// cientry: tdi.tdicientry
		//		The config instance entry atom for the instance
		// assemblyLine: String
		//		The assemblyline name to start
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		var pd = {
			name:assemblyLine,
			sync:false,
			manual:true,
			assemblyLineListener : {
		        "@type": "assemblyLineListener",
		        deliverEntry: false,
		        deliverLogs: true,
		        channel: {
		            "@type": "pollChannel",
		            waitTimeout: 10,
		            batchCap: 7,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
		return dojo.xhrPost( {
			handleAs : this._format,
			headers: {
				"Accept": "application/json",
				"Content-Type": "application/com.ibm.di.api.assembly-line+json"
			},
			postData: dojo.toJson(pd),
			url : cientry.getLink("assembly-line").href
		});
	};

	tdiapi.executeScript = function(alentry, script) {
		// summary:
		//		Executes script code in the alentry's context
		// returns:
		//		The dojo.Deferred object from dojo.xhrPost
		return dojo.xhrPost( {
			handleAs : "json",
			headers: {
				"Content-Type" : "text/plain"
			},
			postData: script,
			url : alentry.getLink("script").href
		});
	};
	
	tdiapi.getALStatus = function(alentry) {
		// summary:
		//		Returns the assemblyline's status object
		// returns:
		//		The dojo.Deferred object from dojo.xhrGet
		return dojo.xhrGet( {
			handleAs : "json",
			url : alentry.getLink("status").href
		});
	};

	tdiapi.executeALCycle = function(alentry) {
		// summary:
		// 		Sends an execute cycle request to the assemblyline.
		// description:
		// 		Sends an execute cycle request to the assemblyline.
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPut( {
			handleAs : this._format,
			headers: {
				"Accept" : "application/com.ibm.di.api.assembly-line+json",
				"Content-Type" : "application/com.ibm.di.api.assembly-line+json"
			},
			postData: "{}",
			url : alentry.getLink("handle").href
		});
	};
	
	tdiapi.getNextEntry = function(alentry) {
		// summary:
		// 		Returns the last entry from the assemblyline started by stepAssemblyLine.
		// description:
		// 		Returns the last entry from the assemblyline started by stepAssemblyLine.
		//		Use executeALCycle to cycle the assemblyline before calling this method.
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : alentry.getLink("handle").href
		});
	};
	
	tdiapi.getResultEntry = function(alentry) {
		// summary:
		// 		Returns the result entry from the assemblyline
		// description:
		// 		Returns the result entry from the assemblyline.
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept" : "application/com.ibm.di.api.entry+json"
			},
			url : alentry.getLink("result").href
		});
	};
	
	tdiapi.getConfigInstances = function() {
		// summary:
		//		Returns the list of running config instances (feed)
		// return:
		//		The dojo.Deferred from dojo.xhrGet
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/ci"
		});
	};

	tdiapi.getConfigInstance = function(configid) {
		// summary:
		//		Returns the list of running config instances (feed)
		// return:
		//		The dojo.Deferred from dojo.xhrGet
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/ci/" + configid
		});
	};

	tdiapi.getAssemblyLineList = function(cientry) {
		// summary:
		// 		Requests the list of running assemblylines for configId
		// description:
		// 		Requests the list of running assemblylines for the configId
		// cientry: tdi.cientry
		//		The config instance entry
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return cientry.getLinkData("assembly-line");
	};

	tdiapi.getAssemblyLineLog = function(alentry) {
		// summary:
		// 		Requests the last 1k log of a running assemblyline
		// description:
		// 		Requests the last 1k log of a running assemblyline
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : "text",
			preventCache: this._preventCache,
			url : alentry.getLink("log").href
		});
	};

	tdiapi.getAssemblyLineLogs = function(config, alname, date) {
		// summary:
		// 		Requests the logs for a specific config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix + "/log/" + config + "/" + alname + (date != null ? "?date=" + date : "")
		});
	};
	
	tdiapi.getLogfileEntry = function(logname, config, alname) {
		// summary:
		// 		Requests the logfile details for a specific config/al/logfile
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		var url = null;
		if(config)
			url = this._url_prefix + "/log/" + config + "/" + alname + "/" + logname;
		else
			url = this._url_prefix + "/log/" + logname;
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept" : "application/json"
			},
			url : url
		});
	};
	
	tdiapi.getTDILogFile = function(options) {
		// summary:
		// 		Requests the logs for a specific config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPost( {
			handleAs : this._format,
			headers: {
				"Content-Type" : "application/json+log",
				"Accept" : "application/json+log"
			},
			postData:dojo.toJson(options),
			url : this._url_prefix + "/log/search"
		});
	};
	
	tdiapi.getTombstoneManagerStatus = function() {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : "text",
			preventCache: this._preventCache,
			url : this._url_prefix + "/server/tombstonemanager"
		});
	};
	
	tdiapi.stopTombstoneManager = function() {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrDelete( {
			url : this._url_prefix + "/server/tombstonesmanager"
		});
	};
	
	tdiapi.startTombstoneManager = function() {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPut( {
			url : this._url_prefix + "/server/tombstonesmanager"
		});
	};
	
	tdiapi.getTombstones = function(config, alname) {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix + "/ts/" + config + "/" + alname
		});
	};

	tdiapi.getAssemblyLineTS = function(config, alname) {
		// summary:
		// 		Requests the last tombstone entry for the specified
		//		config/assemblyline (use * to match all).
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.when(dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/ts/ci/" + config + "/al/" + alname + "/ts"
		}), function(data) {
			var feed = new tdi.tdifeed({feed:data});
			var ts = feed.getEntries();
			if(ts != null && ts.length > 0) {
				return dojo.xhrGet({
					handleAs : this._format,
					preventCache: this._preventCache,
					url:ts[ts.length-1].content.src
				});
			} else {
				return null;
			}
		});
	};

	tdiapi.getConnectorType = function(config) {
		// summary:
		// 		Attempts to locate the correct object from which to load
		//		a form. It searches for a custom form ($form$) and uses that reference if found.
		// return:
		// 		The component name (e.g. ibmdi.LDAP) or a custom form (e.g. ConfigRef:ConnectorName)
		//
		var con = null;
		if(config) {
			if(config.getConnectionConfig().getParam("$form$")) {
				con = this._findConnectorType(config);
			} else {
				con = config.getConnectorType();
				if(con != null && con.indexOf("/") != -1) {
					con = con.substring(con.lastIndexOf("/") + 1);
				}
			}
		}
		return con;
	},
	
	tdiapi._findConnectorType = function(config) {
		if(!config)
			return null;
		con = config.getTop().getConfigName() + ":" + config.getName();
		if(config.getConnectionConfig().getParamByName("$form$", true))
			return con;
		else
			return this._findConnectorType(config.getInheritedObj());
	};
	
	tdiapi.getComponentForm = function(url, lang) {
		// summary:
		// 		Requests the url as com.ibm.di.api.component
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers : {
				Accept: "application/com.ibm.di.api.component+json"
			},
			url : url
		});
	};
	

	tdiapi.getConnectorForm = function(conn, lang) {
		// summary:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// description:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var connector = conn;
		if(connector == undefined)
			return null;
		if(connector.indexOf("/") != -1) {
			connector = connector.substring(connector.lastIndexOf("/")+1);
		}
		return this.getComponentForm("/rest/server/info/comp/co/" + connector + "/content", lang);
	};

	tdiapi.getFunctionForm = function(conn, lang) {
		// summary:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// description:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var connector = conn;
		if(connector == undefined)
			return null;
		if(connector.indexOf("/") != -1) {
			connector = connector.substring(connector.lastIndexOf("/")+1);
		}
		return this.getComponentForm("/rest/server/info/comp/fc/" + connector + "/content", lang);
	};

	tdiapi.getParserForm = function(parser, lang) {
		// summary:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// description:
		// 		Retrieves the form definition for a connector with labels/tooltips in the specified language
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var comp = parser;
		if(comp == undefined)
			return null;
		if(comp.indexOf("/") != -1) {
			comp = comp.substring(comp.lastIndexOf("/")+1);
		}
		return this.getComponentForm("/rest/server/info/comp/ps/" + comp + "/content", lang);
	};

	tdiapi.getConfigEntry = function(config) {
		// summary:
		//		Returns the config entry object for a config id
		// config: String
		//		The config identifier
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url: this._url_prefix_rest + "/config/e%3A" + config
		}).then(function(data) {
			var centry = new tdi.tdiconfigentry({atom:data})
			return centry;
		});
	};
	
	tdiapi.getConfigObjects = function(dir) {
		// summary:
		//		Returns a list of configuration objects in a subdirectory
		// dir: String
		//		The subdirectory (relative to TDI configs directory or absolute path)
		var def = dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/config/" + dir
		});
		// Returns
		return dojo.when(def,
			function(response) {
				var configList = new Array();
				var result = new Object();
				result.label = "name";
				result.identifier = "id";
				result.items = configList;
				dojo.forEach(response.entry, function(node) {
					configList.push({id:node.id, name:node.title.value, entry:new tdi.tdiconfigentry({atom:node}), type:"project"});
				});
				return result;
			}, this.defaultErrHandler);
	};
	
	tdiapi.getServerProjects = function(arrayOnly) {
		// summary:
		// 		Returns a list of projects/configs in the server's configs directory
		// description:
		// 		Returns a list of projects/configs in the server's configs directory
		// arrayOnly: boolean
		//		If true only the items array is returned
		// return:
		// 		A dojo.when promise after parsing the XML from the server
		//
		var def = dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/config"
		});
		
		// Returns
		return dojo.when(def,
			function(response) {
				var configList = new Array();
				var result = new Object();
				result.label = "name";
				result.identifier = "id";
				result.items = configList;
				dojo.forEach(response.entry, function(node) {
					configList.push({url:node.id, id:node.title.value, name:node.title.value, entry:new tdi.tdiconfigentry({atom:node})});
				});
				if(arrayOnly)
					return configList;
				else
					return result;
			}, this.defaultErrHandler);
	};

	tdiapi.getServerStoreData = function(store) {
		// summary:
		// 		Returns the contents of a server store
		// description:
		// 		Returns the contents of a server store
		//
		return dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix + "/ds/" + store
		});
	};
	
	tdiapi.getServerStores = function() {
		// summary:
		// 		Returns a list of server stores
		// description:
		// 		Returns a list of server stores
		//
		var def = dojo.xhrGet( {
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix + "/ds"
		});
		
		// Returns
		return dojo.when(def,
			function(response) {
				var configList = new Array();
				var result = new Object();
				result.label = "name";
				result.identifier = "id";
				result.items = configList;
				dojo.forEach(response.Stores, function(node) {
					configList.push({id:node.Name, name:node.Name, type:node.Type});
				});
				return result;
			}, this.defaultErrHandler);
	};

	tdiapi.getETLProjects = function(user) {
		// summary:
		// 		Retrieves list of ETL project for the user
		// description:
		// 		Retrieves list of ETL project for the user
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		return dojo.xhrPost( {
			handleAs : this._format,
			url : this._url_prefix + "?etl=get" + "&user=" + user
		});
	};

	tdiapi.createETLProject = function(project, user) {
		// summary:
		// 		Creates a new ETL project for the specified user.
		// description:
		// 		Creates a new ETL project for the specified user.
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		return dojo.xhrGet( {
			handleAs : "text",
			preventCache: this._preventCache,
			url : this._url_prefix + "/create/" + project
		});
	};
	
	tdiapi.getAtomLink = function(atom, rel) {
		// summary:
		// 		Returns the link entry from an atom message
		// description:
		// 		Loops over the link items in the atom message and returns the one
		//		matching rel.
		// return:
		// 		The link entry or null if not found
		//
		var arr = dojo.filter(atom.link, function(item) {
			return item.rel == rel;
		});
		if(arr.length == 1)
			return arr[0];
		else
			return null;
	};
	
	tdiapi.getAtomLinkData = function(ci, rel) {
		// summary:
		// 		Returns the contents of an atom link.
		// description:
		// 		Locates the link item matching <i>rel</i> and returns
		//		a dojo.Deferred object for the link's URL
		// return:
		// 		The dojo.Deferred object from dojo.getGet
		//
		var link = getAtomLink(ci, rel);
		if(link == null)
			return null;
		
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url : link.href
		});
	};
	
	tdiapi.getConfig = function(configentry) {
		// summary:
		//		Returns the configuration for the config entry.
		// description:
		// 		Checks out and immediatly unlocks a configuration from the server before
		//		returning the config.
		// configentry:
		//		The tdi.tdiconfigentry object for the config or String configId
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		if(typeof(configentry) == "string") {
			return this.getConfigEntry(configentry).then(function(entry) {
				return tdiapi.getConfig(configentry); 
			});
		} else {
			return dojo.when(this.checkOutConfig(configentry), dojo.hitch(this, function(config) {
				this.unlockConfig(configentry);
				return config;
			}));
		}
	};

	tdiapi.checkOutConfig = function(configentry) {
		// summary:
		// 		Checks out a configuration from the server
		// description:
		// 		Checks out a configuration from the server
		// configentry:
		//		The tdi.tdiconfigentry object for the config
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPost({
			handleAs : this._format,
			headers : {
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			postData: "{}",
			url : configentry.getLink("lock").href
		});
	};
	
	tdiapi.unlockConfig = function(configentry) {
		// summary:
		// 		Checks out a configuration from the server
		// description:
		// 		Checks out a configuration from the server
		// configentry:
		//		The tdi.tdiconfigentry object for the config
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrDelete( {
			handleAs : "text",
			url : configentry.getLink("lock").href
		});
	};

	tdiapi.checkInConfig = function(configentry, config) {
		// summary:
		// 		Checks out a configuration from the server
		// description:
		// 		Checks out a configuration from the server
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		var post = {
			solution: config
		};
		
		if(this._debug) {
			console.log("checkInConfig(" + configentry.getLink("lock").href + ", " + config + ")");
			console.log(dojo.toJson(post, true));
		}
		
		return dojo.xhrPut({
			handleAs : "text",
			headers : {
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			postData: dojo.toJson(post),
			url : configentry.getLink("lock").href
		});
	};
	
	tdiapi.getServerFeed = function() {
		// summary:
		//		Requests the server feed from the REST server
		// returns:
		//		dojo.Deferred form xhrGet
		return dojo.xhrGet({
			handleAs : this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix_rest + "/server"
		});
	};
	
	tdiapi.getServerInfo = function() {
		// summary:
		//		Returns the server info record for the REST server
		// description:
		//		This is done in a sequence of async operations. First it obtains
		//		the server feed from which the "info" entry is located. If that is
		// 		found its "self" link is read and returned.
		// returns:
		//		The dojo.Deferred from xhrGet
		
		return dojo.xhrGet({
			handleAs : this._format,
			preventCache: this._preventCache,
			url : this._url_prefix_rest + "/server/info/content"
		});
//		return dojo.when(getServerFeed(),
//			function(data) {
//				var feed = new tdi.tdifeed({feed:data});
//				return dojo.when(feed.getEntry("info").getLinkData());
//			}, this.defaultErrHandler
//		);
	};
	
	tdiapi.getServerLogs = function() {
		// summary:
		//		Returns the last 100 lines of the ibmdi.log
		//		The position in the ibmdi.log is remembered between calls.
		return dojo.xhrGet({
			handleAs: "json",
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix + "/log"
		});
	};
	
	tdiapi.getServerLog = function() {
		// summary:
		//		Returns the last 100 lines of the ibmdi.log
		//		The position in the ibmdi.log is remembered between calls.
		return dojo.xhrGet({
			handleAs: "text",
			preventCache: this._preventCache,
			headers: {
				"Accept": "text/plain"
			},
			url : this._url_prefix + "/server/log"
		});
	};
	
	tdiapi.getLogSettings = function() {
		// summary:
		//		Returns the JSON object with the current
		//		global SystemAppender log settings.
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : this._url_prefix + "/server/logging"
		});
	};
	
	tdiapi.saveLogSettings = function(settings) {
		// summary:
		//		Updates global SystemAppender log settings.
		//
		return dojo.xhrPut({
			handleAs: this._format,
			url : this._url_prefix + "/server/logging",
			headers : {
				"Content-Type": "application/json"
			},
			postData: dojo.toJson(settings)
		});
	};

	tdiapi.createSolution = function(config) {
		var filename = config.getConfigName();
		if(filename && !filename.match(/\.xml$/)) {
			filename += ".xml";
		}
		var pd = {
			solution:(config.config.solution || config.config),
			name:filename,
			overwrite:false,
			encrypt:false,
			leaveCheckOut:false
		};
		var pdx = dojo.toJson(pd);
		console.log(pdx);
		return dojo.xhrPost({
			handleAs: this._format,
			postData:dojo.toJson(pd),
			url : "/rest/config",
			headers : {
				"Content-Type": "application/com.ibm.di.api.configuration+json;type=createConfig"
			}			
		});
	};
	tdiapi.getWebCeTemplates = function() {
		// summary:
		//		Returns a list of installed templates from the WebCE plugin
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/templates"
		});
	};
	
	tdiapi.createWebCeTemplate = function(templateId, solutionName) {
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/templates/" + templateId + "/" + solutionName
		});
	};
	
	tdiapi.getPropertyStore = function(cientry, storeName) {
		// summary:
		//		Returns the property store atom entry for a given store
		// cientry: tdi.cientry
		//		The config instance entry
		// storeName: String
		//		The store name
		// returns:
		//		The promise from dojo.when
		return dojo.when(
			cientry.getLinkData("property-store"), 
			function(data) {
				var feed = new tdi.tdifeed({feed:data});
				var arr = feed.getEntries(function(ps) {
					return storeName == ps.title.value;
				});
				if(arr.length == 1)
					return new tdi.tdiatom({atom:arr[0]});
				else
					return null;
			}
		);
	};
	
	tdiapi.getPropertyStoreValues = function(cientry, storeName) {
		// summary:
		//		Returns the property name/value pairs for a given store
		// cientry: tdi.cientry
		//		The config instance entry
		// storeName: String
		//		The store name
		// returns:
		//		The promise from dojo.when
		return tdiapi.getPropertyStore(cientry, storeName)
			.then(function(store) {
				if(store == null)
					return null;
				return store.getLink("properties");
			})
			.then(function(store) {
				if(store != null)
					return dojo.xhrGet({
						url:store.href,
						headers: {
							"Accept": store.type
						},
						handleAs:"json",
						preventCache: this._preventCache
					});
				else
					return null;
			});
	};
	
	tdiapi.setPropertyStoreValues = function(cientry, storeName, storeData) {
		// summary:
		//		Updates the property name/value pairs for a given store
		// cientry: tdi.cientry
		//		The config instance entry
		// storeName: String
		//		The store name
		// storeData: Object
		//		Structure with props ( {commit:true/false, property:[ {name:name, value:value, encrypt:true/false} ]} )
		// returns:
		//		The promise from dojo.when
		return tdiapi.getPropertyStore(cientry, storeName)
			.then(function(store) {
				if(store == null)
					return null;
				return store.getLink("properties");
			})
			.then(function(store) {
				if(store != null)
					return dojo.xhrPut({
						url:store.href,
						headers: {
							"Content-Type": store.type
						},
						postData: dojo.toJson(storeData),
						handleAs:"json",
						preventCache: this._preventCache
					});
				else
					return null;
			});
	};
	
	tdiapi.getJavaProperty = function(property) {
		// summary:
		//		Returns the Java VM property
		return xhrGet({
			handleAs: "text",
			preventCache: this._preventCache,
			url:tdidapi._url_prefix + "/server/java/prop/" + property
		});
	};
	
	tdiapi.setJavaProperty = function(property, value) {
		// summary:
		//		Updates the Java VM property and persists it to solution.properties
		return xhrPut({
			handleAs: "text",
			url:tdidapi._url_prefix + "/server/java/prop/" + property + "/" + value + "?persist=true"
		});
	};
	
	tdiapi.getDashboardAuth = function() {
		// summary:
		//		Returns the Dashboard security settings
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/server/auth"
		});
	};
	
	tdiapi.setDashboardAuth = function(auth) {
		// summary:
		//		Updates the Dashboard security settings
		return dojo.xhrPut({
			handleAs: this._format,
			postData:dojo.toJson(auth),
			headers: {
				"Content-Type":"application/json"
			},
			url: this._url_prefix + "/server/auth"
		});
	};
	
	tdiapi.getActiveSchedules = function() {
		// summary:
		//		Returns the active assemblyline schedules for all config instances
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/server/schedules"
		});
	};
	
	tdiapi.stopSchedule = function(config, name) {
		// summary:
		//		Terminates an active scheduler
		// config: String
		//		The config identifier
		// name: String
		//		The scheduler name
		// return:
		//		dojo.xhr promise
		return dojo.xhrDelete({
			preventCache: this._preventCache,
			url:this._url_prefix + "/server/schedules/"+config+"/"+name
		});
	};
	
	tdiapi.startSchedule = function(config, name) {
		// summary:
		//		Starts the scheduler
		// config: String
		//		The config identifier
		// name: String
		//		The scheduler name
		// return:
		//		dojo.xhr promise
		return dojo.xhrPut({
			handleAs: this._format,
			preventCache: this._preventCache,
			url:this._url_prefix + "/server/schedules/"+config+"/"+name
		});
	};
	
	tdiapi.getChildAssemblyLines = function(alid) {
		// summary:
		//		Returns a list of child assemblylines for alid
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/server/assemblyline/" + alid + "/children"
		});
	};
	
	tdiapi.getActiveThreads = function() {
		// summary:
		//		Returns the active threads in the TDI server's JVM
		return dojo.xhrGet({
			handleAs: this._format,
			preventCache: this._preventCache,
			url: this._url_prefix + "/server/threads"
		});
	};
	
	tdiapi.queryConnector = function(tdicfg, assemblyline, connector, command) {
		// summary:
		//		Launches a config with a single connector in passive mode
		//		and executes a script command on the connector.
		// description:
		//		This method creates a temporary config instance with the provided
		//		config. The connectors in the config are set to passive mode and
		//		the command is executed in a script component. The result is then
		//		returned from this command.
		// tdicfg:
		//		The configuration object
		// assemblyline:
		//		The assemblyline where the connector lives
		// connector:
		//		The connector we are querying
		// command:
		//		The script command to execute
		// returns:
		//		The result entry from the execution of a single cycle
		
		// The config member is a String and is copied rather than referenced
		var clone = new tdi.tdicfg({config:tdicfg.config});
		
		// Get al object
		var al = clone.getAssemblyLine(assemblyline);
		
		// First set all connectors to disabled except the connector we are testing
		dojo.forEach(al.getConnectorNames(), function(conn) {
			var c = al.getConnector(conn);
			if(conn == connector)
				c.setState("Passive");
			else
				c.setState("Disabled");
		});
		
		// Start a temp instance
		//startTempConfig(tdicfg, )
		// Start the AL in manual mode
		// Execute command
	};
	
	tdiapi.createServerEventsListenerEntry = function() {
		// summary:
		//		Creates a server events listener entry on the server
		// returns:
		//		promise from dojo.xhrPost (eventually listener feed entry)
		var pd = {
			typeFilter: "di.*",
			"@type" : "diEventListener",
			channel : {
				"@type" : "pollChannel",
				waitTimeout : 2,
				batchCap : 10,
				fillBatch : true,
				onTimeoutGetAll : true
			}
		};
		
		return dojo.xhrPost({
			handleAs: this._format,
			url: this._url_prefix_rest + "/listener",
			headers : {
				"Content-Type" : "application/com.ibm.di.listener+json;type=diEventListener"
			},
			postData: dojo.toJson(pd)
		});
	};
	
	tdiapi.getServerEventsListenerEntry = function() {
		// summary:
		//		Returns the feed entry for the server events listener poll
		// returns:
		//		The promise from dojo.when
		return dojo.when(
			dojo.xhrGet({
				handleAs: this._format,
				preventCache: this._preventCache,
				url: this._url_prefix_rest + "/listener"
			}),
			function(data) {
				var feed = new tdi.tdifeed({feed:data});
				var poll = feed.getEntry("poll");
				if(poll == null) {
					return createServerEventsListenerEntry();
				} else {
					return poll;
				}
			}
		);
	};
	
	tdiapi.getServerEventsListenerUrl = function() {
		// summary:
		//		Returns the poll channel URL for the server events
		//		listener. If no such listener exists one is created.
		//
		return dojo.when(getServerEventsListenerEntry(),null,this.defaultErrHandler)
		.then(function(poll) {
			if(poll == null)
				return null;
			else if(poll.getLink("poll") != null)
				return poll.getLink("poll").href;
			else
				return null;
		});
	};
	
	tdiapi.defaultErrHandler = function(error) {
		alert("Error: " + error.message)
		var idx = dojo.getObject("idx.dialogs");
		idx.error(error.message);
//		var tdiutil = dojo.getObject("tdi.tdiutil");
//		tdiutil.error(error);
	};

	tdiapi.startConfigInstanceStatusPoller = function() {
//		if(_timer == null) {
//			_timer = new dojox.timing.Timer(5000);
//			_timer.onTick = function() {
//				dojo.when(getConfigInstances(), function(data) {
//					dojo.publish(tdiconstants.configInstanceSubject, [data]);
//				});
//			};
//			_timer.start();
			startServerEventNotifications();
//		}
	};
	
	tdiapi.stopConfigInstanceStatusPoller = function() {
//		if(_timer != null) {
//			_timer.stop();
//			_timer = null;
//		}
	};
	
	tdiapi.subscribeConfigStatus = function(pfunc) {
		// summary:
		//		Calls pfunc when status notification are received
		this.startConfigInstanceStatusPoller();
		return dojo.subscribe(tdiconstants.configInstanceSubject, pfunc);
	};

	tdiapi.unsubscribeConfigStatus = function(pfunc) {
		// summary:
		//		Removes pfunc from the CI status 
		dojo.unsubscribe(tdiconstants.configInstanceSubject, pfunc);
	};
		
	tdiapi.subscribeServerEvents = function(pfunc) {
		if(!this._eventsChannel) {
//			this._eventsChannel = setTimeout(function() {
//				delete tdiapi._eventsChannel;
//				tdiapi.startServerEventNotifications();
//			}, 2000);
			tdiapi.startServerEventNotifications();
		}
		return dojo.subscribe(tdiconstants.serverEventsSubject, pfunc);		
	};
	
	tdiapi.unsubscribeServerEvents = function(handle) {
		return dojo.unsubscribe(handle);		
	};
	
	tdiapi.startServerEventNotifications = function() {

		if(!this._eventsChannel && !this._eventsPending) {
			this._eventsPending = true;
			if(this._eventsChannelOld) {
				dojo.xhrDelete({url:this._eventsChannelOld.getLink("self").href});
				delete this._eventsChannelOld;
			}
			
			var pollChannel = {
			        "@type": "diEventListener",
			        typeFilter: "*",
			        idFilter: null,
			        channel: {
						"@type":"pollChannel",
						waitTimeout: tdiapi._waitTimeout,
						batchCap: tdiapi._batchCap,
			            fillBatch: true,
			            onTimeoutGetAll: true
			        }
			};
			var def = dojo.xhrPost({
				url: this._url_prefix_rest + "/listener",
				handleAs: "json",
				headers: {
					"Accept": "application/json",
					"Content-Type": "application/com.ibm.di.api.listener+json"
				},
				postData: dojo.toJson(pollChannel)
			})
			dojo.when(def, dojo.hitch(this, function(data) {
				this._eventsChannel = new tdi.tdiatom({atom:data});
				delete this._eventsPending;
				dojo.publish(tdiconstants.serverEventsSubject, [{type:"di.server.start"}]);
				this.getNextEventNotification();
			}), this.defaultErrHandler);
		}
	};
	
	tdiapi.stopServerEventNotifications = function() {
		// summary:
		//		Tries to release the current events channel. On failure we save the channel ref
		//		until we get a connection back and then make another attempt at removing it.
		if(this._eventsChannel) {
			this._eventsChannelOld = this._eventsChannel;
			dojo.when(dojo.xhrDelete({sync:true, url:this._eventsChannelOld.getLink("self").href}),
				dojo.hitch(this, function() {
						console.log("Events channel successfully deleted");
						delete this._eventsChannelOld;
				}),
				function(err) {
					console.log("While deleting events channel: " + err);
				}
			);
		}
		this._eventsChannel = null;
		// idx.showProgressDialog("Connection lost - waiting for server");
	};
	
	tdiapi.reconnectServer = function() {
		
		//
		// -- just let the user know we're disconnected
		//
		idx.error(tdiapi.nls.getString("FDS.serverConnLost"));
		return;
		
		
		/*
		if(!this._reconnectDialog) {
			this._reconnectDialog = new dijit.Dialog({
				title:tdiapi.nls.getString("Hooks.Reconnect"),
				content:tdiapi.nls.getString("ConfigSettingsEditor.TestConnection.failed", ["TDI Server"]) + "<p>" + nls.getString("ColumnDataFlow_connecting"),
				style:"width:300px"
			});
			//dojo.style(_reconnectDialog.closeButtonNode, "display", "none");
			this._reconnectDialog.show();
		}
		*/
		
		dojo.when(tdiapi.getServerInfo(), dojo.hitch(this, function() {
			/*
			if(this._reconnectDialog) {
				this._reconnectDialog.hide();
				this._reconnectDialog.destroy();
				this._reconnectDialog = null;
			}
			*/
			idx.hideProgressDialog();
			idx.info("Connection to server re-established");
			
			tdiapi.startServerEventNotifications();
		}), dojo.hitch(this, function(err) {
			setTimeout(lang.hitch(this, "reconnectServer"), 5000);
		}));
	};
	
	tdiapi.getNextEventNotification = function() {
		if(!this._eventsChannel)
			return;
		
		var link = this._eventsChannel.getLink("poll");
		dojo.when(dojo.xhrGet({
			url: link.href,
			preventCache: this._preventCache,
			handleAs:"json",
			headers: {
				"Accept": "application/com.ibm.di.api.listener+json"
			}
		}), dojo.hitch(this, function(eventdata) {
			var events = [];
			if(eventdata["@type"] == "batchEvent") {
				events = eventdata.event;
			} else {
				events = [eventdata];
			}
			for(var i = 0; i < events.length; i++) {
				var data = events[i];
				console.log(data);
				if(data && data.type == "di.server.stop") {
					this.stopServerEventNotifications();
				}
				try {
					dojo.publish(tdiconstants.serverEventsSubject, [data]);
				} catch(err) {
					console.log("PublishEvent error: " + err);
				}
			}
			if(this._eventsChannel) {
				// -- if we got a full buffer then there is most
				// -- likely more. otherwise, wait a sec before trying again
				if(events.length == tdiapi._batchCap) 
					this.getNextEventNotification();
				else
//					this.getNextEventNotification();
					setTimeout(lang.hitch(this, "getNextEventNotification"), 1000);
			} else {
				setTimeout(lang.hitch(this, "reconnectServer"), 5000);
			}
		}), dojo.hitch(this, function(err) {
			if(err && (err.status == "408" || err.status == "0")) {
				setTimeout(lang.hitch(this, "getNextEventNotification"), 1000);
//				this.getNextEventNotification();
			} else {
				console.log("ServerEvent failure: " + err);
				dojo.publish(tdiconstants.serverEventsSubject, [{type:"di.server.stop"}]);
				this.stopServerEventNotifications();
				setTimeout(lang.hitch(this, "reconnectServer"), 5000);
			}
		}));
	};
	
	tdiapi.listFiles = function(path) {
		return dojo.xhrGet({
			url: this._url_prefix + "/files" + (path ? "?path=" + path : ""),
			handleAs: tdiapi._format
		});
	};
	
	tdiapi.getTimeZone = function() {
		// summary:
		//		Returns the timezone id and offset from the server
		return dojo.xhrGet({
			url: this._url_prefix + "/server/timezone",
			handleAs: tdiapi._format
		});
	};
	
	return tdiapi;
});
