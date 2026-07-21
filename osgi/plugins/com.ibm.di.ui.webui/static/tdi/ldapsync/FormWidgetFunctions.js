define([
    	"dojo/_base/declare",
    	"dojo/_base/lang",
    	"dojo/_base/array",
    	"dojo/Deferred",
    	"tdi/tdiutil",
    	"tdi/config/connector",
    	"./LDSUtil",
    	"tdi/forms/_FormWidgetMixinFunctions"
    ], function(declare, lang, array, Deferred, tdiutil, tdiconnector, LDSUtil, FormWidgetMixinFunctions) {

	FormWidgetMixinFunctions.selectLDAPSyncSearchBase = function(form, formItem) {
		var clone = tdiutil.clone(form.config.getParent().config);
		var conn = new tdiconnector({config:clone});
		conn.setInheritFrom("system:/Connectors/ibmdi.LDAP");
		LDSUtil.mapProps2LDAP(conn);
		// -- LDSFlow does not provide LDAP credentials in its config or form
		// -- so we provide LDAP params from the provided ldapConfig instead 
		if(form && form.ldapConfig) {
			var arr = ["target.ldap.url", "target.ldap.user", "target.ldap.password"];
			array.forEach(arr, function(p) {
				conn.getConnectionConfig().setParam(p, form.ldapConfig.getConnectionConfig().getParam(p));
			});
		}
		FormWidgetMixinFunctions._selectLDAPSearchBase(conn, formItem.key, form, tdiutil.getFormLabel(formItem));
	};
	
	return FormWidgetMixinFunctions;
});
	

