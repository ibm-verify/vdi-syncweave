dojo.provide("tdi.ServerLog");

dojo.require("dojox.timing");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("tdi.tdiapi");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.ServerLog", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		This widget provides a view of the ibmdi.log on the server.
	//
	
	// Widget/Templated
	templateString : "<div><pre dojoAttachPoint='Log'></pre></div>",
	widgetsInTemplate : true,
	
	onTick : function() {
		dojo.when(tdiapi.getServerLog(), dojo.hitch(this, function(data) {
			this.Log.innerHTML = this.Log.innerHTML + data;
		}));
	},
	
	destroy : function() {
		if(this.timer != null) {
			this.timer.stop();
			this.timer = null;
		}
		this.inherited("destroy", arguments);
	},
	
	postCreate : function() {
		this.timer = new dojox.timing.Timer(5000);
		this.timer.onTick = dojo.hitch(this, "onTick");
		this.timer.start();
	}

});
