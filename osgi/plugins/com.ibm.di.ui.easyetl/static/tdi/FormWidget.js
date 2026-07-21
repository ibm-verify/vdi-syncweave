dojo.provide("tdi.FormWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Dialog");
dojo.require("dijit.Toolbar");
dojo.require("dijit.TitlePane");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.SimpleTextarea");
dojo.require("dijit.form.Textarea");
dojo.require("dijit.layout.ContentPane");

dojo.require("dojo.parser");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");
dojo.require("tdi.NlsMixin");
dojo.require("tdi.forms._FormWidgetMixin");

dojo.declare("tdi.FormWidget",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin,tdi.forms._FormWidgetMixin],
	{
	// summary:
	//		This class renders a table of input fields for a form definition.
	//		Modifications are immediately written to the configuration.
	
		// Template variables
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/FormWidget.html"),
		
		// hideNullValues: boolean
		//		If true only fields with values are shown in the form
		hideNullValues: false,
		
		// verticalLayout: boolean
		//		If true labels appear on a line by itself with the control on the next
		verticalLayout: false,
		
		// config: tdi.connector
		//		The connector configuration
		config: null,
		
		// formData: Object
		//		The form definition as returned by the REST call
		formData: null,
		
		// _rows: Array
		//		The tr rows created by this widget
		_rows: null,
		
		// _sections: Object
		//		Object with table elements for each section(key)
		_sections: null,
		
		// visibleButtons: Array
		//		An array of booleans that determine the 
		//		visible state of the More/Less, Query, Reset and Test connection
		visibleButtons: [true, true, true, false],
		
		moreLessButtonId: 0,
		queryButtonId: 1,
		resetButtonId: 2,
		testButtonId: 3,
		

		closeForm : function() {
			// summary:
			//		Closes the form.
			// description:
			//		Closes the form.
			// tags:
			//		extension
			dojo.forEach(this._rows, dojo.hitch(this, function(control) {
				dojo.destroy(control);
			}));
			this.hideNullValues = !this.hideNullValues;
			this.createForm();
		},
		
		resetForm : function() {
			// summary:
			//		Resets the form
			// description:
			//		Resets the form
			// tags:
			//		extension
		},
		
		
		reloadForm : function() {
			
		},
		
		querySchema : function(config) {
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
		
		getParamValue : function(param) {
			// summary:
			//		Returns the value for the specified param
			// param:
			//		The param name
			// description:
			//		Default impl returns param from this.config
			if(this.config != null)
				return this.config.getParam(param);
			else
				return "";
		},
		
		setParamValue : function(param, value) {
			// summary:
			//		Sets the value for the specified param
			// param:
			//		The param name
			// vaule:
			//		The param value
			// description:
			//		Default impl sets param in this.config
			if(this.config != null)
				this.config.setParam(param, value);
			if(this.formData.javaClass == "com.ibm.di.connector.AssemblyLineConnector" && param == "assemblyLine") {
				this.reloadForm();
			}
		},
		
		testConnection : function() {
			// summary:
			//		Verifies current parameters by attempting a connection
			//
			alert("Connection successful")
		},
		
		updateControl : function(key) {
			if(this.controls[key] != null) {
				this.controls[key].set("value", this.getParamValue(key));
			}
		},
		
		hasLocalValues : function() {
			// summary:
			//		Checks if the config has any value set for the form's parameters.
			// returns: boolean
			//		True if the config has any value set for the form's parameters.
			var hasValues = false;
			dojo.forEach(this.formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
				var value = this.getParamValue(p.key);
				if(value != null)
					hasValues = true;
			}));
			return hasValues;
		},
		
		createLabelFor: function(id, label) {
			return dojo.create("label", {
				"for":id,
				style:"padding-top:15px",
				innerHTML:label
			});
		},
		
		createControl: function(p, value) {
			var readOnly = !this.isParamEditable(p.key);
			
			if(this.formData.javaClass == "com.ibm.di.connector.AssemblyLineConnector" && p.key == "operationInit") {
				var fields = [];
				this.appendALInitParams(fields);
				if(fields.length > 0) {
					this.createSection({id:"ALInitParams", label:p.label, expanded:true});
					this.createFormFields(fields);
				}
				return;
			}
			
			if (p.option != undefined) {
				var store = {
						label: "label",
						identifier: "value",
						items: []
				}
				dojo.forEach(p.option, function(item) {
					store.items.push({value:item.value, label:tdiutil.getFormLabel(item)});
				});
				control = new dijit.form.ComboBox({
					style:"width:100%",
					value: value,
					searchAttr: "label",	
					readOnly:readOnly,
					required:p.required,
					store:new dojo.data.ItemFileReadStore({data:store})
				});
			} else if(p.type == "boolean") {
				var checked = value;
				if(typeof value == "string")
					checked = value == "true" || value == "on";
				control = new dijit.form.CheckBox({readOnly:readOnly, checked:checked});
			} else if(p.type == "textarea") {
				control = new dijit.form.SimpleTextarea({readOnly:readOnly, value:value, rows:"4", style:"width:98%"});
			} else if(p.type == "editorwindow") {
				control = new dijit.form.SimpleTextarea({readOnly:readOnly, value:value, rows:"10", style:"width:98%"});
			} else if(p.type == "password") {
				control = new dijit.form.TextBox({
					value: value,
					style:"width:100%",
					type:"password",
					readOnly:readOnly
				});
			} else if(p.type == "component") {
				control = new dijit.form.TextBox({readOnly:readOnly, style:"width:100%", value:""});
			} else if(p.type == "static") {
				return null;
			} else if(this.hasCustomControlFor(p.type)) {
				control = this.getCustomControlFor(p.type, {
					style:"width:100%", value:value, formItem:p, config:this.config, formWidget:this,
					label:tdiutil.getFormLabel(p)
				});
			} else {
				control = new dijit.form.TextBox({readOnly:readOnly, style:"width:100%", value:value});
			}
			return control;
		},
		
		getConnectorMode : function() {
			if(this.config.getMode)
				return this.config.getMode();
			if(this.config.getParent() && this.config.getParent().getMode)
				return this.config.getParent().getMode();
			return null;
		},
		
		isParamVisible : function(key) {
			return (this.visibleParams == null || this.visibleParams[key] || this.visibleParams["*"]);
		},
		
		isParamEditable: function(key) {
			return (this.editableParams == null || this.editableParams[key] || this.editableParams["*"]);
		},
		
		isParamExcluded: function(modes) {
			if(!modes)
				return false;
			
			var mode = this.getConnectorMode();
			if(!mode || mode.length == 0)
				return false;

			var arr = modes.split(",");
			var minus = false;
			var include = dojo.some(arr, function(m) {
				if(m.substring(0,1) == "-") {
					minus = true;
					m = m.substring(1);
				}
				if(m == mode)
					return !minus;
				else
					return false;
			});
			if(include)
				return false;
			else
				return !minus;
		},
		
		createFormField : function(p, hideNullValues) {
			// summary:
			//		Creates a row with the label and control for a parameter descriptor(p). If hideNullValue
			//		is true then the row is only created if the config has a value for this parameter.
			// description:
			//		Creates a tr with a td for the label and control
			// returns:
			//		The TR dom node created for the field
			var value = this.getParamValue(p.key);
			if(value == null && hideNullValues && !p.required)
				return;
			
			if(!this.isParamVisible(p.key))
				return;
			
			if(this.isParamExcluded(p.modes))
				return;
			
			if(!value && p.defaultValue)
				value = p.defaultValue;
			
			var control = this.createControl(p, value);
			if(!control)
				return;
			
			var target = this.GeneralTable;
			if(this._sections[p.section] != null)
				target = this._sections[p.section];
			else if(this._sections["General"] != null)
				target = this._sections["General"];
			
			var tr = dojo.create("tr", null, target);
			
			var label = this.createLabelFor(control.id, tdiutil.getFormLabel(p));
			
			if(this.verticalLayout) {
				if(p.type == "boolean") {
					var td = dojo.create("td", {valign:"top", style:"padding-top:8px"}, tr);
					control.placeAt(td);
					dojo.place(label, td);
				} else {
					var td = dojo.create("td", {valign:"top", style:"padding-top:8px"}, tr);
					dojo.place(label, td);
					this._rows.push(tr);
					tr = dojo.create("tr", null, target);
					td = dojo.create("td", {valign:"top"}, tr);
					dojo.style(control.domNode, "width", "66%");
					control.placeAt(td);
				}
			} else {
				var td = dojo.create("td", {valign:"top"}, tr);
				dojo.place(label, td);
				td = dojo.create("td", null, tr);
				control.placeAt(td);
			}
			
			this._rows.push(tr);
			
			var td = dojo.create("td", null, tr);
			if(p.webScript != null) {
				var btn = new dijit.form.Button({label:p.webLabel, onClick:dojo.hitch(this, "performCallback",p)}, td);
			}
			
			// add callback for change events
			if(this.isParamEditable(p.key)) {
				dojo.connect(control, "onChange", dojo.hitch(this, function(param, value) {
					this.setParamValue(param, value);
				}, p.key));
			}
			
			// add tooltip
			var tooltip = tdiutil.getFormTooltip(p)
			if(tooltip && control)
				dojo.style(control.domNode, "label", tooltip);
			
			this.controls[p.key] = control;

			return td;
		},
		
		performCallback : function(item) {
			if(this.callback != null) {
				this.callback(this, item);
			}
		},
		
		createSection : function(section) {
			
			// check if this is a mode section
			if(this.formData.supportedModes) {
				if(dojo.some(this.formData.supportedModes.mode, function(obj) {
					return section.id.indexOf(obj.value + "-") == 0;
				})) {
					if(!this.config.getParent())
						return;
					if(!this.config.getParent().getMode || section.id.indexOf(this.config.getParent().getMode() + "-") != 0)
						return;
				}
			}
			
			// check if section has any params (some forms are sloppy)
			if(!dojo.some(this.formData.parameterMapDescriptor.parameterDescriptor, function(item) {
				return item.section == section.id;
			})) {
				return;
			}

			var title = tdiutil.getFormNLS(section, "label");
			var tr = dojo.create("tr", null, this.GeneralTable);
			this._rows.push(tr);
			var td = dojo.create("td", {colspan:"9"}, tr);
			var table = dojo.create("table", {width:"100%"});
			if(title == "General")
				new dijit.layout.ContentPane({content:table}).placeAt(td);
			else
				new dijit.TitlePane({title:title, open:section.expanded, content:table}).placeAt(td);
			this._sections[section.id] = table;
		},
		
		createSections : function(sections) {
			dojo.forEach(sections, dojo.hitch(this, function(section) {
				this.createSection(section);
			}));
		},
		
		appendALInitParams : function(fields) {
			var al = null;
			var target = this.isSchedule ? this.config.getObject("assemblyLine") : this.config.getParam("assemblyLine");
			if(target)
				al = this.config.getTop().getAssemblyLine(target);
			
			var fd = this.formData.parameterMapDescriptor.parameterDescriptor;
			if(al != null) {
				var initParams = al.getInitParams();
				dojo.forEach(initParams.getNames(), dojo.hitch(this, function(name) {
					var key = (this.isSchedule ? "" : "$initialize.") + name;
					var label = (this.isSchedule ? this.getString(name) : name);
					var sic = initParams.getItem(name);
					var type = null;
					if(sic)
						type = sic.getObject("nativeSyntax");
					if(type == null)
						type = "string";
					
					if(!label && this.isSchedule) {
						if(name == "smtp.Host")
							label = this.getString("mail.smtpHost");
						else if(name == "smtp.User")
							label = this.getString("WebCE.username");
						else if(name == "smtp.Password")
							label = this.getString("WebCE.password");
					}
					
					if(!label)
						label = name;
					
					fields.push(key);
					fd.push({
						key:key,
						label:[{
						   lang:dojo.locale,
						   value:label
						}],
						type:type.toLowerCase(),
						section:"ALInitParams"
					});
				}));
			}
		},
		
		createForm : function() {
			// summary:
			//		Creates the form contents in the template table.
			// description:
			//		The form widget is table with one row pr form field. The form
			//		fields are created in the order specified in the formData
			//		returned in the REST call.
			
			this._rows = new Array();
			
			if(!this.hasLocalValues())
				this.hideNullValues = false;
			
			if(this.hideNullValues)
				this.okButton.set("label", this.getString("more"));
			else
				this.okButton.set("label", this.getString("less"));
			
			//
			// -- special case for adapters where we hide most params
			//
			var showFields = [];
			if(this.isSchedule) {
				this.appendALInitParams(showFields);
				if(this.isSchedule || this.config.getInheritFrom().match(/^adapter:/)) {
					showFields.push("shareLog");
					showFields.push("$GLOBAL.debug");
				}
			}
			
			this._sections = new Object();

			if(showFields.length > 0) {
				this.createFormFields(showFields);
			} else {
				var sections = this.formData.parameterMapDescriptor.sectionDescriptor;
				if(sections != null && !this.hideNullValues && showFields.length == 0) {
					this.createSections(sections);
				}
				dojo.forEach(this.formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
					this.createFormField(p, this.hideNullValues);
				}));
			}
			
		},
		
		createFormFields : function(fields) {
			var defs = {};
			dojo.forEach(this.formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
				if(dojo.indexOf(fields, p.key) != -1)
					defs[p.key] = p;
			}));
			dojo.forEach(fields, dojo.hitch(this, function(p) {
				if(defs[p])
					this.createFormField(defs[p], false);
			}));
		},
		
		setButtonEnabled : function(button, enabled) {
			this.buttons[button].set("disabled", !enabled);
		},
		
		postCreate : function() {
			this.controls = {};
			this.createForm();
			this.buttons = new Array();
			this.buttons.push(this.okButton);
			this.buttons.push(this.queryButton);
			this.buttons.push(this.resetButton);
			this.buttons.push(this.testButton);
			for(var i = 0; i < this.buttons.length; i++) {
				if(!this.visibleButtons[i])
					dojo.style(this.buttons[i].domNode, "display", "none");
			}
			// custom toolbar buttons
			if(this.toolbarButtons !== undefined) {
				dojo.forEach(this.toolbarButtons, dojo.hitch(this, function(button) {
					var btn = new dijit.form.Button(button);
					this.frmToolbar.addChild(btn);
				}));
			}
		}
	}
);
