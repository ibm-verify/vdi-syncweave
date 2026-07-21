/**
 * LDSPtaSummary
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSPtaSummary.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, TextArea, TextBox, nls, template) {
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString: template,
		
		normalizeDN: function(dn) {
			if(dn) {
				dn = dn.replace(/\W?,\W?/g, ",");
				return dn.toLowerCase();
			} else {
				return dn;
			}
		},
		
		mouseClick: function() {
			if(this.cell) {
				this.cell.grid.select.row.clear();				
				this.cell.row.select();
			}
		},
		
		updateAffectedFlows: function() {
			var t = this;
			var top = t.parent.config.getTop();
			var subtree = t.normalizeDN(t.data["ibm-slapdptasubtree"]);
			array.forEach(top.getAssemblyLineNames(), function(name) {
				var alc = top.getAssemblyLine(name);
				try {
					var output = alc.getConnector("Output");
					if (!output || typeof output.getConnectionConfig !== "function") return;
					
					var mirror = output.getConnectionConfig().getParamBoolean("global.preserveSourceContainers", false);
					var sb = t.normalizeDN(output.getConnectionConfig().getParam( mirror ? "target.ldap.searchBase" : "target.suffixForUsers"));
					if(sb && sb.indexOf(subtree) != -1) {
						var a = t.data["affectedflows"] = t.data["affectedflows"] || [];
						var ix = sb.indexOf(subtree);
						if(ix > 0) {
							sb = sb.substring(0, ix) + "<b>" + subtree + "</b>";
						}
						a.push(name.substring("Flow_".length) + ": " + sb);
					}
				} catch(err) {
					console.log(err);
				}
			});
		},
		
		setData: function(parent, data) {
			this.parent = parent;
			this.data = data;
			this.updateAffectedFlows();
			this._title.innerHTML = this.data.cn;
			this._branch.innerHTML = this.data["ibm-slapdptasubtree"]
			this._affectedflows.innerHTML = this.data.affectedflows ? this.data.affectedflows.join("<br>") : "";  
		},
		
		postCreate: function() {
		}
	})
});