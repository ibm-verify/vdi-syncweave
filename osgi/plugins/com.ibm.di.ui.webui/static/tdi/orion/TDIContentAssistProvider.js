define([
        'dojo/_base/array',
    	'orion/editor/templates'
], function(array, mTemplates) {
	
	
	var templates = [
		{
			prefix: "dump", //$NON-NLS-0$
			description: "dump - dump work entry",
			template: "task.dumpEntry(work);" //$NON-NLS-0$
		},
		{
			prefix: "dump", //$NON-NLS-0$
			description: "dump - dump connentry",
			template: "task.dumpEntry(conn);" //$NON-NLS-0$
		}
	];
	
	var keywords = [
		 "system",
		 "session",
		 "main",
		 "task",
		 "work",
		 "conn",
		 "current",
		 "error",
		 "entry",
		 "result",
		 "thisConnector",
		 "thisComponent",
		 "search",
		 "connector",
		 "config",
		 "old",
		 "out",
		 "inp",
		 "parser",
		 "source"
	];
	
	var keywords_class = [
   		 "com.ibm.di.function.UserFunctions",
   		 "com.ibm.di.api.",
   		 "com.ibm.di.server.RS",
   		 "com.ibm.di.server.AssemblyLine",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.entry.Entry",
   		 "com.ibm.di.server.SearchCriteria",
   		 "connector",
   		 "com.ibm.di.config.base.BaseConfigurationImpl",
   		 "com.ibm.di.entry.Entry",
   		 "java.io.BufferedWriter",
   		 "java.io.BufferedReader",
   		 "com.ibm.di.parser.ParserImpl",
   		 "source"
   	];
	
	TDIContentAssistProvider.class_map = {};
	
	/**
	 * @name orion.editor.TDIContentAssistProvider
	 * @class Provides content assist for CSS keywords.
	 */
	function TDIContentAssistProvider(config) {
		this.config = config;
	}
	
	TDIContentAssistProvider.prototype = new mTemplates.TemplateContentAssist(keywords, templates);
	
	/**
	 * Finds the class description for parent.obj
	 */
	TDIContentAssistProvider.prototype.getClassDescription = function(parent, obj) {
		var cls = null;
		if(!parent) {
			cls = this.getClassForTopLevel(obj);
		} else {
			var item = this.findMethodOrField(parent, obj);
			if(!item)
				return null;
			else
				cls = item.returnType;
		}
		
		return this.loadClassDefinition(cls);
	};

	/**
	 * Returns the item definition for obj within class definition parent
	 */
	TDIContentAssistProvider.prototype.findMethodOrField = function(parent, obj) {
		var str = obj;
		if(str.indexOf("(") != -1)
			str = str.substring(0, str.indexOf("("));
		var re = new RegExp(str, "i");
		var arr = array.filter(parent.items, function(item) {
			return(re.test(item.name));
		});
		// return first match
		if(arr && arr.length > 0) {
			return arr[0];
		}
		return null;
	};

	/**
	 * Returns a subset of item definitions that matches obj
	 */
	TDIContentAssistProvider.prototype.findMatchingMethodsOrFields = function(parent, obj) {
		var str = obj;
		if(str.indexOf("(") != -1)
			str = str.substring(0, str.indexOf("("));
		var re = new RegExp("^"+str, "i");
		return array.filter(parent.items, function(item) {
			return(re.test(item.name));
		});
	};
	/**
	 * Returns the class name based on a top-level name
	 */
	TDIContentAssistProvider.prototype.getClassForTopLevel = function(obj) {
		for(var i = 0; i < keywords.length; i++) {
			if(keywords[i] == obj)
				return keywords_class[i];
		}
		// -- could be a connector
		var alconfig = this.getAssemblyLine();
		if(alconfig && alconfig.getComponentByName(obj) != null) {
			return "com.ibm.di.server.AssemblyLineComponent";
		}
		return null;
	};
	
	TDIContentAssistProvider.prototype.loadClassDefinition = function(className) {
		var t = this;
		if(!t.getClassMap(className)) {
			dojo.xhrGet({
				sync:true,
				handleAs:"json",
				url:"/fds/script?class="+className,
				error: function(err) {
					t.setClassMap(className, []);
					console.log("Can't load class map for: " + className);
				},
				load: function(data) {
					data.items = data.items.sort(function(a,b) {
						if(a.name < b.name)
							return -1;
						else if(a.name > b.name)
							return 1;
						else
							return 0;
					});
					t.setClassMap(className, data);
				}
			});
		}
		return t.getClassMap(className);
	};
	
	TDIContentAssistProvider.prototype.getClassMap = function(className) {
		return TDIContentAssistProvider.class_map[className];
	}
	
	TDIContentAssistProvider.prototype.setClassMap = function(className, map) {
		TDIContentAssistProvider.class_map[className] = map;
	}
	
	/**
	 * Returns the prefix to use with content assist
	 */
	TDIContentAssistProvider.prototype.getPrefix = function(buffer, offset, context) {
		var index = offset;
		while (index && /[A-Za-z\-\@\.\[\]"' ]/.test(buffer.charAt(index - 1))) {
			index--;
		}
		return index ? buffer.substring(index, offset) : "";
	};
	
	TDIContentAssistProvider.prototype.getAssemblyLine = function() {
		if(this.config)
			return this.config.getAssemblyLine();
		else
			return null;
	};
	
	/**
	 * Compute content assist based on current postion and TDI context.
	 */
	TDIContentAssistProvider.prototype.computeProposals = function(buffer, offset, context) {
		var prefix = this.getPrefix(buffer, offset, context);
		var proposals = [];
		if (!this.isValid(prefix, buffer, offset, context)) {
			return proposals;
		}

		//
		// Add work/connector attributes first
		//
		var alconfig = this.getAssemblyLine();
		if(alconfig) {
			array.forEach(alconfig.getConnectorNames(), function(comp) {
				var name = comp.name;
				if(name.indexOf(prefix) == 0) {
					proposals.push({proposal: name.substring(prefix.length), description: name});
				}
			});

			proposals = proposals.concat(this.getWorkAttributes(alconfig, prefix));
		}

		//
		// In this step traverse the expression to find the next to last
		// class definition. Then generate content assist from the class definition
		// matching the last list item.
		var list = this.parseExpression(buffer, offset);
		if(list.length > 1) {
			var parent = this.getClassDescription(null, list.pop());
			while(parent && list.length > 1) {
				var next = this.getClassDescription(parent, list.pop());
				if(!next && parent.className == "com.ibm.di.entry.Entry") {
					next = this.loadClassDescription("com.ibm.di.entry.Attribute");
				}
				parent = next;
			}
			if(parent && list.length == 1) {
				var str = list.pop();
				array.forEach(this.findMatchingMethodsOrFields(parent, str), function(item) {
					proposals.push({proposal:item.content.substring(str.length), description:item.description});
				});
			}
		}

		proposals = proposals.concat(this.getTemplateProposals(prefix, offset, context));
		proposals = proposals.concat(this.getKeywordProposals(prefix));
		return proposals;
	};
	
	TDIContentAssistProvider.prototype.getWorkAttributes = function(alconfig, prefix) {
		var proposals = [];
		if(prefix.indexOf("work.") == 0) {
			array.forEach(alconfig.getWorkAttributes().sort(), function(attr) {
				var name = "work." + attr;
				if(name.indexOf(prefix) == 0) {
					proposals.push({proposal: name.substring(prefix.length), description: name});
				}
			});
		}
		return proposals;
	};

	TDIContentAssistProvider.prototype.parseExpression = function(buffer, offset) {
		var list = new Array();
		var start = offset - 1;
		var end = start;
		var openParens = 0;
		var openBracket = 0;

		try {
			var oldch = 0xff;
			while (start >= 0) {
				var ch = buffer.charAt(start--);

				if (ch == ')') {
					openParens++;
				} else if (ch == '(' && openParens > 0) {
					openParens--;
				} else if (ch == ']') {
					openBracket++;
				} else if (ch == '[' && openBracket > 0) {
					openBracket--;
					// -- This may an attribute: work["http.body"]
					try {
						var token = buffer.substr(start + 2, (end - start) - 2);
						list.push(token);
					} catch (err) {
					}
					// -- we should never include bracket contents in the
					// expression
					end = start;
					//
				} else if (openBracket > 0 || openParens > 0) {
					// Skip expressions inside brackets/parents.
					continue;
				} else if (ch == '.') {
					list.push(buffer.substr(start + 2, (end - start) - 1));
					end = start;
				} else if (ch.match(/\s/) && oldch == '.') {
					// Permit expressions to cross line boundaries
				} else if (ch.match(/[A-Za-z_]/)) {
					// part of a javascript identifier
				} else if (ch == '"') {
					continue;
				} else {
					start++;
					break;
				}
				oldch = ch;
			}

			if (start < end)
				list.push(buffer.substr(start + 1, (end - start)));

		} catch (e) {
			console.error(e);
		}

		return list;
	};

	return {
		TDIContentAssistProvider: TDIContentAssistProvider
	};
});
