IBM Security Verify Directory Integrator Audit Example
======================

Description:
----------------------
The IBM Security Verify Directory Integrator Audit example consists of an IBM Security Verify Directory Integrator configuration (audit2W7.xml), which receives audit information in form of notifications and stores it in .csv files by first transforming it in W7 format.

The config contains the following two AssemblyLines:
	- notification2W7
	- zxW7FileConnector
 
The first one iterates on a Notification Connector, which is configured to receive audit information about all auth* events and maps them to the appropriate W7 fields using an AssemblyLine Connector.
The second one is supplementary and is called by the AssemblyLine Connector. It contains a File System Connector responsible for storing the mapped data in .csv files as well as scripts for better administration of the results.

Prerequisites:
----------------------
In order to use the audit capabilities of the IBM Security Verify Directory Integrator Server the property "api.audit.on" in global/solution.properties must be set to "true" and the suppression of desired events in global/solution.properties should be disabled.
By default all IBM Security Verify Directory Integrator audit notifications are suppressed, thus the property api.notification.suppress should be commented or modified appropriately.
For example setting api.notification.suppress=di.server.api.authorize.* will allow the user to receive all notifications, which are not of the above defined types. Thus the IBM Security Verify Directory Integrator Server will broadcast audit events of type e.g.: di.server.api.authenticate.   
 

Configure the Server Notifications Connector:
----------------------
There are two options for running the configuration � inside the samIBM Security Verify Directory Integratoror Server instance or using differenIBM Security Verify Directory Integratoror instance. According to this the Server Notifications Connector should be run in local/ correspondingly remote connection type.
By default the Connector is configured to receive notifications from the local IBM Security Verify Directory Integrator Server, when they are not suppressed. Thus the Audit configuration could be automatically loaded by the same IBM Security Verify Directory Integrator Server it audits after setting an appropriate value to the com.ibm.di.server.autoload property in global/solution properties. Loading the configuration in the same IBM Security Verify Directory Integrator instance though carries the risk of not processing all audit events since the Notifications Connector stores the notifications it receives in an internal queue. Thus when stopping the server or performing a shutdown request, some of the messages in the queue might not be processed and will be lost.
The solution of the above mentioned problem is to have a separate IBM Security Verify Directory Integrator instance running the Audit config; for example executing ibmdisrv command from a different solution folder:
 ibmdisrv �s <Path to Solution Dir> �i �c <Path to Audit Configuration>\audit2W7 �r notification2W7 . 
By this means the audited server does not need to take care of its auditing and the generated data will be processed even if it is stopped. Another advantage of this approach is the ability to apply more restrictive security measures to the second IBM Security Verify Directory Integrator instance and thus reduce the exposure to malicious internal IBM Security Verify Directory Integrator developer that otherwise the audit mechanism would have when running in the same instance.
The inconvenience of this approach is the more administrative work that should be performed. The Server Notifications Connector should be configured to communicate with the audited IBM Security Verify Directory Integrator Server and the Audit config must be started/restarted manually each time the audited server is started.
The audit notifications are recognized by the Connector as Custom Notifications, so the �Use custom notification� must be checked and the types of the desired events should be listed. By default all audit notification types are listed.

Configure the AssemblyLine Connector:
----------------------
The AssemblyLine Connector (W7FileConnector) performs mapping of the Attributes in each notification to the predefined W7 fields. 
It also supplies the auxiliary AssemblyLine (zxW7FileConnector) with the necessary information for storing the results.
The actual mapping in the output map of the AssemblyLine Connector follows the schema:
�	when
	Mapped to event.userData.eventDate Attribute in the W7 specified format.
�	whorealname, whologonname
	Both attributes are given the same value - event.userData.logonname.
�	whatverb, whatnoun 
	The first field whatverb reveals the performed action. By authentication audits it is always given the value -�authenticate�. Whatnoun is then set to �user�. When performing authorization these two Attributes represent an actiIBM Security Verify Directory Integratortegrator object IBM Security Verify Directory Integratortegrator object itself (e.g. whatverb = �start�; whatnoun = �AssemblyLine�)
�	whatsuccess
	its field can take two different values - "success" - when the Attribute event.userData.success returns �true� and "failure" otherwise.
�	wheretype, wherename
	Both fields give information about the machine, where the IBM Security Verify Directory Integrator Server is running. Wheretype is mapped to the concatenation of the Attributes event.userData.os.name and event.userData.os.version. The other Attribute contains the value of event.userData.hostname.
�	wheretotype, wheretoname
	The same information as in the above fields (wheretype, wherename) is presented.
�	wherefromtype, wherefromname
	The field wherefromname stores the IP address of the client. Thus it is supplied with the value of event.userData.clientIP.
	By local session wherefromtype is equal to wheretype otherwise is left blank since acquiring information about the remote client operating system through RMI is not usual task.
�	onwhattype
	It is populated with the type of the session - event.userData.session.type.
�	onwhatpath, onwhatname
	These two Attributes are left empty, where not relevant, for example when performing authentication. Otherwise onwhatpath is filled in with the physical path to the IBM Security Verify Directory Integrator instance or the name of the Java class, when auditing custom code invocation (event.userData.path). The value of onwhatname is the name of the IBM Security Verify Directory Integrator object (event.userData.name). The value could be: IBM Security Verify Directory Integrator Server ID, ConfigInstance ID, AL Name or Java Method (by custom code invocation).
�	info
	Any specific relevant information to the concrete notification, which is not available in the previous Attributes, can be passed to the Entry through the info Attribute. By authentication audit points for example, it stores the authentication type (LDAP, SSL, etc�) in format:
 	�Type of authentication:<the performed authentication>�.
	By authorization audit points the interface name and the method, where the audit point is, are presented. The format is as follows:
	�Authorization in:<interface name of the authorization class>.<authorization method>�.


W7FileConnector allows also customization of the output. For this purpose the following parameters should be appropriately configured:
1. "Insight Import directory� parameter
This parameter specifies the name of the directory, where the files with W7 formatted audit data reside.The defined directory should exist before starting the configuration.The default value is: �TDIAudit\Insight_W7\Insight Import Dir�
2. "File creation directory� parameter
This parameter specifies the name of the directory, where the W7 formatted audit data is temporarily kept before it is transferred to �Insight Import directory�. The data here is stored in the file �TempEvents.csv�. The defined directory should exist before starting the configuration.The default value is: �TDIAudit\Insight_W7\W7 Creation Dir�.
3. "How many events per file� parameter
This parameter specifies the number of events in each file in �Insight Import directory�. Before this number is reached the information is stored in the �File creation directory�. It is taken into account even if the specified time in �4. Minutes before sending events to Insight� has elapsed.
The input should be an integer value, otherwise an error is logged. The default value is: �100�
4. "Minutes before sending events to Insight� parameter
This parameter specifies the time to wait in minutes before events are transferred from �File creation directory� to �Insight Import directory�. It is taken into account even if the specified number of events in �3. How many events per file� is reached.
The input should be an integer value, otherwise an error is logged.
The default value is: �0�
5. "Prefix for W7 import files� parameter
This parameter specifies the prefix used in the names of the stored files in �Insight Import directory�. The names of the above mentioned files are created as concatenation of the �5.Prefix for W7 import files� and the current date, saved as long value. 
The default value is: �TDI_Audit_Events_�.

Description of the "zxW7FileConnector" AssemblyLine:
----------------------
The AssemblyLine contains a File System Connector paired with CSV Parser as well as several scripts, which are used to save W7 formatted data in flat files.
The scripts use the parameters set in the "W7FileConnector" to customize the output.
All W7 formatted events are stored first in a �TempEvent.csv� file, which is created in the �File creation directory�. After the predefined number of events is reached and the time specified by the �Minutes before sending events to Insight� has passed, the events are moved to the �Insight Import directory�. The names of the files are built in the manner: �<Prefix for W7 import files>_<current date saved as long>.csv� e.g.( TDI_Audit_Events_1185866129767.csv).
Note that all the directories specified as values of the parameters should exist before starting the AssemblyLine "notification2W7". 


 


