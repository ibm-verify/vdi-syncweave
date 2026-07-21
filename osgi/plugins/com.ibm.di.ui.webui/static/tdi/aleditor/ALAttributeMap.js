/**
 * The ALAttributeMap shows an icon (big) with a scrollable list of attributes that are mapped.
 * Clicking on an attribute item opens the editor.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"./ALEditorMixin",
	"tdi/NlsMixin",
	"dojo/text!./templates/ALAttributeMap.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, editorMixin, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, editorMixin, nls ],
	{
		templateString: template,
		
		baseClass: "tdiWebDevAttributeMap",

		updateAttributeList: function() {
			var t = this;
			
			array.forEach(t.attributeList.children, function(child) {
				t.attributeList.removeChild(child);
			});
			
			array.forEach(this.map.getNames(), function(name) {
				dojo.create("div", {
					innerHTML:name,
					"class":"tdiFormLabel",
					style:"cursor:default",
					onclick:function() {
						t._editAttributeMapItem(name);
					}
				}, t.attributeList, "last");
			});
		},
		
		_editAttributeMapItem: function(name) {
			this.parent.showAttributeEditor(this, this.map, name);
		},
		
		_openAttributeEditor: function() {
			this.parent.showAttributeMap(this.map.getParent(), this);
		},
		
		updateIcons: function() {
			var type = this.map.getParent().getSimpleConnectorType();
			if(type && type.match(/LDAP/)) {
				this.image.setAttribute("src", "/fds/static/images/Directory.png");
			} else if(type && type.match(/JDBC|Database/)) {
				this.image.setAttribute("src", "/fds/static/images/Database.png");
			} else {
				this.image.setAttribute("src", "/fds/static/images/File.png");
			}
			
			if(this.map.getParent().isInput()) {
				this._leftArrow.style.display="none";
				this._rightArrow.style.display="";
			} else {
				this._leftArrow.style.display="";
				this._rightArrow.style.display="none";
			}
		},
		
		onResize: function(box) {
		},
		
		refresh: function() {
			this.map = this.map.getParent().getAttributeMap();
			this.updateAttributeList();
			this.updateIcons();
		},
		
		postCreate: function() {
			this.updateAttributeList();
			this.updateIcons();
		}
	})
});
