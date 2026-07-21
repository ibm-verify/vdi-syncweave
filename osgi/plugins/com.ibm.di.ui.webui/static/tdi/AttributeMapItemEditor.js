dojo.provide("tdi.AttributeMapItemEditor");

dojo.require("dojo.data.ItemFileReadStore");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Editor");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.RadioButton");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.TooltipDialog");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");

dojo.require("dojox.highlight");
dojo.require("dojox.highlight.languages.javascript");

dojo.require("tdi.tdiconfig");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.AttributeMapItemEditor",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/AttributeMapItemEditor.html"),
		ami : null,
		data : null,
		
		onClose : function() {
		},
		
		onSave : function() {
			var simple = this._simple.attr("checked");
			if(simple)
				this._mapstoChanged();
			else
				this._scriptChanged();
			
			var str = this.getAttributeName();
			if(this.ami != null) {
			}
			this.onClose();
		},
		
		_mapstoChanged : function() {
			if(this.ami != null) {
				this.ami.setSimple(this.getSourceAttribute());
			}
		},
		
		_scriptChanged : function() {
			if(this.ami != null) {
				this.ami.setAdvanced(this.getScript());
			}
		},
		
		getAttributeName : function() {
			// summary:
			//		Returns the attribute name field
			return this.AttributeName.get("value");
		},
		
		getSourceAttribute : function() {
			// summary:
			//		Returns attribute name from which this
			//		map item gets its value.
			return this.MapsTo.get("value");
		},
		
		getScript : function() {
			// summary:
			//		Returns the custom script for the att map item
			return this.Script.get("value");
		},
		
		toggleSimple : function() {
			var simple = this._simple.attr("checked");
			this.selectTab(simple);
		},
		
		selectTab : function(simple) {
			if(simple) {
				this._stack.selectChild(this._simple_pane.id, true);
			} else {
				this._stack.selectChild(this._script_pane.id, true);				
			}
			this._stack.layout();
		},
		
		editAttribute : function(data) {
			// summary:
			//		Updates the form fields from the attribute map item
			//		provide in the data structure.
			this.data = data;
			this.ami = data.ami;

			this.MapsTo.set("value", "");
			this.Script.set("value", "");
			this.AttributeName.set("value", "");
			
			if(this.ami != null) {
				var str = this.ami.getMapsTo();
				this.AttributeName.set("value", this.ami.getName());
				if(this.ami.isSimple()) {
					this.MapsTo.set("value", str);
				} else {
					this.Script.set("value", str);
				}
				this._simple.attr("checked", this.ami.isSimple())
				this._advanced.attr("checked", !this.ami.isSimple())
				this.selectTab(this.ami.isSimple());
			} else {
				this.AttributeName.set("value", data.attr);
			}
			
			this._updateDropdownList();
		},
		
		resize: function(obj) {
			if(this._stack != null) {
				this._stack.resize(obj);
			}
		},
		
		_updateDropdownList : function() {
			// summary:
			// 		Updates the dropdown list with attributes
			// description:
			// 		For input maps we shows all schema items and for
			//		the output map we show all input attributes.
			//
			var arr = this.data.availableAttributes;
			arr = arr.sort();
			var content = new Array();
			dojo.forEach(arr, function(entry) {
				content.push({id:entry, name:entry});
			});
			var src = {identifier:"id", label:"name", items:content};
			this.MapsTo.attr("store", new dojo.data.ItemFileReadStore({data:src}));
		}
	}
);
