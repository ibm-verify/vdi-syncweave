define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/attmapconfig",
], function(declare, tdibasecfg, tdiattmapconfig) {
return declare(
	[tdiattmapconfig],
	{
// 	dojo.declare("tdi.schemaconfig", [tdi.attmapconfig], {
		
		constructor: function(args) {
			declare.safeMixin(this, args);
		},
	
		getInheritedSchema : function() {
			var inh = this.getInheritedObj();
			if(!inh && this.getParent())
				inh = this.getParent().getInheritedObj();

			if(inh) {
				if(inh.declaredClass == "tdi.config.connector") {
					return inh.getSchema(this.getName());
				} else if (inh.declaredClass == "tdi.config.schemaconfig") {
					return inh;
				}
			}
			return null;
		},

		newItem : function(item) {
			var arr = this.getArray("item");
			arr.push(item);
			this.setModified(true);
			this._maps[item.name] = new tdibasecfg({config:item, parentConfig:this});
			return this._maps[item.name];
		},
		
		getItem : function(name) {
			if(this._maps[name] == null) {
				this._maps[name] = this._getItem(name);
			}
			return this._maps[name];
		},
		
		_getItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name == name;
			});
			if(arr.length == 1)
				return new tdibasecfg({config:arr[0], parentConfig:this});
			else
				return null;
		},
		
		removeAllItems : function() {
			this.setObject("item", []);
			this._maps = [];
		},
		
		removeItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name != name;
			});
			this.setObject("item", arr);
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			var p = this.getInheritedSchema();
			if(p) {
				dojo.forEach(p.getNames(), function(name) {
					if(dojo.indexOf(arr, name) == -1) {
						arr.push(name);
					}
				});
			}
			return arr.sort();
		}

	});


});
