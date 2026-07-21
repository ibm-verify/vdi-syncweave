define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"tdi/config/basecfg"
], function(declare, lang, array, tdibasecfg) {
return declare(
	[tdibasecfg],
	{
// 	dojo.declare("tdi.schedule", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		partMap : {
			"month": 0,
			"mday": 1,
			"wday": 2,
			"hour": 3,
			"minute": 4,
			"second": 5
		},
		
		setKeepalive: function() {
			var t = this;
			var current = this.getObject("@type");
			if(current != "reviveAl") {
				var keep = ["assemblyLine", "name", "enabled", "initParams"];
				var keepo = {};
				array.forEach(keep, function(f) {
					keepo[f] = t.getObject(f);
				});
				for(f in this.config) {
					delete this.config[f];
				}
				this.setObject("@type", "reviveAl");
				this.setObject("failIfAlDiedIn", 1);
				this.setObject("failureAl", "");
				this.setObject("initParams", {});
				for(f in keepo) {
					this.setObject(f, keepo[f]);
				}
			}
		},
		
		isKeepalive: function() {
			return this.getObject("@type") == "reviveAl";
		},
		
		setTimed: function() {
			var t = this;
			var current = this.getObject("@type");
			if(current != "scheduleAl") {
				var keep = ["assemblyLine", "name", "enabled", "initParams"];
				var keepo = {};
				array.forEach(keep, function(f) {
					keepo[f] = t.getObject(f);
				});
				for(f in this.config) {
					delete this.config[f];
				}
				this.setObject("@type", "scheduleAl");
				for(f in keepo) {
					this.setObject(f, keepo[f]);
				}
			}
		},
		
		setTimePattern : function(part, value) {
			var index = this.partMap[part];
			if(index == null)
				return false;
			
			var arr = this.getExecTimePattern().split(" ");
			if(index >= arr.length || index < 0)
				return false;
			
			if(lang.isArray(value))
				value = value.join(",");
			
			if (value == null || value == "")
				value = "*";
			
			// convert whitespace to comma
			value = value.replace(/\s+/g, ",");
			
			arr[index] = value;
			this.setExecTimePattern(arr.join(" "));
		},
		
		getTimePattern : function(part) {
			var index = this.partMap[part];
			if(index == null)
				return false;
			
			var arr = this.getExecTimePattern().split(" ");
			if(index >= arr.length || index < 0)
				return false;
			
			return arr[index];
		},
		
		setExecTimePattern : function(value) {
			this.setObject("execTimePattern", value);
		},
		
		getExecTimePattern : function() {
			var value = this.getObject("execTimePattern");
			if(value == null || value == "")
				return "* * * * 0 0";
			else
				return value;
		},
		
		setCancelScheduleOnAlFailure : function(cancel) {
			this.setObject("cancelScheduleOnAlFailure", cancel);
		},
		
		getCancelScheduleOnAlFailure : function() {
			return this.getObject("cancelScheduleOnAlFailure");
		},
		
		setSkipExecIfAlRunning : function(skip) {
			this.setObject("skipExecIfAlRunning", skip);
		},
		
		getSkipExecIfAlRunning : function() {
			return this.getObject("skipExecIfAlRunning");
		},
		
		getAssemblyLine : function() {
			return this.getObject("assemblyLine");
		},
		
		getInitParams : function() {
			if(this.initParams == null) {
				if(!this.config.initParams)
					this.config.initParams = {};
				this.initParams = new tdibasecfg({config:this.config.initParams, parentConfig:this});
			}
			return this.initParams;
		}
		
	});

	
});
