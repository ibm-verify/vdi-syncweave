/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"idx/form/CheckBox",
	"idx/form/TextBox",
	"tdi/tdiutil",
	"tdi/NlsMixin",
	"dojo/text!./templates/SimpleForm.html"
], function(declare, array, lang, html, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		CheckBox, TextBox, tdiutil, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString : template,

		createInitParamsForm: function() {
			var t = this;
			var names = t.alInitParams.getNames();
			t.data = t.data || {};
			if(t.config) {
				array.forEach(names, function(p) {
					t.data[p] = t.config.getParam(t.configPrefix+p); 
				});
			}
			
			array.forEach(names, function(p) {
				var item = t.alInitParams.getItem(p);
				var parm = tdiutil.parseALInitParam(item);
				
				t.addRow(
					p,							// Name/key
					parm.nativeSyntax,			// Type (boolean etc)
					t._itemTable,				// Target table
					t.getString(p),				// Label (try to translate)
					t.getString(parm.comment),	// Tooltip
					parm.sample					// Default value (placeholder)
				);
			});
			
		},
		
		addRow: function(name, syntax, table, label, tooltip, defvalue) {
			var bool = syntax && syntax.toLowerCase() == "boolean";
			
			var tr = html.create("tr", {}, table);
			
			html.create("td", {innerHTML:(bool?"":label), "class":"tdiFormLabel", title:tooltip}, tr);
			
			if(this.copyDefaultParams && !this._hasParam(name) && defvalue) {
				this.config.setParam(this.configPrefix + name, defvalue);
			}
			
			var td = html.create("td", {}, tr);
			var ctl = null;
			if(bool) {
				ctl = new CheckBox({
					label:label,
					checked:this._getParamBool(name, defvalue),
					onChange:lang.hitch(this, "_onChange", name),
					title:tooltip
				}).placeAt(td);
			} else {
				ctl = new TextBox({
					style:"width:30em",
					value:this._getParam(name, defvalue),
					onChange:lang.hitch(this, "_onChange", name),
					title:tooltip
				}).placeAt(td);
			}
			ctl.startup();
			this._supportingWidgets.push(ctl);
		},
		
		_hasParam: function(param) {
			var p = this.config ? this.config.getParamByName(this.configPrefix + param, true) : null;
			return p != null;
		},
		
		_setParam: function(param, value) {
			if(this.config) {
				this.config.setParam(this.configPrefix + param, value);
			}
			this.data[param] = value;
		},
		
		_getParam: function(param, defval) {
			var value = null;
			if(this.config) {
				value = this.config.getParam(this.configPrefix + param);
			} else {
				value = this.data[param];
			}
			if(!value && defval !== undefined)
				value = defval;
			return value;
		},
		
		_getParamBool: function(param, defval) {
			if(this.config) {
				return this.config.getParamBoolean(this.configPrefix + param, defval);
			}
			return this.data[param] || defval;
		},
		
		_onChange: function(param, value) {
			if(this.config) {
				this.config.setParam(this.configPrefix + param, value);
			}
		},
		
		postCreate: function() {
			if(this.alInitParams) {
				this.createInitParamsForm();
			}
		}
	})
});