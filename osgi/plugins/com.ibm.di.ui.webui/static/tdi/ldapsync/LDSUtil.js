define([
    	"dojo/_base/declare",
    	"dojo/_base/lang",
    	"dojo/_base/array",
    	"dojo/Deferred",
    	"dojo/topic",
    	"tdi/tdiconstants",
    	"tdi/atom/tdicientry",
    	"tdi/tdiapi",
    	"tdi/tdiutil"
    ], function(declare, lang, array, Deferred, topic, tdiconstants, tdicientry, tdiapi, tdiutil) {

	var LDSUtil = {
		projectName: "LDAPSync",
		projectNameCustomTarget: "FDS_Target",
		writebackSchedule: "WriteBackMain KeepAlive",
		writebackAL: "WritebackMain",
		logSettingsConn: "Logging",
		generalSettingsConn: "GeneralSettings",
		writebackConn: "WriteBack",
		customTarget: null,
		
		ldapProps: {
			"source.ldap.url":"ldapUrl",
			"source.ldap.user":"ldapUsername",
			"source.ldap.password":"ldapPassword",
			"source.ldap.searchBase":"ldapSearchBase",
			"target.ldap.url":"ldapUrl",
			"target.ldap.user":"ldapUsername",
			"target.ldap.password":"ldapPassword",
			"target.ldap.searchBase":"ldapSearchBase"
		},
		
		customFlowSettings: {
			Form_AD: {
				source: {
				    "source.userObjectClass":"User",
				    "source.groupObjectClass":"Group",
				    "source.userRDN":"CN"
				}
			},
			Form_SUN: {
				source: {
				    "source.userObjectClass":"inetOrgPerson",
				    "source.groupObjectClass":"groupOfUniqueNames",
				    "source.userRDN":"UID"
				}
			},
			Form_TDS: {
				source: {
				    "source.userObjectClass":"inetOrgPerson",
				    "source.groupObjectClass":"groupOfUniqueNames",
				    "source.userRDN":"UID"
				}
			},
			Non_LDAP: {
				source: {
				    "source.containersToMigrate":""
				}
			}
		}
	};
	
	LDSUtil.applyCustomFlowSettings = function(source, target) {
		
		//
		// -- Settings for specific forms 
		//
		var type = source.getConnectionConfig().getParam("source.form");
		if(type && LDSUtil.customFlowSettings[type]) {
			var params = LDSUtil.customFlowSettings[type].source;
			for(var f in params) {
				target.getConnectionConfig().setParam(f, params[f]);
			}
		}
		
		//
		// -- Non-LDAP connectors
		//
		if(!source.getConnectionConfig().getParamBoolean("supportsPTA", true)) {
			var params = LDSUtil.customFlowSettings.Non_LDAP.source;
			for(var f in params) {
				target.getConnectionConfig().setParam(f, params[f]);
			}
		}
	};
	
	LDSUtil.mapProps2LDAP = function(conn) {
		var cfg = conn.getConnectionConfig();
		array.forEach(cfg.getNames(), function(f) {
			if(LDSUtil.ldapProps[f]) {
				cfg.setParam(LDSUtil.ldapProps[f], cfg.getParam(f))
			}
		});
	};

	
	LDSUtil.getLdapSyncLogFiles = function(logpath, detailed) {
		return dojo.xhrGet( {
			handleAs : "json",
			headers: {
				"Accept" : "application/json"
			},
			url : tdiapi._url_prefix + "/ldapsync/log" + "?logpath=" + logpath + "&detailed=" + (detailed ? true : false)
		});
	};
	
	LDSUtil.getLdapSyncLogFile = function(file) {
		return dojo.xhrGet( {
			handleAs : "text",
			headers: {
				"Accept" : "text/plain"
			},
			url : tdiapi._url_prefix + "/ldapsync/log/" + file
		});
	};
	
	LDSUtil.getLdapSyncSummary = function(flow, logpath) {
		return dojo.xhrGet( {
			handleAs : "text",
			headers: {
				"Accept" : "text/plain"
			},
			url : tdiapi._url_prefix + "/ldapsync/summary/" + flow + "?logpath=" + logpath
		});
	};
	
	LDSUtil.createConfig = function(name) {
		return dojo.xhrGet( {
			handleAs : "text",
			headers: {
				"Accept" : "text/plain"
			},
			url : tdiapi._url_prefix + "/ldapsync/createconfig?name=" + name
		});
	};
	
	LDSUtil.readLDAPSyncProps = function() {
		return LDSUtil.readPropertyStore(LDSUtil.projectName);
	};
	
	LDSUtil.readPropertyStore = function(config) {
		return this.startLdapSync(config).then(function(cientry) {
			LDSUtil.cientry = cientry;
			return tdiapi.getPropertyStoreValues(cientry, config);
		});
	};
	
	LDSUtil.startLdapSync = function(config) {
		var t = this;
		t.didStartSync = false;
		return dojo.when(tdiapi.getConfigEntry(config), function(centry) {
			t.configEntry = centry;
			return tdiapi.getConfigInstances();
		}, tdiapi.defaultErrHandler).then(function(data) {
			var cientry = array.filter(data.entry, function(e) {
				return (e.title.value == config);
			});

			var def = new Deferred();
			if(cientry.length == 1) {
				def.resolve(new tdicientry({atom:cientry[0]}));
			} else {
				t.didStartSync = true;
				tdiapi.startConfig(t.configEntry).then(function(data) {
					def.resolve(new tdicientry({atom:data}));
				});
			}
			return def.promise;
		});
	};
	
	LDSUtil.getConfigInstance = function(config) {
		var t = this;
		return dojo.when(tdiapi.getConfigEntry(config), function(centry) {
			t.configEntry = centry;
			return tdiapi.getConfigInstances();
		}, tdiapi.defaultErrHandler).then(function(data) {
			var cientry = array.filter(data.entry, function(e) {
				return (e.title.value == config);
			});

			var def = new Deferred();
			if(cientry.length == 1) {
				def.resolve(new tdicientry({atom:cientry[0]}));
			} else {
				def.resolve(null);
			}
			return def.promise;
		});
	};
	
	LDSUtil.startAssemblyLine = function(al, config, temp, tcb, sync) {
		if(temp) {
			return dojo.when(tdiapi.startTempConfig(config, temp), function(cientry) {
				return tdiapi.startAssemblyLine(cientry, al, sync, tcb);
			});
		} else {
			var cfg = typeof(config) == "string" ? config : config.getConfigName();
			return LDSUtil.startLdapSync(cfg).then(function(cientry) {
				return tdiapi.startAssemblyLine(cientry, al, sync, tcb);
			});
		}
	};
	
	LDSUtil.loadSyncEngineLabels = function() {
		// summary:
		//		Should be called early to update the labels (name) of the
		//		sync engine connectors list.
		array.forEach(LDSUtil.getSyncEngineConnectors(), function(item) {
			tdiapi.getConnectorForm("LDAPSync:Form_" + item.name).then(function(data) {
				item.name = tdiutil.getFormNLS(data, "name");
			});
		});
	};
	
	LDSUtil.getSyncEngineConnectors = function() {
		if(!LDSUtil.syncEngineConnectors) {
			var arr = [];
			array.forEach(tdiapi.getNamespace(LDSUtil.projectName).getConnectorNames(), function(conn) {
				var sp = /Form_(.*)/.exec(conn);
				if(sp && sp.length == 2) {
					arr.push({
						id:LDSUtil.projectName + ":/Connectors/" + conn,
						name:sp[1]
					})
				}
			});
			LDSUtil.syncEngineConnectors = arr;
		}
		return LDSUtil.syncEngineConnectors;
	};
	
	LDSUtil.getPTAServers = function() {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
			    "Accept": "application/json",
			},
			url : tdiapi._url_prefix + "/ldapsync/pta"
		});
	};
	
	LDSUtil.executePTAService = function(params)  {
		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
			    "Accept": "application/json",
			    "Content-Type": "application/json"
			},
			postData: dojo.toJson(params),
			url : tdiapi._url_prefix + "/ldapsync/pta"
		});
	};
	
	LDSUtil.getPTAServerEntry = function(suffix)  {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
			    "Accept": "application/json",
			},
			url : tdiapi._url_prefix + "/ldapsync/pta/" + suffix 
		});
	};
	

	LDSUtil.saveSnapshot = function(project, title)  {
		return dojo.xhrPut( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ldapsync/snapshot/" + project + "?title=" + title
		});
	};
	
	LDSUtil.loadSnapshot = function(project, path)  {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ldapsync/snapshot/" + project + "/restore?path=" + path
		});
	};
	
	LDSUtil.deleteSnapshot = function(path)  {
		return dojo.xhrDelete( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ldapsync/snapshot?path=" + path
		});
	};
	
	LDSUtil.listSnapshots = function(project)  {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			url : tdiapi._url_prefix + "/ldapsync/snapshot/"+project
		});
	};
	
	LDSUtil.restartConfig = function(config) {
		var t = this;
		return LDSUtil.getConfigInstance(config).then(function(data) {
			if(data) {
				return tdiapi.stopConfig(data).then(function() {
					return LDSUtil.startLdapSync(config);
				});
			}
		});
	};

	LDSUtil.pretty = function(str) {
		var arr = /(.*):\/(AssemblyLines|Connectors)\/(.*)/.exec(str);
		if(arr && arr.length == 4)
			return this.pretty(arr[3]) + (arr[1] ? " (" + arr[1] + ")" : "");
		
		arr = /(Source_|Target_|Flow_)(.*)/.exec(str);
		if(arr && arr.length == 3)
			return arr[2];
		
		return str;
	};


	LDSUtil.getServerMaps = function() {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept" : "application/json"
			},
			url : tdiapi._url_prefix + "/ldapsync/maps"
		});
	};
	
	LDSUtil.getServerMap = function(name) {
		return dojo.xhrGet( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Accept" : "application/json"
			},
			url : tdiapi._url_prefix + "/ldapsync/maps/" + name
		});
	};
	
	LDSUtil.deleteServerMap = function(name) {
		return dojo.xhrDelete({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			url: tdiapi._url_prefix + "/ldapsync/maps/" + name
		});
	};
	
	LDSUtil.saveServerMap = function(map) {
		return dojo.xhrPost( {
			handleAs : tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: {
				"Content-Type" : "application/json",
				"Accept" : "application/json"
			},
			postData: dojo.toJson(map),
			url : tdiapi._url_prefix + "/ldapsync/maps"
		});
	};
	
	LDSUtil.testConnection = function(conn, start, count, override, useCustom) {
		// description:
		//		Runs the TestConnection assemblyline with the parameters from "conn"
		//		Parameters in "override" override those found in conn.
		var assemblyline = "TestConnection"
		var config = "LDAPSync";
		
		//
		// -- check if custom target has its own TestConnection assemblyline
		//
		if(useCustom && LDSUtil.getTargetProjectName() != "LDAPSync") {
			var targetConfig = tdiapi.getNamespace(LDSUtil.getTargetProjectName());
			if(targetConfig && targetConfig.getAssemblyLine(assemblyline)) {
				config = LDSUtil.getTargetProjectName();
			}
		}
		
		var tcb = {
			"@type":"taskCallBlock",
			runtime: {
				initParam: {
				   "@type": "entry",
				   attribute: [
				   ]
				}
			}
		};
		
		var params = conn.getConnectionConfig().getNames();
		array.forEach(params, function(p) {
			var value = conn.getConnectionConfig().getParam(p);
			if(override && override[p] != undefined)
				value = override[p];
			tcb.runtime.initParam.attribute.push({
				"name": p,
				children: [
				    {
		               value: {
		                  value: value
		               }
		            }
		         ],
		         "protect": false
			});
		});
		
		if(conn.getParserType()) {
			var name = conn.getParserType();
			var ix = name.lastIndexOf("/");
			if(ix != -1) {
				name = name.substring(ix+1);
			}
			tcb.runtime.initParam.attribute.push({
				"name": "source.parser.name",
				children: [
				    {
		               value: {
		                  value: name
		               }
		            }
		         ],
		         "protect": false
			});
			
			var params = conn.getParserConfig().getConfig().getNames();
			array.forEach(params, function(p) {
				tcb.runtime.initParam.attribute.push({
					"name": "source.parser.parameter." + p,
					children: [
					    {
			               value: {
			                  value: conn.getParserConfig().getConfig().getParam(p)
			               }
			            }
			         ],
			         "protect": false
				});
			});
		}
		
		tcb.runtime.initParam.attribute.push({
			"name": "source.form",
			children: [
			    {
	               value: {
	                  value: conn.getConnectionConfig().getParam("source.form")
	               }
	            }
	         ],
	         "protect": false
		});
		
		tcb.runtime.initParam.attribute.push({
			"name": "testId",
			children: [
			    {
	               value: {
	                  value: conn.getConfigName() + "." + conn.getName()
	               }
	            }
	         ],
	         "protect": false
		});
		
		tcb.runtime.initParam.attribute.push({
			"name": "read.start",
			children: [
			    {
	               value: {
	                  value: typeof(start) == "undefined" ? 0 : start
	               }
	            }
	         ],
	         "protect": false
		});
		
		tcb.runtime.initParam.attribute.push({
			"name": "read.count",
			children: [
			    {
	               value: {
	                  value: typeof(count) == "undefined" ? 1 : count
	               }
	            }
	         ],
	         "protect": false
		});
		
		return LDSUtil.startAssemblyLine(assemblyline, config, false, tcb, false);
	};
	
	LDSUtil.testDirectoryServerConnection = function(config, override) {
		var conn = config.createConnector(LDSUtil.generalSettingsConn, "Iterator");
		var gs = config.getConnector(LDSUtil.generalSettingsConn);
		var retargetTarget = LDSUtil.getTargetProjectName() == "LDAPSync";
		
		conn.parentConfig = null;
		conn.setConnectorType("system:/Connectors/ibmdi.LDAP");
		conn.getParserConfig().setInheritFrom("[none]");
		
		array.forEach(gs.getConnectionConfig().getNames(), function(str) {
			var key = retargetTarget ? str.replace(/^target\./, "source.") : str;
			conn.getConnectionConfig().setParam(key, gs.getConnectionConfig().getParam(str));
		});
		
		if(retargetTarget)
			conn.getConnectionConfig().setParam("source.form", "Form_LDAP");
		
		conn.parentConfig = gs.parentConfig;
		return LDSUtil.testConnection(conn, 0, 0, override, true);
	};
	
	LDSUtil.updateTestConnectionSchema = function(conn, json) {
		conn.getSchema(true).removeAllItems();
		var entry = json.entries;
		if(lang.isArray(json.entries))
			entry = json.entries[0];
		for(var f in entry) {
			conn.getSchema(true).newItem({name:f});
		}
	};
	
	LDSUtil.saveCookies = function() {
		dojo.cookie("TDI.FDS.options", dojo.toJson(this.options));
		topic.publish("ldapsync/autosave", this.options);
	};
	
	LDSUtil.loadCookies = function() {
		this.options = {
			autoSave:true,
			autoUpdate:true
		};
		var json = dojo.cookie("TDI.FDS.options");
		if(json) {
			try {
				this.options = dojo.fromJson(json);
			} catch(err) {
				console.log("While parsing cookie: " + json);
				console.log(err);
			}
		}
	};
	
	LDSUtil.getOption = function(opt, defval) {
		if(!LDSUtil.options) {
			LDSUtil.loadCookies();
		}
		if(typeof(LDSUtil.options[opt]) == "undefined")
			return defval;
		else
			return LDSUtil.options[opt];
	};
	
	LDSUtil.setOptions = function(opt, value) {
		if(lang.isObject(opt)) {
			for(var f in opt) {
				LDSUtil.options[f] = opt[f];
			}
		} else {
			LDSUtil.options[opt] = value;
		}
		LDSUtil.saveCookies();
	};
	
	LDSUtil.getWriteBackConnector = function(config) {
		var writeBack = config.getConnector(LDSUtil.writebackConn);
		if(!writeBack) {
			writeBack = config.createDataFlowConnector(LDSUtil.writebackConn);
			writeBack.setConnectorType("system:/Connectors/ibmdi.ScriptConnector");
			writeBack.setState("Disabled");
		}
		return writeBack;
	};
	
	LDSUtil.getGeneralSettingsConnector = function(config) {
		var gs = config.getConnector(LDSUtil.generalSettingsConn);
		if(!gs) {
			gs = config.createLibraryConnector(LDSUtil.generalSettingsConn);
			gs.setConnectorType("LDAPSync:/Connectors/GeneralSettings");
		}
		return gs;
	};
	
	LDSUtil.setCustomTarget = function(target) {
		LDSUtil.customTarget = target;
	};
	
	LDSUtil.getCustomTarget = function() {
		return LDSUtil.customTarget;
	};
	
	LDSUtil.getTargetProjectName = function() {
		// summary:
		//		Returns the project name of the primary target configuration (LDAPSync default)
		return LDSUtil.customTarget ? LDSUtil.customTarget : LDSUtil.projectName;
	};
	
	LDSUtil.getTargetFlowConnector = function() {
		var config = "LDAPSync";
		if(LDSUtil.getTargetProjectName() != config) {
			var targetConfig = tdiapi.getNamespace(LDSUtil.getTargetProjectName());
			if(targetConfig && targetConfig.getConnector("FlowSettings")) {
				config = LDSUtil.getTargetProjectName();
			}
		}
		return config + ":/Connectors/FlowSettings";
	};
	
	LDSUtil.getSyncEngineMonitors = function() {
		if(!LDSUtil.syncEngineMonitors) {
			var arr = [];
			var names = tdiapi.getNamespace(LDSUtil.projectName).getConnectorNames().sort();
			array.forEach(names, function(conn) {
				var sp = /Monitor_(.*)/.exec(conn);
				if(sp && sp.length == 2) {
					arr.push({
						id:LDSUtil.projectName + ":/Connectors/" + conn,
						name:sp[1]
					})
				}
			});
			LDSUtil.syncEngineMonitors = arr;
		}
		return LDSUtil.syncEngineMonitors;
	};
	
	return LDSUtil;

});


