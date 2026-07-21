dojo.provide("tdi.ConfigEditorProps");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");

dojo.require("tdi.PropertyEditor");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.ConfigEditorProps", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A widget for editing the components in an assemblyline.
	//		Only connectors and script components are editable.

	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/ConfigEditorProps.html"),
	widgetsInTemplate : true,
	
	// config: tdi.config
	//		The config to edit
	
	config: null,

	loadProperties: function(data) {
		if(data != null) {
			this.cientry = new tdi.tdicientry({atom:data});
		}
		dojo.forEach(this.properties, dojo.hitch(this, function(item) {
			var p = new tdi.PropertyEditor({cientry:this.cientry,item:item}).placeAt(this.Props, "last");
		}));
	},
	
	destroyRecursive : function() {
		if(this.instanceId != null) {
			tdiapi.stopConfig(this.cientry);
			this.cientry = null;
		}
		this.inherited(arguments);
	},
	
	postCreate : function() {
		if(this.cientry == null) {
			this.instanceId = tdiutil.generateInstanceId(this.config) 
			dojo.when(tdiapi.startTempConfig(this.config, this.instanceId), dojo.hitch(this, "loadProperties"));
		} else {
			this.loadProperties(null);
		}
	}
});

