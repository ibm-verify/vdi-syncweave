This sample configuration shows how you can use some of the scripting functionality of SyncWeave.


This example contains the the following files: 
	rawConnectorScripting.xml, 
	sample.csv, 
	expected_output.txt,
	readme.txt. 

Upon importing the configuration file rawConnectorScripting.xml into your workspace and a TDIProject of your choice, expand the AssemblyLines node, and select the ConnnectorConfigSample assemblyline. In the AssemblyLine Components
pane, select the Hooks checkbox, and then select the component "prolog".  In the edit pane modify the filepaths to correspond to the directory where this sample is installed.
For example the line:  
conn.setParam ( "filePath", "examples/scripting/sample.csv" );
should be changed to "<installation_directory>/examples/scripting/sample.csv"
such that the line may read as follows:
conn.setParam ( "filePath", "C:\Program Files\IBM\TDI\V7.2\examples\scripting\sample.csv" );

To run this demo you must install SyncWeave 7.2.

To run the AssemblyLines:
1. Start the SyncWeave 7.2 Config Editor.
2. Import the rawConnectorScripting.xml file.
3. Select "ConnectorConfigSample" AssemblyLine.
4. Click "Run".

Expected result:
You can see the output in <installation_directory>/examples/scripting/output.txt, and you can compare this result with the expected_output.txt.

Here is a brief description of what each of the components in the rawConnectorScripting.cfg file does:
AssemblyLine->ConnectorConfigSample: In the Prolog part of the AssemblyLine, there is a script which does all the work in the AssemblyLine. The Connector used on input reads a ":" separated text file (sample.csv). Every entry read from the connector is written to another connector for output. This loops until all records have been read on input. 
