define([
	"dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog",
	"dijit/form/Button",
	"dijit/form/TextBox",
	"tdi/NlsMixin",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"dojo/io/iframe",
	"dojo/text!./templates/UploadSolution.html"
], function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, Button, TetxBox, NlsMixin, tdiapi, tdiutil, iframe, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString: template,
		_hasFile : false,
		
		constructor: function(args) {
			if(args)
				declare.safeMixin(this, args);
			declare.safeMixin(this, new NlsMixin());
		},
			
		
		uploadCompleted : function() {
		},

		listFiles: function() {
			dojo.when(tdiapi.getServerProjects(), dojo.hitch(this, "listInstalledFiles"));
			dojo.when(tdiapi.getWebCeTemplates(), dojo.hitch(this, "listTemplates"));
		},

		listInstalledFiles: function(result) {
			for (f in result.items) {
				var item = result.items[f];
				dojo.create("div", {
					innerHTML : item.name
				}, this.installedFiles);
			}
		},

		listTemplates: function(result) {
			for (f in result.ConfigTemplate) {
				var item = result.ConfigTemplate[f];
				dojo.create("div", {
					innerHTML : item.Name
				}, this.templateFiles);
			}
		},
		
		sendFile : function() {
			iframe.send({
				url:"/dashboard/templates",
				method: "post",
				handleAs: "html",
				form: this.Form,
				content: {
					replace: "true"
				},
				handle: dojo.hitch(this, function(data) {
					var msg = data.body.innerText;
					if(msg === undefined)
						msg = data.body.innerHTML;
					if(msg != "OK") {
						tdiutil.alert(msg, "Error");
					} else {
						this.uploadCompleted();
					}
				}),
				error: function(err) {
					tdiutil.error(err);
				}
			});
		},
		
		_fileChanged : function(evt) {
			this._sendButton.set("disabled", false);
			this._hasFile = true;
			this._solChanged();
		},
		
		_solChanged : function(evt) {
			if(!this._hasFile)
				return;
			
			if(this.solution && !this.solution.get("disabled") && this.solution.get("value") == "") {
//				this._sendButton.set("disabled", true);
			} else {			
//				this._sendButton.set("disabled", false);
			}
		},
		
		postCreate : function() {
			this._sendButton.set("disabled", true);
			this.listFiles();
		}
	})
});
