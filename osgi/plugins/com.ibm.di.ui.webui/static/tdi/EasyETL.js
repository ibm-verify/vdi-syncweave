dojo.provide("tdi.EasyETL");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Toolbar");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.Toolbar");
dojo.require("dijit.TooltipDialog");

dojo.require("dojo.dnd.Source");
dojo.require("dojox.timing")
dojo.require("dojo.data.ItemFileReadStore")
dojo.require("dojox.gfx");
dojo.require("dojox.gfx.move");
dojo.require("dojox.gfx.utils");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.AttributeMapGFX3");
dojo.require("tdi.FormWidget");
dojo.require("tdi.AttributeMapItemEditor");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdisession");
dojo.require("tdi.tdiutil");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.EasyETL",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// summary:
		//		This widget shows an EasyETL project assemblyline and lets the user
		//		edit, run and save the configuration.
		//
	
	
		// Template variables
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/EasyETL.html"),

		// summary:
		// 		Config entry (atom) of the configuration to edit.
		// tags:
		//		Input
		configentry: null,
		
		// summary:
		// 		Assemblyline name to edit
		// tags:
		//		Input
		// 
		assemblylineName: null,
		
		// Stepper handle
		handle : null,
		
		// Editing config
		cfg: null,
		
		// CI started by us?
		cistarted: true,
		
		// Sorting input attrs to target map
		targetMapAlign: false,
		
		// Set when Run is used to keep cycling
		_autorun :  false,
		
		// Set when asynch functions should stop processing
		_terminatePending :  false,

		
		toggleSchema : function() {
			// summary:
			//		Toggles display of unmapped attributes
			// 
			this.Transformation.toggleSchema();
		},
		
		toggleSort : function() {
			// summary:
			//		Toggles the sorting order of the attribute maps
			// 
			var newval = this.SortMap.get("value");
			if(newval == this.targetMapAlign)
				return;
			this.targetMapAlign = newval;
		},
		
		startAL : function(data) {
			// summary:
			//		Start the config instance, then the assemblyline and finally calls getnext
			//
			// tags:
			//		callback
			//
			if(this.configentry == null) {
				return;
			}
			
			if(data != null) {
				this.cientry = tdi.tdicientry({atom:data});
				this.cistarted = true;
			}
			
			if(this.cientry == null) {
				//dojo.when(tdiapi.startConfig(this.configentry), dojo.hitch(this, "startAL"), dojo.hitch(this, "_sessionError")); 
				if(!tdiutil.isConnectorConfigured(this.input)) {
					tdiutil.alert(this.getString("connectorNotConfigured", [this.input.getName]));
				} else {
					dojo.when(this._startConfigInstance(), dojo.hitch(this, "startAL"), dojo.hitch(this, "_sessionError")); 
				}
			} else {
				dojo.when(tdiapi.stepAssemblyLine(this.cientry, this.assemblylineName), dojo.hitch(this, "alStarted"), dojo.hitch(this, "_sessionError"));
			}
		},
		
		terminateAL : function() {
			// summary:
			// 		terminates a running assemblyline
			// tags:
			//		public
			if(this.alentry != null) {
				// since we may be in a wait for the next entry
				// we have to make sure the timer in getNextEntry completes 
				// before we stop the config and al, so as to avoid restarting the config al.
				this._terminatePending = true;
				this.btnStop.attr("disabled", true);
				this.btnRead.attr("disabled", true);
				this.btnRun.attr("disabled", true);
				setTimeout(dojo.hitch(this, function() {
					this._stopConfigAndAssemblyline();
					this._terminatePending = false;
				}), 500);
			}
		},
		
		alStarted : function(data) {
			// summary:
			//		Called when the AL has started
			// data:
			//		The ATOM entry for the assemblyline (from rest call)		
			// tags:
			//		callback
			this.btnStop.attr("disabled", false);
			this.alentry = new tdi.tdialentry({atom:data});
			this.getNextEntry();
		},
		
		alStopped : function() {
			// summary:
			//		Called internally when the AL has stopped
			//
			this.btnStop.attr("disabled", true);
			this.btnRead.attr("disabled", false);
			this.btnRun.attr("disabled", false);
			this.btnRun.attr("label", "Run");
			this._autorun = false;
			this.alentry = null;
			this.Transformation.setInputOutputEntry(null, null);
			this._stopConfigAndAssemblyline();
		},
		
		readNextEntry : function() {
			// summary:
			//		called when user presses Read/Write
			// tags:
			//		public
			this.btnRead.attr("disabled", true);
			this.getNextEntry(null);
		},
		
		autoGetNextEntry : function() {
			// summary:
			//		called from template button
			// tags:
			//		private
			if(this._autorun) {
				this._autorun = false;
				this.btnRun.attr("label", "Run");
			} else {
				this.btnRun.attr("label", "Pause");
				this._autorun = true;
				this.readNextEntry();
			}
		},
		
		getNextEntry : function(data) {
			// summary:
			//		Requests the next entry from the assemblyline and updates the display
			//		with the values received.
			//
			// description:
			//		This method will do several things.
			//		1. Start the config instance if it's not running
			//		2. Start the assemblyline if it's not running
			//		3. Execute a next-cycle request on the assemblyline's handle
			//		4. Execute a get data request on the assemblyline's handle
			//
			// 		When the next-cycle request returns, it either contains the next entry read
			//		or it has a single state=processing field, which means we have to do a read on
			//		the handle to get the data.
			//
			//		When state=processing we do a read on the al's handle to get the next entry. If this
			//		returns with a state=processing we wait 500ms and reissue the get request. This is done
			//		until we receive something from the assemblyline.
			// tags:
			//		private
			
			// Terminate has been called ... cease all activities
			if(this._terminatePending)
				return;
			
			// make sure the al is running
			if(this.cientry == null || this.alentry == null) {
				this.startAL();
				return;
			}
			
			if(data == null) {
				// Issue a cycle request
				dojo.when(tdiapi.executeALCycle(this.alentry), dojo.hitch(this, "getNextEntry"), dojo.hitch(this, "_sessionError"));
				
			} else if(data == "get") {
				// Send a script to get the last input
				dojo.when(tdiapi.executeScript(this.alentry, this.input.getName() + ".lastConn")).then(dojo.hitch(this, function(entry) {
					this.lastInputEntry = new tdi.tdientry({data:entry});
					if(tdiutil.isConnectorConfigured(this.output))
						return tdiapi.executeScript(this.alentry, this.output.getName() + ".lastConn");
					else
						return null;
				})).then( dojo.hitch(this, function(entry) {
					if(entry != null)
						this.lastOutputEntry = new tdi.tdientry({data:entry});
					else
						this.lastOutputEntry = null;
					dojo.when(tdiapi.getNextEntry(this.alentry), dojo.hitch(this, "getNextEntry"), dojo.hitch(this, "_sessionError"));
				}));
				
			} else {
				// Got data from cycle or next request
				if(data.state == "processing") {
					// wait 500ms and try another getNextentry
					setTimeout(dojo.hitch(this,"getNextEntry", "get"), 500);
				} else if(data.state == "done") {
					// got a real result entry here
					if(data.resultEntry == undefined) {
						this.alStopped();
					} else {
						// Update display with entry values
						this.Transformation.setInputOutputEntry(this.lastInputEntry, this.lastOutputEntry);
						// auto run?
						if(this._autorun) {
							this.getNextEntry();
						} else {
							this.btnRead.attr("disabled", false);
						}
					}
				} else {
					// AL terminated/closed
					this.alStopped();
				}
			}
		},
		
		_checkinConfig : function() {
			dojo.when(tdiapi.checkInConfig(this.configentry, this.config.config.solution), dojo.hitch(this, function() {
				tdiapi.unlockConfig(this.configentry);
				this.config.setModified(false);
				tdiutil.alert(this.getString("configSaved"));
			}), dojo.hitch(this, "_sessionError"));
		},
		
		setScheduleDialog : function() {
			var schedule = new tdi.ALSchedule({});
			this._supportingWidgets.push(schedule);
			var config = this.config.getAssemblyLine(this.assemblylineName);
			schedule.setConfig(config);
			try {
				var dlg = new dijit.TooltipDialog({
					content:schedule
				});
				this.btnSchedule.dropDown = dlg;
			} catch(err) {
				tdiutil.error(err);
			}
			schedule.startup();
		},
		
		saveConfig : function() {
			// summary:
			//		Checks in the current configuration. This method is called by 
			//		the Save button in the template.
			// tags:
			//		private
			
			try {
				this.config.getTombstoneSettings().setParam("AssemblyLines", "true");
				this.config.getTombstoneSettings().setParam("Configuration", "true");
			} catch(err) {
				console.log("While enabling tombstones for configuration: " + err);
			}
			
			// disable output connector since it is not configured
			this.updateOutputState();
			
			dojo.when(tdiapi.checkOutConfig(this.configentry), dojo.hitch(this, "_checkinConfig"), dojo.hitch(this, "_sessionError"));
		},
		
		resize : function(a) {
			// summary:
			//		This is called by parent widgets to do custom resizing
			//		of the widget. Call inherited version and then resize
			//		the BorderContainer to match the new size.
			this.inherited(arguments);
			if(a.h > 0) {
				var pos = dojo.position(this.domNode);
				var hdr = dojo.position(this.HeaderTable);
				var h = pos.h - hdr.h;
				this.BorderContainer.resize({h:h, w:a.w});
			}
		},
		
		queryConnector : function(conn, input) {
			this._session = new tdi.tdisession({});
			this._discoverButton.set("disabled", true);
			dojo.when(this._session.openSessionForConnector(conn),
					function() {
					},
					dojo.hitch(this, "_sessionError")
			).then(dojo.hitch(this, function() {
				return this._session.getNextEntry();
			})).then(dojo.hitch(this, function(entry) {
				this._session.close();
				this._session = null;
				var e = new tdi.tdientry({data:entry});
				var names = e.getNames();
				var schema = conn.getSchema(input);
				schema.removeAllItems();
				dojo.forEach(names, dojo.hitch(this, function(attr) {
					if(schema.getItem(attr) == null) {
						schema.newItem({name:attr});
					}
				}));
				this._discoverButton.set("disabled", false);
				this.Transformation.showSchema = true;
				this.Transformation.removeAllItems(input);
				this.Transformation.setConfig({input:this.input, output:this.output});
			}));
		},
		
		queryInput : function() {
			this.queryConnector(this.input, true);
		},
		
		queryOutput : function() {
			this.queryConnector(this.output, false);
		},
		
		_sessionError : function(error) {
			// summary:
			//		Close the session and assemblyline and display error message
			if(this._session != null) {
				this._session.close();
				this._session = null;
			}
			this._stopConfigAndAssemblyline();
			tdiutil.error(error);
		},
		
		_startConfigInstance : function() {
			// summary:
			//		Checks the configuration if it has configured the connectors
			//		and disables the output connector if it is not configured.
			//		If the input connector is not configured then we abort the
			//		launch sequence.
			//		If the config has been changed we launch a temporary config.
			//
			// return:
			//		The deferred object for a config instance launch
			//
			
			//
			var state = null;
			
			// disable output connector since it is not configured
			if(!tdiutil.isConnectorConfigured(this.output)) {
				state = this.output.getState();
				if(state !== "Disabled") {
					// set new state without notifying the modification
					this.output.setObject("state", "Disabled", null, false);
				}
			}
			
			// make sure we have something in the input map
			if(this.input.getAttributeMap(true).getNames().length == 0) {
				this.config.getAssemblyLine(this.assemblylineName).setAutomapAttributes(true);
			}
			
			var deferred = tdiapi.startTempConfig(this.config, tdiutil.generateInstanceId(this.config, "TDIDashboard_TEMP"));
			
			// reset value without notifying the modification
			if(state !== null && state !== "Disabled") {
				this.output.setObject("state", state, null, false);
			}
			
			return deferred;
		},
		
		_stopConfigAndAssemblyline : function() {
			// if we started the config then we stop it
			if(this.cientry != null && this.cistarted) {
				dojo.when(tdiapi.stopConfig(this.cientry), dojo.hitch(this, function() {
					this.cientry = null;
					this.cistarted = false;
					this.alentry = null;
					this.alStopped();
				}));
			} else if(this.alentry != null)	{
			// or, if we started the al stop that one
				dojo.when(tdiapi.stopAssemblyLine(this.alentry), dojo.hitch(this, function() {
					this.alentry = null;
					this.alStopped();
				}));
			}
			if(this._discoverButton)
				this._discoverButton.set("disabled", false);
		},
		
		_updateButtonStates : function() {
			this.btnRead.attr("disabled", !tdiutil.isConnectorConfigured(this.input));
			this.btnRun.attr("disabled", !tdiutil.isConnectorConfigured(this.input));
		},
		
		postCreate : function() {
			// Initialize variables and create component widgets.
			// Request the CI entry for the config.
			this.inherited(arguments);
			
			var al = this.config.getAssemblyLine(this.assemblylineName);
			this.input = al.getConnector("Input");
			this.output = al.getConnector("Output");
			
			// Set the schedule drop down
			this.setScheduleDialog();
			
			// Add a handler to enable the Save button when config changes
			this._onModifyHandle = dojo.connect(this.config, "onModify", dojo.hitch(this, function(modified) {
				this._updateButtonStates();
			}));
		},
		
		closeConnectorConfig : function(input) {
			this.Transformation.resetEditorComponent(input ? this.leftForm : this.rightForm, input);
		},
		
		showDatabrowser : function(input) {
			var db = new tdi.Databrowser({
				useSimpleConfig: true,
				config:(input ? this.input : this.output),
				_saveTarget: dojo.hitch(this, function() {
					this.stack.removeChild(this.databrowser);
					this.databrowser.destroyRecursive();
					this.databrowser = null;
					this.stack.selectChild(this.BorderContainer);
					this.Transformation.repaint();
					this.updateOutputState();
				})
			});
			this.stack.addChild(db);
			this.stack.selectChild(db);
			this.databrowser = db;
		},
		
		showConnectorConfig : function(input) {
			if(this.showDatabrowser) {
				this.showDatabrowser(input);
				return;
			}
			
			var config = input ? this.input : this.output;
			var options = {
					ShowConfig:true,
					ShowAttMap:false
				};
			
			var button = {
				label:this.getString("close"),
				onClick:dojo.hitch(this, "closeConnectorConfig", input)
			};

			var comp = new tdi.ConnectorEditor({
				config:config,
				options:options,
				visibleButtons:[false,true,false,true],
				toolbarButtons:[button],
				hideNullValues:false
			});
			
			dojo.style(comp.domNode, "overflow", "scroll");
			
			this.Transformation.setEditorComponent(comp, input);
			if(input)
				this.leftForm = comp;
			else
				this.rightForm = comp;
		},
		
		startup : function() {
			this.inherited("startup", arguments);
			this.Transformation.setConfig({input:this.input, output:this.output});
			
			var configure = new dijit.form.Button({
				label:this.getString("ConnectorFlowWidget_configure"),
				onClick:dojo.hitch(this, "showConnectorConfig", true)
			});
			this.Transformation.addToToolbar("configure", configure, true);
			
			this._discoverButton = new dijit.form.Button({
				label:this.getString("discover"),
				onClick:dojo.hitch(this, "queryInput")
			});
			this.Transformation.addToToolbar("discover", this._discoverButton, true);
			
			var configure2 = new dijit.form.Button({
				label:this.getString("ConnectorFlowWidget_configure"),
				onClick:dojo.hitch(this, "showConnectorConfig", false)
			});
			this.Transformation.addToToolbar("configure2", configure2, false);
			this.Transformation.addToToolbar("discover2", new dijit.form.Button({
				label:this.getString("discover"),
				onClick:dojo.hitch(this, "queryOutput")
			}), false);
			
		},

		updateOutputState : function() {
			// disable output connector since it is not configured
			if(!tdiutil.isConnectorConfigured(this.output)) {
				// set new state without notifying the modification
				this.output.setObject("state", "Disabled", null, false);
			} else {
				this.output.setObject("state", "Enabled", null, false);
			}
		},
		
		destroy : function() {
			// Unlock the config and stop any instances we started
			//
			try {
				tdiapi.unlockConfig(this.configentry);
			} catch(ignore) {}
			try {
				this._stopConfigAndAssemblyline();
			} catch(ignore) {}
			if(this._discoverButton)
				this._discoverButton.destroy();
			
			if(this._onModifyHandle)
				dojo.disconnect(this._onModifyHandle);
			
			this.inherited(arguments);
		}
	}
);
