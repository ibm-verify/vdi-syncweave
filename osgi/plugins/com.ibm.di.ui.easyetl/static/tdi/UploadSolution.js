dojo.provide("tdi.UploadSolution");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.TextBox");

dojo.require("dojo.io.iframe");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.UploadSolution", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		This widget provides a list of configuration files
	//		and running instances (optional) on a server in the left
	//		navigator panel. The right hand part of this widget contains
	//		the editor area, where individual editors are shown.
	//
	
	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/UploadSolution.html"),
	widgetsInTemplate : true,
	_hasFile : false,
	
	uploadCompleted : function() {
		
	},

	listFiles: function() {
		dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, "listInstalledFiles"));
		dojo.when(tdiapi.getWebCeTemplates(), dojo.hitch(this, "listTemplates"));
	},

	listInstalledFiles: function(result) {
		for (f in result.items) {
			var item = result.items[f];
			dojo.create("div", {
				innerHTML : item.name
			}, this.installedFiles);
		}
	},

	listTemplates: function(result) {
		for (f in result.ConfigTemplate) {
			var item = result.ConfigTemplate[f];
			dojo.create("div", {
				innerHTML : item.Name
			}, this.templateFiles);
		}
	},
	
	sendFile : function() {
		dojo.io.iframe.send({
			url:"/dashboard/templates",
			method: "post",
			handleAs: "html",
			form: this.Form,
			content: {
				replace: "true"
			},
			handle: dojo.hitch(this, function(data) {
				var msg = data.body.innerText;
				if(msg === undefined)
					msg = data.body.innerHTML;
				if(msg != "OK") {
					tdiutil.alert(msg, "Error");
				} else {
					this.uploadCompleted();
				}
			}),
			error: function(err) {
				tdiutil.error(err);
			}
		});
	},
	
	_fileChanged : function(evt) {
		this._sendButton.set("disabled", false);
		this._hasFile = true;
		this._solChanged();
	},
	
	_solChanged : function(evt) {
		if(!this._hasFile)
			return;
		
		if(this.solution && !this.solution.get("disabled") && this.solution.get("value") == "") {
			this._sendButton.set("disabled", true);
		} else {			
			this._sendButton.set("disabled", false);
		}
	},
	
	postCreate : function() {
		this._sendButton.set("disabled", true);
		this.listFiles();
	}
	
});

