dojo.provide("tdi.SelectComponent");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.TooltipDialog");
dojo.require("tdi.FormWidget");
dojo.require("tdi.ListSelection");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.tdiatom");

dojo.declare("tdi.SelectComponent",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// Must have this since we use widgets in the template
		widgetsInTemplate: true,
		
		// title: String
		//		The label for the connector config button
		title: "Source",

		// hideNullValues: boolean
		//		If true, the form widget will hide parameters
		//		with no values.
		hideNullValues: false,
		visibleButtons: [true, true, true, false],
		
		// config: tdi.connectorconfig
		//		The connector configuration
		config: null,
		
		// decorate: boolean
		//		If true, the background is decorated with Tree style background
		decorate: true,
		
		// The connector form
		_form: null,
		
		// The parser form/dialog/listselection
		_parserform: null,
		_parserdlg: null,
		_parserls: null,
		
		// The connector form/dialog/listselection
		_connectorform: null,
		_connectordlg: null,
		_connectorls: null,
		
		// Widget template
		templatePath: dojo.moduleUrl("tdi", "templates/SelectComponent.html"),
		
		querySchema : function() {
			// summary:
			//		Query the connector's schema using
			//		the current configuration.
			// config:
			//		The connector's current configuration
			// tags:
			//		extension
			// returns:
			//		Schema based on current configuration
			//		
			
		},
		
		onConnectorChanged : function() {
			// summary:
			//		Called when connector has been reset
			//
		},
		
		openDropDown : function() {
			// summary:
			//		Opens the connector dropdown dialog
			if(this.btnDD.state != "Opened") {
				this.btnDD.openDropDown();
			}
		},
		
		closeDropDown : function() {
			this.btnDD.closeDropDown();
		},
		
		_getCompNameLabel : function(label) {
			if(label.match(/^ibmdi/))
				return label.substring(6);
			else
				return label;
		},
		
		_loadForm : function(data) {
			if(this.config != null) {
				if(this._connectorform != null)
					this._connectorform.destroy();
				if(this._connectorls != null)
					this._connectorls.destroy();
				
				this._connectorform = new tdi.FormWidget({style:"overflow:scroll", formData:data, config:this.config.getConnectionConfig(), hideNullValues:this.hideNullValues, visibleButtons:this.visibleButtons});
				this._connectorform.closeForm = dojo.hitch(this, function() {
					this.hideNullValues = !this.hideNullValues;
					 this.closeDropDown();
					this._loadConnectorForm().addCallback(dojo.hitch(this, function() {
						setTimeout(dojo.hitch(this, "openDropDown"), 300);
					}));
				});
				
				this._connectorform.resetForm = dojo.hitch(this, function() {
					 this.closeDropDown();
					this.config.setConnectorType("[parent]");
					dojo.when(this._loadConnectorForm(), dojo.hitch(this, function() {
						this.onConnectorChanged();
					}));
				});
				
				this._connectorform.querySchema = dojo.hitch(this, "querySchema");

				if(this._connectordlg == null)
					this._connectordlg = new dijit.TooltipDialog({title:this.title, content: this._connectorform});
				else
					this._connectordlg.set("content", this._connectorform);
				
				this.btnDD.dropDown = this._connectordlg;
				
				if(this.config.getMode() == "Iterator")
					this.btnDD.attr("iconClass","tdiConnectorIteratorImage");
				else
					this.btnDD.attr("iconClass", "tdiConnectorAddOnlyImage");
				
				if(data.useParser == "required" || data.useParser == "optional") {
					dojo.style(this._parser, "display", "");
					this.__loadParserForm();
				} else {
					dojo.style(this._parser, "display", "none");
					this._clearParser();
				}
			}
		},
		
		_clearParser : function() {
			if(this._parserform != null)
				this._parserform.destroy();
			if(this._parserls != null)
				this._parserls.destroy();
			this._parserform = null;
			this._parserls = null;
		},
		
		_loadParser : function(data) {
			this._clearParser();
			this._parserform = new tdi.FormWidget({formData:data, config:this.config.getParserConfig().getConfig(), hideNullValues:this.hideNullValues});
			this._parserform.closeForm = dojo.hitch(this, function() {
				this.hideNullValues = !this.hideNullValues;
				this.btnParser.closeDropDown();
				this.__loadParserForm().addCallback(dojo.hitch(this, function() {
					this.btnParser.openDropDown();
				}));
			});
			this._parserform.resetForm = dojo.hitch(this, function() {
				this.btnParser.closeDropDown();
				this.config.getParserConfig().setParserType("[parent]");
				this.__loadParserForm().addCallback(dojo.hitch(this, function() {
					this.btnParser.openDropDown();
				}));
			});

			this._parserform.querySchema = dojo.hitch(this, "querySchema");
			
			if(this._parserdlg == null)
				this._parserdlg = new dijit.TooltipDialog({title:this.title, content: this._parserform});
			else
				this._parserdlg.set("content", this._parserform);
			
			this.btnParser.dropDown = this._parserdlg;
		},
		
		_filterComponentNames : function(data, category) {
			var arr = new Array();
			dojo.forEach(data.entry, dojo.hitch(this, function(item) {
				var atom = new tdi.tdiatom({atom:item});
				if(atom.getCategory(category) != null) {
					var str = item.title.value;
					var label = this._getCompNameLabel(str);
					arr.push({id:str, name:label});
				}
			}));
			
			arr = arr.sort(function(a,b) {
				if(a.name == b.name)
					return 0;
				else if(a.name < b.name)
					return -1;
				else
					return 1;
			});
			return arr;
		},
		
		_chooseConnectorForm : function(data) {
			var arr = this._filterComponentNames(data, "connector");
			if(this._connectorform != null)
				this._connectorform.destroy();
			if(this._connectorls != null)
				this._connectorls.destroy();
				
			this._connectorls = new tdi.ListSelection({content:arr, title:"Select connector"});
			this._connectorls.list.onChange = dojo.hitch(this, function(item, label) {
				if(item != null && item.length > 0) {
					try {
						this.config.setConnectorType("system:/Connectors/" + item);
						this.btnDD.attr("label", this._getCompNameLabel(item));
						 this.closeDropDown();
						dojo.when(this._loadConnectorForm(), dojo.hitch(this, function() {
							this.onConnectorChanged();
						}));
					} catch(err) {
						alert("setInherit: " + item + ": " + err);
					}
				}
			});
			
			if(this._connectordlg == null)
				this._connectordlg = new dijit.TooltipDialog({title:this.title, content: this._connectorls});
			else
				this._connectordlg.set("content", this._connectorls);
			
			this._clearParser();
			dojo.style(this._parser, "display", "none");
			
			this.btnDD.dropDown = this._connectordlg;
		},
		
		_chooseParserForm : function(data) {
			var arr = this._filterComponentNames(data, "parser");

			this._clearParser();
				
			this._parserls = new tdi.ListSelection({content:arr, title:"Select parser"});
			this._parserls.list.onChange = dojo.hitch(this, function(item) {
				if(item != null && item.length > 0) {
					try {
						this.config.getParserConfig().setParserType("system:/Parsers/" + item);
						this.btnParser.attr("label", this._getCompNameLabel(item));
						this.btnParser.closeDropDown();
						this.__loadParserForm().addCallback(dojo.hitch(this, function() {
							this.btnParser.openDropDown();
						}));
					} catch(err) {
						alert("setInherit: " + item + ": " + err);
					}
				}
			});
			
			if(this._parserdlg == null)
				this._parserdlg = new dijit.TooltipDialog({title:this.title, content: this._parserls});
			else
				this._parserdlg.set("content", this._parserls);
			
			this.btnParser.dropDown = this._parserdlg;
		},
		
		// Called by async get code in execcommand
		_errorFunction : function(data) {
			alert("Error: " + data + "\nwhile loading form for: " + this.config.getConnectorType());
		},
		
		_getConnType : function() {
			if(this.config) {
				var con = this.config.getConnectorType();
				if(con != null && con.indexOf("/") != -1) {
					con = con.substring(con.lastIndexOf("/") + 1);
				}
				return con;
			}
		},
		
		_getParserType : function() {
			var con = this.config.getParserConfig().getParserType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
			return con;
		},
		
		_loadConnectorForm : function() {
			var type = this._getConnType();
			var deferred;
			if(this.config != null && type != "[parent]") {
				this.btnDD.attr("label", this._getCompNameLabel(type));
				deferred = tdiapi.getConnectorForm(this._getConnType(), "en");
				deferred.addCallback( dojo.hitch(this, "_loadForm") );
				deferred.addErrback( dojo.hitch(this, "_errorFunction") );
			} else {
				this.btnDD.attr("label", "Select connector...");
				deferred = tdiapi.getInstalledComponents();
				deferred.addCallback( dojo.hitch(this, "_chooseConnectorForm") );
				deferred.addErrback( dojo.hitch(this, "_errorFunction") );
			}
			return deferred;
		},
		
		__loadParserForm : function() {
			var type = this._getParserType();
			if(type != "[parent]") {
				this.btnParser.attr("label", this._getCompNameLabel(type));
				deferred = tdiapi.getParserForm(type, "en");
				deferred.addCallback( dojo.hitch(this, "_loadParser") );
				deferred.addErrback( dojo.hitch(this, "_errorFunction") );
			} else {
				this.btnParser.attr("label", "Select parser...");
				deferred = tdiapi.getInstalledComponents();
				deferred.addCallback( dojo.hitch(this, "_chooseParserForm") );
				deferred.addErrback( dojo.hitch(this, "_errorFunction") );
			}
			return deferred;
		},
		
		
		postCreate : function() {
			this._loadConnectorForm();
			if(!this.decorate) {
				dojo.removeClass(this.Main, "dijitTreeRow");
				dojo.removeClass(this.Main, "dijitTreeRowSelected");
			}
		}
	}
);
