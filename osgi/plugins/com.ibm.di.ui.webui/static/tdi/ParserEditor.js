define([
    "dojo/_base/declare",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/FilteringSelect",
	"tdi/NlsMixin",
	"tdi/FormWidget",
	"tdi/model/ComponentsModel",
	"tdi/tdiapi",
	"tdi/tdiutil"
], function(declare, Widget, TemplatedMixin, WidgetsInTemplate, FilteringSelect, tdiNlsMixin, tdiFormWidget, tdiComponentsModel, tdiapi, tdiutil) {
	
return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, tdiNlsMixin],
	{
	// summary:
	//		A widget for basic editing of a parser.
	// description:
	//		The widget configures a parser

	// Widget/Templated
	templateString : "<div dojoAttachPoint='Form'></div>",
	

	_getParserType : function() {
		var con = this.config.getParserConfig().getParserType();
		if(con != null && con.indexOf("/") != -1) {
			con = con.substring(con.lastIndexOf("/") + 1);
		}
		return con;
	},
	
	_loadParserForm : function() {
		var type = this._getParserType();
		if(this.config != null && type && type != "[parent]") {
			dojo.when(tdiapi.getConnectorForm(this._getParserType(), "en"), dojo.hitch(this, "_loadForm"));
		} else {
			this._resetForm();
		}
	},
	
	_getTitle : function(data) {
		return tdiutil.getFormNLS(data, "name");
	},
	
	_loadForm : function(data) {
		if(this._parserform != null)
			this._parserform.destroy();
		var visibleButtons = [true, true, false];
		this._parserdiv = dojo.create("div", {innerHTML:this.getString("Branch_Type") +this._getTitle(data), style:"border-bottom:1px solid #cdcdcd"}, this.Form);
		this._parserform = new tdiFormWidget({formData:data, config:this.config.getParserConfig().getConfig(), hideNullValues:this.hideNullValues,
			visibleButtons:visibleButtons, verticalLayout:true});
		this._parserform.resetForm = dojo.hitch(this, "_resetForm");
		this._parserform.placeAt(this.Form, "last");
	},
	
	_resetForm : function() {
		if(this._parserform != null) {
			this._parserform.destroy();
			this._parserform = null;
			dojo.destroy(this._parserdiv);
		}
		
		this._selectForm = dojo.create("div", {style:"width:100%;height:100%;position:relative; top:20px; left:20px; "}, this.Form);
		dojo.create("div", {innerHTML:"Select parser"}, this._selectForm);
		this._modelCB = new FilteringSelect({
			store:new tdiComponentsModel({componentType:"parser"})
		}).placeAt(this._selectForm);
		this._modelCB.onChange = dojo.hitch(this, function(item, label) {
			if(item != null && item.length > 0) {
				try {
					this.selectedParser = label;
					this.config.getParserConfig().setParserType("system:/Parsers/" + item);
					this._modelCB.destroy();
					this._modelCB = null;
					dojo.destroy(this._selectForm);
					this._loadParserForm();
				} catch(err) {
					alert("setInherit: " + item + ": " + err);
				}
			}
		});
	},
	
	postCreate : function() {
		this._loadParserForm();
	}

	})
})
