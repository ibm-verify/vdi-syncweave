/*
 * IBM Confidential
 *
 *  OCO Source Materials
 *
 * 5724-D49
 *
 * (C) Copyright IBM Corporation. 2010
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 *
 * @version     %I%, %G%
 * @owner       
 * @history
 */

if (!dojo._hasResource["tdi.tdiconfig"]) {
	dojo._hasResource["tdi.tdiconfig"] = true;

	dojo.provide("tdi.tdiconfig")
	dojo.provide("tdi.baseconfig")
	
	dojo.require("dojox.xml.parser");

	dojo.declare("tdi.tdiconfig", null, {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getAssemblyLines : function() {
			var arr = [];
			dojo.query("AssemblyLine", this.config.documentElement).forEach(
					function(item) {
						arr.push(item.getAttribute("name"));
					});
			return arr.sort();
		},

		getAssemblyLine : function(assemblyline) {
			var elem = dojo.query("AssemblyLine[name='" + assemblyline + "']",
					this.config.documentElement);
			if (elem != null) {
				return new tdi.alconfig( {
					config : elem[0]
				});
			}
		}
	});

	dojo.declare("tdi.baseconfig", null, {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		toXML : function() {
			return dojox.xml.parser.innerXML(this.config.ownerDocument);
		},
		
		domText : function(node) {
			// var txt = null;
			// if(node.text != undefined)
			// txt = node.text;
			// else if(node.textContent != undefined)
			// txt = node.textContent;
			// else
			// txt = node.firstChild.nodeValue;
			if(node != null && node.firstChild != null)
				return node.firstChild.nodeValue;
			else
				return null;
		},
		
		getNodeText : function(qs, root) {
			var list = dojo.query(qs, (root == null || root == undefined ? this.config : root));
			if(list.length == 1) {
				return this.domText(list[0]);
			} else {
				return null;
			}
		},

		getFirstNode : function(qs, root) {
			var list = dojo.query(qs, root);
			if(list.length == 1) {
				return list[0];
			} else {
				return null;
			}
		},

		getName : function() {
			var name = this.config.getAttribute("name");
			if (name == null) {
				var nameNode = null;
				dojo.query("> Name", this.config).forEach(function(item) {
					name = item;
				});
				if (nameNode != null)
					name = this.domText(nameNode);
			}
			return name;
		},

		setInheritFrom : function(type) {
			var item = this.getFirstNode(">InheritFrom", this.config);
			if(item == null) {
				item = this.config.ownerDocument.createElement("InheritFrom");
				this.config.appendChild(item);
			}
			while(item.hasChildNodes()) {
				item.removeChild(item.firstChild);
			}
			var txt = this.config.ownerDocument.createTextNode(type);
			item.appendChild(txt);
		},
		
		isEnabled : function() {
			var enabled = null;
			dojo.query("> Enabled", this.config).forEach(function(item) {
				enabled = item;
			});
			if (enabled != null)
				return this.domText(enabled);
			else
				return enabled;
		},

		getScript : function() {
			var script = null;
			dojo.query(">Script", this.config).forEach(function(item) {
				script = item;
			});
			if (script != null)
				return this.domText(script);

			if (script == null) {
				dojo.query("> parameter[name='script']", this.config).forEach(
						function(item) {
							script = item;
						});
			}

			if (script != null)
				return this.domText(script);
			else
				return script;
		},

		getChildren : function() {
			return null;
		},

		isScript : function() {
			return this.config.tagName == "Script";
		},

		isFunction : function() {
			return this.config.tagName == "Function";
		},

		isLoop : function() {
			return this.config.tagName == "Loop";
		},

		isBranch : function() {
			return this.config.tagName == "Branch";
		},

		isConnector : function() {
			return this.config.tagName == "Connector";
		},

		isAssemblyLine : function() {
			return this.config.tagName == "AssemblyLine";
		},

		isPropertyConfig : function() {
			return this.config.tagName == "PropertyStore";
		},

		isContainer : function() {
			if (this.config.tagName == "ContainerEF")
				return true;
			else if (this.config.tagName == "ContainerDF")
				return true;
			else
				return false;
		},

		getType : function() {
			return this.config.tagName;
		},
		
		
		getParam : function(str) {
			var elem = this.getFirstNode(">parameter[name='" + str + "']", this.config);
			if(elem != null) {
				return this.domText(elem);
			}
			return null;
		},
		
		setParam : function(param, value) {
			var item = this.getFirstNode(">parameter[name='" + param + "']", this.config);
			if(item == null) {
				item = this.config.ownerDocument.createElement("parameter");
				item.attributes.setNamedItem("name", param);
				this.config.appendChild(item);
			}
			while(item.hasChildNodes()) {
				item.removeChild(item.firstChild);
			}
			if(value != null) {
				item.appendChild(this.config.ownerDocument.createTextNode(value));
			}
			return item;
		},
		
	});

	dojo.declare("tdi.mapitem", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},
		
		getName : function() {
			return this.getNodeText(">Name", this.config);
		},
		
		getType : function() {
			return this.getNodeText(">Type", this.config);
		},
		
		getSimple : function() {
			return this.getNodeText(">Simple", this.config);
		},
		
		isSimple : function() {
			return this.getType() == "simple";
		},
		
		isScript : function() {
			return this.getType() == "advanced";
		},
		
		setSimple : function(str) {
			this.setType("simple");
			var item = this.getFirstNode(">Simple", this.config);
			if(item == null) {
				item = this.config.ownerDocument.createElement("Simple");
				this.config.appendChild(item);
			}
			while(item.hasChildNodes()) {
				item.removeChild(item.firstChild);
			}
			var txt = this.config.ownerDocument.createTextNode(str);
			item.appendChild(txt);
		},
		
		setType : function(type) {
		}
	});
	
	dojo.declare("tdi.mapconfig", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
			this.names = null;
			this.map = null;
			this.getNames();
		},

		getNames : function() {
			if(this.names == null) {
				this.map = {};
				var arr = new Array();
				dojo.query(">AttributeMapItem", this.config).forEach(
						dojo.hitch(this, function(item) {
							var m = new tdi.mapitem({config:item});
							arr.push(m.getName());
							this.map[m.getName()] = m;
						}));
	
				this.names = arr.sort();
			}
			return this.names;
		},
		
		getItem : function(name) {
			this.getNames();
			return this.map[name];
		},
		
		isMapped : function(name) {
			return this.map[name] != undefined;
		},
		
		newItem : function(params) {
			var doc = this.config.ownerDocument;
			var item = doc.createElement("AttributeMapItem");
			
			var name = doc.createElement("Name");
			name.appendChild( doc.createTextNode(params.name) );
			item.appendChild(name);

			var type = doc.createElement("Type");
			type.appendChild( doc.createTextNode(params.type) );
			item.appendChild(type);
			 
			var name = doc.createElement("Simple");
			name.appendChild( doc.createTextNode(params.simple) );
			item.appendChild(name);
			
			this.config.appendChild(item);
			
			var m = new tdi.mapitem({config:item});
			this.names.push(m.getName());
			this.map[m.getName()] = m;
		},
		
	});

	dojo.declare("tdi.schemaconfig", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getNames : function() {
			var arr = new Array();
			dojo.query(">SchemaItem", this.config).forEach(
					dojo.hitch(this, function(item) {
						var e = dojo.query("> Name", item)[0];
						arr.push(this.domText(e));
					}));
			return arr.sort();
		}
	});

	dojo.declare("tdi.connection", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},		
	});
	
	dojo.declare("tdi.compconfig", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getConnectorMode : function() {
			var mode = "Script";
			dojo.query(">ConnectorMode", this.config).forEach(
					dojo.hitch(this, function(item) {
						mode = this.domText(item);
					}));
			return mode;
		},
		
		getConnectionConfig : function() {
			var config = null;
			dojo.query(">Configuration", this.config).forEach(
					dojo.hitch(this, function(item) {
						config = item;
					})
			);
			
			if(config != null)
				config = new tdi.connection({config:config});
				
			return config;
		},
		
		getConnectorType : function() {
			var ctype = "system:/Connectors/ibmdi.FileSystem";
			var elem = dojo.query(">Configuration >InheritFrom", this.config).forEach(dojo.hitch(this, function(item) {
				ctype = this.domText(item);
			}));

			if(ctype.charAt(0) != "[")
				return ctype;
			
			elem = dojo.query(">InheritFrom", this.config).forEach(dojo.hitch(this, function(item) {
				ctype = this.domText(item);
			}));

			return ctype;
		},
		
		getAttributeMap : function(inputMap) {
			var name = inputMap ? "Input" : "Output";
			var elem = dojo.query(">AttributeMap[name='" + name + "']",
					this.config)[0];
			if (elem == null)
				return null;
			else
				return new tdi.mapconfig( {
					config : elem
				});
		},

		getSchema : function(inputMap) {
			var name = inputMap ? "Input" : "Output";
			var elem = dojo.query(">Schema[name='" + name + "']",
					this.config)[0];
			if (elem == null)
				return null;
			else
				return new tdi.schemaconfig( {
					config : elem
				});
		},

		getChildren : function() {
			if (this.isBranch() || this.isLoop() || this.isContainer()) {
				var arr = new Array();
				var map = new Object();
				var root = this.config;
				if (this.isLoop()) {
					root = dojo.query(">Branch", this.config)[0];
				}
				dojo.query(">", root).forEach(function(item) {
					var conn = new tdi.compconfig( {
						config : item
					});
					if (conn.getName() != null)
						map[conn.getName()] = conn;
				});
				for (key in map) {
					arr.push(map[key]);
				}
				return arr;
			}
			return null;
		}
	});

	dojo.declare("tdi.alconfig", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getHook : function(hook) {
			var elem = dojo.query("Hook[name='" + hook + "']", this.config);
			if (elem.length == 1) {
				return new tdi.hookconfig( {
					config : elem
				});
			}
			return null;
		},

		getHooks : function() {
			var arr = new Array();
			dojo.query("Hook", this.config).forEach(function(item) {
				arr.push(new tdi.hookconfig( {
					config : item
				}));
			});
			return arr;
		},

		getChildren : function() {
			var arr = new Array();
			dojo.query("> ContainerEF, > ContainerDF", this.config).forEach(
					function(item) {
						var conn = new tdi.compconfig( {
							config : item
						});
						if (conn.getName() != null)
							arr.push(conn);
					});
			return arr;
		},

		getConnector : function(name) {
			var element = null;
			dojo.query("Connector[name='" + name + "']", this.config).forEach(
					function(item) {
						element = item;
					});
			if (element == null)
				return null;
			else
				return new tdi.compconfig( {
					config : element
				});
		}

	});

	dojo.declare("tdi.hookconfig", [ tdi.baseconfig ], {
		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		}
	});

	dojo.declare("tdi.AssemblyLineModel", null, {
		config : null,

		constructor : function(/* Object */args) {
			dojo.safeMixin(this, args);
		},

		getRoot : function(callback) {
			if (callback != undefined)
				callback(this.config);
			return this.config;
		},

		getChildren : function(args, callback) {
			var arr = args.getChildren();
			if (callback != undefined)
				callback(arr);
			return arr;
		},

		mayHaveChildren : function(a, b) {
			var mhc = false;
			try {
				var children = a.getChildren();
				if (children != null)
					mhc = true;
			} catch (err) {
			}
			return mhc;
		},

		getIdentity : function(item) {
			return item.getName();
		},

		getLabel : function(item) {
			return item.getName();
		},

		isItem : function(item) {
			return true;
		},

		fetchItemByIdentity : function(id) {
			alert("fetchItemByIdentity " + id)
		},

		newItem : function(_9, _a, _b) {
			alert("newItem: " + _9 + "; " + _a + "; " + _b);
		},

		pasteItem : function(_c, _d, _e, _f) {
			alert("pasteItem: " + _c + "; " + _d + "; " + _e + "; " + _f);
		},

		onChange : function(_10) {
			alert("onChange: " + _10);
		},

		onChildrenChange : function(parent, children) {
			alert("onChildrenChange: " + parent + "; " + children);
		},

		onDelete : function(item) {
			alert("OnDelete: " + item);
		}

	});
}
