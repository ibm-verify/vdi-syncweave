/**
 * The AssemblyLineEditor shows an assemblyline in a graphical view.
 */
define([
	"dojo/_base/declare",
	"dojo/data/ItemFileWriteStore",
	"tdi/tdiapi",
	"tdi/tdiconfig"
], function(declare, ItemStore, tdiapi, tdiconfig) {
	
return declare(
	[ ItemStore ],
	{
		componentType: "connector",
		
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			
			this.data = {
				identifier: "id",
				label: "name",
				items : []
			}
			this.loadData();
		},
		
		onLoadComplete : function(model) {
			// summary:
			//		Callback when model data loaded
		},
		
		_translateLocale : function(includeCountry) {
			var str = dojo.locale;
			var index = str.indexOf("-");
			if(index != -1) {
				var lang = str.substring(0, index);
				var country = str.substring(index+1);
				if(includeCountry)
					str = lang + "_" + country.toUpperCase();
				else
					str = lang;
			}
			return str;
		},
		
		_getCompNameLabel : function(label, titles) {
			var l = label;
			if(label.match(/^ibmdi/))
				l = label.substring(6);
			
			if(titles) {
				if(titles[dojo.locale])
					l = titles[dojo.locale];
				else if (titles[this._translateLocale(true)])
					l = titles[this._translateLocale(true)];
				else if (titles[this._translateLocale(false)])
					l = titles[this._translateLocale(false)];
			}
			
			return l;
		},
		
		_filterComponentNames : function(data, category) {
			var arr = new Array();
			dojo.forEach(data.entry, dojo.hitch(this, function(item) {
				var atom = new tdi.tdiatom({atom:item});
				if(atom.getCategory(category) != null) {
					var str = item.title.value;
					var label = this._getCompNameLabel(str, item.title.otherAttributes);
					arr.push({id:str, name:label});
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
			return arr;
		},
		
		_updateModel : function(data) {
			var arr = this._filterComponentNames(data, this.componentType);
			dojo.forEach(arr, dojo.hitch(this, function(item) {
				this.addItem(item);
			}));
			
			var adapters = tdiapi.getNamespace("adapter");
			if(adapters != null) {
				dojo.forEach(adapters.getConnectorNames(),  dojo.hitch(this, function(conn) {
					this.addItem({
						id:"adapter:/Connectors/" + conn,
						name:conn
					});
				}));
			}
			
			this.onLoadComplete(this);
		},
		
		loadData : function() {
			dojo.when(tdiapi.getInstalledComponents(), dojo.hitch(this, "_updateModel"));
		},
		
		addItem : function(item, parentInfo) {
			// summary:
			// 		Add a node to the tree
			return this.newItem(item, parentInfo);
		}
	}
)});