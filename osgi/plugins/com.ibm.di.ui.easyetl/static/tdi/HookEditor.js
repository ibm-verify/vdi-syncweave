dojo.provide("tdi.HookEditor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Textarea");

dojo.declare("tdi.HookEditor", [ dijit._Widget, dijit._Templated ], {
	templatePath : dojo.moduleUrl("tdi", "templates/HookEditor.html"),
	widgetsInTemplate : true,
	
	// config: tdi.basecfg
	//		The hook configuration

	_updateScript : function(newValue) {
		if(this.config != null) {
			if(this.config.getScript() != newValue)
				this.config.setScript(newValue);
		}
	},
	
	postCreate : function() {
		if(this.config != null) {
			this.Name.innerHTML = this.config.getName();
			this.Script.attr("value", this.config.getScript());
			this.Script.onChange = dojo.hitch(this, "_updateScript");
		}
	}
});
