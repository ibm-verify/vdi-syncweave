dojo.provide("tdi.JavascriptEditor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");
dojo.require("dijit.form.Button");

dojo.require("dojox.highlight");
dojo.require("dojox.highlight.languages.javascript");
//dojo.require("dojox.highlight.languages.pygments.css");

dojo.require("tdi.NlsMixin");

dojo.declare("tdi.JavascriptEditor",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
	{
		widgetsInTemplate: true,
		templateString: "<div style='margin-top: 5px; overflow:scroll' dojoAttachPoint='Editor'><div>",
	
		postCreate : function() {
			var title = this.config.getName() || this.getString("JavaScriptView.1");
//			dojo.attr(this.Title, {innerHTML:title});
			var str = dojox.highlight.processString(this.config.getScript()).result;
			dojo.attr(this.Editor, {innerHTML:"<pre>" + str + "</pre>"});
		},
		
		postCreatex : function() {
			if(this.config) {
				var title = this.config.getName() || this.getString("JavaScriptView.1");
				var div = dojo.create("h2", {innerHTML:title}, this.Editor);
//				new dijit.form.Button({
//					label:"Toggle",
//					onClick:dojo.hitch(this, "toggleEditor")
//				}).placeAt(this.Editor);
				var table = dojo.create("table", {style:"border-width:0px; border-color:MediumSeaGreen; border-style:solid"}, this.Editor);
				var iscomment = false;
				dojo.forEach(this.config.getScript().split("\n"), function(line) {
					var style = {
						whiteSpace: "pre",
						fontFamily: "Courier",
						fontSize: "12px"
					}
					
					var offset = 0;
					for(i = 0; i < line.length; i++) {
						if(line.charAt(i) == " ")
							offset += 4;
						else if(line.charAt(i) == "\t")
							offset += 16;
						else
							break;
					}
					
					if(offset > 0)
						style.marginLeft = offset + "px";

					//line = line.trim();
					if (line.match(/^\/\*.*/)) {
						iscomment = true;
					}
					
					if(line.match(/^\/\/.*/) || iscomment) {
						style.color = "MediumSeaGreen";
						style.fontStyle = "italic";
					} 
					
					if (line.match(/\*\/$/)) {
						iscomment = false;
					}
					
					var tr = dojo.create("tr", null, table);
					var td = dojo.create("td", {valign:"top"}, tr);
					dojo.create("div", {
						innerHTML:line,
						style:style
					}, td);
				});
				this.table = table;
			}
		}
	}
);