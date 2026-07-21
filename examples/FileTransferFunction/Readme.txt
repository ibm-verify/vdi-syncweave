Using File Transfer Function Component � An Example

Overview
This example shows how to use File Transfer Function Component to transfer files between two systems. The example folder includes: 
 �  FileTransferFCExample.xml
 �  Readme.txt 
 �  FileTransferFC.jar
 
Prerequisites to Run Example Configuration File
 �  Ensure that the source system and the target system are accessible for file transfer operation.
 �  Ensure that the files to be transferred exist on the source system.
 �  Copy the example folder to your current SDI solution directory. For example, if the current solution directory is SDI install directory>, the example folder path is: SDI install directory>/examples/FileTransferFunction/*.*

Running Example Configuration File
To run the configuration file:
 1)  Start the Security Verify Directory Integrator Configuration Editor.
 2)  Import the FileTransferFCExample.xml source file. To import a source file:
       a.  Go to File -> Import. The Import dialog window appears.
       b.  Select IBM Security Verify Directory Integrator -> Configuration from the �Select an import source� list.
       c.  Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
       d.  In the Configuration File field, browse and select the FileTransferFCExample.xml file.
       e.  Click Finish. The New Project dialog window appears.
       f.  Specify a project name in the Project name field.
       g.  Click Finish.
 3)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines under the new project you created. 
 4)  Open the LocalToLocal AssemblyLine and click the Connection tab.
 5)  Configure the File Transfer Function Component connection parameters. For configuration details, refer to the FileTransferFunctionComponent.pdf document.  
 6)  Run the following Assemblyline:
      Local2Local - transfers the specified file from one location to another in the local computer.
	
To run an AssemblyLine using Configuration Editor:
 1)  Select the LocalToLocal AssemblyLine and double click.
 2)  In the console, click Run.

To run an AssemblyLine using command line interface:
 1)  Open the command prompt.
 2)  Go to <SDI install directory> and execute the following command:
     ./ibmdisrv -s <solution dir> -c examples/FileTransferFunction/FileTransferFCExample.xml -r LocalToLocal

Viewing Results
Check whether the specified files have been transferred to the target directory.

File Transfer Function Component Documentation
For detailed information on File Transfer Function Component, see the reference guide.