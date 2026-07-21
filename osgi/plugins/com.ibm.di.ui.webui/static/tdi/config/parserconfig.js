define([
	"dojo/_base/declare",
	"tdi/config/basecfg"
], function(declare, tdibasecfg) {
return declare(
	[tdibasecfg],
	{
// 	dojo.declare("tdi.parserconfig", [tdi.basecfg], {
		
		rawconfig: null,
		
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this.rawconfig = null;
		},

		getInheritedObj : function() {
			var ref = this.getInheritFrom();
			if(ref == "[parent]") {
				var conn = null;
				if(this.getParent() != null) {
					conn = this.getParent().getInheritedObj();
				}
				if(conn) {
					return conn.getParserConfig();
				}
			}
			return this.inherited(arguments);
		},
		
		getParserType : function() {
			var ref = this.getObject("rawConfig.inheritFrom");
			if(ref == undefined || ref == "[parent]")
				ref = this.getObject("inheritFrom");
			
			return ref;
		},
		
		setParserType : function(type) {
			this.setObject("inheritFrom", type);
			this.setObject("rawConfig.inheritFrom", type);
			this._inheritsFrom = null;
			this.rawconfig = null;
		},
		
		getParamByName : function(name, ignoreInherit) {
			return this.getConfig().getParamByName(name, ignoreInherit);
		},
		
		setParam : function(param, value) {
			this.getConfig().setParam(param, value);
		},
		
		getConfig : function() {
			if(this.rawconfig == null)
				this.rawconfig = new tdibasecfg({config:this.getObject("rawConfig"), parentConfig:this});
			return this.rawconfig;
		}

	});

	
});
