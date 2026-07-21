dojo.require("dojo.i18n");
dojo.require("dojo.string");
dojo.requireLocalization("tdinls", "plugin");

dojo.provide("tdi.NlsMixin");

dojo.declare("tdi.NlsMixin", null, {
	__initialized : false,
	nls : null,

	constructor : function() {
		if (!this.__initialized) {
			this.nls = dojo.i18n.getLocalization("tdinls", "plugin");
			this.__initialized = true;
		}
	},

	/**
	 * 
	 * @param {String}
	 *            key - the name of the key in the translation file
	 * @param {Object
	 *            or Array?} substitutes - in cases where the translated string
	 *            is a template for string substitution, this parameter holds
	 *            the values to be used by dojo.string.substitute on that
	 *            template
	 */
	getString : function(/* String */key,
	/* Object or Array? */substitutes) {
		var str = this.nls[key];
		// we use a WebCE prefix in the CE's plugin.properties files
		if(!str)
			str = this.nls["WebCE." + key];
		return (substitutes != null) ? dojo.replace(str, substitutes)
				: str;
	},

	postMixInProperties : function() {
		this.inherited('postMixInProperties', arguments);
	}

});