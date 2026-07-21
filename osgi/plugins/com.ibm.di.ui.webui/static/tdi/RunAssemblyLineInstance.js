/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/TitlePane",
	"dijit/Toolbar",
	"tdi/LogWidget",
	"tdi/tdiapi"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TitlePane, Toolbar, LogWidget, tdiapi) {
	return declare(
		[_Widget, _TemplatedMixin],
		{
	// summary:
	//		This widget shows a running assemblyline's log
	//		in a scrollable table. The widget also has a terminate button
	//		to stop the assemblyline.
		templatePath: dojo.moduleUrl("tdi", "templates/RunAssemblyLineInstance.html"),

		// cientry: tdi.tdicientry
		// 		Users must provide the cientry atom
		cientry: null,
		
		// assemblyline: String
		//		Users must provide the assemblyline name to run
		assemblyline: null,

		_openALLog : function(data) {
			this.alentry = new tdi.tdialentry({atom:data});
			dojo.when(this.alentry.getLinkData("listener"), function(data) {
				return new tdi.tdifeed({feed:data});
			}, tdiapi.defaultErrHandler)
			.then(dojo.hitch(this, function(feed) {
				var listener = feed.getEntry("listener");
				this._logWidget = new tdi.FilteredLogViewer({url:listener.getLink("poll").href}).placeAt(this.Log);
				this._logWidget.logReadTimeout = dojo.hitch(this, "checkALRunStatus");
			}), tdiapi.defaultErrHandler);
		},
		
		_openCILog : function(cientry) {
			return dojo.when(this.cientry.getLinkData("listener"), function(data) {
				return new tdi.tdifeed({feed:data});
			}, tdiapi.defaultErrHandler)
			.then(dojo.hitch(this, function(feed) {
				var listener = feed.getEntry("listener");
				this._logWidget = new tdi.FilteredLogViewer({url:listener.getLink("poll").href}).placeAt(this.Log);
				this._logWidget.logReadTimeout = dojo.hitch(this, "checkALRunStatus");
			}), tdiapi.defaultErrHandler);
		},
		
		openAssemblyLineLog : function(logfile) {
			this._logWidget = new tdi.FilteredLogViewer({hideFileButton:true}).placeAt(this.Log);
			//this._logWidget.openLogfile(config, alname, logfile);
		},
		
		_stop : function() {
			if(this.alentry != null) {
				dojo.when(tdiapi.stopAssemblyLine(this.alentry), null, dojo.hitch(this, function(err) {
					this.assemblylineStopped();
					if(this.cientry) {
						tdiapi.stopConfig(this.cientry);
						this.cientry = null;
					}
					if(this._logWidget) {
						this._logWidget.stop();
					}
				}));
			}
		},
		
		checkALRunStatus : function(err) {
			if(this.alentry) {
				dojo.when(this.alentry.getStatus(), dojo.hitch(this, function(data) {
					console.log("Got data");
				}), dojo.hitch(this, function(err) {
					// no longer running most likely
					if(this._logWidget) {
						this._logWidget.stop();
					}
					if(this.cientry) {
						tdiapi.stopConfig(this.cientry);
						this.cientry = null;
					}
					this.assemblylineStopped();
				}));
			}
		},
		
		assemblylineStopped : function() {
			// summary:
			//		Callback for AL stopped event
		},
		
		assemblylineStarted : function() {
			// summary:
			//		Callback for AL started event
		},
		
		stopAssemblyLine : function() {
			this._stop();
		},
		
		destroy : function() {
			if (this._logWidget != null) {
				this._logWidget.destroyRecursive();
			}
			if(this.cientry)
				tdiapi.stopConfig(this.cientry);
		},
		
		postCreate : function() {
			this.inherited("postCreate", arguments);
			if(this.cientry != null && this.assemblyline != null) {
				dojo.when(this._openCILog(this.cientry), dojo.hitch(this, function() {
					dojo.when(tdiapi.startAssemblyLine(this.cientry, this.assemblyline), dojo.hitch(this, function(data) {
						this.alentry = new tdi.tdialentry({atom:data});
						this.assemblylineStarted();
						this.getALLogFilename();
					}));
				}));
			}
		}
	})
});
