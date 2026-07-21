/**
 * ConfigTreeStore
 *
 * This store provides a tree store of one or more configuration files.
 * 
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/store/Memory",
	"dojo/store/util/QueryResults",
	"tdi/tdiconfig",
	"tdi/tdiapi"
], function(declare, lang, array, Memory, QueryResults, tdiconfig, tdiapi) {
	
return declare( [Memory], {
	
	// configId: Array or String
	//		This object must be the configuration identifier(s) to use as source
	configId: null,
	
	// idProperty: String
	//		The property that uniquely identifies entries from the source
	idProperty: "name",
	
    getChildren: function(object) {
    	if(object.type == "root") {
    		return this.query({root: true});
    	} else if(object.url) {
        	return this.query({item: object});
    	} else if(object.type == "folder") {
        	return this.query({folder: object.name});
    	}
    	return null;
    },
    
	query: function(query, options) {
		// summary:
		//		Sends a query to the UI_LDAP assemblyline to return entries based on the query
		// query: Object
		//		The query to use for retrieving objects from the store.
		// options: __QueryOptions?
		//		The optional arguments to apply to the resultset.
		// returns: dojo/store/api/Store.QueryResults
		//		The results of the query, extended with iterative methods.
		query = query || {};
		
		var results = [];
		if(query.item) {
			if(!query.item.config) {
				results = tdiapi.getConfig(query.item.entry).then(function(data) {
					query.item.config = new tdiconfig({config:data});
					return array.map(query.item.config.getAssemblyLineNames(), function(name) {
						return {name:name, parent:query.item, config:query.item.name, type:"assemblyline"};
					});
				});
			} else {
				results = array.map(query.item.config.getAssemblyLineNames(), function(name) {
					return {name:name, parent:query.item, type:"assemblyline"};
				});
			}
			
		} else if (query.root){
			results = tdiapi.getServerProjects(true);
			results.total = results.then(function(data){
				return data.length;
			});
			
		} else {
			return [{
				name:"Root",
				type:"root"
			}];
			
		}
		return QueryResults(results);
	}
    
});
});
