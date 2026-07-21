dojo.provide("tdi.TombstoneGraph");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Toolbar");
dojo.require("dijit.Menu");
dojo.require("dijit.CheckedMenuItem");
dojo.require("dijit.form.Button");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.themes.MiamiNice");
dojo.require("dojox.charting.themes.ThreeD");
dojo.require("dojox.charting.widget.Legend");
dojo.require("dojox.charting.action2d.Tooltip");

dojo.require("tdi.FilteredLogViewer");

dojo.declare("tdi.TombstoneGraph",
		[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
	{
		templatePath: dojo.moduleUrl("tdi", "templates/TombstoneGraph.html"),
		widgetsInTemplate: true,
		
		// maxNodes: integer
		// 		Maximum number of tombstone events to plot
		maxNodes: 20,
		
		// seriesFilter: Array
		// 		The graphs to plot
		seriesFilter: {"get":true, "err":true},
		
		// plotName: string
		//		The plot name used by this widget
		plotName: "default",
		
		toggleGraph : function() {
			var ts = dojo.style(this.TSView, "display");
			if(ts == "none") {
				dojo.style(this.TSView, "display", "");
				dojo.style(this.Graph, "display", "none");
				this.tombstones.resize();
			} else {
				dojo.style(this.TSView, "display", "none");
				dojo.style(this.Graph, "display", "");
			}
		},
		
		createGraph : function() {
			var div = dojo.create("div", {style:"width:100%; height:100%"});
			this._graph = new dojox.charting.Chart2D(div, {markers:true});
			this._graph.addPlot(this.plotName, {
		            type: "Lines", markers:true
	        }).setTheme(dojox.charting.themes.MiamiNice);
			this._graph.addAxis("x", {includeZero: false, minorTicks:false});
			this._graph.addAxis("y", {vertical:true, includeZero: true, min:0});
			this._legend = new dojox.charting.widget.Legend({chart:this._graph, horizontal:false}).placeAt(div, "last");
			this._tooltip = new dojox.charting.action2d.Tooltip(this._graph, this.plotName, {text:dojo.hitch(this, "tooltipFunc")});
			this._graph.connectToPlot(this.plotName, this, "openLogForAssemblyline");
			
			this.createGraphContextMenu();
			
			this.Graph.set("content", div);
		},
		
		createGraphContextMenu : function() {
			var menu = new dijit.Menu({
				targetNodeIds:[this._graph.node]
			});
			var arr = ["get", "err", "add", "del", "lookup", "mod"];
			while((str = arr.pop())) {
				menu.addChild(new dijit.CheckedMenuItem({
					label:this.getString(str),
					checked:this.seriesFilter[str] == true,
					onChange:dojo.hitch(this, function(series, checked) {
						this.updateGraphSettings(series, checked);
					}, str)
				}));
			}
		},
		
		updateGraphSettings : function(oper, set) {
			this.seriesFilter[oper] = set;
			this.updateInternalGraph();
		},
		
		tooltipFunc : function(o) {
			var item = this._items[o.index];
			var stats = item.statistics[0];
			return "<table>" + 
				"<tr><td>" + this.getString("WebCE.started") + ":</td><td>" + this.tombstones.getStore().getValue(item, "started") + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.stopped") + ":</td><td>" + this.tombstones.getStore().getValue(item, "terminated") + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.get") + ":</td><td>" + stats.get + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.add") + ":</td><td>" + stats.add + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.del") + ":</td><td>" + stats.del + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.skip") + ":</td><td>" + stats.skip + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.mod") + ":</td><td>" + stats.mod + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.lookup") + ":</td><td>" + stats.lookup + "</td></tr>" +
				"<tr><td>" + this.getString("WebCE.err") + ":</td><td>" + stats.err + "</td></tr>" +
				"</table>";
		},
		
		updateGraph : function(assemblyline) {
			// update tombstones
			this.assemblyline = assemblyline;
			this.tombstones.selectTarget(this.assemblyline, this.config);
		},
		
		updateInternalGraph : function() {

			var series = {};
			var items = this._items = [];
			var maxNodes = this.maxNodes;
			
			var sortAttributes = [{attribute: "started", descending: true}];
			
			this.tombstones.getStore().fetch({
				query: {
					component:"AssemblyLines/" + this.assemblyline
				},
				queryOptions: {
					deep:true
				},
				sort:sortAttributes,
				onItem:function(item) {
					if(items.length > maxNodes)
						return;
					
					// use unshift since store is sorted on higher dates first
					items.unshift(item);
					var stats = item.statistics[0];
					for(var f in stats) {
						series[f] = series[f] || [];
						series[f].unshift(stats[f]);
					}
				}
			});
			
			var xhigh = 0;
			var yhigh = 0;
			
			for(var f in this.seriesFilter) {
				if(this.seriesFilter[f] && series[f]) {
					var hasData = dojo.some(series[f], function(num) {return num > 0;});
					if(hasData) {
						xhigh = Math.max(xhigh, series[f].length);
						dojo.forEach(series[f], function(num) {
							yhigh = Math.max(yhigh, num);
						});
						this._graph.addSeries(this.getString(f), series[f], {title:"#" + f});
					} else {
						this._graph.removeSeries(this.getString(f));
					}
				} else {
					this._graph.removeSeries(this.getString(f));	
				}
			}

			this._graph.removeAxis("x");
			this._graph.removeAxis("y");
	
			this._graph.addAxis("x", {includeZero: false, minorTicks:false, min:0, max:xhigh+1});
			this._graph.addAxis("y", {vertical:true, includeZero: true, min:0, max:yhigh+(yhigh*0.1)});
			
			this._graph.render();
			this._legend.refresh();
			this._borderContainer.resize();
			
			try {
				var item = this._items[this._items.length-1];
				if(item) {
					this.openLogfile(this.tombstones.getStore().getValue(item, "started"));
				}
			} catch(err) {
			}
		},

		openLogfile : function(logfile) {
			if(logfile) {
				logfile = logfile.replace("T", "__");
				logfile = logfile.replace(/-/g, "_");
				logfile = logfile.replace(/:/g, "_");
				logfile = logfile.replace(/\./g, "_");
			}
			this.Log.openLogfile(this.config, this.assemblyline, logfile, true);
		},
		
		openLogForAssemblyline : function(o) {
			if(o.type == "onclick") {
				var item = this._items[o.index];
				if(item) {
					this.openLogfile(this.tombstones.getStore().getValue(item, "started"));
				}
			}
		},
		
		openLogFromTombstone : function(item, event) {
			var item = this.tombstones.getSelectedItem();
			this.openLogfile(this.tombstones.getItemValue(item, "started"));
		},
		
		resize : function(obj) {
			if(this._borderContainer) {
				this._borderContainer.resize(obj);
			}
		},
		
		postCreate : function() {
			this.seriesFilter = {};
			this.seriesFilter.get = true;
			this.seriesFilter.err = true;
			this.createGraph();
			this.tombstones.onSelectionChanged = dojo.hitch(this, "openLogFromTombstone");
			this.tombstones.onRefreshComplete = dojo.hitch(this, "updateInternalGraph");
		}
	}
);