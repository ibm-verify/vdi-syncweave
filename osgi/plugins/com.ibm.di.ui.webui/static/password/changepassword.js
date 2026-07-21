define([
	"dojo/_base/declare",
	"dojo/_base/array",
	"dojo/_base/lang",
	"dijit/_Widget",
	"dijit/_TemplatedMixin",
	"dijit/_WidgetsInTemplateMixin",
	"tdi/NlsMixin",
	"dojo/text!./changepassword.html"
], function(declare, array, lang, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls, template) {
	
return declare(
	[ _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, nls],
	{
		templateString : template,
		
		onSubmit: function(newpassword, confirmpassword) {
			var t = this;

			if (newpassword != confirmpassword) {
				alert("Passwords do not match !!");
				return;
			}

            // ISDIDEV-552 password policy regex & disallowed special characters
            // Password must:
            // - Be at least 8 characters
            // - Contain at least one lowercase letter
            // - Contain at least one uppercase letter
            // - Contain at least one number
            // - Contain at least one special character from the allowed set
            var passwordPolicy = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#%^*()_+=\-[\]{}:"\\,.?/]).{8,}$/;
            var disallowedChars = /[<>`$|;&]/;

            // checking for disallowed characters
            if (disallowedChars.test(newpassword)) {
                alert("Password cannot contain any of the following characters: < > ` $ | ; &");
                return;
            }

            // checking for strength    
            if (!passwordPolicy.test(newpassword)) {
                alert("Password must be at least 8 characters long and include uppercase, lowercase, number, and a special character.");
                return;
            }

			dojo.xhrPost({
				url:"index.html",
				postData: {
					action:"changePassword",
					username:"admin",
					newpassword:newpassword,
				}
			}).then(function ok(resp) {
				window.location = "login.html";
			}, function fail(err) {
				t._passwordFrame.invalidLoginDialog.show();
			});

		},
		
		startup: function() {
			const t = this;
			t.inherited(arguments);

			setTimeout(function() {
				const newPasswordField = t._passwordFrame.domNode.querySelector("#idx_form_TextBox_0");
				if (newPasswordField) {
					newPasswordField.type = "password";
				} else {
					// "New password field not found"
				}
			}, 100);

		}
	})
});