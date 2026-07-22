/*
/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * (C) Copyright IBM Corporation. 2012
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     
 * @owner       
 * @history
 */
dojo.provide("tdi.forms.FormWidget_assemblyline");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.Button");
dojo.require("dojox.form.CheckedMultiSelect");
dojo.require("tdi.tdiutil");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.forms.FormWidget_assemblyline", [dijit._Widget,dijit._Templated,tdi.NlsMixin], {
	templateString: "<div style='width:100%'><div data-dojo-type='dijit.form.TextBox' style='width:80%' data-dojo-attach-point='Text'></div><button data-dojo-attach-event='onClick:selectAssemblyLines' iconClass='tdiEditImage' showLabel=false data-dojo-type='dijit.form.Button'>...</button></div>",
	widgetsInTemplate: true,
	label: "",
	
	onChange : function(newvalue) {
		// summary:
		//		Called when a new value has been set in the widget
	},
	
	_updateValue : function(newvalue) {
		this.oldvalue = this.value;
		this.value = this.Text.get("value");
		if(this.value != this.oldvalue)
			this.onChange(this.value);
	},
	
	_updateFromCombo : function(dlg, div) {
		var arr = new Array();
		dojo.forEach(this.lastSelectionValue.getOptions(), function(opt) {
			if(opt.selected)
				arr.push(opt.value);
		})
		this.Text.set("value", arr.join(","));
		dojo.destroy(div);
		this.lastSelectionValue.destroyRecursive();
		dlg.hide();
		dlg.destroyRecursive();
	},
	
	selectAssemblyLines : function() {
		if(this.config) {
			var varr = this.value ? this.value.split(",") : [];
			var arr = this.config.getTop().getAssemblyLineNames();
			var options = dojo.map(arr, function(str) {
				return {
					value:str,
					label:str,
					selected:(dojo.indexOf(varr, str) != -1)
				}
			});
			
			var div = dojo.create("div", {style:"padding:10px"});
			
			this.lastSelectionValue = new dojox.form.CheckedMultiSelect({
				multiple:true,
				options:options,
				style:"width:300px; height:100%"
			}).placeAt(div, "last");
			this.lastSelectionValue.startup();
			
			dojo.create("div", {innerHTML:"<p></p>"}, div);
			
			new dijit.form.Button({
				label:this.getString("ok"),
				type:"submit"
			}).placeAt(div);
			
			new dijit.form.Button({
				label:this.getString("cancel"),
				onClick:dojo.hitch(this, function() {
					dlg.hide();
				})
			}).placeAt(div);
			
			var dlg = new dijit.Dialog({
				title:this.label,
				content:div,
				execute:dojo.hitch(this, "_updateFromCombo", dlg, div)
			});
			dlg.show();
		}
	},
	
	postCreate : function() {
		if(this.value) {
			this.Text.set("value", this.value);
		}
		dojo.connect(this.Text, "onChange", this, "_updateValue"); 
	}
});