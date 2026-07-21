/**
 * The ActivityMonitor maintains a tree view of active configurations and assemblylines.
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dojo/_base/html",
	"dojo/aspect",
	"dojo/topic",
	"dojo/data/ItemFileWriteStore",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"dijit/layout/BorderContainer",
	"dijit/layout/TabContainer",
	"dijit/layout/ContentPane",
	"dijit/form/Button",
	"dijit/form/CheckBox",
	"idx/form/Select",
	"dijit/form/DropDownButton",
	"dijit/form/Form",
	"dijit/form/Textarea",
	"dijit/form/TextBox",
	"dijit/MenuItem",
	"tdi/ALSchedule",
	"tdi/AttributeMap",
	"tdi/tdiapi",
	"tdi/tdiconstants",
	"tdi/tdiutil",
	"tdi/ldapsync/FormWidget2",
	"tdi/ToolbarLabel",
	"tdi/atom/tdiconfigentry",
	"tdi/atom/tdicientry",
	"idx/dialogs",
	"tdi/ldapsync/LDSSource",
	"tdi/ldapsync/LDSPta",
	"tdi/ldapsync/LDSUtil",
	"tdi/ldapsync/LDSMap3",
	"tdi/ldapsync/LDSJoin",
	"tdi/ldapsync/LDSWriteBack",
	"./FormWidgetFunctions",
	"tdi/NlsMixin",
	"dojo/text!./templates/LDSFlow.html"
], function(declare, array, lang, html, aspect, topic, ItemFileWriteStore, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, BorderContainer, TabContainer, ContentPane, 
		Button, CheckBox, Select, DropDownButton, Form, Textarea, TextBox, MenuItem, ALSchedule, AttributeMap, tdiapi, tdiconstants, tdiutil, 
		FormWidget, ToolbarLabel, tdiconfigentry, tdicientry, idx, LDSSource, LDSPta, LDSUtil, LDSMap, LDSJoin, LDSWriteBack, FormWidgetFunctions, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls ],
	{
		templateString: template,
		oldName: "",
		
		mapFilePerson: "target.person.mapFile",
		mapFileGroup: "target.group.mapFile",
		writebackForms: ["Form_LDAP", "Form_AD", "Form_SUN"],
		
		saveFlow: function() {
		},
		
		cancelFlow: function() {
		},
		
		onClose: function(oldName, flowParams) {
		},
		
		onModify: function(source) {
		},
		
		onSelectSource: function(event) {
			var conn = this.config.getConnector("Input");
			var current = conn.getInheritFrom();
			var oldForm = conn.getConnectionConfig().getParam("source.form");
			if(event != current) {
				conn.setInheritFrom(event);
				this.reapplyDefaults(oldForm, conn.getConnectionConfig().getParam("source.form"));
				this.toggleWriteback();
				this.updateMaps();
				this.updateMirroringEnabled();
				this.onModify(this);
			}
		},

		updateMirroringEnabled: function() {
			var conn = this.config.getConnector("Input");
			if(!conn || !conn.getConnectionConfig) return;
			
			var mirroringEnabled = conn.getConnectionConfig().getParamBoolean("mirroring.enabled", false);
			if(this.forms.Output) {
				var ctl = this.forms.Output.getControl("global.preserveSourceContainers");
				if(ctl) {
					ctl.set("disabled", !mirroringEnabled);
					if(!mirroringEnabled && ctl.get("value")) {
						ctl.set("value", false);
					}
				}
			}
		},
		
		toggleWriteback: function() {
			var input = this.config.getConnector("Input");
			if(!input || !input.getConnectionConfig) return;
			
			var cc = input.getConnectionConfig();
			var hasparam = cc.getParam("supportsWriteback");
			var supported = cc.getParamBoolean("supportsWriteback", false);
			if(!supported && !hasparam) {
				var form = cc.getParam("source.form");
				supported = array.some(this.writebackForms, function(name) {
					return name == form;
				});
			}
			
			// only writeback for non-custom targets
			supported = supported && !LDSUtil.getCustomTarget();
			
			if(supported) {
				if(!this.tabWriteBack) {
					this.WriteBack = new LDSWriteBack({});
					this.WriteBack.setConfig(this.config);
					this.tabWriteBack = new ContentPane({
						title:this.getString("FDS_writeBack"),
						content:this.WriteBack
					});
					this.tabContainer.addChild(this.tabWriteBack);
				}
			} else {
				if(this.tabWriteBack) {
					this.tabContainer.removeChild(this.tabWriteBack);
					this.tabWriteBack = null;
				}
			}
			
		},
		
		reapplyDefaults: function(oldform, newform) {
			// summary:
			//		Changes default values for those properties that hasn't been changed by
			//		the user.
			if(oldform == newform) {
				return;
			}
			var conn = this.config.getConnector("Output").getConnectionConfig();
			var org = LDSUtil.customFlowSettings[oldform];
			var newprops = LDSUtil.customFlowSettings[newform];
			var formprops = {};
			if(org && newprops) {
				for(f in org.source) {
					var curval = conn.getParam(f);
					var newval = newprops.source[f];
					if(curval == org.source[f] && newval && (curval != newval)) {
						formprops[f] = newval;
					}
				}
			}
			this.forms["Output"].setValue(formprops);
		},
		
		onSelectTarget: function(event) {
			var conn = this.config.getConnector("Output");
//			var current = conn.getInheritFrom();
//			if(event != current) {
//				conn.setInheritFrom(event);
//				this.onModify(this);
//			}
//			this.updateEndpointParams(this.config.getConnector("Output"), "Output", this.TargetParams);
		},
		
		onSelectAugment: function(event) {
			var conn = this.config.getConnector("Augment");
			if(conn) {
				conn.getConnectionConfig().setParam("assemblyLine", event);
				this.updateEndpointParams(this.TargetParams, this.config.getTop().getAssemblyLine(event));
			}
		},
		
		onSelectPage: function(event) {
			alert(event);
		},
		
		updateEndpointParams: function(conn, name, target) {
			var t = this;
			var type = tdiapi.getConnectorType(conn);
			if(t.forms[name]) {
				t.forms[name].destroyRecursive();
				t.forms[name] = null;
			}
			
			if(type) {
				tdiapi.getConnectorForm(type).then(function(formdata) {
					t.forms[name] = new FormWidget({
						formData:formdata,
						verticalLayout:true,
						config:conn.getConnectionConfig(),
						hideNullValues:false,
						ldapConfig:LDSUtil.getGeneralSettingsConnector(conn.getTop())
					}).placeAt(target);
					t.forms[name].startup();

					if(name == "Output") {
						t.updateMirroringEnabled();
					}
				});
			}
		},
		
		updateAttributeMap2: function() {
			this.attMap.setConfig(this.config);
		},
		
		updateAttributeMap: function(person) {
			// Target connector we are modifying 
			var conn = this.config.getConnector("Output");
			
			// Current map file/config
			var mapConfig = conn.getConnectionConfig().getParam(person ? this.mapFilePerson : this.mapFileGroup);
			var select = person ? this.personMaps : this.groupMaps;
			var header = person ? this.personHeader : this.groupHeader;
			
			if(mapConfig) {
				select.set("value", mapConfig);
				var map = array.filter(this.mapData, function(item) {
					return item.name == mapConfig;
				})[0];
				
				
				header.set("content", new LDSMap({config:map, parent:this}));
			}
		},
		
		resize: function(obj) {
			if(this.Header) {
				this.Header.resize(obj);
			}
		},
		
		setEndpoints: function(endpoints) {
			
var value = "";
if (this.config) {
  var input = this.config.getConnector("Input");
  value = input ? input.getInheritFrom() : "";
}

			this.sourceEndpoint.setStore(endpoints.sources, value);
			if(this.augmentEndpoint)
				this.augmentEndpoint.setStore(endpoints.sources);
//			this.targetEndpoint.setStore(endpoints.targets);
			this.Join.setEndpoints(endpoints.sources);
		},
		
		setConfig: function(config) {
			var t = this;
			t.config = config;
			
			// REQUIRED HARDENING: Check Input connector validity after reload
			var input = config.getConnector("Input");
			if (!input || typeof input.getConnectionConfig !== "function") {
				console.error("Invalid Input connector after reload", input);
				return;
			}
			
			if(t.Schedule) {
				t.Schedule.setConfig(t.config);
				t.Schedule.onModify = lang.hitch(this, "onModify", "Schedule");
			}
			t.Join.setConfig(config);
			t.WriteBack.setConfig(config);
			t.toggleWriteback();
			t.loadServerMaps();

			// -- Show Output form
			this.updateEndpointParams(this.config.getConnector("Output"), "Output", this.TargetParams);
			
			// -- Detect conflicting simulate/schedule condition and let user know
			t._modid = aspect.after(t.config, "onModify", lang.hitch(this, function(modified, param) {
				if(lang.isArray(param) && param.length == 2) {
					var p = param[1];
					if(p.name == "simulate") {
						var simulate = t.config.getConnector("Output").getConnectionConfig().getParamBoolean("simulate", false);
						if(simulate) {
							var schedule = t.config.getTop().getScheduleForAssemblyLine(t.config.getName());
							if(schedule) {
								schedule = t.config.getTop().getSchedule(schedule);
								if(schedule && schedule.getEnabled()) {
									idx.warn(this.getString("FDS.simulateWithScheduleWarn"));
								}
							}
						}
					}
				}
			}));
		},
		
		setData: function(data) {
			this.Name.set("value", data.name);
			this.oldName = data.name;
		},
		
		loadServerMaps: function() {
			var t = this;
			
			t.personMaps = new Select({});
			t.groupMaps = new Select({});

			t.personMap.addToolbarItem(new ToolbarLabel({label:t.getString("FDS.selectPersonMap")}), 0);
			t.personMap.addToolbarItem(t.personMaps, 1);
			t.groupMap.addToolbarItem(new ToolbarLabel({label:t.getString("FDS.selectGroupMap")}), 0);
			t.groupMap.addToolbarItem(t.groupMaps, 1);
			
			LDSUtil.getServerMaps().then(function(data) {
				t.mapData = data;
				data = array.map(data, function(item) {
					item.label = item.name;
					item.value = item.name;
					item.id = item.name;
					return item;
				});
				
				t.personMaps.removeOption(t.personMaps.options);
				t.groupMaps.removeOption(t.groupMaps.options);
				
				array.forEach(data, function(map) {
					t.personMaps.addOption(map);
					t.groupMaps.addOption(map);
				});
				
				t.updateMaps();
			}, function(err) {
				console.log("While loading server maps");
				console.log(err);
			});
		},
		
		addMap: function(config) {
			var t = this;
			var arr = array.filter(t.mapData, function(item) {
				return (item.name == config.name);
			});
			if(arr && arr.length == 1)
				return;
			else
				t.mapData.push(config);
		},
		
		getMapFor: function(str) {
			var t = this;
			var arr = array.filter(t.mapData, function(item) {
				return item.value == str;
			});
			if(arr.length == 1)
				return arr[0];
			else
				return null;
		},
		
		updateSourceAttributes: function(map, source) {
			var t = this;
			var m = source.match(/^\/Connectors\/(\w+)/);
			if(m) {
				source = m[1];
			}
			var conn = t.config.getTop().getConnector(source);
			var arr = [];
			if(conn) {
				array.forEach(conn.getSchema().getNames(), function(key) {
					arr.push(key);
				});
			}
			map.set("sourceAttributes", arr);
		},
		
		updateTargetAttributes: function(map, source) {
			var t = this;
			LDSUtil.getServerMap(source).then(function(data) {
				var arr = [];
				for(var f in data.map) {
					arr.push(f);
				}
				map.set("targetAttributes", arr);
			});
		},
		
		updateMaps: function() {
			var t = this;
			var conn = t.config.getConnector("Output");
			var mapConfig = conn.getConnectionConfig().getParam(t.mapFilePerson);
			if(!mapConfig || mapConfig == "")
				mapConfig = "person.map";
			var arr = array.filter(t.mapData, function(item) {
				return item.id == mapConfig;
			});
			if(arr.length > 0) {
				t.personMap.useMap(arr[0]);
				t.personMaps.set("value", arr[0]["id"])
			}
			mapConfig = conn.getConnectionConfig().getParam(t.mapFileGroup);
			if(!mapConfig || mapConfig == "")
				mapConfig = "group.map";
			arr = array.filter(t.mapData, function(item) {
				return item.id == mapConfig;
			});
			if(arr.length > 0) {
				t.groupMap.useMap(arr[0]);
				t.groupMaps.set("value", arr[0]["id"])
			}

			t.personMap.setAutoSaveTo(t.config.getName() + "_person.map");
			t.personMap.setFlowContext(t.config, "person");
			try {
				var inputConn = t.config.getConnector("Input");
				if(inputConn && inputConn.getInheritFrom) {
					t.updateSourceAttributes(t.personMap, inputConn.getInheritFrom());
				}
				t.updateTargetAttributes(t.personMap, "person.map");
	//				t.personMap.set("sourceAttributes", t.config.getComponentByName("Input").getSchema().getNames());
			} catch(err) {
				console.log(err);
			}
			t.personMap.onAutoSaved = function(config) {
				t.addMap(config);
				t.selectOption(t.personMaps, t.personMap.getAutoSaveTo());
				if(conn && conn.getConnectionConfig) {
					var current = conn.getConnectionConfig().getParam(t.mapFilePerson);
					if(current != t.personMap.getAutoSaveTo())
						conn.getConnectionConfig().setParam(t.mapFilePerson, t.personMap.getAutoSaveTo());
				}
			};
			
			t.groupMap.setAutoSaveTo(t.config.getName() + "_group.map");
			try {
				var inputConn = t.config.getConnector("Input");
				if(inputConn && inputConn.getInheritFrom) {
					t.updateSourceAttributes(t.groupMap, inputConn.getInheritFrom());
				}
				t.updateTargetAttributes(t.groupMap, "group.map");
	//				t.groupMap.set("sourceAttributes", t.config.getComponentByName("Input").getSchema().getNames());
			} catch(err) {
				console.log(err);
			}
			t.groupMap.setFlowContext(t.config, "group");
			t.groupMap.onAutoSaved = function(config) {
				t.addMap(config);
				t.selectOption(t.groupMaps, t.groupMap.getAutoSaveTo());
				var current = conn.getConnectionConfig().getParam(t.mapFileGroup);
				if(current != t.personMap.getAutoSaveTo())
					conn.getConnectionConfig().setParam(t.mapFileGroup, t.groupMap.getAutoSaveTo());
			};
			
			if(!t._gmodid) {
				t._pmodid = aspect.after(t.personMaps, "onChange", function(p1, arr) {
					var current = conn.getConnectionConfig().getParam(t.mapFilePerson);
					if(current != arr[0]) {
						conn.getConnectionConfig().setParam(t.mapFilePerson, arr[0]);
						t.personMap.useMap(t.getMapFor(arr[0]));
					}
				});
				t._gmodid = aspect.after(t.groupMaps, "onChange", function(p1, arr) {
					var current = conn.getConnectionConfig().getParam(t.mapFileGroup);
					if(current != arr[0]) {
						conn.getConnectionConfig().setParam(t.mapFileGroup, arr[0]);
						t.groupMap.useMap(t.getMapFor(arr[0]));
					}
				});
			}
			
		},
		
		selectOption: function(select, value) {
			var arr = array.filter(select.options, function(opt) {
				return opt.value == value;
			})
			if(arr.length == 0) {
				select.addOption({id:value, value:value, label:value});
			}
			select.set("value", value);
		},
		
		selectTab: function(title) {
			if(title == "schedule")
				this.tabContainer.selectChild(this.tabSchedule);
		},
		
		postCreate: function() {
			var t = this;
			t.checkPTA = true;
			this.inherited(arguments);
			this.forms = new Object();
			this.own(topic.subscribe(this.tabContainer.id + "-selectChild", function(page) {
				if(page == t.tabWriteBack) {
					t.WriteBack.onTabSelected();
				}
			}));
		}
	})
});