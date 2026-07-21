dojo.provide("tdi.PropertyEditor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.DateTextBox");
dojo.require("dijit.form.TextBox");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiapi");

dojo.declare("tdi.PropertyEditor", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		A property value editor
	widgetsInTemplate: true,
	templatePath: dojo.moduleUrl("tdi", "templates/PropertyEditor.html"),
	
	postCreate: function() {
		this.Name.innerHTML = this.item.label;
		if(this.item.type == "combo")
			this.Editor = new dijit.form.ComboBox({}, this.Control);
		else if(this.item.type == "date")
			this.Editor = new dijit.form.DateTextBox({}, this.Control);
		else
			this.Editor = new dijit.form.TextBox({placeHolder:"..."}, this.Control);
		
		dojo.when(tdiapi.getPropertyStoreValues(this.cientry, this.item.storeName), dojo.hitch(this, function(data) {
			if(data.property != null) {
				dojo.forEach(data.property, dojo.hitch(this, function(prop) {
					if(prop.name == this.item.name) {
						this.Editor.attr("value", prop.value);
						this.Description.innerHTML = this.item.userComment;
					}
				}))
			}
		}), tdiapi.defaultErrHandler).then()
	}
});
