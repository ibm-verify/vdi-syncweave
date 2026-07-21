define([
    "dojo/_base/declare",
    "dojo/_base/lang",
    "tdi/forms/_FormWidgetMixinFunctions"
], function(declare, lang, FormFunctions) {

	var tdi_formwidget = {
		// tag classes we have attempted to load
		tdi_formwidgetmixin: {},
		// cache for successfully loaded classes
		tdi_formwidgetcache: {}
	};
	
	tdi_formwidget._loadWidget = function(path) {
		var type = path.indexOf("/") == -1 ? "tdi/forms/FormWidget_"+path: path;
		if(!this.tdi_formwidgetmixin[type]) {
			this.tdi_formwidgetmixin[type] = true;
			try {
				require([type], lang.hitch(this, function(widget) {
					this.tdi_formwidgetcache[type] = widget;
				}));
			} catch(e) {
				console.log("Cannot load custom widget for type=" + type + "; " + e);
			}
		}
		return this.tdi_formwidgetcache[type];
	};
	
	tdi_formwidget.hasCustomControlFor  = function(type, config) {
		// summary:
		//		Returns true if there is a custom control defined for the specified type (e.g. string, boolean etc)
		//
		return this._loadWidget(type);
	};
	
	tdi_formwidget.getCustomControlFor  = function(type, args) {
		// summary:
		//		Returns an instance of the custom control
		// description:
		//		The control will displays the value associated with a specific form item.
		//		All custom controls must fire "onChange" events after the value has changed.
		//
		var widget = this._loadWidget(type);
		if(widget) {
			return new widget(args);
		}
		return widget;
	};
		
	tdi_formwidget.hasCustomControlForParam = function(item, form) {
		// summary:
		//		Returns true if there is a custom control for the specified parameter item
		//		in the form.
		return this.getCustomControlForParam(item, form);
	};
	
	tdi_formwidget.getCustomControlForParam = function(item, form) {
		// summary:
		//		Returns the custom control for a specific form item
		//		in the form.
		if(item.widget)
			return this._loadWidget(item.widget);
		else
			return this._loadWidget("param_" + item.key);
	};
	
	tdi_formwidget.getScriptHandlerFor = function(item, form) {
		// summary:
		//		Attempts to locate a script handler for the specified component
		//		and function name.
		// type: String
		//		The component identifier (ibmdi.FileSystem etc)
		// name: String
		//		The function name
		// return: function
		//		An instance
		if(item.script && lang.isFunction(FormFunctions[item.script]))
			return FormFunctions[item.script];
		else
			return null;
	};
	
	return tdi_formwidget;
});
