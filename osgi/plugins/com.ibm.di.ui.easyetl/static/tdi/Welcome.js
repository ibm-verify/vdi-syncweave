dojo.provide("tdi.Welcome");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Toolbar");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.TitlePane");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.Toolbar");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiutil");
dojo.require("tdi.GlobalLogSettings");

dojo.declare("tdi.Welcome",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// summary:
		//		This widget shows an EasyETL project assemblyline and lets the user
		//		edit, run and save the configuration.
		//
	
	
		// Template variables
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/Welcome.html"),
		
		
		loadAltTemplate: function(lang) {
			var url = dojo.moduleUrl("tdi", "templates/" + lang + "/Welcome.html");
			dojo.when(dojo.xhrGet({
				handleAs : "text",
				preventCache: tdiapi._preventCache,
				url:url.path
			}), dojo.hitch(this, function(data) {
				this.domNode.innerHTML = data;
			}), function() {
			})
		},

		postCreate : function() {
			var vars = [];
			var loc = dojo.locale;
			var arr = loc ? loc.split("-") : [];
			if(arr && arr.length == 2) {
				vars.push(arr[0] + "_" + arr[1].toUpperCase());				
				vars.push(arr[0])
			} else if(arr && arr.length == 1) {
				vars.push(arr[0])
			}

			for(var i = 0; i < vars.length; i++) {
				this.loadAltTemplate(vars[i]);
			}
		}
	}
);