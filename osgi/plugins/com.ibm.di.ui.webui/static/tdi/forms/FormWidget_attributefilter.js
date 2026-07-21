/*
/* A simple widget that edits a attribute filter (attribute operator value)
 */
define([
   "dojo/_base/declare",
   "dojo/_base/array",
   "dojo/_base/lang",
   "dojo/store/Memory",
   "dijit/_Widget",
   "dijit/_TemplatedMixin",
   "dijit/_WidgetsInTemplateMixin",
   "tdi/tdiutil",
   "tdi/NlsMixin",
   "dojo/text!./templates/FormWidget_attributefilter.html"
], function(declare, array, lang, Memory, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, tdiutil, nls, template) {

	return declare([ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
		{
		templateString : template,
		
		// value: String
		value: "",
		
		// label: String
		label: "",
		
		_onChange: function() {
			this.onChange(this.get("value"));
		},
		
		onChange: function(value) {
		},

		_setValueAttr: function(value) {
			var arr = value ? value.split("||") : [];
			var formvalue = {
					attribute:"",
					operator:"=",
					value:""
			}
			if(arr.length == 3) {
				formvalue.attribute = arr[0];
				formvalue.operator = arr[1];
				formvalue.value = arr[2];
			}
			
			this._Form.set("value", formvalue);
		},
		
		_getValueAttr: function() {
			var value = this._Form.get("value");
			return value.attribute + "||" + value.operator + "||" + value.value;
		},

		_populateCombo: function() {
			// the group filter should be selected from the endpoint
			var alc = this.config ? this.config.getAssemblyLine() : null;
			if(alc) {
				var conn = alc.getConnector("Input");
				if(conn ) {
					var data = [];
					array.forEach(conn.getSchema().getNames(), function(attr) {
						data.push({id:attr, name:attr})
					});
					this._attribute.set("store", new Memory({data:data}));
				}
			}

		},
		
		postCreate : function() {
			this._populateCombo();
		}
	})
});