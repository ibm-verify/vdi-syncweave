dojo.provide("tdi.LogWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.TitlePane");

dojo.require("dojo.parser");
dojo.require("dojox.timing");

dojo.declare("tdi.LogWidget",
	[dijit._Widget, dijit._Templated],
	{
	// summary:
	//		The log widget shows log messages recieved from a URL and formats each
	//		line in a scrollable table.
	
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		templateString: "<div class='tdiLogWidget' ><table width='100%' dojoAttachPoint='Log'></table></div>",

		// Users should provide a proper title
		label: "Default Title",
		
		// url: String
		// 		Users must provide the URL to the status service
		url: null,
		
		// poll: boolean
		//		True if URL is a poll channel delivering json batch events
		poll: false,
		
		// logoptions: String
		//		Optional parameter with options for logging.
		//		"small" - Only show date and message
		logoptions: null,
		
		_timerFunc : function() {
			dojo.when(dojo.xhrGet({
				url:this.url,
				_preventCache: true,
				handleAs:(this.poll ? "json" : "text")
			}),  dojo.hitch(this, "_loadFunction"), dojo.hitch(this, "_errorFunction"));
		},
		
		_loadFunction : function(data) {
			if(dojo.isString(data)) {
				var arr = data.split("\n");
				for(var i = 0; i < arr.length; i++) {
					var div = dojo.create("tr", {}, this.Log, "last");
					this._formatLogMsg(arr[i], div);
				}
			} else {
				var be = new tdi.tdibatchevent({event:data});
				for(var i = 0; i < be.size(); i++) {
					var event = be.get(i);
					var div = dojo.create("tr", {}, this.Log, "last");
					this._formatLogMsg(event.message, div);
				}
				
				this._timerFunc();
			}
		},
		
		_errorFunction : function(data) {
			if(!this._destroyed)
				this._timerFunc();
		},
		
		_formatLogMsg : function(msg, tr) {
			// summary:
			//		Parses msg and generates three <td>s for date, source and message
			//
			// 13:15:00,015 INFO [AssemblyLine.AssemblyLines/LongRunningMonkey.1] - .....
			// ( DATE  )[skip ..][( SOURCE .....................)]( MESSAGE .... )
			//
			
			dojo.create("td", {colspan:3, innerHTML:msg.replace(/</g, "&lt;")}, tr, "last");
			
//			var arr = msg.match(/(.*),.*\[(.*)\]( -.*)/);
//			if(arr != null && arr.length == 4) {
//				if(this.poll) {
//					var td = dojo.create("td", {valign:"top", innerHTML:arr[1].bold()}, tr, "last");
//					dojo.style(td, "width", "6%");
//				
//					if(this.logoptions != "small") {
//						var source = arr[2].match(/^AssemblyLine\.AssemblyLines\/(.*)/);
//						if(source != null) {
//							var td = dojo.create("td", {valign:"top", innerHTML:source[1]}, tr, "last");
//							dojo.style(td, "width", "10%");
//						} else {
//							dojo.create("td", {valign:"top", innerHTML:arr[2]}, tr, "last");
//							dojo.style(td, "width", "10%");
//						}
//						td = dojo.create("td", {valign:"top", innerHTML:arr[3].replace(/</g, "&lt;")}, tr, "last");
//					} else {
//						td = dojo.create("td", {valign:"top", innerHTML:arr[3].replace(/</g, "&lt;")}, tr, "last");
//					}
//					dojo.style(td, "width", "50%");
//				} else {
//					td = dojo.create("td", {colspan:3, valign:"top", innerHTML:arr[1].bold() + arr[3].replace(/</g, "&lt;")}, tr, "last");
//				}
//				
//			} else {
//				dojo.create("td", {colspan:3, innerHTML:msg.replace(/</g, "&lt;")}, tr, "last");
//			}
		},
		
		postCreate : function() {
			if(this.poll) {
				this._timerFunc();
			} else {
				this.timer = new dojox.timing.Timer(5000);
				this.timer.onTick = dojo.hitch(this, "_timerFunc"); 
				this.timer.start();
			}
			this.inherited("postCreate", arguments);
		},
		
		destroy : function() {
			if(this.timer != null) {
				this.timer.stop();
			}
			this.inherited("destroy", arguments);
		}
	
});
