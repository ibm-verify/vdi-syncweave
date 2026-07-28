
This demo runs on MS Windows systems only as it uses MS Access database and JDBC:ODBC bridge. 
If you use another platform, you must create your own database and configure the JDBC settings of the connectors for this database.


Files included with this example: 
jdbc_demo.mdb, jdbc_demo.xml, readme.txt.


To run this demo you must:
o Install SyncWeave.
o Configure a ODBC Data Source called "JDBCDemo" to point the jdbc_demo.mdb file from this package.


There are 4 AssemblyLines in this example package. Each AssemblyLine demonstrating a mode of the connector.
To run any of the AssemblyLines:
1. Start the SyncWeave Config Editor
2. Open the jdbc_demo.xml configuration file
3. Select the AssemblyLine you wish to run
4. Click "Run".

If in any of the AssemlyLines you try to use the 'Select...' button in the 'Connection' tab of JDBCConnector's configuration panel, you will get a java.SQL.Exception. The reason for this exception ([Microsoft][ODBC Microsoft Access 
Driver]Optional feature not implemented) is a limitation of the ODBC driver used to communicate with the Access database. 
Despite this problem the AssemblyLines will run fine and the results will be as expected. If you are using another 
database, no such problem should be experienced. 


Here is a brief description of what each of the AssemblyLines does:

o "JDBC Iterator" - iterates the table "STUDENT" and outputs its content in a CSV format in the file examples/jdbc_demo/iterator.csv.
Result: The content of the examples/jdbc_demo/iterator.csv file must match the data in the "STUDENT" table. 
(verify by comparing to examples/jdbc_demo/iterator_expected.csv)

o "JDBC Lookup" - iterates the table "STUDENT", looks up in table "STUDENT_ADDRESS" for address and outputs the result in a CSV format in the file examples/jdbc_demo/lookup.csv.
Result: The content of the examples/jdbc_demo/lookup.csv file must match the data in the "STUDENT" and "STUDENT_ADDRESS" tables.
(verify by comparing to examples/jdbc_demo/lookup_expected.csv)

o "JDBC AddOnly" - iterates the table "STUDENT", for each student calculates his average mark and adds it in the "STUDENT_RESULT" table. 
You can find the JavaScript code that calculates the average marks: 
SyncWeave Config Editor -> JDBC AddOnly assembly line -> "AddOnly" connector -> click the "AVERAGE_MARK" attribute.
Result: For each record/student from the "STUDENT" table, a new record is now present in the "STUDENT_RESULT" table with its "AVERAGE_MARK" field set to the average value of the fields "MARK_MATH", "MARK_ENGLISH" and "MARK_PHYSICS" from the "STUDENT" table.

o "JDBC Delete" - iterates the table "STUDENT" and for each student deletes his record (average mark) from the "STUDENT_RESULT" table.
Result: All records from the "STUDENT_RESULT" table related to master records from the "STUDENT" table are now deleted.

