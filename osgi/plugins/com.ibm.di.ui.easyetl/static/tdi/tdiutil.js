/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2010
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

if (!dojo._hasResource["tdi.tdiutil"]) {
	dojo._hasResource["tdi.tdiutil"] = true;

	dojo.provide("tdi.tdiutil");
	dojo.require("dijit.Dialog");
	dojo.require("tdi.DialogContent");
	dojo.require("dijit.form.Button");
	dojo.require("dijit.form.ComboBox");
	dojo.require("dojo.data.ItemFileReadStore");
	dojo.require("dojoe.messagedialog.MessageDialog");
	
	tdiutil = {

	};
	
	
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
			str = "* " + str;
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
		
		var nls = lang == null ? dojo.locale : lang;
		var str = null;
		dojo.forEach(formItem[field], function(label) {
			if(label.lang == nls) {
				str = label.value; // p.required ? labs.value + " *" : labs.value;
			}
		});
		
		// -- Try language without country
		if(!str && nls && nls.indexOf("-") != -1) {
			str = tdiutil.getFormNLS(formItem, field, nls.substring(0,2));
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
		var content = new tdi.DialogContent({templatePath:templatePath});
		var dlg = new dijit.Dialog({
			title:title,
			content: content,
			style: "width: 300px",
			execute: handler
		});
		content.handleOK = function(formData) {
			dlg.hide();
			handler(formData);
		}
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

	tdiutil.confirm = function(msg, callback) {
		var nls = new tdi.NlsMixin();
		var msgDlg = new dojoe.messagedialog.MessageDialog({
			message: msg,
			messageId: "",
			theme:"claro",
			callback: callback,
			buttons: [nls.getString("yes"), nls.getString("no")],
			type: "Confirm"
		});
		msgDlg.show();
	};
	
	tdiutil.askYesNoCancel = function(msg, callback) {
		var nls = new tdi.NlsMixin();
		var msgDlg = new dojoe.messagedialog.MessageDialog({
			message: msg,
			messageId: "",
			theme:"claro",
			callback: callback,
			buttons: [nls.getString("yes"), nls.getString("no"), nls.getString("cancel")],
			type: "Confirm"
		});
		msgDlg.show();
	};
	
	tdiutil.messageDialogResponse = function(buttonId, messageId, checked) {
		tdiutil._dialogResponse = {
				buttonId:buttonId,
				messageId:messageId,
				checked:checked
		};
	};
	
	tdiutil.warning = function(msg) {
		return tdiutil.alert(msg, "Warn");
	};
	
	tdiutil.error = function(msg) {
		var str = msg;
		if(msg.responseText != null) {
			var arr = msg.responseText.match(/<title>(.*)<\/title>/);
			if(arr != null && arr.length > 1) {
				str = arr[1];
			} else if(msg.responseText != "") {
				str = msg.responseText;
			}
		}
		
		return tdiutil.alert(str, "Error");
	};
	
	tdiutil.alert = function(msg, level) {
		if(level == null)
			level = "Info";
		var msgDlg = new dojoe.messagedialog.MessageDialog({
			message: msg,
			messageId: "",
			theme:"claro",
			callback: tdiutil.messageDialogResponse,
			type: level
		});
		msgDlg.show();
	};
	
	tdiutil.selectDialog = function(arr, label, onexecute) {
		var sarr = dojo.map(arr, function(str) {
			return {name:str, id:str};
		});
		
		var store = new dojo.data.ItemFileReadStore({
			data: {
				identifier: "id",
				label: "name",
				items : sarr
			}
		});
		
		var div = dojo.create("div", {style:"padding:10px"});
		dojo.create("div", {innerHTML:label + "<p></p>"}, div);
		
		tdiutil.lastSelectionValue = new dijit.form.ComboBox({
			store:store
		}).placeAt(div, "last");
		
		dojo.create("div", {innerHTML:"<p></p>"}, div);
		
		new dijit.form.Button({
			label:"OK",
			type:"submit"
		}).placeAt(div);
		
		var dialog = new dijit.Dialog({
			title:"Select",
			content:div,
			onExecute:onexecute,
			style:"width:300px"
		});
		
		dialog.show();
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
		
		var comboStore = new dojo.data.ItemFileReadStore({
			data: {
				identifier: "id",
				label: "name",
				items : sarr
			}
		});
		
		var div = dojo.create("div", {style:"padding:10px"});
		var nls = new tdi.NlsMixin();
		dojo.create("div", {innerHTML:nls.getString("ConfigTable.Name") + "<p></p>"}, div);
		
		var combo = new dijit.form.ComboBox({
			store:comboStore
		}).placeAt(div, "last");
		
		dojo.create("div", {innerHTML:"<p></p>"}, div);
		
		var okButton = new dijit.form.Button({
			label:nls.getString("ok"),
			disabled:true,
			type:"submit"
		}).placeAt(div);
		
		dojo.connect(combo, "onChange", function() {
			var str = combo.get("value");
			if(str == null || str == "")
				okButton.set("disabled", true);
			else
				okButton.set("disabled", false)
		});
		
		var dialog = new dijit.Dialog({
			title:nls.getString("AttributeMap.toolbar.Add.tooltip"),
			content:div,
			onExecute:function() {
				var str = combo.get("value");
				if(str != null && str != "") {
					if(!map.isMapped(str)) {
						var item = map.newItem({name:str});
						var newitem = store.newItem({id:item.getName(), name:item.getName(), mapping:str, value:""});
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
	
	tdiutil.formatDate = function(str) {
		var arr = str.match(/(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}).(\d{3})(.*)/);
		if(arr && arr.length > 0) {
			return arr[1] + "-" + arr[2] + "-" + arr[3] + " " + arr[4] + ":" + arr[5] + ":" + arr[6];
		} else {
			return str;
		}
	};
	
}
