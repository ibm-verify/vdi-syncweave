/**
 * LDSMonitorSettings
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/ContentPane",
	"tdi/FormWidget",
	"tdi/tdiapi",
	"idx/dialogs",
	"./LDSUtil",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSMonitorSettings.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, ContentPane, FormWidget, tdiapi, idx, LDSUtil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		
		resize: function(obj) {
			if(obj && obj.h) {
				this.stack.resize(obj);
			}
		},
		
		loadMonitorForm: function(item) {
			var t = this;
			var gs = LDSUtil.getGeneralSettingsConnector(t.config);
			var conn = t.config.getTop().lookup(item.id);
			if(conn) {
				var type = tdiapi.getConnectorType(conn);
				if(type) {
					tdiapi.getConnectorForm(type).then(function(formdata) {
						var form = new FormWidget({
							formData:formdata,
							verticalLayout:true,
							config:gs.getConnectionConfig(),
							hideNullValues:false
						});
						t.stack.addChild(new ContentPane({
							title:item.name,
							content:form
						}))
						t.loadNextMonitorForm();
					}, function(err) {
						t.stack.addChild(new ContentPane({
							title:item.name,
							content:"error: " + err
						}))
						t.loadNextMonitorForm();
					});
				}
			}
		},
		
		loadNextMonitorForm: function() {
			if(this.monitors && this.monitors.length > 0) {
				var item = this.monitors.shift();
				this.loadMonitorForm(item);
			}
		},
		
		startup: function() {
			var t = this;
			t.inherited(arguments);
			t.monitors = [].concat(LDSUtil.getSyncEngineMonitors(t.config));
			t.loadNextMonitorForm();
		}
	})
});