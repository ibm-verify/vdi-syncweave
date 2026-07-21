define([
	"dojo/_base/declare",
	"tdi/config/basecfg",
	"tdi/config/attmapitemconfig"
], function(declare, basecfg, tdiattmapitemconfig) {
return declare(
	[basecfg],
	{
// 	dojo.declare("tdi.attmapconfig", [tdi.basecfg], {
		
		_maps: null,
		
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this._maps = new Object();
		},
		
		getInheritedMap : function() {
			var inh = this.getInheritedObj();
			if(inh) {
				if(inh.declaredClass == "tdi.config.connector") {
					return inh.getAttributeMap(this.getName());
				} else if (inh.declaredClass == "tdi.config.attmapconfig") {
					return inh.getAttributeMap();
				}
			}
			return null;
		},
		
		getNames : function() {
			var arr = [];
			dojo.forEach(this.getArray("item"), function(item) {
				arr.push(item.name);
			});
			
			var map = this.getInheritedMap();
			if(map) {
				dojo.forEach(map.getNames(), function(name) {
					if(dojo.indexOf(arr, name) == -1) {
						arr.push(name);
					}
				});
			}
			return arr.sort();
		},
		
		isMapped : function(name) {
			return dojo.some(this.getArray("item"), function(item) {
				return item.name == name;
			});
		},
		
		removeAllItems : function() {
			this.setObject("item", []);
			this._maps = new Object();
		},
		
		removeItem : function(name) {
			var arr = dojo.filter(this.getArray("item"), function(item) {
				return item.name != name;
			});
			this.setObject("item", arr);
		},
		
		newItem : function(item) {
			var arr = this.getArray("item");
			if(item.enabled == undefined)
				item.enabled = "true";
			if(item.add == undefined)
				item.add = "true";
			if(item.modify == undefined)
				item.modify = "true";
			if(item.type == undefined)
				item.type = "Simple";
			if(item.mapsTo == undefined)
				item.mapsTo = item.name;
			arr.push(item);
			this.setModified(true);
			var ami = new tdiattmapitemconfig({config:item, parentConfig:this});
			this._maps[item.name] = ami;
			return ami;
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
			if(arr.length == 1) {
				return new tdiattmapitemconfig({config:arr[0], parentConfig:this});
			}
			
			var map = this.getInheritedMap();
			if(map)
				return map.getItem(name);
			else
				return null;
		}

	});
	
});
