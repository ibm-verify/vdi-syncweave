define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/NlsMixin",
	"dojo/text!./templates/FormWidget_param_ldapUrl.html"
], function(declare, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls, template) {
	
	return declare(
		[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
		{
			templateString: template,
			
			// when used in a form this property must be present or it won't
			// be affected by Form.set/get("value") calls.
			value: null,
			
			buildRendering: function() {
				this.inherited(arguments);
			},
			
			onChange: function() {
			},
			
			_onChange: function() {
				if(this.SSL.get("checked") != this._ssl) {
					this.set("_ssl", this.SSL.get("checked"));
					this.config.setPA
				}
				if(this.Hostname.get("value") != this._hostname) {
					this.set("_hostname", this.Hostname.get("value"));
				}
				if(this.Port.get("value") != this._port) {
					this.set("_port", this.Port.get("value"));
				}
				this.onChange(this.get("value"));
			},
			
			_getValueAttr: function() {
				var url = this._ssl ? "ldaps" : "ldap";
				return url + "://" + this._hostname + (this._port != "" ? ":"+this._port : "");
			},
			
			_setValueAttr: function(value) {
				this.value = value;
				this._parseUrl();
			},
			
			_parseUrl: function() {
				if(this.value) {
					var match = this.value.match(/(ldaps?):\/\/([^\s:]*):?([\d]*)/);
					if(match && match.length == 4) {
						this._ssl = match[1] == "ldaps";
						this._hostname = match[2];
						this._port = match[3];
					} else {
						var match = this.value.match(/([^\s:]*):?([\d]*)/);
						if(match && match.length > 1) {
							this._ssl = false;
							this._hostname = match[1];
							this._port = match.length > 2 ? match[2] : "389";
						}
					}
					this.SSL.set("checked", this._ssl);
					this.Hostname.set("value", this._hostname);
					this.Port.set("value", this._port);
				}
			},
			
			startup: function() {
				this.inherited(arguments);
				this._parseUrl();
			},
			
			postCreate: function() {
			}
		}
	)
	
});
