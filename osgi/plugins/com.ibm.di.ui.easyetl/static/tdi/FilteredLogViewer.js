dojo.provide("tdi.FilteredLogViewer");

dojo.require("dojo.data.ItemFileReadStore");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.ProgressBar");
dojo.require("dijit.Toolbar");
dojo.require("dijit.TooltipDialog");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.form.HorizontalSlider");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.ComboButton");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.layout.BorderContainer");
dojo.require("dijit.layout.ContentPane");

dojo.require("dojox.charting.Chart2D");
dojo.require("dojox.charting.themes.MiamiNice");
dojo.require("dojox.charting.widget.Legend");
dojo.require("dojox.charting.action2d.Tooltip");

dojo.require("tdi.TreeTableWidget");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.FilteredLogViewerNode", [dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		templateString:"<td valign='top'><span dojoAttachPoint='_expando' dojoAttachEvent='onclick:_toggle' class='dijitTreeExpando dijitTreeExpandoClosed' style='cursor:pointer'></span><span dojoAttachPoint='msg'></span><div dojoAttachPoint='content' style='display:none'></div></td>",
		
		_toggle: function() {
			if(dojo.style(this.content, "display") == "none") {
				dojo.style(this.content, "display", "");
				dojo.removeClass(this._expando, "dijitTreeExpandoClosed");
				dojo.addClass(this._expando, "dijitTreeExpandoOpened");
			} else {
				dojo.style(this.content, "display", "none");
				dojo.removeClass(this._expando, "dijitTreeExpandoOpened");
				dojo.addClass(this._expando, "dijitTreeExpandoClosed");
			}
		},
		
		postCreate: function() {
			var type = this.logmsg.type;
			if(type && this.typeColors[type]) {
				dojo.style(this.msg, "color", this.typeColors[type]);
			}
			
			if(type != "ERROR") {
				this._toggle();
			}

			if(this.logmsg.content) {
				this._expando.innerHTML = "&nbsp;&nbsp;&nbsp;&nbsp;";
				this.content.innerHTML = "<pre>" + this.logmsg.content + "</pre>";
			}
			if(this.showSource && this.logmsg.source)
				this.msg.innerHTML = this.logmsg.source + " " + this.logmsg.text;
			else
				this.msg.innerHTML = this.logmsg.text;
		}
	}
);

dojo.declare("tdi.FilteredLogViewer",
		[dijit._Widget, dijit._Templated, tdi.NlsMixin ],
	{
		templatePath: dojo.moduleUrl("tdi", "templates/FilteredLogViewer.html"),
		widgetsInTemplate: true,
		
		// hideFileButton: boolean
		//		Hides the file drop-down button
		hideFileButton: false,
		
		// hideToolbar: boolean
		//		Hides the entire toolbar (true when input is from non-searchable stream)
		hideToolbar: false,
		
		// cientry: tdi.cientry
		//		If specified this widget will read log messages
		//		off the config instance log
		cientry: null,
		
				
		lastChunk: 1024 * 5,
		_nextMessage: 0,
		_isPolling: false,
		
		// The element to scroll into view after loading log file
		initialPosition: null,
		
		typeColors: {
			"ERROR": "red",
			"WARN": "grey",
			"DEBUG": "Sea Green",
			"USER": "blue"
		},
		
		openLogfile : function(config, alname, file) {
			if(config == null) {
				dojo.when(tdiapi.getServerLogs(), dojo.hitch(this, function(logs) {
					if(file == null)
						this.logfile = logs.items[0];
					else
						this.logfile = dojo.filter(logs.items, function(log) {return log.name == file})[0];
					
					var start = this.logfile.size - (this._getPageSize() * 100);
					if(start < 0)
						start = 0;
					this.options = {
							config:null,
							assemblyline:null,
							logfile: this.logfile.name,
							location: {
								start:start,
								count: this._getPageSize()
							}
					};
					this._isPolling = true;
					this._updateFilter();
					this.updateLogfiles(logs);
				}), tdiapi.defaultErrHandler);
			} else {
				dojo.when(tdiapi.getLogfileEntry(file, config, alname), dojo.hitch(this, function(data) {
					this.logfile = data;
					var start = this.logfile.size - (this._getPageSize() * 100);
					if(start < 0)
						start = 0;
					this.options = {
							logfile: file,
							config: config,
							assemblyline: alname,
							location: {
								start:start,
								count: this._getPageSize()
							}
					};
					this._isPolling = true;
					this._updateFilter();
				}));
			}
		},
		
		_showDetails : function() {
			var str = "<table>";
			for(var f in this.logfile) {
				str += "<tr><td>" + f + "</td><td>";
				if(f == "modified")
					str += new Date(this.logfile[f]);
				else if(f == "path")
					str += "<a href='/dashboard/log/download/" + this.logfile.path + "' target='_blank'>" + this.logfile.path + "</a>";
				else
					str += this.logfile[f];
				str += "</td></tr>";
			}
			
			str += "</table>";
			tdiutil.alert(str);
		},
		
		_createSearchLink : function(value, elem) {
			var label = value + " ";
			var arr = value.match(/\[AssemblyLine.AssemblyLines\/(.*)\]/);
			if(arr && arr.length == 2) {
				label = "[Assemblyline: " + arr[1] + "] ";
			}
			return dojo.create("span", {
				style:"cursor:pointer",
				innerHTML:label,
				onclick:dojo.hitch(this, "_updateSourceFilter", value)
			}, elem);
		},
		
		_createLineLink : function(line, table) {
			var tr = dojo.create("tr", {}, table);
			var td = dojo.create("td", {}, tr);
			
			var span = dojo.create("span", {
				style:"cursor:pointer",
				title:line,
				innerHTML:this.options.logfile + ":" + line,
				onclick:dojo.hitch(this, function() {
					var start = line - 10;
					if(start < 0)
						start = 0;
					this.options.location = {
							start:start,
							count: this._getPageSize()
					};
					this.initialPosition = line;
					this.startPolling(false);
					var str = this._text.get("value");
					this._text.set("value", "");
					if(str == "")
						this._updateFilter();
			})}, td);
			return tr;
		},	
		
		_updateSourceFilter : function(value) {
			this.startPolling(false);
			this.options.source = value;
			var str = this._text.get("value");
			this._text.set("value", "");
			if(str == "")
				this._updateFilter();
		},
		
		_updateLocation : function() {
			var pos = this._progress.get("value");
			if(pos && pos != -1) {
				this.options.location = {
						start:pos,
						count: this._getPageSize()
				};
				this._updateFilter();
			}
		},
		
		_pageTop : function() {
			this.startPolling(false);
			this.options.location = {
					start:0,
					count: this._getPageSize()
			};
			this.initialPosition = 0;
			this._updateFilter();
		},
		
		_pageNext : function() {
			this.startPolling(false);
			this.options.location = {
					start:this.getLastMsg(),
					count: this._getPageSize()
			};
			this.initialPosition = this.getLastMsg();
			this._updateFilter();
		},
		
		_pagePrev : function() {
			this.startPolling(false);
			var start = this.getFirstMsg() - (this._getPageSize() * 100);
			if(start < 0)
				start = 0;
			this.initialPosition = start;
			this.options.location = {
					start:start,
					count: this._getPageSize()
			};
			this._updateFilter();
		},
		
		_pageTail : function() {
			if(this._timer) {
				clearTimeout(this._timer);
				delete this._timer;
			}
			this.options.location = {
				start: this._getEndPage(),
				count: this._getPageSize()
			}
			this._isPolling = true;
			this.initialPosition = null;
			this._updateFilter();
		},
		
		startPolling : function(set) {
			this._isPolling = set;
			if(set)  {
				this._timer = setTimeout(dojo.hitch(this, "_pollLog"), 5000);
			} else if(this._timer) {
				clearTimeout(this._timer);
				delete this._timer;
			}
		},
		
		_getPageSize : function() {
			var numLines = this._pageSize.get("value");
			if(numLines && !isNaN(numLines)) {
				if(numLines > 0)
					return numLines + 1;
			}
			this._pageSize.set("value", "100");
			return 100;
		},
		
		_getEndPage : function() {
			var loc = this.logfile.size - (this._getPageSize() * 100);
			if(loc < 0)
				loc = 0;
			return loc;
		},
		
		_pollLog : function() {
			if(this._destroyed)
				return;
			
			if(this.url) {
				// get pollchannel data
			} else {
				// request file size for current file
				dojo.when(tdiapi.getLogfileEntry(this.logfile.name, this.options.config, this.options.assemblyline), dojo.hitch(this, function(data) {
					if(this.logfile.modified != data.modified) {
						this.logfile.size = data.size;
						this.logfile.modified = data.modified;
						this._pageTail();
					} else {
						this.startPolling(true);
					}
				}));
			}
		},
		
		getLastMsg : function() {
			if(this.data && this.data.result && this.data.result.length > 0) {
				var itm = this.data.result[this.data.result.length-1];
				var pos = itm.endline;
				return pos;
			}
			return 0;
		},
		
		getFirstMsg : function() {
			if(this.data && this.data.result && this.data.result.length > 0) {
				return this.data.result[0].line;
			}
			return 0;
		},
		
		isPolling : function() {
			return this._isPolling;
//			return this._polling.get("value") == "on";
		},
		
		_updateFilter : function() {
			this.options.qualifiers = {
				error:this._error.get("value") == "on", 
				warn:this._warn.get("value") == "on", 
				info:this._info.get("value") == "on", 
				debug:this._debug.get("value") == "on" 
			};
			this.options.regex = this._text.get("value");
			if(this.options.regex == "" || this.options.regex == "*")
				this.options.regex = null;
			
			if(!this.options.location) {
				this.options.location = {
						start: 0,
						count: this._getPageSize()
				};
			}
			
			if(this.filter) {
				if(this.filter.source) {
					this.options.source = this.filter.source;
				}
			}
			
			// clear and create full height table
			this._clearTables();
			table = this._createTable({width:"100%", height:"100%"});
			var tr = dojo.create("tr", null, table);
			dojo.create("td", {innerHTML:"<center><img src='images/processing-toolbar.gif'></img></center>"}, tr);
			
			dojo.when(tdiapi.getTDILogFile(this.options), dojo.hitch(this, "_filterLog"),
				dojo.hitch(this, function(err) {
					this._clearTables();
					table = this._createTable({width:"100%", xheight:"100%"});
					var tr = dojo.create("tr", null, table);
					var str = err;
					if(err.responseText != null) {
						var arr = err.responseText.match(/<title>(.*)<\/title>/);
						if(arr != null && arr.length > 1) {
							str = arr[1];
						} else if(err.responseText != "") {
							str = msg.responseText;
						}
					}
					dojo.create("td", {innerHTML:str}, tr);
				})
			);
			
			this.options.source = null;
			this.options.location = null;
		},
		
		_clearTables : function() {
			dojo.forEach(this._tables, function(table) {
				dojo.destroy(table);
			});
			this._tables = [];
		},
		
		_createTable : function(option) {
			var style = option || {cellspacing:"5px"};
			var table = dojo.create("table", style, this.Log);
			this._tables.push(table);
			return table;
		},
		
		_filterLog : function(data) {

			if(data && data.result && data.result.lenght == 0 && this.isPolling()) {
				this.startPolling(this.isPolling());
				return;
			}
			
			this.data = data;
			
			this.updateButtons();
			
			//
			// -- Check if we have the entire logfile
			//
			var lastmsg = this.getLastMsg();
			if(lastmsg >= this.logfile.size && this.getFirstMsg() == 0) {
				dojo.style(this._progress.domNode, "display", "none");
			} else {
				dojo.style(this._progress.domNode, "display", "");
			}
			
			this._clearTables();
			var table = this._createTable();
			
			var showSource = this._showSource.get("value") == "on";
			var showTime = this._showTime.get("value") == "on";
			var showDate = this._showDate.get("value") == "on";
			
			var nextMessageNumber = null;
			var scrollIntoView = null;
			var lastTR = null;
			
			if(data.result && data.result.length > 0) {
				this._progress.set("minimum", 0);
				if(this.logfile.size > 0)
					this._progress.set("maximum", this.logfile.size);
				else
					this._progress.set("maximum", 100);
				this._progress.onChange = function() {};
				this._progress.set("value", data.result[0].line);
				setTimeout(dojo.hitch(this, function() {
					this._progress.onChange = dojo.hitch(this, "_updateLocation")
				}), 500);
			}

			dojo.forEach(data.result, dojo.hitch(this, function(msg) {
				var display = "";
				if(showDate)
					display += msg.date + " ";
				if(showTime)
					display += msg.time + " ";
				
				// If we are searching and get fragments of the log we create a visual
				// to indicate where in the log the fragment is.
				if(nextMessageNumber && nextMessageNumber != msg.line && this.options.regex != null) {
					//table = this._createTable();
					var ltr = this._createLineLink(msg.msgno, table);
				}
				nextMessageNumber = msg.endline;
				
				var tr = dojo.create("tr", null, table);
				var td1 = dojo.create("td", {
					valign:"top",
					innerHTML:display,
					title:msg.line,
					style:"white-space: nowrap; color:grey"
				}, tr);
				
				if(msg.content) {
					new tdi.FilteredLogViewerNode({logmsg:msg, typeColors:this.typeColors, showSource:showSource}).placeAt(tr);
				} else {
					var td = dojo.create("td", {valign:"top"}, tr);
					if(msg.type && this.typeColors[msg.type])
						dojo.style(td, "color", this.typeColors[msg.type]);
					if(msg.source && msg.source.match(/.*AssemblyLines\//)) {
						if(!msg.text.match(/^CTGD/)) {
							dojo.style(td, "color", this.typeColors.USER);
						}
					}
					if(msg.source && showSource)
						this._createSearchLink(msg.source + " - ", td);
					dojo.create("span", {innerHTML:msg.text}, td);
				}
				
				if(this.initialPosition && this.initialPosition <= msg.line) {
					scrollIntoView = tr;
					delete this.initialPosition;
				}
				
				lastTR = tr;
			}));

			if(data.result.length == 0 && !this.isPolling()) {
				var tr = dojo.create("tr", null, table);
				dojo.create("td", {innerHTML:"<center>***</center>"}, tr);
			} else if(scrollIntoView) {
				dojo.window.scrollIntoView(scrollIntoView);
				dojo.style(scrollIntoView, "background-color", "#FFF8C6");
			} else if(lastTR) {
				dojo.window.scrollIntoView(lastTR);
			}
			
			this._nextMessage = nextMessageNumber || this._nextMessage;
			this.startPolling(this.isPolling());
		},
		
		updateLogfiles : function(logs) {
			dojo.forEach(this._files.getChildren(), dojo.hitch(this, function(m) {
				this._files.removeChild(m);
			}));
			dojo.forEach(logs.items, dojo.hitch(this, function(log) {
				var item = new dijit.MenuItem({
					label:log.name + ":   " + Math.round(log.size/1024) + "KB, " + new Date(log.modified),
					onClick:dojo.hitch(this, "openLogfile", null, null, log.name)
				})
				this._files.addChild(item);
			}));
			this._filesbutton.set("label", this.logfile.name);
		},
		
		_CIPoll : function() {
			if(!this.table) {
				this._clearTables();
				this.table = this._createTable();
			}
			
			dojo.when(dojo.xhrGet({
				url:this.url,
				_preventCache: true,
				handleAs:"json"
			}),  dojo.hitch(this, "_CIDataReceived"), dojo.hitch(this, function(err) {
				// timeout - retry for more messages
				this.logReadTimeout(err);
				if(!this._destroyed && !this._stopped)
					this._CIPoll();
			}));
		},
		
		logReadTimeout : function(err) {
			// summary:
			//		Called when there was an error reading the log
			//		By default the logger will retry the read as it is
			//		often a timeout unless the err code is 404 not found.
			if(err.status && err.status == "404") {
				this.stop();
			}
		},
		
		_CIDataReceived : function(data) {
			if(dojo.isString(data)) {
				var arr = data.split("\n");
				for(var i = 0; i < arr.length; i++) {
					console.log(arr);
				}
			} else {
				var be = new tdi.tdibatchevent({event:data});
				var logdata = new Array();
				for(var i = 0; i < be.size(); i++) {
					var event = be.get(i);
					if(event.message) {
						// 10:35:02,043 INFO  [TDIDashboard_TEMP_JSON_1337848497691] - CTGDIS038I System termination requested by external process.
						var arr = event.message.match(/(.*)\s*(DEBUG|INFO|ERROR|WARN)\s*\[(.*)\]\s*-\s*(.*)/);
						if(arr && arr.length == 5) {
							var msg = {
									type:arr[2],
									date:arr[1],
									source:arr[3],
									text:arr[4]
							};
							
							var tr = dojo.create("tr", null, this.table);
							var td1 = dojo.create("td", {
								valign:"top",
								innerHTML:msg.date,
								style:"white-space: nowrap; color:grey"
							}, tr);
							
//							if(msg.content) {
//								new tdi.FilteredLogViewerNode({logmsg:msg, typeColors:this.typeColors, showSource:showSource}).placeAt(tr);
//							} else {
								var td = dojo.create("td", {valign:"top"}, tr);
								if(msg.type && this.typeColors[msg.type])
									dojo.style(td, "color", this.typeColors[msg.type]);
								if(msg.source && msg.source.match(/.*AssemblyLines\//)) {
									if(!msg.text.match(/^CTGD/)) {
										dojo.style(td, "color", this.typeColors.USER);
									}
								}
//								if(msg.source && showSource)
//									this._createSearchLink(msg.source + " - ", td);
								dojo.create("span", {innerHTML:msg.text}, td);
//							}
						}
					}
				}
				this._CIPoll();
			}
		},
		
		stop : function() {
			this._stopped = true;
		},
		
		updateButtons : function() {
			var top = this.getFirstMsg();
			var bot = this.getLastMsg();
			this._btnTop.set("disabled", (top == 0));
			this._btnPrv.set("disabled", (top == 0));
			this._btnNxt.set("disabled", bot >= this.logfile.size);
		},
		
		resize : function(obj) {
			if(this._borderContainer) {
				if(!obj) {
					obj = {w:this.domNode.clientWidth, h:this.domNode.clientHeight};
				}
				this._borderContainer.resize(obj);
			}
		},
		
		destroy : function() {
			if(this._timer) {
				clearTimeout(this._timer);
				delete this._timer;
			}
			this.inherited(arguments);
		},
		
		postCreate : function() {
			dojo.connect(this._text.textbox, "onkeyup", dojo.hitch(this, function(event) {
				if(event.keyCode == 13) {
					this._updateFilter();
				}
			}));
			
			// disable arrows as key navigators in the toolbar
			delete this._toolbar._keyNavCodes[dojo.keys.LEFT_ARROW];
			delete this._toolbar._keyNavCodes[dojo.keys.RIGHT_ARROW];
			this._toolbar.connectKeyNavHandlers([dojo.keys.TAB], [dojo.keys.TAB]);

			this._progress.onChange = dojo.hitch(this, "_updateLocation");
			
			if(this.hideFileButton) {
				dojo.style(this._filesbutton.domNode, "display", "none");
			}
			if(this.hideToolbar || this.url) {
				dojo.style(this._toolbar.domNode, "display", "none");
				dojo.style(this._progress.domNode, "display", "none");
			}
			if(this.url) {
				this._CIPoll();
			}
		}
	}
);
