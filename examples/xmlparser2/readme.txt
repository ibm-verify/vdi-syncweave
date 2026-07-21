
This example demonstrates how IBM Security Verify Directory Integrator is able to work with various XML documents using the capabilities of the XMLParser. You can find one IBM Security Verify Directory Integrator configuration with the following AssemblyLines defined:

	SimpleXML-filtering - Shows how the XML Parser could be configured to filter unneeded entries when at the same time is unwrapping both Entry and Value Tags. When the AL is executed the XML Parser will return only the three entries which are matched by the configured filter. The returned entries will be output on the standard log.

	SimpleXML-symmetric - Shows the correspondence between the configuration parameters of the XML Parser in input and output mode. The example is using automatic wrapping/unwrapping of both Entry and Value Tags. When executed the AL will produce an XML which is identical to the one provided as input.

	SimpleXML-wildcards - Shows how the XML Parser can use wildcards when parsing XML documents and do unwrapping of Entry and Value Tags at the same time. When the AL is started the XML Parser will retrieve all four entries from the XML document and will output them on the standard log.

	AdvancedXML-filtering - Shows how the XML Parser could be configured to filter unneeded entries. The XML Parser is configured to wrap neither Entry nor Value Tags. When the AL is executed the XML Parser will select all the available Entry elements and then will output those in a different XML structure.

	AdvancedXML-symmetric - Shows the correspondence between the configuration parameters of the XML Parser in input and output mode. The XML Parser is configured to wrap/unwrap neither Entry nor Value Tags. When executed the AL will produce an XML which is identical to the one provided as input.

	AdvancedXML-scripting - Shows how to create hierarchical entry structure in a script and feed it to the XML Parser. The XML Parser is configured to wrap neither Entry nor Value Tags. When executed the AL will use the FormEntryConnector to provide a simple feed of information. That information is used to create a hierarchy which is then provided to a XML Parser that interprets that hierarchical Entry and outputs a XML.


To run this demo you must have installed IBM Security Verify Directory Integrator.

To run the configuration (configs/xmlparser.xml) included in this package, do the following:
	1. start the IBM Security Verify Directory Integrator Configuration Editior.
	2. import the .xml configuration file.
	3. select the desired AssemblyLine.
	4. Click "Run" button.
