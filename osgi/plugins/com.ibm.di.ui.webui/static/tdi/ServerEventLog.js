/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/GridWidget",
	"tdi/model/ServerProjectsModel",
	"tdi/tdiapi",
	"dojo/text!./templates/ActivityMonitor.html"
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplate, _Tree, ServerProjectsModel, tdiapi, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplate ],
	{
		templateString : template,
		widgetsInTemplate: true,
		counter: 1,
		
		resize: function(obj) {
			this.inherited(arguments);
			if(obj && obj.h && this.treeTable) {
				this.treeTable.resize(obj);
			}
		},
		
		eventLogged: function(event) {
			var arr = [];
			for(f in event) {
				arr.push(f + ":" + event[f]);
			}
			this.treeTable.addItem({id:this.counter++, name:arr.join(",")});
		},
	
		startup : function() {
			this.eventsub = dojo.subscribe("/tdi/server/events", dojo.hitch(this, "eventLogged"));
			tdiapi.startServerEventNotifications();
			this.inherited(arguments);
		}
	});

});