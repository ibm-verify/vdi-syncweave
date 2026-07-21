define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/tdibranchconditions",
	"tdi/config/connector",
	"tdi/config/attributeloop"
], function(declare, basecfg, tditdibranchconditions, tdiconnector, tdiattributeloop) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.tdibranch", [tdi.basecfg], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getConditions : function() {
			if(!this._conditions) {
				var obj = this.getObject("condition");
				if(!obj)
					obj = this.getObject("whileCondition");
				if(obj) {
					this._conditions = new tditdibranchconditions({config:obj, parentConfig:this});
				}
			}
			return this._conditions;
		},
		
		getBranchType : function() {
			var type = this.getObject("type");
			if(!type) {
				if(this.getObject("collectionCondition"))
					return "AttributeLoop";
				else if(this.getObject("connectorCondition"))
					return "ConnectorLoop";
				else if(this.getObject("whileCondition"))
					return "WhileLoop";
			}
			return type;
		},
		
		setBranchType: function(type) {
			this.setObject("type", type);
		},
		
		getConnectorConfig : function() {
			if(!this._connectorConfig) {
				this._connectorConfig = new tdiconnector({config:this.getObject("connectorCondition.connector"), parentConfig:this});
			}
			return this._connectorConfig;
		},
		
		getWhileConfig : function() {
			return this.getConditions();
		},
		
		getAttributeConfig : function() {
			if(!this._attributeConfig) {
				this._attributeConfig = new tdiattributeloop({config:this.getObject("collectionCondition"), parentConfig:this});
			}
			return this._attributeConfig;
		}
	});
		
});
