define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/dom-style",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"orion/editor/edit",
	"tdi/orion/TDIContentAssistProvider",
	"dojo/text!./templates/OrionEditor.html"
], function(declare, lang, array, domStyle, Widget, TemplatedMixin, WidgetsInTemplate, edit, _TDIContentAssistProvider, template) {
return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate],
	{
		templateString: template,
		
		getValue: function() {
			if(this.value || this.config) {
				var val = this.value ? this.value : this.config.getScript();
				val = val || this.defaultText;
			}
			return val || "";
		},
		
		onChange: function(text) {
			if(this.autoUpdate && this.config) {
				this.config.setScript(text);
			} else {
				this.value = text;
			}
		},
		
		setConfig: function(config) {
			this.config = config;
			this.editor.setText(this.config.getScript() || "");
		},
		
		resize: function(size) {
			if(this.editor && this.editor._textView) {
				if(size && size.h) {
					domStyle.set(this.editorParent, {height:size.h+"px"});
					this.editor._textView.resize();
				}
			}
		},
		
		startup: function() {
			var t = this;
			t.editor = edit({parent:this.editorParent, xxtheme:"../editor/themes/tierra.css"});
			t.editor.getContentAssist().addEventListener("Activating", function() {
				var tdiContentAssistProvider = new _TDIContentAssistProvider.TDIContentAssistProvider(t.config);
				t.editor.getContentAssist().providers.push(tdiContentAssistProvider);
			});
			t.editor._textView.addEventListener("ModelChanged", function(event) {
				t.onChange(t.editor.getText());
			});
			if(this.config && this.config.getScript)
				this.setConfig(this.config);
		}
	})
});
