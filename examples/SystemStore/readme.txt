
This demo package demonstrates the use of the SystemStore Connector.

These files are included with this example:
systemstore.xml, input.csv, expected_output.txt, readme.txt.

To run this demo you must:
o Install IBM Security Verify Directory Integrator.
o Start the IBM Security Verify Directory Integrator Config Editor.
o Create a new project, e.g. "systemstore".
o Import the file systemstore.xml into the "systemstore" project

This package contains a single IBM Security Verify Directory Integrator configuration file - systemstore.xml. It consists of two AssemblyLines:
1. "Store" AssemblyLine reads a text file in CSV format (input.csv) and stores all Entries in the SystemStore
The configuration of the SystemStore Connector can be seen by selecting IBM Security Verify Directory Integrator CE -> "systemstore" project -> "AssemblyLines" folder -> "Store" AssemblyLine, and then double clicking the "SystemStore" Connector, and selecting the Connection tab. The SystemStore Connector is in Update mode, to make it easier to rerun the AssemblyLine, but it could also have been in AddOnly mode.
2. "Retrieve" AssemblyLine connects to the System Store, receives all Entries there, and writes these Entries in another text file.
The configuration of the SystemStore Connector in this AssemblyLine can be seen by selecting the "Retrieve" AssemblyLine, and then double clicking the "SystemStore" Connector, and selecting the Connection tab. This Connector is set up to delete the table after each run.

The option "Automatically map all attributes" is turned on in the AssemblyLines. The SystemStore Connector assumes that the Attribute "Name" will be present in the Entries, and will create a "SystemStoreDemo" table in the Store AssemblyLine, and store the Entries there. The "Retrive" AssemblyLine reads the Entries from the "SystemStoreDemo" table, and writes them to a file.

To run the demo:
1. Start the IBM Security Verify Directory Integrator CE.
2. Create a project, and inport the systemstore.xml file into the project
3. Select the "Store" AssemblyLine.
4. Correct the File Path if needed in the FileSystemConnector
5. Click "Run".
6. Select the "Retrieve" AssemblyLine.
7. Correct the File Path if needed in the FileSystemConnector
8. Click "Run".
9. Check the content of the generated output.txt against the expected_output.txt included in this package.


