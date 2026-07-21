dojo.provide("tdi.ALSchedule");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.TitlePane");
dojo.require("dijit.form.Button")
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Form");
dojo.require("dijit.form.RadioButton");
dojo.require("dojox.widget.Portlet");
dojo.require("dojox.layout.TableContainer");

dojo.require("dojo.date.locale");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.ALSchedule",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
	// summary:
	//		A simple editor for a Schedule configuration
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templatePath : dojo.moduleUrl("tdi", "templates/ALSchedule.html"),

		// config: tdi.basecfg
		//		The schedule configuration
		config: null,
		
		setConfig : function(config) {
			this.config = config;
			this._getOrCreateSchedule(false);
			
			var vals = {
				enabled: "on",
				noduplicates: "on",
				months: "*",
				days: "*",
				hour: "*",
				minute: "0",
				second: "0"
			};
			
			if(this._connectorform) {
				this._connectorform.destroyRecursive();
				this._connectorform = null;
			}
			
			if(this.schedule != null) {
				var s = this.schedule;
				var weekday = s.getTimePattern("wday");
				var day = s.getTimePattern("mday");
				var days = "";
				if(weekday == "*" && day == "*")
					days = "*";
				else if(weekday != "*")
					days = "w";
				else
					days = "d";
				
				var month = s.getTimePattern("month");
				var months = "*";
				if(month != "*")
					months = "m";
				
				vals = {
					enabled: s.getEnabled() ? "on" : "off",
					noduplicates: s.getSkipExecIfAlRunning() ? "on" : "off",
					cancelonfail: s.getCancelScheduleOnAlFailure() ? "on" : "off",
					months: months,
					month: s.getTimePattern("month"),
					days: days,
					day: day,
					weekday: weekday,
					hour: s.getTimePattern("hour"),
					minute: s.getTimePattern("minute"),
					second: s.getTimePattern("second")
				};
				if(vals.month != null)
					vals.month = vals.month.split(",");
				if(vals.day != "*")
					vals.day = vals.month.split(",");
				this._noUpdate = true;
				this.Form.setValues(vals);
				// reset after 50ms to give controls time to callback
				setTimeout(dojo.hitch(this, function() {
					this._noUpdate = false;
				}), 50);
				this._showHideSections();
				this._createALParamsForm();
			}
			
			dojo.style(this.Empty, "display", this.schedule != null ? "none" : "");
			dojo.style(this.Form.domNode, "display", this.schedule != null ? "" : "none");
		},
		
		_deleteSchedule : function() {
			tdiutil.confirm(this.getString("deleteSchedule"), dojo.hitch(this, function(buttonId, messageId, checked) {
				if(buttonId == 0) {
					var cfg = this.config.getParent();
					var name = cfg.getScheduleForAssemblyLine(this.config.getName());
					cfg.deleteSchedule(name);
					this.schedule = null;
					this.setConfig(this.config);
				}
			}));
		},
		
		_createSchedule : function() {
			this._getOrCreateSchedule(true);
			this.setConfig(this.config);
		},
		
		_getOrCreateSchedule : function(create) {
			if(this.schedule != null)
				return;
			var cfg = this.config.getParent();
			var name = this.config.getName();
			var schedule = cfg.getScheduleForAssemblyLine(name);
			if(schedule != null) {
				this.schedule = cfg.getSchedule(schedule);
			} else if(create) {
				this.schedule = cfg.createSchedule(name);
			}
		},
		
		_updateConfig : function() {
			if(this.config == null || this._noUpdate) {
				return;
			}
			
			this._getOrCreateSchedule(true);
			var values = this.Form.getValues();
			
			this.schedule.setCancelScheduleOnAlFailure(values.cancelonfail == "on");
			this.schedule.setSkipExecIfAlRunning(values.noduplicates == "on");
			this.schedule.setEnabled(values.enabled == "on");
			
			var str = "";
			
			// months
			this.schedule.setTimePattern("month", values.months == "*" ? "*" : values.month);
			
			// days
			this.schedule.setTimePattern("wday", "*");
			if(values.days == "w") {
				this.schedule.setTimePattern("wday", values.weekday);
				this.schedule.setTimePattern("mday", "*");
			} else if (values.days == "d") {
				this.schedule.setTimePattern("mday", values.day);
				this.schedule.setTimePattern("wday", "*");
			} else {
				this.schedule.setTimePattern("wday", "*");
				this.schedule.setTimePattern("mday", "*");
			}
			
			// hours
			this.schedule.setTimePattern("hour", values.hour == "" ? "0" : values.hour);
			
			// hours
			this.schedule.setTimePattern("minute", values.minute == "" ? "0" : values.minute);
			
			// seconds
			this.schedule.setTimePattern("second", values.second == "" ? "0" : values.second);
			
		},
		
		_onMonthChanged : function() {
			this._showHideSections();
			this._updateConfig();
		},
		
		_showHideSections : function() {
			var obj = this.Form.getValues();
			dojo.style(this.MonthTable, "display", obj.months == "*" ? "none" : "");
			if(obj.days == "w") {
				dojo.style(this.WeekDays, "display", "");
				dojo.style(this.Days, "display", "none");
			} else if(obj.days == "d") {
				dojo.style(this.WeekDays, "display", "none");
				dojo.style(this.Days, "display", "");
			} else {
				dojo.style(this.WeekDays, "display", "none");
				dojo.style(this.Days, "display", "none");
			}
		},
		
		_onDayChanged : function() {
			this._showHideSections();
			this._updateConfig();
		},
		
		_onHourChanged : function() {
			this._updateConfig();
		},
		
		_onMinuteChanged : function() {
			this._updateConfig();
		},
		
		_onSecondChanged : function() {
			this._updateConfig();
		},
		
		_onOthersChanged : function() {
			this._updateConfig();
		},
		
		_createDayControls : function() {
			var tr = dojo.create("tr", {}, this.DayTable);
			for(var day = 1; day < 32; day++) {
				if(day % 7 == 0) {
					tr = dojo.create("tr", {}, this.DayTable);
				}
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"day", value:day}).placeAt(td);
				check.onClick = dojo.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:day}, td);
			}
		},
		
		_createWeekDayControls : function() {
			var tr = dojo.create("tr", {}, this.WeekDays);
			var names = dojo.date.locale.getNames("days", "wide");
			for(var day = 0; day < names.length; day++) {
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"weekday", value:day}).placeAt(td);
				check.onClick = dojo.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:names[day]}, td);
			}
		},
		
		_createHourControls : function() {
			var tr = dojo.create("tr", {}, this.Hours);
			for(var hour = 0; hour < 24; hour++) {
				if(hour % 12 == 0) {
					tr = dojo.create("tr", {}, this.Hours);
				}
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"hour", value:hour}).placeAt(td);
				check.onClick = dojo.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:hour}, td);
			}
		},
		
		_createMonthControls : function() {
			var names = dojo.date.locale.getNames("months", "wide");
			var tr = dojo.create("tr", {}, this.MonthTable);
			for(var month = 0; month < 12; month++) {
				if(month % 6 == 0) {
					tr = dojo.create("tr", {}, this.MonthTable);
				}
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"month", value:month+1}).placeAt(td);
				check.onClick = dojo.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:names[month]}, td);
			}
		},
		
		_createALParamsForm : function() {
			dojo.when(tdiapi.getConnectorForm("ibmdi.AssemblyLineConnector", "en"), dojo.hitch(this, function(data) {
				this._connectorform = new tdi.FormWidget({
					formData:data,
					verticalLayout:true,
					isSchedule:true,
					config:this.schedule,
					hideNullValues:false,
					getParamValue: dojo.hitch(this, function(param) {
						return this.schedule.getInitParams().getParam(param);
					}),
					setParamValue: dojo.hitch(this, function(param, value) {
						this.schedule.getInitParams().setParam(param, value);
						this.config.setModified(true);
					}),
					visibleButtons:[false, false, false, false]
				});
				this._connectorform.placeAt(this._alparams, "last");
			}));
		},
		
		postCreate : function() {
			this._createMonthControls();
			this._createWeekDayControls();
			this._createDayControls();
		},
		
		destroy : function() {
			this.inherited(arguments);
		}
});
