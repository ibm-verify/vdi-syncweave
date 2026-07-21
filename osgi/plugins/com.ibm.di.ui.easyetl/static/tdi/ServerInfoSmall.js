dojo.provide("tdi.ServerInfoSmall");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.TitlePane");

dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiutil");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.themes.MiamiNice");

dojo.require("tdi.NlsMixin");

dojo.declare("tdi.ServerInfoSmall", [ dijit._Widget, dijit._Templated, tdi.NlsMixin ], {
	// summary:
	//		This widget provides basic info about the server
	//
	// Widget/Templated
	templatePath : dojo.moduleUrl("tdi", "templates/ServerInfoSmall.html"),
	widgetsInTemplate : true,
	
	freeMem: null,
	maxMem: null,
	totalMem: null,

	_timerFunc : function() {
		dojo.when(tdiapi.getVMStatus(), dojo.hitch(this, function(data) {
			this.setSeries(data);
		}));
	},

	resize : function(obj) {
		if(obj && obj.h) {
			this._border.resize(obj);
			if(this._graph) {
				var box = dojo.position(this.Bottom.domNode);
				this._graph.resize(obj.w * 0.4, obj.h - (box.x + box.h + 10));
				var h = box.x + box.h + 15;
				this._graph2.resize(obj.w * 0.2, obj.h - h);
			}
		} else {
			this._border.resize();
			if(this._graph && this._border._borderBox) {
				var obj = this._border._borderBox;
				var box = dojo.position(this.Bottom.domNode);
				this._graph.resize(obj.w * 0.4, obj.h - (box.x + box.h + 10));
				var h = box.x + box.h + 15;
				this._graph2.resize(obj.w * 0.2, obj.h - h);
			}
		}
	},
	
	setSeries : function(data) {
		var used = {y:data.totalMemory - data.freeMemory, text:this.getString("WebCE.vmUsed")};
		var free = {y:(data.maxMemory-data.totalMemory) + data.freeMemory, text:this.getString("WebCE.vmAvailable")};
		this._graph.addSeries("Memory", [used, free]);
		this._graph.render();
		
		this._graph2.addSeries("Threads", [data.activeThreads]);
		this._graph2.render();
		
		this.data = data;
		this._maxmemory.innerHTML = (data.maxMemory / (1024*1024)).toFixed() + "mb";
	},
	
	_openServerInfo : function() {
		// summary:
		//		callback function to open server info
	},
	
	memTooltip : function(o) {
		var used = this.data.totalMemory - this.data.freeMemory;
		var free = (this.data.maxMemory-this.data.totalMemory) + this.data.freeMemory;
		if(o.index == 0) {
			return (used / (1024*1024)).toFixed() + " " + this.getString("WebCE.vmUsed"); 
		} else {
			return (free / (1024*1024)).toFixed() + " " + this.getString("WebCE.vmAvailable"); 
		}
	},
	
	threadsTooltip : function(o) {
		return "" + this.data.activeThreads;
	},
	
	_createGraph : function() {
		this._graph = new dojox.charting.Chart2D(this.Graph);
		this._graph.addPlot("default", {
	            type: "Pie"
        }).setTheme(dojox.charting.themes.MiamiNice);
		this._tooltip = new dojox.charting.action2d.Tooltip(this._graph, "default", {text:dojo.hitch(this, "memTooltip")});

		this._graph2 = new dojox.charting.Chart2D(this.Graph2);
		this._graph2.addPlot("default", {
	            type: "StackedColumns",
	            markers: true,
	            lines: true,
	            gap: 5, minBarSize: 10, maxBarSize: 10
        }).setTheme(dojox.charting.themes.MiamiNice);
		this._graph2.addAxis("y", {vertical: true, fixUpper: "major", includeZero: true});
		this._tooltip2 = new dojox.charting.action2d.Tooltip(this._graph2, "default", {text:dojo.hitch(this, "threadsTooltip")});
},
	
	destroy : function() {
		if(this._timer) {
			this._timer.stop();
			this._timer = null;
		}
		this.inherited(arguments);
	},
	
	postCreate : function() {
		this.inherited(arguments);
		
		this._createGraph();
		
		this.timer = new dojox.timing.Timer(5000);
		this.timer.onTick = dojo.hitch(this, "_timerFunc"); 
		this.timer.start();
		this._timerFunc();
		
		dojo.when(tdiapi.getServerInfo(), dojo.hitch(this, function(data) {
			this._version.innerHTML = data.serverVersion;
		}));
	}
	
});
	