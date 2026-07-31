Using CCMDB Connector � An Example

This example shows how CCMDB Connector communicates with Change and Configuration Management Database (CCMDB) Server. 
The example folder for CCMDB Connector includes the following files:

�  CCMDBConnectorExample.properties
�  CCMDBConnectorExample.xml
�  Readme.txt
�  CCMDBConnector.jar

Prerequisites to Run Example Configuration Files
�  Ensure that the CCMDB Server is up and running.
�  Ensure that the CCMDB Server has some preloaded sample data for the SYS.COMPUTERSYSTEM class type.
�  Copy the CCMDBConnector.jar file to the <SDI install folder>/jars/connectors folder.
�  Copy the example folder to your current SDI solution directory. For example, if the current solution directory is <SDI install directory>, the example folder path is:
 <SDI install directory>/examples/CCMDBConnector/*.*
 
Running Example Configuration Files
To run the configuration files:
1)  Start the SDI Configuration Editor.
2)  Import the CCMDBConnectorExample.xml source file. To import a source file:
	a.  Go to File ->Import. The Import dialog window appears.
	b.  Select SyncWeave ->Configuration from the Select an import source list.
	c.  Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
	d.  In the Configuration File field, browse to select the CCMDBConnectorExample.xml file.
	e.  Click Finish. The New Project dialog window appears.
	f.  Specify a project name in the Project name field.
	g.  Click Finish.
3)  For your CCMDB Server setup, edit the CCMDBConnectorExample.properties file for the following parameters:
	�  jdbcURL
	�  jdbcDriver
	�  jdbcUsername
	�  jdbcUserPassword
4)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created. 
5)  Run the following Assemblylines:
	�  iterator - reads a configuration item of class type SYS.COMPUTERSYSTEM from CCMDB Server and dumps the attributes returned into a text file, examples/CCMDBConnector/out.txt
	�  lookup - reads examples/CCMDBConnector/out.txt and searches for the configuration item under class type SYS.COMPUTERSYSTEM, based on the specified link criteria
	
To run an AssemblyLine in Configuration Editor:
1)  Select the AssemblyLine and double click to open.
2)  Click Run in console.

For detailed information on CCMDBConnector, see CCMDBConnector.pdf file in the example folder.