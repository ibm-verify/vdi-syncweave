dojo.provide("tdi.AttributeLoop");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.JavascriptEditor");

dojo.declare("tdi.AttributeLoop",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
	{
		widgetsInTemplate: true,
		templateString: "<div style='margin-top: 5px' dojoAttachPoint='Main'><div>",
		
		postCreate : function() {
			dojo.create("h1", {innerHTML:this.getString("LoopConfig.select.entry.label")}, this.Main);
			var table = dojo.create("table", {cellSpacing:"10px"}, this.Main);
			var tr = dojo.create("tr", {}, table);
			dojo.create("td", {innerHTML:this.getString("LoopConfig.attrloop.workattribute")}, tr);
			dojo.create("td", {innerHTML:"<b>" + this.config.getWorkAttributeName() + "</b>"}, tr);

			var tr = dojo.create("tr", {}, table);
			dojo.create("td", {innerHTML:this.getString("LoopConfig.attrloop.loopattribute")}, tr);
			dojo.create("td", {innerHTML:"<b>" + this.config.getLoopAttributeName() + "</b>"}, tr);
		}
	}
);
	
