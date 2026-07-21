Twitter Connector - A custom connector using the Java-JavaScript framework provided by IBM Security Verify Directory Integrator - An Example

This example contains a custom connector created through scripting and shows how to use the connector 
in the Iterator and AddOnly modes. The example folder includes:
�  TwitterConnectorExample.xml - Security Verify Directory Integrator 7.2 configuration file, 
   which contains two AssemblyLines as described in the following sections and also the 
   scripted Twitter Connector.
�  Readme.txt - provides information on how to run the example configuration file.
�  Input folder contains Status.csv file.

Prerequisites to Run Example Configuration File 
�  Ensure that you register yourself in Twitter and obtain a Twitter Account using the following link:
   http://twitter.com/
�  Ensure that the application Security Verify Directory Integrator is registered to use the OAuth Scheme of Twitter using the following link:
   http://twitter.com/oauth_clients/new
   Ensure that the Application Name, Application Type, Default Access Type and Use Twitter for login fields are filled while
   registering. 
�  Ensure that you acquire Consumer Key, Consumer Secret, Access Token, and Access Token Secret after you register 
   Security Verify Directory Integrator to use the OAuth scheme of Twitter.
�  Ensure that you download the available Twitter4J library (For example: twitter4j-core-2.2.5.jar)
   from http://twitter4j.org/en/index.html and drop it into the <Security Verify Directory Integrator install directory>/jars/3rdParty folder
�  Copy the example folder contents to your current Security Verify Directory Integrator solution directory. 
   For example, if the current solution directory is <Security Verify Directory Integrator install directory>, 
   the example folder path is: <Security Verify Directory Integrator install directory>/examples/TwitterConnector/*.*

Running the example configuration file
To import configuration file:
1)  Start the Security Verify Directory Integrator Configuration Editor.
2)  Import the TwitterConnectorExample.xml source file. To import a source file:
     a. Go to File ->Import. The Import dialog window appears.
     b. Select IBM Security Verify Directory Integrator ->Configuration from the "Select an import source" list.
     c. Click Next. The Import Security Verify Directory Integrator configuration dialog window appears.
     d. In the Configuration file field, browse and select the TwitterConnectorExample.xml file.
     e. Click Finish. The New Project dialog window appears.
     f. Specify a project name in the Project name field.
     g. Click Finish.
3)  In the Navigator panel on the left side of the Configuration Editor window, expand AssemblyLines 
    under the new project you created.
4)  Edit the connector configuration for the following parameters for your Twitter Account:
     �   User Name - provide user name of your Twitter Account.
     �   Access Token - provide Access Token obtained from Twitter.
     �   Access Token Secret - provide Access Token Secret obtained from Twitter.
     �   Consumer Key - provide Consumer Key obtained from Twitter.
     �   Consumer Secret - provide Consumer Secret obtained from Twitter.
5)  Run the following AssemblyLines:
     �   Tweet - this AssemblyLine contains a File Connector in Iterator mode to read data from 
	     the Status.csv file, and contains Twitter Connector in AddOnly mode to tweet into Twitter.
     �   Read Tweets - this AssemblyLine contains the Twitter Connector in Iterator mode to read 
	     tweets from Twitter which were added in the AddOnly mode.
	
To run an AssemblyLine:
1)  Select the AssemblyLine and double click.
2)  Click the 'Run in console' button.