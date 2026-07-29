/*
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */
dojo.provide("tdi.DialogContent");

dojo.require("dijit.Dialog");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.DialogContent",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		// summary:
		//	This widget is only used to mixin tdi.NlsMixin so
		//	that proper translation is made for dialog contents.
		//	Always provide templatePath when using this widget.
		//
		templatePath: "",
		widgetsInTemplate: true,
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		onSubmit : function(e) {
			// summary:
			//		Calls handleOK with form values
			dojo.stopEvent(e);
			
			if(this.Form && this.Form.validate() && this.validateValues(this.getFormValues()))
				this.handleOK(this.getFormValues());
			
			else if(!this.Form && this.validateValues(this.getFormValues()))
				this.handleOK(this.getFormValues());
		},
		
		handleOK : function(formData) {
			// summary:
			//		placeholder for overrides
		},
		
		validateValues : function(formData) {
			// summary:
			//		placeholder for overrides.
			//		return true if values are ok or false if not
			return true;
		},
		
		setFormValues : function(values) {
			if(this.Form)
				this.Form.set("value", values);
		},
		
		getFormValues : function() {
			if(this.Form)
				return this.Form.get("value");
			else
				return {};
		},
		
		postCreate: function() {
		},
		
		destroy : function() {
			if(this.Form)
				this._savedValues = this.getFormValues();
			this.inherited(arguments);
		}
	}
);
