/**
 * FileBrowserStore
 */
define([
	"dojo/_base/declare",
	"dojo/_base/lang",
	"dojo/_base/array",
	"dojo/Deferred",
	"dojo/request",
	"dojo/store/Memory",
	"dojo/store/util/QueryResults",
	"tdi/tdiutil",
	"tdi/tdiapi"
], function(declare, lang, array, Deferred, request, Memory, QueryResults, tdiutil, tdiapi) {
	
return declare( [Memory], {
	
	// idProperty: String
	//		The property that uniquely identifies entries from the source
	idProperty: "path",
	
	onError: function(err) {
		// summary:
		//		Callback when som error occurs
	},
	
	_onRootSet: function(data) {
		this.roots = array.map(data.roots, function(r) {
			if(r.path.indexOf(":\\") != -1)
				r.path = r.path.substring(0, r.path.length-1);
			return r;
		});
		this.root = {
			path: data.path,
			name: data.path
		};
		this.top = data.top.replace(":\\", ":");
		this.onRootSet(this.root);
	},
	
	onRootSet: function(root) {
		// summary:
		//		Callback when root path for this session is set
	},
	
    getChildren: function(object) {
    	return this.query({parent: object["path"]});
    },
    
	query: function(query, options) {
		// summary:
		//		Sends a file list query via tdiapi
		// query: Object
		//		The query to use for retrieving objects from the store (should contain parent)
		// returns: dojo/store/api/Store.QueryResults
		//		The results of the query, extended with iterative methods.
		options = options || {};
		query = query || {};

		var parent = query.parent || (query.parent == "" ? "" : ".");
		var result = tdiapi.listFiles(parent);
		var t = this;
		
		// -- if we return a failure the Tree widget will keep
		// -- spinning its loading icon.
		return result.then(function(data){
			if(!t.root) {
				t._onRootSet(data);
				return [t.root];
			} else {
				return data.files;
			}
		}, function(err) {
			tdiutil.error(err);
			t.onError(err);
			return null;
		});
	}
    
});
});
