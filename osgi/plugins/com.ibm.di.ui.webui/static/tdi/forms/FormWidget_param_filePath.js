define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog",
	"tdi/FileBrowser",
	"dojo/text!./templates/FormWidget_param_filePath.html"
], function(declare, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Dialog, FileBrowser, template) {
	
	return declare(
		[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
		{
			templateString: template,
			
			openDialog: function() {
				var fb = new FileBrowser({
					onOk:lang.hitch(this, function() {
						dlg.hide();
						var val = fb.getValue();
						if(val) {
							this.formWidget.setParamValue(this.formItem.key, val.path[0]);
							this.formWidget.updateControl(this.formItem.key);
						}
					}),
					onCancel:function() {
						dlg.hide();
					},
					style:"width:100%; height:100%"
				});
				var dlg = new Dialog({
					content:fb,
					style:"width:300px; height:400px",
					title:"Select File"
				});
				dlg.show();
			},
			
			onChange: function() {
			},
			
			_getValueAttr: function() {
				return this.Filebox.get("value");
			},
			
			_setValueAttr: function(value) {
				this.Filebox.set("value", value);
			},
			
			postCreate: function() {
				if(this.value)
					this.Filebox.set("value", this.value);
			}
		}
	)
	
});
