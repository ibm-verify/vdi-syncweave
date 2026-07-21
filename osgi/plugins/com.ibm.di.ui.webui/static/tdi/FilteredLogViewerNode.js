define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin"
], function(declare, array, lang, _Widget, _TemplatedMixin) {
return declare(
	[_Widget, _TemplatedMixin],
	{
		templateString:"<td valign='top'><span dojoAttachPoint='_expando' dojoAttachEvent='onclick:_toggle' class='dijitTreeExpando dijitTreeExpandoClosed' style='cursor:pointer'></span><span dojoAttachPoint='msg'></span><div dojoAttachPoint='content' style='display:none'></div></td>",
		
		_toggle: function() {
			if(dojo.style(this.content, "display") == "none") {
				dojo.style(this.content, "display", "");
				dojo.removeClass(this._expando, "dijitTreeExpandoClosed");
				dojo.addClass(this._expando, "dijitTreeExpandoOpened");
			} else {
				dojo.style(this.content, "display", "none");
				dojo.removeClass(this._expando, "dijitTreeExpandoOpened");
				dojo.addClass(this._expando, "dijitTreeExpandoClosed");
			}
		},
		
		postCreate: function() {
			var type = this.logmsg.type;
			if(type && this.typeColors[type]) {
				dojo.style(this.msg, "color", this.typeColors[type]);
			}
			
			if(type != "ERROR") {
				this._toggle();
			}

			if(this.logmsg.content) {
				this._expando.innerHTML = "&nbsp;&nbsp;&nbsp;&nbsp;";
				this.content.innerHTML = "<pre style='background:white; font:1em courier'>" + this.logmsg.content + "</pre>";
			}
			if(this.showSource && this.logmsg.source)
				this.msg.innerHTML = this.logmsg.source + " " + this.logmsg.text;
			else
				this.msg.innerHTML = this.logmsg.text;
		}
	})
});