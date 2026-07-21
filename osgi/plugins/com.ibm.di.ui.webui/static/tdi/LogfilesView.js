dojo.provide("tdi.LogfilesView");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.form.NumberTextBox");

dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiapi");
dojo.require("tdi.LogfilesWidget");
dojo.require("tdi.FilteredLogViewer");

dojo.declare("tdi.LogfilesView",
	[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
	// summary:
	//		The treetable widget is a basic widget to manage a TED treetable
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templateString: "<div dojoAttachPoint='Main'></div>",
		
		resize : function(obj) {
			if(this._borderContainer !== null) {
				this._borderContainer.resize(obj);
			}
		},

		showLogFile : function(assemblyline, logfile, kbytes) {
			this._logviewer.openLogfile(this.config.getConfigName(), assemblyline, logfile, kbytes, false);
		},
		
		selectTarget : function(assemblyline) {
			this.logwidget.setFilter(assemblyline);
		},

		postCreate : function() {
			this._borderContainer = new dijit.layout.BorderContainer({style:"width:100%, height:100%"}).placeAt(this.Main);
			
			child = new dijit.layout.ContentPane({region:"leading", splitter:true, style:"width:30%"}).placeAt(this._borderContainer);
			this._borderContainer.addChild(child);
			var tp = new tdi.LogfilesWidget({config:this.config});
			tp.showLogFile = dojo.hitch(this, "showLogFile");
			try {
				child.set("content", tp);
			} catch(err) {
				alert(err);
			}
			this.logwidget = tp;
			
			this._logviewer = new tdi.FilteredLogViewer({hideFileButton:true});
			child = new dijit.layout.ContentPane({region:"center", splitter:true});
			this._borderContainer.addChild(child);
			child.set("content", this._logviewer);
			this._borderContainer.startup();
			
		}

});