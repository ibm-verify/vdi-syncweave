dojo.provide("tdi.AttributeMapItem");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit._Container")
dojo.require("dijit._HasDropDown")
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");

dojo.require("dojo.parser");
dojo.require("dojo.dnd.Source");

dojo.require("tdi.tdiconfig");

dojo.declare("tdi.AttributeMapItem",
	[dijit._Widget, dijit._Templated, dijit._HasDropDown, dijit._Container],
	{
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/AttributeMapItem.html"),
		
		// attmapitem: tdi.attmapitem
		//		The attribute map item config
		attmapitem: null,
		
		// showMapping: boolean
		//		Show the mapping script
		showMapping: false,
		
		attributeMap : {
			label: {node:"_label", type:"innerHTML" },
			value: {node:"_value", type:"innerHTML" }
		},
		
		_mouseout : function() {
			this.onMouseOut(this);
		},
		
		_mouseover : function() {
			this.onMouseOver(this);
		},
		
		_onclick : function() {
			this.onClick(this);
		},
		
		getName : function() {
			return this.attr("label");
		},
	
		onMouseOut : function() {
			// Override to handle mouse out events
		},
		
		onMouseOver : function() {
			// Override to handle mouse over events
		},
		
		onClick : function() {
			// Override to handle click events
		},
		
		show : function() {
			// Shows this widget
			this.setVisible(true);
		},
		
		hide : function() {
			// Hides this widget
			this.setVisible(false);
		},
		
		setVisible : function(visible) {
			// Shows/hides this widget
			if(visible)
				dojo.attr(this.domNode, "visibility", "visible");
			else
				dojo.attr(this.domNode, "visibility", "hidden");
		},
		
		focus : function() {
			// Must be here for the tooltipdialog.
			// We dont do any custom focusing so let the
			// tooltip dialog decide which control to gain focus
		},
		
		postCreate : function() {
			this.inherited(arguments);
		}
		
	}
);
