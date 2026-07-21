dojo.provide("tdi.DashboardAuth");

dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.Form");
dojo.require("dijit.form.ComboBox");
dojo.require("dijit.form.TextBox");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiutil");

dojo.declare("tdi.DashboardAuth",
		[dijit._Widget, dijit._Templated, tdi.NlsMixin],
	{
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/DashboardAuth.html"),
		buttons: null,

		updateFields : function(data) {
			var arr = data.ldapurl.match(/ldap:\/\/([^\/]*)\/(.*)/);
			if(!arr)
				arr = data.ldapurl.match(/ldap:\/\/([^\/]*)/);
			if(arr) {
				var fd = {
						local:data.localhost,
						remote:data.remotehost,
						ldap_host:arr[1],
						ldap_search:arr[2] ? arr[2].replace("/", ",") : "",
						ldap_group:data.ldapgroup ? data.ldapgroup : ""
				}
				this._form.set("value", fd);
			}
		},
		
		updateServer : function() {
			var fd = this._form.get("value");
			var pd = {};
			pd.localhost = fd.local;
			pd.remotehost = fd.remote;
			pd.ldapurl = "ldap://" + fd.ldap_host + (fd.ldap_search != "" ? "/" + fd.ldap_search.replace(",", "/") : "");
			pd.ldapgroup = fd.ldap_group;
			dojo.when(tdiapi.setDashboardAuth(pd), function() {
				tdiutil.alert(this.getString("WebCE.settingsUpdated"))
			},
			function(err) {
				tdiutil.error(err);
			});
		},
		
		postCreate : function() {
			dojo.when(tdiapi.getDashboardAuth(), dojo.hitch(this, "updateFields"));
		}
});