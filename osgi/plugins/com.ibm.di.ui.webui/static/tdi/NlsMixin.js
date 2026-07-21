define("tdi/NlsMixin", [
    	"dojo/_base/declare"
    ], function(declare) {

	return declare([], {
		nls: dojo.i18n.getLocalization("tdinls", "plugin"),
		
		getString: function(/* String */key, /* Object or Array? */substitutes) {
			var str = this.nls[key];
			// we use a WebCE prefix in the CE's plugin.properties files
			if(!str)
				str = this.nls["WebCE." + key];
			if(!str)
				str = key;
			return (substitutes != null) ? dojo.replace(str, substitutes) : str;
		}
	
	});
});

