dojo.provide("tdi.model.InstalledComponentsStore");

dojo.require("dojo.data.ItemFileWriteStore");

dojo.declare("tdi.model.InstalledComponentsStore",
	[dojo.data.ItemFileWriteStore],
	{
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.data = {
				identifier: "id",
				label: "name",
				items : []
			};
		},

		loadData : function() {
			dojo.when(tdiapi.getInstalledComponents(), dojo.hitch(this, "loadComponentNamesFlat"));
		},
		
		loadCompleted : function() {
			
		},

		loadComponentNamesFlat : function(data) {
			var arr = new Array();
			dojo.forEach(data.entry, dojo.hitch(this, function(item) {
				var atom = new tdi.tdiatom({atom:item});
				var str = item.title.value;
				var label = this._getCompNameLabel(str);
				var type = "connector";
				if(atom.getCategory("parser") != null)
					var type = "parser";
				else if(atom.getCategory("function") != null)
					var type = "function";
				
				arr.push({id:str, name:label, type:type, url:item.content.src});
			}));
			
			arr = arr.sort(function(a,b) {
				if(a.name == b.name)
					return 0;
				else if(a.name < b.name)
					return -1;
				else
					return 1;
			});
			dojo.forEach(arr, dojo.hitch(this, function(item) {
				this.newItem(item);
			}));
			
			this.loadCompleted();
		},
		
		loadComponentNames : function(data) {
			this.connectors = this.newItem({
				id:"connectors",
				name:"Connectors",
				items: []
			});
			this.sortComponentNames(data, "connector", this.connectors)
			this.parsers = this.newItem({
				id:"parsers",
				name:"Parsers",
				items: []
			});
			this.sortComponentNames(data, "parser", this.parsers)
		},
		
		_getCompNameLabel : function(label) {
			if(label.match(/^ibmdi/))
				return label.substring(6);
			else
				return label;
		},
		
		sortComponentNames : function(data, category, parent) {
			var arr = new Array();
			dojo.forEach(data.entry, dojo.hitch(this, function(item) {
				var atom = new tdi.tdiatom({atom:item});
				if(atom.getCategory(category) != null) {
					var str = item.title.value;
					var label = this._getCompNameLabel(str);
					arr.push({id:str, name:label, url:item.content.src});
				}
			}));
			
			arr = arr.sort(function(a,b) {
				if(a.name == b.name)
					return 0;
				else if(a.name < b.name)
					return -1;
				else
					return 1;
			});
			var pi = {
					parent:parent,
					attribute:"items"
			}
			dojo.forEach(arr, dojo.hitch(this, function(item) {
				this.newItem(item, pi);
			}));
		}
	}
);
