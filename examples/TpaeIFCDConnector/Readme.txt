Using Tpae IF Change Detection Connector � An Example

Overview
This example shows how to use Tpae IF Change Detection Connector to receive change notifications on configurable TCP port for HTTP requests, from Maximo based systems. You can run the example configuration file (TpaeIFCDConnectorExample.xml) either using the Configuration Editor or from the command line interface. The example folder includes: 
 �  TpaeIFCDConnectorExample.xml
 �  Readme.txt
 �  TpaeIFCDConnector.jar

Prerequisites to Run Example Configuration File
 �  Ensure that the Maximo server is running. The TAMIT application is used to run the example configuration file.
 �  Check whether you can login to Maximo console from your browser. For example, http://localhost:9080/maximo. 
 �  Configure the change notifications in Maximo server. Refer to the TpaeIFChangeDetectionConnector.pdf document for the Maximo server configuration information.
 �  Copy the example folder to your current SDI solution directory. For example, if the current solution directory is <SDI install directory>, the example folder path is: <SDI install directory>/examples/TpaeIFCDConnector/*.*

Running Example Configuration File
To run the configuration file:
 1)  Start the Security Verify Directory Integrator Configuration Editor.
 2)  Import the TpaeIFCDConnectorExample.xml source file. To import a source file:
       a.  Go to File -> Import. The Import dialog window appears.
       b.  Select IBM Security Verify Directory Integrator -> Configuration from the �Select an import source� list.
       c.  Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
       d.  In the Configuration File field, browse and select the TpaeIFCDConnectorExample.xml file.
       e.  Click Finish. The New Project dialog window appears.
       f.  Specify a project name in the Project name field.
       g.  Click Finish.
 3)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created. 
 4)  Open the Server AssemblyLine and click the Connection tab.
 5)  Configure the Tpae IF Change Detection Connector parameters. For configuration details, see TpaeIFChangeDetectionConnector.pdf document. 
 6)  Run the following Assemblyline:
      Server - listens for HTTP requests over configured TCP port.
	
To run an AssemblyLine using Configuration Editor:
 1)  Select the AssemblyLine and double click.
 2)  In the console, click Run.

To run an AssemblyLine using command line interface:
 1)  Open the command prompt.
 2)  Go to <SDI install directory> and execute the following command:
     ./ibmdisrv -s <solution dir> -c examples/TpaeIFCDConnector/TpaeIFCDConnectorExample.xml -r Server

Viewing Results
The change notifications, from the Maximo Server, are posted as HTTP requests to SDI. The received change notifications are then parsed to SDI Entry format and are logged into the <SDI solution directory>/logs/ibmdi.log file.  

Tpae IF Change Detection Connector Documentation
For detailed information on Tpae IF Change Detection Connector, see the reference guide.

