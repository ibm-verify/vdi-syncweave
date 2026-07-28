
This is a simple SyncWeave configuration that demonstrates the SyncWeave logging functionality.

Files included with this example:
logging.xml, expected_dump.xml, dump.txt, readme.txt

To run this demo you must install SyncWeave.

To run this demo:
1. Start the SyncWeave Config Editor
2. Open the logging.xml file.
3. Click "Run".
4. To view the log generated look at the content of the ibmdi.log file which is situated in the server's "logs" directory (e.g. <solution directory>/workspace/TDI Servers/Default.tdiserver.workdir/logs)

Here is a brief description of this demo's workflow:
1. Iterates a sample input file in simple format (the dump.txt file from this package).
2. Copies the content of the input file into an output file in XML format (dump.xml). 
The generated dump.xml matches exactly the expected_dump.xml file included in this demo package. 
3. Dumps to the log file the values of some attributes (both single and multiple values).
You can find the JavaScript code that dumps a single value attribute in:
SyncWeave Config Editor-> "Dump" AssemblyLine -> "Output" connector -> click the attribute named "singleValue".
You can find the JavaScript code that dumps a multiple values attribute in:
SyncWeave Config Editor -> "Dump" AssemblyLine -> "Output" connector -> click the attribute named "multipleValues".
4. After adding an entry in the output file, the demo dumps to the log file the state of the Output connector.
You can find the JavaScript code that dumps the state of the connector in:
SyncWeave Config Editor -> "Dump" AssemblyLine -> "Output" connector -> "Hooks" tab -> "AddOnly" sub-section -> click the "After Add" event.
