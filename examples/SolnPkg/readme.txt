----------------------------------------------------------------------
IBM Verify Directory Integration: Solution Packaging Example
----------------------------------------------------------------------
This example demonstrates how to use the newly introduced features
of AL Operations in conjunction with ALFC/ALConnector and Solution Packaging.
For an in-depth explanation of AL Operations and related features it is 
recommended to read the IBM Security Verify Directory Integrator Reference/User Guides.

The files provided with this example are as follows: 
1. customOperations.xml
   This is the IBM Security Verify Directory Integrator config file which contains five AssemblyLines. 
   a) mainAL is the AssemblyLine that exposes the various custom operations, 
      such as ldapadd, ldapdelete, ldapmodify, etc.
   b) call_ldapadd, call_ldapdelete, call_ldapmod, and call_lookup AssemblyLines
      call the "mainAL" in various "modes" by making use of the AL FC or the AL 
      Connector.
2. propFile.properties
   This is the external properties file which contains parameters for
   the LDAP Server or Active Directory that the solution will connect to.

How to run this example
------------------------
1. Open the "propFile.properties" and enter the LDAP Server or Active Directory 
to connect to. Mention the ldap Url, the bind DN, the bind password, the 
base Dn for searches and search filter. All these parameters are read
by the "ldapConnector" in the Connector library.

2. Open the "customOperations.xml" in the IBM Security Verify Directory Integrator Config Editor(CE).

3. Open the "call_ldapadd" AssemblyLine. The call_ldapadd AssemblyLine contains the following components:
   a) PrepareWorkForAdd - This is a simple script that sets the work entry parameters needed to be
      passed to the AL being called. It has the necessary script code for calling ldapadd operation.
      The idea is to set the attributes of the object to be added to LDAP in an entry object and set that
      as the work entry's "attr" attribute. This entry object will be passed to the "mainAL"
      by the next ALConnector. Modify the ldap attributes being set in this script to suit your LDAP Schema
     (inetOrgPerson is actually a standard objectClass so you may only need to edit the dn). 
  b) ALConnector - This connector invokes the "mainAL" in the "ldapadd" mode. 
  c) Run the "call_ldapadd" AssemblyLine - Right click on the "call_ldapadd" in the left navigation area 
     and select "Run".
     
     You should see the entry constructed in the "PrepareWorkForAdd" script being added into the configured 
     LDAP Server / Active Directory.

4. To run the various other custom "modes" of operations - just select the corresponfing AL, modify the DN
   in the script component and run the AL. 
    - The "call_ldapdelete" AL will delete the entry whose DN is specified  in the "PrepareWorkForDel" script.
    - The "call_ldapmod" AL will modify the entry whose DN is specified  in the "PrepareWorkForMod" script.
    - The "call_lookup" AL will get the entry whose DN is specified  in the "PrepareWorkForLookup" script.
   
   The "call_ldapadd" AL uses the AL Connector, whereas the other three ALs make use of the AL FC to trigger the various 
   modes of the "mainAL". Any of them can be used interchangeably in any scenario - this was just an illustration.
   
In-depth details of this example
---------------------------------
As mentioned above, the "mainAL" exposes "custom" operations. These custom operations are
ldapadd, ldapmod, ldapdelete and ldapsearch. An operation can be added to an AL by going to the "Operations"
tab at the AL Level tabs. If you click on the Operations tab for the "mainAL", you can see the 4 operations
defined for the "mainAL". If any one of these operations is selected, then you can see the Input and Output
Attributes defined for that particular operation. 

The input attributes are those which the AL expects the calling AL to provide when it calls this AL. 
The output attributes are those which the "mainAL" will return to the callingAL. For example, if you 
click the "ldapsearch" operation, the defined input attribute is "dn" - i.e the dn which this operation 
will search for, and the output attribute is "returnEntry" which will contain an Entry object containing 
the complete LDAP Object for the passed "dn". 

The same attributes can also be seen in the Output Map and Input Map of the fc_lookup function component 
in the "call_lookup" AL. The Output Map of the "call_lookup" fc_lookup component contains "dn" and the Input Map of 
the fc_lookup component contains "returnEntry" indicating that "dn" will be sent out by the fc_lookup 
and "returnEntry" will be received by the fc_lookup component.

The "PrepareWorkForLookup" script component of the "call_lookup" sets those parameters in the work entry which the 
next FC (or AL Connector) defines in its Output Map, so that the correct data (attributes) are passed to
the AL being called. 

AL FC, AL Connector and Operations
----------------------------------
The AL FC and AL Connector were improved in IBM Security Verify Directory Integrator v6.1 to "discover" the operations of an AL.
In this example an AL Connector has been used to run the "mainAL" in ldapadd "mode". The AL FC has been
used to run the "mainAL" in the ldapmod, ldapdelete and ldapsearch "modes". Whenever an AL is selected
and its operation is selected, the appropriate attributes show up in the Input and Output Maps for the
AL FC and AL COnnector.

The AL Connector also has built in intelligence to support the various standard connector modes like
Iterator, Lookup, etc. For that to work there are certain operations that must be exposed by the AL.
For example for Iterator mode to be supported, the AL must expose initialize, selectEntries, getNextEntry,
terminate,etc. These are the same "standard" methods that a connector needs to implement programmatically.
More details on this can be found in the IBM Security Verify Directory Integrator Documentation. This feature is outside the scope
of the current example.

Steps to be carried out to create an AL which exposes operations
-----------------------------------------------------------------
1. Create an AL - say "mainAL".
2. Go to the "Operations" tab of the AL and add an Operation - say "ldapsearch". For that operation mention
   the input and output attributes (in this case they are "dn" and "returnEntry" respectively).
3. Right click on the "Feeds" section of the "mainAL" and select "Add Operation branch..". 
   Mention any name for the operation branch. You will notice that automatically for each operation
   one branch is created. In this example there are 4 branches in the "mainAL" - one for each operation. 
4. Implement your custom functionality for each branch as you would do for normal AssemblyLines. 

Steps required to Publish an AL to a Package
--------------------------------------------
An AL can also now be published for distribution and use in other IBM Security Verify Directory Integrator configs. This allows
developers to share and re-use AssemblyLines without diving deep into the implementation
of the AL. Solution developers can create AssemblyLines that expose custom (and user friendly)
operations, and then Publish there AL's as a package and distribute them to other developers.
The same package can be re-used as an "Adapter" by including that package in the AL Connector
and running the "Adapter" in the exposed "modes". That package can also be called by an 
AL FC. 

1. Right click on an AL to publish and select the "Publish..." menu option. 
   The Package information dialog box pops up. 
2. Mention a unique (any) package Id, and other details like description, author, version, etc.
3. Click on "Save" button on top right hand corner to save this package into the "packages"
   folder of your IBM Security Verify Directory Integrator Installation Directory. 
4. Now this package is ready to be distributed and re-used in other solutions, just like the "mainAL"
   was used by the "call_" ALs. You will notice in the bottom left hand window - there is a tab called
   "Packages" where this newly saved package automatically shows up. Also, in the AL FC's and ALConnector's
   AssemblyLine parameter drop down - users can see all packages present in their packages folder.
   These packages also come up as "Adapters" in the "Select Connector" window.


---------------------
 END OF README
---------------------