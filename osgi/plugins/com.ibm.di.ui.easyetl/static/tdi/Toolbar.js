dojo.provide("tdi.Toolbar");

dojo.require("dijit.Toolbar");

dojo.declare("tdi.Toolbar",
		[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		widgetsInTemplate: true,
		templateString: "<div><div dojoType='dijit.Toolbar' dojoAttachPoint='toolbar'></div></div>",
		buttons: null,
	
		addButton : function(button) {
			this.toolbar.addChild(button);
			if(this.buttons == null) {
				this.buttons = [];
			}
			this.buttons.push(button);
		},
		
		getButton : function(command) {
			if(this.buttons == null)
				return null;
			
			var arr = dojo.filter(this.buttons, function(item) {
				return item.cmd == command;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		resize : function(ns) {
			if(ns && ns.w > 0) {
				this.toolbar.resize(ns);
			}
		}
			
});