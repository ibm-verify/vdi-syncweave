
This package demonstrates the use of the SOAP Parser. 
Please note that this demo runs on MS Windows systems only as it uses MS Access database and JDBC:ODBC bridge. 
If you use another platform, you must create your own database and configure the settings of the JDBC Connectors for this database. 

This sample constists of the following files:
soap.xml, soap.mdb, soap_in.xml, expected_soap_out.xml, readme.txt.


To run this demo you must:
1. Install SyncWeave.
2. Obtain an JDBC/ODBC driver, this example is configured for sun.jdbc.odbc.JdbcOdbcDriver but you must acquire this driver to run the example.
3. configure an ODBC Data Source called "Soap" to point the soap.mdb file from this package.
You might want a copy of the soap.mdb file so you can reset it and test over (tables are being changed by the AssemblyLines).

The package contains one SyncWeave configuration file (soap.xml) with two AssemblyLines that must be executed in the following order:
1. "SOAP Reader" AssemblyLine reads an Entry from a SOAP file (soap_in.xml) and inserts it in a database table (soap.mdb -> "PRODUCT" table). 
Note: The SOAP specific Attribute "SOAP_CALL" read from the soap_in.xml file is not included in the Attributes List of the "JDBCOutput" Connector. The reason is in the fact that there is no such field in the "PRODUCT" database table.
2. "SOAP Writer" AssemblyLine reads an Entry from the same database table (soap.mdb -> "PRODUCT" table) and writes it in a SOAP file (soap_out.xml). 
Note: The Attribute "SOAP_CALL" of the "SOAPOutput" Connector is added manually and its value is assigned by scripting (SyncWeave Config Editor -> Select the assembly line "SOAP Writer" -> "SOAPOutput" Connector -> "Attribute Map" tab -> "SOAP_CALL" Attribute).

In both reading and writing the SOAP Parser recognizes the data types of the Attributes.

To run the demo:
1. Start the SyncWeave Config Editor
2. Create a new project or select existing one
3. Import the configuration file (soap.xml).
4. Go to the "SOAP Reader" AssemblyLine.
5. Click "Run".
6. You can check the content of the "PRODUCT" database table--it now contains one record with field values obtained from the input SOAP file (soap_in.xml).
7. Select "SOAP Writer" AssemblyLine.
8. Click "Run".
9. Check the generated soap_out.xml against the expected_soap_out.xml file from this package; note that the SOAP Parser has successfully recognized and set the types of the Attributes ("string", "int" and "double").
10. In order to successfully execute the demo again you must: 
	-open the "PRODUCT" table from the soap.mdb database and delete its only record, or
	-replace the soap.mdb with the copy of the clean soap.mdb you have made before running the demo.