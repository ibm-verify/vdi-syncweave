define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, tdibasecfg) {
return declare(
	[tdibasecfg],
	{
// 	dojo.declare("tdi.scriptconfig", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getType : function() {
			return "script";
		},
		
		setEnabled: function(enabled) {
			if(enabled != this.isEnabled())
				this.setState(enabled ? "Enabled" : "Disabled");
		},
		
		isEnabled: function() {
			return "Enabled" == this.getState();
		},
		
		setState : function(state) {
			this.setObject("state", state);
		},
		
		getState : function() {
			return this.getObject("state");
		},
		
		getScriptConfig : function() {
			if(!this.scriptConfig) {
				this.scriptConfig = new tdibasecfg({config:this.getObject("simpleConfig"), parentConfig:this});
			}
			return this.scriptConfig;
		}
		
	});
	
});
