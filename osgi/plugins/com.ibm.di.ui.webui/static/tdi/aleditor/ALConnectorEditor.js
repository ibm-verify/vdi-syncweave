/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/ComponentForm",
	"tdi/LinkCriteriaWidget",
	"./ALHooks",
	"./Border",
	"./ALButton",
	"./ALConnectorEditorOptions",
	"dojo/text!./templates/ALConnectorEditor.html"
], function(declare, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, 
		TDIComponentForm, TDILinkCriteria, HooksWidget, Border, ALButton, ALConnectorEditorOptions, template) {
	
return declare(
	[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin],
	{
		templateString: "<div></div>",
		
		resize: function(obj) {
			if(this.form) {
				this.form.resize(obj);
			}
		},
		
		customizeWrappedEditor: function(b, title) {
			// summary:
			//		Called from wrapEditor in ALEditor2
			var top = b.getTop();
			if(top) {
				this.editorOptions = new ALConnectorEditorOptions({
					config:this.config,
					title:title,
					onTypeChange:lang.hitch(this, function() {
						this.addConnectorForm();
					})
				});
				top.set("content", this.editorOptions);
			}
		},
		
		addConnectorForm: function() {
			// -- Connection config
			var editor = new TDIComponentForm({
				config:this.config,
				ignoreSections:false,
				verticalLayout:true
			});
			this.form.removeContainerPane("Configuration");
			this.form.addContainerPane(editor, {title:"Configuration"});
		},
		
		postCreate: function() {
			// -- Main form
			var form = this.form = new Border({
				containerType:"stack",
				gutters:false,
				style:"width:100%;height:100%;margin:0; padding:0"
			}).placeAt(this.domNode);

			// -- Connection config
			this.addConnectorForm();

			// -- Link Criteria
			if(this.config.requiresLinkCriteria()) {
				editor = new TDILinkCriteria({config:this.config});
				form.addContainerPane(editor, {title:"Link"});
			}
			
			// -- Hooks
			editor = new HooksWidget({
				config:this.config
			});
			form.addContainerPane(editor, {title:"Hooks"});
			
			// -- select config initially
			form.selectContainerPane("Configuration");
			
			
			// -- nav panel on the right
			var navPanel = form.createContainerPane({"class":"tdiWebDevNavPane"});
			form.setRight(navPanel);

			// -- config button
			var navButton = new ALButton({
				title:"Configuration",
				hoverImage:"Gear.png",
				normalImage:"Gear_gray.png",
				onClick:function() {
					form.selectContainerPane("Configuration")
				}
			});
			navPanel.addChild(navButton);
			
			// -- hooks button
			var navButton = new ALButton({
				title:"Hooks",
				hoverImage:"Hook.png",
				normalImage:"Hook_gray.png",
				onClick:function() {
					form.selectContainerPane("Hooks")
				}
			});
			navPanel.addChild(navButton);
			
			// -- link button
			if(this.config.requiresLinkCriteria()) {
				var navButton = new ALButton({
					title:"Link",
					hoverImage:"Link.png",
					normalImage:"Link_gray.png",
					onClick:function() {
						form.selectContainerPane("Link")
					}
				});
				navPanel.addChild(navButton);
			}			
		}
	})
});
