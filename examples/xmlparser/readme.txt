
This demo package demonstrates how SyncWeave built-in XMLParser functionality can be extended by script-overriding some of its mechanisms. You can find 2 SyncWeave configurations - one that implements XMLParser when handling 3 levels depth XML documents, and one that implements XMLParser when handling arbitrary depth XML documents.


This example contains the following files:
	3level_xml.xml (configuration file), 
	arbitrary_deep_xml_parser.xml (configuration file),
	3level_xml_in.xml (sample input data), 
	expected_3level_xml_out.xml (expected output data),
	deep_xml_in.xml (sample input data),
	expected_deep_xml_out.xml (expected output data,
	readme.txt (this file).

To run this demo you must have installed SyncWeave.

There are 2 .xml configuration files included in this package (3level_xml.xml and arbitrary_deep_xml_parser.xml). To run any of them:
1 start the SyncWeave.
2 open the .xml configuration file of your choice.
3 select the AssemblyLine.
4 Click "Run" icon.

Here is a brief description of what each of the configurations/AssemblyLines does and where you can find the script code involved:

3level_xml.xml :
The AssemblyLine involves the built-in XMLParser in its input and output modes. By overriding some of the connectors' Hooks it enables the XMLParser to parse 3 levels depth XML documents. The AssemblyLine reads as input an XML document (3level_xml_in.xml) and produces as output the same XML document saved in another file (3level_xml_out.xml). 
You can check the output file (3level_xml_out.xml) against the file expected_3level_xml_out.xml (included in this package).
The JavaScript code can be found on the following 3 places:
	1. "XMLInput" Connector -> Hooks tab -> "Prolog" -> "After Selection" hook.
	2. "XMLInput" Connector -> Hooks tab -> "DataFlow" (Iterator) -> "Override GetNext" hook.
	3. "XMLOutput" Connector -> Hooks tab -> "DataFlow" (AddOnly) -> "Override Add" hook.

arbitrary_deep_xml_parser.xml :
The AssemblyLine involves the built-in XMLParser in its input and output modes. By overriding some of the connectors' Hooks it enables the XMLParser to parse arbitrary depth XML documents. The AssemblyLine  reads as input an XML document (deep_xml_in.xml) and produces as output the same XML document saved in another file (deep_xml_out.xml). 
You can check the output produced (deep_xml_out.xml) against the file expected_deep_xml_out.xml (included in this package).
The JavaScript code can be found on the following 3 places:
	1. "XMLInput" Connector -> Hooks tab -> "Prolog" -> "After Selection" hook.
	2. "XMLInput" Connector -> Hooks tab -> "DataFlow" (Iterator) -> "Override GetNext" hook.
	3. "XMLOutput" Connector -> Hooks tab -> "DataFlow" (AddOnly) -> "Override Add" hook.


