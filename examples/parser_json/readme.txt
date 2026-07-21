This is an example using the JSON parser to read and write JSON formatted data.

The example is provided in the JSONParser.xml file. To run the example follow these steps:

1. Launch the configuration editor and select "Open Security Verify Directory Integrator Configuration File..." from the File menu
2. Select the parser_json/JSONParser.xml file from the examples directory
3. Select new project or an existing project and import all items.

You have now have an AssemblyLine called "ReadWrite" in your project. The assemblyline reads some JSON data using the FormEntry connector and writes it back to file called "json_output.txt".

Open the assemblyline and choose "Run in console" from the toolbar. You can also select the "FormEntryConnector" in the assemblyline and use the Connect/Next buttons in the InputMap to look at the hierarchy of the JSON data.