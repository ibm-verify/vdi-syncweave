/**
 * The JavascriptEditor lets the user edit javascript with CodeMirror.
 * 
 */
define( [ "dojo/_base/declare", "dojo/_base/lang", "dijit/_Widget", "dijit/_TemplatedMixin", "tdi/tdiconfig", "tdi/NlsMixin" ],

function(declare, lang, dWidget, dTemplated, tdiconfig, NlsMixin) {

	return declare( [ dWidget, dTemplated ], {
		templateString: '<div data-dojo-attach-point="Main" style="height:100%; width:100%; font-size:12px"></div>',
		// config: tdiconfig
		//		used to get/set script
		
		// value: string
		//		used instead of config
		value: null,
		
		// autoUpdate: boolean
		//		auto update the config object on modifications
		autoUpdate: false,
		
		resize : function(obj) {
			this.inherited(arguments);
			if (this.cm && obj && (obj.h > 0 || obj.w > 0)) {
				var hsize = obj.h; // - 16;
				var vsize = obj.w; //- 16;
				this.cm.setSize({w:vsize, h:hsize});
				var wrapper = this.cm.getWrapperElement();
				if (wrapper) {
					wrapper.style.width = vsize + "px";
					wrapper.style.height = hsize + "px";
				}
				var scroller = this.cm.getScrollerElement();
				if (scroller) {
					scroller.style.width = vsize + "px";
					scroller.style.height = hsize + "px";
				}
			}
		},
		
		getEditor : function() {
			return this.cm;
		},

		getSelectedRange : function() {
			return {
				from : this.getEditor().getCursor(true),
				to : this.getEditor().getCursor(false)
			};
		},
		
		autoFormatSelection : function() {
			var range = this.getSelectedRange();
			this.getEditor().autoFormatRange(range.from, range.to);
		},

		commentSelection : function(isComment) {
			var range = this.getSelectedRange();
			this.getEditor().commentRange(isComment, range.from, range.to);
		},
		
		getValue: function() {
			if(this.value || this.config) {
				var val = this.value ? this.value : this.config.getScript();
				val = val || this.defaultText;
			}
			return val || "";
		},
		
		onChange: function(cm, text) {
			if(this.autoUpdate) {
				this.config.setScript(cm.getValue());
			}
		},
		
		setConfig: function(config) {
			this.config = config;
			this.cm.setValue(this.config.getScript());
		},
		
		postCreate : function() {
			CodeMirror.commands.autocomplete = function(cm) {
				CodeMirror.simpleHint(cm, CodeMirror.javascriptHint);
			}

			this.cm = CodeMirror(this.Main, {
				mode : "javascript",
				theme : "eclipse",
				value : this.getValue(),
				indentUnit : 4,
				smartIndent : true,
				indentWithTabs : true,
				autoClearEmptyLines : true,
				lineNumbers : true,
				onChange: lang.hitch(this, "onChange"),
				extraKeys : {
					"Ctrl-Space" : "autocomplete"
				}
			});
	
		}
	})
});