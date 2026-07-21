/**
 * 
 */
define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/NlsMixin",
	"dojo/text!./LDSLogin.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString : template,
		
		onSubmit: function(username, password) {
			var t = this;

			dojo.xhrPost({
				url:"index.html",
				postData: {
					username:username,
					password:password
				},
				handleAs: "text"
			}).then(function (respText) {
				/*
                * ISDIDEV-552 [ISDIPSIRT-5]
                * Handles the post-login response to determine the next navigation step.
                * If `status` is "forcePasswordChange", redirects to the change password page.
                * If the response is not valid JSON (assumed to indicate a successful login), 
                */
				try {
					resp = JSON.parse(respText);
					if (resp.status === "forcePasswordChange") {
						window.location = "password.html";
						return;
					}
				} 
				catch (e) {
					// "Not JSON, assuming HTML response"
					console.warn("Non-JSON response received, assuming successful HTML login.");
				}
				window.location = "index.html";
			}, function fail(err) {
				t._loginFrame.invalidLoginDialog.show();
			});
		},
		
		startup: function() {
			/*
			this._loginFrame.set("loginSubTitle", this.getString("FDS.loginSubtitle"));
			this._loginFrame.set("inactivityMessage", this.getString("FDS.inactivityMessage"));
			this._loginFrame.set("invalidMessage", this.getString("FDS.invalidLogin"));
			this._loginFrame.set("loginCopyright", this.getString("FDS.loginCopyright"));
			*/
		}
	})
});

