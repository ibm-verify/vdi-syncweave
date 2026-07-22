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

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconstants");
dojo.require("tdi.tdiutil");
dojo.require("tdi.tdiconfig");

dojo.provide("tdi.tdisession");

dojo.declare("tdi.tdisession", null, {
	
	constructor : function(args) {
		dojo.safeMixin(this, args);
	},
	
	openSessionForConnector : function(connector) {
		var cfg = new tdi.tdiconfig({});
		var al = cfg.createAssemblyLine("TDIDashboard");
		var clone = tdiutil.clone(connector.config);
		this.conn = new tdi.connector({config:clone});
		this.conn.setState("Passive");
		this.conn.setMode("Iterator");
		al.addDataFlowComponent(this.conn);
		return tdiapi.startTempConfig(cfg, tdiutil.generateInstanceId())
			.then(dojo.hitch(this, function(atom) {
				this.cientry = new tdi.tdicientry({atom:atom});
				return tdiapi.stepAssemblyLine(this.cientry, "TDIDashboard");
			}))
			.then(dojo.hitch(this, function(atom) {
				this.alentry = new tdi.tdialentry({atom:atom});
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
	
	close : function() {
		if(this.cientry != null) {
			return tdiapi.stopConfig(this.cientry);
		}
	}
	
});
