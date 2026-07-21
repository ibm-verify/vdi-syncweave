dojo.provide("tdi.CreateSolution");

dojo.require("dijit.form.Form");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dojo.data.ItemFileWriteStore");
dojo.require("tdi.UploadSolution");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.CreateSolution", [ tdi.UploadSolution ], {
	// summary:
	//		This widget provides a list of configuration files
	//		and running instances (optional) on a server in the left
	//		navigator panel. The right hand part of this widget contains
	//		the editor area, where individual editors are shown.
	//
	
	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "dialogs/CreateFromTemplate.html"),
	
	store: null,
	store2: null,

	listInstalledFiles: function(result) {
		for (f in result.items) {
			var item = result.items[f];
			this.store2.newItem({
				id:item.name,
				name:item.name,
				value:item.name
			});
		}
	},

	listTemplates: function(result) {
		for (f in result.ConfigTemplate) {
			var item = result.ConfigTemplate[f];
			this.store.newItem({
				id:item.Name,
				name:item.Name,
				value:item.Name
			});
		}
	},
	
	_toggleSolutionCombo : function(enable) {
		if(enable) {
			this.solution.set("disabled", !enable);
			this.template.set("disabled", enable);
		}
	},
	
	_toggleTemplateCombo : function(enable) {
		if(enable) {
			this.template.set("disabled", !enable);
			this.solution.set("disabled", enable);
		}
	},
	
	_toggleP2PCombo : function(enable) {
		if(enable) {
			this.template.set("disabled", enable);
			this.solution.set("disabled", enable);
		}
	},
	
	submitFile : function(e) {
		if(!this.Solution.validate()) {
			e.preventDefault();
		} else if(!this.template.get("disabled") && this.template.get("value") == "") {
			e.preventDefault();
		} else if(!this.solution.get("disabled") && this.solution.get("value") == "") {
			e.preventDefault();
		} else if(this.Solution.get("value") == "") {
			e.preventDefault();
		} else {
			this.sendFile();
			return;
		}
		tdiutil.alert(this.getString("selectTemplate"));
	},
	
	postCreate : function() {
		this.store = new dojo.data.ItemFileWriteStore({data:{
			identifier:"id",
			label:"name",
			items : []
		}});
		this.store2 = new dojo.data.ItemFileWriteStore({data:{
			identifier:"id",
			label:"name",
			items : []
		}});
		this.template.set("store", this.store);
		this.solution.set("store", this.store2);
		this.listFiles();
	}
	
});