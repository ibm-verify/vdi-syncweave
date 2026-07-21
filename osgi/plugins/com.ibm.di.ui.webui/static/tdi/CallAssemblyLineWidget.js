define([
    "dojo/_base/declare",
    "dijit/form/TextBox",
    "dijit/_HasDropDown",
    "tdi/ConfigBrowser"
], function(declare, TextBox, _HasDropDown, ConfigBrowser){
    return declare([TextBox, _HasDropDown], {
    	
    	dropDown: null,
    	
        isLoaded: function(){
            // Returns whether or not we are loaded - if our dropdown has an href,
            // then we want to check that.
            var dropDown = this.dropDown;
            return (!!dropDown && (!dropDown.href || dropDown.isLoaded));
        },

        loadDropDown: function(callback){
            // Loads our dropdown
        	var t = this;
            var dropDown = this.dropDown;
            if(!dropDown){ 
            	this.dropDown = dropDown = new ConfigBrowser({
            		onChange:function() {
            			t.set("value", dropDown.get("value"));
            		}
            	});
            	dropDown.set("value", this.get("value"));
            }
            callback();
        }
    });
});