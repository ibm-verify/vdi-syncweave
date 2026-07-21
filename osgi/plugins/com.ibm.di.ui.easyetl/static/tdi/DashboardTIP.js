dojo.provide("tdi.DashboardTIP");

dojo.require("tdi.Dashboard");

dojo.declare("tdi.DashboardTIP", [ tdi.Dashboard ], {
	// summary:
	//		This widget provides a version of the tdi.Dashboard widget
	//		without the IBM header at the top.
	//
	
	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/DashboardTIP.html"),
	widgetsInTemplate : true,
	
	constructor : function(/* Object */args) {
		dojo.safeMixin(this, args);
	}
});