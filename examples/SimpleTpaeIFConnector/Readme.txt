Using Simple TPAE IF Connector � An Example

This example shows how Simple TPAE IF Connector communicates with Tivoli Assets Management for IT (TAMIT) to exchange data. The example folder for Simple TPAE IF Connector includes the following files:
�  assets.txt
�  SimpleTpaeIFConnector_example.properties
�  expected_assets_output.txt
�  SimpleTpaeIFConnector_example.xml
�  Readme.txt

Prerequisites to run the example configuration files
�  Check to ensure that the TAMIT application is up and running.
�  Check whether you can login to Maximo console from your browser. For example, http://localhost:9080/maximo. 
�  Copy the example folder to your current SDI solution directory. For example, if the current solution directory is <SDI install directory>, the example folder path is: 
   <SDI install directory>\examples\SimpleTpaeIFConnector\*.*

Running the example configuration files
To run the configuration files:
1)  Start the SDI Configuration Editor.
2)  Import the SimpleTpaeIFConnector_example.xml source file. To import a source file:
      a. 	Go to File ->Import. The Import dialog window appears.
      b.	Select SyncWeave ->Configuration from the Select an import source list.
      c.	Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
      d.	In the Configuration File field, browse and select the SimpleTpaeIFConnector_example.xml file.
      e.	Click Finish. The New Project dialog window appears.
      f.	Specify a project name in the Project name field.
      g.	Click Finish.
3)  For your TAMIT setup, edit the SimpleTpaeIFConnector_example.properties file for the following parameters:
       �	baseURL
       �	userid 
       �	password
	Note: You can use the same user ID and password that you used to connect to Maximo console.
4)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created. 
5) Run the following Assemblylines:
       �	addonly - reads input data from a file and writes the data into TAMIT.
       �	lookup - searches for the selected assets in TAMIT.
       �	update - modifies the selected asset details in TAMIT.
       �	delete - deletes the selected assets from TAMIT.
       �	iterator - reads assets from TAMIT for the set query criteria as shown:
	<ASSET>
		<ASSETNUM operator="SW">TDI</ASSETNUM>
	</ASSET>
	This query enables the Simple Tpae IFConnector, in iterator mode, to fetch only the assets whose ASSETNUM value starts with "TDI".
	
To run an AssemblyLine:
1)  Select the AssemblyLine and double click.
2)  Click Run in console.

For more information on Simple Tpae IF Connector, see the reference guide.
