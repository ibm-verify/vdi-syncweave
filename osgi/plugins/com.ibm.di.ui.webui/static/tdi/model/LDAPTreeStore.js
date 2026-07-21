/**
 * LDAPTreeModel
 * 
 * This model requires the following parameters in the "params" object
 * 
 * target.ldap.url - 	The LDAP URL
 * target.ldap.user - 	The username
 * target.ldap.password	The password
 * 
 * Optionally you can provide a starting search base:
 * 
 * target.ldap.searchbase - The starting point in the DIT
 * 
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
	
	// ldap: Object
	//		This object must be passed to provide the LDAP connection details
	//			target.ldap.url: String
	//			target.ldap.user: String
	//			target.ldap.password: String
	ldap: null,
	
	// headers: Object
	//		Additional headers to pass in all requests to the server. These can be overridden
	//		by passing additional headers to calls to the store.
	headers: {},

	// target: String
	//		The target base URL to use in our REST calls
	//		Defaults to /ldapsync/LDAPSync/UI_LDAP
	target: tdiapi._url_prefix + "/ldapsync/runal/LDAPSync/UI_LDAP",
	
	// accepts: String
	//		The content-type we expect and post
	accepts: "application/json",
	
	// idProperty: String
	//		The property that uniquely identifies entries from the source
	idProperty: "$dn",
	
	// schema: Object
	//		This object caches schema definition
	schemaCache: {},
	
	constructor: function(args) {
		this.ldap = new Object();
		if(args)
			lang.mixin(this, args);
	},
	
	addItems: function(arr, parent) {
		var t = this;
		array.forEach(arr, function(item) {
			t.put(item, {parent:parent});
		});
	},
	
	onError: function(err) {
		// summary:
		//		Callback when som error occurs
	},
	
	_buildSchemaObject: function(oclist) {
		// summary:
		//		Create an object containing list of object class definitions
		//		plus the __syntax__ property for attribute definitions.
		var schema = new Object();
		var schemaCache = this.schemaCache;
		schema.__syntax__ = schemaCache.__syntax__;
		array.forEach(oclist, function(oc) {
			var key = oc == "*" ? "objectclasses" : oc;
			schema[key] = schemaCache[key];
		});
		return schema;
	},
	
	readSchema: function(objectclass, includeSuper) {
		// summary:
		//		Returns the schema definition for a specific LDAP object class
		//		Use "*" or null to return all object class names
		var t = this;
		var oclist = objectclass;
		
		// Check what we have and build a new list of missing classes
		if(!oclist) {
			oclist = ["*"];
		} else if(!lang.isArray(oclist)) {
			oclist = objectclass.split(",");
		}
		oclist = array.map(oclist, function(str) {
			return str.toLowerCase();
		});
		
		var list = oclist;
		if(!includeSuper) {
			list = array.filter(oclist, function(oc) {
				return !t.schemaCache[oc];
			});
			
			// If we have cached all object classes then return immediatly
			if(list.length == 0) {
				var def = new Deferred();
				def.resolve(this._buildSchemaObject(oclist));
				return def;
			}
		}
		
		var headers = lang.mixin({Accept: this.accepts, "Content-Type":this.accepts}, this.headers);
		
		var ldap = this._getLdap();
		
		ldap.iwe = {
			objectclasses:list.join(",")
		}
		
		var t = this;
		return request.post(this.target, {
			handleAs: tdiapi._format,
			headers: headers,
			data: dojo.toJson(ldap)
		}).then(function(data) {
			var result = {};
			for(var f in data[0]) {
				result[f.toLowerCase()] = data[0][f];
			}
			array.forEach(list, function(oc) {
				if(oc == "*")
					t.schemaCache.objectclasses = result.objectclasses;
				else
					t.schemaCache[oc] = result[oc];
			});
			t.schemaCache.__syntax__ = t.schemaCache.__syntax__ || {};
			for(var f in result.__syntax__) {
				t.schemaCache.__syntax__[f.toLowerCase()] = result.__syntax__[f];
			}
			if(includeSuper)
				return result;
			else
				return t._buildSchemaObject(oclist);
		});
	},
	
	readEntry: function(dn, initialWorkEntry) {
		var headers = lang.mixin({Accept: this.accepts, "Content-Type":this.accepts}, this.headers);
		
		var ldap = this._getLdap();
		
		ldap.initParams["target.ldap.searchbase"] = dn;
		ldap.initParams["target.ldap.searchscope"] = "baselevel";
		ldap.initParams["target.ldap.searchfilter"] = "objectclass=*";
		if(initialWorkEntry) {
			ldap.iwe = initialWorkEntry;
		}
		
		return request.post(this.target, {
			handleAs: tdiapi._format,
			headers: headers,
			data: dojo.toJson(ldap)
		});
	},
	
	modifyEntry: function(dn, object) {
		var headers = lang.mixin({Accept: this.accepts, "Content-Type":this.accepts}, this.headers);
		
		var ldap = this._getLdap();
		ldap.initParams["target.ldap.searchbase"] = dn;
		ldap.initParams["target.ldap.searchscope"] = "baselevel";
		
		ldap.iwe = lang.mixin({}, object);
		ldap.iwe["$operation"] = "modify";
		if(!ldap.iwe["$dn"])
			ldap.iwe["$dn"] = dn;
		
		return request.post(this.target, {
			handleAs: tdiapi._format,
			headers: headers,
			data: dojo.toJson(ldap)
		});
	},
	
	addEntry: function(dn, object) {
		var headers = lang.mixin({Accept: this.accepts, "Content-Type":this.accepts}, this.headers);
		
		var ldap = this._getLdap();
		ldap.initParams["target.ldap.searchbase"] = dn;
		ldap.initParams["target.ldap.searchscope"] = "baselevel";
		
		ldap.iwe = lang.mixin({}, object);
		ldap.iwe["$operation"] = "add";
		
		return request.post(this.target, {
			handleAs: tdiapi._format,
			headers: headers,
			data: dojo.toJson(ldap)
		});
	},
	
	removeEntry: function(id){
		var headers = lang.mixin({Accept: this.accepts, "Content-Type":this.accepts}, this.headers);
		
		var ldap = this._getLdap();
		
		ldap.iwe = {};
		ldap.iwe["$dn"] = id;
		ldap.iwe["$operation"] = "delete";
		
		return request.post(this.target, {
			handleAs: tdiapi._format,
			headers: headers,
			data: dojo.toJson(ldap)
		});
	},
	
	_getLdap: function() {
		var ldap = {};
		for(var f in this.ldap) {
			ldap[f] = this.ldap[f];
		}
		return {initParams:ldap};
	},
	
    getChildren: function(object) {
    	if(object["$type"] == "Search") {
    		var search = lang.mixin(object, {"$type":"ExecSearch"});
    		return this.query(search);
    	} else {
    		return this.query({parent: object["$dn"]});
    	}
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
		options = options || {};
		query = query || {};
		
		//
		// -- Return query object in case it is a search item
		//
		if(query["$type"] == "Search") {
			return new QueryResults([query]);
		}

		var headers = lang.mixin({ Accept: this.accepts, "Content-Type":this.accepts }, this.headers, options.headers);
		
		var ldap = this._getLdap();

		if(options.start >= 0 || options.count >= 0){
			ldap["start"] = options.start;
			ldap["count"] = options.count;
		}
		
		// Build the LDAP search filter based on values in the query object
		var filter = [];
		
		// Default is to read the root object
		ldap.initParams["target.ldap.searchbase"] = "";
		ldap.initParams["target.ldap.searchscope"] = "baselevel";
		
		var queryFields = [];
		
		for(var f in query){
			queryFields.push(f);
			
			// Reading a specific item
			if(f == "$dn") {
				ldap.initParams["target.ldap.searchbase"] = query[f] || "";
				ldap.initParams["target.ldap.searchscope"] = "baselevel";
				// Use "*" and "+" to make sure we get operational attributes.
				// OpenLDAP won't return unless "+" is present, and other servers
				// will return nothing if only "+" is specified.
				// Having both seems to work for all ldap servers.
				if(ldap.initParams["target.ldap.searchbase"] == "")
					ldap.initParams["target.ldap.attributes"] = "*\n+";
				
				// unless it's SDS of course, then it won't return anything
				// the target cn=configuration and children


			} else if(f == "parent") {
				// special case for the root LDAP object
				// use the 'namingcontexts' to generate child entries
				// or list the children of ldapSearchBase
				if(query[f] == "" && this.root && ldap.initParams["target.ldap.rootdn"]) {
					ldap.initParams["target.ldap.searchbase"] = ldap.initParams["target.ldap.rootdn"];
					ldap.initParams["target.ldap.searchscope"] = "onelevel";
						
				} else if(query[f] == "" && this.root) {
					var result = [];
					var key = "namingcontexts";
					for(var f in this.root) {
						if(f.toLowerCase() == "namingcontexts") {
							key = f;
							break;
						}
					}
					var contexts = this.root[key];
					if(typeof(contexts) == "string")
						contexts = [contexts];
					
					array.forEach(contexts, function(item) {
						result.push({
							"$dn":item,
							name:item,
							contextroot:true
						});
					});
					return new QueryResults(result);
					
				} else {
					ldap.initParams["target.ldap.searchscope"] = "onelevel";
					ldap.initParams["target.ldap.searchbase"] = query[f];
				}
				
			} else {
				filter.push("(" + query[f] + ")");
			}
		}

		if(filter.length == 0) {
			ldap.initParams["target.ldap.searchfilter"] = "objectclass=*";
			// -- for an empty query with a rootdn specified, we list the immediate child items of that branch 
			if(queryFields.length == 0 && ldap["target.ldap.rootdn"]) {
				ldap.initParams["target.ldap.searchbase"] = ldap.initParams["target.ldap.rootdn"];
				ldap.initParams["target.ldap.searchscope"] = "onelevel";
			}
		} else {
			ldap.initParams["target.ldap.searchfilter"] = "(&" + filter.join("") + ")";
		}
		
		//
		// -- ExecSearch?
		//
		if(query["$type"] == "ExecSearch") {
			ldap.initParams["target.ldap.searchbase"] = query.base;
			ldap.initParams["target.ldap.searchfilter"] = query.filter;
			ldap.initParams["target.ldap.searchscope"] = query.scope || "subtree";
		}
		
		//
		// -- when listing or searching we need to set a search limit
		//
		if(ldap.initParams["target.ldap.searchscope"] != "baselevel")
			ldap["count"] = 100;
		
		
		var results = dojo.xhrPost({
			handleAs: tdiapi._format,
			preventCache: tdiapi._preventCache,
			headers: headers,
			postData: dojo.toJson(ldap),
			url : this.target
		});
		
		var t = this;
//		results.total = results.then(function(data){
		return results.then(function(data){
			if(data) {
				if(data.length == 1 && data[0]["$dn"] == "") {
					t.root = data[0];
				} else if(data.length == 1 && data[0]["$dn"] == t.ldap["target.ldap.rootdn"]) {
					data[0].contextroot = true;
				}

//				return data.length;
				return data;
			} else {
//				return 0;
				return null;
			}
		}, function(err) {
			tdiutil.error(err);
			t.onError(err);
			return null;
		});
		return QueryResults(results);
	}
    
});
});
