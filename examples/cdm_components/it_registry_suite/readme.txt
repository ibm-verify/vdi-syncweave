This example demonstrates how to use the IT registry suite of Components to register CI and Relationship entries in 
the IT registry database. The shown Components are: Init IT registry FC and IT registry Ci and Relationship 
Connector.
These files/folders are included in this example:
 �	input.csv
 �	it_registry_suite.xml 
 �	readme.txt (this document)

To run this example, one must: 
 �	Install Security Verify Directory Integrator 
 �	Import the supplied configuration from examples directory
 �	Provide details how to connect to the IT registry database (e.g. JDBC URL, JDBC Driver, username, password). 
	This can be done either in the Advanced section of each IT registry Component or in the 
	etc/it_registry.properties file.
	
This package contains a single SDI configuration file - it_registry_suite.xml, which consists of one AssemblyLine:
 1. CreateITRegistryBook:
This AssemblyLine consists of a File System Connector configured with a CSV Parser, an Init IT registry FC and 
three IT registry Ci and Relationship Connectors. Two of the IT registry Connectors will register Coniguration 
Items to the IT registry database and the remaining one will register Relationships between these two CIs.
The Init IT registry FC is used to register an MSS (Management Software System) for which the CIs and Relationship 
will be added by the IT registry Connector.
The two CIs considered for this example are sys.ComputerSystem and sys.OperatingSystem and the Relationship which 
exists between them is cdm:installedOn. For this Relationship the sys.OperatingSystem CI acts as source and the 
sys.ComputerSystem  CI acts as target.
The input file (input.csv) consists of the following columns: manufacturer, serial_number, model,os ,os_fqdn. The 
last two columns specfy identity information about an Operating System, while the rest describes a Computer System.

The configuration of any of the IT registry Components can be seen by selecting SyncWeave CE  
-> 'AssemblyLine' section -> 'CreateITRegistryBook' AssemblyLine -> 'RegisterMSS'/'addComputerSystem'/
'addOperatingSystem'/'addInstalledOn', double clicking on it and selecting the Connection tab.

To run the demo:
 1.	Start the SyncWeave CE.
 2.	Select 'File' -> 'Import'.
 3.	Choose 'Configuration' and specify the path to the it_registry_suite.xml 
	(e.g. install_dir/examples/cdm_components/it_registry_suite_/it_registry_suite.xml) in the next panel. 
	Optionally, select project from the dropdown and skip step 4.
 4.	Create a new solution project.
 5.	Click 'Finish'.
 6.	Go to the 'AssemblyLines' section.
 7.	Select the 'CreateITRegistryBook' AssemblyLine.
 8.	Click 'Run'.
 9.	If SDI's solution directory is different from the install directory, modify the file path for File System 
	Connector (ReadCSV) to successfully execute the example.
 10. Check the log if the AssemblyLine has run successfully and the provided CIs/Relationships are registered in 
	IT registry.