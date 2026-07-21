define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/_base/html",
	"dojo/dom-class",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Dialog",
	"dijit/Toolbar",
	"dijit/TitlePane",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"dijit/form/FilteringSelect",
	"dijit/form/SimpleTextarea",
	"dijit/form/Textarea",
	"dijit/layout/ContentPane",
	"dijit/registry",
	"tdi/tdiapi",
	"tdi/tdiconfig",
	"tdi/NlsMixin",
	"tdi/forms/_FormWidgetMixin",
	"dojo/text!./templates/FormWidget2.html",
	"tdi/tdiutil"
], function(declare, lang, array, html, domClass, Widget, TemplatedMixin, WidgetsInTemplate, Dialog, Toolbar, TitlePane, Button, CheckBox, FilteringSelect, SimpleTextArea, Textarea, ContentPane,
		registry, tdiapi, tdiconfig, tdiNlsMixin, FormWidgetMixin, template, tdiutil) {

return declare(
	[Widget, TemplatedMixin, WidgetsInTemplate, tdiNlsMixin],
	{
	// summary:
	//		This class renders a table of input fields for a form definition.
	//		Modifications are immediately written to the configuration.
	
		// Template variables
		templateString: template,
		
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
		visibleButtons: [true, false, true],
		
		moreLessButtonId: 0,
//		queryButtonId: 1,
		resetButtonId: 1,
		testButtonId: 2,
		
		autoToggle: {},
		useTabSections: true,
		
		// appendedConfig: Object
		//		keys to config objects used by this form
		appendedConfig: {},
		
		// When adding a parser config we prefix section names with this
		parserLabelPrefix: "", 
		
		// keyRows: Object
		//		Contains the table TR element for the label row (vertical layout) or both label/control (non-vert layout)
		keyRows: {},
		
		// controlRows: Object
		//		Contains the table TR element for the control row in vertical layout mode.
		controlRows: {},
		
		constructor: function(args) {
			lang.mixin(this, args);
			this.keyRows = new Object();
			this.controlRows = new Object();
		},
		
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
			var config = this.config;
			var arr = param.split(":");
			if(arr.length > 1 && this.appendedConfig[arr[0]]) {
				config = this.appendedConfig[arr[0]];
				param = arr[1];
			}
			
			if(config)
				return config.getParam(param);
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
			var config = this.config;
			var cparam = param;
			var arr = param.split(":");
			if(arr.length > 1 && this.appendedConfig[arr[0]]) {
				config = this.appendedConfig[arr[0]];
				cparam = arr[1];
			}
			
			if(config) {
				var prot = this.controls[param] && this.controls[param].type == "password";
				var curval = config.getParam(cparam);
				if(value != curval) {
					config.setParam(cparam, value, prot);
					
					// -- Toggle items?
					this.autoToggleItems(param, value);
					
					this.onModify(cparam, value);
				}
			}
			if(this.formData.javaClass == "com.ibm.di.connector.AssemblyLineConnector" && param == "assemblyLine") {
				this.reloadForm();
			}
		},
		
		autoToggleItems: function(param, value) {
			var t = this;
			var cust = t.autoToggle[param];
			if(cust) {
				array.forEach(cust.enableItems, function(key) {
					// -- Show/Hide the entire row if we have the table row node
					var trow = t.keyRows[key];
					if(trow) {
						trow.style.display = value ? "table-row" : "none";
					}
					var trow = t.controlRows[key];
					if(trow) {
						trow.style.display = value ? "table-row" : "none";
					}
//					var ctl = t.controls[key];
//					if(ctl) {
//						ctl.set("disabled", !value);
//					}
				});
				array.forEach(cust.disableItems, function(key) {
					var trow = t.keyRows[key];
					if(trow) {
						trow.style.display = value ? "none" : "table-row";
					}
					var trow = t.controlRows[key];
					if(trow) {
						trow.style.display = value ? "none" : "table-row";
					}
//					var ctl = t.controls[key];
//					if(ctl) {
//						ctl.set("disabled", value);
//					}
				});
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
		
		createControl: function(formData, p, value) {
			var readOnly = !this.isParamEditable(p.key);
			
			if(formData.javaClass == "com.ibm.di.connector.AssemblyLineConnector" && p.key == "operationInit") {
				var fields = [];
				this.appendALInitParams(fields);
				if(fields.length > 0) {
					this.createSection({id:"ALInitParams", label:p.label, expanded:true});
					this.createFormFields(fields, formData);
				}
				return;
			}
			
			if(p.panel) {
				try {
					var cust = dojo.fromJson(p.panel);
					if(cust && (cust.disableItems || cust.enableItems || cust.toggleItems)) {
						this.autoToggle[p.key] = cust;
					}
					if(cust && cust.placeholder) {
						p.placeholder = cust.placeholder;
					}
					if(cust && cust.widget) {
						p.widget = cust.widget;
					}
				} catch(err) {
					console.log("While parsing panel for " + p.key + ": " + err);
				}
			}
			
			if(FormWidgetMixin.hasCustomControlFor(p.type)) {
				control = FormWidgetMixin.getCustomControlFor(p.type, {
					style:"width:100%", value:value, formItem:p, config:this.config, formWidget:this,
					label:tdiutil.getFormLabel(p)
				});
				
			} else if(FormWidgetMixin.hasCustomControlForParam(p, formData)) {
				var cust = FormWidgetMixin.getCustomControlForParam(p);
				if(cust) {
					try {
						control = new cust({
							style:"width:100%", value:value, formItem:p, config:this.config, formWidget:this,
							label:tdiutil.getFormLabel(p)
						});
					} catch(err) {
						tdiutil.error(err);
					}
				}
					
			} else if (p.option != undefined) {
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
					name:p.key,
					store:new dojo.data.ItemFileReadStore({data:store})
				});
			} else if(p.type == "boolean") {
				var checked = value;
				if(typeof value == "string")
					checked = value == "true" || value == "on";
				control = new dijit.form.CheckBox({readOnly:readOnly, checked:checked, name:p.key});
				
			} else if(p.type == "textarea") {
				control = new dijit.form.SimpleTextarea({
					readOnly:readOnly,
					value:value,
					rows:"4",
					style:"width:98%",
					name:p.key
				});
				
			} else if(p.type == "editorwindow") {
				control = new dijit.form.SimpleTextarea({readOnly:readOnly, value:value, rows:"10", style:"width:98%", name:p.key});
			} else if(p.type == "password") {
				control = new dijit.form.ValidationTextBox({
					value: value,
					style:"width:100%",
					type:"password",
					readOnly:readOnly,
					required:p.required,
					name:p.key
				});
			} else if(p.type == "component") {
				control = new dijit.form.ValidationTextBox({required:p.required, readOnly:readOnly, style:"width:100%", value:"", name:p.key});
			} else if(p.type == "static") {
				return null;
			} else {
				control = new dijit.form.ValidationTextBox({required:p.required, readOnly:readOnly, style:"width:100%", value:value, name:p.key});
			}
			
			if(control && p.placeholder) {
				control.set("placeholder", p.placeholder);
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
		
		createFormField : function(formData, p, hideNullValues) {
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
			
			var control = this.createControl(formData, p, value);
			if(!control && p.type != "static")
				return;
			
			var target = this.GeneralTable;
			if(!this.ignoreSections) {
				if(this._sections[p.section] != null)
					target = this._sections[p.section];
				else if(this._sections["General"] != null)
					target = this._sections["General"];
			}
			
			var tr = dojo.create("tr", null, target);
			domClass.add(tr, "tdiFormRow");
			
			// -- If we have autotoggle then save TR handle so we can display:none it
			this.keyRows[p.key] = tr;
			
			var label = this.createLabelFor(control ? control.id : "", tdiutil.getFormLabel(p));
			
			if(this.verticalLayout) {
				if(p.type == "boolean") {
					var td = dojo.create("td", {valign:"top", style:"padding-top:8px"}, tr);
					control.placeAt(td);
					dojo.place(label, td);
				} else {
					if(p.type == "static") {
						var td = dojo.create("td", {valign:"top", colspan:"99", style:"padding-top:8px"}, tr);
						dojo.place(label, td);
					} else {
						var td = dojo.create("td", {valign:"top", style:"padding-top:8px"}, tr);
						dojo.place(label, td);
						this._rows.push(tr);
						tr = dojo.create("tr", null, target);
						this.controlRows[p.key] = tr;
						td = dojo.create("td", {valign:"top"}, tr);
						dojo.style(control.domNode, "width", "95%");
						control.placeAt(td);
					}
				}
			} else {
				var td = dojo.create("td", {valign:"center", align:"left", style:"white-space:nowrap;"}, tr);
				dojo.place(label, td);
				td = dojo.create("td", {style:"width:100%"}, tr);
				control.placeAt(td);
			}
			
			this._rows.push(tr);
			
			var td = dojo.create("td", null, tr);
			if(p.script && FormWidgetMixin.getScriptHandlerFor(p, this)) {
				var btn = new dijit.form.Button({label:tdiutil.getFormNLS(p, "scriptLabel"), onClick:dojo.hitch(this, "performCallback",p)}, td);
			}
			
			if(control) {
				// add callback for change events
				if(this.isParamEditable(p.key)) {
					dojo.connect(control, "onChange", dojo.hitch(this, "setParamValue", p.key));
				}
				
				// add tooltip
				var tooltip = tdiutil.getFormTooltip(p)
				if(tooltip && control)
					control.set("title", tooltip);
//					dojo.style(control.domNode, "label", tooltip);
				
				this.controls[p.key] = control;
			}

			return td;
		},
		
		performCallback : function(item) {
			var func = FormWidgetMixin.getScriptHandlerFor(item, this);
			if(func) {
				func(this, item);
			} else if(this.callback != null) {
				this.callback(this, item);
			}
		},
		
		createSection : function(formData, section) {
			
			// check if this is a mode section
			if(formData.supportedModes) {
				if(dojo.some(formData.supportedModes.mode, function(obj) {
					return section.id.indexOf(obj.value + "-") == 0;
				})) {
					if(!this.config.getParent())
						return;
					if(!this.config.getParent().getMode || section.id.indexOf(this.config.getParent().getMode() + "-") != 0)
						return;
				}
			}
			
			// check if section has any params (some forms are sloppy)
			if(!dojo.some(formData.parameterMapDescriptor.parameterDescriptor, function(item) {
				return item.section == section.id;
			})) {
				return;
			}

			var title = tdiutil.getFormNLS(section, "label");
			if(section.id.match(/^parser:/)) {
				title = this.parserLabelPrefix + " - " + title;
			}
			
			var table = dojo.create("table", {width:"100%", cellspacing:"5px"});
			
			var cp = new ContentPane({
				content:table, style:"width:100%",
				title:title
			});
			this.formStack.addChild(cp);
			this._sections[section.id] = table;
		},
		
		createSections : function(formData, sections) {
			if(this.ignoreSections)
				return;
			dojo.forEach(sections, dojo.hitch(this, function(section) {
				this.createSection(formData, section);
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
			
			if(this.okButton) {
				if(this.hideNullValues)
					this.okButton.set("label", this.getString("more"));
				else
					this.okButton.set("label", this.getString("less"));
			}
			
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
			
			var formData = this.formData;

			if(showFields.length > 0) {
				this.createFormFields(showFields, formData);
			} else {
				var sections = formData.parameterMapDescriptor.sectionDescriptor;
				if(sections != null && !this.hideNullValues && showFields.length == 0) {
					this.createSections(formData, sections);
				}
				
				// 
				var pd = formData.parameterMapDescriptor.parameterDescriptor;
				var t = this;
				array.forEach(sections, function(sec) {
					var arr = array.filter(pd, function(p) {
						return p.section == sec.id;
					});
					array.forEach(arr, function(p) {
						t.createFormField(formData, p, t.hideNullValues);
					});
				});
				
//				dojo.forEach(this.formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
//					this.createFormField(p, this.hideNullValues);
//				}));
				
				this.FormObject.validate();
			}
			
		},
		
		createFormFields : function(fields, formData) {
			var defs = {};
			dojo.forEach(formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
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
		
		getValue: function() {
			var values = {};
			var t = this;
			array.forEach(t.controls, function(name) {
				values[name] = t.controls[name].get("value"); 
			});
			return values;
		},
		
		setValue: function(obj) {
			var f;
			for(f in obj) {
				if(this.controls[f]) {
					this.controls[f].set("value", obj[f]);
				}
			}
		},
		
		getControl: function(name) {
			return this.controls[name]; 
		},
		
		onModify: function(param, value) {
			// summary:
			//		callback function when form values change
		},
		
		resize: function(obj) {
			this.inherited(arguments);
			if(this.border) {
				this.border.resize();
			}
		},
		
		startup: function() {
			this.inherited(arguments);
			if(this.border) {
				this.border.startup();
			}
		},
		
		appendParser: function(formData, config) {
			var t = this;
			
			// -- remove previous parser object
			var re = new RegExp("^"+this.parserLabelPrefix + " - ");
			array.forEach(t.formStack.getChildren(), function(child) {
				if(child.title.match(re)) {
					t.formStack.removeChild(child);
				}
			});
			
			var sections = {};
			array.forEach(formData.parameterMapDescriptor.parameterDescriptor, dojo.hitch(this, function(p) {
				if(!p.section)
					p.section = "General";
				p.key = "parser:" + p.key
				p.section = "parser:" + p.section;
			}));
			
			t.appendedConfig["parser"] = config;
			
			var sections = formData.parameterMapDescriptor.sectionDescriptor;
			if(sections) {
				array.forEach(sections, function(sec) {
					sec.id = "parser:" + sec.id;
				});
				t.createSections(formData, sections);
			}

			var pd = formData.parameterMapDescriptor.parameterDescriptor;
			array.forEach(sections, function(sec) {
				var arr = array.filter(pd, function(p) {
					return p.section == sec.id;
				});
				array.forEach(arr, function(p) {
					t.createFormField(formData, p, t.hideNullValues);
				});
			});
		},
				
		postCreate : function() {
			this.inherited(arguments);
			this.controls = {};
			this.parserLabelPrefix = this.getString("WebCE.parser");
			this.createForm();
			this.buttons = new Array();
			this.buttons.push(this.okButton);
//			this.buttons.push(this.queryButton);
			this.buttons.push(this.resetButton);
			this.buttons.push(this.testButton);
			for(var i = 0; i < this.buttons.length; i++) {
				if(!this.visibleButtons[i]) {
					if(this.buttons[i])
						dojo.style(this.buttons[i].domNode, "display", "none");
				}
			}
			// custom toolbar buttons
			if(this.toolbarButtons !== undefined) {
				dojo.forEach(this.toolbarButtons, dojo.hitch(this, function(button) {
					var btn = new dijit.form.Button(button);
					this.frmToolbar.addChild(btn);
				}));
			}
			
			// -- inital scan of auto-toggle items
			for(key in this.autoToggle) {
				if(this.getControl(key)) {
					var value = this.getControl(key).get("value");
					this.autoToggleItems(key, value);
				}
			}
		}
	})
});
