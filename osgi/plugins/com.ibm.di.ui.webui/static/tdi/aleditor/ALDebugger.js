/**
 * The ALDebugger starts a debug session with an AssemblyLine and provides
 * functions to control and query the assemblyline.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/tdiconfig",
	"tdi/tdiapi",
	"tdi/atom/tdialentry",
	"tdi/atom/tdicientry",
	"tdi/tdiutil"
], function(declare, array, tdiconfig, tdiapi, tdialentry, tdicientry, tdiutil) {
	
return declare(
	null,
	{
		// config: tdiconfig
		//	summary:
		//		The solution config
		config: null,
		
		// assemblyline: String
		//	summary:
		//		The assemblyline name
		assemblyline: null,
		
		// cientry: tdicientry
		//	summary:
		//		The config instance entry
		cientry: null,
		
		// alentry: tdialentry
		//	summary:
		//		The assemblyline instance entry
		alentry: null,
		
		// breakOnComponents: boolean
		//	summary:
		//		If true the debugger will set a breakpoint on all components
		breakOnComponents: false,
		
		// status: Object
		//	summary:
		//		Last known status
		status: null,
		
		// true if we sent a request to set breakpoints
		breakpointsSet: false,
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
		
		_onDebugInit: function(data) {
			this.alentry = new tdialentry({atom:data})
			this.checkStatus();
			this.onDebugInit(this.alentry);
		},
		
		onDebugInit: function() {
		},
		
		getStatus: function() {
			return this.alentry.getLinkData("debug");
		},			
		
		checkStatus: function(data) {
			if(!data) {
				dojo.when(this.alentry.getLinkData("debug"), dojo.hitch(this, "checkStatus"), dojo.hitch(this, function(err) {
					//tdiutil.error(err);
					this._onDebugTerminate(err);
				}));
			} else {
				this.status = data;
				if(data.status == "waiting") {
					this._onDebugBreak(data);
				} else if(data.status == "idle") {
					this._onDebugTerminate(data);
				} else {
					// poll status until we get a breakpoint
					setTimeout(dojo.hitch(this, "checkStatus"), 10);
				}
			}
		},
		
		updateComponentBreakpoints: function() {
			var al = this.config.getAssemblyLine(this.assemblyline);
			var names = new Array();
			var allNames = al.getComponentNames(true);
			dojo.forEach(allNames, dojo.hitch(this, function(name) {
				var conn = al.getComponentByName(name);
				if(conn.isConnector())
					names.push(name + ".default_ok");
				else
					names.push(name);
			}));
			
			var watch = new Array();
			dojo.forEach(allNames, dojo.hitch(this, function(name) {
				var conn = al.getComponentByName(name);
				if(conn.isConnector())
					watch.push(name + ".lastConn");
			}));
			watch.push("work");
			
			if(!this.breakpointsSet) {
				dojo.when(dojo.xhrGet({
					url: this.alentry.getLink("debug").href + "/watch?param=" + watch.join(",")
				}), dojo.hitch(this, function(result) {
					this.breakpointsSet = true;
					this.runContinue();
				}), function(err) {
					tdiutil.error(err);
				});
				dojo.when(dojo.xhrGet({
					url: this.alentry.getLink("debug").href + "/breakat?param=" + names.join(",")
				}), dojo.hitch(this, function(result) {
					this.breakpointsSet = true;
					this.runContinue();
				}), function(err) {
					tdiutil.error(err);
				});
			} else {
				// still waiting for first breakpoint
				this.checkStatus();
			}
		},
		
		_onDebugTerminate: function(status) {
			alert("Assemblyline has terminated");
		},
		
		_onDebugBreak: function(status) {
			if(status.status == "waiting") {
				if(status.breakpoint == " INIT " && this.breakOnComponents) {
					this.updateComponentBreakpoints();
				} else {
					var arr = status.breakpoint.split("\.");
					if(arr.length > 1) {
						status.component = arr[0];
					} else {
						status.component = status.breakpoint;
					}
					this.onDebugBreak(status);
				}
			}
		},
		
		onDebugBreak: function(status) {
			console.log("Debug break at: " + status.breakpoint);
		},		
		
		onConfigStarted: function(cientry) {
		},
		
		startDebugger: function(data) {
			if(data) {
				this.cientry = new tdicientry({atom:data});
				this.onConfigStarted(this.cientry)
				var alc = this.config.getAssemblyLine(this.assemblyline);
				dojo.when(tdiapi.stepAssemblyLine(this.cientry, alc.getName()), dojo.hitch(this, "_onDebugInit"), function(err) {
					tdiutil.error(err);
				});
			} else {
				dojo.when(tdiapi.startTempConfig(this.config, tdiutil.generateInstanceId(this.config, "TDIDashboard_TEMP")), 
						dojo.hitch(this, "startDebugger"));
			}
		},
		
		stopDebugger: function() {
			if(this.alentry) {
				tdiapi.stopAssemblyLine(this.alentry);
				this.alentry = null;
			}
			if(this.cientry) {
				tdiapi.stopConfig(this.cientry);
				this.cientry = null;
			}
			this.breakpointsSet = false;
		},
		
		runContinue: function() {
			dojo.when(dojo.xhrGet({
				url:this.alentry.getLink("debug").href + "/continue",
				headers: [{"Accept":"application/json"}],
				handleAs: "json"
			}), dojo.hitch(this, "checkStatus"), function(err) {
				tdiutil.error(err);
			});
		},
		
		runUntil: function(component) {
			dojo.when(dojo.xhrGet({
				url:this.alentry.getLink("debug").href + "/rununtil?param=" + component,
				headers: [{"Accept":"application/json"}],
				handleAs: "json"
			}), dojo.hitch(this, "checkStatus"), function(err) {
				tdiutil.error(err);
			});
		},
		
		executeScript: function(script) {
			return tdiapi.executeScript(this.alentry, script);
		}
	}
);
});
