Using Deployed Assets (DPA) Connector - An Example

This example shows how DPA Connector can be used to read and write deployed assets from Tivoli Assets Management for IT (TAMIT) database. The example folder for DPA Connector includes the following files:
�  DPAConnector_example.xml - Security Verify Directory Integrator 7.2 configuration file, which contains four Assembly Lines as described in the following sections.
�  Readme.txt - provides information on how to run the example configuration file.

Prerequisites to run the example configuration file
�  Check to ensure that the TAMIT application is up and running.
�  Copy the configuration file from the example folder to your current SDI solution directory. For example, if the current solution directory is <SDI install directory>, the example folder path is: <SDI install directory>/examples/DPAConnector/*.*

Running the example configuration file
To import configuration file:
1)  Start the SDI Configuration Editor.
2)  Import the DPAConnector_example.xml source file. To import a source file:
	a.  Go to File ->Import. The Import dialog window appears.
	b.  Select SyncWeave ->Configuration from the "Select an import source" list.
	c.  Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
	d.  In the Configuration file field, browse and select the DPAConnector_example.xml file.
	e.  Click Finish. The New Project dialog window appears.
	f.  Specify a project name in the Project name field.
	g.  Click Finish.
3)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created.
4)  For your TAMIT setup, edit the DPA Connector configuration for the following parameters:
	�  JDBC URL - provide the JDBC URL to connect to the TAMIT data base.
	�  Username - Provide the user name for the TAMIT database.
	�  Password - provide the password for the TAMIT database. 
5)  Run the following Assemblylines:
	�  add_DeployedAsset - writes the data into TAMIT.
	�  lookup_DeployedAsset - searches for the selected assets in TAMIT.
	�  iterate_DeployedAsset - reads assets from TAMIT.
	�  delete_DeployedAsset - deletes the selected assets from TAMIT.
	 
This example is configured to manage the deployed asset record of "Network Devices". 
	
To run an AssemblyLine:
1)  Select the AssemblyLine and double click.
2)  Click the 'Run in console' button.

For detailed information on DPA Connector, refer to the DeployedAssetsConnector.pdf file in the example folder.
