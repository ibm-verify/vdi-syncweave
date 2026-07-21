
This example demonstrates the implementation of a custom Parser by scripting, and its use.
The parser is used with the FileSystem Connector. It reads and writes Entries in the following bracket format: 
[AttributeName1:AttributeValue1, AttributeName2:AttributeValue2, ...]
Each Entry must be placed on a separate line in the text file.

Setup:

This example includes the following files: 
script_parser.xml, 
input_1.txt, 
expected_output_1.xml, 
input_2.xml, 
expected_output_2.txt, 
readme.txt


To run this demo you must have installed IBM Security Verify Directory Integrator.

This package contains a single IBM Security Verify Directory Integrator configuraion file, script_parser.xml. 
It contains both the implementation of the Parser and the AssemblyLines that demonstrate it.

To view the implementation of the Parser go to: IBM Security Verify Directory Integrator Admin -> Parsers -> "BracketParser" -> "Script" tab.
You see the JavaScript implementation of the two functions that have to be implemented in a Parser: "readEntry ()" and "writeEntry ()".

The use of the BracketParser is demonstrated by 2 AssemblyLines:
1. The first AssemblyLine reads data from an XML file (input_1.xml) and writes it tp a text file in bracket format (output_1.txt). The AssemblyLine configuration can be seen from: 
IBM Security Verify Directory Integrator Config Editor-> [Your project] -> AssemblyLines -> "XML To Bracket" AssemblyLine. 
You can check the output produced output_1.txt  against expected_output_1.txt.
2. The second AssemblyLine reads data from a text file in bracket format (input_2.txt) and writes it to a file in XML format (output_2.xml). The configuration can be seen from: 
IBM Security Verify Directory Integrator Config Editor-> [Your project] -> AssemblyLines -> "Bracket To XML" AssemblyLine. 
You can check the output produced output_2.xml against the expected_output_2.xml.

To run any of the two AssemblyLines:
1. Start the IBM Security Verify Directory Integrator Config Editor
2. Import the script_parser.xml file.
3. Go to the "AssemblyLines" section.
4. Select the AssemblyLine you choose ("Bracket To XML" or "XML To Bracket").
5. click "Run" icon.
