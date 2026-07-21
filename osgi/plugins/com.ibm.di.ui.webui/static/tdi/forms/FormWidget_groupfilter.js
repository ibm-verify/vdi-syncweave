/*
/* A simple widget that edits a attribute filter (attribute operator value)
 */
define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/aspect",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "tdi/forms/_FormWidgetMixinFunctions",
   "tdi/tdiutil",
   "tdi/NlsMixin",
   "dojo/text!./templates/FormWidget_groupfilter.html"
], function(declare, array, lang, aspect, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiforms, tdiutil, nls, template) {

	return declare([ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
		{
		templateString : template,
		
		// value: String
		value: "",

		getParamValue: function(key) {
			// we override this so the LDAP browser never shows the current value
			// the dialog is meant to add to the current contents of this control
			return "";
		},

		setParamValue: function(key, value) {
			var str = this.get("value");
			if(str && str.length > 0) {
				str = str + "\n" + value;
			} else {
				str = value;
			}
			this.set("value", str);
		},

		updateControl: function() {},
		
		getEndpoint: function() {
			var alc = this.config ? this.config.getAssemblyLine() : null;
			if(!alc) {
				return null;
			}
			return alc.getConnector("Input");
		},
		
		getLDAPEndpoint: function() {
			var conn = this.getEndpoint();
			if(!conn) {
				return null;
			}
			var type = conn.getConnectionConfig().getParam("source.form");
			return (type == "Form_AD" || type == "Form_LDAP" || type == "Form_SUN" || type == "Form_TDS") ? conn : null;
		},
		
		checkIfLdap: function() {
			// disable form fields if endpoint is not LDAP based
			var disabled = this.getLDAPEndpoint() == null;
			this._text.set("disabled", disabled);
			this._button.set("disabled", disabled);
			
			// -- disable the checkbox if we're in a formwidget
			if(this.formWidget && this.formWidget.getControl("filter.groups.enabled")) {
				this.formWidget.getControl("filter.groups.enabled").set("disabled", disabled);
			}

			if(!this._modid) {
				this._modid = aspect.after(this.getEndpoint(), "onModify", lang.hitch(this, function(modified, args) {
					if(args.length == 2) {
						var param = args[1] && args[1].param ? args[1].param : null;
						if(param && typeof(param) == "object")
							param = param.name;
						if(param == "complexConfig.inheritFrom" || param == "join.perform") {
							this.checkIfLdap();
						}
					}
				}));
				this.own(this._modid);
			}
		},
		
		_selectGroup: function() {
			// the group filter should be selected from the endpoint
			var conn = this.getLDAPEndpoint();
			if(conn ) {
				tdiforms._selectLDAPSearchBase(conn, "value", this, this.getString("FDS.selectGroupObject"), {
					"$type":"Search",
					"$display":"DN",
					"$showRoot":true,
					"$dn":conn.getConnectionConfig().getParam("source.ldap.searchBase"),
					"base":conn.getConnectionConfig().getParam("source.ldap.searchBase"),
					"scope":"subtree",
					"filter":"(|(objectclass=group)(objectclass=groupofnames)(objectclass=groupofuniquenames))"
				});
			}
		},

		_onChange: function() {
			this.onChange(this.get("value"));
		},
		
		onChange: function(value) {
		},

		_setValueAttr: function(value) {
			this._text.set("value", value);
			this.checkIfLdap();
		},
		
		_getValueAttr: function() {
			return this._text.get("value");
		},
		
		postCreate : function() {
		}
	})
});