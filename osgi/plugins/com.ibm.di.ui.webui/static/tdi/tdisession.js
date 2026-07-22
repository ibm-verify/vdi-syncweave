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
define([
	"dojo/_base/declare",
	"tdi/tdiapi",
	"tdi/tdiutil",
	"tdi/tdiconfig",
	"tdi/config/connector",
], function(declare, tdiapi, tdiutil, tdiconfig, tdiconnector) {
return declare(
	[],
	{
		openSessionForConnector : function(connector, skipSelect, overrideParams) {
			var cfg = new tdiconfig({});
			var al = cfg.createAssemblyLine("TDIDashboard");
			var clone = tdiutil.clone(connector.config);
			if(clone["@type"] == "connector") {
				clone = {
					"@type": "complex",
					"name": clone.name,
					"initialize": "onStartup",
					"sandboxPlayback": false,
					"sandboxRecord": false,
					"simulateState": "Enabled",
					"state": "Enabled",
					"complexConfig": clone
				};
			}
			this.conn = new tdiconnector({config:clone});
			this.conn.setState("Passive");
			this.conn.parentConfig = connector.parentConfig;
			if(skipSelect)
				this.conn.setMode("Lookup");
			else
				this.conn.setMode("Iterator");
			al.addDataFlowComponent(this.conn);
			
			if(overrideParams) {
				for(var p in overrideParams) {
					if(p == "[type]")
						this.conn.setInheritFrom(overrideParams[p]);
					else
						this.conn.getConnectionConfig().setParam(p, overrideParams[p]);
				}
			}
			
			return tdiapi.startTempConfig(cfg, tdiutil.generateInstanceId())
				.then(dojo.hitch(this, function(atom) {
					this.cientry = new tdi.tdicientry({atom:atom});
					return tdiapi.manualAssemblyLine(this.cientry, "TDIDashboard");
				}))
				.then(dojo.hitch(this, function(atom) {
					this.alentry = new tdi.tdialentry({atom:atom});
					if(skipSelect)
						return 1;
					else
						return tdiapi.executeScript(this.alentry, this.conn.getName() + ".connector.selectEntries()")
				}));
		},
		
		getNextEntry : function() {
			if(this.alentry != null)
				return tdiapi.executeScript(this.alentry, this.conn.getName() + ".connector.getNextEntry()");
			else
				return null;
		},
		
		querySchema : function() {
			if(this.alentry != null)
				return tdiapi.executeScript(this.alentry, this.conn.getName() + ".connector.querySchema(null)");
			else
				return null;
		},
		
		callMethod: function(method) {
			if(this.alentry != null)
				return tdiapi.executeScript(this.alentry, this.conn.getName() + ".connector." + method);
			else
				return null;
		},
		
		executeScript: function(script) {
			if(this.alentry != null)
				return tdiapi.executeScript(this.alentry, script);
			else
				return null;
		},
		
		close : function() {
			if(this.cientry != null) {
				return tdiapi.stopConfig(this.cientry);
			}
		}
	})
});
