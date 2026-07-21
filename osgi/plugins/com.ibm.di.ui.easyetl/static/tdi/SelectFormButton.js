dojo.provide("tdi.SelectFormButton");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.DropDownButton");
dojo.require("dijit.TooltipDialog");
dojo.require("tdi.FormWidget");
dojo.require("tdi.ListSelection");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.SelectFormButton",
	[tdi.NlsMixin],
	{
		// TDI variables
		config: null,
		form: null,
		ls: null,
		dlg: null,
		hideNullValues: true,
		
		loadForm : function(data) {
			if(this.config != null) {
				if(this.form != null)
					this.form.destroy();
				if(this.ls != null)
					this.ls.destroy();
				
				this.form = new tdi.FormWidget({formData:data, config:this.connectionConfig, hideNullValues:this.hideNullValues});
				this.form.closeForm = dojo.hitch(this, function() {
					this.hideNullValues = !this.hideNullValues;
					this.btnDD.closeDropDown();
					this.loadConnectorForm().addCallback(dojo.hitch(this, function() {
						this.btnDD.openDropDown();
					}));
				});

				if(this.dlg == null)
					this.dlg = new dijit.TooltipDialog({title:this.title, content: this.form});
				else
					this.dlg.set("content", this.form);
				
				this.btnDD.dropDown = this.dlg;
				
				if(this.config.getMode() == "Iterator")
					this.btnDD.attr("iconClass","tdiConnectorIteratorImage");
				else
					this.btnDD.attr("iconClass", "tdiConnectorAddOnlyImage");
				
				if(data.useParser == "required" || data.useParser == "optional") {
					dojo.style(this._parser, "display", "");
					this.updateParser();
				} else {
					dojo.style(this._parser, "display", "none");
				}
			}
		},
		
		loadParser : function(data) {
			if(this._parserform != null)
				this._parserform.destroy();
			if(this._parserls != null)
				this._parserls.destroy();
			
			this._parserform = new tdi.FormWidget({formData:data, config:this.config.getConnectionConfig(), hideNullValues:this.hideNullValues});
			this._parserform.closeForm = dojo.hitch(this, function() {
				this.hideNullValues = !this.hideNullValues;
				this.btnDD.closeDropDown();
				this.loadConnectorForm().addCallback(dojo.hitch(this, function() {
					this.btnDD.openDropDown();
				}));
			});

			if(this.dlg == null)
				this.dlg = new dijit.TooltipDialog({title:this.title, content: this.form});
			else
				this.dlg.set("content", this.form);
			
			this.btnDD.dropDown = this.dlg;
			
			if(this.config.getMode() == "Iterator")
				this.btnDD.attr("iconClass","tdiConnectorIteratorImage");
			else
				this.btnDD.attr("iconClass", "tdiConnectorAddOnlyImage");
			
		},
		
		chooseConnectorForm : function(data) {
			var arr = new Array();
			dojo.forEach(data.entry, dojo.hitch(this, function(item) {
				var atom = new tdi.tdiatom({atom:item});
				if(atom.getCategory("connector") != null) {
					var str = item.title.value;
					if(str.match(/^ibmdi/))
						str = str.substring(6);
					arr.push({id:item.id, name:str});
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
			
			if(this.form != null)
				this.form.destroy();
			if(this.ls != null)
				this.ls.destroy();
				
			this.ls = new tdi.ListSelection({content:arr, title:"Select connector"});
			this.ls.list.onChange = dojo.hitch(this, function(item) {
				if(item != null && item.length > 0) {
					try {
						this.config.setConnectorType("system:/Connectors/" + item);
						this.btnDD.attr("label", item);
						this.btnDD.closeDropDown();
						this.loadConnectorForm().addCallback(dojo.hitch(this, function() {
							this.btnDD.openDropDown();
						}));
					} catch(err) {
						alert("setInherit: " + item + ": " + err);
					}
				}
			});
			
			if(this.dlg == null)
				this.dlg = new dijit.TooltipDialog({title:this.title, content: this.ls});
			else
				this.dlg.set("content", this.ls);
			
			this.btnDD.dropDown = this.dlg;
		},
		
		// Called by async get code in execcommand
		errorFunction : function(data) {
			alert("Error: " + data + "\nwhile loading form for: " + this.config.getConnectorType());
		},
		
		getConnType : function() {
			var con = this.config.getConnectorType();
			if(con != null && con.indexOf("/") != -1) {
				con = con.substring(con.lastIndexOf("/") + 1);
			}
			return con;
		},
		
		loadConnectorForm : function() {
			var type = this.getConnType();
			var deferred;
			if(this.config != null && type != "[parent]") {
				this.btnDD.attr("label", type);
				deferred = tdiapi.getConnectorForm(this.getConnType(), "en");
				deferred.addCallback( dojo.hitch(this, "loadForm") );
				deferred.addErrback( dojo.hitch(this, "errorFunction") );
			} else {
				this.btnDD.attr("label", "Select connector...");
				deferred = tdiapi.getInstalledComponents();
				deferred.addCallback( dojo.hitch(this, "chooseConnectorForm") );
				deferred.addErrback( dojo.hitch(this, "errorFunction") );
			}
			return deferred;
		},
		
		loadParserForm : function() {
			var type = this.config.getParserConfig().getParserType();
			if(type != "[parent]") {
				this.btnParser.attr("label", type);
				deferred = tdiapi.getConnectorForm(this.getConnType(), "en");
				deferred.addCallback( dojo.hitch(this, "loadParser") );
				deferred.addErrback( dojo.hitch(this, "errorFunction") );
			} else {
				this.btnDD.attr("label", "Select parser...");
				deferred = tdiapi.getInstalledComponents();
				deferred.addCallback( dojo.hitch(this, "chooseParserForm") );
				deferred.addErrback( dojo.hitch(this, "errorFunction") );
			}
		},
		
		
		postCreate : function() {
			this.loadConnectorForm();
		},
		
	}

);
