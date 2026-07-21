dojo.provide("tdi.GlobalLogSettings");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Toolbar");
dojo.require("dijit.layout.TabContainer");
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit.TitlePane");
dojo.require("dijit.form.Button");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.NumberTextBox");
dojo.require("dijit.Toolbar");

dojo.require("tdi.tdiapi");
dojo.require("tdi.tdiatom");
dojo.require("tdi.tdiconfig");
dojo.require("tdi.tdiutil");
dojo.require("tdi.NlsMixin");

dojo.declare("tdi.GlobalLogSettings",
	[dijit._Widget,dijit._Templated,tdi.NlsMixin],
	{
		// summary:
		//
	
		// Template variables
		widgetsInTemplate: true,
		templatePath: dojo.moduleUrl("tdi", "templates/GlobalLogSettings.html"),
		modhandle: null,
		
		_refresh : function(settings) {
			dojo.disconnect(this._changeConnections);
			if(settings.enabled)
				settings.enabled = "on";
			else
				settings.enabled = "off";
			this._form.set("value", settings);

			// form controls call the onChange handler in a timeout
			// function. Connect after 10ms to add handlers after setting values. 
			setTimeout(dojo.hitch(this, "connectFormElements"), 10);
		},
		
		connectFormElements: function () {
			// summary:
			//		Attaches the onchange event to the form controls
			//		We do this after a minor timeout to prevent the form controls
			//		to call us when we set thh form values.
			dojo.forEach(this._form.getDescendants(), dojo.hitch(this, function(widget) {
				if(widget.name) {
					this._changeConnections.push(dojo.connect(widget, "onChange", dojo.hitch(this, "_enableLogSubmit")));
				}
			}));
			
		},
		
		_enableLogSubmit : function(value, source) {
			if(this._form && this._form.validate())
				this.LogSubmitBtn.set("disabled", false);
			else
				this.LogSubmitBtn.set("disabled", true);
		},
		
		_updateTS : function(status) {
			if(status == "+ running") {
				this.tsButton.set("label", this.getString("stopTombstones"));
				this._status.innerHTML = this.getString("tombstonesRunning");
				this.tsRunning = true;
			} else {
				this.tsButton.set("label", this.getString("startTombstones"));
				this._status.innerHTML = this.getString("tombstonesNotRunning");
				this.tsRunning = false;
			}
		},
		
		updateSettings : function() {
			var obj = this._form.get("value");
			if(obj.enabled[0] == "on")
				obj.enabled = true;
			else
				obj.enabled = false;
			dojo.when(tdiapi.saveLogSettings(obj), dojo.hitch(this, function() {
				tdiutil.alert(this.getString("settingsSaved"), "Info");
				this.LogSubmitBtn.set("disabled", true);
			}),
			function (err) {
				tdiutil.error(err);
			});
		},
		
		activateTombstones : function() {
			var def = null;
			if(this.tsRunning) {
				def = tdiapi.stopTombstoneManager();
				tdiutil.alert(this.getString("restartRequired"), "Info");
			} else {
				def = tdiapi.startTombstoneManager();
			}
			
			dojo.when(def, dojo.hitch(this, function() {
				dojo.when(tdiapi.getTombstoneManagerStatus(), dojo.hitch(this, "_updateTS", "+ running"));
			}),
			function (err) {
				tdiutil.error(err);
			})
		},
		
		postCreate : function() {
			this._changeConnections = [];
			this.LogSubmitBtn.set("disabled", true);
			dojo.when(tdiapi.getLogSettings(), dojo.hitch(this, "_refresh"));
			dojo.when(tdiapi.getTombstoneManagerStatus(), dojo.hitch(this, "_updateTS"));
		}

});