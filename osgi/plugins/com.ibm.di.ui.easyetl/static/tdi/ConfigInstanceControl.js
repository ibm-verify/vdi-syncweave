dojo.provide("tdi.ConfigInstanceControl");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.ContentPane");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.ConfigInstanceControl",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// Widget and Templated
		widgetsInTemplate: true,
		templateString: "<div dojoType='dijit.layout.ContentPane'><span dojoAttachPoint='_name'></span><img dojoAttachPoint='_icon' style='float:right; width:12px; height:12px'></img><img dojoAttachPoint='_sched' src='images/schedule.gif' style='float:right; width:12px; height:12px;'></img></div>",
		
		_onClick: function(event) {
			dojo.stopEvent(event);
			this.onClick(this.name, this);
		},
		
		_onSchedClick: function(event) {
			dojo.stopEvent(event);
			this.onScheduleClick(this.name, this);
		},
		
		onClick: function() {
			
		},
		
		onScheduleClick: function() {
			
		},
		
		disableRun: function() {
			dojo.style(this._icon, "display", "none");
		},
		
		setRunning: function(running) {
			this._icon.src = running ? "images/Stop.gif" : "images/Run.gif";
		},
		
		postCreate: function() {
			this._name.innerHTML = this.name;
			if(!this.showSchedule)
				dojo.style(this._sched, "display", "none");
			this.setRunning(this.running);
			dojo.connect(this._icon, "onclick", this, "_onClick");
			dojo.connect(this._sched, "onclick", this, "_onSchedClick");
		}
	}
);
		
