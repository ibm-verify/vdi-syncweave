define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/TitlePane",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/Form",
	"dijit/form/RadioButton",
	"dojo/date/locale",
	"tdi/NlsMixin",
	"tdi/tdiutil",
	"tdi/tdiapi",
	"dojo/text!./templates/ALSchedule.html"
], function(declare, lang, array, html, Widget, TemplatedMixin, WidgetsInTemplate, TitlePane, Button, Checkbox, Form, RadioButton, locale, tdiNlsMixin, tdiutil, tdiapi, template) {
return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, tdiNlsMixin],
	{
	// summary:
	//		A simple editor for a Schedule configuration
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templateString : template,
		
		keepalive: {
			"@type":"reviveAl",
			failIfAlDiedIn:1,
			failureAL:""
		},
		
		// config: tdi.basecfg
		//		The schedule configuration
		config: null,
		
		constructor: function(args) {
			if(args)
				declare.safeMixin(this, args)
		},
		
		setConfig : function(config) {
			this.config = config;
			this._getOrCreateSchedule(true);
			
			var vals = {
				enabled: "off",
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

			this._keepalive.set("value", this.schedule.getObject("@type"));
			this._enabled.set("value", this.schedule.getEnabled());
			
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
				if(vals.month != null) {
					vals.month = vals.month.split(",");
				}
				if(vals.day != "*")
					vals.day = vals.day.split(",");
				if(vals.weekday != "*")
					vals.weekday = vals.weekday.split(",");
				this._noUpdate = true;
				this.Form.setValues(vals);
				// reset after 50ms to give controls time to callback
				setTimeout(lang.hitch(this, function() {
					this._noUpdate = false;
				}), 50);
				this._showHideSections();
				this._createALParamsForm();
			}
			
			html.style(this.Empty, "display", this.schedule != null ? "none" : "");
			html.style(this.Form.domNode, "display", this.schedule != null ? "" : "none");
		},
		
		setScheduleEnabled: function(enable) {
			if(this.schedule) {
				var vals = {
						enabled: enable ? "on" : ""
				};
				this.Form.setValues(vals);
				this.schedule.setEnabled(enable);
			}
		},
		
		_deleteSchedule : function() {
			tdiutil.confirm(this.getString("deleteSchedule"), lang.hitch(this, function(buttonId, messageId, checked) {
				if(buttonId == 0) {
					var cfg = this.config.getParent();
					var name = cfg.getScheduleForAssemblyLine(this.config.getName());
					cfg.deleteSchedule(name);
					this.schedule = null;
					this.setConfig(this.config);
					this.onModify();
				}
			}));
		},
		
		_createSchedule : function() {
			this._getOrCreateSchedule(true);
			this.schedule.setEnabled(false);
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
				this.schedule.setEnabled(false);
			}
		},
		
		_updateConfig : function() {
			if(this.config == null || this._noUpdate) {
				return;
			}
			
			this._getOrCreateSchedule(true);
			var values = this.Form.get("value");
			
			this.schedule.setCancelScheduleOnAlFailure(values.cancelonfail == "on");
			this.schedule.setSkipExecIfAlRunning(values.noduplicates == "on");
			
			// enabled moved outside Form object
			//this.schedule.setEnabled(values.enabled == "on");
			
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
			
			this.onModify();
		},
		
		_onMonthChanged : function() {
			this._updateConfig();
			this._showHideSections();
		},
		
		_showHideSections : function() {
			var obj = this.Form.get("value");
			
			html.style(this.MonthTable, "display", obj.months == "*" ? "none" : "");
			
			if(obj.days == "w") {
				html.style(this.WeekDays, "display", "");
				html.style(this.Days, "display", "none");
			} else if(obj.days == "d") {
				html.style(this.Days, "display", "");
				html.style(this.WeekDays, "display", "none");
			} else {
				html.style(this.WeekDays, "display", "none");
				html.style(this.Days, "display", "none");
			}
		},
		
		_onDayChanged : function() {
			this._updateConfig();
			this._showHideSections();
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
		
		_onEnabledChange: function(enabled) {
			if(this.schedule) {
				if(this.schedule.getEnabled() != enabled) {
					this.schedule.setEnabled(enabled);
					this.onModify();
				}
			}
		},
		
		_createDayControls : function() {
			var tr = dojo.create("tr", {}, this.DayTable);
			for(var day = 0; day < 31; day++) {
				if(day % 7 == 0) {
					tr = dojo.create("tr", {}, this.DayTable);
				}
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"day", value:day+1}).placeAt(td);
				check.onClick = lang.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:day+1}, td);
			}
		},
		
		_createWeekDayControls : function() {
			var tr = dojo.create("tr", {}, this.WeekDays);
			var names = dojo.date.locale.getNames("days", "wide");
			for(var day = 0; day < names.length; day++) {
				var td = dojo.create("td", {}, tr);
				var check = new dijit.form.CheckBox({name:"weekday", value:day+1}).placeAt(td);
				check.onClick = lang.hitch(this, "_updateConfig");
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
				check.onClick = lang.hitch(this, "_updateConfig");
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
				var check = new dijit.form.CheckBox({name:"month", value:month}).placeAt(td);
				check.onClick = lang.hitch(this, "_updateConfig");
				dojo.create("span", {innerHTML:names[month]}, td);
			}
		},
		
		_createALParamsForm : function() {
			dojo.when(tdiapi.getConnectorForm("ibmdi.AssemblyLineConnector", "en"), lang.hitch(this, function(data) {
				this._connectorform = new tdi.FormWidget({
					formData:data,
					verticalLayout:true,
					isSchedule:true,
					config:this.schedule,
					hideNullValues:false,
					getParamValue: lang.hitch(this, function(param) {
						return this.schedule.getInitParams().getParam(param);
					}),
					setParamValue: lang.hitch(this, function(param, value) {
						this.schedule.getInitParams().setParam(param, value);
						this.config.setModified(true);
					}),
					visibleButtons:[false, false, false, false]
				});
				this._connectorform.placeAt(this._alparams, "last");
			}));
		},
		
		onModify: function() {
			// summary:
			//		callback when schedule config has been updated
		},
		
		changeScheduleType: function(value) {
			if(value == "scheduleAl") {
				this.schedule.setTimed();
				this.Form.set("style", {display:""});
			} else {
				this.schedule.setKeepalive();
				this.Form.set("style", {display:"none"});
			}
		},
		
		postCreate : function() {
			this._createMonthControls();
			this._createWeekDayControls();
			this._createDayControls();
			if(this.hidetopbutton) {
				html.style(this.TopButton.domNode, "display", "none");
			}
		},
		
		destroy : function() {
			this.inherited(arguments);
		}
	});
});