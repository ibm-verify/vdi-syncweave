/**
 * The ALComponentLayout is a container for ALComponent, ALConnection and ALDropPoint objects.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/layout/_LayoutWidget"
	], function(declare, array, lang, _LayoutWidget) {
return declare(
	[ _Widget, _TemplatedMixin ],
	{
		layout: function() {
			array.forEach(this.getChildren(), function(child) {
				
			});
		}
	})
});
