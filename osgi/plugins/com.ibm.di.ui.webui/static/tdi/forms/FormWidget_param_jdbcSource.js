define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Form",
	"tdi/NlsMixin",
	"dojo/text!./templates/FormWidget_param_jdbcSource.html"
], function(declare, lang, array, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Form, nls, template) {
	
	return declare(
		[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
		{
			templateString: template,
			
			/*
			 * If true, login details panel is enabled/shown
			 */
			loginDetails: true,
			
			/*
			 * Parameters to use when reading/writing parameter values
			 */
			loginParamMap: {
				"jdbcUser":"jdbcUser",
				"jdbcPassword":"jdbcPassword",
				"jdbcSchema":"jdbcSchema",
				"jdbcTable": "jdbcTable"
			},
			
			driverParamMap: {
				"jdbcSourceTemplate": "jdbcSourceTemplate",
				"jdbcSource": "jdbcSource",
				"jdbcDriver": "jdbcDriver"
			},
			
			formMap: {
				"jdbc":"jdbc",
				"db2":"db2",
				"derby":"db2",
				"embedded":"db2",
				"mssql":"db2",
				"solid":"db2",
				"oracle":"db2"
			},
			
			urlMap: {
				"db2":"jdbc:db2://{host}:{port}/{dbname}",
				"derby":"jdbc:derby://{host}:{port}/{dbname}",
				"embedded":"jdbc:derby:{dbname}",
				"mssql":"jdbc:sqlserver://{host}:{port};databaseName={dbname}",
				"oracle":"jdbc:oracle:thin:@{host}:{port}:{dbname}",
				"solid":"jdbc:solid://{host}:{port}",
			},
			
			driverMap: {
				"db2":"com.ibm.db2.jcc.DB2Driver",
				"derby":"org.apache.derby.jdbc.ClientDriver",
				"embedded":"org.apache.derby.jdbc.EmbeddedDriver",
				"mssql":"com.microsoft.sqlserver.jdbc.SQLServerDriver",
				"oracle":"oracle.jdbc.driver.OracleDriver",
				"solid":"solid.jdbc.SolidDriver",
			},
			
			_typeChanged: function(type) {
				this.databaseTR.style.display = (type == "solid" ? "none" : "");
				this.hostnameTR.style.display = (type == "embedded" ? "none" : "");
				this._stack.selectChild(this.id + "_" + this.formMap[type]);
				if(type != "jdbc") {
					this.jdbcSource.set("value", this._getURL(type));
					this.jdbcDriver.set("value", this.driverMap[type]);
					// -- only signal change if we change the result
					if(this.value != this.get("value")) {
						this.value = this.get("value");
						this.onChange(this.value);
						this._updateTemplate();
					}
				}
			},
			
			_getURL: function(type) {
				var str = this.urlMap[type];
				str = str.replace(/{port}/, this.Port.get("value"));
				str = str.replace(/{host}/, this.Hostname.get("value"));
				str = str.replace(/{dbname}/, this.Database.get("value"));
				return str;
			},
			
			onChange: function() {
				
			},
			
			_onChange: function() {
				var type = this._type.get("value");
				this._typeChanged(type);
				// -- user may change url and driver. emit change event to update config
				if(type == "jdbc") {
					this.onChange(this.value);
					this._updateTemplate();
				}
			},
			
			_getValueAttr: function() {
				return this._getURL(this._type.get("value"));
			},
			
			_setValueAttr: function(value) {
				this._parseSource();
			},
			
			_getTemplate: function() {
				return this._type.get("value") + "||" + this.Hostname.get("value") + "||"+ this.Port.get("value") + "||" + this.Database.get("value");
			},
			
			_updateTemplate: function() {
				this.config.setParam(this.driverParamMap.jdbcSourceTemplate, this._getTemplate());
			},
			
			_parseSource: function() {
				var template = this.config.getParam(this.driverParamMap.jdbcSourceTemplate); 
				if(template) {
					var match = template.split("||");
					if(match && match.length == 4) {
						this._type.set("value", match[0]);
						this.Hostname.set("value", match[1]);
						this.Port.set("value", match[2]);
						this.Database.set("value", match[3]);
					}
				}
			},
			
			_updateParameter: function(param, value, protect) {
				var v = this.config.getParam(param);
				if(!v || v != value) {
					this.config.setParam(param, value, protect);
				}
			},
			
			_updateURL: function(value) {
				this._updateParameter(this.driverParamMap.jdbcSource, value, false);
			},
			
			_updateDriver: function(value) {
				this._updateParameter(this.driverParamMap.jdbcDriver, value, false);
			},
			
			_updateLogin: function(value) {
				var obj = this.userForm.get("value");
				for(f in obj) {
					this._updateParam(this.loginParamMap[f], (f == "jdbcPassword" ? true : false));
				}
			},
			
			startup: function() {
				this.inherited(arguments);
				
				// -- make sure we don't miss any unknown types
				this.jdbcSource.set("value", this.config.getParam(this.driverParamMap.jdbcSource));
				this.jdbcDriver.set("value", this.config.getParam(this.driverParamMap.jdbcDriver));
				this._typeChanged("jdbc")
				
				// -- now check if we have a template
				this._parseSource();
				
				if(this.loginDetails) {
					var value = {};
					for(f in this.loginParamMap) {
						var val = this.config.getParam(this.loginParamMap[f]);
						if(val) {
							value[f] = val;
						}
					};
					this.userForm.set("value", value);
				} else {
					this._loginDetails.style.display = "none";
				}
			}
		}
	)
});
