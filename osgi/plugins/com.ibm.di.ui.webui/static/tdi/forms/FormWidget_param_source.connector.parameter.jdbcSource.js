define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"./FormWidget_param_jdbcSource"
], function(declare, lang, jdbcSource) {
	
	return declare(
		[jdbcSource],
		{
			loginDetails:false,
			driverParamMap: {
				"jdbcUrl": "source.connector.parameter.jdbcUrl",
				"jdbcSource": "source.connector.parameter.jdbcSource",
				"jdbcDriver": "source.connector.parameter.jdbcDriver",
				"jdbcSourceTemplate": "jdbcSourceTemplate"
			}
		}
	)	
});
