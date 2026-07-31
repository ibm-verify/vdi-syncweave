
This is a simple SyncWeave configuration that demonstrates the SyncWeave Delta functionality.

This demo runs on MS Windows systems (Windows 2000) only as it uses MS Access database and JDBC:ODBC bridge. 
If you use another platform, you must create your own database and configure the JDBC settings of the connectors for this database. 


This example consists of the following files:
deltas.xml, readme.txt, users1.txt, users2.txt, users.mdb, expected_users1.mdb, expected_users2.mdb.


To run this demo you must:
o Install SyncWeave.
o Configure an ODBC datasource named "Deltas" to point to the users.mdb from this package. 
You might want a copy of the users.mdb file so you can reset it and test over (tables are being changed by the AssemblyLines).

Here is a brief description of this demo's workflow:
1. An AssemblyLine reads Entries from a text file in Simple format and populates an empty database. During this operation a System store for the delta functionality is created and all the Entries are stored there.
2. The same AssemblyLine is configured to read data from another text file in Simple format where there are new, deleted and modified records. Using the System store created in step 1, the AssemblyLine updates the database to match the content of the new text file.
The database is synchronized through the use of 3 JDBC Connectors respectively in AddOnly, Update and Delete modes. The Iterator Connector (SyncWeave -> AssemblyLines -> "Deltas" -> "ReadTextFile" connector) using its delta database skips unchanged Entries and marks the others for add, delete or modify.
JavaScript code is placed in:
o SyncWeave -> AssemblyLines -> Deltas -> "DB_INSERT" connector -> "Hooks" tab -> "AddOnly" sub-tab -> "Before Add" event
o SyncWeave -> AssemblyLines -> Deltas -> "DB_UPDATE" connector -> "Hooks" tab -> "Update" sub-tab -> "Before Add" event
o SyncWeave -> AssemblyLines -> Deltas -> "DB_UPDATE" connector -> "Hooks" tab -> "Update" sub-tab -> "Before update" event
o SyncWeave -> AssemblyLines -> Deltas -> "DB_DELETE" connector -> "Hooks" tab -> "Delete" sub-tab -> "Before delete" event


To run this demo:
1. Start the SyncWeave Admin.
2. Open the deltas.xml file
3. Configure the "ReadTextFile" Connector input file: 
SyncWeave -> AssemblyLines -> "Deltas" AssemblyLine -> "ReadTextFile" connector -> click "Connection" -> set "File Path" value to examples/deltas/users1.txt -> click "Close".
4. Click "Run". 
5. users.mdb database is now populated with the Entries from users1.txt file; the content of users.mdb must match exactly the content of expected_users1.mdb.
6. Configure "ReadTextFile" Connector input file: 
SyncWeave -> AssemblyLines -> "Deltas" AssemblyLine -> "ReadTextFile" connector -> click "Connection" -> set "File Path" value to examples/deltas/users2.txt -> click "Close".
7. Click "Run". 
8. users.mdb database is now populated with the Entries from users2.txt file; the content of users.mdb must match exactly the content of expected_users2.mdb.
