dojo.provide("tdi.TextEditor");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");
dojo.require("dijit.Toolbar");
dojo.require("dijit.form.Button");

dojo.declare("tdi.TextEditor",
		[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
		{
		templatePath: dojo.moduleUrl("tdi", "templates/TextEditor.html"),
		widgetsInTemplate: true,
		
		isEditing:function() {
			return dojo.style(this.Editor.domNode, "display") != "none";
		},
		
		setValue: function(text) {
			if(this.isEditing()) {
				this.Editor.setValue(text);
			} else {
				this.Text.innerHTML = text;
			}
		},
		
		getValue: function() {
			if(this.isEditing()) {
				return this.Editor.getValue();
			} else {
				return this.Text.innerHTML;
			}
		},
		
		toggleEditor : function() {
			if(this.isEditing()) {
				this.Text.innerHTML = this.Editor.getValue();
				dojo.style(this.Editor.domNode, "display", "none");
				dojo.style(this.Text, "display", "");
				this.editButton.set("label", this.getString("editDescription"));
			} else {
				dojo.style(this.Text, "display", "none");
				dojo.style(this.Editor.domNode, "display", "");
				this.Editor.setValue(this.Text.innerHTML);
				this.editButton.set("label", this.getString("close"));
			}
		},
		
		resize : function(obj) {
			this.inherited(arguments);
			if(this.Editor != null)
				this.Editor.resize(obj);
		},
		
		postCreate : function() {
			dojo.connect(this.Text, "ondblclick", dojo.hitch(this, "toggleEditor"));
			dojo.connect(this.Editor, "onKeyDown", dojo.hitch(this, function(e) {
				if(e.keyCode == dojo.keys.ESCAPE) {
					this.toggleEditor();
				} else {
					this.textChanged(this.getValue());
				}
			}));
			if(this.text)
				this.setValue(this.text);
		}
	}
);
