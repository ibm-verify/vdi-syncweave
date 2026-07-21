Using Tivoli Application Dependency Discovery Manager (TADDM) and TADDM Change Detection Connectors - An Example

This example shows how to use the TADDM Connector to write, read, lookup, update, and remove data and also detect change from the TADDM Change Detection Connector. The example folder includes:
�  TADDMExample.xml - Security Verify Directory Integrator 7.2 configuration file, which contains six AssemblyLines as described in the following sections.
�  Readme.txt - provides information on how to run the example configuration file.
�  Input folder consisting of Machines.csv and Machines_update.csv files.
 
Prerequisites to Run Example Configuration File 
�  Ensure that the TADDM application is up and running.
�  Check whether you can login to TADDM console from your browser. For example, http://localhost:9430.
�  Copy the example folder contents to your current SDI solution directory. For example, if the current solution directory is <SDI install directory>, the example folder path is: <SDI install directory>/examples/TADDMConnectors/*.*

Running the example configuration file
To import configuration file:
1)  Start the SDI Configuration Editor.
2)  Import the TADDMExample.xml source file. To import a source file:
     a. Go to File ->Import. The Import dialog window appears.
     b. Select IBM Security Verify Directory Integrator ->Configuration from the "Select an import source" list.
     c. Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
     d. In the Configuration file field, browse and select the TADDMExample.xml file.
     e. Click Finish. The New Project dialog window appears.
     f. Specify a project name in the Project name field.
     g. Click Finish.
3)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created.
4)  Edit the Connector configuration for the following parameters for the TADDM setup:
     �   Hostname - provide host name or IP address of the TADDM server.
     �   Username - provide user name of the TADDM Server.
     �   Password - provide password of the TADDM Server.
     �   TADDM SDK - provide the path of the TADDM SDK that needs to be present in the system, where SDI is installed.	
5)  Run the following AssemblyLines:
     �   taddm_add - this AssemblyLine contains a File Connector in iterator mode to read data from the Machines.csv file, and contains TADDM Connector in addonly mode to write data into TADDM.
     �   taddm_lookup - this AssemblyLine contains a File Connector in iterator mode that contains the list of machines, which are searched in TADDM through the TADDM Connector, in lookup mode.
     �   taddm_iterate - reads data from TADDM.
     �   taddm_update - this AssemblyLine contains a File Connector in iterator mode to read data from the Machines_update.csv file, and contains TADDM Connector in update mode to update the data in TADDM.
     �   taddm_cdc - this AssemblyLine contains the TADDM Change Detection Connector, which reads changed data from TADDM. Ensure to provide the correct date and time in the 'Start at' connector parameter.
     �   taddm_delete - this AssemblyLine contains a File Connector in delete mode to read data that needs to be deleted from the Machines.csv file, and contains TADDM Connector in delete mode, which deletes the selected data from TADDM.
	
To run an AssemblyLine:
1)  Select the AssemblyLine and double click.
2)  Click the 'Run in console' button.

For more information on TADDM and TADDM CD Connectors, refer the Security Verify Directory Integrator Reference Guide.
