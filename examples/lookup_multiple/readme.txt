
This is a simple IBM Security Verify Directory Integrator configuration that demostrates how Lookup Connector can handle duplicate Entries.

Note: This demo runs on MS Windows systems (Windows 2000) only as it uses MS Access database and JDBC:ODBC bridge. 
If you use another platform, you must create your own database and configure the JDBC settings of the connectors for this database. 

This example includes the following files:
lookup_multiple.xml, lookup_multiple.mdb, expected_lookup.xml, readme.txt.

To run this demo you must:
o install IBM Security Verify Directory Integrator.
o configure an ODBC datasource named "MultipleLookup" to point to the lookup_multiple.mdb from this package. 

The IBM Security Verify Directory Integrator configuration lookup_multiple.xml from this package contains a single AssemblyLine named "Multiple Lookup". Here is a brief description of its workflow:

1. An Iterator JDBC Connector named "Input" reads Entries from the "PERSON" table. Attributes read are "ID" and "NAME".
2. A Lookup JDBC Connector named "Lookup" looks in the table for "EMAIL" for the "EMAIL_ADDR" Attribute. For each duplicate Entry found its "EMAIL_ADDR" value is added to the AssemblyLine's work Entry "EMAIL_ADDR" Attribute. Thus duplicate Entries (from the "EMAIL" table) results in multiple values "EMAIL_ADDR" Attribute in the AssemblyLine's work Entry. Here are the configuration elements that handle duplicate Entries:
	--The "On Multiple Entries" hook is enabled.  See IBM Security Verify Directory Integrator -> <Configuration name> -> AssemblyLines -> "Mulitple Lookup" AssemblyLine -> "Lookup" Connector -> "Hooks" tab -> "Lookup" sub-tab -> "On Mulitple Entries" Hook.
	--JavaScript code is placed in the "Lookup Successful" Hook to transfer the values of the duplicates to the work Entry's "EMAIL_ADDR" Attribute. See IBM Security Verify Directory Integrator -> <Configuration name> -> AssemblyLines -> "Multiple Lookup" AssemblyLine -> "Lookup" Connector -> "Hooks" tab  -> "Lookup Successful" event.
3. An AddOnly FileSystem Connector named "Output" outputs all Entries ("NAME" and "EMAIL_ADDR" Attributes) into an XML file.


To run this demo:
1. Start the IBM Security Verify Directory Integrator Config Editor
2. Import the lookup_multiple.xml file.
3. Open the "AssemblyLine" branch.
4. Select "Multiple Lookup" AssemblyLine.
5. Click "Run".
6. Check the output generated (lookup.xml) against the expected_lookup.xml file from this package.
