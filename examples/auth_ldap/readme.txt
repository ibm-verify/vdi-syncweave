(If you read this with Notepad, select Edit(or Format)|Wordwrap to better view it.)

This example demonstrates the use of the Server API custom authentication mechanism.

Files included in this example: "ldap_auth.js", "readme.txt".
========================================

The file "ldap_auth.js" contains a ready to use sample JavaScript that performs authentication against an LDAP Server.

To use this example copy the file "ldap_auth.js" in the SDI solution folder and specify api.custom.authentication=ldap_auth.js in global.properties or solution.properties.
The JavaScript code in "ldap_auth.js" will try bind to an LDAP Server with the specified username and password. If the bind operation is successful the script will indicate a successful authentication, otherwise the authentication will be rejected.

Note: Before using this script you'll have to modify the IP address of the LDAP Server (line 3 of "ldap_auth.js"). Also you�ll have to add the user to the registry file of SDI 7.2, example:
[USER]
[ID]:CN=TDI_User,O=IBM,C=US
[ROLE]:admin
[ENDUSER]

