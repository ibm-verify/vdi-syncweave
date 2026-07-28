/*
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * Copyright contributors to the SyncWeave project
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     1.18, 3/26/12
 * @owner       
 * @history
 */
define([
    	"dojo/_base/declare",
    	"dojo/_base/lang",
    	"dojo/_base/array",
    	"dojo/_base/html",
    	"dojo/date/locale",
    	"dojo/store/Memory",
    	"dijit/Dialog",
    	"tdi/DialogContent",
    	"dijit/form/Button",
    	"dijit/form/ComboBox",
    	"dijit/form/TextBox",
    	"dijit/form/FilteringSelect",
    	"dijit/form/ValidationTextBox",
    	"dojo/data/ItemFileReadStore",
    	"idx/form/Select",
    	"idx/widget/Dialog",
    	"tdi/tdiapi",
    	"tdi/tdiconfig",
    	"tdi/tdiconstants",
    	"tdi/ConfigBrowser",
    	"tdi/TableWidget",
    	"tdi/NlsMixin"
    ], function(declare, lang, array, html, dateLocale, Memory, Dialog, DialogContent, Button, ComboBox, TextBox, FilteringSelect, ValidationTextBox, ItemFileReadStore, Select, idxDialog, tdiapi, tdiconfig, tdiconstants, ConfigBrowser, TableWidget, tdinls) {

	var tdiutil = {

	};
	
	declare.safeMixin(tdiutil, new tdinls());
	
	var closeButtonLabel = tdiutil.getString("WebCE.ok");
	
	try {
		tdiutil.recentFiles = dojo.fromJson(dojo.cookie("TDIDashboard_recentfiles")) || {};
	} catch(err) {
	}
	
	if(!lang.isArray(tdiutil.recentFiles)) {
		tdiutil.recentFiles = [];
	}
	
	tdiutil.generateInstanceId = function(config, prefix) {
		var name = null;
		if(config != null && config.getConfigName)
			name = config.getConfigName();
		else if(config != null && config.getName)
			name = config.getName();
		
		//var uniqueId = (prefix ? prefix : "") + (name != null ? name : "Temp") + "_" + new Date().getTime();
		var uniqueId = "TDIDashboard_TEMP_" + (name != null ? name : "") + "_" + new Date().getTime();
		return uniqueId;
	};
	
	tdiutil.clone = function(obj) {
		// summary:
		//		Clone an object
		// obj: null or javascript object
		//		Call with null to clone this.config
	    if (obj == null || typeof obj != "object") return obj;
	    if (obj.constructor != Object && obj.constructor != Array) return obj;
	    if (obj.constructor == Date || obj.constructor == RegExp || obj.constructor == Function ||
	        obj.constructor == String || obj.constructor == Number || obj.constructor == Boolean)
	        return new obj.constructor(obj);

	    var cloned = new obj.constructor();
	    
	    for (var prop in obj)
	    {
    		cloned[prop] = tdiutil.clone(obj[prop]);
	    }

	    return cloned;
	};
	
	tdiutil.getFormTooltip = function(formItem /* Object */, lang /* String */) {
		return tdiutil.getFormNLS(formItem, "description", lang);
	};
	
	tdiutil.getFormLabel = function(formItem /* Object */, lang /* String */) {
		var str = tdiutil.getFormNLS(formItem, "label", lang);
		if(formItem.required)
			str = "<span style='color:red'>*</span> " + str;
		return str;
	};
	
	tdiutil.getFormNLS = function(formItem /* Object */, field /* String */, lang /* String */) {
		// summary:
		//		Returns the label for the language code from the form definition item.
		// formItem: Object
		//		The form item from the parameter descriptor
		// lang: String
		//		The language code. If null the browser's language is used
		if(!formItem[field])
			return null;
		
		var code = lang == null ? dojo.locale : lang;
		
		// Note that the SDI server api uses <lang>_<COUNTRY> and Dojo uses <lang>-<country>
		// so we have map it first.
		var codes = code.split("-");
		if(codes && codes.length > 1) {
			code = codes[0]+ "_" + codes[1].toUpperCase();
		}
		
		var str = null;
		dojo.forEach(formItem[field], function(label) {
			if(label.lang == code) {
				str = label.value; // p.required ? labs.value + " *" : labs.value;
			}
		});
		
		// -- Try language without country
		var code = lang == null ? dojo.locale : lang;
		if(!str && code && code.indexOf("-") != -1) {
			str = tdiutil.getFormNLS(formItem, field, code.substring(0,2));
		}
		
		// -- On no match fall back to "en"
		if(str == null && lang != "en")
			return tdiutil.getFormNLS(formItem, field, "en");
		
		
		return str;
	};

	tdiutil.openDialog = function(params /* Object */, title /* String */, template /* String */, handler /* function */) {
		// summary:
		//		Opens a dialog with the provided template and returns the posted
		//		results of the dialog (or null if dialog was cancelled).
		// params: Object
		//		The initial params for the dialog
		// title: String
		//		The dialog title
		// template: String
		//		The dialog template (e.g. template.html relative to the dialogs directory)
		// handler: function
		//		The function to call on OK
		
		var templatePath = dojo.moduleUrl("tdi", "dialogs/" + template);
		var content = new DialogContent({templatePath:templatePath});
		content.handleOK = function(formData) {
			dlg.hide();
			handler(formData);
		}
		var parms = {
			title:title,
			content:content,
			style:"width: 300px",
			execute:function(data) {
				if(handler)
					handler(data);
			}
		};
		if(params)
			declare.safeMixin(parms, params);
		
		var dlg = new Dialog(parms);
		content.handleCancel = function() {
			dlg.onCancel();
		};
		dlg.show();
	};
	
	
	tdiutil.dataValueArray2Object = function(arr) {
		// summary:
		//		Converts an array of name/value objects to a single object using the
		//		name as property name for the value.
		var obj = {};
		for(var i = 0; i < arr.length; i++) {
			obj[arr[i].name] = arr[i].value;
		}
		return obj;
	};
	
	tdiutil.getCookie = function(name) {
		// summary:
		//		Returns the cookie prefixed with TDI_WEBCE
		return dojo.cookie("TDI_DASHBOARD." + name);
	};

	tdiutil.setCookie = function(name, value) {
		// summary:
		//		Sets a cookie prefixed with TDI_WEBCE
		dojo.cookie("TDI_DASHBOARD." + name, value, {expire:7});
	};

	tdiutil.ask = function(msg, callback, title) {
		// summary:
		//		Invokes callback(true|false) depending on user clicking OK/Cancel
		idx.confirm({text:msg, title:title ? title : "Confirm", closeButtonLabel:tdiutil.closeButtonLabel}, function() {
			callback(true);
		}, function() {
			callback(false);
		});
	};
	
	tdiutil.confirm = function(msg, callback) {
		idx.confirm({text:msg, title:"Confirm", closeButtonLabel:tdiutil.closeButtonLabel}, function() {
			// backwards compat with messagedialog
			callback(0);
		}, function() {
			callback(1);
		});
//		var msgDlg = new dojoe.messagedialog.MessageDialog({
//			message: msg,
//			messageId: "",
//			theme:"claro",
//			callback: callback,
//			buttons: [tdiutil.getString("yes"), tdiutil.getString("no")],
//			type: "Confirm"
//		});
//		msgDlg.show();
	};
	
	tdiutil.askYesNoCancel = function(msg, callback) {
		return tdiutil.confirm(msg, callback);
//		var msgDlg = new dojoe.messagedialog.MessageDialog({
//			message: msg,
//			messageId: "",
//			theme:"claro",
//			callback: callback,
//			buttons: [tdiutil.getString("yes"), tdiutil.getString("no"), tdiutil.getString("cancel")],
//			type: "Confirm"
//		});
//		msgDlg.show();
	};
	
	tdiutil.messageDialogResponse = function(buttonId, messageId, checked) {
		this._dialogResponse = {
				buttonId:buttonId,
				messageId:messageId,
				checked:checked
		};
	};
	
	tdiutil.warning = function(msg) {
		return tdiutil.alert(msg, "Warn");
	};
	
	tdiutil.getErrorMsg = function(msg) {
		// summary:
		//		Returns a human readable string from an exception object
		var str = msg;
		var details = "";
		if(msg.response && msg.response.data && msg.response.data.message) {
			return msg.response.data.message + "<p>" + msg.response.data.exception;
		} else if(msg.responseText) {
			var arr = msg.responseText.match(/<title>(.*)<\/title>/);
			if(arr != null && arr.length > 1) {
				str = arr[1];
			} else if(msg.responseText != "") {
				str = msg.message;
				try {
					var obj = dojo.fromJson(msg.responseText);
					if(obj && obj.exception && obj.message)
						str = obj.message + "<p>" + obj.exception;
				} catch(err) {}
			}
		} else if(msg.message) {
			str = msg.message;
		} else {
			str = msg.toString();
		}
		return str;
	}
	
	tdiutil.error = function(err) {
		// summary:
		//		Shows the 
		idx.error(tdiutil.getErrorMsg(err), null, tdiutil.closeButtonLabel);
	};
	
	tdiutil.alert = function(msg, level) {
		if(level == null)
			level = "Info";
		
		idx.info({text: msg, title: level, closeButtonLabel:tdiutil.closeButtonLabel});
	};
	
	tdiutil.selectDialog = function(arr, label, onexecute, requestName, title) {
		var sarr = dojo.map(arr, function(str) {
			if(dojo.isObject(str))
				return str;
			else
				return {name:str, id:str};
		});
		
		var store = new ItemFileReadStore({
			data: {
				identifier: "id",
				label: "name",
				items : sarr
			}
		});
		
		var div = dojo.create("div", {style:"padding:10px"});
//		dojo.create("div", {innerHTML:label + "<p></p>"}, div);
		
		tdiutil.lastSelectionValue = new Select({
			store:store
		}).placeAt(div, "last");
		
		if(requestName) {
			dojo.create("div", {innerHTML:tdiutil.getString("ConfigTable.Name") + " "}, div);
			tdiutil.lastNameValue = new TextBox({}, div);
		} else if(tdiutil.lastNameValue) {
			tdiutil.lastNameValue.destroy();
			tdiutil.lastNameValue = null;
		}
		
		dojo.create("div", {innerHTML:"<p></p>"}, div);
		
		new Button({
			label:tdiutil.getString("WebCE.ok"),
			type:"submit"
		}).placeAt(div);
		
		var cancel = new Button({
			label:tdiutil.getString("WebCE.cancel")
		}).placeAt(div);
		
		var dialog = new Dialog({
			title:label,
			content:div,
			onExecute:function() {
				if(tdiutil.lastNameValue)
					onexecute(tdiutil.lastSelectionValue.get("value"), tdiutil.lastNameValue.get("value"));
				else
					onexecute(tdiutil.lastSelectionValue.get("value"));
			},
			style:"width:300px"
		});
		
		cancel.onClick = function() {
			dialog.onCancel();
		};

		dialog.show();
	};
	
	tdiutil.selectFromTable = function(arr, title, success) {
		var dlg = null;
		var buttons = [];
		buttons.push(new Button({
			label:this.getString("WebCE.ok"),
			onClick:function() {
				var item = browser.getSelectedItem();
				if(item)
					success(item.id[0]);
				dlg.onCancel();
			}
		}));
		var data = array.map(arr, function(key) {
			return {id:key};
		});
		var browser = new TableWidget({
			structure: [{id:"id", name:"Value", width:"auto"}],
			style:"height:150px"
		});
		browser.setData(data);
		browser.startup();
		dlg = new idxDialog({
			title:title,
			closeButtonLabel:this.getString("WebCE.cancel"),
			content:browser,
			buttons:buttons,
			onShow:function() {
				var box = dojo.contentBox(dlg.containerNode);
				browser.resize({h:box.h});
			}
		});
		dlg.show();
	};
	
	tdiutil.selectFromFilteringSelect = function(arr, title, success, initvalue) {
		var dlg = null;
		var buttons = [];
		var data = array.map(arr, function(key) {
			return {id:key, label:key, value:key};
		});
		
		var store = new Memory({
			data: data
		});
		
		var select = new FilteringSelect({
			store:store,
			searchAttr:"value",
			value:initvalue,
			style:"width:100%"
		});
		
		buttons.push(new Button({
			label:this.getString("WebCE.ok"),
			onClick:function() {
				if(success) {
					success(select.get("value"));
				}
				dlg.onCancel();
			}
		}));
		dlg = new idxDialog({
			title:title,
			closeButtonLabel:this.getString("WebCE.cancel"),
			content:select,
			buttons:buttons
		});
		dlg.show();
	};
	
	tdiutil.isConnectorConfigured = function(conn) {
		// summary:
		//		Returns false if conn is not configured (inherits from [parent])
		//		or has missing required parameters.
		if(conn == null)
			return false;
		
		var con = conn.getConnectorType();
		if(!con)
			return false;
		
		if(con.indexOf("/") != -1) {
			con = con.substring(con.lastIndexOf("/") + 1);
		}
		return (con != "[parent]" && con != "[no inheritance]");
	};

	tdiutil.createNewAttribute = function(map, until, store, callback) {
		var arr = map.getAssemblyLine().getWorkAttributes(until);
		var sarr = [];
		
		// only include attributes that are not mapped
		dojo.forEach(arr, function(str) {
			if(!map.isMapped(str))
				sarr.push({name:str, id:str});
		});
		
		var comboStore = new ItemFileReadStore({
			data: {
				identifier: "id",
				label: "name",
				items : sarr
			}
		});
		
		var div = dojo.create("div", {style:"padding:10px"});
		dojo.create("div", {innerHTML:tdiutil.getString("ConfigTable.Name") + "<p></p>"}, div);
		
		var combo = new ComboBox({
			store:comboStore
		}).placeAt(div, "last");
		
		dojo.create("div", {innerHTML:"<p></p>"}, div);
		
		var okButton = new Button({
			label:tdiutil.getString("ok"),
			type:"submit"
		}).placeAt(div);
		
//		dojo.connect(combo, "onChange", function() {
//			var str = combo.get("value");
//			if(str == null || str == "")
//				okButton.set("disabled", true);
//			else
//				okButton.set("disabled", false)
//		});
		
		var dialog = new Dialog({
			title:tdiutil.getString("AttributeMap.toolbar.Add.tooltip"),
			content:div,
			onExecute:function() {
				var str = combo.get("value");
				if(str != null && str != "") {
					if(!map.isMapped(str)) {
						var item = map.newItem({name:str});
						var newitem = store.put({id:item.getName(), name:item.getName(), mapping:str, value:""});
						if(callback !== undefined) {
							callback(newitem);
						}
					}
				}
			},
			style:"width:300px"
		});
		
		dialog.show();
	};
	
	tdiutil.getConnectorType = function(config) {
		var con = config.getConnectorType();
		if(con != null && con.indexOf("/") != -1) {
			con = con.substring(con.lastIndexOf("/") + 1);
		}
		return con;
	};
	
	tdiutil.getComponentIconURL = function(comp, def) {
		var image = def ? def : "/fds/static/images/Connector_Iterator_Enabled.gif";
		if(comp.isScript && comp.isScript())
			image = "/fds/static/images/Script_16.gif";
		else if(comp.getMode)
			image = "/fds/static/images/Connector_" + comp.getMode() + "_Enabled.gif";
		else if(comp.isBranch && comp.isBranch())
			image = "/fds/static/images/Branch_Enabled.gif";
		
		return image;
	};
	
	tdiutil.getRecentFiles = function() {
		return tdiutil.recentFiles;
	};
	
	tdiutil.addRecentFiles = function(solution, alname) {
		var deleted = false;
		tdiutil.recentFiles = array.filter(tdiutil.recentFiles, function(obj) {
			if(obj.solution == solution && obj.assemblyline == alname) {
				deleted = true;
				return false;
			}
			return true;
		});

		tdiutil.recentFiles.unshift({solution:solution, assemblyline:alname});
		if(tdiutil.recentFiles.length > 10 && !deleted) {
			tdiutil.recentFiles.pop();
		}
		dojo.cookie("TDIDashboard_recentfiles", dojo.toJson(tdiutil.recentFiles));
		dojo.publish(tdiconstants.recentFilesSubject, [tdiutil.recentFiles]);
	};
	
	tdiutil.getComponentType = function(config) {
		var con = null;
		if(config && config.getConnectionConfig) {
			if(config.getConnectionConfig().getParam("$form$")) {
				con = config.getTop().getConfigName() + ":" + config.getName();
			} else {
				con = config.getConnectorType();
				if(con != null && con.indexOf("/") != -1) {
					con = con.substring(con.lastIndexOf("/") + 1);
				}
			}
		}
		return con;
	};
	
	tdiutil.formatDate = function(time, type) {
		// summary:
		//		Returns a locale specific string reprensentation of time
		// time: Date
		//		The date
		// type: String
		//		The format type (short, long)
		return dateLocale.format(time, type ? type : "short");
	};
	
	tdiutil.formatNum = function(num) {
		return num < 10 ? "0" + num : ""+num;
	};

	tdiutil.createAssemblyLine = function(value, cbOk) {
		var pf = [
		    {label:"Project:", type:"text", value:value ? value.split(":")[0] : "CustomFDS"},
		    {label:"AssemblyLine:", type:"text"}
		];
		tdiutil.prompt(pf, function(values) {
			var config = values[0];
			var alname = values[1];
			var centry = null;
			tdiapi.getServerProjects().then(function(data) {
				var entry = array.filter(data.items, function(item) {
					return item.id == config; 
				});
				if(entry && entry.length == 1) {
					return entry[0];
				} else {
					var cfg = new tdiconfig({});
					cfg.setConfigName(config);
					return tdiapi.createSolution(tdiconfig);
				}
				
			}).then(function(configEntry) {
				centry = configEntry;
				return tdiapi.checkoutConfig(configEntry);
				
			}).then(function(config) {
				// -- create assemblyline
				var cfg = new tdiconfig({config:config});
				if(cfg.createAssemblyLine(alname)) {
					return tdiapi.checkinConfig(centry, config.config.solution);
				}
				
			}).then(function() {
				return tdiapi.unlockConfig(centry);
				
			}).then(function() {
				cbOk(config, alname);
				
			});
		}, "Create AssemblyLine");
	};
	
	
	tdiutil.selectAssemblyLine = function(label, value, cbOk, createALButton) {
		var dlg = null;
		var buttons = [];
		
		var browser = new ConfigBrowser({
			style:"width:100%; height:100%"
		});
		
		//
		if(createALButton) {
			buttons.push(new Button({
				label:tdiutil.getString("WebCE.newAssemblyLine"),
				onClick:function() {
					tdiutil.createAssemblyLine(browser.selectedConfig, function(config, al) {
						cbOk(config+":/AssemblyLines/"+al);
						dlg.onCancel();
					});
				}
			}));
		}
		
		var btn = new Button({
			label:tdiutil.getString("WebCE.ok"),
			onClick:function() {
				dlg.onExecute();
			}
		});
		buttons.push(btn);

		dlg = new idxDialog({
			title:label,
			closeButtonLabel:tdiutil.getString("WebCE.cancel"),
			content:browser,
			buttons:buttons,
			onExecute:function() {
				cbOk(browser.get("value"));
			}
		});
		browser.set("value", value);
		dlg.show();
	};
	
	tdiutil.prompt = function(label, cbOk, title) {
		// summary:
		//		Shows a modal dialog requesting input string
		
		var div = html.create("div");
		var arr = null; 
		if(lang.isArray(label))
			arr = label;
		else if(lang.isObject(label))
			arr = [label];
		else
			arr = [{label:label,type:"text"}];
		
		var controls = [];
		
		array.forEach(arr, function(item) {
			if(typeof(item) == "string") {
				item = {label:item};
			}
			html.create("div", {innerHTML:item.label}, div);
			var control = null;
			if(!item.type || item.type == "text") { 
				control = new ValidationTextBox({
					value:item.value ? item.value : "",
					style:"width:30em",
					regExp:item.regExp ? item.regExp : ""
//					tabIndex:item.tabIndex?item.tabIndex:-1
				}).placeAt(div);
				
			} else if(item.type == "password") {
				control = new ValidationTextBox({
					value: item.value ? item.value : "",
					style:"width:30em",
					type:"password"
//					tabIndex:item.tabIndex?item.tabIndex:-1
				}).placeAt(div);
			}
			controls.push(control);
		});
		
		var dlg = null;
		var buttons = [];
		buttons.push(new Button({
			label:this.getString("WebCE.ok"),
			onClick:function() {
				var valid = true;
				array.forEach(controls, function(c) {
					if(c.isValid && !c.isValid())
						valid = false;
				});
				if(valid)
					dlg.onExecute();
			}
		}));
		dlg = new idxDialog({
			title:title || "Dialog",
			closeButtonLabel:this.getString("WebCE.cancel"),
			content:div,
			buttons:buttons,
			onExecute:function() {
				var value = [];
				array.forEach(controls, function(c) {
					value.push(c.get("value"));
				});
				cbOk(value);
			}
		});
		dlg.show();
	};
	
	tdiutil.parseALInitParam = function(item) {
		// summary:
		//		Parses item fields and returns a copy of item.
		//		The item fields may contain simple values or json encoded objects.
		//		JSON encoded objects are merged into the returned object.
		if(!item)
			return item;
		
		var retItem = {
			getObject: function(p) {
				return this[p];
			}
		};
		for(var f in item.config) {
			var value = item.config[f];
			retItem[f] = value;
			if(value && value.trim().match(/{.*}/)) {
				try {
					var obj = dojo.fromJson(value);
					retItem = lang.mixin(retItem, obj);
				} catch(err) {
				}
			}
		}
		
		// allow common aliases 
		retItem.comment = retItem.comment || retItem.description;
		retItem.nativeSyntax = retItem.nativeSyntax || retItem.syntax;
		
		return retItem;
	}
	return tdiutil;

});	
