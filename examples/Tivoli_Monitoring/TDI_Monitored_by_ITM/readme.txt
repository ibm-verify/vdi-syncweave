Overview:
This example demonstrates integration between SDI and IBM Tivoli Monitoring. The purpose of this example is to present how SDI can be monitored by ITM using the SDI JMX interface.

These files are included in the example:
	readme.txt - provides information about the example
	ITM_configuration.txt - provides detailed information for configuring the ITM agent
	itm_toolkit_agent.xml - ITM agent configuration
	custom_notificationsr.xml
	
Prerequisites:
The following software components are needed for the example:
	1. IBM Tivoli Monitoring Server and Portal
	2. IBM Security Verify Directory Integrator
	3. IBM ITM Agent Builder
	
This demo provides one AssemblyLine. Here is a brief description of it:
"Custom_Notifications" - This AssemblyLine demonstrates how to send custom notifications to ITM.

To run this demo:
1. Start the IBM Security Verify Directory Integrator Config Editor.
2. Import the custom_notifications.xml file.
3. Open the "AssemblyLine" branch.
4. Select "Custom_Notifications" AssemblyLine.
5. Click "Run"
6. Open a web browser and load http://localhost/
7. Fill in the required data for your custom notification and click send.
To send more custom notifications repeat step 7 several times.


Here is a brief description of "Custom_Notifications" workflow:
1. The HTTP Server Connector listens to requests from http://localhost/
2. When http://localhost/ is loaded the HTTP Server Connector checks if any data is filled in the text boxes.
	--If you load http://localhost/ for the first time or you have not entered notification type in the text box the page will be updated telling you that no notification was sent.
	--Otherwise a notification will be sent and the page will be updated telling you that a notification was sent.
	
Note that if you monitor SDI via ITM not only will custom notifications be sent but all other events monitored by ITM, too. This includes event as di.al.start. di.ci.start, etc.