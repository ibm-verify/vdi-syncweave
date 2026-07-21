/*
 * This mixin is used to display a custom tooltip where a TooltipDialog is shown as long
 * as the mouse is over either the initiating object as well as the actual tooltip dialog itself.
 */
define([
    "dojo/_base/declare",
	"dojo/_base/lang",
    "dijit/popup",
	"dijit/TooltipDialog"
], function(declare, lang, dPopup, TooltipDialog) {
	return declare(
	[],
	{
		
		tooltipOnMouseOver: function(args) {
			if(this._tooltipCloseTimer) {
				// -- tooltip open pending close
				clearInterval(this._tooltipCloseTimer)
				delete this._tooltipCloseTimer;
			} else if(!this._tooltipOpenTimer) {
				// -- tooltip pending open
				this._tooltipOpenTimer = setTimeout(lang.hitch(this, "showTooltip", args), 400);
			}
		},
		
		tooltipOnMouseOut: function(args) {
			if(this._tooltipOpenTimer) {
				clearTimeout(this._tooltipOpenTimer);
				delete this._tooltipOpenTimer;
			}
			// start a timer to check when mouse leaves
			// either the gfx object or the tooltip window
			if(this._tooltip && !this._tooltipCloseTimer && this._tooltipOpen) {
				this._tooltipCloseTimer = setInterval(lang.hitch(this, "hideTooltip", args), 800);
			}
		},
		
		setTooltipContent: function(content, title) {
			if(!this._tooltip) {
				this._tooltip = new TooltipDialog({
					content:content,
					title:title,
					onClose: lang.hitch(this, function() {
						if(this._tooltipCloseTimer) {
							clearInterval(this._tooltipCloseTimer);
							delete this._tooltipCloseTimer;
						}
						this._tooltipOpen = false;
					})
				});
				dojo.connect(this._tooltip, "onFocus", lang.hitch(this, function(args) {
					this._tooltipFocus = true;
				}));
				dojo.connect(this._tooltip, "onMouseOver", lang.hitch(this, function() {
					this._tooltipFocus = true;
				}));
				dojo.connect(this._tooltip, "onMouseOut", lang.hitch(this, function() {
					this._tooltipFocus = false;
				}));
				
			} else {
				this._tooltip.set("content", content);
			}
		},
		
		showTooltip: function(args) {
			if(this._tooltip) {
				this._tooltipOpen = true;
				var opt = {
						parent:this,
						popup:this._tooltip
				}
				if(this.domNode) {
					opt.around = this.domNode;
					opt.orient = ["after"];
				} else {
					opt.x = args.clientX;
					opt.y = args.clientY;
				}
					
				dPopup.open(opt);
			}
		},
		
		hideTooltip: function() {
			if(this._tooltip && !this._tooltipFocus) {
				dPopup.close(this._tooltip);
			} else if(this._tooltipClosed) {
				this.clearTooltip();
			}
		},
		
		clearTooltip: function() {
			if(this._tooltipCloseTimer) {
				clearInterval(this._tooltipCloseTimer);
				delete this._tooltipCloseTimer;
			}
			this._tooltipFocus = false;
		},
		
		clearFocus: function() {
			this._tooltipFocus = false;
		},
		
		setFocus: function() {
			this._tooltipFocus = false;
		},
		
		clearPopups: function() {
			if(this._tooltip) {
				dPopup.close(this._tooltip);
			}
		},
		
		isLeftToRight: function() {
			return true;
		},
		
		debugVars: function() {
			console.log("State vars: _tooltipOpenTimer=" + this._tooltipOpenTimer);
			console.log("State vars: _tooltipCloseTimer=" + this._tooltipCloseTimer);
			console.log("State vars: _tooltipFocus=" + this._tooltipFocus);
			console.log("State vars: _tooltipOpen=" + this._tooltipOpen);
		}
	})
});