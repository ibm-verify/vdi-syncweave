***********************************************************************************************
* IBM Security Verify Directory Integrator Example using LdapServermode connector and Branch component 
* Sample Name: VirtDir
**********************************************************************************************

This example provides the documentation for the VirtDir example.

IN TDI 7.1.x, the MemoryQueue Connector was rewritten to provide performance and memory enhancements. 
In the process, the paging and blockingAdd parameters were removed.  Therefore, element in the virtDir.xml were modified to address these changes.
The versions of virtDir prior to 7.1.0  will result in error without the code changes. 

The files provided in this example are as follows:

1.  readme.txt -- this file

2.  VirtDir.xml - The SDI 10.0.0.6 config file to be opened by SDI. This contains two assembly lines described below.

3.  VirtDir.properties -- The properties file used to configure VirtDir server.



INSTRUCTIONS:
---- Part 1 -- Setup  -----

Overview:
The VirtDir is a SDI configuration that intercepts LDAP calls from an LDAP client, enables modification 
of the expected behaviour � including communicating with other targets, and returns desired data back to the LDAP clients. The VirtDir supports LDAP operations like ADD, DELETE, MODIFY, MODRDN, COMPARE, BIND, REBIND, SEARCH. 
The VirtDir acts like a proxy to LDAP clients. 
As seen in the diagrammatic representation below, the VirtDir intercepts calls from a clients and then forwards them to actual directory 
servers configured at the backend and returns the results back to the clients. The actual ldap servers are thus transparent to the clients.


	
		LDAPClient2            LDAPBackend1
			   \          /
			    \        /
			      VirtDir			
			    /       \
			   /         \
		LDAPClient1           LDAPBackend2


Configuring the VirtDir server:
The mains components used in the VirtDir config are:
1. The LDAPServermode Connector
2. LDAPConnector
3. The MemQConnector

All these components can be configured through the properties file.
The config parameters of these components are to be set as properties in the VirtDir.properties file.
The properties file is documented and the relevant properties for this example are set in the properties file.

Note:
1. This example uses two LDAPConnectors. The suffixes (_1 and _2) in the properties file are used to differentiate the LDAPConnectors.
2. The ldapurl and the binddn and password parameters need to be specified before starting the VirtDir server. 


----   PART 2 -- Run Assemblylines ----

Before running assemblylines:
1. Verify that the filepaths for the VirtDir.properties are pointing to the correct paths for your machine's configuration.
2. Verify that the LDAP URL, login username, and login password settings on the Ldap connectors are correct for your configuration.

Run the VirtDir server through the command line as given below:
>ibmdisrv -c "<path to virtdir.xml>VirtDir.xml" -r "virtDir"

This starts the LDAPServer mode connector. The server mode connector will listen on the port specified by the VIRTDIR_PORT parameter
in the VirtDir.properties file. The VirtDir now can be referenced by the following ldapurl: ldap://<the ipaddress or the hostname>:VIRTDIR_PORT.

The LDAPServerMode connector will start a new thread to handle each client request. The results of the client requests are then sent back to the
clients using the LDAPServerMode connectors response channel.



----   PART 3 -- Solution Flow Explained----

The solution flow:
1. The VirtDir AL contains an LDAPServermode Connector. 
2. The LDAPServermode Connector listens on the  port specified by the VIRTDIR_PORT parameter in the VirtDir.properties file.
3. Branch components are added under the LDAPServermode connector's flow. Each Branch component handles one LDAP operation. 
Each branch has a condition ldap.operation == <ldap_op> which determines the type of LDAP operation being handled.
4. Two library connectors are used to connect to the actual Directories at the backend. This config can be used with two directories
configured at the backend of the VirtDir.
5. For add, delete, modify, modrdn, bind, compare operatiopns: script components are used which drive the library connectors to perform the operations. 
The results of these operations are then sent back to the clients through the LDAPServermode connectors response channel. 
6. When the Operation is search. The searchAL AL gets fired. This AL, has an LDAPConnector in iterator mode and an MemQConnector in add only mode.
The purpose of the MeMQConnector is to buffer the data sent by the ldap connector so that the downstream datasources can process it at their own speed.
6.1 Paged searches are also handled. You can find the code that handles the paged searches in the pageSrch script component under the SEARCH branch component.
7. Script components under the script library provide utility functions and isolate the code for each operation handling. These can be extended to suit your own purpose.
8. Of importance is how the controls are handled. The VirtDir gets the request controls for the operations from the work entry. 
 work.getAttribute("ldap.controls").getValues() returns us the controls set in the ldap operation. An array of controls are then set 
 using [ldap.getLdapContext().setRequestControls(array of controls);]
9. for each ldap operation the attributes from the work object are obtained and used to dynamically set the parameters for the ldap operations.



Please Note:
* Using ldapsearch option of -R ( do not automatically chase referrals) from commandline would not result in 
ignoring of referrals. The referrals would be automatically chased. The reason this happens is that the commandline does not send the 
control [OID:2.16.840.1.113730.3.4.2] to the VirtDir. As a result of this the VirtDir cannot do anything about it. However, if
you have an AL with LDAPConnector and set the same option in the LDAPConnector and run it against VirtDir, the referrals are correctly handled.

************************* end of document ***********************************
