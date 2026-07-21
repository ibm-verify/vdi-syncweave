dojo.provide("tdi.ListSelection");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ItemFileReadStore");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.ListSelection",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		contents: null,
		templatePath: dojo.moduleUrl("tdi", "templates/ListSelection.html"),

		postCreate : function() {
			var src = {identifier:"id", label:"name", items:this.content};
			this.list.attr("store", new dojo.data.ItemFileReadStore({data:src}));
		}
	}
);