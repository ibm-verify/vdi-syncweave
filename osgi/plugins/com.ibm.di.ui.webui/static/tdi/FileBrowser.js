define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/Tree",
	"dijit/form/Select",
	"dijit/tree/ObjectStoreModel",
	"idx/widget/Breadcrumb",
	"tdi/NlsMixin",
	"tdi/model/FileBrowserStore",
	"dojo/text!./templates/FileBrowser.html"
], function(declare, lang, array, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, Tree, Select, ObjectStoreModel,
		Breadcrumb, nls, FileBrowserStore, template) {
	return declare(
		[_Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
		{
			templateString: template,
			
			onOk: function() {
			},
			
			onCancel: function() {
			},
			
			onChange: function() {
			},
			
			getValue: function() {
				return this.selectedFile;
			},
			
			selectFile: function(obj) {
				var item = this.table.getSelectedItem();
				if(item) {
					if(item.directory[0]) {
						this.selectedDirectory = item;
						this.selectedFile = null;
					} else {
						this.selectedDirectory = null;
						this.selectedFile = item;
					}
				} else {
					this.selectedDirectory = null;
					this.selectedFile = null;
				}
				this.onChange();
			},

			resize: function(obj) {
				if(obj && obj.h && this.tree) {
					this.tree.resize(obj);
				}
			},
			
			createTree: function(root) {
				if(this._breadcrumbs) {
					this._breadcrumbs.destroyRecursive();
					this._breadcrumbs = null;
				}
 				if(this.tree) {
					this.tree.destroyRecursive();
					this.tree = null;
				}
 				
 				if(!this._path) {
 					this._path = new Select({
 						style:"width:100%",
 						onChange:lang.hitch(this, "setRoot")
 					}).placeAt(this.domNode);
 					this._path.startup();
 				}
// 				this._breadcrumbs = new Breadcrumb({
// 					style:"width:100%"
// 				}).placeAt(this.domNode);
// 				this._breadcrumbs.startup();
				
				this.store = new FileBrowserStore({
					onRootSet:lang.hitch(this, "updateBreadcrumb")
				});
				
			    var model = new ObjectStoreModel({
			        store: this.store,
			        query:{parent:root},
			        getLabel: function(item) {
			        	return item.name || item.path;
			        },
			        mayHaveChildren: function(item) {
			        	return item.directory;
			        }
			    });
			    
				this.tree = new Tree({
					model:model,
					showRoot:false,
					style:"height:300px; width:98%",
					title:this.getString("ImportConfigWizard_27")
				})
				
				this.own(
					this.tree.watch(
						"selectedItems",
						lang.hitch(this, function(prop, oldsel, newsel) {
							if(newsel && newsel.length == 1) {
								this.selectedFile = newsel[0];
							} else {
								this.selectedFile = null;
							}
						})
					)
				);
				this.tree.onDblClick = lang.hitch(this, function(item, node, evt) {
					if(item.directory && item.path) {
						this.createTree(item.path);
					}
				});
				this.tree.placeAt(this.domNode);
				this.tree.startup();
			},
			
			updateBreadcrumb: function(root) {
				var t = this;
				if(root.path == t.root)
					return;
				
				t.root = root.path;
				var arr = root.path.split(/\/|\\/);
				
				if(this._path) {
					array.forEach(t._path.getOptions(), function(opt) {
						t._path.removeOption(opt);
					});
					
					array.forEach(t.store.roots, function(fs) {
						if(fs.path != t.store.top)
							t._path.addOption({label:fs.path, value:fs.path});
					});
					
					var p = [];
					if(arr[0] == "") {
						arr[0] = t.store.top;
					}
					
					var space = "";
					for(var i = 0; i < arr.length; i++) {
						p.push(arr[i])
						this._path.addOption({
							value:p.join("/").replace("//", "/"),
							label:space+arr[i],
							selected:false
						})
						space += "&nbsp;";
						space += "&nbsp;";
					}
					this._path.set("value", p.join("/").replace("//", "/"));
				} else {
					this.crumbs = []; //this.crumbs || [];
					for(var i = 0; i < arr.length; i++) {
						t.addBreadCrumb(arr[i])
					}
				}
			},
			
			addBreadCrumb: function(name) {
				var sb = new Breadcrumb.Crumb({ 
					title: lang.isObject(name) ? name.title : name, 
					label: lang.isObject(name) ? name.label : name,
					onClick:lang.hitch(this, "setRoot", this.crumbs.length)
				});
				this.crumbs.push(sb);
				this._breadcrumbs.push(sb);
				
			},
			
			setRoot: function(index) {
				var path = [];
				if(lang.isString(index)) {
					path.push(index);
				} else {
					for(var i = 0; i <= index; i++) {
						path.push(this.crumbs[i].title);
					}
				}
				path = path.join("/");
				if(path != this.root) {
					if(path.match(/:$/)) {
						path += "/";
					}
					this.createTree(path);
				}
			},
			
			postCreate: function() {
				this.inherited(arguments);
				this.createTree(".");
			},
				
			startup: function() {
				this.inherited(arguments);
			}
		})
});