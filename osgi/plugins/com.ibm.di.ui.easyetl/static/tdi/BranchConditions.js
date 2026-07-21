dojo.provide("tdi.BranchConditions");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.JavascriptEditor");

dojo.declare("tdi.BranchConditions",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
	{
		widgetsInTemplate: true,
		templateString: "<div style='margin-top: 5px' dojoAttachPoint='Main'><div>",
		borderStyle: "border-collapse:collapse; border:1px solid MediumSeaGreen; padding:3px",
		headerStyle: "border-collapse:collapse; border:1px solid MediumSeaGreen; text-align:center; margins:5px",
	
		createSwitchCase : function(table) {
			var tr = dojo.create("tr", {}, table);
			dojo.create("td", {style:this.borderStyle, innerHTML:this.conditions.getCondition(0).rightHand}, table);
		},
		
		createConditions : function(table) {
			if(this.conditions.getConditionCount() > 0) {
				var tr = dojo.create("tr", {style:"border-width:1px"}, table);
				for(i = 0; i < 5; i++) {
					dojo.create("th", {style:this.headerStyle, innerHTML:this.getString("BranchingConfig.table."+i)}, tr);
				}
				
				for(i = 0; i < this.conditions.getConditionCount(); i++) {
					var obj = this.conditions.getCondition(i);
					var tr = dojo.create("tr", {}, table);
					if(obj.leftHand) {
						dojo.create("td", {style:this.borderStyle, innerHTML:obj.leftHand}, tr);
						dojo.create("td", {style:this.borderStyle, innerHTML:this.getString("BranchingConfig.Conditions."+obj.operator)}, tr);
					} else {
						dojo.create("td", {style:this.borderStyle, innerHTML:""}, tr);
						dojo.create("td", {style:this.borderStyle, innerHTML:""}, tr);
					}
					var td = dojo.create("td", {style:this.borderStyle, innerHTML:obj.rightHand || ""}, tr);
					
					dojo.create("td", {style:this.borderStyle, innerHTML:obj.negate || "false"}, tr);
					dojo.create("td", {style:this.borderStyle, innerHTML:obj.caseSensitive || "false"}, tr);
				}
			}
		},
		
		postCreate : function() {
			this.conditions = this.config.getBranchConfig().getConditions();
			dojo.create("h1", {innerHTML:"Conditions"}, this.Main);
			var table = dojo.create("table", {width:"98%", height:"98%", style:this.borderStyle}, this.Main);
			
			var type = this.config.getBranchConfig().getBranchType();
			if(type == "Switch" || type == "Case") {
				this.createSwitchCase(table);
			} else {
				this.createConditions(table);
				if(this.conditions.getScript()) {
					dojo.create("p", {}, this.Main);
					new tdi.JavascriptEditor({config:this.conditions}).placeAt(this.Main);
				}
			}
		}
	}
);
