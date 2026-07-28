/*
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
 * @version     1.11, 12/19/11
 * @owner       
 * @history
 */

if (!dojo._hasResource["tdi.tdiapi"]) {
	dojo._hasResource["tdi.tdiapi"] = true;

	dojo.require("dojox.timing");
	dojo.require("dijit.Dialog");
	dojo.require("tdi.tdiatom");
	dojo.require("tdi.tdiconstants");
	dojo.require("tdi.tdiutil");
	dojo.require("tdi.NlsMixin");
	
	dojo.provide("tdi.tdiapi");
	
	tdiapi = {
		_debug : true,
		_format : "json",
		_url_prefix : "/dashboard",
		_format_xml : "xml",
		_url_prefix_rest : "/rest",
		_timer : null,
		_logPollTimeout : 3,
		_namespaces: {},
		_preventCache: true
	};
	
	tdiapi.getStdNamespaces = function() {
		dojo.when(dojo.xhrGet({
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers : {
				"Content-Type": "application/com.ibm.di.configuration+json"
			},
			url:"/rest/internal/system"
		}), dojo.hitch(tdiapi, function(data) {
			tdiapi._namespaces["system"] = new tdi.tdiconfig({config:data});
		}));
		dojo.when(dojo.xhrGet({
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers : {
				"Content-Type": "application/com.ibm.di.configuration+json"
			},
			url:"/rest/internal/adapter"
		}), dojo.hitch(tdiapi, function(data) {
			tdiapi._namespaces["adapter"] = new tdi.tdiconfig({config:data});
		}));
	};

	tdiapi.getNamespace = function(name) {
		if(!tdiapi._namespaces[name]) {
			dojo.xhrGet({
				url:"/rest/internal/" + name,
				handleAs: "json",
				preventCache: tdiapi._preventCache,
				sync: true, // this is not good but currently our only reasonable option
				headers: {
					"Accept":"application/com.ibm.di.configuration+json"
				},
				error: function(err) {
					console.log(err);
				},
				load: function(data) {
					tdiapi._namespaces[name] = new tdi.tdiconfig({config:data});
				}
			});
		}
		return tdiapi._namespaces[name];
	};
	
	tdiapi.getVMStatus = function() {
		// summary:
		// 		Requests the VMStatus object from the server
		// return:
		// 		The dojo.Deferred object from dojo.xhrDelete
		//
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
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
			return tdiapi.deleteLogAndTombstones();
		});
//		return dojo.xhrDelete( {
//			url : configentry.getLink("self").href
//		});
	};
	
	tdiapi.deleteConfigAndData = function(configid) {
		// summary:
		//		Deletes the tombstones, logfiles and configuration for configid
		//
		return dojo.xhrDelete({url:tdiapi._url_prefix + "/server/configdata/" + configid});
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
		            waitTimeout: tdiapi._logPollTimeout,
		            batchCap: 10,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
		
		var postData = dojo.toJson(pd);
			
		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			postData: postData,
			headers: {
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			url : tdiapi._url_prefix_rest + "/ci"
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
		            waitTimeout: tdiapi._logPollTimeout,
		            batchCap: 7,
		            fillBatch: true,
		            onTimeoutGetAll: true
		       }
			}
		};
		
		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			postData: dojo.toJson(pd),
			headers: {
				"Content-Type": "application/com.ibm.di.api.configuration+json"
			},
			url : tdiapi._url_prefix_rest + "/ci"
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix_rest + "/ci/" + configId 
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : cientry.getLink("self").href
		});
	};

	tdiapi.getInstalledComponents = function() {
		// summary:
		// 		Queries the server for installed configurations
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/json"
			},
			url : tdiapi._url_prefix_rest + "/server/info/comp"
		});
	};

	tdiapi.startAssemblyLine = function(cientry, assemblyLine, tcb) {
		// summary:
		// 		Starts an assemblyline instance in the CI specified by ciAtom
		// description:
		// 		Starts the assemblyline in the named configuration. If the
		// 		configuration is not running a new instance is started for the assemblyline to run in.
		// cientry: tdi.tdicientry
		//		The config instance entry atom for the instance
		// assemblyline: string
		//		The name of the assemblyline to start
		// tcb: TaskCallBlock
		//		The TCB provided to the started assemblyline
		// return:
		// 		The dojo.Deferred object from dojo.xhrPost
		//
		var pd = {
				name:assemblyLine,
				sync:false,
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
		if(tcb) {
			pd.tcb = tcb;
		}

		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			headers: {
				"Content-Type": "application/com.ibm.di.api.assembly-line+json"
			},
			postData: dojo.toJson(pd),
			url : cientry.getLink("assembly-line").href
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
				manual:true
		};
		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			headers: {
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

	tdiapi.executeALCycle = function(alentry) {
		// summary:
		// 		Sends an execute cycle request to the assemblyline.
		// description:
		// 		Sends an execute cycle request to the assemblyline.
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPut( {
			handleAs : tdiapi._format,
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : alentry.getLink("handle").href
		});
	};
	
	tdiapi.getConfigInstances = function() {
		// summary:
		//		Returns the list of running config instances (feed)
		// return:
		//		The dojo.Deferred from dojo.xhrGet
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			headers: {
				"Accept":"application/json"
			},
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix_rest + "/ci"
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
		return config.getLinkData("assembly-line");
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
			preventCache: tdiapi._preventCache,
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/log/" + config + "/" + alname + (date != null ? "?date=" + date : "")
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
			url = tdiapi._url_prefix + "/log/" + config + "/" + alname + "/" + logname;
		else
			url = tdiapi._url_prefix + "/log/" + logname;
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
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
			handleAs : tdiapi._format,
			headers: {
				"Content-Type" : "application/json+log",
				"Accept" : "application/json+log"
			},
			postData:dojo.toJson(options),
			url : tdiapi._url_prefix + "/log/search"
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
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/server/tombstonemanager"
		});
	};
	
	tdiapi.stopTombstoneManager = function() {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrDelete( {
			url : tdiapi._url_prefix + "/server/tombstonesmanager"
		});
	};
	
	tdiapi.startTombstoneManager = function() {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrPut( {
			url : tdiapi._url_prefix + "/server/tombstonesmanager"
		});
	};
	
	tdiapi.getTombstones = function(config, alname) {
		// summary:
		// 		Requests the tombstones for a config/al
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ts/" + config + "/" + alname
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/json"
			},
			url : tdiapi._url_prefix_rest + "/ts/ci/" + config + "/al/" + alname + "/ts"
		}), function(data) {
			var feed = new tdi.tdifeed({feed:data});
			var ts = feed.getEntries();
			if(ts != null && ts.length > 0) {
				return dojo.xhrGet({
					handleAs : tdiapi._format,
					preventCache: tdiapi._preventCache,
					url:ts[ts.length-1].content.src
				});
			} else {
				return null;
			}
		});
	};

	tdiapi.getComponentForm = function(url, lang) {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
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

	tdiapi.getConfigObjects = function(dir) {
		// summary:
		//		Returns a list of configuration objects in a subdirectory
		// dir: String
		//		The subdirectory (relative to TDI configs directory or absolute path)
		var def = dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/json"
			},
			url : tdiapi._url_prefix_rest + "/config/" + dir
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
			}, tdiapi.defaultErrHandler);
	};
	
	tdiapi.getServerProjects = function() {
		// summary:
		// 		Returns a list of projects/configs in the server's configs directory
		// description:
		// 		Returns a list of projects/configs in the server's configs directory
		// return:
		// 		A dojo.when promise after parsing the XML from the server
		//
		var def = dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/json"
			},
			url : tdiapi._url_prefix_rest + "/config"
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
			}, tdiapi.defaultErrHandler);
	};

	tdiapi.getServerStoreData = function(store) {
		// summary:
		// 		Returns the contents of a server store
		// description:
		// 		Returns the contents of a server store
		//
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ds/" + store
		});
	};
	
	tdiapi.getServerStores = function() {
		// summary:
		// 		Returns a list of server stores
		// description:
		// 		Returns a list of server stores
		//
		var def = dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ds"
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
			}, tdiapi.defaultErrHandler);
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
			handleAs : tdiapi._format,
			url : tdiapi._url_prefix + "?etl=get" + "&user=" + user
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
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/create/" + project
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
		var link = tdiapi.getAtomLink(ci, rel);
		if(link == null)
			return null;
		
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
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
		//		The tdi.tdiconfigentry object for the config
		// return:
		// 		The dojo.Deferred object from dojo.xhrGet
		//
		return dojo.when(tdiapi.checkOutConfig(configentry), function(config) {
			tdiapi.unlockConfig(configentry);
			return config;
		});
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
			handleAs : tdiapi._format,
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
		
		if(tdiapi._debug) {
			console.log("tdiapi.checkInConfig(" + configentry.getLink("lock").href + ", " + config + ")");
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/json"
			},
			url : tdiapi._url_prefix_rest + "/server"
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
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept":"application/com.ibm.di.api.server.info+json;type=serverInfo"
			},
			url : tdiapi._url_prefix_rest + "/server/info/content"
		});
//		return dojo.when(tdiapi.getServerFeed(),
//			function(data) {
//				var feed = new tdi.tdifeed({feed:data});
//				return dojo.when(feed.getEntry("info").getLinkData());
//			}, tdiapi.defaultErrHandler
//		);
	};
	
	tdiapi.getServerLogs = function() {
		// summary:
		//		Returns the last 100 lines of the ibmdi.log
		//		The position in the ibmdi.log is remembered between calls.
		return dojo.xhrGet({
			handleAs: "json",
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : tdiapi._url_prefix + "/log"
		});
	};
	
	tdiapi.getServerLog = function() {
		// summary:
		//		Returns the last 100 lines of the ibmdi.log
		//		The position in the ibmdi.log is remembered between calls.
		return dojo.xhrGet({
			handleAs: "text",
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept": "text/plain"
			},
			url : tdiapi._url_prefix + "/server/log"
		});
	};
	
	tdiapi.getLogSettings = function() {
		// summary:
		//		Returns the JSON object with the current
		//		global SystemAppender log settings.
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept": "application/json"
			},
			url : tdiapi._url_prefix + "/server/logging"
		});
	};
	
	tdiapi.saveLogSettings = function(settings) {
		// summary:
		//		Updates global SystemAppender log settings.
		//
		return dojo.xhrPut({
			handleAs: tdiapi._format,
			url : tdiapi._url_prefix + "/server/logging",
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
			handleAs: tdiapi._format,
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
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url: tdiapi._url_prefix + "/templates"
		});
	};
	
	tdiapi.createWebCeTemplate = function(templateId, solutionName) {
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url: tdiapi._url_prefix + "/templates/" + templateId + "/" + solutionName
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
					return dojo.xhrGet({url:store.href, handleAs:"json", preventCache: tdiapi._preventCache});
				else
					return null;
			});
	};
	
	tdiapi.getJavaProperty = function(property) {
		// summary:
		//		Returns the Java VM property
		return xhrGet({
			handleAs: "text",
			preventCache: tdiapi._preventCache,
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
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url:tdiapi._url_prefix + "/server/auth"
		});
	};
	
	tdiapi.setDashboardAuth = function(auth) {
		// summary:
		//		Updates the Dashboard security settings
		return dojo.xhrPut({
			handleAs: tdiapi._format,
			postData:dojo.toJson(auth),
			headers: {
				"Content-Type":"application/json"
			},
			url:tdiapi._url_prefix + "/server/auth"
		});
	};
	
	tdiapi.getActiveSchedules = function() {
		// summary:
		//		Returns the active assemblyline schedules for all config instances
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url:tdiapi._url_prefix + "/server/schedules"
		});
	};
	
	tdiapi.getChildAssemblyLines = function(alid) {
		// summary:
		//		Returns a list of child assemblylines for alid
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url:tdiapi._url_prefix + "/server/assemblyline/" + alid + "/children"
		});
	};
	
	tdiapi.getActiveThreads = function() {
		// summary:
		//		Returns the active threads in the TDI server's JVM
		return dojo.xhrGet({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url:tdiapi._url_prefix + "/server/threads"
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
		//tdiapi.startTempConfig(tdicfg, )
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
			handleAs: tdiapi._format,
			url: tdiapi._url_prefix_rest + "/listener",
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
				handleAs: tdiapi._format,
				preventCache: tdiapi._preventCache,
				headers: {
					"Accept":"application/json"
				},
				url: tdiapi._url_prefix_rest + "/listener"
			}),
			function(data) {
				var feed = new tdi.tdifeed({feed:data});
				var poll = feed.getEntry("poll");
				if(poll == null) {
					return tdiapi.createServerEventsListenerEntry();
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
		return dojo.when(tdiapi.getServerEventsListenerEntry(),null,tdiapi.defaultErrHandler)
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
		tdiutil.error(error);
	};

	tdiapi.startConfigInstanceStatusPoller = function() {
		if(tdiapi._timer == null) {
			tdiapi._timer = new dojox.timing.Timer(5000);
			tdiapi._timer.onTick = function() {
				dojo.when(tdiapi.getConfigInstances(), function(data) {
					dojo.publish(tdiconstants.configInstanceSubject, [data]);
				});
			};
			tdiapi._timer.start();
			tdiapi.startServerEventNotifications();
		}
	};
	
	tdiapi.stopConfigInstanceStatusPoller = function() {
		if(tdiapi._timer != null) {
			tdiapi._timer.stop();
			tdiapi._timer = null;
		}
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
		
	tdiapi.startServerEventNotifications = function() {
		if(!tdiapi._eventsChannel) {
			
			if(tdiapi._eventsChannelOld) {
				dojo.xhrDelete({url:tdiapi._eventsChannelOld.getLink("self").href});
				delete tdiapi._eventsChannelOld;
			}
			
			var pollChannel = {
			        "@type": "diEventListener",
			        typeFilter: "*",
			        idFilter: null,
			        channel: {
						"@type":"pollChannel",
						waitTimeout: 3,
						batchCap: 1,
			            fillBatch: false,
			            onTimeoutGetAll: true
			        }
			};
			var def = dojo.xhrPost({
				url:tdiapi._url_prefix_rest + "/listener",
				handleAs: "json",
				headers: {
					"Accept": "application/json",
					"Content-Type": "application/com.ibm.di.api.listener+json"
				},
				postData: dojo.toJson(pollChannel)
			})
			dojo.when(def, function(data) {
				tdiapi._eventsChannel = new tdi.tdiatom({atom:data});
				dojo.publish(tdiconstants.serverEventsSubject, [{type:"di.server.start"}]);
				tdiapi.getNextEventNotification();
			}, tdiapi.defaultErrHandler);
		}
	};
	
	tdiapi.stopServerEventNotifications = function() {
		// summary:
		//		Tries to release the current events channel. On failure we save the channel ref
		//		until we get a connection back and then make another attempt at removing it.
		if(tdiapi._eventsChannel) {
			tdiapi._eventsChannelOld = tdiapi._eventsChannel;
			dojo.when(dojo.xhrDelete({sync:true, url:tdiapi._eventsChannelOld.getLink("self").href}),
				function() {
						console.log("Events channel successfully deleted");
						delete tdiapi._eventsChannelOld;
				},
				function(err) {
					console.log("While deleting events channel: " + err);
				}
			);
		}
		tdiapi._eventsChannel = null;
	};
	
	tdiapi.reconnectServer = function() {
		
		var nls = new tdi.NlsMixin();
		tdiutil.error(nls.getString("FDS.serverConnLost"));
		
		/*
		if(!tdiapi._reconnectDialog) {
			var nls = new tdi.NlsMixin();
			tdiapi._reconnectDialog = new dijit.Dialog({
				title:nls.getString("Hooks.Reconnect"),
				content:nls.getString("ConfigSettingsEditor.TestConnection.failed", ["TDI Server"]) + "<p>" + nls.getString("ColumnDataFlow_connecting"),
				style:"width:300px"
			});
			//dojo.style(tdiapi._reconnectDialog.closeButtonNode, "display", "none");
			tdiapi._reconnectDialog.show();
		}
		
		dojo.when(tdiapi.getServerInfo(), function() {
			if(tdiapi._reconnectDialog) {
				tdiapi._reconnectDialog.hide();
				tdiapi._reconnectDialog.destroy();
				tdiapi._reconnectDialog = null;
			}
			tdiapi.startServerEventNotifications();
		}, function(err) {
			setTimeout(tdiapi.reconnectServer, 1000);
		});
		*/
	};
	
	tdiapi.getNextEventNotification = function() {
		var link = tdiapi._eventsChannel.getLink("poll");
		dojo.when(dojo.xhrGet({
			url: link.href,
			handleAs:"json",
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept": "application/com.ibm.di.api.listener+json"
			}
		}), function(data) {
			console.log("ServerEvent: " + dojo.toJson(data));
			if(data && data.type == "di.server.stop") {
				tdiapi.stopServerEventNotifications();
			}
			try {
				dojo.publish(tdiconstants.serverEventsSubject, [data]);
			} catch(err) {
				console.log("PublishEvent error: " + err);
			}
			if(tdiapi._eventsChannel)
				tdiapi.getNextEventNotification();
			else
				tdiapi.reconnectServer();
		}, function(err) {
			if(err && (err.status == "408" || err.status == "0")) {
				tdiapi.getNextEventNotification();
			} else {
				console.log("ServerEvent failure: " + dojo.toJson(err));
				tdiapi.stopServerEventNotifications();
				tdiapi.reconnectServer();
			}
		});
	};
}
