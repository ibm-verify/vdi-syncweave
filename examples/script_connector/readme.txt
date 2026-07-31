
This demo package demonstrates the implementation of a custom Connector through scripting and use.
The Connector implemented (called SimpleScriptConnector) operates in Iterator, AddOnly and Lookup modes on files in Simple format. 
Note: The SimpleScriptConnector does not use a Parser but itself interprets the Simple format.

Setup:
This example includes the following files: 
	inp_out.xml , lookup.xml (configuration files), 
	input.txt, 
	input.xml,
	lookup_in.txt,
	expected_output.xml,
	expected_output.txt, 
	expected_lookup_out.txt, 
	readme.txt

To run this demo you must install SyncWeave.

This package contains two SyncWeave configuration files - inp_out.xml and lookup.xml. 
They both contain the implementation of the Connector in their "Connector Library" sections as well as AssemblyLines that demonstrate the Connector's use.

1. To view the implementation of the SimpleScriptConnector go to: 
SyncWeave Config Editor -> open inp_out.xml (or lookup.xml) -> Connectors -> "SimpleScriptConnector" Connector -> "Script" tab. 

Here are some notes on the implementation:
o the functions "modEntry()" and "deleteEntry()" are with empty implementations; therefore the SimpleScriptConnector does not work in Update and Delete modes.
o the file (data source) that the SimpleScriptConnector operates with is configured from in the script code. The variable "fileName" is set in the first line of script code - change this value in order to change the input/output file.
o the "findEntry()" method supports only search criteria with operation "equals" ("=").

2. The use of the SimpleScriptConnector is demonstrated by three AssemblyLines:
o The first AssemblyLine reads data from a file in Simple format (input.txt) and writes it to a file in XML format (output.xml). The AssemblyLine configuration can be seen from: 
SyncWeave Config Editor -> open inp_out.xml -> AssemblyLines -> "SimpleScriptConnector Input" AssemblyLine. You can check the output produced (output.xml)  against expexcted_output.xml.
o The second AssemblyLine reads data from a file in XML format (input.xml) and writes it to a file in Simple format (output.txt). The configuration can be seen from: 
SyncWeave Config Editor -> open inp_out.xml -> AssemblyLines -> "SimpleScriptConnector Output" AssemblyLine. You can check the output produced (output.txt) against the expected_output.txt.
Note that SimpleScriptConnector's AddOnly mode actually adds Entries to the file specified, so consecutive calls to this AssemblyLine append data to the output.txt file.
o The third AssemblyLine demonstrates simultaneously SimpleScriptConnector's Iterator, Lookup and AddOnly mode. It reads data from a file in Simple format (input.txt), looks for a new Attribute (e-mail) in another file in Simple format (lookup_in.txt) and outputs the result Entries in a third file in Simple format (lookup_out.txt). The configuration can be seen from: 
SyncWeave Config Editor -> open lookup.xml -> AssemblyLines -> "SimpleScriptConnector Lookup" AssemblyLine. You can check the output produced (lookup_out.txt) against the expected_lookup_out.txt.
Note: SimpleScriptConnector's AddOnly mode actually adds Entries to the file specified, so consecutive calls to this AssemblyLine append data to the lookup_out.txt file.


To run any of the three AssemblyLines:
1. Start the SyncWeave Config Editor.
2. Open the .xml file for the AssemblyLine you have chosen.
3. Go the "AssemblyLines" section (tab).
4. Select the AssemblyLine you have chosen.
5. Click "Run".
