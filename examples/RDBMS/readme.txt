This example demonstrates the use of RDBMS Change Detection Connector.

These files are included in the example:
	readme.txt
	BD2Script.txt
	LoadDBTable.txt
	RDBMS_example.xml

This example contains two assembly lines: "LoadAL1" and "ChAL". 
-"LoadAL1" fills the "EMAIL" table in the database with data. This data is read from a file called "LoadDBTable.txt".
-"ChAL" searches for changes in the table and writes them to a file.


To run this example you will need to do following:
1. Configure the database (DB2 used)
	1.1 Run the "Command Line Processor" from the Command Line Tools and type in: "create database EXAMPLE".
	1.2 Run the "Command Center" from the Command Line Tools and run the script that is provided in "BD2Script.txt" file

2. Start the SyncWeave CE.
	2.1 Open the "RDBMS_example" file.
	2.2 Go to the "AssemblyLines" section.
	2.3 Select the "LoadAL1" AssemblyLine and click "Run" to fill in the "EMAIL" table in the database.
	2.4 Select the "ChAL" AssemblyLine and click "Run" to write the changes in the "EMAIL" table to a file. These changes are written in the "CCDEMAIL" table in the database.

NOTE: You may need to make a few changes in order to run the assembly lines. 
-Pay attention to the username and the password that you use to connect to the database. In case that they are different change them so that they match the right ones.
-Check the URL of the  "LoadDBTable.txt" and if necessary change it to the proper one.
-If you want not to delete the rows you can uncheck the "Remove Processed Rows" field in the "ChAL" assembly line.
-You may change the "Timeout" to determine the number of seconds to wait for the next 'change' row. If left 0, iteration will be held forever.