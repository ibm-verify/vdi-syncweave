/*
/*
 */
define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "dijit/form/Select",
   "dijit/form/Button",
   "tdi/tdiapi",
   "tdi/tdiconfig",
   "tdi/tdiutil",
   "tdi/NlsMixin",
   "dojo/text!./templates/FormWidget_config.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Select, Button,
		tdiapi, tdiconfig, tdiutil, nls, template) {

	return declare([ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
		{
		templateString : template,
		
		// label: String
		label: "",
		
		_onChange: function() {
			this.set("value", this._text.get("value"));
			this.onChange(this._text.get("value"));
		},
		
		onChange: function(value) {
		},

		_createConfig: function() {
			var t = this;
			tdiutil.prompt(this.getString("WebCE.enter_name"), function(name) {
				var cfg = new tdiconfig({});
				cfg.setConfigName(name[0]);
				tdiapi.createSolution(cfg).then(function(ok) {
					t._text.addOption({id:name, label:name});
				}, function(err) {
					tdiutil.error(err);
				});
				
			}, this.getString("NewProject.2"));
		},
		
		postCreate : function() {
			var t = this;
			tdiapi.getServerProjects(true).then(function(data) {
				array.forEach(data, function(item) {
					t._text.addOption({value:item.id, label:item.name});
				});
				t._text.set("value", t.get("value"));
			});
		}
	})
});