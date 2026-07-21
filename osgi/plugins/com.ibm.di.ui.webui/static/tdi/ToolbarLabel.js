/**
 * A toolbar item that displays text/html
 */
define([
    	"dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin"
], function (declare, _Widget, _TemplatedMixin) {
return declare(
		[_Widget, _TemplatedMixin],
		{
			templateString: '<div class="dijit dijitInline" valign="bottom">${label}</div>',
			label:"Default Label",
			
			buildRendering: function(){
				this.inherited(arguments);
				dojo.setSelectable(this.domNode, false);
			},
		
			_setLabelAttr: function(label) {
				this.setLabel(label);
			},
			
			setLabel: function(label) {
				this.domNode.innerHTML = label;
			},
			
			isFocusable: function(){
				return false;
			}
		});
});
