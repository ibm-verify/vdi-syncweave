This example demonstrates how to convert an Entry tagged at value level to a regular Entry and a regular Entry to a Delta Entry.


Explanation:
1. When trying to pass a Delta tagged Entry to a Parser that does not support Delta tags at all or at some levels you may get an incorrect result. If you try to write an Entry tagged on all levels using SPMLv2 Parser all values will be written (including those with operation 'delete'). The reason is that the SPMLv2 Parser does not support Delta tagging on Attribute-Value level. To solve this you can write a script. This is demonstrated in AssemblyLine "Remove_Delta_Tagging_At_Value_Level".
2. If you want to use a Connector in Delta mode and pass it an Entry which is not Delta tagged an exception will be thrown. The reason is that the Delta mode Connector requires a Delta Entry to operate (Delta Entry is a Delta tagged regular Entry). To solve this problem you need to convert the regular Entry to a Delta Entry. AssemblyLine "Regular_Entry_To_Delta_Entry" demonstrates a possible solution.


This example consists of the following files:
delta_tagging.xml, input.ldif, output.xml, usersInfo.txt, users.mdb, readme.txt


This demo provides two AssemblyLines. Here is a brief description of them:
1. "Remove_Delta_Tagging_At_Value_Level" - demonstrates how to convert a Delta tagged Entry to a regular one.
2. "Regular_Entry_To_Delta_Entry" - demonstrates how to modify a regular Entry to a Delta Entry.


To run "Remove_Delta_Tagging_At_Value_Level":
1. Start the IBM Security Verify Directory Integrator Config Editor.
2. Import the delta_tagging.xml file.
3. Open the "AssemblyLine" branch.
4. Select "Remove_Delta_Tagging_At_Value_Level" AssemblyLine.
5. Click "Run"
6. Check the generated output (output.xml).

To run "Regular_Entry_To_Delta_Entry":
1. Start the IBM Security Verify Directory Integrator Config Editor.
2. Import the delta_tagging.xml file.
3. Open the "AssemblyLine" branch.
4. Select "Regular_Entry_To_Delta_Entry" AssemblyLine.
5. Click "Run"
6. Check the generated output (users.mdb).
7. Modify the usersInfo.txt file: Change value of the status with "add", "delete", "modify" or "unchanged" (refer to "Regular_Entry_To_Delta_Entry" workflow). Repeat again points 5 and 6.


NOTE: 
You will need to configure an ODBC datasource named "Deltas" to point to the users.mdb from this package.
Make sure you don't add an user with already existing key in the database.


Here is a brief description of "Remove_Delta_Tagging_At_Value_Level" workflow:
1. Iterate a sample input file "input.ldif".
2. Convert the read Delta tagged Entry to a regular Entry. You can find the JavaScript code that does it in:
IBM Security Verify Directory Integrator -> "Remove_Delta_Tagging_At_Value_Level" AssemblyLine -> select "Remove_Delta_Tagging_At_Value_Level" script
3. Write the Entries to the output file "output.xml" using SPMLv2 Parser.


Here is a brief description of "Regular_Entry_To_Delta_Entry" workflow:
1. Iterate a sample input file "usersInfo.txt".
2. Modify the Entry from a regular Entry to a Delta one:
	2.1. Check the value of the "status" Attribute and set the corresponding value as Delta operation for the Entry. The values are as follows:
	status value -> Entry operation value
	"add" -> "add"
	"delete" -> "delete"
	"modify" -> "modify"
	<anything else> --> "unchanged"
	You can find the JavaScript code that does it in:
	IBM Security Verify Directory Integrator -> "Regular_Entry_To_Delta_Entry" AssemblyLine -> select "Regular_Entry_To_Delta_Entry" script
3. Write the Delta Entries to "users.mdb" using JDBC Connector.

NOTE:
o "Regular_Entry_To_Delta_Entry" runs on MS Windows systems only as it uses MS Access database and JDBC:ODBC bridge. If you use another platform, you must create your own database and configure the JDBC settings of the connectors for this database. 
o For more detailed information on the relevant topic refer to the online documentation.