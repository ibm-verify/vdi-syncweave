if (!dojo._hasResource["tdi._FormWidgetMixin"]) {
	dojo._hasResource["tdi._FormWidgetMixin"] = true;

	tdi_formwidgetmixin = {}
	
	dojo.provide("tdi.forms._FormWidgetMixin");
	dojo.declare("tdi.forms._FormWidgetMixin", [],
		{
			hasCustomControlFor : function(type, config) {
				// summary:
				//		Returns true if there is a custom control defined for the specified type (e.g. string, boolean etc)
				//
				if(!tdi_formwidgetmixin[type]) {
					tdi_formwidgetmixin[type] = true;
					try {
						dojo.require("tdi.forms.FormWidget_"+type);
					} catch(e) {
						console.log("Cannot load custom widget for type=" + type + "; " + e);
					}
				}
				return dojo.getObject("tdi.forms.FormWidget_" + type);
			},
			
			getCustomControlFor : function(type, args) {
				// summary:
				//		Returns an instance of the custom control
				// description:
				//		The control will displays the value associated with a specific form item.
				//		All custom controls must fire "onChange" events after the value has changed.
				//
				if(!tdi_formwidgetmixin[type]) {
					tdi_formwidgetmixin[type] = true;
					try {
						dojo.require("tdi.forms.FormWidget_"+type);
					} catch(e) {
						console.log("Cannot load custom widget for type=" + type + "; " + e);
					}
				}
				var widget = dojo.getObject("tdi.forms.FormWidget_" + type);
				if(widget) {
					return new widget(args);
				}
				return null;
			}
			
		}
	);

}