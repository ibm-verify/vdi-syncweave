define([
    "dojo/_base/declare",
    "tdi/tdisession",
    "tdi/tdiutil",
    "tdi/tdientry",
    "idx/widget/Dialog",
    "tdi/ConfigBrowser",
    "tdi/LDAPBrowser",
    "tdi/FileBrowser",
    "dijit/form/Button",
    "tdi/NlsMixin"
], function(declare, tdisession, tdiutil, tdientry, Dialog, ConfigBrowser, LDAPBrowser, FileBrowser, Button, nls) {
	
	var f = {};
	
	f.nls = new nls();
	
	f.selectFile = function(form, formItem) {
		var browser = new FileBrowser();
		f._openDialogForWidget(browser, tdiutil.getFormLabel(formItem), function() {
			var value = browser.getValue();
			if(value)
				value = value.path;
			else
				value = "";
			form.setParamValue(formItem.key, value);
			form.updateControl(formItem.key);
		})
		browser.resize({w:400, h:300});
	};
	
	f.selectJDBCTable = function(form, formItem) {
		var arr = [];
		var sess = new tdisession({});
		var config = form.config.getParent();
		var key = formItem.key;
		var label = tdiutil.getFormLabel(formItem);
		
		function getp(arr) {
			for(var i = 0; i < arr.length; i++) {
				var val = config.getConnectionConfig().getParam(arr[i]);
				if(val)
					return val;
			}
			return "";
		}
		
		// check if we already have a simple object with props
		var jdbc = {
			"[type]":"system:/Connectors/ibmdi.JDBC",
			"jdbcSource":getp(["source.connector.parameter.jdbcSource", "jdbcSource"]),
			"jdbcDriver":getp(["source.connector.parameter.jdbcDriver", "jdbcDriver"]),
			"jdbcLogin":getp(["source.connector.parameter.jdbcLogin", "jdbcLogin"]),
			"jdbcSchema":getp(["source.connector.parameter.jdbcSchema", "jdbcSchema"]),
			"jdbcPassword":getp(["source.connector.parameter.jdbcPassword", "jdbcPassword"])
		};
		
		sess.openSessionForConnector(form.config.getParent(), true, jdbc).then(function() {
			arr = [
			       "return " + sess.conn.getName() + ".connector.queryTables();"
			];
			return sess.executeScript(arr.join("\n"));
		}).then(function(data) {
			var entry = new tdientry({data:data});
			var arr = entry.getAttributeValues("value");
			tdiutil.selectFromTable(arr, label, function(value) {
				form.setParamValue(formItem.key, value);
				form.updateControl(formItem.key);
			});
			sess.close();
		}, function(err) {
			tdiutil.error(err);
			sess.close();
		});
	};
	
	f.selectAssemblyLine = function(form, formItem, label) {
		tdiutil.selectAssemblyLine(tdiutil.getFormLabel(formItem), form.getParamValue(formItem.key), function(value) {
			form.setParamValue(formItem.key, value);
			form.updateControl(formItem.key);
		});
	};
	
	f.selectLDAPSearchBase = function(form, formItem, label) {
		f._selectLDAPSearchBase(form.config.getParent(), formItem.key, form, tdiutil.getFormLabel(formItem));
	};
	
	f._selectLDAPSearchBase = function(config, key, form, label, rootQuery) {
		
		function getp(arr) {
			for(var i = 0; i < arr.length; i++) {
				var val = config.getConnectionConfig().getParam(arr[i]);
				if(val)
					return val;
			}
			return "";
		}
		
		var ldap = config;
		// check if we already have a simple object with props
		if(!ldap["target.ldap.url"]) {
			ldap = {
				"target.ldap.searchbase":getp(["source.ldap.searchBase", "target.ldap.searchBase", "ldapSearchBase"]),
				"target.ldap.url":getp(["source.ldap.url", "target.ldap.url", "ldapUrl"]),
				"target.ldap.user":getp(["source.ldap.user", "target.ldap.user", "ldapUsername"]),
				"target.ldap.password":getp(["source.ldap.password", "target.ldap.password", "ldapPassword"]),
			};
		}
		var dlg = null;
		var buttons = [];
		buttons.push(new Button({
			label:this.nls.getString("WebCE.ok"),
			onClick:function() {
				if(form.setParamValue) {
					// If it is a FormWidget type object call those methods
					form.setParamValue(key, browser.get("value"));
					form.updateControl(key);
				} else {
					// otherwise just set key=value
					form.set(key, browser.get("value"));
				}
				dlg.onCancel();
			}
		}));
		var browser = new LDAPBrowser({
			ldap:ldap,
			style:"width:98%; height:100%",
			onError:function(){
				dlg.onCancel();
			},
			rootQuery: rootQuery || {"$dn":""}
		});
		dlg = new Dialog({
			title:label,
			closeButtonLabel:this.nls.getString("WebCE.cancel"),
			content:browser,
			buttons:buttons
		});
		if(form.getParamValue) {
			browser.set("value", form.getParamValue(key))
		} else {
			browser.set("value", form.get(key))
		}
		dlg.show();

	};
	
	f._openDialogForWidget = function(widget, title, cbOk) {
		var dlg = null;
		var buttons = [];
		buttons.push(new Button({
			label:f.nls.getString("WebCE.ok"),
			onClick:function() {
				cbOk();
				dlg.onCancel();
			}
		}));
		dlg = new Dialog({
			title:title,
			closeButtonLabel:f.nls.getString("WebCE.cancel"),
			content:widget,
			buttons:buttons
		});
		dlg.show();
	}
	
	return f;

});
