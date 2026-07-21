/**
 * This is the wrapper widget that manages the editors for assemblylines in the solution.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/form/Button",
	"dijit/form/Select",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/Toolbar",
	"dijit/ToolbarSeparator",
	"tdi/aleditor/ALEditor2",
	"tdi/aleditor/Border",
	"tdi/ToolbarLabel",
	"tdi/tdiutil",
	"tdi/NlsMixin"
	], function(declare, array, lang, html, _Widget, _TemplatedMixin, Button, Select, BorderContainer, TabContainer, Toolbar, ToolbarSeparator,
			ALEditor, Border, ToolbarLabel, tdiutil, nlsmixin) {
	
return declare(
	[_Widget, _TemplatedMixin],
	{
		templateString: "<div data-dojo-attach-point='Main' style='width:100%; height:100%; margin:0; padding:0'></div>",
		
		// -- 
		defaultStyle: "width:100%; height:100%; margin:0; padding:0",
		
		// -- The configuration
		config: null,
		
		// -- Container type (stack or tab)
		containerType: "stack", 
		
		// -- The configentry atom
		configentry: null,
		
		constructor: function(args) {
			if(args) {
				declare.safeMixin(this, args);
			}
			this.nls = new nlsmixin();
		},
		
		onEvent: function(eventobj) {
			// summary:
			//		Callback - on rename, create or delete assemblyline
		},
		
		// summary:
		//		Performs actions create, rename and delete
		delegateAction: function(action) {
			var func = action + "AssemblyLine";
			if(this[func]) {
				this[func]();
			}
		},
		
		createAssemblyLine: function() {
			var nls = this.nls;
			tdiutil.openDialog(null, nls.getString("newAssemblyLine"), "CreateJob.html", lang.hitch(this, function(formData) {
				var name = formData.name;
				
				if(this.config.getAssemblyLine(name)) {
					tdiutil.error(nls.getString("RenameConfigAction.AlreadyExists"));
					return;
				}
				
				var al = this.config.createAssemblyLine(name);
				if(al == null)
					return false;
				var input = al.createFeedConnector("Input", "Iterator");
				var output = al.createDataFlowConnector("Output", "AddOnly");
				
				this.config.setModified(true);
				this.onEvent({name:this.config.getConfigName(), action:"create", assemblyline:name});
			}));
		},
		
		removeAssemblyLine: function(alname) {
			if(this.border.getContainerPane(alname)) {
				this.border.removeContainerPane(alname);
			}
		},
		
		renameAssemblyLine: function() {
		},
		
		openAssemblyLine: function(alname) {
			// summary:
			//		Opens or selects the editor for the named assemblyline
			if(!this.border.getContainerPane(alname)) {
				var alEditor = new ALEditor({
					config:this.config, 
					assemblyline:alname,
					projects:this.projects
				});
				alEditor.openConfig(this.configentry, "", alname, this.config);
				this.border.addContainerPane(alEditor, {title:alname, closable:true});
			} else {
				this.border.selectContainerPane(alname);
			}
		},
		
		resize: function(obj) {
			if(obj && obj.h > 0 && this.border) {
				this.border.resize(obj);
			}
		},
		
		postCreate: function() {
			this.border = new Border({containerType:this.containerType, params:{nested:true}}).placeAt(this.Main);
			this.border.startup();
			this._supportingWidgets.push(this.border);
		}
	})		
});

