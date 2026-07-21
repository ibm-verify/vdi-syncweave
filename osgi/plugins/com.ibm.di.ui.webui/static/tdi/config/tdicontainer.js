define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"tdi/config/basecfg",
	"tdi/config/tdibranch",
	"tdi/config/connector",
	"tdi/config/parserconfig",
	"tdi/config/scriptconfig",
	"tdi/config/almapconfig"
], function(declare, dArray, basecfg, tdibranch, tdiconnector, tdiparser, tdiscriptconfig, tdialmapconfig) {
return declare(
	"tdi.config.tdicontainer",
	[basecfg],
	{
// 	dojo.declare("tdi.tdicontainer", [tdi.basecfg], {
		constructor : function(/* Object */args) {
			declare.safeMixin(this, args);
			this.loadComponents();
		},
		
		isBranch : function() {
			return this.getSubType() == "branch";
		},
		
		isSwitch: function() {
			return this.getBranchType() == "Switch";
		},
		
		isCase: function() {
			return this.getBranchType() == "Case";
		},
		
		isIf: function() {
			return this.getBranchType() == "If";
		},
		
		isElse: function() {
			return this.getBranchType() == "Else";
		},
		
		isElseIf: function() {
			return this.getBranchType() == "ElseIf";
		},
		
		setEnabled: function(enabled) {
			if(enabled != this.isEnabled())
				this.setState(enabled ? "Enabled" : "Disabled");
		},
		
		getEnabled: function() {
			return "Enabled" == this.getState();
		},
		
		setState : function(state) {
			this.setObject("state", state);
		},
		
		getState : function() {
			return this.getObject("state");
		},
		
		getBranchType : function() {
			// summary:
			//		Returns the branch type of the configuration (Switch, Case ...)
			return this.getObject(this.getKey("type"));
		},
		
		loadComponents: function() {
			if(!this.components) {
				this.components = new Object();
				dojo.forEach(this.getArray("component"), dojo.hitch(this, function(comp) {
					this.components[comp.name] = this.createTDIComponent(comp, this);
				}));
			}
		},
		
		getComponent : function(name, recursive) {
			if(!this.components[name]) {
				dojo.forEach(this.getArray("component"), dojo.hitch(this, function(comp) {
					if(comp.name == name) {
						this.components[comp.name] = this.createTDIComponent(comp, this);
					}
				}));
//				for(f in this.components) {
//					var comp = this.components[f];
//					if(comp.isContainer()) {
//						var c = comp.getComponent(name);
//						if(c) {
//							return c;
//						}
//					}
//				}
			}
			
			if(!this.components[name] && recursive) {
				for(var comp in this.components) {
					var cc = this.components[comp];
					if(cc.isContainer()) {
						var c = cc.getComponent(name, true);
						if(c != null) {
							return c;
						}
					}
				}
			}
			
			return this.components[name];
		},
		
		getComponentCount: function() {
			return this.getArray("component").length;
		},
		
		getComponentAt: function(index) {
			var arr = this.getArray("component");
			if(arr && arr.length > index) {
				return this.getComponent(arr[index].name);
			}
		},
		
		getComponentNames : function() {
			var names = new Array();
			dojo.forEach(this.getArray("component"), dojo.hitch(this, function(comp) {
				if(comp.name) {
					names.push(comp.name);
				}
			}));
			return names;
		},
		
		getPlaceholder: function() {
			if(!this.placeholder) {
				this.placeholder = new basecfg({
					config:{},
					parentConfig: this,
					isPlaceholder:true
				});
				this.placeholder.setName("[drop component here]");
			}
			return this.placeholder;
		},
		
		createComponent: function(type) {
			var name = "";
			for(var i = 1; i < 100; i++) {
				name = this.getName() + i;
				if(!this.components[name])
					break;
			}
			var comp = null;
			if(type == "connector") {
				comp = this.getAssemblyLine().createConnector(name, "AddOnly");
			} else if(type == "script") {
				comp = this.getAssemblyLine().createScriptComponent(name);
			}
			this.addComponent(comp, -1);
			return comp;
		},
		
		deleteComponent: function(name) {
			var arr = this.getArray("component");
			arr = dArray.filter(arr, function(comp) {
				return comp.name != name;
			});
			this.setObject("component", arr);
			if(this.components[name])
				delete this.components[name];
		},
		
		addComponent: function(newcomp, location, before) {
			// location: String(insert before named component) - Integer(location)
			var arr = this.getArray("component");
			var loc = -1; // append by default
			if(typeof(location) == "string") {
				for(loc = 0; loc < arr.length; loc++) {
					if(arr[loc].name == location) {
						break;
					}
				}
			}
			
			// -- insert before?
			if(loc != -1 && !before) {
				loc++;
			}
			
			if(loc == -1) {
				arr.push(newcomp.config);
			} else {
				var a1 = arr.slice(0, loc);
				a1.push(newcomp.config);
				if(loc < arr.length)
					a1 = a1.concat(arr.slice(loc));
				this.setObject("component", a1);
			}
			
			// -- relocated component
			newcomp.parentConfig = this;
			
			if(!this.components[newcomp.getName()]) {
				this.components[newcomp.getName()] = newcomp;
			}
			
		},
		
		getBranchConfig : function() {
			return this.getCompositeConfig();
		},
		
		getCompositeConfig : function() {
			if(!this.compositeConfig) {
				var type = this.getType();
				if(type == "composite") {
					this.compositeConfig = new tdibranch({config:this.getObject("compositeConfig"), parentConfig:this});
				}
			}
			return this.compositeConfig;
		},
		
		getContainerType : function() {
			var cfg = this.getConfig();
			if(cfg && cfg["@type"])
				return cfg["@type"];
			else
				return this.getType();
		},
		
		createTDIComponent : function(config, parent) {
			if(config["@type"] == "composite") {
				return new tdi.config.tdicontainer({config:config, parentConfig:parent})
				
			} else if(config["@type"] == "complex") {				
				if(config.complexConfig["@type"] == "connector") {
					return new tdiconnector({config:config, parentConfig:parent});
					
				} else if(config.complexConfig["@type"] == "function") {
						return new tdiconnector({config:config, parentConfig:parent});
						
				} else if(config.complexConfig["@type"] == "parser") {
					return new tdiparser({config:config, parentConfig:parent});
					
				}
				
			} else if(config["@type"] == "simple") {
				if(config.simpleConfig["@type"] == "script") {
					return new tdiscriptconfig({config:config, parentConfig:parent});
					
				} else if(config.simpleConfig["@type"] == "map") {
					return new tdialmapconfig({config:config, parentConfig:parent});
				}
				
			}
			return new basecfg({config:config, parentConfig:parent});
		}

	});
	
});
